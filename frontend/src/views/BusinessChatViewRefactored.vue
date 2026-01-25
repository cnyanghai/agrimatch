<script setup lang="ts">
/**
 * BusinessChatView - Refactored
 * Uses new composables and components for cleaner architecture
 */
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../store/auth'

// New composables
import {
  useChatWebSocket,
  useChatMessages,
  useChatConversations,
  useQuoteActions,
  useContractFlow
} from '../composables/chat'

// New layout components
import { ChatLayout } from '../components/chat/layout'

// Existing components
import NegotiationPanel from '../components/chat/NegotiationPanel.vue'
import BasisBargainPanel from '../components/chat/BasisBargainPanel.vue'
import ChatSubjectCard from '../components/chat/ChatSubjectCard.vue'
import ContractDraftModal from '../components/contract/ContractDraftModal.vue'
import ContractSignModal from '../components/contract/ContractSignModal.vue'
import QuoteTimeline from '../components/chat/quote/QuoteTimeline.vue'

// API imports
import { followUser, unfollowUser, checkFollowStatus } from '../api/follow'
import { giftPoints } from '../api/points'
import { uploadImage, uploadAttachment, type FileUploadResponse } from '../api/file'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

// ==================== Composables Setup ====================

// WebSocket connection
const webSocket = useChatWebSocket(
  () => auth.token,
  () => !!auth.me || !!auth.token,
  {
    onMessage: handleWsMessage,
    onConnect: () => {
      console.log('[Chat] WebSocket connected')
    },
    onDisconnect: (reason) => {
      if (reason !== 'logged_out' && reason !== 'manual') {
        ElMessage.warning('连接已断开，正在重连...')
      }
    }
  }
)

// Messages management
const messages = useChatMessages({
  getCurrentUserId: () => auth.me?.userId
})

// Conversations management
const conversations = useChatConversations()

// Quote actions
const quoteActions = useQuoteActions({
  webSocket,
  messages,
  getConversationId: () => conversations.activeConversationId.value
})

// Contract flow
const contractFlow = useContractFlow({
  messages,
  getCurrentCompanyId: () => auth.me?.companyId
})

// ==================== Local State ====================

const loading = ref(false)
const sending = ref(false)
const quotePopoverVisible = ref(false)
const negotiationDrawerOpen = ref(false)
const quoteTimelineDrawerOpen = ref(false)
const subjectDialogOpen = ref(false)
const isFollowing = ref(false)
const showArchivedModal = ref(false)

// File upload
const imageInputRef = ref<HTMLInputElement | null>(null)
const attachmentInputRef = ref<HTMLInputElement | null>(null)
const uploading = ref(false)

// Gift dialog
const giftDialogVisible = ref(false)
const giftForm = ref({ points: 10, remark: '' })
const giftLoading = ref(false)

// ==================== Computed ====================

const canNegotiate = computed(() => {
  return !!conversations.currentConversation.value
})

// 从标的快照中提取产品名称和位置
const subjectInfo = computed(() => {
  const conv = conversations.currentConversation.value
  if (!conv?.subjectSnapshotJson) return { name: '', location: '' }
  try {
    const snapshot = JSON.parse(conv.subjectSnapshotJson)
    return {
      name: snapshot.productName || snapshot.categoryName || snapshot.title || '',
      location: snapshot.deliveryPlace || snapshot.location || snapshot.region || ''
    }
  } catch {
    return { name: '', location: '' }
  }
})

// ==================== WebSocket Message Handler ====================

function handleWsMessage(data: any) {
  const { type, conversationId, message, messageId, tempId, id, payload } = data

  switch (type) {
    case 'MESSAGE':
      // New message received
      if (message && conversationId === conversations.activeConversationId.value) {
        messages.handleIncomingMessage(message, conversations.activeConversationId.value)
      }
      // Update conversation list
      if (message) {
        conversations.updateLastMessage(
          conversationId,
          message.content || '',
          message.createTime || new Date().toISOString()
        )
        // Increment unread if not current conversation
        if (conversationId !== conversations.activeConversationId.value) {
          conversations.incrementUnread(conversationId)
        }
      }
      break

    case 'SENT':
      // Message sent confirmation
      if (tempId && (id || messageId)) {
        messages.confirmMessage(tempId, id || messageId)
      }
      break

    case 'OFFER_UPDATED':
    case 'MESSAGE_UPDATE':
      // Quote status update
      if (messageId && payload) {
        messages.updateMessagePayload(messageId, payload)
      }
      break
  }
}

