<script setup lang="ts">
/**
 * ChatListView - 消息中心
 * 按用户归集显示会话列表，而非按标的
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, MessageCircle, ChevronRight } from 'lucide-vue-next'
import { useAuthStore } from '../store/auth'
import { getConversations, type ChatConversationResponse } from '../api/chat'
import { getAvatarColor } from '../types/chat/conversation'

const router = useRouter()
const auth = useAuthStore()

// ==================== Types ====================

interface UserConversationGroup {
  peerId: number
  peerUserName: string
  peerNickName: string
  peerCompanyName: string
  conversations: ChatConversationResponse[]
  lastTime: string
  lastContent: string
  totalUnread: number
}

// ==================== State ====================

const loading = ref(true)
const conversations = ref<ChatConversationResponse[]>([])
const searchKeyword = ref('')

// ==================== Computed ====================

// 按用户归集会话
const userGroups = computed<UserConversationGroup[]>(() => {
  const groupMap = new Map<number, UserConversationGroup>()

  for (const conv of conversations.value) {
    const peerId = conv.peerUserId
    if (!groupMap.has(peerId)) {
      groupMap.set(peerId, {
        peerId,
        peerUserName: conv.peerUserName || '',
        peerNickName: conv.peerNickName || '',
        peerCompanyName: conv.peerCompanyName || '',
        conversations: [],
        lastTime: conv.lastTime || '',
        lastContent: conv.lastContent || '',
        totalUnread: 0
      })
    }

    const group = groupMap.get(peerId)!
    group.conversations.push(conv)
    group.totalUnread += conv.unreadCount || 0

    // 更新最新时间和消息
    if (conv.lastTime && conv.lastTime > group.lastTime) {
      group.lastTime = conv.lastTime
      group.lastContent = conv.lastContent || ''
    }
  }

  // 按最新时间排序
  return Array.from(groupMap.values()).sort((a, b) =>
    b.lastTime.localeCompare(a.lastTime)
  )
})

// 过滤后的用户组
const filteredGroups = computed(() => {
  if (!searchKeyword.value) return userGroups.value

  const kw = searchKeyword.value.toLowerCase()
  return userGroups.value.filter(g =>
    (g.peerNickName || '').toLowerCase().includes(kw) ||
    (g.peerUserName || '').toLowerCase().includes(kw) ||
    (g.peerCompanyName || '').toLowerCase().includes(kw)
  )
})

// ==================== Methods ====================

// 格式化时间
function formatTime(timeStr?: string): string {
  if (!timeStr) return ''
  try {
    const d = new Date(timeStr)
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

// 获取头像首字母
function getAvatarChar(name?: string): string {
  if (!name) return '?'
  return name[0].toUpperCase()
}

// 获取用户显示名称
function getDisplayName(group: UserConversationGroup): string {
  return group.peerNickName || group.peerUserName || '用户'
}

// 获取标的摘要（显示该用户关联的标的数量和类型）
function getSubjectSummary(group: UserConversationGroup): string {
  const supplyCount = group.conversations.filter(c => c.subjectType === 'SUPPLY').length
  const needCount = group.conversations.filter(c => c.subjectType === 'NEED').length

  const parts: string[] = []
  if (supplyCount > 0) parts.push(`${supplyCount}个供应`)
  if (needCount > 0) parts.push(`${needCount}个采购`)

  return parts.join(' · ') || '暂无标的'
}

// 跳转到聊天视图
function goToChat(group: UserConversationGroup) {
  // 跳转到与该用户的聊天，带上peerId
  router.push(`/chat/${group.peerId}`)
}

// ==================== Lifecycle ====================

onMounted(async () => {
  loading.value = true
  try {
    const res = await getConversations()
    if (res.code === 0 && res.data) {
      conversations.value = res.data
    }
  } catch (e) {
    console.error('Load conversations failed:', e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-100 py-6 px-4 md:px-6">
    <div class="max-w-2xl mx-auto">
      <!-- 页面标题 -->
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-gray-900">消息中心</h1>
        <p class="text-sm text-gray-500 mt-1">与供应商和采购商沟通协商</p>
      </div>

      <!-- 搜索框 -->
      <div class="bg-white rounded-xl shadow-sm p-4 mb-4">
        <div class="relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索联系人或公司..."
            class="w-full h-11 pl-10 pr-4 rounded-lg border border-gray-200 bg-gray-50
                   focus:bg-white focus:border-brand-500 focus:ring-1 focus:ring-brand-500/20
                   text-sm transition-all"
          />
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex items-center justify-center py-20">
        <div class="w-8 h-8 border-2 border-brand-500 border-t-transparent rounded-full animate-spin"></div>
      </div>

      <!-- 空状态 -->
      <div
        v-else-if="filteredGroups.length === 0"
        class="bg-white rounded-xl shadow-sm p-12 text-center"
      >
        <div class="w-16 h-16 mx-auto rounded-2xl bg-gray-100 flex items-center justify-center mb-4">
          <MessageCircle class="w-8 h-8 text-gray-400" />
        </div>
        <p class="text-gray-500 mb-1">{{ searchKeyword ? '没有匹配的联系人' : '暂无会话' }}</p>
        <p class="text-sm text-gray-400">从供应或采购列表发起沟通开始议价</p>
      </div>

      <!-- 用户会话列表 -->
      <div v-else class="space-y-3">
        <div
          v-for="group in filteredGroups"
          :key="group.peerId"
          @click="goToChat(group)"
          class="bg-white rounded-xl shadow-sm p-4 cursor-pointer
                 hover:shadow-md hover:border-brand-500 border-2 border-transparent
                 transition-all group"
        >
          <div class="flex items-center gap-4">
            <!-- 头像 -->
            <div
              :class="[
                'w-12 h-12 rounded-xl text-white flex items-center justify-center font-bold text-lg shrink-0',
                getAvatarColor(getDisplayName(group))
              ]"
            >
              {{ getAvatarChar(getDisplayName(group)) }}
            </div>

            <!-- 信息 -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between">
                <span class="font-bold text-gray-900">
                  {{ getDisplayName(group) }}
                </span>
                <span class="text-xs text-gray-400">{{ formatTime(group.lastTime) }}</span>
              </div>

              <div class="text-sm text-gray-500 mt-0.5">
                {{ group.peerCompanyName || '暂无公司信息' }}
              </div>

              <!-- 标的摘要 -->
              <div class="flex items-center gap-2 mt-1.5">
                <span class="text-xs text-gray-400">
                  {{ getSubjectSummary(group) }}
                </span>
              </div>

              <!-- 最后消息预览 -->
              <p v-if="group.lastContent" class="text-xs text-gray-400 mt-1 truncate">
                {{ group.lastContent }}
              </p>
            </div>

            <!-- 右侧 -->
            <div class="flex flex-col items-end gap-2 shrink-0">
              <!-- 未读数 -->
              <span
                v-if="group.totalUnread > 0"
                class="px-2 py-0.5 text-xs font-bold text-white bg-red-500 rounded-full"
              >
                {{ group.totalUnread > 99 ? '99+' : group.totalUnread }}
              </span>
              <!-- 箭头 -->
              <ChevronRight class="w-5 h-5 text-gray-300 group-hover:text-brand-500 transition-colors" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
