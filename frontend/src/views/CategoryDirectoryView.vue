<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getProductTree, type ProductNode } from '../api/product'
import PublicFooter from '../components/PublicFooter.vue'
import { 
  Package, 
  Wheat, 
  Leaf, 
  Droplets, 
  Microscope, 
  Beef, 
  Gem, 
  FlaskConical,
  Search,
  ChevronRight
} from 'lucide-vue-next'

const router = useRouter()
const categoryTree = ref<ProductNode[]>([])
const loading = ref(false)
const searchKeyword = ref('')

const getIcon = (name: string) => {
  const mapping: Record<string, any> = {
    '谷物': Wheat,
    '油料': Leaf,
    '油脂': Droplets,
    '微生物': Microscope,
    '动物': Beef,
    '矿物质': Gem,
    '添加剂': FlaskConical,
  }
  for (const key in mapping) {
    if (name.includes(key)) return mapping[key]
  }
  return Package
}

const loadData = async () => {
  loading.value = true
  try {
    const r = await getProductTree()
    if (r.code === 0) {
      categoryTree.value = r.data ?? []
    }
  } catch (e) {
    console.error('Failed to load category tree', e)
  } finally {
    loading.value = false
  }
}

const goCategory = (node: ProductNode) => {
  router.push({
    path: '/hall/supply',
    query: {
      categoryId: String(node.id),
      categoryName: node.name
    }
  })
}

const onSearch = () => {
  if (!searchKeyword.value.trim()) return
  router.push({
    path: '/hall/supply',
    query: { keyword: searchKeyword.value.trim() }
  })
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="bg-gray-50 min-h-screen flex flex-col">
    <main class="flex-1">
      <!-- Header / Search -->
      <div class="bg-white border-b border-gray-200 py-12">
        <div class="max-w-7xl mx-auto px-4">
          <div class="flex flex-col md:flex-row md:items-center justify-between gap-6">
            <div>
              <h1 class="text-2xl font-bold text-gray-900 tracking-tight">农牧原料分类目录</h1>
              <p class="text-gray-500 mt-2">浏览全平台供应品类，快速锁定所需原料</p>
            </div>
            <div class="w-full md:w-96 relative">
              <input 
                v-model="searchKeyword"
                type="text" 
                placeholder="搜索品类名称..." 
                class="w-full pl-12 pr-4 py-3 bg-gray-50 border-2 border-transparent rounded-lg focus:bg-white focus:border-slate-400 transition-all outline-none text-sm"
                @keyup.enter="onSearch"
              />
              <Search :size="18" class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
            </div>
          </div>
        </div>
      </div>

      <!-- Category Grid -->
      <div class="max-w-7xl mx-auto px-4 py-16">
        <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-10">
          <div v-for="i in 8" :key="i" class="space-y-4">
            <div class="h-8 bg-white rounded-lg animate-pulse w-1/2 border border-gray-200"></div>
            <div class="space-y-2">
              <div v-for="j in 5" :key="j" class="h-4 bg-white rounded animate-pulse w-full border border-gray-50"></div>
            </div>
          </div>
        </div>
        
        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-x-10 gap-y-16">
          <div v-for="root in categoryTree" :key="root.id" class="flex flex-col">
            <!-- Level 1 Header -->
            <div 
              class="flex items-center justify-between border-b border-gray-200 pb-3 mb-5 group cursor-pointer"
              @click="goCategory(root)"
            >
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 rounded-lg bg-slate-50 text-slate-500 flex items-center justify-center">
                  <component :is="getIcon(root.name)" :size="18" />
                </div>
                <h2 class="font-bold text-gray-900 group-hover:text-slate-900 transition-colors">
                  {{ root.name }}
                </h2>
              </div>
              <ChevronRight :size="16" class="text-gray-300 group-hover:text-slate-600 transition-colors" />
            </div>

            <!-- Level 2 List -->
            <ul class="space-y-2">
              <li 
                v-for="child in root.children" 
                :key="child.id"
                class="flex items-center group cursor-pointer"
                @click="goCategory(child)"
              >
                <div class="w-1 h-1 bg-gray-300 rounded-full mr-3 group-hover:bg-slate-400 transition-colors"></div>
                <span class="text-sm text-gray-500 group-hover:text-slate-900 transition-colors">
                  {{ child.name }}
                </span>
              </li>
              <li v-if="!root.children || root.children.length === 0" class="text-xs text-gray-400 italic">
                暂无子分类
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
/* 隐藏滚动条 */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}
::-webkit-scrollbar-thumb:hover {
  background: #cbd5e1;
}
</style>

