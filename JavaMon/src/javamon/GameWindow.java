package javamon;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.*;

public class GameWindow extends JFrame {

    // Background Panel
    JPanel bgPanel = new JPanel() {
        private Image bg = new ImageIcon(
                getClass().getResource("/javamon/assets/GameBG.png")
        ).getImage();

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        }
    };

    public GameWindow() {
        setTitle("JavaMon");
        setSize(1280, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Move these inside the constructor
        bgPanel.setLayout(null);
        setContentPane(bgPanel);
        
        ImageIcon jmon1 = new ImageIcon(getClass().getResource("/javamon/assets/ally_apoyet.png"));
        JLabel jMon1 = new JLabel(jmon1);
        jMon1.setBounds(95, 239, 384, 345);
        // Add to panel
        bgPanel.add(jMon1);
        
        ImageIcon platformIcon = new ImageIcon(getClass().getResource("/javamon/assets/PLATFORM.png"));
        JLabel platform1 = new JLabel(platformIcon);
        platform1.setBounds(76, 416, 415, 106);
        // Add to panel
        bgPanel.add(platform1);
        
        ImageIcon jmon2 = new ImageIcon(getClass().getResource("/javamon/assets/enemy_lectric.png"));
        JLabel jMon2 = new JLabel(jmon2);
        jMon2.setBounds(749, 49, 384, 345);
        // Add to panel
        bgPanel.add(jMon2);
        
        ImageIcon PlatformIcon = new ImageIcon(getClass().getResource("/javamon/assets/PLATFORM.png"));
        JLabel platform2 = new JLabel(PlatformIcon);
        platform2.setBounds(734, 199, 415, 106);
        // Add to panel
        bgPanel.add(platform2);
        
        ImageIcon fightIcon = new ImageIcon(getClass().getResource("/javamon/assets/FIGHTBTN.png"));
        JButton fightButton = new JButton(fightIcon);
        fightButton.setBorderPainted(false);
        fightButton.setContentAreaFilled(false);
        fightButton.setFocusPainted(false);
        fightButton.setBounds(650, 600, fightIcon.getIconWidth(), fightIcon.getIconHeight());
        bgPanel.add(fightButton);
        
        ImageIcon jMonIcon = new ImageIcon(getClass().getResource("/javamon/assets/JAVAMONBTN.png"));
        JButton jMonButton = new JButton(jMonIcon);
        jMonButton.setBorderPainted(false);
        jMonButton.setContentAreaFilled(false);
        jMonButton.setFocusPainted(false);
        jMonButton.setBounds(920, 600, jMonIcon.getIconWidth(), jMonIcon.getIconHeight());
        bgPanel.add(jMonButton);
        
        ImageIcon helpIcon = new ImageIcon(getClass().getResource("/javamon/assets/HELPBTN.png"));
        JButton helpButton = new JButton(helpIcon);
        helpButton.setBorderPainted(false);
        helpButton.setContentAreaFilled(false);
        helpButton.setFocusPainted(false);
        helpButton.setBounds(780, 660, helpIcon.getIconWidth(), helpIcon.getIconHeight());
        bgPanel.add(helpButton);
        
        JLabel txtWhat = new JLabel("What will JavaMon do?", SwingConstants.LEFT);
        txtWhat.setBounds(170, 625, 800, 50);
        txtWhat.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 35));
        txtWhat.setForeground(java.awt.Color.BLACK);
        txtWhat.setOpaque(false);
        bgPanel.add(txtWhat);
        
        ImageIcon textboxIcon = new ImageIcon(getClass().getResource("/javamon/assets/TEXTBOX.png"));
        JLabel textBorder = new JLabel(textboxIcon);
        textBorder.setBounds(160, 605, textboxIcon.getIconWidth(), textboxIcon.getIconHeight());
        // Add to panel
        bgPanel.add(textBorder);
        
        ImageIcon lBoxIcon = new ImageIcon(getClass().getResource("/javamon/assets/LOWER_TEXTBOX.png"));
        JLabel txtbox = new JLabel(lBoxIcon);
        txtbox.setBounds(-10, 500, lBoxIcon.getIconWidth(), lBoxIcon.getIconHeight());
        bgPanel.add(txtbox);
        
        ImageIcon uBoxIcon1 = new ImageIcon(getClass().getResource("/javamon/assets/UPPER_TEXTBOX.png"));
        JLabel uBoxMon1 = new JLabel(uBoxIcon1);
        uBoxMon1.setBounds(0, 21, 366, 195);
        bgPanel.add(uBoxMon1);
        
        JLabel txtFloor = new JLabel("FLOOR 1", SwingConstants.LEFT);
        txtFloor.setBounds(25, 545, 300, 40);
        txtFloor.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 35));
        txtFloor.setForeground(java.awt.Color.BLACK);
        txtFloor.setOpaque(false);
        bgPanel.add(txtFloor);
        bgPanel.setComponentZOrder(txtFloor, 0);
        
        
        bgPanel.setComponentZOrder(txtWhat, 0);
        
        
        
        JLabel txtJName1 = new JLabel("JavaMon1", SwingConstants.LEFT);
        txtJName1.setBounds(40, 50, 300, 40);
        txtJName1.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 30));
        txtJName1.setForeground(java.awt.Color.BLACK);
        txtJName1.setOpaque(false);
        bgPanel.add(txtJName1);
        bgPanel.setComponentZOrder(txtJName1, 0);
        
        JLabel txtJL1 = new JLabel("L:82", SwingConstants.LEFT);
        txtJL1.setBounds(260, 50, 300, 40);
        txtJL1.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 30));
        txtJL1.setForeground(java.awt.Color.GRAY);
        txtJL1.setOpaque(false);
        bgPanel.add(txtJL1);
        bgPanel.setComponentZOrder(txtJL1, 0);
        
        JLabel txtJLives1 = new JLabel("300 / 300", SwingConstants.LEFT);
        txtJLives1.setBounds(165, 140, 300, 40);
        txtJLives1.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 30));
        txtJLives1.setForeground(java.awt.Color.GRAY);
        txtJLives1.setOpaque(false);
        bgPanel.add(txtJLives1);
        bgPanel.setComponentZOrder(txtJLives1, 0);
        
        JLabel txtJName2 = new JLabel("JavaMon2", SwingConstants.RIGHT);
        txtJName2.setBounds(790, 341, 300, 40);
        txtJName2.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 30));
        txtJName2.setForeground(java.awt.Color.BLACK);
        txtJName2.setOpaque(false);
        bgPanel.add(txtJName2);
        bgPanel.setComponentZOrder(txtJName2, 0);
        
        JLabel txtJL2 = new JLabel("L:82", SwingConstants.RIGHT);
        txtJL2.setBounds(930, 341, 300, 40);
        txtJL2.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 30));
        txtJL2.setForeground(java.awt.Color.GRAY);
        txtJL2.setOpaque(false);
        bgPanel.add(txtJL2);
        bgPanel.setComponentZOrder(txtJL2, 0);
        
        JLabel txtJLives2 = new JLabel("300 / 300", SwingConstants.RIGHT);
        txtJLives2.setBounds(930, 430, 300, 40);
        txtJLives2.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 30));
        txtJLives2.setForeground(java.awt.Color.GRAY);
        txtJLives2.setOpaque(false);
        bgPanel.add(txtJLives2);
        bgPanel.setComponentZOrder(txtJLives2, 0);
        
        ImageIcon uBoxIcon2 = new ImageIcon(getClass().getResource("/javamon/assets/UPPER_TEXTBOX.png"));
        JLabel uBoxMon2 = new JLabel(uBoxIcon2);
        uBoxMon2.setBounds(900, 314, 366, 195);
        bgPanel.add(uBoxMon2);
        
        

        setVisible(true);
    }
}

