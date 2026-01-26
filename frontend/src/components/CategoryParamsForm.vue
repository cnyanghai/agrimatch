<script setup lang="ts">
import { computed, watch } from 'vue'
import { ChevronDown, AlertCircle, HelpCircle } from 'lucide-vue-next'
import type { ProductParamResponse } from '../api/product'

const props = defineProps<{
  params: ProductParamResponse[]
  modelValue: Record<string, any>
}>()

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, any>]
}>()

// 按参数分组归类
const groupedParams = computed(() => {
  const groups: Record<string, { label: string; params: ProductParamResponse[] }> = {
    breed: { label: '品种信息', params: [] },
    biology: { label: '生物指标', params: [] },
    quality: { label: '品质规格', params: [] },
    logistics: { label: '物流包装', params: [] },
    trade: { label: '交易条款', params: [] }
  }

  props.params.forEach(param => {
    const group = param.paramGroup || 'quality'
    if (groups[group]) {
      groups[group].params.push(param)
    } else {
      groups.quality.params.push(param)
    }
  })

  // 只返回有参数的分组
  return Object.entries(groups)
    .filter(([_, g]) => g.params.length > 0)
    .map(([key, g]) => ({
      key,
      label: g.label,
      params: g.params.sort((a, b) => (a.sort || 0) - (b.sort || 0))
    }))
})

// 更新参数值
function updateValue(paramId: number, value: any) {
  const newValues = { ...props.modelValue }
  newValues[paramId] = value
  emit('update:modelValue', newValues)
}

// 获取参数当前值
function getValue(paramId: number): any {
  return props.modelValue[paramId] ?? ''
}

// 解析选项字符串
function parseOptions(param: ProductParamResponse): string[] {
  if (!param.options) return []
  if (Array.isArray(param.options)) return param.options
  try {
    return JSON.parse(param.options as unknown as string)
  } catch {
    return []
  }
}
</script>

<template>
  <div class="space-y-6">
    <!-- 无参数提示 -->
    <div v-if="params.length === 0" class="text-center py-8">
      <div class="w-12 h-12 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-3">
        <HelpCircle class="w-6 h-6 text-gray-400" />
      </div>
      <p class="text-sm text-gray-500">该品类暂无预设参数</p>
      <p class="text-xs text-gray-400 mt-1">您可以在下方添加自定义标签</p>
    </div>

    <!-- 分组参数表单 -->
    <div v-for="group in groupedParams" :key="group.key" class="space-y-4">
      <!-- 分组标题 -->
      <div class="flex items-center gap-2">
        <div class="w-1 h-4 rounded-full" :class="{
          'bg-purple-500': group.key === 'breed',
          'bg-blue-500': group.key === 'biology',
          'bg-brand-500': group.key === 'quality',
          'bg-amber-500': group.key === 'logistics',
          'bg-rose-500': group.key === 'trade'
        }"></div>
        <span class="text-xs font-bold text-gray-500 uppercase tracking-wider">{{ group.label }}</span>
      </div>

      <!-- 参数网格 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="param in group.params"
          :key="param.id"
          class="bg-white border border-gray-200 rounded-xl p-4 hover:border-brand-200 transition-all"
        >
          <!-- 参数标签 -->
          <label class="flex items-center gap-1.5 text-xs font-bold text-gray-600 mb-2">
            {{ param.paramName }}
            <span v-if="param.required" class="text-red-500">*</span>
            <span v-if="param.unit" class="text-gray-400 font-normal">（{{ param.unit }}）</span>
          </label>

          <!-- 下拉选择类型 (paramType = 1) -->
          <el-select
            v-if="param.paramType === 1"
            :model-value="getValue(param.id)"
            @update:model-value="(v: string) => updateValue(param.id, v)"
            placeholder="请选择"
            class="w-full neo-select"
            clearable
          >
            <el-option
              v-for="opt in parseOptions(param)"
              :key="opt"
              :label="opt"
              :value="opt"
            />
          </el-select>

          <!-- 文本输入类型 (paramType = 0) -->
          <input
            v-else
            type="text"
            :value="getValue(param.id)"
            @input="(e: Event) => updateValue(param.id, (e.target as HTMLInputElement).value)"
            :placeholder="param.placeholder || `请输入${param.paramName}`"
            class="w-full px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:border-brand-500 focus:bg-white outline-none transition-all"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.neo-select :deep(.el-select__wrapper) {
  border: 1px solid rgb(229 231 235);
  border-radius: 8px;
  background-color: rgb(249 250 251);
  box-shadow: none;
  transition: all 0.15s ease;
}
.neo-select :deep(.el-select__wrapper:hover) {
  border-color: rgb(209 213 219);
}
.neo-select :deep(.el-select__wrapper.is-focus) {
  border-color: rgb(16 185 129);
  background-color: white;
  box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.1);
}
</style>
