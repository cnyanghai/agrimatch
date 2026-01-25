<script setup lang="ts">
/**
 * Quote Diff Badge Component
 * Displays field changes between quotes
 */
import { computed } from 'vue'
import { TrendingUp, TrendingDown, Plus, Minus, RefreshCw } from 'lucide-vue-next'
import type { QuoteFieldDiff } from '../../../types/chat/quote'

const props = defineProps<{
  diffs: QuoteFieldDiff[]
  isSent?: boolean
  maxShow?: number
}>()

const maxShow = computed(() => props.maxShow || 3)

const visibleDiffs = computed(() => props.diffs.slice(0, maxShow.value))
const hiddenCount = computed(() => Math.max(0, props.diffs.length - maxShow.value))

function getDiffIcon(diff: QuoteFieldDiff) {
  if (diff.type === 'added') return Plus
  if (diff.type === 'removed') return Minus
  return RefreshCw
}

function getDiffColor(diff: QuoteFieldDiff) {
  if (diff.type === 'added') return 'text-brand-600 bg-brand-50'
  if (diff.type === 'removed') return 'text-red-500 bg-red-50'
  return 'text-amber-600 bg-amber-50'
}

function formatDiffValue(diff: QuoteFieldDiff): string {
  if (diff.type === 'added') {
    return `${diff.label}: ${diff.newValue}`
  }
  if (diff.type === 'removed') {
    return `${diff.label}: 已移除`
  }
  // changed
  return `${diff.label}: ${diff.oldValue} → ${diff.newValue}`
}

// 检测是否为价格变化，用于显示趋势图标
function isPriceField(key: string): boolean {
  return ['price', 'basisPrice', 'referencePrice'].includes(key)
}

function getPriceTrend(diff: QuoteFieldDiff): 'up' | 'down' | null {
  if (diff.type !== 'changed') return null
  if (!isPriceField(diff.key)) return null

  const oldNum = parseFloat(diff.oldValue?.replace(/[^0-9.-]/g, '') || '0')
  const newNum = parseFloat(diff.newValue?.replace(/[^0-9.-]/g, '') || '0')

  if (newNum > oldNum) return 'up'
  if (newNum < oldNum) return 'down'
  return null
}
</script>

<template>
  <div v-if="diffs.length > 0" class="flex flex-wrap gap-1.5">
    <div
      v-for="diff in visibleDiffs"
      :key="diff.key"
      :class="[
        'inline-flex items-center gap-1 px-2 py-1 rounded-lg text-[10px] font-medium',
        isSent ? 'bg-white/10 text-white' : getDiffColor(diff)
      ]"
    >
      <!-- 图标 -->
      <component
        :is="getDiffIcon(diff)"
        class="w-3 h-3"
      />

      <!-- 价格趋势图标 -->
      <TrendingUp
        v-if="getPriceTrend(diff) === 'up'"
        class="w-3 h-3 text-rose-500"
      />
      <TrendingDown
        v-else-if="getPriceTrend(diff) === 'down'"
        class="w-3 h-3 text-brand-500"
      />

      <!-- 变更内容 -->
      <span class="truncate max-w-[120px]">{{ formatDiffValue(diff) }}</span>
    </div>

    <!-- 更多变更 -->
    <div
      v-if="hiddenCount > 0"
      :class="[
        'inline-flex items-center px-2 py-1 rounded-lg text-[10px] font-medium',
        isSent ? 'bg-white/10 text-white/70' : 'bg-gray-100 text-gray-500'
      ]"
    >
      +{{ hiddenCount }} 项变更
    </div>
  </div>
</template>
