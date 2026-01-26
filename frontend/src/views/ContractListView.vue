<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  FileText, Search, Pen, RefreshCw,
  ChevronLeft, ChevronRight, BarChart3, AlertCircle,
  Package, FileEdit, FileClock, FileCheck, FileBox, FileCheck2, FileX
} from 'lucide-vue-next'
import { listContracts, deleteContract, cancelContract, type ContractResponse, type ContractQuery } from '../api/contract'
import { Skeleton } from '../components/ui'
import ContractSignModal from '../components/contract/ContractSignModal.vue'

const router = useRouter()

// 状态
const loading = ref(false)
const contracts = ref<ContractResponse[]>([])
const searchKeyword = ref('')
const filterStatus = ref<number | null>(null)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// Tab 配置
const tabs = [
  { key: 'all', label: '所有合同', statusFilter: null },
  { key: 'pending', label: '待我签署', statusFilter: 1 },
  { key: 'executing', label: '履行中', statusFilter: 3 },
  { key: 'archived', label: '已归档', statusFilter: 4 }
]
const activeTab = ref('all')

// 签署弹窗
const signModalVisible = ref(false)
const signContractId = ref<number | null>(null)

// 状态配置
const statusConfig: Record<number, {
  label: string
  color: string
  bgColor: string
  borderColor: string
}> = {
  0: { label: '草稿', color: 'text-gray-600', bgColor: 'bg-gray-100', borderColor: 'border-gray-200' },
  1: { label: '待签署', color: 'text-amber-700', bgColor: 'bg-amber-100', borderColor: 'border-amber-200' },
  2: { label: '已签署', color: 'text-brand-700', bgColor: 'bg-brand-100', borderColor: 'border-brand-200' },
  3: { label: '履行中', color: 'text-autumn-700', bgColor: 'bg-autumn-100', borderColor: 'border-autumn-200' },
  4: { label: '已完成', color: 'text-green-700', bgColor: 'bg-green-100', borderColor: 'border-green-200' },
  5: { label: '已取消', color: 'text-red-700', bgColor: 'bg-red-100', borderColor: 'border-red-200' }
}

// 状态图标组件映射
const statusIconMap: Record<number, any> = {
  0: FileEdit,
  1: FileClock,
  2: FileCheck,
  3: FileBox,
  4: FileCheck2,
  5: FileX
}

// 切换 Tab
function switchTab(key: string) {
  activeTab.value = key
  const tab = tabs.find(t => t.key === key)
  filterStatus.value = tab?.statusFilter ?? null
  currentPage.value = 1
}

// 筛选后的合同列表
const filteredContracts = computed(() => {
  let result = contracts.value

  if (filterStatus.value !== null) {
    result = result.filter(c => c.status === filterStatus.value)
  }

  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.toLowerCase()
    result = result.filter(c =>
      c.contractNo?.toLowerCase().includes(kw) ||
      c.productName?.toLowerCase().includes(kw) ||
      c.buyerCompanyName?.toLowerCase().includes(kw) ||
      c.sellerCompanyName?.toLowerCase().includes(kw)
    )
  }

  return result
})

// 分页后的合同列表
const paginatedContracts = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredContracts.value.slice(start, end)
})

// 总页数
const totalPages = computed(() => Math.ceil(filteredContracts.value.length / pageSize.value) || 1)

// 统计数据
const stats = computed(() => {
  const all = contracts.value
  const pendingTotal = all
    .filter(c => c.status === 1 || c.status === 3)
    .reduce((sum, c) => sum + (c.totalAmount || 0), 0)
  const monthlyNew = all.filter(c => {
    if (!c.createTime) return false
    const d = new Date(c.createTime)
    const now = new Date()
    return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth()
  }).length
  const completed = all.filter(c => c.status === 4).length
  const total = all.length
  const executionRate = total > 0 ? Math.round((completed / total) * 100) : 0

  return {
    total: all.length,
    draft: all.filter(c => c.status === 0).length,
    pending: all.filter(c => c.status === 1).length,
    signed: all.filter(c => c.status === 2).length,
    executing: all.filter(c => c.status === 3).length,
    completed,
    cancelled: all.filter(c => c.status === 5).length,
    pendingTotal,
    monthlyNew,
    executionRate
  }
})

