import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;

/**
 * Aplicacao de Treinamento de Redes de Bayes Classificadoras (BNC).
 */
public class TrainingApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new TrainingFrame());
    }
}

class TrainingFrame extends JFrame {
    private JTextField fileField;
    private JTextField outputField;
    private JTextField maxParentsField;
    private JTextField numGraphsField;
    private JTextField pseudoCountField;
    private JCheckBox optimizeSCheckbox;
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JButton startButton;
    private JButton stopButton;
    private JLabel statusLabel;
    private volatile boolean stopRequested = false;
    private SwingWorker<Void, String> currentWorker;

    private Amostra amostra;
    private GreedyHillClimber ghc;
    private String selectedFilePath;

    public TrainingFrame() {
        setTitle("Aprendizagem de Redes de Bayes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 520);
        setResizable(false);
        setLocationRelativeTo(null);

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Titulo
        JLabel titleLabel = new JLabel("Aprendizagem de BNC");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Arquivo de dados
        mainPanel.add(createLabel("Arquivo de Dados:", 13, false));
        mainPanel.add(Box.createVerticalStrut(5));
        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        filePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        fileField = new JTextField();
        fileField.setFont(new Font("Default", Font.PLAIN, 10));
        fileField.setEditable(false);
        JButton selectButton = new JButton("Selecionar");
        selectButton.setFont(new Font("Default", Font.PLAIN, 10));
        selectButton.setFocusable(false);
        selectButton.addActionListener(e -> selectDataFile());

        filePanel.add(fileField, BorderLayout.CENTER);
        filePanel.add(selectButton, BorderLayout.EAST);
        mainPanel.add(filePanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Arquivo de saida
        mainPanel.add(createLabel("Guardar Rede Como:", 13, false));
        mainPanel.add(Box.createVerticalStrut(5));
        JPanel outputPanel = new JPanel(new BorderLayout(5, 0));
        outputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        outputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        outputField = new JTextField("rede_bayes.bn");
        outputField.setFont(new Font("Default", Font.PLAIN, 10));
        JButton browseButton = new JButton("Procurar");
        browseButton.setFont(new Font("Default", Font.PLAIN, 10));
        browseButton.setFocusable(false);
        browseButton.addActionListener(e -> selectOutputFile());

        outputPanel.add(outputField, BorderLayout.CENTER);
        outputPanel.add(browseButton, BorderLayout.EAST);
        mainPanel.add(outputPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Parametros
        mainPanel.add(createLabel("Parâmetros:", 13, false));
        mainPanel.add(Box.createVerticalStrut(5));
        JPanel paramsPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        paramsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        paramsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel maxParentsPanel = new JPanel(new BorderLayout(0, 3));
        maxParentsPanel.add(createLabel("Max. Pais:", 11, false), BorderLayout.NORTH);
        maxParentsField = new JTextField("2");
        maxParentsField.setFont(new Font("Default", Font.PLAIN, 10));
        maxParentsPanel.add(maxParentsField, BorderLayout.CENTER);

        JPanel numGraphsPanel = new JPanel(new BorderLayout(0, 3));
        numGraphsPanel.add(createLabel("No. Grafos:", 11, false), BorderLayout.NORTH);
        numGraphsField = new JTextField("100");
        numGraphsField.setFont(new Font("Default", Font.PLAIN, 10));
        numGraphsPanel.add(numGraphsField, BorderLayout.CENTER);

        JPanel pseudoCountPanel = new JPanel(new BorderLayout(0, 3));
        pseudoCountPanel.add(createLabel("Pseudo-Contagem S:", 11, false), BorderLayout.NORTH);
        pseudoCountField = new JTextField("0.5");
        pseudoCountField.setFont(new Font("Default", Font.PLAIN, 10));
        pseudoCountPanel.add(pseudoCountField, BorderLayout.CENTER);

        JPanel optimizePanel = new JPanel(new BorderLayout(0, 3));
        optimizePanel.add(createLabel(" ", 11, false), BorderLayout.NORTH);
        JPanel checkboxContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        checkboxContainer.setOpaque(false);
        optimizeSCheckbox = new JCheckBox();
        optimizeSCheckbox.setMargin(new java.awt.Insets(1, 2, 0, 0));
        optimizeSCheckbox.setFocusable(false);
        optimizeSCheckbox.addActionListener(e -> {
            pseudoCountField.setEnabled(!optimizeSCheckbox.isSelected());
        });

        JLabel textLabel = new JLabel("Otimizar S");
        textLabel.setFont(new Font("Default", Font.PLAIN, 11));
        textLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 2, 0));
        textLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                optimizeSCheckbox.doClick();
            }
        });

        checkboxContainer.add(optimizeSCheckbox);
        checkboxContainer.add(textLabel);

        optimizePanel.add(checkboxContainer, BorderLayout.CENTER);

        paramsPanel.add(maxParentsPanel);
        paramsPanel.add(numGraphsPanel);
        paramsPanel.add(pseudoCountPanel);
        paramsPanel.add(optimizePanel);
        mainPanel.add(paramsPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Log
        mainPanel.add(createLabel("Registro:", 13, false));
        mainPanel.add(Box.createVerticalStrut(5));
        logArea = new JTextArea(8, 40);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(scrollPane);
        mainPanel.add(Box.createVerticalStrut(10));

        // Progresso
        statusLabel = createLabel("Pronto", 13, false);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalStrut(3));

        progressBar = new JProgressBar(0, 100);
        progressBar.setFont(new Font("Default", Font.PLAIN, 10));
        progressBar.setStringPainted(true);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        mainPanel.add(progressBar);
        mainPanel.add(Box.createVerticalStrut(15));

        // Botoes
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JButton backButton = new JButton("Voltar");
        backButton.setFocusable(false);
        backButton.setFont(new Font("Default", Font.PLAIN, 10));
        backButton.addActionListener(e -> {
            dispose();
            MainApp.main(new String[] {});
        });

        stopButton = new JButton("■ Parar");
        stopButton.setFocusable(false);
        stopButton.setFont(new Font("Default", Font.PLAIN, 10));
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopLearning());

        startButton = new JButton("▶ Iniciar");
        startButton.setFocusable(false);
        startButton.setFont(new Font("Default", Font.PLAIN, 10));
        startButton.addActionListener(e -> startLearning());

        JButton clearButton = new JButton("Limpar");
        clearButton.setFocusable(false);
        clearButton.setFont(new Font("Default", Font.PLAIN, 10));
        clearButton.addActionListener(e -> clearAll());

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightButtons.add(clearButton);
        rightButtons.add(stopButton);
        rightButtons.add(startButton);

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

    private void selectDataFile() {
        JFileChooser chooser = new JFileChooser();
        File dataSetsFolder = new File("DataSets");
        if (dataSetsFolder.exists()) {
            chooser.setCurrentDirectory(dataSetsFolder);
        }
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivos CSV", "csv"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFilePath = chooser.getSelectedFile().getAbsolutePath();
            fileField.setText(selectedFilePath);
            log("Arquivo selecionado: " + chooser.getSelectedFile().getName());

            String baseName = chooser.getSelectedFile().getName().replace(".csv", "");
            outputField.setText(baseName + "_network.bn");
        }
    }

    private void selectOutputFile() {
        JFileChooser chooser = new JFileChooser();
        File trainedBNFolder = new File("TrainedBN");
        if (trainedBNFolder.exists()) {
            chooser.setCurrentDirectory(trainedBNFolder);
        }
        chooser.setFileFilter(new FileNameExtensionFilter("Rede de Bayes", "bn"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(".bn")) {
                path += ".bn";
            }
            outputField.setText(path);
        }
    }

    private void startLearning() {
        if (selectedFilePath == null || selectedFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um arquivo de dados primeiro.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        stopRequested = false;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        progressBar.setValue(0);

        currentWorker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    int maxParents = Integer.parseInt(maxParentsField.getText().trim());
                    int numGraphs = Integer.parseInt(numGraphsField.getText().trim());

                    if (maxParents < 0 || maxParents > 2)
                        throw new IllegalArgumentException("Número máximo de pais inválido.");
                    if (numGraphs < 1)
                        throw new IllegalArgumentException("Número de grafos deve ser >= 1.");

                    publish("Carregando amostra...");
                    setProgress(5);

                    amostra = ReadCSV.read(selectedFilePath);

                    publish("Amostra: " + amostra.length() + " instâncias, " + amostra.dim() + " variáveis");
                    setProgress(10);

                    long totalStartTime = System.currentTimeMillis();

                    ghc = new GreedyHillClimber(amostra, maxParents, numGraphs);
                    long[] lastUpdate = new long[1];

                    ghc.setListener((iteration, totalIterations, message) -> {
                        long now = System.currentTimeMillis();
                        if (message != null || iteration == totalIterations || now - lastUpdate[0] > 100) {
                            lastUpdate[0] = now;

                            int percent = (int) ((iteration / (double) totalIterations) * 100);
                            int adjustedProgress = 15 + (int) (percent * 0.75);
                            setProgress(adjustedProgress);

                            String status = String.format("Iteração %d/%d | Tempo: %ds", iteration, totalIterations,
                                    (now - totalStartTime) / 1000);
                            publish("STATUS:" + status);

                            if (message != null) {
                                publish(message);
                            }
                        }
                    });

                    publish("Treinando...");
                    ghc.learn();

                    setProgress(90);

                    publish("Guardando rede...");
                    String outputPath = outputField.getText();
                    if (!outputPath.contains(File.separator) && !outputPath.contains("/")) {
                        File trainedBNFolder = new File("TrainedBN");
                        if (!trainedBNFolder.exists()) {
                            trainedBNFolder.mkdirs();
                        }
                        outputPath = trainedBNFolder.getAbsolutePath() + File.separator + outputPath;
                    }

                    double initialS = 0.5;
                    if (!optimizeSCheckbox.isSelected()) {
                        try {
                            initialS = Double.parseDouble(pseudoCountField.getText().trim());
                            if (initialS < 0)
                                throw new IllegalArgumentException("S deve ser >= 0.");
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Valor de S inválido.");
                        }
                    }

                    BN bn = new BN(amostra, ghc.bestGraph, initialS);

                    if (optimizeSCheckbox.isSelected()) {
                        publish("Encontrando melhor Pseudo-Contagem (S)...");

                        bn.setListener((current, total, message) -> {
                            int percent = (int) ((current / (double) total) * 100);
                            int adjustedProgress = 90 + (int) (percent * 0.10);
                            setProgress(Math.min(adjustedProgress, 99));
                            if (message != null) {
                                publish("STATUS:" + message);
                            }
                        });

                        bn.optimizeS(amostra);
                        publish("Melhor S: " + String.format("%.2f", bn.S));
                    } else {
                        publish("Pseudo-Contagem (S) fixa: " + initialS);
                    }

                    bn.save(outputPath);

                    publish("Rede guardada: " + outputPath);
                    setProgress(100);

                    long totalTime = System.currentTimeMillis() - totalStartTime;

                    publish("----------------------------- Concluído -----------------------------");
                    publish("Tempo Total: " + (totalTime / 1000) + "s");
                    publish("Melhor Grafo: " + ghc.bestGraph);
                    publish("MDL: " + String.format("%.4f", ghc.bestMDL));

                } catch (Exception e) {
                    publish("ERRO: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    if (message.startsWith("STATUS:")) {
                        statusLabel.setText(message.substring(7));
                    } else {
                        log(message);
                    }
                }
            }

            @Override
            protected void done() {
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                if (stopRequested) {
                    statusLabel.setText("Interrompido");
                } else {
                    statusLabel.setText("Concluído");
                }
            }
        };

        currentWorker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        currentWorker.execute();
    }

    private void stopLearning() {
        stopRequested = true;
        if (currentWorker != null) {
            currentWorker.cancel(true);
        }
        if (ghc != null) {
            ghc.interrupt();
        }
        log("Treino interrompido.");
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void clearAll() {
        logArea.setText("");
        fileField.setText("");
        outputField.setText("rede_bayes.bn");
        progressBar.setValue(0);
        statusLabel.setText("Pronto");
        selectedFilePath = null;
        amostra = null;
    }
}
