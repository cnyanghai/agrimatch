# 聊天记录：Phase 5 交易闭环实现

- **日期**：2026-02-03
- **序号**：003
- **主要任务**：实现 Phase 5 交易闭环 — 聊天报价 + 合同签约 + 里程碑 + 话题编辑

## 对话摘要

完成了 App 全面重设计 Phase 5 的全部 17 个步骤，包括 Phase 5a（聊天报价系统 + 合同管理增强）和 Phase 5b（里程碑管理 + 话题编辑）。所有代码通过 `vue-tsc --noEmit` 类型检查。

## 完成的工作

### Phase 5a: 聊天报价 + 合同签约 (12 步)

1. **修改 `api/chat.ts`** — ChatMessageResponse 新增 payloadJson/quoteStatus 字段，新增 4 个 API：sendQuoteMessage、confirmChatOffer、rejectChatOffer、createContractFromQuote
2. **修改 `api/contract.ts`** — 新增 7 个 API：getNextContractNo、updateContract、deleteContract、sendForSigning、signContract、cancelContract、getContractPdfUrl + ContractSignRequest 接口
3. **新建 `api/milestone.ts`** — 里程碑 API 7 个端点：list、create、submit、confirm、reject、delete、generateStandard
4. **新建 `composables/useContractActions.ts`** — 合同操作 composable，权限判断 + 操作函数
5. **新建 `components/WgQuoteCard.vue`** — 报价卡片组件，支持 OFFERED/ACCEPTED/REJECTED/EXPIRED 状态
6. **新建 `pages/chat/quote-form.vue`** — 报价表单页（含预填逻辑）
7. **修改 `pages/chat/conversation.vue`** — 核心改造：报价消息渲染、"+"功能按钮、报价操作处理、系统消息渲染
8. **新建 `pages/contract/draft.vue`** — 从报价起草合同页
9. **修改 `pages/contract/detail.vue`** — 新增操作栏（编辑/删除/发送签署/签署/取消）+ 里程碑时间线预览
10. **修改 `pages/contract/list.vue`** — 新增"草稿" tab + 提示卡片
11. **修改 `composables/useWebSocket.ts`** — 新增 sendQuoteMessage 方法 + WsMessage 扩展 payloadJson/quoteStatus
12. **修改 `pages.json`** — 注册 quote-form 和 draft 路由

### Phase 5b: 里程碑 + 话题编辑 (5 步)

13. **新建 `pages/contract/milestones.vue`** — 里程碑时间线管理页，支持提交/确认/驳回/删除/自动生成
14. **修改 `pages/topic/detail.vue`** — 作者"..."菜单（编辑/删除），isMyPost 判断
15. **新建 `pages/topic/edit.vue`** — 话题编辑页，onLoad 预填内容
16. **修改 `api/post.ts`** — 新增 updatePost + deletePost
17. **修改 `pages.json`** — 注册 milestones 和 topic/edit 路由

## 修改的文件

- `app/src/api/chat.ts` — 消息类型扩展 + 4 新端点
- `app/src/api/contract.ts` — 7 新端点 + 签名类型
- `app/src/api/milestone.ts` — 里程碑 API (新建)
- `app/src/api/post.ts` — updatePost + deletePost
- `app/src/composables/useContractActions.ts` — 合同操作 composable (新建)
- `app/src/composables/useWebSocket.ts` — 报价 WS 发送
- `app/src/components/WgQuoteCard.vue` — 报价卡片组件 (新建)
- `app/src/pages/chat/conversation.vue` — 报价消息渲染 + 操作
- `app/src/pages/chat/quote-form.vue` — 报价表单页 (新建)
- `app/src/pages/contract/detail.vue` — 操作栏 + 里程碑预览
- `app/src/pages/contract/draft.vue` — 报价→合同起草页 (新建)
- `app/src/pages/contract/list.vue` — 草稿 tab
- `app/src/pages/contract/milestones.vue` — 里程碑时间线页 (新建)
- `app/src/pages/topic/detail.vue` — 作者编辑/删除菜单
- `app/src/pages/topic/edit.vue` — 话题编辑页 (新建)
- `app/src/pages.json` — 4 新路由

## 技术决策

- 报价使用 V1 JSON 格式 payloadJson，支持未来扩展基差报价
- 合同操作使用 composable 模式封装权限判断和操作函数
- 聊天输入栏新增 "+" 按钮使用 actionSheet，不破坏现有布局
- 里程碑使用时间线 UI，节点状态用颜色编码区分
- 话题编辑复用 publish.vue 的表单结构和样式

## 待办事项

- [ ] 后端需确认 QUOTE 消息类型 WebSocket 协议
- [ ] 合同编辑页面（目前显示"开发中"提示）
- [ ] 基差报价（V1 基差格式）支持
- [ ] 合同 PDF 预览/下载功能
