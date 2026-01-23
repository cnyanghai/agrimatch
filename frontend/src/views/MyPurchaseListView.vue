<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshCcw, Pencil, Ban, RotateCcw, X, ShoppingCart, MapPin, DollarSign, Clock, Search } from 'lucide-vue-next'
import { listRequirements, updateRequirement, type RequirementResponse, type RequirementUpdateRequest } from '../api/requirement'
import { useAuthStore } from '../store/auth'
import { BaseButton, BaseModal, EmptyState, Skeleton } from '../components/ui'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)

const requirements = ref<RequirementResponse[]>([])

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

const pagedRequirements = computed(() => {
  const start = (pagination.page - 1) * pagination.size
  const end = start + pagination.size
  return requirements.value.slice(start, end)
})

async function loadRequirements() {
  loading.value = true
  try {
    const companyId = auth.me?.companyId
    if (!companyId) {
      requirements.value = []
      pagination.total = 0
      return
    }
    const r = await listRequirements({
      companyId,
      categoryName: filters.categoryName || undefined,
      status: filters.status ?? undefined,
      includeExpired: true
    })
    if (r.code === 0) {
      requirements.value = r.data || []
      pagination.total = requirements.value.length
    } else {
      throw new Error(r.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载需求列表失败')
  } finally {
    loading.value = false
  }
}

function handleFilter() {
  pagination.page = 1
  loadRequirements()
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
  if (v == null) return '面议'
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
const editing = ref<RequirementResponse | null>(null)
const editForm = reactive<RequirementUpdateRequest>({
  quantity: undefined,
  expectedPrice: undefined,
  packaging: undefined,
  invoiceType: undefined,
  paymentMethod: undefined,
  deliveryMethod: undefined,
  expireMinutes: undefined,
  purchaseAddress: undefined,
  paramsJson: undefined,
  remark: undefined
})

function openEdit(req: RequirementResponse) {
  editing.value = req
  editForm.quantity = req.quantity
  editForm.expectedPrice = req.expectedPrice
  editForm.packaging = req.packaging
  editForm.invoiceType = req.invoiceType
  editForm.paymentMethod = req.paymentMethod
  editForm.deliveryMethod = req.deliveryMethod
  editForm.expireMinutes = req.expireMinutes
  editForm.purchaseAddress = req.purchaseAddress
  editForm.paramsJson = req.paramsJson
  editForm.remark = req.remark
  editOpen.value = true
}

async function saveEdit() {
  if (!editing.value?.id) return
  saving.value = true
  try {
    const r = await updateRequirement(editing.value.id, {
      quantity: editForm.quantity,
      expectedPrice: editForm.expectedPrice,
      packaging: editForm.packaging,
      invoiceType: editForm.invoiceType,
      paymentMethod: editForm.paymentMethod,
      deliveryMethod: editForm.deliveryMethod,
      expireMinutes: editForm.expireMinutes,
      purchaseAddress: editForm.purchaseAddress,
      paramsJson: editForm.paramsJson,
      remark: editForm.remark
    })
    if (r.code !== 0) throw new Error(r.message)
    ElMessage.success('已保存')
    editOpen.value = false
    await loadRequirements()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function revoke(req: RequirementResponse) {
  if (!req.id) return
  try {
    await ElMessageBox.confirm('撤销后该需求将从大厅隐藏，可随时再次发布。', '确认撤销？', {
      confirmButtonText: '撤销',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const r = await updateRequirement(req.id, { status: 2 })
    if (r.code !== 0) throw new Error(r.message)
    ElMessage.success('已撤销')
    await loadRequirements()
  } catch (e: any) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e?.message || '操作失败')
  }
}

async function republish(req: RequirementResponse) {
  if (!req.id) return
  try {
    await ElMessageBox.confirm('将该需求重新发布到大厅，并按有效期重新计时（如有）。', '再次发布？', {
      confirmButtonText: '发布',
      cancelButtonText: '取消',
      type: 'info'
    })
    const r = await updateRequirement(req.id, { status: 0, expireMinutes: req.expireMinutes ?? undefined })
    if (r.code !== 0) throw new Error(r.message)
    ElMessage.success('已再次发布')
    await loadRequirements()
  } catch (e: any) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e?.message || '操作失败')
  }
}

onMounted(() => {
  loadRequirements()
})
</script>

<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">我的采购</h1>
        <p class="text-sm text-gray-500 mt-1">管理已发布的采购需求</p>
      </div>
      <div class="flex items-center gap-3">
        <BaseButton type="secondary" size="sm" :loading="loading" @click="loadRequirements">
          <RefreshCcw class="w-4 h-4" />
          刷新
        </BaseButton>
        <BaseButton type="primary" size="sm" @click="router.push('/requirements')">
          <Plus class="w-4 h-4" />
          发布新需求
        </BaseButton>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="bg-white rounded-xl border border-gray-200 p-4">
      <div class="flex flex-wrap items-center gap-4">
        <!-- 搜索框 -->
        <div class="relative flex-1 min-w-[200px] max-w-[300px]">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            v-model="filters.categoryName"
            type="text"
            placeholder="搜索品类..."
            class="w-full pl-10 pr-4 py-2.5 border-2 border-gray-200 rounded-xl text-sm focus:border-brand-500 outline-none transition-all"
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
                ? 'bg-brand-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            ]"
            @click="filters.status = opt.value; handleFilter()"
          >
            {{ opt.label }}
          </button>
        </div>

        <!-- 统计 -->
        <div class="ml-auto">
          <span class="px-3 py-1.5 bg-gray-50 text-gray-600 text-xs font-bold rounded-full border border-gray-200">
            共 {{ pagination.total }} 条
          </span>
        </div>
      </div>
    </div>

    <!-- 需求列表 -->
    <div class="bg-white rounded-xl border border-gray-200 overflow-hidden">
      <!-- 加载状态 -->
      <div v-if="loading && requirements.length === 0" class="p-6 space-y-4">
        <Skeleton type="card" />
        <Skeleton type="card" />
        <Skeleton type="card" />
      </div>

      <!-- 空状态 -->
      <EmptyState
        v-else-if="requirements.length === 0"
        type="empty"
        title="暂无采购需求"
        description="点击右上角按钮发布您的第一条采购需求"
        action-text="发布需求"
        size="md"
        @action="router.push('/requirements')"
      />

      <!-- 需求卡片列表 -->
      <div v-else class="divide-y divide-gray-50">
        <div
          v-for="(req, index) in pagedRequirements"
          :key="req.id"
          class="p-5 hover:bg-gray-50/50 transition-all animate-stagger-in"
          :style="{ animationDelay: `${index * 30}ms` }"
        >
          <!-- 头部：品类名 + 状态 -->
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-xl bg-brand-50 flex items-center justify-center">
                <ShoppingCart class="w-5 h-5 text-brand-600" />
              </div>
              <div>
                <h3 class="text-lg font-bold text-gray-900">{{ req.categoryName }}</h3>
                <p v-if="req.contractNo" class="text-xs text-gray-400">{{ req.contractNo }}</p>
              </div>
            </div>
            <span
              :class="[
                'px-3 py-1 rounded-full text-xs font-bold',
                getStatusColor(req.status) === 'emerald' ? 'bg-brand-50 text-brand-600' :
                getStatusColor(req.status) === 'amber' ? 'bg-amber-50 text-amber-600' :
                'bg-gray-100 text-gray-600'
              ]"
            >
              {{ getStatusText(req.status) }}
            </span>
          </div>

          <!-- 核心信息网格 -->
          <div class="grid grid-cols-2 md:grid-cols-4 gap-3 mb-4">
            <div class="bg-gray-50 rounded-xl p-3">
              <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-1">
                <ShoppingCart class="w-3 h-3" />
                数量
              </div>
              <div class="font-bold text-gray-900">
                {{ req.quantity ?? '-' }} 吨
                <span v-if="req.status === 1 && req.remainingQuantity != null" class="text-xs text-gray-500 font-medium">
                  (剩 {{ req.remainingQuantity }})
                </span>
              </div>
            </div>
            <div class="bg-gray-50 rounded-xl p-3">
              <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-1">
                <DollarSign class="w-3 h-3" />
                意向价
              </div>
              <div class="font-bold text-brand-600">{{ formatPrice(req.expectedPrice) }}</div>
            </div>
            <div class="bg-gray-50 rounded-xl p-3">
              <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-1">
                <MapPin class="w-3 h-3" />
                交付地
              </div>
              <div class="font-bold text-gray-900 truncate">{{ req.purchaseAddress || '—' }}</div>
            </div>
            <div class="bg-gray-50 rounded-xl p-3">
              <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-1">
                <Clock class="w-3 h-3" />
                有效期
              </div>
              <div class="font-bold text-gray-900">{{ req.expireTime ? formatDate(req.expireTime) : '长期' }}</div>
            </div>
          </div>

          <!-- 标签 -->
          <div class="flex flex-wrap gap-2 mb-4">
            <span v-if="req.packaging" class="px-2.5 py-1 rounded-full text-xs font-bold bg-white border border-gray-200 text-gray-600">
              包装：{{ req.packaging }}
            </span>
            <span v-if="req.paymentMethod" class="px-2.5 py-1 rounded-full text-xs font-bold bg-white border border-gray-200 text-gray-600">
              付款：{{ req.paymentMethod }}
            </span>
            <span v-if="req.invoiceType" class="px-2.5 py-1 rounded-full text-xs font-bold bg-white border border-gray-200 text-gray-600">
              发票：{{ req.invoiceType }}
            </span>
            <span v-if="req.deliveryMethod" class="px-2.5 py-1 rounded-full text-xs font-bold bg-white border border-gray-200 text-gray-600">
              交付：{{ req.deliveryMethod }}
            </span>
            <span v-if="getParamsSummary(req.paramsJson)" class="px-2.5 py-1 rounded-full text-xs font-bold bg-brand-50 border border-brand-100 text-brand-600">
              指标：{{ getParamsSummary(req.paramsJson) }}
            </span>
          </div>

          <!-- 底部：时间 + 操作 -->
          <div class="flex items-center justify-between pt-4 border-t border-gray-200">
            <div class="text-xs text-gray-400">
              创建：{{ formatDate(req.createTime) }} · 更新：{{ formatDate(req.updateTime) }}
            </div>
            <div class="flex items-center gap-2">
              <BaseButton
                v-if="req.status !== 3"
                type="secondary"
                size="sm"
                @click="openEdit(req)"
              >
                <Pencil class="w-4 h-4" />
                编辑
              </BaseButton>
              <BaseButton
                v-if="req.status === 0 || req.status === 1"
                type="danger"
                size="sm"
                @click="revoke(req)"
              >
                <Ban class="w-4 h-4" />
                撤销
              </BaseButton>
              <BaseButton
                v-else-if="req.status === 2"
                type="primary"
                size="sm"
                @click="republish(req)"
              >
                <RotateCcw class="w-4 h-4" />
                再次发布
              </BaseButton>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="pagination.total > pagination.size" class="p-4 border-t border-gray-200 flex justify-center">
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
      title="编辑采购需求"
      size="lg"
    >
      <div class="space-y-6">
        <p class="text-sm text-gray-500">
          仅修改本条已发布需求，不影响您的公司/个人档案
        </p>

        <!-- 标的信息 -->
        <div class="bg-gray-50 rounded-xl p-4 border border-gray-200">
          <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">采购标的</div>
          <div class="mt-1 font-bold text-gray-900">
            {{ editing?.categoryName || '-' }}
            <span v-if="editing?.contractNo" class="text-sm text-gray-500 font-medium ml-2">{{ editing?.contractNo }}</span>
          </div>
        </div>

        <!-- 表单 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">采购数量（吨）</label>
            <input
              v-model.number="editForm.quantity"
              type="number"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">意向价格（元/吨）</label>
            <input
              v-model.number="editForm.expectedPrice"
              type="number"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">包装方式</label>
            <input
              v-model="editForm.packaging"
              type="text"
              placeholder="例如：散装 / 袋装"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">付款方式</label>
            <input
              v-model="editForm.paymentMethod"
              type="text"
              placeholder="例如：现款 / 账期"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">发票类型</label>
            <input
              v-model="editForm.invoiceType"
              type="text"
              placeholder="例如：专票 / 普票 / 不需要"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">交货方式</label>
            <input
              v-model="editForm.deliveryMethod"
              type="text"
              placeholder="例如：到厂 / 自提"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">有效期（分钟）</label>
            <input
              v-model.number="editForm.expireMinutes"
              type="number"
              placeholder="空=长期有效"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">交付地</label>
            <input
              v-model="editForm.purchaseAddress"
              type="text"
              placeholder="例如：北京市朝阳区..."
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div class="md:col-span-2">
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">指标参数（JSON）</label>
            <textarea
              v-model="editForm.paramsJson"
              rows="3"
              placeholder='例如：{"custom":{"水分":"≤14%","霉变":"≤1%"}}'
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all resize-none"
            ></textarea>
          </div>
          <div class="md:col-span-2">
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">备注</label>
            <textarea
              v-model="editForm.remark"
              rows="2"
              placeholder="补充说明（选填）"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all resize-none"
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
