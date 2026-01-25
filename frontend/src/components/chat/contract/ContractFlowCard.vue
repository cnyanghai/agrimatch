<script setup lang="ts">
/**
 * Contract Flow Card Component
 * Displays contract progress with visual step indicator
 */
import { computed } from 'vue'
import { FileText, Check, Clock, Pen, Truck, CheckCircle } from 'lucide-vue-next'
import type { ContractPayload, ContractStatus } from '../../../types/chat/message'
import { CONTRACT_STATUS_MAP } from '../../../types/chat/message'

const props = defineProps<{
  contract: ContractPayload
  currentCompanyId?: number
}>()

const emit = defineEmits<{
  (e: 'view'): void
  (e: 'sign'): void
}>()

// 流程步骤定义
const steps = [
  { key: 'draft', label: '草稿', icon: FileText },
  { key: 'pending', label: '待签', icon: Clock },
  { key: 'signed', label: '签署', icon: Check },
  { key: 'executing', label: '履约', icon: Truck }
]

// 当前步骤索引
const currentStepIndex = computed(() => {
  const status = props.contract.status
  switch (status) {
    case 0: return 0 // 草稿
    case 1: return 1 // 待签署
    case 2: return 2 // 已签署
    case 3: return 3 // 履约中
    case 4: return 4 // 已完成
    case 5: return -1 // 已取消
    default: return 0
  }
})

const statusInfo = computed(() => {
  return CONTRACT_STATUS_MAP[props.contract.status as ContractStatus] || CONTRACT_STATUS_MAP[0]
})

const isBuyer = computed(() => props.currentCompanyId === props.contract.buyerCompanyId)
const isSeller = computed(() => props.currentCompanyId === props.contract.sellerCompanyId)

const canSign = computed(() => {
  if (props.contract.status !== 1) return false
  if (isBuyer.value && !props.contract.buyerSigned) return true
  if (isSeller.value && !props.contract.sellerSigned) return true
  return false
})

const isCancelled = computed(() => props.contract.status === 5)
const isCompleted = computed(() => props.contract.status === 4)

function formatAmount(val: number | string): string {
  const num = typeof val === 'string' ? parseFloat(val) : val
  return isNaN(num) ? '-' : num.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden max-w-[360px]">
    <!-- 头部 -->
    <div class="px-4 py-3 bg-gradient-to-r from-slate-50 to-gray-50 border-b border-gray-100">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-slate-900 flex items-center justify-center">
          <FileText class="w-5 h-5 text-white" />
        </div>
        <div class="flex-1 min-w-0">
          <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">采购合同</div>
          <div class="text-sm font-bold text-gray-900 truncate">{{ contract.contractNo }}</div>
        </div>
        <span :class="['text-[10px] font-bold px-2.5 py-1 rounded-full', statusInfo.color]">
          {{ statusInfo.label }}
        </span>
      </div>
    </div>

    <!-- 进度条 -->
    <div v-if="!isCancelled" class="px-4 py-4 border-b border-gray-100">
      <div class="flex items-center justify-between mb-2">
        <template v-for="(step, index) in steps" :key="step.key">
          <!-- 步骤节点 -->
          <div class="flex flex-col items-center">
            <div
              :class="[
                'w-8 h-8 rounded-full flex items-center justify-center transition-all',
                index < currentStepIndex
                  ? 'bg-brand-500 text-white'
                  : index === currentStepIndex
                    ? 'bg-brand-500 text-white ring-4 ring-brand-100'
                    : 'bg-gray-100 text-gray-400'
              ]"
            >
              <CheckCircle v-if="index < currentStepIndex" class="w-4 h-4" />
              <component v-else :is="step.icon" class="w-4 h-4" />
            </div>
            <span
              :class="[
                'mt-1.5 text-[10px] font-medium',
                index <= currentStepIndex ? 'text-brand-600' : 'text-gray-400'
              ]"
            >
              {{ step.label }}
            </span>
          </div>

          <!-- 连接线 -->
          <div
            v-if="index < steps.length - 1"
            :class="[
              'flex-1 h-0.5 mx-1 transition-all',
              index < currentStepIndex ? 'bg-brand-500' : 'bg-gray-200'
            ]"
          ></div>
        </template>
      </div>
    </div>

    <!-- 签署状态 -->
    <div class="px-4 py-3 border-b border-gray-100">
      <div class="flex gap-3">
        <!-- 买方签署状态 -->
        <div class="flex-1 bg-gray-50 rounded-xl p-3 text-center">
          <div class="text-[10px] text-gray-400 mb-1">买方</div>
          <div class="flex items-center justify-center gap-1">
            <template v-if="contract.buyerSigned">
              <Check class="w-4 h-4 text-brand-500" />
              <span class="text-xs font-medium text-brand-600">已签署</span>
            </template>
            <template v-else>
              <Clock class="w-4 h-4 text-amber-500" />
              <span class="text-xs font-medium text-amber-600">待签署</span>
            </template>
          </div>
          <div class="text-[10px] text-gray-400 mt-1 truncate">{{ contract.buyerCompanyName }}</div>
        </div>

        <!-- 卖方签署状态 -->
        <div class="flex-1 bg-gray-50 rounded-xl p-3 text-center">
          <div class="text-[10px] text-gray-400 mb-1">卖方</div>
          <div class="flex items-center justify-center gap-1">
            <template v-if="contract.sellerSigned">
              <Check class="w-4 h-4 text-brand-500" />
              <span class="text-xs font-medium text-brand-600">已签署</span>
            </template>
            <template v-else>
              <Clock class="w-4 h-4 text-amber-500" />
              <span class="text-xs font-medium text-amber-600">待签署</span>
            </template>
          </div>
          <div class="text-[10px] text-gray-400 mt-1 truncate">{{ contract.sellerCompanyName }}</div>
        </div>
      </div>
    </div>

    <!-- 合同金额 -->
    <div class="px-4 py-3 flex items-center justify-between">
      <span class="text-xs text-gray-500">合同金额</span>
      <span class="text-lg font-bold text-brand-600">
        <template v-if="contract.basisPrice !== undefined && contract.basisPrice !== null && contract.basisPrice !== ''">
          待结算
        </template>
        <template v-else>
          ¥{{ formatAmount(contract.totalAmount) }}
        </template>
      </span>
    </div>

    <!-- 操作按钮 -->
    <div class="px-4 py-3 bg-gray-50 border-t border-gray-100 flex gap-2">
      <button
        @click="emit('view')"
        class="flex-1 flex items-center justify-center gap-1.5 px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-xs font-bold text-gray-700 hover:bg-gray-50 transition-all active:scale-95"
      >
        <FileText class="w-4 h-4" />
        查看详情
      </button>
      <button
        v-if="canSign"
        @click="emit('sign')"
        class="flex-1 flex items-center justify-center gap-1.5 px-4 py-2.5 bg-brand-600 rounded-xl text-xs font-bold text-white hover:bg-brand-700 transition-all active:scale-95"
      >
        <Pen class="w-4 h-4" />
        立即签署
      </button>
    </div>
  </div>
</template>
