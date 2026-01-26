<script setup lang="ts">
/**
 * ConversationSidebar - 按商户聚合的会话侧边栏
 * 支持三种状态: expanded(展开), mini(收起), hidden(隐藏)
 */
import { ref, computed } from 'vue'
import {
  Search,
  ChevronLeft,
  ChevronRight,
  Package
} from 'lucide-vue-next'
import type { MerchantGroup } from '../../composables/useNegotiationWorkspace'

const props = defineProps<{
  state: 'expanded' | 'mini' | 'hidden'
  merchantGroups: MerchantGroup[]
  currentPeerId?: number
}>()

const emit = defineEmits<{
  (e: 'update:state', state: 'expanded' | 'mini' | 'hidden'): void
  (e: 'select', merchant: MerchantGroup): void
}>()

const searchQuery = ref('')

// 过滤后的商户列表
const filteredMerchants = computed(() => {
  if (!searchQuery.value) return props.merchantGroups
  const kw = searchQuery.value.toLowerCase()
  return props.merchantGroups.filter(m => {
    const text = `${m.peerName}${m.peerCompany || ''}${m.currentSubjectName}`.toLowerCase()
    return text.includes(kw)
  })
})

function toggleState() {
  if (props.state === 'expanded') {
    emit('update:state', 'mini')
  } else {
    emit('update:state', 'expanded')
  }
}

function getAvatarChar(name?: string): string {
  if (!name || name.length === 0) return '?'
  return (name[0] || '?').toUpperCase()
}

function formatTime(time?: string): string {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  if (d.toDateString() === now.toDateString()) {
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return `${d.getMonth() + 1}/${d.getDate()}`
}
</script>

<template>
  <transition name="sidebar">
    <aside
      v-show="state !== 'hidden'"
      :class="[
        'sidebar flex flex-col bg-white border-r border-gray-200 transition-all duration-300 shrink-0',
        state === 'expanded' ? 'w-56' : 'w-14'
      ]"
    >
      <!-- 搜索栏（仅展开时显示） -->
      <div v-if="state === 'expanded'" class="p-2.5 border-b border-gray-100">
        <div class="relative">
          <Search class="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-gray-400" />
          <input
            v-model="searchQuery"
            placeholder="搜索商户..."
            class="w-full h-8 pl-8 pr-2 text-xs border border-gray-200 rounded-lg
                   focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500"
          />
        </div>
      </div>

      <!-- Mini 模式搜索按钮 -->
      <div v-else class="p-2 border-b border-gray-100 flex justify-center">
        <button class="p-1.5 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-50">
          <Search class="w-4 h-4" />
        </button>
      </div>

      <!-- 商户列表 -->
      <div class="flex-1 overflow-y-auto">
        <div
          v-for="merchant in filteredMerchants"
          :key="merchant.peerId"
          :class="[
            'cursor-pointer transition-colors border-b border-gray-50',
            currentPeerId === merchant.peerId
              ? 'bg-brand-50 border-l-2 border-l-brand-500'
              : 'hover:bg-gray-50 border-l-2 border-l-transparent'
          ]"
          @click="$emit('select', merchant)"
        >
          <!-- 展开模式 -->
          <template v-if="state === 'expanded'">
            <div class="p-2.5 flex items-start gap-2.5">
              <div class="w-9 h-9 rounded-full bg-brand-500 text-white flex items-center justify-center font-bold text-sm shrink-0">
                {{ getAvatarChar(merchant.peerName) }}
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between">
                  <span class="font-medium text-sm text-gray-900 truncate">
                    {{ merchant.peerName }}
                  </span>
                  <span class="text-[10px] text-gray-400 shrink-0 ml-1">
                    {{ formatTime(merchant.lastTime) }}
                  </span>
                </div>
                <div v-if="merchant.peerCompany" class="text-[10px] text-gray-400 truncate">
                  {{ merchant.peerCompany }}
                </div>
                <div class="flex items-center gap-1 mt-0.5">
                  <Package class="w-3 h-3 text-brand-400" />
                  <span class="text-xs text-brand-600 truncate font-medium">
                    {{ merchant.currentSubjectName }}
                  </span>
                </div>
                <div class="text-[11px] text-gray-400 truncate mt-0.5">
                  {{ merchant.lastContent || '暂无消息' }}
                </div>
              </div>
              <div
                v-if="merchant.totalUnread"
                class="w-4 h-4 bg-red-500 text-white text-[9px] rounded-full flex items-center justify-center font-bold shrink-0"
              >
                {{ merchant.totalUnread > 9 ? '9+' : merchant.totalUnread }}
              </div>
            </div>
          </template>

          <!-- Mini 模式 -->
          <template v-else>
            <div class="p-1.5 flex flex-col items-center">
              <div class="relative">
                <div class="w-9 h-9 rounded-full bg-brand-500 text-white flex items-center justify-center font-bold text-sm">
                  {{ getAvatarChar(merchant.peerName) }}
                </div>
                <div
                  v-if="merchant.totalUnread"
                  class="absolute -top-0.5 -right-0.5 w-3.5 h-3.5 bg-red-500 text-white text-[8px] rounded-full flex items-center justify-center font-bold"
                >
                  {{ merchant.totalUnread > 9 ? '!' : merchant.totalUnread }}
                </div>
              </div>
            </div>
          </template>
        </div>

        <!-- 空状态 -->
        <div v-if="filteredMerchants.length === 0" class="p-4 text-center text-xs text-gray-400">
          暂无会话
        </div>
      </div>

      <!-- 底部操作 -->
      <div class="p-2 border-t border-gray-200 flex items-center justify-center">
        <button
          class="h-8 w-8 flex items-center justify-center text-gray-400 hover:text-gray-600 hover:bg-gray-50 rounded-lg transition-colors"
          @click="toggleState"
        >
          <ChevronLeft v-if="state === 'expanded'" class="w-3.5 h-3.5" />
          <ChevronRight v-else class="w-3.5 h-3.5" />
        </button>
      </div>
    </aside>
  </transition>
</template>

<style scoped>
.sidebar-enter-active,
.sidebar-leave-active {
  transition: all 0.3s ease;
}

.sidebar-enter-from,
.sidebar-leave-to {
  transform: translateX(-100%);
  opacity: 0;
}
</style>
