# Moonlit Shrine Danmaku

Moonlit Shrine Danmaku is an original JavaFX bullet-hell shooting demo for an OOP course presentation. It is inspired by the readable pattern design of open-source danmaku games, but all code, characters, effects, and visual elements in this demo are newly written for this project.

## How to Run

Requirements:

- Windows PowerShell
- JDK 21 with `java` and `javac` on PATH
- Internet access on first compile, so `compile.ps1` can download OpenJFX 21.0.8 into `.deps/`

Commands:

```powershell
.\compile.ps1
.\run-tests.ps1
.\run.ps1
```

## Controls

- Move: Arrow keys or WASD
- Shoot: Z
- Bomb: X
- Focus / slow movement: Shift
- Pause: P
- Start / retry: Enter

## Stage 1: Moonlit Approach

The first level is now a complete stage flow:

1. Opening movement window.
2. Lantern spirit waves from the left and right.
3. Charm-paper fairy waves with aimed shots.
4. Mixed pressure waves before the boss.
5. Moon Spirit boss entrance after about 65 seconds.
6. Three boss phases: Lantern Ring, Moon Spiral, and Homing Petals.

Sprites and the stage background live under `assets/`. The current project includes original generated fallback PNG assets so the demo runs even when the image API is unavailable. The `tmp/imagegen/stage1_prompts.jsonl` file keeps the `gpt-image-2` prompts used for the intended asset-generation pass.

## Game Rules

Survive the first enemy waves, then defeat the moon spirit boss before losing all lives. The player automatically fires while Z is held. Enemy bullets only hit the small focus point, so holding Shift helps with precise dodging. Bombs clear all hostile bullets and give a short invulnerability window. Grazing bullets awards bonus score.

Win condition: reduce the boss HP to zero.

Lose condition: the player loses all lives.

## OOP Class Design

- `GameApplication`: JavaFX entry point that creates the window, canvas, scene, input controller, and animation loop.
- `GameEngine`: central game-state manager that owns object lists, score, update order, rendering order, and win/lose transitions.
- `GameState`: enum for menu, play, pause, win, and loss states.
- `GameObject`: abstract base for anything that updates and renders.
- `Entity`: abstract subclass for objects with HP.
- `Player`: encapsulates movement, focus mode, shooting, bombs, lives, and invulnerability.
- `Enemy`: encapsulates stage enemies, movement, HP, and attack behavior.
- `Boss`: encapsulates HP, movement, phase changes, and active bullet pattern.
- `Projectile`, `PlayerShot`, `EnemyBullet`: inheritance and polymorphism for different projectile behavior.
- `BulletPattern`: strategy interface for boss attacks.
- `RingPattern`, `SpiralPattern`, `AimedFanPattern`: polymorphic boss attack implementations.
- `LevelManager`, `StageScript`, `EnemyWave`: timed Stage 1 wave scripting and boss entrance.
- `AssetLoader`, `SpriteAnimation`, `AnimatedSpriteRenderer`: JavaFX PNG sprite loading and frame animation.
- `CollisionSystem`: resolves player shots, enemy bullet hits, and graze scoring.
- `HudRenderer`, `ParticleSystem`, `StarfieldBackground`: graphical components for the interface and visual feedback.

## Image Asset Pipeline

The planned asset workflow is:

1. Generate original character and background art with `gpt-image-2`.
2. Use a flat `#00ff00` chroma-key background for spritesheets.
3. Remove the green key locally and save transparent PNG spritesheets.
4. Load the final PNGs in JavaFX through `AssetLoader`.

During implementation, the configured API key returned `401 invalid_api_key`, so the checked-in assets were produced by `tools/create_fallback_assets.py`. The game code already consumes PNG spritesheets, so replacing these files with successful `gpt-image-2` outputs later does not require changing gameplay code.

## Presentation Notes

Suggested demo path:

1. Open with the game concept and objective.
2. Show the menu and controls.
3. Demonstrate movement, focus mode, shooting, grazing, bombs, and the enemy waves.
4. Explain how `LevelManager` spawns the stage and how the `BulletPattern` interface supports polymorphism.
5. End by showing the boss, win or lose state, and the logic smoke test.
