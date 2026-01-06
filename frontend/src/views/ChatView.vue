<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'
import { Search, Picture, Document, Position, Present, ChatDotRound, Star, StarFilled, Loading } from '@element-plus/icons-vue'
import { giftPoints } from '../api/points'
import { uploadImage, uploadAttachment, formatFileSize, type ImagePayload, type AttachmentPayload } from '../api/file'
import { useRoute, useRouter } from 'vue-router'
import { getConversationMessages, listChatConversations, markConversationRead, confirmChatOffer, type ChatConversationResponse, type ChatMessageResponse } from '../api/chat'
import { followUser, unfollowUser, checkFollowStatus } from '../api/follow'
import NegotiationPanel, { type QuoteFields } from '../components/chat/NegotiationPanel.vue'
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

// 会话列表
const conversations = ref<ChatConversationResponse[]>([])

// 消息列表
type UiMessage = {
  id: string | number
  type: 'system' | 'received' | 'sent'
  msgType?: string
  content: string
  payloadJson?: string
  quoteStatus?: string
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

const negotiationBtnLabel = computed(() => {
  if (isDesktopXl.value) return sidePanelOpen.value ? '收起议价' : '展开议价'
  return '议价面板'
})

function toggleNegotiationPanel() {
  if (isDesktopXl.value) {
    sidePanelOpen.value = !sidePanelOpen.value
    return
  }
  negotiationDrawerOpen.value = true
}

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

const filteredConversations = computed(() => {
  if (!searchKeyword.value) return conversations.value
  const kw = searchKeyword.value.toLowerCase()
  return conversations.value.filter(c =>
    `${c.peerCompanyName || ''}${c.peerNickName || ''}${c.peerUserName || ''}`.toLowerCase().includes(kw) ||
    `${c.lastContent || ''}`.toLowerCase().includes(kw)
  )
})

const currentConversation = computed(() => {
  return conversations.value.find(c => c.id === activeConversationId.value) || null
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
  nextTick().then(scrollToBottom)
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

  nextTick().then(scrollToBottom)
}

async function handleConfirmOffer(msgId: number | string) {
  if (typeof msgId !== 'number') return
  try {
    const res = await confirmChatOffer(msgId)
    if (res.code === 0) {
      ElMessage.success('报价已确认，交易达成！')
      // 后端会通过 WS 广播通知，所以这里其实不用手动更新本地状态，等推送即可
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '确认失败')
  }
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
    nextTick().then(scrollToBottom)
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
    time: formatTime(m.createTime),
    fromUserId: m.fromUserId,
    toUserId: m.toUserId
  }
}

function parseQuoteFields(payloadJson?: string): QuoteFields | null {
  if (!payloadJson) return null
  try {
    const obj = JSON.parse(payloadJson)
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

const transactionStep = computed(() => {
  if (hasAcceptedQuote.value) return 2 // 达成意向
  if (messages.value.some(m => m.msgType === 'QUOTE')) return 1 // 议价中
  return 0 // 建立联系
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

    if (conversations.value.length > 0 && !activeConversationId.value) {
      const first = conversations.value[0]
      if (first) await selectConversation(first)
    } else if (conversations.value.length === 0) {
      activeConversationId.value = null
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

function subjectBadge(c: ChatConversationResponse) {
  const st = (c.subjectType || '').toUpperCase()
  if (st === 'SUPPLY') return { label: '供应', cls: 'bg-emerald-50 text-emerald-700 border-emerald-100' }
  if (st === 'NEED') return { label: '采购', cls: 'bg-blue-50 text-blue-700 border-blue-200' }
  return { label: '会话', cls: 'bg-gray-50 text-gray-600 border-gray-200' }
}

function quoteStatusBadge(status?: string) {
  if (status === 'OFFERED') return { label: '待确认', cls: 'bg-blue-50 text-blue-600 border-blue-100' }
  if (status === 'ACCEPTED') return { label: '已达成', cls: 'bg-emerald-50 text-emerald-600 border-emerald-100' }
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
  const fields = parseQuoteFields(payloadJson)
  if (!fields) return []
  const display: Array<{ label: string; value: any }> = []
  
  // 基础字段
  Object.entries(fields)
    .filter(([k, v]) => v && QUOTE_LABEL_MAP[k])
    .forEach(([k, v]) => {
      display.push({ label: QUOTE_LABEL_MAP[k], value: v })
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

// 选择会话
async function selectConversation(c: ChatConversationResponse) {
  activeConversationId.value = c.id
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

// 加载消息（conversation 维度）
async function loadMessages(conversationId: number) {
  loading.value = true
  try {
    const res = await getConversationMessages(conversationId, 100)
    if (res.code !== 0) throw new Error(res.message)
    messages.value = (res.data || []).map(mapApiMessageToUi)
    
    // 滚动到底部
    await nextTick()
    scrollToBottom()
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
  
  // 连接未就绪：不做“假发送”，避免出现永远 pending 的消息
  if (!ws.value || ws.value.readyState !== WebSocket.OPEN) {
    ElMessage.warning('实时连接未就绪，正在重连…')
    connectWs()
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
  scrollToBottom()

  ws.value.send(JSON.stringify({
    type: 'SEND',
    conversationId: activeConversationId.value,
    msgType: 'TEXT',
    content,
    tempId
  }))
}

async function sendQuote(payload: any, summary: string) {
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
  scrollToBottom()

  ws.value.send(JSON.stringify({
    type: 'SEND',
    conversationId: activeConversationId.value,
    msgType: 'QUOTE',
    content: summary || '',
    payload,
    tempId
  }))
}

// 滚动到底部
function scrollToBottom() {
  if (chatContainerRef.value) {
    chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
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
function onContractCreated(contractId: number) {
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
    scrollToBottom()
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
    const res = type === 'image' 
      ? await uploadImage(file, (p) => { uploadProgress.value = p })
      : await uploadAttachment(file, (p) => { uploadProgress.value = p })
    
    if (res.code !== 0) {
      throw new Error(res.message || '上传失败')
    }
    
    const fileData = res.data
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
    scrollToBottom()
    
    // 通过 WebSocket 发送
    ws.value.send(JSON.stringify({
      type: 'SEND',
      conversationId: activeConversationId.value,
      msgType,
      content: summary,
      payload,
      tempId
    }))
    
    ElMessage.success(type === 'image' ? '图片已发送' : '附件已发送')
  } catch (e: any) {
    ElMessage.error(e?.message || '上传失败，请重试')
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

onMounted(() => {
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
    <div class="flex h-full bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
      <!-- 左侧会话列表 -->
      <div class="w-80 border-r border-gray-100 flex flex-col">
        <!-- 搜索栏 -->
        <div class="p-4 border-b border-gray-100">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索会话"
            :prefix-icon="Search"
            clearable
          />
        </div>

        <!-- 会话列表 -->
        <div class="flex-1 overflow-y-auto">
          <div
            v-for="c in filteredConversations"
            :key="c.id"
            class="px-4 py-3 cursor-pointer hover:bg-gray-50/50 transition-all border-b border-gray-50"
            :class="{ 'bg-emerald-50/60': activeConversationId === c.id }"
            @click="selectConversation(c)"
          >
            <div class="flex items-start gap-3">
              <!-- 头像 -->
              <div class="w-12 h-12 rounded-2xl flex items-center justify-center text-white font-bold shrink-0 bg-slate-900">
                {{ avatarText(c.peerNickName || c.peerUserName || c.peerCompanyName) }}
              </div>
              
              <!-- 会话信息 -->
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between">
                  <span class="font-bold text-gray-900 truncate">
                    {{ c.peerNickName || c.peerUserName || '对方' }}
                  </span>
                  <span class="text-xs text-gray-400 shrink-0 ml-2">{{ formatTime(c.lastTime) }}</span>
                </div>
                <div class="text-sm text-gray-500 truncate mt-1">{{ c.lastContent || '暂无消息' }}</div>
                <!-- 关联信息标签 -->
                <div class="mt-2 flex items-center gap-2">
                  <span class="text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 rounded-full border"
                        :class="subjectBadge(c).cls">
                    {{ subjectBadge(c).label }}
                  </span>
                  <span class="text-xs text-gray-400 truncate" v-if="c.peerCompanyName">{{ c.peerCompanyName }}</span>
                </div>
              </div>
              
              <!-- 未读标记 -->
              <div v-if="(c.unreadCount || 0) > 0" class="min-w-5 h-5 px-1 rounded-full bg-red-500 text-white text-xs flex items-center justify-center shrink-0">
                {{ c.unreadCount }}
              </div>
            </div>
          </div>

          <div v-if="filteredConversations.length === 0" class="py-12 text-center text-gray-400">
            暂无会话
          </div>
        </div>
      </div>

      <!-- 右侧聊天区域 -->
      <div class="flex-1 flex flex-col min-w-0">
        <!-- 顶部信息栏简化 -->
        <div v-if="currentConversation" class="px-6 py-3 border-b border-gray-100 bg-white">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-2xl bg-slate-900 flex items-center justify-center text-white font-bold shrink-0">
                {{ avatarText(currentConversation.peerNickName || currentConversation.peerUserName || currentConversation.peerCompanyName) }}
              </div>
              <div>
                <div class="font-bold text-gray-900 leading-tight">{{ peerDisplayName }}</div>
                <div class="text-[10px] flex items-center gap-1.5 mt-0.5">
                  <span :class="wsConnected ? 'text-emerald-500' : 'text-gray-400'">
                    ● {{ wsConnected ? '在线' : '连接中…' }}
                  </span>
                  <span class="text-gray-300">|</span>
                  <span class="text-gray-400 truncate">{{ currentConversation.peerCompanyName || '个人用户' }}</span>
                </div>
              </div>
            </div>
            
            <div class="flex items-center gap-2">
              <!-- 关注按钮 -->
              <el-button 
                size="small" 
                :loading="followLoading"
                :type="isFollowingPeer ? 'warning' : 'default'"
                class="!rounded-xl transition-all active:scale-95"
                @click="toggleFollowPeer"
              >
                <template #icon>
                  <StarFilled v-if="isFollowingPeer" class="w-4 h-4" />
                  <Star v-else class="w-4 h-4" />
                </template>
                {{ isFollowingPeer ? '已关注' : '关注' }}
              </el-button>
              <el-button size="small" circle :icon="Present" @click="openGiftDialog" title="赠送积分" />
              <el-button size="small" class="!rounded-xl !bg-slate-900 !text-white transition-all active:scale-95" @click="initiateContract">
                起草合同
              </el-button>
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
                <button class="text-xs text-emerald-600 font-bold hover:text-emerald-700 transition-colors" @click="viewLinkedInfo">
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
                  <div v-if="idx === 0 || msg.time !== messages[idx-1].time" class="self-center my-2">
                    <span class="text-[10px] text-gray-400 bg-gray-200/50 px-2 py-0.5 rounded-full">{{ msg.time }}</span>
                  </div>

                  <div class="flex" :class="msg.type === 'sent' ? 'justify-end' : msg.type === 'system' ? 'justify-center' : 'justify-start'">
                    <!-- 系统消息 -->
                    <div v-if="msg.type === 'system'" class="px-4 py-1.5 bg-gray-200/80 text-gray-500 text-[11px] rounded-full max-w-[80%] text-center">
                      {{ msg.content }}
                    </div>
                    
                    <!-- 接收的消息 -->
                    <div v-else-if="msg.type === 'received'" class="flex items-start gap-3 max-w-[85%] lg:max-w-[70%]">
                      <div class="w-8 h-8 rounded-xl bg-slate-900 flex items-center justify-center text-white text-xs font-bold shrink-0">
                        {{ avatarText(currentConversation?.peerNickName || currentConversation?.peerUserName || currentConversation?.peerCompanyName) }}
                      </div>
                      <div>
                        <!-- 报价消息 -->
                        <div v-if="(msg.msgType || '').toUpperCase() === 'QUOTE'" class="bg-white rounded-2xl rounded-tl-sm px-4 py-3 shadow-sm border border-gray-100">
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
                              class="!rounded-xl !bg-emerald-600 hover:!bg-emerald-700 !border-emerald-600 !text-white transition-all active:scale-95"
                              @click="handleConfirmOffer(msg.id)"
                            >
                              确认成交
                            </el-button>
                          </div>
                          <!-- 已成交：起草合同按钮 -->
                          <div v-else-if="msg.quoteStatus === 'ACCEPTED'" class="mt-4 pt-3 border-t border-gray-50 flex justify-end">
                            <button 
                              class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold rounded-xl transition-all active:scale-95 flex items-center gap-1.5"
                              @click="openContractDraft(msg)"
                            >
                              <Document class="w-4 h-4" />
                              起草合同
                            </button>
                          </div>
                        </div>
                        <!-- 图片消息 -->
                        <div v-else-if="(msg.msgType || '').toUpperCase() === 'IMAGE'" class="bg-white rounded-2xl rounded-tl-sm p-2 shadow-sm border border-gray-100">
                          <img 
                            v-if="parseImagePayload(msg.payloadJson)?.fileUrl"
                            :src="parseImagePayload(msg.payloadJson)?.fileUrl"
                            :alt="parseImagePayload(msg.payloadJson)?.fileName || '图片'"
                            class="max-w-[280px] max-h-[200px] rounded-xl cursor-pointer hover:opacity-90 transition-opacity object-cover"
                            @click="openImagePreview(parseImagePayload(msg.payloadJson)?.fileUrl || '')"
                          />
                          <div v-else class="text-sm text-gray-500">[图片加载失败]</div>
                        </div>
                        <!-- 附件消息 -->
                        <div v-else-if="(msg.msgType || '').toUpperCase() === 'ATTACHMENT'" class="bg-white rounded-2xl rounded-tl-sm px-4 py-3 shadow-sm border border-gray-100">
                          <div class="flex items-center gap-3">
                            <div class="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center shrink-0">
                              <Document class="w-5 h-5 text-blue-600" />
                            </div>
                            <div class="flex-1 min-w-0">
                              <div class="text-sm font-medium text-gray-900 truncate">{{ parseAttachmentPayload(msg.payloadJson)?.fileName || '附件' }}</div>
                              <div class="text-xs text-gray-400">{{ formatFileSize(parseAttachmentPayload(msg.payloadJson)?.size || 0) }}</div>
                            </div>
                            <button 
                              class="shrink-0 px-3 py-1.5 bg-blue-50 text-blue-600 text-xs font-bold rounded-lg hover:bg-blue-100 transition-all active:scale-95"
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
                        <div v-else class="bg-white rounded-2xl rounded-tl-sm px-4 py-3 shadow-sm border border-gray-100 text-sm text-gray-800">
                          {{ msg.content }}
                        </div>
                      </div>
                    </div>
                    
                    <!-- 发送的消息 -->
                    <div v-else class="flex items-start gap-3 max-w-[85%] lg:max-w-[70%] flex-row-reverse">
                      <div class="w-8 h-8 rounded-xl bg-emerald-600 flex items-center justify-center text-white text-xs font-bold shrink-0">
                        {{ (auth.me?.nickName || 'U')[0] }}
                      </div>
                      <div class="flex flex-col items-end">
                        <!-- 报价消息 -->
                        <div v-if="(msg.msgType || '').toUpperCase() === 'QUOTE'" class="bg-emerald-600 text-white rounded-2xl rounded-tr-sm px-4 py-3 shadow-sm">
                          <div class="flex items-center justify-between mb-2 gap-4">
                            <div class="text-[10px] font-bold uppercase tracking-widest text-emerald-100">电子报价单</div>
                            <div v-if="quoteStatusBadge(msg.quoteStatus)" 
                                 class="text-[10px] px-1.5 py-0.5 rounded-full border border-emerald-400 bg-emerald-500/50 font-bold text-white">
                              {{ quoteStatusBadge(msg.quoteStatus)?.label }}
                            </div>
                          </div>
                          <div class="mt-1 font-bold border-b border-emerald-500 pb-2 mb-3">{{ msg.content || '[报价]' }}</div>
                          <div class="space-y-2">
                            <div v-for="field in getQuoteDisplayFields(msg.payloadJson)" :key="field.label" class="flex justify-between text-xs">
                              <span class="text-emerald-100/80">{{ field.label }}</span>
                              <span class="text-white font-medium">{{ field.value }}</span>
                            </div>
                          </div>
                        </div>
                        <!-- 图片消息 -->
                        <div v-else-if="(msg.msgType || '').toUpperCase() === 'IMAGE'" class="bg-emerald-600 rounded-2xl rounded-tr-sm p-2 shadow-sm">
                          <img 
                            v-if="parseImagePayload(msg.payloadJson)?.fileUrl"
                            :src="parseImagePayload(msg.payloadJson)?.fileUrl"
                            :alt="parseImagePayload(msg.payloadJson)?.fileName || '图片'"
                            class="max-w-[280px] max-h-[200px] rounded-xl cursor-pointer hover:opacity-90 transition-opacity object-cover"
                            @click="openImagePreview(parseImagePayload(msg.payloadJson)?.fileUrl || '')"
                          />
                          <div v-else class="text-sm text-white/80">[图片加载失败]</div>
                        </div>
                        <!-- 附件消息 -->
                        <div v-else-if="(msg.msgType || '').toUpperCase() === 'ATTACHMENT'" class="bg-emerald-600 rounded-2xl rounded-tr-sm px-4 py-3 shadow-sm">
                          <div class="flex items-center gap-3">
                            <div class="w-10 h-10 rounded-xl bg-white/20 flex items-center justify-center shrink-0">
                              <Document class="w-5 h-5 text-white" />
                            </div>
                            <div class="flex-1 min-w-0">
                              <div class="text-sm font-medium text-white truncate">{{ parseAttachmentPayload(msg.payloadJson)?.fileName || '附件' }}</div>
                              <div class="text-xs text-emerald-100/80">{{ formatFileSize(parseAttachmentPayload(msg.payloadJson)?.size || 0) }}</div>
                            </div>
                            <button 
                              class="shrink-0 px-3 py-1.5 bg-white/20 text-white text-xs font-bold rounded-lg hover:bg-white/30 transition-all active:scale-95"
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
                        <div v-else class="bg-emerald-600 text-white rounded-2xl rounded-tr-sm px-4 py-3 shadow-sm text-sm">
                          {{ msg.content }}
                        </div>
                        <div v-if="msg.status === 'pending'" class="text-[10px] text-gray-400 mt-1">发送中…</div>
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
            <div v-if="activeConversationId" class="p-4 border-t border-gray-100 bg-white">
              <!-- 输入框上方的小工具栏 -->
              <div class="flex items-center gap-4 mb-3 px-1">
                <el-popover placement="top-start" :width="700" trigger="click" v-model:visible="quotePopoverVisible" popper-class="!p-0 !rounded-[32px] !border-none !shadow-2xl">
                  <template #reference>
                    <button class="flex items-center gap-1 text-xs font-bold text-emerald-600 hover:text-emerald-700 transition-colors">
                      <Position class="w-3.5 h-3.5" /> 修改价格/发报价
                    </button>
                  </template>
                  <div class="p-0">
                    <NegotiationPanel
                      :disabled="!activeConversationId"
                      :peer-latest-quote="peerLatestQuote"
                      :subject-snapshot-json="currentConversation?.subjectSnapshotJson"
                      @send="({ payload, summary }) => sendQuote(payload, summary)"
                    />
                  </div>
                </el-popover>
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
                  class="shrink-0 !h-12 !px-6 !rounded-xl !bg-emerald-600 hover:!bg-emerald-700 !border-emerald-600 !text-white transition-all active:scale-95"
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
            class="hidden xl:flex w-64 shrink-0 border-l border-gray-100 bg-white flex-col p-6"
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
              <el-step title="签署合同" description="由意向生成正式合同" />
              <el-step title="履约完成" />
            </el-steps>

            <div class="mt-8 pt-6 border-t border-gray-50">
              <div v-if="hasAcceptedQuote" class="space-y-3">
                <div class="bg-emerald-50 text-emerald-700 p-3 rounded-xl text-xs font-medium leading-relaxed">
                  🎉 意向已达成！建议立即起草电子合同以保障双方权益。
                </div>
                <el-button type="primary" class="w-full !rounded-xl !bg-slate-900 !border-slate-900" @click="initiateContract">
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

    <!-- 小屏：结构化议价抽屉（<xl） -->
    <el-drawer
      v-model="negotiationDrawerOpen"
      direction="rtl"
      size="92%"
      :with-header="false"
    >
      <div class="h-full flex flex-col bg-white">
        <div class="px-6 py-5 border-b border-gray-100 flex items-center justify-between">
          <div>
            <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">结构化议价</div>
            <div class="font-bold text-gray-900">报价草稿</div>
          </div>
          <button class="p-2 rounded-full hover:bg-gray-50 transition-all active:scale-95" @click="negotiationDrawerOpen = false">
            <span class="text-gray-500 text-sm font-bold">关闭</span>
          </button>
        </div>
        <div class="flex-1 overflow-y-auto p-6 bg-gray-50 space-y-4">
          <div v-if="currentConversation" class="bg-white rounded-2xl border border-gray-100 shadow-sm p-4">
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
              <el-button size="small" class="!rounded-xl transition-all active:scale-95" @click="viewLinkedInfo">查看</el-button>
            </div>
          </div>

          <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-4">
            <NegotiationPanel
              :disabled="!activeConversationId"
              :peer-latest-quote="peerLatestQuote"
              @send="({ payload, summary }) => sendQuote(payload, summary)"
            />
          </div>
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
      class="!rounded-[32px] overflow-hidden"
    >
      <div class="bg-white">
        <div class="px-8 py-6 border-b flex items-center justify-between bg-white sticky top-0 z-10">
          <div>
            <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">标的详情</div>
            <div class="text-xl font-bold text-gray-900">核对快照与条款</div>
          </div>
          <button
            class="px-4 py-2 rounded-full border border-gray-200 hover:bg-gray-50 transition-all active:scale-95 text-sm font-bold text-gray-700"
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

          <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-4">
            <div class="flex items-center justify-between gap-4">
              <div class="min-w-0">
                <div class="text-sm font-bold text-gray-900">为什么需要“标的详情”</div>
                <div class="text-xs text-gray-500 mt-1">
                  这里展示全量快照/附件/参数（可追溯），用于核对口径；右侧议价面板只负责生成报价卡与对比。
                </div>
              </div>
              <div class="flex items-center gap-2 shrink-0">
                <el-button size="small" class="!rounded-xl transition-all active:scale-95" @click="copySubjectId">复制ID</el-button>
                <el-button size="small" class="!rounded-xl !bg-slate-900 hover:!bg-slate-800 !border-slate-900 !text-white transition-all active:scale-95" @click="openSubjectOrigin">
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
      class="!rounded-[32px] overflow-hidden !border-none"
    >
      <template #header>
        <div class="flex items-center justify-between">
          <div>
            <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">积分赠送</div>
            <div class="text-xl font-bold text-gray-900">赠送积分</div>
          </div>
          <button 
            class="w-8 h-8 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-all active:scale-95"
            @click="giftDialogVisible = false"
          >
            <span class="text-gray-500 text-sm">✕</span>
          </button>
        </div>
      </template>

      <div class="space-y-5">
        <!-- 接收人信息卡片 -->
        <div class="bg-gray-50 rounded-2xl p-4 border border-gray-100">
          <div class="flex items-center gap-3">
            <div class="w-12 h-12 rounded-xl bg-slate-900 flex items-center justify-center text-white font-bold text-lg shrink-0">
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
            class="!rounded-xl"
          />
        </div>
        
        <!-- 快捷选择 -->
        <div class="pt-4 border-t border-gray-100">
          <div class="text-xs text-gray-400 mb-3">快捷选择</div>
          <div class="flex flex-wrap gap-2">
            <button 
              v-for="amt in [10, 50, 100, 500]" 
              :key="amt"
              class="px-4 py-2 rounded-full text-sm font-medium transition-all active:scale-95"
              :class="giftForm.points === amt 
                ? 'bg-emerald-600 text-white' 
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
            class="flex-1 !rounded-xl !h-11 transition-all active:scale-95" 
            @click="giftDialogVisible = false"
          >
            取消
          </el-button>
          <el-button 
            type="primary" 
            class="flex-1 !rounded-xl !h-11 !bg-emerald-600 hover:!bg-emerald-700 !border-emerald-600 transition-all active:scale-95"
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
      :unit-price="draftQuoteData?.price || ''"
      :delivery-place="draftQuoteData?.deliveryPlace || ''"
      :arrival-date="draftQuoteData?.arrivalDate || ''"
      :payment-method="draftQuoteData?.paymentMethod || ''"
      @success="onContractCreated"
    />
    
    <!-- 签署合同弹窗 -->
    <ContractSignModal
      v-model="signModalVisible"
      :contract-id="signContractId"
      @signed="onContractSigned"
    />

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
  font-size: 13px;
  font-weight: 700;
}

:deep(.transaction-steps .el-step__description) {
  font-size: 11px;
  line-height: 1.4;
}

:deep(.transaction-steps .el-step__icon) {
  width: 20px;
  height: 20px;
  font-size: 10px;
}
</style>
