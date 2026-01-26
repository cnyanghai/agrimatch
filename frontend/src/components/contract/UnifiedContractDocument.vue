<script setup lang="ts">
/**
 * UnifiedContractDocument - 统一合同文档展示组件
 *
 * 专业的合同纸张样式，用于：
 * 1. 聊天议价中的合同预览
 * 2. 合同管理中的合同详情
 *
 * 特性：
 * - 双语标题
 * - 买卖双方信息并排展示
 * - 产品明细表格
 * - 结构化条款区块
 * - 法律约束条款（正式合同）
 * - DRAFT 水印（草稿状态）
 * - 电子签章效果
 */
import { computed } from 'vue'
import {
  CheckCircle, Clock, FileSignature, AlertTriangle,
  Building2, User, Phone, MapPin, Landmark
} from 'lucide-vue-next'
import {
  CONTRACT_TERM_SECTIONS,
  LEGAL_NOTICE,
  type ContractTermSection
} from '../../constants/contractTerms'

// ==================== 类型定义 ====================

export interface ContractParty {
  companyName: string
  licenseNo?: string
  contactName?: string
  contactTitle?: string
  address?: string
  phone?: string
  bankInfo?: {
    bankName?: string
    accountName?: string
    accountNo?: string
  }
}

export interface ContractProduct {
  name: string
  categoryName?: string
  grade?: string
  quantity: number
  unit: string
  unitPrice: number
  totalPrice: number
  /** 产品参数 */
  params?: Array<{ label: string; value: string }>
}

export interface QualityStandard {
  label: string
  value: string
}

export interface ContractDocumentData {
  /** 合同编号 */
  contractNo: string
  /** 签订日期 */
  signDate?: string
  /** 创建时间 */
  createTime?: string
  /** 买方信息 */
  buyer: ContractParty
  /** 卖方信息 */
  seller: ContractParty
  /** 产品列表 */
  products: ContractProduct[]
  /** 质量标准 */
  qualityStandards?: QualityStandard[]
  /** 付款方式 */
  paymentMethod?: string
  /** 交货地点 */
  deliveryAddress?: string
  /** 交货日期 */
  deliveryDate?: string
  /** 交货方式 */
  deliveryMode?: string
  /** 合同总金额 */
  totalAmount: number
  /** 备注 */
  remark?: string
  /** 合同签订地（用于争议解决条款） */
  signingPlace?: string
}

/** 合同状态 */
export type ContractStatus = 'DRAFT' | 'PENDING_CONFIRM' | 'CONFIRMED' | 'PENDING_SIGN' | 'SIGNED' | 'EXECUTING' | 'COMPLETED' | 'CANCELLED'

// ==================== Props ====================

const props = withDefaults(defineProps<{
  /** 合同数据 */
  data: ContractDocumentData
  /** 合同状态 */
  status: ContractStatus
  /** 买方是否已确认条款 */
  buyerConfirmed?: boolean
  /** 卖方是否已确认条款 */
  sellerConfirmed?: boolean
  /** 买方是否已签署 */
  buyerSigned?: boolean
  /** 卖方是否已签署 */
  sellerSigned?: boolean
  /** 买方签署时间 */
  buyerSignTime?: string
  /** 卖方签署时间 */
  sellerSignTime?: string
  /** 当前用户是否为买方 */
  currentIsBuyer?: boolean
  /** 是否显示法律约束条款（正式合同） */
  showLegalTerms?: boolean
  /** 是否显示条款确认区域 */
  showConfirmArea?: boolean
  /** 是否显示签署区域 */
  showSignArea?: boolean
  /** 是否为打印/PDF模式（隐藏交互元素） */
  printMode?: boolean
}>(), {
  buyerConfirmed: false,
  sellerConfirmed: false,
  buyerSigned: false,
  sellerSigned: false,
  currentIsBuyer: true,
  showLegalTerms: false,
  showConfirmArea: false,
  showSignArea: true,
  printMode: false
})

const emit = defineEmits<{
  /** 确认条款 */
  (e: 'confirm'): void
  /** 生成正式合同 */
  (e: 'generate-formal'): void
}>()

// ==================== 计算属性 ====================

