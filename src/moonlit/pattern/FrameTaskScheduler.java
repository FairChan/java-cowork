package moonlit.pattern;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import moonlit.engine.GameEngine;
import moonlit.model.Boss;

/**
 * Advances coroutines on a fixed 60 FPS clock regardless of render delta jitter.
 */
public class FrameTaskScheduler {
    private static final double FRAMES_PER_SECOND = 60.0;
    private static final int MAX_CATCH_UP_FRAMES = 12;

    private final List<FrameCoroutine> tasks = new ArrayList<>();
    private double frameAccumulator;

    public void spawn(FrameCoroutine task) {
        tasks.add(task);
    }

    public void update(double deltaSeconds, Boss boss, GameEngine engine) {
        frameAccumulator += deltaSeconds * FRAMES_PER_SECOND;
        int frames = Math.min(MAX_CATCH_UP_FRAMES, (int) frameAccumulator);
        frameAccumulator -= frames;
        for (int i = 0; i < frames; i++) {
            updateOneFrame(boss, engine);
        }
    }

    public void reset() {
        tasks.clear();
        frameAccumulator = 0;
    }

    private void updateOneFrame(Boss boss, GameEngine engine) {
        Iterator<FrameCoroutine> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            FrameCoroutine task = iterator.next();
            task.updateFrame(boss, engine);
            if (task.isFinished()) {
                iterator.remove();
            }
        }
    }
}