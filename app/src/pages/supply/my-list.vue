<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { listSupplies, deleteSupply, type SupplyResponse } from '../../api/supply'
import { useAuthStore } from '../../store/auth'
import { formatPrice, formatRelativeTime } from '../../utils/format'

const authStore = useAuthStore()

const PAGE_SIZE = 20
const allData = ref<SupplyResponse[]>([])
const displayCount = ref(PAGE_SIZE)
const loading = ref(false)
const statusFilter = ref<number | null>(null) // null=全部, 0=发布中, 2=已下架

const filteredList = computed(() => {
  if (statusFilter.value === null) return allData.value
  return allData.value.filter(item => item.status === statusFilter.value)
})

const displayList = computed(() => filteredList.value.slice(0, displayCount.value))

const loadStatus = computed(() => {
  if (loading.value) return 'loading'
  if (displayCount.value >= filteredList.value.length) return 'noMore'
  return 'more'
})

const statusLabel = (status?: number) => {
  switch (status) {
    case 0: return '发布中'
    case 1: return '部分成交'
    case 2: return '已下架'
    case 3: return '已成交'
    default: return '未知'
  }
}

const statusClass = (status?: number) => {
  switch (status) {
    case 0: return 'status--active'
    case 1: return 'status--partial'
    case 2: return 'status--off'
    case 3: return 'status--done'
    default: return ''
  }
}

onShow(() => {
  loadData()
})

onPullDownRefresh(() => {
  loadData().finally(() => uni.stopPullDownRefresh())
})

onReachBottom(() => {
  if (displayCount.value < filteredList.value.length) {
    displayCount.value += PAGE_SIZE
  }
})

async function loadData() {
  if (!authStore.isLoggedIn || !authStore.user?.userId) return
  loading.value = true
  try {
    const res = await listSupplies({
      userId: authStore.user.userId,
      includeExpired: true,
      orderBy: 'create_time',
      order: 'desc',
    })
    allData.value = res || []
    displayCount.value = PAGE_SIZE
  } catch {
    // handled by request.ts
  } finally {
    loading.value = false
  }
}

function setFilter(val: number | null) {
  statusFilter.value = val
  displayCount.value = PAGE_SIZE
}

function goEdit(id: number) {
  uni.navigateTo({ url: `/pages/supply/edit?id=${id}` })
}

function goPublish() {
  uni.navigateTo({ url: '/pages/supply/publish' })
}

