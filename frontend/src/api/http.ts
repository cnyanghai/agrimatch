import axios from 'axios'
import { Capacitor } from '@capacitor/core'
import { useAuthStore } from '../store/auth'
import { showToast } from '@/composables/useToast'

const baseURL = Capacitor.isNativePlatform()
  ? 'https://www.wogucloud.com'
  : ''

export const http = axios.create({
  baseURL,
  timeout: 15000,
  // 为 HttpOnly Cookie 会话准备（同域也无害；跨域部署时必须）
  withCredentials: true
})

export interface Result<T> {
  code: number
  message: string
  data?: T
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  size: number
}

http.interceptors.request.use((config) => {
  try {
    const auth = useAuthStore()
    if (auth.token) {
      config.headers = config.headers ?? {}
      config.headers.Authorization = `Bearer ${auth.token}`
    }
  } catch {
    // ignore pinia not ready
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    // 401未认证：静默处理，由路由守卫决定是否跳转登录页
    if (error.response?.status === 401) {
      return Promise.reject(error)
    }
    const msg = error.response?.data?.message || error.message || '请求失败'
    showToast.error(msg)
    return Promise.reject(error)
  }
)



