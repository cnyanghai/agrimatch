<template>
  <view class="schema-aware-picker">
    <!-- 业态选择器 -->
    <view class="section">
      <text class="section-title">选择业态</text>
      <view class="schema-tabs">
        <text
          v-for="schema in schemas"
          :key="schema.schemaCode"
          class="schema-tab"
          :class="{ 'schema-tab--active': selectedSchemaCode === schema.schemaCode }"
          @tap="selectSchema(schema.schemaCode)"
        >
          {{ schema.schemaName }}
        </text>
      </view>
    </view>

    <!-- 品类选择器 -->
    <view class="section">
      <text class="section-title">选择品类</text>
      
      <!-- 级联选择器 -->
      <view v-if="cascaderOptions.length > 0" class="cascader-container">
        <!-- 一级品类 -->
        <view class="level-selector">
          <text
            v-for="option in cascaderOptions"
            :key="option.value"
            class="level-option"
            :class="{ 'level-option--active': selectedLevel1 === option.value }"
            @tap="selectLevel1(option)"
          >
            {{ option.label }}
          </text>
        </view>

        <!-- 二级品类 -->
        <view v-if="level2Options.length > 0" class="level-selector">
          <text
            v-for="option in level2Options"
            :key="option.value"
            class="level-option"
            :class="{ 'level-option--active': selectedLevel2 === option.value }"
            @tap="selectLevel2(option)"
          >
            {{ option.label }}
          </text>
        </view>
      </view>

      <!-- 自定义品类 -->
      <view class="custom-section">
        <text class="custom-title">没有找到合适的品类？</text>
        <view class="custom-row">
          <input
            v-model="customName"
            class="custom-input"
            placeholder="输入自定义品类名称"
          />
          <view class="custom-btn" @tap="showCustomDialog">
            <text>创建品类</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 当前选择 -->
    <view v-if="currentLabel" class="selected-display">
      <text class="selected-label">已选择：</text>
      <text class="selected-value">{{ currentLabel }}</text>
    </view>

    <!-- 自定义品类弹窗 -->
    <view v-if="customDialogVisible" class="custom-dialog" @tap="hideCustomDialog">
      <view class="custom-dialog-content" @tap.stop>
        <text class="dialog-title">创建自定义品类</text>
        <view class="form-group">
          <text class="form-label">品类名称</text>
          <input
            v-model="customName"
            class="form-input"
            placeholder="请输入品类名称"
          />
        </view>
        <view class="form-group">
          <text class="form-label">父级品类（可选）</text>
          <picker
            :value="customParentIndex"
            :range="parentOptions"
            range-key="label"
            @change="onParentChange"
            class="form-picker"
          >
            <view class="picker-display">
              {{ customParentIndex >= 0 ? parentOptions[customParentIndex].label : '请选择父级品类' }}
            </view>
          </picker>
        </view>
        <view class="dialog-actions">
          <view class="dialog-btn dialog-btn--cancel" @tap="hideCustomDialog">
            <text>取消</text>
          </view>
          <view class="dialog-btn dialog-btn--confirm" @tap="createCustomCategory">
            <text>创建</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { getSchemaTree, type ProductSchemaVO, type CategoryNode } from '../api/productSchema'
import { createCustomProduct } from '../api/product'

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

// 级联选择器数据
const cascaderOptions = computed(() => {
  const schema = schemas.value.find(s => s.schemaCode === selectedSchemaCode.value)
  return schema?.categories
    .filter(x => (x.parentId ?? 0) === 0)
    .map(p => ({
      value: p.id,
      label: p.name,
      hasParams: p.hasParams,
      allowCustomName: p.allowCustomName,
      children: (p.children ?? []).map(c => ({
        value: c.id,
        label: c.name,
        hasParams: c.hasParams,
        allowCustomName: c.allowCustomName
      }))
    })) || []
})

const level2Options = computed(() => {
  const selected = cascaderOptions.value.find(opt => opt.value === selectedLevel1.value)
  return selected?.children || []
})

const selectedLevel1 = ref<number>()
const selectedLevel2 = ref<number>()

// 自定义品类
const customDialogVisible = ref(false)
const customName = ref('')
const customParentId = ref<number>()
const customParentIndex = ref(-1)

const parentOptions = computed(() => {
  return cascaderOptions.value.map(opt => ({
    value: opt.value,
    label: opt.label
  }))
})

const currentLabel = computed(() => props.modelValue?.name ?? '')

// 选择业态
function selectSchema(schemaCode: string) {
  selectedSchemaCode.value = schemaCode
  selectedLevel1.value = undefined
  selectedLevel2.value = undefined
  emit('update:modelValue', null)
  emit('schemaChange', schemaCode)
}

// 选择一级品类
function selectLevel1(option: any) {
  selectedLevel1.value = option.value
  selectedLevel2.value = undefined
  
  // 如果没有二级品类，直接选择
  if (!option.children || option.children.length === 0) {
    emitValue(option.value, option.label, option.hasParams, option.allowCustomName)
  }
}

// 选择二级品类
function selectLevel2(option: any) {
  selectedLevel2.value = option.value
  emitValue(option.value, option.label, option.hasParams, option.allowCustomName)
}

// 发送选择值
function emitValue(id: number, name: string, hasParams?: boolean, allowCustomName?: boolean) {
  const category: PickedCategory = {
    id,
    name,
    schemaCode: selectedSchemaCode.value,
    hasParams,
    allowCustomName
  }
  emit('update:modelValue', category)
}

// 显示自定义弹窗
function showCustomDialog() {
  if (!customName.value.trim()) {
    uni.showToast({ title: '请输入品类名称', icon: 'none' })
    return
  }
  customDialogVisible.value = true
}

// 隐藏自定义弹窗
function hideCustomDialog() {
  customDialogVisible.value = false
}

