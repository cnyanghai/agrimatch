<script setup lang="ts">
/**
 * ChatView - 聊天视图
 * 与某用户的聊天界面，支持多个标的切换
 * - 左侧：该用户的所有标的列表（可切换）
 * - 中间：聊天消息区（普通聊天为主）
 * - 右侧：可选的合同预览面板（点击"生成合同"时展开）
 */
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Wifi, WifiOff, Send, Paperclip,
  FileText, Package, ShoppingCart, ChevronRight, X
} from 'lucide-vue-next'
import { useAuthStore } from '../store/auth'

// Composables
import { useChatWebSocket, useChatMessages } from '../composables/chat'

// API
import { getConversations, getConversationMessages, type ChatConversationResponse } from '../api/chat'

// Components
import { ContractPreview, type ContractData, type ContractStatus } from '../components/negotiation'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

// ==================== Route & State ====================

const peerId = computed(() => Number(route.params.peerId))
const loading = ref(true)
const sending = ref(false)

// 该用户的所有会话
const peerConversations = ref<ChatConversationResponse[]>([])
// 当前选中的会话
const activeConversationId = ref<number | null>(null)
// 消息输入
const messageInput = ref('')
// 是否显示合同面板
const showContractPanel = ref(false)
// 聊天容器引用
const chatContainerRef = ref<HTMLElement | null>(null)

// ==================== Composables ====================

// WebSocket
const webSocket = useChatWebSocket(
  () => auth.token,
  () => !!auth.me || !!auth.token,
  {
    onMessage: handleWsMessage,
    onConnect: () => console.log('[Chat] WebSocket connected'),
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

// ==================== Computed ====================

// 当前会话
const activeConversation = computed(() => {
  if (!activeConversationId.value) return null
  return peerConversations.value.find(c => c.id === activeConversationId.value) || null
})

// 对方信息
const peerInfo = computed(() => {
  const conv = peerConversations.value[0]
  if (!conv) return { name: '加载中...', company: '' }
  return {
    name: conv.peerNickName || conv.peerUserName || '对方',
    company: conv.peerCompanyName || ''
  }
})

// 是否有多个标的
const hasMultipleSubjects = computed(() => peerConversations.value.length > 1)

// 合同数据
const contractData = computed<ContractData>(() => {
  const conv = activeConversation.value
  if (!conv?.subjectSnapshotJson) {
    return getEmptyContractData()
  }

  try {
    const snapshot = JSON.parse(conv.subjectSnapshotJson)
    const price = snapshot.price || snapshot.exFactoryPrice || snapshot.expectedPrice || 0
    const quantity = snapshot.quantity || snapshot.remainingQuantity || 0
    const totalAmount = price * quantity

    return {
      contractNo: `HT-${new Date().getFullYear()}-${String(conv.id).padStart(6, '0')}`,
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
        name: snapshot.productName || snapshot.title || '产品名称',
        grade: snapshot.qualityGrade || '一级品',
        quantity: quantity,
        unit: '吨',
        unitPrice: price,
        totalPrice: totalAmount
      }],
      qualityStandards: [
        { label: '水分 (Moisture)', value: '≤ 14.0%' },
        { label: '杂质 (Impurity)', value: '≤ 1.0%' },
        { label: '霉变粒 (Moldy)', value: '≤ 2.0%' }
      ],
      paymentMethod: snapshot.paymentMethod || '货到付款',
      deliveryPlace: snapshot.deliveryPlace || snapshot.shipAddress || '待定',
      deliveryDate: snapshot.deliveryDate || snapshot.arrivalDate || '',
      totalAmount: totalAmount,
      remark: ''
    }
  } catch {
    return getEmptyContractData()
  }
})

const contractStatus = ref<ContractStatus>('DRAFT')
const buyerSigned = ref(false)
const sellerSigned = ref(false)

const currentIsBuyer = computed(() => {
  return activeConversation.value?.subjectType === 'SUPPLY'
})

// ==================== Methods ====================

