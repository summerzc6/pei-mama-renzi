# 分享预览链接（APK）

## 目标

生成一个可分享给家人或开发者的 APK 下载链接。

## 已配置

项目已内置 GitHub Actions 工作流：

- 文件：`.github/workflows/android-apk.yml`
- 触发方式：
  - 手动触发（`workflow_dispatch`）
  - 推送标签 `v*`（如 `v0.1.0`）
- 输出内容：
  - Actions Artifact：`peimama-renzi-debug-apk`
  - 当推送标签时，自动创建 Release 并附带 `app-debug.apk`

## 使用步骤

1. 将仓库推送到 GitHub。
2. 打开仓库 `Actions` 页面，运行 `Android APK Build`。
3. 方式A（临时分享）：在该次运行的 `Artifacts` 下载 APK。
4. 方式B（稳定链接）：创建并推送标签，例如：

```bash
git tag v0.1.0
git push origin v0.1.0
```

然后使用 Release 链接：

`https://github.com/<你的用户名>/<仓库名>/releases/download/v0.1.0/app-debug.apk`

## 说明

- 这是 Debug APK，适合演示预览。
- 如需正式分发，可后续增加签名与 Release 构建流程。
