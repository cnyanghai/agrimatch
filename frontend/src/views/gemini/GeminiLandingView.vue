<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { requireAuth } from '../../utils/requireAuth'
import { getProductTree, type ProductNode } from '../../api/product'
import { listPosts, type PostResponse } from '../../api/post'
import PublicTopNav from '../../components/PublicTopNav.vue'
import PublicFooter from '../../components/PublicFooter.vue'
import { MessageCircle, FileText, MapPin, Zap, Gift, ArrowRight } from 'lucide-vue-next'

const router = useRouter()

const canvasRef = ref<HTMLCanvasElement | null>(null)
let raf: number | null = null
let onResize: any = null

// 数据库产品分类（树）
const categoryTree = ref<ProductNode[]>([])
const categoryLoading = ref(false)

// 首页热门话题（动态 Top4）
const hotTopicsLoading = ref(false)
const hotTopics = ref<PostResponse[]>([])

const topCategories = computed(() => {
  const list = categoryTree.value ?? []
  // 后端通常直接返回一级树；这里兜底按 parentId=0 过滤
  const roots = list.filter((x) => (x.parentId ?? 0) === 0)
  return roots.length ? roots : list
})

type CategoryGroup = { title: string; items: ProductNode[]; titleNode: ProductNode }
function buildGroups(cat: ProductNode): CategoryGroup[] {
  const seconds = cat.children ?? []
  if (!seconds.length) return []
  return seconds.map((s) => {
    const thirds = s.children ?? []
    return {
      title: s.name,
      titleNode: s,
      // 若无三级类目，则只展示二级标题（避免“标题=唯一子项”重复显示）
      items: thirds.length ? thirds : []
    }
  })
}

