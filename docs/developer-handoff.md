# Starry Illusion Developer Handoff

This document is for the next developer continuing the JavaFX danmaku project in `C:\Users\ssema\Desktop\java`.

## Current Status

- Branch: `codex/pixel-video-asset`
- Current HEAD observed during handoff: `ec501ca`
- Main game: `Starry Illusion` formal JavaFX danmaku stage
- Runtime: Java 21 + JavaFX 21.0.8, plain PowerShell scripts, no Maven or Gradle
- Window: fixed 900x700, playfield 620x652 plus HUD
- Main entry: `src/moonlit/GameApplication.java`
- Important warning: the working tree is intentionally dirty and includes many untracked assets/classes from recent development. Do not run destructive restore/reset commands unless the owner explicitly asks.

Latest implemented features include:

- Formal Stage 1 timeline with enemy waves, midboss dialogue, final boss dialogue, post-battle dialogue, and six Boss phases.
- Four scrolling backgrounds with cloud-fog transitions and star overlay.
- Optional local BGM with stage-to-boss music switching.
- Imported player, Boss, enemy, item, portrait, background, and audio assets.
- Item system with small P, big P, score blue, clear green, bomb fragments, life fragments, and full life.
- Deterministic enemy bullet variety via `EnemyPatternProfile`.
- Debug/demo invincible mode toggled by `I`.
- Smoke tests in `test/moonlit/LogicSmokeTest.java` plus Python tests for pixel-video asset tools.

## Quick Start

Use PowerShell from the project root:

```powershell
cd C:\Users\ssema\Desktop\java
.\compile.ps1
.\run-tests.ps1
.\run.ps1
```

If Python tests fail with `PermissionError` under the default Windows temp folder, set temp to the writable local temp directory:

```powershell
$env:TEMP='C:\tmp'
$env:TMP='C:\tmp'
.\run-tests.ps1
```

First compile downloads OpenJFX into `.deps/javafx-sdk-21.0.8`. The build output goes to `out/`.

## Controls

- Move: Arrow keys or WASD
- Shoot: `Z`
- Dream Seal spell bomb: `X`
- Focus / slow movement: `Shift`
- Pause: `P`
- Invincible demo mode: `I`
- Start / retry: `Enter`
- Advance dialogue: `Z` or `Enter`
- Auxiliary Stage 2 test entry: `2` from menu

Invincible mode resets to off when a stage starts. When enabled, bullets and lasers do not remove lives, but movement, shooting, graze, item pickup, scoring, and laser push still work.

## Project Structure

```text
.
├── AGENTS.md                  # collaboration + memory-sync instructions
├── README.md                  # user-facing project overview and controls
├── compile.ps1                # downloads JavaFX if needed and compiles app/tests
├── run-tests.ps1              # compile + Java smoke tests + Python tool tests
├── run.ps1                    # compile + launch JavaFX app
├── assets/
│   ├── audio/                 # local MP3 BGM
│   ├── backgrounds/           # scrolling stage backgrounds
│   ├── items/                 # collectible item PNG icons
│   ├── portraits/             # dialogue portraits
│   └── sprites/               # player, Boss, enemy sprite sheets
├── docs/
│   ├── developer-handoff.md   # this file
│   └── report-outline.md      # presentation/report outline
├── output/                    # source/generated asset work products
├── src/moonlit/
│   ├── audio/                 # BGM loading and switching
│   ├── dialogue/              # dialogue data, scripts, controller, renderer
│   ├── engine/                # game loop, state, input, collision, config
│   ├── model/                 # player, enemies, boss, bullets, lasers, items
│   ├── pattern/               # bullet pattern strategies and frame coroutines
│   ├── render/                # HUD, sprites, particles, background
│   └── stage/                 # stage timelines and wave directors
├── test/                      # Java and Python smoke tests
├── tools/                     # asset conversion tools and small local UI
└── .codex-memory/             # external working memory / handoff logs
```

The root also currently contains `大创相关/`; it appears separate from the JavaFX game handoff unless the owner says otherwise.

## Main Code Map

### Engine

- `GameApplication`: JavaFX window, scene, canvas, `AnimationTimer`, and input attachment.
- `GameEngine`: central owner of state, object lists, resources, update order, render order, dialogue gates, Boss activation, win/loss, Dream Seal sweep, and invincible mode.
- `GameState`: `MENU`, `PLAYING`, `DIALOGUE`, `PAUSED`, `WON`, `LOST`.
- `InputController`: keyboard state and one-frame key pulses. Add new controls here first.
- `CollisionSystem`: player shot collisions, enemy bullet hit/graze handling.
- `GameConfig`: fixed window/playfield/HUD dimensions.

### Models

- `GameObject`: base class for update/render objects.
- `Entity`: HP-bearing base for player/enemies/Boss.
- `Player`: movement, focus, shooting, lives, bombs, invulnerability, spell-card animation, hitbox marker.
- `Enemy`: enemy kinds and movement styles for Stage 1/Stage 2 enemies.
- `EnemyPatternProfile`: deterministic per-spawn bullet variation for enemies.
- `Boss` + `BossPhase`: formal six-phase Boss queue and pattern implementations.
- `Projectile`, `PlayerShot`, `EnemyBullet`, `BouncingEnemyBullet`: projectile hierarchy.
- `Laser`: warning/active beam, collision, push force.
- `ItemDrop`: collectible resources, attraction, icon rendering.

### Stage And Patterns

- `LevelManager`: selects Stage 1/Stage 2 and delegates timeline updates.
- `StageDirector`: formal Stage 1 timeline. Key trigger times:
  - `0s`, `8s`, `16s`: fairy probe
  - `25s`, `32s`, `41s`: kedama lock
  - `50s`, `66s`: greater fairies
  - `80s`: midboss dialogue then Meteoric Shower
  - `115s`, `128s`, `143s`, `158s`: sunflower rage
  - `170s`: final Boss dialogue then Boss activation
