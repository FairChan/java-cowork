package moonlit.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import moonlit.engine.GameConfig;
import moonlit.engine.GameEngine;
import moonlit.model.Boss;
import moonlit.model.Player;

/**
 * Draws score, boss health, controls, and player resources.
 */
public class HudRenderer {
    public void render(GraphicsContext graphics, GameEngine engine) {
        Player player = engine.getPlayer();
        Boss boss = engine.getBoss();

        graphics.setFill(Color.web("#0d1230", 0.88));
        graphics.fillRoundRect(GameConfig.HUD_X, GameConfig.PLAYFIELD_Y,
                GameConfig.HUD_WIDTH, GameConfig.PLAYFIELD_HEIGHT, 8, 8);
        graphics.setStroke(Color.web("#516399"));
        graphics.strokeRoundRect(GameConfig.HUD_X, GameConfig.PLAYFIELD_Y,
                GameConfig.HUD_WIDTH, GameConfig.PLAYFIELD_HEIGHT, 8, 8);

        graphics.setFont(largeFont());
        graphics.setFill(Color.web("#fff6a8"));
        graphics.fillText("Moonlit Shrine", GameConfig.HUD_X + 18, 66);
        graphics.setFill(Color.web("#c9d7ff"));
        graphics.setFont(bodyFont());
        graphics.fillText("Score", GameConfig.HUD_X + 18, 110);
        graphics.setFill(Color.WHITE);
        graphics.fillText(String.format("%08d", engine.getScore()), GameConfig.HUD_X + 18, 136);

        graphics.setFill(Color.web("#c9d7ff"));
        graphics.fillText("Lives", GameConfig.HUD_X + 18, 183);
        drawPips(graphics, GameConfig.HUD_X + 85, 176, player.getLives(), Color.web("#ff709c"));
        graphics.fillText("Bombs", GameConfig.HUD_X + 18, 225);
        drawPips(graphics, GameConfig.HUD_X + 85, 218, player.getBombs(), Color.web("#8ee7ff"));

        graphics.setFill(Color.web("#c9d7ff"));
        graphics.fillText("Graze", GameConfig.HUD_X + 18, 273);
        graphics.setFill(Color.WHITE);
        graphics.fillText(String.valueOf(engine.getGrazeCount()), GameConfig.HUD_X + 90, 273);

        graphics.setFill(Color.web("#c9d7ff"));
        graphics.fillText(engine.isBossActive() ? "Boss" : "Stage 1", GameConfig.HUD_X + 18, 326);
        graphics.setFill(Color.web("#1b2148"));
        graphics.fillRect(GameConfig.HUD_X + 18, 342, 170, 14);
        if (engine.isBossActive() && boss != null) {
            graphics.setFill(Color.web("#f05c9a"));
            double bossRatio = Math.max(0, boss.getHp() / (double) boss.getMaxHp());
            graphics.fillRect(GameConfig.HUD_X + 18, 342, 170 * bossRatio, 14);
        } else {
            graphics.setFill(Color.web("#8ee7ff"));
            double stageRatio = Math.min(1.0, engine.getStageTime() / 65.0);
            graphics.fillRect(GameConfig.HUD_X + 18, 342, 170 * stageRatio, 14);
        }
        graphics.setStroke(Color.web("#f7bdd7"));
        graphics.strokeRect(GameConfig.HUD_X + 18, 342, 170, 14);

        graphics.setFill(Color.web("#f5f0ff"));
        graphics.fillText(engine.isBossActive() && boss != null
                ? boss.getPhaseName()
                : String.format("Moonlit Approach %.0fs", engine.getStageTime()), GameConfig.HUD_X + 18, 386);

        graphics.setFill(Color.web("#8fa4e8"));
        graphics.fillText("Controls", GameConfig.HUD_X + 18, 462);
        graphics.setFill(Color.web("#dce5ff"));
        graphics.fillText("Move  Arrow / WASD", GameConfig.HUD_X + 18, 492);
        graphics.fillText("Shoot Z", GameConfig.HUD_X + 18, 522);
        graphics.fillText("Bomb  X", GameConfig.HUD_X + 18, 552);
        graphics.fillText("Focus Shift", GameConfig.HUD_X + 18, 582);
        graphics.fillText("Pause P", GameConfig.HUD_X + 18, 612);
    }

    public static Font titleFont() {
        return Font.font("Verdana", FontWeight.BOLD, 34);
    }

    public static Font largeFont() {
        return Font.font("Verdana", FontWeight.BOLD, 20);
    }

    public static Font bodyFont() {
        return Font.font("Consolas", FontWeight.NORMAL, 15);
    }

    private static void drawPips(GraphicsContext graphics, double x, double y, int count, Color color) {
        graphics.setFill(color);
        for (int i = 0; i < count; i++) {
            graphics.fillOval(x + i * 22, y, 14, 14);
        }
    }
}
