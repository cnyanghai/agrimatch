<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { listMapCompanies, type MapCompanyMarkerResponse } from '../../api/map'

// ==================== 数据层 ====================
const loading = ref(true)
const companies = ref<MapCompanyMarkerResponse[]>([])
const keyword = ref('')
const filterType = ref<'all' | 'supply' | 'requirement'>('all')
const viewMode = ref<'list' | 'map'>('list')
const mapReady = ref(false)
const mapError = ref('')

// 高德 JS API Key（从环境变量读取，与 Web 端共享同一个 Key）
const amapKey = (import.meta as any).env?.VITE_AMAP_JS_KEY as string | undefined
const amapSecurityJsCode = (import.meta as any).env?.VITE_AMAP_SECURITY_JS_CODE as string | undefined
const hasKey = computed(() => Boolean(amapKey && String(amapKey).trim().length > 0))

const filteredCompanies = computed(() => {
  let list = companies.value
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    list = list.filter(c =>
      c.companyName.toLowerCase().includes(kw) ||
      (c.address || '').toLowerCase().includes(kw)
    )
  }
  if (filterType.value === 'supply') {
    list = list.filter(c => c.supplyCount > 0)
  } else if (filterType.value === 'requirement') {
    list = list.filter(c => c.requirementCount > 0)
  }
  return list
})

const withCoords = computed(() => filteredCompanies.value.filter(c => c.lat && c.lng))

// 原生 map 组件 markers（非 H5 时使用）
const nativeMarkers = computed(() => {
  return withCoords.value.map(c => ({
    id: c.companyId,
    latitude: c.lat!,
    longitude: c.lng!,
    title: c.companyName,
    width: 24,
    height: 24,
    callout: {
      content: `${c.companyName}\n供应${c.supplyCount} | 需求${c.requirementCount}`,
      display: 'BYCLICK' as const,
      padding: 8,
      borderRadius: 8,
      fontSize: 12,
      bgColor: '#ffffff',
      color: '#1C1917',
    },
  }))
})

// ==================== 高德地图 JS API（H5 模式） ====================
const mapContainerRef = ref<any>(null)
let map: any = null
let cluster: any = null

/** 动态加载高德地图 JS SDK（跟 Web 端同一方式） */
function loadAmapScript(): Promise<any> {
  return new Promise((resolve, reject) => {
    // #ifdef H5
    const w = window as any
    if (w.AMap) return resolve(w.AMap)
    if (!hasKey.value) return reject(new Error('缺少高德地图 Key，请配置 VITE_AMAP_JS_KEY'))

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
    // #endif
    // #ifndef H5
    reject(new Error('非 H5 环境'))
    // #endif
  })
}

/** 加载 MarkerCluster 插件 */
function loadClusterPlugin(AMap: any): Promise<void> {
  return new Promise((resolve, reject) => {
    if (AMap.MarkerCluster) return resolve()
    AMap.plugin(['AMap.MarkerCluster'], () => {
      if (AMap.MarkerCluster) resolve()
      else reject(new Error('MarkerCluster 插件加载失败'))
    })
  })
}

/** 初始化高德地图实例 */
async function initAmapMap() {
  if (map) return
  try {
    const AMap = await loadAmapScript()
    await loadClusterPlugin(AMap)
    await nextTick()
    const el = mapContainerRef.value?.$el || mapContainerRef.value
    if (!el) return
    map = new AMap.Map(el, {
      zoom: 4,
      center: [104.5, 35.5],
      zooms: [3, 18],
      viewMode: '2D',
      mapStyle: 'amap://styles/whitesmoke',
    })
    mapReady.value = true
    renderMarkers()
  } catch (e: any) {
    mapError.value = e?.message || '地图加载失败'
  }
}

function clearMarkers() {
  if (cluster) {
    cluster.setMap(null)
    cluster = null
  }
}

