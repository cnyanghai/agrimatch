<script setup lang="ts">
import { computed } from 'vue'
import { Search, FolderOpened, MoreFilled } from '@element-plus/icons-vue'
import { MessageCircle, Clock, CheckCircle, Pause, XCircle } from 'lucide-vue-next'
import type { PeerGroup, TimeGroup, ChatConversationResponse, ConversationBusinessStatus } from '../../../types/chat/conversation'
import { getPeerDisplayName, getAvatarColor, BUSINESS_STATUS_MAP } from '../../../types/chat/conversation'
import ConversationStatusFilter from './ConversationStatusFilter.vue'

const props = defineProps<{
  timeGroups: TimeGroup[]
  activePeerId: number | null
  activeConversationId: number | null
  currentPeerConversations: ChatConversationResponse[]
  searchKeyword: string
  archivedCount?: number
  loading?: boolean
  // 新增：状态过滤相关
  activeStatusFilter?: ConversationBusinessStatus | 'ALL'
  statusCounts?: Record<ConversationBusinessStatus | 'ALL', number>
  conversationStatusMap?: Map<number, ConversationBusinessStatus>
}>()

const emit = defineEmits<{
  (e: 'select-peer', peer: PeerGroup): void
  (e: 'switch-conversation', conversationId: number): void
  (e: 'archive-conversation', conversationId: number): void
  (e: 'update:searchKeyword', value: string): void
  (e: 'show-archived'): void
  // 新增：状态相关
  (e: 'update:activeStatusFilter', status: ConversationBusinessStatus | 'ALL'): void
  (e: 'change-conversation-status', conversationId: number, status: ConversationBusinessStatus): void
}>()

// 获取会话的业务状态
function getConversationStatus(convId: number): ConversationBusinessStatus {
  return props.conversationStatusMap?.get(convId) || 'ACTIVE'
}

// 获取状态图标组件
function getStatusIcon(status: ConversationBusinessStatus) {
  const iconMap = {
    ACTIVE: MessageCircle,
    PENDING: Clock,
    COMPLETED: CheckCircle,
    ON_HOLD: Pause,
    CLOSED: XCircle
  }
  return iconMap[status]
}

// 默认状态计数
const defaultStatusCounts = computed(() => ({
  ALL: 0,
  ACTIVE: 0,
  PENDING: 0,
  COMPLETED: 0,
  ON_HOLD: 0,
  CLOSED: 0
}))

function formatTimeLabel(time?: string): string {
  if (!time) return ''
  try {
    const d = new Date(time)
    const now = new Date()
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)

    if (d >= today) {
      return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } else if (d >= yesterday) {
      return '昨天'
    } else {
      return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
    }
  } catch {
    return ''
  }
}

function truncateContent(content?: string, maxLen = 20): string {
  if (!content) return ''
  return content.length > maxLen ? content.slice(0, maxLen) + '...' : content
}

function getSubjectTypeLabel(type?: string): string {
  if (type === 'SUPPLY') return '供应'
  if (type === 'NEED') return '采购'
  return ''
}
</script>

