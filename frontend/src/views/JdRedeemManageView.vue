<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { showToast } from '@/composables/useToast'
import { showConfirm } from '@/composables/useConfirm'
import { CreditCard, RefreshCw, Check, X, Clock, User } from 'lucide-vue-next'
import { listAdminJdRedeems, fulfillJdRedeem, failJdRedeem, type AdminJdRedeemResponse } from '../api/points'
import { BaseButton, Skeleton, EmptyState } from '../components/ui'

const loading = ref(false)
const submitting = ref(false)
const redeems = ref<AdminJdRedeemResponse[]>([])
const statusFilter = ref<number | null>(null)

// 发卡弹窗
const showFulfillDialog = ref(false)
const fulfillTarget = ref<AdminJdRedeemResponse | null>(null)
const cardCodeInput = ref('')

// 拒绝弹窗
const showFailDialog = ref(false)
const failTarget = ref<AdminJdRedeemResponse | null>(null)
const failRemarkInput = ref('')

const statusTabs = [
  { value: null, label: '全部' },
  { value: 0, label: '待发卡' },
  { value: 1, label: '已发卡' },
  { value: 2, label: '已失败' }
]

function getStatusInfo(status: number) {
  switch (status) {
    case 0: return { label: '待发卡', color: 'bg-warning-100 text-warning-700' }
    case 1: return { label: '已发卡', color: 'bg-brand-100 text-brand-700' }
    case 2: return { label: '已失败', color: 'bg-red-100 text-red-700' }
    default: return { label: '未知', color: 'bg-neutral-100 text-neutral-700' }
  }
}

