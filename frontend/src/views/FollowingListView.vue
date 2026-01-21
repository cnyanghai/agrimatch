<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, User, Building2, RefreshCw, Plus, X, Package, ShoppingCart, ArrowRight, MapPin } from 'lucide-vue-next'
import { getFollowedUsers, getFollowedSupplies, getFollowedRequirements, unfollowUser, type FollowedUser } from '../api/follow'
import { BaseButton, EmptyState } from '../components/ui'

const router = useRouter()
const loading = ref(false)
const followedUsers = ref<FollowedUser[]>([])
const followedSupplies = ref<any[]>([])
const followedRequirements = ref<any[]>([])
const selectedUserId = ref<number | null>(null)
const activeTab = ref<'supplies' | 'requirements'>('supplies')

// 获取头像文字
function getAvatarText(user: FollowedUser): string {
  const name = user.nickName || user.userName || 'U'
  return name[0].toUpperCase()
}

// 切换用户筛选
function toggleUserFilter(userId: number) {
  selectedUserId.value = selectedUserId.value === userId ? null : userId
}

// 取消关注
async function handleUnfollow(user: FollowedUser) {
  try {
    const r = await unfollowUser(user.userId)
    if (r.code === 0) {
      ElMessage.success(`已取消关注 ${user.nickName || user.userName}`)
      await loadFollowedUsers()
      await loadFollowedSupplies()
      await loadFollowedRequirements()
    } else {
      throw new Error(r.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '取消关注失败')
  }
}

// 加载关注用户列表
async function loadFollowedUsers() {
  try {
    const r = await getFollowedUsers()
    if (r.code === 0) {
      followedUsers.value = r.data || []
    }
  } catch (e) {
    console.error('加载关注列表失败', e)
  }
}

// 加载关注用户的供应信息
async function loadFollowedSupplies() {
  loading.value = true
  try {
    const r = await getFollowedSupplies()
    if (r.code === 0) {
      followedSupplies.value = r.data || []
    }
  } catch (e) {
    console.error('加载关注供应失败', e)
  } finally {
    loading.value = false
  }
}

// 加载关注用户的采购需求
async function loadFollowedRequirements() {
  loading.value = true
  try {
    const r = await getFollowedRequirements()
    if (r.code === 0) {
      followedRequirements.value = r.data || []
    }
  } catch (e) {
    console.error('加载关注采购失败', e)
  } finally {
    loading.value = false
  }
}

// 筛选后的供应列表
const filteredSupplies = computed(() => {
  if (!selectedUserId.value) return followedSupplies.value
  return followedSupplies.value.filter(s => s.userId === selectedUserId.value)
})

// 筛选后的采购列表
const filteredRequirements = computed(() => {
  if (!selectedUserId.value) return followedRequirements.value
  return followedRequirements.value.filter(r => r.userId === selectedUserId.value)
})

// 刷新数据
async function refreshData() {
  await Promise.all([
    loadFollowedUsers(),
    loadFollowedSupplies(),
    loadFollowedRequirements()
  ])
}

// 跳转到大厅
function goToHall() {
  router.push('/hall/supply')
}

onMounted(() => {
  loadFollowedUsers()
  loadFollowedSupplies()
  loadFollowedRequirements()
})
</script>

<template>
  <div class="space-y-6">
    <!-- 页面头部 -->
    <div class="flex items-end justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">关注列表</h1>
        <p class="text-gray-500 mt-1">追踪您关注的商户最新动态</p>
      </div>
      <div class="flex items-center gap-3">
        <BaseButton type="secondary" size="sm" :loading="loading" @click="refreshData">
          <RefreshCw class="w-4 h-4" />
          刷新
        </BaseButton>
        <BaseButton type="primary" size="sm" @click="goToHall">
          <Plus class="w-4 h-4" />
          发现更多
        </BaseButton>
      </div>
    </div>

    <!-- 关注商户头像列表 -->
    <section>
      <div class="flex items-center gap-3 mb-4">
        <span class="text-[10px] font-bold uppercase tracking-widest text-gray-400">关注的商户</span>
        <span class="text-xs text-gray-400">{{ followedUsers.length }} 人</span>
      </div>

      <div v-if="followedUsers.length > 0" class="flex gap-4 overflow-x-auto pb-4 scrollbar-hide">
        <!-- 全部按钮 -->
        <button
          class="flex flex-col items-center gap-2 shrink-0 group"
          @click="selectedUserId = null"
        >
          <div 
            class="w-16 h-16 rounded-full flex items-center justify-center transition-all border-2"
            :class="selectedUserId === null 
              ? 'bg-brand-600 text-white border-brand-600 shadow-md shadow-brand-200' 
              : 'bg-gray-100 text-gray-500 border-gray-200 group-hover:border-brand-300'"
          >
            <User class="w-6 h-6" />
          </div>
          <span class="text-xs font-medium text-gray-600">全部</span>
        </button>

        <!-- 用户头像列表 -->
        <div 
          v-for="user in followedUsers" 
          :key="user.userId"
          class="flex flex-col items-center gap-2 shrink-0 group relative"
        >
          <button
            class="w-16 h-16 rounded-full flex items-center justify-center text-xl font-bold transition-all border-2"
            :class="selectedUserId === user.userId 
              ? 'bg-slate-900 text-white border-slate-900 shadow-md shadow-slate-300' 
              : 'bg-white text-slate-700 border-gray-200 group-hover:border-brand-300 group-hover:shadow-md'"
            @click="toggleUserFilter(user.userId)"
          >
            {{ getAvatarText(user) }}
          </button>
          <div class="text-center max-w-[64px]">
            <div class="text-xs font-medium text-gray-700 truncate">{{ user.nickName || user.userName }}</div>
            <div v-if="user.companyName" class="text-[10px] text-gray-400 truncate">{{ user.companyName }}</div>
          </div>
          
          <!-- 取消关注按钮（悬停显示） -->
          <button
            class="absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white rounded-full text-xs opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center hover:bg-red-600"
            @click.stop="handleUnfollow(user)"
            title="取消关注"
          >
            <X class="w-3 h-3" />
          </button>
        </div>

        <!-- 添加更多按钮 -->
        <button
          class="flex flex-col items-center gap-2 shrink-0 group"
          @click="goToHall"
        >
          <div class="w-16 h-16 rounded-full border-2 border-dashed border-gray-300 flex items-center justify-center text-gray-400 group-hover:border-brand-400 group-hover:text-brand-500 transition-all">
            <Plus class="w-6 h-6" />
          </div>
          <span class="text-xs font-medium text-gray-400 group-hover:text-brand-500">发现更多</span>
        </button>
      </div>

      <!-- 未关注任何人时的提示 -->
      <EmptyState
        v-else
        type="data"
        title="还没有关注任何商户"
        description="前往供应大厅或采购大厅，发现优质商户并关注他们"
        size="md"
      >
        <template #action>
          <BaseButton type="primary" @click="goToHall">
            <Plus class="w-4 h-4" />
            去发现商户
          </BaseButton>
        </template>
      </EmptyState>
    </section>

    <!-- 动态信息流 -->
    <section v-if="followedUsers.length > 0">
      <!-- Tab 切换 -->
      <div class="flex items-center gap-2 mb-6 bg-gray-100 rounded-xl p-1">
        <button
          :class="[
            'flex-1 px-4 py-2 rounded-lg text-sm font-bold transition-all',
            activeTab === 'supplies'
              ? 'bg-white text-brand-600 shadow-sm'
              : 'text-gray-600 hover:text-gray-900'
          ]"
          @click="activeTab = 'supplies'"
        >
          <Package class="w-4 h-4 inline mr-2" />
          供应信息
          <span v-if="filteredSupplies.length > 0" class="ml-2 text-xs bg-brand-50 text-brand-600 px-1.5 py-0.5 rounded-full">
            {{ filteredSupplies.length }}
          </span>
        </button>
        <button
          :class="[
            'flex-1 px-4 py-2 rounded-lg text-sm font-bold transition-all',
            activeTab === 'requirements'
              ? 'bg-white text-blue-600 shadow-sm'
              : 'text-gray-600 hover:text-gray-900'
          ]"
          @click="activeTab = 'requirements'"
        >
          <ShoppingCart class="w-4 h-4 inline mr-2" />
          采购需求
          <span v-if="filteredRequirements.length > 0" class="ml-2 text-xs bg-blue-50 text-blue-600 px-1.5 py-0.5 rounded-full">
            {{ filteredRequirements.length }}
          </span>
        </button>
      </div>

      <!-- 供应信息列表 -->
      <div v-if="activeTab === 'supplies'" v-loading="loading" class="min-h-[200px]">
        <div v-if="filteredSupplies.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="supply in filteredSupplies"
            :key="supply.id"
            class="group bg-white rounded-xl border border-gray-200 overflow-hidden transition-all hover:shadow-md hover:border-brand-100 cursor-pointer"
            @click="router.push(`/supply-browse?id=${supply.id}`)"
          >
            <div class="p-5">
              <div class="flex items-center gap-3 mb-3">
                <div class="w-10 h-10 bg-brand-600 text-white rounded-lg flex items-center justify-center font-bold">
                  {{ (supply.nickName || supply.userName || 'U')[0].toUpperCase() }}
                </div>
                <div class="flex-1 min-w-0">
                  <div class="font-bold text-gray-900 truncate">{{ supply.nickName || supply.userName || '供应商' }}</div>
                  <div class="text-xs text-gray-500 truncate">{{ supply.companyName || '个人商户' }}</div>
                </div>
              </div>
              <h3 class="font-bold text-gray-900 mb-2 line-clamp-2 group-hover:text-brand-600 transition-colors">
                {{ supply.categoryName || '原料供应' }}
              </h3>
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-500">数量: {{ supply.quantity || '-' }}</span>
                <span class="font-bold text-brand-600">¥{{ supply.exFactoryPrice || '-' }}/吨</span>
              </div>
            </div>
          </div>
        </div>
        <EmptyState
          v-else
          type="data"
          :title="selectedUserId ? '该用户暂未发布供应信息' : '您关注的商户暂未发布新的供应'"
          description=""
          size="sm"
        />
      </div>

      <!-- 采购需求列表 -->
      <div v-if="activeTab === 'requirements'" v-loading="loading" class="min-h-[200px]">
        <div v-if="filteredRequirements.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="req in filteredRequirements"
            :key="req.id"
            class="group bg-white rounded-xl border border-gray-200 overflow-hidden transition-all hover:shadow-md hover:border-blue-100 cursor-pointer"
            @click="router.push(`/requirement-browse?id=${req.id}`)"
          >
            <div class="p-5">
              <div class="flex items-center gap-3 mb-3">
                <div class="w-10 h-10 bg-blue-600 text-white rounded-lg flex items-center justify-center font-bold">
                  {{ (req.nickName || req.userName || 'U')[0].toUpperCase() }}
                </div>
                <div class="flex-1 min-w-0">
                  <div class="font-bold text-gray-900 truncate">{{ req.nickName || req.userName || '采购商' }}</div>
                  <div class="text-xs text-gray-500 truncate">{{ req.companyName || '个人商户' }}</div>
                </div>
              </div>
              <h3 class="font-bold text-gray-900 mb-2 line-clamp-2 group-hover:text-blue-600 transition-colors">
                {{ req.categoryName || '原料采购' }}
              </h3>
              <div class="flex items-center justify-between text-sm">
                <span class="text-gray-500">需求: {{ req.quantity || '-' }}</span>
                <span class="font-bold text-blue-600">预算: ¥{{ req.budgetPrice || '-' }}/吨</span>
              </div>
            </div>
          </div>
        </div>
        <EmptyState
          v-else
          type="data"
          :title="selectedUserId ? '该用户暂未发布采购需求' : '您关注的商户暂未发布新的采购'"
          description=""
          size="sm"
        />
      </div>
    </section>
  </div>
</template>

<style scoped>
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
</style>

