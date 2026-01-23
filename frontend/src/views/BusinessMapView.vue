<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MapPin, Building2, Package, ShoppingCart, RefreshCw, Search, Navigation, MessageCircle, AlertTriangle, Zap } from 'lucide-vue-next'
import { listMapCompanies, type MapCompanyMarkerResponse } from '../api/map'
import { Skeleton } from '../components/ui'

const router = useRouter()
const loading = ref(false)
const keyword = ref('')

// 筛选类型：all, supply, requirement, both
const filterType = ref<'all' | 'supply' | 'requirement' | 'both'>('all')

const amapKey = (import.meta as any).env?.VITE_AMAP_JS_KEY as string | undefined
const amapSecurityJsCode = (import.meta as any).env?.VITE_AMAP_SECURITY_JS_CODE as string | undefined
const hasKey = computed(() => Boolean(amapKey && String(amapKey).trim().length > 0))

const mapRef = ref<HTMLDivElement | null>(null)
let map: any = null
let markers: any[] = []
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
    case 'supply': return { bar: 'bg-brand-500', bg: 'bg-brand-50', text: 'text-brand-600', gradient: 'from-brand-500 to-teal-600' }
    case 'requirement': return { bar: 'bg-blue-500', bg: 'bg-blue-50', text: 'text-blue-600', gradient: 'from-blue-500 to-indigo-600' }
    case 'both': return { bar: 'bg-gradient-to-b from-brand-500 to-blue-500', bg: 'bg-purple-50', text: 'text-purple-600', gradient: 'from-purple-500 to-pink-600' }
    default: return { bar: 'bg-gray-300', bg: 'bg-gray-50', text: 'text-gray-500', gradient: 'from-gray-400 to-gray-500' }
  }
}

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

async function initMap() {
  const AMap = await loadAmapScript()
  if (!mapRef.value) return
  map = new AMap.Map(mapRef.value, {
    zoom: 4,
    center: [105.0, 35.0],
    viewMode: '2D',
    mapStyle: 'amap://styles/whitesmoke'
  })
  infoWindow = new AMap.InfoWindow({ offset: new AMap.Pixel(0, -36), isCustom: true })
}

function clearMarkers() {
  if (!map) return
  for (const m of markers) {
    try { map.remove(m) } catch { /* ignore */ }
  }
  markers = []
}

