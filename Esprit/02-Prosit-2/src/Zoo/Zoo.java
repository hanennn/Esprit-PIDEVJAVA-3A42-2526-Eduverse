package Zoo;
import  java.util.ArrayList;

import Zoo.Animal ;

public class Zoo {

    ArrayList <Animal> Animals = new ArrayList <Animal>();
    String name;
    String city ;
    int number_Cages ;

    public Zoo (String name, String city, int number_Cages) {
        this.name = name;
        this.city = city;
        this.number_Cages = number_Cages;
    }

    public void displayZoo () {
        System.out.println("Zoo name : " + this.name + "\ncity : " + this.city + "\nnumber of cages : " +  this.number_Cages);
        for (int i = 0; i < this.Animals.size(); i++) {
            Animals.get(i).displayAnimal();
        }
    }

    public String toString () {
        return "Zoo name : " + this.name + "\ncity : " + this.city + "\nnumber of cages : " +  this.number_Cages;

    }

    public boolean addAnimal(Animal animal){
        if (this.Animals.size() >= 25)
        {
            System.out.println("Zoo plein\n");
            return false;
        }
        else {
            this.Animals.add(animal);
            this.number_Cages ++;
            return true;
        }
    }
}