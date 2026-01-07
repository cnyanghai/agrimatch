<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { requireAuth } from '../../utils/requireAuth'
import PublicTopNav from '../../components/PublicTopNav.vue'
import PublicFooter from '../../components/PublicFooter.vue'
import ChatDrawer from '../../components/chat/ChatDrawer.vue'
import { listSupplies, type SupplyResponse } from '../../api/supply'
import { openChatConversation } from '../../api/chat'
import { followUser, unfollowUser, checkFollowStatus } from '../../api/follow'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../store/auth'

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

// 关注状态 Map: userId -> isFollowing
const followingMap = ref<Map<number, boolean>>(new Map())

// 检查并加载关注状态
async function loadFollowStatus(userIds: number[]) {
  if (!authStore.isLoggedIn) return
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
  // 前端分页（后端暂不支持分页参数）
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
    exFactoryPrice: s.exFactoryPrice,
    quantity: s.quantity,
    shipAddress: s.shipAddress,
    deliveryMode: s.deliveryMode,
    packaging: s.packaging,
    paramsJson: s.paramsJson
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
  } catch {
    supplies.value = []
    total.value = 0
  } finally {
    listLoading.value = false
    applyFocusIfNeeded()
  }
}

// 选择品种筛选
function selectCategory(cat: string | null) {
  selectedCategory.value = selectedCategory.value === cat ? null : cat
  currentPage.value = 1 // 重置分页
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

// 解析品类参数 JSON，提取关键参数显示（兼容新旧格式）
function parseParams(paramsJson?: string): string {
  if (!paramsJson) return '暂无参数'
  try {
    const data = JSON.parse(paramsJson)
    const params = data?.params || {}
    const entries = Object.entries(params)
    if (entries.length === 0) return '暂无参数'
    // 取前3个参数显示，兼容新格式 { name, value } 和旧格式（直接值）
    return entries
      .slice(0, 3)
      .map(([k, v]) => {
        // 新格式：{ name, value }
        if (typeof v === 'object' && v !== null && 'name' in v && 'value' in v) {
          const name = (v as any).name || k
          const value = (v as any).value
          return `${name}:${value}`
        }

        // 旧格式：键=参数名，值=参数值
        const raw = String(v ?? '')
        if (!raw) return `${k}:-`
        // 若值本身已经是 "参数名:参数值" 形式，则直接复用，避免重复
        if (raw.startsWith(`${k}:`)) return raw
        return `${k}:${raw}`
      })
      .join(' / ')
  } catch {
    return '暂无参数'
  }
}
</script>

<template>
  <div class="bg-gray-50 text-gray-900 min-h-screen">
    <!-- 顶部行情走马灯 -->
    <div class="bg-slate-900 text-white py-2 overflow-hidden">
      <div class="flex animate-marquee space-x-12 px-4 text-xs font-medium">
        <span>玉米主力 2505: <b class="text-red-400">2450 ↑</b></span>
        <span>豆粕主力 2505: <b class="text-green-400">3210 ↓</b></span>
        <span>菜粕 2505: <b class="text-red-400">2680 ↑</b></span>
        <span>山东现货玉米: <b>2380</b></span>
        <span>广东港口玉米: <b>2520</b></span>
        <span>玉米主力 2505: <b class="text-red-400">2450 ↑</b></span>
        <span>豆粕主力 2505: <b class="text-green-400">3210 ↓</b></span>
      </div>
    </div>

    <PublicTopNav>
      <template #actions>
        <button class="bg-emerald-600 text-white px-5 py-2 rounded-full font-bold hover:bg-emerald-700 transition-all active:scale-95" @click="onPublishSupply">
          发布供应
        </button>
      </template>
    </PublicTopNav>

    <!-- 公司筛选提示 -->
    <div v-if="companyIdFilter" class="bg-emerald-50 border-b border-emerald-100">
      <div class="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
        <div class="flex items-center gap-2 text-emerald-700">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" /></svg>
          <span class="text-sm font-medium">
            正在查看该公司的供应信息
          </span>
        </div>
        <button 
          class="px-3 py-1 bg-emerald-100 hover:bg-emerald-200 text-emerald-700 rounded-lg text-sm font-medium transition-all"
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
              class="w-full border-2 border-gray-100 rounded-xl py-2.5 px-10 focus:border-emerald-500 outline-none transition-all"
              @keyup.enter="onSearch"
            />
            <svg class="w-5 h-5 absolute left-3 top-3 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
          </div>
          <button 
            class="px-8 py-2.5 bg-emerald-600 text-white rounded-xl font-bold hover:bg-emerald-700 transition-all active:scale-95"
            @click="onSearch"
          >
            查询信息
          </button>
        </div>

        <div class="space-y-4">
          <div class="flex items-start gap-4 text-xs">
            <span class="text-gray-400 shrink-0 mt-1.5 font-medium">常见品种:</span>
            <div class="flex flex-wrap gap-2">
              <button 
                :class="['px-3 py-1 border rounded-full transition-all', selectedCategory === null ? 'bg-emerald-600 text-white border-emerald-600' : 'border-gray-200 hover:border-emerald-500 hover:text-emerald-600']"
                @click="selectCategory(null)"
              >
                全部
              </button>
              <button 
                v-for="cat in categoryOptions" 
                :key="cat"
                :class="['px-3 py-1 border rounded-full transition-all', selectedCategory === cat ? 'bg-emerald-600 text-white border-emerald-600' : 'border-gray-200 hover:border-emerald-500 hover:text-emerald-600']"
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
        <div v-if="listLoading" class="bg-white rounded-2xl border border-gray-100 p-8 text-gray-400 text-sm">
          正在加载货源...
        </div>

        <div
          v-for="s in displaySupplies"
          :key="s.id"
          :ref="(el) => setCardEl(Number(s.id), el as any)"
          class="supply-card bg-white rounded-2xl p-5 border border-gray-100 transition-all"
          :class="focusedId === s.id ? 'ring-2 ring-emerald-500/60 bg-emerald-50/40' : 'hover:shadow-md hover:border-emerald-100'"
        >
          <div class="flex flex-col lg:flex-row lg:flex-wrap items-start gap-6 mx-0">
            <div class="w-full lg:w-52 flex items-center gap-3 shrink-0 border-r border-gray-50 pr-4">
              <div class="w-12 h-12 bg-emerald-100 text-emerald-700 rounded-lg flex items-center justify-center text-xl font-bold shrink-0">
                {{ (s.companyName || s.nickName || s.userName || '供')[0] }}
              </div>
              <div class="overflow-hidden flex-1 min-w-0">
                <div class="text-sm font-bold text-gray-900 truncate">{{ s.companyName || '未填写公司' }}</div>
                <div class="flex items-center gap-1 mt-1">
                  <span class="bg-emerald-50 text-emerald-600 text-[10px] px-1 py-0.5 rounded">供应</span>
                  <span class="text-[10px] text-gray-400">{{ s.nickName || s.userName || '' }}</span>
                </div>
              </div>
              <!-- 关注按钮 -->
              <button
                v-if="authStore.isLoggedIn && s.userId"
                class="shrink-0 text-xs px-2 py-1 rounded-full border transition-all active:scale-95"
                :class="isFollowingUser(s.userId)
                  ? 'bg-emerald-50 text-emerald-600 border-emerald-200'
                  : 'bg-white text-gray-500 border-gray-200 hover:border-emerald-300 hover:text-emerald-600'"
                @click.stop="toggleFollow(s)"
              >
                {{ isFollowingUser(s.userId) ? '已关注' : '+ 关注' }}
              </button>
            </div>

            <div class="w-full lg:w-[120px] shrink-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="bg-orange-100 text-orange-600 text-[10px] px-1.5 py-0.5 rounded font-bold">现货</span>
                <span class="text-gray-400 text-[10px]">ID: {{ s.id }}</span>
              </div>
              <h3 class="text-lg font-bold text-gray-900 truncate">{{ s.categoryName }}</h3>
              <div class="mt-1 text-xl font-black text-red-600 italic">
                <span class="whitespace-nowrap inline-flex items-baseline gap-1">
                  <span>¥{{ s.exFactoryPrice }}</span>
                  <span class="text-xs font-normal text-gray-400 not-italic">元/吨</span>
                </span>
              </div>
            </div>

            <div class="flex-1 grid grid-cols-2 md:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_minmax(0,1fr)_190px] gap-4 text-xs py-2 bg-gray-50/50 rounded-lg px-4">
              <div class="min-w-0">
                <div class="text-gray-400 mb-0.5">规格参数</div>
                <div class="font-semibold text-gray-700 truncate">{{ parseParams(s.paramsJson) }}</div>
              </div>
              <div class="min-w-0">
                <div class="text-gray-400 mb-0.5">发货地</div>
                <div class="font-semibold text-gray-700 truncate">{{ s.shipAddress || '-' }}</div>
              </div>
              <div class="min-w-0">
                <div class="text-gray-400 mb-0.5">库存</div>
                <div class="font-semibold text-gray-700 truncate">{{ s.quantity ?? '-' }}吨</div>
              </div>
              <div class="min-w-0">
                <div class="text-gray-400 mb-0.5">物流方式</div>
                <div class="font-semibold text-gray-700 truncate">{{ s.deliveryMode || '-' }}</div>
              </div>
            </div>

            <div class="shrink-0 flex flex-col items-center gap-2">
              <button class="px-8 py-3 bg-emerald-600 text-white rounded-xl font-bold text-sm shadow-md shadow-emerald-50 hover:bg-emerald-700 transition-all active:scale-95" @click="onConsult(s)">
                立即咨询
              </button>
            </div>
          </div>
        </div>

        <div v-if="!listLoading && supplies.length === 0" class="bg-white rounded-2xl border border-gray-100 p-8 text-center">
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
  border-color: #10b981;
  color: #10b981;
}
.filter-tag.active {
  background-color: #10b981;
  color: white;
  border-color: #10b981;
}
.supply-card:hover {
  border-color: #10b981;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.05);
}
.animate-marquee {
  display: inline-block;
  animation: marquee 30s linear infinite;
}
@keyframes marquee {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}
</style>


