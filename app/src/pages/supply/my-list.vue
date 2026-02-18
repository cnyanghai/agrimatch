<script setup lang="ts">
import { ref, computed } from 'vue'
import { WARM_300, WHITE } from '../../constants/colors'
import { onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { listSupplies, updateSupply, deleteSupply, type SupplyResponse } from '../../api/supply'
import { useAuthStore } from '../../store/auth'
import { formatPrice, formatRelativeTime } from '../../utils/format'

const authStore = useAuthStore()

const PAGE_SIZE = 20
const allData = ref<SupplyResponse[]>([])
const displayCount = ref(PAGE_SIZE)
const loading = ref(false)
const statusFilter = ref<number | null>(null) // null=全部, 0=发布中, 1=部分成交, 2=已下架, 3=已成交

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

/** 下架供应 */
function handleRevoke(item: SupplyResponse) {
  uni.showModal({
    title: '确认下架',
    content: `下架后「${item.categoryName}」将从大厅隐藏，可随时再次发布。`,
    confirmColor: '#E76F51',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await updateSupply(item.id, { status: 2 })
        uni.showToast({ title: '已下架', icon: 'success' })
        loadData()
      } catch {
        // handled
      }
    },
  })
}

/** 重新上架供应 */
function handleRepublish(item: SupplyResponse) {
  uni.showModal({
    title: '再次发布',
    content: `将「${item.categoryName}」重新发布到大厅，并按有效期重新计时。`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await updateSupply(item.id, { status: 0, expireMinutes: 4320 })
        uni.showToast({ title: '已重新发布', icon: 'success' })
        loadData()
      } catch {
        // handled
      }
    },
  })
}

/** 记录部分成交 */
function handleRecordDeal(item: SupplyResponse) {
  const remaining = item.remainingQuantity ?? item.quantity ?? 0
  if (remaining <= 0) {
    uni.showToast({ title: '无剩余可成交数量', icon: 'none' })
    return
  }

  // 弹出输入对话框（uni-app用prompt模拟）
  uni.showModal({
    title: '记录成交',
    content: `当前剩余 ${remaining} 吨，请输入本次成交量`,
    editable: true,
    placeholderText: '成交数量（吨）',
    success: async (res) => {
      if (!res.confirm || !res.content) return
      const qty = parseFloat(res.content)
      if (isNaN(qty) || qty <= 0) {
        uni.showToast({ title: '请输入有效数量', icon: 'none' })
        return
      }
      if (qty > remaining) {
        uni.showToast({ title: '超过剩余数量', icon: 'none' })
        return
      }
      try {
        const newRemaining = remaining - qty
        const newStatus = newRemaining <= 0 ? 3 : 1 // 3=已成交, 1=部分成交
        await updateSupply(item.id, {
          remainingQuantity: newRemaining,
          status: newStatus,
        })
        uni.showToast({ title: `成交 ${qty} 吨`, icon: 'success' })
        loadData()
      } catch {
        // handled
      }
    },
  })
}

/** 计算成交进度百分比 */
function getDealProgress(item: SupplyResponse): number {
  if (!item.quantity || item.quantity <= 0) return 0
  const remaining = item.remainingQuantity ?? item.quantity
  const dealt = item.quantity - remaining
  return Math.round((dealt / item.quantity) * 100)
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
    <WgNavBar title="我的供应" />

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
          :class="{ 'filter-bar__pill--active': statusFilter === 1 }"
          @tap="setFilter(1)"
        >部分成交</text>
        <text
          class="filter-bar__pill"
          :class="{ 'filter-bar__pill--active': statusFilter === 2 }"
          @tap="setFilter(2)"
        >已下架</text>
        <text
          class="filter-bar__pill"
          :class="{ 'filter-bar__pill--active': statusFilter === 3 }"
          @tap="setFilter(3)"
        >已成交</text>
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

          <!-- 成交进度（部分成交时显示） -->
          <view v-if="item.status === 1 && item.remainingQuantity != null" class="my-card__progress">
            <view class="my-card__progress-bar">
              <view class="my-card__progress-fill" :style="{ width: getDealProgress(item) + '%' }" />
            </view>
            <text class="my-card__progress-text">成交 {{ getDealProgress(item) }}%</text>
          </view>

          <view class="my-card__bottom">
            <text class="my-card__time">{{ formatRelativeTime(item.createTime) }}</text>
            <view class="my-card__actions">
              <!-- 编辑（非全部成交状态可编辑） -->
              <view v-if="item.status !== 3" class="my-card__btn my-card__btn--edit" @tap="goEdit(item.id)">
                <text class="my-card__btn-text">编辑</text>
              </view>
              <!-- 记录成交（发布中/部分成交可操作） -->
              <view v-if="(item.status === 0 || item.status === 1) && item.quantity" class="my-card__btn my-card__btn--deal" @tap.stop="handleRecordDeal(item)">
                <text class="my-card__btn-text my-card__btn-text--deal">成交</text>
              </view>
              <!-- 下架（发布中/部分成交可下架） -->
              <view v-if="item.status === 0 || item.status === 1" class="my-card__btn my-card__btn--revoke" @tap.stop="handleRevoke(item)">
                <text class="my-card__btn-text my-card__btn-text--revoke">下架</text>
              </view>
              <!-- 重新上架（已下架可重新发布） -->
              <view v-else-if="item.status === 2" class="my-card__btn my-card__btn--republish" @tap.stop="handleRepublish(item)">
                <text class="my-card__btn-text my-card__btn-text--republish">再发布</text>
              </view>
              <!-- 删除 -->
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
        <WgIcon name="store" :size="48" :color="WARM_300" />
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
      <WgIcon name="plus" :size="24" :color="WHITE" />
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

    &--deal {
      background: $autumn-50;
    }

    &--delete {
      background: rgba(231, 111, 81, 0.08);
    }

    &--revoke {
      background: rgba(231, 111, 81, 0.08);
    }

    &--republish {
      background: $brand-50;
    }
  }

  &__btn-text {
    font-size: $font-xs;
    color: $brand-600;
    font-weight: 600;

    &--deal {
      color: $autumn-500;
      font-weight: 700;
    }

    &--delete {
      color: $accent-400;
    }

    &--revoke {
      color: $accent-400;
    }

    &--republish {
      color: $brand-600;
    }
  }

  /* 成交进度条 */
  &__progress {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    margin-bottom: $spacing-sm;
  }

  &__progress-bar {
    flex: 1;
    height: 8rpx;
    background: $warm-100;
    border-radius: 4rpx;
    overflow: hidden;
  }

  &__progress-fill {
    height: 100%;
    background: $brand-600;
    border-radius: 4rpx;
    transition: width 0.3s;
  }

  &__progress-text {
    font-size: $font-xs;
    color: $brand-600;
    font-weight: 600;
    flex-shrink: 0;
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
    color: $text-inverse;
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
