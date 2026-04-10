# LinkGame - 连连看游戏

一款使用 Jetpack Compose 开发的经典连连看 Android 游戏，包含挑战模式和无尽模式，支持音效、背景音乐、排行榜和昵称设置。

## 项目简介

LinkGame 是一款消除类益智游戏，玩家需要在限时内连接两个相同图案的方块，路径最多只能拐两个弯。游戏包含：

- **挑战模式**：依次通关 4 个预设难度关卡（简单、普通、困难、极限），全部通关即获胜。
- **无尽模式**：选择一个难度后不断挑战，每清空一次棋盘进入下一关，难度不变但时间重新计时。

游戏结束后可以保存成绩到排行榜，并支持查看和删除记录。

## 功能特性

- 🎮 两种游戏模式（挑战/无尽）
- 🧩 自动生成可解棋盘
- ⏱️ 限时挑战，实时显示剩余时间
- 🔊 背景音乐和音效开关（点击、消除）
- 🏆 排行榜（按得分和用时排序）
- ✍️ 用户昵称设置
- 🌈 动态主题颜色（适配浅色/深色模式）
- 🧭 支持返回键退出游戏并保存成绩
<div align="center">
  <img src="https://github.com/user-attachments/assets/72209aa6-ab09-488c-9f13-34b08da7a2f7" width="300" alt="主界面">
  <img src="https://github.com/user-attachments/assets/b2747d90-a558-4508-86f0-6f33ac7b0dcf" width="300" alt="挑战模式">
  <img src="https://github.com/user-attachments/assets/b1fd7ddb-df39-4a80-bb54-68ebed0e0313" width="300" alt="无尽模式">
  <img src="https://github.com/user-attachments/assets/704049cd-4511-4e5d-89e4-d9175753eba0" width="300" alt="排行榜">
  <img src="https://github.com/user-attachments/assets/e4cac8c2-c764-4a91-b078-895cd1d662e2" width="300" alt="排行榜">
  <img src="https://github.com/user-attachments/assets/3a276c2e-0db7-4fc4-822c-66a8843b16bb" width="300" alt="设置">
</div>

## 技术栈

- **语言**：Kotlin
- **UI 框架**：Jetpack Compose
- **架构**：MVVM + ViewModel + StateFlow
- **数据存储**：DataStore（Preferences）存储设置、昵称和排行榜
- **异步处理**：Kotlin Coroutines
- **音频播放**：MediaPlayer
- **序列化**：kotlinx.serialization

## 项目结构

```
com.example.linkgame/
├── audio/                  # 音频管理
│   └── AudioManager.kt
├── data/                   # 数据层
│   ├── model/              # 数据模型（LeaderboardEntry）
│   └── repository/         # 数据仓库（Leaderboard, Nickname, Settings）
├── game/                   # 游戏逻辑
│   ├── engine/             # GameController（游戏状态和逻辑）
│   ├── logic/              # 棋盘生成、连接检测、路径查找、可解性校验
│   └── model/              # Board, GameMode, LevelConfig
├── ui/                     # 界面
│   ├── components/         # 可复用组件（棋盘、对话框、分数条等）
│   ├── navigation/         # 导航（GameNavHost）
│   ├── screen/             # 各个屏幕（Start, Game, Leaderboard）
│   └── theme/              # 主题和颜色
└── utils/                  # 工具类（颜色映射、文字标签）
```

## 构建与运行

### 环境要求

- Android Studio Flamingo 或更高版本
- Android SDK 21+
- Gradle 8.0+

### 克隆项目

```bash
git clone https://github.com/ElovisiaWinslow/LinkGame.git
```

### 在 Android Studio 中打开

1. 打开 Android Studio，选择 **Open an Existing Project**。
2. 选择项目根目录下的 `build.gradle.kts`（或 `build.gradle`）。
3. 等待 Gradle 同步完成。

### 添加资源文件

项目依赖一些音频文件（`click.mp3`, `eliminate.mp3`, `bgm.mp3`）和图片，需要自行放入 `res/raw/` 目录。若没有音频文件，可以将 `AudioManager` 中的相关调用注释掉，或替换为其他资源。

### 运行

- 连接 Android 设备或启动模拟器（API 21+）。
- 点击 Run 按钮（绿色三角形）运行。

## 使用说明

### 开始界面
- 显示当前昵称（未设置时可点击修改）。
- 点击 **挑战模式** 直接开始挑战。
- 点击 **无尽模式** 选择难度后开始。
- **设置** 按钮可控制背景音乐和音效开关。
- **排行榜** 查看历史成绩。
- **退出游戏** 退出应用。

### 游戏界面
- 点击任意方块选中（高亮显示），再次点击另一个相同图案且路径可连通的方块完成消除。
- 顶部显示得分、剩余时间和进度条。
- 右上角“退出”按钮可暂停游戏并选择是否保存成绩。
- 完成一关后显示“下一关”按钮，最后一关通关后自动保存成绩。

### 排行榜
- 按模式（挑战/无尽）筛选。
- 无尽模式可按难度筛选。
- 支持删除记录。

## 贡献指南

欢迎提交 Issue 或 Pull Request。如果您想为项目贡献代码，请遵循以下步骤：

1. Fork 本仓库。
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)。
3. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)。
4. 推送到分支 (`git push origin feature/AmazingFeature`)。
5. 打开 Pull Request。

## 许可证

本项目基于 MIT 许可证开源。详见 [LICENSE](LICENSE) 文件。

## 联系

如果您有任何问题或建议，可以通过以下方式联系：

- 邮箱：your-email@example.com
- GitHub Issues：https://github.com/ElovisiaWinslow/LinkGame/issues

---

**Enjoy the game!** 🎉
