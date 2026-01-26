<script setup lang="ts">
import { ref, computed, nextTick, watch, onMounted } from 'vue'
import { Picture, Document, Position, Star, StarFilled, Loading, ArrowLeft } from '@element-plus/icons-vue'
import type { UiMessage } from '../../../types/chat/message'
import type { ChatConversationResponse } from '../../../types/chat/conversation'
import MessageBubble from '../message/MessageBubble.vue'

const props = defineProps<{
  messages: UiMessage[]
  conversation: ChatConversationResponse | null
  peerDisplayName: string
  wsConnected: boolean
  loading?: boolean
  sending?: boolean
  isFollowing?: boolean
  canNegotiate?: boolean
  isBasisTrade?: boolean
  transactionStep?: number
  showBackButton?: boolean
  currentCompanyId?: number
  subjectName?: string
  subjectLocation?: string
  schemaCode?: string
  categoryName?: string
  quoteCount?: number
}>()

const emit = defineEmits<{
  (e: 'send-text', text: string): void
  (e: 'send-image'): void
  (e: 'send-attachment'): void
  (e: 'send-gift'): void
  (e: 'open-negotiation'): void
  (e: 'open-subject-detail'): void
  (e: 'open-quote-timeline'): void
  (e: 'toggle-follow'): void
  (e: 'back'): void
  (e: 'scroll-to-bottom'): void
  (e: 'confirm-quote', messageId: number): void
  (e: 'reject-quote', messageId: number): void
  (e: 'counter-quote', messageId: number): void
  (e: 'open-draft-contract', messageId: number): void
  (e: 'view-contract', contractId: number): void
  (e: 'sign-contract', contractId: number): void
}>()

const messageInput = ref('')
const chatContainerRef = ref<HTMLElement | null>(null)

function scrollToBottom() {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}