// 父级品类选择变化
function onParentChange(e: any) {
  customParentIndex.value = e.detail.value
  customParentId.value = parentOptions.value[e.detail.value]?.value
}

// 创建自定义品类
async function createCustomCategory() {
  if (!customName.value.trim()) {
    uni.showToast({ title: '请输入品类名称', icon: 'none' })
    return
  }
  
  try {
    uni.showLoading({ title: '创建中...' })
    const result = await createCustomProduct({
      name: customName.value.trim(),
      parentId: customParentId.value
    })
    
    uni.hideLoading()
    uni.showToast({ title: '创建成功', icon: 'success' })
    
    // 重新加载数据
    await loadSchemas()
    
    // 选择新创建的品类
    emitValue(result, customName.value.trim())
    
    // 重置表单
    customName.value = ''
    customParentId.value = undefined
    customParentIndex.value = -1
    hideCustomDialog()
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: '创建失败', icon: 'none' })
  }
}

// 加载业态数据
async function loadSchemas() {
  loading.value = true
  try {
    const r = await getSchemaTree()
    schemas.value = r || []
  } catch (e: any) {
    uni.showToast({ title: '加载业态失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 重置选择
function reset() {
  selectedLevel1.value = undefined
  selectedLevel2.value = undefined
  emit('update:modelValue', null)
}

// 暴露方法
defineExpose({
  reset
})

onMounted(() => {
  loadSchemas()
})

// 监听props变化
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    selectedSchemaCode.value = newVal.schemaCode
    // 根据选择恢复级联状态
    const schema = schemas.value.find(s => s.schemaCode === newVal.schemaCode)
    if (schema) {
      const parent = schema.categories.find(c => c.children?.some(child => child.id === newVal.id))
      if (parent) {
        selectedLevel1.value = parent.id
        selectedLevel2.value = newVal.id
      } else {
        selectedLevel1.value = newVal.id
        selectedLevel2.value = undefined
      }
    }
  }
})
</script>

<style lang="scss" scoped>
.schema-aware-picker {
  padding: $spacing-md;
}

.section {
  margin-bottom: $spacing-lg;
}

.section-title {
  font-size: $font-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.schema-tabs {
  display: flex;
  gap: $spacing-sm;
  flex-wrap: wrap;
}

.schema-tab {
  padding: $spacing-sm $spacing-md;
  border: 2rpx solid $warm-100;
  border-radius: $radius-md;
  font-size: $font-sm;
  color: $text-secondary;
  background: $bg-card;
  transition: all 0.2s ease;

  &--active {
    background: $brand-50;
    border-color: $brand-500;
    color: $brand-600;
  }
}

.cascader-container {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.level-selector {
  flex: 1;
  background: $bg-card;
  border-radius: $radius-md;
  border: 1rpx solid $warm-100;
  max-height: 300rpx;
  overflow-y: auto;
}

.level-option {
  display: block;
  padding: $spacing-md;
  font-size: $font-sm;
  color: $text-primary;
  border-bottom: 1rpx solid $warm-50;
  
  &:last-child {
    border-bottom: none;
  }
  
  &--active {
    background: $brand-50;
    color: $brand-600;
    font-weight: 500;
  }
}

.custom-section {
  margin-top: $spacing-md;
  padding: $spacing-md;
  background: $warm-50;
  border-radius: $radius-md;
  border: 1rpx solid $warm-200;
}

.custom-title {
  font-size: $font-sm;
  color: $text-secondary;
  margin-bottom: $spacing-sm;
}

.custom-row {
  display: flex;
  gap: $spacing-sm;
  align-items: center;
}

.custom-input {
  flex: 1;
  padding: $spacing-sm;
  border: 1rpx solid $warm-200;
  border-radius: $radius-sm;
  font-size: $font-sm;
  background: $bg-card;
}

.custom-btn {
  padding: $spacing-sm $spacing-md;
  background: $brand-600;
  color: $bg-card;
  border-radius: $radius-sm;
  font-size: $font-sm;
  font-weight: 500;
}

.selected-display {
  display: flex;
  align-items: center;
  padding: $spacing-md;
  background: $brand-50;
  border-radius: $radius-md;
  border: 1rpx solid $brand-200;
}

.selected-label {
  font-size: $font-sm;
  color: $text-secondary;
  margin-right: $spacing-sm;
}

.selected-value {
  font-size: $font-sm;
  color: $brand-600;
  font-weight: 500;
}

.custom-dialog {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.custom-dialog-content {
  width: 600rpx;
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-lg;
}

.dialog-title {
  font-size: $font-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-lg;
  text-align: center;
}

.form-group {
  margin-bottom: $spacing-lg;
}

.form-label {
  font-size: $font-sm;
  color: $text-secondary;
  margin-bottom: $spacing-sm;
  display: block;
}

.form-input {
  width: 100%;
  padding: $spacing-md;
  border: 1rpx solid $warm-200;
  border-radius: $radius-sm;
  font-size: $font-sm;
  background: $bg-card;
}

.form-picker {
  width: 100%;
  padding: $spacing-md;
  border: 1rpx solid $warm-200;
  border-radius: $radius-sm;
  background: $bg-card;
}

.picker-display {
  font-size: $font-sm;
  color: $text-primary;
}

.dialog-actions {
  display: flex;
  gap: $spacing-md;
  margin-top: $spacing-lg;
}

.dialog-btn {
  flex: 1;
  padding: $spacing-md;
  text-align: center;
  border-radius: $radius-sm;
  font-size: $font-sm;
  font-weight: 500;
  
  &--cancel {
    background: $warm-100;
    color: $text-secondary;
  }
  
  &--confirm {
    background: $brand-600;
    color: $bg-card;
  }
}
</style>