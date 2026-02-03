import { get, post, del } from '../utils/request'

export interface MilestoneResponse {
  id: number
  contractId: number
  milestoneType: string
  responsibleParty: string
  milestoneName: string
  expectedDate?: string
  actualDate?: string
  status: string  // 'PENDING' | 'SUBMITTED' | 'CONFIRMED' | 'REJECTED'
  remark?: string
  rejectReason?: string
  sortOrder?: number
  operatorName?: string
  confirmUserName?: string
  confirmTime?: string
  createTime?: string
}

export interface MilestoneCreateRequest {
  milestoneType: string
  responsibleParty: string
  milestoneName: string
  expectedDate?: string
  sortOrder?: number
}

export interface MilestoneSubmitRequest {
  actualDate: string
  remark?: string
}

export function listMilestones(contractId: number) {
  return get<MilestoneResponse[]>(`/api/contracts/${contractId}/milestones`)
}

export function createMilestone(contractId: number, req: MilestoneCreateRequest) {
  return post<number>(`/api/contracts/${contractId}/milestones`, req)
}

export function submitMilestone(milestoneId: number, req: MilestoneSubmitRequest) {
  return post<void>(`/api/milestones/${milestoneId}/submit`, req)
}

export function confirmMilestone(milestoneId: number) {
  return post<void>(`/api/milestones/${milestoneId}/confirm`)
}

export function rejectMilestone(milestoneId: number, rejectReason: string) {
  return post<void>(`/api/milestones/${milestoneId}/reject`, { rejectReason })
}

export function deleteMilestone(milestoneId: number) {
  return del<void>(`/api/milestones/${milestoneId}`)
}

export function generateStandardMilestones(contractId: number) {
  return post<void>(`/api/contracts/${contractId}/milestones/generate`)
}
