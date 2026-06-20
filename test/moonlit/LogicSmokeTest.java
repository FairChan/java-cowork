package moonlit;

import moonlit.engine.GameEngine;
import moonlit.engine.GameState;
import moonlit.model.Boss;
import moonlit.model.Enemy;
import moonlit.model.EnemyBullet;
import moonlit.model.Player;
import moonlit.model.PlayerShot;
import moonlit.render.SpriteAnimation;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Minimal logic checks for the demo. Run with Java assertions enabled.
 */
public final class LogicSmokeTest {
    private LogicSmokeTest() {
    }

    public static void main(String[] args) {
        testCollisionRemovesLifeAndBullet();
        testBombClearsEnemyBulletsAndCostsBomb();
        testBossDefeatSetsWinState();
        testBossPhaseChangesAfterDamage();
        testStageOneSpawnsEnemiesBeforeBoss();
        testEnemyCanBeDestroyedByPlayerShot();
        testBossAppearsAfterStageIntroWaves();
        testRequiredAssetsExist();
        testBombStartsCardAnimation();
        testSpellSweepDestroysTouchedEnemies();
        testSpellSweepDamagesBoss();
        System.out.println("LogicSmokeTest passed");
    }

    private static void testCollisionRemovesLifeAndBullet() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        Player player = engine.getPlayer();
        player.setInvulnerableSeconds(0);
        int livesBefore = player.getLives();

        engine.addEnemyBullet(new EnemyBullet(player.getX(), player.getY(), 0, 0, 7, 0));
        engine.updateForTests(1.0 / 60.0);

        assert player.getLives() == livesBefore - 1 : "player should lose exactly one life";
        assert engine.getEnemyBulletCount() == 0 : "colliding bullet should be removed";
    }

    private static void testBombClearsEnemyBulletsAndCostsBomb() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        Player player = engine.getPlayer();
        int bombsBefore = player.getBombs();

        engine.addEnemyBullet(new EnemyBullet(100, 100, 0, 0, 7, 0));
        engine.addEnemyBullet(new EnemyBullet(150, 120, 0, 0, 7, 0));

        boolean used = engine.useBombForTests();

        assert used : "bomb should be usable";
        assert player.getBombs() == bombsBefore - 1 : "bomb count should decrease";
        assert engine.getEnemyBulletCount() == 0 : "bomb should clear all enemy bullets";
    }

    private static void testBossDefeatSetsWinState() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        Boss boss = engine.getBoss();

        boss.takeDamage(boss.getHp());
        engine.updateForTests(1.0 / 60.0);

        assert engine.getState() == GameState.WON : "defeating boss should win the game";
    }

    private static void testBossPhaseChangesAfterDamage() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        Boss boss = engine.getBoss();

        int firstPhase = boss.getPhaseIndex();
        boss.takeDamage(460);
        boss.update(1.0 / 60.0, engine);

        assert boss.getPhaseIndex() > firstPhase : "boss should advance to a harder phase";
    }

    private static void testStageOneSpawnsEnemiesBeforeBoss() {
        GameEngine engine = GameEngine.createForTestsWithStageScript();
        engine.startGame();

        engine.updateForTests(6.0);

        assert engine.getEnemyCount() > 0 : "stage script should spawn first enemy wave";
        assert !engine.isBossActive() : "boss should not be active during opening waves";
    }

    private static void testEnemyCanBeDestroyedByPlayerShot() {
        GameEngine engine = GameEngine.createForTestsWithStageScript();
        engine.startGame();
        Enemy enemy = Enemy.lantern(220, 180, 0, 0);
        engine.addEnemy(enemy);
        int scoreBefore = engine.getScore();

        engine.addPlayerShot(new PlayerShot(enemy.getX(), enemy.getY()));
        engine.updateForTests(1.0 / 60.0);

        assert engine.getEnemyCount() == 0 : "enemy should be removed after lethal player shot";
        assert engine.getScore() > scoreBefore : "destroying enemy should add score";
    }

    private static void testBossAppearsAfterStageIntroWaves() {
        GameEngine engine = GameEngine.createForTestsWithStageScript();
        engine.startGame();

        engine.updateForTests(66.0);

        assert engine.isBossActive() : "boss should enter after the stage wave timeline";
        assert engine.getBoss() != null : "boss instance should exist after entrance";
    }


    private static void testBombStartsCardAnimation() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();

        boolean used = engine.useBombForTests();

        assert used : "spell card should be usable";
        assert engine.getPlayer().isSpellCardActive() : "player should switch to card animation while spell is active";
    }

    private static void testSpellSweepDestroysTouchedEnemies() {
        GameEngine engine = GameEngine.createForTestsWithStageScript();
        engine.startGame();
        Enemy enemy = Enemy.lantern(220, 180, 0, 0);
        engine.addEnemy(enemy);

        engine.useBombForTests();
        engine.updateForTests(0.5);

        assert engine.getEnemyCount() == 0 : "spell sweep should destroy touched stage enemies";
    }

    private static void testSpellSweepDamagesBoss() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        int hpBefore = engine.getBoss().getHp();

        engine.useBombForTests();
        engine.updateForTests(0.5);

        assert engine.getBoss().getHp() < hpBefore : "spell sweep should damage boss when it crosses the beam";
    }
    private static void testRequiredAssetsExist() {
        assert Files.exists(Path.of("assets/sprites/player_flight.png")) : "player flight spritesheet missing";
        assert Files.exists(Path.of("assets/sprites/player_focus.png")) : "player focus spritesheet missing";
        assert Files.exists(Path.of("assets/sprites/player_card.png")) : "player spell card spritesheet missing";
        assert Files.exists(Path.of("assets/sprites/enemy_lantern.png")) : "lantern enemy spritesheet missing";
        assert Files.exists(Path.of("assets/sprites/enemy_charm_fairy.png")) : "charm fairy spritesheet missing";
        assert Files.exists(Path.of("assets/sprites/boss_moon_spirit.png")) : "boss spritesheet missing";
        assert Files.exists(Path.of("assets/backgrounds/stage1_moonlit_shrine.png")) : "stage background missing";
        assert SpriteAnimation.isFrameCompatible(Path.of("assets/sprites/player_flight.png"), 61) : "player frames invalid";
        assert SpriteAnimation.isFrameCompatible(Path.of("assets/sprites/player_card.png"), 61) : "player spell card frames invalid";
        assert SpriteAnimation.isFrameCompatible(Path.of("assets/sprites/boss_moon_spirit.png"), 6) : "boss frames invalid";
    }
}

