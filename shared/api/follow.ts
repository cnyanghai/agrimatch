/**
 * Follow module - Types and API path constants.
 */

// ==================== Types ====================

export interface FollowedUser {
  userId: number
  userName?: string
  nickName?: string
  position?: string
  avatar?: string
  companyId?: number
  companyName?: string
  phone?: string
  isBuyer?: boolean
  isSeller?: boolean
  followTime?: string
}

export interface FollowStats {
  followers: number
  following: number
}

// ==================== API Path Constants ====================

export const FOLLOW_API = {
  /** POST - Follow user: /api/follows/:targetUserId */
  FOLLOW: (targetUserId: number) => `/api/follows/${targetUserId}`,
  /** DELETE - Unfollow user: /api/follows/:targetUserId */
  UNFOLLOW: (targetUserId: number) => `/api/follows/${targetUserId}`,
  /** GET - Check follow status: /api/follows/check/:targetUserId */
  CHECK: (targetUserId: number) => `/api/follows/check/${targetUserId}`,
  /** GET - List followed users */
  LIST: '/api/follows/list',
  /** GET - Get followed users' requirements */
  REQUIREMENTS: '/api/follows/requirements',
  /** GET - Get followed users' supplies */
  SUPPLIES: '/api/follows/supplies',
  /** GET - Get followed users' posts */
  POSTS: '/api/follows/posts',
  /** GET - Get follow stats: /api/follows/stats/:userId */
  STATS: (userId: number) => `/api/follows/stats/${userId}`,
} as const
