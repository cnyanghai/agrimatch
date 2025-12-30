<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createCustomProduct, getProductTree, type ProductNode } from '../api/product'

const props = defineProps<{
  modelValue?: { id: number; name: string } | null
}>()
const emit = defineEmits<{
  (e: 'update:modelValue', v: { id: number; name: string } | null): void
}>()

const loading = ref(false)
const tree = ref<ProductNode[]>([])
const valueId = computed<number | undefined>({
  get: () => props.modelValue?.id,
  set: () => {
    // noop: 由 onNodeChange 统一更新
  }
})

const dialogOpen = ref(false)
const customName = ref('')
const customParentId = ref<number | undefined>(undefined)

const treeSelectProps = {
  value: 'id',
  label: 'name',
  children: 'children'
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

async function refresh() {
  loading.value = true
  try {
    const r = await getProductTree()
    if (r.code !== 0) throw new Error(r.message)
    tree.value = r.data ?? []
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载品类失败')
  } finally {
    loading.value = false
  }
}

function onNodeChange(id?: number) {
  if (!id) {
    emit('update:modelValue', null)
    return
  }
  const name = findNameById(tree.value, id) ?? String(id)
  emit('update:modelValue', { id, name })
}

function openCustom() {
  customName.value = ''
  customParentId.value = props.modelValue?.id
  dialogOpen.value = true
}

async function submitCustom() {
  if (!customName.value.trim()) {
    ElMessage.warning('请输入品类名称')
    return
  }
  loading.value = true
  try {
    const r = await createCustomProduct({ parentId: customParentId.value, name: customName.value.trim() })
    if (r.code !== 0) throw new Error(r.message)
    await refresh()
    const newId = r.data!
    onNodeChange(newId)
    ElMessage.success('已创建并选中')
    dialogOpen.value = false
  } catch (e: any) {
    ElMessage.error(e?.message ?? '创建失败')
  } finally {
    loading.value = false
  }
}

onMounted(refresh)
</script>

<template>
  <div style="display:flex; gap:8px; align-items:center;">
    <el-tree-select
      style="flex:1;"
      :model-value="valueId"
      :data="tree"
      :props="treeSelectProps"
      :loading="loading"
      filterable
      clearable
      placeholder="选择品类（支持搜索）"
      @update:model-value="onNodeChange"
    />
    <el-button :loading="loading" @click="openCustom">自定义</el-button>
  </div>

  <el-dialog v-model="dialogOpen" title="自定义品类" width="520px">
    <el-form label-width="110px">
      <el-form-item label="上级品类(可选)">
        <el-tree-select
          style="width: 360px;"
          v-model="customParentId"
          :data="tree"
          :props="treeSelectProps"
          :loading="loading"
          filterable
          clearable
          placeholder="不选则创建为一级品类"
        />
      </el-form-item>
      <el-form-item label="品类名称">
        <el-input v-model="customName" placeholder="例如：豆粕(高蛋白)" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogOpen = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submitCustom">创建</el-button>
    </template>
  </el-dialog>
</template>


