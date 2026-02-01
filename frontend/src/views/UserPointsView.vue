<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Coins, Wallet, RefreshCw, ArrowUpCircle, ArrowDownCircle, Clock, TrendingUp, TrendingDown, Minus, CreditCard, Copy, Check, X, Crown, ShieldCheck, Package } from 'lucide-vue-next'
import { getPointsMe, listPointsTx, rechargePoints, redeemJdCard, listMyJdRedeems, type PointsTxResponse, type JdRedeemDetailResponse } from '../api/points'
import { BaseButton, EmptyState, Skeleton } from '../components/ui'

const loading = ref(false)
const creating = ref(false)
const me = ref<{ pointsBalance: number; cnyBalance: number } | null>(null)
const txs = ref<PointsTxResponse[]>([])
const myRedeems = ref<JdRedeemDetailResponse[]>([])

// ================= 充值相关 =================
const rechargeAmounts = [100, 500, 1000, 5000, 10000]
const rechargeVal = ref<number>(100)
const payChannel = ref<'wechat' | 'alipay'>('wechat')
const showPayQrDialog = ref(false)
const payQrCode = ref('')
const payOrderNo = ref('')
const payPollingTimer = ref<ReturnType<typeof setInterval> | null>(null)
const payStatus = ref<'pending' | 'success' | 'failed'>('pending')

// ================= 兑换相关 =================
const jdCardFaces = [500, 1000, 2000, 5000]
const selectedJdFace = ref<number>(1000)
const showRedeemDialog = ref(false)
const redeemStep = ref<'confirm' | 'result'>('confirm')
const cardCodeCopied = ref(false)

// ================= 限额信息 =================
const limits = ref({
  rechargeMax: 10000,
  rechargeDayMax: 50000,
  redeemMax: 5000,
  redeemDayMax: 10000
})

const canRecharge = computed(() =>
  Number.isFinite(rechargeVal.value) &&
  rechargeVal.value > 0 &&
  rechargeVal.value <= limits.value.rechargeMax
)

const canRedeem = computed(() =>
  selectedJdFace.value > 0 &&
  (me.value?.pointsBalance ?? 0) >= selectedJdFace.value
)

// 格式化数字
function formatNumber(num: number | undefined): string {
  if (num == null) return '0'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 格式化时间
function formatTime(timeStr?: string): string {
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
    JD_REDEEM: { label: '京东卡', color: 'red', icon: CreditCard },
    GIFT: { label: '赠送', color: 'purple', icon: Coins },
    CONSUME: { label: '消费', color: 'amber', icon: Wallet },
    POST_INCOME: { label: '退款', color: 'emerald', icon: ArrowUpCircle }
  }
  return types[txType] || { label: txType, color: 'gray', icon: Coins }
}

// 获取变动颜色
function getDeltaColor(delta: number): string {
  if (delta > 0) return 'text-brand-600'
  if (delta < 0) return 'text-red-500'
  return 'text-neutral-400'
}

// 格式化变动
function formatDelta(delta: number): string {
  if (delta > 0) return `+${formatNumber(delta)}`
  return formatNumber(delta)
}

// 获取兑换状态信息
function getRedeemStatusInfo(status: number) {
  switch (status) {
    case 0: return { label: '待发卡', color: 'bg-warning-100 text-warning-700' }
    case 1: return { label: '已发卡', color: 'bg-brand-100 text-brand-700' }
    case 2: return { label: '已失败', color: 'bg-red-100 text-red-700' }
    default: return { label: '未知', color: 'bg-neutral-100 text-neutral-700' }
  }
}

// 复制卡密
function copyCardCode(code: string) {
  navigator.clipboard.writeText(code)
  cardCodeCopied.value = true
  ElMessage.success('卡密已复制')
  setTimeout(() => { cardCodeCopied.value = false }, 3000)
}

