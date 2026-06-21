package moonlit.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import moonlit.engine.GameConfig;
import moonlit.engine.GameEngine;
import moonlit.engine.GameEngine.PortraitMood;
import moonlit.model.Boss;
import moonlit.model.Player;

/**
 * Draws the framed shrine UI around the playfield.
 */
public class HudRenderer {
    private static final Color PANEL = Color.web("#100b2a", 0.90);
    private static final Color PANEL_DEEP = Color.web("#070414", 0.82);
    private static final Color GOLD = Color.web("#ffd987");
    private static final Color ROSE = Color.web("#ff8fbd");
    private static final Color CYAN = Color.web("#8ee7ff");
    private static final Color LILAC = Color.web("#bda5ff");
    private static final Color TEXT = Color.web("#fff7e8");
    private static final Color MUTED = Color.web("#c9c8f6");

    private final Image normalPortrait = AssetLoader.loadImage("assets/portraits/reimu_normal.png");
    private final Image hurtPortrait = AssetLoader.loadImage("assets/portraits/reimu_hurt.png");
    private final Image cheerPortrait = AssetLoader.loadImage("assets/portraits/reimu_cheer.png");

    public void render(GraphicsContext graphics, GameEngine engine) {
        graphics.save();
        renderLeftPanel(graphics, engine);
        renderRightPanel(graphics, engine);
        graphics.restore();
    }

    public static Font titleFont() {
        return Font.font("Georgia", FontWeight.BOLD, 38);
    }

    public static Font largeFont() {
        return Font.font("Georgia", FontWeight.BOLD, 22);
    }

    public static Font bodyFont() {
        return Font.font("Consolas", FontWeight.NORMAL, 15);
    }

    public static Font labelFont() {
        return Font.font("Georgia", FontWeight.NORMAL, 14);
    }

    public static Font scoreFont() {
        return Font.font("Consolas", FontWeight.BOLD, 19);
    }

    public static Font smallSerifFont() {
        return Font.font("Georgia", FontPosture.ITALIC, 13);
    }

    private void renderLeftPanel(GraphicsContext graphics, GameEngine engine) {
        Player player = engine.getPlayer();
        double x = GameConfig.LEFT_HUD_X;
        double y = GameConfig.PLAYFIELD_Y;
        double w = GameConfig.LEFT_HUD_WIDTH;
        double h = GameConfig.PLAYFIELD_HEIGHT;
        double contentX = x + 18;
        double contentW = w - 36;

        drawPanel(graphics, x, y, w, h);
        drawLeftTitle(graphics, x, y, w);

        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setFill(MUTED);
        graphics.setFont(labelFont());
        graphics.fillText("Score", x + w / 2.0, y + 158);
        graphics.setFill(TEXT);
        graphics.setFont(Font.font("Consolas", FontWeight.BOLD, 30));
        graphics.fillText(String.format("%08d", engine.getScore()), x + w / 2.0, y + 196);
        drawDivider(graphics, contentX, y + 219, contentW);

        graphics.setTextAlign(TextAlignment.LEFT);
        drawResourceRow(graphics, contentX, y + 256, "Lives", player.getLives(), ROSE, true);
        drawResourceRow(graphics, contentX, y + 296, "Bombs", player.getBombs(), CYAN, false);
        drawDivider(graphics, contentX, y + 318, contentW);

        drawPortraitFrame(graphics, x + 14, y + 340, w - 28, h - 360, engine.getPortraitMood());
    }

    private void renderRightPanel(GraphicsContext graphics, GameEngine engine) {
        Boss boss = engine.getBoss();
        double x = GameConfig.HUD_X;
        double y = GameConfig.PLAYFIELD_Y;
        double w = GameConfig.HUD_WIDTH;
        double h = GameConfig.PLAYFIELD_HEIGHT;
        double contentX = x + 18;
        double contentW = w - 36;

        drawPanel(graphics, x, y, w, h);
        drawStageBanner(graphics, x + 14, y + 28, w - 28, "Stage " + engine.getCurrentStageNumber());

        drawStat(graphics, contentX, y + 134, contentX + contentW, GOLD,
                "Graze", String.valueOf(engine.getGrazeCount()));
        drawStat(graphics, contentX, y + 174, contentX + contentW, ROSE,
                "Power", String.format("%.2f", engine.getPower()));
        drawStat(graphics, contentX, y + 214, contentX + contentW, CYAN,
                "B Frag", engine.getBombFragments() + "/3");
        drawStat(graphics, contentX, y + 254, contentX + contentW, LILAC,
                "L Frag", engine.getLifeFragments() + "/3");
        drawStat(graphics, contentX, y + 294, contentX + contentW, Color.web("#9dffb8"),
                "Invincible", engine.isInvincibleMode() ? "ON" : "OFF",
                engine.isInvincibleMode() ? Color.web("#72ffa8") : Color.web("#9ca2d8"));
        drawDivider(graphics, contentX, y + 322, contentW);

        drawProgressBlock(graphics, engine, boss, contentX, y + 365, contentW);
        drawDivider(graphics, contentX, y + 472, contentW);
        drawControls(graphics, contentX, y + 526, contentW);
    }

