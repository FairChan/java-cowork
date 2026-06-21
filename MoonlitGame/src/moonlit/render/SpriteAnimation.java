package moonlit.render;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javafx.scene.image.Image;

/**
 * Represents a fixed-size horizontal spritesheet animation.
 */
public class SpriteAnimation {
    private final Image image;
    private final int frameWidth;
    private final int frameHeight;
    private final int frameCount;
    private final double frameDuration;
    private double elapsed;

    public SpriteAnimation(String relativePath, int frameWidth, int frameHeight, int frameCount, double frameDuration) {
        this.image = AssetLoader.loadImage(relativePath);
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.frameCount = frameCount;
        this.frameDuration = frameDuration;
    }

    public void update(double deltaSeconds) {
        elapsed += deltaSeconds;
    }

    public boolean isAvailable() {
        return image != null;
    }

    public Image getImage() {
        return image;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public int getCurrentFrame() {
        if (frameDuration <= 0) {
            return 0;
        }
        return (int) (elapsed / frameDuration) % frameCount;
    }

    public static boolean isFrameCompatible(Path path, int expectedFrames) {
        if (!Files.exists(path)) {
            return false;
        }
        try {
            BufferedImage image = ImageIO.read(path.toFile());
            return image != null
                    && image.getWidth() > 0
                    && image.getHeight() > 0
                    && image.getWidth() % expectedFrames == 0;
        } catch (IOException exception) {
            return false;
        }
    }
}
