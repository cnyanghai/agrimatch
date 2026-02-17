<script setup lang="ts">
import { ref, computed } from 'vue'
import { WARM_300, WARM_500, WHITE } from '../../constants/colors'
import { onLoad } from '@dcloudio/uni-app'
import { getRequirement, type RequirementResponse } from '../../api/requirement'
import { openConversation } from '../../api/chat'
import { formatPrice, formatDateTime } from '../../utils/format'
import { parseParams, type ParsedParam } from '../../utils/parseParams'
import { getUnitLabel } from '../../utils/unitConfig'
import { useAuthStore } from '../../store/auth'

const authStore = useAuthStore()
const detail = ref<RequirementResponse | null>(null)
const loading = ref(true)

/** Parse dynamic params from paramsJson (unified parser) */
const dynamicParams = computed<ParsedParam[]>(() => {
  return parseParams(detail.value?.paramsJson)
})

/** Dynamic unit labels */
const quantityUnit = computed(() =>
  getUnitLabel(detail.value?.schemaCode, 'quantity', detail.value?.categoryName)
)
const priceUnit = computed(() =>
  getUnitLabel(detail.value?.schemaCode, 'price', detail.value?.categoryName)
)

/** Status label: 0=发布中, 1=部分成交, 2=已下架, 3=已成交 */
const statusInfo = computed(() => {
  if (!detail.value) return null
  const status = detail.value.status
  const expireTime = detail.value.expireTime
  const now = Date.now()
  const isExpired = expireTime && new Date(expireTime).getTime() < now

  if (status === 2 || isExpired) {
    return { label: '已过期', color: 'status--expired' }
  }
  if (status === 3) {
    return { label: '已成交', color: 'status--expired' }
  }
  if (status === 1) {
    return { label: '部分成交', color: 'status--active' }
  }
  if (status === 0) {
    return { label: '有效', color: 'status--active' }
  }
  return null
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

/** Preview image */
function handlePreviewImage(index: number) {
  uni.previewImage({
    current: index,
    urls: imageList.value,
  })
}

/** Remaining quantity */
const remainingQtyText = computed(() => {
  if (!detail.value) return ''
  const { quantity, remainingQuantity } = detail.value
  if (remainingQuantity !== null && remainingQuantity !== undefined && quantity) {
    if (remainingQuantity <= 0) return '已满足'
    return `${remainingQuantity} / ${quantity} ${quantityUnit.value}`
  }
  return ''
})

onLoad(async (options) => {
  if (options?.id) {
    try {
      detail.value = await getRequirement(Number(options.id))
    } catch {
      // handled
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
      subjectType: 'NEED',
      subjectId: detail.value.id,
      subjectSnapshotJson: JSON.stringify({
        categoryName: detail.value.categoryName,
        expectedPrice: detail.value.expectedPrice,
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
    title: `采购: ${detail.value?.categoryName || ''}`,
    summary: `${formatPrice(detail.value?.expectedPrice)} ${priceUnit.value} - ${detail.value?.companyName || ''}`,
    success() {
      uni.showToast({ title: '分享成功', icon: 'success' })
    },
    fail() {
      uni.setClipboardData({
        data: `采购: ${detail.value?.categoryName} - ${formatPrice(detail.value?.expectedPrice)} ${priceUnit.value}`,
        success() {
          uni.showToast({ title: '已复制到剪贴板', icon: 'success' })
        },
      })
    },
  })
  // #endif
  // #ifdef H5
  uni.setClipboardData({
    data: `采购: ${detail.value?.categoryName} - ${formatPrice(detail.value?.expectedPrice)} ${priceUnit.value}`,
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
    <WgEmpty v-else-if="!detail" text="采购信息不存在" icon="empty" />

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
          <text v-if="statusInfo" class="info-card__status" :class="statusInfo.color">{{ statusInfo.label }}</text>
        </view>
        <text class="info-card__price">{{ formatPrice(detail.expectedPrice) }}</text>
        <text class="info-card__unit">{{ priceUnit }} · 期望价格</text>
      </view>

      <!-- 详情信息 -->
      <view class="detail-card">
        <view class="detail-row" v-if="detail.companyName">
          <text class="detail-row__label">企业</text>
          <text class="detail-row__value">{{ detail.companyName }}</text>
        </view>
        <view class="detail-row" v-if="detail.quantity">
          <text class="detail-row__label">需求数量</text>
          <text class="detail-row__value">{{ detail.quantity }} {{ quantityUnit }}</text>
        </view>
        <view class="detail-row" v-if="remainingQtyText">
          <text class="detail-row__label">剩余数量</text>
          <text class="detail-row__value" :class="{ 'detail-row__value--accent': remainingQtyText === '已满足' }">{{ remainingQtyText }}</text>
        </view>
        <view class="detail-row" v-if="detail.purchaseAddress">
          <text class="detail-row__label">收货地</text>
          <text class="detail-row__value">{{ detail.purchaseAddress }}</text>
        </view>
        <view class="detail-row" v-if="detail.deliveryMethod">
          <text class="detail-row__label">交货方式</text>
          <text class="detail-row__value">{{ detail.deliveryMethod }}</text>
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
          <text class="detail-card__title-text">质量要求</text>
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

      <!-- 企业信息卡片 -->
      <view v-if="detail.companyName" class="company-card" @tap="goCompany">
        <view class="company-card__avatar">
          <text class="company-card__initial">{{ (detail.companyName || '?')[0] }}</text>
        </view>
        <view class="company-card__info">
          <text class="company-card__name">{{ detail.companyName }}</text>
          <text class="company-card__hint">点击查看企业详情</text>
        </view>
        <WgIcon name="right" :size="16" :color="WARM_300" />
      </view>

      <!-- 分享按钮 -->
      <view class="share-bar">
        <view class="share-btn" @tap="handleShare">
          <WgIcon name="share" :size="16" :color="WARM_500" />
          <text class="share-btn__text">分享</text>
        </view>
      </view>

      <!-- 底部操作 -->
      <view class="bottom-bar safe-area-bottom">
        <button class="btn-secondary" @tap="handleCall"><WgIcon name="phone" :size="18" color="#D4A373" /> 电话咨询</button>
        <button class="btn-primary" @tap="handleContact"><WgIcon name="message-circle" :size="18" :color="WHITE" /> 联系采购方</button>
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

  &__status {
    font-size: $font-xs;
    padding: 4rpx 16rpx;
    border-radius: $radius-sm;
    font-weight: 600;
  }

  &__price {
    font-size: 64rpx;
    font-weight: bold;
    color: $autumn-500;
    display: block;
  }

  &__unit {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

.status--active {
  color: $brand-600;
  background: $brand-50;
}

.status--expired {
  color: $text-placeholder;
  background: $warm-100;
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

    &--accent {
      color: $brand-600;
      font-weight: 600;
    }
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

  &:active {
    opacity: 0.85;
  }

  &__avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: $autumn-50;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__initial {
    font-size: $font-xl;
    font-weight: bold;
    color: $autumn-500;
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
  background: $autumn-400;
  color: $text-inverse;
  border: none;
  border-radius: $radius-xl;
  font-size: $font-md;
  font-weight: bold;
  box-shadow: 0 4rpx 16rpx rgba(212, 163, 115, 0.25);
  transition: transform $transition-fast;

  &:active {
    transform: scale(0.95);
  }
}

.btn-secondary {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  background: $autumn-50;
  color: $autumn-500;
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
