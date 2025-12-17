import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.lang.Math;
import java.util.Random;

public class OrientedGraph {
    int n; // Número de nós
    ArrayList<ArrayList<Integer>> adj;

    /**
     * Método construtor que recebe um natural n e retorna o grafo com n nós e sem arestas.
     */
    public OrientedGraph(int n) {
        this.n = n;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < this.n; i++) {
            this.adj.add(new ArrayList<Integer>());
        }
    }

    /**
     * Recebe dois nós e adiciona ao grafo uma aresta de um nó para outro.
     */
    public void add_edge(int o, int d) {
        if (!this.adj.get(o).contains(d)) {
            this.adj.get(o).add(d);
        }
    }

    /**
     * Recebe dois nós e remove do grafo a aresta de um nó para outro.
     */
    public void remove_edge(int o, int d) {
        this.adj.get(o).remove((Integer) d);
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
    public ArrayList<Integer> parents(int o) {
        ArrayList<Integer> parentsList = new ArrayList<>();
        for (int i = 0; i < this.n; i++) {
            if (this.adj.get(i).contains(o)) {
                parentsList.add(i);
            }
        }
        return parentsList;
    }

    double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    /**
     * Calcula Pr(d_i, w_i, c)
     */
    double prdwc(Amostra amostra, int d_iIdx, int d_i, ArrayList<Integer> w_iIdx, ArrayList<Integer> w_i, int c) {
        // Construção do array vars com os indices das variáveis
        ArrayList<Integer> vars = new ArrayList<>();
        vars.add(d_iIdx);
        vars.addAll(w_iIdx);
        vars.add(amostra.element(0).length - 1); // Índice da variável de classificação (última variável)

        // Construção do array vals com os valores das variáveis
        ArrayList<Integer> vals = new ArrayList<>();
        vals.add(d_i);
        vals.addAll(w_i);
        vals.add(c);

        double prdwc = (double) amostra.count(vars, vals) / amostra.length(); // Casting para double para evitar divisão inteira
        return prdwc;
    }

    /**
     * Calcula Pr(d_i, c)
     */
    double prdc(Amostra amostra, int d_iIdx, int di, int c) {
        // Construção da lista vars com os indices das variáveis
        ArrayList<Integer> vars = new ArrayList<>();
        vars.add(d_iIdx);
        vars.add(amostra.element(0).length - 1); // Índice da variável de classificação (última variável)

        // Construção da lista vals com os valores das variáveis
        ArrayList<Integer> vals = new ArrayList<>();
        vals.add(di);
        vals.add(c);

        double prdc = (double) amostra.count(vars, vals) / amostra.length();
        return prdc;
    }

    /**
     * Calcula Pr(w_i, c)
     */
    double prwc(Amostra amostra, ArrayList<Integer> w_iIdx, ArrayList<Integer> wi, int c) {
        // Construção do array vars com os indices das variáveis
        ArrayList<Integer> vars = new ArrayList<>(w_iIdx);
        vars.add(amostra.element(0).length - 1); // Índice da variável de classificação (última variável)

        // Construção do array vals com os valores das variáveis
        ArrayList<Integer> vals = new ArrayList<>(wi);
        vals.add(c);

        double prwc = (double) amostra.count(vars, vals) / amostra.length();
        return prwc;
    }

    /**
     * Calcula Pr(c)
     */
    double prc(Amostra amostra, int c) {
        // Construção da lista vars com os indices das variáveis
        ArrayList<Integer> vars = new ArrayList<>();
        vars.add(amostra.element(0).length - 1); // Índice da variável de classificação (última variável)

        // Construção da lista vals com os valores das variáveis
        ArrayList<Integer> vals = new ArrayList<>();
        vals.add(c);

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
        ArrayList<Integer> w_iIdx = parents(d_iIdx);

        for (int i = 0; i < amostra.domain(d_iIdx); i++) { // para cada valor di
            for (ArrayList<Integer> w_i : amostra.combinations(w_iIdx)) { // para cada combinação de valores wi
                for (int w = 0; w < amostra.domain(amostra.element(0).length - 1); w++) { // para cada valor c
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

    /**
     * Calcula (log2(N) / 2) * θ
     */
    double penalizacao(Amostra amostra) {
        int D_c = amostra.domain(amostra.element(0).length - 1); // Domínio da variável de classificação

        double sum = 0.0;
        for (int i = 0; i < this.n - 1; i++) {
            int k_i = amostra.domain(i);
            int q_i = amostra.domain(parents(i));
            sum += (k_i - 1) * q_i * D_c;
        }

        double theta = (D_c - 1) + sum;
        return (log2(amostra.length()) / 2) * theta;
    }

    public double MDL(Amostra amostra) {
        return LL(amostra) - penalizacao(amostra);
    }

    public double MDLdelta(Amostra amostra, int o, int d, int op) {
        double mdl_before = MDL(amostra);

        if (op == 0) { // remover aresta
            if (!this.adj.get(o).contains(d)) {
                throw new IllegalArgumentException("A aresta não existe para ser removida");
            } else if (isCycle(d)) {
                throw new IllegalArgumentException("Remover a aresta criaria um ciclo");
            } else {
                remove_edge(o, d);
            }
        } else if (op == 1) { // inverter aresta
            if (!this.adj.get(o).contains(d)) {
                throw new IllegalArgumentException("A aresta não existe para ser invertida");
            } else if (isCycle(d)) {
                throw new IllegalArgumentException("Inverter a aresta criaria um ciclo");
            } else {
                invert_edge(o, d);
            }
        } else if (op == 2) { // adicionar aresta
            if (this.adj.get(o).contains(d)) {
                throw new IllegalArgumentException("A aresta já existe para ser adicionada");
            } else if (isCycle(d)) {
                throw new IllegalArgumentException("Adicionar a aresta criaria um ciclo");
            } else {
                add_edge(o, d);
            }
        }

        double mdl_after = MDL(amostra);
        return mdl_after - mdl_before;
    }

    @Override
    public String toString() {
        return "Grafo = " + adj.toString() + ", Número de vértices = " + n + ".";
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
        System.out.println(g.parents(3));

        Amostra amostra = ReadCSV.read("../DataSets/bcancer.csv");
        int d_iIdx = 3;
        ArrayList<Integer> parentsIdx = g.parents(d_iIdx);
        System.out.println(parentsIdx);

        double It = g.It(amostra, d_iIdx);
        double mdl = g.MDL(amostra);
        double mdldelta = g.MDLdelta(amostra, 0, 3, 2);
        double It2 = g.It(amostra, d_iIdx);

        System.out.printf("It - %.10f\n", It);
        System.out.printf("It2 - %.10f\n", It2);
        System.out.printf("MDL Score - %.10f\n", mdl);
        System.out.printf("MDL Delta (adicionar aresta 0->1) - %.10f\n", mdldelta);
        System.out.println();

    }
}