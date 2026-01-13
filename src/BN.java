import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.*;

public class BN implements Serializable {
    private static final long serialVersionUID = 1L;
    public Grafoo graph;
    public double S;

    // Armazena o dominio de cada variavel
    public int[] domains;

    // Armazena as contagens: Lista (por variavel) -> Mapa (Configuracao Pais -> Contagens Valores)
    public List<HashMap<String, int[]>> counts;

    private transient Listener listener;

    public interface Listener {
        void onProgress(int current, int total, String message);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public BN(Amostra amostra, Grafoo graph, double S) {
        super();
        this.graph = graph;
        this.S = S;

        preCalculateCounts(amostra);
    }

    private void preCalculateCounts(Amostra amostra) {
        int n = amostra.dim();
        this.domains = new int[n];
        this.counts = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            domains[i] = amostra.domain(i);
            counts.add(new HashMap<>());
        }

        // Itera sobre todas as instancias da amostra para contar
        for (int k = 0; k < amostra.length(); k++) {
            int[] instance = amostra.element(k);
            int classeIdx = n - 1; // Assumindo ultima coluna como classe

            // Contagem da Classe
            HashMap<String, int[]> classMap = counts.get(classeIdx);
            String classKey = "";
            if (!classMap.containsKey(classKey)) {
                classMap.put(classKey, new int[domains[classeIdx]]);
            }
            classMap.get(classKey)[instance[classeIdx]]++;

            for (int i = 0; i < n - 1; i++) {
                String key = generateParentKey(i, classeIdx, instance);

                HashMap<String, int[]> varMap = counts.get(i);
                if (!varMap.containsKey(key)) {
                    varMap.put(key, new int[domains[i]]);
                }
                varMap.get(key)[instance[i]]++;
            }
        }
    }

    private String generateParentKey(int varIdx, int classIdx, int[] instance) {
        ArrayList<Integer> parents = new ArrayList<>(graph.parents(varIdx));
        parents.add(classIdx); // Classe eh sempre pai

        StringBuilder keyBuilder = new StringBuilder();
        for (int p : parents) {
            keyBuilder.append(instance[p]).append(",");
        }
        return keyBuilder.toString();
    }

    /**
     * Calcula Pr(x_1, ..., x_n, c) = Pr(c) * Produto(i=1 ate n) Pr(x_i | Pais(x_i)) Em que Pr(x_i | Pais(x_i)) = (N(x_i, Pais(x_i)) + S) / (N(Pais(x_i)) + S * |D_xi|)
     */
    public double prob(int[] vector) {
        int classeIdx = vector.length - 1;

        // 1. Probabilidade da Classe
        int classeVal = vector[classeIdx];
        HashMap<String, int[]> classMap = counts.get(classeIdx);
        int[] classCounts = classMap.get("");

        int N_c = 0;
        int total_c = 0;
        if (classCounts != null) {
            N_c = classCounts[classeVal];
            for (int c : classCounts)
                total_c += c;
        }
        double prc = (double) N_c / total_c;

        double prob = 1.0;
        for (int i = 0; i < vector.length - 1; i++) {
            ArrayList<Integer> parents = new ArrayList<>(graph.parents(i));
            parents.add(classeIdx);

            StringBuilder keyBuilder = new StringBuilder();
            for (int p : parents) {
                keyBuilder.append(vector[p]).append(",");
            }
            String key = keyBuilder.toString();

            HashMap<String, int[]> varMap = counts.get(i);
            int[] valCounts = varMap.get(key);

            int N_xi_pa = 0;
            int N_pa = 0;

            if (valCounts != null) {
                N_xi_pa = valCounts[vector[i]];
                for (int c : valCounts)
                    N_pa += c;
            }

            prob *= (N_xi_pa + S) / (N_pa + S * domains[i]);
        }
        return prc * prob;
    }

    /**
     * Retorna as probabilidades para cada classe dada uma instância.
     */
    public double[] getProbabilities(int[] instance) {
        int classeIdx = instance.length;
        int numClasses = domains[classeIdx];
        double[] probs = new double[numClasses];
        double totalProb = 0.0;

       // Cria um vetor com um elemento a mais que a instância, reservado para adicionar um valor
        int[] vector = new int[instance.length + 1];
        System.arraycopy(instance, 0, vector, 0, instance.length);

        for (int c = 0; c < numClasses; c++) {
            vector[classeIdx] = c;
            probs[c] = prob(vector);
            totalProb += probs[c];
        }

        if (totalProb > 0) {
            for (int c = 0; c < numClasses; c++) {
                probs[c] /= totalProb;
            }
        }

        return probs;
    }

    public int classify(int[] vector) {
        int classeIdx = vector.length;
        int[] a = new int[vector.length + 1];
        for (int i = 0; i < vector.length; i++) {
            a[i] = vector[i];
        }

        double maxProb = -1.0;
        int bestClass = -1;

        for (int c = 0; c < domains[classeIdx]; c++) {
            a[a.length - 1] = c;
            double p = prob(a);
            if (p > maxProb) {
                maxProb = p;
                bestClass = c;
            }
        }
        return bestClass;
    }

    /**
     * Otimiza o parametro S testando valores entre 0.01 e 1 na amostra fornecida.
     */
    public void optimizeS(Amostra validationData) {
        double bestS = 0.5;
        double bestAccuracy = -1.0;

        int steps = 0;
        int totalSteps = 100; // 0.01 to 1 with 0.01 step is ~100 steps

        for (double s = 0.01; s <= 1; s += 0.01) {
            this.S = s;
            int hits = 0;
            int n = validationData.length();

            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            for (int k = 0; k < n; k++) {
                int[] instance = validationData.element(k);
                // Copia instancia sem a classe para classificar
                int[] instanceNoClass = new int[instance.length - 1];
                System.arraycopy(instance, 0, instanceNoClass, 0, instance.length - 1);

                int predicted = classify(instanceNoClass);
                int actual = instance[instance.length - 1];

                if (predicted == actual) {
                    hits++;
                }
            }

            double accuracy = (double) hits / n;
            if (accuracy > bestAccuracy) {
                bestAccuracy = accuracy;
                bestS = s;
            }

            steps++;
            if (listener != null) {
                listener.onProgress(steps, totalSteps, String.format("Testando S=%.2f - Acc: %.2f%%", s, accuracy * 100));
            }
        }
        this.S = bestS;
    }

    /**
     * Guarda a rede em um arquivo.
     */
    public void save(String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
        }
    }

    /**
     * Carrega a rede de um arquivo.
     */
    public static BN load(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (BN) in.readObject();
        }
    }

    public String toString() {
        return "BN [graph=" + graph + ", S=" + S + "]";
    }
}
