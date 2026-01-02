<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, ChatDotRound, View, Star, Phone, Location, ShoppingCart, Sort, Refresh, Money, TrendCharts, Timer, Box, Document } from '@element-plus/icons-vue'
import { listRequirements, type RequirementResponse } from '../api/requirement'
import { getProductTree, type ProductNode } from '../api/product'

const router = useRouter()
const loading = ref(false)

// Tab 切换：followed - 关注商户，all - 全部信息
const activeTab = ref('followed')

// 原始采购需求列表
const rawRequirements = ref<RequirementResponse[]>([])

// 关注商户的采购需求（模拟数据）
const followedRequirements = ref<RequirementResponse[]>([
  {
    id: 201,
    categoryName: '小麦',
    companyName: '北京粮油贸易公司',
    companyId: 10,
    userId: 20001,
    nickName: '赵采购',
    quantity: 500,
    expectedPrice: 2900,
    purchaseAddress: '北京市朝阳区',
    paymentMethod: '现款',
    deliveryMethod: '送货上门',
    paramsJson: JSON.stringify({ '水分': '≤13%', '杂质': '≤1%', '容重': '≥760g/L' }),
    createTime: new Date(Date.now() - 1 * 60 * 60 * 1000).toISOString(),
    status: 1
  },
  {
    id: 202,
    categoryName: '玉米',
    companyName: '上海食品加工厂',
    companyId: 11,
    userId: 20002,
    nickName: '钱经理',
    quantity: 1000,
    expectedPrice: 2650,
    purchaseAddress: '上海市浦东新区',
    paymentMethod: '账期',
    deliveryMethod: '物流',
    paramsJson: JSON.stringify({ '水分': '≤14%', '霉变': '≤2%' }),
    createTime: new Date(Date.now() - 3 * 60 * 60 * 1000).toISOString(),
    status: 1
  },
  {
    id: 203,
    categoryName: '大豆',
    companyName: '江苏食品集团',
    companyId: 12,
    userId: 20003,
    nickName: '孙总监',
    quantity: 300,
    expectedPrice: 4700,
    purchaseAddress: '江苏省南京市',
    paymentMethod: '承兑',
    deliveryMethod: '自提',
    paramsJson: JSON.stringify({ '蛋白质': '≥38%', '水分': '≤13%' }),
    createTime: new Date(Date.now() - 8 * 60 * 60 * 1000).toISOString(),
    status: 1
  }
])

// 品类树
const categoryTree = ref<ProductNode[]>([])

// 筛选条件
const filters = reactive({
  keyword: '',
  categoryName: '',
  minQuantity: undefined as number | undefined,
  maxQuantity: undefined as number | undefined,
  paymentMethod: '',
  deliveryMethod: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 12,
  total: 0
})

// 排序配置
const sortConfig = reactive({
  field: 'createTime',
  order: 'desc' as 'asc' | 'desc'
})

// 快捷排序按钮
const sortButtons = [
  { field: 'expectedPrice', order: 'desc', label: '期望价最高', icon: 'Money' },
  { field: 'expectedPrice', order: 'asc', label: '期望价最低', icon: 'TrendCharts' },
  { field: 'createTime', order: 'desc', label: '最新发布', icon: 'Timer' },
  { field: 'quantity', order: 'desc', label: '需求量最大', icon: 'Box' },
  { field: 'quantity', order: 'asc', label: '需求量最小', icon: 'Document' },
]

// icon 字符串映射为真实组件，避免 <component :is="btn.icon" /> 找不到组件
const iconMap: Record<string, any> = {
  Money,
  TrendCharts,
  Timer,
  Box,
  Document
}

// 付款方式选项
const paymentMethods = ['现款', '账期', '承兑', '预付款']

// 交货方式选项
const deliveryMethods = ['送货上门', '自提', '物流']

// 当前激活的排序按钮
const activeSort = computed(() => `${sortConfig.field}-${sortConfig.order}`)

// 设置排序
function setSort(field: string, order: 'asc' | 'desc') {
  sortConfig.field = field
  sortConfig.order = order
  pagination.page = 1
}

