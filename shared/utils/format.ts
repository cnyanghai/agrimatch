/**
 * Pure formatting utilities.
 * No DOM or framework dependencies - safe for use in any JS/TS environment.
 */

// ==================== Date Formatting ====================

/**
 * Format an ISO date string to a localized date display.
 * @param dateStr ISO date string or undefined
 * @param fallback Fallback text when dateStr is empty
 * @returns Formatted date string like "2025-01-15"
 */
export function formatDate(dateStr?: string | null, fallback = '-'): string {
  if (!dateStr) return fallback
  try {
    const d = new Date(dateStr)
    if (isNaN(d.getTime())) return fallback
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${day}`
  } catch {
    return fallback
  }
}

/**
 * Format an ISO date string to date + time display.
 * @param dateStr ISO date string or undefined
 * @param fallback Fallback text when dateStr is empty
 * @returns Formatted datetime string like "2025-01-15 14:30"
 */
export function formatDateTime(dateStr?: string | null, fallback = '-'): string {
  if (!dateStr) return fallback
  try {
    const d = new Date(dateStr)
    if (isNaN(d.getTime())) return fallback
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const h = String(d.getHours()).padStart(2, '0')
    const min = String(d.getMinutes()).padStart(2, '0')
    return `${y}-${m}-${day} ${h}:${min}`
  } catch {
    return fallback
  }
}

/**
 * Format a date as a relative time string (e.g., "3分钟前", "2小时前", "昨天").
 * @param dateStr ISO date string
 * @returns Relative time string in Chinese
 */
export function formatRelativeTime(dateStr?: string | null): string {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    if (isNaN(d.getTime())) return '-'
    const now = Date.now()
    const diff = now - d.getTime()
    const seconds = Math.floor(diff / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)

    if (seconds < 60) return '刚刚'
    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 7) return `${days}天前`
    return formatDate(dateStr)
  } catch {
    return '-'
  }
}

// ==================== Price Formatting ====================

/**
 * Format a price number with currency symbol.
 * @param price Price value
 * @param fallback Fallback text when price is null/undefined
 * @returns Formatted price like "¥3,200"
 */
export function formatPrice(price?: number | null, fallback = '面议'): string {
  if (price === undefined || price === null) return fallback
  return `¥${price.toLocaleString('zh-CN')}`
}

/**
 * Format a price with unit suffix.
 * @param price Price value
 * @param unit Unit string like "元/吨"
 * @param fallback Fallback text
 * @returns Formatted string like "¥3,200/吨"
 */
export function formatPriceWithUnit(price?: number | null, unit?: string, fallback = '面议'): string {
  if (price === undefined || price === null) return fallback
  const unitSuffix = unit ? `/${unit.replace('元/', '')}` : ''
  return `¥${price.toLocaleString('zh-CN')}${unitSuffix}`
}

/**
 * Format a total amount to a display string with "万" for large amounts.
 * @param amount Total amount in yuan
 * @returns Formatted amount like "¥32.5万" or "¥3,200"
 */
export function formatAmount(amount?: number | null, fallback = '-'): string {
  if (amount === undefined || amount === null) return fallback
  if (amount >= 10000) {
    return `¥${(amount / 10000).toFixed(2)}万`
  }
  return `¥${amount.toLocaleString('zh-CN')}`
}

// ==================== Quantity Formatting ====================

/**
 * Format a quantity with unit.
 * @param quantity Quantity value
 * @param unit Unit string like "吨"
 * @param fallback Fallback text
 * @returns Formatted string like "500 吨"
 */
export function formatQuantity(quantity?: number | null, unit = '吨', fallback = '-'): string {
  if (quantity === undefined || quantity === null) return fallback
  return `${quantity} ${unit}`
}

// ==================== Distance Formatting ====================

/**
 * Format distance in kilometers.
 * @param km Distance in km
 * @returns Formatted string like "150km" or "1,200km"
 */
export function formatDistance(km?: number | null, fallback = '-'): string {
  if (km === undefined || km === null) return fallback
  if (km < 1) return `${Math.round(km * 1000)}m`
  return `${Math.round(km).toLocaleString('zh-CN')}km`
}

// ==================== JSON Parse Helpers ====================

/**
 * Safely parse a JSON string.
 * @param json JSON string
 * @returns Parsed value or null on failure
 */
export function safeJsonParse<T = any>(json?: string | null): T | null {
  if (!json) return null
  try {
    return JSON.parse(json) as T
  } catch {
    return null
  }
}

/**
 * Parse bank info JSON string.
 */
export function parseBankInfo(json?: string): { bankName?: string; accountName?: string; accountNo?: string } | null {
  return safeJsonParse(json)
}

// ==================== Text Truncation ====================

/**
 * Truncate text to a maximum length, appending ellipsis if needed.
 * @param text Input text
 * @param maxLen Maximum length
 * @returns Truncated string
 */
export function truncateText(text: string | undefined | null, maxLen = 100): string {
  if (!text) return ''
  if (text.length <= maxLen) return text
  return text.slice(0, maxLen) + '...'
}

// ==================== Phone Masking ====================

/**
 * Mask a phone number for display: 138****1234
 */
export function maskPhone(phone?: string | null): string {
  if (!phone || phone.length < 7) return phone || ''
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}
