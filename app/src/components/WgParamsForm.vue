<script setup lang="ts">
/**
 * WgParamsForm - 动态质量参数表单（uni-app版）
 * 
 * 基于 productId 加载该品类的参数定义，渲染表单控件。
 * 支持：文本输入、数值输入、下拉选择、自定义参数添加。
 * 
 * 用法：
 * <WgParamsForm :product-id="123" v-model="paramsData" />
 */
import { ref, computed, watch, onMounted } from 'vue'
import { getProductParams, type ProductParam } from '../api/product'

const props = defineProps<{
  productId?: number
  modelValue?: Record<string, any>
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: Record<string, any>): void
}>()

const paramList = ref<ProductParam[]>([])
const loading = ref(false)
const localValues = ref<Record<string, any>>({})

/** 自定义参数（用户可自由添加） */
const customParams = ref<Array<{ name: string; value: string }>>([])

/** 当 productId 变化时重新加载参数 */
watch(() => props.productId, async (id) => {
  if (!id) {
    paramList.value = []
    return
  }
  loading.value = true
  try {
    paramList.value = await getProductParams(id) || []
  } catch {
    paramList.value = []
  } finally {
    loading.value = false
  }
}, { immediate: true })

/** 同步外部 modelValue */
watch(() => props.modelValue, (v) => {
  if (v) localValues.value = { ...v }
}, { immediate: true, deep: true })

function updateParam(paramId: number, value: any) {
  localValues.value[paramId] = value
  emitAll()
}

function emitAll() {
  // 合并标准参数 + 自定义参数
  const result: Record<string, any> = { ...localValues.value }
  // 自定义参数以 "custom_N" key存储
  customParams.value.forEach((cp, i) => {
    if (cp.name.trim()) {
      result[`custom_${i}`] = { name: cp.name, value: cp.value }
    }
  })
  emit('update:modelValue', result)
}

function parseOptions(param: ProductParam): string[] {
  if (!param.options) return []
  if (Array.isArray(param.options)) return param.options
  try {
    return JSON.parse(param.options as unknown as string)
  } catch {
    return []
  }
}

function addCustomParam() {
  customParams.value.push({ name: '', value: '' })
}

function removeCustomParam(idx: number) {
  customParams.value.splice(idx, 1)
  emitAll()
}

/** 选择器弹出 */
const pickerParam = ref<ProductParam | null>(null)
const pickerOptions = ref<string[]>([])

function openPicker(param: ProductParam) {
  const opts = parseOptions(param)
  if (opts.length === 0) return
  pickerParam.value = param
  pickerOptions.value = opts
}

function handlePickerConfirm(idx: number) {
  if (pickerParam.value && pickerOptions.value[idx]) {
    updateParam(pickerParam.value.id, pickerOptions.value[idx])
  }
  pickerParam.value = null
}
</script>

