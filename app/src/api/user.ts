import { get, put } from '../utils/request'
import { uploadFile } from '../utils/request'

export interface UserResponse {
  userId: number
  userName: string
  nickName: string
  realName?: string
  phonenumber?: string
  position?: string
  birthDate?: string
  gender?: number
  bio?: string
  avatar?: string
  companyId?: number
  isBuyer?: number
  isSeller?: number
  companyName?: string
  createTime?: string
}

export function getUser(id: number) {
  return get<UserResponse>(`/api/users/${id}`)
}

export function getMe() {
  return get<UserResponse>('/api/users/me')
}

export interface UserUpdateRequest {
  nickName?: string
  position?: string
  gender?: number
  bio?: string
  avatar?: string
}

export function updateMe(req: UserUpdateRequest) {
  return put<void>('/api/users/me', req)
}
