<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getUser, type UserResponse } from '../../api/user'
import { listPosts, type PostResponse } from '../../api/post'
import { formatDate, formatDateTime } from '../../utils/format'

const user = ref<UserResponse | null>(null)
const posts = ref<PostResponse[]>([])
const loading = ref(true)
const postsLoading = ref(false)

onLoad(async (options) => {
  if (options?.id) {
    const id = Number(options.id)
    try {
      user.value = await getUser(id)
      await loadPosts(id)
    } catch {
      // handled by request.ts
    } finally {
      loading.value = false
    }
  }
})

/** Load user posts */
async function loadPosts(userId: number) {
  postsLoading.value = true
  try {
    posts.value = await listPosts({ userId })
  } catch {
    // silent
  } finally {
    postsLoading.value = false
  }
}

/** Get avatar URL or empty for fallback */
function getAvatarUrl(u: UserResponse): string {
  return u.avatar || ''
}

/** Get initial letter for avatar fallback */
function getInitial(u: UserResponse): string {
  const name = u.nickName || u.userName || ''
  return name.charAt(0) || '?'
}

/** Format gender */
function formatGender(gender?: number): string {
  if (gender === 0) return '男'
  if (gender === 1) return '女'
  return ''
}

/** Navigate to post detail */
function handleViewPost(postId: number) {
  uni.navigateTo({ url: `/pages/topic/detail?id=${postId}` })
}

/** Navigate to company detail */
function handleViewCompany(companyId: number) {
  uni.navigateTo({ url: `/pages/company/detail?id=${companyId}` })
}
</script>

<template>
  <view class="profile-page">
    <!-- Loading -->
    <WgSkeleton v-if="loading" type="detail" />

    <!-- Not found -->
    <WgEmpty v-else-if="!user" text="用户不存在" icon="empty" />

    <template v-else>
      <!-- User header -->
      <view class="user-header">
        <view class="user-header__avatar">
          <image
            v-if="getAvatarUrl(user)"
            class="user-header__avatar-img"
            :src="getAvatarUrl(user)"
            mode="aspectFill"
          />
          <text v-else class="user-header__avatar-text">
            {{ getInitial(user) }}
          </text>
        </view>

        <view class="user-header__info">
          <text class="user-header__name">{{ user.nickName || user.userName }}</text>

          <view class="user-header__meta">
            <text v-if="user.position" class="user-header__position">
              {{ user.position }}
            </text>
            <text
              v-if="user.position && user.companyName"
              class="user-header__sep"
            >|</text>
            <text
              v-if="user.companyName"
              class="user-header__company"
              @tap="user.companyId && handleViewCompany(user.companyId)"
            >
              {{ user.companyName }}
            </text>
          </view>

          <!-- Role tags -->
          <view class="user-header__tags">
            <text v-if="user.isSeller" class="user-header__tag user-header__tag--seller">
              供应商
            </text>
            <text v-if="user.isBuyer" class="user-header__tag user-header__tag--buyer">
              采购商
            </text>
          </view>
        </view>
      </view>

      <!-- Bio -->
      <view v-if="user.bio" class="bio-card">
        <text class="bio-card__title">个人简介</text>
        <text class="bio-card__content">{{ user.bio }}</text>
      </view>

      <!-- Extra info -->
      <view class="info-card">
        <view v-if="formatGender(user.gender)" class="info-row">
          <text class="info-row__label">性别</text>
          <text class="info-row__value">{{ formatGender(user.gender) }}</text>
        </view>
        <view v-if="user.companyName" class="info-row">
          <text class="info-row__label">所在企业</text>
          <text
            class="info-row__value info-row__value--link"
            @tap="user!.companyId && handleViewCompany(user!.companyId!)"
          >
            {{ user.companyName }}
          </text>
        </view>
        <view v-if="user.createTime" class="info-row">
          <text class="info-row__label">注册时间</text>
          <text class="info-row__value">{{ formatDate(user.createTime) }}</text>
        </view>
      </view>

      <!-- Posts section -->
      <view class="posts-section">
        <text class="posts-section__title">
          TA的动态 ({{ posts.length }})
        </text>

        <WgSkeleton v-if="postsLoading" type="list" :rows="3" />

        <WgEmpty v-else-if="posts.length === 0" text="暂无动态" />

        <view
          v-for="post in posts"
          :key="post.id"
          class="post-item"
          @tap="handleViewPost(post.id)"
        >
          <text class="post-item__title">{{ post.title }}</text>
          <text v-if="post.content" class="post-item__content">
            {{ post.content }}
          </text>
          <view class="post-item__footer">
            <text class="post-item__time">{{ formatDateTime(post.createTime) }}</text>
            <view class="post-item__stats">
              <text v-if="post.likeCount" class="post-item__stat">
                {{ post.likeCount }} 赞
              </text>
              <text v-if="post.commentCount" class="post-item__stat">
                {{ post.commentCount }} 评论
              </text>
            </view>
          </view>
        </view>
      </view>
    </template>
  </view>
