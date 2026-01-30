<script setup lang="ts">
/**
 * ProductRequirementForm - 完整版产品需求信息表单
 * 字段与供应/采购发布信息完全一致
 * 支持议价时对价格、参数等进行修改
 */
import { ref, computed, watch, onMounted } from 'vue'
import { Package, Send, MapPin, Truck, CreditCard, FileText, Settings } from 'lucide-vue-next'

export interface BasisQuoteItem {
  contractCode: string
  basisPrice: number
  availableQty?: number
  referencePrice?: number | null  // 参考价 = 期货最新价 + 基差
}

export interface RequirementData {
  // 基础信息
  productName: string
  categoryName?: string
  categoryId?: number

  // 数量价格
  quantity: number
  unit: string
  price?: number
  priceType?: 'SPOT' | 'BASIS'  // 一口价 / 基差
  basisPrice?: number       // 保留向后兼容（第一个合约）
  contractCode?: string     // 保留向后兼容（第一个合约）
  basisQuotes?: BasisQuoteItem[]  // 全部基差合约

  // 质量规格
  qualityGrade?: string
  packaging?: string  // 散装、袋装、箱装
  origin?: string     // 产地

  // 交付条款
  deliveryDate: string
  deliveryPlace: string
  deliveryMethod?: string  // 到厂、自提、物流配送

  // 结算条款
  paymentMethod?: string   // 现款、账期、货到付款等
  invoiceType?: string     // 发票类型

  // 动态参数（产品品类相关）
  dynamicParams?: Record<string, string>

  // 备注
  remark?: string
}

const props = defineProps<{
  initialData?: Partial<RequirementData>
  readonly?: boolean
  /** 标的类型：SUPPLY=供应, NEED=采购 */
  subjectType?: 'SUPPLY' | 'NEED'
  /** 是否显示发送报价按钮 */
  showSendButton?: boolean
  /** 发送中状态 */
  sending?: boolean
}>()

const emit = defineEmits<{
  (e: 'update', data: RequirementData): void
  (e: 'reset'): void
  (e: 'send-quote', data: RequirementData): void
}>()

const showAdvanced = ref(false)

// 表单数据
const form = ref<RequirementData>({
  productName: '',
  categoryName: '',
  quantity: 0,
  unit: '吨',
  qualityGrade: '',
  packaging: '散装',
  origin: '',
  deliveryDate: '',
  deliveryPlace: '',
  deliveryMethod: '物流配送',
  price: undefined,
  priceType: 'SPOT',
  basisPrice: undefined,
  contractCode: '',
  basisQuotes: [],
  paymentMethod: '货到付款',
  invoiceType: '增值税专用发票',
  dynamicParams: {},
  remark: ''
})

// 包装方式选项
const packagingOptions = ['散装', '袋装', '箱装', '吨包']

// 交货方式选项
const deliveryMethodOptions = ['到厂', '自提', '物流配送']

// 付款方式选项
const paymentOptions = [
  { value: '现款现货', label: '现款现货' },
  { value: '货到付款', label: '货到付款' },
  { value: '预付30%', label: '预付30%' },
  { value: '预付50%', label: '预付50%' },
  { value: '全款预付', label: '全款预付' },
  { value: '账期15天', label: '账期15天' },
  { value: '账期30天', label: '账期30天' },
]

// 发票类型选项
const invoiceOptions = [
  { value: '增值税专用发票', label: '增值税专用发票' },
  { value: '增值税普通发票', label: '增值税普通发票' },
  { value: '不需要发票', label: '不需要发票' },
]

// 单位选项
const unitOptions = ['吨', '公斤', '斤', '件', '箱', '车']

// 初始化
onMounted(() => {
  if (props.initialData) {
    form.value = { ...form.value, ...props.initialData }
  }
})

// 监听初始数据变化
watch(() => props.initialData, (newData) => {
  if (newData) {
    form.value = { ...form.value, ...newData }
  }
}, { deep: true })

// 监听表单变化，通知父组件
watch(form, (newForm) => {
  emit('update', { ...newForm })
}, { deep: true })

// 计算预计总金额
const estimatedTotal = computed(() => {
  if (!form.value.price || !form.value.quantity) return null
  return form.value.price * form.value.quantity
})

// 格式化金额
function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 0
  }).format(amount)
}

// 获取动态参数的键值对
const dynamicParamsList = computed(() => {
  if (!form.value.dynamicParams) return []
  return Object.entries(form.value.dynamicParams).map(([key, value]) => ({ key, value }))
})

