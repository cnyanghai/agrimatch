/**
 * 浏览器原生通知工具
 */

let permissionGranted = false

/**
 * 请求通知权限
 */
export async function requestNotificationPermission(): Promise<boolean> {
  if (!('Notification' in window)) {
    return false
  }

  if (Notification.permission === 'granted') {
    permissionGranted = true
    return true
  }

  if (Notification.permission === 'denied') {
    return false
  }

  try {
    const permission = await Notification.requestPermission()
    permissionGranted = permission === 'granted'
    return permissionGranted
  } catch {
    return false
  }
}

/**
 * 检查是否有通知权限
 */
export function hasNotificationPermission(): boolean {
  return 'Notification' in window && Notification.permission === 'granted'
}

/**
 * 显示浏览器通知
 */
export function showBrowserNotification(
  title: string,
  options?: {
    body?: string
    icon?: string
    tag?: string
    onClick?: () => void
  }
): Notification | null {
  if (!hasNotificationPermission()) {
    return null
  }

  try {
    const notification = new Notification(title, {
      body: options?.body,
      icon: options?.icon || '/favicon.ico',
      tag: options?.tag,
      silent: true // 不播放系统声音（我们自己播放）
    })

    if (options?.onClick) {
      notification.onclick = () => {
        window.focus()
        options.onClick?.()
        notification.close()
      }
    }

    // 5 秒后自动关闭
    setTimeout(() => {
      notification.close()
    }, 5000)

    return notification
  } catch {
    return null
  }
}

/**
 * 标题闪烁（当页面不在前台时）
 */
let titleFlashInterval: number | null = null
let originalTitle = ''

export function startTitleFlash(message: string = '新消息') {
  if (titleFlashInterval) return
  if (document.hasFocus()) return // 页面在前台不闪烁

  originalTitle = document.title
  let showMessage = true

  titleFlashInterval = window.setInterval(() => {
    document.title = showMessage ? `【${message}】${originalTitle}` : originalTitle
    showMessage = !showMessage
  }, 1000)

  // 页面获得焦点时停止闪烁
  const stopFlash = () => {
    stopTitleFlash()
    window.removeEventListener('focus', stopFlash)
  }
  window.addEventListener('focus', stopFlash)
}

export function stopTitleFlash() {
  if (titleFlashInterval) {
    window.clearInterval(titleFlashInterval)
    titleFlashInterval = null
    document.title = originalTitle
  }
}
