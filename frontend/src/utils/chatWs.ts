import { Capacitor } from '@capacitor/core'

export function buildChatWsUrl(token?: string) {
  let base: string
  if (Capacitor.isNativePlatform()) {
    base = 'wss://www.wogucloud.com/ws/chat'
  } else {
    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
    base = `${protocol}://${window.location.host}/ws/chat`
  }
  if (!token) return base
  const sep = base.includes('?') ? '&' : '?'
  return `${base}${sep}token=${encodeURIComponent(token)}`
}


