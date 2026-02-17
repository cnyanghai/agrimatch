<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
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

/** 作者信息合并成一段文字：名字 · 公司 · 职位 */
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
    <!-- 头部：全宽搜索栏 -->
    <view class="header safe-area-top">
      <view class="search-bar anim-header-in" @tap="goSearch">
        <WgIcon name="search" :size="16" color="#A8A29E" />
        <text class="search-bar__text">搜索话题、用户、企业...</text>
      </view>
    </view>

    <!-- 业务快捷入口 -->
    <view class="quick-entry anim-slide-up">
      <view class="quick-entry__item" @tap="goSupplyHall">
        <view class="quick-entry__icon quick-entry__icon--brand">
          <WgIcon name="store" :size="22" color="#2D6A4F" />
        </view>
        <text class="quick-entry__label">供应大厅</text>
      </view>
      <view class="quick-entry__item" @tap="goRequirementHall">
        <view class="quick-entry__icon quick-entry__icon--autumn">
          <WgIcon name="shopping-bag" :size="22" color="#c28a55" />
        </view>
        <text class="quick-entry__label">采购大厅</text>
      </view>
      <view class="quick-entry__item" @tap="goContracts">
        <view class="quick-entry__icon quick-entry__icon--action">
          <WgIcon name="file-text" :size="22" color="#2563eb" />
        </view>
        <text class="quick-entry__label">合同管理</text>
      </view>
      <view class="quick-entry__item" @tap="goDirectory">
        <view class="quick-entry__icon quick-entry__icon--accent">
          <WgIcon name="building2" :size="22" color="#E76F51" />
        </view>
        <text class="quick-entry__label">企业名录</text>
      </view>
      <view class="quick-entry__item" @tap="goMap">
        <view class="quick-entry__icon quick-entry__icon--warm">
          <WgIcon name="map-pin" :size="22" color="#78716C" />
        </view>
        <text class="quick-entry__label">地图找商</text>
      </view>
      <view class="quick-entry__item" @tap="goCategoryDirectory">
        <view class="quick-entry__icon quick-entry__icon--brand-light">
          <WgIcon name="layout-grid" :size="22" color="#389867" />
        </view>
        <text class="quick-entry__label">品类目录</text>
      </view>
    </view>

    <!-- 排序标签 -->
    <view class="sort-bar">
      <view
        class="sort-bar__tab"
        :class="{ 'sort-bar__tab--active': sortMode === 'latest' }"
        @tap="sortMode = 'latest'"
      >
        <text>最新</text>
      </view>
      <view
        class="sort-bar__tab"
        :class="{ 'sort-bar__tab--active': sortMode === 'hottest' }"
        @tap="sortMode = 'hottest'"
      >
        <text>最热</text>
      </view>
      <view
        class="sort-bar__tab"
        :class="{ 'sort-bar__tab--active': sortMode === 'following' }"
        @tap="sortMode = 'following'"
      >
        <text>关注</text>
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
        class="post-card tap-feedback"
        @tap="goDetail(item.id)"
      >
        <!-- 第1层：标题 -->
        <text class="post-card__title">{{ item.title }}</text>

        <!-- 第2层：作者信息（紧凑一行） -->
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

        <!-- 第3层：内容摘要 -->
        <text v-if="item.content" class="post-card__content">{{ getPreview(item.content) }}</text>

        <!-- 第4层：图片网格 -->
        <view v-if="getImages(item).length > 0" class="post-card__images">
          <view
            v-for="(img, idx) in getImages(item)"
            :key="idx"
            class="post-card__image"
          >
            <image :src="img" mode="aspectFill" style="width: 100%; height: 100%" />
          </view>
        </view>

        <!-- 第5层：互动数据 -->
        <view class="post-card__footer">
          <view class="post-card__stat">
            <WgIcon name="heart" :size="14" color="#A8A29E" />
            <text class="post-card__stat-num">{{ item.likeCount || 0 }}</text>
          </view>
          <view class="post-card__stat">
            <WgIcon name="message-circle" :size="14" color="#A8A29E" />
            <text class="post-card__stat-num">{{ item.commentCount || 0 }}</text>
          </view>
          <view v-if="item.domain" class="post-card__domain">
            <text class="post-card__domain-text">{{ item.domain }}</text>
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

    <!-- 关注 tab 加载中 -->
    <WgSkeleton
      v-if="followingLoading && sortMode === 'following'"
      type="card"
      :rows="3"
    />

    <!-- 加载中骨架 -->
    <WgSkeleton
      v-if="loading && allPosts.length === 0 && sortMode !== 'following'"
      type="card"
      :rows="3"
    />

    <!-- 底部间距 -->
    <view style="height: 130rpx;" />

    <WgTabBar :current="0" />
  </view>
