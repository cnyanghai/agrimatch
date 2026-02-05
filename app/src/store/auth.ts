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
  return 'http://172.28.0.135:8080'
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

  /** 检查手机号是否已注册 */
  async function checkPhone(phone: string): Promise<boolean> {
    try {
      const res = await uni.request({
        url: `${getBaseUrl()}/api/auth/check-phone`,
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        data: { phone },
      })
      const data = res.data as any
      if (data?.code === 0 && data?.data) {
        return data.data.registered === true
      }
      return false
    } catch {
      return false
    }
  }

  /** 获取图形验证码 */
  async function getCaptcha(): Promise<{ captchaKey: string; captchaImage: string } | null> {
    return new Promise((resolve) => {
      uni.request({
        url: `${getBaseUrl()}/api/auth/captcha`,
        method: 'GET',
        success(res) {
          const data = res.data as any
          console.log('[Captcha] response status:', res.statusCode, 'code:', data?.code)
          if (data?.code === 0 && data?.data?.captchaImage) {
            resolve(data.data)
          } else {
            console.warn('[Captcha] unexpected response:', JSON.stringify(data).substring(0, 200))
            resolve(null)
          }
        },
        fail(err) {
          console.error('[Captcha] request failed:', err)
          resolve(null)
        },
      })
    })
  }

  /** 密码登录 */
  async function loginByPassword(userName: string, password: string, captchaKey = '', captchaCode = ''): Promise<{ ok: boolean; msg?: string }> {
    try {
      const res = await uni.request({
        url: `${getBaseUrl()}/api/auth/login`,
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        data: { userName, password, captchaKey, captchaCode },
      })
      const data = res.data as any
      if (data?.code === 0 && data?.data?.token) {
        token.value = data.data.token
        uni.setStorageSync(TOKEN_KEY, token.value)
        await checkSession()
        return { ok: true }
      }
      return { ok: false, msg: data?.message || '登录失败' }
    } catch {
      return { ok: false, msg: '网络错误' }
    }
  }

  /** 发送短信验证码 (type: 1=注册, 2=登录, 3=重置密码) */
  async function sendSmsCode(phone: string, type = 2): Promise<boolean> {
    try {
      const res = await uni.request({
        url: `${getBaseUrl()}/api/auth/sms/send`,
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        data: { phone, type },
      })
      const data = res.data as any
      return data?.code === 0
    } catch {
      return false
    }
  }

  /** 注册（手机号+密码，App端跳过图形验证码） */
  async function register(phone: string, password: string): Promise<{ ok: boolean; msg?: string }> {
    try {
      const res = await uni.request({
        url: `${getBaseUrl()}/api/auth/register`,
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        data: { userName: phone, phonenumber: phone, password, captchaKey: '', captchaCode: '' },
      })
      const data = res.data as any
      if (data?.code === 0 && data?.data?.token) {
        token.value = data.data.token
        uni.setStorageSync(TOKEN_KEY, token.value)
        await checkSession()
        return { ok: true }
      }
      return { ok: false, msg: data?.message || '注册失败' }
    } catch {
      return { ok: false, msg: '网络错误' }
    }
  }

  /** 重置密码 */
  async function resetPassword(phone: string, smsCode: string, newPassword: string): Promise<{ ok: boolean; msg?: string }> {
    try {
      const res = await uni.request({
        url: `${getBaseUrl()}/api/auth/reset-password`,
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        data: { phone, smsCode, newPassword },
      })
      const data = res.data as any
      if (data?.code === 0) {
        return { ok: true }
      }
      return { ok: false, msg: data?.message || '重置失败' }
    } catch {
      return { ok: false, msg: '网络错误' }
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
    checkPhone,
    getCaptcha,
    loginByPassword,
    sendSmsCode,
    loginBySms,
    register,
    resetPassword,
    saveUser,
    clearAuth,
    logout,
  }
})
