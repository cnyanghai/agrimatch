<script setup lang="ts">
/**
 * NegotiationContractView - 议价与合同视图
 * 基于 Stitch 设计的左右分屏布局：
 * - 左侧：产品需求表单 + 聊天面板
 * - 右侧：合同实时预览
 */
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Wifi, WifiOff } from 'lucide-vue-next'
import { useAuthStore } from '../store/auth'

// Composables
import {
  useChatWebSocket,
  useChatMessages
} from '../composables/chat'

// Components
import {
  ProductRequirementForm,
  ChatPanel,
  ContractPreview,
  type RequirementData,
  type ContractData,
  type ContractStatus
} from '../components/negotiation'

// API
import { getConversation, type ChatConversationResponse } from '../api/chat'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

// ==================== Route & State ====================

const conversationId = computed(() => Number(route.params.conversationId))
const loading = ref(true)
const conversation = ref<ChatConversationResponse | null>(null)
const sending = ref(false)

// ==================== Composables ====================

// WebSocket
const webSocket = useChatWebSocket(
  () => auth.token,
  () => !!auth.me || !!auth.token,
  {
    onMessage: handleWsMessage,
    onConnect: () => console.log('[Negotiation] WebSocket connected'),
    onDisconnect: (reason) => {
      if (reason !== 'logged_out' && reason !== 'manual') {
        ElMessage.warning('连接已断开，正在重连...')
      }
    }
  }
)

// Messages
const messages = useChatMessages({
  getCurrentUserId: () => auth.me?.userId
})

// ==================== Derived State ====================

// 对方信息
const peerInfo = computed(() => {
  if (!conversation.value) return { name: '加载中...', company: '' }
  return {
    name: conversation.value.peerNickName || conversation.value.peerUserName || '对方',
    company: conversation.value.peerCompanyName || ''
  }
})

