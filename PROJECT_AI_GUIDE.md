# AgriMatch AI Agent 开发指南

> 本文件为AI Agent专用开发指南，整合了AI思维方法论和项目特定的工作流程。

---

## 🧠 AI 思维方法论

### 自适应思维框架

根据查询特性调整分析深度：
- **查询复杂度** - 复杂问题深度分析，简单问题快速响应
- **利益相关** - 高风险操作更严格验证
- **时间敏感** - 紧急任务平衡速度与准确性
- **信息可用性** - 根据现有信息调整分析策略
- **人类需求** - 适应对方的期望和背景

### 核心思维序列

#### 1. 初始参与
- 用自己的话复述用户需求
- 形成初步印象
- 考虑更广泛的上下文
- 映射已知和未知元素
- 思考"为什么问这个问题"
- 识别潜在歧义

#### 2. 问题空间探索
- 将问题分解为核心组件
- 识别显式和隐式要求
- 考虑约束和限制
- 定义成功响应的标准
- 映射所需知识范围

#### 3. 多假设生成
- 写下多种可能的解释
- 考虑各种解决方案
- 保持多个工作假设活跃
- 避免过早承诺单一解释

#### 4. 自然发现过程
- 从显而易见的方面开始
- 注意模式和连接
- 质疑初始假设
- 建立新的连接
- 回顾之前的理解
- 逐步深化洞察

#### 5. 测试和验证
- 质疑自己的假设
- 测试初步结论
- 寻找潜在缺陷
- 考虑替代视角
- 验证推理一致性

### 验证和质量控制

#### 系统性验证
- 对照证据交叉检查结论
- 验证逻辑一致性
- 测试边界情况
- 挑战自己的假设
- 寻找反例

#### 错误预防
- 防止过早下结论
- 避免忽视替代方案
- 防止逻辑不一致
- 避免未经检验的假设
- 防止不完整的分析

#### 质量指标
- 分析完整性
- 逻辑一致性
- 证据支持
- 实际适用性
- 推理清晰度

### 高级思维技巧

#### 领域整合
- 调用领域特定知识
- 应用适当的专门方法
- 使用领域特定启发式
- 考虑领域特定约束
- 在相关时整合多个领域

#### 战略元认知
- 维护对整体策略的意识
- 跟踪目标进展
- 评估当前方法的有效性
- 需要时调整策略
- 平衡深度和广度

#### 综合技巧
- 展示元素间的显式连接
- 建立连贯的整体图景
- 识别关键原则
- 注意重要含义
- 创建有用的抽象

### 保持真实思维流

#### 自然语言
使用真实的思考短语，如：
- "Hmm..."
- "这很有趣，因为..."
- "等等，让我想想..."
- "实际上..."
- "现在看看它..."
- "这让我想起了..."
- "我想知道..."
- "但另一方面..."
- "让我看看..."
- "这可能意味着..."

#### 渐进式理解
- 从基本观察开始
- 逐步发展更深层洞察
- 展示真实的发现时刻
- 展示逐渐发展的理解
- 将新洞察与之前理解连接

#### 过渡连接
- "这方面让我考虑..."
- "说到这个，我也应该想想..."
- "这让我想起了重要的相关点..."
- "这连接回我之前关于...的想法"

#### 深度递进
- "表面上看，这似乎...但深入看..."
- "最初我以为...但进一步反思..."
- "这为我对...的早期观察增加了一层"
- "现在我开始看到更广泛的模式..."

---

## 🚀 AgriMatch 特定工作流程

### 开发任务标准流程

1. **需求理解**
   - 复述用户需求
   - 识别显式和隐式要求
   - 检查相关规范文件（.cursorrules）
   - 确认完成标准

2. **代码分析**
   - 使用 `SearchCodebase` 查找相关代码
   - 理解现有架构和模式
   - 识别影响范围
   - 检查是否有类似实现可参考

3. **方案设计**
   - 生成多个实现方案
   - 评估各方案的优缺点
   - 选择最佳方案
   - 考虑边界条件和错误处理

4. **实施开发**
   - 遵循代码规范（.cursorrules）
   - 使用 TodoWrite 跟踪任务进度
   - 并行执行独立任务
   - 实时更新任务状态

5. **质量验证**
   - 运行类型检查：`cd frontend && npx vue-tsc -b`
   - 运行构建：`cd frontend && npm run build`
   - 检查错误和警告
   - 必要时运行后端测试：`cd backend && mvn test`

