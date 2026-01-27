import { http } from './http'

/**
 * 控制台首页数据响应
 */
export interface DashboardResponse {
  // 待办事项
  unreadMessageCount: number
  pendingContractCount: number
  pendingInquiryCount: number
  pendingMilestoneCount: number

  // 业务统计
  myActiveListingCount: number
  activeContractCount: number
  totalSignedContractCount: number
  totalDealAmount: number

  // 用户相关
  followingCount: number
  pointsBalance: number
}

/**
 * 获取控制台首页数据
 */
export async function getDashboard(): Promise<{ code: number; message?: string; data?: DashboardResponse }> {
  const { data } = await http.get('/api/dashboard')
  return data
}

/**
 * 获取待办事项总数（用于显示角标）
 */
export async function getPendingCount(): Promise<{ code: number; message?: string; data?: number }> {
  const { data } = await http.get('/api/dashboard/pending-count')
  return data
}

