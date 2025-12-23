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
    }

    private void buildPrior() { //aprende probabilidade da classe
        int N = amostra.length(); //número de amostras (linhas)
        double den = N + S*priorC.length; //denominador
        for(int k = 0; k < priorC.length; k++) {
            double num = amostra.count(new int[]{C} ,new int[]{k}) + S;//numerador
            double prob = num/den; //probabilidade
            priorC[k] = prob;//guarda a probabilidade na posição k
        }
    }

    private void buildCPT() { //aprende a probabilidade do resto
        for(int k = 0; k < C; k++) {
            int[] pais = grafo.parents(k);
            int tamanho = pais.length + 1;
            int[] paiscomclasse = new int[tamanho];
            for(int i = 0; i < pais.length; i++) { //coloca os pais no array
                paiscomclasse[i] = pais[i];
            }
            paiscomclasse[pais.length] = C; //adiciona a classe porque é sempre pai
            ArrayList<int[]> ws = amostra.combinations(paiscomclasse); //conjunto de combinações
            for(int i = 0; i < ws.size(); i++) {
                int[] w = ws.get(i);//cada combinação 
                int denCount = amostra.count(paiscomclasse, w);
                double den = denCount + S*(amostra.max[k] + 1); //denominador
                int dk = amostra.max[k] + 1;
                double[] probs = new double[dk];
                for(int d = 0; d < dk; d++) {
                    int[] vars = new int[paiscomclasse.length + 1];
                    vars[0] = k;
                    for (int j = 0; j < paiscomclasse.length; j++) {
                    vars[j + 1] = paiscomclasse[j];
                    }

                    int[] vals = new int[w.length + 1];
                    vals[0] = d;
                    for (int j = 0; j < w.length; j++) {
                    vals[j + 1] = w[j];
                    }

                    int numCount = amostra.count(vars, vals);
                    double num = numCount + S;

                    probs[d] = num / den;
                    }
                }
            }
        }
    }



