package moonlit.pattern;

import moonlit.engine.GameEngine;
import moonlit.model.Boss;
import moonlit.model.EnemyBullet;
import moonlit.model.Player;

/**
 * Frame-synchronized X-shaped odd-way aimed spell inspired by ph3 task/yield scripts.
 */
public class XOddAimedPattern implements BulletPattern {
    private static final int WAY_COUNT = 9;
    private static final int HALF_WAYS = WAY_COUNT / 2;
    private static final int BURSTS_PER_ROUND = 3;
    private static final int BURST_WAIT_FRAMES = 12;

    private final int densityLevel;
    private final FrameTaskScheduler scheduler = new FrameTaskScheduler();
    private boolean started;

    public XOddAimedPattern() {
        this(1);
    }

    public XOddAimedPattern(int densityLevel) {
        this.densityLevel = Math.max(1, densityLevel);
    }

    @Override
    public void update(Boss boss, GameEngine engine, double deltaSeconds) {
        if (!started) {
            scheduler.spawn(new XOddAimedRoutine());
            started = true;
        }
        scheduler.update(deltaSeconds, boss, engine);
    }

    @Override
    public void reset() {
        scheduler.reset();
        started = false;
    }

    private void fireBurst(Boss boss, GameEngine engine, int burstIndex) {
        Player player = engine.getPlayer();
        double aimAngle = Math.atan2(player.getY() - boss.getY(), player.getX() - boss.getX());
        double[] armAngles = {
                aimAngle - Math.PI / 4.0,
                aimAngle + Math.PI / 4.0,
                aimAngle + Math.PI - Math.PI / 4.0,
                aimAngle + Math.PI + Math.PI / 4.0
        };
        double waySpacing = Math.toRadians(densityLevel >= 3 ? 3.6 : 4.4);
        double baseSpeed = 226 + densityLevel * 10 + burstIndex * 12;
        double acceleration = -92 - densityLevel * 8;
        double minimumSpeed = 88 + densityLevel * 8;

        for (int arm = 0; arm < armAngles.length; arm++) {
            for (int way = -HALF_WAYS; way <= HALF_WAYS; way++) {
                double angle = armAngles[arm] + way * waySpacing;
                double speed = baseSpeed + Math.abs(way) * 5;
                engine.addEnemyBullet(EnemyBullet.acceleratingGlow(
                        boss.getX(), boss.getY(),
                        Math.cos(angle) * speed,
                        Math.sin(angle) * speed,
                        5.8,
                        8 + arm + burstIndex,
                        acceleration,
                        minimumSpeed));
            }
        }
    }

    private final class XOddAimedRoutine extends FrameCoroutine {
        private int burstIndex;

        @Override
        protected void runFrame(Boss boss, GameEngine engine) {
            fireBurst(boss, engine, burstIndex);
            burstIndex++;
            if (burstIndex < BURSTS_PER_ROUND) {
                waitFrames(BURST_WAIT_FRAMES - 1);
                return;
            }
            burstIndex = 0;
            waitFrames((densityLevel >= 3 ? 32 : 48) - 1);
        }
    }
}