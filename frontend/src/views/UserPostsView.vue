<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Clock, MessageSquare, Heart, Share2, Users } from 'lucide-vue-next'
import { listPosts, type PostResponse } from '../api/post'
import { getUser, type UserResponse } from '../api/user'
import { followUser, unfollowUser, checkFollowStatus } from '../api/follow'
import { useAuthStore } from '../store/auth'
import { requireAuth } from '../utils/requireAuth'
import ExpertBadge from '../components/post/ExpertBadge.vue'
import PaidBadge from '../components/post/PaidBadge.vue'
import CollectButton from '../components/post/CollectButton.vue'
import { Card, StatusBadge } from '../components/ui'
import PublicFooter from '../components/PublicFooter.vue'
import { getPostPlaceholderCover } from '../assets/placeholders'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const userId = computed(() => Number(route.params.id))
const user = ref<UserResponse | null>(null)
const posts = ref<PostResponse[]>([])
const loading = ref(false)
const isFollowing = ref(false)
const followLoading = ref(false)

async function loadUser() {
  try {
    const r = await getUser(userId.value)
    if (r.code === 0) {
      user.value = r.data || null
    }
  } catch (e) {
    console.error('加载用户信息失败', e)
  }
}

async function loadFollowStatus() {
  if (!auth.token || userId.value === auth.me?.userId) return
  try {
    const r = await checkFollowStatus(userId.value)
    if (r.code === 0) {
      isFollowing.value = !!r.data
    }
  } catch (e) {
    // ignore
  }
}

