package moonlit.model;

import java.util.Random;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import moonlit.engine.GameConfig;
import moonlit.engine.GameEngine;
import moonlit.render.AnimatedSpriteRenderer;
import moonlit.render.SpriteAnimation;

/**
 * Stage enemy with stage-specific movement and per-spawn shooting variation.
 */
public class Enemy extends Entity {
    public enum Kind {
        LANTERN,
        CHARM_FAIRY,
        KEDAMA,
        GREATER_FAIRY,
        SUNFLOWER
    }

    private enum MotionStyle {
        LINEAR,
        U_PROBE,
        Z_SWEEP,
        HOVER,
        SUNFLOWER_ARC
    }

    private static final int MONSTER_FRAME_COUNT = 46;
    private static final int MONSTER_FRAME_HEIGHT = 96;
    private static final double MONSTER_FRAME_DURATION = 1.0 / 12.0;
    private static final double MONSTER_RENDER_SCALE = 0.62;
    private static final Random VISUAL_RANDOM = new Random(20260620L);
    private static final Random ATTACK_RANDOM = new Random(20260621L);
    private static final SpriteSpec[] MONSTER_SPRITES = new SpriteSpec[] {
            new SpriteSpec("assets/sprites/monster1.png", 57),
            new SpriteSpec("assets/sprites/monster2.png", 63),
            new SpriteSpec("assets/sprites/monster3.png", 61),
            new SpriteSpec("assets/sprites/monster4.png", 71),
            new SpriteSpec("assets/sprites/monster5.png", 90),
            new SpriteSpec("assets/sprites/monster6.png", 67),
            new SpriteSpec("assets/sprites/monster7.png", 69),
            new SpriteSpec("assets/sprites/monster8.png", 83)
    };

    private final Kind kind;
    private final MotionStyle motionStyle;
    private final double velocityX;
    private final double velocityY;
    private final double originX;
    private final double originY;
    private final boolean fromLeft;
    private final double targetX;
    private final double targetY;
    private final SpriteAnimation animation;
    private final String visualSpritePath;
    private final EnemyPatternProfile patternProfile;
    private double age;
    private double shootCooldown;

    private Enemy(Kind kind, MotionStyle motionStyle, double x, double y, double velocityX, double velocityY,
            int hp, double radius, boolean fromLeft, double targetX, double targetY,
            EnemyPatternProfile patternProfile, SpriteSpec spriteSpec) {
        super(x, y, radius, hp);
        this.kind = kind;
        this.motionStyle = motionStyle;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.originX = x;
        this.originY = y;
        this.fromLeft = fromLeft;
        this.targetX = targetX;
        this.targetY = targetY;
        this.visualSpritePath = spriteSpec.path;
        this.patternProfile = patternProfile == null ? EnemyPatternProfile.defaultFor(kind) : patternProfile;
        this.animation = new SpriteAnimation(spriteSpec.path, spriteSpec.frameWidth,
                MONSTER_FRAME_HEIGHT, MONSTER_FRAME_COUNT, MONSTER_FRAME_DURATION);
        this.shootCooldown = initialShootCooldown(kind) * this.patternProfile.cooldownScale();
    }

    public static Enemy lantern(double x, double y, double velocityX, double velocityY) {
        return lantern(x, y, velocityX, velocityY, EnemyPatternProfile.defaultFor(Kind.LANTERN));
    }

    public static Enemy lantern(double x, double y, double velocityX, double velocityY,
            EnemyPatternProfile patternProfile) {
        return new Enemy(Kind.LANTERN, MotionStyle.LINEAR, x, y, velocityX, velocityY,
                10, 16, velocityX >= 0, x, y, patternProfile, randomMonsterSprite());
    }

    public static Enemy charmFairy(double x, double y, double velocityX, double velocityY) {
        return charmFairy(x, y, velocityX, velocityY, EnemyPatternProfile.defaultFor(Kind.CHARM_FAIRY));
    }

    public static Enemy charmFairy(double x, double y, double velocityX, double velocityY,
            EnemyPatternProfile patternProfile) {
        return new Enemy(Kind.CHARM_FAIRY, MotionStyle.LINEAR, x, y, velocityX, velocityY,
                20, 17, velocityX >= 0, x, y, patternProfile, randomMonsterSprite());
    }

