<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getSchemaTree, type ProductSchemaVO, type CategoryNode } from '../api/productSchema'
import { createCustomProduct } from '../api/product'

export interface PickedCategory {
  id: number
  name: string
  schemaCode: string
  hasParams?: boolean
  allowCustomName?: boolean
}

const props = defineProps<{
  modelValue?: PickedCategory | null
}>()
const emit = defineEmits<{
  (e: 'update:modelValue', v: PickedCategory | null): void
  (e: 'schemaChange', schemaCode: string): void
}>()

const loading = ref(false)
const schemas = ref<ProductSchemaVO[]>([])

// 当前选中的业态
const selectedSchemaCode = ref<string>('feed')

// 级联选择器值
const cascaderValue = ref<number[] | undefined>(undefined)
const cascaderRef = ref<any>(null)
const filterText = ref('')

// 当前业态下的品类树
const currentCategories = computed(() => {
  const schema = schemas.value.find(s => s.schemaCode === selectedSchemaCode.value)
  return schema?.categories || []
})

// 级联选择器选项
const cascaderOptions = computed(() =>
  currentCategories.value
    .filter(x => (x.parentId ?? 0) === 0)
    .map(p => ({
      value: p.id,
      label: p.name,
      hasParams: p.hasParams,
      allowCustomName: p.allowCustomName,
      children: (p.children ?? []).map(c => ({
        value: c.id,
        label: c.name,
        hasParams: c.hasParams,
        allowCustomName: c.allowCustomName
      }))
    }))
)

const currentLabel = computed(() => props.modelValue?.name ?? '')

// 自定义品类弹窗
const customDialogOpen = ref(false)
const customName = ref('')
const customParentId = ref<number | undefined>(undefined)
const customParentName = computed(() => {
  const pid = customParentId.value
  if (!pid) return ''
  return findNameById(currentCategories.value, pid) ?? ''
})

async function refresh() {
  loading.value = true
  try {
    const r = await getSchemaTree()
    if (r.code !== 0) throw new Error(r.message)
    schemas.value = r.data ?? []

    // 如果已有选中值，恢复业态和级联值
    if (props.modelValue?.id) {
      if (props.modelValue.schemaCode) {
        selectedSchemaCode.value = props.modelValue.schemaCode
      }
      await nextTick()
      const parent = findParentId(currentCategories.value, props.modelValue.id)
      if (parent != null && parent > 0) {
        cascaderValue.value = [parent, props.modelValue.id]
      }
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载业态分类失败')
  } finally {
    loading.value = false
  }
}

// 监听业态切换
watch(selectedSchemaCode, (newCode) => {
  // 清空当前选择
  cascaderValue.value = undefined
  emit('update:modelValue', null)
  emit('schemaChange', newCode)
})

// 监听外部 modelValue 变化，同步到内部状态
watch(
  () => props.modelValue?.id,
  (id) => {
    if (!id) {
      cascaderValue.value = undefined
      return
    }
    // 如果有 schemaCode，先切换业态
    if (props.modelValue?.schemaCode && props.modelValue.schemaCode !== selectedSchemaCode.value) {
      selectedSchemaCode.value = props.modelValue.schemaCode
    }
    nextTick(() => {
      const parent = findParentId(currentCategories.value, id)
      if (parent != null && parent > 0) {
        cascaderValue.value = [parent, id]
      }
    })
  }
)

function findParentId(list: CategoryNode[], childId: number): number | null {
  for (const n of list) {
    if (n.id === childId) return n.parentId ?? 0
    if (n.children?.length) {
      const r = findParentId(n.children, childId)
      if (r != null) return r
    }
  }
  return null
}

function findNameById(list: CategoryNode[], id: number): string | null {
  for (const n of list) {
    if (n.id === id) return n.name
    if (n.children?.length) {
      const r = findNameById(n.children, id)
      if (r) return r
    }
  }
  return null
}

function findNodeById(list: CategoryNode[], id: number): CategoryNode | null {
  for (const n of list) {
    if (n.id === id) return n
    if (n.children?.length) {
      const r = findNodeById(n.children, id)
      if (r) return r
    }
  }
  return null
}

function onCascaderChange(v: number[] | undefined) {
  cascaderValue.value = v
  if (!v || v.length < 2) {
    emit('update:modelValue', null)
    return
  }
  const id = v[1]
  if (typeof id !== 'number') {
    emit('update:modelValue', null)
    return
  }
  const node = findNodeById(currentCategories.value, id)
  const name = node?.name ?? String(id)
  emit('update:modelValue', {
    id,
    name,
    schemaCode: selectedSchemaCode.value,
    hasParams: node?.hasParams,
    allowCustomName: node?.allowCustomName
  })
}

function hasSecondLevelMatch(parentId: number, keyword: string) {
  const p = findNodeById(currentCategories.value, parentId)
  if (!p?.children?.length) return false
  const kw = keyword.trim().toLowerCase()
  if (!kw) return true
  return p.children.some(c => (c.name ?? '').toLowerCase().includes(kw))
}

let inputEl: HTMLInputElement | null = null
let onInput: ((e: Event) => void) | null = null
let onKeyDown: ((e: KeyboardEvent) => void) | null = null

function detachListeners() {
  if (inputEl && onInput) inputEl.removeEventListener('input', onInput)
  if (inputEl && onKeyDown) inputEl.removeEventListener('keydown', onKeyDown)
  inputEl = null
  onInput = null
  onKeyDown = null
}

async function attachListeners() {
  await nextTick()
  const root = cascaderRef.value?.$el
  if (!(root instanceof HTMLElement)) return
  const el = root.querySelector('input') as HTMLInputElement | null
  if (!el) return

  inputEl = el
  onInput = () => {
    filterText.value = inputEl?.value ?? ''
  }
  onKeyDown = (e: KeyboardEvent) => {
    if (e.key !== 'Enter') return
    const kw = (filterText.value || '').trim()
    if (!kw) return

    const parentId = cascaderValue.value?.[0]
    if (!parentId) {
      ElMessage.warning('请先选择一级品类')
      return
    }

    if (!hasSecondLevelMatch(parentId, kw)) {
      e.preventDefault()
      e.stopPropagation()
      customName.value = kw
      customParentId.value = parentId
      customDialogOpen.value = true
    }
  }

  inputEl.addEventListener('input', onInput)
  inputEl.addEventListener('keydown', onKeyDown)
}

function onVisibleChange(visible: boolean) {
  if (!visible) {
    detachListeners()
    filterText.value = ''
    return
  }
  attachListeners()
}

async function submitCustom() {
  if (!customName.value.trim()) {
    ElMessage.warning('请输入品类名称')
    return
  }
  if (!customParentId.value) {
    ElMessage.warning('请先选择一级品类（作为父级）')
    return
  }
  loading.value = true
  try {
    const r = await createCustomProduct({ parentId: customParentId.value, name: customName.value.trim() })
    if (r.code !== 0) throw new Error(r.message)
    await refresh()
    const newId = r.data!
    const parentId = customParentId.value
    cascaderValue.value = [parentId, newId]
    const name = customName.value.trim()
    emit('update:modelValue', {
      id: newId,
      name,
      schemaCode: selectedSchemaCode.value,
      hasParams: false,
      allowCustomName: true
    })
    customDialogOpen.value = false
    ElMessage.success('已创建并选中')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '创建失败')
  } finally {
    loading.value = false
  }
}

