<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Coins, Wallet, RefreshCw, ArrowUpCircle, ArrowDownCircle, Clock, TrendingUp, TrendingDown, Minus } from 'lucide-vue-next'
import { getPointsMe, listPointsTx, rechargePoints, redeemPoints, type PointsTxResponse } from '../api/points'
import { BaseButton, EmptyState, Skeleton } from '../components/ui'

const loading = ref(false)
const creating = ref(false)
const me = ref<{ pointsBalance: number; cnyBalance: number } | null>(null)
const txs = ref<PointsTxResponse[]>([])

const rechargeVal = ref<number>(100)
const redeemVal = ref<number>(50)

const canRecharge = computed(() => Number.isFinite(rechargeVal.value) && rechargeVal.value > 0)
const canRedeem = computed(() => Number.isFinite(redeemVal.value) && redeemVal.value > 0 && (me.value?.pointsBalance ?? 0) >= redeemVal.value)

// 格式化数字
function formatNumber(num: number | undefined): string {
  if (num == null) return '0'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 格式化时间
function formatTime(timeStr: string): string {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取交易类型信息
function getTxTypeInfo(txType: string) {
  const types: Record<string, { label: string; color: string; icon: any }> = {
    RECHARGE: { label: '充值', color: 'emerald', icon: ArrowUpCircle },
    REDEEM: { label: '兑换', color: 'blue', icon: ArrowDownCircle },
    GIFT: { label: '赠送', color: 'purple', icon: Coins },
    CONSUME: { label: '消费', color: 'amber', icon: Wallet }
  }
  return types[txType] || { label: txType, color: 'gray', icon: Coins }
}

// 获取变动颜色
function getDeltaColor(delta: number): string {
  if (delta > 0) return 'text-emerald-600'
  if (delta < 0) return 'text-red-500'
  return 'text-gray-400'
}

// 格式化变动
function formatDelta(delta: number): string {
  if (delta > 0) return `+${formatNumber(delta)}`
  return formatNumber(delta)
}

async function refresh() {
  loading.value = true
  try {
    const r1 = await getPointsMe()
    if (r1.code !== 0) throw new Error(r1.message)
    me.value = r1.data ?? null

    const r2 = await listPointsTx()
    if (r2.code !== 0) throw new Error(r2.message)
    txs.value = r2.data ?? []
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

async function onRecharge() {
  if (!canRecharge.value) return
  creating.value = true
  try {
    const r = await rechargePoints(rechargeVal.value)
    if (r.code !== 0) throw new Error(r.message)
    me.value = r.data ?? me.value
    ElMessage.success('充值成功')
    await refresh()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '充值失败')
  } finally {
    creating.value = false
  }
}

async function onRedeem() {
  if (!canRedeem.value) return
  creating.value = true
  try {
    const r = await redeemPoints(redeemVal.value)
    if (r.code !== 0) throw new Error(r.message)
    me.value = r.data ?? me.value
    ElMessage.success('兑换成功')
    await refresh()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '兑换失败')
  } finally {
    creating.value = false
  }
}

onMounted(refresh)
</script>

<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">我的积分</h1>
        <p class="text-sm text-gray-500 mt-1">管理您的积分余额、充值与兑换</p>
      </div>
      <div class="flex items-center gap-3">
        <span class="px-3 py-1.5 bg-emerald-50 text-emerald-700 text-xs font-bold rounded-full border border-emerald-100">
          兑换比例：1 积分 = 1 元
        </span>
        <BaseButton type="secondary" size="sm" :loading="loading" @click="refresh">
          <RefreshCw class="w-4 h-4" />
          刷新
        </BaseButton>
      </div>
    </div>

    <!-- 余额卡片区 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <!-- 积分余额 -->
      <div class="bg-gradient-to-br from-emerald-500 to-emerald-600 rounded-2xl p-6 text-white shadow-lg shadow-emerald-100 hover-card animate-stagger-in">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-emerald-100 text-xs font-bold uppercase tracking-widest mb-2">积分余额</p>
            <template v-if="loading && !me">
              <Skeleton type="title" width="120px" height="36px" />
            </template>
            <template v-else>
              <p class="text-4xl font-black count-up">{{ formatNumber(me?.pointsBalance ?? 0) }}</p>
            </template>
            <p class="text-emerald-100 text-sm mt-2">可用于平台服务消费</p>
          </div>
          <div class="w-14 h-14 rounded-2xl bg-white/20 flex items-center justify-center">
            <Coins class="w-7 h-7 text-white" />
          </div>
        </div>
      </div>

      <!-- 人民币余额 -->
      <div class="bg-gradient-to-br from-slate-700 to-slate-800 rounded-2xl p-6 text-white shadow-lg shadow-slate-100 hover-card animate-stagger-in">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-slate-300 text-xs font-bold uppercase tracking-widest mb-2">人民币余额</p>
            <template v-if="loading && !me">
              <Skeleton type="title" width="120px" height="36px" />
            </template>
            <template v-else>
              <p class="text-4xl font-black count-up">¥{{ formatNumber(me?.cnyBalance ?? 0) }}</p>
            </template>
            <p class="text-slate-300 text-sm mt-2">积分兑换所得</p>
          </div>
          <div class="w-14 h-14 rounded-2xl bg-white/10 flex items-center justify-center">
            <Wallet class="w-7 h-7 text-white" />
          </div>
        </div>
      </div>
    </div>

    <!-- 操作区 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <!-- 充值卡片 -->
      <div class="bg-white rounded-2xl border border-gray-100 p-6 hover-card animate-stagger-in">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center">
            <ArrowUpCircle class="w-5 h-5 text-emerald-600" />
          </div>
          <div>
            <h3 class="font-bold text-gray-900">充值积分</h3>
            <p class="text-xs text-gray-500">1元 = 1积分</p>
          </div>
        </div>
        
        <div class="space-y-4">
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
              充值金额（元）
            </label>
            <input
              v-model.number="rechargeVal"
              type="number"
              min="1"
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-lg font-bold text-center"
              placeholder="请输入金额"
            />
          </div>
          
          <!-- 快捷金额 -->
          <div class="flex flex-wrap gap-2">
            <button
              v-for="amount in [50, 100, 200, 500]"
              :key="amount"
              :class="[
                'px-4 py-1.5 rounded-lg text-sm font-bold transition-all',
                rechargeVal === amount 
                  ? 'bg-emerald-600 text-white' 
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              ]"
              @click="rechargeVal = amount"
            >
              {{ amount }}元
            </button>
          </div>
          
          <BaseButton 
            type="primary" 
            block
            :disabled="!canRecharge" 
            :loading="creating" 
            @click="onRecharge"
          >
            <ArrowUpCircle class="w-4 h-4" />
            立即充值
          </BaseButton>
        </div>
      </div>

      <!-- 兑换卡片 -->
      <div class="bg-white rounded-2xl border border-gray-100 p-6 hover-card animate-stagger-in">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center">
            <ArrowDownCircle class="w-5 h-5 text-blue-600" />
          </div>
          <div>
            <h3 class="font-bold text-gray-900">兑换人民币</h3>
            <p class="text-xs text-gray-500">1积分 = 1元</p>
          </div>
        </div>
        
        <div class="space-y-4">
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
              兑换积分数量
            </label>
            <input
              v-model.number="redeemVal"
              type="number"
              min="1"
              :max="me?.pointsBalance ?? 0"
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-blue-500 outline-none transition-all text-lg font-bold text-center"
              placeholder="请输入积分数量"
            />
          </div>
          
          <div class="flex items-center justify-between text-sm">
            <span class="text-gray-500">可兑换积分</span>
            <span class="font-bold text-gray-900">{{ formatNumber(me?.pointsBalance ?? 0) }}</span>
          </div>
          
          <BaseButton 
            type="outline" 
            block
            :disabled="!canRedeem" 
            :loading="creating" 
            @click="onRedeem"
          >
            <ArrowDownCircle class="w-4 h-4" />
            兑换为人民币
          </BaseButton>
          
          <p v-if="!canRedeem && redeemVal > (me?.pointsBalance ?? 0)" class="text-xs text-red-500 text-center">
            积分余额不足
          </p>
        </div>
      </div>
    </div>

    <!-- 流水记录 -->
    <div class="bg-white rounded-2xl border border-gray-100 overflow-hidden animate-stagger-in">
      <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <div class="w-1.5 h-5 bg-emerald-500 rounded-full"></div>
          <h3 class="font-bold text-gray-900">交易记录</h3>
          <span v-if="txs.length > 0" class="text-xs text-gray-400">
            共 {{ txs.length }} 条
          </span>
        </div>
        <Clock class="w-4 h-4 text-gray-400" />
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading && txs.length === 0" class="p-6 space-y-4">
        <Skeleton type="card" />
        <Skeleton type="card" />
      </div>
      
      <!-- 空状态 -->
      <EmptyState 
        v-else-if="txs.length === 0" 
        type="data"
        title="暂无交易记录"
        description="充值或兑换后，交易记录将显示在这里"
        size="sm"
      />
      
      <!-- 交易列表 -->
      <div v-else class="divide-y divide-gray-50">
        <div 
          v-for="(tx, index) in txs" 
          :key="tx.id"
          class="px-6 py-4 hover:bg-gray-50 transition-colors animate-stagger-in"
          :style="{ animationDelay: `${index * 30}ms` }"
        >
          <div class="flex items-center gap-4">
            <!-- 类型图标 -->
            <div 
              :class="[
                'w-10 h-10 rounded-xl flex items-center justify-center',
                `bg-${getTxTypeInfo(tx.txType).color}-50`
              ]"
            >
              <component 
                :is="getTxTypeInfo(tx.txType).icon" 
                :class="['w-5 h-5', `text-${getTxTypeInfo(tx.txType).color}-600`]"
              />
            </div>
            
            <!-- 信息 -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2">
                <span class="font-bold text-gray-900">{{ getTxTypeInfo(tx.txType).label }}</span>
                <span 
                  :class="[
                    'px-2 py-0.5 rounded-full text-[10px] font-bold uppercase',
                    `bg-${getTxTypeInfo(tx.txType).color}-50 text-${getTxTypeInfo(tx.txType).color}-600`
                  ]"
                >
                  #{{ tx.id }}
                </span>
              </div>
              <p class="text-sm text-gray-500 truncate">{{ tx.remark || '无备注' }}</p>
            </div>
            
            <!-- 变动 -->
            <div class="text-right">
              <div v-if="tx.pointsDelta !== 0" class="flex items-center gap-1 justify-end">
                <TrendingUp v-if="tx.pointsDelta > 0" class="w-4 h-4 text-emerald-500" />
                <TrendingDown v-else-if="tx.pointsDelta < 0" class="w-4 h-4 text-red-500" />
                <Minus v-else class="w-4 h-4 text-gray-400" />
                <span :class="['font-bold', getDeltaColor(tx.pointsDelta)]">
                  {{ formatDelta(tx.pointsDelta) }} 积分
                </span>
              </div>
              <div v-if="tx.cnyDelta !== 0" class="flex items-center gap-1 justify-end mt-0.5">
                <TrendingUp v-if="tx.cnyDelta > 0" class="w-4 h-4 text-emerald-500" />
                <TrendingDown v-else-if="tx.cnyDelta < 0" class="w-4 h-4 text-red-500" />
                <span :class="['text-sm', getDeltaColor(tx.cnyDelta)]">
                  {{ formatDelta(tx.cnyDelta) }} 元
                </span>
              </div>
              <p class="text-xs text-gray-400 mt-1">{{ formatTime(tx.createTime) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 动态颜色类 - Tailwind 无法动态生成，需要手动定义 */
.bg-emerald-50 { background-color: rgb(236 253 245); }
.bg-blue-50 { background-color: rgb(239 246 255); }
.bg-purple-50 { background-color: rgb(250 245 255); }
.bg-amber-50 { background-color: rgb(255 251 235); }
.bg-gray-50 { background-color: rgb(249 250 251); }

.text-emerald-600 { color: rgb(5 150 105); }
.text-blue-600 { color: rgb(37 99 235); }
.text-purple-600 { color: rgb(147 51 234); }
.text-amber-600 { color: rgb(217 119 6); }
.text-gray-600 { color: rgb(75 85 99); }
</style>
