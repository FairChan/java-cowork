package moonlit.stage;

import moonlit.engine.GameEngine;

/**
 * Advances current stage time, dispatches the formal director, and requests boss entrances.
 */
public class LevelManager {
    private StageScript script = StageScript.stageOne();
    private final StageDirector starryDirector = new StageDirector();
    private double elapsedSeconds;
    private boolean bossTriggered;

    public void setStage(int stageNumber) {
        script = stageNumber == 2 ? StageScript.stageTwo() : StageScript.stageOne();
        reset();
    }

    public void reset() {
        elapsedSeconds = 0;
        bossTriggered = false;
        script.reset();
        starryDirector.reset();
    }

    public void update(double deltaSeconds, GameEngine engine) {
        elapsedSeconds += deltaSeconds;
        if (script.getStageNumber() == 1) {
            starryDirector.update(elapsedSeconds, engine);
            if (!bossTriggered && elapsedSeconds >= script.getBossEntranceTime()) {
                bossTriggered = true;
            }
            return;
        }

        for (EnemyWave wave : script.getWaves()) {
            if (wave.shouldSpawn(elapsedSeconds)) {
                wave.spawn(engine);
            }
        }
        if (!bossTriggered && elapsedSeconds >= script.getBossEntranceTime()) {
            bossTriggered = true;
            engine.activateBoss();
        }
    }

    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    public int getStageNumber() {
        return script.getStageNumber();
    }

    public String getStageName() {
        return script.getStageName();
    }

    public double getBossEntranceTime() {
        return script.getBossEntranceTime();
    }

    public int getEventCount(String eventId) {
        return script.getStageNumber() == 1 ? starryDirector.getEventCount(eventId) : 0;
    }

    public boolean isMidBossActive() {
        return script.getStageNumber() == 1 && starryDirector.isMidBossActive();
    }
}
