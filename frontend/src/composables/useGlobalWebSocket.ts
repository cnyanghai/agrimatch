/**
 * 全局 WebSocket 连接管理
 * 用户登录后自动连接，接收实时消息通知
 */

import { ref, watch, onBeforeUnmount } from 'vue'
import { useAuthStore } from '../store/auth'
import { useNotificationStore } from '../stores/notification'
import { buildChatWsUrl } from '../utils/chatWs'
import { playNotificationSound } from '../utils/notificationSound'
import { showBrowserNotification, startTitleFlash, hasNotificationPermission } from '../utils/browserNotification'
import { useRouter, useRoute } from 'vue-router'

export type WsStatus = 'disconnected' | 'connecting' | 'connected' | 'reconnecting'

/** WebSocket 收到的消息类型 */
interface WsIncomingMessage {
  type: string
  conversationId?: number
  message?: {
    id: number
    conversationId: number
    fromUserId: number
    toUserId: number
    msgType: string
    content: string
    createTime: string
    fromUserName?: string
    fromNickName?: string
  }
  messageId?: number
  payload?: any
  serverTime?: string
}

// 单例状态
let ws: WebSocket | null = null
let reconnectTimer: number | null = null
let reconnectAttempt = 0
let pingInterval: number | null = null

const MAX_RECONNECT_DELAY = 30000
const BASE_RECONNECT_DELAY = 1000
const PING_INTERVAL = 30000

// 全局响应式状态
const status = ref<WsStatus>('disconnected')
const lastError = ref<string | null>(null)

// 当前活跃的会话 ID（用于判断是否需要通知）
let activeConversationId: number | null = null

/**
 * 全局 WebSocket Composable
 * 在 App.vue 中调用一次即可
 */
