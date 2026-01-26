<script setup lang="ts">
/**
 * ContractPreview - 合同实时预览组件
 * 基于 Stitch 设计，专业的合同纸张样式
 *
 * 流程说明：
 * 1. 双方在此预览合同条款
 * 2. 各自点击"确认条款"表示同意
 * 3. 双方都确认后，生成正式签署合同
 * 4. 跳转第三方签署工具完成电子签章
 */
import { computed } from 'vue'
import { History, Download, PenTool, CheckCircle, Clock, FileSignature, ExternalLink } from 'lucide-vue-next'

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

const props = defineProps<{
  contractData: ContractData
  status: ContractStatus
  /** 买方是否已确认条款 */
  buyerConfirmed?: boolean
  /** 卖方是否已确认条款 */
  sellerConfirmed?: boolean
  currentIsBuyer?: boolean
}>()

const emit = defineEmits<{
  /** 确认条款 */
  (e: 'confirm'): void
  /** 生成正式合同并跳转签署 */
  (e: 'generate-formal-contract'): void
  (e: 'view-history'): void
  (e: 'export-pdf'): void
}>()

// 状态配置
const statusConfig = computed(() => {
  const configs: Record<ContractStatus, { label: string; color: string; bgColor: string }> = {
    DRAFT: { label: '草稿', color: 'text-orange-600', bgColor: 'bg-orange-100' },
    PENDING_CONFIRM: { label: '待确认', color: 'text-amber-600', bgColor: 'bg-amber-100' },
    CONFIRMED: { label: '条款已确认', color: 'text-brand-600', bgColor: 'bg-brand-100' },
    SIGNING: { label: '签署中', color: 'text-blue-600', bgColor: 'bg-blue-100' },
    COMPLETED: { label: '已完成', color: 'text-brand-700', bgColor: 'bg-brand-200' }
  }
  return configs[props.status]
})

// 是否显示水印
const showWatermark = computed(() => props.status === 'DRAFT')

// 双方是否都已确认
const bothConfirmed = computed(() => props.buyerConfirmed && props.sellerConfirmed)

// 是否可以确认条款
const canConfirm = computed(() => {
  if (props.status !== 'PENDING_CONFIRM' && props.status !== 'DRAFT') return false
  if (props.currentIsBuyer) return !props.buyerConfirmed
  return !props.sellerConfirmed
})

// 格式化金额
function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount)
}

// 格式化日期
function formatDate(dateStr: string): string {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
  } catch {
    return dateStr
  }
}
</script>

