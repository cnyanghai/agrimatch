<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getPlatformStats, type StatsResponse } from '../../api/stats'
import { listSupplies, type SupplyResponse } from '../../api/supply'
import { listRequirements, type RequirementResponse } from '../../api/requirement'
import { listPosts, type PostResponse } from '../../api/post'
import { getPendingCount } from '../../api/dashboard'
import { listFuturesContracts, type FuturesContractResponse } from '../../api/futures'
import { formatPrice, formatRelativeTime } from '../../utils/format'
import { useAuthStore } from '../../store/auth'

const authStore = useAuthStore()
const stats = ref<StatsResponse | null>(null)
const supplies = ref<SupplyResponse[]>([])
const requirements = ref<RequirementResponse[]>([])
const hotPosts = ref<PostResponse[]>([])
const pendingCount = ref(0)
const marketPrices = ref<FuturesContractResponse[]>([])
const loading = ref(true)

// 时段问候语
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 11) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

// 品类快捷入口
const categories = [
  { name: '玉米', icon: '🌽' },
  { name: '水稻', icon: '🌾' },
  { name: '大豆', icon: '🫘' },
  { name: '小麦', icon: '🌿' },
  { name: '棉花', icon: '☁️' },
  { name: '花生', icon: '🥜' },
  { name: '饲料', icon: '🌱' },
  { name: '更多', icon: '📋' },
]

// 混合信息流
interface FeedItem {
  id: number
  type: 'supply' | 'requirement'
  categoryName: string
  price: number
  companyName: string
  origin?: string
  quantity?: number
  address?: string
  createTime?: string
}

const feedList = computed<FeedItem[]>(() => {
  const supplyItems: FeedItem[] = supplies.value.map(s => ({
    id: s.id,
    type: 'supply',
    categoryName: s.categoryName,
    price: s.exFactoryPrice,
    companyName: s.companyName || s.nickName || s.userName || '',
    origin: s.origin,
    quantity: s.quantity,
    address: s.shipAddress,
    createTime: s.createTime
  }))
  const reqItems: FeedItem[] = requirements.value.map(r => ({
    id: r.id,
    type: 'requirement',
    categoryName: r.categoryName,
    price: r.expectedPrice || 0,
    companyName: r.companyName || r.nickName || r.userName || '',
    quantity: r.quantity,
    address: r.purchaseAddress,
    createTime: r.createTime
  }))

  // 按时间排序混合
  return [...supplyItems, ...reqItems]
    .sort((a, b) => {
      const ta = a.createTime ? new Date(a.createTime).getTime() : 0
      const tb = b.createTime ? new Date(b.createTime).getTime() : 0
      return tb - ta
    })
    .slice(0, 20)
})

// 双列分配
const leftColumn = computed(() => feedList.value.filter((_, i) => i % 2 === 0))
const rightColumn = computed(() => feedList.value.filter((_, i) => i % 2 === 1))

onMounted(() => loadData())

onPullDownRefresh(() => {
  loadData().finally(() => uni.stopPullDownRefresh())
})

async function loadData() {
  loading.value = true
  try {
    const pendingPromise = authStore.isLoggedIn ? getPendingCount().catch(() => 0) : Promise.resolve(0)
    const [statsRes, suppliesRes, requirementsRes, postsRes, pendingRes, marketRes] = await Promise.allSettled([
      getPlatformStats(),
      listSupplies({ activeOnly: true, orderBy: 'create_time', order: 'desc' }),
      listRequirements({ includeExpired: false, orderBy: 'create_time', order: 'desc' }),
      listPosts({ orderBy: 'like_count', limit: 5 }),
      pendingPromise,
      listFuturesContracts(),
    ])
    if (statsRes.status === 'fulfilled') stats.value = statsRes.value
    if (suppliesRes.status === 'fulfilled') supplies.value = suppliesRes.value?.slice(0, 12) || []
    if (requirementsRes.status === 'fulfilled') requirements.value = requirementsRes.value?.slice(0, 12) || []
    if (postsRes.status === 'fulfilled') hotPosts.value = postsRes.value?.slice(0, 5) || []
    if (pendingRes.status === 'fulfilled') pendingCount.value = (pendingRes.value as number) || 0
    if (marketRes.status === 'fulfilled') marketPrices.value = ((marketRes.value as FuturesContractResponse[]) || []).slice(0, 8)
  } finally {
    loading.value = false
  }
}

