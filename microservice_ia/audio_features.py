import librosa
import numpy as np


def extraire_features(chemin_audio: str) -> dict:
    y, sr = librosa.load(chemin_audio, sr=None, mono=True)
    duree = librosa.get_duration(y=y, sr=sr)

    # Detecter les segments de parole (seuil 30dB sous le max = silence)
    intervalles = librosa.effects.split(y, top_db=30)
    nb_segments = len(intervalles)

    # Estimation du debit : ~3 mots par segment
    if duree > 0:
        debit = int((nb_segments * 3) / (duree / 60))
    else:
        debit = 0

    # Hesitations = silences > 1.5s entre deux segments
    silences_longs = 0
    for i in range(len(intervalles) - 1):
        duree_silence = (intervalles[i + 1][0] - intervalles[i][1]) / sr
        if duree_silence > 1.5:
            silences_longs += 1

    taux_hesitations = round(
        (silences_longs / max(nb_segments, 1)) * 100, 1
    )

    # Energie vocale moyenne (RMS)
    rms = float(np.mean(librosa.feature.rms(y=y)))
    if rms > 0.05:
        energie = "élevée"
    elif rms > 0.02:
        energie = "modérée"
    else:
        energie = "faible"

    return {
        "debit_mots_par_min":  debit,
        "taux_hesitations_pct": taux_hesitations,
        "energie_vocale":       energie,
        "duree_secondes":       round(duree, 1),
    }
