<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from '@/composables/useToast'
import { MapPin, Building2, Package, ShoppingCart, RefreshCw, Search, Navigation, MessageCircle, AlertTriangle, Zap, ArrowUpDown, Eye } from 'lucide-vue-next'
import { listMapCompanies, type MapCompanyMarkerResponse, type MapCompanyQuery } from '../api/map'
import { openChatConversation } from '../api/chat'
import { useAuthStore } from '../store/auth'
import { useUiStore } from '../store/ui'
import { Skeleton } from '../components/ui'
import { debounce } from '../utils/debounce'
import { regionData, codeToText } from 'element-china-area-data'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const ui = useUiStore()
const loading = ref(false)
const keyword = ref('')

// 筛选类型：all, supply, requirement, both
const filterType = ref<'all' | 'supply' | 'requirement' | 'both'>('all')

// D: 地域筛选
const regionCodes = ref<string[]>([])
const selectedProvince = computed(() => regionCodes.value[0] ? (codeToText[regionCodes.value[0]] || '') : '')
const selectedCity = computed(() => regionCodes.value[1] ? (codeToText[regionCodes.value[1]] || '') : '')

// 省市二级数据（去掉区县）
const provinceCityData = computed(() => {
  return regionData.map((prov: any) => ({
    value: prov.value,
    label: prov.label,
    children: (prov.children || []).map((city: any) => ({
      value: city.value,
      label: city.label,
    }))
  }))
})

// E: 公司类型筛选
const companyTypeOptions = [
  { value: '', label: '全部类型' },
  { value: 'feed_factory', label: '饲料厂' },
  { value: 'trader', label: '贸易商' },
  { value: 'grain_depot', label: '粮库' },
  { value: 'processor', label: '加工厂' },
  { value: 'logistics', label: '物流企业' },
  { value: 'other', label: '其他' },
]
const companyTypeLabels: Record<string, string> = Object.fromEntries(
  companyTypeOptions.filter(o => o.value).map(o => [o.value, o.label])
)
const selectedCompanyType = ref('')

// G: 排序
const sortOptions = [
  { value: 'latest', label: '最近更新' },
  { value: 'supply', label: '供应最多' },
  { value: 'requirement', label: '需求最多' },
  { value: 'total', label: '总数最多' },
]
const sortBy = ref('latest')

const amapKey = (import.meta as any).env?.VITE_AMAP_JS_KEY as string | undefined
const amapSecurityJsCode = (import.meta as any).env?.VITE_AMAP_SECURITY_JS_CODE as string | undefined
const hasKey = computed(() => Boolean(amapKey && String(amapKey).trim().length > 0))

const mapRef = ref<HTMLDivElement | null>(null)
let map: any = null
let cluster: any = null
let infoWindow: any = null

const raw = ref<MapCompanyMarkerResponse[]>([])

// 筛选后的列表
const filtered = computed(() => {
  let list = raw.value
  switch (filterType.value) {
    case 'supply':
      list = list.filter((x) => (x.supplyCount ?? 0) > 0)
      break
    case 'requirement':
      list = list.filter((x) => (x.requirementCount ?? 0) > 0)
      break
    case 'both':
      list = list.filter((x) => (x.supplyCount ?? 0) > 0 && (x.requirementCount ?? 0) > 0)
      break
  }
  return list
})

// G: 排序后的列表
const sorted = computed(() => {
  const list = [...filtered.value]
  switch (sortBy.value) {
    case 'supply':
      list.sort((a, b) => (b.supplyCount ?? 0) - (a.supplyCount ?? 0))
      break
    case 'requirement':
      list.sort((a, b) => (b.requirementCount ?? 0) - (a.requirementCount ?? 0))
      break
    case 'total':
      list.sort((a, b) => ((b.supplyCount ?? 0) + (b.requirementCount ?? 0)) - ((a.supplyCount ?? 0) + (a.requirementCount ?? 0)))
      break
    // 'latest' is default server order, no re-sort needed
  }
  return list
})