    private static void drawLeftTitle(GraphicsContext graphics, double x, double y, double w) {
        drawCrescent(graphics, x + 50, y + 48, 22);
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setFill(TEXT);
        graphics.setFont(Font.font("Georgia", FontWeight.BOLD, 32));
        graphics.fillText("Starry", x + w / 2.0 + 18, y + 51);
        graphics.fillText("Illusion", x + w / 2.0 + 18, y + 87);
        graphics.setFont(smallSerifFont());
        graphics.setFill(ROSE);
        graphics.fillText("Moonlit Shrine", x + w / 2.0, y + 116);
        drawDivider(graphics, x + 18, y + 135, w - 36);
    }

    private static void drawStageBanner(GraphicsContext graphics, double x, double y, double w, String text) {
        double h = 64;
        graphics.setFill(Color.web("#7d2759", 0.86));
        graphics.fillPolygon(
                new double[] {x, x + w, x + w - 16, x + w, x, x + 16},
                new double[] {y + 8, y + 8, y + h / 2.0, y + h - 8, y + h - 8, y + h / 2.0},
                6);
        graphics.setStroke(Color.web("#ffd8a8", 0.82));
        graphics.setLineWidth(1.2);
        graphics.strokePolygon(
                new double[] {x + 3, x + w - 3, x + w - 18, x + w - 3, x + 3, x + 18},
                new double[] {y + 11, y + 11, y + h / 2.0, y + h - 11, y + h - 11, y + h / 2.0},
                6);
        drawStar(graphics, x + 24, y + h / 2.0, 5.0, GOLD);
        drawStar(graphics, x + w - 24, y + h / 2.0, 5.0, GOLD);

        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setFont(Font.font("Georgia", FontWeight.BOLD, 26));
        graphics.setFill(TEXT);
        graphics.fillText(text, x + w / 2.0, y + 42);
    }

    private static void drawProgressBlock(GraphicsContext graphics, GameEngine engine, Boss boss,
                                          double x, double y, double w) {
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setFont(largeFont());
        graphics.setFill(ROSE);
        graphics.fillText("Stage Progress", x + w / 2.0, y - 18);

        graphics.setFill(Color.web("#1b173d", 0.95));
        graphics.fillRoundRect(x, y + 8, w, 16, 4, 4);
        if (engine.isBossActive() && boss != null) {
            graphics.setFill(Color.web("#f05c9a"));
            double bossRatio = Math.max(0, boss.getHp() / (double) boss.getMaxHp());
            graphics.fillRoundRect(x, y + 8, w * bossRatio, 16, 4, 4);
        } else {
            graphics.setFill(Color.web("#f4a5ff"));
            double stageRatio = Math.min(1.0, engine.getStageTime() / engine.getCurrentStageBossEntranceTime());
            graphics.fillRoundRect(x, y + 8, w * stageRatio, 16, 4, 4);
        }
        graphics.setStroke(Color.web("#f7bdd7", 0.82));
        graphics.strokeRoundRect(x, y + 8, w, 16, 4, 4);

        graphics.setFont(smallSerifFont());
        graphics.setFill(TEXT);
        graphics.fillText(engine.isBossActive() && boss != null
                ? boss.getPhaseName()
                : String.format("%s  %.0fs", engine.getCurrentStageName(), engine.getStageTime()),
                x + w / 2.0, y + 64);
    }

