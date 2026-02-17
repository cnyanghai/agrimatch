<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '../../store/auth'
import { listSupplies } from '../../api/supply'
import { listRequirements } from '../../api/requirement'
import { listContracts } from '../../api/contract'

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)
const user = computed(() => authStore.user)

/** 统计数据 */
const supplyCount = ref<number | null>(null)
const requirementCount = ref<number | null>(null)
const contractCount = ref<number | null>(null)

// 用户名首字
const avatarChar = computed(() => {
  const name = user.value?.nickName || user.value?.userName || '?'
  return name[0]
})

onShow(() => {
  if (isLoggedIn.value) {
    authStore.checkSession()
    loadStats()
  }
})

async function loadStats() {
  const userId = user.value?.userId
  if (!userId) return
  try {
    const [supplies, requirements, contracts] = await Promise.all([
      listSupplies({ userId }).catch(() => []),
      listRequirements({ userId }).catch(() => []),
      listContracts().catch(() => []),
    ])
    supplyCount.value = supplies?.length ?? 0
    requirementCount.value = requirements?.length ?? 0
    contractCount.value = contracts?.length ?? 0
  } catch {
    // silently fail
  }
}

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

function goSealManage() {
  uni.navigateTo({ url: '/pages/seal/manage' })
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

function goVehicles() {
  uni.navigateTo({ url: '/pages/vehicle/list' })
}

function goMap() {
  uni.navigateTo({ url: '/pages/map/index' })
}

function goCategoryDirectory() {
  uni.navigateTo({ url: '/pages/category/directory' })
}

function goLegal(type = 'terms') {
  uni.navigateTo({ url: `/pages/legal/index?type=${type}` })
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
    <!-- 白色头部 (水平布局) -->
    <view class="header">
      <!-- 已登录 -->
      <view v-if="isLoggedIn" class="header__user" @tap="goEditProfile">
        <view class="header__avatar-wrap">
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
        <view class="header__info">
          <text class="header__name">{{ user?.nickName || user?.userName || '用户' }}</text>
          <text v-if="user?.companyName" class="header__company">{{ user.companyName }}</text>
          <view class="header__roles">
            <text v-if="user?.isSeller" class="header__role">供应商</text>
            <text v-if="user?.isBuyer" class="header__role header__role--autumn">采购商</text>
          </view>
        </view>
        <WgIcon name="right" :size="16" color="#D6CCC0" />
      </view>

      <!-- 未登录 -->
      <view v-else class="header__guest" @tap="goLogin">
        <view class="header__avatar header__avatar--placeholder">
          <WgIcon name="user" :size="32" color="#A8A29E" />
        </view>
        <view class="header__info">
          <text class="header__name">点击登录</text>
          <text class="header__company">登录后享受完整功能</text>
        </view>
        <WgIcon name="right" :size="16" color="#D6CCC0" />
      </view>
    </view>

    <!-- 统计卡片 (独立暖色卡片) -->
    <view v-if="isLoggedIn" class="stats-row anim-fade-in">
      <view class="stat-card" @tap="goMySupplies">
        <text class="stat-card__value font-mono">{{ supplyCount ?? '-' }}</text>
        <text class="stat-card__label">供应</text>
      </view>
      <view class="stat-card" @tap="goMyRequirements">
        <text class="stat-card__value font-mono">{{ requirementCount ?? '-' }}</text>
        <text class="stat-card__label">采购</text>
      </view>
      <view class="stat-card" @tap="goContracts">
        <text class="stat-card__value font-mono">{{ contractCount ?? '-' }}</text>
        <text class="stat-card__label">合同</text>
      </view>
    </view>

    <!-- 功能网格（紧凑4列） -->
    <view class="func-grid anim-slide-up">
      <view class="func-grid__item" @tap="goMySupplies">
        <view class="func-grid__icon func-grid__icon--brand"><WgIcon name="store" :size="22" color="#2D6A4F" /></view>
        <text class="func-grid__label">我的供应</text>
      </view>
      <view class="func-grid__item" @tap="goMyRequirements">
        <view class="func-grid__icon func-grid__icon--autumn"><WgIcon name="shopping-bag" :size="22" color="#c28a55" /></view>
        <text class="func-grid__label">我的采购</text>
      </view>
      <view class="func-grid__item" @tap="goContracts">
        <view class="func-grid__icon func-grid__icon--action"><WgIcon name="file-text" :size="22" color="#2563eb" /></view>
        <text class="func-grid__label">我的合同</text>
      </view>
      <view class="func-grid__item" @tap="goCollections">
        <view class="func-grid__icon func-grid__icon--accent"><WgIcon name="bookmark" :size="22" color="#E76F51" /></view>
        <text class="func-grid__label">我的收藏</text>
      </view>
      <view class="func-grid__item" @tap="goFollowing">
        <view class="func-grid__icon func-grid__icon--brand"><WgIcon name="user-plus" :size="22" color="#2D6A4F" /></view>
        <text class="func-grid__label">我的关注</text>
      </view>
      <view class="func-grid__item" @tap="goNotify">
        <view class="func-grid__icon func-grid__icon--accent"><WgIcon name="bell" :size="22" color="#E76F51" /></view>
        <text class="func-grid__label">通知中心</text>
      </view>
      <view class="func-grid__item" @tap="goPoints">
        <view class="func-grid__icon func-grid__icon--autumn"><WgIcon name="award" :size="22" color="#c28a55" /></view>
        <text class="func-grid__label">我的积分</text>
      </view>
      <view class="func-grid__item" @tap="goTopicSquare">
        <view class="func-grid__icon func-grid__icon--action"><WgIcon name="message-square" :size="22" color="#2563eb" /></view>
        <text class="func-grid__label">话题广场</text>
      </view>
    </view>

    <!-- 发现与工具 -->
    <view class="menu-card">
      <view v-if="user?.companyId" class="menu-item" @tap="goCompany">
        <view class="menu-item__icon menu-item__icon--brand"><WgIcon name="building" :size="20" color="#2D6A4F" /></view>
        <text class="menu-item__label">企业信息</text>
        <WgIcon name="right" :size="16" color="#D6CCC0" />
      </view>
      <view class="menu-item" @tap="goSealManage">
        <view class="menu-item__icon menu-item__icon--accent"><WgIcon name="shield" :size="20" color="#E76F51" /></view>
        <text class="menu-item__label">印章管理</text>
        <WgIcon name="right" :size="16" color="#D6CCC0" />
      </view>
      <view class="menu-item" @tap="goDirectory">
        <view class="menu-item__icon menu-item__icon--action"><WgIcon name="building2" :size="20" color="#2563eb" /></view>
        <text class="menu-item__label">企业名录</text>
        <WgIcon name="right" :size="16" color="#D6CCC0" />
      </view>
      <view class="menu-item" @tap="goCategoryDirectory">
        <view class="menu-item__icon menu-item__icon--brand"><WgIcon name="layout-grid" :size="20" color="#2D6A4F" /></view>
        <text class="menu-item__label">品类目录</text>
        <WgIcon name="right" :size="16" color="#D6CCC0" />
      </view>
      <view class="menu-item" @tap="goMap">
        <view class="menu-item__icon menu-item__icon--action"><WgIcon name="map-pin" :size="20" color="#2563eb" /></view>
        <text class="menu-item__label">地图找商</text>
        <WgIcon name="right" :size="16" color="#D6CCC0" />
      </view>
      <view v-if="user?.companyId" class="menu-item" @tap="goVehicles">
        <view class="menu-item__icon menu-item__icon--autumn"><WgIcon name="truck" :size="20" color="#c28a55" /></view>
        <text class="menu-item__label">车辆管理</text>
        <WgIcon name="right" :size="16" color="#D6CCC0" />
      </view>
    </view>

    <!-- 系统 -->
    <view class="menu-card">
      <view class="menu-item" @tap="goSettings">
        <view class="menu-item__icon menu-item__icon--gray"><WgIcon name="settings" :size="20" color="#78716C" /></view>
        <text class="menu-item__label">设置</text>
        <WgIcon name="right" :size="16" color="#D6CCC0" />
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

/* ===== Header (White, Horizontal) ===== */
.header {
  background: #ffffff;
  padding: $spacing-xl $spacing-lg $spacing-lg;
  padding-top: calc(var(--status-bar-height, 25px) + 40rpx);

  &__user,
  &__guest {
    display: flex;
    align-items: center;
    gap: $spacing-md;
  }

  &__avatar-wrap {
    flex-shrink: 0;
  }

  &__avatar {
    width: 112rpx;
    height: 112rpx;
    border-radius: 50%;

    &--text {
      background: $warm-100;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    &--placeholder {
      background: $warm-100;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  &__avatar-char {
    font-size: $font-2xl;
    font-weight: 800;
    color: $brand-600;
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__name {
    display: block;
    color: $text-primary;
    font-size: $font-xl;
    font-weight: 700;
    margin-bottom: 4rpx;
  }

  &__company {
    display: block;
    color: $text-secondary;
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
    border-radius: $radius-pill;
    background: $brand-50;
    color: $brand-600;
    font-weight: 600;

    &--autumn {
      background: $autumn-50;
      color: $autumn-500;
    }
  }
}

/* ===== Stats Row (Independent warm cards) ===== */
.stats-row {
  display: flex;
  gap: $spacing-sm;
  padding: $spacing-md $spacing-md 0;
}

.stat-card {
  flex: 1;
  background: #ffffff;
  border-radius: $radius-xl;
  padding: $spacing-lg $spacing-sm;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: $shadow-warm-card;

  &__value {
    font-size: $font-xl;
    font-weight: 800;
    color: $text-primary;
    margin-bottom: 4rpx;
  }

  &__label {
    font-size: $font-xs;
    color: $text-secondary;
  }
}

/* ===== 功能网格 ===== */
.func-grid {
  display: flex;
  flex-wrap: wrap;
  background: $bg-card;
  margin: $spacing-md;
  border-radius: $radius-xl;
  box-shadow: $shadow-warm-card;
  padding: $spacing-sm 0;

  &__item {
    width: 25%;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: $spacing-md 0;

    &:active {
      opacity: 0.7;
      transform: scale(0.95);
    }
  }

  &__icon {
    width: 80rpx;
    height: 80rpx;
    border-radius: $radius-lg;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: $spacing-xs;

    &--brand { background: $brand-50; }
    &--autumn { background: $autumn-50; }
    &--action { background: rgba(37, 99, 235, 0.08); }
    &--accent { background: $accent-50; }
  }

  &__label {
    font-size: 22rpx;
    color: $text-secondary;
    font-weight: 500;
  }
}

/* ===== Menu Card ===== */
.menu-card {
  background: $bg-card;
  margin: $spacing-md;
  border-radius: $radius-xl;
  overflow: hidden;
  box-shadow: $shadow-warm-card;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: $spacing-lg $spacing-lg;
  border-bottom: 1rpx solid $warm-100;
  gap: $spacing-md;

  &:last-child {
    border-bottom: none;
  }

  &__icon {
    width: 72rpx;
    height: 72rpx;
    border-radius: $radius-lg;
    display: flex;
    align-items: center;
    justify-content: center;

    &--brand { background: $brand-50; }
    &--autumn { background: $autumn-50; }
    &--action { background: rgba(37, 99, 235, 0.08); }
    &--accent { background: $accent-50; }
    &--gray { background: $warm-100; }
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
  border: 1rpx solid $warm-200;
  border-radius: $radius-xl;
  font-size: $font-md;
  height: 88rpx;
  line-height: 88rpx;
}
</style>
