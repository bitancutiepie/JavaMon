package javamon;

import javax.swing.*;
import java.awt.*;

public class TrainerSelection extends JFrame {

    public TrainerSelection() {
        setTitle("Trainer Selection");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // ============================
        // BACKGROUND PANEL
        // ============================
        JPanel bgPanel = new JPanel() {
            private Image bg = new ImageIcon(
                    getClass().getResource("/javamon/assets/trainerBG.png")
            ).getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        bgPanel.setLayout(null);
        setContentPane(bgPanel);

        // ============================
        // TITLE IMAGE
        // ============================
        ImageIcon titleIcon = new ImageIcon(
                getClass().getResource("/javamon/assets/title.png")
        );
        JLabel titleLabel = new JLabel(titleIcon);
        titleLabel.setBounds(0, 0, titleIcon.getIconWidth(), titleIcon.getIconHeight());
        bgPanel.add(titleLabel);

        // ============================
        // BORDER IMAGE (TOP LAYER)
        // ============================
        ImageIcon borderIcon = new ImageIcon(
                getClass().getResource("/javamon/assets/trBorder.png")
        );
        JLabel borderLabel = new JLabel(borderIcon);
        borderLabel.setBounds(0, 0, 784, 600); // adjust if border size is different
        bgPanel.add(borderLabel);

        // ============================
        // PROCEED BUTTON
        // ============================
        JButton proceedButton = new JButton("Proceed");
        proceedButton.setBounds(700, 500, 100, 50);
        proceedButton.setFont(new Font("Arial", Font.BOLD, 20));
        proceedButton.setBackground(Color.BLUE);
        proceedButton.setForeground(Color.WHITE);
        proceedButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        proceedButton.addActionListener(e -> {
            System.out.println("Proceed button clicked!");

            DraftSelection draft = new DraftSelection();
            draft.setVisible(true);
            this.dispose();
        });

        bgPanel.add(proceedButton);

        bgPanel.setComponentZOrder(borderLabel, 0);
        bgPanel.setComponentZOrder(proceedButton, 1);
        bgPanel.setComponentZOrder(titleLabel, 2);

        // ============================
        // TRAINER CARDS
        // ============================
        String[] trainers = {"ELEMENTALIST", "MYSTIC", "AGGRESSOR", "STRATEGIST", "BEASTMASTER"};
        int[][] positions = {
            {30, 77, 197, 324},   // elementalist x, y, width, height
            {435, 260, 200, 337},  // mystic
            {538, 77, 220, 345},  // aggressor
            {283, 77, 223, 336},  // strategist
            {186, 260, 168, 337}   // beastmaster
        };

        for (int i = 0; i < trainers.length; i++) {
            String trainer = trainers[i];
            int x = positions[i][0];
            int y = positions[i][1];
            int w = positions[i][2];
            int h = positions[i][3];

            ImageIcon icon = new ImageIcon(getClass().getResource("/javamon/assets/" + trainer + ".png"));
            JButton trainerButton = new JButton(icon);
            trainerButton.setBounds(x, y, w, h);
            trainerButton.setBorderPainted(false);
            trainerButton.setContentAreaFilled(false);
            trainerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Action when trainer is selected
            trainerButton.addActionListener(e -> {
                System.out.println(trainer + " selected!");
                // Store selection if needed
            });

            bgPanel.add(trainerButton);
            bgPanel.setComponentZOrder(trainerButton, 1); // above border
        }

        setVisible(true);
    }
}