function handleDelete(item: SupplyResponse) {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除「${item.categoryName}」吗？此操作不可撤销。`,
    confirmColor: '#E76F51',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await deleteSupply(item.id)
        uni.showToast({ title: '已删除', icon: 'success' })
        allData.value = allData.value.filter(d => d.id !== item.id)
      } catch {
        // handled
      }
    },
  })
}
</script>

<template>
  <view class="my-list-page">
    <!-- 状态筛选 -->
    <view class="filter-bar">
      <view class="filter-bar__pills">
        <text
          class="filter-bar__pill"
          :class="{ 'filter-bar__pill--active': statusFilter === null }"
          @tap="setFilter(null)"
        >全部</text>
        <text
          class="filter-bar__pill"
          :class="{ 'filter-bar__pill--active': statusFilter === 0 }"
          @tap="setFilter(0)"
        >发布中</text>
        <text
          class="filter-bar__pill"
          :class="{ 'filter-bar__pill--active': statusFilter === 2 }"
          @tap="setFilter(2)"
        >已下架</text>
      </view>
    </view>

    <!-- 列表 -->
    <view v-if="displayList.length > 0" class="list">
      <view
        v-for="item in displayList"
        :key="item.id"
        class="my-card"
      >
        <view class="my-card__accent" />
        <view class="my-card__content">
          <view class="my-card__top">
            <view class="my-card__title-row">
              <text class="my-card__name">{{ item.categoryName }}</text>
              <text class="my-card__status" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</text>
            </view>
            <text class="my-card__price">{{ formatPrice(item.exFactoryPrice) }}</text>
          </view>

          <view class="my-card__info">
            <text v-if="item.quantity" class="my-card__tag">{{ item.quantity }}吨</text>
            <text v-if="item.origin" class="my-card__tag">{{ item.origin }}</text>
            <text v-if="item.deliveryMode" class="my-card__tag">{{ item.deliveryMode }}</text>
          </view>

          <view class="my-card__bottom">
            <text class="my-card__time">{{ formatRelativeTime(item.createTime) }}</text>
            <view class="my-card__actions">
              <view class="my-card__btn my-card__btn--edit" @tap="goEdit(item.id)">
                <text class="my-card__btn-text">编辑</text>
              </view>
              <view class="my-card__btn my-card__btn--delete" @tap="handleDelete(item)">
                <text class="my-card__btn-text my-card__btn-text--delete">删除</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      <WgLoadMore :status="loadStatus" />
    </view>

    <!-- 空状态 -->
    <view v-else-if="!loading" class="empty">
      <view class="empty__icon">
        <uni-icons type="shop" size="48" color="#d1d5db" />
      </view>
      <text class="empty__text">还没有发布供应</text>
      <view class="empty__btn" @tap="goPublish">
        <text class="empty__btn-text">去发布</text>
      </view>
    </view>

    <!-- 骨架屏 -->
    <WgSkeleton v-if="loading && allData.length === 0" type="card" :rows="3" />

    <!-- FAB -->
    <view class="fab" @tap="goPublish">
      <uni-icons type="plusempty" size="28" color="#fff" />
    </view>
  </view>
</template>

<style lang="scss" scoped>
.my-list-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: 40rpx;
}

/* ===== Filter Bar ===== */
.filter-bar {
  position: sticky;
  top: 0;
  z-index: 10;
  background: $bg-card;
  padding: $spacing-sm $spacing-md;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);

  &__pills {
    display: flex;
    gap: $spacing-xs;
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

/* ===== Card ===== */
.my-card {
  display: flex;
  background: $bg-card;
  border-radius: $radius-lg;
  margin-bottom: $spacing-sm;
  overflow: hidden;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.04);

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

  &__status {
    font-size: $font-xs;
    padding: 2rpx 14rpx;
    border-radius: 20rpx;
    flex-shrink: 0;
    font-weight: 600;
  }

  &__price {
    font-size: $font-xl;
    font-weight: bold;
    color: $accent-400;
    flex-shrink: 0;
    margin-left: $spacing-sm;
  }

  &__info {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
    margin-bottom: $spacing-sm;
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

  &__time {
    font-size: $font-xs;
    color: $text-placeholder;
  }

  &__actions {
    display: flex;
    gap: $spacing-xs;
  }

  &__btn {
    padding: $spacing-xs $spacing-md;
    border-radius: 30rpx;
    transition: transform 0.15s;

    &:active {
      transform: scale(0.95);
    }

    &--edit {
      background: $brand-50;
    }

    &--delete {
      background: rgba(231, 111, 81, 0.08);
    }
  }

  &__btn-text {
    font-size: $font-xs;
    color: $brand-600;
    font-weight: 600;

    &--delete {
      color: $accent-400;
    }
  }
}

/* ===== Status Colors ===== */
.status {
  &--active {
    color: $brand-600;
    background: $brand-50;
  }

  &--partial {
    color: $autumn-500;
    background: $autumn-50;
  }

  &--off {
    color: $text-placeholder;
    background: $bg-page;
  }

  &--done {
    color: $action-600;
    background: rgba(37, 99, 235, 0.08);
  }
}

/* ===== Empty ===== */
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx $spacing-lg;

  &__icon {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background: $bg-page;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: $spacing-md;
  }

  &__text {
    font-size: $font-md;
    color: $text-secondary;
    margin-bottom: $spacing-lg;
  }

  &__btn {
    padding: $spacing-sm $spacing-xl;
    background: $brand-600;
    border-radius: 30rpx;
    transition: transform 0.15s;

    &:active {
      transform: scale(0.95);
    }
  }

  &__btn-text {
    font-size: $font-md;
    color: #fff;
    font-weight: 600;
  }
}

/* ===== FAB ===== */
.fab {
  position: fixed;
  right: 32rpx;
  bottom: 60rpx;
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
