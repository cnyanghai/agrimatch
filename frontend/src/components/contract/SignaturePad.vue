<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { RotateCcw } from 'lucide-vue-next'

const props = defineProps<{
  width?: number
  height?: number
  lineWidth?: number
  lineColor?: string
}>()

const emit = defineEmits<{
  (e: 'change', dataUrl: string | null): void
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)
const ctx = ref<CanvasRenderingContext2D | null>(null)
const isDrawing = ref(false)
const lastX = ref(0)
const lastY = ref(0)
const isEmpty = ref(true)

const canvasWidth = props.width || 400
const canvasHeight = props.height || 200
const strokeWidth = props.lineWidth || 2
const strokeColor = props.lineColor || '#1f2937'

onMounted(() => {
  if (canvasRef.value) {
    ctx.value = canvasRef.value.getContext('2d')
    if (ctx.value) {
      ctx.value.strokeStyle = strokeColor
      ctx.value.lineWidth = strokeWidth
      ctx.value.lineCap = 'round'
      ctx.value.lineJoin = 'round'
    }
  }
})

function getEventPos(e: MouseEvent | TouchEvent) {
  const canvas = canvasRef.value
  if (!canvas) return { x: 0, y: 0 }
  
  const rect = canvas.getBoundingClientRect()
  const scaleX = canvas.width / rect.width
  const scaleY = canvas.height / rect.height
  
  if ('touches' in e && e.touches.length > 0) {
    return {
      x: (e.touches[0]!.clientX - rect.left) * scaleX,
      y: (e.touches[0]!.clientY - rect.top) * scaleY
    }
  } else if ('clientX' in e) {
    return {
      x: (e.clientX - rect.left) * scaleX,
      y: (e.clientY - rect.top) * scaleY
    }
  }
}

function startDrawing(e: MouseEvent | TouchEvent) {
  e.preventDefault()
  isDrawing.value = true
  const pos = getEventPos(e)
  lastX.value = pos.x
  lastY.value = pos.y
}

function draw(e: MouseEvent | TouchEvent) {
  if (!isDrawing.value || !ctx.value) return
  e.preventDefault()
  
  const pos = getEventPos(e)
  
  ctx.value.beginPath()
  ctx.value.moveTo(lastX.value, lastY.value)
  ctx.value.lineTo(pos.x, pos.y)
  ctx.value.stroke()
  
  lastX.value = pos.x
  lastY.value = pos.y
  isEmpty.value = false
}

function stopDrawing() {
  if (isDrawing.value) {
    isDrawing.value = false
    emitChange()
  }
}

function clear() {
  if (!ctx.value || !canvasRef.value) return
  ctx.value.clearRect(0, 0, canvasRef.value.width, canvasRef.value.height)
  isEmpty.value = true
  emit('change', null)
}

function emitChange() {
  if (isEmpty.value || !canvasRef.value) {
    emit('change', null)
    return
  }
  const dataUrl = canvasRef.value.toDataURL('image/png')
  emit('change', dataUrl)
}

function getDataUrl(): string | null {
  if (isEmpty.value || !canvasRef.value) return null
  return canvasRef.value.toDataURL('image/png')
}

// 暴露方法给父组件
defineExpose({
  clear,
  getDataUrl,
  isEmpty: () => isEmpty.value
})
</script>

<template>
  <div class="signature-pad-wrapper">
    <div class="relative rounded-xl border-2 border-dashed border-gray-200 bg-white overflow-hidden">
      <canvas
        ref="canvasRef"
        :width="canvasWidth"
        :height="canvasHeight"
        class="w-full cursor-crosshair touch-none"
        :style="{ height: `${canvasHeight}px` }"
        @mousedown="startDrawing"
        @mousemove="draw"
        @mouseup="stopDrawing"
        @mouseleave="stopDrawing"
        @touchstart="startDrawing"
        @touchmove="draw"
        @touchend="stopDrawing"
        @touchcancel="stopDrawing"
      ></canvas>
      
      <!-- 提示文字 -->
      <div 
        v-if="isEmpty" 
        class="absolute inset-0 flex items-center justify-center pointer-events-none"
      >
        <span class="text-gray-300 text-lg font-medium">请在此处签名</span>
      </div>
    </div>
    
    <!-- 工具栏 -->
    <div class="mt-2 flex items-center justify-end gap-2">
      <button 
        class="flex items-center gap-1.5 px-3 py-1.5 text-xs font-bold text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-all"
        @click="clear"
      >
        <RotateCcw class="w-3.5 h-3.5" />
        清除
      </button>
    </div>
  </div>
</template>

<style scoped>
.signature-pad-wrapper canvas {
  display: block;
}
</style>

