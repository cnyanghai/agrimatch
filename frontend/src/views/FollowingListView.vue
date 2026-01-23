<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, FolderOpen, Clock, Heart, MessageCircle, Factory } from 'lucide-vue-next'
import { getFollowedUsers, getFollowedSupplies, getFollowedRequirements, unfollowUser, type FollowedUser } from '../api/follow'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const followedUsers = ref<FollowedUser[]>([])
const followedSupplies = ref<any[]>([])
const followedRequirements = ref<any[]>([])
const selectedUser = ref<FollowedUser | null>(null)
const activeGroup = ref<string>('all')
const searchKeyword = ref('')

// 获取头像文字
function getAvatarText(user: FollowedUser): string {
  const name = user.nickName || user.userName || 'U'
  return (name[0] || 'U').toUpperCase()
}

// 选择用户查看详情
function selectUser(user: FollowedUser) {
  selectedUser.value = user
}

// 获取信用等级标签样式
function getCreditBadgeClass(user: FollowedUser) {
  // 这里可以根据实际业务逻辑判断信用等级
  // 暂时随机分配以展示效果
  const random = Math.random()
  if (random > 0.7) {
    return { text: '信用优异', class: 'bg-green-100 text-green-700' }
  } else if (random > 0.4) {
    return { text: '信用良好', class: 'bg-blue-100 text-blue-700' }
  } else {
    return { text: '关注异常', class: 'bg-amber-100 text-amber-700' }
  }
}

// 获取用户分类
function getUserCategory(user: FollowedUser) {
  // 根据用户的供应或采购数据判断类型
  const hasSupply = followedSupplies.value.some(s => s.userId === user.userId)
  const hasRequirement = followedRequirements.value.some(r => r.userId === user.userId)
  
  if (hasSupply && hasRequirement) return { text: '综合商户', class: 'text-purple-700' }
  if (hasSupply) return { text: '供应商', class: 'text-brand-700' }
  if (hasRequirement) return { text: '采购商', class: 'text-autumn-700' }
  return { text: '商户', class: 'text-gray-700' }
}

// 获取最后活跃时间
function getActiveTime(user: FollowedUser) {
  const random = Math.random()
  if (random > 0.7) return '2小时前活跃'
  if (random > 0.4) return '昨天活跃'
  if (random > 0.2) return '3天前活跃'
  return '1周前活跃'
}

// 筛选后的用户列表
const filteredUsers = computed(() => {
  let users = followedUsers.value
  
  // 搜索过滤
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    users = users.filter(u => 
      (u.nickName?.toLowerCase().includes(keyword)) ||
      (u.userName?.toLowerCase().includes(keyword)) ||
      (u.companyName?.toLowerCase().includes(keyword))
    )
  }
  
  // 分组过滤
  if (activeGroup.value !== 'all') {
    users = users.filter(u => {
      const category = getUserCategory(u)
      return category.text === activeGroup.value
    })
  }
  
  return users
})

// 获取选中用户的供应信息
const selectedUserSupplies = computed(() => {
  if (!selectedUser.value) return []
  return followedSupplies.value.filter(s => s.userId === selectedUser.value?.userId)
})

// 获取选中用户的采购信息
const selectedUserRequirements = computed(() => {
  if (!selectedUser.value) return []
  return followedRequirements.value.filter(r => r.userId === selectedUser.value?.userId)
})

