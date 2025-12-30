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

function onPublishSupply() {
  if (!requireAuth('/supply')) return
  go('/supply')
}

function onConsult() {
  if (!requireAuth('/chat')) return
  go('/chat')
}
</script>

<template>
  <div class="bg-gray-50 text-gray-900 min-h-screen">
    <!-- 顶部行情走马灯 -->
    <div class="bg-slate-900 text-white py-2 overflow-hidden">
      <div class="flex animate-marquee space-x-12 px-4 text-xs font-medium">
        <span>玉米主力 2505: <b class="text-red-400">2450 ↑</b></span>
        <span>豆粕主力 2505: <b class="text-green-400">3210 ↓</b></span>
        <span>菜粕 2505: <b class="text-red-400">2680 ↑</b></span>
        <span>山东现货玉米: <b>2380</b></span>
        <span>广东港口玉米: <b>2520</b></span>
        <span>玉米主力 2505: <b class="text-red-400">2450 ↑</b></span>
        <span>豆粕主力 2505: <b class="text-green-400">3210 ↓</b></span>
      </div>
    </div>

    <!-- 导航栏 -->
    <nav class="bg-white border-b sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <div class="flex items-center gap-8">
            <span class="text-2xl font-bold text-emerald-600 italic cursor-pointer" @click="go('/')">AgriMatch</span>
            <div class="hidden md:flex space-x-6 text-sm font-medium text-gray-600">
              <button class="hover:text-emerald-600" @click="go('/')">首页</button>
              <button class="text-emerald-600 border-b-2 border-emerald-600 pb-5" @click="go('/hall/supply')">供应大厅</button>
              <button class="hover:text-emerald-600" @click="go('/hall/need')">采购大厅</button>
              <button class="hover:text-emerald-600" @click="go('/insights')">观点资讯</button>
              <button class="hover:text-emerald-600" @click="go('/talks')">话题广场</button>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <button class="text-sm text-gray-500 hover:text-emerald-600" @click="openLogin">登录/注册</button>
            <button class="bg-emerald-600 text-white px-5 py-2 rounded-lg font-bold text-sm hover:bg-emerald-700 transition-colors" @click="onPublishSupply">
              发布供应
            </button>
          </div>
        </div>
      </div>
    </nav>

    <!-- 筛选区 -->
    <section class="bg-white border-b shadow-sm">
      <div class="max-w-7xl mx-auto px-4 py-6">
        <div class="flex flex-col md:flex-row gap-4 mb-6">
          <div class="flex-1 relative">
            <input
              type="text"
              placeholder="搜索品种、产地、指标或公司..."
              class="w-full border-2 border-gray-100 rounded-xl py-2.5 px-10 focus:border-emerald-500 outline-none transition-all"
            />
            <svg class="w-5 h-5 absolute left-3 top-3 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
          </div>
          <button class="px-8 py-2.5 bg-emerald-600 text-white rounded-xl font-bold hover:bg-emerald-700 transition-all">查询信息</button>
        </div>

        <div class="space-y-4">
          <div class="flex items-start gap-4 text-xs">
            <span class="text-gray-400 shrink-0 mt-1.5 font-medium">常见品种:</span>
            <div class="flex flex-wrap gap-2">
              <button class="filter-tag active px-3 py-1 border rounded-full">全部</button>
              <button class="filter-tag px-3 py-1 border rounded-full">玉米</button>
              <button class="filter-tag px-3 py-1 border rounded-full">豆粕</button>
              <button class="filter-tag px-3 py-1 border rounded-full">棉粕</button>
              <button class="filter-tag px-3 py-1 border rounded-full">菜粕</button>
              <button class="filter-tag px-3 py-1 border rounded-full">DDGS</button>
              <button class="filter-tag px-3 py-1 border rounded-full">赖氨酸</button>
              <button class="filter-tag px-3 py-1 border rounded-full">蛋氨酸</button>
              <button class="filter-tag px-3 py-1 border rounded-full">磷酸氢钙</button>
            </div>
          </div>
          <div class="flex items-start gap-4 text-xs">
            <span class="text-gray-400 shrink-0 mt-1.5 font-medium">物流区域:</span>
            <div class="flex flex-wrap gap-2">
              <button class="filter-tag px-3 py-1 border rounded-full">东北区</button>
              <button class="filter-tag px-3 py-1 border rounded-full">华北区</button>
              <button class="filter-tag px-3 py-1 border rounded-full">华中区</button>
              <button class="filter-tag px-3 py-1 border rounded-full">华东沿海</button>
              <button class="filter-tag px-3 py-1 border rounded-full">华南沿海</button>
              <button class="filter-tag px-3 py-1 border rounded-full">西南区</button>
              <button class="filter-tag px-3 py-1 border rounded-full">西北区</button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 列表区（先按设计稿静态，后续二期接接口替换） -->
    <main class="max-w-7xl mx-auto px-4 py-8">
      <div class="space-y-4">
        <div class="supply-card bg-white rounded-xl p-5 border border-gray-100 transition-all">
          <div class="flex flex-col lg:flex-row items-center gap-6">
            <div class="w-full lg:w-44 flex items-center gap-3 shrink-0 border-r border-gray-50 pr-4">
              <div class="w-12 h-12 bg-emerald-100 text-emerald-700 rounded-lg flex items-center justify-center text-xl font-bold shrink-0">泰</div>
              <div class="overflow-hidden">
                <div class="text-sm font-bold text-gray-900 truncate">泰达粮油有限公司</div>
                <div class="flex items-center gap-1 mt-1">
                  <span class="bg-blue-50 text-blue-600 text-[10px] px-1 py-0.5 rounded">实名</span>
                  <span class="text-[10px] text-yellow-600 font-bold">★ 5.0</span>
                </div>
              </div>
            </div>

            <div class="w-full lg:w-64 shrink-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="bg-orange-100 text-orange-600 text-[10px] px-1.5 py-0.5 rounded font-bold">现货</span>
                <span class="text-gray-400 text-[10px]">编号: S24126</span>
              </div>
              <h3 class="text-lg font-bold text-gray-900 truncate">24年新季东北二等玉米</h3>
              <div class="mt-1 text-xl font-black text-red-600 italic">¥2350 <span class="text-xs font-normal text-gray-400 not-italic">元/吨</span></div>
            </div>

            <div class="flex-1 grid grid-cols-2 md:grid-cols-4 gap-4 text-xs py-2 bg-gray-50/50 rounded-lg px-4">
              <div>
                <div class="text-gray-400 mb-0.5">水分/容重</div>
                <div class="font-semibold text-gray-700">14.5% / 710g</div>
              </div>
              <div>
                <div class="text-gray-400 mb-0.5">产地/发货地</div>
                <div class="font-semibold text-gray-700">吉林 / 锦州港</div>
              </div>
              <div>
                <div class="text-gray-400 mb-0.5">起订/库存</div>
                <div class="font-semibold text-gray-700">30吨 / 500吨</div>
              </div>
              <div>
                <div class="text-gray-400 mb-0.5">物流方式</div>
                <div class="font-semibold text-gray-700">火运/汽运</div>
              </div>
            </div>

            <div class="shrink-0 flex flex-col items-center gap-2">
              <button class="px-8 py-3 bg-emerald-600 text-white rounded-xl font-bold text-sm shadow-md shadow-emerald-50 hover:bg-emerald-700 transition-all active:scale-95" @click="onConsult">
                立即咨询
              </button>
              <span class="text-[10px] text-gray-400">今日已有 12 人咨询</span>
            </div>
          </div>
        </div>
      </div>

      <div class="flex justify-center mt-10 gap-2">
        <button class="px-4 py-2 bg-white border rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition-colors">上一页</button>
        <button class="px-4 py-2 bg-emerald-600 text-white border-emerald-600 rounded-lg text-sm font-bold">1</button>
        <button class="px-4 py-2 bg-white border rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition-colors">2</button>
        <button class="px-4 py-2 bg-white border rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition-colors">3</button>
        <button class="px-4 py-2 bg-white border rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition-colors">下一页</button>
      </div>
    </main>

    <footer class="bg-white border-t py-8 mt-12">
      <div class="max-w-7xl mx-auto px-4 text-center">
        <p class="text-xs text-gray-400">© 2024 AgriMatch - 饲料原料全产业链高效撮合平台</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.filter-tag:hover {
  border-color: #10b981;
  color: #10b981;
}
.filter-tag.active {
  background-color: #10b981;
  color: white;
  border-color: #10b981;
}
.supply-card:hover {
  border-color: #10b981;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.05);
}
.animate-marquee {
  display: inline-block;
  animation: marquee 30s linear infinite;
}
@keyframes marquee {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}
</style>


