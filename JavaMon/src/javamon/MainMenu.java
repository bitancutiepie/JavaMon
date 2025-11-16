package javamon;

import javax.swing.*;
import java.awt.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("JavaMon");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        

        // Set custom icon
        Image icon = new ImageIcon(getClass().getResource("/javamon/assets/icon.png")).getImage();
        setIconImage(icon);

        setVisible(true);

        // Play background music
        Clip bgClip = playBackgroundMusic("/javamon/assets/bgsound.wav");

        // Custom panel to paint background
        JPanel bgPanel = new JPanel() {
            private Image bg = new ImageIcon(getClass().getResource("/javamon/assets/loading.png")).getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        bgPanel.setLayout(null);
        setContentPane(bgPanel);

        // Start button
        ImageIcon startIcon = new ImageIcon(getClass().getResource("/javamon/assets/start.png"));
        Image scaledStartImg = startIcon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
        ImageIcon scaledStartIcon = new ImageIcon(scaledStartImg);

        JButton startButton = new JButton(scaledStartIcon);
        startButton.setBounds(102, 459, 155, 53);
        startButton.setBorderPainted(false);
        startButton.setContentAreaFilled(false);
        startButton.setFocusPainted(false);
        startButton.setOpaque(false);
        bgPanel.add(startButton);
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        startButton.addActionListener(e -> {
            TrainerSelection game = new TrainerSelection();
            game.setVisible(true);
            this.dispose();
        });

        // Exit button
        ImageIcon exitIcon = new ImageIcon(getClass().getResource("/javamon/assets/exit.png"));
        Image scaledExitImg = exitIcon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
        ImageIcon scaledExitIcon = new ImageIcon(scaledExitImg);

        JButton exitButton = new JButton(scaledExitIcon);
        exitButton.setBounds(568, 459, 155, 53);
        exitButton.setBorderPainted(false);
        exitButton.setContentAreaFilled(false);
        exitButton.setFocusPainted(false);
        exitButton.setOpaque(false);
        bgPanel.add(exitButton);
        exitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitButton.addActionListener(e -> System.exit(0));

        // volume panel
        JPanel volumePanel = createModernVolumeControl(bgClip);
        volumePanel.setBounds(320, 500, 157, 53);
        bgPanel.add(volumePanel);
    }

    private JPanel createModernVolumeControl(Clip bgClip) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setOpaque(false);

        // Volume icon label
     // Volume icon label with white text and shadow
        JLabel volumeIcon = new JLabel("🔊") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw text shadow for visibility
                g2d.setColor(Color.BLACK);
                g2d.setFont(getFont());
                g2d.drawString(getText(), 2, 22);
                
                // Draw white text
                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), 1, 21);
            }
        };
        volumeIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        volumeIcon.setBounds(0, 15, 30, 30);
        
        volumeIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(volumeIcon);

        // Modern slider with custom UI
        JSlider volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.setBounds(40, 20, 150, 20);
        volumeSlider.setOpaque(false);
        volumeSlider.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Custom modern slider UI
        volumeSlider.setUI(new javax.swing.plaf.basic.BasicSliderUI(volumeSlider) {
            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Rectangle trackBounds = trackRect;
                int trackHeight = 6;
                int trackY = trackBounds.y + (trackBounds.height - trackHeight) / 2;
                
                // Background track
                g2d.setColor(new Color(200, 200, 200, 150));
                g2d.fillRoundRect(trackBounds.x, trackY, trackBounds.width, trackHeight, trackHeight, trackHeight);
                
                // Filled track (progress)
                int fillWidth = (int) (trackBounds.width * (volumeSlider.getValue() / 100.0));
                g2d.setColor(new Color(76, 175, 80)); // Green color
                g2d.fillRoundRect(trackBounds.x, trackY, fillWidth, trackHeight, trackHeight, trackHeight);
            }
            
            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Rectangle knobBounds = thumbRect;
                int thumbSize = 16;
                
                // Shadow
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.fillOval(knobBounds.x + 1, knobBounds.y + 1, thumbSize, thumbSize);
                
                // Thumb
                g2d.setColor(Color.WHITE);
                g2d.fillOval(knobBounds.x, knobBounds.y, thumbSize, thumbSize);
                
                // Border
                g2d.setColor(new Color(76, 175, 80));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(knobBounds.x, knobBounds.y, thumbSize, thumbSize);
            }
        });
        
        panel.add(volumeSlider);

        // Volume percentage label
        JLabel volumeLabel = new JLabel("50%");
        volumeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        volumeLabel.setForeground(Color.WHITE);
        volumeLabel.setBounds(75, 0, 50, 15);
        panel.add(volumeLabel);

        // Update volume icon based on level
        volumeSlider.addChangeListener(e -> {
            int sliderVal = volumeSlider.getValue();
            volumeLabel.setText(sliderVal + "%");

            // Update icon based on volume
            if (sliderVal == 0) {
                volumeIcon.setText("🔇"); // Muted
            } else if (sliderVal < 33) {
                volumeIcon.setText("🔈"); // Low
            } else if (sliderVal < 66) {
                volumeIcon.setText("🔉"); // Medium
            } else {
                volumeIcon.setText("🔊"); // High
            }

            // Control audio
            if (bgClip != null) {
                FloatControl volumeControl = (FloatControl) bgClip.getControl(FloatControl.Type.MASTER_GAIN);

                if (sliderVal == 0) {
                    if (bgClip.isRunning()) bgClip.stop();
                } else {
                    if (!bgClip.isRunning()) bgClip.start();

                    float min = volumeControl.getMinimum();
                    float max = volumeControl.getMaximum();
                    float volume = min + (max - min) * (sliderVal / 100f);
                    volumeControl.setValue(volume);
                }
            }
        });

        // Click icon to mute/unmute
        final int[] lastVolume = {50};
        volumeIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (volumeSlider.getValue() > 0) {
                    lastVolume[0] = volumeSlider.getValue();
                    volumeSlider.setValue(0);
                } else {
                    volumeSlider.setValue(lastVolume[0]);
                }
            }
        });

        return panel;
    }

    private Clip playBackgroundMusic(String path) {
        try {
            URL soundURL = getClass().getResource(path);
            AudioInputStream audio = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}