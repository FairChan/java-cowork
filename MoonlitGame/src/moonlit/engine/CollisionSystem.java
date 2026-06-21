package moonlit.engine;

import javafx.scene.paint.Color;
import moonlit.model.Boss;
import moonlit.model.Enemy;
import moonlit.model.EnemyBullet;
import moonlit.model.Player;
import moonlit.model.PlayerShot;

/**
 * Handles gameplay collisions without owning any game objects.
 */
public class CollisionSystem {
    public void resolve(GameEngine engine) {
        Player player = engine.getPlayer();
        Boss boss = engine.getBoss();

        for (PlayerShot shot : engine.getPlayerShots()) {
            if (!shot.isAlive()) {
                continue;
            }
            for (Enemy enemy : engine.getEnemies()) {
                if (enemy.isAlive() && overlaps(shot.getX(), shot.getY(), shot.getRadius(),
                        enemy.getX(), enemy.getY(), enemy.getRadius())) {
                    shot.destroy();
                    enemy.takeDamage(shot.getDamage());
                    engine.getParticles().spawnSpark(shot.getX(), shot.getY(), Color.web("#9df7ff"));
                    if (!enemy.isAlive()) {
                        engine.addScore(enemy.getScoreValue());
                        engine.dropEnemyRewards(enemy);
                        engine.getParticles().spawnBurst(enemy.getX(), enemy.getY(), Color.web("#ffd175"), 18);
                    }
                    break;
                }
            }
            if (shot.isAlive() && engine.isBossActive() && boss != null && boss.isAlive()
                    && overlaps(shot.getX(), shot.getY(), shot.getRadius(),
                    boss.getX(), boss.getY(), boss.getRadius())) {
                shot.destroy();
                boss.takeDamage(shot.getDamage());
                engine.addScore(90);
                engine.getParticles().spawnSpark(shot.getX(), shot.getY(), Color.web("#9df7ff"));
            }
        }

        for (EnemyBullet bullet : engine.getEnemyBullets()) {
            if (!bullet.isAlive()) {
                continue;
            }
            double dx = bullet.getX() - player.getX();
            double dy = bullet.getY() - player.getY();
            double distanceSquared = dx * dx + dy * dy;
            double hitDistance = bullet.getRadius() + player.getHitRadius();
            if (distanceSquared <= hitDistance * hitDistance && player.canBeHit() && !engine.isInvincibleMode()) {
                bullet.destroy();
                player.hit();
                engine.notifyPlayerDamaged();
                engine.getParticles().spawnBurst(player.getX(), player.getY(), Color.web("#ff709c"), 32);
                continue;
            }

            double grazeDistance = bullet.getRadius() + 27;
            if (!bullet.isGrazed() && distanceSquared <= grazeDistance * grazeDistance) {
                bullet.markGrazed();
                engine.addGrazeScore();
                engine.getParticles().spawnSpark(player.getX(), player.getY(), Color.web("#fff6a8"));
            }
        }
    }

    private static boolean overlaps(double ax, double ay, double ar, double bx, double by, double br) {
        double dx = ax - bx;
        double dy = ay - by;
        double range = ar + br;
        return dx * dx + dy * dy <= range * range;
    }
}

