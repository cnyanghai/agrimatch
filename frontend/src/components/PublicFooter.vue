<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

function go(path: string) {
  router.push(path)
}

function openAgreement(type: 'user' | 'privacy') {
  const title = type === 'user' ? '用户协议' : '隐私政策'
  ElMessage.info(`${title}详情页面正在准备中...`)
}
</script>

<template>
  <footer class="bg-slate-950 text-gray-500 py-20 border-t border-white/5 w-full">
    <div class="max-w-7xl mx-auto px-4">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-12 mb-16">
        <!-- 品牌 -->
        <div class="col-span-1 md:col-span-2">
          <div class="flex items-center gap-2 mb-6">
            <div class="w-8 h-8 bg-emerald-600 rounded-lg flex items-center justify-center text-white font-black italic">A</div>
            <span class="text-2xl font-bold text-white tracking-tighter italic">AgriMatch</span>
          </div>
          <p class="text-sm leading-relaxed max-w-sm mb-8">
            领先的饲料原料供需协作平台。通过数字化手段连接产业上下游，让饲料人的每一笔交易都更安全、更透明、更简单。
          </p>
          <div class="flex gap-4">
            <div class="w-10 h-10 rounded-full bg-white/5 border border-white/10 flex items-center justify-center hover:bg-emerald-600 hover:text-white transition-all cursor-pointer">
              <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2C6.477 2 2 6.477 2 12c0 4.418 2.865 8.166 6.839 9.489.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482C19.138 20.164 22 16.418 22 12c0-5.523-4.477-10-10-10z"/></svg>
            </div>
            <!-- 更多社交媒体图标 -->
          </div>
        </div>

        <!-- 法律合规 -->
        <div>
          <h4 class="text-white font-bold mb-8 text-sm uppercase tracking-widest">法律与合规</h4>
          <ul class="space-y-4 text-xs font-bold">
            <li><button class="hover:text-emerald-400 transition-colors" @click="openAgreement('user')">用户协议</button></li>
            <li><button class="hover:text-emerald-400 transition-colors" @click="openAgreement('privacy')">隐私政策</button></li>
            <li><button class="hover:text-emerald-400 transition-colors">电子签约法律效力</button></li>
            <li><button class="hover:text-emerald-400 transition-colors">免责声明</button></li>
          </ul>
        </div>

        <!-- 联系我们 -->
        <div>
          <h4 class="text-white font-bold mb-8 text-sm uppercase tracking-widest">联系我们</h4>
          <ul class="space-y-4 text-xs font-bold">
            <li><button class="hover:text-emerald-400 transition-colors">商务合作</button></li>
            <li><button class="hover:text-emerald-400 transition-colors">意见反馈</button></li>
            <li class="flex items-center gap-3">
              <span class="w-2 h-2 bg-emerald-500 rounded-full animate-pulse"></span>
              <span class="text-gray-300">客服在线 (9:00-18:00)</span>
            </li>
            <li class="text-[10px] text-gray-600 font-medium">辽宁省大连市瓦房店市工业区</li>
          </ul>
        </div>
      </div>
      
      <!-- 底部版权 -->
      <div class="pt-12 border-t border-white/5 flex flex-col md:flex-row justify-between items-center gap-6 text-[10px] uppercase tracking-[0.2em] font-black">
        <div class="flex items-center gap-4">
          <p>© 2026 AgriMatch Team.</p>
          <div class="hidden md:block w-1 h-1 bg-gray-800 rounded-full"></div>
          <p>Connecting Raw Materials</p>
        </div>
        <div class="flex items-center gap-8">
          <span class="text-gray-700">辽ICP备202401064258号</span>
          <div class="flex items-center gap-2 px-3 py-1 bg-white/5 rounded-lg border border-white/5 text-emerald-500/80">
            <span class="w-1 h-1 bg-emerald-500 rounded-full"></span>
            SYSTEM ONLINE
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

