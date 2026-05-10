import os

try:
    from PyPDF2 import PdfReader
except ImportError:
    PdfReader = None
    print("[WARN] PyPDF2 non installe - analyse CV desactivee")

from emotion_service import classifier, _fallback_analyse

def extraire_texte_pdf(chemin_pdf):
    if PdfReader is None:
        return ""
    reader = PdfReader(chemin_pdf)
    texte = ""
    for page in reader.pages:
        texte += page.extract_text() or ""
    return texte.strip()


def analyser_cv(chemin_pdf):
    texte = extraire_texte_pdf(chemin_pdf)

    if not texte:
        return {
            "texte_extrait": "",
            "competences": [],
            "profil": "CV vide ou illisible",
            "score_pertinence": 0
        }

    mots_cles = {
        "academique": ["licence", "master", "doctorat", "ingenieur", "diplome", "bac", "formation", "universite", "esprit", "etudes"],
        "technique": ["python", "java", "javascript", "sql", "html", "css", "react", "angular", "spring", "machine learning", "ia", "deep learning"],
        "soft_skills": ["leadership", "communication", "equipe", "gestion", "projet", "organisation", "autonome", "responsable"],
        "experience": ["stage", "experience", "travail", "emploi", "poste", "mission", "entreprise", "societe"],
        "langues": ["francais", "anglais", "arabe", "allemand", "espagnol", "italien", "bilingue"]
    }

    texte_lower = texte.lower()
    competences_trouvees = {}
    total_score = 0

    for categorie, mots in mots_cles.items():
        found = [m for m in mots if m in texte_lower]
        competences_trouvees[categorie] = found
        total_score += len(found)

    max_possible = sum(len(v) for v in mots_cles.values())
    score = min(int((total_score / max_possible) * 100), 100)

    texte_court = texte[:500]
    if classifier is not None:
        try:
            results = classifier(texte_court)
            scores_list = results[0] if isinstance(results[0], list) else results
            emotion_dominante = max(scores_list, key=lambda x: x["score"])
            profil_ia = emotion_dominante["label"]
        except Exception:
            profil_ia = "non analyse"
    else:
        profil_ia = "modele non charge"

    if score >= 60:
        profil = f"Candidat avec un profil solide (score {score}/100). Profil IA: {profil_ia}."
    elif score >= 30:
        profil = f"Candidat avec un profil moyen (score {score}/100). Profil IA: {profil_ia}."
    else:
        profil = f"Candidat avec un profil a developper (score {score}/100). Profil IA: {profil_ia}."

    return {
        "texte_extrait": texte[:200] + "..." if len(texte) > 200 else texte,
        "competences": competences_trouvees,
        "profil": profil,
        "score_pertinence": score
    }
