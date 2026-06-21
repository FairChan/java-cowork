package moonlit.model;

import java.util.List;
import java.util.Random;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import moonlit.engine.GameConfig;
import moonlit.engine.GameEngine;
import moonlit.pattern.BulletPattern;
import moonlit.pattern.XOddAimedPattern;
import moonlit.render.AnimatedSpriteRenderer;
import moonlit.render.SpriteAnimation;

/**
 * Boss with a formal nonspell/spell-card phase queue for Starry Illusion.
 */
public class Boss extends Entity {
    private static final double CENTER_X = GameConfig.PLAYFIELD_X + GameConfig.PLAYFIELD_WIDTH / 2.0;

    private final int stageNumber;
    private final List<BossPhase> phases;
    private int phaseIndex;
    private double movementTime;
    private double phaseElapsed;
    private boolean phaseAdvancePending;
    private final SpriteAnimation normalAnimation =
            new SpriteAnimation("assets/sprites/boss_normal.png", 53, 96, 46, 1.0 / 12.0);
    private final SpriteAnimation abnormalAnimation =
            new SpriteAnimation("assets/sprites/boss_abnormal.png", 53, 96, 46, 1.0 / 12.0);

    public Boss(double x, double y) {
        this(x, y, 1);
    }

    public Boss(double x, double y, int stageNumber) {
        super(x, y, 30, initialHp(stageNumber));
        this.stageNumber = stageNumber;
        this.phases = stageNumber == 2 ? createStageTwoPhases() : createStarryIllusionPhases();
        resetPhaseStats();
    }

    @Override
    public void update(double deltaSeconds, GameEngine engine) {
        if (!alive) {
            return;
        }
        normalAnimation.update(deltaSeconds);
        abnormalAnimation.update(deltaSeconds);
        movementTime += deltaSeconds;
        phaseElapsed += deltaSeconds;

        if (phaseAdvancePending) {
            advancePhase(engine, true);
            if (!alive) {
                return;
            }
        }

        updateMovement(deltaSeconds, engine);
        BossPhase phase = phases.get(phaseIndex);
        if (phaseElapsed >= phase.getTimeLimitSeconds()) {
            advancePhase(engine, false);
            if (!alive) {
                return;
            }
            phase = phases.get(phaseIndex);
        }
        phase.getPattern().update(this, engine, deltaSeconds);
    }

    @Override
    public void render(GraphicsContext graphics) {
        SpriteAnimation activeAnimation = isAbnormalState() ? abnormalAnimation : normalAnimation;
        if (!activeAnimation.isAvailable() && activeAnimation == abnormalAnimation) {
            activeAnimation = normalAnimation;
        }
        if (activeAnimation.isAvailable()) {
            AnimatedSpriteRenderer.drawCentered(graphics, activeAnimation, x, y, 1.08);
            return;
        }

        graphics.setFill(Color.web("#23123d"));
        graphics.fillOval(x - 32, y - 28, 64, 56);
        graphics.setFill(Color.web("#e7d2ff"));
        graphics.fillOval(x - 20, y - 23, 40, 46);
        graphics.setFill(Color.web("#5633a6"));
        graphics.fillOval(x - 6, y - 22, 26, 44);
        graphics.setFill(Color.web("#fff6a8"));
        graphics.fillArc(x - 44, y - 44, 32, 32, 300, 230, javafx.scene.shape.ArcType.ROUND);
        graphics.setStroke(Color.web("#f7bdd7"));
        graphics.setLineWidth(2);
        graphics.strokeOval(x - 38, y - 34, 76, 68);
    }

    @Override
    public void takeDamage(int damage) {
        if (!alive || damage <= 0) {
            return;
        }
        hp = Math.max(0, hp - damage);
        if (hp > 0) {
            return;
        }
        if (phaseIndex >= phases.size() - 1) {
            alive = false;
        } else {
            phaseAdvancePending = true;
        }
    }

    public int getPhaseIndex() {
        return phaseIndex;
    }

    public int getPhaseCount() {
        return phases.size();
    }

    public int getStageNumber() {
        return stageNumber;
    }

    public String getPhaseName() {
        return phases.get(phaseIndex).getName();
    }

    public void skipCurrentPhaseForTests(GameEngine engine) {
        advancePhase(engine, false);
    }

