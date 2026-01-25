<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { requireAuth } from '../utils/requireAuth'
import { listPosts, listCollectedPostIds, type PostResponse } from '../api/post'
import { getFollowedPosts, getFollowedUsers, type FollowedUser } from '../api/follow'
import { useAuthStore } from '../store/auth'
import { useUiStore } from '../store/ui'
import PublicFooter from '../components/PublicFooter.vue'
import { MessageSquare, Heart, Search, ChevronDown, Plus, Star, Gift, Coins, CheckCircle, Flame, Users, Clock, ArrowRight, UserPlus } from 'lucide-vue-next'
import ExpertBadge from '../components/post/ExpertBadge.vue'
import PaidBadge from '../components/post/PaidBadge.vue'
import CollectButton from '../components/post/CollectButton.vue'
import { Card, StatusBadge } from '../components/ui'

const router = useRouter()
const auth = useAuthStore()
const ui = useUiStore()

// 用户信息
const isLoggedIn = computed(() => !!auth.me)
const displayName = computed(() => auth.me?.nickName || auth.me?.userName || '游客')

const loading = ref(false)
const posts = ref<PostResponse[]>([])
const keyword = ref('')
const orderBy = ref('create_time')
const order = ref<'asc' | 'desc'>('desc')
const activeTab = ref<'all' | 'hot' | 'following'>('all')
const collectedPostIds = ref<number[]>([])

const followedUsers = ref<FollowedUser[]>([])
const collectedPosts = ref<PostResponse[]>([])

function go(path: string) {
  router.push(path)
}

function onPublishTalk() {
  if (!requireAuth('/talks/publish')) return
  go('/talks/publish')
}

function onEnterMy() {
  if (!requireAuth('/console')) return
  go('/console')
}

async function loadCollectedIds() {
  if (!isLoggedIn.value) return
  try {
    const r = await listCollectedPostIds()
    if (r.code === 0) collectedPostIds.value = r.data || []
  } catch (e) {
    // ignore
  }
}

async function loadFollowedUsers() {
  if (!isLoggedIn.value) return
  try {
    const r = await getFollowedUsers()
    if (r.code === 0) followedUsers.value = (r.data || []).slice(0, 5)
  } catch (e) {
    // ignore
  }
}

async function loadCollectedPosts() {
  if (!isLoggedIn.value) return
  try {
    const r = await listPosts({ onlyCollected: true, limit: 5, viewerUserId: auth.me?.userId })
    if (r.code === 0) collectedPosts.value = r.data || []
  } catch (e) {
    // ignore
  }
}

async function loadPosts() {
  loading.value = true
  try {
    let r
    if (activeTab.value === 'following') {
      if (!requireAuth('/talks')) return
      r = await getFollowedPosts()
    } else {
      r = await listPosts({ 
        keyword: keyword.value.trim() || undefined, 
        orderBy: activeTab.value === 'hot' ? 'hot_7d' : orderBy.value, 
        order: activeTab.value === 'hot' ? 'desc' : order.value 
      })
    }
    
    if (r.code !== 0) throw new Error(r.message)
    posts.value = r.data ?? []
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载话题列表失败')
  } finally {
    loading.value = false
  }
}

function onTabChange(tab: 'all' | 'hot' | 'following') {
  activeTab.value = tab
  loadPosts()
}

function onSearch() {
  loadPosts()
}

function onAuthorClick(e: Event, userId: number) {
  e.stopPropagation()
  go(`/users/${userId}/posts`)
}

