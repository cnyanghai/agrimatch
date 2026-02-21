<script setup lang="ts">
import { ref, computed } from 'vue'
import { BRAND_600, AUTUMN_500, WHITE } from '../../constants/colors'
import { onShow } from '@dcloudio/uni-app'
import { createRequirement, listMyRequirementTemplates, createRequirementTemplate, deleteRequirementTemplate } from '../../api/requirement'
import type { RequirementCreateRequest, RequirementTemplateResponse } from '../../api/requirement'
import type { PickedCategory } from '../../components/SchemaAwareCategoryPicker.vue'
import type { TemplateItem } from '../../components/WgTemplatePicker.vue'
import { uploadFile } from '../../utils/request'
import { useAuthStore } from '../../store/auth'
import SchemaAwareCategoryPicker from '../../components/SchemaAwareCategoryPicker.vue'

const authStore = useAuthStore()

/** 品类选择器选中项 */
const pickedCategory = ref<PickedCategory | null>(null)

/** 动态质量参数表单数据 */
const dynamicParamsData = ref<Record<string, any>>({})

const form = ref<RequirementCreateRequest>({
  categoryName: '',
  quantity: undefined,
  expectedPrice: undefined,
  packaging: '',
  invoiceType: '',
  paymentMethod: '',
  deliveryMethod: '',
  purchaseAddress: '',
  remark: '',
})

// ========== 模板功能 ==========
const templatePickerOpen = ref(false)
const templates = ref<TemplateItem[]>([])
const loadedRawTemplates = ref<RequirementTemplateResponse[]>([])

async function loadRawTemplates() {
  try {
    const list = await listMyRequirementTemplates()
    loadedRawTemplates.value = list || []
    templates.value = (list || []).map((t: RequirementTemplateResponse) => {
      const data = JSON.parse(t.templateJson || '{}')
      return {
        id: t.id,
        name: t.templateName,
        category: data.categoryName || '',
        quantity: data.quantity,
        price: data.expectedPrice,
        tags: data.tags,
      }
    })
  } catch {
    // ignore
  }
}

function handleTemplateSelect(tpl: TemplateItem) {
  const rawTpl = loadedRawTemplates.value.find(t => t.id === tpl.id)
  if (rawTpl) {
    const data = JSON.parse(rawTpl.templateJson || '{}')
    form.value.categoryName = data.categoryName || ''
    form.value.quantity = data.quantity
    form.value.expectedPrice = data.expectedPrice
    form.value.packaging = data.packaging || ''
    form.value.invoiceType = data.invoiceType || ''
    form.value.paymentMethod = data.paymentMethod || ''
    form.value.deliveryMethod = data.deliveryMethod || ''
    form.value.purchaseAddress = data.purchaseAddress || ''
    form.value.remark = data.remark || ''
    if (data.categoryName) {
      pickedCategory.value = { id: data.productId, name: data.categoryName, schemaCode: data.schemaCode || '' }
    }
    uni.showToast({ title: '模板已填入', icon: 'success' })
  }
}

async function handleDeleteTemplate(id: number) {
  try {
    await deleteRequirementTemplate(id)
    templates.value = templates.value.filter(t => t.id !== id)
    loadedRawTemplates.value = loadedRawTemplates.value.filter(t => t.id !== id)
    uni.showToast({ title: '已删除', icon: 'success' })
  } catch {
    // handled
  }
}

async function saveAsTemplate() {
  if (!form.value.categoryName.trim()) {
    uni.showToast({ title: '请先填写品类', icon: 'none' })
    return
  }
  uni.showModal({
    title: '保存为模板',
    content: '将当前表单保存为采购模板',
    editable: true,
    placeholderText: '模板名称',
    success: async (res) => {
      if (!res.confirm || !res.content?.trim()) return
      try {
        const templateJson = JSON.stringify({
          categoryName: form.value.categoryName,
          productId: pickedCategory.value?.id,
          schemaCode: pickedCategory.value?.schemaCode || '',
          quantity: form.value.quantity,
          expectedPrice: form.value.expectedPrice,
          packaging: form.value.packaging,
          invoiceType: form.value.invoiceType,
          paymentMethod: form.value.paymentMethod,
          deliveryMethod: form.value.deliveryMethod,
          purchaseAddress: form.value.purchaseAddress,
          remark: form.value.remark,
        })
        await createRequirementTemplate({ templateName: res.content.trim(), templateJson })
        uni.showToast({ title: '模板已保存', icon: 'success' })
        loadRawTemplates()
      } catch {
        // handled
      }
    },
  })
}

