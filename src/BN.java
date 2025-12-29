import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;


public class BN {
    private Amostra amostra;
    private OrientedGraph grafo;
    private double S;
    private int n; //número de variáveis (colunas)
    private int C; //índice da variável classe no vetor de dados
    private double[] priorC; //array que guarda a distribuição de probabilidade da classe
    private Map<Integer, Map<String, double[]>> cpt;
    //cpt(conditional probability table) - tipo a memoria da rede
    // - recebe integer (indíce da varável Xi), devolve string e double[] (pais de Xi + valor da classe)

    public BN(Amostra amostra, OrientedGraph grafo, double S) { //construtor
        this.amostra = amostra; //guarda dados
        this.grafo = grafo; //guarda o grafo que define a estrutura da rede
        this.S = S; //parâmetro das pseudo-contagens

        this.n = amostra.element(0).length; //numero total de variáveis
        this.C = n-1;//valor da última coluna
        this.priorC = new double[amostra.max[C] + 1]; //+1 porque valores começam no 0
        this.cpt = new HashMap<>();//inicializa o mapa da cpt

        buildPrior();
        buildCPT();
    }

    private void buildPrior() { //aprende probabilidade da classe
        int N = amostra.length(); //número de amostras (linhas)
        double den = N + S*priorC.length; //denominador
        for(int k = 0; k < priorC.length; k++) {
            int numCount = amostra.count(C, k);//numero de exemplos com classe k
            double num =  numCount + S; //numerador
            priorC[k] = num/den;//guarda a probabilidade na posição k
        }
    }

    private void buildCPT() { //aprende a probabilidade do resto
        for(int k = 0; k < C; k++) {
            ArrayList<Integer> pais = grafo.parents(k);
            ArrayList<Integer> paiscomclasse = new ArrayList<>(pais);
            paiscomclasse.add(C);
            
            ArrayList<ArrayList<Integer>> ws = amostra.combinations(paiscomclasse); //conjunto de combinações
            Map<String, double[]> tabelaK = cpt.get(k);
            if (tabelaK == null) {
                tabelaK = new HashMap<>();
                cpt.put(k, tabelaK);
            }

            int dk = amostra.domain(k);
            double Sk = S*dk;//S vezes a quantidade de valores possiveis
            for(ArrayList<Integer> w:ws) {
                int denCount = amostra.count(paiscomclasse, w);//ver quantas matches desses valores de pais existem iguais a w
                double den = denCount + Sk; //denominador

                double[] probs = new double[dk];
            
                ArrayList<Integer> vars = new ArrayList<>();
                vars.add(k);
                vars.addAll(paiscomclasse);
                    
                ArrayList<Integer> vals = new ArrayList<>();
                vals.add(0);
                vals.addAll(w);
                for(int d = 0; d < dk; d++) {
                    vals.set(0,d);

                    int numCount = amostra.count(vars, vals);
                    double num = numCount + S;

                    probs[d] = num/den;
                    
                    }
                String key = w.toString();

                tabelaK.put(key, probs);
                }
            }
        }


    public double prob(int[] x) {
        int classe = x[C];
        double p = priorC[classe];

        for (int k = 0; k < C; k++) {
            ArrayList<Integer> pais = grafo.parents(k);
                
            ArrayList<Integer> valores = new ArrayList<>();
            for (int idx: pais) {
                valores.add(x[idx]);
            }
            valores.add(classe);

            String key = valores.toString();
            double[] probs = cpt.get(k).get(key);
            if (probs == null) return 0.0;
            int val = x[k];

            p *= probs[val];

            }
        return p;
    
    }
    

    public static void main(String[] args){
        Amostra a = ReadCSV.read("C:\\Users\\verso\\Downloads\\Bayesian-Network-Classifier-work\\Bayesian-Network-Classifier-work\\DataSets\\bcancer.csv");
        OrientedGraph g = new OrientedGraph(a.element(0).length);

        BN bn = new BN(a, g, 0.5);

        int[] ex = a.element(0);

        System.out.println(bn.prob(ex));
    }
}