function navigateTo(url: string) {
  uni.navigateTo({ url })
}

function goCategory(name: string) {
  if (name === '更多') {
    navigateTo('/pages/search/index')
  } else {
    navigateTo(`/pages/search/index?keyword=${encodeURIComponent(name)}`)
  }
}

function goFeedDetail(item: FeedItem) {
  if (item.type === 'supply') {
    navigateTo(`/pages/supply/detail?id=${item.id}`)
  } else {
    navigateTo(`/pages/requirement/detail?id=${item.id}`)
  }
}
</script>

<template>
  <view class="home">
    <!-- Hero 渐变头部 -->
    <view class="hero">
      <view class="hero__nav safe-area-top">
        <image class="hero__logo" src="/static/logo-white.svg" mode="aspectFit" />
        <text class="hero__brand">沃谷</text>
        <view class="hero__spacer" />
        <view class="hero__bell" @tap="navigateTo('/pages/notify/index')">
          <uni-icons type="bell" size="20" color="rgba(255,255,255,0.9)" />
          <view v-if="pendingCount > 0" class="hero__bell-badge">
            <text class="hero__bell-badge-text">{{ pendingCount > 99 ? '99+' : pendingCount }}</text>
          </view>
        </view>
        <view class="hero__search" @tap="navigateTo('/pages/search/index')">
          <uni-icons type="search" size="16" color="rgba(255,255,255,0.7)" />
          <text class="hero__search-text">搜索</text>
        </view>
      </view>

      <view class="hero__greeting">
        <text class="hero__greeting-text">{{ greeting }}，欢迎来到沃谷</text>
        <text class="hero__subtitle">农牧供需智能匹配平台</text>
      </view>

      <!-- 统计数据 -->
      <view v-if="stats" class="hero__stats">
        <view class="hero__stat">
          <text class="hero__stat-value">{{ stats.supplyCount || 0 }}</text>
          <text class="hero__stat-label">供应</text>
        </view>
        <view class="hero__stat-divider" />
        <view class="hero__stat">
          <text class="hero__stat-value">{{ stats.requirementCount || 0 }}</text>
          <text class="hero__stat-label">采购</text>
        </view>
        <view class="hero__stat-divider" />
        <view class="hero__stat">
          <text class="hero__stat-value">{{ stats.supplierCount || 0 }}</text>
          <text class="hero__stat-label">企业</text>
        </view>
      </view>
    </view>

    <!-- 品类快捷入口 -->
    <view class="categories">
      <scroll-view scroll-x class="categories__scroll" :show-scrollbar="false">
        <view class="categories__list">
          <view
            v-for="cat in categories"
            :key="cat.name"
            class="categories__item tap-feedback"
            @tap="goCategory(cat.name)"
          >
            <view class="categories__icon">
              <text class="categories__emoji">{{ cat.icon }}</text>
            </view>
            <text class="categories__name">{{ cat.name }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 行情速览 -->
    <view v-if="marketPrices.length > 0" class="market-ticker">
      <view class="market-ticker__header">
        <text class="market-ticker__title">行情速览</text>
        <text class="market-ticker__more" @tap="navigateTo('/pages/market/index')">更多 ></text>
      </view>
      <scroll-view scroll-x class="market-ticker__scroll" :show-scrollbar="false">
        <view class="market-ticker__list">
          <view
            v-for="item in marketPrices"
            :key="item.contractCode"
            class="ticker-card tap-feedback"
            @tap="navigateTo('/pages/market/index')"
          >
            <text class="ticker-card__name">{{ item.productName }}</text>
            <text class="ticker-card__code">{{ item.contractCode }}</text>
            <text class="ticker-card__price">{{ item.lastPrice ? '¥' + item.lastPrice : '-' }}</text>
            <text
              class="ticker-card__change"
              :class="{
                'ticker-card__change--up': (item.changePrice || 0) > 0,
                'ticker-card__change--down': (item.changePrice || 0) < 0,
              }"
            >
              {{ (item.changePrice || 0) > 0 ? '+' : '' }}{{ item.changePercent?.toFixed(2) || '0.00' }}%
            </text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 热门话题 -->
    <view v-if="hotPosts.length > 0" class="hot-topics">
      <view class="hot-topics__header">
        <text class="hot-topics__title">热门话题</text>
        <text class="hot-topics__more" @tap="navigateTo('/pages/topic/square')">查看更多 ></text>
      </view>
      <scroll-view scroll-x class="hot-topics__scroll" :show-scrollbar="false">
        <view class="hot-topics__list">
          <view
            v-for="post in hotPosts"
            :key="post.id"
            class="hot-topic-card tap-feedback"
            @tap="navigateTo(`/pages/topic/detail?id=${post.id}`)"
          >
            <text class="hot-topic-card__title">{{ post.title }}</text>
            <text v-if="post.content" class="hot-topic-card__desc">
              {{ post.content.length > 40 ? post.content.slice(0, 40) + '...' : post.content }}
            </text>
            <view class="hot-topic-card__footer">
              <uni-icons type="heart-filled" size="12" color="#999" />
              <text class="hot-topic-card__stat">{{ post.likeCount || 0 }}</text>
              <uni-icons type="chat" size="12" color="#999" />
              <text class="hot-topic-card__stat">{{ post.commentCount || 0 }}</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 最新动态 -->
    <view class="feed">
      <view class="feed__header">
        <text class="feed__title">最新动态</text>
        <view class="feed__tabs">
          <text class="feed__tab feed__tab--supply" @tap="navigateTo('/pages/supply/index')">供应</text>
          <text class="feed__tab feed__tab--req" @tap="navigateTo('/pages/requirement/index')">采购</text>
        </view>
      </view>

      <!-- 骨架屏 -->
      <WgSkeleton v-if="loading" type="card" :rows="4" />

      <!-- 双列卡片 -->
      <view v-else-if="feedList.length > 0" class="feed__grid">
        <view class="feed__col">
          <view
            v-for="item in leftColumn"
            :key="`${item.type}-${item.id}`"
            class="feed-card tap-feedback anim-fade-up"
            :class="{ 'feed-card--supply': item.type === 'supply', 'feed-card--req': item.type === 'requirement' }"
            @tap="goFeedDetail(item)"
          >
            <view class="feed-card__badge">
              <text class="feed-card__badge-text">{{ item.type === 'supply' ? '供' : '采' }}</text>
            </view>
            <text class="feed-card__category">{{ item.categoryName }}</text>
            <text class="feed-card__price">{{ formatPrice(item.price) }}</text>
            <text class="feed-card__company ellipsis">{{ item.companyName }}</text>
            <view class="feed-card__footer">
              <text v-if="item.quantity" class="feed-card__tag">{{ item.quantity }}吨</text>
              <text v-if="item.origin" class="feed-card__tag">{{ item.origin }}</text>
            </view>
            <text class="feed-card__time">{{ formatRelativeTime(item.createTime) }}</text>
          </view>
        </view>
        <view class="feed__col">
          <view
            v-for="item in rightColumn"
            :key="`${item.type}-${item.id}`"
            class="feed-card tap-feedback anim-fade-up"
            :class="{ 'feed-card--supply': item.type === 'supply', 'feed-card--req': item.type === 'requirement' }"
            @tap="goFeedDetail(item)"
          >
            <view class="feed-card__badge">
              <text class="feed-card__badge-text">{{ item.type === 'supply' ? '供' : '采' }}</text>
            </view>
            <text class="feed-card__category">{{ item.categoryName }}</text>
            <text class="feed-card__price">{{ formatPrice(item.price) }}</text>
            <text class="feed-card__company ellipsis">{{ item.companyName }}</text>
            <view class="feed-card__footer">
              <text v-if="item.quantity" class="feed-card__tag">{{ item.quantity }}吨</text>
              <text v-if="item.origin" class="feed-card__tag">{{ item.origin }}</text>
            </view>
            <text class="feed-card__time">{{ formatRelativeTime(item.createTime) }}</text>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <WgEmpty v-else text="暂无动态" description="信息流为空，下拉刷新试试" />
    </view>

    <!-- 底部间距 -->
    <view style="height: 140rpx;" />

    <WgTabBar :current="0" :badges="pendingCount > 0 ? { 3: pendingCount } : undefined" />
  </view>
