import { get, post } from '../utils/request'

export interface NotifyResponse {
  id: number
  type: string
  title: string
  content?: string
  link?: string
  read: boolean
  createTime?: string
}

/** 获取我的通知列表 */
export function listMyNotifications() {
  return get<NotifyResponse[]>('/api/notify/my')
}

/** 标记单条通知已读 */
export function markNotificationRead(id: number) {
  return post<void>(`/api/notify/my/read/${id}`)
}

/** 标记全部已读 */
export function markAllNotificationsRead() {
  return post<void>('/api/notify/my/read-all')
}
