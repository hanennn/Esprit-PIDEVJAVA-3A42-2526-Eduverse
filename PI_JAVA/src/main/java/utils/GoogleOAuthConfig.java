package utils;

public class GoogleOAuthConfig {

    public static final String CLIENT_ID     = "Client ID ";
    public static final String CLIENT_SECRET = "Client Secret";
    public static final String REDIRECT_URI  = "http://localhost:8888/callback";
    public static final int    CALLBACK_PORT = 8888;


    public static final String AUTH_URL  = "https://accounts.google.com/o/oauth2/v2/auth";
    public static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    public static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    public static final String SCOPES = "openid email profile";
}