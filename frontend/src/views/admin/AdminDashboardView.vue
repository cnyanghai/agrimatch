<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAdminDashboard, type AdminDashboardResponse } from '../../api/admin'
import {
  Users, UserPlus, Building2, Package,
  ShoppingCart, FileCheck, MessageSquareText, Activity
} from 'lucide-vue-next'

const loading = ref(false)
const data = ref<AdminDashboardResponse | null>(null)

const statCards = [
  { key: 'totalUsers', label: '总用户', icon: Users, bg: 'bg-brand-50', iconColor: 'text-brand-600' },
  { key: 'todayNewUsers', label: '今日新增', icon: UserPlus, bg: 'bg-action-50', iconColor: 'text-action-600' },
  { key: 'totalCompanies', label: '总企业', icon: Building2, bg: 'bg-autumn-50', iconColor: 'text-autumn-500' },
  { key: 'activeSupplyCount', label: '活跃供应', icon: Package, bg: 'bg-brand-50', iconColor: 'text-brand-600' },
  { key: 'activeRequirementCount', label: '活跃采购', icon: ShoppingCart, bg: 'bg-autumn-50', iconColor: 'text-autumn-500' },
  { key: 'totalContracts', label: '总合同', icon: FileCheck, bg: 'bg-action-50', iconColor: 'text-action-600' },
  { key: 'totalPosts', label: '总话题', icon: MessageSquareText, bg: 'bg-brand-50', iconColor: 'text-brand-600' },
  { key: 'todayLoginCount', label: '今日登录', icon: Activity, bg: 'bg-action-50', iconColor: 'text-action-600' }
] as const

function getValue(key: string): number {
  if (!data.value) return 0
  return (data.value as any)[key] ?? 0
}

async function load() {
  loading.value = true
  try {
    const res = await getAdminDashboard()
    if (res.code === 0) data.value = res.data
  } catch {
    // silently ignore
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-neutral-900">管理仪表盘</h1>
      <p class="text-sm text-neutral-500 mt-1">平台运营数据概览</p>
    </div>

    <!-- 统计卡片 2x4 -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <div
        v-for="stat in statCards"
        :key="stat.key"
        class="bg-white rounded-xl border border-neutral-200 p-5 transition-shadow hover:shadow-md"
      >
        <div class="flex items-center justify-between mb-3">
          <span class="text-xs font-bold text-neutral-400 uppercase tracking-wider">{{ stat.label }}</span>
          <div :class="['w-9 h-9 rounded-lg flex items-center justify-center', stat.bg]">
            <component :is="stat.icon" :size="18" :class="stat.iconColor" />
          </div>
        </div>
        <div v-if="loading" class="h-9 bg-neutral-100 rounded animate-pulse"></div>
        <p v-else class="text-3xl font-black text-neutral-900">{{ getValue(stat.key).toLocaleString() }}</p>
      </div>
    </div>
  </div>
</template>
