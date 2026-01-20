<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Coins, Wallet, RefreshCw, ArrowUpCircle, ArrowDownCircle, Clock, TrendingUp, TrendingDown, Minus, CreditCard, Smartphone, ShieldCheck, Copy, Check, X } from 'lucide-vue-next'
import { getPointsMe, listPointsTx, rechargePoints, redeemPoints, type PointsTxResponse } from '../api/points'
import { BaseButton, EmptyState, Skeleton } from '../components/ui'

const loading = ref(false)
const creating = ref(false)
const me = ref<{ pointsBalance: number; cnyBalance: number } | null>(null)
const txs = ref<PointsTxResponse[]>([])

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
const jdCardFaces = [50, 100, 200, 500]
const selectedJdFace = ref<number>(100)
const showRedeemDialog = ref(false)
const redeemStep = ref<'select' | 'verify' | 'result'>('select')
const smsCode = ref('')
const smsSending = ref(false)
const smsCountdown = ref(0)
const redeemResult = ref<{ cardCode: string; faceValue: number } | null>(null)
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
    JD_REDEEM: { label: '京东卡', color: 'red', icon: CreditCard },
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
    // 模拟创建订单并获取二维码（实际接入时替换为真实API）
    // const r = await createRechargeOrder({ amount: rechargeVal.value, channel: payChannel.value })
    // payQrCode.value = r.data.qrCodeUrl
    // payOrderNo.value = r.data.orderNo
    
    // 模拟二维码（开发阶段）
    payQrCode.value = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=pay_${Date.now()}`
    payOrderNo.value = `ORD${Date.now()}`
    payStatus.value = 'pending'
    showPayQrDialog.value = true
    
    // 开始轮询支付状态
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
    // 模拟：5秒后自动成功（实际接入时替换为查询API）
    if (count >= 5) {
      payStatus.value = 'success'
      stopPayPolling()
      
      // 模拟充值成功
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
    
    // 超时关闭（5分钟）
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
  redeemStep.value = 'select'
  smsCode.value = ''
  redeemResult.value = null
  cardCodeCopied.value = false
  showRedeemDialog.value = true
}

async function sendSmsCode() {
  if (smsSending.value || smsCountdown.value > 0) return
  
  smsSending.value = true
  try {
    // 模拟发送验证码（实际接入时替换为真实API）
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('验证码已发送')
    
    // 开始倒计时
    smsCountdown.value = 60
    const timer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '发送失败')
  } finally {
    smsSending.value = false
  }
}

async function confirmRedeem() {
  if (smsCode.value.length !== 6) {
    ElMessage.warning('请输入6位验证码')
    return
  }
  
  creating.value = true
  try {
    // 模拟兑换（实际接入时替换为真实API）
    // const r = await redeemJdCard({ faceValue: selectedJdFace.value, smsCode: smsCode.value })
    
    // 模拟扣减积分
    const r = await redeemPoints(selectedJdFace.value)
    if (r.code !== 0) throw new Error(r.message)
    me.value = r.data ?? me.value
    
    // 模拟返回卡密
    redeemResult.value = {
      cardCode: `JD${Date.now()}${Math.random().toString(36).substring(2, 8).toUpperCase()}`,
      faceValue: selectedJdFace.value
    }
    redeemStep.value = 'result'
    
    await refresh()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '兑换失败')
  } finally {
    creating.value = false
  }
}

function copyCardCode() {
  if (!redeemResult.value) return
  navigator.clipboard.writeText(redeemResult.value.cardCode)
  cardCodeCopied.value = true
  ElMessage.success('卡密已复制')
  setTimeout(() => { cardCodeCopied.value = false }, 3000)
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
      <div>
        <h1 class="text-2xl font-bold text-gray-900">我的积分</h1>
        <p class="text-sm text-gray-500 mt-1">管理您的积分余额、充值与兑换</p>
      </div>
      <div class="flex items-center gap-3">
        <span class="px-3 py-1.5 bg-emerald-50 text-emerald-700 text-xs font-bold rounded-full border border-emerald-100">
          1 积分 = 1 元
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
            <p class="text-emerald-100 text-sm mt-2">可兑换京东购物卡</p>
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
            <p class="text-slate-300 text-sm mt-2">历史兑换累计</p>
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
            <p class="text-xs text-gray-500">支持微信、支付宝扫码</p>
          </div>
        </div>
        
        <div class="space-y-4">
          <!-- 金额快选 -->
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">选择金额</label>
            <div class="grid grid-cols-5 gap-2">
              <button
                v-for="amount in rechargeAmounts"
                :key="amount"
                :class="[
                  'py-2 rounded-xl text-sm font-bold transition-all active:scale-95',
                  rechargeVal === amount 
                    ? 'bg-emerald-600 text-white shadow-md shadow-emerald-100' 
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                ]"
                @click="rechargeVal = amount"
              >
                {{ amount >= 1000 ? `${amount/1000}k` : amount }}
              </button>
            </div>
          </div>
          
          <!-- 自定义金额 -->
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
              或自定义金额（元）
            </label>
            <input
              v-model.number="rechargeVal"
              type="number"
              min="1"
              :max="limits.rechargeMax"
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-lg font-bold text-center"
              placeholder="请输入金额"
            />
            <p class="text-xs text-gray-400 mt-1 text-center">单次上限 {{ limits.rechargeMax.toLocaleString() }} 元</p>
          </div>
          
          <!-- 支付方式 -->
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">支付方式</label>
            <div class="grid grid-cols-2 gap-3">
              <button
                :class="[
                  'flex items-center justify-center gap-2 py-3 rounded-xl font-bold transition-all active:scale-95 border-2',
                  payChannel === 'wechat' 
                    ? 'border-emerald-500 bg-emerald-50 text-emerald-700' 
                    : 'border-gray-100 bg-white text-gray-600 hover:border-gray-200'
                ]"
                @click="payChannel = 'wechat'"
              >
                <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 01.213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.32.32 0 00.167-.054l1.903-1.114a.864.864 0 01.717-.098 10.16 10.16 0 002.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178A1.17 1.17 0 014.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 01-1.162 1.178 1.17 1.17 0 01-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 01.598.082l1.584.926a.272.272 0 00.14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.49.49 0 01.176-.553c1.527-1.123 2.501-2.782 2.501-4.615 0-3.372-3.186-6.13-7.059-6.13zM13.12 12.1c.536 0 .969.44.969.983a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.543.433-.983.97-.983zm4.844 0c.536 0 .969.44.969.983a.976.976 0 01-.969.983.976.976 0 01-.969-.983c0-.543.433-.983.97-.983z"/>
                </svg>
                微信支付
              </button>
              <button
                :class="[
                  'flex items-center justify-center gap-2 py-3 rounded-xl font-bold transition-all active:scale-95 border-2',
                  payChannel === 'alipay' 
                    ? 'border-blue-500 bg-blue-50 text-blue-700' 
                    : 'border-gray-100 bg-white text-gray-600 hover:border-gray-200'
                ]"
                @click="payChannel = 'alipay'"
              >
                <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M21.422 15.358c-.483-.222-3.632-1.65-4.16-1.888-.528-.239-.816-.355-1.152.19-.336.547-.816 1.375-1.008 1.663-.192.287-.384.336-.72.12-.336-.216-1.44-.528-2.736-1.68-1.008-.912-1.68-2.016-1.872-2.352-.192-.336-.024-.528.144-.696.144-.144.336-.384.504-.576.168-.192.216-.336.336-.552.12-.216.072-.408-.024-.576-.096-.168-.816-1.968-1.128-2.688-.288-.72-.6-.624-.816-.624-.216 0-.456-.024-.696-.024s-.624.096-.96.456c-.336.36-1.248 1.224-1.248 2.976 0 1.752 1.272 3.456 1.44 3.696.168.24 2.496 3.816 6.048 5.352.84.36 1.512.576 2.016.744.864.264 1.632.216 2.256.144.696-.096 2.112-.864 2.4-1.68.288-.816.288-1.536.216-1.68-.072-.144-.264-.216-.528-.336zM12.048 21.744c-1.872 0-3.648-.504-5.16-1.392l-.36-.216-3.768.984.996-3.648-.24-.384c-.984-1.56-1.512-3.36-1.512-5.256C2.004 6.336 6.336 2.004 12.048 2.004c2.736 0 5.352 1.08 7.296 3.024 1.944 1.944 3.024 4.56 3.024 7.296-.024 5.712-4.608 10.32-10.32 10.32z"/>
                </svg>
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
      <div class="bg-white rounded-2xl border border-gray-100 p-6 hover-card animate-stagger-in">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-xl bg-red-50 flex items-center justify-center">
            <CreditCard class="w-5 h-5 text-red-600" />
          </div>
          <div>
            <h3 class="font-bold text-gray-900">兑换京东购物卡</h3>
            <p class="text-xs text-gray-500">1积分 = 1元面值</p>
          </div>
        </div>
        
        <div class="space-y-4">
          <!-- 面额选择 -->
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">选择面额</label>
            <div class="grid grid-cols-4 gap-2">
              <button
                v-for="face in jdCardFaces"
                :key="face"
                :class="[
                  'py-3 rounded-xl text-sm font-bold transition-all active:scale-95 border-2',
                  selectedJdFace === face 
                    ? 'border-red-500 bg-red-50 text-red-700' 
                    : 'border-gray-100 bg-white text-gray-600 hover:border-gray-200',
                  (me?.pointsBalance ?? 0) < face ? 'opacity-50 cursor-not-allowed' : ''
                ]"
                :disabled="(me?.pointsBalance ?? 0) < face"
                @click="selectedJdFace = face"
              >
                ¥{{ face }}
              </button>
            </div>
          </div>
          
          <!-- 积分信息 -->
          <div class="bg-gray-50 rounded-xl p-4 space-y-2">
            <div class="flex items-center justify-between text-sm">
              <span class="text-gray-500">当前积分</span>
              <span class="font-bold text-gray-900">{{ formatNumber(me?.pointsBalance ?? 0) }}</span>
            </div>
            <div class="flex items-center justify-between text-sm">
              <span class="text-gray-500">需消耗积分</span>
              <span class="font-bold text-red-600">-{{ selectedJdFace }}</span>
            </div>
            <div class="border-t border-gray-200 pt-2 flex items-center justify-between text-sm">
              <span class="text-gray-500">兑换后剩余</span>
              <span class="font-bold text-gray-900">{{ formatNumber((me?.pointsBalance ?? 0) - selectedJdFace) }}</span>
            </div>
          </div>
          
          <!-- 安全提示 -->
          <div class="flex items-start gap-2 p-3 bg-amber-50 rounded-xl border border-amber-100">
            <ShieldCheck class="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
            <p class="text-xs text-amber-700">兑换需要短信验证码确认，卡密将立即显示，请妥善保管。</p>
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

    <!-- 支付二维码弹窗 -->
    <el-dialog
      v-model="showPayQrDialog"
      width="400px"
      :close-on-click-modal="false"
      :show-close="false"
      align-center
      modal-class="bg-slate-900/60 backdrop-blur-sm"
      class="!rounded-[32px] overflow-hidden"
    >
      <div class="p-6 text-center">
        <div v-if="payStatus === 'pending'">
          <h3 class="text-lg font-bold text-gray-900 mb-2">扫码支付</h3>
          <p class="text-sm text-gray-500 mb-6">请使用{{ payChannel === 'wechat' ? '微信' : '支付宝' }}扫描二维码完成支付</p>
          
          <div class="inline-block p-4 bg-white rounded-2xl border-2 border-gray-100 shadow-lg mb-4">
            <img :src="payQrCode" alt="支付二维码" class="w-48 h-48" />
          </div>
          
          <div class="text-2xl font-black text-emerald-600 mb-2">¥{{ rechargeVal }}</div>
          <p class="text-xs text-gray-400">订单号：{{ payOrderNo }}</p>
          
          <div class="flex items-center justify-center gap-2 mt-4 text-amber-600">
            <RefreshCw class="w-4 h-4 animate-spin" />
            <span class="text-sm">等待支付中...</span>
          </div>
        </div>
        
        <div v-else-if="payStatus === 'success'" class="py-8">
          <div class="w-16 h-16 mx-auto rounded-full bg-emerald-100 flex items-center justify-center mb-4">
            <Check class="w-8 h-8 text-emerald-600" />
          </div>
          <h3 class="text-lg font-bold text-gray-900">支付成功</h3>
          <p class="text-sm text-gray-500 mt-2">积分已到账</p>
        </div>
        
        <div v-else class="py-8">
          <div class="w-16 h-16 mx-auto rounded-full bg-red-100 flex items-center justify-center mb-4">
            <X class="w-8 h-8 text-red-600" />
          </div>
          <h3 class="text-lg font-bold text-gray-900">支付超时</h3>
          <p class="text-sm text-gray-500 mt-2">请重新发起支付</p>
        </div>
        
        <button
          v-if="payStatus !== 'pending'"
          class="mt-6 px-6 py-2 bg-gray-100 hover:bg-gray-200 rounded-xl font-bold text-gray-700 transition-all active:scale-95"
          @click="closePayDialog"
        >
          关闭
        </button>
        <button
          v-else
          class="mt-6 px-6 py-2 border border-gray-200 hover:bg-gray-50 rounded-xl font-bold text-gray-500 transition-all active:scale-95"
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
      class="!rounded-[32px] overflow-hidden"
    >
      <div class="p-6">
        <!-- 步骤1：确认信息 -->
        <template v-if="redeemStep === 'select'">
          <div class="text-center mb-6">
            <div class="w-16 h-16 mx-auto rounded-2xl bg-red-50 flex items-center justify-center mb-4">
              <CreditCard class="w-8 h-8 text-red-600" />
            </div>
            <h3 class="text-lg font-bold text-gray-900">兑换京东购物卡</h3>
            <p class="text-sm text-gray-500 mt-1">面额 ¥{{ selectedJdFace }}</p>
          </div>
          
          <div class="bg-gray-50 rounded-xl p-4 space-y-2 mb-6">
            <div class="flex items-center justify-between text-sm">
              <span class="text-gray-500">消耗积分</span>
              <span class="font-bold text-red-600">{{ selectedJdFace }} 积分</span>
            </div>
            <div class="flex items-center justify-between text-sm">
              <span class="text-gray-500">获得卡密</span>
              <span class="font-bold text-gray-900">京东购物卡 ¥{{ selectedJdFace }}</span>
            </div>
          </div>
          
          <div class="flex gap-3">
            <button
              class="flex-1 py-3 rounded-xl font-bold bg-gray-100 hover:bg-gray-200 text-gray-700 transition-all active:scale-95"
              @click="closeRedeemDialog"
            >
              取消
            </button>
            <button
              class="flex-1 py-3 rounded-xl font-bold bg-red-600 hover:bg-red-700 text-white transition-all active:scale-95"
              @click="redeemStep = 'verify'"
            >
              下一步
            </button>
          </div>
        </template>
        
        <!-- 步骤2：短信验证 -->
        <template v-else-if="redeemStep === 'verify'">
          <div class="text-center mb-6">
            <div class="w-16 h-16 mx-auto rounded-2xl bg-blue-50 flex items-center justify-center mb-4">
              <Smartphone class="w-8 h-8 text-blue-600" />
            </div>
            <h3 class="text-lg font-bold text-gray-900">短信验证</h3>
            <p class="text-sm text-gray-500 mt-1">请输入手机收到的验证码</p>
          </div>
          
          <div class="space-y-4 mb-6">
            <div class="flex gap-3">
              <input
                v-model="smsCode"
                type="text"
                maxlength="6"
                class="flex-1 px-4 py-3 border-2 border-gray-100 rounded-xl focus:border-blue-500 outline-none transition-all text-center text-xl font-bold tracking-widest"
                placeholder="000000"
              />
              <button
                :disabled="smsSending || smsCountdown > 0"
                :class="[
                  'px-4 py-3 rounded-xl font-bold transition-all active:scale-95 whitespace-nowrap',
                  smsCountdown > 0 
                    ? 'bg-gray-100 text-gray-400 cursor-not-allowed' 
                    : 'bg-blue-600 hover:bg-blue-700 text-white'
                ]"
                @click="sendSmsCode"
              >
                {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
              </button>
            </div>
          </div>
          
          <div class="flex gap-3">
            <button
              class="flex-1 py-3 rounded-xl font-bold bg-gray-100 hover:bg-gray-200 text-gray-700 transition-all active:scale-95"
              @click="redeemStep = 'select'"
            >
              返回
            </button>
            <button
              :disabled="smsCode.length !== 6 || creating"
              :class="[
                'flex-1 py-3 rounded-xl font-bold transition-all active:scale-95',
                smsCode.length === 6 && !creating
                  ? 'bg-red-600 hover:bg-red-700 text-white'
                  : 'bg-gray-100 text-gray-400 cursor-not-allowed'
              ]"
              @click="confirmRedeem"
            >
              {{ creating ? '兑换中...' : '确认兑换' }}
            </button>
          </div>
        </template>
        
        <!-- 步骤3：兑换成功 -->
        <template v-else-if="redeemStep === 'result' && redeemResult">
          <div class="text-center mb-6">
            <div class="w-16 h-16 mx-auto rounded-2xl bg-emerald-50 flex items-center justify-center mb-4">
              <Check class="w-8 h-8 text-emerald-600" />
            </div>
            <h3 class="text-lg font-bold text-gray-900">兑换成功</h3>
            <p class="text-sm text-gray-500 mt-1">京东购物卡 ¥{{ redeemResult.faceValue }}</p>
          </div>
          
          <div class="bg-gradient-to-br from-red-500 to-red-600 rounded-2xl p-6 text-white mb-6">
            <div class="text-xs font-bold uppercase tracking-widest opacity-80 mb-2">卡密</div>
            <div class="text-lg font-mono font-bold tracking-wider break-all">{{ redeemResult.cardCode }}</div>
          </div>
          
          <button
            :class="[
              'w-full py-3 rounded-xl font-bold flex items-center justify-center gap-2 transition-all active:scale-95',
              cardCodeCopied 
                ? 'bg-emerald-600 text-white' 
                : 'bg-gray-100 hover:bg-gray-200 text-gray-700'
            ]"
            @click="copyCardCode"
          >
            <component :is="cardCodeCopied ? Check : Copy" class="w-4 h-4" />
            {{ cardCodeCopied ? '已复制' : '复制卡密' }}
          </button>
          
          <p class="text-xs text-gray-400 text-center mt-4">请妥善保管卡密，前往京东官网充值使用</p>
          
          <button
            class="w-full mt-4 py-3 rounded-xl font-bold border border-gray-200 hover:bg-gray-50 text-gray-700 transition-all active:scale-95"
            @click="closeRedeemDialog"
          >
            完成
          </button>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 动态颜色类 - Tailwind 无法动态生成，需要手动定义 */
.bg-emerald-50 { background-color: rgb(236 253 245); }
.bg-blue-50 { background-color: rgb(239 246 255); }
.bg-purple-50 { background-color: rgb(250 245 255); }
.bg-amber-50 { background-color: rgb(255 251 235); }
.bg-red-50 { background-color: rgb(254 242 242); }
.bg-gray-50 { background-color: rgb(249 250 251); }

.text-emerald-600 { color: rgb(5 150 105); }
.text-blue-600 { color: rgb(37 99 235); }
.text-purple-600 { color: rgb(147 51 234); }
.text-amber-600 { color: rgb(217 119 6); }
.text-red-600 { color: rgb(220 38 38); }
.text-gray-600 { color: rgb(75 85 99); }
</style>
