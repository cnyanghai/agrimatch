<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '../../store/auth'
import { getDashboard, type DashboardResponse } from '../../api/dashboard'
import { BRAND_600, WARM_300, WARM_400, WARM_500, AUTUMN_500, ACCENT_400, ACTION_600, WHITE } from '../../constants/colors'

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isLoggedIn)
const user = computed(() => authStore.user)

const dashboard = ref<DashboardResponse | null>(null)

const supplyCount = computed(() => dashboard.value?.myActiveListingCount ?? null)
const requirementCount = computed(() =>
  dashboard.value ? (dashboard.value.activeContractCount ?? 0) : null
)
const contractCount = computed(() => dashboard.value?.totalSignedContractCount ?? null)

const pendingItems = computed(() => {
  if (!dashboard.value) return []
  const d = dashboard.value
  const items: { label: string; count: number; url: string; color: string }[] = []
  if (d.unreadMessageCount > 0) items.push({ label: '未读消息', count: d.unreadMessageCount, url: '/pages/chat/index', color: ACTION_600 })
  if (d.pendingContractCount > 0) items.push({ label: '待签合同', count: d.pendingContractCount, url: '/pages/contract/list', color: AUTUMN_500 })
  if (d.pendingMilestoneCount > 0) items.push({ label: '待确认节点', count: d.pendingMilestoneCount, url: '/pages/contract/list', color: ACCENT_400 })
  return items
})

const avatarChar = computed(() => {
  const name = user.value?.nickName || user.value?.userName || '?'
  return name[0]
})

onShow(() => {
  if (isLoggedIn.value) {
    authStore.checkSession()
    loadDashboard()
  }
})

async function loadDashboard() {
  try {
    const res = await getDashboard()
    if (res) dashboard.value = res
  } catch {
    // silently fail
  }
}

