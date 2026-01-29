<script setup lang="ts">
import { computed } from 'vue'
import { Package, ShoppingCart } from 'lucide-vue-next'

export interface ProductInfoData {
  categoryName: string
  quantity?: number
  quantityUnit?: string
  price?: number | string
  priceUnit?: string
  priceLabel?: string // 出厂价/期望价
  address?: string
  addressLabel?: string // 发货地/收货地
  packaging?: string
  paymentMethod?: string
  paramsJson?: string
}

const props = withDefaults(defineProps<{
  data: ProductInfoData
  type?: 'supply' | 'purchase'
  showHeader?: boolean
  showIcon?: boolean
}>(), {
  type: 'supply',
  showHeader: true,
  showIcon: true
})

// 解析质量参数为标签数组
const paramTags = computed(() => {
  if (!props.data.paramsJson) return []
  try {
    const params = JSON.parse(props.data.paramsJson)
    if (typeof params !== 'object' || params === null) return []
    const entries = Object.entries(params).filter(([_, v]) => v !== undefined && v !== '')
    return entries.slice(0, 5).map(([k, v]) => `${k}${v}`)
  } catch {
    return []
  }
})

// 格式化价格
const formattedPrice = computed(() => {
  const price = props.data.price
  if (price === undefined || price === null || price === '') return '面议'
  if (typeof price === 'string') return price
  return `¥${price.toLocaleString()}`
})

// 包装/付款组合
const tradeTerms = computed(() => {
  const terms = [props.data.packaging, props.data.paymentMethod].filter(Boolean)
  return terms.length > 0 ? terms.join('/') : '—'
})

// 是否有操作插槽
const hasActions = computed(() => !!props.data)
</script>

<template>
  <div class="flex gap-3">
    <!-- 左侧：品类图标 + 名称（可选） -->
    <div v-if="showIcon" class="shrink-0 w-[68px] flex flex-col items-center justify-center">
      <div
        class="w-11 h-11 rounded-xl flex items-center justify-center mb-1"
        :class="type === 'supply' ? 'bg-gradient-to-br from-brand-50 to-brand-100' : 'bg-gradient-to-br from-autumn-50 to-autumn-100'"
      >
        <Package v-if="type === 'supply'" class="w-5 h-5 text-brand-600" />
        <ShoppingCart v-else class="w-5 h-5 text-autumn-600" />
      </div>
      <span class="text-xs font-bold text-gray-900 text-center leading-tight line-clamp-2">{{ data.categoryName }}</span>
    </div>

    <!-- 右侧：数据格 + 状态 + 操作 -->
    <div class="flex-1 min-w-0">
      <div
        class="product-info-grid"
        :class="[
          showIcon
            ? ($slots.status && $slots.actions ? 'with-icon-7' : $slots.status || $slots.actions ? 'with-icon-6' : 'with-icon-5')
            : ($slots.status && $slots.actions ? 'no-icon-8' : $slots.status || $slots.actions ? 'no-icon-7' : 'no-icon-6')
        ]"
      >
        <!-- 标题行 -->
        <template v-if="showHeader">
          <!-- 品名列标题（无图标时显示） -->
          <div v-if="!showIcon" class="text-[10px] font-medium text-gray-400 uppercase tracking-wider whitespace-nowrap">品名</div>
          <div class="text-[10px] font-medium text-gray-400 uppercase tracking-wider whitespace-nowrap">
            {{ type === 'supply' ? '供应量' : '采购量' }}
          </div>
          <div class="text-[10px] font-medium text-gray-400 uppercase tracking-wider whitespace-nowrap">
            {{ data.priceLabel || (type === 'supply' ? '出厂价' : '期望价') }}
          </div>
          <div class="text-[10px] font-medium text-gray-400 uppercase tracking-wider whitespace-nowrap">
            {{ data.addressLabel || (type === 'supply' ? '发货地' : '收货地') }}
          </div>
          <div class="text-[10px] font-medium text-gray-400 uppercase tracking-wider whitespace-nowrap">包装/付款</div>
          <div class="text-[10px] font-medium text-gray-400 uppercase tracking-wider whitespace-nowrap">质量指标</div>
          <!-- 状态列标题 -->
          <div v-if="$slots.status" class="text-[10px] font-medium text-gray-400 uppercase tracking-wider whitespace-nowrap">状态</div>
          <!-- 操作列标题 -->
          <div v-if="$slots.actions" class="text-[10px] font-medium text-gray-400 uppercase tracking-wider whitespace-nowrap">操作</div>
        </template>

        <!-- 数据行 -->
        <!-- 品名（无图标时显示） -->
        <div v-if="!showIcon" class="text-sm font-bold whitespace-nowrap" :class="type === 'supply' ? 'text-brand-700' : 'text-autumn-700'">
          {{ data.categoryName }}
        </div>
        <!-- 数量 -->
        <div class="text-sm whitespace-nowrap">
          <span class="font-semibold text-gray-800">{{ data.quantity ?? '—' }}</span>
          <span class="text-gray-500 text-xs ml-0.5">{{ data.quantityUnit || '吨' }}</span>
        </div>

        <!-- 价格 -->
        <div class="text-sm whitespace-nowrap">
          <span
            class="font-bold"
            :class="type === 'supply' ? 'text-brand-600' : 'text-autumn-600'"
          >{{ formattedPrice }}</span>
          <span v-if="data.price && typeof data.price === 'number'" class="text-gray-400 text-xs">/{{ data.priceUnit || '吨' }}</span>
        </div>

        <!-- 地址 -->
        <div class="text-sm text-gray-600 truncate" :title="data.address">
          {{ data.address || '—' }}
        </div>

        <!-- 包装/付款 -->
        <div class="text-sm text-gray-600 whitespace-nowrap">
          {{ tradeTerms }}
        </div>

        <!-- 质量指标 -->
        <div class="flex flex-wrap gap-1 min-w-0">
          <template v-if="paramTags.length > 0">
            <span
              v-for="(tag, idx) in paramTags"
              :key="idx"
              class="inline-block px-1.5 py-0.5 bg-gray-100 rounded text-[10px] text-gray-600 whitespace-nowrap"
            >{{ tag }}</span>
          </template>
          <span v-else class="text-xs text-gray-400">—</span>
        </div>

        <!-- 状态插槽 -->
        <div v-if="$slots.status" class="flex items-center">
          <slot name="status"></slot>
        </div>

        <!-- 操作插槽 -->
        <div v-if="$slots.actions" class="flex items-center gap-1">
          <slot name="actions"></slot>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 统一的网格布局 */
.product-info-grid {
  display: grid;
  gap: 0.375rem 0.625rem;
  align-items: center;
}

/* 有图标时：数量 | 价格 | 地址 | 包装 | 质量 [+ 状态] [+ 操作] */
.with-icon-5 {
  grid-template-columns: auto auto minmax(60px, 120px) auto 1fr;
}
.with-icon-6 {
  grid-template-columns: auto auto minmax(60px, 120px) auto 1fr auto;
}
.with-icon-7 {
  grid-template-columns: auto auto minmax(60px, 120px) auto 1fr auto auto;
}

/* 无图标时：品名 | 数量 | 价格 | 地址 | 包装 | 质量 [+ 状态] [+ 操作] */
.no-icon-6 {
  grid-template-columns: auto auto auto minmax(60px, 120px) auto 1fr;
}
.no-icon-7 {
  grid-template-columns: auto auto auto minmax(60px, 120px) auto 1fr auto;
}
.no-icon-8 {
  grid-template-columns: auto auto auto minmax(60px, 120px) auto 1fr auto auto;
}
</style>
