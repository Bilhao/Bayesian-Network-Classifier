import java.util.LinkedList;
import java.util.Arrays;

public class OrientedGraph {
    int n; // Número de nós
    LinkedList<Integer>[] adj;

    /**
     * Método construtor que recebe um natural n e retorna o grafo com n nós e sem
     * arestas.
     * 
     * @param n
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
     * 
     * @param o
     * @param d
     */
    public void add_edge(int o, int d) {
        if (!this.adj[o].contains(d)) {
            this.adj[o].add(d);
        }
    }

    /**
     * Recebe dois nós e remove do grafo a aresta de um nó para outro.
     * 
     * @param o
     * @param d
     */
    public void remove_edge(int o, int d) {
        this.adj[o].remove((Integer) d);
    }

    /**
     * Recebe dois nós e inverte a direção da aresta entre eles.
     * 
     * @param o
     * @param d
     */
    public void invert_edge(int o, int d) {
        remove_edge(o, d);
        add_edge(d, o);
    }

    /**
     * Recebe um nó e retorna a lista de nós filhos diretos do nó.
     * 
     * @param o
     */
    public LinkedList<Integer> children(int o) {
        return this.adj[o];
    }

    /**
     * Recebe um nó e retorna a lista de nós visitados em uma busca em largura (BFS)
     * a partir do nó.
     * 
     * @param o
     * @return
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
     * @param o
     * @param d
     */
    public boolean connected(int o, int d) {
        return o != d && BFS(o).contains(d); // "o != d" serve para excluir um caminho vazio, ou seja, sem aresta.
    }

    /**
     * Recebe um nó e retorna true se existe um ciclo que passa por esse nó.
     * @param o
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
     * 
     * @param o
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

    @Override
    public String toString() {
        return "Grafo = " + Arrays.toString(adj) + ", Número de vértices = " + n + ".";
    }

    public static void main(String[] args) {
        OrientedGraph g = new OrientedGraph(4);
        g.add_edge(0, 1);
        g.add_edge(0, 2);
        g.add_edge(1, 3);
        g.add_edge(2, 3);
        g.add_edge(3, 0);

        System.out.println(g);

        System.out.println(g.isCycle(0));
    }

}
