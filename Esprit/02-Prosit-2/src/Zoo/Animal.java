package Zoo;


public class Animal {

    String Family ;
    String Name ;
    int Age ;
    boolean isMamel ;

    public Animal(String Family , String Name , int Age , boolean isMamel) {
        this.Family = Family;
        this.Name = Name;
        this.Age = Age;
        this.isMamel = isMamel;
    }

    public void displayAnimal() {
        System.out.printf("\n   Family: " + this.Family + "\n   Name: " + this.Name + "\n   Age: " + this.Age + "\n    isMamel: " + this.isMamel);
    }


}
