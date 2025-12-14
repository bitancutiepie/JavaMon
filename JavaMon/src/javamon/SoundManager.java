package javamon;

import javax.sound.sampled.*;
import java.net.URL;
import java.util.prefs.Preferences;
import java.io.IOException;

public class SoundManager {

    private static SoundManager instance;
    private Clip menuBgClip;
    private Clip gameBgClip; // New Clip for Game Window
    private final String MENU_MUSIC_PATH = "/javamon/assets/bgsound.wav";
    private final String GAME_MUSIC_PATH = "/javamon/assets/gamewindowbg.wav"; // New Path

    // Private constructor to enforce Singleton pattern
    private SoundManager() {
        loadBackgroundMusic();
    }

    // Public method to get the single instance of the SoundManager
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void loadBackgroundMusic() {
        // Load Menu Clip
        menuBgClip = loadClip(MENU_MUSIC_PATH);
        // Load Game Clip
        gameBgClip = loadClip(GAME_MUSIC_PATH);
        
        // Set initial volume on both clips
        Preferences prefs = Preferences.userNodeForPackage(MainMenu.class);
        int savedVolume = prefs.getInt("volume", 50);
        setVolume(savedVolume);
    }
    
    private Clip loadClip(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("Music file not found: " + path);
                return null;
            }
            AudioInputStream audio = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            return clip;
        } catch (Exception e) {
            System.err.println("Failed to load audio: " + path + " | " + e.getMessage());
            return null;
        }
    }

    public void playMenuMusic() {
        stopGameMusic(); // Stop other music
        if (menuBgClip != null) {
            if (menuBgClip.getMicrosecondPosition() > 0 && menuBgClip.getMicrosecondPosition() < menuBgClip.getMicrosecondLength()) {
                // If paused, just start
                menuBgClip.start();
            } else if (!menuBgClip.isRunning()) {
                menuBgClip.setFramePosition(0);
                menuBgClip.loop(Clip.LOOP_CONTINUOUSLY);
                menuBgClip.start();
            }
        }
    }

    // NEW: Control Game Music
    public void playGameMusic() {
        stopMenuMusic(); // Stop other music
        if (gameBgClip != null && !gameBgClip.isRunning()) {
            gameBgClip.setFramePosition(0);
            gameBgClip.loop(Clip.LOOP_CONTINUOUSLY);
            gameBgClip.start();
        }
    }

    public void stopMenuMusic() {
        if (menuBgClip != null && menuBgClip.isRunning()) {
            menuBgClip.stop();
        }
    }
    
    public void stopGameMusic() {
        if (gameBgClip != null && gameBgClip.isRunning()) {
            gameBgClip.stop();
        }
    }

    public void stopAllMusic() {
        stopMenuMusic();
        stopGameMusic();
    }

    public void setVolume(int volume) {
        setClipVolume(menuBgClip, volume);
        setClipVolume(gameBgClip, volume); // Apply volume to the new clip
    }
    
    private void setClipVolume(Clip clip, int volume) {
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float range = gain.getMaximum() - gain.getMinimum();
            float gainVal = (range * (volume / 100f)) + gain.getMinimum();
            gain.setValue(gainVal);
        }
    }

    // --- Accessor for volume slider in MainMenu ---
    // Since MainMenu needs to control the volume, we expose the primary clip for its slider
    public Clip getMenuClip() {
        return menuBgClip;
    }
    
    // --- Accessor for volume slider in MainMenu ---
    // If GameWindow needs a slider, this would be exposed
    public Clip getGameClip() {
        return gameBgClip;
    }
}