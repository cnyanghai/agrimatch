<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCompanyProfile, type CompanyProfileResponse } from '../api/company'
import PublicTopNav from '../components/PublicTopNav.vue'
import PublicFooter from '../components/PublicFooter.vue'
import { 
  MapPin, Phone, User, Globe, ShieldCheck, 
  Package, ShoppingBag, Info, ChevronRight, 
  MessageCircle, ExternalLink, Calendar
} from 'lucide-vue-next'
import { ElMessage } from 'element-plus'

const route = useRoute()
const companyId = Number(route.params.id)

const profile = ref<CompanyProfileResponse | null>(null)
const loading = ref(true)
const activeTab = ref('supplies')

async function loadProfile() {
  loading.value = true
  try {
    const res = await getCompanyProfile(companyId)
    if (res.code === 0 && res.data) {
      profile.value = res.data
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const company = computed(() => profile.value?.company)
const supplies = computed(() => profile.value?.supplies ?? [])
const requirements = computed(() => profile.value?.requirements ?? [])

function formatTime(time?: string) {
  if (!time) return '-'
  return new Date(time).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

onMounted(loadProfile)
</script>

<template>
  <div class="bg-gray-50 min-h-screen flex flex-col">
    <PublicTopNav />

    <main v-if="loading" class="flex-1 max-w-7xl mx-auto px-4 py-12 w-full">
      <div class="h-64 bg-white rounded-xl animate-pulse mb-8"></div>
      <div class="grid grid-cols-3 gap-8">
        <div class="col-span-2 space-y-6">
          <div v-for="i in 3" :key="i" class="h-32 bg-white rounded-xl animate-pulse"></div>
        </div>
        <div class="h-96 bg-white rounded-xl animate-pulse"></div>
      </div>
    </main>

    <main v-else-if="company" class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 w-full">
      <!-- Profile Header (Octopart Style) -->
      <div class="bg-slate-900 rounded-[32px] overflow-hidden shadow-2xl mb-12 text-white relative">
        <!-- Background Pattern -->
        <div class="absolute inset-0 opacity-10 pointer-events-none">
          <div class="absolute top-0 right-0 w-96 h-96 bg-brand-500 rounded-full blur-[120px] -translate-y-1/2 translate-x-1/2"></div>
          <div class="absolute bottom-0 left-0 w-64 h-64 bg-blue-500 rounded-full blur-[100px] translate-y-1/2 -translate-x-1/2"></div>
        </div>

        <div class="relative z-10 p-8 md:p-12 flex flex-col md:flex-row gap-10 items-start">
          <!-- Logo -->
          <div class="w-32 h-32 bg-white rounded-xl flex items-center justify-center text-4xl font-black text-slate-900 shadow-md shrink-0 transition-transform hover:scale-105">
            {{ company.companyName[0] }}
          </div>

          <!-- Basic Info -->
          <div class="flex-1 min-w-0">
            <div class="flex flex-wrap items-center gap-4 mb-4">
              <h1 class="text-3xl md:text-4xl font-extrabold tracking-tight">{{ company.companyName }}</h1>
              <div class="flex items-center gap-1.5 bg-brand-500/20 text-brand-400 px-3 py-1 rounded-full text-xs font-bold border border-brand-500/20">
                <ShieldCheck :size="14" />
                资质已核验
              </div>
            </div>

            <div class="flex flex-wrap gap-x-8 gap-y-4 text-slate-400 text-sm mb-8">
              <div class="flex items-center gap-2">
                <MapPin :size="16" class="text-brand-500" />
                {{ company.province }} · {{ company.city }} · {{ company.district }}
              </div>
              <div class="flex items-center gap-2">
                <Calendar :size="16" class="text-blue-500" />
                入驻时间：{{ formatTime(company.createTime) }}
              </div>
              <div v-if="company.contacts" class="flex items-center gap-2">
                <User :size="16" class="text-purple-500" />
                联系人：{{ company.contacts }}
              </div>
            </div>

            <div class="flex flex-wrap gap-4">
              <button class="bg-brand-600 hover:bg-brand-700 text-white px-8 py-3 rounded-xl font-bold transition-all  flex items-center gap-2">
                <MessageCircle :size="18" />
                立即咨询
              </button>
              <button class="bg-white/10 hover:bg-white/20 border border-white/10 px-8 py-3 rounded-xl font-bold transition-all  flex items-center gap-2">
                <ExternalLink :size="18" />
                分享主页
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Content Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Left Column: Business Data -->
        <div class="lg:col-span-2 space-y-8">
          <!-- Business Tabs -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
            <div class="flex border-b border-gray-50">
              <button
                v-for="tab in [{id:'supplies', label:'在售货源', icon: Package, count: supplies.length}, {id:'requirements', label:'采购需求', icon: ShoppingBag, count: requirements.length}]"
                :key="tab.id"
                class="flex-1 flex items-center justify-center gap-2 py-5 font-bold text-sm transition-all relative"
                :class="activeTab === tab.id ? 'text-brand-600' : 'text-gray-400 hover:text-gray-600'"
                @click="activeTab = tab.id"
              >
                <component :is="tab.icon" :size="18" />
                {{ tab.label }}
                <span v-if="tab.count > 0" class="ml-1.5 bg-gray-100 text-gray-500 px-2 py-0.5 rounded-full text-[10px]">{{ tab.count }}</span>
                <div v-if="activeTab === tab.id" class="absolute bottom-0 left-0 right-0 h-1 bg-brand-600 rounded-t-full"></div>
              </button>
            </div>

            <div class="p-6">
              <!-- Supplies List -->
              <div v-if="activeTab === 'supplies'" class="space-y-4">
                <div v-if="supplies.length === 0" class="py-20 text-center text-gray-400">
                  <Package :size="48" class="mx-auto mb-4 opacity-20" />
                  <p>暂无在售货源</p>
                </div>
                <div
                  v-for="s in supplies"
                  :key="s.id"
                  class="flex items-center gap-6 p-4 rounded-xl border border-gray-50 hover:border-brand-100 hover:bg-brand-50/10 transition-all group"
                >
                  <div class="w-16 h-16 bg-gray-50 rounded-xl flex items-center justify-center font-bold text-gray-400 group-hover:bg-white transition-colors">
                    {{ s.categoryName[0] }}
                  </div>
                  <div class="flex-1 min-w-0">
                    <h4 class="font-bold text-gray-900 mb-1 group-hover:text-brand-600">{{ s.categoryName }}</h4>
                    <div class="flex gap-4 text-xs text-gray-400">
                      <span>库存：{{ s.quantity }} 吨</span>
                      <span>产地：{{ s.origin || '-' }}</span>
                    </div>
                  </div>
                  <div class="text-right shrink-0">
                    <div class="text-lg font-black text-red-600 italic">¥{{ s.exFactoryPrice }}</div>
                    <div class="text-[10px] text-gray-400">出厂价</div>
                  </div>
                  <ChevronRight :size="20" class="text-gray-300" />
                </div>
              </div>

              <!-- Requirements List -->
              <div v-if="activeTab === 'requirements'" class="space-y-4">
                <div v-if="requirements.length === 0" class="py-20 text-center text-gray-400">
                  <ShoppingBag :size="48" class="mx-auto mb-4 opacity-20" />
                  <p>暂无采购需求</p>
                </div>
                <div
                  v-for="r in requirements"
                  :key="r.id"
                  class="flex items-center gap-6 p-4 rounded-xl border border-gray-50 hover:border-blue-100 hover:bg-blue-50/10 transition-all group"
                >
                  <div class="w-16 h-16 bg-gray-50 rounded-xl flex items-center justify-center font-bold text-gray-400 group-hover:bg-white transition-colors">
                    {{ r.categoryName[0] }}
                  </div>
                  <div class="flex-1 min-w-0">
                    <h4 class="font-bold text-gray-900 mb-1 group-hover:text-blue-600">{{ r.categoryName }}</h4>
                    <div class="flex gap-4 text-xs text-gray-400">
                      <span>需求量：{{ r.quantity }} 吨</span>
                      <span>交货地：{{ r.purchaseAddress || '-' }}</span>
                    </div>
                  </div>
                  <div class="text-right shrink-0">
                    <div class="text-xs font-bold text-blue-600 bg-blue-50 px-3 py-1 rounded-full">采购中</div>
                    <div class="text-[10px] text-gray-400 mt-2">{{ formatTime(r.createTime) }} 发布</div>
                  </div>
                  <ChevronRight :size="20" class="text-gray-300" />
                </div>
              </div>
            </div>
          </div>

          <!-- Company Details (About) -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-8">
            <h3 class="text-xl font-extrabold text-gray-900 mb-6 flex items-center gap-2">
              <Info :size="20" class="text-brand-600" />
              企业档案
            </h3>
            <div class="prose prose-sm max-w-none text-gray-600 leading-relaxed mb-8">
              {{ company.address || '该企业尚未填写详细地址信息。' }}
            </div>
            
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div class="bg-gray-50 p-4 rounded-xl flex items-center gap-4">
                <div class="w-10 h-10 bg-white rounded-xl flex items-center justify-center shadow-sm">
                  <Phone :size="18" class="text-brand-600" />
                </div>
                <div>
                  <div class="text-[10px] font-bold text-gray-400 uppercase">联系电话</div>
                  <div class="text-sm font-bold text-gray-900">{{ company.phone || '-' }}</div>
                </div>
              </div>
              <div class="bg-gray-50 p-4 rounded-xl flex items-center gap-4">
                <div class="w-10 h-10 bg-white rounded-xl flex items-center justify-center shadow-sm">
                  <Globe :size="18" class="text-blue-600" />
                </div>
                <div>
                  <div class="text-[10px] font-bold text-gray-400 uppercase">信用代码</div>
                  <div class="text-sm font-bold text-gray-900 truncate w-40">{{ company.licenseNo || '-' }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Column: Sidebar -->
        <div class="space-y-8">
          <!-- Location Card -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
            <div class="p-6 border-b border-gray-50">
              <h3 class="font-bold text-gray-900 flex items-center gap-2">
                <MapPin :size="18" class="text-red-500" />
                公司位置
              </h3>
            </div>
            <div class="aspect-square bg-gray-100 flex items-center justify-center text-gray-400 italic p-8 text-center text-sm">
              <div class="space-y-4">
                <MapPin :size="48" class="mx-auto opacity-20" />
                <p>{{ company.address }}</p>
                <div class="text-[10px] bg-white text-gray-500 px-3 py-1 rounded-full shadow-sm">
                  经度: {{ company.lng?.toFixed(4) }} | 纬度: {{ company.lat?.toFixed(4) }}
                </div>
              </div>
            </div>
          </div>

          <!-- Trust Factors -->
          <div class="bg-gradient-to-br from-slate-800 to-slate-900 rounded-xl p-8 text-white shadow-md">
            <h3 class="font-bold mb-6 flex items-center gap-2">
              <ShieldCheck :size="18" class="text-brand-400" />
              信用保障
            </h3>
            <ul class="space-y-6">
              <li v-for="item in [
                {title: '实名认证', desc: '该企业已完成法人身份实名核验'},
                {title: '资质核实', desc: '营业执照及行业经营许可证已备案'},
                {title: '平台直签', desc: '支持在线签署具备法律效力的合同'}
              ]" :key="item.title" class="flex gap-4">
                <div class="w-1.5 h-1.5 bg-brand-400 rounded-full mt-2 shadow-[0_0_8px_rgba(52,211,153,0.6)]"></div>
                <div>
                  <div class="text-sm font-bold">{{ item.title }}</div>
                  <div class="text-xs text-slate-400 mt-1">{{ item.desc }}</div>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </main>

    <PublicFooter />
  </div>
</template>

<style scoped>
.hero-gradient {
  background: radial-gradient(circle at 50% -20%, #065f46 0%, #022c22 100%);
}
</style>