    public void skipToFinalPhaseForTests(GameEngine engine) {
        phaseIndex = phases.size() - 1;
        movementTime = 0;
        phaseElapsed = 0;
        phaseAdvancePending = false;
        resetPhaseStats();
        phases.get(phaseIndex).getPattern().reset();
        if (engine != null && stageNumber != 2) {
            double width = masterSparkWidth();
            engine.addLaser(Laser.verticalMasterSpark(x, y + 18, width, -105));
        }
    }

    private void advancePhase(GameEngine engine, boolean defeated) {
        int completed = phaseIndex;
        phaseAdvancePending = false;
        if (phaseIndex >= phases.size() - 1) {
            alive = false;
            return;
        }
        phaseIndex++;
        movementTime = 0;
        phaseElapsed = 0;
        resetPhaseStats();
        phases.get(phaseIndex).getPattern().reset();
        if (engine != null) {
            engine.clearEnemyBullets();
            if (defeated) {
                engine.dropBossPhaseReward(completed);
            }
            engine.getParticles().spawnRing(x, y, Color.web("#fff6a8"), 42);
        }
    }

    private void resetPhaseStats() {
        maxHp = phases.get(phaseIndex).getHp();
        hp = maxHp;
        alive = true;
    }

    private void updateMovement(double deltaSeconds, GameEngine engine) {
        if (stageNumber == 2) {
            x = CENTER_X + Math.sin(movementTime * 0.68) * 132;
            y = 116 + Math.sin(movementTime * 1.5) * 18;
            return;
        }

        switch (phaseIndex) {
            case 0 -> {
                x = CENTER_X + Math.sin(movementTime * 1.7) * 165;
                y = 122 + Math.sin(movementTime * 3.4) * 32;
            }
            case 1 -> {
                x += (CENTER_X - x) * Math.min(1.0, deltaSeconds * 3.8);
                y += (104 - y) * Math.min(1.0, deltaSeconds * 3.8);
            }
            case 2 -> {
                int side = ((int) (movementTime / 2.35)) % 2 == 0 ? -1 : 1;
                x += (CENTER_X + side * 190 - x) * Math.min(1.0, deltaSeconds * 6.0);
                y += (112 - y) * Math.min(1.0, deltaSeconds * 4.0);
            }
            case 3 -> {
                double targetX = engine.getPlayer().getX();
                x += (targetX - x) * Math.min(1.0, deltaSeconds * 0.55);
                y += (110 - y) * Math.min(1.0, deltaSeconds * 2.0);
            }
            case 4 -> {
                x += (CENTER_X - x) * Math.min(1.0, deltaSeconds * 4.0);
                y += (156 - y) * Math.min(1.0, deltaSeconds * 4.0);
            }
            default -> {
                double targetX = engine.getPlayer().getX();
                x += (targetX - x) * Math.min(1.0, deltaSeconds * 1.1);
                y += (110 - y) * Math.min(1.0, deltaSeconds * 3.0);
            }
        }
    }

    private boolean isAbnormalState() {
        return phaseIndex >= phases.size() - 1 || hp <= maxHp * 0.35;
    }

    private double masterSparkWidth() {
        double ratio = maxHp == 0 ? 1.0 : hp / (double) maxHp;
        return 150 + (1.0 - ratio) * 80;
    }

    private static int initialHp(int stageNumber) {
        return stageNumber == 2 ? 900 : 2_000;
    }

    private static List<BossPhase> createStageTwoPhases() {
        return List.of(
                new BossPhase("Spell 1: X Odd Aimed", BossPhase.Kind.SPELL, 900, 45, new XOddAimedPattern(1)),
                new BossPhase("Spell 2: Crossing Streams", BossPhase.Kind.SPELL, 900, 45, new XOddAimedPattern(2)),
                new BossPhase("Spell 3: Starcrossed Gate", BossPhase.Kind.SPELL, 900, 55, new XOddAimedPattern(3)));
    }

