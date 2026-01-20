<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Present } from '@element-plus/icons-vue'
import PublicTopNav from '../components/PublicTopNav.vue'
import { 
  getPost, 
  togglePostLike, 
  listPostComments, 
  createPostComment,
  acceptAnswer,
  type PostResponse,
  type PostCommentResponse 
} from '../api/post'
import { giftPoints } from '../api/points'
import { useAuthStore } from '../store/auth'
import { requireAuth } from '../utils/requireAuth'
import PublicFooter from '../components/PublicFooter.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const postId = ref<number>(Number(route.params.id))
const post = ref<PostResponse | null>(null)
const loading = ref(false)
const liking = ref(false)
const comments = ref<PostCommentResponse[]>([])
const commentText = ref('')
const commenting = ref(false)

// 打赏对话框
const tipDialogOpen = ref(false)
const tipForm = reactive({
  points: 10,
  remark: ''
})
const tipping = ref(false)
const accepting = ref(false)

// 是否是赏金帖且可以采纳
const isBountyPost = () => post.value?.postType === 'bounty'
const canAccept = () => isBountyPost() && post.value?.bountyStatus === 0 && post.value?.userId === auth.me?.userId
const isAccepted = (comment: PostCommentResponse) => comment.isAccepted === 1 || comment.id === post.value?.acceptedCommentId

async function loadPost() {
  if (!postId.value) {
    ElMessage.error('话题ID无效')
    router.back()
    return
  }
  
  loading.value = true
  try {
    const r = await getPost(postId.value)
    if (r.code !== 0) throw new Error(r.message)
    post.value = r.data ?? null
    if (!post.value) {
      ElMessage.error('话题不存在')
      router.back()
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载话题失败')
    router.back()
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  if (!postId.value) return
  try {
    const r = await listPostComments(postId.value)
    if (r.code !== 0) throw new Error(r.message)
    comments.value = r.data ?? []
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载评论失败')
  }
}

async function onToggleLike() {
  if (!post.value) return
  if (liking.value) return
  
  // 检查登录状态
  if (!requireAuth('/talks')) {
    ElMessage.warning('请先登录后再点赞')
    return
  }
  
  liking.value = true
  try {
    const r = await togglePostLike(post.value.id)
    if (r.code !== 0) throw new Error(r.message)
    if (post.value) {
      post.value.likedByMe = r.data?.liked ?? post.value.likedByMe
      post.value.likeCount = r.data?.likeCount ?? post.value.likeCount
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '点赞失败')
  } finally {
    liking.value = false
  }
}

async function onAddComment() {
  if (!postId.value) return
  const content = commentText.value.trim()
  if (!content) return
  
  // 检查登录状态
  if (!requireAuth('/talks')) {
    ElMessage.warning('请先登录后再评论')
    return
  }
  
  commenting.value = true
  try {
    const r = await createPostComment(postId.value, content)
    if (r.code !== 0) throw new Error(r.message)
    commentText.value = ''
    await loadComments()
    if (post.value) {
      post.value.commentCount = (post.value.commentCount ?? 0) + 1
    }
    ElMessage.success('评论成功')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '评论失败')
  } finally {
    commenting.value = false
  }
}

function openTipDialog() {
  if (!post.value?.userId) {
    ElMessage.warning('无法获取作者信息')
    return
  }
  // 不能打赏自己
  if (post.value.userId === auth.me?.userId) {
    ElMessage.info('不能打赏自己的话题哦')
    return
  }
  tipForm.points = 10
  tipForm.remark = ''
  tipDialogOpen.value = true
}

async function submitTip() {
  if (!post.value?.userId) {
    ElMessage.warning('无法获取作者信息')
    return
  }
  
  if (tipForm.points < 1) {
    ElMessage.warning('打赏积分数量至少为1')
    return
  }
  
  // 检查登录状态
  if (!requireAuth('/talks')) {
    ElMessage.warning('请先登录后再打赏')
    return
  }
  
  tipping.value = true
  try {
    const authorName = post.value.nickName || post.value.userName || '作者'
    await giftPoints(
      post.value.userId, 
      tipForm.points, 
      tipForm.remark || `打赏话题《${post.value.title}》`
    )
    ElMessage.success(`已成功打赏 ${tipForm.points} 积分给 ${authorName}`)
    tipDialogOpen.value = false
  } catch (e: any) {
    ElMessage.error(e?.message || '打赏失败，请稍后重试')
  } finally {
    tipping.value = false
  }
}

async function onAcceptAnswer(commentId: number) {
  if (!postId.value || !post.value) return
  if (accepting.value) return
  
  accepting.value = true
  try {
    const r = await acceptAnswer(postId.value, commentId)
    if (r.code !== 0) throw new Error(r.message)
    ElMessage.success('采纳成功！积分已发放给回答者')
    // 刷新帖子和评论
    await loadPost()
    await loadComments()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '采纳失败')
  } finally {
    accepting.value = false
  }
}

