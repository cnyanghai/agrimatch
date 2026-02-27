<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import PublicFooter from '../components/PublicFooter.vue'
import ChatDrawer from '../components/chat/ChatDrawer.vue'
import CategorySidebar from '../components/CategorySidebar.vue'
import { listRequirements, type RequirementResponse } from '../api/requirement'
import { getSchemaUnitConfig } from '../utils/schemaUnits'
import { useAuthStore } from '../store/auth'
import { Menu, MessageCircle } from 'lucide-vue-next'
import ProductInfoRow from '../components/ProductInfoRow.vue'
import { useHallFilters } from '../composables/useHallFilters'
import { useHallInteractions } from '../composables/useHallInteractions'

const authStore = useAuthStore()
const router = useRouter()


function go(path: string) {
  router.push(path)
}

// -- Shared composables --
const {
  searchKeyword, selectedCategory, currentPage, pageSize, total,
  schemaTree, selectedSchemaCode, mobileSidebarOpen,
  companyIdFilter, schemaCodeFromRoute,
  loadSchemaTree, findSchemaCodeByCategory, handlePageChange, initFromRoute,
} = useHallFilters()

const {
  focusedId, focusIdFromRoute,
  loadFollowStatus, toggleFollow, isFollowingUser,
  setCardEl, applyFocusIfNeeded,
  drawerOpen, drawerConversationId, drawerPeerName,
  drawerSubjectSnapshotJson, drawerSubjectId,
  openConsultDrawer, onDrawerClosed,
} = useHallInteractions('/hall/need')

// -- Local state --
const requirements = ref<RequirementResponse[]>([])
const listLoading = ref(false)

// 处理业态变化（来自侧边栏）
function onSchemaChange(schemaCode: string | null) {
  selectedSchemaCode.value = schemaCode
  currentPage.value = 1
  loadRequirements()
}

// 处理品类变化（来自侧边栏）
function onCategoryChange(categoryName: string | null) {
  selectedCategory.value = categoryName
  currentPage.value = 1
  loadRequirements()
  mobileSidebarOpen.value = false
}

const displayRequirements = computed(() => {
  if (focusIdFromRoute.value) return requirements.value
  const start = (currentPage.value - 1) * pageSize.value
  return requirements.value.slice(start, start + pageSize.value)
})

function buildNeedSnapshot(r: RequirementResponse) {
  return JSON.stringify({
    snapshotTime: new Date().toLocaleString('zh-CN'),
    title: r.categoryName,
    categoryName: r.categoryName,
    companyName: r.companyName,
    nickName: r.nickName,
    expectedPrice: r.expectedPrice,
    quantity: r.quantity,
    remainingQuantity: r.remainingQuantity,
    purchaseAddress: r.purchaseAddress,
    paymentMethod: r.paymentMethod,
    deliveryMethod: r.deliveryMethod,
    packaging: r.packaging,
    invoiceType: r.invoiceType,
    paramsJson: r.paramsJson,
    remark: r.remark
  })
}

async function onQuote(r: RequirementResponse) {
  await openConsultDrawer(r, 'REQUIREMENT', buildNeedSnapshot(r))
}

