package moonlit.engine;

import java.util.HashSet;
import java.util.Set;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/**
 * Encapsulates keyboard state and one-frame key pulses.
 */
public class InputController {
    private final Set<KeyCode> heldKeys = new HashSet<>();
    private final Set<KeyCode> pressedThisFrame = new HashSet<>();
    private double mouseX;
    private double mouseY;
    private boolean hasMousePosition;
    private boolean primaryClickedThisFrame;

    public void attach(Scene scene) {
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (heldKeys.add(code)) {
                pressedThisFrame.add(code);
            }
        });
        scene.setOnKeyReleased(event -> heldKeys.remove(event.getCode()));
        scene.setOnMouseMoved(event -> {
            mouseX = event.getX();
            mouseY = event.getY();
            hasMousePosition = true;
        });
        scene.setOnMouseDragged(event -> {
            mouseX = event.getX();
            mouseY = event.getY();
            hasMousePosition = true;
        });
        scene.setOnMouseClicked(event -> {
            mouseX = event.getX();
            mouseY = event.getY();
            hasMousePosition = true;
            if (event.getButton() == MouseButton.PRIMARY) {
                primaryClickedThisFrame = true;
            }
        });
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

    public boolean consumeMenuUpPressed() {
        return consume(KeyCode.UP) || consume(KeyCode.W);
    }

    public boolean consumeMenuDownPressed() {
        return consume(KeyCode.DOWN) || consume(KeyCode.S);
    }

    public boolean consumeMenuSelectPressed() {
        return consume(KeyCode.ENTER) || consume(KeyCode.Z);
    }

    public boolean consumeMenuExitPressed() {
        return consume(KeyCode.ESCAPE);
    }

    public boolean consumeDialogueAdvancePressed() {
        return consume(KeyCode.ENTER) || consume(KeyCode.Z);
    }

    public boolean consumeStageTwoPressed() {
        return consume(KeyCode.DIGIT2) || consume(KeyCode.NUMPAD2);
    }

    public boolean hasMousePosition() {
        return hasMousePosition;
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public boolean consumePrimaryClick() {
        boolean clicked = primaryClickedThisFrame;
        primaryClickedThisFrame = false;
        return clicked;
    }

    public void pressForTests(KeyCode code) {
        heldKeys.add(code);
        pressedThisFrame.add(code);
    }

    public void moveMouseForTests(double x, double y) {
        mouseX = x;
        mouseY = y;
        hasMousePosition = true;
    }

    public void clickMouseForTests(double x, double y) {
        moveMouseForTests(x, y);
        primaryClickedThisFrame = true;
    }

    public void endFrame() {
        pressedThisFrame.clear();
        primaryClickedThisFrame = false;
    }

    private boolean isHeld(KeyCode code) {
        return heldKeys.contains(code);
    }

    private boolean consume(KeyCode code) {
        return pressedThisFrame.remove(code);
    }
}
