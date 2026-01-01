import { useAuthStore } from '../store/auth'
import { useUiStore } from '../store/ui'

/**
 * 用于“前台页面/按钮点击”场景：
 * - 已登录：直接返回 true
 * - 未登录：弹出登录注册对话框，并记录跳转目标；返回 false
 */
export function requireAuth(targetPath?: string, query?: Record<string, any>) {
  const auth = useAuthStore()
  const ui = useUiStore()
  if (auth.me) return true
  ui.openAuthDialog('login', targetPath ? { path: targetPath, query } : undefined)
  return false
}


