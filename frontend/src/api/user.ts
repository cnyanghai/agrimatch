import { http, type Result } from './http'

export interface UserCreateRequest {
  userName: string
  nickName: string
  phonenumber?: string
  wechat?: string
  companyId?: number
  isBuyer?: number
  isSeller?: number
  payInfoJson?: string
}

export interface UserUpdateRequest {
  nickName?: string
  realName?: string        // 用户姓名
  phonenumber?: string
  position?: string        // 公司职务
  birthDate?: string       // 出生年月
  gender?: number          // 性别 (1-男, 2-女)
  bio?: string             // 个人介绍
  companyId?: number
  payInfoJson?: string
}

export interface UserRoleUpdateRequest {
  isBuyer: number
  isSeller: number
}

export interface UserResponse {
  userId: number
  userName: string
  nickName: string
  realName?: string        // 用户姓名
  phonenumber?: string
  position?: string        // 公司职务
  birthDate?: string       // 出生年月
  gender?: number          // 性别 (1-男, 2-女)
  bio?: string             // 个人介绍
  companyId?: number
  isBuyer?: number
  isSeller?: number
  userType?: string
  payInfoJson?: string
  createTime?: string
  updateTime?: string
}

export interface UserBriefResponse {
  userId: number
  userName: string
  nickName: string
  companyId?: number
  companyName?: string
}

export async function createUser(req: UserCreateRequest) {
  const { data } = await http.post<Result<number>>('/api/users', req)
  return data
}

export async function getUser(id: number) {
  const { data } = await http.get<Result<UserResponse>>(`/api/users/${id}`)
  return data
}

export async function getMe() {
  const { data } = await http.get<Result<UserResponse>>('/api/users/me')
  return data
}

export async function updateUser(id: number, req: UserUpdateRequest) {
  const { data } = await http.put<Result<void>>(`/api/users/${id}`, req)
  return data
}

export async function updateMe(req: UserUpdateRequest) {
  const { data } = await http.put<Result<void>>('/api/users/me', req)
  return data
}

export async function updateUserRoles(id: number, req: UserRoleUpdateRequest) {
  const { data } = await http.put<Result<void>>(`/api/users/${id}/roles`, req)
  return data
}

export async function updateMyRoles(req: UserRoleUpdateRequest) {
  const { data } = await http.put<Result<void>>('/api/users/me/roles', req)
  return data
}

export async function searchUsers(keyword: string, limit = 20) {
  const { data } = await http.get<Result<UserBriefResponse[]>>('/api/users/search', {
    params: { keyword, limit }
  })
  return data
}


