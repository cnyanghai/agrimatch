<script setup lang="ts">
import { computed, watch, onUnmounted } from 'vue'
import { X } from 'lucide-vue-next'

const props = withDefaults(defineProps<{
  modelValue: boolean
  title?: string
  direction?: 'left' | 'right'
  size?: string
  closeOnClickMask?: boolean
  noHeader?: boolean
}>(), {
  direction: 'right',
  size: '380px',
  closeOnClickMask: true,
  noHeader: false,
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

function close() {
  emit('update:modelValue', false)
}

function onMaskClick() {
  if (props.closeOnClickMask) close()
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && props.modelValue) close()
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      document.addEventListener('keydown', onKeydown)
      document.body.style.overflow = 'hidden'
    } else {
      document.removeEventListener('keydown', onKeydown)
      document.body.style.overflow = ''
    }
  }
)

onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
})

// Panel style – respects size prop on desktop, full width on mobile
const panelStyle = computed(() => ({
  width: props.size,
  maxWidth: '100vw',
}))

// Transition names differ by direction
const transitionName = computed(() =>
  props.direction === 'right' ? 'drawer-right' : 'drawer-left'
)

const panelClasses = computed(() => [
  'fixed top-0 bottom-0 z-[9997] bg-white shadow-2xl flex flex-col overflow-hidden',
  props.direction === 'right' ? 'right-0' : 'left-0',
  // On mobile, full width override is done via max-w-full in style
])
</script>

<template>
  <Teleport to="body">
    <!-- Overlay -->
    <Transition name="drawer-overlay">
      <div
        v-if="modelValue"
        class="fixed inset-0 z-[9996] bg-black/50"
        @click="onMaskClick"
      />
    </Transition>

    <!-- Panel -->
    <Transition :name="transitionName">
      <div
        v-if="modelValue"
        :class="panelClasses"
        :style="panelStyle"
      >
        <!-- Header -->
        <div v-if="!noHeader" class="flex items-center justify-between px-5 py-4 border-b border-neutral-200 shrink-0">
          <h3 class="text-lg font-bold text-neutral-900">{{ title }}</h3>
          <button
            class="w-9 h-9 rounded-xl bg-neutral-100 hover:bg-neutral-200 flex items-center justify-center transition-colors"
            @click="close"
          >
            <X class="w-5 h-5 text-neutral-500" />
          </button>
        </div>

        <!-- Content -->
        <div class="flex-1 overflow-y-auto">
          <slot />
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* Overlay fade */
.drawer-overlay-enter-active,
.drawer-overlay-leave-active {
  transition: opacity 0.25s ease;
}
.drawer-overlay-enter-from,
.drawer-overlay-leave-to {
  opacity: 0;
}

/* Right drawer */
.drawer-right-enter-active,
.drawer-right-leave-active {
  transition: transform 0.3s ease;
}
.drawer-right-enter-from,
.drawer-right-leave-to {
  transform: translateX(100%);
}

/* Left drawer */
.drawer-left-enter-active,
.drawer-left-leave-active {
  transition: transform 0.3s ease;
}
.drawer-left-enter-from,
.drawer-left-leave-to {
  transform: translateX(-100%);
}

/* Mobile: full width */
@media (max-width: 640px) {
  div[class*="fixed top-0 bottom-0"] {
    width: 100% !important;
  }
}
</style>
