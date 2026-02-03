/**
 * Post/Topic module - Types and API path constants.
 */

// ==================== Types ====================

export type PostType = 'general' | 'poll' | 'paid'

export interface PostCreateRequest {
  title: string
  content?: string
  imagesJson?: string
  postType?: PostType
  isPaid?: boolean
  price?: number
  teaserLength?: number
  domain?: string
}

export interface PostResponse {
  id: number
  companyId?: number
  userId: number
  companyName?: string
  userName?: string
  nickName?: string
  position?: string
  avatar?: string
  title: string
  content?: string
  imagesJson?: string
  postType?: PostType
  isPaid?: boolean
  price?: number
  teaserLength?: number
  isExpert?: boolean
  createTime?: string
  likeCount?: number
  commentCount?: number
  likedByMe?: boolean
  collectedByMe?: boolean
  hasPurchased?: boolean
  domain?: string
}

export interface PostQuery {
  companyId?: number
  userId?: number
  keyword?: string
  postType?: string
  orderBy?: string
  order?: string
  recentDays?: number
  limit?: number
  followingUserId?: number
  onlyCollected?: boolean
  domain?: string
  viewerUserId?: number
}

export interface PostLikeToggleResponse {
  liked: boolean
  likeCount: number
}

export interface PostCommentResponse {
  id: number
  postId: number
  companyId?: number
  userId: number
  companyName?: string
  userName?: string
  nickName?: string
  content: string
  isAccepted?: number
  createTime?: string
}

// ==================== API Path Constants ====================

export const POST_API = {
  /** POST - Create post */
  CREATE: '/api/posts',
  /** GET - List posts */
  LIST: '/api/posts',
  /** GET - Get post by ID: /api/posts/:id */
  GET_BY_ID: (id: number) => `/api/posts/${id}`,
  /** PUT - Update post: /api/posts/:id */
  UPDATE: (id: number) => `/api/posts/${id}`,
  /** DELETE - Delete post: /api/posts/:id */
  DELETE: (id: number) => `/api/posts/${id}`,
  /** POST - Toggle like: /api/posts/:id/like */
  TOGGLE_LIKE: (postId: number) => `/api/posts/${postId}/like`,
  /** GET - List comments: /api/posts/:id/comments */
  LIST_COMMENTS: (postId: number) => `/api/posts/${postId}/comments`,
  /** POST - Create comment: /api/posts/:id/comments */
  CREATE_COMMENT: (postId: number) => `/api/posts/${postId}/comments`,
  /** POST - Toggle collect: /api/posts/:id/collect */
  TOGGLE_COLLECT: (postId: number) => `/api/posts/${postId}/collect`,
  /** GET - Get collect status: /api/posts/:id/collect/status */
  COLLECT_STATUS: (postId: number) => `/api/posts/${postId}/collect/status`,
  /** GET - List collected post IDs */
  COLLECTED: '/api/posts/collected',
} as const
