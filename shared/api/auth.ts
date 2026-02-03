/**
 * Auth module - Types and API path constants.
 */

// ==================== Types ====================

export interface LoginResponse {
  token: string
}

export interface MeResponse {
  userId: number
  userName: string
  nickName?: string
  realName?: string
  phonenumber?: string
  wechat?: string
  position?: string
  birthDate?: string       // YYYY-MM-DD
  gender?: number          // 1-男 2-女
  bio?: string
  avatar?: string          // 个人头像URL
  companyId?: number
  isBuyer?: number
  isSeller?: number
  userType?: string
  isAdmin?: boolean
  createTime?: string
  lastLoginTime?: string
}

export interface RegisterRequest {
  phone: string
  password: string
  captchaKey: string
  captchaCode: string
  nickName?: string
  userType: 'buyer' | 'seller'
  companyName?: string
  companyType?: 'feed_factory' | 'trader' | 'grain_depot' | 'processor' | 'logistics' | 'other'
}

export interface CaptchaResponse {
  captchaKey: string
  captchaImage: string
}

// ==================== User Types ====================

export interface UserCreateRequest {
  userName: string
  nickName: string
  phonenumber?: string
  wechat?: string
  companyId?: number
  isBuyer?: number
  isSeller?: number
  payInfoJson?: string
}

export interface UserUpdateRequest {
  nickName?: string
  realName?: string
  phonenumber?: string
  position?: string
  birthDate?: string
  gender?: number
  bio?: string
  avatar?: string
  companyId?: number
  payInfoJson?: string
}

export interface UserRoleUpdateRequest {
  isBuyer: number
  isSeller: number
}

export interface UserResponse {
  userId: number
  userName: string
  nickName: string
  realName?: string
  phonenumber?: string
  position?: string
  birthDate?: string
  gender?: number
  bio?: string
  avatar?: string
  companyId?: number
  isBuyer?: number
  isSeller?: number
  userType?: string
  payInfoJson?: string
  createTime?: string
  updateTime?: string
  companyName?: string
}

export interface UserBriefResponse {
  userId: number
  userName: string
  nickName: string
  companyId?: number
  companyName?: string
}

export interface LoginLogResponse {
  infoId: number
  userName: string
  ipaddr: string
  loginLocation: string
  browser: string
  os: string
  status: string
  msg: string
  loginTime: string
}

// ==================== API Path Constants ====================

export const AUTH_API = {
  /** POST - Login with password */
  LOGIN: '/api/auth/login',
  /** POST - Register new account */
  REGISTER: '/api/auth/register',
  /** POST - Logout */
  LOGOUT: '/api/auth/logout',
  /** GET - Get current user info */
  ME: '/api/auth/me',
  /** GET - Get captcha image */
  CAPTCHA: '/api/auth/captcha',
  /** POST - Send SMS verification code */
  SMS_SEND: '/api/auth/sms/send',
  /** GET - Get site configuration */
  CONFIG: '/api/config',
} as const

export const USER_API = {
  /** POST - Create user */
  CREATE: '/api/users',
  /** GET - Get user by ID: /api/users/:id */
  GET_BY_ID: (id: number) => `/api/users/${id}`,
  /** GET - Get current user */
  ME: '/api/users/me',
  /** PUT - Update user by ID: /api/users/:id */
  UPDATE: (id: number) => `/api/users/${id}`,
  /** PUT - Update current user */
  UPDATE_ME: '/api/users/me',
  /** PUT - Update user roles: /api/users/:id/roles */
  UPDATE_ROLES: (id: number) => `/api/users/${id}/roles`,
  /** PUT - Update current user roles */
  UPDATE_MY_ROLES: '/api/users/me/roles',
  /** GET - Search users */
  SEARCH: '/api/users/search',
  /** GET - Get login logs */
  LOGIN_LOGS: '/api/users/login-logs',
} as const
