# AgriMatch MCP 配置指南

本文档指导您在 Cursor 中配置 MCP (Model Context Protocol) 服务，以增强开发效率和 AI 能力。

---

## 已配置的 MCP 服务（共 6 个）

| 服务 | 包名 | 功能 |
|------|------|------|
| **MySQL** | `mcp-server-mysql` | 直接查询 `nonghuitong` 数据库 |
| **Fetch** | `fetch-mcp` | 调用外部 HTTP API |
| **Filesystem** | `@modelcontextprotocol/server-filesystem` | 增强文件系统操作 |
| **Sequential Thinking** | `@modelcontextprotocol/server-sequential-thinking` | 逻辑链增强，多步骤推理 |
| **GitHub** | `github-mcp` | GitHub 仓库管理与 Issue 操作 |
| **Puppeteer** | `puppeteer-mcp-server` | 网页自动化、截图、PDF 生成 |

---

## 配置文件

配置文件位于：`.cursor/mcp.json`

```json
{
  "mcpServers": {
    "mysql": {
      "command": "npx",
      "args": [
        "-y",
        "mcp-server-mysql",
        "mysql://root:red1986@localhost:3306/nonghuitong"
      ],
      "env": {
        "MYSQL_URL": "mysql://root:red1986@localhost:3306/nonghuitong"
      }
    },
    "fetch": {
      "command": "npx",
      "args": ["-y", "fetch-mcp"]
    },
    "filesystem": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "/home/cnyanghai/nongV1"
      ]
    },
    "sequential-thinking": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-sequential-thinking"]
    },
    "github": {
      "command": "npx",
      "args": ["-y", "github-mcp"]
    },
    "puppeteer": {
      "command": "npx",
      "args": ["-y", "puppeteer-mcp-server"]
    }
  }
}
```

---

## 服务详细说明

### 1. MySQL MCP
**用途**: 直接在 Cursor 中查询和管理 `nonghuitong` 数据库。

**功能**:
- 执行 SQL 查询（SELECT/INSERT/UPDATE/DELETE）
- 查看表结构
- 数据分析与调试

**验证命令**:
```
使用 MySQL MCP 查询 bus_company 表的前 3 条记录
```

---

### 2. Fetch MCP
**用途**: 获取外部 HTTP 资源，测试 API 接口。

**功能**:
- 调用 REST API
- 获取网页内容
- 测试高德地图等第三方 API

**验证命令**:
```
使用 Fetch MCP 获取 https://httpbin.org/get 的内容
```

---

### 3. Filesystem MCP
**用途**: 增强文件系统操作能力。

**功能**:
- 批量文件操作
- 目录遍历
- 文件内容搜索

**配置路径**: `/home/cnyanghai/nongV1`

---

### 4. Sequential Thinking MCP (逻辑链增强)
**用途**: 引导 AI 进行多步骤、深层次的逻辑推理。

**特点**:
- 符合项目的"神级思维"规范
- 显著降低处理复杂业务逻辑（如运费计算、成交闭环）时的差错率
- 支持思维过程的可追溯与回溯

**适用场景**:
- 复杂业务逻辑分析（如 MVP 交易闭环）
- 多条件判断与边界情况处理
- 代码重构与架构设计

**验证命令**:
```
使用 Sequential Thinking 分析：当需求发布人确认成交时，系统需要执行哪些步骤？请列出完整的逻辑链。
```

---

### 5. GitHub MCP
**用途**: 在 Cursor 中直接与 GitHub 仓库交互。

**功能**:
- 检索公开仓库信息
- 管理 Issue（创建、更新、关闭）
- 查看 Pull Request
- 搜索代码和文档

**验证命令**:
```
使用 GitHub MCP 搜索 "modelcontextprotocol" 组织下的仓库列表
```

**注意**: 如需操作私有仓库或进行写操作，请添加 GitHub Personal Access Token：
```json
"github": {
  "command": "npx",
  "args": ["-y", "github-mcp"],
  "env": {
    "GITHUB_TOKEN": "your-personal-access-token"
  }
}
```

---

### 6. Puppeteer MCP (新增)
**用途**: 提供网页自动化、截图和 PDF 生成能力。

**功能**:
- 网页截图（支持全页或指定元素）
- PDF 生成（用于合同预览/下载）
- 表单自动填充
- 页面交互自动化

**适用场景**:
- 电子合同预览与 PDF 导出
- 自动化测试页面功能
- 生成页面快照用于文档

**验证命令**:
```
使用 Puppeteer MCP 截取 https://example.com 的首页截图
```

**高级用法示例**:
```
使用 Puppeteer 打开 http://localhost:5173，等待页面加载完成后截取全页截图
```

---

## 已集成的 Smithery 技能

基于 [Smithery.ai](https://smithery.ai/skills) 调研，以下专家技能已融入项目的 `.cursorrules` 规范：

| 技能 | 来源 | 已集成内容 |
|------|------|-----------|
| **UI/UX Pro Max** | `nextlevelbuilder/ui-ux-pro-max` | Bento Grid 布局、毛玻璃效果、微交互动效 |
| **Vue Engineering 2025** | `vinceh/vue-engineering` | Composition API 最佳实践、Composables 规范 |
| **Spring Boot Best Practices** | `kousen/spring-boot-best-practices` | Controller/Service/Mapper 分层规范 |
| **Spring Boot REST API Standards** | `giuseppe-trisciuoglio/spring-boot-rest-api-standards` | 统一响应格式、参数校验、异常处理 |

---

## 激活配置

配置完成后，请执行以下操作之一：
1. **点击 Retry**: 在 Cursor MCP 设置界面点击对应服务的 "Retry" 按钮
2. **重启 Cursor**: 完全关闭并重新打开 Cursor

---

## 常见问题

### Q: MCP 服务显示红色错误？
**A**: 检查以下几点：
1. 确认 Node.js 已安装（`node --version`）
2. 确认网络正常，可以访问 npm 仓库
3. 查看 Cursor 输出面板的具体错误信息

### Q: MySQL 连接失败？
**A**: 检查：
1. MySQL 服务是否运行：`sudo systemctl status mysql`
2. 用户名密码是否正确
3. 数据库 `nonghuitong` 是否存在

### Q: Puppeteer 无法启动？
**A**: 可能原因：
1. 首次运行需要下载 Chromium，请等待完成
2. Linux 环境可能缺少依赖，运行：`sudo apt install -y libx11-xcb1 libxcomposite1 libxdamage1 libxrandr2 libgbm1 libasound2 libpangocairo-1.0-0 libatk1.0-0 libcups2 libdrm2`

### Q: Sequential Thinking 没有效果？
**A**: 这是一个辅助工具，AI 会在处理复杂问题时自动调用。您也可以显式要求使用它。

---

## 参考资源

- [MCP 官方文档](https://modelcontextprotocol.io/)
- [MCP Servers 列表](https://github.com/modelcontextprotocol/servers)
- [Cursor MCP 配置指南](https://docs.cursor.com/context/model-context-protocol)
- [Smithery Skills 市场](https://smithery.ai/skills)
