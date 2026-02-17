<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { WARM_400, WARM_500, WARM_300, BRAND_600, ACCENT_400 } from '../constants/colors'

export interface TemplateItem {
  id: number
  name: string
  category: string
  quantity?: number
  quantityUnit?: string
  price?: number | string
  priceUnit?: string
  tags?: string[]
}

const props = withDefaults(defineProps<{
  modelValue: boolean
  templates: TemplateItem[]
  title?: string
  emptyText?: string
}>(), {
  title: '选择模板',
  emptyText: '暂无模板，可在发布表单中保存',
})

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'select', template: TemplateItem): void
  (e: 'delete', id: number): void
}>()

const searchQuery = ref('')

const filteredTemplates = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return props.templates
  return props.templates.filter(t =>
    t.name.toLowerCase().includes(q) ||
    t.category.toLowerCase().includes(q) ||
    t.tags?.some(tag => tag.toLowerCase().includes(q))
  )
})

watch(() => props.modelValue, (open) => {
  if (!open) {
    searchQuery.value = ''
  }
})

function close() {
  emit('update:modelValue', false)
}

function selectTemplate(tpl: TemplateItem) {
  emit('select', tpl)
  close()
}

function confirmDelete(tpl: TemplateItem) {
  uni.showModal({
    title: '删除模板',
    content: `确定删除「${tpl.name}」吗？`,
    confirmColor: ACCENT_400,
    success: (res) => {
      if (res.confirm) {
        emit('delete', tpl.id)
      }
    },
  })
}

function formatPrice(price?: number | string, unit?: string): string {
  if (price === undefined || price === null || price === '') return '面议'
  if (typeof price === 'string') return price
  return `¥${price}/${unit || '吨'}`
}
</script>

<template>
  <!-- 遮罩 -->
  <view v-if="modelValue" class="tpl-mask" @tap="close">
    <view class="tpl-panel" @tap.stop>
      <!-- 标题 -->
      <view class="tpl-panel__header">
        <text class="tpl-panel__title">{{ title }}</text>
        <view class="tpl-panel__close" @tap="close">
          <WgIcon name="clear" :size="18" :color="WARM_500" />
        </view>
      </view>

      <!-- 搜索 -->
      <view class="tpl-search">
        <WgIcon name="search" :size="16" :color="WARM_400" />
        <input
          class="tpl-search__input"
          v-model="searchQuery"
          :placeholder="`搜索${title}...`"
          confirm-type="search"
        />
      </view>

      <!-- 列表 -->
      <scroll-view class="tpl-list" scroll-y>
        <!-- 空状态 -->
        <view v-if="filteredTemplates.length === 0" class="tpl-empty">
          <WgIcon name="file-text" :size="40" :color="WARM_300" />
          <text class="tpl-empty__text">{{ searchQuery ? '未找到匹配模板' : emptyText }}</text>
        </view>

        <!-- 模板卡片 -->
        <view
          v-for="tpl in filteredTemplates"
          :key="tpl.id"
          class="tpl-card"
          @tap="selectTemplate(tpl)"
        >
          <view class="tpl-card__icon">
            <WgIcon name="package" :size="20" :color="BRAND_600" />
          </view>
          <view class="tpl-card__body">
            <text class="tpl-card__name">{{ tpl.name }}</text>
            <view class="tpl-card__meta">
              <text class="tpl-card__tag">{{ tpl.category }}</text>
              <text v-if="tpl.quantity" class="tpl-card__qty">{{ tpl.quantity }}{{ tpl.quantityUnit || '吨' }}</text>
              <text class="tpl-card__price">{{ formatPrice(tpl.price, tpl.priceUnit) }}</text>
            </view>
          </view>
          <view class="tpl-card__del" @tap.stop="confirmDelete(tpl)">
            <WgIcon name="trash" :size="16" :color="WARM_400" />
          </view>
        </view>
      </scroll-view>

      <!-- 底部统计 -->
      <view class="tpl-footer">
        <text class="tpl-footer__text">{{ filteredTemplates.length }} 个模板</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.tpl-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 200;
  display: flex;
  align-items: flex-end;
}

.tpl-panel {
  width: 100%;
  background: $bg-card;
  border-radius: 32rpx 32rpx 0 0;
  max-height: 75vh;
  display: flex;
  flex-direction: column;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: $spacing-md $spacing-lg;
    border-bottom: 1rpx solid $warm-100;
  }

  &__title {
    font-size: $font-lg;
    font-weight: 800;
    color: $text-primary;
  }

  &__close {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    background: $warm-100;
    display: flex;
    align-items: center;
    justify-content: center;

    &:active {
      background: $warm-200;
    }
  }
}

.tpl-search {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  margin: $spacing-sm $spacing-lg;
  padding: $spacing-xs $spacing-md;
  background: $bg-page;
  border-radius: $radius-lg;

  &__input {
    flex: 1;
    font-size: $font-base;
    color: $text-primary;
    background: transparent;
  }
}

.tpl-list {
  flex: 1;
  overflow: hidden;
  padding: 0 $spacing-lg;
  max-height: 50vh;
}

.tpl-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;
  gap: $spacing-sm;

  &__text {
    font-size: $font-sm;
    color: $text-placeholder;
  }
}

.tpl-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  margin-bottom: $spacing-xs;
  border-radius: $radius-lg;
  border: 2rpx solid $warm-100;
  background: $bg-card;
  transition: transform 0.15s;

  &:active {
    transform: scale(0.98);
    background: $brand-50;
    border-color: $brand-200;
  }

  &__icon {
    width: 80rpx;
    height: 80rpx;
    border-radius: $radius-md;
    background: $brand-50;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__body {
    flex: 1;
    min-width: 0;
  }

  &__name {
    display: block;
    font-size: $font-base;
    font-weight: 700;
    color: $text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-bottom: 4rpx;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    flex-wrap: wrap;
  }

  &__tag {
    font-size: $font-xs;
    color: $brand-600;
    background: $brand-50;
    padding: 2rpx 12rpx;
    border-radius: $radius-sm;
    font-weight: 600;
  }

  &__qty {
    font-size: $font-xs;
    color: $text-secondary;
  }

  &__price {
    font-size: $font-xs;
    color: $accent-400;
    font-weight: 600;
  }

  &__del {
    width: 56rpx;
    height: 56rpx;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &:active {
      background: rgba(231, 111, 81, 0.1);
    }
  }
}

.tpl-footer {
  padding: $spacing-sm $spacing-lg;
  border-top: 1rpx solid $warm-100;

  &__text {
    font-size: $font-xs;
    color: $text-placeholder;
  }
}
</style>
