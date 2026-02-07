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
  { path: '/pages/home/index', label: '首页', icon: 'home', iconActive: 'home' },
  { path: '/pages/supply/index', label: '供应', icon: 'store', iconActive: 'store' },
  { path: '/pages/requirement/index', label: '采购', icon: 'shopping-bag', iconActive: 'shopping-bag' },
  { path: '/pages/chat/index', label: '消息', icon: 'message-circle', iconActive: 'message-circle' },
  { path: '/pages/profile/index', label: '我的', icon: 'user', iconActive: 'user' },
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

function handlePublish() {
  if (!authStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  uni.navigateTo({ url: '/pages/publish/index' })
}
</script>

<template>
  <view class="tab-bar safe-area-bottom">
    <!-- 前两个 Tab -->
    <view
      v-for="(tab, index) in tabs.slice(0, 2)"
      :key="index"
      class="tab-bar__item"
      :class="{ 'tab-bar__item--active': index === current }"
      @tap="handleTab(index, tab)"
    >
      <view class="tab-bar__normal">
        <WgIcon
          :name="index === current ? tab.iconActive : tab.icon"
          :size="22"
          :color="index === current ? '#2D6A4F' : '#A8A29E'"
          :stroke-width="index === current ? 2.2 : 1.75"
        />
        <view v-if="props.badges?.[index]" class="tab-bar__badge">
          <text class="tab-bar__badge-text">{{ props.badges[index] > 99 ? '99+' : props.badges[index] }}</text>
        </view>
        <text class="tab-bar__label" :class="{ 'tab-bar__label--active': index === current }">{{ tab.label }}</text>
      </view>
    </view>

    <!-- 中间发布按钮 -->
    <view class="tab-bar__item tab-bar__item--center" @tap="handlePublish">
      <view class="tab-bar__publish-btn">
        <WgIcon name="plus" :size="26" color="#ffffff" :stroke-width="2.5" />
      </view>
      <text class="tab-bar__label tab-bar__label--publish">发布</text>
    </view>

    <!-- 后三个 Tab -->
    <view
      v-for="(tab, index) in tabs.slice(2)"
      :key="index + 2"
      class="tab-bar__item"
      :class="{ 'tab-bar__item--active': (index + 2) === current }"
      @tap="handleTab(index + 2, tab)"
    >
      <view class="tab-bar__normal">
        <WgIcon
          :name="(index + 2) === current ? tab.iconActive : tab.icon"
          :size="22"
          :color="(index + 2) === current ? '#2D6A4F' : '#A8A29E'"
          :stroke-width="(index + 2) === current ? 2.2 : 1.75"
        />
        <view v-if="props.badges?.[index + 2]" class="tab-bar__badge">
          <text class="tab-bar__badge-text">{{ props.badges[index + 2] > 99 ? '99+' : props.badges[index + 2] }}</text>
        </view>
        <text class="tab-bar__label" :class="{ 'tab-bar__label--active': (index + 2) === current }">{{ tab.label }}</text>
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
  border-top: 1rpx solid rgba(0, 0, 0, 0.06);
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  box-shadow: 0 -2rpx 20rpx rgba(0, 0, 0, 0.04);

  &__item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 110rpx;
    transition: opacity 0.15s;

    &:active {
      opacity: 0.75;
    }

    &--center {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: flex-start;
      padding-top: 0;
      position: relative;
    }
  }

  &__normal {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4rpx;
    padding-top: 12rpx;
    position: relative;
  }

  &__publish-btn {
    width: 92rpx;
    height: 92rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, #2D6A4F 0%, #389867 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: -30rpx;
    box-shadow: 0 6rpx 24rpx rgba(45, 106, 79, 0.35);
    transition: transform 0.15s;
    position: relative;
    z-index: 2;

    &:active {
      transform: scale(0.92);
    }
  }

  &__badge {
    position: absolute;
    top: 4rpx;
    right: -4rpx;
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
    margin-top: 2rpx;

    &--active {
      color: $brand-600;
      font-weight: 600;
    }

    &--publish {
      font-size: 18rpx;
      color: $brand-600;
      font-weight: 600;
      margin-top: 4rpx;
    }
  }
}
</style>
