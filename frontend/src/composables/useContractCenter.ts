import { computed, ref } from 'vue'
import { createContract, deleteContract as apiDeleteContract, getNextContractNo, listContracts, updateContract } from '../api/contract'
import type { ContractCreateRequest, ContractResponse, ContractStatus, ContractType, ContractUpdateRequest } from '../api/contract'

export interface ContractRow {
  id: number
  title: string
  contractNo: string
  contractType: ContractType
  partyA: string
  partyB: string
  productName?: string
  quantity?: number
  unit?: string
  unitPrice?: number
  totalAmount?: number
  status: ContractStatus
  createTime: string
  signTime?: string
  deliveryDate?: string
  deliveryAddress?: string
  paymentMethod?: string
  terms?: string
  pdfHash?: string
}

export interface ContractPreviewData {
  header: string
  sections: Array<{ title: string; content: string }>
  metadata: { contractNo: string; status: ContractStatus; version: string }
}

export interface ContractDraft {
  id?: number
  contractNo: string
  contractType: ContractType
  title: string
  partyA: string
  partyB: string
  productName: string
  quantity: number
  unit: string
  unitPrice: number
  deliveryDate: string
  deliveryAddress: string
  paymentMethod: string
  terms: string
}

function toRow(c: ContractResponse): ContractRow {
  return {
    id: c.id,
    title: c.title,
    contractNo: c.contractNo,
    contractType: c.contractType,
    partyA: c.partyA,
    partyB: c.partyB,
    productName: c.productName,
    quantity: c.quantity,
    unit: c.unit,
    unitPrice: c.unitPrice,
    totalAmount: c.totalAmount,
    status: c.status,
    createTime: c.createTime || '',
    signTime: c.signTime,
    deliveryDate: c.deliveryDate,
    deliveryAddress: c.deliveryAddress,
    paymentMethod: c.paymentMethod,
    terms: c.terms,
    pdfHash: c.pdfHash
  }
}

