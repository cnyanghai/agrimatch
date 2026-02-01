<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import {
  getAdminPointsOverview,
  listAdminRecharges,
  listAdminRechargeUsers,
  listAdminGifts,
  type AdminPointsOverviewResponse,
  type AdminRechargeRecord,
  type AdminRechargeUser,
  type AdminGiftRecord
} from '../../api/admin'
import { Search, DollarSign, CreditCard, Gift, Coins, ChevronLeft, ChevronRight } from 'lucide-vue-next'

const activeTab = ref<'overview' | 'recharges' | 'recharge-users' | 'gifts'>('overview')

// 概览
const overview = ref<AdminPointsOverviewResponse | null>(null)

// 充值记录
const rechargeKeyword = ref('')
const rechargePage = ref(1)
const rechargeList = ref<AdminRechargeRecord[]>([])
const rechargeTotal = ref(0)

// 充值名单
const rechargeUserKeyword = ref('')
const rechargeUserPage = ref(1)
const rechargeUserList = ref<AdminRechargeUser[]>([])
const rechargeUserTotal = ref(0)

// 赠送记录
const giftKeyword = ref('')
const giftPage = ref(1)
const giftList = ref<AdminGiftRecord[]>([])
const giftTotal = ref(0)

const PAGE_SIZE = 20

async function loadOverview() {
  try {
    const res = await getAdminPointsOverview()
    if (res.code === 0) overview.value = res.data
  } catch (e) { console.error(e) }
}

async function loadRecharges() {
  try {
    const res = await listAdminRecharges({
      keyword: rechargeKeyword.value || undefined,
      page: rechargePage.value,
      size: PAGE_SIZE
    })
    if (res.code === 0) {
      rechargeList.value = res.data.list
      rechargeTotal.value = res.data.total
    }
  } catch (e) { console.error(e) }
}

async function loadRechargeUsers() {
  try {
    const res = await listAdminRechargeUsers({
      keyword: rechargeUserKeyword.value || undefined,
      page: rechargeUserPage.value,
      size: PAGE_SIZE
    })
    if (res.code === 0) {
      rechargeUserList.value = res.data.list
      rechargeUserTotal.value = res.data.total
    }
  } catch (e) { console.error(e) }
}

async function loadGifts() {
  try {
    const res = await listAdminGifts({
      keyword: giftKeyword.value || undefined,
      page: giftPage.value,
      size: PAGE_SIZE
    })
    if (res.code === 0) {
      giftList.value = res.data.list
      giftTotal.value = res.data.total
    }
  } catch (e) { console.error(e) }
}

function handleSearchRecharges() {
  rechargePage.value = 1
  loadRecharges()
}

function handleSearchRechargeUsers() {
  rechargeUserPage.value = 1
  loadRechargeUsers()
}

function handleSearchGifts() {
  giftPage.value = 1
  loadGifts()
}

function formatTime(t: string | null) {
  if (!t) return '-'
  return t.replace('T', ' ').substring(0, 19)
}