async function loadRequirements() {
  listLoading.value = true
  try {
    const params: any = { 
      status: 0, 
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
    
    const res = await listRequirements(params)
    if (res.code !== 0) throw new Error(res.message)
    
    let result = res.data || []
    
    // 前端搜索过滤（关键词搜索 - 全字段匹配）
    if (searchKeyword.value.trim()) {
      const kw = searchKeyword.value.toLowerCase()
      result = result.filter(r => {
        // 基础字段
        if (r.categoryName?.toLowerCase().includes(kw)) return true
        if (r.companyName?.toLowerCase().includes(kw)) return true
        if (r.purchaseAddress?.toLowerCase().includes(kw)) return true
        if (r.nickName?.toLowerCase().includes(kw)) return true
        // 扩展字段：包装、付款、备注、交货方式
        if (r.packaging?.toLowerCase().includes(kw)) return true
        if (r.paymentMethod?.toLowerCase().includes(kw)) return true
        if (r.remark?.toLowerCase().includes(kw)) return true
        if (r.deliveryMethod?.toLowerCase().includes(kw)) return true
        // 质量参数 paramsJson（搜索参数名和参数值）
        if (r.paramsJson) {
          try {
            const params = JSON.parse(r.paramsJson)
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
    
    requirements.value = result
    total.value = result.length
    
    // 加载关注状态
    const userIds = requirements.value.map(r => r.userId).filter(Boolean) as number[]
    const uniqueUserIds = [...new Set(userIds)]
    loadFollowStatus(uniqueUserIds)
  } catch {
    requirements.value = []
    total.value = 0
  } finally {
    listLoading.value = false
    applyFocusIfNeeded()
  }
}

// 搜索
function onSearch() {
  currentPage.value = 1
  loadRequirements()
}

onMounted(() => {
  initFromRoute()
  loadSchemaTree()
  loadRequirements()
})

// 监听 companyId 筛选变化
watch(companyIdFilter, () => {
  currentPage.value = 1
  loadRequirements()
})

watch(focusIdFromRoute, () => {
  applyFocusIfNeeded()
})

// 监听 URL 中的业态筛选变化（从首页跳转）
watch(schemaCodeFromRoute, (newVal) => {
  if (newVal !== selectedSchemaCode.value) {
    selectedSchemaCode.value = newVal
    selectedCategory.value = null
    currentPage.value = 1
    loadRequirements()
  }
})

// 监听搜索关键字变化，清空时自动恢复列表
watch(searchKeyword, (newVal, oldVal) => {
  // 当关键字从有值变为空时，自动重新加载
  if (oldVal && oldVal.trim() && !newVal.trim()) {
    currentPage.value = 1
    loadRequirements()
  }
})

// 获取需求的单位配置
function getRequirementUnitConfig(r: RequirementResponse) {
  const schemaCode = findSchemaCodeByCategory(r.categoryName)
  return getSchemaUnitConfig(schemaCode)
}

</script>

<template>
  <div class="bg-neutral-50 text-neutral-900 min-h-screen">

    <!-- 公司筛选提示 -->
    <div v-if="companyIdFilter" class="bg-autumn-50 border-b border-autumn-100">
      <div class="max-w-7xl mx-auto px-4 py-3 flex items-center justify-between">
        <div class="flex items-center gap-2 text-autumn-700">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" /></svg>
          <span class="text-sm font-medium">
            正在查看该公司的采购需求
          </span>
        </div>
        <button
          class="px-3 py-1 bg-autumn-100 hover:bg-autumn-200 text-autumn-700 rounded-lg text-sm font-medium transition-all"
          @click="router.push('/hall/need')"
        >
          查看全部需求
        </button>
      </div>
    </div>

    <!-- 搜索区 -->
    <section class="bg-white border-b shadow-sm">
      <div class="max-w-7xl mx-auto px-4 py-4">
        <div class="flex gap-3">
          <!-- 移动端侧边栏切换按钮 -->
          <button
            class="lg:hidden flex items-center justify-center w-11 h-11 border-2 border-neutral-200 rounded-xl hover:border-autumn-500 hover:text-autumn-600 transition-all"
            @click="mobileSidebarOpen = true"
          >
            <Menu class="w-5 h-5" />
          </button>
          <div class="flex-1 relative">
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索您想供应的品种、求购区域或指标要求..."
              class="w-full border-2 border-neutral-200 rounded-xl py-2.5 px-10 focus:border-autumn-500 outline-none transition-all"
              @keyup.enter="onSearch"
            />
            <svg class="w-5 h-5 absolute left-3 top-3 text-neutral-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
          </div>
          <button
            class="px-6 py-2.5 bg-autumn-600 text-white rounded-xl font-bold hover:bg-autumn-700 transition-all"
            @click="onSearch"
          >
            搜索
          </button>
        </div>
        <!-- 当前筛选状态 -->
        <div v-if="selectedSchemaCode || selectedCategory" class="flex items-center gap-2 mt-3 text-xs">
          <span class="text-neutral-400">当前筛选:</span>
          <span v-if="selectedSchemaCode" class="px-2 py-0.5 bg-autumn-50 text-autumn-700 rounded-full">
            {{ schemaTree.find(s => s.schemaCode === selectedSchemaCode)?.schemaName }}
          </span>
          <span v-if="selectedCategory" class="px-2 py-0.5 bg-autumn-50 text-autumn-700 rounded-full">
            {{ selectedCategory }}
          </span>
          <button
            class="text-neutral-400 hover:text-autumn-600 ml-1"
            @click="selectedSchemaCode = null; selectedCategory = null; loadRequirements()"
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
          theme="autumn"
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
              theme="autumn"
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
          正在加载需求...
        </div>

        <!-- 采购卡片 -->
        <div
          v-for="r in displayRequirements"
          :key="r.id"
          :ref="(el) => setCardEl(Number(r.id), el as any)"
          class="bg-white rounded-xl border border-neutral-200 overflow-hidden transition-all hover:shadow-md hover:border-autumn-200"
          :class="{ 'ring-2 ring-autumn-500 shadow-lg': focusedId === r.id }"
        >
          <!-- 头部：公司信息 + 操作 -->
          <div class="flex items-center gap-3 px-4 py-3 border-b border-neutral-100 bg-neutral-50/50">
            <div class="w-9 h-9 rounded-lg bg-autumn-100 flex items-center justify-center text-autumn-600 shrink-0">
              <span class="text-sm font-bold">{{ (r.companyName || r.nickName || '采')[0] }}</span>
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2">
                <span
                  class="font-bold text-neutral-900 truncate hover:text-autumn-600 cursor-pointer transition-colors"
                  @click.stop="go(`/companies/${r.companyId}`)"
                >{{ r.companyName || '未填写公司' }}</span>
                <span class="text-neutral-300">·</span>
                <span class="text-xs text-neutral-500 truncate">
                  <template v-if="r.position">{{ r.position }} · </template>{{ r.nickName || r.userName || '' }}
                </span>
              </div>
            </div>
            <!-- 关注按钮 -->
            <button
              v-if="authStore.token && r.userId"
              class="shrink-0 text-[10px] font-bold px-2.5 py-1 rounded-full border transition-all active:scale-95"
              :class="isFollowingUser(r.userId)
                ? 'bg-autumn-50 text-autumn-600 border-autumn-200'
                : 'bg-white text-neutral-500 border-neutral-200 hover:border-autumn-300 hover:text-autumn-600'"
              @click.stop="toggleFollow(r)"
            >
              {{ isFollowingUser(r.userId) ? '已关注' : '+ 关注' }}
            </button>
            <!-- 报价按钮 -->
            <button
              class="shrink-0 flex items-center gap-1.5 px-4 py-1.5 bg-autumn-600 text-white text-xs font-bold rounded-lg hover:bg-autumn-700 transition-all active:scale-95"
              @click.stop="onQuote(r)"
            >
              <MessageCircle class="w-3.5 h-3.5" />
              立即报价
            </button>
          </div>

          <!-- 主体：产品信息 -->
          <div class="p-4">
            <ProductInfoRow
              :data="{
                categoryName: r.categoryName,
                quantity: r.quantity,
                quantityUnit: getRequirementUnitConfig(r).quantityUnit,
                price: r.expectedPrice,
                priceUnit: getRequirementUnitConfig(r).priceUnit.replace('/', ''),
                priceLabel: '期望价',
                address: r.purchaseAddress,
                addressLabel: '收货地',
                packaging: r.packaging,
                paymentMethod: r.paymentMethod,
                paramsJson: r.paramsJson,
                expireTime: r.expireTime
              }"
              type="purchase"
              :show-header="true"
              :show-icon="false"
            />
          </div>
        </div>

        <div v-if="!listLoading && requirements.length === 0" class="bg-white rounded-xl border border-neutral-200 p-8 text-center">
          <div class="text-neutral-400 text-sm mb-2">暂无采购需求</div>
          <div class="text-xs text-neutral-300">
            {{ selectedCategory ? `没有找到「${selectedCategory}」相关的采购需求` : '请尝试调整筛选条件' }}
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
        共 {{ total }} 条采购需求
        <span v-if="selectedCategory" class="ml-2">· 当前筛选：{{ selectedCategory }}</span>
      </div>
      </main>
    </div>

    <PublicFooter />

    <ChatDrawer
      v-model="drawerOpen"
      :conversation-id="drawerConversationId"
      :peer-display-name="drawerPeerName"
      subject-type="NEED"
      :subject-id="drawerSubjectId"
      :subject-snapshot-json="drawerSubjectSnapshotJson"
      @closed="onDrawerClosed"
    />
  </div>
</template>

<style scoped>
.filter-tag:hover {
  border-color: #A5CCDC;
  color: #6BA3B7;
}
.filter-tag.active {
  background-color: #A5CCDC;
  color: white;
  border-color: #A5CCDC;
}
.purchase-card:hover {
  border-color: #A5CCDC;
  box-shadow: 0 4px 12px rgba(165, 204, 220, 0.15);
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


