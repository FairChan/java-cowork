# Starry Illusion / 穿梭于星屑的魔法使

## English

**Starry Illusion** is a JavaFX bullet-hell shooting game demo built for a course presentation. The game uses Java 21, JavaFX Canvas, `AnimationTimer`, object-oriented class design, animated sprites, scrolling backgrounds, item drops, dialogue scenes, boss phases, lasers, and a 60 FPS frame-based bullet pattern system.

The project is inspired by classic danmaku stage structure, but all code and gameplay implementation in this repository are written for this demo.

### Features

- Playable danmaku stage with waves, mini-bosses, and a final boss route.
- 1176x700 JavaFX game window with title screen, HUD panels, menu, pause, win, and lose states.
- Player shooting, focus movement, small hitbox marker, graze, bombs, lives, score, power, and invincible demo mode.
- Item system: small power, big power, score items, clear items, bomb fragments, life fragments, and full life drops.
- Boss phase system with spell-card-like patterns, warning lasers, bouncing bullets, glowing bullets, and Master Spark-style beam pressure.
- Optional BGM loading from `assets/audio/`; the game still runs silently if audio files are missing.

### Requirements

- Windows PowerShell
- JDK 21 with `java` and `javac` on `PATH`
- Internet access on first compile so `compile.ps1` can download OpenJFX into `.deps/`

### Run

```powershell
.\compile.ps1
.\run-tests.ps1
.\run.ps1
```

### Controls

| Action | Key |
| --- | --- |
| Move | Arrow keys / WASD |
| Shoot | Z |
| Bomb / Dream Seal | X |
| Focus / slow movement | Shift |
| Pause | P |
| Invincible demo mode | I |
| Start / confirm / retry | Enter |
| Advance dialogue | Z / Enter |

### Project Structure

- `src/moonlit/engine` - game loop, state, input, collision, and configuration.
- `src/moonlit/model` - player, enemies, boss, bullets, lasers, and item drops.
- `src/moonlit/stage` - stage director, timeline, waves, and boss activation.
- `src/moonlit/render` - HUD, title screen, particles, sprites, and background rendering.
- `src/moonlit/dialogue` - dialogue data, controller, and renderer.
- `src/moonlit/pattern` - frame coroutine tools and reusable bullet patterns.
- `assets/` - sprites, backgrounds, portraits, item icons, title images, and optional audio.
- `test/` - smoke tests for core game logic and resources.

### Asset Notes

Some images and music are local course-demo assets provided for this project. Item icons are based on Game-icons.net resources licensed under CC BY 3.0. If the project is published beyond classroom use, confirm the rights for all provided images and audio.

---

## 中文

**穿梭于星屑的魔法使 / Starry Illusion** 是一个用于课程展示的 JavaFX 弹幕射击游戏 Demo。项目使用 Java 21、JavaFX Canvas、`AnimationTimer`、面向对象类结构、精灵动画、滚动背景、道具掉落、剧情对话、Boss 阶段、激光判定，以及 60 FPS 帧同步弹幕脚本系统。

本项目参考经典弹幕射击游戏的关卡结构与弹幕设计思路，但仓库中的代码和玩法实现均为本 Demo 编写。

### 主要功能

- 可游玩的完整弹幕关卡，包含道中波次、小 Boss 和关底 Boss。
- 1176x700 JavaFX 窗口，包含开始界面、HUD、菜单、暂停、胜利和失败状态。
- 玩家射击、低速聚焦、小判定点显示、擦弹、Bomb、生命、分数、火力和无敌演示模式。
- 道具系统：小 P、大 P、蓝色得分点、绿色清弹点、B 雷碎片、残机碎片和完整残机。
- Boss 阶段系统：符卡风格弹幕、预警激光、反弹子弹、加算发光子弹和 Master Spark 风格大激光。
- 可选 BGM：音频放在 `assets/audio/` 中；如果音频缺失，游戏会静默运行。

### 运行环境

- Windows PowerShell
- JDK 21，并确保 `java` 和 `javac` 已加入 `PATH`
- 首次编译需要网络，用于让 `compile.ps1` 下载 OpenJFX 到 `.deps/`

### 运行方式

```powershell
.\compile.ps1
.\run-tests.ps1
.\run.ps1
```

### 操作说明

| 操作 | 按键 |
| --- | --- |
| 移动 | 方向键 / WASD |
| 射击 | Z |
| Bomb / 梦想封印 | X |
| 低速聚焦 | Shift |
| 暂停 | P |
| 无敌演示模式 | I |
| 开始 / 确认 / 重开 | Enter |
| 推进对话 | Z / Enter |

### 项目结构

- `src/moonlit/engine` - 游戏循环、状态、输入、碰撞和配置。
- `src/moonlit/model` - 玩家、敌人、Boss、子弹、激光和道具。
- `src/moonlit/stage` - 关卡导演、时间轴、波次和 Boss 触发。
- `src/moonlit/render` - HUD、开始界面、粒子、精灵和背景绘制。
- `src/moonlit/dialogue` - 对话数据、控制器和渲染器。
- `src/moonlit/pattern` - 帧协程工具和可复用弹幕脚本。
- `assets/` - 精灵、背景、立绘、道具图标、标题图和可选音频。
- `test/` - 核心逻辑与资源烟雾测试。

### 资源说明

部分图片和音乐为课程 Demo 本地资源。道具图标基于 Game-icons.net 的 CC BY 3.0 资源制作。如果项目用于课堂以外的公开发布，请再次确认所有图片与音频的授权。
