package javamon;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GameOver extends JFrame {

    public GameOver(int floorsCleared, Runnable onMainMenuCallback) {
        setTitle("Game Over");
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLayeredPane lp = new JLayeredPane();
        lp.setPreferredSize(new Dimension(800, 600));
        setContentPane(lp);

        // --- Background ---
        ImageIcon bgIcon = loadIcon("/javamon/assets/gameover_bg.png");
        JLabel bg = new JLabel(bgIcon);
        bg.setBounds(0, 0, 800, 600);
        lp.add(bg, Integer.valueOf(0));

        // --- Main Menu Button ---
        ImageIcon mainIcon = loadIcon("/javamon/assets/btn_main.png");
        JButton btnMain = new JButton(mainIcon);
        btnMain.setBounds(150, 430, mainIcon.getIconWidth(), mainIcon.getIconHeight());
        style(btnMain);
        lp.add(btnMain, Integer.valueOf(1));

        btnMain.addActionListener(e -> {
            dispose(); // close GameOver window
            if (onMainMenuCallback != null) onMainMenuCallback.run();
        });

        // --- Exit Button ---
        ImageIcon exitIcon = loadIcon("/javamon/assets/btn_exit.png");
        JButton btnExit = new JButton(exitIcon);
        btnExit.setBounds(450, 430, exitIcon.getIconWidth(), exitIcon.getIconHeight());
        style(btnExit);
        lp.add(btnExit, Integer.valueOf(1));

        btnExit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit?",
                "Exit Game",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        pack();
    }

    private void style(JButton b) {
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private ImageIcon loadIcon(String path) {
        return new ImageIcon(getClass().getResource(path));
    }
}
	