function formatTime(timeStr?: string): string {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 统计数据
const pendingCount = computed(() => redeems.value.filter(r => r.status === 0).length)
const fulfilledCount = computed(() => redeems.value.filter(r => r.status === 1).length)
const failedCount = computed(() => redeems.value.filter(r => r.status === 2).length)

async function loadRedeems() {
  loading.value = true
  try {
    const res = await listAdminJdRedeems(statusFilter.value ?? undefined)
    if (res.code !== 0) throw new Error(res.message)
    redeems.value = res.data ?? []
  } catch (e: any) {
    showToast.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

function openFulfillDialog(item: AdminJdRedeemResponse) {
  fulfillTarget.value = item
  cardCodeInput.value = ''
  showFulfillDialog.value = true
}

async function submitFulfill() {
  if (!fulfillTarget.value || !cardCodeInput.value.trim()) {
    showToast.warning('请输入卡密')
    return
  }

  submitting.value = true
  try {
    const res = await fulfillJdRedeem(fulfillTarget.value.id, cardCodeInput.value.trim())
    if (res.code !== 0) throw new Error(res.message)
    showToast.success('发卡成功')
    showFulfillDialog.value = false
    await loadRedeems()
  } catch (e: any) {
    showToast.error(e?.message ?? '发卡失败')
  } finally {
    submitting.value = false
  }
}

function openFailDialog(item: AdminJdRedeemResponse) {
  failTarget.value = item
  failRemarkInput.value = ''
  showFailDialog.value = true
}

async function submitFail() {
  if (!failTarget.value) return

  const ok = await showConfirm({ title: '确认拒绝', message: `确认拒绝该订单？将退还用户 ${failTarget.value.pointsCost} 积分。`, type: 'warning' })
  if (!ok) return

  submitting.value = true
  try {
    const res = await failJdRedeem(failTarget.value.id, failRemarkInput.value.trim() || undefined)
    if (res.code !== 0) throw new Error(res.message)
    showToast.success('已拒绝，积分已退还')
    showFailDialog.value = false
    await loadRedeems()
  } catch (e: any) {
    showToast.error(e?.message ?? '操作失败')
  } finally {
    submitting.value = false
  }
}

function switchTab(value: number | null) {
  statusFilter.value = value
  loadRedeems()
}

onMounted(loadRedeems)
</script>

<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-neutral-900">京东卡发卡管理</h1>
        <p class="text-sm text-neutral-500 mt-1">查看兑换订单、手动填入卡密或拒绝发卡</p>
      </div>
      <BaseButton type="secondary" size="sm" :loading="loading" @click="loadRedeems">
        <RefreshCw class="w-4 h-4" />
        刷新
      </BaseButton>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-3 gap-4">
      <div class="bg-warning-50 border border-warning-200 rounded-lg p-4">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-lg bg-warning-500 flex items-center justify-center">
            <Clock class="w-5 h-5 text-white" />
          </div>
          <div>
            <p class="text-2xl font-black text-warning-700">{{ pendingCount }}</p>
            <p class="text-xs text-warning-600">待发卡</p>
          </div>
        </div>
      </div>
      <div class="bg-brand-50 border border-brand-200 rounded-lg p-4">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-lg bg-brand-500 flex items-center justify-center">
            <Check class="w-5 h-5 text-white" />
          </div>
          <div>
            <p class="text-2xl font-black text-brand-700">{{ fulfilledCount }}</p>
            <p class="text-xs text-brand-600">已发卡</p>
          </div>
        </div>
      </div>
      <div class="bg-red-50 border border-red-200 rounded-lg p-4">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-lg bg-red-500 flex items-center justify-center">
            <X class="w-5 h-5 text-white" />
          </div>
          <div>
            <p class="text-2xl font-black text-red-700">{{ failedCount }}</p>
            <p class="text-xs text-red-600">已失败</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 状态筛选 -->
    <div class="flex items-center gap-2">
      <button
        v-for="tab in statusTabs"
        :key="String(tab.value)"
        :class="[
          'px-4 py-2 rounded-xl font-bold text-sm transition-all',
          statusFilter === tab.value
            ? 'bg-brand-600 text-white shadow-md'
            : 'bg-white border border-neutral-200 text-neutral-600 hover:border-brand-300'
        ]"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 订单列表 -->
    <div class="bg-white rounded-lg border border-neutral-200 overflow-hidden">
      <div v-if="loading && redeems.length === 0" class="p-6 space-y-4">
        <Skeleton type="card" />
        <Skeleton type="card" />
        <Skeleton type="card" />
      </div>

      <EmptyState
        v-else-if="redeems.length === 0"
        type="data"
        title="暂无订单"
        description="当前筛选条件下没有兑换订单"
        size="sm"
      />

      <div v-else class="divide-y divide-neutral-100">
        <div
          v-for="item in redeems"
          :key="item.id"
          class="px-6 py-4 hover:bg-neutral-50 transition-colors"
        >
          <div class="flex items-center gap-4">
            <!-- 用户信息 -->
            <div class="w-10 h-10 rounded-full bg-neutral-100 flex items-center justify-center">
              <User class="w-5 h-5 text-neutral-500" />
            </div>

            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="font-bold text-neutral-900">{{ item.nickName || item.userName }}</span>
                <span class="text-xs text-neutral-400">#{{ item.id }}</span>
                <span :class="['px-2 py-0.5 rounded-full text-[10px] font-bold', getStatusInfo(item.status).color]">
                  {{ getStatusInfo(item.status).label }}
                </span>
              </div>
              <div class="flex items-center gap-4 text-sm text-neutral-500">
                <span>面额 ¥{{ item.faceValue }}</span>
                <span>{{ item.pointsCost }} 积分</span>
                <span>{{ formatTime(item.createTime) }}</span>
              </div>

              <!-- 已发卡信息 -->
              <div v-if="item.status === 1 && item.cardCode" class="mt-1">
                <code class="text-xs bg-neutral-100 px-2 py-1 rounded font-mono text-neutral-600 break-all">{{ item.cardCode }}</code>
                <span class="text-xs text-neutral-400 ml-2">{{ formatTime(item.fulfillTime) }}</span>
              </div>

              <!-- 失败原因 -->
              <p v-if="item.status === 2 && item.adminRemark" class="text-xs text-red-500 mt-1">
                拒绝原因：{{ item.adminRemark }}
              </p>
            </div>

            <!-- 操作按钮 -->
            <div v-if="item.status === 0" class="flex items-center gap-2 shrink-0">
              <button
                class="px-4 py-2 rounded-lg font-bold text-sm bg-brand-600 hover:bg-brand-700 text-white transition-all"
                @click="openFulfillDialog(item)"
              >
                发卡
              </button>
              <button
                class="px-4 py-2 rounded-lg font-bold text-sm bg-red-50 hover:bg-red-100 text-red-600 transition-all"
                @click="openFailDialog(item)"
              >
                拒绝
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 发卡弹窗 -->
    <el-dialog
      v-model="showFulfillDialog"
      width="460px"
      :close-on-click-modal="false"
      align-center
      modal-class="bg-slate-900/60 backdrop-blur-sm"
      class="!rounded-2xl overflow-hidden"
    >
      <div class="p-6" v-if="fulfillTarget">
        <div class="text-center mb-6">
          <div class="w-16 h-16 mx-auto rounded-lg bg-brand-50 flex items-center justify-center mb-4">
            <CreditCard class="w-8 h-8 text-brand-600" />
          </div>
          <h3 class="text-2xl font-bold text-neutral-900">填入卡密</h3>
          <p class="text-sm text-neutral-500 mt-1">
            用户: {{ fulfillTarget.nickName || fulfillTarget.userName }} | 面额 ¥{{ fulfillTarget.faceValue }}
          </p>
        </div>

        <div class="space-y-4 mb-6">
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">京东 E 卡卡密</label>
            <textarea
              v-model="cardCodeInput"
              rows="3"
              class="w-full px-4 py-3 border-2 border-neutral-200 rounded-lg focus:border-brand-500 outline-none transition-all font-mono text-sm"
              placeholder="请输入从京东购买的卡密..."
            />
          </div>
        </div>

        <div class="flex gap-3">
          <button
            class="flex-1 py-3 rounded-lg font-bold bg-neutral-100 hover:bg-neutral-200 text-neutral-700 transition-all"
            @click="showFulfillDialog = false"
          >
            取消
          </button>
          <button
            :disabled="!cardCodeInput.trim() || submitting"
            :class="[
              'flex-1 py-3 rounded-lg font-bold transition-all',
              cardCodeInput.trim() && !submitting
                ? 'bg-brand-600 hover:bg-brand-700 text-white'
                : 'bg-neutral-100 text-neutral-400 cursor-not-allowed'
            ]"
            @click="submitFulfill"
          >
            {{ submitting ? '提交中...' : '确认发卡' }}
          </button>
        </div>
      </div>
    </el-dialog>

    <!-- 拒绝弹窗 -->
    <el-dialog
      v-model="showFailDialog"
      width="460px"
      :close-on-click-modal="false"
      align-center
      modal-class="bg-slate-900/60 backdrop-blur-sm"
      class="!rounded-2xl overflow-hidden"
    >
      <div class="p-6" v-if="failTarget">
        <div class="text-center mb-6">
          <div class="w-16 h-16 mx-auto rounded-lg bg-red-50 flex items-center justify-center mb-4">
            <X class="w-8 h-8 text-red-600" />
          </div>
          <h3 class="text-2xl font-bold text-neutral-900">拒绝发卡</h3>
          <p class="text-sm text-neutral-500 mt-1">
            用户: {{ failTarget.nickName || failTarget.userName }} | 面额 ¥{{ failTarget.faceValue }}
          </p>
        </div>

        <div class="bg-warning-50 rounded-lg p-4 mb-4 border border-warning-200">
          <p class="text-sm text-warning-700 font-bold">拒绝后将自动退还用户 {{ failTarget.pointsCost }} 积分</p>
        </div>

        <div class="space-y-4 mb-6">
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">拒绝原因（可选）</label>
            <textarea
              v-model="failRemarkInput"
              rows="3"
              class="w-full px-4 py-3 border-2 border-neutral-200 rounded-lg focus:border-red-500 outline-none transition-all text-sm"
              placeholder="请输入拒绝原因..."
            />
          </div>
        </div>

        <div class="flex gap-3">
          <button
            class="flex-1 py-3 rounded-lg font-bold bg-neutral-100 hover:bg-neutral-200 text-neutral-700 transition-all"
            @click="showFailDialog = false"
          >
            取消
          </button>
          <button
            :disabled="submitting"
            :class="[
              'flex-1 py-3 rounded-lg font-bold transition-all',
              !submitting
                ? 'bg-red-600 hover:bg-red-700 text-white'
                : 'bg-neutral-100 text-neutral-400 cursor-not-allowed'
            ]"
            @click="submitFail"
          >
            {{ submitting ? '提交中...' : '确认拒绝' }}
          </button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
