import axios from 'axios'

// ==================== 接口定义 ====================

export interface AdminDashboardResponse {
  totalUsers: number
  todayNewUsers: number
  totalCompanies: number
  activeSupplyCount: number
  activeRequirementCount: number
  totalContracts: number
  totalPosts: number
  todayLoginCount: number
}

export interface AdminUserResponse {
  userId: number
  userName: string
  nickName: string
  phonenumber: string
  companyId: number | null
  companyName: string | null
  isBuyer: number
  isSeller: number
  userType: string
  isAdmin: number
  isDeleted: number
  createTime: string
}

export interface AdminCompanyResponse {
  id: number
  companyName: string
  companyType: string
  ownerUserId: number
  ownerName: string
  contacts: string
  phone: string
  province: string
  city: string
  district: string
  verifiedStatus: number
  createTime: string
}

export interface AdminListingResponse {
  id: number
  categoryName: string
  companyId: number
  companyName: string
  price: number | null
  priceUnit: string | null
  quantity: number | null
  quantityUnit: string | null
  status: number
  createTime: string
}

export interface AdminPostResponse {
  id: number
  title: string
  authorId: number
  authorName: string
  likeCount: number
  commentCount: number
  isDeleted: number
  createTime: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}

// ==================== 仪表盘 ====================

export function getAdminDashboard() {
  return axios.get<{ code: number; message: string; data: AdminDashboardResponse }>('/api/admin/dashboard')
    .then(r => r.data)
}

// ==================== 用户管理 ====================

export function listAdminUsers(params: { keyword?: string; page?: number; size?: number }) {
  return axios.get<{ code: number; message: string; data: PageResult<AdminUserResponse> }>('/api/admin/users', { params })
    .then(r => r.data)
}

export function toggleAdminFlag(userId: number) {
  return axios.put<{ code: number; message: string }>(`/api/admin/users/${userId}/toggle-admin`)
    .then(r => r.data)
}

export function toggleUserStatus(userId: number) {
  return axios.put<{ code: number; message: string }>(`/api/admin/users/${userId}/toggle-status`)
    .then(r => r.data)
}

// ==================== 企业管理 ====================

export function listAdminCompanies(params: { keyword?: string; status?: number; page?: number; size?: number }) {
  return axios.get<{ code: number; message: string; data: PageResult<AdminCompanyResponse> }>('/api/admin/companies', { params })
    .then(r => r.data)
}

export function verifyCompany(companyId: number) {
  return axios.put<{ code: number; message: string }>(`/api/admin/companies/${companyId}/verify`)
    .then(r => r.data)
}

export function rejectCompany(companyId: number) {
  return axios.put<{ code: number; message: string }>(`/api/admin/companies/${companyId}/reject`)
    .then(r => r.data)
}

// ==================== 信息审核（供应） ====================

export function listAdminSupplies(params: { keyword?: string; page?: number; size?: number }) {
  return axios.get<{ code: number; message: string; data: PageResult<AdminListingResponse> }>('/api/admin/supplies', { params })
    .then(r => r.data)
}

export function takedownSupply(id: number) {
  return axios.put<{ code: number; message: string }>(`/api/admin/supplies/${id}/takedown`)
    .then(r => r.data)
}

export function restoreSupply(id: number) {
  return axios.put<{ code: number; message: string }>(`/api/admin/supplies/${id}/restore`)
    .then(r => r.data)
}

// ==================== 信息审核（采购） ====================

export function listAdminRequirements(params: { keyword?: string; page?: number; size?: number }) {
  return axios.get<{ code: number; message: string; data: PageResult<AdminListingResponse> }>('/api/admin/requirements', { params })
    .then(r => r.data)
}

export function takedownRequirement(id: number) {
  return axios.put<{ code: number; message: string }>(`/api/admin/requirements/${id}/takedown`)
    .then(r => r.data)
}

export function restoreRequirement(id: number) {
  return axios.put<{ code: number; message: string }>(`/api/admin/requirements/${id}/restore`)
    .then(r => r.data)
}

// ==================== 话题管理 ====================

export function listAdminPosts(params: { keyword?: string; page?: number; size?: number }) {
  return axios.get<{ code: number; message: string; data: PageResult<AdminPostResponse> }>('/api/admin/posts', { params })
    .then(r => r.data)
}

export function deleteAdminPost(id: number) {
  return axios.delete<{ code: number; message: string }>(`/api/admin/posts/${id}`)
    .then(r => r.data)
}
