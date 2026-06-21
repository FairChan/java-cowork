package moonlit.stage;

import moonlit.engine.GameConfig;
import moonlit.engine.GameEngine;
import moonlit.model.Enemy;

/**
 * A timed batch of enemies in Stage 1.
 */
public class EnemyWave {
    private final double triggerTime;
    private final Enemy.Kind kind;
    private final int count;
    private final boolean fromLeft;
    private final double speedX;
    private final double speedY;
    private boolean spawned;

    public EnemyWave(double triggerTime, Enemy.Kind kind, int count, boolean fromLeft, double speedX, double speedY) {
        this.triggerTime = triggerTime;
        this.kind = kind;
        this.count = count;
        this.fromLeft = fromLeft;
        this.speedX = speedX;
        this.speedY = speedY;
    }

    public boolean shouldSpawn(double elapsedSeconds) {
        return !spawned && elapsedSeconds >= triggerTime;
    }

    public void spawn(GameEngine engine) {
        spawned = true;
        double startX = fromLeft ? GameConfig.PLAYFIELD_X - 32 : GameConfig.PLAYFIELD_RIGHT + 32;
        double direction = fromLeft ? 1 : -1;
        for (int i = 0; i < count; i++) {
            double y = GameConfig.PLAYFIELD_Y + 72 + i * 46;
            double vx = direction * speedX;
            Enemy enemy = kind == Enemy.Kind.LANTERN
                    ? Enemy.lantern(startX, y, vx, speedY)
                    : Enemy.charmFairy(startX, y, vx, speedY);
            engine.addEnemy(enemy);
        }
    }

    public void reset() {
        spawned = false;
    }
}
