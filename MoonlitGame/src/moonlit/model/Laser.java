package moonlit.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import moonlit.engine.GameConfig;
import moonlit.engine.GameEngine;

/**
 * Timed warning/active laser used by Starry Illusion boss and sunflower waves.
 */
public class Laser extends GameObject {
    private final double angle;
    private final double length;
    private final double width;
    private final double warningSeconds;
    private final double activeSeconds;
    private final double pushX;
    private final Color color;
    private double age;

    public Laser(double x, double y, double angle, double length, double width,
            double warningSeconds, double activeSeconds, double pushX, Color color) {
        super(x, y, width / 2.0);
        this.angle = angle;
        this.length = length;
        this.width = width;
        this.warningSeconds = warningSeconds;
        this.activeSeconds = activeSeconds;
        this.pushX = pushX;
        this.color = color;
    }

    public static Laser verticalMasterSpark(double x, double y, double width, double pushX) {
        return new Laser(x, y, Math.PI / 2.0, GameConfig.PLAYFIELD_HEIGHT + 120, width,
                1.0, 1.7, pushX, Color.web("#fff6a8"));
    }

    public static Laser aimedWarning(double x, double y, double targetX, double targetY, double width, Color color) {
        double angle = Math.atan2(targetY - y, targetX - x);
        return new Laser(x, y, angle, GameConfig.PLAYFIELD_HEIGHT * 1.35, width, 0.5, 0.42, 0, color);
    }

    @Override
    public void update(double deltaSeconds, GameEngine engine) {
        age += deltaSeconds;
        if (isActive()) {
            Player player = engine.getPlayer();
            double distance = distanceToBeam(player.getX(), player.getY());
            if (distance <= width / 2.0 + player.getHitRadius()) {
                if (player.canBeHit() && !engine.isInvincibleMode()) {
                    player.hit();
                    engine.notifyPlayerDamaged();
                }
                if (Math.abs(pushX) > 0) {
                    player.push(pushX * deltaSeconds, 0);
                }
            }
        }
        if (age >= warningSeconds + activeSeconds) {
            alive = false;
        }
    }

    @Override
    public void render(GraphicsContext graphics) {
        double endX = x + Math.cos(angle) * length;
        double endY = y + Math.sin(angle) * length;
        if (!isActive()) {
            graphics.setStroke(Color.web("#ff2a4f", 0.72));
            graphics.setLineWidth(Math.max(2, width * 0.16));
            graphics.strokeLine(x, y, endX, endY);
            return;
        }

        BlendMode oldBlend = graphics.getGlobalBlendMode();
        double oldAlpha = graphics.getGlobalAlpha();
        graphics.setGlobalBlendMode(BlendMode.ADD);
        graphics.setGlobalAlpha(0.36);
        graphics.setStroke(color);
        graphics.setLineWidth(width * 1.55);
        graphics.strokeLine(x, y, endX, endY);
        graphics.setGlobalAlpha(0.78);
        graphics.setStroke(Color.WHITE);
        graphics.setLineWidth(width * 0.62);
        graphics.strokeLine(x, y, endX, endY);
        graphics.setGlobalBlendMode(oldBlend);
        graphics.setGlobalAlpha(oldAlpha);
    }

    public boolean isActive() {
        return age >= warningSeconds;
    }

    private double distanceToBeam(double pointX, double pointY) {
        double dx = pointX - x;
        double dy = pointY - y;
        double projection = dx * Math.cos(angle) + dy * Math.sin(angle);
        projection = Math.max(0, Math.min(length, projection));
        double closestX = x + Math.cos(angle) * projection;
        double closestY = y + Math.sin(angle) * projection;
        return Math.hypot(pointX - closestX, pointY - closestY);
    }
}
