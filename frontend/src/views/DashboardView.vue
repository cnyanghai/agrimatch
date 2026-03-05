<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { getDashboard, type DashboardResponse } from '../api/dashboard'
import { FilePlus, Map, MessageSquare, FileCheck, User, Coins, Star, Bell, Clock, TrendingUp, Check, FileText, ChevronRight } from 'lucide-vue-next'
import { Card, LoadingSpinner } from '../components/ui'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const dashboard = ref<DashboardResponse | null>(null)

// 获取问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 待办事项列表
const pendingItems = computed(() => {
  if (!dashboard.value) return []
  const items = []
  
  if (dashboard.value.unreadMessageCount > 0) {
    items.push({
      icon: Bell,
      label: '条未读消息',
      count: dashboard.value.unreadMessageCount,
      color: 'amber',
      path: '/chat'
    })
  }
  
  if (dashboard.value.pendingContractCount > 0) {
    items.push({
      icon: FileCheck,
      label: '份待签署合同',
      count: dashboard.value.pendingContractCount,
      color: 'red',
      path: '/contracts'
    })
  }
  
  if (dashboard.value.pendingInquiryCount > 0) {
    items.push({
      icon: MessageSquare,
      label: '条待回复询价',
      count: dashboard.value.pendingInquiryCount,
      color: 'blue',
      path: '/chat'
    })
  }
  
  if (dashboard.value.pendingMilestoneCount > 0) {
    items.push({
      icon: Clock,
      label: '个待确认里程碑',
      count: dashboard.value.pendingMilestoneCount,
      color: 'purple',
      path: '/contracts?tab=milestones'
    })
  }
  
  return items
})

// 待办总数
const totalPending = computed(() => {
  if (!dashboard.value) return 0
  return (dashboard.value.unreadMessageCount || 0) +
         (dashboard.value.pendingContractCount || 0) +
         (dashboard.value.pendingInquiryCount || 0) +
         (dashboard.value.pendingMilestoneCount || 0)
})

// 8 大核心模块配置
const consoleModules = computed(() => [
  {
    title: '发布信息',
    desc: '发布供应或采购需求',
    icon: FilePlus,
    path: '/console/publish',
    badge: null
  },
  {
    title: '关注列表',
    desc: '追踪关注商户动态',
    icon: Star,
    path: '/console/following',
    badge: dashboard.value?.followingCount || null
  },
  {
    title: '地图找商',
    desc: '附近合作伙伴地图',
    icon: Map,
    path: '/map',
    badge: null
  },
  {
    title: '聊天议价',
    desc: '在线沟通洽谈',
    icon: MessageSquare,
    path: '/chat',
    badge: dashboard.value?.unreadMessageCount || null
  },
  {
    title: '合同管理',
    desc: '电子合同签署与管理',
    icon: FileCheck,
    path: '/contracts',
    badge: dashboard.value?.pendingContractCount || null
  },
  {
    title: '用户资料',
    desc: '个人信息与公司资料',
    icon: User,
    path: '/profile',
    badge: null
  },
  {
    title: '会员积分',
    desc: '积分余额与交易记录',
    icon: Coins,
    path: '/points',
    badge: dashboard.value?.pointsBalance || null,
    badgeType: 'points'
  },
])

// 统计数据 - 纯展示信息，不可点击
const statsData = computed(() => [
  { label: '我的发布', value: dashboard.value?.myActiveListingCount ?? 0, unit: '个活跃', icon: FilePlus, iconBg: 'bg-brand-50', iconColor: 'text-brand-500' },
  { label: '执行中合同', value: dashboard.value?.activeContractCount ?? 0, unit: '份', icon: FileCheck, iconBg: 'bg-action-50', iconColor: 'text-action-500' },
  { label: '累计签署', value: dashboard.value?.totalSignedContractCount ?? 0, unit: '份合同', icon: FileText, iconBg: 'bg-action-50', iconColor: 'text-action-500' },
  { label: '累计成交', value: formatAmount(dashboard.value?.totalDealAmount), unit: '元', icon: Coins, iconBg: 'bg-warning-50', iconColor: 'text-warning-500' }
])

