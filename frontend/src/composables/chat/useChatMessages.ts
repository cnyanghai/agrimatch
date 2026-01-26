/**
 * Chat Messages Composable
 * Manages message state, loading, and updates
 */

import { ref, computed, watch } from 'vue'
import { getConversationMessages, type ChatMessageResponse } from '../../api/chat'
import type { UiMessage, QuoteStatus } from '../../types/chat/message'
import { toUiMessage, formatMessageTime } from '../../types/chat/message'
import { parseQuotePayload, getQuoteFields } from '../../utils/chat/quoteParser'
import type { QuoteFieldsV1 } from '../../types/chat/quote'

/**
 * Chat Messages Composable Options
 */
export interface UseChatMessagesOptions {
  getCurrentUserId: () => number | undefined
  limit?: number
}

/**
 * Chat Messages Composable
 */
export function useChatMessages(options: UseChatMessagesOptions) {
  const { getCurrentUserId, limit = 50 } = options

  const messages = ref<UiMessage[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const hasMore = ref(true)

  /**
   * 加载会话消息
   */
  async function loadMessages(conversationId: number): Promise<void> {
    if (!conversationId) return

    loading.value = true
    error.value = null

    try {
      const res = await getConversationMessages(conversationId, limit)
      if (res.code === 0 && res.data) {
        const userId = getCurrentUserId()
        if (!userId) {
          messages.value = []
          return
        }

        // 转换为 UI 消息并反转顺序（后端返回最新在前）
        messages.value = res.data
          .map((m: ChatMessageResponse) => toUiMessage(m, userId))
          .reverse()

        hasMore.value = res.data.length >= limit
      } else {
        error.value = res.message || '加载消息失败'
        messages.value = []
      }
    } catch (e: any) {
      console.error('[useChatMessages] Failed to load messages:', e)
      error.value = e.message || '加载消息失败'
      messages.value = []
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载更多历史消息
   */
  async function loadMoreMessages(conversationId: number): Promise<void> {
    if (!conversationId || loading.value || !hasMore.value) return

    const oldestMessage = messages.value[0]
    if (!oldestMessage) return

    loading.value = true

    try {
      // TODO: 实现分页加载（需要后端支持 before 参数）
      // 目前暂不支持
      hasMore.value = false
    } finally {
      loading.value = false
    }
  }

  /**
   * 清空消息
   */
  function clearMessages(): void {
    messages.value = []
    hasMore.value = true
    error.value = null
  }

  /**
   * 添加新消息
   */
  function addMessage(msg: UiMessage): void {
    messages.value.push(msg)
  }

  /**
   * 添加临时消息（乐观更新）
   */
  function addPendingMessage(
    conversationId: number,
    msgType: string,
    content: string,
    tempId: string,
    payloadJson?: string
  ): UiMessage {
    const msg: UiMessage = {
      id: tempId,
      conversationId,
      type: 'sent',
      msgType,
      content,
      payloadJson,
      status: 'pending',
      time: formatMessageTime(new Date().toISOString())
    }
    messages.value.push(msg)
    return msg
  }

  /**
   * 确认消息发送成功（替换 tempId 为真实 id）
   */
  function confirmMessage(tempId: string, realId: number): void {
    const idx = messages.value.findIndex(m => m.id === tempId)
    if (idx >= 0 && messages.value[idx]) {
      messages.value[idx] = {
        ...messages.value[idx],
        id: realId,
        status: 'sent'
      }
    }
  }

  /**
   * 标记消息发送失败
   */
  function failMessage(tempId: string): void {
    const idx = messages.value.findIndex(m => m.id === tempId)
    if (idx >= 0 && messages.value[idx]) {
      messages.value[idx] = {
        ...messages.value[idx],
        status: 'failed'
      }
    }
  }

  /**
   * 更新消息（通过 WebSocket 推送）
   */
  function updateMessage(messageId: number, updates: Partial<UiMessage>): void {
    const idx = messages.value.findIndex(m => m.id === messageId)
    if (idx >= 0 && messages.value[idx]) {
      messages.value.splice(idx, 1, {
        ...messages.value[idx],
        ...updates
      })
    }
  }

  /**
   * 更新消息 payload
   */
  function updateMessagePayload(messageId: number, payload: any): void {
    const idx = messages.value.findIndex(m => m.id === messageId)
    if (idx >= 0 && messages.value[idx]) {
      messages.value.splice(idx, 1, {
        ...messages.value[idx],
        payloadJson: JSON.stringify(payload)
      })
    }
  }

  /**
   * 处理收到的 WebSocket 消息
   */
  function handleIncomingMessage(
    msg: ChatMessageResponse,
    currentConversationId: number | null
  ): void {
    // 只处理当前会话的消息
    if (msg.conversationId !== currentConversationId) return

    const userId = getCurrentUserId()
    if (!userId) return

    const uiMsg = toUiMessage(msg, userId)
    addMessage(uiMsg)
  }

  /**
   * 处理报价状态更新
   */
  function handleQuoteStatusUpdate(
    messageId: number,
    quoteStatus: QuoteStatus
  ): void {
    updateMessage(messageId, { quoteStatus })
  }

  // ==================== Computed Properties ====================

  /**
   * 获取对方最新报价
   */
  const peerLatestQuote = computed<QuoteFieldsV1 | null>(() => {
    for (let i = messages.value.length - 1; i >= 0; i--) {
      const m = messages.value[i]
      if (!m) continue
      if (m.type !== 'received') continue
      if ((m.msgType || '').toUpperCase() !== 'QUOTE') continue

      const payload = parseQuotePayload(m.payloadJson)
      if (payload) {
        return getQuoteFields(payload) as QuoteFieldsV1 | null
      }
    }
    return null
  })

  /**
   * 检查是否有已接受的报价
   */
  const hasAcceptedQuote = computed(() => {
    return messages.value.some(m =>
      (m.msgType || '').toUpperCase() === 'QUOTE' &&
      m.quoteStatus === 'ACCEPTED'
    )
  })

  /**
   * 获取最新的已接受报价消息
   */
  const latestAcceptedQuoteMessage = computed<UiMessage | null>(() => {
    for (let i = messages.value.length - 1; i >= 0; i--) {
      const m = messages.value[i]
      if (!m) continue
      if ((m.msgType || '').toUpperCase() === 'QUOTE' && m.quoteStatus === 'ACCEPTED') {
        return m
      }
    }
    return null
  })

  /**
   * 获取所有报价消息（用于时间线）
   */
  const quoteMessages = computed(() => {
    return messages.value.filter(m =>
      (m.msgType || '').toUpperCase() === 'QUOTE'
    )
  })

  /**
   * 获取合同消息
   */
  const contractMessage = computed<UiMessage | null>(() => {
    return messages.value.find(m =>
      (m.msgType || '').toUpperCase() === 'CONTRACT'
    ) || null
  })

  /**
   * 消息数量
   */
  const messageCount = computed(() => messages.value.length)

  /**
   * 是否为空
   */
  const isEmpty = computed(() => messages.value.length === 0)

  return {
    // 状态
    messages,
    loading,
    error,
    hasMore,

    // 加载
    loadMessages,
    loadMoreMessages,
    clearMessages,

    // 消息操作
    addMessage,
    addPendingMessage,
    confirmMessage,
    failMessage,
    updateMessage,
    updateMessagePayload,

    // WebSocket 处理
    handleIncomingMessage,
    handleQuoteStatusUpdate,
    updateQuoteStatus: handleQuoteStatusUpdate,

    // Computed
    peerLatestQuote,
    hasAcceptedQuote,
    latestAcceptedQuoteMessage,
    quoteMessages,
    contractMessage,
    messageCount,
    isEmpty
  }
}

export type UseChatMessages = ReturnType<typeof useChatMessages>
