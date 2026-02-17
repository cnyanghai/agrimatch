<script setup lang="ts">
/**
 * 合同起草页面 - 补齐所有字段
 *
 * 新增字段（对标 Web 端 ContractFromNegotiationRequest）：
 * - 发票类型（invoiceType）
 * - 包装方式（packaging）
 * - 交货方式（deliveryMode）
 * - 产品参数（productParams / paramsJson）
 * - 动态数量单位
 * - 基差价格相关字段
 */
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { WARM_400 } from '../../constants/colors'
import {
  getConversationMessages,
  createContractFromQuote,
  type ChatMessageResponse,
} from '../../api/chat'
import { createContractFromNegotiation } from '../../api/contract'

const messageId = ref(0)
const conversationId = ref(0)
const quoteMessage = ref<ChatMessageResponse | null>(null)
const loading = ref(true)
const submitting = ref(false)

/** 创建来源: 'quote' = 从报价消息, 'negotiation' = 从议价 */
const createSource = ref<'quote' | 'negotiation'>('quote')

interface QuotePayload {
  price?: number
  quantity?: number
  unit?: string
  deliveryPlace?: string
  paymentMethod?: string
  deliveryDate?: string
  remark?: string
  productName?: string
  categoryName?: string
  basisPrice?: number
  contractCode?: string
  priceType?: string
  paramsJson?: string
  deliveryMode?: string
  invoiceType?: string
  packaging?: string
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
  if (form.value.unitPrice && form.value.quantity) {
    return (form.value.unitPrice * form.value.quantity).toFixed(2)
  }
  return '-'
})

const paymentLabel = computed(() => {
  const opt = paymentOptions.find(o => o.value === form.value.paymentMethod)
  return opt?.label || form.value.paymentMethod || '-'
})

const deliveryModeLabel = computed(() => {
  const opt = deliveryModeOptions.find(o => o.value === form.value.deliveryMode)
  return opt?.label || form.value.deliveryMode || ''
})

const invoiceTypeLabel = computed(() => {
  const opt = invoiceTypeOptions.find(o => o.value === form.value.invoiceType)
  return opt?.label || form.value.invoiceType || ''
})

const form = ref({
  title: '',
  productName: '',
  categoryName: '',
  quantity: 0,
  unit: '吨',
  unitPrice: 0,
  deliveryDate: '',
  deliveryAddress: '',
  paymentMethod: '',
  deliveryMode: '',
  invoiceType: '',
  packaging: '',
  remark: '',
  // 基差相关
  priceType: '',
  basisPrice: 0,
  contractCode: '',
  // 参数JSON
  paramsJson: '',
})

// ==================== 选项数据 ====================

const paymentOptions = [
  { value: '01', label: '款到发货' },
  { value: '02', label: '货到付款' },
  { value: '03', label: '账期30天' },
  { value: '04', label: '账期60天' },
  { value: '05', label: '分期付款' },
  { value: '06', label: '预付定金' },
]

const deliveryModeOptions = [
  { value: '01', label: '送货上门' },
  { value: '02', label: '自提' },
  { value: '03', label: '物流配送' },
  { value: '04', label: '快递' },
]

const invoiceTypeOptions = [
  { value: '增值税专用发票', label: '增值税专用发票' },
  { value: '增值税普通发票', label: '增值税普通发票' },
  { value: '无需发票', label: '无需发票' },
]

const unitOptions = [
  { value: '吨', label: '吨' },
  { value: '千克', label: '千克' },
  { value: '斤', label: '斤' },
  { value: '头', label: '头' },
  { value: '羽', label: '羽' },
  { value: '件', label: '件' },
]

// ==================== 产品参数展示 ====================

interface DisplayParam {
  name: string
  value: string
}

const displayParams = computed<DisplayParam[]>(() => {
  if (!form.value.paramsJson) return []
  try {
    const data = JSON.parse(form.value.paramsJson)
    const params = data?.params || data
    if (Array.isArray(params)) {
      return params.map((p: any) => ({
        name: p.label || p.name || '',
        value: String(p.value ?? ''),
      })).filter((p: any) => p.name && p.value)
    }
    if (typeof params === 'object' && params !== null) {
      return Object.entries(params)
        .filter(([, v]) => v != null && v !== '')
        .map(([k, v]) => ({ name: k, value: String(v) }))
        .slice(0, 10)
    }
  } catch { /* ignore */ }
  return []
})

// ==================== 生命周期 ====================

onLoad(async (options) => {
  if (options?.messageId) {
    messageId.value = Number(options.messageId)
    createSource.value = 'quote'
    await loadQuoteMessage()
  }
  if (options?.conversationId) {
    conversationId.value = Number(options.conversationId)
    createSource.value = 'negotiation'
  }
  // 从 payload 预填充扩展字段
  prefillFromPayload()
  loading.value = false
})

