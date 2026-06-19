package moonlit.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Player projectile that damages the boss.
 */
public class PlayerShot extends Projectile {
    private final int damage;

    public PlayerShot(double x, double y) {
        super(x, y, 4, 0, -560);
        this.damage = 10;
    }

    @Override
    public void render(GraphicsContext graphics) {
        graphics.setFill(Color.web("#8dfbff"));
        graphics.fillOval(x - 3, y - 12, 6, 24);
        graphics.setFill(Color.web("#ffffff", 0.8));
        graphics.fillOval(x - 1.5, y - 8, 3, 16);
    }

    public int getDamage() {
        return damage;
    }
}
