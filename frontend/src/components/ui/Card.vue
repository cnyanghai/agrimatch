<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  variant?: 'default' | 'interactive' | 'info' | 'warning' | 'success'
  padding?: 'sm' | 'md' | 'lg'
  shadow?: 'sm' | 'md' | 'lg' | 'xl'
  hover?: boolean
  border?: boolean
  noPadding?: boolean
}>(), {
  variant: 'default',
  padding: 'md',
  shadow: 'md',
  hover: false,
  border: true,
  noPadding: false
})

// 卡片样式
const cardClasses = computed(() => {
  const variants: Record<string, string> = {
    default: 'bg-white dark:bg-neutral-900',
    interactive: 'bg-white dark:bg-neutral-900 cursor-pointer',
    info: 'bg-blue-50 dark:bg-blue-900/20',
    warning: 'bg-warning/10 dark:bg-warning/10',
    success: 'bg-primary/50 dark:bg-primary/10'
  }

  const paddings: Record<string, string> = {
    sm: 'p-4',
    md: 'p-6',
    lg: 'p-8'
  }

  const shadows: Record<string, string> = {
    sm: 'shadow-sm',
    md: 'shadow-md',
    lg: 'shadow-lg',
    xl: 'shadow-xl'
  }

  let classes = [
    'rounded-2xl transition-all duration-300',
    variants[props.variant],
    paddings[props.padding],
    shadows[props.shadow]
  ]

  if (props.border && props.variant === 'default') {
    classes.push('border-2 border-neutral-200 dark:border-neutral-800')
  }

  if (props.border && props.variant === 'interactive') {
    classes.push('border-2 border-neutral-200 dark:border-neutral-800 hover:border-primary-300')
  }

  if (props.hover && props.variant === 'interactive') {
    classes.push('hover:shadow-lg hover:-translate-y-0.5')
  }

  return classes.filter(Boolean).join(' ')
})
</script>

<template>
  <div :class="cardClasses" :style="noPadding ? 'padding: 0' : ''">
    <slot />
  </div>
</template>
