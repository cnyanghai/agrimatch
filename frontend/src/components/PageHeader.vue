<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
}>()
</script>

<template>
  <div class="bg-white rounded-xl shadow-card border border-gray-100 px-5 py-4 flex items-center justify-between gap-3">
    <div class="min-w-0">
      <div class="text-xl font-bold text-gray-800 truncate">{{ title }}</div>
      <div v-if="subtitle" class="text-sm text-gray-500 mt-0.5 truncate">{{ subtitle }}</div>
    </div>
    <div class="flex items-center gap-2 flex-shrink-0">
      <slot name="right" />
    </div>
  </div>
</template>

<style scoped></style>


