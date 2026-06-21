# Starry Illusion JavaFX Danmaku

`Starry Illusion / 穿梭于星屑的魔法使` is the formal version of the JavaFX bullet-hell course demo. It keeps the existing Canvas, `AnimationTimer`, 60 FPS frame coroutine tools, imported sprite sheets, additive glowing bullets, decelerating projectile infrastructure, portrait dialogue scenes, and the full stage timeline with a six-phase Marisa boss battle.

The game is inspired by Touhou-style danmaku structure and by Sparen's Danmaku Design Studio ideas such as Bullet, Group, Subpattern, Pattern, odd-way aimed shots, rings, stacks, and speed-changing bullets. Code, stage scripting, and classroom demo assets in this repository are original project work.

## How to Run

Requirements:

- Windows PowerShell
- JDK available on PATH or under `%USERPROFILE%\.jdks`
- OpenJFX 21.0.8 under `.deps/`

Commands:

```powershell
.\compile.ps1
.\run-tests.ps1
.\run.ps1
```

You can also double-click `run.bat` on Windows. The scripts use the local JavaFX jars on the classpath with `javafx.controls` and `javafx.media`. The current demo includes local course-demo BGM under `assets/audio/`; if those files are removed, the game falls back to silence.

Local BGM paths:

- `assets/audio/stage_starry_illusion.mp3`
- `assets/audio/boss_master_spark.mp3`

## Controls

- Move: Arrow keys or WASD
- Shoot: `Z`
- Dream Seal spell bomb: `X`
- Focus / slow movement: `Shift` (adds a red hitbox dot over the normal Reimu animation)
- Pause: `P`
- Invincible demo mode: `I` toggles invincibility on/off. When ON, bullets and lasers no longer remove lives, but movement, shooting, graze, item pickup, laser push, and scoring still work.
- Start / retry: `Enter`
- Advance dialogue: `Z` or `Enter`
- Auxiliary Stage 2 test: `2` from the menu

## Formal Stage: Starry Illusion

Stage 1 is now the main entry. Its background uses four seamless vertical images that scroll continuously to simulate high-speed flight. Existing star motion remains layered over the art, and cloud fog covers each major wave transition. The final boss slows the fourth background and adds the existing cosmic magic-circle mood.

Timeline:

1. `00:00 - 00:25` Fairy probe over `starry_wave1`: U-shaped fairy entries, short hover, odd aimed shots.
2. `00:25 - 00:50` Kedama lock over `starry_wave2`: fast Z-shaped side sweeps that leave bullet walls.
3. `00:50 - 01:20` Greater fairies over `starry_wave3`: heavy 16-way rings plus random downward shots.
4. `01:20 - 01:55` Midboss Meteoric Shower over `starry_wave3`: a portrait dialogue encounter pauses the stage, then falling star bullets begin with acceleration variety.
5. `01:55 - 02:50` Sunflower rage over `starry_wave4`: laser warnings, active lasers, and slow oval pressure.
6. `02:50+` Final Marisa boss battle: pre-boss dialogue cuts into the boss BGM before battle starts.

Resource drops include small power, big power, blue score items, green bullet-clear items, bomb fragments, life fragments, and full life drops. Items auto-attract near the top of the playfield or near the player.

Story scenes use `assets/portraits/reimu.png` and `assets/portraits/marisa.png` in a Touhou-style dialogue box. Gameplay objects freeze during dialogue while the background, music, and particles keep moving. The post-battle dialogue appears before `STAGE CLEAR`.

## Boss Battle

The formal boss uses `BossPhase` records instead of the old three-threshold HP switch. Each phase has a name, HP, time limit, movement behavior, pattern object, and reward handling.

Boss phases:

1. `Nonspell 1: High-Mobility Stardust` - figure-eight movement and three red/blue decelerating rings.
2. `Spell 1: Stardust Reverie` - twin magic emitters fire bouncing stars that split on wall reflection, with periodic aimed lasers.
3. `Nonspell 2: Light and Heat` - teleporting side pressure, red warning-line lasers, and yellow noise bullets.
4. `Spell 2: Non-Directional Laser` - six rotating laser emitters plus dense sine-like star waves.
5. `Nonspell 3: Magic Circle Storm` - random magic-circle bursts across the screen.
6. `Final Spell: Final Master Spark` - delayed tracking, a wide vertical Master Spark laser with push force, and crossing side star streams.

The older `XOddAimedPattern` remains as an auxiliary Danmakufu-style script test: 4 diagonal arms times 9-way odd spread, frame-wait timing, additive blending, and negative acceleration.

