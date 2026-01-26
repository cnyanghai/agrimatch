<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { ElDrawer, ElMessage } from 'element-plus'
import { ArrowUpRight, X, MessageCircle } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import { getConversationMessages, markConversationRead, type ChatMessageResponse } from '../../api/chat'
import { useAuthStore } from '../../store/auth'

// 使用统一的类型和工具函数
import type { UiMessage } from '../../types/chat'

// 使用共享的 WebSocket composable
import { useChatWebSocket, type WsIncomingMessage } from '../../composables/chat'

const props = defineProps<{
  modelValue: boolean
  conversationId: number | null
  peerDisplayName?: string
  subjectType?: string
  subjectId?: number | null
  subjectSnapshotJson?: string | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'closed'): void
}>()

const auth = useAuthStore()
const router = useRouter()

// UiMessage 类型现在从 types/chat 导入

const loading = ref(false)
const messageInput = ref('')
const messages = ref<UiMessage[]>([])

// 使用共享的 WebSocket composable
const websocket = useChatWebSocket(
  () => auth.token,
  () => !!auth.me || !!auth.token,
  {
    onMessage: handleWsMessage,
    onDisconnect: (reason) => {
      if (reason !== 'logged_out' && reason !== 'manual') {
        ElMessage.warning('实时连接已断开，正在重连…')
      }
    }
  }
)

// WebSocket 消息处理
function handleWsMessage(data: WsIncomingMessage) {
  const { type, conversationId, message, tempId, id } = data

  if (type === 'MESSAGE' && message) {
    const cid = Number(conversationId)
    if (!props.conversationId || cid !== props.conversationId) return
    messages.value.push(mapApiMessageToUi(message as ChatMessageResponse))
    nextTick().then(scrollToBottom)
    return
  }

  if (type === 'SENT') {
    const cid = Number(conversationId)
    if (!props.conversationId || cid !== props.conversationId) return
    onWsSent(tempId, id)
  }
}

const title = computed(() => props.peerDisplayName || '沟通')

// 简化的标的摘要
const subjectSummary = computed(() => {
  if (!props.subjectSnapshotJson) return ''
  try {
    const snapshot = JSON.parse(props.subjectSnapshotJson)
    const parts = []
    if (snapshot.categoryName) parts.push(snapshot.categoryName)
    if (snapshot.exFactoryPrice) parts.push(`¥${snapshot.exFactoryPrice}`)
    else if (snapshot.expectedPrice) parts.push(`意向¥${snapshot.expectedPrice}`)
    if (snapshot.quantity) parts.push(`${snapshot.quantity}吨`)
    return parts.join(' · ') || '查看详情'
  } catch {
    return '查看详情'
  }
})

