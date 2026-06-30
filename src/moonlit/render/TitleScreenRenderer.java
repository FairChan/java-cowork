package moonlit.render;

import java.util.Random;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import moonlit.engine.GameConfig;
import moonlit.engine.InputController;

/**
 * JavaFX Canvas title screen adapted from the cowork cover demo.
 */
public class TitleScreenRenderer {
    private static final double START_TRANSITION_SECONDS = 2.5;
    private static final String TITLE = "FANTASY DANMAKU FESTIVAL";
    private static final String SUBTITLE = "Sakura Shrine Gate";
    private static final MenuEntry[] MENU = {
            new MenuEntry("START", "Begin the festival"),
            new MenuEntry("PRACTICE", "Warm up a route"),
            new MenuEntry("SPELL PRACTICE", "Train a pattern"),
            new MenuEntry("CHARACTER ARCHIVE", "View portraits"),
            new MenuEntry("MUSIC ROOM", "Listen again"),
            new MenuEntry("OPTIONS", "Adjust settings"),
            new MenuEntry("EXIT", "Close game")
    };

    public enum MenuAction {
        NONE,
        START,
        COMING_SOON,
        EXIT
    }

    private final Image background = AssetLoader.loadImage("assets/title/shrine_gate_background.png");
    private final Image[] marisaPoses = {
            AssetLoader.loadImage("assets/title/poses/marisa_hat.png"),
            AssetLoader.loadImage("assets/title/poses/marisa_broom.png")
    };
    private final Petal[] petals = new Petal[96];
    private final Bounds[] menuBounds = new Bounds[MENU.length];

    private int selectedIndex;
    private double time;
    private boolean startTransitionActive;
    private double startTransitionElapsed;
    private double messageSeconds;
    private MenuAction lastAction = MenuAction.NONE;

    public TitleScreenRenderer() {
        Random random = new Random(20260621L);
        for (int i = 0; i < petals.length; i++) {
            petals[i] = new Petal(random);
        }
        layoutMenuBounds();
    }

    public void update(double deltaSeconds, InputController input, Runnable startGame, Runnable requestExit) {
        time += deltaSeconds;
        if (messageSeconds > 0) {
            messageSeconds = Math.max(0, messageSeconds - deltaSeconds);
        }

        if (startTransitionActive) {
            startTransitionElapsed += deltaSeconds;
            if (startTransitionElapsed >= START_TRANSITION_SECONDS) {
                startTransitionActive = false;
                startTransitionElapsed = 0;
                startGame.run();
            }
            return;
        }

        if (input.consumeMenuExitPressed()) {
            selectedIndex = MENU.length - 1;
            lastAction = MenuAction.NONE;
            return;
        }
        if (input.consumeMenuUpPressed()) {
            selectedIndex = (selectedIndex - 1 + MENU.length) % MENU.length;
            lastAction = MenuAction.NONE;
        }
        if (input.consumeMenuDownPressed()) {
            selectedIndex = (selectedIndex + 1) % MENU.length;
            lastAction = MenuAction.NONE;
        }
        if (input.hasMousePosition()) {
            int hit = hitMenu(input.getMouseX(), input.getMouseY());
            if (hit >= 0) {
                selectedIndex = hit;
            }
        }
        if (input.consumeMenuSelectPressed() || input.consumePrimaryClick()) {
            activateSelection(requestExit);
        }
    }

    public void render(GraphicsContext graphics) {
        graphics.save();
        double width = GameConfig.WINDOW_WIDTH;
        double height = GameConfig.WINDOW_HEIGHT;
        double transition = startTransitionProgress();
        drawBackground(graphics, width, height, transition);
        drawDanmaku(graphics, width, height, transition);
        drawPetals(graphics, width, height, transition);
        drawCharacters(graphics, width, height, transition);
        drawTitle(graphics, width, transition);
        drawMenu(graphics, width, height, transition);
        drawHint(graphics, width, height, transition);
        drawStartTransition(graphics, width, height, transition);
        graphics.restore();
    }

    public int getSelectedIndexForTests() {
        return selectedIndex;
    }

    public boolean isStartTransitionActiveForTests() {
        return startTransitionActive;
    }

