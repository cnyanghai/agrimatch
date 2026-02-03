<script setup lang="ts">
import { FileText, Users, FileSignature, PenTool, MessageCircle, Gift } from 'lucide-vue-next'
import logoWhite from '../../assets/logo-white.svg'

const features = [
  { icon: FileText, label: '供求发布', desc: '一键发布供应与采购信息', color: 'from-emerald-400/20 to-emerald-500/10' },
  { icon: Users, label: '社区交流', desc: '行业话题深度探讨', color: 'from-sky-400/20 to-sky-500/10' },
  { icon: FileSignature, label: '智能合同', desc: 'AI 驱动合同自动生成', color: 'from-violet-400/20 to-violet-500/10' },
  { icon: PenTool, label: '在线签约', desc: '电子签章安全便捷', color: 'from-amber-400/20 to-amber-500/10' },
  { icon: MessageCircle, label: '即时沟通', desc: '站内聊天实时议价', color: 'from-rose-400/20 to-rose-500/10' },
  { icon: Gift, label: '积分兑换', desc: '活跃奖励兑好礼', color: 'from-orange-400/20 to-orange-500/10' },
]
</script>

<template>
  <div class="relative flex flex-col items-center justify-center h-full bg-gradient-to-br from-brand-700 via-brand-600 to-emerald-700 text-white overflow-hidden select-none">
    <!-- Decorative elements -->
    <div class="absolute -top-24 -left-24 w-72 h-72 rounded-full bg-white/[0.03] animate-float" />
    <div class="absolute -bottom-36 -right-20 w-96 h-96 rounded-full bg-white/[0.03] animate-float" style="animation-delay: 1.5s" />
    <div class="absolute top-1/4 right-6 w-44 h-44 rounded-full bg-white/[0.03] animate-float" style="animation-delay: 3s" />
    <!-- Subtle grid pattern -->
    <div class="absolute inset-0 opacity-[0.03]" style="background-image: radial-gradient(circle, #fff 1px, transparent 1px); background-size: 32px 32px;" />

    <!-- Content -->
    <div class="relative z-10 flex flex-col items-center px-10 w-full max-w-md">
      <!-- Logo + Brand -->
      <div class="flex items-center gap-3 mb-2">
        <img :src="logoWhite" alt="沃谷" class="h-9 w-auto drop-shadow-lg" />
        <span class="text-3xl font-black tracking-tight drop-shadow-sm">沃谷</span>
      </div>
      <p class="text-white/50 text-xs tracking-widest uppercase mb-12">农牧供需智能匹配平台</p>

      <!-- Feature cards -->
      <div class="grid grid-cols-2 gap-3 w-full">
        <div
          v-for="(f, i) in features"
          :key="i"
          class="feature-card group"
          :style="{ animationDelay: `${i * 100 + 300}ms` }"
        >
          <div class="feature-card__inner" :class="[`bg-gradient-to-br ${f.color}`]">
            <!-- Icon -->
            <div class="w-10 h-10 rounded-xl bg-white/10 backdrop-blur-sm flex items-center justify-center mb-3 group-hover:bg-white/20 group-hover:scale-110 transition-all duration-300">
              <component :is="f.icon" :size="20" :stroke-width="1.8" class="drop-shadow-sm" />
            </div>
            <!-- Text -->
            <div class="text-[13px] font-bold leading-tight mb-1">{{ f.label }}</div>
            <div class="text-[11px] text-white/45 leading-tight">{{ f.desc }}</div>
          </div>
        </div>
      </div>

      <!-- Bottom tagline -->
      <div class="mt-12 flex items-center gap-2 text-white/30 text-xs">
        <div class="w-8 h-px bg-white/20" />
        <span>让农牧交易更简单</span>
        <div class="w-8 h-px bg-white/20" />
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}
.animate-float {
  animation: float 6s ease-in-out infinite;
}

@keyframes feature-enter {
  0% {
    opacity: 0;
    transform: translateY(16px) scale(0.95);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.feature-card {
  opacity: 0;
  animation: feature-enter 0.5s ease-out forwards;
}

.feature-card__inner {
  padding: 16px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(8px);
  transition: all 0.3s ease;
  height: 100%;
}

.feature-card:hover .feature-card__inner {
  border-color: rgba(255, 255, 255, 0.18);
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}
</style>
