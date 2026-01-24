<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MessageSquare, ThumbsUp, Share2, Gift, ArrowLeft } from 'lucide-vue-next'
import { 
  getPost, 
  togglePostLike, 
  listPostComments, 
  createPostComment,
  type PostResponse,
  type PostCommentResponse 
} from '../api/post'
import { followUser, unfollowUser, checkFollowStatus } from '../api/follow'
import { giftPoints } from '../api/points'
import { useAuthStore } from '../store/auth'
import { requireAuth } from '../utils/requireAuth'
import PublicFooter from '../components/PublicFooter.vue'
import ExpertBadge from '../components/post/ExpertBadge.vue'
import PaidBadge from '../components/post/PaidBadge.vue'
import CollectButton from '../components/post/CollectButton.vue'
import PaywallOverlay from '../components/post/PaywallOverlay.vue'

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

// 关注状态
const isFollowing = ref(false)
const followLoading = ref(false)

// 付费状态（简化：本地模拟，实际应由后端返回 hasPurchased）
const hasPurchased = ref(false)

const teaserContent = computed(() => {
  if (!post.value?.content) return ''
  const len = post.value.teaserLength || 100
  return post.value.content.substring(0, len) + '...'
})

const showPaywall = computed(() => {
  return post.value?.isPaid && !hasPurchased.value && post.value.userId !== auth.me?.userId
})

// 打赏对话框
const tipDialogOpen = ref(false)
const tipForm = reactive({
  points: 10,
  remark: ''
})
const tipping = ref(false)

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
    } else {
      // 加载关注状态
      loadFollowStatus()
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载话题失败')
    router.back()
  } finally {
    loading.value = false
  }
}

async function loadFollowStatus() {
  if (!post.value?.userId || !auth.token) return
  // 不能关注自己
  if (post.value.userId === auth.me?.userId) return
  
  try {
    const r = await checkFollowStatus(post.value.userId)
    if (r.code === 0) {
      isFollowing.value = !!r.data
    }
  } catch (e) {
    // ignore
  }
}

async function onToggleFollow() {
  if (!post.value?.userId || followLoading.value) return
  
  if (!requireAuth()) return
  
  // 不能关注自己
  if (post.value.userId === auth.me?.userId) {
    ElMessage.warning('不能关注你自己哦')
    return
  }
  
  followLoading.value = true
  try {
    const targetId = post.value.userId
    if (isFollowing.value) {
      const r = await unfollowUser(targetId)
      if (r.code === 0) {
        isFollowing.value = false
        ElMessage.success('已取消关注')
      } else {
        throw new Error(r.message)
      }
    } else {
      const r = await followUser(targetId)
      if (r.code === 0) {
        isFollowing.value = true
        ElMessage.success('关注成功')
      } else {
        throw new Error(r.message)
      }
    }
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    followLoading.value = false
  }
}

