<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { useNotificationStore } from '../stores/notification'
import {
  LayoutDashboard,
  Search,
  FilePlus,
  MessageSquare,
  User
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const notificationStore = useNotificationStore()

const unreadCount = computed(() => notificationStore.unreadTotal)

const tabs = computed(() => [
  {
    key: 'console',
    label: '工作台',
    icon: LayoutDashboard,
    path: '/console',
    match: (p: string) => p === '/console',
    badge: 0,
    show: true
  },
  {
    key: 'search',
    label: '找货',
    icon: Search,
    path: '/hall/supply',
    match: (p: string) => p.startsWith('/hall') || p === '/search',
    badge: 0,
    show: true
  },
  {
    key: 'publish',
    label: '发布',
    icon: FilePlus,
    path: '/console/publish',
    match: (p: string) => p === '/console/publish',
    badge: 0,
    show: !auth.isFilingMode,
    accent: true
  },
  {
    key: 'chat',
    label: '消息',
    icon: MessageSquare,
    path: '/chat',
    match: (p: string) => p === '/chat' || p === '/notify',
    badge: unreadCount.value,
    show: !auth.isFilingMode
  },
  {
    key: 'me',
    label: '我的',
    icon: User,
    path: '/profile',
    match: (p: string) => p === '/profile' || p.startsWith('/points'),
    badge: 0,
    show: true
  }
])

const visibleTabs = computed(() => tabs.value.filter(t => t.show))

function isActive(tab: typeof tabs.value[0]) {
  return tab.match(route.path)
}

function go(path: string) {
  router.push(path)
}
</script>

<template>
  <nav class="md:hidden fixed bottom-0 left-0 right-0 z-50 bg-white border-t border-neutral-200 safe-bottom">
    <div class="flex items-center justify-around h-14 px-1">
      <button
        v-for="tab in visibleTabs"
        :key="tab.key"
        class="relative flex flex-col items-center justify-center flex-1 h-full transition-colors"
        :class="[
          isActive(tab)
            ? 'text-brand-600'
            : 'text-neutral-400 active:text-neutral-600',
          tab.accent && !isActive(tab) ? 'text-brand-500' : ''
        ]"
        @click="go(tab.path)"
      >
        <!-- Accent button (publish) -->
        <div
          v-if="tab.accent"
          class="relative -mt-3 w-11 h-11 rounded-xl flex items-center justify-center transition-all"
          :class="isActive(tab) ? 'bg-brand-600 shadow-brand' : 'bg-brand-500 shadow-md'"
        >
          <component :is="tab.icon" class="w-5 h-5 text-white" :stroke-width="2" />
        </div>

        <!-- Normal button -->
        <template v-else>
          <div class="relative">
            <component :is="tab.icon" class="w-5 h-5" :stroke-width="isActive(tab) ? 2.5 : 1.8" />
            <span
              v-if="tab.badge > 0"
              class="absolute -top-1.5 -right-2.5 min-w-[16px] h-4 bg-error-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center px-1"
            >
              {{ tab.badge > 99 ? '99+' : tab.badge }}
            </span>
          </div>
        </template>

        <span
          class="text-[10px] mt-0.5 font-medium"
          :class="tab.accent ? 'text-brand-600' : ''"
        >
          {{ tab.label }}
        </span>
      </button>
    </div>
  </nav>
</template>

<style scoped>
.safe-bottom {
  padding-bottom: env(safe-area-inset-bottom, 0);
}
</style>
