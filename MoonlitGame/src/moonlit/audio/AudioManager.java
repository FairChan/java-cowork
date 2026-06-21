package moonlit.audio;

import java.nio.file.Files;
import java.nio.file.Path;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Optional local BGM loader with safe no-op fallback and simple crossfade support.
 */
public class AudioManager {
    private static final Path STAGE_THEME = Path.of("assets/audio/stage_starry_illusion.mp3");
    private static final Path BOSS_THEME = Path.of("assets/audio/boss_master_spark.mp3");
    private static final Path MIDBOSS_THEME = Path.of("assets/audio/midboss_theme.mp3");
    private static final double CROSSFADE_SECONDS = 1.35;

    private MediaPlayer currentPlayer;
    private MediaPlayer fadingPlayer;
    private double fadeSecondsRemaining;
    private String currentCue = "none";

    public void playStageTheme() {
        playCue("stage", STAGE_THEME);
    }

    public void playBossTheme() {
        playCue("boss", BOSS_THEME);
    }

    public void playBossThemeImmediate() {
        playCue("boss", BOSS_THEME, true);
    }

    public void playMidbossTheme() {
        if (Files.exists(MIDBOSS_THEME)) {
            playCue("midboss", MIDBOSS_THEME);
        }
    }

    public void update(double deltaSeconds) {
        if (fadeSecondsRemaining <= 0) {
            return;
        }
        fadeSecondsRemaining = Math.max(0, fadeSecondsRemaining - deltaSeconds);
        double progress = 1.0 - fadeSecondsRemaining / CROSSFADE_SECONDS;
        if (currentPlayer != null) {
            currentPlayer.setVolume(progress);
        }
        if (fadingPlayer != null) {
            fadingPlayer.setVolume(1.0 - progress);
            if (fadeSecondsRemaining <= 0) {
                fadingPlayer.stop();
                fadingPlayer.dispose();
                fadingPlayer = null;
            }
        }
    }

    public void stop() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer.dispose();
            currentPlayer = null;
        }
        if (fadingPlayer != null) {
            fadingPlayer.stop();
            fadingPlayer.dispose();
            fadingPlayer = null;
        }
        fadeSecondsRemaining = 0;
        currentCue = "none";
    }

    public String getCurrentCueForTests() {
        return currentCue;
    }

    public boolean isStageThemeAvailableForTests() {
        return Files.exists(STAGE_THEME);
    }

    public boolean isBossThemeAvailableForTests() {
        return Files.exists(BOSS_THEME);
    }

    private void playCue(String cue, Path path) {
        playCue(cue, path, false);
    }

    private void playCue(String cue, Path path, boolean immediate) {
        currentCue = cue;
        if (!Files.exists(path) || !Platform.isFxApplicationThread()) {
            return;
        }
        try {
            MediaPlayer next = createLoopingPlayer(path);
            if (immediate && currentPlayer != null) {
                currentPlayer.stop();
                currentPlayer.dispose();
                currentPlayer = null;
            }
            fadingPlayer = immediate ? null : currentPlayer;
            currentPlayer = next;
            currentPlayer.setVolume(fadingPlayer == null ? 1.0 : 0.0);
            currentPlayer.play();
            fadeSecondsRemaining = fadingPlayer == null ? 0 : CROSSFADE_SECONDS;
        } catch (RuntimeException | LinkageError ignored) {
            // Logic tests can run outside the JavaFX media runtime. The cue state still records intent.
        }
    }

    private MediaPlayer createLoopingPlayer(Path path) {
        Media media = new Media(path.toAbsolutePath().toUri().toString());
        MediaPlayer player = new MediaPlayer(media);
        player.setCycleCount(MediaPlayer.INDEFINITE);
        return player;
    }
}
