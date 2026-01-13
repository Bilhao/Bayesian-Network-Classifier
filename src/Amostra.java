import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.Serializable;

public class Amostra implements Serializable {
    private static final long serialVersionUID = 1L;

    ArrayList<int[]> vectorsList;
    int[] max;

    // Cache para o resultado dos It() (thread-safe)
    private transient Map<String, Double> itCache = new ConcurrentHashMap<>();

    public Amostra() {
        super();
        this.vectorsList = new ArrayList<int[]>();
        this.max = null;
    }

    /*
     * Recebe um vetor e adiciona-o à amostra.
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

    /*
     * Retorna o tamanho da amostra.
     */
    public int length() {
        return this.vectorsList.size();
    }

    /*
     * Retorna a dimensão da amostra.
     */
    public int dim() {
        return this.max.length;
    }

    /*
     * Retorna o i-ésimo elemento da amostra.
     */
    public int[] element(int i) {
        return this.vectorsList.get(i);
    }

    /*
     * Retorna o domínio de uma variável.
     */
    public int domain(int varIdx) {
        return this.max[varIdx] + 1;
    }

    /*
     * Retorna o domínio de um conjunto de variáveis.
     */
    public int domain(ArrayList<Integer> vars) {
        int r = 1;
        for (int i : vars) {
            r = r * (this.max[i] + 1);
        }
        return r;
    }

    /*
     * Retorna o número de ocorrências de um conjunto de variáveis com um conjunto de valores.
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

    /*
     * Retorna uma subamostra sem o i-ésimo elemento.
     */
    public Amostra without(int index) {
        Amostra subamostra = new Amostra();
        if (this.max != null) {
            subamostra.max = new int[this.max.length];
            System.arraycopy(this.max, 0, subamostra.max, 0, this.max.length);
        }

        for (int i = 0; i < this.vectorsList.size(); i++) {
            if (i != index) {
                subamostra.add(this.vectorsList.get(i));
            }
        }
        return subamostra;
    }

    public void clearCache() {
        if (itCache == null) {
            this.itCache = new ConcurrentHashMap<>();
        } else {
            itCache.clear();
        }
    }

    public void setCachedIt(int nodeIdx, ArrayList<Integer> parents, double value) {
        if (itCache == null) {
            this.itCache = new ConcurrentHashMap<>();
        }
        String key = nodeIdx + ":" + parents.toString();
        itCache.put(key, value);
    }

    public Double getCachedIt(int nodeIdx, ArrayList<Integer> parents) {
        if (itCache == null) {
            this.itCache = new ConcurrentHashMap<>();
            return null;
        }
        String key = nodeIdx + ":" + parents.toString();
        return itCache.get(key);
    }

    @Override
    public String toString() {
        return "Amostra [Size=" + length() + ", Dim=" + dim() + "]";
    }
}