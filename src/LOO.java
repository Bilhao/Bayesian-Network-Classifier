import java.util.Arrays;

public class LOO { 
    public static double LeaveOneOut(Amostra amostra, double S) {
        int acertos = 0; //numero de classificação corretas
        int N = amostra.length(); //numero de linhas
        for (int i = 0; i < N; i++) { //usar cada linha
            Amostra treino = new Amostra();
            for (int j = 0; j < N; j++) { //adiciona todas as linhas exceto a escolhida
                if (j!=i) {
                    treino.add(amostra.element(j));
                
                }
            }
            OrientedGraph grafoTeste = new OrientedGraph(amostra.element(0).length - 1);
            BN BNTeste = new BN(treino, grafoTeste, S);
            int[] linhaTeste = amostra.element(i); //linha escolhida
            int[] iSemClasse = Arrays.copyOf(linhaTeste, linhaTeste.length - 1); //remove a classe
            int classePrevista = BNTeste.classify(iSemClasse);//classifica o exemplo teste
            int classeReal = linhaTeste[linhaTeste.length -1]; //ultima coluna da linha

            if (classePrevista == classeReal) {
                acertos ++;
            }
        }
        return acertos/ (double) N;
    }
}
