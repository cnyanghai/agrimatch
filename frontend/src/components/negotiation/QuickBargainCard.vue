<script setup lang="ts">
/**
 * QuickBargainCard - 快速议价卡片
 * 借鉴淘宝议价模式：简洁、结构化、一键操作
 *
 * 核心思路：
 * 1. 只展示关键信息：产品名、当前价格、数量
 * 2. 快速调价按钮：-5%, -3%, -1%, +1%, +3%, +5%
 * 3. 明确的操作按钮：发送报价、接受、拒绝
 * 4. 清晰的状态显示
 */
import { ref, computed, watch } from 'vue'
import {
  Package,
  TrendingDown,
  TrendingUp,
  Send,
  Check,
  X,
  Clock,
  ArrowRight
} from 'lucide-vue-next'

export interface BargainState {
  productName: string
  productGrade?: string
  /** 原始/挂牌价 */
  listPrice: number
  /** 当前报价 */
  currentPrice: number
  /** 数量 */
  quantity: number
  unit: string
  /** 议价状态 */
  status: 'IDLE' | 'MY_TURN' | 'PEER_TURN' | 'ACCEPTED' | 'REJECTED'
  /** 最后报价方 */
  lastQuoteBy?: 'ME' | 'PEER'
  /** 对方上次报价 */
  peerLastPrice?: number
}

const props = defineProps<{
  state: BargainState
  sending?: boolean
}>()

const emit = defineEmits<{
  (e: 'submit', price: number): void
  (e: 'accept'): void
  (e: 'reject'): void
}>()

// 本地编辑的价格
const editPrice = ref(props.state.currentPrice)

// 同步外部状态
watch(() => props.state.currentPrice, (val) => {
  editPrice.value = val
})

// 快捷调整按钮
const quickAdjustments = [
  { label: '-5%', value: -0.05, color: 'text-red-600 bg-red-50 hover:bg-red-100' },
  { label: '-3%', value: -0.03, color: 'text-red-500 bg-red-50 hover:bg-red-100' },
  { label: '-1%', value: -0.01, color: 'text-orange-500 bg-orange-50 hover:bg-orange-100' },
  { label: '+1%', value: 0.01, color: 'text-brand-500 bg-brand-50 hover:bg-brand-100' },
  { label: '+3%', value: 0.03, color: 'text-brand-600 bg-brand-50 hover:bg-brand-100' },
  { label: '+5%', value: 0.05, color: 'text-brand-700 bg-brand-50 hover:bg-brand-100' },
]

// 应用快捷调整
function applyAdjustment(pct: number) {
  const base = props.state.peerLastPrice || props.state.listPrice
  editPrice.value = Math.round(base * (1 + pct) * 100) / 100
}

// 重置为原价
function resetPrice() {
  editPrice.value = props.state.listPrice
}

// 价格变化量
const priceChange = computed(() => {
  const base = props.state.peerLastPrice || props.state.listPrice
  return editPrice.value - base
})

const priceChangePercent = computed(() => {
  const base = props.state.peerLastPrice || props.state.listPrice
  if (!base) return 0
  return ((editPrice.value - base) / base * 100).toFixed(1)
})

// 总金额
const totalAmount = computed(() => {
  return editPrice.value * props.state.quantity
})

// 是否可以发送
const canSubmit = computed(() => {
  return editPrice.value > 0 && editPrice.value !== props.state.currentPrice
})

// 状态配置
const statusConfig = computed(() => {
  const configs: Record<string, { label: string; color: string; icon: any }> = {
    IDLE: { label: '待报价', color: 'text-gray-500 bg-gray-100', icon: Clock },
    MY_TURN: { label: '等待我方报价', color: 'text-amber-600 bg-amber-100', icon: Clock },
    PEER_TURN: { label: '等待对方回复', color: 'text-blue-600 bg-blue-100', icon: Clock },
    ACCEPTED: { label: '已成交', color: 'text-green-600 bg-green-100', icon: Check },
    REJECTED: { label: '已拒绝', color: 'text-red-600 bg-red-100', icon: X },
  }
  return configs[props.state.status] || configs.IDLE
})

// 格式化金额
function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount)
}
</script>

