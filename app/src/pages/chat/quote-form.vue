<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { WARM_400 } from '../../constants/colors'
import { sendQuoteMessage } from '../../api/chat'
import { getSupply } from '../../api/supply'
import { getRequirement } from '../../api/requirement'


const conversationId = ref(0)
const subjectType = ref('')
const subjectId = ref(0)
const subjectName = ref('')

const QUOTE_DRAFT_KEY = 'quoteDraft'

function normalizeSubjectType(type?: string): '' | 'supply' | 'requirement' {
  if (!type) return ''
  const t = String(type).toLowerCase()
  if (t === 'supply') return 'supply'
  if (t === 'requirement' || t === 'need') return 'requirement'
  if (t === 'sup' || t === 'supplier') return 'supply'
  if (t === 'req' || t === 'buyer') return 'requirement'
  return ''
}

const form = ref({
  price: '',
  quantity: '',
  unit: '吨',
  deliveryPlace: '',
  deliveryDate: '',
  deliveryMode: '',
  paymentMethod: '',
  remark: '',
})

const submitting = ref(false)

const deliveryModeOptions = ['送货上门', '自提']
const paymentOptions = [
  { value: '01', label: '款到发货' },
  { value: '02', label: '货到付款' },
  { value: '03', label: '账期30天' },
  { value: '04', label: '账期60天' },
  { value: '05', label: '分期付款' },
  { value: '06', label: '预付定金' },
]

const canSubmit = computed(() => {
  return form.value.price && form.value.quantity && !submitting.value
})

onLoad(async (options) => {
  if (options?.conversationId) {
    conversationId.value = Number(options.conversationId)
  }
  if (options?.subjectType) {
    subjectType.value = normalizeSubjectType(options.subjectType)
  }
  if (options?.subjectId) {
    subjectId.value = Number(options.subjectId)
  }

  // Counter-quote draft prefill (from chat)
  if (options?.mode === 'counter') {
    const draft = uni.getStorageSync(QUOTE_DRAFT_KEY)
    if (draft && typeof draft === 'object') {
      if (draft.subjectType && !subjectType.value) subjectType.value = normalizeSubjectType(draft.subjectType)
      if (draft.subjectId && !subjectId.value) subjectId.value = Number(draft.subjectId)
      if (draft.productName || draft.categoryName) {
        subjectName.value = draft.productName || draft.categoryName
      }
      if (draft.unit) form.value.unit = String(draft.unit)
      if (draft.price != null) form.value.price = String(draft.price)
      if (draft.quantity != null) form.value.quantity = String(draft.quantity)
      if (draft.deliveryPlace) form.value.deliveryPlace = String(draft.deliveryPlace)
      if (draft.deliveryDate) form.value.deliveryDate = String(draft.deliveryDate)
      if (draft.deliveryMode) form.value.deliveryMode = String(draft.deliveryMode)
      if (draft.paymentMethod) form.value.paymentMethod = String(draft.paymentMethod)
      if (draft.remark) form.value.remark = String(draft.remark)
    }
    uni.removeStorageSync(QUOTE_DRAFT_KEY)
  }

  // Pre-fill from subject context
  if (subjectType.value && subjectId.value && !subjectName.value) {
    try {
      if (subjectType.value === 'supply') {
        const supply = await getSupply(subjectId.value)
        if (supply) {
          subjectName.value = supply.categoryName || ''
          if (supply.exFactoryPrice) form.value.price = String(supply.exFactoryPrice)
        }
      } else if (subjectType.value === 'requirement') {
        const req = await getRequirement(subjectId.value)
        if (req) {
          subjectName.value = req.categoryName || ''
          if (req.expectedPrice) form.value.price = String(req.expectedPrice)
        }
      }
    } catch {
      // silent
    }
  }
})

function handleDeliveryModePick(e: any) {
  form.value.deliveryMode = deliveryModeOptions[e.detail.value]
}

function handlePaymentPick(e: any) {
  form.value.paymentMethod = paymentOptions[e.detail.value].value
}

function handleDateChange(e: any) {
  form.value.deliveryDate = e.detail.value
}

const paymentLabel = computed(() => {
  const opt = paymentOptions.find(o => o.value === form.value.paymentMethod)
  return opt?.label || ''
})

