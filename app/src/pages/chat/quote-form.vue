<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { WARM_400, WARM_500, BRAND_600 } from '../../constants/colors'
import { sendQuoteMessage } from '../../api/chat'
import { getSupply, type SupplyResponse } from '../../api/supply'
import { getRequirement, type RequirementResponse } from '../../api/requirement'
import { parseParams, type ParsedParam } from '../../utils/parseParams'

const conversationId = ref(0)
const subjectType = ref('')
const subjectId = ref(0)
const subjectName = ref('')

const supplyData = ref<SupplyResponse | null>(null)
const requirementData = ref<RequirementResponse | null>(null)

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
  packaging: '',
  invoiceType: '',
  remark: '',
})

const submitting = ref(false)

const deliveryModeOptions = ['送货上门', '自提', '到厂', '物流运输', '协商']
const paymentOptions = ['款到发货', '货到付款', '现款', '月结30天', '月结60天', '分期付款', '预付定金', '协商']
const packagingOptions = ['编织袋', '纸箱', '吨袋', '散装', '桶装', '协商']
const invoiceTypeOptions = ['增值税专用发票', '增值税普通发票', '不开票', '协商']

const canSubmit = computed(() => form.value.price && form.value.quantity && !submitting.value)

const totalPreview = computed(() => {
  const p = parseFloat(form.value.price)
  const q = parseFloat(form.value.quantity)
  if (!isNaN(p) && p > 0 && !isNaN(q) && q > 0) return (p * q).toFixed(2)
  return ''
})

const productParams = computed<ParsedParam[]>(() => {
  if (supplyData.value?.paramsJson) return parseParams(supplyData.value.paramsJson)
  if (requirementData.value?.paramsJson) return parseParams(requirementData.value.paramsJson)
  return []
})

const productImages = computed<string[]>(() => {
  const json = supplyData.value?.imagesJson || requirementData.value?.imagesJson
  if (!json) return []
  try { const a = JSON.parse(json); return Array.isArray(a) ? a.slice(0, 4) : [] } catch { return [] }
})

const origin = computed(() => supplyData.value?.origin || '')
const storageMethod = computed(() => supplyData.value?.storageMethod || '')

const hasProductInfo = computed(() =>
  productImages.value.length > 0 || productParams.value.length > 0 || origin.value || storageMethod.value
)

onLoad(async (options) => {
  if (options?.conversationId) conversationId.value = Number(options.conversationId)
  if (options?.subjectType) subjectType.value = normalizeSubjectType(options.subjectType)
  if (options?.subjectId) subjectId.value = Number(options.subjectId)

  if (options?.mode === 'counter') {
    const draft = uni.getStorageSync(QUOTE_DRAFT_KEY)
    if (draft && typeof draft === 'object') {
      if (draft.subjectType && !subjectType.value) subjectType.value = normalizeSubjectType(draft.subjectType)
      if (draft.subjectId && !subjectId.value) subjectId.value = Number(draft.subjectId)
      if (draft.productName || draft.categoryName) subjectName.value = draft.productName || draft.categoryName
      if (draft.unit) form.value.unit = String(draft.unit)
      if (draft.price != null) form.value.price = String(draft.price)
      if (draft.quantity != null) form.value.quantity = String(draft.quantity)
      if (draft.deliveryPlace) form.value.deliveryPlace = String(draft.deliveryPlace)
      if (draft.deliveryDate) form.value.deliveryDate = String(draft.deliveryDate)
      if (draft.deliveryMode) form.value.deliveryMode = String(draft.deliveryMode)
      if (draft.paymentMethod) form.value.paymentMethod = String(draft.paymentMethod)
      if (draft.packaging) form.value.packaging = String(draft.packaging)
      if (draft.invoiceType) form.value.invoiceType = String(draft.invoiceType)
      if (draft.remark) form.value.remark = String(draft.remark)
    }
    uni.removeStorageSync(QUOTE_DRAFT_KEY)
  }

  if (subjectType.value && subjectId.value) {
    try {
      if (subjectType.value === 'supply') {
        const s = await getSupply(subjectId.value)
        if (s) {
          supplyData.value = s
          if (!subjectName.value) subjectName.value = s.categoryName || ''
          if (!form.value.price && s.exFactoryPrice) form.value.price = String(s.exFactoryPrice)
          if (!form.value.quantity && s.quantity) form.value.quantity = String(s.quantity)
          if (!form.value.deliveryMode && s.deliveryMode) form.value.deliveryMode = s.deliveryMode
          if (!form.value.deliveryPlace && s.shipAddress) form.value.deliveryPlace = s.shipAddress
          if (!form.value.paymentMethod && s.paymentMethod) form.value.paymentMethod = s.paymentMethod
          if (!form.value.packaging && s.packaging) form.value.packaging = s.packaging
          if (!form.value.invoiceType && s.invoiceType) form.value.invoiceType = s.invoiceType
        }
      } else if (subjectType.value === 'requirement') {
        const r = await getRequirement(subjectId.value)
        if (r) {
          requirementData.value = r
          if (!subjectName.value) subjectName.value = r.categoryName || ''
          if (!form.value.price && r.expectedPrice) form.value.price = String(r.expectedPrice)
          if (!form.value.quantity && r.quantity) form.value.quantity = String(r.quantity)
          if (!form.value.deliveryMode && r.deliveryMethod) form.value.deliveryMode = r.deliveryMethod
          if (!form.value.deliveryPlace && r.purchaseAddress) form.value.deliveryPlace = r.purchaseAddress
          if (!form.value.paymentMethod && r.paymentMethod) form.value.paymentMethod = r.paymentMethod
          if (!form.value.packaging && r.packaging) form.value.packaging = r.packaging
        }
      }
    } catch {}
  }
})

