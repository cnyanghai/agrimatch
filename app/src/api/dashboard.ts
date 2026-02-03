import { get } from '../utils/request'

export interface DashboardResponse {
  unreadMessageCount: number
  pendingContractCount: number
  pendingInquiryCount: number
  pendingMilestoneCount: number
  myActiveListingCount: number
  activeContractCount: number
  totalSignedContractCount: number
  totalDealAmount: number
  followingCount: number
  pointsBalance: number
}

/** 获取完整仪表盘数据 */
export function getDashboard() {
  return get<DashboardResponse>('/api/dashboard')
}

/** 获取待处理数量（轻量级，用于 badge） */
export function getPendingCount() {
  return get<number>('/api/dashboard/pending-count')
}
