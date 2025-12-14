package javamon;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.*; // Needed for audio
import java.net.URL; // Needed for audio
import java.util.prefs.Preferences; // Needed for volume
import java.io.IOException; // Needed for audio

public class GameOver extends JFrame {

    private final SoundManager soundManager = SoundManager.getInstance();

    public GameOver(int floorsCleared, Runnable onMainMenuCallback) {
        setTitle("Game Over");
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
     // Set window icon
        Image icon = AssetLoader.loadImage("/javamon/assets/icon.png", "icon.png");
        if (icon != null) setIconImage(icon);

        // --- MUSIC ---
        soundManager.stopAllMusic(); // Ensure all lingering music is stopped
        playSoundEffect("/javamon/assets/gameover.wav"); // Play game over sound effect
        // --- END MUSIC ---

        JLayeredPane lp = new JLayeredPane();
        lp.setPreferredSize(new Dimension(800, 600));
        setContentPane(lp);

        // --- Background ---
        ImageIcon bgIcon = loadIcon("/javamon/assets/gameover_bg.png");
        JLabel bg = new JLabel(bgIcon);
        bg.setBounds(0, 0, 800, 600);
        lp.add(bg, Integer.valueOf(0));

        // --- Floors Cleared Label (Score Display) ---
        JLabel scoreLabel = new JLabel("Floors Cleared: " + floorsCleared, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Impact", Font.BOLD, 48));
        scoreLabel.setForeground(new Color(255, 215, 0)); // Neon Gold
        
        // **FIXED POSITION: Top Center**
        // Original size: 400x50
        // Centered horizontally: (800 - 400) / 2 = 200
        // Top placement: Y=50
        scoreLabel.setBounds(200, 50, 400, 50); 
        lp.add(scoreLabel, Integer.valueOf(1));


        // --- Main Menu Button ---
        ImageIcon mainIcon = loadIcon("/javamon/assets/btn_main.png");
        JButton btnMain = new JButton(mainIcon);
        btnMain.setBounds(150, 430, mainIcon.getIconWidth(), mainIcon.getIconHeight());
        style(btnMain);
        lp.add(btnMain, Integer.valueOf(1));

        btnMain.addActionListener(e -> {
            playSoundEffect("/javamon/assets/ButtonsFx.wav"); // <--- APPLIED SOUND EFFECT
            Timer t = new Timer(300, ev -> {
                dispose(); // close GameOver window
                if (onMainMenuCallback != null) onMainMenuCallback.run();
                soundManager.playMenuMusic(); // Restart menu music
            });
            t.setRepeats(false);
            t.start();
        });

        // --- Exit Button ---
        ImageIcon exitIcon = loadIcon("/javamon/assets/btn_exit.png");
        JButton btnExit = new JButton(exitIcon);
        btnExit.setBounds(450, 430, exitIcon.getIconWidth(), exitIcon.getIconHeight());
        style(btnExit);
        lp.add(btnExit, Integer.valueOf(1));

        btnExit.addActionListener(e -> {
            playSoundEffect("/javamon/assets/ButtonsFx.wav"); // <--- APPLIED SOUND EFFECT
            // Use a short timer to allow the sound to play before the dialog/exit
            Timer t = new Timer(300, ev -> {
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to exit?",
                    "Exit Game",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    soundManager.stopAllMusic();
                    System.exit(0);
                }
            });
            t.setRepeats(false);
            t.start();
        });

        pack();
        setVisible(true);
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
            
            // Get volume preference from the user settings
            Preferences prefs = Preferences.userNodeForPackage(GameOver.class);
            int savedVolume = prefs.getInt("volume", 50);
            
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gain.getMaximum() - gain.getMinimum();
                // Set the volume based on the saved preference
                float gainVal = (range * (savedVolume / 100f)) + gain.getMinimum();
                gain.setValue(gainVal);
            }
            
            clip.start();
            
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing sound effect: " + e.getMessage());
        }
    }
}