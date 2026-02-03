import { get, post, put, del } from '../utils/request'

export interface PostResponse {
  id: number
  userId: number
  companyId?: number
  companyName?: string
  userName?: string
  nickName?: string
  position?: string
  avatar?: string
  title: string
  content?: string
  imagesJson?: string
  postType?: string
  isPaid?: boolean
  price?: number
  createTime?: string
  likeCount?: number
  commentCount?: number
  likedByMe?: boolean
  collectedByMe?: boolean
  domain?: string
}

export interface PostQuery {
  keyword?: string
  postType?: string
  orderBy?: string
  recentDays?: number
  limit?: number
  domain?: string
  userId?: number
  companyId?: number
  onlyCollected?: boolean
}

export interface PostCommentResponse {
  id: number
  postId: number
  userId: number
  userName?: string
  nickName?: string
  content: string
  createTime?: string
}

export interface PostCreateRequest {
  title: string
  content?: string
  imagesJson?: string
  postType?: string
  domain?: string
}

export function listPosts(params?: PostQuery) {
  return get<PostResponse[]>('/api/posts', params)
}

export function getPost(id: number) {
  return get<PostResponse>(`/api/posts/${id}`)
}

export function createPost(req: PostCreateRequest) {
  return post<number>('/api/posts', req)
}

export function togglePostLike(postId: number) {
  return post<{ liked: boolean; likeCount: number }>(`/api/posts/${postId}/like`)
}

export function listPostComments(postId: number) {
  return get<PostCommentResponse[]>(`/api/posts/${postId}/comments`)
}

export function createPostComment(postId: number, content: string) {
  return post<number>(`/api/posts/${postId}/comments`, { content })
}

export function togglePostCollect(postId: number) {
  return post<boolean>(`/api/posts/${postId}/collect`)
}

export function updatePost(postId: number, req: Partial<PostCreateRequest>) {
  return put<void>(`/api/posts/${postId}`, req)
}

export function deletePost(postId: number) {
  return del<void>(`/api/posts/${postId}`)
}