</template>

<style lang="scss" scoped>
.home {
  min-height: 100vh;
  background: $bg-page;
}

/* ===== Hero ===== */
.hero {
  background: linear-gradient(180deg, $brand-700 0%, $brand-600 60%, $brand-500 100%);
  padding-bottom: $spacing-lg;

  &__nav {
    display: flex;
    align-items: center;
    padding: $spacing-sm $spacing-md;
    gap: $spacing-xs;
  }

  &__logo {
    width: 48rpx;
    height: 48rpx;
  }

  &__brand {
    font-size: $font-xl;
    font-weight: 800;
    color: #fff;
  }

  &__spacer {
    flex: 1;
  }

  &__search {
    display: flex;
    align-items: center;
    gap: 8rpx;
    height: 60rpx;
    padding: 0 24rpx;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 30rpx;
  }

  &__search-text {
    color: rgba(255, 255, 255, 0.7);
    font-size: $font-sm;
  }

  &__greeting {
    padding: $spacing-md $spacing-md 0;
  }

  &__greeting-text {
    display: block;
    font-size: $font-xl;
    font-weight: 700;
    color: #fff;
  }

  &__subtitle {
    display: block;
    font-size: $font-sm;
    color: rgba(255, 255, 255, 0.6);
    margin-top: 4rpx;
  }

  &__bell {
    position: relative;
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: $spacing-xs;
  }

  &__bell-badge {
    position: absolute;
    top: 4rpx;
    right: 0;
    min-width: 28rpx;
    height: 28rpx;
    background: $color-error;
    border-radius: 14rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 6rpx;
  }

  &__bell-badge-text {
    color: #fff;
    font-size: 18rpx;
    font-weight: bold;
    line-height: 28rpx;
  }

  &__stats {
    display: flex;
    align-items: center;
    justify-content: center;
    margin: $spacing-lg $spacing-md 0;
    padding: $spacing-md 0;
    background: rgba(255, 255, 255, 0.12);
    border-radius: $radius-lg;
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
    background: rgba(255, 255, 255, 0.2);
  }
}

