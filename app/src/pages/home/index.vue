<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPlatformStats, type StatsResponse } from '../../api/stats'
import { listSupplies, type SupplyResponse } from '../../api/supply'
import { listRequirements, type RequirementResponse } from '../../api/requirement'
import { formatPrice, formatRelativeTime } from '../../utils/format'

const stats = ref<StatsResponse | null>(null)
const hotSupplies = ref<SupplyResponse[]>([])
const hotRequirements = ref<RequirementResponse[]>([])
const loading = ref(true)

onMounted(async () => {
  await loadData()
})

async function loadData() {
  loading.value = true
  try {
    const [statsRes, suppliesRes, requirementsRes] = await Promise.allSettled([
      getPlatformStats(),
      listSupplies({ activeOnly: true, orderBy: 'create_time', order: 'desc' }),
      listRequirements({ includeExpired: false, orderBy: 'create_time', order: 'desc' }),
    ])
    if (statsRes.status === 'fulfilled') stats.value = statsRes.value
    if (suppliesRes.status === 'fulfilled') hotSupplies.value = suppliesRes.value?.slice(0, 6) || []
    if (requirementsRes.status === 'fulfilled') hotRequirements.value = requirementsRes.value?.slice(0, 6) || []
  } finally {
    loading.value = false
  }
}

function navigateTo(url: string) {
  uni.navigateTo({ url })
}

function switchTab(url: string) {
  uni.switchTab({ url })
}

function goSupplyDetail(id: number) {
  uni.navigateTo({ url: `/pages/supply/detail?id=${id}` })
}

function goRequirementDetail(id: number) {
  uni.navigateTo({ url: `/pages/requirement/detail?id=${id}` })
}

</script>

<template>
  <view class="home-page">
    <!-- 自定义导航栏 -->
    <view class="nav-bar">
      <view class="nav-bar__content">
        <text class="nav-bar__title">沃谷</text>
        <view class="nav-bar__search" @tap="navigateTo('/pages/search/index')">
          <uni-icons type="search" size="16" color="rgba(255,255,255,0.7)" />
          <text class="nav-bar__search-text">搜索供应/采购/企业</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="page-content">
      <!-- 平台数据 -->
      <view v-if="stats" class="stats-bar anim-slide-up">
        <view class="stats-bar__item">
          <text class="stats-bar__value">{{ stats.supplyCount }}</text>
          <text class="stats-bar__label">供应</text>
        </view>
        <view class="stats-bar__item">
          <text class="stats-bar__value">{{ stats.requirementCount }}</text>
          <text class="stats-bar__label">采购</text>
        </view>
        <view class="stats-bar__item">
          <text class="stats-bar__value">{{ stats.supplierCount }}</text>
          <text class="stats-bar__label">供应商</text>
        </view>
        <view class="stats-bar__item">
          <text class="stats-bar__value">{{ stats.buyerCount }}</text>
          <text class="stats-bar__label">采购商</text>
        </view>
      </view>

      <!-- 快捷入口 -->
      <view class="quick-entry anim-slide-up anim-stagger-1">
        <view class="quick-entry__item tap-feedback" @tap="switchTab('/pages/supply/index')">
          <view class="quick-entry__icon quick-entry__icon--brand"><uni-icons type="shop" size="28" color="#fff" /></view>
          <text class="quick-entry__label">供应大厅</text>
        </view>
        <view class="quick-entry__item tap-feedback" @tap="switchTab('/pages/requirement/index')">
          <view class="quick-entry__icon quick-entry__icon--autumn"><uni-icons type="cart" size="28" color="#fff" /></view>
          <text class="quick-entry__label">采购大厅</text>
        </view>
        <view class="quick-entry__item tap-feedback" @tap="navigateTo('/pages/topic/square')">
          <view class="quick-entry__icon quick-entry__icon--action"><uni-icons type="chatbubble" size="28" color="#fff" /></view>
          <text class="quick-entry__label">话题广场</text>
        </view>
        <view class="quick-entry__item tap-feedback" @tap="navigateTo('/pages/supply/publish')">
          <view class="quick-entry__icon quick-entry__icon--accent"><uni-icons type="compose" size="28" color="#fff" /></view>
          <text class="quick-entry__label">发布信息</text>
        </view>
      </view>

      <!-- 热门供应 -->
      <view class="section">
        <view class="section__header">
          <text class="section__title">最新供应</text>
          <text class="section__more" @tap="switchTab('/pages/supply/index')">更多 <uni-icons type="right" size="14" color="#999" /></text>
        </view>
        <WgEmpty v-if="hotSupplies.length === 0 && !loading" text="暂无供应信息" />
        <view v-for="item in hotSupplies" :key="item.id" class="card tap-feedback" @tap="goSupplyDetail(item.id)">
          <view class="card__header">
            <text class="card__category">{{ item.categoryName }}</text>
            <text class="card__price">{{ formatPrice(item.exFactoryPrice) }}</text>
          </view>
          <view class="card__body">
            <text class="card__company">{{ item.companyName || item.nickName || item.userName }}</text>
            <text v-if="item.origin" class="card__tag">{{ item.origin }}</text>
            <text v-if="item.quantity" class="card__tag">{{ item.quantity }}吨</text>
          </view>
          <view class="card__footer">
            <text class="card__time">{{ formatRelativeTime(item.createTime) }}</text>
            <text v-if="item.shipAddress" class="card__address">{{ item.shipAddress }}</text>
          </view>
        </view>
      </view>

      <!-- 热门采购 -->
      <view class="section">
        <view class="section__header">
          <text class="section__title">最新采购</text>
          <text class="section__more" @tap="switchTab('/pages/requirement/index')">更多 <uni-icons type="right" size="14" color="#999" /></text>
        </view>
        <WgEmpty v-if="hotRequirements.length === 0 && !loading" text="暂无采购信息" />
        <view v-for="item in hotRequirements" :key="item.id" class="card card--autumn tap-feedback" @tap="goRequirementDetail(item.id)">
          <view class="card__header">
            <text class="card__category">{{ item.categoryName }}</text>
            <text class="card__price card__price--autumn">{{ formatPrice(item.expectedPrice) }}</text>
          </view>
          <view class="card__body">
            <text class="card__company">{{ item.companyName || item.nickName || item.userName }}</text>
            <text v-if="item.quantity" class="card__tag">{{ item.quantity }}吨</text>
          </view>
          <view class="card__footer">
            <text class="card__time">{{ formatRelativeTime(item.createTime) }}</text>
            <text v-if="item.purchaseAddress" class="card__address">{{ item.purchaseAddress }}</text>
          </view>
        </view>
      </view>

      <!-- 底部间距 -->
      <view class="page-bottom-spacer"></view>
    </scroll-view>
  </view>
