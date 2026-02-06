/**
 * 全局格式化工具函数
 */

/** 格式化价格 */
export function formatPrice(price?: number | null, unit?: string): string {
  if (price === null || price === undefined) return '面议'
  if (price === 0) return '面议'
  const str = price >= 1000
    ? '¥' + price.toLocaleString('zh-CN', { minimumFractionDigits: price % 1 === 0 ? 0 : 2, maximumFractionDigits: 2 })
    : '¥' + price.toFixed(2)
  return unit ? `${str}/${unit}` : str
}

/** 格式化金额（合同总额等） */
export function formatAmount(amount?: number | null): string {
  if (amount === null || amount === undefined || amount === 0) return '待定'
  return '¥' + amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/** 格式化相对时间 */
export function formatRelativeTime(time?: string | null): string {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 0) return '刚刚'
  const seconds = Math.floor(diff / 1000)
  if (seconds < 60) return '刚刚'
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  const month = d.getMonth() + 1
  const day = d.getDate()
  if (d.getFullYear() === now.getFullYear()) {
    return `${month}月${day}日`
  }
  return `${d.getFullYear()}/${month}/${day}`
}

/** 格式化日期 YYYY-MM-DD */
export function formatDate(time?: string | null): string {
  if (!time) return '-'
  return time.slice(0, 10)
}

/** 格式化日期时间 */
export function formatDateTime(time?: string | null): string {
  if (!time) return '-'
  return time.slice(0, 16).replace('T', ' ')
}

/** 格式化聊天时间 HH:MM */
export function formatChatTime(time?: string | null): string {
  if (!time) return ''
  return time.slice(11, 16)
}

/** 格式化数量 + 单位 */
export function formatQuantity(qty?: number | null, unit?: string): string {
  if (qty === null || qty === undefined) return '-'
  const str = qty >= 10000
    ? (qty / 10000).toFixed(1) + '万'
    : qty.toLocaleString('zh-CN')
  return unit ? `${str}${unit}` : str
}

/**
 * 格式化剩余时长
 * - 剩余>1天：返回 { text: "剩余X天", level: "normal" }
 * - 剩余<1天且>1小时：返回 { text: "剩余X小时", level: "normal" }
 * - 剩余<1小时且>0：返回 { text: "即将到期", level: "warning" }
 * - 已过期：返回 { text: "已过期", level: "expired" }
 * - 为空：返回 null（不显示）
 */
export function formatRemainingTime(
  expireTime?: string | null,
): { text: string; level: 'normal' | 'warning' | 'expired' } | null {
  if (!expireTime) return null

  const now = Date.now()
  const expire = new Date(expireTime).getTime()
  if (isNaN(expire)) return null

  const diff = expire - now

  if (diff <= 0) {
    return { text: '已过期', level: 'expired' }
  }

  const hours = diff / (1000 * 60 * 60)
  const days = Math.floor(hours / 24)

  if (days >= 1) {
    return { text: `剩余${days}天`, level: 'normal' }
  }
  if (hours >= 1) {
    return { text: `剩余${Math.floor(hours)}小时`, level: 'normal' }
  }
  return { text: '即将到期', level: 'warning' }
}