<template>
  <div class="h-full flex flex-col bg-gray-50/50">
    <!-- 搜索栏 -->
    <div class="px-4 py-3 bg-white border-b border-gray-100 space-y-3">
      <div class="relative">
        <el-icon class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"><Search /></el-icon>
        <input
          :value="searchKeyword"
          @input="emit('update:searchKeyword', ($event.target as HTMLInputElement).value)"
          type="text"
          placeholder="搜索联系人或消息"
          class="w-full pl-9 pr-3 py-2 text-sm bg-gray-50 border border-gray-200 rounded-lg focus:bg-white focus:border-brand-500 focus:ring-1 focus:ring-brand-500/20 outline-none transition-all"
        />
      </div>

      <!-- 状态过滤器 -->
      <ConversationStatusFilter
        v-if="statusCounts"
        :active-status="activeStatusFilter || 'ALL'"
        :status-counts="statusCounts || defaultStatusCounts"
        @update:active-status="emit('update:activeStatusFilter', $event)"
      />
    </div>

    <!-- 联系人列表 -->
    <div class="flex-1 overflow-y-auto">
      <!-- 加载状态 -->
      <div v-if="loading" class="flex items-center justify-center py-12">
        <div class="w-6 h-6 border-2 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="timeGroups.length === 0" class="flex flex-col items-center justify-center py-12 px-4 text-center">
        <div class="w-16 h-16 rounded-2xl bg-gray-100 flex items-center justify-center mb-3">
          <svg class="w-8 h-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
        </div>
        <p class="text-sm text-gray-500">暂无会话</p>
        <p class="text-xs text-gray-400 mt-1">从供应或采购列表发起沟通</p>
      </div>

      <!-- 按时间分组 -->
      <template v-else>
        <div v-for="group in timeGroups" :key="group.label" class="mb-2">
          <div class="px-4 py-2 text-[10px] font-bold text-gray-400 uppercase tracking-widest sticky top-0 bg-gray-50/90 backdrop-blur-sm">
            {{ group.label }}
          </div>

          <!-- 联系人项 -->
          <div
            v-for="peer in group.peers"
            :key="peer.peerUserId"
            class="group"
          >
            <!-- 联系人头部 -->
            <div
              @click="emit('select-peer', peer)"
              class="flex items-center gap-3 px-4 py-3 cursor-pointer transition-all hover:bg-white"
              :class="{ 'bg-white shadow-sm': activePeerId === peer.peerUserId }"
            >
              <!-- 头像 -->
              <div
                :class="['w-10 h-10 rounded-xl flex items-center justify-center text-white font-bold text-sm shrink-0 overflow-hidden', !peer.peerAvatar && getAvatarColor(peer.peerNickName || peer.peerUserName)]"
              >
                <img v-if="peer.peerAvatar" :src="peer.peerAvatar" alt="头像" class="w-full h-full object-cover" />
                <span v-else>{{ (peer.peerNickName || peer.peerUserName || '?')[0] }}</span>
              </div>

              <!-- 信息 -->
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between">
                  <span class="font-medium text-gray-900 truncate">
                    {{ peer.peerNickName || peer.peerUserName || '用户' }}
                  </span>
                  <span class="text-[10px] text-gray-400 shrink-0 ml-2">
                    {{ formatTimeLabel(peer.lastTime) }}
                  </span>
                </div>
                <div class="flex items-center justify-between mt-0.5">
                  <span class="text-xs text-gray-500 truncate">
                    {{ peer.peerCompanyName || truncateContent(peer.lastContent) }}
                  </span>
                  <span
                    v-if="peer.totalUnread > 0"
                    class="ml-2 px-1.5 min-w-[18px] h-[18px] text-[10px] font-bold text-white bg-red-500 rounded-full flex items-center justify-center shrink-0"
                  >
                    {{ peer.totalUnread > 99 ? '99+' : peer.totalUnread }}
                  </span>
                </div>
              </div>
            </div>

            <!-- 该联系人下的会话列表（展开时显示） -->
            <div
              v-if="activePeerId === peer.peerUserId && currentPeerConversations.length > 1"
              class="bg-white border-y border-gray-100"
            >
              <div
                v-for="conv in currentPeerConversations"
                :key="conv.id"
                @click.stop="emit('switch-conversation', conv.id)"
                class="flex items-center gap-2 pl-14 pr-4 py-2 cursor-pointer transition-all hover:bg-gray-50"
                :class="{ 'bg-brand-50/50': activeConversationId === conv.id }"
              >
                <!-- 状态指示器 -->
                <component
                  :is="getStatusIcon(getConversationStatus(conv.id))"
                  :class="[
                    'w-3.5 h-3.5 shrink-0',
                    BUSINESS_STATUS_MAP[getConversationStatus(conv.id)]?.color || 'text-gray-400'
                  ]"
                  :title="BUSINESS_STATUS_MAP[getConversationStatus(conv.id)]?.label"
                />
                <span
                  v-if="conv.subjectType"
                  :class="[
                    'px-1.5 py-0.5 text-[10px] font-bold rounded',
                    conv.subjectType === 'SUPPLY' ? 'bg-brand-100 text-brand-700' : 'bg-autumn-100 text-autumn-700'
                  ]"
                >
                  {{ getSubjectTypeLabel(conv.subjectType) }}
                </span>
                <span class="text-xs text-gray-600 truncate flex-1">
                  {{ truncateContent(conv.lastContent, 15) || '新会话' }}
                </span>
                <span
                  v-if="conv.unreadCount && conv.unreadCount > 0"
                  class="w-2 h-2 bg-red-500 rounded-full shrink-0"
                ></span>
                <el-dropdown trigger="click" @click.stop>
                  <button class="p-1 rounded hover:bg-gray-200 opacity-0 group-hover:opacity-100 transition-opacity">
                    <el-icon class="text-gray-400"><MoreFilled /></el-icon>
                  </button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item
                        v-if="getConversationStatus(conv.id) !== 'ACTIVE'"
                        @click="emit('change-conversation-status', conv.id, 'ACTIVE')"
                      >
                        <div class="flex items-center gap-2 text-brand-600">
                          <MessageCircle class="w-4 h-4" />
                          标为活跃
                        </div>
                      </el-dropdown-item>
                      <el-dropdown-item
                        v-if="getConversationStatus(conv.id) !== 'PENDING'"
                        @click="emit('change-conversation-status', conv.id, 'PENDING')"
                      >
                        <div class="flex items-center gap-2 text-amber-600">
                          <Clock class="w-4 h-4" />
                          标为待跟进
                        </div>
                      </el-dropdown-item>
                      <el-dropdown-item
                        v-if="getConversationStatus(conv.id) !== 'COMPLETED'"
                        @click="emit('change-conversation-status', conv.id, 'COMPLETED')"
                      >
                        <div class="flex items-center gap-2 text-brand-700">
                          <CheckCircle class="w-4 h-4" />
                          标为已成交
                        </div>
                      </el-dropdown-item>
                      <el-dropdown-item
                        v-if="getConversationStatus(conv.id) !== 'ON_HOLD'"
                        @click="emit('change-conversation-status', conv.id, 'ON_HOLD')"
                      >
                        <div class="flex items-center gap-2 text-gray-500">
                          <Pause class="w-4 h-4" />
                          标为暂缓
                        </div>
                      </el-dropdown-item>
                      <el-dropdown-item divided
                        v-if="getConversationStatus(conv.id) !== 'CLOSED'"
                        @click="emit('change-conversation-status', conv.id, 'CLOSED')"
                      >
                        <div class="flex items-center gap-2 text-red-500">
                          <XCircle class="w-4 h-4" />
                          关闭会话
                        </div>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 已归档入口 -->
    <div
      v-if="archivedCount && archivedCount > 0"
      @click="emit('show-archived')"
      class="px-4 py-3 bg-white border-t border-gray-100 flex items-center gap-2 cursor-pointer hover:bg-gray-50 transition-colors"
    >
      <el-icon class="text-gray-400"><FolderOpened /></el-icon>
      <span class="text-sm text-gray-600">已归档会话</span>
      <span class="ml-auto text-xs text-gray-400">{{ archivedCount }}</span>
    </div>
  </div>
</template>