function handleDeliveryModePick(e: any) { form.value.deliveryMode = deliveryModeOptions[e.detail.value] }
function handlePaymentPick(e: any) { form.value.paymentMethod = paymentOptions[e.detail.value] }
function handlePackagingPick(e: any) { form.value.packaging = packagingOptions[e.detail.value] }
function handleInvoicePick(e: any) { form.value.invoiceType = invoiceTypeOptions[e.detail.value] }
function handleDateChange(e: any) { form.value.deliveryDate = e.detail.value }

function previewImage(url: string) { uni.previewImage({ urls: productImages.value, current: url }) }

async function handleSubmit() {
  if (!canSubmit.value || submitting.value) return
  submitting.value = true
  try {
    const price = parseFloat(form.value.price)
    const quantity = parseFloat(form.value.quantity)
    if (isNaN(price) || price <= 0) { uni.showToast({ title: '请输入有效单价', icon: 'none' }); return }
    if (isNaN(quantity) || quantity <= 0) { uni.showToast({ title: '请输入有效数量', icon: 'none' }); return }

    const payloadJson = JSON.stringify({
      version: 'V1', price, quantity, unit: form.value.unit,
      deliveryPlace: form.value.deliveryPlace || undefined,
      deliveryDate: form.value.deliveryDate || undefined,
      deliveryMode: form.value.deliveryMode || undefined,
      paymentMethod: form.value.paymentMethod || undefined,
      packaging: form.value.packaging || undefined,
      invoiceType: form.value.invoiceType || undefined,
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
  } catch {} finally { submitting.value = false }
}
</script>

<template>
  <view class="qf">
    <WgNavBar title="发送报价" />

    <scroll-view scroll-y class="qf__body">

      <!-- ===== 商品横幅 ===== -->
      <view class="banner">
        <view class="banner__name-row">
          <WgIcon :name="subjectType === 'supply' ? 'store' : 'shopping-bag'" :size="16" :color="BRAND_600" />
          <text class="banner__name">{{ subjectName || '商品' }}</text>
        </view>
        <view v-if="productImages.length" class="banner__imgs">
          <image
            v-for="(img, i) in productImages" :key="i"
            class="banner__img" :src="img" mode="aspectFill" @tap="previewImage(img)"
          />
        </view>
        <view v-if="productParams.length" class="banner__tags">
          <view v-for="(p, i) in productParams" :key="i" class="banner__tag">
            <text class="banner__tag-k">{{ p.key }}</text>
            <text class="banner__tag-v">{{ p.value }}{{ p.unit || '' }}</text>
          </view>
        </view>
        <view v-if="origin || storageMethod" class="banner__meta">
          <text v-if="origin" class="banner__meta-t">📍 {{ origin }}</text>
          <text v-if="storageMethod" class="banner__meta-t">🏪 {{ storageMethod }}</text>
        </view>
      </view>

      <!-- ===== 主表单卡片 ===== -->
      <view class="form-card">

        <!-- 价格区域 -->
        <view class="section section--hero">
          <view class="price-grid">
            <view class="price-grid__cell">
              <text class="price-grid__label">单价 <text class="req">*</text></text>
              <view class="price-grid__input">
                <text class="price-grid__symbol">¥</text>
                <input v-model="form.price" class="price-grid__inp" type="digit" placeholder="0.00" />
                <text class="price-grid__suffix">/{{ form.unit }}</text>
              </view>
            </view>
            <view class="price-grid__divider" />
            <view class="price-grid__cell">
              <text class="price-grid__label">数量 <text class="req">*</text></text>
              <view class="price-grid__input">
                <input v-model="form.quantity" class="price-grid__inp" type="digit" placeholder="0" />
                <text class="price-grid__suffix">{{ form.unit }}</text>
              </view>
            </view>
          </view>
          <view v-if="totalPreview" class="total-bar">
            <text class="total-bar__label">合计金额</text>
            <text class="total-bar__amount">¥{{ totalPreview }}</text>
          </view>
        </view>

        <!-- 分割线 -->
        <view class="sep" />

        <!-- 交易条件 2×2 网格 -->
        <view class="section">
          <text class="section__title">交易条件</text>
          <view class="grid-2x2">
            <view class="grid-2x2__cell">
              <text class="grid-2x2__label">交货方式</text>
              <picker :range="deliveryModeOptions" @change="handleDeliveryModePick">
                <view class="pick-compact">
                  <text class="pick-compact__text" :class="{ 'pick-compact__text--ph': !form.deliveryMode }">{{ form.deliveryMode || '选择' }}</text>
                  <WgIcon name="chevron-down" :size="12" :color="WARM_400" />
                </view>
              </picker>
            </view>
            <view class="grid-2x2__cell">
              <text class="grid-2x2__label">付款方式</text>
              <picker :range="paymentOptions" @change="handlePaymentPick">
                <view class="pick-compact">
                  <text class="pick-compact__text" :class="{ 'pick-compact__text--ph': !form.paymentMethod }">{{ form.paymentMethod || '选择' }}</text>
                  <WgIcon name="chevron-down" :size="12" :color="WARM_400" />
                </view>
              </picker>
            </view>
            <view class="grid-2x2__cell">
              <text class="grid-2x2__label">包装方式</text>
              <picker :range="packagingOptions" @change="handlePackagingPick">
                <view class="pick-compact">
                  <text class="pick-compact__text" :class="{ 'pick-compact__text--ph': !form.packaging }">{{ form.packaging || '选择' }}</text>
                  <WgIcon name="chevron-down" :size="12" :color="WARM_400" />
                </view>
              </picker>
            </view>
            <view class="grid-2x2__cell">
              <text class="grid-2x2__label">发票类型</text>
              <picker :range="invoiceTypeOptions" @change="handleInvoicePick">
                <view class="pick-compact">
                  <text class="pick-compact__text" :class="{ 'pick-compact__text--ph': !form.invoiceType }">{{ form.invoiceType || '选择' }}</text>
                  <WgIcon name="chevron-down" :size="12" :color="WARM_400" />
                </view>
              </picker>
            </view>
          </view>
        </view>

        <!-- 分割线 -->
        <view class="sep" />

        <!-- 交付信息 -->
        <view class="section">
          <text class="section__title">交付信息</text>
          <view class="field-row">
            <view class="field-row__icon">
              <WgIcon name="map-pin" :size="14" :color="WARM_500" />
            </view>
            <input v-model="form.deliveryPlace" class="field-row__input" placeholder="交付地点（可选）" />
          </view>
          <view class="field-row">
            <view class="field-row__icon">
              <WgIcon name="calendar" :size="14" :color="WARM_500" />
            </view>
            <picker mode="date" @change="handleDateChange" class="field-row__picker">
              <text class="field-row__pick-text" :class="{ 'field-row__pick-text--ph': !form.deliveryDate }">
                {{ form.deliveryDate || '到货日期（可选）' }}
              </text>
            </picker>
          </view>
        </view>

        <!-- 分割线 -->
        <view class="sep" />

        <!-- 备注 -->
        <view class="section section--last">
          <text class="section__title">备注</text>
          <textarea
            v-model="form.remark"
            class="remark-area"
            placeholder="补充说明（可选）"
            :maxlength="200"
            :auto-height="true"
          />
        </view>
      </view>

      <!-- ===== 提交 ===== -->
      <view class="submit-btn" :class="{ 'submit-btn--disabled': !canSubmit }" @tap="handleSubmit">
        <WgIcon name="send" :size="18" color="#fff" />
        <text class="submit-btn__text">{{ submitting ? '发送中...' : '发送报价' }}</text>
      </view>

      <view style="height: 60rpx" />
    </scroll-view>
  </view>
</template>

<style lang="scss" scoped>
.qf {
  min-height: 100vh;
  background: $bg-page;

  &__body {
    height: calc(100vh - 44px);
    padding: 0 $spacing-sm;
  }
}

/* ===== 商品横幅 ===== */
.banner {
  background: $bg-card;
  border-radius: $radius-lg;
  margin-top: $spacing-sm;
  padding: 16rpx 20rpx;

  &__name-row {
    display: flex;
    align-items: center;
    gap: 8rpx;
    margin-bottom: 8rpx;
  }

  &__name {
    font-size: $font-md;
    font-weight: 700;
    color: $text-primary;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__imgs {
    display: flex;
    gap: 10rpx;
    margin-bottom: 8rpx;
    overflow-x: auto;
  }

  &__img {
    width: 90rpx;
    height: 90rpx;
    border-radius: $radius-md;
    flex-shrink: 0;
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8rpx;
    margin-bottom: 6rpx;
  }

  &__tag {
    display: inline-flex;
    align-items: center;
    gap: 4rpx;
    padding: 2rpx 12rpx;
    background: $brand-50;
    border-radius: $radius-pill;
  }

  &__tag-k { font-size: 20rpx; color: $text-secondary; }
  &__tag-v { font-size: 20rpx; font-weight: 600; color: $brand-600; }

  &__meta {
    display: flex;
    gap: 16rpx;
    flex-wrap: wrap;
  }

  &__meta-t {
    font-size: 22rpx;
    color: $text-secondary;
  }
}

/* ===== 主表单卡片 ===== */
.form-card {
  background: $bg-card;
  border-radius: $radius-lg;
  margin-top: $spacing-sm;
  overflow: hidden;
}

.section {
  padding: 16rpx 20rpx;

  &--hero { padding-bottom: 0; }
  &--last { padding-bottom: 20rpx; }

  &__title {
    font-size: 22rpx;
    font-weight: 700;
    color: $text-secondary;
    margin-bottom: 12rpx;
    display: block;
    letter-spacing: 1rpx;
  }
}

.sep {
  height: 1rpx;
  background: $border-light;
  margin: 0 20rpx;
}

.req { color: $accent-400; font-weight: 400; }

/* ===== 价格网格 ===== */
.price-grid {
  display: flex;
  align-items: stretch;

  &__cell { flex: 1; padding: 8rpx 0 16rpx; min-width: 0; }

  &__divider {
    width: 1rpx;
    background: $border-light;
    margin: 8rpx 16rpx;
    align-self: stretch;
  }

  &__label {
    font-size: $font-xs;
    font-weight: 600;
    color: $text-secondary;
    display: block;
    margin-bottom: 6rpx;
  }

  &__input {
    display: flex;
    align-items: center;
    gap: 4rpx;
  }

  &__symbol {
    font-size: 36rpx;
    font-weight: 800;
    color: $accent-400;
    flex-shrink: 0;
  }

  &__inp {
    flex: 1;
    height: 60rpx;
    font-size: 36rpx;
    font-weight: 700;
    color: $text-primary;
    padding: 0;
    min-width: 0;
  }

  &__suffix {
    font-size: $font-sm;
    color: $text-placeholder;
    flex-shrink: 0;
  }
}

/* ===== 合计 ===== */
.total-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10rpx 0 14rpx;
  border-top: 1rpx solid $border-light;

  &__label { font-size: $font-xs; color: $text-secondary; }
  &__amount { font-size: 32rpx; font-weight: 800; color: $accent-400; }
}

/* ===== 2×2 网格 ===== */
.grid-2x2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rpx;
  background: $border-light;
  border-radius: $radius-md;
  overflow: hidden;

  &__cell {
    background: $bg-page;
    padding: 14rpx 16rpx;
  }

  &__label {
    font-size: 20rpx;
    color: $text-placeholder;
    display: block;
    margin-bottom: 4rpx;
  }
}

.pick-compact {
  display: flex;
  align-items: center;
  justify-content: space-between;

  &__text {
    font-size: $font-sm;
    color: $text-primary;
    font-weight: 600;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    &--ph { color: $text-placeholder; font-weight: 400; }
  }
}

/* ===== 带图标字段行 ===== */
.field-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 8rpx 0;
  border-bottom: 1rpx solid $border-light;

  &:last-child { border-bottom: none; }

  &__icon { flex-shrink: 0; width: 32rpx; display: flex; justify-content: center; }

  &__input {
    flex: 1;
    height: 52rpx;
    font-size: $font-md;
    color: $text-primary;
    padding: 0;
  }

  &__picker { flex: 1; }

  &__pick-text {
    font-size: $font-md;
    color: $text-primary;
    line-height: 52rpx;
    &--ph { color: $text-placeholder; }
  }
}

/* ===== 备注 ===== */
.remark-area {
  width: 100%;
  min-height: 60rpx;
  font-size: $font-md;
  color: $text-primary;
  line-height: 1.5;
  padding: 0;
}

/* ===== 提交 ===== */
.submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  height: 88rpx;
  margin: $spacing-md 0 0;
  background: $brand-600;
  border-radius: $radius-lg;
  transition: transform 0.12s;

  &:active { transform: scale(0.97); }
  &--disabled { opacity: 0.45; pointer-events: none; }

  &__text { font-size: $font-lg; font-weight: 700; color: #fff; }
}
</style>
