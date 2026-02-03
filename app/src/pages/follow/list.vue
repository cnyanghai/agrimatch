<script setup lang="ts">
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { getFollowedUsers, unfollowUser, type FollowedUser } from '../../api/follow'

const users = ref<FollowedUser[]>([])
const loading = ref(false)
const unfollowingId = ref<number | null>(null)

onShow(() => {
  loadData()
})

onPullDownRefresh(() => {
  loadData().finally(() => uni.stopPullDownRefresh())
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

/** 获取头像首字 */
function goTopicSquare() {
  uni.navigateTo({ url: '/pages/topic/square' })
}

/** 获取头像首字 */
function getInitial(u: FollowedUser): string {
  const name = u.nickName || u.userName || '?'
  return name.charAt(0)
}

/** 获取副标题 */
function getSubInfo(u: FollowedUser): string {
  const parts: string[] = []
  if (u.position) parts.push(u.position)
  if (u.companyName) parts.push(u.companyName)
  return parts.join(' · ') || ''
}

/** 格式化关注时间 */
function formatFollowTime(time?: string): string {
  if (!time) return ''
  const d = new Date(time)
  return `关注于 ${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}
</script>

<template>
  <view class="follow-page">
    <!-- 关注列表 -->
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

    <!-- 空状态 -->
    <WgEmpty
      v-else-if="!loading"
      text="暂无关注"
      description="去发现感兴趣的用户吧"
      actionText="话题广场"
      @action="goTopicSquare"
    />

    <!-- 骨架屏 -->
    <WgSkeleton v-if="loading && users.length === 0" type="list" :rows="5" />
  </view>
</template>

<style lang="scss" scoped>
.follow-page {
  min-height: 100vh;
  background: $bg-page;
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
</style>
