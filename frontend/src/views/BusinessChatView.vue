<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick, onBeforeUnmount, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../store/auth'
import { Search, Picture, Document, Position, Present, ChatDotRound, Star, StarFilled, Loading } from '@element-plus/icons-vue'
import { giftPoints } from '../api/points'
import { uploadImage, uploadAttachment, formatFileSize, type ImagePayload, type AttachmentPayload } from '../api/file'
import { useRoute, useRouter } from 'vue-router'
import { getConversationMessages, listChatConversations, markConversationRead, confirmChatOffer, type ChatConversationResponse, type ChatMessageResponse } from '../api/chat'
import { followUser, unfollowUser, checkFollowStatus } from '../api/follow'
import NegotiationPanel, { type QuoteFields } from '../components/chat/NegotiationPanel.vue'
import BasisBargainPanel from '../components/chat/BasisBargainPanel.vue'
import ChatSubjectCard from '../components/chat/ChatSubjectCard.vue'
import ContractDraftModal from '../components/contract/ContractDraftModal.vue'
import ContractCard from '../components/chat/ContractCard.vue'
import ContractSignModal from '../components/contract/ContractSignModal.vue'
import { buildChatWsUrl } from '../utils/chatWs'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const messageInput = ref('')
const chatContainerRef = ref<HTMLElement | null>(null)

// 当前选中的会话
const activeConversationId = ref<number | null>(null)

// 当前选中的联系人 ID
const activePeerId = ref<number | null>(null)

// 会话列表
const conversations = ref<ChatConversationResponse[]>([])

// 已归档的会话 ID 列表（存储在 localStorage）
const archivedConversationIds = ref<Set<number>>(new Set())
const ARCHIVED_STORAGE_KEY = 'chat_archived_conversations'

// 加载已归档会话列表
function loadArchivedConversations() {
  try {
    const stored = localStorage.getItem(ARCHIVED_STORAGE_KEY)
    if (stored) {
      const ids = JSON.parse(stored) as number[]
      archivedConversationIds.value = new Set(ids)
    }
  } catch {
    archivedConversationIds.value = new Set()
  }
}

// 保存已归档会话列表
function saveArchivedConversations() {
  try {
    const ids = Array.from(archivedConversationIds.value)
    localStorage.setItem(ARCHIVED_STORAGE_KEY, JSON.stringify(ids))
  } catch {
    // ignore
  }
}

// 归档会话
function archiveConversation(conversationId: number) {
  archivedConversationIds.value.add(conversationId)
  saveArchivedConversations()
  
  // 如果归档的是当前会话，切换到下一个
  if (activeConversationId.value === conversationId) {
    const remaining = currentPeerConversations.value.filter(c => c.id !== conversationId)
    if (remaining.length > 0 && remaining[0]) {
      switchConversation(remaining[0].id)
    } else {
      // 该联系人没有其他会话了，切换到下一个联系人
      activeConversationId.value = null
      const otherPeers = groupedByPeer.value.filter(p => p.peerUserId !== activePeerId.value)
      if (otherPeers.length > 0 && otherPeers[0]) {
        selectPeer(otherPeers[0])
      } else {
        activePeerId.value = null
        messages.value = []
      }
    }
  }
  
  ElMessage.success('会话已归档')
}

// 恢复归档的会话
function restoreConversation(conversationId: number) {
  archivedConversationIds.value.delete(conversationId)
  saveArchivedConversations()
  ElMessage.success('会话已恢复')
}

// 显示已归档会话的弹窗
const showArchivedModal = ref(false)

// 已归档的会话列表（用于弹窗显示）
const archivedConversations = computed(() => {
  return conversations.value.filter(c => archivedConversationIds.value.has(c.id))
})

// 活跃的会话列表（过滤掉已归档的）
const activeConversations = computed(() => {
  return conversations.value.filter(c => !archivedConversationIds.value.has(c.id))
})

// 联系人聚合类型
interface PeerGroup {
  peerUserId: number
  peerUserName?: string
  peerNickName?: string
  peerCompanyName?: string
  conversations: ChatConversationResponse[]
  totalUnread: number
  lastTime: string
  lastContent: string
}

