import { http, type Result } from './http'

export interface VehicleInfo {
  driverName: string
  driverIdCard: string
  plateNumber: string
  driverPhone: string
}

export interface VehicleCreateRequest {
  driverName: string
  driverIdCard: string
  plateNumber: string
  driverPhone: string
  vehicleType?: string
  remark?: string
}

export interface VehicleResponse {
  id: number
  companyId: number
  driverName: string
  driverIdCard: string
  plateNumber: string
  driverPhone: string
  vehicleType?: string
  isDefault: boolean
  remark?: string
  createTime: string
}

/**
 * 获取当前公司的车辆列表
 */
export async function listVehicles() {
  const { data } = await http.get<Result<VehicleResponse[]>>('/api/vehicles')
  return data
}

/**
 * 获取车辆详情
 */
export async function getVehicle(id: number) {
  const { data } = await http.get<Result<VehicleResponse>>(`/api/vehicles/${id}`)
  return data
}

/**
 * 添加常用车辆
 */
export async function createVehicle(req: VehicleCreateRequest) {
  const { data } = await http.post<Result<number>>('/api/vehicles', req)
  return data
}

/**
 * 修改车辆信息
 */
export async function updateVehicle(id: number, req: VehicleCreateRequest) {
  const { data } = await http.put<Result<void>>(`/api/vehicles/${id}`, req)
  return data
}

/**
 * 删除车辆
 */
export async function deleteVehicle(id: number) {
  const { data } = await http.delete<Result<void>>(`/api/vehicles/${id}`)
  return data
}

/**
 * 设为默认车辆
 */
export async function setDefaultVehicle(id: number) {
  const { data } = await http.post<Result<void>>(`/api/vehicles/${id}/default`)
  return data
}

