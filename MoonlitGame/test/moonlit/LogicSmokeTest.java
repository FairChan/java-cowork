package moonlit;

import moonlit.audio.AudioManager;
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
import java.util.HashSet;
import java.util.Set;

/**
 * Minimal logic checks for the demo. Run with Java assertions enabled.
 */
public final class LogicSmokeTest {
    private LogicSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        testAudioManagerIgnoresMissingFiles();
        testStarryBackgroundAndAudioAssetsExist();
        testDialoguePortraitAssetsExist();
        testStarryBackgroundWaveTransitions();
        testBossSwitchesToBossMusicState();
        testMidbossDialoguePausesStageUntilAdvanced();
        testFinalBossDialogueBlocksBossSpawnUntilAdvanced();
        testBossDefeatStartsPostBattleDialogueBeforeWin();
        testStarryIllusionIsDefaultStage();
        testStarryIllusionTimelineMilestones();
        testOpeningFairyProbeHoverSlotsDoNotOverlap();
        testOpeningWaveBulletProfilesHaveVariety();
        testBossHasSixFormalPhases();
        testMasterSparkLaserPushesPlayer();
        testBossDefeatConvertsBulletsToGreenItems();
        testCollisionRemovesLifeAndBullet();
        testInvincibleModePreventsBulletDamage();
        testInvincibleModePreventsLaserDamage();
        testBombClearsEnemyBulletsAndCostsBomb();
        testBossDefeatSetsWinState();
        testBossPhaseChangesAfterDamage();
        testStageOneSpawnsEnemiesBeforeBoss();
        testEnemyCanBeDestroyedByPlayerShot();
        testBossAppearsAfterStageIntroWaves();
        testStageTwoCanBeStartedForTests();
        testStageTwoSpawnsEnemiesBeforeBoss();
        testStageTwoBossAppearsAfterTimeline();
        testXOddAimedPatternSpawnsThirtySixAdditiveAcceleratingBullets();
        testXOddAimedCenterAngleTracksPlayer();
        testXOddAimedPatternUsesFrameWaits();
        testAcceleratingBulletSlowsWithoutReversing();
        testDefaultEnemyBulletIsNotAdditive();
        testBombClearsStageTwoGlowBullets();
        testRequiredAssetsExist();
        testItemIconAssetsExistAndMapToTypes();
        testResourceFragmentsExposeHudCounts();
        testMonsterSpritesheetsExist();
        testEnemiesUseRandomMonsterVisuals();
        testBothStagesSpawnMonsterVisualEnemies();
        testBombStartsCardAnimation();
        testSpellSweepDestroysTouchedEnemies();
        testSpellSweepDoesNotSkipEnemiesBetweenFrames();
        testSpellSweepDamagesBoss();
        System.out.println("LogicSmokeTest passed");
    }


    private static void testAudioManagerIgnoresMissingFiles() {
        AudioManager audio = new AudioManager();
        audio.playStageTheme();
        audio.playBossTheme();
        audio.stop();
    }

    private static void testStarryBackgroundAndAudioAssetsExist() throws Exception {
        for (int i = 1; i <= 4; i++) {
            Path path = Path.of("assets/backgrounds/starry_wave" + i + ".png");
            assert Files.exists(path) : "starry wave background missing: " + path;
            try (java.io.InputStream stream = Files.newInputStream(path)) {
                java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(stream);
                assert image != null : "background should be readable: " + path;
                assert image.getWidth() >= 620 : "background should be at least playfield width";
                assert image.getHeight() >= 652 : "background should be at least playfield height";
            }
        }
        assert Files.exists(Path.of("assets/audio/stage_starry_illusion.mp3")) : "stage BGM missing";
        assert Files.exists(Path.of("assets/audio/boss_master_spark.mp3")) : "boss BGM missing";
    }

    private static void testDialoguePortraitAssetsExist() throws Exception {
        assert Files.exists(Path.of("assets/portraits/reimu.png")) : "Reimu dialogue portrait missing";
        assert Files.exists(Path.of("assets/portraits/marisa.png")) : "Marisa dialogue portrait missing";
    }

    private static void testStarryBackgroundWaveTransitions() {
        GameEngine engine = GameEngine.createForTestsWithStageScript();
        engine.startGame();
        assert engine.getBackgroundWaveIndexForTests() == 1 : "stage should begin on wave 1 background";

        engine.updateForTests(25.0);
        assert engine.getBackgroundWaveIndexForTests() == 2 : "0:25 should switch to wave 2 background";
        assert engine.isBackgroundCloudTransitionActiveForTests() : "wave switch should start cloud transition";

        engine.updateForTests(25.0);
        assert engine.getBackgroundWaveIndexForTests() == 3 : "0:50 should switch to wave 3 background";
        assert engine.isBackgroundCloudTransitionActiveForTests() : "second wave switch should start cloud transition";

        engine.updateForTests(65.0);
        assert engine.getBackgroundWaveIndexForTests() == 4 : "1:55 should switch to wave 4 background";
        assert engine.isBackgroundCloudTransitionActiveForTests() : "third wave switch should start cloud transition";
    }

    private static void testBossSwitchesToBossMusicState() {
        GameEngine engine = GameEngine.createForTestsWithStageScript();
        engine.startGame();
        assert "stage".equals(engine.getCurrentMusicCueForTests()) : "stage BGM should be requested on start";
        engine.updateForTests(80.0);
        finishDialogue(engine);
        engine.updateForTests(90.0);
        assert engine.getState() == GameState.DIALOGUE : "final boss dialogue should start before boss BGM";
        advanceDialogueUntilMusicCue(engine, "boss");
        assert "boss".equals(engine.getCurrentMusicCueForTests()) : "boss BGM should be requested during final boss dialogue";
    }

    private static void testMidbossDialoguePausesStageUntilAdvanced() {
        GameEngine engine = GameEngine.createForTestsWithStageScript();
        engine.startGame();

        engine.updateForTests(80.0);

        assert engine.getState() == GameState.DIALOGUE : "midboss encounter should enter dialogue state";
        assert "midboss_encounter".equals(engine.getDialogueSceneIdForTests()) : "midboss dialogue scene id should be exposed";
        assert !engine.isMidBossActive() : "midboss bullet pattern should wait until dialogue finishes";

        finishDialogue(engine);

        assert engine.getState() == GameState.PLAYING : "gameplay should resume after midboss dialogue";
        assert engine.isMidBossActive() : "midboss pattern should begin after dialogue finishes";
    }

    private static void testFinalBossDialogueBlocksBossSpawnUntilAdvanced() {
        GameEngine engine = GameEngine.createForTestsWithStageScript();
        engine.startGame();

        engine.updateForTests(80.0);
        finishDialogue(engine);
        engine.updateForTests(90.0);

        assert engine.getState() == GameState.DIALOGUE : "final boss dialogue should begin at boss time";
        assert "final_boss_intro".equals(engine.getDialogueSceneIdForTests()) : "final boss dialogue scene id should be exposed";
        assert !engine.isBossActive() : "final boss should not spawn before dialogue finishes";

        finishDialogue(engine);

        assert engine.getState() == GameState.PLAYING : "gameplay should resume after final boss dialogue";
        assert engine.isBossActive() : "final boss should spawn after dialogue finishes";
    }

    private static void testBossDefeatStartsPostBattleDialogueBeforeWin() {
        GameEngine engine = GameEngine.createForTestsWithStageScript();
        engine.startGame();
        engine.updateForTests(80.0);
        finishDialogue(engine);
        engine.updateForTests(90.0);
        finishDialogue(engine);

        engine.getBoss().skipToFinalPhaseForTests(engine);
        engine.getBoss().takeDamage(engine.getBoss().getHp());
        engine.updateForTests(1.0 / 60.0);

        assert engine.getState() == GameState.DIALOGUE : "boss defeat should show post-battle dialogue before clear state";
        assert "post_battle".equals(engine.getDialogueSceneIdForTests()) : "post-battle dialogue scene id should be exposed";

        finishDialogue(engine);

        assert engine.getState() == GameState.WON : "stage should clear after post-battle dialogue finishes";
    }
    private static void testStarryIllusionIsDefaultStage() {
        GameEngine engine = GameEngine.createForTestsWithStageScript();
        engine.startGame();

        assert engine.getCurrentStageNumber() == 1 : "Starry Illusion should replace the default stage slot";
        assert "Starry Illusion".equals(engine.getCurrentStageName()) : "default stage should be Starry Illusion";
        assert Math.abs(engine.getCurrentStageBossEntranceTime() - 170.0) < 0.001
                : "final boss should enter at 2:50";
    }

    private static void testStarryIllusionTimelineMilestones() {
        GameEngine engine = GameEngine.createForTestsWithStageScript();
        engine.startGame();

        engine.updateForTests(1.0 / 60.0);
        assert engine.getStageDirectorEventCount("fairy_probe") > 0 : "opening fairy probe should start immediately";

        engine.updateForTests(25.0);
        assert engine.getStageDirectorEventCount("kedama_lock") > 0 : "kedama lockdown should start at 0:25";

        engine.updateForTests(25.0);
        assert engine.getStageDirectorEventCount("greater_fairy") > 0 : "greater fairy pressure should start at 0:50";

        engine.updateForTests(30.0);
        assert engine.getState() == GameState.DIALOGUE : "Marisa midboss should speak before the meteor shower";
        finishDialogue(engine);
        assert engine.isMidBossActive() : "Marisa midboss should enter after dialogue";

        engine.updateForTests(35.0);
        assert engine.getStageDirectorEventCount("sunflower_rage") > 0 : "sunflower laser wave should start after midboss";

        engine.updateForTests(55.0);
        assert engine.getState() == GameState.DIALOGUE : "final boss should speak before formal battle";
        finishDialogue(engine);
        assert engine.isBossActive() : "final boss should enter around 2:50";
    }

    private static void testOpeningFairyProbeHoverSlotsDoNotOverlap() {
        GameEngine engine = GameEngine.createForTestsWithStageScript();
        engine.startGame();

        engine.updateForTests(1.6);

        assert engine.getEnemyCount() == 6 : "opening fairy probe should spawn six enemies";
        for (int i = 0; i < engine.getEnemies().size(); i++) {
            Enemy first = engine.getEnemies().get(i);
            for (int j = i + 1; j < engine.getEnemies().size(); j++) {
                Enemy second = engine.getEnemies().get(j);
                double distance = Math.hypot(first.getX() - second.getX(), first.getY() - second.getY());
                assert distance >= 44.0 : "opening fairies should not overlap while hovering";
            }
        }
    }

    private static void testOpeningWaveBulletProfilesHaveVariety() {
        GameEngine engine = GameEngine.createForTestsWithStageScript();
        engine.startGame();

        engine.updateForTests(1.05);

        Set<Integer> variants = new HashSet<>();
        Set<Integer> speeds = new HashSet<>();
        for (EnemyBullet bullet : engine.getEnemyBullets()) {
            variants.add(bullet.getVariantForTests());
            speeds.add((int) Math.round(bullet.getSpeed()));
        }
        assert variants.size() >= 4 : "same-wave bullets should use varied colors";
        assert speeds.size() >= 2 : "same-wave bullets should use varied speeds";
    }
    private static void testBossHasSixFormalPhases() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        Boss boss = engine.getBoss();

        assert boss.getPhaseCount() == 6 : "formal boss should have 3 nonspells and 3 spell cards";
        assert "Nonspell 1: High-Mobility Stardust".equals(boss.getPhaseName()) : "first phase name should describe nonspell 1";
        boss.skipCurrentPhaseForTests(engine);
        assert "Spell 1: Stardust Reverie".equals(boss.getPhaseName()) : "second phase should be Stardust Reverie";
    }

    private static void testMasterSparkLaserPushesPlayer() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        engine.getBoss().skipToFinalPhaseForTests(engine);
        double before = engine.getPlayer().getX();

        engine.updateForTests(1.2);

        assert engine.getActiveLaserCount() > 0 : "Final Master Spark should create an active laser";
        assert engine.getPlayer().getX() != before : "Master Spark should push the player horizontally";
    }

    private static void testBossDefeatConvertsBulletsToGreenItems() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        engine.addEnemyBullet(new EnemyBullet(200, 200, 0, 0, 6, 1));
        engine.addEnemyBullet(new EnemyBullet(240, 240, 0, 0, 6, 2));

        engine.getBoss().skipToFinalPhaseForTests(engine);
        engine.getBoss().takeDamage(engine.getBoss().getHp());
        engine.updateForTests(1.0 / 60.0);

        assert engine.getState() == GameState.WON : "defeating formal boss should clear the stage";
        assert engine.getEnemyBulletCount() == 0 : "boss defeat should clear hostile bullets";
        assert engine.getItemCountByType("GREEN") >= 2 : "cleared bullets should become green items";
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

    private static void testInvincibleModePreventsBulletDamage() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        Player player = engine.getPlayer();
        player.setInvulnerableSeconds(0);
        engine.setInvincibleMode(true);
        int livesBefore = player.getLives();

        engine.addEnemyBullet(new EnemyBullet(player.getX(), player.getY(), 0, 0, 7, 0));
        engine.updateForTests(1.0 / 60.0);

        assert engine.isInvincibleMode() : "invincible mode should stay enabled";
        assert player.getLives() == livesBefore : "invincible mode should prevent bullet life loss";
        assert engine.getEnemyBulletCount() == 1 : "invincible mode should not consume the bullet as a hit";
    }

    private static void testInvincibleModePreventsLaserDamage() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        Player player = engine.getPlayer();
        player.setInvulnerableSeconds(0);
        engine.setInvincibleMode(true);
        int livesBefore = player.getLives();

        engine.addLaser(new moonlit.model.Laser(player.getX(), player.getY() - 40, Math.PI / 2.0,
                160, 40, 0.0, 0.5, 0, javafx.scene.paint.Color.web("#fff6a8")));
        engine.updateForTests(1.0 / 60.0);

        assert player.getLives() == livesBefore : "invincible mode should prevent laser life loss";
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

        boss.skipToFinalPhaseForTests(engine);
        boss.takeDamage(boss.getHp());
        engine.updateForTests(1.0 / 60.0);

        assert engine.getState() == GameState.WON : "defeating boss should win the game";
    }

    private static void testBossPhaseChangesAfterDamage() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        Boss boss = engine.getBoss();

        int firstPhase = boss.getPhaseIndex();
        boss.takeDamage(boss.getHp());
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
        GameEngine engine = GameEngine.createForTests();
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

        engine.updateForTests(80.0);
        finishDialogue(engine);
        engine.updateForTests(90.0);
        assert !engine.isBossActive() : "boss should wait until final dialogue finishes";
        finishDialogue(engine);

        assert engine.isBossActive() : "boss should enter after the stage wave timeline";
        assert engine.getBoss() != null : "boss instance should exist after entrance";
    }

    private static void testStageTwoCanBeStartedForTests() {
        GameEngine engine = GameEngine.createForTestsWithStageScript();

        engine.startStageTwoForTests();

        assert engine.getCurrentStageNumber() == 2 : "stage two should be selected";
        assert "Starcrossed Moon Gate".equals(engine.getCurrentStageName()) : "stage two name should be exposed";
        assert engine.getState() == GameState.PLAYING : "stage two should enter play state";
    }

    private static void testStageTwoSpawnsEnemiesBeforeBoss() {
        GameEngine engine = GameEngine.createForTestsWithStageScript();
        engine.startStageTwoForTests();

        engine.updateForTests(7.0);

        assert engine.getEnemyCount() > 0 : "stage two should spawn cross-lane enemies";
        assert !engine.isBossActive() : "stage two boss should wait for the timeline";
    }

    private static void testStageTwoBossAppearsAfterTimeline() {
        GameEngine engine = GameEngine.createForTestsWithStageScript();
        engine.startStageTwoForTests();

        engine.updateForTests(71.0);

        assert engine.isBossActive() : "stage two boss should enter after about 70 seconds";
        assert engine.getBoss() != null : "stage two boss should exist";
    }

    private static void testXOddAimedPatternSpawnsThirtySixAdditiveAcceleratingBullets() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        engine.enableStageTwoBossPatternForTests();

        engine.getBoss().update(1.0 / 60.0, engine);

        assert engine.getEnemyBulletCount() == 36 : "one X odd-aimed burst should create 36 bullets";
        for (EnemyBullet bullet : engine.getEnemyBullets()) {
            assert bullet.isAdditive() : "X odd-aimed bullets should use additive blending";
            assert bullet.getAccelerationPerSecond() < 0 : "X odd-aimed bullets should decelerate";
        }
    }

    private static void testXOddAimedCenterAngleTracksPlayer() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        engine.enableStageTwoBossPatternForTests();

        engine.getBoss().update(1.0 / 60.0, engine);

        EnemyBullet centerBullet = engine.getEnemyBullets().get(4);
        double expected = Math.atan2(engine.getPlayer().getY() - engine.getBoss().getY(),
                engine.getPlayer().getX() - engine.getBoss().getX()) - Math.PI / 4.0;
        double actual = Math.atan2(centerBullet.getVelocityY(), centerBullet.getVelocityX());
        assert Math.abs(normalizeAngle(actual - expected)) < 0.0001 : "center bullet should track the aimed X arm";
    }

    private static void testXOddAimedPatternUsesFrameWaits() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        engine.enableStageTwoBossPatternForTests();

        engine.getBoss().update(1.0 / 60.0, engine);
        for (int i = 0; i < 11; i++) {
            engine.getBoss().update(1.0 / 60.0, engine);
        }
        assert engine.getEnemyBulletCount() == 36 : "second burst should wait for frame timing";

        engine.getBoss().update(1.0 / 60.0, engine);

        assert engine.getEnemyBulletCount() == 72 : "second burst should fire after 12 frame ticks";
    }

    private static void testAcceleratingBulletSlowsWithoutReversing() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        EnemyBullet bullet = EnemyBullet.acceleratingGlow(200, 200, 160, 0, 6, 8, -180, 70);
        double before = bullet.getSpeed();

        bullet.update(1.0, engine);

        assert bullet.getSpeed() < before : "negative acceleration should slow the bullet";
        assert bullet.getSpeed() >= 70 : "bullet should not slow below its minimum speed";
        assert bullet.getVelocityX() > 0 : "bullet should not reverse direction";
    }

    private static void testDefaultEnemyBulletIsNotAdditive() {
        EnemyBullet bullet = new EnemyBullet(100, 100, 60, 0, 5, 1);

        assert !bullet.isAdditive() : "legacy enemy bullets should not use additive blending by default";
        assert bullet.getAccelerationPerSecond() == 0 : "legacy enemy bullets should not accelerate by default";
    }

    private static void testBombClearsStageTwoGlowBullets() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        engine.addEnemyBullet(EnemyBullet.acceleratingGlow(200, 200, 160, 0, 6, 8, -100, 70));

        engine.useBombForTests();

        assert engine.getEnemyBulletCount() == 0 : "bomb should clear Stage 2 glow bullets";
    }

    private static void testBombStartsCardAnimation() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();

        boolean used = engine.useBombForTests();

        assert used : "spell card should be usable";
        assert engine.getPlayer().isSpellCardActive() : "player should switch to card animation while spell is active";
    }

    private static void testSpellSweepDestroysTouchedEnemies() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        Enemy enemy = Enemy.lantern(220, 180, 0, 0);
        engine.addEnemy(enemy);

        engine.useBombForTests();
        engine.updateForTests(0.5);

        assert engine.getEnemyCount() == 0 : "spell sweep should destroy touched stage enemies";
    }

    private static void testSpellSweepDoesNotSkipEnemiesBetweenFrames() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        Enemy enemy = Enemy.charmFairy(220, 180, 0, 0);
        engine.addEnemy(enemy);

        engine.useBombForTests();
        engine.updateForTests(1.0);

        assert engine.getEnemyCount() == 0 : "spell sweep should not skip enemies between wide frame steps";
    }

    private static void testSpellSweepDamagesBoss() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        int hpBefore = engine.getBoss().getHp();

        engine.useBombForTests();
        engine.updateForTests(0.5);

        assert engine.getBoss().getHp() < hpBefore : "spell sweep should damage boss when it crosses the beam";
    }

    private static double normalizeAngle(double angle) {
        while (angle <= -Math.PI) {
            angle += Math.PI * 2.0;
        }
        while (angle > Math.PI) {
            angle -= Math.PI * 2.0;
        }
        return angle;
    }

    private static void finishDialogue(GameEngine engine) {
        int guard = 80;
        while (engine.isDialogueActiveForTests() && guard-- > 0) {
            engine.advanceDialogueForTests();
        }
        assert guard > 0 : "dialogue should finish within a bounded number of advances";
    }

    private static void advanceDialogueUntilMusicCue(GameEngine engine, String cue) {
        int guard = 80;
        while (engine.isDialogueActiveForTests() && !cue.equals(engine.getCurrentMusicCueForTests()) && guard-- > 0) {
            engine.advanceDialogueForTests();
        }
        assert cue.equals(engine.getCurrentMusicCueForTests()) : "dialogue should trigger music cue " + cue;
    }

    private static void testItemIconAssetsExistAndMapToTypes() throws Exception {
        for (moonlit.model.ItemDrop.Type type : moonlit.model.ItemDrop.Type.values()) {
            Path path = Path.of(moonlit.model.ItemDrop.iconPathForType(type));
            assert Files.exists(path) : "item icon missing: " + path;
            try (java.io.InputStream stream = Files.newInputStream(path)) {
                java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(stream);
                assert image != null : "item icon should be readable: " + path;
                assert image.getColorModel().hasAlpha() : "item icon should have transparent alpha: " + path;
            }
        }
    }

    private static void testResourceFragmentsExposeHudCounts() {
        GameEngine engine = GameEngine.createForTests();
        engine.startGame();
        int bombsBefore = engine.getPlayer().getBombs();
        int livesBefore = engine.getPlayer().getLives();

        engine.collectItem(new moonlit.model.ItemDrop(moonlit.model.ItemDrop.Type.BOMB_FRAGMENT, 120, 120));
        engine.collectItem(new moonlit.model.ItemDrop(moonlit.model.ItemDrop.Type.BOMB_FRAGMENT, 120, 120));
        assert engine.getBombFragments() == 2 : "two bomb fragments should be visible to the HUD";
        engine.collectItem(new moonlit.model.ItemDrop(moonlit.model.ItemDrop.Type.BOMB_FRAGMENT, 120, 120));
        assert engine.getBombFragments() == 0 : "third bomb fragment should reset the fragment counter";
        assert engine.getPlayer().getBombs() == bombsBefore + 1 : "third bomb fragment should add one bomb";

        engine.collectItem(new moonlit.model.ItemDrop(moonlit.model.ItemDrop.Type.LIFE_FRAGMENT, 120, 120));
        engine.collectItem(new moonlit.model.ItemDrop(moonlit.model.ItemDrop.Type.LIFE_FRAGMENT, 120, 120));
        assert engine.getLifeFragments() == 2 : "two life fragments should be visible to the HUD";
        engine.collectItem(new moonlit.model.ItemDrop(moonlit.model.ItemDrop.Type.LIFE_FRAGMENT, 120, 120));
        assert engine.getLifeFragments() == 0 : "third life fragment should reset the fragment counter";
        assert engine.getPlayer().getLives() == livesBefore + 1 : "third life fragment should add one life";
    }
    private static void testMonsterSpritesheetsExist() {
        for (int i = 1; i <= 8; i++) {
            Path path = Path.of("assets/sprites/monster" + i + ".png");
            assert Files.exists(path) : "monster" + i + " spritesheet missing";
            assert SpriteAnimation.isFrameCompatible(path, 46) : "monster" + i + " frames invalid";
        }
    }

    private static void testEnemiesUseRandomMonsterVisuals() {
        Set<String> paths = new HashSet<>();
        for (int i = 0; i < 16; i++) {
            paths.add(Enemy.lantern(120, 120, 0, 0).getVisualSpritePath());
        }
        assert paths.size() > 1 : "enemy visuals should be randomized across monster sprites";
        for (String path : paths) {
            assert path.startsWith("assets/sprites/monster") : "enemy should use imported monster sprites";
        }
    }
    private static void testBothStagesSpawnMonsterVisualEnemies() {
        GameEngine stageOne = GameEngine.createForTestsWithStageScript();
        stageOne.startGame();
        stageOne.updateForTests(6.0);
        assert stageOne.getEnemies().stream()
                .allMatch(enemy -> enemy.getVisualSpritePath().startsWith("assets/sprites/monster"))
                : "stage one enemies should use imported monster sprites";

        GameEngine stageTwo = GameEngine.createForTestsWithStageScript();
        stageTwo.startStageTwoForTests();
        stageTwo.updateForTests(7.0);
        assert stageTwo.getEnemies().stream()
                .allMatch(enemy -> enemy.getVisualSpritePath().startsWith("assets/sprites/monster"))
                : "stage two enemies should use imported monster sprites";
    }
    private static void testRequiredAssetsExist() {
        assert Files.exists(Path.of("assets/sprites/player_flight.png")) : "player flight spritesheet missing";
        assert Files.exists(Path.of("assets/sprites/player_card.png")) : "player spell card spritesheet missing";
        assert Files.exists(Path.of("assets/sprites/enemy_lantern.png")) : "lantern enemy spritesheet missing";
        assert Files.exists(Path.of("assets/sprites/enemy_charm_fairy.png")) : "charm fairy spritesheet missing";
        assert Files.exists(Path.of("assets/sprites/boss_normal.png")) : "boss normal spritesheet missing";
        assert Files.exists(Path.of("assets/sprites/boss_abnormal.png")) : "boss abnormal spritesheet missing";
        assert Files.exists(Path.of("assets/backgrounds/stage1_moonlit_shrine.png")) : "stage background missing";
        assert SpriteAnimation.isFrameCompatible(Path.of("assets/sprites/player_flight.png"), 61) : "player frames invalid";
        assert SpriteAnimation.isFrameCompatible(Path.of("assets/sprites/player_card.png"), 61) : "player spell card frames invalid";
        assert SpriteAnimation.isFrameCompatible(Path.of("assets/sprites/boss_normal.png"), 46) : "boss normal frames invalid";
        assert SpriteAnimation.isFrameCompatible(Path.of("assets/sprites/boss_abnormal.png"), 46) : "boss abnormal frames invalid";
    }
}




