<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getProductTree, type ProductNode } from '../../api/product'
import { listPosts, type PostResponse } from '../../api/post'
import { getPlatformStats, type StatsResponse } from '../../api/stats'
import { listTopCompanies, type CompanyCardResponse } from '../../api/company'
import PublicTopNav from '../../components/PublicTopNav.vue'
import PublicFooter from '../../components/PublicFooter.vue'
import { MapPin, ArrowRight, Search, TrendingUp, Package, Truck, ShoppingBag, Gift, MessageCircle, FileText } from 'lucide-vue-next'

const router = useRouter()

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
      listTopCompanies('supplier', 8),
      listTopCompanies('buyer', 8)
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
    <section class="relative hero-gradient overflow-hidden text-white min-h-[500px] flex items-center pt-16">
      <canvas ref="canvasRef" class="map-canvas"></canvas>

      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full relative z-10 py-20 flex flex-col items-center text-center">
        <h1 class="text-4xl md:text-6xl font-extrabold mb-4 leading-tight tracking-tight">
          产品物料搜索
        </h1>
        <p class="text-xl md:text-2xl text-gray-300 mb-12 max-w-2xl leading-relaxed">
          最优质、最全面的产品库，交易更透明
        </p>

        <!-- Centered Search Box -->
        <div class="w-full max-w-3xl mb-12">
          <div class="flex p-1.5 bg-white/10 backdrop-blur-xl border border-white/20 rounded-2xl shadow-2xl focus-within:border-emerald-500/50 transition-all">
            <div class="flex-1 flex items-center px-4">
              <Search :size="20" class="text-gray-400 mr-3" />
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="搜索品种、产地、供应商或采购商..."
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
    <section class="py-20 bg-white border-b border-gray-100">
      <div class="max-w-7xl mx-auto px-4">
        <div class="flex items-center justify-between mb-10">
          <div class="flex items-center gap-3">
            <div class="w-1.5 h-6 bg-emerald-600 rounded-full"></div>
            <h2 class="text-2xl font-bold text-gray-900">按原料搜索</h2>
          </div>
          <button class="text-sm font-bold text-emerald-600 hover:underline" @click="go('/hall/supply')">查看全部分类</button>
        </div>

        <div v-if="categoryLoading" class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
          <div v-for="i in 6" :key="i" class="h-32 bg-gray-50 rounded-2xl animate-pulse"></div>
        </div>
        <div v-else class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
          <div
            v-for="cat in topCategories.slice(0, 12)"
            :key="cat.id"
            class="group p-6 bg-gray-50/50 hover:bg-white border border-transparent hover:border-emerald-100 rounded-2xl transition-all cursor-pointer text-center"
            @click="goCategory(cat)"
          >
            <div class="w-12 h-12 bg-white rounded-xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform shadow-sm">
              <Package :size="24" class="text-emerald-600" />
            </div>
            <div class="font-bold text-gray-900 group-hover:text-emerald-600 transition-colors">{{ cat.name }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 按供应商搜索 -->
    <section class="py-20 bg-gray-50/50 border-b border-gray-100">
      <div class="max-w-7xl mx-auto px-4">
        <div class="flex items-center justify-between mb-10">
          <div class="flex items-center gap-3">
            <div class="w-1.5 h-6 bg-blue-600 rounded-full"></div>
            <h2 class="text-2xl font-bold text-gray-900">按供应商搜索</h2>
          </div>
          <button class="text-sm font-bold text-blue-600 hover:underline" @click="go('/hall/supply')">查看更多供应商</button>
        </div>

        <div v-if="dataLoading" class="grid md:grid-cols-4 gap-4">
          <div v-for="i in 4" :key="i" class="h-48 bg-white rounded-2xl animate-pulse"></div>
        </div>
        <div v-else class="grid md:grid-cols-4 gap-4">
          <div
            v-for="s in suppliers"
            :key="s.id"
            class="bg-white p-6 rounded-2xl border border-gray-100 hover:shadow-lg hover:border-blue-100 transition-all cursor-pointer group"
            @click="go(`/companies/${s.id}`)"
          >
            <div class="flex items-center gap-3 mb-4">
              <div class="w-10 h-10 bg-blue-50 text-blue-600 rounded-lg flex items-center justify-center font-bold">
                {{ s.companyName[0] }}
              </div>
              <div class="min-w-0">
                <div class="font-bold text-gray-900 truncate group-hover:text-blue-600">{{ s.companyName }}</div>
                <div class="text-xs text-gray-400">{{ s.province }} {{ s.city }}</div>
              </div>
            </div>
            <div class="space-y-2">
              <div class="flex items-center justify-between text-xs">
                <span class="text-gray-400">主营品类</span>
                <span class="text-gray-900 font-medium truncate ml-2">{{ s.categoryNames?.join(' / ') || '-' }}</span>
              </div>
              <div class="flex items-center justify-between text-xs">
                <span class="text-gray-400">供应信息</span>
                <span class="text-blue-600 font-bold">{{ s.count }} 条</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 按采购商搜索 -->
    <section class="py-20 bg-white border-b border-gray-100">
      <div class="max-w-7xl mx-auto px-4">
        <div class="flex items-center justify-between mb-10">
          <div class="flex items-center gap-3">
            <div class="w-1.5 h-6 bg-purple-600 rounded-full"></div>
            <h2 class="text-2xl font-bold text-gray-900">按采购商搜索</h2>
          </div>
          <button class="text-sm font-bold text-purple-600 hover:underline" @click="go('/hall/need')">查看更多采购商</button>
        </div>

        <div v-if="dataLoading" class="grid md:grid-cols-4 gap-4">
          <div v-for="i in 4" :key="i" class="h-48 bg-gray-50 rounded-2xl animate-pulse"></div>
        </div>
        <div v-else class="grid md:grid-cols-4 gap-4">
          <div
            v-for="b in buyers"
            :key="b.id"
            class="bg-white p-6 rounded-2xl border border-gray-100 hover:shadow-lg hover:border-purple-100 transition-all cursor-pointer group"
            @click="go(`/companies/${b.id}`)"
          >
            <div class="flex items-center gap-3 mb-4">
              <div class="w-10 h-10 bg-purple-50 text-purple-600 rounded-lg flex items-center justify-center font-bold">
                {{ b.companyName[0] }}
              </div>
              <div class="min-w-0">
                <div class="font-bold text-gray-900 truncate group-hover:text-purple-600">{{ b.companyName }}</div>
                <div class="text-xs text-gray-400">{{ b.province }} {{ b.city }}</div>
              </div>
            </div>
            <div class="space-y-2">
              <div class="flex items-center justify-between text-xs">
                <span class="text-gray-400">采购品类</span>
                <span class="text-gray-900 font-medium truncate ml-2">{{ b.categoryNames?.join(' / ') || '-' }}</span>
              </div>
              <div class="flex items-center justify-between text-xs">
                <span class="text-gray-400">需求信息</span>
                <span class="text-purple-600 font-bold">{{ b.count }} 条</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 话题广场 -->
    <section class="max-w-7xl mx-auto px-4 py-24">
      <div class="text-center mb-16">
        <h2 class="text-3xl font-extrabold text-gray-900 mb-4">话题广场</h2>
        <p class="text-gray-500 max-w-2xl mx-auto">
          通过我们的博客、指南、播客、文章、访谈等，了解所有最新新闻和信息
        </p>
      </div>

      <div v-if="hotTopicsLoading" class="grid md:grid-cols-3 gap-8">
        <div v-for="i in 3" :key="i" class="bg-white p-8 rounded-3xl border border-gray-100 animate-pulse h-64"></div>
      </div>
      <div v-else-if="hotTopics.length === 0" class="text-center py-20 bg-white rounded-3xl border border-dashed border-gray-200">
        <p class="text-gray-400">暂无热门话题</p>
      </div>
      <div v-else class="grid md:grid-cols-3 gap-8">
        <div
          v-for="(post, idx) in hotTopics.slice(0, 3)"
          :key="post.id"
          class="bg-white p-8 rounded-3xl border border-gray-100 hover:shadow-xl hover:border-emerald-100 transition-all cursor-pointer flex flex-col group"
          @click="go(`/talks/${post.id}`)"
        >
          <div class="flex items-center gap-3 mb-6">
            <div
              class="w-10 h-10 rounded-xl flex items-center justify-center font-bold text-white shadow-sm text-xs"
              :class="idx % 2 === 0 ? 'bg-emerald-500' : 'bg-blue-500'"
            >
              {{ avatarText(post) }}
            </div>
            <div class="min-w-0">
              <div class="font-bold text-gray-900 text-sm truncate">{{ displayName(post) }}</div>
              <div class="text-[10px] text-gray-400 truncate">{{ formatTime(post.createTime) }}</div>
            </div>
          </div>
          
          <h3 class="text-lg font-bold text-gray-900 mb-4 line-clamp-2 group-hover:text-emerald-600 transition-colors">
            {{ post.title }}
          </h3>
          <p class="text-gray-500 text-xs leading-relaxed mb-6 line-clamp-3">
            {{ post.content || '暂无内容摘要' }}
          </p>
          
          <div class="mt-auto flex items-center justify-between pt-6 border-t border-gray-50 text-[10px]">
            <div class="flex items-center gap-3 text-gray-400 font-medium">
              <span>{{ post.commentCount ?? 0 }} 评论</span>
              <span>{{ post.likeCount ?? 0 }} 赞</span>
            </div>
            <span class="font-bold text-emerald-600 flex items-center gap-1">
              阅读全文
              <ArrowRight :size="12" />
            </span>
          </div>
        </div>
      </div>

      <div class="text-center mt-12">
        <button class="px-8 py-3 bg-gray-900 text-white rounded-xl font-bold hover:bg-black transition-all active:scale-95" @click="go('/talks')">
          查看更多内容
        </button>
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
