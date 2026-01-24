<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { Plus, X, Search, Tag as TagIcon, ChevronDown, Check, Sparkles } from 'lucide-vue-next'
import { listTags, recommendTags, createTag, type Tag, type TagValue } from '../api/tag'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  categoryId?: number
  domain?: string
  modelValue: TagValue[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: TagValue[]]
}>()

// 选中的标签
const selectedTags = ref<TagValue[]>([...props.modelValue])

// 推荐标签
const recommendedTags = ref<Tag[]>([])

// 搜索关键词
const searchKeyword = ref('')
const searchResults = ref<Tag[]>([])
const searchLoading = ref(false)

// 自定义标签
const showCustomInput = ref(false)
const customTagName = ref('')

// 判断标签是否已选中
const isTagSelected = (tagId: number) => {
  return selectedTags.value.some(t => t.tagId === tagId)
}

// 获取推荐标签
async function loadRecommendations() {
  if (!props.categoryId) {
    // 如果没有品类，拉取热门标签作为默认推荐
    const res = await listTags({ isHot: 1, domain: props.domain })
    if (res.code === 0) recommendedTags.value = res.data ?? []
    return
  }
  const res = await recommendTags(props.categoryId)
  if (res.code === 0) {
    recommendedTags.value = res.data ?? []
  }
}

// 搜索标签
async function handleSearch() {
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  searchLoading.value = true
  try {
    const res = await listTags({ keyword: searchKeyword.value, domain: props.domain })
    if (res.code === 0) {
      searchResults.value = res.data ?? []
    }
  } finally {
    searchLoading.value = false
  }
}

// 添加标签
function addTag(tag: Tag) {
  if (isTagSelected(tag.id)) return
  
  const newValue: TagValue = {
    tagId: tag.id,
    tagName: tag.tagName,
    tagKey: tag.tagKey,
    tagType: tag.tagType,
    unit: tag.unit,
    value: ''
  }
  
  selectedTags.value.push(newValue)
  emitChange()
  searchKeyword.value = ''
  searchResults.value = []
}

// 移除标签
function removeTag(tagId: number) {
  selectedTags.value = selectedTags.value.filter(t => t.tagId !== tagId)
  emitChange()
}

// 添加自定义标签
async function addCustomTag() {
  const name = customTagName.value.trim()
  if (!name) return
  
  // 先在库里找找看有没有同名的
  const resSearch = await listTags({ keyword: name })
  if (resSearch.code === 0 && resSearch.data?.length) {
    const existing = resSearch.data.find(t => t.tagName === name)
    if (existing) {
      addTag(existing)
      customTagName.value = ''
      showCustomInput.value = false
      return
    }
  }

  // 如果没有，可以考虑是否要写入库，或者直接作为临时标签
  // 这里为了简单，先作为 ID=0 的临时标签
  const tempTag: TagValue = {
    tagId: 0,
    tagName: name,
    tagKey: 'custom_' + Date.now(),
    tagType: 0,
    value: ''
  }
  
  selectedTags.value.push(tempTag)
  emitChange()
  customTagName.value = ''
  showCustomInput.value = false
}

function emitChange() {
  emit('update:modelValue', [...selectedTags.value])
}

watch(() => props.categoryId, () => {
  loadRecommendations()
})

watch(() => props.modelValue, (val) => {
  selectedTags.value = [...val]
}, { deep: true })

onMounted(loadRecommendations)

// 转换选项字符串为数组
function parseOptions(optionsStr?: string): string[] {
  if (!optionsStr) return []
  try {
    return JSON.parse(optionsStr)
  } catch {
    return []
  }
}
</script>

