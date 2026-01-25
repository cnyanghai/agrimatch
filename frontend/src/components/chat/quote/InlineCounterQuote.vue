<script setup lang="ts">
/**
 * Inline Counter-Quote Component
 * Displays an expandable form for quickly adjusting quote prices
 * Features quick percentage buttons (-5%, -3%, -1%, +1%, +3%, +5%)
 */
import { ref, computed, watch } from 'vue'
import { X, Send, TrendingUp, TrendingDown, Percent } from 'lucide-vue-next'
import type { QuotePayload, QuoteDisplayField } from '../../../types/chat/quote'

const props = defineProps<{
  /** 原始报价的解析字段 */
  originalFields: QuoteDisplayField[]
  /** 原始价格 */
  originalPrice?: number
  /** 原始数量 */
  originalQuantity?: string
  /** 是否为基差报价 */
  isBasis?: boolean
  /** 原始基差价格 */
  originalBasisPrice?: number
  /** 是否正在发送 */
  sending?: boolean
}>()

const emit = defineEmits<{
  (e: 'submit', payload: { price?: number; basisPrice?: number; quantity?: string; remark?: string }): void
  (e: 'cancel'): void
}>()

// 调整后的价格
const adjustedPrice = ref<number | null>(null)
const adjustedBasisPrice = ref<number | null>(null)
const adjustedQuantity = ref<string>('')
const remark = ref<string>('')

// 初始化
watch(() => props.originalPrice, (val) => {
  if (val !== undefined) adjustedPrice.value = val
}, { immediate: true })

watch(() => props.originalBasisPrice, (val) => {
  if (val !== undefined) adjustedBasisPrice.value = val
}, { immediate: true })

watch(() => props.originalQuantity, (val) => {
  if (val) adjustedQuantity.value = val
}, { immediate: true })

// 快捷百分比按钮
const quickPercentages = [
  { label: '-5%', value: -0.05, color: 'text-red-600 bg-red-50 hover:bg-red-100' },
  { label: '-3%', value: -0.03, color: 'text-red-500 bg-red-50 hover:bg-red-100' },
  { label: '-1%', value: -0.01, color: 'text-orange-500 bg-orange-50 hover:bg-orange-100' },
  { label: '+1%', value: 0.01, color: 'text-brand-500 bg-brand-50 hover:bg-brand-100' },
  { label: '+3%', value: 0.03, color: 'text-brand-600 bg-brand-50 hover:bg-brand-100' },
  { label: '+5%', value: 0.05, color: 'text-brand-700 bg-brand-50 hover:bg-brand-100' }
]

// 应用百分比调整
function applyPercentage(pct: number) {
  if (props.isBasis && adjustedBasisPrice.value !== null) {
    // 基差调整：直接加减（基差是固定值，不是百分比）
    const adjustment = Math.round(props.originalBasisPrice! * pct)
    adjustedBasisPrice.value = (props.originalBasisPrice || 0) + adjustment
  } else if (adjustedPrice.value !== null) {
    // 现货价格调整：百分比
    adjustedPrice.value = Math.round((props.originalPrice || 0) * (1 + pct) * 100) / 100
  }
}

// 价格变化
const priceChange = computed(() => {
  if (props.isBasis) {
    if (adjustedBasisPrice.value === null || props.originalBasisPrice === undefined) return null
    return adjustedBasisPrice.value - props.originalBasisPrice
  } else {
    if (adjustedPrice.value === null || props.originalPrice === undefined) return null
    return adjustedPrice.value - props.originalPrice
  }
})

const priceChangePercent = computed(() => {
  if (props.isBasis) {
    // 基差不显示百分比
    return null
  }
  if (priceChange.value === null || !props.originalPrice) return null
  return (priceChange.value / props.originalPrice * 100).toFixed(1)
})

// 是否有变化
const hasChanges = computed(() => {
  if (props.isBasis) {
    return adjustedBasisPrice.value !== props.originalBasisPrice ||
           adjustedQuantity.value !== props.originalQuantity
  }
  return adjustedPrice.value !== props.originalPrice ||
         adjustedQuantity.value !== props.originalQuantity
})

// 提交
function handleSubmit() {
  const payload: { price?: number; basisPrice?: number; quantity?: string; remark?: string } = {}

  if (props.isBasis) {
    if (adjustedBasisPrice.value !== null) {
      payload.basisPrice = adjustedBasisPrice.value
    }
  } else {
    if (adjustedPrice.value !== null) {
      payload.price = adjustedPrice.value
    }
  }

  if (adjustedQuantity.value) {
    payload.quantity = adjustedQuantity.value
  }

  if (remark.value.trim()) {
    payload.remark = remark.value.trim()
  }

  emit('submit', payload)
}

