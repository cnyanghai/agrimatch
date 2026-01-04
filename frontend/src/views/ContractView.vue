<script setup lang="ts">
import { onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ContractEditorPanel from '../components/contracts/ContractEditorPanel.vue'
import ContractListPanel from '../components/contracts/ContractListPanel.vue'
import ContractPreviewPanel from '../components/contracts/ContractPreviewPanel.vue'
import { useContractCenter } from '../composables/useContractCenter'
import { downloadContractPdf } from '../api/contract'

const {
  // state (refs)
  isLoading,
  searchQuery,
  statusFilter,
  selectedId,
  mode,
  editor,

  // computed
  filteredContracts,
  selectedContract,
  previewData,
  stats,

  // actions
  loadContracts,
  selectContract,
  startCreate,
  cancelEdit,
  saveDraft,
  deleteContract,
  changeStatus
} = useContractCenter()

onMounted(() => {
  loadContracts()
})

async function onDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除该合同吗？删除后不可恢复。', '删除合同', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await deleteContract(id)
    ElMessage.success('已删除')
  } catch {
    // cancelled
  }
}

function onPrint() {
  window.print()
}

function onDownload() {
  const c = selectedContract.value
  if (!c) return
  downloadContractPdf(c.id).then(({ blob, hash }) => {
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${c.contractNo || 'contract'}-${c.id}.pdf`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(url)
    if (hash) ElMessage.success(`已下载 PDF（${hash}）`)
    else ElMessage.success('已下载 PDF')
  }).catch((e: any) => {
    ElMessage.error(e?.message || '下载失败')
  })
}

async function onInitiateSign() {
  const c = selectedContract.value
  if (!c) return
  try {
    await ElMessageBox.confirm(`确定要发起合同 ${c.contractNo} 的签署流程吗？`, '发起签署', {
      type: 'info',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await changeStatus(c.id, 'pending')
    ElMessage.success('已发起签署流程，等待对方确认')
  } catch {
    // cancelled
  }
}

async function onSign() {
  const c = selectedContract.value
  if (!c) return
  try {
    await ElMessageBox.confirm(`确定要签署合同 ${c.contractNo} 吗？`, '签署合同', {
      type: 'warning',
      confirmButtonText: '确定签署',
      cancelButtonText: '取消'
    })
    await changeStatus(c.id, 'signed')
    ElMessage.success('合同签署成功')
  } catch {
    // cancelled
  }
}

async function onSaveDraft(nextStatus: 'draft' | 'pending') {
  const d = editor.value
  if (!d) return
  if (!d.title?.trim()) return ElMessage.warning('请输入合同标题')
  if (!d.partyA?.trim()) return ElMessage.warning('请输入甲方名称')
  if (!d.partyB?.trim()) return ElMessage.warning('请输入乙方名称')
  await saveDraft(d, nextStatus)
  if (nextStatus === 'pending') {
    ElMessage.success('已保存并发起签署')
  } else {
    ElMessage.success('已保存草稿')
  }
}
</script>

<template>
  <div class="bg-gray-50 text-gray-900 min-h-screen">
    <div class="max-w-7xl mx-auto px-4 py-8">
      <div class="flex flex-col lg:flex-row gap-6">
        <ContractListPanel
          :contracts="filteredContracts"
          :selected-id="selectedId"
          :loading="isLoading"
          :search-query="searchQuery"
          :status-filter="statusFilter"
          :stats="stats"
          @update:searchQuery="(v) => (searchQuery = v)"
          @update:statusFilter="(v) => (statusFilter = v)"
          @select="selectContract"
          @create="startCreate"
          @delete="onDelete"
        />

        <ContractEditorPanel
          v-if="mode === 'editor' && editor"
          v-model="editor"
          :saving="isLoading"
          @cancel="cancelEdit"
          @saveDraft="onSaveDraft('draft')"
          @saveAndInitiate="onSaveDraft('pending')"
        />

        <ContractPreviewPanel
          v-else
          :contract="selectedContract"
          :preview="previewData"
          @print="onPrint"
          @download="onDownload"
          @initiateSign="onInitiateSign"
          @sign="onSign"
        />
      </div>
    </div>
  </div>
</template>


