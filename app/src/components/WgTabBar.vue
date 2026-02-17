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
}

const tabs: TabItem[] = [
  { path: '/pages/home/index', label: '首页', icon: 'home' },
  { path: '/pages/supply/index', label: '供应', icon: 'store' },
  { path: '/pages/requirement/index', label: '采购', icon: 'shopping-bag' },
  { path: '/pages/chat/index', label: '消息', icon: 'message-circle' },
  { path: '/pages/profile/index', label: '我的', icon: 'user' },
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
      <view class="tab-bar__icon-wrap">
        <WgIcon
          :name="tab.icon"
          :size="22"
          :color="index === current ? '#2D6A4F' : '#A8A29E'"
          :stroke-width="index === current ? 2.2 : 1.75"
        />
        <view v-if="props.badges?.[index]" class="tab-bar__badge">
          <text class="tab-bar__badge-text">{{ props.badges[index]! > 99 ? '99+' : props.badges[index] }}</text>
        </view>
      </view>
      <text class="tab-bar__label" :class="{ 'tab-bar__label--active': index === current }">{{ tab.label }}</text>
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
  align-items: center;
  justify-content: space-around;
  height: 110rpx;
  background: #ffffff;
  border-top: 1rpx solid rgba(0, 0, 0, 0.05);
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  box-shadow: 0 -2rpx 16rpx rgba(0, 0, 0, 0.03);

  &__item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 110rpx;
    gap: 4rpx;
    padding-top: 10rpx;
    transition: opacity 0.15s;

    &:active { opacity: 0.7; }
  }

  &__icon-wrap {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__badge {
    position: absolute;
    top: -6rpx;
    right: -14rpx;
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

  &__label {
    font-size: 20rpx;
    color: #A8A29E;
    line-height: 1.2;

    &--active {
      color: $brand-600;
      font-weight: 600;
    }
  }
}
</style>
