package moonlit.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Hostile bullet with a color variant and one-time graze state.
 */
public class EnemyBullet extends Projectile {
    private final int variant;
    private boolean grazed;

    public EnemyBullet(double x, double y, double velocityX, double velocityY, double radius, int variant) {
        super(x, y, radius, velocityX, velocityY);
        this.variant = variant;
    }

    @Override
    public void render(GraphicsContext graphics) {
        Color outer = switch (variant % 4) {
            case 1 -> Color.web("#ff9f4d");
            case 2 -> Color.web("#d77bff");
            case 3 -> Color.web("#82e8ff");
            default -> Color.web("#ff5f9f");
        };
        graphics.setFill(outer);
        graphics.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        graphics.setFill(Color.web("#fff7fa", 0.75));
        graphics.fillOval(x - radius * 0.42, y - radius * 0.42, radius * 0.84, radius * 0.84);
    }

    public boolean isGrazed() {
        return grazed;
    }

    public void markGrazed() {
        grazed = true;
    }
}