onShow(() => {
  loadRawTemplates()
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

const paymentMethodOptions = ['款到发货', '货到付款', '月结30天', '月结60天', '协商']
const deliveryMethodOptions = ['送货上门', '自提', '物流运输', '协商']
const invoiceTypeOptions = ['增值税专用发票', '增值税普通发票', '不开票', '协商']

/** 选择发票类型 */
function handleInvoiceTypePick(e: any) {
  const idx = e.detail.value
  form.value.invoiceType = invoiceTypeOptions[idx]
}

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
    newErrors.categoryName = '请选择商品品类'
  }

  // Number validations
  if (form.value.quantity !== undefined && form.value.quantity !== null) {
    const qty = Number(form.value.quantity)
    if (isNaN(qty) || qty <= 0) {
      newErrors.quantity = '数量必须大于0'
    }
  }

  if (form.value.expectedPrice !== undefined && form.value.expectedPrice !== null) {
    const price = Number(form.value.expectedPrice)
    if (isNaN(price) || price < 0) {
      newErrors.expectedPrice = '价格不能为负数'
    }
  }

  errors.value = newErrors
  return Object.keys(newErrors).length === 0
}

/** 表单是否可提交 */
const canSubmit = computed(() => {
  return form.value.categoryName.trim().length > 0 && !submitting.value
})

/** 选择付款方式 */
function handlePaymentMethodPick(e: any) {
  const idx = e.detail.value
  form.value.paymentMethod = paymentMethodOptions[idx]
}

/** 选择交货方式 */
function handleDeliveryMethodPick(e: any) {
  const idx = e.detail.value
  form.value.deliveryMethod = deliveryMethodOptions[idx]
}

