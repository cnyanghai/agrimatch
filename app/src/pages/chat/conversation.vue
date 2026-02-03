<script setup lang="ts">
import { ref, computed, nextTick, onUnmounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useAuthStore } from '../../store/auth'
import {
  getConversation,
  getConversationMessages,
  markConversationRead,
  sendMessage as sendMessageApi,
  confirmChatOffer,
  rejectChatOffer,
  type ChatMessageResponse,
  type ChatConversationResponse,
} from '../../api/chat'
import { getSupply, type SupplyResponse } from '../../api/supply'
import { getRequirement, type RequirementResponse } from '../../api/requirement'
import { useWebSocket, type WsMessage } from '../../composables/useWebSocket'
import { formatChatTime, formatPrice } from '../../utils/format'

const authStore = useAuthStore()
const myUserId = computed(() => authStore.user?.userId)

const conversationId = ref(0)
const peerUserId = ref(0)
const peerName = ref('')
const messages = ref<ChatMessageResponse[]>([])
const inputText = ref('')
const loading = ref(true)
const scrollId = ref('')
const showActions = ref(false)

// Subject context
const conversation = ref<ChatConversationResponse | null>(null)
const subjectInfo = ref<{ type: string; name: string; price: string; id: number } | null>(null)

// WebSocket
const { connected, connecting, connect, sendChatMessage, onMessage, disconnect } = useWebSocket()

function handleWsMessage(msg: WsMessage) {
  if (msg.type !== 'chat' || msg.conversationId !== conversationId.value) return
  if (msg.fromUserId === myUserId.value) return

  const newMsg: ChatMessageResponse = {
    id: Date.now(),
    conversationId: msg.conversationId,
    fromUserId: msg.fromUserId!,
    toUserId: msg.toUserId!,
    content: msg.content || '',
    msgType: msg.msgType,
    payloadJson: msg.payloadJson,
    quoteStatus: msg.quoteStatus,
    read: true,
    createTime: msg.timestamp || new Date().toISOString(),
  }
  messages.value.push(newMsg)
  nextTick(() => scrollToBottom())
}

onLoad(async (options) => {
  if (options?.id) {
    conversationId.value = Number(options.id)
    peerUserId.value = Number(options.peerId || 0)
    peerName.value = decodeURIComponent(options.name || '')
    uni.setNavigationBarTitle({ title: peerName.value || '聊天' })

    connect()
    onMessage(handleWsMessage)

    await loadMessages()
    markConversationRead(conversationId.value).catch(() => {})
    loadSubjectContext()
  }
})

onUnmounted(() => {
  disconnect()
})