/** 渲染聚合标记点 */
function renderMarkers() {
  if (!map) return
  clearMarkers()
  // #ifdef H5
  const AMap = (window as any).AMap
  if (!AMap) return

  const list = withCoords.value
  if (list.length === 0) return

  const points = list.map(c => ({
    lnglat: new AMap.LngLat(c.lng, c.lat),
    weight: (c.supplyCount ?? 0) + (c.requirementCount ?? 0),
    data: c,
  }))

  cluster = new AMap.MarkerCluster(map, points, {
    gridSize: 60,
    maxZoom: 18,
    renderMarker(context: any) {
      if (!context.data?.[0]) return
      const c = context.data[0].data as MapCompanyMarkerResponse
      if (!c) return
      const hasSupply = (c.supplyCount ?? 0) > 0
      const hasReq = (c.requirementCount ?? 0) > 0
      const color = hasSupply && hasReq ? '#2563eb' : hasReq ? '#c28a55' : '#2D6A4F'

      const content = document.createElement('div')
      content.innerHTML = `
        <div style="
          width:32px;height:32px;
          background:linear-gradient(135deg,${color},${color}dd);
          border-radius:50% 50% 50% 0;
          transform:rotate(-45deg);
          box-shadow:0 4px 12px ${color}40;
          display:flex;align-items:center;justify-content:center;
          cursor:pointer;
        ">
          <div style="transform:rotate(45deg);color:#fff;font-size:13px;font-weight:700;">
            ${(c.supplyCount ?? 0) + (c.requirementCount ?? 0)}
          </div>
        </div>
      `
      context.marker.setContent(content)
      context.marker.setAnchor('bottom-center')
      context.marker.setOffset(new AMap.Pixel(0, 0))
      context.marker.on('click', () => goCompany(c.companyId))
    },
    renderClusterMarker(context: any) {
      const count = context.count
      const sz = Math.min(24 + Math.sqrt(count) * 4, 56)
      const el = document.createElement('div')
      el.innerHTML = `
        <div style="
          width:${sz}px;height:${sz}px;
          background:rgba(45,106,79,0.85);
          border:3px solid rgba(255,255,255,0.9);
          border-radius:50%;
          display:flex;align-items:center;justify-content:center;
          color:#fff;font-weight:800;font-size:${Math.max(11, sz * 0.3)}px;
          box-shadow:0 4px 16px rgba(45,106,79,0.35);
          cursor:pointer;
        ">${count}</div>
      `
      context.marker.setContent(el)
      context.marker.setAnchor('center')
    },
  })

  // 自适应视野
  if (list.length > 0) {
    const first = list[0]!
    const sw = new AMap.LngLat(first.lng, first.lat)
    const ne = new AMap.LngLat(first.lng, first.lat)
    const bounds = new AMap.Bounds(sw, ne)
    for (let i = 1; i < list.length; i++) {
      bounds.extend(new AMap.LngLat(list[i]!.lng, list[i]!.lat))
    }
    map.setBounds(bounds, false, [80, 80, 80, 80])
  }
  // #endif
}

/** 聚焦某企业 → 切换到地图并定位 */
function focusCompany(company: MapCompanyMarkerResponse) {
  if (!company.lat || !company.lng) return
  viewMode.value = 'map'
  nextTick(() => {
    if (!map) {
      initAmapMap()
    } else {
      // #ifdef H5
      const AMap = (window as any).AMap
      if (AMap) {
        map.setZoomAndCenter(12, new AMap.LngLat(company.lng, company.lat))
      }
      // #endif
    }
  })
}

// ==================== 数据加载 ====================
onMounted(() => loadData())
onPullDownRefresh(() => loadData().finally(() => uni.stopPullDownRefresh()))

