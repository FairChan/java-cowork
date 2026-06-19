package moonlit.model;

import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import moonlit.engine.GameConfig;
import moonlit.engine.GameEngine;
import moonlit.pattern.AimedFanPattern;
import moonlit.pattern.BulletPattern;
import moonlit.pattern.RingPattern;
import moonlit.pattern.SpiralPattern;
import moonlit.render.AnimatedSpriteRenderer;
import moonlit.render.SpriteAnimation;

/**
 * One-stage boss with three spell-card-like attack phases.
 */
public class Boss extends Entity {
    private final List<BulletPattern> patterns = List.of(
            new RingPattern(),
            new SpiralPattern(),
            new AimedFanPattern());
    private int phaseIndex;
    private double movementTime;
    private final SpriteAnimation animation =
            new SpriteAnimation("assets/sprites/boss_moon_spirit.png", 96, 96, 6, 0.14);

    public Boss(double x, double y) {
        super(x, y, 30, 900);
    }

    @Override
    public void update(double deltaSeconds, GameEngine engine) {
        if (!alive) {
            return;
        }
        animation.update(deltaSeconds);
        movementTime += deltaSeconds;
        x = GameConfig.PLAYFIELD_X + GameConfig.PLAYFIELD_WIDTH / 2.0 + Math.sin(movementTime * 0.8) * 155;
        y = 116 + Math.sin(movementTime * 1.5) * 18;

        int previousPhase = phaseIndex;
        updatePhase();
        if (phaseIndex != previousPhase) {
            patterns.get(phaseIndex).reset();
            engine.getParticles().spawnRing(x, y, Color.web("#fff6a8"), 36);
        }
        patterns.get(phaseIndex).update(this, engine, deltaSeconds);
    }

    @Override
    public void render(GraphicsContext graphics) {
        if (animation.isAvailable()) {
            AnimatedSpriteRenderer.drawCentered(graphics, animation, x, y, 1.0);
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
        super.takeDamage(damage);
        updatePhase();
    }

    public int getPhaseIndex() {
        return phaseIndex;
    }

    public String getPhaseName() {
        return switch (phaseIndex) {
            case 1 -> "Spell 2: Moon Spiral";
            case 2 -> "Spell 3: Homing Petals";
            default -> "Spell 1: Lantern Ring";
        };
    }

    private void updatePhase() {
        if (hp <= 300) {
            phaseIndex = 2;
        } else if (hp <= 600) {
            phaseIndex = 1;
        } else {
            phaseIndex = 0;
        }
    }
}
