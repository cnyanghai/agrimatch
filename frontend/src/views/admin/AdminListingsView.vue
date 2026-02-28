<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from '@/composables/useToast'
import { showConfirm } from '@/composables/useConfirm'
import { Search, RefreshCw, EyeOff, RotateCcw, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import {
  listAdminSupplies, takedownSupply, restoreSupply,
  listAdminRequirements, takedownRequirement, restoreRequirement,
  type AdminListingResponse
} from '../../api/admin'

type TabType = 'supply' | 'requirement'

const loading = ref(false)
const activeTab = ref<TabType>('supply')
const keyword = ref('')
const page = ref(1)
const size = ref(20)
const total = ref(0)
const items = ref<AdminListingResponse[]>([])

async function load() {
  loading.value = true
  try {
    const params = { keyword: keyword.value || undefined, page: page.value, size: size.value }
    const res = activeTab.value === 'supply'
      ? await listAdminSupplies(params)
      : await listAdminRequirements(params)
    if (res.code !== 0) throw new Error(res.message)
    items.value = res.data.list
    total.value = res.data.total
  } catch (e: any) {
    showToast.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

function switchTab(tab: TabType) {
  activeTab.value = tab
  page.value = 1
  keyword.value = ''
  load()
}

function doSearch() {
  page.value = 1
  load()
}

async function handleTakedown(item: AdminListingResponse) {
  const ok = await showConfirm({ title: '下架确认', message: `确认下架「${item.categoryName}」？`, type: 'warning' })
  if (!ok) return
  try {
    const res = activeTab.value === 'supply'
      ? await takedownSupply(item.id)
      : await takedownRequirement(item.id)
    if (res.code !== 0) throw new Error(res.message)
    showToast.success('已下架')
    await load()
  } catch (e: any) {
    showToast.error(e?.message ?? '操作失败')
  }
}

async function handleRestore(item: AdminListingResponse) {
  try {
    const res = activeTab.value === 'supply'
      ? await restoreSupply(item.id)
      : await restoreRequirement(item.id)
    if (res.code !== 0) throw new Error(res.message)
    showToast.success('已恢复')
    await load()
  } catch (e: any) {
    showToast.error(e?.message ?? '操作失败')
  }
}

function getStatusLabel(status: number): string {
  switch (status) {
    case 1: return '上架中'
    case 2: return '已下架'
    case 3: return '已过期'
    default: return '草稿'
  }
}

function getStatusColor(status: number): string {
  switch (status) {
    case 1: return 'bg-brand-100 text-brand-700'
    case 2: return 'bg-red-100 text-red-700'
    case 3: return 'bg-neutral-100 text-neutral-500'
    default: return 'bg-neutral-100 text-neutral-500'
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
        <h1 class="text-2xl font-bold text-neutral-900">信息审核</h1>
        <p class="text-sm text-neutral-500 mt-1">管理平台供应与采购信息</p>
      </div>
      <button class="px-3 py-2 rounded-lg bg-white border border-neutral-200 text-sm font-medium text-neutral-600 hover:bg-neutral-50 flex items-center gap-2" :disabled="loading" @click="load">
        <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': loading }" />
        刷新
      </button>
    </div>

    <!-- Tab 切换 -->
    <div class="flex items-center gap-2">
      <button
        :class="['px-4 py-2 rounded-xl font-bold text-sm transition-all', activeTab === 'supply' ? 'bg-brand-600 text-white shadow-md' : 'bg-white border border-neutral-200 text-neutral-600 hover:border-brand-300']"
        @click="switchTab('supply')"
      >供应信息</button>
      <button
        :class="['px-4 py-2 rounded-xl font-bold text-sm transition-all', activeTab === 'requirement' ? 'bg-brand-600 text-white shadow-md' : 'bg-white border border-neutral-200 text-neutral-600 hover:border-brand-300']"
        @click="switchTab('requirement')"
      >采购信息</button>
    </div>

    <!-- 搜索 -->
    <div class="flex gap-3">
      <div class="relative flex-1 max-w-md">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-neutral-400" />
        <input
          v-model="keyword"
          class="w-full pl-10 pr-4 py-2.5 rounded-lg border border-neutral-200 text-sm focus:border-brand-500 focus:ring-1 focus:ring-brand-500 outline-none"
          placeholder="搜索品类名或企业名..."
          @keydown.enter="doSearch"
        />
      </div>
      <button class="px-4 py-2.5 rounded-lg bg-brand-600 hover:bg-brand-700 text-white text-sm font-bold" @click="doSearch">搜索</button>
    </div>

    <!-- 列表 -->
    <div class="bg-white rounded-xl border border-neutral-200 overflow-hidden">
      <div v-if="loading && items.length === 0" class="p-8 text-center text-neutral-400">加载中...</div>
      <div v-else-if="items.length === 0" class="p-8 text-center text-neutral-400">暂无数据</div>
      <table v-else class="w-full text-sm">
        <thead class="bg-neutral-50 border-b border-neutral-200">
          <tr>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">品类</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">企业</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">价格</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">数量</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">状态</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">发布时间</th>
            <th class="px-4 py-3 text-right font-bold text-neutral-500 text-xs uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-neutral-100">
          <tr v-for="item in items" :key="item.id" class="hover:bg-neutral-50 transition-colors">
            <td class="px-4 py-3 font-bold text-neutral-900">{{ item.categoryName }}</td>
            <td class="px-4 py-3 text-neutral-600">{{ item.companyName || '-' }}</td>
            <td class="px-4 py-3 text-neutral-600">{{ item.price != null ? `¥${item.price}/${item.priceUnit || ''}` : '-' }}</td>
            <td class="px-4 py-3 text-neutral-600">{{ item.quantity != null ? `${item.quantity} ${item.quantityUnit || ''}` : '-' }}</td>
            <td class="px-4 py-3">
              <span :class="['px-1.5 py-0.5 rounded text-[9px] font-bold', getStatusColor(item.status)]">
                {{ getStatusLabel(item.status) }}
              </span>
            </td>
            <td class="px-4 py-3 text-neutral-500 text-xs">{{ formatTime(item.createTime) }}</td>
            <td class="px-4 py-3 text-right">
              <div class="flex items-center justify-end gap-2">
                <button
                  v-if="item.status === 1"
                  class="p-1.5 rounded-lg hover:bg-red-50 transition-colors"
                  title="下架"
                  @click="handleTakedown(item)"
                >
                  <EyeOff class="w-4 h-4 text-red-500" />
                </button>
                <button
                  v-if="item.status === 2"
                  class="p-1.5 rounded-lg hover:bg-brand-50 transition-colors"
                  title="恢复"
                  @click="handleRestore(item)"
                >
                  <RotateCcw class="w-4 h-4 text-brand-600" />
                </button>
              </div>
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
