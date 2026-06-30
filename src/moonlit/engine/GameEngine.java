package moonlit.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import moonlit.audio.AudioManager;
import moonlit.dialogue.DialogueController;
import moonlit.dialogue.DialogueLine;
import moonlit.dialogue.DialogueRenderer;
import moonlit.dialogue.DialogueScene;
import moonlit.dialogue.DialogueScripts;
import moonlit.model.Boss;
import moonlit.model.Boss.Encounter;
import moonlit.model.Enemy;
import moonlit.model.EnemyBullet;
import moonlit.model.ItemDrop;
import moonlit.model.Laser;
import moonlit.model.Player;
import moonlit.model.PlayerShot;
import moonlit.render.HudRenderer;
import moonlit.render.ParticleSystem;
import moonlit.render.StarfieldBackground;
import moonlit.render.TitleScreenRenderer;
import moonlit.stage.LevelManager;

/**
 * Owns the main game state, object lists, update order, scoring, resources, and spell-card sweep.
 */
public class GameEngine {
    private static final double SPELL_SWEEP_DURATION = 1.2;
    private static final double SPELL_SWEEP_WIDTH = 220.0;
    private static final int SPELL_BOSS_DAMAGE = 240;
    private static final double HURT_PORTRAIT_SECONDS = 0.68;
    private static final double CHEER_PORTRAIT_SECONDS = 0.92;

    public enum PortraitMood {
        NORMAL,
        HURT,
        CHEER
    }

    private final InputController input;
    private final CollisionSystem collisionSystem = new CollisionSystem();
    private final HudRenderer hudRenderer = new HudRenderer();
    private final TitleScreenRenderer titleScreenRenderer = new TitleScreenRenderer();
    private final StarfieldBackground background = new StarfieldBackground();
    private final ParticleSystem particles = new ParticleSystem();
    private final DialogueController dialogueController = new DialogueController();
    private final DialogueRenderer dialogueRenderer = new DialogueRenderer();
    private final AudioManager audioManager = new AudioManager();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<PlayerShot> playerShots = new ArrayList<>();
    private final List<EnemyBullet> enemyBullets = new ArrayList<>();
    private final List<ItemDrop> items = new ArrayList<>();
    private final List<Laser> lasers = new ArrayList<>();
    private final boolean bossPatternsEnabled;
    private final boolean stageScriptEnabled;
    private final LevelManager levelManager = new LevelManager();

    private GameState state = GameState.MENU;
    private Player player;
    private Boss boss;
    private boolean bossActive;
    private int score;
    private int grazeCount;
    private double power;
    private int bombFragments;
    private int lifeFragments;
    private double playTime;
    private double spellSweepRemaining;
    private boolean spellSweepDamagedBoss;
    private boolean invincibleMode;
    private PortraitMood portraitMood = PortraitMood.NORMAL;
    private double portraitMoodSeconds;
    private boolean exitRequested;

    public GameEngine(InputController input) {
        this(input, true, true);
    }

    private GameEngine(InputController input, boolean bossPatternsEnabled, boolean stageScriptEnabled) {
        this.input = input;
        this.bossPatternsEnabled = bossPatternsEnabled;
        this.stageScriptEnabled = stageScriptEnabled;
        resetObjects();
    }

    public static GameEngine createForTests() {
        return new GameEngine(new InputController(), false, false);
    }

    public static GameEngine createForTestsWithStageScript() {
        return new GameEngine(new InputController(), false, true);
    }

    public void startGame() {
        startStage(1);
    }

    public void startStageTwoForTests() {
        startStage(2);
    }

    public void startStage(int stageNumber) {
        enemies.clear();
        playerShots.clear();
        enemyBullets.clear();
        items.clear();
        lasers.clear();
        particles.clear();
        levelManager.setStage(stageNumber);
        dialogueController.clear();
        resetObjects();
        score = 0;
        grazeCount = 0;
        power = 0;
        bombFragments = 0;
        lifeFragments = 0;
        playTime = 0;
        invincibleMode = false;
        portraitMood = PortraitMood.NORMAL;
        portraitMoodSeconds = 0;
        exitRequested = false;
        state = GameState.PLAYING;
        audioManager.playStageTheme();
    }

