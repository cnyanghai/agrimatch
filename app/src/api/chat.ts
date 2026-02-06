import { get, post } from '../utils/request'

export interface ChatConversationResponse {
  id: number
  peerUserId: number
  peerUserName?: string
  peerNickName?: string
  peerCompanyName?: string
  subjectType?: string
  subjectId?: number
  lastContent?: string
  lastTime?: string
  unreadCount?: number
}

export interface ChatMessageResponse {
  id: number
  conversationId?: number
  fromUserId: number
  fromUserName?: string
  fromNickName?: string
  toUserId: number
  content: string
  msgType?: string
  payloadJson?: string
  quoteStatus?: string  // 'OFFERED' | 'ACCEPTED' | 'REJECTED' | 'EXPIRED'
  read: boolean
  createTime?: string
  /** 前端本地状态，不来自后端 */
  _status?: 'pending' | 'sent' | 'failed'
  /** 前端临时 ID */
  _tempId?: string
}

export function listConversations() {
  return get<ChatConversationResponse[]>('/api/chat/conversations')
}

export function getConversation(id: number) {
  return get<ChatConversationResponse>(`/api/chat/conversations/${id}`)
}

export function getConversationMessages(conversationId: number, limit = 50) {
  return get<ChatMessageResponse[]>(`/api/chat/conversations/${conversationId}/messages`, { limit })
}

export function markConversationRead(conversationId: number) {
  return post<void>(`/api/chat/conversations/${conversationId}/read`)
}

export function sendMessage(conversationId: number, content: string) {
  return post<ChatMessageResponse>(`/api/chat/conversations/${conversationId}/messages`, { content })
}

export interface ChatConversationOpenRequest {
  peerUserId: number
  subjectType: string
  subjectId: number
  subjectSnapshotJson?: string
}

export function openConversation(req: ChatConversationOpenRequest) {
  return post<number>('/api/chat/conversations/open', req)
}

export function sendQuoteMessage(conversationId: number, payloadJson: string, content: string) {
  return post<ChatMessageResponse>(`/api/chat/conversations/${conversationId}/messages`, {
    msgType: 'QUOTE',
    content,
    payloadJson,
  })
}

export function confirmChatOffer(messageId: number) {
  return post<void>(`/api/chat/messages/${messageId}/confirm`)
}

export function rejectChatOffer(messageId: number) {
  return post<void>(`/api/chat/messages/${messageId}/reject`)
}

export interface CreateContractFromQuoteRequest {
  quoteMessageId: number
  title?: string
  deliveryDate?: string
  deliveryAddress?: string
  paymentMethod?: string
  terms?: string
}

export function createContractFromQuote(req: CreateContractFromQuoteRequest) {
  return post<number>('/api/contracts/from-quote', req)
}
