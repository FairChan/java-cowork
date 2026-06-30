package moonlit.dialogue;

/**
 * Holds the active dialogue scene and advances one line at a time.
 */
public final class DialogueController {
    private DialogueScene scene;
    private int lineIndex;

    public DialogueLine.Cue begin(DialogueScene nextScene) {
        scene = nextScene;
        lineIndex = 0;
        return currentLine().cue();
    }

    public DialogueLine.Cue advance() {
        if (!isActive()) {
            return DialogueLine.Cue.NONE;
        }
        lineIndex++;
        if (lineIndex >= scene.lines().size()) {
            Runnable completion = scene.onComplete();
            clear();
            if (completion != null) {
                completion.run();
            }
            return DialogueLine.Cue.NONE;
        }
        return currentLine().cue();
    }

    public boolean isActive() {
        return scene != null;
    }

    public DialogueLine currentLine() {
        return scene.lines().get(lineIndex);
    }

    public String sceneId() {
        return scene == null ? "" : scene.id();
    }

    public int lineIndex() {
        return lineIndex;
    }

    public void clear() {
        scene = null;
        lineIndex = 0;
    }
}