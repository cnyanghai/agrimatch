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
    default: 'bg-surface dark:bg-surface-dark',
    interactive: 'bg-surface dark:bg-surface-dark cursor-pointer',
    info: 'bg-info/10 dark:bg-info/20',
    warning: 'bg-warning/10 dark:bg-warning/20',
    success: 'bg-success/10 dark:bg-success/20'
  }

  const paddings: Record<string, string> = {
    sm: 'p-4',
    md: 'p-6',
    lg: 'p-8'
  }

  const shadows: Record<string, string> = {
    sm: 'shadow-sm',
    md: 'shadow-md',
    lg: 'shadow-md',
    xl: 'shadow-xl'
  }

  let classes = [
    'rounded-xl transition-all duration-300',
    variants[props.variant],
    paddings[props.padding],
    shadows[props.shadow]
  ]

  if (props.border && props.variant === 'default') {
    classes.push('border border-border dark:border-border-dark')
  }

  if (props.border && props.variant === 'interactive') {
    classes.push('border border-border dark:border-border-dark hover:border-primary')
  }

  if (props.hover && props.variant === 'interactive') {
    classes.push('hover:shadow-md hover:-translate-y-0.5')
  }

  return classes.filter(Boolean).join(' ')
})
</script>

<template>
  <div :class="cardClasses" :style="noPadding ? 'padding: 0' : ''">
    <slot />
  </div>
</template>