// ==================== Actions ====================

async function sendTextMessage(text: string) {
  const conversationId = conversations.activeConversationId.value
  if (!conversationId || !text.trim()) return

  if (!webSocket.ensureConnected()) {
    ElMessage.warning('实时连接未就绪，正在重连…')
    return
  }

  sending.value = true
  const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`

  // Optimistic update
  messages.addPendingMessage(conversationId, 'TEXT', text, tempId)

  // Send via WebSocket
  const sent = webSocket.sendText(conversationId, text, tempId)
  if (!sent) {
    messages.failMessage(tempId)
    ElMessage.error('发送失败')
  }

  sending.value = false
}

function handleSendQuote(payload: any) {
  const { msgType, payload: quotePayload, summary } = payload
  quoteActions.sendQuote(quotePayload.fields)
  quotePopoverVisible.value = false
}

async function handleConfirmQuote(messageId: number) {
  await quoteActions.confirmQuote(messageId)
}

async function handleRejectQuote(messageId: number) {
  await quoteActions.rejectQuote(messageId)
}

function handleCounterQuote(messageId: number) {
  // 打开议价面板进行还价
  negotiationDrawerOpen.value = true
}

function openQuoteTimeline() {
  quoteTimelineDrawerOpen.value = true
}

function handleQuoteSelect(message: any) {
  // 可选：滚动到该消息或其他操作
  quoteTimelineDrawerOpen.value = false
}

async function handleDraftContract(messageId: number) {
  contractFlow.openDraftModal(messageId)
}

function handleViewContract(contractId: number) {
  router.push({ name: 'ContractDetail', params: { id: contractId } })
}

function handleSignContract(contractId: number) {
  contractFlow.openSignModal(contractId)
}

async function toggleFollow() {
  const peerId = conversations.activePeerId.value
  if (!peerId) return

  try {
    if (isFollowing.value) {
      await unfollowUser(peerId)
      isFollowing.value = false
      ElMessage.success('已取消关注')
    } else {
      await followUser(peerId)
      isFollowing.value = true
      ElMessage.success('关注成功')
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

async function sendGift() {
  const peerId = conversations.activePeerId.value
  if (!peerId) return

  giftLoading.value = true
  try {
    await giftPoints({
      toUserId: peerId,
      points: giftForm.value.points,
      remark: giftForm.value.remark
    })
    ElMessage.success(`成功赠送 ${giftForm.value.points} 积分`)
    giftDialogVisible.value = false
    giftForm.value = { points: 10, remark: '' }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '赠送失败')
  } finally {
    giftLoading.value = false
  }
}

// ==================== File Upload ====================

function triggerImageUpload() {
  imageInputRef.value?.click()
}

function triggerAttachmentUpload() {
  attachmentInputRef.value?.click()
}

async function handleImageSelect(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }

  // 验证文件大小 (5MB)
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return
  }

  const conversationId = conversations.activeConversationId.value
  if (!conversationId) return

  if (!webSocket.ensureConnected()) {
    ElMessage.warning('连接未就绪，正在重连…')
    return
  }

  uploading.value = true
  const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`

  try {
    // 上传图片到服务器
    const res = await uploadImage(file)
    if (res.code !== 0 || !res.data) {
      throw new Error(res.message || '上传失败')
    }

    const payload = {
      fileId: res.data.fileId,
      fileName: res.data.fileName,
      fileUrl: res.data.fileUrl,
      size: res.data.size,
      mimeType: res.data.mimeType
    }

    // 乐观更新
    messages.addPendingMessage(conversationId, 'IMAGE', '[图片]', tempId, JSON.stringify(payload))

    // 通过 WebSocket 发送
    const sent = webSocket.sendImage(conversationId, JSON.stringify(payload), tempId)
    if (!sent) {
      messages.failMessage(tempId)
      ElMessage.error('发送失败')
    }
  } catch (e: any) {
    console.error('Image upload failed:', e)
    ElMessage.error(e.message || '图片上传失败')
  } finally {
    uploading.value = false
    input.value = '' // 清空 input 以便重复选择同一文件
  }
}