function onOrderChange() {
  loadPosts()
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

onMounted(() => {
  loadPosts()
  loadCollectedIds()
  loadFollowedUsers()
  loadCollectedPosts()
})
</script>

<template>
  <div class="bg-gray-50 text-gray-900 min-h-screen">

    <!-- 头部：知乎风格沉浸式 -->
    <header class="bg-white border-b sticky top-0 z-30 shadow-sm">
      <div class="max-w-7xl mx-auto px-4">
        <div class="flex items-center justify-between h-16">
          <div class="flex items-center gap-8">
            <h1 class="text-xl font-black text-brand-600 tracking-tighter">TopicSquare</h1>
            
            <nav class="flex items-center gap-6 h-16">
              <button 
                v-for="tab in ([
                  { id: 'all', label: '全部', icon: Clock },
                  { id: 'hot', label: '热榜', icon: Flame },
                  { id: 'following', label: '关注', icon: Users }
                ] as const)"
                :key="tab.id"
                class="relative h-16 px-1 flex items-center gap-1.5 text-sm font-bold transition-all group"
                :class="activeTab === tab.id ? 'text-brand-600' : 'text-gray-500 hover:text-gray-900'"
                @click="onTabChange(tab.id)"
              >
                <component :is="tab.icon" :size="16" :stroke-width="activeTab === tab.id ? 3 : 2" />
                {{ tab.label }}
                <div v-if="activeTab === tab.id" class="absolute bottom-0 left-0 right-0 h-1 bg-brand-600 rounded-t-full"></div>
              </button>
            </nav>
          </div>

          <div class="flex-1 max-w-md px-8">
            <div class="relative group">
              <input 
                v-model="keyword"
                type="text" 
                placeholder="搜索你感兴趣的话题..." 
                class="w-full h-10 bg-gray-100 border-none rounded-full px-10 text-sm outline-none focus:ring-2 focus:ring-brand-500/20 transition-all"
                @keyup.enter="onSearch"
              />
              <Search class="w-4 h-4 text-gray-400 absolute left-4 top-3 group-focus-within:text-brand-500 transition-colors" />
            </div>
          </div>

          <div class="flex items-center gap-4">
            <button
              class="bg-brand-600 hover:bg-brand-700 text-white px-5 py-2 rounded-lg text-sm font-black transition-all active:scale-95 shadow-lg shadow-brand-600/20"
              @click="onPublishTalk"
            >
              发布话题
            </button>
          </div>
        </div>
      </div>
    </header>

    <main class="max-w-7xl mx-auto px-4 py-8">
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
        <!-- 左侧 Feed -->
        <div class="lg:col-span-8 space-y-4">
          
          <!-- 快速发布入口 (知乎风格集成卡片) -->
          <Card padding="none" radius="2xl" class="overflow-hidden mb-6 border-none shadow-sm ring-1 ring-gray-100">
            <div class="p-4 flex items-center gap-4">
              <div class="w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center text-gray-400 font-bold">
                {{ displayName[0] }}
              </div>
              <button 
                class="flex-1 h-10 bg-gray-100 rounded-lg px-4 text-left text-sm text-gray-400 font-medium hover:bg-gray-200 transition-colors"
                @click="onPublishTalk"
              >
                有什么行业见解想分享给大家？
              </button>
              <div class="flex items-center gap-4 text-gray-400">
                <button class="hover:text-brand-600 transition-colors flex flex-col items-center" @click="onPublishTalk">
                  <MessageSquare :size="20" />
                  <span class="text-[10px] mt-0.5 font-bold">发文章</span>
                </button>
                <button class="hover:text-orange-500 transition-colors flex flex-col items-center" @click="onPublishTalk">
                  <Plus :size="20" />
                  <span class="text-[10px] mt-0.5 font-bold">提问</span>
                </button>
              </div>
            </div>
          </Card>

          <!-- 内容流 -->
          <div v-loading="loading" class="space-y-4">
            <Card 
              v-for="(post, idx) in posts" 
              :key="post.id"
              padding="none"
              radius="2xl"
              class="group transition-all hover:shadow-xl hover:shadow-brand-900/5 cursor-pointer border-none ring-1 ring-gray-100"
              @click="go(`/talks/${post.id}`)"
            >
              <div class="p-4">
                <!-- 作者行 -->
                <div class="flex items-center justify-between mb-3">
                  <div class="flex items-center gap-2 cursor-pointer group/author" @click="onAuthorClick($event, post.userId)">
                    <div class="w-6 h-6 rounded-md bg-brand-600 text-white flex items-center justify-center text-[10px] font-black shadow-sm group-hover/author:scale-110 transition-transform">
                      {{ (post.nickName || post.userName || '?')[0] }}
                    </div>
                    <span class="text-xs font-bold text-gray-700 group-hover/author:text-brand-600 transition-colors">{{ post.nickName || post.userName }}</span>
                    <ExpertBadge v-if="post.isExpert" />
                    <span class="text-gray-300 mx-1">·</span>
                    <span class="text-xs text-gray-400">{{ formatTime(post.createTime) }}</span>
                  </div>
                  
                  <div class="flex items-center gap-2">
                    <PaidBadge v-if="post.isPaid" :price="post.price" />
                    <StatusBadge v-if="idx === 0 && activeTab === 'all'" type="brand" size="sm">置顶</StatusBadge>
                  </div>
                </div>

                <!-- 标题与摘要 -->
                <div class="flex gap-6">
                  <div class="flex-1">
                    <h3 class="text-lg font-black text-gray-900 mb-2 group-hover:text-brand-600 transition-colors">
                      {{ post.title }}
                    </h3>
                    <p class="text-sm text-gray-500 leading-relaxed line-clamp-3 mb-3">
                      {{ post.content }}
                    </p>
                  </div>
                  <div v-if="post.imagesJson" class="w-32 h-24 rounded-xl overflow-hidden shrink-0 bg-gray-50">
                    <!-- 简化：仅展示第一张图 -->
                    <img :src="JSON.parse(post.imagesJson)[0]" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
                  </div>
                </div>

                <!-- 交互行 -->
                <div class="flex items-center justify-between mt-3 pt-3 border-t border-gray-50">
                  <div class="flex items-center gap-4">
                    <button class="flex items-center gap-1.5 text-xs font-bold transition-all" :class="post.likedByMe ? 'text-red-500' : 'text-gray-400 hover:text-red-500'">
                      <Heart :size="18" :fill="post.likedByMe ? 'currentColor' : 'none'" />
                      {{ post.likeCount ?? 0 }}
                    </button>
                    <button class="flex items-center gap-1.5 text-gray-400 hover:text-brand-600 text-xs font-bold transition-all">
                      <MessageSquare :size="18" />
                      {{ post.commentCount ?? 0 }}
                    </button>
                    <CollectButton :post-id="post.id" :initial-status="collectedPostIds.includes(post.id)" />
                  </div>
                  <button class="text-brand-600 text-xs font-black flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                    阅读全文 <ArrowRight :size="14" />
                  </button>
                </div>
              </div>
            </Card>

            <!-- 空状态 -->
            <div v-if="!loading && posts.length === 0" class="py-24 text-center">
              <div class="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-6 text-gray-300">
                <MessageSquare :size="40" />
              </div>
              <h3 class="text-lg font-bold text-gray-900 mb-2">暂无相关话题</h3>
              <p class="text-sm text-gray-500 mb-8">换个搜索词或者去别的板块看看吧</p>
              <button class="bg-brand-600 text-white px-8 py-2.5 rounded-xl font-bold transition-all active:scale-95" @click="activeTab = 'all'; loadPosts()">
                返回全部话题
              </button>
            </div>
          </div>
        </div>

        <!-- 右侧侧边栏 -->
        <div class="lg:col-span-4 space-y-6">
          
          <!-- 我的帖子入口 -->
          <Card v-if="isLoggedIn" radius="2xl" class="border-none shadow-sm ring-1 ring-gray-100 overflow-hidden mb-6">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-brand-600 text-white flex items-center justify-center text-xs font-black shadow-lg shadow-brand-600/20">
                  {{ displayName[0] }}
                </div>
                <div>
                  <p class="text-sm font-bold text-gray-900">{{ displayName }}</p>
                  <p class="text-[10px] text-gray-400 font-medium">我的内容</p>
                </div>
              </div>
              <button 
                class="text-brand-600 text-xs font-black uppercase tracking-widest hover:underline"
                @click="go(`/users/${auth.me?.userId}/posts`)"
              >
                查看我的帖子
              </button>
            </div>
          </Card>

          <!-- 我的关注模块 -->
          <Card radius="2xl" class="border-none shadow-sm ring-1 ring-gray-100 overflow-hidden">
            <div class="flex items-center justify-between mb-6">
              <h4 class="text-base font-black text-gray-900 flex items-center gap-2">
                <div class="w-1.5 h-4 bg-brand-600 rounded-full"></div>
                我的关注
              </h4>
              <button 
                v-if="isLoggedIn && followedUsers.length > 0"
                class="text-[10px] font-black text-brand-600 uppercase tracking-widest hover:underline"
                @click="go('/talks/following')"
              >查看全部</button>
            </div>
            
            <div v-if="isLoggedIn && followedUsers.length > 0" class="space-y-6">
              <div v-for="user in followedUsers" :key="user.userId" class="flex items-center gap-4 group/exp cursor-pointer" @click="go(`/users/${user.userId}/posts`)">
                <div class="w-10 h-10 rounded-xl bg-slate-900 flex items-center justify-center text-white font-bold shrink-0">
                  {{ (user.nickName || user.userName || '?')[0] }}
                </div>
                <div class="min-w-0 flex-1">
                  <div class="font-bold text-sm text-gray-900 truncate">
                    {{ user.nickName || user.userName }}
                  </div>
                  <p class="text-[10px] text-gray-400 truncate mt-0.5">{{ user.position || '行业同仁' }} · {{ user.companyName || '个人' }}</p>
                </div>
                <ArrowRight :size="14" class="text-gray-300 group-hover/exp:text-brand-600 transition-colors" />
              </div>
            </div>

            <!-- 空状态 -->
            <div v-else-if="isLoggedIn" class="py-8 text-center">
              <div class="w-12 h-12 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300">
                <Users :size="24" />
              </div>
              <p class="text-xs text-gray-400 mb-4">关注感兴趣的用户，这里会显示他们的动态</p>
              <button 
                class="bg-brand-600 text-white px-6 py-2 rounded-xl text-xs font-black transition-all active:scale-95"
                @click="onTabChange('all')"
              >去发现</button>
            </div>

            <!-- 未登录 -->
            <div v-else class="py-8 text-center">
              <div class="w-12 h-12 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300">
                <Users :size="24" />
              </div>
              <p class="text-xs text-gray-400 mb-4">登录后查看你的关注列表</p>
              <button 
                class="border border-brand-600 text-brand-600 px-6 py-2 rounded-xl text-xs font-black transition-all active:scale-95 hover:bg-brand-50"
                @click="requireAuth('/talks')"
              >立即登录</button>
            </div>
          </Card>

          <!-- 我的收藏模块 -->
          <Card radius="2xl" class="border-none shadow-sm ring-1 ring-gray-100 overflow-hidden">
            <div class="flex items-center justify-between mb-6">
              <h4 class="text-base font-black text-gray-900 flex items-center gap-2">
                <Star :size="18" class="text-amber-500" fill="currentColor" />
                我的收藏
              </h4>
              <button 
                v-if="isLoggedIn && collectedPosts.length > 0"
                class="text-[10px] font-black text-brand-600 uppercase tracking-widest hover:underline"
                @click="go('/talks/collected')"
              >查看全部</button>
            </div>

            <div v-if="isLoggedIn && collectedPosts.length > 0" class="space-y-5">
              <div v-for="post in collectedPosts" :key="post.id" class="flex gap-3 cursor-pointer group/hot" @click="go(`/talks/${post.id}`)">
                <div class="min-w-0 flex-1">
                  <div class="text-sm font-bold text-gray-700 line-clamp-2 group-hover/hot:text-brand-600 transition-colors">
                    {{ post.title }}
                  </div>
                  <div class="flex items-center gap-3 mt-1 text-[10px] text-gray-400 font-medium">
                    <span class="flex items-center gap-1"><Heart :size="10" /> {{ post.likeCount || 0 }} 赞同</span>
                    <span>{{ formatTime(post.createTime) }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-else-if="isLoggedIn" class="py-8 text-center">
              <div class="w-12 h-12 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300">
                <Star :size="24" />
              </div>
              <p class="text-xs text-gray-400 mb-2">还没有收藏任何文章</p>
              <p class="text-[10px] text-gray-400">收藏精彩文章，方便下次阅读</p>
            </div>

            <!-- 未登录 -->
            <div v-else class="py-8 text-center">
              <div class="w-12 h-12 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-gray-300">
                <Star :size="24" />
              </div>
              <p class="text-xs text-gray-400 mb-4">登录后同步你的收藏夹</p>
              <button 
                class="border border-brand-600 text-brand-600 px-6 py-2 rounded-xl text-xs font-black transition-all active:scale-95 hover:bg-brand-50"
                @click="requireAuth('/talks')"
              >立即登录</button>
            </div>
          </Card>

          <!-- 社区公约 -->
          <div class="px-4 text-[10px] text-gray-400 font-medium leading-relaxed">
            <div class="flex flex-wrap gap-x-4 gap-y-2 mb-4">
              <a href="#" class="hover:text-brand-600 transition-colors">社区指南</a>
              <a href="#" class="hover:text-brand-600 transition-colors">版权申明</a>
              <a href="#" class="hover:text-brand-600 transition-colors">认证入驻</a>
              <a href="#" class="hover:text-brand-600 transition-colors">侵权举报</a>
            </div>
            <p>© 2026 AgriMatch · 农汇通智库</p>
          </div>
        </div>
      </div>
    </main>

    <PublicFooter />
  </div>
</template>

<style scoped>
.topic-card {
  /* 基础卡片样式已内联 */
}
</style>

