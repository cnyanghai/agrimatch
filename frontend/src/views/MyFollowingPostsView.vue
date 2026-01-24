<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Users, UserMinus, Clock } from 'lucide-vue-next'
import { getFollowedUsers, unfollowUser, type FollowedUser } from '../api/follow'
import ExpertBadge from '../components/post/ExpertBadge.vue'
import { Card } from '../components/ui'
import PublicFooter from '../components/PublicFooter.vue'

const router = useRouter()

const users = ref<FollowedUser[]>([])
const loading = ref(false)

async function loadFollowedUsers() {
  loading.value = true
  try {
    const r = await getFollowedUsers()
    if (r.code === 0) {
      users.value = r.data || []
    } else {
      throw new Error(r.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载关注列表失败')
  } finally {
    loading.value = false
  }
}

async function handleUnfollow(e: Event, user: FollowedUser) {
  e.stopPropagation()
  try {
    await ElMessageBox.confirm(`确定要取消关注 ${user.nickName || user.userName} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const r = await unfollowUser(user.userId)
    if (r.code === 0) {
      ElMessage.success('已取消关注')
      users.value = users.value.filter(u => u.userId !== user.userId)
    } else {
      throw new Error(r.message)
    }
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || '取消关注失败')
    }
  }
}

function formatTime(timeStr: string | undefined) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

function go(path: string) {
  router.push(path)
}

function goToUserPosts(userId: number) {
  go(`/users/${userId}/posts`)
}

onMounted(() => {
  loadFollowedUsers()
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
          我的关注
        </h1>
        <div class="w-20"></div> <!-- Spacer -->
      </div>
    </header>

    <main class="max-w-5xl mx-auto px-4 py-8">
      <div v-loading="loading">
        <div v-if="users.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <Card 
            v-for="user in users" 
            :key="user.userId"
            padding="none"
            radius="2xl"
            class="group transition-all hover:shadow-xl hover:shadow-brand-900/5 cursor-pointer border-none ring-1 ring-gray-100 flex flex-col"
            @click="goToUserPosts(user.userId)"
          >
            <div class="p-6 flex-1">
              <div class="flex items-start justify-between mb-4">
                <div class="w-16 h-16 rounded-2xl bg-brand-600 text-white flex items-center justify-center text-2xl font-black shadow-lg shadow-brand-600/20 group-hover:scale-105 transition-transform">
                  {{ (user.nickName || user.userName || '?')[0] }}
                </div>
                <button 
                  class="p-2 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-xl transition-all opacity-0 group-hover:opacity-100"
                  title="取消关注"
                  @click="handleUnfollow($event, user)"
                >
                  <UserMinus :size="20" />
                </button>
              </div>

              <div class="mb-4">
                <div class="flex items-center gap-2 mb-1">
                  <h4 class="text-lg font-black text-gray-900 group-hover:text-brand-600 transition-colors truncate">
                    {{ user.nickName || user.userName }}
                  </h4>
                  <ExpertBadge v-if="user.position?.includes('专家')" />
                </div>
                <p class="text-sm text-gray-500 font-medium truncate">
                  {{ user.position || '行业同仁' }} · {{ user.companyName || '个人作者' }}
                </p>
              </div>

              <div class="pt-4 border-t border-gray-50 flex items-center justify-between text-[10px] text-gray-400 font-bold uppercase tracking-wider">
                <div class="flex items-center gap-1.5">
                  <Clock :size="12" />
                  关注于 {{ formatTime(user.followTime) }}
                </div>
                <span class="text-brand-600 opacity-0 group-hover:opacity-100 transition-opacity">查看主页</span>
              </div>
            </div>
          </Card>
        </div>

        <!-- 空状态 -->
        <div v-else-if="!loading" class="py-24 text-center">
          <div class="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-6 text-gray-300">
            <Users :size="48" />
          </div>
          <h3 class="text-xl font-black text-gray-900 mb-2">还没有关注任何人</h3>
          <p class="text-sm text-gray-500 mb-8 max-w-xs mx-auto">关注你感兴趣的行业大咖和专家，第一时间获取他们的最新动态与深度见解</p>
          <button class="bg-brand-600 text-white px-8 py-3 rounded-xl font-bold transition-all active:scale-95 shadow-lg shadow-brand-600/20" @click="go('/talks')">
            去发现感兴趣的人
          </button>
        </div>
      </div>
    </main>

    <PublicFooter />
  </div>
</template>

<style scoped>
/* 保持一致的阴影与过渡 */
</style>
