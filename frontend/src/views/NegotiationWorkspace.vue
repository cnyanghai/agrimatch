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
import { onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Menu,
  Wifi,
  WifiOff,
  ArrowLeft,
  MessageCircle,
  ChevronDown
} from 'lucide-vue-next'

// Composables
import { useNegotiationWorkspace, type MerchantGroup } from '../composables/useNegotiationWorkspace'

// Components
import ConversationSidebar from '../components/negotiation/ConversationSidebar.vue'
import {
  ProductRequirementForm,
  ChatPanel,
  ContractPreview,
  type RequirementData
} from '../components/negotiation'
import type { UiMessage } from '../types/chat/message'

const router = useRouter()

// ==================== State Management ====================

const workspace = useNegotiationWorkspace()

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

function goBack() {
  router.push('/console')
}

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

function handleToggleSidebar() {
  if (sidebarState.value === 'hidden') {
    sidebarState.value = 'expanded'
  } else {
    sidebarState.value = 'hidden'
  }
}

function getSubjectName(conv: any): string {
  if (!conv?.subjectSnapshotJson) return '通用会话'
  try {
    const snapshot = JSON.parse(conv.subjectSnapshotJson)
    return snapshot.productName || snapshot.title || '产品'
  } catch {
    return '产品'
  }
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

onMounted(() => {
  initialize()
})

onBeforeUnmount(() => {
  cleanup()
})
</script>

<template>
  <div class="h-screen flex flex-col bg-gray-100 overflow-hidden">
    <!-- 顶部导航 -->
    <header class="bg-white border-b border-gray-200 px-4 py-2 flex items-center justify-between shrink-0 shadow-sm z-10">
      <div class="flex items-center gap-3">
        <!-- 侧边栏切换 -->
        <button
          v-if="sidebarState === 'hidden'"
          @click="handleToggleSidebar"
          class="p-1.5 hover:bg-gray-100 rounded-lg transition-colors flex items-center gap-1.5"
        >
          <Menu class="w-4 h-4 text-gray-600" />
          <span class="text-xs text-gray-600">商户</span>
        </button>

        <!-- 返回按钮 -->
        <button
          @click="goBack"
          class="p-1.5 hover:bg-gray-100 rounded-lg transition-colors"
        >
          <ArrowLeft class="w-4 h-4 text-gray-600" />
        </button>

        <!-- 当前会话信息 -->
        <div v-if="currentConversation" class="flex items-center gap-2">
          <div class="w-8 h-8 rounded-full bg-brand-500 text-white flex items-center justify-center font-bold text-xs">
            {{ peerInfo.name[0]?.toUpperCase() || '?' }}
          </div>
          <div>
            <h1 class="font-bold text-gray-900 flex items-center gap-1.5 text-sm">
              {{ peerInfo.name }}
              <span v-if="peerInfo.company" class="text-gray-400 font-normal text-xs">
                · {{ peerInfo.company }}
              </span>
            </h1>
            <div class="flex items-center gap-2 text-[11px] text-gray-500">
              <!-- 当前标的下拉选择 -->
              <div v-if="currentMerchantConversations.length > 1" class="relative group">
                <button class="flex items-center gap-1 hover:text-brand-600">
                  <span>{{ requirementData.productName || '产品议价' }}</span>
                  <ChevronDown class="w-3 h-3" />
                </button>
                <!-- 下拉菜单 -->
                <div class="absolute top-full left-0 mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1 min-w-[160px] hidden group-hover:block z-20">
                  <div
                    v-for="conv in currentMerchantConversations"
                    :key="conv.id"
                    :class="[
                      'px-3 py-1.5 text-xs cursor-pointer',
                      conv.id === currentConversation?.id
                        ? 'bg-brand-50 text-brand-600'
                        : 'hover:bg-gray-50 text-gray-600'
                    ]"
                    @click="switchToConversation(conv)"
                  >
                    {{ getSubjectName(conv) }}
                    <span v-if="conv.id === currentConversation?.id" class="text-[10px] text-brand-400 ml-1">当前</span>
                  </div>
                </div>
              </div>
              <span v-else>{{ requirementData.productName || '产品议价' }}</span>

              <span class="text-gray-300">|</span>
              <span class="flex items-center gap-1">
                <component
                  :is="isConnected ? Wifi : WifiOff"
                  :class="['w-2.5 h-2.5', isConnected ? 'text-green-500' : 'text-gray-400']"
                />
                {{ isConnected ? '已连接' : '连接中' }}
              </span>
            </div>
          </div>
        </div>
        <div v-else class="text-gray-500 text-sm">
          请选择商户
        </div>
      </div>

      <!-- 右侧状态 -->
      <div class="flex items-center gap-2">
        <div class="flex items-center gap-1 text-[11px] text-gray-500">
          <span class="w-1.5 h-1.5 rounded-full bg-green-500"></span>
          系统正常
        </div>
      </div>
    </header>

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
        <!-- 左侧：产品详情 + 聊天 (5:5) -->
        <div class="w-[38%] min-w-[340px] max-w-[440px] flex flex-col p-3 gap-3 overflow-hidden">
          <!-- 产品需求表单 (50%) - 编辑即调整报价 -->
          <div class="h-1/2 overflow-auto">
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

          <!-- 聊天面板 (50%) - 文本沟通 + 附件 + 赠送积分 -->
          <div class="h-1/2 bg-white rounded-xl shadow-sm overflow-hidden flex flex-col">
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
