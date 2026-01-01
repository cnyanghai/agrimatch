<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { X, Building2, ShieldCheck, Phone, KeyRound, User, MapPin, Briefcase } from 'lucide-vue-next'
import { useAuthStore } from '../store/auth'
import { useUiStore } from '../store/ui'
import { updateCompany, type CompanyType } from '../api/company'

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
  userType: 'buyer' as 'buyer' | 'seller',
  // 企业资料（注册即完善）
  companyType: 'trader' as CompanyType,
  companyName: '',
  contacts: '', // 注册人姓名
  province: '',
  city: '',
  district: '',
  address: ''
})

const loading = ref(false)
const smsLoading = ref(false)
const smsCountdown = ref(0)
let smsTimer: any = null
const agreed = ref(false)

// 不再需要昵称：联系人为空时可用手机号兜底
watch(
  () => registerForm.phone,
  (v) => {
    if (!registerForm.contacts) registerForm.contacts = v || ''
  }
)

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
    if (!registerForm.password || registerForm.password.length < 6) return ElMessage.warning('密码至少6位')
    if (!agreed.value) return ElMessage.warning('请先同意用户协议与隐私政策')
    if (!registerForm.companyName) return ElMessage.warning('请输入公司名称')
    if (!registerForm.contacts) return ElMessage.warning('请输入联系人姓名')
    if (!registerForm.province) return ElMessage.warning('请输入省')
    if (!registerForm.city) return ElMessage.warning('请输入市')
    if (!registerForm.district) return ElMessage.warning('请输入区/县')
    if (!registerForm.address) return ElMessage.warning('请输入详细地址')
    await auth.register({
      phone: registerForm.phone,
      smsCode: registerForm.smsCode,
      password: registerForm.password,
      nickName: registerForm.contacts || registerForm.phone,
      userType: registerForm.userType,
      companyName: registerForm.companyName,
      companyType: registerForm.companyType
    })
    await auth.fetchMe()
    // 注册后补齐公司资料（公司创建与 companyId 绑定由后端 register 负责）
    if (auth.me?.companyId) {
      await updateCompany(auth.me.companyId, {
        companyName: registerForm.companyName,
        companyType: registerForm.companyType,
        contacts: registerForm.contacts,
        phone: registerForm.phone,
        province: registerForm.province,
        city: registerForm.city,
        district: registerForm.district,
        address: registerForm.address
      })
    }
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
    width="560px"
    :close-on-click-modal="false"
    :show-close="false"
    class="auth-dialog"
    @close="onClose"
  >
    <template #header>
      <div class="flex items-start justify-between gap-4">
        <div class="flex items-start gap-3">
          <div class="w-10 h-10 rounded-2xl bg-emerald-50 border border-emerald-100 flex items-center justify-center text-emerald-600">
            <ShieldCheck :size="18" :stroke-width="2" />
          </div>
          <div class="leading-tight">
            <div class="text-lg font-bold text-gray-900">登录 / 注册</div>
            <div class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">AgriMatch 供需协作平台</div>
          </div>
        </div>
        <button class="p-2 rounded-full hover:bg-gray-50/50 transition-all active:scale-95 text-gray-400" @click="onClose">
          <X :size="18" :stroke-width="2" />
        </button>
      </div>
    </template>

    <div class="bg-gray-50 p-6 rounded-2xl border border-gray-100">
      <el-tabs v-model="activeTab" class="auth-tabs">
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
                <el-input v-model="loginForm.phone" placeholder="手机号" class="mb-3">
                  <template #prefix><Phone class="text-gray-400" :size="16" :stroke-width="2" /></template>
                </el-input>
                <div class="flex gap-2">
                  <el-input v-model="loginForm.smsCode" placeholder="验证码" class="flex-1">
                    <template #prefix><ShieldCheck class="text-gray-400" :size="16" :stroke-width="2" /></template>
                  </el-input>
                  <el-button class="!rounded-xl transition-all active:scale-95" :disabled="smsCountdown > 0" :loading="smsLoading" @click="sendLoginSms">
                    {{ smsCountdown > 0 ? `${smsCountdown}s` : '发送验证码' }}
                  </el-button>
                </div>
                <div class="text-xs text-gray-400 mt-2">开发期支持固定验证码：<span class="font-bold">000000</span></div>
              </template>

              <div class="flex justify-between items-center mt-6">
                <button class="text-sm font-bold text-emerald-600 hover:text-emerald-700 transition-all active:scale-95" @click="activeTab = 'register'">
                  没有账号？去注册
                </button>
                <el-button
                  type="primary"
                  class="!rounded-xl !bg-emerald-600 !border-emerald-600 hover:!bg-emerald-700 transition-all active:scale-95"
                  :loading="loading"
                  @click="onLogin"
                >
                  登录
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane name="register" label="注册">
          <div class="space-y-4">
            <!-- 账号信息 -->
            <div class="bg-white p-6 rounded-2xl border border-gray-100">
              <div class="flex items-center gap-2 mb-4">
                <User class="text-gray-400" :size="18" :stroke-width="2" />
                <div class="text-sm font-bold text-gray-900">账号信息</div>
              </div>

              <el-input v-model="registerForm.phone" placeholder="手机号" class="mb-3">
                <template #prefix><Phone class="text-gray-400" :size="16" :stroke-width="2" /></template>
              </el-input>
              <div class="flex gap-2 mb-3">
                <el-input v-model="registerForm.smsCode" placeholder="验证码" class="flex-1">
                  <template #prefix><ShieldCheck class="text-gray-400" :size="16" :stroke-width="2" /></template>
                </el-input>
                <el-button class="!rounded-xl transition-all active:scale-95" :disabled="smsCountdown > 0" :loading="smsLoading" @click="sendRegisterSms">
                  {{ smsCountdown > 0 ? `${smsCountdown}s` : '发送验证码' }}
                </el-button>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                <el-input v-model="registerForm.contacts" placeholder="联系人姓名（必填）">
                  <template #prefix><User class="text-gray-400" :size="16" :stroke-width="2" /></template>
                </el-input>
                <el-input v-model="registerForm.password" placeholder="设置密码(至少6位)" show-password>
                  <template #prefix><KeyRound class="text-gray-400" :size="16" :stroke-width="2" /></template>
                </el-input>
              </div>

              <div class="flex flex-wrap gap-3 items-center mt-4">
                <div class="text-sm text-gray-500">身份：</div>
                <el-radio-group v-model="registerForm.userType">
                  <el-radio-button label="buyer">采购商</el-radio-button>
                  <el-radio-button label="seller">供应商</el-radio-button>
                </el-radio-group>
              </div>
            </div>

            <!-- 企业资料 -->
            <div class="bg-white p-6 rounded-2xl border border-gray-100">
              <div class="flex items-center justify-between mb-4">
                <div class="flex items-center gap-2">
                  <Building2 class="text-gray-400" :size="18" :stroke-width="2" />
                  <div class="text-sm font-bold text-gray-900">企业资料</div>
                </div>
                <span class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">注册即完善</span>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                <el-select v-model="registerForm.companyType" placeholder="企业类型">
                  <el-option label="饲料厂" value="feed_factory" />
                  <el-option label="贸易商" value="trader" />
                  <el-option label="粮库" value="grain_depot" />
                  <el-option label="加工厂" value="processor" />
                  <el-option label="物流/仓储" value="logistics" />
                  <el-option label="其他" value="other" />
                </el-select>
                <el-input v-model="registerForm.companyName" placeholder="公司名称">
                  <template #prefix><Briefcase class="text-gray-400" :size="16" :stroke-width="2" /></template>
                </el-input>
                <el-input v-model="registerForm.province" placeholder="省（必填）" />
                <el-input v-model="registerForm.city" placeholder="市（必填）" />
                <el-input v-model="registerForm.district" placeholder="区/县（必填）" />
                <el-input v-model="registerForm.address" placeholder="详细地址（必填）">
                  <template #prefix><MapPin class="text-gray-400" :size="16" :stroke-width="2" /></template>
                </el-input>
              </div>
            </div>

            <div class="flex items-center justify-between">
              <el-checkbox v-model="agreed">
                我已阅读并同意 <span class="text-emerald-600 font-bold">用户协议</span> 与 <span class="text-emerald-600 font-bold">隐私政策</span>
              </el-checkbox>
              <div class="flex items-center gap-3">
                <button class="text-sm font-bold text-emerald-600 hover:text-emerald-700 transition-all active:scale-95" @click="activeTab = 'login'">
                  已有账号？去登录
                </button>
                <el-button
                  type="primary"
                  class="!rounded-xl !bg-emerald-600 !border-emerald-600 hover:!bg-emerald-700 transition-all active:scale-95"
                  :loading="loading"
                  @click="onRegister"
                >
                  注册并登录
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-dialog>
</template>