    public MenuAction getLastActionForTests() {
        return lastAction;
    }

    public boolean isLeftPortraitEnabledForTests() {
        return false;
    }

    private void activateSelection(Runnable requestExit) {
        if (selectedIndex == 0) {
            startTransitionActive = true;
            startTransitionElapsed = 0;
            lastAction = MenuAction.START;
            return;
        }
        if (selectedIndex == MENU.length - 1) {
            lastAction = MenuAction.EXIT;
            requestExit.run();
            return;
        }
        lastAction = MenuAction.COMING_SOON;
        messageSeconds = 1.8;
    }

    private int hitMenu(double x, double y) {
        for (int i = 0; i < menuBounds.length; i++) {
            Bounds bounds = menuBounds[i];
            if (bounds != null && bounds.contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    private void layoutMenuBounds() {
        double panelW = 360;
        double panelX = GameConfig.WINDOW_WIDTH * 0.5 - panelW * 0.5;
        double panelY = GameConfig.WINDOW_HEIGHT * 0.315;
        double lineGap = 44;
        for (int i = 0; i < MENU.length; i++) {
            double baseline = panelY + 45 + i * lineGap;
            menuBounds[i] = new Bounds(panelX + 28, baseline - 34, panelW - 56, lineGap * 0.88);
        }
    }

    private double startTransitionProgress() {
        if (!startTransitionActive) {
            return 0.0;
        }
        return ease(startTransitionElapsed / START_TRANSITION_SECONDS);
    }

    private void drawBackground(GraphicsContext graphics, double width, double height, double transition) {
        if (background != null) {
            drawImageCover(graphics, background, 0, 0, width, height, transition);
        } else {
            graphics.setFill(new LinearGradient(0, 0, 0, 1, true, null,
                    new Stop(0, Color.web("#172b47")),
                    new Stop(0.55, Color.web("#33415f")),
                    new Stop(1, Color.web("#10091d"))));
            graphics.fillRect(0, 0, width, height);
        }
        graphics.setFill(Color.web("#090a1c", 0.18 + transition * 0.30));
        graphics.fillRect(0, 0, width, height);
        graphics.setFill(Color.web("#fff0c2", 0.10 + transition * 0.16));
        graphics.fillOval(width * 0.38, height * (0.28 - transition * 0.05),
                width * (0.25 + transition * 0.18), height * (0.26 + transition * 0.18));
    }

    private void drawDanmaku(GraphicsContext graphics, double width, double height, double transition) {
        double alpha = Math.max(0, 1.0 - transition * 1.8);
        for (int ring = 0; ring < 3; ring++) {
            double centerX = width * (0.18 + ring * 0.32);
            double centerY = height * (0.32 + 0.08 * Math.sin(time + ring));
            int count = 28 + ring * 8;
            for (int i = 0; i < count; i++) {
                double angle = time * (0.35 + ring * 0.11) + i * Math.PI * 2.0 / count;
                double radius = 82 + ring * 36 + 8 * Math.sin(time * 1.7 + i);
                double x = centerX + Math.cos(angle) * radius;
                double y = centerY + Math.sin(angle) * radius;
                drawStar(graphics, x, y, 3.0 + ring, Color.web(ring % 2 == 0 ? "#ffb5da" : "#8ee7ff", alpha * 0.62));
            }
        }
    }

    private void drawPetals(GraphicsContext graphics, double width, double height, double transition) {
        double alpha = Math.max(0, 1.0 - transition * 1.6);
        graphics.setFill(Color.web("#ffc3df", alpha * 0.55));
        for (Petal petal : petals) {
            double x = (petal.x + time * petal.speedX) % 1.08;
            if (x < -0.04) {
                x += 1.08;
            }
            double y = (petal.y + time * petal.speedY) % 1.12;
            double px = x * width;
            double py = y * height;
            graphics.save();
            graphics.translate(px, py);
            graphics.rotate(petal.angle + time * petal.spin);
            graphics.fillOval(-petal.size * 1.7, -petal.size, petal.size * 3.4, petal.size * 2.0);
            graphics.restore();
        }
    }

    private void drawCharacters(GraphicsContext graphics, double width, double height, double transition) {
        double alpha = Math.max(0, 1.0 - transition * 1.65);
        Image marisa = marisaPoses[((int) ((time + 1.1) / 2.3)) % marisaPoses.length];
        graphics.setGlobalAlpha(alpha);
        if (marisa != null) {
            drawImageContain(graphics, marisa, width - 382, 118 + 10 * Math.sin(time * 1.1 + 1.7), 342, 520);
        }
        graphics.setGlobalAlpha(1.0);
    }

    private void drawTitle(GraphicsContext graphics, double width, double transition) {
        double alpha = Math.max(0, 1.0 - transition * 1.85);
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setGlobalAlpha(alpha);
        graphics.setFont(Font.font("Georgia", FontWeight.BOLD, 48));
        graphics.setStroke(Color.web("#63233f", 0.92));
        graphics.setLineWidth(4);
        graphics.strokeText(TITLE, width / 2.0, 92);
        graphics.setFill(Color.web("#fff8f1"));
        graphics.fillText(TITLE, width / 2.0, 92);
        graphics.setFont(Font.font("Georgia", FontWeight.NORMAL, 22));
        graphics.setFill(Color.web("#fff0df", 0.86));
        graphics.fillText(spaced(SUBTITLE), width / 2.0, 128);
        graphics.setGlobalAlpha(1.0);
    }

    private void drawMenu(GraphicsContext graphics, double width, double height, double transition) {
        double alpha = Math.max(0, 1.0 - transition * 1.85);
        double panelW = 360;
        double panelX = width * 0.5 - panelW * 0.5;
        double panelY = height * 0.315;
        double lineGap = 44;
        double panelH = lineGap * MENU.length + 34;
        graphics.setGlobalAlpha(alpha);
        graphics.setFill(Color.web("#111827", 0.50));
        graphics.fillRoundRect(panelX, panelY, panelW, panelH, 8, 8);
        graphics.setStroke(Color.web("#ffe9c2", 0.62));
        graphics.strokeRoundRect(panelX, panelY, panelW, panelH, 8, 8);
        graphics.setStroke(Color.web("#ff9ccb", 0.38));
        graphics.strokeLine(panelX + 22, panelY + 16, panelX + panelW - 22, panelY + 16);
        graphics.strokeLine(panelX + 22, panelY + panelH - 14, panelX + panelW - 22, panelY + panelH - 14);

        graphics.setTextAlign(TextAlignment.CENTER);
        for (int i = 0; i < MENU.length; i++) {
            double baseline = panelY + 45 + i * lineGap;
            boolean selected = i == selectedIndex;
            if (selected) {
                Bounds bounds = menuBounds[i];
                graphics.setFill(Color.web("#40d8ff", 0.18 + 0.08 * Math.sin(time * 4.4)));
                graphics.fillRect(bounds.x - 8, bounds.y + bounds.h * 0.5, bounds.w, 4);
                drawGlowDot(graphics, bounds.x + 6, bounds.y + bounds.h * 0.52, 6.5, Color.web("#ffa8e9"));
            }
            graphics.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
            graphics.setFill(selected ? Color.web("#fff9ce") : Color.web("#f4f5ee", 0.88));
            graphics.fillText(MENU[i].label, width / 2.0 + (selected ? 8 : 0), baseline);
            graphics.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            graphics.setFill(selected ? Color.web("#ffe1ec", 0.92) : Color.web("#e7eef6", 0.62));
            graphics.fillText(MENU[i].hint, width / 2.0, baseline + 16);
        }

        if (messageSeconds > 0) {
            graphics.setFont(Font.font("Georgia", FontPosture.ITALIC, 18));
            graphics.setFill(Color.web("#fff6a8", Math.min(1, messageSeconds)));
            graphics.fillText("Coming soon - connected after the main demo.", width / 2.0, panelY + panelH + 36);
        }
        graphics.setGlobalAlpha(1.0);
    }

    private void drawHint(GraphicsContext graphics, double width, double height, double transition) {
        double alpha = Math.max(0, 1.0 - transition * 1.85);
        graphics.setTextAlign(TextAlignment.RIGHT);
        graphics.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        graphics.setFill(Color.web("#f2f8ff", alpha * 0.62));
        graphics.fillText("Up/Down / Enter", width - 24, height - 20);
        graphics.setTextAlign(TextAlignment.RIGHT);
        graphics.fillText("Ordinary Magician", width - 94, height - 32);
    }

    private void drawStartTransition(GraphicsContext graphics, double width, double height, double transition) {
        if (transition <= 0) {
            return;
        }
        graphics.setFill(Color.web("#fff0c2", 0.08 + transition * 0.18));
        graphics.fillOval(width * (0.41 - transition * 0.06), height * (0.24 - transition * 0.05),
                width * (0.18 + transition * 0.28), height * (0.22 + transition * 0.34));
        if (transition > 0.62) {
            graphics.setTextAlign(TextAlignment.CENTER);
            graphics.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
            graphics.setFill(Color.web("#fff9e6", (transition - 0.62) / 0.38 * 0.78));
            graphics.fillText("Entering the shrine...", width / 2.0, height - 54);
        }
    }

    private static void drawImageCover(GraphicsContext graphics, Image image, double x, double y,
                                       double w, double h, double transition) {
        double imageW = image.getWidth();
        double imageH = image.getHeight();
        double targetRatio = w / h;
        double sourceW = imageW;
        double sourceH = imageW / targetRatio;
        if (sourceH > imageH) {
            sourceH = imageH;
            sourceW = imageH * targetRatio;
        }
        double zoom = 1.0 + transition * 1.65;
        sourceW /= zoom;
        sourceH /= zoom;
        double sourceX = clamp(imageW * 0.5 - sourceW * 0.5, 0, imageW - sourceW);
        double sourceY = clamp(imageH * (0.5 - transition * 0.10) - sourceH * 0.5, 0, imageH - sourceH);
        graphics.drawImage(image, sourceX, sourceY, sourceW, sourceH, x, y, w, h);
    }

    private static void drawImageContain(GraphicsContext graphics, Image image, double x, double y, double w, double h) {
        double imageW = image.getWidth();
        double imageH = image.getHeight();
        if (imageW <= 0 || imageH <= 0) {
            return;
        }
        double scale = Math.min(w / imageW, h / imageH);
        double drawW = imageW * scale;
        double drawH = imageH * scale;
        graphics.drawImage(image, x + (w - drawW) / 2.0, y + (h - drawH) / 2.0, drawW, drawH);
    }

    private static void drawStar(GraphicsContext graphics, double x, double y, double radius, Color color) {
        double[] xs = new double[10];
        double[] ys = new double[10];
        for (int i = 0; i < 10; i++) {
            double r = i % 2 == 0 ? radius : radius * 0.42;
            double angle = -Math.PI / 2.0 + i * Math.PI / 5.0;
            xs[i] = x + Math.cos(angle) * r;
            ys[i] = y + Math.sin(angle) * r;
        }
        graphics.setFill(color);
        graphics.fillPolygon(xs, ys, 10);
    }

    private static void drawGlowDot(GraphicsContext graphics, double x, double y, double radius, Color color) {
        graphics.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.18));
        graphics.fillOval(x - radius * 3, y - radius * 3, radius * 6, radius * 6);
        graphics.setFill(color);
        graphics.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    private static String spaced(String text) {
        return String.join(" ", text.split(""));
    }

    private static double ease(double value) {
        double t = clamp(value, 0, 1);
        return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2.0;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private record Bounds(double x, double y, double w, double h) {
        boolean contains(double px, double py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }

    private record MenuEntry(String label, String hint) {
    }

    private static final class Petal {
        final double x;
        final double y;
        final double speedX;
        final double speedY;
        final double size;
        final double angle;
        final double spin;

        Petal(Random random) {
            x = random.nextDouble() * 1.08 - 0.04;
            y = random.nextDouble() * 1.12;
            speedX = -0.035 + random.nextDouble() * 0.055;
            speedY = 0.025 + random.nextDouble() * 0.075;
            size = 2.4 + random.nextDouble() * 4.8;
            angle = random.nextDouble() * 360;
            spin = -26 + random.nextDouble() * 52;
        }
    }
}
