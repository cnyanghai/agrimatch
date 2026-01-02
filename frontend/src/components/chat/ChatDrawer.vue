<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { ElDrawer, ElMessage } from 'element-plus'
import { ArrowUpRight, X } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import ChatSubjectCard from './ChatSubjectCard.vue'
import NegotiationPanel, { type QuoteFields } from './NegotiationPanel.vue'
import { getConversationMessages, markConversationRead, type ChatMessageResponse } from '../../api/chat'
import { useAuthStore } from '../../store/auth'
import { buildChatWsUrl } from '../../utils/chatWs'

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

type UiMessage = {
  id: string | number
  type: 'system' | 'received' | 'sent'
  msgType?: string
  content: string
  payloadJson?: string
  time?: string
  status?: 'pending' | 'sent'
}

const loading = ref(false)
const messageInput = ref('')
const messages = ref<UiMessage[]>([])
const ws = ref<WebSocket | null>(null)
const wsConnected = ref(false)
let wsReconnectTimer: number | null = null
let wsReconnectAttempt = 0
const wsCloseHinted = ref(false)
const canRealtime = computed(() => !!auth.me || !!auth.token)

function cleanupWs() {
  if (wsReconnectTimer) {
    window.clearTimeout(wsReconnectTimer)
    wsReconnectTimer = null
  }
  wsReconnectAttempt = 0
  wsConnected.value = false
  wsCloseHinted.value = false
  if (ws.value) {
    try {
      ws.value.close()
    } catch {
      // ignore
    }
    ws.value = null
  }
}

const title = computed(() => props.peerDisplayName || '沟通')

