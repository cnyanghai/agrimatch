<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { formatRelativeTime } from '../../utils/format'
import {
  getPost,
  togglePostLike,
  togglePostCollect,
  listPostComments,
  createPostComment,
  updatePost,
  deletePost,
  type PostResponse,
  type PostCommentResponse,
} from '../../api/post'
import { useAuthStore } from '../../store/auth'
import { useFollow } from '../../composables/useFollow'

const authStore = useAuthStore()
const { isFollowing, followLoading, loadFollowStatus, handleToggleFollow, canFollow }
  = useFollow(() => detail.value?.userId)
const detail = ref<PostResponse | null>(null)
const comments = ref<PostCommentResponse[]>([])
const loading = ref(true)
const commentText = ref('')
const submitting = ref(false)
const likeLoading = ref(false)
const collectLoading = ref(false)

/** 是否是自己的帖子 */
const isMyPost = computed(() => {
  return detail.value?.userId === authStore.user?.userId
})

/** 解析图片列表 */
const imageList = computed<string[]>(() => {
  if (!detail.value?.imagesJson) return []
  try {
    return JSON.parse(detail.value.imagesJson)
  } catch {
    return []
  }
})

onLoad(async (options) => {
  if (options?.id) {
    const postId = Number(options.id)
    await loadDetail(postId)
    loadComments(postId)
  }
})

async function loadDetail(id: number) {
  loading.value = true
  try {
    detail.value = await getPost(id)
    await loadFollowStatus()
  } catch {
    // handled by request.ts
  } finally {
    loading.value = false
  }
}

function goAuthorProfile() {
  if (detail.value?.userId) {
    uni.navigateTo({ url: `/pages/user/profile?id=${detail.value.userId}` })
  }
}

function handleShare() {
  uni.showToast({ title: '分享功能开发中', icon: 'none' })
}

async function loadComments(postId: number) {
  try {
    const res = await listPostComments(postId)
    comments.value = res || []
  } catch {
    // silent
  }
}

