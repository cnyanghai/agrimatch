export function buildChatWsUrl(token?: string) {
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  // Always connect to same-origin and rely on dev proxy (/ws -> :8080).
  // This avoids "localhost:8080" issues when frontend is opened from Windows while backend runs in WSL.
  const base = `${protocol}://${window.location.host}/ws/chat`
  if (!token) return base
  const sep = base.includes('?') ? '&' : '?'
  return `${base}${sep}token=${encodeURIComponent(token)}`
}


