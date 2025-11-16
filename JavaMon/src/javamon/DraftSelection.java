package javamon;

import javax.swing.*;
import java.awt.*;

public class DraftSelection extends JFrame {

    public DraftSelection() {
        setTitle("Draft Selection");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(null); // optional if you want manual placement

        // Example placeholder label
        JLabel label = new JLabel("This is the Draft Selection screen");
        label.setBounds(200, 250, 400, 50);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        add(label);

        setVisible(true);
    }
}
