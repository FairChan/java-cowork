# Starry Illusion 开发交接文档

本文档供下一位开发者继续接手 `C:\Users\ssema\Desktop\java` 下的 JavaFX 弹幕射击项目使用。

## 当前状态

- 当前分支：`codex/pixel-video-asset`
- 当前 HEAD：`ec501ca`
- 主游戏：`Starry Illusion / 穿梭于星屑的魔法使`
- 技术栈：Java 21 + JavaFX 21.0.8，使用 PowerShell 脚本编译和运行，不使用 Maven 或 Gradle
- 窗口尺寸：固定 `900x700`
- 游戏区域：`620x652`，右侧为 HUD
- 主入口：`src/moonlit/GameApplication.java`
- 重要提醒：当前工作区不是干净状态，包含大量最近开发产生的修改和未跟踪资源。接手时请按意图分批暂存文件，不要执行 `git reset --hard` 或批量恢复命令。

目前已实现的主要内容：

- 正式第一关时间轴，包括道中波次、中 Boss 对话、终 Boss 对话、战后对话和 6 个 Boss 阶段。
- 四段高速纵向滚动背景，带云雾转场和星星运动叠加。
- 可选本地 BGM，并支持从道中音乐平滑切换到 Boss 音乐。
- 已导入主角、Boss、小怪、道具、立绘、背景和音频资源。
- 道具系统：小 P、大 P、蓝点、绿点、B 雷碎片、残机碎片和完整残机。
- 小怪弹幕通过 `EnemyPatternProfile` 做固定随机种子的颜色、数量、速度和散布变化。
- 演示用无敌模式，按 `I` 开关。
- Java 逻辑烟测和部分 Python 工具测试。

## 快速启动

在 PowerShell 中进入项目根目录：

```powershell
cd C:\Users\ssema\Desktop\java
.\compile.ps1
.\run-tests.ps1
.\run.ps1
```

第一次编译会自动下载 OpenJFX 到 `.deps/javafx-sdk-21.0.8`。编译产物在 `out/`。

如果 Python 测试因为 Windows 默认临时目录权限失败，可以先设置本地临时目录：

```powershell
$env:TEMP='C:\tmp'
$env:TMP='C:\tmp'
.\run-tests.ps1
```

## 操作方式

- 移动：方向键或 WASD
- 射击：`Z`
- 大招 / 梦想封印：`X`
- 低速聚焦：`Shift`
- 暂停：`P`
- 无敌演示模式：`I`
- 开始 / 重开：`Enter`
- 推进对话：`Z` 或 `Enter`
- 辅助 Stage 2 测试入口：菜单界面按 `2`

无敌模式每次开始关卡时都会重置为关闭。开启后，敌弹和激光不会扣除生命，但移动、射击、擦弹、道具吸附、得分和激光推力仍然有效。

## 项目结构

为避免编码兼容问题，下面使用 ASCII 树形结构：

```text
C:\Users\ssema\Desktop\java
|-- AGENTS.md                         协作和 memory-sync 规则
|-- README.md                         面向玩家和课程展示的说明
|-- compile.ps1                       下载 JavaFX 并编译应用和测试
|-- run-tests.ps1                     编译、Java 烟测、Python 工具测试
|-- run.ps1                           编译并启动 JavaFX 游戏
|-- assets/
|   |-- audio/                        本地 MP3 背景音乐
|   |-- backgrounds/                  四段滚动背景
|   |-- items/                        道具 PNG 图标
|   |-- portraits/                    对话立绘
|   `-- sprites/                      主角、Boss、小怪精灵表
|-- docs/
|   |-- developer-handoff.md          英文交接文档
|   |-- developer-handoff-zh.md       中文交接文档
|   `-- report-outline.md             课程展示和报告提纲
|-- output/                           生成素材和中间产物
|-- src/moonlit/
|   |-- audio/                        BGM 加载、播放、切换
|   |-- dialogue/                     对话数据、控制器、渲染器
|   |-- engine/                       主循环、状态、输入、碰撞、配置
|   |-- model/                        玩家、敌人、Boss、子弹、激光、道具
|   |-- pattern/                      弹幕策略和帧协程
|   |-- render/                       HUD、精灵、粒子、背景
|   `-- stage/                        关卡时间轴和波次导演
|-- test/                             Java 与 Python 烟测
|-- tools/                            素材转换工具和本地辅助 UI
`-- .codex-memory/                    外部工作记忆和交接日志
```

