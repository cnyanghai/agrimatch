<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, ChatDotRound, View, Star, Phone, Location, Box, Sort, Refresh } from '@element-plus/icons-vue'
import { listSupplies, type SupplyResponse } from '../api/supply'
import { getProductTree, type ProductNode } from '../api/product'

const router = useRouter()
const loading = ref(false)

// Tab 切换：followed - 关注商户，all - 全部信息
const activeTab = ref('followed')

// 原始供应列表
const rawSupplies = ref<SupplyResponse[]>([])

// 关注商户的供应列表（模拟数据）
const followedSupplies = ref<SupplyResponse[]>([
  {
    id: 101,
    categoryName: '优质小麦',
    companyName: '山东粮食集团',
    companyId: 1,
    userId: 10001,
    nickName: '张经理',
    quantity: 200,
    exFactoryPrice: 2850,
    deliveredPrice: 2950,
    origin: '山东济南',
    packaging: '袋装',
    storageMethod: '常温',
    shipAddress: '山东省济南市历城区',
    paramsJson: JSON.stringify({ '水分': '12%', '杂质': '0.5%', '容重': '780g/L' }),
    createTime: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
    status: 1
  },
  {
    id: 102,
    categoryName: '东北玉米',
    companyName: '黑龙江优农合作社',
    companyId: 2,
    userId: 10002,
    nickName: '李总',
    quantity: 500,
    exFactoryPrice: 2520,
    deliveredPrice: 2680,
    origin: '黑龙江哈尔滨',
    packaging: '散装',
    storageMethod: '仓储',
    shipAddress: '黑龙江省哈尔滨市道里区',
    paramsJson: JSON.stringify({ '水分': '14%', '霉变': '1%', '杂质': '1%' }),
    createTime: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
    status: 1
  },
  {
    id: 103,
    categoryName: '河南大豆',
    companyName: '河南农业发展公司',
    companyId: 3,
    userId: 10003,
    nickName: '王主任',
    quantity: 150,
    exFactoryPrice: 4680,
    deliveredPrice: 4850,
    origin: '河南郑州',
    packaging: '袋装',
    storageMethod: '低温',
    shipAddress: '河南省郑州市金水区',
    paramsJson: JSON.stringify({ '蛋白质': '40%', '水分': '13%', '杂质': '0.3%' }),
    createTime: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
    status: 1
  }
])

// 品类树
const categoryTree = ref<ProductNode[]>([])

