<script setup lang="ts">
/**
 * Enhanced Quote Card Component
 * Displays quote messages with improved UX following B2B best practices
 */
import { computed, ref } from 'vue'
import { Check, X, Edit, Clock, FileText, TrendingUp, TrendingDown } from 'lucide-vue-next'
import type { UiMessage, QuoteStatus } from '../../../types/chat/message'
import { QUOTE_STATUS_BADGE } from '../../../types/chat/message'
import {
  parseQuotePayload,
  getQuoteDisplayFields,
  isBasisQuote,
  getQuoteRemainingTime,
  calculateQuoteDiff
} from '../../../utils/chat/quoteParser'
import type { QuoteDisplayField, QuoteFieldDiff } from '../../../types/chat/quote'
import QuoteDiffBadge from './QuoteDiffBadge.vue'
import QuoteValidityTimer from '../quote/QuoteValidityTimer.vue'

const props = defineProps<{
  message: UiMessage
  previousQuotePayloadJson?: string
  subjectName?: string
  subjectLocation?: string
}>()

const emit = defineEmits<{
  (e: 'accept'): void
  (e: 'counter'): void
  (e: 'reject'): void
  (e: 'draft-contract'): void
  (e: 'view-timeline'): void
}>()

const payload = computed(() => parseQuotePayload(props.message.payloadJson))
const isBasis = computed(() => isBasisQuote(payload.value))
const displayFields = computed(() => getQuoteDisplayFields(props.message.payloadJson))

const isSent = computed(() => props.message.type === 'sent')
const status = computed(() => props.message.quoteStatus as QuoteStatus | undefined)
const statusBadge = computed(() => status.value ? QUOTE_STATUS_BADGE[status.value] : null)

// 有效期相关
const remainingTime = computed(() => getQuoteRemainingTime(payload.value))
const hasExpiry = computed(() => remainingTime.value !== null)

// 差异计算
const previousPayload = computed(() => parseQuotePayload(props.previousQuotePayloadJson))
const diffs = computed<QuoteFieldDiff[]>(() => {
  if (!payload.value || !previousPayload.value) return []
  return calculateQuoteDiff(payload.value, previousPayload.value)
})
const hasDiffs = computed(() => diffs.value.length > 0)

// 核心字段提取
const coreFields = computed(() => {
  const fields: Array<{ label: string; value: string; highlight?: boolean }> = []

  if (isBasis.value && payload.value?.kind === 'BASIS_QUOTE_V1') {
    const f = (payload.value as any).fields
    if (f.basisPrice !== undefined) {
      const prefix = f.basisPrice > 0 ? '+' : ''
      fields.push({
        label: '基差',
        value: `${prefix}${f.basisPrice}`,
        highlight: true
      })
    }
    if (f.referencePrice) {
      fields.push({
        label: '核算价',
        value: `¥${f.referencePrice.toFixed(2)}`,
        highlight: true
      })
    }
    if (f.quantity) {
      fields.push({ label: '数量', value: f.quantity })
    }
  } else if (payload.value?.kind === 'QUOTE_V1') {
    const f = (payload.value as any).fields
    if (f.price) {
      fields.push({
        label: '单价',
        value: `¥${f.price}/吨`,
        highlight: true
      })
    }
    if (f.quantity) {
      fields.push({
        label: '数量',
        value: f.quantity,
        highlight: true
      })
    }
  }

  return fields
})

// 次要字段
const secondaryFields = computed(() => {
  return displayFields.value.filter(f =>
    !['price', 'quantity', 'basisPrice', 'referencePrice'].includes(f.key)
  )
})

// 可操作状态
const canAccept = computed(() => !isSent.value && status.value === 'OFFERED')
const canDraftContract = computed(() => status.value === 'ACCEPTED')

// 展开/收起
const expanded = ref(false)
</script>

