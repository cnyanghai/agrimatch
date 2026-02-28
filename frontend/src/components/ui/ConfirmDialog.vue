<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue'
import { AlertTriangle, Info, Trash2 } from 'lucide-vue-next'
import BaseButton from './BaseButton.vue'
import { dialogState } from '../../composables/useConfirm'
import type { ConfirmOptions, PromptOptions, ConfirmType } from '../../composables/useConfirm'

// Local input value for prompt mode
const inputValue = ref('')

const isConfirmMode = computed(() => dialogState.mode === 'confirm')
const confirmOpts = computed(() => dialogState.options as ConfirmOptions)
const promptOpts = computed(() => dialogState.options as PromptOptions)

const confirmType = computed<ConfirmType>(() =>
  isConfirmMode.value ? (confirmOpts.value.type ?? 'warning') : 'info'
)

const iconMap = {
  warning: AlertTriangle,
  danger: Trash2,
  info: Info,
}

const iconColorMap: Record<ConfirmType, string> = {
  warning: 'text-orange-500',
  danger: 'text-red-500',
  info: 'text-blue-500',
}

const confirmButtonType = computed(() =>
  confirmType.value === 'danger' ? 'danger' : 'primary'
)

function handleConfirm() {
  if (!dialogState.resolve) return
  if (isConfirmMode.value) {
    dialogState.resolve(true)
  } else {
    dialogState.resolve(inputValue.value)
  }
  close()
}

function handleCancel() {
  if (!dialogState.resolve) return
  dialogState.resolve(isConfirmMode.value ? false : null)
  close()
}

function close() {
  dialogState.visible = false
  dialogState.resolve = null
  inputValue.value = ''
}

// Sync prompt initial value when dialog opens
watch(
  () => dialogState.visible,
  (visible) => {
    if (visible) {
      if (!isConfirmMode.value) {
        inputValue.value = (promptOpts.value as PromptOptions).inputValue ?? ''
      }
      document.addEventListener('keydown', onKeydown)
      document.body.style.overflow = 'hidden'
    } else {
      document.removeEventListener('keydown', onKeydown)
      document.body.style.overflow = ''
    }
  }
)

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && dialogState.visible) {
    handleCancel()
  }
}

onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <Transition name="confirm-dialog">
      <div
        v-if="dialogState.visible"
        class="fixed inset-0 z-[9998] flex items-center justify-center p-4"
      >
        <!-- Overlay -->
        <div
          class="absolute inset-0 bg-slate-900/60"
          @click="handleCancel"
        />

        <!-- Dialog panel -->
        <div
          class="relative bg-white rounded-2xl shadow-2xl w-full max-w-sm mx-auto overflow-hidden"
        >
          <!-- Header with icon -->
          <div class="px-6 pt-6 pb-4 flex items-start gap-4">
            <div class="shrink-0 w-10 h-10 rounded-xl bg-neutral-100 flex items-center justify-center">
              <component
                :is="iconMap[confirmType]"
                class="w-5 h-5"
                :class="iconColorMap[confirmType]"
              />
            </div>
            <div class="flex-1 min-w-0">
              <h3 class="text-lg font-bold text-neutral-900">
                {{ isConfirmMode ? confirmOpts.title : promptOpts.title }}
              </h3>
              <p class="mt-1 text-sm text-neutral-600 leading-relaxed">
                {{ isConfirmMode ? confirmOpts.message : promptOpts.message }}
              </p>
            </div>
          </div>

          <!-- Prompt input -->
          <div v-if="!isConfirmMode" class="px-6 pb-2">
            <input
              v-model="inputValue"
              type="text"
              :placeholder="promptOpts.placeholder"
              class="w-full px-4 py-3 border border-neutral-200 rounded-xl outline-none text-sm
                     focus:border-brand-500 focus:ring-2 focus:ring-brand-500/20 transition-all"
              @keydown.enter="handleConfirm"
            />
          </div>

          <!-- Actions -->
          <div class="px-6 pb-6 pt-4 flex gap-3 justify-end">
            <BaseButton type="secondary" size="sm" @click="handleCancel">
              {{ isConfirmMode ? (confirmOpts.cancelText ?? '取消') : '取消' }}
            </BaseButton>
            <BaseButton :type="confirmButtonType" size="sm" @click="handleConfirm">
              {{ isConfirmMode ? (confirmOpts.confirmText ?? '确认') : '确认' }}
            </BaseButton>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.confirm-dialog-enter-active {
  transition: opacity 0.2s ease-out;
}
.confirm-dialog-leave-active {
  transition: opacity 0.15s ease-in;
}
.confirm-dialog-enter-from,
.confirm-dialog-leave-to {
  opacity: 0;
}
</style>