// 筛选条件
const filters = reactive({
  keyword: '',
  categoryName: '',
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
  origin: '',
  packaging: ''
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
  { field: 'exFactoryPrice', order: 'asc', label: '出厂价最低', icon: '💰' },
  { field: 'deliveredPrice', order: 'asc', label: '到厂价最低', icon: '🚚' },
  { field: 'createTime', order: 'desc', label: '最新发布', icon: '🕐' },
  { field: 'quantity', order: 'desc', label: '数量最多', icon: '📦' },
  { field: 'exFactoryPrice', order: 'desc', label: '出厂价最高', icon: '📈' },
]

// 当前激活的排序按钮
const activeSort = computed(() => `${sortConfig.field}-${sortConfig.order}`)

// 设置排序
function setSort(field: string, order: 'asc' | 'desc') {
  sortConfig.field = field
  sortConfig.order = order
  pagination.page = 1
}

// 排序后的列表
const sortedSupplies = computed(() => {
  const list = [...rawSupplies.value]
  
  return list.sort((a, b) => {
    let valA: any = a[sortConfig.field as keyof SupplyResponse]
    let valB: any = b[sortConfig.field as keyof SupplyResponse]
    
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
const filteredSupplies = computed(() => {
  let result = sortedSupplies.value
  
  // 关键词搜索
  if (filters.keyword) {
    const kw = filters.keyword.toLowerCase()
    result = result.filter(s => 
      s.categoryName?.toLowerCase().includes(kw) ||
      s.companyName?.toLowerCase().includes(kw) ||
      s.origin?.toLowerCase().includes(kw) ||
      s.shipAddress?.toLowerCase().includes(kw)
    )
  }
  
  // 品类筛选
  if (filters.categoryName) {
    result = result.filter(s => s.categoryName === filters.categoryName)
  }
  
  // 价格筛选
  if (filters.minPrice !== undefined) {
    result = result.filter(s => (s.exFactoryPrice || 0) >= filters.minPrice!)
  }
  if (filters.maxPrice !== undefined) {
    result = result.filter(s => (s.exFactoryPrice || 0) <= filters.maxPrice!)
  }
  
  // 产地筛选
  if (filters.origin) {
    result = result.filter(s => s.origin?.includes(filters.origin))
  }
  
  // 包装筛选
  if (filters.packaging) {
    result = result.filter(s => s.packaging?.includes(filters.packaging))
  }
  
  pagination.total = result.length
  
  // 分页
  const start = (pagination.page - 1) * pagination.size
  return result.slice(start, start + pagination.size)
})

// 加载供应列表
async function loadSupplies() {
  loading.value = true
  try {
    const r = await listSupplies({
      status: 1,
      includeExpired: false
    })
    if (r.code === 0) {
      rawSupplies.value = r.data || []
      pagination.total = rawSupplies.value.length
    }
  } catch (e) {
    ElMessage.error('加载供应信息失败')
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
  rawSupplies.value.forEach(s => {
    if (s.categoryName) categories.add(s.categoryName)
  })
  return Array.from(categories)
})

// 获取所有产地
const allOrigins = computed(() => {
  const origins = new Set<string>()
  rawSupplies.value.forEach(s => {
    if (s.origin) origins.add(s.origin)
  })
  return Array.from(origins)
})

// 获取所有包装类型
const allPackagings = computed(() => {
  const packagings = new Set<string>()
  rawSupplies.value.forEach(s => {
    if (s.packaging) packagings.add(s.packaging)
  })
  return Array.from(packagings)
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
function viewDetail(supply: SupplyResponse) {
  ElMessage.info(`查看供应详情：${supply.categoryName}`)
}

// 发起聊天
function startChat(supply: SupplyResponse) {
  router.push({
    path: '/chat',
    query: {
      type: 'supply',
      id: supply.id,
      companyName: supply.companyName ?? ''
    }
  })
}

// 收藏
function toggleFavorite(_supply: SupplyResponse) {
  ElMessage.success('收藏功能开发中...')
}

// 重置筛选
function resetFilters() {
  filters.keyword = ''
  filters.categoryName = ''
  filters.minPrice = undefined
  filters.maxPrice = undefined
  filters.origin = ''
  filters.packaging = ''
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

onMounted(() => {
  loadCategoryTree()
  loadSupplies()
})
</script>

<template>
  <div class="max-w-7xl mx-auto space-y-6">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">供应浏览</h1>
        <p class="text-gray-500 mt-1">浏览供应商发布的原料供应信息，找到合适的供应商</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadSupplies">刷新数据</el-button>
    </div>

    <!-- Tab 切换 -->
    <div class="tab-bar">
      <el-segmented v-model="activeTab" :options="[
        { label: '⭐ 关注商户', value: 'followed' },
        { label: '📋 全部信息', value: 'all' }
      ]" size="large" />
      <div class="tab-hint">
        <span v-if="activeTab === 'followed'" class="text-gray-500 text-sm">
          共 <span class="font-bold text-emerald-600">{{ followedSupplies.length }}</span> 条关注商户发布的信息
        </span>
        <span v-else class="text-gray-500 text-sm">
          共 <span class="font-bold text-emerald-600">{{ pagination.total }}</span> 条供应信息
        </span>
      </div>
    </div>

    <!-- 关注商户为空时的提示 -->
    <div v-if="activeTab === 'followed' && followedSupplies.length === 0" class="empty-followed">
      <el-icon class="empty-followed-icon"><Star /></el-icon>
      <div class="empty-followed-title">暂无关注的商户</div>
      <div class="empty-followed-hint">关注商户后，这里会显示他们发布的最新供应信息</div>
      <el-button
        type="primary"
        class="!rounded-xl !bg-emerald-600 hover:!bg-emerald-700 !border-emerald-600 transition-all active:scale-95"
        @click="activeTab = 'all'"
      >
        去发现优质供应商
      </el-button>
    </div>

    <!-- 关注商户的供应列表 -->
    <div v-if="activeTab === 'followed' && followedSupplies.length > 0" class="card-grid">
      <div
        v-for="supply in followedSupplies"
        :key="supply.id"
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
              {{ (supply.companyName || '未')[0] }}
            </div>
            <div class="company-detail">
              <div class="company-name">{{ supply.companyName || '未知公司' }}</div>
              <div class="contact">
                <el-icon :size="12"><Phone /></el-icon>
                <span>{{ supply.nickName || supply.userName || '联系人' }}</span>
              </div>
            </div>
          </div>
          <el-tag type="success" size="small" effect="light">供应</el-tag>
        </div>
        
        <!-- 卡片主体 -->
        <div class="card-body">
          <h3 class="product-title">
            <el-icon class="text-emerald-600"><Box /></el-icon>
            <span>{{ supply.categoryName }}</span>
          </h3>
          
          <div class="info-grid">
            <div class="info-item">
              <span class="label">产地</span>
              <span class="value">{{ supply.origin || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">数量</span>
              <span class="value">{{ supply.quantity || '-' }} 吨</span>
            </div>
            <div class="info-item">
              <span class="label">包装</span>
              <span class="value">{{ supply.packaging || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">存储</span>
              <span class="value">{{ supply.storageMethod || '-' }}</span>
            </div>
          </div>

          <div class="address-row">
            <el-icon class="text-gray-400"><Location /></el-icon>
            <span>{{ supply.shipAddress || '发货地址未指定' }}</span>
          </div>
          
          <div v-if="parseParams(supply.paramsJson).length > 0" class="params-tags">
            <el-tag
              v-for="param in parseParams(supply.paramsJson).slice(0, 3)"
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
              <span class="amount">{{ supply.exFactoryPrice?.toLocaleString() || '-' }}</span>
              <span class="unit">/吨</span>
            </div>
            <div class="time">{{ formatTime(supply.createTime) }}</div>
          </div>
          <div class="actions">
            <el-button size="small" class="!rounded-xl transition-all active:scale-95" :icon="View" @click="viewDetail(supply)">详情</el-button>
            <el-button
              type="primary"
              size="small"
              class="!rounded-xl !bg-emerald-600 hover:!bg-emerald-700 !border-emerald-600 transition-all active:scale-95"
              :icon="ChatDotRound"
              @click="startChat(supply)"
            >
              联系
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
          <span class="mr-1">{{ btn.icon }}</span>
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
          placeholder="搜索品类/公司/产地/地址"
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
        
        <!-- 产地筛选 -->
        <el-select
          v-model="filters.origin"
          placeholder="选择产地"
          clearable
          filterable
          class="filter-select"
          @change="pagination.page = 1"
        >
          <el-option v-for="o in allOrigins" :key="o" :label="o" :value="o" />
        </el-select>

        <!-- 包装筛选 -->
        <el-select
          v-model="filters.packaging"
          placeholder="包装类型"
          clearable
          class="filter-select-sm"
          @change="pagination.page = 1"
        >
          <el-option v-for="p in allPackagings" :key="p" :label="p" :value="p" />
        </el-select>
      </div>
      
      <div class="filter-row">
        <!-- 价格区间 -->
        <div class="price-range">
          <span class="text-sm text-gray-500 mr-2">出厂价：</span>
          <el-input-number
            v-model="filters.minPrice"
            placeholder="最低"
            :min="0"
            :controls="false"
            class="price-input"
          />
          <span class="text-gray-400 mx-2">—</span>
          <el-input-number
            v-model="filters.maxPrice"
            placeholder="最高"
            :min="0"
            :controls="false"
            class="price-input"
          />
          <span class="text-sm text-gray-500 ml-1">元/吨</span>
        </div>
        
        <div class="flex items-center gap-2 ml-auto">
          <el-button class="!rounded-xl transition-all active:scale-95" @click="resetFilters">重置</el-button>
          <el-tag type="info" effect="plain">
            共 <span class="font-bold text-emerald-600">{{ pagination.total }}</span> 条
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 卡片列表 -->
    <div v-loading="loading" class="card-grid">
      <div
        v-for="supply in filteredSupplies"
        :key="supply.id"
        class="info-card"
      >
        <!-- 卡片头部 -->
        <div class="card-header">
          <div class="company-info">
            <div class="avatar">
              {{ (supply.companyName || '未')[0] }}
            </div>
            <div class="company-detail">
              <div class="company-name">{{ supply.companyName || '未知公司' }}</div>
              <div class="contact">
                <el-icon :size="12"><Phone /></el-icon>
                <span>{{ supply.nickName || supply.userName || '联系人' }}</span>
              </div>
            </div>
          </div>
          <el-tag type="success" size="small" effect="light">供应</el-tag>
        </div>
        
        <!-- 卡片主体 -->
        <div class="card-body">
          <!-- 品类标题 -->
          <h3 class="product-title">
            <el-icon class="text-emerald-600"><Box /></el-icon>
            <span>{{ supply.categoryName }}</span>
          </h3>
          
          <!-- 关键信息 -->
          <div class="info-grid">
            <div class="info-item">
              <span class="label">产地</span>
              <span class="value">{{ supply.origin || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">数量</span>
              <span class="value">{{ supply.quantity || '-' }} 吨</span>
            </div>
            <div class="info-item">
              <span class="label">包装</span>
              <span class="value">{{ supply.packaging || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">存储</span>
              <span class="value">{{ supply.storageMethod || '-' }}</span>
            </div>
          </div>

          <!-- 地址 -->
          <div class="address-row">
            <el-icon class="text-gray-400"><Location /></el-icon>
            <span>{{ supply.shipAddress || '发货地址未指定' }}</span>
          </div>
          
          <!-- 参数标签 -->
          <div v-if="parseParams(supply.paramsJson).length > 0" class="params-tags">
            <el-tag
              v-for="param in parseParams(supply.paramsJson).slice(0, 3)"
              :key="param.name"
              size="small"
              type="info"
              effect="plain"
            >
              {{ param.name }}: {{ param.value }}
            </el-tag>
            <el-tag
              v-if="parseParams(supply.paramsJson).length > 3"
              size="small"
              type="info"
              effect="plain"
            >
              +{{ parseParams(supply.paramsJson).length - 3 }}
            </el-tag>
          </div>
        </div>
        
        <!-- 卡片底部 -->
        <div class="card-footer">
          <div class="price-section">
            <div class="price">
              <span class="currency">¥</span>
              <span class="amount">{{ supply.exFactoryPrice?.toLocaleString() || '-' }}</span>
              <span class="unit">/吨</span>
            </div>
            <div class="time">{{ formatTime(supply.createTime) }}</div>
          </div>
          <div class="actions">
            <el-tooltip content="收藏">
              <el-button :icon="Star" circle size="small" @click="toggleFavorite(supply)" />
            </el-tooltip>
            <el-button size="small" :icon="View" @click="viewDetail(supply)">详情</el-button>
            <el-button type="primary" size="small" :icon="ChatDotRound" @click="startChat(supply)">联系</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && filteredSupplies.length === 0" class="empty-state">
      <div class="empty-icon">
        <el-icon :size="48" class="text-gray-300"><Box /></el-icon>
      </div>
      <div class="empty-text">暂无供应信息</div>
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

.price-range {
  display: flex;
  align-items: center;
}

.price-input {
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
  background: #059669;
  color: white;
  font-weight: bold;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 18px rgba(5,150,105,0.12);
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

.price {
  display: flex;
  align-items: baseline;
}

.price .currency {
  font-size: 14px;
  color: #f59e0b;
  font-weight: 500;
}

.price .amount {
  font-size: 24px;
  font-weight: bold;
  color: #f59e0b;
  margin-left: 2px;
}

.price .unit {
  font-size: 12px;
  color: #9ca3af;
  margin-left: 2px;
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
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
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
  
  .price-range {
    flex-wrap: wrap;
  }
}
</style>