function formatTime(timeStr: string | undefined) {
  if (!timeStr) return '未知时间'
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  loadPost()
  loadComments()
})
</script>

<template>
  <div class="bg-gray-50 text-gray-900 min-h-screen">
    <PublicTopNav>
      <template #actions>
        <button class="text-sm font-bold text-gray-500 hover:text-emerald-600 transition-all active:scale-95" @click="router.push('/talks')">
          ← 返回话题广场
        </button>
      </template>
    </PublicTopNav>

    <main class="max-w-4xl mx-auto px-4 py-8" v-loading="loading">
      <!-- 话题内容 -->
      <div v-if="post" class="bg-white rounded-3xl p-8 border border-gray-100 shadow-sm mb-6">
        <div class="flex items-center gap-3 mb-6">
          <div class="w-12 h-12 rounded-full bg-emerald-600 text-white flex items-center justify-center text-lg font-bold">
            {{ (post.nickName || post.userName || '?')[0] }}
          </div>
          <div class="flex-1">
            <div class="font-bold text-gray-900">{{ post.nickName || post.userName || '匿名用户' }}</div>
            <div class="text-xs text-gray-400">{{ formatTime(post.createTime) }} · {{ post.companyName || '个人' }}</div>
          </div>
        </div>

        <h1 class="text-3xl font-extrabold text-gray-900 mb-4">{{ post.title }}</h1>
        
        <!-- 赏金信息 -->
        <div v-if="isBountyPost()" class="mb-6">
          <div 
            v-if="post.bountyStatus === 1"
            class="inline-flex items-center gap-2 px-4 py-2 bg-emerald-50 border border-emerald-200 rounded-xl text-emerald-700"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
            </svg>
            <span class="font-bold">已采纳最佳答案</span>
            <span class="text-emerald-600">+{{ post.bountyPoints }} 积分已发放</span>
          </div>
          <div 
            v-else
            class="inline-flex items-center gap-2 px-4 py-2 bg-amber-50 border border-amber-200 rounded-xl text-amber-700"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            <span class="font-bold">悬赏 {{ post.bountyPoints }} 积分</span>
            <span class="text-amber-600">等待采纳</span>
          </div>
        </div>
        
        <div v-if="post.content" class="prose max-w-none mb-8">
          <p class="text-gray-700 leading-relaxed whitespace-pre-wrap">{{ post.content }}</p>
        </div>

        <!-- 互动操作 -->
        <div class="flex items-center gap-4 pt-6 border-t border-gray-100">
          <button
            :class="[
              'flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-bold transition-all',
              post.likedByMe 
                ? 'bg-red-50 text-red-600 hover:bg-red-100' 
                : 'bg-gray-50 text-gray-600 hover:bg-gray-100'
            ]"
            :disabled="liking"
            @click="onToggleLike"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
            </svg>
            {{ post.likeCount ?? 0 }}
          </button>
          
          <button
            class="flex items-center gap-2 px-4 py-2 bg-gray-50 text-gray-600 rounded-xl text-sm font-bold hover:bg-gray-100 transition-all"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
            </svg>
            {{ post.commentCount ?? 0 }} 条评论
          </button>

          <button
            class="flex items-center gap-2 px-4 py-2 bg-amber-50 text-amber-600 rounded-xl text-sm font-bold hover:bg-amber-100 transition-all"
            @click="openTipDialog"
          >
            <el-icon><Present /></el-icon>
            打赏
          </button>
        </div>
      </div>

      <!-- 评论区域 -->
      <div class="bg-white rounded-3xl p-8 border border-gray-100 shadow-sm mb-6">
        <h2 class="text-xl font-bold text-gray-900 mb-6">评论 ({{ comments.length }})</h2>
        
        <!-- 发表评论 -->
        <div class="mb-8 pb-8 border-b border-gray-100">
          <textarea
            v-model="commentText"
            placeholder="写下你的评论..."
            class="w-full h-24 p-4 border border-gray-200 rounded-xl resize-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 outline-none"
            :disabled="commenting"
          ></textarea>
          <div class="flex justify-end mt-4">
            <button
              class="px-6 py-2 bg-emerald-600 text-white rounded-xl text-sm font-bold hover:bg-emerald-700 transition-all disabled:opacity-50"
              :disabled="!commentText.trim() || commenting"
              @click="onAddComment"
            >
              {{ commenting ? '提交中...' : '发表评论' }}
            </button>
          </div>
        </div>

        <!-- 评论列表 -->
        <div v-if="comments.length === 0" class="text-center py-12 text-gray-400">
          暂无评论，快来发表第一条评论吧！
        </div>
        
        <div v-else class="space-y-6">
          <div
            v-for="comment in comments"
            :key="comment.id"
            class="flex gap-4 pb-6 border-b border-gray-50 last:border-b-0"
            :class="{ 'bg-emerald-50/50 -mx-4 px-4 py-4 rounded-2xl border border-emerald-200': isAccepted(comment) }"
          >
            <div 
              class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0"
              :class="isAccepted(comment) ? 'bg-emerald-600 text-white' : 'bg-emerald-100 text-emerald-600'"
            >
              {{ (comment.nickName || comment.userName || '?')[0] }}
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-2">
                <span class="font-bold text-gray-900">{{ comment.nickName || comment.userName || '匿名用户' }}</span>
                <!-- 已采纳标记 -->
                <span 
                  v-if="isAccepted(comment)"
                  class="inline-flex items-center gap-1 px-2 py-0.5 bg-emerald-600 text-white text-[10px] font-bold rounded-lg"
                >
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                  </svg>
                  最佳答案
                </span>
                <span class="text-xs text-gray-400">{{ formatTime(comment.createTime) }}</span>
                <span v-if="comment.companyName" class="text-xs text-gray-400">· {{ comment.companyName }}</span>
              </div>
              <p class="text-gray-700 leading-relaxed whitespace-pre-wrap">{{ comment.content }}</p>
              
              <!-- 采纳按钮 -->
              <div v-if="canAccept() && !isAccepted(comment) && comment.userId !== auth.me?.userId" class="mt-3">
                <button
                  class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-amber-100 text-amber-700 rounded-lg text-xs font-bold hover:bg-amber-200 transition-all disabled:opacity-50"
                  :disabled="accepting"
                  @click="onAcceptAnswer(comment.id)"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                  </svg>
                  {{ accepting ? '采纳中...' : '采纳此回答' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 打赏对话框 (Soft Glass 风格) -->
    <el-dialog
      v-model="tipDialogOpen"
      width="420px"
      :close-on-click-modal="false"
      :show-close="false"
      align-center
      modal-class="bg-slate-900/60 backdrop-blur-sm"
      class="!rounded-[32px] overflow-hidden !border-none"
    >
      <template #header>
        <div class="flex items-center justify-between">
          <div>
            <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">积分打赏</div>
            <div class="text-xl font-bold text-gray-900">打赏作者</div>
          </div>
          <button 
            class="w-8 h-8 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-all active:scale-95"
            @click="tipDialogOpen = false"
          >
            <span class="text-gray-500 text-sm">✕</span>
          </button>
        </div>
      </template>

      <div v-if="post" class="space-y-5">
        <!-- 作者信息卡片 -->
        <div class="bg-gray-50 rounded-2xl p-4 border border-gray-100">
          <div class="flex items-center gap-3">
            <div class="w-12 h-12 rounded-xl bg-slate-900 flex items-center justify-center text-white font-bold text-lg shrink-0">
              {{ (post.nickName || post.userName || '?')[0] }}
            </div>
            <div class="flex-1 min-w-0">
              <div class="font-bold text-gray-900 truncate">{{ post.nickName || post.userName || '作者' }}</div>
              <div v-if="post.companyName" class="text-xs text-gray-400 truncate mt-0.5">{{ post.companyName }}</div>
              <div class="text-xs text-gray-400 mt-0.5">积分将直接转入对方账户</div>
            </div>
          </div>
        </div>
        
        <!-- 积分数量 -->
        <div>
          <label class="block text-sm font-bold text-gray-700 mb-2">打赏积分</label>
          <el-input-number
            v-model="tipForm.points"
            :min="1"
            :max="1000"
            :step="10"
            class="!w-full"
            size="large"
          />
        </div>

        <!-- 留言输入 -->
        <div>
          <label class="block text-sm font-bold text-gray-700 mb-2">打赏留言 <span class="text-gray-400 font-normal">(可选)</span></label>
          <el-input
            v-model="tipForm.remark"
            type="textarea"
            :rows="2"
            placeholder="给作者留言..."
            maxlength="200"
            show-word-limit
            class="!rounded-xl"
          />
        </div>
        
        <!-- 快捷选择 -->
        <div class="pt-4 border-t border-gray-100">
          <div class="text-xs text-gray-400 mb-3">快捷选择</div>
          <div class="flex flex-wrap gap-2">
            <button 
              v-for="amt in [5, 10, 50, 100]" 
              :key="amt"
              class="px-4 py-2 rounded-full text-sm font-medium transition-all active:scale-95"
              :class="tipForm.points === amt 
                ? 'bg-emerald-600 text-white' 
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
              @click="tipForm.points = amt"
            >
              {{ amt }} 积分
            </button>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="flex gap-3">
          <el-button 
            class="flex-1 !rounded-xl !h-11 transition-all active:scale-95" 
            @click="tipDialogOpen = false"
          >
            取消
          </el-button>
          <el-button 
            type="primary" 
            class="flex-1 !rounded-xl !h-11 !bg-emerald-600 hover:!bg-emerald-700 !border-emerald-600 transition-all active:scale-95"
            :loading="tipping"
            @click="submitTip"
          >
            确认打赏 {{ tipForm.points }} 积分
          </el-button>
        </div>
      </template>
    </el-dialog>

    <PublicFooter />
  </div>
</template>

<style scoped>
.prose {
  color: #374151;
  line-height: 1.75;
}
.prose p {
  margin-bottom: 1rem;
}
</style>

