package moonlit.pattern;

import moonlit.engine.GameEngine;
import moonlit.model.Boss;

/**
 * Minimal frame-synchronized task used to model Danmakufu-style wait/yield routines.
 */
public abstract class FrameCoroutine {
    private int waitFrames;
    private boolean finished;

    public final void updateFrame(Boss boss, GameEngine engine) {
        if (finished) {
            return;
        }
        if (waitFrames > 0) {
            waitFrames--;
            return;
        }
        runFrame(boss, engine);
    }

    public final boolean isFinished() {
        return finished;
    }

    protected final void waitFrames(int frames) {
        waitFrames = Math.max(0, frames);
    }

    protected final void finish() {
        finished = true;
    }

    protected abstract void runFrame(Boss boss, GameEngine engine);
}