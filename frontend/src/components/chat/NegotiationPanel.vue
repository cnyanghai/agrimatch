<script setup lang="ts">
import { computed, reactive, watch, ref } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'

// 结算方式选项
const paymentMethodOptions = [
  { value: '现结（款到发货）', label: '现结（款到发货）' },
  { value: '货到付款', label: '货到付款' },
  { value: '账期30天', label: '账期30天' },
  { value: '账期60天', label: '账期60天' },
  { value: '账期90天', label: '账期90天' },
  { value: '银行承兑', label: '银行承兑' }
]

// 发票类型选项
const invoiceTypeOptions = [
  { value: '增值税专用发票', label: '增值税专用发票' },
  { value: '增值税普通发票', label: '增值税普通发票' },
  { value: '不需要发票', label: '不需要发票' }
]

export type QuoteFields = {
  price?: string
  quantity?: string
  deliveryPlace?: string
  arrivalDate?: string
  paymentMethod?: string
  remark?: string
  invoiceType?: string
  packaging?: string
  deliveryMethod?: string
  // 存储动态技术指标
  dynamicParams?: Record<string, string>
}

// 只读产品信息
type ProductInfo = {
  categoryName?: string
  companyName?: string
  nickName?: string
  origin?: string
  storageMethod?: string
  remainingQuantity?: string
}

const props = defineProps<{
  disabled?: boolean
  peerLatestQuote?: QuoteFields | null
  subjectSnapshotJson?: string | null
}>()

const emit = defineEmits<{
  (e: 'send', payload: { msgType: 'QUOTE'; payload: any; summary: string }): void
}>()

// 只读产品信息
const productInfo = ref<ProductInfo>({})
const productParams = ref<Record<string, string>>({})

const form = reactive<QuoteFields>({
  price: '',
  quantity: '',
  deliveryPlace: '',
  arrivalDate: '',
  paymentMethod: '',
  remark: '',
  invoiceType: '',
  packaging: '',
  deliveryMethod: '',
  dynamicParams: {}
})

// 解析标的快照并填充默认值
function initFromSnapshot() {
  if (!props.subjectSnapshotJson) return
  try {
    const s = JSON.parse(props.subjectSnapshotJson)
    
    // 只读产品信息
    productInfo.value = {
      categoryName: s.categoryName || s.title,
      companyName: s.companyName,
      nickName: s.nickName,
      origin: s.origin,
      storageMethod: s.storageMethod,
      remainingQuantity: s.remainingQuantity ? String(s.remainingQuantity) : undefined
    }
    
    // 可编辑字段映射
    form.price = String(s.exFactoryPrice ?? s.expectedPrice ?? s.price ?? '')
    form.quantity = String(s.remainingQuantity ?? s.quantity ?? '')
    form.deliveryMethod = s.deliveryMode ?? s.deliveryMethod ?? ''
    form.deliveryPlace = s.shipAddress ?? s.purchaseAddress ?? s.deliveryPlace ?? ''
    // 结算方式：兼容多种字段名
    form.paymentMethod = s.paymentMethod ?? s.payment_method ?? s.settlementMethod ?? s.settlement ?? ''
    // 发票类型：兼容多种字段名
    form.invoiceType = s.invoiceType ?? s.invoice_type ?? s.invoiceRequirement ?? s.invoice ?? ''
    form.packaging = s.packaging ?? s.packagingRequirement ?? ''
    form.remark = s.remark ?? s.note ?? s.notes ?? ''
    // 到货日期：兼容多种字段名
    form.arrivalDate = s.arrivalDate ?? s.arrival_date ?? s.deliveryDate ?? s.delivery_date ?? s.expectedDeliveryDate ?? ''

    // 解析动态参数（支持新的 {"Name": "Value"} 格式）
    if (s.paramsJson) {
      try {
        const paramsData = JSON.parse(s.paramsJson)
        // 支持旧格式 { params: {...} } 和新格式 { "Name": "Value" }
        const params = paramsData?.params || paramsData || {}
        const dynamic: Record<string, string> = {}
        Object.entries(params).forEach(([k, v]) => {
          // 跳过纯数字键名（旧格式遗留）
          if (/^\d+$/.test(k)) return
          if (typeof v === 'object' && v !== null && 'name' in v) {
            // 旧格式: { "1": { name: "xxx", value: "yyy" } }
            dynamic[(v as any).name] = String((v as any).value)
          } else if (typeof v === 'string' || typeof v === 'number') {
            // 新格式: { "参数名": "参数值" }
            dynamic[k] = String(v)
          }
        })
        form.dynamicParams = { ...dynamic }
        productParams.value = { ...dynamic }
      } catch (e) {
        console.error('Failed to parse paramsJson', e)
      }
    }
  } catch (e) {
    console.error('Failed to parse subject snapshot', e)
  }
}

