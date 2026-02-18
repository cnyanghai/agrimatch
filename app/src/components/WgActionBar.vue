<script setup lang="ts">
/**
 * WgActionBar — Stitch 底部操作栏
 *
 * 固定吸底、毛玻璃背景、安全区适配
 * 用法:
 *   <WgActionBar>
 *     <template #left>价格信息</template>
 *     <button class="..." @tap="...">提交</button>
 *   </WgActionBar>
 */
defineSlots<{
  left?(): any
  default(): any
}>()
</script>

<template>
  <view class="wg-action-bar safe-area-bottom">
    <view class="wg-action-bar__inner">
      <view v-if="$slots.left" class="wg-action-bar__left">
        <slot name="left" />
      </view>
      <view class="wg-action-bar__actions">
        <slot />
      </view>
    </view>
  </view>
  <view class="wg-action-bar__placeholder" />
</template>

<style lang="scss" scoped>
.wg-action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 90;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(40rpx) saturate(180%);
  -webkit-backdrop-filter: blur(40rpx) saturate(180%);
  border-top: 1rpx solid rgba(0, 0, 0, 0.04);
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);

  &__inner {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    padding: $spacing-sm $spacing-md;
  }

  &__left {
    flex-shrink: 0;
  }

  &__actions {
    flex: 1;
    display: flex;
    gap: $spacing-sm;
  }

  &__placeholder {
    height: 140rpx;
  }
}

/* 通用底部按钮样式 */
:deep(.wg-btn) {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border: none;
  border-radius: $radius-xl;
  font-size: $font-md;
  font-weight: 700;
  text-align: center;
  transition: transform $transition-fast;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
}

:deep(.wg-btn:active) {
  transform: scale(0.96);
}

:deep(.wg-btn--primary) {
  background: $brand-600;
  color: $text-inverse;
  box-shadow: $shadow-brand;
}

:deep(.wg-btn--secondary) {
  background: $brand-50;
  color: $brand-600;
}

:deep(.wg-btn--action) {
  background: $action-600;
  color: $text-inverse;
  box-shadow: $shadow-action;
}

:deep(.wg-btn--accent) {
  background: $accent-400;
  color: $text-inverse;
  box-shadow: $shadow-accent;
}

:deep(.wg-btn--ghost) {
  background: transparent;
  color: $text-secondary;
}

:deep(.wg-btn--error-soft) {
  background: rgba(239, 68, 68, 0.06);
  color: $color-error;
  border: 1rpx solid rgba(239, 68, 68, 0.2);
}

:deep(.wg-btn[disabled]) {
  opacity: 0.45;
  pointer-events: none;
}
</style>