// 更新基差合约数量
function updateBasisQuoteQty(idx: number, value: string) {
  if (!form.value.basisQuotes || !form.value.basisQuotes[idx]) return
  const qty = parseFloat(value)
  form.value.basisQuotes[idx].availableQty = isNaN(qty) ? undefined : qty
}

// 更新基差合约参考价
function updateBasisQuoteRefPrice(idx: number, value: string) {
  if (!form.value.basisQuotes || !form.value.basisQuotes[idx]) return
  const price = parseFloat(value)
  form.value.basisQuotes[idx].referencePrice = isNaN(price) ? null : price
}

// 更新动态参数
function updateDynamicParam(key: string, value: string) {
  if (!form.value.dynamicParams) {
    form.value.dynamicParams = {}
  }
  form.value.dynamicParams[key] = value
}
</script>

<template>
  <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden flex flex-col h-full">
    <!-- 头部（固定） -->
    <div class="px-3 py-2 border-b border-gray-100 flex items-center bg-gray-50/50 shrink-0">
      <h3 class="text-sm font-bold text-gray-900 flex items-center gap-1.5">
        <Package class="w-4 h-4 text-brand-600" />
        {{ subjectType === 'SUPPLY' ? '供应详情' : '采购需求' }}
      </h3>
    </div>

    <!-- 表单内容（可滚动） -->
    <div class="flex-1 overflow-y-auto p-3 space-y-3">
      <!-- 品名（全宽） -->
      <div>
        <label class="block text-[10px] font-semibold text-gray-500 uppercase mb-1">品名</label>
        <input
          v-model="form.productName"
          :readonly="readonly"
          type="text"
          class="w-full h-8 px-2 text-sm rounded-lg border border-gray-200 bg-white
                 focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500
                 read-only:bg-gray-50 read-only:cursor-default font-medium"
          :placeholder="readonly ? '-' : '产品名称'"
        />
      </div>

      <!-- 一口价模式：数量 + 单位 + 单价 -->
      <div v-if="form.priceType !== 'BASIS'" class="grid grid-cols-3 gap-2">
        <div>
          <label class="block text-[10px] font-semibold text-gray-500 uppercase mb-1">数量</label>
          <input
            v-model.number="form.quantity"
            :readonly="readonly"
            type="number"
            min="0"
            class="w-full h-8 px-2 text-sm rounded-lg border border-gray-200 bg-white
                   focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500
                   read-only:bg-gray-50 read-only:cursor-default font-bold"
            placeholder="0"
          />
        </div>
        <div>
          <label class="block text-[10px] font-semibold text-gray-500 uppercase mb-1">单位</label>
          <select
            v-model="form.unit"
            :disabled="readonly"
            class="w-full h-8 px-2 text-sm rounded-lg border border-gray-200 bg-white
                   focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500
                   disabled:bg-gray-50 disabled:cursor-default"
          >
            <option v-for="unit in unitOptions" :key="unit" :value="unit">{{ unit }}</option>
          </select>
        </div>
        <div>
          <label class="block text-[10px] font-semibold text-gray-500 uppercase mb-1">单价(元)</label>
          <div class="relative">
            <span class="absolute left-2 top-1/2 -translate-y-1/2 text-xs text-gray-400">¥</span>
            <input
              v-model.number="form.price"
              :readonly="readonly"
              type="number"
              min="0"
              class="w-full h-8 pl-5 pr-2 text-sm rounded-lg border border-gray-200 bg-white
                     focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500
                     read-only:bg-gray-50 read-only:cursor-default font-bold"
              placeholder="0"
            />
          </div>
        </div>
      </div>

      <!-- 基差模式：出厂价 + 单位 + 合约明细表 -->
      <template v-if="form.priceType === 'BASIS'">
        <div class="grid grid-cols-2 gap-2">
          <div>
            <label class="block text-[10px] font-semibold text-gray-500 uppercase mb-1">出厂价(元)</label>
            <div class="relative">
              <span class="absolute left-2 top-1/2 -translate-y-1/2 text-xs text-gray-400">¥</span>
              <input
                v-model.number="form.price"
                :readonly="readonly"
                type="number"
                min="0"
                class="w-full h-8 pl-5 pr-2 text-sm rounded-lg border border-gray-200 bg-white
                       focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500
                       read-only:bg-gray-50 read-only:cursor-default font-bold"
                placeholder="0"
              />
            </div>
          </div>
          <div>
            <label class="block text-[10px] font-semibold text-gray-500 uppercase mb-1">单位</label>
            <select
              v-model="form.unit"
              :disabled="readonly"
              class="w-full h-8 px-2 text-sm rounded-lg border border-gray-200 bg-white
                     focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500
                     disabled:bg-gray-50 disabled:cursor-default"
            >
              <option v-for="unit in unitOptions" :key="unit" :value="unit">{{ unit }}</option>
            </select>
          </div>
        </div>

        <!-- 基差合约明细 -->
        <div v-if="form.basisQuotes && form.basisQuotes.length > 0">
          <label class="block text-[10px] font-semibold text-gray-500 uppercase mb-1">基差合约明细</label>
          <div class="border border-gray-200 rounded-lg overflow-hidden">
            <table class="w-full text-xs">
              <thead class="bg-gray-50 text-[10px] uppercase font-semibold text-gray-500">
                <tr>
                  <th class="px-2 py-1.5 text-left">合约</th>
                  <th class="px-2 py-1.5 text-right">基差</th>
                  <th class="px-2 py-1.5 text-right">参考价</th>
                  <th class="px-2 py-1.5 text-right">数量({{ form.unit }})</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-100">
                <tr v-for="(quote, idx) in form.basisQuotes" :key="idx">
                  <td class="px-2 py-1.5 font-bold text-gray-900">{{ quote.contractCode }}</td>
                  <td class="px-2 py-1.5 text-right font-mono font-bold"
                      :class="quote.basisPrice >= 0 ? 'text-red-600' : 'text-green-600'">
                    {{ quote.basisPrice >= 0 ? '+' : '' }}{{ quote.basisPrice }}
                  </td>
                  <td class="px-2 py-1.5 text-right">
                    <input
                      :value="quote.referencePrice ?? ''"
                      @input="updateBasisQuoteRefPrice(idx, ($event.target as HTMLInputElement).value)"
                      :readonly="readonly"
                      type="number"
                      min="0"
                      class="w-16 h-6 px-1 text-xs text-right rounded border border-gray-200 bg-white
                             font-mono font-bold text-brand-600
                             focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500
                             read-only:bg-gray-50 read-only:border-transparent"
                      placeholder="-"
                    />
                  </td>
                  <td class="px-2 py-1.5 text-right">
                    <input
                      :value="quote.availableQty ?? ''"
                      @input="updateBasisQuoteQty(idx, ($event.target as HTMLInputElement).value)"
                      :readonly="readonly"
                      type="number"
                      min="0"
                      class="w-16 h-6 px-1 text-xs text-right rounded border border-gray-200 bg-white
                             font-mono font-bold
                             focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500
                             read-only:bg-gray-50 read-only:border-transparent"
                      placeholder="-"
                    />
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </template>

      <!-- 包装 + 产地 -->
      <div class="grid grid-cols-2 gap-2">
        <div>
          <label class="block text-[10px] font-semibold text-gray-500 uppercase mb-1">
            <Settings class="w-3 h-3 inline mr-0.5" />包装
          </label>
          <select
            v-model="form.packaging"
            :disabled="readonly"
            class="w-full h-8 px-2 text-sm rounded-lg border border-gray-200 bg-white
                   focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500
                   disabled:bg-gray-50 disabled:cursor-default"
          >
            <option v-for="opt in packagingOptions" :key="opt" :value="opt">{{ opt }}</option>
          </select>
        </div>
        <div>
          <label class="block text-[10px] font-semibold text-gray-500 uppercase mb-1">产地</label>
          <input
            v-model="form.origin"
            :readonly="readonly"
            type="text"
            class="w-full h-8 px-2 text-sm rounded-lg border border-gray-200 bg-white
                   focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500
                   read-only:bg-gray-50 read-only:cursor-default"
            placeholder="产地"
          />
        </div>
      </div>

      <!-- 交货日期 + 交货地点 -->
      <div class="grid grid-cols-2 gap-2">
        <div>
          <label class="block text-[10px] font-semibold text-gray-500 uppercase mb-1">交货日期</label>
          <input
            v-model="form.deliveryDate"
            :readonly="readonly"
            type="date"
            class="w-full h-8 px-2 text-sm rounded-lg border border-gray-200 bg-white
                   focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500
                   read-only:bg-gray-50 read-only:cursor-default"
          />
        </div>
        <div>
          <label class="block text-[10px] font-semibold text-gray-500 uppercase mb-1">
            <Truck class="w-3 h-3 inline mr-0.5" />交货方式
          </label>
          <select
            v-model="form.deliveryMethod"
            :disabled="readonly"
            class="w-full h-8 px-2 text-sm rounded-lg border border-gray-200 bg-white
                   focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500
                   disabled:bg-gray-50 disabled:cursor-default"
          >
            <option v-for="opt in deliveryMethodOptions" :key="opt" :value="opt">{{ opt }}</option>
          </select>
        </div>
      </div>

      <!-- 交货地点 -->
      <div>
        <label class="block text-[10px] font-semibold text-gray-500 uppercase mb-1">
          <MapPin class="w-3 h-3 inline mr-0.5" />交货地点
        </label>
        <input
          v-model="form.deliveryPlace"
          :readonly="readonly"
          type="text"
          class="w-full h-8 px-2 text-sm rounded-lg border border-gray-200 bg-white
                 focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500
                 read-only:bg-gray-50 read-only:cursor-default"
          :placeholder="readonly ? '-' : '详细地址'"
        />
      </div>

      <!-- 付款方式 + 发票类型 -->
      <div class="grid grid-cols-2 gap-2">
        <div>
          <label class="block text-[10px] font-semibold text-gray-500 uppercase mb-1">
            <CreditCard class="w-3 h-3 inline mr-0.5" />付款方式
          </label>
          <select
            v-model="form.paymentMethod"
            :disabled="readonly"
            class="w-full h-8 px-2 text-sm rounded-lg border border-gray-200 bg-white
                   focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500
                   disabled:bg-gray-50 disabled:cursor-default"
          >
            <option v-for="opt in paymentOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
        <div>
          <label class="block text-[10px] font-semibold text-gray-500 uppercase mb-1">
            <FileText class="w-3 h-3 inline mr-0.5" />发票类型
          </label>
          <select
            v-model="form.invoiceType"
            :disabled="readonly"
            class="w-full h-8 px-2 text-sm rounded-lg border border-gray-200 bg-white
                   focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500
                   disabled:bg-gray-50 disabled:cursor-default"
          >
            <option v-for="opt in invoiceOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>
      </div>

      <!-- 动态参数（产品品类相关） -->
      <div v-if="dynamicParamsList.length > 0">
        <label class="block text-[10px] font-semibold text-gray-500 uppercase mb-2">品质参数</label>
        <div class="grid grid-cols-2 gap-2">
          <div v-for="param in dynamicParamsList" :key="param.key">
            <label class="block text-xs text-gray-500 mb-0.5">{{ param.key }}</label>
            <input
              :value="param.value"
              @input="updateDynamicParam(param.key, ($event.target as HTMLInputElement).value)"
              :readonly="readonly"
              type="text"
              class="w-full h-7 px-2 text-xs rounded border border-gray-200 bg-white
                     focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500
                     read-only:bg-gray-50"
            />
          </div>
        </div>
      </div>

      <!-- 备注 -->
      <div>
        <label class="block text-[10px] font-semibold text-gray-500 uppercase mb-1">备注</label>
        <textarea
          v-model="form.remark"
          :readonly="readonly"
          rows="2"
          class="w-full px-2 py-1.5 text-sm rounded-lg border border-gray-200 bg-white resize-none
                 focus:ring-1 focus:ring-brand-500/20 focus:border-brand-500
                 read-only:bg-gray-50 read-only:cursor-default"
          placeholder="其他说明..."
        />
      </div>
    </div>

    <!-- 底部发送报价按钮（固定） -->
    <div v-if="showSendButton && !readonly" class="px-3 py-2 bg-brand-50 border-t border-brand-100 shrink-0">
      <button
        @click="emit('send-quote', { ...form })"
        :disabled="sending || (form.priceType === 'BASIS' ? !(form.basisQuotes && form.basisQuotes.length > 0) : (!form.price || !form.quantity))"
        class="w-full px-4 py-2 bg-brand-600 hover:bg-brand-700 disabled:bg-gray-300
               disabled:cursor-not-allowed text-white text-sm font-bold rounded-lg
               flex items-center justify-center gap-1.5 transition-all active:scale-95"
      >
        <Send class="w-4 h-4" />
        {{ sending ? '发送中...' : '发送报价' }}
      </button>
    </div>
  </div>
</template>
