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
        borderLabel.setBounds(0, 0, 800, 600); // adjust if border size is different
        bgPanel.add(borderLabel);

        // ============================
        // PROCEED BUTTON
        // ============================
        JButton proceedButton = new JButton("Proceed");
        proceedButton.setBounds(500, 500, 200, 50);
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
 

        setVisible(true);
    }
}