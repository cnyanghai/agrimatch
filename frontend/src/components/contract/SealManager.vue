<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Check, Trash2, Sparkles } from 'lucide-vue-next'
import { listSeals, createSeal, setDefaultSeal, deleteSeal, type SealResponse } from '../../api/contract'
import { vLazy } from '../../directives/lazyLoad'

const props = defineProps<{
  selectable?: boolean
  selected?: number | null
}>()

const emit = defineEmits<{
  (e: 'select', seal: SealResponse): void
}>()

const seals = ref<SealResponse[]>([])
const loading = ref(false)
const creating = ref(false)

// 新建公章表单
const showCreateForm = ref(false)
const newSealForm = ref({
  sealName: '',
  sealType: 'official',
  generate: true
})

onMounted(() => {
  loadSeals()
})

async function loadSeals() {
  loading.value = true
  try {
    const res = await listSeals()
    if (res.code === 0) {
      seals.value = res.data || []
    }
  } catch (err) {
    console.error('加载公章失败', err)
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!newSealForm.value.sealName.trim()) {
    ElMessage.warning('请输入公章名称')
    return
  }
  
  creating.value = true
  try {
    const res = await createSeal({
      sealName: newSealForm.value.sealName,
      sealType: newSealForm.value.sealType,
      generate: newSealForm.value.generate
    })
    if (res.code === 0) {
      ElMessage.success('公章创建成功')
      showCreateForm.value = false
      newSealForm.value = { sealName: '', sealType: 'official', generate: true }
      await loadSeals()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (err: any) {
    ElMessage.error(err.message || '创建失败')
  } finally {
    creating.value = false
  }
}

async function handleSetDefault(id: number) {
  try {
    const res = await setDefaultSeal(id)
    if (res.code === 0) {
      ElMessage.success('已设为默认公章')
      await loadSeals()
    }
  } catch (err) {
    console.error('设置默认失败', err)
  }
}

async function handleDelete(id: number) {
  try {
    const res = await deleteSeal(id)
    if (res.code === 0) {
      ElMessage.success('公章已删除')
      await loadSeals()
    }
  } catch (err) {
    console.error('删除失败', err)
  }
}

function selectSeal(seal: SealResponse) {
  if (props.selectable) {
    emit('select', seal)
  }
}
</script>

<template>
  <div class="seal-manager">
    <!-- 公章列表 -->
    <div v-if="!loading && seals.length > 0" class="grid grid-cols-2 md:grid-cols-3 gap-4">
      <div 
        v-for="seal in seals" 
        :key="seal.id"
        class="relative bg-white rounded-xl border-2 p-4 cursor-pointer transition-all hover:shadow-md"
        :class="[
          selected === seal.id ? 'border-brand-500 bg-brand-50' : 'border-gray-200 hover:border-gray-200',
          selectable ? 'cursor-pointer' : 'cursor-default'
        ]"
        @click="selectSeal(seal)"
      >
        <!-- 公章图片 -->
        <div class="aspect-square rounded-lg bg-gray-50 flex items-center justify-center mb-3 overflow-hidden">
          <img 
            v-if="seal.sealUrl" 
            v-lazy="seal.sealUrl" 
            :alt="seal.sealName"
            class="max-w-full max-h-full object-contain"
          />
          <div v-else class="text-gray-300 text-sm">无图片</div>
        </div>
        
        <!-- 公章信息 -->
        <div class="text-sm font-medium text-gray-900 truncate">{{ seal.sealName }}</div>
        <div class="text-xs text-gray-400 mt-0.5 flex items-center gap-1">
          <span v-if="seal.isDefault" class="text-brand-600">默认</span>
          <span>{{ seal.sealType === 'official' ? '公章' : seal.sealType === 'contract' ? '合同章' : '电子章' }}</span>
          <span v-if="seal.isGenerated" class="text-blue-500">· 系统生成</span>
        </div>
        
        <!-- 选中标记 -->
        <div 
          v-if="selected === seal.id"
          class="absolute top-2 right-2 w-6 h-6 rounded-full bg-brand-500 flex items-center justify-center"
        >
          <Check class="w-4 h-4 text-white" />
        </div>
        
        <!-- 操作按钮 -->
        <div v-if="!selectable" class="absolute top-2 right-2 flex gap-1">
          <button 
            v-if="!seal.isDefault"
            class="w-6 h-6 rounded-lg bg-gray-100 hover:bg-gray-200 flex items-center justify-center"
            title="设为默认"
            @click.stop="handleSetDefault(seal.id)"
          >
            <Check class="w-3.5 h-3.5 text-gray-500" />
          </button>
          <button 
            class="w-6 h-6 rounded-lg bg-gray-100 hover:bg-red-100 flex items-center justify-center"
            title="删除"
            @click.stop="handleDelete(seal.id)"
          >
            <Trash2 class="w-3.5 h-3.5 text-gray-500 hover:text-red-500" />
          </button>
        </div>
      </div>
      
      <!-- 添加新公章 -->
      <div 
        class="aspect-square bg-gray-50 rounded-xl border-2 border-dashed border-gray-200 flex flex-col items-center justify-center cursor-pointer hover:border-brand-300 hover:bg-brand-50/50 transition-all"
        @click="showCreateForm = true"
      >
        <Plus class="w-8 h-8 text-gray-300 mb-2" />
        <span class="text-sm text-gray-400">添加公章</span>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else-if="!loading" class="text-center py-12">
      <div class="w-16 h-16 mx-auto mb-4 rounded-xl bg-gray-100 flex items-center justify-center">
        <Sparkles class="w-8 h-8 text-gray-300" />
      </div>
      <div class="text-gray-500 mb-4">还没有电子公章</div>
      <button 
        class="px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-xl transition-all "
        @click="showCreateForm = true"
      >
        创建电子公章
      </button>
    </div>
    
    <!-- 加载中 -->
    <div v-else class="text-center py-12 text-gray-400">
      加载中...
    </div>
    
    <!-- 创建公章弹窗 -->
    <Teleport to="body">
      <div v-if="showCreateForm" class="fixed inset-0 z-[2100] flex items-center justify-center">
        <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="showCreateForm = false"></div>
        <div class="relative bg-white rounded-[24px] shadow-2xl w-full max-w-md p-6">
          <h3 class="text-2xl font-bold text-gray-900 mb-4">创建电子公章</h3>
          
          <div class="space-y-4">
            <div>
              <label class="text-sm font-bold text-gray-700 mb-1 block">公章名称</label>
              <input 
                v-model="newSealForm.sealName"
                type="text"
                class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:border-brand-500 outline-none transition-all"
                placeholder="如：公司公章"
              />
            </div>
            
            <div>
              <label class="text-sm font-bold text-gray-700 mb-1 block">公章类型</label>
              <select 
                v-model="newSealForm.sealType"
                class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:border-brand-500 outline-none transition-all"
              >
                <option value="official">公章</option>
                <option value="contract">合同专用章</option>
              </select>
            </div>
            
            <div class="flex items-center gap-3 p-4 bg-gray-50 rounded-xl">
              <input 
                type="checkbox" 
                id="generate" 
                v-model="newSealForm.generate"
                class="w-5 h-5 rounded border-gray-300 text-brand-600 focus:ring-brand-500"
              />
              <label for="generate" class="text-sm text-gray-700">
                <span class="font-medium">自动生成电子章</span>
                <span class="text-gray-400 block text-xs mt-0.5">根据公司名称自动生成圆形电子章</span>
              </label>
            </div>
          </div>
          
          <div class="flex justify-end gap-3 mt-6">
            <button 
              class="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold rounded-xl transition-all"
              @click="showCreateForm = false"
            >
              取消
            </button>
            <button 
              class="px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-xl transition-all disabled:opacity-50"
              :disabled="creating"
              @click="handleCreate"
            >
              {{ creating ? '创建中...' : '创建' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