function formatTime(ts?: string) {
  if (!ts) return ''
  const d = new Date(ts)
  if (Number.isNaN(d.getTime())) return ts
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function mapApiMessageToUi(m: ChatMessageResponse): UiMessage {
  const mt = (m.msgType || 'TEXT').toUpperCase()
  if (mt === 'SYSTEM') {
    return { id: m.id, type: 'system', msgType: mt, content: m.content || '', time: formatTime(m.createTime) }
  }
  const isMine = !!auth.me && m.fromUserId === auth.me.userId
  return {
    id: m.id,
    type: isMine ? 'sent' : 'received',
    msgType: mt,
    content: mt === 'TEXT' ? (m.content || '') : mt === 'QUOTE' ? (m.content || '[报价]') : mt === 'ATTACHMENT' ? '[附件]' : (m.content || ''),
    payloadJson: m.payloadJson,
    time: formatTime(m.createTime),
    status: 'sent'
  }
}

function onWsSent(tempId?: string, id?: number) {
  if (!tempId) return
  const idx = messages.value.findIndex(m => m.id === tempId)
  if (idx < 0) return
  const m = messages.value[idx]
  if (!m) return
  m.id = id ?? m.id
  m.status = 'sent'
}

async function loadMessages() {
  if (!props.conversationId) return
  loading.value = true
  try {
    const res = await getConversationMessages(props.conversationId, 100)
    if (res.code !== 0) throw new Error(res.message)
    messages.value = (res.data || []).map(mapApiMessageToUi)
    await nextTick()
    scrollToBottom()
    try {
      await markConversationRead(props.conversationId)
    } catch {
      // ignore
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载消息失败')
  } finally {
    loading.value = false
  }
}

function scrollToBottom() {
  const el = document.getElementById('chat-drawer-scroll')
  if (!el) return
  el.scrollTop = el.scrollHeight
}

async function sendMessage() {
  if (!props.conversationId) return
  if (!messageInput.value.trim()) return

  if (!websocket.ensureConnected()) {
    ElMessage.warning('实时连接未就绪，正在重连…')
    return
  }

  const content = messageInput.value.trim()
  const tempId = `t_${Date.now()}_${Math.random().toString(16).slice(2)}`
  messageInput.value = ''

  // 乐观更新
  messages.value.push({
    id: tempId,
    type: 'sent',
    msgType: 'TEXT',
    content,
    status: 'pending',
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  await nextTick()
  scrollToBottom()

  // 使用 composable 发送
  const sent = websocket.sendText(props.conversationId, content, tempId)
  if (!sent) {
    ElMessage.error('发送失败')
  }
}

function goToChatCenter() {
  if (!props.conversationId) return
  router.push({ path: '/chat', query: { conversationId: String(props.conversationId) } })
}

function close() {
  emit('update:modelValue', false)
  emit('closed')
}

// 抽屉打开时连接 WebSocket
watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      websocket.connect()
    }
  },
  { immediate: true }
)

// 会话切换时加载消息
watch(
  () => props.conversationId,
  async (id) => {
    if (!id) {
      messages.value = []
      return
    }
    if (props.modelValue) await loadMessages()
  },
  { immediate: true }
)

// 抽屉打开且有会话时加载消息
watch(
  () => props.modelValue,
  async (open) => {
    if (open && props.conversationId) await loadMessages()
  }
)

// 注意：WebSocket 清理由 composable 的 onBeforeUnmount 自动处理
</script>

<template>
  <ElDrawer
    :model-value="modelValue"
    direction="rtl"
    size="520px"
    :with-header="false"
    :append-to-body="true"
    :lock-scroll="true"
    :close-on-click-modal="true"
    :destroy-on-close="false"
    :modal-class="'chat-drawer-mask'"
    @close="close"
  >
    <div class="h-full flex flex-col bg-gray-50">
      <!-- header simplified -->
      <div class="px-5 py-3 bg-white border-b border-gray-200 flex items-center justify-between gap-4">
        <div class="min-w-0">
          <div class="font-bold text-gray-900 truncate leading-tight">{{ title }}</div>
          <div class="text-[10px] text-gray-400 mt-0.5">
            {{ websocket.isConnected.value ? '● 在线' : '连接中…' }}
          </div>
        </div>
        <div class="flex items-center gap-2 shrink-0">
          <button
            class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-900 text-white text-xs font-bold hover:bg-slate-800 transition-all "
            @click="goToChatCenter"
          >
            转入沟通中心
            <ArrowUpRight class="w-3.5 h-3.5" />
          </button>
          <button class="p-1.5 rounded-lg hover:bg-gray-100 transition-all " @click="close">
            <X class="w-5 h-5 text-gray-500" />
          </button>
        </div>
      </div>

      <!-- 简化的标的摘要 -->
      <div v-if="subjectSnapshotJson" class="px-5 py-2 bg-gray-100 border-b border-gray-200 text-xs text-gray-600 flex items-center gap-2">
        <span class="inline-block w-1.5 h-1.5 rounded-full" :class="subjectType === 'SUPPLY' ? 'bg-brand-500' : 'bg-autumn-500'"></span>
        <span class="truncate">{{ subjectSummary }}</span>
      </div>

      <!-- messages -->
      <div id="chat-drawer-scroll" class="flex-1 overflow-y-auto px-5 py-4 space-y-6" v-loading="loading">
        <template v-if="conversationId">
          <div v-for="(m, idx) in messages" :key="m.id" class="flex flex-col">
            <!-- 简化时间显示 -->
            <div v-if="idx === 0 || m.time !== messages[idx-1]?.time" class="self-center my-2">
              <span class="text-[10px] text-gray-400">{{ m.time }}</span>
            </div>

            <div class="flex" :class="m.type === 'sent' ? 'justify-end' : m.type === 'system' ? 'justify-center' : 'justify-start'">
              <div v-if="m.type === 'system'" class="px-4 py-1.5 bg-gray-200/80 text-gray-500 text-[10px] rounded-full">
                {{ m.content }}
              </div>
              <div v-else-if="m.type === 'received'" class="max-w-[85%]">
                <!-- 报价消息简化为链接 -->
                <div v-if="(m.msgType || '').toUpperCase() === 'QUOTE'"
                     class="bg-white rounded-lg rounded-tl-sm px-4 py-2.5 border border-gray-200 shadow-sm cursor-pointer hover:bg-gray-50 transition-all"
                     @click="goToChatCenter">
                  <div class="flex items-center gap-2 text-sm text-brand-600">
                    <MessageCircle class="w-4 h-4" />
                    <span class="font-medium">收到报价单</span>
                    <span class="text-gray-400 text-xs">点击查看详情 →</span>
                  </div>
                </div>
                <div v-else class="bg-white rounded-lg rounded-tl-sm px-4 py-2.5 border border-gray-200 shadow-sm text-sm text-gray-800">
                  {{ m.content }}
                </div>
              </div>
              <div v-else class="max-w-[85%] flex flex-col items-end">
                <!-- 我发送的报价消息简化显示 -->
                <div v-if="(m.msgType || '').toUpperCase() === 'QUOTE'"
                     class="bg-brand-600 text-white rounded-lg rounded-tr-sm px-4 py-2.5 shadow-sm cursor-pointer hover:bg-brand-700 transition-all"
                     @click="goToChatCenter">
                  <div class="flex items-center gap-2 text-sm">
                    <MessageCircle class="w-4 h-4" />
                    <span class="font-medium">已发送报价单</span>
                    <span class="text-brand-200 text-xs">点击查看 →</span>
                  </div>
                </div>
                <div v-else class="bg-brand-600 text-white rounded-lg rounded-tr-sm px-4 py-2.5 shadow-sm text-sm">
                  {{ m.content }}
                </div>
                <div v-if="m.status === 'pending'" class="text-[10px] text-gray-400 mt-1">发送中…</div>
              </div>
            </div>
          </div>
        </template>
        <div v-else class="py-20 text-center text-gray-400">
          暂无会话
        </div>
      </div>

      <!-- 简化的输入区域 -->
      <div class="p-4 bg-white border-t border-gray-200">
        <div class="flex items-end gap-3">
          <textarea
            v-model="messageInput"
            rows="1"
            placeholder="输入消息…"
            class="flex-1 resize-none border-2 border-gray-200 rounded-lg px-3 py-2 text-sm outline-none focus:border-brand-500 transition-all"
            @keydown.enter.prevent="sendMessage"
          />
          <button
            class="px-4 py-2 rounded-lg bg-brand-600 text-white font-bold hover:bg-brand-700 transition-all disabled:opacity-50"
            :disabled="!messageInput.trim() || !conversationId"
            @click="sendMessage"
          >
            发送
          </button>
        </div>
        <!-- 引导提示 -->
        <div class="mt-3 text-center">
          <button
            class="text-xs text-gray-400 hover:text-brand-600 transition-all"
            @click="goToChatCenter"
          >
            💡 需要发送报价、图片或附件？<span class="font-medium text-brand-600">转入沟通中心</span>
          </button>
        </div>
      </div>
    </div>
  </ElDrawer>
</template>

<style>
.chat-drawer-mask {
  background: rgba(15, 23, 42, 0.6) !important; /* slate-900/60 */
  backdrop-filter: blur(6px);
}
</style>


