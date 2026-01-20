<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FileText, Search, Eye, Pen, Trash2, RefreshCw, XCircle, Calendar, Package, CheckCircle, Clock, AlertCircle, MoreHorizontal, FileEdit, FileClock, FileCheck, FileBox, FileCheck2, FileX } from 'lucide-vue-next'
import { listContracts, deleteContract, cancelContract, type ContractResponse, type ContractQuery } from '../api/contract'
import { Skeleton } from '../components/ui'
import ContractSignModal from '../components/contract/ContractSignModal.vue'

const router = useRouter()

// 状态
const loading = ref(false)
const contracts = ref<ContractResponse[]>([])
const searchKeyword = ref('')
const filterStatus = ref<number | null>(null)

// 签署弹窗
const signModalVisible = ref(false)
const signContractId = ref<number | null>(null)

// 状态配置（包含颜色、图标、色条）
const statusConfig: Record<number, { 
  label: string
  color: string
  barColor: string
  bgGradient: string
  iconColor: string
}> = {
  0: { 
    label: '草稿', 
    color: 'bg-gray-100 text-gray-600',
    barColor: 'bg-gray-400',
    bgGradient: 'from-gray-400 to-gray-500',
    iconColor: 'text-gray-500'
  },
  1: { 
    label: '待签署', 
    color: 'bg-amber-50 text-amber-600',
    barColor: 'bg-amber-500',
    bgGradient: 'from-amber-400 to-orange-500',
    iconColor: 'text-amber-500'
  },
  2: { 
    label: '已签署', 
    color: 'bg-brand-50 text-brand-600',
    barColor: 'bg-brand-500',
    bgGradient: 'from-brand-500 to-teal-600',
    iconColor: 'text-brand-500'
  },
  3: { 
    label: '履约中', 
    color: 'bg-blue-50 text-blue-600',
    barColor: 'bg-blue-500',
    bgGradient: 'from-blue-500 to-indigo-600',
    iconColor: 'text-blue-500'
  },
  4: { 
    label: '已完成', 
    color: 'bg-brand-100 text-brand-700',
    barColor: 'bg-brand-600',
    bgGradient: 'from-brand-600 to-green-700',
    iconColor: 'text-brand-600'
  },
  5: { 
    label: '已取消', 
    color: 'bg-red-50 text-red-500',
    barColor: 'bg-red-400',
    bgGradient: 'from-red-400 to-rose-500',
    iconColor: 'text-red-400'
  }
}

