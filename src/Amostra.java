import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

public class Amostra implements Serializable {
    private static final long serialVersionUID = 1L;

    ArrayList<int[]> vectorsList;
    int[] max;
    
    // Cache para o resultado dos It()
    private transient Map<String, Double> itCache = new HashMap<>();

    public Amostra() {
        super();
        this.vectorsList = new ArrayList<int[]>();
        this.max = null;
    }

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

    public int domain(int varIdx) {
        return this.max[varIdx] + 1;
    }

    public int domain(ArrayList<Integer> vars) {
        int r = 1;
        for (int i : vars) {
            r = r * (this.max[i] + 1);
        }
        return r;
    }

    public int count(int var, int val) {
        int r = 0;
        for (int[] vector : this.vectorsList) {
            if (vector[var] == val) {
                r++;
            }
        }
        return r;
    }

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


    public void clearCache() {
        if (itCache == null) {
            this.itCache = new HashMap<>();
        } else {
            itCache.clear();
        }
    }

    public Double getCachedIt(int nodeIdx, ArrayList<Integer> parents) {
        if (itCache == null) {
            this.itCache = new HashMap<>();
            return null;
        }
        String key = nodeIdx + ":" + parents.toString();
        return itCache.get(key);
    }

    public void setCachedIt(int nodeIdx, ArrayList<Integer> parents, double value) {
        if (itCache == null) {
            this.itCache = new HashMap<>();
        }
        String key = nodeIdx + ":" + parents.toString();
        itCache.put(key, value);
    }

    @Override
    public String toString() {
        return "Amostra [Size=" + length() + ", Dim=" + dim() + "]";
    }
}