/**
 * Chat WebSocket Composable
 * Unified WebSocket connection management for chat
 */

import { ref, computed, onBeforeUnmount, watch } from 'vue'
import { buildChatWsUrl } from '../../utils/chatWs'

/** WebSocket 消息类型 */
export type WsMessageType =
  | 'MESSAGE'
  | 'OFFER_UPDATED'
  | 'MESSAGE_UPDATE'
  | 'SENT'
  | 'ERROR'
  | 'TYPING'
  | 'READ'

/** 收到的 WebSocket 消息 */
export interface WsIncomingMessage {
  type: WsMessageType
  conversationId?: number
  message?: any
  messageId?: number
  tempId?: string
  id?: number
  payload?: any
  error?: string
}

/** 发送的 WebSocket 消息 */
export interface WsSendPayload {
  type: 'SEND' | 'TYPING' | 'READ'
  conversationId: number
  msgType?: string
  content?: string
  payload?: string  // 注意：后端期望字段名是 'payload'，不是 'payloadJson'
  tempId?: string
}

/** WebSocket 事件回调 */
export interface WsEventCallbacks {
  onMessage?: (msg: WsIncomingMessage) => void
  onConnect?: () => void
  onDisconnect?: (reason: string) => void
  onError?: (error: Event) => void
}

/** WebSocket 状态 */
export type WsStatus = 'disconnected' | 'connecting' | 'connected' | 'reconnecting'

/**
 * Chat WebSocket Composable
 */