    public static Enemy probeFairy(double y, boolean fromLeft) {
        double stopX = fromLeft ? GameConfig.PLAYFIELD_X + 170 : GameConfig.PLAYFIELD_RIGHT - 170;
        return probeFairy(y, fromLeft, stopX, GameConfig.PLAYFIELD_Y + 128,
                EnemyPatternProfile.defaultFor(Kind.CHARM_FAIRY));
    }

    public static Enemy probeFairy(double y, boolean fromLeft, double targetX, double targetY,
            EnemyPatternProfile patternProfile) {
        double startX = fromLeft ? GameConfig.PLAYFIELD_X - 40 : GameConfig.PLAYFIELD_RIGHT + 40;
        return new Enemy(Kind.CHARM_FAIRY, MotionStyle.U_PROBE, startX, y, 0, 0,
                18, 16, fromLeft, targetX, targetY, patternProfile, randomMonsterSprite());
    }

    public static Enemy kedama(double y, boolean fromLeft, int row) {
        return kedama(y, fromLeft, row, EnemyPatternProfile.defaultFor(Kind.KEDAMA));
    }

    public static Enemy kedama(double y, boolean fromLeft, int row, EnemyPatternProfile patternProfile) {
        double startX = fromLeft ? GameConfig.PLAYFIELD_X - 34 : GameConfig.PLAYFIELD_RIGHT + 34;
        double endX = fromLeft ? GameConfig.PLAYFIELD_RIGHT + 44 : GameConfig.PLAYFIELD_X - 44;
        double direction = fromLeft ? 1 : -1;
        return new Enemy(Kind.KEDAMA, MotionStyle.Z_SWEEP, startX, y, direction * 285, 0,
                7, 13, fromLeft, endX, y + (row % 3 - 1) * 58, patternProfile, randomMonsterSprite());
    }

    public static Enemy greaterFairy(double x, double targetY) {
        return greaterFairy(x, targetY, EnemyPatternProfile.defaultFor(Kind.GREATER_FAIRY));
    }

    public static Enemy greaterFairy(double x, double targetY, EnemyPatternProfile patternProfile) {
        return new Enemy(Kind.GREATER_FAIRY, MotionStyle.HOVER, x, GameConfig.PLAYFIELD_Y - 52, 0, 42,
                95, 24, x < GameConfig.PLAYFIELD_X + GameConfig.PLAYFIELD_WIDTH / 2.0, x, targetY,
                patternProfile, randomMonsterSprite());
    }

    public static Enemy sunflower(double fromX, boolean fromLeft) {
        return sunflower(fromX, fromLeft, EnemyPatternProfile.defaultFor(Kind.SUNFLOWER));
    }

    public static Enemy sunflower(double fromX, boolean fromLeft, EnemyPatternProfile patternProfile) {
        double target = fromLeft ? GameConfig.PLAYFIELD_X + 185 : GameConfig.PLAYFIELD_RIGHT - 185;
        return new Enemy(Kind.SUNFLOWER, MotionStyle.SUNFLOWER_ARC, fromX, GameConfig.PLAYFIELD_BOTTOM + 42, 0, 0,
                80, 24, fromLeft, target, GameConfig.PLAYFIELD_Y + 120, patternProfile, randomMonsterSprite());
    }

    @Override
    public void update(double deltaSeconds, GameEngine engine) {
        age += deltaSeconds;
        animation.update(deltaSeconds);
        updateMovement(deltaSeconds);

        shootCooldown -= deltaSeconds;
        if (shootCooldown <= 0 && y > GameConfig.PLAYFIELD_Y + 30 && y < GameConfig.PLAYFIELD_BOTTOM - 70) {
            shoot(engine);
            shootCooldown = nextShootCooldown(kind) * patternProfile.cooldownScale();
        }

        if (x < GameConfig.PLAYFIELD_X - 110 || x > GameConfig.PLAYFIELD_RIGHT + 110
                || y < GameConfig.PLAYFIELD_Y - 140 || y > GameConfig.PLAYFIELD_BOTTOM + 120) {
            alive = false;
        }
    }