function getEmptyContractData(): ContractData {
  return {
    contractNo: '',
    signDate: '',
    buyer: { companyName: '', contactName: '', contactTitle: '', address: '' },
    seller: { companyName: '', contactName: '', contactTitle: '', address: '' },
    products: [],
    qualityStandards: [],
    paymentMethod: '',
    deliveryPlace: '',
    deliveryDate: '',
    totalAmount: 0,
    remark: ''
  }
}

// 获取标的标题
function getSubjectTitle(conv: ChatConversationResponse): string {
  if (!conv.subjectSnapshotJson) return '新会话'
  try {
    const snapshot = JSON.parse(conv.subjectSnapshotJson)
    return snapshot.productName || snapshot.title || '产品议价'
  } catch {
    return '产品议价'
  }
}

// 获取标的价格
function getSubjectPrice(conv: ChatConversationResponse): string {
  if (!conv.subjectSnapshotJson) return ''
  try {
    const snapshot = JSON.parse(conv.subjectSnapshotJson)
    const price = snapshot.price || snapshot.exFactoryPrice || snapshot.expectedPrice
    if (price) return `¥${price}/吨`
    return ''
  } catch {
    return ''
  }
}

// 格式化时间
function formatTime(timeStr?: string): string {
  if (!timeStr) return ''
  try {
    const d = new Date(timeStr)
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } catch {
    return ''
  }
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}

// 切换会话
async function switchConversation(convId: number) {
  if (convId === activeConversationId.value) return

  activeConversationId.value = convId
  messages.clearMessages()

  try {
    await messages.loadMessages(convId)
    scrollToBottom()
  } catch (e) {
    console.error('Load messages failed:', e)
  }
}

// 返回
function goBack() {
  router.push('/chat')
}