async function loadQuoteMessage() {
  // The actual message content will be fetched by the contract creation endpoint
  // Pre-fill form from payload if available
  prefillFromPayload()
}

function prefillFromPayload() {
  const p = payload.value
  if (p.deliveryPlace) form.value.deliveryAddress = p.deliveryPlace
  if (p.paymentMethod) form.value.paymentMethod = p.paymentMethod
  if (p.deliveryDate) form.value.deliveryDate = p.deliveryDate
  if (p.productName) {
    form.value.productName = p.productName
    form.value.title = `${p.productName}采购合同`
  }
  if (p.categoryName) form.value.categoryName = p.categoryName
  if (p.price) form.value.unitPrice = p.price
  if (p.quantity) form.value.quantity = p.quantity
  if (p.unit) form.value.unit = p.unit
  if (p.remark) form.value.remark = p.remark
  if (p.deliveryMode) form.value.deliveryMode = p.deliveryMode
  if (p.invoiceType) form.value.invoiceType = p.invoiceType
  if (p.packaging) form.value.packaging = p.packaging
  if (p.priceType) form.value.priceType = p.priceType
  if (p.basisPrice) form.value.basisPrice = p.basisPrice
  if (p.contractCode) form.value.contractCode = p.contractCode
  if (p.paramsJson) form.value.paramsJson = p.paramsJson
}

// ==================== 表单交互 ====================

function handlePaymentPick(e: any) {
  form.value.paymentMethod = paymentOptions[e.detail.value].value
}

function handleDeliveryModePick(e: any) {
  form.value.deliveryMode = deliveryModeOptions[e.detail.value].value
}

function handleInvoiceTypePick(e: any) {
  form.value.invoiceType = invoiceTypeOptions[e.detail.value].value
}

function handleUnitPick(e: any) {
  form.value.unit = unitOptions[e.detail.value].value
}

function handleDateChange(e: any) {
  form.value.deliveryDate = e.detail.value
}

// ==================== 提交 ====================