// 统计数据
const stats = computed(() => {
  const all = raw.value
  return {
    total: all.length,
    hasSupply: all.filter(x => (x.supplyCount ?? 0) > 0).length,
    hasRequirement: all.filter(x => (x.requirementCount ?? 0) > 0).length,
    hasBoth: all.filter(x => (x.supplyCount ?? 0) > 0 && (x.requirementCount ?? 0) > 0).length,
    withCoords: all.filter(x => x.lat && x.lng).length
  }
})

// 获取公司类型（供应/需求/供需兼有）
function getCompanyType(c: MapCompanyMarkerResponse): 'supply' | 'requirement' | 'both' | 'none' {
  const hasSupply = (c.supplyCount ?? 0) > 0
  const hasReq = (c.requirementCount ?? 0) > 0
  if (hasSupply && hasReq) return 'both'
  if (hasSupply) return 'supply'
  if (hasReq) return 'requirement'
  return 'none'
}

// 获取类型对应的颜色
function getTypeColor(type: string) {
  switch (type) {
    case 'supply': return { bar: 'bg-brand-500', bg: 'bg-brand-50', text: 'text-brand-600', gradient: 'from-brand-500 to-brand-600' }
    case 'requirement': return { bar: 'bg-action-500', bg: 'bg-action-50', text: 'text-action-600', gradient: 'from-action-500 to-action-700' }
    case 'both': return { bar: 'bg-gradient-to-b from-brand-500 to-action-500', bg: 'bg-action-50', text: 'text-action-600', gradient: 'from-action-500 to-accent-600' }
    default: return { bar: 'bg-neutral-300', bg: 'bg-neutral-50', text: 'text-neutral-500', gradient: 'from-neutral-400 to-neutral-500' }
  }
}

// C: 加载高德地图基础脚本
function loadAmapScript(): Promise<any> {
  return new Promise((resolve, reject) => {
    const w = window as any
    if (w.AMap) return resolve(w.AMap)
    if (!hasKey.value) return reject(new Error('缺少 VITE_AMAP_JS_KEY'))

    if (amapSecurityJsCode && String(amapSecurityJsCode).trim().length > 0) {
      w._AMapSecurityConfig = { securityJsCode: String(amapSecurityJsCode).trim() }
    }

    const id = 'amap-js-sdk'
    const existed = document.getElementById(id) as HTMLScriptElement | null
    if (existed) {
      existed.addEventListener('load', () => resolve(w.AMap))
      existed.addEventListener('error', reject)
      return
    }
    const s = document.createElement('script')
    s.id = id
    s.type = 'text/javascript'
    s.src = `https://webapi.amap.com/maps?v=2.0&key=${encodeURIComponent(String(amapKey).trim())}`
    s.onload = () => resolve(w.AMap)
    s.onerror = () => reject(new Error('高德地图脚本加载失败'))
    document.head.appendChild(s)
  })
}

// C: 动态加载 MarkerCluster 插件
function loadMarkerClusterPlugin(AMap: any): Promise<void> {
  return new Promise((resolve, reject) => {
    if (AMap.MarkerCluster) return resolve()
    AMap.plugin(['AMap.MarkerCluster'], () => {
      if (AMap.MarkerCluster) resolve()
      else reject(new Error('MarkerCluster 插件加载失败'))
    })
  })
}

async function initMap() {
  const AMap = await loadAmapScript()
  await loadMarkerClusterPlugin(AMap)
  if (!mapRef.value) return
  map = new AMap.Map(mapRef.value, {
    zoom: 4,
    center: [104.5, 35.5],
    zooms: [3, 18],
    viewMode: '2D',
    mapStyle: 'amap://styles/whitesmoke'
  })
  infoWindow = new AMap.InfoWindow({ offset: new AMap.Pixel(0, -36), isCustom: true })
}

// C: 清除聚合标记
function clearMarkers() {
  if (cluster) {
    cluster.setMap(null)
    cluster = null
  }
}

