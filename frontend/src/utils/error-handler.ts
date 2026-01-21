import { ElMessage, ElNotification } from 'element-plus'
import type { AxiosError } from 'axios'

export interface ApiError {
  code?: number | string
  message: string
  details?: Record<string, any>
  stack?: string
}

export function isApiError(error: any): error is ApiError {
  return error && typeof error === 'object' && 'message' in error
}

export function isAxiosError(error: any): error is AxiosError {
  return error && error.isAxiosError === true
}

export function handleApiError(error: any): void {
  console.error('[API Error]', error)

  let message = '操作失败，请稍后重试'
  let type: 'error' | 'warning' = 'error'

  if (isAxiosError(error)) {
    if (error.response) {
      const status = error.response.status
      const data = error.response.data as ApiError

      switch (status) {
        case 400:
          message = data?.message || '请求参数错误'
          type = 'warning'
          break
        case 401:
          message = '登录已过期，请重新登录'
          window.location.href = '/login'
          return
        case 403:
          message = '没有权限执行此操作'
          type = 'warning'
          break
        case 404:
          message = '请求的资源不存在'
          type = 'warning'
          break
        case 429:
          message = '请求过于频繁，请稍后再试'
          type = 'warning'
          break
        case 500:
          message = data?.message || '服务器错误，请联系管理员'
          break
        case 502:
        case 503:
          message = '服务暂时不可用，请稍后再试'
          break
        default:
          message = data?.message || `请求失败 (${status})`
      }
    } else if (error.request) {
      message = '网络错误，请检查网络连接'
    } else {
      message = error.message || '请求配置错误'
    }
  } else if (isApiError(error)) {
    message = error.message
  } else if (error instanceof Error) {
    message = error.message
  }

  ElMessage({
    message,
    type,
    duration: 3000,
    grouping: true
  })

  reportToMonitoring(error)
}

export function handleNetworkError(): void {
  ElNotification({
    title: '网络连接异常',
    message: '请检查网络连接后重试',
    type: 'error',
    duration: 5000,
    showClose: true
  })
}

export function handleValidationError(field: string, message: string): void {
  ElMessage({
    message: `${field}: ${message}`,
    type: 'warning',
    duration: 3000
  })
}

export function handleSuccess(message: string): void {
  ElMessage({
    message,
    type: 'success',
    duration: 2000
  })
}

function reportToMonitoring(error: any): void {
  if (import.meta.env.PROD) {
    const errorInfo = {
      message: error?.message || String(error),
      stack: error?.stack,
      url: window.location.href,
      timestamp: new Date().toISOString(),
      userAgent: navigator.userAgent
    }

    try {
      navigator.sendBeacon('/api/monitoring/error', JSON.stringify(errorInfo))
    } catch (e) {
      console.warn('[ErrorHandler] Failed to report error:', e)
    }
  }
}

export function createErrorHandler(context?: string) {
  return (error: any) => {
    if (context) {
      console.error(`[${context}]`, error)
    }
    handleApiError(error)
  }
}

export class ErrorHandler {
  static init(): void {
    window.addEventListener('error', (event) => {
      console.error('[Global Error]', event.error)
      handleApiError(event.error)
    })

    window.addEventListener('unhandledrejection', (event) => {
      console.error('[Unhandled Rejection]', event.reason)
      handleApiError(event.reason)
    })
  }

  static handle(error: any): void {
    handleApiError(error)
  }

  static network(): void {
    handleNetworkError()
  }

  static validation(field: string, message: string): void {
    handleValidationError(field, message)
  }

  static success(message: string): void {
    handleSuccess(message)
  }

  static create(context?: string) {
    return createErrorHandler(context)
  }
}

export default ErrorHandler
