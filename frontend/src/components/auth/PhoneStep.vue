<script setup lang="ts">
import { ref, computed } from 'vue'
import { Phone } from 'lucide-vue-next'

defineProps<{
  loading: boolean
}>()

const emit = defineEmits<{
  submit: [phone: string]
}>()

const phone = ref('')
const isValid = computed(() => /^1\d{10}$/.test(phone.value))

function handleSubmit() {
  if (isValid.value) {
    emit('submit', phone.value)
  }
}
</script>

<template>
  <div class="space-y-6">
    <div class="text-center">
      <h2 class="text-2xl font-black text-neutral-900 mb-2">欢迎使用沃谷</h2>
      <p class="text-sm text-neutral-500">请输入您的手机号码开始</p>
    </div>

    <div>
      <div class="relative">
        <div class="absolute left-4 top-1/2 -translate-y-1/2 text-neutral-400">
          <Phone :size="18" :stroke-width="2" />
        </div>
        <input
          v-model="phone"
          type="tel"
          maxlength="11"
          placeholder="请输入手机号"
          class="w-full h-12 pl-11 pr-4 bg-neutral-50 border border-neutral-200 rounded-xl text-sm text-neutral-900 placeholder:text-neutral-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 focus:bg-white outline-none transition-all"
          @keyup.enter="handleSubmit"
        />
      </div>
      <p
        v-if="phone.length > 0 && phone.length < 11"
        class="text-xs text-red-500 mt-2 ml-1"
      >
        请输入11位手机号
      </p>
    </div>

    <button
      :disabled="!isValid || loading"
      class="w-full h-12 bg-brand-600 hover:bg-brand-700 disabled:bg-neutral-200 disabled:text-neutral-400 text-white font-bold rounded-xl transition-all active:scale-95 flex items-center justify-center gap-2"
      @click="handleSubmit"
    >
      <svg v-if="loading" class="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
      <span>{{ loading ? '检查中...' : '继续' }}</span>
    </button>
  </div>
</template>
