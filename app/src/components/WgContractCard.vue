<script setup lang="ts">
import { computed } from 'vue'
import type { ChatMessageResponse } from '../api/chat'
import { BRAND_600, WHITE, WARM_400, WARM_500 } from '../constants/colors'

const props = defineProps<{
  message: ChatMessageResponse
  currentCompanyId?: number
}>()

const emit = defineEmits<{
  view: [contractId: number]
  sign: [contractId: number]
}>()

interface ContractPayload {
  contractId: number
  contractNo: string
  productName?: string
  quantity?: number | string
  unit?: string
  unitPrice?: number | string
  basisPrice?: number | string
  totalAmount?: number | string
  buyerCompanyId?: number
  buyerCompanyName?: string
  sellerCompanyId?: number
  sellerCompanyName?: string
  status: number
  buyerSigned: boolean
  sellerSigned: boolean
}

const payload = computed<ContractPayload | null>(() => {
  if (!props.message.payloadJson) return null
  try {
    return JSON.parse(props.message.payloadJson)
  } catch {
    return null
  }
})

const steps = [
  { key: 'draft', label: '草稿', icon: 'file-text' },
  { key: 'pending', label: '待签', icon: 'clock' },
  { key: 'signed', label: '签署', icon: 'check' },
  { key: 'executing', label: '履约', icon: 'truck' },
]

const currentStepIndex = computed(() => {
  const s = payload.value?.status ?? 0
  switch (s) {
    case 0: return 0
    case 1: return 1
    case 2: return 2
    case 3: return 3
    case 4: return 4
    case 5: return -1
    default: return 0
  }
})

const statusMap: Record<number, { label: string; variant: string }> = {
  0: { label: '草稿', variant: 'neutral' },
  1: { label: '待签署', variant: 'warning' },
  2: { label: '已签署', variant: 'brand' },
  3: { label: '履约中', variant: 'accent' },
  4: { label: '已完成', variant: 'success' },
  5: { label: '已取消', variant: 'error' },
}

const statusInfo = computed(() => statusMap[payload.value?.status ?? 0] || statusMap[0])

const isBuyer = computed(() => props.currentCompanyId === payload.value?.buyerCompanyId)
const isSeller = computed(() => props.currentCompanyId === payload.value?.sellerCompanyId)
const isCancelled = computed(() => payload.value?.status === 5)

const canSign = computed(() => {
  if (payload.value?.status !== 1) return false
  if (isBuyer.value && !payload.value.buyerSigned) return true
  if (isSeller.value && !payload.value.sellerSigned) return true
  return false
})

