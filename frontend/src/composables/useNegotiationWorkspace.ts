/**
 * useNegotiationWorkspace - 议价工作台核心状态管理
 * 整合会话管理、消息、报价、合同等功能
 */
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from '@/composables/useToast'
import { useAuthStore } from '../store/auth'
import {
  listChatConversations,
  markConversationRead,
  openChatConversation,
  confirmChatOffer,
  rejectChatOffer,
  type ChatConversationResponse
} from '../api/chat'
import {
  useChatWebSocket,
  useChatMessages
} from './chat'
import { getConversationMessages, type ChatMessageResponse } from '../api/chat'
import { toUiMessage, type UiMessage } from '../types/chat/message'
import type { RequirementData } from '../components/negotiation/ProductRequirementForm.vue'
import type { ContractData, ContractStatus } from '../components/negotiation/ContractPreview.vue'
import type { FileUploadResponse } from '../api/file'
import { giftPoints as giftPointsApi } from '../api/points'
import { createContractFromNegotiation, type ContractFromNegotiationRequest } from '../api/contract'
import { getMyCompany, getCompanyByUserId, type CompanyResponse } from '../api/company'
import { getQualityStandards } from '../utils/qualityStandards'
import { parseProductParams } from '../utils/chat/paramsParser'

/** 议价状态 */
export type NegotiationStatus = 'INQUIRING' | 'NEGOTIATING' | 'PENDING_CONFIRM' | 'CONFIRMED' | 'SIGNING' | 'COMPLETED'

/** 会话项（扩展） */
export interface ConversationItem extends ChatConversationResponse {
  negotiationStatus?: NegotiationStatus
  hasAcceptedQuote?: boolean
  contractId?: number
}

/** 商户分组（按商户聚合所有会话） */
export interface MerchantGroup {
  peerId: number
  peerName: string
  peerCompany?: string
  peerAvatar?: string
  /** 该商户的所有会话（按时间倒序） */
  conversations: ConversationItem[]
  /** 最新会话（当前正在议价的） */
  latestConversation: ConversationItem
  /** 总未读数 */
  totalUnread: number
  /** 最后消息时间 */
  lastTime: string
  /** 最后消息内容 */
  lastContent: string
  /** 当前标的名称 */
  currentSubjectName: string
}

