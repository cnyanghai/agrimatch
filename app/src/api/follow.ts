import { get, post, del } from '../utils/request'
import type { PostResponse } from './post'

export interface FollowedUser {
  userId: number
  userName?: string
  nickName?: string
  position?: string
  avatar?: string
  companyId?: number
  companyName?: string
  followTime?: string
}

export interface FollowStats {
  followers: number
  following: number
}

/** 关注用户 */
export function followUser(targetUserId: number) {
  return post<void>(`/api/follows/${targetUserId}`)
}

/** 取消关注 */
export function unfollowUser(targetUserId: number) {
  return del<void>(`/api/follows/${targetUserId}`)
}

/** 检查是否已关注 */
export function checkFollowStatus(targetUserId: number) {
  return get<boolean>(`/api/follows/check/${targetUserId}`)
}

/** 获取我关注的用户列表 */
export function getFollowedUsers() {
  return get<FollowedUser[]>('/api/follows/list')
}

/** 获取关注用户发布的帖子 */
export function getFollowedPosts() {
  return get<PostResponse[]>('/api/follows/posts')
}

/** 获取某用户的粉丝数和关注数 */
export function getFollowStats(userId: number) {
  return get<FollowStats>(`/api/follows/stats/${userId}`)
}

/**
 * 批量检查关注状态（并行请求，后端暂无批量接口）
 * 返回 Map<userId, isFollowing>
 */
export async function batchCheckFollowStatus(userIds: number[]): Promise<Map<number, boolean>> {
  const map = new Map<number, boolean>()
  if (!userIds.length) return map

  const results = await Promise.allSettled(
    userIds.map(async (uid) => {
      const following = await checkFollowStatus(uid)
      return { uid, following }
    })
  )

  for (const result of results) {
    if (result.status === 'fulfilled') {
      map.set(result.value.uid, result.value.following)
    }
  }

  return map
}
