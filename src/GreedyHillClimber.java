import java.util.Random;

public class GreedyHillClimber {
    Amostra amostra;
    int maxParents;
    int numGraphs;

    Grafoo bestGraph; // Grafo com melhor MDL encontrado
    double bestMDL = Double.NEGATIVE_INFINITY;

    private Listener listener;

    private volatile boolean stopRequested = false;

    public interface Listener {
        void onProgress(int iteration, int totalIterations, String message);
    }

    public void interrupt() {
        this.stopRequested = true;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public GreedyHillClimber(Amostra amostra, int maxParents, int numGraphs) {
        super();
        this.amostra = amostra;
        this.maxParents = maxParents;
        this.numGraphs = numGraphs;
    }

    public Grafoo learn() {
        amostra.clearCache();

        int n = amostra.dim() - 1;

        if (numGraphs < 1) {
            numGraphs = 1;
        }

        java.util.concurrent.atomic.AtomicInteger completedIterations = new java.util.concurrent.atomic.AtomicInteger(0);
        Object lock = new Object();

        java.util.stream.IntStream.range(0, numGraphs).parallel().forEach(i -> {
            if (Thread.currentThread().isInterrupted() || stopRequested) {
                return;
            }

            Grafoo graph;
            if (i == 0) {
                graph = new Grafoo(n); // Grafo vazio
            } else {
                graph = randomGraph(n); // Grafo aleatório
            }

            graph = performGreedy(amostra, graph, maxParents, n);
            double score = graph.MDL(amostra);

            synchronized (lock) {
                if (score > bestMDL) {
                    bestMDL = score;
                    bestGraph = graph;
                }
            }

            int current = completedIterations.incrementAndGet();
            if (listener != null) {
                listener.onProgress(current, numGraphs, null);
            }
        });

        return bestGraph;
    }

    /**
     * Greedy hill climbing a partir de um grafo inicial: - testa todos os vizinhos (remove/invert/add) - aplica o melhor com delta - repete até não haver melhorias
     */
    private Grafoo performGreedy(Amostra amostra, Grafoo graph, int maxParents, int n) {

        while (true) {
            if (Thread.currentThread().isInterrupted() || stopRequested) {
                break;
            }
            double maxDelta = 0.0;
            int bestO = -1;
            int bestD = -1;
            int bestOp = -1; // 0=REMOVE, 1=INVERT, 2=ADD

            for (int o = 0; o < n; o++) {
                for (int d = 0; d < n; d++) {
                    if (o == d)
                        continue;

                    boolean hasEdge = graph.children(o).contains(d);

                    if (hasEdge) {
                        double delta = graph.MDLdelta(amostra, o, d, 0);
                        if (delta > maxDelta) {
                            maxDelta = delta;
                            bestO = o;
                            bestD = d;
                            bestOp = 0;
                        }

                        if (graph.parents(o).size() < maxParents && !createsCycle(graph, o, d, 1)) {
                            delta = graph.MDLdelta(amostra, o, d, 1);
                            if (delta > maxDelta) {
                                maxDelta = delta;
                                bestO = o;
                                bestD = d;
                                bestOp = 1;
                            }
                        }
                    } else {
                        if (graph.parents(d).size() < maxParents && !createsCycle(graph, o, d, 2)) {
                            double delta = graph.MDLdelta(amostra, o, d, 2);
                            if (delta > maxDelta) {
                                maxDelta = delta;
                                bestO = o;
                                bestD = d;
                                bestOp = 2;
                            }
                        }
                    }
                }
            }

            if (maxDelta <= 0.0) {
                break;
            } else {
                // Aplicar o melhor movimento permanentemente
                if (bestOp == 0) {
                    graph.remove_edge(bestO, bestD);
                } else if (bestOp == 1) {
                    graph.invert_edge(bestO, bestD);
                } else if (bestOp == 2) {
                    graph.add_edge(bestO, bestD);
                }
            }
        }
        return graph;
    }

    /**
     * Cria um grafo inicial aleatório: - adiciona sempre arestas C -> Xi (classe para todas as variáveis) - depois tenta adicionar arestas entre Xi respeitando: * maxParents (ignorando a classe) * aciclicidade
     */
    private Grafoo randomGraph(int n) {

        Grafoo g = new Grafoo(n);
        Random rand = new Random();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j)
                    continue;
                if (rand.nextBoolean()) {
                    if (g.parents(j).size() < maxParents && !g.connected(j, i)) {
                        g.add_edge(i, j);
                    }
                }
            }
        }
        return g;
    }

    private boolean createsCycle(Grafoo g, int o, int d, int op) {
        if (op == 1) {
            g.remove_edge(o, d);
            boolean hasCycle = g.connected(o, d);
            g.add_edge(o, d);
            return hasCycle;
        }
        if (op == 2) {
            return g.connected(d, o);
        }
        return false;
    }
}