function renderMarkers() {
  if (!map) return
  clearMarkers()
  const AMap = (window as any).AMap
  
  for (const c of filtered.value) {
    if (!c.lat || !c.lng) continue
    
    const type = getCompanyType(c)
    let markerColor = '#10b981' // emerald for supply
    if (type === 'requirement') markerColor = '#3b82f6' // blue
    if (type === 'both') markerColor = '#8b5cf6' // purple
    
    // 自定义标记图标
    const markerContent = `
      <div style="
        width: 32px; height: 32px;
        background: linear-gradient(135deg, ${markerColor}, ${type === 'both' ? '#3b82f6' : markerColor});
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
    
    const marker = new AMap.Marker({
      position: [c.lng, c.lat],
      content: markerContent,
      anchor: 'bottom-center',
      offset: new AMap.Pixel(0, 0)
    })
    
    marker.on('click', () => openInfo(c, marker))
    markers.push(marker)
  }
  
  if (markers.length > 0) {
    map.add(markers)
    map.setFitView(markers, true, [80, 80, 80, 80], 8)
  }
}

function focusCompany(c: MapCompanyMarkerResponse) {
  if (!map || !c.lat || !c.lng) return
  const AMap = (window as any).AMap
  const pos = new AMap.LngLat(c.lng, c.lat)
  map.setZoomAndCenter(Math.max(map.getZoom?.() ?? 10, 12), pos)
  
  const mk = markers.find((m) => {
    try {
      const p = m.getPosition?.()
      return p && Number(p.getLat?.()) === Number(c.lat) && Number(p.getLng?.()) === Number(c.lng)
    } catch { return false }
  })
  if (mk) openInfo(c, mk)
}

function openInfo(c: MapCompanyMarkerResponse, marker: any) {
  const type = getCompanyType(c)
  const typeColors = {
    supply: { bg: '#10b981', light: '#ecfdf5' },
    requirement: { bg: '#3b82f6', light: '#eff6ff' },
    both: { bg: '#8b5cf6', light: '#f5f3ff' },
    none: { bg: '#6b7280', light: '#f9fafb' }
  }
  const color = typeColors[type]
  
  const supplyCats = (c.supplyCategories ?? []).slice(0, 5).map(cat => 
    `<span style="display:inline-block;padding:2px 8px;margin:2px;background:#ecfdf5;color:#059669;border-radius:12px;font-size:12px;">${escapeHtml(cat)}</span>`
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
        background: linear-gradient(135deg, ${color.bg}, ${type === 'both' ? '#3b82f6' : color.bg});
        padding: 16px;
        color: white;
      ">
        <div style="font-size:16px;font-weight:700;margin-bottom:4px;">${escapeHtml(c.companyName)}</div>
        <div style="font-size:12px;opacity:0.9;">${escapeHtml(c.address ?? '地址未填写')}</div>
      </div>
      
      <div style="padding:16px;">
        <div style="display:flex;gap:12px;margin-bottom:12px;">
          <div onclick="window.__mapViewSupply && window.__mapViewSupply(${c.companyId})" style="flex:1;background:#ecfdf5;padding:10px;border-radius:12px;text-align:center;cursor:pointer;transition:all 0.2s;" onmouseover="this.style.background='#d1fae5'" onmouseout="this.style.background='#ecfdf5'">
            <div style="font-size:20px;font-weight:700;color:#059669;">${c.supplyCount ?? 0}</div>
            <div style="font-size:12px;color:#6b7280;">供应发布</div>
          </div>
          <div onclick="window.__mapViewNeed && window.__mapViewNeed(${c.companyId})" style="flex:1;background:#eff6ff;padding:10px;border-radius:12px;text-align:center;cursor:pointer;transition:all 0.2s;" onmouseover="this.style.background='#dbeafe'" onmouseout="this.style.background='#eff6ff'">
            <div style="font-size:20px;font-weight:700;color:#2563eb;">${c.requirementCount ?? 0}</div>
            <div style="font-size:12px;color:#6b7280;">采购需求</div>
          </div>
        </div>
        
        ${supplyCats ? `<div style="margin-bottom:8px;"><div style="font-size:12px;color:#6b7280;margin-bottom:4px;">供应品类</div>${supplyCats}</div>` : ''}
        ${reqCats ? `<div><div style="font-size:12px;color:#6b7280;margin-bottom:4px;">采购品类</div>${reqCats}</div>` : ''}
      </div>
      
      <div style="padding:0 16px 16px;display:flex;gap:8px;">
        <button onclick="window.__mapViewChat && window.__mapViewChat(${c.companyId}, '${escapeHtml(c.companyName)}')" style="
          flex:1;
          padding:10px;
          background:linear-gradient(135deg,#059669,#0d9488);
          color:white;
          border:none;
          border-radius:10px;
          font-size:14px;
          font-weight:600;
          cursor:pointer;
        ">立即沟通</button>
      </div>
    </div>
  `
  
  infoWindow.setContent(html)
  infoWindow.open(map, marker.getPosition())
}

// 全局回调用于信息窗口按钮
;(window as any).__mapViewChat = async (companyId: number, _companyName: string) => {
  try {
    // 查找该公司的用户ID（使用公司管理员）
    const company = raw.value.find(c => c.companyId === companyId)
    if (!company) {
      ElMessage.warning('未找到该公司信息')
      return
    }
    
    // 假设该公司有发布供应/需求，我们需要找到一个可以联系的用户
    // 这里直接跳转到聊天页面，让用户从那里选择具体的供应/需求来发起沟通
    ElMessage.success(`正在跳转到消息中心...`)
    router.push('/chat')
  } catch (e: any) {
    ElMessage.error('跳转失败：' + (e.message || '未知错误'))
  }
}

// 跳转到供应大厅，筛选该公司
;(window as any).__mapViewSupply = (companyId: number) => {
  router.push(`/hall/supply?companyId=${companyId}`)
}

// 跳转到需求大厅，筛选该公司
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
    const r = await listMapCompanies(keyword.value.trim() || undefined)
    if (r.code !== 0) throw new Error(r.message)
    raw.value = r.data ?? []
    if (map) renderMarkers()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载地图数据失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  refresh()
}

onMounted(async () => {
  try {
    await initMap()
  } catch (e: any) {
    ElMessage.warning(e?.message ?? '地图初始化失败（请配置 VITE_AMAP_JS_KEY）')
  }
  await refresh()
})
</script>

