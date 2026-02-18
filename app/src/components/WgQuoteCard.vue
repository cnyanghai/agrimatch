<script setup lang="ts">
import { computed } from 'vue'
import type { ChatMessageResponse } from '../api/chat'
import { WARM_500, BRAND_600 } from '../constants/colors'

const props = defineProps<{
  message: ChatMessageResponse
  isMine: boolean
  prevMessage?: ChatMessageResponse | null
  round?: number
}>()

const emit = defineEmits<{
  accept: []
  reject: []
  counter: []
  'draft-contract': []
}>()

interface QuotePayload {
  version?: string
  price?: number
  quantity?: number
  unit?: string
  deliveryPlace?: string
  paymentMethod?: string
  deliveryMode?: string
  deliveryDate?: string
  packaging?: string
  invoiceType?: string
  remark?: string
  productName?: string
  categoryName?: string
  subjectType?: string
  subjectId?: number
}

const payload = computed<QuotePayload>(() => {
  if (!props.message.payloadJson) return {}
  try { return JSON.parse(props.message.payloadJson) } catch { return {} }
})

const prevPayload = computed<QuotePayload>(() => {
  if (!props.prevMessage?.payloadJson) return {}
  try { return JSON.parse(props.prevMessage.payloadJson) } catch { return {} }
})

const status = computed(() => props.message.quoteStatus || 'OFFERED')

const statusLabel = computed(() => {
  const map: Record<string, string> = { OFFERED: '待确认', ACCEPTED: '已接受', REJECTED: '已拒绝', EXPIRED: '已过期' }
  return map[status.value] || status.value
})

const statusClass = computed(() => {
  const map: Record<string, string> = { OFFERED: 'badge--warning', ACCEPTED: 'badge--success', REJECTED: 'badge--error', EXPIRED: 'badge--muted' }
  return map[status.value] || ''
})

const totalAmount = computed(() => {
  if (payload.value.price && payload.value.quantity) return (payload.value.price * payload.value.quantity).toFixed(2)
  return null
})

const productLabel = computed(() => payload.value.productName || payload.value.categoryName || '')

const priceDelta = computed(() => {
  const cur = payload.value.price, prev = prevPayload.value.price
  if (cur == null || prev == null) return null
  const d = cur - prev
  return Number.isFinite(d) && d !== 0 ? d : null
})

const qtyDelta = computed(() => {
  const cur = payload.value.quantity, prev = prevPayload.value.quantity
  if (cur == null || prev == null) return null
  const d = cur - prev
  return Number.isFinite(d) && d !== 0 ? d : null
})

function fmtDelta(v: number, u?: string) { return `${v > 0 ? '+' : ''}${v}${u || ''}` }
function deltaClass(v: number) { return v > 0 ? 'delta--up' : v < 0 ? 'delta--down' : '' }

const tradeTerms = computed(() => {
  const items: { label: string; value: string; icon: string }[] = []
  if (payload.value.deliveryMode) items.push({ label: '交货', icon: 'truck', value: payload.value.deliveryMode })
  if (payload.value.paymentMethod) items.push({ label: '付款', icon: 'credit-card', value: payload.value.paymentMethod })
  if (payload.value.packaging) items.push({ label: '包装', icon: 'package', value: payload.value.packaging })
  if (payload.value.invoiceType) items.push({ label: '发票', icon: 'file-text', value: payload.value.invoiceType })
  return items
})

const logisticsInfo = computed(() => {
  const items: { label: string; value: string; icon: string }[] = []
  if (payload.value.deliveryPlace) items.push({ label: '交付地', icon: 'map-pin', value: payload.value.deliveryPlace })
  if (payload.value.deliveryDate) items.push({ label: '到货日', icon: 'calendar', value: payload.value.deliveryDate })
  return items
})

const showActions = computed(() => !props.isMine && status.value === 'OFFERED')
const showDraftContract = computed(() => status.value === 'ACCEPTED')
</script>

