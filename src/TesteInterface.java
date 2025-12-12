import java.awt.Color;

import javax.swing.*;

public class TesteInterface {

    public static void main(String[] args) {
        MyFrame mf = new MyFrame();
        Label lb = new Label();
        mf.add(lb);

    }
}

class MyFrame extends JFrame {
    MyFrame() {
        this.setTitle("My Frame"); // Cria uma nova janela com o título "My Frame"
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Define a operação padrão de fechar a janela
        // this.setResizable(false); // Define a janela como não redimensionável
        this.setSize(500, 500);
        this.setVisible(true);
        // this.getContentPane().setBackground(Color.darkGray); // Define a cor de fundo
        // da janela como azul
    }
}

class Label extends JLabel {
    Label() {
        this.setText("Hello World"); // Define o texto do rótulo
        this.setForeground(Color.black); // Define a cor do texto como preta
        this.setHorizontalTextPosition(JLabel.CENTER); // Define a posição horizontal do texto em relação ao ícone
        this.setVerticalTextPosition(JLabel.TOP); // Define a posição vertical do texto em relação ao ícone
        this.setVerticalAlignment(JLabel.CENTER); // Define o alinhamento vertical do rótulo
        this.setHorizontalAlignment(JLabel.CENTER); // Define o alinhamento horizontal do rótulo
        // this.setBounds(100, 100, 250, 250); // Define a posição e o tamanho do rótulo
    }
}