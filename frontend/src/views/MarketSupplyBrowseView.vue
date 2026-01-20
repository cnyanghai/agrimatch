<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, View, Location, Box, Plus, UserFilled, Refresh } from '@element-plus/icons-vue'
import { getFollowedUsers, getFollowedSupplies, unfollowUser, type FollowedUser } from '../api/follow'
import { type SupplyResponse } from '../api/supply'
import { requireAuth } from '../utils/requireAuth'
import { openChatConversation } from '../api/chat'
import ChatDrawer from '../components/chat/ChatDrawer.vue'

const router = useRouter()
const loading = ref(false)

// 关注的用户列表
const followedUsers = ref<FollowedUser[]>([])

// 关注用户发布的供应信息
const supplies = ref<SupplyResponse[]>([])

// 选中的用户筛选（null 表示显示全部）
const selectedUserId = ref<number | null>(null)

// ChatDrawer 状态
const drawerOpen = ref(false)
const drawerConversationId = ref<number | null>(null)
const drawerPeerName = ref('')
const drawerSubjectSnapshotJson = ref<string | null>(null)
const drawerSubjectId = ref<number | null>(null)

// 筛选后的供应列表
const filteredSupplies = computed(() => {
  if (!selectedUserId.value) return supplies.value
  return supplies.value.filter(s => s.userId === selectedUserId.value)
})

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
      supplies.value = r.data || []
    }
  } catch (e) {
    console.error('加载供应信息失败', e)
  } finally {
    loading.value = false
  }
}

// 刷新数据
async function refreshData() {
  await Promise.all([loadFollowedUsers(), loadFollowedSupplies()])
  ElMessage.success('数据已刷新')
}

// 选择/取消选择用户筛选
function toggleUserFilter(userId: number) {
  if (selectedUserId.value === userId) {
    selectedUserId.value = null
  } else {
    selectedUserId.value = userId
  }
}

// 取消关注
async function handleUnfollow(user: FollowedUser) {
  try {
    const r = await unfollowUser(user.userId)
    if (r.code === 0) {
      ElMessage.success(`已取消关注 ${user.nickName || user.userName}`)
      await refreshData()
      if (selectedUserId.value === user.userId) {
        selectedUserId.value = null
      }
    }
  } catch (e) {
    ElMessage.error('取消关注失败')
  }
}

// 构建供应快照
function buildSupplySnapshot(s: SupplyResponse) {
  return JSON.stringify({
    snapshotTime: new Date().toLocaleString('zh-CN'),
    title: s.categoryName,
    categoryName: s.categoryName,
    companyName: s.companyName,
    nickName: s.nickName,
    exFactoryPrice: s.exFactoryPrice,
    quantity: s.quantity,
    remainingQuantity: s.remainingQuantity,
    shipAddress: s.shipAddress,
    deliveryMode: s.deliveryMode,
    packaging: s.packaging,
    paramsJson: s.paramsJson
  })
}

// 发起咨询
async function onConsult(s: SupplyResponse) {
  if (!requireAuth('/supply-browse')) return
  if (!s.userId || !s.id) {
    ElMessage.warning('该条供应暂不支持咨询')
    return
  }
  try {
    const res = await openChatConversation({
      peerUserId: s.userId,
      subjectType: 'SUPPLY',
      subjectId: s.id,
      subjectSnapshotJson: buildSupplySnapshot(s)
    })
    if (res.code !== 0 || !res.data) throw new Error(res.message)

    drawerConversationId.value = res.data
    drawerPeerName.value = s.nickName || s.userName || s.companyName || '对方'
    drawerSubjectId.value = s.id
    drawerSubjectSnapshotJson.value = buildSupplySnapshot(s)
    drawerOpen.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '发起咨询失败')
  }
}

function onDrawerClosed() {
  drawerConversationId.value = null
  drawerSubjectSnapshotJson.value = null
  drawerSubjectId.value = null
  drawerPeerName.value = ''
}

// 查看详情
function viewDetail(supply: SupplyResponse) {
  router.push({ path: '/hall/supply', query: { focusId: supply.id } })
}

// 跳转供应大厅
function goToHall() {
  router.push('/hall/supply')
}

