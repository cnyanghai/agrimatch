import { http, type Result } from './http'

/**
 * 发送短信验证码
 * @param phone 手机号
 * @param type 验证码类型 (1=注册, 2=登录, 3=身份验证, 4=合同签署)
 */
export async function sendSmsCode(phone: string, type: number): Promise<Result<void>> {
  const res = await http.post<Result<void>>('/api/auth/sms/send', { phone, type })
  return res.data
}
