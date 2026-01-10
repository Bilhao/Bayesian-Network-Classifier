import java.util.Arrays;
import java.io.IOException;

public class LOO {
    public static double LeaveOneOut(Amostra amostra, String bnFilePath) throws IOException, ClassNotFoundException {
        int acertos = 0;
        int n = amostra.length();

        BN bnLoaded = BN.load(bnFilePath);

        for (int i = 0; i < n; i++) {
            int[] linhaTeste = amostra.element(i);
            int[] iSemClasse = Arrays.copyOf(linhaTeste, linhaTeste.length - 1);

            int classePrevista = bnLoaded.classify(iSemClasse);
            int classeReal = linhaTeste[linhaTeste.length - 1];

            if (classePrevista == classeReal) {
                acertos++;
            }
        }
        return acertos / (double) n;
    }

    public static void main(String[] args) {
        Amostra amostra = ReadCSV.read("DataSets/bcancer.csv");
        String bnFile = "TrainedBN/bcancer_network.bn";

        try {
            double acertos = LeaveOneOut(amostra, bnFile);
            System.out.println("Accuracy: " + String.format("%.2f", acertos * 100) + "%");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
