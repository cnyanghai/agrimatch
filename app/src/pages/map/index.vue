<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { listMapCompanies, type MapCompanyMarkerResponse } from '../../api/map'

const loading = ref(true)
const companies = ref<MapCompanyMarkerResponse[]>([])
const keyword = ref('')
const filterType = ref<'all' | 'supply' | 'requirement'>('all')
const showList = ref(false)

// Map settings
const latitude = ref(35.86)  // China center
const longitude = ref(104.19)
const scale = ref(5)

const markers = computed(() => {
  return filteredCompanies.value
    .filter(c => c.lat && c.lng)
    .map(c => ({
      id: c.companyId,
      latitude: c.lat!,
      longitude: c.lng!,
      title: c.companyName,
      iconPath: '/static/tab/supply-active.png',
      width: 24,
      height: 24,
      callout: {
        content: `${c.companyName}\n供应${c.supplyCount} | 需求${c.requirementCount}`,
        display: 'BYCLICK',
        padding: 8,
        borderRadius: 8,
        fontSize: 12,
        bgColor: '#ffffff',
        color: '#1C1917',
      },
    }))
})

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

onMounted(() => loadData())

onPullDownRefresh(() => {
  loadData().finally(() => uni.stopPullDownRefresh())
})

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

function locateCompany(company: MapCompanyMarkerResponse) {
  if (company.lat && company.lng) {
    latitude.value = company.lat
    longitude.value = company.lng
    scale.value = 12
    showList.value = false
  }
}

function handleMarkerTap(e: any) {
  const markerId = e.detail?.markerId || e.markerId
  const company = companies.value.find(c => c.companyId === markerId)
  if (company) {
    goCompany(company.companyId)
  }
}
</script>

<template>
  <view class="map-page">
    <!-- Top search bar -->
    <view class="top-bar safe-area-top">
      <view class="search-bar">
        <uni-icons type="search" size="16" color="#A8A29E" />
        <input
          v-model="keyword"
          class="search-bar__input"
          placeholder="搜索企业名称/地址"
          placeholder-class="search-bar__placeholder"
          confirm-type="search"
          @confirm="handleSearch"
        />
      </view>
      <view class="filter-pills">
        <text
          class="filter-pill"
          :class="{ 'filter-pill--active': filterType === 'all' }"
          @tap="filterType = 'all'"
        >全部</text>
        <text
          class="filter-pill"
          :class="{ 'filter-pill--active': filterType === 'supply' }"
          @tap="filterType = 'supply'"
        >有供应</text>
        <text
          class="filter-pill"
          :class="{ 'filter-pill--active': filterType === 'requirement' }"
          @tap="filterType = 'requirement'"
        >有采购</text>
      </view>
    </view>

    <!-- Map -->
    <map
      class="map-container"
      :latitude="latitude"
      :longitude="longitude"
      :scale="scale"
      :markers="markers"
      :show-location="true"
      @markertap="handleMarkerTap"
    />

    <!-- Stats overlay -->
    <view class="stats-overlay">
      <text class="stats-overlay__text">
        共 {{ filteredCompanies.length }} 家企业
      </text>
      <view class="stats-overlay__toggle" @tap="showList = !showList">
        <uni-icons :type="showList ? 'bottom' : 'top'" size="14" color="#2D6A4F" />
        <text class="stats-overlay__toggle-text">{{ showList ? '收起' : '列表' }}</text>
      </view>
    </view>

    <!-- Company list panel -->
    <view v-if="showList" class="list-panel">
      <scroll-view scroll-y class="list-panel__scroll">
        <view
          v-for="c in filteredCompanies"
          :key="c.companyId"
          class="company-item tap-feedback"
        >
          <view class="company-item__main" @tap="goCompany(c.companyId)">
            <text class="company-item__name">{{ c.companyName }}</text>
            <text v-if="c.address" class="company-item__address">{{ c.address }}</text>
            <view class="company-item__tags">
              <text v-if="c.supplyCount > 0" class="company-item__tag company-item__tag--supply">
                供应 {{ c.supplyCount }}
              </text>
              <text v-if="c.requirementCount > 0" class="company-item__tag company-item__tag--need">
                需求 {{ c.requirementCount }}
              </text>
            </view>
          </view>
          <view v-if="c.lat && c.lng" class="company-item__locate" @tap="locateCompany(c)">
            <uni-icons type="location" size="18" color="#2D6A4F" />
          </view>
        </view>
        <WgEmpty v-if="filteredCompanies.length === 0 && !loading" text="暂无企业数据" />
      </scroll-view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.map-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* Top bar */
.top-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 20;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  padding: $spacing-sm $spacing-md;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  background: $warm-100;
  border-radius: $radius-pill;
  padding: $spacing-sm $spacing-lg;
  margin-bottom: $spacing-xs;

  &__input {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;
    background: transparent;
  }

  &__placeholder {
    color: $text-placeholder;
    font-size: $font-md;
  }
}

.filter-pills {
  display: flex;
  gap: $spacing-xs;
}

.filter-pill {
  font-size: $font-sm;
  padding: $spacing-xs $spacing-md;
  border-radius: $radius-pill;
  background: $warm-100;
  color: $text-secondary;

  &--active {
    background: $brand-50;
    color: $brand-600;
    font-weight: 600;
  }
}

/* Map */
.map-container {
  flex: 1;
  width: 100%;
}

/* Stats overlay */
.stats-overlay {
  position: absolute;
  bottom: env(safe-area-inset-bottom);
  left: $spacing-md;
  right: $spacing-md;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  border-radius: $radius-xl;
  padding: $spacing-md $spacing-lg;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: $shadow-warm-elevated;
  z-index: 20;

  &__text {
    font-size: $font-md;
    color: $text-primary;
    font-weight: 600;
  }

  &__toggle {
    display: flex;
    align-items: center;
    gap: 4rpx;
  }

  &__toggle-text {
    font-size: $font-sm;
    color: $brand-600;
    font-weight: 500;
  }
}

/* List panel */
.list-panel {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  max-height: 50vh;
  background: #ffffff;
  border-radius: $radius-xl $radius-xl 0 0;
  box-shadow: $shadow-warm-elevated;
  z-index: 30;

  &__scroll {
    max-height: 50vh;
    padding: $spacing-md;
    padding-bottom: env(safe-area-inset-bottom);
  }
}

.company-item {
  display: flex;
  align-items: center;
  padding: $spacing-md;
  border-bottom: 1rpx solid $border-light;

  &:last-child {
    border-bottom: none;
  }

  &__main {
    flex: 1;
    min-width: 0;
  }

  &__name {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
    display: block;
    margin-bottom: 4rpx;
  }

  &__address {
    font-size: $font-sm;
    color: $text-secondary;
    display: block;
    margin-bottom: $spacing-xs;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__tags {
    display: flex;
    gap: $spacing-xs;
  }

  &__tag {
    font-size: $font-xs;
    padding: 2rpx 14rpx;
    border-radius: $radius-pill;

    &--supply {
      background: $brand-50;
      color: $brand-600;
    }

    &--need {
      background: $autumn-50;
      color: $autumn-500;
    }
  }

  &__locate {
    flex-shrink: 0;
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    background: $brand-50;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-left: $spacing-sm;

    &:active {
      background: $brand-100;
    }
  }
}
</style>
