package moonlit.pattern;

import moonlit.engine.GameEngine;
import moonlit.model.Boss;
import moonlit.model.EnemyBullet;

/**
 * Rotating four-arm spiral that creates lanes for focused movement.
 */
public class SpiralPattern implements BulletPattern {
    private double timer;
    private double spin;

    @Override
    public void update(Boss boss, GameEngine engine, double deltaSeconds) {
        timer -= deltaSeconds;
        spin += deltaSeconds * 2.2;
        if (timer > 0) {
            return;
        }
        timer = 0.085;
        for (int i = 0; i < 4; i++) {
            double angle = spin + i * Math.PI / 2.0;
            double speed = i % 2 == 0 ? 126 : 150;
            engine.addEnemyBullet(new EnemyBullet(
                    boss.getX(), boss.getY(),
                    Math.cos(angle) * speed,
                    Math.sin(angle) * speed,
                    5.8,
                    i + 1));
        }
    }

    @Override
    public void reset() {
        timer = 0;
        spin = 0;
    }
}
