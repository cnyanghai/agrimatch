<script setup lang="ts">
import { computed } from 'vue'
import { CheckCircle2, Clock3, FileText, Hourglass, XCircle } from 'lucide-vue-next'
import type { ContractStatus } from '../../api/contract'

const props = defineProps<{
  status: ContractStatus
}>()

const config = computed(() => {
  const base = 'inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full border text-[10px] font-bold'
  switch (props.status) {
    case 'signed':
      return { label: '已签署', cls: `${base} bg-emerald-50 text-emerald-700 border-emerald-100`, Icon: CheckCircle2 }
    case 'pending':
      return { label: '待签署', cls: `${base} bg-amber-50 text-amber-700 border-amber-100`, Icon: Hourglass }
    case 'executing':
      return { label: '履行中', cls: `${base} bg-blue-50 text-blue-700 border-blue-100`, Icon: Clock3 }
    case 'completed':
      return { label: '已完成', cls: `${base} bg-gray-50 text-gray-700 border-gray-200`, Icon: CheckCircle2 }
    case 'cancelled':
      return { label: '已取消', cls: `${base} bg-red-50 text-red-700 border-red-100`, Icon: XCircle }
    case 'draft':
    default:
      return { label: '草稿', cls: `${base} bg-slate-50 text-slate-600 border-slate-100`, Icon: FileText }
  }
})
</script>

<template>
  <span :class="config.cls">
    <component :is="config.Icon" :size="12" :stroke-width="2" />
    {{ config.label }}
  </span>
</template>


