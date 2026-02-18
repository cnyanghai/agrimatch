<script setup lang="ts">
import { ref, computed } from 'vue'
import { BRAND_600, WARM_500, WHITE } from '../../constants/colors'
import { onLoad } from '@dcloudio/uni-app'
import { getSupply, type SupplyResponse, type BasisQuoteResponse } from '../../api/supply'
import { openConversation } from '../../api/chat'
import { formatDateTime } from '../../utils/format'
import { parseParams, type ParsedParam } from '../../utils/parseParams'
import { getUnitLabel } from '../../utils/unitConfig'
import { useAuthStore } from '../../store/auth'

const authStore = useAuthStore()
const detail = ref<SupplyResponse | null>(null)
const loading = ref(true)

const dynamicParams = computed<ParsedParam[]>(() => parseParams(detail.value?.paramsJson))

const priceRules = computed(() => {
  if (!detail.value?.priceRulesJson) return []
  try {
    const parsed = JSON.parse(detail.value.priceRulesJson)
    return Array.isArray(parsed) ? parsed : []
  } catch { return [] }
})

const imageList = computed<string[]>(() => {
  if (!detail.value?.imagesJson) return []
  try {
    const arr = JSON.parse(detail.value.imagesJson)
    return Array.isArray(arr) ? arr : []
  } catch { return [] }
})

const quantityUnit = computed(() =>
  getUnitLabel(detail.value?.schemaCode, 'quantity', detail.value?.categoryName)
)
const priceUnit = computed(() =>
  getUnitLabel(detail.value?.schemaCode, 'price', detail.value?.categoryName)
)

const isBasisQuote = computed(() => detail.value?.priceType === 1)

const basisQuotes = computed<BasisQuoteResponse[]>(() => {
  if (!detail.value?.basisQuotes || !Array.isArray(detail.value.basisQuotes)) return []
  return detail.value.basisQuotes
})

function formatBasisPrice(price: number): string {
  return price >= 0 ? `+${price}` : `${price}`
}

type ChipVariant = 'brand' | 'autumn' | 'accent' | 'success' | 'warning' | 'error' | 'neutral'

const statusInfo = computed<{ label: string; variant: ChipVariant } | null>(() => {
  if (!detail.value) return null
  const status = detail.value.status
  const expireTime = detail.value.expireTime
  const now = Date.now()
  const isExpired = expireTime && new Date(expireTime).getTime() < now
  if (status === 2 || isExpired) return { label: '已过期', variant: 'neutral' }
  if (status === 3) return { label: '已成交', variant: 'neutral' }
  if (status === 1) return { label: '部分成交', variant: 'autumn' }
  if (status === 0) return { label: '有效', variant: 'success' }
  return null
})

const infoRows = computed(() => {
  if (!detail.value) return []
  const d = detail.value
  const rows: { label: string; value: string }[] = []
  if (d.companyName) rows.push({ label: '企业', value: d.companyName })
  if (d.quantity) rows.push({ label: '数量', value: `${d.quantity} ${quantityUnit.value}` })
  if (d.remainingQuantity != null && d.quantity) {
    rows.push({ label: '剩余数量', value: `${d.remainingQuantity} / ${d.quantity} ${quantityUnit.value}` })
  }
  if (d.shipAddress) rows.push({ label: '发货地', value: d.shipAddress })
  if (d.deliveryMode) rows.push({ label: '交货方式', value: d.deliveryMode })
  if (d.paymentMethod) rows.push({ label: '付款方式', value: d.paymentMethod })
  if (d.remark) rows.push({ label: '备注', value: d.remark })
  if (d.expireTime) rows.push({ label: '有效期', value: formatDateTime(d.expireTime) })
  rows.push({ label: '发布时间', value: formatDateTime(d.createTime) })
  return rows
})

function handlePreviewImage(index: number) {
  uni.previewImage({ current: index, urls: imageList.value })
}

onLoad(async (options) => {
  if (options?.id) {
    try {
      detail.value = await getSupply(Number(options.id))
    } catch { /* handled */ } finally {
      loading.value = false
    }
  }
})

