<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { useAuthStore } from '../../store/auth'
import { listConversations, type ChatConversationResponse } from '../../api/chat'
import { formatRelativeTime } from '../../utils/format'

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)
const conversations = ref<ChatConversationResponse[]>([])
const loading = ref(false)

onMounted(() => {
  if (isLoggedIn.value) loadData()
})

onShow(() => {
  if (isLoggedIn.value) loadData()
})

onPullDownRefresh(() => {
  if (isLoggedIn.value) {
    loadData().finally(() => uni.stopPullDownRefresh())
  } else {
    uni.stopPullDownRefresh()
  }
})

async function loadData() {
  loading.value = true
  try {
    conversations.value = await listConversations() || []
  } catch {
    // not logged in or error
  } finally {
    loading.value = false
  }
}

function goConversation(conv: ChatConversationResponse) {
  uni.navigateTo({ url: `/pages/chat/conversation?id=${conv.id}&name=${encodeURIComponent(conv.peerNickName || conv.peerUserName || '')}` })
}

function goLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}
</script>

<template>
  <view class="chat-page">
    <!-- 未登录 -->
    <WgEmpty v-if="!isLoggedIn" text="登录后查看消息" icon="auth" actionText="去登录" @action="goLogin" />

    <!-- 会话列表 -->
    <view v-else-if="conversations.length > 0" class="conv-list">
      <view
        v-for="conv in conversations"
        :key="conv.id"
        class="conv-item"
        @tap="goConversation(conv)"
      >
        <!-- 头像占位 -->
        <view class="conv-item__avatar">
          <text class="conv-item__avatar-text">{{ (conv.peerNickName || conv.peerUserName || '?')[0] }}</text>
        </view>

        <view class="conv-item__content">
          <view class="conv-item__top">
            <text class="conv-item__name">{{ conv.peerNickName || conv.peerUserName }}</text>
            <text class="conv-item__time">{{ formatRelativeTime(conv.lastTime) }}</text>
          </view>
          <view class="conv-item__bottom">
            <text class="conv-item__msg">{{ conv.lastContent || '暂无消息' }}</text>
            <view v-if="conv.unreadCount" class="conv-item__badge">
              <text class="conv-item__badge-text">{{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}</text>
            </view>
          </view>
          <text v-if="conv.peerCompanyName" class="conv-item__company">{{ conv.peerCompanyName }}</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <WgEmpty v-else-if="!loading" text="暂无消息" description="去供应大厅或采购大厅找到合适的信息后，开始聊天吧" />

    <WgSkeleton v-if="loading" type="list" :rows="5" :avatar="true" />
  </view>
</template>

<style lang="scss" scoped>
.chat-page {
  min-height: 100vh;
  background: $bg-page;
}

.conv-list {
  background: $bg-card;
}

.conv-item {
  display: flex;
  align-items: center;
  padding: $spacing-md $spacing-md;
  border-bottom: 1rpx solid $border-light;
  gap: $spacing-md;

  &__avatar {
    width: 96rpx;
    height: 96rpx;
    border-radius: 50%;
    background: $brand-100;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__avatar-text {
    font-size: $font-lg;
    font-weight: bold;
    color: $brand-600;
  }

  &__content {
    flex: 1;
    min-width: 0;
  }

  &__top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 4rpx;
  }

  &__name {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
  }

  &__time {
    font-size: $font-xs;
    color: $text-placeholder;
    flex-shrink: 0;
  }

  &__bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  &__msg {
    font-size: $font-sm;
    color: $text-secondary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
  }

  &__badge {
    background: $color-error;
    border-radius: 20rpx;
    padding: 0 12rpx;
    height: 36rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    margin-left: $spacing-xs;
  }

  &__badge-text {
    color: #fff;
    font-size: $font-xs;
    font-weight: bold;
  }

  &__company {
    font-size: $font-xs;
    color: $text-placeholder;
    margin-top: 4rpx;
  }
}
</style>
