import javax.swing.*;
import java.awt.*;

/**
 * Lancador principal para as aplicacoes de BNC.
 */
public class MainApp {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new LauncherFrame());
    }
}

class LauncherFrame extends JFrame {    
    public LauncherFrame() {
        setTitle("Projeto AMC - Redes de Bayes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 200);
        setResizable(false);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        
        JLabel titleLabel = new JLabel("Redes de Bayes");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(25));
        
        JButton trainingButton = new JButton("Aprendizagem de Rede");
        trainingButton.setFont(new Font("Default", Font.PLAIN, 11));
        trainingButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        trainingButton.setMaximumSize(new Dimension(250, 32));
        trainingButton.setFocusable(false);
        trainingButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new TrainingFrame());
        });
        
        JButton classificationButton = new JButton("Classificação de Pacientes");
        classificationButton.setFont(new Font("Default", Font.PLAIN, 11));
        classificationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        classificationButton.setMaximumSize(new Dimension(250, 32));
        trainingButton.setForeground(Color.BLACK);
        classificationButton.setFocusable(false);
        classificationButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new ClassificationFrame());
        });
        
        mainPanel.add(trainingButton);
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(classificationButton);
        
        add(mainPanel);
        setVisible(true);
    }
}