    private void drawPortraitFrame(GraphicsContext graphics, double x, double y, double w, double h,
                                   PortraitMood mood) {
        Color moodColor = switch (mood) {
            case HURT -> ROSE;
            case CHEER -> GOLD;
            case NORMAL -> LILAC;
        };

        graphics.setFill(Color.web("#09051a", 0.96));
        graphics.fillRoundRect(x, y, w, h, 12, 12);
        graphics.save();
        graphics.beginPath();
        graphics.rect(x + 6, y + 6, w - 12, h - 12);
        graphics.clip();
        Image portrait = selectPortrait(mood);
        if (portrait != null) {
            drawImageCover(graphics, portrait, x + 6, y + 6, w - 12, h - 12);
        } else {
            drawPortraitFallback(graphics, x + 6, y + 6, w - 12, h - 12);
        }
        if (mood == PortraitMood.HURT) {
            graphics.setFill(Color.web("#ff5b91", 0.16));
            graphics.fillRect(x + 6, y + 6, w - 12, h - 12);
        } else if (mood == PortraitMood.CHEER) {
            graphics.setFill(Color.web("#fff6a8", 0.13));
            graphics.fillRect(x + 6, y + 6, w - 12, h - 12);
        }
        graphics.restore();

        graphics.setStroke(Color.web("#0b0821", 0.75));
        graphics.setLineWidth(4);
        graphics.strokeRoundRect(x + 4, y + 4, w - 8, h - 8, 10, 10);
        graphics.setStroke(Color.color(moodColor.getRed(), moodColor.getGreen(), moodColor.getBlue(), 0.88));
        graphics.setLineWidth(1.4);
        graphics.strokeRoundRect(x + 4, y + 4, w - 8, h - 8, 10, 10);
        drawCornerStars(graphics, x + 16, y + 16);
        drawCornerStars(graphics, x + w - 16, y + 16);
        drawCornerStars(graphics, x + 16, y + h - 16);
        drawCornerStars(graphics, x + w - 16, y + h - 16);
    }

    private Image selectPortrait(PortraitMood mood) {
        Image selected = switch (mood) {
            case HURT -> hurtPortrait;
            case CHEER -> cheerPortrait;
            case NORMAL -> normalPortrait;
        };
        if (selected != null) {
            return selected;
        }
        if (normalPortrait != null) {
            return normalPortrait;
        }
        return AssetLoader.loadImage("assets/portraits/reimu.png");
    }

    private static void drawImageCover(GraphicsContext graphics, Image image, double x, double y, double w, double h) {
        double imageW = image.getWidth();
        double imageH = image.getHeight();
        if (imageW <= 0 || imageH <= 0) {
            return;
        }
        double imageRatio = imageW / imageH;
        double targetRatio = w / h;
        double drawW;
        double drawH;
        if (imageRatio > targetRatio) {
            drawH = h;
            drawW = h * imageRatio;
        } else {
            drawW = w;
            drawH = w / imageRatio;
        }
        double drawX = x - (drawW - w) / 2.0;
        double drawY = y - (drawH - h) * 0.36;
        graphics.drawImage(image, drawX, drawY, drawW, drawH);
    }

    private static void drawPortraitFallback(GraphicsContext graphics, double x, double y, double w, double h) {
        graphics.setFill(Color.web("#251a46"));
        graphics.fillRect(x, y, w, h);
        graphics.setFill(Color.web("#d91f5c"));
        graphics.fillOval(x + w * 0.31, y + h * 0.24, w * 0.38, w * 0.38);
        graphics.setFill(Color.web("#fff6f9"));
        graphics.fillOval(x + w * 0.38, y + h * 0.18, w * 0.24, w * 0.28);
        graphics.setFill(Color.web("#d91f5c"));
        graphics.fillPolygon(
                new double[] {x + w * 0.5, x + w * 0.24, x + w * 0.76},
                new double[] {y + h * 0.40, y + h * 0.88, y + h * 0.88},
                3);
    }

    private static void drawPanel(GraphicsContext graphics, double x, double y, double w, double h) {
        graphics.setFill(PANEL);
        graphics.fillRoundRect(x, y, w, h, 22, 22);
        graphics.setFill(PANEL_DEEP);
        graphics.fillRoundRect(x + 7, y + 7, w - 14, h - 14, 16, 16);
        graphics.setStroke(Color.web("#f5cba8", 0.75));
        graphics.setLineWidth(1.4);
        graphics.strokeRoundRect(x + 3, y + 3, w - 6, h - 6, 20, 20);
        graphics.setStroke(Color.web("#7a5cff", 0.50));
        graphics.setLineWidth(1.0);
        graphics.strokeRoundRect(x + 10, y + 10, w - 20, h - 20, 14, 14);
        drawCornerStars(graphics, x + 18, y + 18);
        drawCornerStars(graphics, x + w - 18, y + 18);
        drawCornerStars(graphics, x + 18, y + h - 18);
        drawCornerStars(graphics, x + w - 18, y + h - 18);
    }

    private static void drawDivider(GraphicsContext graphics, double x, double y, double w) {
        graphics.setStroke(Color.web("#f4a5c8", 0.38));
        graphics.setLineWidth(1.0);
        graphics.strokeLine(x + 20, y, x + w - 20, y);
        drawStar(graphics, x + 10, y, 4.5, GOLD);
        drawStar(graphics, x + w - 10, y, 4.5, GOLD);
    }

