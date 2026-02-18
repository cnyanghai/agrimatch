<script setup lang="ts">
import { computed } from 'vue'
import { BRAND_600, WARM_400, ACCENT_400, ACTION_600 } from '../../constants/colors'
import { useAuthStore } from '../../store/auth'

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)
const user = computed(() => authStore.user)

function goEditProfile() {
  uni.navigateTo({ url: '/pages/settings/edit-profile' })
}

function goChangePassword() {
  uni.navigateTo({ url: '/pages/settings/change-password' })
}

function goAbout() {
  uni.showToast({ title: '沃谷 - 农牧供需智能匹配平台 v1.0.0', icon: 'none' })
}

function goLegal(type: string) {
  uni.navigateTo({ url: `/pages/legal/index?type=${type}` })
}

function clearCache() {
  uni.showModal({
    title: '清除缓存',
    content: '确定要清除所有缓存数据吗？（不会清除登录状态）',
    success(res) {
      if (res.confirm) {
        const token = uni.getStorageSync('token')
        uni.clearStorageSync()
        if (token) uni.setStorageSync('token', token)
        uni.showToast({ title: '缓存已清除', icon: 'success' })
      }
    }
  })
}

function handleLogout() {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success(res) {
      if (res.confirm) {
        authStore.logout()
        uni.switchTab({ url: '/pages/home/index' })
      }
    }
  })
}
</script>

<template>
  <view class="settings-page">
    <WgNavBar title="设置" />

    <!-- 账号设置 -->
    <view v-if="isLoggedIn" class="menu-group stitch-card stitch-fade-up">
      <view class="menu-group__title">账号设置</view>
      <view class="menu-item" @tap="goEditProfile">
        <view class="menu-item__left">
          <view class="menu-item__icon"><WgIcon name="user" :size="20" :color="BRAND_600" /></view>
          <text class="menu-item__label">编辑资料</text>
        </view>
        <view class="menu-item__right">
          <text class="menu-item__value">{{ user?.nickName || user?.userName || '' }}</text>
          <WgIcon name="chevron-right" :size="16" :color="WARM_400" />
        </view>
      </view>
      <view class="menu-item" @tap="goChangePassword">
        <view class="menu-item__left">
          <view class="menu-item__icon"><WgIcon name="lock" :size="20" color="#666" /></view>
          <text class="menu-item__label">修改密码</text>
        </view>
        <WgIcon name="chevron-right" :size="16" :color="WARM_400" />
      </view>
    </view>

    <!-- 通用设置 -->
    <view class="menu-group stitch-card stitch-fade-up" style="animation-delay: .05s">
      <view class="menu-group__title">通用</view>
      <view class="menu-item" @tap="clearCache">
        <view class="menu-item__left">
          <view class="menu-item__icon"><WgIcon name="trash" :size="20" :color="WARM_400" /></view>
          <text class="menu-item__label">清除缓存</text>
        </view>
        <WgIcon name="chevron-right" :size="16" :color="WARM_400" />
      </view>
      <view class="menu-item" @tap="goAbout">
        <view class="menu-item__left">
          <view class="menu-item__icon"><WgIcon name="info" :size="20" :color="ACTION_600" /></view>
          <text class="menu-item__label">关于沃谷</text>
        </view>
        <view class="menu-item__right">
          <text class="menu-item__value">v1.0.0</text>
          <WgIcon name="chevron-right" :size="16" :color="WARM_400" />
        </view>
      </view>
    </view>

    <!-- 法律文档 -->
    <view class="menu-group stitch-card stitch-fade-up" style="animation-delay: .1s">
      <view class="menu-group__title">法律与合规</view>
      <view class="menu-item" @tap="goLegal('terms')">
        <view class="menu-item__left">
          <view class="menu-item__icon"><WgIcon name="file-text" :size="20" :color="BRAND_600" /></view>
          <text class="menu-item__label">用户协议</text>
        </view>
        <WgIcon name="chevron-right" :size="16" :color="WARM_400" />
      </view>
      <view class="menu-item" @tap="goLegal('privacy')">
        <view class="menu-item__left">
          <view class="menu-item__icon"><WgIcon name="lock" :size="20" :color="BRAND_600" /></view>
          <text class="menu-item__label">隐私政策</text>
        </view>
        <WgIcon name="chevron-right" :size="16" :color="WARM_400" />
      </view>
      <view class="menu-item" @tap="goLegal('disclaimer')">
        <view class="menu-item__left">
          <view class="menu-item__icon"><WgIcon name="info" :size="20" :color="ACCENT_400" /></view>
          <text class="menu-item__label">免责声明</text>
        </view>
        <WgIcon name="chevron-right" :size="16" :color="WARM_400" />
      </view>
      <view class="menu-item" @tap="goLegal('feedback')">
        <view class="menu-item__left">
          <view class="menu-item__icon"><WgIcon name="message-square" :size="20" :color="ACTION_600" /></view>
          <text class="menu-item__label">意见反馈</text>
        </view>
        <WgIcon name="chevron-right" :size="16" :color="WARM_400" />
      </view>
    </view>

    <!-- 退出登录 -->
    <view v-if="isLoggedIn" class="logout-section">
      <button class="btn-logout" @tap="handleLogout">退出登录</button>
    </view>

    <view class="version-info">
      <text>沃谷 v1.0.0</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.settings-page {
  min-height: 100vh;
  background: $bg-page;
  padding-top: $spacing-sm;
}

.menu-group {
  background: $bg-card;
  margin: 0 $spacing-md $spacing-sm;
  border-radius: $radius-xl;
  overflow: hidden;
  box-shadow: $shadow-warm-card;

  &__title {
    font-size: $font-xs;
    color: $text-placeholder;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 2rpx;
    padding: $spacing-md $spacing-md $spacing-xs;
  }
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-lg $spacing-md;
  border-bottom: 1rpx solid $border-light;
  transition: background 0.15s;

  &:last-child { border-bottom: none; }

  &:active {
    background: $bg-hover;
  }

  &__left {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 52rpx;
    height: 52rpx;
    border-radius: $radius-lg;
    background: $warm-50;
  }

  &__label {
    font-size: $font-md;
    color: $text-primary;
    font-weight: 500;
  }

  &__right {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
  }

  &__value {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

.logout-section {
  padding: $spacing-xl $spacing-lg;
}

.btn-logout {
  background: $bg-card;
  color: $color-error;
  border: 1rpx solid rgba($color-error, 0.2);
  border-radius: $radius-full;
  font-size: $font-md;
  font-weight: 600;
  height: 88rpx;
  line-height: 88rpx;
  box-shadow: 0 4rpx 16rpx rgba($color-error, 0.08);
}

.version-info {
  text-align: center;
  padding: $spacing-xl;
  color: $text-placeholder;
  font-size: $font-sm;
}
</style>
