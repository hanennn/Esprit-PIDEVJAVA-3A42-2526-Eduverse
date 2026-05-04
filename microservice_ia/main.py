import os, sys, tempfile

os.environ["PATH"] = os.path.dirname(os.path.abspath(__file__)) + os.pathsep + os.environ.get("PATH", "")

from fastapi import FastAPI, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from whisper_service import transcrire_audio
from emotion_service import analyser_emotions
from audio_features import extraire_features
from cv_service import analyser_cv

app = FastAPI(title="EduVerse Interview IA")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
def health():
    return {"status": "ok", "service": "EduVerse Interview IA"}

@app.post("/analyser-interview")
async def analyser(audio: UploadFile = File(...)):
    with tempfile.NamedTemporaryFile(delete=False, suffix=".webm") as tmp:
        tmp.write(await audio.read())
        tmp_path = tmp.name

    try:
        transcription = transcrire_audio(tmp_path)
        emotions = analyser_emotions(transcription)
        features = extraire_features(tmp_path)
        profil = generer_profil(emotions, features)
        recommandation = generer_recommandation(emotions)

        return {
            "transcription": transcription,
            "scores_emotions": emotions,
            "features_audio": features,
            "profil_global": profil,
            "recommandation": recommandation
        }

    finally:
        if os.path.exists(tmp_path):
            os.unlink(tmp_path)


@app.post("/analyser-cv")
async def analyser_cv_endpoint(cv: UploadFile = File(...)):
    import traceback
    with tempfile.NamedTemporaryFile(delete=False, suffix=".pdf") as tmp:
        tmp.write(await cv.read())
        tmp_path = tmp.name

    try:
        result = analyser_cv(tmp_path)
        return result
    except Exception as e:
        traceback.print_exc()
        return {"error": str(e), "texte_extrait": "", "competences": {}, "profil": "Erreur analyse", "score_pertinence": 0}
    finally:
        if os.path.exists(tmp_path):
            os.unlink(tmp_path)


def generer_profil(emotions: dict, features: dict) -> str:
    dominant = max(emotions, key=emotions.get)
    score = emotions[dominant]

    descriptions = {
        "déterminé": "Étudiant très déterminé",
        "motivé":    "Étudiant très motivé",
        "confiant":  "Étudiant confiant",
        "anxieux":   "Étudiant montrant de l'anxiété",
        "hésitant":  "Étudiant hésitant",
    }
    profil = descriptions.get(dominant, f"Étudiant {dominant}")

    if features.get("taux_hesitations_pct", 0) > 20:
        profil += ", discours avec hésitations fréquentes"
    if features.get("debit_mots_par_min", 130) > 160:
        profil += ", débit de parole rapide"
    elif features.get("debit_mots_par_min", 130) < 90:
        profil += ", débit de parole lent"
    if features.get("energie_vocale") == "élevée":
        profil += ", voix assurée"

    return profil + "."


def generer_recommandation(emotions: dict) -> str:
    det = emotions.get("déterminé", 0)
    mot = emotions.get("motivé", 0)
    anx = emotions.get("anxieux", 0)
    conf = emotions.get("confiant", 0)

    if (det + mot) / 2 > 0.70:
        return "Profil excellent. Candidat très motivé et déterminé. Fortement recommandé pour l'attribution."
    elif conf > 0.65:
        return "Profil solide. Candidat confiant et structuré. Recommandé pour l'attribution."
    elif anx > 0.60:
        return "Candidat montrant de l'anxiété. Un entretien de suivi est conseillé avant la décision finale."
    else:
        return "Profil neutre. Une analyse complémentaire ou un entretien direct est recommandé."
