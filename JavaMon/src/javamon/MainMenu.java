package javamon;

import javax.swing.*;
import java.awt.*;
import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("JavaMon");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        
        // Use AssetLoader
        Image icon = AssetLoader.loadImage("/javamon/assets/icon.png", "icon.png");
        if (icon != null) setIconImage(icon);

        setVisible(true);

        // Music
        Clip bgClip = playBackgroundMusic("/javamon/assets/bgsound.wav");

        JPanel bgPanel = new JPanel() {
            private Image bg = AssetLoader.loadImage("/javamon/assets/loading.png", "loading.png");
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if(bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        bgPanel.setLayout(null);
        setContentPane(bgPanel);

        // Start button
        ImageIcon startIcon = AssetLoader.loadIcon("/javamon/assets/start.png", "start.png");
        if(startIcon != null) {
            Image scaled = startIcon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
            startIcon = new ImageIcon(scaled);
        }
        
        JButton startButton = createButton(startIcon, 102, 459, 155, 53);
        startButton.addActionListener(e -> {
            new TrainerSelection(); // Logic is inside constructor
            this.dispose();
        });
        bgPanel.add(startButton);

        // Exit button
        ImageIcon exitIcon = AssetLoader.loadIcon("/javamon/assets/exit.png", "exit.png");
        if(exitIcon != null) {
            Image scaled = exitIcon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
            exitIcon = new ImageIcon(scaled);
        }

        JButton exitButton = createButton(exitIcon, 568, 459, 155, 53);
        exitButton.addActionListener(e -> System.exit(0));
        bgPanel.add(exitButton);

        // Volume
        JPanel volumePanel = createModernVolumeControl(bgClip);
        volumePanel.setBounds(320, 500, 157, 53);
        bgPanel.add(volumePanel);
    }
    
    private JButton createButton(ImageIcon icon, int x, int y, int w, int h) {
        JButton btn = new JButton(icon);
        btn.setBounds(x, y, w, h);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createModernVolumeControl(Clip bgClip) {
        JPanel panel = new JPanel(null);
        panel.setOpaque(false);

        JLabel volumeIcon = new JLabel("🔊") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.BLACK); g2d.setFont(getFont()); g2d.drawString(getText(), 2, 22);
                g2d.setColor(Color.WHITE); g2d.drawString(getText(), 1, 21);
            }
        };
        volumeIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        volumeIcon.setBounds(0, 15, 30, 30);
        volumeIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(volumeIcon);

        JSlider volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.setBounds(40, 20, 150, 20);
        volumeSlider.setOpaque(false);
        volumeSlider.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Simplified custom UI for brevity/stability
        volumeSlider.setUI(new javax.swing.plaf.basic.BasicSliderUI(volumeSlider) {
            public void paintTrack(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(200, 200, 200, 150));
                g2d.fillRoundRect(trackRect.x, trackRect.y + 7, trackRect.width, 6, 6, 6);
                int fillW = (int)(trackRect.width * (volumeSlider.getValue()/100.0));
                g2d.setColor(new Color(76, 175, 80));
                g2d.fillRoundRect(trackRect.x, trackRect.y + 7, fillW, 6, 6, 6);
            }
            public void paintThumb(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(Color.WHITE);
                g2d.fillOval(thumbRect.x, thumbRect.y, 16, 16);
            }
        });
        panel.add(volumeSlider);

        JLabel volumeLabel = new JLabel("50%");
        volumeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        volumeLabel.setForeground(Color.WHITE);
        volumeLabel.setBounds(75, 0, 50, 15);
        panel.add(volumeLabel);

        volumeSlider.addChangeListener(e -> {
            int val = volumeSlider.getValue();
            volumeLabel.setText(val + "%");
            if (bgClip != null) {
                FloatControl gain = (FloatControl) bgClip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gain.getMaximum() - gain.getMinimum();
                float gainVal = (range * (val / 100f)) + gain.getMinimum();
                gain.setValue(gainVal);
            }
        });

        return panel;
    }

    private Clip playBackgroundMusic(String path) {
        try {
            URL url = getClass().getResource(path);
            if(url == null) return null;
            AudioInputStream audio = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            return clip;
        } catch (Exception e) { return null; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu());
    }
}