<script setup lang="ts">
import { computed, reactive, watch, ref, onMounted } from 'vue'
import { listFuturesContracts, type FuturesContractResponse } from '../../api/futures'
import { Loading, InfoFilled } from '@element-plus/icons-vue'
import { parseProductParams } from '../../utils/chat/paramsParser'

const props = defineProps<{
  disabled?: boolean
  peerLatestBasis?: { basisPrice?: number; contractCode?: string; quantity?: string } | null
  subjectSnapshotJson?: string | null
}>()

const emit = defineEmits<{
  (e: 'send', payload: { msgType: 'QUOTE'; payload: any; summary: string; basisPrice?: number; contractCode?: string }): void
}>()

const contracts = ref<FuturesContractResponse[]>([])
const loadingContracts = ref(false)
const selectedContract = ref<FuturesContractResponse | null>(null)
const productParams = ref<Record<string, string>>({})

const form = reactive({
  contractCode: '',
  basisPrice: undefined as number | undefined,
  quantity: '',
  remark: ''
})

// 解析标的快照
function initFromSnapshot() {
  if (!props.subjectSnapshotJson) return
  try {
    const s = JSON.parse(props.subjectSnapshotJson)
    // 如果是基差报价，快照中应该有 basisQuotes
    if (s.priceType === 1 && s.basisQuotes && s.basisQuotes.length > 0) {
      // 默认选择第一个合约
      const first = s.basisQuotes[0]
      form.contractCode = first.contractCode
      form.basisPrice = first.basisPrice
      form.quantity = String(first.availableQty || '')
    } else {
      // 现货转基差？或者普通报价
      form.quantity = String(s.remainingQuantity ?? s.quantity ?? '')
    }

    // 使用统一的参数解析工具
    if (s.paramsJson) {
      productParams.value = parseProductParams(s.paramsJson)
    }
  } catch (e) {
    console.error('Failed to parse subject snapshot', e)
  }
}

async function loadContracts() {
  loadingContracts.value = true
  try {
    // 尝试从快照中识别品种
    let productCode = undefined
    if (props.subjectSnapshotJson) {
      const s = JSON.parse(props.subjectSnapshotJson)
      const name = s.categoryName || s.productName || s.title || ''
      if (name.includes('豆粕')) productCode = 'M'
      else if (name.includes('菜粕')) productCode = 'RM'
      else if (name.includes('豆油')) productCode = 'Y'
      else if (name.includes('菜油')) productCode = 'OI'
    }

    const res = await listFuturesContracts(productCode)
    if (res.code === 0) {
      contracts.value = res.data || []
      updateSelectedContract()
    }
  } finally {
    loadingContracts.value = false
  }
}

function updateSelectedContract() {
  if (!form.contractCode) {
    selectedContract.value = null
    return
  }
  selectedContract.value = contracts.value.find(c => c.contractCode === form.contractCode) || null
}

watch(() => form.contractCode, updateSelectedContract)

watch(() => props.subjectSnapshotJson, () => {
  initFromSnapshot()
}, { immediate: true })

onMounted(() => {
  loadContracts()
})

const currentFuturesPrice = computed(() => {
  if (!selectedContract.value) return 0
  return selectedContract.value.lastPrice || selectedContract.value.prevClose || 0
})

const referencePrice = computed(() => {
  if (form.basisPrice === undefined) return currentFuturesPrice.value
  return currentFuturesPrice.value + form.basisPrice
})

const canSend = computed(() => {
  return !!(form.contractCode && form.basisPrice !== undefined && form.quantity)
})

function send() {
  if (!canSend.value) return
  
  const summary = `${selectedContract.value?.contractName || form.contractCode} | 基差 ${form.basisPrice! > 0 ? '+' : ''}${form.basisPrice} | 核算价 ¥${referencePrice.value.toFixed(2)}`
  
  const payload = {
    version: 1,
    kind: 'BASIS_QUOTE_V1',
    createdAt: new Date().toISOString(),
    fields: {
      contractCode: form.contractCode,
      contractName: selectedContract.value?.contractName,
      basisPrice: form.basisPrice,
      futuresPrice: currentFuturesPrice.value,
      referencePrice: referencePrice.value,
      quantity: form.quantity,
      remark: form.remark
    }
  }
  
  emit('send', { 
    msgType: 'QUOTE', 
    payload, 
    summary,
    basisPrice: form.basisPrice,
    contractCode: form.contractCode
  })
}

