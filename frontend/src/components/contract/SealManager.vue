<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Check, Trash2, Stamp, ArrowRight } from 'lucide-vue-next'
import { listSeals, createSeal, setDefaultSeal, deleteSeal, type SealResponse } from '../../api/contract'
import { vLazy } from '../../directives/lazyLoad'
import SealUploader from '../seal/SealUploader.vue'

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
const uploadedSealUrl = ref('')
const confirmBtnRef = ref<HTMLButtonElement>()

const newSealForm = ref({
  sealName: '',
  sealType: 'official',
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
  if (!uploadedSealUrl.value) {
    ElMessage.warning('请先上传并提取印章图片')
    return
  }

  creating.value = true
  try {
    const res = await createSeal({
      sealName: newSealForm.value.sealName,
      sealType: newSealForm.value.sealType,
      sealUrl: uploadedSealUrl.value,
      generate: false
    })
    if (res.code === 0) {
      ElMessage.success('公章创建成功')
      resetForm()
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

function resetForm() {
  showCreateForm.value = false
  uploadedSealUrl.value = ''
  newSealForm.value = { sealName: '', sealType: 'official' }
}

function onSealExtracted(url: string) {
  uploadedSealUrl.value = url
  // 提取完成后，滚动到确认创建按钮并高亮提示
  nextTick(() => {
    confirmBtnRef.value?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  })
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
        class="relative bg-white rounded-xl border-2 p-4 transition-all hover:shadow-md"
        :class="[
          selected === seal.id ? 'border-brand-500 bg-brand-50' : 'border-neutral-200 hover:border-neutral-300',
          selectable ? 'cursor-pointer' : 'cursor-default'
        ]"
        @click="selectSeal(seal)"
      >
        <!-- 公章图片 -->
        <div class="aspect-square rounded-lg bg-neutral-50 flex items-center justify-center mb-3 overflow-hidden">
          <img
            v-if="seal.sealUrl"
            v-lazy="seal.sealUrl"
            :alt="seal.sealName"
            class="max-w-full max-h-full object-contain"
          />
          <div v-else class="text-neutral-300 text-sm">无图片</div>
        </div>

        <!-- 公章信息 -->
        <div class="text-sm font-medium text-neutral-900 truncate">{{ seal.sealName }}</div>
        <div class="text-xs text-neutral-400 mt-0.5 flex items-center gap-1">
          <span v-if="seal.isDefault" class="text-brand-600 font-medium">默认</span>
          <span>{{ seal.sealType === 'official' ? '公章' : seal.sealType === 'contract' ? '合同章' : '电子章' }}</span>
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
            class="w-6 h-6 rounded-lg bg-neutral-100 hover:bg-neutral-200 flex items-center justify-center"
            title="设为默认"
            @click.stop="handleSetDefault(seal.id)"
          >
            <Check class="w-3.5 h-3.5 text-neutral-500" />
          </button>
          <button
            class="w-6 h-6 rounded-lg bg-neutral-100 hover:bg-error-100 flex items-center justify-center"
            title="删除"
            @click.stop="handleDelete(seal.id)"
          >
            <Trash2 class="w-3.5 h-3.5 text-neutral-500 hover:text-error-500" />
          </button>
        </div>
      </div>

      <!-- 添加新公章 -->
      <div
        class="aspect-square bg-neutral-50 rounded-xl border-2 border-dashed border-neutral-200 flex flex-col items-center justify-center cursor-pointer hover:border-brand-300 hover:bg-brand-50/50 transition-all"
        @click="showCreateForm = true"
      >
        <Plus class="w-8 h-8 text-neutral-300 mb-2" />
        <span class="text-sm text-neutral-400">添加公章</span>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading" class="text-center py-12">
      <div class="w-16 h-16 mx-auto mb-4 rounded-xl bg-neutral-100 flex items-center justify-center">
        <Stamp class="w-8 h-8 text-neutral-300" />
      </div>
      <div class="text-neutral-500 mb-2">还没有电子公章</div>
      <div class="text-xs text-neutral-400 mb-4">上传公章/合同章的印鉴照片即可创建</div>
      <button
        class="px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-xl transition-all active:scale-95"
        @click="showCreateForm = true"
      >
        上传印鉴创建
      </button>
    </div>

    <!-- 加载中 -->
    <div v-else class="text-center py-12 text-neutral-400">
      加载中...
    </div>

    <!-- 创建公章弹窗 -->
    <Teleport to="body">
      <div v-if="showCreateForm" class="fixed inset-0 z-[2100] flex items-center justify-center">
        <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="resetForm"></div>
        <div class="relative bg-white rounded-[24px] shadow-2xl w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto">
          <h3 class="text-2xl font-bold text-neutral-900 mb-1">上传印鉴创建公章</h3>
          <p class="text-sm text-neutral-500 mb-5">请在白纸上清晰盖章后拍照上传，系统自动提取红色印章</p>

          <div class="space-y-5">
            <!-- Step 1: 基本信息 -->
            <div>
              <div class="flex items-center gap-2 mb-3">
                <div class="w-6 h-6 rounded-full bg-brand-600 text-white flex items-center justify-center text-xs font-bold">1</div>
                <span class="text-sm font-bold text-neutral-700">填写公章信息</span>
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="text-xs font-medium text-neutral-500 mb-1 block">公章名称</label>
                  <input
                    v-model="newSealForm.sealName"
                    type="text"
                    class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-xl focus:border-brand-500 outline-none transition-all"
                    placeholder="如：公司公章"
                  />
                </div>
                <div>
                  <label class="text-xs font-medium text-neutral-500 mb-1 block">公章类型</label>
                  <select
                    v-model="newSealForm.sealType"
                    class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-xl focus:border-brand-500 outline-none transition-all"
                  >
                    <option value="official">公章</option>
                    <option value="contract">合同专用章</option>
                  </select>
                </div>
              </div>
            </div>

            <!-- Step 2: 上传印鉴 -->
            <div>
              <div class="flex items-center gap-2 mb-3">
                <div class="w-6 h-6 rounded-full text-white flex items-center justify-center text-xs font-bold" :class="uploadedSealUrl ? 'bg-brand-600' : 'bg-neutral-300'">2</div>
                <span class="text-sm font-bold text-neutral-700">上传印鉴照片</span>
                <Check v-if="uploadedSealUrl" class="w-4 h-4 text-brand-600" />
              </div>
              <SealUploader @extracted="onSealExtracted" />
            </div>

            <!-- Step 3: 确认创建（提取完成后高亮显示） -->
            <div
              class="rounded-xl p-4 border-2 transition-all"
              :class="uploadedSealUrl ? 'border-brand-500 bg-brand-50 animate-pulse-once' : 'border-neutral-100 bg-neutral-50 opacity-50'"
            >
              <div class="flex items-center gap-2 mb-3">
                <div class="w-6 h-6 rounded-full text-white flex items-center justify-center text-xs font-bold" :class="uploadedSealUrl ? 'bg-brand-600' : 'bg-neutral-300'">3</div>
                <span class="text-sm font-bold text-neutral-700">确认创建公章</span>
                <ArrowRight v-if="uploadedSealUrl" class="w-4 h-4 text-brand-600 animate-bounce-x" />
              </div>
              <p class="text-xs text-neutral-500 mb-3">确认以上信息无误后，点击下方按钮完成公章创建</p>
              <div class="flex gap-3">
                <button
                  class="flex-1 px-4 py-2.5 bg-neutral-100 hover:bg-neutral-200 text-neutral-700 font-bold rounded-xl transition-all"
                  @click="resetForm"
                >
                  取消
                </button>
                <button
                  ref="confirmBtnRef"
                  class="flex-1 px-4 py-2.5 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-xl transition-all disabled:opacity-50 disabled:cursor-not-allowed active:scale-95"
                  :disabled="creating || !uploadedSealUrl || !newSealForm.sealName.trim()"
                  @click="handleCreate"
                >
                  {{ creating ? '创建中...' : '确认创建公章' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
@keyframes pulse-once {
  0%, 100% { box-shadow: 0 0 0 0 transparent; }
  50% { box-shadow: 0 0 0 6px rgba(45, 106, 79, 0.15); }
}
.animate-pulse-once {
  animation: pulse-once 1s ease-in-out 2;
}
@keyframes bounce-x {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(4px); }
}
.animate-bounce-x {
  animation: bounce-x 0.8s ease-in-out infinite;
}
</style>
