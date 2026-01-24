<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  variant?: 'default' | 'interactive' | 'info' | 'warning' | 'success' | 'glass' | 'slate'
  padding?: 'none' | 'sm' | 'md' | 'lg' | 'xl'
  shadow?: 'none' | 'sm' | 'md' | 'lg' | 'xl' | '2xl' | 'brand'
  radius?: 'xl' | '2xl' | '3xl'
  hover?: boolean
  border?: boolean
}>(), {
  variant: 'default',
  padding: 'md',
  shadow: 'sm',
  radius: 'xl',
  hover: false,
  border: true
})

const cardClasses = computed(() => {
  const base = 'transition-all duration-300 overflow-hidden'
  
  const variants: Record<string, string> = {
    default: 'bg-white dark:bg-slate-900',
    interactive: 'bg-white dark:bg-slate-900 cursor-pointer',
    info: 'bg-autumn-50/50 dark:bg-autumn-900/20',
    warning: 'bg-amber-50/50 dark:bg-amber-900/20',
    success: 'bg-brand-50/50 dark:bg-brand-900/20',
    glass: 'bg-white/10 backdrop-blur-xl border-white/20',
    slate: 'bg-slate-900 text-white'
  }

  const paddings: Record<string, string> = {
    none: 'p-0',
    sm: 'p-4',
    md: 'p-6',
    lg: 'p-8',
    xl: 'p-10'
  }

  const shadows: Record<string, string> = {
    none: 'shadow-none',
    sm: 'shadow-sm',
    md: 'shadow-md',
    lg: 'shadow-lg',
    xl: 'shadow-xl',
    '2xl': 'shadow-2xl',
    brand: 'shadow-brand'
  }

  const radii: Record<string, string> = {
    xl: 'rounded-xl',
    '2xl': 'rounded-2xl',
    '3xl': 'rounded-3xl'
  }

  let classes = [
    base,
    radii[props.radius],
    variants[props.variant],
    paddings[props.padding],
    shadows[props.shadow]
  ]

  if (props.border && props.variant !== 'glass') {
    classes.push('border border-gray-100 dark:border-slate-800')
  }

  if (props.hover) {
    classes.push('hover:shadow-lg hover:-translate-y-1')
    if (props.variant === 'interactive') {
      classes.push('hover:border-brand-200 dark:hover:border-brand-900/50')
    }
  }

  return classes.filter(Boolean).join(' ')
})
</script>

<template>
  <div :class="cardClasses">
    <slot />
  </div>
</template>
