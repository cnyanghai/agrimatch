<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { KeyRound, ShieldCheck, RefreshCw, Image as ImageIcon } from 'lucide-vue-next'
import { useAuthStore } from '../../store/auth'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  phone: string
  loading: boolean
}>()

const emit = defineEmits<{
  success: []
  back: []
}>()

const auth = useAuthStore()
const localLoading = ref(false)
const agreed = ref(false)

const form = reactive({
  password: '',
  confirmPassword: '',
  captchaCode: ''
})

const captcha = reactive({
  key: '',
  image: '',
  loading: false
})

const passwordStrength = computed(() => {
  const p = form.password
  if (p.length < 6) return 0
  let score = 0
  if (p.length >= 8) score++
  if (/[a-z]/.test(p) && /[A-Z]/.test(p)) score++
  if (/\d/.test(p)) score++
  if (/[^a-zA-Z0-9]/.test(p)) score++
  return Math.min(score, 3)
})

const strengthLabel = computed(() => ['弱', '中', '强', '很强'][passwordStrength.value])
const strengthColor = computed(() =>
  passwordStrength.value === 0 ? 'bg-red-400' :
  passwordStrength.value === 1 ? 'bg-amber-400' :
  'bg-brand-500'
)
const strengthTextColor = computed(() =>
  passwordStrength.value === 0 ? 'text-red-500' :
  passwordStrength.value === 1 ? 'text-amber-500' :
  'text-brand-600'
)
const strengthWidth = computed(() =>
  passwordStrength.value === 0 ? 'w-1/4' :
  passwordStrength.value === 1 ? 'w-2/4' :
  passwordStrength.value === 2 ? 'w-3/4' :
  'w-full'
)

async function refreshCaptcha() {
  captcha.loading = true
  try {
    const res = await auth.getCaptcha()
    captcha.key = res.captchaKey
    captcha.image = res.captchaImage
  } catch {
    // silent
  } finally {
    captcha.loading = false
  }
}

onMounted(refreshCaptcha)

async function handleRegister() {
  if (!agreed.value) {
    ElMessage.warning('请先同意用户协议和隐私政策')
    return
  }
  if (!form.password || form.password.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  if (!form.captchaCode) {
    ElMessage.warning('请输入验证码')
    return
  }
  localLoading.value = true
  try {
    await auth.register({
      phone: props.phone,
      password: form.password,
      captchaKey: captcha.key,
      captchaCode: form.captchaCode
    })
    await auth.fetchMe()
    emit('success')
  } catch (e: any) {
    if (e?.message?.includes('已存在') || e?.response?.status === 409) {
      ElMessage.error('该手机号已注册，请返回登录')
    } else {
      ElMessage.error(e?.message || '注册失败')
    }
    refreshCaptcha()
    form.captchaCode = ''
  } finally {
    localLoading.value = false
  }
}
</script>

<template>
  <div class="space-y-5">
    <div>
      <h2 class="text-xl font-black text-neutral-900 mb-1">创建账号</h2>
      <p class="text-sm text-neutral-500">{{ phone }}</p>
    </div>

    <!-- Password -->
    <div>
      <div class="relative">
        <div class="absolute left-4 top-1/2 -translate-y-1/2 text-neutral-400">
          <KeyRound :size="18" :stroke-width="2" />
        </div>
        <input
          v-model="form.password"
          type="password"
          placeholder="设置密码（至少6位）"
          class="w-full h-12 pl-11 pr-4 bg-neutral-50 border border-neutral-200 rounded-xl text-sm text-neutral-900 placeholder:text-neutral-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 focus:bg-white outline-none transition-all"
        />
      </div>
      <!-- Password strength -->
      <div v-if="form.password" class="mt-2 flex items-center gap-2">
        <div class="flex-1 h-1 rounded-full bg-neutral-100 overflow-hidden">
          <div class="h-full transition-all duration-300" :class="[strengthWidth, strengthColor]" />
        </div>
        <span class="text-xs font-medium" :class="strengthTextColor">{{ strengthLabel }}</span>
      </div>
    </div>

    <!-- Confirm Password -->
    <div class="relative">
      <div class="absolute left-4 top-1/2 -translate-y-1/2 text-neutral-400">
        <KeyRound :size="18" :stroke-width="2" />
      </div>
      <input
        v-model="form.confirmPassword"
        type="password"
        placeholder="确认密码"
        class="w-full h-12 pl-11 pr-4 bg-neutral-50 border border-neutral-200 rounded-xl text-sm text-neutral-900 placeholder:text-neutral-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 focus:bg-white outline-none transition-all"
      />
    </div>

    <!-- Captcha -->
    <div class="flex gap-3 items-center">
      <div class="relative flex-1">
        <div class="absolute left-4 top-1/2 -translate-y-1/2 text-neutral-400">
          <ShieldCheck :size="18" :stroke-width="2" />
        </div>
        <input
          v-model="form.captchaCode"
          maxlength="4"
          placeholder="验证码"
          class="w-full h-12 pl-11 pr-4 bg-neutral-50 border border-neutral-200 rounded-xl text-sm text-neutral-900 placeholder:text-neutral-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 focus:bg-white outline-none transition-all"
          @keyup.enter="handleRegister"
        />
      </div>
      <div
        class="h-12 w-28 rounded-xl border border-neutral-200 overflow-hidden cursor-pointer flex items-center justify-center bg-neutral-50 hover:border-brand-300 transition-all shrink-0"
        @click="refreshCaptcha"
      >
        <div v-if="captcha.loading" class="flex items-center gap-1 text-neutral-400">
          <RefreshCw class="animate-spin" :size="14" :stroke-width="2" />
          <span class="text-xs">加载中</span>
        </div>
        <img v-else-if="captcha.image" :src="captcha.image" alt="验证码" class="h-full w-full object-cover" />
        <div v-else class="flex items-center gap-1 text-neutral-400">
          <ImageIcon :size="14" :stroke-width="2" />
          <span class="text-xs">点击获取</span>
        </div>
      </div>
    </div>

    <!-- Agreement -->
    <label class="flex items-start gap-2.5 cursor-pointer select-none">
      <input
        v-model="agreed"
        type="checkbox"
        class="mt-0.5 w-4 h-4 rounded border-neutral-300 text-brand-600 focus:ring-brand-500 cursor-pointer"
      />
      <span class="text-xs text-neutral-500 leading-relaxed">
        我已阅读并同意
        <a href="/legal/terms" target="_blank" class="text-brand-600 font-semibold hover:underline" @click.stop>《用户协议》</a>
        和
        <a href="/legal/privacy" target="_blank" class="text-brand-600 font-semibold hover:underline" @click.stop>《隐私政策》</a>
      </span>
    </label>

    <!-- Register Button -->
    <button
      :disabled="localLoading || !agreed"
      class="w-full h-12 bg-brand-600 hover:bg-brand-700 disabled:bg-neutral-200 disabled:text-neutral-400 text-white font-bold rounded-xl transition-all active:scale-95 flex items-center justify-center gap-2"
      @click="handleRegister"
    >
      <svg v-if="localLoading" class="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
      <span>{{ localLoading ? '注册中...' : '注册' }}</span>
    </button>

    <!-- Back -->
    <div class="text-center">
      <button class="text-sm text-neutral-400 hover:text-neutral-600 transition-colors" @click="$emit('back')">
        ← 返回
      </button>
    </div>
  </div>
</template>