</template>

<style lang="scss" scoped>
.profile-page {
  min-height: 100vh;
  background: $bg-page;

}

/* ===== User header ===== */
.user-header {
  display: flex;
  align-items: flex-start;
  gap: $spacing-md;
  background: $bg-card;
  margin: $spacing-sm;
  border-radius: $radius-lg;
  padding: $spacing-lg;

  &__avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background: $brand-100;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    overflow: hidden;
  }

  &__avatar-img {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
  }

  &__avatar-text {
    font-size: $font-2xl;
    font-weight: bold;
    color: $brand-600;
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__name {
    font-size: $font-xl;
    font-weight: bold;
    color: $text-primary;
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    margin-top: $spacing-xs;
    flex-wrap: wrap;
  }

  &__position {
    font-size: $font-sm;
    color: $text-secondary;
  }

  &__sep {
    font-size: $font-sm;
    color: $text-placeholder;
  }

  &__company {
    font-size: $font-sm;
    color: $action-600;
  }

  &__tags {
    display: flex;
    gap: $spacing-xs;
    margin-top: $spacing-sm;
  }

  &__tag {
    font-size: $font-xs;
    padding: 4rpx 16rpx;
    border-radius: $radius-sm;

    &--seller {
      color: $brand-600;
      background: $brand-50;
    }

    &--buyer {
      color: $autumn-500;
      background: rgba($autumn-400, 0.12);
    }
  }
}

/* ===== Bio card ===== */
.bio-card {
  background: $bg-card;
  margin: $spacing-sm;
  border-radius: $radius-lg;
  padding: $spacing-lg;

  &__title {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;
    display: block;
    margin-bottom: $spacing-sm;
  }

  &__content {
    font-size: $font-md;
    color: $text-secondary;
    line-height: 1.8;
  }
}

/* ===== Info card ===== */
.info-card {
  background: $bg-card;
  margin: $spacing-sm;
  border-radius: $radius-lg;
  padding: $spacing-md $spacing-lg;
}

.info-row {
  display: flex;
  padding: $spacing-xs 0;

  &__label {
    width: 160rpx;
    font-size: $font-md;
    color: $text-secondary;
    flex-shrink: 0;
  }

  &__value {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;

    &--link {
      color: $action-600;
    }
  }
}

/* ===== Posts section ===== */
.posts-section {
  margin: $spacing-sm;

  &__title {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;
    display: block;
    margin-bottom: $spacing-sm;
    padding: 0 $spacing-xs;
  }

}

/* ===== Post item ===== */
.post-item {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-sm;

  &:active {
    background: $bg-hover;
  }

  &__title {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__content {
    font-size: $font-sm;
    color: $text-secondary;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    margin-top: $spacing-xs;
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: $spacing-sm;
  }

  &__time {
    font-size: $font-xs;
    color: $text-placeholder;
  }

  &__stats {
    display: flex;
    gap: $spacing-md;
  }

  &__stat {
    font-size: $font-xs;
    color: $text-placeholder;
  }
}
</style>