    public void update(double deltaSeconds) {
        updatePortraitMood(deltaSeconds);
        if (state == GameState.MENU) {
            if (input.consumeStageTwoPressed()) {
                startStage(2);
            } else {
                titleScreenRenderer.update(deltaSeconds, input, this::startGame, this::requestExit);
            }
            audioManager.update(deltaSeconds);
            particles.update(deltaSeconds);
            return;
        }
        if (state == GameState.DIALOGUE) {
            if (input.consumeDialogueAdvancePressed()) {
                advanceDialogue();
            }
            background.update(deltaSeconds);
            audioManager.update(deltaSeconds);
            particles.update(deltaSeconds);
            return;
        }
        if (state != GameState.PLAYING && state != GameState.PAUSED) {
            if (input.consumeStageTwoPressed()) {
                startStage(2);
            } else if (input.consumeStartPressed()) {
                startGame();
            }
        }
        if (input.consumePausePressed() && (state == GameState.PLAYING || state == GameState.PAUSED)) {
            state = state == GameState.PLAYING ? GameState.PAUSED : GameState.PLAYING;
        }
        if (input.consumeInvincibleTogglePressed() && (state == GameState.PLAYING || state == GameState.PAUSED)) {
            toggleInvincibleMode();
        }
        if (state != GameState.PLAYING) {
            background.update(deltaSeconds);
            audioManager.update(deltaSeconds);
            particles.update(deltaSeconds);
            return;
        }

        if (input.consumeBombPressed()) {
            useBomb();
        }
        updateForTests(deltaSeconds);
    }

    public void updateForTests(double deltaSeconds) {
        if (state != GameState.PLAYING) {
            return;
        }

        playTime += deltaSeconds;
        background.update(deltaSeconds);
        audioManager.update(deltaSeconds);
        if (stageScriptEnabled && !bossActive) {
            levelManager.update(deltaSeconds, this);
        }
        player.update(deltaSeconds, this);
        updateEnemies(deltaSeconds);
        if (bossActive && bossPatternsEnabled) {
            boss.update(deltaSeconds, this);
        }
        applySpellSweep(deltaSeconds);
        updateLasers(deltaSeconds);
        updateProjectiles(deltaSeconds);
        updateItems(deltaSeconds);
        collisionSystem.resolve(this);
        removeDead(enemies);
        removeDead(playerShots);
        removeDead(enemyBullets);
        removeDead(items);
        removeDead(lasers);
        particles.update(deltaSeconds);

        if (bossActive && boss != null && !boss.isAlive()) {
            handleBossDefeat();
        } else if (player.getLives() <= 0) {
            state = GameState.LOST;
        }
    }

    public void render(GraphicsContext graphics) {
        graphics.setFill(Color.web("#080814"));
        graphics.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);

        if (state == GameState.MENU) {
            titleScreenRenderer.render(graphics);
            return;
        }

        background.render(graphics);
        renderPlayfieldFrame(graphics);

        for (Enemy enemy : enemies) {
            enemy.render(graphics);
        }
        if (bossActive && boss != null) {
            boss.render(graphics);
        }
        renderSpellSweep(graphics);
        for (PlayerShot shot : playerShots) {
            shot.render(graphics);
        }
        for (EnemyBullet bullet : enemyBullets) {
            bullet.render(graphics);
        }
        for (Laser laser : lasers) {
            laser.render(graphics);
        }
        for (ItemDrop item : items) {
            item.render(graphics);
        }
        player.render(graphics);
        particles.render(graphics);
        hudRenderer.render(graphics, this);

