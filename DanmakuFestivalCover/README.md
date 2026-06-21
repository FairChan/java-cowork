# Danmaku Festival Cover

这是一个独立 Java Swing 封面示例，不依赖旧项目。

## 目录

- `src/DanmakuCover.java`：封面窗口和全部绘制逻辑
- `assets/reimu.png`：红色角色立绘
- `assets/marisa.png`：魔女角色立绘
- `assets/shrine_gate_background.png`：神社大门背景
- `assets/poses/*.png`：新版俏皮动作透明立绘
- `assets/poses/chroma/*.png`：生成时保留的纯色背景原图
- `run.ps1` / `run.bat`：Windows 运行脚本

## 运行

需要电脑已经安装 JDK，并且 `java`、`javac` 在 PATH 中。

PowerShell：

```powershell
.\run.ps1
```

CMD：

```bat
run.bat
```

手动编译运行：

```powershell
javac -encoding UTF-8 -d out src\DanmakuCover.java
java -cp out DanmakuCover
```

## 可改位置

标题、英文副标题、菜单项都在 `src/DanmakuCover.java` 顶部附近：

```java
private static final String TITLE = "FANTASY DANMAKU FESTIVAL";
private static final String SUBTITLE = "Sakura Shrine Gate";
```

菜单项在 `MenuEntry[] menu` 中。

新版封面默认使用 `assets/poses` 下的动作立绘，并每隔几秒自动切换左右角色姿势。
选择 `START` 后，镜头会向神社大门内推进，菜单和角色会随动画淡出。
