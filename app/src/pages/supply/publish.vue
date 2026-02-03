<script setup lang="ts">
import { ref, computed } from 'vue'
import { createSupply } from '../../api/supply'
import type { SupplyCreateRequest } from '../../api/supply'
import { uploadFile } from '../../utils/request'
import { useAuthStore } from '../../store/auth'

const authStore = useAuthStore()

const form = ref<SupplyCreateRequest>({
  categoryName: '',
  origin: '',
  quantity: undefined,
  exFactoryPrice: undefined,
  shipAddress: '',
  deliveryMode: '',
  paymentMethod: '',
  remark: '',
})

const submitting = ref(false)

/** 有效期选项 */
const expireOptions = [
  { label: '1小时', value: 60 },
  { label: '1天', value: 1440 },
  { label: '3天', value: 4320 },
  { label: '7天', value: 10080 },
  { label: '30天', value: 43200 },
]
const expireIndex = ref(3) // 默认7天
const expireMinutes = computed(() => expireOptions[expireIndex.value].value)

/** 图片上传 */
const images = ref<string[]>([])
const uploading = ref(false)

/** 选择并上传图片 */
function chooseImage() {
  const remaining = 6 - images.value.length
  if (remaining <= 0) return

  uni.chooseImage({
    count: remaining,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success(res) {
      const tempFiles = res.tempFilePaths as string[]
      uploading.value = true
      let pending = tempFiles.length
      tempFiles.forEach((filePath: string) => {
        uploadFile(filePath, '/api/files/upload/image')
          .then((data: any) => {
            images.value.push(data.fileUrl)
          })
          .catch(() => {
            uni.showToast({ title: '图片上传失败', icon: 'none' })
          })
          .finally(() => {
            pending--
            if (pending <= 0) uploading.value = false
          })
      })
    },
  })
}

/** 移除图片 */
function removeImage(idx: number) {
  images.value.splice(idx, 1)
}

/** 预览图片 */
function previewImage(idx: number) {
  uni.previewImage({ urls: images.value, current: idx })
}

function handleExpirePick(e: any) {
  expireIndex.value = Number(e.detail.value)
}

const deliveryModeOptions = ['送货上门', '自提', '物流运输', '协商']
const paymentMethodOptions = ['款到发货', '货到付款', '月结30天', '月结60天', '协商']

/** 错误状态 */
const errors = ref<Record<string, string>>({})

function clearError(field: string) {
  delete errors.value[field]
}

/** 表单校验 */
function validate(): boolean {
  const newErrors: Record<string, string> = {}

  // Required: categoryName
  if (!form.value.categoryName.trim()) {
    newErrors.categoryName = '请输入商品名称'
  }

  // Number validations
  if (form.value.quantity !== undefined && form.value.quantity !== null) {
    const qty = Number(form.value.quantity)
    if (isNaN(qty) || qty <= 0) {
      newErrors.quantity = '数量必须大于0'
    }
  }

  if (form.value.exFactoryPrice !== undefined && form.value.exFactoryPrice !== null) {
    const price = Number(form.value.exFactoryPrice)
    if (isNaN(price) || price < 0) {
      newErrors.exFactoryPrice = '价格不能为负数'
    }
  }

  errors.value = newErrors
  return Object.keys(newErrors).length === 0
}

/** 表单是否可提交 */
const canSubmit = computed(() => {
  return form.value.categoryName.trim().length > 0 && !submitting.value
})

/** 选择交货方式 */
function handleDeliveryModePick(e: any) {
  const idx = e.detail.value
  form.value.deliveryMode = deliveryModeOptions[idx]
}

/** 选择付款方式 */
function handlePaymentMethodPick(e: any) {
  const idx = e.detail.value
  form.value.paymentMethod = paymentMethodOptions[idx]
}

