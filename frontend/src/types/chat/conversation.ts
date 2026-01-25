/**
 * Chat Conversation Types
 * Type definitions for conversations and peer groups
 */

/** 标的类型 */
export type SubjectType = 'SUPPLY' | 'NEED'

/** 会话响应 - 来自后端 API */
export interface ChatConversationResponse {
  id: number
  peerUserId: number
  peerUserName?: string
  peerNickName?: string
  peerCompanyName?: string
  subjectType?: SubjectType | string
  subjectId?: number
  subjectSnapshotJson?: string
  lastContent?: string
  lastTime?: string
  unreadCount?: number
}

/** 联系人响应 - 来自后端 API */
export interface ChatPeerResponse {
  peerUserId: number
  peerUserName?: string
  peerNickName?: string
  peerCompanyName?: string
  lastContent?: string
  lastTime?: string
  unreadCount?: number
}

/** 联系人分组 - 按联系人聚合会话 */
export interface PeerGroup {
  peerUserId: number
  peerUserName?: string
  peerNickName?: string
  peerCompanyName?: string
  conversations: ChatConversationResponse[]
  totalUnread: number
  lastTime: string
  lastContent: string
}

/** 时间分组 */
export interface TimeGroup {
  label: string
  peers: PeerGroup[]
}

/** 时间分组标签 */
export const TIME_GROUP_LABELS = {
  TODAY: '今天',
  YESTERDAY: '昨天',
  THIS_WEEK: '本周',
  EARLIER: '更早'
} as const

/** 开启会话请求 */
export interface ChatConversationOpenRequest {
  peerUserId: number
  subjectType: SubjectType | string
  subjectId: number
  subjectSnapshotJson?: string
}

/** 标的快照 - 通用字段 */
export interface SubjectSnapshot {
  // 基础信息
  title?: string
  categoryName?: string
  productName?: string
  companyName?: string
  nickName?: string

  // 定价
  price?: number
  exFactoryPrice?: number
  expectedPrice?: number
  priceType?: number  // 0: 现货, 1: 基差

  // 数量
  quantity?: number
  remainingQuantity?: number

  // 位置和物流
  origin?: string
  shipAddress?: string
  purchaseAddress?: string
  deliveryMode?: string
  deliveryMethod?: string

  // 商务条款
  paymentMethod?: string
  payment_method?: string
  settlementMethod?: string
  invoiceType?: string
  invoice_type?: string
  packaging?: string
  storageMethod?: string
  remark?: string

  // 基差报价相关
  basisQuotes?: Array<{
    contractCode: string
    basisPrice: number
    availableQty?: number
  }>

  // 动态参数
  paramsJson?: string
}

/** 解析标的快照 */
export function parseSubjectSnapshot(json?: string | null): SubjectSnapshot | null {
  if (!json) return null
  try {
    return JSON.parse(json) as SubjectSnapshot
  } catch {
    return null
  }
}

/** 获取标的价格 */
export function getSubjectPrice(snapshot: SubjectSnapshot | null): number | null {
  if (!snapshot) return null
  return snapshot.exFactoryPrice ?? snapshot.expectedPrice ?? snapshot.price ?? null
}

/** 获取标的地址 */
export function getSubjectAddress(snapshot: SubjectSnapshot | null): string {
  if (!snapshot) return ''
  return snapshot.shipAddress || snapshot.purchaseAddress || snapshot.origin || ''
}

/** 获取结算方式 */
export function getPaymentMethod(snapshot: SubjectSnapshot | null): string {
  if (!snapshot) return ''
  return snapshot.paymentMethod || snapshot.payment_method || snapshot.settlementMethod || ''
}

/** 获取发票类型 */
export function getInvoiceType(snapshot: SubjectSnapshot | null): string {
  if (!snapshot) return ''
  return snapshot.invoiceType || snapshot.invoice_type || ''
}

/** 是否为基差交易 */
export function isBasisTrade(snapshot: SubjectSnapshot | null): boolean {
  if (!snapshot) return false
  return snapshot.priceType === 1
}

/** 按联系人聚合会话 */
export function groupConversationsByPeer(
  conversations: ChatConversationResponse[],
  archivedIds: Set<number>
): PeerGroup[] {
  const activeConversations = conversations.filter(c => !archivedIds.has(c.id))
  const map = new Map<number, PeerGroup>()

  activeConversations.forEach(c => {
    const existing = map.get(c.peerUserId)
    if (existing) {
      existing.conversations.push(c)
      existing.totalUnread += c.unreadCount || 0
      if (c.lastTime && c.lastTime > existing.lastTime) {
        existing.lastTime = c.lastTime
        existing.lastContent = c.lastContent || ''
      }
    } else {
      map.set(c.peerUserId, {
        peerUserId: c.peerUserId,
        peerUserName: c.peerUserName,
        peerNickName: c.peerNickName,
        peerCompanyName: c.peerCompanyName,
        conversations: [c],
        totalUnread: c.unreadCount || 0,
        lastTime: c.lastTime || '',
        lastContent: c.lastContent || ''
      })
    }
  })

  return Array.from(map.values())
    .sort((a, b) => (b.lastTime || '').localeCompare(a.lastTime || ''))
}

/** 按时间分组联系人 */
export function groupPeersByTime(peers: PeerGroup[], searchKeyword?: string): TimeGroup[] {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
  const thisWeek = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)

  const groups: TimeGroup[] = [
    { label: TIME_GROUP_LABELS.TODAY, peers: [] },
    { label: TIME_GROUP_LABELS.YESTERDAY, peers: [] },
    { label: TIME_GROUP_LABELS.THIS_WEEK, peers: [] },
    { label: TIME_GROUP_LABELS.EARLIER, peers: [] }
  ]

  // 搜索过滤
  let filteredPeers = peers
  if (searchKeyword) {
    const kw = searchKeyword.toLowerCase()
    filteredPeers = peers.filter(p =>
      `${p.peerCompanyName || ''}${p.peerNickName || ''}${p.peerUserName || ''}`.toLowerCase().includes(kw) ||
      `${p.lastContent || ''}`.toLowerCase().includes(kw)
    )
  }

  filteredPeers.forEach(peer => {
    const d = peer.lastTime ? new Date(peer.lastTime) : new Date(0)
    if (d >= today) {
      groups[0]?.peers.push(peer)
    } else if (d >= yesterday) {
      groups[1]?.peers.push(peer)
    } else if (d >= thisWeek) {
      groups[2]?.peers.push(peer)
    } else {
      groups[3]?.peers.push(peer)
    }
  })

  return groups.filter(g => g.peers.length > 0)
}

/** 获取联系人显示名称 */
export function getPeerDisplayName(peer: Pick<PeerGroup, 'peerNickName' | 'peerUserName' | 'peerCompanyName'>): string {
  const name = peer.peerNickName || peer.peerUserName || '对方'
  if (peer.peerCompanyName) return `${name} · ${peer.peerCompanyName}`
  return name
}

/** 获取头像颜色（基于名称哈希） */
export function getAvatarColor(name?: string): string {
  if (!name) return 'bg-gray-400'
  const colors = [
    'bg-brand-500',
    'bg-autumn-500',
    'bg-blue-500',
    'bg-purple-500',
    'bg-pink-500',
    'bg-orange-500',
    'bg-teal-500',
    'bg-indigo-500'
  ]
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]!
}