watch(() => props.subjectSnapshotJson, () => {
  initFromSnapshot()
}, { immediate: true })

function clean(v?: string) {
  const s = (v ?? '').trim()
  return s || undefined
}

const normalized = computed(() => {
  const out: QuoteFields = {
    price: clean(form.price),
    quantity: clean(form.quantity),
    deliveryPlace: clean(form.deliveryPlace),
    arrivalDate: clean(form.arrivalDate),
    paymentMethod: clean(form.paymentMethod),
    remark: clean(form.remark),
    invoiceType: clean(form.invoiceType),
    packaging: clean(form.packaging),
    deliveryMethod: clean(form.deliveryMethod),
    dynamicParams: { ...form.dynamicParams }
  }
  return out
})

const canSend = computed(() => {
  const v = normalized.value
  return !!(v.price || v.quantity)
})

const diff = computed(() => {
  const peer = props.peerLatestQuote || {}
  const mine = normalized.value
  const keys: Array<keyof QuoteFields> = [
    'price', 'quantity', 'deliveryPlace', 'arrivalDate', 
    'paymentMethod', 'remark',
    'invoiceType', 'packaging', 'deliveryMethod'
  ]
  const changed: Array<{ k: string; mine?: string; peer?: string }> = []
  
  // 检查基础字段
  for (const k of keys) {
    const mv = mine[k as keyof QuoteFields] as string
    const pv = peer[k as keyof QuoteFields] as string
    if ((mv || '') !== (pv || '')) {
      if (mv || pv) changed.push({ k: labelOf(k as any), mine: mv, peer: pv })
    }
  }

  // 检查动态字段
  const mDyn = mine.dynamicParams || {}
  const pDyn = peer.dynamicParams || {}
  const allDynKeys = new Set([...Object.keys(mDyn), ...Object.keys(pDyn)])
  for (const dk of allDynKeys) {
    if ((mDyn[dk] || '') !== (pDyn[dk] || '')) {
      changed.push({ k: dk, mine: mDyn[dk], peer: pDyn[dk] })
    }
  }

  return changed
})

function labelOf(k: keyof QuoteFields) {
  if (k === 'price') return '单价'
  if (k === 'quantity') return '数量'
  if (k === 'deliveryPlace') return '交付地'
  if (k === 'arrivalDate') return '到货期'
  if (k === 'paymentMethod') return '结算方式'
  if (k === 'invoiceType') return '发票类型'
  if (k === 'packaging') return '包装方式'
  if (k === 'deliveryMethod') return '交货方式'
  return '备注'
}

function buildSummary(v: QuoteFields) {
  const parts: string[] = []
  if (v.price) parts.push(`¥${v.price}`)
  if (v.quantity) parts.push(`${v.quantity}`)
  return parts.length ? parts.join(' · ') : '报价卡'
}

function clear() {
  form.price = ''
  form.quantity = ''
  form.deliveryPlace = ''
  form.arrivalDate = ''
  form.paymentMethod = ''
  form.remark = ''
  form.invoiceType = ''
  form.packaging = ''
  form.deliveryMethod = ''
  form.dynamicParams = {}
  // 清空后重新根据快照初始化默认值
  initFromSnapshot()
}

