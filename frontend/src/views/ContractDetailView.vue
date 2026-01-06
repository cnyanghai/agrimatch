<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, FileText, Download, Pen, Check, Clock, 
  Building2, Package, MapPin, Calendar, CreditCard, 
  Shield, RefreshCw
} from 'lucide-vue-next'
import { getContract, sendContractForSigning, getContractPdfUrl, type ContractResponse } from '../api/contract'
import ContractSignModal from '../components/contract/ContractSignModal.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const contract = ref<ContractResponse | null>(null)
const signModalVisible = ref(false)

// 状态映射
const statusMap: Record<number, { label: string; color: string; bgColor: string }> = {
  0: { label: '草稿', color: 'text-gray-600', bgColor: 'bg-gray-100' },
  1: { label: '待签署', color: 'text-amber-600', bgColor: 'bg-amber-50' },
  2: { label: '已签署', color: 'text-emerald-600', bgColor: 'bg-emerald-50' },
  3: { label: '履约中', color: 'text-blue-600', bgColor: 'bg-blue-50' },
  4: { label: '已完成', color: 'text-emerald-700', bgColor: 'bg-emerald-100' },
  5: { label: '已取消', color: 'text-red-500', bgColor: 'bg-red-50' }
}

const statusInfo = computed(() => {
  if (!contract.value) return statusMap[0]
  return statusMap[contract.value.status] || statusMap[0]
})

// 是否可以签署
const canSign = computed(() => {
  if (!contract.value) return false
  return contract.value.status === 1 && (!contract.value.buyerSigned || !contract.value.sellerSigned)
})

// 是否可以发送签署
const canSendForSign = computed(() => {
  if (!contract.value) return false
  return contract.value.status === 0
})

