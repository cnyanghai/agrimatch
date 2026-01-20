<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getProductTree, type ProductNode } from '../api/product'
import { listPosts, type PostResponse } from '../api/post'
import { getPlatformStats, type StatsResponse } from '../api/stats'
import { listTopCompanies, type CompanyCardResponse } from '../api/company'
import PublicTopNav from '../components/PublicTopNav.vue'
import PublicFooter from '../components/PublicFooter.vue'
import { 
  MapPin, 
  ArrowRight, 
  Search, 
  TrendingUp, 
  Package, 
  Truck, 
  ShoppingBag, 
  Gift, 
  MessageCircle, 
  FileText,
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
  Users
} from 'lucide-vue-next'

const router = useRouter()

// 分类元数据映射
const getCategoryMeta = (name: string) => {
  const mapping: Record<string, { icon: any; color: string; bgColor: string }> = {
    '谷物': { icon: Wheat, color: 'text-amber-600', bgColor: 'bg-amber-50' },
    '油料': { icon: Leaf, color: 'text-emerald-600', bgColor: 'bg-emerald-50' },
    '油脂': { icon: Droplets, color: 'text-blue-600', bgColor: 'bg-blue-50' },
    '微生物': { icon: Microscope, color: 'text-purple-600', bgColor: 'bg-purple-50' },
    '动物': { icon: Beef, color: 'text-red-600', bgColor: 'bg-red-50' },
    '矿物质': { icon: Gem, color: 'text-sky-600', bgColor: 'bg-sky-50' },
    '添加剂': { icon: FlaskConical, color: 'text-indigo-600', bgColor: 'bg-indigo-50' },
  }

  for (const key in mapping) {
    if (name.includes(key)) return mapping[key]
  }
  return { icon: Package, color: 'text-gray-600', bgColor: 'bg-gray-50' }
}

const canvasRef = ref<HTMLCanvasElement | null>(null)
let raf: number | null = null
let onResize: any = null

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
    path: '/hall/supply',
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

function startCanvas() {
  const canvas = canvasRef.value
  if (!canvas) return
  const c = canvas
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const points: Array<{ x: number; y: number; vx: number; vy: number; size: number }> = []
  const numPoints = 80

  function resize() {
    c.width = c.offsetWidth
    c.height = c.offsetHeight
  }

  onResize = () => resize()
  window.addEventListener('resize', onResize)
  resize()

  for (let i = 0; i < numPoints; i++) {
    points.push({
      x: Math.random() * c.width,
      y: Math.random() * c.height,
      vx: (Math.random() - 0.5) * 0.5,
      vy: (Math.random() - 0.5) * 0.5,
      size: Math.random() * 2 + 1
    })
  }

  const loop = () => {
    ctx.clearRect(0, 0, c.width, c.height)
    ctx.strokeStyle = 'rgba(16, 185, 129, 0.15)'
    ctx.lineWidth = 0.5

    points.forEach((p, i) => {
      p.x += p.vx
      p.y += p.vy
      if (p.x < 0 || p.x > c.width) p.vx *= -1
      if (p.y < 0 || p.y > c.height) p.vy *= -1

      ctx.beginPath()
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
      const alpha = 0.3 + Math.sin(Date.now() * 0.001 + i) * 0.2
      ctx.fillStyle = `rgba(16, 185, 129, ${alpha})`
      ctx.fill()

      for (let j = i + 1; j < points.length; j++) {
        const p2 = points[j]
        if (!p2) continue
        const dist = Math.hypot(p.x - p2.x, p.y - p2.y)
        if (dist < 150) {
          ctx.beginPath()
          ctx.moveTo(p.x, p.y)
          ctx.lineTo(p2.x, p2.y)
          ctx.globalAlpha = 1 - dist / 150
          ctx.stroke()
          ctx.globalAlpha = 1
        }
      }
    })

    raf = requestAnimationFrame(loop)
  }

  loop()
}

onMounted(() => {
  startCanvas()
  loadCategories()
  loadHotTopics()
  loadData()
})
onBeforeUnmount(() => {
  if (raf) cancelAnimationFrame(raf)
  if (onResize) window.removeEventListener('resize', onResize)
})
</script>