<template>
  <view class="qc">
    <!-- Header -->
    <view class="qc__head">
      <view class="qc__head-left">
        <text class="qc__title">报价单</text>
        <text v-if="round" class="qc__round">R{{ round }}</text>
      </view>
      <text class="qc__badge" :class="statusClass">{{ statusLabel }}</text>
    </view>

    <!-- 商品名 -->
    <text v-if="productLabel" class="qc__product">{{ productLabel }}</text>

    <!-- 核心价格区 -->
    <view class="qc__hero">
      <view class="qc__price-row">
        <text class="qc__price">¥{{ payload.price ?? '-' }}</text>
        <text class="qc__unit">/{{ payload.unit || '吨' }}</text>
      </view>
      <view class="qc__calc">
        <text class="qc__calc-text">× {{ payload.quantity ?? '-' }}{{ payload.unit || '吨' }}</text>
        <text v-if="totalAmount" class="qc__calc-total">= ¥{{ totalAmount }}</text>
      </view>
    </view>

    <!-- 变动对比 -->
    <view v-if="priceDelta || qtyDelta" class="qc__deltas">
      <text v-if="priceDelta" class="qc__delta" :class="deltaClass(priceDelta)">单价{{ fmtDelta(priceDelta) }}</text>
      <text v-if="qtyDelta" class="qc__delta" :class="deltaClass(qtyDelta)">数量{{ fmtDelta(qtyDelta, payload.unit || '吨') }}</text>
    </view>

    <!-- 交易条件网格 -->
    <view v-if="tradeTerms.length" class="qc__grid">
      <view v-for="(item, i) in tradeTerms" :key="i" class="qc__grid-cell">
        <text class="qc__grid-label">{{ item.label }}</text>
        <view class="qc__grid-val-row">
          <WgIcon :name="item.icon" :size="11" :color="WARM_500" />
          <text class="qc__grid-val">{{ item.value }}</text>
        </view>
      </view>
    </view>

    <!-- 物流信息 -->
    <view v-if="logisticsInfo.length" class="qc__rows">
      <view v-for="(item, i) in logisticsInfo" :key="i" class="qc__row">
        <view class="qc__row-left">
          <WgIcon :name="item.icon" :size="11" :color="WARM_500" />
          <text class="qc__row-label">{{ item.label }}</text>
        </view>
        <text class="qc__row-val">{{ item.value }}</text>
      </view>
    </view>

    <!-- 备注 -->
    <view v-if="payload.remark" class="qc__remark">
      <text class="qc__remark-text">{{ payload.remark }}</text>
    </view>

    <!-- 操作区 -->
    <view v-if="showActions" class="qc__actions">
      <view class="qc__btn qc__btn--pri" @tap="emit('accept')">
        <text class="qc__btn-t qc__btn-t--w">接受</text>
      </view>
      <view class="qc__btn qc__btn--sec" @tap="emit('counter')">
        <text class="qc__btn-t">还价</text>
      </view>
    </view>

    <view v-if="showDraftContract" class="qc__actions">
      <view class="qc__btn qc__btn--pri qc__btn--full" @tap="emit('draft-contract')">
        <WgIcon name="file-text" :size="13" color="#fff" />
        <text class="qc__btn-t qc__btn-t--w">起草合同</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.qc {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: 20rpx 24rpx;
  min-width: 400rpx;
  max-width: 520rpx;
  box-shadow: $shadow-sm;

  /* ===== Header ===== */
  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8rpx;
  }

  &__head-left { display: flex; align-items: center; gap: 8rpx; }

  &__title {
    font-size: $font-xs;
    color: $text-secondary;
    font-weight: 700;
    letter-spacing: 1rpx;
  }

  &__round {
    font-size: 20rpx;
    color: $brand-600;
    background: $brand-50;
    padding: 2rpx 10rpx;
    border-radius: $radius-pill;
    font-weight: 600;
  }

  &__badge {
    font-size: 20rpx;
    padding: 2rpx 12rpx;
    border-radius: $radius-pill;
    font-weight: 600;
  }

  .badge--warning { color: $color-warning; background: rgba($color-warning, 0.1); }
  .badge--success { color: $brand-600; background: $brand-50; }
  .badge--error   { color: $color-error; background: rgba($color-error, 0.1); }
  .badge--muted   { color: $text-placeholder; background: $bg-hover; }

  /* ===== 商品名 ===== */
  &__product {
    font-size: $font-xs;
    color: $text-secondary;
    display: block;
    margin-bottom: 10rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  /* ===== 核心价格区 ===== */
  &__hero {
    background: linear-gradient(135deg, rgba($brand-600, 0.06) 0%, rgba($accent-400, 0.06) 100%);
    border-radius: $radius-md;
    padding: 14rpx 16rpx;
    margin-bottom: 10rpx;
  }

  &__price-row {
    display: flex;
    align-items: baseline;
    gap: 2rpx;
  }

  &__price {
    font-size: 44rpx;
    font-weight: 800;
    color: $accent-400;
    line-height: 1.1;
  }

  &__unit {
    font-size: $font-sm;
    color: $text-secondary;
  }

  &__calc {
    display: flex;
    align-items: center;
    gap: 8rpx;
    margin-top: 4rpx;
  }

  &__calc-text {
    font-size: $font-xs;
    color: $text-secondary;
  }

  &__calc-total {
    font-size: $font-sm;
    font-weight: 700;
    color: $text-primary;
  }

  /* ===== 变动 ===== */
  &__deltas {
    display: flex;
    gap: 8rpx;
    flex-wrap: wrap;
    margin-bottom: 10rpx;
  }

  &__delta {
    font-size: 20rpx;
    padding: 2rpx 10rpx;
    border-radius: $radius-pill;
    font-weight: 600;
  }

  .delta--up   { color: $color-error; background: rgba($color-error, 0.08); }
  .delta--down { color: $brand-600; background: $brand-50; }

  /* ===== 交易条件网格 ===== */
  &__grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rpx;
    background: $border-light;
    border-radius: $radius-md;
    overflow: hidden;
    margin-bottom: 10rpx;
  }

  &__grid-cell {
    background: $bg-page;
    padding: 10rpx 12rpx;
  }

  &__grid-label {
    font-size: 20rpx;
    color: $text-placeholder;
    display: block;
    margin-bottom: 2rpx;
  }

  &__grid-val-row {
    display: flex;
    align-items: center;
    gap: 4rpx;
  }

  &__grid-val {
    font-size: 22rpx;
    color: $text-primary;
    font-weight: 600;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  /* ===== 物流信息行 ===== */
  &__rows {
    margin-bottom: 10rpx;
  }

  &__row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 6rpx 0;
  }

  &__row-left {
    display: flex;
    align-items: center;
    gap: 6rpx;
    flex-shrink: 0;
  }

  &__row-label {
    font-size: 22rpx;
    color: $text-placeholder;
  }

  &__row-val {
    font-size: 22rpx;
    color: $text-primary;
    font-weight: 500;
    text-align: right;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 300rpx;
  }

  /* ===== 备注 ===== */
  &__remark {
    padding: 8rpx 14rpx;
    background: $bg-page;
    border-radius: $radius-sm;
    margin-bottom: 8rpx;
  }

  &__remark-text {
    font-size: 20rpx;
    color: $text-secondary;
    line-height: 1.4;
  }

  /* ===== 操作 ===== */
  &__actions {
    display: flex;
    gap: 12rpx;
    margin-top: 12rpx;
    padding-top: 12rpx;
    border-top: 1rpx solid $border-light;
  }

  &__btn {
    flex: 1;
    height: 56rpx;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6rpx;
    transition: transform 0.12s;

    &:active { transform: scale(0.96); }
    &--pri { background: $brand-600; }
    &--sec { background: $bg-page; border: 1rpx solid $border-color; }
    &--full { flex: none; width: 100%; }
  }

  &__btn-t {
    font-size: $font-sm;
    font-weight: 600;
    color: $text-primary;
    &--w { color: #fff; }
  }
}
</style>
