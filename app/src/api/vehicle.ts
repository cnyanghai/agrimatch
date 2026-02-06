import { get, post, put, del } from '../utils/request'

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

/** List vehicles for current company */
export function listVehicles() {
  return get<VehicleResponse[]>('/api/vehicles')
}

/** Get vehicle detail */
export function getVehicle(id: number) {
  return get<VehicleResponse>(`/api/vehicles/${id}`)
}

/** Create a vehicle */
export function createVehicle(req: VehicleCreateRequest) {
  return post<number>('/api/vehicles', req)
}

/** Update a vehicle */
export function updateVehicle(id: number, req: VehicleCreateRequest) {
  return put<void>(`/api/vehicles/${id}`, req)
}

/** Delete a vehicle */
export function deleteVehicle(id: number) {
  return del<void>(`/api/vehicles/${id}`)
}

/** Set as default vehicle */
export function setDefaultVehicle(id: number) {
  return post<void>(`/api/vehicles/${id}/default`)
}
