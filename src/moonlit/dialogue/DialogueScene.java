package moonlit.dialogue;

import java.util.List;

/**
 * Immutable story scene with a completion callback used to resume stage flow.
 */
public final class DialogueScene {
    private final String id;
    private final List<DialogueLine> lines;
    private final Runnable onComplete;

    public DialogueScene(String id, List<DialogueLine> lines, Runnable onComplete) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("dialogue scene requires at least one line");
        }
        this.id = id;
        this.lines = List.copyOf(lines);
        this.onComplete = onComplete;
    }

    public String id() {
        return id;
    }

    public List<DialogueLine> lines() {
        return lines;
    }

    public Runnable onComplete() {
        return onComplete;
    }
}