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
const filterMode = ref<'all' | 'unread' | 'read'>('all')

const filteredConversations = computed(() => {
  if (filterMode.value === 'unread') {
    return conversations.value.filter(c => (c.unreadCount || 0) > 0)
  }
  if (filterMode.value === 'read') {
    return conversations.value.filter(c => !c.unreadCount)
  }
  return conversations.value
})

const totalUnread = computed(() =>
  conversations.value.reduce((sum, c) => sum + (c.unreadCount || 0), 0)
)

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

function goSupply() {
  uni.switchTab({ url: '/pages/supply/index' })
}

function goRequirement() {
  uni.navigateTo({ url: '/pages/requirement/index' })
}
</script>

<template>
  <view class="chat-page">
    <!-- 未登录 -->
    <WgEmpty v-if="!isLoggedIn" text="登录后查看消息" icon="auth" actionText="去登录" @action="goLogin" />

    <template v-else>
      <!-- 筛选 tabs -->
      <view class="filter-bar">
        <view class="filter-bar__pills">
          <text
            class="filter-bar__pill"
            :class="{ 'filter-bar__pill--active': filterMode === 'all' }"
            @tap="filterMode = 'all'"
          >全部</text>
          <text
            class="filter-bar__pill"
            :class="{ 'filter-bar__pill--active': filterMode === 'unread' }"
            @tap="filterMode = 'unread'"
          >
            未读
            <text v-if="totalUnread > 0" class="filter-bar__count">{{ totalUnread > 99 ? '99+' : totalUnread }}</text>
          </text>
          <text
            class="filter-bar__pill"
            :class="{ 'filter-bar__pill--active': filterMode === 'read' }"
            @tap="filterMode = 'read'"
          >已读</text>
        </view>
      </view>

      <!-- 会话列表 -->
      <view v-if="filteredConversations.length > 0" class="conv-list">
        <view
          v-for="conv in filteredConversations"
          :key="conv.id"
          class="conv-item"
          @tap="goConversation(conv)"
        >
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
      <view v-else-if="!loading" class="empty-state">
        <view class="empty-state__icon">
          <uni-icons type="chat" size="48" color="#d1d5db" />
        </view>
        <text class="empty-state__text">
          {{ filterMode === 'unread' ? '没有未读消息' : filterMode === 'read' ? '没有已读消息' : '还没有对话' }}
        </text>
        <text v-if="filterMode === 'all'" class="empty-state__desc">去供应大厅或采购大厅找合作伙伴吧</text>
        <view v-if="filterMode === 'all'" class="empty-state__actions">
          <view class="empty-state__btn empty-state__btn--brand" @tap="goSupply">
            <text class="empty-state__btn-text">去供应</text>
          </view>
          <view class="empty-state__btn empty-state__btn--autumn" @tap="goRequirement">
            <text class="empty-state__btn-text">去采购</text>
          </view>
        </view>
      </view>

      <WgSkeleton v-if="loading" type="list" :rows="5" :avatar="true" />
    </template>

    <WgTabBar :current="3" />
  </view>
</template>

<style lang="scss" scoped>
.chat-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: 130rpx;
}

/* ===== Filter Bar ===== */
.filter-bar {
  position: sticky;
  top: 0;
  z-index: 10;
  background: $bg-card;
  padding: $spacing-sm $spacing-md;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);

  &__pills {
    display: flex;
    gap: $spacing-xs;
  }

  &__pill {
    font-size: $font-sm;
    color: $text-secondary;
    padding: $spacing-xs $spacing-md;
    border-radius: 30rpx;
    background: $bg-page;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    gap: 6rpx;

    &--active {
      color: $brand-600;
      background: $brand-50;
      font-weight: 600;
    }
  }

  &__count {
    font-size: 20rpx;
    color: #fff;
    background: $color-error;
    border-radius: 16rpx;
    padding: 0 8rpx;
    min-width: 28rpx;
    height: 28rpx;
    line-height: 28rpx;
    text-align: center;
  }
}

/* ===== Conversation List ===== */
.conv-list {
  background: $bg-card;
}

.conv-item {
  display: flex;
  align-items: center;
  padding: $spacing-md;
  border-bottom: 1rpx solid $border-light;
  gap: $spacing-md;
  transition: background 0.15s;

  &:active {
    background: $bg-hover;
  }

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

/* ===== Empty State ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx $spacing-lg;

  &__icon {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background: $bg-card;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: $spacing-md;
    box-shadow: $shadow-sm;
  }

  &__text {
    font-size: $font-lg;
    color: $text-primary;
    font-weight: 600;
    margin-bottom: $spacing-xs;
  }

  &__desc {
    font-size: $font-sm;
    color: $text-secondary;
    margin-bottom: $spacing-lg;
  }

  &__actions {
    display: flex;
    gap: $spacing-md;
  }

  &__btn {
    padding: $spacing-sm $spacing-xl;
    border-radius: 30rpx;
    transition: transform 0.15s;

    &:active {
      transform: scale(0.95);
    }

    &--brand {
      background: $brand-600;
    }

    &--autumn {
      background: $autumn-400;
    }
  }

  &__btn-text {
    font-size: $font-md;
    color: #fff;
    font-weight: 600;
  }
}
</style>
