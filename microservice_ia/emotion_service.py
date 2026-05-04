from transformers import pipeline
import os

MODEL_PATH = "./mon_modele_bourse"

if os.path.exists(MODEL_PATH):
    print(f"[OK] Chargement du modele fine-tune : {MODEL_PATH}")
    classifier = pipeline(
        "text-classification",
        model=MODEL_PATH,
        tokenizer=MODEL_PATH,
        top_k=None,
    )
    LABELS = None
else:
    print("[WARN] Modele fine-tune introuvable -- utilisation du mode fallback")
    classifier = None

EXPECTED_LABELS = ["déterminé", "anxieux", "confiant", "hésitant", "motivé"]


def analyser_emotions(texte: str) -> dict:
    if classifier is None:
        return _fallback_analyse(texte)

    results = classifier(texte)
    scores_list = results[0] if isinstance(results[0], list) else results
    return {r["label"]: round(r["score"], 3) for r in scores_list}


def _fallback_analyse(texte: str) -> dict:
    texte_lower = texte.lower()

    mots_determine  = ["déterminé", "convaincu", "sûr", "certain", "absolument", "engagé"]
    mots_motiv      = ["motivé", "passion", "envie", "objectif", "ambition", "réussir"]
    mots_confiant   = ["confiant", "capable", "compétent", "expérience", "mérite"]
    mots_anxieux    = ["inquiet", "nerveux", "peur", "anxieux", "stress", "difficile"]
    mots_hesitant   = ["peut-être", "je ne sais", "hm", "euh", "bof", "pas sûr"]

    def score(mots):
        hits = sum(1 for m in mots if m in texte_lower)
        return round(min(hits / max(len(mots), 1), 1.0), 3)

    scores = {
        "déterminé": score(mots_determine),
        "motivé":    score(mots_motiv),
        "confiant":  score(mots_confiant),
        "anxieux":   score(mots_anxieux),
        "hésitant":  score(mots_hesitant),
    }

    total = sum(scores.values()) or 1
    return {k: round(v / total, 3) for k, v in scores.items()}