    private static List<BossPhase> createStarryIllusionPhases() {
        return List.of(
                new BossPhase("Nonspell 1: High-Mobility Stardust", BossPhase.Kind.NONSPELL, 2_000, 30,
                        new HighMobilityStardustPattern()),
                new BossPhase("Spell 1: Stardust Reverie", BossPhase.Kind.SPELL, 2_500, 45,
                        new StardustReveriePattern()),
                new BossPhase("Nonspell 2: Light and Heat", BossPhase.Kind.NONSPELL, 2_200, 30,
                        new LightAndHeatPattern()),
                new BossPhase("Spell 2: Non-Directional Laser", BossPhase.Kind.SPELL, 3_000, 50,
                        new NonDirectionalLaserPattern()),
                new BossPhase("Nonspell 3: Magic Circle Storm", BossPhase.Kind.NONSPELL, 2_500, 30,
                        new MagicCircleStormPattern()),
                new BossPhase("Final Spell: Final Master Spark", BossPhase.Kind.SPELL, 4_500, 60,
                        new FinalMasterSparkPattern()));
    }

    private static final class HighMobilityStardustPattern implements BulletPattern {
        private double timer;
        private double angleOffset;

        @Override
        public void update(Boss boss, GameEngine engine, double deltaSeconds) {
            timer -= deltaSeconds;
            if (timer > 0) {
                return;
            }
            timer = 0.95;
            angleOffset += Math.PI / 22.0;
            for (int ring = 0; ring < 3; ring++) {
                int count = 26 + ring * 4;
                double speed = 285 - ring * 35;
                for (int i = 0; i < count; i++) {
                    double angle = angleOffset + ring * 0.08 + Math.PI * 2.0 * i / count;
                    engine.addEnemyBullet(EnemyBullet.acceleratingGlow(boss.getX(), boss.getY(),
                            Math.cos(angle) * speed, Math.sin(angle) * speed, 5.4,
                            i % 2 == 0 ? 3 : 8, -190, 72));
                }
            }
        }

        @Override
        public void reset() {
            timer = 0;
            angleOffset = 0;
        }
    }

    private static final class StardustReveriePattern implements BulletPattern {
        private double bounceTimer;
        private double laserTimer = 1.0;
        private double angle;

        @Override
        public void update(Boss boss, GameEngine engine, double deltaSeconds) {
            bounceTimer -= deltaSeconds;
            laserTimer -= deltaSeconds;
            angle += deltaSeconds * 1.2;
            if (bounceTimer <= 0) {
                bounceTimer = 0.18;
                fireEmitter(engine, boss.getX() - 72, boss.getY() + 6, Math.PI + Math.sin(angle) * 0.55);
                fireEmitter(engine, boss.getX() + 72, boss.getY() + 6, Math.sin(angle + Math.PI) * 0.55);
            }
            if (laserTimer <= 0) {
                laserTimer = 5.0;
                engine.addLaser(Laser.aimedWarning(boss.getX(), boss.getY(), engine.getPlayer().getX(),
                        engine.getPlayer().getY(), 18, Color.web("#fff6a8")));
            }
        }

        @Override
        public void reset() {
            bounceTimer = 0;
            laserTimer = 1.0;
            angle = 0;
        }

        private void fireEmitter(GameEngine engine, double x, double y, double angle) {
            double speed = 155;
            engine.addEnemyBullet(new BouncingEnemyBullet(x, y,
                    Math.cos(angle) * speed, Math.sin(angle) * speed + 70, 6.0, 8, 2));
        }
    }

    private static final class LightAndHeatPattern implements BulletPattern {
        private final Random random = new Random(42);
        private double laserTimer;
        private double noiseTimer;

        @Override
        public void update(Boss boss, GameEngine engine, double deltaSeconds) {
            laserTimer -= deltaSeconds;
            noiseTimer -= deltaSeconds;
            if (laserTimer <= 0) {
                laserTimer = 2.05;
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4.0;
                    engine.addLaser(new Laser(boss.getX(), boss.getY(), angle, 820, 17, 0.5, 0.32, 0,
                            Color.web("#ff3e64")));
                }
            }
            if (noiseTimer <= 0) {
                noiseTimer = 0.08;
                double angle = random.nextDouble() * Math.PI * 2.0;
                double speed = 75 + random.nextDouble() * 120;
                engine.addEnemyBullet(new EnemyBullet(boss.getX(), boss.getY(), Math.cos(angle) * speed,
                        Math.sin(angle) * speed, 4.3, 1));
            }
        }