<template>
  <div class="flex flex-col h-full">
    <!-- 工具栏 -->
    <div class="flex items-center justify-between mb-4 shrink-0">
      <div class="flex items-center gap-2">
        <!-- 状态标签 -->
        <div :class="[
          'h-8 px-3 rounded-lg flex items-center gap-2 text-xs font-bold',
          statusConfig.bgColor, statusConfig.color
        ]">
          <span v-if="status === 'DRAFT'" class="w-2 h-2 rounded-full bg-orange-500 animate-pulse" />
          <Clock v-else-if="status === 'PENDING_CONFIRM'" class="w-3.5 h-3.5" />
          <FileSignature v-else-if="status === 'SIGNING'" class="w-3.5 h-3.5" />
          <CheckCircle v-else class="w-3.5 h-3.5" />
          {{ statusConfig.label }}
        </div>
        <span class="text-xs text-gray-400">
          合同编号: {{ contractData.contractNo }}
        </span>
      </div>
      <div class="flex gap-2">
        <button
          @click="emit('view-history')"
          class="h-8 px-3 rounded-lg bg-white border border-gray-200 hover:bg-gray-50
                 text-xs font-medium text-gray-600 flex items-center gap-1 transition-colors"
        >
          <History class="w-4 h-4" />
          历史版本
        </button>
        <button
          @click="emit('export-pdf')"
          class="h-8 px-3 rounded-lg bg-white border border-gray-200 hover:bg-gray-50
                 text-xs font-medium text-gray-600 flex items-center gap-1 transition-colors"
        >
          <Download class="w-4 h-4" />
          导出 PDF
        </button>
      </div>
    </div>

    <!-- 合同文档 -->
    <div class="flex-1 overflow-y-auto rounded-xl bg-white shadow-xl border border-gray-200 relative">
      <!-- DRAFT 水印 -->
      <div
        v-if="showWatermark"
        class="absolute inset-0 flex items-center justify-center pointer-events-none overflow-hidden"
      >
        <div class="text-[100px] font-bold text-gray-200 rotate-[-30deg] select-none uppercase tracking-widest">
          DRAFT
        </div>
      </div>

      <!-- 合同内容 -->
      <div class="relative p-8 md:p-10 lg:px-14">
        <!-- 合同头部 -->
        <div class="text-center mb-10 border-b-2 border-gray-800 pb-6">
          <h1 class="text-2xl md:text-3xl font-bold tracking-wide mb-2 font-serif">
            粮食购销合同
          </h1>
          <p class="text-sm text-gray-500">Grain Purchase and Sale Contract</p>
          <div class="mt-4 flex justify-between text-sm">
            <span>合同编号: <span class="font-mono font-medium">{{ contractData.contractNo }}</span></span>
            <span>签订日期: {{ formatDate(contractData.signDate) }}</span>
          </div>
        </div>

        <!-- 买卖双方信息 -->
        <div class="mb-8 grid grid-cols-1 md:grid-cols-2 gap-8">
          <!-- 买方 -->
          <div>
            <h3 class="font-bold text-sm bg-brand-50 p-2 mb-3 px-3 border-l-4 border-brand-600">
              买方 (The Buyer)
            </h3>
            <div class="text-sm space-y-1.5 pl-3">
              <p>
                <span class="text-gray-500 w-16 inline-block">公司名称:</span>
                <strong>{{ contractData.buyer.companyName }}</strong>
              </p>
              <p v-if="contractData.buyer.contactName">
                <span class="text-gray-500 w-16 inline-block">联系人:</span>
                {{ contractData.buyer.contactName }}
                <span v-if="contractData.buyer.contactTitle" class="text-gray-400">
                  ({{ contractData.buyer.contactTitle }})
                </span>
              </p>
              <p v-if="contractData.buyer.address">
                <span class="text-gray-500 w-16 inline-block">地址:</span>
                {{ contractData.buyer.address }}
              </p>
              <p v-if="contractData.buyer.phone">
                <span class="text-gray-500 w-16 inline-block">联系方式:</span>
                {{ contractData.buyer.phone }}
              </p>
            </div>
          </div>

          <!-- 卖方 -->
          <div>
            <h3 class="font-bold text-sm bg-gray-100 p-2 mb-3 px-3 border-l-4 border-gray-400">
              卖方 (The Seller)
            </h3>
            <div class="text-sm space-y-1.5 pl-3">
              <p>
                <span class="text-gray-500 w-16 inline-block">公司名称:</span>
                <strong>{{ contractData.seller.companyName }}</strong>
              </p>
              <p v-if="contractData.seller.contactName">
                <span class="text-gray-500 w-16 inline-block">联系人:</span>
                {{ contractData.seller.contactName }}
                <span v-if="contractData.seller.contactTitle" class="text-gray-400">
                  ({{ contractData.seller.contactTitle }})
                </span>
              </p>
              <p v-if="contractData.seller.address">
                <span class="text-gray-500 w-16 inline-block">地址:</span>
                {{ contractData.seller.address }}
              </p>
              <p v-if="contractData.seller.phone">
                <span class="text-gray-500 w-16 inline-block">联系方式:</span>
                {{ contractData.seller.phone }}
              </p>
            </div>
          </div>
        </div>

        <!-- 产品明细 -->
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
                <tr v-for="(product, idx) in contractData.products" :key="idx">
                  <td class="px-4 py-3 font-medium">{{ product.name }}</td>
                  <td class="px-4 py-3 text-gray-600">{{ product.grade }}</td>
                  <td class="px-4 py-3 text-right font-mono bg-action-50/30">
                    {{ formatCurrency(product.quantity) }} {{ product.unit }}
                  </td>
                  <td class="px-4 py-3 text-right font-mono bg-action-50/30">
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
                    ¥ {{ formatCurrency(contractData.totalAmount) }}
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>

        <!-- 质量标准与条款 -->
        <div class="mb-8 grid grid-cols-1 md:grid-cols-2 gap-8">
          <!-- 质量标准 -->
          <div>
            <h3 class="font-bold text-sm bg-brand-50 p-2 mb-3 px-3 border-l-4 border-brand-600">
              二、质量标准 (Quality Standards)
            </h3>
            <ul class="list-disc list-inside text-sm space-y-2 text-gray-700 bg-accent-50/30 p-3 rounded-lg border border-accent-100">
              <li v-for="(std, idx) in contractData.qualityStandards" :key="idx">
                <span class="font-medium">{{ std.label }}:</span> {{ std.value }}
              </li>
            </ul>
          </div>

          <!-- 付款与交货 -->
          <div>
            <h3 class="font-bold text-sm bg-brand-50 p-2 mb-3 px-3 border-l-4 border-brand-600">
              三、付款与交货 (Payment & Delivery)
            </h3>
            <div class="text-sm space-y-2 text-gray-700">
              <p><span class="font-bold">付款方式:</span> {{ contractData.paymentMethod }}</p>
              <p><span class="font-bold">交货地点:</span> {{ contractData.deliveryPlace }}</p>
              <p><span class="font-bold">交货期限:</span> {{ formatDate(contractData.deliveryDate) }}前完成交付</p>
            </div>
          </div>
        </div>

        <!-- 备注 -->
        <div v-if="contractData.remark" class="mb-8">
          <h3 class="font-bold text-sm bg-gray-100 p-2 mb-3 px-3 border-l-4 border-gray-400">
            四、备注 (Remarks)
          </h3>
          <p class="text-sm text-gray-600 pl-3">{{ contractData.remark }}</p>
        </div>

        <!-- 条款确认区域 -->
        <div class="mt-12 pt-8 border-t border-gray-300">
          <h3 class="font-bold text-sm bg-gray-100 p-2 mb-4 px-3 border-l-4 border-gray-400">
            五、条款确认 (Terms Confirmation)
          </h3>
          <p class="text-xs text-gray-500 mb-4 pl-3">
            双方确认上述条款后，可生成正式签署合同。正式合同将通过第三方电子签章平台进行签署。
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
                <div class="absolute bottom-2 left-4 text-[10px] text-gray-400">
                  确认时间: {{ new Date().toLocaleString('zh-CN') }}
                </div>
              </template>
              <template v-else>
                <PenTool class="w-8 h-8 text-gray-300 group-hover:text-brand-500 mb-2 transition-colors" />
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
                <div class="absolute bottom-2 left-4 text-[10px] text-gray-400">
                  确认时间: {{ new Date().toLocaleString('zh-CN') }}
                </div>
              </template>
              <template v-else>
                <PenTool class="w-8 h-8 text-gray-300 group-hover:text-brand-500 mb-2 transition-colors" />
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
                    可以生成正式签署合同，并通过第三方电子签章平台完成签署
                  </p>
                </div>
                <button
                  @click="emit('generate-formal-contract')"
                  class="h-10 px-5 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-lg
                         flex items-center gap-2 transition-all active:scale-95 shadow-md"
                >
                  <FileSignature class="w-4 h-4" />
                  生成正式合同
                  <ExternalLink class="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          </div>

          <!-- 签署中状态 -->
          <div v-if="status === 'SIGNING'" class="mt-6">
            <div class="bg-blue-50 border border-blue-200 rounded-xl p-4">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center">
                  <FileSignature class="w-5 h-5 text-blue-600 animate-pulse" />
                </div>
                <div>
                  <h4 class="font-bold text-blue-700">正式合同签署中</h4>
                  <p class="text-xs text-blue-600 mt-0.5">
                    合同已提交至第三方电子签章平台，请前往完成签署
                  </p>
                </div>
                <button
                  class="ml-auto h-9 px-4 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg
                         flex items-center gap-1.5 transition-all text-sm"
                >
                  前往签署
                  <ExternalLink class="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          </div>

          <!-- 已完成状态 -->
          <div v-if="status === 'COMPLETED'" class="mt-6">
            <div class="bg-brand-50 border border-brand-200 rounded-xl p-4">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-brand-100 flex items-center justify-center">
                  <CheckCircle class="w-5 h-5 text-brand-600" />
                </div>
                <div>
                  <h4 class="font-bold text-brand-700">合同已签署完成</h4>
                  <p class="text-xs text-brand-600 mt-0.5">
                    双方已完成电子签章，合同正式生效
                  </p>
                </div>
                <!-- 公章效果 -->
                <div class="ml-auto flex gap-4">
                  <div class="w-14 h-14 rounded-full border-2 border-red-600 flex items-center justify-center opacity-60 rotate-[-10deg]">
                    <span class="text-red-600 font-bold text-[7px] text-center leading-tight">
                      {{ contractData.buyer.companyName.slice(0, 4) }}<br/>合同章
                    </span>
                  </div>
                  <div class="w-14 h-14 rounded-full border-2 border-red-600 flex items-center justify-center opacity-60 rotate-[8deg]">
                    <span class="text-red-600 font-bold text-[7px] text-center leading-tight">
                      {{ contractData.seller.companyName.slice(0, 4) }}<br/>合同章
                    </span>
                  </div>
                </div>
              </div>
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
