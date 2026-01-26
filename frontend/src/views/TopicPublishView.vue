<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, ref, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createPost } from '../api/post'
import { getPointsMe } from '../api/points'
import { useAuthStore } from '../store/auth'
import { requireAuth } from '../utils/requireAuth'
import { Coins, ImagePlus, X, Loader2, ArrowLeft } from 'lucide-vue-next'

// WangEditor
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import type { IEditorConfig, IToolbarConfig } from '@wangeditor/editor'

const router = useRouter()
const auth = useAuthStore()

type PostType = 'general' | 'poll' | 'paid'
const postType = ref<PostType>('general')

const title = ref('')
const content = ref('')  // HTML 内容
const submitting = ref(false)

const price = ref(9.9)
const teaserLength = ref(100)
const myPoints = ref(0)

// 封面图上传
const coverImages = ref<string[]>([])
const uploadingCover = ref(false)
const coverInputRef = ref<HTMLInputElement | null>(null)

// WangEditor 实例
const editorRef = shallowRef()
const mode = 'default'

const showPaidSettings = computed(() => postType.value === 'paid')

// 编辑器工具栏配置
const toolbarConfig: Partial<IToolbarConfig> = {
  excludeKeys: [
    'group-video',
    'insertTable',
    'codeBlock',
    'todo',
    'fullScreen',
  ]
}

// 编辑器配置
const editorConfig: Partial<IEditorConfig> = {
  placeholder: '写下你的想法...',
  MENU_CONF: {
    uploadImage: {
      async customUpload(file: File, insertFn: (url: string, alt?: string, href?: string) => void) {
        if (file.size > 10 * 1024 * 1024) {
          ElMessage.warning('图片大小不能超过 10MB')
          return
        }

        const formData = new FormData()
        formData.append('file', file)

        try {
          const response = await fetch('/api/files/upload/image', {
            method: 'POST',
            body: formData,
            credentials: 'include'
          })
          const result = await response.json()

          if (result.code === 0 && result.data?.fileUrl) {
            insertFn(result.data.fileUrl, file.name)
          } else {
            throw new Error(result.message || '上传失败')
          }
        } catch (e: any) {
          ElMessage.error(e?.message || '图片上传失败')
        }
      },
      allowedFileTypes: ['image/*'],
      maxFileSize: 10 * 1024 * 1024,
    }
  }
}

function handleCreated(editor: any) {
  editorRef.value = editor
}

function close() {
  router.back()
}

// 封面图上传
function triggerCoverUpload() {
  coverInputRef.value?.click()
}

async function handleCoverUpload(e: Event) {
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

  uploadingCover.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const response = await fetch('/api/files/upload/image', {
      method: 'POST',
      body: formData,
      credentials: 'include'
    })
    const result = await response.json()

    if (result.code === 0 && result.data?.fileUrl) {
      coverImages.value.push(result.data.fileUrl)
      ElMessage.success('封面图上传成功')
    } else {
      throw new Error(result.message || '上传失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '图片上传失败')
  } finally {
    uploadingCover.value = false
    input.value = ''
  }
}

function removeCoverImage(index: number) {
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

function getPlainText(): string {
  if (!editorRef.value) return ''
  return editorRef.value.getText().trim()
}

async function submit() {
  if (!title.value.trim()) return ElMessage.warning('请输入标题')

  const plainText = getPlainText()
  if (!plainText) return ElMessage.warning('请输入内容')

  if (!requireAuth('/talks/publish')) {
    ElMessage.warning('请先登录后再发布话题')
    return
  }

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
      content: content.value || undefined,
      imagesJson: coverImages.value.length > 0 ? JSON.stringify(coverImages.value) : undefined,
      postType: postType.value,
      isPaid: postType.value === 'paid',
      price: postType.value === 'paid' ? price.value : undefined,
      teaserLength: postType.value === 'paid' ? teaserLength.value : undefined
    })
    if (r.code !== 0) throw new Error(r.message)
    ElMessage.success('发布成功！')
    title.value = ''
    content.value = ''
    coverImages.value = []
    postType.value = 'general'
    router.push('/talks')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '发布失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (!auth.me) requireAuth('/talks/publish')
  loadMyPoints()
})

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) {
    editor.destroy()
  }
})
</script>

