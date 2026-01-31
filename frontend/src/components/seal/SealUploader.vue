<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, RotateCcw, ImagePlus } from 'lucide-vue-next'
import { useSealExtractor } from '../../composables/useSealExtractor'
import { uploadImage } from '../../api/file'

const emit = defineEmits<{
  (e: 'extracted', sealUrl: string): void
}>()

const { extractSeal, dataUrlToFile } = useSealExtractor()

const originalPreview = ref('')
const extractedPreview = ref('')
const processing = ref(false)
const uploading = ref(false)
const dragOver = ref(false)
const fileInput = ref<HTMLInputElement>()

function handleDragOver(e: DragEvent) {
  e.preventDefault()
  dragOver.value = true
}

function handleDragLeave() {
  dragOver.value = false
}

function handleDrop(e: DragEvent) {
  e.preventDefault()
  dragOver.value = false
  const files = e.dataTransfer?.files
  if (files && files.length > 0) processFile(files[0]!)
}

function handleFileSelect(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    processFile(input.files[0]!)
    input.value = ''
  }
}

async function processFile(file: File) {
  // 验证文件类型和大小
  if (!['image/jpeg', 'image/png', 'image/jpg'].includes(file.type)) {
    ElMessage.warning('仅支持 JPG/PNG 格式')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不超过 5MB')
    return
  }

  // 显示原图预览
  originalPreview.value = URL.createObjectURL(file)
  extractedPreview.value = ''
  processing.value = true

  try {
    const result = await extractSeal(file)
    extractedPreview.value = result
  } catch (err: any) {
    ElMessage.error(err.message || '印章提取失败')
    originalPreview.value = ''
  } finally {
    processing.value = false
  }
}

function handleReset() {
  originalPreview.value = ''
  extractedPreview.value = ''
}

async function handleConfirm() {
  if (!extractedPreview.value) return

  uploading.value = true
  try {
    const file = dataUrlToFile(extractedPreview.value, 'seal-extracted.png')
    const res = await uploadImage(file)
    if (res.code === 0 && res.data) {
      ElMessage.success('印章上传成功')
      emit('extracted', res.data.fileUrl)
      handleReset()
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (err: any) {
    ElMessage.error(err.message || '上传失败')
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <div class="seal-uploader space-y-4">
    <!-- 上传区 -->
    <div v-if="!originalPreview">
      <div
        class="border-2 border-dashed rounded-2xl p-8 text-center cursor-pointer transition-all"
        :class="dragOver ? 'border-brand-500 bg-brand-50' : 'border-gray-200 hover:border-brand-300 hover:bg-brand-50/30'"
        @dragover="handleDragOver"
        @dragleave="handleDragLeave"
        @drop="handleDrop"
        @click="fileInput?.click()"
      >
        <div class="w-14 h-14 mx-auto mb-3 rounded-2xl bg-gray-100 flex items-center justify-center">
          <ImagePlus class="w-7 h-7 text-gray-400" />
        </div>
        <div class="text-sm font-medium text-gray-600 mb-1">点击或拖拽上传印章照片</div>
        <div class="text-xs text-gray-400">支持 JPG/PNG，不超过 5MB</div>
      </div>
      <input
        ref="fileInput"
        type="file"
        accept="image/jpeg,image/png"
        class="hidden"
        @change="handleFileSelect"
      />

      <!-- 提示 -->
      <div class="bg-amber-50 border border-amber-200 rounded-xl p-3 mt-3">
        <div class="text-xs text-amber-700 leading-relaxed">
          <span class="font-bold">使用说明：</span>请在白纸上清晰地盖上公章/合同章，然后拍照上传。系统将自动提取红色印章部分。
        </div>
      </div>
    </div>

    <!-- 预览对比 -->
    <div v-else class="space-y-4">
      <div class="grid grid-cols-2 gap-4">
        <!-- 原图 -->
        <div>
          <div class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">原图</div>
          <div class="aspect-square rounded-xl border border-gray-200 bg-gray-50 flex items-center justify-center overflow-hidden">
            <img :src="originalPreview" class="max-w-full max-h-full object-contain" alt="原图" />
          </div>
        </div>

        <!-- 提取效果 -->
        <div>
          <div class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">提取效果</div>
          <div
            class="aspect-square rounded-xl border border-gray-200 flex items-center justify-center overflow-hidden"
            style="background-image: url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAiIGhlaWdodD0iMjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjEwIiBoZWlnaHQ9IjEwIiBmaWxsPSIjZjBmMGYwIi8+PHJlY3QgeD0iMTAiIHk9IjEwIiB3aWR0aD0iMTAiIGhlaWdodD0iMTAiIGZpbGw9IiNmMGYwZjAiLz48L3N2Zz4='); background-size: 20px 20px;"
          >
            <div v-if="processing" class="text-sm text-gray-400">提取中...</div>
            <img v-else-if="extractedPreview" :src="extractedPreview" class="max-w-full max-h-full object-contain" alt="提取效果" />
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div v-if="extractedPreview" class="flex justify-end gap-3">
        <button
          class="flex items-center gap-2 px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold rounded-xl transition-all active:scale-95"
          @click="handleReset"
        >
          <RotateCcw class="w-4 h-4" />
          重新上传
        </button>
        <button
          class="flex items-center gap-2 px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-xl transition-all active:scale-95 disabled:opacity-50"
          :disabled="uploading"
          @click="handleConfirm"
        >
          <Upload v-if="!uploading" class="w-4 h-4" />
          {{ uploading ? '上传中...' : '确认使用' }}
        </button>
      </div>
    </div>
  </div>
</template>
