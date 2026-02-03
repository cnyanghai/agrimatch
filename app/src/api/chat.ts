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
  read: boolean
  createTime?: string
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
}

export function openConversation(req: ChatConversationOpenRequest) {
  return post<number>('/api/chat/conversations/open', req)
}