// 排序后的列表
const sortedRequirements = computed(() => {
  const list = [...rawRequirements.value]
  
  return list.sort((a, b) => {
    let valA: any = a[sortConfig.field as keyof RequirementResponse]
    let valB: any = b[sortConfig.field as keyof RequirementResponse]
    
    // 处理日期排序
    if (sortConfig.field === 'createTime') {
      valA = valA ? new Date(valA).getTime() : 0
      valB = valB ? new Date(valB).getTime() : 0
    }
    
    // 处理数值排序
    if (typeof valA === 'number' || typeof valB === 'number') {
      valA = valA ?? 0
      valB = valB ?? 0
    }
    
    if (sortConfig.order === 'asc') {
      return valA > valB ? 1 : -1
    } else {
      return valA < valB ? 1 : -1
    }
  })
})

// 筛选后的列表
const filteredRequirements = computed(() => {
  let result = sortedRequirements.value
  
  // 关键词搜索
  if (filters.keyword) {
    const kw = filters.keyword.toLowerCase()
    result = result.filter(r => 
      r.categoryName?.toLowerCase().includes(kw) ||
      r.companyName?.toLowerCase().includes(kw) ||
      r.purchaseAddress?.toLowerCase().includes(kw)
    )
  }
  
  // 品类筛选
  if (filters.categoryName) {
    result = result.filter(r => r.categoryName === filters.categoryName)
  }
  
  // 数量筛选
  if (filters.minQuantity !== undefined) {
    result = result.filter(r => (r.quantity || 0) >= filters.minQuantity!)
  }
  if (filters.maxQuantity !== undefined) {
    result = result.filter(r => (r.quantity || 0) <= filters.maxQuantity!)
  }
  
  // 付款方式筛选
  if (filters.paymentMethod) {
    result = result.filter(r => r.paymentMethod === filters.paymentMethod)
  }
  
  // 交货方式筛选
  if (filters.deliveryMethod) {
    result = result.filter(r => r.deliveryMethod === filters.deliveryMethod)
  }
  
  pagination.total = result.length
  
  // 分页
  const start = (pagination.page - 1) * pagination.size
  return result.slice(start, start + pagination.size)
})

// 加载采购需求列表
async function loadRequirements() {
  loading.value = true
  try {
    const r = await listRequirements({
      status: 1
    })
    if (r.code === 0) {
      rawRequirements.value = r.data || []
      pagination.total = rawRequirements.value.length
    }
  } catch (e) {
    ElMessage.error('加载采购需求失败')
  } finally {
    loading.value = false
  }
}

// 加载品类树
async function loadCategoryTree() {
  try {
    const r = await getProductTree()
    if (r.code === 0) {
      categoryTree.value = r.data || []
    }
  } catch (e) {
    // 静默失败
  }
}

// 获取所有品类名称
const allCategories = computed(() => {
  const categories = new Set<string>()
  rawRequirements.value.forEach(r => {
    if (r.categoryName) categories.add(r.categoryName)
  })
  return Array.from(categories)
})

// 解析参数 JSON
function parseParams(paramsJson?: string): Array<{name: string; value: string}> {
  if (!paramsJson) return []
  try {
    const obj = JSON.parse(paramsJson)
    return Object.entries(obj).map(([name, value]) => ({
      name,
      value: String(value)
    }))
  } catch {
    return []
  }
}

// 查看详情
function viewDetail(requirement: RequirementResponse) {
  ElMessage.info(`查看采购详情：${requirement.categoryName}`)
}

// 发起聊天/报价
function startChat(requirement: RequirementResponse) {
  router.push({
    path: '/chat',
    query: {
      type: 'requirement',
      id: requirement.id,
      companyName: requirement.companyName
    }
  })
}

// 收藏
function toggleFavorite(_requirement: RequirementResponse) {
  ElMessage.success('收藏功能开发中...')
}

// 重置筛选
function resetFilters() {
  filters.keyword = ''
  filters.categoryName = ''
  filters.minQuantity = undefined
  filters.maxQuantity = undefined
  filters.paymentMethod = ''
  filters.deliveryMethod = ''
  sortConfig.field = 'createTime'
  sortConfig.order = 'desc'
  pagination.page = 1
}