根目录里还有一个中文命名目录，和当前 JavaFX 游戏主线关系不明确。除非项目负责人明确要求，否则接手时先不要改动它。

## 核心模块说明

### 引擎层

- `GameApplication`：JavaFX 窗口、场景、Canvas、`AnimationTimer` 和输入绑定。
- `GameEngine`：游戏状态、对象列表、资源计数、更新顺序、渲染顺序、对话门、Boss 激活、胜负条件、大招扫射和无敌模式的中心协调器。
- `GameState`：包含 `MENU`、`PLAYING`、`DIALOGUE`、`PAUSED`、`WON`、`LOST`。
- `InputController`：键盘状态和单帧按键脉冲。新增按键优先从这里接入。
- `CollisionSystem`：玩家子弹命中、敌弹命中、擦弹和无敌模式判定。
- `GameConfig`：固定窗口、游戏区域和 HUD 尺寸。

### 模型层

- `GameObject`：所有可更新、可渲染对象的抽象基类。
- `Entity`：带 HP 的实体基类，供玩家、小怪和 Boss 继承。
- `Player`：玩家移动、低速聚焦、射击、生命、B 雷、无敌帧、大招动画和判定点显示。
- `Enemy`：道中敌人的类型、路径、血量、掉落和发弹逻辑。
- `EnemyPatternProfile`：同波小怪的弹幕随机配置，使用固定种子保证演示可复现。
- `Boss` 与 `BossPhase`：终 Boss 的 6 阶段队列、HP、限时、移动和弹幕实现。
- `Projectile`、`PlayerShot`、`EnemyBullet`、`BouncingEnemyBullet`：子弹继承体系。
- `Laser`：预警线、激光判定、宽度、持续时间和推力。
- `ItemDrop`：道具类型、吸附、收集效果和图标渲染。

### 关卡与弹幕层

- `LevelManager`：选择 Stage 1 / Stage 2，并把时间轴更新委托给对应脚本。
- `StageDirector`：正式第一关导演。关键触发时间：
  - `0s`、`8s`、`16s`：妖精试探波次
  - `25s`、`32s`、`41s`：毛玉高速封锁
  - `50s`、`66s`：大妖精重火力压制
  - `80s`：中 Boss 对话和 `Meteoric Shower`
  - `115s`、`128s`、`143s`、`158s`：向日葵妖精激光引导
  - `170s`：终 Boss 对话并激活 Boss 战
- `StageScript` / `EnemyWave`：旧关卡脚本和 Stage 2 辅助支持。
- `BulletPattern`、`RingPattern`、`SpiralPattern`、`AimedFanPattern`：经典弹幕策略接口与实现。
- `FrameCoroutine`、`FrameTaskScheduler`、`XOddAimedPattern`：Danmakufu 风格帧等待弹幕脚本，其中 `XOddAimedPattern` 是 4 臂 X 形奇数狙示例。

### 渲染与音频层

- `StarfieldBackground`：四段背景循环滚动、星星叠加、云雾转场和 Boss 氛围叠加。
- `HudRenderer`：分数、生命、B 雷、擦弹、火力、碎片、无敌状态、Boss 血条和操作提示。
- `ParticleSystem`：爆炸、环形、火花、大招扫射粒子。
- `SpriteAnimation`、`AnimatedSpriteRenderer`、`AssetLoader`：PNG 精灵表加载、动画和失败回退。
- `AudioManager`：可选道中 / Boss BGM 播放与切换。音频文件缺失时保持静默，不影响游戏运行。

### 对话层

- `DialogueLine`：单句对话的数据结构，包括说话人、文本、站位和事件 cue。
- `DialogueScene`：一组有序对话和完成回调。
- `DialogueController`：当前对话场景、行号和推进状态。
- `DialogueScripts`：中 Boss、终 Boss、通关后的对话脚本。
- `DialogueRenderer`：Canvas 上的立绘和对话框渲染。

## 资源清单

- `assets/sprites/player_flight.png`：玩家普通飞行动画。
- `assets/sprites/player_card.png`：大招 / 梦想封印动画。
- `assets/sprites/boss_normal.png`、`boss_abnormal.png`：Boss 普通和异常状态动画。
- `assets/sprites/monster1.png` 到 `monster8.png`：随机小怪动画。
- `assets/backgrounds/starry_wave1.png` 到 `starry_wave4.png`：四段无缝滚动背景。
- `assets/audio/stage_starry_illusion.mp3`：道中 BGM。
- `assets/audio/boss_master_spark.mp3`：Boss BGM。
- `assets/portraits/reimu.png`、`marisa.png`：对话立绘。
- `assets/items/*.png`：道具图标。

