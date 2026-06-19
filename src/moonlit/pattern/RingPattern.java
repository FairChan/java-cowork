package moonlit.pattern;

import moonlit.engine.GameEngine;
import moonlit.model.Boss;
import moonlit.model.EnemyBullet;

/**
 * Slow radial rings used as the readable opening spell.
 */
public class RingPattern implements BulletPattern {
    private double timer;
    private double angleOffset;

    @Override
    public void update(Boss boss, GameEngine engine, double deltaSeconds) {
        timer -= deltaSeconds;
        if (timer > 0) {
            return;
        }
        timer = 1.0;
        angleOffset += Math.PI / 18.0;
        int count = 28;
        for (int i = 0; i < count; i++) {
            double angle = angleOffset + (Math.PI * 2 * i / count);
            double speed = 118;
            engine.addEnemyBullet(new EnemyBullet(
                    boss.getX(), boss.getY(),
                    Math.cos(angle) * speed,
                    Math.sin(angle) * speed,
                    6.5,
                    i));
        }
    }

    @Override
    public void reset() {
        timer = 0;
        angleOffset = 0;
    }
}