function handleSend() {
  const text = messageInput.value.trim()
  if (!text) return
  emit('send-text', text)
  messageInput.value = ''
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

// 获取前一个报价的 payloadJson（用于差异对比）
function getPreviousQuotePayload(currentIndex: number): string | undefined {
  const currentMsg = props.messages[currentIndex]
  if (!currentMsg || (currentMsg.msgType || '').toUpperCase() !== 'QUOTE') {
    return undefined
  }

  // 向前查找上一个报价消息
  for (let i = currentIndex - 1; i >= 0; i--) {
    const msg = props.messages[i]
    if (msg && (msg.msgType || '').toUpperCase() === 'QUOTE') {
      return msg.payloadJson
    }
  }
  return undefined
}

// 消息变化时滚动到底部
watch(() => props.messages.length, () => {
  scrollToBottom()
})

onMounted(() => {
  scrollToBottom()
})

defineExpose({
  scrollToBottom
})
</script>

<template>
  <div class="h-full flex flex-col bg-gray-50">
    <!-- 顶部栏 -->
    <div class="bg-white border-b border-gray-200 px-4 py-3 flex items-center gap-3 shrink-0">
      <!-- 返回按钮（移动端） -->
      <button
        v-if="showBackButton"
        @click="emit('back')"
        class="p-2 -ml-2 rounded-lg hover:bg-gray-100 transition-colors lg:hidden"
      >
        <ArrowLeft class="w-5 h-5 text-gray-600" />
      </button>

      <!-- 对方信息 -->
      <div class="flex-1 min-w-0">
        <div class="flex items-center gap-2">
          <span class="font-bold text-gray-900 truncate">{{ peerDisplayName || '选择会话' }}</span>
          <button
            v-if="conversation"
            @click="emit('toggle-follow')"
            class="p-1 rounded hover:bg-gray-100 transition-colors"
          >
            <component
              :is="isFollowing ? StarFilled : Star"
              class="w-4 h-4"
              :class="isFollowing ? 'text-amber-500' : 'text-gray-400'"
            />
          </button>
        </div>
        <div class="flex items-center gap-2 mt-0.5">
          <div
            :class="[
              'w-2 h-2 rounded-full',
              wsConnected ? 'bg-brand-500' : 'bg-gray-300'
            ]"
          ></div>
          <span class="text-xs text-gray-500">
            {{ wsConnected ? '已连接' : '连接中...' }}
          </span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div v-if="conversation" class="flex items-center gap-2">
        <button
          v-if="quoteCount && quoteCount > 0"
          @click="emit('open-quote-timeline')"
          class="px-3 py-1.5 text-xs font-medium text-gray-600 bg-gray-100 rounded-lg hover:bg-gray-200 transition-all flex items-center gap-1"
        >
          <span>报价历史</span>
          <span class="px-1.5 py-0.5 text-[10px] font-bold bg-brand-100 text-brand-600 rounded-full">
            {{ quoteCount }}
          </span>
        </button>
        <button
          @click="emit('open-subject-detail')"
          class="px-3 py-1.5 text-xs font-medium text-gray-600 bg-gray-100 rounded-lg hover:bg-gray-200 transition-all"
        >
          查看标的
        </button>
        <button
          v-if="canNegotiate"
          @click="emit('open-negotiation')"
          class="px-3 py-1.5 text-xs font-bold text-white bg-brand-600 rounded-lg hover:bg-brand-700 transition-all"
        >
          {{ isBasisTrade ? '基差报价' : '发送报价' }}
        </button>
      </div>
    </div>

    <!-- 交易进度条 -->
    <div v-if="transactionStep && transactionStep > 0" class="bg-white border-b border-gray-100 px-4 py-2">
      <div class="flex items-center gap-2">
        <span class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">交易进度</span>
        <div class="flex-1 flex items-center gap-1">
          <template v-for="i in 5" :key="i">
            <div
              :class="[
                'h-1 flex-1 rounded-full transition-all',
                i <= transactionStep ? 'bg-brand-500' : 'bg-gray-200'
              ]"
            ></div>
          </template>
        </div>
        <span class="text-xs text-gray-500">{{ transactionStep }}/5</span>
      </div>
    </div>

    <!-- 消息列表 -->
    <div
      ref="chatContainerRef"
      class="flex-1 overflow-y-auto px-4 py-4 space-y-4"
    >
      <!-- 加载状态 -->
      <div v-if="loading" class="flex items-center justify-center py-8">
        <div class="w-6 h-6 border-2 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!conversation" class="flex flex-col items-center justify-center h-full text-center">
        <div class="w-20 h-20 rounded-3xl bg-gray-100 flex items-center justify-center mb-4">
          <svg class="w-10 h-10 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
        </div>
        <p class="text-lg font-medium text-gray-600 mb-1">选择一个会话开始聊天</p>
        <p class="text-sm text-gray-400">从左侧列表选择联系人</p>
      </div>

      <!-- 消息列表 -->
      <template v-else>
        <MessageBubble
          v-for="(msg, idx) in messages"
          :key="msg.id"
          :message="msg"
          :previous-quote-payload-json="getPreviousQuotePayload(idx)"
          :subject-name="subjectName"
          :subject-location="subjectLocation"
          :schema-code="schemaCode"
          :category-name="categoryName"
          :current-company-id="currentCompanyId"
          @confirm-quote="emit('confirm-quote', $event)"
          @reject-quote="emit('reject-quote', $event)"
          @counter-quote="emit('counter-quote', $event)"
          @open-draft-contract="emit('open-draft-contract', $event)"
          @view-contract="emit('view-contract', $event)"
          @sign-contract="emit('sign-contract', $event)"
        />
      </template>
    </div>

    <!-- 输入区域 -->
    <div v-if="conversation" class="bg-white border-t border-gray-200 p-4 shrink-0">
      <!-- 工具栏 -->
      <div class="flex items-center gap-2 mb-3">
        <button
          @click="emit('send-image')"
          class="p-2 rounded-lg text-gray-500 hover:bg-gray-100 hover:text-gray-700 transition-all"
          title="发送图片"
        >
          <Picture class="w-5 h-5" />
        </button>
        <button
          @click="emit('send-attachment')"
          class="p-2 rounded-lg text-gray-500 hover:bg-gray-100 hover:text-gray-700 transition-all"
          title="发送附件"
        >
          <Document class="w-5 h-5" />
        </button>
        <button
          @click="emit('send-gift')"
          class="p-2 rounded-lg text-gray-500 hover:bg-gray-100 hover:text-gray-700 transition-all"
          title="赠送积分"
        >
          <Position class="w-5 h-5" />
        </button>

        <div class="flex-1"></div>

        <button
          @click="emit('open-negotiation')"
          class="px-3 py-1.5 text-xs font-bold text-brand-600 bg-brand-50 rounded-lg hover:bg-brand-100 transition-all"
        >
          {{ isBasisTrade ? '基差报价' : '修改报价' }}
        </button>
      </div>

      <!-- 输入框 -->
      <div class="flex items-end gap-3">
        <textarea
          v-model="messageInput"
          @keydown="handleKeydown"
          rows="1"
          placeholder="输入消息..."
          class="flex-1 px-4 py-2.5 text-sm bg-gray-50 border border-gray-200 rounded-xl resize-none focus:bg-white focus:border-brand-500 focus:ring-1 focus:ring-brand-500/20 outline-none transition-all"
          style="min-height: 42px; max-height: 120px;"
        ></textarea>
        <button
          @click="handleSend"
          :disabled="!messageInput.trim() || sending"
          class="px-5 py-2.5 bg-brand-600 text-white font-bold text-sm rounded-xl hover:bg-brand-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
        >
          <Loading v-if="sending" class="w-4 h-4 animate-spin" />
          <span v-else>发送</span>
        </button>
      </div>
    </div>
  </div>
</template>
