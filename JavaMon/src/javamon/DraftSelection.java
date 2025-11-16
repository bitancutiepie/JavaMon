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
        setLayout(null); 

        // Example placeholder label
        JLabel label = new JLabel("This is the Draft Selection screen");
        label.setBounds(200, 250, 400, 50);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        add(label);

        // CENTER BOX BUTTON
        ImageIcon startIcon = new ImageIcon(getClass().getResource("/javamon/assets/CENTER BOX.png"));
        Image scaledStartImg = startIcon.getImage().getScaledInstance(350, 350, Image.SCALE_SMOOTH);
        ImageIcon scaledStartIcon = new ImageIcon(scaledStartImg);

        JButton startButton = new JButton(scaledStartIcon);
        startButton.setBounds(223, 130, 350, 350); 
        startButton.setBorderPainted(false);
        startButton.setContentAreaFilled(false);
        startButton.setFocusPainted(false);
        startButton.setOpaque(false);
        add(startButton);  // ← FIXED

        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        startButton.addActionListener(e -> {
            TrainerSelection game = new TrainerSelection();
            game.setVisible(true);
            this.dispose();
        });

        setVisible(true);
    }
}
