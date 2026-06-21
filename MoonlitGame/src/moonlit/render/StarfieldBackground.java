package moonlit.render;

import java.util.Random;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import moonlit.engine.GameConfig;

/**
 * Four-wave scrolling stage background with retained star overlay and cloud transitions.
 */
public class StarfieldBackground {
    private static final int STAR_COUNT = 96;
    private static final int CLOUD_COUNT = 30;
    private static final double TRANSITION_SECONDS = 1.2;
    private static final double STAGE_SCROLL_SPEED = 420;
    private static final double BOSS_SCROLL_SPEED = 34;

    private final Star[] stars = new Star[STAR_COUNT];
    private final Cloud[] clouds = new Cloud[CLOUD_COUNT];
    private final Image[] waveImages = new Image[] {
            AssetLoader.loadImage("assets/backgrounds/starry_wave1.png"),
            AssetLoader.loadImage("assets/backgrounds/starry_wave2.png"),
            AssetLoader.loadImage("assets/backgrounds/starry_wave3.png"),
            AssetLoader.loadImage("assets/backgrounds/starry_wave4.png")
    };
    private final Image shrinePathImage = AssetLoader.loadImage("assets/backgrounds/moonlit_shrine_path.png");
    private final Image fallbackImage = AssetLoader.loadImage("assets/backgrounds/stage1_moonlit_shrine.png");
    private double time;
    private double scrollOffset;
    private double cloudTransitionSeconds;
    private boolean bossMode;
    private int currentWaveIndex = 1;

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
        Random cloudRandom = new Random(20260621L);
        for (int i = 0; i < clouds.length; i++) {
            clouds[i] = new Cloud(
                    GameConfig.PLAYFIELD_X - 80 + cloudRandom.nextDouble() * (GameConfig.PLAYFIELD_WIDTH + 160),
                    GameConfig.PLAYFIELD_Y - 80 + cloudRandom.nextDouble() * (GameConfig.PLAYFIELD_HEIGHT + 160),
                    54 + cloudRandom.nextDouble() * 116,
                    0.32 + cloudRandom.nextDouble() * 0.5,
                    16 + cloudRandom.nextDouble() * 48);
        }
    }

    public void update(double deltaSeconds) {
        time += deltaSeconds;
        double scrollSpeed = bossMode ? BOSS_SCROLL_SPEED : STAGE_SCROLL_SPEED;
        scrollOffset = positiveModulo(scrollOffset + scrollSpeed * deltaSeconds, getScaledTileHeight(currentImage()));
        cloudTransitionSeconds = Math.max(0, cloudTransitionSeconds - deltaSeconds);
        for (Star star : stars) {
            star.y += star.speed * (bossMode ? 0.7 : 5.6) * deltaSeconds;
            if (star.y > GameConfig.PLAYFIELD_BOTTOM) {
                star.y = GameConfig.PLAYFIELD_Y;
            }
        }
    }

    public void requestWaveBackground(int waveIndex) {
        int clamped = Math.max(1, Math.min(waveImages.length, waveIndex));
        if (currentWaveIndex == clamped) {
            return;
        }
        currentWaveIndex = clamped;
        scrollOffset = 0;
        cloudTransitionSeconds = TRANSITION_SECONDS;
    }

    public void setBossMode(boolean bossMode) {
        this.bossMode = bossMode;
        if (bossMode) {
            requestWaveBackground(4);
        }
    }

    public int getCurrentWaveIndexForTests() {
        return currentWaveIndex;
    }

    public boolean isCloudTransitionActiveForTests() {
        return cloudTransitionSeconds > 0;
    }

    public void render(GraphicsContext graphics) {
        if (shrinePathImage != null) {
            renderCoverImage(graphics, shrinePathImage);
            renderShrineMoodOverlay(graphics);
        } else {
            Image image = currentImage();
            if (image != null) {
                renderScrollingImage(graphics, image);
            } else if (fallbackImage != null) {
                graphics.drawImage(fallbackImage, GameConfig.PLAYFIELD_X, GameConfig.PLAYFIELD_Y,
                        GameConfig.PLAYFIELD_WIDTH, GameConfig.PLAYFIELD_HEIGHT);
            } else {
                renderFallbackPrimitive(graphics);
            }
        }

        if (bossMode) {
            renderBossAtmosphere(graphics);
        }
        renderStars(graphics);
        renderCloudTransition(graphics);
    }

    private void renderCoverImage(GraphicsContext graphics, Image image) {
        double sourceAspect = image.getWidth() / image.getHeight();
        double targetAspect = GameConfig.PLAYFIELD_WIDTH / GameConfig.PLAYFIELD_HEIGHT;
        double sx = 0;
        double sy = 0;
        double sw = image.getWidth();
        double sh = image.getHeight();

        if (sourceAspect > targetAspect) {
            sw = image.getHeight() * targetAspect;
            sx = (image.getWidth() - sw) / 2.0;
        } else {
            sh = image.getWidth() / targetAspect;
            sy = (image.getHeight() - sh) / 2.0;
        }

        graphics.drawImage(image, sx, sy, sw, sh,
                GameConfig.PLAYFIELD_X, GameConfig.PLAYFIELD_Y,
                GameConfig.PLAYFIELD_WIDTH, GameConfig.PLAYFIELD_HEIGHT);
    }

    private void renderShrineMoodOverlay(GraphicsContext graphics) {
        graphics.setFill(Color.web("#050416", 0.16));
        graphics.fillRect(GameConfig.PLAYFIELD_X, GameConfig.PLAYFIELD_Y,
                GameConfig.PLAYFIELD_WIDTH, GameConfig.PLAYFIELD_HEIGHT);
        graphics.setFill(Color.web("#1e0f3e", 0.18));
        graphics.fillRect(GameConfig.PLAYFIELD_X, GameConfig.PLAYFIELD_Y,
                GameConfig.PLAYFIELD_WIDTH, GameConfig.PLAYFIELD_HEIGHT);
    }

    private Image currentImage() {
        Image image = waveImages[currentWaveIndex - 1];
        return image != null ? image : fallbackImage;
    }

    private void renderScrollingImage(GraphicsContext graphics, Image image) {
        double scale = GameConfig.PLAYFIELD_WIDTH / image.getWidth();
        double scaledHeight = image.getHeight() * scale;
        double offset = positiveModulo(scrollOffset, scaledHeight);
        double firstY = GameConfig.PLAYFIELD_Y + offset - scaledHeight;
        for (double y = firstY; y < GameConfig.PLAYFIELD_BOTTOM; y += scaledHeight) {
            graphics.drawImage(image,
                    GameConfig.PLAYFIELD_X, y,
                    GameConfig.PLAYFIELD_WIDTH, scaledHeight);
        }
    }

    private double getScaledTileHeight(Image image) {
        if (image == null || image.getWidth() <= 0) {
            return GameConfig.PLAYFIELD_HEIGHT;
        }
        return image.getHeight() * (GameConfig.PLAYFIELD_WIDTH / image.getWidth());
    }

    private void renderFallbackPrimitive(GraphicsContext graphics) {
        graphics.setFill(Color.web("#090a1d"));
        graphics.fillRect(GameConfig.PLAYFIELD_X, GameConfig.PLAYFIELD_Y,
                GameConfig.PLAYFIELD_WIDTH, GameConfig.PLAYFIELD_HEIGHT);
        graphics.setFill(Color.web("#11194a", 0.55));
        graphics.fillOval(GameConfig.PLAYFIELD_X + 70, 80, 480, 260);
        drawShrineSilhouette(graphics);
    }

    private void renderBossAtmosphere(GraphicsContext graphics) {
        graphics.setFill(Color.web("#050512", 0.28));
        graphics.fillRect(GameConfig.PLAYFIELD_X, GameConfig.PLAYFIELD_Y,
                GameConfig.PLAYFIELD_WIDTH, GameConfig.PLAYFIELD_HEIGHT);
        BlendMode oldBlend = graphics.getGlobalBlendMode();
        double oldAlpha = graphics.getGlobalAlpha();
        graphics.setGlobalBlendMode(BlendMode.ADD);
        graphics.setGlobalAlpha(0.22 + Math.sin(time * 1.4) * 0.05);
        graphics.setStroke(Color.web("#7d5dff"));
        graphics.setLineWidth(3);
        graphics.strokeOval(GameConfig.PLAYFIELD_X + 98, GameConfig.PLAYFIELD_Y + 74, 424, 424);
        graphics.strokeOval(GameConfig.PLAYFIELD_X + 172, GameConfig.PLAYFIELD_Y + 148, 276, 276);
        graphics.setGlobalAlpha(oldAlpha);
        graphics.setGlobalBlendMode(oldBlend);
    }

    private void renderStars(GraphicsContext graphics) {
        for (Star star : stars) {
            double twinkle = 0.55 + Math.sin(time * 2.5 + star.x) * 0.18;
            graphics.setFill(Color.web("#d7e7ff", star.alpha * twinkle));
            graphics.fillOval(star.x, star.y, star.size, star.size);
        }
    }

    private void renderCloudTransition(GraphicsContext graphics) {
        if (cloudTransitionSeconds <= 0) {
            return;
        }
        double progress = 1.0 - cloudTransitionSeconds / TRANSITION_SECONDS;
        double alpha = progress < 0.5 ? progress * 2.0 : (1.0 - progress) * 2.0;
        double oldAlpha = graphics.getGlobalAlpha();
        BlendMode oldBlend = graphics.getGlobalBlendMode();
        graphics.setGlobalBlendMode(BlendMode.SRC_OVER);
        for (int i = 0; i < clouds.length; i++) {
            Cloud cloud = clouds[i];
            double drift = (progress - 0.5) * cloud.drift;
            double pulse = 0.86 + Math.sin(time * 2.0 + i) * 0.08;
            graphics.setGlobalAlpha(alpha * cloud.alpha * pulse);
            graphics.setFill(Color.web("#dbe7ff"));
            graphics.fillOval(cloud.x + drift, cloud.y, cloud.size * 1.7, cloud.size * 0.82);
            graphics.setGlobalAlpha(alpha * cloud.alpha * 0.55 * pulse);
            graphics.setFill(Color.web("#ffffff"));
            graphics.fillOval(cloud.x + drift + cloud.size * 0.34, cloud.y - cloud.size * 0.18,
                    cloud.size * 1.15, cloud.size * 0.68);
        }
        graphics.setGlobalAlpha(oldAlpha);
        graphics.setGlobalBlendMode(oldBlend);
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

    private static double positiveModulo(double value, double divisor) {
        if (divisor <= 0) {
            return 0;
        }
        double result = value % divisor;
        return result < 0 ? result + divisor : result;
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

    private static final class Cloud {
        private final double x;
        private final double y;
        private final double size;
        private final double alpha;
        private final double drift;

        private Cloud(double x, double y, double size, double alpha, double drift) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.alpha = alpha;
            this.drift = drift;
        }
    }
}
