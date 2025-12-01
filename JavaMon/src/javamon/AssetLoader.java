package javamon;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

public class AssetLoader {

    private static final String LOCAL_DIR = "/mnt/data/";

    /**
     * Attempts to load an image, preferring the resource stream (for JAR/deployed app)
     * and falling back to a local file path (for development/testing).
     *
     * @param resourcePath The path to the asset within the JAR (e.g., "/javamon/assets/icon.png").
     * @param fileName The name of the file for the local path (e.g., "icon.png").
     * @return The loaded Image, or null if loading fails from both sources.
     */
    public static Image loadImagePreferResource(String resourcePath, String fileName) {
        // 1. Try to load from resource stream
        try {
            URL url = AssetLoader.class.getResource(resourcePath);
            if (url != null) {
                BufferedImage img = ImageIO.read(url);
                if (img != null) return img;
            }
        } catch (Exception ignored) {
            // Ignore exception for resource load failure and try local file
        }

        // 2. Try to load from local directory
        String localPath = LOCAL_DIR + fileName;
        try {
            File f = new File(localPath);
            if (f.exists()) {
                BufferedImage img = ImageIO.read(f);
                if (img != null) return img;
            }
        } catch (Exception ignored) {
            // Ignore exception for local file load failure
        }

        System.err.println("Image not found: " + resourcePath + " | " + localPath);
        return null;
    }
}