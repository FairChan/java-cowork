package moonlit.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import moonlit.engine.GameEngine;

/**
 * Hostile bullet with a color variant, optional acceleration, additive glow, and one-time graze state.
 */
public class EnemyBullet extends Projectile {
    private final int variant;
    private final boolean additive;
    private final double accelerationPerSecond;
    private final double minimumSpeed;
    private boolean grazed;

    public EnemyBullet(double x, double y, double velocityX, double velocityY, double radius, int variant) {
        this(x, y, velocityX, velocityY, radius, variant, 0, 0, false);
    }

    private EnemyBullet(double x, double y, double velocityX, double velocityY, double radius, int variant,
            double accelerationPerSecond, double minimumSpeed, boolean additive) {
        super(x, y, radius, velocityX, velocityY);
        this.variant = variant;
        this.accelerationPerSecond = accelerationPerSecond;
        this.minimumSpeed = Math.max(0, minimumSpeed);
        this.additive = additive;
    }

    public static EnemyBullet acceleratingGlow(double x, double y, double velocityX, double velocityY,
            double radius, int variant, double accelerationPerSecond, double minimumSpeed) {
        return new EnemyBullet(x, y, velocityX, velocityY, radius, variant,
                accelerationPerSecond, minimumSpeed, true);
    }

    @Override
    public void update(double deltaSeconds, GameEngine engine) {
        applyAcceleration(deltaSeconds);
        super.update(deltaSeconds, engine);
    }

    @Override
    public void render(GraphicsContext graphics) {
        Color outer = switch (Math.floorMod(variant, 8)) {
            case 1 -> Color.web("#ff9f4d");
            case 2 -> Color.web("#d77bff");
            case 3 -> Color.web("#82e8ff");
            case 4 -> Color.web("#fff06a");
            case 5 -> Color.web("#67ff9a");
            case 6 -> Color.web("#ff5f5f");
            case 7 -> Color.web("#f5f0ff");
            default -> Color.web("#ff5f9f");
        };
        if (additive) {
            BlendMode oldBlend = graphics.getGlobalBlendMode();
            double oldAlpha = graphics.getGlobalAlpha();
            graphics.setGlobalBlendMode(BlendMode.ADD);
            graphics.setGlobalAlpha(0.46);
            graphics.setFill(outer);
            graphics.fillOval(x - radius * 2.2, y - radius * 2.2, radius * 4.4, radius * 4.4);
            graphics.setGlobalAlpha(0.74);
            graphics.setFill(Color.WHITE);
            graphics.fillOval(x - radius * 1.18, y - radius * 1.18, radius * 2.36, radius * 2.36);
            graphics.setGlobalBlendMode(oldBlend);
            graphics.setGlobalAlpha(oldAlpha);
        }

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

    public int getVariantForTests() {
        return variant;
    }

    public boolean isAdditive() {
        return additive;
    }

    public double getAccelerationPerSecond() {
        return accelerationPerSecond;
    }

    private void applyAcceleration(double deltaSeconds) {
        if (accelerationPerSecond == 0) {
            return;
        }
        double speed = getSpeed();
        if (speed <= 0) {
            return;
        }
        double nextSpeed = speed + accelerationPerSecond * deltaSeconds;
        if (accelerationPerSecond < 0) {
            nextSpeed = Math.max(minimumSpeed, nextSpeed);
        }
        if (nextSpeed <= 0) {
            velocityX = 0;
            velocityY = 0;
            return;
        }
        double scale = nextSpeed / speed;
        velocityX *= scale;
        velocityY *= scale;
    }
}