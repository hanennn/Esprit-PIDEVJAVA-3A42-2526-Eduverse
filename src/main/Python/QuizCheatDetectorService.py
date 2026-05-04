import os
os.environ["OMP_NUM_THREADS"] = "1"
os.environ["MKL_NUM_THREADS"] = "1"
os.environ["OPENBLAS_NUM_THREADS"] = "1"
os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"
os.environ["KMP_INIT_AT_FORK"] = "FALSE"
os.environ["TORCH_NUM_THREADS"] = "1"
os.environ["PYTORCH_NO_CUDA_MEMORY_CACHING"] = "1"

import sys, json, threading, base64, time
import numpy as np

import torch
import torch.nn as nn
torch.set_num_threads(1)
torch.set_num_interop_threads(1)

import mediapipe as mp
import cv2
import joblib

WINDOW_SIZE     = 90
COOLDOWN_FRAMES = 30
MAX_CHEATS      = 2
FRAME_EVERY     = 3
NO_FACE_MAX     = 90

class CheatDetector(nn.Module):
    def __init__(self):
        super().__init__()
        self.lstm1 = nn.LSTM(input_size=4, hidden_size=64, batch_first=True)
        self.drop1 = nn.Dropout(0.3)
        self.lstm2 = nn.LSTM(input_size=64, hidden_size=32, batch_first=True)
        self.drop2 = nn.Dropout(0.3)
        self.fc1   = nn.Linear(32, 16)
        self.relu  = nn.ReLU()
        self.fc2   = nn.Linear(16, 1)
    def forward(self, x):
        x, _ = self.lstm1(x); x = self.drop1(x)
        x, _ = self.lstm2(x); x = x[:, -1, :]
        x = self.drop2(x); x = self.relu(self.fc1(x))
        return self.fc2(x)

def emit(obj):
    print(json.dumps(obj), flush=True)
    sys.stdout.flush()

def terminate():
    emit({"event": "terminated", "count": MAX_CHEATS, "prob": 1.0, "reason": "cheat"})

stop_flag = threading.Event()
def stdin_watcher():
    for line in sys.stdin:
        if line.strip().upper() == "STOP":
            stop_flag.set(); break
threading.Thread(target=stdin_watcher, daemon=True).start()

def frame_to_b64(frame, width=320):
    h, w = frame.shape[:2]
    small = cv2.resize(frame, (width, int(h * width / w)))
    ok, buf = cv2.imencode(".jpg", small, [cv2.IMWRITE_JPEG_QUALITY, 60])
    return base64.b64encode(buf).decode("utf-8") if ok else None

def predict(sequence, model, scaler, classes):
    arr = scaler.transform(np.array(sequence, dtype=np.float32))
    with torch.no_grad():
        prob = torch.sigmoid(model(torch.tensor(arr).unsqueeze(0))).item()
    return str(classes[int(prob > 0.5)]), float(prob)

def open_camera_with_timeout(timeout=8):
    result = [None]
    def try_open():
        cap = cv2.VideoCapture(0, cv2.CAP_DSHOW)  # ADD cv2.CAP_DSHOW
        result[0] = cap
    t = threading.Thread(target=try_open)
    t.daemon = True
    t.start()
    t.join(timeout)
    if t.is_alive() or result[0] is None or not result[0].isOpened():
        return None
    return result[0]

def read_frame_with_timeout(cap, timeout=3):
    result = [False, None]
    def try_read():
        result[0], result[1] = cap.read()
    t = threading.Thread(target=try_read)
    t.daemon = True
    t.start()
    t.join(timeout)
    return result[0], result[1]

