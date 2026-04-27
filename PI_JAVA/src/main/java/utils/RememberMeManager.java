package utils;

import java.util.prefs.Preferences;

public class RememberMeManager {

    private static final Preferences prefs =
            Preferences.userNodeForPackage(RememberMeManager.class);

    private static final String KEY_EMAIL    = "email";
    private static final String KEY_PASSWORD = "passwd";

    public static void saveCredentials(String email, String password) {
        prefs.put(KEY_EMAIL, email);
        prefs.put(KEY_PASSWORD, password);
    }

    public static void clearCredentials() {
        prefs.remove(KEY_EMAIL);
        prefs.remove(KEY_PASSWORD);
    }

    public static boolean hasCredentials() {
        return prefs.get(KEY_EMAIL, null) != null;
    }

    public static String fetchEmail() {
        return prefs.get(KEY_EMAIL, null);
    }

    public static String fetchPassword() {
        return prefs.get(KEY_PASSWORD, null);
    }
}