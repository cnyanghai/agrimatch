<script setup lang="ts">
/**
 * WgAvatar — Stitch 头像组件
 *
 * 支持图片/首字母fallback，圆形和方形（带大圆角）
 */
import { computed } from 'vue'
import { BRAND_600 } from '../constants/colors'

const props = withDefaults(defineProps<{
  src?: string
  name?: string
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'
  shape?: 'circle' | 'square'
}>(), {
  src: '',
  name: '',
  size: 'md',
  shape: 'circle',
})

const sizeMap: Record<string, number> = {
  xs: 48,
  sm: 64,
  md: 80,
  lg: 100,
  xl: 128,
}

const fontMap: Record<string, number> = {
  xs: 22, sm: 26, md: 32, lg: 40, xl: 48,
}

const dimension = computed(() => sizeMap[props.size] + 'rpx')
const fontSize = computed(() => fontMap[props.size] + 'rpx')
const initial = computed(() => (props.name || '?')[0].toUpperCase())
const borderRadius = computed(() =>
  props.shape === 'circle' ? '50%' : '24rpx'
)
</script>

<template>
  <view
    class="wg-avatar"
    :style="{
      width: dimension,
      height: dimension,
      borderRadius,
    }"
  >
    <image
      v-if="src"
      class="wg-avatar__img"
      :src="src"
      mode="aspectFill"
      :style="{ borderRadius }"
    />
    <text
      v-else
      class="wg-avatar__initial"
      :style="{ fontSize }"
    >{{ initial }}</text>
  </view>
</template>

<style lang="scss" scoped>
.wg-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: $brand-50;
  flex-shrink: 0;
  overflow: hidden;

  &__img {
    width: 100%;
    height: 100%;
  }

  &__initial {
    font-weight: 700;
    color: $brand-600;
  }
}
</style>
