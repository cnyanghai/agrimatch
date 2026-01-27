<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { getDashboard, type DashboardResponse } from '../api/dashboard'
import { ArrowRight, FilePlus, Map, MessageSquare, FileCheck, User, Coins, Star, Bell, Clock, TrendingUp } from 'lucide-vue-next'
import ProfileGuideCard from '../components/ProfileGuideCard.vue'
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
      path: '/contracts'
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

// 7 大核心模块配置
const consoleModules = computed(() => {
  const modules = [
    {
      title: '发布信息',
      desc: '发布供应或采购需求',
      icon: FilePlus,
      path: '/console/publish',
      color: 'slate',
      badge: null
    },
    {
      title: '关注列表',
      desc: '追踪关注商户动态',
      icon: Star,
      path: '/console/following',
      color: 'slate',
      badge: dashboard.value?.followingCount || null
    },
    {
      title: '地图找商',
      desc: '附近合作伙伴地图',
      icon: Map,
      path: '/map',
      color: 'slate',
      badge: null
    },
    {
      title: '聊天议价',
      desc: '在线沟通洽谈',
      icon: MessageSquare,
      path: '/chat',
      color: 'slate',
      badge: dashboard.value?.unreadMessageCount || null
    },
    {
      title: '合同管理',
      desc: '电子合同签署与管理',
      icon: FileCheck,
      path: '/contracts',
      color: 'slate',
      badge: dashboard.value?.pendingContractCount || null
    },
    {
      title: '用户资料',
      desc: '个人信息与公司资料',
      icon: User,
      path: '/profile',
      color: 'slate',
      badge: null
    },
    {
      title: '会员积分',
      desc: '积分充值与兑换商城',
      icon: Coins,
      path: '/points',
      color: 'slate',
      badge: null
    }
  ]
  return modules
})

// 统计数据 - 使用computed确保响应式更新
const statsData = computed(() => [
  { label: '我的发布', value: dashboard.value?.myActiveListingCount ?? 0, unit: '个活跃', color: 'text-brand-600', icon: FilePlus },
  { label: '今日咨询', value: dashboard.value?.todayViewCount ?? 0, unit: '次访问', color: 'text-autumn-600', icon: MessageSquare },
  { label: '累计成交', value: formatNumber(dashboard.value?.totalDealQuantity), unit: '吨', color: 'text-purple-600', icon: Coins },
  { label: '执行中合同', value: dashboard.value?.activeContractCount ?? 0, unit: '份', color: 'text-blue-600', icon: FileCheck }
])

// 获取图标容器的样式类（统一为 slate 风格，与首页对齐）
function getIconClass() {
  // 统一使用 slate 风格，与首页搜索模块保持一致
  return 'bg-slate-50 text-slate-500 group-hover:bg-brand-50 group-hover:text-brand-600'
}

// 待办项颜色
function getPendingColorClass(color?: string): { bg: string; text: string; icon: string } {
  const colorMap: Record<string, { bg: string; text: string; icon: string }> = {
    amber: { bg: 'bg-amber-50', text: 'text-amber-700', icon: 'text-amber-500' },
    red: { bg: 'bg-red-50', text: 'text-red-700', icon: 'text-red-500' },
    blue: { bg: 'bg-autumn-50', text: 'text-autumn-700', icon: 'text-autumn-500' },
    purple: { bg: 'bg-purple-50', text: 'text-purple-700', icon: 'text-purple-500' }
  }
  const key = color || 'amber'
  return (colorMap[key] ?? colorMap.amber) as { bg: string; text: string; icon: string }
}

async function loadDashboard() {
  loading.value = true
  try {
    const res = await getDashboard()
    if (res.code === 0 && res.data) {
      dashboard.value = res.data
    }
  } catch (e) {
    console.error('加载首页数据失败', e)
  } finally {
    loading.value = false
  }
}

function go(path: string) {
  router.push(path)
}

