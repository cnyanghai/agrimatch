<script setup lang="ts">
defineProps<{
  /** 行数 */
  rows?: number
  /** 是否显示头像 */
  avatar?: boolean
  /** 是否显示标题 */
  title?: boolean
  /** 骨架屏类型 */
  type?: 'card' | 'list' | 'detail' | 'text'
}>()
</script>

<template>
  <view class="wg-skeleton" :class="`wg-skeleton--${type || 'card'}`">
    <!-- 卡片骨架 -->
    <view v-if="type === 'card' || !type" v-for="i in (rows || 3)" :key="i" class="wg-skeleton__card">
      <view v-if="avatar" class="wg-skeleton__avatar shimmer" />
      <view class="wg-skeleton__content">
        <view v-if="title" class="wg-skeleton__title shimmer" />
        <view class="wg-skeleton__line shimmer" />
        <view class="wg-skeleton__line wg-skeleton__line--short shimmer" />
      </view>
    </view>

    <!-- 列表骨架 -->
    <view v-if="type === 'list'" v-for="i in (rows || 5)" :key="i" class="wg-skeleton__list-item">
      <view v-if="avatar" class="wg-skeleton__avatar shimmer" />
      <view class="wg-skeleton__content">
        <view class="wg-skeleton__line shimmer" />
        <view class="wg-skeleton__line wg-skeleton__line--short shimmer" />
      </view>
    </view>

    <!-- 详情骨架 -->
    <view v-if="type === 'detail'" class="wg-skeleton__detail">
      <view class="wg-skeleton__block shimmer" />
      <view class="wg-skeleton__section">
        <view class="wg-skeleton__title shimmer" />
        <view v-for="i in (rows || 4)" :key="i" class="wg-skeleton__line shimmer" />
      </view>
      <view class="wg-skeleton__section">
        <view class="wg-skeleton__title shimmer" />
        <view class="wg-skeleton__line shimmer" />
        <view class="wg-skeleton__line wg-skeleton__line--short shimmer" />
      </view>
    </view>

    <!-- 文本骨架 -->
    <view v-if="type === 'text'" class="wg-skeleton__text">
      <view v-for="i in (rows || 3)" :key="i"
        class="wg-skeleton__line shimmer"
        :style="{ width: i === (rows || 3) ? '60%' : '100%' }"
      />
    </view>
  </view>
</template>

<style lang="scss" scoped>
.wg-skeleton {
  padding: $spacing-md;
}

/* Shimmer animation */
@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.shimmer {
  background: linear-gradient(90deg, #F5F0E8 25%, #E8E0D4 37%, #F5F0E8 63%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite ease-in-out;
  border-radius: $radius-sm;
}

/* Card skeleton */
.wg-skeleton__card {
  display: flex;
  gap: $spacing-md;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;
  background: $bg-card;
  border-radius: $radius-lg;
}

.wg-skeleton__avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.wg-skeleton__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.wg-skeleton__title {
  height: 36rpx;
  width: 50%;
}

.wg-skeleton__line {
  height: 28rpx;
  width: 100%;

  &--short {
    width: 65%;
  }
}

/* List skeleton */
.wg-skeleton__list-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-light;
}

/* Detail skeleton */
.wg-skeleton__block {
  height: 300rpx;
  margin-bottom: $spacing-md;
  border-radius: $radius-lg;
}

.wg-skeleton__section {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-lg;
  margin-bottom: $spacing-sm;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

/* Text skeleton */
.wg-skeleton__text {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  padding: $spacing-md;
}
</style>
