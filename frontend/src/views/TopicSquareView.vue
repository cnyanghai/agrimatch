<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { requireAuth } from '../utils/requireAuth'
import { listPosts, type PostResponse } from '../api/post'
import { useAuthStore } from '../store/auth'
import { useUiStore } from '../store/ui'
import PublicFooter from '../components/PublicFooter.vue'
import { MessageSquare, Heart, Search, ChevronDown, Plus, Star, Gift, Coins, CheckCircle } from 'lucide-vue-next'

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
const activeTab = ref<'all' | 'bounty'>('all')

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

async function loadPosts() {
  loading.value = true
  try {
    const r = await listPosts({ 
      keyword: keyword.value.trim() || undefined, 
      postType: activeTab.value === 'bounty' ? 'bounty' : undefined,
      orderBy: orderBy.value, 
      order: order.value 
    })
    if (r.code !== 0) throw new Error(r.message)
    posts.value = r.data ?? []
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载话题列表失败')
  } finally {
    loading.value = false
  }
}

function onTabChange(tab: 'all' | 'bounty') {
  activeTab.value = tab
  loadPosts()
}

function onSearch() {
  loadPosts()
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
})
</script>

<template>
  <div class="bg-gray-50 text-gray-900 min-h-screen">

    <!-- 头部 -->
    <header class="bg-white border-b">
      <div class="max-w-7xl mx-auto px-4 py-12">
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-8">
          <div>
            <h1 class="text-4xl font-extrabold text-gray-900 tracking-tight mb-2">话题广场</h1>
            <p class="text-gray-500 text-lg">在这里，每一个关于饲料原料的思考都有价值</p>
          </div>
          <div class="bg-amber-50 border border-amber-100 rounded-xl p-6 flex items-center gap-6 shadow-sm">
            <div class="w-14 h-14 bg-amber-100 rounded-xl flex items-center justify-center text-amber-600 shadow-inner">
              <Star :size="28" fill="currentColor" />
            </div>
            <div>
              <p class="text-sm font-bold text-amber-800">发布高质量话题</p>
              <p class="text-xs text-amber-600/80 font-medium">最高可得 <span class="text-lg font-black">500</span> 积分</p>
            </div>
            <button class="bg-amber-500 text-white text-sm font-bold px-6 py-2.5 rounded-xl hover:bg-amber-600 transition-all  shadow-md shadow-amber-500/20" @click="onPublishTalk">立即参与</button>
          </div>
        </div>

        <div class="flex items-center gap-8 mt-12 border-b relative">
          <button 
            class="pb-4 px-2 text-sm font-bold transition-colors"
            :class="activeTab === 'all' ? 'border-b-2 border-brand-600 text-brand-600' : 'text-gray-400 hover:text-brand-600'"
            @click="onTabChange('all')"
          >全部话题</button>
          <button 
            class="pb-4 px-2 text-sm font-medium transition-colors flex items-center gap-1.5 group"
            :class="activeTab === 'bounty' ? 'font-bold border-b-2 border-amber-500 text-amber-600' : 'text-gray-400 hover:text-amber-500'"
            @click="onTabChange('bounty')"
          >
            赏金求助
            <span class="w-2 h-2 bg-red-500 rounded-full group-hover:animate-ping"></span>
          </button>
        </div>
      </div>
    </header>

    <main class="max-w-7xl mx-auto px-4 py-12">
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-12">
        <!-- 列表 -->
        <div class="lg:col-span-8 space-y-8">
          <div class="flex flex-col sm:flex-row gap-4 mb-10">
            <div class="flex-1 relative group">
              <input 
                v-model="keyword"
                type="text" 
                placeholder="搜索你感兴趣的话题或关键字..." 
                class="w-full h-12 bg-white border border-gray-200 rounded-xl px-12 text-sm outline-none focus:border-brand-500 focus:ring-4 focus:ring-brand-500/5 transition-all shadow-sm"
                @keyup.enter="onSearch"
              />
              <Search class="w-5 h-5 text-gray-400 absolute left-4 top-3.5 group-focus-within:text-brand-500 transition-colors" />
            </div>
            <div class="relative min-w-[140px]">
              <select 
                v-model="order"
                class="w-full h-12 bg-white border border-gray-200 rounded-xl px-4 pr-10 text-sm font-bold text-gray-600 appearance-none outline-none cursor-pointer hover:border-brand-500 transition-all shadow-sm"
                @change="onOrderChange"
              >
                <option value="desc">最新发布</option>
                <option value="asc">最早发布</option>
              </select>
              <ChevronDown class="w-4 h-4 text-gray-400 absolute right-4 top-4 pointer-events-none" />
            </div>
          </div>

          <div v-loading="loading" class="space-y-8">
            <div 
              v-for="(post, idx) in posts" 
              :key="post.id"
              class="topic-card bg-white rounded-[32px] p-8 border border-gray-200 transition-all cursor-pointer hover:shadow-2xl hover:shadow-brand-900/5 "
              @click="go(`/talks/${post.id}`)"
            >
              <div class="flex justify-between items-start mb-6">
                <div class="flex items-center gap-3">
                  <span 
                    v-if="idx === 0"
                    class="bg-brand-600 text-white text-[10px] font-black uppercase tracking-widest px-2.5 py-1 rounded-lg shadow-md shadow-brand-500/20"
                  >
                    TOP
                  </span>
                  <span class="text-[10px] text-gray-400 font-bold uppercase tracking-wider">
                    {{ formatTime(post.createTime) }} · {{ post.companyName || '行业同仁' }}
                  </span>
                </div>
                <!-- 赏金标签 -->
                <div v-if="post.postType === 'bounty'" class="flex items-center gap-2">
                  <span 
                    v-if="post.bountyStatus === 1"
                    class="flex items-center gap-1 bg-brand-100 text-brand-700 text-[10px] font-bold px-2.5 py-1 rounded-lg"
                  >
                    <CheckCircle :size="12" />
                    已采纳
                  </span>
                  <span 
                    v-else
                    class="flex items-center gap-1 bg-amber-100 text-amber-700 text-[10px] font-bold px-2.5 py-1 rounded-lg animate-pulse"
                  >
                    <Coins :size="12" />
                    {{ post.bountyPoints }} 积分
                  </span>
                </div>
              </div>
              <h3 
                class="text-2xl font-black text-gray-900 mb-4 hover:text-brand-600 transition-colors line-clamp-1"
              >
                {{ post.title }}
              </h3>
              <p v-if="post.content" class="text-gray-500 leading-relaxed line-clamp-2 text-sm">
                {{ post.content }}
              </p>
              
              <div class="mt-8 flex items-center justify-between pt-6 border-t border-gray-50">
                <div class="flex items-center gap-3">
                  <div class="w-8 h-8 rounded-xl bg-brand-600 text-white flex items-center justify-center text-xs font-black shadow-md shadow-brand-500/20">
                    {{ (post.nickName || post.userName || '?')[0] }}
                  </div>
                  <span class="text-xs font-bold text-gray-700">
                    {{ post.nickName || post.userName || '匿名' }}
                    <span class="text-gray-400 font-medium"> · {{ post.position || '资深专家' }}</span>
                  </span>
                </div>
                <div class="flex items-center gap-6 text-xs font-bold text-gray-400">
                  <span class="flex items-center gap-1.5 hover:text-brand-600 transition-colors">
                    <MessageSquare :size="16" />
                    {{ post.commentCount ?? 0 }}
                  </span>
                  <span class="flex items-center gap-1.5 hover:text-red-500 transition-colors">
                    <Heart :size="16" />
                    {{ post.likeCount ?? 0 }}
                  </span>
                </div>
              </div>
            </div>

            <div v-if="!loading && posts.length === 0" class="text-center py-24 bg-white rounded-[32px] border border-dashed border-gray-200">
              <MessageSquare :size="48" class="mx-auto text-gray-200 mb-4" />
              <div class="text-gray-400 font-bold">暂无话题，快来发布第一个话题吧！</div>
            </div>
          </div>
        </div>

        <!-- 右侧 -->
        <div class="lg:col-span-4 space-y-8">
          <!-- 用户卡片 -->
          <div class="bg-slate-900 rounded-[32px] p-8 text-white shadow-2xl relative overflow-hidden group">
            <div class="absolute -right-10 -top-10 w-40 h-40 bg-brand-500/10 rounded-full blur-3xl group-hover:bg-brand-500/20 transition-all"></div>
            <div class="relative z-10">
              <div class="flex items-center gap-4 mb-8">
                <div class="w-14 h-14 rounded-xl bg-gradient-to-br from-brand-500 to-teal-600 flex items-center justify-center font-black text-xl text-white shadow-md shadow-brand-500/20">
                  {{ displayName[0] || '我' }}
                </div>
                <div>
                  <h4 class="font-black text-lg">{{ displayName }}</h4>
                  <p class="text-xs text-brand-400 font-bold uppercase tracking-widest mt-1">
                    {{ isLoggedIn ? 'Professional Member' : 'Guest Viewer' }}
                  </p>
                </div>
              </div>
              <button 
                v-if="isLoggedIn"
                class="w-full py-4 bg-brand-600 text-white rounded-xl text-sm font-black hover:bg-brand-700 transition-all active:scale-[0.98] shadow-md shadow-brand-600/20" 
                @click="onEnterMy"
              >
                进入控制台
              </button>
              <button 
                v-else
                class="w-full py-4 bg-brand-600 text-white rounded-xl text-sm font-black hover:bg-brand-700 transition-all active:scale-[0.98] shadow-md shadow-brand-600/20" 
                @click="ui.openAuthDialog('login')"
              >
                立即登录
              </button>
            </div>
          </div>

          <!-- 积分兑换卡片 -->
          <div class="bg-gradient-to-br from-amber-500 to-orange-600 rounded-[32px] p-8 text-white relative overflow-hidden shadow-md group cursor-pointer" @click="onPublishTalk">
            <div class="absolute -right-4 -bottom-4 opacity-20 transform group-hover:scale-110 group-hover:-rotate-12 transition-all duration-500">
              <Gift :size="120" />
            </div>
            <div class="relative z-10">
              <div class="bg-white/20 w-10 h-10 rounded-xl flex items-center justify-center mb-6 backdrop-blur-md">
                <Plus :size="20" class="text-white" stroke-width="3" />
              </div>
              <h4 class="font-black text-xl mb-2">发布话题赚积分</h4>
              <p class="text-sm text-amber-50 font-medium mb-6 leading-relaxed">分享您的真知灼见，积累积分可直接在线兑换实物好礼。</p>
              <span class="text-xs font-black uppercase tracking-widest border-b-2 border-white/40 pb-1 group-hover:border-white transition-all">立即开始发布 →</span>
            </div>
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
