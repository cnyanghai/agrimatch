<script setup lang="ts">
/**
 * WgCategoryPicker - 品类选择器组件（uni-app版）
 * 
 * 移植自Web端 SchemaAwareCategoryPicker，适配移动端交互：
 * - 业态Tab切换（横向滚动）
 * - 一级品类Popup选择
 * - 二级品类列表选择
 * - 支持自定义品类创建
 * - 支持搜索过滤
 */
import { ref, computed, watch, onMounted } from 'vue'
import { getSchemaTree, type ProductSchemaVO, type CategoryNode } from '../api/productSchema'
import { createCustomProduct } from '../api/product'
import { WARM_400, BRAND_600 } from '../constants/colors'

export interface PickedCategory {
  id: number
  name: string
  schemaCode: string
  hasParams?: boolean
  allowCustomName?: boolean
}

const props = defineProps<{
  modelValue?: PickedCategory | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: PickedCategory | null): void
  (e: 'schemaChange', schemaCode: string): void
}>()

const loading = ref(false)
const schemas = ref<ProductSchemaVO[]>([])
const selectedSchemaCode = ref<string>('feed')

// 选择状态
const selectedParentId = ref<number | null>(null)
const showPicker = ref(false)
const searchKeyword = ref('')

// 自定义品类
const showCustomDialog = ref(false)
const customName = ref('')
const customSubmitting = ref(false)

// 当前业态下的品类树
const currentCategories = computed(() => {
  const schema = schemas.value.find(s => s.schemaCode === selectedSchemaCode.value)
  return schema?.categories || []
})

// 一级品类（顶级节点）
const topCategories = computed(() =>
  currentCategories.value.filter(x => (x.parentId ?? 0) === 0)
)

// 当前选中一级品类的子品类
const childCategories = computed(() => {
  if (!selectedParentId.value) return []
  const parent = topCategories.value.find(p => p.id === selectedParentId.value)
  return parent?.children || []
})

// 搜索过滤后的子品类
const filteredChildren = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()
  if (!kw) return childCategories.value
  return childCategories.value.filter(c => c.name.toLowerCase().includes(kw))
})

// 当前显示的选中文本
const displayText = computed(() => {
  if (!props.modelValue) return ''
  return props.modelValue.name
})

