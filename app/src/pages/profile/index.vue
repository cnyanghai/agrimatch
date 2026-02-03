<script setup lang="ts">
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '../../store/auth'

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)
const user = computed(() => authStore.user)

// 用户名首字
const avatarChar = computed(() => {
  const name = user.value?.nickName || user.value?.userName || '?'
  return name[0]
})

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
  uni.navigateTo({ url: '/pages/supply/my-list' })
}

function goMyRequirements() {
  uni.navigateTo({ url: '/pages/requirement/my-list' })
}

function goCompany() {
  if (user.value?.companyId) {
    uni.navigateTo({ url: `/pages/company/detail?id=${user.value.companyId}` })
  }
}

function goCollections() {
  uni.navigateTo({ url: '/pages/topic/collections' })
}

function goTopicSquare() {
  uni.navigateTo({ url: '/pages/topic/square' })
}

function goMarket() {
  uni.navigateTo({ url: '/pages/market/index' })
}

function goDirectory() {
  uni.navigateTo({ url: '/pages/company/directory' })
}

function goFollowing() {
  uni.navigateTo({ url: '/pages/follow/list' })
}

function goNotify() {
  uni.navigateTo({ url: '/pages/notify/index' })
}

function goEditProfile() {
  uni.navigateTo({ url: '/pages/settings/edit-profile' })
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
    <!-- 头部区域 -->
    <view class="header">
      <!-- 装饰圆 -->
      <view class="header__deco header__deco--1" />
      <view class="header__deco header__deco--2" />

      <!-- 已登录 -->
      <view v-if="isLoggedIn" class="header__user">
        <view class="header__avatar-wrap" @tap="goEditProfile">
          <image
            v-if="user?.avatar"
            class="header__avatar"
            :src="user.avatar"
            mode="aspectFill"
          />
          <view v-else class="header__avatar header__avatar--text">
            <text class="header__avatar-char">{{ avatarChar }}</text>
          </view>
        </view>
        <text class="header__name">{{ user?.nickName || user?.userName || '用户' }}</text>
        <text v-if="user?.companyName" class="header__company">{{ user.companyName }}</text>
        <view class="header__roles">
          <text v-if="user?.isSeller" class="header__role">供应商</text>
          <text v-if="user?.isBuyer" class="header__role header__role--autumn">采购商</text>
        </view>
      </view>

      <!-- 未登录 -->
      <view v-else class="header__guest" @tap="goLogin">
        <view class="header__avatar header__avatar--placeholder">
          <uni-icons type="person" size="40" color="rgba(255,255,255,0.6)" />
        </view>
        <text class="header__name">点击登录</text>
        <text class="header__company">登录后享受完整功能</text>
      </view>

      <!-- 统计行 -->
      <view v-if="isLoggedIn" class="header__stats">
        <view class="header__stat" @tap="goMySupplies">
          <text class="header__stat-value">-</text>
          <text class="header__stat-label">供应</text>
        </view>
        <view class="header__stat-divider" />
        <view class="header__stat" @tap="goMyRequirements">
          <text class="header__stat-value">-</text>
          <text class="header__stat-label">采购</text>
        </view>
        <view class="header__stat-divider" />
        <view class="header__stat" @tap="goContracts">
          <text class="header__stat-value">-</text>
          <text class="header__stat-label">合同</text>
        </view>
      </view>
    </view>

    <!-- 业务功能组 -->
    <view class="menu-card">
      <view class="menu-item" @tap="goMySupplies">
        <view class="menu-item__icon menu-item__icon--brand">
          <uni-icons type="shop" size="20" color="#fff" />
        </view>
        <text class="menu-item__label">我的供应</text>
        <uni-icons type="right" size="16" color="#d1d5db" />
      </view>
      <view class="menu-item" @tap="goMyRequirements">
        <view class="menu-item__icon menu-item__icon--autumn">
          <uni-icons type="cart" size="20" color="#fff" />
        </view>
        <text class="menu-item__label">我的采购</text>
        <uni-icons type="right" size="16" color="#d1d5db" />
      </view>
      <view class="menu-item" @tap="goContracts">
        <view class="menu-item__icon menu-item__icon--action">
          <uni-icons type="list" size="20" color="#fff" />
        </view>
        <text class="menu-item__label">我的合同</text>
        <uni-icons type="right" size="16" color="#d1d5db" />
      </view>
      <view v-if="user?.companyId" class="menu-item" @tap="goCompany">
        <view class="menu-item__icon menu-item__icon--brand">
          <uni-icons type="flag" size="20" color="#fff" />
        </view>
        <text class="menu-item__label">企业信息</text>
        <uni-icons type="right" size="16" color="#d1d5db" />
      </view>
      <view class="menu-item" @tap="goMarket">
        <view class="menu-item__icon menu-item__icon--accent">
          <uni-icons type="bars" size="20" color="#fff" />
        </view>
        <text class="menu-item__label">行情中心</text>
        <uni-icons type="right" size="16" color="#d1d5db" />
      </view>
      <view class="menu-item" @tap="goDirectory">
        <view class="menu-item__icon menu-item__icon--action">
          <uni-icons type="contact" size="20" color="#fff" />
        </view>
        <text class="menu-item__label">企业名录</text>
        <uni-icons type="right" size="16" color="#d1d5db" />
      </view>
    </view>

    <!-- 社交功能组 -->
    <view class="menu-card">
      <view class="menu-item" @tap="goCollections">
        <view class="menu-item__icon menu-item__icon--accent">
          <uni-icons type="star-filled" size="20" color="#fff" />
        </view>
        <text class="menu-item__label">我的收藏</text>
        <uni-icons type="right" size="16" color="#d1d5db" />
      </view>
      <view class="menu-item" @tap="goTopicSquare">
        <view class="menu-item__icon menu-item__icon--action">
          <uni-icons type="chatboxes" size="20" color="#fff" />
        </view>
        <text class="menu-item__label">话题广场</text>
        <uni-icons type="right" size="16" color="#d1d5db" />
      </view>
      <view class="menu-item" @tap="goFollowing">
        <view class="menu-item__icon menu-item__icon--brand">
          <uni-icons type="personadd" size="20" color="#fff" />
        </view>
        <text class="menu-item__label">我的关注</text>
        <uni-icons type="right" size="16" color="#d1d5db" />
      </view>
      <view class="menu-item" @tap="goNotify">
        <view class="menu-item__icon menu-item__icon--accent">
          <uni-icons type="bell" size="20" color="#fff" />
        </view>
        <text class="menu-item__label">通知中心</text>
        <uni-icons type="right" size="16" color="#d1d5db" />
      </view>
    </view>

    <!-- 系统功能组 -->
    <view class="menu-card">
      <view class="menu-item" @tap="goPoints">
        <view class="menu-item__icon menu-item__icon--accent">
          <uni-icons type="gift" size="20" color="#fff" />
        </view>
        <text class="menu-item__label">我的积分</text>
        <uni-icons type="right" size="16" color="#d1d5db" />
      </view>
      <view class="menu-item" @tap="goSettings">
        <view class="menu-item__icon menu-item__icon--gray">
          <uni-icons type="gear" size="20" color="#fff" />
        </view>
        <text class="menu-item__label">设置</text>
        <uni-icons type="right" size="16" color="#d1d5db" />
      </view>
    </view>

    <!-- 退出登录 -->
    <view v-if="isLoggedIn" class="logout-wrap">
      <button class="btn-logout" @tap="handleLogout">退出登录</button>
    </view>

    <WgTabBar :current="4" />
  </view>
</template>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: 130rpx;
}