// 深色待办面板的背景色
function getPendingBgClass(color?: string): string {
  const map: Record<string, string> = {
    amber: 'bg-warning-500/20',
    red: 'bg-error-500/20',
    blue: 'bg-action-500/20',
    purple: 'bg-action-500/20'
  }
  return map[color || 'amber'] ?? map.amber ?? ''
}

// 深色待办面板的图标色
function getPendingIconClass(color?: string): string {
  const map: Record<string, string> = {
    amber: 'text-warning-400',
    red: 'text-error-400',
    blue: 'text-action-400',
    purple: 'text-action-400'
  }
  return map[color || 'amber'] ?? map.amber ?? ''
}

async function loadDashboard() {
  loading.value = true
  try {
    const res = await getDashboard()
    if (res.code === 0 && res.data) {
      dashboard.value = res.data
    }
  } catch {
    // silently ignore
  } finally {
    loading.value = false
  }
}

function go(path: string) {
  router.push(path)
}

// 格式化金额
function formatAmount(num: number | undefined): string {
  if (num == null) return '0'
  if (num >= 100000000) {
    return (num / 100000000).toFixed(2) + '亿'
  }
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
}

onMounted(() => {
  loadDashboard()
})
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      
      <!-- Top Section: Welcome & Pending (Bento Grid Style) -->
      <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <!-- Welcome Card (Span 3) -->
        <Card radius="2xl" padding="lg" shadow="sm" class="lg:col-span-3 relative overflow-hidden group">
          <div class="absolute top-0 right-0 w-64 h-full bg-gradient-to-l from-brand-50/50 to-transparent"></div>
          <div class="relative z-10">
            <h2 class="text-3xl font-black text-neutral-900 mb-2 tracking-tight">
                {{ greeting }}，{{ auth.me?.nickName || auth.me?.userName || '用户' }}
              </h2>
            <p class="text-neutral-500 max-w-lg leading-relaxed">
              欢迎回到沃谷农牧供应链管理控制台。
                <template v-if="totalPending > 0">
                您当前有 <span class="text-brand-600 font-bold underline decoration-2 underline-offset-4">{{ totalPending }}</span> 项待办事项需要关注。
                </template>
                <template v-else>
                目前一切正常，祝您今天工作愉快。
                </template>
              </p>
            
            <div class="mt-8 flex flex-wrap gap-4">
              <button class="bg-brand-600 hover:bg-brand-700 text-white px-6 py-2.5 rounded-xl font-bold transition-all active:scale-95 shadow-lg shadow-brand-900/10 flex items-center gap-2" @click="go('/console/publish')">
                <FilePlus :size="18" />
                <span>快速发布</span>
              </button>
              <button class="bg-slate-100 hover:bg-slate-200 text-slate-700 px-6 py-2.5 rounded-xl font-bold transition-all active:scale-95 flex items-center gap-2" @click="go('/profile')">
                <User :size="18" />
                <span>完善资料</span>
              </button>
            </div>
          </div>
          
          <!-- Decorative Icon -->
          <div class="absolute -bottom-10 -right-10 opacity-[0.03] group-hover:scale-110 transition-transform duration-700 pointer-events-none">
            <TrendingUp :size="240" />
          </div>
        </Card>

        <!-- 深色待办面板 (Span 1) -->
        <Card variant="slate" radius="2xl" padding="none" shadow="xl" class="lg:col-span-1 flex flex-col">
          <!-- 头部 -->
          <div class="p-4 border-b border-white/10">
            <div class="flex items-center justify-between">
              <span class="text-xs font-bold text-slate-400 uppercase tracking-widest">待办概览</span>
              <span class="text-2xl font-black text-white">{{ totalPending }}</span>
            </div>
          </div>

          <!-- 待办项列表 -->
          <div class="flex-1 overflow-y-auto">
            <button
              v-for="item in pendingItems"
              :key="item.label"
              class="w-full px-4 py-3 hover:bg-white/5 transition-colors text-left flex items-center gap-3 border-b border-white/5 last:border-b-0"
              @click="go(item.path)"
            >
              <div :class="['w-8 h-8 rounded-lg flex items-center justify-center shrink-0', getPendingBgClass(item.color)]">
                <component :is="item.icon" class="w-4 h-4" :class="getPendingIconClass(item.color)" stroke-width="2.5" />
              </div>
              <div class="min-w-0 flex-1">
                <div class="flex items-baseline gap-1.5">
                  <span class="text-lg font-black text-white">{{ item.count }}</span>
                  <span class="text-xs text-slate-400 truncate">{{ item.label }}</span>
                </div>
              </div>
            </button>

            <!-- 空状态 -->
            <div v-if="pendingItems.length === 0" class="p-6 text-center">
              <div class="w-12 h-12 rounded-full bg-white/10 flex items-center justify-center mx-auto mb-3">
                <Check :size="24" class="text-brand-400" />
              </div>
              <p class="text-sm text-slate-400">暂无待办事项</p>
              <p class="text-xs text-slate-500 mt-1">一切正常，继续保持</p>
            </div>
          </div>

          <!-- 底部按钮 -->
          <div class="p-4 border-t border-white/10">
            <button
              class="w-full py-2.5 bg-white/10 hover:bg-white/15 rounded-xl text-xs font-bold text-white transition-colors"
              @click="go('/chat')"
            >
              进入待办中心
            </button>
          </div>
        </Card>
      </div>

      <!-- 统计卡片区 (4列) - 纯展示，无交互 -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div
          v-for="stat in statsData"
          :key="stat.label"
          class="bg-gradient-to-br from-slate-50 to-white border border-slate-100 rounded-2xl p-4 select-none"
        >
          <div class="flex items-center justify-between mb-3">
            <span class="text-[10px] font-bold text-slate-400 uppercase tracking-widest">{{ stat.label }}</span>
            <div :class="['w-8 h-8 rounded-lg flex items-center justify-center', stat.iconBg]">
              <component :is="stat.icon" :size="16" :class="stat.iconColor" />
            </div>
          </div>
          <div class="flex items-baseline gap-2">
            <span class="text-3xl font-black text-neutral-900">{{ stat.value ?? 0 }}</span>
            <span class="text-xs text-slate-400">{{ stat.unit }}</span>
          </div>
        </div>
      </div>

      <!-- 功能入口区 (4x2 网格) - 可点击模块 -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <button
          v-for="module in consoleModules"
          :key="module.title"
          class="relative bg-white border border-slate-200 rounded-2xl p-4 text-left cursor-pointer transition-all duration-200 hover:border-brand-300 hover:shadow-lg hover:shadow-brand-100/50 hover:-translate-y-0.5 active:scale-[0.98] group"
          @click="go(module.path)"
        >
          <!-- 图标容器 -->
          <div class="w-11 h-11 rounded-xl bg-slate-100 flex items-center justify-center mb-3 transition-all group-hover:bg-brand-100 group-hover:scale-105">
            <component :is="module.icon" class="w-5 h-5 text-slate-500 group-hover:text-brand-600 transition-colors" stroke-width="2" />
          </div>

          <!-- 标题与描述 -->
          <div class="pr-6">
            <h4 class="font-bold text-neutral-900 group-hover:text-brand-600 transition-colors mb-0.5 text-sm">
              {{ module.title }}
            </h4>
            <p class="text-[10px] text-slate-400 leading-tight">
              {{ module.desc }}
            </p>
          </div>

          <!-- 箭头指示器 -->
          <div class="absolute right-3 top-1/2 -translate-y-1/2 w-6 h-6 rounded-full bg-slate-50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all group-hover:bg-brand-50">
            <ChevronRight :size="14" class="text-slate-400 group-hover:text-brand-500" />
          </div>

          <!-- 角标 -->
          <div v-if="module.badge" :class="[
            'absolute top-3 right-3 min-w-5 h-5 px-1.5 rounded-full text-[9px] font-bold flex items-center justify-center shadow-sm',
            module.badgeType === 'points' ? 'bg-warning-500 text-white' : 'bg-error-500 text-white'
          ]">
            {{ module.badgeType === 'points' ? module.badge.toLocaleString() : (module.badge > 99 ? '99+' : module.badge) }}
          </div>
        </button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex items-center justify-center py-12">
        <LoadingSpinner size="md" color="brand" />
      </div>
    </div>
  </div>
</template>