<style scoped>
.auth-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.auth-dialog :deep(.el-dialog) {
  border-radius: 16px;
  --el-color-primary: #059669;
  --el-color-primary-light-3: #34d399;
  --el-color-primary-light-5: #6ee7b7;
  --el-color-primary-light-7: #a7f3d0;
  --el-color-primary-light-8: #d1fae5;
  --el-color-primary-light-9: #ecfdf5;
}
.auth-dialog :deep(.el-dialog__header) {
  margin: 0;
  padding: 20px 20px 12px 20px;
}
.auth-dialog :deep(.el-dialog__body) {
  padding: 0 20px 20px 20px;
}

/* Tabs 更像“胶囊切换”，去掉默认底边 */
.auth-dialog :deep(.el-tabs__nav-wrap::after) {
  display: none;
}
.auth-dialog :deep(.el-tabs__item) {
  height: 32px;
  line-height: 32px;
  padding: 0 14px;
  border-radius: 9999px;
  font-weight: 700;
  font-size: 12px;
  color: #6b7280;
}
.auth-dialog :deep(.el-tabs__item.is-active) {
  background: #ecfdf5;
  color: #059669;
}
.auth-dialog :deep(.el-tabs__item:hover) {
  color: #065f46;
}
.auth-dialog :deep(.el-tabs__active-bar) {
  display: none;
}