function send() {
  if (!canSend.value) return
  const v = normalized.value
  const payload = {
    version: 1,
    kind: 'QUOTE_V1',
    createdAt: new Date().toISOString(),
    fields: v
  }
  emit('send', { msgType: 'QUOTE', payload, summary: buildSummary(v) })
}
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-200 p-5 shadow-2xl">
    <div class="flex items-center justify-between mb-5">
      <div>
        <div class="text-[10px] font-bold uppercase tracking-widest text-brand-600">Double Confirmation</div>
        <div class="mt-0.5 font-bold text-gray-900 text-lg">正式交易报价单</div>
      </div>
      <div class="flex items-center gap-2">
        <button
          class="px-4 py-2 rounded-full bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-bold transition-all  disabled:opacity-50"
          :disabled="disabled"
          @click="clear"
        >
          重置默认
        </button>
        <button
          class="px-6 py-2 rounded-full bg-brand-600 hover:bg-brand-700 text-white text-sm font-bold shadow-md shadow-brand-100 transition-all  disabled:opacity-50"
          :disabled="disabled || !canSend"
          @click="send"
        >
          发送报价
        </button>
      </div>
    </div>

    <!-- 产品规格参数 (只读) -->
    <div v-if="Object.keys(productParams).length > 0" class="mb-5 p-4 bg-gray-50 rounded-lg border border-gray-200">
      <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-3 flex items-center gap-1">
        产品规格参数 <InfoFilled class="w-3 h-3" />
      </div>
      <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3">
        <div v-for="(v, k) in productParams" :key="k" class="bg-white px-3 py-2 rounded-lg border border-gray-50 flex flex-col gap-0.5 shadow-sm">
          <span class="text-[10px] text-gray-400 font-medium">{{ k }}</span>
          <span class="text-xs font-bold text-gray-700 truncate">{{ v }}</span>
        </div>
      </div>
    </div>

    <!-- 只读产品信息区 -->
    <div v-if="productInfo.categoryName || productInfo.companyName" class="mb-4 p-3 bg-gray-50 rounded-lg border border-gray-200">
      <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-2">产品信息（只读）</div>
      <div class="flex flex-wrap gap-3 text-xs">
        <div v-if="productInfo.categoryName" class="flex items-center gap-1">
          <span class="text-gray-400">品类:</span>
          <span class="font-bold text-gray-700">{{ productInfo.categoryName }}</span>
        </div>
        <div v-if="productInfo.companyName" class="flex items-center gap-1">
          <span class="text-gray-400">公司:</span>
          <span class="font-bold text-gray-700">{{ productInfo.companyName }}</span>
        </div>
        <div v-if="productInfo.origin" class="flex items-center gap-1">
          <span class="text-gray-400">产地:</span>
          <span class="font-bold text-gray-700">{{ productInfo.origin }}</span>
        </div>
        <div v-if="productInfo.storageMethod" class="flex items-center gap-1">
          <span class="text-gray-400">储存:</span>
          <span class="font-bold text-gray-700">{{ productInfo.storageMethod }}</span>
        </div>
        <div v-if="productInfo.remainingQuantity" class="flex items-center gap-1">
          <span class="text-gray-400">剩余:</span>
          <span class="font-bold text-brand-600">{{ productInfo.remainingQuantity }} 吨</span>
        </div>
      </div>
    </div>

    <!-- 可编辑报价字段 -->
    <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
      <!-- 核心交易项 -->
      <div class="space-y-1">
        <div class="text-[10px] font-bold text-gray-400 uppercase tracking-tight">意向单价 (元/吨)</div>
        <input v-model="form.price" :disabled="disabled" class="w-full border-2 border-gray-200 rounded-lg px-3 py-2 text-sm font-bold text-brand-600 focus:border-brand-500 outline-none transition-all" placeholder="必填" />
      </div>
      <div class="space-y-1">
        <div class="text-[10px] font-bold text-gray-400 uppercase tracking-tight">意向数量</div>
        <input v-model="form.quantity" :disabled="disabled" class="w-full border-2 border-gray-200 rounded-lg px-3 py-2 text-sm font-bold text-gray-900 focus:border-brand-500 outline-none transition-all" placeholder="必填" />
      </div>

      <!-- 物流与商务项 -->
      <div class="space-y-1">
        <div class="text-[10px] font-bold text-gray-400 uppercase tracking-tight">交货方式</div>
        <input v-model="form.deliveryMethod" :disabled="disabled" class="w-full border-2 border-gray-200 rounded-lg px-3 py-2 text-sm focus:border-brand-500 outline-none transition-all" />
      </div>
      <div class="space-y-1">
        <div class="text-[10px] font-bold text-gray-400 uppercase tracking-tight">交付地点</div>
        <input v-model="form.deliveryPlace" :disabled="disabled" class="w-full border-2 border-gray-200 rounded-lg px-3 py-2 text-sm focus:border-brand-500 outline-none transition-all" />
      </div>
      <div class="space-y-1">
        <div class="text-[10px] font-bold text-gray-400 uppercase tracking-tight">到货日期</div>
        <el-date-picker
          v-model="form.arrivalDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择日期"
          :disabled="disabled"
          class="!w-full neo-date-picker"
        />
      </div>
      <div class="space-y-1">
        <div class="text-[10px] font-bold text-gray-400 uppercase tracking-tight">结算方式</div>
        <el-select
          v-model="form.paymentMethod"
          placeholder="选择结算方式"
          :disabled="disabled"
          filterable
          allow-create
          class="!w-full neo-select"
        >
          <el-option
            v-for="opt in paymentMethodOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </div>
      <div class="space-y-1">
        <div class="text-[10px] font-bold text-gray-400 uppercase tracking-tight">发票要求</div>
        <el-select
          v-model="form.invoiceType"
          placeholder="选择发票类型"
          :disabled="disabled"
          filterable
          allow-create
          class="!w-full neo-select"
        >
          <el-option
            v-for="opt in invoiceTypeOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </div>
      <div class="space-y-1">
        <div class="text-[10px] font-bold text-gray-400 uppercase tracking-tight">包装要求</div>
        <input v-model="form.packaging" :disabled="disabled" class="w-full border-2 border-gray-200 rounded-lg px-3 py-2 text-sm focus:border-brand-500 outline-none transition-all" />
      </div>

      <!-- 动态技术指标 -->
      <div v-for="(_, name) in form.dynamicParams" :key="name" class="space-y-1">
        <div class="text-[10px] font-bold text-gray-400 uppercase tracking-tight">{{ name }}</div>
        <input v-model="form.dynamicParams![name]" :disabled="disabled" class="w-full border-2 border-gray-200 rounded-lg px-3 py-2 text-sm focus:border-brand-500 outline-none transition-all" />
      </div>

      <!-- 备注（独占两列或更多） -->
      <div class="col-span-2 space-y-1">
        <div class="text-[10px] font-bold text-gray-400 uppercase tracking-tight">备注信息</div>
        <textarea v-model="form.remark" :disabled="disabled" rows="1" class="w-full resize-none border-2 border-gray-200 rounded-lg px-3 py-2 text-sm focus:border-brand-500 outline-none transition-all" placeholder="如有其他特殊约定请在此说明" />
      </div>
    </div>

    <!-- 差异对比 -->
    <div class="mt-5 pt-5 border-t border-gray-50" v-if="diff.length">
      <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-3">参数变更预览</div>
      <div class="flex flex-wrap gap-2">
        <div v-for="d in diff" :key="d.k" class="bg-brand-50/50 border border-brand-100/50 rounded-lg px-3 py-1.5 text-xs flex items-center gap-1.5">
          <span class="text-gray-500 font-medium">{{ d.k }}：</span>
          <div class="flex items-center gap-1.5">
            <template v-if="d.peer">
              <span class="text-gray-400 line-through">{{ d.peer }}</span>
              <span class="text-brand-400 font-bold">→</span>
            </template>
            <span :class="[d.mine ? 'text-brand-700 font-black' : 'text-red-400 italic font-medium']">
              {{ d.mine || '[已清空]' }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 日期选择器样式 */
.neo-date-picker :deep(.el-input__wrapper) {
  border: 2px solid rgb(243 244 246);
  border-radius: 0.75rem;
  padding: 0.375rem 0.75rem;
  box-shadow: none;
  transition: all 0.15s;
}

.neo-date-picker :deep(.el-input__wrapper:hover),
.neo-date-picker :deep(.el-input__wrapper.is-focus) {
  border-color: rgb(16 185 129);
  box-shadow: none;
}

/* 下拉选择器样式 */
.neo-select :deep(.el-input__wrapper) {
  border: 2px solid rgb(243 244 246);
  border-radius: 0.75rem;
  padding: 0.375rem 0.75rem;
  box-shadow: none;
  transition: all 0.15s;
}

.neo-select :deep(.el-input__wrapper:hover),
.neo-select :deep(.el-input__wrapper.is-focus) {
  border-color: rgb(16 185 129);
  box-shadow: none;
}

.neo-select :deep(.el-input__inner) {
  font-size: var(--font-sm);
}
</style>
