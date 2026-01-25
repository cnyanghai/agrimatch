/**
 * Chat Message Types
 * Unified type definitions for chat messages
 */

/** 消息类型 */
export type MessageType = 'TEXT' | 'QUOTE' | 'IMAGE' | 'ATTACHMENT' | 'SYSTEM' | 'CONTRACT'

/** 报价状态 */
export type QuoteStatus = 'OFFERED' | 'ACCEPTED' | 'REJECTED' | 'EXPIRED'

/** 基础消息接口 - 来自后端 API */
export interface ChatMessageResponse {
  id: number
  conversationId?: number
  fromUserId: number
  fromUserName?: string
  fromNickName?: string
  toUserId: number
  toUserName?: string
  toNickName?: string
  msgType?: MessageType | string
  content: string
  payloadJson?: string
  quoteStatus?: QuoteStatus | string
  read: boolean
  createTime?: string
}

/** UI 渲染用消息类型 */
export interface UiMessage {
  id: string | number
  conversationId?: number
  type: 'system' | 'received' | 'sent'
  msgType?: MessageType | string
  content: string
  payloadJson?: string
  quoteStatus?: QuoteStatus | string
  time?: string
  status?: 'pending' | 'sent' | 'failed'
  fromUserId?: number
  toUserId?: number
  /** 基差价格（用于基差报价） */
  basisPrice?: number
  /** 期货合约代码（用于基差报价） */
  contractCode?: string
}

/** 图片消息 payload */
export interface ImagePayload {
  fileUrl: string
  fileName?: string
  size?: number
  width?: number
  height?: number
}

/** 附件消息 payload */
export interface AttachmentPayload {
  fileUrl: string
  fileName: string
  size: number
  mimeType?: string
}

/** 合同消息 payload */
export interface ContractPayload {
  contractId: number
  contractNo: string
  productName: string
  quantity: number | string
  unit: string
  unitPrice: number | string
  basisPrice?: number | string
  contractCode?: string
  totalAmount: number | string
  buyerCompanyId: number
  buyerCompanyName: string
  sellerCompanyId: number
  sellerCompanyName: string
  status: ContractStatus
  buyerSigned: boolean
  sellerSigned: boolean
}

/** 合同状态 */
export type ContractStatus = 0 | 1 | 2 | 3 | 4 | 5

/** 合同状态映射 */
export const CONTRACT_STATUS_MAP: Record<ContractStatus, { label: string; color: string }> = {
  0: { label: '草稿', color: 'text-gray-500 bg-gray-100' },
  1: { label: '待签署', color: 'text-amber-600 bg-amber-50' },
  2: { label: '已签署', color: 'text-brand-600 bg-brand-50' },
  3: { label: '履约中', color: 'text-blue-600 bg-blue-50' },
  4: { label: '已完成', color: 'text-brand-700 bg-brand-100' },
  5: { label: '已取消', color: 'text-red-500 bg-red-50' }
}

/** 报价状态徽章 */
export const QUOTE_STATUS_BADGE: Record<QuoteStatus, { label: string; color: string; bgColor: string }> = {
  OFFERED: { label: '待确认', color: 'text-amber-600', bgColor: 'bg-amber-50' },
  ACCEPTED: { label: '已接受', color: 'text-brand-600', bgColor: 'bg-brand-50' },
  REJECTED: { label: '已拒绝', color: 'text-red-500', bgColor: 'bg-red-50' },
  EXPIRED: { label: '已过期', color: 'text-gray-500', bgColor: 'bg-gray-100' }
}

/** 将后端消息转换为 UI 消息 */
export function toUiMessage(m: ChatMessageResponse, currentUserId: number): UiMessage {
  const isMine = m.fromUserId === currentUserId
  const mt = (m.msgType || 'TEXT').toUpperCase() as MessageType

  let content = m.content || ''
  if (mt === 'QUOTE') content = m.content || '[报价]'
  else if (mt === 'IMAGE') content = '[图片]'
  else if (mt === 'ATTACHMENT') content = '[附件]'
  else if (mt === 'CONTRACT') content = '[合同]'

  return {
    id: m.id,
    conversationId: m.conversationId,
    type: isMine ? 'sent' : 'received',
    msgType: mt,
    content,
    payloadJson: m.payloadJson,
    quoteStatus: m.quoteStatus as QuoteStatus,
    time: formatMessageTime(m.createTime),
    fromUserId: m.fromUserId,
    toUserId: m.toUserId
  }
}

/** 格式化消息时间 */
export function formatMessageTime(createTime?: string): string {
  if (!createTime) return ''
  try {
    const d = new Date(createTime)
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } catch {
    return ''
  }
}