// 重置
function resetToOriginal() {
  adjustedPrice.value = props.originalPrice ?? null
  adjustedBasisPrice.value = props.originalBasisPrice ?? null
  adjustedQuantity.value = props.originalQuantity || ''
  remark.value = ''
}
</script>

<template>
  <div class="bg-gray-50 rounded-xl border border-gray-200 overflow-hidden">
    <!-- 头部 -->
    <div class="px-4 py-3 bg-white border-b border-gray-100 flex items-center justify-between">
      <div class="flex items-center gap-2">
        <Percent class="w-4 h-4 text-brand-600" />
        <span class="text-sm font-bold text-gray-900">快速还价</span>
      </div>
      <button
        @click="emit('cancel')"
        class="p-1 rounded-lg hover:bg-gray-100 text-gray-400 hover:text-gray-600 transition-colors"
      >
        <X class="w-4 h-4" />
      </button>
    </div>

    <!-- 快捷百分比按钮 -->
    <div class="px-4 py-3 border-b border-gray-100">
      <div class="text-[10px] font-medium text-gray-500 uppercase tracking-wider mb-2">快捷调整</div>
      <div class="flex flex-wrap gap-2">
        <button
          v-for="pct in quickPercentages"
          :key="pct.label"
          @click="applyPercentage(pct.value)"
          :class="[
            'px-3 py-1.5 text-xs font-bold rounded-lg transition-all active:scale-95',
            pct.color
          ]"
        >
          {{ pct.label }}
        </button>
        <button
          @click="resetToOriginal"
          class="px-3 py-1.5 text-xs font-medium text-gray-500 bg-gray-100 rounded-lg hover:bg-gray-200 transition-all"
        >
          重置
        </button>
      </div>
    </div>

    <!-- 调整后的值 -->
    <div class="px-4 py-3 space-y-3">
      <!-- 价格/基差输入 -->
      <div>
        <label class="text-[10px] font-medium text-gray-500 uppercase tracking-wider">
          {{ isBasis ? '基差价格' : '单价 (元/吨)' }}
        </label>
        <div class="mt-1 flex items-center gap-2">
          <input
            v-if="isBasis"
            v-model.number="adjustedBasisPrice"
            type="number"
            class="flex-1 px-3 py-2 text-sm border border-gray-200 rounded-lg focus:border-brand-500 focus:ring-1 focus:ring-brand-500/20 outline-none"
            :placeholder="String(originalBasisPrice || 0)"
          />
          <input
            v-else
            v-model.number="adjustedPrice"
            type="number"
            step="0.01"
            class="flex-1 px-3 py-2 text-sm border border-gray-200 rounded-lg focus:border-brand-500 focus:ring-1 focus:ring-brand-500/20 outline-none"
            :placeholder="String(originalPrice || 0)"
          />
          <!-- 变化指示 -->
          <div
            v-if="priceChange !== null && priceChange !== 0"
            :class="[
              'flex items-center gap-1 px-2 py-1 rounded-lg text-xs font-bold',
              priceChange > 0 ? 'bg-brand-50 text-brand-600' : 'bg-red-50 text-red-600'
            ]"
          >
            <component :is="priceChange > 0 ? TrendingUp : TrendingDown" class="w-3 h-3" />
            <span>{{ priceChange > 0 ? '+' : '' }}{{ priceChange }}</span>
            <span v-if="priceChangePercent">({{ priceChangePercent }}%)</span>
          </div>
        </div>
      </div>

      <!-- 数量输入 -->
      <div>
        <label class="text-[10px] font-medium text-gray-500 uppercase tracking-wider">数量</label>
        <input
          v-model="adjustedQuantity"
          type="text"
          class="mt-1 w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:border-brand-500 focus:ring-1 focus:ring-brand-500/20 outline-none"
          :placeholder="originalQuantity || '输入数量'"
        />
      </div>

      <!-- 备注输入 -->
      <div>
        <label class="text-[10px] font-medium text-gray-500 uppercase tracking-wider">备注（可选）</label>
        <textarea
          v-model="remark"
          rows="2"
          class="mt-1 w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:border-brand-500 focus:ring-1 focus:ring-brand-500/20 outline-none resize-none"
          placeholder="添加备注说明..."
        />
      </div>
    </div>

    <!-- 底部操作 -->
    <div class="px-4 py-3 bg-white border-t border-gray-100 flex items-center justify-end gap-2">
      <button
        @click="emit('cancel')"
        class="px-4 py-2 text-sm font-medium text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
      >
        取消
      </button>
      <button
        @click="handleSubmit"
        :disabled="!hasChanges || sending"
        class="px-4 py-2 text-sm font-bold text-white bg-brand-600 rounded-lg hover:bg-brand-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all flex items-center gap-1.5"
      >
        <Send v-if="!sending" class="w-4 h-4" />
        <span v-if="sending">发送中...</span>
        <span v-else>发送还价</span>
      </button>
    </div>
  </div>
</template>