export function useNegotiationWorkspace() {
  const route = useRoute()
  const router = useRouter()
  const auth = useAuthStore()

  // ==================== State ====================

  /** 所有会话列表 */
  const conversations = ref<ConversationItem[]>([])

  /** 当前选中的会话 */
  const currentConversation = ref<ConversationItem | null>(null)

  /** 加载状态 */
  const loading = ref(false)
  const loadingMessages = ref(false)

  /** 侧边栏状态 */
  const sidebarState = ref<'expanded' | 'mini'>('expanded')

  /** 发送中状态 */
  const sending = ref(false)

  /** 合同状态 */
  const contractStatus = ref<ContractStatus>('DRAFT')
  /** 买方是否已确认条款 */
  const buyerConfirmed = ref(false)
  /** 卖方是否已确认条款 */
  const sellerConfirmed = ref(false)

  /** 当前商户所有会话的消息 Map: conversationId -> UiMessage[] */
  const merchantMessagesMap = ref<Map<number, UiMessage[]>>(new Map())

  /** 当前激活的产品会话 ID */
  const activeConversationId = ref<number | null>(null)

  /** 本地编辑的需求数据（用于实时更新合同预览） */
  const localEditedRequirement = ref<Partial<RequirementData>>({})

  /** 我方公司信息 */
  const myCompany = ref<CompanyResponse | null>(null)
  /** 对方公司信息 */
  const peerCompany = ref<CompanyResponse | null>(null)

  /** 更新本地编辑数据 */
  function updateLocalRequirement(data: Partial<RequirementData>) {
    localEditedRequirement.value = { ...data }
  }

  // ==================== WebSocket ====================

  const webSocket = useChatWebSocket(
    () => auth.token,
    () => !!auth.me || !!auth.token,
    {
      onMessage: handleWsMessage,
      onConnect: () => {},
      onDisconnect: () => {}
    }
  )

  // ==================== Messages ====================

  const chatMessages = useChatMessages({
    getCurrentUserId: () => auth.me?.userId
  })

  // ==================== Computed ====================

  /** 按商户聚合的会话列表 */
  const merchantGroups = computed<MerchantGroup[]>(() => {
    const map = new Map<number, MerchantGroup>()

    // 按商户聚合
    conversations.value.forEach(conv => {
      const peerId = conv.peerUserId
      const existing = map.get(peerId)

      // 解析标的名称
      let subjectName = '通用会话'
      if (conv.subjectSnapshotJson) {
        try {
          const snapshot = JSON.parse(conv.subjectSnapshotJson)
          subjectName = snapshot.productName || snapshot.title || '产品'
        } catch { /* ignore */ }
      }

      if (existing) {
        existing.conversations.push(conv)
        existing.totalUnread += conv.unreadCount || 0
        // 更新最新会话
        if (conv.lastTime && conv.lastTime > existing.lastTime) {
          existing.lastTime = conv.lastTime
          existing.lastContent = conv.lastContent || ''
          existing.latestConversation = conv
          existing.currentSubjectName = subjectName
        }
      } else {
        map.set(peerId, {
          peerId,
          peerName: conv.peerNickName || conv.peerUserName || '对方',
          peerCompany: conv.peerCompanyName,
          conversations: [conv],
          latestConversation: conv,
          totalUnread: conv.unreadCount || 0,
          lastTime: conv.lastTime || '',
          lastContent: conv.lastContent || '',
          currentSubjectName: subjectName
        })
      }
    })

    // 按最后消息时间排序
    return Array.from(map.values())
      .sort((a, b) => (b.lastTime || '').localeCompare(a.lastTime || ''))
  })

  /** 当前选中商户的所有会话（历史标的） */
  const currentMerchantConversations = computed(() => {
    if (!currentConversation.value) return []
    const peerId = currentConversation.value.peerUserId
    return conversations.value
      .filter(c => c.peerUserId === peerId)
      .sort((a, b) => (b.lastTime || '').localeCompare(a.lastTime || ''))
  })

  /** 当前激活的会话对象 */
  const activeConversation = computed<ConversationItem | null>(() => {
    if (!activeConversationId.value) return currentConversation.value
    return conversations.value.find(c => c.id === activeConversationId.value) || currentConversation.value
  })

  /** 当前激活产品名称 */
  const activeProductName = computed<string>(() => {
    const conv = activeConversation.value
    if (!conv?.subjectSnapshotJson) return ''
    try {
      const snapshot = JSON.parse(conv.subjectSnapshotJson)
      return snapshot.productName || snapshot.title || '产品'
    } catch {
      return ''
    }
  })

  /** 合并所有会话消息为单一时间线（按时间升序） */
  const mergedMessages = computed<UiMessage[]>(() => {
    const all: UiMessage[] = []
    for (const msgs of merchantMessagesMap.value.values()) {
      all.push(...msgs)
    }
    return all.sort((a, b) => (a.timestamp || 0) - (b.timestamp || 0))
  })

  /** 对方信息 */
  const peerInfo = computed(() => {
    if (!currentConversation.value) return { name: '加载中...', company: '' }
    return {
      name: currentConversation.value.peerNickName || currentConversation.value.peerUserName || '对方',
      company: currentConversation.value.peerCompanyName || ''
    }
  })

  /** 从标的快照解析需求数据（基于激活的会话） */
  const requirementData = computed<Partial<RequirementData>>(() => {
    const conv = activeConversation.value
    if (!conv?.subjectSnapshotJson) return {}
    try {
      const snapshot = JSON.parse(conv.subjectSnapshotJson)
      const isSupply = conv.subjectType === 'SUPPLY'

      // 基差合约：解析全部 basisQuotes
      let basisQuotes: Array<{ contractCode: string; basisPrice: number; availableQty?: number }> = []
      let basisPrice: number | undefined
      let contractCode: string | undefined
      if (isSupply && snapshot.priceType === 1 && Array.isArray(snapshot.basisQuotes) && snapshot.basisQuotes.length > 0) {
        basisQuotes = snapshot.basisQuotes.map((q: any) => ({
          contractCode: q.contractCode || '',
          basisPrice: q.basisPrice ?? 0,
          availableQty: q.availableQty ?? q.remainingQty ?? undefined,
          referencePrice: q.referencePrice ?? null
        }))
        basisPrice = basisQuotes[0]?.basisPrice
        contractCode = basisQuotes[0]?.contractCode
      }

      return {
        productName: snapshot.productName || snapshot.title || '',
        categoryName: snapshot.categoryName || '',
        quantity: snapshot.quantity || snapshot.remainingQuantity || 0,
        unit: snapshot.unit || '吨',
        price: snapshot.price || snapshot.exFactoryPrice || snapshot.expectedPrice,
        priceType: isSupply ? (snapshot.priceType === 1 ? 'BASIS' : 'SPOT') : undefined,
        basisPrice,
        contractCode,
        basisQuotes,
        packaging: snapshot.packaging || '',
        deliveryDate: snapshot.deliveryDate || snapshot.arrivalDate || '',
        deliveryPlace: snapshot.deliveryPlace || snapshot.shipAddress || snapshot.purchaseAddress || '',
        deliveryMethod: snapshot.deliveryMethod || snapshot.deliveryMode || '',
        paymentMethod: snapshot.paymentMethod || '',
        invoiceType: snapshot.invoiceType || '',
        dynamicParams: parseProductParams(snapshot.paramsJson),
        remark: snapshot.remark || ''
      }
    } catch {
      return {}
    }
  })

  /** 格式化公司地址 */
  function formatCompanyAddress(company: CompanyResponse | null): string {
    if (!company) return '待填写'
    const parts = [company.province, company.city, company.district, company.address].filter(Boolean)
    return parts.length > 0 ? parts.join('') : '待填写'
  }

  /** 合同数据（基于需求和本地编辑，实时更新） */
  const contractData = computed<ContractData>(() => {
    // 优先使用本地编辑的数据，其次使用原始需求数据
    const baseReq = requirementData.value
    const edited = localEditedRequirement.value
    const req = {
      ...baseReq,
      ...edited,
      // 确保有值时才覆盖
      price: edited.price !== undefined ? edited.price : baseReq.price,
      quantity: edited.quantity !== undefined ? edited.quantity : baseReq.quantity
    }
    const latestQuote = chatMessages.peerLatestQuote.value

    let price = req.price || 0
    let quantity = req.quantity || 0

    // 尝试从报价中解析价格和数量
    if (latestQuote) {
      // peerLatestQuote 是 QuoteFieldsV1 类型，直接访问字段
      if (latestQuote.price) {
        const parsedPrice = parseFloat(String(latestQuote.price))
        if (!isNaN(parsedPrice)) price = parsedPrice
      }
      if (latestQuote.quantity) {
        const qtyStr = String(latestQuote.quantity).replace(/[^\d.]/g, '')
        const parsedQty = parseFloat(qtyStr)
        if (!isNaN(parsedQty)) quantity = parsedQty
      }
    }

    const totalAmount = price * quantity
    const convId = activeConversation.value?.id || currentConversation.value?.id || 0
    const isBuyer = currentIsBuyer.value

    // 根据当前用户角色确定买方和卖方信息
    const buyerCompany = isBuyer ? myCompany.value : peerCompany.value
    const sellerCompany = isBuyer ? peerCompany.value : myCompany.value
    const buyerName = isBuyer
      ? (auth.me?.nickName || auth.me?.userName || '')
      : peerInfo.value.name
    const sellerName = isBuyer
      ? peerInfo.value.name
      : (auth.me?.nickName || auth.me?.userName || '')

    return {
      contractNo: `HT-${new Date().getFullYear()}-${String(convId).padStart(6, '0')}`,
      signDate: new Date().toISOString().split('T')[0] || '',
      buyer: {
        companyName: buyerCompany?.companyName || (isBuyer ? '我方公司' : '对方公司'),
        contactName: buyerName,
        contactTitle: buyerCompany?.legalPerson ? `法人: ${buyerCompany.legalPerson}` : '采购部',
        address: formatCompanyAddress(buyerCompany),
        phone: buyerCompany?.wechat || undefined
      },
      seller: {
        companyName: sellerCompany?.companyName || (isBuyer ? '对方公司' : '我方公司'),
        contactName: sellerName,
        contactTitle: sellerCompany?.legalPerson ? `法人: ${sellerCompany.legalPerson}` : '销售部',
        address: formatCompanyAddress(sellerCompany),
        phone: sellerCompany?.wechat || undefined
      },
      products: [{
        name: req.productName || '产品名称',
        grade: req.qualityGrade || '一级品',
        quantity: quantity,
        unit: req.unit || '吨',
        unitPrice: price,
        totalPrice: totalAmount
      }],
      qualityStandards: getQualityStandards(req.productName, req.categoryName),
      paymentMethod: req.paymentMethod || '货到付款',
      deliveryPlace: req.deliveryPlace || formatCompanyAddress(isBuyer ? buyerCompany : sellerCompany),
      deliveryDate: req.deliveryDate || '',
      deliveryMode: req.deliveryMethod || '',
      totalAmount: totalAmount,
      invoiceType: req.invoiceType || '',
      packaging: req.packaging || '',
      remark: req.remark || '',
      priceType: req.priceType,
      basisQuotes: req.basisQuotes?.map(q => ({
        contractCode: q.contractCode,
        basisPrice: q.basisPrice,
        availableQty: q.availableQty,
        referencePrice: q.referencePrice
      }))
    }
  })

  /** 当前用户是否为买方（基于激活的会话） */
  const currentIsBuyer = computed(() => {
    const conv = activeConversation.value
    if (!conv) return true

    const myUserId = auth.me?.userId
    if (myUserId && conv.initiatorUserId) {
      const isInitiator = myUserId === conv.initiatorUserId
      // SUPPLY: 发起人是买方（买家联系卖家）
      // NEED: 发起人是卖方（卖家联系买家）
      if (conv.subjectType === 'SUPPLY') return isInitiator
      if (conv.subjectType === 'NEED') return !isInitiator
    }

    // 旧会话无 initiatorUserId 时的兜底：subjectType === 'SUPPLY' 视为买方
    return conv.subjectType === 'SUPPLY'
  })

  // ==================== WebSocket Handler ====================

  function handleWsMessage(data: any) {
    const { type, conversationId: msgConvId, message } = data

    // 检查消息是否属于当前商户的某个会话
    const isCurrentMerchantConv = currentConversation.value &&
      merchantMessagesMap.value.has(msgConvId)

    if (type === 'MESSAGE' && message && isCurrentMerchantConv) {
      // 路由消息到 Map 中对应的 conversation
      handleIncomingMessageToMap(msgConvId, message)

      // 对方就新产品发起咨询时，自动切换到该会话
      if (activeConversationId.value !== msgConvId) {
        activateConversation(msgConvId)
      }

      // 同时更新 chatMessages（兼容旧接口，仅当是激活会话时）
      if (activeConversationId.value === msgConvId) {
        chatMessages.handleIncomingMessage(message, msgConvId)
      }

      // 检查是否是对方的确认消息（仅激活会话）
      if (activeConversationId.value === msgConvId && message.msgType === 'SYSTEM' && message.payloadJson) {
        try {
          const payload = JSON.parse(message.payloadJson)
          if (payload.action === 'CONFIRM_TERMS') {
            if (payload.role === 'buyer') {
              buyerConfirmed.value = true
            } else if (payload.role === 'seller') {
              sellerConfirmed.value = true
            }

            // 检查是否双方都已确认
            if (buyerConfirmed.value && sellerConfirmed.value) {
              contractStatus.value = 'CONFIRMED'
              showToast.success('双方已确认条款，可以生成正式合同！')
            } else {
              contractStatus.value = 'PENDING_CONFIRM'
              showToast.info('对方已确认条款')
            }
          }
        } catch {
          // 忽略解析错误
        }
      }

      // 收到 CONTRACT 消息时，更新状态为 SIGNING（对方已生成合同）
      if (activeConversationId.value === msgConvId && message.msgType === 'CONTRACT' && message.payloadJson) {
        try {
          const payload = JSON.parse(message.payloadJson)
          contractStatus.value = 'SIGNING'
          if (payload.contractId) {
            showToast.success('合同已生成，正在跳转到合同详情...')
            router.push(`/contracts/${payload.contractId}`)
          }
        } catch {
          // 忽略解析错误
        }
      }
    }

    if (type === 'SENT' && data.tempId && (data.id || data.messageId)) {
      confirmMessageInMap(data.tempId, data.id || data.messageId)
      chatMessages.confirmMessage(data.tempId, data.id || data.messageId)
    }

    if (type === 'ERROR' && data.tempId) {
      failMessageInMap(data.tempId)
      chatMessages.failMessage(data.tempId)
      showToast.error(data.message || '发送失败')
    }

    // 更新会话列表的最新消息
    if (type === 'MESSAGE' && message) {
      updateConversationPreview(msgConvId, message)
    }

    // 处理消息已读通知
    if (type === 'MESSAGES_READ' && data.messageIds && data.readAt) {
      chatMessages.markMessagesAsRead(data.messageIds, data.readAt)
      // 同时更新 Map 中的消息
      markMessagesAsReadInMap(data.messageIds, data.readAt)
    }
  }

  /** 更新会话预览 */
  function updateConversationPreview(convId: number, message: any) {
    const conv = conversations.value.find(c => c.id === convId)
    if (conv) {
      conv.lastContent = message.content
      conv.lastTime = message.createTime || new Date().toISOString()
      if (currentConversation.value?.id !== convId) {
        conv.unreadCount = (conv.unreadCount || 0) + 1
      }
    }
  }

  // ==================== Map Helper Functions ====================

  /** 向 Map 中指定会话添加待发送消息 */
  function addPendingMessageToConv(
    convId: number,
    msgType: string,
    content: string,
    tempId: string,
    payloadJson?: string
  ): UiMessage {
    const now = Date.now()
    const msg: UiMessage = {
      id: tempId,
      conversationId: convId,
      type: 'sent',
      msgType,
      content,
      payloadJson,
      status: 'pending',
      time: new Date(now).toISOString(),
      timestamp: now
    }
    const msgs = merchantMessagesMap.value.get(convId)
    if (msgs) {
      msgs.push(msg)
    } else {
      merchantMessagesMap.value.set(convId, [msg])
    }
    return msg
  }

  /** 确认 Map 中的消息发送成功 */
  function confirmMessageInMap(tempId: string, realId: number) {
    for (const msgs of merchantMessagesMap.value.values()) {
      const idx = msgs.findIndex(m => m.id === tempId)
      if (idx >= 0 && msgs[idx]) {
        msgs[idx] = { ...msgs[idx], id: realId, status: 'sent' }
        return
      }
    }
  }

  /** 标记 Map 中的消息发送失败 */
  function failMessageInMap(tempId: string) {
    for (const msgs of merchantMessagesMap.value.values()) {
      const idx = msgs.findIndex(m => m.id === tempId)
      if (idx >= 0 && msgs[idx]) {
        msgs[idx] = { ...msgs[idx], status: 'failed' }
        return
      }
    }
  }

  /** 处理收到的 WebSocket 消息，路由到 Map */
  function handleIncomingMessageToMap(convId: number, message: ChatMessageResponse) {
    const userId = auth.me?.userId
    if (!userId) return
    const uiMsg = toUiMessage(message, userId)
    const msgs = merchantMessagesMap.value.get(convId)
    if (msgs) {
      msgs.push(uiMsg)
    } else {
      merchantMessagesMap.value.set(convId, [uiMsg])
    }
  }

  /** 标记 Map 中的消息为已读 */
  function markMessagesAsReadInMap(messageIds: number[], readAt: string) {
    for (const msgs of merchantMessagesMap.value.values()) {
      for (const id of messageIds) {
        const idx = msgs.findIndex(m => m.id === id)
        if (idx >= 0 && msgs[idx]) {
          msgs[idx] = { ...msgs[idx], read: true, readAt }
        }
      }
    }
  }

  // ==================== Actions ====================

  /** 加载公司信息 */
  async function loadCompanyInfo(peerUserId: number) {
    // 并行加载我方和对方公司信息
    const [myRes, peerRes] = await Promise.all([
      getMyCompany().catch(() => ({ code: -1, data: null })),
      getCompanyByUserId(peerUserId).catch(() => ({ code: -1, data: null }))
    ])

    if (myRes.code === 0 && myRes.data) {
      myCompany.value = myRes.data
    }
    if (peerRes.code === 0 && peerRes.data) {
      peerCompany.value = peerRes.data
    }
  }

  /** 加载所有会话 */
  async function loadConversations() {
    loading.value = true
    try {
      const res = await listChatConversations()
      if (res.code === 0 && res.data) {
        conversations.value = res.data.map(c => ({
          ...c,
          negotiationStatus: inferNegotiationStatus(c)
        }))
      }
    } catch {
      // silently ignore
    } finally {
      loading.value = false
    }
  }

  /** 推断议价状态 */
  function inferNegotiationStatus(_conv: ChatConversationResponse): NegotiationStatus {
    // 简单推断，实际应该从后端获取
    // TODO: 根据 _conv 的报价和合同状态推断
    return 'NEGOTIATING'
  }

  /** 选择会话 */
  async function selectConversation(conv: ConversationItem) {
    if (currentConversation.value?.id === conv.id) return

    // 重置确认状态
    buyerConfirmed.value = false
    sellerConfirmed.value = false
    contractStatus.value = 'DRAFT'
    peerCompany.value = null

    currentConversation.value = conv
    activeConversationId.value = conv.id
    loadingMessages.value = true

    try {
      // 并行加载消息和公司信息
      await Promise.all([
        chatMessages.loadMessages(conv.id),
        loadCompanyInfo(conv.peerUserId)
      ])
      await markConversationRead(conv.id)
      conv.unreadCount = 0

      // 同步到 merchantMessagesMap
      merchantMessagesMap.value.set(conv.id, [...chatMessages.messages.value])

      // 从消息历史解析确认状态
      parseConfirmationsFromMessages()

      // 如果有已接受的报价，更新合同状态
      if (chatMessages.hasAcceptedQuote.value && contractStatus.value === 'DRAFT') {
        contractStatus.value = 'PENDING_CONFIRM'
      }
    } catch {
      showToast.error('加载消息失败')
    } finally {
      loadingMessages.value = false
    }

    // 更新路由
    router.replace({
      path: '/chat',
      query: { conversationId: String(conv.id) }
    })
  }

  /** 选择商户：并行加载该商户所有会话的消息 */
  async function selectMerchant(merchant: MerchantGroup) {
    const peerId = merchant.peerId
    // 如果已经选中同一商户，不重复加载
    if (currentConversation.value?.peerUserId === peerId) return

    // 重置状态
    buyerConfirmed.value = false
    sellerConfirmed.value = false
    contractStatus.value = 'DRAFT'
    peerCompany.value = null
    localEditedRequirement.value = {}

    currentConversation.value = merchant.latestConversation
    activeConversationId.value = merchant.latestConversation.id
    loadingMessages.value = true

    try {
      const userId = auth.me?.userId
      if (!userId) return

      // 并行加载该商户所有会话的消息 + 公司信息
      const allConvs = merchant.conversations
      const [, ...msgResults] = await Promise.all([
        loadCompanyInfo(peerId),
        ...allConvs.map(async (conv) => {
          try {
            const res = await getConversationMessages(conv.id, 50)
            if (res.code === 0 && res.data) {
              return {
                convId: conv.id,
                messages: res.data.map((m: ChatMessageResponse) => toUiMessage(m, userId)).reverse()
              }
            }
          } catch {
            // silently ignore
          }
          return { convId: conv.id, messages: [] as UiMessage[] }
        })
      ])

      // 存入 Map
      const newMap = new Map<number, UiMessage[]>()
      for (const result of msgResults) {
        if (result) {
          newMap.set(result.convId, result.messages)
        }
      }
      merchantMessagesMap.value = newMap

      // 同步 chatMessages（保持兼容）
      const activeMessages = newMap.get(activeConversationId.value!) || []
      chatMessages.messages.value = [...activeMessages]

      // 标记所有会话已读
      await Promise.all(
        allConvs.map(async (conv) => {
          try {
            await markConversationRead(conv.id)
            conv.unreadCount = 0
          } catch { /* ignore */ }
        })
      )

      // 从激活会话的消息解析确认状态
      parseConfirmationsFromMessages()

      if (chatMessages.hasAcceptedQuote.value && contractStatus.value === 'DRAFT') {
        contractStatus.value = 'PENDING_CONFIRM'
      }
    } catch {
      showToast.error('加载消息失败')
    } finally {
      loadingMessages.value = false
    }

    // 更新路由
    router.replace({
      path: '/chat',
      query: { conversationId: String(activeConversationId.value) }
    })
  }

  /** 切换到商户的某个历史会话 */
  async function switchToConversation(conv: ConversationItem) {
    await selectConversation(conv)
  }

  /** 激活商户内的某个产品会话（不重新加载消息，仅切换上下文） */
  function activateConversation(convId: number) {
    if (activeConversationId.value === convId) return
    activeConversationId.value = convId

    // 重置合同确认状态
    buyerConfirmed.value = false
    sellerConfirmed.value = false
    contractStatus.value = 'DRAFT'
    localEditedRequirement.value = {}

    // 同步 chatMessages 到激活的会话消息
    const msgs = merchantMessagesMap.value.get(convId) || []
    chatMessages.messages.value = [...msgs]

    // 重新解析确认状态
    parseConfirmationsFromMessages()

    if (chatMessages.hasAcceptedQuote.value && contractStatus.value === 'DRAFT') {
      contractStatus.value = 'PENDING_CONFIRM'
    }

    // 更新路由
    router.replace({
      path: '/chat',
      query: { conversationId: String(convId) }
    })
  }

  /** 打开或创建会话 */
  async function openOrCreateConversation(
    peerId: number,
    subjectType: 'SUPPLY' | 'NEED',
    subjectId: number,
    snapshotJson?: string
  ) {
    try {
      const res = await openChatConversation({
        peerUserId: peerId,
        subjectType,
        subjectId,
        subjectSnapshotJson: snapshotJson
      })
      if (res.code === 0 && res.data) {
        await loadConversations()
        const conv = conversations.value.find(c => c.id === res.data)
        if (conv) {
          await selectConversation(conv)
        }
      }
    } catch {
      showToast.error('打开会话失败')
    }
  }

  /** 发送文本消息 */
  function sendText(text: string) {
    const convId = activeConversationId.value
    if (!text.trim() || !convId) return

    sending.value = true
    const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`

    // 先做乐观更新，确保消息立即可见
    addPendingMessageToConv(convId, 'TEXT', text, tempId)
    chatMessages.addPendingMessage(convId, 'TEXT', text, tempId)

    // 再检查连接并发送
    if (!webSocket.ensureConnected()) {
      failMessageInMap(tempId)
      chatMessages.failMessage(tempId)
      showToast.error('连接未就绪，请稍后重试')
    } else {
      const sent = webSocket.sendText(convId, text, tempId)
      if (!sent) {
        failMessageInMap(tempId)
        chatMessages.failMessage(tempId)
        showToast.error('发送失败')
      }
    }

    sending.value = false
  }

  /** 发送报价 */
  function sendQuote(payloadJson: string, previewText: string) {
    const convId = activeConversationId.value
    if (!convId) return

    sending.value = true
    const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`

    // 先做乐观更新，确保消息立即可见
    addPendingMessageToConv(convId, 'QUOTE', previewText, tempId)
    chatMessages.addPendingMessage(convId, 'QUOTE', previewText, tempId)

    // 再检查连接并发送
    if (!webSocket.ensureConnected()) {
      failMessageInMap(tempId)
      chatMessages.failMessage(tempId)
      showToast.error('连接未就绪，请稍后重试')
    } else {
      const sent = webSocket.sendQuote(convId, payloadJson, previewText, tempId)
      if (!sent) {
        failMessageInMap(tempId)
        chatMessages.failMessage(tempId)
        showToast.error('发送失败')
      }
    }

    sending.value = false
  }

  /** 从表单数据构建报价并发送 */
  function sendQuoteFromForm(formData: {
    price: number
    quantity: string
    unit: string
    deliveryPlace: string
    arrivalDate: string
    paymentMethod: string
    remark?: string
  }) {
    const payload = {
      version: 1 as const,
      kind: 'QUOTE_V1' as const,
      createdAt: new Date().toISOString(),
      fields: {
        price: String(formData.price),
        quantity: `${formData.quantity}${formData.unit}`,
        deliveryPlace: formData.deliveryPlace,
        arrivalDate: formData.arrivalDate,
        paymentMethod: formData.paymentMethod,
        remark: formData.remark || ''
      }
    }

    const payloadJson = JSON.stringify(payload)
    const previewText = `报价: ¥${formData.price}/${formData.unit}, ${formData.quantity}${formData.unit}`

    sendQuote(payloadJson, previewText)
  }

  /** 发送图片消息 */
  function sendImage(fileData: FileUploadResponse) {
    const convId = activeConversationId.value
    if (!convId) return

    sending.value = true
    const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`

    const payloadJson = JSON.stringify({
      fileId: fileData.fileId,
      fileName: fileData.fileName,
      fileUrl: fileData.fileUrl,
      size: fileData.size,
      mimeType: fileData.mimeType
    })

    // 先做乐观更新，确保消息立即可见
    addPendingMessageToConv(convId, 'IMAGE', `[图片] ${fileData.fileName}`, tempId, payloadJson)
    chatMessages.addPendingMessage(convId, 'IMAGE', `[图片] ${fileData.fileName}`, tempId, payloadJson)

    // 再检查连接并发送
    if (!webSocket.ensureConnected()) {
      failMessageInMap(tempId)
      chatMessages.failMessage(tempId)
      showToast.error('连接未就绪，请稍后重试')
    } else {
      const sent = webSocket.sendImage(convId, payloadJson, tempId)
      if (!sent) {
        failMessageInMap(tempId)
        chatMessages.failMessage(tempId)
        showToast.error('发送失败')
      }
    }

    sending.value = false
  }

  /** 发送附件消息 */
  function sendAttachment(fileData: FileUploadResponse) {
    const convId = activeConversationId.value
    if (!convId) return

    sending.value = true
    const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`

    const payloadJson = JSON.stringify({
      fileId: fileData.fileId,
      fileName: fileData.fileName,
      fileUrl: fileData.fileUrl,
      size: fileData.size,
      mimeType: fileData.mimeType
    })

    // 先做乐观更新，确保消息立即可见
    addPendingMessageToConv(convId, 'ATTACHMENT', `[附件] ${fileData.fileName}`, tempId, payloadJson)
    chatMessages.addPendingMessage(convId, 'ATTACHMENT', `[附件] ${fileData.fileName}`, tempId, payloadJson)

    // 再检查连接并发送
    if (!webSocket.ensureConnected()) {
      failMessageInMap(tempId)
      chatMessages.failMessage(tempId)
      showToast.error('连接未就绪，请稍后重试')
    } else {
      const sent = webSocket.sendAttachment(convId, payloadJson, fileData.fileName, tempId)
      if (!sent) {
        failMessageInMap(tempId)
        chatMessages.failMessage(tempId)
        showToast.error('发送失败')
      }
    }

    sending.value = false
  }

  /** 赠送积分给对方 */
  async function giftPoints(toUserId: number, points: number, remark?: string) {
    if (!activeConversationId.value) return

    try {
      const res = await giftPointsApi(toUserId, points, remark)
      if (res.code === 0) {
        showToast.success(`成功赠送 ${points} 积分`)
        // 发送一条系统消息通知对方
        sendText(`🎁 我向您赠送了 ${points} 积分${remark ? `，备注：${remark}` : ''}`)
      } else {
        showToast.error(res.message || '赠送失败')
      }
    } catch (e: any) {
      showToast.error(e.response?.data?.message || '赠送失败')
    }
  }

  /** 接受报价 */
  async function acceptQuote(messageId: number) {
    const convId = activeConversationId.value
    if (!convId || !webSocket.ensureConnected()) return

    try {
      // 调用后端API确认报价
      const res = await confirmChatOffer(messageId)
      if (res.code !== 0) {
        showToast.error(res.message || '接受报价失败')
        return
      }

      // 更新本地消息状态
      chatMessages.updateQuoteStatus(messageId, 'ACCEPTED')

      // 发送接受报价的系统消息
      const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`
      addPendingMessageToConv(convId, 'TEXT', '已接受报价，可以生成合同', tempId)
      chatMessages.addPendingMessage(convId, 'TEXT', '已接受报价，可以生成合同', tempId)
      webSocket.sendText(convId, '已接受报价，可以生成合同', tempId)

      // 更新合同状态
      contractStatus.value = 'PENDING_CONFIRM'
      showToast.success('已接受报价')
    } catch (e: any) {
      showToast.error(e.response?.data?.message || '接受报价失败')
    }
  }

  /** 拒绝报价 */
  async function rejectQuote(messageId: number) {
    if (!activeConversationId.value || !webSocket.ensureConnected()) return

    try {
      // 调用后端API拒绝报价
      const res = await rejectChatOffer(messageId)
      if (res.code !== 0) {
        showToast.error(res.message || '拒绝报价失败')
        return
      }

      // 更新本地消息状态
      chatMessages.updateQuoteStatus(messageId, 'REJECTED')

      showToast.info('已拒绝报价')
    } catch (e: any) {
      showToast.error(e.response?.data?.message || '拒绝报价失败')
    }
  }

  /** 发送还价 */
  function sendCounterQuote(counterData: {
    price?: number
    basisPrice?: number
    quantity?: string
    remark?: string
  }) {
    const req = requirementData.value
    const payload = {
      version: 1 as const,
      kind: 'QUOTE_V1' as const,
      createdAt: new Date().toISOString(),
      fields: {
        price: counterData.price ? String(counterData.price) : undefined,
        quantity: counterData.quantity || undefined,
        deliveryPlace: req.deliveryPlace,
        paymentMethod: req.paymentMethod,
        remark: counterData.remark || '还价'
      }
    }

    const payloadJson = JSON.stringify(payload)
    const previewText = counterData.price
      ? `还价: ¥${counterData.price}`
      : `还价: ${counterData.quantity || ''}`

    sendQuote(payloadJson, previewText)
  }

  /** 确认合同条款 */
  function confirmContract() {
    const convId = activeConversationId.value
    if (!convId) return

    const isBuyer = currentIsBuyer.value
    const role = isBuyer ? 'buyer' : 'seller'
    const roleLabel = isBuyer ? '买方' : '卖方'

    // 更新本地状态
    if (isBuyer) {
      buyerConfirmed.value = true
    } else {
      sellerConfirmed.value = true
    }

    // 发送确认消息给对方
    const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`
    const payloadJson = JSON.stringify({
      action: 'CONFIRM_TERMS',
      role: role,
      confirmedAt: new Date().toISOString()
    })
    const content = `${roleLabel}已确认合同条款`

    // 先做乐观更新，确保消息立即可见
    addPendingMessageToConv(convId, 'SYSTEM', content, tempId, payloadJson)
    chatMessages.addPendingMessage(convId, 'SYSTEM', content, tempId, payloadJson)

    // 再检查连接并发送
    if (!webSocket.ensureConnected()) {
      failMessageInMap(tempId)
      chatMessages.failMessage(tempId)
      showToast.error('连接未就绪，请稍后重试')
    } else {
      webSocket.sendSystem(convId, content, payloadJson, tempId)
    }

    // 检查是否双方都已确认
    if (buyerConfirmed.value && sellerConfirmed.value) {
      contractStatus.value = 'CONFIRMED'
      showToast.success('双方已确认条款，可以生成正式合同！')
    } else {
      contractStatus.value = 'PENDING_CONFIRM'
      showToast.success('条款确认成功，等待对方确认')
    }
  }

  /** 从消息中解析确认状态 */
  function parseConfirmationsFromMessages() {
    let buyer = false
    let seller = false

    for (const msg of chatMessages.messages.value) {
      if ((msg.msgType || '').toUpperCase() !== 'SYSTEM') continue
      if (!msg.payloadJson) {
        continue
      }

      try {
        const payload = JSON.parse(msg.payloadJson)
        if (payload.action === 'CONFIRM_TERMS') {
          if (payload.role === 'buyer') buyer = true
          if (payload.role === 'seller') seller = true
        }
      } catch {
        // silently ignore
      }
    }

    buyerConfirmed.value = buyer
    sellerConfirmed.value = seller

    // 更新合同状态
    if (buyer && seller) {
      contractStatus.value = 'CONFIRMED'
    } else if (buyer || seller) {
      contractStatus.value = 'PENDING_CONFIRM'
    }
  }

  /** 生成正式合同并跳转到合同详情页 */
  async function generateFormalContract() {
    if (!buyerConfirmed.value || !sellerConfirmed.value) {
      showToast.warning('请等待双方都确认条款后再生成正式合同')
      return
    }

    const convId = activeConversationId.value
    if (!convId) {
      showToast.error('未选中会话，无法生成合同')
      return
    }

    sending.value = true

    try {
      // 从合同预览数据构建请求
      const cd = contractData.value
      const product = cd.products[0]
      const baseReq = requirementData.value
      const req: ContractFromNegotiationRequest = {
        conversationId: convId,
        productName: product?.name,
        categoryName: baseReq.categoryName,
        quantity: product?.quantity,
        unit: product?.unit,
        unitPrice: product?.unitPrice,
        basisPrice: cd.basisQuotes?.[0]?.basisPrice,
        contractCode: cd.basisQuotes?.[0]?.contractCode,
        priceType: cd.priceType,
        deliveryDate: cd.deliveryDate,
        deliveryAddress: cd.deliveryPlace,
        deliveryMode: cd.deliveryMode || baseReq.deliveryMethod,
        paymentMethod: cd.paymentMethod,
        invoiceType: cd.invoiceType || baseReq.invoiceType,
        packaging: cd.packaging || baseReq.packaging,
        remark: cd.remark || baseReq.remark
      }

      // 调用后端API创建合同（后端会同时发送CONTRACT聊天消息）
      const res = await createContractFromNegotiation(req)

      if (res.code === 0 && res.data) {
        const contractId = res.data

        // 更新状态为签署中
        contractStatus.value = 'SIGNING'

        showToast.success('合同已生成，正在跳转到合同详情...')

        // 跳转到合同详情页
        router.push(`/contracts/${contractId}`)
      } else {
        showToast.error(res.message || '生成合同失败')
      }
    } catch (e: any) {
      showToast.error(e.response?.data?.message || '生成合同失败')
    } finally {
      sending.value = false
    }
  }

  /** 切换侧边栏 */
  function toggleSidebar() {
    sidebarState.value = sidebarState.value === 'expanded' ? 'mini' : 'expanded'
  }

  // ==================== Lifecycle ====================

  async function initialize() {
    loading.value = true

    try {
      await loadConversations()

      // 从路由参数恢复会话
      const queryConvId = route.query.conversationId
      const queryPeerId = route.query.peerId
      const querySubjectType = route.query.subjectType as 'SUPPLY' | 'NEED' | undefined
      const querySubjectId = route.query.subjectId

      if (queryConvId) {
        const conv = conversations.value.find(c => c.id === Number(queryConvId))
        if (conv) {
          await selectConversation(conv)
        }
      } else if (queryPeerId && querySubjectType && querySubjectId) {
        await openOrCreateConversation(
          Number(queryPeerId),
          querySubjectType,
          Number(querySubjectId)
        )
      } else if (conversations.value.length > 0) {
        // 默认选择第一个会话
        await selectConversation(conversations.value[0]!)
      }

      // 连接 WebSocket
      webSocket.connect()
    } catch {
      // silently ignore
    } finally {
      loading.value = false
    }
  }

  function cleanup() {
    webSocket.disconnect()
  }

  return {
    // State
    conversations,
    currentConversation,
    loading,
    loadingMessages,
    sidebarState,
    sending,
    contractStatus,
    buyerConfirmed,
    sellerConfirmed,
    merchantMessagesMap,
    activeConversationId,

    // Computed
    merchantGroups,
    currentMerchantConversations,
    activeConversation,
    activeProductName,
    mergedMessages,
    peerInfo,
    requirementData,
    contractData,
    currentIsBuyer,

    // Messages
    messages: chatMessages.messages,
    quoteMessages: chatMessages.quoteMessages,
    hasAcceptedQuote: chatMessages.hasAcceptedQuote,

    // WebSocket
    isConnected: webSocket.isConnected,

    // Actions
    loadConversations,
    selectConversation,
    selectMerchant,
    switchToConversation,
    activateConversation,
    openOrCreateConversation,
    sendText,
    sendQuote,
    sendQuoteFromForm,
    sendImage,
    sendAttachment,
    giftPoints,
    acceptQuote,
    rejectQuote,
    sendCounterQuote,
    confirmContract,
    generateFormalContract,
    toggleSidebar,
    updateLocalRequirement,
    initialize,
    cleanup
  }
}
