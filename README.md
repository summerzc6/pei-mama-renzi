# 陪妈妈认字（MVP 演示版）

面向 50 岁以上、识字基础薄弱用户的离线识字学习 App。首版目标是围绕真实生活场景，帮助用户做到“会认、会读、会写、会用”。

## 1. 当前版本定位

这是可直接演示给家人和潜在开发者的版本，不是空壳 Demo：

- 首页为产品化仪表盘（今日学习、连续天数、继续学习、推荐课程）
- 场景 -> 课程 -> 字卡预览 -> 学习流程 的跳转完整
- 复习页、我的字本、家属陪学可联动到学习流程
- 内置较完整场景词库与初始学习记录，首次打开就有可看内容

## 2. 已实现页面（Compose）

- `HomeScreen`
- `SceneListScreen`
- `LessonListScreen`
- `LessonPreviewScreen`
- `FlashcardScreen`
- `ReadAlongScreen`
- `AudioChoiceScreen`
- `ImageChoiceScreen`
- `WritingPracticeScreen`（Compose Canvas 手写板）
- `SceneJudgeScreen`
- `LessonCompleteScreen`
- `ReviewScreen`
- `WordBookScreen`
- `CaregiverScreen`

## 3. 学习流程（单课）

每课固定流程：

1. 看图识字（字卡）
2. 跟读（播放发音 / 录音 / 回放 / 重录）
3. 听音选字
4. 看图选字
5. 写字描红（手写板）
6. 场景判断
7. 完成页

## 4. 技术栈与架构

- Kotlin
- Jetpack Compose + Material 3
- MVVM
- Navigation Compose
- Room（数据结构完整）
- Fake Repository（首版默认数据源，保证无后端可运行）

关键版本（当前工程）：

- AGP: `8.5.2`
- Kotlin: `1.9.24`
- Compose BOM: `2024.06.00`
- Navigation Compose: `2.7.7`
- Room: `2.6.1`
- Serialization: `1.6.3`

## 5. 数据与 Mock 说明

- 词库文件：`app/src/main/assets/sample_words.json`
- 场景覆盖：厨房家庭、买菜购物、外出交通、医院看病、手机生活
- 首版仓储：`FakeLearningRepository`
  - 启动即带“已学/学习中/待复习/今日统计”等演示数据
- 音频：`MockAudioManager`
- 录音：`MockRecordingManager`
- 图片：占位图逻辑（结构完整，后续可替换真实资源）

## 6. 如何运行

1. 用 Android Studio 打开项目目录：`D:\识字app`
2. 使用 JDK 17+
3. 安装 Android SDK（建议 API 35）
4. Sync Gradle
5. 运行 `app`

## 7. 导航与数据流摘要

- 首页 -> 场景列表 -> 课程列表 -> 课程预览 -> 学习流程
- 首页推荐课程 / 继续学习 -> 课程预览 -> 学习流程
- 我的字本 -> 指定课程预览 -> 学习流程
- 复习页 -> 对应课程学习流程

UI 由 `ViewModel` 订阅 `LearningRepository` 的 `Flow` 数据；首版使用 Fake 仓储，后续可平滑切换到 Room 实现。

## 8. 一键产出分享链接（APK）

项目已内置 GitHub Actions 自动打包：

- 工作流：`.github/workflows/android-apk.yml`
- 文档：`docs/share-preview-link.md`

使用方式：

1. 把仓库推送到 GitHub
2. 在 Actions 运行 `Android APK Build`
3. 推送标签 `v0.1.0` 后会自动创建 Release 并上传 APK
4. 分享链接格式：

`https://github.com/<你的用户名>/<仓库名>/releases/download/v0.1.0/app-debug.apk`

## 9. 本地检查说明

- 依赖解析已验证通过（`debugRuntimeClasspath`）
- 已对齐 Kotlin 依赖冲突，避免 `kotlin-stdlib` 版本不一致
- 当前终端环境缺少 Android SDK 路径，因此无法在终端执行 `:app:compileDebugKotlin` / `assembleDebug`
  - 在 Android Studio 配置 `sdk.dir` 或 `ANDROID_HOME` 后即可完成本地编译验证

## 10. 文档

- 架构文档：`docs/app-structure.md`
- 未来规划：`docs/future-roadmap.md`
- 分享链接：`docs/share-preview-link.md`

## 11. 建议预览入口

先从首页（`HomeScreen`）开始预览，再按“推荐课程”进入完整学习链路。
