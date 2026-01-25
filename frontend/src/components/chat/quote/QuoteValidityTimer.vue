<script setup lang="ts">
/**
 * Quote Validity Timer Component
 * Displays countdown for quote expiration
 */
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { Clock, AlertTriangle } from 'lucide-vue-next'

const props = defineProps<{
  remainingMs: number
  warningThresholdMs?: number
  criticalThresholdMs?: number
}>()

const emit = defineEmits<{
  (e: 'expired'): void
  (e: 'warning'): void
}>()

const remaining = ref(props.remainingMs)
const warningThreshold = computed(() => props.warningThresholdMs || 5 * 60 * 1000) // 5分钟
const criticalThreshold = computed(() => props.criticalThresholdMs || 1 * 60 * 1000) // 1分钟

let intervalId: number | null = null

// 格式化时间
const formattedTime = computed(() => {
  if (remaining.value <= 0) return '已过期'

  const totalSeconds = Math.floor(remaining.value / 1000)
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60

  if (hours > 0) {
    return `${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
  }
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})

const isExpired = computed(() => remaining.value <= 0)
const isWarning = computed(() => remaining.value > 0 && remaining.value <= warningThreshold.value)
const isCritical = computed(() => remaining.value > 0 && remaining.value <= criticalThreshold.value)

const statusColor = computed(() => {
  if (isExpired.value) return 'text-gray-400'
  if (isCritical.value) return 'text-red-500'
  if (isWarning.value) return 'text-amber-500'
  return 'text-gray-500'
})

function startTimer() {
  if (intervalId) return

  intervalId = window.setInterval(() => {
    remaining.value -= 1000

    if (remaining.value === warningThreshold.value) {
      emit('warning')
    }

    if (remaining.value <= 0) {
      remaining.value = 0
      emit('expired')
      stopTimer()
    }
  }, 1000)
}

function stopTimer() {
  if (intervalId) {
    window.clearInterval(intervalId)
    intervalId = null
  }
}

watch(() => props.remainingMs, (newVal) => {
  remaining.value = newVal
  if (newVal > 0) {
    stopTimer()
    startTimer()
  }
})

onMounted(() => {
  if (remaining.value > 0) {
    startTimer()
  }
})

onBeforeUnmount(() => {
  stopTimer()
})
</script>

<template>
  <div :class="['flex items-center gap-1 text-xs font-medium', statusColor]">
    <AlertTriangle v-if="isCritical" class="w-3.5 h-3.5 animate-pulse" />
    <Clock v-else class="w-3.5 h-3.5" />
    <span :class="{ 'animate-pulse': isCritical }">
      {{ formattedTime }}
    </span>
  </div>
</template>
