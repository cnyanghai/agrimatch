<script setup lang="ts">
import { CheckCircle, XCircle, AlertTriangle, Info, X } from 'lucide-vue-next'
import type { ToastItem } from '../../composables/useToast'

defineProps<{
  toasts: ToastItem[]
}>()

const emit = defineEmits<{
  (e: 'remove', id: number): void
}>()

const iconMap = {
  success: CheckCircle,
  error: XCircle,
  warning: AlertTriangle,
  info: Info,
}

const colorMap = {
  success: {
    bg: 'bg-green-50 border-green-200',
    icon: 'text-green-500',
    text: 'text-green-900',
  },
  error: {
    bg: 'bg-red-50 border-red-200',
    icon: 'text-red-500',
    text: 'text-red-900',
  },
  warning: {
    bg: 'bg-orange-50 border-orange-200',
    icon: 'text-orange-500',
    text: 'text-orange-900',
  },
  info: {
    bg: 'bg-blue-50 border-blue-200',
    icon: 'text-blue-500',
    text: 'text-blue-900',
  },
}
</script>

<template>
  <Teleport to="body">
    <div
      class="fixed top-0 left-1/2 -translate-x-1/2 z-[9999] flex flex-col items-center gap-2 pt-safe"
      style="padding-top: max(16px, env(safe-area-inset-top)); pointer-events: none;"
    >
      <TransitionGroup
        name="toast"
        tag="div"
        class="flex flex-col items-center gap-2"
      >
        <div
          v-for="toast in toasts"
          :key="toast.id"
          :class="[
            'flex items-start gap-3 px-4 py-3 rounded-xl border shadow-lg',
            'w-[calc(100vw-32px)] max-w-sm',
            'pointer-events-auto',
            colorMap[toast.type].bg,
          ]"
        >
          <!-- Icon -->
          <component
            :is="iconMap[toast.type]"
            class="w-5 h-5 shrink-0 mt-0.5"
            :class="colorMap[toast.type].icon"
          />

          <!-- Message -->
          <span
            class="flex-1 text-sm font-medium leading-snug break-words"
            :class="colorMap[toast.type].text"
          >
            {{ toast.message }}
          </span>

          <!-- Close button (persistent mode) -->
          <button
            v-if="toast.persistent"
            class="shrink-0 w-5 h-5 flex items-center justify-center rounded opacity-60 hover:opacity-100 transition-opacity"
            :class="colorMap[toast.type].text"
            @click="emit('remove', toast.id)"
          >
            <X class="w-4 h-4" />
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-enter-active {
  transition: all 0.3s ease-out;
}

.toast-leave-active {
  transition: all 0.25s ease-in;
}

.toast-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.toast-move {
  transition: transform 0.25s ease;
}
</style>
