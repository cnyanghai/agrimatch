import { defineStore } from 'pinia'
import { http, type Result } from '../api/http'

export interface LoginResponse {
  token: string
}

export interface MeResponse {
  userId: number
  userName: string
  nickName?: string
  phonenumber?: string
  wechat?: string
  position?: string
  birthDate?: string // YYYY-MM-DD
  gender?: number    // 1-男 2-女
  bio?: string
  companyId?: number
  isBuyer?: number
  isSeller?: number
  userType?: string
  createTime?: string
  lastLoginTime?: string
}

export interface RegisterRequest {
  phone: string
  smsCode: string
  password: string
  // 系统不强制昵称：前端可用联系人姓名/手机号代替
  nickName?: string
  userType: 'buyer' | 'seller'
  companyName?: string
  companyType?: 'feed_factory' | 'trader' | 'grain_depot' | 'processor' | 'logistics' | 'other'
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('agrimatch_token') || '',
    me: null as MeResponse | null
  }),
  getters: {
    // 是否为采购商
    isBuyer: (state) => state.me?.isBuyer === 1,
    // 是否为供应商
    isSeller: (state) => state.me?.isSeller === 1,
    // 用户类型标签
    userTypeLabel: (state) => {
      if (state.me?.isBuyer === 1) return '采购商'
      if (state.me?.isSeller === 1) return '供应商'
      return '未设置'
    },
    // 是否已完善资料
    isProfileComplete: (state) => {
      if (!state.me) return false
      return !!(state.me.nickName && state.me.phonenumber && state.me.companyId)
    }
  },
  actions: {
    setToken(token: string) {
      this.token = token
      localStorage.setItem('agrimatch_token', token)
    },
    clear() {
      this.token = ''
      this.me = null
      localStorage.removeItem('agrimatch_token')
    },

    // HttpOnly Cookie 会话退出
    async logout() {
      try {
        await http.post<Result<void>>('/api/auth/logout')
      } catch {
        // ignore
      } finally {
        this.clear()
      }
    },
    
    // 发送短信验证码
    async sendSmsCode(phone: string, type: number) {
      // type: 1-注册, 2-登录, 3-找回密码
      const { data } = await http.post<Result<void>>('/api/auth/sms/send', { phone, type })
      if (data.code !== 0) throw new Error(data.message)
    },
    
    // 密码登录（兼容旧方式，使用手机号作为用户名）
    async loginByPassword(phone: string, password: string) {
      const { data } = await http.post<Result<LoginResponse>>('/api/auth/login', { 
        userName: phone, 
        password 
      })
      if (data.code !== 0 || !data.data?.token) throw new Error(data.message)
      this.setToken(data.data.token)
    },
    
    // 验证码登录
    async loginBySms(phone: string, smsCode: string) {
      const { data } = await http.post<Result<LoginResponse>>('/api/auth/login/sms', { 
        phone, 
        smsCode 
      })
      if (data.code !== 0 || !data.data?.token) throw new Error(data.message)
      this.setToken(data.data.token)
    },
    
    // 旧版登录方法（兼容）
    async login(userName: string, password: string) {
      const { data } = await http.post<Result<LoginResponse>>('/api/auth/login', { userName, password })
      if (data.code !== 0 || !data.data?.token) throw new Error(data.message)
      this.setToken(data.data.token)
    },
    
    // 新版注册
    async register(req: RegisterRequest) {
      const { data } = await http.post<Result<LoginResponse>>('/api/auth/register', {
        userName: req.phone,
        phonenumber: req.phone,
        password: req.password,
        nickName: req.nickName || req.phone,
        companyName: req.companyName,
        companyType: req.companyType,
        smsCode: req.smsCode,
        isBuyer: req.userType === 'buyer' ? 1 : 0,
        isSeller: req.userType === 'seller' ? 1 : 0
      })
      if (data.code !== 0 || !data.data?.token) throw new Error(data.message)
      this.setToken(data.data.token)
    },
    
    // 获取当前用户信息
    async fetchMe() {
      const { data } = await http.get<Result<MeResponse>>('/api/auth/me')
      if (data.code !== 0) throw new Error(data.message)
      this.me = data.data ?? null
    },
    
    // 更新用户身份
    async updateUserType(userType: 'buyer' | 'seller') {
      const { data } = await http.put<Result<void>>('/api/users/me/roles', {
        isBuyer: userType === 'buyer' ? 1 : 0,
        isSeller: userType === 'seller' ? 1 : 0
      })
      if (data.code !== 0) throw new Error(data.message)
      // 刷新用户信息
      await this.fetchMe()
    }
  }
})
