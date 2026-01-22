<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useCompany } from '../composables/useCompany'
import { useCompanyStore } from '../stores/company'
import PublicTopNav from '../components/PublicTopNav.vue'
import PublicFooter from '../components/PublicFooter.vue'
import CompanyHeader from '../components/company/CompanyHeader.vue'
import CompanySidebar from '../components/company/CompanySidebar.vue'
import { Package, ShoppingBag, Info, ChevronRight, Globe } from 'lucide-vue-next'
import CompanySkeleton from '../components/company/CompanySkeleton.vue'
import EmptyState from '../components/common/EmptyState.vue'

const route = useRoute()
const companyStore = useCompanyStore()

const { loading, error, profile, company, supplies, requirements, loadProfile } = useCompany()
const activeTab = ref<'supplies' | 'requirements'>('supplies')

const tabs = [
  { id: 'supplies' as const, label: '在售货源', icon: Package, count: supplies.value.length },
  { id: 'requirements' as const, label: '采购需求', icon: ShoppingBag, count: requirements.value.length }
]

function formatTime(time?: string) {
  if (!time) return '-'
  return new Date(time).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

async function loadCompanyProfile() {
  const id = route.params.id
  if (!id) {
    error.value = '无效的公司ID'
    loading.value = false
    return
  }
  
  const companyId = Number(id)
  const cached = companyStore.activeProfile(companyId)
  
  if (cached) {
    profile.value = cached
  } else {
    await loadProfile(companyId)
    if (profile.value) {
      companyStore.profiles.set(companyId, profile.value)
    }
  }
}

onMounted(loadCompanyProfile)
</script>

<template>
  <div class="bg-gray-50 min-h-screen flex flex-col">
    <PublicTopNav />

    <main v-if="loading" class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 w-full">
      <CompanySkeleton type="profile" />
    </main>

    <main v-else-if="company" class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 w-full">
      <CompanyHeader
        :company-name="company.companyName"
        :verified="true"
        :province="company.province"
        :city="company.city"
        :district="company.district"
        :create-time="company.createTime"
        :legal-person="company.legalPerson"
      />

      <!-- Main Content Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Left Column: Business Data -->
        <div class="lg:col-span-2 space-y-8">
          <!-- Business Tabs -->
          <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
            <div class="flex border-b border-gray-50">
              <button
                v-for="tab in tabs"
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
                <div v-if="supplies.length === 0" class="py-8">
                  <EmptyState
                    :icon="Package"
                    title="暂无在售货源"
                    description="该企业暂时没有在售的货源信息"
                  />
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
                <div v-if="requirements.length === 0" class="py-8">
                  <EmptyState
                    :icon="ShoppingBag"
                    title="暂无采购需求"
                    description="该企业暂时没有采购需求信息"
                  />
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
            
            <div class="bg-gray-50 p-4 rounded-xl flex items-center gap-4">
              <div class="w-10 h-10 bg-white rounded-xl flex items-center justify-center shadow-sm">
                <Globe :size="18" class="text-blue-600" aria-hidden="true" />
              </div>
              <div>
                <div class="text-[10px] font-bold text-gray-400 uppercase">信用代码</div>
                <div class="text-sm font-bold text-gray-900 truncate w-40">{{ company.licenseNo || '-' }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Column: Sidebar -->
        <div class="space-y-8">
          <CompanySidebar
            :address="company.address"
            :legal-person="company.legalPerson"
            :license-no="company.licenseNo"
            :business-scope="company.businessScope"
            :lng="company.lng"
            :lat="company.lat"
          />
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

