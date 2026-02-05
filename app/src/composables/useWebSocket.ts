/**
 * WebSocket composable for uni-app
 * Uses uni.connectSocket for cross-platform support (H5 / APP-PLUS)
 * Provides heartbeat, auto-reconnect with exponential backoff, and message routing.
 */
import { ref, onUnmounted } from 'vue'
import { useAuthStore } from '../store/auth'

export interface WsMessage {
  type: 'chat' | 'system' | 'ping'
  conversationId?: number
  fromUserId?: number
  toUserId?: number
  content?: string
  msgType?: string
  payloadJson?: string
  quoteStatus?: string
  timestamp?: string
}

export function useWebSocket() {
  const authStore = useAuthStore()
  const connected = ref(false)
  const connecting = ref(false)

  let socketTask: UniApp.SocketTask | null = null
  let heartbeatTimer: ReturnType<typeof setInterval> | null = null
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  let reconnectAttempts = 0

  const MAX_RECONNECT = 5
  const HEARTBEAT_INTERVAL = 30000

  // --- message handler registry ---
  const messageHandlers: ((msg: WsMessage) => void)[] = []

  function onMessage(handler: (msg: WsMessage) => void) {
    messageHandlers.push(handler)
  }

  function offMessage(handler: (msg: WsMessage) => void) {
    const idx = messageHandlers.indexOf(handler)
    if (idx > -1) messageHandlers.splice(idx, 1)
  }

  // --- WebSocket URL (conditional compilation) ---
  function getWsUrl(): string {
    let baseUrl = ''
    // #ifdef H5
    const loc = window.location
    const wsProtocol = loc.protocol === 'https:' ? 'wss:' : 'ws:'
    baseUrl = `${wsProtocol}//${loc.host}`
    // #endif
    // #ifdef APP-PLUS
    baseUrl = 'ws://172.28.0.135:8080'
    // #endif
    return `${baseUrl}/ws/chat?token=${authStore.token}`
  }

  // --- connection lifecycle ---
  function connect() {
    if (connected.value || connecting.value || !authStore.token) return

    connecting.value = true

    socketTask = uni.connectSocket({
      url: getWsUrl(),
      complete: () => {},
    })

    socketTask.onOpen(() => {
      connected.value = true
      connecting.value = false
      reconnectAttempts = 0
      startHeartbeat()
    })

    socketTask.onMessage((res) => {
      try {
        const msg: WsMessage = JSON.parse(res.data as string)
        if (msg.type === 'ping') {
          send({ type: 'ping' })
          return
        }
        messageHandlers.forEach((h) => h(msg))
      } catch {
        // non-JSON message, ignore
      }
    })

    socketTask.onClose(() => {
      connected.value = false
      connecting.value = false
      stopHeartbeat()
      scheduleReconnect()
    })

    socketTask.onError(() => {
      connected.value = false
      connecting.value = false
      stopHeartbeat()
      scheduleReconnect()
    })
  }

  function disconnect() {
    stopHeartbeat()
    clearReconnect()
    reconnectAttempts = MAX_RECONNECT // prevent auto-reconnect after explicit disconnect
    if (socketTask) {
      socketTask.close({})
      socketTask = null
    }
    connected.value = false
    connecting.value = false
  }

  // --- send helpers ---
  function send(data: Record<string, unknown>): boolean {
    if (!connected.value || !socketTask) return false
    try {
      socketTask.send({
        data: JSON.stringify(data),
        complete: () => {},
      })
      return true
    } catch {
      return false
    }
  }

  function sendChatMessage(conversationId: number, toUserId: number, content: string): boolean {
    return send({
      type: 'chat',
      conversationId,
      toUserId,
      content,
      msgType: 'text',
    })
  }

  function sendQuoteMessage(conversationId: number, toUserId: number, payloadJson: string, content: string): boolean {
    return send({
      type: 'chat',
      conversationId,
      toUserId,
      content,
      msgType: 'QUOTE',
      payloadJson,
    })
  }

  // --- heartbeat ---
  function startHeartbeat() {
    stopHeartbeat()
    heartbeatTimer = setInterval(() => {
      send({ type: 'ping' })
    }, HEARTBEAT_INTERVAL)
  }

  function stopHeartbeat() {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer)
      heartbeatTimer = null
    }
  }

  // --- reconnect with exponential backoff ---
  function clearReconnect() {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }

  function scheduleReconnect() {
    if (reconnectAttempts >= MAX_RECONNECT) return
    clearReconnect()
    const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), 30000)
    reconnectAttempts++
    reconnectTimer = setTimeout(() => {
      connect()
    }, delay)
  }

  // --- cleanup on component unmount ---
  onUnmounted(() => {
    disconnect()
  })

  return {
    connected,
    connecting,
    connect,
    disconnect,
    send,
    sendChatMessage,
    sendQuoteMessage,
    onMessage,
    offMessage,
  }
}
