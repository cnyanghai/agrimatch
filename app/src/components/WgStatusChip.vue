<script setup lang="ts">
/**
 * WgStatusChip — Stitch 状态胶囊
 *
 * 6 种语义：brand(品牌绿) | autumn(采购金) | accent(价格橙)
 *           success | warning | error | neutral
 *
 * 用法：<WgStatusChip label="有效" variant="brand" />
 */

withDefaults(defineProps<{
  label: string
  variant?: 'brand' | 'autumn' | 'accent' | 'success' | 'warning' | 'error' | 'neutral'
  size?: 'sm' | 'md'
  icon?: string
  dot?: boolean
}>(), {
  variant: 'neutral',
  size: 'md',
  icon: '',
  dot: false,
})
</script>

<template>
  <view
    class="wg-chip"
    :class="[`wg-chip--${variant}`, `wg-chip--${size}`]"
  >
    <view v-if="dot" class="wg-chip__dot" />
    <WgIcon v-if="icon" :name="icon" :size="size === 'sm' ? 12 : 14" color="currentColor" />
    <text class="wg-chip__label">{{ label }}</text>
  </view>
</template>

<style lang="scss" scoped>
.wg-chip {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  border-radius: $radius-full;
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;

  &--md {
    height: 48rpx;
    padding: 0 $spacing-sm;
    font-size: $font-xs;
  }

  &--sm {
    height: 36rpx;
    padding: 0 12rpx;
    font-size: 20rpx;
  }

  &__dot {
    width: 12rpx;
    height: 12rpx;
    border-radius: 50%;
    background: currentColor;
  }

  &__label {
    line-height: 1;
  }

  &--brand {
    background: $brand-50;
    color: $brand-600;
  }

  &--autumn {
    background: $autumn-50;
    color: $autumn-600;
  }

  &--accent {
    background: $accent-50;
    color: $accent-400;
  }

  &--success {
    background: rgba(34, 197, 94, 0.08);
    color: $color-success;
  }

  &--warning {
    background: rgba(245, 158, 11, 0.08);
    color: $color-warning;
  }

  &--error {
    background: rgba(239, 68, 68, 0.08);
    color: $color-error;
  }

  &--neutral {
    background: $warm-100;
    color: $warm-500;
  }
}
</style>
