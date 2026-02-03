<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import {
  listFuturesProducts,
  listFuturesContracts,
  type FuturesContractResponse,
  type FuturesProduct,
} from '../../api/futures'

const products = ref<FuturesProduct[]>([])
const activeProduct = ref('')
const contracts = ref<FuturesContractResponse[]>([])
const loading = ref(false)
const productsLoading = ref(true)

onLoad(async () => {
  await loadProducts()
})

onPullDownRefresh(() => {
  loadContracts(activeProduct.value).finally(() => uni.stopPullDownRefresh())
})

async function loadProducts() {
  productsLoading.value = true
  try {
    const res = await listFuturesProducts()
    products.value = res || []
    if (products.value.length > 0) {
      activeProduct.value = products.value[0].code
      await loadContracts(activeProduct.value)
    }
  } catch {
    // handled
  } finally {
    productsLoading.value = false
  }
}

async function loadContracts(productCode: string) {
  loading.value = true
  try {
    contracts.value = await listFuturesContracts(productCode) || []
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function handleProductChange(code: string) {
  if (code === activeProduct.value) return
  activeProduct.value = code
  loadContracts(code)
}

/** 格式化价格变动 */
function formatChange(change?: number): string {
  if (!change) return '0'
  return (change > 0 ? '+' : '') + change.toFixed(0)
}

/** 格式化涨跌幅 */
function formatPercent(pct?: number): string {
  if (!pct) return '0.00%'
  return (pct > 0 ? '+' : '') + pct.toFixed(2) + '%'
}

/** 是否上涨 */
function isUp(change?: number): boolean {
  return (change || 0) > 0
}

/** 是否下跌 */
function isDown(change?: number): boolean {
  return (change || 0) < 0
}

/** 格式化成交量 */
function formatVolume(v?: number): string {
  if (!v) return '-'
  if (v >= 10000) return (v / 10000).toFixed(1) + '万'
  return v.toLocaleString()
}

/** 格式化更新时间 */
function formatUpdateTime(time?: string): string {
  if (!time) return '-'
  const d = new Date(time)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

/** 最新更新时间 */
function getLatestUpdateTime(): string {
  if (contracts.value.length === 0) return '-'
  const times = contracts.value.map(c => c.priceUpdateTime).filter(Boolean)
  if (times.length === 0) return '-'
  return formatUpdateTime(times.sort().reverse()[0])
}
</script>

<template>
  <view class="market-page">
    <!-- 品种 tabs -->
    <view class="product-tabs">
      <WgSkeleton v-if="productsLoading" type="list" :rows="1" />
      <scroll-view v-else scroll-x :show-scrollbar="false" class="product-tabs__scroll">
        <view class="product-tabs__list">
          <view
            v-for="p in products"
            :key="p.code"
            class="product-tabs__item"
            :class="{ 'product-tabs__item--active': activeProduct === p.code }"
            @tap="handleProductChange(p.code)"
          >
            <text>{{ p.name }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 更新时间 -->
    <view v-if="contracts.length > 0" class="update-time">
      <text class="update-time__text">更新时间: {{ getLatestUpdateTime() }}</text>
    </view>

    <!-- 合约列表 -->
    <view v-if="contracts.length > 0" class="contract-list">
      <view
        v-for="c in contracts"
        :key="c.contractCode"
        class="contract-card"
      >
        <view class="contract-card__header">
          <text class="contract-card__code">{{ c.contractCode }}</text>
          <text class="contract-card__name">{{ c.contractName }}</text>
          <view
            v-if="c.isTrading !== undefined"
            class="contract-card__status"
            :class="{ 'contract-card__status--trading': c.isTrading }"
          >
            <text>{{ c.isTrading ? '交易中' : '已收盘' }}</text>
          </view>
        </view>

        <view class="contract-card__price-row">
          <text class="contract-card__price">
            {{ c.lastPrice ? '¥' + c.lastPrice.toLocaleString() : '-' }}
          </text>
          <view class="contract-card__change">
            <text
              class="contract-card__change-val"
              :class="{
                'contract-card__change-val--up': isUp(c.changePrice),
                'contract-card__change-val--down': isDown(c.changePrice),
              }"
            >
              {{ formatChange(c.changePrice) }}
            </text>
            <text
              class="contract-card__change-pct"
              :class="{
                'contract-card__change-pct--up': isUp(c.changePrice),
                'contract-card__change-pct--down': isDown(c.changePrice),
              }"
            >
              ({{ formatPercent(c.changePercent) }})
            </text>
          </view>
        </view>

        <view class="contract-card__ohlcv">
          <view class="contract-card__ohlcv-item">
            <text class="contract-card__ohlcv-label">开</text>
            <text class="contract-card__ohlcv-value">{{ c.openPrice || '-' }}</text>
          </view>
          <view class="contract-card__ohlcv-item">
            <text class="contract-card__ohlcv-label">高</text>
            <text class="contract-card__ohlcv-value">{{ c.highPrice || '-' }}</text>
          </view>
          <view class="contract-card__ohlcv-item">
            <text class="contract-card__ohlcv-label">低</text>
            <text class="contract-card__ohlcv-value">{{ c.lowPrice || '-' }}</text>
          </view>
          <view class="contract-card__ohlcv-item">
            <text class="contract-card__ohlcv-label">量</text>
            <text class="contract-card__ohlcv-value">{{ formatVolume(c.volume) }}</text>
          </view>
        </view>

        <view v-if="c.daysToDelivery !== undefined" class="contract-card__delivery">
          <text class="contract-card__delivery-text">距交割: {{ c.daysToDelivery }}天</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <WgEmpty
      v-else-if="!loading && !productsLoading"
      text="暂无行情数据"
      description="稍后再来查看最新价格"
    />

    <!-- 骨架屏 -->
    <WgSkeleton v-if="loading && contracts.length === 0" type="card" :rows="3" />
  </view>
</template>

<style lang="scss" scoped>
.market-page {
  min-height: 100vh;
  background: $bg-page;
}

/* ===== Product tabs ===== */
.product-tabs {
  background: $bg-card;
  border-bottom: 1rpx solid $border-light;

  &__scroll {
    white-space: nowrap;
  }

  &__list {
    display: inline-flex;
    padding: $spacing-sm $spacing-md;
    gap: $spacing-sm;
  }

  &__item {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: $spacing-xs $spacing-md;
    border-radius: 100rpx;
    font-size: $font-md;
    color: $text-secondary;
    background: $bg-page;
    white-space: nowrap;

    &--active {
      background: $brand-600;
      color: #ffffff;
      font-weight: bold;
    }
  }
}

/* ===== Update time ===== */
.update-time {
  padding: $spacing-sm $spacing-md 0;

  &__text {
    font-size: $font-xs;
    color: $text-placeholder;
  }
}

/* ===== Contract list ===== */
.contract-list {
  padding: $spacing-sm;
}

.contract-card {
  background: $bg-card;
  border-radius: $radius-xl;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;
  box-shadow: $shadow-sm;

  &__header {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    margin-bottom: $spacing-sm;
  }

  &__code {
    font-size: $font-md;
    font-weight: 800;
    color: $text-primary;
  }

  &__name {
    font-size: $font-sm;
    color: $text-secondary;
    flex: 1;
  }

  &__status {
    padding: 4rpx 16rpx;
    border-radius: $radius-sm;
    font-size: $font-xs;
    background: $bg-page;
    color: $text-placeholder;

    &--trading {
      background: $brand-50;
      color: $brand-600;
    }
  }

  &__price-row {
    display: flex;
    align-items: baseline;
    gap: $spacing-md;
    margin-bottom: $spacing-sm;
  }

  &__price {
    font-size: $font-2xl;
    font-weight: 800;
    color: $text-primary;
  }

  &__change {
    display: flex;
    align-items: baseline;
    gap: $spacing-xs;
  }

  &__change-val,
  &__change-pct {
    font-size: $font-md;
    font-weight: 600;
    color: $text-secondary;

    &--up {
      color: $color-error;
    }

    &--down {
      color: $color-success;
    }
  }

  &__ohlcv {
    display: flex;
    gap: $spacing-md;
    padding: $spacing-sm 0;
    border-top: 1rpx solid $border-light;
  }

  &__ohlcv-item {
    display: flex;
    gap: $spacing-xs;
  }

  &__ohlcv-label {
    font-size: $font-xs;
    color: $text-placeholder;
  }

  &__ohlcv-value {
    font-size: $font-xs;
    color: $text-secondary;
    font-weight: 600;
  }

  &__delivery {
    padding-top: $spacing-xs;
  }

  &__delivery-text {
    font-size: $font-xs;
    color: $text-placeholder;
  }
}
</style>
