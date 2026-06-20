# Thread Memory: main-thread


## 2026-06-20 20:30 中国标准时间 - Added player normal-state animation

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
