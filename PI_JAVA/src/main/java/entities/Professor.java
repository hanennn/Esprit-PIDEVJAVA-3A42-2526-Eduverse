package entities;

public final class Professor extends User{


    private String DateLastConnexion;
    private String googleId;
    private String Experience;
    private String specialty;

    public Professor(){
    }

    //ID
    public Professor(int Id, String FirstName, String LastName, String UserName, String Email, String Password, Boolean isActive, String DateInscription, String googleId, String specialty, String Experience, String DateLastConnexion) {
        super(Id, FirstName, LastName, UserName, Email,Password, isActive, DateInscription);
        this.DateLastConnexion = DateLastConnexion;
        this.googleId = googleId;
        this.Experience = Experience;
        this.specialty = specialty;
    }

    //Sans ID et date inscription
    public Professor(String FirstName, String LastName, String UserName, String Email, String Password, Boolean isActive, String googleId, String specialty, String Experience, String DateLastConnexion) {
        super(FirstName, LastName, UserName, Email, Password, isActive);
        this.DateLastConnexion = DateLastConnexion;
        this.googleId = googleId;
        this.Experience = Experience;
        this.specialty = specialty;
    }


    public String getDateLastConnexion() {
        return DateLastConnexion;
    }
    public String getGoogleId() {
            return googleId;
    }
    public String getExperience() {
        return Experience;
    }
    public String getSpecialty() {
        return specialty;
    }

    public void setDateLastConnexion(String DateLastConnexion) {
        this.DateLastConnexion = DateLastConnexion;
    }
    public void setGoogleId(String GoogleId) {
        this.googleId = GoogleId;
    }
    public void setExperience(String Experience) {
        this.Experience = Experience;
    }
    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }


    @Override
    public String toString() {
        return "Professor{" +
                "Id = " + Id +'\n' +
                "FirstName = " + FirstName + '\n' +
                "LastName ="  + LastName + '\n' +
                "UserName = " + UserName + '\n' +
                "Email = " + Email + '\n' +
                "Password = " + Password + '\n' +
                "GooleId = " + googleId + '\n' +
                "isActive = " + isActive +
                "DateInscription =" + DateInscription + '\n' +
                "DateLastConnexion =" + DateLastConnexion + '\n' +
                "Experience =" + Experience + '\n' +
                "specialty =" + specialty + '\n' +
                '}';
    }


}
