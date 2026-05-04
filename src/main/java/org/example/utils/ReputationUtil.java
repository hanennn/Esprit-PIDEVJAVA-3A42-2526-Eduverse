package org.example.utils;

import org.example.services.MessageService;
import org.example.services.SujetService;

public final class ReputationUtil {
    private ReputationUtil() {}

    public static String calculerReputation(int auteurId, SujetService sujetService, MessageService messageService) {
        int nbSujets = sujetService.countByAuteur(auteurId);
        int nbMessages = messageService.countByAuteur(auteurId);
        int total = nbSujets + nbMessages;
        if (total == 0) return "🌱 Nouveau";
        if (total <= 2) return "🌱 Débutant";
        if (total <= 9) return "⭐ Actif";
        return "🏆 Expert";
    }
}