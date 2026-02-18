<script setup lang="ts">
import { ref, computed } from 'vue'
import { WARM_400 } from '../../constants/colors'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { useAuthStore } from '../../store/auth'
import {
  listContracts,
  contractStatusMap,
  type ContractResponse,
} from '../../api/contract'
import { formatAmount, formatRelativeTime } from '../../utils/format'

const authStore = useAuthStore()
const contracts = ref<ContractResponse[]>([])
const loading = ref(false)
const activeTab = ref<number>(-1) // -1 = all

// ==================== 搜索功能 ====================
const searchKeyword = ref('')
const searchInput = ref('') // 用于防抖
let searchTimer: ReturnType<typeof setTimeout> | null = null

/** 搜索输入防抖（300ms） */
function onSearchInput(val: string) {
  searchInput.value = val
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    searchKeyword.value = val.trim()
  }, 300)
}

/** 清空搜索 */
function clearSearch() {
  searchInput.value = ''
  searchKeyword.value = ''
  if (searchTimer) clearTimeout(searchTimer)
}

// ==================== 统计数据（前端计算） ====================
const stats = computed(() => {
  const all = contracts.value
  // 待结算总额：待签署(1) + 履约中(3) 的合同总额
  const pendingTotal = all
    .filter(c => c.status === 1 || c.status === 3)
    .reduce((sum, c) => sum + (c.totalAmount || 0), 0)
  // 本月新增
  const monthlyNew = all.filter(c => {
    if (!c.createTime) return false
    const d = new Date(c.createTime)
    const now = new Date()
    return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth()
  }).length
  // 完成率
  const completed = all.filter(c => c.status === 4).length
  const total = all.length
  const executionRate = total > 0 ? Math.round((completed / total) * 100) : 0

  return { pendingTotal, monthlyNew, executionRate }
})

// ==================== 待办提醒 ====================
/** 待我签署的合同数 */
const pendingSignCount = computed(() => {
  return contracts.value.filter(c => c.status === 1).length
})

/** 待确认节点的合同数 */
const pendingMilestoneCount = computed(() => {
  return contracts.value.filter(c =>
    c.milestoneTotal != null && c.milestoneTotal > 0 &&
    (c.milestoneCompleted ?? 0) < c.milestoneTotal
  ).length
})

/** 待办区域是否展开 */
const todoExpanded = ref(false)
/** 是否有待办 */
const hasTodo = computed(() => pendingSignCount.value > 0 || pendingMilestoneCount.value > 0)

/** 点击待办项跳转到对应Tab */
function goTodoTab(tabValue: number) {
  activeTab.value = tabValue
  todoExpanded.value = false
}

// ==================== 状态Tab ====================
const statusTabs = [
  { value: -1, label: '全部' },
  { value: 0, label: '草稿' },
  { value: 1, label: '待签署' },
  { value: -2, label: '待确认节点' },
  { value: 2, label: '已签署' },
  { value: 3, label: '履约中' },
  { value: 4, label: '已完成' },
]

// ==================== 筛选后的合同列表（搜索 + 状态Tab 组合） ====================
const filteredContracts = computed(() => {
  let result = contracts.value

  // 状态筛选
  if (activeTab.value === -2) {
    // "待确认节点" Tab: 筛选有未完成履约节点的合同
    result = result.filter(c =>
      c.milestoneTotal != null && c.milestoneTotal > 0 &&
      (c.milestoneCompleted ?? 0) < c.milestoneTotal
    )
  } else if (activeTab.value !== -1) {
    result = result.filter(c => c.status === activeTab.value)
  }

  // 搜索关键词筛选
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    result = result.filter(c =>
      c.contractNo?.toLowerCase().includes(kw) ||
      c.productName?.toLowerCase().includes(kw) ||
      c.buyerCompanyName?.toLowerCase().includes(kw) ||
      c.sellerCompanyName?.toLowerCase().includes(kw)
    )
  }

  return result
})

