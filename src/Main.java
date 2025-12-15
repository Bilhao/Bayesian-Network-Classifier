import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    
    public static String show(ArrayList<int[]> lista) {
        String s = "";
        for (int[] x : lista) { // Para todos os elementos de lista
            s = s + Arrays.toString(x) + ",";
        }
        return s;
    }

    public static void main(String[] args) {

        Amostra amostra = ReadCSV.read("../bcancer.csv");
        // System.out.println(amostra);
        System.out.println(amostra.length());
        System.out.println(amostra.domain(new int[]{0,1}));
        System.out.println(show(amostra.combinations(new int[] {0,2})));
    }
}
