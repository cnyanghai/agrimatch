<script setup lang="ts">
import { ref, computed } from 'vue'
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

const statusTabs = [
  { value: -1, label: '全部' },
  { value: 1, label: '待签署' },
  { value: 2, label: '已签署' },
  { value: 3, label: '履约中' },
  { value: 4, label: '已完成' },
]

const filteredContracts = computed(() => {
  if (activeTab.value === -1) return contracts.value
  return contracts.value.filter(c => c.status === activeTab.value)
})

onLoad(() => {
  if (!authStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    uni.navigateTo({ url: '/pages/auth/login' })
    return
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
</script>

<template>
  <view class="contract-page">
    <!-- 状态筛选标签 -->
    <scroll-view scroll-x class="status-tabs">
      <view
        v-for="tab in statusTabs"
        :key="tab.value"
        class="status-tabs__item"
        :class="{ 'status-tabs__item--active': activeTab === tab.value }"
        @tap="activeTab = tab.value"
      >
        <text>{{ tab.label }}</text>
      </view>
    </scroll-view>

    <!-- 合同列表 -->
    <view v-if="filteredContracts.length > 0" class="contract-list">
      <view
        v-for="item in filteredContracts"
        :key="item.id"
        class="contract-card"
        @tap="goDetail(item.id)"
      >
        <!-- 头部：合同编号 + 状态标签 -->
        <view class="contract-card__header">
          <text class="contract-card__no">{{ item.contractNo }}</text>
          <text
            class="contract-card__status"
            :style="{ color: getStatusColor(item.status), backgroundColor: getStatusColor(item.status) + '18' }"
          >
            {{ getStatusLabel(item.status) }}
          </text>
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

    <!-- 空状态 -->
    <WgEmpty v-else-if="!loading && filteredContracts.length === 0" text="暂无合同记录" icon="empty" />
  </view>
</template>

<style lang="scss" scoped>
.contract-page {
  min-height: 100vh;
  background: $bg-page;
}

.status-tabs {
  white-space: nowrap;
  background: $bg-card;
  padding: $spacing-sm 0;
  border-bottom: 1rpx solid $border-light;

  &__item {
    display: inline-block;
    padding: $spacing-xs $spacing-lg;
    font-size: $font-md;
    color: $text-secondary;

    &--active {
      color: $brand-600;
      font-weight: bold;
    }
  }
}

.contract-list {
  padding: $spacing-sm;
}

.contract-card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;

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