async function refresh() {
  loading.value = true
  try {
    const [r1, r2, r3] = await Promise.all([
      getPointsMe(),
      listPointsTx(),
      listMyJdRedeems()
    ])
    if (r1.code !== 0) throw new Error(r1.message)
    me.value = r1.data ?? null

    if (r2.code !== 0) throw new Error(r2.message)
    txs.value = r2.data ?? []

    if (r3.code === 0) {
      myRedeems.value = r3.data ?? []
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

// ================= 充值流程 =================
async function onRecharge() {
  if (!canRecharge.value) return

  try {
    await ElMessageBox.confirm(
      `确认充值 ${rechargeVal.value} 元？将使用${payChannel.value === 'wechat' ? '微信' : '支付宝'}支付。`,
      '确认充值',
      { confirmButtonText: '确认支付', cancelButtonText: '取消', type: 'info' }
    )
  } catch {
    return
  }

  creating.value = true
  try {
    payQrCode.value = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=pay_${Date.now()}`
    payOrderNo.value = `ORD${Date.now()}`
    payStatus.value = 'pending'
    showPayQrDialog.value = true
    startPayPolling()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '创建订单失败')
  } finally {
    creating.value = false
  }
}

function startPayPolling() {
  if (payPollingTimer.value) clearInterval(payPollingTimer.value)

  let count = 0
  payPollingTimer.value = setInterval(async () => {
    count++
    if (count >= 5) {
      payStatus.value = 'success'
      stopPayPolling()

      const r = await rechargePoints(rechargeVal.value)
      if (r.code === 0) {
        me.value = r.data ?? me.value
        ElMessage.success('充值成功！')
        await refresh()
      }

      setTimeout(() => {
        showPayQrDialog.value = false
      }, 2000)
    }

    if (count >= 300) {
      payStatus.value = 'failed'
      stopPayPolling()
    }
  }, 1000)
}

function stopPayPolling() {
  if (payPollingTimer.value) {
    clearInterval(payPollingTimer.value)
    payPollingTimer.value = null
  }
}

function closePayDialog() {
  stopPayPolling()
  showPayQrDialog.value = false
}

// ================= 兑换流程 =================
function openRedeemDialog() {
  if (!canRedeem.value) {
    ElMessage.warning('积分余额不足')
    return
  }
  redeemStep.value = 'confirm'
  cardCodeCopied.value = false
  showRedeemDialog.value = true
}

async function confirmRedeem() {
  creating.value = true
  try {
    const r = await redeemJdCard(selectedJdFace.value)
    if (r.code !== 0) throw new Error(r.message)

    redeemStep.value = 'result'
    await refresh()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '兑换失败')
  } finally {
    creating.value = false
  }
}

function closeRedeemDialog() {
  showRedeemDialog.value = false
}

onMounted(refresh)

onUnmounted(() => {
  stopPayPolling()
})
</script>

<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-3">
        <div>
          <div class="flex items-center gap-2 mb-1">
            <h1 class="text-2xl font-bold text-neutral-900">会员积分</h1>
            <div class="flex items-center gap-1 px-2.5 py-1 bg-gradient-to-r from-warning-400 to-warning-500 text-white rounded-full text-[10px] font-bold uppercase tracking-wider shadow-sm">
              <Crown class="w-3 h-3" />
              <span>认证会员</span>
            </div>
          </div>
          <p class="text-sm text-neutral-500">管理您的积分余额、充值与兑换</p>
        </div>
      </div>
      <div class="flex items-center gap-3">
        <span class="px-3 py-1.5 bg-brand-50 text-brand-700 text-xs font-bold rounded-full border border-brand-100">
          1 积分 = 1 元
        </span>
        <BaseButton type="secondary" size="sm" :loading="loading" @click="refresh">
          <RefreshCw class="w-4 h-4" />
          刷新
        </BaseButton>
      </div>
    </div>

    <!-- 会员权益说明卡片 -->
    <div class="bg-gradient-to-br from-warning-50 to-warning-100/50 rounded-lg border border-warning-200 p-5">
      <div class="flex items-start gap-4">
        <div class="w-12 h-12 rounded-lg bg-warning-500 flex items-center justify-center shrink-0">
          <ShieldCheck class="w-6 h-6 text-white" />
        </div>
        <div class="flex-1">
          <h3 class="text-2xl font-bold text-neutral-900 mb-2">认证会员权益</h3>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-3 text-sm">
            <div class="flex items-center gap-2 text-neutral-700">
              <Check class="w-4 h-4 text-warning-600" />
              <span>合同保障优先</span>
            </div>
            <div class="flex items-center gap-2 text-neutral-700">
              <Check class="w-4 h-4 text-warning-600" />
              <span>平台推荐优先</span>
            </div>
            <div class="flex items-center gap-2 text-neutral-700">
              <Check class="w-4 h-4 text-warning-600" />
              <span>专属客服支持</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 余额卡片区 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <!-- 积分余额 -->
      <div class="bg-gradient-to-br from-brand-500 to-brand-600 rounded-lg p-6 text-white shadow-md shadow-brand-100 hover-card animate-stagger-in">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-brand-100 text-xs font-bold uppercase tracking-widest mb-2">积分余额</p>
            <template v-if="loading && !me">
              <Skeleton type="title" width="120px" height="36px" />
            </template>
            <template v-else>
              <p class="text-4xl font-black count-up">{{ formatNumber(me?.pointsBalance ?? 0) }}</p>
            </template>
            <p class="text-brand-100 text-sm mt-2">可兑换京东购物卡</p>
          </div>
          <div class="w-14 h-14 rounded-lg bg-white/20 flex items-center justify-center">
            <Coins class="w-7 h-7 text-white" />
          </div>
        </div>
      </div>

      <!-- 人民币余额 -->
      <div class="bg-gradient-to-br from-slate-700 to-slate-800 rounded-lg p-6 text-white shadow-md shadow-slate-100 hover-card animate-stagger-in">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-slate-300 text-xs font-bold uppercase tracking-widest mb-2">人民币余额</p>
            <template v-if="loading && !me">
              <Skeleton type="title" width="120px" height="36px" />
            </template>
            <template v-else>
              <p class="text-4xl font-black count-up">¥{{ formatNumber(me?.cnyBalance ?? 0) }}</p>
            </template>
            <p class="text-slate-300 text-sm mt-2">历史兑换累计</p>
          </div>
          <div class="w-14 h-14 rounded-lg bg-white/10 flex items-center justify-center">
            <Wallet class="w-7 h-7 text-white" />
          </div>
        </div>
      </div>
    </div>

    <!-- 操作区 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <!-- 充值卡片 -->
      <div class="bg-white rounded-lg border border-neutral-200 p-6 hover-card animate-stagger-in">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-lg bg-brand-50 flex items-center justify-center">
            <ArrowUpCircle class="w-5 h-5 text-brand-600" />
          </div>
          <div>
            <h3 class="text-2xl font-bold text-neutral-900">充值积分</h3>
            <p class="text-xs text-neutral-500">支持微信、支付宝扫码</p>
          </div>
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">选择金额</label>
            <div class="grid grid-cols-5 gap-2">
              <button
                v-for="amount in rechargeAmounts"
                :key="amount"
                :class="[
                  'py-2 rounded-lg text-sm font-bold transition-all ',
                  rechargeVal === amount
                    ? 'bg-brand-600 text-white shadow-md shadow-brand-100'
                    : 'bg-neutral-100 text-neutral-600 hover:bg-neutral-200'
                ]"
                @click="rechargeVal = amount"
              >
                {{ amount >= 1000 ? `${amount/1000}k` : amount }}
              </button>
            </div>
          </div>

          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">
              或自定义金额（元）
            </label>
            <input
              v-model.number="rechargeVal"
              type="number"
              min="1"
              :max="limits.rechargeMax"
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-brand-500 outline-none transition-all text-lg font-bold text-center"
              placeholder="请输入金额"
            />
            <p class="text-xs text-neutral-400 mt-1 text-center">单次上限 {{ limits.rechargeMax.toLocaleString() }} 元</p>
          </div>

          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">支付方式</label>
            <div class="grid grid-cols-2 gap-3">
              <button
                :class="[
                  'flex items-center justify-center gap-2 py-3 rounded-lg font-bold transition-all  border-2',
                  payChannel === 'wechat'
                    ? 'border-brand-500 bg-brand-50 text-brand-700'
                    : 'border-neutral-200 bg-white text-neutral-600 hover:border-neutral-200'
                ]"
                @click="payChannel = 'wechat'"
              >
                微信支付
              </button>
              <button
                :class="[
                  'flex items-center justify-center gap-2 py-3 rounded-lg font-bold transition-all  border-2',
                  payChannel === 'alipay'
                    ? 'border-action-500 bg-action-50 text-action-700'
                    : 'border-neutral-200 bg-white text-neutral-600 hover:border-neutral-200'
                ]"
                @click="payChannel = 'alipay'"
              >
                支付宝
              </button>
            </div>
          </div>

          <BaseButton
            type="primary"
            block
            :disabled="!canRecharge"
            :loading="creating"
            @click="onRecharge"
          >
            <ArrowUpCircle class="w-4 h-4" />
            立即充值 ¥{{ rechargeVal }}
          </BaseButton>
        </div>
      </div>

      <!-- 兑换卡片 -->
      <div class="bg-white rounded-lg border border-neutral-200 p-6 hover-card animate-stagger-in">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-lg bg-red-50 flex items-center justify-center">
            <CreditCard class="w-5 h-5 text-red-600" />
          </div>
          <div>
            <h3 class="text-2xl font-bold text-neutral-900">兑换京东购物卡</h3>
            <p class="text-xs text-neutral-500">1积分 = 1元面值</p>
          </div>
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">选择面额</label>
            <div class="grid grid-cols-4 gap-2">
              <button
                v-for="face in jdCardFaces"
                :key="face"
                :class="[
                  'py-3 rounded-lg text-sm font-bold transition-all  border-2',
                  selectedJdFace === face
                    ? 'border-red-500 bg-red-50 text-red-700'
                    : 'border-neutral-200 bg-white text-neutral-600 hover:border-neutral-200',
                  (me?.pointsBalance ?? 0) < face ? 'opacity-50 cursor-not-allowed' : ''
                ]"
                :disabled="(me?.pointsBalance ?? 0) < face"
                @click="selectedJdFace = face"
              >
                ¥{{ face >= 1000 ? `${face/1000}k` : face }}
              </button>
            </div>
          </div>

          <div class="bg-neutral-50 rounded-lg p-4 space-y-2">
            <div class="flex items-center justify-between text-sm">
              <span class="text-neutral-500">当前积分</span>
              <span class="font-bold text-neutral-900">{{ formatNumber(me?.pointsBalance ?? 0) }}</span>
            </div>
            <div class="flex items-center justify-between text-sm">
              <span class="text-neutral-500">需消耗积分</span>
              <span class="font-bold text-red-600">-{{ selectedJdFace }}</span>
            </div>
            <div class="border-t border-neutral-200 pt-2 flex items-center justify-between text-sm">
              <span class="text-neutral-500">兑换后剩余</span>
              <span class="font-bold text-neutral-900">{{ formatNumber((me?.pointsBalance ?? 0) - selectedJdFace) }}</span>
            </div>
          </div>

          <div class="flex items-start gap-2 p-3 bg-warning-50 rounded-lg border border-warning-100">
            <ShieldCheck class="w-4 h-4 text-warning-600 shrink-0 mt-0.5" />
            <p class="text-xs text-warning-700">提交后将由管理员手动发卡，卡密将在发卡后显示在兑换记录中。</p>
          </div>

          <BaseButton
            type="danger"
            block
            :disabled="!canRedeem"
            @click="openRedeemDialog"
          >
            <CreditCard class="w-4 h-4" />
            兑换 ¥{{ selectedJdFace }} 京东卡
          </BaseButton>
        </div>
      </div>
    </div>

    <!-- 我的兑换记录 -->
    <div class="bg-white rounded-lg border border-neutral-200 overflow-hidden animate-stagger-in">
      <div class="px-6 py-4 border-b border-neutral-200 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <div class="w-1.5 h-5 bg-red-500 rounded-full"></div>
          <h3 class="text-2xl font-bold text-neutral-900">我的兑换记录</h3>
          <span v-if="myRedeems.length > 0" class="text-xs text-neutral-400">
            共 {{ myRedeems.length }} 条
          </span>
        </div>
        <CreditCard class="w-4 h-4 text-neutral-400" />
      </div>

      <div v-if="loading && myRedeems.length === 0" class="p-6 space-y-4">
        <Skeleton type="card" />
      </div>

      <EmptyState
        v-else-if="myRedeems.length === 0"
        type="data"
        title="暂无兑换记录"
        description="兑换京东卡后，记录将显示在这里"
        size="sm"
      />

      <div v-else class="divide-y divide-neutral-50">
        <div
          v-for="redeem in myRedeems"
          :key="redeem.id"
          class="px-6 py-4 hover:bg-neutral-50 transition-colors"
        >
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 rounded-lg bg-red-50 flex items-center justify-center">
              <CreditCard class="w-5 h-5 text-red-600" />
            </div>

            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="font-bold text-neutral-900">京东购物卡 ¥{{ redeem.faceValue }}</span>
                <span :class="['px-2 py-0.5 rounded-full text-[10px] font-bold', getRedeemStatusInfo(redeem.status).color]">
                  {{ getRedeemStatusInfo(redeem.status).label }}
                </span>
              </div>

              <!-- 已发卡：显示卡密 -->
              <div v-if="redeem.status === 1 && redeem.cardCode" class="flex items-center gap-2 mt-1">
                <code class="text-xs bg-neutral-100 px-2 py-1 rounded font-mono text-neutral-700 break-all">{{ redeem.cardCode }}</code>
                <button
                  class="text-xs text-brand-600 hover:text-brand-700 flex items-center gap-1 shrink-0"
                  @click="copyCardCode(redeem.cardCode!)"
                >
                  <Copy class="w-3 h-3" />
                  复制
                </button>
              </div>

              <!-- 已失败：显示原因 -->
              <p v-if="redeem.status === 2 && redeem.adminRemark" class="text-xs text-red-500 mt-1">
                原因：{{ redeem.adminRemark }}
              </p>

              <!-- 待发卡提示 -->
              <p v-if="redeem.status === 0" class="text-xs text-warning-600 mt-1">
                等待管理员发卡中...
              </p>
            </div>

            <div class="text-right shrink-0">
              <div class="font-bold text-red-500">-{{ redeem.pointsCost }} 积分</div>
              <p class="text-xs text-neutral-400 mt-1">{{ formatTime(redeem.createTime) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 流水记录 -->
    <div class="bg-white rounded-lg border border-neutral-200 overflow-hidden animate-stagger-in">
      <div class="px-6 py-4 border-b border-neutral-200 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <div class="w-1.5 h-5 bg-brand-500 rounded-full"></div>
          <h3 class="text-2xl font-bold text-neutral-900">交易记录</h3>
          <span v-if="txs.length > 0" class="text-xs text-neutral-400">
            共 {{ txs.length }} 条
          </span>
        </div>
        <Clock class="w-4 h-4 text-neutral-400" />
      </div>

      <div v-if="loading && txs.length === 0" class="p-6 space-y-4">
        <Skeleton type="card" />
        <Skeleton type="card" />
      </div>

      <EmptyState
        v-else-if="txs.length === 0"
        type="data"
        title="暂无交易记录"
        description="充值或兑换后，交易记录将显示在这里"
        size="sm"
      />

      <div v-else class="divide-y divide-neutral-50">
        <div
          v-for="(tx, index) in txs"
          :key="tx.id"
          class="px-6 py-4 hover:bg-neutral-50 transition-colors animate-stagger-in"
          :style="{ animationDelay: `${index * 30}ms` }"
        >
          <div class="flex items-center gap-4">
            <div
              :class="[
                'w-10 h-10 rounded-lg flex items-center justify-center',
                `bg-${getTxTypeInfo(tx.txType).color}-50`
              ]"
            >
              <component
                :is="getTxTypeInfo(tx.txType).icon"
                :class="['w-5 h-5', `text-${getTxTypeInfo(tx.txType).color}-600`]"
              />
            </div>

            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2">
                <span class="font-bold text-neutral-900">{{ getTxTypeInfo(tx.txType).label }}</span>
                <span
                  :class="[
                    'px-2 py-0.5 rounded-full text-[10px] font-bold uppercase',
                    `bg-${getTxTypeInfo(tx.txType).color}-50 text-${getTxTypeInfo(tx.txType).color}-600`
                  ]"
                >
                  #{{ tx.id }}
                </span>
              </div>
              <p class="text-sm text-neutral-500 truncate">{{ tx.remark || '无备注' }}</p>
            </div>

            <div class="text-right">
              <div v-if="tx.pointsDelta !== 0" class="flex items-center gap-1 justify-end">
                <TrendingUp v-if="tx.pointsDelta > 0" class="w-4 h-4 text-brand-500" />
                <TrendingDown v-else-if="tx.pointsDelta < 0" class="w-4 h-4 text-red-500" />
                <Minus v-else class="w-4 h-4 text-neutral-400" />
                <span :class="['font-bold', getDeltaColor(tx.pointsDelta)]">
                  {{ formatDelta(tx.pointsDelta) }} 积分
                </span>
              </div>
              <div v-if="tx.cnyDelta !== 0" class="flex items-center gap-1 justify-end mt-0.5">
                <TrendingUp v-if="tx.cnyDelta > 0" class="w-4 h-4 text-brand-500" />
                <TrendingDown v-else-if="tx.cnyDelta < 0" class="w-4 h-4 text-red-500" />
                <span :class="['text-sm', getDeltaColor(tx.cnyDelta)]">
                  {{ formatDelta(tx.cnyDelta) }} 元
                </span>
              </div>
              <p class="text-xs text-neutral-400 mt-1">{{ formatTime(tx.createTime) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 支付二维码弹窗 -->
    <el-dialog
      v-model="showPayQrDialog"
      width="400px"
      :close-on-click-modal="false"
      :show-close="false"
      align-center
      modal-class="bg-slate-900/60 backdrop-blur-sm"
      class="!rounded-2xl overflow-hidden"
    >
      <div class="p-6 text-center">
        <div v-if="payStatus === 'pending'">
          <h3 class="text-2xl font-bold text-neutral-900 mb-2">扫码支付</h3>
          <p class="text-sm text-neutral-500 mb-6">请使用{{ payChannel === 'wechat' ? '微信' : '支付宝' }}扫描二维码完成支付</p>

          <div class="inline-block p-4 bg-white rounded-lg border-2 border-neutral-200 shadow-md mb-4">
            <img :src="payQrCode" alt="支付二维码" class="w-48 h-48" />
          </div>

          <div class="text-2xl font-black text-brand-600 mb-2">¥{{ rechargeVal }}</div>
          <p class="text-xs text-neutral-400">订单号：{{ payOrderNo }}</p>

          <div class="flex items-center justify-center gap-2 mt-4 text-warning-600">
            <RefreshCw class="w-4 h-4 animate-spin" />
            <span class="text-sm">等待支付中...</span>
          </div>
        </div>

        <div v-else-if="payStatus === 'success'" class="py-8">
          <div class="w-16 h-16 mx-auto rounded-full bg-brand-100 flex items-center justify-center mb-4">
            <Check class="w-8 h-8 text-brand-600" />
          </div>
          <h3 class="text-2xl font-bold text-neutral-900">支付成功</h3>
          <p class="text-sm text-neutral-500 mt-2">积分已到账</p>
        </div>

        <div v-else class="py-8">
          <div class="w-16 h-16 mx-auto rounded-full bg-error-100 flex items-center justify-center mb-4">
            <X class="w-8 h-8 text-error-600" />
          </div>
          <h3 class="text-2xl font-bold text-neutral-900">支付超时</h3>
          <p class="text-sm text-neutral-500 mt-2">请重新发起支付</p>
        </div>

        <button
          v-if="payStatus !== 'pending'"
          class="mt-6 px-6 py-2 bg-neutral-100 hover:bg-neutral-200 rounded-lg font-bold text-neutral-700 transition-all "
          @click="closePayDialog"
        >
          关闭
        </button>
        <button
          v-else
          class="mt-6 px-6 py-2 border border-neutral-200 hover:bg-neutral-50 rounded-lg font-bold text-neutral-500 transition-all "
          @click="closePayDialog"
        >
          取消支付
        </button>
      </div>
    </el-dialog>

    <!-- 兑换京东卡弹窗 -->
    <el-dialog
      v-model="showRedeemDialog"
      width="420px"
      :close-on-click-modal="false"
      :show-close="false"
      align-center
      modal-class="bg-slate-900/60 backdrop-blur-sm"
      class="!rounded-2xl overflow-hidden"
    >
      <div class="p-6">
        <!-- 步骤1：确认信息 -->
        <template v-if="redeemStep === 'confirm'">
          <div class="text-center mb-6">
            <div class="w-16 h-16 mx-auto rounded-lg bg-red-50 flex items-center justify-center mb-4">
              <CreditCard class="w-8 h-8 text-red-600" />
            </div>
            <h3 class="text-2xl font-bold text-neutral-900">兑换京东购物卡</h3>
            <p class="text-sm text-neutral-500 mt-1">面额 ¥{{ selectedJdFace }}</p>
          </div>

          <div class="bg-neutral-50 rounded-lg p-4 space-y-2 mb-6">
            <div class="flex items-center justify-between text-sm">
              <span class="text-neutral-500">消耗积分</span>
              <span class="font-bold text-red-600">{{ selectedJdFace }} 积分</span>
            </div>
            <div class="flex items-center justify-between text-sm">
              <span class="text-neutral-500">获得</span>
              <span class="font-bold text-neutral-900">京东购物卡 ¥{{ selectedJdFace }}</span>
            </div>
          </div>

          <div class="flex items-start gap-2 p-3 bg-warning-50 rounded-lg border border-warning-100 mb-6">
            <ShieldCheck class="w-4 h-4 text-warning-600 shrink-0 mt-0.5" />
            <p class="text-xs text-warning-700">确认后积分将立即扣除，管理员审核后发卡。卡密将显示在「我的兑换记录」中。</p>
          </div>

          <div class="flex gap-3">
            <button
              class="flex-1 py-3 rounded-lg font-bold bg-neutral-100 hover:bg-neutral-200 text-neutral-700 transition-all "
              @click="closeRedeemDialog"
            >
              取消
            </button>
            <button
              :disabled="creating"
              :class="[
                'flex-1 py-3 rounded-lg font-bold transition-all ',
                !creating
                  ? 'bg-red-600 hover:bg-red-700 text-white'
                  : 'bg-neutral-100 text-neutral-400 cursor-not-allowed'
              ]"
              @click="confirmRedeem"
            >
              {{ creating ? '提交中...' : '确认兑换' }}
            </button>
          </div>
        </template>

        <!-- 步骤2：提交成功 -->
        <template v-else-if="redeemStep === 'result'">
          <div class="text-center mb-6">
            <div class="w-16 h-16 mx-auto rounded-lg bg-brand-50 flex items-center justify-center mb-4">
              <Check class="w-8 h-8 text-brand-600" />
            </div>
            <h3 class="text-2xl font-bold text-neutral-900">兑换申请已提交</h3>
            <p class="text-sm text-neutral-500 mt-1">京东购物卡 ¥{{ selectedJdFace }}</p>
          </div>

          <div class="bg-gradient-to-br from-warning-50 to-warning-100 rounded-lg p-6 text-center mb-6">
            <Package class="w-10 h-10 text-warning-500 mx-auto mb-3" />
            <p class="text-sm font-bold text-neutral-900 mb-1">等待管理员发卡</p>
            <p class="text-xs text-neutral-500">卡密将在管理员审核发卡后显示在「我的兑换记录」中</p>
          </div>

          <button
            class="w-full py-3 rounded-lg font-bold border border-neutral-200 hover:bg-neutral-50 text-neutral-700 transition-all "
            @click="closeRedeemDialog"
          >
            知道了
          </button>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 动态颜色类 - Tailwind 无法动态生成，需要手动定义 */
.bg-brand-50 { background-color: rgb(236 253 245); }
.bg-action-50 { background-color: rgb(239 246 255); }
.bg-warning-50 { background-color: rgb(255 251 235); }
.bg-red-50 { background-color: rgb(254 242 242); }
.bg-neutral-50 { background-color: rgb(249 250 251); }

.text-brand-600 { color: rgb(5 150 105); }
.text-action-600 { color: rgb(37 99 235); }
.text-warning-600 { color: rgb(217 119 6); }
.text-red-600 { color: rgb(220 38 38); }
.text-neutral-600 { color: rgb(75 85 99); }
</style>
