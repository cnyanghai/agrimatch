# 前端开发说明（AgriMatch）

## 本地启动

- 安装依赖：
  - `npm install`
- 启动开发服务器（默认 `5173`）：
  - `npm run dev`

后端默认代理到 `http://localhost:8080`（见 `vite.config.ts`）。

## 高德地图 Key 配置

全域地图页面使用高德地图 JS API，需要在前端环境变量里配置：

- `VITE_AMAP_JS_KEY`
- （可选）`VITE_AMAP_SECURITY_JS_CODE`（部分新建 Key 会要求“安全密钥”）

推荐做法：在 `frontend/` 目录下创建 `.env.local`：

```bash
VITE_AMAP_JS_KEY=你的高德JSKey
VITE_AMAP_SECURITY_JS_CODE=你的高德安全密钥(可选)
```

修改后重启前端。

后端地址解析（公司地址 -> 坐标）使用高德 Web 服务 Key（环境变量）：

- `AMAP_WEB_KEY=你的高德Web服务Key`

Key 获取入口（高德控制台）：[https://console.amap.com/dev/key/app](https://console.amap.com/dev/key/app)

# Vue 3 + TypeScript + Vite

This template should help get you started developing with Vue 3 and TypeScript in Vite. The template uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.

Learn more about the recommended Project Setup and IDE Support in the [Vue Docs TypeScript Guide](https://vuejs.org/guide/typescript/overview.html#project-setup).
