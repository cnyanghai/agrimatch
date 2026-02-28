<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { useUiStore } from '../store/ui'
import { useNotificationStore } from '../stores/notification'
import {
  X, LayoutDashboard, FilePlus, Star, Map,
  MessageSquare, FileCheck, User, LogOut, Coins,
  ShieldCheck, Package, ShoppingBag, Users2,
  Search, Newspaper
} from 'lucide-vue-next'

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{ (e: 'close'): void }>()

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const ui = useUiStore()
const notificationStore = useNotificationStore()

const isLoggedIn = computed(() => Boolean(auth.me))
const displayName = computed(() => auth.me?.nickName || auth.me?.userName || '未登录')
const avatarChar = computed(() => {
  const name = (auth.me?.nickName || auth.me?.userName || '').trim()
  if (!name) return 'U'
  if (/^\d+$/.test(name)) return 'U'
  return (name[0] || 'U').toUpperCase()
})
const unreadCount = computed(() => notificationStore.unreadTotal)

// Public navigation items
const publicNav = [
  { label: '供应大厅', icon: Package, path: '/hall/supply' },
  { label: '采购大厅', icon: ShoppingBag, path: '/hall/need' },
  { label: '话题广场', icon: Newspaper, path: '/talks' },
  { label: '全站搜索', icon: Search, path: '/search' },
  { label: '企业名录', icon: Users2, path: '/companies/directory' },
]

// Console navigation items (logged in)
const consoleNav = computed(() => {
  const items = [
    { label: '控制台首页', icon: LayoutDashboard, path: '/console', show: true },
    { label: '发布信息', icon: FilePlus, path: '/console/publish', show: !auth.isFilingMode },
    { label: '关注列表', icon: Star, path: '/console/following', show: true },
    { label: '地图找商', icon: Map, path: '/map', show: true },
    { label: '聊天议价', icon: MessageSquare, path: '/chat', show: !auth.isFilingMode, badge: unreadCount.value },
    { label: '合同管理', icon: FileCheck, path: '/contracts', show: !auth.isFilingMode },
    { label: '用户资料', icon: User, path: '/profile', show: true },
    { label: '会员积分', icon: Coins, path: '/points', show: !auth.isFilingMode },
    { label: '管理后台', icon: ShieldCheck, path: '/admin', show: auth.isAdmin && !auth.isFilingMode },
  ]
  return items.filter(i => i.show)
})

function go(path: string) {
  router.push(path)
  emit('close')
}

function openLogin() {
  ui.openAuthDialog('login', { path: route.path, query: route.query as any })
  emit('close')
}

async function logout() {
  await auth.logout()
  emit('close')
  router.push('/')
}

function isActive(path: string) {
  if (path === '/console') return route.path === '/console'
  return route.path.startsWith(path)
}

// Close drawer on route change
watch(() => route.path, () => {
  emit('close')
})
</script>

<template>
  <Teleport to="body">
    <!-- Backdrop -->
    <Transition name="fade">
      <div
        v-if="visible"
        class="fixed inset-0 bg-black/40 z-[100] backdrop-blur-sm"
        @click="emit('close')"
      />
    </Transition>

    <!-- Drawer -->
    <Transition name="slide-left">
      <div
        v-if="visible"
        class="fixed inset-y-0 left-0 w-72 max-w-[85vw] bg-white z-[101] flex flex-col shadow-2xl safe-all"
      >
        <!-- Header -->
        <div class="flex items-center justify-between px-5 py-4 border-b border-neutral-100">
          <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-xl bg-brand-600 flex items-center justify-center">
              <span class="text-white font-bold text-sm">沃谷</span>
            </div>
            <span class="text-lg font-bold text-neutral-900">沃谷</span>
          </div>
          <button
            class="p-2 rounded-lg hover:bg-neutral-100 text-neutral-400 transition-colors"
            @click="emit('close')"
          >
            <X class="w-5 h-5" />
          </button>
        </div>

        <!-- User Section -->
        <div v-if="isLoggedIn" class="px-5 py-4 border-b border-neutral-100 bg-neutral-50/50">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-xl bg-brand-100 text-brand-700 flex items-center justify-center font-bold text-sm">
              <img v-if="auth.me?.avatar" :src="auth.me.avatar" alt="" class="w-full h-full rounded-xl object-cover" />
              <span v-else>{{ avatarChar }}</span>
            </div>
            <div class="flex-1 min-w-0">
              <div class="text-sm font-semibold text-neutral-900 truncate">{{ displayName }}</div>
              <div class="text-xs text-neutral-400">{{ auth.me?.phonenumber || '已登录' }}</div>
            </div>
          </div>
        </div>

        <!-- Navigation -->
        <div class="flex-1 overflow-y-auto py-3">
          <!-- Public Section -->
          <div class="px-3 mb-3">
            <div class="px-3 py-1.5 text-[10px] font-semibold text-neutral-400 uppercase tracking-widest">发现</div>
            <nav class="space-y-0.5">
              <button
                v-for="item in publicNav"
                :key="item.path"
                class="w-full text-left px-3 py-2.5 rounded-lg transition-all flex items-center gap-3 text-sm"
                :class="isActive(item.path)
                  ? 'bg-brand-50 text-brand-700 font-medium'
                  : 'text-neutral-600 hover:bg-neutral-50'"
                @click="go(item.path)"
              >
                <component :is="item.icon" class="w-[18px] h-[18px] shrink-0" :stroke-width="2" />
                {{ item.label }}
              </button>
            </nav>
          </div>

          <!-- Console Section (logged in only) -->
          <div v-if="isLoggedIn" class="px-3">
            <div class="px-3 py-1.5 text-[10px] font-semibold text-neutral-400 uppercase tracking-widest">控制台</div>
            <nav class="space-y-0.5">
              <button
                v-for="item in consoleNav"
                :key="item.path"
                class="w-full text-left px-3 py-2.5 rounded-lg transition-all flex items-center gap-3 text-sm"
                :class="isActive(item.path)
                  ? 'bg-brand-50 text-brand-700 font-medium'
                  : 'text-neutral-600 hover:bg-neutral-50'"
                @click="go(item.path)"
              >
                <div class="relative">
                  <component :is="item.icon" class="w-[18px] h-[18px] shrink-0" :stroke-width="2" />
                  <span
                    v-if="item.badge && item.badge > 0"
                    class="absolute -top-1 -right-1.5 min-w-[14px] h-3.5 bg-error-500 text-white text-[9px] font-bold rounded-full flex items-center justify-center px-0.5"
                  >
                    {{ item.badge > 99 ? '99+' : item.badge }}
                  </span>
                </div>
                {{ item.label }}
              </button>
            </nav>
          </div>
        </div>

        <!-- Footer -->
        <div class="px-5 py-4 border-t border-neutral-100">
          <button
            v-if="isLoggedIn"
            class="w-full text-left px-3 py-2.5 rounded-lg text-neutral-500 hover:text-error-600 hover:bg-error-50 flex items-center gap-3 text-sm transition-colors"
            @click="logout"
          >
            <LogOut class="w-[18px] h-[18px]" :stroke-width="2" />
            退出登录
          </button>
          <button
            v-else
            class="w-full bg-brand-600 text-white py-3 rounded-xl font-semibold text-sm hover:bg-brand-700 transition-colors"
            @click="openLogin"
          >
            登录 / 注册
          </button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-left-enter-active,
.slide-left-leave-active {
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-left-enter-from,
.slide-left-leave-to {
  transform: translateX(-100%);
}
</style>
