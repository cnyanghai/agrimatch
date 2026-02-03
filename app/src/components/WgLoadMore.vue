<script setup lang="ts">
defineProps<{
  /** 当前状态 */
  status: 'loading' | 'more' | 'noMore'
}>()

defineEmits<{
  loadMore: []
}>()
</script>

<template>
  <view class="wg-load-more" @tap="status === 'more' && $emit('loadMore')">
    <view v-if="status === 'loading'" class="wg-load-more__loading">
      <view class="wg-load-more__spinner" />
      <text class="wg-load-more__text">加载中...</text>
    </view>
    <view v-else-if="status === 'more'" class="wg-load-more__more">
      <text class="wg-load-more__text wg-load-more__text--action">点击加载更多</text>
    </view>
    <view v-else class="wg-load-more__end">
      <view class="wg-load-more__line" />
      <text class="wg-load-more__text">没有更多了</text>
      <view class="wg-load-more__line" />
    </view>
  </view>
</template>

<style lang="scss" scoped>
.wg-load-more {
  padding: $spacing-lg 0;
  display: flex;
  align-items: center;
  justify-content: center;

  &__loading,
  &__more,
  &__end {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__text {
    font-size: $font-sm;
    color: $text-placeholder;

    &--action {
      color: $brand-600;
    }
  }

  &__spinner {
    width: 32rpx;
    height: 32rpx;
    border: 4rpx solid $border-light;
    border-top-color: $brand-600;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  &__line {
    width: 60rpx;
    height: 1rpx;
    background: $border-color;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
