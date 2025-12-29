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