function formatTime(ts?: string) {
  if (!ts) return ''
  const d = new Date(ts)
  if (Number.isNaN(d.getTime())) return ts
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function wsUrl() {
  return buildChatWsUrl(auth.token || undefined)
}

function connectWs() {
  try {
    if (!canRealtime.value) return
    if (ws.value && (ws.value.readyState === WebSocket.OPEN || ws.value.readyState === WebSocket.CONNECTING)) return
    const socket = new WebSocket(wsUrl())
    ws.value = socket

    socket.onopen = () => {
      wsConnected.value = true
      wsReconnectAttempt = 0
      wsCloseHinted.value = false
    }
    socket.onclose = (ev: CloseEvent) => {
      wsConnected.value = false
      // 已退出/未登录：不提示、不重连（避免“不断刷新/断开”）
      if (!canRealtime.value) return
      if (!wsCloseHinted.value) {
        wsCloseHinted.value = true
        const reason = (ev?.reason || '').trim()
        const code = ev?.code
        if (reason) {
          ElMessage.warning(`实时连接已断开：${reason}`)
        } else if (code && code !== 1000) {
          ElMessage.warning(`实时连接已断开（code=${code}），正在重连…`)
        }
      }
      scheduleReconnect()
    }
    socket.onerror = () => {
      wsConnected.value = false
    }
    socket.onmessage = (ev) => {
      try {
        const payload = JSON.parse(ev.data)
        if (payload?.type === 'MESSAGE' && payload?.message) {
          const cid = Number(payload.conversationId)
          if (!props.conversationId || cid !== props.conversationId) return
          messages.value.push(mapApiMessageToUi(payload.message as ChatMessageResponse))
          nextTick().then(scrollToBottom)
          return
        }
        if (payload?.type === 'SENT') {
          const cid = Number(payload.conversationId)
          if (!props.conversationId || cid !== props.conversationId) return
          onWsSent(payload?.tempId, payload?.id)
        }
      } catch {
        // ignore
      }
    }
  } catch {
    scheduleReconnect()
  }
}

function scheduleReconnect() {
  if (wsReconnectTimer) return
  if (!canRealtime.value) return
  wsReconnectAttempt += 1
  const delay = Math.min(8000, 500 * wsReconnectAttempt)
  wsReconnectTimer = window.setTimeout(() => {
    wsReconnectTimer = null
    connectWs()
  }, delay)
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

function parseQuoteFields(payloadJson?: string): QuoteFields | null {
  if (!payloadJson) return null
  try {
    const obj = JSON.parse(payloadJson)
    if (obj?.kind === 'QUOTE_V1' && obj?.fields) return obj.fields as QuoteFields
    if (obj?.fields) return obj.fields as QuoteFields
    return obj as QuoteFields
  } catch {
    return null
  }
}

const peerLatestQuote = computed<QuoteFields | null>(() => {
  for (let i = messages.value.length - 1; i >= 0; i--) {
    const m = messages.value[i]
    if (!m) continue
    if (m.type !== 'received') continue
    if ((m.msgType || '').toUpperCase() !== 'QUOTE') continue
    return parseQuoteFields(m.payloadJson) || null
  }
  return null
})

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
  if (!ws.value || ws.value.readyState !== WebSocket.OPEN) {
    ElMessage.warning('实时连接未就绪，正在重连…')
    connectWs()
    return
  }
  const content = messageInput.value.trim()
  const tempId = `t_${Date.now()}_${Math.random().toString(16).slice(2)}`
  messageInput.value = ''

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

  ws.value.send(JSON.stringify({
    type: 'SEND',
    conversationId: props.conversationId,
    msgType: 'TEXT',
    content,
    tempId
  }))
}

async function sendQuote(payload: any, summary: string) {
  if (!props.conversationId) return
  if (!ws.value || ws.value.readyState !== WebSocket.OPEN) {
    ElMessage.warning('实时连接未就绪，正在重连…')
    connectWs()
    return
  }

  const tempId = `q_${Date.now()}_${Math.random().toString(16).slice(2)}`
  messages.value.push({
    id: tempId,
    type: 'sent',
    msgType: 'QUOTE',
    content: summary || '[报价]',
    payloadJson: JSON.stringify(payload),
    status: 'pending',
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  await nextTick()
  scrollToBottom()

  ws.value.send(JSON.stringify({
    type: 'SEND',
    conversationId: props.conversationId,
    msgType: 'QUOTE',
    content: summary || '',
    payload,
    tempId
  }))
}

function goToChatCenter() {
  if (!props.conversationId) return
  router.push({ path: '/chat', query: { conversationId: String(props.conversationId) } })
}

function close() {
  emit('update:modelValue', false)
  emit('closed')
}

watch(
  () => props.modelValue,
  (open) => {
    if (open) connectWs()
  },
  { immediate: true }
)

watch(canRealtime, (ok) => {
  if (!ok) cleanupWs()
  else if (props.modelValue) connectWs()
})

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

watch(
  () => props.modelValue,
  async (open) => {
    if (open && props.conversationId) await loadMessages()
  }
)

onBeforeUnmount(() => {
  cleanupWs()
})
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
      <!-- header -->
      <div class="px-5 py-4 bg-white border-b border-gray-100 flex items-center justify-between gap-4">
        <div class="min-w-0">
          <div class="font-bold text-gray-900 truncate">{{ title }}</div>
          <div class="text-xs text-gray-400 flex items-center gap-2">
            <span>{{ wsConnected ? '实时连接已就绪' : '连接中…' }}</span>
            <span class="w-1 h-1 rounded-full bg-gray-300"></span>
            <span class="truncate">会话 #{{ conversationId ?? '-' }}</span>
          </div>
        </div>
        <div class="flex items-center gap-2 shrink-0">
          <button
            class="inline-flex items-center gap-2 px-3 py-2 rounded-xl bg-slate-900 text-white text-sm font-bold hover:bg-slate-800 transition-all active:scale-95"
            @click="goToChatCenter"
          >
            转入沟通中心
            <ArrowUpRight class="w-4 h-4" />
          </button>
          <button class="p-2 rounded-xl hover:bg-gray-100 transition-all active:scale-95" @click="close" aria-label="close">
            <X class="w-5 h-5 text-gray-500" />
          </button>
        </div>
      </div>

      <!-- subject -->
      <div class="p-5">
        <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-2">本次咨询标的</div>
        <ChatSubjectCard
          :subject-type="subjectType"
          :subject-id="subjectId"
          :subject-snapshot-json="subjectSnapshotJson"
        />

        <div class="mt-4">
          <NegotiationPanel
            :disabled="!conversationId"
            :peer-latest-quote="peerLatestQuote"
            @send="({ payload, summary }) => sendQuote(payload, summary)"
          />
        </div>
      </div>

      <!-- messages -->
      <div id="chat-drawer-scroll" class="flex-1 overflow-y-auto px-5 pb-4 space-y-4" v-loading="loading">
        <template v-if="conversationId">
          <div v-for="m in messages" :key="m.id" class="flex" :class="m.type === 'sent' ? 'justify-end' : m.type === 'system' ? 'justify-center' : 'justify-start'">
            <div v-if="m.type === 'system'" class="px-4 py-1.5 bg-gray-200 text-gray-500 text-xs rounded-full">
              {{ m.content }}
            </div>
            <div v-else-if="m.type === 'received'" class="max-w-[78%]">
              <div v-if="(m.msgType || '').toUpperCase() === 'QUOTE'" class="bg-white rounded-2xl rounded-tl-sm px-4 py-3 border border-gray-100">
                <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">报价卡</div>
                <div class="mt-1 font-bold text-gray-900">{{ m.content || '[报价]' }}</div>
                <div class="mt-3 grid grid-cols-2 gap-2 text-sm" v-if="parseQuoteFields(m.payloadJson)">
                  <div class="text-gray-700 truncate" v-for="(v, k) in (parseQuoteFields(m.payloadJson) as any)" :key="k">
                    <span class="text-xs text-gray-500">{{ k }}：</span>
                    <span class="font-semibold">{{ v || '-' }}</span>
                  </div>
                </div>
              </div>
              <div v-else class="bg-white rounded-2xl rounded-tl-sm px-4 py-3 border border-gray-100">
                <div class="text-gray-800">{{ m.content }}</div>
              </div>
              <div class="text-xs text-gray-400 mt-1 ml-1">{{ m.time }}</div>
            </div>
            <div v-else class="max-w-[78%]">
              <div v-if="(m.msgType || '').toUpperCase() === 'QUOTE'" class="bg-emerald-600 text-white rounded-2xl rounded-tr-sm px-4 py-3 shadow-sm">
                <div class="text-[10px] font-bold uppercase tracking-widest text-emerald-100">报价卡</div>
                <div class="mt-1 font-bold">{{ m.content || '[报价]' }}</div>
                <div class="mt-3 grid grid-cols-2 gap-2 text-sm text-emerald-50" v-if="parseQuoteFields(m.payloadJson)">
                  <div class="truncate" v-for="(v, k) in (parseQuoteFields(m.payloadJson) as any)" :key="k">
                    <span class="text-emerald-100">{{ k }}：</span>
                    <span class="font-semibold">{{ v || '-' }}</span>
                  </div>
                </div>
              </div>
              <div v-else class="bg-emerald-600 text-white rounded-2xl rounded-tr-sm px-4 py-3 shadow-sm">
                <div>{{ m.content }}</div>
              </div>
              <div class="text-xs text-gray-400 mt-1 mr-1 text-right">
                <span v-if="m.status === 'pending'">发送中…</span>
                <span v-else>{{ m.time }}</span>
              </div>
            </div>
          </div>
        </template>
        <div v-else class="py-20 text-center text-gray-400">
          暂无会话
        </div>
      </div>

      <!-- composer -->
      <div class="p-4 bg-white border-t border-gray-100">
        <div class="flex items-end gap-3">
          <textarea
            v-model="messageInput"
            rows="2"
            placeholder="输入消息…"
            class="flex-1 resize-none border-2 border-gray-100 rounded-xl px-3 py-2 text-sm outline-none focus:border-emerald-500 transition-all"
            @keydown.enter.prevent="sendMessage"
          />
          <button
            class="px-4 py-2.5 rounded-xl bg-emerald-600 text-white font-bold hover:bg-emerald-700 transition-all active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="!messageInput.trim() || !conversationId"
            @click="sendMessage"
          >
            发送
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


