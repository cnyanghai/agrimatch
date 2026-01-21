<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './store/auth'
import { useUiStore } from './store/ui'
import AuthDialog from './components/AuthDialog.vue'
import {
  HomeFilled, Management, Search,
  MapLocation, DocumentChecked, ChatDotRound,
  User, SwitchButton, ArrowDown, Check, Plus,
  Star, DocumentAdd
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const ui = useUiStore()

const minimal = computed(() => Boolean(route.meta.minimal))
const isLoggedIn = computed(() => Boolean(auth.me))
const showProfileGuide = ref(false)
const profileGuideChecked = ref(false)

// 检查用户是否需要完善资料
const needCompleteProfile = computed(() => {
  if (!auth.me) return false
  // 未设置昵称
  return !auth.me.nickName
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

function goToProfile() {
  showProfileGuide.value = false
  router.push('/profile')
}

function skipProfileGuide() {
  showProfileGuide.value = false
}

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
})
</script>

<template>
  <div v-if="minimal">
    <router-view />
    <AuthDialog />
  </div>

  <div v-else class="h-full flex bg-neutral-50">
    <!-- Sidebar -->
    <aside v-if="isLoggedIn" class="hidden md:flex w-60 flex-col bg-white shadow-modern">
      <div
        class="px-5 py-4 border-b border-neutral-100 flex items-center gap-3 cursor-pointer hover:bg-gray-50/50 transition-all"
        @click="go('/')"
      >
        <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-brand-600 to-brand-700 flex items-center justify-center font-bold text-white shadow-md">A</div>
        <div class="leading-tight">
          <div class="font-bold text-lg text-neutral-800">AgriMatch</div>
          <div class="text-xs text-neutral-500">农汇通 · 供需匹配平台</div>
        </div>
      </div>

      <!-- 用户信息 -->
      <div class="px-5 py-3 border-b border-neutral-100">
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 rounded-full flex items-center justify-center text-white bg-brand-600 font-bold text-sm">
            {{ (auth.me?.nickName || auth.me?.userName || 'U')[0].toUpperCase() }}
          </div>
          <div>
            <div class="font-medium text-sm text-neutral-800">{{ auth.me?.nickName || auth.me?.userName || '未设置昵称' }}</div>
            <div class="text-xs text-neutral-500">平台用户</div>
          </div>
        </div>
      </div>

      <!-- 核心功能（8大模块） -->
      <div class="px-3 py-2 flex-1">
        <nav class="space-y-1">
          <!-- 1. 控制台首页 -->
          <button class="w-full text-left px-4 py-3 rounded-lg transition-all hover:bg-brand-50 text-neutral-700 hover:text-brand-700 flex items-center gap-3"
                  :class="route.path==='/console' ? 'bg-brand-50 text-brand-700 font-medium' : ''"
                  @click="go('/console')">
            <HomeFilled class="h-5 w-5" />
            控制台首页
          </button>

          <!-- 2. 发布信息 -->
          <button class="w-full text-left px-4 py-3 rounded-lg transition-all hover:bg-brand-50 text-neutral-700 hover:text-brand-700 flex items-center gap-3"
                  :class="route.path==='/console/publish' ? 'bg-brand-50 text-brand-700 font-medium' : ''"
                  @click="go('/console/publish')">
            <DocumentAdd class="h-5 w-5" />
            发布信息
          </button>

          <!-- 3. 关注列表 -->
          <button class="w-full text-left px-4 py-3 rounded-lg transition-all hover:bg-brand-50 text-neutral-700 hover:text-brand-700 flex items-center gap-3"
                  :class="route.path==='/console/following' ? 'bg-brand-50 text-brand-700 font-medium' : ''"
                  @click="go('/console/following')">
            <Star class="h-5 w-5" />
            关注列表
          </button>

          <!-- 4. 地图找商 -->
          <button class="w-full text-left px-4 py-3 rounded-lg transition-all hover:bg-brand-50 text-neutral-700 hover:text-brand-700 flex items-center gap-3"
                  :class="route.path==='/map' ? 'bg-brand-50 text-brand-700 font-medium' : ''"
                  @click="go('/map')">
            <MapLocation class="h-5 w-5" />
            地图找商
          </button>

          <!-- 5. 聊天议价 -->
          <button class="w-full text-left px-4 py-3 rounded-lg transition-all hover:bg-brand-50 text-neutral-700 hover:text-brand-700 flex items-center gap-3"
                  :class="route.path==='/chat' || route.path==='/notify' ? 'bg-brand-50 text-brand-700 font-medium' : ''"
                  @click="go('/chat')">
            <ChatDotRound class="h-5 w-5" />
            聊天议价
          </button>

          <!-- 6. 合同管理 -->
          <button class="w-full text-left px-4 py-3 rounded-lg transition-all hover:bg-brand-50 text-neutral-700 hover:text-brand-700 flex items-center gap-3"
                  :class="route.path.startsWith('/contracts') ? 'bg-brand-50 text-brand-700 font-medium' : ''"
                  @click="go('/contracts')">
            <DocumentChecked class="h-5 w-5" />
            合同管理
          </button>

          <!-- 7. 用户资料 -->
          <button class="w-full text-left px-4 py-3 rounded-lg transition-all hover:bg-brand-50 text-neutral-700 hover:text-brand-700 flex items-center gap-3"
                  :class="route.path==='/profile' ? 'bg-brand-50 text-brand-700 font-medium' : ''"
                  @click="go('/profile')">
            <User class="h-5 w-5" />
            用户资料
          </button>

          <!-- 8. 会员积分 -->
          <button class="w-full text-left px-4 py-3 rounded-lg transition-all hover:bg-brand-50 text-neutral-700 hover:text-brand-700 flex items-center gap-3"
                  :class="route.path.startsWith('/points') ? 'bg-brand-50 text-brand-700 font-medium' : ''"
                  @click="go('/points')">
            <Coin class="h-5 w-5" />
            会员积分
          </button>
        </nav>
      </div>

      <!-- 底部区域 -->
      <div class="px-3 py-2 mt-auto border-t border-neutral-100">
        <nav class="space-y-1">
          <button class="w-full text-left px-4 py-3 rounded-xl transition-all hover:bg-red-50 text-neutral-700 hover:text-red-600 flex items-center gap-3"
                  @click="logout">
            <SwitchButton class="h-5 w-5" />
            退出登录
          </button>
        </nav>
      </div>
    </aside>

    <!-- Main -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- Topbar -->
      <header class="bg-white border-b border-neutral-200 shadow-sm">
        <div class="px-4 py-3 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="md:hidden w-10 h-10 rounded-lg bg-gradient-to-br from-brand-600 to-brand-700 text-white flex items-center justify-center font-bold shadow-md">A</div>
            <div class="font-bold text-neutral-800">农汇通 AgriMatch</div>
            <span class="hidden sm:inline text-xs text-neutral-500">供需匹配平台</span>
          </div>

          <div class="flex items-center gap-3">
            <!-- 用户下拉菜单 -->
            <el-dropdown>
              <el-button class="!rounded-lg">
                {{ auth.me?.nickName || auth.me?.userName || '未登录' }}
                <el-icon class="el-icon--right">
                  <ArrowDown />
                </el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="auth.me" @click="go('/console')">
                    <User class="w-4 h-4 mr-2" />我的账户
                  </el-dropdown-item>
                  <el-dropdown-item v-if="!auth.me" @click="ui.openAuthDialog('login')">去登录/注册</el-dropdown-item>
                  <el-dropdown-item v-if="auth.me" divided @click="logout">
                    <SwitchButton class="w-4 h-4 mr-2" />注销
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </header>

      <main class="flex-1 overflow-auto p-4 md:p-6">
        <router-view />
      </main>
    </div>

    <!-- 资料完善引导弹窗 -->
    <el-dialog
      v-model="showProfileGuide"
      title=""
      width="440px"
      :show-close="false"
      :close-on-click-modal="false"
      center
    >
      <div class="text-center py-4">
        <div class="w-20 h-20 mx-auto mb-4 rounded-full bg-brand-50 flex items-center justify-center">
          <User class="w-10 h-10 text-brand-600" />
        </div>
        <h3 class="text-xl font-bold text-gray-800 mb-2">完善您的资料</h3>
        <p class="text-gray-500 mb-6">
          完善个人信息可以帮助您获得更精准的匹配推荐，<br/>
          提升交易成功率
        </p>
        
        <div class="space-y-3 text-left mb-6 px-4">
          <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
            <div class="w-8 h-8 rounded-full flex items-center justify-center"
                 :class="auth.me?.nickName ? 'bg-green-100 text-green-600' : 'bg-gray-200 text-gray-400'">
              <Check v-if="auth.me?.nickName" class="w-4 h-4" />
              <Plus v-else class="w-4 h-4" />
            </div>
            <span :class="auth.me?.nickName ? 'text-gray-800' : 'text-gray-500'">设置昵称</span>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="flex gap-3 justify-center">
          <el-button @click="skipProfileGuide">稍后完善</el-button>
          <el-button type="primary" @click="goToProfile">立即完善</el-button>
        </div>
      </template>
    </el-dialog>

    <AuthDialog />
  </div>
</template>

<style scoped>
/* 主体使用 Tailwind */
</style>
