package moonlit.model;

import javafx.scene.canvas.GraphicsContext;
import moonlit.engine.GameEngine;

/**
 * Base class for anything that updates and draws inside the playfield.
 */
public abstract class GameObject {
    protected double x;
    protected double y;
    protected double radius;
    protected boolean alive = true;

    protected GameObject(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public abstract void update(double deltaSeconds, GameEngine engine);

    public abstract void render(GraphicsContext graphics);

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return radius;
    }

    public boolean isAlive() {
        return alive;
    }

    public void destroy() {
        alive = false;
    }
}
