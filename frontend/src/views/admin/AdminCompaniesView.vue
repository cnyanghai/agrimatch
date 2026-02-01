<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshCw, CheckCircle, XCircle, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { listAdminCompanies, verifyCompany, rejectCompany, type AdminCompanyResponse } from '../../api/admin'

const loading = ref(false)
const keyword = ref('')
const statusFilter = ref<number | undefined>(undefined)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const companies = ref<AdminCompanyResponse[]>([])

const statusTabs = [
  { value: undefined, label: '全部' },
  { value: 0, label: '未审核' },
  { value: 1, label: '已认证' },
  { value: 2, label: '已拒绝' }
]

function getStatusInfo(status: number) {
  switch (status) {
    case 1: return { label: '已认证', color: 'bg-brand-100 text-brand-700' }
    case 2: return { label: '已拒绝', color: 'bg-red-100 text-red-700' }
    default: return { label: '未审核', color: 'bg-warning-100 text-warning-700' }
  }
}

async function load() {
  loading.value = true
  try {
    const res = await listAdminCompanies({ keyword: keyword.value || undefined, status: statusFilter.value, page: page.value, size: size.value })
    if (res.code !== 0) throw new Error(res.message)
    companies.value = res.data.list
    total.value = res.data.total
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

function doSearch() {
  page.value = 1
  load()
}

function switchTab(val: number | undefined) {
  statusFilter.value = val
  page.value = 1
  load()
}

async function handleVerify(c: AdminCompanyResponse) {
  try {
    await ElMessageBox.confirm(`确认认证企业「${c.companyName}」？`, '企业认证', { type: 'info' })
  } catch { return }
  try {
    const res = await verifyCompany(c.id)
    if (res.code !== 0) throw new Error(res.message)
    ElMessage.success('已认证')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '操作失败')
  }
}

async function handleReject(c: AdminCompanyResponse) {
  try {
    await ElMessageBox.confirm(`确认拒绝企业「${c.companyName}」的认证？`, '拒绝认证', { type: 'warning' })
  } catch { return }
  try {
    const res = await rejectCompany(c.id)
    if (res.code !== 0) throw new Error(res.message)
    ElMessage.success('已拒绝')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '操作失败')
  }
}

const totalPages = () => Math.ceil(total.value / size.value)

function formatTime(t: string): string {
  return new Date(t).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(load)
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-neutral-900">企业管理</h1>
        <p class="text-sm text-neutral-500 mt-1">审核企业资质与认证管理</p>
      </div>
      <button class="px-3 py-2 rounded-lg bg-white border border-neutral-200 text-sm font-medium text-neutral-600 hover:bg-neutral-50 flex items-center gap-2" :disabled="loading" @click="load">
        <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': loading }" />
        刷新
      </button>
    </div>

    <!-- 状态筛选 -->
    <div class="flex items-center gap-2">
      <button
        v-for="tab in statusTabs"
        :key="String(tab.value)"
        :class="[
          'px-4 py-2 rounded-xl font-bold text-sm transition-all',
          statusFilter === tab.value
            ? 'bg-brand-600 text-white shadow-md'
            : 'bg-white border border-neutral-200 text-neutral-600 hover:border-brand-300'
        ]"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 搜索 -->
    <div class="flex gap-3">
      <div class="relative flex-1 max-w-md">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-neutral-400" />
        <input
          v-model="keyword"
          class="w-full pl-10 pr-4 py-2.5 rounded-lg border border-neutral-200 text-sm focus:border-brand-500 focus:ring-1 focus:ring-brand-500 outline-none"
          placeholder="搜索企业名、负责人..."
          @keydown.enter="doSearch"
        />
      </div>
      <button class="px-4 py-2.5 rounded-lg bg-brand-600 hover:bg-brand-700 text-white text-sm font-bold" @click="doSearch">搜索</button>
    </div>

    <!-- 列表 -->
    <div class="bg-white rounded-xl border border-neutral-200 overflow-hidden">
      <div v-if="loading && companies.length === 0" class="p-8 text-center text-neutral-400">加载中...</div>
      <div v-else-if="companies.length === 0" class="p-8 text-center text-neutral-400">暂无数据</div>
      <table v-else class="w-full text-sm">
        <thead class="bg-neutral-50 border-b border-neutral-200">
          <tr>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">企业名称</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">类型</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">负责人</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">地区</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">认证状态</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">创建时间</th>
            <th class="px-4 py-3 text-right font-bold text-neutral-500 text-xs uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-neutral-100">
          <tr v-for="c in companies" :key="c.id" class="hover:bg-neutral-50 transition-colors">
            <td class="px-4 py-3">
              <span class="font-bold text-neutral-900">{{ c.companyName }}</span>
            </td>
            <td class="px-4 py-3 text-neutral-600">{{ c.companyType || '-' }}</td>
            <td class="px-4 py-3 text-neutral-600">{{ c.contacts || c.ownerName || '-' }}</td>
            <td class="px-4 py-3 text-neutral-500 text-xs">{{ [c.province, c.city, c.district].filter(Boolean).join(' ') || '-' }}</td>
            <td class="px-4 py-3">
              <span :class="['px-1.5 py-0.5 rounded text-[9px] font-bold', getStatusInfo(c.verifiedStatus).color]">
                {{ getStatusInfo(c.verifiedStatus).label }}
              </span>
            </td>
            <td class="px-4 py-3 text-neutral-500 text-xs">{{ formatTime(c.createTime) }}</td>
            <td class="px-4 py-3 text-right">
              <div v-if="c.verifiedStatus !== 1" class="flex items-center justify-end gap-2">
                <button
                  class="p-1.5 rounded-lg hover:bg-brand-50 transition-colors"
                  title="认证"
                  @click="handleVerify(c)"
                >
                  <CheckCircle class="w-4 h-4 text-brand-600" />
                </button>
                <button
                  v-if="c.verifiedStatus !== 2"
                  class="p-1.5 rounded-lg hover:bg-red-50 transition-colors"
                  title="拒绝"
                  @click="handleReject(c)"
                >
                  <XCircle class="w-4 h-4 text-red-500" />
                </button>
              </div>
              <span v-else class="text-xs text-neutral-400">已认证</span>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div v-if="total > size" class="flex items-center justify-between px-4 py-3 border-t border-neutral-200 bg-neutral-50">
        <span class="text-xs text-neutral-500">共 {{ total }} 条记录</span>
        <div class="flex items-center gap-2">
          <button class="p-1.5 rounded hover:bg-neutral-200 disabled:opacity-50" :disabled="page <= 1" @click="page--; load()">
            <ChevronLeft class="w-4 h-4" />
          </button>
          <span class="text-sm text-neutral-600">{{ page }} / {{ totalPages() }}</span>
          <button class="p-1.5 rounded hover:bg-neutral-200 disabled:opacity-50" :disabled="page >= totalPages()" @click="page++; load()">
            <ChevronRight class="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
