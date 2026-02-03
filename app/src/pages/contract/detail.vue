<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  getContract,
  contractStatusMap,
  paymentMethodMap,
  type ContractResponse,
} from '../../api/contract'
import { formatAmount, formatPrice, formatDate, formatDateTime } from '../../utils/format'

const detail = ref<ContractResponse | null>(null)
const loading = ref(true)

onLoad(async (options) => {
  if (options?.id) {
    try {
      detail.value = await getContract(Number(options.id))
    } catch {
      // handled by request.ts
    } finally {
      loading.value = false
    }
  }
})

function getStatusLabel(status: number): string {
  return contractStatusMap[status]?.label || '未知'
}

function getStatusColor(status: number): string {
  return contractStatusMap[status]?.color || '#999'
}

function formatPayment(method?: string): string {
  if (!method) return '-'
  return paymentMethodMap[method] || method
}

/** 里程碑进度百分比 */
function milestonePercent(): number {
  if (!detail.value?.milestoneTotal) return 0
  return Math.round(
    ((detail.value.milestoneCompleted || 0) / detail.value.milestoneTotal) * 100,
  )
}
</script>

<template>
  <view class="detail-page">
    <!-- 加载中 -->
    <WgSkeleton v-if="loading" type="detail" />

    <!-- 不存在 -->
    <WgEmpty v-else-if="!detail" text="合同信息不存在" icon="empty" />

    <template v-else>
      <!-- 状态头部 -->
      <view class="status-header" :style="{ backgroundColor: getStatusColor(detail.status) }">
        <text class="status-header__label">{{ getStatusLabel(detail.status) }}</text>
        <text class="status-header__no">合同编号：{{ detail.contractNo }}</text>
      </view>

      <!-- 金额卡片 -->
      <view class="amount-card">
        <view class="amount-card__row">
          <text class="amount-card__label">合同总额</text>
          <text class="amount-card__value">{{ formatAmount(detail.totalAmount) }}</text>
        </view>
        <view class="amount-card__sub">
          <text class="amount-card__detail">
            单价 {{ formatPrice(detail.unitPrice) }} x {{ detail.quantity || '-' }}{{ detail.unit || '' }}
          </text>
        </view>
      </view>

      <!-- 商品信息 -->
      <view class="info-section">
        <text class="info-section__title">商品信息</text>
        <view class="info-row">
          <text class="info-row__label">商品名称</text>
          <text class="info-row__value">{{ detail.productName || '-' }}</text>
        </view>
        <view class="info-row" v-if="detail.categoryName">
          <text class="info-row__label">商品分类</text>
          <text class="info-row__value">{{ detail.categoryName }}</text>
        </view>
        <view class="info-row">
          <text class="info-row__label">数量</text>
          <text class="info-row__value">{{ detail.quantity || '-' }}{{ detail.unit || '' }}</text>
        </view>
        <view class="info-row">
          <text class="info-row__label">单价</text>
          <text class="info-row__value">{{ formatPrice(detail.unitPrice) }}</text>
        </view>
      </view>

      <!-- 交易双方 -->
      <view class="info-section">
        <text class="info-section__title">交易双方</text>
        <view class="party-block">
          <text class="party-block__role party-block__role--seller">卖方</text>
          <view class="party-block__detail">
            <text class="party-block__name">{{ detail.sellerCompanyName || '-' }}</text>
            <text class="party-block__contact" v-if="detail.sellerContacts">
              {{ detail.sellerContacts }}
              <text v-if="detail.sellerPhone"> · {{ detail.sellerPhone }}</text>
            </text>
          </view>
          <view class="party-block__sign">
            <text
              class="party-block__sign-tag"
              :class="detail.sellerSigned ? 'party-block__sign-tag--done' : 'party-block__sign-tag--pending'"
            >
              {{ detail.sellerSigned ? '已签署' : '待签署' }}
            </text>
          </view>
        </view>
        <view class="party-block">
          <text class="party-block__role party-block__role--buyer">买方</text>
          <view class="party-block__detail">
            <text class="party-block__name">{{ detail.buyerCompanyName || '-' }}</text>
            <text class="party-block__contact" v-if="detail.buyerContacts">
              {{ detail.buyerContacts }}
              <text v-if="detail.buyerPhone"> · {{ detail.buyerPhone }}</text>
            </text>
          </view>
          <view class="party-block__sign">
            <text
              class="party-block__sign-tag"
              :class="detail.buyerSigned ? 'party-block__sign-tag--done' : 'party-block__sign-tag--pending'"
            >
              {{ detail.buyerSigned ? '已签署' : '待签署' }}
            </text>
          </view>
        </view>
      </view>

      <!-- 交付信息 -->
      <view class="info-section">
        <text class="info-section__title">交付信息</text>
        <view class="info-row">
          <text class="info-row__label">交付日期</text>
          <text class="info-row__value">{{ formatDate(detail.deliveryDate) }}</text>
        </view>
        <view class="info-row" v-if="detail.deliveryAddress">
          <text class="info-row__label">交付地址</text>
          <text class="info-row__value">{{ detail.deliveryAddress }}</text>
        </view>
        <view class="info-row" v-if="detail.deliveryMode">
          <text class="info-row__label">交货方式</text>
          <text class="info-row__value">{{ detail.deliveryMode }}</text>
        </view>
        <view class="info-row">
          <text class="info-row__label">付款方式</text>
          <text class="info-row__value">{{ formatPayment(detail.paymentMethod) }}</text>
        </view>
      </view>

      <!-- 履约进度 -->
      <view class="info-section" v-if="detail.milestoneTotal">
        <text class="info-section__title">履约进度</text>
        <view class="milestone">
          <view class="milestone__bar">
            <view
              class="milestone__fill"
              :style="{ width: milestonePercent() + '%' }"
            />
          </view>
          <view class="milestone__info">
            <text class="milestone__text">
              {{ detail.milestoneCompleted || 0 }} / {{ detail.milestoneTotal }} 项已完成
            </text>
            <text class="milestone__percent">{{ milestonePercent() }}%</text>
          </view>
        </view>
      </view>

      <!-- 备注 -->
      <view class="info-section" v-if="detail.remark">
        <text class="info-section__title">备注</text>
        <view class="remark-block">
          <text class="remark-block__text">{{ detail.remark }}</text>
        </view>
      </view>

      <!-- 时间信息 -->
      <view class="info-section info-section--last">
        <text class="info-section__title">时间记录</text>
        <view class="info-row">
          <text class="info-row__label">创建时间</text>
          <text class="info-row__value">{{ formatDateTime(detail.createTime) }}</text>
        </view>
        <view class="info-row">
          <text class="info-row__label">更新时间</text>
          <text class="info-row__value">{{ formatDateTime(detail.updateTime) }}</text>
        </view>
      </view>
    </template>
  </view>
