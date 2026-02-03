/**
 * Chat module - Types and API path constants.
 */

// ==================== Types ====================

export type ChatMsgType = 'TEXT' | 'QUOTE' | 'SYSTEM' | 'ATTACHMENT' | string
export type ChatQuoteStatus = 'OFFERED' | 'ACCEPTED' | 'REJECTED' | 'EXPIRED' | string
export type ChatSubjectType = 'SUPPLY' | 'NEED' | string

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
  msgType?: ChatMsgType
  content: string
  payloadJson?: string
  quoteStatus?: ChatQuoteStatus
  read: boolean
  readAt?: string
  createTime?: string
}

export interface ChatConversationResponse {
  id: number
  peerUserId: number
  peerUserName?: string
  peerNickName?: string
  peerCompanyName?: string
  subjectType?: ChatSubjectType
  subjectId?: number
  subjectSnapshotJson?: string
  /** 会话发起人userId（用于判断买方/卖方角色） */
  initiatorUserId?: number
  lastContent?: string
  lastTime?: string
  unreadCount?: number
}

export interface ChatConversationOpenRequest {
  peerUserId: number
  subjectType: ChatSubjectType
  subjectId: number
  subjectSnapshotJson?: string
}

// ==================== API Path Constants ====================

export const CHAT_API = {
  /** GET - List chat peers */
  PEERS: '/api/chat/peers',
  /** GET - Get chat history with peer */
  HISTORY: '/api/chat/history',
  /** POST - Mark chat as read */
  MARK_READ: '/api/chat/read',
  /** POST - Open conversation */
  OPEN_CONVERSATION: '/api/chat/conversations/open',
  /** GET - List conversations */
  LIST_CONVERSATIONS: '/api/chat/conversations',
  /** GET - Get conversation by ID: /api/chat/conversations/:id */
  GET_CONVERSATION: (conversationId: number) => `/api/chat/conversations/${conversationId}`,
  /** GET - Get conversation messages: /api/chat/conversations/:id/messages */
  CONVERSATION_MESSAGES: (conversationId: number) => `/api/chat/conversations/${conversationId}/messages`,
  /** POST - Mark conversation read: /api/chat/conversations/:id/read */
  MARK_CONVERSATION_READ: (conversationId: number) => `/api/chat/conversations/${conversationId}/read`,
  /** POST - Confirm chat offer: /api/chat/messages/:id/confirm */
  CONFIRM_OFFER: (messageId: number) => `/api/chat/messages/${messageId}/confirm`,
  /** POST - Reject chat offer: /api/chat/messages/:id/reject */
  REJECT_OFFER: (messageId: number) => `/api/chat/messages/${messageId}/reject`,
} as const
