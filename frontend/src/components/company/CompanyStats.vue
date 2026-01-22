<script setup lang="ts">
import { computed } from 'vue'
import { Package, ShoppingCart, Star, TrendingUp, Users } from 'lucide-vue-next'

export interface CompanyStatsProps {
  supplyCount?: number
  requirementCount?: number
  creditScore?: number
  dealCount?: number
  followerCount?: number
  layout?: 'horizontal' | 'grid'
}

const props = withDefaults(defineProps<CompanyStatsProps>(), {
  supplyCount: 0,
  requirementCount: 0,
  creditScore: 0,
  dealCount: 0,
  followerCount: 0,
  layout: 'horizontal'
})

const stats = computed(() => [
  {
    icon: Package,
    label: '供应',
    value: props.supplyCount,
    color: 'text-brand-600',
    bg: 'bg-brand-50'
  },
  {
    icon: ShoppingCart,
    label: '需求',
    value: props.requirementCount,
    color: 'text-blue-600',
    bg: 'bg-blue-50'
  },
  {
    icon: Star,
    label: '信用分',
    value: props.creditScore,
    color: 'text-amber-600',
    bg: 'bg-amber-50'
  },
  {
    icon: TrendingUp,
    label: '成交',
    value: props.dealCount,
    color: 'text-emerald-600',
    bg: 'bg-emerald-50'
  },
  {
    icon: Users,
    label: '关注',
    value: props.followerCount,
    color: 'text-purple-600',
    bg: 'bg-purple-50'
  }
])
</script>

<template>
  <section :class="['flex gap-6', layout === 'horizontal' ? 'flex-wrap' : 'grid grid-cols-2 md:grid-cols-5']" role="region" aria-label="公司统计信息">
    <div
      v-for="(stat, index) in stats"
      :key="index"
      class="flex items-center gap-3 p-4 rounded-xl transition-all hover:scale-105 cursor-pointer focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-brand-500"
      :class="[stat.bg, layout === 'horizontal' ? 'flex-1 min-w-[140px]' : 'flex-col justify-center']"
      tabindex="0"
      role="button"
      :aria-label="`${stat.label}: ${stat.value}`"
    >
      <component :is="stat.icon" :class="[stat.color, layout === 'horizontal' ? 'w-5 h-5' : 'w-6 h-6 mb-2']" aria-hidden="true" />
      <div :class="[layout === 'horizontal' ? '' : 'text-center']">
        <div :class="['font-bold', stat.color]">{{ stat.value }}</div>
        <div class="text-xs text-gray-500">{{ stat.label }}</div>
      </div>
    </div>
  </section>
</template>
