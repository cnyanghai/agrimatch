<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import ConversationList from './ConversationList.vue'
import ChatMain from './ChatMain.vue'
import type { PeerGroup, TimeGroup, ChatConversationResponse, ConversationBusinessStatus } from '../../../types/chat/conversation'
import type { UiMessage } from '../../../types/chat/message'

const props = defineProps<{
  // Conversation List Props
  timeGroups: TimeGroup[]
  activePeerId: number | null
  activeConversationId: number | null
  currentPeerConversations: ChatConversationResponse[]
  searchKeyword: string
  archivedCount?: number
  conversationsLoading?: boolean
  // 新增：状态过滤相关
  activeStatusFilter?: ConversationBusinessStatus | 'ALL'
  statusCounts?: Record<ConversationBusinessStatus | 'ALL', number>
  conversationStatusMap?: Map<number, ConversationBusinessStatus>

  // Chat Main Props
  messages: UiMessage[]
  currentConversation: ChatConversationResponse | null
  peerDisplayName: string
  wsConnected: boolean
  messagesLoading?: boolean
  sending?: boolean
  isFollowing?: boolean
  canNegotiate?: boolean
  isBasisTrade?: boolean
  transactionStep?: number
  currentCompanyId?: number
  subjectName?: string
  subjectLocation?: string
  quoteCount?: number
}>()

const emit = defineEmits<{
  // Conversation List Events
  (e: 'select-peer', peer: PeerGroup): void
  (e: 'switch-conversation', conversationId: number): void
  (e: 'archive-conversation', conversationId: number): void
  (e: 'update:searchKeyword', value: string): void
  (e: 'show-archived'): void
  // 新增：状态相关
  (e: 'update:activeStatusFilter', status: ConversationBusinessStatus | 'ALL'): void
  (e: 'change-conversation-status', conversationId: number, status: ConversationBusinessStatus): void

  // Chat Main Events
  (e: 'send-text', text: string): void
  (e: 'send-image'): void
  (e: 'send-attachment'): void
  (e: 'send-gift'): void
  (e: 'open-negotiation'): void
  (e: 'open-subject-detail'): void
  (e: 'open-quote-timeline'): void
  (e: 'toggle-follow'): void
  (e: 'confirm-quote', messageId: number): void
  (e: 'reject-quote', messageId: number): void
  (e: 'counter-quote', messageId: number): void
  (e: 'open-draft-contract', messageId: number): void
  (e: 'view-contract', contractId: number): void
  (e: 'sign-contract', contractId: number): void
}>()

// 移动端状态
const isMobile = ref(false)
const showChatOnMobile = ref(false)

let mediaQuery: MediaQueryList | null = null

function checkMobile(e?: MediaQueryListEvent) {
  isMobile.value = e ? !e.matches : window.innerWidth < 1024
}

onMounted(() => {
  mediaQuery = window.matchMedia('(min-width: 1024px)')
  checkMobile()
  mediaQuery.addEventListener('change', checkMobile)
})

onBeforeUnmount(() => {
  if (mediaQuery) {
    mediaQuery.removeEventListener('change', checkMobile)
  }
})

// 移动端选择联系人后显示聊天区域
function handleSelectPeer(peer: PeerGroup) {
  emit('select-peer', peer)
  if (isMobile.value) {
    showChatOnMobile.value = true
  }
}

function handleSwitchConversation(conversationId: number) {
  emit('switch-conversation', conversationId)
  if (isMobile.value) {
    showChatOnMobile.value = true
  }
}

function handleBack() {
  showChatOnMobile.value = false
}

const chatMainRef = ref<InstanceType<typeof ChatMain> | null>(null)

function scrollToBottom() {
  chatMainRef.value?.scrollToBottom()
}

defineExpose({
  scrollToBottom
})
</script>

<template>
  <div class="h-full flex bg-white rounded-2xl shadow-lg overflow-hidden">
    <!-- 会话列表（桌面端始终显示，移动端根据状态显示） -->
    <div
      :class="[
        'w-full lg:w-80 xl:w-96 shrink-0 border-r border-gray-200',
        isMobile && showChatOnMobile ? 'hidden' : 'block'
      ]"
    >
      <ConversationList
        :time-groups="timeGroups"
        :active-peer-id="activePeerId"
        :active-conversation-id="activeConversationId"
        :current-peer-conversations="currentPeerConversations"
        :search-keyword="searchKeyword"
        :archived-count="archivedCount"
        :loading="conversationsLoading"
        :active-status-filter="activeStatusFilter"
        :status-counts="statusCounts"
        :conversation-status-map="conversationStatusMap"
        @select-peer="handleSelectPeer"
        @switch-conversation="handleSwitchConversation"
        @archive-conversation="emit('archive-conversation', $event)"
        @update:search-keyword="emit('update:searchKeyword', $event)"
        @show-archived="emit('show-archived')"
        @update:active-status-filter="emit('update:activeStatusFilter', $event)"
        @change-conversation-status="(id, status) => emit('change-conversation-status', id, status)"
      />
    </div>

    <!-- 聊天区域（桌面端始终显示，移动端根据状态显示） -->
    <div
      :class="[
        'flex-1 min-w-0',
        isMobile && !showChatOnMobile ? 'hidden' : 'block'
      ]"
    >
      <ChatMain
        ref="chatMainRef"
        :messages="messages"
        :conversation="currentConversation"
        :peer-display-name="peerDisplayName"
        :ws-connected="wsConnected"
        :loading="messagesLoading"
        :sending="sending"
        :is-following="isFollowing"
        :can-negotiate="canNegotiate"
        :is-basis-trade="isBasisTrade"
        :transaction-step="transactionStep"
        :show-back-button="isMobile"
        :current-company-id="currentCompanyId"
        :subject-name="subjectName"
        :subject-location="subjectLocation"
        :quote-count="quoteCount"
        @send-text="emit('send-text', $event)"
        @send-image="emit('send-image')"
        @send-attachment="emit('send-attachment')"
        @send-gift="emit('send-gift')"
        @open-negotiation="emit('open-negotiation')"
        @open-subject-detail="emit('open-subject-detail')"
        @open-quote-timeline="emit('open-quote-timeline')"
        @toggle-follow="emit('toggle-follow')"
        @back="handleBack"
        @confirm-quote="emit('confirm-quote', $event)"
        @reject-quote="emit('reject-quote', $event)"
        @counter-quote="emit('counter-quote', $event)"
        @open-draft-contract="emit('open-draft-contract', $event)"
        @view-contract="emit('view-contract', $event)"
        @sign-contract="emit('sign-contract', $event)"
      />
    </div>
  </div>
</template>
