<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import PublicFooter from '../components/PublicFooter.vue'
import ChatDrawer from '../components/chat/ChatDrawer.vue'
import CategorySidebar from '../components/CategorySidebar.vue'
import { listSupplies, type SupplyResponse } from '../api/supply'
import { batchGetFuturesPrices, type FuturesContractResponse } from '../api/futures'
import { getSchemaUnitConfig } from '../utils/schemaUnits'
import { useAuthStore } from '../store/auth'
import { Menu, MessageCircle } from 'lucide-vue-next'
import ProductInfoRow from '../components/ProductInfoRow.vue'
import { useHallFilters } from '../composables/useHallFilters'
import { useHallInteractions } from '../composables/useHallInteractions'

const router = useRouter()

const authStore = useAuthStore()

function go(path: string) {
  router.push(path)
}

// -- Shared composables --
const {
  searchKeyword, selectedCategory, currentPage, pageSize, total,
  schemaTree, selectedSchemaCode, mobileSidebarOpen,
  companyIdFilter, schemaCodeFromRoute, categoryNameFromRoute,
  loadSchemaTree, findSchemaCodeByCategory, handlePageChange, initFromRoute,
} = useHallFilters()

const {
  focusedId, focusIdFromRoute,
  loadFollowStatus, toggleFollow, isFollowingUser,
  setCardEl, applyFocusIfNeeded,
  drawerOpen, drawerConversationId, drawerPeerName,
  drawerSubjectSnapshotJson, drawerSubjectId,
  openConsultDrawer, onDrawerClosed,
} = useHallInteractions('/hall/supply')

// -- Local state --
const supplies = ref<SupplyResponse[]>([])
const listLoading = ref(false)

// 处理业态变化（来自侧边栏）
function onSchemaChange(schemaCode: string | null) {
  selectedSchemaCode.value = schemaCode
  currentPage.value = 1
  loadSupplies()
}

// 处理品类变化（来自侧边栏）
function onCategoryChange(categoryName: string | null) {
  selectedCategory.value = categoryName
  currentPage.value = 1
  loadSupplies()
  mobileSidebarOpen.value = false
}

const displaySupplies = computed(() => {
  if (focusIdFromRoute.value) return supplies.value
  const start = (currentPage.value - 1) * pageSize.value
  return supplies.value.slice(start, start + pageSize.value)
})

function buildSupplySnapshot(s: SupplyResponse) {
  return JSON.stringify({
    snapshotTime: new Date().toLocaleString('zh-CN'),
    title: s.categoryName,
    categoryName: s.categoryName,
    companyName: s.companyName,
    nickName: s.nickName,
    priceType: s.priceType,
    exFactoryPrice: s.exFactoryPrice,
    basisQuotes: s.basisQuotes,
    quantity: s.quantity,
    remainingQuantity: s.remainingQuantity,
    shipAddress: s.shipAddress,
    deliveryMode: s.deliveryMode,
    packaging: s.packaging,
    storageMethod: s.storageMethod,
    paramsJson: s.paramsJson,
    remark: s.remark
  })
}

async function onConsult(s: SupplyResponse) {
  await openConsultDrawer(s, 'SUPPLY', buildSupplySnapshot(s))
}

// 期货价格缓存
const futuresPriceCache = ref<Record<string, FuturesContractResponse>>({})

async function loadSupplies() {
  listLoading.value = true
  try {
    const params: any = { 
      activeOnly: true, 
      includeExpired: false, 
      orderBy: 'create_time', 
      order: 'desc' 
    }
    
    // 应用筛选条件
    if (selectedCategory.value) {
      params.categoryName = selectedCategory.value
    }

    if (selectedSchemaCode.value) {
      params.schemaCode = selectedSchemaCode.value
    }
    
    // 应用公司筛选（从地图跳转）
    if (companyIdFilter.value) {
      params.companyId = companyIdFilter.value
    }
    
    const res = await listSupplies(params)
    if (res.code !== 0) throw new Error(res.message)
    
    let result = res.data || []
    
    // 前端搜索过滤（关键词搜索 - 全字段匹配）
    if (searchKeyword.value.trim()) {
      const kw = searchKeyword.value.toLowerCase()
      result = result.filter(s => {
        // 基础字段
        if (s.categoryName?.toLowerCase().includes(kw)) return true
        if (s.companyName?.toLowerCase().includes(kw)) return true
        if (s.shipAddress?.toLowerCase().includes(kw)) return true
        if (s.nickName?.toLowerCase().includes(kw)) return true
        // 扩展字段：包装、付款、备注
        if (s.packaging?.toLowerCase().includes(kw)) return true
        if (s.paymentMethod?.toLowerCase().includes(kw)) return true
        if (s.remark?.toLowerCase().includes(kw)) return true
        // 质量参数 paramsJson（搜索参数名和参数值）
        if (s.paramsJson) {
          try {
            const params = JSON.parse(s.paramsJson)
            for (const [key, value] of Object.entries(params)) {
              if (key.toLowerCase().includes(kw) || String(value).toLowerCase().includes(kw)) {
                return true
              }
            }
          } catch { /* ignore */ }
        }
        return false
      })
    }
    
    supplies.value = result
    total.value = result.length
    
    // 加载关注状态
    const userIds = supplies.value.map(s => s.userId).filter(Boolean) as number[]
    const uniqueUserIds = [...new Set(userIds)]
    loadFollowStatus(uniqueUserIds)
    
    // 加载基差报价的期货价格
    await loadFuturesPrices(result)
  } catch {
    supplies.value = []
    total.value = 0
  } finally {
    listLoading.value = false
    applyFocusIfNeeded()
  }
}

