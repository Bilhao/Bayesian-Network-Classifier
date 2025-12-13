public class Main {
    public static void main(String[] args) {

        Amostra amostra = ReadCSV.read("..//bcancer.csv");
        // System.out.println(amostra);
        System.out.println(amostra);
        System.out.println(amostra.length());
        System.out.println(amostra.domain(new int[]{0,2}));
    }
}
