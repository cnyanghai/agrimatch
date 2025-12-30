<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { Phone, Lock, Message, User, ShoppingCart, Box } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const loading = ref(false)
const active = ref<'login' | 'register'>((route.query.tab as 'login' | 'register') || 'login')
const loginMethod = ref<'password' | 'sms'>('password')
const countdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

// 登录表单
const loginForm = reactive({
  phone: '',
  password: '',
  smsCode: ''
})

// 注册表单
const regForm = reactive({
  phone: '',
  smsCode: '',
  password: '',
  confirmPassword: '',
  nickName: '',
  userType: '' as 'buyer' | 'seller' | ''
})

// 表单验证
const isLoginValid = computed(() => {
  if (loginMethod.value === 'password') {
    // 密码登录支持手机号或用户名
    return loginForm.phone.length >= 4 && loginForm.password.length >= 6
  } else {
    // 验证码登录必须是11位手机号
    return loginForm.phone.length === 11 && loginForm.smsCode.length === 6
  }
})

const isRegisterValid = computed(() => {
  return regForm.phone.length === 11 && 
         regForm.smsCode.length === 6 && 
         regForm.password.length >= 6 &&
         regForm.password === regForm.confirmPassword &&
         regForm.nickName.trim().length > 0 &&
         regForm.userType !== ''
})

// 发送验证码
async function sendSmsCode(type: 'login' | 'register') {
  const phone = type === 'login' ? loginForm.phone : regForm.phone
  if (phone.length !== 11) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  loading.value = true
  try {
    await auth.sendSmsCode(phone, type === 'login' ? 2 : 1)
    ElMessage.success('验证码已发送')
    // 开始倒计时
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        if (countdownTimer) clearInterval(countdownTimer)
      }
    }, 1000)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '发送失败')
  } finally {
    loading.value = false
  }
}

// 密码登录
async function onPasswordLogin() {
  if (!isLoginValid.value) return
  loading.value = true
  try {
    await auth.loginByPassword(loginForm.phone, loginForm.password)
    await auth.fetchMe()
    ElMessage.success('登录成功')
    await router.replace('/')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '登录失败')
  } finally {
    loading.value = false
  }
}

// 验证码登录
async function onSmsLogin() {
  if (!isLoginValid.value) return
  loading.value = true
  try {
    await auth.loginBySms(loginForm.phone, loginForm.smsCode)
    await auth.fetchMe()
    ElMessage.success('登录成功')
    await router.replace('/')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '登录失败')
  } finally {
    loading.value = false
  }
}