- `StageScript` / `EnemyWave`: auxiliary older stage scripting and Stage 2 support.
- `BulletPattern`, `RingPattern`, `SpiralPattern`, `AimedFanPattern`: classic pattern strategies.
- `FrameCoroutine`, `FrameTaskScheduler`, `XOddAimedPattern`: Danmakufu-style frame-wait pattern support.

### Rendering And Audio

- `StarfieldBackground`: four-wave scrolling background, star overlay, cloud transitions, Boss-mode atmosphere.
- `HudRenderer`: score, lives, bombs, graze, power, fragments, invincible status, Boss/stage bar, controls.
- `ParticleSystem`: burst, ring, spark, sweep-column particles.
- `SpriteAnimation`, `AnimatedSpriteRenderer`, `AssetLoader`: PNG animation loading and fallback.
- `AudioManager`: optional stage/Boss BGM playback and dialogue-triggered cue changes. Missing audio is a no-op.

### Dialogue

- `DialogueLine`: speaker, text, side, cue.
- `DialogueScene`: ordered line list plus completion callback.
- `DialogueController`: current scene/line state.
- `DialogueScripts`: midboss, final Boss, and post-battle scripts.
- `DialogueRenderer`: portrait dialogue UI on Canvas.

## Asset Inventory

- `assets/sprites/player_flight.png`: normal player animation.
- `assets/sprites/player_card.png`: Dream Seal animation.
- `assets/sprites/boss_normal.png`, `boss_abnormal.png`: Boss animation states.
- `assets/sprites/monster1.png` through `monster8.png`: randomized enemy visuals.
- `assets/backgrounds/starry_wave1.png` through `starry_wave4.png`: seamless scrolling backgrounds.
- `assets/audio/stage_starry_illusion.mp3`: stage BGM.
- `assets/audio/boss_master_spark.mp3`: Boss BGM.
- `assets/portraits/reimu.png`, `marisa.png`: dialogue portraits.
- `assets/items/*.png`: item icons based on Game-icons.net assets.

Licensing/caution:

- MP3 and portrait assets are local/user-provided. Confirm rights before public release.
- `assets/items/*.png` are recolored Game-icons.net CC BY 3.0 assets. README already includes attribution.
- Avoid adding Touhou original art, music, or ripped assets if the project will be public.

## Tests And Validation

Primary commands:

```powershell
$env:TEMP='C:\tmp'
$env:TMP='C:\tmp'
.\run-tests.ps1
.\compile.ps1
```

Manual smoke test:

```powershell
.\run.ps1
```

Then verify:

- Menu starts and `Enter` begins Stage 1.
- Background scrolls continuously and changes at the major wave breaks.
- Dialogue appears around `80s`, `170s`, and after Boss defeat.
- `I` toggles invincible mode and HUD changes between `ON` and `OFF`.
- Items drop and auto-attract.
- Boss BGM starts during the final Boss dialogue.
- `X` Dream Seal sweep kills touched enemies and damages Boss.

Automated coverage currently includes:

- Java `LogicSmokeTest`: timeline, dialogue gates, assets, Boss phases, Master Spark, collisions, bombs, item resources, invincible mode, Stage 2 frame-script pattern, sprite compatibility, Dream Seal sweep.
- Python unittest tests: pixel-video asset pipeline and local UI helper logic.

## Working Memory And Collaboration

This project uses `.codex-memory/` as external memory. The configured script path `scripts/memory_sync.py` is currently missing, so recent work used manual/emulated memory commits.

Before continuing:

1. Read `AGENTS.md`.
2. Read recent entries in:
   - `.codex-memory/CURRENT_WORK.md`
   - `.codex-memory/HANDOFF.md`
   - `.codex-memory/MEMORY_COMMITS.md`
3. Check for active ownership/risks before editing shared files.
4. After a meaningful change, append a memory entry to the same files if the sync script is still missing.

## Known Risks

- Worktree is not clean. There are many modified and untracked files from ongoing work. Stage only the files you intend to commit.
- `GameEngine.java`, `Boss.java`, `Enemy.java`, and `LogicSmokeTest.java` are large. Prefer focused edits and add tests before behavior changes.
- `run-tests.ps1` may fail if Python temporary directories use a restricted Windows temp location. Use `C:\tmp`.
- Audio and portrait assets may need rights review before pushing to a public GitHub repo.
- JavaFX SDK is downloaded by `compile.ps1`; offline machines need `.deps/javafx-sdk-21.0.8` already present.

## Suggested Next Tasks

- Commit a clean checkpoint with the current game/demo changes and `.codex-memory` entries.
- Manually tune Stage 1 difficulty now that invincible mode makes testing easier.
- Split large files if future work becomes hard:
  - move Boss phase pattern classes out of `Boss.java`;
  - move Dream Seal / item reward logic out of `GameEngine.java`;
  - split `LogicSmokeTest` by subsystem.
- Add screenshots or a short demo video for the presentation.
- If publishing publicly, replace or document all user-provided licensed assets.

## Git Handoff Notes

Recommended before committing:

```powershell
git status --short
git diff -- README.md docs/developer-handoff.md src/moonlit test/moonlit
$env:TEMP='C:\tmp'; $env:TMP='C:\tmp'; .\run-tests.ps1
.\compile.ps1
```

Suggested commit message:

```text
docs: add developer handoff guide
```

If committing the whole game state, inspect every untracked asset first. The safest path is to stage by intent rather than `git add .`.
