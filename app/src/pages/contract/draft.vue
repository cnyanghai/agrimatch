<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  getConversationMessages,
  createContractFromQuote,
  type ChatMessageResponse,
} from '../../api/chat'

const messageId = ref(0)
const quoteMessage = ref<ChatMessageResponse | null>(null)
const loading = ref(true)
const submitting = ref(false)

interface QuotePayload {
  price?: number
  quantity?: number
  unit?: string
  deliveryPlace?: string
  paymentMethod?: string
  deliveryDate?: string
  remark?: string
  productName?: string
}

const payload = computed<QuotePayload>(() => {
  if (!quoteMessage.value?.payloadJson) return {}
  try {
    return JSON.parse(quoteMessage.value.payloadJson)
  } catch {
    return {}
  }
})

const totalAmount = computed(() => {
  if (payload.value.price && payload.value.quantity) {
    return (payload.value.price * payload.value.quantity).toFixed(2)
  }
  return '-'
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
  return map[payload.value.paymentMethod || ''] || payload.value.paymentMethod || '-'
})

const form = ref({
  title: '',
  deliveryDate: '',
  deliveryAddress: '',
  paymentMethod: '',
  terms: '',
})

const paymentOptions = [
  { value: '01', label: '款到发货' },
  { value: '02', label: '货到付款' },
  { value: '03', label: '账期30天' },
  { value: '04', label: '账期60天' },
  { value: '05', label: '分期付款' },
  { value: '06', label: '预付定金' },
]

onLoad(async (options) => {
  if (options?.messageId) {
    messageId.value = Number(options.messageId)
    await loadQuoteMessage()
  }
})

async function loadQuoteMessage() {
  loading.value = true
  try {
    // We fetch the message from its conversation; the backend should return it
    // For now, use the messageId to populate form defaults from the payload
    // The actual message content will be fetched by the contract creation endpoint
    loading.value = false

    // Pre-fill form from payload if available
    if (payload.value.deliveryPlace) {
      form.value.deliveryAddress = payload.value.deliveryPlace
    }
    if (payload.value.paymentMethod) {
      form.value.paymentMethod = payload.value.paymentMethod
    }
    if (payload.value.deliveryDate) {
      form.value.deliveryDate = payload.value.deliveryDate
    }
    if (payload.value.productName) {
      form.value.title = `${payload.value.productName}采购合同`
    }
  } catch {
    loading.value = false
  }
}

function handlePaymentPick(e: any) {
  form.value.paymentMethod = paymentOptions[e.detail.value].value
}

function handleDateChange(e: any) {
  form.value.deliveryDate = e.detail.value
}

const formPaymentLabel = computed(() => {
  const opt = paymentOptions.find(o => o.value === form.value.paymentMethod)
  return opt?.label || ''
})

