<script setup lang="ts">
import { computed } from 'vue'
import { Document, Loading, Check, Close, Warning } from '@element-plus/icons-vue'
import type { UiMessage, QuoteStatus } from '../../../types/chat/message'
import { QUOTE_STATUS_BADGE } from '../../../types/chat/message'
import { getQuoteDisplayFields, isBasisQuote, parseQuotePayload } from '../../../utils/chat/quoteParser'
import QuoteCard from './QuoteCard.vue'
import { ContractFlowCard } from '../contract'
import { formatFileSize } from '../../../api/file'

const props = defineProps<{
  message: UiMessage
  previousQuotePayloadJson?: string
  subjectName?: string
  subjectLocation?: string
  currentCompanyId?: number
}>()

const emit = defineEmits<{
  (e: 'confirm-quote', messageId: number): void
  (e: 'reject-quote', messageId: number): void
  (e: 'counter-quote', messageId: number): void
  (e: 'open-draft-contract', messageId: number): void
  (e: 'view-contract', contractId: number): void
  (e: 'sign-contract', contractId: number): void
  (e: 'preview-image', url: string): void
  (e: 'download-attachment', url: string, fileName: string): void
}>()

const isSent = computed(() => props.message.type === 'sent')
const isSystem = computed(() => props.message.type === 'system')
const msgType = computed(() => (props.message.msgType || 'TEXT').toUpperCase())

const quoteStatus = computed(() => props.message.quoteStatus as QuoteStatus | undefined)
const quoteStatusBadge = computed(() => {
  if (!quoteStatus.value) return null
  return QUOTE_STATUS_BADGE[quoteStatus.value]
})

const quoteDisplayFields = computed(() => {
  if (msgType.value !== 'QUOTE') return []
  return getQuoteDisplayFields(props.message.payloadJson)
})

const isQuoteBasis = computed(() => {
  if (msgType.value !== 'QUOTE') return false
  const payload = parseQuotePayload(props.message.payloadJson)
  return isBasisQuote(payload)
})

// 可以确认的报价条件：收到的、待确认状态
const canConfirmQuote = computed(() => {
  return !isSent.value &&
    msgType.value === 'QUOTE' &&
    quoteStatus.value === 'OFFERED'
})

// 确认后可以起草合同
const canDraftContract = computed(() => {
  return msgType.value === 'QUOTE' &&
    quoteStatus.value === 'ACCEPTED'
})

// 解析图片 payload
function parseImagePayload(payloadJson?: string) {
  if (!payloadJson) return null
  try {
    return JSON.parse(payloadJson) as { fileUrl: string; fileName?: string }
  } catch {
    return null
  }
}

// 解析附件 payload
function parseAttachmentPayload(payloadJson?: string) {
  if (!payloadJson) return null
  try {
    return JSON.parse(payloadJson) as { fileUrl: string; fileName: string; size: number }
  } catch {
    return null
  }
}

// 解析合同 payload
function parseContractPayload(payloadJson?: string) {
  if (!payloadJson) return null
  try {
    return JSON.parse(payloadJson)
  } catch {
    return null
  }
}

function handleConfirmQuote() {
  if (typeof props.message.id === 'number') {
    emit('confirm-quote', props.message.id)
  }
}

function handleRejectQuote() {
  if (typeof props.message.id === 'number') {
    emit('reject-quote', props.message.id)
  }
}

function handleDraftContract() {
  if (typeof props.message.id === 'number') {
    emit('open-draft-contract', props.message.id)
  }
}

function handleCounterQuote() {
  if (typeof props.message.id === 'number') {
    emit('counter-quote', props.message.id)
  }
}
</script>

