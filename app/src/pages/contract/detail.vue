<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  getContract,
  contractStatusMap,
  paymentMethodMap,
  type ContractResponse,
} from '../../api/contract'
import { listMilestones, type MilestoneResponse } from '../../api/milestone'
import { useContractActions } from '../../composables/useContractActions'
import { formatAmount, formatPrice, formatDate, formatDateTime } from '../../utils/format'

const detail = ref<ContractResponse | null>(null)
const loading = ref(true)
const milestones = ref<MilestoneResponse[]>([])

const {
  canEdit,
  canSend,
  canSign,
  canCancel,
  canAddMilestone,
  handleSend,
  handleSign,
  handleCancel,
  handleDelete,
} = useContractActions(detail)

onLoad(async (options) => {
  if (options?.id) {
    try {
      detail.value = await getContract(Number(options.id))
      loadMilestones()
    } catch {
      // handled by request.ts
    } finally {
      loading.value = false
    }
  }
})

async function loadMilestones() {
  if (!detail.value) return
  try {
    const res = await listMilestones(detail.value.id)
    milestones.value = res || []
  } catch {
    // silent
  }
}

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

function getMilestoneStatusIcon(status: string): string {
  const map: Record<string, string> = {
    CONFIRMED: '✅',
    SUBMITTED: '📤',
    PENDING: '⏳',
    REJECTED: '❌',
  }
  return map[status] || '⏳'
}

function getMilestoneStatusLabel(status: string): string {
  const map: Record<string, string> = {
    CONFIRMED: '已确认',
    SUBMITTED: '已提交',
    PENDING: '待执行',
    REJECTED: '已驳回',
  }
  return map[status] || status
}

const previewMilestones = ref<MilestoneResponse[]>([])
function updatePreview() {
  previewMilestones.value = milestones.value.slice(0, 4)
}
// Watch milestones change
import { watch } from 'vue'
watch(milestones, updatePreview, { immediate: true })

function goMilestones() {
  if (!detail.value) return
  uni.navigateTo({ url: `/pages/contract/milestones?contractId=${detail.value.id}` })
}

function goEdit() {
  // For now, navigate to detail with edit mode in the future
  uni.showToast({ title: '编辑功能开发中', icon: 'none' })
}

// Action handlers with reload
async function onSend() {
  const ok = await handleSend()
  if (ok && detail.value) {
    detail.value = await getContract(detail.value.id)
  }
}

async function onSign() {
  const ok = await handleSign()
  if (ok && detail.value) {
    detail.value = await getContract(detail.value.id)
  }
}

async function onCancel() {
  const ok = await handleCancel()
  if (ok && detail.value) {
    detail.value = await getContract(detail.value.id)
  }
}

const hasActions = ref(true)
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

      <!-- 履约节点预览 -->
      <view class="info-section" v-if="milestones.length > 0">
        <text class="info-section__title">履约节点</text>
        <view
          v-for="ms in previewMilestones"
          :key="ms.id"
          class="milestone-item"
        >
          <view
            class="milestone-item__dot"
            :class="{
              'milestone-item__dot--confirmed': ms.status === 'CONFIRMED',
              'milestone-item__dot--submitted': ms.status === 'SUBMITTED',
              'milestone-item__dot--rejected': ms.status === 'REJECTED',
            }"
          />
          <text class="milestone-item__name">{{ ms.milestoneName }}</text>
          <text class="milestone-item__date">
            {{ ms.actualDate || ms.expectedDate || '' }}
          </text>
          <text class="milestone-item__status">
            {{ getMilestoneStatusIcon(ms.status) }} {{ getMilestoneStatusLabel(ms.status) }}
          </text>
        </view>
        <view class="milestone-more" @tap="goMilestones">
          <text class="milestone-more__text">查看全部节点 →</text>
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
      <view class="info-section">
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

      <!-- 底部占位 -->
      <view class="bottom-placeholder" />

      <!-- 操作栏 -->
      <view
        v-if="canEdit || canSend || canSign || canCancel"
        class="action-bar safe-area-bottom"
      >
        <view v-if="canEdit" class="action-bar__btn action-bar__btn--secondary" @tap="goEdit">
          <text class="action-bar__btn-text action-bar__btn-text--secondary">编辑</text>
        </view>
        <view v-if="canEdit" class="action-bar__btn action-bar__btn--danger" @tap="handleDelete">
          <text class="action-bar__btn-text action-bar__btn-text--danger">删除</text>
        </view>
        <view v-if="canSend" class="action-bar__btn action-bar__btn--primary" @tap="onSend">
          <text class="action-bar__btn-text action-bar__btn-text--primary">发送签署</text>
        </view>
        <view v-if="canSign" class="action-bar__btn action-bar__btn--primary" @tap="onSign">
          <text class="action-bar__btn-text action-bar__btn-text--primary">签署</text>
        </view>
        <view v-if="canCancel && !canEdit" class="action-bar__btn action-bar__btn--danger" @tap="onCancel">
          <text class="action-bar__btn-text action-bar__btn-text--danger">取消合同</text>
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

/* ===== 履约节点预览 ===== */
.milestone-item {
  display: flex;
  align-items: center;
  padding: $spacing-xs 0;
  gap: $spacing-sm;

  &__dot {
    width: 16rpx;
    height: 16rpx;
    border-radius: 50%;
    background: $border-color;
    flex-shrink: 0;

    &--confirmed {
      background: $brand-600;
    }
    &--submitted {
      background: $action-600;
    }
    &--rejected {
      background: $color-error;
    }
  }

  &__name {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__date {
    font-size: $font-xs;
    color: $text-placeholder;
    flex-shrink: 0;
  }

  &__status {
    font-size: $font-xs;
    color: $text-secondary;
    flex-shrink: 0;
  }
}

.milestone-more {
  padding: $spacing-sm 0;
  text-align: center;

  &__text {
    font-size: $font-sm;
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

/* ===== 底部占位 ===== */
.bottom-placeholder {
  height: 140rpx;
}

/* ===== 操作栏 ===== */
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  background: $bg-card;
  border-top: 1rpx solid $border-light;
  z-index: 10;

  &__btn {
    flex: 1;
    height: 80rpx;
    border-radius: $radius-lg;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: transform $transition-fast;

    &:active {
      transform: scale(0.95);
    }

    &--primary {
      background: $brand-600;
    }

    &--secondary {
      background: $bg-page;
      border: 1rpx solid $border-color;
    }

    &--danger {
      background: $bg-page;
      border: 1rpx solid $color-error;
    }
  }

  &__btn-text {
    font-size: $font-md;
    font-weight: 600;

    &--primary {
      color: #fff;
    }

    &--secondary {
      color: $text-primary;
    }

    &--danger {
      color: $color-error;
    }
  }
}
</style>