// 加载基差报价需要的期货价格
async function loadFuturesPrices(supplyList: SupplyResponse[]) {
  // 收集所有基差报价中的合约代码
  const contractCodes = new Set<string>()
  for (const s of supplyList) {
    if (s.priceType === 1 && s.basisQuotes) {
      for (const bq of s.basisQuotes) {
        if (bq.contractCode) contractCodes.add(bq.contractCode)
      }
    }
  }
  
  if (contractCodes.size === 0) return
  
  try {
    const res = await batchGetFuturesPrices([...contractCodes])
    if (res.code === 0 && res.data) {
      futuresPriceCache.value = { ...futuresPriceCache.value, ...res.data }
    }
  } catch {
    // 静默失败
  }
}

// 获取合约的期货价格
function getFuturesPrice(contractCode: string): number | null {
  return futuresPriceCache.value[contractCode]?.lastPrice ?? null
}

// 计算核算价格（期货价 + 基差）
function calcReferencePrice(contractCode: string, basisPrice: number): number | null {
  const futuresPrice = getFuturesPrice(contractCode)
  if (futuresPrice === null) return null
  return futuresPrice + basisPrice
}

// 搜索
function onSearch() {
  currentPage.value = 1
  loadSupplies()
}

onMounted(() => {
  initFromRoute()
  loadSchemaTree()
  loadSupplies()
})

watch(focusIdFromRoute, () => {
  applyFocusIfNeeded()
})

// 监听 companyId 筛选变化
watch(companyIdFilter, () => {
  currentPage.value = 1
  loadSupplies()
})

// 监听 URL 中的 schemaCode 变化
watch(schemaCodeFromRoute, (newVal) => {
  if (newVal !== selectedSchemaCode.value) {
    selectedSchemaCode.value = newVal
    // 只有当 URL 中没有 categoryName 时才重置分类
    if (!categoryNameFromRoute.value) {
      selectedCategory.value = null
    }
    currentPage.value = 1
    loadSupplies()
  }
})

// 监听 URL 中的 categoryName 变化
watch(categoryNameFromRoute, (newVal) => {
  if (newVal !== selectedCategory.value) {
    selectedCategory.value = newVal
    currentPage.value = 1
    loadSupplies()
  }
})

// 监听搜索关键字变化，清空时自动恢复列表
watch(searchKeyword, (newVal, oldVal) => {
  // 当关键字从有值变为空时，自动重新加载
  if (oldVal && oldVal.trim() && !newVal.trim()) {
    currentPage.value = 1
    loadSupplies()
  }
})

// 获取供应的单位配置
function getSupplyUnitConfig(s: SupplyResponse) {
  const schemaCode = s.schemaCode || findSchemaCodeByCategory(s.categoryName)
  return getSchemaUnitConfig(schemaCode)
}

</script>