// 状态图标组件映射
const statusIconMap: Record<number, any> = {
  0: FileEdit,      // 草稿
  1: FileClock,     // 待签署
  2: FileCheck,     // 已签署
  3: FileBox,       // 履约中
  4: FileCheck2,    // 已完成
  5: FileX          // 已取消
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

// 统计数据
const stats = computed(() => {
  const all = contracts.value
  return {
    total: all.length,
    draft: all.filter(c => c.status === 0).length,
    pending: all.filter(c => c.status === 1).length,
    signed: all.filter(c => c.status === 2).length,
    executing: all.filter(c => c.status === 3).length,
    completed: all.filter(c => c.status === 4).length,
    cancelled: all.filter(c => c.status === 5).length,
  }
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

// 格式化金额
function formatAmount(val?: number): string {
  if (val == null) return '-'
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

// 格式化日期
function formatDate(val?: string): string {
  if (!val) return '-'
  return val.split('T')[0]
}

// 格式化完整时间
function formatDateTime(val?: string): string {
  if (!val) return '-'
  const d = new Date(val)
  return d.toLocaleString('zh-CN', { 
    month: '2-digit', 
    day: '2-digit', 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

onMounted(() => {
  loadContracts()
})
</script>

<template>
  <div class="contract-list-view">
    <!-- 顶部标题 -->
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">合同管理</h1>
        <p class="text-sm text-gray-500 mt-1">管理您的所有采购合同</p>
      </div>
      <button
        class="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-brand-600 to-teal-600 hover:from-brand-700 hover:to-teal-700 text-white text-sm font-bold rounded-xl transition-all  shadow-md shadow-brand-500/20"
        @click="loadContracts"
      >
        <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': loading }" />
        刷新
      </button>
    </div>
    
    <!-- 统计卡片 -->
    <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-7 gap-3 mb-6">
      <div 
        v-for="(info, status) in statusConfig" 
        :key="status"
        class="bg-white rounded-xl border border-gray-200 p-3 cursor-pointer transition-all hover:shadow-md"
        :class="{ 'ring-2 ring-brand-500 ring-offset-1': filterStatus === Number(status) }"
        @click="filterStatus = filterStatus === Number(status) ? null : Number(status)"
      >
        <div class="flex items-center gap-2">
          <component :is="statusIconMap[Number(status)]" class="w-4 h-4" :class="info.iconColor" />
          <span class="text-xs font-medium text-gray-500">{{ info.label }}</span>
        </div>
        <div class="text-xl font-bold text-gray-900 mt-1">
          {{ status === '0' ? stats.draft : 
             status === '1' ? stats.pending :
             status === '2' ? stats.signed :
             status === '3' ? stats.executing :
             status === '4' ? stats.completed :
             stats.cancelled }}
        </div>
      </div>
    </div>
    
    <!-- 主内容 -->
    <div class="space-y-6">
      <!-- 搜索栏 -->
      <div class="bg-white rounded-xl border border-gray-200 p-4">
        <div class="flex flex-wrap gap-4 items-center">
          <!-- 搜索框 -->
          <div class="flex-1 min-w-[280px] relative">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索合同编号、产品名称、公司名称..."
              class="w-full pl-10 pr-4 py-2.5 border-2 border-gray-200 rounded-xl focus:border-brand-500 outline-none transition-all text-sm"
            />
          </div>
          
          <!-- 快速筛选 -->
          <div class="flex gap-2 flex-wrap">
            <button
              :class="[
                'px-4 py-2 text-xs font-bold rounded-xl transition-all',
                filterStatus === null 
                  ? 'bg-gradient-to-r from-brand-600 to-teal-600 text-white shadow-md' 
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              ]"
              @click="filterStatus = null"
            >
              全部 ({{ stats.total }})
            </button>
            <button
              v-if="stats.pending > 0"
              :class="[
                'px-4 py-2 text-xs font-bold rounded-xl transition-all flex items-center gap-1',
                filterStatus === 1 
                  ? 'bg-amber-500 text-white shadow-md' 
                  : 'bg-amber-50 text-amber-600 hover:bg-amber-100'
              ]"
              @click="filterStatus = filterStatus === 1 ? null : 1"
            >
              <Clock class="w-3 h-3" />
              待签署 ({{ stats.pending }})
            </button>
            <button
              v-if="stats.executing > 0"
              :class="[
                'px-4 py-2 text-xs font-bold rounded-xl transition-all flex items-center gap-1',
                filterStatus === 3 
                  ? 'bg-blue-500 text-white shadow-md' 
                  : 'bg-blue-50 text-blue-600 hover:bg-blue-100'
              ]"
              @click="filterStatus = filterStatus === 3 ? null : 3"
            >
              <Package class="w-3 h-3" />
              履约中 ({{ stats.executing }})
            </button>
          </div>
        </div>
      </div>
      
      <!-- 加载状态 - 骨架屏 -->
      <div v-if="loading" class="space-y-4">
        <div v-for="i in 3" :key="i" class="bg-white rounded-xl border border-gray-200 p-5">
          <div class="flex gap-4">
            <Skeleton type="avatar" class="!w-14 !h-14 !rounded-xl" />
            <div class="flex-1 space-y-3">
              <Skeleton type="title" class="!w-48" />
              <Skeleton type="text" class="!w-72" />
              <div class="flex gap-4">
                <Skeleton type="text" class="!w-24" />
                <Skeleton type="text" class="!w-24" />
              </div>
            </div>
            <Skeleton type="title" class="!w-24" />
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="filteredContracts.length === 0" class="bg-white rounded-xl border border-gray-200 p-12 text-center">
        <div class="w-20 h-20 mx-auto rounded-xl bg-gradient-to-br from-gray-100 to-gray-200 flex items-center justify-center mb-4">
          <FileText class="w-10 h-10 text-gray-400" />
        </div>
        <p class="text-lg font-bold text-gray-700">暂无合同</p>
        <p class="text-sm text-gray-400 mt-2">在聊天中确认报价后可起草合同</p>
      </div>
      
      <!-- 合同列表 -->
      <div v-else class="space-y-4">
        <div
          v-for="(contract, idx) in filteredContracts"
          :key="contract.id"
          class="contract-card group bg-white rounded-xl border border-gray-200 hover:shadow-md hover:border-brand-100 transition-all cursor-pointer overflow-hidden"
          :style="{ animationDelay: `${idx * 50}ms` }"
          @click="viewContract(contract.id)"
        >
          <!-- 左侧状态色条 -->
          <div class="flex">
            <div 
              class="w-1.5 shrink-0 rounded-l-2xl"
              :class="statusConfig[contract.status]?.barColor || 'bg-gray-400'"
            />
            
            <div class="flex-1 p-5">
              <!-- 头部：图标 + 基本信息 + 金额 -->
              <div class="flex items-start gap-4">
                <!-- 合同图标（渐变） -->
                <div 
                  class="w-14 h-14 rounded-xl bg-gradient-to-br flex items-center justify-center shrink-0 shadow-md"
                  :class="statusConfig[contract.status]?.bgGradient || 'from-gray-400 to-gray-500'"
                >
                  <FileText class="w-7 h-7 text-white" />
                </div>
                
                <!-- 核心信息 -->
                <div class="flex-1 min-w-0">
                  <!-- 合同编号 + 状态 -->
                  <div class="flex items-center gap-3 flex-wrap">
                    <span class="text-base font-bold text-gray-900">{{ contract.contractNo }}</span>
                    <span 
                      :class="['text-[10px] font-bold px-2.5 py-1 rounded-full flex items-center gap-1', statusConfig[contract.status]?.color]"
                    >
                      <component :is="statusIconMap[contract.status]" class="w-3 h-3" />
                      {{ statusConfig[contract.status]?.label || '未知' }}
                    </span>
                    <span class="text-xs text-gray-400">{{ formatDate(contract.createTime) }}</span>
                  </div>
                  
                  <!-- 产品信息：数量 × 单价 -->
                  <div class="mt-2 text-sm text-gray-600">
                    <span class="font-medium text-gray-900">{{ contract.productName || '-' }}</span>
                    <span class="mx-2 text-gray-300">·</span>
                    <span>{{ contract.quantity }} {{ contract.unit }}</span>
                    <span v-if="contract.unitPrice" class="text-gray-400">
                      × ¥{{ formatAmount(contract.unitPrice) }}/{{ contract.unit }}
                    </span>
                  </div>
                </div>
                
                <!-- 金额（突出显示） -->
                <div class="text-right shrink-0">
                  <div class="text-2xl font-bold text-brand-600">
                    ¥{{ formatAmount(contract.totalAmount) }}
                  </div>
                  <div class="text-[10px] text-gray-400 mt-1">合同金额</div>
                </div>
              </div>
              
              <!-- 分隔线 -->
              <div class="border-t border-gray-50 my-4" />
              
              <!-- 签署信息 -->
              <div class="grid grid-cols-2 gap-4">
                <!-- 买方 -->
                <div class="flex items-center gap-3">
                  <div :class="['w-6 h-6 rounded-full flex items-center justify-center text-[10px]', contract.buyerSigned ? 'bg-brand-100' : 'bg-amber-100']">
                    <CheckCircle v-if="contract.buyerSigned" class="w-4 h-4 text-brand-600" />
                    <Clock v-else class="w-4 h-4 text-amber-500" />
                  </div>
                  <div class="min-w-0">
                    <div class="text-[10px] text-gray-400">买方</div>
                    <div class="text-xs font-medium text-gray-900 truncate">{{ contract.buyerCompanyName || '-' }}</div>
                    <div class="text-[10px] text-gray-400">
                      {{ contract.buyerSigned ? `已签署 ${formatDateTime(contract.buyerSignTime)}` : '待签署' }}
                    </div>
                  </div>
                </div>
                
                <!-- 卖方 -->
                <div class="flex items-center gap-3">
                  <div :class="['w-6 h-6 rounded-full flex items-center justify-center text-[10px]', contract.sellerSigned ? 'bg-brand-100' : 'bg-amber-100']">
                    <CheckCircle v-if="contract.sellerSigned" class="w-4 h-4 text-brand-600" />
                    <Clock v-else class="w-4 h-4 text-amber-500" />
                  </div>
                  <div class="min-w-0">
                    <div class="text-[10px] text-gray-400">卖方</div>
                    <div class="text-xs font-medium text-gray-900 truncate">{{ contract.sellerCompanyName || '-' }}</div>
                    <div class="text-[10px] text-gray-400">
                      {{ contract.sellerSigned ? `已签署 ${formatDateTime(contract.sellerSignTime)}` : '待签署' }}
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 履约进度（仅履约中状态显示） -->
              <div v-if="contract.status === 3" class="mt-4 p-3 bg-blue-50 rounded-xl">
                <div class="flex items-center justify-between text-xs">
                  <div class="flex items-center gap-2 text-blue-600 font-medium">
                    <Package class="w-4 h-4" />
                    履约进行中
                  </div>
                  <span class="text-blue-500">点击查看详情 →</span>
                </div>
              </div>
              
              <!-- 交付日期（如果有） -->
              <div v-if="contract.deliveryDate" class="mt-4 flex items-center gap-4 text-xs text-gray-500">
                <div class="flex items-center gap-1.5">
                  <Calendar class="w-3.5 h-3.5" />
                  <span>交付日期：{{ formatDate(contract.deliveryDate) }}</span>
                </div>
              </div>
              
              <!-- 操作按钮 -->
              <div class="flex justify-end gap-2 mt-4 opacity-0 group-hover:opacity-100 transition-opacity" @click.stop>
                <button
                  class="px-3 py-1.5 bg-gray-100 hover:bg-gray-200 text-gray-600 text-xs font-medium rounded-lg transition-all flex items-center gap-1"
                  @click="viewContract(contract.id)"
                >
                  <Eye class="w-3.5 h-3.5" />
                  查看
                </button>
                <button
                  v-if="contract.status === 1 && (!contract.buyerSigned || !contract.sellerSigned)"
                  class="px-3 py-1.5 bg-brand-50 hover:bg-brand-100 text-brand-600 text-xs font-medium rounded-lg transition-all flex items-center gap-1"
                  @click="openSignModal(contract.id)"
                >
                  <Pen class="w-3.5 h-3.5" />
                  签署
                </button>
                <button
                  v-if="contract.status === 0"
                  class="px-3 py-1.5 bg-red-50 hover:bg-red-100 text-red-500 text-xs font-medium rounded-lg transition-all flex items-center gap-1"
                  @click="handleDelete(contract)"
                >
                  <Trash2 class="w-3.5 h-3.5" />
                  删除
                </button>
                <button
                  v-if="contract.status === 0 || contract.status === 1"
                  class="px-3 py-1.5 bg-amber-50 hover:bg-amber-100 text-amber-600 text-xs font-medium rounded-lg transition-all flex items-center gap-1"
                  @click="handleCancel(contract)"
                >
                  <XCircle class="w-3.5 h-3.5" />
                  取消
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
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
/* 合同卡片入场动画 */
.contract-card {
  animation: slide-up 0.3s ease-out backwards;
}

@keyframes slide-up {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