// 注册
async function onRegister() {
  if (!isRegisterValid.value) return
  if (regForm.password !== regForm.confirmPassword) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  loading.value = true
  try {
    await auth.register({
      phone: regForm.phone,
      smsCode: regForm.smsCode,
      password: regForm.password,
      nickName: regForm.nickName,
      userType: regForm.userType as 'buyer' | 'seller'
    })
    await auth.fetchMe()
    ElMessage.success('注册成功')
    await router.replace('/')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center p-4 bg-gradient-to-br from-blue-50 via-white to-orange-50">
    <!-- 背景装饰 -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -right-40 w-80 h-80 bg-blue-100 rounded-full opacity-50 blur-3xl"></div>
      <div class="absolute -bottom-40 -left-40 w-80 h-80 bg-orange-100 rounded-full opacity-50 blur-3xl"></div>
    </div>

    <div class="relative w-full max-w-md">
      <!-- Logo区域 -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-blue-600 to-blue-700 text-white font-bold text-2xl shadow-lg shadow-blue-200 mb-4">
          A
        </div>
        <h1 class="text-2xl font-bold text-gray-800">农汇通 AgriMatch</h1>
        <p class="text-gray-500 mt-1">农产品供需智能匹配平台</p>
      </div>

      <!-- 主卡片 -->
      <div class="bg-white rounded-2xl shadow-xl overflow-hidden">
        <!-- Tab切换 -->
        <div class="flex border-b border-gray-100">
          <button
            class="flex-1 py-4 text-center font-medium transition-all relative"
            :class="active === 'login' ? 'text-blue-600' : 'text-gray-400 hover:text-gray-600'"
            @click="active = 'login'"
          >
            登录
            <div v-if="active === 'login'" class="absolute bottom-0 left-1/2 -translate-x-1/2 w-12 h-0.5 bg-blue-600 rounded-full"></div>
          </button>
          <button
            class="flex-1 py-4 text-center font-medium transition-all relative"
            :class="active === 'register' ? 'text-blue-600' : 'text-gray-400 hover:text-gray-600'"
            @click="active = 'register'"
          >
            注册
            <div v-if="active === 'register'" class="absolute bottom-0 left-1/2 -translate-x-1/2 w-12 h-0.5 bg-blue-600 rounded-full"></div>
          </button>
        </div>

        <div class="p-6">
          <!-- 登录表单 -->
          <div v-if="active === 'login'" class="space-y-5">
            <!-- 登录方式切换 -->
            <div class="flex gap-2 p-1 bg-gray-100 rounded-lg">
              <button
                class="flex-1 py-2 px-4 rounded-md text-sm font-medium transition-all"
                :class="loginMethod === 'password' ? 'bg-white text-gray-800 shadow-sm' : 'text-gray-500'"
                @click="loginMethod = 'password'"
              >
                密码登录
              </button>
              <button
                class="flex-1 py-2 px-4 rounded-md text-sm font-medium transition-all"
                :class="loginMethod === 'sms' ? 'bg-white text-gray-800 shadow-sm' : 'text-gray-500'"
                @click="loginMethod = 'sms'"
              >
                验证码登录
              </button>
            </div>

            <!-- 手机号/用户名输入 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                {{ loginMethod === 'password' ? '手机号/用户名' : '手机号' }}
              </label>
              <el-input
                v-model="loginForm.phone"
                :placeholder="loginMethod === 'password' ? '请输入手机号或用户名' : '请输入手机号'"
                size="large"
                :maxlength="loginMethod === 'password' ? 50 : 11"
                :prefix-icon="Phone"
              />
            </div>

            <!-- 密码输入 -->
            <div v-if="loginMethod === 'password'">
              <label class="block text-sm font-medium text-gray-700 mb-2">密码</label>
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                show-password
                :prefix-icon="Lock"
                @keyup.enter="onPasswordLogin"
              />
            </div>

            <!-- 验证码输入 -->
            <div v-else>
              <label class="block text-sm font-medium text-gray-700 mb-2">验证码</label>
              <div class="flex gap-3">
                <el-input
                  v-model="loginForm.smsCode"
                  placeholder="请输入验证码"
                  size="large"
                  maxlength="6"
                  :prefix-icon="Message"
                  @keyup.enter="onSmsLogin"
                />
                <el-button
                  size="large"
                  :disabled="countdown > 0 || loginForm.phone.length !== 11"
                  @click="sendSmsCode('login')"
                >
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </el-button>
              </div>
            </div>

            <el-button
              type="primary"
              size="large"
              class="w-full !rounded-lg"
              :loading="loading"
              :disabled="!isLoginValid"
              @click="loginMethod === 'password' ? onPasswordLogin() : onSmsLogin()"
            >
              登录
            </el-button>

            <div class="text-center text-sm text-gray-500">
              还没有账号？<span class="text-blue-600 cursor-pointer hover:underline" @click="active = 'register'">立即注册</span>
            </div>
          </div>

          <!-- 注册表单 -->
          <div v-else class="space-y-5">
            <!-- 手机号 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">手机号 <span class="text-red-500">*</span></label>
              <el-input
                v-model="regForm.phone"
                placeholder="请输入手机号"
                size="large"
                maxlength="11"
                :prefix-icon="Phone"
              />
            </div>

            <!-- 验证码 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">验证码 <span class="text-red-500">*</span></label>
              <div class="flex gap-3">
                <el-input
                  v-model="regForm.smsCode"
                  placeholder="请输入验证码"
                  size="large"
                  maxlength="6"
                  :prefix-icon="Message"
                />
                <el-button
                  size="large"
                  :disabled="countdown > 0 || regForm.phone.length !== 11"
                  @click="sendSmsCode('register')"
                >
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </el-button>
              </div>
            </div>

            <!-- 昵称 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">昵称 <span class="text-red-500">*</span></label>
              <el-input
                v-model="regForm.nickName"
                placeholder="请输入您的昵称"
                size="large"
                :prefix-icon="User"
              />
            </div>

            <!-- 密码 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">设置密码 <span class="text-red-500">*</span></label>
              <el-input
                v-model="regForm.password"
                type="password"
                placeholder="请设置6位以上密码"
                size="large"
                show-password
                :prefix-icon="Lock"
              />
            </div>

            <!-- 确认密码 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">确认密码 <span class="text-red-500">*</span></label>
              <el-input
                v-model="regForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                size="large"
                show-password
                :prefix-icon="Lock"
              />
            </div>

            <!-- 身份选择 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-3">选择您的身份 <span class="text-red-500">*</span></label>
              <div class="grid grid-cols-2 gap-4">
                <div
                  class="relative p-4 border-2 rounded-xl cursor-pointer transition-all hover:border-blue-300"
                  :class="regForm.userType === 'buyer' ? 'border-blue-500 bg-blue-50' : 'border-gray-200'"
                  @click="regForm.userType = 'buyer'"
                >
                  <div class="flex flex-col items-center">
                    <div class="w-12 h-12 rounded-full flex items-center justify-center mb-2"
                         :class="regForm.userType === 'buyer' ? 'bg-blue-500 text-white' : 'bg-gray-100 text-gray-500'">
                      <el-icon :size="24"><ShoppingCart /></el-icon>
                    </div>
                    <div class="font-medium" :class="regForm.userType === 'buyer' ? 'text-blue-700' : 'text-gray-700'">采购商</div>
                    <div class="text-xs text-gray-500 mt-1 text-center">发布采购需求<br/>浏览供应信息</div>
                  </div>
                  <div v-if="regForm.userType === 'buyer'" class="absolute top-2 right-2 w-5 h-5 bg-blue-500 rounded-full flex items-center justify-center">
                    <el-icon class="text-white" :size="12"><Check /></el-icon>
                  </div>
                </div>

                <div
                  class="relative p-4 border-2 rounded-xl cursor-pointer transition-all hover:border-orange-300"
                  :class="regForm.userType === 'seller' ? 'border-orange-500 bg-orange-50' : 'border-gray-200'"
                  @click="regForm.userType = 'seller'"
                >
                  <div class="flex flex-col items-center">
                    <div class="w-12 h-12 rounded-full flex items-center justify-center mb-2"
                         :class="regForm.userType === 'seller' ? 'bg-orange-500 text-white' : 'bg-gray-100 text-gray-500'">
                      <el-icon :size="24"><Box /></el-icon>
                    </div>
                    <div class="font-medium" :class="regForm.userType === 'seller' ? 'text-orange-700' : 'text-gray-700'">供应商</div>
                    <div class="text-xs text-gray-500 mt-1 text-center">发布供应信息<br/>浏览采购需求</div>
                  </div>
                  <div v-if="regForm.userType === 'seller'" class="absolute top-2 right-2 w-5 h-5 bg-orange-500 rounded-full flex items-center justify-center">
                    <el-icon class="text-white" :size="12"><Check /></el-icon>
                  </div>
                </div>
              </div>
              <p class="text-xs text-gray-400 mt-2 text-center">身份可在注册后于个人中心修改</p>
            </div>

            <el-button
              type="primary"
              size="large"
              class="w-full !rounded-lg"
              :loading="loading"
              :disabled="!isRegisterValid"
              @click="onRegister"
            >
              注册
            </el-button>

            <div class="text-center text-sm text-gray-500">
              已有账号？<span class="text-blue-600 cursor-pointer hover:underline" @click="active = 'login'">立即登录</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部信息 -->
      <div class="mt-6 text-center text-xs text-gray-400">
        <p>登录即表示同意 <span class="text-blue-500 cursor-pointer">用户协议</span> 和 <span class="text-blue-500 cursor-pointer">隐私政策</span></p>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.el-input__wrapper) {
  border-radius: 8px;
}
</style>
