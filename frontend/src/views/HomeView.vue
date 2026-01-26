<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getProductTree, type ProductNode } from '../api/product'
import { listPosts, type PostResponse } from '../api/post'
import { getPlatformStats, type StatsResponse } from '../api/stats'
import { listTopCompanies, type CompanyCardResponse } from '../api/company'
import PublicFooter from '../components/PublicFooter.vue'
import {
  MapPin,
  Search,
  TrendingUp,
  Package,
  Truck,
  ShoppingBag,
  Gift,
  MessageCircle,
  Wheat,
  Leaf,
  Droplets,
  Microscope,
  Beef,
  Gem,
  FlaskConical,
  LayoutGrid,
  ChevronRight,
  Building2,
  Users,
  Sprout,
  Factory,
  Cog
} from 'lucide-vue-next'

const router = useRouter()

// 产业板块定义（key 与 schemaCode 统一）
const domains = [
  {
    key: 'feed',  // 原料饲料
    name: '原料饲料',
    desc: '玉米、豆粕、油脂、添加剂...',
    icon: Wheat,
    color: 'text-brand-600',
    bgColor: 'bg-brand-50',
    tags: ['#水分', '#蛋白', '#产地']
  },
  {
    key: 'breed',  // 生物种苗
    name: '生物种苗',
    desc: '种禽、种蛋、鱼苗、种猪...',
    icon: Sprout,
    color: 'text-green-600',
    bgColor: 'bg-green-50',
    tags: ['#日龄', '#疫苗', '#亲本品种']
  },
  {
    key: 'process',  // 农牧加工
    name: '农牧加工',
    desc: '肉禽分割、禽蛋、水产成品...',
    icon: Factory,
    color: 'text-orange-600',
    bgColor: 'bg-orange-50',
    tags: ['#分割部位', '#温控', '#保质期']
  },
  {
    key: 'equipment',  // 装备物流
    name: '装备物流',
    desc: '料车、风机、农机、包装...',
    icon: Cog,
    color: 'text-blue-600',
    bgColor: 'bg-blue-50',
    tags: ['#载重', '#马力', '#保修']
  }
]

// 平台统计
const stats = ref<StatsResponse | null>(null)

// 数据库产品分类（树）
const categoryTree = ref<ProductNode[]>([])
const categoryLoading = ref(false)

// 供应商/采购商
const suppliers = ref<CompanyCardResponse[]>([])
const buyers = ref<CompanyCardResponse[]>([])
const dataLoading = ref(false)

// 首页热门话题（动态 Top4）
const hotTopicsLoading = ref(false)
const hotTopics = ref<PostResponse[]>([])

const searchKeyword = ref('')

const topCategories = computed(() => {
  const list = categoryTree.value ?? []
  const roots = list.filter((x) => (x.parentId ?? 0) === 0)
  return roots.length ? roots : list
})

async function loadData() {
  dataLoading.value = true
  try {
    const [statsRes, supRes, buyRes] = await Promise.all([
      getPlatformStats(),
      listTopCompanies('supplier', 12),
      listTopCompanies('buyer', 12)
    ])
    if (statsRes.code === 0) stats.value = statsRes.data ?? null
    if (supRes.code === 0) suppliers.value = supRes.data ?? []
    if (buyRes.code === 0) buyers.value = buyRes.data ?? []
  } catch (e) {
    console.error('Failed to load landing data', e)
  } finally {
    dataLoading.value = false
  }
}

function onSearch() {
  if (!searchKeyword.value.trim()) return
  router.push({
    path: '/search',
    query: { keyword: searchKeyword.value.trim() }
  })
}

function go(path: string) {
  router.push(path)
}

function displayName(p: PostResponse) {
  return (p.nickName || p.userName || '用户').trim() || '用户'
}

function avatarText(p: PostResponse) {
  const n = displayName(p)
  const ch = n[0] ?? 'U'
  return /\d/.test(ch) ? 'U' : ch
}

