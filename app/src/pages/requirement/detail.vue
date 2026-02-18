<script setup lang="ts">
import { ref, computed } from 'vue'
import { AUTUMN_500, WARM_500, WHITE } from '../../constants/colors'
import { onLoad } from '@dcloudio/uni-app'
import { getRequirement, type RequirementResponse } from '../../api/requirement'
import { openConversation } from '../../api/chat'
import { formatDateTime } from '../../utils/format'
import { parseParams, type ParsedParam } from '../../utils/parseParams'
import { getUnitLabel } from '../../utils/unitConfig'
import { useAuthStore } from '../../store/auth'

const authStore = useAuthStore()
const detail = ref<RequirementResponse | null>(null)
const loading = ref(true)

const dynamicParams = computed<ParsedParam[]>(() => parseParams(detail.value?.paramsJson))

const quantityUnit = computed(() =>
  getUnitLabel(detail.value?.schemaCode, 'quantity', detail.value?.categoryName)
)
const priceUnit = computed(() =>
  getUnitLabel(detail.value?.schemaCode, 'price', detail.value?.categoryName)
)

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

const imageList = computed<string[]>(() => {
  if (!detail.value?.imagesJson) return []
  try {
    const arr = JSON.parse(detail.value.imagesJson)
    return Array.isArray(arr) ? arr : []
  } catch { return [] }
})

function handlePreviewImage(index: number) {
  uni.previewImage({ current: index, urls: imageList.value })
}

const remainingQtyText = computed(() => {
  if (!detail.value) return ''
  const { quantity, remainingQuantity } = detail.value
  if (remainingQuantity != null && quantity) {
    if (remainingQuantity <= 0) return '已满足'
    return `${remainingQuantity} / ${quantity} ${quantityUnit.value}`
  }
  return ''
})

const infoRows = computed(() => {
  if (!detail.value) return []
  const d = detail.value
  const rows: { label: string; value: string; accent?: boolean }[] = []
  if (d.companyName) rows.push({ label: '企业', value: d.companyName })
  if (d.quantity) rows.push({ label: '需求数量', value: `${d.quantity} ${quantityUnit.value}` })
  if (remainingQtyText.value) rows.push({ label: '剩余数量', value: remainingQtyText.value, accent: remainingQtyText.value === '已满足' })
  if (d.purchaseAddress) rows.push({ label: '收货地', value: d.purchaseAddress })
  if (d.deliveryMethod) rows.push({ label: '交货方式', value: d.deliveryMethod })
  if (d.paymentMethod) rows.push({ label: '付款方式', value: d.paymentMethod })
  if (d.remark) rows.push({ label: '备注', value: d.remark })
  if (d.expireTime) rows.push({ label: '有效期', value: formatDateTime(d.expireTime) })
  rows.push({ label: '发布时间', value: formatDateTime(d.createTime) })
  return rows
})

onLoad(async (options) => {
  if (options?.id) {
    try {
      detail.value = await getRequirement(Number(options.id))
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
  const text = `采购: ${detail.value?.categoryName} - ${detail.value?.expectedPrice ?? '面议'} ${priceUnit.value}`
  uni.setClipboardData({ data: text, success() { uni.showToast({ title: '已复制到剪贴板', icon: 'success' }) } })
}
</script>

<template>
  <view class="detail-page">
    <WgNavBar title="采购详情">
      <template #right>
        <view class="nav-share tap-feedback" @tap="handleShare">
          <WgIcon name="share" :size="18" :color="WARM_500" />
        </view>
      </template>
    </WgNavBar>

    <WgSkeleton v-if="loading" type="detail" />
    <WgEmpty v-else-if="!detail" text="采购信息不存在" icon="empty" />

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

      <!-- 核心信息 -->
      <view class="hero-card stitch-card stitch-card--elevated stitch-scale-in">
        <view class="hero-card__header">
          <text class="hero-card__name">{{ detail.categoryName }}</text>
          <WgStatusChip v-if="statusInfo" :label="statusInfo.label" :variant="statusInfo.variant" size="sm" dot />
        </view>
        <WgPriceTag :value="detail.expectedPrice" :unit="`${priceUnit} · 期望价格`" size="lg" />
      </view>

      <!-- 详情 -->
      <view class="section-card stitch-card">
        <view v-for="(row, idx) in infoRows" :key="idx" class="info-row">
          <text class="info-row__label">{{ row.label }}</text>
          <text class="info-row__value" :class="{ 'info-row__value--accent': row.accent }">{{ row.value }}</text>
        </view>
      </view>

      <!-- 动态参数 -->
      <view v-if="dynamicParams.length > 0" class="section-card stitch-card">
        <view class="section-card__title">
          <text class="stitch-section-title">质量要求</text>
        </view>
        <view v-for="(p, idx) in dynamicParams" :key="idx" class="info-row">
          <text class="info-row__label">{{ p.key }}</text>
          <text class="info-row__value">{{ p.value }}{{ p.unit ? ` ${p.unit}` : '' }}</text>
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
      <button class="wg-btn wg-btn--autumn-soft" @tap="handleCall">
        <WgIcon name="phone" :size="18" :color="AUTUMN_500" />
        <text>电话咨询</text>
      </button>
      <button class="wg-btn wg-btn--autumn" @tap="handleContact">
        <WgIcon name="message-circle" :size="18" :color="WHITE" />
        <text>联系采购方</text>
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

.image-section {
  margin: $spacing-sm $spacing-sm 0;
  padding: 0;
  overflow: hidden;

  &__scroll { white-space: nowrap; }
  &__list { display: flex; gap: $spacing-xs; padding: $spacing-sm; }
  &__item { width: 400rpx; height: 300rpx; border-radius: $radius-md; flex-shrink: 0; }
}

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
}

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
      color: $brand-600;
      font-weight: 600;
    }
  }
}

.company-card {
  margin: $spacing-sm;
  padding: $spacing-lg;
  display: flex;
  align-items: center;
  gap: $spacing-md;

  &__info { flex: 1; min-width: 0; }
  &__name { font-size: $font-md; font-weight: 600; color: $text-primary; display: block; }
  &__hint { font-size: $font-xs; color: $text-placeholder; margin-top: 4rpx; }
}

/* 采购侧定制按钮 */
:deep(.wg-btn--autumn) {
  background: $autumn-400;
  color: $text-inverse;
  box-shadow: 0 4rpx 14rpx rgba(212, 163, 115, 0.25);
}

:deep(.wg-btn--autumn-soft) {
  background: $autumn-50;
  color: $autumn-500;
}
</style>
