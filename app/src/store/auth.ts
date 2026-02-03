import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/** 用户信息（对应后端 MeResponse） */
export interface User {
  userId: number
  userName: string
  nickName?: string
  phonenumber?: string
  avatar?: string
  companyId?: number
  companyName?: string
  isBuyer?: number
  isSeller?: number
  userType?: string
  position?: string
  bio?: string
  gender?: number
  isAdmin?: boolean
}

const TOKEN_KEY = 'wogu_token'
const USER_KEY = 'wogu_user'

/** API 基础地址 */
function getBaseUrl(): string {
  // #ifdef H5
  return '' // H5 模式使用相对路径（由 proxy 代理）
  // #endif
  // #ifdef APP-PLUS
  return 'http://localhost:8080' // TODO: 配置生产环境地址
  // #endif
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>('')
  const user = ref<User | null>(null)

  const isLoggedIn = computed(() => !!token.value && !!user.value)

  /** 恢复本地存储的登录状态 */
  function restoreSession() {
    try {
      token.value = uni.getStorageSync(TOKEN_KEY) || ''
      const userJson = uni.getStorageSync(USER_KEY)
      if (userJson) {
        user.value = JSON.parse(userJson)
      }
    } catch {
      // storage read failed
    }

    if (token.value) {
      checkSession()
    }
  }

  /** 验证当前 token 是否有效，获取最新用户信息 */
  async function checkSession() {
    try {
      const res = await uni.request({
        url: `${getBaseUrl()}/api/auth/me`,
        header: { Authorization: `Bearer ${token.value}` },
      })
      const data = res.data as any
      if (data?.code === 0 && data?.data) {
        saveUser(data.data)
      } else {
        clearAuth()
      }
    } catch {
      // 网络错误，保持当前状态
    }
  }

  /** 发送短信验证码 */
  async function sendSmsCode(phone: string): Promise<boolean> {
    try {
      const res = await uni.request({
        url: `${getBaseUrl()}/api/auth/sms/send`,
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        data: { phone, type: 2 },
      })
      const data = res.data as any
      return data?.code === 0
    } catch {
      return false
    }
  }

  /** 短信登录 */
  async function loginBySms(phone: string, smsCode: string): Promise<boolean> {
    try {
      const res = await uni.request({
        url: `${getBaseUrl()}/api/auth/login/sms`,
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        data: { phone, smsCode },
      })
      const data = res.data as any
      if (data?.code === 0 && data?.data?.token) {
        token.value = data.data.token
        uni.setStorageSync(TOKEN_KEY, token.value)
        // 登录成功后获取用户信息
        await checkSession()
        return true
      }
      return false
    } catch {
      return false
    }
  }

  /** 保存用户信息到本地 */
  function saveUser(userVal: User) {
    user.value = userVal
    uni.setStorageSync(USER_KEY, JSON.stringify(userVal))
  }

  /** 清除登录状态 */
  function clearAuth() {
    token.value = ''
    user.value = null
    uni.removeStorageSync(TOKEN_KEY)
    uni.removeStorageSync(USER_KEY)
  }

  /** 退出登录 */
  async function logout() {
    try {
      await uni.request({
        url: `${getBaseUrl()}/api/auth/logout`,
        method: 'POST',
        header: { Authorization: `Bearer ${token.value}` },
      })
    } catch {
      // ignore
    }
    clearAuth()
    uni.reLaunch({ url: '/pages/home/index' })
  }

  return {
    token,
    user,
    isLoggedIn,
    restoreSession,
    checkSession,
    sendSmsCode,
    loginBySms,
    saveUser,
    clearAuth,
    logout,
  }
})
