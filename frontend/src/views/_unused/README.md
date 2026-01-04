## 归档说明（_unused）

该目录用于存放**当前未被路由挂载、也未被代码 import** 的历史页面/草稿页面，便于：

- 降低 `src/views` 目录噪音，避免误用旧页面
- 保留实现以便后续参考/迁移

### 目前归档的文件

- `HomeView.vue`：旧版落地页实现（当前项目入口已使用 `views/gemini/GeminiLandingView.vue`）
- `SupplyPublishView.vue`：旧版/草稿发布页（当前发布入口使用 `views/SupplyView.vue` 等）

如需恢复使用，请将文件移回 `src/views/` 并在 `src/router/index.ts` 中挂载路由。