/* ===== Categories ===== */
.categories {
  margin: -20rpx $spacing-sm 0;
  position: relative;
  z-index: 2;

  &__scroll {
    white-space: nowrap;
  }

  &__list {
    display: inline-flex;
    gap: 0;
    background: $bg-card;
    border-radius: $radius-xl;
    padding: $spacing-md $spacing-sm;
    box-shadow: $shadow-md;
  }

  &__item {
    display: inline-flex;
    flex-direction: column;
    align-items: center;
    width: 120rpx;
    gap: $spacing-xs;
  }

  &__icon {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: $brand-50;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__emoji {
    font-size: 40rpx;
  }

  &__name {
    font-size: $font-xs;
    color: $text-secondary;
    white-space: nowrap;
  }
}

/* ===== Market Ticker ===== */
.market-ticker {
  padding: $spacing-md $spacing-sm 0;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-sm;
    padding: 0 $spacing-xs;
  }

  &__title {
    font-size: $font-lg;
    font-weight: 700;
    color: $text-primary;
  }

  &__more {
    font-size: $font-sm;
    color: $text-secondary;
  }

  &__scroll {
    white-space: nowrap;
  }

  &__list {
    display: inline-flex;
    gap: $spacing-sm;
    padding: 0 $spacing-xs $spacing-xs;
  }
}

.ticker-card {
  width: 200rpx;
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md;
  box-shadow: $shadow-sm;
  display: inline-flex;
  flex-direction: column;
  white-space: normal;

  &__name {
    font-size: $font-sm;
    color: $text-secondary;
    margin-bottom: 2rpx;
  }

  &__code {
    font-size: $font-xs;
    color: $text-placeholder;
    margin-bottom: $spacing-xs;
  }

  &__price {
    font-size: $font-lg;
    font-weight: 800;
    color: $text-primary;
    margin-bottom: 2rpx;
  }

  &__change {
    font-size: $font-sm;
    font-weight: 600;
    color: $text-secondary;

    &--up {
      color: $color-error;
    }

    &--down {
      color: $color-success;
    }
  }
}

