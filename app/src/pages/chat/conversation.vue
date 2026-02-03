<script setup lang="ts">
import { ref, computed, nextTick, onUnmounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useAuthStore } from '../../store/auth'
import {
  getConversationMessages,
  markConversationRead,
  sendMessage as sendMessageApi,
  type ChatMessageResponse,
} from '../../api/chat'
import { useWebSocket, type WsMessage } from '../../composables/useWebSocket'
import { formatChatTime } from '../../utils/format'

const authStore = useAuthStore()
const myUserId = computed(() => authStore.user?.userId)

const conversationId = ref(0)
const peerUserId = ref(0)
const peerName = ref('')
const messages = ref<ChatMessageResponse[]>([])
const inputText = ref('')
const loading = ref(true)
const scrollId = ref('')

// WebSocket
const { connected, connecting, connect, sendChatMessage, onMessage, disconnect } = useWebSocket()

function handleWsMessage(msg: WsMessage) {
  if (msg.type !== 'chat' || msg.conversationId !== conversationId.value) return

  // Avoid duplicating messages that were sent by the current user (optimistic insert)
  if (msg.fromUserId === myUserId.value) return

  const newMsg: ChatMessageResponse = {
    id: Date.now(),
    conversationId: msg.conversationId,
    fromUserId: msg.fromUserId!,
    toUserId: msg.toUserId!,
    content: msg.content || '',
    msgType: msg.msgType,
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

    // Connect WebSocket and register handler
    connect()
    onMessage(handleWsMessage)

    await loadMessages()
    markConversationRead(conversationId.value).catch(() => {})
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

function scrollToBottom() {
  if (messages.value.length > 0) {
    scrollId.value = `msg-${messages.value[messages.value.length - 1].id}`
  }
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text) return
  inputText.value = ''

  // Optimistic: show the message immediately
  const tempMsg: ChatMessageResponse = {
    id: Date.now(),
    conversationId: conversationId.value,
    fromUserId: myUserId.value!,
    toUserId: peerUserId.value,
    content: text,
    msgType: 'text',
    read: true,
    createTime: new Date().toISOString(),
  }
  messages.value.push(tempMsg)
  await nextTick()
  scrollToBottom()

  // Try WebSocket first; fall back to HTTP API
  const sent = sendChatMessage(conversationId.value, peerUserId.value, text)
  if (!sent) {
    try {
      await sendMessageApi(conversationId.value, text)
    } catch {
      uni.showToast({ title: '发送失败', icon: 'none' })
    }
  }
}

function isMyMsg(msg: ChatMessageResponse): boolean {
  return msg.fromUserId === myUserId.value
}
</script>

<template>
  <view class="conversation-page">
    <!-- Connection status bar -->
    <view v-if="!connected" class="status-bar">
      <text class="status-bar__text">{{ connecting ? '连接中...' : '未连接' }}</text>
    </view>

    <scroll-view
      scroll-y
      class="message-list"
      :scroll-into-view="scrollId"
      scroll-with-animation
    >
      <WgSkeleton v-if="loading" type="list" :rows="5" />

      <view
        v-for="msg in messages"
        :key="msg.id"
        :id="`msg-${msg.id}`"
        :class="['message', isMyMsg(msg) ? 'message--mine' : 'message--peer']"
      >
        <view class="message__bubble">
          <text class="message__text">{{ msg.content }}</text>
        </view>
        <text class="message__time">{{ formatChatTime(msg.createTime) }}</text>
      </view>

      <WgEmpty v-if="messages.length === 0 && !loading" text="暂无消息" description="发送一条消息开始聊天吧" />
    </scroll-view>

    <!-- Input area -->
    <view class="input-bar safe-area-bottom">
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

.message-list {
  flex: 1;
  padding: $spacing-md;
  padding-bottom: 120rpx;
}

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

  &__time {
    font-size: $font-xs;
    color: $text-placeholder;
    margin-top: 4rpx;
  }
}

.input-bar {
  display: flex;
  align-items: center;
  padding: $spacing-sm $spacing-md;
  background: $bg-card;
  border-top: 1rpx solid $border-light;
  gap: $spacing-sm;

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
  }

  &__send-text {
    color: #fff;
    font-size: $font-md;
    font-weight: bold;
  }
}
</style>