function chunk<T>(arr: T[], columns: number): T[][] {
  if (columns <= 1) return [arr]
  const colLen = Math.ceil(arr.length / columns)
  const out: T[][] = []
  for (let i = 0; i < columns; i++) {
    out.push(arr.slice(i * colLen, (i + 1) * colLen))
  }
  return out.filter((x) => x.length > 0)
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

function onPublishSupply() {
  if (!requireAuth('/supply')) return
  go('/supply')
}

function onPublishNeed() {
  if (!requireAuth('/requirements')) return
  go('/requirements')
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
    <section class="relative hero-gradient overflow-hidden text-white min-h-[600px] flex items-center">
      <canvas ref="canvasRef" class="map-canvas"></canvas>

      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full relative z-10 py-20 flex flex-col md:flex-row gap-12 md:gap-60">
        <!-- 左侧悬浮菜单 -->
        <div class="w-64 glass-card rounded-xl p-2 self-start hidden lg:block">
          <div class="p-3 font-bold border-b border-white/10 mb-2 flex items-center gap-2">
            <Zap :size="16" class="text-emerald-400" />
            全部分类
          </div>
          <div class="space-y-1 relative">
            <!-- 加载提示 -->
            <div v-if="categoryLoading" class="p-3 text-sm text-white/70">正在加载分类...</div>
            <div v-else-if="topCategories.length === 0" class="p-3 text-sm text-white/70">暂无分类数据</div>

            <!-- 一级分类 -->
            <div v-for="cat in topCategories" :key="cat.id" class="hover-menu group relative">
              <button
                type="button"
                class="w-full p-3 flex justify-between items-center cursor-pointer hover:bg-white/10 rounded-lg transition-colors text-left"
                @click="goCategory(cat)"
              >
                <span class="truncate pr-2">{{ cat.name }}</span>
                <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M9 5l7 7-7 7"></path></svg>
              </button>

              <!-- 悬浮二/三级菜单 -->
              <div
                v-if="(cat.children?.length ?? 0) > 0"
                class="submenu hidden absolute left-full top-0 w-[520px] bg-white text-gray-800 rounded-xl shadow-2xl p-6 grid-cols-3 gap-4 border border-gray-100 border-l-8 border-l-transparent z-50"
              >
                <template v-for="(col, idx) in chunk(buildGroups(cat), 3)" :key="idx">
                  <div class="space-y-5">
                    <div v-for="g in col" :key="g.title">
                      <button
                        type="button"
                        class="font-bold text-emerald-700 mb-2 cursor-pointer hover:text-emerald-600 text-left"
                        @click="goCategory(g.titleNode)"
                      >
                        {{ g.title }}
                      </button>
                      <ul class="text-sm space-y-2">
                        <li v-for="n in g.items" :key="n.id">
                          <button type="button" class="hover:text-emerald-600 cursor-pointer text-left" @click="goCategory(n)">
                            {{ n.name }}
                          </button>
                        </li>
                      </ul>
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </div>

        <!-- 文案区 -->
        <div class="flex-1 flex flex-col justify-center items-center md:items-start md:pl-8 text-center md:text-left">
          <h1 class="text-5xl md:text-7xl font-extrabold mb-6 leading-tight tracking-tight">
            连接饲料人，<br /><span class="text-emerald-400">交易更透明</span>
          </h1>
          <p class="text-xl text-gray-300 mb-10 max-w-xl leading-relaxed">
            采购经理与期货大佬观点碰撞。全产业链真实交易数据，让每一份合约都胸有成竹。
          </p>
          <div class="flex flex-wrap gap-4 justify-center md:justify-start">
            <button class="btn-primary px-10 py-4 rounded-xl text-xl font-bold shadow-lg shadow-emerald-500/20" @click="onPublishSupply">发布供应</button>
            <button class="bg-white/10 backdrop-blur-md border border-white/20 hover:bg-white/20 px-10 py-4 rounded-xl text-xl font-bold transition-all" @click="onPublishNeed">
              发布采购
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 平台价值核心区域 -->
    <section class="py-24 bg-white">
      <div class="max-w-7xl mx-auto px-4">
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-extrabold text-gray-900 mb-4">为什么选择 AgriMatch</h2>
          <p class="text-gray-500 max-w-2xl mx-auto">打破行业信息壁垒，构建饲料原料交易新生态</p>
        </div>

        <div class="grid md:grid-cols-3 gap-12">
          <!-- 价值1 -->
          <div class="flex flex-col items-center text-center group">
            <div class="w-20 h-20 bg-emerald-50 text-emerald-600 rounded-3xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
              <MessageCircle :size="32" :stroke-width="2" />
            </div>
            <h3 class="text-xl font-bold text-gray-900 mb-3">高效沟通</h3>
            <p class="text-sm text-gray-500 leading-relaxed">
              内置专业即时通讯系统，支持询价、报价、改价一键完成，所有沟通记录留档备查。
            </p>
          </div>

          <!-- 价值2 -->
          <div class="flex flex-col items-center text-center group">
            <div class="w-20 h-20 bg-blue-50 text-blue-600 rounded-3xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
              <FileText :size="32" :stroke-width="2" />
            </div>
            <h3 class="text-xl font-bold text-gray-900 mb-3">电子签约</h3>
            <p class="text-sm text-gray-500 leading-relaxed">
              基于《电子签名法》的专业合同系统，支持印章管理与在线签署，具备完整法律效力。
            </p>
          </div>

          <!-- 价值3 -->
          <div class="flex flex-col items-center text-center group">
            <div class="w-20 h-20 bg-purple-50 text-purple-600 rounded-3xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
              <MapPin :size="32" :stroke-width="2" />
            </div>
            <h3 class="text-xl font-bold text-gray-900 mb-3">全域地图</h3>
            <p class="text-sm text-gray-500 leading-relaxed">
              直观展示全国供应商分布与货源动态，支持地理位置搜索，帮助您寻找最近、最合适的货源。
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- 热门话题区域 - 占据更宽幅面 -->
    <section class="max-w-7xl mx-auto px-4 py-24 border-t border-gray-100">
      <div class="flex items-center justify-between mb-12">
        <div class="flex items-center gap-3">
          <div class="w-1.5 h-8 bg-emerald-600 rounded-full"></div>
          <h2 class="text-3xl font-extrabold text-gray-900">社区热门讨论</h2>
        </div>
        <button class="flex items-center gap-2 text-emerald-600 font-bold hover:gap-3 transition-all" @click="go('/talks')">
          进入话题广场
          <ArrowRight :size="20" />
        </button>
      </div>

      <div v-if="hotTopicsLoading" class="grid md:grid-cols-2 gap-8">
        <div v-for="i in 4" :key="i" class="bg-white p-8 rounded-3xl border border-gray-100 animate-pulse h-48"></div>
      </div>
      <div v-else-if="hotTopics.length === 0" class="text-center py-20 bg-white rounded-3xl border border-dashed border-gray-200">
        <p class="text-gray-400">暂无热门话题</p>
      </div>
      <div v-else class="grid md:grid-cols-2 gap-8">
        <div
          v-for="(post, idx) in hotTopics"
          :key="post.id"
          class="bg-white p-8 rounded-3xl border border-gray-100 hover:shadow-xl hover:border-emerald-100 transition-all cursor-pointer flex flex-col"
          @click="go(`/talks/${post.id}`)"
        >
          <div class="flex items-center gap-4 mb-6">
            <div
              class="w-12 h-12 rounded-2xl flex items-center justify-center font-bold text-white shadow-sm"
              :class="idx % 2 === 0 ? 'bg-gradient-to-br from-emerald-500 to-teal-600' : 'bg-gradient-to-br from-blue-500 to-indigo-600'"
            >
              {{ avatarText(post) }}
            </div>
            <div class="min-w-0">
              <div class="font-bold text-gray-900 truncate">{{ displayName(post) }}</div>
              <div class="text-xs text-gray-400 truncate">
                {{ post.companyName || '行业同仁' }} · {{ formatTime(post.createTime) }}
              </div>
            </div>
            <div class="ml-auto flex items-center gap-1.5 text-xs font-bold text-emerald-600 bg-emerald-50 px-3 py-1 rounded-full">
              <Zap :size="12" />
              热议中
            </div>
          </div>
          
          <h3 class="text-xl font-bold text-gray-900 mb-4 line-clamp-1 group-hover:text-emerald-600 transition-colors">
            {{ post.title }}
          </h3>
          <p class="text-gray-500 text-sm leading-relaxed mb-6 line-clamp-2">
            {{ post.content || '暂无内容摘要' }}
          </p>
          
          <div class="mt-auto flex items-center justify-between pt-6 border-t border-gray-50">
            <div class="flex items-center gap-4 text-xs text-gray-400 font-medium">
              <span>{{ post.commentCount ?? 0 }} 评论</span>
              <span>{{ post.likeCount ?? 0 }} 赞</span>
            </div>
            <span class="text-xs font-bold text-emerald-600 flex items-center gap-1">
              查看详情
              <ArrowRight :size="14" />
            </span>
          </div>
        </div>
      </div>
    </section>

    <!-- 底部流程引导 -->
    <section class="bg-slate-900 py-24 relative overflow-hidden">
      <div class="max-w-7xl mx-auto px-4 text-center">
        <h2 class="text-3xl font-extrabold text-white mb-4">开启您的专业交易之旅</h2>
        <p class="text-gray-400 mb-16 max-w-xl mx-auto">从发现商机到合约履行，AgriMatch 为您提供全流程保障</p>

        <div class="grid grid-cols-2 md:grid-cols-4 gap-8">
          <div class="flex flex-col items-center">
            <div class="w-16 h-16 bg-white/5 border border-white/10 text-emerald-400 rounded-2xl flex items-center justify-center mb-4 backdrop-blur-xl">
              <span class="text-2xl font-black">01</span>
            </div>
            <h4 class="font-bold text-white mb-2">发布/发现</h4>
            <p class="text-[10px] text-gray-500 px-4">精准匹配海量供需信息</p>
          </div>

          <div class="flex flex-col items-center">
            <div class="w-16 h-16 bg-white/5 border border-white/10 text-blue-400 rounded-2xl flex items-center justify-center mb-4 backdrop-blur-xl">
              <span class="text-2xl font-black">02</span>
            </div>
            <h4 class="font-bold text-white mb-2">洽谈议价</h4>
            <p class="text-[10px] text-gray-500 px-4">在线直聊，快速达成意向</p>
          </div>

          <div class="flex flex-col items-center">
            <div class="w-16 h-16 bg-white/5 border border-white/10 text-purple-400 rounded-2xl flex items-center justify-center mb-4 backdrop-blur-xl">
              <span class="text-2xl font-black">03</span>
            </div>
            <h4 class="font-bold text-white mb-2">电子签约</h4>
            <p class="text-[10px] text-gray-500 px-4">合法合规，保障双方权益</p>
          </div>

          <div class="flex flex-col items-center">
            <div class="w-16 h-16 bg-emerald-600 text-white rounded-2xl flex items-center justify-center mb-4 shadow-lg shadow-emerald-600/20">
              <Gift :size="28" />
            </div>
            <h4 class="font-bold text-white mb-2">履约/奖励</h4>
            <p class="text-[10px] text-gray-500 px-4">完成交易，赢取丰厚积分</p>
          </div>
        </div>
      </div>
    </section>

    <PublicFooter />
  </div>
</template>

<style scoped>
.hero-gradient {
  background: linear-gradient(135deg, #064e3b 0%, #022c22 100%);
}
.map-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0.4;
  pointer-events: none;
}
.hover-menu:hover .submenu {
  display: grid;
}
.glass-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}
.btn-primary {
  background: #10b981;
  transition: all 0.3s ease;
}
.btn-primary:hover {
  background: #059669;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}
</style>