    @Override
    public void render(GraphicsContext graphics) {
        if (animation.isAvailable()) {
            double scale = kind == Kind.GREATER_FAIRY || kind == Kind.SUNFLOWER ? 0.78 : MONSTER_RENDER_SCALE;
            AnimatedSpriteRenderer.drawCentered(graphics, animation, x, y, scale);
            return;
        }

        if (kind == Kind.LANTERN || kind == Kind.KEDAMA) {
            graphics.setFill(kind == Kind.KEDAMA ? Color.web("#f5f0ff") : Color.web("#f06b65"));
            graphics.fillOval(x - radius, y - radius, radius * 2, radius * 2.2);
            graphics.setFill(Color.web("#ffd175"));
            graphics.fillRect(x - 8, y - 6, 16, 12);
        } else {
            graphics.setFill(kind == Kind.SUNFLOWER ? Color.web("#ffe65c") : Color.web("#f5eabb"));
            graphics.fillRoundRect(x - radius * 0.85, y - radius, radius * 1.7, radius * 2, 6, 6);
            graphics.setStroke(Color.web("#e3466b"));
            graphics.strokeLine(x - radius * 0.75, y - 7, x + radius * 0.75, y - 7);
        }
    }

    public int getScoreValue() {
        return switch (kind) {
            case KEDAMA -> 220;
            case GREATER_FAIRY -> 2_500;
            case SUNFLOWER -> 2_000;
            case CHARM_FAIRY -> 850;
            case LANTERN -> 500;
        };
    }

    public Kind getKind() {
        return kind;
    }

    public String getVisualSpritePath() {
        return visualSpritePath;
    }

    private void updateMovement(double deltaSeconds) {
        switch (motionStyle) {
            case LINEAR -> {
                x += velocityX * deltaSeconds + Math.sin(age * 2.0) * 10 * deltaSeconds;
                y += velocityY * deltaSeconds;
            }
            case U_PROBE -> updateProbeMotion();
            case Z_SWEEP -> {
                x += velocityX * deltaSeconds;
                double p = Math.min(1.0, age / 2.8);
                y = originY + Math.sin(p * Math.PI * 3.0) * 42 + (targetY - originY) * p;
            }
            case HOVER -> {
                y += Math.signum(targetY - y) * Math.min(Math.abs(targetY - y), 84 * deltaSeconds);
                x = targetX + Math.sin(age * 1.6) * 36;
            }
            case SUNFLOWER_ARC -> {
                double p = Math.min(1.0, age / 2.2);
                double eased = 1.0 - Math.pow(1.0 - p, 3.0);
                x = originX + (targetX - originX) * eased;
                y = originY + (targetY - originY) * eased - Math.sin(p * Math.PI) * 95;
                if (age > 2.2) {
                    x = targetX + Math.sin(age * 1.25) * 58;
                    y = targetY + Math.sin(age * 1.9) * 22;
                }
            }
        }
    }

    private void updateProbeMotion() {
        if (age < 1.35) {
            double p = age / 1.35;
            double eased = 1.0 - Math.pow(1.0 - p, 2.0);
            x = originX + (targetX - originX) * eased;
            y = originY + Math.sin(p * Math.PI) * 72 + p * 54;
        } else if (age < 2.85) {
            x = targetX + Math.sin(age * 5.0) * 7;
            y = targetY + Math.sin(age * 4.0) * 8;
        } else {
            double p = Math.min(1.0, (age - 2.85) / 1.45);
            x = targetX + (fromLeft ? -55 : 55) * p;
            y = targetY - p * 210;
        }
    }

    private void shoot(GameEngine engine) {
        switch (kind) {
            case LANTERN -> shootLanternRing(engine);
            case CHARM_FAIRY -> shootOddAimed(engine, patternProfile.oddBulletCount(), patternProfile.spacing(),
                    patternProfile.speed(), patternProfile.radius(), patternProfile.bulletVariantBase());
            case KEDAMA -> engine.addEnemyBullet(new EnemyBullet(jitteredX(), jitteredY(), 0,
                    patternProfile.speed(), patternProfile.radius(), patternProfile.bulletVariantBase()));
            case GREATER_FAIRY -> shootGreaterFairy(engine);
            case SUNFLOWER -> shootSunflower(engine);
        }
    }

