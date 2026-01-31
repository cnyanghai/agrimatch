<script setup lang="ts">
import { Coins } from 'lucide-vue-next'

defineProps<{
  price?: number
}>()
</script>

<template>
  <span class="inline-flex items-center gap-1 bg-warning-50 text-warning-700 text-[10px] font-black uppercase tracking-wider px-1.5 py-0.5 rounded-md border border-warning-100 shadow-sm">
    <Coins :size="10" stroke-width="3" />
    付费 {{ price ? '¥' + price.toFixed(2) : '' }}
  </span>
</template>

