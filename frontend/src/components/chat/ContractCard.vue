<script setup lang="ts">
import { computed } from 'vue'
import { FileText, Check, Clock, Eye, Pen } from 'lucide-vue-next'

interface ContractPayload {
  contractId: number
  contractNo: string
  productName: string
  quantity: number | string
  unit: string
  unitPrice: number | string
  basisPrice?: number | string
  contractCode?: string
  totalAmount: number | string
  buyerCompanyId: number
  buyerCompanyName: string
  sellerCompanyId: number
  sellerCompanyName: string
  status: number
  buyerSigned: boolean
  sellerSigned: boolean
}

const props = defineProps<{
  payload: ContractPayload
  isSent: boolean
}>()

const emit = defineEmits<{
  (e: 'view', contractId: number): void
  (e: 'sign', contractId: number): void
}>()

// 状态映射
const statusMap: Record<number, { label: string; color: string }> = {
  0: { label: '草稿', color: 'text-gray-500 bg-gray-100' },
  1: { label: '待签署', color: 'text-amber-600 bg-amber-50' },
  2: { label: '已签署', color: 'text-brand-600 bg-brand-50' },
  3: { label: '履约中', color: 'text-blue-600 bg-blue-50' },
  4: { label: '已完成', color: 'text-brand-700 bg-brand-100' },
  5: { label: '已取消', color: 'text-red-500 bg-red-50' }
}

const statusInfo = computed(() => statusMap[props.payload.status] || statusMap[0])

const formatAmount = (val: number | string) => {
  const num = typeof val === 'string' ? parseFloat(val) : val
  return isNaN(num) ? '-' : num.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

// 是否可以签署（状态为待签署且还没签）
const canSign = computed(() => {
  return props.payload.status === 1 && (!props.payload.buyerSigned || !props.payload.sellerSigned)
})
</script>

<template>
  <div class="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden max-w-[320px]">
    <!-- 头部 -->
    <div class="px-4 py-3 bg-gradient-to-r from-slate-50 to-gray-50 border-b border-gray-200">
      <div class="flex items-center gap-2">
        <div class="w-8 h-8 rounded-xl bg-slate-900 flex items-center justify-center">
          <FileText class="w-4 h-4 text-white" />
        </div>
        <div class="flex-1 min-w-0">
          <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">采购合同</div>
          <div class="text-sm font-bold text-gray-900 truncate">{{ payload.contractNo }}</div>
        </div>
        <span :class="['text-[10px] font-bold px-2 py-1 rounded-full', statusInfo.color]">
          {{ statusInfo.label }}
        </span>
      </div>
    </div>
    
    <!-- 内容 -->
    <div class="p-4 space-y-3">
      <!-- 产品信息 -->
      <div class="bg-gray-50 rounded-xl p-3">
        <div class="text-xs text-gray-500 mb-1">交易标的</div>
        <div class="font-bold text-gray-900">{{ payload.productName }}</div>
        <div class="text-sm text-gray-600 mt-1">
          <template v-if="payload.basisPrice !== undefined && payload.basisPrice !== null && payload.basisPrice !== ''">
            {{ payload.quantity }} {{ payload.unit }} · 基差 {{ (Number(payload.basisPrice) > 0 ? '+' : '') + payload.basisPrice }} ({{ payload.contractCode }})
          </template>
          <template v-else>
            {{ payload.quantity }} {{ payload.unit }} × ¥{{ formatAmount(payload.unitPrice) }}
          </template>
        </div>
      </div>
      
      <!-- 金额 -->
      <div class="flex items-center justify-between">
        <span class="text-xs text-gray-500">合同金额</span>
        <span class="text-lg font-bold text-brand-600">
          <template v-if="payload.basisPrice !== undefined && payload.basisPrice !== null && payload.basisPrice !== ''">
            待结算
          </template>
          <template v-else>
            ¥{{ formatAmount(payload.totalAmount) }}
          </template>
        </span>
      </div>
      
      <!-- 签署状态 -->
      <div class="flex gap-2">
        <div class="flex-1 bg-gray-50 rounded-lg p-2 text-center">
          <div class="text-[10px] text-gray-400 mb-1">买方</div>
          <div class="flex items-center justify-center gap-1">
            <Check v-if="payload.buyerSigned" class="w-3.5 h-3.5 text-brand-500" />
            <Clock v-else class="w-3.5 h-3.5 text-amber-500" />
            <span :class="['text-xs font-medium', payload.buyerSigned ? 'text-brand-600' : 'text-amber-600']">
              {{ payload.buyerSigned ? '已签署' : '待签署' }}
            </span>
          </div>
        </div>
        <div class="flex-1 bg-gray-50 rounded-lg p-2 text-center">
          <div class="text-[10px] text-gray-400 mb-1">卖方</div>
          <div class="flex items-center justify-center gap-1">
            <Check v-if="payload.sellerSigned" class="w-3.5 h-3.5 text-brand-500" />
            <Clock v-else class="w-3.5 h-3.5 text-amber-500" />
            <span :class="['text-xs font-medium', payload.sellerSigned ? 'text-brand-600' : 'text-amber-600']">
              {{ payload.sellerSigned ? '已签署' : '待签署' }}
            </span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 操作按钮 -->
    <div class="px-4 py-3 bg-gray-50 border-t border-gray-200 flex gap-2">
      <button
        class="flex-1 flex items-center justify-center gap-1.5 px-3 py-2 bg-white border border-gray-200 rounded-xl text-xs font-bold text-gray-700 hover:bg-gray-50 transition-all "
        @click="emit('view', payload.contractId)"
      >
        <Eye class="w-3.5 h-3.5" />
        查看详情
      </button>
      <button
        v-if="canSign"
        class="flex-1 flex items-center justify-center gap-1.5 px-3 py-2 bg-brand-600 rounded-xl text-xs font-bold text-white hover:bg-brand-700 transition-all "
        @click="emit('sign', payload.contractId)"
      >
        <Pen class="w-3.5 h-3.5" />
        签署合同
      </button>
    </div>
  </div>
</template>

