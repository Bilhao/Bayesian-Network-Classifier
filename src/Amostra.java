import java.util.ArrayList;
import java.util.Arrays;

public class Amostra {
    ArrayList <int[]> valuesList;
    int[] max; //este atributo é um array de inteiros que armazenará o máximo para cada posição

    public Amostra() {
        super();
        this.valuesList = new ArrayList <int[]>();
        this.max = null;
    }

    /*
    recebe um vetor e adiciona-o à amostra.
    Cria uma lsita de inteiros assim que adicionados os vetores com os máximos de cada posição.
    */
    public void add(int[] x) {
        this.valuesList.add(x);
        if (this.max == null) {             
            this.max = new int[x.length]; //cria um novo array de inteiros que armzena o máximo para cada posição.
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

    //retorna o comprimento da amostra. 
    public int length() {
        return this.valuesList.size();
    }

    //recebe uma posição e retorna o vetor da amostra.
    public int[] element(int i) {
        return this.valuesList.get(i);
    }

    /*
    Recebe uma amostra e um vetor de posições e retorna o n~umero de elementos possíveis
    desse vetor de posições.
    */
    public int domain(int[] v) {
        int r = 1;
        for (int i = 0; i < v.length; i++) {
            r = r * (this.max[i] + 1); //asumimos sempre a existência de valores intermédios.
        }
        return r;
    }

    /*
    Função auxiliar do método count() que verifica se existe os valores dados nas variáveis pedidas.
    */
    public static boolean condQ(int[] var, int[] val, int[] x) { 
        boolean r = true;
        for (int i = 0; i < var.length; i++) {
            if (x[var[i]] != val[i]) {
                r = false;
            }
        }
        return r; 
    }

    /*
    Recebe um vetor de variáveis e um vetor de valores, retornando o número de ocorrências
    desses valores para essas variáveis na amostra.
    */
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
    @Override
    public String toString() {
        return "Amostra = {Lista de amostras = " + show(valuesList) + "], máximos = " + Arrays.toString(max) + "]}";
    }
    
    public static String show(ArrayList<int[]> lista) {
        String s = "";
        for (int[] x : lista) {  //para todos os elementos de lista
            s = s + Arrays.toString(x) + ",";
        }
        return s;
    }

    public static void main(String[] args) {
        Amostra m = new Amostra();
        int [] a1 = {1, 2, 3, 4};
        int [] a2 = {0, 3, 4, 5};
        int [] a3 = {0, 3, 1, 2};
        m.add(a1);
        m.add(a2);
        m.add(a3);
        int[] x1 = {0,1};
        int[] x2 = {0,3};
        System.out.println(m);
        System.out.println(m.domain(x1));
        System.out.println(m.count(x1,x2));
        System.out.println(m.length());
        System.out.println(Arrays.toString(m.element(0)));
    }
}




