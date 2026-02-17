<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { BRAND_600, WARM_500, ACTION_600, COLOR_ERROR, WHITE } from '../../constants/colors'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import {
  listVehicles,
  createVehicle,
  updateVehicle,
  deleteVehicle,
  setDefaultVehicle,
  type VehicleResponse,
  type VehicleCreateRequest,
} from '../../api/vehicle'

const vehicles = ref<VehicleResponse[]>([])
const loading = ref(false)

// Form state
const showForm = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const editingId = ref<number | null>(null)
const submitting = ref(false)
const form = ref<VehicleCreateRequest>({
  driverName: '',
  driverIdCard: '',
  plateNumber: '',
  driverPhone: '',
  vehicleType: '',
  remark: '',
})

const vehicleTypes = ['厢式货车', '平板车', '自卸车', '罐车', '冷藏车', '其他']

onMounted(() => loadData())

onPullDownRefresh(() => {
  loadData().finally(() => uni.stopPullDownRefresh())
})

async function loadData() {
  loading.value = true
  try {
    vehicles.value = await listVehicles() || []
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function openCreate() {
  formMode.value = 'create'
  editingId.value = null
  form.value = { driverName: '', driverIdCard: '', plateNumber: '', driverPhone: '', vehicleType: '', remark: '' }
  showForm.value = true
}

function openEdit(v: VehicleResponse) {
  formMode.value = 'edit'
  editingId.value = v.id
  form.value = {
    driverName: v.driverName,
    driverIdCard: v.driverIdCard,
    plateNumber: v.plateNumber,
    driverPhone: v.driverPhone,
    vehicleType: v.vehicleType || '',
    remark: v.remark || '',
  }
  showForm.value = true
}

async function handleSubmit() {
  if (!form.value.driverName.trim()) {
    uni.showToast({ title: '请输入司机姓名', icon: 'none' })
    return
  }
  if (!form.value.plateNumber.trim()) {
    uni.showToast({ title: '请输入车牌号', icon: 'none' })
    return
  }
  if (!form.value.driverPhone.trim()) {
    uni.showToast({ title: '请输入司机手机号', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    if (formMode.value === 'create') {
      await createVehicle(form.value)
      uni.showToast({ title: '添加成功', icon: 'success' })
    } else if (editingId.value) {
      await updateVehicle(editingId.value, form.value)
      uni.showToast({ title: '修改成功', icon: 'success' })
    }
    showForm.value = false
    await loadData()
  } catch {
    // handled by request.ts
  } finally {
    submitting.value = false
  }
}

function handleDelete(v: VehicleResponse) {
  uni.showModal({
    title: '删除确认',
    content: `确定删除车辆 ${v.plateNumber} 吗？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await deleteVehicle(v.id)
        uni.showToast({ title: '删除成功', icon: 'success' })
        await loadData()
      } catch {
        // handled
      }
    },
  })
}

async function handleSetDefault(v: VehicleResponse) {
  if (v.isDefault) return
  try {
    await setDefaultVehicle(v.id)
    uni.showToast({ title: '已设为默认', icon: 'success' })
    await loadData()
  } catch {
    // handled
  }
}

/** Mask ID card middle digits */
function maskIdCard(val: string): string {
  if (!val || val.length < 14) return val
  return val.substring(0, 6) + '********' + val.substring(14)
}
</script>

<template>
  <view class="vehicle-page">
    <!-- Vehicle list -->
    <view v-if="vehicles.length > 0" class="vehicle-list">
      <view
        v-for="v in vehicles"
        :key="v.id"
        class="vehicle-card"
        :class="{ 'vehicle-card--default': v.isDefault }"
      >
        <view class="vehicle-card__header">
          <view class="vehicle-card__plate-wrap">
            <WgIcon name="truck" :size="18" :color="BRAND_600" />
            <text class="vehicle-card__plate">{{ v.plateNumber }}</text>
          </view>
          <view v-if="v.isDefault" class="vehicle-card__badge">
            <text class="vehicle-card__badge-text">默认</text>
          </view>
        </view>
        <view class="vehicle-card__info">
          <view class="vehicle-card__row">
            <text class="vehicle-card__label">司机</text>
            <text class="vehicle-card__value">{{ v.driverName }}</text>
          </view>
          <view class="vehicle-card__row">
            <text class="vehicle-card__label">手机</text>
            <text class="vehicle-card__value">{{ v.driverPhone }}</text>
          </view>
          <view v-if="v.driverIdCard" class="vehicle-card__row">
            <text class="vehicle-card__label">身份证</text>
            <text class="vehicle-card__value">{{ maskIdCard(v.driverIdCard) }}</text>
          </view>
          <view v-if="v.vehicleType" class="vehicle-card__row">
            <text class="vehicle-card__label">车型</text>
            <text class="vehicle-card__value">{{ v.vehicleType }}</text>
          </view>
          <view v-if="v.remark" class="vehicle-card__row">
            <text class="vehicle-card__label">备注</text>
            <text class="vehicle-card__value">{{ v.remark }}</text>
          </view>
        </view>
        <view class="vehicle-card__actions">
          <view v-if="!v.isDefault" class="vehicle-card__action" @tap="handleSetDefault(v)">
            <WgIcon name="star" :size="16" color="#D4A373" />
            <text class="vehicle-card__action-text">设为默认</text>
          </view>
          <view class="vehicle-card__action" @tap="openEdit(v)">
            <WgIcon name="square-pen" :size="16" :color="ACTION_600" />
            <text class="vehicle-card__action-text vehicle-card__action-text--blue">编辑</text>
          </view>
          <view class="vehicle-card__action" @tap="handleDelete(v)">
            <WgIcon name="trash" :size="16" :color="COLOR_ERROR" />
            <text class="vehicle-card__action-text vehicle-card__action-text--red">删除</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Empty -->
    <WgEmpty
      v-else-if="!loading"
      text="暂无车辆信息"
      description="添加常用物流车辆，方便合同填写"
      actionText="添加车辆"
      @action="openCreate"
    />

    <!-- Loading -->
    <WgSkeleton v-if="loading && vehicles.length === 0" type="card" :rows="2" />

    <!-- FAB add -->
    <view v-if="vehicles.length > 0" class="fab" @tap="openCreate">
      <WgIcon name="plus" :size="24" :color="WHITE" />
    </view>

    <!-- Form popup -->
    <view v-if="showForm" class="popup-mask" @tap.self="showForm = false">
      <view class="popup-sheet">
        <view class="popup-sheet__header">
          <text class="popup-sheet__title">{{ formMode === 'create' ? '添加车辆' : '编辑车辆' }}</text>
          <view @tap="showForm = false">
            <WgIcon name="clear" :size="22" :color="WARM_500" />
          </view>
        </view>
        <scroll-view scroll-y class="popup-sheet__body">
          <view class="form-group">
            <text class="form-label">车牌号 <text class="form-required">*</text></text>
            <input v-model="form.plateNumber" class="form-input" placeholder="如：京A12345" />
          </view>
          <view class="form-group">
            <text class="form-label">司机姓名 <text class="form-required">*</text></text>
            <input v-model="form.driverName" class="form-input" placeholder="请输入司机姓名" />
          </view>
          <view class="form-group">
            <text class="form-label">司机手机号 <text class="form-required">*</text></text>
            <input v-model="form.driverPhone" class="form-input" type="number" placeholder="请输入手机号" />
          </view>
          <view class="form-group">
            <text class="form-label">身份证号</text>
            <input v-model="form.driverIdCard" class="form-input" placeholder="选填" />
          </view>
          <view class="form-group">
            <text class="form-label">车辆类型</text>
            <view class="form-pills">
              <text
                v-for="t in vehicleTypes"
                :key="t"
                class="form-pill"
                :class="{ 'form-pill--active': form.vehicleType === t }"
                @tap="form.vehicleType = form.vehicleType === t ? '' : t"
              >{{ t }}</text>
            </view>
          </view>
          <view class="form-group">
            <text class="form-label">备注</text>
            <textarea
              v-model="form.remark"
              class="form-textarea"
              placeholder="选填，如载重量等"
              :maxlength="200"
            />
          </view>
        </scroll-view>
        <view class="popup-sheet__footer">
          <view class="btn-submit" :class="{ 'btn-submit--disabled': submitting }" @tap="handleSubmit">
            <text class="btn-submit__text">{{ submitting ? '提交中...' : '确认提交' }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.vehicle-page {
  min-height: 100vh;
  background: $bg-page;
  padding: $spacing-md;
  padding-bottom: 180rpx;
}

/* Vehicle card */
.vehicle-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.vehicle-card {
  background: $bg-card;
  border-radius: $radius-xl;
  overflow: hidden;
  box-shadow: $shadow-warm-card;

  &--default {
    border: 2rpx solid $brand-200;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-lg $spacing-lg $spacing-sm;
  }

  &__plate-wrap {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
  }

  &__plate {
    font-size: $font-xl;
    font-weight: bold;
    color: $text-primary;
  }

  &__badge {
    background: $brand-50;
    padding: 4rpx 16rpx;
    border-radius: $radius-pill;
  }

  &__badge-text {
    font-size: $font-xs;
    color: $brand-600;
    font-weight: 600;
  }

  &__info {
    padding: 0 $spacing-lg $spacing-sm;
  }

  &__row {
    display: flex;
    align-items: center;
    padding: $spacing-xs 0;
  }

  &__label {
    font-size: $font-sm;
    color: $text-placeholder;
    width: 120rpx;
    flex-shrink: 0;
  }

  &__value {
    font-size: $font-md;
    color: $text-primary;
    flex: 1;
  }

  &__actions {
    display: flex;
    border-top: 1rpx solid $border-light;
  }

  &__action {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-xs;
    padding: $spacing-md 0;
    border-right: 1rpx solid $border-light;

    &:last-child {
      border-right: none;
    }

    &:active {
      background: $warm-100;
    }
  }

  &__action-text {
    font-size: $font-sm;
    color: $autumn-500;

    &--blue {
      color: $action-600;
    }

    &--red {
      color: $color-error;
    }
  }
}

/* FAB */
.fab {
  position: fixed;
  right: 32rpx;
  bottom: 64rpx;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: $brand-600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 20rpx rgba(45, 106, 79, 0.3);
  z-index: 20;

  &:active {
    transform: scale(0.92);
  }
}

/* Popup */
.popup-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 100;
  display: flex;
  align-items: flex-end;
}

.popup-sheet {
  width: 100%;
  max-height: 85vh;
  background: $bg-card;
  border-radius: $radius-xl $radius-xl 0 0;
  display: flex;
  flex-direction: column;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-lg;
    border-bottom: 1rpx solid $border-light;
  }

  &__title {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;
  }

  &__body {
    flex: 1;
    padding: $spacing-md $spacing-lg;
    max-height: 60vh;
  }

  &__footer {
    padding: $spacing-md $spacing-lg;
    padding-bottom: calc($spacing-md + env(safe-area-inset-bottom));
    border-top: 1rpx solid $border-light;
  }
}

/* Form */
.form-group {
  margin-bottom: $spacing-lg;
}

.form-label {
  display: block;
  font-size: $font-sm;
  color: $text-secondary;
  margin-bottom: $spacing-xs;
  font-weight: 500;
}

.form-required {
  color: $color-error;
}

.form-input {
  width: 100%;
  height: 80rpx;
  padding: 0 $spacing-md;
  border: 2rpx solid $warm-200;
  border-radius: $radius-lg;
  font-size: $font-md;
  color: $text-primary;
  background: $bg-card;

  &:focus {
    border-color: $brand-500;
  }
}

.form-textarea {
  width: 100%;
  min-height: 120rpx;
  padding: $spacing-sm $spacing-md;
  border: 2rpx solid $warm-200;
  border-radius: $radius-lg;
  font-size: $font-md;
  color: $text-primary;
  background: $bg-card;
}

.form-pills {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-xs;
}

.form-pill {
  font-size: $font-sm;
  padding: $spacing-xs $spacing-md;
  border-radius: $radius-pill;
  background: $warm-100;
  color: $text-secondary;

  &--active {
    background: $brand-50;
    color: $brand-600;
    font-weight: 600;
  }
}

/* Submit button */
.btn-submit {
  background: $brand-600;
  border-radius: $radius-lg;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;

  &:active {
    opacity: 0.85;
  }

  &--disabled {
    opacity: 0.6;
    pointer-events: none;
  }

  &__text {
    color: $text-inverse;
    font-size: $font-md;
    font-weight: bold;
  }
}
</style>
