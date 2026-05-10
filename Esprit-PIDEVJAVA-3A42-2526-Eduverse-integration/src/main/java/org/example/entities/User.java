package org.example.entities;

 public sealed class User permits Admin, Student, Professor{

    protected int Id;
    protected String FirstName;
    protected String LastName;
    protected String UserName;
    protected String Email;
    protected String Password;
    protected Boolean isActive;
    protected String DateInscription;

    public User(){
    }

    //Avec ID et date inscription pour la lecture depuis la base de donne
    public User(int Id, String FirstName, String LastName, String UserName, String Email, String Password, Boolean isActive, String DateInscription) {
        this.Id = Id;
        this.FirstName = FirstName;
        this.LastName = LastName;
        this.UserName = UserName;
        this.Email = Email;
        this.Password = Password;
        this.isActive = true;
        this.DateInscription = DateInscription;
    }


    //Sans ID  et Date inscription pour la l'ajout
     public User(String FirstName, String LastName, String UserName, String Email, String Password, Boolean isActive) {
         this.FirstName = FirstName;
         this.LastName = LastName;
         this.UserName = UserName;
         this.Email = Email;
         this.Password = Password;
         this.isActive = true;
     }


    public int  getId() {
        return Id;
    }
    public String getFirstName() {
        return FirstName;
    }
    public String getLastName() {
        return LastName;
    }
    public String getUserName() {
        return UserName;
    }
    public String getEmail() {
        return Email;
    }
    public String getPassword() {
        return Password;
    }
    public Boolean getIsActive() {
        return isActive;
    }
    public String getDateInscription() {
        return DateInscription;
    }


    public void setId(int Id) {
        this.Id = Id;
    }
    public void setFirstName(String FirstName) {
        this.FirstName = FirstName;
    }
    public void setLastName(String LastName) {
        this.LastName = LastName;
    }
    public void setUserName(String UserName) {
        this.UserName = UserName;
    }
    public void setEmail(String Email) {
        this.Email = Email;
    }
    public void setPassword(String Password) {
        this.Password = Password;
    }
    public void setIsActive(Boolean IsActive) {
        this.isActive = IsActive;
    }
    public void setDateInscription(String DateInscription) {
        this.DateInscription = DateInscription;
    }

    @Override
    public String toString() {
        return "User{" +
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
