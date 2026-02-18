<script setup lang="ts">
/**
 * WgPriceTag — Stitch 价格展示
 *
 * 整数·小数分离排版，¥ 符号自动缩小
 * 用法：<WgPriceTag :value="1280.50" unit="元/吨" />
 */
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  value?: number | string | null
  unit?: string
  size?: 'sm' | 'md' | 'lg'
  prefix?: string
}>(), {
  value: null,
  unit: '',
  size: 'md',
  prefix: '¥',
})

const parts = computed(() => {
  if (props.value == null || props.value === '') return null
  const n = typeof props.value === 'string' ? parseFloat(props.value) : props.value
  if (isNaN(n)) return null
  const [int, dec] = n.toFixed(2).split('.')
  return { int, dec: dec === '00' ? '' : `.${dec}` }
})
</script>

<template>
  <view class="wg-price" :class="`wg-price--${size}`">
    <template v-if="parts">
      <text class="wg-price__symbol">{{ prefix }}</text>
      <text class="wg-price__int">{{ parts.int }}</text>
      <text v-if="parts.dec" class="wg-price__dec">{{ parts.dec }}</text>
    </template>
    <text v-else class="wg-price__empty">面议</text>
    <text v-if="unit" class="wg-price__unit">{{ unit }}</text>
  </view>
</template>

<style lang="scss" scoped>
.wg-price {
  display: inline-flex;
  align-items: baseline;
  color: $accent-400;
  font-weight: 800;

  &--lg {
    .wg-price__symbol { font-size: $font-lg; }
    .wg-price__int { font-size: 64rpx; }
    .wg-price__dec { font-size: $font-lg; }
    .wg-price__empty { font-size: $font-xl; }
    .wg-price__unit { font-size: $font-sm; }
  }

  &--md {
    .wg-price__symbol { font-size: $font-sm; }
    .wg-price__int { font-size: 48rpx; }
    .wg-price__dec { font-size: $font-sm; }
    .wg-price__empty { font-size: $font-lg; }
    .wg-price__unit { font-size: $font-xs; }
  }

  &--sm {
    .wg-price__symbol { font-size: $font-xs; }
    .wg-price__int { font-size: $font-lg; }
    .wg-price__dec { font-size: $font-xs; }
    .wg-price__empty { font-size: $font-md; }
    .wg-price__unit { font-size: 20rpx; }
  }

  &__symbol {
    font-weight: 700;
    margin-right: 2rpx;
  }

  &__int {
    font-family: $font-mono;
    letter-spacing: -1rpx;
  }

  &__dec {
    font-family: $font-mono;
    font-weight: 700;
  }

  &__empty {
    font-weight: 700;
  }

  &__unit {
    color: $text-secondary;
    font-weight: 400;
    margin-left: $spacing-xs;
  }
}
</style>
