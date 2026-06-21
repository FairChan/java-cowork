package moonlit.model;

import moonlit.engine.GameConfig;
import moonlit.engine.GameEngine;

/**
 * Star bullet that reflects from side walls and splits once on bounce.
 */
public class BouncingEnemyBullet extends EnemyBullet {
    private int bouncesLeft;
    private boolean splitUsed;

    public BouncingEnemyBullet(double x, double y, double velocityX, double velocityY, double radius, int variant,
            int bouncesLeft) {
        super(x, y, velocityX, velocityY, radius, variant);
        this.bouncesLeft = bouncesLeft;
    }

    @Override
    public void update(double deltaSeconds, GameEngine engine) {
        x += velocityX * deltaSeconds;
        y += velocityY * deltaSeconds;
        if ((x <= GameConfig.PLAYFIELD_X + radius && velocityX < 0)
                || (x >= GameConfig.PLAYFIELD_RIGHT - radius && velocityX > 0)) {
            x = Math.max(GameConfig.PLAYFIELD_X + radius, Math.min(GameConfig.PLAYFIELD_RIGHT - radius, x));
            velocityX = -velocityX;
            bouncesLeft--;
            split(engine);
        }
        if (bouncesLeft < 0 || y < GameConfig.PLAYFIELD_Y - 120 || y > GameConfig.PLAYFIELD_BOTTOM + 120) {
            alive = false;
        }
    }

    private void split(GameEngine engine) {
        if (splitUsed) {
            return;
        }
        splitUsed = true;
        double base = Math.atan2(velocityY, velocityX);
        double speed = Math.hypot(velocityX, velocityY) + 55;
        for (int i = -1; i <= 1; i++) {
            double angle = base + i * 0.28;
            engine.addEnemyBullet(EnemyBullet.acceleratingGlow(x, y,
                    Math.cos(angle) * speed, Math.sin(angle) * speed,
                    Math.max(3.5, radius * 0.62), 9 + i, -55, 80));
        }
    }
}
