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
    warning: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
    error: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
    info: 'bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400',
    brand: 'bg-brand-50 text-brand-600 dark:bg-brand-900/30 dark:text-brand-400',
    autumn: 'bg-autumn-50 text-autumn-700 dark:bg-autumn-900/30 dark:text-autumn-400',
    purple: 'bg-purple-50 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400'
  }
  
  return `${base} ${sizeClass} ${typeMap[props.type] || typeMap.info}`
})
</script>

<template>
  <span :class="classes">
    <slot />
  </span>
</template>

