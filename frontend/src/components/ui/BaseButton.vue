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

// 类型样式映射（使用新的设计系统）
const typeClasses = computed(() => {
  const types: Record<string, string> = {
    primary: 'bg-primary-600 hover:bg-primary-700 text-white shadow-lg shadow-primary/25 hover:scale-105',
    secondary: 'bg-white hover:bg-neutral-50 border-2 border-neutral-200 hover:border-neutral-300 text-neutral-700',
    danger: 'bg-error/10 hover:bg-error/20 text-error',
    ghost: 'bg-transparent hover:bg-primary/10 text-neutral-600 hover:text-primary-600',
    outline: 'bg-white border-2 border-neutral-200 hover:border-primary-500 hover:bg-primary/50 text-neutral-700 hover:text-primary-600'
  }
  return types[props.type] || types.primary
})

// 尺寸样式映射（使用新的设计系统）
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
    sm: 'px-4 py-2 text-sm rounded-lg',
    md: 'px-6 py-3 text-base rounded-xl',
    lg: 'px-8 py-4 text-lg rounded-2xl'
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
      'font-semibold transition-all duration-200 ease-out flex items-center justify-center gap-2 active:scale-95',
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
