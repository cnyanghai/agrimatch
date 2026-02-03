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
  expireTime?: string // 过期时间
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

// 计算剩余发布时长
const remainingTime = computed(() => {
  const expireTime = props.data.expireTime
  if (!expireTime) return '长期有效'

  const now = new Date().getTime()
  const expire = new Date(expireTime).getTime()
  const diff = expire - now

  if (diff <= 0) return '已过期'

  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))

  if (days > 0) {
    return `${days}天${hours}小时`
  } else if (hours > 0) {
    return `${hours}小时${minutes}分`
  } else {
    return `${minutes}分钟`
  }
})

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
      <span class="text-xs font-bold text-neutral-900 text-center leading-tight line-clamp-2">{{ data.categoryName }}</span>
    </div>

    <!-- 右侧：数据格 + 状态 + 操作 -->
    <div class="flex-1 min-w-0">
      <div
        class="product-info-grid"
        :class="[
          showIcon
            ? ($slots.status && $slots.actions ? 'with-icon-8' : $slots.status || $slots.actions ? 'with-icon-7' : 'with-icon-6')
            : ($slots.status && $slots.actions ? 'no-icon-9' : $slots.status || $slots.actions ? 'no-icon-8' : 'no-icon-7')
        ]"
      >
        <!-- 标题行 -->
        <template v-if="showHeader">
          <!-- 品名列标题（无图标时显示） -->
          <div v-if="!showIcon" class="text-[10px] font-medium text-neutral-400 uppercase tracking-wider whitespace-nowrap">品名</div>
          <div class="text-[10px] font-medium text-neutral-400 uppercase tracking-wider whitespace-nowrap">
            {{ type === 'supply' ? '供应量' : '采购量' }}
          </div>
          <div class="text-[10px] font-medium text-neutral-400 uppercase tracking-wider whitespace-nowrap">
            {{ data.priceLabel || (type === 'supply' ? '出厂价' : '期望价') }}
          </div>
          <div class="text-[10px] font-medium text-neutral-400 uppercase tracking-wider whitespace-nowrap">
            {{ data.addressLabel || (type === 'supply' ? '发货地' : '收货地') }}
          </div>
          <div class="text-[10px] font-medium text-neutral-400 uppercase tracking-wider whitespace-nowrap">包装/付款</div>
          <div class="text-[10px] font-medium text-neutral-400 uppercase tracking-wider whitespace-nowrap">质量指标</div>
          <div class="text-[10px] font-medium text-neutral-400 uppercase tracking-wider whitespace-nowrap">剩余时长</div>
          <!-- 状态列标题 -->
          <div v-if="$slots.status" class="text-[10px] font-medium text-neutral-400 uppercase tracking-wider whitespace-nowrap">状态</div>
          <!-- 操作列标题 -->
          <div v-if="$slots.actions" class="text-[10px] font-medium text-neutral-400 uppercase tracking-wider whitespace-nowrap">操作</div>
        </template>

        <!-- 数据行 -->
        <!-- 品名（无图标时显示） -->
        <div v-if="!showIcon" class="text-[13px] font-semibold whitespace-nowrap" :class="type === 'supply' ? 'text-brand-700' : 'text-autumn-700'">
          {{ data.categoryName }}
        </div>
        <!-- 数量 -->
        <div class="text-[13px] font-medium text-neutral-700 whitespace-nowrap tabular-nums">
          {{ data.quantity ?? '—' }}<span class="text-neutral-400 ml-0.5">{{ data.quantityUnit || '吨' }}</span>
        </div>

        <!-- 价格 -->
        <div class="text-[13px] whitespace-nowrap tabular-nums">
          <span
            class="font-semibold"
            :class="type === 'supply' ? 'text-brand-600' : 'text-autumn-600'"
          >{{ formattedPrice }}</span><span v-if="data.price && typeof data.price === 'number'" class="text-neutral-400">/{{ data.priceUnit || '吨' }}</span>
        </div>

        <!-- 地址 -->
        <div class="text-[13px] font-medium text-neutral-700 truncate" :title="data.address">
          {{ data.address || '—' }}
        </div>

        <!-- 包装/付款 -->
        <div class="text-[13px] font-medium text-neutral-700 whitespace-nowrap">
          {{ tradeTerms }}
        </div>

        <!-- 质量指标 -->
        <div class="flex flex-wrap gap-1 min-w-0">
          <template v-if="paramTags.length > 0">
            <span
              v-for="(tag, idx) in paramTags"
              :key="idx"
              class="inline-block px-1.5 py-0.5 bg-neutral-50 border border-neutral-200 rounded text-[11px] font-medium text-neutral-600 whitespace-nowrap"
            >{{ tag }}</span>
          </template>
          <span v-else class="text-[13px] font-medium text-neutral-400">—</span>
        </div>

        <!-- 剩余时长 -->
        <div class="text-[13px] font-medium text-neutral-700 whitespace-nowrap tabular-nums">
          {{ remainingTime }}
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
      <!-- 扩展内容插槽（如基差报价详情） -->
      <slot name="extra"></slot>
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

/* 有图标时：数量 | 价格 | 地址 | 包装 | 质量 | 剩余时长 [+ 状态] [+ 操作] */
.with-icon-6 {
  grid-template-columns: auto auto minmax(60px, 120px) auto 1fr auto;
}
.with-icon-7 {
  grid-template-columns: auto auto minmax(60px, 120px) auto 1fr auto auto;
}
.with-icon-8 {
  grid-template-columns: auto auto minmax(60px, 120px) auto 1fr auto auto auto;
}

/* 无图标时：品名 | 数量 | 价格 | 地址 | 包装 | 质量 | 剩余时长 [+ 状态] [+ 操作] */
.no-icon-7 {
  grid-template-columns: auto auto auto minmax(60px, 120px) auto 1fr auto;
}
.no-icon-8 {
  grid-template-columns: auto auto auto minmax(60px, 120px) auto 1fr auto auto;
}
.no-icon-9 {
  grid-template-columns: auto auto auto minmax(60px, 120px) auto 1fr auto auto auto;
}
</style>
