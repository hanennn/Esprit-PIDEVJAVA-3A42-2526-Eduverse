package Services;

public class GoogleUserInfo {
    public final String googleId;
    public final String email;
    public final String prenom;
    public final String fullName;

    public GoogleUserInfo(String googleId, String email, String prenom, String fullName) {
        this.googleId = googleId;
        this.email = email;
        this.prenom = prenom;
        this.fullName = fullName;
    }
}