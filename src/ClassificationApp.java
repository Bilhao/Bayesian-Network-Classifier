import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * Aplicacao de Classificacao usando Redes de Bayes.
 */
public class ClassificationApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new ClassificationFrame());
    }
}

class ClassificationFrame extends JFrame {
    private JTextField networkFileField;
    private JPanel parametersPanel;
    private JLabel classResultLabel;
    private JLabel confidenceLabel;
    private JTextArea probabilitiesArea;
    private JButton classifyButton;

    private BN network;
    private Amostra trainingData;
    private ArrayList<JSpinner> parameterSpinners;
    private String[] variableNames;

    public ClassificationFrame() {
        setTitle("Classificação com Redes de Bayes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 550);
        setResizable(false);
        setLocationRelativeTo(null);

        parameterSpinners = new ArrayList<>();

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Titulo
        JLabel titleLabel = new JLabel("Classificação de Pacientes");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Carregar rede
        mainPanel.add(createLabel("Rede de Bayes:", 13, false));
        mainPanel.add(Box.createVerticalStrut(5));
        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        filePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        networkFileField = new JTextField();
        networkFileField.setFont(new Font("Default", Font.PLAIN, 10));
        networkFileField.setEditable(false);
        JButton loadButton = new JButton("Carregar");
        loadButton.setFont(new Font("Default", Font.PLAIN, 10));
        loadButton.setFocusable(false);
        loadButton.addActionListener(e -> loadNetwork());

        filePanel.add(networkFileField, BorderLayout.CENTER);
        filePanel.add(loadButton, BorderLayout.EAST);
        mainPanel.add(filePanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Parametros do paciente
        mainPanel.add(createLabel("Parametros do Paciente:", 13, false));
        mainPanel.add(Box.createVerticalStrut(5));
        parametersPanel = new JPanel(new BorderLayout(5, 5));
        parametersPanel.setLayout(new GridLayout(0, 4, 8, 8));
        parametersPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel placeholder = new JLabel("Carregue uma rede primeiro");
        placeholder.setForeground(Color.GRAY);
        parametersPanel.add(placeholder);

        JScrollPane paramsScroll = new JScrollPane(parametersPanel);
        paramsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        paramsScroll.setPreferredSize(new Dimension(460, 100));
        paramsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        paramsScroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        mainPanel.add(paramsScroll);
        mainPanel.add(Box.createVerticalStrut(15));

        // Resultado
        mainPanel.add(createLabel("Resultado:", 13, false));
        mainPanel.add(Box.createVerticalStrut(5));
        JPanel resultPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        resultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel classPanel = new JPanel(new BorderLayout());
        classPanel.add(createLabel("Classe:", 11, true), BorderLayout.NORTH);
        classPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        classResultLabel = new JLabel("-");
        classResultLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        classResultLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        classResultLabel.setHorizontalAlignment(JLabel.CENTER);
        classPanel.add(classResultLabel, BorderLayout.CENTER);

        JPanel confPanel = new JPanel(new BorderLayout());
        confPanel.add(createLabel("Confiança:", 11, true), BorderLayout.NORTH);
        confPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        confidenceLabel = new JLabel("-");
        confidenceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        confidenceLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        confidenceLabel.setHorizontalAlignment(JLabel.CENTER);
        confPanel.add(confidenceLabel, BorderLayout.CENTER);

        resultPanel.add(classPanel);
        resultPanel.add(confPanel);
        mainPanel.add(resultPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Probabilidades
        mainPanel.add(createLabel("Probabilidades:", 13, false));
        mainPanel.add(Box.createVerticalStrut(5));
        probabilitiesArea = new JTextArea(4, 40);
        probabilitiesArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        probabilitiesArea.setEditable(false);
        JScrollPane probScroll = new JScrollPane(probabilitiesArea);
        probScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(probScroll);
        mainPanel.add(Box.createVerticalStrut(15));

        // Botoes
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JButton backButton = new JButton("Voltar");
        backButton.setFont(new Font("Default", Font.PLAIN, 10));
        backButton.setFocusable(false);
        backButton.addActionListener(e -> {
            dispose();
            MainApp.main(new String[] {});
        });

        JButton clearButton = new JButton("Limpar");
        clearButton.setFont(new Font("Default", Font.PLAIN, 10));
        clearButton.setFocusable(false);
        clearButton.addActionListener(e -> clearResults());

        classifyButton = new JButton("Classificar");
        classifyButton.setFont(new Font("Default", Font.PLAIN, 10));
        classifyButton.setFocusable(false);
        classifyButton.setEnabled(false);
        classifyButton.addActionListener(e -> classifyPatient());

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightButtons.add(clearButton);
        rightButtons.add(classifyButton);

        buttonPanel.add(backButton, BorderLayout.WEST);
        buttonPanel.add(rightButtons, BorderLayout.EAST);
        mainPanel.add(buttonPanel);

        add(mainPanel);
    }

    private JLabel createLabel(String text, int size, boolean needBorder) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, size));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (needBorder)
            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return label;
    }

