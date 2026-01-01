import axios from 'axios'
import { useAuthStore } from '../store/auth'

export const http = axios.create({
  baseURL: '',
  timeout: 15000,
  // 为 HttpOnly Cookie 会话准备（同域也无害；跨域部署时必须）
  withCredentials: true
})

export interface Result<T> {
  code: number
  message: string
  data?: T
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



