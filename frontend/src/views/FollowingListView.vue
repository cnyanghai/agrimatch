<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from '@/composables/useToast'
import { Search, Heart, MessageCircle, FileText, ExternalLink } from 'lucide-vue-next'
import { getFollowedUsers, getFollowedSupplies, getFollowedRequirements, unfollowUser, type FollowedUser } from '../api/follow'
import { openChatConversation } from '../api/chat'
import { useAuthStore } from '../store/auth'
import ProductInfoRow from '../components/ProductInfoRow.vue'

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

// 获取用户分类
function getUserCategory(user: FollowedUser) {
  // 根据用户的供应或采购数据判断类型（优先判断供应商）
  const hasSupply = followedSupplies.value.some(s => s.userId === user.userId)
  const hasRequirement = followedRequirements.value.some(r => r.userId === user.userId)

  if (hasSupply) return { text: '供应商', class: 'text-brand-700' }
  if (hasRequirement) return { text: '采购商', class: 'text-autumn-700' }
  return { text: '商户', class: 'text-neutral-700' }
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
      showToast.success(`已取消关注 ${selectedUser.value.nickName || selectedUser.value.userName}`)
      selectedUser.value = null
      await loadData()
    } else {
      throw new Error(r.message)
    }
  } catch (e: any) {
    showToast.error(e?.message ?? '取消关注失败')
  }
}

// 联系商家（供应）
async function contactForSupply(supply: any) {
  if (!authStore.me) {
    showToast.warning('请先登录')
    return
  }
  if (!supply.userId) {
    showToast.warning('无法联系该商户')
    return
  }
  try {
    const res = await openChatConversation({
      peerUserId: supply.userId,
      subjectType: 'SUPPLY',
      subjectId: supply.id,
      subjectSnapshotJson: JSON.stringify({
        productName: supply.categoryName,
        categoryName: supply.categoryName,
        companyName: supply.companyName,
        supplyNo: supply.supplyNo,
        exFactoryPrice: supply.exFactoryPrice,
        quantity: supply.quantity,
        remainingQuantity: supply.remainingQuantity ?? supply.quantity,
        shipAddress: supply.shipAddress,
        deliveryMode: supply.deliveryMode,
        packaging: supply.packaging,
        paymentMethod: supply.paymentMethod,
        invoiceType: supply.invoiceType,
        storageMethod: supply.storageMethod,
        paramsJson: supply.paramsJson,
        priceType: supply.priceType,
        basisQuotes: supply.basisQuotes,
      })
    })
    if (res.code === 0 && res.data) {
      router.push({ path: '/chat', query: { conversationId: String(res.data) } })
    } else {
      showToast.error(res.message || '打开聊天失败')
    }
  } catch (e: any) {
    showToast.error(e?.message || '联系商家失败')
  }
}

// 联系商家（采购需求）
async function contactForRequirement(req: any) {
  if (!authStore.me) {
    showToast.warning('请先登录')
    return
  }
  if (!req.userId) {
    showToast.warning('无法联系该商户')
    return
  }
  try {
    const res = await openChatConversation({
      peerUserId: req.userId,
      subjectType: 'REQUIREMENT',
      subjectId: req.id,
      subjectSnapshotJson: JSON.stringify({
        productName: req.categoryName,
        categoryName: req.categoryName,
        companyName: req.companyName,
        expectedPrice: req.budgetPrice ?? req.expectedPrice,
        quantity: req.quantity,
        purchaseAddress: req.purchaseAddress ?? req.address,
        deliveryMode: req.deliveryMode,
        paymentMethod: req.paymentMethod,
        paramsJson: req.paramsJson,
      })
    })
    if (res.code === 0 && res.data) {
      router.push({ path: '/chat', query: { conversationId: String(res.data) } })
    } else {
      showToast.error(res.message || '打开聊天失败')
    }
  } catch (e: any) {
    showToast.error(e?.message || '联系商家失败')
  }
}

// 格式化价格
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
      selectedUser.value = followedUsers.value[0] ?? null
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
  } catch {
    // silently ignore
  }
}

