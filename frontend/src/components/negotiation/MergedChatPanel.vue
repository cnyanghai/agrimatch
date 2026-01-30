<script setup lang="ts">
/**
 * MergedChatPanel - 合并聊天面板
 * 将同一商户所有产品会话的消息按时间混排为一条连续对话流
 * 顶部显示产品 chips 可切换当前议价上下文（影响左侧表单和右侧合同预览）
 */
import { ref, nextTick, watch, onMounted, computed } from 'vue'
import { Send, Paperclip, Gift, FileText, X, Download, Loader2, CheckCircle, Info, AlertCircle, FileSignature, ExternalLink, Package } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { formatMessageTime, shouldShowTimeSeparator, type UiMessage } from '../../types/chat/message'
import QuoteCard from '../chat/message/QuoteCard.vue'
import { uploadImage, uploadAttachment, isImageFile, formatFileSize, type FileUploadResponse } from '../../api/file'
import type { ConversationItem } from '../../composables/useNegotiationWorkspace'

const props = defineProps<{
  /** 当前商户的所有会话 */
  conversations: ConversationItem[]
  /** 合并后的消息时间线（所有会话按时间排序） */
  mergedMessages: UiMessage[]
  /** 当前激活的会话 ID */
  activeConversationId: number | null
  /** 当前激活的产品名称 */
  activeProductName: string
  peerName: string
  peerAvatar?: string
  peerCompany?: string
  peerUserId?: number
  wsConnected: boolean
  sending?: boolean
}>()

const emit = defineEmits<{
  (e: 'send', text: string): void
  (e: 'send-image', payload: FileUploadResponse): void
  (e: 'send-attachment', payload: FileUploadResponse): void
  (e: 'accept-quote', message: UiMessage): void
  (e: 'counter-quote', message: UiMessage, payload: { price?: number; basisPrice?: number; quantity?: string; remark?: string }): void
  (e: 'reject-quote', message: UiMessage): void
  (e: 'draft-contract', message: UiMessage): void
  (e: 'gift-points', toUserId: number, points: number, remark?: string): void
  (e: 'activate-conversation', convId: number): void
}>()

const router = useRouter()
const messageInput = ref('')
const chatContainerRef = ref<HTMLElement | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const uploadProgress = ref(0)

// 赠送积分弹窗
const showGiftPointsDialog = ref(false)
const giftPointsAmount = ref(10)
const giftPointsRemark = ref('')
const giftingPoints = ref(false)

/** 会话信息映射: convId → { name, type } */
const convInfoMap = computed(() => {
  const map = new Map<number, { name: string; type: string }>()
  for (const conv of props.conversations) {
    let name = '产品'
    if (conv.subjectSnapshotJson) {
      try {
        const s = JSON.parse(conv.subjectSnapshotJson)
        name = s.productName || s.title || '产品'
      } catch { /* ignore */ }
    }
    map.set(conv.id, { name, type: conv.subjectType || '' })
  }
  return map
})

/** 根据 conversationId 获取产品名 */
function getProductNameByConvId(convId?: number): string {
  if (!convId) return '产品'
  return convInfoMap.value.get(convId)?.name || '产品'
}

/** 根据 conversationId 获取类型标签 */
function getProductTypeByConvId(convId?: number): string {
  if (!convId) return ''
  const type = convInfoMap.value.get(convId)?.type
  return type === 'SUPPLY' ? '供应' : type === 'DEMAND' ? '需求' : ''
}

/** 判断是否需要在该消息前显示产品标签 */
function shouldShowProductTag(index: number): boolean {
  const msg = props.mergedMessages[index]
  if (!msg?.conversationId) return false
  if (index === 0) return true
  const prevMsg = props.mergedMessages[index - 1]
  return msg.conversationId !== prevMsg?.conversationId
}

// ==================== 消息渲染辅助 ====================

function needsTimeSeparator(index: number): boolean {
  if (index === 0) return true
  const curr = props.mergedMessages[index]
  const prev = props.mergedMessages[index - 1]
  return shouldShowTimeSeparator(prev?.timestamp, curr?.timestamp)
}

function isLastSentMessage(index: number): boolean {
  const msg = props.mergedMessages[index]
  if (!msg || msg.type !== 'sent') return false
  for (let i = props.mergedMessages.length - 1; i >= 0; i--) {
    if (props.mergedMessages[i]?.type === 'sent') return i === index
  }
  return false
}

