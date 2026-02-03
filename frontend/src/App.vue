<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './store/auth'
import { useNotificationStore } from './stores/notification'
import { useGlobalWebSocket } from './composables/useGlobalWebSocket'
import { requestNotificationPermission } from './utils/browserNotification'
import { initAudioContext } from './utils/notificationSound'
import AuthDialog from './components/AuthDialog.vue'
import PublicTopNav from './components/PublicTopNav.vue'
import NotificationToast from './components/notification/NotificationToast.vue'
import {
  LayoutDashboard, FilePlus, Star, Map,
  MessageSquare, FileCheck, User, LogOut,
  Coins, ShieldCheck
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const notificationStore = useNotificationStore()

// 初始化全局 WebSocket
useGlobalWebSocket()

// 未读消息数
const unreadCount = computed(() => notificationStore.unreadTotal)

const minimal = computed(() => Boolean(route.meta.minimal))
const isLoggedIn = computed(() => Boolean(auth.me))
const hideNav = computed(() => Boolean(route.meta.hideNav))
const showProfileGuide = ref(false)
const profileGuideChecked = ref(false)

// 检查用户是否需要完善资料
const needCompleteProfile = computed(() => {
  if (!auth.me) return false
  // 未设置真实姓名
  return !auth.me.realName
})

// 监听登录状态，在登录后检查是否需要完善资料
watch(() => auth.me, (me) => {
  if (me && !profileGuideChecked.value && needCompleteProfile.value) {
    // 延迟显示，避免页面加载时立即弹出
    setTimeout(() => {
      showProfileGuide.value = true
      profileGuideChecked.value = true
    }, 1000)
  }
}, { immediate: true })

function go(path: string) {
  router.push(path)
}

function logout() {
  auth.logout()
  go('/')
}

// App 启动时尝试用 HttpOnly Cookie 恢复登录态
onMounted(async () => {
  try {
    if (!auth.me) await auth.fetchMe()
  } catch {
    // 未登录/过期：静默即可
  }

  // 请求浏览器通知权限（需要用户交互后才会生效）
  requestNotificationPermission()

  // 初始化音频上下文（首次用户交互时）
  const initAudio = () => {
    initAudioContext()
    document.removeEventListener('click', initAudio)
    document.removeEventListener('keydown', initAudio)
  }
  document.addEventListener('click', initAudio, { once: true })
  document.addEventListener('keydown', initAudio, { once: true })
})
</script>

<template>
  <div class="h-screen flex flex-col overflow-hidden bg-neutral-50">
    <!-- Global Navigation -->
    <PublicTopNav v-if="!hideNav" />

    <!-- Main Content Area -->
    <div v-if="minimal" id="main-scroll" class="flex-1 overflow-auto">
      <router-view />
    </div>

    <div v-else class="flex-1 flex overflow-hidden">
      <!-- Sidebar -->
      <aside v-if="isLoggedIn" class="hidden md:flex w-52 shrink-0 flex-col bg-white border-r border-neutral-200 overflow-y-auto">
        <!-- 核心功能（8大模块） -->
      <div class="px-3 py-2 flex-1">
        <nav class="space-y-1">
          <!-- 1. 控制台首页 -->
          <button class="w-full text-left px-4 py-2.5 rounded-lg transition-all text-neutral-600 hover:text-neutral-900 hover:bg-neutral-100 flex items-center gap-3"
                  :class="route.path==='/console' ? 'bg-brand-50 text-brand-700 font-medium border-l-2 border-brand-500' : ''"
                  @click="go('/console')">
            <LayoutDashboard class="h-5 w-5" stroke-width="2" />
            控制台首页
          </button>

          <!-- 2. 发布信息 -->
          <button v-if="!auth.isFilingMode"
                  class="w-full text-left px-4 py-2.5 rounded-lg transition-all text-neutral-600 hover:text-neutral-900 hover:bg-neutral-100 flex items-center gap-3"
                  :class="route.path==='/console/publish' ? 'bg-brand-50 text-brand-700 font-medium border-l-2 border-brand-500' : ''"
                  @click="go('/console/publish')">
            <FilePlus class="h-5 w-5" stroke-width="2" />
            发布信息
          </button>

          <!-- 3. 关注列表 -->
          <button class="w-full text-left px-4 py-2.5 rounded-lg transition-all text-neutral-600 hover:text-neutral-900 hover:bg-neutral-100 flex items-center gap-3"
                  :class="route.path==='/console/following' ? 'bg-brand-50 text-brand-700 font-medium border-l-2 border-brand-500' : ''"
                  @click="go('/console/following')">
            <Star class="h-5 w-5" stroke-width="2" />
            关注列表
          </button>

          <!-- 4. 地图找商 -->
          <button class="w-full text-left px-4 py-2.5 rounded-lg transition-all text-neutral-600 hover:text-neutral-900 hover:bg-neutral-100 flex items-center gap-3"
                  :class="route.path==='/map' ? 'bg-brand-50 text-brand-700 font-medium border-l-2 border-brand-500' : ''"
                  @click="go('/map')">
            <Map class="h-5 w-5" stroke-width="2" />
            地图找商
          </button>

          <!-- 5. 聊天议价 -->
          <button v-if="!auth.isFilingMode"
                  class="w-full text-left px-4 py-2.5 rounded-lg transition-all text-neutral-600 hover:text-neutral-900 hover:bg-neutral-100 flex items-center gap-3 relative"
                  :class="route.path==='/chat' || route.path==='/notify' ? 'bg-brand-50 text-brand-700 font-medium border-l-2 border-brand-500' : ''"
                  @click="go('/chat')">
            <div class="relative">
              <MessageSquare class="h-5 w-5" stroke-width="2" />
              <!-- 未读角标 -->
              <span
                v-if="unreadCount > 0"
                class="absolute -top-1.5 -right-1.5 min-w-[18px] h-[18px] bg-error-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center px-1"
              >
                {{ unreadCount > 99 ? '99+' : unreadCount }}
              </span>
            </div>
            聊天议价
          </button>

          <!-- 6. 合同管理 -->
          <button v-if="!auth.isFilingMode"
                  class="w-full text-left px-4 py-2.5 rounded-lg transition-all text-neutral-600 hover:text-neutral-900 hover:bg-neutral-100 flex items-center gap-3"
                  :class="route.path.startsWith('/contracts') ? 'bg-brand-50 text-brand-700 font-medium border-l-2 border-brand-500' : ''"
                  @click="go('/contracts')">
            <FileCheck class="h-5 w-5" stroke-width="2" />
            合同管理
          </button>

          <!-- 7. 用户资料 -->
          <button class="w-full text-left px-4 py-2.5 rounded-lg transition-all text-neutral-600 hover:text-neutral-900 hover:bg-neutral-100 flex items-center gap-3"
                  :class="route.path==='/profile' ? 'bg-brand-50 text-brand-700 font-medium border-l-2 border-brand-500' : ''"
                  @click="go('/profile')">
            <User class="h-5 w-5" stroke-width="2" />
            用户资料
          </button>

          <!-- 8. 会员积分 -->
          <button v-if="!auth.isFilingMode"
                  class="w-full text-left px-4 py-2.5 rounded-lg transition-all text-neutral-600 hover:text-neutral-900 hover:bg-neutral-100 flex items-center gap-3"
                  :class="route.path.startsWith('/points') ? 'bg-brand-50 text-brand-700 font-medium border-l-2 border-brand-500' : ''"
                  @click="go('/points')">
            <Coins class="h-5 w-5" stroke-width="2" />
            会员积分
          </button>

          <!-- 管理员：管理后台 -->
          <button v-if="auth.isAdmin && !auth.isFilingMode"
                  class="w-full text-left px-4 py-2.5 rounded-lg transition-all text-neutral-600 hover:text-neutral-900 hover:bg-neutral-100 flex items-center gap-3"
                  :class="route.path.startsWith('/admin') ? 'bg-brand-50 text-brand-700 font-medium border-l-2 border-brand-500' : ''"
                  @click="go('/admin')">
            <ShieldCheck class="h-5 w-5" stroke-width="2" />
            管理后台
          </button>
        </nav>
      </div>

      <!-- 底部区域 -->
      <div class="px-3 py-2 mt-auto border-t border-neutral-200">
        <nav class="space-y-1">
          <button class="w-full text-left px-4 py-2.5 rounded-xl transition-all text-neutral-500 hover:text-error-600 hover:bg-error-50 flex items-center gap-3"
                  @click="logout">
            <LogOut class="h-5 w-5" stroke-width="2" />
            退出登录
          </button>
        </nav>
      </div>
    </aside>

      <!-- Main Content -->
      <main id="main-scroll" class="flex-1 overflow-auto p-4 md:p-6 min-w-0">
        <router-view />
      </main>
    </div>

    <AuthDialog />

    <!-- 全局通知 Toast -->
    <NotificationToast />
  </div>
</template>

<style scoped>
/* 主体使用 Tailwind */
</style>