<template>
  <view class="params-form">
    <!-- Loading -->
    <view v-if="loading" class="params-form__loading">
      <text class="params-form__loading-text">加载参数中...</text>
    </view>

    <!-- 无参数 -->
    <view v-else-if="paramList.length === 0 && !productId" class="params-form__empty">
      <text class="params-form__empty-text">请先选择商品品类</text>
    </view>

    <!-- 参数列表 -->
    <template v-else>
      <view
        v-for="param in paramList"
        :key="param.id"
        class="param-row"
      >
        <view class="param-row__header">
          <text class="param-row__name">{{ param.paramName }}</text>
          <text v-if="param.required" class="param-row__required">*</text>
          <text v-if="param.unit" class="param-row__unit">{{ param.unit }}</text>
        </view>

        <!-- 下拉选择类型 -->
        <view
          v-if="parseOptions(param).length > 0"
          class="param-row__select"
          @tap="openPicker(param)"
        >
          <text :class="localValues[param.id] ? 'param-row__value' : 'param-row__placeholder'">
            {{ localValues[param.id] || '请选择' }}
          </text>
          <WgIcon name="chevron-down" :size="14" color="#A8A29E" />
        </view>

        <!-- 数值输入 -->
        <input
          v-else-if="param.paramType === 'number'"
          class="param-row__input"
          type="digit"
          :value="localValues[param.id]"
          :placeholder="`请输入${param.paramName}`"
          @input="(e: any) => updateParam(param.id, e.detail?.value)"
        />

        <!-- 文本输入 -->
        <input
          v-else
          class="param-row__input"
          :value="localValues[param.id]"
          :placeholder="`请输入${param.paramName}`"
          @input="(e: any) => updateParam(param.id, e.detail?.value)"
        />
      </view>

      <!-- 自定义参数 -->
      <view class="custom-section">
        <view
          v-for="(cp, idx) in customParams"
          :key="idx"
          class="custom-row"
        >
          <input
            class="custom-row__name"
            v-model="cp.name"
            placeholder="参数名"
            @blur="emitAll"
          />
          <input
            class="custom-row__value"
            v-model="cp.value"
            placeholder="参数值"
            @blur="emitAll"
          />
          <view class="custom-row__remove" @tap="removeCustomParam(idx)">
            <WgIcon name="clear" :size="16" color="#ef4444" />
          </view>
        </view>
        <view class="custom-add" @tap="addCustomParam">
          <WgIcon name="plus" :size="14" color="#2D6A4F" />
          <text class="custom-add__text">添加自定义参数</text>
        </view>
      </view>
    </template>

    <!-- Picker 弹窗 -->
    <view v-if="pickerParam" class="picker-mask" @tap="pickerParam = null">
      <view class="picker-popup" @tap.stop>
        <view class="picker-popup__header">
          <text class="picker-popup__title">选择{{ pickerParam.paramName }}</text>
          <view @tap="pickerParam = null"><WgIcon name="clear" :size="18" color="#78716C" /></view>
        </view>
        <scroll-view scroll-y class="picker-popup__body">
          <view
            v-for="(opt, idx) in pickerOptions"
            :key="idx"
            class="picker-popup__option"
            :class="{ 'picker-popup__option--active': localValues[pickerParam.id] === opt }"
            @tap="handlePickerConfirm(idx)"
          >
            <text>{{ opt }}</text>
            <WgIcon v-if="localValues[pickerParam.id] === opt" name="check" :size="16" color="#2D6A4F" />
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.params-form {
  &__loading, &__empty {
    padding: $spacing-lg;
    text-align: center;
  }
  &__loading-text, &__empty-text {
    font-size: $font-sm;
    color: $text-placeholder;
  }
}

.param-row {
  padding: $spacing-sm 0;
  border-bottom: 1rpx solid $border-light;

  &:last-child { border-bottom: none; }

  &__header {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    margin-bottom: $spacing-xs;
  }

  &__name {
    font-size: $font-sm;
    color: $text-secondary;
    font-weight: 500;
  }

  &__required {
    color: $color-error;
    font-size: $font-sm;
  }

  &__unit {
    font-size: $font-xs;
    color: $text-placeholder;
    margin-left: auto;
  }

  &__input {
    width: 100%;
    height: 72rpx;
    font-size: $font-md;
    color: $text-primary;
    background: $warm-50;
    border-radius: $radius-md;
    padding: 0 $spacing-md;
  }

  &__select {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 72rpx;
    background: $warm-50;
    border-radius: $radius-md;
    padding: 0 $spacing-md;
  }

  &__value {
    font-size: $font-md;
    color: $text-primary;
  }

  &__placeholder {
    font-size: $font-md;
    color: $text-placeholder;
  }
}

.custom-section {
  padding-top: $spacing-sm;
}

.custom-row {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  margin-bottom: $spacing-xs;

  &__name {
    flex: 2;
    height: 64rpx;
    font-size: $font-sm;
    background: $warm-50;
    border-radius: $radius-sm;
    padding: 0 $spacing-sm;
  }

  &__value {
    flex: 3;
    height: 64rpx;
    font-size: $font-sm;
    background: $warm-50;
    border-radius: $radius-sm;
    padding: 0 $spacing-sm;
  }

  &__remove {
    width: 48rpx;
    height: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
}

.custom-add {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  padding: $spacing-sm;
  border: 2rpx dashed $warm-300;
  border-radius: $radius-md;
  margin-top: $spacing-xs;

  &__text {
    font-size: $font-sm;
    color: $brand-600;
    font-weight: 500;
  }
}

.picker-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 200;
  display: flex;
  align-items: flex-end;
}

.picker-popup {
  width: 100%;
  max-height: 60vh;
  background: #ffffff;
  border-radius: 24rpx 24rpx 0 0;
  overflow: hidden;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-md $spacing-lg;
    border-bottom: 1rpx solid $border-light;
  }

  &__title {
    font-size: $font-lg;
    font-weight: 700;
    color: $text-primary;
  }

  &__body {
    max-height: 50vh;
    padding: $spacing-sm 0;
  }

  &__option {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-md $spacing-lg;

    &--active {
      background: $brand-50;
      color: $brand-600;
      font-weight: 600;
    }
  }
}
</style>
