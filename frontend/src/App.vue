<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './store/auth'
import AuthDialog from './components/AuthDialog.vue'
import { 
  ShoppingCart, Box, HomeFilled, Management, Search, 
  MapLocation, DocumentChecked, ChatDotRound, Postcard, 
  Bell, Coin, User, SwitchButton, ArrowDown, Check, Plus
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const minimal = computed(() => Boolean(route.meta.minimal))
const showProfileGuide = ref(false)
const profileGuideChecked = ref(false)

// 检查用户是否需要完善资料
const needCompleteProfile = computed(() => {
  if (!auth.me) return false
  // 未设置昵称或未设置身份
  return !auth.me.nickName || (auth.me.isBuyer !== 1 && auth.me.isSeller !== 1)
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

// 用户身份
const isBuyer = computed(() => auth.me?.isBuyer === 1)
const isSeller = computed(() => auth.me?.isSeller === 1)
const userTypeLabel = computed(() => {
  if (isBuyer.value) return '采购商'
  if (isSeller.value) return '供应商'
  return '未设置'
})
const userTypeColor = computed(() => isBuyer.value ? 'success' : 'warning')

function go(path: string) {
  router.push(path)
}

function logout() {
  auth.clear()
  go('/login')
}
</script>

<template>
  <div v-if="minimal">
    <router-view />
    <AuthDialog />
  </div>

  <div v-else class="h-full flex bg-neutral-50">
    <!-- Sidebar -->
    <aside v-if="auth.token" class="hidden md:flex w-60 flex-col bg-white shadow-modern">
      <div
        class="px-5 py-4 border-b border-neutral-100 flex items-center gap-3 cursor-pointer hover:bg-gray-50/50 transition-all active:scale-[0.99]"
        @click="go('/')"
      >
        <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-600 to-blue-700 flex items-center justify-center font-bold text-white shadow-lg shadow-blue-200">A</div>
        <div class="leading-tight">
          <div class="font-bold text-lg text-neutral-800">AgriMatch</div>
          <div class="text-xs text-neutral-500">农汇通 · 供需匹配平台</div>
        </div>
      </div>

      <!-- 用户身份标识 - 统一蓝色调 -->
      <div class="px-5 py-3 border-b border-neutral-100">
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 rounded-full flex items-center justify-center text-white bg-blue-600">
            <ShoppingCart v-if="isBuyer" class="h-4 w-4" />
            <Box v-else class="h-4 w-4" />
          </div>
          <div>
            <div class="font-medium text-sm text-neutral-800">{{ auth.me?.nickName || '未设置昵称' }}</div>
            <div class="text-xs text-blue-600">{{ userTypeLabel }}</div>
          </div>
        </div>
      </div>

      <!-- 核心业务 -->
      <div class="px-3 py-2">
        <div class="text-xs font-semibold text-neutral-400 uppercase tracking-wider px-3 py-2">核心业务</div>
        <nav class="space-y-1">
          <!-- 首页 - 所有用户 -->
          <button class="w-full text-left px-4 py-3 rounded-xl transition-all hover:bg-blue-50 text-neutral-700 hover:text-blue-700 flex items-center gap-3"
                  :class="route.path==='/console' ? 'bg-blue-50 text-blue-700 font-medium' : ''"
                  @click="go('/console')">
            <HomeFilled class="h-5 w-5" />
            首页
          </button>

          <!-- 采购商菜单 -->
          <template v-if="isBuyer">
            <button class="w-full text-left px-4 py-3 rounded-xl transition-all hover:bg-blue-50 text-neutral-700 hover:text-blue-700 flex items-center gap-3"
                    :class="route.path==='/requirements' ? 'bg-blue-50 text-blue-700 font-medium' : ''"
                    @click="go('/requirements')">
              <Management class="h-5 w-5" />
              采购管理
            </button>
            <button class="w-full text-left px-4 py-3 rounded-xl transition-all hover:bg-blue-50 text-neutral-700 hover:text-blue-700 flex items-center gap-3"
                    :class="route.path==='/supply-browse' ? 'bg-blue-50 text-blue-700 font-medium' : ''"
                    @click="go('/supply-browse')">
              <Search class="h-5 w-5" />
              供应浏览
            </button>
          </template>

          <!-- 供应商菜单 -->
          <template v-if="isSeller">
            <button class="w-full text-left px-4 py-3 rounded-xl transition-all hover:bg-blue-50 text-neutral-700 hover:text-blue-700 flex items-center gap-3"
                    :class="route.path==='/supply' ? 'bg-blue-50 text-blue-700 font-medium' : ''"
                    @click="go('/supply')">
              <Box class="h-5 w-5" />
              供应管理
            </button>
            <button class="w-full text-left px-4 py-3 rounded-xl transition-all hover:bg-blue-50 text-neutral-700 hover:text-blue-700 flex items-center gap-3"
                    :class="route.path==='/requirement-browse' ? 'bg-blue-50 text-blue-700 font-medium' : ''"
                    @click="go('/requirement-browse')">
              <Search class="h-5 w-5" />
              采购浏览
            </button>
          </template>

          <!-- 地图找商 - 所有用户 -->
          <button class="w-full text-left px-4 py-3 rounded-xl transition-all hover:bg-blue-50 text-neutral-700 hover:text-blue-700 flex items-center gap-3"
                  :class="route.path==='/map' ? 'bg-blue-50 text-blue-700 font-medium' : ''"
                  @click="go('/map')">
            <MapLocation class="h-5 w-5" />
            地图找商
          </button>

          <!-- 合同管理 - 所有用户 -->
          <button class="w-full text-left px-4 py-3 rounded-xl transition-all hover:bg-blue-50 text-neutral-700 hover:text-blue-700 flex items-center gap-3"
                  :class="route.path==='/contracts' ? 'bg-blue-50 text-blue-700 font-medium' : ''"
                  @click="go('/contracts')">
            <DocumentChecked class="h-5 w-5" />
            合同管理
          </button>
        </nav>
      </div>

      <!-- 社区互动 -->
      <div class="px-3 py-2">
        <div class="text-xs font-semibold text-neutral-400 uppercase tracking-wider px-3 py-2">社区互动</div>
        <nav class="space-y-1">
          <button class="w-full text-left px-4 py-3 rounded-xl transition-all hover:bg-blue-50 text-neutral-700 hover:text-blue-700 flex items-center gap-3"
                  :class="route.path==='/chat' ? 'bg-blue-50 text-blue-700 font-medium' : ''"
                  @click="go('/chat')">
            <ChatDotRound class="h-5 w-5" />
            商务聊天
          </button>
          <button class="w-full text-left px-4 py-3 rounded-xl transition-all hover:bg-blue-50 text-neutral-700 hover:text-blue-700 flex items-center gap-3"
                  :class="route.path==='/posts' ? 'bg-blue-50 text-blue-700 font-medium' : ''"
                  @click="go('/posts')">
            <Postcard class="h-5 w-5" />
            社区论坛
          </button>
        </nav>
      </div>

      <!-- 个人中心 -->
      <div class="px-3 py-2">
        <div class="text-xs font-semibold text-neutral-400 uppercase tracking-wider px-3 py-2">个人中心</div>
        <nav class="space-y-1">
          <button class="w-full text-left px-4 py-3 rounded-xl transition-all hover:bg-blue-50 text-neutral-700 hover:text-blue-700 flex items-center gap-3"
                  :class="route.path==='/notify' ? 'bg-blue-50 text-blue-700 font-medium' : ''"
                  @click="go('/notify')">
            <Bell class="h-5 w-5" />
            消息中心
          </button>
          <button class="w-full text-left px-4 py-3 rounded-xl transition-all hover:bg-blue-50 text-neutral-700 hover:text-blue-700 flex items-center gap-3"
                  :class="route.path==='/points' ? 'bg-blue-50 text-blue-700 font-medium' : ''"
                  @click="go('/points')">
            <Coin class="h-5 w-5" />
            我的积分
          </button>
          <button class="w-full text-left px-4 py-3 rounded-xl transition-all hover:bg-blue-50 text-neutral-700 hover:text-blue-700 flex items-center gap-3"
                  :class="route.path==='/profile' ? 'bg-blue-50 text-blue-700 font-medium' : ''"
                  @click="go('/profile')">
            <User class="h-5 w-5" />
            个人中心
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
            <div class="md:hidden w-10 h-10 rounded-xl bg-gradient-to-br from-blue-600 to-blue-700 text-white flex items-center justify-center font-bold shadow-lg shadow-blue-200">A</div>
            <div class="font-bold text-neutral-800">农汇通 AgriMatch</div>
            <span class="hidden sm:inline text-xs text-neutral-500">供需匹配平台</span>
          </div>

          <div class="flex items-center gap-3">
            <!-- 用户身份标签 -->
            <el-tag :type="userTypeColor" effect="light" class="!rounded-lg">
              {{ userTypeLabel }}
            </el-tag>
            
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
                  <el-dropdown-item v-if="auth.me" @click="go('/profile')">个人中心</el-dropdown-item>
                  <el-dropdown-item v-if="!auth.me" @click="go('/login')">去登录/注册</el-dropdown-item>
                  <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
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
        <div class="w-20 h-20 mx-auto mb-4 rounded-full bg-blue-100 flex items-center justify-center">
          <User class="w-10 h-10 text-blue-600" />
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
          
          <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
            <div class="w-8 h-8 rounded-full flex items-center justify-center"
                 :class="(auth.me?.isBuyer === 1 || auth.me?.isSeller === 1) ? 'bg-green-100 text-green-600' : 'bg-gray-200 text-gray-400'">
              <Check v-if="auth.me?.isBuyer === 1 || auth.me?.isSeller === 1" class="w-4 h-4" />
              <Plus v-else class="w-4 h-4" />
            </div>
            <span :class="(auth.me?.isBuyer === 1 || auth.me?.isSeller === 1) ? 'text-gray-800' : 'text-gray-500'">选择身份类型</span>
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