// 解析参数 JSON
function parseParams(paramsJson?: string): Array<{name: string; value: string}> {
  if (!paramsJson) return []
  try {
    const obj = JSON.parse(paramsJson)
    // 支持 { params: {...} } 或直接 {...} 格式
    const params = obj.params || obj
    return Object.entries(params).map(([key, val]) => {
      // 新格式：{ name, value } 对象 - 优先使用对象内的 name 字段
      if (typeof val === 'object' && val !== null && 'name' in val && 'value' in val) {
        return { name: String((val as any).name), value: String((val as any).value) }
      }
      // 旧格式：键名是参数名，值是参数值
      return { name: key, value: String(val) }
    })
  } catch {
    return []
  }
}

// 格式化时间
function formatTime(timeStr?: string): string {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  return timeStr.split('T')[0] ?? timeStr
}

// 获取用户头像文字
function getAvatarText(user: FollowedUser): string {
  return (user.nickName || user.userName || '?')[0]
}

// 获取供应发布者头像
function getSupplyAvatar(s: SupplyResponse): string {
  return (s.nickName || s.companyName || '?')[0]
}

onMounted(() => {
  loadFollowedUsers()
  loadFollowedSupplies()
})
</script>

<template>
  <div class="space-y-6">
    <!-- 页面头部 -->
    <div class="flex items-end justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">我的关注</h1>
        <p class="text-gray-500 mt-1">追踪您关注的供应商最新动态</p>
      </div>
      <div class="flex items-center gap-3">
        <button 
          class="flex items-center gap-2 px-4 py-2 text-sm font-bold text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-xl transition-all "
          @click="refreshData"
        >
          <Refresh class="w-4 h-4" />
          刷新
        </button>
        <button 
          class="bg-brand-600 text-white px-5 py-2 rounded-xl font-bold hover:bg-brand-700 transition-all  flex items-center gap-2"
          @click="goToHall"
        >
          <Box class="w-4 h-4" />
          供应大厅
        </button>
      </div>
    </div>

    <!-- 关注商户头像列表 -->
    <section>
      <div class="flex items-center gap-3 mb-4">
        <span class="text-[10px] font-bold uppercase tracking-widest text-gray-400">关注的供应商</span>
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
            <UserFilled class="w-6 h-6" />
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
            ×
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
      <div v-else class="bg-white rounded-xl border border-gray-200 p-8 text-center">
        <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4">
          <UserFilled class="w-10 h-10 text-gray-300" />
        </div>
        <h3 class="text-lg font-bold text-gray-900 mb-2">还没有关注任何供应商</h3>
        <p class="text-sm text-gray-500 mb-6">前往供应大厅，发现优质供应商并关注他们</p>
        <button 
          class="px-6 py-2.5 bg-brand-600 text-white rounded-full font-bold hover:bg-brand-700 transition-all "
          @click="goToHall"
        >
          去供应大厅看看
        </button>
      </div>
    </section>

    <!-- 供应信息流 -->
    <section v-if="followedUsers.length > 0">
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center gap-3">
          <span class="text-[10px] font-bold uppercase tracking-widest text-gray-400">最新供应信息</span>
          <span v-if="selectedUserId" class="text-xs text-brand-600 bg-brand-50 px-2 py-0.5 rounded-full">
            已筛选
          </span>
        </div>
        <span class="text-xs text-gray-400">{{ filteredSupplies.length }} 条</span>
      </div>

      <div v-loading="loading" class="min-h-[200px]">
        <!-- 供应卡片网格 -->
        <div v-if="filteredSupplies.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="supply in filteredSupplies"
            :key="supply.id"
            class="group bg-white rounded-xl border border-gray-200 overflow-hidden transition-all hover:shadow-md hover:border-brand-100"
          >
            <!-- 卡片头部 -->
            <div class="p-5 bg-gradient-to-br from-brand-50/50 to-white border-b border-gray-50">
              <div class="flex items-center gap-3">
                <div class="w-12 h-12 bg-brand-600 text-white rounded-xl flex items-center justify-center font-bold text-lg shadow-md">
                  {{ getSupplyAvatar(supply) }}
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2">
                    <span class="font-bold text-gray-900 truncate">{{ supply.nickName || supply.userName || '供应商' }}</span>
                    <span class="text-[10px] font-bold uppercase tracking-widest text-brand-600 bg-brand-50 px-2 py-0.5 rounded-full border border-brand-100">
                      SELLER
                    </span>
                  </div>
                  <div class="text-xs text-gray-500 truncate mt-0.5">{{ supply.companyName || '个人商户' }}</div>
                </div>
              </div>
            </div>

            <!-- 卡片主体 -->
            <div class="p-5">
              <!-- 品类与价格 -->
              <div class="flex items-start justify-between mb-4">
                <div>
                  <div class="flex items-center gap-2 mb-1">
                    <Box class="w-4 h-4 text-brand-600" />
                    <h3 class="text-xl font-black text-gray-900">{{ supply.categoryName }}</h3>
                  </div>
                  <div class="text-xs text-gray-400">#{{ supply.id }} · {{ formatTime(supply.createTime) }}</div>
                </div>
                <div class="text-right">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-1">出厂单价</div>
                  <div v-if="supply.exFactoryPrice" class="text-xl font-black text-red-600">
                    ¥{{ supply.exFactoryPrice }}
                    <span class="text-xs font-normal text-gray-400">/吨</span>
                  </div>
                  <div v-else class="text-lg font-bold text-gray-400 italic">面议</div>
                </div>
              </div>

              <!-- 关键信息网格 -->
              <div class="grid grid-cols-2 gap-3 mb-4 bg-gray-50/50 rounded-xl p-4 border border-gray-100/50">
                <div>
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-1">库存量</div>
                  <div class="text-sm font-bold text-gray-900">{{ supply.quantity || '-' }} <span class="text-xs font-normal text-gray-500">吨</span></div>
                </div>
                <div>
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-1">交货方式</div>
                  <div class="text-sm font-medium text-gray-700">{{ supply.deliveryMode || '自提' }}</div>
                </div>
                <div>
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-1">包装方式</div>
                  <div class="text-sm font-medium text-gray-700">{{ supply.packaging || '散装' }}</div>
                </div>
                <div>
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-1">产地</div>
                  <div class="text-sm font-medium text-gray-700">{{ supply.origin || '-' }}</div>
                </div>
              </div>

              <!-- 发货地址 -->
              <div class="flex items-center gap-2 text-sm text-gray-500 mb-4">
                <Location class="w-4 h-4 text-gray-400 shrink-0" />
                <span class="truncate">{{ supply.shipAddress || '发货地址未指定' }}</span>
              </div>

              <!-- 指标参数 -->
              <div v-if="parseParams(supply.paramsJson).length > 0" class="flex flex-wrap gap-2">
                <span
                  v-for="param in parseParams(supply.paramsJson).slice(0, 4)"
                  :key="param.name"
                  class="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded-lg"
                >
                  {{ param.name }}: {{ param.value }}
                </span>
                <span
                  v-if="parseParams(supply.paramsJson).length > 4"
                  class="text-xs bg-gray-100 text-gray-400 px-2 py-1 rounded-lg"
                >
                  +{{ parseParams(supply.paramsJson).length - 4 }}
                </span>
              </div>
            </div>

            <!-- 卡片底部 -->
            <div class="px-5 py-4 bg-gray-50 border-t border-gray-200 flex items-center justify-end gap-3">
              <button 
                class="px-4 py-2 text-sm font-bold text-gray-600 hover:bg-gray-100 rounded-xl transition-all  flex items-center gap-2"
                @click="viewDetail(supply)"
              >
                <View class="w-4 h-4" />
                详情
              </button>
              <button 
                class="px-6 py-2 bg-brand-600 text-white rounded-xl font-bold text-sm shadow-md shadow-brand-200 hover:bg-brand-700 transition-all  flex items-center gap-2"
                @click="onConsult(supply)"
              >
                <ChatDotRound class="w-4 h-4" />
                立即咨询
              </button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else-if="!loading" class="bg-white rounded-xl border border-gray-200 p-12 text-center">
          <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4">
            <Box class="w-8 h-8 text-gray-300" />
          </div>
          <h3 class="text-lg font-bold text-gray-900 mb-2">暂无供应信息</h3>
          <p class="text-sm text-gray-500">
            {{ selectedUserId ? '该用户暂未发布供应信息' : '您关注的供应商暂未发布新的供应' }}
          </p>
        </div>
      </div>
    </section>

    <!-- 咨询抽屉 -->
    <ChatDrawer
      v-model="drawerOpen"
      :conversation-id="drawerConversationId"
      :peer-display-name="drawerPeerName"
      subject-type="SUPPLY"
      :subject-id="drawerSubjectId"
      :subject-snapshot-json="drawerSubjectSnapshotJson"
      @closed="onDrawerClosed"
    />
  </div>
</template>

<style scoped>
/* 隐藏滚动条但保持滚动功能 */
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
</style>
