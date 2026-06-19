package moonlit;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import moonlit.engine.GameConfig;
import moonlit.engine.GameEngine;
import moonlit.engine.InputController;

/**
 * JavaFX entry point for Moonlit Shrine Danmaku.
 */
public class GameApplication extends Application {
    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        GraphicsContext graphics = canvas.getGraphicsContext2D();

        InputController input = new InputController();
        GameEngine engine = new GameEngine(input);

        StackPane root = new StackPane(canvas);
        root.setStyle("-fx-background-color: #080814;");
        Scene scene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT, Color.BLACK);
        input.attach(scene);

        stage.setTitle("Moonlit Shrine Danmaku");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        new AnimationTimer() {
            private long previousFrame = 0L;

            @Override
            public void handle(long now) {
                if (previousFrame == 0L) {
                    previousFrame = now;
                    engine.render(graphics);
                    return;
                }

                double deltaSeconds = Math.min(0.033, (now - previousFrame) / 1_000_000_000.0);
                previousFrame = now;
                engine.update(deltaSeconds);
                engine.render(graphics);
                input.endFrame();
            }
        }.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
