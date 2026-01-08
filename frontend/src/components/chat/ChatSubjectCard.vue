<script setup lang="ts">
import { computed, ref } from 'vue'

type SubjectType = 'SUPPLY' | 'NEED' | string

const props = defineProps<{
  subjectType?: SubjectType
  subjectId?: number | null
  subjectSnapshotJson?: string | null
  isMini?: boolean
}>()

const isExpanded = ref(false)

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
  if (st === 'SUPPLY') return { label: '供应', cls: 'bg-emerald-50 text-emerald-700 border-emerald-100' }
  if (st === 'NEED') return { label: '采购', cls: 'bg-blue-50 text-blue-700 border-blue-200' }
  return { label: '标的', cls: 'bg-gray-50 text-gray-600 border-gray-200' }
})

const title = computed(() => {
  const s = parsed.value || {}
  return s.title || s.categoryName || s.name || `标的 #${props.subjectId ?? '-'}`
})

const price = computed(() => {
  const s = parsed.value || {}
  return s.exFactoryPrice ?? s.expectedPrice ?? s.price
})

const quantity = computed(() => {
  const s = parsed.value || {}
  return s.quantity ?? s.remainingQuantity
})

function pickKV() {
  const s = parsed.value || {}
  const kv: Array<{ k: string; v: string }> = []

  const push = (k: string, v: any) => {
    if (v === undefined || v === null || v === '') return
    kv.push({ k, v: String(v) })
  }

  // 基础信息
  push('发布公司', s.companyName)
  push('单价/意向', price.value ? `¥${price.value} 元/吨` : '面议')
  push('发布数量', quantity.value ? `${quantity.value} 吨` : null)
  push('剩余数量', s.remainingQuantity ? `${s.remainingQuantity} 吨` : null)
  push('产地', s.origin)
  push('发货地', s.shipAddress)
  push('收货地', s.purchaseAddress)
  push('交货方式', s.deliveryMode ?? s.deliveryMethod)
  push('付款方式', s.paymentMethod)
  push('包装方式', s.packaging)
  push('储存方式', s.storageMethod)
  push('开票要求', s.invoiceType)
  
  // 动态参数处理（支持新的 {"Name": "Value"} 格式）
  if (s.paramsJson) {
    try {
      const paramsData = JSON.parse(s.paramsJson)
      // 支持旧格式 { params: {...} } 和新格式 { "Name": "Value" }
      const params = paramsData?.params || paramsData || {}
      Object.entries(params).forEach(([k, v]) => {
        // 跳过纯数字键名（旧格式遗留）
        if (/^\d+$/.test(k)) return
        if (typeof v === 'object' && v !== null && 'name' in v) {
          // 旧格式: { "1": { name: "xxx", value: "yyy" } }
          push((v as any).name, (v as any).value)
        } else if (typeof v === 'string' || typeof v === 'number') {
          // 新格式: { "参数名": "参数值" }
          push(k, v)
        }
      })
    } catch { /* ignore */ }
  }

  // 备注放在最后
  push('备注', s.remark)

  return kv
}

const kvs = computed(() => pickKV())
</script>

<template>
  <!-- Mini 模式：常驻聊天顶部的窄条，支持展开 -->
  <div v-if="isMini" class="relative">
    <div 
      class="bg-white/90 backdrop-blur-md px-4 py-2 border-b flex items-center gap-3 shadow-sm cursor-pointer hover:bg-white transition-colors"
      @click="isExpanded = !isExpanded"
    >
      <div 
        class="w-10 h-10 rounded-lg flex items-center justify-center font-bold shrink-0"
        :class="badge.cls"
      >
        {{ title[0] }}
      </div>
      <div class="flex-1 min-w-0">
        <div class="flex items-center gap-2">
          <span class="text-[10px] font-bold uppercase tracking-widest px-1.5 py-0.5 rounded-full border" :class="badge.cls">
            {{ badge.label }}
          </span>
          <div class="text-sm font-bold text-gray-900 truncate">{{ title }}</div>
        </div>
        <div class="text-[10px] text-gray-500 mt-0.5">
          {{ price ? `意向: ¥${price}` : '价格面议' }} 
          <span v-if="quantity" class="mx-1">·</span>
          {{ quantity ? `数量: ${quantity}吨` : '' }}
          <span class="ml-2 text-emerald-600 font-bold">{{ isExpanded ? '收起详情 ↑' : '查看全部 ↓' }}</span>
        </div>
      </div>
      <slot name="action"></slot>
    </div>

    <!-- 展开的全量信息面板 -->
    <transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="transform -translate-y-2 opacity-0"
      enter-to-class="transform translate-y-0 opacity-100"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="transform translate-y-0 opacity-100"
      leave-to-class="transform -translate-y-2 opacity-0"
    >
      <div v-if="isExpanded" class="absolute top-full left-0 right-0 z-20 bg-white border-b shadow-xl max-h-[60vh] overflow-y-auto p-4">
        <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
          <div v-for="item in kvs" :key="item.k" class="bg-gray-50 rounded-xl border border-gray-100 px-3 py-2">
            <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">{{ item.k }}</div>
            <div class="mt-0.5 text-sm font-semibold text-gray-800 truncate" :title="item.v">{{ item.v }}</div>
          </div>
        </div>
        <div class="mt-4 flex justify-center">
          <button @click="isExpanded = false" class="text-xs text-gray-400 hover:text-gray-600 font-bold">收起全部信息 ↑</button>
        </div>
      </div>
    </transition>
  </div>

  <!-- 原有的卡片模式 (用于弹窗或其他静态展示) -->
  <div v-else class="bg-white rounded-2xl border border-gray-100 p-4">
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

    <div v-if="kvs.length" class="mt-3 grid grid-cols-2 sm:grid-cols-3 gap-2">
      <div v-for="item in kvs" :key="item.k" class="bg-gray-50 rounded-xl border border-gray-100 px-3 py-2">
        <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">{{ item.k }}</div>
        <div class="mt-0.5 text-sm font-semibold text-gray-800 truncate" :title="item.v">{{ item.v }}</div>
      </div>
    </div>
  </div>
</template>