</template>

<style lang="scss" scoped>
.home {
  min-height: 100vh;
  background: $bg-page;
}

/* ===== Header ===== */
.header {
  background: #ffffff;
  padding: $spacing-sm $spacing-md;
}

/* ===== 全宽搜索栏 ===== */
.search-bar {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  height: 72rpx;
  padding: 0 $spacing-lg;
  background: $warm-100;
  border-radius: $radius-pill;

  &__text {
    color: $text-placeholder;
    font-size: $font-md;
  }
}

/* ===== Quick Entry ===== */
.quick-entry {
  display: flex;
  flex-wrap: wrap;
  background: #ffffff;
  padding: $spacing-md $spacing-sm $spacing-xs;

  &__item {
    width: 33.333%;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: $spacing-sm 0;

    &:active {
      opacity: 0.7;
      transform: scale(0.95);
    }
  }

  &__icon {
    width: 88rpx;
    height: 88rpx;
    border-radius: $radius-xl;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: $spacing-xs;

    &--brand { background: $brand-50; }
    &--brand-light { background: rgba(56, 152, 103, 0.08); }
    &--autumn { background: $autumn-50; }
    &--action { background: rgba(37, 99, 235, 0.08); }
    &--accent { background: $accent-50; }
    &--warm { background: $warm-100; }
  }

  &__label {
    font-size: $font-sm;
    color: $text-primary;
    font-weight: 500;
  }
}

/* ===== Sort Bar ===== */
.sort-bar {
  display: flex;
  background: #ffffff;
  padding: $spacing-sm $spacing-md;
  gap: $spacing-lg;
  border-bottom: 1rpx solid $border-light;
  position: sticky;
  top: 0;
  z-index: 10;

  &__tab {
    font-size: $font-md;
    color: $text-secondary;
    padding-bottom: $spacing-xs;
    position: relative;

    &--active {
      color: $brand-600;
      font-weight: bold;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 40rpx;
        height: 4rpx;
        background: $brand-600;
        border-radius: 2rpx;
      }
    }
  }
}

/* ===== Post List ===== */
.post-list {
  padding: $spacing-xs $spacing-md;
}

.post-card {
  background: #ffffff;
  padding: $spacing-md $spacing-md;
  border-radius: $radius-lg;
  margin-bottom: $spacing-sm;
  box-shadow: $shadow-warm-card;

  /* 第1层：标题 */
  &__title {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;
    display: -webkit-box;
    overflow: hidden;
    text-overflow: ellipsis;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-height: 1.4;
    margin-bottom: $spacing-xs;
  }

  /* 第2层：作者信息（紧凑一行） */
  &__author {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-sm;
  }

  &__avatar {
    width: 40rpx;
    height: 40rpx;
    border-radius: 50%;
    background: $brand-100;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    overflow: hidden;
    margin-right: $spacing-xs;
  }

  &__avatar-img {
    width: 40rpx;
    height: 40rpx;
  }

  &__avatar-text {
    font-size: 20rpx;
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

  /* 第3层：内容摘要 */
  &__content {
    font-size: $font-md;
    color: $text-secondary;
    line-height: 1.6;
    display: -webkit-box;
    overflow: hidden;
    text-overflow: ellipsis;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    margin-bottom: $spacing-sm;
  }

  /* 第4层：图片 */
  &__images {
    display: flex;
    gap: $spacing-xs;
    margin-bottom: $spacing-sm;
  }

  &__image {
    flex: 1;
    height: 200rpx;
    border-radius: $radius-sm;
    overflow: hidden;
  }

  /* 第5层：互动 */
  &__footer {
    display: flex;
    align-items: center;
    gap: $spacing-lg;
  }

  &__stat {
    display: flex;
    align-items: center;
    gap: 6rpx;
  }

  &__stat-num {
    font-size: $font-xs;
    color: $text-placeholder;
  }

  &__domain {
    margin-left: auto;
  }

  &__domain-text {
    font-size: $font-xs;
    color: $brand-600;
    background: $brand-50;
    padding: 2rpx 14rpx;
    border-radius: $radius-sm;
  }
}

/* FAB removed per user request */
</style>
