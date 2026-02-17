<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { WARM_400, WHITE } from '../../constants/colors'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { listPosts, type PostResponse } from '../../api/post'
import { getFollowedPosts } from '../../api/follow'
import { useAuthStore } from '../../store/auth'
import { formatRelativeTime } from '../../utils/format'

const authStore = useAuthStore()
const allPosts = ref<PostResponse[]>([])
const followingPosts = ref<PostResponse[]>([])
const followingLoading = ref(false)
const loading = ref(false)
const sortMode = ref<'latest' | 'hottest' | 'following'>('latest')
const displayCount = ref(15)
const PAGE_SIZE = 15

/** Phase C: 话题搜索 */
const searchKeyword = ref('')
const showSearch = ref(false)

const searchedList = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()
  if (!kw) return []
  return allPosts.value.filter(p =>
    (p.title || '').toLowerCase().includes(kw) ||
    stripHtml(p.content || '').toLowerCase().includes(kw) ||
    (p.nickName || '').toLowerCase().includes(kw) ||
    (p.companyName || '').toLowerCase().includes(kw) ||
    (p.domain || '').toLowerCase().includes(kw)
  )
})

const filteredList = computed(() => {
  // 如果搜索模式激活且有关键词，显示搜索结果
  if (showSearch.value && searchKeyword.value.trim()) return searchedList.value
  if (sortMode.value === 'following') return followingPosts.value
  const list = [...allPosts.value]
  if (sortMode.value === 'hottest') {
    list.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
  }
  return list
})

function toggleSearch() {
  showSearch.value = !showSearch.value
  if (!showSearch.value) {
    searchKeyword.value = ''
  }
}

function clearSearch() {
  searchKeyword.value = ''
}

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

