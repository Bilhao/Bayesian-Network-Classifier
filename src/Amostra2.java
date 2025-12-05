import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.Buffer;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;

public class Amostra2 implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<int[]> listaAmostras;
    // Talvez definir o domínio como um atributo da classe Amostra, mas não entendi
    // direito o motivo de existência dessa coisa.

    public Amostra2() {
        this.listaAmostras = new ArrayList<int[]>();
    }

    public Amostra2(String csvFile) {
        this.listaAmostras = new ArrayList<int[]>();
        // Se definir o dominio como atributo, inicializar no construtor.

        BufferedReader br = null;
        String line = "";

        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                // Converter e Adicionar à Amostra.
                add(convert(line));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static int[] convert(String line) {
        // Como é um CSV o separador é ",":
        String[] lineSplit = line.split(",");
        // Conversão de cada string dentro de lineSplit para int <=> VETOR
        int[] vector = new int[lineSplit.length];
        for (int i = 0; i < lineSplit.length; i++) {
            vector[i] = Integer.parseInt(lineSplit[i]);
        }
        return vector;
    }

    public void add(int[] vector) {
        this.listaAmostras.add(vector);
    }

    public int length() {
        return this.listaAmostras.size();
    }

    public int[] element(int position) {
        return this.listaAmostras.get(position);
    }

    public int domain(Amostra amostra, int[] positionVector) {
        int r = 1;
        for (int i = 0; i < amostra.length(); i++) {
            int[] temp = new int[positionVector.length];
            for (int j = 0; j < positionVector.length; j++) {
                temp[j] = amostra.element(i)[positionVector[j]];
            }
        }
        return r;
    }

    public static void main(String[] args) {
        Amostra2 amostra = new Amostra2("bcancer.csv");
        System.out.println("Amostra: " + amostra);
    }

}