def main():
    emit({"event": "ready", "step": "started"})

    dir_path = os.path.dirname(os.path.realpath(__file__))

    try:
        model = CheatDetector()
        model.load_state_dict(torch.load(
            os.path.join(dir_path, "cheat_detector.pth"),
            map_location="cpu",
            weights_only=True
        ))
        model.eval()
        scaler  = joblib.load(os.path.join(dir_path, "scaler.pkl"))
        classes = np.load(os.path.join(dir_path, "label_classes.npy"), allow_pickle=True)
    except Exception as e:
        terminate()
        return

    emit({"event": "ready", "step": "model_loaded"})

    BaseOptions           = mp.tasks.BaseOptions
    FaceLandmarker        = mp.tasks.vision.FaceLandmarker
    FaceLandmarkerOptions = mp.tasks.vision.FaceLandmarkerOptions
    VisionRunningMode     = mp.tasks.vision.RunningMode

    options = FaceLandmarkerOptions(
        base_options=BaseOptions(model_asset_path=os.path.join(dir_path, "face_landmarker.task")),
        running_mode=VisionRunningMode.IMAGE, num_faces=1)

    emit({"event": "ready", "step": "opening_camera"})

    cap = open_camera_with_timeout(timeout=8)
    if cap is None:
        terminate()
        return

    ret, test_frame = read_frame_with_timeout(cap, timeout=5)
    if not ret or test_frame is None:
        try: cap.release()
        except: pass
        terminate()
        return

    emit({"event": "ready"})

    frame_buffer, recording, cooldown = [], False, 0
    cheat_count, last_label, last_prob, frame_idx = 0, "ok", 0.0, 0
    no_face_count = 0

    with FaceLandmarker.create_from_options(options) as landmarker:
        while not stop_flag.is_set():
            ret, frame = read_frame_with_timeout(cap, timeout=3)
            if not ret or frame is None:
                try: cap.release()
                except: pass
                terminate()
                return

            frame_idx += 1
            rgb    = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            result = landmarker.detect(mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb))
            face_detected = bool(result.face_landmarks)

            if face_detected:
                no_face_count = 0
                lms = result.face_landmarks[0]
                li, ri = lms[468], lms[473]
                h, w, _ = frame.shape
                lx, ly = int(li.x*w), int(li.y*h)
                rx, ry = int(ri.x*w), int(ri.y*h)
                cv2.circle(frame, (lx,ly), 6, (0,255,0), -1)
                cv2.circle(frame, (rx,ry), 6, (0,255,0), -1)
                cv2.line(frame, (lx,ly), (rx,ry), (0,200,255), 2)

                if not recording and cooldown <= 0:
                    frame_buffer, recording = [], True
                if cooldown > 0:
                    cooldown -= 1
                if recording:
                    frame_buffer.append((li.x, li.y, ri.x, ri.y))
                    if len(frame_buffer) >= WINDOW_SIZE:
                        recording = False
                        last_label, last_prob = predict(frame_buffer[:WINDOW_SIZE], model, scaler, classes)
                        frame_buffer, cooldown = [], COOLDOWN_FRAMES
                        if last_label == "cheat":
                            cheat_count += 1
                            if cheat_count >= MAX_CHEATS:
                                b64 = frame_to_b64(frame)
                                if b64: emit({"event":"frame","data":b64,"label":last_label,"prob":last_prob,"cheats":cheat_count,"face":True})
                                try: cap.release()
                                except: pass
                                terminate()
                                return
                            else:
                                emit({"event":"cheat","count":cheat_count,"prob":last_prob})
                        else:
                            emit({"event":"ok","prob":last_prob})

                color = (0,255,0) if last_label=="ok" else (0,0,255)
                cv2.putText(frame, f"{last_label.upper()} {last_prob:.0%}", (10,38), cv2.FONT_HERSHEY_SIMPLEX, 0.85, color, 2)
                cv2.putText(frame, f"Cheats: {cheat_count}/{MAX_CHEATS}", (10,72), cv2.FONT_HERSHEY_SIMPLEX, 0.65, (255,255,255), 2)
                progress = len(frame_buffer)/WINDOW_SIZE if recording else 0.0
                bh = frame.shape[0]-10
                cv2.rectangle(frame, (0,bh-8), (frame.shape[1],bh), (50,50,50), -1)
                cv2.rectangle(frame, (0,bh-8), (int(frame.shape[1]*progress),bh), (0,180,255), -1)
            else:
                no_face_count += 1
                remaining = NO_FACE_MAX - no_face_count
                cv2.putText(frame, f"No face detected ({remaining})", (10,38), cv2.FONT_HERSHEY_SIMPLEX, 0.85, (0,165,255), 2)
                if no_face_count >= NO_FACE_MAX:
                    b64 = frame_to_b64(frame)
                    if b64: emit({"event":"frame","data":b64,"label":"cheat","prob":1.0,"cheats":MAX_CHEATS,"face":False})
                    try: cap.release()
                    except: pass
                    terminate()
                    return

            if frame_idx % FRAME_EVERY == 0:
                b64 = frame_to_b64(frame)
                if b64:
                    emit({"event":"frame","data":b64,"label":last_label,"prob":last_prob,"cheats":cheat_count,"face":face_detected})

    try: cap.release()
    except: pass

if __name__ == "__main__":
    main()