<template>
  <div class="bg-gray-50 text-gray-900 min-h-screen">
    <PublicTopNav />

    <!-- Hero -->
    <section class="relative hero-gradient overflow-hidden text-white h-[510px] flex items-center pt-16">
      <canvas ref="canvasRef" class="map-canvas"></canvas>

      <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 w-full relative z-10 py-12 flex flex-col items-center text-center">
        <h1 class="text-4xl md:text-6xl font-extrabold mb-4 leading-tight tracking-tight">
          智慧畜牧供应链
        </h1>
        <h1 class="text-4xl md:text-6xl font-extrabold mb-4 leading-tight tracking-tight">
          一站式采购与管理平台
        </h1>
        <p class="text-xl md:text-2xl text-gray-300 mb-12 max-w-2xl leading-relaxed">
          链接全国优质饲料、兽药、养殖设备供应商，提供在线合同生成及全流程追溯服务
        </p>

        <!-- Centered Search Box -->
        <div class="w-full max-w-3xl mb-12">
          <div class="flex p-1.5 bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-2xl focus-within:border-emerald-500/50 transition-all">
            <div class="flex-1 flex items-center px-4">
              <Search :size="20" class="text-gray-400 mr-3" />
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="搜索玉米、豆粕、疫苗、自动喂料机、型号..."
                class="w-full bg-transparent border-none outline-none text-white placeholder:text-gray-400 py-3"
                @keyup.enter="onSearch"
              />
            </div>
            <button class="bg-emerald-600 hover:bg-emerald-700 text-white px-8 py-3 rounded-xl font-bold transition-all active:scale-95" @click="onSearch">
              搜索
            </button>
          </div>
        </div>

        <!-- Stats Display -->
        <div class="flex flex-wrap justify-center gap-8 md:gap-16 text-sm font-medium text-gray-400">
          <div class="flex items-center gap-2">
            <TrendingUp :size="16" class="text-emerald-500" />
            <span>每月搜索量超过 <b class="text-white">{{ stats?.userCount ? stats.userCount * 123 : 400 }}</b> 万次</span>
          </div>
          <div class="flex items-center gap-2">
            <Truck :size="16" class="text-emerald-500" />
            <span>搜索数千家供应商 <b class="text-white">({{ stats?.supplierCount ?? 0 }})</b></span>
          </div>
          <div class="flex items-center gap-2">
            <ShoppingBag :size="16" class="text-emerald-500" />
            <span>搜索数百家采购商 <b class="text-white">({{ stats?.buyerCount ?? 0 }})</b></span>
          </div>
        </div>
      </div>
    </section>

    <!-- 按原料搜索 -->
    <section class="py-24 bg-white border-b border-gray-100">
      <div class="max-w-7xl mx-auto px-4">
        <div class="flex flex-col md:flex-row md:items-end justify-between mb-12 gap-4">
          <div>
            <h2 class="text-3xl font-extrabold text-gray-900 tracking-tight">
              按 <span class="text-emerald-600">原料</span> 搜索
            </h2>
          </div>
          <button 
            class="group flex items-center gap-2 text-sm font-bold text-gray-500 hover:text-emerald-600 transition-colors"
            @click="go('/hall/supply')"
          >
            查看所有类别
            <div class="w-8 h-8 rounded-full bg-gray-50 group-hover:bg-emerald-50 flex items-center justify-center transition-colors">
              <ChevronRight :size="16" />
            </div>
          </button>
        </div>

        <div v-if="categoryLoading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <div v-for="i in 8" :key="i" class="h-24 bg-gray-50 rounded-2xl animate-pulse"></div>
        </div>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <!-- 核心分类卡片 -->
          <div
            v-for="cat in topCategories.slice(0, 7)"
            :key="cat.id"
            class="group p-5 bg-white border border-gray-100 rounded-2xl transition-all cursor-pointer flex items-center gap-4 hover:shadow-xl hover:border-emerald-100 hover:-translate-y-1"
            @click="goCategory(cat)"
          >
            <div 
              class="w-14 h-14 shrink-0 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform shadow-sm"
              :class="getCategoryMeta(cat.name).bgColor"
            >
              <component :is="getCategoryMeta(cat.name).icon" :size="28" :class="getCategoryMeta(cat.name).color" />
            </div>
            <div class="min-w-0">
              <div class="font-bold text-gray-900 group-hover:text-emerald-600 transition-colors truncate text-lg">
                {{ cat.name }}
              </div>
              <div class="text-xs text-gray-400 mt-0.5">探索优质{{ cat.name }}资源</div>
            </div>
          </div>

          <!-- “更多”卡片 -->
          <div
            class="group p-5 bg-gray-50/50 border border-dashed border-gray-200 rounded-2xl transition-all cursor-pointer flex items-center gap-4 hover:bg-white hover:border-emerald-200 hover:shadow-lg hover:-translate-y-1"
            @click="go('/hall/supply')"
          >
            <div class="w-14 h-14 shrink-0 bg-white rounded-xl flex items-center justify-center group-hover:bg-emerald-600 group-hover:text-white transition-all shadow-sm text-gray-400">
              <LayoutGrid :size="28" />
            </div>
            <div>
              <div class="font-bold text-gray-900 group-hover:text-emerald-600 transition-colors text-lg">更多原料</div>
              <div class="text-xs text-gray-400 mt-0.5">查看全部分类信息</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 按供应商搜索 (品牌墙风格) -->
    <section class="py-24 bg-gray-50/50 border-b border-gray-100">
      <div class="max-w-7xl mx-auto px-4">
        <div class="flex flex-col md:flex-row md:items-end justify-between mb-12 gap-4">
          <div>
            <h2 class="text-3xl font-extrabold text-gray-900 tracking-tight">
              按 <span class="text-emerald-600">供应商</span> 搜索
            </h2>
          </div>
          <button 
            class="group flex items-center gap-2 text-sm font-bold text-gray-500 hover:text-emerald-600 transition-colors"
            @click="go('/hall/supply')"
          >
            查看所有供应商
            <div class="w-8 h-8 rounded-full bg-white group-hover:bg-emerald-50 flex items-center justify-center transition-colors">
              <ChevronRight :size="16" />
            </div>
          </button>
        </div>

        <div v-if="dataLoading" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
          <div v-for="i in 12" :key="i" class="h-32 bg-white rounded-2xl animate-pulse"></div>
        </div>
        <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
          <div
            v-for="s in suppliers.slice(0, 12)"
            :key="s.id"
            class="group bg-white p-6 rounded-2xl border border-gray-100 transition-all cursor-pointer flex flex-col items-center justify-center text-center hover:shadow-lg hover:border-emerald-100 hover:-translate-y-1"
            @click="go(`/companies/${s.id}`)"
          >
            <div class="w-16 h-16 bg-gray-50 rounded-xl flex items-center justify-center mb-4 group-hover:scale-105 transition-transform overflow-hidden shadow-inner">
              <div v-if="!s.logo" class="text-xl font-black text-emerald-600/30 tracking-tighter">
                {{ s.companyName[0] }}
              </div>
              <img v-else :src="s.logo" class="w-full h-full object-contain p-2" />
            </div>
            <div class="text-sm font-bold text-gray-600 group-hover:text-emerald-600 transition-colors truncate w-full px-2">
              {{ s.companyName }}
            </div>
            <div class="text-[10px] text-gray-400 mt-1 flex items-center gap-1">
              <MapPin :size="10" />
              {{ s.city || s.province }}
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 按采购商搜索 (品牌墙风格) -->
    <section class="py-24 bg-white border-b border-gray-100">
      <div class="max-w-7xl mx-auto px-4">
        <div class="flex flex-col md:flex-row md:items-end justify-between mb-12 gap-4">
          <div>
            <h2 class="text-3xl font-extrabold text-gray-900 tracking-tight">
              按 <span class="text-blue-600">采购商</span> 搜索
            </h2>
          </div>
          <button 
            class="group flex items-center gap-2 text-sm font-bold text-gray-500 hover:text-blue-600 transition-colors"
            @click="go('/hall/need')"
          >
            查看所有采购商
            <div class="w-8 h-8 rounded-full bg-gray-50 group-hover:bg-blue-50 flex items-center justify-center transition-colors">
              <ChevronRight :size="16" />
            </div>
          </button>
        </div>

        <div v-if="dataLoading" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
          <div v-for="i in 12" :key="i" class="h-32 bg-gray-50 rounded-2xl animate-pulse"></div>
        </div>
        <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
          <div
            v-for="b in buyers.slice(0, 12)"
            :key="b.id"
            class="group bg-white p-6 rounded-2xl border border-gray-100 transition-all cursor-pointer flex flex-col items-center justify-center text-center hover:shadow-lg hover:border-blue-100 hover:-translate-y-1"
            @click="go(`/companies/${b.id}`)"
          >
            <div class="w-16 h-16 bg-gray-50 rounded-xl flex items-center justify-center mb-4 group-hover:scale-105 transition-transform overflow-hidden shadow-inner">
              <div v-if="!b.logo" class="text-xl font-black text-blue-600/30 tracking-tighter">
                {{ b.companyName[0] }}
              </div>
              <img v-else :src="b.logo" class="w-full h-full object-contain p-2" />
            </div>
            <div class="text-sm font-bold text-gray-600 group-hover:text-blue-600 transition-colors truncate w-full px-2">
              {{ b.companyName }}
            </div>
            <div class="text-[10px] text-gray-400 mt-1 flex items-center gap-1">
              <Users :size="10" />
              {{ b.city || b.province }}
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 话题广场 -->
    <section class="max-w-7xl mx-auto px-4 py-24 border-b border-gray-100">
      <div class="flex flex-col md:flex-row md:items-end justify-between mb-16 gap-4">
        <div>
          <h2 class="text-3xl font-extrabold text-gray-900 tracking-tight">话题广场</h2>
          <p class="text-gray-500 mt-4 max-w-2xl leading-relaxed">
            通过我们的博客、指南、播客、文章、访谈等，了解所有最新新闻和信息
          </p>
        </div>
        <button 
          class="group flex items-center gap-2 text-sm font-bold text-gray-500 hover:text-emerald-600 transition-colors"
          @click="go('/talks')"
        >
          查看更多内容
          <div class="w-8 h-8 rounded-full bg-gray-50 group-hover:bg-emerald-50 flex items-center justify-center transition-colors">
            <ChevronRight :size="16" />
          </div>
        </button>
      </div>

      <div v-if="hotTopicsLoading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div v-for="i in 4" :key="i" class="bg-white rounded-2xl border border-gray-100 animate-pulse overflow-hidden">
          <div class="aspect-video bg-gray-50"></div>
          <div class="p-6 space-y-4">
            <div class="h-4 bg-gray-50 rounded w-3/4"></div>
            <div class="h-4 bg-gray-50 rounded w-1/2"></div>
          </div>
        </div>
      </div>
      <div v-else-if="hotTopics.length === 0" class="text-center py-20 bg-white rounded-3xl border border-dashed border-gray-200">
        <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4">
          <MessageCircle :size="32" class="text-gray-300" />
        </div>
        <p class="text-gray-400">暂无热门话题</p>
      </div>
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div
          v-for="post in hotTopics.slice(0, 4)"
          :key="post.id"
          class="group bg-white rounded-2xl border border-gray-100 hover:shadow-xl hover:border-emerald-100 transition-all cursor-pointer overflow-hidden flex flex-col hover:-translate-y-1"
          @click="go(`/talks/${post.id}`)"
        >
          <!-- 封面图 -->
          <div class="aspect-video overflow-hidden bg-gray-100 relative">
            <img :src="getPostCover(post)" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
            <div class="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent"></div>
          </div>

          <!-- 内容 -->
          <div class="p-6 flex-1 flex flex-col">
            <h3 class="font-bold text-gray-900 mb-3 line-clamp-2 group-hover:text-emerald-600 transition-colors leading-snug">
              {{ post.title }}
            </h3>
            <p class="text-xs text-gray-500 leading-relaxed line-clamp-3 mb-6 flex-1">
              {{ post.content || '暂无内容摘要' }}
            </p>
            
            <!-- 作者信息 -->
            <div class="flex items-center gap-3 pt-4 border-t border-gray-50">
              <div class="w-8 h-8 rounded-full bg-emerald-500 flex items-center justify-center text-[10px] font-bold text-white shadow-sm shrink-0">
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

    <!-- 平台价值核心区域 (简化) -->
    <section class="py-20 bg-gray-50 border-t border-gray-100">
      <div class="max-w-7xl mx-auto px-4">
        <div class="grid md:grid-cols-3 gap-12">
          <div class="flex items-center gap-4 group">
            <div class="w-14 h-14 bg-emerald-100 text-emerald-600 rounded-2xl flex items-center justify-center shrink-0 group-hover:scale-110 transition-transform">
              <MessageCircle :size="24" />
            </div>
            <div>
              <h3 class="font-bold text-gray-900 mb-1">高效沟通</h3>
              <p class="text-xs text-gray-500">内置专业即时通讯系统</p>
            </div>
          </div>

          <div class="flex items-center gap-4 group">
            <div class="w-14 h-14 bg-blue-100 text-blue-600 rounded-2xl flex items-center justify-center shrink-0 group-hover:scale-110 transition-transform">
              <FileText :size="24" />
            </div>
            <div>
              <h3 class="font-bold text-gray-900 mb-1">电子签约</h3>
              <p class="text-xs text-gray-500">专业合法的合同保障</p>
            </div>
          </div>

          <div class="flex items-center gap-4 group">
            <div class="w-14 h-14 bg-purple-100 text-purple-600 rounded-2xl flex items-center justify-center shrink-0 group-hover:scale-110 transition-transform">
              <MapPin :size="24" />
            </div>
            <div>
              <h3 class="font-bold text-gray-900 mb-1">全域地图</h3>
              <p class="text-xs text-gray-500">货源动态直观展示</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 底部流程引导 -->
    <section class="bg-slate-900 py-24 relative overflow-hidden border-t border-white/5">
      <div class="max-w-7xl mx-auto px-4 text-center">
        <h2 class="text-3xl font-extrabold text-white mb-4">开启您的专业交易之旅</h2>
        <p class="text-gray-400 mb-16 max-w-xl mx-auto">从发现商机到合约履行，AgriMatch 为您提供全流程保障</p>

        <div class="grid grid-cols-2 md:grid-cols-4 gap-8">
          <div class="flex flex-col items-center">
            <div class="w-16 h-16 bg-white/5 border border-white/10 text-emerald-400 rounded-2xl flex items-center justify-center mb-4 backdrop-blur-xl">
              <span class="text-2xl font-black">01</span>
            </div>
            <h4 class="font-bold text-white mb-2">发布/发现</h4>
            <p class="text-[10px] text-gray-500 px-4 leading-relaxed">精准匹配海量供需信息</p>
          </div>

          <div class="flex flex-col items-center">
            <div class="w-16 h-16 bg-white/5 border border-white/10 text-blue-400 rounded-2xl flex items-center justify-center mb-4 backdrop-blur-xl">
              <span class="text-2xl font-black">02</span>
            </div>
            <h4 class="font-bold text-white mb-2">洽谈议价</h4>
            <p class="text-[10px] text-gray-500 px-4 leading-relaxed">在线直聊，快速达成意向</p>
          </div>

          <div class="flex flex-col items-center">
            <div class="w-16 h-16 bg-white/5 border border-white/10 text-purple-400 rounded-2xl flex items-center justify-center mb-4 backdrop-blur-xl">
              <span class="text-2xl font-black">03</span>
            </div>
            <h4 class="font-bold text-white mb-2">电子签约</h4>
            <p class="text-[10px] text-gray-500 px-4 leading-relaxed">合法合规，保障双方权益</p>
          </div>

          <div class="flex flex-col items-center">
            <div class="w-16 h-16 bg-emerald-600 text-white rounded-2xl flex items-center justify-center mb-4 shadow-lg shadow-emerald-600/20">
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
  background: radial-gradient(circle at 50% -20%, #065f46 0%, #022c22 100%);
}
.map-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0.3;
  pointer-events: none;
}
</style>
