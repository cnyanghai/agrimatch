/**
 * Chat Conversations Composable
 * Manages conversation list, archiving, and peer grouping
 */

import { ref, computed, watch } from 'vue'
import { listChatConversations, markConversationRead } from '../../api/chat'
import type { ChatConversationResponse, PeerGroup, TimeGroup } from '../../types/chat/conversation'
import {
  groupConversationsByPeer,
  groupPeersByTime,
  getPeerDisplayName,
  parseSubjectSnapshot,
  isBasisTrade
} from '../../types/chat/conversation'

const ARCHIVED_STORAGE_KEY = 'chat_archived_conversations'

/**
 * Chat Conversations Composable
 */
export function useChatConversations() {
  const conversations = ref<ChatConversationResponse[]>([])
  const archivedIds = ref<Set<number>>(new Set())
  const loading = ref(false)
  const error = ref<string | null>(null)

  const activeConversationId = ref<number | null>(null)
  const activePeerId = ref<number | null>(null)
  const searchKeyword = ref('')

  // ==================== Archived Management ====================

  /**
   * 从 localStorage 加载已归档会话
   */
  function loadArchivedFromStorage(): void {
    try {
      const stored = localStorage.getItem(ARCHIVED_STORAGE_KEY)
      if (stored) {
        const ids = JSON.parse(stored) as number[]
        archivedIds.value = new Set(ids)
      }
    } catch {
      archivedIds.value = new Set()
    }
  }

  /**
   * 保存已归档会话到 localStorage
   */
  function saveArchivedToStorage(): void {
    try {
      const ids = Array.from(archivedIds.value)
      localStorage.setItem(ARCHIVED_STORAGE_KEY, JSON.stringify(ids))
    } catch {
      // ignore
    }
  }

  /**
   * 归档会话
   */
  function archiveConversation(conversationId: number): void {
    archivedIds.value.add(conversationId)
    saveArchivedToStorage()
  }

  /**
   * 恢复归档会话
   */
  function restoreConversation(conversationId: number): void {
    archivedIds.value.delete(conversationId)
    saveArchivedToStorage()
  }

  /**
   * 检查会话是否已归档
   */
  function isArchived(conversationId: number): boolean {
    return archivedIds.value.has(conversationId)
  }

  // ==================== Data Loading ====================

  /**
   * 加载会话列表
   */
  async function loadConversations(): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const res = await listChatConversations()
      if (res.code === 0 && res.data) {
        conversations.value = res.data
      } else {
        error.value = res.message || '加载会话失败'
      }
    } catch (e: any) {
      console.error('[useChatConversations] Failed to load:', e)
      error.value = e.message || '加载会话失败'
    } finally {
      loading.value = false
    }
  }

  /**
   * 标记会话已读
   */
  async function markRead(conversationId: number): Promise<void> {
    try {
      await markConversationRead(conversationId)
      // 更新本地状态
      const conv = conversations.value.find(c => c.id === conversationId)
      if (conv) {
        conv.unreadCount = 0
      }
    } catch (e) {
      console.error('[useChatConversations] Failed to mark read:', e)
    }
  }

  /**
   * 更新会话最后消息
   */
  function updateLastMessage(
    conversationId: number,
    content: string,
    time: string
  ): void {
    const conv = conversations.value.find(c => c.id === conversationId)
    if (conv) {
      conv.lastContent = content
      conv.lastTime = time
    }
  }

  /**
   * 增加未读计数
   */
  function incrementUnread(conversationId: number): void {
    const conv = conversations.value.find(c => c.id === conversationId)
    if (conv) {
      conv.unreadCount = (conv.unreadCount || 0) + 1
    }
  }

  // ==================== Computed Properties ====================

  /**
   * 活跃（未归档）会话列表
   */
  const activeConversations = computed(() => {
    return conversations.value.filter(c => !archivedIds.value.has(c.id))
  })

  /**
   * 已归档会话列表
   */
  const archivedConversations = computed(() => {
    return conversations.value.filter(c => archivedIds.value.has(c.id))
  })

  /**
   * 按联系人分组（仅活跃会话）
   */
  const groupedByPeer = computed<PeerGroup[]>(() => {
    return groupConversationsByPeer(conversations.value, archivedIds.value)
  })

  /**
   * 按时间分组（支持搜索过滤）
   */
  const timeGroupedPeers = computed<TimeGroup[]>(() => {
    return groupPeersByTime(groupedByPeer.value, searchKeyword.value)
  })

  /**
   * 当前选中联系人的会话列表
   */
  const currentPeerConversations = computed(() => {
    if (!activePeerId.value) return []
    const peer = groupedByPeer.value.find(p => p.peerUserId === activePeerId.value)
    return peer?.conversations || []
  })

  /**
   * 当前选中的会话
   */
  const currentConversation = computed(() => {
    return conversations.value.find(c => c.id === activeConversationId.value) || null
  })

  /**
   * 当前会话的对方显示名称
   */
  const currentPeerDisplayName = computed(() => {
    const conv = currentConversation.value
    if (!conv) return ''
    return getPeerDisplayName({
      peerNickName: conv.peerNickName,
      peerUserName: conv.peerUserName,
      peerCompanyName: conv.peerCompanyName
    })
  })

  /**
   * 当前会话是否为基差交易
   */
  const currentIsBasisTrade = computed(() => {
    const conv = currentConversation.value
    if (!conv) return false
    const snapshot = parseSubjectSnapshot(conv.subjectSnapshotJson)
    return isBasisTrade(snapshot)
  })

  /**
   * 总未读数
   */
  const totalUnread = computed(() => {
    return activeConversations.value.reduce((sum, c) => sum + (c.unreadCount || 0), 0)
  })

  // ==================== Selection ====================

  /**
   * 选择联系人
   */
  function selectPeer(peer: PeerGroup): void {
    activePeerId.value = peer.peerUserId
    // 自动选择该联系人的第一个会话
    if (peer.conversations.length > 0) {
      const firstConv = peer.conversations.sort((a, b) =>
        (b.lastTime || '').localeCompare(a.lastTime || '')
      )[0]
      if (firstConv) {
        activeConversationId.value = firstConv.id
      }
    }
  }

  /**
   * 切换会话
   */
  function switchConversation(conversationId: number): void {
    activeConversationId.value = conversationId
    // 更新 activePeerId
    const conv = conversations.value.find(c => c.id === conversationId)
    if (conv) {
      activePeerId.value = conv.peerUserId
    }
  }

  /**
   * 清除选择
   */
  function clearSelection(): void {
    activeConversationId.value = null
    activePeerId.value = null
  }

  /**
   * 根据路由参数初始化选择
   */
  function initFromRoute(conversationId?: number | string): void {
    if (!conversationId) return

    const id = typeof conversationId === 'string' ? parseInt(conversationId, 10) : conversationId
    if (isNaN(id)) return

    const conv = conversations.value.find(c => c.id === id)
    if (conv) {
      activeConversationId.value = id
      activePeerId.value = conv.peerUserId
    }
  }

  // ==================== Search ====================

  /**
   * 设置搜索关键词
   */
  function setSearchKeyword(keyword: string): void {
    searchKeyword.value = keyword
  }

  /**
   * 清除搜索
   */
  function clearSearch(): void {
    searchKeyword.value = ''
  }

  // ==================== Initialization ====================

  /**
   * 初始化
   */
  function init(): void {
    loadArchivedFromStorage()
  }

  return {
    // 状态
    conversations,
    archivedIds,
    loading,
    error,
    activeConversationId,
    activePeerId,
    searchKeyword,

    // 归档管理
    archiveConversation,
    restoreConversation,
    isArchived,

    // 数据加载
    loadConversations,
    markRead,
    updateLastMessage,
    incrementUnread,

    // Computed
    activeConversations,
    archivedConversations,
    groupedByPeer,
    timeGroupedPeers,
    currentPeerConversations,
    currentConversation,
    currentPeerDisplayName,
    currentIsBasisTrade,
    totalUnread,

    // 选择
    selectPeer,
    switchConversation,
    clearSelection,
    initFromRoute,

    // 搜索
    setSearchKeyword,
    clearSearch,

    // 初始化
    init
  }
}

export type UseChatConversations = ReturnType<typeof useChatConversations>