<template>
  <div class="map-view space-y-6">
    <!-- 标题区 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-brand-500 to-teal-600 flex items-center justify-center shadow-md shadow-brand-500/20">
            <MapPin class="w-5 h-5 text-white" />
          </div>
          全域供需地图
        </h1>
        <p class="text-sm text-gray-500 mt-1 ml-12">发现全国优质供应商和采购商，一图览全局</p>
      </div>
      
      <!-- 搜索框 -->
      <div class="flex items-center gap-3">
        <div class="relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索公司名称或地址..."
            class="w-64 pl-10 pr-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all text-sm"
            @keyup.enter="handleSearch"
          />
        </div>
        <button
          class="flex items-center gap-2 px-4 py-2.5 bg-gradient-to-r from-brand-600 to-teal-600 hover:from-brand-700 hover:to-teal-700 text-white text-sm font-bold rounded-xl transition-all  shadow-md shadow-brand-500/20"
          :disabled="loading"
          @click="refresh"
        >
          <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': loading }" />
          刷新
        </button>
      </div>
    </div>
    
    <!-- API Key 缺失提示 -->
    <div v-if="!hasKey" class="bg-amber-50 border border-amber-200 rounded-xl p-5">
      <div class="flex items-start gap-3">
        <div class="w-10 h-10 rounded-xl bg-amber-100 flex items-center justify-center shrink-0">
          <AlertTriangle class="w-5 h-5 text-amber-600" />
        </div>
        <div>
          <div class="font-bold text-amber-800 mb-2">地图功能需要配置高德 JS API Key</div>
          <div class="text-sm text-amber-700 space-y-1">
            <p>1. 访问 <a href="https://lbs.amap.com/" target="_blank" class="text-blue-600 underline">高德开放平台</a> 注册账号并创建应用</p>
            <p>2. 申请 "Web端(JS API)" 类型的 Key</p>
            <p>3. 在 <code class="bg-amber-100 px-1.5 py-0.5 rounded text-xs">frontend/.env</code> 中添加 <code class="bg-amber-100 px-1.5 py-0.5 rounded text-xs">VITE_AMAP_JS_KEY=您的Key</code></p>
            <p>4. 重启前端开发服务器</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 筛选器 -->
    <div class="bg-white rounded-xl border border-gray-200 p-4">
      <div class="flex items-center justify-between flex-wrap gap-4">
        <div class="flex gap-2">
          <button
            :class="[
              'px-4 py-2 text-sm font-bold rounded-xl transition-all flex items-center gap-2',
              filterType === 'all' 
                ? 'bg-gray-900 text-white shadow-md' 
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            ]"
            @click="filterType = 'all'; renderMarkers()"
          >
            <Building2 class="w-4 h-4" />
            全部
            <span class="px-1.5 py-0.5 text-[10px] rounded-full" :class="filterType === 'all' ? 'bg-white/20' : 'bg-gray-200'">{{ stats.total }}</span>
          </button>
          
          <button
            :class="[
              'px-4 py-2 text-sm font-bold rounded-xl transition-all flex items-center gap-2',
              filterType === 'supply' 
                ? 'bg-brand-600 text-white shadow-md shadow-brand-500/20' 
                : 'bg-brand-50 text-brand-600 hover:bg-brand-100'
            ]"
            @click="filterType = 'supply'; renderMarkers()"
          >
            <Package class="w-4 h-4" />
            有供应
            <span class="px-1.5 py-0.5 text-[10px] rounded-full" :class="filterType === 'supply' ? 'bg-white/20' : 'bg-brand-100'">{{ stats.hasSupply }}</span>
          </button>
          
          <button
            :class="[
              'px-4 py-2 text-sm font-bold rounded-xl transition-all flex items-center gap-2',
              filterType === 'requirement' 
                ? 'bg-blue-600 text-white shadow-md shadow-blue-500/20' 
                : 'bg-blue-50 text-blue-600 hover:bg-blue-100'
            ]"
            @click="filterType = 'requirement'; renderMarkers()"
          >
            <ShoppingCart class="w-4 h-4" />
            有需求
            <span class="px-1.5 py-0.5 text-[10px] rounded-full" :class="filterType === 'requirement' ? 'bg-white/20' : 'bg-blue-100'">{{ stats.hasRequirement }}</span>
          </button>
          
          <button
            :class="[
              'px-4 py-2 text-sm font-bold rounded-xl transition-all flex items-center gap-2',
              filterType === 'both' 
                ? 'bg-purple-600 text-white shadow-md shadow-purple-500/20' 
                : 'bg-purple-50 text-purple-600 hover:bg-purple-100'
            ]"
            @click="filterType = 'both'; renderMarkers()"
          >
            <Zap class="w-4 h-4" />
            供需兼有
            <span class="px-1.5 py-0.5 text-[10px] rounded-full" :class="filterType === 'both' ? 'bg-white/20' : 'bg-purple-100'">{{ stats.hasBoth }}</span>
          </button>
        </div>
        
        <div class="text-sm text-gray-500">
          已标注 <span class="font-bold text-gray-900">{{ stats.withCoords }}</span> 家公司
        </div>
      </div>
    </div>
    
    <!-- 主体内容 -->
    <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
      <!-- 地图区域 -->
      <div class="lg:col-span-3 bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
        <div ref="mapRef" class="h-[600px] w-full"></div>
      </div>
      
      <!-- 公司列表 -->
      <div class="bg-white rounded-xl border border-gray-200 overflow-hidden">
        <div class="p-4 border-b border-gray-200 bg-gray-50/50">
          <div class="flex items-center justify-between">
            <h3 class="text-2xl font-bold text-gray-900 flex items-center gap-2">
              <Building2 class="w-4 h-4 text-gray-500" />
              公司列表
            </h3>
            <span class="text-xs text-gray-500">{{ filtered.length }} 家</span>
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
        
        <!-- 公司列表 -->
        <div v-else class="max-h-[540px] overflow-y-auto">
          <div
            v-for="c in filtered"
            :key="c.companyId"
            class="company-card group flex border-b border-gray-50 last:border-b-0 hover:bg-gray-50/50 transition-all cursor-pointer"
            @click="focusCompany(c)"
          >
            <!-- 左侧状态色条 -->
            <div 
              class="w-1 shrink-0"
              :class="getTypeColor(getCompanyType(c)).bar"
            />
            
            <div class="flex-1 p-4">
              <!-- 公司名称 -->
              <div class="flex items-start justify-between gap-2">
                <div class="min-w-0">
                  <div class="font-bold text-gray-900 truncate">{{ c.companyName }}</div>
                  <div class="text-xs text-gray-500 truncate mt-0.5">{{ c.address || '地址未填写' }}</div>
                </div>
              </div>
              
              <!-- 供需数量 -->
              <div class="flex items-center gap-3 mt-3">
                <div class="flex items-center gap-1.5 px-2 py-1 bg-brand-50 rounded-lg">
                  <Package class="w-3 h-3 text-brand-600" />
                  <span class="text-xs font-bold text-brand-600">{{ c.supplyCount ?? 0 }}</span>
                </div>
                <div class="flex items-center gap-1.5 px-2 py-1 bg-blue-50 rounded-lg">
                  <ShoppingCart class="w-3 h-3 text-blue-600" />
                  <span class="text-xs font-bold text-blue-600">{{ c.requirementCount ?? 0 }}</span>
                </div>
              </div>
              
              <!-- 品类标签 -->
              <div v-if="(c.supplyCategories?.length || 0) + (c.requirementCategories?.length || 0) > 0" class="flex flex-wrap gap-1 mt-2">
                <span 
                  v-for="cat in [...(c.supplyCategories ?? []).slice(0, 2), ...(c.requirementCategories ?? []).slice(0, 2)]" 
                  :key="cat"
                  class="text-[10px] px-2 py-0.5 bg-gray-100 text-gray-600 rounded-full"
                >
                  {{ cat }}
                </span>
                <span 
                  v-if="(c.supplyCategories?.length ?? 0) + (c.requirementCategories?.length ?? 0) > 4"
                  class="text-[10px] px-2 py-0.5 bg-gray-100 text-gray-400 rounded-full"
                >
                  +{{ (c.supplyCategories?.length ?? 0) + (c.requirementCategories?.length ?? 0) - 4 }}
                </span>
              </div>
              
              <!-- 坐标缺失提示 -->
              <div v-if="!c.lat || !c.lng" class="flex items-center gap-1.5 mt-3 text-amber-600">
                <AlertTriangle class="w-3.5 h-3.5" />
                <span class="text-[10px]">坐标缺失，请完善公司档案</span>
              </div>
              
              <!-- 操作按钮 -->
              <div v-else class="flex gap-2 mt-3 opacity-0 group-hover:opacity-100 transition-opacity">
                <button
                  class="flex items-center gap-1 px-2.5 py-1.5 bg-gray-100 hover:bg-gray-200 text-gray-600 text-xs font-medium rounded-lg transition-all"
                  @click.stop="focusCompany(c)"
                >
                  <Navigation class="w-3 h-3" />
                  定位
                </button>
                <button
                  class="flex items-center gap-1 px-2.5 py-1.5 bg-brand-50 hover:bg-brand-100 text-brand-600 text-xs font-medium rounded-lg transition-all"
                  @click.stop="ElMessage.info('功能开发中')"
                >
                  <MessageCircle class="w-3 h-3" />
                  沟通
                </button>
              </div>
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-if="filtered.length === 0 && !loading" class="p-8 text-center">
            <div class="w-16 h-16 mx-auto mb-4 rounded-xl bg-gray-100 flex items-center justify-center">
              <Building2 class="w-8 h-8 text-gray-300" />
            </div>
            <p class="text-sm font-medium text-gray-500">暂无符合条件的公司</p>
            <p class="text-xs text-gray-400 mt-1">尝试调整筛选条件</p>
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