// 发送消息
function handleSend() {
  const text = messageInput.value.trim()
  if (!text || !activeConversationId.value) return
  if (!webSocket.ensureConnected()) {
    ElMessage.warning('正在连接，请稍后重试')
    return
  }

  sending.value = true
  const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`

  messages.addPendingMessage(activeConversationId.value, 'TEXT', text, tempId)
  scrollToBottom()

  const sent = webSocket.sendText(activeConversationId.value, text, tempId)
  if (!sent) {
    messages.failMessage(tempId)
    ElMessage.error('发送失败')
  }

  messageInput.value = ''
  sending.value = false
}

// 键盘事件
function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

// 打开合同面板
function openContractPanel() {
  if (!activeConversation.value) {
    ElMessage.warning('请先选择一个标的')
    return
  }
  showContractPanel.value = true
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

// WebSocket 消息处理
function handleWsMessage(data: any) {
  const { type, conversationId: msgConvId, message } = data

  if (type === 'MESSAGE' && message && msgConvId === activeConversationId.value) {
    messages.handleIncomingMessage(message, activeConversationId.value)
    scrollToBottom()
  }

  if (type === 'SENT' && data.tempId && (data.id || data.messageId)) {
    messages.confirmMessage(data.tempId, data.id || data.messageId)
  }

  if (type === 'ERROR' && data.tempId) {
    messages.failMessage(data.tempId)
    ElMessage.error(data.message || '发送失败')
  }
}

// 获取头像首字母
function getAvatarChar(name?: string): string {
  if (!name) return '?'
  return name[0].toUpperCase()
}

// ==================== Lifecycle ====================

onMounted(async () => {
  loading.value = true

  try {
    // 加载该用户的所有会话
    const res = await getConversations()
    if (res.code === 0 && res.data) {
      peerConversations.value = res.data.filter(c => c.peerUserId === peerId.value)

      if (peerConversations.value.length === 0) {
        ElMessage.error('未找到与该用户的会话')
        router.push('/chat')
        return
      }

      // 默认选中第一个会话
      activeConversationId.value = peerConversations.value[0]!.id
      await messages.loadMessages(activeConversationId.value)
      scrollToBottom()
    }

    // 连接 WebSocket
    webSocket.connect()
  } catch (e) {
    console.error('Load conversations failed:', e)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  webSocket.disconnect()
})

// 监听消息变化，滚动到底部
watch(() => messages.messages.value.length, scrollToBottom)
</script>

<template>
  <div class="h-screen flex flex-col bg-gray-100">
    <!-- 顶部导航 -->
    <header class="bg-white border-b border-gray-200 px-4 py-3 flex items-center justify-between shrink-0 shadow-sm">
      <div class="flex items-center gap-3">
        <button @click="goBack" class="p-2 hover:bg-gray-100 rounded-lg transition-colors">
          <ArrowLeft class="w-5 h-5 text-gray-600" />
        </button>
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-full bg-brand-500 text-white flex items-center justify-center font-bold">
            {{ getAvatarChar(peerInfo.name) }}
          </div>
          <div>
            <h1 class="font-bold text-gray-900">{{ peerInfo.name }}</h1>
            <div class="flex items-center gap-2 text-xs text-gray-500">
              <span>{{ peerInfo.company || '暂无公司' }}</span>
              <span class="text-gray-300">|</span>
              <span class="flex items-center gap-1">
                <component
                  :is="webSocket.isConnected.value ? Wifi : WifiOff"
                  :class="['w-3 h-3', webSocket.isConnected.value ? 'text-green-500' : 'text-gray-400']"
                />
                {{ webSocket.isConnected.value ? '在线' : '离线' }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 生成合同按钮 -->
      <button
        v-if="activeConversation"
        @click="openContractPanel"
        class="flex items-center gap-2 px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white rounded-lg text-sm font-medium transition-colors"
      >
        <FileText class="w-4 h-4" />
        <span>生成合同</span>
      </button>
    </header>

    <!-- 加载状态 -->
    <div v-if="loading" class="flex-1 flex items-center justify-center">
      <div class="w-8 h-8 border-2 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- 主内容区 -->
    <div v-else class="flex-1 flex overflow-hidden">
      <!-- 左侧：标的列表（仅当有多个标的时显示） -->
      <div
        v-if="hasMultipleSubjects"
        class="w-64 bg-white border-r border-gray-200 flex flex-col shrink-0"
      >
        <div class="px-4 py-3 border-b border-gray-100">
          <h3 class="text-sm font-bold text-gray-700">相关标的</h3>
        </div>
        <div class="flex-1 overflow-y-auto">
          <div
            v-for="conv in peerConversations"
            :key="conv.id"
            @click="switchConversation(conv.id)"
            :class="[
              'px-4 py-3 cursor-pointer border-b border-gray-50 transition-colors',
              activeConversationId === conv.id
                ? 'bg-brand-50 border-l-2 border-l-brand-500'
                : 'hover:bg-gray-50'
            ]"
          >
            <div class="flex items-center gap-2">
              <component
                :is="conv.subjectType === 'SUPPLY' ? Package : ShoppingCart"
                :class="[
                  'w-4 h-4',
                  conv.subjectType === 'SUPPLY' ? 'text-brand-600' : 'text-autumn-600'
                ]"
              />
              <span class="text-sm font-medium text-gray-800 truncate">
                {{ getSubjectTitle(conv) }}
              </span>
            </div>
            <div class="flex items-center justify-between mt-1">
              <span class="text-xs text-gray-400">
                {{ conv.subjectType === 'SUPPLY' ? '供应' : '采购' }}
              </span>
              <span class="text-xs text-brand-600 font-medium">
                {{ getSubjectPrice(conv) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间：聊天区 -->
      <div class="flex-1 flex flex-col bg-gray-50 min-w-0">
        <!-- 当前标的提示（单个标的时） -->
        <div
          v-if="!hasMultipleSubjects && activeConversation"
          class="px-4 py-2 bg-white border-b border-gray-100 flex items-center gap-2"
        >
          <component
            :is="activeConversation.subjectType === 'SUPPLY' ? Package : ShoppingCart"
            :class="[
              'w-4 h-4',
              activeConversation.subjectType === 'SUPPLY' ? 'text-brand-600' : 'text-autumn-600'
            ]"
          />
          <span class="text-sm text-gray-700">{{ getSubjectTitle(activeConversation) }}</span>
          <span class="text-xs text-brand-600 font-medium">{{ getSubjectPrice(activeConversation) }}</span>
        </div>

        <!-- 消息列表 -->
        <div
          ref="chatContainerRef"
          class="flex-1 overflow-y-auto p-4 space-y-4"
        >
          <!-- 空状态 -->
          <div
            v-if="messages.messages.value.length === 0"
            class="flex flex-col items-center justify-center h-full text-center text-gray-400"
          >
            <p class="text-sm">开始与 {{ peerInfo.name }} 沟通</p>
            <p class="text-xs mt-1">发送消息开始交流</p>
          </div>

          <!-- 消息列表 -->
          <template v-for="msg in messages.messages.value" :key="msg.id">
            <!-- 系统消息 -->
            <div v-if="msg.type === 'system'" class="flex justify-center">
              <span class="text-[10px] font-medium text-gray-400 bg-gray-100 px-2 py-0.5 rounded-full">
                {{ msg.content }}
              </span>
            </div>

            <!-- 对方消息 -->
            <div v-else-if="msg.type === 'received'" class="flex gap-2 max-w-[75%]">
              <div class="w-8 h-8 rounded-full bg-brand-500 text-white flex items-center justify-center font-bold text-xs shrink-0">
                {{ getAvatarChar(peerInfo.name) }}
              </div>
              <div class="flex flex-col gap-1">
                <div class="bg-white border border-gray-100 p-3 rounded-2xl rounded-tl-none shadow-sm text-sm text-gray-800">
                  {{ msg.content }}
                </div>
                <span class="text-[10px] text-gray-400 ml-1">{{ formatTime(msg.time) }}</span>
              </div>
            </div>

            <!-- 我的消息 -->
            <div v-else class="flex flex-row-reverse gap-2 max-w-[75%] ml-auto">
              <div class="flex flex-col gap-1 items-end">
                <div
                  :class="[
                    'p-3 rounded-2xl rounded-tr-none shadow-sm text-sm',
                    msg.status === 'failed'
                      ? 'bg-red-100 text-red-800'
                      : 'bg-brand-600 text-white'
                  ]"
                >
                  {{ msg.content }}
                </div>
                <span class="text-[10px] text-gray-400 mr-1">
                  {{ msg.status === 'pending' ? '发送中...' : msg.status === 'failed' ? '发送失败' : formatTime(msg.time) }}
                </span>
              </div>
            </div>
          </template>
        </div>

        <!-- 输入区域 -->
        <div class="p-4 bg-white border-t border-gray-100 shrink-0">
          <div class="flex items-center gap-3">
            <button class="p-2 text-gray-400 hover:text-brand-600 hover:bg-gray-50 rounded-lg transition-colors">
              <Paperclip class="w-5 h-5" />
            </button>
            <input
              v-model="messageInput"
              @keydown="handleKeydown"
              type="text"
              class="flex-1 h-11 px-4 rounded-xl bg-gray-50 border border-gray-200
                     focus:bg-white focus:border-brand-500 focus:ring-1 focus:ring-brand-500/20
                     text-sm transition-all"
              placeholder="输入消息..."
              :disabled="sending || !activeConversationId"
            />
            <button
              @click="handleSend"
              :disabled="!messageInput.trim() || sending || !activeConversationId"
              class="h-11 px-5 bg-brand-600 hover:bg-brand-700 disabled:bg-gray-300 disabled:cursor-not-allowed
                     text-white rounded-xl font-medium text-sm flex items-center gap-2 transition-colors"
            >
              <span>发送</span>
              <Send class="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      <!-- 右侧：合同预览面板（可选） -->
      <div
        v-if="showContractPanel"
        class="w-[500px] bg-white border-l border-gray-200 flex flex-col shrink-0 shadow-lg"
      >
        <div class="px-4 py-3 border-b border-gray-100 flex items-center justify-between">
          <h3 class="font-bold text-gray-900">合同预览</h3>
          <button
            @click="showContractPanel = false"
            class="p-1.5 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <X class="w-5 h-5 text-gray-500" />
          </button>
        </div>
        <div class="flex-1 overflow-hidden">
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
  </div>
</template>
