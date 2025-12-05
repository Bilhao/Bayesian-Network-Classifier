import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class Amostra {
    ArrayList<int[]> sampleLists; // Lista de vetores em que cada vetor é a amostra de um paciente
    int[] maxs;

    public Amostra() {
        super();
        this.sampleLists = new ArrayList<int[]>();
        this.maxs = null;

    }

    @Override
    public String toString() {
        return "Amostra [sampleLists = " + sampleLists + ", maxs = " + Arrays.toString(maxs);
    }

    public void add(int[] x) {
        this.sampleLists.add(x);
        if (this.maxs == null) {
            this.maxs = new int[x.length];
            for (int i = 0; i < x.length; i++) {
                this.maxs[i] = x[i];
            }
        } else {
            for (int i = 0; i < x.length; i++) {
                if (x[i] > this.maxs[i]) {
                    this.maxs[i] = x[i];
                }
            }
        }
    }

    public int length() {
        return this.sampleLists.size();
    }

    public int[] element(int i) {
        return this.sampleLists.get(i);
    }

    public static String show(ArrayList<int[]> lista) {
        String s = "";
        for (int[] x : lista)
            s = s + Arrays.toString(x) + ",";
        return s;
    }

    public int domain(int[] v) {
        int r = 1;
        for (int i = 0; i < v.length; i++) {
            r = r * (this.maxs[v[i]] + 1);
        }
        return r;
    }

    public static boolean condQ(int[] vars, int[] vals, int[] x) {
        boolean b = true;
        for (int i = 0; i < vars.length; i++) {
            if (x[vars[i]] != vals[i]) {
                b = false;
            }
        }
        return b;
    }

    public static void main(String[] args) {
        Amostra a = new Amostra();
        System.out.println(a);
        int[] m1 = { 1, 2, 3, 4 };
        int[] m2 = { 3, 1, 5, 2 };
        a.add(m1);
        a.add(m2);
        int v[] = { 0, 2 };
        System.out.println(a.domain(v));

    }

}
