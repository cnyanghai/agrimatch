<script setup lang="ts">
import { useAuthStore } from '../store/auth'
import { BRAND_600, WARM_400 } from '../constants/colors'

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
  if ((index === 3 || index === 4) && !authStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  uni.switchTab({ url: tab.path })
}
</script>

<template>
  <view class="tab-bar safe-area-bottom">
    <view class="tab-bar__inner">
      <view
        v-for="(tab, index) in tabs"
        :key="index"
        class="tab-bar__item"
        @tap="handleTab(index, tab)"
      >
        <view
          class="tab-bar__pill"
          :class="{ 'tab-bar__pill--active': index === current }"
        >
          <WgIcon
            :name="tab.icon"
            :size="20"
            :color="index === current ? BRAND_600 : WARM_400"
            :stroke-width="index === current ? 2.2 : 1.6"
          />
          <text
            v-if="index === current"
            class="tab-bar__label"
          >{{ tab.label }}</text>
        </view>
        <view v-if="props.badges?.[index]" class="tab-bar__badge">
          <text class="tab-bar__badge-text">{{ props.badges[index]! > 99 ? '99+' : props.badges[index] }}</text>
        </view>
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
  padding: 0 $spacing-md $spacing-sm;
  padding-bottom: calc(#{$spacing-sm} + constant(safe-area-inset-bottom));
  padding-bottom: calc(#{$spacing-sm} + env(safe-area-inset-bottom));

  &__inner {
    display: flex;
    align-items: center;
    justify-content: space-around;
    height: 108rpx;
    background: rgba(255, 255, 255, 0.88);
    backdrop-filter: blur(40rpx) saturate(180%);
    -webkit-backdrop-filter: blur(40rpx) saturate(180%);
    border-radius: $radius-full;
    box-shadow: 0 4rpx 24rpx rgba(120, 90, 50, 0.08), 0 0 1rpx rgba(0, 0, 0, 0.05);
    padding: 0 $spacing-xs;
  }

  &__item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    height: 108rpx;
    transition: opacity $transition-fast;

    &:active { opacity: 0.7; }
  }

  &__pill {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-xs;
    height: 72rpx;
    padding: 0 $spacing-md;
    border-radius: $radius-full;
    transition: all 300ms cubic-bezier(0.34, 1.56, 0.64, 1);

    &--active {
      background: $brand-50;
      padding: 0 $spacing-xl;
    }
  }

  &__label {
    font-size: 24rpx;
    color: $brand-600;
    font-weight: 700;
    white-space: nowrap;
    animation: tabLabelIn 250ms ease-out both;
  }

  &__badge {
    position: absolute;
    top: 14rpx;
    right: 16rpx;
    min-width: 28rpx;
    height: 28rpx;
    background: $color-error;
    border-radius: $radius-full;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 6rpx;
    border: 3rpx solid #ffffff;
  }

  &__badge-text {
    color: $text-inverse;
    font-size: 18rpx;
    font-weight: bold;
    line-height: 28rpx;
  }
}

@keyframes tabLabelIn {
  from {
    opacity: 0;
    transform: translateX(-8rpx);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>
