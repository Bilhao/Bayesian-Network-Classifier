import java.util.Arrays;

public class LOO {
    public static double LeaveOneOut(Amostra amostra, double S) {
        int acertos = 0;
        int N = amostra.length(); //numero de linhas
        for (int i = 0; i < N; i++) {
            Amostra treino = new Amostra();
            for (int j = 0; j < N; j++) {
                if (j!=i) {
                    treino.add(amostra.element(j));
                
                }
            }
            OrientedGraph grafoTeste = new OrientedGraph(amostra.element(0).length - 1);
            BN BNTeste = new BN(treino, grafoTeste, S);
            int[] linhaTeste = amostra.element(i);
            int[] xSemClasse = Arrays.copyOf(linhaTeste, linhaTeste.length - 1);
            int classePrevista = BNTeste.classify(xSemClasse);
            int classeReal = linhaTeste[linhaTeste.length -1];

            if (classePrevista == classeReal) {
                acertos ++;
            }
        }
        return acertos/ (double) N;
    }
}
