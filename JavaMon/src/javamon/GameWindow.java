package javamon;

import javax.swing.*;

public class GameWindow extends JFrame {

    public GameWindow() {
        setTitle("JavaMon");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }
}
