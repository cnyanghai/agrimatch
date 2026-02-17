<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  listMilestones,
  createMilestone,
  submitMilestone,
  confirmMilestone,
  rejectMilestone,
  deleteMilestone,
  generateStandardMilestones,
  type MilestoneResponse,
} from '../../api/milestone'
import { useAuthStore } from '../../store/auth'
import { formatDate } from '../../utils/format'

const authStore = useAuthStore()
const contractId = ref(0)
const milestones = ref<MilestoneResponse[]>([])
const loading = ref(true)

onLoad(async (options) => {
  if (options?.contractId) {
    contractId.value = Number(options.contractId)
    await loadData()
  }
})

async function loadData() {
  loading.value = true
  try {
    const res = await listMilestones(contractId.value)
    milestones.value = res || []
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function getStatusIcon(status: string): string {
  const map: Record<string, string> = {
    CONFIRMED: '✅',
    SUBMITTED: '📤',
    PENDING: '⏳',
    REJECTED: '❌',
  }
  return map[status] || '⏳'
}

function getStatusLabel(status: string): string {
  const map: Record<string, string> = {
    CONFIRMED: '已确认',
    SUBMITTED: '已提交',
    PENDING: '待执行',
    REJECTED: '已驳回',
  }
  return map[status] || status
}

function getDotClass(status: string): string {
  const map: Record<string, string> = {
    CONFIRMED: 'timeline__dot--confirmed',
    SUBMITTED: 'timeline__dot--submitted',
    PENDING: '',
    REJECTED: 'timeline__dot--rejected',
  }
  return map[status] || ''
}

// --- Actions ---
async function handleSubmit(ms: MilestoneResponse) {
  uni.showModal({
    title: '提交完成',
    content: '请输入实际完成日期备注',
    editable: true,
    placeholderText: '备注信息（可选）',
    success: async (res) => {
      if (!res.confirm) return
      try {
        const today = new Date().toISOString().split('T')[0]
        await submitMilestone(ms.id, {
          actualDate: today,
          remark: res.content?.trim() || undefined,
        })
        uni.showToast({ title: '已提交', icon: 'success' })
        await loadData()
      } catch {
        // handled
      }
    },
  })
}

async function handleConfirm(ms: MilestoneResponse) {
  uni.showModal({
    title: '确认节点',
    content: `确认"${ms.milestoneName}"已完成？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await confirmMilestone(ms.id)
        uni.showToast({ title: '已确认', icon: 'success' })
        await loadData()
      } catch {
        // handled
      }
    },
  })
}

async function handleReject(ms: MilestoneResponse) {
  uni.showModal({
    title: '驳回节点',
    content: '请输入驳回原因',
    editable: true,
    placeholderText: '请输入驳回原因',
    success: async (res) => {
      if (!res.confirm) return
      const reason = res.content?.trim()
      if (!reason) {
        uni.showToast({ title: '请输入驳回原因', icon: 'none' })
        return
      }
      try {
        await rejectMilestone(ms.id, reason)
        uni.showToast({ title: '已驳回', icon: 'none' })
        await loadData()
      } catch {
        // handled
      }
    },
  })
}

async function handleDelete(ms: MilestoneResponse) {
  uni.showModal({
    title: '删除节点',
    content: `确认删除"${ms.milestoneName}"？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await deleteMilestone(ms.id)
        uni.showToast({ title: '已删除', icon: 'none' })
        await loadData()
      } catch {
        // handled
      }
    },
  })
}

async function handleGenerate() {
  uni.showModal({
    title: '自动生成标准节点',
    content: '将根据合同类型自动生成发货、收货、付款等标准节点。',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await generateStandardMilestones(contractId.value)
        uni.showToast({ title: '已生成', icon: 'success' })
        await loadData()
      } catch {
        // handled
      }
    },
  })
}

async function handleAddCustom() {
  uni.showModal({
    title: '添加自定义节点',
    content: '请输入节点名称',
    editable: true,
    placeholderText: '例如：质量检测',
    success: async (res) => {
      if (!res.confirm) return
      const name = res.content?.trim()
      if (!name) {
        uni.showToast({ title: '请输入节点名称', icon: 'none' })
        return
      }
      try {
        await createMilestone(contractId.value, {
          milestoneType: 'CUSTOM',
          responsibleParty: 'BUYER',
          milestoneName: name,
        })
        uni.showToast({ title: '已添加', icon: 'success' })
        await loadData()
      } catch {
        // handled
      }
    },
  })
}
</script>

