/**
 * useNegotiationWorkspace - 议价工作台核心状态管理
 * 整合会话管理、消息、报价、合同等功能
 */
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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
import type { RequirementData } from '../components/negotiation/ProductRequirementForm.vue'
import type { ContractData, ContractStatus } from '../components/negotiation/ContractPreview.vue'
import type { FileUploadResponse } from '../api/file'
import { giftPoints as giftPointsApi } from '../api/points'
import { createContractFromQuote, type ContractFromQuoteRequest } from '../api/contract'
import { getMyCompany, getCompanyByUserId, type CompanyResponse } from '../api/company'
import { getQualityStandards } from '../utils/qualityStandards'

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
  const sidebarState = ref<'expanded' | 'mini' | 'hidden'>('expanded')

  /** 发送中状态 */
  const sending = ref(false)

  /** 合同状态 */
  const contractStatus = ref<ContractStatus>('DRAFT')
  /** 买方是否已确认条款 */
  const buyerConfirmed = ref(false)
  /** 卖方是否已确认条款 */
  const sellerConfirmed = ref(false)

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
      onConnect: () => console.log('[NegotiationWorkspace] WebSocket connected'),
      onDisconnect: (reason) => {
        if (reason !== 'logged_out' && reason !== 'manual') {
          console.warn('[NegotiationWorkspace] WebSocket disconnected:', reason)
        }
      }
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

  /** 对方信息 */
  const peerInfo = computed(() => {
    if (!currentConversation.value) return { name: '加载中...', company: '' }
    return {
      name: currentConversation.value.peerNickName || currentConversation.value.peerUserName || '对方',
      company: currentConversation.value.peerCompanyName || ''
    }
  })

  /** 从标的快照解析需求数据 */
  const requirementData = computed<Partial<RequirementData>>(() => {
    if (!currentConversation.value?.subjectSnapshotJson) return {}
    try {
      const snapshot = JSON.parse(currentConversation.value.subjectSnapshotJson)
      return {
        productName: snapshot.productName || snapshot.title || '',
        categoryName: snapshot.categoryName || '',
        quantity: snapshot.quantity || snapshot.remainingQuantity || 0,
        unit: '吨',
        qualityGrade: snapshot.qualityGrade || '一级品',
        deliveryDate: snapshot.deliveryDate || snapshot.arrivalDate || '',
        deliveryPlace: snapshot.deliveryPlace || snapshot.shipAddress || snapshot.purchaseAddress || '',
        price: snapshot.price || snapshot.exFactoryPrice || snapshot.expectedPrice,
        paymentMethod: snapshot.paymentMethod || snapshot.payment_method || '货到付款'
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
    const convId = currentConversation.value?.id || 0
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
      totalAmount: totalAmount,
      remark: ''
    }
  })

  /** 当前用户是否为买方 */
  const currentIsBuyer = computed(() => {
    return currentConversation.value?.subjectType === 'SUPPLY'
  })

  // ==================== WebSocket Handler ====================

  function handleWsMessage(data: any) {
    const { type, conversationId: msgConvId, message } = data

    if (type === 'MESSAGE' && message && currentConversation.value && msgConvId === currentConversation.value.id) {
      chatMessages.handleIncomingMessage(message, currentConversation.value.id)

      // 检查是否是对方的确认消息
      if (message.msgType === 'SYSTEM' && message.payloadJson) {
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
              ElMessage.success('双方已确认条款，可以生成正式合同！')
            } else {
              contractStatus.value = 'PENDING_CONFIRM'
              ElMessage.info('对方已确认条款')
            }
          }
        } catch {
          // 忽略解析错误
        }
      }
    }

    if (type === 'SENT' && data.tempId && (data.id || data.messageId)) {
      chatMessages.confirmMessage(data.tempId, data.id || data.messageId)
    }

    if (type === 'ERROR' && data.tempId) {
      chatMessages.failMessage(data.tempId)
      ElMessage.error(data.message || '发送失败')
    }

    // 更新会话列表的最新消息
    if (type === 'MESSAGE' && message) {
      updateConversationPreview(msgConvId, message)
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
    } catch (e) {
      console.error('Load conversations failed:', e)
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
    loadingMessages.value = true

    try {
      // 并行加载消息和公司信息
      await Promise.all([
        chatMessages.loadMessages(conv.id),
        loadCompanyInfo(conv.peerUserId)
      ])
      await markConversationRead(conv.id)
      conv.unreadCount = 0

      // 从消息历史解析确认状态
      parseConfirmationsFromMessages()

      // 如果有已接受的报价，更新合同状态
      if (chatMessages.hasAcceptedQuote.value && contractStatus.value === 'DRAFT') {
        contractStatus.value = 'PENDING_CONFIRM'
      }
    } catch (e) {
      console.error('Load messages failed:', e)
      ElMessage.error('加载消息失败')
    } finally {
      loadingMessages.value = false
    }

    // 更新路由
    router.replace({
      path: '/chat',
      query: { conversationId: String(conv.id) }
    })
  }

  /** 选择商户（自动选择该商户的最新会话） */
  async function selectMerchant(merchant: MerchantGroup) {
    await selectConversation(merchant.latestConversation)
  }

  /** 切换到商户的某个历史会话 */
  async function switchToConversation(conv: ConversationItem) {
    await selectConversation(conv)
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
    } catch (e) {
      console.error('Open conversation failed:', e)
      ElMessage.error('打开会话失败')
    }
  }

  /** 发送文本消息 */
  function sendText(text: string) {
    if (!text.trim() || !currentConversation.value || !webSocket.ensureConnected()) return

    sending.value = true
    const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`

    chatMessages.addPendingMessage(currentConversation.value.id, 'TEXT', text, tempId)

    const sent = webSocket.sendText(currentConversation.value.id, text, tempId)
    if (!sent) {
      chatMessages.failMessage(tempId)
      ElMessage.error('发送失败')
    }

    sending.value = false
  }

  /** 发送报价 */
  function sendQuote(payloadJson: string, previewText: string) {
    if (!currentConversation.value || !webSocket.ensureConnected()) return

    sending.value = true
    const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`

    chatMessages.addPendingMessage(currentConversation.value.id, 'QUOTE', previewText, tempId)

    const sent = webSocket.sendQuote(currentConversation.value.id, payloadJson, previewText, tempId)
    if (!sent) {
      chatMessages.failMessage(tempId)
      ElMessage.error('发送失败')
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
    if (!currentConversation.value || !webSocket.ensureConnected()) return

    sending.value = true
    const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`

    const payloadJson = JSON.stringify({
      fileId: fileData.fileId,
      fileName: fileData.fileName,
      fileUrl: fileData.fileUrl,
      size: fileData.size,
      mimeType: fileData.mimeType
    })

    chatMessages.addPendingMessage(currentConversation.value.id, 'IMAGE', `[图片] ${fileData.fileName}`, tempId, payloadJson)

    const sent = webSocket.sendImage(currentConversation.value.id, payloadJson, tempId)
    if (!sent) {
      chatMessages.failMessage(tempId)
      ElMessage.error('发送失败')
    }

    sending.value = false
  }

  /** 发送附件消息 */
  function sendAttachment(fileData: FileUploadResponse) {
    if (!currentConversation.value || !webSocket.ensureConnected()) return

    sending.value = true
    const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`

    const payloadJson = JSON.stringify({
      fileId: fileData.fileId,
      fileName: fileData.fileName,
      fileUrl: fileData.fileUrl,
      size: fileData.size,
      mimeType: fileData.mimeType
    })

    chatMessages.addPendingMessage(currentConversation.value.id, 'ATTACHMENT', `[附件] ${fileData.fileName}`, tempId, payloadJson)

    const sent = webSocket.sendAttachment(currentConversation.value.id, payloadJson, fileData.fileName, tempId)
    if (!sent) {
      chatMessages.failMessage(tempId)
      ElMessage.error('发送失败')
    }

    sending.value = false
  }

  /** 赠送积分给对方 */
  async function giftPoints(toUserId: number, points: number, remark?: string) {
    if (!currentConversation.value) return

    try {
      const res = await giftPointsApi(toUserId, points, remark)
      if (res.code === 0) {
        ElMessage.success(`成功赠送 ${points} 积分`)
        // 发送一条系统消息通知对方
        sendText(`🎁 我向您赠送了 ${points} 积分${remark ? `，备注：${remark}` : ''}`)
      } else {
        ElMessage.error(res.message || '赠送失败')
      }
    } catch (e: any) {
      console.error('Gift points failed:', e)
      ElMessage.error(e.response?.data?.message || '赠送失败')
    }
  }

  /** 接受报价 */
  async function acceptQuote(messageId: number) {
    if (!currentConversation.value || !webSocket.ensureConnected()) return

    try {
      // 调用后端API确认报价
      const res = await confirmChatOffer(messageId)
      if (res.code !== 0) {
        ElMessage.error(res.message || '接受报价失败')
        return
      }

      // 更新本地消息状态
      chatMessages.updateQuoteStatus(messageId, 'ACCEPTED')

      // 发送接受报价的系统消息
      const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`
      chatMessages.addPendingMessage(currentConversation.value.id, 'TEXT', '已接受报价，可以生成合同', tempId)
      webSocket.sendText(currentConversation.value.id, '已接受报价，可以生成合同', tempId)

      // 更新合同状态
      contractStatus.value = 'PENDING_CONFIRM'
      ElMessage.success('已接受报价')
    } catch (e: any) {
      console.error('Accept quote failed:', e)
      ElMessage.error(e.response?.data?.message || '接受报价失败')
    }
  }

  /** 拒绝报价 */
  async function rejectQuote(messageId: number) {
    if (!currentConversation.value || !webSocket.ensureConnected()) return

    try {
      // 调用后端API拒绝报价
      const res = await rejectChatOffer(messageId)
      if (res.code !== 0) {
        ElMessage.error(res.message || '拒绝报价失败')
        return
      }

      // 更新本地消息状态
      chatMessages.updateQuoteStatus(messageId, 'REJECTED')

      ElMessage.info('已拒绝报价')
    } catch (e: any) {
      console.error('Reject quote failed:', e)
      ElMessage.error(e.response?.data?.message || '拒绝报价失败')
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
    if (!currentConversation.value || !webSocket.ensureConnected()) return

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

    chatMessages.addPendingMessage(currentConversation.value.id, 'SYSTEM', content, tempId, payloadJson)
    webSocket.sendSystem(currentConversation.value.id, content, payloadJson, tempId)

    // 检查是否双方都已确认
    if (buyerConfirmed.value && sellerConfirmed.value) {
      contractStatus.value = 'CONFIRMED'
      ElMessage.success('双方已确认条款，可以生成正式合同！')
    } else {
      contractStatus.value = 'PENDING_CONFIRM'
      ElMessage.success('条款确认成功，等待对方确认')
    }
  }

  /** 从消息中解析确认状态 */
  function parseConfirmationsFromMessages() {
    let buyer = false
    let seller = false

    // DEBUG: 打印所有 SYSTEM 消息
    const systemMsgs = chatMessages.messages.value.filter(m => (m.msgType || '').toUpperCase() === 'SYSTEM')
    console.log('[DEBUG] SYSTEM messages:', systemMsgs.map(m => ({
      id: m.id,
      msgType: m.msgType,
      content: m.content,
      payloadJson: m.payloadJson
    })))

    for (const msg of chatMessages.messages.value) {
      if ((msg.msgType || '').toUpperCase() !== 'SYSTEM') continue
      if (!msg.payloadJson) {
        console.log('[DEBUG] SYSTEM message without payloadJson:', msg.id, msg.content)
        continue
      }

      try {
        const payload = JSON.parse(msg.payloadJson)
        console.log('[DEBUG] Parsed payload:', payload)
        if (payload.action === 'CONFIRM_TERMS') {
          if (payload.role === 'buyer') buyer = true
          if (payload.role === 'seller') seller = true
        }
      } catch (e) {
        console.error('[DEBUG] Failed to parse payloadJson:', msg.payloadJson, e)
      }
    }

    console.log('[DEBUG] Confirmation result - buyer:', buyer, 'seller:', seller)
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
      ElMessage.warning('请等待双方都确认条款后再生成正式合同')
      return
    }

    // 获取已接受的报价消息
    const acceptedQuote = chatMessages.latestAcceptedQuoteMessage.value
    if (!acceptedQuote || typeof acceptedQuote.id !== 'number') {
      ElMessage.error('未找到已接受的报价，无法生成合同')
      return
    }

    sending.value = true

    try {
      // 构建请求数据
      const contract = contractData.value
      const req: ContractFromQuoteRequest = {
        quoteMessageId: acceptedQuote.id,
        deliveryDate: contract.deliveryDate,
        deliveryAddress: contract.deliveryPlace,
        paymentMethod: contract.paymentMethod
      }

      // 调用后端API创建合同
      const res = await createContractFromQuote(req)

      if (res.code === 0 && res.data) {
        const contractId = res.data

        // 更新状态为签署中
        contractStatus.value = 'SIGNING'

        // 发送合同消息到聊天
        if (currentConversation.value && webSocket.ensureConnected()) {
          const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`
          const contract = contractData.value
          const contractPayload = JSON.stringify({
            contractId: contractId,
            contractNo: contract.contractNo,
            productName: contract.products[0]?.name || '产品',
            totalAmount: contract.totalAmount,
            status: 1 // PENDING_CONFIRM
          })

          chatMessages.addPendingMessage(currentConversation.value.id, 'CONTRACT', '[合同]', tempId, contractPayload)
          webSocket.sendContract(currentConversation.value.id, contractPayload, tempId)
        }

        ElMessage.success('合同已生成，正在跳转到合同详情...')

        // 跳转到合同详情页
        router.push(`/contracts/${contractId}`)
      } else {
        ElMessage.error(res.message || '生成合同失败')
      }
    } catch (e: any) {
      console.error('Generate contract failed:', e)
      ElMessage.error(e.response?.data?.message || '生成合同失败')
    } finally {
      sending.value = false
    }
  }

  /** 切换侧边栏 */
  function toggleSidebar() {
    if (sidebarState.value === 'hidden') {
      sidebarState.value = 'expanded'
    } else if (sidebarState.value === 'expanded') {
      sidebarState.value = 'mini'
    } else {
      sidebarState.value = 'hidden'
    }
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
    } catch (e) {
      console.error('Initialize workspace failed:', e)
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

    // Computed
    merchantGroups,
    currentMerchantConversations,
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