        @Override
        public void reset() {
            laserTimer = 0;
            noiseTimer = 0;
        }
    }

    private static final class NonDirectionalLaserPattern implements BulletPattern {
        private double laserTimer;
        private double starTimer;
        private double spin;

        @Override
        public void update(Boss boss, GameEngine engine, double deltaSeconds) {
            laserTimer -= deltaSeconds;
            starTimer -= deltaSeconds;
            spin += deltaSeconds * 1.9;
            if (laserTimer <= 0) {
                laserTimer = 0.28;
                for (int i = 0; i < 6; i++) {
                    engine.addLaser(new Laser(boss.getX(), boss.getY(), spin + i * Math.PI / 3.0,
                            520, 24, 0.05, 0.24, 0, Color.web("#8ee7ff")));
                }
            }
            if (starTimer <= 0) {
                starTimer = 0.075;
                for (int i = 0; i < 5; i++) {
                    double angle = spin + i * Math.PI * 2.0 / 5.0 + Math.sin(spin * 2.1) * 0.45;
                    double speed = 128 + i * 8;
                    engine.addEnemyBullet(new EnemyBullet(boss.getX(), boss.getY(), Math.cos(angle) * speed,
                            Math.sin(angle) * speed, 4.8, i + 7));
                }
            }
        }

        @Override
        public void reset() {
            laserTimer = 0;
            starTimer = 0;
            spin = 0;
        }
    }

    private static final class MagicCircleStormPattern implements BulletPattern {
        private final Random random = new Random(93);
        private double burstTimer;

        @Override
        public void update(Boss boss, GameEngine engine, double deltaSeconds) {
            burstTimer -= deltaSeconds;
            if (burstTimer > 0) {
                return;
            }
            burstTimer = 0.24;
            double x = GameConfig.PLAYFIELD_X + 45 + random.nextDouble() * (GameConfig.PLAYFIELD_WIDTH - 90);
            double y = GameConfig.PLAYFIELD_Y + 80 + random.nextDouble() * 330;
            engine.getParticles().spawnRing(x, y, Color.web("#d77bff"), 18);
            int count = 8 + random.nextInt(5);
            for (int i = 0; i < count; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0;
                double speed = 85 + random.nextDouble() * 170;
                engine.addEnemyBullet(new EnemyBullet(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed,
                        7.8, i + random.nextInt(4)));
            }
        }

        @Override
        public void reset() {
            burstTimer = 0;
        }
    }

    private static final class FinalMasterSparkPattern implements BulletPattern {
        private final Random random = new Random(111);
        private double laserTimer;
        private double streamTimer;

        @Override
        public void update(Boss boss, GameEngine engine, double deltaSeconds) {
            laserTimer -= deltaSeconds;
            streamTimer -= deltaSeconds;
            if (laserTimer <= 0) {
                laserTimer = 2.7;
                double push = engine.getPlayer().getX() <= boss.getX() ? -118 : 118;
                engine.addLaser(Laser.verticalMasterSpark(boss.getX(), boss.getY() + 18, boss.masterSparkWidth(), push));
            }
            if (streamTimer <= 0) {
                streamTimer = 0.055;
                double y = GameConfig.PLAYFIELD_Y + 170 + random.nextDouble() * (GameConfig.PLAYFIELD_HEIGHT - 230);
                double leftAngle = Math.atan2(engine.getPlayer().getY() - y, engine.getPlayer().getX() - GameConfig.PLAYFIELD_X);
                double rightAngle = Math.atan2(engine.getPlayer().getY() - y,
                        engine.getPlayer().getX() - GameConfig.PLAYFIELD_RIGHT);
                double speed = 150 + random.nextDouble() * 95;
                engine.addEnemyBullet(EnemyBullet.acceleratingGlow(GameConfig.PLAYFIELD_X, y,
                        Math.cos(leftAngle) * speed, Math.sin(leftAngle) * speed, 4.8, 8, 34, speed + 90));
                engine.addEnemyBullet(EnemyBullet.acceleratingGlow(GameConfig.PLAYFIELD_RIGHT, y + 18,
                        Math.cos(rightAngle) * speed, Math.sin(rightAngle) * speed, 4.8, 9, 34, speed + 90));
            }
        }

        @Override
        public void reset() {
            laserTimer = 0;
            streamTimer = 0;
        }
    }
}