<template>
  <div class="min-h-screen bg-white flex flex-col">
    <!-- 顶部导航栏 -->
    <header class="h-14 border-b border-gray-100 flex items-center justify-between px-6 bg-white sticky top-0 z-50">
      <div class="flex items-center gap-4">
        <button
          class="flex items-center gap-2 text-gray-500 hover:text-gray-900 transition-colors"
          @click="close"
        >
          <ArrowLeft :size="20" />
          <span class="text-sm font-medium">返回</span>
        </button>
        <div class="w-px h-5 bg-gray-200"></div>
        <h1 class="text-base font-bold text-gray-900">发布话题</h1>
      </div>

      <div class="flex items-center gap-4">
        <!-- 类型选择 -->
        <div class="flex items-center gap-1 bg-gray-100 p-1 rounded-lg">
          <button
            class="px-4 py-1.5 rounded-md text-sm font-medium transition-all"
            :class="postType === 'general' ? 'bg-white text-brand-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'"
            @click="postType = 'general'"
          >
            普通话题
          </button>
          <button
            class="px-4 py-1.5 rounded-md text-sm font-medium transition-all"
            :class="postType === 'paid' ? 'bg-white text-amber-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'"
            @click="postType = 'paid'"
          >
            专家文章
          </button>
          <button
            class="px-4 py-1.5 rounded-md text-sm font-medium transition-all"
            :class="postType === 'poll' ? 'bg-white text-brand-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'"
            @click="postType = 'poll'"
          >
            发起投票
          </button>
        </div>

        <!-- 发布按钮 -->
        <button
          class="bg-brand-600 text-white px-6 py-2 rounded-lg text-sm font-bold hover:bg-brand-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed active:scale-95"
          :disabled="submitting"
          @click="submit"
        >
          {{ submitting ? '发布中...' : '发布' }}
        </button>
      </div>
    </header>

    <!-- 工具栏 - 固定在顶部导航下方 -->
    <div class="border-b border-gray-100 bg-white sticky top-14 z-40">
      <div class="max-w-4xl mx-auto px-6">
        <Toolbar
          class="editor-toolbar"
          :editor="editorRef"
          :defaultConfig="toolbarConfig"
          :mode="mode"
        />
      </div>
    </div>

    <!-- 主编辑区域 -->
    <main class="flex-1 flex justify-center bg-white">
      <div class="w-full max-w-4xl px-6 py-8">
        <!-- 标题输入 -->
        <input
          v-model="title"
          type="text"
          placeholder="请输入标题"
          class="w-full text-2xl font-bold placeholder:text-gray-300 outline-none border-none bg-transparent"
          maxlength="100"
        />

        <!-- 分隔线 -->
        <div class="h-px bg-gray-100 my-4"></div>

        <!-- 编辑器内容区（无边框，与标题融为一体） -->
        <div class="editor-wrapper">
          <Editor
            class="editor-content"
            v-model="content"
            :defaultConfig="editorConfig"
            :mode="mode"
            @onCreated="handleCreated"
          />
        </div>
      </div>
    </main>

    <!-- 底部工具栏 -->
    <footer class="border-t border-gray-100 bg-gray-50 px-6 py-4 sticky bottom-0 z-40">
      <div class="max-w-4xl mx-auto flex items-center justify-between">
        <!-- 左侧：封面图上传 -->
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-3">
            <span class="text-sm text-gray-500">封面图</span>
            <div class="flex gap-2">
              <!-- 已上传的封面图 -->
              <div
                v-for="(img, idx) in coverImages"
                :key="idx"
                class="relative w-14 h-14 rounded-lg overflow-hidden group border border-gray-200"
              >
                <img :src="img" class="w-full h-full object-cover" />
                <button
                  class="absolute inset-0 bg-black/50 flex items-center justify-center text-white opacity-0 group-hover:opacity-100 transition-opacity"
                  @click="removeCoverImage(idx)"
                >
                  <X :size="16" />
                </button>
              </div>

              <!-- 上传按钮 -->
              <button
                v-if="coverImages.length < 1"
                class="w-14 h-14 rounded-lg border-2 border-dashed border-gray-300 flex items-center justify-center text-gray-400 hover:border-brand-400 hover:text-brand-500 transition-colors"
                :disabled="uploadingCover"
                @click="triggerCoverUpload"
              >
                <Loader2 v-if="uploadingCover" :size="18" class="animate-spin" />
                <ImagePlus v-else :size="20" />
              </button>
            </div>
            <span class="text-xs text-gray-400">建议尺寸 16:9</span>
          </div>

          <input
            ref="coverInputRef"
            type="file"
            accept="image/*"
            class="hidden"
            @change="handleCoverUpload"
          />
        </div>

        <!-- 右侧：付费设置 -->
        <div v-if="showPaidSettings" class="flex items-center gap-4 bg-amber-50 rounded-lg px-4 py-2 border border-amber-200">
          <Coins :size="16" class="text-amber-600" />
          <div class="flex items-center gap-3">
            <div class="flex items-center gap-2">
              <span class="text-sm text-amber-700">阅读价格</span>
              <el-input-number v-model="price" :min="0.1" :max="999" :precision="1" :step="1" size="small" class="!w-24" />
              <span class="text-sm text-amber-600">积分</span>
            </div>
            <div class="w-px h-5 bg-amber-200"></div>
            <div class="flex items-center gap-2">
              <span class="text-sm text-amber-700">免费预览</span>
              <el-input-number v-model="teaserLength" :min="20" :max="1000" :step="10" size="small" class="!w-24" />
              <span class="text-sm text-amber-600">字</span>
            </div>
          </div>
        </div>

        <!-- 非付费时显示提示 -->
        <div v-else class="text-sm text-gray-400">
          支持粘贴或拖拽图片到编辑器
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* 工具栏样式 */
.editor-toolbar :deep(.w-e-toolbar) {
  background-color: transparent;
  border: none;
  padding: 8px 0;
}

/* 编辑器容器样式 */
.editor-wrapper :deep(.w-e-text-container) {
  background-color: transparent;
  border: none !important;
}

.editor-content {
  min-height: calc(100vh - 320px);
  height: auto !important;
}

.editor-content :deep(.w-e-text-placeholder) {
  color: #d1d5db;
  font-style: normal;
  font-size: 17px;
  line-height: 1.875;
}

.editor-content :deep(p) {
  margin: 0.5em 0;
  line-height: 1.875;
  font-size: 17px;
  color: #374151;
}

.editor-content :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 1em 0;
}

.editor-content :deep(blockquote) {
  border-left: 4px solid #e5e7eb;
  padding-left: 1em;
  margin: 1em 0;
  color: #6b7280;
  font-style: italic;
}

.editor-content :deep(h1),
.editor-content :deep(h2),
.editor-content :deep(h3) {
  font-weight: 700;
  margin-top: 1.25em;
  margin-bottom: 0.5em;
  color: #111827;
}

.editor-content :deep(ul),
.editor-content :deep(ol) {
  margin: 0.75em 0;
  padding-left: 1.5em;
}

.editor-content :deep(li) {
  margin: 0.25em 0;
}
</style>