function clear() {
  initFromSnapshot()
}
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-200 p-6 shadow-2xl">
    <!-- 头部 -->
    <div class="flex items-center justify-between mb-6">
      <div>
        <div class="text-[10px] font-bold uppercase tracking-widest text-brand-600">Basis Trading</div>
        <div class="mt-0.5 font-bold text-gray-900 text-lg">基差交易出价单</div>
      </div>
      <div class="flex items-center gap-2">
        <button
          class="px-4 py-2 rounded-full bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-bold transition-all  disabled:opacity-50"
          :disabled="disabled"
          @click="clear"
        >
          重置
        </button>
        <button
          class="px-6 py-2 rounded-full bg-brand-600 hover:bg-brand-700 text-white text-sm font-bold shadow-md shadow-brand-100 transition-all  disabled:opacity-50"
          :disabled="disabled || !canSend"
          @click="send"
        >
          发送议价
        </button>
      </div>
    </div>

    <!-- 产品规格参数 -->
    <div v-if="Object.keys(productParams).length > 0" class="mb-6 p-4 bg-gray-50 rounded-lg border border-gray-200">
      <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-3 flex items-center gap-1">
        产品规格参数 <InfoFilled class="w-3 h-3" />
      </div>
      <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
        <div v-for="(v, k) in productParams" :key="k" class="bg-white px-3 py-2 rounded-lg border border-gray-50 flex flex-col gap-0.5">
          <span class="text-[10px] text-gray-400 font-medium">{{ k }}</span>
          <span class="text-xs font-bold text-gray-700 truncate">{{ v }}</span>
        </div>
      </div>
    </div>

    <!-- 期货盘面状态 -->
    <div v-if="selectedContract" class="mb-6 p-4 bg-slate-900 rounded-lg text-white">
      <div class="flex items-center justify-between mb-3">
        <div class="flex items-center gap-2">
          <span class="text-xs font-bold text-slate-400">当前盘面：</span>
          <span class="text-sm font-bold">{{ selectedContract.contractName }} ({{ selectedContract.contractCode }})</span>
        </div>
        <div class="flex items-center gap-1.5">
          <div class="w-2 h-2 rounded-full" :class="selectedContract.isTrading ? 'bg-brand-500 animate-pulse' : 'bg-slate-500'"></div>
          <span class="text-[10px] font-bold uppercase tracking-tight text-slate-400">
            {{ selectedContract.isTrading ? '交易中' : '已休盘' }}
          </span>
        </div>
      </div>
      
      <div class="flex items-end justify-between">
        <div class="flex items-baseline gap-1">
          <span class="text-3xl font-black tabular-nums">{{ currentFuturesPrice }}</span>
          <span class="text-xs text-slate-400">元/吨</span>
        </div>
        <div class="text-right">
          <div class="text-[10px] text-slate-500 mb-0.5">价格更新时间</div>
          <div class="text-[10px] font-medium text-slate-300">{{ selectedContract.priceUpdateTime || '-' }}</div>
        </div>
      </div>
    </div>

    <!-- 议价输入区 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- 合约选择 -->
      <div class="space-y-2">
        <label class="text-[10px] font-bold text-gray-400 uppercase tracking-widest flex items-center gap-1">
          选择期货合约 <Loading v-if="loadingContracts" class="w-3 h-3 animate-spin" />
        </label>
        <select 
          v-model="form.contractCode" 
          :disabled="disabled"
          class="w-full bg-gray-50 border-2 border-gray-200 rounded-lg px-4 py-2.5 text-sm font-bold focus:border-brand-500 focus:bg-white outline-none transition-all"
        >
          <option value="" disabled>请选择合约</option>
          <option v-for="c in contracts" :key="c.contractCode" :value="c.contractCode">
            {{ c.contractName }} ({{ c.contractCode }})
          </option>
        </select>
      </div>

      <!-- 基差输入 -->
      <div class="space-y-2">
        <label class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">出价基差 (元/吨)</label>
        <div class="relative">
          <input 
            v-model.number="form.basisPrice" 
            type="number"
            :disabled="disabled"
            class="w-full border-2 border-gray-200 rounded-lg px-4 py-2.5 text-sm font-bold focus:border-brand-500 outline-none transition-all"
            :class="[
              form.basisPrice && form.basisPrice > 0 ? 'text-rose-600' : 
              form.basisPrice && form.basisPrice < 0 ? 'text-brand-600' : 'text-gray-900'
            ]"
            placeholder="如：+80 或 -20"
          />
          <div class="absolute right-3 top-1/2 -translate-y-1/2 text-[10px] font-bold text-gray-400">
            {{ form.basisPrice && form.basisPrice > 0 ? '升水' : form.basisPrice && form.basisPrice < 0 ? '贴水' : '平水' }}
          </div>
        </div>
      </div>

      <!-- 数量输入 -->
      <div class="space-y-2">
        <label class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">采购数量 (吨)</label>
        <input 
          v-model="form.quantity" 
          :disabled="disabled"
          class="w-full border-2 border-gray-200 rounded-lg px-4 py-2.5 text-sm font-bold focus:border-brand-500 outline-none transition-all"
          placeholder="请输入吨数"
        />
      </div>

      <!-- 核算价格预览 -->
      <div class="space-y-2">
        <label class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">实时核算价 (元/吨)</label>
        <div class="w-full bg-brand-50 border-2 border-brand-100 rounded-lg px-4 py-2.5 flex items-center justify-between">
          <span class="text-sm font-black text-brand-700">¥ {{ referencePrice.toFixed(2) }}</span>
          <el-tooltip content="核算价格 = 期货最新价 + 基差" placement="top">
            <el-icon class="text-brand-400 cursor-help"><InfoFilled /></el-icon>
          </el-tooltip>
        </div>
      </div>

      <!-- 备注信息 -->
      <div class="md:col-span-2 space-y-2">
        <label class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">补充说明</label>
        <textarea 
          v-model="form.remark" 
          :disabled="disabled"
          rows="2"
          class="w-full border-2 border-gray-200 rounded-lg px-4 py-2.5 text-sm focus:border-brand-500 outline-none transition-all resize-none"
          placeholder="如有其他特殊约定请说明..."
        ></textarea>
      </div>
    </div>

    <!-- 风险提示 -->
    <div class="mt-6 p-3 bg-amber-50 rounded-lg border border-amber-100 flex items-start gap-2">
      <el-icon class="text-amber-500 shrink-0 mt-0.5"><InfoFilled /></el-icon>
      <p class="text-[10px] text-amber-700 leading-relaxed">
        提示：基差交易的最终成交价受期货盘面波动影响。当前的“实时核算价”仅供参考，实际成交价格将以签署合同时点的期货盘面价为准。
      </p>
    </div>
  </div>
</template>

<style scoped>
input::-webkit-outer-spin-button,
input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
input[type=number] {
  -moz-appearance: textfield;
}
</style>

