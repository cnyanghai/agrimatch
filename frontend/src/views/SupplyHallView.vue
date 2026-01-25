<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { requireAuth } from '../utils/requireAuth'
import PublicFooter from '../components/PublicFooter.vue'
import ChatDrawer from '../components/chat/ChatDrawer.vue'
import { listSupplies, type SupplyResponse } from '../api/supply'
import { openChatConversation } from '../api/chat'
import { followUser, unfollowUser, checkFollowStatus } from '../api/follow'
import { batchGetFuturesPrices, type FuturesContractResponse } from '../api/futures'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'
import { Card, StatusBadge } from '../components/ui'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

function go(path: string) {
  router.push(path)
}

function onPublishSupply() {
  if (!requireAuth('/supply')) return
  go('/supply')
}

const supplies = ref<SupplyResponse[]>([])
const listLoading = ref(false)
const focusedId = ref<number | null>(null)
const cardEls = new Map<number, HTMLElement>()
let focusTimer: number | null = null

// 筛选条件
const searchKeyword = ref('')
const selectedCategory = ref<string | null>(null)

// 从 URL 读取公司筛选参数
const companyIdFilter = computed(() => {
  const raw = route.query.companyId
  const s = Array.isArray(raw) ? raw[0] : raw
  const n = s ? Number(s) : NaN
  return Number.isFinite(n) ? n : null
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 品种列表
const categoryOptions = ['玉米', '豆粕', '棉粕', '菜粕', 'DDGS', '赖氨酸', '蛋氨酸', '磷酸氢钙']

// 板块列表
const domains = [
  { key: 'biological', name: '生物种苗' },
  { key: 'processing', name: '农业加工' },
  { key: 'material', name: '原料辅料' },
  { key: 'equipment', name: '装备物流' }
]
const selectedDomain = ref<string | null>(null)

// 关注状态 Map: userId -> isFollowing
const followingMap = ref<Map<number, boolean>>(new Map())

// 检查并加载关注状态
async function loadFollowStatus(userIds: number[]) {
  if (!authStore.token) return
  for (const userId of userIds) {
    if (followingMap.value.has(userId)) continue
    try {
      const r = await checkFollowStatus(userId)
      if (r.code === 0) {
        followingMap.value.set(userId, r.data || false)
      }
    } catch {
      // 忽略单个检查失败
    }
  }
}

// 关注/取消关注
async function toggleFollow(s: SupplyResponse) {
  if (!requireAuth('/hall/supply')) return
  if (!s.userId) {
    ElMessage.warning('无法关注该用户')
    return
  }

  const isFollowing = followingMap.value.get(s.userId) || false
  try {
    if (isFollowing) {
      await unfollowUser(s.userId)
      followingMap.value.set(s.userId, false)
      ElMessage.success(`已取消关注 ${s.nickName || s.companyName || '该用户'}`)
    } else {
      await followUser(s.userId)
      followingMap.value.set(s.userId, true)
      ElMessage.success(`已关注 ${s.nickName || s.companyName || '该用户'}`)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

// 获取用户关注状态
function isFollowingUser(userId?: number): boolean {
  if (!userId) return false
  return followingMap.value.get(userId) || false
}

const focusIdFromRoute = computed(() => {
  const raw = route.query.focusId
  const s = Array.isArray(raw) ? raw[0] : raw
  const n = s ? Number(s) : NaN
  return Number.isFinite(n) ? n : null
})

const displaySupplies = computed(() => {
  // 若存在 focusId，则展示完整列表以保证可定位
  if (focusIdFromRoute.value) return supplies.value
  // 前端分页
  const start = (currentPage.value - 1) * pageSize.value
  return supplies.value.slice(start, start + pageSize.value)
})

function setCardEl(id: number, el: Element | null) {
  if (!id) return
  if (el) cardEls.set(id, el as HTMLElement)
  else cardEls.delete(id)
}

async function applyFocusIfNeeded() {
  const id = focusIdFromRoute.value
  if (!id) return

  // 等待 DOM 渲染
  await nextTick()
  const el = cardEls.get(id)
  if (!el) return

  focusedId.value = id
  el.scrollIntoView({ behavior: 'smooth', block: 'center' })

  // 清掉 query，避免刷新/切换重复触发
  router.replace({ path: route.path, query: { ...route.query, focusId: undefined } })

  if (focusTimer) window.clearTimeout(focusTimer)
  focusTimer = window.setTimeout(() => {
    focusedId.value = null
    focusTimer = null
  }, 2500)
}

const drawerOpen = ref(false)
const drawerConversationId = ref<number | null>(null)
const drawerPeerName = ref('')
const drawerSubjectSnapshotJson = ref<string | null>(null)
const drawerSubjectId = ref<number | null>(null)

function buildSupplySnapshot(s: SupplyResponse) {
  return JSON.stringify({
    snapshotTime: new Date().toLocaleString('zh-CN'),
    title: s.categoryName,
    categoryName: s.categoryName,
    companyName: s.companyName,
    nickName: s.nickName,
    priceType: s.priceType,
    exFactoryPrice: s.exFactoryPrice,
    basisQuotes: s.basisQuotes,
    quantity: s.quantity,
    remainingQuantity: s.remainingQuantity,
    origin: s.origin,
    shipAddress: s.shipAddress,
    deliveryMode: s.deliveryMode,
    packaging: s.packaging,
    storageMethod: s.storageMethod,
    paramsJson: s.paramsJson,
    remark: s.remark
  })
}

async function onConsult(s: SupplyResponse) {
  if (!requireAuth('/hall/supply')) return
  if (!s.userId || !s.id) {
    ElMessage.warning('该条信息暂不支持咨询')
    return
  }
  try {
    const res = await openChatConversation({
      peerUserId: s.userId,
      subjectType: 'SUPPLY',
      subjectId: s.id,
      subjectSnapshotJson: buildSupplySnapshot(s)
    })
    if (res.code !== 0 || !res.data) throw new Error(res.message)

    drawerConversationId.value = res.data
    drawerPeerName.value = s.nickName || s.userName || s.companyName || '对方'
    drawerSubjectId.value = s.id
    drawerSubjectSnapshotJson.value = buildSupplySnapshot(s)
    drawerOpen.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '发起咨询失败')
  }
}

function onDrawerClosed() {
  drawerConversationId.value = null
  drawerSubjectSnapshotJson.value = null
  drawerSubjectId.value = null
  drawerPeerName.value = ''
}

// 期货价格缓存
const futuresPriceCache = ref<Record<string, FuturesContractResponse>>({})

async function loadSupplies() {
  listLoading.value = true
  try {
    const params: any = { 
      activeOnly: true, 
      includeExpired: false, 
      orderBy: 'create_time', 
      order: 'desc' 
    }
    
    // 应用筛选条件
    if (selectedCategory.value) {
      params.categoryName = selectedCategory.value
    }

    if (selectedDomain.value) {
      params.domain = selectedDomain.value
    }
    
    // 应用公司筛选（从地图跳转）
    if (companyIdFilter.value) {
      params.companyId = companyIdFilter.value
    }
    
    const res = await listSupplies(params)
    if (res.code !== 0) throw new Error(res.message)
    
    let result = res.data || []
    
    // 前端搜索过滤（关键词搜索）
    if (searchKeyword.value.trim()) {
      const kw = searchKeyword.value.toLowerCase()
      result = result.filter(s => 
        s.categoryName?.toLowerCase().includes(kw) ||
        s.companyName?.toLowerCase().includes(kw) ||
        s.shipAddress?.toLowerCase().includes(kw) ||
        s.nickName?.toLowerCase().includes(kw)
      )
    }
    
    supplies.value = result
    total.value = result.length
    
    // 加载关注状态
    const userIds = supplies.value.map(s => s.userId).filter(Boolean) as number[]
    const uniqueUserIds = [...new Set(userIds)]
    loadFollowStatus(uniqueUserIds)
    
    // 加载基差报价的期货价格
    await loadFuturesPrices(result)
  } catch {
    supplies.value = []
    total.value = 0
  } finally {
    listLoading.value = false
    applyFocusIfNeeded()
  }
}

// 加载基差报价需要的期货价格
async function loadFuturesPrices(supplyList: SupplyResponse[]) {
  // 收集所有基差报价中的合约代码
  const contractCodes = new Set<string>()
  for (const s of supplyList) {
    if (s.priceType === 1 && s.basisQuotes) {
      for (const bq of s.basisQuotes) {
        if (bq.contractCode) contractCodes.add(bq.contractCode)
      }
    }
  }
  
  if (contractCodes.size === 0) return
  
  try {
    const res = await batchGetFuturesPrices([...contractCodes])
    if (res.code === 0 && res.data) {
      futuresPriceCache.value = { ...futuresPriceCache.value, ...res.data }
    }
  } catch {
    // 静默失败
  }
}

// 获取合约的期货价格
function getFuturesPrice(contractCode: string): number | null {
  return futuresPriceCache.value[contractCode]?.lastPrice ?? null
}

// 计算核算价格（期货价 + 基差）
function calcReferencePrice(contractCode: string, basisPrice: number): number | null {
  const futuresPrice = getFuturesPrice(contractCode)
  if (futuresPrice === null) return null
  return futuresPrice + basisPrice
}

// 选择品种筛选
function selectCategory(cat: string | null) {
  selectedCategory.value = selectedCategory.value === cat ? null : cat
  currentPage.value = 1 // 重置分页
  loadSupplies()
}

// 选择板块筛选
function selectDomain(domainKey: string | null) {
  selectedDomain.value = selectedDomain.value === domainKey ? null : domainKey
  currentPage.value = 1
  loadSupplies()
}

// 搜索
function onSearch() {
  currentPage.value = 1 // 重置分页
  loadSupplies()
}

// 分页变更
function handlePageChange(page: number) {
  currentPage.value = page
  // 滚动到列表顶部
  window.scrollTo({ top: 400, behavior: 'smooth' })
}

onMounted(() => {
  loadSupplies()
})

watch(focusIdFromRoute, () => {
  applyFocusIfNeeded()
})

// 监听 companyId 筛选变化
watch(companyIdFilter, () => {
  currentPage.value = 1
  loadSupplies()
})

// 解析品类参数 JSON，提取关键参数显示（极致精简版，支持标准化格式）
function parseParams(paramsJson?: string): string {
  if (!paramsJson) return '暂无参数'
  try {
    const data = JSON.parse(paramsJson)
    // 兼容逻辑：
    // 1. 标准格式: {"水分": "14%", "容重": "720"}
    // 2. 旧格式: {"params": {"1": {"name": "水分", "value": "14%"}}, "custom": {}}
    let flatParams: Record<string, any> = {}

    if (data.params || data.custom) {
      if (data.params) {
        Object.entries(data.params).forEach(([k, v]) => {
          const name = (typeof v === 'object' && v !== null && (v as any).name) ? (v as any).name : k
          const val = (typeof v === 'object' && v !== null && 'value' in (v as any)) ? (v as any).value : v
          flatParams[name] = val
        })
      }
      if (data.custom) Object.assign(flatParams, data.custom)
    } else if (typeof data === 'object' && data !== null) {
      flatParams = data
    }

    const entries = Object.entries(flatParams)
    if (entries.length === 0) return '暂无参数'
    
    return entries
      .slice(0, 3)
      .map(([k, v]) => {
        // 如果键是纯数字（旧格式 ID），则只显示值
        if (/^\d+$/.test(k)) return String(v)
        return `${k}:${v}`
      })
      .join(' / ')
  } catch {
    return '暂无参数'
  }
}
</script>

<template>
  <div class="bg-gray-50 text-gray-900 min-h-screen">
    <!-- 公司筛选提示 -->
    <div v-if="companyIdFilter" class="bg-brand-50 border-b border-brand-100">
      <div class="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
        <div class="flex items-center gap-2 text-brand-700">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" /></svg>
          <span class="text-sm font-medium">
            正在查看该公司的供应信息
          </span>
        </div>
        <button 
          class="px-3 py-1 bg-brand-100 hover:bg-brand-200 text-brand-700 rounded-lg text-sm font-medium transition-all"
          @click="router.push('/hall/supply')"
        >
          查看全部供应
        </button>
      </div>
    </div>

    <!-- 筛选区 -->
    <section class="bg-white border-b shadow-sm">
      <div class="max-w-7xl mx-auto px-4 py-6">
        <div class="flex flex-col md:flex-row gap-4 mb-6">
          <div class="flex-1 relative">
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索品种、产地、指标或公司..."
              class="w-full border-2 border-gray-200 rounded-lg py-2.5 px-10 focus:border-brand-500 outline-none transition-all"
              @keyup.enter="onSearch"
            />
            <svg class="w-5 h-5 absolute left-3 top-3 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
          </div>
          <button 
            class="px-8 py-2.5 bg-brand-600 text-white rounded-xl font-bold hover:bg-brand-700 transition-all "
            @click="onSearch"
          >
            查询信息
          </button>
        </div>

        <div class="space-y-4">
          <div class="flex items-start gap-4 text-xs">
            <span class="text-gray-400 shrink-0 mt-1.5 font-medium">所属板块:</span>
            <div class="flex flex-wrap gap-2">
              <button 
                :class="['px-3 py-1 border rounded-full transition-all', selectedDomain === null ? 'bg-brand-600 text-white border-brand-600' : 'border-gray-200 hover:border-brand-500 hover:text-brand-600']"
                @click="selectDomain(null)"
              >
                全部
              </button>
              <button 
                v-for="d in domains" 
                :key="d.key"
                :class="['px-3 py-1 border rounded-full transition-all', selectedDomain === d.key ? 'bg-brand-600 text-white border-brand-600' : 'border-gray-200 hover:border-brand-500 hover:text-brand-600']"
                @click="selectDomain(d.key)"
              >
                {{ d.name }}
              </button>
            </div>
          </div>

          <div class="flex items-start gap-4 text-xs">
            <span class="text-gray-400 shrink-0 mt-1.5 font-medium">常见品种:</span>
            <div class="flex flex-wrap gap-2">
              <button 
                :class="['px-3 py-1 border rounded-full transition-all', selectedCategory === null ? 'bg-brand-600 text-white border-brand-600' : 'border-gray-200 hover:border-brand-500 hover:text-brand-600']"
                @click="selectCategory(null)"
              >
                全部
              </button>
              <button 
                v-for="cat in categoryOptions" 
                :key="cat"
                :class="['px-3 py-1 border rounded-full transition-all', selectedCategory === cat ? 'bg-brand-600 text-white border-brand-600' : 'border-gray-200 hover:border-brand-500 hover:text-brand-600']"
                @click="selectCategory(cat)"
              >
                {{ cat }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 列表区（先按设计稿静态，后续二期接接口替换） -->
    <main class="max-w-7xl mx-auto px-4 py-8">
      <div class="space-y-4">
        <div v-if="listLoading" class="bg-white rounded-xl border border-gray-200 p-8 text-gray-400 text-sm">
          正在加载货源...
        </div>

          <!-- 供应卡片 -->
          <Card 
          v-for="s in displaySupplies"
          :key="s.id"
            :ref="el => setCardEl(Number(s.id), el as any)"
            radius="2xl"
            padding="none"
            hover
            class="flex flex-col lg:flex-row items-stretch lg:items-center gap-6 p-6 group transition-all"
            :class="{ 'ring-2 ring-brand-500 shadow-lg': focusedId === s.id }"
            @click="onViewDetail(s)"
        >
            <!-- 左侧公司/用户信息 -->
            <div class="flex items-start gap-3 w-full lg:w-[200px] shrink-0 border-b lg:border-b-0 lg:border-r border-gray-100 pb-4 lg:pb-0 lg:pr-4">
              <div class="w-10 h-10 rounded-xl bg-brand-50 flex items-center justify-center text-brand-600 shrink-0">
                <span class="text-sm font-bold">{{ (s.companyName || s.nickName || '户')[0] }}</span>
              </div>
              <div class="min-w-0 flex-1">
                <div class="font-bold text-gray-900 truncate hover:text-brand-600 transition-colors" @click.stop="go(`/companies/${s.companyId}`)">
                  {{ s.companyName || '个人用户' }}
                </div>
                <div class="flex items-center gap-1 mt-1">
                  <StatusBadge type="brand">供应</StatusBadge>
                  <span class="text-[10px] text-gray-400 truncate">{{ s.nickName || s.userName || '' }}</span>
                </div>
              </div>
              <!-- 关注按钮 -->
              <button
                v-if="authStore.token && s.userId"
                class="shrink-0 text-[10px] font-bold px-2.5 py-1 rounded-full border transition-all active:scale-95"
                :class="isFollowingUser(s.userId)
                  ? 'bg-brand-50 text-brand-600 border-brand-200'
                  : 'bg-white text-gray-500 border-gray-200 hover:border-brand-300 hover:text-brand-600'"
                @click.stop="toggleFollow(s)"
              >
                {{ isFollowingUser(s.userId) ? '已关注' : '+ 关注' }}
              </button>
            </div>

            <!-- 产品核心信息 -->
            <div class="w-full lg:w-auto shrink-0" :class="s.priceType === 1 ? 'lg:min-w-[200px]' : 'lg:w-[120px]'">
              <div class="flex items-center gap-2 mb-1">
                <StatusBadge v-if="s.priceType === 1" type="warning">基差</StatusBadge>
                <StatusBadge v-else type="success">现货</StatusBadge>
                <span class="text-gray-400 text-[10px]">ID: {{ s.id }}</span>
              </div>
              <h3 class="text-lg font-black text-gray-900 truncate">{{ s.categoryName }}</h3>
              
              <!-- 现货一口价 -->
              <div v-if="s.priceType !== 1" class="mt-1 text-xl font-black text-red-600 italic">
                <span class="whitespace-nowrap inline-flex items-baseline gap-1">
                  <span>¥{{ s.exFactoryPrice }}</span>
                  <span class="text-xs font-normal text-gray-400 not-italic">元/吨</span>
                </span>
              </div>
              
              <!-- 基差报价 -->
              <div v-else class="mt-2 space-y-1.5">
                <div v-for="bq in (s.basisQuotes || []).slice(0, 3)" :key="bq.id" class="bg-amber-50/50 rounded-lg px-2 py-1.5 border border-amber-100">
                  <div class="flex items-center justify-between gap-2">
                    <span class="text-gray-700 font-bold text-[10px]">{{ bq.contractName || bq.contractCode }}</span>
                    <span class="text-[10px] text-gray-400">{{ bq.availableQty }}吨</span>
                  </div>
                  <div class="flex items-center justify-between gap-2 mt-0.5">
                    <div class="flex flex-col">
                      <span class="text-[9px] text-gray-500">
                        期货 ¥{{ getFuturesPrice(bq.contractCode) || '-' }}
                      </span>
                      <span class="font-bold text-[10px]" :class="bq.basisPrice >= 0 ? 'text-red-500' : 'text-green-500'">
                        {{ bq.basisPrice >= 0 ? '+' : '' }}{{ bq.basisPrice }}
                      </span>
                    </div>
                    <div class="text-right">
                      <div class="text-[8px] text-gray-400 font-bold uppercase">核算价</div>
                      <span class="font-black text-brand-600 text-sm">
                        ¥{{ calcReferencePrice(bq.contractCode, bq.basisPrice)?.toFixed(0) || '-' }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 参数详情 -->
            <div class="flex-1 grid grid-cols-2 md:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_minmax(0,1fr)_190px] gap-4 text-xs py-3 bg-gray-50/50 rounded-2xl px-5">
              <div class="min-w-0">
                <div class="text-gray-400 text-[10px] font-bold uppercase mb-1">规格参数</div>
                <div class="font-bold text-gray-700 truncate">{{ parseParams(s.paramsJson) }}</div>
              </div>
              <div class="min-w-0">
                <div class="text-gray-400 text-[10px] font-bold uppercase mb-1">发货地</div>
                <div class="font-bold text-gray-700 truncate">{{ s.shipAddress || '-' }}</div>
              </div>
              <div class="min-w-0">
                <div class="text-gray-400 text-[10px] font-bold uppercase mb-1">库存</div>
                <div class="font-bold text-gray-700 truncate">{{ s.quantity ?? '-' }}吨</div>
              </div>
              <div class="min-w-0">
                <div class="text-gray-400 text-[10px] font-bold uppercase mb-1">物流方式</div>
                <div class="font-bold text-gray-700 truncate">{{ s.deliveryMode || '-' }}</div>
              </div>
            </div>

            <!-- 操作 -->
            <div class="shrink-0 flex flex-col items-center justify-center gap-2">
              <button 
                class="w-full lg:w-auto px-10 py-3 bg-brand-600 text-white rounded-xl font-bold text-sm shadow-lg shadow-brand-900/10 hover:bg-brand-700 hover:-translate-y-0.5 transition-all active:scale-95" 
                @click.stop="onConsult(s)"
              >
                立即咨询
              </button>
            </div>
          </Card>

        <div v-if="!listLoading && supplies.length === 0" class="bg-white rounded-xl border border-gray-200 p-8 text-center">
          <div class="text-gray-400 text-sm mb-2">暂无货源数据</div>
          <div class="text-xs text-gray-300">
            {{ selectedCategory ? `没有找到「${selectedCategory}」相关的供应信息` : '请尝试调整筛选条件' }}
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="flex justify-center mt-10">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="handlePageChange"
        />
      </div>
      
      <!-- 数据统计 -->
      <div v-if="total > 0" class="text-center mt-4 text-xs text-gray-400">
        共 {{ total }} 条供应信息
        <span v-if="selectedCategory" class="ml-2">· 当前筛选：{{ selectedCategory }}</span>
      </div>
    </main>

    <PublicFooter />

    <ChatDrawer
      v-model="drawerOpen"
      :conversation-id="drawerConversationId"
      :peer-display-name="drawerPeerName"
      subject-type="SUPPLY"
      :subject-id="drawerSubjectId"
      :subject-snapshot-json="drawerSubjectSnapshotJson"
      @closed="onDrawerClosed"
    />
  </div>
</template>

<style scoped>
.filter-tag:hover {
  border-color: #84BB9F;
  color: #84BB9F;
}
.filter-tag.active {
  background-color: #84BB9F;
  color: white;
  border-color: #84BB9F;
}
.supply-card:hover {
  border-color: #84BB9F;
  box-shadow: 0 4px 12px rgba(132, 187, 159, 0.1);
}
</style>


