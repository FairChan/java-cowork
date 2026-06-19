package moonlit.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import moonlit.engine.GameConfig;
import moonlit.engine.GameEngine;
import moonlit.render.AnimatedSpriteRenderer;
import moonlit.render.SpriteAnimation;

/**
 * Stage enemy with simple movement and shooting behavior.
 */
public class Enemy extends Entity {
    public enum Kind {
        LANTERN,
        CHARM_FAIRY
    }

    private final Kind kind;
    private final double velocityX;
    private final double velocityY;
    private final SpriteAnimation animation;
    private double age;
    private double shootCooldown;

    private Enemy(Kind kind, double x, double y, double velocityX, double velocityY,
            int hp, double radius, SpriteAnimation animation) {
        super(x, y, radius, hp);
        this.kind = kind;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.animation = animation;
        this.shootCooldown = kind == Kind.LANTERN ? 1.2 : 1.0;
    }

    public static Enemy lantern(double x, double y, double velocityX, double velocityY) {
        return new Enemy(Kind.LANTERN, x, y, velocityX, velocityY, 10, 16,
                new SpriteAnimation("assets/sprites/enemy_lantern.png", 56, 56, 4, 0.16));
    }

    public static Enemy charmFairy(double x, double y, double velocityX, double velocityY) {
        return new Enemy(Kind.CHARM_FAIRY, x, y, velocityX, velocityY, 20, 17,
                new SpriteAnimation("assets/sprites/enemy_charm_fairy.png", 56, 56, 4, 0.14));
    }

    @Override
    public void update(double deltaSeconds, GameEngine engine) {
        age += deltaSeconds;
        animation.update(deltaSeconds);
        x += velocityX * deltaSeconds + Math.sin(age * 2.0) * 10 * deltaSeconds;
        y += velocityY * deltaSeconds;

        shootCooldown -= deltaSeconds;
        if (shootCooldown <= 0 && y > GameConfig.PLAYFIELD_Y + 40 && y < GameConfig.PLAYFIELD_BOTTOM - 120) {
            shoot(engine);
            shootCooldown = kind == Kind.LANTERN ? 1.65 : 1.25;
        }

        if (x < GameConfig.PLAYFIELD_X - 90 || x > GameConfig.PLAYFIELD_RIGHT + 90
                || y > GameConfig.PLAYFIELD_BOTTOM + 90) {
            alive = false;
        }
    }

    @Override
    public void render(GraphicsContext graphics) {
        if (animation.isAvailable()) {
            AnimatedSpriteRenderer.drawCentered(graphics, animation, x, y, 1.0);
            return;
        }

        if (kind == Kind.LANTERN) {
            graphics.setFill(Color.web("#f06b65"));
            graphics.fillOval(x - 16, y - 18, 32, 36);
            graphics.setFill(Color.web("#ffd175"));
            graphics.fillRect(x - 10, y - 8, 20, 16);
        } else {
            graphics.setFill(Color.web("#f5eabb"));
            graphics.fillRoundRect(x - 12, y - 18, 24, 36, 6, 6);
            graphics.setStroke(Color.web("#e3466b"));
            graphics.strokeLine(x - 12, y - 8, x + 12, y - 8);
        }
    }

    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);
    }

    public int getScoreValue() {
        return kind == Kind.LANTERN ? 500 : 850;
    }

    private void shoot(GameEngine engine) {
        if (kind == Kind.LANTERN) {
            for (int i = 0; i < 8; i++) {
                double angle = Math.PI * 2 * i / 8.0;
                engine.addEnemyBullet(new EnemyBullet(x, y, Math.cos(angle) * 88, Math.sin(angle) * 88, 5.2, i));
            }
            return;
        }

        double base = Math.atan2(engine.getPlayer().getY() - y, engine.getPlayer().getX() - x);
        for (int i = -1; i <= 1; i++) {
            double angle = base + i * 0.22;
            engine.addEnemyBullet(new EnemyBullet(x, y, Math.cos(angle) * 136, Math.sin(angle) * 136, 5.6, i + 5));
        }
    }
}