async function handleAttachmentSelect(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  // 验证文件大小 (20MB)
  if (file.size > 20 * 1024 * 1024) {
    ElMessage.warning('附件大小不能超过 20MB')
    return
  }

  const conversationId = conversations.activeConversationId.value
  if (!conversationId) return

  if (!webSocket.ensureConnected()) {
    ElMessage.warning('连接未就绪，正在重连…')
    return
  }

  uploading.value = true
  const tempId = `temp_${Date.now()}_${Math.random().toString(36).slice(2)}`

  try {
    // 上传附件到服务器
    const res = await uploadAttachment(file)
    if (res.code !== 0 || !res.data) {
      throw new Error(res.message || '上传失败')
    }

    const payload = {
      fileId: res.data.fileId,
      fileName: res.data.fileName,
      fileUrl: res.data.fileUrl,
      size: res.data.size,
      mimeType: res.data.mimeType
    }

    // 乐观更新
    messages.addPendingMessage(conversationId, 'ATTACHMENT', `[附件] ${file.name}`, tempId, JSON.stringify(payload))

    // 通过 WebSocket 发送
    const sent = webSocket.sendAttachment(conversationId, JSON.stringify(payload), file.name, tempId)
    if (!sent) {
      messages.failMessage(tempId)
      ElMessage.error('发送失败')
    }
  } catch (e: any) {
    console.error('Attachment upload failed:', e)
    ElMessage.error(e.message || '附件上传失败')
  } finally {
    uploading.value = false
    input.value = ''
  }
}

// ==================== Lifecycle ====================

onMounted(async () => {
  conversations.init()

  loading.value = true
  await conversations.loadConversations()

  // Initialize from route
  const routeConvId = route.query.conversationId
  if (routeConvId) {
    conversations.initFromRoute(routeConvId as string)
  }

  // Connect WebSocket
  webSocket.connect()

  loading.value = false
})

onBeforeUnmount(() => {
  webSocket.disconnect()
})

// Watch for conversation changes
watch(() => conversations.activeConversationId.value, async (newId) => {
  if (newId) {
    await messages.loadMessages(newId)
    await conversations.markRead(newId)

    // Check follow status
    const peerId = conversations.activePeerId.value
    if (peerId) {
      try {
        const res = await checkFollowStatus(peerId)
        isFollowing.value = res.data || false
      } catch {
        isFollowing.value = false
      }
    }
  } else {
    messages.clearMessages()
  }
})
</script>

