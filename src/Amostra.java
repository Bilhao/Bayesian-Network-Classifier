import java.util.ArrayList;
import java.util.Arrays;

public class Amostra {
    ArrayList<int[]> vectorsList;
    int[] max; // Array de inteiros que armazenará o máximo para cada posição

    public Amostra() {
        super();
        this.vectorsList = new ArrayList<int[]>();
        this.max = null;
    }

    /**
     * Recebe um vetor e adiciona-o à amostra.
     * 
     * Depois de adicionar o vetor, atualiza o array de máximos.
     */
    public void add(int[] vector) {
        this.vectorsList.add(vector);
        if (this.max == null) {
            this.max = new int[vector.length];
            for (int i = 0; i < vector.length; i++) {
                this.max[i] = vector[i];
            }
        } else {
            for (int i = 0; i < vector.length; i++) {
                if (vector[i] > this.max[i]) {
                    this.max[i] = vector[i];
                }
            }
        }
    }

    public int length() {
        return this.vectorsList.size();
    }
    
    public int dim() {
        return this.max.length;
    }

    public int[] element(int i) {
        return this.vectorsList.get(i);
    }

    /**
     * Recebe uma posição e retorna o domínio das variáveis nessa posição.
     */
    public int domain(int varIdx) {
        return this.max[varIdx] + 1;
    }

    /**
     * Recebe uma lista de posições e retorna o domínio conjunto das variáveis nessas posições.
     */
    public int domain(ArrayList<Integer> vars) {
        int r = 1;
        for (int i : vars) {
            r = r * (this.max[i] + 1); // Assumimos sempre a existência de valores intermédios.
        }
        return r;
    }

    /**
     * Conta quantas vezes um certo valor ocorre na variável indicada.
     */
    public int count(int var, int val) {
        int r = 0;
        for (int[] vector : this.vectorsList) {
            if (vector[var] == val) {
                r++;
            }
        }
        return r;
    }

    /**
     * Conta quantas vezes uma certa combinação de valores ocorre nas variáveis indicadas.
     * 
     * Exemplo: se vars = [0,2] e vals = [1,3], conta quantas amostras têm valor 1 na variável 0 e valor 3 na variável 2.
     */
    public int count(ArrayList<Integer> vars, ArrayList<Integer> vals) {
        int r = 0;
        for (int[] vector : this.vectorsList) {
            boolean match = true;
            for (int i = 0; i < vars.size(); i++) {
                if (vector[vars.get(i)] != vals.get(i)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                r++;
            }
        }
        return r;
    }

    /**
     * Retorna as combinações de valores possível dado uma lista de posições
     */
    public ArrayList<ArrayList<Integer>> combinations(ArrayList<Integer> vars) {
        int combinationsLength = domain(vars);
        ArrayList<ArrayList<Integer>> combinations = new ArrayList<>();

        for (int i = 0; i < combinationsLength; i++) { // Para cada combinação
            int temp = i;
            ArrayList<Integer> value = new ArrayList<>();
            for (int j = 0; j < vars.size(); j++) { // Inicializar com zeros
                value.add(0);
            }
            for (int j = vars.size() - 1; j >= 0; j--) { // Para cada variável
                int var = vars.get(j);
                value.set(j, temp % (max[var] + 1));
                temp /= (max[var] + 1);
            }
            combinations.add(value);
        }
        return combinations;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Amostra [vectorsList=[");
        for (int i = 0; i < vectorsList.size(); i++) {
            sb.append(Arrays.toString(vectorsList.get(i)));
            if (i < vectorsList.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("], max=").append(Arrays.toString(max)).append("]");
        return sb.toString();
    }

}
