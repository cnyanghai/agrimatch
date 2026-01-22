<script setup lang="ts">
import { MapPin, Calendar, User, ShieldCheck, MessageCircle, ExternalLink } from 'lucide-vue-next'
import { vLazy } from '../../directives/lazyLoad'

export interface CompanyHeaderProps {
  companyName: string
  verified?: boolean
  logo?: string
  size?: 'sm' | 'md' | 'lg'
  province?: string
  city?: string
  district?: string
  createTime?: string
  legalPerson?: string
}

const emit = defineEmits<{
  consult: []
  share: []
}>()

withDefaults(defineProps<CompanyHeaderProps>(), {
  verified: false,
  size: 'lg',
  province: '',
  city: '',
  district: '',
  createTime: '',
  legalPerson: ''
})

const sizeClasses = {
  sm: 'w-12 h-12 text-xl',
  md: 'w-20 h-20 text-2xl',
  lg: 'w-32 h-32 text-4xl'
}

function formatTime(time?: string) {
  if (!time) return '-'
  return new Date(time).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}
</script>

<template>
  <header class="bg-slate-900 rounded-2xl overflow-hidden shadow-2xl mb-12 text-white relative">
    <div class="absolute inset-0 opacity-10 pointer-events-none" aria-hidden="true">
      <div class="absolute top-0 right-0 w-96 h-96 bg-brand-500 rounded-full blur-[120px] -translate-y-1/2 translate-x-1/2"></div>
      <div class="absolute bottom-0 left-0 w-64 h-64 bg-blue-500 rounded-full blur-[100px] translate-y-1/2 -translate-x-1/2"></div>
    </div>
    <div class="relative z-10 p-8 md:p-12 flex flex-col md:flex-row gap-10 items-start">
      <div
        :class="['bg-white rounded-lg flex items-center justify-center font-black text-slate-900 shadow-md shrink-0 transition-transform hover:scale-105', sizeClasses[size]]"
      >
        <template v-if="logo">
          <img v-lazy="logo" :alt="`${companyName} logo`" class="w-full h-full object-cover rounded-lg" />
        </template>
        <template v-else>
          {{ companyName[0] }}
        </template>
      </div>
      <div class="flex-1 min-w-0">
        <div class="flex flex-wrap items-center gap-4 mb-4">
          <h1 class="text-3xl md:text-4xl font-extrabold tracking-tight">
            {{ companyName }}
          </h1>
          <div v-if="verified" class="flex items-center gap-1.5 bg-brand-500/20 text-brand-400 px-3 py-1 rounded-full text-xs font-bold border border-brand-500/20">
            <ShieldCheck :size="14" aria-hidden="true" />
            资质已核验
          </div>
        </div>

        <div class="flex flex-wrap gap-x-8 gap-y-4 text-slate-400 text-sm mb-8">
          <div v-if="province || city || district" class="flex items-center gap-2">
            <MapPin :size="16" class="text-brand-500" aria-hidden="true" />
            {{ province }} · {{ city }} · {{ district }}
          </div>
          <div v-if="createTime" class="flex items-center gap-2">
            <Calendar :size="16" class="text-blue-500" aria-hidden="true" />
            入驻时间：{{ formatTime(createTime) }}
          </div>
          <div v-if="legalPerson" class="flex items-center gap-2">
            <User :size="16" class="text-purple-500" aria-hidden="true" />
            法人：{{ legalPerson }}
          </div>
        </div>

        <div class="flex flex-wrap gap-4">
          <button 
            @click="emit('consult')"
            class="bg-brand-600 hover:bg-brand-700 text-white px-8 py-3 rounded-lg font-bold transition-all flex items-center gap-2 focus:ring-2 focus:ring-brand-400 focus:outline-none focus:ring-offset-2 focus:ring-offset-slate-900"
            type="button"
            aria-label="立即咨询该公司"
          >
            <MessageCircle :size="18" aria-hidden="true" />
            立即咨询
          </button>
          <button 
            @click="emit('share')"
            class="bg-white/10 hover:bg-white/20 border border-white/10 px-8 py-3 rounded-lg font-bold transition-all flex items-center gap-2 focus:ring-2 focus:ring-white/30 focus:outline-none focus:ring-offset-2 focus:ring-offset-slate-900"
            type="button"
            aria-label="分享该公司主页"
          >
            <ExternalLink :size="18" aria-hidden="true" />
            分享主页
          </button>
        </div>
      </div>
    </div>
  </header>
</template>
