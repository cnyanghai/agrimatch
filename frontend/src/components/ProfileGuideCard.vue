<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Building2, User, MapPin, ArrowRight, Sparkles } from 'lucide-vue-next'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const auth = useAuthStore()

// 检测资料完善度
const profileStatus = computed(() => {
  const me = auth.me
  if (!me) return null

  const issues: string[] = []

  // 检查公司ID（通过 companyId 判断是否已设置公司）
  if (!me.companyId) {
    issues.push('公司信息')
  }

  // 检查联系人
  if (!me.nickName || me.nickName === me.phonenumber) {
    issues.push('联系人姓名')
  }

  // 检查手机号
  if (!me.phonenumber) {
    issues.push('手机号码')
  }

  return {
    isComplete: issues.length === 0,
    missingFields: issues,
    completionRate: Math.round(((3 - issues.length) / 3) * 100)
  }
})

// 是否显示引导卡片
const showGuide = computed(() => {
  return profileStatus.value && !profileStatus.value.isComplete
})

function goToProfile() {
  router.push('/profile')
}
</script>

<template>
  <div v-if="showGuide" class="profile-guide-card">
    <div class="bg-gradient-to-br from-amber-50 to-orange-50 rounded-xl border border-amber-200 p-5 relative overflow-hidden">
      <!-- 装饰背景 -->
      <div class="absolute top-0 right-0 w-32 h-32 bg-amber-100/50 rounded-full -translate-y-1/2 translate-x-1/2" />
      <div class="absolute bottom-0 left-0 w-24 h-24 bg-orange-100/50 rounded-full translate-y-1/2 -translate-x-1/2" />
      
      <div class="relative">
        <!-- 标题 -->
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-xl bg-amber-100 border border-amber-200 flex items-center justify-center">
            <Sparkles class="text-amber-600" :size="20" :stroke-width="2" />
          </div>
          <div>
            <div class="text-sm font-bold text-gray-900">完善您的资料</div>
            <div class="text-xs text-gray-500">解锁全部功能，提升可信度</div>
          </div>
        </div>

        <!-- 进度条 -->
        <div class="mb-4">
          <div class="flex items-center justify-between mb-1">
            <span class="text-xs font-medium text-gray-500">资料完善度</span>
            <span class="text-xs font-bold text-amber-600">{{ profileStatus?.completionRate }}%</span>
          </div>
          <div class="h-2 bg-amber-100 rounded-full overflow-hidden">
            <div 
              class="h-full bg-gradient-to-r from-amber-400 to-orange-400 rounded-full transition-all duration-500"
              :style="{ width: `${profileStatus?.completionRate}%` }"
            />
          </div>
        </div>

        <!-- 待完善项目 -->
        <div class="space-y-2 mb-4">
          <div 
            v-for="field in profileStatus?.missingFields" 
            :key="field"
            class="flex items-center gap-2 p-2 bg-white/60 rounded-lg"
          >
            <div class="w-5 h-5 rounded-full bg-amber-100 flex items-center justify-center">
              <component 
                :is="field === '公司名称' ? Building2 : field === '联系人姓名' ? User : MapPin"
                class="text-amber-600"
                :size="12"
                :stroke-width="2"
              />
            </div>
            <span class="text-xs font-medium text-gray-600">{{ field }}</span>
            <span class="text-xs text-amber-600 ml-auto">待完善</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <button 
          class="w-full flex items-center justify-center gap-2 px-4 py-2.5 bg-gradient-to-r from-amber-500 to-orange-500 hover:from-amber-600 hover:to-orange-600 text-white rounded-xl font-bold text-sm transition-all active:scale-[0.98] shadow-md shadow-amber-500/20"
          @click="goToProfile"
        >
          立即完善
          <ArrowRight :size="16" :stroke-width="2" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-guide-card {
  animation: slideUp 0.4s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

