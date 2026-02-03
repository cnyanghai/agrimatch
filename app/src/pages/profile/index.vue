<script setup lang="ts">
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '../../store/auth'

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)
const user = computed(() => authStore.user)

onShow(() => {
  if (isLoggedIn.value) {
    authStore.checkSession()
  }
})

function goLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}

function goSettings() {
  uni.navigateTo({ url: '/pages/settings/index' })
}

function goPoints() {
  uni.navigateTo({ url: '/pages/points/index' })
}

function goContracts() {
  uni.navigateTo({ url: '/pages/contract/list' })
}

function goMySupplies() {
  // TODO: 我的供应管理
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

function goMyRequirements() {
  // TODO: 我的采购管理
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

function goCompany() {
  if (user.value?.companyId) {
    uni.navigateTo({ url: `/pages/company/detail?id=${user.value.companyId}` })
  }
}

function handleLogout() {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success(res) {
      if (res.confirm) {
        authStore.logout()
      }
    }
  })
}
</script>

<template>
  <view class="profile-page">
    <!-- 用户信息卡 -->
    <view class="user-card">
      <view v-if="isLoggedIn" class="user-card__info">
        <image
          class="user-card__avatar"
          :src="user?.avatar || '/static/logo.png'"
          mode="aspectFill"
        />
        <view class="user-card__detail">
          <text class="user-card__name">{{ user?.nickName || user?.userName || '用户' }}</text>
          <text class="user-card__company" v-if="user?.companyName">{{ user.companyName }}</text>
          <view class="user-card__roles">
            <text v-if="user?.isSeller" class="role-tag role-tag--brand">供应商</text>
            <text v-if="user?.isBuyer" class="role-tag role-tag--autumn">采购商</text>
          </view>
        </view>
      </view>
      <view v-else class="user-card__login" @tap="goLogin">
        <view class="user-card__avatar user-card__avatar--placeholder">
          <uni-icons type="person" size="32" color="#fff" />
        </view>
        <view>
          <text class="user-card__login-text">点击登录</text>
          <text class="user-card__login-hint">登录后查看更多功能</text>
        </view>
      </view>
    </view>

    <!-- 业务功能 -->
    <view class="menu-group">
      <view class="menu-group__title">我的业务</view>
      <view class="menu-item" @tap="goMySupplies">
        <view class="menu-item__left">
          <view class="menu-item__icon"><uni-icons type="shop" size="20" color="#2D6A4F" /></view>
          <text class="menu-item__label">我的供应</text>
        </view>
        <uni-icons type="right" size="16" color="#999" />
      </view>
      <view class="menu-item" @tap="goMyRequirements">
        <view class="menu-item__left">
          <view class="menu-item__icon"><uni-icons type="cart" size="20" color="#D4A373" /></view>
          <text class="menu-item__label">我的采购</text>
        </view>
        <uni-icons type="right" size="16" color="#999" />
      </view>
      <view class="menu-item" @tap="goContracts">
        <view class="menu-item__left">
          <view class="menu-item__icon"><uni-icons type="list" size="20" color="#2563eb" /></view>
          <text class="menu-item__label">我的合同</text>
        </view>
        <uni-icons type="right" size="16" color="#999" />
      </view>
      <view class="menu-item" @tap="goPoints">
        <view class="menu-item__left">
          <view class="menu-item__icon"><uni-icons type="gift" size="20" color="#E76F51" /></view>
          <text class="menu-item__label">我的积分</text>
        </view>
        <uni-icons type="right" size="16" color="#999" />
      </view>
    </view>

    <!-- 企业 & 设置 -->
    <view class="menu-group">
      <view v-if="user?.companyId" class="menu-item" @tap="goCompany">
        <view class="menu-item__left">
          <view class="menu-item__icon"><uni-icons type="flag" size="20" color="#2D6A4F" /></view>
          <text class="menu-item__label">企业信息</text>
        </view>
        <uni-icons type="right" size="16" color="#999" />
      </view>
      <view class="menu-item" @tap="goSettings">
        <view class="menu-item__left">
          <view class="menu-item__icon"><uni-icons type="gear" size="20" color="#666" /></view>
          <text class="menu-item__label">设置</text>
        </view>
        <uni-icons type="right" size="16" color="#999" />
      </view>
    </view>

    <view v-if="isLoggedIn" class="logout-section">
      <button class="btn-logout" @tap="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: $bg-page;
}

.user-card {
  background: linear-gradient(135deg, $brand-700, $brand-600);
  padding: $spacing-xl $spacing-lg;
  padding-top: calc(var(--status-bar-height, 25px) + 40rpx);

  &__info,
  &__login {
    display: flex;
    align-items: center;
    gap: $spacing-md;
  }

  &__avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    border: 4rpx solid rgba(255, 255, 255, 0.3);

    &--placeholder {
      background: rgba(255, 255, 255, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  &__avatar-text {
    color: #fff;
    font-size: $font-2xl;
  }

  &__detail {
    display: flex;
    flex-direction: column;
    gap: 6rpx;
  }

  &__name {
    color: #fff;
    font-size: $font-xl;
    font-weight: bold;
  }

  &__company {
    color: rgba(255, 255, 255, 0.7);
    font-size: $font-sm;
  }

  &__roles {
    display: flex;
    gap: $spacing-xs;
    margin-top: 4rpx;
  }

  &__login-text {
    color: #fff;
    font-size: $font-xl;
    font-weight: bold;
    display: block;
  }

  &__login-hint {
    color: rgba(255, 255, 255, 0.6);
    font-size: $font-sm;
  }
}

.role-tag {
  font-size: $font-xs;
  padding: 2rpx 14rpx;
  border-radius: $radius-sm;
  font-weight: bold;

  &--brand {
    background: rgba(255, 255, 255, 0.2);
    color: #fff;
  }

  &--autumn {
    background: rgba(212, 163, 115, 0.3);
    color: #fff;
  }
}

.menu-group {
  background: $bg-card;
  margin: $spacing-sm $spacing-md;
  border-radius: $radius-lg;
  overflow: hidden;

  &__title {
    font-size: $font-sm;
    color: $text-secondary;
    padding: $spacing-sm $spacing-md 0;
  }
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-lg $spacing-md;
  border-bottom: 1rpx solid $border-light;

  &:last-child { border-bottom: none; }

  &__left {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 44rpx;
  }

  &__label {
    font-size: $font-md;
    color: $text-primary;
  }
}

.logout-section {
  padding: $spacing-xl $spacing-lg;
}

.btn-logout {
  background: $bg-card;
  color: $color-error;
  border: 1rpx solid $border-color;
  border-radius: $radius-lg;
  font-size: $font-md;
  height: 88rpx;
  line-height: 88rpx;
}
</style>
