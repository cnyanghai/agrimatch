<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { requireAuth } from '../utils/requireAuth'
import PublicFooter from '../components/PublicFooter.vue'
import ChatDrawer from '../components/chat/ChatDrawer.vue'
import { listRequirements, type RequirementResponse } from '../api/requirement'
import { openChatConversation } from '../api/chat'
import { followUser, unfollowUser, checkFollowStatus } from '../api/follow'
import { getSchemaTree, type ProductSchemaVO, type CategoryNode } from '../api/productSchema'
import { getSchemaUnitConfig } from '../utils/schemaUnits'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'

const authStore = useAuthStore()

const router = useRouter()
const route = useRoute()

function go(path: string) {
  router.push(path)
}

function onPublishNeed() {
  if (!requireAuth('/requirements')) return
  go('/requirements')
}

const requirements = ref<RequirementResponse[]>([])
const listLoading = ref(false)
const focusedId = ref<number | null>(null)
const cardEls = new Map<number, HTMLElement>()
let focusTimer: number | null = null

// 筛选条件
const searchKeyword = ref('')
const selectedCategory = ref<string | null>(null)

// 从 URL 读取公司筛选参数
const companyIdFilter = computed(() => {
  const raw = route.query.companyId
  const s = Array.isArray(raw) ? raw[0] : raw
  const n = s ? Number(s) : NaN
  return Number.isFinite(n) ? n : null
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 业态与品类数据（从API加载）
const schemaTree = ref<ProductSchemaVO[]>([])
const selectedSchemaCode = ref<string | null>(null)

// 获取当前业态下的常见品类（用于快速筛选）
const quickCategoryOptions = computed(() => {
  if (!selectedSchemaCode.value) {
    // 未选择业态时，显示饲料原料的常见品类
    const feedSchema = schemaTree.value.find(s => s.schemaCode === 'feed')
    if (feedSchema?.categories) {
      return flattenCategories(feedSchema.categories).slice(0, 8)
    }
    return []
  }
  const schema = schemaTree.value.find(s => s.schemaCode === selectedSchemaCode.value)
  if (schema?.categories) {
    return flattenCategories(schema.categories).slice(0, 8)
  }
  return []
})

// 扁平化分类树
function flattenCategories(nodes: CategoryNode[]): string[] {
  const result: string[] = []
  function traverse(list: CategoryNode[]) {
    for (const node of list) {
      if (node.children && node.children.length > 0) {
        traverse(node.children)
      } else {
        result.push(node.name)
      }
    }
  }
  traverse(nodes)
  return result
}

// 加载业态树
async function loadSchemaTree() {
  try {
    const r = await getSchemaTree()
    if (r.code === 0 && r.data) {
      schemaTree.value = r.data
    }
  } catch {
    // 静默失败
  }
}

// 选择业态
function selectSchema(schemaCode: string | null) {
  selectedSchemaCode.value = selectedSchemaCode.value === schemaCode ? null : schemaCode
  selectedCategory.value = null // 切换业态时清除品类筛选
  currentPage.value = 1
  loadRequirements()
}

// 关注状态 Map: userId -> isFollowing
const followingMap = ref<Map<number, boolean>>(new Map())

// 检查并加载关注状态
async function loadFollowStatus(userIds: number[]) {
  if (!authStore.token) return
  for (const userId of userIds) {
    if (!followingMap.value.has(userId)) {
      try {
        const r = await checkFollowStatus(userId)
        if (r.code === 0) {
          followingMap.value.set(userId, r.data || false)
        }
      } catch {
        // ignore
      }
    }
  }
}

// 关注/取消关注
async function toggleFollow(r: RequirementResponse) {
  if (!requireAuth('/hall/need')) return
  if (!r.userId) {
    ElMessage.warning('无法关注该用户')
    return
  }

  const isFollowing = followingMap.value.get(r.userId) || false
  try {
    if (isFollowing) {
      await unfollowUser(r.userId)
      followingMap.value.set(r.userId, false)
      ElMessage.success(`已取消关注 ${r.nickName || r.companyName || '该用户'}`)
    } else {
      await followUser(r.userId)
      followingMap.value.set(r.userId, true)
      ElMessage.success(`已关注 ${r.nickName || r.companyName || '该用户'}`)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

function isFollowingUser(userId?: number): boolean {
  if (!userId) return false
  return followingMap.value.get(userId) || false
}

const focusIdFromRoute = computed(() => {
  const raw = route.query.focusId
  const s = Array.isArray(raw) ? raw[0] : raw
  const n = s ? Number(s) : NaN
  return Number.isFinite(n) ? n : null
})

const displayRequirements = computed(() => {
  if (focusIdFromRoute.value) return requirements.value
  // 前端分页
  const start = (currentPage.value - 1) * pageSize.value
  return requirements.value.slice(start, start + pageSize.value)
})

function setCardEl(id: number, el: Element | null) {
  if (!id) return
  if (el) cardEls.set(id, el as HTMLElement)
  else cardEls.delete(id)
}

async function applyFocusIfNeeded() {
  const id = focusIdFromRoute.value
  if (!id) return
  await nextTick()
  const el = cardEls.get(id)
  if (!el) return

  focusedId.value = id
  el.scrollIntoView({ behavior: 'smooth', block: 'center' })
  router.replace({ path: route.path, query: { ...route.query, focusId: undefined } })

  if (focusTimer) window.clearTimeout(focusTimer)
  focusTimer = window.setTimeout(() => {
    focusedId.value = null
    focusTimer = null
  }, 2500)
}

const drawerOpen = ref(false)
const drawerConversationId = ref<number | null>(null)
const drawerPeerName = ref('')
const drawerSubjectSnapshotJson = ref<string | null>(null)
const drawerSubjectId = ref<number | null>(null)

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
  if (!requireAuth('/hall/need')) return
  if (!r.userId || !r.id) {
    ElMessage.warning('该条需求暂不支持报价')
    return
  }
  try {
    const res = await openChatConversation({
      peerUserId: r.userId,
      subjectType: 'NEED',
      subjectId: r.id,
      subjectSnapshotJson: buildNeedSnapshot(r)
    })
    if (res.code !== 0 || !res.data) throw new Error(res.message)

    drawerConversationId.value = res.data
    drawerPeerName.value = r.nickName || r.userName || r.companyName || '对方'
    drawerSubjectId.value = r.id
    drawerSubjectSnapshotJson.value = buildNeedSnapshot(r)
    drawerOpen.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '发起报价失败')
  }
}

function onDrawerClosed() {
  drawerConversationId.value = null
  drawerSubjectSnapshotJson.value = null
  drawerSubjectId.value = null
  drawerPeerName.value = ''
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
    
    // 前端搜索过滤（关键词搜索）
    if (searchKeyword.value.trim()) {
      const kw = searchKeyword.value.toLowerCase()
      result = result.filter(r => 
        r.categoryName?.toLowerCase().includes(kw) ||
        r.companyName?.toLowerCase().includes(kw) ||
        r.purchaseAddress?.toLowerCase().includes(kw) ||
        r.nickName?.toLowerCase().includes(kw)
      )
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

// 选择品种筛选
function selectCategory(cat: string | null) {
  selectedCategory.value = selectedCategory.value === cat ? null : cat
  currentPage.value = 1 // 重置分页
  loadRequirements()
}

// 搜索
function onSearch() {
  currentPage.value = 1 // 重置分页
  loadRequirements()
}

// 分页变更
function handlePageChange(page: number) {
  currentPage.value = page
  window.scrollTo({ top: 400, behavior: 'smooth' })
}

onMounted(() => {
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

// 根据品类名称查找业态代码
function findSchemaCodeByCategory(categoryName: string): string {
  for (const schema of schemaTree.value) {
    const categories = flattenCategories(schema.categories)
    if (categories.includes(categoryName)) {
      return schema.schemaCode
    }
  }
  return 'feed' // 默认饲料原料
}

// 获取需求的单位配置
function getRequirementUnitConfig(r: RequirementResponse) {
  const schemaCode = findSchemaCodeByCategory(r.categoryName)
  return getSchemaUnitConfig(schemaCode)
}

// 解析品类参数 JSON，提取关键参数显示（极致精简版，支持标准化格式）
function parseParams(paramsJson?: string): string {
  if (!paramsJson) return '暂无参数'
  try {
    const data = JSON.parse(paramsJson)
    // 兼容逻辑：标准格式是 {"参数名": "参数值"}
    let flatParams: Record<string, any> = {}

    if (data.params || data.custom) {
      if (data.params) {
        Object.entries(data.params).forEach(([k, v]) => {
          const name = (typeof v === 'object' && v !== null && (v as any).name) ? (v as any).name : k
          const val = (typeof v === 'object' && v !== null && 'value' in (v as any)) ? (v as any).value : v
          flatParams[name] = val
        })
      }
      if (data.custom) Object.assign(flatParams, data.custom)
    } else if (typeof data === 'object' && data !== null) {
      flatParams = data
    }

    const entries = Object.entries(flatParams)
    if (entries.length === 0) return '暂无参数'
    
    return entries
      .slice(0, 3)
      .map(([k, v]) => {
        if (/^\d+$/.test(k)) return String(v)
        return `${k}:${v}`
      })
      .join(' / ')
  } catch {
    return '暂无参数'
  }
}
</script>

<template>
  <div class="bg-gray-50 text-gray-900 min-h-screen">

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

    <!-- 筛选区 -->
    <section class="bg-white border-b shadow-sm">
      <div class="max-w-7xl mx-auto px-4 py-6">
        <div class="flex flex-col md:flex-row gap-4 mb-6">
          <div class="flex-1 relative">
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索您想供应的品种、求购区域或指标要求..."
              class="w-full border-2 border-gray-200 rounded-lg py-2.5 px-10 focus:border-brand-500 outline-none transition-all"
              @keyup.enter="onSearch"
            />
            <svg class="w-5 h-5 absolute left-3 top-3 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
          </div>
          <button 
            class="px-8 py-2.5 bg-brand-600 text-white rounded-xl font-bold hover:bg-brand-700 transition-all "
            @click="onSearch"
          >
            搜索需求
          </button>
        </div>

        <div class="space-y-4">
          <!-- 业态筛选 -->
          <div class="flex items-start gap-4 text-xs">
            <span class="text-gray-400 shrink-0 mt-1.5 font-medium">产品业态:</span>
            <div class="flex flex-wrap gap-2">
              <button
                :class="['px-3 py-1.5 border rounded-full transition-all font-medium', selectedSchemaCode === null ? 'bg-autumn-600 text-white border-autumn-600' : 'border-gray-200 hover:border-autumn-500 hover:text-autumn-600']"
                @click="selectSchema(null)"
              >
                全部
              </button>
              <button
                v-for="schema in schemaTree"
                :key="schema.schemaCode"
                :class="['px-3 py-1.5 border rounded-full transition-all font-medium', selectedSchemaCode === schema.schemaCode ? 'bg-autumn-600 text-white border-autumn-600' : 'border-gray-200 hover:border-autumn-500 hover:text-autumn-600']"
                @click="selectSchema(schema.schemaCode)"
              >
                {{ schema.schemaName }}
              </button>
            </div>
          </div>

          <!-- 常见品类快速筛选 -->
          <div class="flex items-start gap-4 text-xs">
            <span class="text-gray-400 shrink-0 mt-1.5 font-medium">求购品类:</span>
            <div class="flex flex-wrap gap-2">
              <button
                :class="['px-3 py-1 border rounded-full transition-all', selectedCategory === null ? 'bg-autumn-600 text-white border-autumn-600' : 'border-gray-200 hover:border-autumn-500 hover:text-autumn-600']"
                @click="selectCategory(null)"
              >
                全部
              </button>
              <button
                v-for="cat in quickCategoryOptions"
                :key="cat"
                :class="['px-3 py-1 border rounded-full transition-all', selectedCategory === cat ? 'bg-autumn-600 text-white border-autumn-600' : 'border-gray-200 hover:border-autumn-500 hover:text-autumn-600']"
                @click="selectCategory(cat)"
              >
                {{ cat }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 列表区（先按设计稿静态，后续二期接接口替换） -->
    <main class="max-w-7xl mx-auto px-4 py-8">
      <div class="space-y-4">
        <div v-if="listLoading" class="bg-white rounded-xl border border-gray-200 p-8 text-gray-400 text-sm">
          正在加载需求...
        </div>

        <div
          v-for="r in displayRequirements"
          :key="r.id"
          :ref="(el) => setCardEl(Number(r.id), el as any)"
          class="purchase-card bg-white rounded-xl p-5 border border-gray-200 transition-all"
          :class="focusedId === r.id ? 'ring-2 ring-autumn-500/50 bg-autumn-50/40' : 'hover:shadow-md hover:border-autumn-100'"
        >
          <div class="flex flex-col lg:flex-row lg:flex-wrap items-start gap-6 mx-0">
            <div class="w-full lg:w-52 flex items-center gap-3 shrink-0 border-r border-gray-50 pr-4">
              <div class="w-12 h-12 bg-autumn-50 text-autumn-700 rounded-lg flex items-center justify-center text-xl font-bold shrink-0">
                {{ (r.companyName || r.nickName || r.userName || '采')[0] }}
              </div>
              <div class="overflow-hidden flex-1">
                <div class="text-sm font-bold text-gray-900 truncate">{{ r.companyName || '未填写公司' }}</div>
                <div class="flex items-center gap-1 mt-1">
                  <span class="bg-autumn-50 text-autumn-600 text-[10px] px-1 py-0.5 rounded">企业用户</span>
                  <span class="text-[10px] text-gray-400">{{ r.nickName || r.userName || '' }}</span>
                </div>
              </div>
              <!-- 关注按钮 -->
              <button
                v-if="authStore.token && r.userId"
                class="shrink-0 text-xs px-2 py-1 rounded-full border transition-all "
                :class="isFollowingUser(r.userId) 
                  ? 'bg-amber-50 text-amber-600 border-amber-200 hover:bg-amber-100' 
                  : 'bg-white text-gray-500 border-gray-200 hover:border-autumn-300 hover:text-autumn-600'"
                @click.stop="toggleFollow(r)"
              >
                {{ isFollowingUser(r.userId) ? '已关注' : '+ 关注' }}
              </button>
            </div>

            <div class="w-full lg:w-[120px] shrink-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="bg-red-100 text-red-600 text-[10px] px-1.5 py-0.5 rounded font-bold">急需</span>
                <span class="text-gray-400 text-[10px]">ID: {{ r.id }}</span>
              </div>
              <h3 class="text-lg font-bold text-gray-900 truncate">{{ r.categoryName }}</h3>
              <div class="mt-1 text-xl font-black text-brand-600 italic">
                <template v-if="r.expectedPrice != null">
                  <span class="whitespace-nowrap inline-flex items-baseline gap-1">
                    <span>意向: ¥{{ r.expectedPrice }}</span>
                    <span class="text-xs font-normal text-gray-400 not-italic">{{ getRequirementUnitConfig(r).priceUnit }}</span>
                  </span>
                </template>
                <span v-else>面议 / 基差报价</span>
              </div>
            </div>

            <div class="flex-1 grid grid-cols-2 md:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_minmax(0,1fr)_190px] gap-4 text-xs py-2 bg-gray-50/50 rounded-lg px-4">
              <div class="min-w-0">
                <div class="text-gray-400 mb-0.5">需求数量</div>
                <div class="font-semibold text-gray-700">{{ r.quantity ?? '-' }} {{ getRequirementUnitConfig(r).quantityUnit }}</div>
              </div>
              <div class="min-w-0">
                <div class="text-gray-400 mb-0.5">收货地</div>
                <div class="font-semibold text-gray-700 truncate">{{ r.purchaseAddress || '-' }}</div>
              </div>
              <div class="min-w-0">
                <div class="text-gray-400 mb-0.5">指标要求</div>
                <div class="font-semibold text-gray-700 truncate">{{ parseParams(r.paramsJson) }}</div>
              </div>
              <div class="min-w-0">
                <div class="text-gray-400 mb-0.5">最晚到货</div>
                <div class="font-semibold text-red-500">-</div>
              </div>
            </div>

            <div class="shrink-0 flex flex-col items-center gap-2">
              <button class="px-8 py-3 bg-brand-600 text-white rounded-xl font-bold text-sm shadow-sm hover:bg-brand-700 transition-all " @click="onQuote(r)">
                立即报价
              </button>
            </div>
          </div>
        </div>

        <div v-if="!listLoading && requirements.length === 0" class="bg-white rounded-xl border border-gray-200 p-8 text-center">
          <div class="text-gray-400 text-sm mb-2">暂无采购需求</div>
          <div class="text-xs text-gray-300">
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
      <div v-if="total > 0" class="text-center mt-4 text-xs text-gray-400">
        共 {{ total }} 条采购需求
        <span v-if="selectedCategory" class="ml-2">· 当前筛选：{{ selectedCategory }}</span>
      </div>
    </main>

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
</style>