// 格式化数字
function formatNumber(num: number | undefined): string {
  if (num == null) return '0'
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
            <h2 class="text-3xl font-black text-gray-900 mb-2 tracking-tight">
                {{ greeting }}，{{ auth.me?.nickName || auth.me?.userName || '用户' }}
              </h2>
            <p class="text-gray-500 max-w-lg leading-relaxed">
              欢迎回到 AgriMatch 智慧畜牧管理控制台。
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

        <!-- Pending Count Widget (Span 1) -->
        <Card variant="slate" radius="2xl" padding="lg" shadow="xl" class="group cursor-pointer hover:bg-slate-800" @click="go('/chat')">
          <div class="flex justify-between items-start">
            <div class="w-12 h-12 rounded-2xl bg-white/10 flex items-center justify-center text-amber-400 backdrop-blur-md group-hover:scale-110 transition-transform">
              <Bell :size="24" />
            </div>
            <span class="text-[10px] font-bold text-slate-400 uppercase tracking-widest mt-1">待办概览</span>
          </div>
          <div class="mt-8">
            <div class="text-5xl font-black mb-1">{{ totalPending }}</div>
            <p class="text-xs text-slate-400 font-bold uppercase tracking-wider">个待处理任务</p>
          </div>
          <div class="w-full mt-6 py-3 bg-white/10 rounded-xl text-xs font-bold transition-colors border border-white/10 text-center">
            进入待办中心
          </div>
        </Card>
      </div>

      <!-- Main Bento Grid -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        
        <!-- Pending Details (Bento Card - Tall) -->
        <Card v-if="pendingItems.length > 0" radius="2xl" padding="none" class="md:row-span-2 flex flex-col overflow-hidden">
          <div class="p-6 border-b border-gray-50 bg-gray-50/50">
            <h3 class="text-sm font-black text-gray-900 uppercase tracking-wider">任务详情</h3>
          </div>
          <div class="flex-1 overflow-y-auto">
          <button
            v-for="item in pendingItems"
            :key="item.label"
              class="w-full p-6 hover:bg-slate-50 transition-colors text-left border-b border-gray-50 flex items-center gap-4 group"
            @click="go(item.path)"
          >
              <div :class="['w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 transition-transform group-hover:scale-110', getPendingColorClass(item.color).bg]">
                <component :is="item.icon" class="w-6 h-6" :class="getPendingColorClass(item.color).icon" stroke-width="2.5" />
              </div>
              <div class="min-w-0">
                <div class="flex items-baseline gap-1">
                  <span class="text-xl font-black text-gray-900">{{ item.count }}</span>
                  <span :class="['text-[10px] font-bold uppercase truncate', getPendingColorClass(item.color).text]">{{ item.label }}</span>
                </div>
                <p class="text-[10px] text-gray-400 mt-0.5">点击处理该事项</p>
            </div>
          </button>
          </div>
        </Card>

        <!-- Stats Grid (Bento Cards - Regular) -->
        <Card
          v-for="stat in statsData" 
          :key="stat.label"
          radius="2xl"
          padding="md"
          hover
          class="group"
        >
          <div class="flex items-center justify-between mb-4">
            <div class="w-10 h-10 rounded-xl bg-slate-50 flex items-center justify-center text-slate-400 group-hover:scale-110 transition-transform">
              <component :is="stat.icon" :size="20" />
        </div>
            <span class="text-[10px] font-black text-gray-400 uppercase tracking-widest">{{ stat.label }}</span>
          </div>
          <div class="flex items-baseline gap-2">
            <span class="text-3xl font-black text-gray-900">{{ stat.value ?? 0 }}</span>
            <span :class="['text-[10px] font-bold uppercase', stat.color]">{{ stat.unit }}</span>
          </div>
        </Card>

        <!-- Core Functions Bento Area -->
        <div class="md:col-span-3 grid grid-cols-1 sm:grid-cols-3 gap-4">
          <Card
            v-for="(module, index) in consoleModules.slice(0, 6)"
            :key="module.title"
            variant="interactive"
            radius="2xl"
            padding="md"
            hover
            class="relative overflow-hidden text-left"
            @click="go(module.path)"
          >
            <!-- Background Accent -->
            <div class="absolute top-0 right-0 w-12 h-12 bg-slate-50 rounded-bl-3xl -mr-4 -mt-4 group-hover:bg-brand-50 transition-colors"></div>
            
            <!-- 图标容器 -->
            <div :class="`w-12 h-12 rounded-2xl flex items-center justify-center mb-4 transition-all group-hover:scale-110 ${getIconClass()}`">
              <component :is="module.icon" class="w-5 h-5" stroke-width="2" />
            </div>
            
            <!-- 标题与描述 -->
            <div class="relative z-10">
              <h4 class="font-black text-gray-900 group-hover:text-brand-600 transition-colors mb-1 text-sm">
                {{ module.title }}
              </h4>
              <p class="text-[10px] text-gray-400 leading-tight">
                {{ module.desc }}
              </p>
            </div>

            <!-- 角标 -->
            <div v-if="module.badge" class="absolute top-4 right-4 w-5 h-5 rounded-full bg-red-500 text-white text-[9px] font-bold flex items-center justify-center shadow-sm">
              {{ module.badge > 99 ? '99+' : module.badge }}
            </div>
          </Card>
        </div>

      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex items-center justify-center py-12">
        <LoadingSpinner size="md" color="brand" />
      </div>
    </div>
  </div>
</template>
