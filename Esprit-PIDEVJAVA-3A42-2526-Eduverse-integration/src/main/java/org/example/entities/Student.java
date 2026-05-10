package org.example.entities;

public final class Student extends User{

    private String googleId;
    private String DateLastConnexion;

    public Student() {
    }
    //ID
    public Student(int Id, String FirstName, String LastName, String UserName, String Email, String Password, Boolean isActive, String DateInscription, String googleId, String DateLastConnexion) {
        super(Id, FirstName, LastName, UserName, Email,Password, isActive, DateInscription);
        this.googleId = googleId;
        this.DateLastConnexion = DateLastConnexion;
    }

    //Sans ID et date inscription
    public Student(String FirstName, String LastName, String UserName, String Email, String Password, Boolean isActive, String googleId, String DateLastConnexion) {
        super(FirstName, LastName, UserName, Email, Password, isActive);
        this.googleId = googleId;
        this.DateLastConnexion = DateLastConnexion;
    }

    public String getGoogleId() {
        return googleId;
    }
    public String getDateLastConnexion() {
        return DateLastConnexion;
    }


    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }
    public void setDateLastConnexion(String DateLastConnexion) {
        this.DateLastConnexion = DateLastConnexion;
    }


    @Override
    public String toString() {
        return "Student{" +
                "Id = " + Id +'\n' +
                "FirstName = " + FirstName + '\n' +
                "LastName ="  + LastName + '\n' +
                "UserName = " + UserName + '\n' +
                "Email = " + Email + '\n' +
                "Password = " + Password + '\n' +
                "isActive = " + isActive +
                "DateInscription =" + DateInscription + '\n' +
                "DateLastConnexion = " + DateLastConnexion +'\n'+
                "googleId = " + googleId + '\n' +
                '}';
    }

}
