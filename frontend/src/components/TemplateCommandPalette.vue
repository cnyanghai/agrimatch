<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Search, Trash2, Command, ArrowUp, ArrowDown, CornerDownLeft, Package, FileText } from 'lucide-vue-next'

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
  emptyText: '暂无模板，可在发布表单中保存'
})

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'select', template: TemplateItem): void
  (e: 'delete', id: number): void
}>()

const searchQuery = ref('')
const selectedIndex = ref(0)
const inputRef = ref<HTMLInputElement | null>(null)
const listRef = ref<HTMLDivElement | null>(null)

// 过滤后的模板列表
const filteredTemplates = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return props.templates
  return props.templates.filter(t =>
    t.name.toLowerCase().includes(q) ||
    t.category.toLowerCase().includes(q) ||
    t.tags?.some(tag => tag.toLowerCase().includes(q))
  )
})

// 关闭面板
function close() {
  emit('update:modelValue', false)
  searchQuery.value = ''
  selectedIndex.value = 0
}

// 选择模板
function selectTemplate(template: TemplateItem) {
  emit('select', template)
  close()
}

// 删除模板
function deleteTemplate(id: number, event: Event) {
  event.stopPropagation()
  emit('delete', id)
}

// 键盘事件处理
function handleKeydown(e: KeyboardEvent) {
  const len = filteredTemplates.value.length
  if (len === 0) return

  switch (e.key) {
    case 'ArrowDown':
      e.preventDefault()
      selectedIndex.value = (selectedIndex.value + 1) % len
      scrollToSelected()
      break
    case 'ArrowUp':
      e.preventDefault()
      selectedIndex.value = (selectedIndex.value - 1 + len) % len
      scrollToSelected()
      break
    case 'Enter':
      e.preventDefault()
      if (filteredTemplates.value[selectedIndex.value]) {
        selectTemplate(filteredTemplates.value[selectedIndex.value])
      }
      break
    case 'Escape':
      e.preventDefault()
      close()
      break
    // 移除 Backspace/Delete 快捷键删除功能，防止误触
    // 用户可通过点击删除按钮删除模板
    default:
      // 数字键 1-9 快速选择
      if (/^[1-9]$/.test(e.key)) {
        const idx = parseInt(e.key) - 1
        if (filteredTemplates.value[idx]) {
          e.preventDefault()
          selectTemplate(filteredTemplates.value[idx])
        }
      }
  }
}

// 滚动到选中项
function scrollToSelected() {
  nextTick(() => {
    const list = listRef.value
    if (!list) return
    const item = list.querySelector(`[data-index="${selectedIndex.value}"]`) as HTMLElement
    if (item) {
      item.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
    }
  })
}

// 格式化价格显示
function formatPrice(price?: number | string, unit?: string): string {
  if (price === undefined || price === null || price === '') return '面议'
  if (typeof price === 'string') return price
  return `¥${price}/${unit || '吨'}`
}

// 监听打开状态
watch(() => props.modelValue, (open) => {
  if (open) {
    nextTick(() => {
      inputRef.value?.focus()
    })
  } else {
    searchQuery.value = ''
    selectedIndex.value = 0
  }
})

// 监听搜索变化，重置选中索引
watch(searchQuery, () => {
  selectedIndex.value = 0
})

// 全局键盘监听
function globalKeyHandler(e: KeyboardEvent) {
  if (!props.modelValue) return
  // 如果焦点在 input 上，由 input 的 keydown 处理
  if (document.activeElement === inputRef.value) return
  handleKeydown(e)
}

onMounted(() => {
  document.addEventListener('keydown', globalKeyHandler)
})

onBeforeUnmount(() => {
  document.removeEventListener('keydown', globalKeyHandler)
})
</script>