<template>
  <div class="bg-neutral-50 text-neutral-900 min-h-screen">
    <!-- 公司筛选提示 -->
    <div v-if="companyIdFilter" class="bg-brand-50 border-b border-brand-100">
      <div class="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
        <div class="flex items-center gap-2 text-brand-700">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" /></svg>
          <span class="text-sm font-medium">
            正在查看该公司的供应信息
          </span>
        </div>
        <button 
          class="px-3 py-1 bg-brand-100 hover:bg-brand-200 text-brand-700 rounded-lg text-sm font-medium transition-all"
          @click="router.push('/hall/supply')"
        >
          查看全部供应
        </button>
      </div>
    </div>

    <!-- 搜索区 -->
    <section class="bg-white border-b shadow-sm">
      <div class="max-w-7xl mx-auto px-4 py-4">
        <div class="flex gap-3">
          <!-- 移动端侧边栏切换按钮 -->
          <button
            class="lg:hidden flex items-center justify-center w-11 h-11 border-2 border-neutral-200 rounded-xl hover:border-brand-500 hover:text-brand-600 transition-all"
            @click="mobileSidebarOpen = true"
          >
            <Menu class="w-5 h-5" />
          </button>
          <div class="flex-1 relative">
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索品种、产地、指标或公司..."
              class="w-full border-2 border-neutral-200 rounded-xl py-2.5 px-10 focus:border-brand-500 outline-none transition-all"
              @keyup.enter="onSearch"
            />
            <svg class="w-5 h-5 absolute left-3 top-3 text-neutral-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
          </div>
          <button
            class="px-6 py-2.5 bg-brand-600 text-white rounded-xl font-bold hover:bg-brand-700 transition-all"
            @click="onSearch"
          >
            搜索
          </button>
        </div>
        <!-- 当前筛选状态 -->
        <div v-if="selectedSchemaCode || selectedCategory" class="flex items-center gap-2 mt-3 text-xs">
          <span class="text-neutral-400">当前筛选:</span>
          <span v-if="selectedSchemaCode" class="px-2 py-0.5 bg-brand-50 text-brand-700 rounded-full">
            {{ schemaTree.find(s => s.schemaCode === selectedSchemaCode)?.schemaName }}
          </span>
          <span v-if="selectedCategory" class="px-2 py-0.5 bg-brand-50 text-brand-700 rounded-full">
            {{ selectedCategory }}
          </span>
          <button
            class="text-neutral-400 hover:text-brand-600 ml-1"
            @click="selectedSchemaCode = null; selectedCategory = null; loadSupplies()"
          >
            清除
          </button>
        </div>
      </div>
    </section>

    <!-- 主体布局：侧边栏 + 列表 -->
    <div class="max-w-7xl mx-auto flex">
      <!-- 左侧边栏（桌面端） -->
      <aside class="hidden lg:block w-64 shrink-0 border-r border-neutral-100 bg-white sticky top-0 h-[calc(100vh-120px)] overflow-hidden">
        <CategorySidebar
          :schema-tree="schemaTree"
          v-model:selected-schema-code="selectedSchemaCode"
          v-model:selected-category="selectedCategory"
          theme="brand"
          @schema-change="onSchemaChange"
          @category-change="onCategoryChange"
        />
      </aside>

      <!-- 移动端侧边栏抽屉 -->
      <Teleport to="body">
        <Transition name="fade">
          <div
            v-if="mobileSidebarOpen"
            class="fixed inset-0 bg-black/50 z-40 lg:hidden"
            @click="mobileSidebarOpen = false"
          />
        </Transition>
        <Transition name="slide-left">
          <div
            v-if="mobileSidebarOpen"
            class="fixed left-0 top-0 bottom-0 w-72 bg-white z-50 lg:hidden shadow-2xl"
          >
            <div class="flex items-center justify-between p-4 border-b border-neutral-100">
              <span class="font-bold text-neutral-900">筛选条件</span>
              <button
                class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-neutral-100"
                @click="mobileSidebarOpen = false"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
              </button>
            </div>
            <CategorySidebar
              :schema-tree="schemaTree"
              v-model:selected-schema-code="selectedSchemaCode"
              v-model:selected-category="selectedCategory"
              theme="brand"
              @schema-change="onSchemaChange"
              @category-change="onCategoryChange"
            />
          </div>
        </Transition>
      </Teleport>

      <!-- 右侧列表区 -->
      <main class="flex-1 px-4 py-6 min-w-0">
      <div class="space-y-4">
        <div v-if="listLoading" class="bg-white rounded-xl border border-neutral-200 p-8 text-neutral-400 text-sm">
          正在加载货源...
        </div>

          <!-- 供应卡片 -->
          <div
            v-for="s in displaySupplies"
            :key="s.id"
            :ref="el => setCardEl(Number(s.id), el as any)"
            class="bg-white rounded-xl border border-neutral-200 overflow-hidden transition-all hover:shadow-md hover:border-brand-200"
            :class="{ 'ring-2 ring-brand-500 shadow-lg': focusedId === s.id }"
          >
            <!-- 头部：公司信息 + 操作 -->
            <div class="flex items-center gap-3 px-4 py-3 border-b border-neutral-100 bg-neutral-50/50">
              <div class="w-9 h-9 rounded-lg bg-brand-100 flex items-center justify-center text-brand-600 shrink-0">
                <span class="text-sm font-bold">{{ (s.companyName || s.nickName || '户')[0] }}</span>
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2">
                  <span
                    class="font-bold text-neutral-900 truncate hover:text-brand-600 cursor-pointer transition-colors"
                    @click.stop="go(`/companies/${s.companyId}`)"
                  >{{ s.companyName || '个人用户' }}</span>
                  <span class="text-neutral-300">·</span>
                  <span class="text-xs text-neutral-500 truncate">
                    <template v-if="s.position">{{ s.position }} · </template>{{ s.nickName || s.userName || '' }}
                  </span>
                </div>
              </div>
              <!-- 关注按钮 -->
              <button
                v-if="authStore.token && s.userId"
                class="shrink-0 text-[10px] font-bold px-2.5 py-1 rounded-full border transition-all active:scale-95"
                :class="isFollowingUser(s.userId)
                  ? 'bg-brand-50 text-brand-600 border-brand-200'
                  : 'bg-white text-neutral-500 border-neutral-200 hover:border-brand-300 hover:text-brand-600'"
                @click.stop="toggleFollow(s)"
              >
                {{ isFollowingUser(s.userId) ? '已关注' : '+ 关注' }}
              </button>
              <!-- 咨询按钮 -->
              <button
                class="shrink-0 flex items-center gap-1.5 px-4 py-1.5 bg-brand-600 text-white text-xs font-bold rounded-lg hover:bg-brand-700 transition-all active:scale-95"
                @click.stop="onConsult(s)"
              >
                <MessageCircle class="w-3.5 h-3.5" />
                立即咨询
              </button>
            </div>

            <!-- 主体：产品信息 -->
            <div class="p-4">
              <ProductInfoRow
                :data="{
                  categoryName: s.categoryName,
                  quantity: s.quantity,
                  quantityUnit: getSupplyUnitConfig(s).quantityUnit,
                  price: s.priceType === 1 ? '基差报价' : s.exFactoryPrice,
                  priceUnit: getSupplyUnitConfig(s).priceUnit.replace('/', ''),
                  address: s.shipAddress,
                  packaging: s.packaging,
                  paymentMethod: s.paymentMethod,
                  paramsJson: s.paramsJson,
                  expireTime: s.expireTime
                }"
                type="supply"
                :show-header="true"
                :show-icon="false"
              />
              <!-- 基差报价详情（仅基差类型显示） -->
              <div v-if="s.priceType === 1 && s.basisQuotes?.length" class="mt-3 flex flex-wrap gap-2">
                <div
                  v-for="bq in (s.basisQuotes || []).slice(0, 3)"
                  :key="bq.id"
                  class="inline-flex items-center gap-2 px-2.5 py-1.5 bg-warning-50 border border-warning-200 rounded-lg text-xs"
                >
                  <span class="font-bold text-neutral-700">{{ bq.contractName || bq.contractCode }}</span>
                  <span :class="bq.basisPrice >= 0 ? 'text-red-500' : 'text-green-500'" class="font-bold">
                    {{ bq.basisPrice >= 0 ? '+' : '' }}{{ bq.basisPrice }}
                  </span>
                  <span class="text-neutral-400">→</span>
                  <span class="font-black text-brand-600">
                    ¥{{ calcReferencePrice(bq.contractCode, bq.basisPrice)?.toFixed(0) || '-' }}
                  </span>
                  <span class="text-neutral-400">·</span>
                  <span class="font-medium text-neutral-600">{{ bq.remainingQty ?? bq.availableQty }}吨</span>
                </div>
              </div>
            </div>
          </div>

        <div v-if="!listLoading && supplies.length === 0" class="bg-white rounded-xl border border-neutral-200 p-8 text-center">
          <div class="text-neutral-400 text-sm mb-2">暂无货源数据</div>
          <div class="text-xs text-neutral-300">
            {{ selectedCategory ? `没有找到「${selectedCategory}」相关的供应信息` : '请尝试调整筛选条件' }}
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="flex justify-center mt-10">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="handlePageChange"
        />
      </div>
      
      <!-- 数据统计 -->
      <div v-if="total > 0" class="text-center mt-4 text-xs text-neutral-400">
        共 {{ total }} 条供应信息
        <span v-if="selectedCategory" class="ml-2">· 当前筛选：{{ selectedCategory }}</span>
      </div>
      </main>
    </div>

    <PublicFooter />

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
.filter-tag:hover {
  border-color: #84BB9F;
  color: #84BB9F;
}
.filter-tag.active {
  background-color: #84BB9F;
  color: white;
  border-color: #84BB9F;
}
.supply-card:hover {
  border-color: #84BB9F;
  box-shadow: 0 4px 12px rgba(132, 187, 159, 0.1);
}

/* 侧边栏过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-left-enter-active,
.slide-left-leave-active {
  transition: transform 0.3s ease;
}
.slide-left-enter-from,
.slide-left-leave-to {
  transform: translateX(-100%);
}
</style>