function formatMoney(v: number | null | undefined) {
  if (v == null) return '0.00'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

watch(activeTab, (tab) => {
  if (tab === 'overview') loadOverview()
  if (tab === 'recharges') loadRecharges()
  if (tab === 'recharge-users') loadRechargeUsers()
  if (tab === 'gifts') loadGifts()
})

onMounted(() => {
  loadOverview()
})
</script>

<template>
  <div>
    <h1 class="text-xl font-bold text-slate-800 mb-5">积分管理</h1>

    <!-- Tabs -->
    <div class="flex gap-1 mb-5 bg-white rounded-xl p-1 shadow-sm border border-slate-200 w-fit">
      <button
        v-for="tab in [
          { key: 'overview', label: '积分概览' },
          { key: 'recharges', label: '充值记录' },
          { key: 'recharge-users', label: '充值名单' },
          { key: 'gifts', label: '赠送记录' }
        ]"
        :key="tab.key"
        :class="[
          'px-4 py-2 rounded-lg text-sm font-medium transition-all',
          activeTab === tab.key
            ? 'bg-brand-600 text-white shadow-sm'
            : 'text-slate-600 hover:bg-slate-50'
        ]"
        @click="activeTab = tab.key as any"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- Tab 1: 积分概览 -->
    <div v-if="activeTab === 'overview'" class="grid grid-cols-2 md:grid-cols-3 gap-4">
      <div class="bg-white rounded-xl p-5 border border-slate-200 shadow-sm">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-9 h-9 rounded-lg bg-green-100 flex items-center justify-center">
            <DollarSign class="w-5 h-5 text-green-600" />
          </div>
          <span class="text-sm text-slate-500">总充值金额</span>
        </div>
        <p class="text-2xl font-bold text-slate-800">¥{{ formatMoney(overview?.totalRechargeAmount) }}</p>
      </div>

      <div class="bg-white rounded-xl p-5 border border-slate-200 shadow-sm">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-9 h-9 rounded-lg bg-green-50 flex items-center justify-center">
            <DollarSign class="w-5 h-5 text-green-500" />
          </div>
          <span class="text-sm text-slate-500">今日充值金额</span>
        </div>
        <p class="text-2xl font-bold text-slate-800">¥{{ formatMoney(overview?.todayRechargeAmount) }}</p>
      </div>

      <div class="bg-white rounded-xl p-5 border border-slate-200 shadow-sm">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-9 h-9 rounded-lg bg-orange-100 flex items-center justify-center">
            <CreditCard class="w-5 h-5 text-orange-600" />
          </div>
          <span class="text-sm text-slate-500">总发卡金额</span>
        </div>
        <p class="text-2xl font-bold text-slate-800">¥{{ formatMoney(overview?.totalCardAmount) }}</p>
      </div>

      <div class="bg-white rounded-xl p-5 border border-slate-200 shadow-sm">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-9 h-9 rounded-lg bg-orange-50 flex items-center justify-center">
            <CreditCard class="w-5 h-5 text-orange-500" />
          </div>
          <span class="text-sm text-slate-500">今日发卡金额</span>
        </div>
        <p class="text-2xl font-bold text-slate-800">¥{{ formatMoney(overview?.todayCardAmount) }}</p>
      </div>

      <div class="bg-white rounded-xl p-5 border border-slate-200 shadow-sm">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-9 h-9 rounded-lg bg-purple-100 flex items-center justify-center">
            <Gift class="w-5 h-5 text-purple-600" />
          </div>
          <span class="text-sm text-slate-500">总赠送积分</span>
        </div>
        <p class="text-2xl font-bold text-slate-800">{{ overview?.totalGiftPoints?.toLocaleString() ?? 0 }}</p>
      </div>

      <div class="bg-white rounded-xl p-5 border border-slate-200 shadow-sm">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-9 h-9 rounded-lg bg-blue-100 flex items-center justify-center">
            <Coins class="w-5 h-5 text-blue-600" />
          </div>
          <span class="text-sm text-slate-500">平台积分流通量</span>
        </div>
        <p class="text-2xl font-bold text-slate-800">{{ overview?.totalCirculatingPoints?.toLocaleString() ?? 0 }}</p>
      </div>
    </div>

    <!-- Tab 2: 充值记录 -->
    <div v-if="activeTab === 'recharges'">
      <div class="flex items-center gap-3 mb-4">
        <div class="relative flex-1 max-w-xs">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            v-model="rechargeKeyword"
            class="w-full pl-9 pr-3 py-2 rounded-lg border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500/30 focus:border-brand-500"
            placeholder="搜索用户..."
            @keyup.enter="handleSearchRecharges"
          />
        </div>
      </div>

      <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <table class="w-full text-sm">
          <thead class="bg-slate-50 text-slate-600">
            <tr>
              <th class="text-left px-4 py-3 font-medium">用户</th>
              <th class="text-right px-4 py-3 font-medium">充值积分</th>
              <th class="text-left px-4 py-3 font-medium">时间</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="r in rechargeList" :key="r.id" class="hover:bg-slate-50/50">
              <td class="px-4 py-3">
                <div class="text-slate-800 font-medium">{{ r.nickName || '-' }}</div>
                <div class="text-xs text-slate-400">{{ r.phonenumber }}</div>
              </td>
              <td class="px-4 py-3 text-right font-bold text-accent-400">+{{ r.points }}</td>
              <td class="px-4 py-3 text-sm text-slate-500">{{ formatTime(r.createTime) }}</td>
            </tr>
            <tr v-if="rechargeList.length === 0">
              <td colspan="3" class="px-4 py-12 text-center text-slate-400">暂无充值记录</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div v-if="rechargeTotal > PAGE_SIZE" class="flex items-center justify-between mt-4">
        <span class="text-sm text-slate-500">共 {{ rechargeTotal }} 条</span>
        <div class="flex items-center gap-2">
          <button
            :disabled="rechargePage <= 1"
            class="p-1.5 rounded-lg border border-slate-300 disabled:opacity-40 hover:bg-slate-50"
            @click="rechargePage--; loadRecharges()"
          ><ChevronLeft class="w-4 h-4" /></button>
          <span class="text-sm text-slate-600">{{ rechargePage }} / {{ Math.ceil(rechargeTotal / PAGE_SIZE) }}</span>
          <button
            :disabled="rechargePage >= Math.ceil(rechargeTotal / PAGE_SIZE)"
            class="p-1.5 rounded-lg border border-slate-300 disabled:opacity-40 hover:bg-slate-50"
            @click="rechargePage++; loadRecharges()"
          ><ChevronRight class="w-4 h-4" /></button>
        </div>
      </div>
    </div>

    <!-- Tab 3: 充值名单 -->
    <div v-if="activeTab === 'recharge-users'">
      <div class="flex items-center gap-3 mb-4">
        <div class="relative flex-1 max-w-xs">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            v-model="rechargeUserKeyword"
            class="w-full pl-9 pr-3 py-2 rounded-lg border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500/30 focus:border-brand-500"
            placeholder="搜索用户..."
            @keyup.enter="handleSearchRechargeUsers"
          />
        </div>
      </div>

      <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <table class="w-full text-sm">
          <thead class="bg-slate-50 text-slate-600">
            <tr>
              <th class="text-left px-4 py-3 font-medium">用户</th>
              <th class="text-left px-4 py-3 font-medium">手机号</th>
              <th class="text-right px-4 py-3 font-medium">充值次数</th>
              <th class="text-right px-4 py-3 font-medium">总充值金额</th>
              <th class="text-left px-4 py-3 font-medium">最近充值</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="u in rechargeUserList" :key="u.userId" class="hover:bg-slate-50/50">
              <td class="px-4 py-3 text-slate-800 font-medium">{{ u.nickName || '-' }}</td>
              <td class="px-4 py-3 text-slate-600">{{ u.phonenumber }}</td>
              <td class="px-4 py-3 text-right text-slate-600">{{ u.rechargeCount }}</td>
              <td class="px-4 py-3 text-right font-medium text-accent-400">¥{{ formatMoney(u.totalAmount) }}</td>
              <td class="px-4 py-3 text-xs text-slate-500">{{ formatTime(u.lastRechargeTime) }}</td>
            </tr>
            <tr v-if="rechargeUserList.length === 0">
              <td colspan="5" class="px-4 py-12 text-center text-slate-400">暂无充值用户</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="rechargeUserTotal > PAGE_SIZE" class="flex items-center justify-between mt-4">
        <span class="text-sm text-slate-500">共 {{ rechargeUserTotal }} 人</span>
        <div class="flex items-center gap-2">
          <button
            :disabled="rechargeUserPage <= 1"
            class="p-1.5 rounded-lg border border-slate-300 disabled:opacity-40 hover:bg-slate-50"
            @click="rechargeUserPage--; loadRechargeUsers()"
          ><ChevronLeft class="w-4 h-4" /></button>
          <span class="text-sm text-slate-600">{{ rechargeUserPage }} / {{ Math.ceil(rechargeUserTotal / PAGE_SIZE) }}</span>
          <button
            :disabled="rechargeUserPage >= Math.ceil(rechargeUserTotal / PAGE_SIZE)"
            class="p-1.5 rounded-lg border border-slate-300 disabled:opacity-40 hover:bg-slate-50"
            @click="rechargeUserPage++; loadRechargeUsers()"
          ><ChevronRight class="w-4 h-4" /></button>
        </div>
      </div>
    </div>

    <!-- Tab 4: 赠送记录 -->
    <div v-if="activeTab === 'gifts'">
      <div class="flex items-center gap-3 mb-4">
        <div class="relative flex-1 max-w-xs">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            v-model="giftKeyword"
            class="w-full pl-9 pr-3 py-2 rounded-lg border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500/30 focus:border-brand-500"
            placeholder="搜索用户..."
            @keyup.enter="handleSearchGifts"
          />
        </div>
      </div>

      <div class="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <table class="w-full text-sm">
          <thead class="bg-slate-50 text-slate-600">
            <tr>
              <th class="text-left px-4 py-3 font-medium">赠送者</th>
              <th class="text-center px-4 py-3 font-medium"></th>
              <th class="text-left px-4 py-3 font-medium">接收者</th>
              <th class="text-right px-4 py-3 font-medium">积分数</th>
              <th class="text-left px-4 py-3 font-medium">留言</th>
              <th class="text-left px-4 py-3 font-medium">时间</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-100">
            <tr v-for="g in giftList" :key="g.id" class="hover:bg-slate-50/50">
              <td class="px-4 py-3 text-slate-800">{{ g.senderName || '-' }}</td>
              <td class="px-4 py-3 text-center text-slate-400">&rarr;</td>
              <td class="px-4 py-3 text-slate-800">{{ g.receiverName || '-' }}</td>
              <td class="px-4 py-3 text-right font-medium text-purple-600">{{ g.points }}</td>
              <td class="px-4 py-3 text-slate-500 max-w-[200px] truncate">{{ g.message || '-' }}</td>
              <td class="px-4 py-3 text-xs text-slate-500">{{ formatTime(g.createTime) }}</td>
            </tr>
            <tr v-if="giftList.length === 0">
              <td colspan="6" class="px-4 py-12 text-center text-slate-400">暂无赠送记录</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="giftTotal > PAGE_SIZE" class="flex items-center justify-between mt-4">
        <span class="text-sm text-slate-500">共 {{ giftTotal }} 条</span>
        <div class="flex items-center gap-2">
          <button
            :disabled="giftPage <= 1"
            class="p-1.5 rounded-lg border border-slate-300 disabled:opacity-40 hover:bg-slate-50"
            @click="giftPage--; loadGifts()"
          ><ChevronLeft class="w-4 h-4" /></button>
          <span class="text-sm text-slate-600">{{ giftPage }} / {{ Math.ceil(giftTotal / PAGE_SIZE) }}</span>
          <button
            :disabled="giftPage >= Math.ceil(giftTotal / PAGE_SIZE)"
            class="p-1.5 rounded-lg border border-slate-300 disabled:opacity-40 hover:bg-slate-50"
            @click="giftPage++; loadGifts()"
          ><ChevronRight class="w-4 h-4" /></button>
        </div>
      </div>
    </div>
  </div>
</template>
