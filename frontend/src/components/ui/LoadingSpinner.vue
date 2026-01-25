<script setup lang="ts">
const props = withDefaults(defineProps<{
  size?: 'sm' | 'md' | 'lg'
  color?: 'brand' | 'gray' | 'white'
}>(), {
  size: 'md',
  color: 'brand'
})

// 尺寸映射
const sizeClasses: Record<string, string> = {
  sm: 'w-5 h-5 border-2',
  md: 'w-8 h-8 border-4',
  lg: 'w-12 h-12 border-4'
}

// 颜色映射
const colorClasses: Record<string, string> = {
  brand: 'border-brand-200 border-t-brand-600',
  gray: 'border-gray-200 border-t-gray-600',
  white: 'border-white/30 border-t-white'
}
</script>

<template>
  <div
    role="status"
    aria-label="加载中"
    :class="[
      'rounded-full animate-spin',
      sizeClasses[size],
      colorClasses[color]
    ]"
  />
</template>
