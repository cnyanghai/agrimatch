<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getRequirement, updateRequirement, type RequirementResponse, type RequirementUpdateRequest } from '../../api/requirement'

const requirementId = ref(0)
const original = ref<RequirementResponse | null>(null)
const submitting = ref(false)
const loading = ref(true)

const form = ref<RequirementUpdateRequest>({
  quantity: undefined,
  expectedPrice: undefined,
  packaging: '',
  paymentMethod: '',
  deliveryMethod: '',
  purchaseAddress: '',
  remark: '',
})

const packagingOptions = ['袋装', '散装', '桶装', '箱装', '协商']
const deliveryMethodOptions = ['送货上门', '自提', '物流运输', '协商']
const paymentMethodOptions = ['款到发货', '货到付款', '月结30天', '月结60天', '协商']

onLoad((query) => {
  if (query?.id) {
    requirementId.value = Number(query.id)
    loadDetail()
  }
})

async function loadDetail() {
  loading.value = true
  try {
    const res = await getRequirement(requirementId.value)
    if (res) {
      original.value = res
      form.value = {
        quantity: res.quantity,
        expectedPrice: res.expectedPrice,
        packaging: res.packaging || '',
        paymentMethod: res.paymentMethod || '',
        deliveryMethod: res.deliveryMethod || '',
        purchaseAddress: res.purchaseAddress || '',
        remark: res.remark || '',
      }
    }
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const errors = ref<Record<string, string>>({})

function clearError(field: string) {
  delete errors.value[field]
}

function validate(): boolean {
  const newErrors: Record<string, string> = {}
  if (form.value.quantity !== undefined && form.value.quantity !== null) {
    const qty = Number(form.value.quantity)
    if (isNaN(qty) || qty <= 0) newErrors.quantity = '数量必须大于0'
  }
  if (form.value.expectedPrice !== undefined && form.value.expectedPrice !== null) {
    const price = Number(form.value.expectedPrice)
    if (isNaN(price) || price < 0) newErrors.expectedPrice = '价格不能为负数'
  }
  errors.value = newErrors
  return Object.keys(newErrors).length === 0
}

const canSubmit = computed(() => !submitting.value && !loading.value)

function handlePackagingPick(e: any) {
  form.value.packaging = packagingOptions[e.detail.value]
}

function handleDeliveryMethodPick(e: any) {
  form.value.deliveryMethod = deliveryMethodOptions[e.detail.value]
}

function handlePaymentMethodPick(e: any) {
  form.value.paymentMethod = paymentMethodOptions[e.detail.value]
}

async function handleSubmit() {
  if (!validate()) {
    uni.showToast({ title: '请检查填写内容', icon: 'none' })
    return
  }
  if (submitting.value) return
  submitting.value = true
  try {
    await updateRequirement(requirementId.value, {
      quantity: form.value.quantity || undefined,
      expectedPrice: form.value.expectedPrice || undefined,
      packaging: form.value.packaging?.trim() || undefined,
      paymentMethod: form.value.paymentMethod || undefined,
      deliveryMethod: form.value.deliveryMethod || undefined,
      purchaseAddress: form.value.purchaseAddress?.trim() || undefined,
      remark: form.value.remark?.trim() || undefined,
    })
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1000)
  } catch {
    // handled by request.ts
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="edit-page">
    <WgNavBar title="编辑采购" />

    <!-- Loading -->
    <view v-if="loading" class="edit-page__loading">
      <text class="edit-page__loading-text">加载中...</text>
    </view>

    <template v-else-if="original">
      <view class="form-card">
        <!-- 商品名称（只读） -->
        <view class="form-card__field">
          <text class="form-card__label">商品名称</text>
          <view class="form-card__readonly">
            <text class="form-card__readonly-text">{{ original.categoryName }}</text>
          </view>
        </view>

        <!-- 数量 -->
        <view class="form-card__field" :class="{ 'form-card__field--error': errors.quantity }">
          <text class="form-card__label">数量</text>
          <view class="form-card__input-row">
            <input
              class="form-card__input form-card__input--flex"
              :class="{ 'form-card__input--error': errors.quantity }"
              v-model="form.quantity"
              type="digit"
              placeholder="请输入数量"
              @input="clearError('quantity')"
            />
            <text class="form-card__unit">吨</text>
          </view>
          <text v-if="errors.quantity" class="form-card__error">{{ errors.quantity }}</text>
        </view>

        <!-- 期望价格 -->
        <view class="form-card__field" :class="{ 'form-card__field--error': errors.expectedPrice }">
          <text class="form-card__label">期望价格</text>
          <view class="form-card__input-row">
            <text class="form-card__prefix">&#xa5;</text>
            <input
              class="form-card__input form-card__input--flex"
              :class="{ 'form-card__input--error': errors.expectedPrice }"
              v-model="form.expectedPrice"
              type="digit"
              placeholder="请输入价格"
              @input="clearError('expectedPrice')"
            />
            <text class="form-card__unit">元/吨</text>
          </view>
          <text v-if="errors.expectedPrice" class="form-card__error">{{ errors.expectedPrice }}</text>
        </view>

        <!-- 收货地址 -->
        <view class="form-card__field">
          <text class="form-card__label">收货地址</text>
          <input
            class="form-card__input"
            v-model="form.purchaseAddress"
            placeholder="请输入收货地址"
            :maxlength="200"
          />
        </view>

        <!-- 包装方式 -->
        <view class="form-card__field">
          <text class="form-card__label">包装方式</text>
          <picker :range="packagingOptions" @change="handlePackagingPick">
            <view class="form-card__picker">
              <text
                class="form-card__picker-text"
                :class="{ 'form-card__picker-text--placeholder': !form.packaging }"
              >{{ form.packaging || '请选择包装方式' }}</text>
              <text class="form-card__picker-arrow">&#x203A;</text>
            </view>
          </picker>
        </view>

        <!-- 交货方式 -->
        <view class="form-card__field">
          <text class="form-card__label">交货方式</text>
          <picker :range="deliveryMethodOptions" @change="handleDeliveryMethodPick">
            <view class="form-card__picker">
              <text
                class="form-card__picker-text"
                :class="{ 'form-card__picker-text--placeholder': !form.deliveryMethod }"
              >{{ form.deliveryMethod || '请选择交货方式' }}</text>
              <text class="form-card__picker-arrow">&#x203A;</text>
            </view>
          </picker>
        </view>

        <!-- 付款方式 -->
        <view class="form-card__field">
          <text class="form-card__label">付款方式</text>
          <picker :range="paymentMethodOptions" @change="handlePaymentMethodPick">
            <view class="form-card__picker">
              <text
                class="form-card__picker-text"
                :class="{ 'form-card__picker-text--placeholder': !form.paymentMethod }"
              >{{ form.paymentMethod || '请选择付款方式' }}</text>
              <text class="form-card__picker-arrow">&#x203A;</text>
            </view>
          </picker>
        </view>

        <!-- 备注 -->
        <view class="form-card__field">
          <text class="form-card__label">备注</text>
          <textarea
            class="form-card__textarea"
            v-model="form.remark"
            placeholder="补充说明（可选）"
            :maxlength="500"
            :auto-height="true"
          />
        </view>
      </view>

      <!-- 保存按钮 -->
      <view class="edit-page__action">
        <view
          class="save-btn"
          :class="{ 'save-btn--disabled': !canSubmit }"
          @tap="handleSubmit"
        >
          <text class="save-btn__text">{{ submitting ? '保存中...' : '保存修改' }}</text>
        </view>
      </view>
    </template>
  </view>
</template>

<style lang="scss" scoped>
.edit-page {
  min-height: 100vh;
  background: $bg-page;
  padding: $spacing-md;

  &__loading {
    display: flex;
    justify-content: center;
    padding: 120rpx 0;
  }

  &__loading-text {
    font-size: $font-md;
    color: $text-secondary;
  }

  &__action {
    margin-top: $spacing-lg;
    padding-bottom: $spacing-xl;
  }
}

.form-card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md $spacing-lg;

  &__field {
    padding: $spacing-sm 0;
    border-bottom: 1rpx solid $border-light;

    &:last-child {
      border-bottom: none;
    }

    &--error {
      border-left: 4rpx solid $color-error;
      padding-left: $spacing-sm;
    }
  }

  &__label {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
    display: block;
    margin-bottom: $spacing-xs;
  }

  &__readonly {
    height: 80rpx;
    display: flex;
    align-items: center;
  }

  &__readonly-text {
    font-size: $font-md;
    color: $text-secondary;
  }

  &__input {
    width: 100%;
    height: 80rpx;
    font-size: $font-md;
    color: $text-primary;
    padding: 0;

    &--flex {
      flex: 1;
      width: auto;
    }

    &--error {
      border-bottom: 2rpx solid $color-error;
    }
  }

  &__input-row {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
  }

  &__prefix {
    font-size: $font-lg;
    color: $accent-400;
    font-weight: bold;
  }

  &__unit {
    font-size: $font-sm;
    color: $text-secondary;
    flex-shrink: 0;
  }

  &__error {
    font-size: $font-xs;
    color: $color-error;
    margin-top: 4rpx;
    display: block;
  }

  &__textarea {
    width: 100%;
    min-height: 200rpx;
    font-size: $font-md;
    color: $text-primary;
    line-height: 1.8;
    padding: $spacing-xs 0;
  }

  &__picker {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 80rpx;
  }

  &__picker-text {
    font-size: $font-md;
    color: $text-primary;

    &--placeholder {
      color: $text-placeholder;
    }
  }

  &__picker-arrow {
    font-size: $font-lg;
    color: $text-placeholder;
  }
}

.save-btn {
  width: 100%;
  height: 96rpx;
  background: $autumn-400;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.15s;

  &:active {
    transform: scale(0.98);
  }

  &--disabled {
    opacity: 0.5;
  }

  &__text {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-inverse;
  }
}
</style>
