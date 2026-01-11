import java.util.Map;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class BN implements java.io.Serializable {
    Amostra amostra;
    Grafoo grafo;
    double S;

    // Array que guarda a distribuição de probabilidade da classe
    double[] classProb;

    // Conditional Probability Table (CPT)
    Map<Integer, Map<String, double[]>> cpt;

    int n;
    int classIdx;

    public BN(Amostra amostra, Grafoo grafo, double S) {
        this.amostra = amostra;
        this.grafo = grafo;
        this.S = S;

        this.n = amostra.dim();
        this.classIdx = n - 1;
        this.classProb = new double[amostra.domain(classIdx)];
        this.cpt = new HashMap<>();

        buildClassProbabilities();
        buildCPT();
    }

    private void buildClassProbabilities() {
        for (int i = 0; i < classProb.length; i++) {
            classProb[i] = (double) amostra.count(classIdx, i) / amostra.length();
        }
    }

    private void buildCPT() {
        for (int i = 0; i < classIdx; i++) {
            ArrayList<Integer> parents = new ArrayList<>(grafo.parents(i));
            parents.add(classIdx); // Adiciona a classe como pai

            ArrayList<ArrayList<Integer>> combinations = amostra.combinations(parents);
            Map<String, double[]> varTable = new HashMap<>();
            cpt.put(i, varTable);

            int D_i = amostra.domain(i);

            for (ArrayList<Integer> value : combinations) {
                double[] probs = new double[D_i];

                ArrayList<Integer> vars = new ArrayList<>();
                vars.add(i);
                vars.addAll(parents);

                ArrayList<Integer> vals = new ArrayList<>();
                vals.add(0);
                vals.addAll(value);

                for (int d = 0; d < D_i; d++) {
                    vals.set(0, d);
                    probs[d] = (amostra.count(vars, vals) + S) / (amostra.count(parents, value) + S * D_i);
                }
                varTable.put(value.toString(), probs);
            }
        }
    }

    public double prob(int[] instance) {
        int classe = instance[this.classIdx];
        double p = classProb[classe];

        for (int i = 0; i < this.classIdx; i++) { // percorre todas as variaveis exceto a classe
            ArrayList<Integer> pais = grafo.parents(i);
            ArrayList<Integer> valores = new ArrayList<>();
            for (int idx : pais) {
                valores.add(instance[idx]);
            }
            valores.add(classe);// adiciona a classe

            String key = valores.toString(); // converte a combinação numa chave para a CPT
            double[] probs = cpt.get(i).get(key);// obtem a distribuição
            if (probs == null) {
                // se a combinação nunca foi observada, aplica suavização (0 + S)/(0 + S * D_i) = 1/D_i
                p *= 1.0 / amostra.domain(i);
            } else {
                int val = instance[i]; // valor observado da variavel
                p *= probs[val];// multiplica pela probabilidade condicional correspondente
            }

        }
        return p;
    }

    public int classify(int[] instance) {
        double bestProb = -1.0;
        int bestClass = -1;

        int[] x = new int[instance.length + 1];
        for (int i = 0; i < instance.length; i++) {
            x[i] = instance[i];
        }

        for (int c = 0; c < amostra.domain(classIdx); c++) {
            x[classIdx] = c;
            double prob = prob(x);
            if (prob > bestProb) {
                bestProb = prob;
                bestClass = c;
            }
        }
        return bestClass;
    }

    public double[] getProbabilities(int[] instance) {
        double[] probs = new double[classProb.length];
        double sum = 0.0;

        // Cria um vetor com um elemento a mais que a instância, reservado para adicionar um valor
        int[] vector = new int[instance.length + 1];
        System.arraycopy(instance, 0, vector, 0, instance.length);
        for (int c = 0; c < classProb.length; c++) {
            vector[classIdx] = c;
            probs[c] = prob(vector);
            sum += probs[c];
        }

        if (sum > 0) {
            for (int c = 0; c < probs.length; c++) {
                probs[c] /= sum;
            }
        }
        return probs;
    }

    /**
     * Otimiza o parametro S testando valores entre 0.1 e 2 na amostra fornecida.
     */
    public void optimizeS(Amostra amostra) {
        double bestS = 0.5;
        double bestAccuracy = -1.0;

        for (double s = 0.1; s <= 2; s += 0.1) {
            this.S = s;
            buildClassProbabilities();
            buildCPT();

            int acertos = 0;
            int n = amostra.length();

            for (int i = 0; i < n; i++) {
                int[] linhaTeste = amostra.element(i);
                int[] iSemClasse = Arrays.copyOf(linhaTeste, linhaTeste.length - 1);

                int classePrevista = classify(iSemClasse);
                int classeReal = linhaTeste[linhaTeste.length - 1];

                if (classePrevista == classeReal) {
                    acertos++;
                }
            }
            double accuracy = (double) acertos / n;
            if (accuracy > bestAccuracy) {
                bestAccuracy = accuracy;
                bestS = s;
            }
        }
        this.S = bestS;
        buildCPT(); // Atualiza a CPT com o melhor S encontrado
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
}