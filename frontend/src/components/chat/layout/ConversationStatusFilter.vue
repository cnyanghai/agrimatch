<script setup lang="ts">
import { computed } from 'vue'
import { MessageCircle, Clock, CheckCircle, Pause, XCircle, List } from 'lucide-vue-next'
import type { ConversationBusinessStatus } from '../../../types/chat/conversation'

const props = defineProps<{
  activeStatus: ConversationBusinessStatus | 'ALL'
  statusCounts: Record<ConversationBusinessStatus | 'ALL', number>
}>()

const emit = defineEmits<{
  (e: 'update:activeStatus', status: ConversationBusinessStatus | 'ALL'): void
}>()

const statusOptions = computed(() => [
  {
    value: 'ALL' as const,
    label: '全部',
    icon: List,
    color: 'text-gray-600',
    bgColor: 'bg-gray-100',
    activeColor: 'bg-gray-600',
    count: props.statusCounts.ALL
  },
  {
    value: 'ACTIVE' as const,
    label: '活跃',
    icon: MessageCircle,
    color: 'text-brand-600',
    bgColor: 'bg-brand-50',
    activeColor: 'bg-brand-600',
    count: props.statusCounts.ACTIVE
  },
  {
    value: 'PENDING' as const,
    label: '待跟进',
    icon: Clock,
    color: 'text-amber-600',
    bgColor: 'bg-amber-50',
    activeColor: 'bg-amber-500',
    count: props.statusCounts.PENDING
  },
  {
    value: 'COMPLETED' as const,
    label: '已成交',
    icon: CheckCircle,
    color: 'text-brand-700',
    bgColor: 'bg-brand-100',
    activeColor: 'bg-brand-700',
    count: props.statusCounts.COMPLETED
  },
  {
    value: 'ON_HOLD' as const,
    label: '暂缓',
    icon: Pause,
    color: 'text-gray-500',
    bgColor: 'bg-gray-100',
    activeColor: 'bg-gray-500',
    count: props.statusCounts.ON_HOLD
  },
  {
    value: 'CLOSED' as const,
    label: '关闭',
    icon: XCircle,
    color: 'text-red-500',
    bgColor: 'bg-red-50',
    activeColor: 'bg-red-500',
    count: props.statusCounts.CLOSED
  }
])

function selectStatus(status: ConversationBusinessStatus | 'ALL') {
  emit('update:activeStatus', status)
}
</script>

<template>
  <div class="flex items-center gap-1 overflow-x-auto pb-2 scrollbar-hide">
    <button
      v-for="opt in statusOptions"
      :key="opt.value"
      @click="selectStatus(opt.value)"
      :class="[
        'flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg text-xs font-medium whitespace-nowrap transition-all',
        activeStatus === opt.value
          ? `${opt.activeColor} text-white shadow-sm`
          : `${opt.bgColor} ${opt.color} hover:opacity-80`
      ]"
    >
      <component :is="opt.icon" class="w-3.5 h-3.5" />
      <span>{{ opt.label }}</span>
      <span
        v-if="opt.count > 0"
        :class="[
          'px-1.5 py-0.5 text-[10px] font-bold rounded-full',
          activeStatus === opt.value
            ? 'bg-white/20 text-white'
            : 'bg-white/60 text-inherit'
        ]"
      >
        {{ opt.count > 99 ? '99+' : opt.count }}
      </span>
    </button>
  </div>
</template>

<style scoped>
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
</style>