// 加载关注用户的供应信息
async function loadFollowedSupplies() {
  try {
    const r = await getFollowedSupplies()
    if (r.code === 0) {
      followedSupplies.value = r.data || []
    }
  } catch {
    // silently ignore
  }
}

// 加载关注用户的采购需求
async function loadFollowedRequirements() {
  try {
    const r = await getFollowedRequirements()
    if (r.code === 0) {
      followedRequirements.value = r.data || []
    }
  } catch {
    // silently ignore
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
          <h2 class="text-2xl font-bold text-neutral-900">关注列表</h2>
          <span class="bg-brand-100 text-brand-700 text-[10px] px-1.5 py-0.5 rounded-full font-bold">
            {{ followedUsers.length }}
          </span>
      </div>
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
              <h3 class="text-base font-bold text-neutral-900">
                {{ user.companyName || user.nickName || user.userName }}
              </h3>
              <span :class="['text-[10px] px-1.5 py-0.5 rounded font-bold', getUserCategory(user).class.replace('text-', 'bg-').replace('-700', '-100'), getUserCategory(user).class]">
                {{ getUserCategory(user).text }}
              </span>
            </div>
            <p class="text-xs text-slate-500 line-clamp-1">
              {{ user.nickName || user.userName }}
              <span v-if="user.companyName && user.nickName"> · {{ user.companyName }}</span>
            </p>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="flex flex-col items-center justify-center py-16 px-4 text-center">
          <div class="w-16 h-16 bg-neutral-50 rounded-full flex items-center justify-center mb-4">
            <Heart class="w-8 h-8 text-neutral-300" />
          </div>
          <h3 class="text-2xl font-bold text-neutral-900 mb-1">暂无关注</h3>
          <p class="text-xs text-neutral-500">
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
                <img v-if="selectedUser.avatar" :src="selectedUser.avatar" alt="头像" class="w-full h-full object-cover" />
                <span v-else class="text-3xl font-black text-brand-700">{{ getAvatarText(selectedUser) }}</span>
              </div>
              <div>
                <h2 class="text-2xl font-bold text-neutral-900 mb-2">
                  {{ selectedUser.companyName || selectedUser.nickName || selectedUser.userName }}
                </h2>
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
              <button
                v-if="selectedUser.companyId"
                class="border border-slate-200 text-slate-600 px-5 py-2.5 rounded font-bold text-sm hover:bg-slate-50 transition-colors flex items-center gap-2"
                @click="router.push(`/companies/${selectedUser.companyId}`)"
              >
                <ExternalLink class="w-4 h-4" />
                查看主页
              </button>
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
        <div class="p-6 space-y-6 overflow-y-auto" style="max-height: calc(100vh - 280px);">
          <!-- 供应信息 -->
          <section v-if="selectedUserSupplies.length > 0" class="bg-white rounded-lg border border-slate-200 shadow-sm overflow-hidden">
            <div class="px-5 py-3 border-b border-slate-200 flex justify-between items-center bg-slate-50/50">
              <h3 class="text-base font-bold text-neutral-900 flex items-center gap-2">
                <FileText class="w-4 h-4 text-brand-700" />
                供应信息 ({{ selectedUserSupplies.length }})
              </h3>
            </div>
            <div class="p-4 space-y-3">
              <div
                v-for="supply in selectedUserSupplies"
                :key="supply.id"
                class="bg-white rounded-xl border border-neutral-200 p-4 hover:shadow-md hover:border-brand-200 transition-all duration-200"
              >
                <ProductInfoRow
                  :data="{
                    categoryName: supply.categoryName || '未知品类',
                    quantity: supply.quantity,
                    quantityUnit: '吨',
                    price: supply.priceType === 1 ? '基差报价' : supply.exFactoryPrice,
                    priceUnit: '吨',
                    address: supply.shipAddress,
                    packaging: supply.packaging,
                    paymentMethod: supply.paymentMethod,
                    paramsJson: supply.paramsJson,
                    expireTime: supply.expireTime
                  }"
                  type="supply"
                >
                  <!-- 基差报价详情 -->
                  <template v-if="supply.priceType === 1 && supply.basisQuotes?.length" #extra>
                    <div class="mt-2 flex flex-wrap gap-2">
                      <div
                        v-for="bq in (supply.basisQuotes || []).slice(0, 3)"
                        :key="bq.id"
                        class="inline-flex items-center gap-1.5 px-2 py-1 bg-warning-50 border border-warning-200 rounded-lg text-xs"
                      >
                        <span class="font-bold text-neutral-700">{{ bq.contractName || bq.contractCode }}</span>
                        <span :class="bq.basisPrice >= 0 ? 'text-red-500' : 'text-green-500'" class="font-bold">
                          {{ bq.basisPrice >= 0 ? '+' : '' }}{{ bq.basisPrice }}
                        </span>
                        <span class="text-neutral-400">·</span>
                        <span class="font-medium text-neutral-600">{{ bq.remainingQty ?? bq.availableQty }}吨</span>
                      </div>
                    </div>
                  </template>
                  <template #actions>
                    <button
                      class="flex items-center gap-1.5 px-3 py-1.5 bg-brand-600 hover:bg-brand-700 text-white text-xs font-medium rounded-lg transition-all active:scale-95"
                      @click="contactForSupply(supply)"
                    >
                      <MessageCircle class="w-3.5 h-3.5" />
                      联系洽谈
                    </button>
                  </template>
                </ProductInfoRow>
              </div>
            </div>
          </section>

          <!-- 采购需求 -->
          <section v-if="selectedUserRequirements.length > 0" class="bg-white rounded-lg border border-slate-200 shadow-sm overflow-hidden">
            <div class="px-5 py-3 border-b border-slate-200 flex justify-between items-center bg-slate-50/50">
              <h3 class="text-base font-bold text-neutral-900 flex items-center gap-2">
                <FileText class="w-4 h-4 text-autumn-600" />
                采购需求 ({{ selectedUserRequirements.length }})
              </h3>
            </div>
            <div class="p-4 space-y-3">
              <div
                v-for="req in selectedUserRequirements"
                :key="req.id"
                class="bg-white rounded-xl border border-neutral-200 p-4 hover:shadow-md hover:border-autumn-200 transition-all duration-200"
              >
                <ProductInfoRow
                  :data="{
                    categoryName: req.categoryName || '未知品类',
                    quantity: req.quantity,
                    quantityUnit: '吨',
                    price: req.budgetPrice ?? req.expectedPrice,
                    priceUnit: '吨',
                    address: req.purchaseAddress ?? req.address,
                    packaging: req.packaging,
                    paymentMethod: req.paymentMethod,
                    paramsJson: req.paramsJson,
                    expireTime: req.expireTime
                  }"
                  type="purchase"
                >
                  <template #actions>
                    <button
                      class="flex items-center gap-1.5 px-3 py-1.5 bg-autumn-600 hover:bg-autumn-700 text-white text-xs font-medium rounded-lg transition-all active:scale-95"
                      @click="contactForRequirement(req)"
                    >
                      <MessageCircle class="w-3.5 h-3.5" />
                      联系洽谈
                    </button>
                  </template>
                </ProductInfoRow>
              </div>
            </div>
          </section>

          <!-- 空状态 -->
          <div
            v-if="selectedUserSupplies.length === 0 && selectedUserRequirements.length === 0"
            class="bg-white rounded-lg border border-slate-200 p-12 text-center"
          >
            <div class="w-16 h-16 bg-neutral-50 rounded-full flex items-center justify-center mx-auto mb-4">
              <FileText class="w-8 h-8 text-neutral-300" />
            </div>
            <h3 class="text-lg font-bold text-neutral-900 mb-1">暂无动态</h3>
            <p class="text-xs text-neutral-500">该商户暂未发布供应或采购信息</p>
          </div>
        </div>
      </div>

      <!-- 未选中用户时的空状态 -->
      <div v-else class="flex-1 flex items-center justify-center">
        <div class="text-center">
          <div class="w-24 h-24 bg-neutral-50 rounded-full flex items-center justify-center mx-auto mb-6">
            <Heart class="w-12 h-12 text-neutral-300" />
          </div>
          <h3 class="text-2xl font-bold text-neutral-900 mb-2">选择一个商户查看详情</h3>
          <p class="text-sm text-neutral-500">从左侧列表中选择您关注的商户</p>
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

