<script setup lang="ts">
/**
 * Contract Sign Inline Component
 * Inline contract signing within chat context
 */
import { ref, computed } from 'vue'
import { FileText, Check, AlertCircle, Pen, X } from 'lucide-vue-next'
import type { ContractPayload } from '../../../types/chat/message'

const props = defineProps<{
  contract: ContractPayload
  currentCompanyId?: number
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'sign'): void
  (e: 'cancel'): void
  (e: 'view-full'): void
}>()

const confirmed = ref(false)

const isBuyer = computed(() => props.currentCompanyId === props.contract.buyerCompanyId)
const isSeller = computed(() => props.currentCompanyId === props.contract.sellerCompanyId)
const role = computed(() => isBuyer.value ? '买方' : isSeller.value ? '卖方' : '未知')

const alreadySigned = computed(() => {
  if (isBuyer.value) return props.contract.buyerSigned
  if (isSeller.value) return props.contract.sellerSigned
  return false
})

const canSign = computed(() => {
  return props.contract.status === 1 && !alreadySigned.value
})

function formatAmount(val: number | string): string {
  const num = typeof val === 'string' ? parseFloat(val) : val
  return isNaN(num) ? '-' : num.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

function handleSign() {
  if (!confirmed.value) return
  emit('sign')
}
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-200 shadow-lg overflow-hidden">
    <!-- 头部 -->
    <div class="px-5 py-4 bg-gradient-to-r from-brand-600 to-brand-700 text-white">
      <div class="flex items-center gap-3">
        <div class="w-12 h-12 rounded-xl bg-white/20 flex items-center justify-center">
          <Pen class="w-6 h-6" />
        </div>
        <div>
          <div class="text-xs font-medium text-brand-100">合同签署确认</div>
          <div class="text-lg font-bold">{{ contract.contractNo }}</div>
        </div>
      </div>
    </div>

    <!-- 已签署状态 -->
    <div v-if="alreadySigned" class="px-5 py-8 text-center">
      <div class="w-16 h-16 rounded-full bg-brand-100 flex items-center justify-center mx-auto mb-4">
        <Check class="w-8 h-8 text-brand-600" />
      </div>
      <div class="text-lg font-bold text-gray-900 mb-1">您已完成签署</div>
      <div class="text-sm text-gray-500">等待另一方完成签署后合同即可生效</div>
      <button
        @click="emit('view-full')"
        class="mt-4 px-6 py-2 text-sm font-medium text-brand-600 hover:bg-brand-50 rounded-lg transition-colors"
      >
        查看合同详情
      </button>
    </div>

    <!-- 签署表单 -->
    <template v-else-if="canSign">
      <!-- 合同摘要 -->
      <div class="px-5 py-4 space-y-3">
        <div class="flex justify-between items-center">
          <span class="text-sm text-gray-500">交易标的</span>
          <span class="text-sm font-medium text-gray-900">{{ contract.productName }}</span>
        </div>
        <div class="flex justify-between items-center">
          <span class="text-sm text-gray-500">交易数量</span>
          <span class="text-sm font-medium text-gray-900">{{ contract.quantity }} {{ contract.unit }}</span>
        </div>
        <div class="flex justify-between items-center">
          <span class="text-sm text-gray-500">合同金额</span>
          <span class="text-lg font-bold text-brand-600">
            <template v-if="contract.basisPrice !== undefined && contract.basisPrice !== null && contract.basisPrice !== ''">
              待结算
            </template>
            <template v-else>
              ¥{{ formatAmount(contract.totalAmount) }}
            </template>
          </span>
        </div>
      </div>

      <!-- 签署身份 -->
      <div class="px-5 py-3 bg-gray-50 border-y border-gray-100">
        <div class="flex items-center gap-2">
          <FileText class="w-4 h-4 text-gray-400" />
          <span class="text-sm text-gray-600">
            您将以 <span class="font-bold text-gray-900">{{ role }}</span> 身份签署此合同
          </span>
        </div>
      </div>

      <!-- 确认勾选 -->
      <div class="px-5 py-4">
        <label class="flex items-start gap-3 cursor-pointer group">
          <div class="mt-0.5">
            <input
              type="checkbox"
              v-model="confirmed"
              class="w-5 h-5 rounded border-gray-300 text-brand-600 focus:ring-brand-500 cursor-pointer"
            />
          </div>
          <span class="text-sm text-gray-600 leading-relaxed">
            我已阅读并同意
            <button
              @click.prevent="emit('view-full')"
              class="text-brand-600 hover:underline"
            >
              合同条款
            </button>
            ，确认以上交易信息无误，同意签署此合同。
          </span>
        </label>
      </div>

      <!-- 提示 -->
      <div class="px-5 pb-4">
        <div class="flex items-start gap-2 p-3 bg-amber-50 rounded-lg">
          <AlertCircle class="w-4 h-4 text-amber-500 shrink-0 mt-0.5" />
          <p class="text-xs text-amber-700">
            电子签名具有法律效力。签署后合同将发送至双方，请确认信息准确无误。
          </p>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="px-5 py-4 bg-gray-50 border-t border-gray-100 flex gap-3">
        <button
          @click="emit('cancel')"
          class="flex-1 px-4 py-3 bg-white border border-gray-200 rounded-xl text-sm font-medium text-gray-700 hover:bg-gray-50 transition-all active:scale-95"
        >
          取消
        </button>
        <button
          @click="handleSign"
          :disabled="!confirmed || loading"
          class="flex-1 px-4 py-3 bg-brand-600 rounded-xl text-sm font-bold text-white hover:bg-brand-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all active:scale-95 flex items-center justify-center gap-2"
        >
          <div v-if="loading" class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
          <Pen v-else class="w-4 h-4" />
          <span>确认签署</span>
        </button>
      </div>
    </template>

    <!-- 不可签署状态 -->
    <div v-else class="px-5 py-8 text-center">
      <div class="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center mx-auto mb-4">
        <X class="w-8 h-8 text-gray-400" />
      </div>
      <div class="text-lg font-medium text-gray-900 mb-1">无法签署</div>
      <div class="text-sm text-gray-500">当前合同状态不允许签署操作</div>
    </div>
  </div>
</template>