onLoad((options) => {
  if (!authStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  // 从URL query恢复tab状态（如从待办跳转而来）
  if (options?.tab) {
    const tabVal = Number(options.tab)
    if (!isNaN(tabVal)) activeTab.value = tabVal
  }
  loadData()
})

onPullDownRefresh(() => {
  loadData().finally(() => {
    uni.stopPullDownRefresh()
  })
})

async function loadData() {
  loading.value = true
  try {
    const res = await listContracts()
    contracts.value = res || []
  } catch {
    // handled by request.ts
  } finally {
    loading.value = false
  }
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/contract/detail?id=${id}` })
}

/** 获取对方企业名称（当前用户如果是买方则显示卖方，反之亦然） */
function getCounterparty(item: ContractResponse): string {
  const myCompany = authStore.user?.companyName
  if (myCompany && item.sellerCompanyName === myCompany) {
    return item.buyerCompanyName || '未知买方'
  }
  return item.sellerCompanyName || '未知卖方'
}

function getStatusLabel(status: number): string {
  return contractStatusMap[status]?.label || '未知'
}

function getStatusColor(status: number): string {
  return contractStatusMap[status]?.color || '#999'
}

type ChipVariant = 'brand' | 'autumn' | 'accent' | 'success' | 'warning' | 'error' | 'neutral'

function getStatusVariant(status: number): ChipVariant {
  const map: Record<number, ChipVariant> = {
    0: 'neutral',    // 草稿
    1: 'warning',    // 待签署
    2: 'brand',      // 已签署
    3: 'accent',     // 履约中
    4: 'success',    // 已完成
    5: 'error',      // 已取消
  }
  return map[status] || 'neutral'
}

/** 获取Tab对应的合同数量 */
function getTabCount(tabValue: number): number {
  if (tabValue === -1) return contracts.value.length
  if (tabValue === -2) return pendingMilestoneCount.value
  return contracts.value.filter(c => c.status === tabValue).length
}

/** 格式化金额为紧凑显示（统计卡片用） */
function formatStatAmount(val: number): string {
  if (val >= 10000) {
    return (val / 10000).toFixed(1) + '万'
  }
  return val.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}
</script>

<template>
  <view class="contract-page">
    <WgNavBar title="合同管理" :back="true" />

    <!-- ===== 搜索栏 ===== -->
    <view class="search-bar">
      <view class="stitch-search">
        <WgIcon name="search" :size="16" :color="WARM_400" />
        <input
          class="stitch-search__input"
          type="text"
          :value="searchInput"
          placeholder="搜索合同编号、产品、公司"
          placeholder-class="stitch-search__placeholder"
          confirm-type="search"
          @input="onSearchInput(($event as any).detail.value)"
        />
        <view v-if="searchInput" class="stitch-search__action" @tap="clearSearch">
          <WgIcon name="x" :size="14" :color="WARM_400" />
        </view>
      </view>
    </view>

    <!-- ===== 统计卡片区域 ===== -->
    <view class="stats-bar stitch-card">
      <view class="stats-bar__item">
        <text class="stats-bar__value stats-bar__value--amount font-mono">{{ formatStatAmount(stats.pendingTotal) }}</text>
        <text class="stats-bar__label">待结算(元)</text>
      </view>
      <view class="stats-bar__divider" />
      <view class="stats-bar__item">
        <text class="stats-bar__value font-mono">{{ stats.monthlyNew }}</text>
        <text class="stats-bar__label">本月新增</text>
      </view>
      <view class="stats-bar__divider" />
      <view class="stats-bar__item">
        <text class="stats-bar__value font-mono">{{ stats.executionRate }}%</text>
        <text class="stats-bar__label">完成率</text>
      </view>
    </view>

    <!-- ===== 待办提醒（可折叠） ===== -->
    <view v-if="hasTodo" class="todo-section stitch-card">
      <view class="todo-section__header" @tap="todoExpanded = !todoExpanded">
        <view class="todo-section__title-wrap">
          <WgIcon name="alert-circle" :size="16" color="#f59e0b" />
          <text class="todo-section__title">待办提醒</text>
          <view class="todo-section__badge">
            <text class="todo-section__badge-text">{{ pendingSignCount + pendingMilestoneCount }}</text>
          </view>
        </view>
        <WgIcon :name="todoExpanded ? 'chevron-up' : 'chevron-down'" :size="14" :color="WARM_400" />
      </view>
      <view v-if="todoExpanded" class="todo-section__body">
        <view
          v-if="pendingSignCount > 0"
          class="todo-item"
          @tap="goTodoTab(1)"
        >
          <view class="todo-item__dot todo-item__dot--sign" />
          <text class="todo-item__text">待我签署</text>
          <text class="todo-item__count">({{ pendingSignCount }})</text>
          <WgIcon name="chevron-right" :size="14" :color="WARM_400" />
        </view>
        <view
          v-if="pendingMilestoneCount > 0"
          class="todo-item"
          @tap="goTodoTab(-2)"
        >
          <view class="todo-item__dot todo-item__dot--milestone" />
          <text class="todo-item__text">待确认节点</text>
          <text class="todo-item__count">({{ pendingMilestoneCount }})</text>
          <WgIcon name="chevron-right" :size="14" :color="WARM_400" />
        </view>
      </view>
    </view>

    <!-- ===== 状态筛选标签 ===== -->
    <scroll-view scroll-x class="status-tabs">
      <view
        v-for="tab in statusTabs"
        :key="tab.value"
        class="status-tabs__item"
        :class="{ 'status-tabs__item--active': activeTab === tab.value }"
        @tap="activeTab = tab.value"
      >
        <text>{{ tab.label }}</text>
        <text v-if="getTabCount(tab.value) > 0" class="status-tabs__count">{{ getTabCount(tab.value) }}</text>
      </view>
    </scroll-view>

    <!-- 提示 -->
    <view v-if="activeTab === 0 && filteredContracts.length === 0 && !loading" class="hint-card">
      <text class="hint-card__text">在聊天中发送报价来创建合同</text>
    </view>

    <!-- 合同列表 -->
    <view v-if="filteredContracts.length > 0" class="contract-list">
      <view
        v-for="item in filteredContracts"
        :key="item.id"
        class="contract-card stitch-card tap-feedback stitch-fade-up"
        @tap="goDetail(item.id)"
      >
        <!-- 头部：合同编号 + 状态标签 -->
        <view class="contract-card__header">
          <text class="contract-card__no">{{ item.contractNo }}</text>
          <WgStatusChip :label="getStatusLabel(item.status)" :variant="getStatusVariant(item.status)" size="sm" />
        </view>

        <!-- 商品 + 金额 -->
        <view class="contract-card__body">
          <view class="contract-card__info">
            <text class="contract-card__product">{{ item.productName || item.categoryName || '未填写商品' }}</text>
            <text class="contract-card__counterparty">{{ getCounterparty(item) }}</text>
          </view>
          <text class="contract-card__amount">{{ formatAmount(item.totalAmount) }}</text>
        </view>

        <!-- 底部：时间 -->
        <view class="contract-card__footer">
          <text class="contract-card__time">{{ formatRelativeTime(item.createTime) }}</text>
          <text v-if="item.milestoneTotal" class="contract-card__milestone">
            进度 {{ item.milestoneCompleted || 0 }}/{{ item.milestoneTotal }}
          </text>
        </view>
      </view>
    </view>

    <!-- 加载中 -->
    <WgSkeleton v-if="loading && contracts.length === 0" type="card" :rows="3" />

    <!-- 搜索无结果 -->
    <WgEmpty
      v-else-if="!loading && searchKeyword && filteredContracts.length === 0"
      :text="'未找到与 &quot;' + searchKeyword + '&quot; 相关的合同'"
      icon="empty"
    />

    <!-- 空状态 -->
    <WgEmpty v-else-if="!loading && filteredContracts.length === 0" text="暂无合同记录" icon="empty" />
  </view>
</template>

<style lang="scss" scoped>
.contract-page {
  min-height: 100vh;
  background: $bg-page;
}

/* ===== 搜索栏 ===== */
.search-bar {
  background: $bg-card;
  padding: $spacing-sm $spacing-md;

  &__input-wrap {
    display: flex;
    align-items: center;
    background: $warm-100;
    border-radius: $radius-pill;
    padding: 0 $spacing-lg;
    height: 72rpx;
  }

  &__input {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;
    height: 72rpx;
    margin-left: $spacing-xs;
  }

  &__placeholder {
    color: $text-placeholder;
    font-size: $font-md;
  }

  &__clear {
    padding: $spacing-xs;
    flex-shrink: 0;
  }
}

/* ===== 统计卡片 ===== */
.stats-bar {
  display: flex;
  align-items: center;
  background: $bg-card;
  margin: $spacing-md $spacing-md 0;
  border-radius: $radius-xl;
  padding: $spacing-md $spacing-md;
  box-shadow: $shadow-warm-card;

  &__item {
    flex: 1;
    text-align: center;
  }

  &__value {
    font-size: 36rpx;
    font-weight: bold;
    color: $text-primary;
    display: block;

    &--amount {
      color: $accent-400;
    }
  }

  &__label {
    font-size: $font-xs;
    color: $text-secondary;
    display: block;
    margin-top: 2rpx;
  }

  &__divider {
    width: 1rpx;
    height: 56rpx;
    background: $border-light;
    flex-shrink: 0;
  }
}

/* ===== 待办提醒 ===== */
.todo-section {
  background: $bg-card;
  margin: $spacing-sm $spacing-md 0;
  border-radius: $radius-xl;
  overflow: hidden;
  box-shadow: $shadow-warm-card;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-sm $spacing-md;
  }

  &__title-wrap {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
  }

  &__title {
    font-size: $font-md;
    font-weight: 600;
    color: $text-primary;
  }

  &__badge {
    background: $color-error;
    border-radius: $radius-pill;
    padding: 0 10rpx;
    min-width: 32rpx;
    height: 32rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__badge-text {
    font-size: 20rpx;
    color: $text-inverse;
    font-weight: bold;
  }

  &__body {
    padding: 0 $spacing-md $spacing-sm;
  }
}

.todo-item {
  display: flex;
  align-items: center;
  padding: $spacing-xs 0;
  border-top: 1rpx solid $border-light;

  &__dot {
    width: 12rpx;
    height: 12rpx;
    border-radius: 50%;
    flex-shrink: 0;
    margin-right: $spacing-xs;

    &--sign {
      background: $color-warning;
    }

    &--milestone {
      background: $brand-600;
    }
  }

  &__text {
    font-size: $font-sm;
    color: $text-primary;
  }

  &__count {
    font-size: $font-sm;
    color: $text-secondary;
    margin-left: 4rpx;
  }

  /* arrow icon handled by WgIcon */
}

/* ===== 状态Tab ===== */
.status-tabs {
  white-space: nowrap;
  background: $bg-card;
  padding: $spacing-sm 0;
  border-bottom: 1rpx solid $border-light;
  margin-top: $spacing-xs;

  &__item {
    display: inline-flex;
    align-items: center;
    gap: 6rpx;
    padding: $spacing-xs $spacing-lg;
    font-size: $font-md;
    color: $text-secondary;

    &--active {
      color: $brand-600;
      font-weight: bold;
    }
  }

  &__count {
    font-size: 20rpx;
    color: $text-placeholder;
    min-width: 28rpx;
    height: 28rpx;
    line-height: 28rpx;
    text-align: center;
    background: $bg-page;
    border-radius: $radius-pill;
    padding: 0 6rpx;
  }

  &__item--active &__count {
    color: $brand-600;
    background: $brand-50;
  }
}

.hint-card {
  margin: $spacing-md;
  padding: $spacing-md $spacing-lg;
  background: $brand-50;
  border-radius: $radius-lg;
  text-align: center;

  &__text {
    font-size: $font-sm;
    color: $brand-600;
  }
}

.contract-list {
  padding: $spacing-sm;
}

.contract-card {
  background: $bg-card;
  border-radius: $radius-xl;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  box-shadow: $shadow-warm-card;
  transition: transform $transition-fast;

  &:active {
    transform: scale(0.98);
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-sm;
  }

  &__no {
    font-size: $font-sm;
    color: $text-secondary;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__status {
    font-size: $font-xs;
    padding: 4rpx 16rpx;
    border-radius: $radius-sm;
    flex-shrink: 0;
    margin-left: $spacing-sm;
  }

  &__body {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: $spacing-sm;
  }

  &__info {
    flex: 1;
    overflow: hidden;
  }

  &__product {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;
    display: block;
    margin-bottom: 4rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__counterparty {
    font-size: $font-sm;
    color: $text-secondary;
    display: block;
  }

  &__amount {
    font-size: $font-xl;
    font-weight: bold;
    color: $accent-400;
    flex-shrink: 0;
    margin-left: $spacing-sm;
  }

  &__footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: $spacing-xs;
    border-top: 1rpx solid $border-light;
  }

  &__time {
    font-size: $font-xs;
    color: $text-placeholder;
  }

  &__milestone {
    font-size: $font-xs;
    color: $brand-600;
    background: $brand-50;
    padding: 2rpx 14rpx;
    border-radius: $radius-sm;
  }
}
</style>
