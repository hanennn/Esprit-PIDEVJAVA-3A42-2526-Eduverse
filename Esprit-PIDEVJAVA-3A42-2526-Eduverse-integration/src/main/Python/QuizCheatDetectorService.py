import os
import sys
import threading
import base64
import time
import numpy as np

os.environ["OMP_NUM_THREADS"] = "1"
os.environ["MKL_NUM_THREADS"] = "1"
os.environ["OPENBLAS_NUM_THREADS"] = "1"
os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"
os.environ["KMP_INIT_AT_FORK"] = "FALSE"
os.environ["TORCH_NUM_THREADS"] = "1"
os.environ["PYTORCH_NO_CUDA_MEMORY_CACHING"] = "1"
os.environ["OPENCV_VIDEOIO_PRIORITY_MSMF"] = "0"

import torch
import torch.nn as nn
torch.set_num_threads(1)
torch.set_num_interop_threads(1)

import mediapipe as mp
import cv2
import joblib


def emit(token, *fields):
    sys.stdout.write(":".join([token] + [str(f) for f in fields]) + "\n")
    sys.stdout.flush()


WINDOW_SIZE = 90
COOLDOWN_FRAMES = 90
MAX_CHEATS = 4
FRAME_INTERVAL = 3.0
NO_FACE_MAX = 90
DEBUG = False


class CheatDetector(nn.Module):
    def __init__(self):
        super().__init__()
        self.lstm1 = nn.LSTM(4, 64, batch_first=True)
        self.drop1 = nn.Dropout(0.3)
        self.lstm2 = nn.LSTM(64, 32, batch_first=True)
        self.drop2 = nn.Dropout(0.3)
        self.fc1 = nn.Linear(32, 16)
        self.relu = nn.ReLU()
        self.fc2 = nn.Linear(16, 1)

    def forward(self, x):
        x, _ = self.lstm1(x)
        x = self.drop1(x)
        x, _ = self.lstm2(x)
        x = x[:, -1, :]
        x = self.drop2(x)
        x = self.relu(self.fc1(x))
        return self.fc2(x)


def terminate():
    emit("TERMINATED", MAX_CHEATS, "1.00")


stop_flag = threading.Event()


def frame_to_b64(frame, width=320):
    h, w = frame.shape[:2]
    img = cv2.resize(frame, (width, int(h * width / w)))
    ok, buf = cv2.imencode(".jpg", img, [cv2.IMWRITE_JPEG_QUALITY, 60])
    return base64.b64encode(buf).decode() if ok else None


def predict(seq, model, scaler, classes):
    arr = scaler.transform(np.array(seq, dtype=np.float32))
    with torch.no_grad():
        prob = torch.sigmoid(model(torch.tensor(arr).unsqueeze(0))).item()
    return str(classes[int(prob > 0.5)]), float(prob)


def open_camera():
    cap = cv2.VideoCapture(0, cv2.CAP_DSHOW)
    cap.set(cv2.CAP_PROP_BUFFERSIZE, 1)
    return cap if cap.isOpened() else None


def read_frame(cap):
    return cap.read()


def main():

    emit("READY", "started")

    path = os.path.dirname(os.path.abspath(__file__))

    try:
        model = CheatDetector()
        model.load_state_dict(torch.load(os.path.join(path, "cheat_detector.pth"), map_location="cpu"))
        model.eval()

        scaler = joblib.load(os.path.join(path, "scaler.pkl"))
        classes = np.load(os.path.join(path, "label_classes.npy"), allow_pickle=True)

    except Exception:
        terminate()
        return

    emit("READY", "model_loaded")

    BaseOptions = mp.tasks.BaseOptions
    FaceLandmarker = mp.tasks.vision.FaceLandmarker
    FaceLandmarkerOptions = mp.tasks.vision.FaceLandmarkerOptions
    RunningMode = mp.tasks.vision.RunningMode

    options = FaceLandmarkerOptions(
        base_options=BaseOptions(model_asset_path=os.path.join(path, "face_landmarker.task")),
        running_mode=RunningMode.IMAGE,
        num_faces=1
    )

    emit("READY", "opening_camera")

    cap = open_camera()
    if cap is None:
        terminate()
        return

    emit("READY")

    buffer = []
    recording = False
    cooldown = 0
    cheat_count = 0
    last_label = "ok"
    last_prob = 0.0
    no_face = 0
    last_time = 0

    with FaceLandmarker.create_from_options(options) as landmarker:

        while not stop_flag.is_set():

            ok, frame = read_frame(cap)
            if not ok:
                break

            rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            result = landmarker.detect(mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb))

            face = bool(result.face_landmarks)

            if face:

                no_face = 0
                lms = result.face_landmarks[0]
                li, ri = lms[468], lms[473]

                if cooldown > 0:
                    cooldown -= 1

                if not recording and cooldown == 0:
                    buffer = []
                    recording = True

                if recording:
                    buffer.append((li.x, li.y, ri.x, ri.y))

                    if len(buffer) >= WINDOW_SIZE:
                        recording = False
                        last_label, last_prob = predict(buffer[:WINDOW_SIZE], model, scaler, classes)
                        buffer = []
                        cooldown = COOLDOWN_FRAMES

                        if last_label == "cheat":
                            cheat_count += 1
                            emit("CHEAT", cheat_count, f"{last_prob:.2f}")

                            if cheat_count >= MAX_CHEATS:
                                b64 = frame_to_b64(frame)
                                if b64:
                                    emit("FRAME", b64, last_label, f"{last_prob:.2f}", cheat_count, "1")
                                break
                        else:
                            emit("OK", f"{last_prob:.2f}")

            else:
                no_face += 1
                if no_face >= NO_FACE_MAX:
                    b64 = frame_to_b64(frame)
                    if b64:
                        emit("FRAME", b64, "cheat", "1.00", MAX_CHEATS, "0")
                    break

            now = time.monotonic()
            if now - last_time > FRAME_INTERVAL:
                last_time = now
                b64 = frame_to_b64(frame)
                if b64:
                    emit("FRAME", b64, last_label, f"{last_prob:.2f}", cheat_count, "1" if face else "0")

            if DEBUG:
                cv2.imshow("debug", frame)
                if cv2.waitKey(1) & 0xFF == ord("q"):
                    break

    cap.release()
    cv2.destroyAllWindows()
    terminate()


if __name__ == "__main__":
    main()
