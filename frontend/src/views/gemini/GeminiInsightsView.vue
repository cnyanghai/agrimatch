<script setup lang="ts">
import { useRouter } from 'vue-router'
import { requireAuth } from '../../utils/requireAuth'
import { useUiStore } from '../../store/ui'

const router = useRouter()
const ui = useUiStore()

function go(path: string) {
  router.push(path)
}

function openLogin() {
  ui.openAuthDialog('login')
}

function onSubscribe() {
  if (!requireAuth('/insights')) return
  // 订阅简报暂未实现，先引导到控制台/论坛
  go('/posts')
}
</script>

<template>
  <div class="bg-gray-50 text-gray-900 min-h-screen">
    <nav class="bg-white border-b sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <div class="flex items-center gap-8">
            <span class="text-2xl font-bold text-indigo-600 italic cursor-pointer" @click="go('/')">AgriMatch</span>
            <div class="hidden md:flex space-x-6 text-sm font-medium text-gray-600">
              <button class="hover:text-indigo-600" @click="go('/')">首页</button>
              <button class="hover:text-indigo-600" @click="go('/hall/supply')">供应大厅</button>
              <button class="hover:text-indigo-600" @click="go('/hall/need')">采购大厅</button>
              <button class="text-indigo-600 border-b-2 border-indigo-600 pb-5" @click="go('/insights')">观点资讯</button>
              <button class="hover:text-indigo-600" @click="go('/talks')">话题广场</button>
            </div>
          </div>
          <div class="flex items-center gap-4">
            <button class="text-gray-500 hover:text-indigo-600 text-sm font-medium" @click="openLogin">登录</button>
            <button class="bg-indigo-600 text-white px-4 py-2 rounded-lg font-bold text-sm hover:bg-indigo-700 transition-colors" @click="onSubscribe">
              订阅简报
            </button>
          </div>
        </div>
      </div>
    </nav>

    <section class="bg-indigo-900 text-white py-8">
      <div class="max-w-7xl mx-auto px-4">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8 items-center">
          <div class="lg:col-span-2">
            <span class="bg-red-500 text-white text-[10px] px-2 py-1 rounded font-bold uppercase tracking-wider">置顶研报</span>
            <h1 class="text-3xl font-bold mt-4 leading-tight">2025年Q1饲料原料市场趋势分析：玉米供应宽松与大豆进口不确定性交织</h1>
            <div class="flex items-center gap-6 mt-6">
              <div class="flex items-center gap-2">
                <div class="w-8 h-8 rounded-full bg-indigo-500 flex items-center justify-center font-bold">陈</div>
                <span class="text-xs">首席分析师 陈明</span>
              </div>
              <span class="text-xs text-indigo-300">阅读时长 8 分钟</span>
              <button class="text-xs font-bold underline hover:text-white" @click="go('/posts')">阅读全文 →</button>
            </div>
          </div>
          <div class="bg-white/10 p-5 rounded-2xl backdrop-blur-sm border border-white/10">
            <h3 class="text-xs font-bold mb-4 flex justify-between">市场看涨情绪指数 <span class="text-emerald-400">偏强</span></h3>
            <div class="space-y-3">
              <div class="h-1 bg-white/10 rounded-full overflow-hidden"><div class="bg-emerald-400 h-full w-[65%]"></div></div>
              <div class="flex justify-between text-[10px] text-indigo-200"><span>玉米 65%</span><span>豆粕 42%</span><span>氨基酸 88%</span></div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <main class="max-w-7xl mx-auto px-4 py-12">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="column-card bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden flex flex-col">
          <div class="p-6 border-b-4 border-indigo-600">
            <div class="flex justify-between items-center">
              <h2 class="text-xl font-bold flex items-center gap-2">全部资讯</h2>
              <button class="p-1 hover:bg-gray-100 rounded">
                <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M4 6h16M4 12h16M4 18h16"></path></svg>
              </button>
            </div>
          </div>
          <div class="p-4 space-y-4 custom-scrollbar">
            <div class="content-item p-4 rounded-2xl transition-all cursor-pointer border border-transparent hover:border-indigo-100">
              <span class="text-[10px] font-bold text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded">行情</span>
              <h3 class="font-bold text-gray-900 mt-2 mb-1">农业部：本周豆粕均价下调</h3>
              <p class="text-xs text-gray-500 line-clamp-2">全国主要油厂库存回升，补库节奏明显放缓...</p>
              <div class="flex justify-between items-center mt-3 text-[10px] text-gray-400"><span>15分钟前</span><span>👁️ 1.4k</span></div>
            </div>
          </div>
          <div class="p-6 mt-auto">
            <button class="w-full py-3 text-xs font-bold text-indigo-600 border border-indigo-100 rounded-xl hover:bg-indigo-50 transition-colors" @click="go('/posts')">
              查看更多资讯
            </button>
          </div>
        </div>

        <div class="column-card bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden flex flex-col">
          <div class="p-6 border-b-4 border-purple-600">
            <div class="flex justify-between items-center">
              <h2 class="text-xl font-bold flex items-center gap-2">大咖观点</h2>
              <span class="text-[10px] text-purple-600 font-bold bg-purple-50 px-2 py-1 rounded-full uppercase">专家智库</span>
            </div>
          </div>
          <div class="p-4 space-y-4">
            <div class="content-item p-5 rounded-2xl bg-gradient-to-br from-white to-purple-50 border border-purple-100 cursor-pointer">
              <div class="flex items-center gap-3 mb-3">
                <div class="w-10 h-10 rounded-full bg-gray-200 overflow-hidden shrink-0"><img src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix" /></div>
                <div>
                  <h4 class="font-bold text-sm">李华 <span class="text-[8px] bg-yellow-400 px-1 rounded">V</span></h4>
                  <p class="text-[10px] text-gray-400">大连商报特约分析师</p>
                </div>
              </div>
              <h3 class="font-bold text-gray-900 text-sm leading-snug">“豆粕3000点保卫战：现货商该如何套保？”</h3>
              <div class="mt-4 flex justify-between items-center">
                <span class="text-[10px] text-purple-600 font-bold">阅读解析 →</span>
                <span class="text-[10px] text-gray-400">1.2k 赞同</span>
              </div>
            </div>
          </div>
          <div class="p-6 mt-auto">
            <button class="w-full py-3 text-xs font-bold text-purple-600 border border-purple-100 rounded-xl hover:bg-purple-50 transition-colors" @click="go('/posts')">
              申请入驻专家库
            </button>
          </div>
        </div>

        <div class="column-card bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden flex flex-col">
          <div class="p-6 border-b-4 border-amber-500">
            <div class="flex justify-between items-center">
              <h2 class="text-xl font-bold flex items-center gap-2">热门话题</h2>
              <span class="text-[10px] text-amber-600 font-bold bg-amber-50 px-2 py-1 rounded-full uppercase animate-pulse">Live</span>
            </div>
          </div>
          <div class="p-4 space-y-3">
            <div class="content-item p-4 rounded-2xl border border-gray-50 hover:border-amber-200 cursor-pointer" @click="go('/talks')">
              <div class="flex justify-between text-[10px] mb-2 font-bold italic"><span class="text-amber-500">TOP 01</span><span class="text-gray-400">1.5k 讨论</span></div>
              <h4 class="text-sm font-bold text-gray-800"># 东北玉米地趴粮何时出货？</h4>
            </div>
          </div>
          <div class="p-6 mt-auto">
            <button class="w-full py-3 text-xs font-bold text-amber-600 border border-amber-100 rounded-xl hover:bg-amber-50 transition-colors" @click="go('/talks')">
              进入话题广场
            </button>
          </div>
        </div>
      </div>
    </main>

    <footer class="bg-white border-t py-12">
      <div class="max-w-7xl mx-auto px-4 flex flex-col md:flex-row justify-between items-center gap-8">
        <div class="text-center md:text-left">
          <span class="text-xl font-bold text-indigo-600 italic">AgriMatch</span>
          <p class="text-[10px] text-gray-400 mt-2">专业的饲料原料撮合与资讯情报平台</p>
        </div>
        <div class="flex gap-8 text-[10px] text-gray-500 font-medium">
          <button class="hover:text-indigo-600 transition-colors" @click="go('/insights')">关于我们</button>
          <button class="hover:text-indigo-600 transition-colors" @click="go('/insights')">法律声明</button>
          <button class="hover:text-indigo-600 transition-colors" @click="go('/insights')">联系我们</button>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.column-card {
  transition: all 0.3s ease;
  height: fit-content;
}
.content-item:hover {
  background-color: #f8fafc;
  transform: translateX(4px);
}
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e2e8f0;
  border-radius: 10px;
}
</style>


