package moonlit.engine;

/**
 * Shared constants for the fixed-size course demo window.
 */
public final class GameConfig {
    public static final int WINDOW_WIDTH = 900;
    public static final int WINDOW_HEIGHT = 700;

    public static final double PLAYFIELD_X = 24;
    public static final double PLAYFIELD_Y = 24;
    public static final double PLAYFIELD_WIDTH = 620;
    public static final double PLAYFIELD_HEIGHT = 652;
    public static final double PLAYFIELD_RIGHT = PLAYFIELD_X + PLAYFIELD_WIDTH;
    public static final double PLAYFIELD_BOTTOM = PLAYFIELD_Y + PLAYFIELD_HEIGHT;

    public static final double HUD_X = 668;
    public static final double HUD_WIDTH = WINDOW_WIDTH - HUD_X - 24;

    private GameConfig() {
    }
}
