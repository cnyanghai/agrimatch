/**
 * Contract Flow Composable
 * Manages contract creation and signing flow within chat
 */

import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { ContractPayload, ContractStatus } from '../../types/chat/message'
import { CONTRACT_STATUS_MAP } from '../../types/chat/message'
import type { QuoteFieldsV1, BasisQuoteFieldsV1 } from '../../types/chat/quote'
import { parseQuotePayload, isBasisQuote } from '../../utils/chat/quoteParser'
import type { UseChatMessages } from './useChatMessages'

/** 合同流程步骤 */
export type ContractStep = 'draft' | 'pending' | 'signing' | 'signed' | 'executing' | 'completed'

/** 合同流程状态 */
export interface ContractFlowState {
  step: ContractStep
  contractId?: number
  contractNo?: string
  buyerSigned: boolean
  sellerSigned: boolean
  canSign: boolean
}

/**
 * Contract Flow Composable Options
 */
export interface UseContractFlowOptions {
  messages: UseChatMessages
  getCurrentCompanyId: () => number | undefined
}

/**
 * Contract Flow Composable
 */
export function useContractFlow(options: UseContractFlowOptions) {
  const { messages, getCurrentCompanyId } = options

  // 弹窗状态
  const draftModalVisible = ref(false)
  const signModalVisible = ref(false)

  // 当前操作的数据
  const draftQuoteMessageId = ref<number | null>(null)
  const draftQuoteData = ref<QuoteFieldsV1 | BasisQuoteFieldsV1 | null>(null)
  const signContractId = ref<number | null>(null)
  const viewContractId = ref<number | null>(null)

  // ==================== Contract Status Parsing ====================

  /**
   * 解析合同消息 payload
   */
  function parseContractPayload(payloadJson?: string): ContractPayload | null {
    if (!payloadJson) return null
    try {
      return JSON.parse(payloadJson) as ContractPayload
    } catch {
      return null
    }
  }

  /**
   * 从消息中获取合同状态
   */
  function getContractFromMessages(): ContractPayload | null {
    const contractMsg = messages.contractMessage.value
    if (!contractMsg) return null
    return parseContractPayload(contractMsg.payloadJson)
  }

  /**
   * 获取合同流程状态
   */
  const flowState = computed<ContractFlowState | null>(() => {
    const contract = getContractFromMessages()
    if (!contract) {
      // 检查是否有已接受的报价（可以起草合同）
      if (messages.hasAcceptedQuote.value) {
        return {
          step: 'draft',
          buyerSigned: false,
          sellerSigned: false,
          canSign: false
        }
      }
      return null
    }

    const companyId = getCurrentCompanyId()
    const isBuyer = companyId === contract.buyerCompanyId
    const isSeller = companyId === contract.sellerCompanyId
    const canSign = contract.status === 1 && (
      (isBuyer && !contract.buyerSigned) ||
      (isSeller && !contract.sellerSigned)
    )

    let step: ContractStep = 'pending'
    switch (contract.status) {
      case 0:
        step = 'draft'
        break
      case 1:
        step = contract.buyerSigned || contract.sellerSigned ? 'signing' : 'pending'
        break
      case 2:
        step = 'signed'
        break
      case 3:
        step = 'executing'
        break
      case 4:
        step = 'completed'
        break
    }

    return {
      step,
      contractId: contract.contractId,
      contractNo: contract.contractNo,
      buyerSigned: contract.buyerSigned,
      sellerSigned: contract.sellerSigned,
      canSign
    }
  })

  /**
   * 获取当前步骤索引（用于进度条）
   */
  const currentStepIndex = computed(() => {
    const state = flowState.value
    if (!state) return -1

    const steps: ContractStep[] = ['draft', 'pending', 'signing', 'signed', 'executing', 'completed']
    return steps.indexOf(state.step)
  })

  /**
   * 获取合同状态信息
   */
  const contractStatusInfo = computed(() => {
    const contract = getContractFromMessages()
    if (!contract) return null
    return CONTRACT_STATUS_MAP[contract.status as ContractStatus] || CONTRACT_STATUS_MAP[0]
  })

  // ==================== Draft Contract ====================

  /**
   * 打开起草合同弹窗
   */
  function openDraftModal(messageId?: number): void {
    if (messageId) {
      draftQuoteMessageId.value = messageId
      // 从消息中获取报价数据
      const msg = messages.messages.value.find(m => m.id === messageId)
      if (msg) {
        const payload = parseQuotePayload(msg.payloadJson)
        if (payload) {
          draftQuoteData.value = payload.fields as QuoteFieldsV1 | BasisQuoteFieldsV1
        }
      }
    } else {
      // 从最新已接受报价获取
      const acceptedMsg = messages.latestAcceptedQuoteMessage.value
      if (acceptedMsg) {
        draftQuoteMessageId.value = typeof acceptedMsg.id === 'number' ? acceptedMsg.id : null
        const payload = parseQuotePayload(acceptedMsg.payloadJson)
        if (payload) {
          draftQuoteData.value = payload.fields as QuoteFieldsV1 | BasisQuoteFieldsV1
        }
      }
    }

    draftModalVisible.value = true
  }

  /**
   * 关闭起草合同弹窗
   */
  function closeDraftModal(): void {
    draftModalVisible.value = false
    draftQuoteMessageId.value = null
    draftQuoteData.value = null
  }

  /**
   * 合同创建成功回调
   */
  function onContractCreated(contractId: number): void {
    closeDraftModal()
    ElMessage.success('合同已创建')
  }

  // ==================== Sign Contract ====================

  /**
   * 打开签署合同弹窗
   */
  function openSignModal(contractId: number): void {
    signContractId.value = contractId
    signModalVisible.value = true
  }

  /**
   * 关闭签署合同弹窗
   */
  function closeSignModal(): void {
    signModalVisible.value = false
    signContractId.value = null
  }

  /**
   * 合同签署成功回调
   */
  function onContractSigned(): void {
    closeSignModal()
    ElMessage.success('签署成功')
  }

  // ==================== View Contract ====================

  /**
   * 打开查看合同
   */
  function openViewContract(contractId: number): void {
    viewContractId.value = contractId
    // 可以跳转到合同详情页或打开弹窗
    // router.push({ name: 'ContractDetail', params: { id: contractId } })
  }

  // ==================== Transaction Step ====================

  /**
   * 获取交易进度步骤
   * 0: 无进度
   * 1: 报价中
   * 2: 已确认
   * 3: 合同已起草
   * 4: 合同待签署
   * 5: 合同已签署
   */
  const transactionStep = computed(() => {
    const state = flowState.value

    if (state?.step === 'signed' || state?.step === 'executing' || state?.step === 'completed') {
      return 5
    }
    if (state?.step === 'signing' || state?.step === 'pending') {
      return 4
    }
    if (state?.step === 'draft' && state.contractId) {
      return 3
    }
    if (messages.hasAcceptedQuote.value) {
      return 2
    }
    if (messages.quoteMessages.value.length > 0) {
      return 1
    }
    return 0
  })

  /**
   * 获取交易步骤标签
   */
  const transactionStepLabels = [
    { step: 1, label: '报价中', description: '双方正在商议价格和条款' },
    { step: 2, label: '已确认', description: '报价已被接受，可以起草合同' },
    { step: 3, label: '合同起草', description: '合同已创建，等待发送' },
    { step: 4, label: '待签署', description: '合同待双方签署' },
    { step: 5, label: '已签署', description: '合同已生效' }
  ]

  /**
   * 是否可以起草合同
   */
  const canDraftContract = computed(() => {
    return messages.hasAcceptedQuote.value && !flowState.value?.contractId
  })

  /**
   * 是否可以签署合同
   */
  const canSignContract = computed(() => {
    return flowState.value?.canSign || false
  })

  return {
    // 弹窗状态
    draftModalVisible,
    signModalVisible,

    // 当前操作数据
    draftQuoteMessageId,
    draftQuoteData,
    signContractId,
    viewContractId,

    // Computed
    flowState,
    currentStepIndex,
    contractStatusInfo,
    transactionStep,
    transactionStepLabels,
    canDraftContract,
    canSignContract,

    // 起草合同
    openDraftModal,
    closeDraftModal,
    onContractCreated,

    // 签署合同
    openSignModal,
    closeSignModal,
    onContractSigned,

    // 查看合同
    openViewContract,

    // 工具函数
    parseContractPayload,
    getContractFromMessages
  }
}

export type UseContractFlow = ReturnType<typeof useContractFlow>
