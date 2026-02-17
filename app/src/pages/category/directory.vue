<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getProductTree, type ProductNode } from '../../api/product'

const categoryTree = ref<ProductNode[]>([])
const loading = ref(true)
const searchKeyword = ref('')
const expandedIds = ref<Set<number>>(new Set())

/** Filtered tree based on keyword */
const filteredTree = computed(() => {
  if (!searchKeyword.value.trim()) return categoryTree.value
  const kw = searchKeyword.value.trim().toLowerCase()
  return filterTree(categoryTree.value, kw)
})

function filterTree(nodes: ProductNode[], kw: string): ProductNode[] {
  const result: ProductNode[] = []
  for (const node of nodes) {
    if (node.name.toLowerCase().includes(kw)) {
      result.push(node)
    } else if (node.children?.length) {
      const filtered = filterTree(node.children, kw)
      if (filtered.length > 0) {
        result.push({ ...node, children: filtered })
      }
    }
  }
  return result
}

function toggleExpand(id: number) {
  if (expandedIds.value.has(id)) {
    expandedIds.value.delete(id)
  } else {
    expandedIds.value.add(id)
  }
  // Force reactivity
  expandedIds.value = new Set(expandedIds.value)
}

function isExpanded(id: number): boolean {
  return expandedIds.value.has(id)
}

function goSupplyHall(name: string) {
  uni.navigateTo({
    url: `/pages/search/index?tab=supply&keyword=${encodeURIComponent(name)}`
  })
}

function goRequirementHall(name: string) {
  uni.navigateTo({
    url: `/pages/search/index?tab=requirement&keyword=${encodeURIComponent(name)}`
  })
}

