<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import {
  listMyNotifications,
  markNotificationRead,
  markAllNotificationsRead,
  type NotifyResponse,
} from '../../api/notify'
import { formatRelativeTime } from '../../utils/format'

const list = ref<NotifyResponse[]>([])
const loading = ref(false)
const filterMode = ref<'all' | 'unread' | 'read'>('all')

const filteredList = computed(() => {
  if (filterMode.value === 'unread') return list.value.filter(n => !n.read)
  if (filterMode.value === 'read') return list.value.filter(n => n.read)
  return list.value
})

const unreadCount = computed(() => list.value.filter(n => !n.read).length)

onShow(() => {
  loadData()
})

onPullDownRefresh(() => {
  loadData().finally(() => uni.stopPullDownRefresh())
})

async function loadData() {
  loading.value = true
  try {
    list.value = await listMyNotifications() || []
  } catch {
    // handled by request.ts
  } finally {
    loading.value = false
  }
}

function handleTap(item: NotifyResponse) {
  if (!item.read) {
    markNotificationRead(item.id)
    item.read = true
  }
  if (item.link) {
    uni.navigateTo({ url: item.link })
  }
}

async function handleReadAll() {
  try {
    await markAllNotificationsRead()
    list.value.forEach(n => { n.read = true })
    uni.showToast({ title: '全部已读', icon: 'none' })
  } catch {
    // handled
  }
}

/** 通知类型图标配置 */
function getNotifyStyle(type: string): { icon: string; bgClass: string } {
  switch (type) {
    case 'LIKE': return { icon: 'heart-filled', bgClass: 'notify-icon--like' }
    case 'COMMENT': return { icon: 'chat', bgClass: 'notify-icon--comment' }
    case 'MESSAGE': return { icon: 'email', bgClass: 'notify-icon--message' }
    default: return { icon: 'info', bgClass: 'notify-icon--system' }
  }
}
</script>

<template>
  <view class="notify-page">
    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view class="filter-bar__tabs">
        <view
          class="filter-bar__tab"
          :class="{ 'filter-bar__tab--active': filterMode === 'all' }"
          @tap="filterMode = 'all'"
        >
          <text>全部</text>
        </view>
        <view
          class="filter-bar__tab"
          :class="{ 'filter-bar__tab--active': filterMode === 'unread' }"
          @tap="filterMode = 'unread'"
        >
          <text>未读{{ unreadCount > 0 ? `(${unreadCount})` : '' }}</text>
        </view>
        <view
          class="filter-bar__tab"
          :class="{ 'filter-bar__tab--active': filterMode === 'read' }"
          @tap="filterMode = 'read'"
        >
          <text>已读</text>
        </view>
      </view>
      <view v-if="unreadCount > 0" class="filter-bar__action" @tap="handleReadAll">
        <text class="filter-bar__action-text">全部已读</text>
      </view>
    </view>

    <!-- 通知列表 -->
    <view v-if="filteredList.length > 0" class="notify-list">
      <view
        v-for="item in filteredList"
        :key="item.id"
        class="notify-card tap-feedback"
        :class="{ 'notify-card--unread': !item.read }"
        @tap="handleTap(item)"
      >
        <view class="notify-icon" :class="getNotifyStyle(item.type).bgClass">
          <uni-icons :type="getNotifyStyle(item.type).icon" size="18" color="#fff" />
        </view>
        <view class="notify-card__body">
          <text class="notify-card__title">{{ item.title }}</text>
          <text v-if="item.content" class="notify-card__content">{{ item.content }}</text>
          <text class="notify-card__time">{{ formatRelativeTime(item.createTime) }}</text>
        </view>
        <view v-if="!item.read" class="notify-card__dot" />
      </view>
    </view>

    <!-- 空状态 -->
    <WgEmpty v-else-if="!loading" text="暂无通知" description="当有新消息时会在这里提醒你" />

    <!-- 骨架屏 -->
    <WgSkeleton v-if="loading && list.length === 0" type="list" :rows="5" />
  </view>
</template>

<style lang="scss" scoped>
.notify-page {
  min-height: 100vh;
  background: $bg-page;
}

/* ===== Filter bar ===== */
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: $bg-card;
  padding: $spacing-sm $spacing-md;
  border-bottom: 1rpx solid $border-light;

  &__tabs {
    display: flex;
    gap: $spacing-md;
  }

  &__tab {
    font-size: $font-md;
    color: $text-secondary;
    padding-bottom: $spacing-xs;
    position: relative;

    &--active {
      color: $brand-600;
      font-weight: bold;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 40rpx;
        height: 4rpx;
        background: $brand-600;
        border-radius: 2rpx;
      }
    }
  }

  &__action {
    padding: 8rpx 20rpx;
    background: $brand-50;
    border-radius: 100rpx;
  }

  &__action-text {
    font-size: $font-xs;
    color: $brand-600;
  }
}

/* ===== Notify list ===== */
.notify-list {
  padding: $spacing-sm;
}

.notify-card {
  display: flex;
  align-items: flex-start;
  gap: $spacing-sm;
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;
  position: relative;

  &--unread {
    background: $brand-50;
  }

  &__body {
    flex: 1;
    min-width: 0;
  }

  &__title {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
    display: block;
    margin-bottom: 4rpx;
  }

  &__content {
    font-size: $font-sm;
    color: $text-secondary;
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-bottom: 4rpx;
  }

  &__time {
    font-size: $font-xs;
    color: $text-placeholder;
  }

  &__dot {
    width: 16rpx;
    height: 16rpx;
    border-radius: 50%;
    background: $color-error;
    flex-shrink: 0;
    margin-top: $spacing-xs;
  }
}

/* ===== Notify icon ===== */
.notify-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &--like {
    background: $accent-400;
  }

  &--comment {
    background: $action-600;
  }

  &--message {
    background: $autumn-400;
  }

  &--system {
    background: $brand-600;
  }
}
</style>
