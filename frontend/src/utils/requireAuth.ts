import { useAuthStore } from '../store/auth'
import { useRouter } from 'vue-router'

/**
 * For "public page / button click" scenarios:
 * - Logged in: returns true
 * - Not logged in: redirects to /login with optional redirect target; returns false
 */
export function requireAuth(targetPath?: string, query?: Record<string, any>) {
  const auth = useAuthStore()
  if (auth.me) return true
  const router = useRouter()
  const redirect = targetPath
    ? targetPath + (query ? '?' + new URLSearchParams(query as any).toString() : '')
    : undefined
  router.push({ path: '/login', query: redirect ? { redirect } : undefined })
  return false
}
