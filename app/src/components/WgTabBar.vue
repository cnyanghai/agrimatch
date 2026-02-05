<script setup lang="ts">
import { useAuthStore } from '../store/auth'

const props = defineProps<{
  current: number
  badges?: Record<number, number>
}>()

const authStore = useAuthStore()

interface TabItem {
  path: string
  label: string
  icon: string
  iconActive: string
}

const tabs: TabItem[] = [
  {
    path: '/pages/home/index',
    label: '首页',
    icon: '/static/tab/home.png',
    iconActive: '/static/tab/home-active.png'
  },
  {
    path: '/pages/supply/index',
    label: '供应',
    icon: '/static/tab/supply.png',
    iconActive: '/static/tab/supply-active.png'
  },
  {
    path: '/pages/requirement/index',
    label: '采购',
    icon: '/static/tab/requirement.png',
    iconActive: '/static/tab/requirement-active.png'
  },
  {
    path: '/pages/chat/index',
    label: '消息',
    icon: '/static/tab/chat.png',
    iconActive: '/static/tab/chat-active.png'
  },
  {
    path: '/pages/profile/index',
    label: '我的',
    icon: '/static/tab/profile.png',
    iconActive: '/static/tab/profile-active.png'
  }
]

function handleTab(index: number, tab: TabItem) {
  if (index === props.current) return

  // 消息和我的需要登录
  if ((index === 3 || index === 4) && !authStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }

  uni.switchTab({ url: tab.path })
}
</script>

<template>
  <view class="tab-bar safe-area-bottom">
    <view
      v-for="(tab, index) in tabs"
      :key="index"
      class="tab-bar__item"
      :class="{ 'tab-bar__item--active': index === current }"
      @tap="handleTab(index, tab)"
    >
      <view class="tab-bar__normal">
        <image
          class="tab-bar__icon"
          :src="index === current ? tab.iconActive : tab.icon"
          mode="aspectFit"
        />
        <view v-if="props.badges?.[index]" class="tab-bar__badge">
          <text class="tab-bar__badge-text">{{ props.badges[index] > 99 ? '99+' : props.badges[index] }}</text>
        </view>
        <text
          class="tab-bar__label"
          :class="{ 'tab-bar__label--active': index === current }"
        >{{ tab.label }}</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 999;
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 110rpx;
  background: #ffffff;
  border-top: 1rpx solid $warm-200;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);

  &__item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 110rpx;
  }

  &__normal {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4rpx;
    padding-top: 10rpx;
    position: relative;
  }

  &__badge {
    position: absolute;
    top: 4rpx;
    right: 16rpx;
    min-width: 28rpx;
    height: 28rpx;
    background: $color-error;
    border-radius: 14rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 6rpx;
  }

  &__badge-text {
    color: #fff;
    font-size: 18rpx;
    font-weight: bold;
    line-height: 28rpx;
  }

  &__icon {
    width: 48rpx;
    height: 48rpx;
  }

  &__label {
    font-size: 20rpx;
    color: $warm-400;
    line-height: 1.2;

    &--active {
      color: $brand-600;
      font-weight: 600;
    }
  }
}
</style>
