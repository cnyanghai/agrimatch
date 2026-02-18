import { computed, type Ref } from 'vue'
import { useAuthStore } from '../store/auth'
import {
  sendForSigning,
  signContract,
  cancelContract,
  deleteContract,
  type ContractResponse,
  type ContractSignRequest,
} from '../api/contract'

export function useContractActions(detail: Ref<ContractResponse | null>) {
  const authStore = useAuthStore()

  const isMyBuyer = computed(() => {
    if (!detail.value) return false
    const myId = authStore.user?.companyId
    if (myId && detail.value.buyerCompanyId) return myId === detail.value.buyerCompanyId
    if (authStore.user?.companyName && detail.value.buyerCompanyName) {
      return detail.value.buyerCompanyName === authStore.user.companyName
    }
    return false
  })

  const isMySeller = computed(() => {
    if (!detail.value) return false
    const myId = authStore.user?.companyId
    if (myId && detail.value.sellerCompanyId) return myId === detail.value.sellerCompanyId
    if (authStore.user?.companyName && detail.value.sellerCompanyName) {
      return detail.value.sellerCompanyName === authStore.user.companyName
    }
    return false
  })

  const canEdit = computed(() => detail.value?.status === 0)
  const canSend = computed(() => detail.value?.status === 0)

  const canSign = computed(() => {
    if (detail.value?.status !== 1) return false
    if (isMyBuyer.value && !detail.value.buyerSigned) return true
    if (isMySeller.value && !detail.value.sellerSigned) return true
    return false
  })

  const canCancel = computed(() => {
    if (!detail.value) return false
    return detail.value.status < 4
  })

  const canAddMilestone = computed(() => {
    if (!detail.value) return false
    return detail.value.status === 2 || detail.value.status === 3
  })

  async function handleSend(): Promise<boolean> {
    if (!detail.value) return false
    return new Promise((resolve) => {
      uni.showModal({
        title: '确认发送',
        content: '发送后对方将收到签署邀请，确认发送？',
        success: async (res) => {
          if (!res.confirm) { resolve(false); return }
          try {
            await sendForSigning(detail.value!.id)
            uni.showToast({ title: '已发送签署', icon: 'success' })
            resolve(true)
          } catch {
            resolve(false)
          }
        },
      })
    })
  }

  async function handleSign(): Promise<boolean> {
    if (!detail.value) return false
    return new Promise((resolve) => {
      uni.showModal({
        title: '电子签署',
        content: '请输入您的签名确认签署',
        editable: true,
        placeholderText: '请输入您的姓名',
        success: async (res) => {
          if (!res.confirm || !res.content?.trim()) { resolve(false); return }
          try {
            const req: ContractSignRequest = {
              signType: 'typed',
              typedName: res.content.trim(),
              signerName: res.content.trim(),
            }
            await signContract(detail.value!.id, req)
            uni.showToast({ title: '签署成功', icon: 'success' })
            resolve(true)
          } catch {
            resolve(false)
          }
        },
      })
    })
  }

  async function handleCancel(): Promise<boolean> {
    if (!detail.value) return false
    return new Promise((resolve) => {
      uni.showModal({
        title: '取消合同',
        content: '确认取消该合同？此操作不可撤销。',
        success: async (res) => {
          if (!res.confirm) { resolve(false); return }
          try {
            await cancelContract(detail.value!.id)
            uni.showToast({ title: '已取消', icon: 'success' })
            resolve(true)
          } catch {
            resolve(false)
          }
        },
      })
    })
  }

  async function handleDelete(): Promise<boolean> {
    if (!detail.value) return false
    return new Promise((resolve) => {
      uni.showModal({
        title: '删除合同',
        content: '确认删除该合同草稿？此操作不可撤销。',
        success: async (res) => {
          if (!res.confirm) { resolve(false); return }
          try {
            await deleteContract(detail.value!.id)
            uni.showToast({ title: '已删除', icon: 'success' })
            setTimeout(() => uni.navigateBack(), 500)
            resolve(true)
          } catch {
            resolve(false)
          }
        },
      })
    })
  }

  return {
    isMyBuyer,
    isMySeller,
    canEdit,
    canSend,
    canSign,
    canCancel,
    canAddMilestone,
    handleSend,
    handleSign,
    handleCancel,
    handleDelete,
  }
}
