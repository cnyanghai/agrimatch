<script setup lang="ts">
/**
 * Quote Timeline Component
 * Displays history of quotes in a conversation
 */
import { computed } from 'vue'
import { Check, X, Clock, Send, ArrowRight } from 'lucide-vue-next'
import type { UiMessage, QuoteStatus } from '../../../types/chat/message'
import { QUOTE_STATUS_BADGE } from '../../../types/chat/message'
import { parseQuotePayload, isBasisQuote, buildQuoteSummary, getPriceChangeSummary } from '../../../utils/chat/quoteParser'

const props = defineProps<{
  quotes: UiMessage[]
  currentUserId?: number
}>()

const emit = defineEmits<{
  (e: 'select', message: UiMessage): void
}>()

interface TimelineItem {
  message: UiMessage
  summary: string
  isSent: boolean
  status: QuoteStatus | undefined
  time: string
  priceChange?: string
  isBasis: boolean
}

const timelineItems = computed<TimelineItem[]>(() => {
  return props.quotes.map((msg, index) => {
    const payload = parseQuotePayload(msg.payloadJson)
    const summary = payload ? buildQuoteSummary(payload) : msg.content

    // 与上一个报价比较
    let priceChange: string | undefined
    if (index > 0) {
      const prevPayload = parseQuotePayload(props.quotes[index - 1]?.payloadJson)
      priceChange = getPriceChangeSummary(payload, prevPayload) || undefined
    }

    return {
      message: msg,
      summary,
      isSent: msg.type === 'sent',
      status: msg.quoteStatus as QuoteStatus | undefined,
      time: msg.time || '',
      priceChange,
      isBasis: isBasisQuote(payload)
    }
  })
})

function getStatusIcon(status?: QuoteStatus) {
  switch (status) {
    case 'ACCEPTED': return Check
    case 'REJECTED': return X
    case 'EXPIRED': return Clock
    default: return Send
  }
}

function getStatusColor(status?: QuoteStatus) {
  switch (status) {
    case 'ACCEPTED': return 'bg-brand-500 text-white'
    case 'REJECTED': return 'bg-red-500 text-white'
    case 'EXPIRED': return 'bg-gray-400 text-white'
    default: return 'bg-amber-500 text-white'
  }
}

function formatTime(time: string): string {
  return time || ''
}
</script>

<template>
  <div class="relative">
    <!-- 时间线竖线 -->
    <div class="absolute left-5 top-0 bottom-0 w-0.5 bg-gray-200"></div>

    <!-- 时间线项目 -->
    <div
      v-for="(item, index) in timelineItems"
      :key="item.message.id"
      class="relative pl-12 pb-6 last:pb-0"
    >
      <!-- 节点 -->
      <div
        :class="[
          'absolute left-3 w-4 h-4 rounded-full flex items-center justify-center',
          getStatusColor(item.status)
        ]"
      >
        <component :is="getStatusIcon(item.status)" class="w-2.5 h-2.5" />
      </div>

      <!-- 内容卡片 -->
      <div
        @click="emit('select', item.message)"
        :class="[
          'bg-white border rounded-xl p-3 cursor-pointer transition-all hover:shadow-md',
          item.isSent ? 'border-brand-200 bg-brand-50/30' : 'border-gray-200'
        ]"
      >
        <!-- 头部 -->
        <div class="flex items-center justify-between mb-2">
          <div class="flex items-center gap-2">
            <span :class="['text-xs font-bold', item.isSent ? 'text-brand-600' : 'text-gray-600']">
              {{ item.isSent ? '您的报价' : '对方报价' }}
            </span>
            <span
              v-if="item.isBasis"
              class="px-1.5 py-0.5 text-[9px] font-bold bg-slate-100 text-slate-600 rounded"
            >
              基差
            </span>
            <div
              v-if="item.status && QUOTE_STATUS_BADGE[item.status]"
              :class="[
                'px-1.5 py-0.5 rounded text-[9px] font-bold',
                QUOTE_STATUS_BADGE[item.status].bgColor,
                QUOTE_STATUS_BADGE[item.status].color
              ]"
            >
              {{ QUOTE_STATUS_BADGE[item.status].label }}
            </div>
          </div>
          <span class="text-[10px] text-gray-400">{{ formatTime(item.time) }}</span>
        </div>

        <!-- 摘要 -->
        <div class="text-sm font-medium text-gray-900">{{ item.summary }}</div>

        <!-- 价格变更 -->
        <div v-if="item.priceChange" class="mt-2 flex items-center gap-1 text-xs text-amber-600">
          <ArrowRight class="w-3 h-3" />
          <span>{{ item.priceChange }}</span>
        </div>
      </div>

      <!-- 连接线箭头（非最后一个） -->
      <div
        v-if="index < timelineItems.length - 1"
        class="absolute left-[18px] top-8 h-4 flex items-center"
      >
        <svg class="w-2 h-4 text-gray-300" viewBox="0 0 8 16" fill="currentColor">
          <path d="M0 0 L4 8 L0 16 L0 0" />
        </svg>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="timelineItems.length === 0" class="text-center py-8 text-gray-400 text-sm">
      暂无报价记录
    </div>
  </div>
</template>