<template>
  <Teleport to="body">
    <Transition name="palette">
      <div
        v-if="modelValue"
        class="fixed inset-0 z-[2000] flex items-start justify-center pt-[15vh]"
        @click.self="close"
      >
        <!-- 背景遮罩 -->
        <div class="absolute inset-0 bg-slate-900/60" @click="close" />

        <!-- 命令面板 -->
        <div class="relative w-full max-w-xl mx-4 animate-palette-in">
          <div class="bg-white rounded-2xl shadow-2xl border border-gray-200 overflow-hidden">
            <!-- 搜索头部 -->
            <div class="flex items-center gap-3 px-4 py-3 border-b border-gray-100">
              <Search class="w-5 h-5 text-gray-400 shrink-0" />
              <input
                ref="inputRef"
                v-model="searchQuery"
                type="text"
                class="flex-1 text-base outline-none placeholder:text-gray-400"
                :placeholder="`搜索${title}...`"
                @keydown="handleKeydown"
              />
              <div class="flex items-center gap-1 text-[10px] text-gray-400 font-mono bg-gray-100 px-1.5 py-0.5 rounded">
                <Command class="w-3 h-3" />
                <span>K</span>
              </div>
            </div>

            <!-- 模板列表 -->
            <div ref="listRef" class="max-h-[50vh] overflow-y-auto p-3 space-y-2">
              <!-- 空状态 -->
              <div v-if="filteredTemplates.length === 0" class="py-12 text-center">
                <div class="w-14 h-14 bg-gray-100 rounded-2xl flex items-center justify-center mx-auto mb-3">
                  <FileText class="w-7 h-7 text-gray-300" />
                </div>
                <div class="text-gray-500 font-medium">{{ searchQuery ? '未找到匹配的模板' : '暂无模板' }}</div>
                <div class="text-gray-400 text-sm mt-1">{{ searchQuery ? '请尝试其他关键词' : emptyText }}</div>
              </div>

              <!-- 模板卡片 -->
              <div
                v-for="(tpl, index) in filteredTemplates"
                :key="tpl.id"
                :data-index="index"
                class="group relative rounded-xl border-2 p-3 cursor-pointer transition-all duration-150"
                :class="[
                  index === selectedIndex
                    ? 'border-brand-500 bg-brand-50/50 shadow-sm shadow-brand-100'
                    : 'border-gray-100 bg-white hover:border-gray-200 hover:bg-gray-50/50'
                ]"
                @click="selectTemplate(tpl)"
                @mouseenter="selectedIndex = index"
              >
                <div class="flex items-start gap-3">
                  <!-- 左侧：图标 + 快捷键 -->
                  <div class="relative shrink-0">
                    <div
                      class="w-11 h-11 rounded-xl flex items-center justify-center transition-colors"
                      :class="index === selectedIndex ? 'bg-brand-500' : 'bg-gray-100'"
                    >
                      <Package class="w-5 h-5" :class="index === selectedIndex ? 'text-white' : 'text-gray-400'" />
                    </div>
                    <!-- 快捷键角标 -->
                    <div
                      v-if="index < 9"
                      class="absolute -top-1 -right-1 w-5 h-5 rounded-md flex items-center justify-center text-[10px] font-bold shadow-sm"
                      :class="index === selectedIndex ? 'bg-white text-brand-600' : 'bg-gray-700 text-white'"
                    >
                      {{ index + 1 }}
                    </div>
                  </div>

                  <!-- 中间：主要信息 -->
                  <div class="flex-1 min-w-0 py-0.5">
                    <!-- 模板名称 -->
                    <div class="font-bold text-gray-900 truncate mb-1">{{ tpl.name }}</div>
                    <!-- 详情行 -->
                    <div class="flex items-center gap-2 text-sm">
                      <span class="px-2 py-0.5 bg-gray-100 rounded-md text-xs font-semibold text-gray-600">
                        {{ tpl.category }}
                      </span>
                      <span class="text-gray-300">·</span>
                      <span class="text-gray-500">
                        <span class="font-semibold text-gray-700">{{ tpl.quantity || 0 }}</span>
                        <span class="text-xs ml-0.5">{{ tpl.quantityUnit || '吨' }}</span>
                      </span>
                      <span class="text-gray-300">·</span>
                      <span class="font-semibold" :class="index === selectedIndex ? 'text-brand-600' : 'text-gray-600'">
                        {{ formatPrice(tpl.price, tpl.priceUnit) }}
                      </span>
                    </div>
                  </div>

                  <!-- 右侧：删除按钮 -->
                  <button
                    class="shrink-0 w-8 h-8 rounded-lg flex items-center justify-center opacity-0 group-hover:opacity-100 hover:bg-red-100 text-gray-400 hover:text-red-500 transition-all"
                    title="删除模板"
                    @click="deleteTemplate(tpl.id, $event)"
                  >
                    <Trash2 class="w-4 h-4" />
                  </button>
                </div>

                <!-- 选中指示器 -->
                <div
                  v-if="index === selectedIndex"
                  class="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-8 bg-brand-500 rounded-r-full"
                ></div>
              </div>
            </div>

            <!-- 底部快捷键提示 -->
            <div class="flex items-center justify-between px-4 py-2.5 bg-gray-50 border-t border-gray-100 text-[11px] text-gray-400">
              <div class="flex items-center gap-4">
                <div class="flex items-center gap-1.5">
                  <div class="flex gap-0.5">
                    <kbd class="px-1.5 py-0.5 bg-white border border-gray-200 rounded text-[10px] font-mono shadow-sm"><ArrowUp class="w-3 h-3" /></kbd>
                    <kbd class="px-1.5 py-0.5 bg-white border border-gray-200 rounded text-[10px] font-mono shadow-sm"><ArrowDown class="w-3 h-3" /></kbd>
                  </div>
                  <span>导航</span>
                </div>
                <div class="flex items-center gap-1.5">
                  <kbd class="px-1.5 py-0.5 bg-white border border-gray-200 rounded text-[10px] font-mono shadow-sm"><CornerDownLeft class="w-3 h-3" /></kbd>
                  <span>选择</span>
                </div>
                <div class="flex items-center gap-1.5">
                  <kbd class="px-1.5 py-0.5 bg-white border border-gray-200 rounded text-[10px] font-mono shadow-sm">1-9</kbd>
                  <span>快捷</span>
                </div>
                <div class="flex items-center gap-1.5">
                  <kbd class="px-1.5 py-0.5 bg-white border border-gray-200 rounded text-[10px] font-mono shadow-sm">ESC</kbd>
                  <span>关闭</span>
                </div>
              </div>
              <div class="text-gray-300">
                {{ filteredTemplates.length }} 个模板
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* 面板进入动画 */
@keyframes palette-in {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.animate-palette-in {
  animation: palette-in 0.2s ease-out;
}

/* 过渡效果 */
.palette-enter-active {
  transition: opacity 0.2s ease-out;
}

.palette-leave-active {
  transition: opacity 0.15s ease-in;
}

.palette-enter-from,
.palette-leave-to {
  opacity: 0;
}

.palette-enter-from .animate-palette-in,
.palette-leave-to .animate-palette-in {
  transform: translateY(-20px) scale(0.98);
}

/* 滚动条样式 */
div[ref="listRef"]::-webkit-scrollbar {
  width: 6px;
}

div[ref="listRef"]::-webkit-scrollbar-track {
  background: transparent;
}

div[ref="listRef"]::-webkit-scrollbar-thumb {
  background: rgb(209 213 219);
  border-radius: 3px;
}

div[ref="listRef"]::-webkit-scrollbar-thumb:hover {
  background: rgb(156 163 175);
}
</style>