function getPreviousQuotePayload(index: number): string | undefined {
  for (let i = index - 1; i >= 0; i--) {
    const msg = props.mergedMessages[i]
    if (msg && (msg.msgType === 'QUOTE' || msg.payloadJson)) return msg.payloadJson
  }
  return undefined
}

function scrollToBottom() {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}

function handleSend() {
  const text = messageInput.value.trim()
  if (!text) return
  emit('send', text)
  messageInput.value = ''
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

function getAvatarChar(name?: string): string {
  if (!name || name.length === 0) return '?'
  return (name[0] || '?').toUpperCase()
}

function isQuoteMessage(msg: UiMessage): boolean {
  return msg.msgType === 'QUOTE' || (!!msg.payloadJson && msg.payloadJson.length > 10)
}
function isImageMessage(msg: UiMessage): boolean { return msg.msgType === 'IMAGE' }
function isAttachmentMessage(msg: UiMessage): boolean { return msg.msgType === 'ATTACHMENT' }
function isContractMessage(msg: UiMessage): boolean { return msg.msgType === 'CONTRACT' }

interface ContractMessagePayload {
  contractId?: number
  contractNo?: string
  productName?: string
  totalAmount?: number
  status?: number
}
function parseContractPayload(msg: UiMessage): ContractMessagePayload | null {
  if (!msg.payloadJson) return null
  try { return JSON.parse(msg.payloadJson) } catch { return null }
}
function goToContract(contractId?: number) {
  if (contractId) router.push(`/contracts/${contractId}`)
}

function isSystemActionMessage(msg: UiMessage): boolean {
  return msg.msgType === 'SYSTEM' && !!msg.payloadJson
}
interface SystemMessagePayload { action?: string; role?: 'buyer' | 'seller'; confirmedAt?: string }
function parseSystemPayload(msg: UiMessage): SystemMessagePayload | null {
  if (!msg.payloadJson) return null
  try { return JSON.parse(msg.payloadJson) } catch { return null }
}
function getSystemMessageStyle(payload: SystemMessagePayload | null) {
  if (!payload) return { icon: 'info' as const, bgColor: 'bg-gray-50', textColor: 'text-gray-600', borderColor: 'border-gray-200' }
  switch (payload.action) {
    case 'CONFIRM_TERMS': return { icon: 'check' as const, bgColor: 'bg-brand-50', textColor: 'text-brand-700', borderColor: 'border-brand-200' }
    case 'CONTRACT_CREATED': return { icon: 'check' as const, bgColor: 'bg-blue-50', textColor: 'text-blue-700', borderColor: 'border-blue-200' }
    case 'QUOTE_ACCEPTED': return { icon: 'check' as const, bgColor: 'bg-green-50', textColor: 'text-green-700', borderColor: 'border-green-200' }
    case 'QUOTE_REJECTED': return { icon: 'alert' as const, bgColor: 'bg-red-50', textColor: 'text-red-600', borderColor: 'border-red-200' }
    default: return { icon: 'info' as const, bgColor: 'bg-gray-50', textColor: 'text-gray-600', borderColor: 'border-gray-200' }
  }
}

function parseFilePayload(msg: UiMessage): { fileName: string; fileUrl: string; size: number } | null {
  if (!msg.payloadJson) return null
  try { return JSON.parse(msg.payloadJson) } catch { return null }
}

function triggerFileInput() { fileInputRef.value?.click() }

async function handleFileSelect(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  input.value = ''
  if (file.size > 20 * 1024 * 1024) { ElMessage.warning('文件大小不能超过 20MB'); return }

  uploading.value = true
  uploadProgress.value = 0
  try {
    if (isImageFile(file)) {
      const res = await uploadImage(file, (p) => { uploadProgress.value = p })
      if (res.code === 0 && res.data) { emit('send-image', res.data); ElMessage.success('图片发送成功') }
      else ElMessage.error(res.message || '图片上传失败')
    } else {
      const res = await uploadAttachment(file, (p) => { uploadProgress.value = p })
      if (res.code === 0 && res.data) { emit('send-attachment', res.data); ElMessage.success('附件发送成功') }
      else ElMessage.error(res.message || '附件上传失败')
    }
  } catch (e: any) {
    console.error('Upload failed:', e)
    ElMessage.error(e.response?.data?.message || '上传失败')
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

function openGiftPointsDialog() {
  giftPointsAmount.value = 10
  giftPointsRemark.value = ''
  showGiftPointsDialog.value = true
}
async function confirmGiftPoints() {
  if (!props.peerUserId) { ElMessage.warning('无法获取对方用户信息'); return }
  if (giftPointsAmount.value <= 0) { ElMessage.warning('请输入有效的积分数量'); return }
  giftingPoints.value = true
  try {
    emit('gift-points', props.peerUserId, giftPointsAmount.value, giftPointsRemark.value || undefined)
    showGiftPointsDialog.value = false
  } finally { giftingPoints.value = false }
}

function openImagePreview(url?: string) { if (url) window.open(url, '_blank') }

watch(() => props.mergedMessages.length, scrollToBottom)
onMounted(scrollToBottom)

defineExpose({ scrollToBottom })
</script>

<template>
  <div class="flex flex-col h-full">
    <!-- 聊天头部 -->
    <div class="px-3 py-2 border-b border-gray-100 bg-gray-50/50 shrink-0">
      <div class="flex items-center gap-2">
        <div class="relative">
          <div
            v-if="peerAvatar"
            class="w-8 h-8 rounded-xl bg-cover bg-center"
            :style="{ backgroundImage: `url(${peerAvatar})` }"
          />
          <div
            v-else
            class="w-8 h-8 rounded-xl bg-brand-500 text-white flex items-center justify-center font-bold text-xs"
          >
            {{ getAvatarChar(peerName) }}
          </div>
          <div
            :class="['absolute bottom-0 right-0 w-2 h-2 rounded-full border-2 border-white', wsConnected ? 'bg-green-500' : 'bg-gray-300']"
          />
        </div>
        <div>
          <h4 class="text-sm font-bold text-gray-900">{{ peerName }}</h4>
          <p class="text-[10px] text-gray-500">
            {{ peerCompany || (wsConnected ? '在线' : '离线') }}
          </p>
        </div>
      </div>
    </div>

    <!-- 消息列表（单一连续时间线） -->
    <div
      ref="chatContainerRef"
      class="flex-1 overflow-y-auto p-3 space-y-3 bg-gray-50"
    >
      <!-- 空状态 -->
      <div
        v-if="mergedMessages.length === 0"
        class="flex flex-col items-center justify-center h-full text-center text-gray-400"
      >
        <p class="text-sm">开始与 {{ peerName }} 协商</p>
        <p class="text-xs mt-1">发送消息或报价开始议价</p>
      </div>

      <!-- 连续消息流 -->
      <template v-for="(msg, idx) in mergedMessages" :key="msg.id">
        <!-- 产品标签（会话切换时内联显示） -->
        <div v-if="shouldShowProductTag(idx)" class="flex justify-center my-2">
          <button
            :class="[
              'flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium transition-all border cursor-pointer',
              activeConversationId === msg.conversationId
                ? 'bg-brand-50 text-brand-700 border-brand-300 shadow-sm'
                : 'bg-white text-gray-600 border-gray-200 hover:bg-gray-50 hover:border-gray-300'
            ]"
            @click="$emit('activate-conversation', msg.conversationId!)"
          >
            <Package class="w-3.5 h-3.5" />
            <span>{{ getProductNameByConvId(msg.conversationId) }}</span>
            <span
              v-if="getProductTypeByConvId(msg.conversationId)"
              :class="[
                'text-[10px] px-1.5 py-0.5 rounded-full',
                getProductTypeByConvId(msg.conversationId) === '供应'
                  ? 'bg-brand-100 text-brand-700'
                  : 'bg-autumn-100 text-autumn-700'
              ]"
            >{{ getProductTypeByConvId(msg.conversationId) }}</span>
            <span
              v-if="activeConversationId === msg.conversationId"
              class="text-[10px] px-1.5 py-0.5 rounded-full bg-brand-500 text-white"
            >议价中</span>
          </button>
        </div>

        <!-- 时间分隔线 -->
        <div v-if="needsTimeSeparator(idx)" class="flex justify-center py-2">
          <span class="text-[11px] text-gray-400 bg-gray-100/80 px-3 py-1 rounded-full">
            {{ formatMessageTime(msg.timestamp || msg.time) }}
          </span>
        </div>

        <!-- 普通系统消息 -->
        <div v-if="msg.type === 'system' && !isSystemActionMessage(msg)" class="flex justify-center">
          <span class="text-[10px] font-medium text-gray-400 bg-gray-100 px-2 py-0.5 rounded-full">
            {{ msg.content }}
          </span>
        </div>

        <!-- 系统操作消息 -->
        <div v-else-if="isSystemActionMessage(msg)" class="flex justify-center my-2">
          <div
            :class="[
              'flex items-center gap-2 px-4 py-2 rounded-lg border text-sm',
              getSystemMessageStyle(parseSystemPayload(msg)).bgColor,
              getSystemMessageStyle(parseSystemPayload(msg)).textColor,
              getSystemMessageStyle(parseSystemPayload(msg)).borderColor
            ]"
          >
            <CheckCircle v-if="getSystemMessageStyle(parseSystemPayload(msg)).icon === 'check'" class="w-4 h-4 shrink-0" />
            <Info v-else-if="getSystemMessageStyle(parseSystemPayload(msg)).icon === 'info'" class="w-4 h-4 shrink-0" />
            <AlertCircle v-else class="w-4 h-4 shrink-0" />
            <span class="font-medium">{{ msg.content }}</span>
          </div>
        </div>

        <!-- 合同消息卡片 -->
        <div v-else-if="isContractMessage(msg)" class="flex justify-center my-3">
          <div
            v-if="parseContractPayload(msg)"
            class="bg-gradient-to-br from-blue-50 to-indigo-50 border border-blue-200 rounded-xl p-4 shadow-sm max-w-[85%] w-80 cursor-pointer hover:shadow-md transition-all"
            @click="goToContract(parseContractPayload(msg)?.contractId)"
          >
            <div class="flex items-center gap-2 mb-3">
              <div class="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center">
                <FileSignature class="w-5 h-5 text-white" />
              </div>
              <div class="flex-1">
                <h4 class="text-sm font-bold text-gray-900">交易合同</h4>
                <p class="text-xs text-gray-500">{{ parseContractPayload(msg)?.contractNo || '生成中...' }}</p>
              </div>
              <ExternalLink class="w-4 h-4 text-blue-500" />
            </div>
            <div class="space-y-1.5 text-xs">
              <div v-if="parseContractPayload(msg)?.productName" class="flex justify-between">
                <span class="text-gray-500">商品:</span>
                <span class="text-gray-900 font-medium">{{ parseContractPayload(msg)?.productName }}</span>
              </div>
              <div v-if="parseContractPayload(msg)?.totalAmount" class="flex justify-between">
                <span class="text-gray-500">合同金额:</span>
                <span class="text-brand-600 font-bold">¥{{ parseContractPayload(msg)?.totalAmount?.toLocaleString() }}</span>
              </div>
            </div>
            <div class="mt-3 pt-2 border-t border-blue-200/50 text-center">
              <span class="text-[10px] text-blue-600 font-medium">点击查看合同详情 →</span>
            </div>
          </div>
        </div>

        <!-- 报价消息 -->
        <template v-else-if="isQuoteMessage(msg)">
          <div :class="['flex', msg.type === 'sent' ? 'justify-end' : 'justify-start']">
            <QuoteCard
              :message="msg"
              :previous-quote-payload-json="getPreviousQuotePayload(idx)"
              @accept="emit('accept-quote', msg)"
              @counter-submit="(payload) => emit('counter-quote', msg, payload)"
              @reject="emit('reject-quote', msg)"
              @draft-contract="emit('draft-contract', msg)"
            />
          </div>
        </template>

        <!-- 图片消息 -->
        <template v-else-if="isImageMessage(msg)">
          <div :class="['flex', msg.type === 'sent' ? 'justify-end' : 'justify-start']">
            <div v-if="msg.type === 'received'" class="flex gap-2 max-w-[70%]">
              <div class="w-7 h-7 rounded-lg bg-brand-500 text-white flex items-center justify-center font-bold text-[10px] shrink-0">
                {{ getAvatarChar(peerName) }}
              </div>
              <div class="bg-white border border-gray-100 p-1 rounded-xl rounded-tl-none shadow-sm overflow-hidden">
                <img
                  v-if="parseFilePayload(msg)"
                  :src="parseFilePayload(msg)?.fileUrl"
                  :alt="parseFilePayload(msg)?.fileName"
                  class="max-w-[200px] max-h-[200px] rounded-lg cursor-pointer hover:opacity-90 transition-opacity"
                  @click="openImagePreview(parseFilePayload(msg)?.fileUrl)"
                />
              </div>
            </div>
            <div v-else class="max-w-[70%]">
              <div class="bg-brand-600 p-1 rounded-xl rounded-tr-none shadow-md overflow-hidden">
                <img
                  v-if="parseFilePayload(msg)"
                  :src="parseFilePayload(msg)?.fileUrl"
                  :alt="parseFilePayload(msg)?.fileName"
                  class="max-w-[200px] max-h-[200px] rounded-lg cursor-pointer hover:opacity-90 transition-opacity"
                  @click="openImagePreview(parseFilePayload(msg)?.fileUrl)"
                />
              </div>
            </div>
          </div>
        </template>

        <!-- 附件消息 -->
        <template v-else-if="isAttachmentMessage(msg)">
          <div :class="['flex', msg.type === 'sent' ? 'justify-end' : 'justify-start']">
            <div v-if="msg.type === 'received'" class="flex gap-2 max-w-[85%]">
              <div class="w-7 h-7 rounded-lg bg-brand-500 text-white flex items-center justify-center font-bold text-[10px] shrink-0">
                {{ getAvatarChar(peerName) }}
              </div>
              <a
                v-if="parseFilePayload(msg)"
                :href="parseFilePayload(msg)?.fileUrl"
                target="_blank"
                class="bg-white border border-gray-100 px-3 py-2 rounded-xl rounded-tl-none shadow-sm flex items-center gap-2 hover:bg-gray-50 transition-colors"
              >
                <FileText class="w-8 h-8 text-brand-500 shrink-0" />
                <div class="flex-1 min-w-0">
                  <div class="text-sm text-gray-800 truncate">{{ parseFilePayload(msg)?.fileName }}</div>
                  <div class="text-[10px] text-gray-400">{{ formatFileSize(parseFilePayload(msg)?.size || 0) }}</div>
                </div>
                <Download class="w-4 h-4 text-gray-400" />
              </a>
            </div>
            <div v-else class="max-w-[85%]">
              <a
                v-if="parseFilePayload(msg)"
                :href="parseFilePayload(msg)?.fileUrl"
                target="_blank"
                class="bg-brand-600 px-3 py-2 rounded-xl rounded-tr-none shadow-md flex items-center gap-2 hover:bg-brand-700 transition-colors"
              >
                <FileText class="w-8 h-8 text-white/80 shrink-0" />
                <div class="flex-1 min-w-0">
                  <div class="text-sm text-white truncate">{{ parseFilePayload(msg)?.fileName }}</div>
                  <div class="text-[10px] text-white/70">{{ formatFileSize(parseFilePayload(msg)?.size || 0) }}</div>
                </div>
                <Download class="w-4 h-4 text-white/70" />
              </a>
            </div>
          </div>
        </template>

        <!-- 对方文本消息 -->
        <div v-else-if="msg.type === 'received'" class="flex gap-2 max-w-[85%]">
          <div class="w-7 h-7 rounded-lg bg-brand-500 text-white flex items-center justify-center font-bold text-[10px] shrink-0">
            {{ getAvatarChar(peerName) }}
          </div>
          <div class="bg-white border border-gray-100 px-3 py-2 rounded-2xl rounded-tl-none shadow-sm text-sm text-gray-800">
            {{ msg.content }}
          </div>
        </div>

        <!-- 我的文本消息 -->
        <div v-else class="flex flex-col items-end max-w-[85%] ml-auto">
          <div class="bg-brand-600 text-white px-3 py-2 rounded-2xl rounded-tr-none shadow-md text-sm">
            {{ msg.content }}
          </div>
          <div v-if="isLastSentMessage(idx)" class="flex items-center gap-1 mt-0.5 mr-1">
            <span v-if="msg.status === 'pending'" class="text-[10px] text-gray-400">发送中...</span>
            <span v-else-if="msg.status === 'failed'" class="text-[10px] text-red-500">发送失败</span>
            <span v-else-if="msg.read" class="text-[10px] text-brand-500">已读</span>
            <span v-else class="text-[10px] text-gray-400">未读</span>
          </div>
        </div>
      </template>
    </div>

    <!-- 输入区域 -->
    <div class="p-2 bg-white border-t border-gray-100 shrink-0">
      <!-- 上传进度条 -->
      <div v-if="uploading" class="mb-2 flex items-center gap-2">
        <Loader2 class="w-4 h-4 text-brand-500 animate-spin" />
        <div class="flex-1 h-1.5 bg-gray-200 rounded-full overflow-hidden">
          <div class="h-full bg-brand-500 transition-all duration-200" :style="{ width: `${uploadProgress}%` }" />
        </div>
        <span class="text-xs text-gray-500">{{ uploadProgress }}%</span>
      </div>

      <div class="flex items-center gap-1.5">
        <input
          ref="fileInputRef"
          type="file"
          class="hidden"
          accept="image/*,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.zip,.rar,.7z,.txt"
          @change="handleFileSelect"
        />

        <button
          @click="triggerFileInput"
          :disabled="uploading"
          class="p-2 text-gray-400 hover:text-brand-600 hover:bg-gray-50 rounded-lg transition-colors disabled:opacity-50"
          title="发送图片/附件"
        >
          <Paperclip class="w-5 h-5" />
        </button>

        <button
          @click="openGiftPointsDialog"
          class="p-2 text-amber-500 hover:text-amber-600 hover:bg-amber-50 rounded-lg transition-colors"
          title="赠送积分"
        >
          <Gift class="w-5 h-5" />
        </button>

        <div class="flex-1">
          <input
            v-model="messageInput"
            @keydown="handleKeydown"
            type="text"
            class="w-full h-9 px-3 rounded-lg bg-gray-50 border-none focus:ring-1 focus:ring-brand-500 text-sm"
            placeholder="输入消息..."
            :disabled="sending || uploading"
          />
        </div>
        <button
          @click="handleSend"
          :disabled="!messageInput.trim() || sending || uploading"
          class="h-9 px-3 bg-brand-600 hover:bg-brand-700 disabled:bg-gray-300 disabled:cursor-not-allowed
                 text-white rounded-lg font-medium text-sm flex items-center gap-1 transition-colors"
        >
          <Send class="w-4 h-4" />
        </button>
      </div>
    </div>

    <!-- 赠送积分弹窗 -->
    <Teleport to="body">
      <div
        v-if="showGiftPointsDialog"
        class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
        @click.self="showGiftPointsDialog = false"
      >
        <div class="bg-white rounded-xl shadow-2xl w-80 overflow-hidden">
          <div class="px-4 py-3 bg-gradient-to-r from-amber-500 to-orange-500 text-white flex items-center justify-between">
            <div class="flex items-center gap-2">
              <Gift class="w-5 h-5" />
              <span class="font-bold">赠送积分</span>
            </div>
            <button @click="showGiftPointsDialog = false" class="p-1 hover:bg-white/20 rounded-full transition-colors">
              <X class="w-4 h-4" />
            </button>
          </div>
          <div class="p-4">
            <div class="text-center mb-4">
              <div class="w-12 h-12 rounded-xl bg-brand-500 text-white flex items-center justify-center font-bold text-lg mx-auto mb-2">
                {{ getAvatarChar(peerName) }}
              </div>
              <p class="text-sm text-gray-600">赠送给 <span class="font-bold text-gray-900">{{ peerName }}</span></p>
            </div>
            <div class="mb-4">
              <label class="text-xs text-gray-500 block mb-1">积分数量</label>
              <div class="flex items-center gap-2">
                <button
                  v-for="amount in [10, 50, 100, 200]"
                  :key="amount"
                  @click="giftPointsAmount = amount"
                  :class="[
                    'flex-1 h-9 rounded-lg text-sm font-medium transition-all',
                    giftPointsAmount === amount ? 'bg-amber-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  ]"
                >
                  {{ amount }}
                </button>
              </div>
              <div class="mt-2">
                <input
                  v-model.number="giftPointsAmount"
                  type="number"
                  min="1"
                  class="w-full h-9 px-3 rounded-lg border border-gray-200 focus:ring-1 focus:ring-amber-500 focus:border-amber-500 text-sm"
                  placeholder="自定义数量"
                />
              </div>
            </div>
            <div class="mb-4">
              <label class="text-xs text-gray-500 block mb-1">备注（可选）</label>
              <input
                v-model="giftPointsRemark"
                type="text"
                class="w-full h-9 px-3 rounded-lg border border-gray-200 focus:ring-1 focus:ring-amber-500 focus:border-amber-500 text-sm"
                placeholder="感谢您的合作！"
              />
            </div>
            <button
              @click="confirmGiftPoints"
              :disabled="giftPointsAmount <= 0 || giftingPoints"
              class="w-full h-10 bg-gradient-to-r from-amber-500 to-orange-500 hover:from-amber-600 hover:to-orange-600
                     disabled:from-gray-300 disabled:to-gray-400 text-white font-bold rounded-lg
                     flex items-center justify-center gap-2 transition-all"
            >
              <Gift class="w-4 h-4" />
              {{ giftingPoints ? '赠送中...' : `确认赠送 ${giftPointsAmount} 积分` }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
