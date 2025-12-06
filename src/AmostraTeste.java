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
            r = r * (this.max[i] + 1); //asumimos sempre a existência de valores intermédios
        }
        return r;
    }

    public static boolean condQ(int[] var, int[] val, int[] x) { //recebe um array com referência às variáveis, um com os valores de cada variável, e uma amostra
        boolean r = true;
        for (int i = 0; i < var.length; i++) {
            if (x[var[i]] != val[i]) { //se o valor da variável i da amostra x for diferente do valor para essa variável no vetor de valores, r = false
                r = false;
            }
        }
        return r; 
    }

    public int count(int[] var, int[] val) {
        int r = 0;
        for (int j = 0; j < this.length(); j++) {
            int[] sample = this.element(j);
            if (condQ(var, val, sample)) {
                r++;
            }
        }
        return r;
    }

    public String toString() {
        return "Amostra = {Lista de amostras = " + valuesList + "], máximos = " + max + "]}";
    }
     

    public static void main(String[] args) {
        AmostraTeste m = new AmostraTeste();
        int [] a1 = { 1, 2, 3, 4};
        int [] a2 = { 0, 3, 4, 5};
        m.add(a1);
        m.add(a2);
        System.out.println(m);

    }
}




