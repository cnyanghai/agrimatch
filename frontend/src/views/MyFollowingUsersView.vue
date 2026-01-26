<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Users, UserMinus, MessageSquare, ArrowRight } from 'lucide-vue-next'
import { getFollowedUsers, unfollowUser, type FollowedUser } from '../api/follow'
import { Card } from '../components/ui'
import PublicFooter from '../components/PublicFooter.vue'
import ExpertBadge from '../components/post/ExpertBadge.vue'

const router = useRouter()

const users = ref<FollowedUser[]>([])
const loading = ref(false)
const unfollowingId = ref<number | null>(null)

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

async function handleUnfollow(user: FollowedUser) {
  if (unfollowingId.value) return

  unfollowingId.value = user.userId
  try {
    const r = await unfollowUser(user.userId)
    if (r.code === 0) {
      users.value = users.value.filter(u => u.userId !== user.userId)
      ElMessage.success(`已取消关注 ${user.nickName || user.userName}`)
    } else {
      throw new Error(r.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '取消关注失败')
  } finally {
    unfollowingId.value = null
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
        <div class="w-20"></div>
      </div>
    </header>

    <main class="max-w-4xl mx-auto px-4 py-8">
      <div v-loading="loading">
        <!-- 用户列表 -->
        <div v-if="users.length > 0" class="space-y-4">
          <Card
            v-for="user in users"
            :key="user.userId"
            padding="none"
            radius="2xl"
            class="group transition-all hover:shadow-lg border-none ring-1 ring-gray-100"
          >
            <div class="p-6">
              <div class="flex items-center gap-6">
                <!-- 头像 -->
                <div
                  class="w-16 h-16 rounded-2xl bg-brand-600 text-white flex items-center justify-center text-2xl font-black shadow-lg shadow-brand-600/20 cursor-pointer hover:scale-105 transition-transform"
                  @click="go(`/users/${user.userId}/posts`)"
                >
                  {{ (user.nickName || user.userName || '?')[0] }}
                </div>

                <!-- 用户信息 -->
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-3 mb-2">
                    <h3
                      class="text-lg font-black text-gray-900 cursor-pointer hover:text-brand-600 transition-colors"
                      @click="go(`/users/${user.userId}/posts`)"
                    >
                      {{ user.nickName || user.userName }}
                    </h3>
                    <ExpertBadge v-if="user.position?.includes('专家')" />
                  </div>
                  <p class="text-sm text-gray-500 mb-2">
                    {{ user.position || '行业同仁' }}
                    <span v-if="user.companyName"> · {{ user.companyName }}</span>
                  </p>
                  <p class="text-xs text-gray-400">
                    关注于 {{ formatTime(user.followTime) }}
                  </p>
                </div>

                <!-- 操作按钮 -->
                <div class="flex items-center gap-3">
                  <button
                    class="flex items-center gap-2 px-5 py-2.5 bg-brand-600 text-white rounded-xl text-sm font-bold hover:bg-brand-700 transition-all active:scale-95 shadow-lg shadow-brand-600/10"
                    @click="go(`/users/${user.userId}/posts`)"
                  >
                    <MessageSquare :size="16" />
                    查看动态
                  </button>
                  <button
                    class="flex items-center gap-2 px-5 py-2.5 border border-gray-200 text-gray-500 rounded-xl text-sm font-bold hover:bg-gray-50 hover:text-red-500 hover:border-red-200 transition-all active:scale-95"
                    :disabled="unfollowingId === user.userId"
                    @click.stop="handleUnfollow(user)"
                  >
                    <UserMinus :size="16" />
                    {{ unfollowingId === user.userId ? '取消中...' : '取消关注' }}
                  </button>
                </div>
              </div>
            </div>
          </Card>
        </div>

        <!-- 空状态 -->
        <div v-else-if="!loading" class="py-24 text-center">
          <div class="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-6 text-gray-300">
            <Users :size="48" />
          </div>
          <h3 class="text-xl font-black text-gray-900 mb-2">暂无关注</h3>
          <p class="text-sm text-gray-500 mb-8 max-w-xs mx-auto">
            关注感兴趣的用户，及时获取他们发布的精彩内容
          </p>
          <button
            class="bg-brand-600 text-white px-8 py-3 rounded-xl font-bold transition-all active:scale-95 shadow-lg shadow-brand-600/20"
            @click="go('/talks')"
          >
            去发现感兴趣的人
          </button>
        </div>
      </div>
    </main>

    <PublicFooter />
  </div>
</template>
