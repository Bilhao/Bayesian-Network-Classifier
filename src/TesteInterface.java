import javax.swing.JFrame;
import javax.swing.JTextArea;

import java.awt.EventQueue;

public class TesteInterface {
    private JFrame frame;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    TesteInterface window = new TesteInterface();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public TesteInterface() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        
        JTextArea textArea = new JTextArea();
        textArea.setBounds(150, 80, 150, 100);
        frame.getContentPane().add(textArea);

        
    }
}
