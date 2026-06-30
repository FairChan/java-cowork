# Handoff

## Memory sync setup

Owner: codex
Branch: codex/pixel-video-asset
Last updated: 2026-06-20 20:26 中国标准时间

Context:
- Memory sync has been initialized.

What changed:
- Created repository-local Codex memory files.

Why:
- Codex sessions are stateless, so project memory must live in versioned files.

Files touched:
- .codex-memory/*
- AGENTS.md

Files locked:
- None.

Tests:
- Not applicable.

Known risks:
- Team members must follow the read-before-work and write-before-handoff protocol.

Next step:
- Register active tasks before code edits.


## Added player normal-state animation

Actor: codex
Thread: main-thread
Purpose: work

Summary:
- Added player normal-state animation

Details:
  Initialized/synced Codex memory, combined output/pixel_assets/reimu/frames frame_000..frame_060 into assets/sprites/player_flight.png, updated Player to use the 61-frame 93x96 normal animation scaled to gameplay size, and updated the resource smoke test to expect 61 frames. Existing unrelated pixel-video worktree changes were left untouched.

Files touched:
- assets/sprites/player_flight.png
- src/moonlit/model/Player.java
- test/moonlit/LogicSmokeTest.java
- tools/build_player_flight_sheet.py
- .codex-memory

Tests:
- TEMP=workspace tmp; .\\run-tests.ps1 passed; JavaFX launch smoke ran 5 seconds

Risks:
- run-tests.ps1 also runs pixel-video Python tests from existing branch work; they require TEMP/TMP inside workspace under the current sandbox.

Next:
- Optionally commit these changes and push after reviewing the existing unrelated worktree changes.


## Updated spell-card bomb sweep

Actor: codex
Thread: main-thread
Purpose: work

Summary:
- Updated spell-card bomb sweep

Details:
  Implemented the X-key bomb as a spell-card sweep. Built assets/sprites/player_card.png from output/pixel_assets/reimu-card/frames, kept Player switching to the card spritesheet during the spell, added GameEngine sweep beam rendering and damage logic that destroys swept stage enemies and damages the boss, and expanded logic smoke tests for card animation/resource presence/sweep damage. Used online references for bullet-hell bomb/spell-card conventions: bombs clear bullets, grant temporary invulnerability, and deal heavy damage.

Files touched:
- assets/sprites/player_card.png
- src/moonlit/engine/GameEngine.java
- src/moonlit/model/Player.java
- src/moonlit/render/ParticleSystem.java
- test/moonlit/LogicSmokeTest.java
- tools/build_player_card_sheet.py
- README.md

Tests:
- TEMP=workspace tmp; .\\run-tests.ps1 passed; JavaFX launch smoke ran 5 seconds

Risks:
- Worktree contains unrelated pixel-video UI/tool modifications; left untouched. Current sandbox requires TEMP/TMP inside workspace for Python tests.

Next:
- Manually playtest X spell-card sweep during a wave to tune beam width/damage if desired.
## Refined spell-card sweep particles and collision

Actor: codex
Thread: main-thread
Purpose: work

Summary:
- Refined spell-card sweep particles and collision

Details:
  Refined the X-key spell-card sweep so its hit test covers the full swept interval between frames instead of only the final beam center. This prevents skipped enemies during fast updates or frame drops. Enhanced sweep particles with brighter white sparks and cyan lateral flares, kept the player card animation active during the spell, and documented the sweep behavior in README.

Files touched:
- src/moonlit/engine/GameEngine.java
- src/moonlit/render/ParticleSystem.java
- test/moonlit/LogicSmokeTest.java
- README.md
- .codex-memory

Tests:
- TEMP=workspace tmp; .\\run-tests.ps1 passed
- Hidden JavaFX launch smoke stayed alive for 5 seconds

Risks:
- Worktree still contains unrelated pixel-video UI/tool modifications; left untouched.

Next:
- Manually play the X spell-card during dense waves to tune sweep width or boss damage if desired.
## Added Stage 2 Starcrossed Moon Gate

Actor: codex
Thread: main-thread
Purpose: work

Summary:
- Added Stage 2 Starcrossed Moon Gate

Details:
  Implemented Stage 2: Starcrossed Moon Gate as a playable JavaFX stage selected with the 2 key from the menu. Added stage-aware scripts and boss creation, a fixed-60FPS FrameTaskScheduler/FrameCoroutine layer, XOddAimedPattern with four X arms and 9-way odd aimed bursts, and EnemyBullet acceleration plus additive glow rendering. Added smoke tests for Stage 2 start, waves, boss timing, 36-bullet bursts, aimed center angle, frame waits, deceleration, additive defaults, and bomb clearing.

Files touched:
- README.md
- src/moonlit/engine/GameEngine.java
- src/moonlit/engine/InputController.java
- src/moonlit/model/Boss.java
- src/moonlit/model/EnemyBullet.java
- src/moonlit/model/Projectile.java
- src/moonlit/pattern/FrameCoroutine.java
- src/moonlit/pattern/FrameTaskScheduler.java
- src/moonlit/pattern/XOddAimedPattern.java
- src/moonlit/render/HudRenderer.java
- src/moonlit/stage/LevelManager.java
- src/moonlit/stage/StageScript.java
- test/moonlit/LogicSmokeTest.java
- .codex-memory

Tests:
- TEMP=workspace tmp; .\\run-tests.ps1 passed`r`n- Hidden JavaFX launch smoke stayed alive for 5 seconds

Risks:
- Worktree still contains pre-existing unrelated pixel-video changes and untracked boss asset PNGs; left untouched.`r`n- Stage 2 currently reuses existing sprites/background as planned.

Next:
- Manually play Stage 2 from the menu with key 2 and tune bullet density if the classroom demo needs easier or harder lanes.
## Replaced player and boss animation sprites

Actor: codex
Thread: main-thread
Purpose: work

Summary:
- Replaced player and boss animation sprites

Details:
  Rebuilt assets/sprites/player_flight.png from output/pixel_assets/reimu/frames, added assets/sprites/boss_normal.png and assets/sprites/boss_abnormal.png from their frame folders, updated Player so Shift keeps the normal Reimu animation and overlays only a red hitbox dot, and updated Boss to switch from normal to abnormal animation in the final low-HP phase. Resource tests now validate the 61-frame player sheet and both 46-frame boss sheets. Note: boss_normal and boss_abnormal source frame bytes are currently identical, so the code switches states but the visual result will match until different abnormal frames are supplied.

Files touched:
- assets/sprites/player_flight.png
- assets/sprites/boss_normal.png
- assets/sprites/boss_abnormal.png
- src/moonlit/model/Player.java
- src/moonlit/model/Boss.java
- test/moonlit/LogicSmokeTest.java
- README.md
- .codex-memory

Tests:
- TEMP=workspace tmp; .\\run-tests.ps1 passed
- Sprite alpha/dimension smoke passed for player_flight, boss_normal, and boss_abnormal
- Hidden JavaFX launch smoke stayed alive for 5 seconds

Risks:
- boss_normal and boss_abnormal source frames are byte-identical right now, so abnormal state support is wired but visually identical until the source frames differ.
- Worktree still contains unrelated Stage 2 and pixel-video modifications; left untouched except where Boss/LogicSmokeTest had to be extended in place.

Next:
- Replace output/pixel_assets/boss_abnormal/frames with visually distinct frames if a visibly changed boss state is required.
## Replaced stage enemy visuals with randomized monster sprites

Actor: codex
Thread: main-thread
Purpose: work

Summary:
- Replaced stage enemy visuals with randomized monster sprites

Details:
  Built assets/sprites/monster1.png through assets/sprites/monster8.png from output/pixel_assets/monster1..8/frames. Updated Enemy so both gameplay kinds keep their existing movement, HP, score, and bullet behavior while randomly selecting one of the eight imported monster animations on spawn. Added smoke tests for monster spritesheet presence, randomized visual selection, and Stage 1 / Stage 2 spawned enemies using imported monster sprites.

Files touched:
- assets/sprites/monster1.png
- assets/sprites/monster2.png
- assets/sprites/monster3.png
- assets/sprites/monster4.png
- assets/sprites/monster5.png
- assets/sprites/monster6.png
- assets/sprites/monster7.png
- assets/sprites/monster8.png
- src/moonlit/model/Enemy.java
- test/moonlit/LogicSmokeTest.java
- README.md
- .codex-memory

Tests:
- TEMP=workspace tmp; .\run-tests.ps1 passed
- Sprite alpha/dimension smoke passed for monster1 through monster8
- Hidden JavaFX launch smoke stayed alive for 5 seconds

Risks:
- Enemy visual randomization uses a fixed-seed pseudo-random generator for reproducible demos and tests.
- Worktree still contains unrelated Stage 2 and pixel-video modifications; left untouched except for shared Enemy/LogicSmokeTest updates.

Next:
- Run the game manually and watch a few Stage 1 and Stage 2 waves if visual variety needs tuning.
## 2026-06-21 00:55 China Standard Time - Rebuilt formal Starry Illusion stage

Actor: codex
Thread: starry-illusion-rebuild
Purpose: work

Summary:
- Rebuilt formal Starry Illusion stage

Details:
  Replaced the default Stage 1 flow with Starry Illusion: a 0:00-2:50 formal timeline, fairy/kedama/greater-fairy/midboss/sunflower waves, resource drops, laser objects, item collection, optional local BGM, and a six-phase Marisa boss queue. Preserved Stage 2's X odd-aimed frame-script pattern as an auxiliary test/demo path. Added BossPhase, StageDirector, ItemDrop, Laser, BouncingEnemyBullet, and AudioManager; updated README/report outline and JavaFX scripts for javafx.media.

Files touched:
- README.md
- compile.ps1
- run.ps1
- run-tests.ps1
- docs/report-outline.md
- src/moonlit/audio/AudioManager.java
- src/moonlit/engine/GameEngine.java
- src/moonlit/engine/CollisionSystem.java
- src/moonlit/model/Boss.java
- src/moonlit/model/BossPhase.java
- src/moonlit/model/BouncingEnemyBullet.java
- src/moonlit/model/Enemy.java
- src/moonlit/model/ItemDrop.java
- src/moonlit/model/Laser.java
- src/moonlit/model/Player.java
- src/moonlit/render/HudRenderer.java
- src/moonlit/render/StarfieldBackground.java
- src/moonlit/stage/LevelManager.java
- src/moonlit/stage/StageDirector.java
- src/moonlit/stage/StageScript.java
- test/moonlit/LogicSmokeTest.java
- .codex-memory

Tests:
- TEMP=workspace tmp; .\run-tests.ps1 passed
- .\compile.ps1 passed
- Hidden JavaFX launch smoke stayed alive for 5 seconds and was stopped

Risks:
- Worktree still contains pre-existing unrelated pixel-video tool/UI changes and asset changes; they were not reverted.
- Boss patterns are playable representative implementations of the supplied formal design, not a pixel-perfect clone of any copyrighted Touhou content.
- Optional audio loads only user-provided local mp3 files; no BGM files are bundled.

Next:
- Manually play the full stage to tune difficulty/density for the 20-minute presentation if needed.
## 2026-06-21 01:27 China Standard Time - Added scrolling four-wave backgrounds and BGM crossfade

Actor: codex
Thread: bg-audio-scroll
Purpose: work

Summary:
- Added scrolling four-wave backgrounds and BGM crossfade

Details:
  Imported output/background1-4.png as assets/backgrounds/starry_wave1-4.png and copied the user-provided stage/Boss MP3 files into assets/audio. Reworked StarfieldBackground into a four-wave seamless vertical scroller with retained star overlay, boss slowdown/atmosphere, and 1.2s procedural cloud-fog transitions. StageDirector now requests background changes at 25s, 50s, and 115s. AudioManager now tracks cue state, only creates MediaPlayer on the JavaFX application thread, and crossfades stage music into boss music. Logic smoke tests cover resources, wave transitions, cloud transition state, and boss music cue.

Files touched:
- assets/backgrounds/starry_wave1.png
- assets/backgrounds/starry_wave2.png
- assets/backgrounds/starry_wave3.png
- assets/backgrounds/starry_wave4.png
- assets/audio/stage_starry_illusion.mp3
- assets/audio/boss_master_spark.mp3
- src/moonlit/render/StarfieldBackground.java
- src/moonlit/audio/AudioManager.java
- src/moonlit/engine/GameEngine.java
- src/moonlit/stage/StageDirector.java
- test/moonlit/LogicSmokeTest.java
- README.md
- docs/report-outline.md
- .codex-memory

Tests:
- TEMP=workspace tmp; .\run-tests.ps1 passed
- .\compile.ps1 passed
- Hidden JavaFX launch smoke stayed alive for 5 seconds and was stopped

Risks:
- MP3 files are local course-demo assets supplied by the user; repository/public sharing should consider licensing.
- Worktree still contains pre-existing unrelated pixel-video/tool/sprite changes; left untouched.

Next:
- Manually play through the first two minutes to tune scroll speed or cloud opacity if desired.
## 2026-06-21 02:29 C2ina SAan21ar21 Ti29e - Fixed opening overlap, varied enemy patterns, and added item icons

Actor: codex
Thread: starry-items-variety
Purpose: work

Summary:
- Fixed opening overlap, varied enemy patterns, and added item icons

Details:
  Fixed the opening fairy probe by assigning separate hover slots for the six enemies. Added deterministic per-spawn EnemyPatternProfile values so same-wave bullets vary in color, count, speed, spread, radius, cooldown, and jitter. Added Game-icons.net-derived transparent item icons, ItemDrop image rendering with fallback, HUD fragment counts, ordinary enemy-drop rewards on player-shot kills, and tests for overlap, bullet variety, icon assets, and fragment resources.

Files touched:
- assets/items/small_power.png
- assets/items/big_power.png
- assets/items/score_blue.png
- assets/items/clear_green.png
- assets/items/bomb_fragment.png
- assets/items/life_fragment.png
- assets/items/life_full.png
- src/moonlit/model/EnemyPatternProfile.java
- src/moonlit/model/Enemy.java
- src/moonlit/model/EnemyBullet.java
- src/moonlit/model/ItemDrop.java
- src/moonlit/stage/StageDirector.java
- src/moonlit/engine/GameEngine.java
- src/moonlit/engine/CollisionSystem.java
- src/moonlit/render/HudRenderer.java
- test/moonlit/LogicSmokeTest.java
- README.md
- .codex-memory

Tests:
- $env:TEMP='C:\tmp'; $env:TMP='C:\tmp'; .\run-tests.ps1 passed
- .\compile.ps1 passed
- Hidden JavaFX launch smoke stayed alive for 5 seconds and was stopped

Risks:
- Item icons are based on Game-icons.net CC BY 3.0 assets and require attribution in public distributions.
- Pattern randomization uses a fixed seed for reproducible demos; manual difficulty tuning may still be useful.
- Worktree still contains pre-existing unrelated dirty changes; left untouched.

Next:
- Manually play the opening waves to tune density and confirm the visual rhythm feels less repetitive.
## 2026-06-21 02:44:46 +08:00 - Added portrait dialogue scenes

Actor: codex
Thread: starry-dialogue-portraits
Purpose: work

Summary:
- Added portrait dialogue scenes

Details:
  Added Touhou-style dialogue gates for the Starry Illusion midboss encounter, final boss intro, and post-battle clear sequence. Imported Reimu and Marisa portrait PNGs, added dialogue model/controller/renderer classes, paused gameplay logic during dialogue while keeping background/audio/particles alive, and wired dialogue cues to boss music/stop music events.

Files touched:
- assets/portraits/reimu.png
- assets/portraits/marisa.png
- src/moonlit/dialogue/DialogueLine.java
- src/moonlit/dialogue/DialogueScene.java
- src/moonlit/dialogue/DialogueController.java
- src/moonlit/dialogue/DialogueScripts.java
- src/moonlit/dialogue/DialogueRenderer.java
- src/moonlit/audio/AudioManager.java
- src/moonlit/engine/GameEngine.java
- src/moonlit/engine/GameState.java
- src/moonlit/engine/InputController.java
- src/moonlit/stage/StageDirector.java
- test/moonlit/LogicSmokeTest.java
- README.md
- docs/report-outline.md
- .codex-memory

Tests:
- $env:TEMP='C:\tmp'; $env:TMP='C:\tmp'; .\run-tests.ps1 passed
- .\compile.ps1 passed
- Hidden JavaFX launch smoke stayed alive for 5 seconds and was stopped

Risks:
- Portrait PNGs are framed with their existing background rather than chroma-keyed transparent cutouts.
- Public distribution should confirm rights for user-provided portrait and MP3 assets.
- Worktree still contains pre-existing unrelated dirty changes; left untouched.

Next:
- Manually play through the 80s and 170s dialogue gates to tune text pacing and portrait placement if desired.
## 2026-06-21 11:55:46 +08:00 - Added invincible demo mode

Actor: codex
Thread: invincible-mode
Purpose: work

Summary:
- Added invincible demo mode

Details:
  Added an I-key invincible demo toggle. When enabled, enemy bullets and lasers no longer remove player lives, while movement, shooting, graze, item pickup, scoring, and laser push still work. HUD now displays Invincible ON/OFF and controls/README document the switch.

Files touched:
- src/moonlit/engine/InputController.java
- src/moonlit/engine/GameEngine.java
- src/moonlit/engine/CollisionSystem.java
- src/moonlit/model/Laser.java
- src/moonlit/render/HudRenderer.java
- test/moonlit/LogicSmokeTest.java
- README.md
- .codex-memory

Tests:
- $env:TEMP='C:\tmp'; $env:TMP='C:\tmp'; .\run-tests.ps1 passed
- .\compile.ps1 passed
- Hidden JavaFX launch smoke stayed alive for 5 seconds and was stopped

Risks:
- Invincible mode is intended for demo/debug use and resets to OFF whenever a stage starts.

Next:
- Use I during manual demos when showing dense boss patterns without risking a game over.
## 2026-06-21 12:30:39 +08:00 - Added developer handoff documentation

Actor: codex
Thread: colleague-handoff
Purpose: work

Summary:
- Added developer handoff documentation

Details:
  Created docs/developer-handoff.md with current project status, setup commands, controls, package/class map, stage timeline, asset inventory, validation commands, memory-sync guidance, known risks, and suggested next tasks for a teammate taking over development.

Files touched:
- docs/developer-handoff.md
- .codex-memory

Tests:
- Not run; documentation-only handoff update.

Risks:
- Worktree remains intentionally dirty with many ongoing game assets/classes; teammate should stage files by intent and avoid destructive restores.

Next:
- Share docs/developer-handoff.md with the teammate before they begin edits.
## 2026-06-21 14:14:14 +08:00 - Generated Chinese handoff PDF

Actor: codex
Thread: chinese-handoff-pdf
Purpose: work

Summary:
- Generated Chinese handoff PDF

Details:
  Created docs/developer-handoff-zh.md as a Chinese version of the developer handoff and generated output/pdf/starry-illusion-developer-handoff-zh.pdf. The PDF uses embedded Microsoft YaHei font, includes page footers, and was checked by text extraction plus first-page PNG rendering.

Files touched:
- docs/developer-handoff-zh.md
- output/pdf/starry-illusion-developer-handoff-zh.pdf
- tmp/pdfs/starry-illusion-developer-handoff-zh-page1.png
- .codex-memory

Tests:
- PDF generated with ReportLab
- pdfplumber extraction confirmed title, subtitle, quick start, invincible mode, and StageDirector text
- PyMuPDF rendered page 1 successfully; visual inspection confirmed readable Chinese

Risks:
- Documentation/PDF-only update; Java compile and game smoke tests were not rerun.
- Worktree remains intentionally dirty with many ongoing game files and assets.

Next:
- Share the Chinese Markdown and PDF handoff files with the teammate before they continue development.
## 2026-06-22 01:46:22 +08:00 - Merged cowork UI and title screen

Actor: codex
Thread: cowork-ui-title-merge
Purpose: work

Summary:
- Merged cowork UI and title screen

Details:
  Added cowork remote, fetched cowork/main at c8b1e9a, and selectively ported the cowork UI. The app keeps Starry Illusion gameplay but now boots into a JavaFX Canvas title screen adapted from DanmakuFestivalCover, then transitions into the 1176x700 dual-HUD MoonlitGame layout. Damage and reward events drive Reimu mood portraits in the left HUD.

Files touched:
- assets/title/**
- assets/portraits/reimu_normal.png
- assets/portraits/reimu_hurt.png
- assets/portraits/reimu_cheer.png
- src/moonlit/render/TitleScreenRenderer.java
- src/moonlit/render/HudRenderer.java
- src/moonlit/engine/GameConfig.java
- src/moonlit/engine/InputController.java
- src/moonlit/engine/GameEngine.java
- src/moonlit/engine/CollisionSystem.java
- src/moonlit/GameApplication.java
- test/moonlit/LogicSmokeTest.java
- README.md
- docs/report-outline.md
- .codex-memory

Tests:
- $env:TEMP='C:\tmp'; $env:TMP='C:\tmp'; .\run-tests.ps1 passed
- .\compile.ps1 passed
- Hidden JavaFX launch smoke stayed alive for 5 seconds and was stopped

Risks:
- Title and portrait assets came from cowork/local demo materials; confirm rights before public distribution.
- Worktree remains intentionally dirty with many earlier game changes and untracked assets; stage by intent.
- README/report outline still contain some pre-existing mojibake outside the new cowork UI notes.

Next:
- Manually launch .\run.ps1 and visually inspect the title screen, START transition, and dual HUD on the actual display.
## 2026-06-30 13:09:29 +08:00 - Removed title left portrait

Actor: codex
Thread: remove-title-left-portrait
Purpose: work

Summary:
- Removed title left portrait

Details:
  Removed the title screen's left-side Reimu portrait and left label only. The right title portrait, title menu interactions, START transition, dual HUD, and gameplay systems are unchanged.

Files touched:
- src/moonlit/render/TitleScreenRenderer.java
- test/moonlit/LogicSmokeTest.java
- .codex-memory

Tests:
- .\compile.ps1 passed
- $env:TEMP='C:\tmp'; $env:TMP='C:\tmp'; .\run-tests.ps1 passed

Risks:
- Avoid running compile.ps1 and run-tests.ps1 in parallel; both clean out/classes.

Next:
- Launch .\run.ps1 and visually confirm the title screen no longer shows the left portrait.
## 2026-06-30 13:36:59 +08:00 - Merged MoonlitGame three-boss route

Actor: codex
Thread: moonlitgame-folder-merge
Purpose: work

Summary:
- Selectively merged cowork MoonlitGame folder changes into the current Starry Illusion project.

Details:
  Integrated the cowork folder's three-boss route while preserving the current formal six-phase final boss, existing JavaFX Canvas title screen, and the user's previous request to remove the title screen's left portrait. Stage 1 now gates through Kitsune Envoy at 34s, Lantern Butterfly at 82s, and Star Oracle at 132s. Mini-boss battles use shorter patterns and portrait art; final boss keeps the existing six-phase BossPhase sequence.

Files touched:
- assets/bosses/boss_kitsune.png
- assets/bosses/boss_lantern_fairy.png
- assets/bosses/boss_star_oracle.png
- src/moonlit/model/Boss.java
- src/moonlit/engine/GameEngine.java
- src/moonlit/stage/StageDirector.java
- src/moonlit/dialogue/DialogueScripts.java
- src/moonlit/dialogue/DialogueRenderer.java
- test/moonlit/LogicSmokeTest.java
- README.md
- docs/report-outline.md
- .codex-memory

Tests:
- .\compile.ps1 passed
- $env:TEMP = 'C:\tmp'; $env:TMP = 'C:\tmp'; .\run-tests.ps1 passed

Risks:
- compile.ps1 and run-tests.ps1 must be run sequentially.
- Cowork portrait assets should be rights-reviewed before public distribution.
- Worktree remains intentionally dirty with many prior uncommitted files; stage by intent.

Next:
- Run .\run.ps1 and visually inspect mini-boss dialogue portraits, boss BGM switching, wave 4 cloud transition, and final boss clear flow.