// 格式化时间
function formatTime(timeStr?: string): string {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`
  return timeStr.split('T')[0] ?? timeStr
}

// 计算剩余数量百分比
function getRemainingPercent(req: RequirementResponse): number {
  if (!req.quantity) return 100
  const remaining = req.remainingQuantity ?? req.quantity
  return Math.round((remaining / req.quantity) * 100)
}

onMounted(() => {
  loadCategoryTree()
  loadRequirements()
})
</script>

<template>
  <div class="max-w-7xl mx-auto space-y-6">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">采购浏览</h1>
        <p class="text-gray-500 mt-1">浏览采购商发布的原料采购需求，寻找商机</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadRequirements">刷新数据</el-button>
    </div>

    <!-- Tab 切换 -->
    <div class="tab-bar">
      <el-segmented v-model="activeTab" :options="[
        { label: '⭐ 关注商户', value: 'followed' },
        { label: '📋 全部信息', value: 'all' }
      ]" size="large" />
      <div class="tab-hint">
        <span v-if="activeTab === 'followed'" class="text-gray-500 text-sm">
          共 <span class="font-bold text-blue-700">{{ followedRequirements.length }}</span> 条关注商户发布的需求
        </span>
        <span v-else class="text-gray-500 text-sm">
          共 <span class="font-bold text-blue-700">{{ pagination.total }}</span> 条采购需求
        </span>
      </div>
    </div>

    <!-- 关注商户为空时的提示 -->
    <div v-if="activeTab === 'followed' && followedRequirements.length === 0" class="empty-followed">
      <el-icon class="empty-followed-icon"><Star /></el-icon>
      <div class="empty-followed-title">暂无关注的商户</div>
      <div class="empty-followed-hint">关注商户后，这里会显示他们发布的最新采购需求</div>
      <el-button
        type="primary"
        class="!rounded-xl !bg-slate-900 hover:!bg-slate-800 !border-slate-900 !text-white transition-all active:scale-95"
        @click="activeTab = 'all'"
      >
        去发现优质采购商
      </el-button>
    </div>

    <!-- 关注商户的采购需求列表 -->
    <div v-if="activeTab === 'followed' && followedRequirements.length > 0" class="card-grid">
      <div
        v-for="requirement in followedRequirements"
        :key="requirement.id"
        class="info-card followed-card"
      >
        <!-- 关注标记 -->
        <div class="followed-badge">
          <el-icon><Star /></el-icon> 已关注
        </div>
        <!-- 卡片头部 -->
        <div class="card-header">
          <div class="company-info">
            <div class="avatar">
              {{ (requirement.companyName || '未')[0] }}
            </div>
            <div class="company-detail">
              <div class="company-name">{{ requirement.companyName || '未知公司' }}</div>
              <div class="contact">
                <el-icon :size="12"><Phone /></el-icon>
                <span>{{ requirement.nickName || requirement.userName || '联系人' }}</span>
              </div>
            </div>
          </div>
          <el-tag type="info" size="small" effect="plain" class="!bg-blue-50 !text-blue-700 !border-blue-200">采购</el-tag>
        </div>
        
        <!-- 卡片主体 -->
        <div class="card-body">
          <h3 class="product-title">
            <el-icon class="text-blue-700"><ShoppingCart /></el-icon>
            <span>{{ requirement.categoryName }}</span>
          </h3>
          
          <div class="info-grid">
            <div class="info-item">
              <span class="label">需求量</span>
              <span class="value highlight">{{ requirement.quantity || '-' }} 吨</span>
            </div>
            <div class="info-item">
              <span class="label">期望价</span>
              <span class="value highlight">¥{{ requirement.expectedPrice?.toLocaleString() || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">付款方式</span>
              <span class="value">{{ requirement.paymentMethod || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">交货方式</span>
              <span class="value">{{ requirement.deliveryMethod || '-' }}</span>
            </div>
          </div>

          <div class="address-row">
            <el-icon class="text-gray-400"><Location /></el-icon>
            <span>{{ requirement.purchaseAddress || '交货地址未指定' }}</span>
          </div>
          
          <div v-if="parseParams(requirement.paramsJson).length > 0" class="params-tags">
            <el-tag
              v-for="param in parseParams(requirement.paramsJson).slice(0, 3)"
              :key="param.name"
              size="small"
              type="info"
              effect="plain"
            >
              {{ param.name }}: {{ param.value }}
            </el-tag>
          </div>
        </div>
        
        <!-- 卡片底部 -->
        <div class="card-footer">
          <div class="price-section">
            <div class="price">
              <span class="currency">¥</span>
              <span class="amount">{{ requirement.expectedPrice?.toLocaleString() || '-' }}</span>
              <span class="unit">/吨</span>
            </div>
            <div class="time">{{ formatTime(requirement.createTime) }}</div>
          </div>
          <div class="actions">
            <el-button size="small" class="!rounded-xl transition-all active:scale-95" :icon="View" @click="viewDetail(requirement)">详情</el-button>
            <el-button
              type="primary"
              size="small"
              class="!rounded-xl !bg-slate-900 hover:!bg-slate-800 !border-slate-900 !text-white transition-all active:scale-95"
              :icon="ChatDotRound"
              @click="startChat(requirement)"
            >
              报价
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 全部信息部分 -->
    <template v-if="activeTab === 'all'">
      <!-- 快捷排序按钮 -->
    <div class="sort-bar">
      <div class="flex items-center gap-2 text-sm text-gray-500 mr-4">
        <el-icon><Sort /></el-icon>
        <span>排序方式：</span>
      </div>
      <div class="flex flex-wrap gap-2">
        <el-button
          v-for="btn in sortButtons"
          :key="`${btn.field}-${btn.order}`"
          :type="activeSort === `${btn.field}-${btn.order}` ? 'primary' : 'default'"
          :class="activeSort === `${btn.field}-${btn.order}` ? '!bg-slate-900 hover:!bg-slate-800 !border-slate-900 !text-white' : '!border-gray-200 hover:!bg-gray-50 !text-gray-700'"
          size="small"
          round
          @click="setSort(btn.field, btn.order as 'asc' | 'desc')"
        >
          <el-icon class="mr-1"><component :is="iconMap[String(btn.icon)] ?? btn.icon" /></el-icon>
          {{ btn.label }}
        </el-button>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-bar">
      <div class="filter-row">
        <!-- 关键词搜索 -->
        <el-input
          v-model="filters.keyword"
          placeholder="搜索品类/公司/地址"
          :prefix-icon="Search"
          clearable
          class="filter-input"
          @keyup.enter="pagination.page = 1"
        />
        
        <!-- 品类筛选 -->
        <el-select
          v-model="filters.categoryName"
          placeholder="选择品类"
          clearable
          class="filter-select"
          @change="pagination.page = 1"
        >
          <el-option v-for="cat in allCategories" :key="cat" :label="cat" :value="cat" />
        </el-select>
        
        <!-- 付款方式 -->
        <el-select
          v-model="filters.paymentMethod"
          placeholder="付款方式"
          clearable
          class="filter-select"
          @change="pagination.page = 1"
        >
          <el-option v-for="m in paymentMethods" :key="m" :label="m" :value="m" />
        </el-select>

        <!-- 交货方式 -->
        <el-select
          v-model="filters.deliveryMethod"
          placeholder="交货方式"
          clearable
          class="filter-select-sm"
          @change="pagination.page = 1"
        >
          <el-option v-for="d in deliveryMethods" :key="d" :label="d" :value="d" />
        </el-select>
      </div>
      
      <div class="filter-row">
        <!-- 数量区间 -->
        <div class="quantity-range">
          <span class="text-sm text-gray-500 mr-2">需求量：</span>
          <el-input-number
            v-model="filters.minQuantity"
            placeholder="最小"
            :min="0"
            :controls="false"
            class="quantity-input"
          />
          <span class="text-gray-400 mx-2">—</span>
          <el-input-number
            v-model="filters.maxQuantity"
            placeholder="最大"
            :min="0"
            :controls="false"
            class="quantity-input"
          />
          <span class="text-sm text-gray-500 ml-1">吨</span>
        </div>
        
        <div class="flex items-center gap-2 ml-auto">
          <el-button class="!rounded-xl transition-all active:scale-95" @click="resetFilters">重置</el-button>
          <el-tag type="info" effect="plain">
            共 <span class="font-bold text-blue-700">{{ pagination.total }}</span> 条
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 卡片列表 -->
    <div v-loading="loading" class="card-grid">
      <div
        v-for="req in filteredRequirements"
        :key="req.id"
        class="info-card"
      >
        <!-- 卡片头部 -->
        <div class="card-header">
          <div class="company-info">
            <div class="avatar">
              {{ (req.companyName || '未')[0] }}
            </div>
            <div class="company-detail">
              <div class="company-name">{{ req.companyName || '未知公司' }}</div>
              <div class="contact">
                <el-icon :size="12"><Phone /></el-icon>
                <span>{{ req.nickName || req.userName || '联系人' }}</span>
              </div>
            </div>
          </div>
          <el-tag type="info" size="small" effect="plain" class="!bg-blue-50 !text-blue-700 !border-blue-200">采购</el-tag>
        </div>
        
        <!-- 卡片主体 -->
        <div class="card-body">
          <!-- 品类标题 -->
          <h3 class="product-title">
            <el-icon class="text-blue-700"><ShoppingCart /></el-icon>
            <span>{{ req.categoryName }}</span>
          </h3>
          
          <!-- 采购数量进度条 -->
          <div class="quantity-progress">
            <div class="progress-header">
              <span class="label">采购数量</span>
              <span class="value">{{ req.quantity || '-' }} 吨</span>
            </div>
            <div class="progress-bar">
              <div 
                class="progress-fill"
                :style="{ width: getRemainingPercent(req) + '%' }"
              ></div>
            </div>
            <div class="progress-hint">剩余 {{ req.remainingQuantity ?? req.quantity }} 吨</div>
          </div>
          
          <!-- 关键信息 -->
          <div class="info-grid">
            <div class="info-item">
              <span class="label">包装</span>
              <span class="value">{{ req.packaging || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">付款</span>
              <span class="value">{{ req.paymentMethod || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">发票</span>
              <span class="value">{{ req.invoiceType || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">交货</span>
              <span class="value">{{ req.deliveryMethod || '-' }}</span>
            </div>
          </div>

          <!-- 地址 -->
          <div class="address-row">
            <el-icon class="text-gray-400"><Location /></el-icon>
            <span>{{ req.purchaseAddress || '交货地址未指定' }}</span>
          </div>
          
          <!-- 参数标签 -->
          <div v-if="parseParams(req.paramsJson).length > 0" class="params-tags">
            <el-tag
              v-for="param in parseParams(req.paramsJson).slice(0, 3)"
              :key="param.name"
              size="small"
              type="info"
              effect="plain"
            >
              {{ param.name }}: {{ param.value }}
            </el-tag>
            <el-tag
              v-if="parseParams(req.paramsJson).length > 3"
              size="small"
              type="info"
              effect="plain"
            >
              +{{ parseParams(req.paramsJson).length - 3 }}
            </el-tag>
          </div>
        </div>
        
        <!-- 卡片底部 -->
        <div class="card-footer">
          <div class="price-section">
            <div class="price-label">期望价格</div>
            <div v-if="req.expectedPrice" class="price">
              <span class="currency">¥</span>
              <span class="amount">{{ req.expectedPrice?.toLocaleString() }}</span>
              <span class="unit">/吨</span>
            </div>
            <div v-else class="price-negotiable">面议</div>
            <div class="time">{{ formatTime(req.createTime) }}</div>
          </div>
          <div class="actions">
            <el-tooltip content="收藏">
              <el-button :icon="Star" circle size="small" @click="toggleFavorite(req)" />
            </el-tooltip>
            <el-button size="small" :icon="View" @click="viewDetail(req)">详情</el-button>
            <el-button type="primary" size="small" :icon="ChatDotRound" @click="startChat(req)">报价</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && filteredRequirements.length === 0" class="empty-state">
      <div class="empty-icon">
        <el-icon :size="48" class="text-gray-300"><ShoppingCart /></el-icon>
      </div>
      <div class="empty-text">暂无采购需求</div>
      <div class="empty-hint">请调整筛选条件或稍后再试</div>
    </div>

    <!-- 分页 -->
    <div v-if="pagination.total > pagination.size" class="pagination-bar">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next"
        background
      />
    </div>
    </template>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

/* Tab 切换栏 */
.tab-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  border: 1px solid #f3f4f6;
  border-radius: 16px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
  padding: 16px 20px;
  margin-bottom: 16px;
}

.tab-hint {
  padding-left: 16px;
}

/* 关注商户为空时 */
.empty-followed {
  background: white;
  border: 1px solid #f3f4f6;
  border-radius: 24px;
  padding: 60px 20px;
  text-align: center;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}

.empty-followed-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-followed-title {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.empty-followed-hint {
  color: #9ca3af;
  margin-bottom: 24px;
}

/* 关注卡片特殊样式 */
.followed-card {
  position: relative;
}

.followed-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #fffbeb;
  color: #b45309;
  border: 1px solid #fef3c7;
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 4px;
  z-index: 1;
}

.followed-card .card-header {
  padding-right: 100px;
}

/* 排序按钮栏 */
.sort-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  background: #f9fafb;
  border: 1px solid #f3f4f6;
  border-radius: 16px;
  padding: 12px 16px;
  margin-bottom: 16px;
}

/* 筛选区域 */
.filter-bar {
  background: white;
  border: 1px solid #f3f4f6;
  border-radius: 16px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
  padding: 16px 20px;
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.filter-row + .filter-row {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e5e7eb;
}

.filter-input {
  width: 240px;
}

.filter-select {
  width: 140px;
}

.filter-select-sm {
  width: 120px;
}

.quantity-range {
  display: flex;
  align-items: center;
}

.quantity-input {
  width: 100px;
}

/* 卡片网格 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
}

/* 信息卡片 */
.info-card {
  background: white;
  border-radius: 24px;
  border: 1px solid #f3f4f6;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
  overflow: hidden;
  transition: all 0.3s ease;
}

.info-card:hover {
  box-shadow: 0 6px 18px rgba(0,0,0,0.06);
}

.card-header {
  padding: 16px 20px;
  background: #f9fafb;
  border-bottom: 1px solid #f3f4f6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.company-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: #0f172a;
  color: white;
  font-weight: bold;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 18px rgba(15,23,42,0.12);
}

.company-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.company-name {
  font-weight: 600;
  color: #1f2937;
  font-size: 15px;
}

.contact {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #6b7280;
  font-size: 12px;
}

.card-body {
  padding: 20px;
}

.product-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

/* 数量进度条 */
.quantity-progress {
  margin-bottom: 16px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 6px;
}

.progress-header .label {
  color: #9ca3af;
}

.progress-header .value {
  color: #374151;
  font-weight: 600;
}

.progress-bar {
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #1d4ed8;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-hint {
  font-size: 11px;
  color: #9ca3af;
  margin-top: 4px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px 16px;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.info-item .label {
  color: #9ca3af;
}

.info-item .value {
  color: #374151;
  font-weight: 500;
}

.address-row {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.params-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.card-footer {
  padding: 16px 20px;
  background: #f9fafb;
  border-top: 1px solid #f3f4f6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-section {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.price-label {
  font-size: 12px;
  color: #9ca3af;
}

.price {
  display: flex;
  align-items: baseline;
}

.price .currency {
  font-size: 14px;
  color: #1d4ed8;
  font-weight: 500;
}

.price .amount {
  font-size: 24px;
  font-weight: bold;
  color: #1d4ed8;
  margin-left: 2px;
}

.price .unit {
  font-size: 12px;
  color: #9ca3af;
  margin-left: 2px;
}

.price-negotiable {
  font-size: 18px;
  color: #6b7280;
  font-weight: 500;
}

.time {
  font-size: 12px;
  color: #9ca3af;
}

.actions {
  display: flex;
  gap: 8px;
}

/* 空状态 */
.empty-state {
  background: white;
  border-radius: 24px;
  border: 1px solid #f3f4f6;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  background: #f3f4f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-text {
  color: #6b7280;
  font-size: 16px;
  margin-bottom: 8px;
}

.empty-hint {
  color: #9ca3af;
  font-size: 14px;
}

/* 分页 */
.pagination-bar {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-input {
    width: 100%;
  }
  
  .filter-select,
  .filter-select-sm {
    width: 100%;
  }
  
  .quantity-range {
    flex-wrap: wrap;
  }
}
</style>