<template>
  <div
    :class="[
      'rounded-2xl overflow-hidden shadow-lg transition-all duration-300',
      isSent
        ? 'bg-gradient-to-br from-brand-600 to-brand-700'
        : 'bg-white border border-gray-200'
    ]"
    style="max-width: 360px;"
  >
    <!-- 头部状态栏 -->
    <div
      :class="[
        'px-4 py-2.5 flex items-center justify-between',
        isSent ? 'bg-brand-700/50' : 'bg-gray-50 border-b border-gray-100'
      ]"
    >
      <div class="flex items-center gap-2">
        <!-- 状态徽章 -->
        <div
          v-if="statusBadge"
          :class="[
            'px-2.5 py-1 rounded-full text-[10px] font-bold flex items-center gap-1',
            isSent ? 'bg-white/20 text-white' : `${statusBadge.bgColor} ${statusBadge.color}`
          ]"
        >
          <Check v-if="status === 'ACCEPTED'" class="w-3 h-3" />
          <X v-else-if="status === 'REJECTED'" class="w-3 h-3" />
          <Clock v-else-if="status === 'EXPIRED'" class="w-3 h-3" />
          {{ statusBadge.label }}
        </div>
      </div>

      <!-- 有效期倒计时 -->
      <QuoteValidityTimer
        v-if="hasExpiry && status === 'OFFERED'"
        :remaining-ms="remainingTime!"
        :class="isSent ? 'text-white/80' : 'text-gray-500'"
      />
    </div>

    <!-- 产品信息 -->
    <div v-if="subjectName" :class="['px-4 py-3 border-b', isSent ? 'border-brand-500/30' : 'border-gray-100']">
      <div :class="['font-bold', isSent ? 'text-white' : 'text-gray-900']">
        {{ subjectName }}
      </div>
      <div v-if="subjectLocation" :class="['text-xs mt-0.5', isSent ? 'text-brand-100' : 'text-gray-500']">
        {{ subjectLocation }}
      </div>
    </div>

    <!-- 核心字段 -->
    <div class="px-4 py-4">
      <div class="flex gap-3">
        <div
          v-for="field in coreFields"
          :key="field.label"
          :class="[
            'flex-1 rounded-xl p-3 text-center',
            isSent
              ? 'bg-white/10'
              : field.highlight ? 'bg-brand-50' : 'bg-gray-50'
          ]"
        >
          <div :class="['text-[10px] font-medium uppercase tracking-wider mb-1', isSent ? 'text-brand-100' : 'text-gray-400']">
            {{ field.label }}
          </div>
          <div :class="['text-lg font-bold', isSent ? 'text-white' : 'text-brand-700']">
            {{ field.value }}
          </div>
        </div>
      </div>
    </div>

    <!-- 变更差异 -->
    <div v-if="hasDiffs" :class="['px-4 pb-3']">
      <QuoteDiffBadge :diffs="diffs" :is-sent="isSent" />
    </div>

    <!-- 次要字段（可展开） -->
    <div v-if="secondaryFields.length > 0" :class="['border-t', isSent ? 'border-brand-500/30' : 'border-gray-100']">
      <button
        @click="expanded = !expanded"
        :class="[
          'w-full px-4 py-2 text-xs font-medium flex items-center justify-center gap-1 transition-colors',
          isSent ? 'text-brand-100 hover:bg-white/5' : 'text-gray-500 hover:bg-gray-50'
        ]"
      >
        {{ expanded ? '收起详情' : '展开详情' }}
        <svg
          :class="['w-4 h-4 transition-transform', expanded ? 'rotate-180' : '']"
          fill="none" viewBox="0 0 24 24" stroke="currentColor"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
        </svg>
      </button>

      <div
        v-show="expanded"
        :class="['px-4 pb-4 space-y-2', isSent ? 'bg-brand-700/30' : 'bg-gray-50']"
      >
        <div
          v-for="field in secondaryFields"
          :key="field.key"
          class="flex justify-between text-xs"
        >
          <span :class="isSent ? 'text-brand-100/70' : 'text-gray-400'">{{ field.label }}</span>
          <span :class="isSent ? 'text-white' : 'text-gray-700'">{{ field.value }}</span>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div
      v-if="canAccept || canDraftContract"
      :class="['px-4 py-3 border-t flex gap-2', isSent ? 'border-brand-500/30' : 'border-gray-100']"
    >
      <template v-if="canAccept">
        <button
          @click="emit('accept')"
          class="flex-1 px-4 py-2.5 bg-brand-600 text-white text-sm font-bold rounded-xl hover:bg-brand-700 transition-all flex items-center justify-center gap-1.5 active:scale-95"
        >
          <Check class="w-4 h-4" />
          接受报价
        </button>
        <button
          @click="emit('counter')"
          class="px-4 py-2.5 bg-gray-100 text-gray-700 text-sm font-medium rounded-xl hover:bg-gray-200 transition-all flex items-center justify-center gap-1.5 active:scale-95"
        >
          <Edit class="w-4 h-4" />
          还价
        </button>
        <button
          @click="emit('reject')"
          class="px-3 py-2.5 text-gray-400 hover:text-red-500 transition-colors"
        >
          <X class="w-5 h-5" />
        </button>
      </template>

      <template v-if="canDraftContract">
        <button
          @click="emit('draft-contract')"
          class="flex-1 px-4 py-2.5 bg-brand-50 text-brand-700 text-sm font-bold rounded-xl hover:bg-brand-100 transition-all flex items-center justify-center gap-1.5 active:scale-95"
        >
          <FileText class="w-4 h-4" />
          起草合同
        </button>
      </template>
    </div>

    <!-- 时间戳 -->
    <div :class="['px-4 py-2 text-[10px]', isSent ? 'text-brand-200 text-right' : 'text-gray-400']">
      {{ message.time }}
    </div>
  </div>
</template>
