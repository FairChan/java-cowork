package moonlit.stage;

import java.util.ArrayList;
import java.util.List;
import moonlit.model.Enemy;

/**
 * Defines the first level's timed waves and boss entrance.
 */
public class StageScript {
    public static final double BOSS_ENTRANCE_TIME = 65.0;
    private final List<EnemyWave> waves = new ArrayList<>();

    public StageScript() {
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

    public List<EnemyWave> getWaves() {
        return waves;
    }

    public void reset() {
        for (EnemyWave wave : waves) {
            wave.reset();
        }
    }
}