export function useGlobalWebSocket() {
  const authStore = useAuthStore()
  const notificationStore = useNotificationStore()
  const router = useRouter()
  const route = useRoute()

  /**
   * 清理连接
   */
  function cleanup() {
    if (reconnectTimer) {
      window.clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (pingInterval) {
      window.clearInterval(pingInterval)
      pingInterval = null
    }
    reconnectAttempt = 0

    if (ws) {
      try {
        ws.close()
      } catch {
        // ignore
      }
      ws = null
    }
    status.value = 'disconnected'
  }

  /**
   * 建立连接
   */
  function connect() {
    // 未登录不连接
    if (!authStore.me) {
      return
    }

    // 已连接或正在连接
    if (ws) {
      const readyState = ws.readyState
      if (readyState === WebSocket.OPEN || readyState === WebSocket.CONNECTING) {
        return
      }
    }

    try {
      const url = buildChatWsUrl()
      ws = new WebSocket(url)
      status.value = 'connecting'
      lastError.value = null

      ws.onopen = () => {
        status.value = 'connected'
        reconnectAttempt = 0

        // 启动心跳
        startPing()
      }

      ws.onclose = () => {
        status.value = 'disconnected'
        stopPing()

        // 如果已登录，尝试重连
        if (authStore.me) {
          scheduleReconnect()
        }
      }

      ws.onerror = () => {
        status.value = 'disconnected'
        lastError.value = 'WebSocket error'
      }

      ws.onmessage = (ev) => {
        try {
          const data = JSON.parse(ev.data) as WsIncomingMessage
          handleMessage(data)
        } catch {
          // silently ignore
        }
      }
    } catch (e) {
      status.value = 'disconnected'
      lastError.value = String(e)
      scheduleReconnect()
    }
  }

  /**
   * 处理收到的消息
   */
  function handleMessage(data: WsIncomingMessage) {
    switch (data.type) {
      case 'CONNECTED':
        break

      case 'PONG':
        // 心跳响应，忽略
        break

      case 'MESSAGE':
        handleNewMessage(data)
        break

      case 'OFFER_UPDATED':
      case 'MESSAGE_UPDATE':
        // 这些事件由聊天页面内部处理
        break

      case 'MESSAGES_READ':
        // 已读回执
        break

      default:
        // unknown message type, ignore
    }
  }

  /**
   * 处理新消息
   */
  function handleNewMessage(data: WsIncomingMessage) {
    const msg = data.message
    if (!msg) return

    // 如果消息是自己发的，不通知
    if (msg.fromUserId === authStore.me?.userId) {
      return
    }

    // 如果当前正在查看这个会话，不弹出通知（但仍然更新计数）
    const isViewingConversation =
      (route.path === '/chat' || route.path.startsWith('/chat/')) &&
      activeConversationId === msg.conversationId

    const fromName = msg.fromNickName || msg.fromUserName || '新消息'
    const contentPreview = getContentPreview(msg.msgType, msg.content)

    // 更新通知 store
    notificationStore.onNewChatMessage(
      msg.conversationId,
      msg.fromUserId,
      fromName,
      contentPreview
    )

    // 如果不在当前会话，显示通知
    if (!isViewingConversation) {
      // 播放提示音
      playNotificationSound()

      // 浏览器通知（页面不在前台时）
      if (!document.hasFocus() && hasNotificationPermission()) {
        showBrowserNotification(fromName, {
          body: contentPreview,
          tag: `chat-${msg.conversationId}`,
          onClick: () => {
            router.push(`/chat?conversationId=${msg.conversationId}`)
          }
        })
      }

      // 标题闪烁
      if (!document.hasFocus()) {
        startTitleFlash('新消息')
      }
    }
  }

  /**
   * 获取消息内容预览
   */
  function getContentPreview(msgType: string, content: string): string {
    switch (msgType) {
      case 'TEXT':
        return content
      case 'IMAGE':
        return '[图片]'
      case 'QUOTE':
        return '[报价]'
      case 'CONTRACT':
        return '[合同]'
      case 'ATTACHMENT':
        return '[附件]'
      case 'SYSTEM':
        return content || '[系统消息]'
      default:
        return content || '[消息]'
    }
  }

  /**
   * 安排重连
   */
  function scheduleReconnect() {
    if (reconnectTimer) return
    if (!authStore.me) return

    status.value = 'reconnecting'
    reconnectAttempt += 1
    const delay = Math.min(MAX_RECONNECT_DELAY, BASE_RECONNECT_DELAY * Math.pow(2, reconnectAttempt - 1))

    reconnectTimer = window.setTimeout(() => {
      reconnectTimer = null
      connect()
    }, delay)
  }

  /**
   * 启动心跳
   */
  function startPing() {
    stopPing()
    pingInterval = window.setInterval(() => {
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: 'PING' }))
      }
    }, PING_INTERVAL)
  }

  /**
   * 停止心跳
   */
  function stopPing() {
    if (pingInterval) {
      window.clearInterval(pingInterval)
      pingInterval = null
    }
  }

  /**
   * 设置当前活跃会话（用于判断是否需要通知）
   */
  function setActiveConversation(conversationId: number | null) {
    activeConversationId = conversationId
  }

  /**
   * 主动断开
   */
  function disconnect() {
    cleanup()
  }

  // 监听登录状态
  watch(
    () => authStore.me,
    (me) => {
      if (me) {
        // 登录后连接
        connect()
      } else {
        // 退出后断开
        disconnect()
        notificationStore.clearAll()
      }
    },
    { immediate: true }
  )

  // 组件卸载时清理
  onBeforeUnmount(() => {
    // 注意：App.vue 通常不会卸载，这里只是保险
  })

  return {
    status,
    lastError,
    connect,
    disconnect,
    setActiveConversation
  }
}

/**
 * 获取全局 WebSocket 状态（供其他组件使用）
 */
export function getGlobalWsStatus() {
  return status
}

/**
 * 设置当前活跃会话（供聊天页面调用）
 */
export function setActiveConversation(conversationId: number | null) {
  activeConversationId = conversationId
}
