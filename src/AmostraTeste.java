import java.util.ArrayList;

public class AmostraTeste {
    ArrayList <int[]> valuesList;
    int[] max; //este atributo é um array de inteiros que armazenará o máximo para cada posição

    public AmostraTeste() {
        super();
        this.valuesList = new ArrayList <int[]>();
        this.max = null;
    }

    public void add(int[] x) {
        this.valuesList.add(x);
        if (this.max == null) {             
            this.max = new int[x.length]; //cria um novo array de inteiros que armzena o máximo para cada posição
            for (int i = 0; i < x.length; i++) {
                this.max[i] = x[i];
            }
        }
        else {
            for (int i = 0; i < x.length; i++) {
                if (x[i] > this.max[i]) {
                    this.max[i] = x[i];
                }
            } 
        }
    }

    public int length() {
        return this.valuesList.size();
    }

    public int[] element(int i) {
        return this.valuesList.get(i);
    }

    public int domain(int[] v) {
        int r = 1;
        for (int i = 0; i < v.length; i++) {
            r = r * (this.max[i] + 1);
        }
        return r;
    }

    public static void main(String[] args) {


    }
}