async function loadPosts() {
  loading.value = true
  try {
    const r = await listPosts({ userId: userId.value, orderBy: 'create_time', order: 'desc' })
    if (r.code === 0) {
      posts.value = r.data || []
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载帖子失败')
  } finally {
    loading.value = false
  }
}

async function onToggleFollow() {
  if (followLoading.value) return
  if (!requireAuth()) return
  
  followLoading.value = true
  try {
    if (isFollowing.value) {
      const r = await unfollowUser(userId.value)
      if (r.code === 0) {
        isFollowing.value = false
        ElMessage.success('已取消关注')
      }
    } else {
      const r = await followUser(userId.value)
      if (r.code === 0) {
        isFollowing.value = true
        ElMessage.success('关注成功')
      }
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    followLoading.value = false
  }
}

function formatTime(timeStr: string | undefined) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

// 获取帖子封面图，没有图片时返回本地占位图
function getPostCover(post: PostResponse): string {
  if (post.imagesJson) {
    try {
      const imgs = JSON.parse(post.imagesJson)
      if (Array.isArray(imgs) && imgs.length > 0) return imgs[0]
    } catch (e) { /* ignore */ }
  }
  return getPostPlaceholderCover(post.id)
}

function go(path: string) {
  router.push(path)
}

async function handleShare() {
  const url = window.location.href
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success('链接已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动复制链接')
  }
}

onMounted(() => {
  loadUser()
  loadFollowStatus()
  loadPosts()
})
</script>

<template>
  <div class="bg-gray-50 min-h-screen">
    <header class="bg-white border-b sticky top-0 z-30 shadow-sm">
      <div class="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
        <button @click="router.back()" class="flex items-center gap-2 text-gray-500 hover:text-gray-900 transition-colors">
          <ArrowLeft :size="20" />
          <span class="font-bold">返回话题广场</span>
        </button>
        <h1 class="text-lg font-black text-gray-900">用户主页</h1>
        <div class="w-20"></div> <!-- Spacer -->
      </div>
    </header>

    <main class="max-w-4xl mx-auto px-4 py-8">
      <!-- 用户资料卡片 -->
      <Card radius="2xl" class="mb-8 border-none shadow-sm ring-1 ring-gray-100">
        <div class="flex flex-col md:flex-row items-center md:items-start gap-8">
          <div class="w-24 h-24 rounded-3xl bg-brand-600 text-white flex items-center justify-center text-4xl font-black shadow-xl shadow-brand-600/20">
            {{ (user?.nickName || user?.userName || '?')[0] }}
          </div>
          <div class="flex-1 text-center md:text-left">
            <div class="flex flex-col md:flex-row md:items-center gap-4 mb-4">
              <h2 class="text-3xl font-black text-gray-900">{{ user?.nickName || user?.userName || '加载中...' }}</h2>
              <ExpertBadge v-if="user?.position?.includes('专家')" />
              <div class="flex-1"></div>
              <div class="flex items-center gap-2">
                <button
                  class="p-2.5 rounded-lg border border-gray-200 text-gray-400 hover:text-brand-600 hover:border-brand-200 transition-all"
                  title="分享"
                  @click="handleShare"
                >
                  <Share2 :size="18" />
                </button>
                <button
                  v-if="user && user.userId !== auth.me?.userId"
                  class="px-8 py-2.5 rounded-lg font-black text-sm transition-all active:scale-95 shadow-lg shadow-brand-600/10"
                  :class="isFollowing
                    ? 'bg-gray-100 text-gray-500 hover:bg-gray-200'
                    : 'bg-brand-600 text-white hover:bg-brand-700'"
                  :disabled="followLoading"
                  @click="onToggleFollow"
                >
                  {{ isFollowing ? '已关注' : '+ 关注' }}
                </button>
              </div>
            </div>
            <p class="text-gray-500 font-medium mb-4">
              {{ user?.position || '行业同仁' }} · {{ user?.companyName || '个人作者' }}
            </p>
            <div class="flex items-center justify-center md:justify-start gap-6 text-sm">
              <div class="flex flex-col">
                <span class="text-gray-400 font-bold uppercase text-[10px] tracking-widest mb-1">文章发布</span>
                <span class="text-xl font-black text-gray-900">{{ posts.length }}</span>
              </div>
              <div class="w-px h-8 bg-gray-100"></div>
              <div class="flex flex-col text-gray-400">
                <span class="font-bold uppercase text-[10px] tracking-widest mb-1">活跃于</span>
                <span class="text-sm font-bold text-gray-900">{{ formatTime(user?.createTime) }} 加入平台</span>
              </div>
            </div>
          </div>
        </div>
      </Card>

      <!-- 帖子列表 -->
      <div class="space-y-6">
        <h3 class="text-xl font-black text-gray-900 px-2 flex items-center gap-2">
          <div class="w-1.5 h-5 bg-brand-600 rounded-full"></div>
          发布的帖子
        </h3>

        <div v-loading="loading" class="space-y-4">
          <Card 
            v-for="post in posts" 
            :key="post.id"
            padding="none"
            radius="2xl"
            class="group transition-all hover:shadow-xl hover:shadow-brand-900/5 cursor-pointer border-none ring-1 ring-gray-100"
            @click="go(`/talks/${post.id}`)"
          >
            <div class="p-6">
              <div class="flex items-center justify-between mb-4">
                <div class="flex items-center gap-2">
                  <span class="text-xs text-gray-400 font-bold uppercase tracking-wider">{{ formatTime(post.createTime) }}</span>
                  <StatusBadge v-if="post.postType === 'paid'" type="success" size="sm">付费文章</StatusBadge>
                  <StatusBadge v-if="post.postType === 'poll'" type="warning" size="sm">投票</StatusBadge>
                </div>
                <PaidBadge v-if="post.isPaid" :price="post.price" />
              </div>

              <div class="flex gap-8">
                <div class="flex-1">
                  <h4 class="text-xl font-black text-gray-900 mb-3 group-hover:text-brand-600 transition-colors line-clamp-2">
                    {{ post.title }}
                  </h4>
                  <p class="text-gray-500 leading-relaxed line-clamp-3 mb-4">
                    {{ post.content }}
                  </p>
                </div>
                <div class="w-40 h-28 rounded-2xl overflow-hidden shrink-0 bg-gray-100">
                  <img :src="getPostCover(post)" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
                </div>
              </div>

              <div class="flex items-center justify-between mt-4 pt-6 border-t border-gray-50">
                <div class="flex items-center gap-8">
                  <div class="flex items-center gap-2 text-gray-400 text-sm font-bold">
                    <Heart :size="18" :fill="post.likedByMe ? 'currentColor' : 'none'" :class="{'text-red-500': post.likedByMe}" />
                    {{ post.likeCount || 0 }} 赞同
                  </div>
                  <div class="flex items-center gap-2 text-gray-400 text-sm font-bold">
                    <MessageSquare :size="18" />
                    {{ post.commentCount || 0 }} 评论
                  </div>
                </div>
                <div class="flex items-center gap-4">
                  <CollectButton :post-id="post.id" :initial-collected="post.collectedByMe || false" />
                  <button class="text-brand-600 font-black text-sm flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                    阅读全文 <ArrowLeft class="rotate-180" :size="16" />
                  </button>
                </div>
              </div>
            </div>
          </Card>

          <!-- 空状态 -->
          <div v-if="!loading && posts.length === 0" class="py-24 text-center">
            <div class="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-6 text-gray-300">
              <MessageSquare :size="40" />
            </div>
            <h3 class="text-lg font-bold text-gray-900 mb-2">该用户很懒，还没发布过帖子</h3>
          </div>
        </div>
      </div>
    </main>

    <PublicFooter />
  </div>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>

