<script setup lang="ts">
import { ref, computed } from 'vue'
import { BRAND_600, ACCENT_400, AUTUMN_500, WHITE } from '../../constants/colors'
import { onShow } from '@dcloudio/uni-app'
import { createSupply, getMySupplyTemplates, createSupplyTemplate, deleteSupplyTemplate } from '../../api/supply'
import type { SupplyCreateRequest, BasisQuoteRequest, SupplyTemplateResponse } from '../../api/supply'
import { uploadFile } from '../../utils/request'
import { useAuthStore } from '../../store/auth'
import type { PickedCategory } from '../../components/WgCategoryPicker.vue'
import type { TemplateItem } from '../../components/WgTemplatePicker.vue'

const authStore = useAuthStore()

/** 品类选择器选中项 */
const pickedCategory = ref<PickedCategory | null>(null)

/** 动态质量参数表单数据 */
const dynamicParamsData = ref<Record<string, any>>({})

const form = ref<SupplyCreateRequest>({
  categoryName: '',
  origin: '',
  quantity: undefined,
  priceType: 0,
  exFactoryPrice: undefined,
  shipAddress: '',
  deliveryMode: '',
  paymentMethod: '',
  invoiceType: '',
  packaging: '',
  storageMethod: '',
  remark: '',
})

// ========== 模板功能 ==========
const templatePickerOpen = ref(false)
const templates = ref<TemplateItem[]>([])

async function loadTemplates() {
  try {
    const list = await getMySupplyTemplates()
    templates.value = (list || []).map((t: SupplyTemplateResponse) => {
      const data = JSON.parse(t.templateJson || '{}')
      return {
        id: t.id,
        name: t.templateName,
        category: data.categoryName || '',
        quantity: data.quantity,
        price: data.exFactoryPrice,
        tags: data.tags,
      }
    })
  } catch {
    // ignore
  }
}

function handleTemplateSelect(tpl: TemplateItem) {
  // 从原始数据还原
  const raw = (templates.value as any).__raw?.[tpl.id]
  // 用 API 重新拿一下原始 json
  const found = templates.value.find(t => t.id === tpl.id)
  if (!found) return
  // 从 templates 中拿不到完整 JSON，需要用 loadedRaw
  const rawTpl = loadedRawTemplates.value.find(t => t.id === tpl.id)
  if (rawTpl) {
    const data = JSON.parse(rawTpl.templateJson || '{}')
    form.value.categoryName = data.categoryName || ''
    form.value.origin = data.origin || ''
    form.value.quantity = data.quantity
    form.value.priceType = data.priceType ?? 0
    form.value.exFactoryPrice = data.exFactoryPrice
    form.value.shipAddress = data.shipAddress || ''
    form.value.deliveryMode = data.deliveryMode || ''
    form.value.paymentMethod = data.paymentMethod || ''
    form.value.invoiceType = data.invoiceType || ''
    form.value.packaging = data.packaging || ''
    form.value.storageMethod = data.storageMethod || ''
    form.value.remark = data.remark || ''
    // 重置品类选择器
    if (data.categoryName) {
      pickedCategory.value = { id: data.productId, name: data.categoryName, schemaCode: data.schemaCode || '' }
    }
    uni.showToast({ title: '模板已填入', icon: 'success' })
  }
}

const loadedRawTemplates = ref<SupplyTemplateResponse[]>([])

async function loadRawTemplates() {
  try {
    const list = await getMySupplyTemplates()
    loadedRawTemplates.value = list || []
    templates.value = (list || []).map((t: SupplyTemplateResponse) => {
      const data = JSON.parse(t.templateJson || '{}')
      return {
        id: t.id,
        name: t.templateName,
        category: data.categoryName || '',
        quantity: data.quantity,
        price: data.exFactoryPrice,
        tags: data.tags,
      }
    })
  } catch {
    // ignore
  }
}

