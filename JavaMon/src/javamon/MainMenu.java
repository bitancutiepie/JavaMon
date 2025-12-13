package javamon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*; // <-- NEW IMPORT
import javax.sound.sampled.*;
import java.net.URL;
import java.util.prefs.Preferences;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.geom.RoundRectangle2D; // Needed for ModernSliderUI

public class MainMenu extends JFrame {

    private static final ConcurrentHashMap<Clip, Timer> activeFadeTimers = new ConcurrentHashMap<>();
    private Clip bgClip; // Store clip reference

    public MainMenu() {
        setTitle("JavaMon");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        
        // Use AssetLoader
        Image icon = AssetLoader.loadImage("/javamon/assets/icon.png", "icon.png");
        if (icon != null) setIconImage(icon);

        // Music
        bgClip = playBackgroundMusic("/javamon/assets/bgsound.wav"); // Clip starts here and volume is set

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
            if (bgClip != null) bgClip.stop(); // Stop music on exit
            new TrainerSelection();
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
        // NEW: Enhanced Volume Panel (Wider to accommodate features)
        JPanel volumePanel = createEnhancedVolumeControl(bgClip);
        volumePanel.setBounds(270, 490, 260, 70); // Repositioned and larger
        bgPanel.add(volumePanel);
        
        setVisible(true);
    }
    
