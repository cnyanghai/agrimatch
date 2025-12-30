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

function onPublishTalk() {
  if (!requireAuth('/talks/publish')) return
  go('/talks/publish')
}

function onEnterMy() {
  if (!requireAuth('/console')) return
  go('/console')
}
</script>

<template>
  <div class="bg-gray-50 text-gray-900 min-h-screen">
    <!-- 导航栏 -->
    <nav class="bg-white border-b sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <div class="flex items-center gap-8">
            <span class="text-2xl font-bold text-indigo-600 italic cursor-pointer" @click="go('/')">AgriMatch</span>
            <div class="hidden md:flex space-x-6 text-sm font-medium text-gray-600">
              <button class="hover:text-indigo-600" @click="go('/')">首页</button>
              <button class="hover:text-indigo-600" @click="go('/hall/supply')">交易大厅</button>
              <button class="hover:text-indigo-600" @click="go('/insights')">观点资讯</button>
              <button class="text-indigo-600 border-b-2 border-indigo-600 pb-5" @click="go('/talks')">话题广场</button>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <button class="text-sm text-gray-500 hover:text-indigo-600" @click="openLogin">登录/注册</button>
            <button class="bg-indigo-600 text-white px-5 py-2 rounded-full font-bold text-sm hover:bg-indigo-700 transition-all flex items-center gap-2" @click="onPublishTalk">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
              发布话题
            </button>
          </div>
        </div>
      </div>
    </nav>

    <!-- 头部 -->
    <header class="bg-white border-b">
      <div class="max-w-7xl mx-auto px-4 py-8">
        <div class="flex flex-col md:flex-row md:items-end justify-between gap-6">
          <div>
            <h1 class="text-3xl font-extrabold text-gray-900 font-sans tracking-tight">话题广场</h1>
            <p class="text-gray-500 mt-2">在这里，每一个关于饲料原料的思考都有价值</p>
          </div>
          <div class="bg-amber-50 border border-amber-100 rounded-2xl p-4 flex items-center gap-4">
            <div class="w-12 h-12 bg-amber-100 rounded-full flex items-center justify-center text-amber-600">
              <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"></path></svg>
            </div>
            <div>
              <p class="text-xs font-bold text-amber-800">发布高质量话题</p>
              <p class="text-[10px] text-amber-600 italic">最高可得 500 积分，兑换实物好礼</p>
            </div>
            <button class="bg-amber-500 text-white text-[10px] font-bold px-4 py-2 rounded-lg hover:bg-amber-600 transition-colors" @click="onPublishTalk">立即参与</button>
          </div>
        </div>

        <div class="flex items-center gap-8 mt-10 border-b relative">
          <button class="pb-4 px-2 text-sm font-bold tab-active">全部话题</button>
          <button class="pb-4 px-2 text-sm font-medium text-gray-500 hover:text-indigo-600 transition-colors flex items-center gap-1.5">
            赏金求助
            <span class="w-2 h-2 bg-red-500 rounded-full"></span>
          </button>
        </div>
      </div>
    </header>

    <main class="max-w-7xl mx-auto px-4 py-8">
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
        <!-- 列表 -->
        <div class="lg:col-span-8 space-y-6">
          <div class="flex flex-col sm:flex-row gap-4 mb-8">
            <div class="flex-1 relative search-focus border bg-white rounded-xl transition-all">
              <input type="text" placeholder="搜索你感兴趣的话题或关键字..." class="w-full h-11 bg-transparent px-10 text-sm outline-none" />
              <svg class="w-4 h-4 text-gray-400 absolute left-4 top-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
            </div>
            <div class="relative min-w-[120px]">
              <select class="w-full h-11 bg-white border rounded-xl px-4 text-sm font-medium text-gray-600 appearance-none outline-none cursor-pointer hover:bg-gray-50">
                <option>最热排序</option>
                <option>最新发布</option>
                <option>积分最高</option>
                <option>最多评论</option>
              </select>
              <svg class="w-4 h-4 text-gray-400 absolute right-4 top-3.5 pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M19 9l-7 7-7-7"></path></svg>
            </div>
          </div>

          <div class="topic-card bg-white rounded-3xl p-6 border border-gray-100 transition-all cursor-pointer">
            <div class="flex justify-between items-start mb-4">
              <div class="flex items-center gap-3">
                <span class="bg-red-100 text-red-600 text-[10px] font-bold px-2 py-1 rounded">热议</span>
                <span class="text-[10px] text-gray-400 font-medium">15分钟前 · 玉米专区</span>
              </div>
              <button class="text-gray-300 hover:text-indigo-600">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M5 12h.01M12 12h.01M19 12h.01M6 12a1 1 0 11-2 0 1 1 0 012 0zm7 0a1 1 0 11-2 0 1 1 0 012 0zm7 0a1 1 0 11-2 0 1 1 0 012 0z"></path></svg>
              </button>
            </div>
            <h3 class="text-xl font-bold text-gray-900 mb-3 hover:text-indigo-600 transition-colors">
              东北地区“地趴粮”集中出货期将至，今年价格底部在哪里？
            </h3>
            <p class="text-sm text-gray-600 leading-relaxed line-clamp-2">
              近期气温回升较快，辽宁、吉林部分地区农户售粮意愿增强。销区需求疲软，港口库存高企，这种供需博弈下价格走势如何？
            </p>
            <div class="mt-6 flex items-center justify-between">
              <div class="flex items-center gap-4">
                <div class="flex -space-x-2">
                  <img class="w-7 h-7 rounded-full border-2 border-white bg-gray-100" src="https://api.dicebear.com/7.x/avataaars/svg?seed=John" />
                  <img class="w-7 h-7 rounded-full border-2 border-white bg-gray-200" src="https://api.dicebear.com/7.x/avataaars/svg?seed=Sarah" />
                  <img class="w-7 h-7 rounded-full border-2 border-white bg-gray-300" src="https://api.dicebear.com/7.x/avataaars/svg?seed=Mike" />
                </div>
                <span class="text-xs text-gray-400 font-medium">1,286 人正在讨论</span>
              </div>
              <div class="flex items-center gap-4 text-xs text-gray-500">
                <span class="flex items-center gap-1">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path></svg>
                  256
                </span>
                <span class="flex items-center gap-1">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path></svg>
                  842
                </span>
              </div>
            </div>
          </div>

          <button class="w-full py-4 text-sm font-bold text-gray-400 hover:text-indigo-600 transition-colors border-2 border-dashed border-gray-100 rounded-3xl hover:border-indigo-100">
            加载更多话题...
          </button>
        </div>

        <!-- 右侧 -->
        <div class="lg:col-span-4 space-y-8">
          <div class="bg-indigo-900 rounded-3xl p-6 text-white shadow-xl">
            <div class="flex items-center gap-4 mb-6">
              <div class="w-12 h-12 rounded-full bg-white/20 flex items-center justify-center font-bold text-white ring-2 ring-white/10">我</div>
              <div>
                <h4 class="font-bold">游客_7291</h4>
                <p class="text-[10px] text-indigo-300">活跃等级：青铜</p>
              </div>
            </div>
            <div class="grid grid-cols-2 gap-3 mb-6">
              <div class="bg-white/10 p-3 rounded-2xl text-center border border-white/5">
                <p class="text-lg font-bold">12</p>
                <p class="text-[8px] text-indigo-200 uppercase tracking-tighter">关注话题</p>
              </div>
              <div class="bg-white/10 p-3 rounded-2xl text-center border border-white/5">
                <p class="text-lg font-bold">350</p>
                <p class="text-[8px] text-indigo-200 uppercase tracking-tighter">累计积分</p>
              </div>
            </div>
            <button class="w-full py-3 bg-white text-indigo-900 rounded-xl text-xs font-bold hover:bg-indigo-50 transition-colors shadow-sm" @click="onEnterMy">
              进入我的讨论
            </button>
          </div>

          <div class="bg-white rounded-3xl p-6 border border-gray-100 shadow-sm">
            <div class="flex justify-between items-center mb-6">
              <h3 class="font-bold text-gray-900 flex items-center gap-2">贡献榜</h3>
              <span class="text-[10px] text-indigo-600 font-bold">周榜</span>
            </div>
            <div class="space-y-6">
              <div class="flex items-center justify-between group cursor-pointer">
                <div class="flex items-center gap-3">
                  <span class="text-sm font-black text-amber-500 w-4">1</span>
                  <div class="w-8 h-8 rounded-full bg-gray-100 overflow-hidden"><img src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix" /></div>
                  <span class="text-xs font-bold group-hover:text-indigo-600 transition-colors">老王谈粮</span>
                </div>
                <span class="text-[10px] text-gray-400">8.2k 讨论</span>
              </div>
            </div>
          </div>

          <div class="bg-gradient-to-br from-indigo-600 to-purple-700 rounded-3xl p-6 text-white relative overflow-hidden shadow-lg group">
            <div class="absolute -right-4 -bottom-4 opacity-10 group-hover:scale-110 transition-transform">
              <svg class="w-32 h-32" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M5 5a3 3 0 015-2.236A3 3 0 0114.83 6H16a2 2 0 110 4h-5V9a1 1 0 10-2 0v1H4a2 2 0 110-4h1.17C5.06 5.687 5 5.35 5 5zm4 1V5a1 1 0 10-1 1h1zm3 0a1 1 0 10-1-1v1h1z" clip-rule="evenodd"></path>
                <path d="M9 11H3v5a2 2 0 002 2h4v-7zM11 18h4a2 2 0 002-2v-5h-6v7z"></path>
              </svg>
            </div>
            <div class="relative z-10">
              <h4 class="font-bold mb-1">积分换礼品卡</h4>
              <p class="text-[10px] text-indigo-100 mb-4 italic">当前有 3 件奖品可兑换</p>
              <div class="flex items-center gap-2 mb-4">
                <span class="text-2xl font-black">JD卡</span>
                <span class="text-xs bg-white/20 px-2 py-0.5 rounded">￥1000</span>
              </div>
              <button class="text-xs font-bold underline hover:text-white" @click="go('/points')">立即前往商城 →</button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <footer class="bg-white border-t py-12 mt-12">
      <div class="max-w-7xl mx-auto px-4 text-center">
        <span class="text-xl font-bold text-indigo-600 italic">AgriMatch</span>
        <p class="text-[10px] text-gray-400 mt-4 italic">连接饲料行业每一个微小的声音</p>
        <div class="flex justify-center gap-8 mt-6 text-[10px] text-gray-500 font-medium">
          <button class="hover:text-indigo-600" @click="go('/talks')">社区公约</button>
          <button class="hover:text-indigo-600" @click="go('/points')">积分说明</button>
          <button class="hover:text-indigo-600" @click="go('/insights')">商务合作</button>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.topic-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05);
}
.tab-active {
  color: #4f46e5;
  border-bottom: 2px solid #4f46e5;
}
.search-focus:focus-within {
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.1);
  border-color: #4f46e5;
}
</style>


