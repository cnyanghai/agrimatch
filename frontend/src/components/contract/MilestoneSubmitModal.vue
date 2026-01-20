<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Trash2, Check } from 'lucide-vue-next'
import { submitMilestone, type MilestoneResponse, type MilestoneSubmitRequest } from '../../api/contract'
import { uploadImage } from '../../api/file'
import { BaseModal, BaseButton } from '../ui'

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
      const res = await uploadImage(file, () => {})
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
  <BaseModal
    v-model="visible"
    title="提交履约凭证"
    subtitle="履约管理"
    size="md"
    @close="resetForm"
  >
    <!-- 头部图标 -->
    <template #icon>
      <div class="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center">
        <Upload class="w-5 h-5 text-blue-600" />
      </div>
    </template>

    <!-- 自定义标题 -->
    <template #title>
      <div class="text-xl font-bold text-gray-900">提交履约凭证</div>
      <p class="text-xs text-gray-500 mt-0.5">{{ milestone?.milestoneName }}</p>
    </template>

    <div class="space-y-5">
      <!-- 完成日期 -->
      <div>
        <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
          实际完成日期
        </label>
        <input
          v-model="form.actualDate"
          type="date"
          class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:border-brand-500 outline-none transition-all text-sm"
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
            class="aspect-square rounded-xl border-2 border-dashed border-gray-200 hover:border-brand-500 hover:bg-brand-50/50 flex flex-col items-center justify-center gap-1 transition-colors"
            :disabled="uploading"
            @click="triggerFileInput"
          >
            <Upload v-if="!uploading" class="w-6 h-6 text-gray-400" />
            <div v-else class="w-6 h-6 border-2 border-gray-300 border-t-brand-500 rounded-full animate-spin" />
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
          class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:border-brand-500 outline-none transition-all text-sm resize-none"
        />
      </div>
    </div>
    
    <!-- 底部 -->
    <template #footer>
      <BaseButton type="secondary" block @click="close">
        取消
      </BaseButton>
      <BaseButton 
        type="primary" 
        block
        :loading="loading" 
        :disabled="form.evidenceUrls.length === 0"
        @click="handleSubmit"
      >
        <Check v-if="!loading" class="w-4 h-4" />
        提交凭证
      </BaseButton>
    </template>
  </BaseModal>
</template>
