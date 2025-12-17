import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        Amostra amostra = ReadCSV.read("../DataSets/bcancer.csv");
        // System.out.println(amostra);
        System.out.println(amostra.length());

        ArrayList<Integer> vars = new ArrayList<>(Arrays.asList(0, 2));
        System.out.println(amostra.domain(vars));
        System.out.println(amostra.combinations(vars));
    }
}
