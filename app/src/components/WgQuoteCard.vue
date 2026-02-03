<script setup lang="ts">
import { computed } from 'vue'
import type { ChatMessageResponse } from '../api/chat'

const props = defineProps<{
  message: ChatMessageResponse
  isMine: boolean
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
  remark?: string
  productName?: string
  categoryName?: string
}

const payload = computed<QuotePayload>(() => {
  if (!props.message.payloadJson) return {}
  try {
    return JSON.parse(props.message.payloadJson)
  } catch {
    return {}
  }
})

const status = computed(() => props.message.quoteStatus || 'OFFERED')

const statusLabel = computed(() => {
  const map: Record<string, string> = {
    OFFERED: '待确认',
    ACCEPTED: '已接受',
    REJECTED: '已拒绝',
    EXPIRED: '已过期',
  }
  return map[status.value] || status.value
})

const statusClass = computed(() => {
  const map: Record<string, string> = {
    OFFERED: 'quote-card__badge--warning',
    ACCEPTED: 'quote-card__badge--success',
    REJECTED: 'quote-card__badge--error',
    EXPIRED: 'quote-card__badge--muted',
  }
  return map[status.value] || ''
})

const totalAmount = computed(() => {
  if (payload.value.price && payload.value.quantity) {
    return (payload.value.price * payload.value.quantity).toFixed(2)
  }
  return null
})

const paymentLabel = computed(() => {
  const map: Record<string, string> = {
    '01': '款到发货',
    '02': '货到付款',
    '03': '账期30天',
    '04': '账期60天',
    '05': '分期付款',
    '06': '预付定金',
  }
  return map[payload.value.paymentMethod || ''] || payload.value.paymentMethod || ''
})

const showActions = computed(() => !props.isMine && status.value === 'OFFERED')
const showDraftContract = computed(() => status.value === 'ACCEPTED')
</script>

<template>
  <view class="quote-card">
    <!-- Header -->
    <view class="quote-card__header">
      <text class="quote-card__title">报价</text>
      <text class="quote-card__badge" :class="statusClass">{{ statusLabel }}</text>
    </view>

    <!-- Price info -->
    <view class="quote-card__price-row">
      <text class="quote-card__price">
        ¥{{ payload.price || '-' }}/{{ payload.unit || '吨' }}
      </text>
      <text class="quote-card__qty">× {{ payload.quantity || '-' }}{{ payload.unit || '吨' }}</text>
    </view>
    <text v-if="totalAmount" class="quote-card__total">合计 ¥{{ totalAmount }}</text>

    <!-- Details -->
    <view v-if="payload.deliveryPlace || paymentLabel" class="quote-card__details">
      <text v-if="payload.deliveryPlace" class="quote-card__detail-item">
        交付: {{ payload.deliveryPlace }}
      </text>
      <text v-if="payload.deliveryPlace && paymentLabel" class="quote-card__separator">|</text>
      <text v-if="paymentLabel" class="quote-card__detail-item">
        {{ paymentLabel }}
      </text>
    </view>

    <view v-if="payload.deliveryDate" class="quote-card__details">
      <text class="quote-card__detail-item">到货日期: {{ payload.deliveryDate }}</text>
    </view>

    <view v-if="payload.remark" class="quote-card__remark">
      <text class="quote-card__remark-text">{{ payload.remark }}</text>
    </view>

    <!-- Actions (visible to the other party when OFFERED) -->
    <view v-if="showActions" class="quote-card__divider" />
    <view v-if="showActions" class="quote-card__actions">
      <view class="quote-card__btn quote-card__btn--primary" @tap="emit('accept')">
        <text class="quote-card__btn-text quote-card__btn-text--primary">接受报价</text>
      </view>
      <view class="quote-card__btn quote-card__btn--secondary" @tap="emit('counter')">
        <text class="quote-card__btn-text quote-card__btn-text--secondary">还价</text>
      </view>
    </view>

    <!-- Draft contract button (after accepted) -->
    <view v-if="showDraftContract" class="quote-card__divider" />
    <view v-if="showDraftContract" class="quote-card__actions">
      <view class="quote-card__btn quote-card__btn--primary quote-card__btn--full" @tap="emit('draft-contract')">
        <text class="quote-card__btn-text quote-card__btn-text--primary">起草合同 →</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.quote-card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md;
  min-width: 420rpx;
  max-width: 520rpx;
  box-shadow: $shadow-sm;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-sm;
  }

  &__title {
    font-size: $font-sm;
    color: $text-secondary;
    font-weight: 600;
  }

  &__badge {
    font-size: $font-xs;
    padding: 4rpx 14rpx;
    border-radius: $radius-sm;

    &--warning {
      color: $color-warning;
      background: rgba($color-warning, 0.1);
    }
    &--success {
      color: $brand-600;
      background: $brand-50;
    }
    &--error {
      color: $color-error;
      background: rgba($color-error, 0.1);
    }
    &--muted {
      color: $text-placeholder;
      background: $bg-hover;
    }
  }

  &__price-row {
    display: flex;
    align-items: baseline;
    gap: $spacing-sm;
  }

  &__price {
    font-size: $font-xl;
    font-weight: bold;
    color: $accent-400;
  }

  &__qty {
    font-size: $font-md;
    color: $text-secondary;
  }

  &__total {
    font-size: $font-sm;
    color: $text-secondary;
    display: block;
    margin-top: 4rpx;
  }

  &__details {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    margin-top: $spacing-xs;
  }

  &__detail-item {
    font-size: $font-sm;
    color: $text-secondary;
  }

  &__separator {
    font-size: $font-xs;
    color: $text-placeholder;
  }

  &__remark {
    margin-top: $spacing-xs;
    padding: $spacing-xs $spacing-sm;
    background: $bg-page;
    border-radius: $radius-sm;
  }

  &__remark-text {
    font-size: $font-xs;
    color: $text-secondary;
    line-height: 1.5;
  }

  &__divider {
    height: 1rpx;
    background: $border-light;
    margin: $spacing-sm 0;
  }

  &__actions {
    display: flex;
    gap: $spacing-sm;
  }

  &__btn {
    flex: 1;
    height: 64rpx;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: transform $transition-fast;

    &:active {
      transform: scale(0.95);
    }

    &--primary {
      background: $brand-600;
    }

    &--secondary {
      background: $bg-page;
      border: 1rpx solid $border-color;
    }

    &--full {
      flex: none;
      width: 100%;
    }
  }

  &__btn-text {
    font-size: $font-sm;
    font-weight: 600;

    &--primary {
      color: #fff;
    }

    &--secondary {
      color: $text-primary;
    }
  }
}
</style>
