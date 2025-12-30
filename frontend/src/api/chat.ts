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
  fromUserId: number
  fromUserName?: string
  fromNickName?: string
  toUserId: number
  toUserName?: string
  toNickName?: string
  content: string
  read: boolean
  createTime?: string
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