async function handleContact() {
  if (!authStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  if (!detail.value) return
  if (detail.value.userId === authStore.user?.userId) {
    uni.showToast({ title: '不能和自己聊天', icon: 'none' })
    return
  }
  try {
    const conversationId = await openConversation({
      peerUserId: detail.value.userId,
      subjectType: 'SUPPLY',
      subjectId: detail.value.id,
      subjectSnapshotJson: JSON.stringify({
        categoryName: detail.value.categoryName,
        exFactoryPrice: detail.value.exFactoryPrice,
        quantity: detail.value.quantity,
        unit: priceUnit.value,
        companyName: detail.value.companyName,
      }),
    })
    const peerName = detail.value.companyName || detail.value.nickName || detail.value.userName || ''
    uni.navigateTo({
      url: `/pages/chat/conversation?id=${conversationId}&peerId=${detail.value.userId}&name=${encodeURIComponent(peerName)}`,
    })
  } catch {
    uni.showToast({ title: '打开会话失败', icon: 'none' })
  }
}

function handleCall() {
  if (detail.value?.companyId) {
    uni.navigateTo({ url: `/pages/company/detail?id=${detail.value.companyId}` })
  } else {
    uni.showToast({ title: '暂无联系电话', icon: 'none' })
  }
}

function goCompany() {
  if (detail.value?.companyId) {
    uni.navigateTo({ url: `/pages/company/detail?id=${detail.value.companyId}` })
  }
}

function handleShare() {
  const text = `供应: ${detail.value?.categoryName} - ${detail.value?.exFactoryPrice ?? '面议'} ${priceUnit.value}`
  // #ifdef H5
  uni.setClipboardData({ data: text, success() { uni.showToast({ title: '已复制到剪贴板', icon: 'success' }) } })
  // #endif
  // #ifdef APP-PLUS
  uni.setClipboardData({ data: text, success() { uni.showToast({ title: '已复制到剪贴板', icon: 'success' }) } })
  // #endif
}
</script>

<template>
  <view class="detail-page">
    <WgNavBar title="供应详情">
      <template #right>
        <view class="nav-share tap-feedback" @tap="handleShare">
          <WgIcon name="share" :size="18" :color="WARM_500" />
        </view>
      </template>
    </WgNavBar>

    <WgSkeleton v-if="loading" type="detail" />
    <WgEmpty v-else-if="!detail" text="供应信息不存在" icon="empty" />

    <scroll-view v-else scroll-y class="detail-scroll">
      <!-- 商品图片 -->
      <view v-if="imageList.length > 0" class="image-section stitch-card">
        <scroll-view scroll-x class="image-section__scroll">
          <view class="image-section__list">
            <image
              v-for="(img, idx) in imageList"
              :key="idx"
              class="image-section__item"
              :src="img"
              mode="aspectFill"
              @tap="handlePreviewImage(idx)"
            />
          </view>
        </scroll-view>
      </view>

      <!-- 核心信息卡片 -->
      <view class="hero-card stitch-card stitch-card--elevated stitch-scale-in">
        <view class="hero-card__header">
          <text class="hero-card__name">{{ detail.categoryName }}</text>
          <WgStatusChip v-if="detail.origin" :label="detail.origin" variant="brand" size="sm" />
          <WgStatusChip v-if="isBasisQuote" label="基差报价" variant="autumn" size="sm" />
          <WgStatusChip v-if="statusInfo" :label="statusInfo.label" :variant="statusInfo.variant" size="sm" dot />
        </view>

        <template v-if="!isBasisQuote">
          <WgPriceTag :value="detail.exFactoryPrice" :unit="`${priceUnit} · 出厂价`" size="lg" />
        </template>
        <template v-else>
          <view class="hero-card__basis">
            <text class="hero-card__basis-label">基差定价</text>
            <text class="hero-card__basis-hint">价格由期货价格 + 基差决定</text>
          </view>
        </template>
      </view>

      <!-- 基差报价明细 -->
      <view v-if="isBasisQuote && basisQuotes.length > 0" class="section-card stitch-card">
        <view class="section-card__title">
          <text class="stitch-section-title">基差报价明细</text>
        </view>
        <view v-for="bq in basisQuotes" :key="bq.id" class="basis-row">
          <view class="basis-row__header">
            <text class="basis-row__contract">{{ bq.contractName || bq.contractCode }}</text>
            <text
              class="basis-row__price"
              :class="bq.basisPrice >= 0 ? 'basis-row__price--up' : 'basis-row__price--down'"
            >基差 {{ formatBasisPrice(bq.basisPrice) }}</text>
          </view>
          <view class="basis-row__meta">
            <view v-if="bq.lastPrice != null" class="basis-row__item">
              <text class="basis-row__label">期货价</text>
              <text class="basis-row__value">¥{{ bq.lastPrice }}</text>
            </view>
            <view v-if="bq.referencePrice != null" class="basis-row__item">
              <text class="basis-row__label">参考价</text>
              <text class="basis-row__value basis-row__value--brand">¥{{ bq.referencePrice }}</text>
            </view>
            <view class="basis-row__item">
              <text class="basis-row__label">可售量</text>
              <text class="basis-row__value">{{ bq.remainingQty ?? bq.availableQty }}{{ quantityUnit }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 详情信息 -->
      <view class="section-card stitch-card">
        <view v-for="(row, idx) in infoRows" :key="idx" class="info-row">
          <text class="info-row__label">{{ row.label }}</text>
          <text class="info-row__value">{{ row.value }}</text>
        </view>
      </view>

      <!-- 动态参数 -->
      <view v-if="dynamicParams.length > 0" class="section-card stitch-card">
        <view class="section-card__title">
          <text class="stitch-section-title">产品参数</text>
        </view>
        <view v-for="(p, idx) in dynamicParams" :key="idx" class="info-row">
          <text class="info-row__label">{{ p.key }}</text>
          <text class="info-row__value">{{ p.value }}{{ p.unit ? ` ${p.unit}` : '' }}</text>
        </view>
      </view>

      <!-- 价格规则 -->
      <view v-if="priceRules.length > 0" class="section-card stitch-card">
        <view class="section-card__title">
          <text class="stitch-section-title">价格规则</text>
        </view>
        <view v-for="(rule, idx) in priceRules" :key="idx" class="info-row">
          <text class="info-row__label">{{ rule.label || rule.key || `规则${idx + 1}` }}</text>
          <text class="info-row__value info-row__value--accent">{{ rule.value }}</text>
        </view>
      </view>

      <!-- 企业卡片 -->
      <view v-if="detail.companyName" class="company-card stitch-card tap-feedback" @tap="goCompany">
        <WgAvatar :name="detail.companyName" size="md" />
        <view class="company-card__info">
          <text class="company-card__name">{{ detail.companyName }}</text>
          <text class="company-card__hint">点击查看企业详情</text>
        </view>
        <WgIcon name="chevron-right" :size="16" color="#D6CCC0" />
      </view>

      <view style="height: 160rpx" />
    </scroll-view>

    <!-- 底部操作 -->
    <WgActionBar>
      <button class="wg-btn wg-btn--secondary" @tap="handleCall">
        <WgIcon name="phone" :size="18" :color="BRAND_600" />
        <text>电话咨询</text>
      </button>
      <button class="wg-btn wg-btn--primary" @tap="handleContact">
        <WgIcon name="message-circle" :size="18" :color="WHITE" />
        <text>在线聊天</text>
      </button>
    </WgActionBar>
  </view>
</template>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  background: $bg-page;
}

.detail-scroll {
  height: 100vh;
}

.nav-share {
  width: 72rpx;
  height: 72rpx;
  border-radius: $radius-full;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Image gallery */
.image-section {
  margin: $spacing-sm $spacing-sm 0;
  padding: 0;
  overflow: hidden;

  &__scroll { white-space: nowrap; }

  &__list {
    display: flex;
    gap: $spacing-xs;
    padding: $spacing-sm;
  }

  &__item {
    width: 400rpx;
    height: 300rpx;
    border-radius: $radius-md;
    flex-shrink: 0;
  }
}

/* Hero card */
.hero-card {
  margin: $spacing-sm;
  padding: $spacing-lg;

  &__header {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: $spacing-xs;
    margin-bottom: $spacing-md;
  }

  &__name {
    font-size: $font-xl;
    font-weight: 800;
    color: $text-primary;
    margin-right: $spacing-xs;
  }

  &__basis {
    padding-top: $spacing-xs;
  }

  &__basis-label {
    font-size: 48rpx;
    font-weight: 800;
    color: $autumn-600;
    display: block;
  }

  &__basis-hint {
    font-size: $font-sm;
    color: $text-secondary;
    margin-top: 4rpx;
  }
}

/* Section card */
.section-card {
  margin: $spacing-sm;
  padding: $spacing-sm 0;

  &__title {
    padding: $spacing-sm $spacing-lg;
    border-bottom: 1rpx solid $border-light;
    margin-bottom: $spacing-xs;
  }
}

.info-row {
  display: flex;
  padding: $spacing-sm $spacing-lg;

  &__label {
    width: 160rpx;
    font-size: $font-md;
    color: $text-secondary;
    flex-shrink: 0;
  }

  &__value {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;

    &--accent {
      color: $accent-400;
      font-weight: 600;
    }
  }
}

/* Basis quote row */
.basis-row {
  padding: $spacing-sm $spacing-lg;
  border-bottom: 1rpx solid $border-light;

  &:last-child { border-bottom: none; }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-xs;
  }

  &__contract {
    font-size: $font-md;
    font-weight: 700;
    color: $text-primary;
  }

  &__price {
    font-size: $font-md;
    font-weight: 700;

    &--up { color: $color-error; }
    &--down { color: $color-success; }
  }

  &__meta {
    display: flex;
    gap: $spacing-lg;
    flex-wrap: wrap;
  }

  &__item {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
  }

  &__label {
    font-size: $font-sm;
    color: $text-secondary;
  }

  &__value {
    font-size: $font-sm;
    color: $text-primary;
    font-weight: 500;

    &--brand {
      color: $brand-600;
      font-weight: 700;
    }
  }
}

/* Company card */
.company-card {
  margin: $spacing-sm;
  padding: $spacing-lg;
  display: flex;
  align-items: center;
  gap: $spacing-md;

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__name {
    font-size: $font-md;
    font-weight: 600;
    color: $text-primary;
    display: block;
  }

  &__hint {
    font-size: $font-xs;
    color: $text-placeholder;
    margin-top: 4rpx;
  }
}
</style>
