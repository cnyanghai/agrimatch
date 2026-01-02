<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { requireAuth } from '../../utils/requireAuth'
import PublicTopNav from '../../components/PublicTopNav.vue'
import ChatDrawer from '../../components/chat/ChatDrawer.vue'
import { listSupplies, type SupplyResponse } from '../../api/supply'
import { openChatConversation } from '../../api/chat'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

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

const focusIdFromRoute = computed(() => {
  const raw = route.query.focusId
  const s = Array.isArray(raw) ? raw[0] : raw
  const n = s ? Number(s) : NaN
  return Number.isFinite(n) ? n : null
})

const displaySupplies = computed(() => {
  // 默认只展示前 10 条；若存在 focusId，则展示完整列表以保证可定位
  if (focusIdFromRoute.value) return supplies.value
  return supplies.value.slice(0, 10)
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
    const res = await listSupplies({ activeOnly: true, includeExpired: false, orderBy: 'create_time', order: 'desc' })
    if (res.code !== 0) throw new Error(res.message)
    supplies.value = res.data || []
  } catch {
    supplies.value = []
  } finally {
    listLoading.value = false
    applyFocusIfNeeded()
  }
}

onMounted(() => {
  loadSupplies()
})

watch(focusIdFromRoute, () => {
  applyFocusIfNeeded()
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

    <!-- 筛选区 -->
    <section class="bg-white border-b shadow-sm">
      <div class="max-w-7xl mx-auto px-4 py-6">
        <div class="flex flex-col md:flex-row gap-4 mb-6">
          <div class="flex-1 relative">
            <input
              type="text"
              placeholder="搜索品种、产地、指标或公司..."
              class="w-full border-2 border-gray-100 rounded-xl py-2.5 px-10 focus:border-emerald-500 outline-none transition-all"
            />
            <svg class="w-5 h-5 absolute left-3 top-3 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
          </div>
          <button class="px-8 py-2.5 bg-emerald-600 text-white rounded-xl font-bold hover:bg-emerald-700 transition-all active:scale-95">查询信息</button>
        </div>

        <div class="space-y-4">
          <div class="flex items-start gap-4 text-xs">
            <span class="text-gray-400 shrink-0 mt-1.5 font-medium">常见品种:</span>
            <div class="flex flex-wrap gap-2">
              <button class="filter-tag active px-3 py-1 border rounded-full">全部</button>
              <button class="filter-tag px-3 py-1 border rounded-full">玉米</button>
              <button class="filter-tag px-3 py-1 border rounded-full">豆粕</button>
              <button class="filter-tag px-3 py-1 border rounded-full">棉粕</button>
              <button class="filter-tag px-3 py-1 border rounded-full">菜粕</button>
              <button class="filter-tag px-3 py-1 border rounded-full">DDGS</button>
              <button class="filter-tag px-3 py-1 border rounded-full">赖氨酸</button>
              <button class="filter-tag px-3 py-1 border rounded-full">蛋氨酸</button>
              <button class="filter-tag px-3 py-1 border rounded-full">磷酸氢钙</button>
            </div>
          </div>
          <div class="flex items-start gap-4 text-xs">
            <span class="text-gray-400 shrink-0 mt-1.5 font-medium">物流区域:</span>
            <div class="flex flex-wrap gap-2">
              <button class="filter-tag px-3 py-1 border rounded-full">东北区</button>
              <button class="filter-tag px-3 py-1 border rounded-full">华北区</button>
              <button class="filter-tag px-3 py-1 border rounded-full">华中区</button>
              <button class="filter-tag px-3 py-1 border rounded-full">华东沿海</button>
              <button class="filter-tag px-3 py-1 border rounded-full">华南沿海</button>
              <button class="filter-tag px-3 py-1 border rounded-full">西南区</button>
              <button class="filter-tag px-3 py-1 border rounded-full">西北区</button>
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
            <div class="w-full lg:w-44 flex items-center gap-3 shrink-0 border-r border-gray-50 pr-4">
              <div class="w-12 h-12 bg-emerald-100 text-emerald-700 rounded-lg flex items-center justify-center text-xl font-bold shrink-0">
                {{ (s.companyName || s.nickName || s.userName || '供')[0] }}
              </div>
              <div class="overflow-hidden">
                <div class="text-sm font-bold text-gray-900 truncate">{{ s.companyName || '未填写公司' }}</div>
                <div class="flex items-center gap-1 mt-1">
                  <span class="bg-blue-50 text-blue-600 text-[10px] px-1 py-0.5 rounded">供应</span>
                  <span class="text-[10px] text-gray-400">{{ s.nickName || s.userName || '' }}</span>
                </div>
              </div>
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
              <span class="text-[10px] text-gray-400">今日已有 12 人咨询</span>
            </div>
          </div>
        </div>

        <div v-if="!listLoading && supplies.length === 0" class="bg-white rounded-2xl border border-gray-100 p-8 text-gray-400 text-sm">
          暂无货源数据（后续会继续完善筛选与排序）
        </div>
      </div>

      <div class="flex justify-center mt-10 gap-2">
        <button class="px-4 py-2 bg-white border rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition-colors">上一页</button>
        <button class="px-4 py-2 bg-emerald-600 text-white border-emerald-600 rounded-lg text-sm font-bold">1</button>
        <button class="px-4 py-2 bg-white border rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition-colors">2</button>
        <button class="px-4 py-2 bg-white border rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition-colors">3</button>
        <button class="px-4 py-2 bg-white border rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition-colors">下一页</button>
      </div>
    </main>

    <footer class="bg-white border-t py-8 mt-12">
      <div class="max-w-7xl mx-auto px-4 text-center">
        <p class="text-xs text-gray-400">© 2024 AgriMatch - 饲料原料全产业链高效撮合平台</p>
      </div>
    </footer>

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


