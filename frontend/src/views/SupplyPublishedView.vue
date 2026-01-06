<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshCcw, Pencil, Ban, RotateCcw, X, Package, MapPin, DollarSign, Clock, Search } from 'lucide-vue-next'
import { listSupplies, updateSupply, type SupplyResponse, type SupplyUpdateRequest } from '../api/supply'
import { useAuthStore } from '../store/auth'
import { BaseButton, BaseModal, EmptyState, Skeleton } from '../components/ui'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)

const supplies = ref<SupplyResponse[]>([])

// 分页相关
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 筛选条件
const filters = reactive({
  categoryName: '',
  status: null as number | null
})

const statusOptions = [
  { value: null, label: '全部状态', color: 'gray' },
  { value: 0, label: '发布中', color: 'emerald' },
  { value: 1, label: '部分成交', color: 'amber' },
  { value: 2, label: '已下架', color: 'gray' },
  { value: 3, label: '全部成交', color: 'emerald' }
]

const pagedSupplies = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  const end = start + pagination.size
  return supplies.value.slice(start, end)
})

async function loadSupplies() {
  loading.value = true
  try {
    const companyId = auth.me?.companyId
    if (!companyId) {
      supplies.value = []
      pagination.total = 0
      return
    }
    const r = await listSupplies({
      companyId,
      categoryName: filters.categoryName || undefined,
      status: filters.status ?? undefined,
      includeExpired: true
    })
    if (r.code === 0) {
      supplies.value = r.data || []
      pagination.total = supplies.value.length
    } else {
      throw new Error(r.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载供应列表失败')
  } finally {
    loading.value = false
  }
}

function handleFilter() {
  pagination.page = 1
  loadSupplies()
}

function clearFilters() {
  filters.categoryName = ''
  filters.status = null
  handleFilter()
}

function handlePageChange(page: number) {
  pagination.page = page
}

function getStatusText(status?: number) {
  return statusOptions.find(o => o.value === status)?.label || '未知'
}

function getStatusColor(status?: number) {
  return statusOptions.find(o => o.value === status)?.color || 'gray'
}

function formatDate(s?: string) {
  if (!s) return ''
  return new Date(s).toLocaleDateString('zh-CN')
}

function formatPrice(v?: number) {
  if (v == null) return '—'
  return `¥${Number(v).toLocaleString('zh-CN')}/吨`
}

function getParamsSummary(paramsJson?: string) {
  if (!paramsJson) return null
  try {
    const obj = JSON.parse(paramsJson)
    const custom = obj?.custom
    if (custom && typeof custom === 'object') {
      const items = Object.entries(custom)
        .slice(0, 4)
        .map(([k, v]) => `${k}:${String(v)}`)
      return items.length ? items.join('，') : null
    }
    return null
  } catch {
    return null
  }
}

const editOpen = ref(false)
const saving = ref(false)
const editing = ref<SupplyResponse | null>(null)
const editForm = reactive<SupplyUpdateRequest>({
  origin: undefined,
  quantity: undefined,
  exFactoryPrice: undefined,
  shipAddress: undefined,
  deliveryMode: undefined,
  packaging: undefined,
  storageMethod: undefined,
  expireMinutes: undefined,
  paramsJson: undefined,
  priceRulesJson: undefined,
  remark: undefined
})

function openEdit(s: SupplyResponse) {
  editing.value = s
  editForm.origin = s.origin
  editForm.quantity = s.quantity
  editForm.exFactoryPrice = s.exFactoryPrice
  editForm.shipAddress = s.shipAddress
  editForm.deliveryMode = s.deliveryMode
  editForm.packaging = s.packaging
  editForm.storageMethod = s.storageMethod
  editForm.expireMinutes = s.expireMinutes
  editForm.paramsJson = s.paramsJson
  editForm.priceRulesJson = s.priceRulesJson
  editForm.remark = s.remark
  editOpen.value = true
}

async function saveEdit() {
  if (!editing.value?.id) return
  saving.value = true
  try {
    const r = await updateSupply(editing.value.id, {
      origin: editForm.origin,
      quantity: editForm.quantity,
      exFactoryPrice: editForm.exFactoryPrice,
      shipAddress: editForm.shipAddress,
      deliveryMode: editForm.deliveryMode,
      packaging: editForm.packaging,
      storageMethod: editForm.storageMethod,
      expireMinutes: editForm.expireMinutes,
      paramsJson: editForm.paramsJson,
      priceRulesJson: editForm.priceRulesJson,
      remark: editForm.remark
    })
    if (r.code !== 0) throw new Error(r.message)
    ElMessage.success('已保存')
    editOpen.value = false
    await loadSupplies()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function revoke(s: SupplyResponse) {
  if (!s.id) return
  try {
    await ElMessageBox.confirm('下架后该供应将从大厅隐藏，可随时再次发布。', '确认下架？', {
      confirmButtonText: '下架',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const r = await updateSupply(s.id, { status: 2 })
    if (r.code !== 0) throw new Error(r.message)
    ElMessage.success('已下架')
    await loadSupplies()
  } catch (e: any) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e?.message || '操作失败')
  }
}

async function republish(s: SupplyResponse) {
  if (!s.id) return
  try {
    await ElMessageBox.confirm('将该供应重新发布到大厅，并按有效期重新计时（如有）。', '再次发布？', {
      confirmButtonText: '发布',
      cancelButtonText: '取消',
      type: 'info'
    })
    const r = await updateSupply(s.id, { status: 0, expireMinutes: s.expireMinutes ?? undefined })
    if (r.code !== 0) throw new Error(r.message)
    ElMessage.success('已再次发布')
    await loadSupplies()
  } catch (e: any) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e?.message || '操作失败')
  }
}

onMounted(() => {
  loadSupplies()
})
</script>

<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">我的供应</h1>
        <p class="text-sm text-gray-500 mt-1">管理已发布的供应信息</p>
      </div>
      <div class="flex items-center gap-3">
        <BaseButton type="secondary" size="sm" :loading="loading" @click="loadSupplies">
          <RefreshCcw class="w-4 h-4" />
          刷新
        </BaseButton>
        <BaseButton type="primary" size="sm" @click="router.push('/supply')">
          <Plus class="w-4 h-4" />
          发布新供应
        </BaseButton>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="bg-white rounded-2xl border border-gray-100 p-4">
      <div class="flex flex-wrap items-center gap-4">
        <!-- 搜索框 -->
        <div class="relative flex-1 min-w-[200px] max-w-[300px]">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            v-model="filters.categoryName"
            type="text"
            placeholder="搜索品类..."
            class="w-full pl-10 pr-4 py-2.5 border-2 border-gray-100 rounded-xl text-sm focus:border-emerald-500 outline-none transition-all"
            @keyup.enter="handleFilter"
          />
        </div>

        <!-- 状态筛选 -->
        <div class="flex gap-2">
          <button
            v-for="opt in statusOptions"
            :key="opt.value ?? 'all'"
            :class="[
              'px-3 py-2 text-xs font-bold rounded-xl transition-all',
              filters.status === opt.value
                ? 'bg-emerald-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            ]"
            @click="filters.status = opt.value; handleFilter()"
          >
            {{ opt.label }}
          </button>
        </div>

        <!-- 统计 -->
        <div class="ml-auto">
          <span class="px-3 py-1.5 bg-gray-50 text-gray-600 text-xs font-bold rounded-full border border-gray-100">
            共 {{ pagination.total }} 条
          </span>
        </div>
      </div>
    </div>

    <!-- 供应列表 -->
    <div class="bg-white rounded-2xl border border-gray-100 overflow-hidden">
      <!-- 加载状态 -->
      <div v-if="loading && supplies.length === 0" class="p-6 space-y-4">
        <Skeleton type="card" />
        <Skeleton type="card" />
        <Skeleton type="card" />
      </div>

      <!-- 空状态 -->
      <EmptyState
        v-else-if="supplies.length === 0"
        type="empty"
        title="暂无供应信息"
        description="点击右上角按钮发布您的第一条供应"
        action-text="发布供应"
        size="md"
        @action="router.push('/supply')"
      />

      <!-- 供应卡片列表 -->
      <div v-else class="divide-y divide-gray-50">
        <div
          v-for="(s, index) in pagedSupplies"
          :key="s.id"
          class="p-5 hover:bg-gray-50/50 transition-all animate-stagger-in"
          :style="{ animationDelay: `${index * 30}ms` }"
        >
          <!-- 头部：品类名 + 状态 -->
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center">
                <Package class="w-5 h-5 text-emerald-600" />
              </div>
              <div>
                <h3 class="font-bold text-gray-900">{{ s.categoryName }}</h3>
                <p v-if="s.supplyNo" class="text-xs text-gray-400">{{ s.supplyNo }}</p>
              </div>
            </div>
            <span
              :class="[
                'px-3 py-1 rounded-full text-xs font-bold',
                getStatusColor(s.status) === 'emerald' ? 'bg-emerald-50 text-emerald-600' :
                getStatusColor(s.status) === 'amber' ? 'bg-amber-50 text-amber-600' :
                'bg-gray-100 text-gray-600'
              ]"
            >
              {{ getStatusText(s.status) }}
            </span>
          </div>

          <!-- 核心信息网格 -->
          <div class="grid grid-cols-2 md:grid-cols-4 gap-3 mb-4">
            <div class="bg-gray-50 rounded-xl p-3">
              <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-1">
                <Package class="w-3 h-3" />
                数量
              </div>
              <div class="font-bold text-gray-900">
                {{ s.quantity ?? '-' }} 吨
                <span v-if="s.status === 1 && s.remainingQuantity != null" class="text-xs text-gray-500 font-medium">
                  (剩 {{ s.remainingQuantity }})
                </span>
              </div>
            </div>
            <div class="bg-gray-50 rounded-xl p-3">
              <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-1">
                <DollarSign class="w-3 h-3" />
                出厂价
              </div>
              <div class="font-bold text-emerald-600">{{ formatPrice(s.exFactoryPrice) }}</div>
            </div>
            <div class="bg-gray-50 rounded-xl p-3">
              <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-1">
                <MapPin class="w-3 h-3" />
                发货地
              </div>
              <div class="font-bold text-gray-900 truncate">{{ s.shipAddress || '—' }}</div>
            </div>
            <div class="bg-gray-50 rounded-xl p-3">
              <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-1">
                <Clock class="w-3 h-3" />
                有效期
              </div>
              <div class="font-bold text-gray-900">{{ s.expireTime ? formatDate(s.expireTime) : '长期' }}</div>
            </div>
          </div>

          <!-- 标签 -->
          <div class="flex flex-wrap gap-2 mb-4">
            <span v-if="s.origin" class="px-2.5 py-1 rounded-full text-xs font-bold bg-white border border-gray-100 text-gray-600">
              产地：{{ s.origin }}
            </span>
            <span v-if="s.packaging" class="px-2.5 py-1 rounded-full text-xs font-bold bg-white border border-gray-100 text-gray-600">
              包装：{{ s.packaging }}
            </span>
            <span v-if="s.deliveryMode" class="px-2.5 py-1 rounded-full text-xs font-bold bg-white border border-gray-100 text-gray-600">
              交付：{{ s.deliveryMode }}
            </span>
            <span v-if="getParamsSummary(s.paramsJson)" class="px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-50 border border-emerald-100 text-emerald-600">
              指标：{{ getParamsSummary(s.paramsJson) }}
            </span>
          </div>

          <!-- 底部：时间 + 操作 -->
          <div class="flex items-center justify-between pt-4 border-t border-gray-100">
            <div class="text-xs text-gray-400">
              创建：{{ formatDate(s.createTime) }} · 更新：{{ formatDate(s.updateTime) }}
            </div>
            <div class="flex items-center gap-2">
              <BaseButton
                v-if="s.status !== 3"
                type="secondary"
                size="sm"
                @click="openEdit(s)"
              >
                <Pencil class="w-4 h-4" />
                编辑
              </BaseButton>
              <BaseButton
                v-if="s.status === 0 || s.status === 1"
                type="danger"
                size="sm"
                @click="revoke(s)"
              >
                <Ban class="w-4 h-4" />
                下架
              </BaseButton>
              <BaseButton
                v-else-if="s.status === 2"
                type="primary"
                size="sm"
                @click="republish(s)"
              >
                <RotateCcw class="w-4 h-4" />
                再次发布
              </BaseButton>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="pagination.total > pagination.size" class="p-4 border-t border-gray-100 flex justify-center">
        <el-pagination
          v-model:current-page="pagination.page"
          :page-size="pagination.size"
          :total="pagination.total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <BaseModal
      v-model="editOpen"
      title="编辑供应信息"
      size="lg"
    >
      <div class="space-y-6">
        <p class="text-sm text-gray-500">
          仅修改本条已发布供应，不影响您的公司/个人档案
        </p>

        <!-- 标的信息 -->
        <div class="bg-gray-50 rounded-xl p-4 border border-gray-100">
          <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">交易标的</div>
          <div class="mt-1 font-bold text-gray-900">
            {{ editing?.categoryName || '-' }}
            <span v-if="editing?.supplyNo" class="text-sm text-gray-500 font-medium ml-2">{{ editing?.supplyNo }}</span>
          </div>
        </div>

        <!-- 表单 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">供应数量（吨）</label>
            <input
              v-model.number="editForm.quantity"
              type="number"
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">出厂价（元/吨）</label>
            <input
              v-model.number="editForm.exFactoryPrice"
              type="number"
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">产地</label>
            <input
              v-model="editForm.origin"
              type="text"
              placeholder="例如：山东济南..."
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">发货地址</label>
            <input
              v-model="editForm.shipAddress"
              type="text"
              placeholder="例如：山东省济南市..."
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">交付方式</label>
            <input
              v-model="editForm.deliveryMode"
              type="text"
              placeholder="例如：到厂 / 自提"
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">包装方式</label>
            <input
              v-model="editForm.packaging"
              type="text"
              placeholder="例如：散装 / 袋装"
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">储存方式</label>
            <input
              v-model="editForm.storageMethod"
              type="text"
              placeholder="例如：常温 / 冷藏"
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">有效期（分钟）</label>
            <input
              v-model.number="editForm.expireMinutes"
              type="number"
              placeholder="空=长期有效"
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all"
            />
          </div>
          <div class="md:col-span-2">
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">指标参数（JSON）</label>
            <textarea
              v-model="editForm.paramsJson"
              rows="3"
              placeholder='例如：{"custom":{"水分":"≤14%","霉变":"≤1%"}}'
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all resize-none"
            ></textarea>
          </div>
          <div class="md:col-span-2">
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">备注</label>
            <textarea
              v-model="editForm.remark"
              rows="2"
              placeholder="补充说明（选填）"
              class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all resize-none"
            ></textarea>
          </div>
        </div>
      </div>

      <template #footer>
        <BaseButton type="secondary" @click="editOpen = false">
          <X class="w-4 h-4" />
          取消
        </BaseButton>
        <BaseButton type="primary" :loading="saving" @click="saveEdit">
          保存修改
        </BaseButton>
      </template>
    </BaseModal>
  </div>
</template>
