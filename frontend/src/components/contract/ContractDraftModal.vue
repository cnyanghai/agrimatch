<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { X, FileText, Building2, Package, Calendar, MapPin, CreditCard, FileEdit } from 'lucide-vue-next'
import { createContractFromQuote, type ContractFromQuoteRequest } from '../../api/contract'

const props = defineProps<{
  modelValue: boolean
  quoteMessageId: number
  // 预填数据
  partyA?: string
  partyB?: string
  productName?: string
  quantity?: string
  unit?: string
  unitPrice?: string
  deliveryPlace?: string
  arrivalDate?: string
  paymentMethod?: string
  paramsJson?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'success', contractId: number): void
}>()

const loading = ref(false)

// 表单数据
const form = ref({
  title: '',
  deliveryDate: '',
  deliveryAddress: '',
  paymentMethod: '',
  terms: ''
})

// 计算总金额
const totalAmount = computed(() => {
  const qty = parseFloat(props.quantity || '0')
  const price = parseFloat(props.unitPrice || '0')
  if (isNaN(qty) || isNaN(price)) return '0.00'
  return (qty * price).toFixed(2)
})

// 生成默认标题
const defaultTitle = computed(() => {
  return `采购合同 - ${props.productName || '商品'}`
})

// 生成默认条款
const defaultTerms = computed(() => {
  return `一、标的物
    产品名称：${props.productName || '____'}
    数量：${props.quantity || '____'} ${props.unit || '吨'}
    单价：${props.unitPrice || '____'} 元/${props.unit || '吨'}

二、交付
    交付地点：${form.value.deliveryAddress || props.deliveryPlace || '____'}
    交付日期：${form.value.deliveryDate || props.arrivalDate || '____'}

三、付款方式
    ${form.value.paymentMethod || props.paymentMethod || '____'}

四、违约责任
    任何一方违反本合同约定的，应承担违约责任。

五、争议解决
    本合同发生争议，双方应友好协商解决；协商不成的，提交甲方所在地人民法院管辖。`
})

// 监听弹窗打开，初始化表单
watch(() => props.modelValue, (visible) => {
  if (visible) {
    form.value = {
      title: defaultTitle.value,
      deliveryDate: props.arrivalDate || '',
      deliveryAddress: props.deliveryPlace || '',
      paymentMethod: props.paymentMethod || '',
      terms: ''
    }
  }
})