// 按联系人聚合会话（只聚合活跃的会话，排除已归档的）
const groupedByPeer = computed<PeerGroup[]>(() => {
  const map = new Map<number, PeerGroup>()
  
  activeConversations.value.forEach(c => {
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
})

// 按时间分组
interface TimeGroup {
  label: string
  peers: PeerGroup[]
}

const timeGroupedPeers = computed<TimeGroup[]>(() => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
  const thisWeek = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
  
  const groups: TimeGroup[] = [
    { label: '今天', peers: [] },
    { label: '昨天', peers: [] },
    { label: '本周', peers: [] },
    { label: '更早', peers: [] }
  ]
  
  // 如果有搜索关键词，先过滤
  let peers = groupedByPeer.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    peers = peers.filter(p =>
      `${p.peerCompanyName || ''}${p.peerNickName || ''}${p.peerUserName || ''}`.toLowerCase().includes(kw) ||
      `${p.lastContent || ''}`.toLowerCase().includes(kw)
    )
  }

  peers.forEach(peer => {
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
})

// 当前选中联系人的会话列表
const currentPeerConversations = computed(() => {
  if (!activePeerId.value) return []
  const peer = groupedByPeer.value.find(p => p.peerUserId === activePeerId.value)
  return peer?.conversations || []
})

// 消息列表
type UiMessage = {
  id: string | number
  type: 'system' | 'received' | 'sent'
  msgType?: string
  content: string
  payloadJson?: string
  quoteStatus?: string
  basisPrice?: number
  contractCode?: string
  time?: string
  status?: 'pending' | 'sent'
  fromUserId?: number
  toUserId?: number
  conversationId?: number
}
const messages = ref<UiMessage[]>([])

const ws = ref<WebSocket | null>(null)
const wsConnected = ref(false)
let wsReconnectTimer: number | null = null
let wsReconnectAttempt = 0
const wsCloseHinted = ref(false)
const canRealtime = computed(() => !!auth.me || !!auth.token)

function cleanupWs() {
  if (wsReconnectTimer) {
    window.clearTimeout(wsReconnectTimer)
    wsReconnectTimer = null
  }
  wsReconnectAttempt = 0
  wsConnected.value = false
  wsCloseHinted.value = false
  if (ws.value) {
    try {
      ws.value.close()
    } catch {
      // ignore
    }
    ws.value = null
  }
}

// Layout: desktop side panel + mobile drawer
const sidePanelOpen = ref(true)
const quotePopoverVisible = ref(false)
const negotiationDrawerOpen = ref(false)
const isDesktopXl = ref(false)
const subjectDialogOpen = ref(false)
let xlMql: MediaQueryList | null = null
let xlListener: ((e: MediaQueryListEvent) => void) | null = null

// 搜索关键词
const searchKeyword = ref('')

// 赠送积分对话框
const giftDialogVisible = ref(false)
const giftForm = reactive({
  points: 10,
  remark: ''
})
const giftLoading = ref(false)

// 起草合同弹窗
const contractDraftVisible = ref(false)
const draftQuoteMessageId = ref<number | null>(null)
const draftQuoteData = ref<QuoteFields | null>(null)

// 文件上传
const imageInputRef = ref<HTMLInputElement | null>(null)
const attachmentInputRef = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadType = ref<'image' | 'attachment'>('image')

const currentConversation = computed(() => {
  return conversations.value.find(c => c.id === activeConversationId.value) || null
})

const isBasisTrade = computed(() => {
  try {
    const json = currentConversation.value?.subjectSnapshotJson
    if (!json) return false
    const obj = JSON.parse(json)
    return obj.priceType === 1
  } catch {
    return false
  }
})

const peerDisplayName = computed(() => {
  const c = currentConversation.value
  if (!c) return ''
  const name = c.peerNickName || c.peerUserName || '对方'
  if (c.peerCompanyName) return `${name} · ${c.peerCompanyName}`
  return name
})

// 关注状态
const isFollowingPeer = ref(false)
const followLoading = ref(false)

// 检查当前聊天对象的关注状态
async function checkPeerFollowStatus() {
  const c = currentConversation.value
  if (!c || !c.peerUserId) {
    isFollowingPeer.value = false
    return
  }
  try {
    const r = await checkFollowStatus(c.peerUserId)
    if (r.code === 0) {
      isFollowingPeer.value = r.data || false
    }
  } catch {
    isFollowingPeer.value = false
  }
}

// 关注/取消关注当前聊天对象
async function toggleFollowPeer() {
  const c = currentConversation.value
  if (!c || !c.peerUserId) {
    ElMessage.warning('无法关注该用户')
    return
  }
  
  followLoading.value = true
  try {
    if (isFollowingPeer.value) {
      await unfollowUser(c.peerUserId)
      isFollowingPeer.value = false
      ElMessage.success(`已取消关注 ${peerDisplayName.value}`)
    } else {
      await followUser(c.peerUserId)
      isFollowingPeer.value = true
      ElMessage.success(`已关注 ${peerDisplayName.value}`)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    followLoading.value = false
  }
}

function formatTime(ts?: string) {
  if (!ts) return ''
  const d = new Date(ts)
  if (Number.isNaN(d.getTime())) return ts
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

// 格式化相对时间（用于会话列表）
function formatRelativeTime(ts?: string) {
  if (!ts) return ''
  const d = new Date(ts)
  if (Number.isNaN(d.getTime())) return ''
  
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
  
  if (d >= today) {
    return d.toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (d >= yesterday) {
    return '昨天'
  } else {
    return d.toLocaleString('zh-CN', { month: 'numeric', day: 'numeric' })
  }
}

// 获取标的简称
function getSubjectShortName(c: ChatConversationResponse): string {
  try {
    if (c.subjectSnapshotJson) {
      const obj = JSON.parse(c.subjectSnapshotJson)
      return obj.productName || obj.title || ''
    }
  } catch {}
  const st = (c.subjectType || '').toUpperCase()
  return st === 'SUPPLY' ? '供应' : st === 'NEED' ? '采购' : '会话'
}

function wsUrl() {
  return buildChatWsUrl(auth.token || undefined)
}

function connectWs() {
  try {
    if (!canRealtime.value) return
    if (ws.value && (ws.value.readyState === WebSocket.OPEN || ws.value.readyState === WebSocket.CONNECTING)) return
    const socket = new WebSocket(wsUrl())
    ws.value = socket

    socket.onopen = () => {
      wsConnected.value = true
      wsReconnectAttempt = 0
      wsCloseHinted.value = false
    }
    socket.onclose = (ev: CloseEvent) => {
      wsConnected.value = false
      // 已退出/未登录：不提示、不重连（避免“不断刷新/断开”）
      if (!canRealtime.value) return
      if (!wsCloseHinted.value) {
        wsCloseHinted.value = true
        const reason = (ev?.reason || '').trim()
        const code = ev?.code
        if (reason) {
          ElMessage.warning(`实时连接已断开：${reason}`)
        } else if (code && code !== 1000) {
          ElMessage.warning(`实时连接已断开（code=${code}），正在重连…`)
        }
      }
      scheduleReconnect()
    }
    socket.onerror = () => {
      wsConnected.value = false
    }
    socket.onmessage = (ev) => {
      try {
        const payload = JSON.parse(ev.data)
        if (payload?.type === 'MESSAGE' && payload?.message) {
          onWsMessage(payload.conversationId, payload.message as ChatMessageResponse)
          return
        }
        if (payload?.type === 'OFFER_UPDATED' && payload?.message) {
          onOfferUpdated(payload.conversationId, payload.message as ChatMessageResponse)
          return
        }
        if (payload?.type === 'MESSAGE_UPDATE' && payload?.messageId) {
          onMessageUpdate(payload.conversationId, payload.messageId, payload.payload)
          return
        }
        if (payload?.type === 'SENT') {
          onWsSent(payload?.tempId, payload?.id, payload?.conversationId)
        }
      } catch {
        // ignore
      }
    }
  } catch {
    scheduleReconnect()
  }
}

function scheduleReconnect() {
  if (wsReconnectTimer) return
  if (!canRealtime.value) return
  wsReconnectAttempt += 1
  const delay = Math.min(8000, 500 * wsReconnectAttempt)
  wsReconnectTimer = window.setTimeout(() => {
    wsReconnectTimer = null
    connectWs()
  }, delay)
}

function updateConversationListOnIncoming(conversationId: number, msg: ChatMessageResponse) {
  const idx = conversations.value.findIndex(c => c.id === conversationId)
  if (idx < 0) return
  const c = conversations.value[idx]
  if (!c) return
  const mt = (msg.msgType || 'TEXT').toUpperCase()
  const lastContent =
    mt === 'TEXT'
      ? (msg.content || '')
      : mt === 'QUOTE'
        ? (msg.content || '[报价]')
        : mt === 'ATTACHMENT'
          ? '[附件]'
          : '[系统]'
  c.lastContent = lastContent
  c.lastTime = new Date().toISOString()
  if (activeConversationId.value !== conversationId) {
    c.unreadCount = (c.unreadCount || 0) + 1
  }
  // move to top
  conversations.value.splice(idx, 1)
  conversations.value.unshift(c)
}

function onWsMessage(conversationId: number, msg: ChatMessageResponse) {
  updateConversationListOnIncoming(conversationId, msg)
  if (activeConversationId.value !== conversationId) return
  messages.value.push(mapApiMessageToUi(msg))
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}

function onOfferUpdated(conversationId: number, msg: ChatMessageResponse) {
  if (activeConversationId.value !== conversationId) return
  
  // 查找并更新现有消息
  const idx = messages.value.findIndex(m => m.id === msg.id)
  if (idx >= 0) {
    const old = messages.value[idx]
    messages.value[idx] = { ...old, ...mapApiMessageToUi(msg) }
  } else {
    // 如果本地没找到（比如刚进入会话），则直接插入（或者忽略，因为 loadMessages 会处理）
    messages.value.push(mapApiMessageToUi(msg))
  }

  // 同时也可能需要让其他同一会话的 OFFERED 消息变为 EXPIRED（如果后端已经处理并广播了，这里会自动匹配）
  // 简单起见，如果收到的是 ACCEPTED，我们可以本地把其他 OFFERED 且是该会话的改为 EXPIRED
  if (msg.quoteStatus === 'ACCEPTED') {
    messages.value.forEach(m => {
      if (m.id !== msg.id && m.msgType === 'QUOTE' && m.quoteStatus === 'OFFERED') {
        m.quoteStatus = 'EXPIRED'
      }
    })
  }

  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}

/**
 * 处理消息更新事件（用于合同签署状态同步）
 */
function onMessageUpdate(conversationId: number, messageId: number, payload: any) {
  if (activeConversationId.value !== conversationId) return

  // 使用 splice 确保 Vue 能检测到数组变化
  const idx = messages.value.findIndex(m => m.id === messageId)
  if (idx >= 0) {
    const oldMsg = messages.value[idx]
    if (!oldMsg) return
    const updated: UiMessage = {
      ...oldMsg,
      id: oldMsg.id,
      type: oldMsg.type || 'received',
      payloadJson: JSON.stringify(payload)
    }
    messages.value.splice(idx, 1, updated)
    console.log('[ChatView] Message updated via splice:', messageId, payload)
  }
}

async function handleConfirmOffer(msgId: number | string) {
  if (typeof msgId !== 'number') return
  try {
    const res = await confirmChatOffer(msgId)
    if (res.code === 0) {
      ElMessage.success('报价已确认，交易达成！')
      // 后端会通过 WS 广播通知，所以这里其实不用手动更新本地状态，等推送即可
      
      // 延迟一点后询问是否起草合同，让状态有时间更新
      setTimeout(() => {
        promptContractDraft()
      }, 800)
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '确认失败')
  }
}

// 提示用户是否起草合同
function promptContractDraft() {
  ElMessageBox.confirm(
    '交易意向已达成，是否立即起草电子合同？',
    '起草合同',
    {
      confirmButtonText: '立即起草',
      cancelButtonText: '稍后处理',
      type: 'success',
      center: true
    }
  ).then(() => {
    initiateContract()
  }).catch(() => {
    // 用户选择稍后处理
    ElMessage.info('您可以随时点击右侧「起草合同」按钮来创建合同')
  })
}

function onWsSent(tempId?: string, id?: number, conversationId?: number) {
  if (!tempId) return
  const idx = messages.value.findIndex(m => m.id === tempId)
  if (idx < 0) return
  const m = messages.value[idx]
  if (!m) return
  m.id = id ?? m.id
  m.status = 'sent'
  if (conversationId && activeConversationId.value === conversationId) {
    nextTick(() => {
      if (chatContainerRef.value) {
        chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
      }
    })
  }
}

function mapApiMessageToUi(m: ChatMessageResponse): UiMessage {
  const mt = (m.msgType || 'TEXT').toUpperCase()
  if (mt === 'SYSTEM') {
    return {
      id: m.id,
      type: 'system',
      msgType: mt,
      content: m.content || '',
      time: formatTime(m.createTime)
    }
  }
  const isMine = !!auth.me && m.fromUserId === auth.me.userId
  return {
    id: m.id,
    conversationId: m.conversationId,
    type: isMine ? 'sent' : 'received',
    msgType: mt,
    content: mt === 'TEXT' ? (m.content || '') : mt === 'QUOTE' ? (m.content || '[报价]') : mt === 'ATTACHMENT' ? '[附件]' : (m.content || ''),
    payloadJson: m.payloadJson,
    quoteStatus: m.quoteStatus,
    basisPrice: (m as any).basisPrice,
    contractCode: (m as any).contractCode,
    time: formatTime(m.createTime),
    fromUserId: m.fromUserId,
    toUserId: m.toUserId
  }
}

function parseQuoteFields(payloadJson?: string): QuoteFields | null {
  if (!payloadJson) return null
  try {
    const obj = JSON.parse(payloadJson)
    if (obj?.kind === 'BASIS_QUOTE_V1' && obj?.fields) return obj.fields as any
    if (obj?.kind === 'QUOTE_V1' && obj?.fields) return obj.fields as QuoteFields
    if (obj?.fields) return obj.fields as QuoteFields
    return obj as QuoteFields
  } catch {
    return null
  }
}

const peerLatestQuote = computed<QuoteFields | null>(() => {
  for (let i = messages.value.length - 1; i >= 0; i--) {
    const m = messages.value[i]
    if (!m) continue
    if (m.type !== 'received') continue
    if ((m.msgType || '').toUpperCase() !== 'QUOTE') continue
    return parseQuoteFields(m.payloadJson) || null
  }
  return null
})

const hasAcceptedQuote = computed(() => {
  return messages.value.some(m => m.msgType === 'QUOTE' && m.quoteStatus === 'ACCEPTED')
})

// 检查是否有合同消息及其状态
const contractStatus = computed(() => {
  const contractMsg = messages.value.find(m => 
    (m.msgType || '').toUpperCase() === 'CONTRACT'
  )
  if (!contractMsg) return null
  const payload = parseContractPayload(contractMsg.payloadJson)
  return payload
})

const transactionStep = computed(() => {
  // 检查合同状态
  const contract = contractStatus.value
  if (contract) {
    // status: 0=草稿, 1=待签署, 2=已签署, 3=履约中, 4=已完成
    if (contract.status >= 3) return 4 // 履约中/已完成
    if (contract.status === 2 || (contract.buyerSigned && contract.sellerSigned)) return 4 // 已签署
    if (contract.status === 1 || contract.buyerSigned || contract.sellerSigned) return 3 // 签署中
    return 3 // 有合同草稿
  }
  
  if (hasAcceptedQuote.value) return 2 // 达成意向
  if (messages.value.some(m => m.msgType === 'QUOTE')) return 1 // 议价中
  return 0 // 建立联系
})

// 签署合同步骤描述
const contractStepDesc = computed(() => {
  const contract = contractStatus.value
  if (!contract) return '由意向生成正式合同'
  if (contract.buyerSigned && contract.sellerSigned) return '✅ 双方已签署'
  if (contract.buyerSigned) return '买方已签署，等待卖方'
  if (contract.sellerSigned) return '卖方已签署，等待买方'
  if (contract.status === 1) return '待签署'
  return '合同草稿已创建'
})

// 履约完成步骤描述
const fulfillmentStepDesc = computed(() => {
  const contract = contractStatus.value
  if (!contract) return ''
  if (contract.status === 4) return '✅ 交易已完成'
  if (contract.status === 3) return '履约进行中'
  if (contract.status === 2 || (contract.buyerSigned && contract.sellerSigned)) return '合同已生效，进入履约'
  return ''
})

// 加载会话列表
async function loadConversations() {
  loading.value = true
  try {
    const res = await listChatConversations()
    if (res.code !== 0) throw new Error(res.message)
    conversations.value = (res.data || []).map(c => ({ ...c }))

    // 支持 /chat?conversationId=xxx 直接定位
    const qid = Number(route.query.conversationId)
    const preferred = Number.isFinite(qid) ? conversations.value.find(c => c.id === qid) : null
    if (preferred) {
      await selectConversation(preferred)
      return
    }

    // 选择第一个联系人
    if (groupedByPeer.value.length > 0 && !activePeerId.value) {
      const firstPeer = groupedByPeer.value[0]
      if (firstPeer) await selectPeer(firstPeer)
    } else if (conversations.value.length === 0) {
      activeConversationId.value = null
      activePeerId.value = null
      messages.value = []
    }
  } finally {
    loading.value = false
  }
}

function avatarText(name?: string) {
  const s = (name || '').trim()
  return s ? s[0] : 'A'
}

// 头像渐变色方案 - 根据用户名生成个性化渐变
const AVATAR_GRADIENTS = [
  'from-violet-500 to-purple-600',      // 紫罗兰
  'from-brand-500 to-teal-600',       // 翡翠绿
  'from-sky-500 to-blue-600',           // 天蓝
  'from-orange-400 to-rose-500',        // 珊瑚橙
  'from-pink-500 to-fuchsia-600',       // 品红
  'from-cyan-500 to-blue-500',          // 青蓝
  'from-amber-500 to-orange-600',       // 琥珀
  'from-indigo-500 to-violet-600',      // 靛蓝
  'from-rose-500 to-pink-600',          // 玫瑰
  'from-teal-500 to-brand-600',       // 青绿
]

function avatarGradient(name?: string): string {
  const s = (name || '').trim()
  if (!s) return AVATAR_GRADIENTS[0] || 'from-brand-500 to-teal-600'
  
  // 使用名字的字符码之和来确定渐变色
  let hash = 0
  for (let i = 0; i < s.length; i++) {
    hash = ((hash << 5) - hash) + s.charCodeAt(i)
    hash = hash & hash // Convert to 32bit integer
  }
  
  const index = Math.abs(hash) % AVATAR_GRADIENTS.length
  return AVATAR_GRADIENTS[index]!
}

function subjectBadge(c: ChatConversationResponse) {
  const st = (c.subjectType || '').toUpperCase()
  if (st === 'SUPPLY') return { label: '供应', cls: 'bg-brand-50 text-brand-700 border-brand-100' }
  if (st === 'NEED') return { label: '采购', cls: 'bg-blue-50 text-blue-700 border-blue-200' }
  return { label: '会话', cls: 'bg-gray-50 text-gray-600 border-gray-200' }
}

function quoteStatusBadge(status?: string) {
  if (status === 'OFFERED') return { label: '待确认', cls: 'bg-blue-50 text-blue-600 border-blue-100' }
  if (status === 'ACCEPTED') return { label: '已达成', cls: 'bg-brand-50 text-brand-600 border-brand-100' }
  if (status === 'EXPIRED') return { label: '已失效', cls: 'bg-gray-50 text-gray-400 border-gray-100' }
  if (status === 'REJECTED') return { label: '已拒绝', cls: 'bg-red-50 text-red-600 border-red-100' }
  return null
}

const QUOTE_LABEL_MAP: Record<string, string> = {
  price: '单价(元/吨)',
  quantity: '数量',
  deliveryMethod: '交货方式',
  deliveryPlace: '交付地',
  arrivalDate: '到货期',
  paymentMethod: '结算方式',
  invoiceType: '发票类型',
  packaging: '包装方式',
  validUntil: '有效期',
  remark: '备注'
}

function getQuoteDisplayFields(payloadJson?: string) {
  const payload = parseQuoteFields(payloadJson)
  if (!payload) return []
  
  // 处理基差报价 V1
  if ((payload as any).kind === 'BASIS_QUOTE_V1' && (payload as any).fields) {
    const f = (payload as any).fields
    return [
      { label: '期货合约', value: f.contractName || f.contractCode },
      { label: '基差', value: `${f.basisPrice > 0 ? '+' : ''}${f.basisPrice} 元/吨` },
      { label: '盘面价', value: `¥${f.futuresPrice}` },
      { label: '核算价', value: `¥${f.referencePrice}` },
      { label: '数量', value: `${f.quantity} 吨` },
      { label: '备注', value: f.remark }
    ].filter(i => i.value !== undefined && i.value !== '')
  }

  const fields = payload
  const display: Array<{ label: string; value: any }> = []

  // 基础字段
  Object.entries(fields)
    .filter(([k, v]) => v && QUOTE_LABEL_MAP[k])
    .forEach(([k, v]) => {
      display.push({ label: QUOTE_LABEL_MAP[k]!, value: v })
    })

  // 动态字段
  const dynamic = fields.dynamicParams || {}
  Object.entries(dynamic).forEach(([k, v]) => {
    if (v) display.push({ label: k, value: v })
  })

  return display
}

// 解析图片消息 payload
function parseImagePayload(payloadJson?: string): ImagePayload | null {
  if (!payloadJson) return null
  try {
    return JSON.parse(payloadJson) as ImagePayload
  } catch {
    return null
  }
}

// 解析附件消息 payload
function parseAttachmentPayload(payloadJson?: string): AttachmentPayload | null {
  if (!payloadJson) return null
  try {
    return JSON.parse(payloadJson) as AttachmentPayload
  } catch {
    return null
  }
}

// 合同消息 payload 类型
interface ContractPayload {
  contractId: number
  contractNo: string
  productName: string
  quantity: number | string
  unit: string
  unitPrice: number | string
  totalAmount: number | string
  buyerCompanyId: number
  buyerCompanyName: string
  sellerCompanyId: number
  sellerCompanyName: string
  status: number
  buyerSigned: boolean
  sellerSigned: boolean
}

// 解析合同消息 payload
function parseContractPayload(payloadJson?: string): ContractPayload | null {
  if (!payloadJson) return null
  try {
    return JSON.parse(payloadJson) as ContractPayload
  } catch {
    return null
  }
}

// 签署弹窗相关
const signModalVisible = ref(false)
const signContractId = ref<number | null>(null)

// 查看合同详情
function handleViewContract(contractId: number) {
  router.push(`/contracts/${contractId}`)
}

// 打开签署弹窗
function handleSignContract(contractId: number) {
  signContractId.value = contractId
  signModalVisible.value = true
}

// 图片预览相关
const imagePreviewVisible = ref(false)
const imagePreviewUrl = ref('')

function openImagePreview(url: string) {
  imagePreviewUrl.value = url
  imagePreviewVisible.value = true
}

function closeImagePreview() {
  imagePreviewVisible.value = false
  imagePreviewUrl.value = ''
}

// 下载附件
function downloadAttachment(url: string, fileName: string) {
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 选择联系人
async function selectPeer(peer: PeerGroup) {
  activePeerId.value = peer.peerUserId
  
  // 默认选择该联系人最新的会话
  const latestConversation = peer.conversations.sort((a, b) => 
    (b.lastTime || '').localeCompare(a.lastTime || '')
  )[0]
  
  if (latestConversation) {
    await selectConversation(latestConversation)
  }
}

// 选择会话（在已选中联系人内切换标的）
async function selectConversation(c: ChatConversationResponse) {
  activeConversationId.value = c.id
  activePeerId.value = c.peerUserId
  c.unreadCount = 0

  await loadMessages(c.id)
  try {
    await markConversationRead(c.id)
  } catch {
    // ignore
  }

  // 检查关注状态
  checkPeerFollowStatus()

  // 同步 URL（便于"转入沟通中心"/刷新保留）
  router.replace({ path: '/chat', query: { conversationId: String(c.id) } })
}

// 切换同一联系人的不同标的会话
async function switchConversation(conversationId: number) {
  const c = conversations.value.find(c => c.id === conversationId)
  if (c) {
    await selectConversation(c)
  }
}

// 加载消息（conversation 维度）
async function loadMessages(conversationId: number) {
  loading.value = true
  try {
    const res = await getConversationMessages(conversationId, 100)
    if (res.code !== 0) throw new Error(res.message)
    messages.value = (res.data || []).map(mapApiMessageToUi)

    // 滚动到底部
    await nextTick()
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  } finally {
    loading.value = false
  }
}

// 发送消息
async function sendMessage() {
  if (!messageInput.value.trim()) return
  if (!activeConversationId.value) {
    ElMessage.warning('请先选择会话')
    return
  }

  const content = messageInput.value.trim()
  const nowIso = new Date().toISOString()
  const tempId = `t_${Date.now()}_${Math.random().toString(16).slice(2)}`
  messageInput.value = ''

  // 更新会话列表摘要（本地先更新，保证列表“看起来像真的”）
  const cidx = conversations.value.findIndex(c => c.id === activeConversationId.value)
  if (cidx >= 0) {
    const c = conversations.value[cidx]
    if (!c) return
    c.lastContent = content
    c.lastTime = nowIso
    c.unreadCount = 0
    conversations.value.splice(cidx, 1)
    conversations.value.unshift(c)
  }

  // 本地先插入（pending）
  messages.value.push({
    id: tempId,
    conversationId: activeConversationId.value,
    type: 'sent',
    msgType: 'TEXT',
    content,
    status: 'pending',
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })

  // 滚动到底部
  await nextTick()
  if (chatContainerRef.value) {
    chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
  }

  if (ws.value) {
    ws.value.send(JSON.stringify({
      type: 'SEND',
      conversationId: activeConversationId.value,
      msgType: 'TEXT',
      content,
      tempId
    }))
  }
}

async function sendQuote(payload: any, summary: string, basisPrice?: number, contractCode?: string) {
  if (!activeConversationId.value) {
    ElMessage.warning('请先选择会话')
    return
  }
  quotePopoverVisible.value = false
  if (!ws.value || ws.value.readyState !== WebSocket.OPEN) {
    ElMessage.warning('实时连接未就绪，正在重连…')
    connectWs()
    return
  }

  const nowIso = new Date().toISOString()
  const tempId = `q_${Date.now()}_${Math.random().toString(16).slice(2)}`

  // 更新会话列表摘要
  const cidx = conversations.value.findIndex(c => c.id === activeConversationId.value)
  if (cidx >= 0) {
    const c = conversations.value[cidx]
    if (c) {
      c.lastContent = summary || '[报价]'
      c.lastTime = nowIso
      c.unreadCount = 0
      conversations.value.splice(cidx, 1)
      conversations.value.unshift(c)
    }
  }

  messages.value.push({
    id: tempId,
    conversationId: activeConversationId.value,
    type: 'sent',
    msgType: 'QUOTE',
    content: summary || '[报价]',
    payloadJson: JSON.stringify(payload),
    status: 'pending',
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  await nextTick()
  if (chatContainerRef.value) {
    chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
  }

  if (ws.value) {
    ws.value.send(JSON.stringify({
      type: 'SEND',
      conversationId: activeConversationId.value,
      msgType: 'QUOTE',
      content: summary || '',
      payload,
      basisPrice,
      contractCode,
      tempId
    }))
  }
}

// 查看关联信息详情
function viewLinkedInfo() {
  if (!currentConversation.value) return
  subjectDialogOpen.value = true
}

async function copySubjectId() {
  const c = currentConversation.value
  if (!c?.subjectId) return
  try {
    await navigator.clipboard.writeText(String(c.subjectId))
    ElMessage.success('已复制标的ID')
  } catch {
    ElMessage.warning('复制失败，请手动复制')
  }
}

function openSubjectOrigin() {
  const c = currentConversation.value
  if (!c) return
  const st = (c.subjectType || '').toUpperCase()
  const path = st === 'SUPPLY' ? '/hall/supply' : '/hall/need'
  router.push({ path, query: { focusId: c.subjectId ? String(c.subjectId) : '' } })
  subjectDialogOpen.value = false
}

// 发起合同
function initiateContract() {
  if (!currentConversation.value) return
  
  // 找到已确认的报价消息
  const acceptedQuote = messages.value.find(
    m => m.msgType === 'QUOTE' && m.quoteStatus === 'ACCEPTED' && typeof m.id === 'number'
  )
  
  if (!acceptedQuote) {
    ElMessage.warning('请先在报价单上确认成交')
    return
  }
  
  // 调用已有的打开弹窗函数
  openContractDraft(acceptedQuote)
}

// 打开赠送积分对话框
function openGiftDialog() {
  if (!currentConversation.value) {
    ElMessage.warning('请先选择会话')
    return
  }
  giftForm.points = 10
  giftForm.remark = ''
  giftDialogVisible.value = true
}

// 打开起草合同弹窗
function openContractDraft(msg: UiMessage) {
  if (typeof msg.id !== 'number') return
  draftQuoteMessageId.value = msg.id
  draftQuoteData.value = parseQuoteFields(msg.payloadJson)
  contractDraftVisible.value = true
}

// 从标的快照获取产品名称
function getSubjectProductName(): string {
  try {
    const json = currentConversation.value?.subjectSnapshotJson
    if (!json) return ''
    const obj = JSON.parse(json)
    return obj.productName || obj.title || ''
  } catch {
    return ''
  }
}

// 合同创建成功
function onContractCreated(_contractId: number) {
  ElMessage.success('合同创建成功')
  // 刷新消息列表以显示合同卡片
  if (activeConversationId.value) {
    loadMessages(activeConversationId.value)
  }
}

// 合同签署成功后刷新
function onContractSigned() {
  ElMessage.success('签署成功')
  // 刷新消息列表
  if (activeConversationId.value) {
    loadMessages(activeConversationId.value)
  }
}

// 提交赠送积分
async function submitGiftPoints() {
  if (!currentConversation.value?.peerUserId) {
    ElMessage.warning('无法获取对方用户信息')
    return
  }
  
  if (giftForm.points < 1) {
    ElMessage.warning('赠送积分数量至少为1')
    return
  }
  
  giftLoading.value = true
  try {
    await giftPoints(
      currentConversation.value.peerUserId,
      giftForm.points, 
      giftForm.remark || `商务聊天赠送给${peerDisplayName.value}`
    )
    ElMessage.success(`已成功赠送 ${giftForm.points} 积分给 ${peerDisplayName.value}`)
    giftDialogVisible.value = false
    
    // 添加系统消息
    messages.value.push({
      id: Date.now(),
      type: 'system',
      content: `您已赠送 ${giftForm.points} 积分给对方`,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })

    await nextTick()
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '赠送积分失败，请稍后重试')
  } finally {
    giftLoading.value = false
  }
}

// 打开图片选择
function openImagePicker() {
  if (!activeConversationId.value) {
    ElMessage.warning('请先选择会话')
    return
  }
  imageInputRef.value?.click()
}

// 打开附件选择
function openAttachmentPicker() {
  if (!activeConversationId.value) {
    ElMessage.warning('请先选择会话')
    return
  }
  attachmentInputRef.value?.click()
}

// 处理图片选择
async function handleImageSelect(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  
  // 重置 input 以便可以再次选择同一文件
  input.value = ''
  
  await uploadAndSendFile(file, 'image')
}

// 处理附件选择
async function handleAttachmentSelect(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  
  input.value = ''
  
  await uploadAndSendFile(file, 'attachment')
}

// 上传并发送文件消息
async function uploadAndSendFile(file: File, type: 'image' | 'attachment') {
  if (!activeConversationId.value) {
    ElMessage.warning('请先选择会话')
    return
  }

  if (!ws.value || ws.value.readyState !== WebSocket.OPEN) {
    ElMessage.warning('实时连接未就绪，正在重连…')
    connectWs()
    return
  }

  uploading.value = true
  uploadProgress.value = 0
  uploadType.value = type

  try {
    let uploadFile = file

    const res = type === 'image'
      ? await uploadImage(uploadFile, (p) => { uploadProgress.value = p })
      : await uploadAttachment(uploadFile, (p) => { uploadProgress.value = p })
    
    if (res.code !== 0) {
      throw new Error(res.message || '上传失败')
    }

    const fileData = res.data
    if (!fileData) {
      throw new Error('上传失败：文件数据为空')
    }

    const msgType = type === 'image' ? 'IMAGE' : 'ATTACHMENT'
    const payload: ImagePayload | AttachmentPayload = {
      fileId: fileData.fileId,
      fileName: fileData.fileName,
      fileUrl: fileData.fileUrl,
      size: fileData.size,
      mimeType: fileData.mimeType
    }

    const summary = type === 'image' ? '[图片]' : `[文件] ${fileData.fileName}`
    const nowIso = new Date().toISOString()
    const tempId = `f_${Date.now()}_${Math.random().toString(16).slice(2)}`
    
    // 更新会话列表摘要
    const cidx = conversations.value.findIndex(c => c.id === activeConversationId.value)
    if (cidx >= 0) {
      const c = conversations.value[cidx]
      if (c) {
        c.lastContent = summary
        c.lastTime = nowIso
        c.unreadCount = 0
        conversations.value.splice(cidx, 1)
        conversations.value.unshift(c)
      }
    }
    
    // 本地插入消息
    messages.value.push({
      id: tempId,
      conversationId: activeConversationId.value,
      type: 'sent',
      msgType,
      content: summary,
      payloadJson: JSON.stringify(payload),
      status: 'pending',
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })

    await nextTick()
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }

    // 通过 WebSocket 发送
    if (ws.value) {
      ws.value.send(JSON.stringify({
        type: 'SEND',
        conversationId: activeConversationId.value,
        msgType,
        content: summary,
        payload,
        tempId
      }))
    }

    ElMessage.success(type === 'image' ? '图片已发送' : '附件已发送')
  } catch (e: any) {
    ElMessage.error(e?.message || '上传失败，请重试')
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

onMounted(() => {
  loadArchivedConversations()
  if (canRealtime.value) connectWs()
  loadConversations()
  xlMql = window.matchMedia('(min-width: 1280px)')
  isDesktopXl.value = xlMql.matches
  xlListener = (e: MediaQueryListEvent) => {
    isDesktopXl.value = e.matches
    if (e.matches && negotiationDrawerOpen.value) negotiationDrawerOpen.value = false
  }
  xlMql.addEventListener('change', xlListener)
})

// 退出后停止 WS 重连；登录态恢复后自动重连
watch(canRealtime, (ok) => {
  if (!ok) cleanupWs()
  else connectWs()
})

onBeforeUnmount(() => {
  cleanupWs()
  if (xlMql && xlListener) {
    xlMql.removeEventListener('change', xlListener)
  }
})
</script>

<template>
  <div class="chat-view h-full">
    <div class="flex h-full bg-white rounded-lg border border-gray-200 shadow-sm overflow-hidden">
      <!-- 左侧联系人列表（聚合后） -->
      <div class="w-80 border-r border-gray-200 flex flex-col bg-white">
        <!-- 搜索栏 -->
        <div class="p-4 border-b border-gray-200">
          <div class="relative">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索联系人..."
              class="w-full pl-10 pr-4 py-2.5 bg-gray-50 border-2 border-transparent rounded-lg text-sm focus:bg-white focus:border-brand-500 outline-none transition-all"
            />
          </div>
        </div>

        <!-- 联系人列表（按时间分组） -->
        <div class="flex-1 overflow-y-auto">
          <template v-for="group in timeGroupedPeers" :key="group.label">
            <!-- 时间分组标题 -->
            <div class="sticky top-0 z-10 px-4 py-2 bg-gray-50/95 backdrop-blur-sm">
              <span class="text-[10px] font-bold uppercase tracking-widest text-gray-400">{{ group.label }}</span>
            </div>
            
            <!-- 联系人卡片 -->
            <div
              v-for="(peer, peerIdx) in group.peers"
              :key="peer.peerUserId"
              class="peer-card relative cursor-pointer transition-all"
              :class="[
                activePeerId === peer.peerUserId 
                  ? 'bg-white shadow-sm' 
                  : 'hover:bg-gray-50/50'
              ]"
              :style="{ animationDelay: `${peerIdx * 50}ms` }"
              @click="selectPeer(peer)"
            >
              <!-- 选中指示条 -->
              <div 
                v-if="activePeerId === peer.peerUserId"
                class="indicator-bar absolute left-0 top-3 bottom-3 w-1 bg-brand-500 rounded-r-full"
              ></div>
              
              <div class="px-4 py-3 flex items-start gap-3">
                <!-- 头像 + 在线状态 -->
                <div class="relative shrink-0">
                  <div 
                    class="w-12 h-12 rounded-lg flex items-center justify-center text-white font-bold bg-gradient-to-br shadow-md"
                    :class="avatarGradient(peer.peerNickName || peer.peerUserName || peer.peerCompanyName)"
                  >
                    {{ avatarText(peer.peerNickName || peer.peerUserName || peer.peerCompanyName) }}
                  </div>
                  <!-- 在线状态指示器 -->
                  <div class="absolute -bottom-0.5 -right-0.5 w-3.5 h-3.5 bg-brand-500 border-2 border-white rounded-full"></div>
                </div>
                
                <!-- 联系人信息 -->
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between">
                    <span class="font-bold text-gray-900 truncate">
                      {{ peer.peerNickName || peer.peerUserName || '对方' }}
                    </span>
                    <span class="text-[10px] text-gray-400 shrink-0 ml-2">{{ formatRelativeTime(peer.lastTime) }}</span>
                  </div>
                  <div class="text-xs text-gray-500 truncate mt-1">{{ peer.lastContent || '暂无消息' }}</div>
                  <!-- 标的标签（多个会话时显示数量） -->
                  <div class="mt-2 flex items-center gap-2">
                    <span
                      v-if="peer.conversations.length === 1 && peer.conversations[0]"
                      class="text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 rounded-full border"
                      :class="subjectBadge(peer.conversations[0]).cls"
                    >
                      {{ getSubjectShortName(peer.conversations[0]) || subjectBadge(peer.conversations[0]).label }}
                    </span>
                    <span 
                      v-else
                      class="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-600"
                    >
                      {{ peer.conversations.length }} 个话题
                    </span>
                    <span class="text-[10px] text-gray-400 truncate" v-if="peer.peerCompanyName">{{ peer.peerCompanyName }}</span>
                  </div>
                </div>
                
                <!-- 未读角标 -->
                <div 
                  v-if="peer.totalUnread > 0" 
                  class="unread-badge min-w-5 h-5 px-1.5 rounded-full bg-brand-500 text-white text-[10px] font-bold flex items-center justify-center shrink-0"
                >
                  {{ peer.totalUnread > 99 ? '99+' : peer.totalUnread }}
                </div>
              </div>
            </div>
          </template>

          <!-- 空状态 -->
          <div v-if="timeGroupedPeers.length === 0" class="py-16 text-center">
            <div class="w-16 h-16 mx-auto mb-4 rounded-lg bg-gray-100 flex items-center justify-center">
              <ChatDotRound class="w-8 h-8 text-gray-300" />
            </div>
            <p class="text-sm font-medium text-gray-500">暂无会话</p>
            <p class="text-xs text-gray-400 mt-1">从供应/需求大厅发起沟通</p>
          </div>
        </div>
      </div>

      <!-- 右侧聊天区域 -->
      <div class="flex-1 flex flex-col min-w-0">
        <!-- 顶部信息栏 + 标的切换器 -->
        <div v-if="currentConversation" class="border-b border-gray-200 bg-white">
          <!-- 联系人信息 -->
          <div class="px-6 py-3 flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="relative">
                <div 
                  class="w-10 h-10 rounded-lg bg-gradient-to-br flex items-center justify-center text-white font-bold shrink-0 shadow-md"
                  :class="avatarGradient(currentConversation.peerNickName || currentConversation.peerUserName || currentConversation.peerCompanyName)"
                >
                  {{ avatarText(currentConversation.peerNickName || currentConversation.peerUserName || currentConversation.peerCompanyName) }}
                </div>
                <div class="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-brand-500 border-2 border-white rounded-full"></div>
              </div>
              <div>
                <div class="font-bold text-gray-900 leading-tight">{{ peerDisplayName }}</div>
                <div class="text-[10px] flex items-center gap-1.5 mt-0.5">
                  <span :class="wsConnected ? 'text-brand-500' : 'text-gray-400'">
                    ● {{ wsConnected ? '在线' : '连接中…' }}
                  </span>
                  <span class="text-gray-300">|</span>
                  <span class="text-gray-400 truncate">{{ currentConversation.peerCompanyName || '个人用户' }}</span>
                </div>
              </div>
            </div>
            
            <div class="flex items-center gap-2">
              <!-- 关注按钮 -->
              <button 
                class="px-3 py-1.5 rounded-lg text-xs font-bold transition-all  flex items-center gap-1"
                :class="isFollowingPeer 
                  ? 'bg-amber-50 text-amber-600 hover:bg-amber-100' 
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
                :disabled="followLoading"
                @click="toggleFollowPeer"
              >
                <StarFilled v-if="isFollowingPeer" class="w-3.5 h-3.5" />
                <Star v-else class="w-3.5 h-3.5" />
                {{ isFollowingPeer ? '已关注' : '关注' }}
              </button>
              <button 
                class="p-2 rounded-lg bg-gray-100 hover:bg-gray-200 transition-all "
                @click="openGiftDialog" 
                title="赠送积分"
              >
                <Present class="w-4 h-4 text-gray-600" />
              </button>
              <button 
                class="px-4 py-2 rounded-lg bg-gradient-to-r from-brand-600 to-teal-600 hover:from-brand-700 hover:to-teal-700 text-white text-xs font-bold transition-all  shadow-md shadow-brand-500/20"
                @click="initiateContract"
              >
                起草合同
              </button>
            </div>
          </div>
          
          <!-- 标的切换器（多个会话时显示） -->
          <div 
            v-if="currentPeerConversations.length > 1" 
            class="px-6 py-2 border-t border-gray-50 bg-gray-50/50 flex items-center gap-2 overflow-x-auto"
          >
            <span class="text-[10px] text-gray-400 shrink-0">话题：</span>
            <div
              v-for="conv in currentPeerConversations"
              :key="conv.id"
              class="subject-tab group relative flex items-center gap-1 px-3 py-1.5 pr-7 rounded-lg text-xs font-medium transition-all whitespace-nowrap shrink-0 cursor-pointer"
              :class="activeConversationId === conv.id 
                ? 'bg-brand-600 text-white shadow-sm' 
                : 'bg-white border border-gray-200 text-gray-600 hover:border-brand-300 hover:text-brand-600'"
              @click="switchConversation(conv.id)"
            >
              <span 
                class="w-1.5 h-1.5 rounded-full shrink-0"
                :class="(conv.subjectType || '').toUpperCase() === 'SUPPLY' ? 'bg-brand-400' : 'bg-blue-400'"
              ></span>
              <span>{{ getSubjectShortName(conv) || subjectBadge(conv).label }}</span>
              <!-- 关闭按钮 -->
              <button
                class="absolute right-1 top-1/2 -translate-y-1/2 w-5 h-5 rounded-full flex items-center justify-center transition-all opacity-0 group-hover:opacity-100"
                :class="activeConversationId === conv.id 
                  ? 'hover:bg-white/20 text-white' 
                  : 'hover:bg-gray-200 text-gray-400'"
                @click.stop="archiveConversation(conv.id)"
                title="归档此话题"
              >
                <span class="text-[10px] font-bold">×</span>
              </button>
            </div>
            
            <!-- 已归档入口 -->
            <button
              v-if="archivedConversations.length > 0"
              class="flex items-center gap-1 px-2 py-1 text-[10px] text-gray-400 hover:text-gray-600 transition-colors shrink-0"
              @click="showArchivedModal = true"
            >
              <span>📦</span>
              <span>已归档 ({{ archivedConversations.length }})</span>
            </button>
          </div>
          
          <!-- 单个会话时也显示关闭按钮 -->
          <div 
            v-else-if="currentPeerConversations.length === 1" 
            class="px-6 py-2 border-t border-gray-50 bg-gray-50/50 flex items-center justify-between"
          >
            <div class="flex items-center gap-2">
              <span class="text-[10px] text-gray-400">话题：</span>
              <span class="text-xs font-medium text-gray-700">
                {{ getSubjectShortName(currentPeerConversations[0]!) || subjectBadge(currentPeerConversations[0]!).label }}
              </span>
            </div>
            <div class="flex items-center gap-2">
              <button
                class="px-2 py-1 text-[10px] text-gray-400 hover:text-red-500 hover:bg-red-50 rounded transition-all"
                @click="archiveConversation(currentPeerConversations[0]!.id)"
                title="归档此话题"
              >
                归档
              </button>
              <button
                v-if="archivedConversations.length > 0"
                class="flex items-center gap-1 px-2 py-1 text-[10px] text-gray-400 hover:text-gray-600 transition-colors"
                @click="showArchivedModal = true"
              >
                <span>📦</span>
                <span>已归档 ({{ archivedConversations.length }})</span>
              </button>
            </div>
          </div>
        </div>

        <!-- 主体：消息区 + 右侧议价栏（桌面端） -->
        <div class="flex-1 min-h-0 flex">
          <!-- 主聊天列 -->
          <div class="flex-1 min-w-0 flex flex-col relative">
            <!-- 常驻标的摘要 -->
            <ChatSubjectCard
              v-if="currentConversation"
              is-mini
              class="sticky top-0 z-10"
              :subject-type="currentConversation.subjectType"
              :subject-id="currentConversation.subjectId"
              :subject-snapshot-json="currentConversation.subjectSnapshotJson"
            >
              <template #action>
                <button class="text-xs text-brand-600 font-bold hover:text-brand-700 transition-colors" @click="viewLinkedInfo">
                  详情 >
                </button>
              </template>
            </ChatSubjectCard>

            <!-- 消息区域 -->
            <div
              ref="chatContainerRef"
              class="flex-1 overflow-y-auto p-6 bg-gray-50"
              v-loading="loading"
            >
              <div v-if="activeConversationId" class="space-y-6">
                <div
                  v-for="(msg, idx) in messages"
                  :key="msg.id"
                  class="flex flex-col"
                >
                  <!-- 只有间隔较长或第一条显示时间 -->
                  <div v-if="idx === 0 || (idx > 0 && msg.time !== messages[idx - 1]?.time)" class="self-center my-2">
                    <span class="text-[10px] text-gray-400 bg-gray-200/50 px-2 py-0.5 rounded-full">{{ msg.time }}</span>
                  </div>

                  <div class="flex" :class="msg.type === 'sent' ? 'justify-end' : msg.type === 'system' ? 'justify-center' : 'justify-start'">
                    <!-- 系统消息 -->
                    <div v-if="msg.type === 'system'" class="px-4 py-1.5 bg-gray-200/80 text-gray-500 text-[11px] rounded-full max-w-[80%] text-center">
                      {{ msg.content }}
                    </div>
                    
                    <!-- 接收的消息 -->
                    <div v-else-if="msg.type === 'received'" class="message-received flex items-start gap-3 max-w-[85%] lg:max-w-[70%]">
                      <div 
                        class="w-8 h-8 rounded-lg bg-gradient-to-br flex items-center justify-center text-white text-xs font-bold shrink-0 shadow-sm"
                        :class="avatarGradient(currentConversation?.peerNickName || currentConversation?.peerUserName || currentConversation?.peerCompanyName)"
                      >
                        {{ avatarText(currentConversation?.peerNickName || currentConversation?.peerUserName || currentConversation?.peerCompanyName) }}
                      </div>
                      <div>
                        <!-- 报价消息 -->
                        <div v-if="(msg.msgType || '').toUpperCase() === 'QUOTE'" class="bg-white rounded-lg rounded-tl-sm px-4 py-3 shadow-sm border border-gray-200">
                          <div class="flex items-center justify-between mb-2">
                            <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">电子报价单</div>
                            <div v-if="quoteStatusBadge(msg.quoteStatus)" 
                                 :class="['text-[10px] px-1.5 py-0.5 rounded-full border font-bold', quoteStatusBadge(msg.quoteStatus)?.cls]">
                              {{ quoteStatusBadge(msg.quoteStatus)?.label }}
                            </div>
                          </div>
                          <div class="mt-1 font-bold text-gray-900 border-b pb-2 mb-3">{{ msg.content || '[报价]' }}</div>
                          <div class="space-y-2">
                            <div v-for="field in getQuoteDisplayFields(msg.payloadJson)" :key="field.label" class="flex justify-between text-xs">
                              <span class="text-gray-400">{{ field.label }}</span>
                              <span class="text-gray-700 font-medium">{{ field.value }}</span>
                            </div>
                          </div>
                          <!-- 确认下单按钮 -->
                          <div v-if="msg.quoteStatus === 'OFFERED'" class="mt-4 pt-3 border-t border-gray-50 flex justify-end">
                            <el-button 
                              size="small" 
                              type="primary" 
                              class="!rounded-lg !bg-brand-600 hover:!bg-brand-700 !border-brand-600 !text-white transition-all "
                              @click="handleConfirmOffer(msg.id)"
                            >
                              确认成交
                            </el-button>
                          </div>
                          <!-- 已成交：起草合同按钮 -->
                          <div v-else-if="msg.quoteStatus === 'ACCEPTED'" class="mt-4 pt-3 border-t border-gray-50 flex justify-end">
                            <button 
                              class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold rounded-lg transition-all  flex items-center gap-1.5"
                              @click="openContractDraft(msg)"
                            >
                              <Document class="w-4 h-4" />
                              起草合同
                            </button>
                          </div>
                        </div>
                        <!-- 图片消息 -->
                        <div v-else-if="(msg.msgType || '').toUpperCase() === 'IMAGE'" class="bg-white rounded-lg rounded-tl-sm p-2 shadow-sm border border-gray-200">
                          <img 
                            v-if="parseImagePayload(msg.payloadJson)?.fileUrl"
                            :src="parseImagePayload(msg.payloadJson)?.fileUrl"
                            :alt="parseImagePayload(msg.payloadJson)?.fileName || '图片'"
                            class="max-w-[280px] max-h-[200px] rounded-lg cursor-pointer hover:opacity-90 transition-opacity object-cover"
                            @click="openImagePreview(parseImagePayload(msg.payloadJson)?.fileUrl || '')"
                          />
                          <div v-else class="text-sm text-gray-500">[图片加载失败]</div>
                        </div>
                        <!-- 附件消息 -->
                        <div v-else-if="(msg.msgType || '').toUpperCase() === 'ATTACHMENT'" class="bg-white rounded-lg rounded-tl-sm px-4 py-3 shadow-sm border border-gray-200">
                          <div class="flex items-center gap-3">
                            <div class="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center shrink-0">
                              <Document class="w-5 h-5 text-blue-600" />
                            </div>
                            <div class="flex-1 min-w-0">
                              <div class="text-sm font-medium text-gray-900 truncate">{{ parseAttachmentPayload(msg.payloadJson)?.fileName || '附件' }}</div>
                              <div class="text-xs text-gray-400">{{ formatFileSize(parseAttachmentPayload(msg.payloadJson)?.size || 0) }}</div>
                            </div>
                            <button 
                              class="shrink-0 px-3 py-1.5 bg-blue-50 text-blue-600 text-xs font-bold rounded-lg hover:bg-blue-100 transition-all "
                              @click="downloadAttachment(parseAttachmentPayload(msg.payloadJson)?.fileUrl || '', parseAttachmentPayload(msg.payloadJson)?.fileName || 'file')"
                            >
                              下载
                            </button>
                          </div>
                        </div>
                        <!-- 合同消息 -->
                        <ContractCard
                          v-else-if="(msg.msgType || '').toUpperCase() === 'CONTRACT' && parseContractPayload(msg.payloadJson)"
                          :payload="parseContractPayload(msg.payloadJson)!"
                          :is-sent="false"
                          @view="handleViewContract"
                          @sign="handleSignContract"
                        />
                        <!-- 普通文本消息 -->
                        <div v-else class="bg-white rounded-lg rounded-tl-sm px-4 py-3 shadow-sm border border-gray-200 text-sm text-gray-800">
                          {{ msg.content }}
                        </div>
                      </div>
                    </div>
                    
                    <!-- 发送的消息 -->
                    <div v-else class="message-sent flex items-start gap-3 max-w-[85%] lg:max-w-[70%] flex-row-reverse">
                      <div class="w-8 h-8 rounded-lg bg-brand-600 flex items-center justify-center text-white text-xs font-bold shrink-0">
                        {{ (auth.me?.nickName || 'U')[0] }}
                      </div>
                      <div class="flex flex-col items-end">
                        <!-- 报价消息 -->
                        <div v-if="(msg.msgType || '').toUpperCase() === 'QUOTE'" class="bg-brand-600 text-white rounded-lg rounded-tr-sm px-4 py-3 shadow-sm">
                          <div class="flex items-center justify-between mb-2 gap-4">
                            <div class="text-[10px] font-bold uppercase tracking-widest text-brand-100">电子报价单</div>
                            <div v-if="quoteStatusBadge(msg.quoteStatus)" 
                                 class="text-[10px] px-1.5 py-0.5 rounded-full border border-brand-400 bg-brand-500/50 font-bold text-white">
                              {{ quoteStatusBadge(msg.quoteStatus)?.label }}
                            </div>
                          </div>
                          <div class="mt-1 font-bold border-b border-brand-500 pb-2 mb-3">{{ msg.content || '[报价]' }}</div>
                          <div class="space-y-2">
                            <div v-for="field in getQuoteDisplayFields(msg.payloadJson)" :key="field.label" class="flex justify-between text-xs">
                              <span class="text-brand-100/80">{{ field.label }}</span>
                              <span class="text-white font-medium">{{ field.value }}</span>
                            </div>
                          </div>
                        </div>
                        <!-- 图片消息 -->
                        <div v-else-if="(msg.msgType || '').toUpperCase() === 'IMAGE'" class="bg-brand-600 rounded-lg rounded-tr-sm p-2 shadow-sm">
                          <img 
                            v-if="parseImagePayload(msg.payloadJson)?.fileUrl"
                            :src="parseImagePayload(msg.payloadJson)?.fileUrl"
                            :alt="parseImagePayload(msg.payloadJson)?.fileName || '图片'"
                            class="max-w-[280px] max-h-[200px] rounded-lg cursor-pointer hover:opacity-90 transition-opacity object-cover"
                            @click="openImagePreview(parseImagePayload(msg.payloadJson)?.fileUrl || '')"
                          />
                          <div v-else class="text-sm text-white/80">[图片加载失败]</div>
                        </div>
                        <!-- 附件消息 -->
                        <div v-else-if="(msg.msgType || '').toUpperCase() === 'ATTACHMENT'" class="bg-brand-600 rounded-lg rounded-tr-sm px-4 py-3 shadow-sm">
                          <div class="flex items-center gap-3">
                            <div class="w-10 h-10 rounded-lg bg-white/20 flex items-center justify-center shrink-0">
                              <Document class="w-5 h-5 text-white" />
                            </div>
                            <div class="flex-1 min-w-0">
                              <div class="text-sm font-medium text-white truncate">{{ parseAttachmentPayload(msg.payloadJson)?.fileName || '附件' }}</div>
                              <div class="text-xs text-brand-100/80">{{ formatFileSize(parseAttachmentPayload(msg.payloadJson)?.size || 0) }}</div>
                            </div>
                            <button 
                              class="shrink-0 px-3 py-1.5 bg-white/20 text-white text-xs font-bold rounded-lg hover:bg-white/30 transition-all "
                              @click="downloadAttachment(parseAttachmentPayload(msg.payloadJson)?.fileUrl || '', parseAttachmentPayload(msg.payloadJson)?.fileName || 'file')"
                            >
                              下载
                            </button>
                          </div>
                        </div>
                        <!-- 合同消息 -->
                        <ContractCard
                          v-else-if="(msg.msgType || '').toUpperCase() === 'CONTRACT' && parseContractPayload(msg.payloadJson)"
                          :payload="parseContractPayload(msg.payloadJson)!"
                          :is-sent="true"
                          @view="handleViewContract"
                          @sign="handleSignContract"
                        />
                        <!-- 普通文本消息 -->
                        <div v-else class="bg-brand-600 text-white rounded-lg rounded-tr-sm px-4 py-3 shadow-sm text-sm">
                          {{ msg.content }}
                        </div>
                        <div v-show="msg.status === 'pending'" class="text-[10px] text-gray-400 mt-1">发送中…</div>
                      </div>
                  </div>
                </div>
              </div>

              </div>
              <div v-else class="h-full flex items-center justify-center text-gray-400">
                <div class="text-center">
                  <el-icon class="mx-auto mb-4 text-gray-300" :size="64"><ChatDotRound /></el-icon>
                  <p class="text-sm font-medium">选择一个会话开始聊天</p>
                </div>
              </div>
            </div>

            <!-- 输入区域 -->
            <div v-if="activeConversationId" class="p-4 border-t border-gray-200 bg-white">
              <!-- 输入框上方的小工具栏 -->
              <div class="flex items-center gap-4 mb-3 px-1">
                <button 
                  class="flex items-center gap-1 text-xs font-bold text-brand-600 hover:text-brand-700 transition-colors"
                  @click="quotePopoverVisible = true"
                >
                  <Position class="w-3.5 h-3.5" /> 修改价格/发报价
                </button>
                <button class="flex items-center gap-1 text-xs font-bold text-gray-500 hover:text-gray-700 transition-colors" @click="openGiftDialog">
                  <Present class="w-3.5 h-3.5" /> 赠送积分
                </button>
                <button 
                  class="flex items-center gap-1 text-xs font-bold text-gray-500 hover:text-gray-700 transition-colors"
                  :disabled="uploading"
                  @click="openImagePicker"
                >
                  <Loading v-if="uploading && uploadType === 'image'" class="w-3.5 h-3.5 animate-spin" />
                  <Picture v-else class="w-3.5 h-3.5" />
                  {{ uploading && uploadType === 'image' ? `${uploadProgress}%` : '图片' }}
                </button>
                <button 
                  class="flex items-center gap-1 text-xs font-bold text-gray-500 hover:text-gray-700 transition-colors"
                  :disabled="uploading"
                  @click="openAttachmentPicker"
                >
                  <Loading v-if="uploading && uploadType === 'attachment'" class="w-3.5 h-3.5 animate-spin" />
                  <Document v-else class="w-3.5 h-3.5" />
                  {{ uploading && uploadType === 'attachment' ? `${uploadProgress}%` : '附件' }}
                </button>
                
                <!-- 隐藏的文件选择器 -->
                <input 
                  ref="imageInputRef"
                  type="file" 
                  accept="image/jpeg,image/png,image/gif,image/webp"
                  class="hidden"
                  @change="handleImageSelect"
                />
                <input 
                  ref="attachmentInputRef"
                  type="file" 
                  accept=".pdf,.doc,.docx,.xls,.xlsx,.zip,.rar,.txt"
                  class="hidden"
                  @change="handleAttachmentSelect"
                />
              </div>

              <div class="flex items-end gap-3">
                <!-- 输入框 -->
                <div class="flex-1">
                  <el-input
                    v-model="messageInput"
                    type="textarea"
                    :rows="2"
                    placeholder="按回车发送消息..."
                    resize="none"
                    @keydown.enter.prevent="sendMessage"
                  />
                </div>

                <!-- 发送按钮 -->
                <el-button
                  type="primary"
                  class="shrink-0 !h-12 !px-6 !rounded-lg !bg-brand-600 hover:!bg-brand-700 !border-brand-600 !text-white transition-all "
                  :disabled="!messageInput.trim()"
                  @click="sendMessage"
                >
                  发送
                </el-button>
              </div>
            </div>
          </div>

          <!-- 右侧边栏：交易轨迹 -->
          <div
            v-if="currentConversation && sidePanelOpen"
            class="hidden xl:flex w-64 shrink-0 border-l border-gray-200 bg-white flex-col p-6"
          >
            <div class="flex items-center justify-between mb-8">
              <div class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">交易轨迹</div>
              <button class="text-gray-400 hover:text-gray-600 transition-colors" @click="sidePanelOpen = false">
                <X class="w-4 h-4" />
              </button>
            </div>

            <el-steps direction="vertical" :active="transactionStep" finish-status="success" class="flex-1 transaction-steps">
              <el-step title="建立联系" description="双方正在初步沟通" />
              <el-step title="议价中" :description="peerLatestQuote ? '已有报价，待确认' : '正在商讨细节'" />
              <el-step title="达成意向" :description="hasAcceptedQuote ? '已达成交易意向' : '待确认报价'" />
              <el-step title="签署合同" :description="contractStepDesc" />
              <el-step title="履约完成" :description="fulfillmentStepDesc" />
            </el-steps>

            <div class="mt-8 pt-6 border-t border-gray-50">
              <div v-if="hasAcceptedQuote" class="space-y-3">
                <div class="bg-brand-50 text-brand-700 p-3 rounded-lg text-xs font-medium leading-relaxed">
                  🎉 意向已达成！建议立即起草电子合同以保障双方权益。
                </div>
                <el-button type="primary" class="w-full !rounded-lg !bg-gradient-to-r !from-brand-600 !to-teal-600 hover:!from-brand-700 hover:!to-teal-700 !border-transparent !shadow-md !shadow-brand-500/20" @click="initiateContract">
                  起草合同
                </el-button>
              </div>
              <div v-else class="text-center">
                <p class="text-[10px] text-gray-400 leading-relaxed px-2">
                  双方达成一致后，点击报价单上的“确认下单”即可推进至下一步。
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 桌面端：悬浮式议价面板 -->
    <el-drawer
      v-model="quotePopoverVisible"
      direction="rtl"
      :size="isBasisTrade ? 520 : 720"
      :with-header="false"
      class="floating-quote-drawer"
      :modal-class="'bg-slate-900/10 backdrop-blur-[2px]'"
    >
      <!-- pt-0 确保面板紧贴容器顶部（即距离屏幕顶部 10px），px/pb 为阴影留出空间 -->
      <div class="h-full flex flex-col pt-0 px-4 pb-4 bg-transparent overflow-y-auto">
        <BasisBargainPanel
          v-if="isBasisTrade"
          :disabled="!activeConversationId"
          :subject-snapshot-json="currentConversation?.subjectSnapshotJson"
          @send="({ payload, summary, basisPrice, contractCode }) => { sendQuote(payload, summary, basisPrice, contractCode); quotePopoverVisible = false }"
        />
        <NegotiationPanel
          v-else
          :disabled="!activeConversationId"
          :peer-latest-quote="peerLatestQuote"
          :subject-snapshot-json="currentConversation?.subjectSnapshotJson"
          @send="({ payload, summary }) => { sendQuote(payload, summary); quotePopoverVisible = false }"
        />
      </div>
    </el-drawer>

    <!-- 小屏：结构化议价抽屉（<xl） -->
    <el-drawer
      v-model="negotiationDrawerOpen"
      direction="rtl"
      size="92%"
      :with-header="false"
    >
      <div class="h-full flex flex-col bg-white">
        <div class="px-6 py-5 border-b border-gray-200 flex items-center justify-between">
          <div>
            <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">结构化议价</div>
            <div class="font-bold text-gray-900">报价草稿</div>
          </div>
          <button class="p-2 rounded-full hover:bg-gray-50 transition-all " @click="negotiationDrawerOpen = false">
            <span class="text-gray-500 text-sm font-bold">关闭</span>
          </button>
        </div>
        <div class="flex-1 overflow-y-auto p-6 bg-gray-50 space-y-4">
          <div v-if="currentConversation" class="bg-white rounded-lg border border-gray-200 shadow-sm p-4">
            <div class="flex items-start justify-between gap-4">
              <div class="min-w-0">
                <div class="flex items-center gap-2">
                  <span
                    class="text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 rounded-full border"
                    :class="subjectBadge(currentConversation).cls"
                  >
                    {{ subjectBadge(currentConversation).label }}
                  </span>
                  <span class="text-xs text-gray-400">标的ID：{{ currentConversation.subjectId ?? '-' }}</span>
                </div>
                <div class="mt-1 font-bold text-gray-900 truncate">
                  {{ currentConversation.subjectSnapshotJson ? '已附带标的快照' : '未附带标的快照（建议从大厅入口带上）' }}
                </div>
              </div>
              <el-button size="small" class="!rounded-lg transition-all " @click="viewLinkedInfo">查看</el-button>
            </div>
          </div>

          <BasisBargainPanel
            v-if="isBasisTrade"
            :disabled="!activeConversationId"
            :subject-snapshot-json="currentConversation?.subjectSnapshotJson"
            @send="({ payload, summary, basisPrice, contractCode }) => { sendQuote(payload, summary, basisPrice, contractCode); negotiationDrawerOpen = false }"
          />
          <NegotiationPanel
            v-else
            :disabled="!activeConversationId"
            :peer-latest-quote="peerLatestQuote"
            :subject-snapshot-json="currentConversation?.subjectSnapshotJson"
            @send="({ payload, summary }) => { sendQuote(payload, summary); negotiationDrawerOpen = false }"
          />
        </div>
      </div>
    </el-drawer>

    <!-- 标的详情弹窗（Soft Glass） -->
    <el-dialog
      v-model="subjectDialogOpen"
      width="720px"
      align-center
      :show-close="false"
      :append-to-body="true"
      modal-class="bg-slate-900/60 backdrop-blur-sm"
      class="!rounded-2xl overflow-hidden"
    >
      <div class="bg-white">
        <div class="px-8 py-6 border-b flex items-center justify-between bg-white sticky top-0 z-10">
          <div>
            <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">标的详情</div>
            <div class="text-xl font-bold text-gray-900">核对快照与条款</div>
          </div>
          <button
            class="px-4 py-2 rounded-full border border-gray-200 hover:bg-gray-50 transition-all  text-sm font-bold text-gray-700"
            @click="subjectDialogOpen = false"
          >
            关闭
          </button>
        </div>

        <div class="p-8 bg-gray-50 space-y-4">
          <ChatSubjectCard
            v-if="currentConversation"
            :subject-type="currentConversation.subjectType"
            :subject-id="currentConversation.subjectId ?? null"
            :subject-snapshot-json="currentConversation.subjectSnapshotJson ?? null"
          />

          <div class="bg-white rounded-lg border border-gray-200 shadow-sm p-4">
            <div class="flex items-center justify-between gap-4">
              <div class="min-w-0">
                <div class="text-sm font-bold text-gray-900">为什么需要“标的详情”</div>
                <div class="text-xs text-gray-500 mt-1">
                  这里展示全量快照/附件/参数（可追溯），用于核对口径；右侧议价面板只负责生成报价卡与对比。
                </div>
              </div>
              <div class="flex items-center gap-2 shrink-0">
                <el-button size="small" class="!rounded-lg transition-all " @click="copySubjectId">复制ID</el-button>
                <el-button size="small" class="!rounded-lg !bg-gradient-to-r !from-brand-600 !to-teal-600 hover:!from-brand-700 hover:!to-teal-700 !border-transparent !text-white transition-all  !shadow-md" @click="openSubjectOrigin">
                  打开原始发布
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 赠送积分对话框 (Soft Glass 风格) -->
    <el-dialog 
      v-model="giftDialogVisible" 
      width="420px"
      :close-on-click-modal="false"
      :show-close="false"
      align-center
      modal-class="bg-slate-900/60 backdrop-blur-sm"
      class="!rounded-2xl overflow-hidden !border-none"
    >
      <template #header>
        <div class="flex items-center justify-between">
          <div>
            <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">积分赠送</div>
            <div class="text-xl font-bold text-gray-900">赠送积分</div>
          </div>
          <button 
            class="w-8 h-8 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-all "
            @click="giftDialogVisible = false"
          >
            <span class="text-gray-500 text-sm">✕</span>
          </button>
        </div>
      </template>

      <div class="space-y-5">
        <!-- 接收人信息卡片 -->
        <div class="bg-gray-50 rounded-lg p-4 border border-gray-200">
          <div class="flex items-center gap-3">
            <div 
              class="w-12 h-12 rounded-lg bg-gradient-to-br flex items-center justify-center text-white font-bold text-lg shrink-0 shadow-md"
              :class="avatarGradient(currentConversation?.peerNickName || currentConversation?.peerUserName || currentConversation?.peerCompanyName)"
            >
              {{ avatarText(currentConversation?.peerNickName || currentConversation?.peerUserName || currentConversation?.peerCompanyName) }}
            </div>
            <div class="flex-1 min-w-0">
              <div class="font-bold text-gray-900 truncate">{{ peerDisplayName }}</div>
              <div class="text-xs text-gray-400 mt-0.5">积分将直接转入对方账户</div>
            </div>
          </div>
        </div>
        
        <!-- 积分数量 -->
        <div>
          <label class="block text-sm font-bold text-gray-700 mb-2">赠送数量</label>
          <el-input-number 
            v-model="giftForm.points" 
            :min="1" 
            :max="10000" 
            :step="10"
            class="!w-full"
            size="large"
          />
        </div>

        <!-- 留言输入 -->
        <div>
          <label class="block text-sm font-bold text-gray-700 mb-2">赠送留言 <span class="text-gray-400 font-normal">(可选)</span></label>
          <el-input 
            v-model="giftForm.remark" 
            placeholder="感谢合作，期待下次合作..." 
            maxlength="100" 
            show-word-limit
            class="!rounded-lg"
          />
        </div>
        
        <!-- 快捷选择 -->
        <div class="pt-4 border-t border-gray-200">
          <div class="text-xs text-gray-400 mb-3">快捷选择</div>
          <div class="flex flex-wrap gap-2">
            <button 
              v-for="amt in [10, 50, 100, 500]" 
              :key="amt"
              class="px-4 py-2 rounded-full text-sm font-medium transition-all "
              :class="giftForm.points === amt 
                ? 'bg-brand-600 text-white' 
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
              @click="giftForm.points = amt"
            >
              {{ amt }} 积分
            </button>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="flex gap-3">
          <el-button 
            class="flex-1 !rounded-lg !h-11 transition-all " 
            @click="giftDialogVisible = false"
          >
            取消
          </el-button>
          <el-button 
            type="primary" 
            class="flex-1 !rounded-lg !h-11 !bg-brand-600 hover:!bg-brand-700 !border-brand-600 transition-all "
            :loading="giftLoading"
            @click="submitGiftPoints"
          >
            确认赠送 {{ giftForm.points }} 积分
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 起草合同弹窗 -->
    <ContractDraftModal
      v-model="contractDraftVisible"
      :quote-message-id="draftQuoteMessageId || 0"
      :party-a="currentConversation?.peerCompanyName || currentConversation?.peerNickName || ''"
      :party-b="auth.me?.nickName || auth.me?.userName || ''"
      :product-name="getSubjectProductName()"
      :quantity="draftQuoteData?.quantity || ''"
      :unit="'吨'"
      :unit-price="draftQuoteData?.price || (draftQuoteData as any)?.referencePrice || ''"
      :delivery-place="draftQuoteData?.deliveryPlace || ''"
      :arrival-date="draftQuoteData?.arrivalDate || ''"
    :payment-method="draftQuoteData?.paymentMethod || ''"
    :params-json="currentConversation?.subjectSnapshotJson"
    :basis-price="(draftQuoteData as any)?.basisPrice"
    :contract-code="(draftQuoteData as any)?.contractCode"
    :futures-price="(draftQuoteData as any)?.futuresPrice"
    @success="onContractCreated"
    />

    <!-- 签署合同弹窗 -->
    <ContractSignModal
      v-model="signModalVisible"
      :contract-id="signContractId"
      @signed="onContractSigned"
    />

    <!-- 已归档会话弹窗 -->
    <Teleport to="body">
      <Transition name="fade">
        <div 
          v-if="showArchivedModal" 
          class="fixed inset-0 z-[9998] bg-slate-900/60 backdrop-blur-sm flex items-center justify-center p-4"
          @click.self="showArchivedModal = false"
        >
          <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden animate-zoom-in">
            <!-- 头部 -->
            <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
              <div>
                <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">会话管理</div>
                <h2 class="text-lg font-bold text-gray-900">已归档的会话</h2>
              </div>
              <button 
                class="w-8 h-8 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-all "
                @click="showArchivedModal = false"
              >
                <span class="text-gray-500 text-sm">✕</span>
              </button>
            </div>
            
            <!-- 内容 -->
            <div class="max-h-[60vh] overflow-y-auto">
              <div v-if="archivedConversations.length === 0" class="py-12 text-center">
                <div class="w-16 h-16 mx-auto mb-4 rounded-lg bg-gray-100 flex items-center justify-center">
                  <span class="text-3xl">📦</span>
                </div>
                <p class="text-sm font-medium text-gray-500">暂无归档会话</p>
                <p class="text-xs text-gray-400 mt-1">归档的会话将显示在这里</p>
              </div>
              
              <div v-else class="divide-y divide-gray-50">
                <div 
                  v-for="conv in archivedConversations" 
                  :key="conv.id"
                  class="px-6 py-4 hover:bg-gray-50 transition-colors"
                >
                  <div class="flex items-center justify-between gap-4">
                    <div class="flex-1 min-w-0">
                      <div class="flex items-center gap-2">
                        <span 
                          class="text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 rounded-full border"
                          :class="subjectBadge(conv).cls"
                        >
                          {{ subjectBadge(conv).label }}
                        </span>
                        <span class="text-sm font-bold text-gray-900 truncate">
                          {{ getSubjectShortName(conv) || '未命名话题' }}
                        </span>
                      </div>
                      <div class="mt-1 text-xs text-gray-500 truncate">
                        {{ conv.peerNickName || conv.peerUserName || '对方' }} · {{ conv.lastContent || '暂无消息' }}
                      </div>
                    </div>
                    <button 
                      class="shrink-0 px-3 py-1.5 bg-brand-50 hover:bg-brand-100 text-brand-600 text-xs font-bold rounded-lg transition-all "
                      @click="restoreConversation(conv.id)"
                    >
                      恢复
                    </button>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 底部 -->
            <div class="px-6 py-4 bg-gray-50 border-t border-gray-200">
              <p class="text-[10px] text-gray-400 text-center">
                归档的会话不会显示在列表中，但聊天记录会保留
              </p>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 图片预览弹窗 -->
    <Teleport to="body">
      <Transition name="fade">
        <div 
          v-if="imagePreviewVisible" 
          class="fixed inset-0 z-[9999] bg-black/90 flex items-center justify-center"
          @click="closeImagePreview"
        >
          <button 
            class="absolute top-4 right-4 w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center transition-all"
            @click="closeImagePreview"
          >
            <span class="text-white text-xl">✕</span>
          </button>
          <img 
            :src="imagePreviewUrl" 
            class="max-w-[90vw] max-h-[90vh] object-contain rounded-lg shadow-2xl"
            @click.stop
          />
        </div>
      </Transition>
    </Teleport>
    </div>
</template>
<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.chat-view {
  height: calc(100vh - 140px);
  min-height: 500px;
}

:deep(.el-textarea__inner) {
  border-radius: 12px;
}

:deep(.transaction-steps .el-step__title) {
  font-size: var(--font-xs);
  font-weight: var(--font-weight-bold);
}

:deep(.transaction-steps .el-step__description) {
  font-size: var(--font-xs);
  line-height: var(--leading-snug);
}

:deep(.transaction-steps .el-step__icon) {
  width: 20px;
  height: 20px;
  font-size: var(--font-xs);
}

/* 联系人列表入场动画 */
@keyframes slide-in-left {
  from {
    opacity: 0;
    transform: translateX(-12px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 联系人卡片悬停效果 */
.peer-card {
  animation: slide-in-left 0.3s ease-out backwards;
}

/* 选中指示条动画 */
.indicator-bar {
  transition: all 0.2s ease-out;
}

/* 未读角标脉冲动画 */
@keyframes pulse-badge {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.9;
  }
}

.unread-badge {
  animation: pulse-badge 2s ease-in-out infinite;
}

/* 标的切换器标签过渡 */
.subject-tab {
  transition: all 0.2s ease-out;
}

.subject-tab:hover {
  transform: translateY(-1px);
}

/* 消息气泡入场动画 */
@keyframes message-in-left {
  from {
    opacity: 0;
    transform: translateX(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}

@keyframes message-in-right {
  from {
    opacity: 0;
    transform: translateX(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}

.message-received {
  animation: message-in-left 0.25s ease-out;
}

.message-sent {
  animation: message-in-right 0.25s ease-out;
}

/* 滚动条美化 */
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.2);
}
</style>

<style>
/* Popover 内容是 teleport 到 body 的，需使用非 scoped 样式 */
.chat-quote-popper {
  max-height: calc(100vh - 140px);
  overflow: auto;
}

/* 悬浮式议价面板样式 */
.floating-quote-drawer {
  background-color: transparent !important;
}

.floating-quote-drawer .el-drawer {
  top: 10px !important;
  right: 10px !important;
  bottom: 10px !important;
  height: calc(100vh - 20px) !important;
  background-color: transparent !important;
  box-shadow: none !important;
}

.floating-quote-drawer .el-drawer__body {
  padding: 0 !important;
  overflow: visible !important;
}
</style>