async function handleSubmit() {
  if (submitting.value) return
  submitting.value = true

  try {
    const contractId = await createContractFromQuote({
      quoteMessageId: messageId.value,
      title: form.value.title || undefined,
      deliveryDate: form.value.deliveryDate || undefined,
      deliveryAddress: form.value.deliveryAddress || undefined,
      paymentMethod: form.value.paymentMethod || undefined,
      terms: form.value.terms || undefined,
    })

    uni.showToast({ title: '合同已创建', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: `/pages/contract/detail?id=${contractId}` })
    }, 500)
  } catch {
    // handled by request.ts
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="draft-page">
    <WgSkeleton v-if="loading" type="detail" />

    <template v-else>
      <!-- Quote summary -->
      <view class="summary-card">
        <text class="summary-card__title">报价摘要</text>
        <view class="summary-card__row">
          <text class="summary-card__price">
            ¥{{ payload.price || '-' }}/{{ payload.unit || '吨' }}
            × {{ payload.quantity || '-' }}{{ payload.unit || '吨' }}
          </text>
          <text class="summary-card__total">= ¥{{ totalAmount }}</text>
        </view>
        <view v-if="payload.deliveryPlace || paymentLabel !== '-'" class="summary-card__details">
          <text v-if="payload.deliveryPlace" class="summary-card__detail">
            交付: {{ payload.deliveryPlace }}
          </text>
          <text v-if="payload.deliveryPlace && paymentLabel !== '-'" class="summary-card__sep">|</text>
          <text v-if="paymentLabel !== '-'" class="summary-card__detail">
            {{ paymentLabel }}
          </text>
        </view>
      </view>

      <!-- Contract form -->
      <view class="form-card">
        <text class="form-card__section-title">合同补充信息</text>

        <view class="form-card__field">
          <text class="form-card__label">合同标题</text>
          <input
            v-model="form.title"
            class="form-card__input"
            placeholder="请输入合同标题"
            :maxlength="100"
          />
        </view>

        <view class="form-card__field">
          <text class="form-card__label">交付日期</text>
          <picker mode="date" @change="handleDateChange">
            <view class="form-card__picker">
              <text
                class="form-card__picker-text"
                :class="{ 'form-card__picker-text--placeholder': !form.deliveryDate }"
              >
                {{ form.deliveryDate || '选择交付日期' }}
              </text>
              <uni-icons type="right" size="16" color="#999" />
            </view>
          </picker>
        </view>

        <view class="form-card__field">
          <text class="form-card__label">交付地址</text>
          <input
            v-model="form.deliveryAddress"
            class="form-card__input"
            placeholder="请输入交付地址"
          />
        </view>

        <view class="form-card__field">
          <text class="form-card__label">付款方式</text>
          <picker :range="paymentOptions" range-key="label" @change="handlePaymentPick">
            <view class="form-card__picker">
              <text
                class="form-card__picker-text"
                :class="{ 'form-card__picker-text--placeholder': !form.paymentMethod }"
              >
                {{ formPaymentLabel || '选择付款方式' }}
              </text>
              <uni-icons type="right" size="16" color="#999" />
            </view>
          </picker>
        </view>

        <view class="form-card__field form-card__field--last">
          <text class="form-card__label">补充条款</text>
          <textarea
            v-model="form.terms"
            class="form-card__textarea"
            placeholder="请输入补充条款（可选）"
            :maxlength="2000"
            :auto-height="true"
          />
        </view>
      </view>

      <!-- Submit -->
      <view class="draft-page__action">
        <view
          class="submit-btn"
          :class="{ 'submit-btn--disabled': submitting }"
          @tap="handleSubmit"
        >
          <text class="submit-btn__text">{{ submitting ? '创建中...' : '确认起草合同' }}</text>
        </view>
      </view>
    </template>
  </view>
</template>

<style lang="scss" scoped>
.draft-page {
  min-height: 100vh;
  background: $bg-page;
  padding: $spacing-md;

  &__action {
    margin-top: $spacing-lg;
    padding-bottom: $spacing-xl;
  }
}

.summary-card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-sm;

  &__title {
    font-size: $font-sm;
    color: $text-secondary;
    font-weight: 600;
    display: block;
    margin-bottom: $spacing-sm;
  }

  &__row {
    display: flex;
    align-items: baseline;
    gap: $spacing-sm;
    flex-wrap: wrap;
  }

  &__price {
    font-size: $font-md;
    color: $text-primary;
  }

  &__total {
    font-size: $font-xl;
    font-weight: bold;
    color: $accent-400;
  }

  &__details {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    margin-top: $spacing-xs;
  }

  &__detail {
    font-size: $font-sm;
    color: $text-secondary;
  }

  &__sep {
    font-size: $font-xs;
    color: $text-placeholder;
  }
}

.form-card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md $spacing-lg;

  &__section-title {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
    display: block;
    margin-bottom: $spacing-sm;
  }

  &__field {
    padding: $spacing-sm 0;
    border-bottom: 1rpx solid $border-light;

    &--last {
      border-bottom: none;
    }
  }

  &__label {
    font-size: $font-md;
    font-weight: 600;
    color: $text-primary;
    display: block;
    margin-bottom: $spacing-xs;
  }

  &__input {
    width: 100%;
    height: 80rpx;
    font-size: $font-md;
    color: $text-primary;
    padding: 0;
  }

  &__textarea {
    width: 100%;
    min-height: 160rpx;
    font-size: $font-md;
    color: $text-primary;
    line-height: 1.8;
    padding: $spacing-xs 0;
  }

  &__picker {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 80rpx;
  }

  &__picker-text {
    font-size: $font-md;
    color: $text-primary;

    &--placeholder {
      color: $text-placeholder;
    }
  }
}

.submit-btn {
  width: 100%;
  height: 96rpx;
  background: $brand-600;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform $transition-fast;

  &:active {
    transform: scale(0.95);
  }

  &--disabled {
    opacity: 0.5;
  }

  &__text {
    font-size: $font-lg;
    font-weight: bold;
    color: #fff;
  }
}
</style>
