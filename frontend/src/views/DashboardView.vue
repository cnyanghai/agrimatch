<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { getDashboard, type DashboardResponse } from '../api/dashboard'
import { ArrowRight, FilePlus, Map, MessageSquare, FileCheck, User, Coins, Star, Bell, Clock } from 'lucide-vue-next'
import ProfileGuideCard from '../components/ProfileGuideCard.vue'

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

// 获取图标容器的样式类（统一为 slate 风格，与首页对齐）
function getIconClass(color: string) {
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
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      
      <!-- 欢迎区域 + 资料引导 -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 欢迎卡片 -->
        <section class="lg:col-span-2 bg-white rounded-xl border border-gray-200 shadow-sm p-6">
          <div class="flex items-center justify-between">
            <div>
              <h2 class="text-2xl font-bold text-gray-800 mb-1">
                {{ greeting }}，{{ auth.me?.nickName || auth.me?.userName || '用户' }}
              </h2>
              <p class="text-sm text-gray-500">
                <template v-if="totalPending > 0">
                  您有 <span class="text-amber-600 font-bold">{{ totalPending }}</span> 项待处理事务
                </template>
                <template v-else>
                  欢迎使用 AgriMatch 平台，开始您的业务之旅
                </template>
              </p>
            </div>
          </div>
        </section>

        <!-- 资料完善引导卡片 -->
        <ProfileGuideCard />
      </div>

      <!-- 待办事项（核心模块） -->
      <section v-if="pendingItems.length > 0" class="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
        <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <div class="w-1.5 h-5 bg-amber-500 rounded-full"></div>
            <h3 class="font-bold text-gray-800">待处理</h3>
            <span class="text-[10px] font-bold px-2 py-0.5 rounded-full bg-amber-100 text-amber-700">
              {{ totalPending }} 项
            </span>
          </div>
        </div>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-px bg-gray-100">
          <button
            v-for="item in pendingItems"
            :key="item.label"
            class="bg-white p-5 hover:bg-gray-50 transition-colors text-left"
            @click="go(item.path)"
          >
            <div class="flex items-center gap-3">
              <div :class="['w-10 h-10 rounded-xl flex items-center justify-center', getPendingColorClass(item.color).bg]">
                <component :is="item.icon" class="w-5 h-5" :class="getPendingColorClass(item.color).icon" stroke-width="2" />
              </div>
              <div>
                <div class="flex items-baseline gap-1">
                  <span class="text-2xl font-bold text-gray-900">{{ item.count }}</span>
                  <span :class="['text-xs font-medium', getPendingColorClass(item.color).text]">{{ item.label }}</span>
                </div>
              </div>
            </div>
          </button>
        </div>
      </section>

      <!-- 数据统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="bg-white p-5 rounded-xl border border-gray-200 shadow-sm hover-card animate-stagger-in">
          <p class="text-[10px] font-bold text-gray-400 mb-2 uppercase tracking-wider">
            我的发布
          </p>
          <div class="flex items-end gap-2">
            <span class="text-2xl font-bold text-gray-800 count-up">{{ dashboard?.myActiveListingCount ?? 0 }}</span>
            <span class="text-xs text-brand-500 font-bold pb-1">个活跃</span>
          </div>
        </div>
        <div class="bg-white p-5 rounded-xl border border-gray-200 shadow-sm hover-card animate-stagger-in">
          <p class="text-[10px] font-bold text-gray-400 mb-2 uppercase tracking-wider">今日咨询</p>
          <div class="flex items-end gap-2">
            <span class="text-2xl font-bold text-gray-800 count-up">{{ dashboard?.todayViewCount ?? 0 }}</span>
            <span class="text-xs text-autumn-500 font-bold pb-1">次访问</span>
          </div>
        </div>
        <div class="bg-white p-5 rounded-xl border border-gray-200 shadow-sm hover-card animate-stagger-in">
          <p class="text-[10px] font-bold text-gray-400 mb-2 uppercase tracking-wider">累计成交</p>
          <div class="flex items-end gap-2">
            <span class="text-2xl font-bold text-gray-800 count-up">{{ formatNumber(dashboard?.totalDealQuantity) }}</span>
            <span class="text-xs text-gray-500 font-bold pb-1">吨</span>
          </div>
        </div>
        <div class="bg-white p-5 rounded-xl border border-gray-200 shadow-sm hover-card animate-stagger-in">
          <p class="text-[10px] font-bold text-gray-400 mb-2 uppercase tracking-wider">进行中合同</p>
          <div class="flex items-end gap-2">
            <span class="text-2xl font-bold text-gray-800 count-up">{{ dashboard?.activeContractCount ?? 0 }}</span>
            <span class="text-xs text-brand-500 font-bold pb-1">份</span>
          </div>
        </div>
      </div>

      <!-- 核心功能模块矩阵 (Bento Grid) -->
      <section>
        <div class="flex items-center justify-between mb-6">
          <div>
            <h3 class="text-xl font-bold text-gray-900">核心功能</h3>
            <p class="text-sm text-gray-500 mt-1">快速访问平台核心功能模块</p>
          </div>
        </div>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <button
            v-for="(module, index) in consoleModules"
            :key="module.title"
            class="group relative bg-white p-6 rounded-2xl border border-gray-100 hover:shadow-lg hover:border-brand-200 hover:-translate-y-1 transition-all cursor-pointer text-left animate-stagger-in"
            :style="{ animationDelay: `${index * 50}ms` }"
            @click="go(module.path)"
          >
            <!-- 图标容器 -->
            <div :class="`w-14 h-14 rounded-xl flex items-center justify-center mb-4 transition-all group-hover:scale-110 ${getIconClass(module.color)}`">
              <component :is="module.icon" class="w-6 h-6" stroke-width="2" />
            </div>
            
            <!-- 标题与描述 -->
            <div class="mb-2">
              <h4 class="font-bold text-gray-900 group-hover:text-brand-600 transition-colors mb-1">
                {{ module.title }}
              </h4>
              <p class="text-xs text-gray-500 leading-relaxed">
                {{ module.desc }}
              </p>
            </div>
            
            <!-- 角标 -->
            <div v-if="module.badge" class="absolute top-4 right-4 w-6 h-6 rounded-full bg-red-500 text-white text-[10px] font-bold flex items-center justify-center">
              {{ module.badge > 99 ? '99+' : module.badge }}
            </div>
            
            <!-- 箭头指示 -->
            <div class="flex items-center gap-1 text-gray-400 group-hover:text-brand-500 transition-colors mt-4">
              <span class="text-xs font-bold">进入</span>
              <ArrowRight class="w-3.5 h-3.5" stroke-width="2" />
            </div>
          </button>
        </div>
      </section>

      <!-- 加载状态 -->
      <div v-if="loading" class="flex items-center justify-center py-12">
        <div class="text-gray-400 text-sm">正在加载...</div>
      </div>
    </div>
  </div>
</template>