async function handleDeleteTemplate(id: number) {
  try {
    await deleteSupplyTemplate(id)
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
    content: '将当前表单保存为发布模板',
    editable: true,
    placeholderText: '模板名称',
    success: async (res) => {
      if (!res.confirm || !res.content?.trim()) return
      try {
        const templateJson = JSON.stringify({
          categoryName: form.value.categoryName,
          productId: pickedCategory.value?.id,
          schemaCode: pickedCategory.value?.schemaCode || '',
          origin: form.value.origin,
          quantity: form.value.quantity,
          priceType: form.value.priceType,
          exFactoryPrice: form.value.exFactoryPrice,
          shipAddress: form.value.shipAddress,
          deliveryMode: form.value.deliveryMode,
          paymentMethod: form.value.paymentMethod,
          invoiceType: form.value.invoiceType,
          packaging: form.value.packaging,
          storageMethod: form.value.storageMethod,
          remark: form.value.remark,
        })
        await createSupplyTemplate({ templateName: res.content.trim(), templateJson })
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

/** 基差报价列表 */
const basisQuotes = ref<BasisQuoteRequest[]>([])

/** 添加一条基差报价 */
function addBasisQuote() {
  basisQuotes.value.push({ contractCode: '', basisPrice: 0, availableQty: 0 })
}

/** 删除一条基差报价 */
function removeBasisQuote(idx: number) {
  basisQuotes.value.splice(idx, 1)
}

/** 切换报价类型时重置相关字段 */
function handlePriceTypeChange(type: number) {
  form.value.priceType = type
  if (type === 0) {
    basisQuotes.value = []
  } else if (type === 1 && basisQuotes.value.length === 0) {
    addBasisQuote()
  }
}

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
const invoiceTypeOptions = ['增值税专用发票', '增值税普通发票', '不开票', '协商']
const packagingOptions = ['编织袋', '纸箱', '吨袋', '散装', '桶装', '协商']
const storageOptions = ['常温仓储', '冷链仓储', '恒温恒湿', '露天堆放', '协商']

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

/** 选择发票类型 */
function handleInvoiceTypePick(e: any) {
  const idx = e.detail.value
  form.value.invoiceType = invoiceTypeOptions[idx]
}

/** 选择包装方式 */
function handlePackagingPick(e: any) {
  const idx = e.detail.value
  form.value.packaging = packagingOptions[idx]
}

/** 选择仓储方式 */
function handleStoragePick(e: any) {
  const idx = e.detail.value
  form.value.storageMethod = storageOptions[idx]
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
      priceType: form.value.priceType ?? 0,
      exFactoryPrice: form.value.exFactoryPrice || undefined,
      shipAddress: form.value.shipAddress?.trim() || undefined,
      deliveryMode: form.value.deliveryMode || undefined,
      paymentMethod: form.value.paymentMethod || undefined,
      invoiceType: form.value.invoiceType || undefined,
      packaging: form.value.packaging || undefined,
      storageMethod: form.value.storageMethod || undefined,
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
    // 基差报价（priceType=1时附带）
    if (form.value.priceType === 1 && basisQuotes.value.length > 0) {
      req.basisQuotes = basisQuotes.value.filter(q => q.contractCode.trim())
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
          form.value = { categoryName: '', origin: '', quantity: undefined, priceType: 0, exFactoryPrice: undefined, shipAddress: '', deliveryMode: '', paymentMethod: '', invoiceType: '', packaging: '', storageMethod: '', remark: '' }
          basisQuotes.value = []
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
    <WgNavBar title="发布供应" />

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
      title="供应模板"
      empty-text="暂无模板，可点击「保存模板」创建"
      @select="handleTemplateSelect"
      @delete="handleDeleteTemplate"
    />

    <view class="form-card">
      <!-- 商品品类选择 -->
      <view class="form-card__field" :class="{ 'form-card__field--error': errors.categoryName }">
        <text class="form-card__label">商品品类 <text class="form-card__required">*</text></text>
        <WgCategoryPicker
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

      <!-- 报价类型 -->
      <view class="form-card__field">
        <text class="form-card__label">报价方式</text>
        <view class="price-type-tabs">
          <view
            class="price-type-tabs__item"
            :class="{ 'price-type-tabs__item--active': form.priceType === 0 }"
            @tap="handlePriceTypeChange(0)"
          >
            <text class="price-type-tabs__text">现货一口价</text>
          </view>
          <view
            class="price-type-tabs__item"
            :class="{ 'price-type-tabs__item--active': form.priceType === 1 }"
            @tap="handlePriceTypeChange(1)"
          >
            <text class="price-type-tabs__text">基差报价</text>
          </view>
        </view>
      </view>

      <!-- 出厂价（现货模式） -->
      <view v-if="form.priceType === 0" class="form-card__field" :class="{ 'form-card__field--error': errors.exFactoryPrice }">
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

      <!-- 基差报价明细（基差模式） -->
      <view v-if="form.priceType === 1" class="form-card__field">
        <text class="form-card__label">基差报价明细</text>
        <view v-for="(quote, idx) in basisQuotes" :key="idx" class="basis-quote-row">
          <view class="basis-quote-row__fields">
            <input
              class="basis-quote-row__input"
              v-model="quote.contractCode"
              placeholder="合约代码 (如M2509)"
              :maxlength="20"
            />
            <view class="basis-quote-row__num-group">
              <input
                class="basis-quote-row__input basis-quote-row__input--short"
                v-model.number="quote.basisPrice"
                type="digit"
                placeholder="基差"
              />
              <text class="basis-quote-row__unit">元</text>
            </view>
            <view class="basis-quote-row__num-group">
              <input
                class="basis-quote-row__input basis-quote-row__input--short"
                v-model.number="quote.availableQty"
                type="digit"
                placeholder="可售量"
              />
              <text class="basis-quote-row__unit">吨</text>
            </view>
          </view>
          <view class="basis-quote-row__delete" @tap="removeBasisQuote(idx)">
            <WgIcon name="clear" :size="20" :color="ACCENT_400" />
          </view>
        </view>
        <view class="basis-quote-add" @tap="addBasisQuote">
          <WgIcon name="plus" :size="16" :color="BRAND_600" />
          <text class="basis-quote-add__text">添加合约</text>
        </view>
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

      <!-- 包装方式 -->
      <view class="form-card__field">
        <text class="form-card__label">包装方式</text>
        <picker :range="packagingOptions" @change="handlePackagingPick">
          <view class="form-card__picker">
            <text
              class="form-card__picker-text"
              :class="{ 'form-card__picker-text--placeholder': !form.packaging }"
            >
              {{ form.packaging || '请选择包装方式' }}
            </text>
            <text class="form-card__picker-arrow">&#x203A;</text>
          </view>
        </picker>
      </view>

      <!-- 仓储条件 -->
      <view class="form-card__field">
        <text class="form-card__label">仓储条件</text>
        <picker :range="storageOptions" @change="handleStoragePick">
          <view class="form-card__picker">
            <text
              class="form-card__picker-text"
              :class="{ 'form-card__picker-text--placeholder': !form.storageMethod }"
            >
              {{ form.storageMethod || '请选择仓储条件' }}
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
        <text class="publish-btn__text">{{ submitting ? '发布中...' : '发布供应' }}</text>
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

/* ===== 报价类型切换 ===== */
.price-type-tabs {
  display: flex;
  gap: $spacing-sm;
  margin-top: $spacing-xs;

  &__item {
    flex: 1;
    height: 72rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $radius-md;
    border: 2rpx solid $border-color;
    transition: all 0.2s;

    &--active {
      border-color: $brand-600;
      background: $brand-50;
    }
  }

  &__text {
    font-size: $font-sm;
    color: $text-secondary;
  }

  &__item--active &__text {
    color: $brand-600;
    font-weight: 600;
  }
}

/* ===== 基差报价行 ===== */
.basis-quote-row {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  margin-bottom: $spacing-sm;
  padding: $spacing-sm;
  background: $bg-page;
  border-radius: $radius-md;

  &__fields {
    flex: 1;
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
  }

  &__input {
    flex: 1;
    min-width: 200rpx;
    height: 64rpx;
    font-size: $font-sm;
    color: $text-primary;
    background: $bg-card;
    border-radius: $radius-sm;
    padding: 0 $spacing-sm;

    &--short {
      min-width: 120rpx;
      flex: 0.6;
    }
  }

  &__num-group {
    display: flex;
    align-items: center;
    gap: 4rpx;
  }

  &__unit {
    font-size: $font-xs;
    color: $text-secondary;
    flex-shrink: 0;
  }

  &__delete {
    flex-shrink: 0;
    padding: $spacing-xs;
  }
}

.basis-quote-add {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  padding: $spacing-sm;
  border: 2rpx dashed $border-color;
  border-radius: $radius-md;

  &__text {
    font-size: $font-sm;
    color: $brand-600;
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
    color: $text-inverse;
  }
}
</style>