/** 切换点赞 */
async function handleToggleLike() {
  if (!detail.value) return
  if (!authStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  if (likeLoading.value) return
  likeLoading.value = true
  try {
    const res = await togglePostLike(detail.value.id)
    detail.value.likedByMe = res.liked
    detail.value.likeCount = res.likeCount
  } catch {
    // handled by request.ts
  } finally {
    likeLoading.value = false
  }
}

/** 切换收藏 */
async function handleToggleCollect() {
  if (!detail.value) return
  if (!authStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  if (collectLoading.value) return
  collectLoading.value = true
  try {
    const collected = await togglePostCollect(detail.value.id)
    detail.value.collectedByMe = collected
    uni.showToast({ title: collected ? '已收藏' : '已取消收藏', icon: 'none' })
  } catch {
    // handled by request.ts
  } finally {
    collectLoading.value = false
  }
}

/** 提交评论 */
async function handleSubmitComment() {
  if (!detail.value) return
  if (!authStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  const content = commentText.value.trim()
  if (!content) {
    uni.showToast({ title: '请输入评论内容', icon: 'none' })
    return
  }
  if (submitting.value) return
  submitting.value = true
  try {
    await createPostComment(detail.value.id, content)
    commentText.value = ''
    uni.showToast({ title: '评论成功', icon: 'none' })
    // 重新加载评论列表
    await loadComments(detail.value.id)
    // 更新评论数
    if (detail.value.commentCount !== undefined) {
      detail.value.commentCount += 1
    }
  } catch {
    // handled by request.ts
  } finally {
    submitting.value = false
  }
}

/** 预览图片 */
function handlePreviewImage(index: number) {
  uni.previewImage({
    current: index,
    urls: imageList.value,
  })
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

/** 获取评论者显示名称 */
function getCommentName(c: PostCommentResponse): string {
  return c.nickName || c.userName || '匿名用户'
}

/** 获取评论者首字 */
function getCommentInitial(c: PostCommentResponse): string {
  const name = c.nickName || c.userName || '?'
  return name.charAt(0)
}

/** 作者更多菜单 */
function handleAuthorMenu() {
  uni.showActionSheet({
    itemList: ['编辑', '删除'],
    success: (res) => {
      if (res.tapIndex === 0) {
        // 编辑
        if (detail.value) {
          uni.navigateTo({ url: `/pages/topic/edit?id=${detail.value.id}` })
        }
      } else if (res.tapIndex === 1) {
        // 删除
        handleDeletePost()
      }
    },
  })
}

/** 删除帖子 */
function handleDeletePost() {
  if (!detail.value) return
  uni.showModal({
    title: '删除话题',
    content: '确认删除该话题？此操作不可撤销。',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await deletePost(detail.value!.id)
        uni.showToast({ title: '已删除', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 500)
      } catch {
        // handled
      }
    },
  })
}

</script>

<template>
  <view class="detail-page">
    <!-- 加载中 -->
    <WgSkeleton v-if="loading" type="detail" />

    <!-- 不存在 -->
    <WgEmpty v-else-if="!detail" text="话题不存在" description="该话题已删除或不存在" icon="empty" />

    <template v-else>
      <scroll-view scroll-y class="detail-page__scroll">
        <!-- 作者信息 -->
        <view class="author-section">
          <view class="author-section__avatar" @tap="goAuthorProfile">
            <image
              v-if="detail.avatar"
              class="author-section__avatar-img"
              :src="detail.avatar"
              mode="aspectFill"
            />
            <text v-else class="author-section__avatar-text">{{ getInitial(detail) }}</text>
          </view>
          <view class="author-section__info" @tap="goAuthorProfile">
            <text class="author-section__name">{{ getDisplayName(detail) }}</text>
            <view class="author-section__meta">
              <text v-if="detail.companyName" class="author-section__company">{{ detail.companyName }}</text>
              <text v-if="detail.position" class="author-section__position">{{ detail.position }}</text>
            </view>
          </view>
          <view
            v-if="canFollow()"
            class="follow-btn"
            :class="{ 'follow-btn--active': isFollowing }"
            @tap="handleToggleFollow"
          >
            <text class="follow-btn__text">{{ followLoading ? '...' : (isFollowing ? '已关注' : '+ 关注') }}</text>
          </view>
          <text class="author-section__time">{{ formatRelativeTime(detail.createTime) }}</text>
          <view
            v-if="isMyPost"
            class="author-menu-btn"
            @tap="handleAuthorMenu"
          >
            <text class="author-menu-btn__text">···</text>
          </view>
        </view>

        <!-- 帖子内容 -->
        <view class="content-section">
          <text class="content-section__title">{{ detail.title }}</text>

          <!-- 领域标签 -->
          <view v-if="detail.domain" class="content-section__domain">
            <text class="content-section__domain-tag">{{ detail.domain }}</text>
          </view>

          <view v-if="detail.content" class="content-section__body rich-content" v-html="detail.content" />

          <!-- 图片列表 -->
          <view v-if="imageList.length > 0" class="content-section__images">
            <image
              v-for="(img, idx) in imageList"
              :key="idx"
              class="content-section__image"
              :src="img"
              mode="widthFix"
              @tap="handlePreviewImage(idx)"
            />
          </view>
        </view>

        <!-- 互动栏 -->
        <view class="action-bar">
          <view
            class="action-bar__item"
            @tap="handleToggleLike"
          >
            <uni-icons :type="detail.likedByMe ? 'heart-filled' : 'heart'" size="22" :color="detail.likedByMe ? '#E76F51' : '#999'" />
            <text class="action-bar__label">{{ detail.likeCount || 0 }}</text>
          </view>
          <view class="action-bar__item">
            <uni-icons type="chat" size="22" color="#999" />
            <text class="action-bar__label">{{ detail.commentCount || 0 }}</text>
          </view>
          <view
            class="action-bar__item"
            @tap="handleToggleCollect"
          >
            <uni-icons :type="detail.collectedByMe ? 'star-filled' : 'star'" size="22" :color="detail.collectedByMe ? '#D4A373' : '#999'" />
            <text class="action-bar__label">{{ detail.collectedByMe ? '已收藏' : '收藏' }}</text>
          </view>
          <view class="action-bar__item" @tap="handleShare">
            <uni-icons type="redo" size="22" color="#999" />
            <text class="action-bar__label">分享</text>
          </view>
        </view>

        <!-- 评论区 -->
        <view class="comment-section">
          <text class="comment-section__title">评论 ({{ comments.length }})</text>

          <view v-if="comments.length > 0" class="comment-list">
            <view
              v-for="c in comments"
              :key="c.id"
              class="comment-item"
            >
              <view class="comment-item__avatar">
                <text class="comment-item__avatar-text">{{ getCommentInitial(c) }}</text>
              </view>
              <view class="comment-item__body">
                <view class="comment-item__header">
                  <text class="comment-item__name">{{ getCommentName(c) }}</text>
                  <text class="comment-item__time">{{ formatRelativeTime(c.createTime) }}</text>
                </view>
                <text class="comment-item__content">{{ c.content }}</text>
              </view>
            </view>
          </view>

          <view v-else style="padding: 24rpx 0;">
            <WgEmpty text="暂无评论" description="快来抢沙发" />
          </view>
        </view>

        <!-- 底部占位，防止内容被输入框遮挡 -->
        <view class="bottom-placeholder"></view>
      </scroll-view>

      <!-- 底部评论输入 -->
      <view class="comment-bar safe-area-bottom">
        <input
          class="comment-bar__input"
          v-model="commentText"
          placeholder="写下你的评论..."
          :maxlength="500"
          confirm-type="send"
          @confirm="handleSubmitComment"
        />
        <view
          class="comment-bar__btn"
          :class="{ 'comment-bar__btn--disabled': !commentText.trim() || submitting }"
          @tap="handleSubmitComment"
        >
          <text class="comment-bar__btn-text">{{ submitting ? '...' : '发送' }}</text>
        </view>
      </view>
    </template>
  </view>
</template>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  background: $bg-page;

  &__scroll {
    height: calc(100vh - 120rpx);
  }
}

/* 作者区域 */
.author-section {
  display: flex;
  align-items: center;
  background: $bg-card;
  padding: $spacing-md $spacing-lg;

  &__avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: $brand-100;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    overflow: hidden;
  }

  &__avatar-img {
    width: 80rpx;
    height: 80rpx;
  }

  &__avatar-text {
    font-size: $font-lg;
    font-weight: bold;
    color: $brand-600;
  }

  &__info {
    flex: 1;
    margin-left: $spacing-sm;
    overflow: hidden;
  }

  &__name {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
    display: block;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    margin-top: 4rpx;
  }

  &__company {
    font-size: $font-xs;
    color: $text-secondary;
  }

  &__position {
    font-size: $font-xs;
    color: $text-placeholder;
  }

  &__time {
    font-size: $font-xs;
    color: $text-placeholder;
    flex-shrink: 0;
    margin-left: $spacing-sm;
  }
}

/* 作者菜单按钮 */
.author-menu-btn {
  flex-shrink: 0;
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: $spacing-xs;

  &__text {
    font-size: $font-xl;
    color: $text-secondary;
    font-weight: bold;
    letter-spacing: 2rpx;
  }
}

/* 关注按钮 */
.follow-btn {
  flex-shrink: 0;
  height: 56rpx;
  padding: 0 $spacing-md;
  background: $brand-600;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: $spacing-xs;
  transition: all 0.2s;

  &:active {
    transform: scale(0.95);
  }

  &--active {
    background: $bg-page;
    border: 1rpx solid $border-color;
  }

  &__text {
    font-size: $font-sm;
    color: #fff;
    font-weight: bold;
    white-space: nowrap;
  }

  &--active &__text {
    color: $text-secondary;
  }
}

/* 内容区域 */
.content-section {
  background: $bg-card;
  padding: $spacing-md $spacing-lg;
  padding-top: 0;

  &__title {
    font-size: $font-xl;
    font-weight: bold;
    color: $text-primary;
    display: block;
    line-height: 1.5;
    margin-bottom: $spacing-sm;
  }

  &__domain {
    margin-bottom: $spacing-sm;
  }

  &__domain-tag {
    font-size: $font-xs;
    color: $brand-600;
    background: $brand-50;
    padding: 6rpx 20rpx;
    border-radius: $radius-sm;
  }

  &__body {
    font-size: $font-md;
    color: $text-primary;
    line-height: 1.8;
    word-break: break-all;
  }
}

/* v-html 内部 HTML 元素样式（需要 :deep 穿透 scoped） */
.rich-content {
  :deep(p) {
    margin: 0 0 $spacing-sm;
    line-height: 1.8;
  }

  :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: $radius-md;
    margin: $spacing-xs 0;
    display: block;
  }

  :deep(a) {
    color: $brand-600;
    text-decoration: underline;
  }

  :deep(strong),
  :deep(b) {
    font-weight: bold;
  }

  :deep(em),
  :deep(i) {
    font-style: italic;
  }

  :deep(ul),
  :deep(ol) {
    padding-left: $spacing-lg;
    margin: $spacing-xs 0;
  }

  :deep(li) {
    margin-bottom: $spacing-xs;
  }

  :deep(blockquote) {
    border-left: 6rpx solid $brand-200;
    padding-left: $spacing-md;
    color: $text-secondary;
    margin: $spacing-sm 0;
  }

  :deep(h1),
  :deep(h2),
  :deep(h3) {
    font-weight: bold;
    margin: $spacing-md 0 $spacing-sm;
  }

  :deep(br) {
    content: '';
    display: block;
    margin-top: $spacing-xs;
  }
}

