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
        setLayout(null); // manual placement

        // HEADER IMAGE
        ImageIcon titleIcon = new ImageIcon(getClass().getResource("/javamon/assets/title.png"));
        JLabel titleLabel = new JLabel(titleIcon);
        titleLabel.setBounds(0, 0, titleIcon.getIconWidth(), titleIcon.getIconHeight());
        add(titleLabel);

        setVisible(true);
        
     // PROCEED BUTTON (placeholder)
        JButton proceedButton = new JButton("Proceed");
        proceedButton.setBounds(500, 500, 200, 50); // x, y, width, height
        proceedButton.setFont(new Font("Arial", Font.BOLD, 20));
        proceedButton.setBackground(Color.BLUE);
        proceedButton.setForeground(Color.WHITE);
        proceedButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Optional: action when clicked
        proceedButton.addActionListener(e -> {
            // For now, just print to console
            System.out.println("Proceed button clicked!");
            
            DraftSelection draft = new DraftSelection();
            draft.setVisible(true);
            this.dispose();
        });

        // Add to frame
        add(proceedButton);

        
        
    }
}
