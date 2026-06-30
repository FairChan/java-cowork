package moonlit.stage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import moonlit.engine.GameConfig;
import moonlit.dialogue.DialogueScripts;
import moonlit.engine.GameEngine;
import moonlit.model.Enemy;
import moonlit.model.EnemyPatternProfile;
import moonlit.model.ItemDrop;

/**
 * Frame-timed director for Stage 1: Starry Illusion.
 */
public class StageDirector {
    public static final double MINI_BOSS_ONE_TIME = 34.0;
    public static final double MINI_BOSS_TWO_TIME = 82.0;
    public static final double FINAL_BOSS_TIME = 132.0;

    private static final long PATTERN_SEED = 2026062101L;

    private final Map<String, Integer> eventCounts = new HashMap<>();
    private final Set<String> firedEvents = new HashSet<>();
    private Random patternRandom = new Random(PATTERN_SEED);

    public void reset() {
        eventCounts.clear();
        firedEvents.clear();
        patternRandom = new Random(PATTERN_SEED);
    }

    public void update(double elapsedSeconds, GameEngine engine) {
        trigger(elapsedSeconds, 0.01, "fairy_probe", () -> spawnFairyProbe(engine, 0));
        trigger(elapsedSeconds, 9.0, "fairy_probe_b", () -> spawnFairyProbe(engine, 1));
        trigger(elapsedSeconds, 18.0, "fairy_probe_c", () -> spawnFairyProbe(engine, 2));

        trigger(elapsedSeconds, 25.0, "kedama_lock", () -> {
            engine.requestWaveBackground(2);
            spawnKedamaLock(engine, 0);
        });

        trigger(elapsedSeconds, MINI_BOSS_ONE_TIME, "mini_boss_one",
                () -> engine.startDialogue(DialogueScripts.miniBossOneIntro(engine::activateMiniBossOne)));

        trigger(elapsedSeconds, 48.0, "kedama_lock_b", () -> spawnKedamaLock(engine, 1));
        trigger(elapsedSeconds, 50.0, "greater_fairy", () -> {
            engine.requestWaveBackground(3);
            spawnGreaterFairies(engine);
        });
        trigger(elapsedSeconds, 68.0, "greater_fairy_followup", () -> spawnGreaterFairyFollowup(engine));

        trigger(elapsedSeconds, MINI_BOSS_TWO_TIME, "mini_boss_two",
                () -> engine.startDialogue(DialogueScripts.miniBossTwoIntro(engine::activateMiniBossTwo)));

        trigger(elapsedSeconds, 96.0, "sunflower_rage", () -> spawnSunflowerRage(engine, 0));
        trigger(elapsedSeconds, 110.0, "sunflower_rage_b", () -> spawnSunflowerRage(engine, 1));
        trigger(elapsedSeconds, 115.0, "wave4_background", () -> {
            engine.requestWaveBackground(4);
        });
        trigger(elapsedSeconds, 122.0, "sunflower_rage_c", () -> spawnSunflowerRage(engine, 2));

        trigger(elapsedSeconds, FINAL_BOSS_TIME, "final_boss",
                () -> engine.startDialogue(DialogueScripts.finalBossIntro(engine::activateFinalBoss)));
    }

    public int getEventCount(String eventId) {
        return eventCounts.getOrDefault(eventId, 0);
    }

    public boolean isMidBossActive() {
        return false;
    }

    private void trigger(double elapsedSeconds, double time, String id, Runnable action) {
        if (elapsedSeconds < time || firedEvents.contains(id)) {
            return;
        }
        firedEvents.add(id);
        String publicId = normalizePublicId(id);
        eventCounts.put(publicId, eventCounts.getOrDefault(publicId, 0) + 1);
        action.run();
    }

    private String normalizePublicId(String id) {
        if (id.startsWith("fairy_probe")) {
            return "fairy_probe";
        }
        if (id.startsWith("kedama_lock")) {
            return "kedama_lock";
        }
        if (id.startsWith("greater_fairy")) {
            return "greater_fairy";
        }
        if (id.startsWith("sunflower_rage")) {
            return "sunflower_rage";
        }
        return id;
    }

    private void spawnFairyProbe(GameEngine engine, int batch) {
        for (int side = 0; side < 2; side++) {
            boolean fromLeft = side == 0;
            for (int lane = 0; lane < 3; lane++) {
                double entryY = GameConfig.PLAYFIELD_Y + 34 + lane * 46 + batch * 7;
                double targetX = fromLeft
                        ? GameConfig.PLAYFIELD_X + 150 + lane * 65
                        : GameConfig.PLAYFIELD_RIGHT - 150 - lane * 65;
                double targetY = GameConfig.PLAYFIELD_Y + 96 + lane * 52 + batch * 7;
                engine.addEnemy(Enemy.probeFairy(entryY, fromLeft, targetX, targetY,
                        nextProfile(Enemy.Kind.CHARM_FAIRY, batch, side * 3 + lane)));
            }
        }
    }

