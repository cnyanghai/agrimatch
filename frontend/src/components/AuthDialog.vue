<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { ShieldCheck, Phone, KeyRound, Sparkles, CheckCircle } from 'lucide-vue-next'
import { useAuthStore } from '../store/auth'
import { useUiStore } from '../store/ui'
import { BaseModal, BaseButton } from './ui'

const router = useRouter()
const auth = useAuthStore()
const ui = useUiStore()

const visible = computed({
  get: () => ui.authDialogVisible,
  set: (v: boolean) => (v ? ui.openAuthDialog(ui.authDialogMode, ui.pendingNav ?? undefined) : ui.closeAuthDialog())
})

const activeTab = ref<'login' | 'register'>(ui.authDialogMode)
watch(
  () => ui.authDialogMode,
  (m) => (activeTab.value = m),
  { immediate: true }
)

// 登录表单
const loginForm = reactive({
  mode: 'password' as 'password' | 'sms',
  phoneOrUser: '',
  password: '',
  phone: '',
  smsCode: ''
})

// 注册表单（极简：手机号+验证码+密码）
const registerForm = reactive({
  phone: '',
  smsCode: '',
  password: ''
})

const loading = ref(false)
const smsLoading = ref(false)
const smsCountdown = ref(0)
let smsTimer: any = null
const agreed = ref(false)

// 注册成功状态
const registerSuccess = ref(false)

function startCountdown() {
  smsCountdown.value = 60
  smsTimer = setInterval(() => {
    smsCountdown.value -= 1
    if (smsCountdown.value <= 0) {
      clearInterval(smsTimer)
      smsTimer = null
    }
  }, 1000)
}

async function sendLoginSms() {
  if (!loginForm.phone || loginForm.phone.length < 11) return ElMessage.warning('请输入正确的手机号')
  smsLoading.value = true
  try {
    await auth.sendSmsCode(loginForm.phone, 2)
    ElMessage.success('验证码已发送')
    startCountdown()
  } catch (e: any) {
    ElMessage.error(e?.message || '发送失败')
  } finally {
    smsLoading.value = false
  }
}

async function sendRegisterSms() {
  if (!registerForm.phone || registerForm.phone.length < 11) return ElMessage.warning('请输入正确的手机号')
  smsLoading.value = true
  try {
    await auth.sendSmsCode(registerForm.phone, 1)
    ElMessage.success('验证码已发送')
    startCountdown()
  } catch (e: any) {
    ElMessage.error(e?.message || '发送失败')
  } finally {
    smsLoading.value = false
  }
}

