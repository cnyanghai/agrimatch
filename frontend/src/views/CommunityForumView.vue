<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Heart, MessageCircle, Gift, Trash2, Send, X, RefreshCw } from 'lucide-vue-next'
import { createPost, createPostComment, deletePost, listPostComments, listPosts, togglePostLike, type PostCommentResponse, type PostResponse } from '../api/post'
import { useAuthStore } from '../store/auth'
import { getMyCompany, type CompanyResponse } from '../api/company'
import { giftPoints } from '../api/points'
import { requireAuth } from '../utils/requireAuth'
import { BaseButton, BaseModal, EmptyState, Skeleton } from '../components/ui'

const auth = useAuthStore()
const company = ref<CompanyResponse | null>(null)

const creating = ref(false)
const loading = ref(false)
const list = ref<PostResponse[]>([])
const liking = ref<Record<number, boolean>>({})
const commentDialogOpen = ref(false)
const currentPostId = ref<number | null>(null)
const comments = ref<PostCommentResponse[]>([])
const commentText = ref('')
const commentsLoading = ref(false)

// 打赏对话框
const tipDialogOpen = ref(false)
const currentTipPost = ref<PostResponse | null>(null)
const tipForm = reactive({ points: 10, remark: '' })
const tipping = ref(false)

const form = reactive({ title: '', content: '', domain: 'general' })
const q = reactive({ keyword: '', domain: '' })

const domains = [
  { key: 'general', name: '综合讨论' },
  { key: 'biological', name: '生物种苗' },
  { key: 'processing', name: '农业加工' },
  { key: 'material', name: '原料辅料' },
  { key: 'equipment', name: '装备物流' }
]

const canCreate = computed(() => form.title.trim().length > 0 && !!auth.token)
const isLoggedIn = computed(() => !!auth.token)

async function refresh() {
  loading.value = true
  try {
    const r = await listPosts({ 
      keyword: q.keyword || undefined, 
      domain: q.domain || undefined,
      orderBy: 'create_time', 
      order: 'desc' 
    })
    if (r.code !== 0) throw new Error(r.message)
    list.value = r.data ?? []
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

async function onToggleLike(row: PostResponse) {
  if (liking.value[row.id]) return
  if (!requireAuth('/posts')) { ElMessage.warning('请先登录后再点赞'); return }
  liking.value = { ...liking.value, [row.id]: true }
  try {
    const r = await togglePostLike(row.id)
    if (r.code !== 0) throw new Error(r.message)
    row.likedByMe = r.data?.liked ?? row.likedByMe
    row.likeCount = r.data?.likeCount ?? row.likeCount
  } catch (e: any) {
    ElMessage.error(e?.message ?? '点赞失败')
  } finally {
    liking.value = { ...liking.value, [row.id]: false }
  }
}

async function openComments(row: PostResponse) {
  currentPostId.value = row.id
  commentDialogOpen.value = true
  await refreshComments()
}

async function refreshComments() {
  if (!currentPostId.value) return
  commentsLoading.value = true
  try {
    const r = await listPostComments(currentPostId.value)
    if (r.code !== 0) throw new Error(r.message)
    comments.value = r.data ?? []
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载评论失败')
  } finally {
    commentsLoading.value = false
  }
}

async function onAddComment() {
  const pid = currentPostId.value
  if (!pid) return
  const content = commentText.value.trim()
  if (!content) return
  if (!requireAuth('/posts')) { ElMessage.warning('请先登录后再评论'); return }
  try {
    const r = await createPostComment(pid, content)
    if (r.code !== 0) throw new Error(r.message)
    commentText.value = ''
    await refreshComments()
    await refresh()
    ElMessage.success('评论成功')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '评论失败')
  }
}

async function onCreate() {
  if (!canCreate.value) return
  if (!requireAuth('/posts')) { ElMessage.warning('请先登录后再发布话题'); return }
  creating.value = true
  try {
    const r = await createPost({ 
      title: form.title.trim(), 
      content: form.content?.trim() || undefined,
      domain: form.domain
    })
    if (r.code !== 0) throw new Error(r.message)
    ElMessage.success('发布成功')
    form.title = ''
    form.content = ''
    form.domain = 'general'
    await refresh()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '发布失败')
  } finally {
    creating.value = false
  }
}

async function onDelete(id: number) {
  loading.value = true
  try {
    const r = await deletePost(id)
    if (r.code !== 0) throw new Error(r.message)
    ElMessage.success('已删除')
    await refresh()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '删除失败')
  } finally {
    loading.value = false
  }
}

