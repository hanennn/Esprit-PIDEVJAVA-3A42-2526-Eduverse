package org.example.entities;

public final class Admin extends User{



    public Admin() {
    }

    //ID
    public Admin(int Id, String FirstName, String LastName, String UserName, String Email, String Password, Boolean isActive, String DateInscription) {
        super(Id, FirstName, LastName, UserName, Email,Password, isActive, DateInscription);
    }


    //Sans ID et date inscription
    public Admin(String FirstName, String LastName, String UserName, String Email, String Password, Boolean isActive) {
        super(FirstName, LastName, UserName, Email, Password, isActive);
    }

    @Override
    public String toString() {
        return "Admin{" +
                "Id = " + Id +'\n' +
                "FirstName = " + FirstName + '\n' +
                "LastName ="  + LastName + '\n' +
                "UserName = " + UserName + '\n' +
                "Email = " + Email + '\n' +
                "Password = " + Password + '\n' +
                "isActive = " + isActive +
                "DateInscription =" + DateInscription + '\n' +
                '}';
    }


}