/** 提交供应信息 */
async function handleSubmit() {
  if (!authStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }

  if (!validate()) {
    uni.showToast({ title: '请检查填写内容', icon: 'none' })
    return
  }

  if (submitting.value) return
  submitting.value = true

  try {
    const categoryName = form.value.categoryName.trim()
    const req: any = {
      categoryName,
      origin: form.value.origin?.trim() || undefined,
      quantity: form.value.quantity || undefined,
      exFactoryPrice: form.value.exFactoryPrice || undefined,
      shipAddress: form.value.shipAddress?.trim() || undefined,
      deliveryMode: form.value.deliveryMode || undefined,
      paymentMethod: form.value.paymentMethod || undefined,
      remark: form.value.remark?.trim() || undefined,
    }
    req.expireMinutes = expireMinutes.value
    if (images.value.length > 0) {
      req.imagesJson = JSON.stringify(images.value)
    }
    await createSupply(req)
    uni.showModal({
      title: '发布成功',
      content: '供应信息已发布',
      confirmText: '查看我的发布',
      cancelText: '继续发布',
      success(res) {
        if (res.confirm) {
          uni.navigateTo({ url: '/pages/supply/my-list' })
        } else {
          // Reset form
          form.value = { categoryName: '', origin: '', quantity: undefined, exFactoryPrice: undefined, shipAddress: '', deliveryMode: '', paymentMethod: '', remark: '' }
          images.value = []
          expireIndex.value = 3
        }
      },
    })
  } catch {
    // handled by request.ts
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="publish-page">
    <view class="form-card">
      <!-- 商品名称 -->
      <view class="form-card__field" :class="{ 'form-card__field--error': errors.categoryName }">
        <text class="form-card__label">商品名称 <text class="form-card__required">*</text></text>
        <input
          class="form-card__input"
          :class="{ 'form-card__input--error': errors.categoryName }"
          v-model="form.categoryName"
          placeholder="请输入商品名称"
          :maxlength="50"
          @input="clearError('categoryName')"
        />
        <text v-if="errors.categoryName" class="form-card__error">{{ errors.categoryName }}</text>
      </view>

      <!-- 产地 -->
      <view class="form-card__field">
        <text class="form-card__label">产地</text>
        <input
          class="form-card__input"
          v-model="form.origin"
          placeholder="请输入产地"
          :maxlength="100"
        />
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

      <!-- 出厂价 -->
      <view class="form-card__field" :class="{ 'form-card__field--error': errors.exFactoryPrice }">
        <text class="form-card__label">出厂价</text>
        <view class="form-card__input-row">
          <text class="form-card__prefix">&#xa5;</text>
          <input
            class="form-card__input form-card__input--flex"
            :class="{ 'form-card__input--error': errors.exFactoryPrice }"
            v-model="form.exFactoryPrice"
            type="digit"
            placeholder="请输入价格"
            @input="clearError('exFactoryPrice')"
          />
          <text class="form-card__unit">元/吨</text>
        </view>
        <text v-if="errors.exFactoryPrice" class="form-card__error">{{ errors.exFactoryPrice }}</text>
      </view>

      <!-- 发货地 -->
      <view class="form-card__field">
        <text class="form-card__label">发货地</text>
        <input
          class="form-card__input"
          v-model="form.shipAddress"
          placeholder="请输入发货地址"
          :maxlength="200"
        />
      </view>

      <!-- 交货方式 -->
      <view class="form-card__field">
        <text class="form-card__label">交货方式</text>
        <picker :range="deliveryModeOptions" @change="handleDeliveryModePick">
          <view class="form-card__picker">
            <text
              class="form-card__picker-text"
              :class="{ 'form-card__picker-text--placeholder': !form.deliveryMode }"
            >
              {{ form.deliveryMode || '请选择交货方式' }}
            </text>
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
            >
              {{ form.paymentMethod || '请选择付款方式' }}
            </text>
            <text class="form-card__picker-arrow">&#x203A;</text>
          </view>
        </picker>
      </view>

      <!-- 有效期 -->
      <view class="form-card__field">
        <text class="form-card__label">有效期</text>
        <picker :range="expireOptions.map(o => o.label)" :value="expireIndex" @change="handleExpirePick">
          <view class="form-card__picker">
            <text class="form-card__picker-text">{{ expireOptions[expireIndex].label }}</text>
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

      <!-- 图片 -->
      <view class="form-card__field">
        <text class="form-card__label">商品图片</text>
        <view class="image-grid">
          <view v-for="(img, idx) in images" :key="idx" class="image-grid__item">
            <image :src="img" mode="aspectFill" class="image-grid__img" @tap="previewImage(idx)" />
            <view class="image-grid__delete" @tap.stop="removeImage(idx)">
              <uni-icons type="clear" size="20" color="#fff" />
            </view>
          </view>
          <view v-if="images.length < 6 && !uploading" class="image-grid__add" @tap="chooseImage">
            <uni-icons type="plusempty" size="40" color="#ccc" />
            <text class="image-grid__add-text">{{ images.length }}/6</text>
          </view>
          <view v-if="uploading" class="image-grid__add">
            <text class="image-grid__add-text">上传中...</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 发布按钮 -->
    <view class="publish-page__action">
      <view
        class="publish-btn"
        :class="{ 'publish-btn--disabled': !canSubmit }"
        @tap="handleSubmit"
      >
        <text class="publish-btn__text">{{ submitting ? '发布中...' : '发布供应' }}</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.publish-page {
  min-height: 100vh;
  background: $bg-page;
  padding: $spacing-md;

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

  &__required {
    color: $accent-400;
    font-weight: normal;
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

/* ===== 图片网格 ===== */
.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;

  &__item {
    position: relative;
    width: 200rpx;
    height: 200rpx;
    border-radius: $radius-md;
    overflow: hidden;
  }

  &__img {
    width: 100%;
    height: 100%;
  }

  &__delete {
    position: absolute;
    top: 0;
    right: 0;
    width: 44rpx;
    height: 44rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.5);
    border-radius: 0 0 0 $radius-sm;
  }

  &__add {
    width: 200rpx;
    height: 200rpx;
    border: 2rpx dashed $border-color;
    border-radius: $radius-md;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: $spacing-xs;
  }

  &__add-text {
    font-size: $font-xs;
    color: $text-placeholder;
  }
}

.publish-btn {
  width: 100%;
  height: 96rpx;
  background: $brand-600;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;

  &--disabled {
    opacity: 0.5;
  }

  &__text {
    font-size: $font-lg;
    font-weight: bold;
    color: #fff;
  }
}
</style>