/* 输入框统一风格：灰底、细边框、圆角、focus 主色 */
.auth-dialog :deep(.el-input__wrapper),
.auth-dialog :deep(.el-textarea__inner) {
  box-shadow: none !important;
  background: #f9fafb !important;
  border: 1px solid #e5e7eb !important;
  border-radius: 12px !important;
}
.auth-dialog :deep(.el-input__wrapper.is-focus) {
  border-color: #34d399 !important;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.15) !important;
}
.auth-dialog :deep(.el-select .el-input__wrapper) {
  border-radius: 12px !important;
}

/* Segmented（密码/验证码）统一为 Emerald 胶囊 */
.auth-dialog :deep(.el-segmented) {
  background: #f3f4f6;
  border-radius: 9999px;
  padding: 4px;
}
.auth-dialog :deep(.el-segmented .el-segmented__item) {
  border-radius: 9999px !important;
  font-weight: 800 !important;
  background: transparent !important;
}
.auth-dialog :deep(.el-segmented .el-segmented__item .el-segmented__item-label) {
  color: #6b7280 !important;
}
.auth-dialog :deep(.el-segmented .el-segmented__item:hover) {
  background: #ecfdf5 !important;
}
.auth-dialog :deep(.el-segmented .el-segmented__item:hover .el-segmented__item-label) {
  color: #047857 !important;
}
.auth-dialog :deep(.el-segmented .el-segmented__item.is-selected) {
  background: #059669 !important;
}
.auth-dialog :deep(.el-segmented .el-segmented__item.is-selected .el-segmented__item-label) {
  color: #ffffff !important;
}

/* RadioButton（身份）统一 */
.auth-dialog :deep(.el-radio-button__inner) {
  border-radius: 9999px;
  font-weight: 700;
  border: 1px solid #e5e7eb;
  color: #6b7280;
  background: #ffffff;
}
.auth-dialog :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #059669;
  border-color: #059669;
  color: #ffffff;
  box-shadow: none;
}
.auth-dialog :deep(.el-radio-button__inner:hover) {
  background: #ecfdf5;
  border-color: #a7f3d0;
  color: #047857;
}

/* Checkbox 统一主色 */
.auth-dialog :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #059669;
  border-color: #059669;
}
.auth-dialog :deep(.el-checkbox__label) {
  color: #374151;
  font-weight: 600;
  font-size: 12px;
}

/* Text button/Link（例如“去注册/去登录”） */
.auth-dialog :deep(.el-button.is-text) {
  font-weight: 800;
  color: #059669;
}
.auth-dialog :deep(.el-button.is-text:hover) {
  color: #047857;
  background: #ecfdf5;
  border-radius: 12px;
}
</style>


