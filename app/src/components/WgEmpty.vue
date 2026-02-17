<script setup lang="ts">
defineProps<{
  /** 提示文字 */
  text?: string
  /** 详细描述 */
  description?: string
  /** 操作按钮文字 */
  actionText?: string
  /** 图标类型 */
  icon?: 'empty' | 'search' | 'network' | 'auth'
}>()

defineEmits<{
  action: []
}>()

import { WARM_400 } from '../constants/colors'

const iconMap: Record<string, { name: string; color: string }> = {
  empty: { name: 'mail', color: WARM_400 },
  search: { name: 'search', color: WARM_400 },
  network: { name: 'info', color: WARM_400 },
  auth: { name: 'lock', color: WARM_400 },
}
</script>

<template>
  <view class="wg-empty">
    <view class="wg-empty__icon-wrapper">
      <WgIcon
        :name="iconMap[icon || 'empty'].name"
        :size="40"
        :color="iconMap[icon || 'empty'].color"
      />
    </view>
    <text class="wg-empty__text">{{ text || '暂无数据' }}</text>
    <text v-if="description" class="wg-empty__desc">{{ description }}</text>
    <view v-if="actionText" class="wg-empty__action" @tap="$emit('action')">
      <text class="wg-empty__action-text">{{ actionText }}</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.wg-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx $spacing-xl 80rpx;

  &__icon-wrapper {
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    background: $warm-100;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: $spacing-lg;
  }

  &__text {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;
    margin-bottom: $spacing-xs;
  }

  &__desc {
    font-size: $font-sm;
    color: $text-placeholder;
    text-align: center;
    padding: 0 40rpx;
    line-height: 1.6;
    margin-bottom: $spacing-lg;
  }

  &__action {
    background: $brand-600;
    border-radius: $radius-lg;
    padding: $spacing-sm $spacing-xl;
    min-width: 240rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 80rpx;

    &:active {
      opacity: 0.85;
      transform: scale(0.97);
    }
  }

  &__action-text {
    color: $text-inverse;
    font-size: $font-md;
    font-weight: bold;
  }
}
</style>
