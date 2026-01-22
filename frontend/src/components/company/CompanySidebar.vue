<script setup lang="ts">
import { ref, computed } from 'vue'
import { MapPin, ShieldCheck, Globe, ChevronDown, ChevronUp } from 'lucide-vue-next'

export interface CompanySidebarProps {
  address?: string
  legalPerson?: string
  licenseNo?: string
  businessScope?: string
  lng?: number
  lat?: number
  showMap?: boolean
}

withDefaults(defineProps<CompanySidebarProps>(), {
  showMap: true
})

const isMobile = computed(() => {
  if (typeof window === 'undefined') return false
  return window.innerWidth < 1024
})

const isCollapsed = ref(!isMobile.value)

const trustFactors = [
  { title: '实名认证', desc: '该企业已完成法人身份实名核验' },
  { title: '资质核实', desc: '营业执照及行业经营许可证已备案' },
  { title: '平台直签', desc: '支持在线签署具备法律效力的合同' }
]

function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
}
</script>

<template>
  <aside class="space-y-4 lg:space-y-8" aria-label="公司侧边栏">
    <button
      v-if="isMobile"
      @click="toggleCollapse"
      class="w-full flex items-center justify-between p-4 bg-white rounded-xl shadow-sm border border-gray-200 font-bold text-gray-900 text-sm"
      :aria-expanded="!isCollapsed"
      aria-controls="sidebar-content"
    >
      <span class="flex items-center gap-2">
        <ShieldCheck :size="16" class="text-brand-600" />
        公司信息
      </span>
      <component
        :is="isCollapsed ? ChevronDown : ChevronUp"
        :size="18"
        class="text-gray-400 transition-transform"
        aria-hidden="true"
      />
    </button>

    <div
      id="sidebar-content"
      v-show="!isMobile || !isCollapsed"
      class="space-y-4 lg:space-y-8"
      :class="{ 'lg:space-y-8': !isMobile }"
    >
    <section v-if="showMap" class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden" aria-labelledby="map-heading">
      <div class="p-6 border-b border-gray-50">
        <h3 id="map-heading" class="font-bold text-gray-900 flex items-center gap-2">
          <MapPin :size="18" class="text-red-500" aria-hidden="true" />
          公司位置
        </h3>
      </div>
      <div 
        class="aspect-square bg-gray-100 flex items-center justify-center text-gray-400 italic p-8 text-center text-sm"
        role="img"
        :aria-label="`公司位置: ${address || '暂无地址信息'}`"
      >
        <div class="space-y-4">
          <MapPin :size="48" class="mx-auto opacity-20" aria-hidden="true" />
          <p>{{ address || '暂无地址信息' }}</p>
          <div v-if="lng || lat" class="text-[10px] bg-white text-gray-500 px-3 py-1 rounded-full shadow-sm">
            经度: {{ lng?.toFixed(4) }} | 纬度: {{ lat?.toFixed(4) }}
          </div>
        </div>
      </div>
    </section>

    <section class="bg-white rounded-xl shadow-sm border border-gray-200 p-6" aria-labelledby="contact-heading">
      <h3 id="contact-heading" class="font-bold text-gray-900 mb-4">公司信息</h3>
      <div class="space-y-4">
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 bg-gray-50 rounded-lg flex items-center justify-center">
            <ShieldCheck :size="16" class="text-brand-600" aria-hidden="true" />
          </div>
          <div class="text-sm">
            <div class="text-[10px] text-gray-400 uppercase font-bold">法人</div>
            <div class="font-bold text-gray-900">{{ legalPerson || '-' }}</div>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 bg-gray-50 rounded-lg flex items-center justify-center">
            <Globe :size="16" class="text-blue-600" aria-hidden="true" />
          </div>
          <div class="text-sm">
            <div class="text-[10px] text-gray-400 uppercase font-bold">信用代码</div>
            <div class="font-bold text-gray-900 truncate max-w-[200px]" :title="licenseNo || '-'">{{ licenseNo || '-' }}</div>
          </div>
        </div>
      </div>
    </section>

    <section v-if="businessScope" class="bg-white rounded-xl shadow-sm border border-gray-200 p-6" aria-labelledby="scope-heading">
      <h3 id="scope-heading" class="font-bold text-gray-900 mb-4">经营范围</h3>
      <p class="text-sm text-gray-600 leading-relaxed">{{ businessScope }}</p>
    </section>

    <section class="bg-gradient-to-br from-slate-800 to-slate-900 rounded-xl p-8 text-white shadow-md" aria-labelledby="trust-heading">
      <h3 id="trust-heading" class="font-bold mb-6 flex items-center gap-2">
        <ShieldCheck :size="18" class="text-brand-400" aria-hidden="true" />
        信用保障
      </h3>
      <ul class="space-y-6" role="list">
        <li v-for="item in trustFactors" :key="item.title" class="flex gap-4">
          <div class="w-1.5 h-1.5 bg-brand-400 rounded-full mt-2 shadow-[0_0_8px_rgba(52,211,153,0.6)]" aria-hidden="true"></div>
          <div>
            <div class="text-sm font-bold">{{ item.title }}</div>
            <div class="text-xs text-slate-400 mt-1">{{ item.desc }}</div>
          </div>
        </li>
      </ul>
    </section>
    </div>
  </aside>
</template>
