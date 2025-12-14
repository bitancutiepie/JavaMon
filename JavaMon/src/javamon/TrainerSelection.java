package javamon;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.prefs.Preferences;
import javax.sound.sampled.*;
import java.io.IOException;

public class TrainerSelection extends JFrame {

    private String selectedTrainerClass = "ELEMENTALIST"; 
    private JLabel floatingPreview; 
    private JDialog confirmationDialog;
    private final SoundManager soundManager = SoundManager.getInstance();

    private static final Color DARK_BG = new Color(20, 20, 20);
    private static final Color NEON_GOLD = new Color(255, 215, 0);
    private static final Color NEON_CYAN = new Color(0, 255, 255);
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 24);
    private static final Font DESC_FONT = new Font("Arial", Font.PLAIN, 16);
    private static final Font SUGGEST_FONT = new Font("Arial", Font.ITALIC, 14);

    public TrainerSelection() {
        setTitle("Trainer Selection");
        setSize(1280, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
     // Set window icon
        Image iconlogo = AssetLoader.loadImage("/javamon/assets/icon.png", "icon.png");
        if (iconlogo != null) setIconImage(iconlogo);
        
        // Music: Ensure menu music is playing
        soundManager.playMenuMusic();

        confirmationDialog = new JDialog(this, "Confirm Class", Dialog.ModalityType.APPLICATION_MODAL);
        confirmationDialog.setSize(500, 480);
        confirmationDialog.setLocationRelativeTo(this);
        confirmationDialog.setUndecorated(true);

        JPanel bgPanel = new JPanel() {
            private Image bg = AssetLoader.loadImage("/javamon/assets/trainerBG.png", "trainerBG.png");
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if(bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        bgPanel.setLayout(null);
        setContentPane(bgPanel);

        // Border
        ImageIcon borderIcon = AssetLoader.loadIcon("/javamon/assets/UPPERBORDERRR.png", "UPPERBORDERRR.png");
        JLabel borderLabel = new JLabel(borderIcon);
        borderLabel.setBounds(0, -30, 1280, 200);
        bgPanel.add(borderLabel);

        // Title Label
        ImageIcon selectIcon = AssetLoader.loadIcon("/javamon/assets/SELECTED TRAINER CLASS BUTT.png", "SELECTED TRAINER CLASS BUTT.png");
        JLabel selectLabel = new JLabel(selectIcon);
        selectLabel.setBounds(1000, 155, 229, 94);
        bgPanel.add(selectLabel);
        bgPanel.setComponentZOrder(selectLabel, 1);

        // Preview Rectangle
        ImageIcon rectIcon = AssetLoader.loadIcon("/javamon/assets/RECTANGLE.png", "RECTANGLE.png");
        JLabel rectangle = new JLabel(rectIcon);
        int rectW = (rectIcon != null) ? rectIcon.getIconWidth() : 198;
        int rectH = (rectIcon != null) ? rectIcon.getIconHeight() : 371;
        rectangle.setBounds(1015, 253, rectW, rectH);
        bgPanel.add(rectangle);
        bgPanel.setComponentZOrder(rectangle, 1);

        floatingPreview = new JLabel();
        floatingPreview.setVisible(false);
        bgPanel.add(floatingPreview);
        bgPanel.setComponentZOrder(floatingPreview, 0); 

        // Buttons
        ImageIcon backIcon = AssetLoader.loadIcon("/javamon/assets/BACK BUTTON.png", "BACK BUTTON.png");
        JButton backButton = new JButton(backIcon);
        backButton.setBounds(20, 630, 204, 84);
        styleButton(backButton);
        backButton.addActionListener(e -> { 
            playSoundEffect("/javamon/assets/ButtonsFx.wav"); 
            Timer t = new Timer(300, ev -> { new MainMenu(); dispose(); });
            t.setRepeats(false); t.start();
        });
        bgPanel.add(backButton);

        ImageIcon proceedIcon = AssetLoader.loadIcon("/javamon/assets/PROCEED BUTTON.png", "PROCEED BUTTON.png");
        JButton proceedButton = new JButton(proceedIcon);
        proceedButton.setBounds(1016, 630, proceedIcon != null ? proceedIcon.getIconWidth() : 200, proceedIcon != null ? proceedIcon.getIconHeight() : 80);
        styleButton(proceedButton);
        proceedButton.addActionListener(e -> {
            playSoundEffect("/javamon/assets/ButtonsFx.wav"); 
            Timer t = new Timer(300, ev -> { new DraftSelection(selectedTrainerClass); dispose(); });
            t.setRepeats(false); t.start();
        });
        bgPanel.add(proceedButton);

        // Z-Ordering
        bgPanel.setComponentZOrder(borderLabel, bgPanel.getComponentCount()-1);
        bgPanel.setComponentZOrder(rectangle, bgPanel.getComponentCount()-2);

        // Trainer Cards
        String[] trainers = {"ELEMENTALIST", "STRATEGIST", "AGGRESSOR", "BEASTMASTER", "MYSTIC"};
        int[][] positions = {{26, 100, 267, 441}, {325, 95, 302, 456}, {637, 89, 298, 467}, {195, 320, 228, 460}, {502, 328, 270, 441}};

        for (int i = 0; i < trainers.length; i++) {
            String tName = trainers[i];
            int[] pos = positions[i];
            ImageIcon icon = AssetLoader.loadIcon("/javamon/assets/" + tName + ".png", tName + ".png");
            
            JButton btn = new JButton(icon);
            btn.setBounds(pos[0], pos[1], pos[2], pos[3]);
            styleButton(btn);
            
            btn.addActionListener(e -> {
                playSoundEffect("/javamon/assets/ButtonsFx.wav"); 
                showConfirmationDialog(tName, icon, pos[2], pos[3], rectangle);
            });
            bgPanel.add(btn);
            bgPanel.setComponentZOrder(btn, 1);
        }

        setVisible(true);
    }
    
    private void styleButton(JButton b) {
        b.setBorderPainted(false); b.setContentAreaFilled(false); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    // ========================================
    // SOUND EFFECT PLAYBACK METHOD
    // ========================================
    private void playSoundEffect(String path) {
        try {
            URL url = getClass().getResource(path);
            if(url == null) {
                System.err.println("Sound file not found: " + path);
                return;
            }
            
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            
            // Get volume preference from the user settings saved in MainMenu
            Preferences prefs = Preferences.userNodeForPackage(TrainerSelection.class);
            int savedVolume = prefs.getInt("volume", 50);
            
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gain.getMaximum() - gain.getMinimum();
                // Set the volume based on the saved preference
                float gainVal = (range * (savedVolume / 100f)) + gain.getMinimum();
                gain.setValue(gainVal);
            }
            
            // Start playing the clip once
            clip.start();
            
            // Add a listener to close the clip resources when it finishes playing
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing sound effect: " + e.getMessage());
        }
    }
    
    // --- CONFIRMATION POPUP ---
    private void showConfirmationDialog(String trainerName, ImageIcon icon, int w, int h, JLabel rectangle) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(DARK_BG); panel.setBorder(new LineBorder(NEON_GOLD, 3));
        
        JLabel header = new JLabel("CONFIRM CLASS", SwingConstants.CENTER);
        header.setFont(HEADER_FONT); header.setForeground(NEON_GOLD);
        header.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        panel.add(header, BorderLayout.NORTH);
        
        JPanel content = new JPanel(); content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false); content.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));
        
        JLabel nameLabel = new JLabel(trainerName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 36)); nameLabel.setForeground(Color.WHITE); nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(nameLabel); content.add(Box.createVerticalStrut(25));
        
        JLabel descHeader = new JLabel("CLASS PERK", SwingConstants.CENTER);
        descHeader.setFont(new Font("Arial", Font.BOLD, 14)); descHeader.setForeground(NEON_GOLD); descHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(descHeader);
        content.add(createCenteredTextPane(getClassDescription(trainerName), DESC_FONT, Color.LIGHT_GRAY));
        content.add(Box.createVerticalStrut(25));

        JLabel stratHeader = new JLabel("STRATEGY GUIDE", SwingConstants.CENTER);
        stratHeader.setFont(new Font("Arial", Font.BOLD, 14)); stratHeader.setForeground(NEON_CYAN); stratHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(stratHeader);
        JTextPane suggest = createCenteredTextPane(getClassSuggestion(trainerName), SUGGEST_FONT, Color.WHITE);
        suggest.setBackground(new Color(30, 30, 45)); suggest.setBorder(BorderFactory.createCompoundBorder(new LineBorder(NEON_CYAN, 1, true), BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        content.add(suggest);
        panel.add(content, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15)); btnPanel.setOpaque(false);
        JButton confirm = new JButton(" SELECT "); confirm.setBackground(new Color(50, 200, 50)); confirm.setForeground(Color.WHITE); confirm.setFocusPainted(false);
        JButton cancel = new JButton(" CANCEL "); cancel.setBackground(new Color(200, 50, 50)); cancel.setForeground(Color.WHITE); cancel.setFocusPainted(false);
        
        confirm.addActionListener(e -> { 
            playSoundEffect("/javamon/assets/ButtonsFx.wav"); 
            selectedTrainerClass = trainerName; 
            updatePreview(icon, w, h, rectangle); 
            confirmationDialog.dispose(); 
        });
        cancel.addActionListener(e -> {
            playSoundEffect("/javamon/assets/ButtonsFx.wav"); 
            confirmationDialog.dispose();
        });
        
        btnPanel.add(confirm); btnPanel.add(cancel); panel.add(btnPanel, BorderLayout.SOUTH);
        confirmationDialog.setContentPane(panel); confirmationDialog.revalidate(); confirmationDialog.repaint(); confirmationDialog.setVisible(true);
    }
    
    private JTextPane createCenteredTextPane(String text, Font font, Color color) {
        JTextPane pane = new JTextPane(); pane.setText(text); pane.setFont(font); pane.setForeground(color); pane.setBackground(DARK_BG); pane.setEditable(false); pane.setFocusable(false); pane.setOpaque(true);
        StyledDocument doc = pane.getStyledDocument(); SimpleAttributeSet center = new SimpleAttributeSet(); StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER); doc.setParagraphAttributes(0, doc.getLength(), center, false);
        pane.setAlignmentX(Component.CENTER_ALIGNMENT); return pane;
    }
    
    private String getClassDescription(String name) {
        if(name.equals("ELEMENTALIST")) return "Bonus effectiveness on advantage attacks (+0.5x Multiplier).";
        if(name.equals("STRATEGIST")) return "All healing and defensive abilities are 25% better.";
        if(name.equals("AGGRESSOR")) return "Attacks deal 20% more damage, but defenses are weaker.";
        if(name.equals("BEASTMASTER")) return "When HP < 50%, Attack and Speed increase by 20%.";
        if(name.equals("MYSTIC")) return "Increases the chance of status effects by +20%.";
        return "Unknown Class";
    }

    private String getClassSuggestion(String name) {
        if(name.equals("ELEMENTALIST")) return "Draft different Types (Fire, Water, Lightning) to cover all weaknesses. Best for offensive players.";
        if(name.equals("STRATEGIST")) return "Best with Tanks like Guyum or Healers like Wilkeens. Stall and outlast your opponent!";
        if(name.equals("AGGRESSOR")) return "Draft Fast Sweepers like Lectric or Alailaw. Kill them before they hit you!";
        if(name.equals("BEASTMASTER")) return "Works well with Bulky attackers like Sawalee. Survive a hit, then get angry!";
        if(name.equals("MYSTIC")) return "Pick Disruptors like Santan (Stun) or Sorbeetez (Freeze) to lock down enemies.";
        return "Choose wisely.";
    }

    private void updatePreview(ImageIcon icon, int w, int h, JLabel rectangle) {
        if(icon == null) return;
        int fw = icon.getIconWidth(), fh = icon.getIconHeight();
        if (fw <= 0) fw = w; if (fh <= 0) fh = h;
        int rectX = rectangle.getX(), rectY = rectangle.getY(), rectW = rectangle.getWidth(), rectH = rectangle.getHeight();
        int fx = rectX + (rectW - fw) / 2, fy = rectY + (rectH - fh) / 2;
        floatingPreview.setIcon(icon); floatingPreview.setBounds(fx, fy, fw, fh); floatingPreview.setVisible(true);
        this.getContentPane().setComponentZOrder(floatingPreview, 0); this.revalidate(); this.repaint();
    }
}