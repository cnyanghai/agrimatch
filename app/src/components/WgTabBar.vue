<script setup lang="ts">
import { computed } from 'vue'
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
  isCenter?: boolean
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
    path: '/pages/publish/index',
    label: '发布',
    icon: '',
    iconActive: '',
    isCenter: true
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

  // 中间发布按钮 → 弹出选择
  if (tab.isCenter) {
    showPublishSheet()
    return
  }

  // 消息和我的需要登录
  if ((index === 3 || index === 4) && !authStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }

  uni.switchTab({ url: tab.path })
}

function showPublishSheet() {
  if (!authStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  uni.showActionSheet({
    itemList: ['发布供应', '发布采购'],
    success: (res) => {
      if (res.tapIndex === 0) {
        uni.navigateTo({ url: '/pages/supply/publish' })
      } else if (res.tapIndex === 1) {
        uni.navigateTo({ url: '/pages/requirement/publish' })
      }
    }
  })
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
      <!-- 中间发布按钮 -->
      <view v-if="tab.isCenter" class="tab-bar__center">
        <view class="tab-bar__center-btn">
          <text class="tab-bar__center-icon">+</text>
        </view>
        <text class="tab-bar__label tab-bar__label--center">{{ tab.label }}</text>
      </view>

      <!-- 普通 tab -->
      <view v-else class="tab-bar__normal">
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
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(40rpx);
  -webkit-backdrop-filter: blur(40rpx);
  border-top: 1rpx solid rgba(0, 0, 0, 0.04);
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);

  &__item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 110rpx;

    &--active {
      // active state handled by child classes
    }
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
    color: #999;
    line-height: 1.2;

    &--active {
      color: $brand-600;
      font-weight: 600;
    }

    &--center {
      font-size: 20rpx;
      color: $brand-600;
      font-weight: 600;
      margin-top: 4rpx;
    }
  }

  &__center {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-top: -40rpx;
  }

  &__center-btn {
    width: 96rpx;
    height: 96rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, $brand-500, $brand-600);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8rpx 24rpx rgba(45, 106, 79, 0.3);
    transition: transform $transition-fast ease;

    &:active {
      transform: scale(0.92);
    }
  }

  &__center-icon {
    font-size: 48rpx;
    color: #fff;
    font-weight: 300;
    line-height: 1;
    margin-top: -4rpx;
  }
}
</style>