/* ===== Header ===== */
.header {
  background: linear-gradient(180deg, $brand-700 0%, $brand-600 100%);
  padding: $spacing-xl $spacing-lg $spacing-lg;
  padding-top: calc(var(--status-bar-height, 25px) + 40rpx);
  position: relative;
  overflow: hidden;

  &__deco {
    position: absolute;
    border-radius: 50%;
    opacity: 0.06;
    background: #fff;

    &--1 {
      width: 300rpx;
      height: 300rpx;
      top: -80rpx;
      right: -60rpx;
    }

    &--2 {
      width: 200rpx;
      height: 200rpx;
      bottom: -40rpx;
      left: -40rpx;
    }
  }

  &__user,
  &__guest {
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
    z-index: 1;
  }

  &__avatar-wrap {
    margin-bottom: $spacing-sm;
  }

  &__avatar {
    width: 128rpx;
    height: 128rpx;
    border-radius: 50%;
    border: 4rpx solid rgba(255, 255, 255, 0.3);

    &--text {
      background: rgba(255, 255, 255, 0.15);
      display: flex;
      align-items: center;
      justify-content: center;
    }

    &--placeholder {
      background: rgba(255, 255, 255, 0.12);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: $spacing-sm;
    }
  }

  &__avatar-char {
    font-size: $font-2xl;
    font-weight: 800;
    color: #fff;
  }

  &__name {
    color: #fff;
    font-size: $font-xl;
    font-weight: 700;
    margin-bottom: 4rpx;
  }

  &__company {
    color: rgba(255, 255, 255, 0.6);
    font-size: $font-sm;
    margin-bottom: $spacing-xs;
  }

  &__roles {
    display: flex;
    gap: $spacing-xs;
  }

  &__role {
    font-size: $font-xs;
    padding: 4rpx 16rpx;
    border-radius: 20rpx;
    background: rgba(255, 255, 255, 0.15);
    color: #fff;
    font-weight: 600;

    &--autumn {
      background: rgba(212, 163, 115, 0.3);
    }
  }

  &__stats {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: $spacing-lg;
    padding: $spacing-md 0;
    background: rgba(255, 255, 255, 0.1);
    border-radius: $radius-lg;
    position: relative;
    z-index: 1;
  }

  &__stat {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  &__stat-value {
    font-size: $font-xl;
    font-weight: 800;
    color: #fff;
  }

  &__stat-label {
    font-size: $font-xs;
    color: rgba(255, 255, 255, 0.6);
    margin-top: 2rpx;
  }

  &__stat-divider {
    width: 1rpx;
    height: 48rpx;
    background: rgba(255, 255, 255, 0.15);
  }
}

/* ===== Menu Card ===== */
.menu-card {
  background: $bg-card;
  margin: $spacing-sm $spacing-md;
  border-radius: $radius-xl;
  overflow: hidden;
  box-shadow: $shadow-sm;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: $spacing-lg $spacing-md;
  border-bottom: 1rpx solid $border-light;
  gap: $spacing-sm;

  &:last-child {
    border-bottom: none;
  }

  &__icon {
    width: 64rpx;
    height: 64rpx;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;

    &--brand { background: $brand-600; }
    &--autumn { background: $autumn-400; }
    &--action { background: $action-600; }
    &--accent { background: $accent-400; }
    &--gray { background: #9ca3af; }
  }

  &__label {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;
    font-weight: 500;
  }
}

/* ===== Logout ===== */
.logout-wrap {
  padding: $spacing-xl $spacing-lg;
}

.btn-logout {
  background: $bg-card;
  color: $color-error;
  border: 1rpx solid $border-color;
  border-radius: $radius-xl;
  font-size: $font-md;
  height: 88rpx;
  line-height: 88rpx;
}
</style>