    @Override
    public void dispose() {
        if (bgClip != null) {
            bgClip.stop();
            bgClip.close();
        }
        super.dispose();
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

    // ========================================
    // ENHANCED VOLUME CONTROL SYSTEM
    // ========================================
    
    private JPanel createEnhancedVolumeControl(Clip bgClip) {
        JPanel panel = new JPanel(null);
        panel.setOpaque(false);

        Preferences prefs = Preferences.userNodeForPackage(MainMenu.class);
        int savedVolume = prefs.getInt("volume", 50);
        
        final boolean[] isMuted = {savedVolume == 0};
        final int[] lastVolume = {savedVolume == 0 ? 50 : savedVolume};

        // ===== VOLUME ICON (Clickable Mute Button) =====
        VolumeIconButton volumeIcon = new VolumeIconButton(savedVolume);
        volumeIcon.setBounds(5, 15, 40, 40);
        
        // ===== VOLUME SLIDER =====
        JSlider volumeSlider = new JSlider(0, 100, savedVolume);
        volumeSlider.setBounds(55, 25, 140, 20);
        volumeSlider.setOpaque(false);
        volumeSlider.setCursor(new Cursor(Cursor.HAND_CURSOR));
        volumeSlider.setFocusable(true);
        
        // ===== VOLUME PERCENTAGE LABEL =====
        JLabel volumeLabel = new JLabel(savedVolume == 0 ? "MUTE" : savedVolume + "%");
        volumeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        volumeLabel.setForeground(Color.WHITE);
        volumeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        volumeLabel.setBounds(200, 23, 55, 25);
        
        // ===== POPUP VOLUME INDICATOR =====
        JLabel popupIndicator = new JLabel(savedVolume + "%");
        popupIndicator.setFont(new Font("Arial", Font.BOLD, 20));
        popupIndicator.setForeground(Color.WHITE);
        popupIndicator.setOpaque(true);
        popupIndicator.setBackground(new Color(0, 0, 0, 220));
        popupIndicator.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(76, 175, 80), 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        popupIndicator.setHorizontalAlignment(SwingConstants.CENTER);
        popupIndicator.setBounds(85, -50, 80, 40);
        popupIndicator.setVisible(false);
        panel.add(popupIndicator);

        // ===== MUTE BUTTON LOGIC =====
        volumeIcon.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                isMuted[0] = !isMuted[0];
                
                if (isMuted[0]) {
                    lastVolume[0] = volumeSlider.getValue() > 0 ? volumeSlider.getValue() : 50;
                    volumeSlider.setValue(0);
                } else {
                    volumeSlider.setValue(lastVolume[0]);
                }
                
                showVolumePopup(popupIndicator, volumeSlider.getValue());
            }
            
            public void mouseEntered(MouseEvent e) {
                volumeIcon.setHovered(true);
            }
            
            public void mouseExited(MouseEvent e) {
                volumeIcon.setHovered(false);
            }
        });
        
        // ===== CUSTOM SLIDER UI =====
        volumeSlider.setUI(new ModernSliderUI(volumeSlider));
        
        // ===== SLIDER CHANGE LISTENER =====
        volumeSlider.addChangeListener(e -> {
            int val = volumeSlider.getValue();
            
            prefs.putInt("volume", val);
            
            volumeIcon.setVolume(val);
            volumeLabel.setText(val == 0 ? "MUTE" : val + "%");
            
            isMuted[0] = (val == 0);
            if (val > 0) lastVolume[0] = val;
            
            setVolumeSmooth(bgClip, val);
            
            if (volumeSlider.getValueIsAdjusting()) {
                showVolumePopup(popupIndicator, val);
            }
        });
        
        // ===== KEYBOARD CONTROLS =====
        volumeSlider.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int current = volumeSlider.getValue();
                int step = e.isShiftDown() ? 10 : 5;
                
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_RIGHT:
                        volumeSlider.setValue(Math.min(100, current + step));
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_LEFT:
                        volumeSlider.setValue(Math.max(0, current - step));
                        break;
                    case KeyEvent.VK_M:
                        // Trigger the mute click logic
                        volumeIcon.dispatchEvent(new MouseEvent(volumeIcon, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 0, 0, 1, false));
                        break;
                }
            }
        });
        
        // ===== MOUSE WHEEL SUPPORT =====
        volumeSlider.addMouseWheelListener(e -> {
            int current = volumeSlider.getValue();
            int delta = -e.getWheelRotation() * 5;
            volumeSlider.setValue(Math.max(0, Math.min(100, current + delta)));
        });

        panel.add(volumeIcon);
        panel.add(volumeSlider);
        panel.add(volumeLabel);

        return panel;
    }
    
    // ===== SHOW VOLUME POPUP =====
    private Timer popupTimer;
    
    private void showVolumePopup(JLabel popup, int volume) {
        popup.setText(volume == 0 ? "MUTE" : volume + "%");
        popup.setVisible(true);
        
        if (popupTimer != null && popupTimer.isRunning()) {
            popupTimer.stop();
        }
        
        popupTimer = new Timer(1200, e -> {
            popup.setVisible(false);
            popupTimer.stop();
        });
        popupTimer.setRepeats(false);
        popupTimer.start();
    }
    
    // ===== DYNAMIC VOLUME ICON (Uses standard Unicode Emojis) =====
    private String getVolumeIcon(int volume) {
        if (volume == 0) return "🔇";
        if (volume < 33) return "🔈";
        if (volume < 66) return "🔉";
        return "🔊";
    }
    
    // ===== SMOOTH VOLUME FADE =====
    private void setVolumeSmooth(Clip clip, int targetVolume) {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
        
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float range = gain.getMaximum() - gain.getMinimum();
        float targetGain = (range * (targetVolume / 100f)) + gain.getMinimum();
        
        Timer existingTimer = activeFadeTimers.get(clip);
        if (existingTimer != null && existingTimer.isRunning()) {
            existingTimer.stop();
        }

        float currentGain = gain.getValue();
        if (Math.abs(currentGain - targetGain) < 0.01f) return;
        
        Timer fadeTimer = new Timer(10, null);
        fadeTimer.addActionListener(e -> {
            float current = gain.getValue();
            float diff = targetGain - current;
            
            if (Math.abs(diff) < 0.05f) {
                gain.setValue(targetGain);
                fadeTimer.stop();
                activeFadeTimers.remove(clip);
            } else {
                // Increased step to 15% for faster response as requested
                gain.setValue(current + (diff * 0.15f));
            }
        });
        
        activeFadeTimers.put(clip, fadeTimer);
        fadeTimer.start();
    }
    
    // ===== BACKGROUND MUSIC PLAYBACK =====
    private Clip playBackgroundMusic(String path) {
        try {
            URL url = getClass().getResource(path);
            if(url == null) return null;
            
            AudioInputStream audio = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            
            Preferences prefs = Preferences.userNodeForPackage(MainMenu.class);
            int savedVolume = prefs.getInt("volume", 50);
            
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gain.getMaximum() - gain.getMinimum();
                float gainVal = (range * (savedVolume / 100f)) + gain.getMinimum();
                gain.setValue(gainVal);
            }
            
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            return clip;
        } catch (Exception e) { 
            System.err.println("Failed to load audio: " + e.getMessage());
            return null; 
        }
    }

    // ========================================
    // CUSTOM COMPONENTS
    // ========================================
    
    // ===== CUSTOM VOLUME ICON BUTTON (Inner Class) =====
    class VolumeIconButton extends JLabel {
        private int volume;
        private boolean hovered = false;
        
        public VolumeIconButton(int initialVolume) {
            this.volume = initialVolume;
            setText(getVolumeIcon(volume));
            setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(SwingConstants.CENTER);
        }
        
        public void setVolume(int vol) {
            this.volume = vol;
            setText(getVolumeIcon(vol));
            repaint();
        }
        
        public void setHovered(boolean h) {
            this.hovered = h;
            repaint();
        }
        
        // Custom paint to draw shadow and hover effect
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Hover glow effect
            if (hovered) {
                g2d.setColor(new Color(76, 175, 80, 50));
                g2d.fillOval(2, 2, getWidth()-4, getHeight()-4);
            }
            
            // Shadow
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.setFont(getFont());
            // Adjust coordinates for center based on font size/position
            g2d.drawString(getText(), getWidth()/2 - 13, getHeight()/2 + 12);
            
            // Main icon
            g2d.setColor(hovered ? new Color(100, 255, 100) : Color.WHITE);
            g2d.drawString(getText(), getWidth()/2 - 14, getHeight()/2 + 11);
        }
    }
    
    // ===== MODERN SLIDER UI (Inner Class) =====
    class ModernSliderUI extends BasicSliderUI {
        private static final int TRACK_HEIGHT = 8;
        private static final int THUMB_SIZE = 20;
        
        public ModernSliderUI(JSlider slider) {
            super(slider);
        }
        
        // Override calculateThumbSize and calculateTrackRect to ensure the thumb is drawn completely
        @Override
        protected Dimension getThumbSize() {
            return new Dimension(THUMB_SIZE, THUMB_SIZE);
        }

        // Adjust the track rectangle to account for the larger thumb size if needed, 
        // but often the BasicSliderUI handles this if the preferred size is set correctly.
        // We'll rely on BasicSliderUI's default calculations being sufficient for the 140px width.

        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int cy = trackRect.y + trackRect.height / 2;
            
            // Background track with shadow
            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.fill(new RoundRectangle2D.Float(trackRect.x + 1, cy - TRACK_HEIGHT/2 + 1, trackRect.width, TRACK_HEIGHT, TRACK_HEIGHT, TRACK_HEIGHT));
            
            g2d.setColor(new Color(50, 50, 50, 200));
            g2d.fill(new RoundRectangle2D.Float(trackRect.x, cy - TRACK_HEIGHT/2, trackRect.width, TRACK_HEIGHT, TRACK_HEIGHT, TRACK_HEIGHT));
            
            // Filled track with gradient
            int fillWidth = (int)(trackRect.width * (slider.getValue() / 100.0));
            
            if (fillWidth > 0) {
                GradientPaint gp = new GradientPaint(
                    trackRect.x, cy, new Color(50, 200, 80),
                    trackRect.x + fillWidth, cy, new Color(76, 255, 100)
                );
                g2d.setPaint(gp);
                g2d.fill(new RoundRectangle2D.Float(trackRect.x, cy - TRACK_HEIGHT/2, fillWidth, TRACK_HEIGHT, TRACK_HEIGHT, TRACK_HEIGHT));
                
                // Shine effect
                g2d.setColor(new Color(255, 255, 255, 80));
                g2d.fill(new RoundRectangle2D.Float(trackRect.x, cy - TRACK_HEIGHT/2, fillWidth, TRACK_HEIGHT/2, TRACK_HEIGHT/2, TRACK_HEIGHT/2));
            }
        }
        
        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int cx = thumbRect.x + thumbRect.width / 2;
            int cy = thumbRect.y + thumbRect.height / 2;
            
            // Shadow
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillOval(cx - THUMB_SIZE/2 + 2, cy - THUMB_SIZE/2 + 2, THUMB_SIZE, THUMB_SIZE);
            
            // Outer glow
            g2d.setColor(new Color(76, 175, 80, 100));
            g2d.fillOval(cx - THUMB_SIZE/2 - 2, cy - THUMB_SIZE/2 - 2, THUMB_SIZE + 4, THUMB_SIZE + 4);
            
            // Thumb gradient
            RadialGradientPaint rgp = new RadialGradientPaint(
                new Point(cx, cy - 3), THUMB_SIZE/2,
                new float[]{0f, 0.7f, 1f},
                new Color[]{Color.WHITE, new Color(240, 240, 240), new Color(200, 200, 200)}
            );
            g2d.setPaint(rgp);
            g2d.fillOval(cx - THUMB_SIZE/2, cy - THUMB_SIZE/2, THUMB_SIZE, THUMB_SIZE);
            
            // Border
            g2d.setColor(new Color(76, 175, 80));
            g2d.setStroke(new BasicStroke(2.5f));
            g2d.drawOval(cx - THUMB_SIZE/2, cy - THUMB_SIZE/2, THUMB_SIZE, THUMB_SIZE);
            
            // Center dot
            g2d.setColor(new Color(76, 175, 80));
            g2d.fillOval(cx - 3, cy - 3, 6, 6);
        }
        
        // This method ensures the thumb doesn't jump when track position is recalculated
        @Override
        public void calculateThumbSize() {
            super.calculateThumbSize();
            thumbRect.setSize(THUMB_SIZE, THUMB_SIZE);
        }

        // We must override this to ensure the thumb stays centered vertically regardless of default UI calculations.
        @Override
        protected void calculateThumbLocation() {
            super.calculateThumbLocation();
            thumbRect.y = trackRect.y + (trackRect.height / 2) - (THUMB_SIZE / 2);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu());
    }
}