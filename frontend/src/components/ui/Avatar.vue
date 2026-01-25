<script setup lang="ts">
import { computed } from 'vue'
import { User } from 'lucide-vue-next'

const props = withDefaults(defineProps<{
  src?: string | null
  name?: string
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'
  shape?: 'circle' | 'rounded'
}>(), {
  size: 'md',
  shape: 'circle'
})

// 尺寸映射
const sizeClasses: Record<string, { container: string; text: string; icon: number }> = {
  xs: { container: 'w-6 h-6', text: 'text-[10px]', icon: 12 },
  sm: { container: 'w-8 h-8', text: 'text-xs', icon: 14 },
  md: { container: 'w-10 h-10', text: 'text-sm', icon: 18 },
  lg: { container: 'w-14 h-14', text: 'text-xl', icon: 24 },
  xl: { container: 'w-20 h-20', text: 'text-2xl', icon: 32 }
}

// 形状映射
const shapeClasses: Record<string, string> = {
  circle: 'rounded-full',
  rounded: 'rounded-xl'
}

// 获取首字母
const initial = computed(() => {
  if (!props.name) return ''
  const trimmed = props.name.trim()
  if (!trimmed) return ''
  const firstChar = trimmed[0] || ''
  // 如果是数字，返回 'U'
  return /\d/.test(firstChar) ? 'U' : firstChar.toUpperCase()
})

const currentSize = computed((): { container: string; text: string; icon: number } => {
  return sizeClasses[props.size] ?? { container: 'w-10 h-10', text: 'text-sm', icon: 18 }
})
</script>

<template>
  <div
    :class="[
      'flex items-center justify-center shrink-0 overflow-hidden',
      shapeClasses[shape],
      currentSize.container,
      !src && 'bg-brand-50 text-brand-600'
    ]"
  >
    <!-- 图片头像 -->
    <img
      v-if="src"
      :src="src"
      :alt="name || '头像'"
      class="w-full h-full object-cover"
    />
    <!-- 文字首字母 -->
    <span
      v-else-if="initial"
      :class="['font-bold', currentSize.text]"
    >
      {{ initial }}
    </span>
    <!-- 默认图标 -->
    <User
      v-else
      :size="currentSize.icon"
      class="text-brand-400"
    />
  </div>
</template>
