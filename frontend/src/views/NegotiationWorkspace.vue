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
import { onMounted, onBeforeUnmount, watch, ref } from 'vue'
import { showToast } from '@/composables/useToast'
import { MessageCircle, ArrowLeft, FileText } from 'lucide-vue-next'

// Composables
import { useNegotiationWorkspace, type MerchantGroup } from '../composables/useNegotiationWorkspace'
import { setActiveConversation } from '../composables/useGlobalWebSocket'
import { useNotificationStore } from '../stores/notification'

// Components
import ConversationSidebar from '../components/negotiation/ConversationSidebar.vue'
import {
  ProductRequirementForm,
  MergedChatPanel,
  ContractPreview,
  type RequirementData
} from '../components/negotiation'
import type { UiMessage } from '../types/chat/message'

// ==================== Mobile State ====================

/** 是否为移动端（< 768px） */
const isMobile = ref(false)
/** 移动端当前面板：'list' | 'chat' | 'contract' */
const mobilePanelView = ref<'list' | 'chat' | 'contract'>('list')
/** 移动端合同预览弹窗 */
const showContractModal = ref(false)

function setupMobileDetect() {
  const mq = window.matchMedia('(max-width: 767px)')
  isMobile.value = mq.matches
  mq.addEventListener('change', (e) => {
    isMobile.value = e.matches
    if (!e.matches) {
      // 回到 PC 端时关闭合同弹窗
      showContractModal.value = false
    }
  })
}

function handleMobileSelectMerchant(merchant: MerchantGroup) {
  handleSelectMerchant(merchant)
  if (isMobile.value) {
    mobilePanelView.value = 'chat'
  }
}

function goBackToList() {
  mobilePanelView.value = 'list'
}

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
  activeConversationId,

  // Computed
  merchantGroups,
  currentMerchantConversations,
  activeConversation,
  mergedMessages,
  activeProductName,
  peerInfo,
  requirementData,
  contractData,
  currentIsBuyer,

  // WebSocket
  isConnected,

  // Actions
  selectMerchant,
  activateConversation,
  sendText,
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

function handleActivateConversation(convId: number) {
  activateConversation(convId)
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
  showToast.success('合同已准备就绪，请双方确认条款')
}

// ==================== Lifecycle ====================

// 监听激活会话变化，设置活跃会话和标记已读
watch(
  () => activeConversationId.value,
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
  setupMobileDetect()
  initialize()
})

onBeforeUnmount(() => {
  // 清除活跃会话
  setActiveConversation(null)
  cleanup()
})
</script>

