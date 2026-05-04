package org.example.utils;

import org.example.entities.Badword;
import org.example.services.BadwordService;
import java.util.List;
import java.util.regex.Pattern;

public final class BadwordFilterUtil {
    private BadwordFilterUtil() {}

    public static class FilterResult {
        public boolean hasViolation;
        public String action; // MASK, BLOCK, ALERT
        public String filtered; // contenu filtré
        public String violatedWord; // le mot qui a déclenché

        public FilterResult(boolean hasViolation, String action, String filtered, String violatedWord) {
            this.hasViolation = hasViolation;
            this.action = action;
            this.filtered = filtered;
            this.violatedWord = violatedWord;
        }
    }

    public static FilterResult filter(String content) {
        if (content == null || content.isBlank()) {
            return new FilterResult(false, null, content, null);
        }

        try {
            BadwordService service = new BadwordService();
            List<Badword> badwords = service.findAllActive();

            String filtered = content;
            String violatedWord = null;
            String maxAction = null;

            for (Badword badword : badwords) {
                Pattern pattern = Pattern.compile("\\b" + Pattern.quote(badword.getWord()) + "\\b", Pattern.CASE_INSENSITIVE);
                if (pattern.matcher(filtered).find()) {
                    violatedWord = badword.getWord();
                    maxAction = getMaxPriorityAction(maxAction, badword.getAction());
                    filtered = pattern.matcher(filtered).replaceAll("***");
                }
            }

            if (violatedWord != null) {
                return new FilterResult(true, maxAction, filtered, violatedWord);
            }
        } catch (RuntimeException e) {
            // Si la DB est indisponible, laisser passer
        }

        return new FilterResult(false, null, content, null);
    }

    private static String getMaxPriorityAction(String current, String next) {
        if (current == null) return next;
        // BLOCK > ALERT > MASK
        if ("BLOCK".equals(next) || "BLOCK".equals(current)) return "BLOCK";
        if ("ALERT".equals(next) || "ALERT".equals(current)) return "ALERT";
        return "MASK";
    }
}
