package moonlit.engine;

/**
 * Shared constants for the fixed-size course demo window.
 */
public final class GameConfig {
    public static final int WINDOW_WIDTH = 1176;
    public static final int WINDOW_HEIGHT = 700;

    public static final double OUTER_MARGIN = 24;
    public static final double PANEL_GAP = 24;
    public static final double LEFT_HUD_X = OUTER_MARGIN;
    public static final double LEFT_HUD_WIDTH = 230;

    public static final double PLAYFIELD_X = LEFT_HUD_X + LEFT_HUD_WIDTH + PANEL_GAP;
    public static final double PLAYFIELD_Y = 24;
    public static final double PLAYFIELD_WIDTH = 620;
    public static final double PLAYFIELD_HEIGHT = 652;
    public static final double PLAYFIELD_RIGHT = PLAYFIELD_X + PLAYFIELD_WIDTH;
    public static final double PLAYFIELD_BOTTOM = PLAYFIELD_Y + PLAYFIELD_HEIGHT;

    public static final double HUD_X = PLAYFIELD_RIGHT + PANEL_GAP;
    public static final double HUD_WIDTH = WINDOW_WIDTH - HUD_X - OUTER_MARGIN;

    private GameConfig() {
    }
}