// 状态配置
const statusConfig = computed(() => {
  const configs: Record<ContractStatus, { label: string; color: string; bgColor: string }> = {
    DRAFT: { label: '草稿', color: 'text-orange-600', bgColor: 'bg-orange-100' },
    PENDING_CONFIRM: { label: '待确认', color: 'text-amber-600', bgColor: 'bg-amber-100' },
    CONFIRMED: { label: '条款已确认', color: 'text-brand-600', bgColor: 'bg-brand-100' },
    PENDING_SIGN: { label: '待签署', color: 'text-amber-600', bgColor: 'bg-amber-100' },
    SIGNED: { label: '已签署', color: 'text-brand-600', bgColor: 'bg-brand-100' },
    EXECUTING: { label: '履约中', color: 'text-autumn-600', bgColor: 'bg-autumn-100' },
    COMPLETED: { label: '已完成', color: 'text-brand-700', bgColor: 'bg-brand-200' },
    CANCELLED: { label: '已取消', color: 'text-red-600', bgColor: 'bg-red-100' }
  }
  return configs[props.status] || configs.DRAFT
})

// 是否显示水印
const showWatermark = computed(() => props.status === 'DRAFT' || props.status === 'PENDING_CONFIRM')

// 双方是否都已确认
const bothConfirmed = computed(() => props.buyerConfirmed && props.sellerConfirmed)

// 双方是否都已签署
const bothSigned = computed(() => props.buyerSigned && props.sellerSigned)

// 是否可以确认条款
const canConfirm = computed(() => {
  if (props.status !== 'PENDING_CONFIRM' && props.status !== 'DRAFT') return false
  if (props.currentIsBuyer) return !props.buyerConfirmed
  return !props.sellerConfirmed
})

// 法律约束条款
const legalTerms = computed((): ContractTermSection[] => {
  if (!props.showLegalTerms) return []
  return CONTRACT_TERM_SECTIONS.map(section => {
    if (section.number === '七' && props.data.signingPlace) {
      return {
        ...section,
        content: section.content + `\n\n7.3 合同签订地：${props.data.signingPlace}`
      }
    }
    return section
  })
})

// ==================== 方法 ====================

function formatCurrency(amount: number | undefined): string {
  if (amount == null) return '-'
  return new Intl.NumberFormat('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount)
}

function formatDate(dateStr: string | undefined): string {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
  } catch {
    return dateStr
  }
}

function formatDateTime(dateStr: string | undefined): string {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    return d.toLocaleString('zh-CN')
  } catch {
    return dateStr
  }
}
</script>

