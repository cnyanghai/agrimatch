<script setup lang="ts">
import { Lock, Coins } from 'lucide-vue-next'

defineProps<{
  price?: number
}>()

const emit = defineEmits(['purchase'])
</script>

<template>
  <div class="relative py-12 px-6 overflow-hidden rounded-2xl border border-amber-100 bg-gradient-to-b from-white/50 to-amber-50/30 backdrop-blur-sm">
    <!-- 背景虚化装饰 -->
    <div class="absolute -top-24 -right-24 w-64 h-64 bg-amber-200/20 rounded-full blur-3xl"></div>
    <div class="absolute -bottom-24 -left-24 w-64 h-64 bg-brand-200/20 rounded-full blur-3xl"></div>

    <div class="relative z-10 flex flex-col items-center text-center max-w-sm mx-auto">
      <div class="w-16 h-16 bg-amber-100 rounded-2xl flex items-center justify-center text-amber-600 mb-6 shadow-inner">
        <Lock :size="32" stroke-width="2.5" />
      </div>
      
      <h3 class="text-xl font-black text-gray-900 mb-2">深度付费内容</h3>
      <p class="text-sm text-gray-500 mb-8 leading-relaxed">
        本文包含行业核心分析与专家独家见解，支付少量积分即可解锁全文阅读权限。
      </p>

      <button 
        class="group relative w-full bg-slate-900 text-white py-4 px-8 rounded-xl font-black transition-all hover:bg-slate-800 active:scale-[0.98] shadow-xl shadow-slate-900/20 overflow-hidden"
        @click="emit('purchase')"
      >
        <div class="flex items-center justify-center gap-2">
          <Coins :size="18" class="text-amber-400" />
          <span>立即支付 {{ price?.toFixed(2) }} 积分解锁</span>
        </div>
        <!-- 光效 -->
        <div class="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent -translate-x-full group-hover:animate-[shimmer_1.5s_infinite]"></div>
      </button>
      
      <p class="mt-4 text-[10px] text-gray-400 font-bold uppercase tracking-widest">
        解锁后永久有效 · 支持专家持续创作
      </p>
    </div>
  </div>
</template>

<style scoped>
@keyframes shimmer {
  100% {
    transform: translateX(100%);
  }
}
</style>

