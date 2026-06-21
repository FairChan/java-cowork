package moonlit.dialogue;

/**
 * One line in a story dialogue scene, including portrait placement and optional cue.
 */
public final class DialogueLine {
    public enum PortraitSide {
        LEFT,
        RIGHT,
        NONE
    }

    public enum Cue {
        NONE,
        BOSS_THEME,
        BOSS_THEME_SHAKE,
        STOP_MUSIC,
        SHAKE
    }

    private final String speaker;
    private final String text;
    private final PortraitSide portraitSide;
    private final Cue cue;

    public DialogueLine(String speaker, String text, PortraitSide portraitSide) {
        this(speaker, text, portraitSide, Cue.NONE);
    }

    public DialogueLine(String speaker, String text, PortraitSide portraitSide, Cue cue) {
        this.speaker = speaker;
        this.text = text;
        this.portraitSide = portraitSide;
        this.cue = cue;
    }

    public String speaker() {
        return speaker;
    }

    public String text() {
        return text;
    }

    public PortraitSide portraitSide() {
        return portraitSide;
    }

    public Cue cue() {
        return cue;
    }
}