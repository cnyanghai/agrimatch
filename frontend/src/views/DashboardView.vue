<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { getDashboard, type DashboardResponse } from '../api/dashboard'
import { ArrowRight, ShoppingCart, Box, ChatDotRound, Location, DocumentChecked, Bell, Clock } from '@element-plus/icons-vue'
import ProfileGuideCard from '../components/ProfileGuideCard.vue'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const dashboard = ref<DashboardResponse | null>(null)

const isBuyer = computed(() => auth.me?.isBuyer === 1)
const isSeller = computed(() => auth.me?.isSeller === 1)

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
      icon: DocumentChecked,
      label: '份待签署合同',
      count: dashboard.value.pendingContractCount,
      color: 'red',
      path: '/contracts'
    })
  }
  
  if (dashboard.value.pendingInquiryCount > 0) {
    items.push({
      icon: ChatDotRound,
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

// 快捷操作配置 - 根据角色显示图标颜色
const quickActions = computed(() => {
  const actions = []
  
  // 采购商操作
  if (isBuyer.value) {
    actions.push(
      { title: '发布采购', desc: '快速发布采购需求', icon: ShoppingCart, path: '/requirements', color: 'orange' },
      { title: '发现供应', desc: '查找优质供应商', icon: Box, path: '/supply-browse', color: 'brand' }
    )
  }

  // 供应商操作
  if (isSeller.value) {
    actions.push(
      { title: '发布供应', desc: '展示您的产品', icon: Box, path: '/supply', color: 'brand' },
      { title: '发现采购', desc: '查找采购商', icon: ShoppingCart, path: '/requirement-browse', color: 'orange' }
    )
  }

  // 通用操作
  actions.push(
    { title: '地图找商', desc: '附近合作伙伴地图', icon: Location, path: '/map', color: 'slate' },
    { title: '查看消息', desc: '在线沟通洽谈', icon: ChatDotRound, path: '/chat', color: 'brand' }
  )
  
  return actions.slice(0, 4) // 限制显示 4 项，保持排版整齐
})

// 获取图标容器的样式类
function getIconClass(color: string) {
  const colorMap: Record<string, string> = {
    brand: 'bg-brand-50 text-brand-600 group-hover:bg-brand-600 group-hover:text-white',
    emerald: 'bg-brand-50 text-brand-600 group-hover:bg-brand-600 group-hover:text-white',
    orange: 'bg-orange-50 text-orange-600 group-hover:bg-orange-600 group-hover:text-white',
    slate: 'bg-slate-50 text-slate-600 group-hover:bg-slate-900 group-hover:text-white'
  }
  return colorMap[color] || colorMap.brand
}

// 待办项颜色
function getPendingColorClass(color: string) {
  const colorMap: Record<string, { bg: string; text: string; icon: string }> = {
    amber: { bg: 'bg-amber-50', text: 'text-amber-700', icon: 'text-amber-500' },
    red: { bg: 'bg-red-50', text: 'text-red-700', icon: 'text-red-500' },
    blue: { bg: 'bg-blue-50', text: 'text-blue-700', icon: 'text-blue-500' },
    purple: { bg: 'bg-purple-50', text: 'text-purple-700', icon: 'text-purple-500' }
  }
  return colorMap[color] || colorMap.amber
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
                  {{ isBuyer ? '今日有新供应信息等待您查看' : '今日有新采购需求等待您报价' }}
                </template>
              </p>
            </div>
            <span
              class="text-[10px] font-bold px-3 py-1.5 rounded-full uppercase tracking-wider"
              :class="isBuyer ? 'bg-brand-100 text-brand-700 border border-brand-200' : 'bg-brand-100 text-brand-700 border border-brand-200'"
            >
              {{ isBuyer ? '采购商' : '供应商' }}
            </span>
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
                <el-icon :size="20" :class="getPendingColorClass(item.color).icon">
                  <component :is="item.icon" />
                </el-icon>
              </div>
              <div>
                <div class="flex items-baseline gap-1">
                  <span class="text-2xl font-black text-gray-900">{{ item.count }}</span>
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
            {{ isBuyer ? '我的采购' : '我的供应' }}
          </p>
          <div class="flex items-end gap-2">
            <span class="text-2xl font-black text-gray-800 count-up">{{ dashboard?.myActiveListingCount ?? 0 }}</span>
            <span class="text-xs text-brand-500 font-bold pb-1">个活跃</span>
          </div>
        </div>
        <div class="bg-white p-5 rounded-xl border border-gray-200 shadow-sm hover-card animate-stagger-in">
          <p class="text-[10px] font-bold text-gray-400 mb-2 uppercase tracking-wider">今日咨询</p>
          <div class="flex items-end gap-2">
            <span class="text-2xl font-black text-gray-800 count-up">{{ dashboard?.todayViewCount ?? 0 }}</span>
            <span class="text-xs text-blue-500 font-bold pb-1">次访问</span>
          </div>
        </div>
        <div class="bg-white p-5 rounded-xl border border-gray-200 shadow-sm hover-card animate-stagger-in">
          <p class="text-[10px] font-bold text-gray-400 mb-2 uppercase tracking-wider">累计成交</p>
          <div class="flex items-end gap-2">
            <span class="text-2xl font-black text-gray-800 count-up">{{ formatNumber(dashboard?.totalDealQuantity) }}</span>
            <span class="text-xs text-gray-500 font-bold pb-1">吨</span>
          </div>
        </div>
        <div class="bg-white p-5 rounded-xl border border-gray-200 shadow-sm hover-card animate-stagger-in">
          <p class="text-[10px] font-bold text-gray-400 mb-2 uppercase tracking-wider">进行中合同</p>
          <div class="flex items-end gap-2">
            <span class="text-2xl font-black text-gray-800 count-up">{{ dashboard?.activeContractCount ?? 0 }}</span>
            <span class="text-xs text-brand-500 font-bold pb-1">份</span>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <section>
        <div class="flex items-center justify-between mb-4">
          <h3 class="font-bold text-gray-800">快捷操作</h3>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <button
            v-for="(action, index) in quickActions"
            :key="action.title"
            class="flex items-center gap-4 bg-white p-4 rounded-xl border border-gray-200 shadow-sm hover-card group cursor-pointer animate-stagger-in"
            :style="{ animationDelay: `${(index + 4) * 50}ms` }"
            @click="go(action.path)"
          >
            <div :class="`w-12 h-12 rounded-xl flex items-center justify-center transition-all hover-rotate ${getIconClass(action.color)}`">
              <el-icon :size="24"><component :is="action.icon" /></el-icon>
            </div>
            <div class="text-left flex-1">
              <p class="font-bold text-gray-800 group-hover:text-brand-600 transition-colors">{{ action.title }}</p>
              <p class="text-[10px] text-gray-400 font-medium">{{ action.desc }}</p>
            </div>
            <el-icon class="text-gray-300 group-hover:text-brand-500 transition-colors group-hover:translate-x-1"><ArrowRight /></el-icon>
          </button>
        </div>
      </section>

      <!-- 无待办时的引导 -->
      <section v-if="pendingItems.length === 0 && !loading" class="bg-gradient-to-br from-brand-50 to-brand-100/50 rounded-xl border border-brand-100 p-8 text-center">
        <div class="w-16 h-16 mx-auto mb-4 rounded-xl bg-brand-100 flex items-center justify-center">
          <span class="text-3xl">✨</span>
        </div>
        <h3 class="text-lg font-bold text-gray-800 mb-2">太棒了，暂无待办事项！</h3>
        <p class="text-sm text-gray-500 mb-6">您可以浏览市场发现新机会，或发布您的供应/采购信息</p>
        <div class="flex justify-center gap-4">
          <button
            class="px-6 py-2.5 bg-brand-600 text-white rounded-xl text-sm font-bold shadow-md shadow-brand-100 hover:bg-brand-700 transition-all "
            @click="go(isBuyer ? '/supply-browse' : '/requirement-browse')"
          >
            {{ isBuyer ? '浏览供应' : '浏览采购' }}
          </button>
          <button 
            class="px-6 py-2.5 bg-white text-gray-700 rounded-xl text-sm font-bold border border-gray-200 hover:bg-gray-50 transition-all "
            @click="go(isBuyer ? '/requirements' : '/supply')"
          >
            {{ isBuyer ? '发布采购' : '发布供应' }}
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
