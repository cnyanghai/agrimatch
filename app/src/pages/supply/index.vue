<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { listSupplies, type SupplyResponse } from '../../api/supply'
import { formatPrice, formatRelativeTime } from '../../utils/format'

const PAGE_SIZE = 20

const allData = ref<SupplyResponse[]>([])
const displayCount = ref(PAGE_SIZE)
const loading = ref(false)
const keyword = ref('')
const sortMode = ref<'newest' | 'priceDesc' | 'priceAsc'>('newest')

const filteredList = computed(() => {
  let list = [...allData.value]
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    list = list.filter(item =>
      item.categoryName.toLowerCase().includes(kw) ||
      (item.companyName || '').toLowerCase().includes(kw) ||
      (item.origin || '').toLowerCase().includes(kw)
    )
  }
  if (sortMode.value === 'priceDesc') {
    list.sort((a, b) => (b.exFactoryPrice || 0) - (a.exFactoryPrice || 0))
  } else if (sortMode.value === 'priceAsc') {
    list.sort((a, b) => (a.exFactoryPrice || 0) - (b.exFactoryPrice || 0))
  }
  // default 'newest': API returns in create_time desc order
  return list
})

const displayList = computed(() => filteredList.value.slice(0, displayCount.value))

const loadStatus = computed(() => {
  if (loading.value) return 'loading'
  if (displayCount.value >= filteredList.value.length) return 'noMore'
  return 'more'
})

watch([keyword, sortMode], () => {
  displayCount.value = PAGE_SIZE
})

onMounted(() => {
  loadData()
})

onPullDownRefresh(() => {
  loadData().finally(() => {
    uni.stopPullDownRefresh()
  })
})

onReachBottom(() => {
  loadMore()
})

