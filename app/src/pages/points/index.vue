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
  redeemJdCard,
  listMyJdRedeems,
  type PointsMeResponse,
  type PointsTxResponse,
  type JdRedeemDetailResponse,
} from '../../api/points'
import { formatRelativeTime } from '../../utils/format'

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)

const balanceInfo = ref<PointsMeResponse | null>(null)
const txList = ref<PointsTxResponse[]>([])
const jdRedeems = ref<JdRedeemDetailResponse[]>([])
const loading = ref(false)
const refreshing = ref(false)

const jdFaces = [500, 1000, 2000, 5000]
const selectedJdFace = ref(1000)
const jdRedeeming = ref(false)
const jdPointsCost = computed(() => Math.ceil(selectedJdFace.value * 10 / 8))
const canRedeemJd = computed(() =>
  selectedJdFace.value > 0 && (balanceInfo.value?.pointsBalance ?? 0) >= jdPointsCost.value
)

onShow(() => {
  if (isLoggedIn.value) {
    loadData()
  }
})

async function loadData() {
  loading.value = true
  try {
    const [meRes, txRes, jdRes] = await Promise.allSettled([
      getPointsMe(),
      listPointsTx(),
      listMyJdRedeems(),
    ])
    if (meRes.status === 'fulfilled') balanceInfo.value = meRes.value
    if (txRes.status === 'fulfilled') txList.value = txRes.value || []
    if (jdRes.status === 'fulfilled') jdRedeems.value = jdRes.value || []
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

/** JD card redeem */
async function handleJdRedeem() {
  if (!canRedeemJd.value) {
    uni.showToast({ title: '积分余额不足', icon: 'none' }); return
  }
  uni.showModal({
    title: '京东卡兑换',
    content: `确认兑换 ¥${selectedJdFace.value} 京东卡？\n消耗 ${jdPointsCost.value} 积分`,
    success: async (res) => {
      if (!res.confirm) return
      jdRedeeming.value = true
      try {
        uni.showLoading({ title: '兑换中...' })
        await redeemJdCard(selectedJdFace.value)
        uni.hideLoading()
        uni.showToast({ title: '兑换申请已提交', icon: 'success' })
        await loadData()
      } catch (e: any) {
        uni.hideLoading()
      } finally {
        jdRedeeming.value = false
      }
    }
  })
}

function jdStatusLabel(status: number): string {
  const map: Record<number, string> = { 0: '待处理', 1: '已完成', 2: '已拒绝' }
  return map[status] ?? '未知'
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
    <WgNavBar title="积分中心" />

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
      <view class="balance-card stitch-hero">
        <view class="balance-card__header">
          <text class="balance-card__title">我的积分</text>
        </view>
        <view class="balance-card__body">
          <view class="balance-card__main">
            <text class="balance-card__points font-mono">{{ formatPoints(balanceInfo?.pointsBalance) }}</text>
            <text class="balance-card__points-label">积分余额</text>
          </view>
          <view class="balance-card__divider"></view>
          <view class="balance-card__sub">
            <text class="balance-card__cny font-mono">¥{{ formatCny(balanceInfo?.cnyBalance) }}</text>
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

      <!-- JD Card Redeem Section -->
      <view class="jd-section stitch-card">
        <view class="jd-section__header">
          <text class="jd-section__title">京东卡兑换</text>
          <text class="jd-section__ratio">兑换比例：10积分 = ¥8</text>
        </view>

        <view class="jd-faces">
          <view
            v-for="face in jdFaces"
            :key="face"
            class="jd-face-item"
            :class="{ 'jd-face-item--active': selectedJdFace === face }"
            @tap="selectedJdFace = face"
          >
            <text class="jd-face-item__value">¥{{ face }}</text>
          </view>
        </view>

        <view class="jd-cost-info">
          <text class="jd-cost-info__text">需消耗 <text class="jd-cost-info__num">{{ jdPointsCost }}</text> 积分</text>
        </view>

        <button
          class="jd-redeem-btn"
          :class="{ 'jd-redeem-btn--disabled': !canRedeemJd }"
          :disabled="!canRedeemJd || jdRedeeming"
          @tap="handleJdRedeem"
        >
          {{ jdRedeeming ? '兑换中...' : '兑换京东卡' }}
        </button>

        <!-- Recent JD redeems -->
        <view v-if="jdRedeems.length" class="jd-history">
          <text class="jd-history__title">兑换记录</text>
          <view v-for="r in jdRedeems" :key="r.id" class="jd-history-item">
            <view class="jd-history-item__left">
              <text class="jd-history-item__face">¥{{ r.faceValue }}</text>
              <text class="jd-history-item__time">{{ formatRelativeTime(r.createTime) }}</text>
            </view>
            <view class="jd-history-item__right">
              <text v-if="r.cardCode" class="jd-history-item__code">{{ r.cardCode }}</text>
              <text
                class="jd-history-item__status"
                :class="{
                  'jd-history-item__status--done': r.status === 1,
                  'jd-history-item__status--reject': r.status === 2,
                }"
              >{{ jdStatusLabel(r.status) }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Transaction history -->
      <view class="tx-section stitch-card">
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

/* ===== JD Card Redeem ===== */
.jd-section {
  margin: $spacing-md;
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-lg;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-md;
  }

  &__title {
    font-size: $font-md;
    font-weight: 700;
    color: $text-primary;
  }

  &__ratio {
    font-size: $font-xs;
    color: $text-secondary;
  }
}

.jd-faces {
  display: flex;
  gap: $spacing-sm;
  flex-wrap: wrap;
}

.jd-face-item {
  flex: 1;
  min-width: 140rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid $border-color;
  border-radius: $radius-md;
  background: $bg-page;
  transition: all 0.2s;

  &--active {
    border-color: $color-brand;
    background: rgba($color-brand, 0.06);
  }

  &__value {
    font-size: $font-md;
    font-weight: 700;
    color: $text-primary;
  }
}

.jd-cost-info {
  margin-top: $spacing-md;
  text-align: center;

  &__text {
    font-size: $font-sm;
    color: $text-secondary;
  }

  &__num {
    color: $color-brand;
    font-weight: 700;
  }
}

.jd-redeem-btn {
  margin-top: $spacing-md;
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: #E53935;
  color: white;
  border-radius: $radius-lg;
  font-size: $font-md;
  font-weight: 700;

  &--disabled {
    background: $warm-200;
    color: $text-placeholder;
  }
}

.jd-history {
  margin-top: $spacing-lg;
  padding-top: $spacing-md;
  border-top: 1rpx solid $border-light;

  &__title {
    font-size: $font-sm;
    font-weight: 600;
    color: $text-secondary;
    margin-bottom: $spacing-sm;
  }
}

.jd-history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-sm 0;
  border-bottom: 1rpx solid $border-light;

  &:last-child { border-bottom: none; }

  &__left {
    display: flex;
    flex-direction: column;
    gap: 4rpx;
  }

  &__face {
    font-size: $font-md;
    font-weight: 700;
    color: $text-primary;
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
  }

  &__code {
    font-size: $font-xs;
    color: $text-secondary;
    font-family: $font-mono;
  }

  &__status {
    font-size: $font-xs;
    color: $text-placeholder;
    padding: 2rpx 12rpx;
    border-radius: $radius-sm;
    background: $warm-100;

    &--done {
      color: $color-success;
      background: rgba(#16a34a, 0.08);
    }

    &--reject {
      color: $color-error;
      background: rgba(#dc2626, 0.08);
    }
  }
}
</style>