// 从标的快照解析需求数据
const requirementData = computed<Partial<RequirementData>>(() => {
  if (!conversation.value?.subjectSnapshotJson) return {}
  try {
    const snapshot = JSON.parse(conversation.value.subjectSnapshotJson)
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

// 合同数据（基于需求和报价）
const contractData = computed<ContractData>(() => {
  const req = requirementData.value
  const latestQuote = messages.peerLatestQuote.value

  // 解析最新报价
  let price = req.price || 0
  let quantity = req.quantity || 0
  if (latestQuote?.payloadJson) {
    try {
      const payload = JSON.parse(latestQuote.payloadJson)
      if (payload.fields?.price) price = parseFloat(payload.fields.price)
      if (payload.fields?.quantity) {
        const qtyStr = payload.fields.quantity.replace(/[^\d.]/g, '')
        quantity = parseFloat(qtyStr) || quantity
      }
    } catch { /* ignore */ }
  }

  const totalAmount = price * quantity

  return {
    contractNo: `HT-${new Date().getFullYear()}-${String(conversationId.value).padStart(6, '0')}`,
    signDate: new Date().toISOString().split('T')[0],
    buyer: {
      companyName: auth.me?.companyName || '买方公司',
      contactName: auth.me?.nickName || auth.me?.userName,
      contactTitle: '采购部',
      address: '待填写'
    },
    seller: {
      companyName: peerInfo.value.company || '卖方公司',
      contactName: peerInfo.value.name,
      contactTitle: '销售部',
      address: '待填写'
    },
    products: [{
      name: req.productName || '产品名称',
      grade: req.qualityGrade || '一级品',
      quantity: quantity,
      unit: req.unit || '吨',
      unitPrice: price,
      totalPrice: totalAmount
    }],
    qualityStandards: [
      { label: '水分 (Moisture)', value: '≤ 14.0%' },
      { label: '杂质 (Impurity)', value: '≤ 1.0%' },
      { label: '霉变粒 (Moldy)', value: '≤ 2.0%' }
    ],
    paymentMethod: req.paymentMethod || '货到付款',
    deliveryPlace: req.deliveryPlace || '待定',
    deliveryDate: req.deliveryDate || '',
    totalAmount: totalAmount,
    remark: ''
  }
})

// 合同状态
const contractStatus = ref<ContractStatus>('DRAFT')
const buyerSigned = ref(false)
const sellerSigned = ref(false)

// 当前用户是否为买方
const currentIsBuyer = computed(() => {
  // 简化判断：如果标的类型是 SUPPLY，当前用户是买方
  return conversation.value?.subjectType === 'SUPPLY'
})

// AI 建议
const aiSuggestion = computed(() => {
  const quoteCount = messages.quoteMessages.value.length
  if (quoteCount === 0) return '发送询价开始协商'
  if (quoteCount === 1) return '查看报价并确认条款'
  if (quoteCount > 1) return '确认最终价格，准备签署合同'
  return undefined
})

// ==================== WebSocket Handler ====================

function handleWsMessage(data: any) {
  const { type, conversationId: msgConvId, message } = data

  if (type === 'MESSAGE' && message && msgConvId === conversationId.value) {
    messages.handleIncomingMessage(message, conversationId.value)
  }

  if (type === 'SENT' && data.tempId && (data.id || data.messageId)) {
    messages.confirmMessage(data.tempId, data.id || data.messageId)
  }

  if (type === 'ERROR' && data.tempId) {
    messages.failMessage(data.tempId)
    ElMessage.error(data.message || '发送失败')
  }
}

// ==================== Actions ====================

// 返回会话列表
function goBack() {
  router.push('/chat')
}

// 发送文本消息
function handleSendMessage(text: string) {
  if (!text.trim() || !webSocket.ensureConnected()) return

  sending.value = true
  const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`

  messages.addPendingMessage(conversationId.value, 'TEXT', text, tempId)

  const sent = webSocket.sendText(conversationId.value, text, tempId)
  if (!sent) {
    messages.failMessage(tempId)
    ElMessage.error('发送失败')
  }

  sending.value = false
}

// 需求表单更新
function handleRequirementUpdate(data: RequirementData) {
  // 这里可以添加逻辑：当表单更新时同步到合同预览
  console.log('Requirement updated:', data)
}

// 签署合同
function handleSign() {
  if (currentIsBuyer.value) {
    buyerSigned.value = true
  } else {
    sellerSigned.value = true
  }

  if (buyerSigned.value && sellerSigned.value) {
    contractStatus.value = 'SIGNED'
    ElMessage.success('合同已签署完成！')
  } else {
    contractStatus.value = 'PENDING_SIGN'
    ElMessage.success('签署成功，等待对方签署')
  }
}

// ==================== Lifecycle ====================

onMounted(async () => {
  loading.value = true

  try {
    // 加载会话信息
    const convRes = await getConversation(conversationId.value)
    if (convRes.code === 0 && convRes.data) {
      conversation.value = convRes.data
    } else {
      ElMessage.error('会话不存在')
      router.push('/chat')
      return
    }

    // 加载消息
    await messages.loadMessages(conversationId.value)

    // 连接 WebSocket
    webSocket.connect()
  } catch (e: any) {
    console.error('Load conversation failed:', e)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  webSocket.disconnect()
})
</script>

<template>
  <div class="h-screen flex flex-col bg-gray-100">
    <!-- 顶部导航 -->
    <header class="bg-white border-b border-gray-200 px-6 py-3 flex items-center justify-between shrink-0 shadow-sm">
      <div class="flex items-center gap-4">
        <button
          @click="goBack"
          class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
        >
          <ArrowLeft class="w-5 h-5 text-gray-600" />
        </button>
        <div>
          <h1 class="font-bold text-gray-900 flex items-center gap-2">
            {{ peerInfo.name }}
            <span v-if="peerInfo.company" class="text-gray-400 font-normal">·</span>
            <span v-if="peerInfo.company" class="text-gray-500 font-normal text-sm">
              {{ peerInfo.company }}
            </span>
          </h1>
          <div class="flex items-center gap-2 text-xs text-gray-500">
            <span>{{ requirementData.productName || '产品议价' }}</span>
            <span class="text-gray-300">|</span>
            <span class="flex items-center gap-1">
              <component
                :is="webSocket.isConnected.value ? Wifi : WifiOff"
                :class="['w-3 h-3', webSocket.isConnected.value ? 'text-green-500' : 'text-gray-400']"
              />
              {{ webSocket.isConnected.value ? '已连接' : '连接中' }}
            </span>
          </div>
        </div>
      </div>
      <div class="flex items-center gap-2 text-sm text-gray-500">
        <span class="flex items-center gap-1">
          <span class="w-2 h-2 rounded-full bg-green-500"></span>
          系统状态: 正常
        </span>
      </div>
    </header>

    <!-- 加载状态 -->
    <div v-if="loading" class="flex-1 flex items-center justify-center">
      <div class="w-8 h-8 border-2 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- 主内容区：左右分屏 -->
    <div v-else class="flex-1 flex overflow-hidden p-6 gap-6">
      <!-- 左侧：需求表单 + 聊天 (40%) -->
      <div class="w-[40%] min-w-[400px] flex flex-col gap-4 overflow-hidden">
        <!-- 产品需求表单 -->
        <ProductRequirementForm
          :initial-data="requirementData"
          @update="handleRequirementUpdate"
        />

        <!-- 聊天面板 -->
        <div class="flex-1 bg-white rounded-xl shadow-sm overflow-hidden flex flex-col min-h-[300px]">
          <ChatPanel
            :messages="messages.messages.value"
            :peer-name="peerInfo.name"
            :peer-company="peerInfo.company"
            :ws-connected="webSocket.isConnected.value"
            :sending="sending"
            :ai-suggestion="aiSuggestion"
            @send="handleSendMessage"
          />
        </div>
      </div>

      <!-- 右侧：合同实时预览 (60%) -->
      <div class="flex-1 flex flex-col overflow-hidden relative">
        <ContractPreview
          :contract-data="contractData"
          :status="contractStatus"
          :buyer-signed="buyerSigned"
          :seller-signed="sellerSigned"
          :current-is-buyer="currentIsBuyer"
          @sign="handleSign"
        />
      </div>
    </div>
  </div>
</template>
