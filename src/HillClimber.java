
import java.util.ArrayList;
import java.util.Random;

public class HillClimber {

    private final Random rng;

    public HillClimber() {
        this.rng = new Random();
    }

    public OrientedGraph learn(Amostra amostra, int maxParents, int numRandomStarts) {

        int n = amostra.element(0).length;   // Nº total de colunas
        int classIdx = n - 1;                // índice da classe (última coluna)

        if (numRandomStarts < 1) { // garante que temos pelo menos um grafo inicial
            numRandomStarts = 1;
        }

        OrientedGraph bestG = null;  // o melhor grafo encontrado
        double bestScore = Double.NEGATIVE_INFINITY;

        // 1) inicializar grafo vazio e testar
        OrientedGraph empty = new OrientedGraph(n);
        OrientedGraph local0 = greedyFromStart(amostra, empty, maxParents, classIdx, n);
        double score0 = local0.MDL(amostra);

        bestG = cloneGraph(local0, n);
        bestScore = score0;

        // 2) Hill climb a partir de pontos aleatórios
        for (int s = 1; s < numRandomStarts; s++) {
            OrientedGraph start = randomStart(n, classIdx, maxParents);
            OrientedGraph local = greedyFromStart(amostra, start, maxParents, classIdx, n);
            double score = local.MDL(amostra);

            if (score > bestScore) {
                bestScore = score;
                bestG = cloneGraph(local, n);
            }
        }

        return bestG;
    }

    /**
     * Greedy hill climbing a partir de um grafo inicial:
     * - testa todos os vizinhos (remove/invert/add)
     * - aplica o melhor com delta>0
     * - repete até não haver melhorias
     */
    private OrientedGraph greedyFromStart(Amostra amostra, OrientedGraph start, int maxParents, int classIdx, int n) {

        OrientedGraph g = cloneGraph(start, n);

        while (true) {
            double bestDelta = 0.0;
            int bestO = -1;
            int bestD = -1;
            int bestOp = -1; // 0=REMOVE, 1=INVERT, 2=ADD

            // 1) testar REMOVE e INVERT em arestas existentes (ignorando arestas envolvendo a classe)
            for (int o = 0; o <= classIdx - 1; o++) {
                ArrayList<Integer> childs = new ArrayList<>(g.children(o)); // cópia segura

                for (int d : childs) {
                    if (d == classIdx) continue; // não mexer em arestas que vão para a classe
                    // (se também não quiser mexer em arestas que saem da classe, isso já está garantido porque o< classIdx)

                    double deltaRemove = g.MDLdelta(amostra, o, d, 0);
                    if (deltaRemove > bestDelta) {
                        bestDelta = deltaRemove;
                        bestO = o;
                        bestD = d;
                        bestOp = 0;
                    }

                    if (canInvert(g, o, d, maxParents, classIdx)) {
                        double deltaInvert = g.MDLdelta(amostra, o, d, 1);
                        if (deltaInvert > bestDelta) {
                            bestDelta = deltaInvert;
                            bestO = o;
                            bestD = d;
                            bestOp = 1;
                        }
                    }
                }
            }

            // 2) testar ADD para arestas que não existem (ignorando a classe)
            for (int o = 0; o <= classIdx - 1; o++) {
                for (int d = 0; d <= classIdx - 1; d++) {
                    if (o == d) continue;
                    if (g.children(o).contains(d)) continue;
                    if (!canAdd(g, o, d, maxParents, classIdx)) continue;

                    double deltaAdd = g.MDLdelta(amostra, o, d, 2);
                    if (deltaAdd > bestDelta) {
                        bestDelta = deltaAdd;
                        bestO = o;
                        bestD = d;
                        bestOp = 2;
                    }
                }
            }

            // parar se não há melhoria
            if (bestDelta <= 0.0) {
                break;
            }

            // aplicar o melhor movimento permanentemente
            if (bestOp == 0) {
                g.remove_edge(bestO, bestD);
            } else if (bestOp == 1) {
                g.invert_edge(bestO, bestD);
            } else { // bestOp == 2
                g.add_edge(bestO, bestD);
            }
        }

        return g;
    }

