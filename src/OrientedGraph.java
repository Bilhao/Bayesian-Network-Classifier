import java.util.ArrayList;
import java.util.LinkedList;
import java.lang.Math;

public class OrientedGraph {
    int n; // Número de nós = dimensão do dataset excluindo a classe
    ArrayList<ArrayList<Integer>> adj;
    ArrayList<ArrayList<Integer>> adjParents;

    /**
     * Método construtor que recebe um natural n e retorna o grafo com n nós e sem arestas (vazio).
     */
    public OrientedGraph(int n) {
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
     * Recebe um nó e retorna a lista de nós visitados em uma busca em largura (BFS) a partir do nó.
     */
    public ArrayList<Integer> BFS(int o) {
        LinkedList<Integer> queue = new LinkedList<>(); // LinkedList como fila (eficiente para removeFirst)
        ArrayList<Integer> order = new ArrayList<>();
        boolean[] visited = new boolean[this.n];

        visited[o] = true;
        queue.add(o);

        while (!queue.isEmpty()) {
            int current = queue.removeFirst();
            order.add(current);
            for (int child : children(current)) {
                if (!visited[child]) {
                    visited[child] = true;
                    queue.add(child);
                }
            }
        }
        return order;
    }

    /**
     * Recebe dois nós e retorna true se existe um caminho de um nó para outro.
     */
    public boolean connected(int o, int d) {
        if (o == d)
            return true;

        LinkedList<Integer> queue = new LinkedList<>();
        boolean[] visited = new boolean[this.n];

        queue.add(o);
        visited[o] = true;
        while (!queue.isEmpty()) {
            int current = queue.removeFirst();
            if (current == d)
                return true;
            for (int child : children(current)) {
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
    double It(Amostra amostra, int d_iIdx) {
        double it = 0.0;
        ArrayList<Integer> w_iIdx = parents(d_iIdx);
        int classIdx = amostra.element(0).length - 1;

        for (int i = 0; i < amostra.domain(d_iIdx); i++) { // para cada valor di
            for (ArrayList<Integer> w_i : amostra.combinations(w_iIdx)) { // para cada combinação de valores wi
                for (int c = 0; c < amostra.domain(classIdx); c++) { // para cada valor c

                    ArrayList<Integer> dwcVars = new ArrayList<>();
                    ArrayList<Integer> dwcVals = new ArrayList<>();
                    ArrayList<Integer> dcVars = new ArrayList<>();
                    ArrayList<Integer> dcVals = new ArrayList<>();
                    ArrayList<Integer> wcVars = new ArrayList<>();
                    ArrayList<Integer> wcVals = new ArrayList<>();
                    ArrayList<Integer> cVars = new ArrayList<>();
                    ArrayList<Integer> cVals = new ArrayList<>();

                    dwcVars.add(d_iIdx);
                    dwcVars.addAll(w_iIdx);
                    dwcVars.add(classIdx);
                    dwcVals.add(i);
                    dwcVals.addAll(w_i);
                    dwcVals.add(c);

                    dcVars.add(d_iIdx);
                    dcVars.add(classIdx);
                    dcVals.add(i);
                    dcVals.add(c);

                    wcVars.addAll(w_iIdx);
                    wcVars.add(classIdx);
                    wcVals.addAll(w_i);
                    wcVals.add(c);

                    cVars.add(classIdx);
                    cVals.add(c);

                    double prdwc = (double) amostra.count(dwcVars, dwcVals) / amostra.length();
                    double prdc = (double) amostra.count(dcVars, dcVals) / amostra.length();
                    double prwc = (double) amostra.count(wcVars, wcVals) / amostra.length();
                    double prc = (double) amostra.count(cVars, cVals) / amostra.length();

                    if (prdwc > 0 && prc > 0 && prdc > 0 && prwc > 0) {
                        it += prdwc * log2((prdwc * prc) / (prdc * prwc));
                    }
                }
            }
        }

        return it;
    }

    /**
     * Calcula LL(G | D) = N * ∑ It(X_i; Π_i | C)
     */
    double LL(Amostra amostra) {
        double sum = 0.0;
        for (int i = 0; i < this.n - 1; i++) { // Para cada nó (excluindo o nó de classificação)
            sum += It(amostra, i);
        }
        return amostra.length() * sum;
    }

    /**
     * Calcula (log2(N) / 2) * θ
     */
    double penalizacao(Amostra amostra) {
        int D_c = amostra.domain(amostra.dim() - 1); // Domínio da variável de classificação

        double sum = 0.0;
        for (int i = 0; i < this.n - 1; i++) {
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
            // Remover o → d significa que só d é afetado no score (perde um pai)
            scoreBefore = nodeScore(amostra, d);
            remove_edge(o, d);
            scoreAfter = nodeScore(amostra, d);
            add_edge(o, d);
        } else if (op == 1) {
            // Inverter o → d para d → o significa que o e d são afetados no score
            scoreBefore = nodeScore(amostra, o) + nodeScore(amostra, d);
            invert_edge(o, d);
            scoreAfter = nodeScore(amostra, o) + nodeScore(amostra, d);
            invert_edge(d, o);
        } else {
            // Adicionar o → d significa que só d é afetado no score (ganha um pai)
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