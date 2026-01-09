# 合同流程优化实施计划

## 目标

实现完整的合同签署流程：起草 → 聊天展示 → 双方签署 → 合同生效 → 合同管理---

## 任务清单

### 阶段一：后端支持

#### 1.1 添加 CONTRACT 消息类型

- 文件：`ChatServiceImpl.java`
- 在 msgType 校验中添加 `CONTRACT`
- 创建合同时自动发送 CONTRACT 消息到聊天

#### 1.2 完善签署接口

- 文件：`ContractController.java`, `ContractServiceImpl.java`
- 确保签署接口能处理打字签名
- 签署后更新合同状态

---

### 阶段二：前端 - 聊天合同卡片

#### 2.1 创建合同卡片消息组件

- 新文件：`frontend/src/components/chat/ContractCard.vue`
- 显示合同摘要（编号、产品、金额）
- 显示双方签署状态
- 提供"查看详情"和"签署"按钮

#### 2.2 更新聊天消息渲染

- 文件：`ChatView.vue`
- 识别 CONTRACT 类型消息并渲染 ContractCard

#### 2.3 更新合同起草弹窗

- 文件：`ContractDraftModal.vue`
- 创建合同成功后，调用发送合同消息接口

---

### 阶段三：前端 - 签署功能

#### 3.1 创建签署弹窗组件

- 新文件：`frontend/src/components/contract/ContractSignModal.vue`
- 支持三种签署方式切换：
- 打字签名（默认，输入姓名）
- 电子章（选择/上传）
- 手写签名（使用 SignaturePad）
- 签署确认后调用签署 API

---

### 阶段四：前端 - 合同管理页面

#### 4.1 创建合同列表页

- 新文件：`frontend/src/views/ContractListView.vue`
- 显示所有合同（草稿/待签/已签/履约中/已完成）
- 支持搜索和状态筛选
- 点击进入详情

#### 4.2 创建合同详情页

- 新文件：`frontend/src/views/ContractDetailView.vue`
- 显示合同完整信息
- 显示签署状态和签署记录
- 提供签署入口（如果未签）

#### 4.3 添加路由

- 文件：`frontend/src/router/index.ts`
- `/contracts` - 合同列表
- `/contracts/:id` - 合同详情

#### 4.4 更新侧边栏

- 文件：`frontend/src/components/layout/AppSidebar.vue`
- 添加"合同管理"菜单项

---

## 数据流设计

```javascript
创建合同 (ContractDraftModal)
    ↓
调用 POST /api/contracts/from-quote
    ↓
后端创建合同 + 自动发送 CONTRACT 消息
    ↓
聊天界面收到消息，渲染 ContractCard
    ↓
用户点击"签署" → 打开 ContractSignModal
    ↓
调用 POST /api/contracts/{id}/sign
    ↓
更新合同状态 + 广播状态变更
    ↓
双方都签署后 → 合同生效
```

---

## 实施优先级

1. **P0 - 必须**：CONTRACT 消息 + 合同卡片 + 签署弹窗
2. **P1 - 重要**：合同列表页 + 详情页 + 侧边栏入口
3. **P2 - 优化**：签署状态实时推送

---

## 预计工作量

- 后端改动：约 30 分钟
- 前端合同卡片 + 签署弹窗：约 1 小时
- 前端合同管理页面：约 1 小时
- 测试调试：约 30 分钟