async function handleSubmit() {
  if (submitting.value) return
  submitting.value = true

  try {
    let contractId: number

    if (createSource.value === 'quote' && messageId.value) {
      // 从报价消息创建
      contractId = await createContractFromQuote({
        quoteMessageId: messageId.value,
        title: form.value.title || undefined,
        deliveryDate: form.value.deliveryDate || undefined,
        deliveryAddress: form.value.deliveryAddress || undefined,
        paymentMethod: form.value.paymentMethod || undefined,
        terms: form.value.remark || undefined,
      })
    } else {
      // 从议价创建（包含所有字段）
      contractId = await createContractFromNegotiation({
        conversationId: conversationId.value,
        productName: form.value.productName || undefined,
        categoryName: form.value.categoryName || undefined,
        quantity: form.value.quantity || undefined,
        unit: form.value.unit || undefined,
        unitPrice: form.value.unitPrice || undefined,
        basisPrice: form.value.basisPrice || undefined,
        contractCode: form.value.contractCode || undefined,
        priceType: form.value.priceType || undefined,
        deliveryDate: form.value.deliveryDate || undefined,
        deliveryAddress: form.value.deliveryAddress || undefined,
        deliveryMode: form.value.deliveryMode || undefined,
        paymentMethod: form.value.paymentMethod || undefined,
        invoiceType: form.value.invoiceType || undefined,
        packaging: form.value.packaging || undefined,
        remark: form.value.remark || undefined,
        paramsJson: form.value.paramsJson || undefined,
      })
    }

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
            ¥{{ form.unitPrice || '-' }}/{{ form.unit || '吨' }}
            x {{ form.quantity || '-' }}{{ form.unit || '吨' }}
          </text>
          <text class="summary-card__total">= ¥{{ totalAmount }}</text>
        </view>
        <view v-if="form.deliveryAddress || paymentLabel !== '-'" class="summary-card__details">
          <text v-if="form.deliveryAddress" class="summary-card__detail">
            交付: {{ form.deliveryAddress }}
          </text>
          <text v-if="form.deliveryAddress && paymentLabel !== '-'" class="summary-card__sep">|</text>
          <text v-if="paymentLabel !== '-'" class="summary-card__detail">
            {{ paymentLabel }}
          </text>
        </view>
        <!-- 基差信息 -->
        <view v-if="form.priceType === 'BASIS' || form.priceType === '1'" class="summary-card__basis">
          <text class="summary-card__basis-label">定价方式: 基差定价</text>
          <text v-if="form.contractCode" class="summary-card__basis-info">
            合约: {{ form.contractCode }} | 基差: {{ form.basisPrice >= 0 ? '+' : '' }}{{ form.basisPrice }}
          </text>
        </view>
      </view>

      <!-- 产品参数（如果有） -->
      <view v-if="displayParams.length > 0" class="params-card">
        <text class="params-card__title">产品参数</text>
        <view class="params-card__grid">
          <view
            v-for="(param, idx) in displayParams"
            :key="idx"
            class="params-card__item"
          >
            <text class="params-card__item-label">{{ param.name }}</text>
            <text class="params-card__item-value">{{ param.value }}</text>
          </view>
        </view>
      </view>

      <!-- Contract form - 必填字段 -->
      <view class="form-card">
        <text class="form-card__section-title">基本信息</text>

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
              <WgIcon name="right" :size="16" :color="WARM_400" />
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
                {{ paymentLabel || '选择付款方式' }}
              </text>
              <WgIcon name="right" :size="16" :color="WARM_400" />
            </view>
          </picker>
        </view>

        <view class="form-card__field">
          <text class="form-card__label">交货方式</text>
          <picker :range="deliveryModeOptions" range-key="label" @change="handleDeliveryModePick">
            <view class="form-card__picker">
              <text
                class="form-card__picker-text"
                :class="{ 'form-card__picker-text--placeholder': !form.deliveryMode }"
              >
                {{ deliveryModeLabel || '选择交货方式' }}
              </text>
              <WgIcon name="right" :size="16" :color="WARM_400" />
            </view>
          </picker>
        </view>
      </view>

      <!-- 扩展字段 -->
      <view class="form-card">
        <text class="form-card__section-title">补充信息</text>

        <view class="form-card__field">
          <text class="form-card__label">数量单位</text>
          <picker :range="unitOptions" range-key="label" @change="handleUnitPick">
            <view class="form-card__picker">
              <text class="form-card__picker-text">
                {{ form.unit || '吨' }}
              </text>
              <WgIcon name="right" :size="16" :color="WARM_400" />
            </view>
          </picker>
        </view>

        <view class="form-card__field">
          <text class="form-card__label">发票类型</text>
          <picker :range="invoiceTypeOptions" range-key="label" @change="handleInvoiceTypePick">
            <view class="form-card__picker">
              <text
                class="form-card__picker-text"
                :class="{ 'form-card__picker-text--placeholder': !form.invoiceType }"
              >
                {{ invoiceTypeLabel || '选择发票类型' }}
              </text>
              <WgIcon name="right" :size="16" :color="WARM_400" />
            </view>
          </picker>
        </view>

        <view class="form-card__field">
          <text class="form-card__label">包装方式</text>
          <input
            v-model="form.packaging"
            class="form-card__input"
            placeholder="请输入包装方式（如：散装、袋装等）"
          />
        </view>

        <view class="form-card__field form-card__field--last">
          <text class="form-card__label">补充条款/备注</text>
          <textarea
            v-model="form.remark"
            class="form-card__textarea"
            placeholder="请输入补充条款或备注（可选）"
            :maxlength="2000"
            :auto-height="true"
          />
        </view>
      </view>

      <!-- 基差定价字段（仅当基差交易时显示） -->
      <view v-if="form.priceType === 'BASIS' || form.priceType === '1'" class="form-card">
        <text class="form-card__section-title">基差定价信息</text>

        <view class="form-card__field">
          <text class="form-card__label">期货合约代码</text>
          <input
            v-model="form.contractCode"
            class="form-card__input"
            placeholder="如：M2509"
          />
        </view>

        <view class="form-card__field form-card__field--last">
          <text class="form-card__label">基差价格（元/吨）</text>
          <input
            v-model.number="form.basisPrice"
            type="digit"
            class="form-card__input"
            placeholder="输入基差价格"
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

  &__basis {
    margin-top: $spacing-sm;
    padding: $spacing-xs $spacing-sm;
    background: rgba($action-600, 0.06);
    border-radius: $radius-md;
  }

  &__basis-label {
    font-size: $font-sm;
    color: $action-600;
    font-weight: 600;
    display: block;
  }

  &__basis-info {
    font-size: $font-xs;
    color: $text-secondary;
    display: block;
    margin-top: 2rpx;
  }
}

/* ===== 产品参数卡片 ===== */
.params-card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md $spacing-lg;
  margin-bottom: $spacing-sm;

  &__title {
    font-size: $font-sm;
    font-weight: bold;
    color: $text-secondary;
    text-transform: uppercase;
    letter-spacing: 4rpx;
    display: block;
    margin-bottom: $spacing-sm;
  }

  &__grid {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  &__item {
    padding: $spacing-xs $spacing-sm;
    background: $bg-page;
    border-radius: $radius-md;
  }

  &__item-label {
    font-size: $font-xs;
    color: $text-secondary;
    display: block;
  }

  &__item-value {
    font-size: $font-sm;
    font-weight: 600;
    color: $text-primary;
    display: block;
  }
}

.form-card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md $spacing-lg;
  margin-bottom: $spacing-sm;

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
    color: $text-inverse;
  }
}
</style>
