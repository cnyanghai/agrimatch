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

// 输入框样式（使用新的设计系统）
const inputClasses = computed(() => {
  const base = 'w-full px-4 py-3 border rounded-xl outline-none transition-all duration-200 text-sm'
  const focus = 'focus:border-primary focus:ring-2 focus:ring-primary/50'
  const disabled = props.disabled ? 'bg-background text-text-secondary cursor-not-allowed' : 'bg-surface'
  const error = props.error ? 'border-error focus:border-error focus:ring-error/50' : 'border-border'

  return `${base} ${focus} ${disabled} ${error}`
})
</script>

<template>
  <div class="space-y-1.5">
    <!-- 标签 -->
    <label v-if="label" class="block text-xs font-bold text-text-secondary uppercase tracking-wider">
      {{ label }}
      <span v-if="required" class="text-error ml-0.5">*</span>
    </label>

    <!-- 输入框容器 -->
    <div class="relative">
      <!-- 前缀图标插槽 -->
      <div v-if="$slots.prefix" class="absolute left-4 top-1/2 -translate-y-1/2 text-text-secondary">
        <slot name="prefix" />
      </div>

      <!-- 文本域 -->
      <textarea
        v-if="type === 'textarea'"
        v-model="inputValue"
        :class="[inputClasses, $slots.prefix ? 'pl-12' : '', $slots.suffix ? 'pr-12' : '']"
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
        :class="[inputClasses, $slots.prefix ? 'pl-12' : '', $slots.suffix ? 'pr-12' : '']"
        :placeholder="placeholder"
        :disabled="disabled"
        @focus="emit('focus')"
        @blur="emit('blur')"
      />

      <!-- 后缀图标插槽 -->
      <div v-if="$slots.suffix" class="absolute right-4 top-1/2 -translate-y-1/2 text-text-secondary">
        <slot name="suffix" />
      </div>
    </div>

    <!-- 错误提示 -->
    <p v-if="error" class="text-xs text-error font-medium">
      {{ error }}
    </p>

    <!-- 提示信息 -->
    <p v-else-if="hint" class="text-xs text-text-secondary">
      {{ hint }}
    </p>
  </div>
</template>
