<script setup lang="ts">
import { computed } from 'vue'
import { WARM_400, WARM_300 } from '../constants/colors'

const props = withDefaults(defineProps<{
  label?: string
  required?: boolean
  placeholder?: string
  modelValue?: string | number
  type?: 'text' | 'number' | 'password' | 'textarea' | 'picker' | 'readonly'
  maxlength?: number
  icon?: string
  error?: string
  disabled?: boolean
  pickerText?: string
  rows?: number
}>(), {
  label: '',
  required: false,
  placeholder: '请输入',
  type: 'text',
  maxlength: -1,
  icon: '',
  error: '',
  disabled: false,
  pickerText: '',
  rows: 3,
})

const emit = defineEmits<{
  'update:modelValue': [val: string | number]
  tap: []
}>()

const inputType = computed(() => {
  if (props.type === 'number') return 'number'
  if (props.type === 'password') return 'text'
  return 'text'
})

function onInput(e: any) {
  emit('update:modelValue', e.detail.value)
}
</script>

<template>
  <view
    class="wg-field"
    :class="{
      'wg-field--error': !!error,
      'wg-field--disabled': disabled,
      'wg-field--picker': type === 'picker',
    }"
    @tap="type === 'picker' ? emit('tap') : undefined"
  >
    <view v-if="label" class="wg-field__label-row">
      <text class="wg-field__label">{{ label }}</text>
      <text v-if="required" class="wg-field__required">*</text>
    </view>

    <view class="wg-field__body">
      <view v-if="icon" class="wg-field__icon">
        <WgIcon :name="icon" :size="18" :color="WARM_400" />
      </view>

      <!-- readonly -->
      <text v-if="type === 'readonly'" class="wg-field__readonly">
        {{ modelValue || placeholder }}
      </text>

      <!-- picker -->
      <template v-else-if="type === 'picker'">
        <text
          class="wg-field__picker-text"
          :class="{ 'wg-field__picker-text--placeholder': !pickerText }"
        >{{ pickerText || placeholder }}</text>
        <WgIcon name="chevron-right" :size="16" :color="WARM_300" />
      </template>

      <!-- textarea -->
      <textarea
        v-else-if="type === 'textarea'"
        class="wg-field__textarea"
        :value="String(modelValue ?? '')"
        :placeholder="placeholder"
        :maxlength="maxlength"
        :disabled="disabled"
        :auto-height="true"
        placeholder-class="wg-field__placeholder"
        @input="onInput"
      />

      <!-- input -->
      <input
        v-else
        class="wg-field__input"
        :type="inputType"
        :password="type === 'password'"
        :value="String(modelValue ?? '')"
        :placeholder="placeholder"
        :maxlength="maxlength"
        :disabled="disabled"
        placeholder-class="wg-field__placeholder"
        @input="onInput"
      />

      <slot name="suffix" />
    </view>

    <text v-if="error" class="wg-field__error">{{ error }}</text>
  </view>
</template>

<style lang="scss" scoped>
.wg-field {
  margin-bottom: $spacing-md;

  &--disabled {
    opacity: 0.5;
    pointer-events: none;
  }

  &__label-row {
    display: flex;
    align-items: center;
    gap: 4rpx;
    margin-bottom: $spacing-xs;
    padding-left: 4rpx;
  }

  &__label {
    font-size: $font-sm;
    font-weight: 600;
    color: $text-primary;
  }

  &__required {
    font-size: $font-sm;
    color: $color-error;
    font-weight: 700;
  }

  &__body {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    min-height: 92rpx;
    background: $warm-100;
    border-radius: $radius-lg;
    padding: 0 $spacing-md;
    border: 2rpx solid transparent;
    transition: border-color $transition-fast, background $transition-fast;
  }

  &--error &__body {
    border-color: $color-error;
    background: rgba(239, 68, 68, 0.04);
  }

  &--picker &__body {
    cursor: pointer;
  }

  &--picker:active &__body {
    background: $warm-200;
  }

  &__icon {
    flex-shrink: 0;
  }

  &__input {
    flex: 1;
    height: 92rpx;
    font-size: $font-md;
    color: $text-primary;
    background: transparent;
    box-sizing: border-box;
  }

  &__textarea {
    flex: 1;
    min-height: 160rpx;
    font-size: $font-md;
    color: $text-primary;
    padding: $spacing-sm 0;
    background: transparent;
    box-sizing: border-box;
    line-height: 1.5;
  }

  &__readonly {
    flex: 1;
    font-size: $font-md;
    color: $text-secondary;
    padding: $spacing-sm 0;
  }

  &__picker-text {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;

    &--placeholder {
      color: $text-placeholder;
    }
  }

  &__placeholder {
    color: $text-placeholder;
    font-size: $font-sm;
  }

  &__error {
    display: block;
    font-size: $font-xs;
    color: $color-error;
    margin-top: $spacing-xs;
    padding-left: 4rpx;
  }
}
</style>
