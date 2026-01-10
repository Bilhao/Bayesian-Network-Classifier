import java.util.Map;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class BN2 implements java.io.Serializable {
    Amostra amostra;
    Grafoo grafo;
    double S;

    // Array que guarda a distribuição de probabilidade da classe
    double[] classProb;

    // Conditional Probability Table (CPT)
    Map<Integer, Map<String, double[]>> cpt;

    public BN2(Amostra amostra, Grafoo grafo, double S) {
        super();
        this.amostra = amostra;
        this.grafo = grafo;
        this.S = S;

        this.classProb = buildClassProbabilities();
        this.cpt = buildCPT();
    }

    private double[] buildClassProbabilities() {
        int n = amostra.dim();
        int classIdx = n - 1;

        double[] classProb = new double[amostra.domain(classIdx)];

        for (int i = 0; i < classProb.length; i++) {
            classProb[i] = (double) amostra.count(classIdx, i) / amostra.length();
        }
        return classProb;
    }

    private Map<Integer, Map<String, double[]>> buildCPT() {
        int n = amostra.dim();
        int classIdx = n - 1;

        Map<Integer, Map<String, double[]>> cpt = new HashMap<>();

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
        return cpt;
    }

    public double prob(int[] instance) {
        int n = instance.length;
        int classIdx = n - 1;

        int classe = instance[classIdx]; // extrai a classe do vetor
        double p = classProb[classe]; // começa com o prior da classe P(C)

        for (int i = 0; i < classIdx; i++) { // percorre todas as variaveis exceto a classe
            ArrayList<Integer> parents = new ArrayList<>(grafo.parents(i));

            ArrayList<Integer> values = new ArrayList<>();
            for (int idx : parents) {
                values.add(instance[idx]);
            }
            values.add(classe);

            String key = values.toString(); // converte a combinação numa chave para a CPT
            double[] probs = cpt.get(i).get(key); // obtem a distribuição
            if (probs == null)
                return 0.0; // se a combinação nunca foi observada, probabilidade é zero
            int val = instance[i]; // valor observado da variavel

            p *= probs[val];// multiplica
        }
        return p;
    }

    public int classify(int[] instance) {
        int n = instance.length;
        int classIdx = n - 1;

        double bestProb = -1.0; // nestes dois probabilidades nem classe podem ter valores negativos então garante que este será sempre substituido
        int bestClass = -1;
        for (int c = 0; c < classProb.length; c++) { // percorre todas as classes possíveis
            int[] x = new int[n]; // vetor atributos mais classe
            for (int i = 0; i < classIdx; i++) { // copia atributos para o vetor completo
                x[i] = instance[i];
            }
            x[classIdx] = c; // fixa a classe para testar na ultima posição
            double probability = prob(x); // calcula a probabilidade conjunta
            if (probability > bestProb) {
                bestProb = probability;
                bestClass = c; // caso a probabilidade seja maior diz que esta é a classe correta
            }
        }
        return bestClass; // devolve a classe
    }

    public double[] getProbabilities(int[] instance) {
        int classIdx = instance.length - 1;

        double[] probs = new double[classProb.length];
        double sum = 0.0;

        // Cria um vetor com um elemento a mais que a instância, reservado para adicionar um valor
        int[] vector = new int[instance.length + 1];

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
            this.cpt = buildCPT();

            int acertos = 0;

            for (int i = 0; i < amostra.length(); i++) {
                int[] linhaTeste = amostra.element(i);
                int[] iSemClasse = Arrays.copyOf(linhaTeste, linhaTeste.length - 1);

                int classePrevista = classify(iSemClasse);
                int classeReal = linhaTeste[linhaTeste.length - 1];

                if (classePrevista == classeReal) {
                    acertos++;
                }
            }
            double accuracy = (double) acertos / amostra.length();
            if (accuracy > bestAccuracy) {
                bestAccuracy = accuracy;
                bestS = s;
            }
        }
        this.S = bestS;
        this.cpt = buildCPT();
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
    public static BN2 load(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (BN2) in.readObject();
        }
    }
}
