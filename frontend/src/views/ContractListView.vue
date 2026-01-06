<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { FileText, Search, Eye, Pen, Trash2, RefreshCw, XCircle } from 'lucide-vue-next'
import { listContracts, deleteContract, cancelContract, type ContractResponse, type ContractQuery } from '../api/contract'
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

// 状态映射
const statusMap: Record<number, { label: string; color: string }> = {
  0: { label: '草稿', color: 'bg-gray-100 text-gray-600' },
  1: { label: '待签署', color: 'bg-amber-50 text-amber-600' },
  2: { label: '已签署', color: 'bg-emerald-50 text-emerald-600' },
  3: { label: '履约中', color: 'bg-blue-50 text-blue-600' },
  4: { label: '已完成', color: 'bg-emerald-100 text-emerald-700' },
  5: { label: '已取消', color: 'bg-red-50 text-red-500' }
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
  
  if (!confirm('确定要删除此合同吗？')) return
  
  try {
    const res = await deleteContract(contract.id)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      loadContracts()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '删除失败')
  }
}

// 取消合同
async function handleCancel(contract: ContractResponse) {
  if (contract.status !== 0 && contract.status !== 1) {
    ElMessage.warning('只有草稿或待签署状态的合同可以取消')
    return
  }
  
  const reason = prompt('请输入取消原因（可选）：')
  if (reason === null) return // 用户点击了取消按钮
  
  try {
    const res = await cancelContract(contract.id, reason || undefined)
    if (res.code === 0) {
      ElMessage.success('合同已取消')
      loadContracts()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '取消失败')
  }
}

