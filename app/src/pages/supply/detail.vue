<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getSupply, type SupplyResponse } from '../../api/supply'
import { openConversation } from '../../api/chat'
import { formatPrice, formatDateTime } from '../../utils/format'
import { useAuthStore } from '../../store/auth'

const authStore = useAuthStore()
const detail = ref<SupplyResponse | null>(null)
const loading = ref(true)

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

</script>

<template>
  <view class="detail-page">
    <WgSkeleton v-if="loading" type="detail" />

    <WgEmpty v-else-if="!detail" text="供应信息不存在" icon="empty" />

    <template v-else>
      <!-- 基础信息 -->
      <view class="info-card">
        <view class="info-card__header">
          <text class="info-card__name">{{ detail.categoryName }}</text>
          <text v-if="detail.origin" class="info-card__origin">{{ detail.origin }}</text>
        </view>
        <text class="info-card__price">{{ formatPrice(detail.exFactoryPrice) }}</text>
        <text class="info-card__unit">元/吨 · 出厂价</text>
      </view>

      <!-- 详情信息 -->
      <view class="detail-card">
        <view class="detail-row" v-if="detail.companyName">
          <text class="detail-row__label">企业</text>
          <text class="detail-row__value">{{ detail.companyName }}</text>
        </view>
        <view class="detail-row" v-if="detail.quantity">
          <text class="detail-row__label">数量</text>
          <text class="detail-row__value">{{ detail.quantity }} 吨</text>
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
        <view class="detail-row">
          <text class="detail-row__label">发布时间</text>
          <text class="detail-row__value">{{ formatDateTime(detail.createTime) }}</text>
        </view>
      </view>

      <!-- 底部操作 -->
      <view class="bottom-bar safe-area-bottom">
        <button class="btn-secondary" @tap="handleCall"><uni-icons type="phone" size="18" color="#2D6A4F" /> 电话咨询</button>
        <button class="btn-primary" @tap="handleContact"><uni-icons type="chat" size="18" color="#fff" /> 在线聊天</button>
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
  }

  &__price {
    font-size: 64rpx;
    font-weight: bold;
    color: $accent-400;
    display: block;
  }

  &__unit {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

.detail-card {
  background: $bg-card;
  margin: $spacing-sm $spacing-sm 0;
  border-radius: $radius-xl;
  padding: $spacing-sm 0;
  box-shadow: $shadow-sm;
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
