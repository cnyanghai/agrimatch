<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { BRAND_600, WARM_400, WARM_500, ACCENT_400, AUTUMN_500, ACTION_600, WHITE } from '../../constants/colors'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { listPosts, type PostResponse } from '../../api/post'
import { getFollowedPosts } from '../../api/follow'
import { useAuthStore } from '../../store/auth'
import { formatRelativeTime } from '../../utils/format'

const authStore = useAuthStore()
const allPosts = ref<PostResponse[]>([])
const followingPosts = ref<PostResponse[]>([])
const followingLoading = ref(false)
const loading = ref(true)
const sortMode = ref<'latest' | 'hottest' | 'following'>('latest')
const displayCount = ref(15)
const PAGE_SIZE = 15

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const userName = computed(() => {
  if (!authStore.isLoggedIn) return ''
  return authStore.user?.nickName || authStore.user?.userName || ''
})

const filteredList = computed(() => {
  if (sortMode.value === 'following') return followingPosts.value
  const list = [...allPosts.value]
  if (sortMode.value === 'hottest') {
    list.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
  }
  return list
})

const displayList = computed(() => filteredList.value.slice(0, displayCount.value))

const loadStatus = computed<'loading' | 'more' | 'noMore'>(() => {
  if (loading.value) return 'loading'
  if (displayCount.value >= filteredList.value.length) return 'noMore'
  return 'more'
})

watch(sortMode, (mode) => {
  displayCount.value = PAGE_SIZE
  if (mode === 'following' && authStore.isLoggedIn) {
    loadFollowingPosts()
  }
})

async function loadFollowingPosts() {
  followingLoading.value = true
  try {
    followingPosts.value = await getFollowedPosts() || []
  } catch {
    // handled
  } finally {
    followingLoading.value = false
  }
}

onMounted(() => loadData())

onReachBottom(() => {
  if (loadStatus.value === 'more') {
    displayCount.value += PAGE_SIZE
  }
})

onPullDownRefresh(() => {
  loadData().finally(() => uni.stopPullDownRefresh())
})

async function loadData() {
  loading.value = true
  try {
    const res = await listPosts({ orderBy: 'create_time', limit: 100 })
    allPosts.value = res || []
    displayCount.value = PAGE_SIZE
  } finally {
    loading.value = false
  }
}

