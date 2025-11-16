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
    }
}
