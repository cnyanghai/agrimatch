<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '../../stores/notification'
import { MessageCircle, X, FileText, Bell } from 'lucide-vue-next'

const router = useRouter()
const notificationStore = useNotificationStore()

const toast = computed(() => notificationStore.currentToast)
const show = computed(() => notificationStore.showToast)

// 调试：监听 show 变化
watch(show, (val) => {
  console.log('[NotificationToast] show changed to:', val, 'toast:', toast.value)
}, { immediate: true })

function handleClick() {
  if (toast.value?.conversationId) {
    router.push(`/chat?conversationId=${toast.value.conversationId}`)
  }
  notificationStore.hideToast()
}

function handleClose(e: Event) {
  e.stopPropagation()
  notificationStore.hideToast()
}

const iconComponent = computed(() => {
  switch (toast.value?.type) {
    case 'chat':
      return MessageCircle
    case 'contract':
      return FileText
    default:
      return Bell
  }
})
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="translate-x-full opacity-0"
      enter-to-class="translate-x-0 opacity-100"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="translate-x-0 opacity-100"
      leave-to-class="translate-x-full opacity-0"
    >
      <div
        v-if="show && toast"
        class="fixed top-20 right-4 z-50 max-w-sm w-full bg-white rounded-xl shadow-2xl border border-gray-100 overflow-hidden cursor-pointer hover:shadow-xl transition-shadow"
        @click="handleClick"
      >
        <!-- 顶部颜色条 -->
        <div
          class="h-1"
          :class="{
            'bg-brand-500': toast.type === 'chat',
            'bg-blue-500': toast.type === 'contract',
            'bg-amber-500': toast.type === 'system'
          }"
        />

        <div class="p-4 flex items-start gap-3">
          <!-- 图标 -->
          <div
            class="w-10 h-10 rounded-full flex items-center justify-center shrink-0"
            :class="{
              'bg-brand-100 text-brand-600': toast.type === 'chat',
              'bg-blue-100 text-blue-600': toast.type === 'contract',
              'bg-amber-100 text-amber-600': toast.type === 'system'
            }"
          >
            <component :is="iconComponent" class="w-5 h-5" />
          </div>

          <!-- 内容 -->
          <div class="flex-1 min-w-0">
            <div class="flex items-center justify-between gap-2">
              <h4 class="font-bold text-gray-900 truncate">{{ toast.title }}</h4>
              <span class="text-xs text-gray-400 shrink-0">刚刚</span>
            </div>
            <p class="text-sm text-gray-600 mt-1 line-clamp-2">{{ toast.content }}</p>
          </div>

          <!-- 关闭按钮 -->
          <button
            class="shrink-0 w-6 h-6 rounded-full hover:bg-gray-100 flex items-center justify-center text-gray-400 hover:text-gray-600 transition-colors"
            @click="handleClose"
          >
            <X class="w-4 h-4" />
          </button>
        </div>

        <!-- 点击提示 -->
        <div class="px-4 pb-3 text-xs text-gray-400">
          点击查看详情
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
