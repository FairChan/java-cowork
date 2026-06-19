# Moonlit Shrine Danmaku Report Outline

Footer placeholder for every report page:

`Member Name: ____________________    Contribution: ____________________`

## 15-Minute Presentation

1. Introduction, 2 minutes
   - Original bullet-hell shooting concept.
   - Objective: defeat one boss while dodging patterned bullets.

2. Game features, 4 minutes
   - Menu, HUD, scoring, lives, bombs, graze reward.
   - Stage 1 enemy waves, boss entrance, and stage-clear/game-over states.
   - Night shrine background, original player/enemy/Boss sprites, particle feedback.
   - Boss phases: ring, spiral, aimed fan.

3. OOP class design, 5 minutes
   - Inheritance: `GameObject` -> `Entity` / `Projectile`; `Entity` -> `Player` / `Boss`; `Projectile` -> `PlayerShot` / `EnemyBullet`.
   - Inheritance update: `Enemy` also extends `Entity`, so small enemies and Boss share HP/damage behavior.
   - Polymorphism: `BulletPattern` interface with three implementations; sprite rendering works through shared animation classes.
   - Encapsulation: input, collision, HUD, particles, and background are separated into focused classes.

4. JavaFX implementation, 2 minutes
   - `Canvas` rendering with `AnimationTimer`.
   - PNG spritesheets loaded from `assets/` using `AssetLoader` and `SpriteAnimation`.
   - Keyboard interactions through `InputController`.
   - Graphical feedback through particles, boss HP bar, and overlays.

5. Demo and Q&A transition, 2 minutes
   - Run `.\run.ps1`.
   - Show `.\run-tests.ps1` for logic verification.

## 5-Minute Q&A Preparation

- Why Canvas instead of many JavaFX nodes?
- How does the boss switch phases?
- Where are inheritance and polymorphism used?
- What makes the game fair and playable?
- How could the demo be extended with levels, audio, or more bosses?
- What happened with image generation? The `gpt-image-2` prompts are prepared, but the current API key returned `401 invalid_api_key`; local original fallback sprites keep the demo runnable.
