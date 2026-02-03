<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ShieldCheck, KeyRound } from 'lucide-vue-next'
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
const sending = ref(false)
const countdown = ref(0)

const form = reactive({
  smsCode: '',
  newPassword: '',
  confirmPassword: ''
})

async function sendCode() {
  if (countdown.value > 0) return
  sending.value = true
  try {
    await auth.sendResetSmsCode(props.phone)
    ElMessage.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e: any) {
    ElMessage.error(e?.message || '发送失败')
  } finally {
    sending.value = false
  }
}

async function handleReset() {
  if (!form.smsCode) {
    ElMessage.warning('请输入验证码')
    return
  }
  if (!form.newPassword || form.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  localLoading.value = true
  try {
    await auth.resetPassword(props.phone, form.smsCode, form.newPassword)
    ElMessage.success('密码重置成功')
    emit('success')
  } catch (e: any) {
    ElMessage.error(e?.message || '重置失败')
  } finally {
    localLoading.value = false
  }
}
</script>

<template>
  <div class="space-y-5">
    <div>
      <h2 class="text-xl font-black text-neutral-900 mb-1">重置密码</h2>
      <p class="text-sm text-neutral-500">{{ phone }}</p>
    </div>

    <!-- SMS Code -->
    <div class="flex gap-3 items-center">
      <div class="relative flex-1">
        <div class="absolute left-4 top-1/2 -translate-y-1/2 text-neutral-400">
          <ShieldCheck :size="18" :stroke-width="2" />
        </div>
        <input
          v-model="form.smsCode"
          type="text"
          maxlength="6"
          placeholder="短信验证码"
          class="w-full h-12 pl-11 pr-4 bg-neutral-50 border border-neutral-200 rounded-xl text-sm text-neutral-900 placeholder:text-neutral-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 focus:bg-white outline-none transition-all"
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

    <!-- New Password -->
    <div class="relative">
      <div class="absolute left-4 top-1/2 -translate-y-1/2 text-neutral-400">
        <KeyRound :size="18" :stroke-width="2" />
      </div>
      <input
        v-model="form.newPassword"
        type="password"
        placeholder="新密码（至少6位）"
        class="w-full h-12 pl-11 pr-4 bg-neutral-50 border border-neutral-200 rounded-xl text-sm text-neutral-900 placeholder:text-neutral-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 focus:bg-white outline-none transition-all"
      />
    </div>

    <!-- Confirm Password -->
    <div class="relative">
      <div class="absolute left-4 top-1/2 -translate-y-1/2 text-neutral-400">
        <KeyRound :size="18" :stroke-width="2" />
      </div>
      <input
        v-model="form.confirmPassword"
        type="password"
        placeholder="确认新密码"
        class="w-full h-12 pl-11 pr-4 bg-neutral-50 border border-neutral-200 rounded-xl text-sm text-neutral-900 placeholder:text-neutral-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 focus:bg-white outline-none transition-all"
        @keyup.enter="handleReset"
      />
    </div>

    <!-- Submit -->
    <button
      :disabled="localLoading"
      class="w-full h-12 bg-brand-600 hover:bg-brand-700 disabled:bg-neutral-200 disabled:text-neutral-400 text-white font-bold rounded-xl transition-all active:scale-95 flex items-center justify-center gap-2"
      @click="handleReset"
    >
      <svg v-if="localLoading" class="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
      <span>{{ localLoading ? '提交中...' : '重置密码' }}</span>
    </button>

    <!-- Back -->
    <div class="text-center">
      <button class="text-sm text-neutral-400 hover:text-neutral-600 transition-colors" @click="$emit('back')">
        ← 返回
      </button>
    </div>
  </div>
</template>
