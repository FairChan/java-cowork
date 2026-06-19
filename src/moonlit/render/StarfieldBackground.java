package moonlit.render;

import java.util.Random;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import moonlit.engine.GameConfig;

/**
 * Animated night-sky shrine background drawn entirely with JavaFX primitives.
 */
public class StarfieldBackground {
    private static final int STAR_COUNT = 96;
    private final Star[] stars = new Star[STAR_COUNT];
    private final Image stageImage = AssetLoader.loadImage("assets/backgrounds/stage1_moonlit_shrine.png");
    private double time;

    public StarfieldBackground() {
        Random random = new Random(13);
        for (int i = 0; i < stars.length; i++) {
            stars[i] = new Star(
                    GameConfig.PLAYFIELD_X + random.nextDouble() * GameConfig.PLAYFIELD_WIDTH,
                    GameConfig.PLAYFIELD_Y + random.nextDouble() * GameConfig.PLAYFIELD_HEIGHT,
                    18 + random.nextDouble() * 46,
                    1 + random.nextDouble() * 2.2,
                    0.35 + random.nextDouble() * 0.55);
        }
    }

    public void update(double deltaSeconds) {
        time += deltaSeconds;
        for (Star star : stars) {
            star.y += star.speed * deltaSeconds;
            if (star.y > GameConfig.PLAYFIELD_BOTTOM) {
                star.y = GameConfig.PLAYFIELD_Y;
            }
        }
    }

    public void render(GraphicsContext graphics) {
        if (stageImage != null) {
            graphics.drawImage(stageImage, GameConfig.PLAYFIELD_X, GameConfig.PLAYFIELD_Y,
                    GameConfig.PLAYFIELD_WIDTH, GameConfig.PLAYFIELD_HEIGHT);
            renderStars(graphics);
            return;
        }

        graphics.setFill(Color.web("#090a1d"));
        graphics.fillRect(GameConfig.PLAYFIELD_X, GameConfig.PLAYFIELD_Y,
                GameConfig.PLAYFIELD_WIDTH, GameConfig.PLAYFIELD_HEIGHT);

        graphics.setFill(Color.web("#11194a", 0.55));
        graphics.fillOval(GameConfig.PLAYFIELD_X + 70, 80, 480, 260);

        graphics.setFill(Color.web("#f7e9a5", 0.9));
        graphics.fillOval(GameConfig.PLAYFIELD_RIGHT - 140, 58, 68, 68);
        graphics.setFill(Color.web("#090a1d", 0.88));
        graphics.fillOval(GameConfig.PLAYFIELD_RIGHT - 118, 48, 68, 68);

        renderStars(graphics);

        drawShrineSilhouette(graphics);
    }

    private void renderStars(GraphicsContext graphics) {
        for (Star star : stars) {
            double twinkle = 0.55 + Math.sin(time * 2.5 + star.x) * 0.18;
            graphics.setFill(Color.web("#d7e7ff", star.alpha * twinkle));
            graphics.fillOval(star.x, star.y, star.size, star.size);
        }
    }

    private static void drawShrineSilhouette(GraphicsContext graphics) {
        double baseY = GameConfig.PLAYFIELD_BOTTOM - 16;
        graphics.setFill(Color.web("#161229"));
        graphics.fillRect(GameConfig.PLAYFIELD_X + 70, baseY - 32, 470, 34);
        graphics.fillPolygon(
                new double[] {GameConfig.PLAYFIELD_X + 45, GameConfig.PLAYFIELD_X + 310, GameConfig.PLAYFIELD_X + 575},
                new double[] {baseY - 31, baseY - 88, baseY - 31},
                3);
        graphics.setFill(Color.web("#251d3a"));
        graphics.fillRect(GameConfig.PLAYFIELD_X + 175, baseY - 72, 18, 72);
        graphics.fillRect(GameConfig.PLAYFIELD_X + 430, baseY - 72, 18, 72);
    }

    private static final class Star {
        private final double x;
        private double y;
        private final double speed;
        private final double size;
        private final double alpha;

        private Star(double x, double y, double speed, double size, double alpha) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.size = size;
            this.alpha = alpha;
        }
    }
}
