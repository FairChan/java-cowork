# Task Log


## 2026-06-20 20:30 中国标准时间 - codex / main-thread

Branch: codex/pixel-video-asset

Changed:
- Added player normal-state animation

Files:
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


## 2026-06-20 21:08 中国标准时间 - codex / main-thread

Branch: codex/pixel-video-asset

Changed:
- Updated spell-card bomb sweep

Files:
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
## 2026-06-20 21:19 China Standard Time - codex / main-thread

Branch: codex/pixel-video-asset

Changed:
- Refined spell-card sweep particles and collision

Files:
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
## 2026-06-20 23:38 China Standard Time - codex / main-thread

Branch: codex/pixel-video-asset

Changed:
- Added Stage 2 Starcrossed Moon Gate

Files:
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
## 2026-06-20 23:40 China Standard Time - codex / main-thread

Branch: codex/pixel-video-asset

Changed:
- Replaced player and boss animation sprites

Files:
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
## 2026-06-21 00:01 China Standard Time - codex / main-thread

Branch: codex/pixel-video-asset

Changed:
- Replaced stage enemy visuals with randomized monster sprites

Files:
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
## 2026-06-21 14:14:14 +08:00 - codex / chinese-handoff-pdf

Summary:
- Generated Chinese handoff PDF

Files:
- docs/developer-handoff-zh.md
- output/pdf/starry-illusion-developer-handoff-zh.pdf
- tmp/pdfs/starry-illusion-developer-handoff-zh-page1.png
- .codex-memory

Tests:
- ReportLab PDF generation completed
- pdfplumber extraction confirmed key Chinese phrases and no question-mark placeholders
- PyMuPDF rendered page 1 and visual inspection passed

Risks:
- Documentation/PDF-only update; Java compile and game smoke tests were not rerun.
## 2026-06-22 01:46:22 +08:00 - codex / cowork-ui-title-merge

Summary:
- Merged cowork UI and title screen

Files:
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
- Title/portrait assets require rights review before public distribution.
- Worktree remains dirty; stage by intent.
## 2026-06-30 13:09:29 +08:00 - codex / remove-title-left-portrait

Summary:
- Removed title left portrait

Files:
- src/moonlit/render/TitleScreenRenderer.java
- test/moonlit/LogicSmokeTest.java
- .codex-memory

Tests:
- .\compile.ps1 passed
- $env:TEMP='C:\tmp'; $env:TMP='C:\tmp'; .\run-tests.ps1 passed

Risks:
- compile.ps1 and run-tests.ps1 both clean out/classes; run them sequentially.
## 2026-06-30 13:36:59 +08:00 - codex / moonlitgame-folder-merge

Summary:
- Merged cowork MoonlitGame three-boss route

Files:
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
- Do not run compile.ps1 and run-tests.ps1 in parallel.
- Cowork boss portrait assets need rights review before public release.
