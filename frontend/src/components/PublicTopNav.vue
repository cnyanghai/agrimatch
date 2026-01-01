<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { useUiStore } from '../store/ui'
import { LogOut, LayoutDashboard, Settings } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const ui = useUiStore()

const isLoggedIn = computed(() => Boolean(auth.me))
const displayName = computed(() => auth.me?.nickName || auth.me?.userName || '未登录')
const avatarChar = computed(() => {
  const name = (auth.me?.nickName || auth.me?.userName || '').trim()
  if (!name) return 'U'
  // 若是纯数字账号（手机号等），不要显示“1/2/3”，统一兜底 U
  if (/^\d+$/.test(name)) return 'U'
  return (name[0] || 'U').toUpperCase()
})

function go(path: string) {
  router.push(path)
}

function openLogin() {
  ui.openAuthDialog('login', { path: route.path, query: route.query as any })
}

async function onLogout() {
  await auth.logout()
  go('/')
}
</script>

<template>
  <nav class="bg-white border-b sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="h-16 flex items-center justify-between gap-6">
        <!-- Left: Logo -->
        <div class="flex items-center gap-3 cursor-pointer hover:bg-gray-50/50 px-2 py-1 rounded-xl transition-all active:scale-[0.99]" @click="go('/')">
          <div class="w-9 h-9 rounded-xl bg-emerald-600 text-white flex items-center justify-center font-black">A</div>
          <div class="leading-tight">
            <div class="font-bold text-gray-900">AgriMatch</div>
            <div class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">农汇通 · 供需匹配平台</div>
          </div>
        </div>

        <!-- Middle: Nav -->
        <div class="hidden md:flex items-center gap-5 text-sm font-bold text-gray-500">
          <button class="hover:text-emerald-600 transition-all active:scale-95" @click="go('/')">首页</button>
          <button class="hover:text-emerald-600 transition-all active:scale-95" @click="go('/hall/supply')">供应大厅</button>
          <button class="hover:text-emerald-600 transition-all active:scale-95" @click="go('/hall/need')">采购大厅</button>
          <button class="hover:text-emerald-600 transition-all active:scale-95" @click="go('/insights')">观点资讯</button>
          <button class="hover:text-emerald-600 transition-all active:scale-95" @click="go('/talks')">话题广场</button>
        </div>

        <!-- Right: User area -->
        <div class="flex items-center gap-3">
          <slot name="actions" />
          <template v-if="!isLoggedIn">
            <button class="bg-emerald-600 text-white px-5 py-2 rounded-full font-bold hover:bg-emerald-700 transition-all active:scale-95" @click="openLogin">
              登录/注册
            </button>
          </template>
          <template v-else>
            <el-dropdown>
              <button class="flex items-center gap-3 bg-gray-50 border border-gray-100 px-3 py-2 rounded-full hover:bg-gray-50/50 transition-all active:scale-95">
                <div class="w-8 h-8 rounded-full bg-emerald-600 text-white flex items-center justify-center font-bold">
                  {{ avatarChar }}
                </div>
                <div class="text-left leading-tight">
                  <div class="text-sm font-bold text-gray-900">{{ displayName }}</div>
                  <div class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">已登录</div>
                </div>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="go('/console')">
                    <div class="flex items-center gap-2">
                      <LayoutDashboard :size="16" :stroke-width="2" />
                      控制台
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item @click="go('/profile')">
                    <div class="flex items-center gap-2">
                      <Settings :size="16" :stroke-width="2" />
                      个人资料
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="onLogout">
                    <div class="flex items-center gap-2 text-red-600">
                      <LogOut :size="16" :stroke-width="2" />
                      退出登录
                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>
    </div>
  </nav>
</template>


