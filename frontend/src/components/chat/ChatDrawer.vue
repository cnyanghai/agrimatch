<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { ElDrawer, ElMessage } from 'element-plus'
import { ArrowUpRight, X } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import ChatSubjectCard from './ChatSubjectCard.vue'
import NegotiationPanel, { type QuoteFields } from './NegotiationPanel.vue'
import { getConversationMessages, markConversationRead, type ChatMessageResponse } from '../../api/chat'
import { useAuthStore } from '../../store/auth'

// 使用统一的类型和工具函数
import type { UiMessage } from '../../types/chat'
import { QUOTE_STATUS_BADGE } from '../../types/chat'
import { parseQuotePayload, getQuoteDisplayFields } from '../../utils/chat/quoteParser'

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
const quotePopoverVisible = ref(false)

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

// 使用新的统一报价解析器（兼容旧接口）
function parseQuoteFields(payloadJson?: string): QuoteFields | null {
  const payload = parseQuotePayload(payloadJson)
  if (!payload) return null
  return payload.fields as QuoteFields
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

// 使用统一的报价状态样式
function quoteStatusBadge(status?: string) {
  if (!status) return null
  return QUOTE_STATUS_BADGE[status as keyof typeof QUOTE_STATUS_BADGE] || null
}

// QUOTE_LABEL_MAP 和 getQuoteDisplayFields 现在从 utils/chat/quoteParser 导入

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

async function sendQuote(payload: any, summary: string) {
  if (!props.conversationId) return
  quotePopoverVisible.value = false

  if (!websocket.ensureConnected()) {
    ElMessage.warning('实时连接未就绪，正在重连…')
    return
  }

  const tempId = `q_${Date.now()}_${Math.random().toString(16).slice(2)}`

  // 乐观更新
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

  // 使用 composable 发送
  const sent = websocket.sendQuote(
    props.conversationId,
    JSON.stringify(payload),
    summary || '',
    tempId
  )
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
            <div v-if="idx === 0 || m.time !== messages[idx-1]?.time" class="self-center my-2">
              <span class="text-[10px] text-gray-400">{{ m.time }}</span>
            </div>

            <div class="flex" :class="m.type === 'sent' ? 'justify-end' : m.type === 'system' ? 'justify-center' : 'justify-start'">
              <div v-if="m.type === 'system'" class="px-4 py-1.5 bg-gray-200/80 text-gray-500 text-[10px] rounded-full">
                {{ m.content }}
              </div>
              <div v-else-if="m.type === 'received'" class="max-w-[85%]">
                <div v-if="(m.msgType || '').toUpperCase() === 'QUOTE'" class="bg-white rounded-lg rounded-tl-sm px-4 py-3 border border-gray-200 shadow-sm">
                  <div class="flex items-center justify-between mb-2">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">电子报价单</div>
                    <div v-if="quoteStatusBadge(m.quoteStatus)"
                         :class="['text-[10px] px-1.5 py-0.5 rounded-full border font-bold', quoteStatusBadge(m.quoteStatus)?.bgColor, quoteStatusBadge(m.quoteStatus)?.color]">
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
                <div v-else class="bg-white rounded-lg rounded-tl-sm px-4 py-2.5 border border-gray-200 shadow-sm text-sm text-gray-800">
                  {{ m.content }}
                </div>
              </div>
              <div v-else class="max-w-[85%] flex flex-col items-end">
                <div v-if="(m.msgType || '').toUpperCase() === 'QUOTE'" class="bg-brand-600 text-white rounded-lg rounded-tr-sm px-4 py-3 shadow-sm">
                  <div class="flex items-center justify-between mb-2 gap-4">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-brand-100">电子报价单</div>
                    <div v-if="quoteStatusBadge(m.quoteStatus)" 
                         class="text-[10px] px-1.5 py-0.5 rounded-full border border-brand-400 bg-brand-500/50 font-bold text-white">
                      {{ quoteStatusBadge(m.quoteStatus)?.label }}
                    </div>
                  </div>
                  <div class="mt-1 font-bold text-sm border-b border-brand-500 pb-2 mb-3">{{ m.content || '[报价]' }}</div>
                  <div class="space-y-2">
                    <div v-for="field in getQuoteDisplayFields(m.payloadJson)" :key="field.label" class="flex justify-between text-[11px]">
                      <span class="text-brand-100/80">{{ field.label }}</span>
                      <span class="text-white font-medium">{{ field.value }}</span>
                    </div>
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

      <!-- composer simplified -->
      <div class="p-4 bg-white border-t border-gray-200">
        <div class="flex items-center gap-4 mb-3">
          <el-popover placement="top-start" :width="600" trigger="click" v-model:visible="quotePopoverVisible" popper-class="!p-0 !rounded-2xl !border-none !shadow-2xl">
            <template #reference>
              <button class="text-xs font-bold text-brand-600">修改价格/报价</button>
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
            class="flex-1 resize-none border-2 border-gray-200 rounded-lg px-3 py-2 text-sm outline-none focus:border-brand-500 transition-all"
            @keydown.enter.prevent="sendMessage"
          />
          <button
            class="px-4 py-2 rounded-lg bg-brand-600 text-white font-bold hover:bg-brand-700 transition-all  disabled:opacity-50"
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


