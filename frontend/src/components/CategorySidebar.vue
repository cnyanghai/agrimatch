<script setup lang="ts">
/**
 * 品类侧边栏组件
 * 用于供应大厅和需求大厅的左侧分类导航
 */
import { ref, computed, watch } from 'vue'
import { ChevronDown, ChevronRight, Wheat, Sprout, Factory, Truck, LayoutGrid } from 'lucide-vue-next'
import type { ProductSchemaVO, CategoryNode } from '../api/productSchema'

const props = defineProps<{
  /** 业态树数据 */
  schemaTree: ProductSchemaVO[]
  /** 当前选中的业态代码 */
  selectedSchemaCode: string | null
  /** 当前选中的品类名称 */
  selectedCategory: string | null
  /** 主题色：brand(供应) 或 autumn(需求) */
  theme?: 'brand' | 'autumn'
}>()

const emit = defineEmits<{
  (e: 'update:selectedSchemaCode', value: string | null): void
  (e: 'update:selectedCategory', value: string | null): void
  (e: 'schema-change', schemaCode: string | null): void
  (e: 'category-change', categoryName: string | null): void
}>()

const themeColor = computed(() => props.theme || 'brand')

// 业态图标映射
const schemaIcons: Record<string, any> = {
  feed: Wheat,       // 原料饲料
  breed: Sprout,     // 生物种苗
  process: Factory,  // 农牧加工
  equipment: Truck,  // 装备物流
  // 向后兼容旧代码
  poultry: Sprout,
  meat: Factory,
  other: Truck
}

// 展开的分类节点
const expandedNodes = ref<Set<number>>(new Set())

// 当前选中的业态
const currentSchema = computed(() => {
  if (!props.selectedSchemaCode) return props.schemaTree[0] || null
  return props.schemaTree.find(s => s.schemaCode === props.selectedSchemaCode) || null
})

// 选择业态
function selectSchema(schemaCode: string | null) {
  emit('update:selectedSchemaCode', schemaCode)
  emit('update:selectedCategory', null)
  emit('schema-change', schemaCode)
  emit('category-change', null)
}

// 选择品类
function selectCategory(categoryName: string | null) {
  emit('update:selectedCategory', categoryName)
  emit('category-change', categoryName)
}

// 切换节点展开状态
function toggleNode(nodeId: number) {
  if (expandedNodes.value.has(nodeId)) {
    expandedNodes.value.delete(nodeId)
  } else {
    expandedNodes.value.add(nodeId)
  }
}

// 判断节点是否展开
function isExpanded(nodeId: number): boolean {
  return expandedNodes.value.has(nodeId)
}

// 判断品类是否选中
function isCategorySelected(name: string): boolean {
  return props.selectedCategory === name
}

// 统计品类下的数量（可扩展为实际数据）
function getCategoryCount(_node: CategoryNode): number | null {
  // TODO: 可以从后端获取每个品类的供应/需求数量
  return null
}

// 切换业态时重置展开状态（默认折叠）
watch(() => currentSchema.value, () => {
  expandedNodes.value.clear()
})
</script>

