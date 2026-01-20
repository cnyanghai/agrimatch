<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { useUiStore } from '../store/ui'
import { LogOut, LayoutDashboard, Settings, ChevronDown, ShoppingBag, Truck } from 'lucide-vue-next'
import { getProductTree, type ProductNode } from '../api/product'
import { listTopCompanies, type CompanyCardResponse } from '../api/company'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const ui = useUiStore()

const isLoggedIn = computed(() => Boolean(auth.me))
const displayName = computed(() => auth.me?.nickName || auth.me?.userName || '未登录')
const avatarChar = computed(() => {
  const name = (auth.me?.nickName || auth.me?.userName || '').trim()
  if (!name) return 'U'
  // 若是纯数字账号（手机号等），不要显示“1/2/3”，统一兜底 U
  if (/^\d+$/.test(name)) return 'U'
  return (name[0] || 'U').toUpperCase()
})

function go(path: string, query?: any) {
  router.push({ path, query })
}

function openLogin() {
  ui.openAuthDialog('login', { path: route.path, query: route.query as any })
}

async function onLogout() {
  await auth.logout()
  go('/')
}

// Mega Menu Data
const categoryTree = ref<ProductNode[]>([])
const topSuppliers = ref<CompanyCardResponse[]>([])
const topBuyers = ref<CompanyCardResponse[]>([])

const topCategories = computed(() => {
  return categoryTree.value.filter(cat => (cat.parentId ?? 0) === 0)
})

function buildGroups(cat: ProductNode) {
  const seconds = cat.children ?? []
  return seconds.map((s) => ({
    title: s.name,
    titleNode: s,
    items: s.children ?? []
  }))
}

onMounted(async () => {
  // 异步加载菜单数据
  const [catRes, supRes, buyRes] = await Promise.all([
    getProductTree(),
    listTopCompanies('supplier', 50),
    listTopCompanies('buyer', 50)
  ])
  if (catRes.code === 0) categoryTree.value = catRes.data ?? []
  if (supRes.code === 0) topSuppliers.value = supRes.data ?? []
  if (buyRes.code === 0) topBuyers.value = buyRes.data ?? []
})
</script>