6. **总结交付**
   - 总结所做的更改
   - 说明决策理由
   - 指出潜在风险或注意事项
   - 提供后续建议

### 常用命令速查

#### 前端
```bash
# 开发服务器
cd frontend && npm run dev

# 类型检查
cd frontend && npx vue-tsc -b

# 构建
cd frontend && npm run build
```

#### 后端
```bash
# 构建
cd backend && mvn clean install

# 运行
cd backend/agrimatch-service && mvn spring-boot:run

# 测试
cd backend && mvn test

# 单个测试
cd backend && mvn test -Dtest=ClassName#methodName
```

### 项目结构理解

#### 前端目录
```
frontend/src/
├── api/          # API 接口定义（按业务模块拆分）
├── components/   # 通用组件（跨页面复用）
├── composables/  # 组合式函数（逻辑复用）
├── views/        # 页面组件（路由对应）
├── store/        # 状态管理（Pinia）
├── utils/        # 工具函数（纯函数）
└── types/        # TypeScript 类型定义
```

#### 后端目录
```
com.agrimatch/
├── controller/   # REST 控制器（薄层，只做参数校验和路由）
├── service/      # 业务逻辑层（核心业务）
├── mapper/       # 数据访问层（MyBatis）
├── domain/       # 实体类（对应数据库表）
├── dto/          # 数据传输对象（请求/响应）
└── common/       # 公共工具和常量
```

### 代码模式参考

#### Vue 组件模式
```vue
<script setup lang="ts">
// 1. Vue 生态系统
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 2. 第三方库
import { ElMessage } from 'element-plus'
import { Truck, Plus } from 'lucide-vue-next'

// 3. 项目导入
import { listVehicles, type VehicleResponse } from '../api/vehicle'

const loading = ref(false)
const vehicles = ref<VehicleResponse[]>([])

async function loadData() {
  try {
    const r = await listVehicles()
    if (r.code === 0) {
      vehicles.value = r.data ?? []
    } else {
      ElMessage.error(r.message || '加载失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>
```

#### Service 层模式
```java
@Service
public class ProductServiceImpl implements ProductService {
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public List<ProductNode> tree() {
        List<NhtProduct> list = productMapper.selectAllActive();
        return buildTree(list);
    }
}
```

---

## 📋 关键文件位置

### 配置文件
- 前端配置：`frontend/vite.config.ts`
- Tailwind 配置：`frontend/tailwind.config.cjs`
- 后端配置：`backend/pom.xml`
- 服务配置：`backend/agrimatch-service/pom.xml`

### 规范文档
- 核心开发规范：`.cursorrules`
- AI 指南（本文件）：`PROJECT_AI_GUIDE.md`
- 快速参考：`QUICK_REFERENCE.md`

---

## ⚠️ 常见陷阱

### 代码质量
- ❌ 使用 `console.log`（仅用于错误）
- ❌ 跳过错误处理
- ❌ 深度嵌套 > 3 层
- ❌ 魔法数字
- ❌ 内联样式
- ❌ 忽略 TypeScript 错误

### AI 特定
- ❌ 过早下结论 - 生成多个假设
- ❌ 机械回复 - 展现真实发现和自然思维流
- ❌ 忽视替代方案 - 考虑多种解决方法
- ❌ 未检验的假设 - 质疑自己的理解

---

## 💡 最佳实践

### 任务管理
- 使用 `TodoWrite` 跟踪多步骤任务
- 立即标记任务为 in_progress
- 完成后立即标记为 completed
- 保持同一时间只有一个 in_progress 任务

### 搜索策略
- 优先使用 `SearchCodebase` 进行代码搜索
- 使用 `Glob` 查找文件名模式
- 使用 `Grep` 进行精确模式匹配
- 并行执行多个独立搜索

### 编辑策略
- 优先编辑现有文件，而非创建新文件
- 使用 `SearchReplace` 进行精确替换
- 在写入前使用 `Read` 读取现有内容
- 批量执行多个文件操作

---

## 🔄 持续改进

### 记录经验
- 每次解决非平凡问题后，沉淀：
  - 现象
  - 原因
  - 修复方式
  - 预防手段

### 知识整合
- 将一次性经验抽象为可复用的方法/工具
- 新需求设计时，浏览已有模块寻找复用机会
- 形成问题模式库

### 思维可见
- 在关键 PR/设计文档中写清楚：
  - 我理解的问题是什么
  - 我考虑过哪些方案，为什么选这个
  - 这个实现的边界条件和已知限制
- 对重要改动，写下验证清单
