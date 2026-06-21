package moonlit.engine;

import java.util.HashSet;
import java.util.Set;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

/**
 * Encapsulates keyboard state and one-frame key pulses.
 */
public class InputController {
    private final Set<KeyCode> heldKeys = new HashSet<>();
    private final Set<KeyCode> pressedThisFrame = new HashSet<>();

    public void attach(Scene scene) {
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (heldKeys.add(code)) {
                pressedThisFrame.add(code);
            }
        });
        scene.setOnKeyReleased(event -> heldKeys.remove(event.getCode()));
    }

    public boolean isMoveLeft() {
        return isHeld(KeyCode.LEFT) || isHeld(KeyCode.A);
    }

    public boolean isMoveRight() {
        return isHeld(KeyCode.RIGHT) || isHeld(KeyCode.D);
    }

    public boolean isMoveUp() {
        return isHeld(KeyCode.UP) || isHeld(KeyCode.W);
    }

    public boolean isMoveDown() {
        return isHeld(KeyCode.DOWN) || isHeld(KeyCode.S);
    }

    public boolean isFocusHeld() {
        return isHeld(KeyCode.SHIFT);
    }

    public boolean isShootHeld() {
        return isHeld(KeyCode.Z);
    }

    public boolean consumeBombPressed() {
        return consume(KeyCode.X);
    }

    public boolean consumePausePressed() {
        return consume(KeyCode.P);
    }

    public boolean consumeInvincibleTogglePressed() {
        return consume(KeyCode.I);
    }

    public boolean consumeStartPressed() {
        return consume(KeyCode.ENTER);
    }

    public boolean consumeDialogueAdvancePressed() {
        return consume(KeyCode.ENTER) || consume(KeyCode.Z);
    }

    public boolean consumeStageTwoPressed() {
        return consume(KeyCode.DIGIT2) || consume(KeyCode.NUMPAD2);
    }

    public void endFrame() {
        pressedThisFrame.clear();
    }

    private boolean isHeld(KeyCode code) {
        return heldKeys.contains(code);
    }

    private boolean consume(KeyCode code) {
        return pressedThisFrame.remove(code);
    }
}
