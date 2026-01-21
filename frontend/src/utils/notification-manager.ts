import { ElNotification } from 'element-plus'
import { ref, onMounted, onUnmounted, inject, type InjectionKey } from 'vue'

export interface Notification {
  id: string
  type: 'info' | 'success' | 'warning' | 'error'
  title: string
  message: string
  timestamp: number
  read: boolean
  action?: {
    label: string
    handler: () => void
  }
}

export const notificationManagerKey: InjectionKey<NotificationManager> = Symbol('notificationManager')

class NotificationManager {
  private eventSource: EventSource | null = null
  private listeners: Array<(notification: Notification) => void> = []

  connect() {
    if (this.eventSource) {
      this.eventSource.close()
    }

    this.eventSource = new EventSource('/api/notifications/sse', {
      withCredentials: true
    })

    this.eventSource.onmessage = (event) => {
      try {
        const notification = JSON.parse(event.data)
        this.showNotification(notification)
      } catch (error) {
        console.error('[NotificationManager] Failed to parse SSE message:', error)
      }
    }

    this.eventSource.onerror = (error) => {
      console.error('[NotificationManager] SSE error:', error)
      if (this.eventSource?.readyState === EventSource.CLOSED) {
        this.eventSource = null
      }
    }
  }

  disconnect() {
    if (this.eventSource) {
      this.eventSource.close()
      this.eventSource = null
    }
  }

  showNotification(notification: Notification): void {
    this.notifyListeners(notification)

    if ('Notification' in window && Notification.permission === 'granted') {
      const desktopNotif = new Notification(notification.title, {
        body: notification.message,
        icon: '/favicon.ico',
        tag: notification.id
      })

      desktopNotif.onclick = () => {
        window.focus()
        if (notification.action) {
          notification.action.handler()
        }
      }
    } else {
      ElNotification({
        title: notification.title,
        message: notification.message,
        type: notification.type === 'error' ? 'error' : notification.type === 'warning' ? 'warning' : notification.type === 'success' ? 'success' : 'info',
        duration: 3000,
        showClose: true,
        onClick: () => {
          if (notification.action) {
            notification.action.handler()
          }
        }
      })
    }
  }

  addListener(listener: (notification: Notification) => void) {
    this.listeners.push(listener)
  }

  removeListener(listener: (notification: Notification) => void) {
    const index = this.listeners.indexOf(listener)
    if (index > -1) {
      this.listeners.splice(index, 1)
    }
  }

  private notifyListeners(notification: Notification) {
    this.listeners.forEach(listener => listener(notification))
  }

  async requestPermission(): Promise<void> {
    if ('Notification' in window && Notification.permission === 'default') {
      await Notification.requestPermission()
    }
  }
}

export function useNotificationManager() {
  const notifications = ref<Notification[]>([])
  const unreadCount = ref(0)
  const manager = inject(notificationManagerKey)

  onMounted(() => {
    if (manager) {
      manager.addListener((notification) => {
        notifications.value.unshift(notification)
        unreadCount.value++
      })
    }
  })

  onUnmounted(() => {
    if (manager) {
      const listener = (notification: Notification) => {
        const index = notifications.value.findIndex(n => n.id === notification.id)
        if (index > -1) {
          notifications.value.splice(index, 1)
        }
      }
      manager.removeListener(listener)
    }
  })

  function markAsRead(id: string): void {
    const notification = notifications.value.find(n => n.id === id)
    if (notification && !notification.read) {
      notification.read = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  }

  function markAllAsRead(): void {
    notifications.value.forEach(n => n.read = true)
    unreadCount.value = 0
  }

  function removeNotification(id: string): void {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      notifications.value.splice(index, 1)
      if (!notifications.value[index]?.read) {
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }
    }
  }

  async function requestNotificationPermission(): Promise<void> {
    await manager?.requestPermission()
  }

  return {
    notifications,
    unreadCount,
    markAsRead,
    markAllAsRead,
    removeNotification,
    requestNotificationPermission
  }
}
