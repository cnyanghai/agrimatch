export const CONTRACT_STATUS_CONFIG = {
  DRAFT: { value: 0, label: '草稿', color: 'text-neutral-600', bgColor: 'bg-neutral-100', barColor: 'bg-neutral-400', bgGradient: 'from-neutral-400 to-neutral-500', icon: 'FileEdit' },
  PENDING: { value: 1, label: '待签署', color: 'text-warning-600', bgColor: 'bg-warning-50', barColor: 'bg-warning-500', bgGradient: 'from-warning-400 to-accent-500', icon: 'FileClock' },
  SIGNED: { value: 2, label: '已签署', color: 'text-brand-600', bgColor: 'bg-brand-50', barColor: 'bg-brand-500', bgGradient: 'from-brand-400 to-brand-600', icon: 'FileCheck' },
  EXECUTING: { value: 3, label: '履约中', color: 'text-action-600', bgColor: 'bg-action-50', barColor: 'bg-action-500', bgGradient: 'from-action-400 to-action-600', icon: 'FileBox' },
  COMPLETED: { value: 4, label: '已完成', color: 'text-brand-700', bgColor: 'bg-brand-100', barColor: 'bg-brand-600', bgGradient: 'from-brand-500 to-brand-700', icon: 'FileCheck2' },
  CANCELLED: { value: 5, label: '已取消', color: 'text-error-500', bgColor: 'bg-error-50', barColor: 'bg-error-500', bgGradient: 'from-error-400 to-error-600', icon: 'FileX' }
} as const

export const CONTRACT_STATUS_MAP: Record<number, { label: string; color: string; bgColor: string }> = {
  0: { label: CONTRACT_STATUS_CONFIG.DRAFT.label, color: CONTRACT_STATUS_CONFIG.DRAFT.color, bgColor: CONTRACT_STATUS_CONFIG.DRAFT.bgColor },
  1: { label: CONTRACT_STATUS_CONFIG.PENDING.label, color: CONTRACT_STATUS_CONFIG.PENDING.color, bgColor: CONTRACT_STATUS_CONFIG.PENDING.bgColor },
  2: { label: CONTRACT_STATUS_CONFIG.SIGNED.label, color: CONTRACT_STATUS_CONFIG.SIGNED.color, bgColor: CONTRACT_STATUS_CONFIG.SIGNED.bgColor },
  3: { label: CONTRACT_STATUS_CONFIG.EXECUTING.label, color: CONTRACT_STATUS_CONFIG.EXECUTING.color, bgColor: CONTRACT_STATUS_CONFIG.EXECUTING.bgColor },
  4: { label: CONTRACT_STATUS_CONFIG.COMPLETED.label, color: CONTRACT_STATUS_CONFIG.COMPLETED.color, bgColor: CONTRACT_STATUS_CONFIG.COMPLETED.bgColor },
  5: { label: CONTRACT_STATUS_CONFIG.CANCELLED.label, color: CONTRACT_STATUS_CONFIG.CANCELLED.color, bgColor: CONTRACT_STATUS_CONFIG.CANCELLED.bgColor }
}

export const MILESTONE_STATUS_CONFIG = {
  PENDING: { value: 0, label: '待执行', color: 'text-neutral-600', bgColor: 'bg-neutral-100', icon: 'Circle' },
  IN_PROGRESS: { value: 1, label: '进行中', color: 'text-action-600', bgColor: 'bg-action-50', icon: 'CircleDot' },
  SUBMITTED: { value: 2, label: '已提交', color: 'text-warning-600', bgColor: 'bg-warning-50', icon: 'Clock' },
  APPROVED: { value: 3, label: '已确认', color: 'text-brand-600', bgColor: 'bg-brand-50', icon: 'CircleCheck' },
  REJECTED: { value: 4, label: '已驳回', color: 'text-error-500', bgColor: 'bg-error-50', icon: 'CircleX' }
} as const

export const MILESTONE_STATUS_MAP: Record<number, { label: string; color: string; bgColor: string }> = {
  0: { label: MILESTONE_STATUS_CONFIG.PENDING.label, color: MILESTONE_STATUS_CONFIG.PENDING.color, bgColor: MILESTONE_STATUS_CONFIG.PENDING.bgColor },
  1: { label: MILESTONE_STATUS_CONFIG.IN_PROGRESS.label, color: MILESTONE_STATUS_CONFIG.IN_PROGRESS.color, bgColor: MILESTONE_STATUS_CONFIG.IN_PROGRESS.bgColor },
  2: { label: MILESTONE_STATUS_CONFIG.SUBMITTED.label, color: MILESTONE_STATUS_CONFIG.SUBMITTED.color, bgColor: MILESTONE_STATUS_CONFIG.SUBMITTED.bgColor },
  3: { label: MILESTONE_STATUS_CONFIG.APPROVED.label, color: MILESTONE_STATUS_CONFIG.APPROVED.color, bgColor: MILESTONE_STATUS_CONFIG.APPROVED.bgColor },
  4: { label: MILESTONE_STATUS_CONFIG.REJECTED.label, color: MILESTONE_STATUS_CONFIG.REJECTED.color, bgColor: MILESTONE_STATUS_CONFIG.REJECTED.bgColor }
}

export const CHAT_OFFER_STATUS_CONFIG = {
  PENDING: { value: 0, label: '待确认', color: 'text-warning-600', bgColor: 'bg-warning-50', icon: 'Clock' },
  ACCEPTED: { value: 1, label: '已接受', color: 'text-brand-600', bgColor: 'bg-brand-50', icon: 'CheckCircle' },
  REJECTED: { value: 2, label: '已拒绝', color: 'text-error-500', bgColor: 'bg-error-50', icon: 'XCircle' },
  EXPIRED: { value: 3, label: '已过期', color: 'text-neutral-500', bgColor: 'bg-neutral-100', icon: 'AlertCircle' }
} as const

export const CHAT_OFFER_STATUS_MAP: Record<number, { label: string; color: string; bgColor: string }> = {
  0: { label: CHAT_OFFER_STATUS_CONFIG.PENDING.label, color: CHAT_OFFER_STATUS_CONFIG.PENDING.color, bgColor: CHAT_OFFER_STATUS_CONFIG.PENDING.bgColor },
  1: { label: CHAT_OFFER_STATUS_CONFIG.ACCEPTED.label, color: CHAT_OFFER_STATUS_CONFIG.ACCEPTED.color, bgColor: CHAT_OFFER_STATUS_CONFIG.ACCEPTED.bgColor },
  2: { label: CHAT_OFFER_STATUS_CONFIG.REJECTED.label, color: CHAT_OFFER_STATUS_CONFIG.REJECTED.color, bgColor: CHAT_OFFER_STATUS_CONFIG.REJECTED.bgColor },
  3: { label: CHAT_OFFER_STATUS_CONFIG.EXPIRED.label, color: CHAT_OFFER_STATUS_CONFIG.EXPIRED.color, bgColor: CHAT_OFFER_STATUS_CONFIG.EXPIRED.bgColor }
}