    private void loadNetwork() {
        JFileChooser chooser = new JFileChooser();
        File dataSetsFolder = new File("TrainedBN");
        if (dataSetsFolder.exists()) {
            chooser.setCurrentDirectory(dataSetsFolder);
        }
        chooser.setFileFilter(new FileNameExtensionFilter("Rede de Bayes (*.bn)", "bn"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String networkPath = chooser.getSelectedFile().getAbsolutePath();

                network = BN.load(networkPath);
                networkFileField.setText(networkPath);

                int n = network.domains.length;
                variableNames = new String[n - 1];
                for (int i = 0; i < n - 1; i++) {
                    variableNames[i] = "X" + (i + 1);
                }

                createParameterFields();
                classifyButton.setEnabled(true);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar a rede: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void createParameterFields() {
        parametersPanel.removeAll();
        parameterSpinners.clear();

        int n = network.domains.length;

        parametersPanel.setLayout(new GridLayout(0, 4, 3, 3));

        for (int i = 0; i < n - 1; i++) {
            JPanel paramPanel = new JPanel(new BorderLayout(3, 0));
            paramPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JLabel label = new JLabel(variableNames[i] + ":");
            label.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            int maxVal = Math.max(0, network.domains[i]);
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, maxVal, 1));
            parameterSpinners.add(spinner);

            paramPanel.add(label, BorderLayout.WEST);
            paramPanel.add(spinner, BorderLayout.CENTER);
            parametersPanel.add(paramPanel);
        }

        parametersPanel.revalidate();
        parametersPanel.repaint();
    }

    private void classifyPatient() {
        if (network == null || trainingData == null) {
            JOptionPane.showMessageDialog(this, "Carregue uma rede primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int n = network.domains.length;
        int[] instance = new int[n - 1];

        for (int i = 0; i < n - 1; i++) {
            instance[i] = (Integer) parameterSpinners.get(i).getValue();
        }

        int predictedClass = network.classify(instance);
        double[] probabilities = network.getProbabilities(instance);

        classResultLabel.setText(String.valueOf(predictedClass));

        double confidence = probabilities[predictedClass] * 100;
        confidenceLabel.setText(String.format("%.1f%%", confidence));

        StringBuilder probText = new StringBuilder();
        for (int c = 0; c < probabilities.length; c++) {
            probText.append(String.format("Classe %d: %.4f (%.2f%%)", c, probabilities[c], probabilities[c] * 100));
            if (c == predictedClass)
                probText.append(" *");
            probText.append("\n");
        }
        probabilitiesArea.setText(probText.toString());
    }

    private void clearResults() {
        classResultLabel.setText("-");
        confidenceLabel.setText("-");
        probabilitiesArea.setText("");

        for (JSpinner spinner : parameterSpinners) {
            spinner.setValue(0);
        }
    }
}