async function onPurchase() {
  if (!requireAuth()) return
  
  try {
    // 模拟购买逻辑：直接扣积分（后续可接入真实支付接口）
    await giftPoints(post.value!.userId, post.value!.price || 0, `解锁文章《${post.value!.title}》`)
    hasPurchased.value = true
    ElMessage.success('文章已解锁')
  } catch (e: any) {
    ElMessage.error(e.message || '支付失败')
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

    <main class="max-w-4xl mx-auto px-4 py-8" v-loading="loading">
      <!-- 话题内容 -->
      <div v-if="post" class="bg-white rounded-[32px] p-10 border border-gray-100 shadow-sm mb-8">
        <div class="flex items-center justify-between mb-10">
          <div class="flex items-center gap-4">
            <div class="w-14 h-14 rounded-2xl bg-brand-600 text-white flex items-center justify-center text-xl font-black shadow-lg shadow-brand-600/20">
              {{ (post.nickName || post.userName || '?')[0] }}
            </div>
            <div>
              <div class="flex items-center gap-2 mb-1">
                <span class="font-black text-gray-900 text-lg">{{ post.nickName || post.userName || '匿名用户' }}</span>
                <ExpertBadge v-if="post.isExpert" />
              </div>
              <div class="text-xs text-gray-400 font-bold uppercase tracking-wider">{{ formatTime(post.createTime) }} · {{ post.companyName || '个人作者' }}</div>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <PaidBadge v-if="post.isPaid" :price="post.price" />
            <button 
              v-if="post.userId !== auth.me?.userId"
              class="px-6 py-2 rounded-full border text-xs font-black transition-all active:scale-95 disabled:opacity-50"
              :class="isFollowing 
                ? 'bg-gray-100 border-gray-200 text-gray-500 hover:bg-gray-200' 
                : 'border-brand-200 text-brand-600 hover:bg-brand-50'"
              :disabled="followLoading"
              @click="onToggleFollow"
            >
              {{ isFollowing ? '已关注' : '+ 关注作者' }}
            </button>
          </div>
        </div>

        <h1 class="text-3xl font-black text-gray-900 mb-8 leading-tight">{{ post.title }}</h1>
        
        <div class="prose max-w-none mb-12">
          <!-- 正文区：处理付费逻辑 -->
          <template v-if="showPaywall">
            <p class="text-gray-700 leading-relaxed whitespace-pre-wrap mb-8 opacity-60 italic">{{ teaserContent }}</p>
            <PaywallOverlay :price="post.price" @purchase="onPurchase" />
          </template>
          <template v-else>
            <p class="text-gray-700 leading-relaxed whitespace-pre-wrap text-lg">{{ post.content }}</p>
          </template>
        </div>

        <!-- 互动操作 -->
        <div class="flex items-center justify-between pt-8 border-t border-gray-100">
          <div class="flex items-center gap-8">
            <button
              :class="[
                'flex items-center gap-2 px-6 py-2.5 rounded-full text-sm font-black transition-all active:scale-95',
                post.likedByMe 
                  ? 'bg-red-50 text-red-600' 
                  : 'bg-gray-50 text-gray-500 hover:bg-gray-100'
              ]"
              :disabled="liking"
              @click="onToggleLike"
            >
              <ThumbsUp :size="18" />
              {{ post.likeCount ?? 0 }} 赞同
            </button>
            
            <button class="flex items-center gap-2 text-gray-400 hover:text-brand-600 transition-colors text-sm font-bold">
              <MessageSquare :size="18" />
              {{ post.commentCount ?? 0 }} 条评论
            </button>

            <CollectButton :post-id="post.id" :initial-status="post.collectedByMe" />
          </div>

          <div class="flex items-center gap-4">
            <button class="p-2 text-gray-400 hover:text-brand-600 transition-colors" title="分享">
              <Share2 :size="20" />
            </button>
            <button
              class="flex items-center gap-2 px-6 py-2.5 bg-amber-50 text-amber-600 rounded-full text-sm font-black hover:bg-amber-100 transition-all active:scale-95"
              @click="openTipDialog"
            >
              <Gift :size="18" />
              打赏
            </button>
          </div>
        </div>
      </div>

      <!-- 评论区域 -->
      <div class="bg-white rounded-[32px] p-10 border border-gray-100 shadow-sm mb-8">
        <h2 class="text-xl font-black text-gray-900 mb-8">互动讨论 ({{ comments.length }})</h2>
        
        <!-- 发表评论 -->
        <div class="mb-8 pb-8 border-b border-gray-200">
          <textarea
            v-model="commentText"
            placeholder="写下你的评论..."
            class="w-full h-24 p-4 border border-gray-200 rounded-lg resize-none focus:ring-2 focus:ring-brand-500 focus:border-brand-500 outline-none"
            :disabled="commenting"
          ></textarea>
          <div class="flex justify-end mt-4">
            <button
              class="px-6 py-2 bg-brand-600 text-white rounded-xl text-sm font-bold hover:bg-brand-700 transition-all disabled:opacity-50"
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
          >
            <div 
              class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0 bg-brand-100 text-brand-600"
            >
              {{ (comment.nickName || comment.userName || '?')[0] }}
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-2">
                <span class="font-bold text-gray-900">{{ comment.nickName || comment.userName || '匿名用户' }}</span>
                <span class="text-xs text-gray-400">{{ formatTime(comment.createTime) }}</span>
                <span v-if="comment.companyName" class="text-xs text-gray-400">· {{ comment.companyName }}</span>
              </div>
              <p class="text-gray-700 leading-relaxed whitespace-pre-wrap">{{ comment.content }}</p>
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
            class="w-8 h-8 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-all "
            @click="tipDialogOpen = false"
          >
            <span class="text-gray-500 text-sm">✕</span>
          </button>
        </div>
      </template>

      <div v-if="post" class="space-y-5">
        <!-- 作者信息卡片 -->
        <div class="bg-gray-50 rounded-xl p-4 border border-gray-200">
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
        <div class="pt-4 border-t border-gray-200">
          <div class="text-xs text-gray-400 mb-3">快捷选择</div>
          <div class="flex flex-wrap gap-2">
            <button 
              v-for="amt in [5, 10, 50, 100]" 
              :key="amt"
              class="px-4 py-2 rounded-full text-sm font-medium transition-all "
              :class="tipForm.points === amt 
                ? 'bg-brand-600 text-white' 
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
            class="flex-1 !rounded-xl !h-11 transition-all " 
            @click="tipDialogOpen = false"
          >
            取消
          </el-button>
          <el-button 
            type="primary" 
            class="flex-1 !rounded-xl !h-11 !bg-brand-600 hover:!bg-brand-700 !border-brand-600 transition-all "
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