function formatAmount(val?: number | string): string {
  if (val == null) return '-'
  const num = typeof val === 'string' ? parseFloat(val) : val
  return isNaN(num) ? '-' : num.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

function handleView() {
  if (payload.value?.contractId) emit('view', payload.value.contractId)
}

function handleSign() {
  if (payload.value?.contractId) emit('sign', payload.value.contractId)
}
</script>

<template>
  <view v-if="payload" class="contract-card stitch-card stitch-card--elevated">
    <!-- Header -->
    <view class="contract-card__header">
      <view class="contract-card__icon-box">
        <WgIcon name="file-text" :size="18" :color="WHITE" />
      </view>
      <view class="contract-card__header-info">
        <text class="contract-card__label">采购合同</text>
        <text class="contract-card__no">{{ payload.contractNo }}</text>
      </view>
      <WgStatusChip :label="statusInfo.label" :variant="statusInfo.variant as any" size="sm" />
    </view>

    <!-- Progress Steps -->
    <view v-if="!isCancelled" class="contract-card__progress">
      <template v-for="(step, index) in steps" :key="step.key">
        <view class="contract-card__step">
          <view
            class="contract-card__step-dot"
            :class="{
              'contract-card__step-dot--done': index < currentStepIndex,
              'contract-card__step-dot--active': index === currentStepIndex,
            }"
          >
            <WgIcon
              v-if="index < currentStepIndex"
              name="check"
              :size="12"
              :color="WHITE"
            />
            <WgIcon
              v-else
              :name="step.icon"
              :size="12"
              :color="index === currentStepIndex ? WHITE : WARM_400"
            />
          </view>
          <text
            class="contract-card__step-label"
            :class="{ 'contract-card__step-label--active': index <= currentStepIndex }"
          >{{ step.label }}</text>
        </view>
        <view
          v-if="index < steps.length - 1"
          class="contract-card__step-line"
          :class="{ 'contract-card__step-line--done': index < currentStepIndex }"
        />
      </template>
    </view>

    <!-- Signing Status -->
    <view class="contract-card__sign-row">
      <view class="contract-card__sign-box">
        <text class="contract-card__sign-role">买方</text>
        <view class="contract-card__sign-status">
          <WgIcon
            :name="payload.buyerSigned ? 'check-circle' : 'clock'"
            :size="14"
            :color="payload.buyerSigned ? BRAND_600 : WARM_500"
          />
          <text :class="['contract-card__sign-text', payload.buyerSigned ? 'contract-card__sign-text--done' : 'contract-card__sign-text--pending']">
            {{ payload.buyerSigned ? '已签署' : '待签署' }}
          </text>
        </view>
        <text class="contract-card__sign-name">{{ payload.buyerCompanyName || '-' }}</text>
      </view>
      <view class="contract-card__sign-box">
        <text class="contract-card__sign-role">卖方</text>
        <view class="contract-card__sign-status">
          <WgIcon
            :name="payload.sellerSigned ? 'check-circle' : 'clock'"
            :size="14"
            :color="payload.sellerSigned ? BRAND_600 : WARM_500"
          />
          <text :class="['contract-card__sign-text', payload.sellerSigned ? 'contract-card__sign-text--done' : 'contract-card__sign-text--pending']">
            {{ payload.sellerSigned ? '已签署' : '待签署' }}
          </text>
        </view>
        <text class="contract-card__sign-name">{{ payload.sellerCompanyName || '-' }}</text>
      </view>
    </view>

    <!-- Amount -->
    <view class="contract-card__amount-row">
      <text class="contract-card__amount-label">合同金额</text>
      <text class="contract-card__amount-value font-mono">
        <template v-if="payload.basisPrice != null && payload.basisPrice !== ''">待结算</template>
        <template v-else>¥{{ formatAmount(payload.totalAmount) }}</template>
      </text>
    </view>

    <!-- Actions -->
    <view class="contract-card__actions">
      <view class="contract-card__btn contract-card__btn--ghost tap-feedback" @tap="handleView">
        <WgIcon name="file-text" :size="14" :color="WARM_500" />
        <text class="contract-card__btn-text">查看详情</text>
      </view>
      <view v-if="canSign" class="contract-card__btn contract-card__btn--primary tap-feedback" @tap="handleSign">
        <WgIcon name="pen-line" :size="14" :color="WHITE" />
        <text class="contract-card__btn-text contract-card__btn-text--white">立即签署</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.contract-card {
  min-width: 420rpx;
  max-width: 540rpx;
  padding: 0;
  overflow: hidden;

  &__header {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-md;
    background: linear-gradient(135deg, $warm-50 0%, $bg-card 100%);
    border-bottom: 1rpx solid $border-light;
  }

  &__icon-box {
    width: 64rpx;
    height: 64rpx;
    border-radius: $radius-md;
    background: $warm-900;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__header-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
  }

  &__label {
    font-size: 18rpx;
    font-weight: 700;
    letter-spacing: 2rpx;
    text-transform: uppercase;
    color: $text-placeholder;
  }

  &__no {
    font-size: $font-sm;
    font-weight: 700;
    color: $text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__progress {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    padding: $spacing-md;
    border-bottom: 1rpx solid $border-light;
  }

  &__step {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6rpx;
  }

  &__step-dot {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    background: $warm-100;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all $transition-normal;

    &--done,
    &--active {
      background: $brand-600;
    }

    &--active {
      box-shadow: 0 0 0 6rpx rgba($brand-600, 0.15);
    }
  }

  &__step-label {
    font-size: 20rpx;
    color: $text-placeholder;
    font-weight: 500;

    &--active {
      color: $brand-600;
    }
  }

  &__step-line {
    flex: 1;
    height: 4rpx;
    background: $warm-200;
    margin-top: 22rpx;
    margin-left: 4rpx;
    margin-right: 4rpx;
    border-radius: 2rpx;
    transition: background $transition-normal;

    &--done {
      background: $brand-600;
    }
  }

  &__sign-row {
    display: flex;
    gap: $spacing-sm;
    padding: $spacing-md;
    border-bottom: 1rpx solid $border-light;
  }

  &__sign-box {
    flex: 1;
    background: $warm-50;
    border-radius: $radius-md;
    padding: $spacing-sm;
    text-align: center;
  }

  &__sign-role {
    font-size: 20rpx;
    color: $text-placeholder;
    display: block;
    margin-bottom: 4rpx;
  }

  &__sign-status {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4rpx;
  }

  &__sign-text {
    font-size: $font-xs;
    font-weight: 600;

    &--done {
      color: $brand-600;
    }

    &--pending {
      color: $color-warning;
    }
  }

  &__sign-name {
    font-size: 20rpx;
    color: $text-placeholder;
    display: block;
    margin-top: 4rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__amount-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-sm $spacing-md;
  }

  &__amount-label {
    font-size: $font-xs;
    color: $text-secondary;
  }

  &__amount-value {
    font-size: $font-xl;
    font-weight: 700;
    color: $brand-600;
  }

  &__actions {
    display: flex;
    gap: $spacing-sm;
    padding: $spacing-sm $spacing-md $spacing-md;
    background: $warm-50;
    border-top: 1rpx solid $border-light;
  }

  &__btn {
    flex: 1;
    height: 68rpx;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6rpx;

    &--ghost {
      background: $bg-card;
      border: 1rpx solid $border-color;
    }

    &--primary {
      background: $brand-600;
    }
  }

  &__btn-text {
    font-size: $font-sm;
    font-weight: 600;
    color: $text-primary;

    &--white {
      color: $text-inverse;
    }
  }
}
</style>