/* 内容区域续 - 图片 */
.content-section {
  &__images {
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
    margin-top: $spacing-md;
  }

  &__image {
    width: 100%;
    border-radius: $radius-md;
  }
}

/* 互动栏 */
.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: $bg-card;
  padding: $spacing-md 0;
  margin-top: $spacing-xs;

  &__item {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    padding: $spacing-xs $spacing-md;
    border-radius: $radius-md;

  }

  &__label {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

/* 评论区 */
.comment-section {
  background: $bg-card;
  margin-top: $spacing-xs;
  padding: $spacing-md $spacing-lg;

  &__title {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;
    display: block;
    margin-bottom: $spacing-md;
  }

}

.comment-list {
  display: flex;
  flex-direction: column;
}

.comment-item {
  display: flex;
  padding: $spacing-sm 0;
  border-bottom: 1rpx solid $border-light;

  &:last-child {
    border-bottom: none;
  }

  &__avatar {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    background: $brand-50;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__avatar-text {
    font-size: $font-xs;
    font-weight: bold;
    color: $brand-600;
  }

  &__body {
    flex: 1;
    margin-left: $spacing-sm;
    overflow: hidden;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-xs;
  }

  &__name {
    font-size: $font-sm;
    font-weight: bold;
    color: $text-primary;
  }

  &__time {
    font-size: $font-xs;
    color: $text-placeholder;
  }

  &__content {
    font-size: $font-md;
    color: $text-primary;
    line-height: 1.6;
    display: block;
    word-break: break-all;
  }
}

/* 底部评论输入 */
.comment-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  background: $bg-card;
  border-top: 1rpx solid $border-light;

  &__input {
    flex: 1;
    height: 72rpx;
    padding: 0 $spacing-md;
    background: $bg-page;
    border-radius: $radius-lg;
    font-size: $font-md;
    color: $text-primary;
  }

  &__btn {
    flex-shrink: 0;
    height: 72rpx;
    padding: 0 $spacing-lg;
    background: $brand-600;
    border-radius: $radius-lg;
    display: flex;
    align-items: center;
    justify-content: center;

    &--disabled {
      opacity: 0.5;
    }
  }

  &__btn-text {
    font-size: $font-md;
    color: #fff;
    font-weight: bold;
  }
}

.bottom-placeholder {
  height: 140rpx;
}
</style>