<template>
  <div class="h-screen bg-gray-100 p-4 lg:p-6">
    <!-- 隐藏的文件输入框 -->
    <input
      ref="imageInputRef"
      type="file"
      accept="image/*"
      class="hidden"
      @change="handleImageSelect"
    />
    <input
      ref="attachmentInputRef"
      type="file"
      accept="*/*"
      class="hidden"
      @change="handleAttachmentSelect"
    />

    <ChatLayout
      :time-groups="conversations.timeGroupedPeers.value"
      :active-peer-id="conversations.activePeerId.value"
      :active-conversation-id="conversations.activeConversationId.value"
      :current-peer-conversations="conversations.currentPeerConversations.value"
      :search-keyword="conversations.searchKeyword.value"
      :archived-count="conversations.archivedConversations.value.length"
      :conversations-loading="loading"
      :messages="messages.messages.value"
      :current-conversation="conversations.currentConversation.value"
      :peer-display-name="conversations.currentPeerDisplayName.value"
      :ws-connected="webSocket.isConnected.value"
      :messages-loading="messages.loading.value"
      :sending="sending"
      :is-following="isFollowing"
      :can-negotiate="canNegotiate"
      :is-basis-trade="conversations.currentIsBasisTrade.value"
      :transaction-step="contractFlow.transactionStep.value"
      :current-company-id="auth.me?.companyId"
      :subject-name="subjectInfo.name"
      :subject-location="subjectInfo.location"
      :quote-count="messages.quoteMessages.value.length"
      @select-peer="conversations.selectPeer"
      @switch-conversation="conversations.switchConversation"
      @archive-conversation="conversations.archiveConversation"
      @update:search-keyword="conversations.setSearchKeyword"
      @show-archived="showArchivedModal = true"
      @send-text="sendTextMessage"
      @send-image="triggerImageUpload"
      @send-attachment="triggerAttachmentUpload"
      @send-gift="giftDialogVisible = true"
      @open-negotiation="negotiationDrawerOpen = true"
      @open-subject-detail="subjectDialogOpen = true"
      @open-quote-timeline="openQuoteTimeline"
      @toggle-follow="toggleFollow"
      @confirm-quote="handleConfirmQuote"
      @reject-quote="handleRejectQuote"
      @counter-quote="handleCounterQuote"
      @open-draft-contract="handleDraftContract"
      @view-contract="handleViewContract"
      @sign-contract="handleSignContract"
    />

    <!-- Negotiation Drawer -->
    <el-drawer
      v-model="negotiationDrawerOpen"
      direction="rtl"
      size="500px"
      :with-header="false"
    >
      <div class="p-4">
        <NegotiationPanel
          v-if="!conversations.currentIsBasisTrade.value"
          :peer-latest-quote="messages.peerLatestQuote.value"
          :subject-snapshot-json="conversations.currentConversation.value?.subjectSnapshotJson"
          @send="handleSendQuote"
        />
        <BasisBargainPanel
          v-else
          :subject-snapshot-json="conversations.currentConversation.value?.subjectSnapshotJson"
          @send="handleSendQuote"
        />
      </div>
    </el-drawer>

    <!-- Quote Timeline Drawer -->
    <el-drawer
      v-model="quoteTimelineDrawerOpen"
      title="报价历史"
      direction="rtl"
      size="400px"
    >
      <div class="p-4">
        <QuoteTimeline
          :quotes="messages.quoteMessages.value"
          :current-user-id="auth.me?.userId"
          @select="handleQuoteSelect"
        />
      </div>
    </el-drawer>

    <!-- Subject Detail Dialog -->
    <el-dialog v-model="subjectDialogOpen" title="标的详情" width="600px">
      <ChatSubjectCard
        v-if="conversations.currentConversation.value"
        :subject-snapshot-json="conversations.currentConversation.value.subjectSnapshotJson"
        :subject-type="conversations.currentConversation.value.subjectType"
      />
    </el-dialog>

    <!-- Contract Draft Modal -->
    <ContractDraftModal
      v-model="contractFlow.draftModalVisible.value"
      :quote-message-id="contractFlow.draftQuoteMessageId.value"
      :quote-data="contractFlow.draftQuoteData.value"
      :conversation="conversations.currentConversation.value"
      @created="contractFlow.onContractCreated"
    />

    <!-- Contract Sign Modal -->
    <ContractSignModal
      v-model="contractFlow.signModalVisible.value"
      :contract-id="contractFlow.signContractId.value"
      @signed="contractFlow.onContractSigned"
    />

    <!-- Gift Dialog -->
    <el-dialog v-model="giftDialogVisible" title="赠送积分" width="400px">
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">积分数量</label>
          <el-input-number v-model="giftForm.points" :min="1" :max="10000" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">留言（可选）</label>
          <el-input v-model="giftForm.remark" placeholder="感谢您的合作" />
        </div>
      </div>
      <template #footer>
        <el-button @click="giftDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="giftLoading" @click="sendGift">
          赠送
        </el-button>
      </template>
    </el-dialog>

    <!-- Archived Conversations Modal -->
    <el-dialog v-model="showArchivedModal" title="已归档会话" width="500px">
      <div v-if="conversations.archivedConversations.value.length === 0" class="text-center py-8 text-gray-500">
        暂无已归档会话
      </div>
      <div v-else class="space-y-2">
        <div
          v-for="conv in conversations.archivedConversations.value"
          :key="conv.id"
          class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
        >
          <div>
            <div class="font-medium">{{ conv.peerNickName || conv.peerUserName }}</div>
            <div class="text-sm text-gray-500">{{ conv.lastContent }}</div>
          </div>
          <el-button size="small" @click="conversations.restoreConversation(conv.id)">
            恢复
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
