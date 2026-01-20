<script setup lang="ts">
import { computed, reactive, ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { ShieldCheck, Phone, KeyRound, Sparkles, CheckCircle, RefreshCw, Image as ImageIcon } from 'lucide-vue-next'
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

// 图形验证码
const captcha = reactive({
  key: '',
  image: '',
  loading: false
})

// 登录表单
const loginForm = reactive({
  phoneOrUser: '',
  password: '',
  captchaCode: ''
})

// 注册表单（极简：手机号+密码+验证码）
const registerForm = reactive({
  phone: '',
  password: '',
  captchaCode: ''
})

const loading = ref(false)
const agreed = ref(false)

// 注册成功状态
const registerSuccess = ref(false)

// 获取验证码
async function refreshCaptcha() {
  captcha.loading = true
  try {
    const res = await auth.getCaptcha()
    captcha.key = res.captchaKey
    captcha.image = res.captchaImage
  } catch (e: any) {
    ElMessage.error(e?.message || '获取验证码失败')
  } finally {
    captcha.loading = false
  }
}

// 监听弹窗打开，刷新验证码
watch(() => ui.authDialogVisible, (v) => {
  if (v) {
    refreshCaptcha()
    // 重置表单
    loginForm.phoneOrUser = ''
    loginForm.password = ''
    loginForm.captchaCode = ''
    registerForm.phone = ''
    registerForm.password = ''
    registerForm.captchaCode = ''
  }
})

// Tab 切换时也刷新验证码
watch(activeTab, () => {
  refreshCaptcha()
  loginForm.captchaCode = ''
  registerForm.captchaCode = ''
})

async function onLogin() {
  loading.value = true
  try {
    if (!loginForm.phoneOrUser || loginForm.phoneOrUser.length < 4) {
      ElMessage.warning('请输入手机号/用户名')
      loading.value = false
      return
    }
    if (!loginForm.password) {
      ElMessage.warning('请输入密码')
      loading.value = false
      return
    }
    if (!loginForm.captchaCode) {
      ElMessage.warning('请输入验证码')
      loading.value = false
      return
    }
    
    await auth.loginByPassword(loginForm.phoneOrUser, loginForm.password, captcha.key, loginForm.captchaCode)
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
    // 登录失败时刷新验证码
    refreshCaptcha()
    loginForm.captchaCode = ''
  } finally {
    loading.value = false
  }
}

async function onRegister() {
  loading.value = true
  try {
    // 验证
    if (!registerForm.phone || registerForm.phone.length < 11) {
      ElMessage.warning('请输入正确的手机号')
      loading.value = false
      return
    }
    if (!registerForm.password || registerForm.password.length < 6) {
      ElMessage.warning('密码至少6位')
      loading.value = false
      return
    }
    if (!registerForm.captchaCode) {
      ElMessage.warning('请输入验证码')
      loading.value = false
      return
    }
    if (!agreed.value) {
      ElMessage.warning('请先同意用户协议与隐私政策')
      loading.value = false
      return
    }
    
    // 注册
    await auth.register({
      phone: registerForm.phone,
      password: registerForm.password,
      captchaKey: captcha.key,
      captchaCode: registerForm.captchaCode,
      nickName: registerForm.phone,
      userType: 'buyer',
      companyName: '',
      companyType: 'trader'
    })
    await auth.fetchMe()
    
    // 显示成功动画
    registerSuccess.value = true
    
    // 延迟跳转
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
    // 注册失败时刷新验证码
    refreshCaptcha()
    registerForm.captchaCode = ''
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
      <div class="w-10 h-10 rounded-xl bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-600">
        <ShieldCheck :size="20" :stroke-width="2" />
      </div>
    </template>
    
    <!-- 标题 -->
    <template #title>
      <div class="text-xl font-bold text-gray-900">欢迎使用</div>
      <div class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mt-0.5">AgriMatch 供需协作平台</div>
    </template>

    <div class="bg-gray-50 p-6 rounded-xl border border-gray-200">
      <el-tabs v-model="activeTab" class="auth-tabs">
        <!-- 登录 Tab -->
        <el-tab-pane name="login" label="登录">
          <div class="space-y-4">
            <div class="bg-white p-6 rounded-xl border border-gray-200">
              <div class="text-sm font-bold text-gray-900 mb-4">账号密码登录</div>

              <el-input v-model="loginForm.phoneOrUser" placeholder="手机号/用户名" class="mb-3">
                <template #prefix><Phone class="text-gray-400" :size="16" :stroke-width="2" /></template>
              </el-input>
              <el-input v-model="loginForm.password" placeholder="密码" show-password class="mb-3">
                <template #prefix><KeyRound class="text-gray-400" :size="16" :stroke-width="2" /></template>
              </el-input>
              
              <!-- 图形验证码 -->
              <div class="flex gap-3 items-center">
                <el-input v-model="loginForm.captchaCode" placeholder="验证码" class="flex-1" maxlength="4">
                  <template #prefix><ShieldCheck class="text-gray-400" :size="16" :stroke-width="2" /></template>
                </el-input>
                <div 
                  class="h-10 w-28 rounded-xl border border-gray-200 overflow-hidden cursor-pointer flex items-center justify-center bg-gray-50 hover:border-brand-300 transition-all"
                  @click="refreshCaptcha"
                  :title="'点击刷新验证码'"
                >
                  <div v-if="captcha.loading" class="flex items-center gap-1 text-gray-400">
                    <RefreshCw class="animate-spin" :size="14" :stroke-width="2" />
                    <span class="text-xs">加载中</span>
                  </div>
                  <img 
                    v-else-if="captcha.image" 
                    :src="captcha.image" 
                    alt="验证码" 
                    class="h-full w-full object-cover"
                  />
                  <div v-else class="flex items-center gap-1 text-gray-400">
                    <ImageIcon :size="14" :stroke-width="2" />
                    <span class="text-xs">点击获取</span>
                  </div>
                </div>
              </div>

              <div class="flex justify-between items-center mt-6">
                <button class="text-sm font-bold text-brand-600 hover:text-brand-700 transition-all " @click="activeTab = 'register'">
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
          <div v-if="registerSuccess" class="bg-white p-8 rounded-xl border border-gray-200 text-center">
            <div class="w-16 h-16 mx-auto mb-4 rounded-full bg-brand-100 flex items-center justify-center animate-bounce">
              <CheckCircle class="text-brand-600" :size="32" :stroke-width="2" />
            </div>
            <div class="text-xl font-bold text-gray-900 mb-2">注册成功！</div>
            <div class="text-sm text-gray-500">正在为您跳转...</div>
          </div>

          <!-- 注册表单 -->
          <div v-else class="space-y-4">
            <div class="bg-white p-6 rounded-xl border border-gray-200">
              <!-- 极简提示 -->
              <div class="flex items-center gap-2 mb-4 p-3 bg-brand-50 rounded-xl border border-brand-100">
                <Sparkles class="text-brand-600 shrink-0" :size="16" :stroke-width="2" />
                <span class="text-xs text-brand-700 font-medium">30秒极速注册，企业资料稍后完善</span>
              </div>

              <!-- 手机号 -->
              <el-input v-model="registerForm.phone" placeholder="手机号" class="mb-3" maxlength="11">
                <template #prefix><Phone class="text-gray-400" :size="16" :stroke-width="2" /></template>
              </el-input>

              <!-- 密码 -->
              <el-input v-model="registerForm.password" placeholder="设置密码（至少6位）" show-password class="mb-3">
                <template #prefix><KeyRound class="text-gray-400" :size="16" :stroke-width="2" /></template>
              </el-input>

              <!-- 密码强度提示 -->
              <div v-if="registerForm.password" class="mb-3">
                <div class="flex items-center gap-2">
                  <div class="flex-1 h-1 rounded-full bg-gray-100 overflow-hidden">
                    <div 
                      class="h-full transition-all duration-300"
                      :class="[
                        registerForm.password.length < 6 ? 'w-1/4 bg-red-400' :
                        registerForm.password.length < 8 ? 'w-2/4 bg-amber-400' :
                        registerForm.password.length < 12 ? 'w-3/4 bg-brand-400' :
                        'w-full bg-brand-600'
                      ]"
                    />
                  </div>
                  <span class="text-xs font-medium" :class="[
                    registerForm.password.length < 6 ? 'text-red-500' :
                    registerForm.password.length < 8 ? 'text-amber-500' :
                    'text-brand-600'
                  ]">
                    {{ registerForm.password.length < 6 ? '弱' : registerForm.password.length < 8 ? '中' : '强' }}
                  </span>
                </div>
              </div>

              <!-- 图形验证码 -->
              <div class="flex gap-3 items-center">
                <el-input v-model="registerForm.captchaCode" placeholder="验证码" class="flex-1" maxlength="4">
                  <template #prefix><ShieldCheck class="text-gray-400" :size="16" :stroke-width="2" /></template>
                </el-input>
                <div 
                  class="h-10 w-28 rounded-xl border border-gray-200 overflow-hidden cursor-pointer flex items-center justify-center bg-gray-50 hover:border-brand-300 transition-all"
                  @click="refreshCaptcha"
                  :title="'点击刷新验证码'"
                >
                  <div v-if="captcha.loading" class="flex items-center gap-1 text-gray-400">
                    <RefreshCw class="animate-spin" :size="14" :stroke-width="2" />
                    <span class="text-xs">加载中</span>
                  </div>
                  <img 
                    v-else-if="captcha.image" 
                    :src="captcha.image" 
                    alt="验证码" 
                    class="h-full w-full object-cover"
                  />
                  <div v-else class="flex items-center gap-1 text-gray-400">
                    <ImageIcon :size="14" :stroke-width="2" />
                    <span class="text-xs">点击获取</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 协议与注册按钮 -->
            <div class="flex flex-col gap-4">
              <el-checkbox v-model="agreed">
                我已阅读并同意 
                <button type="button" class="text-brand-600 font-bold hover:underline" @click.stop="openAgreement('user')">用户协议</button> 
                与 
                <button type="button" class="text-brand-600 font-bold hover:underline" @click.stop="openAgreement('privacy')">隐私政策</button>
              </el-checkbox>
              
              <div class="flex items-center justify-between">
                <button class="text-sm font-bold text-brand-600 hover:text-brand-700 transition-all " @click="activeTab = 'login'">
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
