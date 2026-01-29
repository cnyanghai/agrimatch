/**
 * 全局通知状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/** 通知类型 */
export interface Notification {
  id: string
  type: 'chat' | 'system' | 'contract'
  title: string
  content: string
  fromUserId?: number
  fromUserName?: string
  conversationId?: number
  timestamp: Date
  read: boolean
}

/** 未读消息统计 */
export interface UnreadStats {
  total: number
  chat: number
  system: number
}

export const useNotificationStore = defineStore('notification', () => {
  // 通知列表
  const notifications = ref<Notification[]>([])

  // 未读聊天消息数（按会话统计）
  const unreadChatMap = ref<Map<number, number>>(new Map())

  // 是否显示 Toast
  const showToast = ref(false)
  const currentToast = ref<Notification | null>(null)

  // 计算属性：总未读数
  const unreadTotal = computed(() => {
    let total = 0
    unreadChatMap.value.forEach(count => {
      total += count
    })
    return total
  })

  // 计算属性：未读统计
  const unreadStats = computed<UnreadStats>(() => ({
    total: unreadTotal.value,
    chat: unreadTotal.value,
    system: 0
  }))

  /**
   * 添加新通知
   */
  function addNotification(notification: Omit<Notification, 'id' | 'timestamp' | 'read'>) {
    const newNotification: Notification = {
      ...notification,
      id: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      timestamp: new Date(),
      read: false
    }

    notifications.value.unshift(newNotification)

    // 限制通知数量，最多保留 50 条
    if (notifications.value.length > 50) {
      notifications.value = notifications.value.slice(0, 50)
    }

    // 显示 Toast
    showToastNotification(newNotification)

    return newNotification
  }

  /**
   * 收到新聊天消息
   */
  function onNewChatMessage(
    conversationId: number,
    fromUserId: number,
    fromUserName: string,
    content: string
  ) {
    // 更新未读计数
    const current = unreadChatMap.value.get(conversationId) || 0
    unreadChatMap.value.set(conversationId, current + 1)

    // 添加通知
    addNotification({
      type: 'chat',
      title: fromUserName || '新消息',
      content: content.length > 50 ? content.slice(0, 50) + '...' : content,
      fromUserId,
      fromUserName,
      conversationId
    })
  }

  /**
   * 标记会话已读
   */
  function markConversationRead(conversationId: number) {
    unreadChatMap.value.delete(conversationId)
  }

  /**
   * 标记所有已读
   */
  function markAllRead() {
    unreadChatMap.value.clear()
    notifications.value.forEach(n => {
      n.read = true
    })
  }

  /**
   * 显示 Toast 通知
   */
  function showToastNotification(notification: Notification) {
    currentToast.value = notification
    showToast.value = true

    // 5 秒后自动关闭
    setTimeout(() => {
      if (currentToast.value?.id === notification.id) {
        hideToast()
      }
    }, 5000)
  }

  /**
   * 隐藏 Toast
   */
  function hideToast() {
    showToast.value = false
    currentToast.value = null
  }

  /**
   * 清空所有通知
   */
  function clearAll() {
    notifications.value = []
    unreadChatMap.value.clear()
  }

  /**
   * 设置初始未读数（从 API 获取）
   */
  function setInitialUnread(conversationId: number, count: number) {
    if (count > 0) {
      unreadChatMap.value.set(conversationId, count)
    } else {
      unreadChatMap.value.delete(conversationId)
    }
  }

  return {
    // 状态
    notifications,
    unreadChatMap,
    showToast,
    currentToast,

    // 计算属性
    unreadTotal,
    unreadStats,

    // 方法
    addNotification,
    onNewChatMessage,
    markConversationRead,
    markAllRead,
    showToastNotification,
    hideToast,
    clearAll,
    setInitialUnread
  }
})
