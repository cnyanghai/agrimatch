<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  modelValue?: string | number
  label?: string
  placeholder?: string
  type?: 'text' | 'password' | 'email' | 'number' | 'tel' | 'textarea'
  required?: boolean
  disabled?: boolean
  error?: string
  hint?: string
  rows?: number
}>(), {
  type: 'text',
  required: false,
  disabled: false,
  rows: 3
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | number): void
  (e: 'focus'): void
  (e: 'blur'): void
}>()

const inputValue = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val ?? '')
})

// 输入框样式
const inputClasses = computed(() => {
  const base = 'w-full px-4 py-2.5 border-2 rounded-xl outline-none transition-all text-sm'
  const focus = 'focus:border-emerald-500 focus:ring-2 focus:ring-emerald-50'
  const disabled = props.disabled ? 'bg-gray-50 text-gray-400 cursor-not-allowed' : 'bg-white'
  const error = props.error ? 'border-red-300 focus:border-red-500 focus:ring-red-50' : 'border-gray-100'
  
  return `${base} ${focus} ${disabled} ${error}`
})
</script>

<template>
  <div class="space-y-1.5">
    <!-- 标签 -->
    <label v-if="label" class="block text-xs font-bold text-gray-500 uppercase tracking-wider">
      {{ label }}
      <span v-if="required" class="text-red-500 ml-0.5">*</span>
    </label>
    
    <!-- 输入框容器 -->
    <div class="relative">
      <!-- 前缀图标插槽 -->
      <div v-if="$slots.prefix" class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
        <slot name="prefix" />
      </div>
      
      <!-- 文本域 -->
      <textarea
        v-if="type === 'textarea'"
        v-model="inputValue"
        :class="[inputClasses, $slots.prefix ? 'pl-10' : '', $slots.suffix ? 'pr-10' : '']"
        :placeholder="placeholder"
        :disabled="disabled"
        :rows="rows"
        @focus="emit('focus')"
        @blur="emit('blur')"
      />
      
      <!-- 普通输入框 -->
      <input
        v-else
        v-model="inputValue"
        :type="type"
        :class="[inputClasses, $slots.prefix ? 'pl-10' : '', $slots.suffix ? 'pr-10' : '']"
        :placeholder="placeholder"
        :disabled="disabled"
        @focus="emit('focus')"
        @blur="emit('blur')"
      />
      
      <!-- 后缀图标插槽 -->
      <div v-if="$slots.suffix" class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
        <slot name="suffix" />
      </div>
    </div>
    
    <!-- 错误提示 -->
    <p v-if="error" class="text-xs text-red-500 font-medium">
      {{ error }}
    </p>
    
    <!-- 提示信息 -->
    <p v-else-if="hint" class="text-xs text-gray-400">
      {{ hint }}
    </p>
  </div>
</template>

