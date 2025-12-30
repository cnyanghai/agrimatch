<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listMapCompanies, type MapCompanyMarkerResponse } from '../api/map'

const loading = ref(false)
const keyword = ref('')
const onlySupply = ref(false)
const onlyRequirement = ref(false)

const amapKey = (import.meta as any).env?.VITE_AMAP_JS_KEY as string | undefined
const amapSecurityJsCode = (import.meta as any).env?.VITE_AMAP_SECURITY_JS_CODE as string | undefined
const hasKey = computed(() => Boolean(amapKey && String(amapKey).trim().length > 0))

const mapRef = ref<HTMLDivElement | null>(null)
let map: any = null
let markers: any[] = []
let infoWindow: any = null

const raw = ref<MapCompanyMarkerResponse[]>([])

const filtered = computed(() => {
  let list = raw.value
  if (onlySupply.value) list = list.filter((x) => (x.supplyCount ?? 0) > 0)
  if (onlyRequirement.value) list = list.filter((x) => (x.requirementCount ?? 0) > 0)
  return list
})

function loadAmapScript(): Promise<any> {
  return new Promise((resolve, reject) => {
    const w = window as any
    if (w.AMap) return resolve(w.AMap)
    if (!hasKey.value) return reject(new Error('缺少 VITE_AMAP_JS_KEY'))

    // 可选：高德安全密钥（部分新建 Key 会要求配置 securityJsCode）
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
    viewMode: '2D'
  })
  infoWindow = new AMap.InfoWindow({ offset: new AMap.Pixel(0, -30) })
}

function clearMarkers() {
  if (!map) return
  for (const m of markers) {
    try {
      map.remove(m)
    } catch {
      // ignore
    }
  }
  markers = []
}

function renderMarkers() {
  if (!map) return
  clearMarkers()
  const AMap = (window as any).AMap
  for (const c of filtered.value) {
    if (!c.lat || !c.lng) continue
    const marker = new AMap.Marker({
      position: [c.lng, c.lat],
      title: c.companyName
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
  if (!map) return
  if (!c.lat || !c.lng) return
  const AMap = (window as any).AMap
  const pos = new AMap.LngLat(c.lng, c.lat)
  map.setZoomAndCenter(Math.max(map.getZoom?.() ?? 10, 10), pos)
  // 找到对应 marker 并打开浮窗
  const mk = markers.find((m) => {
    try {
      const p = m.getPosition?.()
      return p && Number(p.getLat?.()) === Number(c.lat) && Number(p.getLng?.()) === Number(c.lng)
    } catch {
      return false
    }
  })
  if (mk) openInfo(c, mk)
}

function openInfo(c: MapCompanyMarkerResponse, marker: any) {
  const supplyCats = (c.supplyCategories ?? []).slice(0, 8).join('、') || '—'
  const reqCats = (c.requirementCategories ?? []).slice(0, 8).join('、') || '—'
  const html = `
    <div style="min-width:260px;">
      <div style="font-weight:700;margin-bottom:6px;">${escapeHtml(c.companyName)}</div>
      <div style="font-size:12px;color:#666;margin-bottom:8px;">${escapeHtml(c.address ?? '')}</div>
      <div style="display:flex;gap:10px;font-size:12px;margin-bottom:6px;">
        <div>供应：<b>${c.supplyCount ?? 0}</b></div>
        <div>需求：<b>${c.requirementCount ?? 0}</b></div>
      </div>
      <div style="font-size:12px;color:#333;margin-bottom:4px;">供应品类：${escapeHtml(supplyCats)}</div>
      <div style="font-size:12px;color:#333;">采购品类：${escapeHtml(reqCats)}</div>
    </div>
  `
  infoWindow.setContent(html)
  infoWindow.open(map, marker.getPosition())
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
  <div class="space-y-4">
    <div class="bg-white rounded-xl shadow-card border border-gray-100 p-4">
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
        <div>
          <div class="text-xl font-bold text-gray-800">全域地图</div>
          <div class="text-sm text-gray-500">展示所有公司点位及其供需发布情况（坐标来自公司档案或地址自动解析）</div>
        </div>

        <div class="flex flex-wrap gap-2 items-center">
          <el-input v-model="keyword" placeholder="公司名/地址关键字" style="width: 240px;" clearable @keyup.enter="refresh" />
          <el-checkbox v-model="onlySupply">仅看有供应</el-checkbox>
          <el-checkbox v-model="onlyRequirement">仅看有需求</el-checkbox>
          <el-button :loading="loading" type="primary" @click="refresh">刷新</el-button>
        </div>
      </div>

      <div v-if="!hasKey" class="mt-3 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
        <div class="flex items-center gap-2 text-yellow-800 font-medium mb-2">
          <el-icon class="w-5 h-5"><Warning /></el-icon>
          地图功能需要配置高德 JS API Key
        </div>
        <div class="text-sm text-yellow-700 space-y-2">
          <p>请按以下步骤配置：</p>
          <ol class="list-decimal list-inside space-y-1 ml-2">
            <li>访问 <a href="https://lbs.amap.com/" target="_blank" class="text-blue-600 underline">高德开放平台</a> 注册账号</li>
            <li>创建应用并申请 "Web端(JS API)" 类型的 Key</li>
            <li>在 <code class="bg-yellow-100 px-1 rounded">frontend/.env</code> 文件中添加：<code class="bg-yellow-100 px-1 rounded">VITE_AMAP_JS_KEY=您的Key</code></li>
            <li>如果Key是2021年12月后创建的，还需添加：<code class="bg-yellow-100 px-1 rounded">VITE_AMAP_SECURITY_JS_CODE=您的安全密钥</code></li>
            <li>重启前端开发服务器</li>
          </ol>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-4 gap-4">
      <div class="lg:col-span-3 bg-white rounded-xl shadow-card border border-gray-100 overflow-hidden">
        <div ref="mapRef" class="h-[560px] w-full"></div>
      </div>

      <div class="bg-white rounded-xl shadow-card border border-gray-100 p-4">
        <div class="font-bold text-gray-800 mb-2">公司列表</div>
        <div class="text-xs text-gray-500 mb-3">共 {{ filtered.length }} 家（无坐标的公司不会出现在地图上）</div>
        <div class="space-y-2 max-h-[520px] overflow-auto pr-1">
          <div v-for="c in filtered" :key="c.companyId" class="p-3 border border-gray-100 rounded-lg hover:shadow-card transition-all-300">
            <div class="font-medium text-gray-800">{{ c.companyName }}</div>
            <div class="text-xs text-gray-500 mt-1 truncate">{{ c.address || '-' }}</div>
            <div class="text-xs text-gray-700 mt-2 flex gap-3">
              <span>供应 {{ c.supplyCount ?? 0 }}</span>
              <span>需求 {{ c.requirementCount ?? 0 }}</span>
            </div>
            <div v-if="(!c.lat || !c.lng)" class="text-xs text-red-600 mt-2">
              缺少坐标：去“我的档案-公司档案”补地址或手填坐标
            </div>
            <div v-else class="mt-2">
              <el-button size="small" @click="focusCompany(c)">定位</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>





