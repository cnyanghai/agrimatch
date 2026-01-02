<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshCcw, Pencil, Ban, RotateCcw, X } from 'lucide-vue-next'
import PageHeader from '../components/PageHeader.vue'
import { listSupplies, updateSupply, type SupplyResponse, type SupplyUpdateRequest } from '../api/supply'
import { useAuthStore } from '../store/auth'

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
  status: undefined as number | undefined
})

const hasActiveFilters = computed(() => {
  return !!filters.categoryName.trim() || filters.status != null
})

const activeFilterSummary = computed(() => {
  const parts: string[] = []
  const kw = filters.categoryName.trim()
  if (kw) parts.push(`品类:${kw}`)
  if (filters.status != null) parts.push(`状态:${getStatusText(filters.status)}`)
  return parts.length ? parts.join(' · ') : '全部'
})

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
      status: filters.status,
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
  filters.status = undefined
  handleFilter()
}

function handlePageChange(page: number) {
  pagination.page = page
}

function getStatusText(status?: number) {
  switch (status) {
    case 0: return '发布中'
    case 1: return '部分成交'
    case 2: return '已下架'
    case 3: return '全部成交'
    default: return '未知'
  }
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
  if (!paramsJson) return '无'
  try {
    const obj = JSON.parse(paramsJson)
    const custom = obj?.custom
    if (custom && typeof custom === 'object') {
      const items = Object.entries(custom)
        .slice(0, 4)
        .map(([k, v]) => `${k}:${String(v)}`)
      return items.length ? items.join('，') : '无'
    }
    return '无'
  } catch {
    return '无'
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
  <div class="bg-gray-50 text-gray-900 min-h-screen">
    <div class="max-w-7xl mx-auto p-4 md:p-6 space-y-6">
      <PageHeader title="已发布供应" subtitle="管理您已发布的供应信息列表">
        <template #right>
          <el-button class="!rounded-xl transition-all active:scale-95" :loading="loading" @click="loadSupplies">
            <span class="inline-flex items-center gap-2">
              <RefreshCcw class="w-4 h-4" />
              刷新
            </span>
          </el-button>
          <el-button class="!rounded-xl transition-all active:scale-95" @click="router.push('/supply')">
            返回发布
          </el-button>
          <el-button
            type="primary"
            class="!rounded-xl !bg-emerald-600 hover:!bg-emerald-700 !border-emerald-600 transition-all active:scale-95"
            @click="router.push('/supply')"
          >
            <span class="inline-flex items-center gap-2">
              <Plus class="w-4 h-4" />
              发布新供应
            </span>
          </el-button>
        </template>
      </PageHeader>

      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        <div class="p-6 border-b border-gray-100 flex items-center justify-between gap-4">
          <div>
            <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">LIST</div>
            <div class="text-lg font-bold text-gray-900 mt-1">供应列表</div>
            <div class="text-sm text-gray-500 mt-1">筛选 · 编辑 · 下架 · 再次发布</div>
            <div class="text-xs text-gray-400 mt-1">
              当前筛选：<span class="font-medium text-gray-600">{{ activeFilterSummary }}</span>
            </div>
          </div>
          <el-tag type="info" effect="light" size="large" class="!rounded-full">
            共 {{ pagination.total }} 条
          </el-tag>
        </div>

        <div class="p-6">
          <div class="neo-form flex flex-wrap gap-3 mb-5 p-3 bg-gray-50 rounded-2xl border border-gray-100">
            <div class="flex flex-wrap gap-3 items-center">
              <div class="flex items-center gap-2">
                <span class="text-xs font-bold text-gray-500 uppercase tracking-wider">品类</span>
                <el-input
                  v-model="filters.categoryName"
                  placeholder="搜索品类"
                  class="w-44"
                  @keyup.enter="handleFilter"
                />
              </div>
              <div class="flex items-center gap-2">
                <span class="text-xs font-bold text-gray-500 uppercase tracking-wider">状态</span>
                <el-select v-model="filters.status" class="w-[189px] neo-select-fixed" clearable placeholder="全部状态">
                  <el-option label="全部状态" :value="undefined">
                    <div class="flex items-center gap-2">
                      <span class="w-2 h-2 rounded-full bg-gray-300"></span>
                      <span class="text-sm text-gray-700">全部状态</span>
                    </div>
                  </el-option>
                  <el-option label="发布中" :value="0">
                    <div class="flex items-center gap-2">
                      <span class="w-2 h-2 rounded-full bg-emerald-500"></span>
                      <span class="text-sm text-gray-700">发布中</span>
                    </div>
                  </el-option>
                  <el-option label="部分成交" :value="1">
                    <div class="flex items-center gap-2">
                      <span class="w-2 h-2 rounded-full bg-amber-500"></span>
                      <span class="text-sm text-gray-700">部分成交</span>
                    </div>
                  </el-option>
                  <el-option label="已下架" :value="2">
                    <div class="flex items-center gap-2">
                      <span class="w-2 h-2 rounded-full bg-gray-400"></span>
                      <span class="text-sm text-gray-700">已下架</span>
                    </div>
                  </el-option>
                  <el-option label="全部成交" :value="3">
                    <div class="flex items-center gap-2">
                      <span class="w-2 h-2 rounded-full bg-emerald-600"></span>
                      <span class="text-sm text-gray-700">全部成交</span>
                    </div>
                  </el-option>
                </el-select>
              </div>
              <el-button
                class="!rounded-xl !bg-gray-100 hover:!bg-gray-200 !text-gray-700 transition-all active:scale-95"
                :disabled="!hasActiveFilters"
                @click="clearFilters"
              >
                清空
              </el-button>
              <el-button
                type="primary"
                class="!rounded-xl !bg-emerald-600 hover:!bg-emerald-700 !border-emerald-600 transition-all active:scale-95"
                @click="handleFilter"
              >
                搜索
              </el-button>
            </div>
          </div>

          <div v-if="supplies.length === 0" class="text-center py-12 text-gray-500">
            暂无供应信息
          </div>
          <div v-else class="space-y-4">
            <div
              v-for="s in pagedSupplies"
              :key="s.id"
              class="bg-white rounded-2xl border border-gray-100 p-5 hover:shadow-md transition-shadow"
            >
              <div class="min-w-0">
                <div class="flex items-center gap-2">
                  <h3 class="text-lg font-bold text-gray-900 truncate">{{ s.categoryName }}</h3>
                  <span
                    class="text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 rounded-full border"
                    :class="s.status === 0 ? 'bg-emerald-50 text-emerald-700 border-emerald-100' :
                      s.status === 1 ? 'bg-amber-50 text-amber-700 border-amber-100' :
                      s.status === 2 ? 'bg-gray-50 text-gray-600 border-gray-100' :
                      'bg-emerald-50 text-emerald-700 border-emerald-100'"
                  >
                    {{ getStatusText(s.status) }}
                  </span>
                  <span v-if="s.supplyNo" class="text-xs text-gray-400 truncate">
                    · {{ s.supplyNo }}
                  </span>
                </div>

                <div class="mt-2 grid grid-cols-1 md:grid-cols-3 gap-3">
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">
                      {{ s.status === 1 ? '数量/剩余' : '数量' }}
                    </div>
                    <div class="mt-1 font-bold text-gray-900">
                      {{ s.quantity ?? '-' }} 吨
                      <span v-if="s.status === 1 && s.remainingQuantity != null" class="text-sm text-gray-500 font-medium"> · 剩余 {{ s.remainingQuantity }} 吨</span>
                    </div>
                  </div>
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">出厂价</div>
                    <div class="mt-1 font-bold text-gray-900">{{ formatPrice(s.exFactoryPrice) }}</div>
                  </div>
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">发货地</div>
                    <div class="mt-1 font-bold text-gray-900 truncate">{{ s.shipAddress || '—' }}</div>
                  </div>
                </div>

                <div class="mt-3 flex flex-wrap gap-2">
                  <span v-if="s.origin" class="px-2.5 py-1 rounded-full text-xs font-bold bg-white border border-gray-100 text-gray-700">
                    产地：{{ s.origin }}
                  </span>
                  <span v-if="s.packaging" class="px-2.5 py-1 rounded-full text-xs font-bold bg-white border border-gray-100 text-gray-700">
                    包装：{{ s.packaging }}
                  </span>
                  <span v-if="s.storageMethod" class="px-2.5 py-1 rounded-full text-xs font-bold bg-white border border-gray-100 text-gray-700">
                    储存：{{ s.storageMethod }}
                  </span>
                  <span v-if="s.deliveryMode" class="px-2.5 py-1 rounded-full text-xs font-bold bg-white border border-gray-100 text-gray-700">
                    交付：{{ s.deliveryMode }}
                  </span>
                  <span v-if="s.expireTime || s.expireMinutes != null" class="px-2.5 py-1 rounded-full text-xs font-bold bg-white border border-gray-100 text-gray-700">
                    有效期：{{ s.expireTime ? formatDate(s.expireTime) : '长期有效' }}
                  </span>
                  <span v-if="getParamsSummary(s.paramsJson) !== '无'" class="px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-50 border border-emerald-100 text-emerald-700">
                    指标：{{ getParamsSummary(s.paramsJson) }}
                  </span>
                </div>

                <div class="mt-4 pt-4 border-t border-gray-100 flex flex-col md:flex-row md:items-center justify-between gap-3">
                  <div class="text-xs text-gray-400">
                    创建：{{ formatDate(s.createTime) }} · 更新：{{ formatDate(s.updateTime) }}
                  </div>

                  <div class="flex flex-wrap items-center gap-2 md:justify-end">
                    <template v-if="s.status === 3">
                      <el-tooltip content="全部成交的供应建议保留为历史记录" placement="top">
                        <span>
                          <el-button class="!rounded-xl transition-all active:scale-95" disabled>
                            <span class="inline-flex items-center gap-2">
                              <Pencil class="w-4 h-4" />
                              编辑
                            </span>
                          </el-button>
                        </span>
                      </el-tooltip>
                    </template>
                    <template v-else>
                      <el-button class="!rounded-xl transition-all active:scale-95" @click="openEdit(s)">
                        <span class="inline-flex items-center gap-2">
                          <Pencil class="w-4 h-4" />
                          编辑
                        </span>
                      </el-button>
                    </template>

                    <el-button
                      v-if="s.status === 0 || s.status === 1"
                      class="!rounded-xl transition-all active:scale-95 !text-red-600 hover:!bg-red-50"
                      @click="revoke(s)"
                    >
                      <span class="inline-flex items-center gap-2">
                        <Ban class="w-4 h-4" />
                        下架
                      </span>
                    </el-button>

                    <el-button
                      v-else-if="s.status === 2"
                      type="primary"
                      class="!rounded-xl transition-all active:scale-95"
                      @click="republish(s)"
                    >
                      <span class="inline-flex items-center gap-2">
                        <RotateCcw class="w-4 h-4" />
                        再次发布
                      </span>
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="mt-6 flex justify-center">
            <el-pagination
              v-if="pagination.total > 0"
              v-model:current-page="pagination.page"
              :page-size="pagination.size"
              :total="pagination.total"
              layout="prev, pager, next"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>

      <el-dialog v-model="editOpen" width="720px" class="neo-dialog" title="编辑供应信息">
        <div class="p-6">
          <div class="text-xs text-gray-500 mb-4">
            仅修改本条已发布供应，不影响您的公司/个人档案
          </div>
          <div class="bg-gray-50 border border-gray-100 rounded-2xl p-4 mb-6">
            <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">标的</div>
            <div class="mt-1 font-bold text-gray-900">
              {{ editing?.categoryName || '-' }}
              <span v-if="editing?.supplyNo" class="text-sm text-gray-500 font-medium"> · {{ editing?.supplyNo }}</span>
            </div>
          </div>

          <el-form label-position="top" class="neo-dialog-form">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <el-form-item label="供应数量（吨）">
                <el-input-number v-model="editForm.quantity" class="w-full" :controls="false" :min="0" />
              </el-form-item>
              <el-form-item label="出厂价（元/吨）">
                <el-input-number v-model="editForm.exFactoryPrice" class="w-full" :controls="false" :min="0" />
              </el-form-item>
              <el-form-item label="产地">
                <el-input v-model="editForm.origin" placeholder="例如：山东济南…" />
              </el-form-item>
              <el-form-item label="发货地址">
                <el-input v-model="editForm.shipAddress" placeholder="例如：山东省济南市…" />
              </el-form-item>
              <el-form-item label="交付方式">
                <el-input v-model="editForm.deliveryMode" placeholder="例如：到厂 / 自提" />
              </el-form-item>
              <el-form-item label="包装方式">
                <el-input v-model="editForm.packaging" placeholder="例如：散装 / 袋装" />
              </el-form-item>
              <el-form-item label="储存方式">
                <el-input v-model="editForm.storageMethod" placeholder="例如：常温 / 冷藏" />
              </el-form-item>
              <el-form-item label="发布有效期（分钟，空=长期有效）">
                <el-input-number v-model="editForm.expireMinutes" class="w-full" :controls="false" :min="0" />
              </el-form-item>
              <el-form-item class="md:col-span-2" label="指标（JSON，可选）">
                <el-input v-model="editForm.paramsJson" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" placeholder='例如：{"custom":{"水分":"≤14%","霉变":"≤1%"}}' />
              </el-form-item>
              <el-form-item class="md:col-span-2" label="价格规则（JSON，可选）">
                <el-input v-model="editForm.priceRulesJson" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" placeholder='例如：[{"mode":"到厂","price":2800}]' />
              </el-form-item>
              <el-form-item class="md:col-span-2" label="备注">
                <el-input v-model="editForm.remark" type="textarea" :autosize="{ minRows: 2, maxRows: 5 }" placeholder="补充说明（选填）" />
              </el-form-item>
            </div>
          </el-form>
        </div>
        <template #footer>
          <div class="flex items-center justify-end gap-3 px-6 py-4">
            <el-button class="!rounded-xl transition-all active:scale-95" @click="editOpen = false">
              <span class="inline-flex items-center gap-2">
                <X class="w-4 h-4" />
                取消
              </span>
            </el-button>
            <el-button type="primary" class="!rounded-xl transition-all active:scale-95" :loading="saving" @click="saveEdit">
              保存修改
            </el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<style scoped>
/* 本页筛选条：Element Plus 输入控件统一为 Neo-Minimal（限定在 .neo-form 内） */
:deep(.neo-form .el-input__wrapper),
:deep(.neo-form .el-select__wrapper) {
  border: 2px solid rgb(243 244 246); /* gray-100 */
  border-radius: 12px; /* rounded-xl */
  box-shadow: none;
  background-color: #fff;
  transition: all 0.15s ease;
}
/* 修复：Element Plus el-select__wrapper 默认会按内容收缩，强制撑满父级宽度（父级由 w-[189px] 控制） */
:deep(.neo-form .neo-select-fixed .el-select__wrapper) {
  width: 189px !important;
  min-width: 189px !important;
}
:deep(.neo-form .el-input__wrapper.is-focus),
:deep(.neo-form .el-select__wrapper.is-focus) {
  border-color: rgb(16 185 129); /* emerald-500 */
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18);
}

/* 复用“neo-dialog”弹窗风格（与采购管理一致） */
:deep(.neo-dialog) {
  border-radius: 32px;
  overflow: hidden;
  border: 1px solid rgb(243 244 246);
  box-shadow: 0 25px 50px -12px rgb(0 0 0 / 0.25);
}
:deep(.neo-dialog .el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid rgb(243 244 246);
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
}
:deep(.neo-dialog .el-dialog__title) {
  font-weight: 800;
  color: rgb(17 24 39);
}
:deep(.neo-dialog .el-dialog__body) {
  padding: 0;
  background: #fff;
}
:deep(.neo-dialog .el-dialog__footer) {
  padding: 0;
  border-top: 1px solid rgb(243 244 246);
  background: rgb(249 250 251);
}

:deep(.neo-dialog-form .el-form-item__label) {
  font-weight: 800;
  font-size: 12px;
  color: rgb(107 114 128);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}
:deep(.neo-dialog-form .el-input__wrapper),
:deep(.neo-dialog-form .el-textarea__inner),
:deep(.neo-dialog-form .el-input-number .el-input__wrapper) {
  border: 2px solid rgb(243 244 246);
  border-radius: 12px;
  box-shadow: none;
  transition: all 0.15s ease;
}
:deep(.neo-dialog-form .el-input__wrapper.is-focus),
:deep(.neo-dialog-form .el-input-number .el-input__wrapper.is-focus),
:deep(.neo-dialog-form .el-textarea__inner:focus) {
  border-color: rgb(16 185 129);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18);
}
</style>


