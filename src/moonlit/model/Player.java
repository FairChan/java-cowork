package moonlit.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import moonlit.engine.GameConfig;
import moonlit.engine.GameEngine;
import moonlit.engine.InputController;
import moonlit.render.AnimatedSpriteRenderer;
import moonlit.render.SpriteAnimation;

/**
 * Controllable shrine guardian with focused movement, shots, lives, bombs, and spell-card animation.
 */
public class Player extends Entity {
    private static final double FAST_SPEED = 300;
    private static final double FOCUS_SPEED = 155;
    private static final double SHOT_INTERVAL = 0.075;

    private int lives = 3;
    private int bombs = 3;
    private double shotCooldown;
    private double invulnerableSeconds = 1.2;
    private double spellCardSeconds;
    private boolean focused;
    private final SpriteAnimation flightAnimation =
            new SpriteAnimation("assets/sprites/player_flight.png", 93, 96, 61, 1.0 / 24.0);
    private final SpriteAnimation focusAnimation =
            new SpriteAnimation("assets/sprites/player_focus.png", 64, 64, 4, 0.12);
    private final SpriteAnimation cardAnimation =
            new SpriteAnimation("assets/sprites/player_card.png", 98, 96, 61, 1.0 / 24.0);

    public Player(double x, double y) {
        super(x, y, 14, 1);
    }

    @Override
    public void update(double deltaSeconds, GameEngine engine) {
        InputController input = engine.getInput();
        focused = input.isFocusHeld();
        flightAnimation.update(deltaSeconds);
        focusAnimation.update(deltaSeconds);
        cardAnimation.update(deltaSeconds);
        spellCardSeconds = Math.max(0, spellCardSeconds - deltaSeconds);

        double dx = (input.isMoveRight() ? 1 : 0) - (input.isMoveLeft() ? 1 : 0);
        double dy = (input.isMoveDown() ? 1 : 0) - (input.isMoveUp() ? 1 : 0);
        double length = Math.hypot(dx, dy);
        if (length > 0) {
            dx /= length;
            dy /= length;
        }

        double speed = focused ? FOCUS_SPEED : FAST_SPEED;
        x = clamp(x + dx * speed * deltaSeconds, GameConfig.PLAYFIELD_X + 18, GameConfig.PLAYFIELD_RIGHT - 18);
        y = clamp(y + dy * speed * deltaSeconds, GameConfig.PLAYFIELD_Y + 32, GameConfig.PLAYFIELD_BOTTOM - 18);

        shotCooldown = Math.max(0, shotCooldown - deltaSeconds);
        if (input.isShootHeld() && shotCooldown <= 0) {
            engine.addPlayerShot(new PlayerShot(x - 9, y - 16));
            engine.addPlayerShot(new PlayerShot(x + 9, y - 16));
            shotCooldown = SHOT_INTERVAL;
        }

        invulnerableSeconds = Math.max(0, invulnerableSeconds - deltaSeconds);
    }

    @Override
    public void render(GraphicsContext graphics) {
        double alpha = invulnerableSeconds > 0 && ((int) (invulnerableSeconds * 14) % 2 == 0) ? 0.42 : 1.0;
        graphics.setGlobalAlpha(alpha);

        SpriteAnimation activeAnimation = isSpellCardActive()
                ? cardAnimation
                : focused ? focusAnimation : flightAnimation;
        if (activeAnimation.isAvailable()) {
            double scale = isSpellCardActive() ? 0.76 : (focused ? 1.0 : 0.72);
            AnimatedSpriteRenderer.drawCentered(graphics, activeAnimation, x, y, scale);
            renderFocusMarker(graphics);
            graphics.setGlobalAlpha(1.0);
            return;
        }

        graphics.setFill(Color.web("#f7f2ff"));
        graphics.fillOval(x - 10, y - 18, 20, 24);
        graphics.setFill(Color.web("#d91f5c"));
        graphics.fillPolygon(new double[] {x, x - 18, x + 18}, new double[] {y - 2, y + 23, y + 23}, 3);
        graphics.setFill(Color.web("#fff6a8"));
        graphics.fillPolygon(new double[] {x, x - 8, x + 8}, new double[] {y - 28, y - 12, y - 12}, 3);
        graphics.setStroke(Color.web("#ffb3d1"));
        graphics.setLineWidth(3);
        graphics.strokeLine(x - 22, y - 4, x - 8, y + 4);
        graphics.strokeLine(x + 22, y - 4, x + 8, y + 4);

        renderFocusMarker(graphics);

        graphics.setGlobalAlpha(1.0);
    }

    public boolean useBomb() {
        if (bombs <= 0) {
            return false;
        }
        bombs--;
        invulnerableSeconds = Math.max(invulnerableSeconds, 1.35);
        return true;
    }

    public void startSpellCard(double durationSeconds) {
        spellCardSeconds = Math.max(spellCardSeconds, durationSeconds);
    }

    public boolean isSpellCardActive() {
        return spellCardSeconds > 0;
    }

    public void hit() {
        if (!canBeHit()) {
            return;
        }
        lives--;
        invulnerableSeconds = 2.0;
        x = GameConfig.PLAYFIELD_X + GameConfig.PLAYFIELD_WIDTH / 2.0;
        y = GameConfig.PLAYFIELD_BOTTOM - 80;
    }

    public boolean canBeHit() {
        return invulnerableSeconds <= 0;
    }

    public double getHitRadius() {
        return 4.0;
    }

    public int getLives() {
        return lives;
    }

    public int getBombs() {
        return bombs;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setInvulnerableSeconds(double invulnerableSeconds) {
        this.invulnerableSeconds = Math.max(0, invulnerableSeconds);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void renderFocusMarker(GraphicsContext graphics) {
        if (!focused) {
            return;
        }
        graphics.setStroke(Color.web("#b9f8ff"));
        graphics.setLineWidth(1.5);
        graphics.strokeOval(x - 11, y - 11, 22, 22);
        graphics.setFill(Color.web("#ffffff"));
        graphics.fillOval(x - 3, y - 3, 6, 6);
    }
}