async function loadData() {
  loading.value = true
  try {
    const res = await listSupplies({ activeOnly: true, orderBy: 'create_time', order: 'desc' })
    allData.value = res || []
    displayCount.value = PAGE_SIZE
  } catch {
    // handled by request.ts
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (displayCount.value < filteredList.value.length) {
    displayCount.value += PAGE_SIZE
  }
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/supply/detail?id=${id}` })
}

function goPublish() {
  uni.navigateTo({ url: '/pages/supply/publish' })
}
</script>

<template>
  <view class="supply-page">
    <!-- Sticky filter bar -->
    <view class="filter-bar">
      <view class="filter-bar__search">
        <uni-icons type="search" size="16" color="#9ca3af" />
        <input
          v-model="keyword"
          class="filter-bar__input"
          placeholder="搜索商品/企业/产地"
          placeholder-class="filter-bar__placeholder"
          confirm-type="search"
        />
      </view>
      <view class="filter-bar__pills">
        <text
          class="filter-bar__pill"
          :class="{ 'filter-bar__pill--active': sortMode === 'newest' }"
          @tap="sortMode = 'newest'"
        >最新</text>
        <text
          class="filter-bar__pill"
          :class="{ 'filter-bar__pill--active': sortMode === 'priceDesc' }"
          @tap="sortMode = 'priceDesc'"
        >价高</text>
        <text
          class="filter-bar__pill"
          :class="{ 'filter-bar__pill--active': sortMode === 'priceAsc' }"
          @tap="sortMode = 'priceAsc'"
        >价低</text>
      </view>
    </view>

    <!-- List -->
    <view v-if="displayList.length > 0" class="list">
      <view
        v-for="item in displayList"
        :key="item.id"
        class="supply-card tap-feedback"
        @tap="goDetail(item.id)"
      >
        <view class="supply-card__accent" />
        <view class="supply-card__content">
          <view class="supply-card__top">
            <view class="supply-card__title-row">
              <text class="supply-card__name">{{ item.categoryName }}</text>
              <text v-if="item.origin" class="supply-card__badge supply-card__badge--brand">{{ item.origin }}</text>
            </view>
            <text class="supply-card__price">{{ formatPrice(item.exFactoryPrice) }}</text>
          </view>
          <view class="supply-card__company-row">
            <uni-icons type="shop" size="14" color="#9ca3af" />
            <text class="supply-card__company">{{ item.companyName || item.nickName || item.userName }}</text>
          </view>
          <view class="supply-card__tags">
            <text v-if="item.quantity" class="supply-card__tag">{{ item.quantity }}吨</text>
            <text v-if="item.deliveryMode" class="supply-card__tag">{{ item.deliveryMode }}</text>
            <text v-if="item.paymentMethod" class="supply-card__tag">{{ item.paymentMethod }}</text>
          </view>
          <view class="supply-card__bottom">
            <view class="supply-card__meta">
              <text class="supply-card__time">{{ formatRelativeTime(item.createTime) }}</text>
              <view v-if="item.shipAddress" class="supply-card__location">
                <uni-icons type="location" size="12" color="#9ca3af" />
                <text class="supply-card__address">{{ item.shipAddress }}</text>
              </view>
            </view>
            <view class="supply-card__action" @tap.stop="goDetail(item.id)">
              <text class="supply-card__action-text">咨询</text>
            </view>
          </view>
        </view>
      </view>
      <WgLoadMore :status="loadStatus" @loadMore="loadMore" />
    </view>

    <!-- Empty -->
    <WgEmpty v-else-if="!loading" text="暂无供应信息" description="目前还没有供应信息发布" />

    <!-- Loading (initial only) -->
    <WgSkeleton v-if="loading && allData.length === 0" type="card" :rows="3" />

    <!-- FAB -->
    <view class="fab anim-fab-enter" @tap="goPublish">
      <uni-icons type="plusempty" size="28" color="#fff" />
    </view>

    <WgTabBar :current="1" />
  </view>
</template>

<style lang="scss" scoped>
.supply-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: 130rpx;
}

/* ===== Filter Bar ===== */
.filter-bar {
  position: sticky;
  top: 0;
  z-index: 10;
  background: $bg-card;
  padding: $spacing-sm $spacing-sm 0;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);

  &__search {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    background: $bg-page;
    border-radius: $radius-sm;
    padding: $spacing-xs $spacing-sm;
    margin-bottom: $spacing-sm;
  }

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

  &__pills {
    display: flex;
    gap: $spacing-xs;
    padding: $spacing-xs 0 $spacing-sm;
  }

  &__pill {
    font-size: $font-sm;
    color: $text-secondary;
    padding: $spacing-xs $spacing-md;
    border-radius: 30rpx;
    background: $bg-page;
    transition: all 0.2s;

    &--active {
      color: $brand-600;
      background: $brand-50;
      font-weight: 600;
    }
  }
}

/* ===== List ===== */
.list {
  padding: $spacing-sm;
}

/* ===== Supply Card ===== */
.supply-card {
  display: flex;
  background: $bg-card;
  border-radius: $radius-lg;
  margin-bottom: $spacing-sm;
  overflow: hidden;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.04);
  transition: transform 0.15s;

  &:active {
    transform: scale(0.98);
  }

  &__accent {
    width: 6rpx;
    flex-shrink: 0;
    background: $brand-600;
    border-radius: $radius-lg 0 0 $radius-lg;
  }

  &__content {
    flex: 1;
    padding: $spacing-md;
    min-width: 0;
  }

  &__top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: $spacing-xs;
  }

  &__title-row {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    flex: 1;
    min-width: 0;
  }

  &__name {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__badge {
    font-size: $font-xs;
    padding: 2rpx 12rpx;
    border-radius: $radius-sm;
    flex-shrink: 0;
    white-space: nowrap;

    &--brand {
      color: $brand-600;
      background: $brand-50;
    }
  }

  &__price {
    font-size: $font-xl;
    font-weight: bold;
    color: $accent-400;
    flex-shrink: 0;
    margin-left: $spacing-sm;
  }

  &__company-row {
    display: flex;
    align-items: center;
    gap: 6rpx;
    margin-bottom: $spacing-xs;
  }

  &__company {
    font-size: $font-sm;
    color: $text-secondary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
    margin-bottom: $spacing-xs;
  }

  &__tag {
    font-size: $font-xs;
    color: $brand-600;
    background: $brand-50;
    padding: 4rpx 14rpx;
    border-radius: $radius-sm;
  }

  &__bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: $spacing-xs;
    border-top: 1rpx solid $border-light;
  }

  &__meta {
    flex: 1;
    min-width: 0;
  }

  &__time {
    font-size: $font-xs;
    color: $text-placeholder;
    display: block;
  }

  &__location {
    display: flex;
    align-items: center;
    gap: 4rpx;
    max-width: 300rpx;
    margin-top: 4rpx;
  }

  &__address {
    font-size: $font-xs;
    color: $text-placeholder;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__action {
    flex-shrink: 0;
    padding: $spacing-xs $spacing-md;
    background: $brand-50;
    border-radius: 30rpx;
    margin-left: $spacing-sm;
  }

  &__action-text {
    font-size: $font-xs;
    color: $brand-600;
    font-weight: 600;
  }
}

/* ===== FAB ===== */
.fab {
  position: fixed;
  right: 32rpx;
  bottom: 180rpx;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: $brand-600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(45, 106, 79, 0.3);
  transition: transform 0.15s;
  z-index: 20;

  &:active {
    transform: scale(0.92);
  }
}
</style>