</template>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background: $bg-page;
}

.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: linear-gradient(135deg, $brand-700, $brand-600);

  &__content {
    display: flex;
    align-items: center;
    padding: 20rpx 24rpx;
    padding-top: calc(var(--status-bar-height, 25px) + 20rpx);
  }

  &__title {
    color: #fff;
    font-size: $font-xl;
    font-weight: bold;
    margin-right: 20rpx;
    flex-shrink: 0;
  }

  &__search {
    flex: 1;
    height: 64rpx;
    background: rgba(255, 255, 255, 0.2);
    border-radius: $radius-lg;
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 0 24rpx;
  }

  &__search-text {
    color: rgba(255, 255, 255, 0.7);
    font-size: $font-sm;
  }
}

.page-content {
  height: 100vh;
  padding-top: 170rpx;
}

.stats-bar {
  display: flex;
  justify-content: space-around;
  background: $bg-card;
  margin: $spacing-sm;
  border-radius: $radius-lg;
  padding: $spacing-md 0;

  &__item {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  &__value {
    font-size: $font-xl;
    font-weight: bold;
    color: $brand-600;
  }

  &__label {
    font-size: $font-xs;
    color: $text-secondary;
    margin-top: 4rpx;
  }
}

.quick-entry {
  display: flex;
  justify-content: space-around;
  padding: $spacing-md;
  background: $bg-card;
  margin: 0 $spacing-sm $spacing-sm;
  border-radius: $radius-lg;

  &__item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $spacing-xs;
  }

  &__icon {
    width: 88rpx;
    height: 88rpx;
    border-radius: $radius-lg;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: $font-lg;
    font-weight: bold;
    color: #fff;

    &--brand { background: $brand-600; }
    &--autumn { background: $autumn-400; }
    &--action { background: $action-600; }
    &--accent { background: $accent-400; }
  }

  &__label {
    font-size: $font-xs;
    color: $text-secondary;
  }
}

.section {
  background: $bg-card;
  margin: 0 $spacing-sm $spacing-sm;
  border-radius: $radius-lg;
  padding: $spacing-md;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-md;
  }

  &__title {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;
  }

  &__more {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

.card {
  padding: $spacing-md;
  border-bottom: 1rpx solid $border-light;
  &:last-child { border-bottom: none; }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-xs;
  }

  &__category {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
  }

  &__price {
    font-size: $font-lg;
    font-weight: bold;
    color: $accent-400;

    &--autumn { color: $autumn-500; }
  }

  &__body {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: $spacing-xs;
    margin-bottom: $spacing-xs;
  }

  &__company {
    font-size: $font-sm;
    color: $text-secondary;
  }

  &__tag {
    font-size: $font-xs;
    color: $text-secondary;
    background: $bg-page;
    padding: 2rpx 12rpx;
    border-radius: $radius-sm;
  }

  &__footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  &__time {
    font-size: $font-xs;
    color: $text-placeholder;
  }

  &__address {
    font-size: $font-xs;
    color: $text-placeholder;
    max-width: 300rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.page-bottom-spacer {
  height: 30rpx;
}
</style>