授权注意：

- MP3 和立绘是本地用户提供资源。公开发布前必须确认授权。
- 道具图标基于 Game-icons.net 的 CC BY 3.0 图标重新着色，`README.md` 中已经加入署名说明。
- 如果课程或公开仓库对版权更严格，应替换所有直接指向东方原作姓名、符卡和音乐标题的文本与资源。

## 测试与验证

推荐的自动验证命令：

```powershell
$env:TEMP='C:\tmp'
$env:TMP='C:\tmp'
.\run-tests.ps1
.\compile.ps1
```

手动启动：

```powershell
.\run.ps1
```

手动检查重点：

- 菜单能正常进入，按 `Enter` 开始 Stage 1。
- 背景持续高速滚动，并在主要波次切换时出现云雾转场。
- 约 `80s`、`170s` 和击败 Boss 后出现对话。
- 按 `I` 能开启 / 关闭无敌模式，HUD 能显示状态。
- 小怪掉落道具，道具能自动吸附并更新分数、火力、B 雷碎片和残机碎片。
- Boss 对话阶段开始切换 Boss BGM。
- 按 `X` 释放梦想封印，扫到的小怪死亡，Boss 受到伤害，屏幕敌弹被清除。

现有自动测试覆盖：

- Java `LogicSmokeTest`：关卡时间轴、对话门、资源文件、Boss 阶段、Master Spark、碰撞、B 雷、道具资源、无敌模式、Stage 2 帧脚本弹幕、精灵兼容性和梦想封印扫射。
- Python unittest：像素视频素材管线和本地 UI 辅助逻辑。

## 协作记忆

项目使用 `.codex-memory/` 作为外部工作记忆。当前配置中提到的 `scripts/memory_sync.py` 不存在，所以最近的记忆提交是手动模拟写入。

接手前建议按顺序阅读：

1. `AGENTS.md`
2. `.codex-memory/CURRENT_WORK.md`
3. `.codex-memory/HANDOFF.md`
4. `.codex-memory/MEMORY_COMMITS.md`

继续开发时请遵守：

- 不要依赖聊天记录作为唯一事实来源。
- 修改共享文件前，先看是否有活跃任务或风险提示。
- 每次有意义的修改后，如果同步脚本仍不存在，就手动补充 `.codex-memory` 相关记录。
- 与他人交接时，更新 `HANDOFF.md`，写明变更、原因、文件、测试、风险和下一步。

## 已知风险

- 当前工作树不干净，有很多已修改和未跟踪文件。提交时请按意图分批 `git add`。
- `GameEngine.java`、`Boss.java`、`Enemy.java` 和 `LogicSmokeTest.java` 较大。后续新增行为时建议先加测试，再做聚焦修改。
- `run-tests.ps1` 在受限临时目录下可能失败，可使用 `C:\tmp` 规避。
- 音频和立绘资源公开发布前需要授权复核。
- `compile.ps1` 会下载 JavaFX SDK。离线机器需要提前准备 `.deps/javafx-sdk-21.0.8`。
- 当前 Boss 弹幕是按课程演示设计实现的代表性版本，不是任何商业游戏或原作的逐像素复刻。

## 建议下一步

- 先提交一个干净 checkpoint，把当前游戏、素材和记忆文件固定下来。
- 使用无敌模式完整手动跑一遍 Stage 1，调节弹幕密度、道具掉落和 Boss HP。
- 如果后续开发继续扩大，建议拆分大文件：
  - 把 Boss 各阶段模式从 `Boss.java` 拆到独立类。
  - 把梦想封印和道具奖励逻辑从 `GameEngine.java` 拆出。
  - 把 `LogicSmokeTest` 按系统拆成多个测试文件。
- 为课堂展示补充截图或短演示视频。
- 如果要公开到 GitHub，先替换或明确标注所有用户提供素材的授权。

## Git 交接建议

提交前建议执行：

```powershell
git status --short
git diff -- README.md docs/developer-handoff.md docs/developer-handoff-zh.md src/moonlit test/moonlit
$env:TEMP='C:\tmp'; $env:TMP='C:\tmp'; .\run-tests.ps1
.\compile.ps1
```

如果只提交文档，可使用类似提交信息：

```text
docs: add Chinese developer handoff guide
```

如果要提交整个游戏状态，请先逐个检查未跟踪资源。最安全的方式是按模块分批暂存，而不是直接 `git add .`。
