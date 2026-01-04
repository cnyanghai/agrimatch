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
const quotePopoverVisible = ref(false)
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

function quoteStatusBadge(status?: string) {
  if (status === 'OFFERED') return { label: '待确认', cls: 'bg-blue-50 text-blue-600 border-blue-100' }
  if (status === 'ACCEPTED') return { label: '已达成', cls: 'bg-green-50 text-green-600 border-green-100' }
  if (status === 'EXPIRED') return { label: '已失效', cls: 'bg-gray-50 text-gray-400 border-gray-100' }
  return null
}

const QUOTE_LABEL_MAP: Record<string, string> = {
  price: '单价(元/吨)',
  quantity: '数量',
  deliveryMethod: '交货方式',
  deliveryPlace: '交付地',
  arrivalDate: '到货期',
  paymentMethod: '结算方式',
  invoiceType: '发票类型',
  packaging: '包装方式',
  validUntil: '有效期',
  remark: '备注'
}

function getQuoteDisplayFields(payloadJson?: string) {
  const fields = parseQuoteFields(payloadJson)
  if (!fields) return []
  const display: Array<{ label: string; value: any }> = []
  
  // 基础字段
  Object.entries(fields)
    .filter(([k, v]) => v && QUOTE_LABEL_MAP[k])
    .forEach(([k, v]) => {
      display.push({ label: QUOTE_LABEL_MAP[k], value: v })
    })

  // 动态字段
  const dynamic = fields.dynamicParams || {}
  Object.entries(dynamic).forEach(([k, v]) => {
    if (v) display.push({ label: k, value: v })
  })

  return display
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
  quotePopoverVisible.value = false
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
      <!-- header simplified -->
      <div class="px-5 py-3 bg-white border-b border-gray-100 flex items-center justify-between gap-4">
        <div class="min-w-0">
          <div class="font-bold text-gray-900 truncate leading-tight">{{ title }}</div>
          <div class="text-[10px] text-gray-400 mt-0.5">
            {{ wsConnected ? '● 在线' : '连接中…' }}
          </div>
        </div>
        <div class="flex items-center gap-2 shrink-0">
          <button
            class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-900 text-white text-xs font-bold hover:bg-slate-800 transition-all active:scale-95"
            @click="goToChatCenter"
          >
            转入沟通中心
            <ArrowUpRight class="w-3.5 h-3.5" />
          </button>
          <button class="p-1.5 rounded-xl hover:bg-gray-100 transition-all active:scale-95" @click="close">
            <X class="w-5 h-5 text-gray-500" />
          </button>
        </div>
      </div>

      <!-- 常驻标的摘要 -->
      <ChatSubjectCard
        is-mini
        class="sticky top-0 z-10"
        :subject-type="subjectType"
        :subject-id="subjectId"
        :subject-snapshot-json="subjectSnapshotJson"
      />

      <!-- messages -->
      <div id="chat-drawer-scroll" class="flex-1 overflow-y-auto px-5 py-4 space-y-6" v-loading="loading">
        <template v-if="conversationId">
          <div v-for="(m, idx) in messages" :key="m.id" class="flex flex-col">
            <!-- 简化时间显示 -->
            <div v-if="idx === 0 || m.time !== messages[idx-1].time" class="self-center my-2">
              <span class="text-[10px] text-gray-400">{{ m.time }}</span>
            </div>

            <div class="flex" :class="m.type === 'sent' ? 'justify-end' : m.type === 'system' ? 'justify-center' : 'justify-start'">
              <div v-if="m.type === 'system'" class="px-4 py-1.5 bg-gray-200/80 text-gray-500 text-[10px] rounded-full">
                {{ m.content }}
              </div>
              <div v-else-if="m.type === 'received'" class="max-w-[85%]">
                <div v-if="(m.msgType || '').toUpperCase() === 'QUOTE'" class="bg-white rounded-2xl rounded-tl-sm px-4 py-3 border border-gray-100 shadow-sm">
                  <div class="flex items-center justify-between mb-2">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">电子报价单</div>
                    <div v-if="quoteStatusBadge(m.quoteStatus)" 
                         :class="['text-[10px] px-1.5 py-0.5 rounded-full border font-bold', quoteStatusBadge(m.quoteStatus)?.cls]">
                      {{ quoteStatusBadge(m.quoteStatus)?.label }}
                    </div>
                  </div>
                  <div class="mt-1 font-bold text-gray-900 text-sm border-b pb-2 mb-3">{{ m.content || '[报价]' }}</div>
                  <div class="space-y-2">
                    <div v-for="field in getQuoteDisplayFields(m.payloadJson)" :key="field.label" class="flex justify-between text-[11px]">
                      <span class="text-gray-400">{{ field.label }}</span>
                      <span class="text-gray-700 font-medium">{{ field.value }}</span>
                    </div>
                  </div>
                </div>
                <div v-else class="bg-white rounded-2xl rounded-tl-sm px-4 py-2.5 border border-gray-100 shadow-sm text-sm text-gray-800">
                  {{ m.content }}
                </div>
              </div>
              <div v-else class="max-w-[85%] flex flex-col items-end">
                <div v-if="(m.msgType || '').toUpperCase() === 'QUOTE'" class="bg-emerald-600 text-white rounded-2xl rounded-tr-sm px-4 py-3 shadow-sm">
                  <div class="flex items-center justify-between mb-2 gap-4">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-emerald-100">电子报价单</div>
                    <div v-if="quoteStatusBadge(m.quoteStatus)" 
                         class="text-[10px] px-1.5 py-0.5 rounded-full border border-emerald-400 bg-emerald-500/50 font-bold text-white">
                      {{ quoteStatusBadge(m.quoteStatus)?.label }}
                    </div>
                  </div>
                  <div class="mt-1 font-bold text-sm border-b border-emerald-500 pb-2 mb-3">{{ m.content || '[报价]' }}</div>
                  <div class="space-y-2">
                    <div v-for="field in getQuoteDisplayFields(m.payloadJson)" :key="field.label" class="flex justify-between text-[11px]">
                      <span class="text-emerald-100/80">{{ field.label }}</span>
                      <span class="text-white font-medium">{{ field.value }}</span>
                    </div>
                  </div>
                </div>
                <div v-else class="bg-emerald-600 text-white rounded-2xl rounded-tr-sm px-4 py-2.5 shadow-sm text-sm">
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

      <!-- composer simplified -->
      <div class="p-4 bg-white border-t border-gray-100">
        <div class="flex items-center gap-4 mb-3">
          <el-popover placement="top-start" :width="600" trigger="click" v-model:visible="quotePopoverVisible" popper-class="!p-0 !rounded-[32px] !border-none !shadow-2xl">
            <template #reference>
              <button class="text-xs font-bold text-emerald-600">修改价格/报价</button>
            </template>
            <NegotiationPanel
              :disabled="!conversationId"
              :peer-latest-quote="peerLatestQuote"
              :subject-snapshot-json="subjectSnapshotJson"
              @send="({ payload, summary }) => sendQuote(payload, summary)"
            />
          </el-popover>
          <button class="text-xs font-bold text-gray-400">图片</button>
          <button class="text-xs font-bold text-gray-400">附件</button>
        </div>
        <div class="flex items-end gap-3">
          <textarea
            v-model="messageInput"
            rows="1"
            placeholder="输入消息…"
            class="flex-1 resize-none border-2 border-gray-100 rounded-xl px-3 py-2 text-sm outline-none focus:border-emerald-500 transition-all"
            @keydown.enter.prevent="sendMessage"
          />
          <button
            class="px-4 py-2 rounded-xl bg-emerald-600 text-white font-bold hover:bg-emerald-700 transition-all active:scale-95 disabled:opacity-50"
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