// Tab 显示数量
function getTabCount(key: string): number {
  switch (key) {
    case 'all': return stats.value.total
    case 'pending': return stats.value.pending
    case 'executing': return stats.value.executing
    case 'archived': return stats.value.completed
    default: return 0
  }
}

// 待办事项
const todoItems = computed(() => {
  const items: Array<{
    type: 'sign' | 'execute' | 'warning'
    title: string
    desc: string
    contractId: number
  }> = []

  // 待签署的合同
  contracts.value
    .filter(c => c.status === 1)
    .slice(0, 2)
    .forEach(c => {
      items.push({
        type: 'sign',
        title: `待签署: ${c.productName || c.contractNo}`,
        desc: `合同金额 ¥${formatAmount(c.totalAmount)}`,
        contractId: c.id
      })
    })

  // 履行中的合同
  contracts.value
    .filter(c => c.status === 3)
    .slice(0, 1)
    .forEach(c => {
      items.push({
        type: 'execute',
        title: `履约跟进: ${c.productName || c.contractNo}`,
        desc: '请及时更新履约进度',
        contractId: c.id
      })
    })

  return items.slice(0, 3)
})

// 加载合同列表
async function loadContracts() {
  loading.value = true
  try {
    const query: ContractQuery = {}
    const res = await listContracts(query)
    if (res.code === 0 && res.data) {
      contracts.value = res.data
    }
  } catch (e) {
    console.error('加载合同列表失败', e)
  } finally {
    loading.value = false
  }
}

// 查看详情
function viewContract(id: number) {
  router.push(`/contracts/${id}`)
}

// 签署合同
function openSignModal(id: number) {
  signContractId.value = id
  signModalVisible.value = true
}

// 签署完成后刷新
function onSigned() {
  loadContracts()
}

// 删除合同
async function handleDelete(contract: ContractResponse) {
  if (contract.status !== 0) {
    ElMessage.warning('只能删除草稿状态的合同')
    return
  }

  try {
    await ElMessageBox.confirm(
      '确定要删除此合同吗？删除后不可恢复。',
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    const res = await deleteContract(contract.id)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      loadContracts()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '删除失败')
    }
  }
}

// 取消合同
async function handleCancel(contract: ContractResponse) {
  if (contract.status !== 0 && contract.status !== 1) {
    ElMessage.warning('只有草稿或待签署状态的合同可以取消')
    return
  }

  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请输入取消原因（可选）',
      '取消合同',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '返回',
        inputPlaceholder: '例如：买卖双方协商一致取消',
      }
    )

    const res = await cancelContract(contract.id, reason || undefined)
    if (res.code === 0) {
      ElMessage.success('合同已取消')
      loadContracts()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '取消失败')
    }
  }
}

// 重置筛选
function resetFilters() {
  searchKeyword.value = ''
  filterStatus.value = null
  activeTab.value = 'all'
  currentPage.value = 1
}

// 格式化金额
function formatAmount(val?: number): string {
  if (val == null) return '-'
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

// 格式化日期
function formatDate(val?: string): string {
  if (!val) return '-'
  return val.split('T')[0] || '-'
}

// 获取交易对手（对方公司名称）
function getCounterparty(contract: ContractResponse): string {
  // 这里可以根据当前用户判断显示买方还是卖方
  // 暂时显示买方或卖方（非空的那个）
  return contract.buyerCompanyName || contract.sellerCompanyName || '-'
}

// 分页
function goToPage(page: number) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
}

// 生成分页数字
const pageNumbers = computed(() => {
  const pages: (number | string)[] = []
  const total = totalPages.value
  const current = currentPage.value

  if (total <= 7) {
    for (let i = 1; i <= total; i++) pages.push(i)
  } else {
    pages.push(1)
    if (current > 3) pages.push('...')
    for (let i = Math.max(2, current - 1); i <= Math.min(total - 1, current + 1); i++) {
      pages.push(i)
    }
    if (current < total - 2) pages.push('...')
    pages.push(total)
  }

  return pages
})

// 监听筛选变化重置页码
watch([searchKeyword, filterStatus], () => {
  currentPage.value = 1
})

onMounted(() => {
  loadContracts()
})
</script>

