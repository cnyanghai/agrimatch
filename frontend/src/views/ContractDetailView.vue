<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Download, Pen, RefreshCw, Scale
} from 'lucide-vue-next'
import html2pdf from 'html2pdf.js'
import {
  getContract, sendContractForSigning,
  getPaymentMethodText, getDeliveryModeText, parseBankInfo,
  type ContractResponse,
  type MilestoneResponse
} from '../api/contract'
import ContractSignModal from '../components/contract/ContractSignModal.vue'
import MilestoneList from '../components/contract/MilestoneList.vue'
import MilestoneCreateModal from '../components/contract/MilestoneCreateModal.vue'
import MilestoneSubmitModal from '../components/contract/MilestoneSubmitModal.vue'
import UnifiedContractDocument, {
  type ContractDocumentData,
  type ContractStatus as DocContractStatus
} from '../components/contract/UnifiedContractDocument.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const contract = ref<ContractResponse | null>(null)
const signModalVisible = ref(false)

// 履约管理
const milestoneListRef = ref<InstanceType<typeof MilestoneList> | null>(null)
// 合同文档引用（用于PDF导出）
const contractDocRef = ref<HTMLElement | null>(null)
const pdfGenerating = ref(false)
const createMilestoneVisible = ref(false)
const submitMilestoneVisible = ref(false)
const currentMilestone = ref<MilestoneResponse | null>(null)

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

// 转换合同状态为文档组件状态
const documentStatus = computed((): DocContractStatus => {
  if (!contract.value) return 'DRAFT'
  const statusMap: Record<number, DocContractStatus> = {
    0: 'DRAFT',
    1: 'PENDING_SIGN',
    2: 'SIGNED',
    3: 'EXECUTING',
    4: 'COMPLETED',
    5: 'CANCELLED'
  }
  return statusMap[contract.value.status] || 'DRAFT'
})

// 是否显示法律条款（已签署及之后显示）
const showLegalTerms = computed(() => {
  if (!contract.value) return false
  return contract.value.status >= 1  // 待签署及之后都显示法律条款
})

// 转换合同数据为文档组件格式
const documentData = computed((): ContractDocumentData | null => {
  if (!contract.value) return null

  const c = contract.value
  const buyerBank = parseBankInfo(c.buyerBankInfo)
  const sellerBank = parseBankInfo(c.sellerBankInfo)

  return {
    contractNo: c.contractNo,
    signDate: c.buyerSignTime || c.sellerSignTime,
    createTime: c.createTime,
    buyer: {
      companyName: c.buyerCompanyName || '-',
      licenseNo: c.buyerLicenseNo,
      contactName: c.buyerContacts,
      phone: c.buyerPhone,
      address: c.buyerAddress,
      bankInfo: buyerBank ? {
        bankName: buyerBank.bankName,
        accountName: buyerBank.accountName,
        accountNo: buyerBank.accountNo
      } : undefined
    },
    seller: {
      companyName: c.sellerCompanyName || '-',
      licenseNo: c.sellerLicenseNo,
      contactName: c.sellerContacts,
      phone: c.sellerPhone,
      address: c.sellerAddress,
      bankInfo: sellerBank ? {
        bankName: sellerBank.bankName,
        accountName: sellerBank.accountName,
        accountNo: sellerBank.accountNo
      } : undefined
    },
    products: [{
      name: c.productName || '-',
      categoryName: c.categoryName,
      grade: '-',
      quantity: c.quantity || 0,
      unit: c.unit || '吨',
      unitPrice: c.unitPrice || 0,
      totalPrice: c.totalAmount || 0,
      params: c.productParams
    }],
    paymentMethod: getPaymentMethodText(c.paymentMethod),
    deliveryAddress: c.deliveryAddress,
    deliveryDate: c.deliveryDate,
    deliveryMode: getDeliveryModeText(c.deliveryMode),
    totalAmount: c.totalAmount || 0,
    signingPlace: c.buyerAddress || c.sellerAddress
  }
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

// 打开添加履约节点弹窗
function openCreateMilestone() {
  createMilestoneVisible.value = true
}

// 履约节点创建完成
function onMilestoneCreated() {
  loadContract()
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

// 下载 PDF（前端生成，与页面显示一致）
async function downloadPdf() {
  if (!contract.value || !contractDocRef.value) return

  pdfGenerating.value = true
  try {
    const element = contractDocRef.value
    const contractNo = contract.value.contractNo || 'contract'

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const opt: any = {
      margin: [10, 10, 10, 10],
      filename: `合同-${contractNo}.pdf`,
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: {
        scale: 2,
        useCORS: true,
        logging: false
      },
      jsPDF: {
        unit: 'mm',
        format: 'a4',
        orientation: 'portrait'
      },
      pagebreak: { mode: 'avoid-all' }
    }

    await html2pdf().set(opt).from(element).save()
    ElMessage.success('PDF 下载成功')
  } catch (e: any) {
    console.error('PDF generation failed:', e)
    ElMessage.error('PDF 生成失败，请重试')
  } finally {
    pdfGenerating.value = false
  }
}

onMounted(() => {
  loadContract()
})
</script>

<template>
  <div class="contract-detail-view">
    <!-- 顶部导航栏 -->
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
          class="flex items-center gap-2 px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-bold rounded-xl transition-all"
          @click="loadContract"
        >
          <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': loading }" />
          刷新
        </button>
        <button
          v-if="canSendForSign"
          class="flex items-center gap-2 px-4 py-2 bg-autumn-600 hover:bg-autumn-700 text-white text-sm font-bold rounded-xl transition-all"
          @click="handleSendForSign"
        >
          发送签署
        </button>
        <button
          v-if="canSign"
          class="flex items-center gap-2 px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white text-sm font-bold rounded-xl transition-all"
          @click="openSignModal"
        >
          <Pen class="w-4 h-4" />
          签署合同
        </button>
        <button
          class="flex items-center gap-2 px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white text-sm font-bold rounded-xl transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="pdfGenerating"
          @click="downloadPdf"
        >
          <Download class="w-4 h-4" :class="{ 'animate-pulse': pdfGenerating }" />
          {{ pdfGenerating ? '生成中...' : '下载 PDF' }}
        </button>
      </div>
    </div>

    <!-- 主内容 -->
    <div class="space-y-6">
      <!-- 加载状态 -->
      <div v-if="loading" class="flex items-center justify-center py-20">
        <div class="text-gray-400">加载中...</div>
      </div>

      <div v-else-if="contract && documentData" class="max-w-5xl mx-auto space-y-6">
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

        <!-- 统一合同文档 -->
        <div ref="contractDocRef">
          <UnifiedContractDocument
            :data="documentData"
            :status="documentStatus"
            :buyer-signed="contract.buyerSigned"
            :seller-signed="contract.sellerSigned"
            :buyer-sign-time="contract.buyerSignTime"
            :seller-sign-time="contract.sellerSignTime"
            :show-legal-terms="showLegalTerms"
            :show-confirm-area="false"
            :show-sign-area="true"
            :print-mode="pdfGenerating"
          />
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
