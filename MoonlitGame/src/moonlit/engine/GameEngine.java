package moonlit.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import moonlit.audio.AudioManager;
import moonlit.dialogue.DialogueController;
import moonlit.dialogue.DialogueLine;
import moonlit.dialogue.DialogueRenderer;
import moonlit.dialogue.DialogueScene;
import moonlit.dialogue.DialogueScripts;
import moonlit.model.Boss;
import moonlit.model.Enemy;
import moonlit.model.EnemyBullet;
import moonlit.model.ItemDrop;
import moonlit.model.Laser;
import moonlit.model.Player;
import moonlit.model.PlayerShot;
import moonlit.render.HudRenderer;
import moonlit.render.ParticleSystem;
import moonlit.render.StarfieldBackground;
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
        state = GameState.PLAYING;
        audioManager.playStageTheme();
    }

    public void update(double deltaSeconds) {
        updatePortraitMood(deltaSeconds);
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
        if (stageScriptEnabled) {
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

        background.render(graphics);
        renderPlayfieldFrame(graphics);

        if (state == GameState.MENU) {
            renderMenu(graphics);
        } else {
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
        if (bossActive) {
            return;
        }
        boss = new Boss(GameConfig.PLAYFIELD_X + GameConfig.PLAYFIELD_WIDTH / 2.0, 116, getCurrentStageNumber());
        bossActive = true;
        enemyBullets.clear();
        lasers.clear();
        background.setBossMode(true);
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

    private static boolean isPowerUpItem(ItemDrop item) {
        return switch (item.getType()) {
            case SMALL_POWER, BIG_POWER, BOMB_FRAGMENT, LIFE_FRAGMENT, LIFE -> true;
            case BLUE, GREEN -> false;
        };
    }

    private void handleBossDefeat() {
        double bossX = boss.getX();
        double bossY = boss.getY();
        convertEnemyBulletsToGreenItems();
        enemyBullets.clear();
        lasers.clear();
        particles.spawnBurst(bossX, bossY, Color.web("#fff6a8"), 80);
        addItem(new ItemDrop(ItemDrop.Type.LIFE, bossX, bossY));
        score += 20_000;
        if (stageScriptEnabled && getCurrentStageNumber() == 1) {
            bossActive = false;
            startDialogue(DialogueScripts.postBattle(() -> state = GameState.WON));
        } else {
            state = GameState.WON;
        }
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
        graphics.save();
        double x = GameConfig.PLAYFIELD_X + 92;
        double y = 108;
        double w = GameConfig.PLAYFIELD_WIDTH - 184;
        double h = 438;
        drawGlassPanel(graphics, x, y, w, h, 0.80);
        drawCrescent(graphics, x + 58, y + 66, 25);

        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setFill(Color.web("#fff6d5"));
        graphics.setFont(HudRenderer.titleFont());
        graphics.fillText("Starry Illusion", x + w / 2.0 + 14, y + 82);
        drawSparkLine(graphics, x + 56, y + 112, w - 112);

        graphics.setFill(Color.web("#ffd987"));
        graphics.setFont(HudRenderer.largeFont());
        graphics.fillText("Press Enter to Start", x + w / 2.0, y + 172);

        graphics.setFill(Color.web("#d9dcff"));
        graphics.setFont(HudRenderer.bodyFont());
        graphics.fillText("Arrow / WASD  Move", x + w / 2.0, y + 236);
        graphics.fillText("Z  Shoot     X  Dream Seal     Shift  Focus", x + w / 2.0, y + 270);
        graphics.fillText("P  Pause     I  Toggle Invincible", x + w / 2.0, y + 304);

        graphics.setFill(Color.web("#f3cde2"));
        graphics.setFont(HudRenderer.smallSerifFont());
        graphics.fillText("Climb the moonlit shrine road and survive six spell phases.", x + w / 2.0, y + 365);
        graphics.fillText("Graze, collect stars, and keep your dreams intact.", x + w / 2.0, y + 395);
        graphics.restore();
    }

    private void renderOverlay(GraphicsContext graphics, String title, String subtitle) {
        graphics.save();
        graphics.setFill(Color.web("#05040f", 0.56));
        graphics.fillRect(GameConfig.PLAYFIELD_X, GameConfig.PLAYFIELD_Y,
                GameConfig.PLAYFIELD_WIDTH, GameConfig.PLAYFIELD_HEIGHT);

        double centerX = GameConfig.PLAYFIELD_X + GameConfig.PLAYFIELD_WIDTH / 2.0;
        double centerY = GameConfig.PLAYFIELD_Y + GameConfig.PLAYFIELD_HEIGHT * 0.39;
        drawCrescent(graphics, centerX - 108, centerY - 53, 22);
        drawSparkLine(graphics, centerX - 170, centerY - 8, 340);

        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setFill(Color.web("#fff4df"));
        graphics.setFont(HudRenderer.titleFont());
        graphics.fillText(title, centerX, centerY);
        graphics.setFill(Color.web("#f4c7de"));
        graphics.setFont(HudRenderer.largeFont());
        graphics.fillText(subtitle, centerX, centerY + 48);
        graphics.restore();
    }

    private static void drawGlassPanel(GraphicsContext graphics, double x, double y, double w, double h, double alpha) {
        graphics.setFill(Color.web("#0b0821", alpha));
        graphics.fillRoundRect(x, y, w, h, 18, 18);
        graphics.setStroke(Color.web("#ffd8a8", 0.72));
        graphics.setLineWidth(1.4);
        graphics.strokeRoundRect(x + 4, y + 4, w - 8, h - 8, 14, 14);
        graphics.setStroke(Color.web("#bda5ff", 0.45));
        graphics.setLineWidth(1.0);
        graphics.strokeRoundRect(x + 12, y + 12, w - 24, h - 24, 10, 10);
    }

    private static void drawSparkLine(GraphicsContext graphics, double x, double y, double width) {
        graphics.setStroke(Color.web("#f4a5c8", 0.55));
        graphics.setLineWidth(1.0);
        graphics.strokeLine(x + 22, y, x + width - 22, y);
        drawStar(graphics, x + 10, y, 5.0, Color.web("#ffd987"));
        drawStar(graphics, x + width - 10, y, 5.0, Color.web("#ffd987"));
        drawStar(graphics, x + width / 2.0, y, 3.0, Color.web("#fff7e8", 0.78));
    }

    private static void drawCrescent(GraphicsContext graphics, double x, double y, double r) {
        graphics.setFill(Color.web("#fff3b8"));
        graphics.fillOval(x - r, y - r, r * 2, r * 2);
        graphics.setFill(Color.web("#0b0821"));
        graphics.fillOval(x - r * 0.24, y - r * 1.04, r * 2.0, r * 2.0);
    }

    private static void drawStar(GraphicsContext graphics, double x, double y, double radius, Color color) {
        double[] xs = new double[10];
        double[] ys = new double[10];
        for (int i = 0; i < 10; i++) {
            double r = i % 2 == 0 ? radius : radius * 0.42;
            double angle = -Math.PI / 2.0 + i * Math.PI / 5.0;
            xs[i] = x + Math.cos(angle) * r;
            ys[i] = y + Math.sin(angle) * r;
        }
        graphics.setFill(color);
        graphics.fillPolygon(xs, ys, 10);
    }
}
