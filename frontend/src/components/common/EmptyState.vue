<script setup lang="ts">
import { type Component } from 'vue'

export interface EmptyStateProps {
  icon?: Component
  title?: string
  description?: string
  actionLabel?: string
  showAction?: boolean
}

withDefaults(defineProps<EmptyStateProps>(), {
  title: '暂无数据',
  description: '这里暂时没有任何内容',
  actionLabel: '返回首页',
  showAction: false
})

const emit = defineEmits<{
  action: []
}>()

function handleAction() {
  emit('action')
}
</script>

<template>
  <div class="py-16 text-center" role="status" aria-live="polite">
    <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-6">
      <component
        :is="icon"
        v-if="icon"
        :size="32"
        class="text-gray-300"
        aria-hidden="true"
      />
      <div v-else class="w-8 h-8 border-4 border-gray-200 border-t-gray-300 rounded-full animate-spin" aria-hidden="true"></div>
    </div>
    <h3 class="text-2xl font-bold text-gray-900 mb-2">{{ title }}</h3>
    <p class="text-gray-400 max-w-md mx-auto mb-6">{{ description }}</p>
    <button
      v-if="showAction"
      @click="handleAction"
      class="px-6 py-2.5 bg-brand-600 hover:bg-brand-700 text-white rounded-xl font-bold text-sm transition-all focus:ring-2 focus:ring-brand-200 focus:outline-none"
      type="button"
    >
      {{ actionLabel }}
    </button>
  </div>
</template>
