# App Structure

## 分层结构

- `data/`
  - `repository/`
    - `LearningRepository`：统一业务接口
    - `FakeLearningRepository`：演示数据源（默认启用）
    - `LearningRepositoryImpl`：Room 实现（已保留，便于切换）
  - `local/`：Room 实体与 DAO
  - `seed/`：JSON 词库加载
  - `model/`：跨页面业务模型（含 `HomeDashboard`、`LessonHighlight`）
- `ui/viewmodel/`
  - 按页面拆分状态与交互
  - 例如：`HomeViewModel`、`SceneLessonsViewModel`、`LessonPreviewViewModel`、`LessonFlowViewModel`
- `ui/screen/`
  - 按功能拆分页面，不把流程页面塞进一个大文件
  - 学习流程拆为：字卡、跟读、听音选字、看图选字、写字、场景判断、完成页
- `app/navigation/`
  - `AppRoutes` + `AppNavHost` + `AppNavigation`
  - 支持完整链路跳转

## 演示链路

1. `HomeScreen`
2. `SceneListScreen`
3. `LessonListScreen`
4. `LessonPreviewScreen`
5. `LessonFlowScreen`（内部再分 7 个步骤）
6. `LessonCompleteScreen`
7. `ReviewScreen / WordBookScreen / CaregiverScreen`

## 可商业化扩展点（已预留）

- 仓储抽象：`LearningRepository` 可从 Fake 切到 Room/网络混合实现
- 音频抽象：`AudioManager`、`RecordingManager` 已有接口与 mock 实现
- 课程模型扩展：`LessonWithStatus` 已包含词数、掌握度、重点字、预计时长
- 首页运营位：`HomeDashboard` 支持继续学习、推荐课程、易忘字等可运营内容
- 路由清晰：独立 `LessonPreview` 路由，便于后续加入付费试学/引导页

## 目录

```text
app/src/main/java/com/peimama/renzi
├─ app/
│  └─ navigation/
├─ audio/
├─ data/
│  ├─ local/
│  ├─ model/
│  ├─ repository/
│  └─ seed/
├─ di/
├─ ui/
│  ├─ components/
│  ├─ screen/
│  │  ├─ home/
│  │  ├─ scene/
│  │  ├─ lesson/
│  │  ├─ review/
│  │  ├─ notebook/
│  │  └─ family/
│  ├─ theme/
│  └─ viewmodel/
└─ MainActivity.kt
```
