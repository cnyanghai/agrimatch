<script setup lang="ts">
/**
 * ChatPanel - 议价聊天面板
 * 支持文本消息、报价消息、图片消息、附件消息显示
 * 集成报价操作、附件上传、赠送积分功能
 */
import { ref, nextTick, watch, onMounted } from 'vue'
import { Send, Paperclip, MoreVertical, DollarSign, Gift, FileText, X, Download, Loader2, CheckCircle, Info, AlertCircle, FileSignature, ExternalLink } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { UiMessage } from '../../types/chat/message'
import QuoteCard from '../chat/message/QuoteCard.vue'
import { uploadImage, uploadAttachment, isImageFile, formatFileSize, type FileUploadResponse } from '../../api/file'

const props = defineProps<{
  messages: UiMessage[]
  peerName: string
  peerAvatar?: string
  peerCompany?: string
  peerUserId?: number
  wsConnected: boolean
  sending?: boolean
  /** 是否显示报价按钮 */
  showQuoteButton?: boolean
}>()

const emit = defineEmits<{
  (e: 'send', text: string): void
  (e: 'send-image', payload: FileUploadResponse): void
  (e: 'send-attachment', payload: FileUploadResponse): void
  (e: 'open-quote'): void
  (e: 'accept-quote', message: UiMessage): void
  (e: 'counter-quote', message: UiMessage, payload: { price?: number; basisPrice?: number; quantity?: string; remark?: string }): void
  (e: 'reject-quote', message: UiMessage): void
  (e: 'draft-contract', message: UiMessage): void
  (e: 'gift-points', toUserId: number, points: number, remark?: string): void
}>()

const router = useRouter()
const messageInput = ref('')
const chatContainerRef = ref<HTMLElement | null>(null)
const fileInputRef = ref<HTMLInputElement | null>(null)
const uploading = ref(false)
const uploadProgress = ref(0)

// 赠送积分弹窗状态
const showGiftPointsDialog = ref(false)
const giftPointsAmount = ref(10)
const giftPointsRemark = ref('')
const giftingPoints = ref(false)

// 获取消息的前一条报价（用于计算差异）
function getPreviousQuotePayload(index: number): string | undefined {
  for (let i = index - 1; i >= 0; i--) {
    const msg = props.messages[i]
    if (msg && (msg.msgType === 'QUOTE' || msg.payloadJson)) {
      return msg.payloadJson
    }
  }
  return undefined
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}

// 发送消息
function handleSend() {
  const text = messageInput.value.trim()
  if (!text) return
  emit('send', text)
  messageInput.value = ''
}

// 键盘事件
function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

// 格式化时间
function formatMessageTime(time?: string): string {
  if (!time) return ''
  try {
    const d = new Date(time)
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } catch {
    return ''
  }
}

// 获取头像首字母
function getAvatarChar(name?: string): string {
  if (!name || name.length === 0) return '?'
  return (name[0] || '?').toUpperCase()
}

// 判断是否为报价消息
function isQuoteMessage(msg: UiMessage): boolean {
  return msg.msgType === 'QUOTE' || (!!msg.payloadJson && msg.payloadJson.length > 10)
}

// 判断是否为图片消息
function isImageMessage(msg: UiMessage): boolean {
  return msg.msgType === 'IMAGE'
}

// 判断是否为附件消息
function isAttachmentMessage(msg: UiMessage): boolean {
  return msg.msgType === 'ATTACHMENT'
}

// 判断是否为合同消息
function isContractMessage(msg: UiMessage): boolean {
  return msg.msgType === 'CONTRACT'
}

// 解析合同消息payload
interface ContractMessagePayload {
  contractId?: number
  contractNo?: string
  productName?: string
  totalAmount?: number
  status?: number
}

function parseContractPayload(msg: UiMessage): ContractMessagePayload | null {
  if (!msg.payloadJson) return null
  try {
    return JSON.parse(msg.payloadJson)
  } catch {
    return null
  }
}

