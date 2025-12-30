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
        int classe = x[C]; //extrai a classe do vetor
        double p = priorC[classe]; //começa com o prior da classe P(C)

        for (int k = 0; k < C; k++) { //percorre todas as variaveis exceto a classe
            ArrayList<Integer> pais = grafo.parents(k);
                
            ArrayList<Integer> valores = new ArrayList<>();
            for (int idx: pais) {
                valores.add(x[idx]);
            }
            valores.add(classe);//adiciona a classe

            String key = valores.toString(); //converte a combinação numa chave para a CPT
            double[] probs = cpt.get(k).get(key);//obtem a distribuição
            if (probs == null) return 0.0; //se a combinação nunca foi observada, probabilidade é zero
            int val = x[k]; //valor observado da variavel

            p *= probs[val];//multiplica pela probabilidade condicional correspondente

            }
        return p;
    }

    public int classify(int[] xSemClasse) {
        double bestProb = -1.0; //nestes dois probabilidades nem classe podem ter valores negativos então garante que este será sempre substituido
        int bestClass = -1;
        for(int c = 0; c < priorC.length; c++){ //percorre todas as classes possíveis
            int[] x = new int[n]; //vetor atributos mais classe
            for(int i = 0; i < C; i++){ //copia atributos para o vetor completo
                x[i] = xSemClasse[i];
            }
            x[C] = c; //fixa a classe para testar na ultima posição
            double probability = prob(x); //calcula a probabilidade conjunta
            if (probability > bestProb) {
                bestProb = probability;
                bestClass = c; //caso a probabilidade seja maior diz que esta é a classe correta
            }
        }
        return bestClass; //devolve a classe
    }
}



                    
