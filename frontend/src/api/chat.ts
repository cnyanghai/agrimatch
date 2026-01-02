import { http, type Result } from './http'

export interface ChatPeerResponse {
  peerUserId: number
  peerUserName?: string
  peerNickName?: string
  peerCompanyName?: string
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
  toUserName?: string
  toNickName?: string
  msgType?: 'TEXT' | 'QUOTE' | 'SYSTEM' | 'ATTACHMENT' | string
  content: string
  payloadJson?: string
  read: boolean
  createTime?: string
}

export interface ChatConversationResponse {
  id: number
  peerUserId: number
  peerUserName?: string
  peerNickName?: string
  peerCompanyName?: string
  subjectType?: 'SUPPLY' | 'NEED' | string
  subjectId?: number
  subjectSnapshotJson?: string
  lastContent?: string
  lastTime?: string
  unreadCount?: number
}

export interface ChatConversationOpenRequest {
  peerUserId: number
  subjectType: 'SUPPLY' | 'NEED' | string
  subjectId: number
  subjectSnapshotJson?: string
}

export async function listChatPeers() {
  const { data } = await http.get<Result<ChatPeerResponse[]>>('/api/chat/peers')
  return data
}

export async function getChatHistory(peerUserId: number, limit = 50) {
  const { data } = await http.get<Result<ChatMessageResponse[]>>('/api/chat/history', { params: { peerUserId, limit } })
  return data
}

export async function markChatRead(peerUserId: number) {
  const { data } = await http.post<Result<void>>('/api/chat/read', null, { params: { peerUserId } })
  return data
}

export async function openChatConversation(req: ChatConversationOpenRequest) {
  const { data } = await http.post<Result<number>>('/api/chat/conversations/open', req)
  return data
}

export async function listChatConversations() {
  const { data } = await http.get<Result<ChatConversationResponse[]>>('/api/chat/conversations')
  return data
}

export async function getConversationMessages(conversationId: number, limit = 50) {
  const { data } = await http.get<Result<ChatMessageResponse[]>>(`/api/chat/conversations/${conversationId}/messages`, { params: { limit } })
  return data
}

export async function markConversationRead(conversationId: number) {
  const { data } = await http.post<Result<void>>(`/api/chat/conversations/${conversationId}/read`)
  return data
}