<template>
  <div
    :class="[
      'flex gap-2',
      isSystem ? 'justify-center' : isSent ? 'justify-end' : 'justify-start'
    ]"
  >
    <!-- 系统消息 -->
    <div
      v-if="isSystem"
      class="px-3 py-1.5 bg-gray-100 rounded-full text-xs text-gray-500"
    >
      {{ message.content }}
    </div>

    <!-- 普通消息 -->
    <div
      v-else
      :class="['max-w-[75%] flex flex-col', isSent ? 'items-end' : 'items-start']"
    >
      <!-- 消息内容 -->
      <div>
        <!-- 报价消息 - 使用增强版 QuoteCard -->
        <QuoteCard
          v-if="msgType === 'QUOTE'"
          :message="message"
          :previous-quote-payload-json="previousQuotePayloadJson"
          :subject-name="subjectName"
          :subject-location="subjectLocation"
          @accept="handleConfirmQuote"
          @counter="handleCounterQuote"
          @reject="handleRejectQuote"
          @draft-contract="handleDraftContract"
        />

        <!-- 图片消息 -->
        <div
          v-else-if="msgType === 'IMAGE'"
          :class="[
            'rounded-xl p-1.5 shadow-sm',
            isSent ? 'bg-brand-600' : 'bg-white border border-gray-200'
          ]"
        >
          <img
            v-if="parseImagePayload(message.payloadJson)?.fileUrl"
            :src="parseImagePayload(message.payloadJson)?.fileUrl"
            :alt="parseImagePayload(message.payloadJson)?.fileName || '图片'"
            class="max-w-[280px] max-h-[200px] rounded-lg cursor-pointer hover:opacity-90 transition-opacity object-cover"
            @click="emit('preview-image', parseImagePayload(message.payloadJson)?.fileUrl || '')"
          />
          <div v-else :class="['text-sm', isSent ? 'text-white/80' : 'text-gray-500']">[图片加载失败]</div>
        </div>

        <!-- 附件消息 -->
        <div
          v-else-if="msgType === 'ATTACHMENT'"
          :class="[
            'rounded-xl px-4 py-3 shadow-sm',
            isSent ? 'bg-brand-600' : 'bg-white border border-gray-200'
          ]"
        >
          <div class="flex items-center gap-3">
            <div
              :class="[
                'w-10 h-10 rounded-lg flex items-center justify-center shrink-0',
                isSent ? 'bg-white/20' : 'bg-blue-50'
              ]"
            >
              <Document :class="['w-5 h-5', isSent ? 'text-white' : 'text-blue-600']" />
            </div>
            <div class="flex-1 min-w-0">
              <div :class="['text-sm font-medium truncate', isSent ? 'text-white' : 'text-gray-900']">
                {{ parseAttachmentPayload(message.payloadJson)?.fileName || '附件' }}
              </div>
              <div :class="['text-xs', isSent ? 'text-brand-100/80' : 'text-gray-400']">
                {{ formatFileSize(parseAttachmentPayload(message.payloadJson)?.size || 0) }}
              </div>
            </div>
            <button
              :class="[
                'shrink-0 px-3 py-1.5 text-xs font-bold rounded-lg transition-all',
                isSent ? 'bg-white/20 text-white hover:bg-white/30' : 'bg-blue-50 text-blue-600 hover:bg-blue-100'
              ]"
              @click="emit('download-attachment', parseAttachmentPayload(message.payloadJson)?.fileUrl || '', parseAttachmentPayload(message.payloadJson)?.fileName || 'file')"
            >
              下载
            </button>
          </div>
        </div>

        <!-- 合同消息 - 使用增强版 ContractFlowCard -->
        <ContractFlowCard
          v-else-if="msgType === 'CONTRACT' && parseContractPayload(message.payloadJson)"
          :contract="parseContractPayload(message.payloadJson)!"
          :current-company-id="currentCompanyId"
          @view="emit('view-contract', parseContractPayload(message.payloadJson)?.contractId)"
          @sign="emit('sign-contract', parseContractPayload(message.payloadJson)?.contractId)"
        />

        <!-- 普通文本消息 -->
        <div
          v-else
          :class="[
            'px-4 py-2.5 rounded-2xl shadow-sm',
            isSent
              ? 'bg-brand-600 text-white rounded-tr-sm'
              : 'bg-white text-gray-900 border border-gray-200 rounded-tl-sm'
          ]"
        >
          <p class="text-sm whitespace-pre-wrap break-words">{{ message.content }}</p>
        </div>
      </div>

      <!-- 消息元信息 -->
      <div
        :class="[
          'flex items-center gap-2 mt-1 px-1',
          isSent ? 'flex-row-reverse' : 'flex-row'
        ]"
      >
        <span class="text-[10px] text-gray-400">{{ message.time }}</span>

        <!-- 发送状态 -->
        <template v-if="isSent">
          <Loading v-if="message.status === 'pending'" class="w-3 h-3 text-gray-400 animate-spin" />
          <Check v-else-if="message.status === 'sent'" class="w-3 h-3 text-brand-500" />
          <Warning v-else-if="message.status === 'failed'" class="w-3 h-3 text-red-500" />
        </template>
      </div>
    </div>
  </div>
</template>
