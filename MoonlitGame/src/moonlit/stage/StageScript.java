package moonlit.stage;

import java.util.ArrayList;
import java.util.List;
import moonlit.model.Enemy;

/**
 * Defines timed enemy waves and boss entrance for a stage.
 */
public class StageScript {
    public static final double BOSS_ENTRANCE_TIME = StageDirector.FINAL_BOSS_TIME;

    private final int stageNumber;
    private final String stageName;
    private final double bossEntranceTime;
    private final List<EnemyWave> waves = new ArrayList<>();

    public StageScript() {
        this(1, "Starry Illusion", BOSS_ENTRANCE_TIME);
    }

    private StageScript(int stageNumber, String stageName, double bossEntranceTime) {
        this.stageNumber = stageNumber;
        this.stageName = stageName;
        this.bossEntranceTime = bossEntranceTime;
    }

    public static StageScript stageOne() {
        return new StageScript();
    }

    public static StageScript stageTwo() {
        StageScript script = new StageScript(2, "Starcrossed Moon Gate", 70.0);
        script.addStageTwoWaves();
        return script;
    }

    public List<EnemyWave> getWaves() {
        return waves;
    }

    public int getStageNumber() {
        return stageNumber;
    }

    public String getStageName() {
        return stageName;
    }

    public double getBossEntranceTime() {
        return bossEntranceTime;
    }

    public void reset() {
        for (EnemyWave wave : waves) {
            wave.reset();
        }
    }

    private void addStageOneWaves() {
        waves.add(new EnemyWave(5.0, Enemy.Kind.LANTERN, 3, true, 75, 22));
        waves.add(new EnemyWave(9.0, Enemy.Kind.LANTERN, 3, false, 75, 22));
        waves.add(new EnemyWave(13.0, Enemy.Kind.LANTERN, 4, true, 82, 25));
        waves.add(new EnemyWave(18.0, Enemy.Kind.CHARM_FAIRY, 3, false, 68, 28));
        waves.add(new EnemyWave(24.0, Enemy.Kind.CHARM_FAIRY, 3, true, 68, 28));
        waves.add(new EnemyWave(31.0, Enemy.Kind.CHARM_FAIRY, 4, false, 76, 30));
        waves.add(new EnemyWave(36.0, Enemy.Kind.LANTERN, 4, true, 95, 32));
        waves.add(new EnemyWave(42.0, Enemy.Kind.CHARM_FAIRY, 4, false, 90, 30));
        waves.add(new EnemyWave(48.0, Enemy.Kind.LANTERN, 5, false, 100, 34));
        waves.add(new EnemyWave(52.0, Enemy.Kind.CHARM_FAIRY, 3, true, 95, 34));
        waves.add(new EnemyWave(58.0, Enemy.Kind.LANTERN, 4, true, 105, 36));
        waves.add(new EnemyWave(62.0, Enemy.Kind.CHARM_FAIRY, 4, false, 100, 36));
    }

    private void addStageTwoWaves() {
        waves.add(new EnemyWave(6.0, Enemy.Kind.LANTERN, 4, true, 86, 18));
        waves.add(new EnemyWave(10.0, Enemy.Kind.LANTERN, 4, false, 128, 18));
        waves.add(new EnemyWave(15.0, Enemy.Kind.CHARM_FAIRY, 3, true, 118, 26));
        waves.add(new EnemyWave(20.0, Enemy.Kind.CHARM_FAIRY, 3, false, 118, 26));
        waves.add(new EnemyWave(27.0, Enemy.Kind.LANTERN, 5, true, 142, 28));
        waves.add(new EnemyWave(33.0, Enemy.Kind.LANTERN, 5, false, 142, 28));
        waves.add(new EnemyWave(41.0, Enemy.Kind.CHARM_FAIRY, 4, true, 132, 30));
        waves.add(new EnemyWave(47.0, Enemy.Kind.CHARM_FAIRY, 4, false, 132, 30));
        waves.add(new EnemyWave(55.0, Enemy.Kind.LANTERN, 5, true, 150, 34));
        waves.add(new EnemyWave(60.0, Enemy.Kind.CHARM_FAIRY, 5, false, 138, 34));
        waves.add(new EnemyWave(66.0, Enemy.Kind.LANTERN, 6, false, 160, 36));
    }
}

