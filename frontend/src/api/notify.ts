import { http, type Result } from './http'

export interface NotifyResponse {
  id: number
  type: string
  title: string
  content?: string
  link?: string
  read: boolean
  createTime?: string
}

export async function listMyNotify() {
  const { data } = await http.get<Result<NotifyResponse[]>>('/api/notify/my')
  return data
}

export async function markNotifyRead(id: number) {
  const { data } = await http.post<Result<void>>(`/api/notify/my/read/${id}`)
  return data
}

export async function markNotifyReadAll() {
  const { data } = await http.post<Result<void>>('/api/notify/my/read-all')
  return data
}