</template>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: env(safe-area-inset-bottom);
}

/* ===== 状态头部 ===== */
.status-header {
  padding: $spacing-xl $spacing-lg $spacing-lg;
  color: #fff;

  &__label {
    font-size: $font-2xl;
    font-weight: bold;
    display: block;
    margin-bottom: $spacing-xs;
  }

  &__no {
    font-size: $font-sm;
    opacity: 0.85;
  }
}

/* ===== 金额卡片 ===== */
.amount-card {
  background: $bg-card;
  margin: -#{$spacing-md} $spacing-sm $spacing-sm;
  border-radius: $radius-lg;
  padding: $spacing-lg;
  position: relative;
  z-index: 1;

  &__row {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
  }

  &__label {
    font-size: $font-md;
    color: $text-secondary;
  }

  &__value {
    font-size: 56rpx;
    font-weight: bold;
    color: $accent-400;
  }

  &__sub {
    margin-top: $spacing-xs;
  }

  &__detail {
    font-size: $font-sm;
    color: $text-placeholder;
  }
}

/* ===== 信息分区 ===== */
.info-section {
  background: $bg-card;
  margin: 0 $spacing-sm $spacing-sm;
  border-radius: $radius-lg;
  padding: $spacing-md $spacing-lg $spacing-sm;

  &--last {
    margin-bottom: $spacing-xl;
  }

  &__title {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
    display: block;
    margin-bottom: $spacing-sm;
  }
}

.info-row {
  display: flex;
  padding: $spacing-xs 0;
  border-bottom: 1rpx solid $border-light;

  &:last-child {
    border-bottom: none;
  }

  &__label {
    width: 160rpx;
    font-size: $font-md;
    color: $text-secondary;
    flex-shrink: 0;
  }

  &__value {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;
    text-align: right;
  }
}

/* ===== 交易双方 ===== */
.party-block {
  display: flex;
  align-items: center;
  padding: $spacing-sm 0;
  border-bottom: 1rpx solid $border-light;

  &:last-child {
    border-bottom: none;
  }

  &__role {
    font-size: $font-xs;
    padding: 4rpx 16rpx;
    border-radius: $radius-sm;
    flex-shrink: 0;
    margin-right: $spacing-sm;

    &--seller {
      color: $brand-600;
      background: $brand-50;
    }

    &--buyer {
      color: $autumn-400;
      background: rgba($autumn-400, 0.1);
    }
  }

  &__detail {
    flex: 1;
    overflow: hidden;
  }

  &__name {
    font-size: $font-md;
    color: $text-primary;
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__contact {
    font-size: $font-xs;
    color: $text-secondary;
    display: block;
    margin-top: 4rpx;
  }

  &__sign {
    flex-shrink: 0;
    margin-left: $spacing-sm;
  }

  &__sign-tag {
    font-size: $font-xs;
    padding: 4rpx 14rpx;
    border-radius: $radius-sm;

    &--done {
      color: $color-success;
      background: rgba($color-success, 0.1);
    }

    &--pending {
      color: $color-warning;
      background: rgba($color-warning, 0.1);
    }
  }
}

/* ===== 履约进度 ===== */
.milestone {
  padding-bottom: $spacing-sm;

  &__bar {
    height: 16rpx;
    background: $border-light;
    border-radius: 8rpx;
    overflow: hidden;
  }

  &__fill {
    height: 100%;
    background: $brand-600;
    border-radius: 8rpx;
    transition: width 0.3s ease;
  }

  &__info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: $spacing-xs;
  }

  &__text {
    font-size: $font-sm;
    color: $text-secondary;
  }

  &__percent {
    font-size: $font-sm;
    font-weight: bold;
    color: $brand-600;
  }
}

/* ===== 备注 ===== */
.remark-block {
  background: $bg-page;
  border-radius: $radius-md;
  padding: $spacing-sm $spacing-md;
  margin-bottom: $spacing-sm;

  &__text {
    font-size: $font-md;
    color: $text-primary;
    line-height: 1.6;
  }
}
</style>