function openTipDialog(row: PostResponse) {
  if (!row.userId) { ElMessage.warning('无法获取作者信息'); return }
  if (row.userId === auth.me?.userId) { ElMessage.info('不能打赏自己的帖子哦'); return }
  currentTipPost.value = row
  tipForm.points = 10
  tipForm.remark = ''
  tipDialogOpen.value = true
}

async function submitTip() {
  if (!currentTipPost.value?.userId) { ElMessage.warning('无法获取作者信息'); return }
  if (tipForm.points < 1) { ElMessage.warning('打赏积分数量至少为1'); return }
  tipping.value = true
  try {
    const authorName = currentTipPost.value.nickName || currentTipPost.value.userName || '作者'
    await giftPoints(currentTipPost.value.userId, tipForm.points, tipForm.remark || `打赏帖子《${currentTipPost.value.title}》`)
    ElMessage.success(`已成功打赏 ${tipForm.points} 积分给 ${authorName}`)
    tipDialogOpen.value = false
  } catch (e: any) {
    ElMessage.error(e?.message || '打赏失败，请稍后重试')
  } finally {
    tipping.value = false
  }
}

function formatTime(s?: string) {
  if (!s) return ''
  const d = new Date(s)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  return d.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  refresh()
  ;(async () => {
    try {
      const r = await getMyCompany()
      if (r.code === 0) company.value = r.data ?? null
    } catch { /* ignore */ }
  })()
})
</script>

