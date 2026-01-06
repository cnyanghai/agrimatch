<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { X, Truck, Package, CreditCard, ClipboardCheck, Settings, Plus } from 'lucide-vue-next'
import { createMilestone, type MilestoneCreateRequest } from '../../api/contract'

const props = defineProps<{
  modelValue: boolean
  contractId: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'created'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)

// 表单数据
const form = ref<MilestoneCreateRequest>({
  milestoneType: 'CUSTOM',
  milestoneName: '',
  description: '',
  expectedDate: '',
  sortOrder: 0
})

// 节点类型选项
const typeOptions = [
  { value: 'SHIP', label: '发货', icon: Truck, desc: '卖方发货' },
  { value: 'RECEIVE', label: '收货', icon: Package, desc: '买方确认收货' },
  { value: 'PAY', label: '付款', icon: CreditCard, desc: '买方付款' },
  { value: 'INSPECT', label: '质检', icon: ClipboardCheck, desc: '质量检验' },
  { value: 'CUSTOM', label: '自定义', icon: Settings, desc: '自定义节点' }
]

// 选择类型时自动填充名称
function selectType(type: string) {
  form.value.milestoneType = type
  const option = typeOptions.find(t => t.value === type)
  if (option && type !== 'CUSTOM') {
    form.value.milestoneName = option.label
  }
}

// 重置表单
function resetForm() {
  form.value = {
    milestoneType: 'CUSTOM',
    milestoneName: '',
    description: '',
    expectedDate: '',
    sortOrder: 0
  }
}

// 关闭弹窗
function close() {
  visible.value = false
  resetForm()
}

// 提交
async function handleSubmit() {
  if (!form.value.milestoneName?.trim()) {
    ElMessage.warning('请输入节点名称')
    return
  }
  
  loading.value = true
  try {
    const res = await createMilestone(props.contractId, {
      milestoneType: form.value.milestoneType,
      milestoneName: form.value.milestoneName.trim(),
      description: form.value.description?.trim() || undefined,
      expectedDate: form.value.expectedDate || undefined,
      sortOrder: form.value.sortOrder || 0
    })
    
    if (res.code === 0) {
      ElMessage.success('节点添加成功')
      emit('created')
      close()
    } else {
      ElMessage.error(res.message || '添加失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '添加失败')
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
            <div class="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center">
              <Plus class="w-5 h-5 text-emerald-600" />
            </div>
            <h2 class="text-lg font-bold text-gray-900">添加履约节点</h2>
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
          <!-- 节点类型 -->
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
              节点类型
            </label>
            <div class="grid grid-cols-5 gap-2">
              <button
                v-for="option in typeOptions"
                :key="option.value"
                :class="[
                  'flex flex-col items-center gap-1 p-3 rounded-xl border-2 transition-all',
                  form.milestoneType === option.value 
                    ? 'border-emerald-500 bg-emerald-50' 
                    : 'border-gray-100 hover:border-gray-200 hover:bg-gray-50'
                ]"
                @click="selectType(option.value)"
              >
                <component 
                  :is="option.icon" 
                  :class="['w-5 h-5', form.milestoneType === option.value ? 'text-emerald-600' : 'text-gray-400']"
                />
                <span :class="['text-xs font-bold', form.milestoneType === option.value ? 'text-emerald-600' : 'text-gray-600']">
                  {{ option.label }}
                </span>
              </button>
            </div>
          </div>
          
          <!-- 节点名称 -->
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
              节点名称 <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.milestoneName"
              type="text"
              placeholder="例如：发货确认、质量检验"
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-sm"
            />
          </div>
          
          <!-- 描述 -->
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
              描述说明
            </label>
            <textarea
              v-model="form.description"
              rows="2"
              placeholder="可选，说明此节点的具体要求..."
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-sm resize-none"
            />
          </div>
          
          <!-- 预计完成日期 -->
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
              预计完成日期
            </label>
            <input
              v-model="form.expectedDate"
              type="date"
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-sm"
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
            :disabled="loading || !form.milestoneName?.trim()"
            @click="handleSubmit"
          >
            <Plus v-if="!loading" class="w-4 h-4" />
            {{ loading ? '添加中...' : '添加节点' }}
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

