package com.eduverse.forum.utils;

import java.util.regex.Pattern;

public final class InputValidationUtil {
    private static final Pattern BADWORD_PATTERN = Pattern.compile("^[\\p{L}\\p{N}][\\p{L}\\p{N}_-]*$");

    private InputValidationUtil() {}

    public static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public static boolean isLengthBetween(String value, int min, int max) {
        if (value == null) {
            return false;
        }
        int length = value.trim().length();
        return length >= min && length <= max;
    }

    public static boolean isValidBadword(String value) {
        if (value == null) {
            return false;
        }
        return BADWORD_PATTERN.matcher(value.trim()).matches();
    }
}