async function loadMessages() {
  loading.value = true
  try {
    const res = await getConversationMessages(conversationId.value, 100)
    messages.value = (res || []).reverse()
    await nextTick()
    scrollToBottom()
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

async function loadSubjectContext() {
  try {
    const conv = await getConversation(conversationId.value)
    conversation.value = conv
    if (!conv?.subjectType || !conv?.subjectId) return

    if (conv.subjectType === 'supply') {
      const supply = await getSupply(conv.subjectId)
      if (supply) {
        subjectInfo.value = {
          type: 'supply',
          name: supply.categoryName,
          price: formatPrice(supply.exFactoryPrice),
          id: supply.id,
        }
      }
    } else if (conv.subjectType === 'requirement') {
      const req = await getRequirement(conv.subjectId)
      if (req) {
        subjectInfo.value = {
          type: 'requirement',
          name: req.categoryName,
          price: formatPrice(req.expectedPrice),
          id: req.id,
        }
      }
    }
  } catch {
    // silent
  }
}

function goSubjectDetail() {
  if (!subjectInfo.value) return
  const { type, id } = subjectInfo.value
  if (type === 'supply') {
    uni.navigateTo({ url: `/pages/supply/detail?id=${id}` })
  } else {
    uni.navigateTo({ url: `/pages/requirement/detail?id=${id}` })
  }
}

function scrollToBottom() {
  if (messages.value.length > 0) {
    scrollId.value = `msg-${messages.value[messages.value.length - 1].id}`
  }
}

/** 时间分组：两条消息间隔 > 5 分钟显示分隔线 */
function shouldShowTimeSeparator(index: number): boolean {
  if (index === 0) return true
  const cur = messages.value[index]
  const prev = messages.value[index - 1]
  if (!cur.createTime || !prev.createTime) return false
  const diff = new Date(cur.createTime).getTime() - new Date(prev.createTime).getTime()
  return diff > 5 * 60 * 1000
}

function formatTimeSeparator(time?: string): string {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const hm = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`

  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const target = new Date(d.getFullYear(), d.getMonth(), d.getDate())
  const dayDiff = Math.floor((today.getTime() - target.getTime()) / (24 * 60 * 60 * 1000))

  if (dayDiff === 0) return hm
  if (dayDiff === 1) return `昨天 ${hm}`
  if (dayDiff < 7) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return `${weekdays[d.getDay()]} ${hm}`
  }
  return `${d.getMonth() + 1}月${d.getDate()}日 ${hm}`
}

let tempIdCounter = 0

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text) return
  inputText.value = ''

  const tempId = `temp-${++tempIdCounter}-${Date.now()}`
  const tempMsg: ChatMessageResponse = {
    id: Date.now(),
    conversationId: conversationId.value,
    fromUserId: myUserId.value!,
    toUserId: peerUserId.value,
    content: text,
    msgType: 'text',
    read: true,
    createTime: new Date().toISOString(),
    _status: 'pending',
    _tempId: tempId,
  }
  messages.value.push(tempMsg)
  await nextTick()
  scrollToBottom()

  // Try WebSocket first; fall back to HTTP API
  const sent = sendChatMessage(conversationId.value, peerUserId.value, text)
  if (sent) {
    updateMsgStatus(tempId, 'sent')
  } else {
    try {
      await sendMessageApi(conversationId.value, text)
      updateMsgStatus(tempId, 'sent')
    } catch {
      updateMsgStatus(tempId, 'failed')
    }
  }
}

function updateMsgStatus(tempId: string, status: 'sent' | 'failed') {
  const msg = messages.value.find(m => m._tempId === tempId)
  if (msg) msg._status = status
}

async function retryMessage(msg: ChatMessageResponse) {
  if (!msg._tempId) return
  msg._status = 'pending'
  try {
    await sendMessageApi(conversationId.value, msg.content)
    msg._status = 'sent'
  } catch {
    msg._status = 'failed'
  }
}

function isMyMsg(msg: ChatMessageResponse): boolean {
  return msg.fromUserId === myUserId.value
}

// --- Action sheet ("+") ---
function handleShowActions() {
  showActions.value = false
  uni.showActionSheet({
    itemList: ['发送报价'],
    success: (res) => {
      if (res.tapIndex === 0) {
        goQuoteForm()
      }
    },
  })
}

function goQuoteForm() {
  const params = [`conversationId=${conversationId.value}`]
  if (conversation.value?.subjectType) {
    params.push(`subjectType=${conversation.value.subjectType}`)
  }
  if (conversation.value?.subjectId) {
    params.push(`subjectId=${conversation.value.subjectId}`)
  }
  uni.navigateTo({ url: `/pages/chat/quote-form?${params.join('&')}` })
}

// --- Quote operations ---
async function handleAcceptQuote(msg: ChatMessageResponse) {
  uni.showModal({
    title: '接受报价',
    content: '确认接受该报价？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await confirmChatOffer(msg.id)
        msg.quoteStatus = 'ACCEPTED'
        uni.showToast({ title: '已接受报价', icon: 'success' })
      } catch {
        // handled
      }
    },
  })
}

async function handleRejectQuote(msg: ChatMessageResponse) {
  uni.showModal({
    title: '拒绝报价',
    content: '确认拒绝该报价？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await rejectChatOffer(msg.id)
        msg.quoteStatus = 'REJECTED'
        uni.showToast({ title: '已拒绝', icon: 'none' })
      } catch {
        // handled
      }
    },
  })
}

function handleCounterQuote() {
  goQuoteForm()
}

function handleDraftContract(msg: ChatMessageResponse) {
  uni.navigateTo({ url: `/pages/contract/draft?messageId=${msg.id}` })
}
</script>

<template>
  <view class="conversation-page">
    <!-- Connection status bar -->
    <view v-if="!connected" class="status-bar">
      <text class="status-bar__text">{{ connecting ? '连接中...' : '未连接' }}</text>
    </view>

    <!-- Subject context card -->
    <view v-if="subjectInfo" class="context-card" @tap="goSubjectDetail">
      <view class="context-card__icon" :class="subjectInfo.type === 'supply' ? 'context-card__icon--brand' : 'context-card__icon--autumn'">
        <uni-icons :type="subjectInfo.type === 'supply' ? 'shop' : 'cart'" size="16" color="#fff" />
      </view>
      <view class="context-card__info">
        <text class="context-card__name">{{ subjectInfo.name }}</text>
        <text class="context-card__price">{{ subjectInfo.price }}</text>
      </view>
      <uni-icons type="right" size="14" color="#d1d5db" />
    </view>

    <scroll-view
      scroll-y
      class="message-list"
      :scroll-into-view="scrollId"
      scroll-with-animation
    >
      <WgSkeleton v-if="loading" type="list" :rows="5" />

      <template v-for="(msg, index) in messages" :key="msg.id">
        <!-- 时间分隔线 -->
        <view v-if="shouldShowTimeSeparator(index)" class="time-separator">
          <text class="time-separator__text">{{ formatTimeSeparator(msg.createTime) }}</text>
        </view>

        <!-- 系统消息 -->
        <view v-if="msg.msgType === 'SYSTEM'" class="system-message">
          <text class="system-message__text">{{ msg.content }}</text>
        </view>

        <!-- 报价消息 -->
        <view
          v-else-if="msg.msgType === 'QUOTE'"
          :id="`msg-${msg.id}`"
          :class="['message', isMyMsg(msg) ? 'message--mine' : 'message--peer']"
        >
          <WgQuoteCard
            :message="msg"
            :is-mine="isMyMsg(msg)"
            @accept="handleAcceptQuote(msg)"
            @reject="handleRejectQuote(msg)"
            @counter="handleCounterQuote"
            @draft-contract="handleDraftContract(msg)"
          />
          <view class="message__meta">
            <text class="message__time">{{ formatChatTime(msg.createTime) }}</text>
          </view>
        </view>

        <!-- 普通文字消息 -->
        <view
          v-else
          :id="`msg-${msg.id}`"
          :class="['message', isMyMsg(msg) ? 'message--mine' : 'message--peer']"
        >
          <view class="message__bubble">
            <text class="message__text">{{ msg.content }}</text>
          </view>
          <view class="message__meta">
            <text class="message__time">{{ formatChatTime(msg.createTime) }}</text>
            <!-- 发送状态 -->
            <template v-if="isMyMsg(msg) && msg._status">
              <text v-if="msg._status === 'pending'" class="message__status message__status--pending">...</text>
              <text v-else-if="msg._status === 'sent'" class="message__status message__status--sent">✓</text>
              <view v-else-if="msg._status === 'failed'" class="message__status--failed" @tap.stop="retryMessage(msg)">
                <text class="message__status message__status--error">⚠</text>
                <text class="message__retry">重发</text>
              </view>
            </template>
          </view>
        </view>
      </template>

      <WgEmpty v-if="messages.length === 0 && !loading" text="暂无消息" description="发送一条消息开始聊天吧" />
    </scroll-view>

    <!-- Input area -->
    <view class="input-bar safe-area-bottom">
      <view class="input-bar__plus" @tap="handleShowActions">
        <text class="input-bar__plus-icon">+</text>
      </view>
      <input
        v-model="inputText"
        class="input-bar__input"
        placeholder="输入消息..."
        confirm-type="send"
        @confirm="sendMessage"
      />
      <view class="input-bar__send" @tap="sendMessage">
        <text class="input-bar__send-text">发送</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.conversation-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: $bg-page;
}

.status-bar {
  padding: $spacing-xs $spacing-md;
  background: $bg-hover;
  text-align: center;

  &__text {
    font-size: $font-xs;
    color: $accent-400;
  }
}

/* ===== Context Card ===== */
.context-card {
  display: flex;
  align-items: center;
  padding: $spacing-sm $spacing-md;
  background: $bg-card;
  border-bottom: 1rpx solid $border-light;
  gap: $spacing-sm;

  &__icon {
    width: 56rpx;
    height: 56rpx;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &--brand { background: $brand-600; }
    &--autumn { background: $autumn-400; }
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__name {
    font-size: $font-sm;
    color: $text-primary;
    font-weight: 600;
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__price {
    font-size: $font-xs;
    color: $accent-400;
    font-weight: 600;
  }
}

/* ===== Message List ===== */
.message-list {
  flex: 1;
  padding: $spacing-md;
  padding-bottom: 120rpx;
}

/* ===== System Message ===== */
.system-message {
  display: flex;
  justify-content: center;
  padding: $spacing-sm 0;

  &__text {
    font-size: $font-xs;
    color: $text-placeholder;
    background: rgba(0, 0, 0, 0.04);
    padding: 6rpx 24rpx;
    border-radius: 20rpx;
    max-width: 80%;
    text-align: center;
  }
}

/* ===== Time Separator ===== */
.time-separator {
  display: flex;
  justify-content: center;
  padding: $spacing-md 0 $spacing-sm;

  &__text {
    font-size: $font-xs;
    color: $text-placeholder;
    background: rgba(0, 0, 0, 0.04);
    padding: 4rpx 20rpx;
    border-radius: 20rpx;
  }
}

/* ===== Message ===== */
.message {
  display: flex;
  flex-direction: column;
  margin-bottom: $spacing-md;

  &--mine {
    align-items: flex-end;

    .message__bubble {
      background: $brand-600;
      border-radius: $radius-lg $radius-lg 4rpx $radius-lg;
    }
    .message__text { color: #fff; }
  }

  &--peer {
    align-items: flex-start;

    .message__bubble {
      background: $bg-card;
      border-radius: $radius-lg $radius-lg $radius-lg 4rpx;
    }
    .message__text { color: $text-primary; }
  }

  &__bubble {
    max-width: 70%;
    padding: $spacing-sm $spacing-md;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  }

  &__text {
    font-size: $font-md;
    line-height: 1.5;
    word-break: break-all;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: 8rpx;
    margin-top: 4rpx;
  }

  &__time {
    font-size: $font-xs;
    color: $text-placeholder;
  }

  &__status {
    font-size: $font-xs;

    &--pending {
      color: $text-placeholder;
    }

    &--sent {
      color: $brand-600;
    }

    &--error {
      color: $accent-400;
    }

    &--failed {
      display: flex;
      align-items: center;
      gap: 4rpx;
    }
  }

  &__retry {
    font-size: $font-xs;
    color: $accent-400;
    text-decoration: underline;
  }
}

/* ===== Input Bar ===== */
.input-bar {
  display: flex;
  align-items: center;
  padding: $spacing-sm $spacing-md;
  background: $bg-card;
  border-top: 1rpx solid $border-light;
  gap: $spacing-sm;

  &__plus {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    background: $bg-page;
    border: 1rpx solid $border-color;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    transition: transform $transition-fast;

    &:active {
      transform: scale(0.9);
    }
  }

  &__plus-icon {
    font-size: 44rpx;
    color: $text-secondary;
    font-weight: 300;
    line-height: 1;
  }

  &__input {
    flex: 1;
    height: 72rpx;
    background: $bg-page;
    border-radius: $radius-lg;
    padding: 0 $spacing-md;
    font-size: $font-md;
  }

  &__send {
    height: 72rpx;
    padding: 0 $spacing-lg;
    background: $brand-600;
    border-radius: $radius-lg;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: transform 0.15s;

    &:active {
      transform: scale(0.95);
    }
  }

  &__send-text {
    color: #fff;
    font-size: $font-md;
    font-weight: bold;
  }
}
</style>