## OOP Class Design

- `GameApplication`: JavaFX entry point, window, scene, canvas, and loop.
- `GameEngine`: central game-state manager for objects, resources, update order, rendering order, win/loss, and Dream Seal sweep.
- `GameState`: menu, play, dialogue, pause, win, and loss states.
- `GameObject`: abstract base for updatable/renderable objects.
- `Entity`: HP-bearing base class for player, enemies, and boss.
- `Player`: movement, focus, shooting, bombs, lives, invulnerability, hitbox marker, and spell animation.
- `Enemy`: polymorphic enemy kinds and movement styles for fairy, kedama, greater fairy, and sunflower waves.
- `Boss` and `BossPhase`: six-phase boss queue, phase movement, timeouts, and pattern switching.
- `Projectile`, `PlayerShot`, `EnemyBullet`, `BouncingEnemyBullet`: polymorphic projectile behavior.
- `BulletPattern`: strategy interface for boss attacks, including frame-coroutine patterns.
- `StageDirector`, `LevelManager`, `StageScript`, `EnemyWave`: formal stage timeline and auxiliary stage scripting.
- `Laser`: warning/active beam object with width, duration, collision, and push force.
- `ItemDrop`: collectible resources with attraction and scoring effects.
- `AudioManager`: optional local BGM playback with missing-file no-op behavior and dialogue-triggered cue changes.
- `DialogueScene`, `DialogueLine`, `DialogueController`, `DialogueRenderer`: story scripting, portrait dialogue state, and Canvas dialogue UI.
- `AssetLoader`, `SpriteAnimation`, `AnimatedSpriteRenderer`: PNG sprite loading and animation.
- `FrameTaskScheduler`, `FrameCoroutine`: lightweight 60 FPS `waitFrames`-style coroutine layer.
- `CollisionSystem`, `HudRenderer`, `ParticleSystem`, `StarfieldBackground`: separated collision, UI, effects, and background components.

## Asset Notes

Current animation sheets were imported from local frame folders and are already wired into gameplay:

- `assets/sprites/player_flight.png` from `output/pixel_assets/reimu/frames`
- `assets/sprites/player_card.png` from `output/pixel_assets/reimu-card/frames`
- `assets/sprites/boss_normal.png` from `output/pixel_assets/boss_normal/frames`
- `assets/sprites/boss_abnormal.png` from `output/pixel_assets/boss_abnormal/frames`
- `assets/sprites/monster1.png` through `assets/sprites/monster8.png` from `output/pixel_assets/monster*/frames`
- `assets/backgrounds/starry_wave1.png` through `assets/backgrounds/starry_wave4.png` from `output/background1.png` through `output/background4.png`
- `assets/backgrounds/moonlit_shrine_path.png` is the active night sakura shrine path gameplay background.
- `assets/audio/stage_starry_illusion.mp3` and `assets/audio/boss_master_spark.mp3` from the user-provided local MP3 files
- `assets/portraits/reimu.png` and `assets/portraits/marisa.png` from `output/reimu.png` and `output/boss.png`
- `assets/items/*.png` are recolored transparent PNG icons based on Game-icons.net assets: `power-lightning`, `crystal-bars`, `emerald`, `soul`, and `glass-heart`. Game-icons.net assets are licensed under CC BY 3.0; attribution: https://game-icons.net/

Enemies randomly select one of the eight monster sprite sheets while keeping their gameplay behavior. Same-wave enemies now receive deterministic randomized bullet profiles for color, count, speed, spacing, and spawn jitter, so the demo stays reproducible without looking identical every time.

## Verification

Primary checks:

```powershell
.\run-tests.ps1
.\compile.ps1
.\run.ps1
```

`LogicSmokeTest` covers default stage selection, dialogue gates, portrait assets, timeline milestones, six boss phases, Master Spark laser push, bullet-clear item conversion, collision, bombs, Stage 2 frame script behavior, sprite sheet compatibility, and Dream Seal sweep behavior.

## Presentation Path

1. Introduce `Starry Illusion` and the objective.
2. Show controls, focus hitbox, shooting, graze, Dream Seal, and item drops.
3. Demonstrate the five stage timeline sections and the midboss/final boss dialogue gates.
4. Explain the six-phase boss queue and show Master Spark.
5. Open class design: inheritance, polymorphism, encapsulation, and separate renderer/logic systems.
6. Run `run-tests.ps1` to demonstrate verification.
