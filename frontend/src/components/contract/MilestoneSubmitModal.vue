<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { X, Upload, Image, FileText, Trash2, Check } from 'lucide-vue-next'
import { submitMilestone, type MilestoneResponse, type MilestoneSubmitRequest } from '../../api/contract'
import { uploadImage } from '../../api/file'

const props = defineProps<{
  modelValue: boolean
  contractId: number
  milestone: MilestoneResponse | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'submitted'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const uploading = ref(false)

// 表单数据
const form = ref<{
  actualDate: string
  evidenceUrls: string[]
  remark: string
}>({
  actualDate: new Date().toISOString().split('T')[0],
  evidenceUrls: [],
  remark: ''
})

// 文件上传
const fileInputRef = ref<HTMLInputElement | null>(null)

function triggerFileInput() {
  fileInputRef.value?.click()
}

async function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (!files || files.length === 0) return
  
  uploading.value = true
  try {
    for (const file of Array.from(files)) {
      const res = await uploadImage(file, (progress) => {
        console.log('Upload progress:', progress)
      })
      if (res.code === 0 && res.data?.url) {
        form.value.evidenceUrls.push(res.data.url)
      }
    }
    ElMessage.success('上传成功')
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  } finally {
    uploading.value = false
    // 重置 input
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  }
}

// 删除图片
function removeEvidence(index: number) {
  form.value.evidenceUrls.splice(index, 1)
}

// 重置表单
function resetForm() {
  form.value = {
    actualDate: new Date().toISOString().split('T')[0],
    evidenceUrls: [],
    remark: ''
  }
}

// 关闭弹窗
function close() {
  visible.value = false
  resetForm()
}

// 提交
async function handleSubmit() {
  if (!props.milestone) return
  
  if (form.value.evidenceUrls.length === 0) {
    ElMessage.warning('请上传至少一张凭证图片')
    return
  }
  
  loading.value = true
  try {
    const req: MilestoneSubmitRequest = {
      actualDate: form.value.actualDate || undefined,
      evidenceUrl: form.value.evidenceUrls[0], // 主凭证
      evidenceUrls: form.value.evidenceUrls,
      remark: form.value.remark?.trim() || undefined
    }
    
    const res = await submitMilestone(props.contractId, props.milestone.id, req)
    
    if (res.code === 0) {
      ElMessage.success('凭证提交成功，等待对方确认')
      emit('submitted')
      close()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    loading.value = false
  }
}

// 监听打开时重置表单
watch(visible, (val) => {
  if (val) {
    resetForm()
  }
})
</script>

<template>
  <Teleport to="body">
    <div 
      v-if="visible"
      class="fixed inset-0 z-50 flex items-center justify-center p-4"
    >
      <!-- 遮罩 -->
      <div 
        class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm"
        @click="close"
      />
      
      <!-- 弹窗 -->
      <div class="relative bg-white rounded-[32px] shadow-2xl w-full max-w-md overflow-hidden animate-zoom-in">
        <!-- 头部 -->
        <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center">
              <Upload class="w-5 h-5 text-blue-600" />
            </div>
            <div>
              <h2 class="text-lg font-bold text-gray-900">提交履约凭证</h2>
              <p class="text-xs text-gray-500">{{ milestone?.milestoneName }}</p>
            </div>
          </div>
          <button 
            class="w-8 h-8 rounded-lg hover:bg-gray-100 flex items-center justify-center transition-colors"
            @click="close"
          >
            <X class="w-5 h-5 text-gray-400" />
          </button>
        </div>
        
        <!-- 内容 -->
        <div class="p-6 space-y-5">
          <!-- 完成日期 -->
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
              实际完成日期
            </label>
            <input
              v-model="form.actualDate"
              type="date"
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-sm"
            />
          </div>
          
          <!-- 凭证上传 -->
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
              凭证图片 <span class="text-red-500">*</span>
            </label>
            
            <input
              ref="fileInputRef"
              type="file"
              accept="image/*"
              multiple
              class="hidden"
              @change="handleFileChange"
            />
            
            <div class="grid grid-cols-3 gap-3">
              <!-- 已上传的图片 -->
              <div 
                v-for="(url, index) in form.evidenceUrls"
                :key="index"
                class="relative aspect-square rounded-xl overflow-hidden group"
              >
                <img :src="url" alt="凭证" class="w-full h-full object-cover" />
                <button
                  class="absolute top-1 right-1 w-6 h-6 bg-red-500 hover:bg-red-600 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                  @click="removeEvidence(index)"
                >
                  <Trash2 class="w-3.5 h-3.5 text-white" />
                </button>
              </div>
              
              <!-- 上传按钮 -->
              <button
                v-if="form.evidenceUrls.length < 9"
                class="aspect-square rounded-xl border-2 border-dashed border-gray-200 hover:border-emerald-500 hover:bg-emerald-50/50 flex flex-col items-center justify-center gap-1 transition-colors"
                :disabled="uploading"
                @click="triggerFileInput"
              >
                <Upload v-if="!uploading" class="w-6 h-6 text-gray-400" />
                <div v-else class="w-6 h-6 border-2 border-gray-300 border-t-emerald-500 rounded-full animate-spin" />
                <span class="text-xs text-gray-400">{{ uploading ? '上传中' : '上传' }}</span>
              </button>
            </div>
            
            <p class="text-xs text-gray-400 mt-2">
              支持 JPG、PNG 格式，最多 9 张
            </p>
          </div>
          
          <!-- 备注 -->
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
              备注说明
            </label>
            <textarea
              v-model="form.remark"
              rows="2"
              placeholder="可选，补充说明..."
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-sm resize-none"
            />
          </div>
        </div>
        
        <!-- 底部 -->
        <div class="px-6 py-4 bg-gray-50 border-t border-gray-100 flex gap-3">
          <button
            class="flex-1 px-4 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-bold rounded-xl transition-all active:scale-95"
            @click="close"
          >
            取消
          </button>
          <button
            class="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 bg-emerald-600 hover:bg-emerald-700 disabled:bg-gray-300 text-white text-sm font-bold rounded-xl transition-all active:scale-95"
            :disabled="loading || form.evidenceUrls.length === 0"
            @click="handleSubmit"
          >
            <Check v-if="!loading" class="w-4 h-4" />
            {{ loading ? '提交中...' : '提交凭证' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.animate-zoom-in {
  animation: zoomIn 0.2s ease-out;
}

@keyframes zoomIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>

