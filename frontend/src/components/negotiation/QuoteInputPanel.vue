<script setup lang="ts">
/**
 * QuoteInputPanel - 报价输入面板
 * 基于产品需求数据快速发送报价
 */
import { ref, computed, watch } from 'vue'
import { X, Send, Package, DollarSign, MapPin, Calendar, CreditCard } from 'lucide-vue-next'
import type { RequirementData } from './ProductRequirementForm.vue'

const props = defineProps<{
  /** 产品需求数据 */
  requirementData?: Partial<RequirementData>
  /** 是否显示 */
  visible: boolean
  /** 是否正在发送 */
  sending?: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'submit', payload: QuotePayload): void
}>()

export interface QuotePayload {
  price: number
  quantity: string
  unit: string
  deliveryPlace: string
  arrivalDate: string
  paymentMethod: string
  remark?: string
}

// 表单数据
const form = ref<QuotePayload>({
  price: 0,
  quantity: '',
  unit: '吨',
  deliveryPlace: '',
  arrivalDate: '',
  paymentMethod: '货到付款',
  remark: ''
})

// 从需求数据初始化
watch(() => props.requirementData, (data) => {
  if (data) {
    form.value = {
      price: data.price || 0,
      quantity: data.quantity ? String(data.quantity) : '',
      unit: data.unit || '吨',
      deliveryPlace: data.deliveryPlace || '',
      arrivalDate: data.deliveryDate || '',
      paymentMethod: data.paymentMethod || '货到付款',
      remark: ''
    }
  }
}, { immediate: true, deep: true })

// 付款方式选项
const paymentOptions = [
  '货到付款',
  '预付30%',
  '预付50%',
  '全款预付',
  '账期30天'
]

// 单位选项
const unitOptions = ['吨', '公斤', '斤', '件', '箱']

// 计算总金额
const totalAmount = computed(() => {
  const qty = parseFloat(form.value.quantity) || 0
  return form.value.price * qty
})

// 表单是否有效
const isValid = computed(() => {
  return form.value.price > 0 &&
         form.value.quantity &&
         parseFloat(form.value.quantity) > 0
})

// 格式化金额
function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 0
  }).format(amount)
}

// 提交报价
function handleSubmit() {
  if (!isValid.value) return
  emit('submit', { ...form.value })
}
</script>

<template>
  <Transition name="slide-up">
    <div
      v-if="visible"
      class="fixed inset-0 z-50 flex items-end justify-center bg-black/30"
      @click.self="emit('close')"
    >
      <div class="w-full max-w-lg bg-white rounded-t-2xl shadow-2xl overflow-hidden">
        <!-- 头部 -->
        <div class="px-4 py-3 border-b border-gray-100 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <DollarSign class="w-5 h-5 text-brand-600" />
            <h3 class="font-bold text-gray-900">发送报价</h3>
          </div>
          <button
            @click="emit('close')"
            class="p-1.5 hover:bg-gray-100 rounded-lg text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X class="w-5 h-5" />
          </button>
        </div>

        <!-- 产品信息 -->
        <div v-if="requirementData?.productName" class="px-4 py-3 bg-gray-50 border-b border-gray-100">
          <div class="flex items-center gap-2">
            <Package class="w-4 h-4 text-brand-500" />
            <span class="font-medium text-gray-900">{{ requirementData.productName }}</span>
            <span v-if="requirementData.qualityGrade" class="text-xs text-gray-500 bg-gray-200 px-2 py-0.5 rounded">
              {{ requirementData.qualityGrade }}
            </span>
          </div>
        </div>

        <!-- 表单 -->
        <div class="p-4 space-y-4 max-h-[60vh] overflow-y-auto">
          <!-- 价格和数量 -->
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-xs font-medium text-gray-500 mb-1">
                <DollarSign class="w-3 h-3 inline mr-1" />
                单价 (元)
              </label>
              <input
                v-model.number="form.price"
                type="number"
                min="0"
                step="0.01"
                class="w-full h-10 px-3 text-lg font-bold border border-gray-200 rounded-lg
                       focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
                placeholder="0.00"
              />
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-500 mb-1">数量</label>
              <div class="flex gap-2">
                <input
                  v-model="form.quantity"
                  type="text"
                  class="flex-1 h-10 px-3 text-lg font-bold border border-gray-200 rounded-lg
                         focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
                  placeholder="0"
                />
                <select
                  v-model="form.unit"
                  class="w-16 h-10 px-2 border border-gray-200 rounded-lg text-sm
                         focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
                >
                  <option v-for="u in unitOptions" :key="u" :value="u">{{ u }}</option>
                </select>
              </div>
            </div>
          </div>

          <!-- 交货信息 -->
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-xs font-medium text-gray-500 mb-1">
                <MapPin class="w-3 h-3 inline mr-1" />
                交货地点
              </label>
              <input
                v-model="form.deliveryPlace"
                type="text"
                class="w-full h-10 px-3 border border-gray-200 rounded-lg text-sm
                       focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
                placeholder="输入地址"
              />
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-500 mb-1">
                <Calendar class="w-3 h-3 inline mr-1" />
                交货日期
              </label>
              <input
                v-model="form.arrivalDate"
                type="date"
                class="w-full h-10 px-3 border border-gray-200 rounded-lg text-sm
                       focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
              />
            </div>
          </div>

          <!-- 付款方式 -->
          <div>
            <label class="block text-xs font-medium text-gray-500 mb-1">
              <CreditCard class="w-3 h-3 inline mr-1" />
              付款方式
            </label>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="opt in paymentOptions"
                :key="opt"
                @click="form.paymentMethod = opt"
                :class="[
                  'px-3 py-1.5 text-sm rounded-lg border transition-all',
                  form.paymentMethod === opt
                    ? 'border-brand-500 bg-brand-50 text-brand-700 font-medium'
                    : 'border-gray-200 text-gray-600 hover:border-gray-300'
                ]"
              >
                {{ opt }}
              </button>
            </div>
          </div>

          <!-- 备注 -->
          <div>
            <label class="block text-xs font-medium text-gray-500 mb-1">备注（可选）</label>
            <textarea
              v-model="form.remark"
              rows="2"
              class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm resize-none
                     focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
              placeholder="添加备注..."
            />
          </div>
        </div>

        <!-- 底部操作 -->
        <div class="px-4 py-3 bg-gray-50 border-t border-gray-100 flex items-center justify-between">
          <div>
            <span class="text-xs text-gray-500">预计总金额</span>
            <div class="text-lg font-bold text-brand-600">{{ formatCurrency(totalAmount) }}</div>
          </div>
          <div class="flex gap-2">
            <button
              @click="emit('close')"
              class="px-4 py-2 text-sm font-medium text-gray-600 hover:bg-gray-100 rounded-lg transition-colors"
            >
              取消
            </button>
            <button
              @click="handleSubmit"
              :disabled="!isValid || sending"
              class="px-6 py-2 text-sm font-bold text-white bg-brand-600 rounded-lg
                     hover:bg-brand-700 disabled:opacity-50 disabled:cursor-not-allowed
                     transition-all flex items-center gap-2"
            >
              <Send v-if="!sending" class="w-4 h-4" />
              <span>{{ sending ? '发送中...' : '发送报价' }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
}

.slide-up-enter-from > div,
.slide-up-leave-to > div {
  transform: translateY(100%);
}
</style>
