<script setup lang="ts">
import { Check } from 'lucide-vue-next'

defineProps<{
  current: number
  total: number
}>()
</script>

<template>
  <div class="flex items-center justify-center gap-2 mb-8">
    <template v-for="i in total" :key="i">
      <!-- Dot -->
      <div
        class="flex items-center justify-center rounded-full transition-all duration-300 ease-out"
        :class="[
          i - 1 < current
            ? 'w-6 h-6 bg-brand-400 text-white'
            : i - 1 === current
              ? 'w-7 h-7 bg-brand-600 text-white scale-110 shadow-md shadow-brand-200'
              : 'w-6 h-6 bg-neutral-200'
        ]"
      >
        <Check v-if="i - 1 < current" :size="12" :stroke-width="3" />
        <span v-else-if="i - 1 === current" class="text-xs font-bold">{{ i }}</span>
      </div>
      <!-- Connector line -->
      <div
        v-if="i < total"
        class="h-0.5 w-8 rounded-full transition-all duration-300"
        :class="i - 1 < current ? 'bg-brand-400' : 'bg-neutral-200'"
      />
    </template>
  </div>
</template>