async function handleSubmit() {
  if (!canSubmit.value) return
  if (submitting.value) return
  submitting.value = true

  try {
    const price = parseFloat(form.value.price)
    const quantity = parseFloat(form.value.quantity)
    if (isNaN(price) || price <= 0) {
      uni.showToast({ title: '请输入有效单价', icon: 'none' })
      return
    }
    if (isNaN(quantity) || quantity <= 0) {
      uni.showToast({ title: '请输入有效数量', icon: 'none' })
      return
    }

    const payloadJson = JSON.stringify({
      version: 'V1',
      price,
      quantity,
      unit: form.value.unit,
      deliveryPlace: form.value.deliveryPlace || undefined,
      deliveryDate: form.value.deliveryDate || undefined,
      deliveryMode: form.value.deliveryMode || undefined,
      paymentMethod: form.value.paymentMethod || undefined,
      remark: form.value.remark || undefined,
      productName: subjectName.value || undefined,
      categoryName: subjectName.value || undefined,
      subjectType: normalizeSubjectType(subjectType.value) || undefined,
      subjectId: subjectId.value || undefined,
    })

    const content = `报价: ¥${price}/${form.value.unit} × ${quantity}${form.value.unit}`

    await sendQuoteMessage(conversationId.value, payloadJson, content)
    uni.showToast({ title: '报价已发送', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 500)
  } catch {
    // handled by request.ts
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="quote-form-page">
    <view class="form-card">
      <!-- Subject name (read-only) -->
      <view v-if="subjectName" class="form-card__field">
        <text class="form-card__label">商品</text>
        <view class="form-card__readonly">
          <text class="form-card__readonly-text">{{ subjectName }}</text>
        </view>
      </view>

      <!-- Price -->
      <view class="form-card__field">
        <text class="form-card__label">单价 <text class="form-card__required">*</text></text>
        <view class="form-card__input-row">
          <text class="form-card__prefix">¥</text>
          <input
            v-model="form.price"
            class="form-card__input"
            type="digit"
            placeholder="请输入单价"
          />
          <text class="form-card__suffix">/{{ form.unit }}</text>
        </view>
      </view>

      <!-- Quantity -->
      <view class="form-card__field">
        <text class="form-card__label">数量 <text class="form-card__required">*</text></text>
        <view class="form-card__input-row">
          <input
            v-model="form.quantity"
            class="form-card__input"
            type="digit"
            placeholder="请输入数量"
          />
          <text class="form-card__suffix">{{ form.unit }}</text>
        </view>
      </view>

      <!-- Delivery mode -->
      <view class="form-card__field">
        <text class="form-card__label">交货方式</text>
        <picker :range="deliveryModeOptions" @change="handleDeliveryModePick">
          <view class="form-card__picker">
            <text
              class="form-card__picker-text"
              :class="{ 'form-card__picker-text--placeholder': !form.deliveryMode }"
            >
              {{ form.deliveryMode || '选择交货方式（可选）' }}
            </text>
            <WgIcon name="right" :size="16" :color="WARM_400" />
          </view>
        </picker>
      </view>

      <!-- Delivery place -->
      <view class="form-card__field">
        <text class="form-card__label">交付地</text>
        <input
          v-model="form.deliveryPlace"
          class="form-card__input"
          placeholder="请输入交付地（可选）"
        />
      </view>

      <!-- Delivery date -->
      <view class="form-card__field">
        <text class="form-card__label">到货日期</text>
        <picker mode="date" @change="handleDateChange">
          <view class="form-card__picker">
            <text
              class="form-card__picker-text"
              :class="{ 'form-card__picker-text--placeholder': !form.deliveryDate }"
            >
              {{ form.deliveryDate || '选择到货日期（可选）' }}
            </text>
            <WgIcon name="right" :size="16" :color="WARM_400" />
          </view>
        </picker>
      </view>

      <!-- Payment method -->
      <view class="form-card__field">
        <text class="form-card__label">付款方式</text>
        <picker :range="paymentOptions" range-key="label" @change="handlePaymentPick">
          <view class="form-card__picker">
            <text
              class="form-card__picker-text"
              :class="{ 'form-card__picker-text--placeholder': !form.paymentMethod }"
            >
              {{ paymentLabel || '选择付款方式（可选）' }}
            </text>
            <WgIcon name="right" :size="16" :color="WARM_400" />
          </view>
        </picker>
      </view>

      <!-- Remark -->
      <view class="form-card__field form-card__field--last">
        <text class="form-card__label">备注</text>
        <input
          v-model="form.remark"
          class="form-card__input"
          placeholder="请输入备注（可选）"
          :maxlength="200"
        />
      </view>
    </view>

    <!-- Submit -->
    <view class="quote-form-page__action">
      <view
        class="submit-btn"
        :class="{ 'submit-btn--disabled': !canSubmit }"
        @tap="handleSubmit"
      >
        <text class="submit-btn__text">{{ submitting ? '发送中...' : '发送报价' }}</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.quote-form-page {
  min-height: 100vh;
  background: $bg-page;
  padding: $spacing-md;

  &__action {
    margin-top: $spacing-lg;
  }
}

.form-card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md $spacing-lg;

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

  &__required {
    color: $accent-400;
    font-weight: normal;
  }

  &__input {
    width: 100%;
    height: 80rpx;
    font-size: $font-md;
    color: $text-primary;
    padding: 0;
  }

  &__input-row {
    display: flex;
    align-items: center;
    height: 80rpx;
    gap: $spacing-xs;
  }

  &__prefix {
    font-size: $font-lg;
    font-weight: bold;
    color: $accent-400;
    flex-shrink: 0;
  }

  &__suffix {
    font-size: $font-md;
    color: $text-secondary;
    flex-shrink: 0;
  }

  &__readonly {
    padding: $spacing-xs 0;
  }

  &__readonly-text {
    font-size: $font-md;
    color: $text-secondary;
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
    color: $text-inverse;
  }
}
</style>