const tabBarPaths = ['/pages/home/index', '/pages/supply/index', '/pages/requirement/index', '/pages/chat/index', '/pages/profile/index']
function goUrl(url: string) {
  if (tabBarPaths.includes(url)) {
    uni.switchTab({ url })
  } else {
    uni.navigateTo({ url })
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
  uni.navigateTo({ url: '/pages/company/edit' })
}

function goCompanyDetail() {
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
    <!-- ===== Hero Header (品牌渐变) ===== -->
    <view class="hero stitch-hero">
      <view class="hero__safe safe-area-top" />
      <view class="hero__content">
        <!-- 已登录 -->
        <view v-if="isLoggedIn" class="hero__user" @tap="goEditProfile">
          <view class="hero__avatar-wrap">
            <image
              v-if="user?.avatar"
              class="hero__avatar"
              :src="user.avatar"
              mode="aspectFill"
            />
            <view v-else class="hero__avatar hero__avatar--text">
              <text class="hero__avatar-char">{{ avatarChar }}</text>
            </view>
          </view>
          <view class="hero__info">
            <text class="hero__name">{{ user?.nickName || user?.userName || '用户' }}</text>
            <text v-if="user?.companyName" class="hero__company">{{ user.companyName }}</text>
            <view class="hero__roles">
              <text v-if="user?.isSeller" class="hero__role">供应商</text>
              <text v-if="user?.isBuyer" class="hero__role hero__role--gold">采购商</text>
            </view>
          </view>
          <view class="hero__edit-btn">
            <WgIcon name="edit-2" :size="16" :color="WHITE" />
          </view>
        </view>

        <!-- 未登录 -->
        <view v-else class="hero__guest" @tap="goLogin">
          <view class="hero__avatar hero__avatar--guest">
            <WgIcon name="user" :size="36" :color="WHITE" />
          </view>
          <view class="hero__info">
            <text class="hero__name">点击登录</text>
            <text class="hero__company">登录后享受完整功能</text>
          </view>
          <WgIcon name="right" :size="18" :color="WHITE" />
        </view>
      </view>
    </view>

    <!-- ===== 统计卡片 (浮动在 hero 之上) ===== -->
    <view v-if="isLoggedIn" class="stats-float stitch-scale-in">
      <view class="stats-row">
        <view class="stat-item" @tap="goMySupplies">
          <text class="stat-item__value font-mono stitch-bounce-in">{{ supplyCount ?? '-' }}</text>
          <text class="stat-item__label">供应</text>
        </view>
        <view class="stat-divider" />
        <view class="stat-item" @tap="goMyRequirements">
          <text class="stat-item__value font-mono stitch-bounce-in" style="animation-delay: 80ms">{{ requirementCount ?? '-' }}</text>
          <text class="stat-item__label">采购</text>
        </view>
        <view class="stat-divider" />
        <view class="stat-item" @tap="goContracts">
          <text class="stat-item__value font-mono stitch-bounce-in" style="animation-delay: 160ms">{{ contractCount ?? '-' }}</text>
          <text class="stat-item__label">合同</text>
        </view>
      </view>
    </view>

    <!-- ===== 待办提醒 ===== -->
    <view v-if="pendingItems.length" class="pending-bar">
      <view
        v-for="(item, idx) in pendingItems"
        :key="item.label"
        class="pending-item stitch-fade-up"
        @tap="goUrl(item.url)"
      >
        <view class="pending-item__badge" :style="{ background: item.color }">
          <text class="pending-item__count">{{ item.count }}</text>
        </view>
        <text class="pending-item__label">{{ item.label }}</text>
      </view>
    </view>

    <!-- ===== 功能网格 ===== -->
    <view class="section stitch-fade-up">
      <text class="stitch-section-title">我的服务</text>
      <view class="func-grid">
        <view class="func-item" @tap="goMySupplies">
          <view class="stitch-icon-box stitch-icon-box--sm stitch-icon-box--brand"><WgIcon name="store" :size="20" :color="BRAND_600" /></view>
          <text class="func-item__label">我的供应</text>
        </view>
        <view class="func-item" @tap="goMyRequirements">
          <view class="stitch-icon-box stitch-icon-box--sm stitch-icon-box--autumn"><WgIcon name="shopping-bag" :size="20" :color="AUTUMN_500" /></view>
          <text class="func-item__label">我的采购</text>
        </view>
        <view class="func-item" @tap="goContracts">
          <view class="stitch-icon-box stitch-icon-box--sm stitch-icon-box--action"><WgIcon name="file-text" :size="20" :color="ACTION_600" /></view>
          <text class="func-item__label">我的合同</text>
        </view>
        <view class="func-item" @tap="goCollections">
          <view class="stitch-icon-box stitch-icon-box--sm stitch-icon-box--accent"><WgIcon name="bookmark" :size="20" :color="ACCENT_400" /></view>
          <text class="func-item__label">我的收藏</text>
        </view>
        <view class="func-item" @tap="goFollowing">
          <view class="stitch-icon-box stitch-icon-box--sm stitch-icon-box--brand"><WgIcon name="user-plus" :size="20" :color="BRAND_600" /></view>
          <text class="func-item__label">我的关注</text>
        </view>
        <view class="func-item" @tap="goNotify">
          <view class="stitch-icon-box stitch-icon-box--sm stitch-icon-box--accent"><WgIcon name="bell" :size="20" :color="ACCENT_400" /></view>
          <text class="func-item__label">通知中心</text>
        </view>
        <view class="func-item" @tap="goPoints">
          <view class="stitch-icon-box stitch-icon-box--sm stitch-icon-box--autumn"><WgIcon name="award" :size="20" :color="AUTUMN_500" /></view>
          <text class="func-item__label">我的积分</text>
        </view>
        <view class="func-item" @tap="goTopicSquare">
          <view class="stitch-icon-box stitch-icon-box--sm stitch-icon-box--action"><WgIcon name="message-square" :size="20" :color="ACTION_600" /></view>
          <text class="func-item__label">话题广场</text>
        </view>
      </view>
    </view>

    <!-- ===== 工具与发现 ===== -->
    <view class="section stitch-fade-up">
      <text class="stitch-section-title">工具与发现</text>
      <view class="menu-card stitch-card" style="padding: 0;">
        <view v-if="isLoggedIn" class="menu-item" @tap="goCompany">
          <view class="stitch-icon-box stitch-icon-box--sm stitch-icon-box--brand"><WgIcon name="building" :size="18" :color="BRAND_600" /></view>
          <text class="menu-item__label">{{ user?.companyId ? '编辑企业信息' : '创建企业信息' }}</text>
          <WgIcon name="chevron-right" :size="16" :color="WARM_300" />
        </view>
        <view class="menu-item" @tap="goSealManage">
          <view class="stitch-icon-box stitch-icon-box--sm stitch-icon-box--accent"><WgIcon name="shield" :size="18" :color="ACCENT_400" /></view>
          <text class="menu-item__label">印章管理</text>
          <WgIcon name="chevron-right" :size="16" :color="WARM_300" />
        </view>
        <view class="menu-item" @tap="goDirectory">
          <view class="stitch-icon-box stitch-icon-box--sm stitch-icon-box--action"><WgIcon name="building2" :size="18" :color="ACTION_600" /></view>
          <text class="menu-item__label">企业名录</text>
          <WgIcon name="chevron-right" :size="16" :color="WARM_300" />
        </view>
        <view class="menu-item" @tap="goCategoryDirectory">
          <view class="stitch-icon-box stitch-icon-box--sm stitch-icon-box--brand"><WgIcon name="layout-grid" :size="18" :color="BRAND_600" /></view>
          <text class="menu-item__label">品类目录</text>
          <WgIcon name="chevron-right" :size="16" :color="WARM_300" />
        </view>
        <view class="menu-item" @tap="goMap">
          <view class="stitch-icon-box stitch-icon-box--sm stitch-icon-box--action"><WgIcon name="map-pin" :size="18" :color="ACTION_600" /></view>
          <text class="menu-item__label">地图找商</text>
          <WgIcon name="chevron-right" :size="16" :color="WARM_300" />
        </view>
        <view v-if="user?.companyId" class="menu-item menu-item--last" @tap="goVehicles">
          <view class="stitch-icon-box stitch-icon-box--sm stitch-icon-box--autumn"><WgIcon name="truck" :size="18" :color="AUTUMN_500" /></view>
          <text class="menu-item__label">车辆管理</text>
          <WgIcon name="chevron-right" :size="16" :color="WARM_300" />
        </view>
      </view>
    </view>

    <!-- ===== 系统 ===== -->
    <view class="section">
      <view class="menu-card stitch-card" style="padding: 0;">
        <view class="menu-item menu-item--last" @tap="goSettings">
          <view class="stitch-icon-box stitch-icon-box--sm stitch-icon-box--warm"><WgIcon name="settings" :size="18" :color="WARM_500" /></view>
          <text class="menu-item__label">设置</text>
          <WgIcon name="chevron-right" :size="16" :color="WARM_300" />
        </view>
      </view>
    </view>

    <!-- ===== 退出登录 ===== -->
    <view v-if="isLoggedIn" class="logout-section">
      <button class="logout-btn" @tap="handleLogout">退出登录</button>
    </view>

    <view style="height: 160rpx;" />
    <WgTabBar :current="4" />
  </view>
</template>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: $bg-page;
}

/* ===== Hero Header ===== */
.hero {
  padding-bottom: $spacing-3xl;

  &__safe {
    height: 0;
  }

  &__content {
    padding: $spacing-xl $spacing-xl 0;
  }

  &__user,
  &__guest {
    display: flex;
    align-items: center;
    gap: $spacing-lg;
  }

  &__avatar-wrap {
    flex-shrink: 0;
  }

  &__avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    border: 4rpx solid rgba(255, 255, 255, 0.3);

    &--text {
      background: rgba(255, 255, 255, 0.15);
      display: flex;
      align-items: center;
      justify-content: center;
    }

    &--guest {
      background: rgba(255, 255, 255, 0.15);
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  &__avatar-char {
    font-size: $font-3xl;
    font-weight: 800;
    color: $text-inverse;
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__name {
    display: block;
    color: $text-inverse;
    font-size: $font-xl;
    font-weight: 800;
    margin-bottom: 4rpx;
  }

  &__company {
    display: block;
    color: rgba(255, 255, 255, 0.7);
    font-size: $font-sm;
    margin-bottom: $spacing-sm;
  }

  &__roles {
    display: flex;
    gap: $spacing-xs;
  }

  &__role {
    font-size: $font-xs;
    padding: 4rpx 18rpx;
    border-radius: $radius-full;
    background: rgba(255, 255, 255, 0.18);
    color: $text-inverse;
    font-weight: 600;

    &--gold {
      background: rgba(255, 200, 100, 0.25);
      color: #FFF2D0;
    }
  }

  &__edit-btn {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.15);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &:active {
      background: rgba(255, 255, 255, 0.25);
    }
  }
}

/* ===== 统计卡片 (浮动) ===== */
.stats-float {
  margin: -#{$spacing-xl} $spacing-lg $spacing-md;
  position: relative;
  z-index: 2;
}

/* ===== 待办提醒 ===== */
.pending-bar {
  display: flex;
  gap: $spacing-sm;
  margin: 0 $spacing-lg $spacing-md;
}

.pending-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-md $spacing-xs;
  background: $bg-card;
  border-radius: $radius-xl;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
  transition: transform $transition-fast;

  &:active { transform: scale(0.95); }

  &__badge {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__count {
    font-size: $font-lg;
    font-weight: 800;
    font-family: $font-mono;
    color: $text-inverse;
  }

  &__label {
    font-size: $font-xs;
    color: $text-secondary;
    font-weight: 500;
  }
}

.stats-row {
  display: flex;
  align-items: center;
  background: $bg-card;
  border-radius: $radius-2xl;
  padding: $spacing-xl 0;
  box-shadow: 0 4rpx 20rpx rgba(120, 90, 50, 0.06);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;

  &__value {
    font-size: $font-2xl;
    font-weight: 800;
    color: $text-primary;
    margin-bottom: 4rpx;
  }

  &__label {
    font-size: $font-xs;
    color: $text-secondary;
  }
}

.stat-divider {
  width: 1rpx;
  height: 56rpx;
  background: $warm-200;
}

/* ===== Section ===== */
.section {
  padding: $spacing-lg $spacing-lg 0;
}

/* ===== 功能网格 ===== */
.func-grid {
  display: flex;
  flex-wrap: wrap;
  margin-top: $spacing-md;
}

.func-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-lg 0;
  transition: transform $transition-fast;

  &:active {
    transform: scale(0.92);
  }

  &__label {
    font-size: 22rpx;
    color: $text-secondary;
    font-weight: 500;
    margin-top: $spacing-xs;
  }
}

/* ===== 菜单卡片 ===== */
.menu-item {
  display: flex;
  align-items: center;
  padding: $spacing-lg $spacing-xl;
  gap: $spacing-md;
  border-bottom: 1rpx solid $warm-100;
  transition: background $transition-fast;

  &--last {
    border-bottom: none;
  }

  &:active {
    background: $warm-50;
  }

  &__label {
    flex: 1;
    font-size: $font-base;
    color: $text-primary;
    font-weight: 500;
  }
}

/* ===== 退出 ===== */
.logout-section {
  padding: $spacing-2xl $spacing-lg;
}

.logout-btn {
  background: transparent;
  color: $color-error;
  border: 1rpx solid $warm-200;
  border-radius: $radius-full;
  font-size: $font-md;
  font-weight: 600;
  height: 92rpx;
  line-height: 92rpx;

  &:active {
    background: rgba(239, 68, 68, 0.05);
  }
}
</style>
