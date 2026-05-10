package org.example.utils;

public class GoogleOAuthConfig {

    public static final String CLIENT_ID     = "1068811012999-gv2aid4cn7lr183m88k3c69m279ikmfi.apps.googleusercontent.com";
    public static final String CLIENT_SECRET = "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";
    public static final String REDIRECT_URI  = "http://localhost:9999/callback";
    public static final int    CALLBACK_PORT = 9999;


    public static final String AUTH_URL  = "https://accounts.google.com/o/oauth2/v2/auth";
    public static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    public static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    public static final String SCOPES = "openid email profile";
}