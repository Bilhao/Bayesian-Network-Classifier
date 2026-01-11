import java.util.Arrays;

public class LOO {

    public static double LeaveOneOut(Amostra amostra, int numGraphs, int maxParents) {
        int acertos = 0;
        int n = amostra.length();

        System.out.println("Executando LOO (n=" + n + ")...");

        for (int i = 0; i < n; i++) {
            GreedyHillClimber ghc = new GreedyHillClimber(amostra, maxParents, numGraphs);
            ghc.learn();
            Grafoo bestGraph = ghc.bestGraph;

            BN bn = new BN(amostra, bestGraph, 0.5);
            bn.optimizeS(amostra);

            int[] linhaTeste = amostra.element(i);
            int[] iSemClasse = Arrays.copyOf(linhaTeste, linhaTeste.length - 1);
            int classePrevista = bn.classify(iSemClasse);
            int classeReal = linhaTeste[linhaTeste.length - 1];

            if (classePrevista == classeReal) {
                acertos++;
            }
        }
        return acertos / (double) n;
    }

    public static void main(String[] args) {
        String file  = "DataSets/bcancer.csv";
        Amostra amostra = ReadCSV.read(file);

        int numGraphs = 100;
        int maxParents = 2;

        try {
            long startTime = System.currentTimeMillis();
            double acertos = LeaveOneOut(amostra, numGraphs, maxParents);
            long elapsed = System.currentTimeMillis() - startTime;

            System.out.println("Dataset: " + file);
            System.out.println("Acertos (Accuracy): " + String.format("%.2f", acertos * 100) + "%");
            System.out.println("Tempo: " + (elapsed / 1000) + "s");
            System.out.println("--------------------------------------------------");
        } catch (Exception e) {
            System.err.println("Erro ao processar " + file + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