// 跳转到合同详情
function goToContract(contractId?: number) {
  if (contractId) {
    router.push(`/contracts/${contractId}`)
  }
}

// 判断是否为系统消息（非普通系统类型）
function isSystemActionMessage(msg: UiMessage): boolean {
  return msg.msgType === 'SYSTEM' && !!msg.payloadJson
}

// 解析系统消息payload
interface SystemMessagePayload {
  action?: string
  role?: 'buyer' | 'seller'
  confirmedAt?: string
}

function parseSystemPayload(msg: UiMessage): SystemMessagePayload | null {
  if (!msg.payloadJson) return null
  try {
    return JSON.parse(msg.payloadJson)
  } catch {
    return null
  }
}

// 获取系统消息图标和样式
function getSystemMessageStyle(payload: SystemMessagePayload | null): {
  icon: 'check' | 'info' | 'alert'
  bgColor: string
  textColor: string
  borderColor: string
} {
  if (!payload) {
    return { icon: 'info', bgColor: 'bg-gray-50', textColor: 'text-gray-600', borderColor: 'border-gray-200' }
  }

  switch (payload.action) {
    case 'CONFIRM_TERMS':
      return { icon: 'check', bgColor: 'bg-brand-50', textColor: 'text-brand-700', borderColor: 'border-brand-200' }
    case 'CONTRACT_CREATED':
      return { icon: 'check', bgColor: 'bg-blue-50', textColor: 'text-blue-700', borderColor: 'border-blue-200' }
    case 'QUOTE_ACCEPTED':
      return { icon: 'check', bgColor: 'bg-green-50', textColor: 'text-green-700', borderColor: 'border-green-200' }
    case 'QUOTE_REJECTED':
      return { icon: 'alert', bgColor: 'bg-red-50', textColor: 'text-red-600', borderColor: 'border-red-200' }
    default:
      return { icon: 'info', bgColor: 'bg-gray-50', textColor: 'text-gray-600', borderColor: 'border-gray-200' }
  }
}

// 解析图片/附件payload
function parseFilePayload(msg: UiMessage): { fileName: string; fileUrl: string; size: number } | null {
  if (!msg.payloadJson) return null
  try {
    return JSON.parse(msg.payloadJson)
  } catch {
    return null
  }
}

// 触发文件选择
function triggerFileInput() {
  fileInputRef.value?.click()
}

