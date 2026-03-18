# 陪妈妈认字（可演示安装版）

面向 50 岁以上、识字基础薄弱用户的离线识字学习 App，目标是“会认、会读、会写、会用”。

## 1. 当前版本

- 首页、场景、课程、字卡、整课学习流程已打通
- 复习 / 我的字本 / 家属陪学 / 词组练习可用
- 已修复：词库乱码、语音可用性、看图识字空内容、写字页手势卡顿

## 2. 已实现页面

- `HomeScreen`
- `SceneListScreen`
- `LessonListScreen`
- `LessonPreviewScreen`
- `FlashcardScreen`
- `ReadAlongScreen`
- `AudioChoiceScreen`
- `ImageChoiceScreen`
- `WritingPracticeScreen`
- `SceneJudgeScreen`
- `LessonCompleteScreen`
- `ReviewScreen`
- `WordBookScreen`
- `CaregiverScreen`
- `PhraseScreen`

## 3. 学习流程

1. 看图识字
2. 跟读（播放标准发音 / 录音 / 回放 / 重录）
3. 听音选字
4. 看图选字
5. 写字描红
6. 场景判断
7. 完成页

## 4. 数据规模

词库：`app/src/main/assets/sample_words.json`

- 21 个场景
- 436 节课
- 3992 个学习条目
- 含 `3000常用字通关`（300 课 x 10 字）

## 5. Mock 说明

- 音频：优先本地 `raw` 音频；无资源时自动回退系统 TTS
- 录音：麦克风录音（需要 `RECORD_AUDIO` 权限）
- 图片：使用场景视觉卡动态渲染（可后续替换真实图片）

## 6. 运行方式

1. Android Studio 打开项目目录：`D:\识字app`
2. 使用 JDK 17+
3. 安装 Android SDK（建议 API 35）
4. Gradle Sync 后运行 `app`

Windows PowerShell 构建：

```powershell
$env:JAVA_HOME=(Resolve-Path 'tools/jdk-17.0.18+8').Path
$env:ANDROID_SDK_ROOT=(Resolve-Path 'android-sdk-temp').Path
$env:ANDROID_HOME=$env:ANDROID_SDK_ROOT

$androidHome = Join-Path (Get-Location) '.android-home'
if(-not(Test-Path $androidHome)){ New-Item -ItemType Directory -Path $androidHome | Out-Null }
$env:ANDROID_USER_HOME=$androidHome

$localApp = Join-Path (Get-Location) '.localapp'
if(-not(Test-Path $localApp)){ New-Item -ItemType Directory -Path $localApp | Out-Null }
$env:LOCALAPPDATA=$localApp
$env:APPDATA=$localApp

$tmp = Join-Path (Get-Location) '.temp'
if(-not(Test-Path $tmp)){ New-Item -ItemType Directory -Path $tmp | Out-Null }
$env:TEMP=$tmp
$env:TMP=$tmp

$env:GRADLE_USER_HOME=(Resolve-Path '.gradle').Path
.\gradlew.bat :app:assembleDebug
```

## 7. 安装包

- `dist/app-debug-v3000-fix1.apk`
- `dist/app-debug.apk`（最新调试包）

## 8. 文档

- `docs/app-structure.md`
- `docs/future-roadmap.md`

## 9. 预览建议

先从首页进入“3000常用字通关”，再从“家里识字/买菜识字”演示整课流程。
