import axios from 'axios'
import { useAuthStore } from '../store/auth'

export const http = axios.create({
  baseURL: '',
  timeout: 15000
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