// C: MarkerClusterer 渲染
function renderMarkers() {
  if (!map) return
  clearMarkers()
  const AMap = (window as any).AMap

  const withCoords = filtered.value.filter(c => c.lat && c.lng)
  if (withCoords.length === 0) return

  const points = withCoords.map(c => ({
    lnglat: new AMap.LngLat(c.lng, c.lat),
    weight: (c.supplyCount ?? 0) + (c.requirementCount ?? 0),
    data: c
  }))

  cluster = new AMap.MarkerCluster(map, points, {
    gridSize: 60,
    maxZoom: 18,
    renderMarker(context: any) {
      if (!context.data || !context.data[0]) return
      const c = context.data[0].data as MapCompanyMarkerResponse
      if (!c) return
      const type = getCompanyType(c)
      let markerColor = '#2D6A4F'
      if (type === 'requirement') markerColor = '#2563eb'
      if (type === 'both') markerColor = '#2563eb'

      const content = document.createElement('div')
      content.innerHTML = `
        <div style="
          width: 32px; height: 32px;
          background: linear-gradient(135deg, ${markerColor}, ${type === 'both' ? '#1d4ed8' : markerColor});
          border-radius: 50% 50% 50% 0;
          transform: rotate(-45deg);
          box-shadow: 0 4px 12px ${markerColor}40;
          display: flex; align-items: center; justify-content: center;
        ">
          <div style="
            transform: rotate(45deg);
            color: white;
            font-size: 14px;
            font-weight: 700;
          ">${(c.supplyCount ?? 0) + (c.requirementCount ?? 0)}</div>
        </div>
      `
      context.marker.setContent(content)
      context.marker.setAnchor('bottom-center')
      context.marker.setOffset(new AMap.Pixel(0, 0))
      context.marker.on('click', () => openInfo(c, context.marker))
    },
    renderClusterMarker(context: any) {
      const count = context.count
      let size = 40
      if (count >= 100) size = 64
      else if (count >= 50) size = 56
      else if (count >= 20) size = 48
      else if (count >= 5) size = 44

      const content = document.createElement('div')
      content.innerHTML = `
        <div style="
          width: ${size}px; height: ${size}px;
          background: linear-gradient(135deg, #2D6A4F, #1a4532);
          border-radius: 50%;
          box-shadow: 0 4px 16px rgba(45,106,79,0.4);
          display: flex; align-items: center; justify-content: center;
          color: white;
          font-size: ${size >= 56 ? 16 : 14}px;
          font-weight: 700;
          border: 3px solid rgba(255,255,255,0.8);
        ">${count}</div>
      `
      context.marker.setContent(content)
      context.marker.setAnchor('center')
      context.marker.on('click', () => {
        const center = context.marker.getPosition()
        const curZoom = map.getZoom?.() ?? 4
        map.setZoomAndCenter(Math.min(curZoom + 3, 18), center)
      })
    }
  })

  // fitView — 用第一个点初始化 bounds，避免空 Bounds 从 (0,0) 开始导致缩放到全球
  if (withCoords.length > 0) {
    const first = withCoords[0]!
    const sw = new AMap.LngLat(first.lng, first.lat)
    const ne = new AMap.LngLat(first.lng, first.lat)
    const bounds = new AMap.Bounds(sw, ne)
    for (let i = 1; i < withCoords.length; i++) {
      bounds.extend(new AMap.LngLat(withCoords[i]!.lng, withCoords[i]!.lat))
    }
    map.setBounds(bounds, false, [80, 80, 80, 80])
  }
}

function focusCompany(c: MapCompanyMarkerResponse) {
  if (!map || !c.lat || !c.lng) return
  const AMap = (window as any).AMap
  const pos = new AMap.LngLat(c.lng, c.lat)
  map.setZoomAndCenter(Math.max(map.getZoom?.() ?? 10, 12), pos)
}

// A: 沟通功能
async function startChat(c: MapCompanyMarkerResponse) {
  if (!auth.me) {
    ui.openAuthDialog('login', { path: '/map' })
    return
  }
  if (!c.ownerUserId) {
    showToast.warning('该公司暂无联系人信息')
    return
  }
  if (c.ownerUserId === auth.me.userId) {
    showToast.info('不能和自己沟通')
    return
  }
  try {
    // 用第一条供应作为话题发起沟通（如有）
    const res = await openChatConversation({
      peerUserId: c.ownerUserId,
      subjectType: (c.supplyCount ?? 0) > 0 ? 'SUPPLY' : 'NEED',
      subjectId: 0, // 通用会话
    })
    if (res.code === 0 && res.data) {
      router.push(`/chat?conversationId=${res.data}`)
    } else {
      router.push('/chat')
    }
  } catch (e: any) {
    showToast.error('发起沟通失败：' + (e.response?.data?.message || e.message || '未知错误'))
  }
}