async function loadSchemas() {
  loading.value = true
  try {
    const data = await getSchemaTree()
    schemas.value = (Array.isArray(data) ? data : []) as ProductSchemaVO[]
    // 恢复已选值
    if (props.modelValue?.schemaCode) {
      selectedSchemaCode.value = props.modelValue.schemaCode
    }
  } catch {
    uni.showToast({ title: '加载品类失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function switchSchema(code: string) {
  if (code === selectedSchemaCode.value) return
  selectedSchemaCode.value = code
  selectedParentId.value = null
  searchKeyword.value = ''
  emit('update:modelValue', null)
  emit('schemaChange', code)
}

function selectParent(id: number) {
  selectedParentId.value = id
  searchKeyword.value = ''
}

function selectChild(node: CategoryNode) {
  emit('update:modelValue', {
    id: node.id,
    name: node.name,
    schemaCode: selectedSchemaCode.value,
    hasParams: node.hasParams,
    allowCustomName: node.allowCustomName,
  })
  showPicker.value = false
}

function openPicker() {
  showPicker.value = true
  // 默认选中第一个一级品类
  if (!selectedParentId.value && topCategories.value.length > 0) {
    selectedParentId.value = topCategories.value[0].id
  }
}

function openCustomDialog() {
  if (!selectedParentId.value) {
    uni.showToast({ title: '请先选择一级品类', icon: 'none' })
    return
  }
  customName.value = searchKeyword.value.trim()
  showCustomDialog.value = true
}

async function submitCustom() {
  const name = customName.value.trim()
  if (!name) {
    uni.showToast({ title: '请输入品类名称', icon: 'none' })
    return
  }
  if (!selectedParentId.value) return

  customSubmitting.value = true
  try {
    const newId = await createCustomProduct({ parentId: selectedParentId.value, name })
    await loadSchemas()
    emit('update:modelValue', {
      id: newId as number,
      name,
      schemaCode: selectedSchemaCode.value,
      hasParams: false,
      allowCustomName: true,
    })
    showCustomDialog.value = false
    showPicker.value = false
    uni.showToast({ title: '已创建并选中', icon: 'success' })
  } catch {
    uni.showToast({ title: '创建失败', icon: 'none' })
  } finally {
    customSubmitting.value = false
  }
}

function clearSelection() {
  emit('update:modelValue', null)
}

// 监听外部值变化
watch(() => props.modelValue?.schemaCode, (code) => {
  if (code && code !== selectedSchemaCode.value) {
    selectedSchemaCode.value = code
  }
})

onMounted(loadSchemas)
</script>

<template>
  <view class="category-picker">
    <!-- 当前选中值 / 触发选择 -->
    <view class="category-picker__trigger" @tap="openPicker">
      <text v-if="displayText" class="category-picker__value">{{ displayText }}</text>
      <text v-else class="category-picker__placeholder">请选择品类</text>
      <view v-if="displayText" class="category-picker__clear" @tap.stop="clearSelection">
        <WgIcon name="clear" :size="16" :color="WARM_400" />
      </view>
      <text v-else class="category-picker__arrow">&#x203A;</text>
    </view>

    <!-- 品类选择弹窗 -->
    <view v-if="showPicker" class="picker-mask" @tap="showPicker = false">
      <view class="picker-popup" @tap.stop>
        <!-- 业态Tab -->
        <scroll-view scroll-x class="picker-popup__schema-bar">
          <view class="picker-popup__schema-list">
            <view
              v-for="schema in schemas"
              :key="schema.schemaCode"
              class="picker-popup__schema-item"
              :class="{ 'picker-popup__schema-item--active': selectedSchemaCode === schema.schemaCode }"
              @tap="switchSchema(schema.schemaCode)"
            >
              <text class="picker-popup__schema-text">{{ schema.schemaName }}</text>
            </view>
          </view>
        </scroll-view>

        <!-- 搜索框 -->
        <view class="picker-popup__search">
          <input
            class="picker-popup__search-input"
            v-model="searchKeyword"
            placeholder="搜索品类..."
            :maxlength="30"
          />
        </view>

        <!-- 品类两栏布局 -->
        <view class="picker-popup__body">
          <!-- 左侧一级品类 -->
          <scroll-view scroll-y class="picker-popup__left">
            <view
              v-for="parent in topCategories"
              :key="parent.id"
              class="picker-popup__parent"
              :class="{ 'picker-popup__parent--active': selectedParentId === parent.id }"
              @tap="selectParent(parent.id)"
            >
              <text class="picker-popup__parent-text">{{ parent.name }}</text>
            </view>
          </scroll-view>

          <!-- 右侧二级品类 -->
          <scroll-view scroll-y class="picker-popup__right">
            <view v-if="filteredChildren.length > 0" class="picker-popup__children">
              <view
                v-for="child in filteredChildren"
                :key="child.id"
                class="picker-popup__child"
                :class="{ 'picker-popup__child--selected': modelValue?.id === child.id }"
                @tap="selectChild(child)"
              >
                <text class="picker-popup__child-text">{{ child.name }}</text>
                <WgIcon v-if="modelValue?.id === child.id" name="check" :size="14" :color="BRAND_600" />
              </view>
            </view>
            <view v-else class="picker-popup__empty">
              <text class="picker-popup__empty-text">未找到匹配品类</text>
            </view>

            <!-- 自定义创建入口 -->
            <view class="picker-popup__custom-btn" @tap="openCustomDialog">
              <WgIcon name="plus" :size="14" :color="BRAND_600" />
              <text class="picker-popup__custom-text">找不到？手动创建</text>
            </view>
          </scroll-view>
        </view>
      </view>
    </view>

    <!-- 自定义品类弹窗 -->
    <view v-if="showCustomDialog" class="picker-mask" @tap="showCustomDialog = false">
      <view class="custom-dialog" @tap.stop>
        <text class="custom-dialog__title">自定义品类</text>
        <view class="custom-dialog__field">
          <text class="custom-dialog__label">所属大类</text>
          <text class="custom-dialog__readonly">{{ topCategories.find(p => p.id === selectedParentId)?.name || '-' }}</text>
        </view>
        <view class="custom-dialog__field">
          <text class="custom-dialog__label">品类名称</text>
          <input
            class="custom-dialog__input"
            v-model="customName"
            placeholder="例如：特种鱼苗、二手料车..."
            :maxlength="30"
          />
        </view>
        <view class="custom-dialog__actions">
          <view class="custom-dialog__btn custom-dialog__btn--cancel" @tap="showCustomDialog = false">
            <text class="custom-dialog__btn-text">取消</text>
          </view>
          <view
            class="custom-dialog__btn custom-dialog__btn--confirm"
            :class="{ 'custom-dialog__btn--disabled': customSubmitting }"
            @tap="submitCustom"
          >
            <text class="custom-dialog__btn-text custom-dialog__btn-text--white">
              {{ customSubmitting ? '创建中...' : '确认创建' }}
            </text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.category-picker {
  &__trigger {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 80rpx;
    padding: 0;
  }

  &__value {
    font-size: $font-md;
    color: $text-primary;
    font-weight: 600;
  }

  &__placeholder {
    font-size: $font-md;
    color: $text-placeholder;
  }

  &__clear {
    padding: $spacing-xs;
  }

  &__arrow {
    font-size: $font-lg;
    color: $text-placeholder;
  }
}

/* 遮罩 */
.picker-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
}

/* 弹窗主体 */
.picker-popup {
  width: 100%;
  max-height: 80vh;
  background: $bg-card;
  border-radius: $radius-xl $radius-xl 0 0;
  overflow: hidden;

  &__schema-bar {
    padding: $spacing-sm $spacing-md;
    border-bottom: 1rpx solid $border-light;
    white-space: nowrap;
  }

  &__schema-list {
    display: flex;
    gap: $spacing-sm;
  }

  &__schema-item {
    display: inline-flex;
    padding: $spacing-xs $spacing-md;
    border-radius: 30rpx;
    background: $bg-page;
    flex-shrink: 0;

    &--active {
      background: $brand-600;
    }
  }

  &__schema-text {
    font-size: $font-sm;
    color: $text-secondary;
    font-weight: 600;

    .picker-popup__schema-item--active & {
      color: $text-inverse;
    }
  }

  &__search {
    padding: $spacing-sm $spacing-md;
    border-bottom: 1rpx solid $border-light;
  }

  &__search-input {
    width: 100%;
    height: 72rpx;
    background: $bg-page;
    border-radius: $radius-md;
    padding: 0 $spacing-md;
    font-size: $font-sm;
  }

  &__body {
    display: flex;
    height: 60vh;
  }

  &__left {
    width: 240rpx;
    background: $bg-page;
    border-right: 1rpx solid $border-light;
  }

  &__parent {
    padding: $spacing-md;
    border-left: 6rpx solid transparent;
    transition: all 0.2s;

    &--active {
      background: $bg-card;
      border-left-color: $brand-600;
    }
  }

  &__parent-text {
    font-size: $font-sm;
    color: $text-secondary;

    .picker-popup__parent--active & {
      color: $brand-600;
      font-weight: bold;
    }
  }

  &__right {
    flex: 1;
    padding: $spacing-sm;
  }

  &__children {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
  }

  &__child {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    padding: $spacing-sm $spacing-md;
    border-radius: $radius-md;
    background: $bg-page;
    transition: all 0.2s;

    &--selected {
      background: $brand-50;
      border: 2rpx solid $brand-600;
    }
  }

  &__child-text {
    font-size: $font-sm;
    color: $text-primary;

    .picker-popup__child--selected & {
      color: $brand-600;
      font-weight: 600;
    }
  }

  &__empty {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: $spacing-xl;
  }

  &__empty-text {
    font-size: $font-sm;
    color: $text-placeholder;
  }

  &__custom-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-xs;
    margin-top: $spacing-md;
    padding: $spacing-sm;
    border: 2rpx dashed $border-color;
    border-radius: $radius-md;
  }

  &__custom-text {
    font-size: $font-sm;
    color: $brand-600;
    font-weight: 600;
  }
}

/* 自定义品类弹窗 */
.custom-dialog {
  width: 85%;
  margin: auto auto;
  background: $bg-card;
  border-radius: $radius-xl;
  padding: $spacing-lg;

  &__title {
    font-size: $font-xl;
    font-weight: bold;
    color: $text-primary;
    display: block;
    margin-bottom: $spacing-lg;
    text-align: center;
  }

  &__field {
    margin-bottom: $spacing-md;
  }

  &__label {
    font-size: $font-xs;
    color: $text-secondary;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 2rpx;
    display: block;
    margin-bottom: $spacing-xs;
  }

  &__readonly {
    font-size: $font-md;
    color: $text-secondary;
    background: $bg-page;
    padding: $spacing-sm $spacing-md;
    border-radius: $radius-md;
    display: block;
  }

  &__input {
    width: 100%;
    height: 80rpx;
    border: 2rpx solid $border-color;
    border-radius: $radius-md;
    padding: 0 $spacing-md;
    font-size: $font-md;
  }

  &__actions {
    display: flex;
    gap: $spacing-sm;
    margin-top: $spacing-lg;
  }

  &__btn {
    flex: 1;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $radius-md;

    &--cancel {
      background: $bg-page;
    }

    &--confirm {
      background: $brand-600;
    }

    &--disabled {
      opacity: 0.5;
    }
  }

  &__btn-text {
    font-size: $font-md;
    font-weight: bold;
    color: $text-secondary;

    &--white {
      color: $text-inverse;
    }
  }
}
</style>