<template>
  <div class="h-full flex flex-col bg-neutral-100 overflow-hidden -m-4 md:-m-6">
    <!-- 加载状态 -->
    <div v-if="loading" class="flex-1 flex items-center justify-center">
      <div class="w-6 h-6 border-2 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
    </div>

    <!-- ===================== 移动端布局（< md） ===================== -->
    <template v-else-if="isMobile">
      <!-- 面板：会话列表 -->
      <div v-if="mobilePanelView === 'list'" class="flex-1 flex flex-col overflow-hidden bg-white">
        <!-- 顶部标题栏 -->
        <div class="px-4 py-3 border-b border-neutral-100 flex items-center gap-2 shrink-0">
          <MessageCircle class="w-5 h-5 text-brand-500" />
          <h2 class="text-base font-bold text-neutral-900">聊天议价</h2>
        </div>

        <!-- 空状态 -->
        <div v-if="merchantGroups.length === 0" class="flex-1 flex items-center justify-center">
          <div class="text-center px-6">
            <MessageCircle class="w-12 h-12 text-neutral-300 mx-auto mb-3" />
            <h3 class="text-base font-medium text-neutral-600 mb-1">暂无会话</h3>
            <p class="text-xs text-neutral-400">从供应/采购详情页发起议价</p>
          </div>
        </div>

        <!-- 商户列表（直接展开渲染，不用 ConversationSidebar） -->
        <div v-else class="flex-1 overflow-y-auto divide-y divide-neutral-50">
          <div
            v-for="merchant in merchantGroups"
            :key="merchant.peerId"
            class="flex items-center gap-3 px-4 py-3 cursor-pointer active:bg-neutral-100 transition-colors"
            :class="currentConversation?.peerUserId === merchant.peerId ? 'bg-brand-50' : ''"
            @click="handleMobileSelectMerchant(merchant)"
          >
            <div class="w-10 h-10 rounded-xl bg-brand-500 text-white flex items-center justify-center font-bold text-sm shrink-0 relative">
              {{ merchant.peerName?.[0]?.toUpperCase() || '?' }}
              <span
                v-if="merchant.totalUnread"
                class="absolute -top-1 -right-1 w-4 h-4 bg-error-500 text-white text-[9px] rounded-full flex items-center justify-center font-bold"
              >
                {{ merchant.totalUnread > 9 ? '9+' : merchant.totalUnread }}
              </span>
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between">
                <span class="font-medium text-sm text-neutral-900 truncate">{{ merchant.peerName }}</span>
                <span class="text-[10px] text-neutral-400 shrink-0 ml-1">
                  {{ merchant.lastTime ? new Date(merchant.lastTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) : '' }}
                </span>
              </div>
              <div v-if="merchant.peerCompany" class="text-xs text-neutral-400 truncate">{{ merchant.peerCompany }}</div>
              <div class="text-xs text-neutral-500 truncate">{{ merchant.lastContent || '暂无消息' }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 面板：聊天工作区 -->
      <div v-else-if="mobilePanelView === 'chat'" class="flex-1 flex flex-col overflow-hidden">
        <!-- 顶部导航栏 -->
        <div class="bg-white px-3 py-2.5 border-b border-neutral-200 flex items-center gap-2 shrink-0">
          <button
            class="p-1.5 rounded-lg text-neutral-500 hover:bg-neutral-100 active:bg-neutral-200 transition-colors"
            @click="goBackToList"
          >
            <ArrowLeft class="w-5 h-5" />
          </button>
          <div class="flex-1 min-w-0">
            <div class="font-medium text-sm text-neutral-900 truncate">{{ peerInfo.name }}</div>
            <div v-if="peerInfo.company" class="text-xs text-neutral-400 truncate">{{ peerInfo.company }}</div>
          </div>
          <!-- 查看合同按钮 -->
          <button
            class="flex items-center gap-1 px-2.5 py-1.5 bg-brand-50 text-brand-600 rounded-lg text-xs font-bold shrink-0"
            @click="showContractModal = true"
          >
            <FileText class="w-3.5 h-3.5" />
            合同预览
          </button>
        </div>

        <!-- 聊天内容区（产品表单 + 聊天面板上下排列） -->
        <div class="flex-1 flex flex-col overflow-hidden">
          <!-- 产品需求表单（折叠区） -->
          <div class="overflow-auto bg-white border-b border-neutral-100" style="max-height: 35%;">
            <ProductRequirementForm
              :initial-data="requirementData"
              :readonly="!!buyerConfirmed || !!sellerConfirmed"
              :subject-type="activeConversation?.subjectType === 'SUPPLY' ? 'SUPPLY' : 'NEED'"
              :sending="sending"
              @update="handleRequirementUpdate"
            />
          </div>

          <!-- 聊天面板 -->
          <div class="flex-1 bg-white overflow-hidden flex flex-col">
            <MergedChatPanel
              v-if="!loadingMessages"
              :conversations="currentMerchantConversations"
              :merged-messages="mergedMessages"
              :active-conversation-id="activeConversationId"
              :active-product-name="activeProductName"
              :peer-name="peerInfo.name"
              :peer-company="peerInfo.company"
              :peer-user-id="currentConversation?.peerUserId"
              :ws-connected="isConnected"
              :sending="sending"
              @send="handleSendMessage"
              @send-image="handleSendImage"
              @send-attachment="handleSendAttachment"
              @gift-points="handleGiftPoints"
              @accept-quote="handleAcceptQuote"
              @counter-quote="handleCounterQuote"
              @reject-quote="handleRejectQuote"
              @draft-contract="handleDraftContract"
              @activate-conversation="handleActivateConversation"
            />
            <div v-else class="flex-1 flex items-center justify-center">
              <div class="w-5 h-5 border-2 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 移动端合同预览弹窗（全屏 Modal） -->
      <Teleport to="body">
        <div
          v-if="showContractModal"
          class="fixed inset-0 z-50 flex flex-col bg-white"
        >
          <!-- 弹窗顶栏 -->
          <div class="flex items-center gap-2 px-4 py-3 border-b border-neutral-200 shrink-0">
            <button
              class="p-1.5 rounded-lg text-neutral-500 hover:bg-neutral-100 active:bg-neutral-200 transition-colors"
              @click="showContractModal = false"
            >
              <ArrowLeft class="w-5 h-5" />
            </button>
            <span class="font-bold text-neutral-900">合同预览</span>
          </div>
          <!-- 合同内容 -->
          <div class="flex-1 overflow-auto p-3">
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
        </div>
      </Teleport>
    </template>

    <!-- ===================== PC 端布局（>= md） ===================== -->
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
        class="flex-1 flex items-center justify-center bg-neutral-50"
      >
        <div class="text-center">
          <MessageCircle class="w-12 h-12 text-neutral-300 mx-auto mb-3" />
          <h3 class="text-base font-medium text-neutral-600 mb-1">选择一个商户开始议价</h3>
          <p class="text-xs text-neutral-400">
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
              :subject-type="activeConversation?.subjectType === 'SUPPLY' ? 'SUPPLY' : 'NEED'"
              :sending="sending"
              @update="handleRequirementUpdate"
              class="h-full"
            />
          </div>

          <!-- 聊天面板 (60%) - 按产品分段的合并聊天视图 -->
          <div class="h-3/5 bg-white rounded-xl shadow-sm overflow-hidden flex flex-col">
            <MergedChatPanel
              v-if="!loadingMessages"
              :conversations="currentMerchantConversations"
              :merged-messages="mergedMessages"
              :active-conversation-id="activeConversationId"
              :active-product-name="activeProductName"
              :peer-name="peerInfo.name"
              :peer-company="peerInfo.company"
              :peer-user-id="currentConversation?.peerUserId"
              :ws-connected="isConnected"
              :sending="sending"
              @send="handleSendMessage"
              @send-image="handleSendImage"
              @send-attachment="handleSendAttachment"
              @gift-points="handleGiftPoints"
              @accept-quote="handleAcceptQuote"
              @counter-quote="handleCounterQuote"
              @reject-quote="handleRejectQuote"
              @draft-contract="handleDraftContract"
              @activate-conversation="handleActivateConversation"
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
