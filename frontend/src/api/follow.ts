import { http, type Result } from './http'
import type { RequirementResponse } from './requirement'
import type { SupplyResponse } from './supply'
import type { PostResponse } from './post'

/**
 * 关注的用户信息
 */
export interface FollowedUser {
  userId: number
  userName?: string
  nickName?: string
  position?: string
  companyId?: number
  companyName?: string
  isBuyer?: boolean
  isSeller?: boolean
  followTime?: string
}

/**
 * 关注统计
 */
export interface FollowStats {
  followers: number
  following: number
}

/**
 * 关注用户
 */
export async function followUser(targetUserId: number): Promise<Result<void>> {
  const r = await http.post<Result<void>>(`/api/follows/${targetUserId}`)
  return r.data
}

/**
 * 取消关注
 */
export async function unfollowUser(targetUserId: number): Promise<Result<void>> {
  const r = await http.delete<Result<void>>(`/api/follows/${targetUserId}`)
  return r.data
}

/**
 * 检查是否已关注某用户
 */
export async function checkFollowStatus(targetUserId: number): Promise<Result<boolean>> {
  const r = await http.get<Result<boolean>>(`/api/follows/check/${targetUserId}`)
  return r.data
}

/**
 * 获取我关注的用户列表
 */
export async function getFollowedUsers(): Promise<Result<FollowedUser[]>> {
  const r = await http.get<Result<FollowedUser[]>>('/api/follows/list')
  return r.data
}

/**
 * 获取关注用户发布的采购需求
 */
export async function getFollowedRequirements(): Promise<Result<RequirementResponse[]>> {
  const r = await http.get<Result<RequirementResponse[]>>('/api/follows/requirements')
  return r.data
}

/**
 * 获取关注用户发布的供应信息
 */
export async function getFollowedSupplies(): Promise<Result<SupplyResponse[]>> {
  const r = await http.get<Result<SupplyResponse[]>>('/api/follows/supplies')
  return r.data
}

/**
 * 获取某用户的粉丝数和关注数
 */
export async function getFollowStats(userId: number): Promise<Result<FollowStats>> {
  const r = await http.get<Result<FollowStats>>(`/api/follows/stats/${userId}`)
  return r.data
}

/**
 * 获取关注用户发布的帖子
 */
export async function getFollowedPosts(): Promise<Result<PostResponse[]>> {
  const r = await http.get<Result<PostResponse[]>>('/api/follows/posts')
  return r.data
}

