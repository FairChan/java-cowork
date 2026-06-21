package moonlit.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import moonlit.engine.GameConfig;
import moonlit.engine.GameEngine;
import moonlit.render.AssetLoader;

/**
 * Collectible item dropped by enemies, bosses, and bullet clears.
 */
public class ItemDrop extends GameObject {
    public enum Type {
        SMALL_POWER,
        BIG_POWER,
        BLUE,
        GREEN,
        BOMB_FRAGMENT,
        LIFE_FRAGMENT,
        LIFE
    }

    private final Type type;
    private final Image icon;
    private double velocityY = 76;

    public ItemDrop(Type type, double x, double y) {
        super(x, y, 7);
        this.type = type;
        this.icon = AssetLoader.loadImage(iconPathForType(type));
    }

    @Override
    public void update(double deltaSeconds, GameEngine engine) {
        Player player = engine.getPlayer();
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double distance = Math.hypot(dx, dy);
        boolean attracted = player.getY() < GameConfig.PLAYFIELD_Y + 150 || distance < 92;
        if (attracted && distance > 0.001) {
            double speed = 360;
            x += dx / distance * speed * deltaSeconds;
            y += dy / distance * speed * deltaSeconds;
        } else {
            y += velocityY * deltaSeconds;
            velocityY = Math.min(160, velocityY + 42 * deltaSeconds);
        }
        if (distance <= radius + 12) {
            engine.collectItem(this);
            alive = false;
        }
        if (y > GameConfig.PLAYFIELD_BOTTOM + 32) {
            alive = false;
        }
    }

    @Override
    public void render(GraphicsContext graphics) {
        if (icon != null) {
            double size = type == Type.BIG_POWER || type == Type.LIFE ? 30 : 26;
            graphics.drawImage(icon, x - size / 2.0, y - size / 2.0, size, size);
            return;
        }
        graphics.setFill(colorForType(type));
        graphics.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        graphics.setFill(Color.web("#ffffff", 0.78));
        graphics.fillOval(x - 2.4, y - 2.4, 4.8, 4.8);
    }

    public Type getType() {
        return type;
    }

    public static String iconPathForType(Type type) {
        return switch (type) {
            case SMALL_POWER -> "assets/items/small_power.png";
            case BIG_POWER -> "assets/items/big_power.png";
            case BLUE -> "assets/items/score_blue.png";
            case GREEN -> "assets/items/clear_green.png";
            case BOMB_FRAGMENT -> "assets/items/bomb_fragment.png";
            case LIFE_FRAGMENT -> "assets/items/life_fragment.png";
            case LIFE -> "assets/items/life_full.png";
        };
    }

    public static Color colorForType(Type type) {
        return switch (type) {
            case SMALL_POWER -> Color.web("#ff8fb6");
            case BIG_POWER -> Color.web("#ff3976");
            case BLUE -> Color.web("#72b7ff");
            case GREEN -> Color.web("#68ff9f");
            case BOMB_FRAGMENT -> Color.web("#8ee7ff");
            case LIFE_FRAGMENT -> Color.web("#ffd76e");
            case LIFE -> Color.web("#ffeff8");
        };
    }
}