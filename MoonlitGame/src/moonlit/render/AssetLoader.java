package moonlit.render;

import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.image.Image;

/**
 * Loads project PNG assets from disk and lets callers fall back gracefully.
 */
public final class AssetLoader {
    private AssetLoader() {
    }

    public static Image loadImage(String relativePath) {
        Path path = Path.of(relativePath);
        if (!Files.exists(path)) {
            return null;
        }
        try {
            Image image = new Image(path.toUri().toString(), false);
            return image.isError() ? null : image;
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
