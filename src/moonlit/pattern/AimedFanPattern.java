package moonlit.pattern;

import moonlit.engine.GameEngine;
import moonlit.model.Boss;
import moonlit.model.EnemyBullet;
import moonlit.model.Player;

/**
 * Late-stage aimed fan volleys that ask the player to dodge deliberately.
 */
public class AimedFanPattern implements BulletPattern {
    private double timer;
    private double ringTimer;

    @Override
    public void update(Boss boss, GameEngine engine, double deltaSeconds) {
        timer -= deltaSeconds;
        ringTimer -= deltaSeconds;
        Player player = engine.getPlayer();

        if (timer <= 0) {
            timer = 0.48;
            double base = Math.atan2(player.getY() - boss.getY(), player.getX() - boss.getX());
            int count = 9;
            for (int i = 0; i < count; i++) {
                double angle = base + (i - (count - 1) / 2.0) * 0.105;
                double speed = 162 + Math.abs(i - 4) * 8;
                engine.addEnemyBullet(new EnemyBullet(
                        boss.getX(), boss.getY(),
                        Math.cos(angle) * speed,
                        Math.sin(angle) * speed,
                        6.2,
                        2));
            }
        }

        if (ringTimer <= 0) {
            ringTimer = 1.3;
            for (int i = 0; i < 18; i++) {
                double angle = Math.PI * 2 * i / 18.0;
                engine.addEnemyBullet(new EnemyBullet(
                        boss.getX(), boss.getY(),
                        Math.cos(angle) * 105,
                        Math.sin(angle) * 105,
                        5.4,
                        i + 3));
            }
        }
    }

    @Override
    public void reset() {
        timer = 0;
        ringTimer = 0.6;
    }
}