export function useContractCenter() {
  const isLoading = ref(false)
  const searchQuery = ref('')
  const statusFilter = ref<ContractStatus | 'all'>('all')
  const selectedId = ref<number | null>(null)
  const mode = ref<'preview' | 'editor'>('preview')

  // editor state
  const editor = ref<ContractDraft | null>(null)

  const contracts = ref<ContractRow[]>([])

  const stats = computed(() => {
    const all = contracts.value.length
    const by = (s: ContractStatus) => contracts.value.filter((c) => c.status === s).length
    return {
      all,
      draft: by('draft'),
      pending: by('pending'),
      signed: by('signed'),
      executing: by('executing'),
      completed: by('completed'),
      cancelled: by('cancelled')
    }
  })

  const filteredContracts = computed(() => {
    const q = searchQuery.value.trim().toLowerCase()
    return contracts.value.filter((c) => {
      if (statusFilter.value !== 'all' && c.status !== statusFilter.value) return false
      if (!q) return true
      return (
        c.title.toLowerCase().includes(q) ||
        c.contractNo.toLowerCase().includes(q) ||
        (c.productName || '').toLowerCase().includes(q) ||
        c.partyA.toLowerCase().includes(q) ||
        c.partyB.toLowerCase().includes(q)
      )
    })
  })

  const selectedContract = computed(() => {
    if (!selectedId.value) return null
    return contracts.value.find((c) => c.id === selectedId.value) ?? null
  })

  const previewData = computed<ContractPreviewData | null>(() => {
    const c = selectedContract.value
    if (!c) return null
    const qty = c.quantity != null ? `${c.quantity} ${c.unit || ''}`.trim() : '-'
    const price = c.unitPrice != null ? `${c.unitPrice}/${c.unit || '单位'}` : '-'
    const total = c.totalAmount != null ? `¥${Math.round(c.totalAmount).toLocaleString('zh-CN')}` : '-'
    const typeLabel = c.contractType === 'purchase' ? '采购合同' : '供应合同'

    return {
      header: c.title || '合同预览',
      sections: [
        { title: '一、合同概览', content: `合同编号：${c.contractNo}\n合同类型：${typeLabel}\n状态：${c.status}\n创建时间：${c.createTime || '-'}` },
        { title: '二、签署主体', content: `甲方：${c.partyA}\n乙方：${c.partyB}` },
        { title: '三、标的与数量', content: `产品：${c.productName || '-'}\n数量：${qty}\n单价：${price}\n合同金额：${total}` },
        { title: '四、交付与结算', content: `交付日期：${c.deliveryDate || '-'}\n交付地址：${c.deliveryAddress || '-'}\n付款方式：${c.paymentMethod || '-'}` },
        { title: '五、其他条款', content: c.terms || '无' }
      ],
      metadata: {
        contractNo: c.contractNo,
        status: c.status,
        version: 'V1.0'
      }
    }
  })

  async function loadContracts() {
    isLoading.value = true
    try {
      const r = await listContracts({
        keyword: searchQuery.value || undefined,
        status: statusFilter.value,
        orderBy: 'create_time',
        order: 'desc'
      })
      if (r.code !== 0) throw new Error(r.message)
      contracts.value = (r.data ?? []).map(toRow)
    } finally {
      isLoading.value = false
    }
  }

  function selectContract(id: number) {
    selectedId.value = id
    mode.value = 'preview'
    editor.value = null
  }

  async function startCreate() {
    mode.value = 'editor'
    // 获取后端编号，保持一致
    let no = ''
    try {
      const r = await getNextContractNo()
      if (r.code === 0 && r.data) no = r.data
    } catch {
      // ignore
    }
    editor.value = {
      contractNo: no || `HT-${new Date().getFullYear()}-${String(Date.now()).slice(-4)}`,
      contractType: 'purchase',
      title: '',
      partyA: '',
      partyB: '',
      productName: '',
      quantity: 0,
      unit: '吨',
      unitPrice: 0,
      deliveryDate: '',
      deliveryAddress: '',
      paymentMethod: '',
      terms: ''
    }
  }

  function cancelEdit() {
    mode.value = 'preview'
    editor.value = null
  }

  async function saveDraft(draft: ContractDraft, nextStatus: ContractStatus = 'draft') {
    isLoading.value = true
    try {
      if (!draft.id) {
        const req: ContractCreateRequest = {
          contractNo: draft.contractNo,
          contractType: draft.contractType,
          title: draft.title || '未命名合同',
          partyA: draft.partyA,
          partyB: draft.partyB,
          productName: draft.productName || undefined,
          quantity: draft.quantity,
          unit: draft.unit,
          unitPrice: draft.unitPrice,
          deliveryDate: draft.deliveryDate || undefined,
          deliveryAddress: draft.deliveryAddress || undefined,
          paymentMethod: draft.paymentMethod || undefined,
          terms: draft.terms || undefined,
          status: nextStatus
        }
        const r = await createContract(req)
        if (r.code !== 0 || !r.data) throw new Error(r.message)
      } else {
        const req: ContractUpdateRequest = {
          title: draft.title,
          contractType: draft.contractType,
          partyA: draft.partyA,
          partyB: draft.partyB,
          productName: draft.productName || undefined,
          quantity: draft.quantity,
          unit: draft.unit,
          unitPrice: draft.unitPrice,
          deliveryDate: draft.deliveryDate || undefined,
          deliveryAddress: draft.deliveryAddress || undefined,
          paymentMethod: draft.paymentMethod || undefined,
          terms: draft.terms || undefined,
          status: nextStatus
        }
        const r = await updateContract(draft.id, req)
        if (r.code !== 0) throw new Error(r.message)
      }
      await loadContracts()
      // 选中最近一条（按 create_time desc）
      selectedId.value = contracts.value[0]?.id ?? null
      mode.value = 'preview'
      editor.value = null
    } finally {
      isLoading.value = false
    }
  }

  async function deleteContract(id: number) {
    const r = await apiDeleteContract(id)
    if (r.code !== 0) throw new Error(r.message)
    await loadContracts()
    if (selectedId.value === id) selectedId.value = null
  }

  async function changeStatus(id: number, s: ContractStatus) {
    const r = await updateContract(id, { status: s })
    if (r.code !== 0) throw new Error(r.message)
    await loadContracts()
  }

  return {
    // state
    contracts,
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
  }
}