    private void spawnKedamaLock(GameEngine engine, int batch) {
        for (int i = 0; i < 15; i++) {
            boolean fromLeft = i % 2 == 0;
            double y = GameConfig.PLAYFIELD_Y + 245 + (i % 5) * 38 + batch * 10;
            engine.addEnemy(Enemy.kedama(y, fromLeft, i, nextProfile(Enemy.Kind.KEDAMA, batch, i)));
        }
        if (batch == 2) {
            engine.addItem(new ItemDrop(ItemDrop.Type.BOMB_FRAGMENT,
                    GameConfig.PLAYFIELD_X + GameConfig.PLAYFIELD_WIDTH / 2.0, GameConfig.PLAYFIELD_Y + 260));
        }
    }

    private void spawnGreaterFairies(GameEngine engine) {
        engine.addEnemy(Enemy.greaterFairy(GameConfig.PLAYFIELD_X + 190, GameConfig.PLAYFIELD_Y + 178,
                nextProfile(Enemy.Kind.GREATER_FAIRY, 0, 0)));
        engine.addEnemy(Enemy.greaterFairy(GameConfig.PLAYFIELD_RIGHT - 190, GameConfig.PLAYFIELD_Y + 178,
                nextProfile(Enemy.Kind.GREATER_FAIRY, 0, 1)));
    }

    private void spawnGreaterFairyFollowup(GameEngine engine) {
        engine.addEnemy(Enemy.greaterFairy(GameConfig.PLAYFIELD_X + 310, GameConfig.PLAYFIELD_Y + 156,
                nextProfile(Enemy.Kind.GREATER_FAIRY, 1, 0)));
    }

    private void spawnSunflowerRage(GameEngine engine, int batch) {
        double leftX = GameConfig.PLAYFIELD_X - 45;
        double rightX = GameConfig.PLAYFIELD_RIGHT + 45;
        engine.addEnemy(Enemy.sunflower(leftX, true, nextProfile(Enemy.Kind.SUNFLOWER, batch, 0)));
        engine.addEnemy(Enemy.sunflower(rightX, false, nextProfile(Enemy.Kind.SUNFLOWER, batch, 1)));
        if (batch % 2 == 0) {
            engine.addEnemy(Enemy.sunflower(leftX - 35, true, nextProfile(Enemy.Kind.SUNFLOWER, batch, 2)));
        } else {
            engine.addEnemy(Enemy.sunflower(rightX + 35, false, nextProfile(Enemy.Kind.SUNFLOWER, batch, 2)));
        }
    }

    private EnemyPatternProfile nextProfile(Enemy.Kind kind, int batch, int slot) {
        int variant = batch * 11 + slot * 3 + patternRandom.nextInt(8);
        return switch (kind) {
            case LANTERN -> new EnemyPatternProfile(variant, oddBetween(7, 11), 0.0,
                    between(78, 118), between(4.7, 6.1), between(0.85, 1.18), between(2, 7));
            case CHARM_FAIRY -> new EnemyPatternProfile(variant, oddBetween(3, 5), between(0.19, 0.31),
                    between(122, 176), between(5.0, 6.4), between(0.82, 1.20), between(3, 9));
            case KEDAMA -> new EnemyPatternProfile(variant, 1, 0.0,
                    between(14, 34), between(3.6, 5.2), between(0.82, 1.20), between(0, 5));
            case GREATER_FAIRY -> new EnemyPatternProfile(variant, evenBetween(14, 18), 0.0,
                    between(82, 122), between(7.3, 9.3), between(0.86, 1.18), between(2, 8));
            case SUNFLOWER -> new EnemyPatternProfile(variant, 8 + patternRandom.nextInt(6), 0.0,
                    between(48, 104), between(6.2, 8.2), between(0.78, 1.14), between(8, 18));
        };
    }

    private int oddBetween(int minInclusive, int maxInclusive) {
        int value = minInclusive + patternRandom.nextInt(maxInclusive - minInclusive + 1);
        return value % 2 == 0 ? Math.min(maxInclusive, value + 1) : value;
    }

    private int evenBetween(int minInclusive, int maxInclusive) {
        int value = minInclusive + patternRandom.nextInt(maxInclusive - minInclusive + 1);
        return value % 2 == 0 ? value : Math.min(maxInclusive, value + 1);
    }

    private double between(double minInclusive, double maxExclusive) {
        return minInclusive + patternRandom.nextDouble() * (maxExclusive - minInclusive);
    }
}