<template>
  <div class="h-full flex flex-col bg-white">
    <!-- 业态选择区 -->
    <div class="p-4 border-b border-gray-100">
      <div class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-3">产品业态</div>
      <div class="grid grid-cols-2 gap-2">
        <button
          v-for="schema in schemaTree"
          :key="schema.schemaCode"
          @click="selectSchema(schema.schemaCode)"
          :class="[
            'flex flex-col items-center gap-1.5 p-3 rounded-xl border-2 transition-all',
            selectedSchemaCode === schema.schemaCode || (!selectedSchemaCode && schema === schemaTree[0])
              ? themeColor === 'brand'
                ? 'border-brand-500 bg-brand-50 text-brand-700'
                : 'border-autumn-500 bg-autumn-50 text-autumn-700'
              : 'border-gray-100 hover:border-gray-200 text-gray-600 hover:bg-gray-50'
          ]"
        >
          <component
            :is="schemaIcons[schema.schemaCode] || LayoutGrid"
            class="w-5 h-5"
          />
          <span class="text-xs font-medium">{{ schema.schemaName }}</span>
        </button>
      </div>
    </div>

    <!-- 品类树区域 -->
    <div class="flex-1 overflow-y-auto">
      <div class="p-4">
        <div class="flex items-center justify-between mb-3">
          <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">品类分类</span>
          <button
            v-if="selectedCategory"
            @click="selectCategory(null)"
            :class="[
              'text-xs px-2 py-0.5 rounded-full',
              themeColor === 'brand' ? 'text-brand-600 hover:bg-brand-50' : 'text-autumn-600 hover:bg-autumn-50'
            ]"
          >
            清除筛选
          </button>
        </div>

        <!-- 全部选项 -->
        <button
          @click="selectCategory(null)"
          :class="[
            'w-full text-left px-3 py-2 rounded-lg text-sm font-medium transition-all mb-1',
            !selectedCategory
              ? themeColor === 'brand'
                ? 'bg-brand-50 text-brand-700'
                : 'bg-autumn-50 text-autumn-700'
              : 'text-gray-600 hover:bg-gray-50'
          ]"
        >
          全部品类
        </button>

        <!-- 品类树 -->
        <div v-if="currentSchema?.categories" class="space-y-0.5">
          <template v-for="node in currentSchema.categories" :key="node.id">
            <!-- 一级分类 -->
            <div>
              <button
                @click="node.children?.length ? toggleNode(node.id) : selectCategory(node.name)"
                :class="[
                  'w-full flex items-center gap-2 px-3 py-2 rounded-lg text-sm transition-all',
                  isCategorySelected(node.name)
                    ? themeColor === 'brand'
                      ? 'bg-brand-50 text-brand-700 font-medium'
                      : 'bg-autumn-50 text-autumn-700 font-medium'
                    : 'text-gray-700 hover:bg-gray-50'
                ]"
              >
                <!-- 展开/折叠图标 -->
                <template v-if="node.children?.length">
                  <ChevronDown v-if="isExpanded(node.id)" class="w-4 h-4 text-gray-400 shrink-0" />
                  <ChevronRight v-else class="w-4 h-4 text-gray-400 shrink-0" />
                </template>
                <span v-else class="w-4 shrink-0"></span>

                <span class="flex-1 text-left truncate">{{ node.name }}</span>

                <span v-if="getCategoryCount(node)" class="text-xs text-gray-400">
                  {{ getCategoryCount(node) }}
                </span>
              </button>

              <!-- 二级分类 -->
              <div
                v-if="node.children?.length && isExpanded(node.id)"
                class="ml-4 pl-2 border-l border-gray-100"
              >
                <template v-for="child in node.children" :key="child.id">
                  <button
                    @click="selectCategory(child.name)"
                    :class="[
                      'w-full flex items-center gap-2 px-3 py-1.5 rounded-lg text-sm transition-all',
                      isCategorySelected(child.name)
                        ? themeColor === 'brand'
                          ? 'bg-brand-50 text-brand-700 font-medium'
                          : 'bg-autumn-50 text-autumn-700 font-medium'
                        : 'text-gray-600 hover:bg-gray-50'
                    ]"
                  >
                    <span class="flex-1 text-left truncate">{{ child.name }}</span>
                    <span v-if="getCategoryCount(child)" class="text-xs text-gray-400">
                      {{ getCategoryCount(child) }}
                    </span>
                  </button>

                  <!-- 三级分类 -->
                  <div
                    v-if="child.children?.length"
                    class="ml-4 pl-2 border-l border-gray-100"
                  >
                    <button
                      v-for="grandChild in child.children"
                      :key="grandChild.id"
                      @click="selectCategory(grandChild.name)"
                      :class="[
                        'w-full flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs transition-all',
                        isCategorySelected(grandChild.name)
                          ? themeColor === 'brand'
                            ? 'bg-brand-50 text-brand-700 font-medium'
                            : 'bg-autumn-50 text-autumn-700 font-medium'
                          : 'text-gray-500 hover:bg-gray-50'
                      ]"
                    >
                      <span class="flex-1 text-left truncate">{{ grandChild.name }}</span>
                    </button>
                  </div>
                </template>
              </div>
            </div>
          </template>
        </div>

        <!-- 空状态 -->
        <div v-else class="text-center py-8 text-gray-400 text-sm">
          暂无品类数据
        </div>
      </div>
    </div>

    <!-- 底部筛选区（预留） -->
    <div class="p-4 border-t border-gray-100 hidden lg:block">
      <div class="text-xs text-gray-400 text-center">
        更多筛选条件开发中...
      </div>
    </div>
  </div>
</template>
