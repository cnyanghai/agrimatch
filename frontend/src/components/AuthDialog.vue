<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { useUiStore } from '../store/ui'

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

// 注册表单（短信注册）
const registerForm = reactive({
  phone: '',
  smsCode: '',
  password: '',
  nickName: '',
  userType: 'buyer' as 'buyer' | 'seller'
})

const loading = ref(false)
const smsLoading = ref(false)
const smsCountdown = ref(0)
let smsTimer: any = null

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
  if (!loginForm.phone || loginForm.phone.length < 6) return ElMessage.warning('请输入手机号')
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
  if (!registerForm.phone || registerForm.phone.length < 6) return ElMessage.warning('请输入手机号')
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
      if (!loginForm.phone || loginForm.phone.length < 6) return ElMessage.warning('请输入手机号')
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
    if (!registerForm.phone || registerForm.phone.length < 6) return ElMessage.warning('请输入手机号')
    if (!registerForm.smsCode) return ElMessage.warning('请输入验证码')
    if (!registerForm.nickName) return ElMessage.warning('请输入昵称')
    if (!registerForm.password || registerForm.password.length < 6) return ElMessage.warning('密码至少6位')
    await auth.register({
      phone: registerForm.phone,
      smsCode: registerForm.smsCode,
      password: registerForm.password,
      nickName: registerForm.nickName,
      userType: registerForm.userType
    })
    await auth.fetchMe()
    ElMessage.success('注册成功')
    const pending = ui.pendingNav
    ui.closeAuthDialog()
    ui.clearPendingNav()
    if (pending?.path) {
      await router.push({ path: pending.path, query: pending.query })
    } else {
      await router.push('/console')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '注册失败')
  } finally {
    loading.value = false
  }
}

function onClose() {
  ui.closeAuthDialog()
}
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="activeTab === 'login' ? '登录/注册' : '注册账号'"
    width="520px"
    :close-on-click-modal="false"
    @close="onClose"
  >
    <el-tabs v-model="activeTab" class="auth-tabs">
      <el-tab-pane name="login" label="登录">
        <div class="space-y-4">
          <div class="flex gap-2">
            <el-segmented
              v-model="loginForm.mode"
              :options="[
                { label: '密码登录', value: 'password' },
                { label: '验证码登录', value: 'sms' }
              ]"
            />
          </div>

          <template v-if="loginForm.mode === 'password'">
            <el-input v-model="loginForm.phoneOrUser" placeholder="手机号/用户名" />
            <el-input v-model="loginForm.password" placeholder="密码" show-password />
          </template>

          <template v-else>
            <el-input v-model="loginForm.phone" placeholder="手机号" />
            <div class="flex gap-2">
              <el-input v-model="loginForm.smsCode" placeholder="验证码" class="flex-1" />
              <el-button
                :disabled="smsCountdown > 0"
                :loading="smsLoading"
                @click="sendLoginSms"
              >
                {{ smsCountdown > 0 ? `${smsCountdown}s` : '发送验证码' }}
              </el-button>
            </div>
          </template>

          <div class="flex justify-between items-center">
            <el-button text type="primary" @click="activeTab = 'register'">没有账号？去注册</el-button>
            <el-button type="primary" :loading="loading" @click="onLogin">登录</el-button>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane name="register" label="注册">
        <div class="space-y-4">
          <el-input v-model="registerForm.phone" placeholder="手机号" />
          <div class="flex gap-2">
            <el-input v-model="registerForm.smsCode" placeholder="验证码" class="flex-1" />
            <el-button
              :disabled="smsCountdown > 0"
              :loading="smsLoading"
              @click="sendRegisterSms"
            >
              {{ smsCountdown > 0 ? `${smsCountdown}s` : '发送验证码' }}
            </el-button>
          </div>
          <el-input v-model="registerForm.nickName" placeholder="昵称" />
          <el-input v-model="registerForm.password" placeholder="密码(至少6位)" show-password />
          <div class="flex gap-3 items-center">
            <div class="text-sm text-gray-500">身份：</div>
            <el-radio-group v-model="registerForm.userType">
              <el-radio-button label="buyer">采购商</el-radio-button>
              <el-radio-button label="seller">供应商</el-radio-button>
            </el-radio-group>
          </div>
          <div class="flex justify-between items-center">
            <el-button text type="primary" @click="activeTab = 'login'">已有账号？去登录</el-button>
            <el-button type="primary" :loading="loading" @click="onRegister">注册并登录</el-button>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>

<style scoped>
.auth-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}
</style>