async function onLogin() {
  loading.value = true
  try {
    if (loginForm.mode === 'password') {
      if (!loginForm.phoneOrUser || loginForm.phoneOrUser.length < 4) return ElMessage.warning('请输入手机号/用户名')
      if (!loginForm.password) return ElMessage.warning('请输入密码')
      await auth.loginByPassword(loginForm.phoneOrUser, loginForm.password)
    } else {
      if (!loginForm.phone || loginForm.phone.length < 11) return ElMessage.warning('请输入正确的手机号')
      if (!loginForm.smsCode) return ElMessage.warning('请输入验证码')
      await auth.loginBySms(loginForm.phone, loginForm.smsCode)
    }
    await auth.fetchMe()
    ElMessage.success('登录成功')
    const pending = ui.pendingNav
    ui.closeAuthDialog()
    ui.clearPendingNav()
    if (pending?.path) {
      await router.push({ path: pending.path, query: pending.query })
    } else {
      await router.push('/console')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

async function onRegister() {
  loading.value = true
  try {
    // 极简验证
    if (!registerForm.phone || registerForm.phone.length < 11) return ElMessage.warning('请输入正确的手机号')
    if (!registerForm.smsCode) return ElMessage.warning('请输入验证码')
    if (!registerForm.password || registerForm.password.length < 6) return ElMessage.warning('密码至少6位')
    if (!agreed.value) return ElMessage.warning('请先同意用户协议与隐私政策')
    
    // 极简注册：只传必要字段，后端自动创建空公司
    await auth.register({
      phone: registerForm.phone,
      smsCode: registerForm.smsCode,
      password: registerForm.password,
      nickName: registerForm.phone, // 默认用手机号作为昵称
      userType: 'buyer', // 默认采购商，后续可在资料中修改
      companyName: '', // 空公司名，后续完善
      companyType: 'trader' // 默认类型
    })
    await auth.fetchMe()
    
    // 显示成功动画
    registerSuccess.value = true
    
    // 延迟跳转，让用户看到成功动画
    setTimeout(async () => {
      const pending = ui.pendingNav
      ui.closeAuthDialog()
      ui.clearPendingNav()
      registerSuccess.value = false
      if (pending?.path) {
        await router.push({ path: pending.path, query: pending.query })
      } else {
        await router.push('/console')
      }
    }, 1500)
  } catch (e: any) {
    ElMessage.error(e?.message || '注册失败')
  } finally {
    loading.value = false
  }
}

// 打开协议弹窗（简化处理）
function openAgreement(type: 'user' | 'privacy') {
  const title = type === 'user' ? '用户协议' : '隐私政策'
  ElMessage.info(`${title}详情页面开发中...`)
}
</script>

<template>
  <BaseModal
    v-model="visible"
    size="md"
    :show-footer="false"
    :close-on-overlay="false"
  >
    <!-- 头部图标 -->
    <template #icon>
      <div class="w-10 h-10 rounded-xl bg-emerald-50 border border-emerald-100 flex items-center justify-center text-emerald-600">
        <ShieldCheck :size="20" :stroke-width="2" />
      </div>
    </template>
    
    <!-- 标题 -->
    <template #title>
      <div class="text-xl font-bold text-gray-900">欢迎使用</div>
      <div class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mt-0.5">AgriMatch 供需协作平台</div>
    </template>

    <div class="bg-gray-50 p-6 rounded-2xl border border-gray-100">
      <el-tabs v-model="activeTab" class="auth-tabs">
        <!-- 登录 Tab -->
        <el-tab-pane name="login" label="登录">
          <div class="space-y-4">
            <div class="bg-white p-6 rounded-2xl border border-gray-100">
              <div class="flex items-center justify-between mb-4">
                <div class="text-sm font-bold text-gray-900">账号登录</div>
                <el-segmented
                  v-model="loginForm.mode"
                  :options="[
                    { label: '密码登录', value: 'password' },
                    { label: '验证码登录', value: 'sms' }
                  ]"
                />
              </div>

              <template v-if="loginForm.mode === 'password'">
                <el-input v-model="loginForm.phoneOrUser" placeholder="手机号/用户名" class="mb-3">
                  <template #prefix><Phone class="text-gray-400" :size="16" :stroke-width="2" /></template>
                </el-input>
                <el-input v-model="loginForm.password" placeholder="密码" show-password>
                  <template #prefix><KeyRound class="text-gray-400" :size="16" :stroke-width="2" /></template>
                </el-input>
              </template>

              <template v-else>
                <el-input v-model="loginForm.phone" placeholder="手机号" class="mb-3" maxlength="11">
                  <template #prefix><Phone class="text-gray-400" :size="16" :stroke-width="2" /></template>
                </el-input>
                <div class="flex gap-2">
                  <el-input v-model="loginForm.smsCode" placeholder="验证码" class="flex-1" maxlength="6">
                    <template #prefix><ShieldCheck class="text-gray-400" :size="16" :stroke-width="2" /></template>
                  </el-input>
                  <BaseButton 
                    type="secondary"
                    :disabled="smsCountdown > 0"
                    :loading="smsLoading"
                    @click="sendLoginSms"
                  >
                    {{ smsCountdown > 0 ? `${smsCountdown}s` : '发送验证码' }}
                  </BaseButton>
                </div>
              </template>

              <div class="flex justify-between items-center mt-6">
                <button class="text-sm font-bold text-emerald-600 hover:text-emerald-700 transition-all active:scale-95" @click="activeTab = 'register'">
                  没有账号？去注册
                </button>
                <BaseButton 
                  type="primary"
                  :loading="loading"
                  @click="onLogin"
                >
                  登录
                </BaseButton>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 注册 Tab -->
        <el-tab-pane name="register" label="注册">
          <!-- 注册成功状态 -->
          <div v-if="registerSuccess" class="bg-white p-8 rounded-2xl border border-gray-100 text-center">
            <div class="w-16 h-16 mx-auto mb-4 rounded-full bg-emerald-100 flex items-center justify-center animate-bounce">
              <CheckCircle class="text-emerald-600" :size="32" :stroke-width="2" />
            </div>
            <div class="text-xl font-bold text-gray-900 mb-2">注册成功！</div>
            <div class="text-sm text-gray-500">正在为您跳转...</div>
          </div>

          <!-- 注册表单 -->
          <div v-else class="space-y-4">
            <div class="bg-white p-6 rounded-2xl border border-gray-100">
              <!-- 极简提示 -->
              <div class="flex items-center gap-2 mb-4 p-3 bg-emerald-50 rounded-xl border border-emerald-100">
                <Sparkles class="text-emerald-600 shrink-0" :size="16" :stroke-width="2" />
                <span class="text-xs text-emerald-700 font-medium">30秒极速注册，企业资料稍后完善</span>
              </div>

              <!-- 手机号 -->
              <el-input v-model="registerForm.phone" placeholder="手机号" class="mb-3" maxlength="11">
                <template #prefix><Phone class="text-gray-400" :size="16" :stroke-width="2" /></template>
              </el-input>

              <!-- 验证码 -->
              <div class="flex gap-2 mb-3">
                <el-input v-model="registerForm.smsCode" placeholder="验证码" class="flex-1" maxlength="6">
                  <template #prefix><ShieldCheck class="text-gray-400" :size="16" :stroke-width="2" /></template>
                </el-input>
                <BaseButton 
                  type="secondary"
                  :disabled="smsCountdown > 0"
                  :loading="smsLoading"
                  @click="sendRegisterSms"
                >
                  {{ smsCountdown > 0 ? `${smsCountdown}s` : '发送验证码' }}
                </BaseButton>
              </div>

              <!-- 密码 -->
              <el-input v-model="registerForm.password" placeholder="设置密码（至少6位）" show-password>
                <template #prefix><KeyRound class="text-gray-400" :size="16" :stroke-width="2" /></template>
              </el-input>

              <!-- 密码强度提示 -->
              <div v-if="registerForm.password" class="mt-2">
                <div class="flex items-center gap-2">
                  <div class="flex-1 h-1 rounded-full bg-gray-100 overflow-hidden">
                    <div 
                      class="h-full transition-all duration-300"
                      :class="[
                        registerForm.password.length < 6 ? 'w-1/4 bg-red-400' :
                        registerForm.password.length < 8 ? 'w-2/4 bg-amber-400' :
                        registerForm.password.length < 12 ? 'w-3/4 bg-emerald-400' :
                        'w-full bg-emerald-600'
                      ]"
                    />
                  </div>
                  <span class="text-xs font-medium" :class="[
                    registerForm.password.length < 6 ? 'text-red-500' :
                    registerForm.password.length < 8 ? 'text-amber-500' :
                    'text-emerald-600'
                  ]">
                    {{ registerForm.password.length < 6 ? '弱' : registerForm.password.length < 8 ? '中' : '强' }}
                  </span>
                </div>
              </div>
            </div>

            <!-- 协议与注册按钮 -->
            <div class="flex flex-col gap-4">
              <el-checkbox v-model="agreed">
                我已阅读并同意 
                <button type="button" class="text-emerald-600 font-bold hover:underline" @click.stop="openAgreement('user')">用户协议</button> 
                与 
                <button type="button" class="text-emerald-600 font-bold hover:underline" @click.stop="openAgreement('privacy')">隐私政策</button>
              </el-checkbox>
              
              <div class="flex items-center justify-between">
                <button class="text-sm font-bold text-emerald-600 hover:text-emerald-700 transition-all active:scale-95" @click="activeTab = 'login'">
                  已有账号？去登录
                </button>
                <BaseButton 
                  type="primary"
                  :loading="loading"
                  :disabled="!agreed"
                  @click="onRegister"
                >
                  立即注册
                </BaseButton>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </BaseModal>
</template>

<style scoped>
/* 样式已通过全局 animations.css 统一 */
</style>
