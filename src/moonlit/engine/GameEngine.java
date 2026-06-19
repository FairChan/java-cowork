package moonlit.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import moonlit.model.Boss;
import moonlit.model.Enemy;
import moonlit.model.EnemyBullet;
import moonlit.model.Player;
import moonlit.model.PlayerShot;
import moonlit.render.HudRenderer;
import moonlit.render.ParticleSystem;
import moonlit.render.StarfieldBackground;
import moonlit.stage.LevelManager;

/**
 * Owns the main game state, object lists, update order, and scoring.
 */
public class GameEngine {
    private final InputController input;
    private final CollisionSystem collisionSystem = new CollisionSystem();
    private final HudRenderer hudRenderer = new HudRenderer();
    private final StarfieldBackground background = new StarfieldBackground();
    private final ParticleSystem particles = new ParticleSystem();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<PlayerShot> playerShots = new ArrayList<>();
    private final List<EnemyBullet> enemyBullets = new ArrayList<>();
    private final boolean bossPatternsEnabled;
    private final boolean stageScriptEnabled;
    private final LevelManager levelManager = new LevelManager();

    private GameState state = GameState.MENU;
    private Player player;
    private Boss boss;
    private boolean bossActive;
    private int score;
    private int grazeCount;
    private double playTime;

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
        enemies.clear();
        playerShots.clear();
        enemyBullets.clear();
        particles.clear();
        resetObjects();
        score = 0;
        grazeCount = 0;
        playTime = 0;
        state = GameState.PLAYING;
    }

    public void update(double deltaSeconds) {
        if (input.consumeStartPressed() && state != GameState.PLAYING && state != GameState.PAUSED) {
            startGame();
        }
        if (input.consumePausePressed() && (state == GameState.PLAYING || state == GameState.PAUSED)) {
            state = state == GameState.PLAYING ? GameState.PAUSED : GameState.PLAYING;
        }
        if (state != GameState.PLAYING) {
            background.update(deltaSeconds);
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
        if (stageScriptEnabled) {
            levelManager.update(deltaSeconds, this);
        }
        player.update(deltaSeconds, this);
        updateEnemies(deltaSeconds);
        if (bossActive && bossPatternsEnabled) {
            boss.update(deltaSeconds, this);
        }
        updateProjectiles(deltaSeconds);
        collisionSystem.resolve(this);
        removeDead(enemies);
        removeDead(playerShots);
        removeDead(enemyBullets);
        particles.update(deltaSeconds);

        if (bossActive && boss != null && !boss.isAlive()) {
            state = GameState.WON;
            particles.spawnBurst(boss.getX(), boss.getY(), Color.web("#fff6a8"), 80);
            score += 20_000;
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
            for (PlayerShot shot : playerShots) {
                shot.render(graphics);
            }
            for (EnemyBullet bullet : enemyBullets) {
                bullet.render(graphics);
            }
            player.render(graphics);
            particles.render(graphics);
            hudRenderer.render(graphics, this);

            if (state == GameState.PAUSED) {
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

    public int getScore() {
        return score;
    }

    public int getGrazeCount() {
        return grazeCount;
    }

    public double getPlayTime() {
        return playTime;
    }

    public GameState getState() {
        return state;
    }

    public double getStageTime() {
        return stageScriptEnabled ? levelManager.getElapsedSeconds() : playTime;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public void activateBoss() {
        if (bossActive) {
            return;
        }
        boss = new Boss(GameConfig.PLAYFIELD_X + GameConfig.PLAYFIELD_WIDTH / 2.0, 116);
        bossActive = true;
        enemyBullets.clear();
        particles.spawnRing(boss.getX(), boss.getY(), Color.web("#fff6a8"), 46);
    }

    private void resetObjects() {
        levelManager.reset();
        player = new Player(
                GameConfig.PLAYFIELD_X + GameConfig.PLAYFIELD_WIDTH / 2.0,
                GameConfig.PLAYFIELD_BOTTOM - 80);
        if (stageScriptEnabled) {
            boss = null;
            bossActive = false;
        } else {
            boss = new Boss(GameConfig.PLAYFIELD_X + GameConfig.PLAYFIELD_WIDTH / 2.0, 116);
            bossActive = true;
        }
    }

    private void updateEnemies(double deltaSeconds) {
        for (Enemy enemy : enemies) {
            enemy.update(deltaSeconds, this);
        }
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
        graphics.fillText("Moonlit Shrine Danmaku", 115, 175);

        graphics.setFill(Color.web("#f7bdd7"));
        graphics.setFont(HudRenderer.largeFont());
        graphics.fillText("Press Enter to Start", 210, 245);

        graphics.setFill(Color.web("#b7c8ff"));
        graphics.setFont(HudRenderer.bodyFont());
        graphics.fillText("Move: Arrow Keys / WASD", 178, 318);
        graphics.fillText("Shoot: Z    Bomb: X    Focus: Shift    Pause: P", 134, 354);
        graphics.fillText("Defeat the moon spirit while weaving through readable bullet patterns.", 92, 405);
        graphics.fillText("Graze bullets for bonus points. Bombs clear danger in emergencies.", 113, 438);
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
