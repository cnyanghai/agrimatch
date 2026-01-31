<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  type?: 'success' | 'warning' | 'error' | 'info' | 'brand' | 'autumn' | 'purple'
  size?: 'sm' | 'md'
}

const props = withDefaults(defineProps<Props>(), {
  type: 'info',
  size: 'sm'
})

const classes = computed(() => {
  const base = 'inline-flex items-center font-bold uppercase tracking-wider rounded-full'
  const sizeClass = props.size === 'sm' ? 'px-2 py-0.5 text-[10px]' : 'px-3 py-1 text-xs'
  
  const typeMap = {
    success: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
    warning: 'bg-warning-100 text-warning-700 dark:bg-warning-900/30 dark:text-warning-400',
    error: 'bg-error-100 text-error-700 dark:bg-error-900/30 dark:text-error-400',
    info: 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400',
    brand: 'bg-brand-50 text-brand-600 dark:bg-brand-900/30 dark:text-brand-400',
    autumn: 'bg-autumn-50 text-autumn-700 dark:bg-autumn-900/30 dark:text-autumn-400',
    purple: 'bg-action-50 text-action-700 dark:bg-action-900/30 dark:text-action-400'
  }
  
  return `${base} ${sizeClass} ${typeMap[props.type] || typeMap.info}`
})
</script>

<template>
  <span :class="classes">
    <slot />
  </span>
</template>

