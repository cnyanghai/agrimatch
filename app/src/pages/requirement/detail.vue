<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getRequirement, type RequirementResponse } from '../../api/requirement'
import { openConversation } from '../../api/chat'
import { formatPrice, formatDateTime } from '../../utils/format'
import { useAuthStore } from '../../store/auth'

const authStore = useAuthStore()
const detail = ref<RequirementResponse | null>(null)
const loading = ref(true)

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
    })
    const peerName = detail.value.companyName || detail.value.nickName || detail.value.userName || ''
    uni.navigateTo({
      url: `/pages/chat/conversation?id=${conversationId}&peerId=${detail.value.userId}&name=${encodeURIComponent(peerName)}`,
    })
  } catch {
    uni.showToast({ title: '打开会话失败', icon: 'none' })
  }
}

function handleViewCompany() {
  if (detail.value?.companyId) {
    uni.navigateTo({ url: `/pages/company/detail?id=${detail.value.companyId}` })
  }
}

</script>

<template>
  <view class="detail-page">
    <WgSkeleton v-if="loading" type="detail" />
    <WgEmpty v-else-if="!detail" text="采购信息不存在" icon="empty" />

    <template v-else>
      <view class="info-card">
        <text class="info-card__name">{{ detail.categoryName }}</text>
        <text class="info-card__price">{{ formatPrice(detail.expectedPrice) }}</text>
        <text class="info-card__unit">元/吨 · 期望价格</text>
      </view>

      <view class="detail-card">
        <view class="detail-row" v-if="detail.companyName">
          <text class="detail-row__label">企业</text>
          <text class="detail-row__value">{{ detail.companyName }}</text>
        </view>
        <view class="detail-row" v-if="detail.quantity">
          <text class="detail-row__label">数量</text>
          <text class="detail-row__value">{{ detail.quantity }} 吨</text>
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
        <view class="detail-row">
          <text class="detail-row__label">发布时间</text>
          <text class="detail-row__value">{{ formatDateTime(detail.createTime) }}</text>
        </view>
      </view>

      <view class="bottom-bar safe-area-bottom">
        <button class="btn-secondary" @tap="handleViewCompany"><uni-icons type="shop" size="18" color="#D4A373" /> 企业信息</button>
        <button class="btn-primary" @tap="handleContact"><uni-icons type="chat" size="18" color="#fff" /> 联系采购方</button>
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
  border-radius: $radius-lg;
  padding: $spacing-lg;

  &__name {
    font-size: $font-xl;
    font-weight: bold;
    color: $text-primary;
    display: block;
    margin-bottom: $spacing-sm;
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

.detail-card {
  background: $bg-card;
  margin: 0 $spacing-sm;
  border-radius: $radius-lg;
  padding: $spacing-sm 0;
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
  padding: $spacing-sm $spacing-md;
  background: $bg-card;
  border-top: 1rpx solid $border-light;
}

.btn-primary {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  background: $autumn-400;
  color: #fff;
  border: none;
  border-radius: $radius-lg;
  font-size: $font-md;
  font-weight: bold;
}

.btn-secondary {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  background: $bg-page;
  color: $autumn-500;
  border: 1rpx solid $autumn-200;
  border-radius: $radius-lg;
  font-size: $font-md;
}
</style>
