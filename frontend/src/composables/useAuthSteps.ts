import { ref, computed, type Ref, type ComputedRef } from 'vue'
import { useAuthStore } from '../store/auth'

export type AuthStep = 'PHONE' | 'PASSWORD_LOGIN' | 'SMS_LOGIN' | 'REGISTER' | 'RESET_PASSWORD' | 'SUCCESS'

const STEP_ORDER: Record<AuthStep, number> = {
  PHONE: 0,
  PASSWORD_LOGIN: 1,
  SMS_LOGIN: 1,
  REGISTER: 1,
  RESET_PASSWORD: 1,
  SUCCESS: 2
}

export interface UseAuthStepsReturn {
  currentStep: Ref<AuthStep>
  phone: Ref<string>
  direction: Ref<'forward' | 'backward'>
  loading: Ref<boolean>
  error: Ref<string>
  stepIndex: ComputedRef<number>
  transitionName: ComputedRef<string>
  submitPhone: (phone: string) => Promise<void>
  goToStep: (step: AuthStep) => void
  goBack: () => void
  markSuccess: () => void
  reset: () => void
}

export function useAuthSteps(): UseAuthStepsReturn {
  const auth = useAuthStore()

  const currentStep = ref<AuthStep>('PHONE')
  const phone = ref('')
  const direction = ref<'forward' | 'backward'>('forward')
  const loading = ref(false)
  const error = ref('')

  const stepIndex = computed(() => STEP_ORDER[currentStep.value] ?? 0)

  const transitionName = computed(() =>
    direction.value === 'forward' ? 'slide-left' : 'slide-right'
  )

  async function submitPhone(phoneVal: string) {
    error.value = ''
    loading.value = true
    try {
      phone.value = phoneVal
      const registered = await auth.checkPhone(phoneVal)
      direction.value = 'forward'
      currentStep.value = registered ? 'PASSWORD_LOGIN' : 'REGISTER'
    } catch (e: any) {
      error.value = e?.message || '检查失败'
    } finally {
      loading.value = false
    }
  }

  function goToStep(step: AuthStep) {
    direction.value = 'forward'
    currentStep.value = step
  }

  function goBack() {
    direction.value = 'backward'
    currentStep.value = 'PHONE'
  }

  function markSuccess() {
    direction.value = 'forward'
    currentStep.value = 'SUCCESS'
  }

  function reset() {
    currentStep.value = 'PHONE'
    phone.value = ''
    direction.value = 'forward'
    loading.value = false
    error.value = ''
  }

  return {
    currentStep,
    phone,
    direction,
    loading,
    error,
    stepIndex,
    transitionName,
    submitPhone,
    goToStep,
    goBack,
    markSuccess,
    reset
  }
}
