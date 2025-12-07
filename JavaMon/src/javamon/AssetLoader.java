package javamon;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

public class AssetLoader {

    /**
     * Primary method to load images.
     */
    public static Image loadImage(String resourcePath, String fileName) {
        // 1. Try to load from resource stream (JAR/ClassPath)
        try {
            URL url = AssetLoader.class.getResource(resourcePath);
            if (url != null) {
                return ImageIO.read(url);
            }
        } catch (Exception ignored) {
            // Fallthrough to failure if resource not found
        }

        System.err.println("Asset not found: " + resourcePath + " | " + fileName);
        return null;
    }
    
    /**
     * COMPATIBILITY FIX: 
     * This method simply calls loadImage. 
     * It exists so Monster.java and GameWindow.java don't crash looking for "loadImagePreferResource".
     */
    public static Image loadImagePreferResource(String resourcePath, String fileName) {
        return loadImage(resourcePath, fileName);
    }
    
    /**
     * Helper to load an ImageIcon directly.
     */
    public static ImageIcon loadIcon(String resourcePath, String fileName) {
        Image img = loadImage(resourcePath, fileName);
        if (img != null) return new ImageIcon(img);
        return null;
    }
}