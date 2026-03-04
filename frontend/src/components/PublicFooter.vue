<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { Mail, Clock, MapPin, Download } from 'lucide-vue-next'
import logoWhite from '../assets/logo-white.svg'
import { ref, onMounted } from 'vue'
import QRCode from 'qrcode'
import IosInstallGuide from './IosInstallGuide.vue'

const qrDataUrl = ref('')
const showIosGuide = ref(false)

onMounted(async () => {
  try {
    qrDataUrl.value = await QRCode.toDataURL('https://www.wogucloud.com', {
      width: 120,
      margin: 1,
      color: {
        dark: '#0f172a',
        light: '#ffffff',
      },
    })
  } catch {
    // 生成失败时静默忽略
  }
})
</script>

<template>
  <footer class="bg-slate-950 text-neutral-500 w-full">
    <!-- 主体区域 -->
    <div class="max-w-7xl mx-auto px-4 py-16">
      <div class="grid grid-cols-2 md:grid-cols-5 gap-10 md:gap-8">
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
            <Mail class="w-3.5 h-3.5 text-neutral-600" />
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
              <Clock class="w-3.5 h-3.5 text-neutral-600 mt-0.5 shrink-0" />
              <span class="text-neutral-400 leading-relaxed">周一至周五<br />9:00 - 18:00</span>
            </li>
            <li class="flex items-start gap-2">
              <MapPin class="w-3.5 h-3.5 text-neutral-600 mt-0.5 shrink-0" />
              <span class="text-neutral-400">天津市东丽区</span>
            </li>
          </ul>
        </div>

        <!-- 下载App -->
        <div>
          <h4 class="text-white font-bold mb-5 text-xs uppercase tracking-widest">下载App</h4>
          <ul class="space-y-3 text-xs">
            <li>
              <a
                href="/download/wogu.apk"
                download
                class="flex items-center gap-1.5 hover:text-brand-400 transition-colors"
              >
                <Download class="w-3.5 h-3.5 text-neutral-600 shrink-0" />
                Android 下载
              </a>
            </li>
            <li>
              <button
                class="flex items-center gap-1.5 hover:text-brand-400 transition-colors text-left"
                @click="showIosGuide = true"
              >
                <svg class="w-3.5 h-3.5 text-neutral-600 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 17h.01" /><path d="M12 3a9 9 0 1 0 0 18A9 9 0 0 0 12 3z" />
                </svg>
                iPhone 安装指南
              </button>
            </li>
            <li class="!mt-4 pt-4 border-t border-white/5">
              <p class="text-neutral-600 mb-2">扫码访问网站</p>
              <img
                v-if="qrDataUrl"
                :src="qrDataUrl"
                alt="沃谷二维码"
                class="w-[120px] h-[120px] rounded-lg bg-white p-1"
              />
            </li>
          </ul>
        </div>
      </div>

      <!-- iOS安装引导浮层 -->
      <IosInstallGuide v-model="showIosGuide" />
    </div>

    <!-- 底部版权 -->
    <div class="border-t border-white/5">
      <div class="max-w-7xl mx-auto px-4 py-5 flex flex-col md:flex-row justify-between items-center gap-3 text-[11px] text-neutral-600">
        <p>© 2026 天津市东丽区农汇通网络科技信息咨询厅 · <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener noreferrer" class="hover:text-neutral-400 transition-colors">津ICP备2026001703号</a></p>
        <div class="flex items-center gap-1 text-neutral-700">
          <RouterLink to="/legal/terms" class="hover:text-neutral-400 transition-colors">用户协议</RouterLink>
          <span class="text-neutral-800">·</span>
          <RouterLink to="/legal/privacy" class="hover:text-neutral-400 transition-colors">隐私政策</RouterLink>
          <span class="text-neutral-800">·</span>
          <RouterLink to="/legal/disclaimer" class="hover:text-neutral-400 transition-colors">免责声明</RouterLink>
        </div>
      </div>
    </div>
  </footer>
</template>