<template>
  <div class="contract-list-view">
    <!-- 页面标题 -->
    <div class="flex flex-wrap justify-between items-end gap-4 mb-8">
      <div class="flex flex-col gap-2">
        <h1 class="text-3xl font-black text-gray-900 tracking-tight">合同管理中心</h1>
        <p class="text-gray-500">管理并追踪所有采购、销售合同，实时洞察交易状态</p>
      </div>
      <button
        class="flex items-center gap-2 px-5 py-2.5 bg-brand-600 hover:bg-brand-700 text-white rounded-xl font-bold transition-all shadow-sm"
        @click="loadContracts"
      >
        <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': loading }" />
        刷新数据
      </button>
    </div>

    <!-- 主内容区域 -->
    <div class="flex gap-8">
      <!-- 左侧：筛选 & 表格 -->
      <div class="flex-1 min-w-0">
        <!-- Tab + 筛选栏 -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 mb-6">
          <!-- Tab 导航 -->
          <div class="flex border-b border-gray-100 px-6">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              class="py-4 px-4 text-sm font-medium transition-colors border-b-2 -mb-px"
              :class="activeTab === tab.key
                ? 'border-brand-600 text-brand-600 font-bold'
                : 'border-transparent text-gray-500 hover:text-gray-700'"
              @click="switchTab(tab.key)"
            >
              {{ tab.label }} ({{ getTabCount(tab.key) }})
            </button>
          </div>

          <!-- 筛选栏 -->
          <div class="p-4 flex flex-wrap items-center gap-3">
            <!-- 状态筛选 -->
            <div class="flex items-center gap-2 bg-gray-50 rounded-lg px-3 py-1.5 border border-gray-100">
              <span class="text-xs text-gray-400 uppercase font-bold">状态</span>
              <select
                v-model="filterStatus"
                class="bg-transparent border-none focus:ring-0 text-sm py-0 pl-0 pr-6 font-medium cursor-pointer"
              >
                <option :value="null">全部状态</option>
                <option :value="0">草稿</option>
                <option :value="1">待签署</option>
                <option :value="2">已签署</option>
                <option :value="3">履行中</option>
                <option :value="4">已完成</option>
                <option :value="5">已取消</option>
              </select>
            </div>

            <!-- 搜索框 -->
            <div class="flex-1 min-w-[200px] relative">
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="搜索合同编号、产品名称或交易对手..."
                class="w-full pl-10 pr-4 py-2 bg-gray-50 border border-gray-100 rounded-lg text-sm focus:ring-brand-500 focus:border-brand-500 outline-none transition-all"
              />
            </div>

            <!-- 重置按钮 -->
            <button
              class="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg text-sm font-bold hover:bg-gray-200 transition-colors"
              @click="resetFilters"
            >
              重置筛选
            </button>
          </div>
        </div>

        <!-- 表格 -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <!-- 加载状态 -->
          <div v-if="loading" class="p-6">
            <div v-for="i in 5" :key="i" class="flex gap-4 py-4 border-b border-gray-50 last:border-0">
              <Skeleton type="text" class="!w-32" />
              <Skeleton type="text" class="!w-40" />
              <Skeleton type="text" class="!w-32" />
              <Skeleton type="text" class="!w-24" />
              <Skeleton type="text" class="!w-24" />
              <Skeleton type="text" class="!w-20" />
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else-if="filteredContracts.length === 0" class="p-12 text-center">
            <div class="w-20 h-20 mx-auto rounded-xl bg-gradient-to-br from-gray-100 to-gray-200 flex items-center justify-center mb-4">
              <FileText class="w-10 h-10 text-gray-400" />
            </div>
            <p class="text-lg font-bold text-gray-700">暂无合同</p>
            <p class="text-sm text-gray-400 mt-2">在聊天中确认报价后可起草合同</p>
          </div>

          <!-- 表格内容 -->
          <template v-else>
            <table class="w-full text-left border-collapse">
              <thead>
                <tr class="bg-gray-50 text-gray-500 text-xs font-bold uppercase tracking-wider">
                  <th class="px-6 py-4">合同编号</th>
                  <th class="px-6 py-4">产品/标题</th>
                  <th class="px-6 py-4">交易对手</th>
                  <th class="px-6 py-4">合同总额</th>
                  <th class="px-6 py-4">创建日期</th>
                  <th class="px-6 py-4 text-center">状态</th>
                  <th class="px-6 py-4 text-right">操作</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-100">
                <tr
                  v-for="contract in paginatedContracts"
                  :key="contract.id"
                  class="hover:bg-gray-50 transition-colors cursor-pointer"
                  @click="viewContract(contract.id)"
                >
                  <td class="px-6 py-4 text-sm font-mono text-gray-500">
                    {{ contract.contractNo || '-' }}
                  </td>
                  <td class="px-6 py-4 text-sm font-bold text-gray-900">
                    {{ contract.productName || '-' }}
                    <span v-if="contract.quantity" class="text-gray-400 font-normal ml-2">
                      {{ contract.quantity }}{{ contract.unit }}
                    </span>
                  </td>
                  <td class="px-6 py-4 text-sm">
                    <span class="text-brand-600 hover:underline font-medium">
                      {{ getCounterparty(contract) }}
                    </span>
                  </td>
                  <td class="px-6 py-4 text-sm font-medium text-gray-900">
                    ¥{{ formatAmount(contract.totalAmount) }}
                  </td>
                  <td class="px-6 py-4 text-sm text-gray-500">
                    {{ formatDate(contract.createTime) }}
                  </td>
                  <td class="px-6 py-4 text-center">
                    <span
                      :class="[
                        'inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold border',
                        statusConfig[contract.status]?.bgColor,
                        statusConfig[contract.status]?.color,
                        statusConfig[contract.status]?.borderColor
                      ]"
                    >
                      <component :is="statusIconMap[contract.status]" class="w-3 h-3" />
                      {{ statusConfig[contract.status]?.label || '未知' }}
                    </span>
                  </td>
                  <td class="px-6 py-4 text-right whitespace-nowrap" @click.stop>
                    <div class="flex justify-end gap-3 text-sm font-bold text-brand-600">
                      <button class="hover:text-brand-800" @click="viewContract(contract.id)">
                        查看
                      </button>
                      <button
                        v-if="contract.status === 1 && (!contract.buyerSigned || !contract.sellerSigned)"
                        class="hover:text-brand-800"
                        @click="openSignModal(contract.id)"
                      >
                        签署
                      </button>
                      <button
                        v-if="contract.status === 0"
                        class="text-red-500 hover:text-red-600"
                        @click="handleDelete(contract)"
                      >
                        删除
                      </button>
                      <button
                        v-if="contract.status === 0 || contract.status === 1"
                        class="text-amber-600 hover:text-amber-700"
                        @click="handleCancel(contract)"
                      >
                        取消
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>

            <!-- 分页 -->
            <div class="px-6 py-4 border-t border-gray-100 flex items-center justify-between">
              <p class="text-xs text-gray-500">
                显示第 {{ (currentPage - 1) * pageSize + 1 }} 至 {{ Math.min(currentPage * pageSize, filteredContracts.length) }} 条，共 {{ filteredContracts.length }} 条记录
              </p>
              <div class="flex gap-1">
                <button
                  class="w-8 h-8 flex items-center justify-center rounded border border-gray-200 text-gray-400 disabled:opacity-50"
                  :disabled="currentPage === 1"
                  @click="goToPage(currentPage - 1)"
                >
                  <ChevronLeft class="w-4 h-4" />
                </button>
                <template v-for="(page, idx) in pageNumbers" :key="idx">
                  <span v-if="page === '...'" class="px-1 text-gray-400 flex items-center">...</span>
                  <button
                    v-else
                    class="w-8 h-8 flex items-center justify-center rounded text-xs font-bold transition-colors"
                    :class="page === currentPage
                      ? 'bg-brand-600 text-white'
                      : 'border border-gray-200 text-gray-600 hover:bg-gray-50'"
                    @click="goToPage(page as number)"
                  >
                    {{ page }}
                  </button>
                </template>
                <button
                  class="w-8 h-8 flex items-center justify-center rounded border border-gray-200 text-gray-600 disabled:opacity-50 disabled:text-gray-400"
                  :disabled="currentPage === totalPages"
                  @click="goToPage(currentPage + 1)"
                >
                  <ChevronRight class="w-4 h-4" />
                </button>
              </div>
            </div>
          </template>
        </div>
      </div>

      <!-- 右侧边栏 -->
      <aside class="w-80 flex-shrink-0 flex flex-col gap-6">
        <!-- 合同统计 -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <h3 class="text-sm font-bold mb-6 flex items-center gap-2">
            <BarChart3 class="w-4 h-4 text-brand-600" />
            合同统计
          </h3>
          <div class="space-y-6">
            <!-- 待结算总额 -->
            <div>
              <p class="text-xs text-gray-500 mb-1">待结算总额 (CNY)</p>
              <p class="text-2xl font-black text-brand-600">¥{{ formatAmount(stats.pendingTotal) }}</p>
            </div>

            <!-- 网格统计 -->
            <div class="grid grid-cols-2 gap-4">
              <div class="bg-gray-50 p-3 rounded-lg">
                <p class="text-[10px] text-gray-500 uppercase font-bold">本月新增</p>
                <p class="text-lg font-bold">{{ stats.monthlyNew }} 份</p>
              </div>
              <div class="bg-gray-50 p-3 rounded-lg">
                <p class="text-[10px] text-gray-500 uppercase font-bold">完成率</p>
                <p class="text-lg font-bold">{{ stats.executionRate }}%</p>
              </div>
            </div>

            <!-- 进度条 -->
            <div class="pt-4 border-t border-gray-100">
              <div class="flex justify-between items-center text-xs mb-2">
                <span class="text-gray-500">履约中合同占比</span>
                <span class="font-bold">{{ stats.total > 0 ? Math.round((stats.executing / stats.total) * 100) : 0 }}%</span>
              </div>
              <div class="w-full h-1.5 bg-gray-100 rounded-full overflow-hidden">
                <div
                  class="h-full bg-autumn-500 transition-all"
                  :style="{ width: `${stats.total > 0 ? (stats.executing / stats.total) * 100 : 0}%` }"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 待办提醒 -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <h3 class="text-sm font-bold mb-4 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <AlertCircle class="w-4 h-4 text-amber-500" />
              待办提醒
            </div>
            <span
              v-if="todoItems.length > 0"
              class="bg-red-500 text-white text-[10px] px-1.5 py-0.5 rounded-full"
            >
              {{ todoItems.length }}
            </span>
          </h3>

          <div v-if="todoItems.length === 0" class="text-center py-6 text-gray-400 text-sm">
            暂无待办事项
          </div>

          <div v-else class="space-y-4">
            <div
              v-for="(item, idx) in todoItems"
              :key="idx"
              class="flex gap-3 group cursor-pointer"
              @click="viewContract(item.contractId)"
            >
              <div
                class="w-8 h-8 rounded-lg flex items-center justify-center shrink-0"
                :class="{
                  'bg-amber-50 text-amber-600': item.type === 'sign',
                  'bg-autumn-50 text-autumn-600': item.type === 'execute',
                  'bg-red-50 text-red-600': item.type === 'warning'
                }"
              >
                <Pen v-if="item.type === 'sign'" class="w-4 h-4" />
                <Package v-else-if="item.type === 'execute'" class="w-4 h-4" />
                <AlertCircle v-else class="w-4 h-4" />
              </div>
              <div class="min-w-0">
                <p class="text-sm font-bold text-gray-900 group-hover:text-brand-600 transition-colors truncate">
                  {{ item.title }}
                </p>
                <p class="text-xs text-gray-400 mt-1">{{ item.desc }}</p>
              </div>
            </div>
          </div>

          <button
            v-if="stats.pending > 0 || stats.executing > 0"
            class="w-full mt-6 py-2 text-xs font-bold text-gray-500 hover:text-brand-600 border border-dashed border-gray-200 rounded-lg transition-colors"
            @click="switchTab('pending')"
          >
            查看全部待办
          </button>
        </div>

      </aside>
    </div>

    <!-- 签署弹窗 -->
    <ContractSignModal
      v-model="signModalVisible"
      :contract-id="signContractId"
      @signed="onSigned"
    />
  </div>
</template>

<style scoped>
/* 表格行动画 */
tbody tr {
  animation: fade-in 0.2s ease-out;
}

@keyframes fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
</style>