<template>
  <div class="unified-contract-document">
    <!-- 合同文档 -->
    <div class="bg-white rounded-xl shadow-xl border border-gray-200 relative overflow-hidden">
      <!-- DRAFT 水印 -->
      <div
        v-if="showWatermark && !printMode"
        class="absolute inset-0 flex items-center justify-center pointer-events-none overflow-hidden z-10"
      >
        <div class="text-[100px] font-bold text-gray-200/60 rotate-[-30deg] select-none uppercase tracking-widest">
          DRAFT
        </div>
      </div>

      <!-- 合同内容 -->
      <div class="relative p-8 md:p-10 lg:px-14">
        <!-- ========== 合同头部 ========== -->
        <div class="text-center mb-10 border-b-2 border-gray-800 pb-6">
          <h1 class="text-2xl md:text-3xl font-bold tracking-wide mb-2 font-serif">
            粮食购销合同
          </h1>
          <p class="text-sm text-gray-500">Grain Purchase and Sale Contract</p>
          <div class="mt-4 flex justify-between text-sm">
            <span>合同编号: <span class="font-mono font-medium">{{ data.contractNo }}</span></span>
            <span>签订日期: {{ formatDate(data.signDate || data.createTime) }}</span>
          </div>
          <!-- 状态标签 -->
          <div v-if="!printMode" class="mt-3 flex justify-center">
            <span :class="[
              'inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold',
              statusConfig.bgColor, statusConfig.color
            ]">
              <Clock v-if="status === 'PENDING_CONFIRM' || status === 'PENDING_SIGN'" class="w-3.5 h-3.5" />
              <FileSignature v-else-if="status === 'SIGNED' || status === 'EXECUTING'" class="w-3.5 h-3.5" />
              <CheckCircle v-else-if="status === 'COMPLETED'" class="w-3.5 h-3.5" />
              {{ statusConfig.label }}
            </span>
          </div>
        </div>

        <!-- ========== 买卖双方信息 ========== -->
        <div class="mb-8 grid grid-cols-1 md:grid-cols-2 gap-8">
          <!-- 买方（甲方） -->
          <div>
            <h3 class="font-bold text-sm bg-autumn-50 p-2 mb-3 px-3 border-l-4 border-autumn-500 flex items-center gap-2">
              <Building2 class="w-4 h-4" />
              甲方（买方） The Buyer
            </h3>
            <div class="text-sm space-y-2 pl-3">
              <p class="flex items-start gap-2">
                <span class="text-gray-500 w-20 shrink-0">公司名称:</span>
                <strong class="text-gray-900">{{ data.buyer.companyName }}</strong>
              </p>
              <p v-if="data.buyer.licenseNo" class="flex items-start gap-2">
                <span class="text-gray-500 w-20 shrink-0">统一信用码:</span>
                <span class="font-mono text-gray-700">{{ data.buyer.licenseNo }}</span>
              </p>
              <p v-if="data.buyer.contactName" class="flex items-start gap-2">
                <User class="w-4 h-4 text-gray-400 mt-0.5" />
                <span>{{ data.buyer.contactName }}
                  <span v-if="data.buyer.contactTitle" class="text-gray-400">({{ data.buyer.contactTitle }})</span>
                </span>
              </p>
              <p v-if="data.buyer.phone" class="flex items-start gap-2">
                <Phone class="w-4 h-4 text-gray-400 mt-0.5" />
                <span>{{ data.buyer.phone }}</span>
              </p>
              <p v-if="data.buyer.address" class="flex items-start gap-2">
                <MapPin class="w-4 h-4 text-gray-400 mt-0.5" />
                <span class="text-gray-600">{{ data.buyer.address }}</span>
              </p>
              <!-- 银行信息 -->
              <div v-if="data.buyer.bankInfo" class="mt-3 p-3 bg-gray-50 rounded-lg">
                <div class="flex items-center gap-1.5 text-xs text-gray-500 mb-2">
                  <Landmark class="w-3.5 h-3.5" />
                  银行账户
                </div>
                <div class="text-xs space-y-1 text-gray-700">
                  <p v-if="data.buyer.bankInfo.bankName">开户行: {{ data.buyer.bankInfo.bankName }}</p>
                  <p v-if="data.buyer.bankInfo.accountName">户名: {{ data.buyer.bankInfo.accountName }}</p>
                  <p v-if="data.buyer.bankInfo.accountNo">账号: <span class="font-mono">{{ data.buyer.bankInfo.accountNo }}</span></p>
                </div>
              </div>
            </div>
          </div>

          <!-- 卖方（乙方） -->
          <div>
            <h3 class="font-bold text-sm bg-brand-50 p-2 mb-3 px-3 border-l-4 border-brand-500 flex items-center gap-2">
              <Building2 class="w-4 h-4" />
              乙方（卖方） The Seller
            </h3>
            <div class="text-sm space-y-2 pl-3">
              <p class="flex items-start gap-2">
                <span class="text-gray-500 w-20 shrink-0">公司名称:</span>
                <strong class="text-gray-900">{{ data.seller.companyName }}</strong>
              </p>
              <p v-if="data.seller.licenseNo" class="flex items-start gap-2">
                <span class="text-gray-500 w-20 shrink-0">统一信用码:</span>
                <span class="font-mono text-gray-700">{{ data.seller.licenseNo }}</span>
              </p>
              <p v-if="data.seller.contactName" class="flex items-start gap-2">
                <User class="w-4 h-4 text-gray-400 mt-0.5" />
                <span>{{ data.seller.contactName }}
                  <span v-if="data.seller.contactTitle" class="text-gray-400">({{ data.seller.contactTitle }})</span>
                </span>
              </p>
              <p v-if="data.seller.phone" class="flex items-start gap-2">
                <Phone class="w-4 h-4 text-gray-400 mt-0.5" />
                <span>{{ data.seller.phone }}</span>
              </p>
              <p v-if="data.seller.address" class="flex items-start gap-2">
                <MapPin class="w-4 h-4 text-gray-400 mt-0.5" />
                <span class="text-gray-600">{{ data.seller.address }}</span>
              </p>
              <!-- 银行信息 -->
              <div v-if="data.seller.bankInfo" class="mt-3 p-3 bg-gray-50 rounded-lg">
                <div class="flex items-center gap-1.5 text-xs text-gray-500 mb-2">
                  <Landmark class="w-3.5 h-3.5" />
                  银行账户
                </div>
                <div class="text-xs space-y-1 text-gray-700">
                  <p v-if="data.seller.bankInfo.bankName">开户行: {{ data.seller.bankInfo.bankName }}</p>
                  <p v-if="data.seller.bankInfo.accountName">户名: {{ data.seller.bankInfo.accountName }}</p>
                  <p v-if="data.seller.bankInfo.accountNo">账号: <span class="font-mono">{{ data.seller.bankInfo.accountNo }}</span></p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ========== 一、产品明细与价格 ========== -->
        <div class="mb-8">
          <h3 class="font-bold text-sm bg-brand-50 p-2 mb-4 px-3 border-l-4 border-brand-600">
            一、产品明细与价格 (Product Details & Pricing)
          </h3>
          <div class="overflow-hidden rounded-lg border border-gray-200">
            <table class="w-full text-sm text-left">
              <thead class="bg-gray-50 text-xs uppercase font-bold text-gray-600 border-b border-gray-200">
                <tr>
                  <th class="px-4 py-3">产品名称</th>
                  <th class="px-4 py-3">规格等级</th>
                  <th class="px-4 py-3 text-right">数量</th>
                  <th class="px-4 py-3 text-right">单价 (元)</th>
                  <th class="px-4 py-3 text-right">金额 (元)</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-100">
                <tr v-for="(product, idx) in data.products" :key="idx">
                  <td class="px-4 py-3">
                    <div class="font-medium text-gray-900">{{ product.name }}</div>
                    <div v-if="product.categoryName" class="text-xs text-gray-500">{{ product.categoryName }}</div>
                  </td>
                  <td class="px-4 py-3 text-gray-600">{{ product.grade || '-' }}</td>
                  <td class="px-4 py-3 text-right font-mono bg-gray-50/50">
                    {{ formatCurrency(product.quantity) }} {{ product.unit }}
                  </td>
                  <td class="px-4 py-3 text-right font-mono bg-gray-50/50">
                    {{ formatCurrency(product.unitPrice) }}
                  </td>
                  <td class="px-4 py-3 text-right font-mono font-bold">
                    {{ formatCurrency(product.totalPrice) }}
                  </td>
                </tr>
              </tbody>
              <tfoot class="bg-gray-50 font-bold border-t border-gray-200">
                <tr>
                  <td class="px-4 py-3 text-right" colspan="4">合计 (Total Amount):</td>
                  <td class="px-4 py-3 text-right text-brand-600 text-base font-mono">
                    ¥ {{ formatCurrency(data.totalAmount) }}
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>

          <!-- 产品参数 -->
          <div v-if="data.products[0]?.params && data.products[0].params.length > 0" class="mt-4">
            <div class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">产品参数</div>
            <div class="grid grid-cols-3 gap-3">
              <div
                v-for="param in data.products[0].params"
                :key="param.label"
                class="p-3 bg-gray-50 rounded-lg"
              >
                <div class="text-xs text-gray-500">{{ param.label }}</div>
                <div class="text-sm font-medium text-gray-900">{{ param.value }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- ========== 二、质量标准 ========== -->
        <div v-if="data.qualityStandards && data.qualityStandards.length > 0" class="mb-8">
          <h3 class="font-bold text-sm bg-brand-50 p-2 mb-3 px-3 border-l-4 border-brand-600">
            二、质量标准 (Quality Standards)
          </h3>
          <ul class="list-disc list-inside text-sm space-y-2 text-gray-700 bg-gray-50 p-4 rounded-lg border border-gray-100">
            <li v-for="(std, idx) in data.qualityStandards" :key="idx">
              <span class="font-medium">{{ std.label }}:</span> {{ std.value }}
            </li>
          </ul>
        </div>

        <!-- ========== 三、付款方式 ========== -->
        <div v-if="data.paymentMethod" class="mb-8">
          <h3 class="font-bold text-sm bg-brand-50 p-2 mb-3 px-3 border-l-4 border-brand-600">
            三、付款方式 (Payment Method)
          </h3>
          <p class="text-sm text-gray-700 pl-3">{{ data.paymentMethod }}</p>
        </div>

        <!-- ========== 四、交货与验收 ========== -->
        <div class="mb-8">
          <h3 class="font-bold text-sm bg-brand-50 p-2 mb-3 px-3 border-l-4 border-brand-600">
            四、交货与验收 (Delivery & Acceptance)
          </h3>
          <div class="text-sm space-y-2 pl-3 text-gray-700">
            <p v-if="data.deliveryAddress">
              <span class="font-medium">交货地点:</span> {{ data.deliveryAddress }}
            </p>
            <p v-if="data.deliveryDate">
              <span class="font-medium">交货期限:</span> {{ formatDate(data.deliveryDate) }} 前完成交付
            </p>
            <p v-if="data.deliveryMode">
              <span class="font-medium">交货方式:</span> {{ data.deliveryMode }}
            </p>
          </div>
        </div>

        <!-- ========== 法律约束条款（正式合同显示） ========== -->
        <template v-if="showLegalTerms">
          <div v-for="term in legalTerms" :key="term.number" class="mb-8">
            <h3 class="font-bold text-sm bg-slate-100 p-2 mb-3 px-3 border-l-4 border-slate-500">
              {{ term.number }}、{{ term.titleCn }} ({{ term.titleEn }})
            </h3>
            <div class="text-sm text-gray-700 pl-3 whitespace-pre-line leading-relaxed">
              {{ term.content }}
            </div>
          </div>
        </template>

        <!-- ========== 备注 ========== -->
        <div v-if="data.remark" class="mb-8">
          <h3 class="font-bold text-sm bg-gray-100 p-2 mb-3 px-3 border-l-4 border-gray-400">
            {{ showLegalTerms ? '九' : '五' }}、备注 (Remarks)
          </h3>
          <p class="text-sm text-gray-600 pl-3">{{ data.remark }}</p>
        </div>

        <!-- ========== 法律提示 ========== -->
        <div v-if="showLegalTerms && !printMode" class="mb-8 p-4 bg-amber-50 rounded-xl border border-amber-200">
          <div class="flex items-start gap-3">
            <AlertTriangle class="w-5 h-5 text-amber-500 shrink-0 mt-0.5" />
            <div>
              <div class="text-sm font-bold text-amber-700">法律提示</div>
              <div class="text-xs text-amber-600 mt-1">{{ LEGAL_NOTICE }}</div>
            </div>
          </div>
        </div>

        <!-- ========== 条款确认区域 ========== -->
        <div v-if="showConfirmArea && !printMode" class="mt-12 pt-8 border-t border-gray-300">
          <h3 class="font-bold text-sm bg-gray-100 p-2 mb-4 px-3 border-l-4 border-gray-400">
            条款确认 (Terms Confirmation)
          </h3>
          <p class="text-xs text-gray-500 mb-4 pl-3">
            双方确认上述条款后，可生成正式签署合同。正式合同将包含完整的法律约束条款。
          </p>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
            <!-- 买方确认 -->
            <div
              :class="[
                'relative p-6 rounded-lg min-h-[120px] flex flex-col items-center justify-center transition-all',
                buyerConfirmed
                  ? 'border border-brand-200 bg-brand-50/50'
                  : 'border-2 border-dashed border-gray-300 bg-gray-50 cursor-pointer hover:border-brand-500 hover:bg-brand-50/30 group'
              ]"
              @click="currentIsBuyer && canConfirm && emit('confirm')"
            >
              <template v-if="buyerConfirmed">
                <CheckCircle class="w-8 h-8 text-brand-500 mb-2" />
                <span class="text-sm font-medium text-brand-600">买方已确认条款</span>
              </template>
              <template v-else>
                <FileSignature class="w-8 h-8 text-gray-300 group-hover:text-brand-500 mb-2 transition-colors" />
                <span class="text-sm font-medium text-gray-500 group-hover:text-brand-600 transition-colors">
                  {{ currentIsBuyer ? '点击确认条款 (买方)' : '等待买方确认' }}
                </span>
              </template>
            </div>

            <!-- 卖方确认 -->
            <div
              :class="[
                'relative p-6 rounded-lg min-h-[120px] flex flex-col items-center justify-center transition-all',
                sellerConfirmed
                  ? 'border border-brand-200 bg-brand-50/50'
                  : 'border-2 border-dashed border-gray-300 bg-gray-50 cursor-pointer hover:border-brand-500 hover:bg-brand-50/30 group'
              ]"
              @click="!currentIsBuyer && canConfirm && emit('confirm')"
            >
              <template v-if="sellerConfirmed">
                <CheckCircle class="w-8 h-8 text-brand-500 mb-2" />
                <span class="text-sm font-medium text-brand-600">卖方已确认条款</span>
              </template>
              <template v-else>
                <FileSignature class="w-8 h-8 text-gray-300 group-hover:text-brand-500 mb-2 transition-colors" />
                <span class="text-sm font-medium text-gray-500 group-hover:text-brand-600 transition-colors">
                  {{ !currentIsBuyer ? '点击确认条款 (卖方)' : '等待卖方确认' }}
                </span>
              </template>
            </div>
          </div>

          <!-- 双方确认后：生成正式合同按钮 -->
          <div v-if="bothConfirmed" class="mt-6">
            <div class="bg-brand-50 border border-brand-200 rounded-xl p-4">
              <div class="flex items-center justify-between">
                <div>
                  <h4 class="font-bold text-brand-700 flex items-center gap-2">
                    <CheckCircle class="w-5 h-5" />
                    双方已确认条款
                  </h4>
                  <p class="text-xs text-brand-600 mt-1">
                    可以生成包含完整法律约束条款的正式签署合同
                  </p>
                </div>
                <button
                  @click="emit('generate-formal')"
                  class="h-10 px-5 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-lg
                         flex items-center gap-2 transition-all active:scale-95 shadow-md"
                >
                  <FileSignature class="w-4 h-4" />
                  生成正式合同
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- ========== 签署区域 ========== -->
        <div v-if="showSignArea" class="mt-12 pt-8 border-t-2 border-gray-300">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
            <!-- 买方签署 -->
            <div class="text-center">
              <h4 class="font-bold text-sm text-gray-700 mb-4">甲方（买方）签章</h4>
              <div
                :class="[
                  'h-32 rounded-lg flex flex-col items-center justify-center',
                  buyerSigned ? 'bg-brand-50/50' : 'bg-gray-50 border-2 border-dashed border-gray-200'
                ]"
              >
                <template v-if="buyerSigned">
                  <!-- 电子签章效果 -->
                  <div class="w-20 h-20 rounded-full border-2 border-red-600 flex items-center justify-center opacity-70 rotate-[-8deg]">
                    <span class="text-red-600 font-bold text-[8px] text-center leading-tight">
                      {{ data.buyer.companyName.slice(0, 4) }}<br/>合同章
                    </span>
                  </div>
                  <div class="text-xs text-gray-500 mt-2">
                    签署时间: {{ formatDateTime(buyerSignTime) }}
                  </div>
                </template>
                <template v-else>
                  <Clock class="w-8 h-8 text-gray-300 mb-2" />
                  <span class="text-sm text-gray-400">待签署</span>
                </template>
              </div>
              <p class="text-xs text-gray-500 mt-2">{{ data.buyer.companyName }}</p>
            </div>

            <!-- 卖方签署 -->
            <div class="text-center">
              <h4 class="font-bold text-sm text-gray-700 mb-4">乙方（卖方）签章</h4>
              <div
                :class="[
                  'h-32 rounded-lg flex flex-col items-center justify-center',
                  sellerSigned ? 'bg-brand-50/50' : 'bg-gray-50 border-2 border-dashed border-gray-200'
                ]"
              >
                <template v-if="sellerSigned">
                  <!-- 电子签章效果 -->
                  <div class="w-20 h-20 rounded-full border-2 border-red-600 flex items-center justify-center opacity-70 rotate-[6deg]">
                    <span class="text-red-600 font-bold text-[8px] text-center leading-tight">
                      {{ data.seller.companyName.slice(0, 4) }}<br/>合同章
                    </span>
                  </div>
                  <div class="text-xs text-gray-500 mt-2">
                    签署时间: {{ formatDateTime(sellerSignTime) }}
                  </div>
                </template>
                <template v-else>
                  <Clock class="w-8 h-8 text-gray-300 mb-2" />
                  <span class="text-sm text-gray-400">待签署</span>
                </template>
              </div>
              <p class="text-xs text-gray-500 mt-2">{{ data.seller.companyName }}</p>
            </div>
          </div>

          <!-- 双方签署完成提示 -->
          <div v-if="bothSigned && !printMode" class="mt-6">
            <div class="bg-brand-50 border border-brand-200 rounded-xl p-4 text-center">
              <CheckCircle class="w-8 h-8 text-brand-600 mx-auto mb-2" />
              <h4 class="font-bold text-brand-700">合同已签署完成</h4>
              <p class="text-xs text-brand-600 mt-1">双方已完成电子签章，合同正式生效</p>
            </div>
          </div>
        </div>

        <!-- 底部留白 -->
        <div class="h-8"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.font-serif {
  font-family: 'Noto Serif SC', 'Times New Roman', serif;
}
</style>
