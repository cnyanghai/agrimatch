<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useAuthSteps } from '../composables/useAuthSteps'
import BrandPanel from '../components/auth/BrandPanel.vue'
import StepIndicator from '../components/auth/StepIndicator.vue'
import PhoneStep from '../components/auth/PhoneStep.vue'
import PasswordLoginStep from '../components/auth/PasswordLoginStep.vue'
import SmsLoginStep from '../components/auth/SmsLoginStep.vue'
import RegisterStep from '../components/auth/RegisterStep.vue'
import ResetPasswordStep from '../components/auth/ResetPasswordStep.vue'
import { CheckCircle } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()

const {
  currentStep,
  phone,
  loading,
  stepIndex,
  transitionName,
  submitPhone,
  goToStep,
  goBack,
  markSuccess
} = useAuthSteps()

function handleSuccess() {
  markSuccess()
  setTimeout(() => {
    const redirect = route.query.redirect as string
    router.replace(redirect || '/console')
  }, 1500)
}

function handleResetSuccess() {
  // After password reset, go back to password login
  goToStep('PASSWORD_LOGIN')
}
</script>

<template>
  <div class="flex h-full bg-stone-50">
    <!-- Brand Panel (desktop only) -->
    <BrandPanel class="hidden lg:flex w-[45%]" />

    <!-- Form Area -->
    <div class="flex-1 flex flex-col items-center justify-center px-6 py-12">
      <!-- Mobile brand bar -->
      <div class="lg:hidden flex items-center justify-between w-full max-w-md mb-10">
        <div class="flex items-center gap-2">
          <div class="w-10 h-10 rounded-xl bg-brand-600 flex items-center justify-center">
            <span class="text-white font-black text-sm">沃谷</span>
          </div>
          <span class="text-lg font-black text-neutral-900">沃谷</span>
        </div>
        <button
          class="text-sm text-neutral-500 hover:text-neutral-700 transition-colors px-3 py-1.5 rounded-lg hover:bg-neutral-100"
          @click="router.push('/')"
        >
          返回首页
        </button>
      </div>
      <!-- Desktop back link -->
      <div class="hidden lg:block w-full max-w-md mb-6">
        <button
          class="text-sm text-neutral-500 hover:text-neutral-700 transition-colors flex items-center gap-1"
          @click="router.push('/')"
        >
          ← 返回首页
        </button>
      </div>

      <div class="w-full max-w-md">
        <StepIndicator :current="stepIndex" :total="3" />

        <Transition :name="transitionName" mode="out-in">
          <!-- Phone Step -->
          <PhoneStep
            v-if="currentStep === 'PHONE'"
            key="phone"
            :loading="loading"
            @submit="submitPhone"
          />

          <!-- Password Login Step -->
          <PasswordLoginStep
            v-else-if="currentStep === 'PASSWORD_LOGIN'"
            key="password"
            :phone="phone"
            :loading="loading"
            @success="handleSuccess"
            @switch-to-sms="goToStep('SMS_LOGIN')"
            @forgot-password="goToStep('RESET_PASSWORD')"
            @back="goBack"
          />

          <!-- SMS Login Step -->
          <SmsLoginStep
            v-else-if="currentStep === 'SMS_LOGIN'"
            key="sms"
            :phone="phone"
            :loading="loading"
            @success="handleSuccess"
            @switch-to-password="goToStep('PASSWORD_LOGIN')"
            @back="goBack"
          />

          <!-- Register Step -->
          <RegisterStep
            v-else-if="currentStep === 'REGISTER'"
            key="register"
            :phone="phone"
            :loading="loading"
            @success="handleSuccess"
            @back="goBack"
          />

          <!-- Reset Password Step -->
          <ResetPasswordStep
            v-else-if="currentStep === 'RESET_PASSWORD'"
            key="reset"
            :phone="phone"
            :loading="loading"
            @success="handleResetSuccess"
            @back="goBack"
          />

          <!-- Success -->
          <div v-else-if="currentStep === 'SUCCESS'" key="success" class="text-center py-12">
            <div class="w-20 h-20 mx-auto mb-6 rounded-full bg-brand-100 flex items-center justify-center success-check-enter-active">
              <CheckCircle class="text-brand-600" :size="40" :stroke-width="2" />
            </div>
            <h2 class="text-2xl font-black text-neutral-900 mb-2">登录成功</h2>
            <p class="text-sm text-neutral-500">正在为您跳转...</p>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>
