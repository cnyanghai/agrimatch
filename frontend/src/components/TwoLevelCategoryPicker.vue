<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createCustomProduct, getProductTree, type ProductNode } from '../api/product'

type Picked = { id: number; name: string } | null

const props = defineProps<{
  modelValue?: Picked
}>()
const emit = defineEmits<{
  (e: 'update:modelValue', v: Picked): void
}>()

const loading = ref(false)
const tree = ref<ProductNode[]>([])

// el-cascader value: [level1Id, level2Id]
const cascaderValue = ref<number[] | undefined>(undefined)

const cascaderOptions = computed(() =>
  tree.value
    .filter((x) => (x.parentId ?? 0) === 0)
    .map((p) => ({
      value: p.id,
      label: p.name,
      children: (p.children ?? []).map((c) => ({ value: c.id, label: c.name }))
    }))
)

const currentLabel = computed(() => props.modelValue?.name ?? '')

const customDialogOpen = ref(false)
const customName = ref('')
const customParentId = ref<number | undefined>(undefined) // 一级品类 id
const customParentName = computed(() => {
  const pid = customParentId.value
  if (!pid) return ''
  return findNameById(tree.value, pid) ?? ''
})

async function refresh() {
  loading.value = true
  try {
    const r = await getProductTree()
    if (r.code !== 0) throw new Error(r.message)
    tree.value = r.data ?? []

    // 回显：如果已有二级选中，推导一级并设置级联值
    if (props.modelValue?.id) {
      const parent = findParentId(tree.value, props.modelValue.id)
      if (parent != null && parent > 0) {
        cascaderValue.value = [parent, props.modelValue.id]
      }
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载品类失败')
  } finally {
    loading.value = false
  }
}

function findParentId(list: ProductNode[], childId: number): number | null {
  for (const n of list) {
    if (n.id === childId) return n.parentId ?? 0
    if (n.children?.length) {
      const r = findParentId(n.children, childId)
      if (r != null) return r
    }
  }
  return null
}

function findNameById(list: ProductNode[], id: number): string | null {
  for (const n of list) {
    if (n.id === id) return n.name
    if (n.children?.length) {
      const r = findNameById(n.children, id)
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
  const name = findNameById(tree.value, id) ?? String(id)
  emit('update:modelValue', { id, name })
}

function openCustom() {
  const p = cascaderValue.value?.[0]
  if (!p) {
    ElMessage.warning('请先选择一级品类')
    return
  }
  customName.value = ''
  customParentId.value = p
  customDialogOpen.value = true
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
    emit('update:modelValue', { id: newId, name })
    customDialogOpen.value = false
    ElMessage.success('已创建并选中')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '创建失败')
  } finally {
    loading.value = false
  }
}

onMounted(refresh)
</script>

<template>
  <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;">
    <el-cascader
      style="width: 360px;"
      :model-value="cascaderValue"
      :options="cascaderOptions"
      :props="{ expandTrigger: 'hover', emitPath: true }"
      :show-all-levels="false"
      clearable
      filterable
      :loading="loading"
      placeholder="一级 hover 展开二级"
      @update:model-value="onCascaderChange"
    />

    <el-input :model-value="currentLabel" disabled placeholder="已选二级品类" style="width: 220px;" />
    <el-button :loading="loading" @click="openCustom">自定义二级</el-button>
  </div>

  <el-dialog v-model="customDialogOpen" title="自定义二级品类" width="520px">
    <el-form label-width="110px">
      <el-form-item label="父级(一级品类)">
        <el-input :model-value="customParentName" disabled placeholder="请先在左侧选择一级品类" />
      </el-form-item>
      <el-form-item label="品类名称">
        <el-input v-model="customName" placeholder="例如：豆粕(高蛋白)" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="customDialogOpen = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submitCustom">创建并选中</el-button>
    </template>
  </el-dialog>
</template>