<template>
  <div class="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
    <!-- 头部：产品信息 + 状态 -->
    <div class="px-4 py-3 bg-gray-50 border-b border-gray-100 flex items-center justify-between">
      <div class="flex items-center gap-2">
        <Package class="w-5 h-5 text-brand-600" />
        <div>
          <span class="font-bold text-gray-900">{{ state.productName }}</span>
          <span v-if="state.productGrade" class="ml-2 text-xs text-gray-500 bg-gray-200 px-1.5 py-0.5 rounded">
            {{ state.productGrade }}
          </span>
        </div>
      </div>
      <div :class="['px-2.5 py-1 rounded-full text-xs font-bold flex items-center gap-1', statusConfig.color]">
        <component :is="statusConfig.icon" class="w-3 h-3" />
        {{ statusConfig.label }}
      </div>
    </div>

    <!-- 价格区域 -->
    <div class="p-4">
      <!-- 价格对比 -->
      <div class="flex items-center justify-between mb-4">
        <!-- 挂牌价 -->
        <div class="text-center">
          <div class="text-[10px] text-gray-400 uppercase">挂牌价</div>
          <div class="text-lg font-mono text-gray-400 line-through">
            ¥{{ formatCurrency(state.listPrice) }}
          </div>
        </div>

        <ArrowRight class="w-5 h-5 text-gray-300" />

        <!-- 当前报价输入 -->
        <div class="text-center flex-1 max-w-[160px]">
          <div class="text-[10px] text-gray-500 uppercase mb-1">我的报价</div>
          <div class="relative">
            <span class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 font-bold">¥</span>
            <input
              v-model.number="editPrice"
              type="number"
              step="0.01"
              :disabled="state.status === 'PEER_TURN' || state.status === 'ACCEPTED'"
              class="w-full h-12 pl-8 pr-3 text-xl font-bold text-center border-2 border-brand-200 rounded-xl
                     focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20
                     disabled:bg-gray-50 disabled:text-gray-400"
            />
          </div>
          <!-- 变化指示 -->
          <div
            v-if="priceChange !== 0"
            :class="['mt-1 text-xs font-bold flex items-center justify-center gap-1',
                     priceChange < 0 ? 'text-red-500' : 'text-brand-600']"
          >
            <component :is="priceChange < 0 ? TrendingDown : TrendingUp" class="w-3 h-3" />
            {{ priceChange > 0 ? '+' : '' }}{{ formatCurrency(priceChange) }}
            ({{ priceChangePercent }}%)
          </div>
        </div>

        <!-- 对方报价（如果有） -->
        <div v-if="state.peerLastPrice" class="text-center">
          <div class="text-[10px] text-gray-400 uppercase">对方报价</div>
          <div class="text-lg font-mono text-gray-700 font-bold">
            ¥{{ formatCurrency(state.peerLastPrice) }}
          </div>
        </div>
      </div>

      <!-- 快捷调整按钮 -->
      <div v-if="state.status !== 'ACCEPTED' && state.status !== 'PEER_TURN'" class="mb-4">
        <div class="text-[10px] text-gray-400 uppercase mb-2">快速调整</div>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="adj in quickAdjustments"
            :key="adj.label"
            @click="applyAdjustment(adj.value)"
            :class="['px-3 py-1.5 text-xs font-bold rounded-lg transition-all active:scale-95', adj.color]"
          >
            {{ adj.label }}
          </button>
          <button
            @click="resetPrice"
            class="px-3 py-1.5 text-xs font-medium text-gray-500 bg-gray-100 rounded-lg hover:bg-gray-200"
          >
            重置
          </button>
        </div>
      </div>

      <!-- 数量和总额 -->
      <div class="flex items-center justify-between py-3 border-t border-gray-100">
        <div class="text-sm text-gray-600">
          数量: <span class="font-bold">{{ state.quantity }} {{ state.unit }}</span>
        </div>
        <div class="text-right">
          <div class="text-[10px] text-gray-400">预计总额</div>
          <div class="text-xl font-bold text-brand-600">¥{{ formatCurrency(totalAmount) }}</div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="px-4 py-3 bg-gray-50 border-t border-gray-100 flex gap-2">
      <!-- 我方回合：发送报价 -->
      <template v-if="state.status === 'IDLE' || state.status === 'MY_TURN'">
        <button
          @click="emit('submit', editPrice)"
          :disabled="!canSubmit || sending"
          class="flex-1 h-11 bg-brand-600 hover:bg-brand-700 disabled:bg-gray-300
                 text-white font-bold rounded-xl flex items-center justify-center gap-2
                 transition-all active:scale-[0.98]"
        >
          <Send class="w-4 h-4" />
          {{ sending ? '发送中...' : '发送报价' }}
        </button>
      </template>

      <!-- 对方回合：等待中 -->
      <template v-else-if="state.status === 'PEER_TURN'">
        <div class="flex-1 h-11 bg-gray-100 text-gray-500 font-medium rounded-xl
                    flex items-center justify-center gap-2">
          <Clock class="w-4 h-4 animate-pulse" />
          等待对方回复...
        </div>
      </template>

      <!-- 收到对方报价：接受/拒绝/还价 -->
      <template v-else-if="state.lastQuoteBy === 'PEER' && state.status !== 'ACCEPTED'">
        <button
          @click="emit('accept')"
          class="flex-1 h-11 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-xl
                 flex items-center justify-center gap-2 transition-all active:scale-[0.98]"
        >
          <Check class="w-4 h-4" />
          接受
        </button>
        <button
          @click="emit('submit', editPrice)"
          :disabled="!canSubmit || sending"
          class="flex-1 h-11 bg-amber-500 hover:bg-amber-600 disabled:bg-gray-300
                 text-white font-bold rounded-xl flex items-center justify-center gap-2
                 transition-all active:scale-[0.98]"
        >
          还价
        </button>
        <button
          @click="emit('reject')"
          class="h-11 px-4 bg-gray-100 hover:bg-gray-200 text-gray-600 font-medium rounded-xl
                 flex items-center justify-center transition-all"
        >
          <X class="w-4 h-4" />
        </button>
      </template>

      <!-- 已成交 -->
      <template v-else-if="state.status === 'ACCEPTED'">
        <div class="flex-1 h-11 bg-green-100 text-green-700 font-bold rounded-xl
                    flex items-center justify-center gap-2">
          <Check class="w-5 h-5" />
          已成交，可生成合同
        </div>
      </template>
    </div>
  </div>
</template>
