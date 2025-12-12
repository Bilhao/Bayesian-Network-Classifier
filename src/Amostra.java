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
     * 
     * @param vector Vetor de inteiros a adicionar à amostra
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

    /**
     * Retorna o número de vetores na amostra.
     */
    public int length() {
        return this.vectorsList.size();
    }

    /**
     * Recebe um índice i e retorna o vetor na posição i da amostra.
     * 
     * @param i Índice do vetor a retornar
     *
     */
    public int[] element(int i) {
        return this.vectorsList.get(i);
    }

    /**
     * Recebe um vetor de posições e retorna o domínio conjunto das variáveis nessas
     * posições.
     * 
     * @param vector Vetor de posições
     */
    public int domain(int[] vector) {
        int r = 1;
        for (int i : vector) {
            r = r * (this.max[i] + 1); // Assumimos sempre a existência de valores intermédios.
        }
        return r;
    }

    /**
     * Conta quantas vezes uma certa combinação de valores ocorre nas variáveis
     * indicadas.
     * Exemplo: se vars = [0,2] e vals = [1,3], conta quantas amostras têm valor 1
     * na variável 0 e valor 3 na variável 2.
     * 
     * @param vars vetor de variáveis
     * @param vals vetor de valores
     */
    public int count(int[] vars, int[] vals) {
        int r = 0;
        for (int[] vetor : this.vectorsList) {
            for (int i = 0; i < vars.length; i++) {
                if (vetor[vars[i]] == vals[i]) {
                    r++;
                    break;
                }
            }
        }
        return r;
    }

    @Override
    public String toString() {
        return "Amostra = {Lista de vetores na amostra = [" + show(vectorsList) + "]; Maximos = " + Arrays.toString(max)
                + "}";
    }

    public static String show(ArrayList<int[]> lista) {
        String s = "";
        for (int[] x : lista) { // Para todos os elementos de lista
            s = s + Arrays.toString(x) + ",";
        }
        return s;
    }

}
