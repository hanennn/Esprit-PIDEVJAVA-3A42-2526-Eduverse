import os
os.environ["PATH"] = os.path.dirname(os.path.abspath(__file__)) + os.pathsep + os.environ.get("PATH", "")

import whisper

print("[...] Chargement du modele Whisper 'medium'...")
model = whisper.load_model("medium")
print("[OK] Whisper pret !")


def transcrire_audio(chemin_audio: str) -> str:
    result = model.transcribe(
        chemin_audio,
        language="fr",
        task="transcribe",
        fp16=False,
    )
    return result["text"].strip()
