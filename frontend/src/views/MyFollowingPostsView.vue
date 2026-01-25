<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Users, Heart, MessageSquare, Clock } from 'lucide-vue-next'
import { getFollowedPosts } from '../api/follow'
import { listCollectedPostIds, type PostResponse } from '../api/post'
import ExpertBadge from '../components/post/ExpertBadge.vue'
import PaidBadge from '../components/post/PaidBadge.vue'
import CollectButton from '../components/post/CollectButton.vue'
import { Card } from '../components/ui'
import PublicFooter from '../components/PublicFooter.vue'

const router = useRouter()

const posts = ref<PostResponse[]>([])
const loading = ref(false)
const collectedPostIds = ref<number[]>([])

async function loadFollowedPosts() {
  loading.value = true
  try {
    const r = await getFollowedPosts()
    if (r.code === 0) {
      posts.value = r.data || []
    } else {
      throw new Error(r.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载关注动态失败')
  } finally {
    loading.value = false
  }
}

async function loadCollectedIds() {
  try {
    const r = await listCollectedPostIds()
    if (r.code === 0) collectedPostIds.value = r.data || []
  } catch (e) {
    // ignore
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
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

function go(path: string) {
  router.push(path)
}

function onAuthorClick(e: Event, userId: number) {
  e.stopPropagation()
  go(`/users/${userId}/posts`)
}

onMounted(() => {
  loadFollowedPosts()
  loadCollectedIds()
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
          <Users :size="20" class="text-brand-600" />
          关注动态
        </h1>
        <div class="w-20"></div> <!-- Spacer -->
      </div>
    </header>

    <main class="max-w-4xl mx-auto px-4 py-8">
      <div v-loading="loading">
        <div v-if="posts.length > 0" class="space-y-4">
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
                <div class="flex items-center gap-3 cursor-pointer group/author" @click="onAuthorClick($event, post.userId)">
                  <div class="w-10 h-10 rounded-xl bg-brand-600 text-white flex items-center justify-center text-sm font-black shadow-lg shadow-brand-600/20 group-hover/author:scale-105 transition-transform">
                    {{ (post.nickName || post.userName || '?')[0] }}
                  </div>
                  <div>
                    <div class="flex items-center gap-2">
                      <span class="font-bold text-gray-900 group-hover/author:text-brand-600 transition-colors">{{ post.nickName || post.userName }}</span>
                      <ExpertBadge v-if="post.isExpert" />
                    </div>
                    <div class="flex items-center gap-2 text-xs text-gray-400">
                      <Clock :size="12" />
                      {{ formatTime(post.createTime) }}
                    </div>
                  </div>
                </div>

                <PaidBadge v-if="post.isPaid" :price="post.price" />
              </div>

              <!-- 标题与内容 -->
              <h3 class="text-lg font-black text-gray-900 mb-2 group-hover:text-brand-600 transition-colors">
                {{ post.title }}
              </h3>
              <p class="text-sm text-gray-500 leading-relaxed line-clamp-3 mb-4">
                {{ post.content }}
              </p>

              <!-- 交互行 -->
              <div class="flex items-center gap-6 pt-4 border-t border-gray-50">
                <button class="flex items-center gap-1.5 text-xs font-bold transition-all" :class="post.likedByMe ? 'text-red-500' : 'text-gray-400 hover:text-red-500'">
                  <Heart :size="16" :fill="post.likedByMe ? 'currentColor' : 'none'" />
                  {{ post.likeCount ?? 0 }}
                </button>
                <button class="flex items-center gap-1.5 text-gray-400 hover:text-brand-600 text-xs font-bold transition-all">
                  <MessageSquare :size="16" />
                  {{ post.commentCount ?? 0 }}
                </button>
                <CollectButton :post-id="post.id" :initial-status="collectedPostIds.includes(post.id)" size="sm" />
              </div>
            </div>
          </Card>
        </div>

        <!-- 空状态 -->
        <div v-else-if="!loading" class="py-24 text-center">
          <div class="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-6 text-gray-300">
            <Users :size="48" />
          </div>
          <h3 class="text-xl font-black text-gray-900 mb-2">暂无关注动态</h3>
          <p class="text-sm text-gray-500 mb-8 max-w-xs mx-auto">关注你感兴趣的用户，这里会显示他们发布的最新话题</p>
          <button class="bg-brand-600 text-white px-8 py-3 rounded-xl font-bold transition-all active:scale-95 shadow-lg shadow-brand-600/20" @click="go('/talks')">
            去发现感兴趣的人
          </button>
        </div>
      </div>
    </main>

    <PublicFooter />
  </div>
</template>