<template>
  <div class="space-y-4">
    <!-- 已选标签展示 -->
    <div v-if="selectedTags.length > 0" class="flex flex-wrap gap-3">
      <div
        v-for="tag in selectedTags"
        :key="tag.tagId || tag.tagKey"
        class="group relative flex flex-col p-3 bg-white border border-gray-200 rounded-xl hover:border-brand-500 transition-all min-w-[140px] shadow-sm"
      >
        <div class="flex items-center justify-between mb-2">
          <div class="flex items-center gap-1.5 overflow-hidden">
            <TagIcon class="w-3 h-3 text-brand-600 shrink-0" />
            <span class="text-xs font-bold text-gray-700 truncate">{{ tag.tagName }}</span>
          </div>
          <button @click="removeTag(tag.tagId)" class="p-1 hover:bg-red-50 text-gray-400 hover:text-red-500 rounded-lg transition-colors">
            <X class="w-3 h-3" />
          </button>
        </div>

        <!-- 值输入区 -->
        <div class="mt-1">
          <!-- 文本类型 (0) -->
          <input
            v-if="tag.tagType === 0"
            v-model="tag.value"
            type="text"
            placeholder="输入说明"
            class="w-full px-2 py-1.5 text-xs bg-gray-50 border-none rounded-lg focus:ring-1 focus:ring-brand-500 outline-none"
            @change="emitChange"
          />
          
          <!-- 数值类型 (1) -->
          <div v-else-if="tag.tagType === 1" class="flex items-center gap-1">
            <input
              v-model="tag.value"
              type="number"
              placeholder="0.00"
              class="flex-1 px-2 py-1.5 text-xs bg-gray-50 border-none rounded-lg focus:ring-1 focus:ring-brand-500 outline-none"
              @change="emitChange"
            />
            <span v-if="tag.unit" class="text-[10px] font-bold text-gray-400">{{ tag.unit }}</span>
          </div>

          <!-- 选项类型 (2) -->
          <el-select
            v-else-if="tag.tagType === 2"
            v-model="tag.value"
            placeholder="请选择"
            size="small"
            class="neo-select-sm"
            @change="emitChange"
          >
            <el-option
              v-for="opt in parseOptions(recommendedTags.find(t => t.id === tag.tagId)?.options)"
              :key="opt"
              :label="opt"
              :value="opt"
            />
          </el-select>
        </div>
      </div>
    </div>

    <!-- 交互区域：搜索与推荐 -->
    <div class="bg-gray-50/50 rounded-2xl p-4 border border-dashed border-gray-200">
      <div class="flex items-center gap-4 mb-4">
        <!-- 搜索框 -->
        <div class="relative flex-1">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索标签，如：水分、产地、日龄..."
            class="w-full pl-10 pr-4 py-2 bg-white border border-gray-200 rounded-xl text-sm focus:border-brand-500 outline-none transition-all shadow-sm"
            @input="handleSearch"
            @keyup.enter="addCustomTag"
          />
          
          <!-- 搜索下拉结果 -->
          <div v-if="searchResults.length > 0" class="absolute z-10 left-0 right-0 mt-2 bg-white border border-gray-200 rounded-xl shadow-xl overflow-hidden animate-in fade-in slide-in-from-top-2">
            <div
              v-for="item in searchResults"
              :key="item.id"
              class="px-4 py-3 hover:bg-brand-50 cursor-pointer flex items-center justify-between group"
              @click="addTag(item)"
            >
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-lg bg-gray-100 flex items-center justify-center text-gray-500 group-hover:bg-brand-100 group-hover:text-brand-600 transition-colors">
                  <TagIcon class="w-4 h-4" />
                </div>
                <div>
                  <div class="text-sm font-bold text-gray-900">{{ item.tagName }}</div>
                  <div class="text-[10px] text-gray-400">{{ item.domain }}</div>
                </div>
              </div>
              <Check v-if="isTagSelected(item.id)" class="w-4 h-4 text-brand-600" />
              <Plus v-else class="w-4 h-4 text-gray-300 group-hover:text-brand-600" />
            </div>
          </div>
        </div>

        <!-- 自定义入口 -->
        <button
          @click="showCustomInput = true"
          class="flex items-center gap-1.5 px-4 py-2 bg-white border border-gray-200 rounded-xl text-xs font-bold text-gray-600 hover:text-brand-600 hover:border-brand-200 transition-all shadow-sm"
        >
          <Plus class="w-3.5 h-3.5" />
          手动录入
        </button>
      </div>

      <!-- 推荐区域 -->
      <div v-if="recommendedTags.length > 0">
        <div class="flex items-center gap-1.5 text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-3">
          <Sparkles class="w-3 h-3 text-amber-500" />
          为你推荐
        </div>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="tag in recommendedTags"
            :key="tag.id"
            @click="addTag(tag)"
            :class="[
              'px-3 py-1.5 rounded-lg text-xs font-bold transition-all border flex items-center gap-1.5',
              isTagSelected(tag.id)
                ? 'bg-brand-500 text-white border-brand-500 shadow-brand/20 shadow-md'
                : 'bg-white text-gray-600 border-gray-200 hover:border-brand-200 hover:bg-brand-50 hover:text-brand-700 shadow-sm'
            ]"
          >
            {{ tag.tagName }}
            <Plus v-if="!isTagSelected(tag.id)" class="w-3 h-3 opacity-50" />
            <Check v-else class="w-3 h-3" />
          </button>
        </div>
      </div>
    </div>

    <!-- 自定义标签弹窗 -->
    <el-dialog v-model="showCustomInput" title="手动录入标签" width="400px" align-center class="!rounded-3xl">
      <div class="space-y-4">
        <p class="text-xs text-gray-500">如果预设标签中没有您需要的，可以直接录入。如：“产地”、“保质期”等。</p>
        <input
          v-model="customTagName"
          type="text"
          placeholder="输入标签名称"
          class="w-full px-4 py-3 border-2 border-gray-100 rounded-2xl focus:border-brand-500 outline-none transition-all"
          @keyup.enter="addCustomTag"
        />
      </div>
      <template #footer>
        <div class="flex gap-3">
          <button @click="showCustomInput = false" class="flex-1 px-4 py-2.5 bg-gray-100 text-gray-600 rounded-xl font-bold">取消</button>
          <button @click="addCustomTag" class="flex-1 px-4 py-2.5 bg-brand-600 text-white rounded-xl font-bold">添加到表单</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.neo-select-sm :deep(.el-select__wrapper) {
  border-radius: 8px;
  background-color: rgb(249 250 251);
  border: none;
  box-shadow: none;
}
</style>