    private static void drawResourceRow(GraphicsContext graphics, double x, double y,
                                        String label, int count, Color color, boolean hearts) {
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFont(labelFont());
        graphics.setFill(MUTED);
        graphics.fillText(label, x, y);
        int iconCount = Math.min(count, 5);
        for (int i = 0; i < iconCount; i++) {
            double iconX = x + 90 + i * 24;
            if (hearts) {
                drawHeart(graphics, iconX, y - 7, 7.0, color);
            } else {
                drawStar(graphics, iconX + 7, y - 7, 7.0, color);
            }
        }
        if (count > iconCount) {
            graphics.setFill(TEXT);
            graphics.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
            graphics.fillText("+" + (count - iconCount), x + 90 + iconCount * 24, y - 3);
        }
    }

    private static void drawStat(GraphicsContext graphics, double x, double y, double rightX, Color iconColor,
                                 String label, String value) {
        drawStat(graphics, x, y, rightX, iconColor, label, value, TEXT);
    }

    private static void drawStat(GraphicsContext graphics, double x, double y, double rightX, Color iconColor,
                                 String label, String value, Color valueColor) {
        drawDiamond(graphics, x + 6, y - 5, 6, iconColor);
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFont(labelFont());
        graphics.setFill(MUTED);
        graphics.fillText(label, x + 22, y);
        graphics.setTextAlign(TextAlignment.RIGHT);
        graphics.setFill(valueColor);
        graphics.fillText(value, rightX, y);
    }

    private static void drawControls(GraphicsContext graphics, double x, double y, double w) {
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setFont(largeFont());
        graphics.setFill(ROSE);
        graphics.fillText("Controls", x + w / 2.0, y - 20);

        drawKeyLine(graphics, x, y + 8, w, "Move", "Arrow/WASD", "DIR");
        drawKeyLine(graphics, x, y + 42, w, "Shoot", "Z", "Z");
        drawKeyLine(graphics, x, y + 76, w, "Dream", "X", "X");
        drawKeyLine(graphics, x, y + 110, w, "Focus", "Shift", "Shift");
    }

    private static void drawKeyLine(GraphicsContext graphics, double x, double y, double w,
                                    String label, String key, String badge) {
        graphics.setTextAlign(TextAlignment.LEFT);
        double keyW = badge.length() > 3 ? 50 : 36;
        graphics.setFill(Color.web("#19133b", 0.96));
        graphics.fillRoundRect(x, y - 20, keyW, 24, 4, 4);
        graphics.setStroke(Color.web("#bda5ff", 0.72));
        graphics.strokeRoundRect(x, y - 20, keyW, 24, 4, 4);
        graphics.setFill(TEXT);
        graphics.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
        graphics.fillText(badge, x + 8, y - 3);
        graphics.setFont(labelFont());
        graphics.setFill(MUTED);
        graphics.fillText(label, x + keyW + 12, y - 3);
        graphics.setTextAlign(TextAlignment.RIGHT);
        graphics.setFill(TEXT);
        graphics.fillText(key, x + w, y - 3);
    }

    private static void drawCrescent(GraphicsContext graphics, double x, double y, double r) {
        graphics.setFill(Color.web("#fff3b8"));
        graphics.fillOval(x - r, y - r, r * 2, r * 2);
        graphics.setFill(PANEL_DEEP);
        graphics.fillOval(x - r * 0.30, y - r * 1.05, r * 2.08, r * 2.08);
    }

    private static void drawHeart(GraphicsContext graphics, double x, double y, double size, Color color) {
        graphics.setFill(color);
        graphics.beginPath();
        graphics.moveTo(x, y + size * 0.75);
        graphics.bezierCurveTo(x - size * 1.45, y - size * 0.25, x - size * 0.70, y - size * 1.35, x, y - size * 0.55);
        graphics.bezierCurveTo(x + size * 0.70, y - size * 1.35, x + size * 1.45, y - size * 0.25, x, y + size * 0.75);
        graphics.closePath();
        graphics.fill();
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

    private static void drawDiamond(GraphicsContext graphics, double x, double y, double radius, Color color) {
        graphics.setFill(color);
        graphics.fillPolygon(
                new double[] {x, x + radius, x, x - radius},
                new double[] {y - radius, y, y + radius, y},
                4);
    }

    private static void drawCornerStars(GraphicsContext graphics, double x, double y) {
        drawStar(graphics, x, y, 4.0, Color.web("#ffd987", 0.70));
        drawStar(graphics, x + 9, y + 7, 2.6, Color.web("#ffffff", 0.48));
    }
}
