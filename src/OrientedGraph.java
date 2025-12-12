import java.util.LinkedList;
import java.util.Arrays;

public class OrientedGraph {
    int n;
    LinkedList<Integer>[] adj;

    /*
    Método construtor que recebe um natural n e retorna o grafo com n nós e sem arestas.
    */
   @SuppressWarnings("unchecked")
    public OrientedGraph(int n) {
        this.n = n;
        this.adj = new LinkedList[n];
        for (int i = 0; i < this.n; i++) {
            this.adj[i] = new LinkedList<Integer>();
        }
    }

    /*
    Recebe dois nós e adiciona ao grafo uma aresta de um nó para outro.
    */
    public void add_edge(int o, int d) {
        if (!this.adj[o].contains(d)) {
            this.adj[o].add(d);
        }
    }

    /*
    Recebe dois nós e retira ao grafo uma aresta de um nó para outro.
    */
    public void remove_edge(int o, int d) {
        this.adj[o].remove((Integer)d);
    }

    /*
    Recebe dois nós e inverte no grafo a aresta de um nó para outro.
    */
    public void invert_edge(int o, int d) {
        remove_edge(o,d);
        add_edge(d,o);
    }

    /*
    Retorna os descendentes de um vértice.
    */
    public LinkedList<Integer> children(int o) {
        return this.adj[o];
    }
    
    /*
    Pesquisa em largura como método auxiliar.
    */
    public LinkedList<Integer> BFS(int o) {
        LinkedList<Integer> queue =  new LinkedList<>();
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

    /*
    Recebe dois nós e verifica se há um caminho (não vazio) de um nó para o outro.
    */
    public boolean connected(int o, int d) {
        return o != d && BFS(o).contains(d);  //"o != d" serve para excluir um caminho vazio, ou seja, sem aresta.
    }

    /* 
    Recebe um nó e retorna a lista de nós que são pais do nó.
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

    public String toString() {
        return "Grafo = " + Arrays.toString(adj) + ", Número de vértices = " + n + ".";
    }
    
    public static void main(String[] args) {
        OrientedGraph g = new OrientedGraph(3);
        g.add_edge(0, 1);
        g.add_edge(0, 2);
        g.add_edge(1,2);
        System.out.println(g);
    }

}
