import java.util.ArrayList;
import java.util.LinkedList;
import java.io.Serializable;

public class Grafoo implements Serializable {
    private static final long serialVersionUID = 1L;
    int n; // Número de nós = dimensão do dataset excluindo a classe
    ArrayList<ArrayList<Integer>> adj;
    ArrayList<ArrayList<Integer>> adjParents;

    /**
     * Método construtor que recebe um natural n e retorna o grafo com n nós e sem arestas (vazio).
     */
    public Grafoo(int n) {
        super();
        this.n = n;
        this.adj = new ArrayList<>(n);
        this.adjParents = new ArrayList<>(n);
        for (int i = 0; i < this.n; i++) {
            this.adj.add(new ArrayList<Integer>());
            this.adjParents.add(new ArrayList<Integer>());
        }
    }

    /**
     * Recebe dois nós e adiciona ao grafo uma aresta de um nó para outro.
     */
    public void add_edge(int o, int d) {
        if (!this.adj.get(o).contains(d)) {
            this.adj.get(o).add(d);
            this.adjParents.get(d).add(o);
        }
    }

    /**
     * Recebe dois nós e remove do grafo a aresta de um nó para outro.
     */
    public void remove_edge(int o, int d) {
        this.adj.get(o).remove((Integer) d);
        this.adjParents.get(d).remove((Integer) o);
    }

    /**
     * Recebe dois nós e inverte a direção da aresta entre eles.
     */
    public void invert_edge(int o, int d) {
        remove_edge(o, d);
        add_edge(d, o);
    }

    /**
     * Recebe um nó e retorna a lista de nós filhos diretos do nó.
     */
    public ArrayList<Integer> children(int o) {
        return this.adj.get(o);
    }

    /**
     * Recebe dois nós e retorna true se existe um caminho de um nó para outro realizando uma busca em largura (BFS).
     */
    public boolean connected(int o, int d) {
        if (o == d)
            return true;

        LinkedList<Integer> queue = new LinkedList<>();
        boolean[] visited = new boolean[this.n];

        visited[o] = true;
        queue.add(o);

        while (!queue.isEmpty()) {
            int current = queue.removeFirst();
            for (int child : children(current)) {
                if (child == d) {
                    return true;
                }
                if (!visited[child]) {
                    visited[child] = true;
                    queue.add(child);
                }
            }
        }
        return false;
    }

    /**
     * Recebe um nó e retorna a lista de nós pais diretos do nó.
     */
    public ArrayList<Integer> parents(int o) {
        return this.adjParents.get(o);
    }

    double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    /**
     * Calcula It(X_i; Π_i | C) = ∑ Pr(d_i, w_i, c) * log2( (Pr(d_i, w_i, c) * Pr(c)) / (Pr(d_i, c) * Pr(w_i, c)) )
     * 
     * É a informação mútua condicional entre o nó i e os seus pais, dado a classe c.
     */
    double It(Amostra amostra, int nodeIdx) {
        ArrayList<Integer> parentsIdx = parents(nodeIdx);

        // Verificar cache primeiro
        Double cached = amostra.getCachedIt(nodeIdx, parentsIdx);
        if (cached != null) {
            return cached;
        }

        int classIdx = amostra.dim() - 1;

        int D_d = amostra.domain(nodeIdx);
        int D_w = amostra.domain(parentsIdx);
        int D_c = amostra.domain(classIdx);

        // Tabelas de contagem - uma matriz tridimensional que vai guardar quantas vezes cada combinação de (d_i, w_i, classe) aparece
        int[][][] count_dwc = new int[D_d][D_w][D_c];

        // Tabelas de contagem parciais - uma matriz bidimensional que vai guardar quantas vezes cada combinação de (d_i, classe) aparece
        int[][] count_dc = new int[D_d][D_c];

        // Tabelas de contagem parciais - uma matriz bidimensional que vai guardar quantas vezes cada combinação de (w_i, classe) aparece
        int[][] count_wc = new int[D_w][D_c];

        // Tabela de contagem parcial - um array unidimensional que vai guardar quantas vezes cada classe aparece
        int[] count_c = new int[D_c];

        for (int[] vector : amostra.vectorsList) {
            int d_val = vector[nodeIdx];
            int c_val = vector[classIdx];

            // Calcula o índice combinado dos pais (transforma uma combinação de valores num único índice, usando os domínios dos pais)
            int w_idx = 0;
            for (int pIdx : parentsIdx) {
                w_idx = w_idx * amostra.domain(pIdx) + vector[pIdx];
            }

            count_dwc[d_val][w_idx][c_val]++;
        }

        // Preencher as tabelas de contagem parciais
        for (int d = 0; d < D_d; d++) {
            for (int w = 0; w < D_w; w++) {
                for (int c = 0; c < D_c; c++) {
                    int cnt = count_dwc[d][w][c];
                    count_dc[d][c] += cnt;
                    count_wc[w][c] += cnt;
                    count_c[c] += cnt;
                }
            }
        }

        // Calcular It usando as contagens
        double it = 0.0;
        for (int d = 0; d < D_d; d++) {
            for (int w = 0; w < D_w; w++) {
                for (int c = 0; c < D_c; c++) {
                    int cnt_dwc = count_dwc[d][w][c];
                    if (cnt_dwc == 0)
                        continue;

                    int cnt_dc = count_dc[d][c];
                    int cnt_wc = count_wc[w][c];
                    int cnt_c = count_c[c];

                    // Probabilidades
                    double pr_dwc = (double) cnt_dwc / amostra.length();
                    double pr_dc = (double) cnt_dc / amostra.length();
                    double pr_wc = (double) cnt_wc / amostra.length();
                    double pr_c = (double) cnt_c / amostra.length();

                    // Fórmula da informação mútua condicional
                    if (pr_dc > 0 && pr_wc > 0 && pr_c > 0) {
                        it += pr_dwc * log2((pr_dwc * pr_c) / (pr_dc * pr_wc));
                    }
                }
            }
        }
        amostra.setCachedIt(nodeIdx, parentsIdx, it);
        return it;
    }

