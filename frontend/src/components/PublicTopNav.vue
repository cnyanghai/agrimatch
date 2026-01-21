<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { useUiStore } from '../store/ui'
import { LogOut, Settings, ChevronDown, ShoppingBag, Truck } from 'lucide-vue-next'
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

const activeCategoryId = ref<number | null>(null)

const topCategories = computed(() => {
  return categoryTree.value.filter(cat => (cat.parentId ?? 0) === 0)
})

const activeCategory = computed(() => {
  if (!activeCategoryId.value) return topCategories.value[0] || null
  return topCategories.value.find(c => c.id === activeCategoryId.value) || topCategories.value[0] || null
})

function buildGroups(cat: ProductNode | null) {
  if (!cat) return []
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
  if (catRes.code === 0) {
    categoryTree.value = catRes.data ?? []
    // 默认选中第一个一级分类
    const roots = categoryTree.value.filter(cat => (cat.parentId ?? 0) === 0)
    if (roots.length > 0) {
      activeCategoryId.value = roots[0]!.id
    }
  }
  if (supRes.code === 0) topSuppliers.value = supRes.data ?? []
  if (buyRes.code === 0) topBuyers.value = buyRes.data ?? []
})
</script>

<template>
  <nav class="bg-white border-b sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="min-h-[80px] py-4 flex items-center justify-between gap-6">
        <!-- Left: Logo & Dropdowns (Stacked) -->
        <div class="flex flex-col items-start gap-3">
          <div class="flex items-center gap-3 cursor-pointer hover:bg-gray-50/50 px-2 py-1 rounded-xl transition-all active:scale-[0.99]" @click="go('/')">
            <div class="w-9 h-9 rounded-lg bg-brand-600 text-white flex items-center justify-center font-black">A</div>
            <div class="leading-tight hidden sm:block">
              <div class="font-bold text-gray-900">AgriMatch</div>
              <div class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">农汇通 · 供需匹配平台</div>
            </div>
          </div>

          <!-- Dropdowns Row -->
          <div class="hidden lg:flex items-center gap-6 ml-2">
            <!-- 产品分类 -->
            <el-dropdown trigger="hover" popper-class="mega-menu-popper">
              <button class="flex items-center gap-1 text-sm font-bold text-gray-700 hover:text-brand-600 transition-colors">
                产品分类 <ChevronDown :size="14" />
              </button>
              <template #dropdown>
                <div class="flex w-[900px] h-[540px] overflow-hidden bg-white rounded-xl">
                  <!-- Sidebar: 1st Level Categories -->
                  <div class="w-64 bg-gray-50 border-r border-gray-200 py-4 overflow-y-auto">
                    <div 
                      v-for="cat in topCategories" 
                      :key="cat.id"
                      class="px-6 py-3 cursor-pointer transition-all flex items-center justify-between group"
                      :class="activeCategoryId === cat.id ? 'bg-white text-brand-700 font-bold border-r-2 border-brand-600' : 'text-gray-600 hover:bg-gray-100'"
                      @mouseenter="activeCategoryId = cat.id"
                      @click="go('/hall/supply', { categoryId: cat.id })"
                    >
                      <span>{{ cat.name }}</span>
                      <ChevronDown :size="12" class="-rotate-90 text-gray-300 group-hover:text-brand-500 transition-colors" />
                    </div>
                  </div>

                  <!-- Details Panel: 2nd & 3rd Level Categories -->
                  <div class="flex-1 p-8 overflow-y-auto bg-white">
                    <div v-if="activeCategory" class="space-y-8">
                      <!-- Panel Header -->
                      <div class="flex items-center justify-between border-b border-gray-50 pb-4 mb-6">
                        <div class="flex items-center gap-3">
                          <div class="w-1.5 h-5 bg-brand-600 rounded-full"></div>
                          <h3 class="text-xl font-black text-gray-900">{{ activeCategory.name }}</h3>
                        </div>
                        <button 
                          class="text-xs font-bold text-brand-600 hover:underline flex items-center gap-1"
                          @click="go('/hall/supply', { categoryId: activeCategory.id })"
                        >
                          查看该大类全部
                        </button>
                      </div>

                      <!-- Sub Groups -->
                      <div class="grid grid-cols-3 gap-x-8 gap-y-10">
                        <div v-for="group in buildGroups(activeCategory)" :key="group.title" class="space-y-4">
                          <div 
                            class="text-xs font-black text-gray-900 cursor-pointer hover:text-brand-600 flex items-center gap-1 group/title" 
                            @click="go('/hall/supply', { categoryId: group.titleNode.id })"
                          >
                            {{ group.title }}
                            <ChevronDown :size="10" class="-rotate-90 text-gray-300 group-hover/title:text-brand-500 opacity-0 group-hover/title:opacity-100 transition-all" />
                          </div>
                          <div class="flex flex-wrap gap-x-1.5 gap-y-1">
                            <button 
                              v-for="item in group.items" 
                              :key="item.id"
                              class="text-xs text-gray-500 hover:text-brand-600 hover:bg-brand-50 px-2 py-1 rounded-md transition-all border border-transparent hover:border-brand-100" 
                              @click="go('/hall/supply', { categoryId: item.id })"
                            >
                              {{ item.name }}
                            </button>
                          </div>
                        </div>
                      </div>

                      <!-- Empty State -->
                      <div v-if="!activeCategory.children?.length" class="py-20 text-center text-gray-400">
                        该分类下暂无子项
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </el-dropdown>

            <!-- 供应商 -->
            <el-dropdown trigger="hover" popper-class="mega-menu-popper">
              <button class="flex items-center gap-1 text-sm font-bold text-gray-700 hover:text-brand-600 transition-colors">
                供应商 <ChevronDown :size="14" />
              </button>
              <template #dropdown>
                <div class="p-6 w-[720px]">
                  <div class="flex items-center justify-between mb-6">
                    <div class="flex items-center gap-2 text-brand-700">
                      <Truck :size="18" />
                      <span class="font-bold text-lg">50 强制售商</span>
                    </div>
                  </div>
                  
                  <div class="grid grid-cols-3 gap-x-8 gap-y-2 mb-8">
                    <button 
                      v-for="s in topSuppliers" 
                      :key="s.id" 
                      class="text-left text-xs text-gray-600 hover:text-brand-600 hover:font-bold transition-all truncate py-1"
                      @click="go(`/companies/${s.id}`)"
                    >
                      {{ s.companyName }}
                    </button>
                  </div>

                  <div class="border-t pt-4">
                    <button 
                      class="w-full bg-brand-50 hover:bg-brand-100 text-brand-700 py-3 rounded-xl font-bold text-sm transition-all flex items-center justify-center gap-2"
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
              <button class="flex items-center gap-1 text-sm font-bold text-gray-700 hover:text-brand-600 transition-colors">
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
            <button class="hover:text-brand-600 transition-all " @click="go('/hall/supply')">供应大厅</button>
            <button class="hover:text-brand-600 transition-all " @click="go('/hall/need')">采购大厅</button>
            <button class="hover:text-brand-600 transition-all " @click="go('/talks')">话题广场</button>
          </div>

          <div class="flex items-center gap-3">
            <slot name="actions" />
            <template v-if="!isLoggedIn">
              <button class="bg-brand-600 text-white px-5 py-2 rounded-full font-bold hover:bg-brand-700 transition-all  text-sm" @click="openLogin">
                登录/注册
              </button>
            </template>
            <template v-else>
              <el-dropdown>
                <button class="flex items-center gap-3 bg-gray-50 border border-gray-200 px-3 py-2 rounded-full hover:bg-gray-50/50 transition-all ">
                  <div class="w-8 h-8 rounded-full bg-brand-600 text-white flex items-center justify-center font-bold text-sm">
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
                        <Settings :size="16" :stroke-width="2" />
                        我的账户
                      </div>
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="onLogout">
                      <div class="flex items-center gap-2 text-red-600">
                        <LogOut :size="16" :stroke-width="2" />
                        注销
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
  border-radius: 20px !important;
  border: none !important;
  padding: 0 !important;
  margin-top: 12px !important;
}
.mega-menu-popper .el-dropdown-menu {
  padding: 0 !important;
  background: transparent !important;
}
</style>



