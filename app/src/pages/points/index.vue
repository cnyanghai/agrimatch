<script setup lang="ts">
import { ref, computed } from 'vue'
import { BRAND_600, ACCENT_400 } from '../../constants/colors'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '../../store/auth'
import {
  getPointsMe,
  listPointsTx,
  rechargePoints,
  redeemPoints,
  type PointsMeResponse,
  type PointsTxResponse,
} from '../../api/points'
import { formatRelativeTime } from '../../utils/format'

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)

const balanceInfo = ref<PointsMeResponse | null>(null)
const txList = ref<PointsTxResponse[]>([])
const loading = ref(false)
const refreshing = ref(false)

onShow(() => {
  if (isLoggedIn.value) {
    loadData()
  }
})

async function loadData() {
  loading.value = true
  try {
    const [meRes, txRes] = await Promise.allSettled([
      getPointsMe(),
      listPointsTx(),
    ])
    if (meRes.status === 'fulfilled') balanceInfo.value = meRes.value
    if (txRes.status === 'fulfilled') txList.value = txRes.value || []
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

async function handleRefresh() {
  refreshing.value = true
  await loadData()
}

function goLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}

/** Format points balance with comma separators */
function formatPoints(val?: number): string {
  if (val == null) return '0'
  return val.toLocaleString()
}

/** Format CNY balance to 2 decimal places */
function formatCny(val?: number): string {
  if (val == null) return '0.00'
  return val.toFixed(2)
}

/** Format pointsDelta with sign prefix */
function formatDelta(val: number): string {
  if (val > 0) return `+${val}`
  return `${val}`
}

/** Format cnyDelta with sign prefix and currency */
function formatCnyDelta(val: number): string {
  if (val > 0) return `+¥${val.toFixed(2)}`
  if (val < 0) return `-¥${Math.abs(val).toFixed(2)}`
  return '¥0.00'
}

/** Map txType to a readable label */
function getTxTypeLabel(txType: string): string {
  const map: Record<string, string> = {
    RECHARGE: '充值',
    REDEEM: '兑换',
    GIFT_OUT: '赠送',
    GIFT_IN: '收到赠送',
    REWARD: '奖励',
    CONSUME: '消费',
    REFUND: '退还',
    JD_REDEEM: '京东卡兑换',
    ADMIN: '管理员操作',
  }
  return map[txType] || txType
}

/** Recharge flow */
function handleRecharge() {
  // uni.showModal editorClue: true enables input
  uni.showModal({
    title: '积分充值',
    content: '',
    editable: true,
    placeholderText: '请输入充值积分数量',
    success: async (res) => {
      if (!res.confirm || !res.content) return
      const points = parseInt(res.content, 10)
      if (isNaN(points) || points <= 0) {
        uni.showToast({ title: '请输入有效的积分数量', icon: 'none' })
        return
      }
      try {
        uni.showLoading({ title: '充值中...' })
        const result = await rechargePoints(points)
        balanceInfo.value = result
        uni.hideLoading()
        uni.showToast({ title: '充值成功', icon: 'success' })
        // Reload transaction list
        const txRes = await listPointsTx()
        txList.value = txRes || []
      } catch (e: any) {
        uni.hideLoading()
        console.error('Recharge failed:', e)
      }
    },
  })
}

/** Redeem flow */
function handleRedeem() {
  if (!balanceInfo.value || balanceInfo.value.pointsBalance <= 0) {
    uni.showToast({ title: '积分余额不足', icon: 'none' })
    return
  }
  uni.showModal({
    title: '积分兑换',
    content: '',
    editable: true,
    placeholderText: `可兑换积分（余额${balanceInfo.value.pointsBalance}）`,
    success: async (res) => {
      if (!res.confirm || !res.content) return
      const points = parseInt(res.content, 10)
      if (isNaN(points) || points <= 0) {
        uni.showToast({ title: '请输入有效的积分数量', icon: 'none' })
        return
      }
      if (balanceInfo.value && points > balanceInfo.value.pointsBalance) {
        uni.showToast({ title: '积分余额不足', icon: 'none' })
        return
      }
      try {
        uni.showLoading({ title: '兑换中...' })
        const result = await redeemPoints(points)
        balanceInfo.value = result
        uni.hideLoading()
        uni.showToast({ title: '兑换成功', icon: 'success' })
        // Reload transaction list
        const txRes = await listPointsTx()
        txList.value = txRes || []
      } catch (e: any) {
        uni.hideLoading()
        console.error('Redeem failed:', e)
      }
    },
  })
}
</script>

<template>
  <view class="points-page">
    <!-- Not logged in state -->
    <view v-if="!isLoggedIn" class="points-page__login-prompt">
      <view class="login-prompt__card">
        <view class="login-prompt__icon"><WgIcon name="lock" :size="48" :color="BRAND_600" /></view>
        <text class="login-prompt__title">请先登录</text>
        <text class="login-prompt__desc">登录后查看积分信息</text>
        <view class="login-prompt__btn" @tap="goLogin">
          <text class="login-prompt__btn-text">去登录</text>
        </view>
      </view>
    </view>

    <!-- Logged in content -->
    <view v-else>
      <!-- Balance card -->
      <view class="balance-card">
        <view class="balance-card__header">
          <text class="balance-card__title">我的积分</text>
        </view>
        <view class="balance-card__body">
          <view class="balance-card__main">
            <text class="balance-card__points">{{ formatPoints(balanceInfo?.pointsBalance) }}</text>
            <text class="balance-card__points-label">积分余额</text>
          </view>
          <view class="balance-card__divider"></view>
          <view class="balance-card__sub">
            <text class="balance-card__cny">¥{{ formatCny(balanceInfo?.cnyBalance) }}</text>
            <text class="balance-card__cny-label">约合人民币</text>
          </view>
        </view>
        <view class="balance-card__hint">
          <text class="balance-card__hint-text">1 积分 = 0.01 元</text>
        </view>
      </view>

      <!-- Action buttons -->
      <view class="action-bar">
        <view class="action-bar__btn action-bar__btn--recharge" @tap="handleRecharge">
          <WgIcon name="plus" :size="20" :color="BRAND_600" />
          <text class="action-bar__btn-text">充值</text>
        </view>
        <view class="action-bar__btn action-bar__btn--redeem" @tap="handleRedeem">
          <WgIcon name="refresh-cw" :size="20" color="#D4A373" />
          <text class="action-bar__btn-text">兑换</text>
        </view>
      </view>

      <!-- Transaction history -->
      <view class="tx-section">
        <view class="tx-section__header">
          <text class="tx-section__title">交易记录</text>
          <text class="tx-section__refresh" @tap="handleRefresh">刷新</text>
        </view>

        <!-- Loading state -->
        <WgSkeleton v-if="loading && txList.length === 0" type="list" :rows="3" />

        <!-- Empty state -->
        <WgEmpty v-else-if="txList.length === 0" text="暂无交易记录" />

        <!-- Transaction list -->
        <view v-else class="tx-list">
          <view
            v-for="tx in txList"
            :key="tx.id"
            class="tx-item"
          >
            <view class="tx-item__left">
              <view class="tx-item__type-badge" :class="tx.pointsDelta >= 0 ? 'tx-item__type-badge--in' : 'tx-item__type-badge--out'">
                <WgIcon v-if="tx.pointsDelta >= 0" name="chevron-up" :size="16" color="#16a34a" />
                <WgIcon v-else name="chevron-down" :size="16" :color="ACCENT_400" />
              </view>
              <view class="tx-item__info">
                <text class="tx-item__type">{{ getTxTypeLabel(tx.txType) }}</text>
                <text v-if="tx.remark" class="tx-item__remark">{{ tx.remark }}</text>
                <text class="tx-item__time">{{ formatRelativeTime(tx.createTime) }}</text>
              </view>
            </view>
            <view class="tx-item__right">
              <text
                class="tx-item__delta"
                :class="tx.pointsDelta > 0 ? 'tx-item__delta--positive' : tx.pointsDelta < 0 ? 'tx-item__delta--negative' : ''"
              >
                {{ formatDelta(tx.pointsDelta) }}
              </text>
              <text class="tx-item__cny-delta">{{ formatCnyDelta(tx.cnyDelta) }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Bottom safe area -->
      <view style="height: 40rpx;"></view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.points-page {
  min-height: 100vh;
  background: $bg-page;
}

/* ===== Login Prompt ===== */
.points-page__login-prompt {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-lg;
}

.login-prompt__card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-xl $spacing-lg;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 600rpx;
}

.login-prompt__icon {
  font-size: 80rpx;
  margin-bottom: $spacing-md;
}

.login-prompt__title {
  font-size: $font-xl;
  font-weight: bold;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.login-prompt__desc {
  font-size: $font-sm;
  color: $text-secondary;
  margin-bottom: $spacing-lg;
}

.login-prompt__btn {
  background: $brand-600;
  border-radius: $radius-lg;
  padding: $spacing-sm $spacing-xl;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 80rpx;
}

.login-prompt__btn-text {
  color: $text-inverse;
  font-size: $font-md;
  font-weight: bold;
}

/* ===== Balance Card ===== */
.balance-card {
  background: linear-gradient(135deg, $brand-700, $brand-600);
  padding: $spacing-lg;
  padding-top: calc(var(--status-bar-height, 25px) + 40rpx);

  &__header {
    margin-bottom: $spacing-md;
  }

  &__title {
    color: rgba(255, 255, 255, 0.8);
    font-size: $font-md;
  }

  &__body {
    display: flex;
    align-items: center;
    padding: $spacing-md 0;
  }

  &__main {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  &__points {
    color: $text-inverse;
    font-size: 64rpx;
    font-weight: bold;
    line-height: 1.2;
  }

  &__points-label {
    color: rgba(255, 255, 255, 0.7);
    font-size: $font-sm;
    margin-top: $spacing-xs;
  }

  &__divider {
    width: 1rpx;
    height: 80rpx;
    background: rgba(255, 255, 255, 0.2);
    margin: 0 $spacing-lg;
  }

  &__sub {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  &__cny {
    color: $text-inverse;
    font-size: $font-2xl;
    font-weight: bold;
    line-height: 1.2;
  }

  &__cny-label {
    color: rgba(255, 255, 255, 0.7);
    font-size: $font-sm;
    margin-top: $spacing-xs;
  }

  &__hint {
    text-align: center;
    margin-top: $spacing-sm;
    padding-top: $spacing-sm;
    border-top: 1rpx solid rgba(255, 255, 255, 0.15);
  }

  &__hint-text {
    color: rgba(255, 255, 255, 0.5);
    font-size: $font-xs;
  }
}

/* ===== Action Bar ===== */
.action-bar {
  display: flex;
  gap: $spacing-md;
  padding: $spacing-md $spacing-md 0;
  margin-top: -24rpx;
  position: relative;
  z-index: 1;

  &__btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-xs;
    height: 96rpx;
    border-radius: $radius-lg;
    background: $bg-card;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

    &:active {
      transform: scale(0.97);
    }

    &--recharge {
      border: 2rpx solid $brand-100;
    }

    &--redeem {
      border: 2rpx solid rgba(212, 163, 115, 0.3);
    }
  }

  &__btn-text {
    font-size: $font-md;
    font-weight: bold;
  }

  &__btn--recharge &__btn-text {
    color: $brand-600;
  }

  &__btn--redeem &__btn-text {
    color: $autumn-400;
  }
}

/* ===== Transaction Section ===== */
.tx-section {
  background: $bg-card;
  margin: $spacing-md;
  border-radius: $radius-lg;
  overflow: hidden;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: $spacing-md;
    border-bottom: 1rpx solid $border-light;
  }

  &__title {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;
  }

  &__refresh {
    font-size: $font-sm;
    color: $brand-600;
  }

}

/* ===== Transaction List ===== */
.tx-list {
  padding: 0 $spacing-md;
}

.tx-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-light;

  &:last-child {
    border-bottom: none;
  }

  &__left {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    flex: 1;
    min-width: 0;
  }

  &__type-badge {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &--in {
      background: rgba(45, 106, 79, 0.1);
    }

    &--out {
      background: rgba(231, 111, 81, 0.1);
    }
  }

  &__info {
    display: flex;
    flex-direction: column;
    gap: 4rpx;
    min-width: 0;
  }

  &__type {
    font-size: $font-md;
    color: $text-primary;
    font-weight: 500;
  }

  &__remark {
    font-size: $font-xs;
    color: $text-secondary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 320rpx;
  }

  &__time {
    font-size: $font-xs;
    color: $text-placeholder;
  }

  &__right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 4rpx;
    flex-shrink: 0;
    margin-left: $spacing-sm;
  }

  &__delta {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;

    &--positive {
      color: $color-success;
    }

    &--negative {
      color: $color-error;
    }
  }

  &__cny-delta {
    font-size: $font-xs;
    color: $text-secondary;
  }
}
</style>
