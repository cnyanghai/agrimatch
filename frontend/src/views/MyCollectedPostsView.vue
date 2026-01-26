<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Star, MessageSquare, Heart } from 'lucide-vue-next'
import { listPosts, type PostResponse } from '../api/post'
import { useAuthStore } from '../store/auth'
import ExpertBadge from '../components/post/ExpertBadge.vue'
import PaidBadge from '../components/post/PaidBadge.vue'
import CollectButton from '../components/post/CollectButton.vue'
import { Card, StatusBadge } from '../components/ui'
import PublicFooter from '../components/PublicFooter.vue'
import { getPostPlaceholderCover } from '../assets/placeholders'

const router = useRouter()
const auth = useAuthStore()

const posts = ref<PostResponse[]>([])
const loading = ref(false)

async function loadCollectedPosts() {
  loading.value = true
  try {
    const r = await listPosts({ onlyCollected: true, orderBy: 'create_time', order: 'desc', viewerUserId: auth.me?.userId })
    if (r.code === 0) {
      posts.value = r.data || []
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载收藏列表失败')
  } finally {
    loading.value = false
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

function onAuthorClick(e: Event, userId: number) {
  e.stopPropagation()
  go(`/users/${userId}/posts`)
}

onMounted(() => {
  loadCollectedPosts()
})
</script>

<template>
  <div class="bg-gray-50 min-h-screen">
    <header class="bg-white border-b sticky top-0 z-30 shadow-sm">
      <div class="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
        <button @click="router.back()" class="flex items-center gap-2 text-gray-500 hover:text-gray-900 transition-colors">
          <ArrowLeft :size="20" />
          <span class="font-bold">返回</span>
        </button>
        <h1 class="text-lg font-black text-gray-900 flex items-center gap-2">
          <Star :size="20" class="text-amber-500" fill="currentColor" />
          我的收藏
        </h1>
        <div class="w-20"></div> <!-- Spacer -->
      </div>
    </header>

    <main class="max-w-4xl mx-auto px-4 py-8">
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
            <!-- 作者行 -->
            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center gap-3" @click="onAuthorClick($event, post.userId)">
                <div class="w-8 h-8 rounded-lg bg-brand-600 text-white flex items-center justify-center text-xs font-black shadow-md shadow-brand-600/10">
                  {{ (post.nickName || post.userName || '?')[0] }}
                </div>
                <div>
                  <div class="flex items-center gap-2">
                    <span class="text-sm font-bold text-gray-900 hover:text-brand-600 transition-colors">{{ post.nickName || post.userName }}</span>
                    <ExpertBadge v-if="post.isExpert" />
                  </div>
                  <p class="text-[10px] text-gray-400 font-bold uppercase tracking-wider">{{ formatTime(post.createTime) }}</p>
                </div>
              </div>
              <div class="flex items-center gap-2">
                <PaidBadge v-if="post.isPaid" :price="post.price" />
                <StatusBadge v-if="post.postType === 'paid'" type="success" size="sm">付费文章</StatusBadge>
              </div>
            </div>

            <div class="flex gap-8">
              <div class="flex-1">
                <h4 class="text-xl font-black text-gray-900 mb-2 group-hover:text-brand-600 transition-colors line-clamp-2">
                  {{ post.title }}
                </h4>
                <p class="text-sm text-gray-500 leading-relaxed line-clamp-2 mb-4">
                  {{ post.content }}
                </p>
              </div>
              <div class="w-32 h-24 rounded-xl overflow-hidden shrink-0 bg-gray-100">
                <img :src="getPostCover(post)" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
              </div>
            </div>

            <div class="flex items-center justify-between mt-2 pt-4 border-t border-gray-50">
              <div class="flex items-center gap-6">
                <div class="flex items-center gap-1.5 text-gray-400 text-xs font-bold">
                  <Heart :size="16" :fill="post.likedByMe ? 'currentColor' : 'none'" :class="{'text-red-500': post.likedByMe}" />
                  {{ post.likeCount || 0 }} 赞同
                </div>
                <div class="flex items-center gap-1.5 text-gray-400 text-xs font-bold">
                  <MessageSquare :size="16" />
                  {{ post.commentCount || 0 }} 评论
                </div>
              </div>
              <div class="flex items-center gap-4">
                <CollectButton :post-id="post.id" :initial-collected="true" />
                <button class="text-brand-600 font-black text-xs flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                  继续阅读 <ArrowLeft class="rotate-180" :size="14" />
                </button>
              </div>
            </div>
          </div>
        </Card>

        <!-- 空状态 -->
        <div v-if="!loading && posts.length === 0" class="py-24 text-center">
          <div class="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-6 text-gray-300">
            <Star :size="40" />
          </div>
          <h3 class="text-lg font-bold text-gray-900 mb-2">暂无收藏</h3>
          <p class="text-sm text-gray-500 mb-8">看到感兴趣的内容，点击收藏方便以后查阅</p>
          <button class="bg-brand-600 text-white px-8 py-2.5 rounded-xl font-bold transition-all active:scale-95" @click="go('/talks')">
            去广场逛逛
          </button>
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
</style>