    /**
     * Calcula LL(G | D) = N * ∑ It(X_i; Π_i | C)
     */
    double LL(Amostra amostra) {
        double sum = 0.0;
        for (int i = 0; i < this.n; i++) {
            sum += It(amostra, i);
        }
        return amostra.length() * sum;
    }

    /**
     * Calcula (log2(N) / 2) * θ
     */
    double penalizacao(Amostra amostra) {
        int D_c = amostra.domain(amostra.dim() - 1);

        double sum = 0.0;
        for (int i = 0; i < this.n; i++) {
            int k_i = amostra.domain(i);
            int q_i = amostra.domain(parents(i));
            sum += (k_i - 1) * q_i * D_c;
        }

        double theta = (D_c - 1) + sum;
        return (log2(amostra.length()) / 2) * theta;
    }

    /**
     * Calcula o score (LL - penalização) para um único nó i.
     */
    double nodeScore(Amostra amostra, int i) {
        int classIdx = amostra.element(0).length - 1;
        int D_c = amostra.domain(classIdx);
        int k_i = amostra.domain(i);
        int q_i = amostra.domain(parents(i));

        double ll_i = amostra.length() * It(amostra, i);
        double penalty_i = (log2(amostra.length()) / 2) * (k_i - 1) * q_i * D_c;

        return ll_i - penalty_i;
    }

    public double MDL(Amostra amostra) {
        return LL(amostra) - penalizacao(amostra);
    }

    /**
     * Calcula a diferença de MDL após uma operação, calculando apenas os nós afetados. op: 0 = remover, 1 = inverter, 2 = adicionar
     */
    public double MDLdelta(Amostra amostra, int o, int d, int op) {
        double scoreBefore;
        double scoreAfter;

        if (op == 0) {
            // Remover o → d: só d é afetado (perde um pai)
            scoreBefore = nodeScore(amostra, d);
            remove_edge(o, d);
            scoreAfter = nodeScore(amostra, d);
            add_edge(o, d);
        } else if (op == 1) {
            // Inverter o → d para d → o: o e d são afetados
            scoreBefore = nodeScore(amostra, o) + nodeScore(amostra, d);
            invert_edge(o, d);
            scoreAfter = nodeScore(amostra, o) + nodeScore(amostra, d);
            invert_edge(d, o);
        } else {
            // Adicionar o → d: só d é afetado (ganha um pai)
            scoreBefore = nodeScore(amostra, d);
            add_edge(o, d);
            scoreAfter = nodeScore(amostra, d);
            remove_edge(o, d);
        }
        return scoreAfter - scoreBefore;
    }

    @Override
    public String toString() {
        return "OrientedGraph [n=" + n + ", adj=" + adj + "]";
    }
}