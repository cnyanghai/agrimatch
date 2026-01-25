<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, FileText, Download, Pen, Check, Clock, 
  Building2, Package, MapPin, Calendar, CreditCard, 
  Shield, RefreshCw, Phone, User, Landmark, ChevronDown, ChevronUp,
  ScrollText, AlertTriangle, Scale
} from 'lucide-vue-next'
import {
  getContract, sendContractForSigning, getContractPdfUrl,
  getPaymentMethodText, getDeliveryModeText, parseBankInfo,
  contractStatusMap,
  type ContractResponse,
  type MilestoneResponse
} from '../api/contract'
import ContractSignModal from '../components/contract/ContractSignModal.vue'
import MilestoneList from '../components/contract/MilestoneList.vue'
import MilestoneCreateModal from '../components/contract/MilestoneCreateModal.vue'
import MilestoneSubmitModal from '../components/contract/MilestoneSubmitModal.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const contract = ref<ContractResponse | null>(null)
const signModalVisible = ref(false)

// 履约管理
const milestoneListRef = ref<InstanceType<typeof MilestoneList> | null>(null)
const createMilestoneVisible = ref(false)
const submitMilestoneVisible = ref(false)
const currentMilestone = ref<MilestoneResponse | null>(null)

// 折叠状态
const showBuyerDetail = ref(false)
const showSellerDetail = ref(false)
const showTerms = ref(false)