function openInfo(c: MapCompanyMarkerResponse, marker: any) {
  const type = getCompanyType(c)
  const typeColors = {
    supply: { bg: '#2D6A4F', light: '#f0f7f4' },
    requirement: { bg: '#2563eb', light: '#eff6ff' },
    both: { bg: '#2563eb', light: '#eff6ff' },
    none: { bg: '#71717a', light: '#fafafa' }
  }
  const color = typeColors[type]

  const supplyCats = (c.supplyCategories ?? []).slice(0, 5).map(cat =>
    `<span style="display:inline-block;padding:2px 8px;margin:2px;background:#f0f7f4;color:#2D6A4F;border-radius:12px;font-size:12px;">${escapeHtml(cat)}</span>`
  ).join('')

  const reqCats = (c.requirementCategories ?? []).slice(0, 5).map(cat =>
    `<span style="display:inline-block;padding:2px 8px;margin:2px;background:#eff6ff;color:#2563eb;border-radius:12px;font-size:12px;">${escapeHtml(cat)}</span>`
  ).join('')

  const html = `
    <div style="
      background: white;
      border-radius: 16px;
      box-shadow: 0 10px 40px rgba(0,0,0,0.15);
      min-width: 280px;
      max-width: 320px;
      overflow: hidden;
      font-family: system-ui, -apple-system, sans-serif;
    ">
      <div style="
        background: linear-gradient(135deg, ${color.bg}, ${type === 'both' ? '#1d4ed8' : color.bg});
        padding: 16px;
        color: white;
      ">
        <div style="font-size:16px;font-weight:700;margin-bottom:4px;">${escapeHtml(c.companyName)}</div>
        <div style="font-size:12px;opacity:0.9;">${escapeHtml(c.address ?? '地址未填写')}</div>
      </div>

      <div style="padding:16px;">
        <div style="display:flex;gap:12px;margin-bottom:12px;">
          <div onclick="window.__mapViewSupply && window.__mapViewSupply(${c.companyId})" style="flex:1;background:#f0f7f4;padding:10px;border-radius:12px;text-align:center;cursor:pointer;transition:all 0.2s;" onmouseover="this.style.background='#daeee3'" onmouseout="this.style.background='#f0f7f4'">
            <div style="font-size:20px;font-weight:700;color:#2D6A4F;">${c.supplyCount ?? 0}</div>
            <div style="font-size:12px;color:#71717a;">供应发布</div>
          </div>
          <div onclick="window.__mapViewNeed && window.__mapViewNeed(${c.companyId})" style="flex:1;background:#eff6ff;padding:10px;border-radius:12px;text-align:center;cursor:pointer;transition:all 0.2s;" onmouseover="this.style.background='#dbeafe'" onmouseout="this.style.background='#eff6ff'">
            <div style="font-size:20px;font-weight:700;color:#2563eb;">${c.requirementCount ?? 0}</div>
            <div style="font-size:12px;color:#71717a;">采购需求</div>
          </div>
        </div>

        ${supplyCats ? `<div style="margin-bottom:8px;"><div style="font-size:12px;color:#71717a;margin-bottom:4px;">供应品类</div>${supplyCats}</div>` : ''}
        ${reqCats ? `<div><div style="font-size:12px;color:#71717a;margin-bottom:4px;">采购品类</div>${reqCats}</div>` : ''}
      </div>

      <div style="padding:0 16px 16px;display:flex;gap:8px;">
        <button onclick="window.__mapViewChat && window.__mapViewChat(${c.companyId})" style="
          flex:1;
          padding:10px;
          background:linear-gradient(135deg,#2D6A4F,#1a4532);
          color:white;
          border:none;
          border-radius:10px;
          font-size:14px;
          font-weight:600;
          cursor:pointer;
        ">立即沟通</button>
        <button onclick="window.__mapViewProfile && window.__mapViewProfile(${c.companyId})" style="
          padding:10px 14px;
          background:#f5f5f5;
          color:#525252;
          border:none;
          border-radius:10px;
          font-size:14px;
          font-weight:600;
          cursor:pointer;
        ">查看主页</button>
      </div>
    </div>
  `

  infoWindow.setContent(html)
  infoWindow.open(map, marker.getPosition())
}