// 加载合同
async function loadContract() {
  const id = Number(route.params.id)
  if (!id) return
  
  loading.value = true
  try {
    const res = await getContract(id)
    if (res.code === 0 && res.data) {
      contract.value = res.data
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 发送签署
async function handleSendForSign() {
  if (!contract.value) return
  
  try {
    const res = await sendContractForSigning(contract.value.id)
    if (res.code === 0) {
      ElMessage.success('已发送给对方签署')
      loadContract()
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '发送失败')
  }
}

// 打开签署弹窗
function openSignModal() {
  signModalVisible.value = true
}

// 签署完成
function onSigned() {
  loadContract()
}

// 下载 PDF
function downloadPdf() {
  if (!contract.value) return
  const url = getContractPdfUrl(contract.value.id)
  window.open(url, '_blank')
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

onMounted(() => {
  loadContract()
})
</script>

<template>
  <div class="contract-detail-view">
    <!-- 顶部 -->
    <div class="mb-6 flex items-center justify-between">
      <div class="flex items-center gap-4">
        <button
          class="w-10 h-10 rounded-xl bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-colors"
          @click="router.push('/contracts')"
        >
          <ArrowLeft class="w-5 h-5 text-gray-600" />
        </button>
        <div>
          <h1 class="text-2xl font-bold text-gray-900">合同详情</h1>
          <p class="text-sm text-gray-500 mt-0.5">{{ contract?.contractNo || '...' }}</p>
        </div>
      </div>
      
      <div class="flex gap-3">
        <button
          class="flex items-center gap-2 px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-bold rounded-xl transition-all active:scale-95"
          @click="loadContract"
        >
          <RefreshCw class="w-4 h-4" />
          刷新
        </button>
        <button
          v-if="canSendForSign"
          class="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-bold rounded-xl transition-all active:scale-95"
          @click="handleSendForSign"
        >
          发送签署
        </button>
        <button
          v-if="canSign"
          class="flex items-center gap-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-bold rounded-xl transition-all active:scale-95"
          @click="openSignModal"
        >
          <Pen class="w-4 h-4" />
          签署合同
        </button>
        <button
          class="flex items-center gap-2 px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white text-sm font-bold rounded-xl transition-all active:scale-95"
          @click="downloadPdf"
        >
          <Download class="w-4 h-4" />
          下载 PDF
        </button>
      </div>
    </div>
    
    <!-- 主内容 -->
    <div class="space-y-6">
      <div v-if="loading" class="flex items-center justify-center py-20">
        <div class="text-gray-400">加载中...</div>
      </div>
      
      <div v-else-if="contract" class="max-w-4xl mx-auto space-y-6">
          <!-- 状态卡片 -->
          <div class="bg-white rounded-2xl border border-gray-100 p-6">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-4">
                <div class="w-14 h-14 rounded-2xl bg-slate-900 flex items-center justify-center">
                  <FileText class="w-7 h-7 text-white" />
                </div>
                <div>
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">采购合同</div>
                  <div class="text-xl font-bold text-gray-900 mt-1">{{ contract.contractNo }}</div>
                  <div class="text-sm text-gray-500 mt-0.5">创建于 {{ formatDate(contract.createTime) }}</div>
                </div>
              </div>
              <div :class="['px-4 py-2 rounded-full text-sm font-bold', statusInfo.bgColor, statusInfo.color]">
                {{ statusInfo.label }}
              </div>
            </div>
            
            <!-- 签署状态 -->
            <div class="grid grid-cols-2 gap-4 mt-6 pt-6 border-t border-gray-100">
              <div class="bg-gray-50 rounded-xl p-4">
                <div class="flex items-center gap-3">
                  <div :class="['w-10 h-10 rounded-xl flex items-center justify-center', contract.buyerSigned ? 'bg-emerald-100' : 'bg-amber-100']">
                    <Check v-if="contract.buyerSigned" class="w-5 h-5 text-emerald-600" />
                    <Clock v-else class="w-5 h-5 text-amber-500" />
                  </div>
                  <div>
                    <div class="text-xs text-gray-500">买方签署</div>
                    <div :class="['font-bold', contract.buyerSigned ? 'text-emerald-600' : 'text-amber-600']">
                      {{ contract.buyerSigned ? '已签署' : '待签署' }}
                    </div>
                  </div>
                </div>
                <div class="mt-2 text-sm text-gray-600">{{ contract.buyerCompanyName || '-' }}</div>
                <div v-if="contract.buyerSignTime" class="text-xs text-gray-400 mt-1">
                  签署时间：{{ formatDate(contract.buyerSignTime) }}
                </div>
              </div>
              
              <div class="bg-gray-50 rounded-xl p-4">
                <div class="flex items-center gap-3">
                  <div :class="['w-10 h-10 rounded-xl flex items-center justify-center', contract.sellerSigned ? 'bg-emerald-100' : 'bg-amber-100']">
                    <Check v-if="contract.sellerSigned" class="w-5 h-5 text-emerald-600" />
                    <Clock v-else class="w-5 h-5 text-amber-500" />
                  </div>
                  <div>
                    <div class="text-xs text-gray-500">卖方签署</div>
                    <div :class="['font-bold', contract.sellerSigned ? 'text-emerald-600' : 'text-amber-600']">
                      {{ contract.sellerSigned ? '已签署' : '待签署' }}
                    </div>
                  </div>
                </div>
                <div class="mt-2 text-sm text-gray-600">{{ contract.sellerCompanyName || '-' }}</div>
                <div v-if="contract.sellerSignTime" class="text-xs text-gray-400 mt-1">
                  签署时间：{{ formatDate(contract.sellerSignTime) }}
                </div>
              </div>
            </div>
          </div>
          
          <!-- 交易信息 -->
          <div class="bg-white rounded-2xl border border-gray-100 p-6">
            <h3 class="text-sm font-bold text-gray-900 mb-4 flex items-center gap-2">
              <Package class="w-4 h-4 text-emerald-600" />
              交易标的
            </h3>
            
            <div class="bg-emerald-50 rounded-xl p-4 mb-4">
              <div class="flex justify-between items-start">
                <div>
                  <div class="text-lg font-bold text-gray-900">{{ contract.productName }}</div>
                  <div class="text-sm text-gray-600 mt-1">
                    {{ contract.quantity }} {{ contract.unit }} × ¥{{ formatAmount(contract.unitPrice) }}/{{ contract.unit }}
                  </div>
                </div>
                <div class="text-right">
                  <div class="text-xs text-gray-500">合同金额</div>
                  <div class="text-2xl font-bold text-emerald-600">¥{{ formatAmount(contract.totalAmount) }}</div>
                </div>
              </div>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                <MapPin class="w-5 h-5 text-gray-400" />
                <div>
                  <div class="text-xs text-gray-500">交付地点</div>
                  <div class="text-sm font-medium text-gray-900">{{ contract.deliveryAddress || '-' }}</div>
                </div>
              </div>
              <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                <Calendar class="w-5 h-5 text-gray-400" />
                <div>
                  <div class="text-xs text-gray-500">交付日期</div>
                  <div class="text-sm font-medium text-gray-900">{{ formatDate(contract.deliveryDate) }}</div>
                </div>
              </div>
              <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                <CreditCard class="w-5 h-5 text-gray-400" />
                <div>
                  <div class="text-xs text-gray-500">付款方式</div>
                  <div class="text-sm font-medium text-gray-900">{{ contract.paymentMethod || '-' }}</div>
                </div>
              </div>
              <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                <Shield class="w-5 h-5 text-gray-400" />
                <div>
                  <div class="text-xs text-gray-500">交付方式</div>
                  <div class="text-sm font-medium text-gray-900">{{ contract.deliveryMode || '-' }}</div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 双方信息 -->
          <div class="grid grid-cols-2 gap-6">
            <div class="bg-white rounded-2xl border border-gray-100 p-6">
              <h3 class="text-sm font-bold text-gray-900 mb-4 flex items-center gap-2">
                <Building2 class="w-4 h-4 text-blue-600" />
                买方信息
              </h3>
              <div class="text-lg font-bold text-gray-900">{{ contract.buyerCompanyName || '-' }}</div>
            </div>
            
            <div class="bg-white rounded-2xl border border-gray-100 p-6">
              <h3 class="text-sm font-bold text-gray-900 mb-4 flex items-center gap-2">
                <Building2 class="w-4 h-4 text-emerald-600" />
                卖方信息
              </h3>
              <div class="text-lg font-bold text-gray-900">{{ contract.sellerCompanyName || '-' }}</div>
            </div>
          </div>
          
          <!-- PDF 哈希 -->
          <div v-if="contract.pdfHash" class="bg-white rounded-2xl border border-gray-100 p-6">
            <h3 class="text-sm font-bold text-gray-900 mb-2">合同防伪码</h3>
            <div class="bg-gray-50 rounded-xl p-3 font-mono text-xs text-gray-600 break-all">
              {{ contract.pdfHash }}
            </div>
            <p class="text-xs text-gray-400 mt-2">
              此哈希值可用于验证合同文件的完整性和真实性
            </p>
          </div>
      </div>
    </div>
    
    <!-- 签署弹窗 -->
    <ContractSignModal
      v-model="signModalVisible"
      :contract-id="contract?.id || null"
      @signed="onSigned"
    />
  </div>
</template>
