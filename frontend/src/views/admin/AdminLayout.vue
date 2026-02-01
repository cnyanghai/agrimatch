<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import {
  LayoutDashboard, Users, Building2, ShieldAlert,
  MessageSquareText, CreditCard, Coins, ArrowLeft
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

const navItems = [
  { title: '仪表盘', icon: LayoutDashboard, path: '/admin' },
  { title: '用户管理', icon: Users, path: '/admin/users' },
  { title: '企业管理', icon: Building2, path: '/admin/companies' },
  { title: '信息审核', icon: ShieldAlert, path: '/admin/listings' },
  { title: '话题管理', icon: MessageSquareText, path: '/admin/posts' },
  { title: '京东卡管理', icon: CreditCard, path: '/admin/jd-redeems' },
  { title: '积分管理', icon: Coins, path: '/admin/points-manage' }
]

function isActive(path: string): boolean {
  if (path === '/admin') return route.path === '/admin'
  return route.path.startsWith(path)
}
</script>

<template>
  <div class="flex h-full">
    <!-- 管理侧边栏 -->
    <aside class="w-56 shrink-0 bg-slate-900 text-white flex flex-col">
      <!-- 标题 -->
      <div class="px-5 py-4 border-b border-white/10">
        <h2 class="text-lg font-black tracking-tight">管理后台</h2>
        <p class="text-[10px] text-slate-400 mt-0.5">沃谷运营管理控制台</p>
      </div>

      <!-- 导航 -->
      <nav class="flex-1 px-3 py-3 space-y-1 overflow-y-auto">
        <button
          v-for="item in navItems"
          :key="item.path"
          :class="[
            'w-full text-left px-3 py-2.5 rounded-lg transition-all flex items-center gap-3 text-sm',
            isActive(item.path)
              ? 'bg-white/15 text-white font-bold'
              : 'text-slate-400 hover:text-white hover:bg-white/5'
          ]"
          @click="router.push(item.path)"
        >
          <component :is="item.icon" class="w-4.5 h-4.5 shrink-0" stroke-width="2" />
          {{ item.title }}
        </button>
      </nav>

      <!-- 底部 -->
      <div class="px-3 py-3 border-t border-white/10">
        <button
          class="w-full text-left px-3 py-2.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/5 transition-all flex items-center gap-3 text-sm"
          @click="router.push('/console')"
        >
          <ArrowLeft class="w-4.5 h-4.5" stroke-width="2" />
          返回用户控制台
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="flex-1 overflow-auto bg-slate-50 p-6">
      <router-view />
    </main>
  </div>
</template>
