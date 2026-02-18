<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { WARM_400 } from '../../constants/colors'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { getFollowedUsers, getFollowedPosts, unfollowUser, type FollowedUser } from '../../api/follow'
import type { PostResponse } from '../../api/post'
import { formatRelativeTime } from '../../utils/format'

const activeTab = ref<'users' | 'posts'>('users')
const users = ref<FollowedUser[]>([])
const posts = ref<PostResponse[]>([])
const loading = ref(false)
const postsLoading = ref(false)
const unfollowingId = ref<number | null>(null)

onShow(() => {
  loadData()
})

onPullDownRefresh(() => {
  loadData().finally(() => uni.stopPullDownRefresh())
})

watch(activeTab, (tab) => {
  if (tab === 'posts' && posts.value.length === 0) {
    loadPosts()
  }
})

async function loadData() {
  loading.value = true
  try {
    users.value = await getFollowedUsers() || []
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

async function loadPosts() {
  postsLoading.value = true
  try {
    posts.value = await getFollowedPosts() || []
  } catch {
    // handled
  } finally {
    postsLoading.value = false
  }
}

async function handleUnfollow(user: FollowedUser) {
  if (unfollowingId.value) return
  uni.showModal({
    title: '取消关注',
    content: `确定取消关注 ${user.nickName || user.userName || '该用户'} 吗？`,
    success: async (res) => {
      if (!res.confirm) return
      unfollowingId.value = user.userId
      try {
        await unfollowUser(user.userId)
        users.value = users.value.filter(u => u.userId !== user.userId)
        uni.showToast({ title: '已取消关注', icon: 'none' })
      } catch {
        // handled
      } finally {
        unfollowingId.value = null
      }
    },
  })
}

function goUserProfile(userId: number) {
  uni.navigateTo({ url: `/pages/user/profile?id=${userId}` })
}

function goTopicSquare() {
  uni.navigateTo({ url: '/pages/topic/square' })
}

function goPostDetail(id: number) {
  uni.navigateTo({ url: `/pages/topic/detail?id=${id}` })
}

function getInitial(u: FollowedUser): string {
  const name = u.nickName || u.userName || '?'
  return name.charAt(0)
}

function getSubInfo(u: FollowedUser): string {
  const parts: string[] = []
  if (u.position) parts.push(u.position)
  if (u.companyName) parts.push(u.companyName)
  return parts.join(' · ') || ''
}

function formatFollowTime(time?: string): string {
  if (!time) return ''
  const d = new Date(time)
  return `关注于 ${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}

function getPostDisplayName(item: PostResponse): string {
  return item.nickName || item.userName || '匿名用户'
}

function stripHtml(html: string): string {
  return html.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').replace(/&amp;/g, '&').trim()
}

function getPostPreview(content?: string): string {
  if (!content) return ''
  const text = stripHtml(content)
  return text.length > 80 ? text.slice(0, 80) + '...' : text
}

function getPostInitial(item: PostResponse): string {
  const name = item.nickName || item.userName || '?'
  return name.charAt(0)
}
</script>

<template>
  <view class="follow-page">
    <WgNavBar title="关注列表" />

    <!-- Tab 栏 -->
    <view class="tab-bar">
      <view
        class="tab-bar__item"
        :class="{ 'tab-bar__item--active': activeTab === 'users' }"
        @tap="activeTab = 'users'"
      >
        <text>关注的人</text>
      </view>
      <view
        class="tab-bar__item"
        :class="{ 'tab-bar__item--active': activeTab === 'posts' }"
        @tap="activeTab = 'posts'"
      >
        <text>关注动态</text>
      </view>
    </view>

    <!-- 用户列表 tab -->
    <template v-if="activeTab === 'users'">
      <view v-if="users.length > 0" class="follow-list">
        <view
          v-for="user in users"
          :key="user.userId"
          class="follow-item"
        >
          <view class="follow-item__left" @tap="goUserProfile(user.userId)">
            <view class="follow-item__avatar">
              <image
                v-if="user.avatar"
                class="follow-item__avatar-img"
                :src="user.avatar"
                mode="aspectFill"
              />
              <text v-else class="follow-item__avatar-text">{{ getInitial(user) }}</text>
            </view>
            <view class="follow-item__info">
              <text class="follow-item__name">{{ user.nickName || user.userName || '用户' }}</text>
              <text v-if="getSubInfo(user)" class="follow-item__sub">{{ getSubInfo(user) }}</text>
              <text v-if="user.followTime" class="follow-item__time">{{ formatFollowTime(user.followTime) }}</text>
            </view>
          </view>
          <view
            class="follow-item__btn"
            :class="{ 'follow-item__btn--loading': unfollowingId === user.userId }"
            @tap="handleUnfollow(user)"
          >
            <text class="follow-item__btn-text">
              {{ unfollowingId === user.userId ? '...' : '取消关注' }}
            </text>
          </view>
        </view>
      </view>

      <WgEmpty
        v-else-if="!loading"
        text="暂无关注"
        description="去发现感兴趣的用户吧"
        actionText="话题广场"
        @action="goTopicSquare"
      />

      <WgSkeleton v-if="loading && users.length === 0" type="list" :rows="5" />
    </template>

    <!-- 关注动态 tab -->
    <template v-if="activeTab === 'posts'">
      <view v-if="posts.length > 0" class="post-list">
        <view
          v-for="item in posts"
          :key="item.id"
          class="post-item"
          @tap="goPostDetail(item.id)"
        >
          <view class="post-item__header">
            <view class="post-item__avatar">
              <image
                v-if="item.avatar"
                class="post-item__avatar-img"
                :src="item.avatar"
                mode="aspectFill"
              />
              <text v-else class="post-item__avatar-text">{{ getPostInitial(item) }}</text>
            </view>
            <view class="post-item__meta">
              <text class="post-item__author">{{ getPostDisplayName(item) }}</text>
              <text class="post-item__time">{{ formatRelativeTime(item.createTime) }}</text>
            </view>
          </view>
          <text class="post-item__title">{{ item.title }}</text>
          <text v-if="item.content" class="post-item__preview">{{ getPostPreview(item.content) }}</text>
          <view class="post-item__footer">
            <view class="post-item__stat">
              <WgIcon name="heart" :size="14" :color="WARM_400" />
              <text class="post-item__stat-num">{{ item.likeCount || 0 }}</text>
            </view>
            <view class="post-item__stat">
              <WgIcon name="message-circle" :size="14" :color="WARM_400" />
              <text class="post-item__stat-num">{{ item.commentCount || 0 }}</text>
            </view>
          </view>
        </view>
      </view>

      <WgEmpty
        v-else-if="!postsLoading"
        text="暂无动态"
        description="关注更多用户获取最新动态"
        actionText="话题广场"
        @action="goTopicSquare"
      />

      <WgSkeleton v-if="postsLoading && posts.length === 0" type="card" :rows="3" />
    </template>
  </view>
</template>

<style lang="scss" scoped>
.follow-page {
  min-height: 100vh;
  background: $bg-page;
}

/* ===== Tab bar ===== */
.tab-bar {
  display: flex;
  background: $bg-card;
  padding: 0 $spacing-md;
  border-bottom: 1rpx solid $border-light;

  &__item {
    padding: $spacing-md $spacing-lg;
    font-size: $font-md;
    color: $text-secondary;
    position: relative;
    flex-shrink: 0;

    &--active {
      color: $brand-600;
      font-weight: bold;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 48rpx;
        height: 4rpx;
        background: $brand-600;
        border-radius: 2rpx;
      }
    }
  }
}

/* ===== Follow list ===== */
.follow-list {
  padding: $spacing-sm;
}

.follow-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;

  &__left {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    flex: 1;
    min-width: 0;
  }

  &__avatar {
    width: 96rpx;
    height: 96rpx;
    border-radius: 50%;
    background: $brand-100;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    overflow: hidden;
  }

  &__avatar-img {
    width: 96rpx;
    height: 96rpx;
  }

  &__avatar-text {
    font-size: $font-xl;
    font-weight: bold;
    color: $brand-600;
  }

  &__info {
    flex: 1;
    min-width: 0;
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
    display: block;
    margin-top: 4rpx;
  }

  &__btn {
    padding: 12rpx 24rpx;
    background: $bg-page;
    border: 1rpx solid $border-color;
    border-radius: $radius-lg;
    flex-shrink: 0;

    &:active {
      border-color: $color-error;
    }

    &--loading {
      opacity: 0.6;
    }
  }

  &__btn-text {
    font-size: $font-xs;
    color: $text-secondary;
    white-space: nowrap;
  }
}

/* ===== Post list (following posts tab) ===== */
.post-list {
  padding: $spacing-sm;
}

.post-item {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;

  &:active {
    opacity: 0.85;
  }

  &__header {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    margin-bottom: $spacing-sm;
  }

  &__avatar {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    background: $brand-100;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    overflow: hidden;
  }

  &__avatar-img {
    width: 64rpx;
    height: 64rpx;
  }

  &__avatar-text {
    font-size: $font-sm;
    font-weight: bold;
    color: $brand-600;
  }

  &__meta {
    flex: 1;
    min-width: 0;
  }

  &__author {
    font-size: $font-md;
    font-weight: 600;
    color: $text-primary;
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__time {
    font-size: $font-xs;
    color: $text-placeholder;
  }

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

  &__preview {
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

  &__footer {
    display: flex;
    align-items: center;
    gap: $spacing-lg;
    padding-top: $spacing-xs;
    border-top: 1rpx solid $border-light;
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
}
</style>
