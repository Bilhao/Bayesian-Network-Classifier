import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

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
    private JSpinner maxParentsSpinner;
    private JSpinner numGraphsSpinner;
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JButton startButton;
    private JLabel statusLabel;

    private Amostra amostra;
    private BayesianNetwork bestNetwork;
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
        mainPanel.add(createLabel("Parametros:", 13, false));
        mainPanel.add(Box.createVerticalStrut(5));
        JPanel paramsPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        paramsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        paramsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel maxParentsPanel = new JPanel(new BorderLayout(0, 3));
        maxParentsPanel.add(createLabel("Max. Pais:", 11, false), BorderLayout.NORTH);
        maxParentsSpinner = new JSpinner(new SpinnerNumberModel(2, 0, 2, 1));
        maxParentsSpinner.setFont(new Font("Default", Font.PLAIN, 10));
        maxParentsPanel.add(maxParentsSpinner, BorderLayout.CENTER);

        JPanel numGraphsPanel = new JPanel(new BorderLayout(0, 3));
        numGraphsPanel.add(createLabel("Grafos iniciais:", 11, false), BorderLayout.NORTH);
        numGraphsSpinner = new JSpinner(new SpinnerNumberModel(1000, 1, null, 1));
        numGraphsSpinner.setFont(new Font("Default", Font.PLAIN, 10));
        numGraphsPanel.add(numGraphsSpinner, BorderLayout.CENTER);

        paramsPanel.add(maxParentsPanel);
        paramsPanel.add(numGraphsPanel);
        paramsPanel.add(new JLabel());
        paramsPanel.add(new JLabel());
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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        startButton = new JButton("Iniciar");
        startButton.setFocusable(false);
        startButton.setFont(new Font("Default", Font.PLAIN, 10));
        startButton.addActionListener(e -> startLearning());

        JButton clearButton = new JButton("Limpar");
        clearButton.setFocusable(false);
        clearButton.setFont(new Font("Default", Font.PLAIN, 10));
        clearButton.addActionListener(e -> clearAll());

        buttonPanel.add(clearButton);
        buttonPanel.add(startButton);
        mainPanel.add(buttonPanel);

        add(mainPanel);
    }

    private JLabel createLabel(String text, int size, boolean needBorder) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, size));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (needBorder)
            label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        return label;
    }

    private void selectDataFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("../DataSets"));
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
            JOptionPane.showMessageDialog(this, "Por favor, selecione um arquivo de dados primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        startButton.setEnabled(false);
        progressBar.setValue(0);

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    int maxParents = (Integer) maxParentsSpinner.getValue();
                    int numGraphs = (Integer) numGraphsSpinner.getValue();

                    publish("Carregando amostra...");
                    setProgress(5);
                    amostra = ReadCSV.read(selectedFilePath);
                    publish("Amostra: " + amostra.length() + " instancias, " + amostra.element(0).length + " variaveis");
                    setProgress(10);

                    int n = amostra.element(0).length;

                    publish("Iniciando com grafo vazio...");
                    bestNetwork = new BayesianNetwork(n);

                    int[] domains = new int[n];
                    for (int i = 0; i < n; i++) {
                        domains[i] = amostra.domain(i);
                    }
                    bestNetwork.setDomains(domains);

                    String[] names = new String[n];
                    for (int i = 0; i < n - 1; i++) {
                        names[i] = "X" + i;
                    }
                    names[n - 1] = "Classe";
                    bestNetwork.setVariableNames(names);

                    double bestMDL = bestNetwork.calculateMDL(amostra);
                    publish("MDL inicial: " + String.format("%.4f", bestMDL));
                    setProgress(15);

                    int numThreads = Runtime.getRuntime().availableProcessors();
                    publish("Usando " + numThreads + " threads...");
                    ExecutorService executor = Executors.newFixedThreadPool(numThreads);

                    AtomicReference<Double> atomicBestMDL = new AtomicReference<>(bestMDL);
                    AtomicReference<BayesianNetwork> atomicBestNetwork = new AtomicReference<>(bestNetwork);
                    AtomicInteger completed = new AtomicInteger(0);

                    final int[] finalDomains = domains;
                    final String[] finalNames = names;
                    final int finalN = n;
                    final int finalMaxParents = maxParents;
                    final int finalNumGraphs = numGraphs;

                    java.util.List<Future<?>> futures = new ArrayList<>();

                    for (int g = 0; g < numGraphs; g++) {
                        final int graphIndex = g;
                        Future<?> future = executor.submit(() -> {
                            try {
                                Random localRandom = new Random();
                                BayesianNetwork randomNet = createRandomNetwork(finalN, finalMaxParents, localRandom);
                                randomNet.setDomains(finalDomains);
                                randomNet.setVariableNames(finalNames);

                                BayesianNetwork optimized = greedyOptimize(randomNet, finalMaxParents);
                                double mdl = optimized.getMdlScore();

                                synchronized (atomicBestMDL) {
                                    if (mdl > atomicBestMDL.get()) {
                                        atomicBestMDL.set(mdl);
                                        atomicBestNetwork.set(optimized);
                                    }
                                }

                                int done = completed.incrementAndGet();
                                int progressPercent = 15 + (int) (done * 70.0 / finalNumGraphs);
                                setProgress(progressPercent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        futures.add(future);
                    }

                    // Aguardar todas as threads
                    for (Future<?> f : futures) {
                        f.get();
                    }
                    executor.shutdown();

                    bestMDL = atomicBestMDL.get();
                    bestNetwork = atomicBestNetwork.get();
                    publish("Melhor MDL dos grafos aleatorios: " + String.format("%.4f", bestMDL));

                    publish("Otimizando grafo vazio...");
                    BayesianNetwork emptyNet = new BayesianNetwork(n);
                    emptyNet.setDomains(domains);
                    emptyNet.setVariableNames(names);
                    BayesianNetwork optimizedEmpty = greedyOptimize(emptyNet, maxParents);

                    if (optimizedEmpty.getMdlScore() > bestMDL) {
                        bestMDL = optimizedEmpty.getMdlScore();
                        bestNetwork = optimizedEmpty;
                    }

                    setProgress(90);

                    publish("Guardando rede...");
                    String outputPath = outputField.getText();
                    if (!outputPath.contains(File.separator) && !outputPath.contains("/")) {
                        outputPath = new File(selectedFilePath).getParent() + File.separator + outputPath;
                    }
                    bestNetwork.save(outputPath);

                    String samplePath = outputPath.replace(".bn", "_sample.dat");
                    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(samplePath))) {
                        out.writeObject(amostra);
                    }

                    publish("Rede guardada: " + outputPath);
                    setProgress(100);

                    publish("--- Concluido ---");
                    publish("MDL: " + String.format("%.4f", bestMDL) + " | Arestas: " + bestNetwork.getEdgeCount());

                } catch (Exception e) {
                    publish("ERRO: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    log(message);
                }
            }

            @Override
            protected void done() {
                startButton.setEnabled(true);
                statusLabel.setText("Concluido");
            }
        };

        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
                statusLabel.setText("Processando... " + evt.getNewValue() + "%");
            }
        });

        worker.execute();
    }

    private BayesianNetwork createRandomNetwork(int n, int maxParents, Random random) {
        BayesianNetwork net = new BayesianNetwork(n);

        for (int i = 0; i < n - 1; i++) {
            int numParents = random.nextInt(Math.min(maxParents + 1, i + 1));

            ArrayList<Integer> possibleParents = new ArrayList<>();
            for (int j = 0; j < n - 1; j++) {
                if (j != i)
                    possibleParents.add(j);
            }

            Collections.shuffle(possibleParents, random);

            for (int p = 0; p < numParents && p < possibleParents.size(); p++) {
                int parent = possibleParents.get(p);
                if (!net.wouldCreateCycle(parent, i)) {
                    net.addEdge(parent, i);
                }
            }
        }

        return net;
    }

    private BayesianNetwork greedyOptimize(BayesianNetwork network, int maxParents) {
        BayesianNetwork current = network.copy();
        double currentMDL = current.calculateMDL(amostra);
        boolean improved = true;
        int n = current.getN();

        while (improved) {
            improved = false;
            double bestDeltaMDL = 0;
            int bestOp = -1;
            int bestFrom = -1;
            int bestTo = -1;

            for (int i = 0; i < n - 1; i++) {
                for (int j = 0; j < n - 1; j++) {
                    if (i == j)
                        continue;

                    if (current.hasEdge(i, j)) {
                        current.removeEdge(i, j);
                        double newMDL = current.calculateMDL(amostra);
                        double delta = newMDL - currentMDL;

                        if (delta > bestDeltaMDL) {
                            bestDeltaMDL = delta;
                            bestOp = 0;
                            bestFrom = i;
                            bestTo = j;
                        }
                        current.addEdge(i, j);
                    } else {
                        if (current.getParentCount(j) < maxParents && !current.wouldCreateCycle(i, j)) {
                            current.addEdge(i, j);
                            double newMDL = current.calculateMDL(amostra);
                            double delta = newMDL - currentMDL;

                            if (delta > bestDeltaMDL) {
                                bestDeltaMDL = delta;
                                bestOp = 1;
                                bestFrom = i;
                                bestTo = j;
                            }
                            current.removeEdge(i, j);
                        }
                    }
                }
            }

            if (bestDeltaMDL > 0) {
                if (bestOp == 0) {
                    current.removeEdge(bestFrom, bestTo);
                } else {
                    current.addEdge(bestFrom, bestTo);
                }
                currentMDL += bestDeltaMDL;
                current.setMdlScore(currentMDL);
                improved = true;
            }
        }

        return current;
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
        bestNetwork = null;
    }
}
