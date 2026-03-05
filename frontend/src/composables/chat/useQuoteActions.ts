/**
 * Quote Actions Composable
 * Manages quote operations (confirm, counter, reject)
 */

import { ref, computed } from 'vue'
import { showToast } from '@/composables/useToast'
import { showConfirm } from '@/composables/useConfirm'
import { confirmChatOffer } from '../../api/chat'
import type { QuotePayloadV1, BasisQuotePayloadV1, QuoteFieldsV1, BasisQuoteFieldsV1 } from '../../types/chat/quote'
import type { UiMessage } from '../../types/chat/message'
import { parseQuotePayload, buildQuoteSummary } from '../../utils/chat/quoteParser'
import type { UseChatWebSocket } from './useChatWebSocket'
import type { UseChatMessages } from './useChatMessages'

/**
 * Quote Actions Composable Options
 */
export interface UseQuoteActionsOptions {
  webSocket: UseChatWebSocket
  messages: UseChatMessages
  getConversationId: () => number | null
}

/**
 * Quote Actions Composable
 */
export function useQuoteActions(options: UseQuoteActionsOptions) {
  const { webSocket, messages, getConversationId } = options

  const confirming = ref(false)
  const rejecting = ref(false)

  /**
   * 确认/接受报价
   */
  async function confirmQuote(messageId: number): Promise<boolean> {
    if (typeof messageId !== 'number') return false

    confirming.value = true

    try {
      const res = await confirmChatOffer(messageId)
      if (res.code === 0) {
        // 更新消息状态
        messages.handleQuoteStatusUpdate(messageId, 'ACCEPTED')
        showToast.success('报价已确认')
        return true
      } else {
        showToast.error(res.message || '确认失败')
        return false
      }
    } catch (e: any) {
      showToast.error(e.response?.data?.message || '确认失败')
      return false
    } finally {
      confirming.value = false
    }
  }

  /**
   * 确认报价并弹出合同起草
   */
  async function confirmQuoteAndDraft(
    messageId: number,
    onDraftContract: (messageId: number, quoteFields: QuoteFieldsV1 | BasisQuoteFieldsV1) => void
  ): Promise<void> {
    const success = await confirmQuote(messageId)
    if (success) {
      // 获取报价数据用于起草合同
      const msg = messages.messages.value.find(m => m.id === messageId)
      if (msg) {
        const payload = parseQuotePayload(msg.payloadJson)
        if (payload) {
          onDraftContract(messageId, payload.fields)
        }
      }
    }
  }

  /**
   * 拒绝报价（需确认）
   */
  async function rejectQuote(messageId: number): Promise<boolean> {
    const ok = await showConfirm({ title: '拒绝报价', message: '确定要拒绝此报价吗？', type: 'warning' })
    if (!ok) return false

    rejecting.value = true

    try {
      // TODO: 实现拒绝 API
      // const res = await rejectChatOffer(messageId)
      messages.handleQuoteStatusUpdate(messageId, 'REJECTED')
      showToast.info('已拒绝报价')
      return true
    } catch (e: any) {
      showToast.error('操作失败')
      return false
    } finally {
      rejecting.value = false
    }
  }

  /**
   * 发送报价
   */
  function sendQuote(
    fields: QuoteFieldsV1
  ): boolean {
    const conversationId = getConversationId()
    if (!conversationId) {
      showToast.warning('请先选择会话')
      return false
    }

    if (!webSocket.ensureConnected()) {
      showToast.warning('实时连接未就绪，正在重连…')
      return false
    }

    const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`
    const payload: QuotePayloadV1 = {
      version: 1,
      kind: 'QUOTE_V1',
      createdAt: new Date().toISOString(),
      fields
    }

    const summary = buildQuoteSummary(payload)
    const payloadJson = JSON.stringify(payload)

    // 乐观更新
    messages.addPendingMessage(conversationId, 'QUOTE', summary, tempId, payloadJson)

    // 发送
    const sent = webSocket.sendQuote(conversationId, payloadJson, summary, tempId)
    if (!sent) {
      messages.failMessage(tempId)
      showToast.error('发送失败')
      return false
    }

    return true
  }

  /**
   * 发送基差报价
   */
  function sendBasisQuote(
    fields: BasisQuoteFieldsV1
  ): boolean {
    const conversationId = getConversationId()
    if (!conversationId) {
      showToast.warning('请先选择会话')
      return false
    }

    if (!webSocket.ensureConnected()) {
      showToast.warning('实时连接未就绪，正在重连…')
      return false
    }

    const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`
    const payload: BasisQuotePayloadV1 = {
      version: 1,
      kind: 'BASIS_QUOTE_V1',
      createdAt: new Date().toISOString(),
      fields
    }

    const summary = buildQuoteSummary(payload)
    const payloadJson = JSON.stringify(payload)

    // 乐观更新
    messages.addPendingMessage(conversationId, 'QUOTE', summary, tempId, payloadJson)

    // 发送
    const sent = webSocket.sendQuote(conversationId, payloadJson, summary, tempId)
    if (!sent) {
      messages.failMessage(tempId)
      showToast.error('发送失败')
      return false
    }

    return true
  }

  /**
   * 发送还价（带关联）
   */
  function sendCounterQuote(
    _originalMessage: UiMessage,
    newFields: QuoteFieldsV1
  ): boolean {
    // 发送新报价
    return sendQuote(newFields)
  }

  // ==================== Computed ====================

  /**
   * 是否有待确认的报价
   */
  const hasPendingQuote = computed(() => {
    return messages.messages.value.some(m =>
      m.type === 'received' &&
      (m.msgType || '').toUpperCase() === 'QUOTE' &&
      m.quoteStatus === 'OFFERED'
    )
  })

  /**
   * 获取待确认的报价消息
   */
  const pendingQuoteMessages = computed(() => {
    return messages.messages.value.filter(m =>
      m.type === 'received' &&
      (m.msgType || '').toUpperCase() === 'QUOTE' &&
      m.quoteStatus === 'OFFERED'
    )
  })

  /**
   * 获取最新待确认报价
   */
  const latestPendingQuote = computed<UiMessage | null>(() => {
    const pending = pendingQuoteMessages.value
    return pending.length > 0 ? pending[pending.length - 1]! : null
  })

  return {
    // 状态
    confirming,
    rejecting,

    // 操作
    confirmQuote,
    confirmQuoteAndDraft,
    rejectQuote,
    sendQuote,
    sendBasisQuote,
    sendCounterQuote,

    // Computed
    hasPendingQuote,
    pendingQuoteMessages,
    latestPendingQuote
  }
}

export type UseQuoteActions = ReturnType<typeof useQuoteActions>