        if (state == GameState.DIALOGUE) {
            dialogueRenderer.render(graphics, dialogueController);
        } else if (state == GameState.PAUSED) {
            renderOverlay(graphics, "PAUSED", "Press P to resume");
        } else if (state == GameState.WON) {
            renderOverlay(graphics, "STAGE CLEAR", "Press Enter to play again");
        } else if (state == GameState.LOST) {
            renderOverlay(graphics, "GAME OVER", "Press Enter to retry");
        }
    }

    public boolean useBombForTests() {
        return useBomb();
    }

    public boolean useBomb() {
        if (state != GameState.PLAYING || !player.useBomb()) {
            return false;
        }
        player.startSpellCard(SPELL_SWEEP_DURATION);
        spellSweepRemaining = SPELL_SWEEP_DURATION;
        spellSweepDamagedBoss = false;
        int cleared = enemyBullets.size();
        enemyBullets.clear();
        particles.spawnRing(player.getX(), player.getY(), Color.web("#8ee7ff"), 52);
        score += cleared * 40;
        return true;
    }

    public void addPlayerShot(PlayerShot shot) {
        playerShots.add(shot);
    }

    public void addEnemyBullet(EnemyBullet bullet) {
        enemyBullets.add(bullet);
    }

    public void addLaser(Laser laser) {
        lasers.add(laser);
    }

    public void addItem(ItemDrop item) {
        items.add(item);
    }

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    public void addScore(int amount) {
        score += amount;
    }

    public void addGrazeScore() {
        grazeCount++;
        score += 25;
    }

    public void collectItem(ItemDrop item) {
        switch (item.getType()) {
            case SMALL_POWER -> {
                power = Math.min(4.0, power + 0.05);
                score += 120;
            }
            case BIG_POWER -> {
                power = Math.min(4.0, power + 1.0);
                score += 800;
            }
            case BLUE -> score += 1_000;
            case GREEN -> score += 250;
            case BOMB_FRAGMENT -> {
                bombFragments++;
                score += 400;
                if (bombFragments >= 3) {
                    bombFragments = 0;
                    player.addBomb();
                }
            }
            case LIFE_FRAGMENT -> {
                lifeFragments++;
                score += 600;
                if (lifeFragments >= 3) {
                    lifeFragments = 0;
                    player.addLife();
                }
            }
            case LIFE -> {
                player.addLife();
                score += 5_000;
            }
        }
        if (isPowerUpItem(item)) {
            triggerPortraitMood(PortraitMood.CHEER, CHEER_PORTRAIT_SECONDS);
        }
    }

    public void dropEnemyRewards(Enemy enemy) {
        switch (enemy.getKind()) {
            case KEDAMA -> {
                addItem(new ItemDrop(ItemDrop.Type.BLUE, enemy.getX() - 8, enemy.getY()));
                addItem(new ItemDrop(ItemDrop.Type.BLUE, enemy.getX() + 8, enemy.getY()));
            }
            case GREATER_FAIRY -> {
                addItem(new ItemDrop(ItemDrop.Type.BIG_POWER, enemy.getX() - 12, enemy.getY()));
                addItem(new ItemDrop(ItemDrop.Type.LIFE_FRAGMENT, enemy.getX() + 12, enemy.getY()));
            }
            case SUNFLOWER -> {
                addItem(new ItemDrop(ItemDrop.Type.BOMB_FRAGMENT, enemy.getX(), enemy.getY()));
                addItem(new ItemDrop(ItemDrop.Type.SMALL_POWER, enemy.getX() - 16, enemy.getY() + 8));
                addItem(new ItemDrop(ItemDrop.Type.SMALL_POWER, enemy.getX() + 16, enemy.getY() + 8));
            }
            case CHARM_FAIRY -> {
                addItem(new ItemDrop(ItemDrop.Type.SMALL_POWER, enemy.getX() - 14, enemy.getY() - 4));
                addItem(new ItemDrop(ItemDrop.Type.SMALL_POWER, enemy.getX() + 14, enemy.getY() - 4));
                addItem(new ItemDrop(ItemDrop.Type.BLUE, enemy.getX() - 18, enemy.getY() + 10));
                addItem(new ItemDrop(ItemDrop.Type.BLUE, enemy.getX(), enemy.getY() + 14));
                addItem(new ItemDrop(ItemDrop.Type.BLUE, enemy.getX() + 18, enemy.getY() + 10));
            }
            case LANTERN -> {
                addItem(new ItemDrop(ItemDrop.Type.SMALL_POWER, enemy.getX() - 9, enemy.getY()));
                addItem(new ItemDrop(ItemDrop.Type.BLUE, enemy.getX() + 9, enemy.getY()));
            }
        }
    }

    public void dropBossPhaseReward(int phaseIndex) {
        if (boss == null) {
            return;
        }
        if (phaseIndex == 3) {
            addItem(new ItemDrop(ItemDrop.Type.LIFE_FRAGMENT, boss.getX(), boss.getY()));
        } else {
            addItem(new ItemDrop(ItemDrop.Type.BIG_POWER, boss.getX() - 15, boss.getY()));
            addItem(new ItemDrop(ItemDrop.Type.BLUE, boss.getX() + 15, boss.getY()));
        }
    }

    public void clearEnemyBullets() {
        enemyBullets.clear();
    }

    public void startDialogue(DialogueScene scene) {
        DialogueLine.Cue cue = dialogueController.begin(scene);
        state = GameState.DIALOGUE;
        applyDialogueCue(cue);
    }

    public void advanceDialogue() {
        DialogueLine.Cue cue = dialogueController.advance();
        if (!dialogueController.isActive() && state == GameState.DIALOGUE) {
            state = GameState.PLAYING;
        }
        applyDialogueCue(cue);
    }

    public boolean isDialogueActiveForTests() {
        return dialogueController.isActive();
    }

    public void advanceDialogueForTests() {
        advanceDialogue();
    }

    public String getDialogueSceneIdForTests() {
        return dialogueController.sceneId();
    }

    public ParticleSystem getParticles() {
        return particles;
    }

    public InputController getInput() {
        return input;
    }

    public Player getPlayer() {
        return player;
    }

    public Boss getBoss() {
        return boss;
    }

    public boolean isBossActive() {
        return bossActive;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public int getEnemyCount() {
        return enemies.size();
    }

    public List<PlayerShot> getPlayerShots() {
        return playerShots;
    }

    public List<EnemyBullet> getEnemyBullets() {
        return enemyBullets;
    }

    public int getEnemyBulletCount() {
        return enemyBullets.size();
    }

    public int getActiveLaserCount() {
        int count = 0;
        for (Laser laser : lasers) {
            if (laser.isAlive() && laser.isActive()) {
                count++;
            }
        }
        return count;
    }

    public int getItemCountByType(String typeName) {
        int count = 0;
        for (ItemDrop item : items) {
            if (item.isAlive() && item.getType().name().equals(typeName)) {
                count++;
            }
        }
        return count;
    }

    public int getScore() {
        return score;
    }

    public int getGrazeCount() {
        return grazeCount;
    }

    public double getPlayTime() {
        return playTime;
    }

    public double getPower() {
        return power;
    }

    public int getBombFragments() {
        return bombFragments;
    }

    public int getLifeFragments() {
        return lifeFragments;
    }

    public boolean isInvincibleMode() {
        return invincibleMode;
    }

    public PortraitMood getPortraitMood() {
        return portraitMood;
    }

    public void notifyPlayerDamaged() {
        triggerPortraitMood(PortraitMood.HURT, HURT_PORTRAIT_SECONDS);
    }

    public int getTitleSelectedMenuIndexForTests() {
        return titleScreenRenderer.getSelectedIndexForTests();
    }

    public TitleScreenRenderer.MenuAction getTitleLastActionForTests() {
        return titleScreenRenderer.getLastActionForTests();
    }

    public boolean isTitleStartTransitionActiveForTests() {
        return titleScreenRenderer.isStartTransitionActiveForTests();
    }

    public boolean isExitRequestedForTests() {
        return exitRequested;
    }

    public boolean consumeExitRequested() {
        boolean requested = exitRequested;
        exitRequested = false;
        return requested;
    }

    public void setInvincibleMode(boolean invincibleMode) {
        this.invincibleMode = invincibleMode;
    }

    public void toggleInvincibleMode() {
        invincibleMode = !invincibleMode;
    }
    public GameState getState() {
        return state;
    }

    public double getStageTime() {
        return stageScriptEnabled ? levelManager.getElapsedSeconds() : playTime;
    }

    public int getCurrentStageNumber() {
        return levelManager.getStageNumber();
    }

    public String getCurrentStageName() {
        return levelManager.getStageName();
    }

    public double getCurrentStageBossEntranceTime() {
        return levelManager.getBossEntranceTime();
    }

    public int getStageDirectorEventCount(String eventId) {
        return levelManager.getEventCount(eventId);
    }

    public boolean isMidBossActive() {
        return levelManager.isMidBossActive();
    }
    public void requestWaveBackground(int waveIndex) {
        background.requestWaveBackground(waveIndex);
    }

    public int getBackgroundWaveIndexForTests() {
        return background.getCurrentWaveIndexForTests();
    }

    public boolean isBackgroundCloudTransitionActiveForTests() {
        return background.isCloudTransitionActiveForTests();
    }

    public String getCurrentMusicCueForTests() {
        return audioManager.getCurrentCueForTests();
    }

    public void enableStageTwoBossPatternForTests() {
        levelManager.setStage(2);
        boss = new Boss(GameConfig.PLAYFIELD_X + GameConfig.PLAYFIELD_WIDTH / 2.0, 116, 2);
        bossActive = true;
        enemyBullets.clear();
        lasers.clear();
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public void activateBoss() {
        activateBossEncounter(getCurrentStageNumber() == 2 ? Encounter.STAGE_TWO : Encounter.FINAL_BOSS);
    }

    public void activateMiniBossOne() {
        activateBossEncounter(Encounter.MINI_BOSS_ONE);
    }

    public void activateMiniBossTwo() {
        activateBossEncounter(Encounter.MINI_BOSS_TWO);
    }

    public void activateFinalBoss() {
        activateBossEncounter(Encounter.FINAL_BOSS);
    }

    private void activateBossEncounter(Encounter encounter) {
        if (bossActive) {
            return;
        }
        double spawnY = encounter == Encounter.FINAL_BOSS ? 116 : 122;
        boss = new Boss(GameConfig.PLAYFIELD_X + GameConfig.PLAYFIELD_WIDTH / 2.0, spawnY, encounter);
        bossActive = true;
        enemyBullets.clear();
        lasers.clear();
        background.setBossMode(encounter == Encounter.FINAL_BOSS || encounter == Encounter.STAGE_TWO);
        audioManager.playBossTheme();
        particles.spawnRing(boss.getX(), boss.getY(), Color.web("#fff6a8"), 46);
    }

    private void resetObjects() {
        dialogueController.clear();
        spellSweepRemaining = 0;
        spellSweepDamagedBoss = false;
        levelManager.reset();
        background.setBossMode(false);
        background.requestWaveBackground(1);
        player = new Player(
                GameConfig.PLAYFIELD_X + GameConfig.PLAYFIELD_WIDTH / 2.0,
                GameConfig.PLAYFIELD_BOTTOM - 80);
        if (stageScriptEnabled) {
            boss = null;
            bossActive = false;
        } else {
            boss = new Boss(GameConfig.PLAYFIELD_X + GameConfig.PLAYFIELD_WIDTH / 2.0, 116, getCurrentStageNumber());
            bossActive = true;
        }
    }

    private void updatePortraitMood(double deltaSeconds) {
        if (portraitMoodSeconds <= 0) {
            return;
        }
        portraitMoodSeconds = Math.max(0, portraitMoodSeconds - deltaSeconds);
        if (portraitMoodSeconds == 0) {
            portraitMood = PortraitMood.NORMAL;
        }
    }

    private void triggerPortraitMood(PortraitMood mood, double seconds) {
        if (mood == PortraitMood.CHEER && portraitMood == PortraitMood.HURT && portraitMoodSeconds > 0) {
            return;
        }
        portraitMood = mood;
        portraitMoodSeconds = Math.max(0, seconds);
    }

    private void requestExit() {
        exitRequested = true;
    }

    private static boolean isPowerUpItem(ItemDrop item) {
        return switch (item.getType()) {
            case SMALL_POWER, BIG_POWER, BOMB_FRAGMENT, LIFE_FRAGMENT, LIFE -> true;
            case BLUE, GREEN -> false;
        };
    }

    private void handleBossDefeat() {
        Encounter defeatedEncounter = boss.getEncounter();
        double bossX = boss.getX();
        double bossY = boss.getY();
        convertEnemyBulletsToGreenItems();
        enemyBullets.clear();
        lasers.clear();
        particles.spawnBurst(bossX, bossY, Color.web("#fff6a8"), 80);
        dropDefeatedBossReward(defeatedEncounter, bossX, bossY);
        score += defeatedEncounter == Encounter.FINAL_BOSS || defeatedEncounter == Encounter.STAGE_TWO ? 20_000 : 8_000;
        bossActive = false;
        background.setBossMode(false);

        if (stageScriptEnabled && getCurrentStageNumber() == 1) {
            switch (defeatedEncounter) {
                case MINI_BOSS_ONE -> startDialogue(DialogueScripts.miniBossOneDefeated(() -> {
                    state = GameState.PLAYING;
                    audioManager.playStageTheme();
                }));
                case MINI_BOSS_TWO -> startDialogue(DialogueScripts.miniBossTwoDefeated(() -> {
                    state = GameState.PLAYING;
                    audioManager.playStageTheme();
                }));
                case FINAL_BOSS -> startDialogue(DialogueScripts.finalBossDefeated(() -> state = GameState.WON));
                case STAGE_TWO -> state = GameState.WON;
            }
            return;
        }
        state = GameState.WON;
    }

    private void dropDefeatedBossReward(Encounter defeatedEncounter, double bossX, double bossY) {
        if (defeatedEncounter == Encounter.FINAL_BOSS || defeatedEncounter == Encounter.STAGE_TWO) {
            addItem(new ItemDrop(ItemDrop.Type.LIFE, bossX, bossY));
            return;
        }
        addItem(new ItemDrop(ItemDrop.Type.LIFE_FRAGMENT, bossX, bossY));
        addItem(new ItemDrop(ItemDrop.Type.BIG_POWER, bossX - 24, bossY + 12));
        addItem(new ItemDrop(ItemDrop.Type.BOMB_FRAGMENT, bossX + 24, bossY + 12));
    }

    private void applyDialogueCue(DialogueLine.Cue cue) {
        switch (cue) {
            case BOSS_THEME -> audioManager.playBossThemeImmediate();
            case BOSS_THEME_SHAKE -> {
                audioManager.playBossThemeImmediate();
                spawnDialogueShakeBurst();
            }
            case STOP_MUSIC -> audioManager.stop();
            case SHAKE -> spawnDialogueShakeBurst();
            case NONE -> {
                // No cue for this line.
            }
        }
    }

    private void spawnDialogueShakeBurst() {
        double x = boss != null ? boss.getX() : GameConfig.PLAYFIELD_X + GameConfig.PLAYFIELD_WIDTH / 2.0;
        double y = boss != null ? boss.getY() : GameConfig.PLAYFIELD_Y + 150;
        particles.spawnRing(x, y, Color.web("#fff6a8"), 36);
    }

    private void updateEnemies(double deltaSeconds) {
        for (Enemy enemy : enemies) {
            enemy.update(deltaSeconds, this);
        }
    }

    private void updateLasers(double deltaSeconds) {
        for (Laser laser : lasers) {
            laser.update(deltaSeconds, this);
        }
    }

    private void updateItems(double deltaSeconds) {
        for (ItemDrop item : items) {
            item.update(deltaSeconds, this);
        }
    }

    private void applySpellSweep(double deltaSeconds) {
        if (spellSweepRemaining <= 0) {
            return;
        }
        double previousBeamX = getSpellSweepX();
        spellSweepRemaining = Math.max(0, spellSweepRemaining - deltaSeconds);
        double beamX = getSpellSweepX();
        spawnSpellSweepParticles(previousBeamX, beamX);

        double sweepMin = Math.min(previousBeamX, beamX) - SPELL_SWEEP_WIDTH / 2.0;
        double sweepMax = Math.max(previousBeamX, beamX) + SPELL_SWEEP_WIDTH / 2.0;
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && isInsideSpellSweep(enemy.getX(), enemy.getRadius(), sweepMin, sweepMax)) {
                enemy.takeDamage(enemy.getHp());
                score += enemy.getScoreValue();
                dropEnemyRewards(enemy);
                particles.spawnBurst(enemy.getX(), enemy.getY(), Color.web("#fff6a8"), 22);
            }
        }

        if (!spellSweepDamagedBoss && bossActive && boss != null && boss.isAlive()
                && isInsideSpellSweep(boss.getX(), boss.getRadius(), sweepMin, sweepMax)) {
            boss.takeDamage(SPELL_BOSS_DAMAGE);
            score += 1_500;
            spellSweepDamagedBoss = true;
            particles.spawnBurst(boss.getX(), boss.getY(), Color.web("#ffffff"), 36);
        }
    }

    private void spawnSpellSweepParticles(double previousBeamX, double beamX) {
        int columns = Math.max(2, Math.min(5, (int) (Math.abs(beamX - previousBeamX) / 90.0) + 1));
        for (int i = 0; i < columns; i++) {
            double t = columns == 1 ? 1.0 : (double) i / (columns - 1);
            double x = previousBeamX + (beamX - previousBeamX) * t;
            particles.spawnSweepColumn(x, GameConfig.PLAYFIELD_Y, GameConfig.PLAYFIELD_BOTTOM, Color.web("#fff6a8"));
        }
    }

    private boolean isInsideSpellSweep(double objectX, double objectRadius, double sweepMin, double sweepMax) {
        return objectX + objectRadius >= sweepMin && objectX - objectRadius <= sweepMax;
    }

    private double getSpellSweepX() {
        double progress = 1.0 - spellSweepRemaining / SPELL_SWEEP_DURATION;
        progress = Math.max(0, Math.min(1, progress));
        return GameConfig.PLAYFIELD_X - SPELL_SWEEP_WIDTH / 2.0
                + progress * (GameConfig.PLAYFIELD_WIDTH + SPELL_SWEEP_WIDTH);
    }

    private void renderSpellSweep(GraphicsContext graphics) {
        if (spellSweepRemaining <= 0) {
            return;
        }
        double beamX = getSpellSweepX();
        double oldAlpha = graphics.getGlobalAlpha();
        graphics.setGlobalAlpha(0.30);
        graphics.setFill(Color.web("#fff6a8"));
        graphics.fillRect(beamX - SPELL_SWEEP_WIDTH / 2.0, GameConfig.PLAYFIELD_Y,
                SPELL_SWEEP_WIDTH, GameConfig.PLAYFIELD_HEIGHT);
        graphics.setGlobalAlpha(0.72);
        graphics.setFill(Color.web("#ffffff"));
        graphics.fillRect(beamX - 18, GameConfig.PLAYFIELD_Y, 36, GameConfig.PLAYFIELD_HEIGHT);
        graphics.setGlobalAlpha(0.88);
        graphics.setStroke(Color.web("#8ee7ff"));
        graphics.setLineWidth(3);
        graphics.strokeLine(beamX - SPELL_SWEEP_WIDTH / 2.0, GameConfig.PLAYFIELD_Y,
                beamX + SPELL_SWEEP_WIDTH / 2.0, GameConfig.PLAYFIELD_BOTTOM);
        graphics.strokeLine(beamX + SPELL_SWEEP_WIDTH / 2.0, GameConfig.PLAYFIELD_Y,
                beamX - SPELL_SWEEP_WIDTH / 2.0, GameConfig.PLAYFIELD_BOTTOM);
        graphics.setGlobalAlpha(oldAlpha);
    }

    private void updateProjectiles(double deltaSeconds) {
        for (PlayerShot shot : playerShots) {
            shot.update(deltaSeconds, this);
        }
        for (EnemyBullet bullet : enemyBullets) {
            bullet.update(deltaSeconds, this);
        }
        removeDead(playerShots);
        removeDead(enemyBullets);
    }

    private void convertEnemyBulletsToGreenItems() {
        for (EnemyBullet bullet : enemyBullets) {
            if (bullet.isAlive()) {
                addItem(new ItemDrop(ItemDrop.Type.GREEN, bullet.getX(), bullet.getY()));
            }
        }
    }

    private static <T extends moonlit.model.GameObject> void removeDead(List<T> objects) {
        Iterator<T> iterator = objects.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().isAlive()) {
                iterator.remove();
            }
        }
    }

    private void renderPlayfieldFrame(GraphicsContext graphics) {
        graphics.setStroke(Color.web("#2e3f73"));
        graphics.setLineWidth(2);
        graphics.strokeRect(GameConfig.PLAYFIELD_X, GameConfig.PLAYFIELD_Y,
                GameConfig.PLAYFIELD_WIDTH, GameConfig.PLAYFIELD_HEIGHT);
        graphics.setStroke(Color.web("#8cb8ff", 0.35));
        graphics.setLineWidth(1);
        graphics.strokeRect(GameConfig.PLAYFIELD_X + 5, GameConfig.PLAYFIELD_Y + 5,
                GameConfig.PLAYFIELD_WIDTH - 10, GameConfig.PLAYFIELD_HEIGHT - 10);
    }

    private void renderMenu(GraphicsContext graphics) {
        graphics.setFill(Color.web("#0a0d24", 0.72));
        graphics.fillRect(GameConfig.PLAYFIELD_X + 45, 96, GameConfig.PLAYFIELD_WIDTH - 90, 430);

        graphics.setFill(Color.web("#f5f0ff"));
        graphics.setFont(HudRenderer.titleFont());
        graphics.fillText("Starry Illusion", 175, 175);

        graphics.setFill(Color.web("#f7bdd7"));
        graphics.setFont(HudRenderer.largeFont());
        graphics.fillText("Enter: Start Formal Stage", 170, 245);

        graphics.setFill(Color.web("#b7c8ff"));
        graphics.setFont(HudRenderer.bodyFont());
        graphics.fillText("Move: Arrow Keys / WASD", 178, 318);
        graphics.fillText("Shoot: Z    Dream Seal: X    Focus: Shift    Pause: P    I: Invincible", 72, 354);
        graphics.fillText("Climb from magic forest dusk into a high-speed nebula duel.", 116, 405);
        graphics.fillText("Defeat all six Marisa phases to clear the stage.", 151, 438);
    }

    private void renderOverlay(GraphicsContext graphics, String title, String subtitle) {
        graphics.setFill(Color.web("#02030b", 0.72));
        graphics.fillRect(GameConfig.PLAYFIELD_X, GameConfig.PLAYFIELD_Y,
                GameConfig.PLAYFIELD_WIDTH, GameConfig.PLAYFIELD_HEIGHT);
        graphics.setFill(Color.WHITE);
        graphics.setFont(HudRenderer.titleFont());
        graphics.fillText(title, GameConfig.PLAYFIELD_X + 170, 300);
        graphics.setFill(Color.web("#c9d7ff"));
        graphics.setFont(HudRenderer.largeFont());
        graphics.fillText(subtitle, GameConfig.PLAYFIELD_X + 160, 355);
    }
}