<template>
  <view class="milestones-page">
    <!-- Header actions -->
    <view v-if="milestones.length === 0 && !loading" class="generate-bar">
      <view class="generate-bar__btn" @tap="handleGenerate">
        <text class="generate-bar__text">自动生成标准节点</text>
      </view>
    </view>

    <WgSkeleton v-if="loading" type="list" :rows="4" />

    <WgEmpty
      v-else-if="milestones.length === 0"
      text="暂无履约节点"
      description="点击上方按钮自动生成标准节点"
    />

    <!-- Timeline -->
    <view v-else class="timeline">
      <view
        v-for="(ms, index) in milestones"
        :key="ms.id"
        class="timeline__item"
      >
        <!-- Vertical line -->
        <view class="timeline__line-wrap">
          <view class="timeline__dot" :class="getDotClass(ms.status)" />
          <view v-if="index < milestones.length - 1" class="timeline__line" />
        </view>

        <!-- Content -->
        <view class="timeline__content">
          <view class="timeline__header">
            <text class="timeline__name">
              {{ ms.milestoneName }}
              <text v-if="ms.responsibleParty" class="timeline__party">
                ({{ ms.responsibleParty === 'SELLER' ? '卖方' : '买方' }})
              </text>
            </text>
            <text class="timeline__status">
              {{ getStatusIcon(ms.status) }} {{ getStatusLabel(ms.status) }}
            </text>
          </view>

          <view class="timeline__dates">
            <text v-if="ms.expectedDate" class="timeline__date">
              预期 {{ formatDate(ms.expectedDate) }}
            </text>
            <text v-if="ms.actualDate" class="timeline__date timeline__date--actual">
              实际 {{ formatDate(ms.actualDate) }}
            </text>
          </view>

          <text v-if="ms.remark" class="timeline__remark">{{ ms.remark }}</text>
          <text v-if="ms.rejectReason" class="timeline__reject">
            驳回原因：{{ ms.rejectReason }}
          </text>

          <!-- Actions -->
          <view class="timeline__actions">
            <view
              v-if="ms.status === 'PENDING' || ms.status === 'REJECTED'"
              class="timeline__action timeline__action--primary"
              @tap="handleSubmit(ms)"
            >
              <text class="timeline__action-text timeline__action-text--primary">提交完成</text>
            </view>
            <view
              v-if="ms.status === 'SUBMITTED'"
              class="timeline__action timeline__action--primary"
              @tap="handleConfirm(ms)"
            >
              <text class="timeline__action-text timeline__action-text--primary">确认</text>
            </view>
            <view
              v-if="ms.status === 'SUBMITTED'"
              class="timeline__action timeline__action--danger"
              @tap="handleReject(ms)"
            >
              <text class="timeline__action-text timeline__action-text--danger">驳回</text>
            </view>
            <view
              v-if="ms.status === 'PENDING'"
              class="timeline__action timeline__action--muted"
              @tap="handleDelete(ms)"
            >
              <text class="timeline__action-text timeline__action-text--muted">删除</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- Add custom -->
    <view v-if="milestones.length > 0" class="add-bar">
      <view class="add-bar__btn" @tap="handleAddCustom">
        <text class="add-bar__text">+ 添加自定义节点</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.milestones-page {
  min-height: 100vh;
  background: $bg-page;
  padding: $spacing-md;
}

.generate-bar {
  margin-bottom: $spacing-md;

  &__btn {
    width: 100%;
    height: 80rpx;
    background: $brand-600;
    border-radius: $radius-lg;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: transform $transition-fast;

    &:active {
      transform: scale(0.95);
    }
  }

  &__text {
    font-size: $font-md;
    font-weight: 600;
    color: $text-inverse;
  }
}

/* ===== Timeline ===== */
.timeline {
  &__item {
    display: flex;
    gap: $spacing-md;
  }

  &__line-wrap {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 32rpx;
    flex-shrink: 0;
  }

  &__dot {
    width: 24rpx;
    height: 24rpx;
    border-radius: 50%;
    background: $border-color;
    flex-shrink: 0;
    margin-top: 6rpx;

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

  &__line {
    width: 4rpx;
    flex: 1;
    background: $border-light;
    margin: 8rpx 0;
  }

  &__content {
    flex: 1;
    background: $bg-card;
    border-radius: $radius-lg;
    padding: $spacing-md;
    margin-bottom: $spacing-md;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-xs;
  }

  &__name {
    font-size: $font-md;
    font-weight: 600;
    color: $text-primary;
    flex: 1;
  }

  &__party {
    font-size: $font-xs;
    color: $text-secondary;
    font-weight: normal;
  }

  &__status {
    font-size: $font-xs;
    color: $text-secondary;
    flex-shrink: 0;
    margin-left: $spacing-sm;
  }

  &__dates {
    display: flex;
    gap: $spacing-md;
    margin-bottom: $spacing-xs;
  }

  &__date {
    font-size: $font-sm;
    color: $text-placeholder;

    &--actual {
      color: $brand-600;
      font-weight: 600;
    }
  }

  &__remark {
    font-size: $font-sm;
    color: $text-secondary;
    display: block;
    margin-bottom: $spacing-xs;
  }

  &__reject {
    font-size: $font-sm;
    color: $color-error;
    display: block;
    margin-bottom: $spacing-xs;
  }

  &__actions {
    display: flex;
    gap: $spacing-sm;
    margin-top: $spacing-xs;
  }

  &__action {
    height: 56rpx;
    padding: 0 $spacing-md;
    border-radius: $radius-md;
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

    &--danger {
      background: $bg-page;
      border: 1rpx solid $color-error;
    }

    &--muted {
      background: $bg-page;
      border: 1rpx solid $border-color;
    }
  }

  &__action-text {
    font-size: $font-sm;
    font-weight: 600;

    &--primary {
      color: $text-inverse;
    }

    &--danger {
      color: $color-error;
    }

    &--muted {
      color: $text-secondary;
    }
  }
}

.add-bar {
  margin-top: $spacing-sm;

  &__btn {
    width: 100%;
    height: 80rpx;
    background: $bg-card;
    border: 2rpx dashed $border-color;
    border-radius: $radius-lg;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__text {
    font-size: $font-md;
    color: $text-secondary;
  }
}
</style>
