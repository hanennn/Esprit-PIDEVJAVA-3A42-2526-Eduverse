import Zoo.Zoo ;
import Zoo.Animal;

public class Main {
    public static void main(String[] args) {

        Zoo myZoo = new Zoo("best zoo" , "Tunis" , 12);
        Animal cat = new Animal( "Felin" , "Louna" , 7 , true);
        Animal nemo = new Animal("Fish", "nemo" , 1, false);
        Animal lion = new Animal( "Felin" , "Lion" , 10 , true) ;
        Animal tiger = new Animal( "Felin" , "tiger" , 6 , true) ;

        System.out.println(myZoo.toString());
        System.out.println("###############################################################################");
        myZoo.addAnimal(cat);
        myZoo.addAnimal(nemo);
        myZoo.addAnimal(lion);
        myZoo.addAnimal(tiger);

        myZoo.displayZoo();


    }
}