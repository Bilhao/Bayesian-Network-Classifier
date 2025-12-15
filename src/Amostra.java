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
     * Recebe um vetor de posições e retorna o domínio conjunto das variáveis nessas posições.
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
     * Conta quantas vezes uma certa combinação de valores ocorre nas variáveis indicadas.
     * 
     * Exemplo: se vars = [0,2] e vals = [1,3], conta quantas amostras têm valor 1 na variável 0 e valor 3 na variável 2.
     * 
     * @param vars vetor de variáveis
     * @param vals vetor de valores
     */
    public int count(int[] vars, int[] vals) {
        int r = 0;
        for (int[] vector : this.vectorsList) {
            boolean match = true;
            for (int i = 0; i < vars.length; i++) {
                if (vector[vars[i]] != vals[i]) {
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
     * Retorna as combinações de valores possível dado um vetor de posições
     * 
     * @param vars
     */
    public ArrayList<int[]> combinations(int[] vars) {
        int combinationsLength = domain(vars);
        ArrayList<int[]> combinations = new ArrayList<int[]>();

        for (int i = 0; i < combinationsLength; i++) { // Para cada combinação
            int temp = i;
            int[] value = new int[vars.length];
            for (int j = vars.length - 1; j >= 0; j--) { // Para cada variável
                int var = vars[j];
                value[j] = temp % (max[var] + 1);
                temp /= (max[var] + 1);
            }
            combinations.add(value);
        }
        return combinations;
    }

    // /**
    // * Retorna as combinações de valores possível dado um vetor de posições
    // *
    // * @param vars
    // * @return
    // */
    // public ArrayList<int[]> combinations(int[] vars) {
    // ArrayList<int[]> result = new ArrayList<>();
    // generateCombinations(vars, 0, new int[vars.length], result);
    // return result;
    // }

    // /**
    // * Método auxiliar recursivo para gerar combinações de valores.
    // *
    // * Percorre todas as posições em vars e para cada posição, itera sobre todos os valores possíveis (de 0 ao máximo dessa variável), armzenando-os na combinação atual.
    // * Se a combinação atual estiver completa, adiciona-a à lista de resultados.
    // *
    // * Para evitar duplicados, logo após adicionar uma combinação à lista de resultados, retorna imediatamente.
    // *
    // * @param vars - vetor de posições
    // * @param index - índice atual em vars
    // * @param current - combinação atual sendo construída
    // * @param result - lista de combinações resultantes
    // */
    // private void generateCombinations(int[] vars, int index, int[] current, ArrayList<int[]> result) {
    // if (index == vars.length) {
    // result.add(current);
    // return;
    // }

    // int varIndex = vars[index];
    // int maxValue = this.max[varIndex];

    // for (int val = 0; val <= maxValue; val++) {
    // current[index] = val;
    // generateCombinations(vars, index + 1, current, result);
    // }
    // }

    @Override
    public String toString() {
        return "Amostra = {Lista de vetores na amostra = [" + show(vectorsList) + "]; Maximos = " + Arrays.toString(max) + "}";
    }

    public static String show(ArrayList<int[]> lista) {
        String s = "";
        for (int[] x : lista) { // Para todos os elementos de lista
            s = s + Arrays.toString(x) + ",";
        }
        return s;
    }

}
