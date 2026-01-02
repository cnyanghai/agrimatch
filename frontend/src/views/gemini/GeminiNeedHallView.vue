<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { requireAuth } from '../../utils/requireAuth'
import PublicTopNav from '../../components/PublicTopNav.vue'
import ChatDrawer from '../../components/chat/ChatDrawer.vue'
import { listRequirements, type RequirementResponse } from '../../api/requirement'
import { openChatConversation } from '../../api/chat'
import { ElMessage } from 'element-plus'

const router = useRouter()

function go(path: string) {
  router.push(path)
}

function onPublishNeed() {
  if (!requireAuth('/requirements')) return
  go('/requirements')
}

const requirements = ref<RequirementResponse[]>([])
const listLoading = ref(false)

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
    packaging: r.packaging
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
    const res = await listRequirements({ status: 0, orderBy: 'create_time', order: 'desc' })
    if (res.code !== 0) throw new Error(res.message)
    requirements.value = res.data || []
  } catch {
    requirements.value = []
  } finally {
    listLoading.value = false
  }
}

onMounted(() => {
  loadRequirements()
})
</script>

<template>
  <div class="bg-gray-50 text-gray-900 min-h-screen">
    <!-- 顶部行情走马灯 -->
    <div class="bg-slate-900 text-white py-2 overflow-hidden">
      <div class="flex animate-marquee space-x-12 px-4 text-xs font-medium">
        <span>玉米主力 2505: <b class="text-red-400">2450 ↑</b></span>
        <span>豆粕主力 2505: <b class="text-green-400">3210 ↓</b></span>
        <span>菜粕 2505: <b class="text-red-400">2680 ↑</b></span>
        <span>山东现货玉米: <b>2380</b></span>
        <span>广东港口玉米: <b>2520</b></span>
        <span>玉米主力 2505: <b class="text-red-400">2450 ↑</b></span>
        <span>豆粕主力 2505: <b class="text-green-400">3210 ↓</b></span>
      </div>
    </div>

    <PublicTopNav>
      <template #actions>
        <button class="bg-emerald-600 text-white px-5 py-2 rounded-full font-bold hover:bg-emerald-700 transition-all active:scale-95" @click="onPublishNeed">
          发布采购
        </button>
      </template>
    </PublicTopNav>

    <!-- 筛选区 -->
    <section class="bg-white border-b shadow-sm">
      <div class="max-w-7xl mx-auto px-4 py-6">
        <div class="flex flex-col md:flex-row gap-4 mb-6">
          <div class="flex-1 relative">
            <input
              type="text"
              placeholder="搜索您想供应的品种、求购区域或指标要求..."
              class="w-full border-2 border-gray-100 rounded-xl py-2.5 px-10 focus:border-emerald-500 outline-none transition-all"
            />
            <svg class="w-5 h-5 absolute left-3 top-3 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
          </div>
          <button class="px-8 py-2.5 bg-emerald-600 text-white rounded-xl font-bold hover:bg-emerald-700 transition-all active:scale-95">搜索需求</button>
        </div>

        <div class="space-y-4">
          <div class="flex items-start gap-4 text-xs">
            <span class="text-gray-400 shrink-0 mt-1.5 font-medium">求购品种:</span>
            <div class="flex flex-wrap gap-2">
              <button class="filter-tag active px-3 py-1 border rounded-full">全部</button>
              <button class="filter-tag px-3 py-1 border rounded-full">玉米</button>
              <button class="filter-tag px-3 py-1 border rounded-full">豆粕</button>
              <button class="filter-tag px-3 py-1 border rounded-full">棉粕</button>
              <button class="filter-tag px-3 py-1 border rounded-full">菜粕</button>
              <button class="filter-tag px-3 py-1 border rounded-full">DDGS</button>
              <button class="filter-tag px-3 py-1 border rounded-full">氨基酸</button>
              <button class="filter-tag px-3 py-1 border rounded-full">磷酸氢钙</button>
            </div>
          </div>
          <div class="flex items-start gap-4 text-xs">
            <span class="text-gray-400 shrink-0 mt-1.5 font-medium">收货区域:</span>
            <div class="flex flex-wrap gap-2">
              <button class="filter-tag px-3 py-1 border rounded-full">东北区</button>
              <button class="filter-tag px-3 py-1 border rounded-full">华北区</button>
              <button class="filter-tag px-3 py-1 border rounded-full">华中区</button>
              <button class="filter-tag px-3 py-1 border rounded-full">西南区</button>
              <button class="filter-tag px-3 py-1 border rounded-full">华东沿海</button>
              <button class="filter-tag px-3 py-1 border rounded-full">华南沿海</button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 列表区（先按设计稿静态，后续二期接接口替换） -->
    <main class="max-w-7xl mx-auto px-4 py-8">
      <div class="space-y-4">
        <div v-if="listLoading" class="bg-white rounded-2xl border border-gray-100 p-8 text-gray-400 text-sm">
          正在加载需求...
        </div>

        <div
          v-for="r in requirements.slice(0, 10)"
          :key="r.id"
          class="purchase-card bg-white rounded-xl p-5 border border-gray-100 transition-all"
        >
          <div class="flex flex-col lg:flex-row items-center gap-6">
            <div class="w-full lg:w-44 flex items-center gap-3 shrink-0 border-r border-gray-50 pr-4">
              <div class="w-12 h-12 bg-blue-50 text-blue-700 rounded-lg flex items-center justify-center text-xl font-bold shrink-0">
                {{ (r.companyName || r.nickName || r.userName || '采')[0] }}
              </div>
              <div class="overflow-hidden">
                <div class="text-sm font-bold text-gray-900 truncate">{{ r.companyName || '未填写公司' }}</div>
                <div class="flex items-center gap-1 mt-1">
                  <span class="bg-blue-50 text-blue-600 text-[10px] px-1 py-0.5 rounded">企业用户</span>
                  <span class="text-[10px] text-gray-400">{{ r.nickName || r.userName || '' }}</span>
                </div>
              </div>
            </div>

            <div class="w-full lg:w-64 shrink-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="bg-red-100 text-red-600 text-[10px] px-1.5 py-0.5 rounded font-bold">急需</span>
                <span class="text-gray-400 text-[10px]">ID: {{ r.id }}</span>
              </div>
              <h3 class="text-lg font-bold text-gray-900 truncate">{{ r.categoryName }}</h3>
              <div class="mt-1 text-xl font-black text-emerald-600 italic">
                <span v-if="r.expectedPrice != null">意向: ¥{{ r.expectedPrice }}</span>
                <span v-else>面议 / 基差报价</span>
                <span class="text-xs font-normal text-gray-400 not-italic">元/吨</span>
              </div>
            </div>

            <div class="flex-1 grid grid-cols-2 md:grid-cols-4 gap-4 text-xs py-2 bg-gray-50/50 rounded-lg px-4">
              <div>
                <div class="text-gray-400 mb-0.5">需求数量</div>
                <div class="font-semibold text-gray-700">{{ r.quantity ?? '-' }} 吨</div>
              </div>
              <div>
                <div class="text-gray-400 mb-0.5">收货地</div>
                <div class="font-semibold text-gray-700 truncate">{{ r.purchaseAddress || '-' }}</div>
              </div>
              <div>
                <div class="text-gray-400 mb-0.5">指标要求</div>
                <div class="font-semibold text-gray-700 truncate">详见参数</div>
              </div>
              <div>
                <div class="text-gray-400 mb-0.5">最晚到货</div>
                <div class="font-semibold text-red-500">-</div>
              </div>
            </div>

            <div class="shrink-0 flex flex-col items-center gap-2">
              <button class="px-8 py-3 bg-slate-900 text-white rounded-xl font-bold text-sm shadow-sm hover:bg-slate-800 transition-all active:scale-95" @click="onQuote(r)">
                立即报价
              </button>
              <div class="w-24 bg-gray-200 h-1.5 rounded-full overflow-hidden">
                <div class="bg-emerald-500 h-full w-2/3"></div>
              </div>
              <span class="text-[10px] text-gray-400">已有 8 家参与竞价</span>
            </div>
          </div>
        </div>

        <div v-if="!listLoading && requirements.length === 0" class="bg-white rounded-2xl border border-gray-100 p-8 text-gray-400 text-sm">
          暂无需求数据（后续会继续完善筛选与排序）
        </div>
      </div>

      <div class="flex justify-center mt-10 gap-2">
        <button class="px-4 py-2 bg-white border rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition-colors">上一页</button>
        <button class="px-4 py-2 bg-slate-900 text-white border-slate-900 rounded-lg text-sm font-bold">1</button>
        <button class="px-4 py-2 bg-white border rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition-colors">2</button>
        <button class="px-4 py-2 bg-white border rounded-lg text-sm text-gray-500 hover:bg-gray-50 transition-colors">下一页</button>
      </div>
    </main>

    <footer class="bg-white border-t py-8 mt-12">
      <div class="max-w-7xl mx-auto px-4 text-center">
        <p class="text-xs text-gray-400">© 2024 AgriMatch - 饲料原料全产业链高效撮合平台 | 采购大厅</p>
      </div>
    </footer>

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
  border-color: #10b981;
  color: #047857;
}
.filter-tag.active {
  background-color: #10b981;
  color: white;
  border-color: #10b981;
}
.purchase-card:hover {
  border-color: #a7f3d0;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.08);
}
.animate-marquee {
  display: inline-block;
  animation: marquee 30s linear infinite;
}
@keyframes marquee {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}
</style>