onMounted(() => {
  loadData()
})

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
  } catch {
    // handled by request.ts
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

/** 获取头像首字 */
function getInitial(item: PostResponse): string {
  const name = item.nickName || item.userName || item.companyName || '?'
  return name.charAt(0)
}

/** 获取显示名称 */
function getDisplayName(item: PostResponse): string {
  return item.nickName || item.userName || '匿名用户'
}

/** 获取副标题信息 */
function getSubInfo(item: PostResponse): string {
  const parts: string[] = []
  if (item.companyName) parts.push(item.companyName)
  if (item.position) parts.push(item.position)
  return parts.join(' · ') || ''
}

function stripHtml(html: string): string {
  return html.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').replace(/&amp;/g, '&').replace(/&lt;/g, '<').replace(/&gt;/g, '>').trim()
}

/** 截取预览文本 */
function getPreview(content?: string): string {
  if (!content) return ''
  const text = stripHtml(content)
  return text.length > 80 ? text.slice(0, 80) + '...' : text
}

/** 解析图片列表 */
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
  <view class="square-page">
    <!-- 搜索栏 -->
    <view v-if="showSearch" class="search-bar">
      <view class="search-bar__inner">
        <WgIcon name="search" :size="16" :color="WARM_400" />
        <input
          class="search-bar__input"
          v-model="searchKeyword"
          placeholder="搜索话题、作者、领域..."
          :focus="showSearch"
          :maxlength="50"
        />
        <view v-if="searchKeyword" class="search-bar__clear" @tap="clearSearch">
          <WgIcon name="clear" :size="16" :color="WARM_400" />
        </view>
      </view>
      <text class="search-bar__cancel" @tap="toggleSearch">取消</text>
    </view>

    <!-- 排序标签 -->
    <view class="sort-bar">
      <view class="sort-bar__tabs">
        <view
          class="sort-bar__tab"
          :class="{ 'sort-bar__tab--active': sortMode === 'latest' && !showSearch }"
          @tap="sortMode = 'latest'; showSearch = false; searchKeyword = ''"
        >
          <text>最新</text>
        </view>
        <view
          class="sort-bar__tab"
          :class="{ 'sort-bar__tab--active': sortMode === 'hottest' && !showSearch }"
          @tap="sortMode = 'hottest'; showSearch = false; searchKeyword = ''"
        >
          <text>最热</text>
        </view>
        <view
          class="sort-bar__tab"
          :class="{ 'sort-bar__tab--active': sortMode === 'following' && !showSearch }"
          @tap="sortMode = 'following'; showSearch = false; searchKeyword = ''"
        >
          <text>关注</text>
        </view>
      </view>
      <view class="sort-bar__search-icon" @tap="toggleSearch">
        <WgIcon name="search" :size="20" :color="showSearch ? '#2D6A4F' : '#999'" />
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
        <!-- 作者信息 -->
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
          <view class="post-card__author-info">
            <text class="post-card__name">{{ getDisplayName(item) }}</text>
            <text v-if="getSubInfo(item)" class="post-card__sub">{{ getSubInfo(item) }}</text>
          </view>
          <text class="post-card__time">{{ formatRelativeTime(item.createTime) }}</text>
        </view>

        <!-- 帖子内容 -->
        <view class="post-card__body">
          <text class="post-card__title">{{ item.title }}</text>
          <text v-if="item.content" class="post-card__content">{{ getPreview(item.content) }}</text>
        </view>

        <!-- 图片网格 -->
        <view v-if="getImages(item).length > 0" class="post-card__images">
          <view
            v-for="(img, idx) in getImages(item)"
            :key="idx"
            class="post-card__image"
          >
            <image
              :src="img"
              mode="aspectFill"
              style="width: 100%; height: 100%"
            />
          </view>
        </view>

        <!-- 领域标签 -->
        <view v-if="item.domain" class="post-card__tags">
          <text class="post-card__tag">{{ item.domain }}</text>
        </view>

        <!-- 互动数据 -->
        <view class="post-card__footer">
          <view class="post-card__stat">
            <WgIcon name="heart" :size="14" :color="WARM_400" />
            <text class="post-card__stat-num">{{ item.likeCount || 0 }}</text>
          </view>
          <view class="post-card__stat">
            <WgIcon name="message-circle" :size="14" :color="WARM_400" />
            <text class="post-card__stat-num">{{ item.commentCount || 0 }}</text>
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
      :avatar="true"
      :title="true"
    />

    <!-- 加载中骨架 -->
    <WgSkeleton
      v-if="loading && allPosts.length === 0 && sortMode !== 'following'"
      type="card"
      :rows="3"
      :avatar="true"
      :title="true"
    />

    <!-- 发布按钮 -->
    <view class="fab anim-fab-enter" @tap="goPublish">
      <WgIcon name="square-pen" :size="24" :color="WHITE" />
    </view>
  </view>
</template>

<style lang="scss" scoped>
.square-page {
  min-height: 100vh;
  background: $bg-page;
}

/* ===== 搜索栏 ===== */
.search-bar {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  background: $bg-card;
  padding: $spacing-sm $spacing-md;
  border-bottom: 1rpx solid $border-light;

  &__inner {
    flex: 1;
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    background: $bg-page;
    border-radius: 30rpx;
    padding: 0 $spacing-md;
    height: 64rpx;
  }

  &__input {
    flex: 1;
    font-size: $font-sm;
    height: 64rpx;
  }

  &__clear {
    padding: $spacing-xs;
  }

  &__cancel {
    font-size: $font-sm;
    color: $brand-600;
    font-weight: 600;
    flex-shrink: 0;
  }
}

.sort-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: $bg-card;
  padding: $spacing-sm $spacing-md;
  border-bottom: 1rpx solid $border-light;

  &__tabs {
    display: flex;
    gap: $spacing-lg;
  }

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

  &__search-icon {
    padding: $spacing-xs;
  }
}

.post-list {
  padding: $spacing-sm;
}

.post-card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;

  &__author {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-sm;
  }

  &__avatar {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    background: $brand-100;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    overflow: hidden;
  }

  &__avatar-img {
    width: 72rpx;
    height: 72rpx;
  }

  &__avatar-text {
    font-size: $font-md;
    font-weight: bold;
    color: $brand-600;
  }

  &__author-info {
    flex: 1;
    margin-left: $spacing-sm;
    overflow: hidden;
  }

  &__name {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__sub {
    font-size: $font-xs;
    color: $text-secondary;
    display: block;
    margin-top: 4rpx;
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

  &__body {
    margin-bottom: $spacing-sm;
  }

  &__title {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;
    display: -webkit-box;
    margin-bottom: $spacing-xs;
    overflow: hidden;
    text-overflow: ellipsis;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  &__content {
    font-size: $font-md;
    color: $text-secondary;
    line-height: 1.6;
    display: block;
  }

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

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
    margin-bottom: $spacing-sm;
  }

  &__tag {
    font-size: $font-xs;
    color: $brand-600;
    background: $brand-50;
    padding: 4rpx 16rpx;
    border-radius: $radius-sm;
  }

  &__footer {
    display: flex;
    align-items: center;
    gap: $spacing-lg;
    padding-top: $spacing-sm;
    border-top: 1rpx solid $border-light;
  }

  &__stat {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
  }

  &__stat-num {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

.fab {
  position: fixed;
  right: 32rpx;
  bottom: 180rpx;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: $brand-600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(45, 106, 79, 0.3);
}
</style>
