import { reactive } from 'vue'

export interface ToastItem {
  id: number
  type: 'success' | 'error' | 'warning' | 'info'
  message: string
  persistent: boolean
}

export interface ToastOptions {
  duration?: number
  persistent?: boolean
}

let nextId = 0

// Global reactive toast list – shared across all consumers
const toasts = reactive<ToastItem[]>([])

function addToast(
  type: ToastItem['type'],
  message: string,
  options: ToastOptions = {}
): void {
  const { duration = 3000, persistent = false } = options
  const id = nextId++

  toasts.push({ id, type, message, persistent })

  if (!persistent) {
    setTimeout(() => removeToast(id), duration)
  }
}

function removeToast(id: number): void {
  const idx = toasts.findIndex((t) => t.id === id)
  if (idx !== -1) toasts.splice(idx, 1)
}

/** composable for use inside Vue components */
export function useToast() {
  return {
    toasts,
    removeToast,
    success: (message: string, options?: ToastOptions) =>
      addToast('success', message, options),
    error: (message: string, options?: ToastOptions) =>
      addToast('error', message, options),
    warning: (message: string, options?: ToastOptions) =>
      addToast('warning', message, options),
    info: (message: string, options?: ToastOptions) =>
      addToast('info', message, options),
  }
}

/** Global singleton for non-component contexts (e.g. http interceptors) */
export const showToast = {
  success: (message: string, options?: ToastOptions) =>
    addToast('success', message, options),
  error: (message: string, options?: ToastOptions) =>
    addToast('error', message, options),
  warning: (message: string, options?: ToastOptions) =>
    addToast('warning', message, options),
  info: (message: string, options?: ToastOptions) =>
    addToast('info', message, options),
}
