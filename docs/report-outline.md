# Starry Illusion Report Outline

Footer placeholder for every report page:

`Member Name: ____________________    Contribution: ____________________`

## 15-Minute Presentation

Current route note: after merging the cowork `MoonlitGame` folder, present Stage 1 as a three-boss route: `Kitsune Envoy`, `Lantern Butterfly`, then the six-phase `Star Oracle` final boss. Older midboss wording in historical notes should be treated as design context only.

1. Introduction, 2 minutes
   - Formal game title: `Starry Illusion / 穿梭于星屑的魔法使`.
   - Objective: guide Reimu through a high-speed danmaku stage and defeat Marisa's six-phase boss battle.

2. Game features, 4 minutes
   - Cowork title screen, dual-panel HUD, scoring, lives, bombs, graze reward, power and resource drops.
   - Story scenes: midboss encounter, final boss intro, and post-battle dialogue with Reimu/Marisa portraits.
   - Stage timeline: fairy probe, kedama lock, greater fairy pressure, midboss meteor shower, sunflower lasers, final boss.
   - Visual feedback: shrine gate title screen, imported sprites, left HUD mood portrait, portrait dialogue UI, four seamless scrolling backgrounds, cloud-fog wave transitions, retained star overlay, laser warnings, additive glowing bullets, particles.
   - Win/loss conditions and stage clear behavior.

3. OOP class design, 5 minutes
   - Inheritance: `GameObject` -> `Entity` / `Projectile`; `Entity` -> `Player` / `Enemy` / `Boss`; `Projectile` -> `PlayerShot` / `EnemyBullet` / `BouncingEnemyBullet`.
   - Polymorphism: `BulletPattern` implementations, `BossPhase` queue, enemy movement/attack kinds, projectile subclasses, and dialogue cues.
   - Encapsulation: input, collision, HUD, dialogue, audio, particles, background, stage director, and item drops are separated into focused classes.
   - Frame scripting: `FrameTaskScheduler` and `FrameCoroutine` emulate Danmakufu-style `waitFrames` timing.

4. JavaFX implementation, 2 minutes
   - `Canvas` rendering with `AnimationTimer`.
   - PNG spritesheets and portraits loaded from `assets/` using `AssetLoader` and `SpriteAnimation`.
   - JavaFX rewrite of the cowork cover screen using `TitleScreenRenderer`.
   - `javafx.media` BGM playback from local files, with Boss music triggered during dialogue.
   - Graphical feedback through HUD bars, dialogue overlays, portraits, lasers, additive bullets, and particles.

5. Demo and Q&A transition, 2 minutes
   - Run `.\run.ps1`.
   - Show the START transition, dialogue advancement, focus movement, Dream Seal sweep, item pickup, a dense wave, and a Boss phase.
   - Show `.\run-tests.ps1` for logic verification.

## 5-Minute Q&A Preparation

- Why Canvas instead of many JavaFX nodes?
- How does `BossPhase` improve on simple HP thresholds?
- Where are inheritance, polymorphism, and encapsulation used?
- How does the formal timeline and dialogue gating keep the stage from feeling repetitive?
- How does the X-shaped odd aimed script map Danmakufu-style frame waits into JavaFX?
- How can the project be extended with licensed music, more stages, or more sprite states?