const statusInfo = computed(() => {
  if (!contract.value) return contractStatusMap[0]!
  return contractStatusMap[contract.value.status] ?? contractStatusMap[0]!
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

// 解析银行信息
const buyerBank = computed(() => parseBankInfo(contract.value?.buyerBankInfo))
const sellerBank = computed(() => parseBankInfo(contract.value?.sellerBankInfo))

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

// 打开添加履约节点弹窗
function openCreateMilestone() {
  createMilestoneVisible.value = true
}

// 履约节点创建完成
function onMilestoneCreated() {
  loadContract() // 刷新合同状态
  milestoneListRef.value?.refresh()
}

// 打开提交凭证弹窗
function openSubmitMilestone(milestone: MilestoneResponse) {
  currentMilestone.value = milestone
  submitMilestoneVisible.value = true
}

// 凭证提交完成
function onMilestoneSubmitted() {
  milestoneListRef.value?.refresh()
}

// 履约刷新后检查合同状态
function onMilestoneRefresh() {
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
  return val.split('T')[0] || '-'
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
          class="flex items-center gap-2 px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-bold rounded-xl transition-all "
          @click="loadContract"
        >
          <RefreshCw class="w-4 h-4" />
          刷新
        </button>
        <button
          v-if="canSendForSign"
          class="flex items-center gap-2 px-4 py-2 bg-autumn-600 hover:bg-autumn-700 text-white text-sm font-bold rounded-xl transition-all "
          @click="handleSendForSign"
        >
          发送签署
        </button>
        <button
          v-if="canSign"
          class="flex items-center gap-2 px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white text-sm font-bold rounded-xl transition-all "
          @click="openSignModal"
        >
          <Pen class="w-4 h-4" />
          签署合同
        </button>
        <button
          class="flex items-center gap-2 px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white text-sm font-bold rounded-xl transition-all "
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
      
      <div v-else-if="contract" class="max-w-5xl mx-auto space-y-6">
        <!-- 状态卡片 -->
        <div class="bg-white rounded-xl border border-gray-200 p-6">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-4">
              <div class="w-14 h-14 rounded-xl bg-slate-900 flex items-center justify-center">
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
          <div class="grid grid-cols-2 gap-4 mt-6 pt-6 border-t border-gray-200">
            <div class="bg-gray-50 rounded-xl p-4">
              <div class="flex items-center gap-3">
                <div :class="['w-10 h-10 rounded-xl flex items-center justify-center', contract.buyerSigned ? 'bg-brand-100' : 'bg-amber-100']">
                  <Check v-if="contract.buyerSigned" class="w-5 h-5 text-brand-600" />
                  <Clock v-else class="w-5 h-5 text-amber-500" />
                </div>
                <div>
                  <div class="text-xs text-gray-500">买方签署</div>
                  <div :class="['font-bold', contract.buyerSigned ? 'text-brand-600' : 'text-amber-600']">
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
                <div :class="['w-10 h-10 rounded-xl flex items-center justify-center', contract.sellerSigned ? 'bg-brand-100' : 'bg-amber-100']">
                  <Check v-if="contract.sellerSigned" class="w-5 h-5 text-brand-600" />
                  <Clock v-else class="w-5 h-5 text-amber-500" />
                </div>
                <div>
                  <div class="text-xs text-gray-500">卖方签署</div>
                  <div :class="['font-bold', contract.sellerSigned ? 'text-brand-600' : 'text-amber-600']">
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

        <!-- 履约进度（已签署及之后的状态显示） -->
        <div v-if="contract.status >= 2" class="bg-white rounded-xl border border-gray-200 p-6">
          <MilestoneList
            ref="milestoneListRef"
            :contract-id="contract.id"
            :contract-status="contract.status"
            :buyer-company-id="contract.buyerCompanyId"
            :seller-company-id="contract.sellerCompanyId"
            @add="openCreateMilestone"
            @submit="openSubmitMilestone"
            @refresh="onMilestoneRefresh"
          />
        </div>

        <!-- 甲方（买方）信息 -->
        <div class="bg-white rounded-xl border border-gray-200 overflow-hidden">
          <button
            class="w-full p-6 flex items-center justify-between hover:bg-gray-50/50 transition-colors"
            @click="showBuyerDetail = !showBuyerDetail"
          >
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-xl bg-autumn-50 flex items-center justify-center">
                <Building2 class="w-5 h-5 text-autumn-600" />
              </div>
              <div class="text-left">
                <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">甲方（买方）</div>
                <div class="text-lg font-bold text-gray-900 mt-0.5">{{ contract.buyerCompanyName || '-' }}</div>
              </div>
            </div>
            <component :is="showBuyerDetail ? ChevronUp : ChevronDown" class="w-5 h-5 text-gray-400" />
          </button>
          
          <div v-if="showBuyerDetail" class="px-6 pb-6 pt-0 border-t border-gray-200">
            <div class="grid grid-cols-2 gap-4 mt-4">
              <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                <Shield class="w-5 h-5 text-gray-400" />
                <div>
                  <div class="text-xs text-gray-500">统一社会信用代码</div>
                  <div class="text-sm font-medium text-gray-900">{{ contract.buyerLicenseNo || '-' }}</div>
                </div>
              </div>
              <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                <User class="w-5 h-5 text-gray-400" />
                <div>
                  <div class="text-xs text-gray-500">联系人</div>
                  <div class="text-sm font-medium text-gray-900">{{ contract.buyerContacts || '-' }}</div>
                </div>
              </div>
              <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                <Phone class="w-5 h-5 text-gray-400" />
                <div>
                  <div class="text-xs text-gray-500">联系电话</div>
                  <div class="text-sm font-medium text-gray-900">{{ contract.buyerPhone || '-' }}</div>
                </div>
              </div>
              <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                <MapPin class="w-5 h-5 text-gray-400" />
                <div>
                  <div class="text-xs text-gray-500">地址</div>
                  <div class="text-sm font-medium text-gray-900">{{ contract.buyerAddress || '-' }}</div>
                </div>
              </div>
            </div>
            <!-- 银行信息 -->
            <div v-if="buyerBank" class="mt-4 p-4 bg-autumn-50 rounded-xl">
              <div class="flex items-center gap-2 mb-2">
                <Landmark class="w-4 h-4 text-autumn-600" />
                <span class="text-xs font-bold text-autumn-600">银行账户信息</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm">
                <div>
                  <span class="text-gray-500">开户行：</span>
                  <span class="text-gray-900">{{ buyerBank.bankName || '-' }}</span>
                </div>
                <div>
                  <span class="text-gray-500">户名：</span>
                  <span class="text-gray-900">{{ buyerBank.accountName || '-' }}</span>
                </div>
                <div>
                  <span class="text-gray-500">账号：</span>
                  <span class="text-gray-900 font-mono">{{ buyerBank.accountNo || '-' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 乙方（卖方）信息 -->
        <div class="bg-white rounded-xl border border-gray-200 overflow-hidden">
          <button
            class="w-full p-6 flex items-center justify-between hover:bg-gray-50/50 transition-colors"
            @click="showSellerDetail = !showSellerDetail"
          >
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-xl bg-brand-50 flex items-center justify-center">
                <Building2 class="w-5 h-5 text-brand-600" />
              </div>
              <div class="text-left">
                <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">乙方（卖方）</div>
                <div class="text-lg font-bold text-gray-900 mt-0.5">{{ contract.sellerCompanyName || '-' }}</div>
              </div>
            </div>
            <component :is="showSellerDetail ? ChevronUp : ChevronDown" class="w-5 h-5 text-gray-400" />
          </button>
          
          <div v-if="showSellerDetail" class="px-6 pb-6 pt-0 border-t border-gray-200">
            <div class="grid grid-cols-2 gap-4 mt-4">
              <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                <Shield class="w-5 h-5 text-gray-400" />
                <div>
                  <div class="text-xs text-gray-500">统一社会信用代码</div>
                  <div class="text-sm font-medium text-gray-900">{{ contract.sellerLicenseNo || '-' }}</div>
                </div>
              </div>
              <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                <User class="w-5 h-5 text-gray-400" />
                <div>
                  <div class="text-xs text-gray-500">联系人</div>
                  <div class="text-sm font-medium text-gray-900">{{ contract.sellerContacts || '-' }}</div>
                </div>
              </div>
              <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                <Phone class="w-5 h-5 text-gray-400" />
                <div>
                  <div class="text-xs text-gray-500">联系电话</div>
                  <div class="text-sm font-medium text-gray-900">{{ contract.sellerPhone || '-' }}</div>
                </div>
              </div>
              <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                <MapPin class="w-5 h-5 text-gray-400" />
                <div>
                  <div class="text-xs text-gray-500">地址</div>
                  <div class="text-sm font-medium text-gray-900">{{ contract.sellerAddress || '-' }}</div>
                </div>
              </div>
            </div>
            <!-- 银行信息 -->
            <div v-if="sellerBank" class="mt-4 p-4 bg-brand-50 rounded-xl">
              <div class="flex items-center gap-2 mb-2">
                <Landmark class="w-4 h-4 text-brand-600" />
                <span class="text-xs font-bold text-brand-600">银行账户信息</span>
              </div>
              <div class="grid grid-cols-3 gap-4 text-sm">
                <div>
                  <span class="text-gray-500">开户行：</span>
                  <span class="text-gray-900">{{ sellerBank.bankName || '-' }}</span>
                </div>
                <div>
                  <span class="text-gray-500">户名：</span>
                  <span class="text-gray-900">{{ sellerBank.accountName || '-' }}</span>
                </div>
                <div>
                  <span class="text-gray-500">账号：</span>
                  <span class="text-gray-900 font-mono">{{ sellerBank.accountNo || '-' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 交易信息 -->
        <div class="bg-white rounded-xl border border-gray-200 p-6">
          <h3 class="text-sm font-bold text-gray-900 mb-4 flex items-center gap-2">
            <Package class="w-4 h-4 text-brand-600" />
            交易标的
          </h3>
          
          <div class="bg-brand-50 rounded-xl p-4 mb-4">
            <div class="flex justify-between items-start">
              <div>
                <div class="text-lg font-bold text-gray-900">{{ contract.productName }}</div>
                <div class="text-sm text-gray-600 mt-1">
                  类目：{{ contract.categoryName || '-' }}
                </div>
                <div class="text-sm text-gray-600 mt-1">
                  {{ contract.quantity }} {{ contract.unit }} × ¥{{ formatAmount(contract.unitPrice) }}/{{ contract.unit }}
                </div>
              </div>
              <div class="text-right">
                <div class="text-xs text-gray-500">合同金额</div>
                <div class="text-2xl font-bold text-brand-600">¥{{ formatAmount(contract.totalAmount) }}</div>
              </div>
            </div>
          </div>
          
          <!-- 产品参数 -->
          <div v-if="contract.productParams && contract.productParams.length > 0" class="mb-4">
            <div class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">产品参数</div>
            <div class="grid grid-cols-3 gap-3">
              <div 
                v-for="param in contract.productParams" 
                :key="param.label"
                class="p-3 bg-gray-50 rounded-xl"
              >
                <div class="text-xs text-gray-500">{{ param.label }}</div>
                <div class="text-sm font-medium text-gray-900">{{ param.value }}</div>
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
                <div class="text-sm font-medium text-gray-900">{{ getPaymentMethodText(contract.paymentMethod) }}</div>
              </div>
            </div>
            <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
              <Shield class="w-5 h-5 text-gray-400" />
              <div>
                <div class="text-xs text-gray-500">交付方式</div>
                <div class="text-sm font-medium text-gray-900">{{ getDeliveryModeText(contract.deliveryMode) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 合同条款 -->
        <div class="bg-white rounded-xl border border-gray-200 overflow-hidden">
          <button
            class="w-full p-6 flex items-center justify-between hover:bg-gray-50/50 transition-colors"
            @click="showTerms = !showTerms"
          >
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-xl bg-slate-100 flex items-center justify-center">
                <ScrollText class="w-5 h-5 text-slate-600" />
              </div>
              <div class="text-left">
                <div class="text-sm font-bold text-gray-900">合同条款</div>
                <div class="text-xs text-gray-500 mt-0.5">包含质量标准、违约责任、争议解决等法律条款</div>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-xs text-gray-400">{{ showTerms ? '收起' : '展开查看' }}</span>
              <component :is="showTerms ? ChevronUp : ChevronDown" class="w-5 h-5 text-gray-400" />
            </div>
          </button>
          
          <div v-if="showTerms" class="px-6 pb-6 pt-0 border-t border-gray-200">
            <div class="mt-4 p-6 bg-gray-50 rounded-xl">
              <pre class="whitespace-pre-wrap text-sm text-gray-700 font-sans leading-relaxed">{{ contract.formattedTerms || '暂无条款内容' }}</pre>
            </div>
            
            <!-- 法律提示 -->
            <div class="mt-4 p-4 bg-amber-50 rounded-xl">
              <div class="flex items-start gap-3">
                <AlertTriangle class="w-5 h-5 text-amber-500 shrink-0 mt-0.5" />
                <div>
                  <div class="text-sm font-bold text-amber-700">法律提示</div>
                  <div class="text-xs text-amber-600 mt-1">
                    本合同条款具有法律效力。签署前请仔细阅读所有条款，如有疑问请咨询专业法律人士。合同一经双方签署即生效，对双方均具有约束力。
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- PDF 哈希 -->
        <div v-if="contract.pdfHash" class="bg-white rounded-xl border border-gray-200 p-6">
          <div class="flex items-center gap-2 mb-3">
            <Scale class="w-4 h-4 text-gray-500" />
            <h3 class="text-sm font-bold text-gray-900">合同防伪码</h3>
          </div>
          <div class="bg-gray-50 rounded-xl p-3 font-mono text-xs text-gray-600 break-all">
            {{ contract.pdfHash }}
          </div>
          <p class="text-xs text-gray-400 mt-2">
            此哈希值基于 SHA-256 算法生成，可用于验证合同 PDF 文件的完整性和真实性
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
    
    <!-- 添加履约节点弹窗 -->
    <MilestoneCreateModal
      v-model="createMilestoneVisible"
      :contract-id="contract?.id || 0"
      @created="onMilestoneCreated"
    />
    
    <!-- 提交凭证弹窗 -->
    <MilestoneSubmitModal
      v-model="submitMilestoneVisible"
      :contract-id="contract?.id || 0"
      :milestone="currentMilestone"
      @submitted="onMilestoneSubmitted"
    />
  </div>
</template>