// 处理文件选择
async function handleFileSelect(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  // 重置input以便可以再次选择同一个文件
  input.value = ''

  // 检查文件大小（最大 20MB）
  if (file.size > 20 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过 20MB')
    return
  }

  uploading.value = true
  uploadProgress.value = 0

  try {
    if (isImageFile(file)) {
      // 上传图片
      const res = await uploadImage(file, (percent) => {
        uploadProgress.value = percent
      })
      if (res.code === 0 && res.data) {
        emit('send-image', res.data)
        ElMessage.success('图片发送成功')
      } else {
        ElMessage.error(res.message || '图片上传失败')
      }
    } else {
      // 上传附件
      const res = await uploadAttachment(file, (percent) => {
        uploadProgress.value = percent
      })
      if (res.code === 0 && res.data) {
        emit('send-attachment', res.data)
        ElMessage.success('附件发送成功')
      } else {
        ElMessage.error(res.message || '附件上传失败')
      }
    }
  } catch (e: any) {
    console.error('Upload failed:', e)
    ElMessage.error(e.response?.data?.message || '上传失败')
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

// 打开赠送积分弹窗
function openGiftPointsDialog() {
  giftPointsAmount.value = 10
  giftPointsRemark.value = ''
  showGiftPointsDialog.value = true
}

// 确认赠送积分
async function confirmGiftPoints() {
  if (!props.peerUserId) {
    ElMessage.warning('无法获取对方用户信息')
    return
  }
  if (giftPointsAmount.value <= 0) {
    ElMessage.warning('请输入有效的积分数量')
    return
  }

  giftingPoints.value = true
  try {
    emit('gift-points', props.peerUserId, giftPointsAmount.value, giftPointsRemark.value || undefined)
    showGiftPointsDialog.value = false
  } finally {
    giftingPoints.value = false
  }
}

// 打开图片预览
function openImagePreview(url?: string) {
  if (url) {
    window.open(url, '_blank')
  }
}

// 消息变化时滚动到底部
watch(() => props.messages.length, scrollToBottom)

onMounted(scrollToBottom)

defineExpose({ scrollToBottom })
</script>

<template>
  <div class="flex flex-col h-full">
    <!-- 聊天头部 -->
    <div class="px-3 py-2 border-b border-gray-100 bg-gray-50/50 flex items-center justify-between shrink-0">
      <div class="flex items-center gap-2">
        <!-- 对方头像 -->
        <div class="relative">
          <div
            v-if="peerAvatar"
            class="w-8 h-8 rounded-full bg-cover bg-center"
            :style="{ backgroundImage: `url(${peerAvatar})` }"
          />
          <div
            v-else
            class="w-8 h-8 rounded-full bg-brand-500 text-white flex items-center justify-center font-bold text-xs"
          >
            {{ getAvatarChar(peerName) }}
          </div>
          <!-- 在线状态 -->
          <div
            :class="[
              'absolute bottom-0 right-0 w-2 h-2 rounded-full border-2 border-white',
              wsConnected ? 'bg-green-500' : 'bg-gray-300'
            ]"
          />
        </div>
        <!-- 对方信息 -->
        <div>
          <h4 class="text-sm font-bold text-gray-900">{{ peerName }}</h4>
          <p class="text-[10px] text-gray-500">
            {{ peerCompany || (wsConnected ? '在线' : '离线') }}
          </p>
        </div>
      </div>
      <button class="p-1 hover:bg-gray-100 rounded-md text-gray-400 transition-colors">
        <MoreVertical class="w-4 h-4" />
      </button>
    </div>

    <!-- 消息列表 -->
    <div
      ref="chatContainerRef"
      class="flex-1 overflow-y-auto p-3 space-y-3 bg-gray-50"
    >
      <!-- 空状态 -->
      <div
        v-if="messages.length === 0"
        class="flex flex-col items-center justify-center h-full text-center text-gray-400"
      >
        <p class="text-sm">开始与 {{ peerName }} 协商</p>
        <p class="text-xs mt-1">发送消息或报价开始议价</p>
      </div>

      <!-- 消息列表 -->
      <template v-for="(msg, idx) in messages" :key="msg.id">
        <!-- 普通系统消息 -->
        <div v-if="msg.type === 'system' && !isSystemActionMessage(msg)" class="flex justify-center">
          <span class="text-[10px] font-medium text-gray-400 bg-gray-100 px-2 py-0.5 rounded-full">
            {{ msg.content }}
          </span>
        </div>

        <!-- 系统操作消息（条款确认、合同创建等） -->
        <div v-else-if="isSystemActionMessage(msg)" class="flex justify-center my-2">
          <div
            :class="[
              'flex items-center gap-2 px-4 py-2 rounded-lg border text-sm',
              getSystemMessageStyle(parseSystemPayload(msg)).bgColor,
              getSystemMessageStyle(parseSystemPayload(msg)).textColor,
              getSystemMessageStyle(parseSystemPayload(msg)).borderColor
            ]"
          >
            <CheckCircle
              v-if="getSystemMessageStyle(parseSystemPayload(msg)).icon === 'check'"
              class="w-4 h-4 shrink-0"
            />
            <Info
              v-else-if="getSystemMessageStyle(parseSystemPayload(msg)).icon === 'info'"
              class="w-4 h-4 shrink-0"
            />
            <AlertCircle
              v-else
              class="w-4 h-4 shrink-0"
            />
            <span class="font-medium">{{ msg.content }}</span>
            <span class="text-[10px] opacity-70 ml-2">{{ formatMessageTime(msg.time) }}</span>
          </div>
        </div>

        <!-- 合同消息卡片 -->
        <div v-else-if="isContractMessage(msg)" class="flex justify-center my-3">
          <div
            v-if="parseContractPayload(msg)"
            class="bg-gradient-to-br from-blue-50 to-indigo-50 border border-blue-200 rounded-xl p-4 shadow-sm max-w-[85%] w-80 cursor-pointer hover:shadow-md transition-all"
            @click="goToContract(parseContractPayload(msg)?.contractId)"
          >
            <!-- 卡片头部 -->
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

            <!-- 合同信息 -->
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

            <!-- 底部提示 -->
            <div class="mt-3 pt-2 border-t border-blue-200/50 text-center">
              <span class="text-[10px] text-blue-600 font-medium">点击查看合同详情 →</span>
            </div>

            <!-- 时间戳 -->
            <div class="text-right mt-1">
              <span class="text-[10px] text-gray-400">{{ formatMessageTime(msg.time) }}</span>
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
              <div class="w-7 h-7 rounded-full bg-brand-500 text-white flex items-center justify-center font-bold text-[10px] shrink-0">
                {{ getAvatarChar(peerName) }}
              </div>
              <div class="flex flex-col gap-0.5">
                <div class="bg-white border border-gray-100 p-1 rounded-xl rounded-tl-none shadow-sm overflow-hidden">
                  <img
                    v-if="parseFilePayload(msg)"
                    :src="parseFilePayload(msg)?.fileUrl"
                    :alt="parseFilePayload(msg)?.fileName"
                    class="max-w-[200px] max-h-[200px] rounded-lg cursor-pointer hover:opacity-90 transition-opacity"
                    @click="openImagePreview(parseFilePayload(msg)?.fileUrl)"
                  />
                </div>
                <span class="text-[10px] text-gray-400 ml-1">{{ formatMessageTime(msg.time) }}</span>
              </div>
            </div>
            <div v-else class="flex flex-row-reverse gap-2 max-w-[70%]">
              <div class="flex flex-col gap-0.5 items-end">
                <div class="bg-brand-600 p-1 rounded-xl rounded-tr-none shadow-md overflow-hidden">
                  <img
                    v-if="parseFilePayload(msg)"
                    :src="parseFilePayload(msg)?.fileUrl"
                    :alt="parseFilePayload(msg)?.fileName"
                    class="max-w-[200px] max-h-[200px] rounded-lg cursor-pointer hover:opacity-90 transition-opacity"
                    @click="openImagePreview(parseFilePayload(msg)?.fileUrl)"
                  />
                </div>
                <span class="text-[10px] text-gray-400 mr-1">{{ formatMessageTime(msg.time) }}</span>
              </div>
            </div>
          </div>
        </template>

        <!-- 附件消息 -->
        <template v-else-if="isAttachmentMessage(msg)">
          <div :class="['flex', msg.type === 'sent' ? 'justify-end' : 'justify-start']">
            <div v-if="msg.type === 'received'" class="flex gap-2 max-w-[85%]">
              <div class="w-7 h-7 rounded-full bg-brand-500 text-white flex items-center justify-center font-bold text-[10px] shrink-0">
                {{ getAvatarChar(peerName) }}
              </div>
              <div class="flex flex-col gap-0.5">
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
                <span class="text-[10px] text-gray-400 ml-1">{{ formatMessageTime(msg.time) }}</span>
              </div>
            </div>
            <div v-else class="flex flex-row-reverse gap-2 max-w-[85%]">
              <div class="flex flex-col gap-0.5 items-end">
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
                <span class="text-[10px] text-gray-400 mr-1">{{ formatMessageTime(msg.time) }}</span>
              </div>
            </div>
          </div>
        </template>

        <!-- 对方文本消息 -->
        <div v-else-if="msg.type === 'received'" class="flex gap-2 max-w-[85%]">
          <div class="w-7 h-7 rounded-full bg-brand-500 text-white flex items-center justify-center font-bold text-[10px] shrink-0">
            {{ getAvatarChar(peerName) }}
          </div>
          <div class="flex flex-col gap-0.5">
            <div class="bg-white border border-gray-100 px-3 py-2 rounded-2xl rounded-tl-none shadow-sm text-sm text-gray-800">
              {{ msg.content }}
            </div>
            <span class="text-[10px] text-gray-400 ml-1">{{ formatMessageTime(msg.time) }}</span>
          </div>
        </div>

        <!-- 我的文本消息 -->
        <div v-else class="flex flex-row-reverse gap-2 max-w-[85%] ml-auto">
          <div class="flex flex-col gap-0.5 items-end">
            <div class="bg-brand-600 text-white px-3 py-2 rounded-2xl rounded-tr-none shadow-md text-sm">
              {{ msg.content }}
            </div>
            <span class="text-[10px] text-gray-400 mr-1">{{ formatMessageTime(msg.time) }}</span>
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
          <div
            class="h-full bg-brand-500 transition-all duration-200"
            :style="{ width: `${uploadProgress}%` }"
          />
        </div>
        <span class="text-xs text-gray-500">{{ uploadProgress }}%</span>
      </div>

      <div class="flex items-center gap-1.5">
        <!-- 隐藏的文件输入 -->
        <input
          ref="fileInputRef"
          type="file"
          class="hidden"
          accept="image/*,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.zip,.rar,.7z,.txt"
          @change="handleFileSelect"
        />

        <!-- 报价按钮 -->
        <button
          v-if="showQuoteButton"
          @click="emit('open-quote')"
          class="p-2 text-brand-600 hover:text-brand-700 hover:bg-brand-50 rounded-lg transition-colors"
          title="发送报价"
        >
          <DollarSign class="w-5 h-5" />
        </button>

        <!-- 附件按钮 -->
        <button
          @click="triggerFileInput"
          :disabled="uploading"
          class="p-2 text-gray-400 hover:text-brand-600 hover:bg-gray-50 rounded-lg transition-colors disabled:opacity-50"
          title="发送图片/附件"
        >
          <Paperclip class="w-5 h-5" />
        </button>

        <!-- 赠送积分按钮 -->
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
          <!-- 弹窗头部 -->
          <div class="px-4 py-3 bg-gradient-to-r from-amber-500 to-orange-500 text-white flex items-center justify-between">
            <div class="flex items-center gap-2">
              <Gift class="w-5 h-5" />
              <span class="font-bold">赠送积分</span>
            </div>
            <button @click="showGiftPointsDialog = false" class="p-1 hover:bg-white/20 rounded-full transition-colors">
              <X class="w-4 h-4" />
            </button>
          </div>

          <!-- 弹窗内容 -->
          <div class="p-4">
            <div class="text-center mb-4">
              <div class="w-12 h-12 rounded-full bg-brand-500 text-white flex items-center justify-center font-bold text-lg mx-auto mb-2">
                {{ getAvatarChar(peerName) }}
              </div>
              <p class="text-sm text-gray-600">赠送给 <span class="font-bold text-gray-900">{{ peerName }}</span></p>
            </div>

            <!-- 积分数量 -->
            <div class="mb-4">
              <label class="text-xs text-gray-500 block mb-1">积分数量</label>
              <div class="flex items-center gap-2">
                <button
                  v-for="amount in [10, 50, 100, 200]"
                  :key="amount"
                  @click="giftPointsAmount = amount"
                  :class="[
                    'flex-1 h-9 rounded-lg text-sm font-medium transition-all',
                    giftPointsAmount === amount
                      ? 'bg-amber-500 text-white'
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
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

            <!-- 备注 -->
            <div class="mb-4">
              <label class="text-xs text-gray-500 block mb-1">备注（可选）</label>
              <input
                v-model="giftPointsRemark"
                type="text"
                class="w-full h-9 px-3 rounded-lg border border-gray-200 focus:ring-1 focus:ring-amber-500 focus:border-amber-500 text-sm"
                placeholder="感谢您的合作！"
              />
            </div>

            <!-- 确认按钮 -->
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