// A: 全局回调 — InfoWindow 按钮
;(window as any).__mapViewChat = (companyId: number) => {
  const company = raw.value.find(c => c.companyId === companyId)
  if (company) startChat(company)
}

;(window as any).__mapViewProfile = (companyId: number) => {
  router.push(`/companies/${companyId}`)
}

// 跳转到供应大厅
;(window as any).__mapViewSupply = (companyId: number) => {
  router.push(`/hall/supply?companyId=${companyId}`)
}

// 跳转到需求大厅
;(window as any).__mapViewNeed = (companyId: number) => {
  router.push(`/hall/need?companyId=${companyId}`)
}

function escapeHtml(s: string) {
  return s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}

async function refresh() {
  loading.value = true
  try {
    const params: MapCompanyQuery = {}
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (selectedProvince.value) params.province = selectedProvince.value
    if (selectedCity.value) params.city = selectedCity.value
    if (selectedCompanyType.value) params.companyType = selectedCompanyType.value

    const r = await listMapCompanies(params)
    if (r.code !== 0) throw new Error(r.message)
    raw.value = r.data ?? []
    if (map) renderMarkers()
    updateUrl()
  } catch (e: any) {
    showToast.error(e?.message ?? '加载地图数据失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  refresh()
}

// B: 搜索防抖
const debouncedRefresh = debounce(() => {
  refresh()
}, 300)

// 监听搜索关键字变化
watch(keyword, (newVal, oldVal) => {
  if (oldVal && oldVal.trim() && !newVal.trim()) {
    refresh()
  } else if (newVal.trim()) {
    debouncedRefresh()
  }
})

// D/E: 地域和公司类型变化时刷新
watch(regionCodes, () => { refresh() })
watch(selectedCompanyType, () => { refresh() })

// 筛选类型变化时渲染标记
watch(filterType, () => {
  renderMarkers()
  updateUrl()
})

// G: 排序变化时更新 URL
watch(sortBy, () => { updateUrl() })

// I: URL 状态持久化
function updateUrl() {
  const query: Record<string, string> = {}
  if (keyword.value.trim()) query.keyword = keyword.value.trim()
  if (filterType.value !== 'all') query.filterType = filterType.value
  if (regionCodes.value.length > 0) {
    if (selectedProvince.value) query.province = selectedProvince.value
    if (selectedCity.value) query.city = selectedCity.value
  }
  if (selectedCompanyType.value) query.companyType = selectedCompanyType.value
  if (sortBy.value !== 'latest') query.sort = sortBy.value
  router.replace({ query })
}

// I: 恢复 URL 状态
function restoreFromUrl() {
  const q = route.query
  if (q.keyword) keyword.value = String(q.keyword)
  if (q.filterType && ['all', 'supply', 'requirement', 'both'].includes(String(q.filterType))) {
    filterType.value = String(q.filterType) as any
  }
  if (q.companyType) selectedCompanyType.value = String(q.companyType)
  if (q.sort && ['latest', 'supply', 'requirement', 'total'].includes(String(q.sort))) {
    sortBy.value = String(q.sort)
  }
  // Restore region codes from province/city names
  if (q.province) {
    const province = String(q.province)
    const p = regionData.find((item: any) => item.label === province)
    if (p) {
      const codes = [p.value]
      if (q.city) {
        const city = String(q.city)
        const c = p.children?.find((item: any) => item.label === city)
        if (c) codes.push(c.value)
      }
      regionCodes.value = codes
    }
  }
}

onMounted(async () => {
  restoreFromUrl()
  try {
    await initMap()
  } catch (e: any) {
    showToast.warning(e?.message ?? '地图初始化失败（请配置 VITE_AMAP_JS_KEY）')
  }
  await refresh()
})
</script>

<template>
  <div class="map-view space-y-6">
    <!-- 标题区 -->
    <div class="flex items-center justify-between flex-wrap gap-4">
      <div>
        <h1 class="text-2xl font-bold text-neutral-900 flex items-center gap-2">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-brand-500 to-brand-600 flex items-center justify-center shadow-md shadow-brand-500/20">
            <MapPin class="w-5 h-5 text-white" />
          </div>
          全域供需地图
        </h1>
        <p class="text-sm text-neutral-500 mt-1 ml-12">发现全国优质供应商和采购商，一图览全局</p>
      </div>

      <!-- 搜索框 -->
      <div class="flex items-center gap-3">
        <div class="relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-neutral-400" />
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索公司、地址或品类..."
            class="w-64 pl-10 pr-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-brand-500 outline-none transition-all text-sm"
            @keyup.enter="handleSearch"
          />
        </div>
        <button
          class="flex items-center gap-2 px-4 py-2.5 bg-gradient-to-r from-brand-600 to-brand-600 hover:from-brand-700 hover:to-brand-700 text-white text-sm font-bold rounded-xl transition-all shadow-md shadow-brand-500/20"
          :disabled="loading"
          @click="refresh"
        >
          <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': loading }" />
          刷新
        </button>
      </div>
    </div>

    <!-- API Key 缺失提示 -->
    <div v-if="!hasKey" class="bg-warning-50 border border-warning-200 rounded-xl p-5">
      <div class="flex items-start gap-3">
        <div class="w-10 h-10 rounded-xl bg-warning-100 flex items-center justify-center shrink-0">
          <AlertTriangle class="w-5 h-5 text-warning-600" />
        </div>
        <div>
          <div class="font-bold text-warning-800 mb-2">地图功能需要配置高德 JS API Key</div>
          <div class="text-sm text-warning-700 space-y-1">
            <p>1. 访问 <a href="https://lbs.amap.com/" target="_blank" class="text-action-600 underline">高德开放平台</a> 注册账号并创建应用</p>
            <p>2. 申请 "Web端(JS API)" 类型的 Key</p>
            <p>3. 在 <code class="bg-warning-100 px-1.5 py-0.5 rounded text-xs">frontend/.env</code> 中添加 <code class="bg-warning-100 px-1.5 py-0.5 rounded text-xs">VITE_AMAP_JS_KEY=您的Key</code></p>
            <p>4. 重启前端开发服务器</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="bg-white rounded-xl border border-neutral-200 p-4">
      <div class="flex items-center justify-between flex-wrap gap-4">
        <!-- 供需类型筛选 -->
        <div class="flex flex-wrap gap-2">
          <button
            :class="[
              'px-4 py-2 text-sm font-bold rounded-xl transition-all flex items-center gap-2',
              filterType === 'all'
                ? 'bg-neutral-900 text-white shadow-md'
                : 'bg-neutral-100 text-neutral-600 hover:bg-neutral-200'
            ]"
            @click="filterType = 'all'"
          >
            <Building2 class="w-4 h-4" />
            全部
            <span class="px-1.5 py-0.5 text-[10px] rounded-full" :class="filterType === 'all' ? 'bg-white/20' : 'bg-neutral-200'">{{ stats.total }}</span>
          </button>

          <button
            :class="[
              'px-4 py-2 text-sm font-bold rounded-xl transition-all flex items-center gap-2',
              filterType === 'supply'
                ? 'bg-brand-600 text-white shadow-md shadow-brand-500/20'
                : 'bg-brand-50 text-brand-600 hover:bg-brand-100'
            ]"
            @click="filterType = 'supply'"
          >
            <Package class="w-4 h-4" />
            有供应
            <span class="px-1.5 py-0.5 text-[10px] rounded-full" :class="filterType === 'supply' ? 'bg-white/20' : 'bg-brand-100'">{{ stats.hasSupply }}</span>
          </button>

          <button
            :class="[
              'px-4 py-2 text-sm font-bold rounded-xl transition-all flex items-center gap-2',
              filterType === 'requirement'
                ? 'bg-action-600 text-white shadow-md shadow-action-500/20'
                : 'bg-action-50 text-action-600 hover:bg-action-100'
            ]"
            @click="filterType = 'requirement'"
          >
            <ShoppingCart class="w-4 h-4" />
            有需求
            <span class="px-1.5 py-0.5 text-[10px] rounded-full" :class="filterType === 'requirement' ? 'bg-white/20' : 'bg-action-100'">{{ stats.hasRequirement }}</span>
          </button>

          <button
            :class="[
              'px-4 py-2 text-sm font-bold rounded-xl transition-all flex items-center gap-2',
              filterType === 'both'
                ? 'bg-action-600 text-white shadow-md shadow-action-500/20'
                : 'bg-action-50 text-action-600 hover:bg-action-100'
            ]"
            @click="filterType = 'both'"
          >
            <Zap class="w-4 h-4" />
            供需兼有
            <span class="px-1.5 py-0.5 text-[10px] rounded-full" :class="filterType === 'both' ? 'bg-white/20' : 'bg-action-100'">{{ stats.hasBoth }}</span>
          </button>

          <!-- D: 地域筛选 -->
          <el-cascader
            v-model="regionCodes"
            :options="provinceCityData"
            :props="{ expandTrigger: 'hover', checkStrictly: true }"
            placeholder="选择地区"
            class="neo-cascader"
            clearable
            size="default"
          />

          <!-- E: 公司类型筛选 -->
          <select
            v-model="selectedCompanyType"
            class="px-3 py-2 text-sm border-2 border-neutral-200 rounded-xl bg-white focus:border-brand-500 outline-none transition-all"
          >
            <option v-for="opt in companyTypeOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>

        <div class="text-sm text-neutral-500">
          已标注 <span class="font-bold text-neutral-900">{{ stats.withCoords }}</span> 家公司
        </div>
      </div>
    </div>

    <!-- 主体内容 F: 自适应布局 -->
    <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
      <!-- 地图区域 -->
      <div class="lg:col-span-3 bg-white rounded-xl border border-neutral-200 overflow-hidden shadow-sm">
        <div ref="mapRef" class="h-[50vh] lg:h-[calc(100vh-280px)] lg:min-h-[400px] w-full"></div>
      </div>

      <!-- 公司列表 -->
      <div class="bg-white rounded-xl border border-neutral-200 overflow-hidden">
        <div class="p-4 border-b border-neutral-200 bg-neutral-50/50">
          <div class="flex items-center justify-between gap-2">
            <h3 class="text-2xl font-bold text-neutral-900 flex items-center gap-2">
              <Building2 class="w-4 h-4 text-neutral-500" />
              公司列表
            </h3>
            <div class="flex items-center gap-2">
              <!-- G: 排序选择 -->
              <div class="flex items-center gap-1">
                <ArrowUpDown class="w-3 h-3 text-neutral-400" />
                <select
                  v-model="sortBy"
                  class="text-xs border border-neutral-200 rounded-lg px-2 py-1 bg-white outline-none focus:border-brand-500"
                >
                  <option v-for="opt in sortOptions" :key="opt.value" :value="opt.value">
                    {{ opt.label }}
                  </option>
                </select>
              </div>
              <span class="text-xs text-neutral-500">{{ filtered.length }} 家</span>
            </div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="p-4 space-y-3">
          <div v-for="i in 5" :key="i" class="flex gap-3">
            <Skeleton type="avatar" class="!w-10 !h-10 !rounded-lg" />
            <div class="flex-1 space-y-2">
              <Skeleton type="text" class="!w-32" />
              <Skeleton type="text" class="!w-24" />
            </div>
          </div>
        </div>

        <!-- G: 使用 sorted 列表 / F: 自适应高度 -->
        <div v-else class="lg:max-h-[calc(100vh-280px)] overflow-y-auto">
          <div
            v-for="c in sorted"
            :key="c.companyId"
            class="company-card group flex border-b border-neutral-50 last:border-b-0 hover:bg-neutral-50/50 transition-all cursor-pointer"
            @click="focusCompany(c)"
          >
            <!-- 左侧状态色条 -->
            <div
              class="w-1 shrink-0"
              :class="getTypeColor(getCompanyType(c)).bar"
            />

            <div class="flex-1 p-4">
              <!-- 公司名称 + E: 类型标签 -->
              <div class="flex items-start justify-between gap-2">
                <div class="min-w-0">
                  <div class="font-bold text-neutral-900 truncate">{{ c.companyName }}</div>
                  <div class="text-xs text-neutral-500 truncate mt-0.5">{{ c.address || '地址未填写' }}</div>
                </div>
                <span
                  v-if="c.companyType && companyTypeLabels[c.companyType]"
                  class="shrink-0 text-[10px] px-2 py-0.5 bg-neutral-100 text-neutral-600 rounded-full font-medium"
                >{{ companyTypeLabels[c.companyType] }}</span>
              </div>

              <!-- 供需数量 -->
              <div class="flex items-center gap-3 mt-3">
                <div class="flex items-center gap-1.5 px-2 py-1 bg-brand-50 rounded-lg">
                  <Package class="w-3 h-3 text-brand-600" />
                  <span class="text-xs font-bold text-brand-600">{{ c.supplyCount ?? 0 }}</span>
                </div>
                <div class="flex items-center gap-1.5 px-2 py-1 bg-action-50 rounded-lg">
                  <ShoppingCart class="w-3 h-3 text-action-600" />
                  <span class="text-xs font-bold text-action-600">{{ c.requirementCount ?? 0 }}</span>
                </div>
              </div>

              <!-- 品类标签 -->
              <div v-if="(c.supplyCategories?.length || 0) + (c.requirementCategories?.length || 0) > 0" class="flex flex-wrap gap-1 mt-2">
                <span
                  v-for="cat in [...(c.supplyCategories ?? []).slice(0, 2), ...(c.requirementCategories ?? []).slice(0, 2)]"
                  :key="cat"
                  class="text-[10px] px-2 py-0.5 bg-neutral-100 text-neutral-600 rounded-full"
                >
                  {{ cat }}
                </span>
                <span
                  v-if="(c.supplyCategories?.length ?? 0) + (c.requirementCategories?.length ?? 0) > 4"
                  class="text-[10px] px-2 py-0.5 bg-neutral-100 text-neutral-400 rounded-full"
                >
                  +{{ (c.supplyCategories?.length ?? 0) + (c.requirementCategories?.length ?? 0) - 4 }}
                </span>
              </div>

              <!-- 坐标缺失提示 -->
              <div v-if="!c.lat || !c.lng" class="flex items-center gap-1.5 mt-3 text-warning-600">
                <AlertTriangle class="w-3.5 h-3.5" />
                <span class="text-[10px]">坐标缺失，请完善公司档案</span>
              </div>

              <!-- A: 操作按钮 -->
              <div v-else class="flex gap-2 mt-3 opacity-0 group-hover:opacity-100 transition-opacity">
                <button
                  class="flex items-center gap-1 px-2.5 py-1.5 bg-neutral-100 hover:bg-neutral-200 text-neutral-600 text-xs font-medium rounded-lg transition-all"
                  @click.stop="focusCompany(c)"
                >
                  <Navigation class="w-3 h-3" />
                  定位
                </button>
                <button
                  class="flex items-center gap-1 px-2.5 py-1.5 bg-brand-50 hover:bg-brand-100 text-brand-600 text-xs font-medium rounded-lg transition-all"
                  @click.stop="startChat(c)"
                >
                  <MessageCircle class="w-3 h-3" />
                  沟通
                </button>
                <button
                  class="flex items-center gap-1 px-2.5 py-1.5 bg-neutral-100 hover:bg-neutral-200 text-neutral-600 text-xs font-medium rounded-lg transition-all"
                  @click.stop="router.push(`/companies/${c.companyId}`)"
                >
                  <Eye class="w-3 h-3" />
                  主页
                </button>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="filtered.length === 0 && !loading" class="p-8 text-center">
            <div class="w-16 h-16 mx-auto mb-4 rounded-xl bg-neutral-100 flex items-center justify-center">
              <Building2 class="w-8 h-8 text-neutral-300" />
            </div>
            <p class="text-sm font-medium text-neutral-500">暂无符合条件的公司</p>
            <p class="text-xs text-neutral-400 mt-1">尝试调整筛选条件</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 公司卡片入场动画 */
.company-card {
  animation: fade-in 0.2s ease-out;
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateX(-8px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 滚动条美化 */
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.2);
}
</style>