/* ===== Hot Topics ===== */
.hot-topics {
  padding: $spacing-md $spacing-sm 0;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-sm;
    padding: 0 $spacing-xs;
  }

  &__title {
    font-size: $font-lg;
    font-weight: 700;
    color: $text-primary;
  }

  &__more {
    font-size: $font-sm;
    color: $text-secondary;
  }

  &__scroll {
    white-space: nowrap;
  }

  &__list {
    display: inline-flex;
    gap: $spacing-sm;
    padding: 0 $spacing-xs $spacing-xs;
  }
}

.hot-topic-card {
  width: 280rpx;
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md;
  box-shadow: $shadow-sm;
  display: inline-flex;
  flex-direction: column;
  white-space: normal;

  &__title {
    font-size: $font-md;
    font-weight: 700;
    color: $text-primary;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    margin-bottom: $spacing-xs;
  }

  &__desc {
    font-size: $font-xs;
    color: $text-secondary;
    line-height: 1.5;
    flex: 1;
    margin-bottom: $spacing-sm;
  }

  &__footer {
    display: flex;
    align-items: center;
    gap: 6rpx;
  }

  &__stat {
    font-size: $font-xs;
    color: $text-placeholder;
    margin-right: $spacing-xs;
  }
}

/* ===== Feed ===== */
.feed {
  padding: $spacing-md $spacing-sm 0;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-md;
    padding: 0 $spacing-xs;
  }

  &__title {
    font-size: $font-lg;
    font-weight: 700;
    color: $text-primary;
  }

  &__tabs {
    display: flex;
    gap: $spacing-sm;
  }

  &__tab {
    font-size: $font-sm;
    padding: 4rpx 20rpx;
    border-radius: 20rpx;

    &--supply {
      color: $brand-600;
      background: $brand-50;
    }

    &--req {
      color: $autumn-500;
      background: $autumn-50;
    }
  }

  &__grid {
    display: flex;
    gap: $spacing-sm;
  }

  &__col {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
  }
}

/* ===== Feed Card ===== */
.feed-card {
  background: $bg-card;
  border-radius: $radius-xl;
  padding: $spacing-md;
  box-shadow: $shadow-sm;
  position: relative;
  overflow: hidden;

  &--supply {
    border-left: 6rpx solid $brand-500;
  }

  &--req {
    border-left: 6rpx solid $autumn-400;
  }

  &__badge {
    position: absolute;
    top: 0;
    right: 0;
    padding: 4rpx 16rpx;
    border-radius: 0 $radius-xl 0 $radius-lg;
  }

  .feed-card--supply &__badge {
    background: $brand-50;
  }

  .feed-card--req &__badge {
    background: $autumn-50;
  }

  &__badge-text {
    font-size: $font-xs;
    font-weight: 700;
  }

  .feed-card--supply &__badge-text {
    color: $brand-600;
  }

  .feed-card--req &__badge-text {
    color: $autumn-500;
  }

  &__category {
    display: block;
    font-size: $font-md;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: 4rpx;
    padding-right: 60rpx;
  }

  &__price {
    display: block;
    font-size: $font-lg;
    font-weight: 800;
    color: $accent-400;
    margin-bottom: $spacing-xs;
  }

  &__company {
    display: block;
    font-size: $font-xs;
    color: $text-secondary;
    margin-bottom: $spacing-xs;
  }

  &__footer {
    display: flex;
    flex-wrap: wrap;
    gap: 6rpx;
    margin-bottom: $spacing-xs;
  }

  &__tag {
    font-size: 18rpx;
    color: $text-secondary;
    background: $bg-page;
    padding: 2rpx 10rpx;
    border-radius: $radius-sm;
  }

  &__time {
    display: block;
    font-size: 18rpx;
    color: $text-placeholder;
  }
}
</style>
