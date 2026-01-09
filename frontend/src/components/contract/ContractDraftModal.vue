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
  basisPrice?: number
  contractCode?: string
  futuresPrice?: number
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

// 字段名语义映射
const FIELD_MAP: Record<string, string> = {
  origin: '产地',
  quantity: '发布量',
  remark: '备注',
  storageMethod: '存储方式',
  packaging: '包装方式',
  deliveryMode: '交货方式',
  productionDate: '生产日期',
  shelfLife: '保质期'
}

// 解析产品参数用于展示
const displayParams = computed(() => {
  if (!props.paramsJson) return []
  try {
    const data = JSON.parse(props.paramsJson)
    const rawParams = data?.params || data || {}
    const finalParams: Array<{ name: string; value: string }> = []
    
    // 黑名单：系统元数据、重复展示的业务字段
    const BLACKLIST = [
      'snapshotTime', 'priceType', 'id', 'categoryName', 'title', 
      'productName', 'companyName', 'nickName', 'exFactoryPrice', 'expectedPrice',
      'remainingQuantity', 'unit', 'basisQuotes', 'basisPrice', 
      'contractCode', 'futuresPrice', 'originPrice', 'shipAddress', 'purchaseAddress',
      'deliveryMode', 'storageMethod', 'packaging', 'paramsJson'
    ]

    // 智能展开函数
    const processEntry = (name: string, value: any) => {
      if (BLACKLIST.includes(name) || /^\d+$/.test(name)) return
      
      // 过滤空值
      if (value === null || value === undefined || value === '' || value === 'null' || value === 'undefined') return

      // 处理嵌套 paramsJson 字符串
      if (name === 'paramsJson' && typeof value === 'string' && value.startsWith('{')) {
        try {
          const inner = JSON.parse(value)
          Object.entries(inner).forEach(([k, v]) => processEntry(k, v))
          return
        } catch { /* ignore */ }
      }

      // 处理对象格式 { name, value }
      if (typeof value === 'object' && value !== null && 'name' in value) {
        finalParams.push({ name: (value as any).name, value: String((value as any).value) })
      } else {
        // 语义映射
        const displayName = FIELD_MAP[name] || name
        finalParams.push({ name: displayName, value: String(value) })
      }
    }

    Object.entries(rawParams).forEach(([k, v]) => processEntry(k, v))
    
    // 如果顶级没有处理 paramsJson，尝试手动处理一次（兼容不同快照格式）
    if (rawParams.paramsJson && typeof rawParams.paramsJson === 'string') {
      try {
        const inner = JSON.parse(rawParams.paramsJson)
        Object.entries(inner).forEach(([k, v]) => processEntry(k, v))
      } catch { /* ignore */ }
    }

    return finalParams
  } catch {
    return []
  }
})

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
  const isBasis = props.basisPrice !== undefined && props.contractCode
  
  let priceDesc = ''
  if (isBasis) {
    const fPrice = props.futuresPrice || 0
    const bPrice = props.basisPrice!
    const rPrice = fPrice + bPrice
    priceDesc = `基差定价：${bPrice > 0 ? '+' : ''}${bPrice} 元/吨\n    挂钩合约：${props.contractCode}\n    盘面参考价：¥${fPrice.toFixed(2)}\n    当前核算单价：¥${rPrice.toFixed(2)}\n    （最终结算单价 = 签署合同时点的期货盘面价 + 基差）`
  } else {
    priceDesc = `单价：¥${props.unitPrice || '____'} 元/${props.unit || '吨'}`
  }

  // 格式化参数
  const paramsText = displayParams.value.length > 0 
    ? '\n    质量标准：' + displayParams.value.map(p => `${p.name}: ${p.value}`).join('; ')
    : ''

  return `一、标的物
    产品名称：${props.productName || '____'}
    数量：${props.quantity || '____'} ${props.unit || '吨'}
    ${priceDesc}${paramsText}

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
            <div class="text-xs text-gray-400">{{ basisPrice !== undefined ? '定价机制' : '成交单价 (元/吨)' }}</div>
            <div class="flex flex-col gap-0.5">
              <template v-if="basisPrice !== undefined">
                <span class="font-bold text-gray-900 text-base">
                  基差 {{ basisPrice > 0 ? '+' : '' }}{{ basisPrice }}
                  <span class="text-xs text-gray-400 font-normal">({{ contractCode }})</span>
                </span>
                <span v-if="futuresPrice !== undefined" class="text-[10px] text-gray-500 leading-tight">
                  当前核算价: <span class="text-emerald-600 font-bold">¥{{ (futuresPrice + basisPrice).toFixed(2) }}</span>
                  <span class="text-gray-300 ml-1">(盘面 ¥{{ futuresPrice }})</span>
                </span>
                <span v-else class="text-[10px] text-gray-400 leading-tight">
                  核算公式: 盘面价 {{ basisPrice > 0 ? '+' : '' }}{{ basisPrice }}
                </span>
              </template>
              <template v-else>
                <span class="font-bold text-gray-900 text-base">¥{{ unitPrice || '-' }}</span>
              </template>
            </div>
          </div>
          <div class="space-y-1">
            <div class="text-xs text-gray-400">预估总金额</div>
            <span class="font-bold text-emerald-600 text-lg">
              <template v-if="basisPrice !== undefined">
                待结算 <span class="text-[10px] font-normal text-gray-400">(根据签署时刻盘面核算)</span>
              </template>
              <template v-else>
                ¥{{ totalAmount }}
              </template>
            </span>
          </div>
        </div>
      </div>

      <!-- 详细规格参数 -->
      <div v-if="displayParams.length > 0" class="bg-gray-50/50 rounded-[24px] p-5 border border-gray-100/50">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 rounded-lg bg-amber-50 flex items-center justify-center">
              <Package class="w-4 h-4 text-amber-600" />
            </div>
            <span class="font-bold text-gray-900">质量标准与规格</span>
          </div>
          <div class="text-[10px] font-bold text-amber-600 bg-amber-50 px-2 py-0.5 rounded-full uppercase tracking-widest">
            Specifications
          </div>
        </div>
        <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
          <div v-for="p in displayParams" :key="p.name" class="flex flex-col gap-1 bg-white px-3 py-2 rounded-xl border border-gray-100 shadow-sm">
            <span class="text-[10px] font-bold text-gray-400 uppercase tracking-tight">{{ p.name }}</span>
            <span class="text-xs font-black text-gray-700 truncate">{{ p.value }}</span>
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
