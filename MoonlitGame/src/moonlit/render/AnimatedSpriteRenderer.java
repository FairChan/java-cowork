package moonlit.render;

import javafx.scene.canvas.GraphicsContext;

/**
 * Draws a spritesheet animation using a center anchor.
 */
public final class AnimatedSpriteRenderer {
    private AnimatedSpriteRenderer() {
    }

    public static void drawCentered(GraphicsContext graphics, SpriteAnimation animation,
            double centerX, double centerY, double scale) {
        if (animation == null || !animation.isAvailable()) {
            return;
        }
        int frame = animation.getCurrentFrame();
        double sx = frame * animation.getFrameWidth();
        double sy = 0;
        double sw = animation.getFrameWidth();
        double sh = animation.getFrameHeight();
        double dw = sw * scale;
        double dh = sh * scale;
        graphics.drawImage(animation.getImage(), sx, sy, sw, sh, centerX - dw / 2.0, centerY - dh / 2.0, dw, dh);
    }
}
