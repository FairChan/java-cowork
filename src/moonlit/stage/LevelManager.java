package moonlit.stage;

import moonlit.engine.GameEngine;

/**
 * Advances Stage 1 time, spawns waves, and requests the boss entrance.
 */
public class LevelManager {
    private final StageScript script = new StageScript();
    private double elapsedSeconds;
    private boolean bossTriggered;

    public void reset() {
        elapsedSeconds = 0;
        bossTriggered = false;
        script.reset();
    }

    public void update(double deltaSeconds, GameEngine engine) {
        elapsedSeconds += deltaSeconds;
        for (EnemyWave wave : script.getWaves()) {
            if (wave.shouldSpawn(elapsedSeconds)) {
                wave.spawn(engine);
            }
        }
        if (!bossTriggered && elapsedSeconds >= StageScript.BOSS_ENTRANCE_TIME) {
            bossTriggered = true;
            engine.activateBoss();
        }
    }

    public double getElapsedSeconds() {
        return elapsedSeconds;
    }
}
