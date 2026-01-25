<script setup lang="ts">
import { computed } from 'vue'
import { FileText, Check, Clock, Eye, Pen, CheckCircle, XCircle } from 'lucide-vue-next'
import type { ContractPayload, ContractStatus } from '../../types/chat/message'
import { CONTRACT_STATUS_MAP } from '../../types/chat/message'

const props = defineProps<{
  payload: ContractPayload
  isSent: boolean
  currentCompanyId?: number
}>()

const emit = defineEmits<{
  (e: 'view', contractId: number): void
  (e: 'sign', contractId: number): void
}>()

const statusInfo = computed(() => CONTRACT_STATUS_MAP[props.payload.status as ContractStatus] ?? CONTRACT_STATUS_MAP[0]!)

// 进度计算
const progressSteps = computed(() => {
  const status = props.payload.status
  if (status === 5) return 0 // 已取消
  if (status >= 4) return 4 // 已完成
  if (status >= 3) return 3 // 履约中
  if (status >= 2) return 2 // 已签署
  if (props.payload.buyerSigned || props.payload.sellerSigned) return 1.5 // 部分签署
  if (status >= 1) return 1 // 待签署
  return 0.5 // 草稿
})

// 是否为当前用户公司
const isBuyer = computed(() => props.currentCompanyId === props.payload.buyerCompanyId)
const isSeller = computed(() => props.currentCompanyId === props.payload.sellerCompanyId)

const formatAmount = (val: number | string) => {
  const num = typeof val === 'string' ? parseFloat(val) : val
  return isNaN(num) ? '-' : num.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

// 是否可以签署（状态为待签署且当前用户还没签）
const canSign = computed(() => {
  if (props.payload.status !== 1) return false
  if (isBuyer.value && !props.payload.buyerSigned) return true
  if (isSeller.value && !props.payload.sellerSigned) return true
  // 如果没有 currentCompanyId，则显示按钮（兼容旧逻辑）
  if (!props.currentCompanyId) {
    return !props.payload.buyerSigned || !props.payload.sellerSigned
  }
  return false
})

// 是否已取消
const isCancelled = computed(() => props.payload.status === 5)

// 是否已完成
const isCompleted = computed(() => props.payload.status === 4)
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
        <span :class="['text-[10px] font-bold px-2.5 py-1 rounded-full flex items-center gap-1', statusInfo.color]">
          <CheckCircle v-if="isCompleted" class="w-3 h-3" />
          <XCircle v-else-if="isCancelled" class="w-3 h-3" />
          {{ statusInfo.label }}
        </span>
      </div>
    </div>

    <!-- 进度条 -->
    <div v-if="!isCancelled" class="px-4 py-2 bg-gray-50/50">
      <div class="flex gap-1">
        <div
          v-for="i in 4"
          :key="i"
          :class="[
            'flex-1 h-1 rounded-full transition-all',
            i <= progressSteps ? 'bg-brand-500' : 'bg-gray-200'
          ]"
        ></div>
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

