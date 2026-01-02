<script setup lang="ts">
import { computed } from 'vue'

type SubjectType = 'SUPPLY' | 'NEED' | string

const props = defineProps<{
  subjectType?: SubjectType
  subjectId?: number | null
  subjectSnapshotJson?: string | null
}>()

const parsed = computed<Record<string, any> | null>(() => {
  if (!props.subjectSnapshotJson) return null
  try {
    return JSON.parse(props.subjectSnapshotJson)
  } catch {
    return null
  }
})

const badge = computed(() => {
  const st = (props.subjectType || '').toUpperCase()
  if (st === 'SUPPLY') return { label: '供应标的', cls: 'bg-emerald-50 text-emerald-700 border-emerald-100' }
  if (st === 'NEED') return { label: '采购标的', cls: 'bg-blue-50 text-blue-700 border-blue-200' }
  return { label: '标的', cls: 'bg-gray-50 text-gray-600 border-gray-200' }
})

const title = computed(() => {
  const s = parsed.value || {}
  return s.title || s.categoryName || s.name || `标的 #${props.subjectId ?? '-'}`
})

function pickKV() {
  const s = parsed.value || {}
  const kv: Array<{ k: string; v: string }> = []

  const push = (k: string, v: any) => {
    if (v === undefined || v === null || v === '') return
    kv.push({ k, v: String(v) })
  }

  push('数量', s.quantity ?? s.remainingQuantity)
  push('价格', s.exFactoryPrice ?? s.expectedPrice ?? s.price)
  push('产地', s.origin)
  push('发货地', s.shipAddress)
  push('收货地', s.purchaseAddress)
  push('交付', s.deliveryMode ?? s.deliveryMethod)
  push('结算', s.paymentMethod)

  return kv.slice(0, 6)
}

const kvs = computed(() => pickKV())
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-100 p-4">
    <div class="flex items-start justify-between gap-4">
      <div class="min-w-0">
        <div class="flex items-center gap-2">
          <span class="text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 rounded-full border" :class="badge.cls">
            {{ badge.label }}
          </span>
          <span class="text-xs text-gray-400">ID：{{ subjectId ?? '-' }}</span>
        </div>
        <div class="mt-2 font-bold text-gray-900 truncate">
          {{ title }}
        </div>
      </div>
      <div class="text-right shrink-0" v-if="parsed?.snapshotTime">
        <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">快照</div>
        <div class="text-xs text-gray-500">{{ parsed.snapshotTime }}</div>
      </div>
    </div>

    <div v-if="kvs.length" class="mt-3 grid grid-cols-2 gap-2">
      <div v-for="item in kvs" :key="item.k" class="bg-gray-50 rounded-xl border border-gray-100 px-3 py-2">
        <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">{{ item.k }}</div>
        <div class="mt-0.5 text-sm font-semibold text-gray-800 truncate">{{ item.v }}</div>
      </div>
    </div>
  </div>
</template>


