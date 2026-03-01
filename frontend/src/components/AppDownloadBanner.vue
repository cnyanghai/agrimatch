<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { X } from 'lucide-vue-next'
import { Capacitor } from '@capacitor/core'
import { isMobileBrowser, isAndroid, isStandalone } from '../utils/device'
import IosInstallGuide from './IosInstallGuide.vue'

const SESSION_KEY = 'app_banner_dismissed'

const visible = ref(false)
const showIosGuide = ref(false)

onMounted(() => {
  // 不在原生App内、是移动端浏览器、未独立模式、当次会话未关闭
  if (
    !Capacitor.isNativePlatform() &&
    isMobileBrowser() &&
    !isStandalone() &&
    !sessionStorage.getItem(SESSION_KEY)
  ) {
    visible.value = true
  }
})

function dismiss() {
  sessionStorage.setItem(SESSION_KEY, '1')
  visible.value = false
}

function handleOpen() {
  if (isAndroid()) {
    // Android: 直接触发APK下载
    const a = document.createElement('a')
    a.href = '/download/wogu.apk'
    a.download = 'wogu.apk'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
  } else {
    // iPhone/iPad: 弹出安装引导
    showIosGuide.value = true
  }
}
</script>

<template>
  <!-- Smart Banner：仅移动端浏览器显示，md断点以上自动隐藏 -->
  <div
    v-if="visible"
    class="fixed top-0 left-0 right-0 z-[9999] flex md:hidden items-center gap-3 bg-white border-b border-neutral-200 shadow-md px-3"
    style="padding-top: max(8px, env(safe-area-inset-top)); padding-bottom: 8px; min-height: 56px;"
  >
    <!-- App图标 -->
    <img
      src="/icons/icon-192x192.png"
      alt="沃谷"
      class="w-8 h-8 rounded-lg shrink-0"
    />

    <!-- 文字区域 -->
    <div class="flex-1 min-w-0">
      <p class="text-sm font-semibold text-neutral-900 leading-tight">沃谷</p>
      <p class="text-xs text-neutral-500 leading-tight">体验更流畅</p>
    </div>

    <!-- 打开按钮 -->
    <button
      class="shrink-0 px-4 py-1.5 bg-brand-600 hover:bg-brand-700 text-white text-sm font-semibold rounded-lg transition-all duration-200"
      @click="handleOpen"
    >
      打开
    </button>

    <!-- 关闭按钮 -->
    <button
      class="shrink-0 w-7 h-7 flex items-center justify-center text-neutral-400 hover:text-neutral-600 transition-colors"
      @click="dismiss"
    >
      <X class="w-4 h-4" />
    </button>
  </div>

  <!-- iOS安装引导浮层 -->
  <IosInstallGuide v-model="showIosGuide" />
</template>
