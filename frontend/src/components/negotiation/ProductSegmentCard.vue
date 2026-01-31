<script setup lang="ts">
/**
 * ProductSegmentCard - 产品分段卡片
 * 在合并聊天面板中用于分隔不同产品的会话消息
 */
import { computed } from 'vue'
import { Package, ArrowUpDown, Banknote } from 'lucide-vue-next'
import type { ConversationItem } from '../../composables/useNegotiationWorkspace'

const props = defineProps<{
  conversation: ConversationItem
  active: boolean
  messagesCount: number
}>()

defineEmits<{
  (e: 'click'): void
}>()

/** 解析标的快照 */
const snapshot = computed(() => {
  if (!props.conversation.subjectSnapshotJson) return null
  try {
    return JSON.parse(props.conversation.subjectSnapshotJson)
  } catch {
    return null
  }
})

const productName = computed(() => snapshot.value?.productName || snapshot.value?.title || '产品')

const price = computed(() => {
  const s = snapshot.value
  if (!s) return ''
  const p = s.price || s.exFactoryPrice || s.expectedPrice
  return p ? `¥${p}` : ''
})

const quantity = computed(() => {
  const s = snapshot.value
  if (!s) return ''
  const q = s.quantity || s.remainingQuantity
  return q ? `${q}吨` : ''
})

const typeLabel = computed(() => {
  return props.conversation.subjectType === 'SUPPLY' ? '供应' : '需求'
})

const typeBgColor = computed(() => {
  return props.conversation.subjectType === 'SUPPLY'
    ? 'bg-brand-100 text-brand-700'
    : 'bg-autumn-100 text-autumn-700'
})
</script>

<template>
  <div
    :class="[
      'flex items-center gap-2.5 px-3 py-2 rounded-lg border cursor-pointer transition-all duration-200 select-none',
      active
        ? 'bg-brand-50 border-brand-300 shadow-sm'
        : 'bg-white border-neutral-200 hover:bg-neutral-50 hover:border-neutral-300'
    ]"
    @click="$emit('click')"
  >
    <!-- 产品图标 -->
    <div
      :class="[
        'w-8 h-8 rounded-lg flex items-center justify-center shrink-0',
        active ? 'bg-brand-500 text-white' : 'bg-neutral-100 text-neutral-500'
      ]"
    >
      <Package class="w-4 h-4" />
    </div>

    <!-- 产品信息 -->
    <div class="flex-1 min-w-0">
      <div class="flex items-center gap-1.5">
        <span class="text-sm font-bold text-neutral-900 truncate">{{ productName }}</span>
        <span :class="['text-[10px] px-1.5 py-0.5 rounded-full font-medium shrink-0', typeBgColor]">
          {{ typeLabel }}
        </span>
        <span
          v-if="active"
          class="text-[10px] px-1.5 py-0.5 rounded-full bg-brand-500 text-white font-medium shrink-0"
        >
          议价中
        </span>
      </div>
      <div class="flex items-center gap-2 mt-0.5 text-[11px] text-neutral-500">
        <span v-if="price" class="flex items-center gap-0.5">
          <Banknote class="w-3 h-3" />{{ price }}
        </span>
        <span v-if="quantity" class="flex items-center gap-0.5">
          <ArrowUpDown class="w-3 h-3" />{{ quantity }}
        </span>
        <span class="text-neutral-400">{{ messagesCount }}条消息</span>
      </div>
    </div>
  </div>
</template>
