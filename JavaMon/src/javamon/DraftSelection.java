package javamon;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.io.File;

public class DraftSelection extends JFrame {

    private static final String LOCAL_DIR = "/mnt/data/"; 

    public DraftSelection() {
        setTitle("Draft Selection");
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // set icon (try classpath then local fallback)
        Image icon = loadImagePreferResource("/javamon/assets/icon.png", LOCAL_DIR + "icon.png");
        if (icon != null) setIconImage(icon);

        // show early (matching MainMenu style)
        setVisible(true);

        // Background panel (scaled exactly to 1280x760)
        JPanel bgPanel = new JPanel() {
            private final Image bg = loadImagePreferResource("/javamon/assets/DRAFTING PHASE BG.png",
                                                            LOCAL_DIR + "DRAFTING PHASE BG.png");

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bg != null) {
                    g.drawImage(bg, 0, 0, 1280, 760, this);
                } else {
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        bgPanel.setLayout(null);
        setContentPane(bgPanel);

        // -------------------------
        // Top Bar (1319 × 56 at x=-40 y=-6)
        // -------------------------
        ImageIcon topBarIcon = tryCreateIcon("/javamon/assets/Top Bar.png", LOCAL_DIR + "Top Bar.png");
        JLabel topBarLabel = iconLabelFor(topBarIcon, 1319, 56, "Top Bar");
        topBarLabel.setBounds(-40, -6, 1319, 56);
        bgPanel.add(topBarLabel);

        // -------------------------
        // DraftingPhase title (371 × 44 at x=420 y=50)
        // -------------------------
        ImageIcon draftingTitleIcon = tryCreateIcon("/javamon/assets/DraftingPhase.png", LOCAL_DIR + "DraftingPhase.png");
        JLabel draftingTitle = iconLabelFor(draftingTitleIcon, 371, 44, "DraftingPhase");
        draftingTitle.setBounds(420, 50, 371, 44);
        bgPanel.add(draftingTitle);

        // -------------------------
        // Center Box (498 × 498 at x=353 y=142)
        // -------------------------
        ImageIcon centerIcon = tryCreateIcon("/javamon/assets/CenterBox.png", LOCAL_DIR + "CenterBox.png");
        JLabel centerBox = iconLabelFor(centerIcon, 498, 498, "CenterBox");
        centerBox.setBounds(353, 142, 498, 498);
        bgPanel.add(centerBox);

        // -------------------------
        // Player Blue (3 copies)
        // -------------------------
        ImageIcon pBlueIcon = tryCreateIcon("/javamon/assets/PlayerBlue.png", LOCAL_DIR + "PlayerBlue.png");
        JLabel pBlue1 = iconLabelFor(pBlueIcon, 282, 112, "PlayerBlue");
        pBlue1.setBounds(53, 186, 282, 112);
        bgPanel.add(pBlue1);

        JLabel pBlue2 = iconLabelFor(pBlueIcon, 282, 112, "PlayerBlue");
        pBlue2.setBounds(53, 316, 282, 112);
        bgPanel.add(pBlue2);

        JLabel pBlue3 = iconLabelFor(pBlueIcon, 282, 112, "PlayerBlue");
        pBlue3.setBounds(53, 446, 282, 112);
        bgPanel.add(pBlue3);

        // -------------------------
        // Player Red (3 copies)
        // -------------------------
        ImageIcon pRedIcon = tryCreateIcon("/javamon/assets/PlayerRed.png", LOCAL_DIR + "PlayerRed.png");
        JLabel pRed1 = iconLabelFor(pRedIcon, 282, 112, "PlayerRed");
        pRed1.setBounds(863, 186, 282, 112);
        bgPanel.add(pRed1);

        JLabel pRed2 = iconLabelFor(pRedIcon, 282, 112, "PlayerRed");
        pRed2.setBounds(863, 316, 282, 112);
        bgPanel.add(pRed2);

        JLabel pRed3 = iconLabelFor(pRedIcon, 282, 112, "PlayerRed");
        pRed3.setBounds(863, 446, 282, 112);
        bgPanel.add(pRed3);

        // -------------------------
        // Choose Button (208 × 71 at x=400 y=640)
        // -------------------------
        ImageIcon chooseIcon = tryCreateIcon("/javamon/assets/ChooseButton.png", LOCAL_DIR + "ChooseButton.png");
        JButton chooseBtn = iconButtonFor(chooseIcon, 208, 71, "CHOOSE");
        chooseBtn.setBounds(400, 640, 208, 71);
        chooseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chooseBtn.addActionListener(e -> System.out.println("CHOOSE pressed"));
        bgPanel.add(chooseBtn);

        // -------------------------
        // Battle Button (208 × 71 at x=634 y=640)
        // -------------------------
        ImageIcon battleIcon = tryCreateIcon("/javamon/assets/BattleButton.png", LOCAL_DIR + "BattleButton.png");
        JButton battleBtn = iconButtonFor(battleIcon, 208, 71, "BATTLE");
        battleBtn.setBounds(634, 640, 208, 71);
        battleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // OPEN GameWindow and close DraftSelection
        battleBtn.addActionListener(e -> {
            GameWindow gw = new GameWindow();
            gw.setVisible(true);
            DraftSelection.this.dispose();
        });
        bgPanel.add(battleBtn);

        // -------------------------
        // Back Button (text)
        // -------------------------
        JButton backButton = new JButton("Back");
        backButton.setBounds(20, 20, 120, 40);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.setOpaque(false);
        backButton.addActionListener(e -> dispose());
        bgPanel.add(backButton);
    }

    private JLabel iconLabelFor(ImageIcon icon, int w, int h, String fallbackText) {
        if (icon != null) {
            Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            JLabel lbl = new JLabel(new ImageIcon(scaled));
            lbl.setOpaque(false);
            return lbl;
        } else {
            JLabel lbl = new JLabel(fallbackText);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setForeground(Color.WHITE);
            lbl.setBackground(new Color(0,0,0,140));
            lbl.setOpaque(true);
            lbl.setPreferredSize(new Dimension(w, h));
            return lbl;
        }
    }

    private JButton iconButtonFor(ImageIcon icon, int w, int h, String fallbackText) {
        JButton btn;
        if (icon != null) {
            Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            btn = new JButton(new ImageIcon(scaled));
        } else {
            btn = new JButton(fallbackText);
        }
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        return btn;
    }

    private Image loadImagePreferResource(String resourcePath, String localPath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url != null) return new ImageIcon(url).getImage();
        } catch (Exception ignored) {}

        try {
            File f = new File(localPath);
            if (f.exists()) return new ImageIcon(f.getAbsolutePath()).getImage();
        } catch (Exception ignored) {}

        System.err.println("Image not found: " + resourcePath + " | " + localPath);
        return null;
    }

    private ImageIcon tryCreateIcon(String resourcePath, String localPath) {
        Image im = loadImagePreferResource(resourcePath, localPath);
        return im != null ? new ImageIcon(im) : null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DraftSelection().setVisible(true));
    }
}