    private void shootLanternRing(GameEngine engine) {
        int count = patternProfile.bulletCount();
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2 * i / count + age * 0.12;
            engine.addEnemyBullet(new EnemyBullet(jitteredX(), jitteredY(), Math.cos(angle) * patternProfile.speed(),
                    Math.sin(angle) * patternProfile.speed(), patternProfile.radius(),
                    patternProfile.bulletVariantBase() + i));
        }
    }

    private void shootOddAimed(GameEngine engine, int count, double spacing, double speed, double bulletRadius,
            int variantBase) {
        double base = Math.atan2(engine.getPlayer().getY() - y, engine.getPlayer().getX() - x);
        int half = count / 2;
        for (int i = -half; i <= half; i++) {
            double angle = base + i * spacing;
            engine.addEnemyBullet(new EnemyBullet(jitteredX(), jitteredY(), Math.cos(angle) * speed,
                    Math.sin(angle) * speed, bulletRadius, variantBase + i + half));
        }
    }

    private void shootGreaterFairy(GameEngine engine) {
        int count = patternProfile.bulletCount();
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2 * i / count + age * 0.2;
            engine.addEnemyBullet(new EnemyBullet(jitteredX(), jitteredY(), Math.cos(angle) * patternProfile.speed(),
                    Math.sin(angle) * patternProfile.speed(), patternProfile.radius(),
                    patternProfile.bulletVariantBase() + i));
        }
        int scatterCount = Math.max(4, count / 3);
        for (int i = 0; i < scatterCount; i++) {
            double angle = Math.PI / 2.0 + (ATTACK_RANDOM.nextDouble() - 0.5) * 0.75;
            double speed = patternProfile.speed() + 35 + ATTACK_RANDOM.nextDouble() * 100;
            engine.addEnemyBullet(new EnemyBullet(jitteredX(), jitteredY(), Math.cos(angle) * speed,
                    Math.sin(angle) * speed, 5.2, patternProfile.bulletVariantBase() + i + 7));
        }
    }

    private void shootSunflower(GameEngine engine) {
        double targetX = engine.getPlayer().getX() + (ATTACK_RANDOM.nextDouble() - 0.5) * patternProfile.spawnJitter() * 6.0;
        double targetY = engine.getPlayer().getY() + (ATTACK_RANDOM.nextDouble() - 0.5) * patternProfile.spawnJitter() * 6.0;
        engine.addLaser(Laser.aimedWarning(x, y, targetX, targetY, 24, Color.web("#ff4f73")));
        for (int i = 0; i < patternProfile.bulletCount(); i++) {
            double angle = ATTACK_RANDOM.nextDouble() * Math.PI * 2.0;
            double speed = patternProfile.speed() * 0.65 + ATTACK_RANDOM.nextDouble() * patternProfile.speed();
            engine.addEnemyBullet(new EnemyBullet(jitteredX(), jitteredY(), Math.cos(angle) * speed,
                    Math.sin(angle) * speed, patternProfile.radius(), patternProfile.bulletVariantBase() + i));
        }
    }

    private static double initialShootCooldown(Kind kind) {
        return switch (kind) {
            case KEDAMA -> 0.2;
            case GREATER_FAIRY -> 0.9;
            case SUNFLOWER -> 1.0;
            case CHARM_FAIRY -> 1.0;
            case LANTERN -> 1.2;
        };
    }

    private static double nextShootCooldown(Kind kind) {
        return switch (kind) {
            case KEDAMA -> 0.22;
            case GREATER_FAIRY -> 1.0;
            case SUNFLOWER -> 1.35;
            case CHARM_FAIRY -> 1.25;
            case LANTERN -> 1.65;
        };
    }

    private double jitteredX() {
        return x + (ATTACK_RANDOM.nextDouble() - 0.5) * patternProfile.spawnJitter();
    }

    private double jitteredY() {
        return y + (ATTACK_RANDOM.nextDouble() - 0.5) * patternProfile.spawnJitter();
    }

    private static SpriteSpec randomMonsterSprite() {
        return MONSTER_SPRITES[VISUAL_RANDOM.nextInt(MONSTER_SPRITES.length)];
    }

    private static final class SpriteSpec {
        private final String path;
        private final int frameWidth;

        private SpriteSpec(String path, int frameWidth) {
            this.path = path;
            this.frameWidth = frameWidth;
        }
    }
}