// 格式化金额
function formatAmount(val?: number): string {
  if (val == null) return '-'
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

// 格式化时间
function formatTime(val?: string): string {
  if (!val) return '-'
  return val.split('T')[0]
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
        class="flex items-center gap-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-bold rounded-xl transition-all active:scale-95"
        @click="loadContracts"
      >
        <RefreshCw class="w-4 h-4" />
        刷新
      </button>
    </div>
    
    <!-- 主内容 -->
    <div class="space-y-6">
          <!-- 搜索和筛选 -->
          <div class="bg-white rounded-2xl border border-gray-100 p-4">
            <div class="flex flex-wrap gap-4 items-center">
              <!-- 搜索框 -->
              <div class="flex-1 min-w-[200px] relative">
                <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input
                  v-model="searchKeyword"
                  type="text"
                  placeholder="搜索合同编号、产品、公司..."
                  class="w-full pl-10 pr-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-sm"
                />
              </div>
              
              <!-- 状态筛选 -->
              <div class="flex gap-2">
                <button
                  :class="[
                    'px-3 py-2 text-xs font-bold rounded-xl transition-all',
                    filterStatus === null ? 'bg-emerald-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  ]"
                  @click="filterStatus = null"
                >
                  全部
                </button>
                <button
                  v-for="(info, status) in statusMap"
                  :key="status"
                  :class="[
                    'px-3 py-2 text-xs font-bold rounded-xl transition-all',
                    filterStatus === Number(status) ? 'bg-emerald-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  ]"
                  @click="filterStatus = Number(status)"
                >
                  {{ info.label }}
                </button>
              </div>
            </div>
          </div>
          
          <!-- 合同列表 -->
          <div v-if="loading" class="flex items-center justify-center py-20">
            <div class="text-gray-400">加载中...</div>
          </div>
          
          <div v-else-if="filteredContracts.length === 0" class="bg-white rounded-2xl border border-gray-100 p-12 text-center">
            <div class="w-16 h-16 mx-auto rounded-2xl bg-gray-100 flex items-center justify-center mb-4">
              <FileText class="w-8 h-8 text-gray-400" />
            </div>
            <p class="text-gray-500">暂无合同</p>
            <p class="text-xs text-gray-400 mt-1">在聊天中确认报价后可起草合同</p>
          </div>
          
          <div v-else class="space-y-4">
            <div
              v-for="contract in filteredContracts"
              :key="contract.id"
              class="bg-white rounded-2xl border border-gray-100 hover:shadow-lg hover:border-emerald-100 transition-all cursor-pointer overflow-hidden"
              @click="viewContract(contract.id)"
            >
              <div class="p-5">
                <div class="flex items-start justify-between gap-4">
                  <!-- 左侧信息 -->
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center gap-3 mb-2">
                      <div class="w-10 h-10 rounded-xl bg-slate-900 flex items-center justify-center shrink-0">
                        <FileText class="w-5 h-5 text-white" />
                      </div>
                      <div>
                        <div class="text-sm font-bold text-gray-900">{{ contract.contractNo }}</div>
                        <div class="text-xs text-gray-500">{{ formatTime(contract.createTime) }}</div>
                      </div>
                      <span :class="['text-[10px] font-bold px-2 py-1 rounded-full', statusMap[contract.status]?.color || 'bg-gray-100 text-gray-600']">
                        {{ statusMap[contract.status]?.label || '未知' }}
                      </span>
                    </div>
                    
                    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mt-4">
                      <div>
                        <div class="text-[10px] text-gray-400 uppercase tracking-wider">产品</div>
                        <div class="text-sm font-medium text-gray-900 truncate">{{ contract.productName || '-' }}</div>
                      </div>
                      <div>
                        <div class="text-[10px] text-gray-400 uppercase tracking-wider">数量</div>
                        <div class="text-sm font-medium text-gray-900">{{ contract.quantity }} {{ contract.unit }}</div>
                      </div>
                      <div>
                        <div class="text-[10px] text-gray-400 uppercase tracking-wider">买方</div>
                        <div class="text-sm font-medium text-gray-900 truncate">{{ contract.buyerCompanyName || '-' }}</div>
                      </div>
                      <div>
                        <div class="text-[10px] text-gray-400 uppercase tracking-wider">卖方</div>
                        <div class="text-sm font-medium text-gray-900 truncate">{{ contract.sellerCompanyName || '-' }}</div>
                      </div>
                    </div>
                  </div>
                  
                  <!-- 右侧金额和操作 -->
                  <div class="text-right shrink-0">
                    <div class="text-xl font-bold text-emerald-600">¥{{ formatAmount(contract.totalAmount) }}</div>
                    <div class="flex gap-2 mt-3" @click.stop>
                      <button
                        class="p-2 bg-gray-100 hover:bg-gray-200 rounded-lg transition-all"
                        title="查看详情"
                        @click="viewContract(contract.id)"
                      >
                        <Eye class="w-4 h-4 text-gray-600" />
                      </button>
                      <button
                        v-if="contract.status === 1 && (!contract.buyerSigned || !contract.sellerSigned)"
                        class="p-2 bg-emerald-50 hover:bg-emerald-100 rounded-lg transition-all"
                        title="签署"
                        @click="openSignModal(contract.id)"
                      >
                        <Pen class="w-4 h-4 text-emerald-600" />
                      </button>
                      <button
                        v-if="contract.status === 0"
                        class="p-2 bg-red-50 hover:bg-red-100 rounded-lg transition-all"
                        title="删除"
                        @click="handleDelete(contract)"
                      >
                        <Trash2 class="w-4 h-4 text-red-500" />
                      </button>
                      <button
                        v-if="contract.status === 0 || contract.status === 1"
                        class="p-2 bg-amber-50 hover:bg-amber-100 rounded-lg transition-all"
                        title="取消合同"
                        @click="handleCancel(contract)"
                      >
                        <XCircle class="w-4 h-4 text-amber-600" />
                      </button>
                    </div>
                  </div>
                </div>
                
                <!-- 签署状态 -->
                <div class="flex gap-4 mt-4 pt-4 border-t border-gray-50">
                  <div class="flex items-center gap-2">
                    <div :class="['w-2 h-2 rounded-full', contract.buyerSigned ? 'bg-emerald-500' : 'bg-amber-400']" />
                    <span class="text-xs text-gray-500">买方：{{ contract.buyerSigned ? '已签署' : '待签署' }}</span>
                  </div>
                  <div class="flex items-center gap-2">
                    <div :class="['w-2 h-2 rounded-full', contract.sellerSigned ? 'bg-emerald-500' : 'bg-amber-400']" />
                    <span class="text-xs text-gray-500">卖方：{{ contract.sellerSigned ? '已签署' : '待签署' }}</span>
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