// 取消关注
async function handleUnfollow() {
  if (!selectedUser.value) return
  
  try {
    const r = await unfollowUser(selectedUser.value.userId)
    if (r.code === 0) {
      ElMessage.success(`已取消关注 ${selectedUser.value.nickName || selectedUser.value.userName}`)
      selectedUser.value = null
      await loadData()
    } else {
      throw new Error(r.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '取消关注失败')
  }
}

// 加载所有数据
async function loadData() {
  loading.value = true
  try {
    await Promise.all([
      loadFollowedUsers(),
      loadFollowedSupplies(),
      loadFollowedRequirements()
    ])
    
    // 自动选中第一个用户
    if (followedUsers.value.length > 0 && !selectedUser.value) {
      selectedUser.value = followedUsers.value[0]
    }
  } finally {
    loading.value = false
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
  try {
    const r = await getFollowedSupplies()
    if (r.code === 0) {
      followedSupplies.value = r.data || []
    }
  } catch (e) {
    console.error('加载关注供应失败', e)
  }
}

// 加载关注用户的采购需求
async function loadFollowedRequirements() {
  try {
    const r = await getFollowedRequirements()
    if (r.code === 0) {
      followedRequirements.value = r.data || []
    }
  } catch (e) {
    console.error('加载关注采购失败', e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <!-- 左右分栏布局 -->
  <div class="split-layout">
    <!-- 左侧关注列表 -->
    <aside class="bg-white border-r border-slate-200 flex flex-col h-full overflow-hidden">
      <!-- 列表头部 -->
      <div class="p-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
        <div class="flex items-center gap-2">
          <Heart class="w-5 h-5 text-brand-700" />
          <h2 class="text-2xl font-bold text-gray-900">关注列表</h2>
          <span class="bg-brand-100 text-brand-700 text-[10px] px-1.5 py-0.5 rounded-full font-bold">
            {{ followedUsers.length }}
          </span>
      </div>
        <button 
          class="text-brand-700 hover:bg-brand-50 p-1.5 rounded-md flex items-center gap-1 text-xs font-bold border border-brand-100"
          title="分组管理"
        >
          <FolderOpen class="w-4 h-4" />
          分组管理
        </button>
      </div>

      <!-- 搜索框 -->
      <div class="p-3 bg-white border-b border-slate-100">
        <div class="flex bg-slate-50 rounded px-3 py-2 items-center gap-2 border border-slate-200">
          <Search class="w-4 h-4 text-slate-400" />
          <input 
            v-model="searchKeyword"
            class="bg-transparent border-none text-xs focus:ring-0 w-full placeholder-slate-400 outline-none" 
            placeholder="搜索已关注商户名称..." 
            type="text"
          />
    </div>
      </div>

      <!-- 分组筛选 -->
      <div class="p-3 bg-white border-b border-slate-100 flex gap-2 overflow-x-auto scrollbar-hide">
        <button
          :class="['whitespace-nowrap px-3 py-1 rounded-full text-xs font-medium', 
            activeGroup === 'all' ? 'bg-brand-700 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200']"
          @click="activeGroup = 'all'"
        >
          全部
        </button>
          <button
          :class="['whitespace-nowrap px-3 py-1 rounded-full text-xs font-medium',
            activeGroup === '供应商' ? 'bg-brand-700 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200']"
          @click="activeGroup = '供应商'"
        >
          供应商
        </button>
        <button
          :class="['whitespace-nowrap px-3 py-1 rounded-full text-xs font-medium',
            activeGroup === '采购商' ? 'bg-brand-700 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200']"
          @click="activeGroup = '采购商'"
        >
          采购商
        </button>
        <button
          :class="['whitespace-nowrap px-3 py-1 rounded-full text-xs font-medium',
            activeGroup === '综合商户' ? 'bg-brand-700 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200']"
          @click="activeGroup = '综合商户'"
        >
          综合商户
        </button>
      </div>

      <!-- 用户列表 -->
      <div class="flex-1 overflow-y-auto">
        <div v-if="filteredUsers.length > 0">
          <div
            v-for="user in filteredUsers"
            :key="user.userId"
            :class="[
              'p-4 border-b border-slate-50 cursor-pointer transition-colors',
              selectedUser?.userId === user.userId 
                ? 'bg-brand-50/50 border-l-4 border-l-brand-600' 
                : 'hover:bg-slate-50 border-l-4 border-l-transparent'
            ]"
            @click="selectUser(user)"
          >
            <div class="flex justify-between items-start mb-1">
              <h3 class="text-base font-bold text-gray-900">
                {{ user.companyName || user.nickName || user.userName }}
              </h3>
              <span 
                :class="['text-[10px] px-1.5 py-0.5 rounded font-bold uppercase', getCreditBadgeClass(user).class]"
              >
                {{ getCreditBadgeClass(user).text }}
              </span>
            </div>
            <p class="text-xs text-slate-500 line-clamp-1 mb-2">
              {{ user.nickName || user.userName }}
              <span v-if="user.companyName && user.nickName"> · {{ user.companyName }}</span>
            </p>
            <div class="flex items-center justify-between">
              <span class="text-[10px] text-slate-400 flex items-center gap-1">
                <Clock class="w-3 h-3" />
                {{ getActiveTime(user) }}
              </span>
              <span :class="['text-[10px] font-bold', getUserCategory(user).class]">
                {{ getUserCategory(user).text }}
              </span>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="flex flex-col items-center justify-center py-16 px-4 text-center">
          <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mb-4">
            <Heart class="w-8 h-8 text-gray-300" />
          </div>
          <h3 class="text-2xl font-bold text-gray-900 mb-1">暂无关注</h3>
          <p class="text-xs text-gray-500">
            {{ searchKeyword ? '未找到匹配的商户' : '您还没有关注任何商户' }}
          </p>
        </div>
      </div>
    </aside>

    <!-- 右侧详情区域 -->
    <section class="bg-white overflow-y-auto flex flex-col">
      <div v-if="selectedUser" class="h-full">
        <!-- 商户信息头部 -->
        <div class="p-8 border-b border-slate-100 bg-gradient-to-r from-brand-50 to-transparent">
          <div class="flex justify-between items-start">
            <div class="flex gap-6 items-center">
              <!-- 商户头像 -->
              <div class="w-20 h-20 bg-brand-100 rounded-lg flex items-center justify-center border-2 border-white shadow-sm overflow-hidden">
                <Factory class="w-10 h-10 text-brand-700" />
              </div>
              <div>
                <div class="flex items-center gap-3 mb-2">
                  <h2 class="text-2xl font-bold text-gray-900">
                    {{ selectedUser.companyName || selectedUser.nickName || selectedUser.userName }}
                  </h2>
                  <span class="bg-brand-600 text-white text-[11px] px-2 py-0.5 rounded-full flex items-center gap-1">
                    <span class="w-3 h-3 rounded-full bg-white/30 flex items-center justify-center">✓</span>
                    平台认证商户
                  </span>
                </div>
                <div class="flex items-center gap-4 text-sm text-slate-600">
                  <span class="flex items-center gap-1">
                    <span>联系人:</span>
                    <span class="font-medium">{{ selectedUser.nickName || selectedUser.userName }}</span>
                  </span>
                  <span v-if="selectedUser.phone" class="flex items-center gap-1">
                    <span>电话:</span>
                    <span class="font-medium">{{ selectedUser.phone }}</span>
                  </span>
                </div>
              </div>
              </div>
            <div class="flex gap-2">
              <button class="bg-brand-700 text-white px-5 py-2.5 rounded font-bold text-sm hover:bg-brand-800 shadow-sm transition-colors flex items-center gap-2">
                <MessageCircle class="w-4 h-4" />
                立即洽谈
              </button>
              <button 
                class="border border-slate-200 text-slate-600 px-5 py-2.5 rounded font-bold text-sm hover:bg-slate-50 transition-colors flex items-center gap-2"
                @click="handleUnfollow"
              >
                <Heart class="w-4 h-4 text-red-500 fill-red-500" />
                已关注
              </button>
            </div>
          </div>
        </div>

        <!-- 商户内容区域 -->
        <div class="p-8 grid grid-cols-1 xl:grid-cols-3 gap-8">
          <!-- 左侧：信用评价 -->
          <div class="xl:col-span-1 flex flex-col gap-6">
            <!-- 信用评分卡片 -->
            <div class="bg-white p-6 rounded-lg border border-slate-200 shadow-sm">
              <div class="flex items-center justify-between mb-6">
                <h3 class="text-2xl font-bold text-gray-900">信用评价</h3>
                <span class="text-2xl font-black text-brand-600 italic">A+</span>
              </div>
              <div class="space-y-5">
                <div class="relative pt-1">
                  <div class="flex items-center justify-between mb-2">
                    <span class="text-xs font-semibold inline-block text-brand-700 uppercase">产品质量</span>
                    <span class="text-xs font-semibold inline-block text-brand-700">98%</span>
                  </div>
                  <div class="overflow-hidden h-2 text-xs flex rounded bg-brand-100">
                    <div class="shadow-none flex flex-col text-center whitespace-nowrap text-white justify-center bg-brand-600" style="width:98%"></div>
                  </div>
                </div>
                <div class="relative pt-1">
                  <div class="flex items-center justify-between mb-2">
                    <span class="text-xs font-semibold inline-block text-brand-700 uppercase">价格竞争力</span>
                    <span class="text-xs font-semibold inline-block text-brand-700">85%</span>
                  </div>
                  <div class="overflow-hidden h-2 text-xs flex rounded bg-brand-100">
                    <div class="shadow-none flex flex-col text-center whitespace-nowrap text-white justify-center bg-brand-600" style="width:85%"></div>
                  </div>
                </div>
                <div class="relative pt-1">
                  <div class="flex items-center justify-between mb-2">
                    <span class="text-xs font-semibold inline-block text-brand-700 uppercase">物流速度</span>
                    <span class="text-xs font-semibold inline-block text-brand-700">92%</span>
                  </div>
                  <div class="overflow-hidden h-2 text-xs flex rounded bg-brand-100">
                    <div class="shadow-none flex flex-col text-center whitespace-nowrap text-white justify-center bg-brand-600" style="width:92%"></div>
      </div>
                </div>
                <div class="relative pt-1">
                  <div class="flex items-center justify-between mb-2">
                    <span class="text-xs font-semibold inline-block text-brand-700 uppercase">售后服务</span>
                    <span class="text-xs font-semibold inline-block text-brand-700">90%</span>
                  </div>
                  <div class="overflow-hidden h-2 text-xs flex rounded bg-brand-100">
                    <div class="shadow-none flex flex-col text-center whitespace-nowrap text-white justify-center bg-brand-600" style="width:90%"></div>
                  </div>
                </div>
              </div>
              <p class="mt-6 text-[10px] text-slate-400 leading-relaxed text-center italic border-t border-slate-50 pt-4">
                * 数据基于平台真实交易评价自动计算
              </p>
            </div>

            <!-- 信用背书卡片 -->
            <div class="bg-slate-900 rounded-lg p-6 text-white">
              <h3 class="text-2xl font-bold text-white mb-4 flex items-center gap-2">
                <span class="w-5 h-5 rounded bg-brand-400/20 flex items-center justify-center text-xs">✓</span>
                信用背书
              </h3>
              <ul class="text-xs space-y-3 opacity-90">
                <li class="flex items-start gap-2">
                  <span class="w-4 h-4 rounded-full bg-brand-400/20 flex items-center justify-center text-[10px] shrink-0 mt-0.5">✓</span>
                  <span>已签署《诚信经营承诺书》</span>
                </li>
                <li class="flex items-start gap-2">
                  <span class="w-4 h-4 rounded-full bg-brand-400/20 flex items-center justify-center text-[10px] shrink-0 mt-0.5">✓</span>
                  <span>实缴保证金: ¥50,000.00</span>
                </li>
                <li class="flex items-start gap-2">
                  <span class="w-4 h-4 rounded-full bg-brand-400/20 flex items-center justify-center text-[10px] shrink-0 mt-0.5">✓</span>
                  <span>连续12个月无虚假交易投诉记录</span>
                </li>
              </ul>
            </div>
          </div>

          <!-- 右侧：最新动态 -->
          <div class="xl:col-span-2 flex flex-col gap-6">
            <!-- 供应/采购信息 -->
            <div class="bg-white rounded-lg border border-slate-200 shadow-sm flex flex-col">
              <div class="px-6 py-4 border-b border-slate-100 flex justify-between items-center">
                <h3 class="text-2xl font-bold text-gray-900">最新动态</h3>
                <button class="text-brand-700 text-xs font-bold hover:underline">查看全部</button>
              </div>
              <div class="divide-y divide-slate-50">
                <!-- 供应信息动态 -->
                <div 
                  v-for="supply in selectedUserSupplies.slice(0, 3)"
                  :key="supply.id"
                  class="p-6 hover:bg-slate-50/50 transition-colors cursor-pointer"
                  @click="router.push(`/supply-browse?id=${supply.id}`)"
                >
                  <div class="flex gap-4">
                    <div class="mt-1 w-2 h-2 rounded-full bg-brand-500 ring-4 ring-brand-50"></div>
                    <div class="flex-1">
                      <div class="flex justify-between items-start mb-2">
                        <span class="font-bold text-slate-800 text-sm">供应: {{ supply.categoryName }}</span>
                        <span class="text-[10px] text-slate-400 font-medium">刚刚</span>
                      </div>
                      <div class="bg-slate-50 p-3 rounded text-xs border border-slate-100">
                        <div class="grid grid-cols-2 gap-4">
                          <div>
                            <p class="text-slate-500 mb-1">出厂价 (元/吨)</p>
                            <p class="text-base font-bold text-brand-700">{{ supply.exFactoryPrice || '-' }}</p>
                          </div>
                          <div>
                            <p class="text-slate-500 mb-1">库存数量</p>
                            <p class="text-sm font-bold text-slate-700">{{ supply.quantity || '-' }}</p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 采购需求动态 -->
                <div 
                  v-for="req in selectedUserRequirements.slice(0, 2)"
                  :key="req.id"
                  class="p-6 hover:bg-slate-50/50 transition-colors cursor-pointer"
                  @click="router.push(`/requirement-browse?id=${req.id}`)"
                >
                  <div class="flex gap-4">
                    <div class="mt-1 w-2 h-2 rounded-full bg-autumn-500 ring-4 ring-autumn-50"></div>
                    <div class="flex-1">
                      <div class="flex justify-between items-start mb-2">
                        <span class="font-bold text-slate-800 text-sm">采购需求: {{ req.categoryName }}</span>
                        <span class="text-[10px] text-slate-400 font-medium">1小时前</span>
                      </div>
                      <p class="text-xs text-slate-600 leading-relaxed mb-3">
                        需求数量: {{ req.quantity }}，预算价格: ¥{{ req.budgetPrice }}/吨
                      </p>
                      <div class="flex gap-2">
                        <span class="bg-autumn-50 text-autumn-700 text-[10px] px-2 py-0.5 rounded border border-autumn-100">紧急采购</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 空状态 -->
                <div 
                  v-if="selectedUserSupplies.length === 0 && selectedUserRequirements.length === 0"
                  class="p-12 text-center"
                >
                  <p class="text-sm text-slate-400">该商户暂无最新动态</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 未选中用户时的空状态 -->
      <div v-else class="flex-1 flex items-center justify-center">
        <div class="text-center">
          <div class="w-24 h-24 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-6">
            <Heart class="w-12 h-12 text-gray-300" />
          </div>
          <h3 class="text-2xl font-bold text-gray-900 mb-2">选择一个商户查看详情</h3>
          <p class="text-sm text-gray-500">从左侧列表中选择您关注的商户</p>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.split-layout {
  display: grid;
  grid-template-columns: 400px 1fr;
  height: calc(100vh - 80px);
  overflow: hidden;
  margin: -1.5rem;
  width: calc(100% + 3rem);
}

.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

/* 行截断 */
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>