onMounted(refresh)
onBeforeUnmount(detachListeners)
</script>

<template>
  <div class="space-y-3">
    <!-- 业态选择 -->
    <div class="flex items-center gap-2 flex-wrap">
      <button
        v-for="schema in schemas"
        :key="schema.schemaCode"
        type="button"
        class="px-4 py-2 rounded-xl text-sm font-bold transition-all border-2"
        :class="selectedSchemaCode === schema.schemaCode
          ? 'bg-brand-500 text-white border-brand-500 shadow-md'
          : 'bg-white text-gray-600 border-gray-200 hover:border-brand-300 hover:text-brand-600'"
        @click="selectedSchemaCode = schema.schemaCode"
      >
        {{ schema.schemaName }}
      </button>
    </div>

    <!-- 品类级联选择 -->
    <div class="flex flex-wrap items-center gap-2">
      <el-cascader
        ref="cascaderRef"
        class="w-full md:w-[360px]"
        :model-value="cascaderValue"
        :options="cascaderOptions"
        :props="{ expandTrigger: 'hover', emitPath: true }"
        :show-all-levels="false"
        clearable
        filterable
        :loading="loading"
        placeholder="选择品类（一级 hover 展开二级）"
        @update:model-value="onCascaderChange"
        @visible-change="onVisibleChange"
      />

      <div v-if="currentLabel" class="px-3 py-2 bg-gray-50 border border-gray-200 rounded-xl text-sm font-bold text-gray-800 truncate max-w-full md:max-w-[220px]">
        {{ currentLabel }}
      </div>

      <button
        type="button"
        class="text-xs text-brand-600 hover:text-brand-700 font-bold ml-2 underline underline-offset-4"
        @click="customDialogOpen = true"
      >
        找不到品类？手动录入
      </button>
    </div>
  </div>

  <el-dialog v-model="customDialogOpen" title="自定义品类" width="520px" append-to-body class="!rounded-3xl">
    <div class="space-y-4 py-2">
      <div v-if="!customParentId">
        <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">选择所属大类</label>
        <el-select v-model="customParentId" placeholder="请选择一级大类" class="w-full neo-select">
          <el-option v-for="opt in cascaderOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </div>
      <div v-else>
        <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">所属大类</label>
        <div class="px-4 py-2.5 bg-gray-50 border-2 border-gray-100 rounded-xl text-sm text-gray-600 font-bold">
          {{ customParentName }}
        </div>
      </div>

      <div>
        <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">品类名称</label>
        <input
          v-model="customName"
          placeholder="例如：特种鱼苗、二手料车..."
          class="w-full px-4 py-3 border-2 border-gray-100 rounded-xl focus:border-brand-500 outline-none transition-all"
          @keyup.enter="submitCustom"
        />
      </div>
    </div>
    <template #footer>
      <div class="flex gap-3">
        <button @click="customDialogOpen = false" class="flex-1 px-4 py-2.5 bg-gray-100 text-gray-600 rounded-xl font-bold">取消</button>
        <button @click="submitCustom" :disabled="loading" class="flex-1 px-4 py-2.5 bg-brand-600 text-white rounded-xl font-bold disabled:opacity-50">
          {{ loading ? '创建中...' : '确认创建' }}
        </button>
      </div>
    </template>
  </el-dialog>
</template>
