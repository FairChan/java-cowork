package moonlit.model;

/**
 * Per-enemy firing variation chosen when a wave spawns, keeping danmaku readable but less repetitive.
 */
public final class EnemyPatternProfile {
    private final int bulletVariantBase;
    private final int bulletCount;
    private final double spacing;
    private final double speed;
    private final double radius;
    private final double cooldownScale;
    private final double spawnJitter;

    public EnemyPatternProfile(int bulletVariantBase, int bulletCount, double spacing, double speed,
            double radius, double cooldownScale, double spawnJitter) {
        this.bulletVariantBase = bulletVariantBase;
        this.bulletCount = Math.max(1, bulletCount);
        this.spacing = spacing;
        this.speed = speed;
        this.radius = radius;
        this.cooldownScale = Math.max(0.45, cooldownScale);
        this.spawnJitter = Math.max(0, spawnJitter);
    }

    public static EnemyPatternProfile defaultFor(Enemy.Kind kind) {
        return switch (kind) {
            case LANTERN -> new EnemyPatternProfile(0, 8, 0.0, 88, 5.2, 1.0, 0.0);
            case CHARM_FAIRY -> new EnemyPatternProfile(5, 3, 0.24, 142, 5.6, 1.0, 0.0);
            case KEDAMA -> new EnemyPatternProfile(2, 1, 0.0, 18, 4.5, 1.0, 0.0);
            case GREATER_FAIRY -> new EnemyPatternProfile(4, 16, 0.0, 92, 8.4, 1.0, 0.0);
            case SUNFLOWER -> new EnemyPatternProfile(2, 10, 0.0, 72, 7.0, 1.0, 0.0);
        };
    }

    public int bulletVariantBase() {
        return bulletVariantBase;
    }

    public int bulletCount() {
        return bulletCount;
    }

    public int oddBulletCount() {
        return bulletCount % 2 == 0 ? bulletCount + 1 : bulletCount;
    }

    public double spacing() {
        return spacing;
    }

    public double speed() {
        return speed;
    }

    public double radius() {
        return radius;
    }

    public double cooldownScale() {
        return cooldownScale;
    }

    public double spawnJitter() {
        return spawnJitter;
    }
}