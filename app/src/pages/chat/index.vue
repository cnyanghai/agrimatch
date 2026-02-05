<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { useAuthStore } from '../../store/auth'
import { listConversations, type ChatConversationResponse } from '../../api/chat'
import { listMyNotifications, type NotifyResponse } from '../../api/notify'
import { formatRelativeTime } from '../../utils/format'

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)
const conversations = ref<ChatConversationResponse[]>([])
const notifications = ref<NotifyResponse[]>([])
const loading = ref(false)

// 按通知类型统计未读数
const notifyCategories = computed(() => {
  const unread = notifications.value.filter(n => !n.read)
  const countByType = (types: string[]) =>
    unread.filter(n => types.includes(n.type)).length

  return [
    { key: 'like', label: '赞和收藏', icon: 'heart', count: countByType(['LIKE', 'COLLECT', 'like', 'collect']) },
    { key: 'comment', label: '评论回复', icon: 'chat', count: countByType(['COMMENT', 'REPLY', 'comment', 'reply']) },
    { key: 'contract', label: '合同动态', icon: 'list', count: countByType(['CONTRACT', 'MILESTONE', 'contract', 'milestone']) },
    { key: 'system', label: '系统通知', icon: 'notification', count: countByType(['SYSTEM', 'system', 'POINTS', 'points']) },
  ]
})

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
    const [convRes, notifyRes] = await Promise.allSettled([
      listConversations(),
      listMyNotifications(),
    ])
    if (convRes.status === 'fulfilled') conversations.value = convRes.value || []
    if (notifyRes.status === 'fulfilled') notifications.value = notifyRes.value || []
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function goConversation(conv: ChatConversationResponse) {
  uni.navigateTo({ url: `/pages/chat/conversation?id=${conv.id}&name=${encodeURIComponent(conv.peerNickName || conv.peerUserName || '')}` })
}

function goNotify(type: string) {
  uni.navigateTo({ url: `/pages/notify/index?type=${type}` })
}

function goLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}

function goSupply() {
  uni.switchTab({ url: '/pages/supply/index' })
}

function goRequirement() {
  uni.switchTab({ url: '/pages/requirement/index' })
}
</script>

<template>
  <view class="chat-page">
    <!-- 未登录 -->
    <WgEmpty v-if="!isLoggedIn" text="登录后查看消息" icon="auth" actionText="去登录" @action="goLogin" />

    <template v-else>
      <!-- 通知分类入口 -->
      <view class="notify-bar">
        <view
          v-for="cat in notifyCategories"
          :key="cat.key"
          class="notify-bar__item"
          @tap="goNotify(cat.key)"
        >
          <view class="notify-bar__icon-wrap">
            <uni-icons :type="cat.icon" size="24" color="#57534E" />
            <view v-if="cat.count > 0" class="notify-bar__badge">
              <text class="notify-bar__badge-text">{{ cat.count > 99 ? '99+' : cat.count }}</text>
            </view>
          </view>
          <text class="notify-bar__label">{{ cat.label }}</text>
        </view>
      </view>

      <!-- 会话列表标题 -->
      <view class="section-header">
        <text class="section-header__title">会话</text>
      </view>

      <!-- 会话列表 -->
      <view v-if="conversations.length > 0" class="conv-list">
        <view
          v-for="conv in conversations"
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
              <view v-if="conv.unreadCount" class="conv-item__unread">
                <text class="conv-item__unread-text">{{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}</text>
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
        <text class="empty-state__text">还没有对话</text>
        <text class="empty-state__desc">去供应大厅或采购大厅找合作伙伴吧</text>
        <view class="empty-state__actions">
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

/* ===== Notification Bar ===== */
.notify-bar {
  display: flex;
  justify-content: space-around;
  background: #ffffff;
  padding: $spacing-lg $spacing-md;
  margin-bottom: $spacing-sm;

  &__item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $spacing-xs;
  }

  &__icon-wrap {
    position: relative;
    width: 96rpx;
    height: 96rpx;
    border-radius: 50%;
    background: $warm-100;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: transform 0.15s;

    &:active {
      transform: scale(0.92);
    }
  }

  &__badge {
    position: absolute;
    top: -4rpx;
    right: -4rpx;
    min-width: 32rpx;
    height: 32rpx;
    background: $color-error;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 8rpx;
    border: 2rpx solid #fff;
  }

  &__badge-text {
    color: #fff;
    font-size: 18rpx;
    font-weight: bold;
    line-height: 32rpx;
  }

  &__label {
    font-size: $font-xs;
    color: $text-secondary;
  }
}

/* ===== Section Header ===== */
.section-header {
  padding: $spacing-sm $spacing-md;

  &__title {
    font-size: $font-lg;
    font-weight: 700;
    color: $text-primary;
  }
}

/* ===== Conversation List ===== */
.conv-list {
  background: #ffffff;
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

  &__unread {
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

  &__unread-text {
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
    background: #ffffff;
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
