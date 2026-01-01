<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { requireAuth } from '../../utils/requireAuth'
import { getProductTree, type ProductNode } from '../../api/product'
import PublicTopNav from '../../components/PublicTopNav.vue'

const router = useRouter()

const canvasRef = ref<HTMLCanvasElement | null>(null)
let raf: number | null = null
let onResize: any = null

// 数据库产品分类（树）
const categoryTree = ref<ProductNode[]>([])
const categoryLoading = ref(false)

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
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const points: Array<{ x: number; y: number; vx: number; vy: number; size: number }> = []
  const numPoints = 80

  function resize() {
    canvas.width = canvas.offsetWidth
    canvas.height = canvas.offsetHeight
  }

  onResize = () => resize()
  window.addEventListener('resize', onResize)
  resize()

  for (let i = 0; i < numPoints; i++) {
    points.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      vx: (Math.random() - 0.5) * 0.5,
      vy: (Math.random() - 0.5) * 0.5,
      size: Math.random() * 2 + 1
    })
  }

  const loop = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    ctx.strokeStyle = 'rgba(16, 185, 129, 0.15)'
    ctx.lineWidth = 0.5

    points.forEach((p, i) => {
      p.x += p.vx
      p.y += p.vy
      if (p.x < 0 || p.x > canvas.width) p.vx *= -1
      if (p.y < 0 || p.y > canvas.height) p.vy *= -1

      ctx.beginPath()
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
      const alpha = 0.3 + Math.sin(Date.now() * 0.001 + i) * 0.2
      ctx.fillStyle = `rgba(16, 185, 129, ${alpha})`
      ctx.fill()

      for (let j = i + 1; j < points.length; j++) {
        const p2 = points[j]
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
        <div class="w-64 glass-card rounded-xl p-2 self-start">
          <div class="p-3 font-bold border-b border-white/10 mb-2">全部分类</div>
          <div class="space-y-1 relative">
            <!-- 加载提示 -->
            <div v-if="categoryLoading" class="p-3 text-sm text-white/70">正在加载分类...</div>
            <div v-else-if="topCategories.length === 0" class="p-3 text-sm text-white/70">暂无分类数据</div>

            <!-- 一级分类 -->
            <div v-for="cat in topCategories" :key="cat.id" class="hover-menu group">
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
                class="submenu hidden absolute left-full top-0 ml-2 w-[520px] bg-white text-gray-800 rounded-xl shadow-2xl p-6 grid-cols-3 gap-4 border z-50"
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
          <h1 class="text-5xl md:text-6xl font-extrabold mb-6 leading-tight">
            连接饲料人，<br /><span class="text-emerald-400">交易更透明</span>
          </h1>
          <p class="text-xl text-gray-300 mb-10 max-w-xl">
            采购经理与期货大佬观点碰撞。全产业链真实交易数据，让每一份合约都胸有成竹。
          </p>
          <div class="flex flex-wrap gap-4 justify-center md:justify-start">
            <button class="btn-primary px-10 py-4 rounded-xl text-xl font-bold" @click="onPublishSupply">发布供应</button>
            <button class="bg-white/10 backdrop-blur-md border border-white/20 hover:bg-white/20 px-10 py-4 rounded-xl text-xl font-bold transition-all" @click="onPublishNeed">
              发布采购
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 行业洞察与社区精选 -->
    <section class="max-w-7xl mx-auto px-4 py-20">
      <div class="grid md:grid-cols-3 gap-8">
        <div>
          <div class="flex items-center gap-2 mb-6">
            <div class="w-1 h-6 bg-emerald-600"></div>
            <h3 class="text-xl font-bold">热门话题</h3>
          </div>
          <div class="space-y-4">
            <div class="bg-white p-5 rounded-2xl border hover:shadow-md transition-shadow cursor-pointer" @click="go('/talks')">
              <div class="flex items-center gap-3 mb-3">
                <div class="w-10 h-10 rounded-full bg-orange-100 flex items-center justify-center font-bold text-orange-600">老</div>
                <div>
                  <div class="font-bold text-sm">老王谈粮</div>
                  <div class="text-xs text-gray-500">10分钟前</div>
                </div>
              </div>
              <p class="text-gray-700 leading-relaxed mb-4">“大雪封路，东北玉米发运困难，短期内南方销区价格还要涨吗？”</p>
              <div class="flex items-center text-xs text-emerald-600 font-semibold bg-emerald-50 w-max px-2 py-1 rounded">128人参与讨论</div>
            </div>

            <div class="bg-white p-5 rounded-2xl border hover:shadow-md transition-shadow cursor-pointer" @click="go('/talks')">
              <div class="flex items-center gap-3 mb-3">
                <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center font-bold text-blue-600">李</div>
                <div>
                  <div class="font-bold text-sm">饲料配方师李工</div>
                  <div class="text-xs text-gray-500">2小时前</div>
                </div>
              </div>
              <p class="text-gray-700 leading-relaxed mb-4">“最近DDGS的毒素问题大家怎么看？有没有好的替代方案？”</p>
              <div class="flex items-center text-xs text-blue-600 font-semibold bg-blue-50 w-max px-2 py-1 rounded">56条评论</div>
            </div>
          </div>
        </div>

        <div>
          <div class="flex items-center gap-2 mb-6">
            <div class="w-1 h-6 bg-blue-600"></div>
            <h3 class="text-xl font-bold">大咖观点</h3>
          </div>
          <div class="space-y-4">
            <div class="bg-slate-900 text-white p-5 rounded-2xl relative overflow-hidden">
              <div class="absolute top-0 right-0 p-2">
                <span class="text-[10px] bg-yellow-500 text-black px-2 py-1 rounded-full font-bold">V 认证</span>
              </div>
              <div class="flex items-center gap-3 mb-4">
                <div class="w-10 h-10 rounded-full bg-gray-700 overflow-hidden">
                  <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix" alt="avatar" />
                </div>
                <div>
                  <div class="font-bold text-sm">期货冠军李华</div>
                  <div class="text-xs text-gray-400">大连商报特约专家</div>
                </div>
              </div>
              <h4 class="font-bold text-lg mb-3">“豆粕主力合约一周走势预测”</h4>
              <p class="text-gray-400 text-sm mb-4">基于南美干旱预测及国内压榨厂库存数据，我认为...</p>
              <div class="flex justify-between items-center">
                <span class="text-xs text-emerald-400">528人参与讨论</span>
                <button class="text-xs underline hover:text-emerald-400" @click="go('/insights')">查看全文</button>
              </div>
            </div>
          </div>
        </div>

        <div>
          <div class="flex items-center gap-2 mb-6">
            <div class="w-1 h-6 bg-red-600"></div>
            <h3 class="text-xl font-bold">官方资讯</h3>
          </div>
          <div class="bg-gray-100 rounded-2xl p-6">
            <ul class="space-y-4">
              <li class="group cursor-pointer" @click="go('/insights')">
                <div class="text-xs text-gray-400 mb-1">2024.12.26</div>
                <h4 class="font-medium group-hover:text-emerald-600 transition-colors">《2024年小麦市场行情展望与年度白皮书》</h4>
              </li>
              <li class="group cursor-pointer border-t pt-4" @click="go('/insights')">
                <div class="text-xs text-gray-400 mb-1">2024.12.25</div>
                <h4 class="font-medium group-hover:text-emerald-600 transition-colors">全国主要饲料原料价格日报 (第48周)</h4>
              </li>
              <li class="group cursor-pointer border-t pt-4" @click="go('/insights')">
                <div class="text-xs text-gray-400 mb-1">2024.12.24</div>
                <h4 class="font-medium group-hover:text-emerald-600 transition-colors">新版饲料添加剂安全准则发布...</h4>
              </li>
            </ul>
            <button class="w-full mt-6 py-2 border border-gray-300 rounded-lg text-sm hover:bg-white transition-all" @click="go('/insights')">查看更多数据</button>
          </div>
        </div>
      </div>
    </section>

    <!-- 流程 -->
    <section class="bg-white py-24 relative overflow-hidden">
      <div class="max-w-7xl mx-auto px-4 text-center">
        <h2 class="text-3xl font-extrabold mb-2">在这里，交流就是价值</h2>
        <p class="text-gray-500 mb-16">打破信息孤岛，每一句对话、每一个观点都在沉淀财富</p>

        <div class="relative flex flex-col md:flex-row justify-between items-start gap-12 px-10">
          <div class="hidden md:block step-line"></div>

          <div class="relative z-10 flex-1 flex flex-col items-center">
            <div class="w-16 h-16 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center mb-4 border-4 border-white shadow-lg">
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path></svg>
            </div>
            <h4 class="font-bold mb-2">聊生意</h4>
            <p class="text-xs text-gray-500">在线直聊，保护隐私</p>
          </div>

          <div class="relative z-10 flex-1 flex flex-col items-center">
            <div class="w-16 h-16 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center mb-4 border-4 border-white shadow-lg">
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path></svg>
            </div>
            <h4 class="font-bold mb-2">签合同</h4>
            <p class="text-xs text-gray-500">电子签约，法律保障</p>
          </div>

          <div class="relative z-10 flex-1 flex flex-col items-center">
            <div class="w-16 h-16 bg-purple-100 text-purple-600 rounded-full flex items-center justify-center mb-4 border-4 border-white shadow-lg">
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M9 20l-5.447-2.724A2 2 0 013 15.488V5.112a2 2 0 011.176-1.815l7-3.111a2 2 0 011.648 0l7 3.111A2 2 0 0121 5.112v10.376a2 2 0 01-1.176 1.815L14.382 20a2 2 0 01-1.764 0L9 20z"></path></svg>
            </div>
            <h4 class="font-bold mb-2">全域地图</h4>
            <p class="text-xs text-gray-500">地图找商，近距离寻货</p>
          </div>

          <div class="relative z-10 flex-1 flex flex-col items-center">
            <div class="w-16 h-16 bg-yellow-100 text-yellow-600 rounded-full flex items-center justify-center mb-4 border-4 border-white shadow-lg">
              <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            </div>
            <h4 class="font-bold mb-2">得积分/赏金</h4>
            <p class="text-xs text-gray-500">发帖、打赏获收益</p>
          </div>

          <div class="relative z-10 flex-1 flex flex-col items-center group">
            <div class="w-20 h-20 bg-emerald-600 text-white rounded-full flex items-center justify-center mb-4 border-4 border-white shadow-xl transform group-hover:scale-110 transition-transform cursor-pointer">
              <svg class="w-10 h-10" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"></path></svg>
            </div>
            <h4 class="font-bold mb-2 text-emerald-600">换好礼</h4>
            <p class="text-xs text-gray-500">iPhone、京东卡等你拿</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="bg-gray-900 text-gray-400 py-16">
      <div class="max-w-7xl mx-auto px-4">
        <div class="flex flex-col md:flex-row justify-between items-center gap-8">
          <div>
            <span class="text-2xl font-bold text-white mb-4 block">AgriMatch</span>
            <p class="text-sm">© 2024 AgriMatch 饲料人交易社区. All rights reserved.</p>
          </div>

          <div class="flex gap-8 items-center">
            <div class="text-right">
              <p class="text-white font-medium">APP 聊天更方便</p>
              <p class="text-xs">随时随地 掌握商机</p>
            </div>
            <div class="w-24 h-24 bg-white p-2 rounded-lg">
              <div class="w-full h-full bg-slate-800 rounded flex items-center justify-center">
                <svg class="w-12 h-12 text-white/50" fill="currentColor" viewBox="0 0 24 24"><path d="M3 3h8v8H3V3zm10 0h8v8h-8V3zM3 13h8v8H3v-8zm13 0h5v2h-5v-2zm0 3h2v5h-2v-5zm3 0h2v2h-2v-2zm0 3h2v2h-2v-2z"></path></svg>
              </div>
            </div>
          </div>
        </div>
      </div>
    </footer>
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
.step-line {
  position: absolute;
  top: 2rem;
  left: 10%;
  right: 10%;
  height: 2px;
  background: #d1d5db;
  z-index: 0;
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