async function loadData() {
  loading.value = true
  try {
    const params = keyword.value.trim() ? { keyword: keyword.value.trim() } : undefined
    companies.value = await listMapCompanies(params) || []
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadData()
}

function goCompany(companyId: number) {
  uni.navigateTo({ url: `/pages/company/detail?id=${companyId}` })
}

function handleNativeMarkerTap(e: any) {
  const markerId = e.detail?.markerId || e.markerId
  const c = companies.value.find(c => c.companyId === markerId)
  if (c) goCompany(c.companyId)
}

function getCategoryTags(c: MapCompanyMarkerResponse): string[] {
  const tags: string[] = []
  if (c.supplyCategories?.length) tags.push(...c.supplyCategories.slice(0, 3))
  if (c.requirementCategories?.length) tags.push(...c.requirementCategories.slice(0, 2))
  return [...new Set(tags)].slice(0, 4)
}

function getInitial(c: MapCompanyMarkerResponse): string {
  return c.companyName?.charAt(0) || '企'
}

// 切换到地图视图时初始化
watch(viewMode, (mode) => {
  if (mode === 'map') {
    nextTick(() => initAmapMap())
  }
})

// 筛选条件变化 → 重新渲染标记
watch([filteredCompanies, mapReady], () => {
  if (mapReady.value && viewMode.value === 'map') {
    renderMarkers()
  }
})

onUnmounted(() => {
  clearMarkers()
  if (map) {
    map.destroy?.()
    map = null
  }
})
</script>

<template>
  <view class="map-page">
    <!-- 顶部搜索 & 筛选 -->
    <view class="top-bar safe-area-top">
      <view class="search-bar">
        <WgIcon name="search" :size="16" color="#A8A29E" />
        <input
          v-model="keyword"
          class="search-bar__input"
          placeholder="搜索企业名称/地址"
          placeholder-class="search-bar__placeholder"
          confirm-type="search"
          @confirm="handleSearch"
        />
        <view v-if="keyword" class="search-bar__clear" @tap="keyword = ''; handleSearch()">
          <WgIcon name="clear" :size="16" color="#A8A29E" />
        </view>
      </view>

      <view class="action-row">
        <view class="filter-pills">
          <text class="filter-pill" :class="{ 'filter-pill--active': filterType === 'all' }" @tap="filterType = 'all'">全部</text>
          <text class="filter-pill" :class="{ 'filter-pill--active': filterType === 'supply' }" @tap="filterType = 'supply'">有供应</text>
          <text class="filter-pill" :class="{ 'filter-pill--active': filterType === 'requirement' }" @tap="filterType = 'requirement'">有采购</text>
        </view>
        <view class="view-toggle" @tap="viewMode = viewMode === 'list' ? 'map' : 'list'">
          <WgIcon :name="viewMode === 'list' ? 'map-pin' : 'layout-grid'" :size="16" color="#2D6A4F" />
          <text class="view-toggle__text">{{ viewMode === 'list' ? '地图' : '列表' }}</text>
        </view>
      </view>
    </view>

    <!-- 统计栏 -->
    <view class="stats-bar">
      <text class="stats-bar__text">共 <text class="stats-bar__count font-mono">{{ filteredCompanies.length }}</text> 家企业</text>
      <text v-if="viewMode === 'map'" class="stats-bar__sub">（{{ withCoords.length }} 家有坐标）</text>
    </view>

    <!-- ==================== 列表视图 ==================== -->
    <template v-if="viewMode === 'list'">
      <WgSkeleton v-if="loading && companies.length === 0" type="card" :rows="4" />

      <view v-else-if="filteredCompanies.length > 0" class="company-list">
        <view
          v-for="c in filteredCompanies"
          :key="c.companyId"
          class="company-card tap-feedback"
          @tap="goCompany(c.companyId)"
        >
          <view class="company-card__header">
            <view class="company-card__avatar">
              <text class="company-card__avatar-text">{{ getInitial(c) }}</text>
            </view>
            <view class="company-card__info">
              <text class="company-card__name">{{ c.companyName }}</text>
              <view v-if="c.address" class="company-card__address-row">
                <WgIcon name="map-pin" :size="12" color="#A8A29E" />
                <text class="company-card__address">{{ c.address }}</text>
              </view>
            </view>
            <view v-if="c.lat && c.lng" class="company-card__locate" @tap.stop="focusCompany(c)">
              <WgIcon name="navigation" :size="16" color="#2D6A4F" />
            </view>
          </view>

          <view v-if="getCategoryTags(c).length > 0" class="company-card__tags">
            <text v-for="tag in getCategoryTags(c)" :key="tag" class="company-card__tag">{{ tag }}</text>
          </view>

          <view class="company-card__footer">
            <view v-if="c.supplyCount > 0" class="company-card__stat">
              <WgIcon name="store" :size="14" color="#2D6A4F" />
              <text class="company-card__stat-text company-card__stat-text--brand">供应 {{ c.supplyCount }}</text>
            </view>
            <view v-if="c.requirementCount > 0" class="company-card__stat">
              <WgIcon name="shopping-bag" :size="14" color="#c28a55" />
              <text class="company-card__stat-text company-card__stat-text--autumn">需求 {{ c.requirementCount }}</text>
            </view>
            <view v-if="!c.supplyCount && !c.requirementCount" class="company-card__stat">
              <text class="company-card__stat-text company-card__stat-text--muted">暂无供需</text>
            </view>
            <WgIcon name="right" :size="14" color="#d1d5db" />
          </view>
        </view>
      </view>

      <WgEmpty v-else-if="!loading" text="暂无企业数据" description="换个关键词或筛选条件试试" />
    </template>

    <!-- ==================== 地图视图 ==================== -->
    <template v-else>
      <view class="map-wrapper">
        <!-- H5 模式：高德 JS API 渲染 -->
        <!-- #ifdef H5 -->
        <view ref="mapContainerRef" class="amap-container" />
        <view v-if="mapError" class="map-error">
          <WgIcon name="info" :size="32" color="#E76F51" />
          <text class="map-error__text">{{ mapError }}</text>
          <view class="map-error__btn" @tap="viewMode = 'list'">
            <text class="map-error__btn-text">切换到列表</text>
          </view>
        </view>
        <!-- #endif -->

        <!-- 原生 App 模式：uni-app map 组件 -->
        <!-- #ifndef H5 -->
        <map
          class="native-map"
          :latitude="35.86"
          :longitude="104.19"
          :scale="5"
          :markers="nativeMarkers"
          :show-location="true"
          @markertap="handleNativeMarkerTap"
        />
        <!-- #endif -->

        <!-- 底部浮窗 -->
        <view class="map-overlay">
          <text class="map-overlay__text">{{ withCoords.length }} 家企业</text>
          <view class="map-overlay__btn" @tap="viewMode = 'list'">
            <WgIcon name="layout-grid" :size="14" color="#2D6A4F" />
            <text class="map-overlay__btn-text">列表</text>
          </view>
        </view>
      </view>
    </template>
  </view>
</template>

<style lang="scss" scoped>
.map-page {
  min-height: 100vh;
  background: $bg-page;
  display: flex;
  flex-direction: column;
}

.top-bar {
  background: #ffffff;
  padding: $spacing-sm $spacing-md;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
  z-index: 20;
  position: sticky;
  top: 0;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  background: $bg-page;
  border-radius: $radius-pill;
  padding: $spacing-sm $spacing-lg;
  margin-bottom: $spacing-xs;

  &__input {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;
    background: transparent;
  }
  &__placeholder { color: $text-placeholder; }
  &__clear { padding: 4rpx; flex-shrink: 0; }
}

.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filter-pills {
  display: flex;
  gap: $spacing-xs;
}

.filter-pill {
  font-size: $font-sm;
  padding: $spacing-xs $spacing-md;
  border-radius: $radius-pill;
  background: $bg-page;
  color: $text-secondary;
  &--active {
    background: $brand-50;
    color: $brand-600;
    font-weight: 600;
  }
}

.view-toggle {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: $spacing-xs $spacing-md;
  border-radius: $radius-pill;
  background: $brand-50;
  transition: transform 0.15s;
  &:active { transform: scale(0.95); }
  &__text {
    font-size: $font-sm;
    color: $brand-600;
    font-weight: 600;
  }
}

.stats-bar {
  padding: $spacing-xs $spacing-md;
  background: #ffffff;
  border-bottom: 1rpx solid $border-light;
  display: flex;
  align-items: center;
  gap: $spacing-xs;

  &__text { font-size: $font-sm; color: $text-secondary; }
  &__count { color: $brand-600; font-weight: 700; }
  &__sub { font-size: $font-xs; color: $text-placeholder; }
}

.company-list { padding: $spacing-sm; }

.company-card {
  background: #ffffff;
  border-radius: $radius-xl;
  padding: $spacing-lg;
  margin-bottom: $spacing-sm;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  transition: transform 0.15s;
  &:active { transform: scale(0.98); }

  &__header {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    margin-bottom: $spacing-sm;
  }
  &__avatar {
    width: 88rpx; height: 88rpx;
    border-radius: $radius-lg;
    background: $brand-50;
    display: flex; align-items: center; justify-content: center;
    flex-shrink: 0;
  }
  &__avatar-text { font-size: 36rpx; font-weight: 800; color: $brand-600; }
  &__info { flex: 1; min-width: 0; }
  &__name {
    display: block;
    font-size: $font-lg; font-weight: bold; color: $text-primary;
    overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
    margin-bottom: 4rpx;
  }
  &__address-row { display: flex; align-items: center; gap: 4rpx; }
  &__address {
    font-size: $font-sm; color: $text-secondary;
    overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  }
  &__locate {
    width: 68rpx; height: 68rpx;
    border-radius: 50%;
    background: $brand-50;
    display: flex; align-items: center; justify-content: center;
    flex-shrink: 0;
    &:active { background: $brand-100; }
  }
  &__tags {
    display: flex; flex-wrap: wrap; gap: $spacing-xs;
    margin-bottom: $spacing-sm;
  }
  &__tag {
    font-size: $font-xs; color: $autumn-500; background: $autumn-50;
    padding: 4rpx 14rpx; border-radius: $radius-pill; font-weight: 500;
  }
  &__footer {
    display: flex; align-items: center; gap: $spacing-md;
    padding-top: $spacing-sm;
    border-top: 1rpx solid $border-light;
  }
  &__stat { display: flex; align-items: center; gap: 4rpx; }
  &__stat-text {
    font-size: $font-sm; font-weight: 600;
    &--brand { color: $brand-600; }
    &--autumn { color: $autumn-500; }
    &--muted { color: $text-placeholder; font-weight: 400; }
  }
}

.map-wrapper {
  flex: 1;
  position: relative;
  min-height: 60vh;
}

.amap-container {
  width: 100%;
  height: 100%;
  min-height: 60vh;
}

.native-map {
  width: 100%;
  height: 100%;
  min-height: 60vh;
}

.map-error {
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  display: flex; flex-direction: column; align-items: center;
  gap: $spacing-md;
  padding: $spacing-xl;

  &__text { font-size: $font-md; color: $text-secondary; text-align: center; }
  &__btn {
    padding: $spacing-sm $spacing-xl;
    background: $brand-600;
    border-radius: $radius-pill;
    &:active { opacity: 0.85; }
  }
  &__btn-text { font-size: $font-md; color: #ffffff; font-weight: 600; }
}

.map-overlay {
  position: absolute;
  bottom: calc(#{$spacing-md} + env(safe-area-inset-bottom));
  left: $spacing-md; right: $spacing-md;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  border-radius: $radius-xl;
  padding: $spacing-md $spacing-lg;
  display: flex; align-items: center; justify-content: space-between;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.08);

  &__text { font-size: $font-sm; color: $text-primary; font-weight: 600; }
  &__btn {
    display: flex; align-items: center; gap: 4rpx;
    padding: $spacing-xs $spacing-md;
    border-radius: $radius-pill;
    background: $brand-50;
    &:active { background: $brand-100; }
  }
  &__btn-text { font-size: $font-sm; color: $brand-600; font-weight: 600; }
}
</style>
