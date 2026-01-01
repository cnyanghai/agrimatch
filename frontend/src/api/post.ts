import { http, type Result } from './http'

export interface PostCreateRequest {
  title: string
  content?: string
  imagesJson?: string
}

export interface PostResponse {
  id: number
  companyId?: number
  userId: number
  companyName?: string
  userName?: string
  nickName?: string
  title: string
  content?: string
  imagesJson?: string
  createTime?: string
  likeCount?: number
  commentCount?: number
  likedByMe?: boolean
}

export interface PostQuery {
  companyId?: number
  userId?: number
  keyword?: string
  orderBy?: string
  order?: string
  recentDays?: number
  limit?: number
}

export async function createPost(req: PostCreateRequest) {
  const { data } = await http.post<Result<number>>('/api/posts', req)
  return data
}

export async function getPost(id: number) {
  const { data } = await http.get<Result<PostResponse>>(`/api/posts/${id}`)
  return data
}

export async function listPosts(q: PostQuery) {
  const { data } = await http.get<Result<PostResponse[]>>('/api/posts', { params: q })
  return data
}

export async function deletePost(id: number) {
  const { data } = await http.delete<Result<void>>(`/api/posts/${id}`)
  return data
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
  createTime?: string
}

export async function togglePostLike(postId: number) {
  const { data } = await http.post<Result<PostLikeToggleResponse>>(`/api/posts/${postId}/like`)
  return data
}

export async function listPostComments(postId: number) {
  const { data } = await http.get<Result<PostCommentResponse[]>>(`/api/posts/${postId}/comments`)
  return data
}

export async function createPostComment(postId: number, content: string) {
  const { data } = await http.post<Result<number>>(`/api/posts/${postId}/comments`, { content })
  return data
}


