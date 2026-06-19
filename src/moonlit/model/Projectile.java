package moonlit.model;

import moonlit.engine.GameConfig;
import moonlit.engine.GameEngine;

/**
 * Moving object with velocity and automatic out-of-bounds cleanup.
 */
public abstract class Projectile extends GameObject {
    protected double velocityX;
    protected double velocityY;

    protected Projectile(double x, double y, double radius, double velocityX, double velocityY) {
        super(x, y, radius);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    @Override
    public void update(double deltaSeconds, GameEngine engine) {
        x += velocityX * deltaSeconds;
        y += velocityY * deltaSeconds;
        if (x < GameConfig.PLAYFIELD_X - 80 || x > GameConfig.PLAYFIELD_RIGHT + 80
                || y < GameConfig.PLAYFIELD_Y - 100 || y > GameConfig.PLAYFIELD_BOTTOM + 100) {
            alive = false;
        }
    }
}
