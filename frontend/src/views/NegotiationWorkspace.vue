<script setup lang="ts">
/**
 * NegotiationWorkspace - 议价工作台
 * 整合的聊天+议价+合同确认工作空间
 *
 * 流程说明：
 * 1. 编辑产品需求表单 = 调整报价条款
 * 2. 点击"发送报价"将当前表单数据发送给对方
 * 3. 右侧合同实时根据表单数据更新
 * 4. 双方在合同预览区域确认条款
 * 5. 双方都确认后，生成正式签署合同
 * 6. 跳转第三方电子签章平台完成签署
 */
import { onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { MessageCircle } from 'lucide-vue-next'

// Composables
import { useNegotiationWorkspace, type MerchantGroup } from '../composables/useNegotiationWorkspace'
import { setActiveConversation } from '../composables/useGlobalWebSocket'
import { useNotificationStore } from '../stores/notification'

// Components
import ConversationSidebar from '../components/negotiation/ConversationSidebar.vue'
import {
  ProductRequirementForm,
  ChatPanel,
  ContractPreview,
  type RequirementData
} from '../components/negotiation'
import type { UiMessage } from '../types/chat/message'

// ==================== State Management ====================

const workspace = useNegotiationWorkspace()
const notificationStore = useNotificationStore()

const {
  // State
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
  messages,

  // WebSocket
  isConnected,

  // Actions
  selectMerchant,
  switchToConversation,
  sendText,
  sendQuoteFromForm,
  sendImage,
  sendAttachment,
  giftPoints,
  acceptQuote,
  rejectQuote,
  sendCounterQuote,
  confirmContract,
  generateFormalContract,
  updateLocalRequirement,
  initialize,
  cleanup
} = workspace

// ==================== Handlers ====================

function handleSendMessage(text: string) {
  sendText(text)
}

function handleRequirementUpdate(data: RequirementData) {
  // 实时更新本地数据，合同预览会自动响应
  updateLocalRequirement(data)
}

function handleSendQuote(data: RequirementData) {
  // 发送当前表单数据作为报价
  sendQuoteFromForm({
    price: data.price || 0,
    quantity: String(data.quantity),
    unit: data.unit,
    deliveryPlace: data.deliveryPlace,
    arrivalDate: data.deliveryDate,
    paymentMethod: data.paymentMethod || '货到付款',
    remark: data.remark
  })
  ElMessage.success('报价已发送')
}

function handleSendImage(payload: any) {
  sendImage(payload)
}

function handleSendAttachment(payload: any) {
  sendAttachment(payload)
}

function handleGiftPoints(toUserId: number, points: number, remark?: string) {
  giftPoints(toUserId, points, remark)
}

function handleConfirm() {
  confirmContract()
}

function handleGenerateFormalContract() {
  generateFormalContract()
}

function handleSelectMerchant(merchant: MerchantGroup) {
  selectMerchant(merchant)
}

// ==================== Quote Handlers (from chat) ====================

function handleAcceptQuote(msg: UiMessage) {
  if (typeof msg.id === 'number') {
    acceptQuote(msg.id)
  }
}

function handleCounterQuote(
  _msg: UiMessage,
  payload: { price?: number; basisPrice?: number; quantity?: string; remark?: string }
) {
  sendCounterQuote(payload)
}

function handleRejectQuote(msg: UiMessage) {
  if (typeof msg.id === 'number') {
    rejectQuote(msg.id)
  }
}

function handleDraftContract(_msg: UiMessage) {
  contractStatus.value = 'PENDING_CONFIRM'
  ElMessage.success('合同已准备就绪，请双方确认条款')
}

// ==================== Lifecycle ====================

// 监听当前会话变化，设置活跃会话和标记已读
watch(
  () => currentConversation.value?.id,
  (conversationId) => {
    if (conversationId) {
      // 设置为活跃会话（不弹通知）
      setActiveConversation(conversationId)
      // 标记该会话已读
      notificationStore.markConversationRead(conversationId)
    }
  },
  { immediate: true }
)

onMounted(() => {
  initialize()
})

onBeforeUnmount(() => {
  // 清除活跃会话
  setActiveConversation(null)
  cleanup()
})
</script>

<template>
  <div class="h-full flex flex-col bg-gray-100 overflow-hidden -m-4 md:-m-6">
    <!-- 加载状态 -->
    <div v-if="loading" class="flex-1 flex items-center justify-center">
      <div class="w-6 h-6 border-2 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- 主内容区 -->
    <div v-else class="flex-1 flex overflow-hidden">
      <!-- 商户侧边栏 -->
      <ConversationSidebar
        :state="sidebarState"
        :merchant-groups="merchantGroups"
        :current-peer-id="currentConversation?.peerUserId"
        @update:state="sidebarState = $event"
        @select="handleSelectMerchant"
      />

      <!-- 空状态 -->
      <div
        v-if="!currentConversation"
        class="flex-1 flex items-center justify-center bg-gray-50"
      >
        <div class="text-center">
          <MessageCircle class="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <h3 class="text-base font-medium text-gray-600 mb-1">选择一个商户开始议价</h3>
          <p class="text-xs text-gray-400">
            从左侧选择商户，或从供应/采购详情页发起
          </p>
        </div>
      </div>

      <!-- 工作区内容 -->
      <template v-else>
        <!-- 左侧：产品详情 + 聊天 (4:6) -->
        <div
          :class="[
            'flex flex-col p-3 gap-3 overflow-hidden transition-all duration-300',
            sidebarState === 'mini'
              ? 'w-[45%] min-w-[380px] max-w-[520px]'
              : 'w-[38%] min-w-[340px] max-w-[440px]'
          ]"
        >
          <!-- 产品需求表单 (40%) - 编辑即调整报价 -->
          <div class="h-2/5 overflow-auto">
            <ProductRequirementForm
              :initial-data="requirementData"
              :readonly="!!buyerConfirmed || !!sellerConfirmed"
              :show-send-button="true"
              :sending="sending"
              @update="handleRequirementUpdate"
              @send-quote="handleSendQuote"
              class="h-full"
            />
          </div>

          <!-- 聊天面板 (60%) - 文本沟通 + 附件 + 赠送积分 -->
          <div class="h-3/5 bg-white rounded-xl shadow-sm overflow-hidden flex flex-col">
            <ChatPanel
              v-if="!loadingMessages"
              :messages="messages"
              :peer-name="peerInfo.name"
              :peer-company="peerInfo.company"
              :peer-user-id="currentConversation?.peerUserId"
              :ws-connected="isConnected"
              :sending="sending"
              :show-quote-button="false"
              @send="handleSendMessage"
              @send-image="handleSendImage"
              @send-attachment="handleSendAttachment"
              @gift-points="handleGiftPoints"
              @accept-quote="handleAcceptQuote"
              @counter-quote="handleCounterQuote"
              @reject-quote="handleRejectQuote"
              @draft-contract="handleDraftContract"
            />
            <div v-else class="flex-1 flex items-center justify-center">
              <div class="w-5 h-5 border-2 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
            </div>
          </div>
        </div>

        <!-- 右侧：合同实时预览（条款确认 + 正式签约） -->
        <div class="flex-1 flex flex-col p-3 pl-0 overflow-hidden">
          <ContractPreview
            :contract-data="contractData"
            :status="contractStatus"
            :buyer-confirmed="buyerConfirmed"
            :seller-confirmed="sellerConfirmed"
            :current-is-buyer="currentIsBuyer"
            @confirm="handleConfirm"
            @generate-formal-contract="handleGenerateFormalContract"
          />
        </div>
      </template>
    </div>
  </div>
</template>
