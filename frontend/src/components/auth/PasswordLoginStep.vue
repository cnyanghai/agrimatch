<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { KeyRound, ShieldCheck, RefreshCw, Image as ImageIcon } from 'lucide-vue-next'
import { useAuthStore } from '../../store/auth'
import { showToast } from '@/composables/useToast'

const props = defineProps<{
  phone: string
  loading: boolean
}>()

const emit = defineEmits<{
  success: []
  switchToSms: []
  forgotPassword: []
  back: []
}>()

const auth = useAuthStore()
const localLoading = ref(false)

const form = reactive({
  password: '',
  captchaCode: ''
})

const captcha = reactive({
  key: '',
  image: '',
  loading: false
})

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

async function handleLogin() {
  if (!form.password) {
    showToast.warning('请输入密码')
    return
  }
  if (!form.captchaCode) {
    showToast.warning('请输入验证码')
    return
  }
  localLoading.value = true
  try {
    await auth.loginByPassword(props.phone, form.password, captcha.key, form.captchaCode)
    await auth.fetchMe()
    emit('success')
  } catch (e: any) {
    showToast.error(e?.message || '登录失败')
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
      <h2 class="text-xl font-black text-neutral-900 mb-1">密码登录</h2>
      <p class="text-sm text-neutral-500">{{ phone }}</p>
    </div>

    <!-- Password -->
    <div class="relative">
      <div class="absolute left-4 top-1/2 -translate-y-1/2 text-neutral-400">
        <KeyRound :size="18" :stroke-width="2" />
      </div>
      <input
        v-model="form.password"
        type="password"
        placeholder="请输入密码"
        class="w-full h-12 pl-11 pr-4 bg-neutral-50 border border-neutral-200 rounded-xl text-sm text-neutral-900 placeholder:text-neutral-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 focus:bg-white outline-none transition-all"
        @keyup.enter="handleLogin"
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
          @keyup.enter="handleLogin"
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
        <img
          v-else-if="captcha.image"
          :src="captcha.image"
          alt="验证码"
          class="h-full w-full object-cover"
        />
        <div v-else class="flex items-center gap-1 text-neutral-400">
          <ImageIcon :size="14" :stroke-width="2" />
          <span class="text-xs">点击获取</span>
        </div>
      </div>
    </div>

    <!-- Login Button -->
    <button
      :disabled="localLoading"
      class="w-full h-12 bg-brand-600 hover:bg-brand-700 disabled:bg-neutral-200 disabled:text-neutral-400 text-white font-bold rounded-xl transition-all active:scale-95 flex items-center justify-center gap-2"
      @click="handleLogin"
    >
      <svg v-if="localLoading" class="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
      <span>{{ localLoading ? '登录中...' : '登录' }}</span>
    </button>

    <!-- Links -->
    <div class="flex items-center justify-between text-sm">
      <button class="text-brand-600 hover:text-brand-700 font-medium transition-colors" @click="$emit('switchToSms')">
        改用短信登录
      </button>
      <button class="text-neutral-400 hover:text-neutral-600 transition-colors" @click="$emit('forgotPassword')">
        忘记密码？
      </button>
    </div>
    <div class="text-center">
      <button class="text-sm text-neutral-400 hover:text-neutral-600 transition-colors" @click="$emit('back')">
        ← 返回
      </button>
    </div>
  </div>
</template>