<template>
  <nav class="bg-white border-b sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="h-16 flex items-center justify-between gap-6">
        <!-- Left: Logo & Dropdowns -->
        <div class="flex items-center gap-8">
          <div class="flex items-center gap-3 cursor-pointer hover:bg-gray-50/50 px-2 py-1 rounded-xl transition-all active:scale-[0.99]" @click="go('/')">
            <div class="w-9 h-9 rounded-xl bg-emerald-600 text-white flex items-center justify-center font-black">A</div>
            <div class="leading-tight hidden sm:block">
              <div class="font-bold text-gray-900">AgriMatch</div>
              <div class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">农汇通 · 供需匹配平台</div>
            </div>
          </div>

          <!-- Dropdowns -->
          <div class="hidden lg:flex items-center gap-4">
            <!-- 产品分类 -->
            <el-dropdown trigger="hover" popper-class="mega-menu-popper">
              <button class="flex items-center gap-1 text-sm font-bold text-gray-700 hover:text-emerald-600 transition-colors">
                产品分类 <ChevronDown :size="14" />
              </button>
              <template #dropdown>
                <div class="p-6 w-[800px] max-h-[600px] overflow-y-auto">
                  <div class="grid grid-cols-4 gap-8">
                    <div v-for="cat in topCategories" :key="cat.id" class="space-y-4">
                      <div class="font-bold text-emerald-700 border-b border-emerald-50 pb-2 flex items-center justify-between group cursor-pointer" @click="go('/hall/supply', { categoryId: cat.id })">
                        {{ cat.name }}
                        <span class="text-[10px] opacity-0 group-hover:opacity-100 transition-opacity">查看全部</span>
                      </div>
                      <div v-for="group in buildGroups(cat)" :key="group.title" class="space-y-2">
                        <div class="text-xs font-bold text-gray-900 cursor-pointer hover:text-emerald-600" @click="go('/hall/supply', { categoryId: group.titleNode.id })">
                          {{ group.title }}
                        </div>
                        <ul class="space-y-1">
                          <li v-for="item in group.items" :key="item.id">
                            <button class="text-[11px] text-gray-500 hover:text-emerald-600 transition-colors" @click="go('/hall/supply', { categoryId: item.id })">
                              {{ item.name }}
                            </button>
                          </li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </el-dropdown>

            <!-- 供应商 -->
            <el-dropdown trigger="hover" popper-class="mega-menu-popper">
              <button class="flex items-center gap-1 text-sm font-bold text-gray-700 hover:text-emerald-600 transition-colors">
                供应商 <ChevronDown :size="14" />
              </button>
              <template #dropdown>
                <div class="p-6 w-[720px]">
                  <div class="flex items-center justify-between mb-6">
                    <div class="flex items-center gap-2 text-emerald-700">
                      <Truck :size="18" />
                      <span class="font-bold text-lg">50 强制售商</span>
                    </div>
                  </div>
                  
                  <div class="grid grid-cols-3 gap-x-8 gap-y-2 mb-8">
                    <button 
                      v-for="s in topSuppliers" 
                      :key="s.id" 
                      class="text-left text-xs text-gray-600 hover:text-emerald-600 hover:font-bold transition-all truncate py-1"
                      @click="go(`/companies/${s.id}`)"
                    >
                      {{ s.companyName }}
                    </button>
                  </div>

                  <div class="border-t pt-4">
                    <button 
                      class="w-full bg-emerald-50 hover:bg-emerald-100 text-emerald-700 py-3 rounded-xl font-bold text-sm transition-all flex items-center justify-center gap-2"
                      @click="go('/companies/directory', { type: 'supplier' })"
                    >
                      所有制造商
                    </button>
                  </div>
                </div>
              </template>
            </el-dropdown>

            <!-- 采购商 -->
            <el-dropdown trigger="hover" popper-class="mega-menu-popper">
              <button class="flex items-center gap-1 text-sm font-bold text-gray-700 hover:text-emerald-600 transition-colors">
                采购商 <ChevronDown :size="14" />
              </button>
              <template #dropdown>
                <div class="p-6 w-[720px]">
                  <div class="flex items-center justify-between mb-6">
                    <div class="flex items-center gap-2 text-blue-700">
                      <ShoppingBag :size="18" />
                      <span class="font-bold text-lg">50 强采购商</span>
                    </div>
                  </div>
                  
                  <div class="grid grid-cols-3 gap-x-8 gap-y-2 mb-8">
                    <button 
                      v-for="b in topBuyers" 
                      :key="b.id" 
                      class="text-left text-xs text-gray-600 hover:text-blue-600 hover:font-bold transition-all truncate py-1"
                      @click="go(`/companies/${b.id}`)"
                    >
                      {{ b.companyName }}
                    </button>
                  </div>

                  <div class="border-t pt-4">
                    <button 
                      class="w-full bg-blue-50 hover:bg-blue-100 text-blue-700 py-3 rounded-xl font-bold text-sm transition-all flex items-center justify-center gap-2"
                      @click="go('/companies/directory', { type: 'buyer' })"
                    >
                      所有采购商名录
                    </button>
                  </div>
                </div>
              </template>
            </el-dropdown>
          </div>
        </div>

        <!-- Right: Nav & User -->
        <div class="flex items-center gap-6">
          <div class="hidden md:flex items-center gap-5 text-sm font-bold text-gray-500">
            <button class="hover:text-emerald-600 transition-all active:scale-95" @click="go('/hall/supply')">供应大厅</button>
            <button class="hover:text-emerald-600 transition-all active:scale-95" @click="go('/hall/need')">采购大厅</button>
            <button class="hover:text-emerald-600 transition-all active:scale-95" @click="go('/talks')">话题广场</button>
          </div>

          <div class="flex items-center gap-3">
            <slot name="actions" />
            <template v-if="!isLoggedIn">
              <button class="bg-emerald-600 text-white px-5 py-2 rounded-full font-bold hover:bg-emerald-700 transition-all active:scale-95 text-sm" @click="openLogin">
                登录/注册
              </button>
            </template>
            <template v-else>
              <el-dropdown>
                <button class="flex items-center gap-3 bg-gray-50 border border-gray-100 px-3 py-2 rounded-full hover:bg-gray-50/50 transition-all active:scale-95">
                  <div class="w-8 h-8 rounded-full bg-emerald-600 text-white flex items-center justify-center font-bold text-sm">
                    {{ avatarChar }}
                  </div>
                  <div class="text-left leading-tight hidden sm:block">
                    <div class="text-sm font-bold text-gray-900">{{ displayName }}</div>
                    <div class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">已登录</div>
                  </div>
                </button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="go('/console')">
                      <div class="flex items-center gap-2">
                        <LayoutDashboard :size="16" :stroke-width="2" />
                        控制台
                      </div>
                    </el-dropdown-item>
                    <el-dropdown-item @click="go('/profile')">
                      <div class="flex items-center gap-2">
                        <Settings :size="16" :stroke-width="2" />
                        个人资料
                      </div>
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="onLogout">
                      <div class="flex items-center gap-2 text-red-600">
                        <LogOut :size="16" :stroke-width="2" />
                        退出登录
                      </div>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>


<style>
.mega-menu-popper {
  --el-dropdown-menu-box-shadow: 0 20px 60px rgba(0,0,0,0.15);
  border-radius: 16px !important;
  border: none !important;
}
</style>