/** Category icon mapping */
function getCategoryIcon(name: string): string {
  if (name.includes('谷物') || name.includes('玉米') || name.includes('小麦')) return 'package'
  if (name.includes('油') || name.includes('豆')) return 'package'
  if (name.includes('动物') || name.includes('鱼粉')) return 'package'
  if (name.includes('矿物') || name.includes('钙')) return 'package'
  if (name.includes('添加') || name.includes('维生素')) return 'star'
  if (name.includes('微生物') || name.includes('酶')) return 'eye'
  return 'folder'
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getProductTree()
    categoryTree.value = res || []
    // Auto-expand top level
    categoryTree.value.forEach(node => expandedIds.value.add(node.id))
  } catch {
    // handled
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <view class="category-page">
    <!-- Search bar -->
    <view class="search-section">
      <view class="search-bar">
        <WgIcon name="search" :size="16" color="#A8A29E" />
        <input
          v-model="searchKeyword"
          class="search-bar__input"
          placeholder="搜索品类..."
          placeholder-class="search-bar__placeholder"
        />
        <view v-if="searchKeyword" class="search-bar__clear" @tap="searchKeyword = ''">
          <WgIcon name="clear" :size="16" color="#A8A29E" />
        </view>
      </view>
    </view>

    <!-- Category count -->
    <view class="stats-bar">
      <text class="stats-bar__text">
        共 {{ categoryTree.length }} 个主分类
      </text>
    </view>

    <!-- Loading -->
    <WgSkeleton v-if="loading" type="list" :rows="6" />

    <!-- Category tree -->
    <view v-else-if="filteredTree.length > 0" class="tree-list">
      <view
        v-for="topNode in filteredTree"
        :key="topNode.id"
        class="tree-section"
      >
        <!-- Top-level category -->
        <view class="tree-header" @tap="toggleExpand(topNode.id)">
          <view class="tree-header__left">
            <view class="tree-header__icon">
              <WgIcon :name="getCategoryIcon(topNode.name)" :size="20" color="#2D6A4F" />
            </view>
            <text class="tree-header__name">{{ topNode.name }}</text>
            <text v-if="topNode.children?.length" class="tree-header__count">
              {{ topNode.children.length }}
            </text>
          </view>
          <WgIcon
            :name="isExpanded(topNode.id) ? 'chevron-up' : 'chevron-down'"
            :size="14"
            color="#A8A29E"
          />
        </view>

        <!-- Children -->
        <view v-if="isExpanded(topNode.id) && topNode.children?.length" class="tree-children">
          <view
            v-for="child in topNode.children"
            :key="child.id"
            class="tree-child"
          >
            <view class="tree-child__main">
              <text class="tree-child__name">{{ child.name }}</text>
              <!-- Third level -->
              <view v-if="child.children?.length" class="tree-child__sub">
                <text
                  v-for="sub in child.children"
                  :key="sub.id"
                  class="tree-child__sub-tag"
                  @tap="goSupplyHall(sub.name)"
                >{{ sub.name }}</text>
              </view>
            </view>
            <view class="tree-child__actions">
              <view class="tree-child__btn tree-child__btn--supply" @tap="goSupplyHall(child.name)">
                <text class="tree-child__btn-text">找供应</text>
              </view>
              <view class="tree-child__btn tree-child__btn--need" @tap="goRequirementHall(child.name)">
                <text class="tree-child__btn-text tree-child__btn-text--need">找采购</text>
              </view>
            </view>
          </view>
        </view>

        <!-- No children, clickable directly -->
        <view v-else-if="isExpanded(topNode.id) && !topNode.children?.length" class="tree-children">
          <view class="tree-child">
            <view class="tree-child__main">
              <text class="tree-child__name tree-child__name--leaf">{{ topNode.name }}</text>
            </view>
            <view class="tree-child__actions">
              <view class="tree-child__btn tree-child__btn--supply" @tap="goSupplyHall(topNode.name)">
                <text class="tree-child__btn-text">找供应</text>
              </view>
              <view class="tree-child__btn tree-child__btn--need" @tap="goRequirementHall(topNode.name)">
                <text class="tree-child__btn-text tree-child__btn-text--need">找采购</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- Empty -->
    <WgEmpty
      v-else
      text="未找到相关品类"
      description="试试调整搜索关键词"
    />
  </view>
</template>

<style lang="scss" scoped>
.category-page {
  min-height: 100vh;
  background: $bg-page;
}

/* Search */
.search-section {
  background: #ffffff;
  padding: $spacing-sm $spacing-md;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  background: $warm-100;
  border-radius: $radius-pill;
  padding: $spacing-sm $spacing-lg;

  &__input {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;
    background: transparent;
  }

  &__placeholder {
    color: $text-placeholder;
    font-size: $font-md;
  }

  &__clear {
    flex-shrink: 0;
    padding: 4rpx;
  }
}

/* Stats */
.stats-bar {
  padding: $spacing-sm $spacing-md;

  &__text {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

/* Tree */
.tree-list {
  padding: 0 $spacing-md $spacing-md;
}

.tree-section {
  background: #ffffff;
  border-radius: $radius-xl;
  margin-bottom: $spacing-sm;
  overflow: hidden;
  box-shadow: $shadow-warm-card;
}

.tree-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-lg;

  &__left {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    flex: 1;
    min-width: 0;
  }

  &__icon {
    width: 72rpx;
    height: 72rpx;
    border-radius: $radius-lg;
    background: $brand-50;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__name {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;
    flex: 1;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__count {
    font-size: $font-xs;
    color: $text-placeholder;
    background: $warm-100;
    padding: 2rpx 14rpx;
    border-radius: $radius-pill;
    flex-shrink: 0;
  }
}

.tree-children {
  border-top: 1rpx solid $border-light;
}

.tree-child {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md $spacing-lg;
  border-bottom: 1rpx solid $border-light;

  &:last-child {
    border-bottom: none;
  }

  &__main {
    flex: 1;
    min-width: 0;
    margin-right: $spacing-sm;
  }

  &__name {
    font-size: $font-md;
    color: $text-primary;
    font-weight: 500;

    &--leaf {
      color: $text-secondary;
    }
  }

  &__sub {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
    margin-top: $spacing-xs;
  }

  &__sub-tag {
    font-size: $font-xs;
    color: $brand-600;
    background: $brand-50;
    padding: 4rpx 14rpx;
    border-radius: $radius-pill;
  }

  &__actions {
    display: flex;
    gap: $spacing-xs;
    flex-shrink: 0;
  }

  &__btn {
    padding: $spacing-xs $spacing-md;
    border-radius: $radius-pill;

    &--supply {
      background: $brand-50;
    }

    &--need {
      background: $autumn-50;
    }

    &:active {
      opacity: 0.7;
    }
  }

  &__btn-text {
    font-size: $font-xs;
    color: $brand-600;
    font-weight: 600;

    &--need {
      color: $autumn-500;
    }
  }
}
</style>
