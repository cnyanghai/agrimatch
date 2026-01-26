<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createPost } from '../api/post'
import { getPointsMe } from '../api/points'
import { uploadImage } from '../api/file'
import { useAuthStore } from '../store/auth'
import { requireAuth } from '../utils/requireAuth'
import { Coins, BookOpen, HelpCircle, LayoutGrid, Info, ImagePlus, X, Loader2 } from 'lucide-vue-next'

const router = useRouter()
const auth = useAuthStore()

type PostType = 'general' | 'poll' | 'paid'
const postType = ref<PostType>('general')

const title = ref('')
const content = ref('')
const submitting = ref(false)

const price = ref(9.9)
const teaserLength = ref(100)
const myPoints = ref(0)

// 封面图上传
const coverImages = ref<string[]>([])
const uploadingImage = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)

const showPaidSettings = computed(() => postType.value === 'paid')

function close() {
  router.back()
}

// 图片上传相关
function triggerImageUpload() {
  fileInputRef.value?.click()
}

async function handleImageUpload(e: Event) {
  const input = e.target as HTMLInputElement
  const files = input.files
  if (!files || files.length === 0) return

  const file = files[0]
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return
  }

  uploadingImage.value = true
  try {
    const r = await uploadImage(file)
    if (r.code === 0 && r.data) {
      coverImages.value.push(r.data.fileUrl)
      ElMessage.success('图片上传成功')
    } else {
      throw new Error(r.message || '上传失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '图片上传失败')
  } finally {
    uploadingImage.value = false
    input.value = ''  // 重置 input 以便重复上传同一文件
  }
}

function removeImage(index: number) {
  coverImages.value.splice(index, 1)
}

async function loadMyPoints() {
  try {
    const r = await getPointsMe()
    if (r.code === 0 && r.data) {
      myPoints.value = r.data.pointsBalance ?? 0
    }
  } catch {
    // ignore
  }
}

async function submit() {
  if (!title.value.trim()) return ElMessage.warning('请输入标题')
  if (!content.value.trim()) return ElMessage.warning('请输入内容')
  
  // 检查登录状态
  if (!requireAuth('/talks/publish')) {
    ElMessage.warning('请先登录后再发布话题')
    return
  }

  // 付费类型检查
  if (postType.value === 'paid') {
    if (!price.value || price.value <= 0) {
      ElMessage.warning('请输入有效的阅读价格')
      return
    }
  }
  
  submitting.value = true
  try {
    const r = await createPost({
      title: title.value.trim(),
      content: content.value.trim() || undefined,
      imagesJson: coverImages.value.length > 0 ? JSON.stringify(coverImages.value) : undefined,
      postType: postType.value,
      isPaid: postType.value === 'paid',
      price: postType.value === 'paid' ? price.value : undefined,
      teaserLength: postType.value === 'paid' ? teaserLength.value : undefined
    })
    if (r.code !== 0) throw new Error(r.message)
    ElMessage.success('发布成功！')
    // 清空表单
    title.value = ''
    content.value = ''
    coverImages.value = []
    postType.value = 'general'
    // 返回话题广场
    router.push('/talks')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '发布失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  // 检查登录状态
  if (!auth.me) requireAuth('/talks/publish')
  loadMyPoints()
})
</script>

<template>
  <div class="bg-gray-100 min-h-screen flex items-center justify-center p-4 sm:p-6">
    <div class="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6">
      <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="close"></div>

      <div class="relative bg-white w-full max-w-2xl rounded-2xl shadow-2xl overflow-hidden flex flex-col animate-fade-in animate-zoom-in">
        <!-- 头部 -->
        <div class="px-8 py-6 border-b flex items-center justify-between bg-white sticky top-0 z-10">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-indigo-50 rounded-lg flex items-center justify-center text-indigo-600">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"></path></svg>
            </div>
            <div>
              <h2 class="text-2xl font-bold text-gray-900">发布新话题</h2>
              <p class="text-[10px] text-gray-400 font-medium uppercase tracking-wider">分享行业见解，共建透明市场</p>
            </div>
          </div>
          <button class="p-2 hover:bg-gray-100 rounded-full text-gray-400 transition-colors" @click="close">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </div>

        <!-- 主体 -->
        <div class="flex-1 overflow-y-auto p-8 space-y-8 max-h-[70vh]">
          <section>
            <label class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-4 block">选择发布类型</label>
            <div class="grid grid-cols-3 gap-4">
              <button
                class="type-btn flex flex-col items-center gap-2 p-4 rounded-xl border-2 transition-all"
                :class="postType === 'general' ? 'border-brand-600 bg-brand-50 text-brand-600' : 'border-gray-100 text-gray-500 hover:border-brand-200'"
                @click="postType = 'general'"
              >
                <LayoutGrid :size="20" />
                <span class="text-sm font-bold">普通话题</span>
              </button>
              <button
                class="type-btn flex flex-col items-center gap-2 p-4 rounded-xl border-2 transition-all"
                :class="postType === 'paid' ? 'border-amber-500 bg-amber-50 text-amber-600' : 'border-gray-100 text-gray-500 hover:border-amber-200'"
                @click="postType = 'paid'"
              >
                <BookOpen :size="20" />
                <span class="text-sm font-bold">专家文章</span>
              </button>
              <button
                class="type-btn flex flex-col items-center gap-2 p-4 rounded-xl border-2 transition-all"
                :class="postType === 'poll' ? 'border-brand-500 bg-brand-50 text-brand-600' : 'border-gray-100 text-gray-500 hover:border-brand-200'"
                @click="postType = 'poll'"
              >
                <HelpCircle :size="20" />
                <span class="text-sm font-bold">发起投票</span>
              </button>
            </div>
          </section>

          <section class="space-y-4">
            <input v-model="title" type="text" placeholder="给你的话题起个响亮的标题" class="w-full text-2xl font-black placeholder:text-gray-200 outline-none border-none" />
            <div class="h-px bg-gray-100 w-full"></div>
            <textarea v-model="content" placeholder="分享你的见解、经验或疑问..." class="w-full h-64 resize-none outline-none text-gray-700 leading-relaxed placeholder:text-gray-200 text-base"></textarea>
          </section>

          <!-- 封面图上传 -->
          <section class="space-y-3">
            <div class="flex items-center justify-between">
              <label class="text-xs font-bold text-gray-400 uppercase tracking-widest flex items-center gap-2">
                <ImagePlus :size="14" />
                封面配图
                <span class="text-gray-300 normal-case tracking-normal font-normal">(推荐)</span>
              </label>
              <span class="text-[10px] text-gray-400">添加配图可以吸引更多读者关注</span>
            </div>

            <div class="flex flex-wrap gap-3">
              <!-- 已上传的图片 -->
              <div
                v-for="(img, idx) in coverImages"
                :key="idx"
                class="relative w-24 h-24 rounded-xl overflow-hidden group border border-gray-100"
              >
                <img :src="img" class="w-full h-full object-cover" />
                <button
                  class="absolute top-1 right-1 w-6 h-6 bg-black/50 rounded-full flex items-center justify-center text-white opacity-0 group-hover:opacity-100 transition-opacity hover:bg-black/70"
                  @click="removeImage(idx)"
                >
                  <X :size="14" />
                </button>
              </div>

              <!-- 上传按钮 -->
              <button
                v-if="coverImages.length < 9"
                class="w-24 h-24 rounded-xl border-2 border-dashed border-gray-200 flex flex-col items-center justify-center gap-1 text-gray-400 hover:border-brand-400 hover:text-brand-500 transition-colors"
                :disabled="uploadingImage"
                @click="triggerImageUpload"
              >
                <Loader2 v-if="uploadingImage" :size="24" class="animate-spin" />
                <template v-else>
                  <ImagePlus :size="24" />
                  <span class="text-[10px] font-medium">添加图片</span>
                </template>
              </button>
            </div>

            <input
              ref="fileInputRef"
              type="file"
              accept="image/*"
              class="hidden"
              @change="handleImageUpload"
            />
          </section>

          <!-- 付费设置区块 -->
          <div v-if="showPaidSettings" class="bg-amber-50 rounded-2xl p-6 border border-amber-100 space-y-6 animate-fade-in">
            <div class="flex items-center gap-2 text-amber-800 font-bold">
              <Coins :size="18" />
              <span>付费文章设置</span>
            </div>
            
            <div class="grid grid-cols-2 gap-8">
              <div class="space-y-3">
                <div class="flex justify-between items-center">
                  <label class="text-xs font-black text-amber-700 uppercase tracking-wider">阅读价格 (积分)</label>
                  <span class="text-lg font-black text-amber-600">{{ price }} pts</span>
                </div>
                <el-input-number v-model="price" :min="0.1" :max="999" :precision="1" :step="1" class="!w-full" />
              </div>
              
              <div class="space-y-3">
                <div class="flex justify-between items-center">
                  <label class="text-xs font-black text-amber-700 uppercase tracking-wider">免费预览字数</label>
                  <span class="text-lg font-black text-amber-600">{{ teaserLength }} 字</span>
                </div>
                <el-slider v-model="teaserLength" :min="20" :max="1000" :step="10" />
              </div>
            </div>

            <div class="flex items-start gap-2 bg-white/50 p-3 rounded-lg border border-amber-200/50">
              <Info :size="14" class="text-amber-500 mt-0.5 shrink-0" />
              <p class="text-[10px] text-amber-700 leading-normal">
                只有通过平台认证的专家才建议发布付费文章。普通用户发布的付费文章若质量不佳，可能会被举报并导致积分冻结。
              </p>
            </div>
          </div>
        </div>

        <!-- 底部 -->
        <div class="px-8 py-6 bg-gray-50 border-t flex items-center justify-between">
          <div class="flex items-center gap-2">
            <div class="w-2 h-2 bg-brand-500 rounded-full animate-pulse"></div>
            <span class="text-[10px] text-gray-500 font-bold">账号已登录 · 发布可获得积分</span>
          </div>
          <div class="flex gap-4">
            <button class="text-sm font-bold text-gray-400 hover:text-gray-600" @click="close">取消</button>
            <button 
              class="bg-indigo-600 text-white px-8 py-2.5 rounded-lg text-sm font-bold shadow-md shadow-indigo-100 hover:bg-indigo-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed" 
              :disabled="submitting"
              @click="submit"
            >
              {{ submitting ? '发布中...' : '立即发布' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}
.animate-zoom-in {
  animation: zoomIn 0.3s ease-out;
}
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
@keyframes zoomIn {
  from {
    transform: scale(0.95);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
</style>