const getPostCover = (post: PostResponse) => {
  if (post.imagesJson) {
    try {
      const imgs = JSON.parse(post.imagesJson)
      if (Array.isArray(imgs) && imgs.length > 0) return imgs[0]
    } catch (e) {}
  }
  // 模拟占位图：基于 ID 选择不同的 Unsplash 图片
  const placeholders = [
    'https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?auto=format&fit=crop&q=80&w=800',
    'https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&q=80&w=800',
    'https://images.unsplash.com/photo-1523348837708-15d4a09cfac2?auto=format&fit=crop&q=80&w=800',
    'https://images.unsplash.com/photo-1495107332209-39780009581a?auto=format&fit=crop&q=80&w=800'
  ]
  return placeholders[post.id % placeholders.length]
}

function formatTime(timeStr: string | undefined) {
  if (!timeStr) return '未知时间'
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

async function loadHotTopics() {
  hotTopicsLoading.value = true
  try {
    const r = await listPosts({ orderBy: 'hot_7d', recentDays: 7, limit: 4 })
    if (r.code === 0) hotTopics.value = r.data ?? []
    else hotTopics.value = []
  } catch {
    hotTopics.value = []
  } finally {
    hotTopicsLoading.value = false
  }
}

async function loadCategories() {
  categoryLoading.value = true
  try {
    const r = await getProductTree()
    if (r.code === 0) categoryTree.value = r.data ?? []
    else categoryTree.value = []
  } catch {
    categoryTree.value = []
  } finally {
    categoryLoading.value = false
  }
}

function goCategory(node: ProductNode) {
  // 先跳供应大厅，并带上品类参数，后续大厅页可联动筛选
  router.push({
    path: '/hall/supply',
    query: {
      categoryId: String(node.id),
      categoryName: node.name
    }
  })
}

onMounted(() => {
  loadCategories()
  loadHotTopics()
  loadData()
})
onBeforeUnmount(() => {
})
</script>

<template>
  <div class="bg-gray-50 text-gray-900 min-h-screen">
    <!-- Hero -->
    <section class="relative min-h-[600px] flex items-center justify-center overflow-hidden">
      <!-- Background Image with Parallax-like effect -->
      <div class="absolute inset-0 z-0">
        <img 
          src="https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&q=80&w=2000" 
          class="w-full h-full object-cover scale-105"
          alt="Farmland background"
        />
        <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-[2px]"></div>
      </div>

      <!-- Content -->
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full relative z-10 py-24 flex flex-col items-center text-center animate-fade-in">
        <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-brand-500/20 border border-brand-500/30 backdrop-blur-md mb-8 animate-slide-up">
          <span class="relative flex h-2 w-2">
            <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-brand-400 opacity-75"></span>
            <span class="relative inline-flex rounded-full h-2 w-2 bg-brand-500"></span>
          </span>
          <span class="text-[10px] font-bold text-brand-200 uppercase tracking-widest">2026 智慧畜牧新标准</span>
        </div>

        <h1 class="text-4xl md:text-6xl font-extrabold text-white mb-6 leading-tight tracking-tight animate-slide-up" style="animation-delay: 100ms">
          连接全球资源<br/>
          <span class="text-transparent bg-clip-text bg-gradient-to-r from-brand-300 to-brand-500">重塑农牧供应链</span>
        </h1>
        
        <p class="text-lg md:text-xl text-gray-300 mb-12 max-w-2xl leading-relaxed animate-slide-up" style="animation-delay: 200ms">
          链接全国优质饲料、兽药、养殖设备供应商，提供在线合同签约及交流服务
        </p>

        <!-- Glassmorphism Search Box -->
        <div class="w-full max-w-3xl animate-slide-up" style="animation-delay: 300ms">
          <div class="group flex p-2 bg-white/10 backdrop-blur-2xl border border-white/20 rounded-2xl shadow-2xl focus-within:bg-white/15 focus-within:border-brand-500/50 transition-all duration-300">
            <div class="flex-1 flex items-center px-4">
              <Search :size="22" class="text-brand-300 group-focus-within:text-brand-400 transition-colors" />
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="搜索玉米、豆粕、疫苗、自动喂料机、型号..."
                class="w-full bg-transparent border-none outline-none text-white placeholder:text-gray-400/80 py-4 px-3 text-lg"
                @keyup.enter="onSearch"
              />
            </div>
            <button 
              class="bg-brand-600 hover:bg-brand-500 text-white px-10 py-4 rounded-xl font-bold transition-all active:scale-95 shadow-lg shadow-brand-900/20 flex items-center gap-2"
              @click="onSearch"
            >
              <span>搜索</span>
              <ArrowRight :size="18" />
            </button>
          </div>
          
          <!-- Hot Search Tags -->
          <div class="flex flex-wrap justify-center gap-4 mt-6 text-xs font-medium text-gray-400">
            <span class="text-gray-500">热门搜索:</span>
            <button class="hover:text-brand-400 transition-colors">优质豆粕</button>
            <button class="hover:text-brand-400 transition-colors">智能喂料系统</button>
            <button class="hover:text-brand-400 transition-colors">疫苗直供</button>
          </div>
        </div>
      </div>
    </section>

    <!-- Platform Stats (Stitch Style Layout) -->
    <section class="relative z-20 -mt-16 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="bg-white p-8 rounded-2xl shadow-xl border border-gray-100 flex items-center gap-6 group hover:border-brand-200 transition-all">
          <div class="w-16 h-16 rounded-2xl bg-brand-50 flex items-center justify-center text-brand-600 group-hover:scale-110 transition-transform">
            <Users :size="32" />
          </div>
          <div>
            <div class="text-3xl font-black text-gray-900">{{ stats?.userCount ? stats.userCount * 123 : '4.8' }}w+</div>
            <div class="text-xs font-bold text-gray-400 uppercase tracking-widest mt-1 text-nowrap">每月活跃搜索</div>
          </div>
        </div>
        <div class="bg-white p-8 rounded-2xl shadow-xl border border-gray-100 flex items-center gap-6 group hover:border-autumn-200 transition-all">
          <div class="w-16 h-16 rounded-2xl bg-autumn-50 flex items-center justify-center text-autumn-600 group-hover:scale-110 transition-transform">
            <Factory :size="32" />
          </div>
          <div>
            <div class="text-3xl font-black text-gray-900">{{ stats?.supplierCount ?? '5,200' }}+</div>
            <div class="text-xs font-bold text-gray-400 uppercase tracking-widest mt-1 text-nowrap">认证供应商</div>
          </div>
        </div>
        <div class="bg-white p-8 rounded-2xl shadow-xl border border-gray-100 flex items-center gap-6 group hover:border-purple-200 transition-all">
          <div class="w-16 h-16 rounded-2xl bg-purple-50 flex items-center justify-center text-purple-600 group-hover:scale-110 transition-transform">
            <Gem :size="32" />
          </div>
          <div>
            <div class="text-3xl font-black text-gray-900">{{ stats?.buyerCount ?? '860' }}+</div>
            <div class="text-xs font-bold text-gray-400 uppercase tracking-widest mt-1 text-nowrap">大宗采购商</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 全产业链支柱板块 (Bento Grid) -->
    <section class="py-24 bg-white">
      <div class="max-w-7xl mx-auto px-4">
        <div class="flex flex-col md:flex-row md:items-end justify-between mb-16 gap-6">
          <div class="max-w-2xl">
            <div class="inline-block px-3 py-1 rounded-lg bg-brand-50 text-brand-600 text-xs font-bold uppercase tracking-wider mb-4">核心业务范围</div>
            <h2 class="text-3xl md:text-4xl font-black text-gray-900 tracking-tight">
              覆盖从种苗培育到<br/>
              加工流通的 <span class="text-brand-600">全链路物资</span>
            </h2>
          </div>
          <button 
            class="group flex items-center gap-3 px-6 py-3 rounded-xl bg-gray-50 hover:bg-brand-50 text-sm font-bold text-gray-600 hover:text-brand-600 transition-all active:scale-95"
            @click="go('/categories')"
          >
            浏览全部分类
            <ChevronRight :size="18" class="group-hover:translate-x-1 transition-transform" />
          </button>
        </div>

        <div class="grid grid-cols-4 grid-rows-2 gap-6 h-[500px]">
          <!-- Big Bento Card (原料饲料) -->
          <div
            class="col-span-2 row-span-2 group relative overflow-hidden rounded-3xl bg-brand-600 cursor-pointer p-10 flex flex-col justify-end"
            @click="go(`/hall/supply?schemaCode=feed`)"
          >
            <div class="absolute top-10 right-10 opacity-20 group-hover:scale-110 transition-transform duration-500">
              <Wheat :size="180" class="text-white" />
            </div>
            <div class="relative z-10">
              <div class="flex flex-wrap gap-2 mb-6">
                <span class="px-3 py-1 rounded-full bg-white/20 backdrop-blur-md text-[10px] font-bold text-white uppercase"># 玉米豆粕</span>
                <span class="px-3 py-1 rounded-full bg-white/20 backdrop-blur-md text-[10px] font-bold text-white uppercase"># 进口添加剂</span>
              </div>
              <h3 class="text-4xl font-black text-white mb-4">原料饲料</h3>
              <p class="text-brand-50 max-w-sm text-sm leading-relaxed">提供全球大宗原料及核心添加剂的集采、比价与实时行情分析。</p>
            </div>
          </div>

          <!-- Medium Bento Card (生物种苗) -->
          <div
            class="col-span-2 group relative overflow-hidden rounded-3xl bg-slate-900 cursor-pointer p-8 flex flex-col justify-between"
            @click="go(`/hall/supply?schemaCode=breed`)"
          >
            <div class="absolute top-0 right-0 w-48 h-full bg-gradient-to-l from-brand-500/20 to-transparent"></div>
            <div class="flex justify-between items-start relative z-10">
              <h3 class="text-2xl font-black text-white">生物种苗</h3>
              <div class="w-12 h-12 rounded-2xl bg-white/10 flex items-center justify-center text-brand-400 backdrop-blur-md">
                <Sprout :size="24" />
              </div>
            </div>
            <div class="flex items-end justify-between relative z-10">
              <p class="text-gray-400 text-sm max-w-[200px]">优质种禽、种蛋、鱼苗及畜种资源调度。</p>
              <div class="flex gap-2">
                <span class="w-1.5 h-1.5 rounded-full bg-brand-500"></span>
                <span class="w-1.5 h-1.5 rounded-full bg-brand-500/30"></span>
              </div>
            </div>
          </div>

          <!-- Small Bento Card (农牧加工) -->
          <div
            class="group relative overflow-hidden rounded-3xl bg-orange-50 border border-orange-100 cursor-pointer p-6 flex flex-col justify-between hover:border-orange-200 transition-all"
            @click="go(`/hall/supply?schemaCode=process`)"
          >
            <div class="w-12 h-12 rounded-2xl bg-orange-100 flex items-center justify-center text-orange-600">
              <Factory :size="24" />
            </div>
            <div>
              <h4 class="font-black text-gray-900 mb-1">农牧加工</h4>
              <p class="text-[10px] text-gray-500 font-bold uppercase tracking-widest">成品/半成品流通</p>
            </div>
          </div>

          <!-- Small Bento Card (装备物流) -->
          <div
            class="group relative overflow-hidden rounded-3xl bg-blue-50 border border-blue-100 cursor-pointer p-6 flex flex-col justify-between hover:border-blue-200 transition-all"
            @click="go(`/hall/supply?schemaCode=equipment`)"
          >
            <div class="w-12 h-12 rounded-2xl bg-blue-100 flex items-center justify-center text-blue-600">
              <Cog :size="24" />
            </div>
            <div>
              <h4 class="font-black text-gray-900 mb-1">装备物流</h4>
              <p class="text-[10px] text-gray-500 font-bold uppercase tracking-widest">自动化养殖系统</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 按供应商搜索 (品牌墙风格) -->
    <section class="py-20 bg-gray-50/50 border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4">
        <div class="bg-white dark:bg-slate-800 rounded-xl p-8 shadow-sm">
          <div class="flex flex-col md:flex-row md:items-end justify-between mb-8 border-b border-gray-100 dark:border-slate-700 pb-4">
            <div>
              <h2 class="text-2xl font-bold text-gray-900 dark:text-white tracking-tight">
                按 <span class="text-brand-600">供应商</span> 搜索
              </h2>
            </div>
            <button 
              class="group flex items-center gap-2 text-sm font-medium text-brand-600 hover:underline transition-colors active:scale-95"
              @click="go('/companies/directory')"
            >
              查看所有供应商
            </button>
          </div>

          <div v-if="dataLoading" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            <div v-for="i in 12" :key="i" class="h-16 bg-slate-50 rounded-xl animate-pulse"></div>
          </div>
          <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            <div
              v-for="s in suppliers.slice(0, 12)"
              :key="s.id"
              class="group h-16 border border-gray-200 dark:border-slate-600 rounded-xl flex items-center justify-center bg-slate-50 dark:bg-slate-700 hover:border-brand-600 dark:hover:border-brand-600 cursor-pointer transition"
              @click="go(`/companies/${s.id}`)"
            >
              <span class="font-bold text-gray-700 dark:text-slate-300 text-sm truncate px-3">{{ s.companyName }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 按采购商搜索 (品牌墙风格) -->
    <section class="py-20 bg-white border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4">
        <div class="bg-white dark:bg-slate-800 rounded-xl p-8 shadow-sm">
          <div class="flex flex-col md:flex-row md:items-end justify-between mb-8 border-b border-gray-100 dark:border-slate-700 pb-4">
            <div>
              <h2 class="text-2xl font-bold text-gray-900 dark:text-white tracking-tight">
                按 <span class="text-autumn-600">采购商</span> 搜索
              </h2>
            </div>
            <button 
              class="group flex items-center gap-2 text-sm font-medium text-autumn-600 hover:underline transition-colors active:scale-95"
              @click="go('/companies/directory?type=buyer')"
            >
              查看所有采购商
            </button>
          </div>

          <div v-if="dataLoading" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            <div v-for="i in 12" :key="i" class="h-16 bg-slate-50 rounded-xl animate-pulse"></div>
          </div>
          <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            <div
              v-for="b in buyers.slice(0, 12)"
              :key="b.id"
              class="group h-16 border border-gray-200 dark:border-slate-600 rounded-xl flex items-center justify-center bg-slate-50 dark:bg-slate-700 hover:border-autumn-600 dark:hover:border-autumn-600 cursor-pointer transition"
              @click="go(`/companies/${b.id}`)"
            >
              <span class="font-bold text-gray-700 dark:text-slate-300 text-sm truncate px-3">{{ b.companyName }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 话题广场 -->
    <section class="max-w-7xl mx-auto px-4 py-20 border-b border-gray-200">
      <div class="flex flex-col md:flex-row md:items-end justify-between mb-16 gap-4">
        <div>
          <h2 class="text-2xl font-bold text-gray-900 tracking-tight">话题广场</h2>
          <p class="text-gray-500 mt-4 max-w-2xl leading-relaxed">
            通过我们的博客、指南、播客、文章、访谈等，了解所有最新新闻和信息
          </p>
        </div>
        <button 
          class="group flex items-center gap-2 text-sm font-bold text-gray-500 hover:text-brand-600 transition-colors active:scale-95"
          @click="go('/talks')"
        >
          查看更多内容
          <div class="w-8 h-8 rounded-full bg-gray-50 group-hover:bg-brand-50 flex items-center justify-center transition-colors">
            <ChevronRight :size="16" />
          </div>
        </button>
      </div>

      <div v-if="hotTopicsLoading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div v-for="i in 4" :key="i" class="bg-white rounded-2xl border border-gray-200 animate-pulse overflow-hidden">
          <div class="aspect-video bg-gray-50"></div>
          <div class="p-6 space-y-4">
            <div class="h-4 bg-gray-50 rounded w-3/4"></div>
            <div class="h-4 bg-gray-50 rounded w-1/2"></div>
          </div>
        </div>
      </div>
      <div v-else-if="hotTopics.length === 0" class="text-center py-20 bg-white rounded-2xl border border-dashed border-gray-200">
        <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4">
          <MessageCircle :size="32" class="text-gray-300" />
        </div>
        <p class="text-gray-400">暂无热门话题</p>
      </div>
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div
          v-for="post in hotTopics.slice(0, 4)"
          :key="post.id"
          class="group bg-white rounded-2xl border border-gray-200 hover:shadow-md hover:border-brand-100 transition-all cursor-pointer overflow-hidden flex flex-col"
          @click="go(`/talks/${post.id}`)"
        >
          <!-- 封面图 -->
          <div class="aspect-video overflow-hidden bg-gray-100 relative">
            <img :src="getPostCover(post)" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300 ease-out" />
            <div class="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent"></div>
          </div>

          <!-- 内容 -->
          <div class="p-6 flex-1 flex flex-col">
            <h3 class="text-lg font-bold text-gray-900 mb-3 line-clamp-2 group-hover:text-brand-600 transition-colors leading-snug">
              {{ post.title }}
            </h3>
            <p class="text-xs text-gray-500 leading-relaxed line-clamp-3 mb-6 flex-1">
              {{ post.content || '暂无内容摘要' }}
            </p>
            
            <!-- 作者信息 -->
            <div class="flex items-center gap-3 pt-4 border-t border-gray-50">
              <div class="w-8 h-8 rounded-full bg-brand-500 flex items-center justify-center text-[10px] font-bold text-white shadow-sm shrink-0">
                {{ avatarText(post) }}
              </div>
              <div class="min-w-0">
                <div class="text-xs font-bold text-gray-900 truncate">{{ displayName(post) }}</div>
                <div class="text-[10px] text-gray-400 mt-0.5">{{ formatTime(post.createTime) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
    <!-- 底部流程引导 -->
    <section class="bg-slate-900 py-24 relative overflow-hidden border-t border-white/5">
      <div class="max-w-7xl mx-auto px-4 text-center">
        <h2 class="text-2xl font-bold text-white mb-4">开启您的专业交易之旅</h2>
        <p class="text-gray-400 mb-16 max-w-xl mx-auto">从发现商机到合约履行，AgriMatch 为您提供全流程保障</p>

        <div class="grid grid-cols-2 md:grid-cols-4 gap-8">
          <div class="flex flex-col items-center">
            <div class="w-16 h-16 bg-white/5 border border-white/10 text-brand-400 rounded-xl flex items-center justify-center mb-4 backdrop-blur-xl">
              <span class="text-2xl font-bold">01</span>
            </div>
            <h4 class="font-bold text-white mb-2">发布/发现</h4>
            <p class="text-[10px] text-gray-500 px-4 leading-relaxed">精准匹配海量供需信息</p>
          </div>

          <div class="flex flex-col items-center">
            <div class="w-16 h-16 bg-white/5 border border-white/10 text-autumn-400 rounded-xl flex items-center justify-center mb-4 backdrop-blur-xl">
              <span class="text-2xl font-bold">02</span>
            </div>
            <h4 class="font-bold text-white mb-2">洽谈议价</h4>
            <p class="text-[10px] text-gray-500 px-4 leading-relaxed">在线直聊，快速达成意向</p>
          </div>

          <div class="flex flex-col items-center">
            <div class="w-16 h-16 bg-white/5 border border-white/10 text-purple-400 rounded-xl flex items-center justify-center mb-4 backdrop-blur-xl">
              <span class="text-2xl font-bold">03</span>
            </div>
            <h4 class="font-bold text-white mb-2">电子签约</h4>
            <p class="text-[10px] text-gray-500 px-4 leading-relaxed">合法合规，保障双方权益</p>
          </div>

          <div class="flex flex-col items-center">
            <div class="w-16 h-16 bg-brand-600 text-white rounded-xl flex items-center justify-center mb-4 shadow-md">
              <Gift :size="28" />
            </div>
            <h4 class="font-bold text-white mb-2">履约/奖励</h4>
            <p class="text-[10px] text-gray-500 px-4 leading-relaxed">完成交易，赢取丰厚积分</p>
          </div>
        </div>
      </div>
    </section>

    <PublicFooter />
  </div>
</template>

<style scoped>
.hero-gradient {
  background: linear-gradient(135deg, #047857 0%, #065f46 30%, #064e3b 60%, #022c22 100%);
  position: relative;
}

.hero-gradient::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 30% 20%, rgba(132, 187, 159, 0.15) 0%, transparent 50%),
              radial-gradient(circle at 70% 80%, rgba(132, 187, 159, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

.hero-pattern {
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
}
</style>
