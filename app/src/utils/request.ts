/**
 * uni.request 封装
 * - 自动附加 Bearer token
 * - 401 拦截 → 跳转登录
 * - 统一错误处理
 */
import { useAuthStore } from '../store/auth'

/** API 基础地址 */
function getBaseUrl(): string {
  // #ifdef H5
  return ''
  // #endif
  // #ifdef APP-PLUS
  return 'http://172.28.0.135:8080'
  // #endif
}

/** 统一响应格式 */
export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
}

/** 请求配置 */
export interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: Record<string, string>
  /** 是否不自动处理错误 */
  silent?: boolean
}

/** 发起请求 */
export function request<T = any>(options: RequestOptions): Promise<T> {
  const authStore = useAuthStore()
  const { url, method = 'GET', data, header = {}, silent = false } = options

  // 自动附加 token
  if (authStore.token) {
    header['Authorization'] = `Bearer ${authStore.token}`
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${getBaseUrl()}${url}`,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...header,
      },
      success(res) {
        const statusCode = res.statusCode
        const result = res.data as ApiResult<T>

        if (statusCode === 401) {
          // Token 过期，清除登录并跳转
          authStore.clearAuth()
          uni.navigateTo({ url: '/pages/auth/login' })
          reject(new Error('登录已过期，请重新登录'))
          return
        }

        if (statusCode >= 200 && statusCode < 300) {
          if (result.code === 0) {
            resolve(result.data)
          } else {
            if (!silent) {
              uni.showToast({ title: result.message || '操作失败', icon: 'none' })
            }
            reject(new Error(result.message))
          }
        } else {
          if (!silent) {
            uni.showToast({ title: `请求失败 (${statusCode})`, icon: 'none' })
          }
          reject(new Error(`HTTP ${statusCode}`))
        }
      },
      fail(err) {
        if (!silent) {
          uni.showToast({ title: '网络连接失败', icon: 'none' })
        }
        reject(err)
      },
    })
  })
}

/** GET 请求 */
export function get<T = any>(url: string, data?: any, options?: Partial<RequestOptions>) {
  return request<T>({ url, method: 'GET', data, ...options })
}

/** POST 请求 */
export function post<T = any>(url: string, data?: any, options?: Partial<RequestOptions>) {
  return request<T>({ url, method: 'POST', data, ...options })
}

/** PUT 请求 */
export function put<T = any>(url: string, data?: any, options?: Partial<RequestOptions>) {
  return request<T>({ url, method: 'PUT', data, ...options })
}

/** DELETE 请求 */
export function del<T = any>(url: string, data?: any, options?: Partial<RequestOptions>) {
  return request<T>({ url, method: 'DELETE', data, ...options })
}

/** 上传文件 */
export function uploadFile(filePath: string, url: string, name = 'file'): Promise<any> {
  const authStore = useAuthStore()

  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${getBaseUrl()}${url}`,
      filePath,
      name,
      header: authStore.token
        ? { Authorization: `Bearer ${authStore.token}` }
        : {},
      success(res) {
        if (res.statusCode === 200) {
          try {
            const data = JSON.parse(res.data)
            if (data.code === 0) {
              resolve(data.data)
            } else {
              reject(new Error(data.message))
            }
          } catch {
            reject(new Error('解析响应失败'))
          }
        } else {
          reject(new Error(`上传失败 (${res.statusCode})`))
        }
      },
      fail(err) {
        reject(err)
      },
    })
  })
}