    /**
     * Cria um grafo inicial aleatório:
     * - adiciona sempre arestas C -> Xi (classe para todas as variáveis)
     * - depois tenta adicionar arestas entre Xi respeitando:
     *   * maxParents (ignorando a classe)
     *   * aciclicidade
     */
    private OrientedGraph randomStart(int n, int classIdx, int maxParents) {

        OrientedGraph g = new OrientedGraph(n);

        // (opcional mas normalmente exigido no projeto): classe -> todos os atributos
        for (int i = 0; i <= classIdx - 1; i++) {
            g.add_edge(classIdx, i);
        }

        if (maxParents <= 0) return g;

        // Para cada nó d (atributo), escolhe um número alvo de pais (0..maxParents)
        for (int d = 0; d <= classIdx - 1; d++) {
            int targetParents = (maxParents == 0) ? 0: rng.nextInt(maxParents +1);

            int tries = 0;

            // tenta adicionar pais até ou ter 2 pais ou dps de 50 tentativas
            while (parentsCountIgnoringClass(g,d,classIdx) < targetParents && tries < 50){
                int o = rng.nextInt(classIdx);

                if (o == d) { tries ++; continue;}  // nao permitir ciclos 

                if (g.children(o).contains(d)) { tries++; continue;} // se ja existir o caminho o -> d, nao adicionar again

                if (canAdd(g, o, d, maxParents, classIdx)){ // se é valido adicionar, entao adiciona
                    g.add_edge(o,d);
                }

                tries++; 
            }
        }
        return g; 
    }

    private OrientedGraph cloneGraph(OrientedGraph g, int n){
        OrientedGraph copy = new OrientedGraph(n);

        for (int o = 0; o < n; o ++){
            for (int d : g.children(o)) {
                copy.add_edge(o, d);
            }
        }
        return copy; 
    }

    private int parentsCountIgnoringClass(OrientedGraph g, int node, int classIdx){
        int count = 0;
        for (int p : g.parents(node)){  //lista dos pais 
            if (p != classIdx) count++;  // se o p nao for a classe, então conta
        }
        return count;
    }

    private boolean canAdd(OrientedGraph g, int o, int d, int maxParents, int classIdx){
        if (o == classIdx || d == classIdx) return false;  // as adições nao podem afetar a classe

        if (g.connected(d,o)) return false; // proibe self loops, tipo o -> o

        return parentsCountIgnoringClass(g, d, classIdx) < maxParents;
    }

    private boolean canInvert(OrientedGraph g, int o, int d, int maxParents, int classIdx){
        if (o == classIdx || d == classIdx) return false; 

        g.remove_edge(o,d); 
        boolean wouldCreateCycle = g.connected(o,d);
        g.add_edge(o,d);

        if (wouldCreateCycle) return false; 

        int parentsO = parentsCountIgnoringClass(g, o,classIdx);
        return parentsO + 1 <= maxParents;



    }
    /* =======================
       SIMPLE SELF-TEST SECTION
       ======================= */

    public static void main(String[] args) {

        // ---------- 1) Build a tiny dataset ----------
        // 3 features + 1 class (class is last index)
        Amostra a = new Amostra();

        a.add(new int[]{0, 0, 0, 0});
        a.add(new int[]{0, 1, 0, 0});
        a.add(new int[]{1, 0, 1, 1});
        a.add(new int[]{1, 1, 1, 1});
        a.add(new int[]{0, 0, 1, 0});
        a.add(new int[]{1, 0, 0, 1});
        a.add(new int[]{0, 1, 1, 0});
        a.add(new int[]{1, 1, 0, 1});

        // ---------- 2) Test MDLdelta undo safety ----------
        OrientedGraph tg = new OrientedGraph(a.element(0).length);
        tg.add_edge(0, 1);
        tg.add_edge(1, 2);

        String before = tg.toString();
        tg.MDLdelta(a, 0, 1, 0); // test REMOVE
        String after = tg.toString();

        if (!before.equals(after)) {
            throw new RuntimeException("❌ MDLdelta changed the graph permanently!");
        }
        System.out.println("MDLdelta undo test passed ✅");

        // ---------- 3) Run Hill Climber ----------
        HillClimber hc = new HillClimber();

        int maxParents = 2;
        int numRandomStarts = 10;

        OrientedGraph g = hc.learn(a, maxParents, numRandomStarts);

        System.out.println("Learned graph:");
        System.out.println(g);
        System.out.println("MDL = " + g.MDL(a));

        // ---------- 4) Structural checks ----------
        checkNoCycles(g);
        checkMaxParentsIgnoringClass(g, a, maxParents);

        System.out.println("All HillClimber tests passed ✅");
    }

    // ---------- Helper: check acyclicity ----------
    private static void checkNoCycles(OrientedGraph g) {
        for (int o = 0; o < g.n; o++) {
            for (int d : g.children(o)) {
                if (g.connected(d, o)) {
                    throw new RuntimeException(
                        "❌ Cycle detected involving edge " + o + " -> " + d
                    );
                }
            }
        }
    }

    // ---------- Helper: check maxParents ignoring class ----------
    private static void checkMaxParentsIgnoringClass(OrientedGraph g,
                                                     Amostra a,
                                                     int maxParents) {

        int classIdx = a.element(0).length - 1;

        for (int node = 0; node < classIdx; node++) { // only features
            int count = 0;
            for (int p : g.parents(node)) {
                if (p != classIdx) count++;
            }
            if (count > maxParents) {
                throw new RuntimeException(
                    "❌ Too many parents on node " + node + ": " + count
                );
            }
        }
    }







}




















































