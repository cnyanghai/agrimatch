<script setup lang="ts">
import { ref, computed } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { WARM_400 } from '../../constants/colors'
import { onShow } from '@dcloudio/uni-app'
import { listPosts, type PostResponse } from '../../api/post'
import { formatRelativeTime } from '../../utils/format'

const allPosts = ref<PostResponse[]>([])
const loading = ref(false)
const displayCount = ref(15)
const PAGE_SIZE = 15

const displayList = computed(() => allPosts.value.slice(0, displayCount.value))

const loadStatus = computed<'loading' | 'more' | 'noMore'>(() => {
  if (loading.value) return 'loading'
  if (displayCount.value >= allPosts.value.length) return 'noMore'
  return 'more'
})

onShow(() => {
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
    const res = await listPosts({ onlyCollected: true })
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

function goSquare() {
  uni.navigateTo({ url: '/pages/topic/square' })
}

function getInitial(item: PostResponse): string {
  const name = item.nickName || item.userName || item.companyName || '?'
  return name.charAt(0)
}

function getDisplayName(item: PostResponse): string {
  return item.nickName || item.userName || '匿名用户'
}

function getSubInfo(item: PostResponse): string {
  const parts: string[] = []
  if (item.companyName) parts.push(item.companyName)
  if (item.position) parts.push(item.position)
  return parts.join(' · ') || ''
}

function stripHtml(html: string): string {
  return html.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').replace(/&amp;/g, '&').replace(/&lt;/g, '<').replace(/&gt;/g, '>').trim()
}

function getPreview(content?: string): string {
  if (!content) return ''
  const text = stripHtml(content)
  return text.length > 80 ? text.slice(0, 80) + '...' : text
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
  <view class="collections-page">
    <!-- Post list -->
    <view v-if="displayList.length > 0" class="post-list">
      <view
        v-for="item in displayList"
        :key="item.id"
        class="post-card tap-feedback"
        @tap="goDetail(item.id)"
      >
        <!-- Author -->
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

        <!-- Content -->
        <view class="post-card__body">
          <text class="post-card__title">{{ item.title }}</text>
          <text v-if="item.content" class="post-card__content">{{ getPreview(item.content) }}</text>
        </view>

        <!-- Images -->
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

        <!-- Domain tag -->
        <view v-if="item.domain" class="post-card__tags">
          <text class="post-card__tag">{{ item.domain }}</text>
        </view>

        <!-- Stats -->
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

    <!-- Empty state -->
    <WgEmpty
      v-else-if="!loading"
      text="暂无收藏"
      description="去话题广场看看吧"
      actionText="话题广场"
      @action="goSquare"
    />

    <!-- Loading skeleton -->
    <WgSkeleton
      v-if="loading && allPosts.length === 0"
      type="card"
      :rows="3"
      :avatar="true"
      :title="true"
    />
  </view>
</template>

<style lang="scss" scoped>
.collections-page {
  min-height: 100vh;
  background: $bg-page;
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
</style>
