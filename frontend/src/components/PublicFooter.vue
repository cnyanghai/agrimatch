<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { Mail, Clock, MapPin } from 'lucide-vue-next'
import logoWhite from '../assets/logo-white.svg'
</script>

<template>
  <footer class="bg-slate-950 text-gray-500 w-full">
    <!-- 主体区域 -->
    <div class="max-w-7xl mx-auto px-4 py-16">
      <div class="grid grid-cols-2 md:grid-cols-4 gap-10 md:gap-8">
        <!-- 品牌 -->
        <div class="col-span-2 md:col-span-1">
          <div class="flex items-center gap-2 mb-4">
            <img :src="logoWhite" alt="沃谷" class="h-8 w-auto opacity-90" />
            <span class="text-xl font-bold text-white tracking-wide">沃谷</span>
          </div>
          <p class="text-xs leading-relaxed mb-5 max-w-[220px]">
            农牧供需智能匹配平台<br />让每一笔交易更安全、更透明
          </p>
          <div class="flex items-center gap-2 text-xs">
            <Mail class="w-3.5 h-3.5 text-gray-600" />
            <a href="mailto:cnyanghai@icloud.com" class="hover:text-brand-400 transition-colors">cnyanghai@icloud.com</a>
          </div>
        </div>

        <!-- 平台导航 -->
        <div>
          <h4 class="text-white font-bold mb-5 text-xs uppercase tracking-widest">平台</h4>
          <ul class="space-y-3 text-xs">
            <li><RouterLink to="/hall/supply" class="hover:text-brand-400 transition-colors">供应大厅</RouterLink></li>
            <li><RouterLink to="/hall/need" class="hover:text-brand-400 transition-colors">采购大厅</RouterLink></li>
            <li><RouterLink to="/talks" class="hover:text-brand-400 transition-colors">话题广场</RouterLink></li>
            <li><RouterLink to="/companies/directory" class="hover:text-brand-400 transition-colors">企业名录</RouterLink></li>
            <li><RouterLink to="/categories" class="hover:text-brand-400 transition-colors">品类目录</RouterLink></li>
          </ul>
        </div>

        <!-- 法律合规 -->
        <div>
          <h4 class="text-white font-bold mb-5 text-xs uppercase tracking-widest">法律合规</h4>
          <ul class="space-y-3 text-xs">
            <li><RouterLink to="/legal/terms" class="hover:text-brand-400 transition-colors">用户协议</RouterLink></li>
            <li><RouterLink to="/legal/privacy" class="hover:text-brand-400 transition-colors">隐私政策</RouterLink></li>
            <li><RouterLink to="/legal/e-signature" class="hover:text-brand-400 transition-colors">电子签约效力</RouterLink></li>
            <li><RouterLink to="/legal/disclaimer" class="hover:text-brand-400 transition-colors">免责声明</RouterLink></li>
          </ul>
        </div>

        <!-- 联系我们 -->
        <div>
          <h4 class="text-white font-bold mb-5 text-xs uppercase tracking-widest">联系我们</h4>
          <ul class="space-y-3 text-xs">
            <li><RouterLink to="/legal/cooperation" class="hover:text-brand-400 transition-colors">商务合作</RouterLink></li>
            <li><RouterLink to="/legal/feedback" class="hover:text-brand-400 transition-colors">意见反馈</RouterLink></li>
            <li class="flex items-start gap-2 !mt-5 pt-4 border-t border-white/5">
              <Clock class="w-3.5 h-3.5 text-gray-600 mt-0.5 shrink-0" />
              <span class="text-gray-400 leading-relaxed">周一至周五<br />9:00 - 18:00</span>
            </li>
            <li class="flex items-start gap-2">
              <MapPin class="w-3.5 h-3.5 text-gray-600 mt-0.5 shrink-0" />
              <span class="text-gray-400">天津市东丽区</span>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 底部版权 -->
    <div class="border-t border-white/5">
      <div class="max-w-7xl mx-auto px-4 py-5 flex flex-col md:flex-row justify-between items-center gap-3 text-[11px] text-gray-600">
        <p>© 2026 天津市东丽区农汇通网络科技信息咨询厅</p>
        <div class="flex items-center gap-1 text-gray-700">
          <RouterLink to="/legal/terms" class="hover:text-gray-400 transition-colors">用户协议</RouterLink>
          <span class="text-gray-800">·</span>
          <RouterLink to="/legal/privacy" class="hover:text-gray-400 transition-colors">隐私政策</RouterLink>
          <span class="text-gray-800">·</span>
          <RouterLink to="/legal/disclaimer" class="hover:text-gray-400 transition-colors">免责声明</RouterLink>
        </div>
      </div>
    </div>
  </footer>
</template>
