<script setup lang="ts">
/**
 * ContractPreview - 合同实时预览组件
 * 基于统一合同文档组件，用于聊天议价中的合同预览
 *
 * 流程说明：
 * 1. 双方在此预览合同条款
 * 2. 各自点击"确认条款"表示同意
 * 3. 双方都确认后，生成正式签署合同（包含法律约束条款）
 * 4. 跳转第三方签署工具完成电子签章
 */
import { computed } from 'vue'
import UnifiedContractDocument, {
  type ContractDocumentData,
  type ContractStatus as DocContractStatus
} from '../contract/UnifiedContractDocument.vue'

// ==================== 类型定义（保持兼容） ====================

export interface ContractParty {
  companyName: string
  contactName?: string
  contactTitle?: string
  address?: string
  phone?: string
}

export interface ContractProduct {
  name: string
  grade: string
  quantity: number
  unit: string
  unitPrice: number
  totalPrice: number
}

export interface QualityStandard {
  label: string
  value: string
}

export interface ContractData {
  contractNo: string
  signDate: string
  buyer: ContractParty
  seller: ContractParty
  products: ContractProduct[]
  qualityStandards: QualityStandard[]
  paymentMethod: string
  deliveryPlace: string
  deliveryDate: string
  totalAmount: number
  remark?: string
}

/** 合同状态：草稿 -> 待确认 -> 已确认 -> 签署中 -> 已完成 */
export type ContractStatus = 'DRAFT' | 'PENDING_CONFIRM' | 'CONFIRMED' | 'SIGNING' | 'COMPLETED'

// ==================== Props ====================

const props = withDefaults(defineProps<{
  contractData: ContractData
  status: ContractStatus
  /** 买方是否已确认条款 */
  buyerConfirmed?: boolean
  /** 卖方是否已确认条款 */
  sellerConfirmed?: boolean
  currentIsBuyer?: boolean
  /** 是否显示法律约束条款（已确认后显示） */
  showLegalTerms?: boolean
}>(), {
  buyerConfirmed: false,
  sellerConfirmed: false,
  currentIsBuyer: true,
  showLegalTerms: false
})

const emit = defineEmits<{
  /** 确认条款 */
  (e: 'confirm'): void
  /** 生成正式合同并跳转签署 */
  (e: 'generate-formal-contract'): void
}>()

// ==================== 计算属性 ====================

// 转换为统一组件的状态
const documentStatus = computed((): DocContractStatus => {
  return props.status as DocContractStatus
})

// 是否显示法律条款（已确认或正在签署或已完成时显示）
const shouldShowLegalTerms = computed(() => {
  if (props.showLegalTerms) return true
  return props.status === 'CONFIRMED' || props.status === 'SIGNING' || props.status === 'COMPLETED'
})

// 是否显示条款确认区域
const showConfirmArea = computed(() => {
  return props.status === 'DRAFT' || props.status === 'PENDING_CONFIRM' || props.status === 'CONFIRMED'
})

// 是否已签署（用于签署区域显示）
const isSigned = computed(() => {
  return props.status === 'SIGNING' || props.status === 'COMPLETED'
})

// 转换合同数据为统一组件格式
const documentData = computed((): ContractDocumentData => {
  const c = props.contractData
  return {
    contractNo: c.contractNo,
    signDate: c.signDate,
    buyer: {
      companyName: c.buyer.companyName,
      contactName: c.buyer.contactName,
      contactTitle: c.buyer.contactTitle,
      address: c.buyer.address,
      phone: c.buyer.phone
    },
    seller: {
      companyName: c.seller.companyName,
      contactName: c.seller.contactName,
      contactTitle: c.seller.contactTitle,
      address: c.seller.address,
      phone: c.seller.phone
    },
    products: c.products.map(p => ({
      name: p.name,
      grade: p.grade,
      quantity: p.quantity,
      unit: p.unit,
      unitPrice: p.unitPrice,
      totalPrice: p.totalPrice
    })),
    qualityStandards: c.qualityStandards,
    paymentMethod: c.paymentMethod,
    deliveryAddress: c.deliveryPlace,
    deliveryDate: c.deliveryDate,
    totalAmount: c.totalAmount,
    remark: c.remark,
    signingPlace: c.buyer.address || c.seller.address
  }
})

// ==================== 事件处理 ====================

function handleConfirm() {
  emit('confirm')
}

function handleGenerateFormal() {
  emit('generate-formal-contract')
}
</script>

<template>
  <div class="flex flex-col h-full">
    <!-- 统一合同文档 -->
    <div class="flex-1 overflow-y-auto">
      <UnifiedContractDocument
        :data="documentData"
        :status="documentStatus"
        :buyer-confirmed="buyerConfirmed"
        :seller-confirmed="sellerConfirmed"
        :buyer-signed="isSigned"
        :seller-signed="isSigned"
        :current-is-buyer="currentIsBuyer"
        :show-legal-terms="shouldShowLegalTerms"
        :show-confirm-area="showConfirmArea"
        :show-sign-area="isSigned"
        @confirm="handleConfirm"
        @generate-formal="handleGenerateFormal"
      />
    </div>
  </div>
</template>
