import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Classe para leitura de arquivos CSV e criação de objetos Amostra.
 */
public class ReadCSV {
    /**
     * Lê um arquivo CSV e retorna uma Amostra com os dados lidos.
     */
    public static Amostra read(String csvFile) {
        Amostra amostra = new Amostra();

        BufferedReader br = null; // Leitor de buffer para o arquivo
        String line = ""; // Linha temporária para leitura

        try {
            br = new BufferedReader(new FileReader(csvFile)); // Inicializa o leitor
            while ((line = br.readLine()) != null) {
                amostra.add(convert(line)); // Converte a linha e adiciona à amostra
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
        return amostra;
    }

    /**
     * Converte uma linha CSV em um vetor de inteiros.
     */
    private static int[] convert(String line) {
        String[] lineSplit = line.split(","); // Como é um CSV o separador é ","
        int[] vector = new int[lineSplit.length]; // Conversão de cada string dentro de lineSplit para int <=> VETOR
        for (int i = 0; i < lineSplit.length; i++) {
            vector[i] = Integer.parseInt(lineSplit[i]);
        }
        return vector;
    }        
}

    