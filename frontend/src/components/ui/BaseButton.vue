<script setup lang="ts">
import { computed } from 'vue'
import { Loader2 } from 'lucide-vue-next'

const props = withDefaults(defineProps<{
  type?: 'primary' | 'secondary' | 'danger' | 'ghost' | 'outline'
  size?: 'sm' | 'md' | 'lg'
  loading?: boolean
  disabled?: boolean
  block?: boolean
  icon?: boolean
}>(), {
  type: 'primary',
  size: 'md',
  loading: false,
  disabled: false,
  block: false,
  icon: false
})

const emit = defineEmits<{
  (e: 'click', event: MouseEvent): void
}>()

// 类型样式映射
const typeClasses = computed(() => {
  const types: Record<string, string> = {
    primary: 'bg-emerald-600 hover:bg-emerald-700 text-white shadow-sm shadow-emerald-100',
    secondary: 'bg-gray-100 hover:bg-gray-200 text-gray-700',
    danger: 'bg-red-50 hover:bg-red-100 text-red-600',
    ghost: 'bg-transparent hover:bg-gray-50 text-gray-600',
    outline: 'bg-white border-2 border-gray-200 hover:border-emerald-500 hover:bg-emerald-50 text-gray-700 hover:text-emerald-600'
  }
  return types[props.type] || types.primary
})

// 尺寸样式映射
const sizeClasses = computed(() => {
  if (props.icon) {
    const iconSizes: Record<string, string> = {
      sm: 'w-8 h-8',
      md: 'w-10 h-10',
      lg: 'w-12 h-12'
    }
    return iconSizes[props.size] || iconSizes.md
  }
  
  const sizes: Record<string, string> = {
    sm: 'px-3 py-1.5 text-xs',
    md: 'px-4 py-2.5 text-sm',
    lg: 'px-6 py-3 text-base'
  }
  return sizes[props.size] || sizes.md
})

// 是否禁用
const isDisabled = computed(() => props.disabled || props.loading)

function handleClick(e: MouseEvent) {
  if (!isDisabled.value) {
    emit('click', e)
  }
}
</script>

<template>
  <button
    :class="[
      'rounded-xl font-bold transition-all active:scale-95 flex items-center justify-center gap-2',
      typeClasses,
      sizeClasses,
      {
        'w-full': block,
        'opacity-50 cursor-not-allowed active:scale-100': isDisabled,
        'hover:-translate-y-0.5': !isDisabled && !icon
      }
    ]"
    :disabled="isDisabled"
    @click="handleClick"
  >
    <!-- Loading 图标 -->
    <Loader2 v-if="loading" class="w-4 h-4 animate-spin" />
    
    <!-- 内容插槽 -->
    <slot />
  </button>
</template>