<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">社区论坛</h1>
        <p class="text-sm text-gray-500 mt-1">发布观点、点赞评论、与供需交流</p>
      </div>
      <BaseButton type="secondary" size="sm" :loading="loading" @click="refresh">
        <RefreshCw class="w-4 h-4" />
        刷新
      </BaseButton>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 发布帖子 -->
      <div class="bg-white rounded-xl border border-gray-200 p-6">
        <h3 class="text-2xl font-bold text-gray-900 mb-4">发布话题</h3>
        
        <div v-if="!isLoggedIn" class="py-8">
          <EmptyState
            type="default"
            title="请先登录"
            description="登录后即可发布话题"
            action-text="去登录"
            size="sm"
            @action="requireAuth('/posts')"
          />
        </div>
        
        <div v-else class="space-y-4">
          <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
            <div class="w-10 h-10 rounded-xl bg-brand-500 text-white flex items-center justify-center font-bold overflow-hidden">
              <img v-if="auth.me?.avatar" :src="auth.me.avatar" alt="头像" class="w-full h-full object-cover" />
              <span v-else>{{ (auth.me?.nickName || auth.me?.userName || 'U')[0] }}</span>
            </div>
            <div class="flex-1 min-w-0">
              <div class="font-bold text-gray-900 truncate">{{ auth.me?.nickName || auth.me?.userName || '-' }}</div>
              <div class="text-xs text-gray-500 truncate">{{ company?.companyName ?? '未绑定公司' }}</div>
            </div>
          </div>

          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">所属板块</label>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="d in domains"
                :key="d.key"
                :class="[
                  'px-3 py-1.5 rounded-lg text-xs font-bold transition-all border',
                  form.domain === d.key
                    ? 'bg-brand-600 text-white border-brand-600'
                    : 'bg-gray-50 text-gray-600 border-gray-200 hover:border-brand-500'
                ]"
                @click="form.domain = d.key"
              >
                {{ d.name }}
              </button>
            </div>
          </div>

          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
              话题标题 <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.title"
              type="text"
              maxlength="120"
              placeholder="请输入话题标题"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
            />
          </div>

          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">话题内容</label>
            <textarea
              v-model="form.content"
              rows="4"
              maxlength="20000"
              placeholder="分享你的观点..."
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all resize-none"
            ></textarea>
          </div>

          <BaseButton type="primary" :disabled="!canCreate" :loading="creating" class="w-full" @click="onCreate">
            <Send class="w-4 h-4" />
            发布话题
          </BaseButton>
        </div>
      </div>

      <!-- 帖子列表 -->
      <div class="lg:col-span-2 bg-white rounded-xl border border-gray-200 overflow-hidden text-sm">
        <div class="p-4 border-b border-gray-200 space-y-4">
          <div class="flex items-center justify-between gap-4">
            <h3 class="text-2xl font-bold text-gray-900">话题列表</h3>
            <div class="relative flex-1 max-w-[300px]">
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                v-model="q.keyword"
                type="text"
                placeholder="搜索话题..."
                class="w-full pl-10 pr-4 py-2 border-2 border-gray-200 rounded-xl text-sm focus:border-brand-500 outline-none transition-all"
                @keyup.enter="refresh"
              />
            </div>
          </div>

          <!-- 板块过滤 -->
          <div class="flex items-center gap-3">
            <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">板块:</span>
            <div class="flex flex-wrap gap-2">
              <button
                class="px-3 py-1 rounded-full text-xs font-bold transition-all border"
                :class="!q.domain ? 'bg-brand-600 text-white border-brand-600' : 'bg-white text-gray-500 border-gray-200 hover:border-brand-500'"
                @click="q.domain = ''; refresh()"
              >
                全部
              </button>
              <button
                v-for="d in domains"
                :key="d.key"
                class="px-3 py-1 rounded-full text-xs font-bold transition-all border"
                :class="q.domain === d.key ? 'bg-brand-600 text-white border-brand-600' : 'bg-white text-gray-500 border-gray-200 hover:border-brand-500'"
                @click="q.domain = d.key; refresh()"
              >
                {{ d.name }}
              </button>
            </div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading && list.length === 0" class="p-6 space-y-4">
          <Skeleton type="card" />
          <Skeleton type="card" />
        </div>

        <!-- 空状态 -->
        <EmptyState
          v-else-if="list.length === 0"
          type="message"
          title="暂无话题"
          description="成为第一个发布话题的人吧"
          size="md"
        />

        <!-- 帖子卡片列表 -->
        <div v-else class="divide-y divide-gray-50">
          <div
            v-for="(post, index) in list"
            :key="post.id"
            class="p-5 hover:bg-gray-50/50 transition-all animate-stagger-in"
            :style="{ animationDelay: `${index * 30}ms` }"
          >
            <div class="flex items-start gap-4">
              <!-- 头像 -->
              <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-blue-600 text-white flex items-center justify-center font-bold flex-shrink-0 overflow-hidden">
                <img v-if="post.avatar" :src="post.avatar" alt="头像" class="w-full h-full object-cover" />
                <span v-else>{{ (post.nickName || post.userName || '?')[0] }}</span>
              </div>

              <div class="flex-1 min-w-0">
                <!-- 作者信息 -->
                <div class="flex items-center gap-2 mb-1">
                  <span class="font-bold text-gray-900">{{ post.nickName || post.userName || '匿名' }}</span>
                  <span v-if="post.domain && post.domain !== 'general'" class="bg-blue-50 text-blue-600 text-[10px] px-1.5 py-0.5 rounded font-bold">
                    {{ domains.find(d => d.key === post.domain)?.name || post.domain }}
                  </span>
                  <span v-if="post.companyName" class="text-xs text-gray-400">· {{ post.companyName }}</span>
                  <span class="text-xs text-gray-400 ml-auto">{{ formatTime(post.createTime) }}</span>
                </div>

                <!-- 标题 -->
                <h4 class="font-bold text-gray-900 mb-2 line-clamp-2">{{ post.title }}</h4>

                <!-- 内容预览 -->
                <p v-if="post.content" class="text-sm text-gray-600 line-clamp-2 mb-3">{{ post.content }}</p>

                <!-- 互动按钮 -->
                <div class="flex items-center gap-3">
                  <button
                    :class="[
                      'flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-bold transition-all',
                      post.likedByMe 
                        ? 'bg-red-50 text-red-500' 
                        : 'bg-gray-100 text-gray-600 hover:bg-red-50 hover:text-red-500'
                    ]"
                    :disabled="!!liking[post.id]"
                    @click="onToggleLike(post)"
                  >
                    <Heart :class="['w-4 h-4', post.likedByMe ? 'fill-current' : '']" />
                    {{ post.likeCount ?? 0 }}
                  </button>

                  <button
                    class="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-bold bg-gray-100 text-gray-600 hover:bg-blue-50 hover:text-blue-500 transition-all"
                    @click="openComments(post)"
                  >
                    <MessageCircle class="w-4 h-4" />
                    {{ post.commentCount ?? 0 }}
                  </button>

                  <button
                    class="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-bold bg-gray-100 text-gray-600 hover:bg-amber-50 hover:text-amber-500 transition-all"
                    @click="openTipDialog(post)"
                  >
                    <Gift class="w-4 h-4" />
                    打赏
                  </button>

                  <button
                    v-if="post.userId === auth.me?.userId"
                    class="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-bold bg-gray-100 text-gray-600 hover:bg-red-50 hover:text-red-500 transition-all ml-auto"
                    @click="onDelete(post.id)"
                  >
                    <Trash2 class="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 评论对话框 -->
    <BaseModal v-model="commentDialogOpen" title="评论" size="md">
      <div class="space-y-4">
        <!-- 评论输入 -->
        <div class="flex gap-3">
          <textarea
            v-model="commentText"
            rows="2"
            maxlength="1000"
            placeholder="输入评论..."
            class="flex-1 px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all resize-none"
          ></textarea>
          <BaseButton type="primary" :disabled="!commentText.trim()" @click="onAddComment">
            <Send class="w-4 h-4" />
          </BaseButton>
        </div>

        <div class="border-t border-gray-200 pt-4">
          <!-- 加载状态 -->
          <div v-if="commentsLoading" class="space-y-3">
            <Skeleton type="text" />
            <Skeleton type="text" />
          </div>

          <!-- 空状态 -->
          <EmptyState
            v-else-if="comments.length === 0"
            type="message"
            title="暂无评论"
            description="成为第一个评论的人吧"
            size="sm"
          />

          <!-- 评论列表 -->
          <div v-else class="space-y-4 max-h-[400px] overflow-y-auto">
            <div
              v-for="c in comments"
              :key="c.id"
              class="flex gap-3"
            >
              <div class="w-8 h-8 rounded-lg bg-gray-100 text-gray-600 flex items-center justify-center text-xs font-bold flex-shrink-0">
                {{ (c.nickName || c.userName || '?')[0] }}
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <span class="font-bold text-gray-900 text-sm">{{ c.nickName || c.userName || '匿名' }}</span>
                  <span v-if="c.companyName" class="text-xs text-brand-600 bg-brand-50 px-2 py-0.5 rounded-full">{{ c.companyName }}</span>
                  <span class="text-xs text-gray-400 ml-auto">{{ formatTime(c.createTime) }}</span>
                </div>
                <p class="text-sm text-gray-700">{{ c.content }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </BaseModal>

    <!-- 打赏对话框 -->
    <BaseModal v-model="tipDialogOpen" title="积分打赏" size="sm">
      <div class="space-y-5">
        <!-- 帖子信息卡片 -->
        <div class="bg-gray-50 rounded-xl p-4 border border-gray-200">
          <div class="font-bold text-gray-900 line-clamp-2 mb-3">{{ currentTipPost?.title }}</div>
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-slate-700 to-slate-900 flex items-center justify-center text-white font-bold flex-shrink-0 overflow-hidden">
              <img v-if="currentTipPost?.avatar" :src="currentTipPost.avatar" alt="头像" class="w-full h-full object-cover" />
              <span v-else>{{ (currentTipPost?.nickName || currentTipPost?.userName || '?')[0] }}</span>
            </div>
            <div class="flex-1 min-w-0">
              <div class="font-bold text-gray-700 truncate">{{ currentTipPost?.nickName || currentTipPost?.userName || '作者' }}</div>
              <div v-if="currentTipPost?.companyName" class="text-xs text-gray-400 truncate">{{ currentTipPost?.companyName }}</div>
            </div>
          </div>
        </div>
        
        <!-- 积分数量 -->
        <div>
          <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">打赏积分</label>
          <input
            v-model.number="tipForm.points"
            type="number"
            min="1"
            max="10000"
            class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all text-center text-lg font-bold"
          />
        </div>

        <!-- 快捷选择 -->
        <div class="flex flex-wrap gap-2">
          <button 
            v-for="amt in [5, 10, 50, 100]" 
            :key="amt"
            :class="[
              'px-4 py-2 rounded-full text-sm font-bold transition-all ',
              tipForm.points === amt 
                ? 'bg-brand-600 text-white' 
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            ]"
            @click="tipForm.points = amt"
          >
            {{ amt }} 积分
          </button>
        </div>

        <!-- 留言输入 -->
        <div>
          <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
            打赏留言 <span class="text-gray-400 font-normal">(可选)</span>
          </label>
          <input
            v-model="tipForm.remark"
            type="text"
            placeholder="写得好！支持一下..."
            maxlength="100"
            class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
          />
        </div>
      </div>

      <template #footer>
        <BaseButton type="secondary" @click="tipDialogOpen = false">
          <X class="w-4 h-4" />
          取消
        </BaseButton>
        <BaseButton type="primary" :loading="tipping" @click="submitTip">
          <Gift class="w-4 h-4" />
          确认打赏 {{ tipForm.points }} 积分
        </BaseButton>
      </template>
    </BaseModal>
  </div>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
