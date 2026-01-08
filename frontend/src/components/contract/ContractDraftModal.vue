<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FileText, Building2, Package, Calendar, MapPin, CreditCard, FileEdit } from 'lucide-vue-next'
import { createContractFromQuote, type ContractFromQuoteRequest } from '../../api/contract'
import { BaseModal, BaseButton } from '../ui'
import { useRouter } from 'vue-router'

const router = useRouter()

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

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

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
watch(() => props.modelValue, (v) => {
  if (v) {
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
      handleCreateError(res.message || '创建失败')
    }
  } catch (err: any) {
    handleCreateError(err.message || '创建失败')
  } finally {
    loading.value = false
  }
}

// 处理创建合同错误，提供友好提示
function handleCreateError(message: string) {
  // 检查是否是公司信息缺失的错误
  if (message.includes('公司信息') || message.includes('公司档案') || message.includes('完善资料')) {
    ElMessageBox.confirm(
      message,
      '无法创建合同',
      {
        confirmButtonText: '前往完善资料',
        cancelButtonText: '稍后再说',
        type: 'warning',
        confirmButtonClass: '!bg-emerald-600 !border-emerald-600 hover:!bg-emerald-700',
      }
    ).then(() => {
      emit('update:modelValue', false)
      router.push('/profile')
    }).catch(() => {
      // 用户选择稍后再说
    })
  } else {
    ElMessage.error(message)
  }
}
</script>

<template>
  <BaseModal
    v-model="visible"
    title="从报价单创建合同"
    subtitle="起草合同"
    size="xl"
  >
    <!-- 头部图标 -->
    <template #icon>
      <div class="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center">
        <FileText class="w-5 h-5 text-emerald-600" />
      </div>
    </template>

    <div class="space-y-6">
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
        <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider">合同标题</label>
        <input 
          v-model="form.title"
          type="text"
          class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-sm"
          :placeholder="defaultTitle"
        />
      </div>

      <!-- 交付信息 -->
      <div class="grid grid-cols-2 gap-4">
        <div class="space-y-2">
          <label class="text-xs font-bold text-gray-500 uppercase tracking-wider flex items-center gap-2">
            <Calendar class="w-4 h-4 text-gray-400" />
            交付日期
          </label>
          <input 
            v-model="form.deliveryDate"
            type="date"
            class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-sm"
          />
        </div>
        <div class="space-y-2">
          <label class="text-xs font-bold text-gray-500 uppercase tracking-wider flex items-center gap-2">
            <MapPin class="w-4 h-4 text-gray-400" />
            交付地点
          </label>
          <input 
            v-model="form.deliveryAddress"
            type="text"
            class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-sm"
            :placeholder="deliveryPlace || '请输入交付地点'"
          />
        </div>
      </div>

      <!-- 付款方式 -->
      <div class="space-y-2">
        <label class="text-xs font-bold text-gray-500 uppercase tracking-wider flex items-center gap-2">
          <CreditCard class="w-4 h-4 text-gray-400" />
          付款方式
        </label>
        <input 
          v-model="form.paymentMethod"
          type="text"
          class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-sm"
          :placeholder="paymentMethod || '请输入付款方式'"
        />
      </div>

      <!-- 合同条款 -->
      <div class="space-y-2">
        <label class="text-xs font-bold text-gray-500 uppercase tracking-wider flex items-center gap-2">
          <FileEdit class="w-4 h-4 text-gray-400" />
          合同条款
        </label>
        <textarea 
          v-model="form.terms"
          rows="10"
          class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all resize-none font-mono text-sm"
          :placeholder="defaultTerms"
        ></textarea>
        <div class="text-xs text-gray-400">留空将使用默认条款模板</div>
      </div>
    </div>

    <!-- 底部操作 -->
    <template #footer>
      <div class="flex-1 text-sm text-gray-500">
        创建后可在合同管理中心继续编辑
      </div>
      <BaseButton type="secondary" @click="visible = false">
        取消
      </BaseButton>
      <BaseButton type="primary" :loading="loading" @click="handleSubmit">
        创建合同草稿
      </BaseButton>
    </template>
  </BaseModal>
</template>
