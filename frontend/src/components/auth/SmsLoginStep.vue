<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ShieldCheck } from 'lucide-vue-next'
import { useAuthStore } from '../../store/auth'
import { showToast } from '@/composables/useToast'

const props = defineProps<{
  phone: string
  loading: boolean
}>()

const emit = defineEmits<{
  success: []
  switchToPassword: []
  back: []
}>()

const auth = useAuthStore()
const localLoading = ref(false)
const smsCode = ref('')
const sending = ref(false)
const countdown = ref(0)

async function sendCode() {
  if (countdown.value > 0) return
  sending.value = true
  try {
    await auth.sendLoginSmsCode(props.phone)
    showToast.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e: any) {
    showToast.error(e?.message || '发送失败')
  } finally {
    sending.value = false
  }
}

async function handleLogin() {
  if (!smsCode.value) {
    showToast.warning('请输入验证码')
    return
  }
  localLoading.value = true
  try {
    await auth.loginBySms(props.phone, smsCode.value)
    await auth.fetchMe()
    emit('success')
  } catch (e: any) {
    showToast.error(e?.message || '登录失败')
  } finally {
    localLoading.value = false
  }
}

onMounted(sendCode)
</script>

<template>
  <div class="space-y-5">
    <div>
      <h2 class="text-xl font-black text-neutral-900 mb-1">短信验证码登录</h2>
      <p class="text-sm text-neutral-500">验证码已发送至 {{ phone }}</p>
    </div>

    <!-- SMS Code Input -->
    <div class="flex gap-3 items-center">
      <div class="relative flex-1">
        <div class="absolute left-4 top-1/2 -translate-y-1/2 text-neutral-400">
          <ShieldCheck :size="18" :stroke-width="2" />
        </div>
        <input
          v-model="smsCode"
          type="text"
          maxlength="6"
          placeholder="短信验证码"
          class="w-full h-12 pl-11 pr-4 bg-neutral-50 border border-neutral-200 rounded-xl text-sm text-neutral-900 placeholder:text-neutral-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 focus:bg-white outline-none transition-all"
          @keyup.enter="handleLogin"
        />
      </div>
      <button
        :disabled="countdown > 0 || sending"
        class="h-12 px-4 border border-neutral-200 rounded-xl text-sm font-medium text-brand-600 hover:bg-brand-50 disabled:text-neutral-400 disabled:hover:bg-transparent transition-all shrink-0"
        @click="sendCode"
      >
        {{ sending ? '发送中...' : countdown > 0 ? `${countdown}s` : '发送验证码' }}
      </button>
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
      <button class="text-brand-600 hover:text-brand-700 font-medium transition-colors" @click="$emit('switchToPassword')">
        改用密码登录
      </button>
    </div>
    <div class="text-center">
      <button class="text-sm text-neutral-400 hover:text-neutral-600 transition-colors" @click="$emit('back')">
        ← 返回
      </button>
    </div>
  </div>
</template>
