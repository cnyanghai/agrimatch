<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { useUiStore } from '../store/ui'
import { LogOut, Settings, ChevronDown, ShoppingBag, Truck } from 'lucide-vue-next'
import { getProductTree, type ProductNode } from '../api/product'
import { listTopCompanies, type CompanyCardResponse } from '../api/company'
import logoWhite from '../assets/logo-white.png'

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
  <nav class="bg-brand-700/90 backdrop-blur-md border-b border-brand-800/30 sticky top-0 z-50 shadow-lg shadow-brand-900/20">
    <div class="w-full px-4 md:px-10">
      <div class="min-h-[52px] py-2 flex items-center justify-between gap-6">
        <!-- Left: Logo & Dropdowns (Stacked) -->
        <div class="flex flex-col items-start gap-3">
          <div class="flex items-center gap-2 cursor-pointer hover:bg-white/10 px-2 py-1.5 rounded-xl transition-all active:scale-95" @click="go('/')">
            <!-- Logo: NHT 线条图形（白色版本，透明背景） -->
            <img :src="logoWhite" alt="农汇通" class="h-8 w-auto" />
            <div class="leading-tight hidden sm:block">
              <div class="font-bold text-white text-base tracking-wide">农汇通</div>
              <div class="text-[9px] font-medium text-brand-200/80">农牧供需智能匹配平台</div>
            </div>
          </div>

          <!-- Dropdowns Row -->
          <div class="hidden lg:flex items-center gap-6 ml-2">
            <!-- 产品分类 -->
            <el-dropdown trigger="hover" popper-class="mega-menu-popper" :hide-timeout="300" :show-timeout="100">
              <button class="flex items-center gap-1 text-sm font-medium text-white hover:text-brand-100 transition-colors active:scale-95">
                产品分类 <ChevronDown :size="14" class="text-white/70" />
              </button>
              <template #dropdown>
                <div class="flex w-[520px] h-[480px] overflow-hidden bg-white rounded-2xl shadow-2xl">
                  <!-- Sidebar: 1st Level Categories -->
                  <div class="w-40 bg-gray-50/80 border-r border-gray-100 py-3 overflow-y-auto">
                    <div
                      v-for="cat in topCategories"
                      :key="cat.id"
                      class="px-4 py-2.5 mx-2 rounded-lg cursor-pointer transition-all flex items-center justify-between group text-[13px]"
                      :class="activeCategoryId === cat.id ? 'bg-white text-brand-600 font-medium shadow-sm' : 'text-gray-600 hover:bg-white/60'"
                      @mouseenter="activeCategoryId = cat.id"
                      @click="go('/hall/supply', { categoryId: cat.id })"
                    >
                      <span>{{ cat.name }}</span>
                      <ChevronDown :size="12" class="-rotate-90 text-gray-300 group-hover:text-brand-500 transition-colors" />
                    </div>
                  </div>

                  <!-- Details Panel: 2nd & 3rd Level Categories -->
                  <div class="flex-1 py-3 overflow-y-auto bg-white">
                    <div v-if="activeCategory">
                      <template v-for="group in buildGroups(activeCategory)" :key="group.titleNode.id">
                        <!-- Second Level Category -->
                        <div
                          class="px-5 py-2.5 cursor-pointer transition-all flex items-center justify-between group text-[13px] font-medium text-gray-800 hover:bg-gray-50 hover:text-brand-600"
                          @click="go('/hall/supply', { categoryId: group.titleNode.id })"
                        >
                          <span>{{ group.title }}</span>
                          <ChevronDown :size="12" class="-rotate-90 text-gray-300 group-hover:text-brand-500 transition-colors" />
                        </div>
                        <!-- Third Level Products -->
                        <div
                          v-for="item in group.items"
                          :key="item.id"
                          class="px-5 pl-9 py-2 cursor-pointer transition-all flex items-center justify-between group text-[13px] text-gray-500 hover:bg-gray-50 hover:text-brand-600"
                          @click="go('/hall/supply', { categoryId: item.id })"
                        >
                          <span>{{ item.name }}</span>
                          <ChevronDown :size="10" class="-rotate-90 text-gray-200 group-hover:text-brand-400 transition-colors" />
                        </div>
                      </template>

                      <!-- Empty State -->
                      <div v-if="!activeCategory.children?.length" class="py-16 text-center text-gray-400 text-sm">
                        该分类下暂无子项
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </el-dropdown>

            <!-- 供应商 -->
            <el-dropdown trigger="hover" popper-class="mega-menu-popper" :hide-timeout="300" :show-timeout="100">
              <button class="flex items-center gap-1 text-sm font-medium text-white hover:text-brand-100 transition-colors active:scale-95">
                供应商 <ChevronDown :size="14" class="text-white/70" />
              </button>
              <template #dropdown>
                <div class="p-5 w-[600px] bg-white rounded-2xl shadow-2xl">
                  <div class="flex items-center justify-between mb-4 pb-3 border-b border-gray-100">
                    <div class="flex items-center gap-2 text-brand-600">
                      <Truck :size="18" />
                      <span class="font-semibold text-sm">优质供应商</span>
                    </div>
                    <span class="text-xs text-gray-400">TOP 50</span>
                  </div>

                  <div class="grid grid-cols-3 gap-x-6 gap-y-1 mb-5">
                    <button
                      v-for="s in topSuppliers"
                      :key="s.id"
                      class="text-left text-[13px] text-gray-600 hover:text-brand-600 hover:bg-brand-50 transition-all truncate py-2 px-2 rounded-lg active:scale-95"
                      @click.stop="() => { router.push(`/companies/${s.id}`) }"
                    >
                      {{ s.companyName }}
                    </button>
                  </div>

                  <button
                    class="w-full bg-brand-50 hover:bg-brand-100 text-brand-600 py-2.5 rounded-xl font-medium text-[13px] transition-all flex items-center justify-center gap-2 active:scale-95"
                    @click="go('/companies/directory', { type: 'supplier' })"
                  >
                    查看所有供应商
                    <ChevronDown :size="14" class="-rotate-90" />
                  </button>
                </div>
              </template>
            </el-dropdown>

            <!-- 采购商 -->
            <el-dropdown trigger="hover" popper-class="mega-menu-popper" :hide-timeout="300" :show-timeout="100">
              <button class="flex items-center gap-1 text-sm font-medium text-white hover:text-brand-100 transition-colors active:scale-95">
                采购商 <ChevronDown :size="14" class="text-white/70" />
              </button>
              <template #dropdown>
                <div class="p-5 w-[600px] bg-white rounded-2xl shadow-2xl">
                  <div class="flex items-center justify-between mb-4 pb-3 border-b border-gray-100">
                    <div class="flex items-center gap-2 text-autumn-600">
                      <ShoppingBag :size="18" />
                      <span class="font-semibold text-sm">优质采购商</span>
                    </div>
                    <span class="text-xs text-gray-400">TOP 50</span>
                  </div>

                  <div class="grid grid-cols-3 gap-x-6 gap-y-1 mb-5">
                    <button
                      v-for="b in topBuyers"
                      :key="b.id"
                      class="text-left text-[13px] text-gray-600 hover:text-autumn-600 hover:bg-autumn-50 transition-all truncate py-2 px-2 rounded-lg active:scale-95"
                      @click.stop="() => { router.push(`/companies/${b.id}`) }"
                    >
                      {{ b.companyName }}
                    </button>
                  </div>

                  <button
                    class="w-full bg-autumn-50 hover:bg-autumn-100 text-autumn-600 py-2.5 rounded-xl font-medium text-[13px] transition-all flex items-center justify-center gap-2 active:scale-95"
                    @click="go('/companies/directory', { type: 'buyer' })"
                  >
                    查看所有采购商
                    <ChevronDown :size="14" class="-rotate-90" />
                  </button>
                </div>
              </template>
            </el-dropdown>
          </div>
        </div>

        <!-- Right: Nav & User -->
        <div class="flex items-center gap-6">
          <div class="hidden md:flex items-center gap-5 text-sm font-bold text-white">
            <button class="hover:text-brand-100 hover:bg-white/10 px-3 py-1.5 rounded-md transition-all active:scale-95" @click="go('/hall/supply')">供应大厅</button>
            <button class="hover:text-brand-100 hover:bg-white/10 px-3 py-1.5 rounded-md transition-all active:scale-95" @click="go('/hall/need')">采购大厅</button>
            <button class="hover:text-brand-100 hover:bg-white/10 px-3 py-1.5 rounded-md transition-all active:scale-95" @click="go('/talks')">话题广场</button>
          </div>

          <div class="flex items-center gap-3">
            <slot name="actions" />
            <template v-if="!isLoggedIn">
              <button class="border border-white/20 text-white px-5 py-2 rounded-full font-bold hover:bg-white/10 transition-all active:scale-95 text-sm" @click="openLogin">
                登录/注册
              </button>
            </template>
            <template v-else>
              <el-dropdown>
                <button class="flex items-center gap-3 bg-white/10 border border-white/20 px-3 py-2 rounded-xl hover:bg-white/20 transition-all active:scale-95">
                  <div class="w-8 h-8 rounded-lg bg-white text-brand-700 flex items-center justify-center font-bold text-sm">
                    {{ avatarChar }}
                  </div>
                  <div class="text-left leading-tight hidden sm:block">
                    <div class="text-sm font-bold text-white">{{ displayName }}</div>
                    <div class="text-[10px] font-bold text-brand-200 uppercase tracking-widest">已登录</div>
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
  border-radius: 1.5rem !important; /* rounded-2xl */
  border: none !important;
  padding: 0 !important;
  margin-top: 12px !important;
}
.mega-menu-popper .el-dropdown-menu {
  padding: 0 !important;
  background: transparent !important;
}
</style>



