<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getSupply, type SupplyResponse, type BasisQuoteResponse } from '../../api/supply'
import { openConversation } from '../../api/chat'
import { formatPrice, formatDateTime } from '../../utils/format'
import { parseParams, type ParsedParam } from '../../utils/parseParams'
import { getUnitLabel } from '../../utils/unitConfig'
import { useAuthStore } from '../../store/auth'

const authStore = useAuthStore()
const detail = ref<SupplyResponse | null>(null)
const loading = ref(true)

/** Parse dynamic params from paramsJson (unified parser) */
const dynamicParams = computed<ParsedParam[]>(() => {
  return parseParams(detail.value?.paramsJson)
})

/** Parse price rules */
const priceRules = computed(() => {
  if (!detail.value?.priceRulesJson) return []
  try {
    const parsed = JSON.parse(detail.value.priceRulesJson)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
})

/** Parse images */
const imageList = computed<string[]>(() => {
  if (!detail.value?.imagesJson) return []
  try {
    const arr = JSON.parse(detail.value.imagesJson)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
})

/** Dynamic unit labels */
const quantityUnit = computed(() =>
  getUnitLabel(detail.value?.schemaCode, 'quantity', detail.value?.categoryName)
)
const priceUnit = computed(() =>
  getUnitLabel(detail.value?.schemaCode, 'price', detail.value?.categoryName)
)

/** Is basis quote type */
const isBasisQuote = computed(() => detail.value?.priceType === 1)

/** Basis quotes list (Task 2) */
const basisQuotes = computed<BasisQuoteResponse[]>(() => {
  if (!detail.value?.basisQuotes || !Array.isArray(detail.value.basisQuotes)) return []
  return detail.value.basisQuotes
})

/** Format basis price with +/- sign */
function formatBasisPrice(price: number): string {
  return price >= 0 ? `+${price}` : `${price}`
}

/** Status info */
const statusInfo = computed(() => {
  if (!detail.value) return null
  const status = detail.value.status
  const expireTime = detail.value.expireTime
  const now = Date.now()

  if (status === 0 || (expireTime && new Date(expireTime).getTime() < now)) {
    return { label: '已过期', color: 'status--expired' }
  }
  if (status === 1) {
    return { label: '有效', color: 'status--active' }
  }
  return null
})

/** Preview image */
function handlePreviewImage(index: number) {
  uni.previewImage({
    current: index,
    urls: imageList.value,
  })
}

onLoad(async (options) => {
  if (options?.id) {
    try {
      detail.value = await getSupply(Number(options.id))
    } catch {
      // handled by request.ts
    } finally {
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
  // #ifdef APP-PLUS
  uni.share({
    provider: 'weixin',
    type: 0,
    title: `供应: ${detail.value?.categoryName || ''}`,
    summary: `${formatPrice(detail.value?.exFactoryPrice)} ${priceUnit.value} - ${detail.value?.companyName || ''}`,
    success() {
      uni.showToast({ title: '分享成功', icon: 'success' })
    },
    fail() {
      // Fallback: copy link
      uni.setClipboardData({
        data: `供应: ${detail.value?.categoryName} - ${formatPrice(detail.value?.exFactoryPrice)} ${priceUnit.value}`,
        success() {
          uni.showToast({ title: '已复制到剪贴板', icon: 'success' })
        },
      })
    },
  })
  // #endif
  // #ifdef H5
  uni.setClipboardData({
    data: `供应: ${detail.value?.categoryName} - ${formatPrice(detail.value?.exFactoryPrice)} ${priceUnit.value}`,
    success() {
      uni.showToast({ title: '已复制到剪贴板', icon: 'success' })
    },
  })
  // #endif
}

</script>

<template>
  <view class="detail-page">
    <WgSkeleton v-if="loading" type="detail" />

    <WgEmpty v-else-if="!detail" text="供应信息不存在" icon="empty" />

    <template v-else>
      <!-- 商品图片 -->
      <view v-if="imageList.length > 0" class="image-section">
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

      <!-- 基础信息 -->
      <view class="info-card">
        <view class="info-card__header">
          <text class="info-card__name">{{ detail.categoryName }}</text>
          <text v-if="detail.origin" class="info-card__origin">{{ detail.origin }}</text>
          <text v-if="isBasisQuote" class="info-card__origin info-card__origin--basis">基差报价</text>
          <text v-if="statusInfo" class="info-card__status" :class="statusInfo.color">{{ statusInfo.label }}</text>
        </view>
        <!-- 一口价显示价格 (Task 2) -->
        <template v-if="!isBasisQuote">
          <text class="info-card__price">{{ formatPrice(detail.exFactoryPrice) }}</text>
          <text class="info-card__unit">{{ priceUnit }} · 出厂价</text>
        </template>
        <!-- 基差报价显示"基差定价" (Task 2) -->
        <template v-else>
          <text class="info-card__basis-label">基差定价</text>
          <text class="info-card__unit">价格由期货价格 + 基差决定</text>
        </template>
      </view>

      <!-- 基差报价详情 (Task 2) -->
      <view v-if="isBasisQuote && basisQuotes.length > 0" class="detail-card">
        <view class="detail-card__title">
          <text class="detail-card__title-text">基差报价明细</text>
        </view>
        <view
          v-for="bq in basisQuotes"
          :key="bq.id"
          class="basis-quote-row"
        >
          <view class="basis-quote-row__header">
            <text class="basis-quote-row__contract">{{ bq.contractName || bq.contractCode }}</text>
            <text
              class="basis-quote-row__basis"
              :class="bq.basisPrice >= 0 ? 'basis-quote-row__basis--up' : 'basis-quote-row__basis--down'"
            >基差 {{ formatBasisPrice(bq.basisPrice) }}</text>
          </view>
          <view class="basis-quote-row__details">
            <view v-if="bq.lastPrice != null" class="basis-quote-row__item">
              <text class="basis-quote-row__label">期货价</text>
              <text class="basis-quote-row__value">¥{{ bq.lastPrice }}</text>
            </view>
            <view v-if="bq.referencePrice != null" class="basis-quote-row__item">
              <text class="basis-quote-row__label">参考价</text>
              <text class="basis-quote-row__value basis-quote-row__value--highlight">¥{{ bq.referencePrice }}</text>
            </view>
            <view class="basis-quote-row__item">
              <text class="basis-quote-row__label">可售量</text>
              <text class="basis-quote-row__value">{{ bq.remainingQty ?? bq.availableQty }}{{ quantityUnit }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 详情信息 -->
      <view class="detail-card">
        <view class="detail-row" v-if="detail.companyName">
          <text class="detail-row__label">企业</text>
          <text class="detail-row__value">{{ detail.companyName }}</text>
        </view>
        <view class="detail-row" v-if="detail.quantity">
          <text class="detail-row__label">数量</text>
          <text class="detail-row__value">{{ detail.quantity }} {{ quantityUnit }}</text>
        </view>
        <view class="detail-row" v-if="detail.remainingQuantity !== undefined && detail.remainingQuantity !== null && detail.quantity">
          <text class="detail-row__label">剩余数量</text>
          <text class="detail-row__value">{{ detail.remainingQuantity }} / {{ detail.quantity }} {{ quantityUnit }}</text>
        </view>
        <view class="detail-row" v-if="detail.shipAddress">
          <text class="detail-row__label">发货地</text>
          <text class="detail-row__value">{{ detail.shipAddress }}</text>
        </view>
        <view class="detail-row" v-if="detail.deliveryMode">
          <text class="detail-row__label">交货方式</text>
          <text class="detail-row__value">{{ detail.deliveryMode }}</text>
        </view>
        <view class="detail-row" v-if="detail.paymentMethod">
          <text class="detail-row__label">付款方式</text>
          <text class="detail-row__value">{{ detail.paymentMethod }}</text>
        </view>
        <view class="detail-row" v-if="detail.remark">
          <text class="detail-row__label">备注</text>
          <text class="detail-row__value">{{ detail.remark }}</text>
        </view>
        <view class="detail-row" v-if="detail.expireTime">
          <text class="detail-row__label">有效期</text>
          <text class="detail-row__value">{{ formatDateTime(detail.expireTime) }}</text>
        </view>
        <view class="detail-row">
          <text class="detail-row__label">发布时间</text>
          <text class="detail-row__value">{{ formatDateTime(detail.createTime) }}</text>
        </view>
      </view>

      <!-- 动态参数 -->
      <view v-if="dynamicParams.length > 0" class="detail-card">
        <view class="detail-card__title">
          <text class="detail-card__title-text">产品参数</text>
        </view>
        <view
          v-for="(p, idx) in dynamicParams"
          :key="idx"
          class="detail-row"
        >
          <text class="detail-row__label">{{ p.key }}</text>
          <text class="detail-row__value">{{ p.value }}{{ p.unit ? ` ${p.unit}` : '' }}</text>
        </view>
      </view>

      <!-- 价格规则 -->
      <view v-if="priceRules.length > 0" class="detail-card">
        <view class="detail-card__title">
          <text class="detail-card__title-text">价格规则</text>
        </view>
        <view
          v-for="(rule, idx) in priceRules"
          :key="idx"
          class="detail-row"
        >
          <text class="detail-row__label">{{ rule.label || rule.key || `规则${idx + 1}` }}</text>
          <text class="detail-row__value detail-row__value--price">{{ rule.value }}</text>
        </view>
      </view>

      <!-- 企业信息卡片 -->
      <view v-if="detail.companyName" class="company-card tap-feedback" @tap="goCompany">
        <view class="company-card__avatar">
          <text class="company-card__initial">{{ (detail.companyName || '?')[0] }}</text>
        </view>
        <view class="company-card__info">
          <text class="company-card__name">{{ detail.companyName }}</text>
          <text class="company-card__hint">点击查看企业详情</text>
        </view>
        <WgIcon name="right" :size="16" color="#D6CCC0" />
      </view>

      <!-- 分享按钮 -->
      <view class="share-bar">
        <view class="share-btn" @tap="handleShare">
          <WgIcon name="share" :size="16" color="#78716C" />
          <text class="share-btn__text">分享</text>
        </view>
      </view>

      <!-- 底部操作 -->
      <view class="bottom-bar safe-area-bottom">
        <button class="btn-secondary" @tap="handleCall"><WgIcon name="phone" :size="18" color="#2D6A4F" /> 电话咨询</button>
        <button class="btn-primary" @tap="handleContact"><WgIcon name="message-circle" :size="18" color="#fff" /> 在线聊天</button>
      </view>
    </template>
  </view>
</template>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: 140rpx;
}

/* Image gallery */
.image-section {
  background: $bg-card;
  margin: $spacing-sm $spacing-sm 0;
  border-radius: $radius-xl;
  overflow: hidden;

  &__scroll {
    white-space: nowrap;
  }

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

/* Status badges */
.status--active {
  font-size: $font-xs;
  padding: 4rpx 16rpx;
  border-radius: $radius-sm;
  color: $brand-600;
  background: $brand-50;
}

.status--expired {
  font-size: $font-xs;
  padding: 4rpx 16rpx;
  border-radius: $radius-sm;
  color: $text-placeholder;
  background: $warm-100;
}

.info-card {
  background: $bg-card;
  margin: $spacing-sm;
  border-radius: $radius-xl;
  padding: $spacing-lg;
  box-shadow: $shadow-sm;

  &__header {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    margin-bottom: $spacing-sm;
  }

  &__name {
    font-size: $font-xl;
    font-weight: bold;
    color: $text-primary;
  }

  &__origin {
    font-size: $font-xs;
    color: $brand-600;
    background: $brand-50;
    padding: 4rpx 16rpx;
    border-radius: $radius-sm;

    &--basis {
      color: #B45309;
      background: #FFFBEB;
    }
  }

  &__price {
    font-size: 64rpx;
    font-weight: bold;
    color: $accent-400;
    display: block;
  }

  &__basis-label {
    font-size: 48rpx;
    font-weight: bold;
    color: #B45309;
    display: block;
  }

  &__unit {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

/* 基差报价行 (Task 2) */
.basis-quote-row {
  padding: $spacing-sm $spacing-lg;
  border-bottom: 1rpx solid $border-light;

  &:last-child {
    border-bottom: none;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-xs;
  }

  &__contract {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
  }

  &__basis {
    font-size: $font-md;
    font-weight: bold;

    &--up {
      color: #DC2626;
    }

    &--down {
      color: #16A34A;
    }
  }

  &__details {
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

    &--highlight {
      color: $brand-600;
      font-weight: bold;
    }
  }
}

.detail-card {
  background: $bg-card;
  margin: $spacing-sm $spacing-sm 0;
  border-radius: $radius-xl;
  padding: $spacing-sm 0;
  box-shadow: $shadow-sm;

  &__title {
    padding: $spacing-sm $spacing-lg;
    border-bottom: 1rpx solid $border-light;
    margin-bottom: $spacing-xs;
  }

  &__title-text {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
  }
}

.detail-row {
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
  }
}

/* Company card */
.company-card {
  background: $bg-card;
  margin: $spacing-sm;
  border-radius: $radius-xl;
  padding: $spacing-lg;
  display: flex;
  align-items: center;
  gap: $spacing-md;
  box-shadow: $shadow-sm;

  &__avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: $brand-50;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__initial {
    font-size: $font-xl;
    font-weight: bold;
    color: $brand-600;
  }

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
  }
}

/* Share bar */
.share-bar {
  padding: $spacing-sm $spacing-md;
  display: flex;
  justify-content: center;
}

.share-btn {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-lg;
  background: $warm-100;
  border-radius: $radius-pill;

  &:active { opacity: 0.7; }

  &__text {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

.detail-row__value--price {
  color: $accent-400;
  font-weight: 600;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: $spacing-sm;
  padding: $spacing-md $spacing-md;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(40rpx);
  -webkit-backdrop-filter: blur(40rpx);
  border-top: 1rpx solid rgba(0, 0, 0, 0.04);
}

.btn-primary {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  background: $brand-600;
  color: #fff;
  border: none;
  border-radius: $radius-xl;
  font-size: $font-md;
  font-weight: bold;
  box-shadow: $shadow-brand;
  transition: transform $transition-fast;

  &:active {
    transform: scale(0.95);
  }
}

.btn-secondary {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  background: $brand-50;
  color: $brand-600;
  border: none;
  border-radius: $radius-xl;
  font-size: $font-md;
  font-weight: 500;
  transition: transform $transition-fast;

  &:active {
    transform: scale(0.95);
  }
}
</style>
