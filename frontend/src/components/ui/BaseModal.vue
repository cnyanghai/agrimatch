<script setup lang="ts">
import { computed, watch, onUnmounted } from 'vue'
import { X } from 'lucide-vue-next'

const props = withDefaults(defineProps<{
  modelValue: boolean
  title?: string
  subtitle?: string
  size?: 'sm' | 'md' | 'lg' | 'xl' | 'full'
  closeOnOverlay?: boolean
  closeOnEsc?: boolean
  showClose?: boolean
  showFooter?: boolean
}>(), {
  size: 'md',
  closeOnOverlay: true,
  closeOnEsc: true,
  showClose: true,
  showFooter: true
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'close'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 尺寸映射
const sizeClass = computed(() => {
  const sizes: Record<string, string> = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg',
    xl: 'max-w-3xl',
    full: 'max-w-[90vw]'
  }
  return sizes[props.size] || sizes.md
})

// 关闭弹窗
function close() {
  visible.value = false
  emit('close')
}

// 点击遮罩关闭
function onOverlayClick() {
  if (props.closeOnOverlay) {
    close()
  }
}

// ESC键关闭
function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && props.closeOnEsc && visible.value) {
    close()
  }
}

// 监听键盘事件
watch(visible, (val) => {
  if (val) {
    document.addEventListener('keydown', onKeydown)
    // 防止背景滚动
    document.body.style.overflow = 'hidden'
  } else {
    document.removeEventListener('keydown', onKeydown)
    document.body.style.overflow = ''
  }
})

onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div 
        v-if="visible" 
        class="fixed inset-0 z-[2000] flex items-center justify-center p-4"
      >
        <!-- 遮罩 -->
        <div 
          class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm"
          @click="onOverlayClick"
        />
        
        <!-- 弹窗容器 -->
        <div 
          :class="[
            'relative bg-white rounded-[32px] shadow-2xl w-full overflow-hidden flex flex-col max-h-[90vh]',
            sizeClass
          ]"
          class="animate-zoom-in"
        >
          <!-- 头部 -->
          <div class="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between z-10">
            <div class="flex items-center gap-3">
              <!-- 图标插槽 -->
              <slot name="icon" />
              
              <div>
                <!-- 小标签 -->
                <div v-if="subtitle" class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-0.5">
                  {{ subtitle }}
                </div>
                <!-- 标题 -->
                <h2 v-if="title" class="text-2xl font-bold text-gray-900">{{ title }}</h2>
                <!-- 自定义标题插槽 -->
                <slot name="title" />
              </div>
            </div>
            
            <!-- 关闭按钮 -->
            <button 
              v-if="showClose"
              class="w-10 h-10 rounded-xl bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-all "
              @click="close"
            >
              <X class="w-5 h-5 text-gray-500" />
            </button>
          </div>

          <!-- 内容区域 -->
          <div class="flex-1 overflow-y-auto px-6 py-5">
            <slot />
          </div>

          <!-- 底部 -->
          <div 
            v-if="showFooter" 
            class="sticky bottom-0 px-6 py-4 bg-gray-50 border-t border-gray-200 flex justify-end gap-3"
          >
            <slot name="footer">
              <!-- 默认底部内容（可被覆盖） -->
            </slot>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* 进场动画 */
@keyframes zoom-in {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.animate-zoom-in {
  animation: zoom-in 0.25s ease-out;
}

/* 过渡动画 */
.modal-enter-active {
  transition: opacity 0.25s ease-out;
}

.modal-leave-active {
  transition: opacity 0.2s ease-in;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .animate-zoom-in,
.modal-leave-to .animate-zoom-in {
  transform: scale(0.95) translateY(10px);
}
</style>

