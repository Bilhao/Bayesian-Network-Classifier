import java.util.LinkedList;
import java.util.Arrays;
import java.lang.Math;
import java.util.Random;

public class OrientedGraph {
    int n; // Número de nós
    LinkedList<Integer>[] adj;

    /**
     * Método construtor que recebe um natural n e retorna o grafo com n nós e sem arestas.
     */
    @SuppressWarnings("unchecked")
    public OrientedGraph(int n) {
        this.n = n;
        this.adj = new LinkedList[n];
        for (int i = 0; i < this.n; i++) {
            this.adj[i] = new LinkedList<Integer>();
        }
    }

    /**
     * Recebe dois nós e adiciona ao grafo uma aresta de um nó para outro.
     */
    public void add_edge(int o, int d) {
        if (!this.adj[o].contains(d)) {
            this.adj[o].add(d);
        }
    }

    /**
     * Recebe dois nós e remove do grafo a aresta de um nó para outro.
     */
    public void remove_edge(int o, int d) {
        this.adj[o].remove((Integer) d);
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
    public LinkedList<Integer> children(int o) {
        return this.adj[o];
    }

    /**
     * Recebe um nó e retorna a lista de nós visitados em uma busca em largura (BFS) a partir do nó.
     */
    public LinkedList<Integer> BFS(int o) {
        LinkedList<Integer> queue = new LinkedList<>();
        LinkedList<Integer> order = new LinkedList<>();
        boolean[] visited = new boolean[this.n];

        visited[o] = true;
        queue.add(o);

        while (!queue.isEmpty()) {
            int v = queue.removeFirst();
            order.add(v);
            for (int w : children(v)) {
                if (!visited[w]) {
                    visited[w] = true;
                    queue.add(w);
                }
            }
        }
        return order;
    }

    /**
     * Recebe dois nós e retorna true se existe um caminho de um nó para outro.
     */
    public boolean connected(int o, int d) {
        for (int child : children(o)) {
            if (BFS(child).contains(d)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Recebe um nó e retorna true se existe um ciclo que passa por esse nó.
     */
    public boolean isCycle(int o) {
        for (int child : children(o)) {
            if (BFS(child).contains(o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Recebe um nó e retorna a lista de nós pais diretos do nó.
     */
    public int[] parents(int o) {
        LinkedList<Integer> parentsList = new LinkedList<>();
        for (int i = 0; i < this.n; i++) {
            if (this.adj[i].contains(o)) {
                parentsList.add(i);
            }
        }
        int[] parentsArray = new int[parentsList.size()];
        for (int i = 0; i < parentsList.size(); i++) {
            parentsArray[i] = parentsList.get(i);
        }
        return parentsArray;
    }

    double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    /**
     * Calcula Pr(d_i, w_i, c)
     */
    double prdwc(Amostra amostra, int d_iIdx, int d_i, int[] w_iIdx, int[] w_i, int c) {
        // Construção do array vars com os indices das variáveis
        int[] vars = new int[1 + w_iIdx.length + 1];

        vars[0] = d_iIdx;
        for (int i = 0; i < w_iIdx.length; i++) {
            vars[i + 1] = w_iIdx[i];
        }
        vars[vars.length - 1] = amostra.element(0).length - 1; // Índice da variável de classificação (última variável)

        // Construção do array vals com os valores das variáveis
        int[] vals = new int[1 + w_i.length + 1];
        vals[0] = d_i;
        for (int i = 0; i < w_i.length; i++) {
            vals[i + 1] = w_i[i];
        }
        vals[vals.length - 1] = c;

        double prdwc = (double) amostra.count(vars, vals) / amostra.length(); // Casting para double para evitar divisão inteira
        return prdwc;
    }

    /**
     * Calcula Pr(d_i, c)
     */
    double prdc(Amostra amostra, int d_iIdx, int di, int c) {
        // Construção do array vars com os indices das variáveis
        int[] vars = new int[2];
        vars[0] = d_iIdx;
        vars[1] = amostra.element(0).length - 1; // Índice da variável de classificação (última variável)

        // Construção do array vals com os valores das variáveis
        int[] vals = new int[2];
        vals[0] = di;
        vals[1] = c;

        double prdc = (double) amostra.count(vars, vals) / amostra.length();
        return prdc;
    }

    /**
     * Calcula Pr(w_i, c)
     */
    double prwc(Amostra amostra, int[] w_iIdx, int[] wi, int c) {
        // Construção do array vars com os indices das variáveis
        int[] vars = new int[w_iIdx.length + 1];
        for (int i = 0; i < w_iIdx.length; i++) {
            vars[i] = w_iIdx[i];
        }
        vars[vars.length - 1] = amostra.element(0).length - 1; // Índice da variável de classificação (última variável)

        // Construção do array vals com os valores das variáveis
        int[] vals = new int[wi.length + 1];
        for (int i = 0; i < wi.length; i++) {
            vals[i] = wi[i];
        }
        vals[vals.length - 1] = c;

        double prwc = (double) amostra.count(vars, vals) / amostra.length();
        return prwc;
    }

    /**
     * Calcula Pr(c)
     */
    double prc(Amostra amostra, int c) {
        // Construção do array vars com os indices das variáveis
        int[] vars = new int[1];
        vars[0] = amostra.element(0).length - 1; // Índice da variável de classificação (última variável)

        // Construção do array vals com os valores das variáveis
        int[] vals = new int[1];
        vals[0] = c;

        double prc = (double) amostra.count(vars, vals) / amostra.length();
        return prc;
    }

    /**
     * Calcula It(X_i; Π_i | C) = ∑ Pr(d_i, w_i, c) * log2( (Pr(d_i, w_i, c) * Pr(c)) / (Pr(d_i, c) * Pr(w_i, c)) )
     * 
     * É a informação mútua condicional entre o nó i e os seus pais, dado a classe c.
     */
    double It(Amostra amostra, int d_iIdx) {
        double it = 0.0;
        int[] w_iIdx = parents(d_iIdx);

        for (int i = 0; i < amostra.domain(new int[] { d_iIdx }); i++) { // para cada valor di
            for (int[] w_i : amostra.combinations(w_iIdx)) { // para cada combinação de valores wi
                for (int w = 0; w < amostra.domain(new int[] { amostra.element(0).length - 1 }); w++) { // para cada valor c
                    double prdwc = prdwc(amostra, d_iIdx, i, w_iIdx, w_i, w);
                    double prc = prc(amostra, w);
                    double prdc = prdc(amostra, d_iIdx, i, w);
                    double prwc = prwc(amostra, w_iIdx, w_i, w);

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

    double penalizacao(Amostra amostra) {
        double theta = 0.0;
        int D_c = amostra.domain(new int[] { amostra.element(0).length - 1 }); // Domínio da variável de classificação

        double sum = 0.0;
        for (int i = 0; i < this.n; i++) {
            int k_i = amostra.domain(new int[] { i });
            int q_i = amostra.domain(parents(k_i));
            sum += (k_i - 1) * q_i * D_c;
        }

        theta = (D_c - 1) + sum;
        return (log2(amostra.length()) / 2) * theta;
    }

    public double MDL(Amostra amostra) {
        return LL(amostra) - penalizacao(amostra);
    }

    @Override
    public String toString() {
        return "Grafo = " + Arrays.toString(adj) + ", Número de vértices = " + n + ".";
    }

    public static void main(String[] args) {
        OrientedGraph g = new OrientedGraph(10);
        g.add_edge(0, 1);
        g.add_edge(0, 2);
        g.add_edge(3, 2);
        // g.add_edge(2, 3);
        // g.add_edge(3, 4);
        // g.add_edge(4, 5);
        // g.add_edge(4, 2);
        // g.add_edge(5, 6);
        // g.add_edge(5, 8);
        // g.add_edge(6, 7);
        // g.add_edge(7, 8);
        // g.add_edge(8, 9);
        // g.add_edge(8, 6);
        // g.add_edge(9, 10);

        System.out.println(g);
        System.out.println(Arrays.toString(g.parents(3)));

        Amostra amostra = ReadCSV.read("../DataSets/bcancer.csv");
        int d_iIdx = 0;
        int[] parentsIdx = g.parents(d_iIdx);
        System.out.println(Arrays.toString(parentsIdx));

        double It = g.It(amostra, d_iIdx);
        double mdl = g.MDL(amostra);

        System.out.printf("It - %.10f\n", It);
        System.out.printf("MDL Score - %.10f\n", mdl);
        System.out.println();
    }
}