function loadMore() {
  displayCount.value += PAGE_SIZE
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/topic/detail?id=${id}` })
}

function goLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}

function goPublish() {
  if (!authStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  uni.navigateTo({ url: '/pages/topic/publish' })
}

function goSearch() {
  uni.navigateTo({ url: '/pages/search/index' })
}

function goSupplyHall() {
  uni.switchTab({ url: '/pages/supply/index' })
}

function goRequirementHall() {
  uni.switchTab({ url: '/pages/requirement/index' })
}

function goContracts() {
  uni.navigateTo({ url: '/pages/contract/list' })
}

function goDirectory() {
  uni.navigateTo({ url: '/pages/company/directory' })
}

function goMap() {
  uni.navigateTo({ url: '/pages/map/index' })
}

function goCategoryDirectory() {
  uni.navigateTo({ url: '/pages/category/directory' })
}

function getInitial(item: PostResponse): string {
  const name = item.nickName || item.userName || item.companyName || '?'
  return name.charAt(0)
}

function getDisplayName(item: PostResponse): string {
  return item.nickName || item.userName || '匿名用户'
}

function getAuthorLine(item: PostResponse): string {
  const parts: string[] = [getDisplayName(item)]
  if (item.companyName) parts.push(item.companyName)
  if (item.position) parts.push(item.position)
  return parts.join(' · ')
}

function stripHtml(html: string): string {
  return html.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').replace(/&amp;/g, '&').replace(/&lt;/g, '<').replace(/&gt;/g, '>').trim()
}

function getPreview(content?: string): string {
  if (!content) return ''
  const text = stripHtml(content)
  return text.length > 100 ? text.slice(0, 100) + '...' : text
}

function getImages(item: PostResponse): string[] {
  if (!item.imagesJson) return []
  try {
    const imgs = JSON.parse(item.imagesJson)
    return Array.isArray(imgs) ? imgs.slice(0, 3) : []
  } catch {
    return []
  }
}
</script>

<template>
  <view class="home">
    <!-- ===== Hero Header (品牌色渐变) ===== -->
    <view class="hero stitch-hero">
      <view class="hero__safe safe-area-top" />
      <view class="hero__content">
        <view class="hero__greeting">
          <text class="hero__greeting-text stitch-page-title">{{ greeting }}</text>
          <text v-if="userName" class="hero__greeting-name stitch-page-subtitle">{{ userName }}，探索今日农贸动态</text>
          <text v-else class="hero__greeting-name stitch-page-subtitle">探索今日农贸动态</text>
        </view>
        <view class="hero__search stitch-search" @tap="goSearch">
          <WgIcon name="search" :size="18" :color="WHITE" />
          <text class="stitch-search__text">搜索话题、用户、企业...</text>
        </view>
      </view>
    </view>

    <!-- ===== 快捷入口 (2行3列 → 大图标) ===== -->
    <view class="quick-section">
      <view class="quick-grid">
        <view class="quick-item" @tap="goSupplyHall">
          <view class="stitch-icon-box stitch-icon-box--md stitch-icon-box--brand">
            <WgIcon name="store" :size="26" :color="BRAND_600" />
          </view>
          <text class="quick-item__label">供应大厅</text>
        </view>
        <view class="quick-item" @tap="goRequirementHall">
          <view class="stitch-icon-box stitch-icon-box--md stitch-icon-box--autumn">
            <WgIcon name="shopping-bag" :size="26" :color="AUTUMN_500" />
          </view>
          <text class="quick-item__label">采购大厅</text>
        </view>
        <view class="quick-item" @tap="goContracts">
          <view class="stitch-icon-box stitch-icon-box--md stitch-icon-box--action">
            <WgIcon name="file-text" :size="26" :color="ACTION_600" />
          </view>
          <text class="quick-item__label">合同管理</text>
        </view>
        <view class="quick-item" @tap="goDirectory">
          <view class="stitch-icon-box stitch-icon-box--md stitch-icon-box--accent">
            <WgIcon name="building2" :size="26" :color="ACCENT_400" />
          </view>
          <text class="quick-item__label">企业名录</text>
        </view>
        <view class="quick-item" @tap="goMap">
          <view class="stitch-icon-box stitch-icon-box--md stitch-icon-box--warm">
            <WgIcon name="map-pin" :size="26" :color="WARM_500" />
          </view>
          <text class="quick-item__label">地图找商</text>
        </view>
        <view class="quick-item" @tap="goCategoryDirectory">
          <view class="stitch-icon-box stitch-icon-box--md stitch-icon-box--brand-bold">
            <WgIcon name="layout-grid" :size="26" :color="BRAND_600" />
          </view>
          <text class="quick-item__label">品类目录</text>
        </view>
      </view>
    </view>

    <!-- ===== 话题区域 ===== -->
    <view class="topic-section">
      <!-- 排序标签 (Stitch Pill 风格) -->
      <view class="sort-bar">
        <text class="stitch-section-title">话题动态</text>
        <view class="sort-bar__tabs">
          <text
            class="sort-tab"
            :class="{ 'sort-tab--active': sortMode === 'latest' }"
            @tap="sortMode = 'latest'"
          >最新</text>
          <text
            class="sort-tab"
            :class="{ 'sort-tab--active': sortMode === 'hottest' }"
            @tap="sortMode = 'hottest'"
          >最热</text>
          <text
            class="sort-tab"
            :class="{ 'sort-tab--active': sortMode === 'following' }"
            @tap="sortMode = 'following'"
          >关注</text>
        </view>
      </view>

      <!-- 关注 tab 未登录提示 -->
      <view v-if="sortMode === 'following' && !authStore.isLoggedIn" class="login-prompt">
        <WgEmpty
          text="登录后查看关注动态"
          description="关注感兴趣的用户，获取他们的最新话题"
          actionText="去登录"
          icon="auth"
          @action="goLogin"
        />
      </view>

      <!-- 帖子列表 -->
      <view v-if="displayList.length > 0" class="post-list">
        <view
          v-for="item in displayList"
          :key="item.id"
          class="post-card stitch-card"
          @tap="goDetail(item.id)"
        >
          <text class="post-card__title">{{ item.title }}</text>

          <view class="post-card__author">
            <view class="post-card__avatar">
              <image
                v-if="item.avatar"
                class="post-card__avatar-img"
                :src="item.avatar"
                mode="aspectFill"
              />
              <text v-else class="post-card__avatar-text">{{ getInitial(item) }}</text>
            </view>
            <text class="post-card__author-line">{{ getAuthorLine(item) }}</text>
            <text class="post-card__time">{{ formatRelativeTime(item.createTime) }}</text>
          </view>

          <text v-if="item.content" class="post-card__content">{{ getPreview(item.content) }}</text>

          <view v-if="getImages(item).length > 0" class="post-card__images">
            <view
              v-for="(img, idx) in getImages(item)"
              :key="idx"
              class="post-card__image"
            >
              <image :src="img" mode="aspectFill" style="width: 100%; height: 100%" />
            </view>
          </view>

          <view class="post-card__footer">
            <view class="post-card__stat">
              <WgIcon name="heart" :size="14" :color="WARM_400" />
              <text class="post-card__stat-num">{{ item.likeCount || 0 }}</text>
            </view>
            <view class="post-card__stat">
              <WgIcon name="message-circle" :size="14" :color="WARM_400" />
              <text class="post-card__stat-num">{{ item.commentCount || 0 }}</text>
            </view>
            <view v-if="item.domain" class="post-card__domain">
              <text class="stitch-tag stitch-tag--brand">{{ item.domain }}</text>
            </view>
          </view>
        </view>

        <WgLoadMore :status="loadStatus" @loadMore="loadMore" />
      </view>

      <!-- 空状态 -->
      <WgEmpty
        v-else-if="!loading"
        text="暂无话题"
        description="快来发布第一个话题吧"
        actionText="发布话题"
        @action="goPublish"
      />

      <!-- 加载态 -->
      <WgSkeleton
        v-if="followingLoading && sortMode === 'following'"
        type="card"
        :rows="3"
      />
      <WgSkeleton
        v-if="loading && allPosts.length === 0 && sortMode !== 'following'"
        type="card"
        :rows="3"
      />
    </view>

    <view style="height: 160rpx;" />
    <WgTabBar :current="0" />
  </view>
</template>

<style lang="scss" scoped>
.home {
  min-height: 100vh;
  background: $bg-page;
}

/* ===== Hero Header ===== */
.hero {
  padding-bottom: $spacing-xl;

  &__safe {
    height: 0;
  }

  &__content {
    padding: $spacing-xl $spacing-xl 0;
  }

  &__greeting {
    margin-bottom: $spacing-xl;
  }

  &__greeting-text {
    display: block;
    color: $text-inverse;
  }

  &__greeting-name {
    display: block;
    color: rgba(255, 255, 255, 0.75);
  }

  &__search {
    margin-bottom: $spacing-sm;
  }
}

/* ===== Quick Section ===== */
.quick-section {
  margin: -#{$spacing-lg} $spacing-lg 0;
  position: relative;
  z-index: 2;
}

.quick-grid {
  display: flex;
  flex-wrap: wrap;
  background: $bg-card;
  border-radius: $radius-2xl;
  padding: $spacing-lg $spacing-sm;
  box-shadow: 0 4rpx 20rpx rgba(120, 90, 50, 0.06), 0 0 1rpx rgba(0, 0, 0, 0.04);
}

.quick-item {
  width: 33.333%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-md 0;
  transition: transform $transition-fast ease;

  &:active {
    transform: scale(0.92);
  }

  &__label {
    font-size: $font-sm;
    color: $text-primary;
    font-weight: 600;
    margin-top: $spacing-sm;
  }
}

/* ===== Topic Section ===== */
.topic-section {
  padding: $spacing-xl $spacing-lg 0;
}

/* ===== Sort Bar ===== */
.sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;

  &__tabs {
    display: flex;
    gap: $spacing-xs;
    background: $warm-100;
    border-radius: $radius-full;
    padding: 4rpx;
  }
}

.sort-tab {
  font-size: $font-sm;
  color: $text-secondary;
  padding: $spacing-xs $spacing-lg;
  border-radius: $radius-full;
  font-weight: 500;
  transition: all $transition-fast ease;

  &--active {
    color: $brand-600;
    background: $bg-card;
    font-weight: 700;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  }
}

/* ===== Post Card (inherits stitch-card) ===== */
.post-card {
  margin-bottom: $spacing-md;

  &__title {
    font-size: $font-lg;
    font-weight: 800;
    color: $text-primary;
    display: -webkit-box;
    overflow: hidden;
    text-overflow: ellipsis;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.4;
    margin-bottom: $spacing-sm;
  }

  &__author {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-md;
  }

  &__avatar {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    background: $brand-50;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    overflow: hidden;
    margin-right: $spacing-sm;
  }

  &__avatar-img {
    width: 48rpx;
    height: 48rpx;
  }

  &__avatar-text {
    font-size: 22rpx;
    font-weight: bold;
    color: $brand-600;
  }

  &__author-line {
    flex: 1;
    font-size: $font-xs;
    color: $text-secondary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__time {
    font-size: $font-xs;
    color: $text-placeholder;
    flex-shrink: 0;
    margin-left: $spacing-sm;
  }

  &__content {
    font-size: $font-md;
    color: $text-secondary;
    line-height: 1.7;
    display: -webkit-box;
    overflow: hidden;
    text-overflow: ellipsis;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    margin-bottom: $spacing-md;
  }

  &__images {
    display: flex;
    gap: $spacing-sm;
    margin-bottom: $spacing-md;
  }

  &__image {
    flex: 1;
    height: 220rpx;
    border-radius: $radius-lg;
    overflow: hidden;
  }

  &__footer {
    display: flex;
    align-items: center;
    gap: $spacing-xl;
    padding-top: $spacing-md;
    border-top: 1rpx solid $warm-100;
  }

  &__stat {
    display: flex;
    align-items: center;
    gap: 8rpx;
  }

  &__stat-num {
    font-size: $font-xs;
    color: $text-placeholder;
  }

  &__domain {
    margin-left: auto;
  }
}
</style>