/** 提交采购需求 */
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
      quantity: form.value.quantity || undefined,
      expectedPrice: form.value.expectedPrice || undefined,
      packaging: form.value.packaging?.trim() || undefined,
      invoiceType: form.value.invoiceType || undefined,
      paymentMethod: form.value.paymentMethod || undefined,
      deliveryMethod: form.value.deliveryMethod || undefined,
      purchaseAddress: form.value.purchaseAddress?.trim() || undefined,
      remark: form.value.remark?.trim() || undefined,
    }
    // 动态质量参数
    if (Object.keys(dynamicParamsData.value).length > 0) {
      req.paramsJson = JSON.stringify(dynamicParamsData.value)
    }
    req.expireMinutes = expireMinutes.value
    if (images.value.length > 0) {
      req.imagesJson = JSON.stringify(images.value)
    }
    await createRequirement(req)
    uni.showModal({
      title: '发布成功',
      content: '采购需求已发布',
      confirmText: '查看我的发布',
      cancelText: '继续发布',
      success(res) {
        if (res.confirm) {
          uni.navigateTo({ url: '/pages/requirement/my-list' })
        } else {
          form.value = { categoryName: '', quantity: undefined, expectedPrice: undefined, packaging: '', invoiceType: '', paymentMethod: '', deliveryMethod: '', purchaseAddress: '', remark: '' }
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
    <WgNavBar title="发布采购" />

    <!-- 模板操作栏 -->
    <view class="template-bar">
      <view class="template-bar__btn" @tap="templatePickerOpen = true">
        <WgIcon name="file-text" :size="16" :color="BRAND_600" />
        <text class="template-bar__text">使用模板</text>
      </view>
      <view class="template-bar__btn" @tap="saveAsTemplate">
        <WgIcon name="bookmark" :size="16" :color="AUTUMN_500" />
        <text class="template-bar__text template-bar__text--save">保存模板</text>
      </view>
    </view>

    <!-- 模板选择器 -->
    <WgTemplatePicker
      v-model="templatePickerOpen"
      :templates="templates"
      title="采购模板"
      empty-text="暂无模板，可点击「保存模板」创建"
      @select="handleTemplateSelect"
      @delete="handleDeleteTemplate"
    />

    <view class="form-card">
      <!-- 商品品类选择 -->
      <view class="form-card__field" :class="{ 'form-card__field--error': errors.categoryName }">
        <text class="form-card__label">商品品类 <text class="form-card__required">*</text></text>
        <SchemaAwareCategoryPicker
          v-model="pickedCategory"
          @update:modelValue="(v: PickedCategory | null) => { form.categoryName = v?.name || ''; form.productId = v?.id; clearError('categoryName'); }"
        />
        <text v-if="errors.categoryName" class="form-card__error">{{ errors.categoryName }}</text>
      </view>

      <!-- 动态质量参数 -->
      <view v-if="pickedCategory?.id" class="form-card__field">
        <text class="form-card__label">质量参数</text>
        <WgParamsForm :product-id="pickedCategory.id" v-model="dynamicParamsData" />
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
            placeholder="请输入期望价格"
            @input="clearError('expectedPrice')"
          />
          <text class="form-card__unit">元/吨</text>
        </view>
        <text v-if="errors.expectedPrice" class="form-card__error">{{ errors.expectedPrice }}</text>
      </view>

      <!-- 包装要求 -->
      <view class="form-card__field">
        <text class="form-card__label">包装要求</text>
        <input
          class="form-card__input"
          v-model="form.packaging"
          placeholder="请输入包装要求"
          :maxlength="100"
        />
      </view>

      <!-- 发票类型 -->
      <view class="form-card__field">
        <text class="form-card__label">发票类型</text>
        <picker :range="invoiceTypeOptions" @change="handleInvoiceTypePick">
          <view class="form-card__picker">
            <text
              class="form-card__picker-text"
              :class="{ 'form-card__picker-text--placeholder': !form.invoiceType }"
            >
              {{ form.invoiceType || '请选择发票类型' }}
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

      <!-- 交货方式 -->
      <view class="form-card__field">
        <text class="form-card__label">交货方式</text>
        <picker :range="deliveryMethodOptions" @change="handleDeliveryMethodPick">
          <view class="form-card__picker">
            <text
              class="form-card__picker-text"
              :class="{ 'form-card__picker-text--placeholder': !form.deliveryMethod }"
            >
              {{ form.deliveryMethod || '请选择交货方式' }}
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

      <!-- 收货地 -->
      <view class="form-card__field">
        <text class="form-card__label">收货地</text>
        <input
          class="form-card__input"
          v-model="form.purchaseAddress"
          placeholder="请输入收货地址"
          :maxlength="200"
        />
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
              <WgIcon name="clear" :size="20" :color="WHITE" />
            </view>
          </view>
          <view v-if="images.length < 6 && !uploading" class="image-grid__add" @tap="chooseImage">
            <WgIcon name="plus" :size="40" color="#ccc" />
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
        <text class="publish-btn__text">{{ submitting ? '发布中...' : '发布需求' }}</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
/* ===== 模板操作栏 ===== */
.template-bar {
  display: flex;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;

  &__btn {
    display: flex;
    align-items: center;
    gap: 6rpx;
    padding: $spacing-xs $spacing-md;
    border-radius: 30rpx;
    background: $bg-card;
    border: 1rpx solid $warm-200;
    transition: transform 0.15s;

    &:active {
      transform: scale(0.95);
    }
  }

  &__text {
    font-size: $font-xs;
    color: $brand-600;
    font-weight: 600;

    &--save {
      color: $autumn-500;
    }
  }
}

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
  background: $autumn-400;
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
    color: $text-inverse;
  }
}
</style>