// 提交创建合同
async function handleSubmit() {
  if (!props.quoteMessageId) {
    ElMessage.error('报价消息ID缺失')
    return
  }
  
  loading.value = true
  try {
    const req: ContractFromQuoteRequest = {
      quoteMessageId: props.quoteMessageId,
      title: form.value.title || defaultTitle.value,
      deliveryDate: form.value.deliveryDate || undefined,
      deliveryAddress: form.value.deliveryAddress || undefined,
      paymentMethod: form.value.paymentMethod || undefined,
      terms: form.value.terms || defaultTerms.value
    }
    
    const res = await createContractFromQuote(req)
    if (res.code === 0 && res.data) {
      ElMessage.success('合同草稿创建成功')
      emit('success', res.data)
      emit('update:modelValue', false)
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (err: any) {
    ElMessage.error(err.message || '创建失败')
  } finally {
    loading.value = false
  }
}

function close() {
  emit('update:modelValue', false)
}
</script>

<template>
  <Teleport to="body">
    <div v-if="modelValue" class="fixed inset-0 z-[2000] flex items-center justify-center">
      <!-- 遮罩 -->
      <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="close"></div>
      
      <!-- 弹窗内容 -->
      <div class="relative bg-white rounded-[32px] shadow-2xl w-full max-w-3xl max-h-[90vh] overflow-hidden flex flex-col">
        <!-- 头部 -->
        <div class="sticky top-0 bg-white border-b border-gray-100 px-8 py-5 flex items-center justify-between z-10">
          <div>
            <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-1">起草合同</div>
            <h2 class="text-xl font-bold text-gray-900">从报价单创建合同</h2>
          </div>
          <button 
            class="w-10 h-10 rounded-xl bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-all active:scale-95"
            @click="close"
          >
            <X class="w-5 h-5 text-gray-500" />
          </button>
        </div>

        <!-- 内容区域 -->
        <div class="flex-1 overflow-y-auto px-8 py-6 space-y-6">
          <!-- 交易信息卡片 -->
          <div class="bg-gray-50 rounded-2xl p-5 border border-gray-100">
            <div class="flex items-center gap-2 mb-4">
              <div class="w-8 h-8 rounded-lg bg-emerald-50 flex items-center justify-center">
                <FileText class="w-4 h-4 text-emerald-600" />
              </div>
              <span class="font-bold text-gray-900">交易信息</span>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div class="space-y-1">
                <div class="text-xs text-gray-400">甲方（买方）</div>
                <div class="flex items-center gap-2">
                  <Building2 class="w-4 h-4 text-gray-400" />
                  <span class="font-medium text-gray-900">{{ partyA || '待确定' }}</span>
                </div>
              </div>
              <div class="space-y-1">
                <div class="text-xs text-gray-400">乙方（卖方）</div>
                <div class="flex items-center gap-2">
                  <Building2 class="w-4 h-4 text-gray-400" />
                  <span class="font-medium text-gray-900">{{ partyB || '待确定' }}</span>
                </div>
              </div>
              <div class="space-y-1">
                <div class="text-xs text-gray-400">产品名称</div>
                <div class="flex items-center gap-2">
                  <Package class="w-4 h-4 text-gray-400" />
                  <span class="font-medium text-gray-900">{{ productName || '-' }}</span>
                </div>
              </div>
              <div class="space-y-1">
                <div class="text-xs text-gray-400">数量</div>
                <span class="font-medium text-gray-900">{{ quantity || '-' }} {{ unit || '吨' }}</span>
              </div>
              <div class="space-y-1">
                <div class="text-xs text-gray-400">单价</div>
                <span class="font-medium text-gray-900">¥{{ unitPrice || '-' }}/{{ unit || '吨' }}</span>
              </div>
              <div class="space-y-1">
                <div class="text-xs text-gray-400">总金额</div>
                <span class="font-bold text-emerald-600 text-lg">¥{{ totalAmount }}</span>
              </div>
            </div>
          </div>

          <!-- 合同标题 -->
          <div class="space-y-2">
            <label class="text-sm font-bold text-gray-700">合同标题</label>
            <input 
              v-model="form.title"
              type="text"
              class="w-full px-4 py-3 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all"
              :placeholder="defaultTitle"
            />
          </div>

          <!-- 交付信息 -->
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <label class="text-sm font-bold text-gray-700 flex items-center gap-2">
                <Calendar class="w-4 h-4 text-gray-400" />
                交付日期
              </label>
              <input 
                v-model="form.deliveryDate"
                type="date"
                class="w-full px-4 py-3 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all"
              />
            </div>
            <div class="space-y-2">
              <label class="text-sm font-bold text-gray-700 flex items-center gap-2">
                <MapPin class="w-4 h-4 text-gray-400" />
                交付地点
              </label>
              <input 
                v-model="form.deliveryAddress"
                type="text"
                class="w-full px-4 py-3 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all"
                :placeholder="deliveryPlace || '请输入交付地点'"
              />
            </div>
          </div>

          <!-- 付款方式 -->
          <div class="space-y-2">
            <label class="text-sm font-bold text-gray-700 flex items-center gap-2">
              <CreditCard class="w-4 h-4 text-gray-400" />
              付款方式
            </label>
            <input 
              v-model="form.paymentMethod"
              type="text"
              class="w-full px-4 py-3 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all"
              :placeholder="paymentMethod || '请输入付款方式'"
            />
          </div>

          <!-- 合同条款 -->
          <div class="space-y-2">
            <label class="text-sm font-bold text-gray-700 flex items-center gap-2">
              <FileEdit class="w-4 h-4 text-gray-400" />
              合同条款
            </label>
            <textarea 
              v-model="form.terms"
              rows="10"
              class="w-full px-4 py-3 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all resize-none font-mono text-sm"
              :placeholder="defaultTerms"
            ></textarea>
            <div class="text-xs text-gray-400">留空将使用默认条款模板</div>
          </div>
        </div>

        <!-- 底部操作 -->
        <div class="sticky bottom-0 bg-gray-50 border-t border-gray-100 px-8 py-4 flex items-center justify-between">
          <div class="text-sm text-gray-500">
            创建后可在合同管理中心继续编辑
          </div>
          <div class="flex items-center gap-3">
            <button 
              class="px-6 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold rounded-xl transition-all active:scale-95"
              @click="close"
            >
              取消
            </button>
            <button 
              class="px-6 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold rounded-xl transition-all active:scale-95 disabled:opacity-50"
              :disabled="loading"
              @click="handleSubmit"
            >
              {{ loading ? '创建中...' : '创建合同草稿' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

