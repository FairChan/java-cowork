package moonlit.dialogue;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import moonlit.dialogue.DialogueLine.PortraitSide;
import moonlit.engine.GameConfig;
import moonlit.render.AssetLoader;

/**
 * Canvas renderer for Touhou-style character dialogue overlays.
 */
public final class DialogueRenderer {
    private final Image reimuPortrait = AssetLoader.loadImage("assets/portraits/reimu.png");
    private final Image marisaPortrait = AssetLoader.loadImage("assets/portraits/marisa.png");
    private final Image kitsunePortrait = AssetLoader.loadImage("assets/bosses/boss_kitsune.png");
    private final Image lanternFairyPortrait = AssetLoader.loadImage("assets/bosses/boss_lantern_fairy.png");
    private final Image starOraclePortrait = AssetLoader.loadImage("assets/bosses/boss_star_oracle.png");

    public void render(GraphicsContext graphics, DialogueController controller) {
        if (!controller.isActive()) {
            return;
        }
        DialogueLine line = controller.currentLine();

        graphics.setFill(Color.web("#02030b", 0.26));
        graphics.fillRect(GameConfig.PLAYFIELD_X, GameConfig.PLAYFIELD_Y,
                GameConfig.PLAYFIELD_WIDTH, GameConfig.PLAYFIELD_HEIGHT);

        drawPortrait(graphics, reimuPortrait, GameConfig.PLAYFIELD_X + 24, GameConfig.PLAYFIELD_Y + 184,
                line.portraitSide() == PortraitSide.LEFT);
        drawPortrait(graphics, selectRightPortrait(line), GameConfig.PLAYFIELD_RIGHT - 184, GameConfig.PLAYFIELD_Y + 184,
                line.portraitSide() == PortraitSide.RIGHT);
        drawTextBox(graphics, line);
    }

    private Image selectRightPortrait(DialogueLine line) {
        return switch (line.speaker()) {
            case "Kitsune Envoy" -> kitsunePortrait != null ? kitsunePortrait : marisaPortrait;
            case "Lantern Butterfly" -> lanternFairyPortrait != null ? lanternFairyPortrait : marisaPortrait;
            case "Star Oracle" -> starOraclePortrait != null ? starOraclePortrait : marisaPortrait;
            default -> marisaPortrait;
        };
    }

    private void drawPortrait(GraphicsContext graphics, Image image, double x, double y, boolean active) {
        double width = 160;
        double height = 230;
        double oldAlpha = graphics.getGlobalAlpha();
        graphics.setGlobalAlpha(active ? 1.0 : 0.42);
        graphics.setFill(Color.web("#f9f2ff", active ? 0.86 : 0.46));
        graphics.fillRoundRect(x, y, width, height, 8, 8);
        graphics.setStroke(active ? Color.web("#fff6a8") : Color.web("#7080bb"));
        graphics.setLineWidth(active ? 3 : 1.5);
        graphics.strokeRoundRect(x, y, width, height, 8, 8);
        if (image != null) {
            double scale = Math.min((width - 14) / image.getWidth(), (height - 14) / image.getHeight());
            double drawWidth = image.getWidth() * scale;
            double drawHeight = image.getHeight() * scale;
            graphics.drawImage(image, x + (width - drawWidth) / 2.0, y + height - drawHeight - 7, drawWidth, drawHeight);
        }
        graphics.setGlobalAlpha(oldAlpha);
    }

    private void drawTextBox(GraphicsContext graphics, DialogueLine line) {
        double boxX = GameConfig.PLAYFIELD_X + 32;
        double boxY = GameConfig.PLAYFIELD_BOTTOM - 178;
        double boxW = GameConfig.PLAYFIELD_WIDTH - 64;
        double boxH = 154;
        graphics.setFill(Color.web("#090d24", 0.88));
        graphics.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        graphics.setStroke(Color.web("#8cb8ff", 0.86));
        graphics.setLineWidth(2);
        graphics.strokeRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        graphics.setFill(nameColor(line.portraitSide()));
        graphics.fillRoundRect(boxX + 18, boxY - 18, 108, 32, 8, 8);
        graphics.setFill(Color.web("#080814"));
        graphics.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        graphics.fillText(line.speaker(), boxX + 34, boxY + 4);

        graphics.setFill(Color.web("#f5f0ff"));
        graphics.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 18));
        List<String> wrapped = wrap(line.text(), 34.0);
        for (int i = 0; i < wrapped.size() && i < 4; i++) {
            graphics.fillText(wrapped.get(i), boxX + 24, boxY + 44 + i * 27);
        }
        graphics.setFill(Color.web("#fff6a8"));
        graphics.setFont(Font.font("Verdana", FontWeight.BOLD, 13));
        graphics.fillText("Z / Enter", boxX + boxW - 94, boxY + boxH - 18);
    }

    private Color nameColor(PortraitSide side) {
        return switch (side) {
            case LEFT -> Color.web("#ffbdd3");
            case RIGHT -> Color.web("#fff1a5");
            case NONE -> Color.web("#b7c8ff");
        };
    }

    private List<String> wrap(String text, double maxUnits) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        double units = 0;
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            double width = character < 128 ? 0.55 : 1.0;
            if (units + width > maxUnits && current.length() > 0) {
                lines.add(current.toString());
                current.setLength(0);
                units = 0;
            }
            current.append(character);
            units += width;
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }
}