export function useChatWebSocket(
  getToken: () => string | undefined,
  canConnect: () => boolean,
  callbacks?: WsEventCallbacks
) {
  const ws = ref<WebSocket | null>(null)
  const status = ref<WsStatus>('disconnected')
  const lastError = ref<string | null>(null)

  let reconnectTimer: number | null = null
  let reconnectAttempt = 0
  let closeHinted = false

  // 最大重连间隔 8 秒
  const MAX_RECONNECT_DELAY = 8000
  // 基础重连间隔 500ms
  const BASE_RECONNECT_DELAY = 500

  const isConnected = computed(() => status.value === 'connected')
  const isConnecting = computed(() => status.value === 'connecting' || status.value === 'reconnecting')

  /**
   * 清理 WebSocket 连接
   */
  function cleanup() {
    if (reconnectTimer) {
      window.clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    reconnectAttempt = 0
    closeHinted = false

    if (ws.value) {
      try {
        ws.value.close()
      } catch {
        // ignore
      }
      ws.value = null
    }
    status.value = 'disconnected'
  }

  /**
   * 建立 WebSocket 连接
   */
  function connect() {
    try {
      if (!canConnect()) return

      // 如果已连接或正在连接，不重复连接
      if (ws.value) {
        const readyState = ws.value.readyState
        if (readyState === WebSocket.OPEN || readyState === WebSocket.CONNECTING) {
          return
        }
      }

      const token = getToken()
      const url = buildChatWsUrl(token)
      const socket = new WebSocket(url)
      ws.value = socket
      status.value = 'connecting'
      lastError.value = null

      socket.onopen = () => {
        status.value = 'connected'
        reconnectAttempt = 0
        closeHinted = false
        callbacks?.onConnect?.()
      }

      socket.onclose = (ev: CloseEvent) => {
        status.value = 'disconnected'

        // 未登录或已退出：不重连
        if (!canConnect()) {
          callbacks?.onDisconnect?.('logged_out')
          return
        }

        // 通知断开
        if (!closeHinted) {
          closeHinted = true
          callbacks?.onDisconnect?.(ev.reason || 'connection_closed')
        }

        // 安排重连
        scheduleReconnect()
      }

      socket.onerror = (err) => {
        status.value = 'disconnected'
        lastError.value = 'WebSocket error'
        callbacks?.onError?.(err)
      }

      socket.onmessage = (ev) => {
        try {
          const data = JSON.parse(ev.data) as WsIncomingMessage
          callbacks?.onMessage?.(data)
        } catch (e) {
          console.error('[useChatWebSocket] Failed to parse message:', e)
        }
      }
    } catch (e) {
      console.error('[useChatWebSocket] Failed to connect:', e)
      status.value = 'disconnected'
      lastError.value = String(e)
      scheduleReconnect()
    }
  }

  /**
   * 安排重连
   */
  function scheduleReconnect() {
    if (reconnectTimer) return
    if (!canConnect()) return

    status.value = 'reconnecting'
    reconnectAttempt += 1
    const delay = Math.min(MAX_RECONNECT_DELAY, BASE_RECONNECT_DELAY * reconnectAttempt)

    reconnectTimer = window.setTimeout(() => {
      reconnectTimer = null
      connect()
    }, delay)
  }

  /**
   * 主动断开连接
   */
  function disconnect() {
    cleanup()
    callbacks?.onDisconnect?.('manual')
  }

  /**
   * 发送消息
   */
  function send(payload: WsSendPayload): boolean {
    if (!ws.value || ws.value.readyState !== WebSocket.OPEN) {
      return false
    }

    try {
      ws.value.send(JSON.stringify(payload))
      return true
    } catch (e) {
      console.error('[useChatWebSocket] Failed to send:', e)
      return false
    }
  }

  /**
   * 发送文本消息
   */
  function sendText(conversationId: number, content: string, tempId: string): boolean {
    return send({
      type: 'SEND',
      conversationId,
      msgType: 'TEXT',
      content,
      tempId
    })
  }

  /**
   * 发送报价消息
   */
  function sendQuote(
    conversationId: number,
    payloadJson: string,
    content: string,
    tempId: string
  ): boolean {
    return send({
      type: 'SEND',
      conversationId,
      msgType: 'QUOTE',
      content,
      payload: payloadJson,
      tempId
    })
  }

  /**
   * 发送图片消息
   */
  function sendImage(
    conversationId: number,
    payloadJson: string,
    tempId: string
  ): boolean {
    return send({
      type: 'SEND',
      conversationId,
      msgType: 'IMAGE',
      content: '[图片]',
      payload: payloadJson,
      tempId
    })
  }

  /**
   * 发送附件消息
   */
  function sendAttachment(
    conversationId: number,
    payloadJson: string,
    fileName: string,
    tempId: string
  ): boolean {
    return send({
      type: 'SEND',
      conversationId,
      msgType: 'ATTACHMENT',
      content: `[附件] ${fileName}`,
      payload: payloadJson,
      tempId
    })
  }

  /**
   * 发送合同消息
   */
  function sendContract(
    conversationId: number,
    payloadJson: string,
    tempId: string
  ): boolean {
    return send({
      type: 'SEND',
      conversationId,
      msgType: 'CONTRACT',
      content: '[合同]',
      payload: payloadJson,
      tempId
    })
  }

  /**
   * 发送系统消息（用于条款确认等）
   */
  function sendSystem(
    conversationId: number,
    content: string,
    payloadJson: string,
    tempId: string
  ): boolean {
    return send({
      type: 'SEND',
      conversationId,
      msgType: 'SYSTEM',
      content,
      payload: payloadJson,
      tempId
    })
  }

  /**
   * 发送正在输入状态
   */
  function sendTyping(conversationId: number): boolean {
    return send({
      type: 'TYPING',
      conversationId
    })
  }

  /**
   * 发送已读状态
   */
  function sendRead(conversationId: number): boolean {
    return send({
      type: 'READ',
      conversationId
    })
  }

  /**
   * 检查连接状态并尝试重连
   */
  function ensureConnected(): boolean {
    if (isConnected.value) return true

    if (!isConnecting.value) {
      connect()
    }
    return false
  }

  // 组件卸载时清理
  onBeforeUnmount(() => {
    cleanup()
  })

  return {
    // 状态
    status,
    isConnected,
    isConnecting,
    lastError,

    // 连接管理
    connect,
    disconnect,
    cleanup,
    ensureConnected,

    // 消息发送
    send,
    sendText,
    sendQuote,
    sendImage,
    sendAttachment,
    sendContract,
    sendSystem,
    sendTyping,
    sendRead
  }
}

export type UseChatWebSocket = ReturnType<typeof useChatWebSocket>
