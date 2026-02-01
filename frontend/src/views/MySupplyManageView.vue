<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Trash2, FileText, Save, Send, List, Package, Truck, Clock, FileCheck, TrendingUp, Plus, X, ChevronDown, ChevronUp, Pencil, Ban, RotateCcw, RefreshCcw, MapPin, DollarSign, Search, MoreHorizontal, Calendar, CreditCard, BoxIcon } from 'lucide-vue-next'
import { createSupply, getNextSupplyNo, createSupplyTemplate, getMySupplyTemplates, deleteSupplyTemplate, listSupplies, updateSupply, type SupplyCreateRequest, type BasisQuoteRequest, type SupplyTemplateResponse, type SupplyResponse, type SupplyUpdateRequest } from '../api/supply'
import { listFuturesContracts, type FuturesContractResponse } from '../api/futures'
import { getProductParams, type ProductParamResponse } from '../api/product'
import { getMyCompany, type CompanyResponse } from '../api/company'
import { getMe, type UserResponse } from '../api/user'
import { codeToText } from 'element-china-area-data'
import SchemaAwareCategoryPicker, { type PickedCategory } from '../components/SchemaAwareCategoryPicker.vue'
import CategoryParamsForm from '../components/CategoryParamsForm.vue'
import { getSchemaUnitConfig, getCategoryUnitConfig } from '../utils/schemaUnits'
import { BaseButton, BaseModal, EmptyState, Skeleton } from '../components/ui'
import TemplateCommandPalette, { type TemplateItem } from '../components/TemplateCommandPalette.vue'
import ProductInfoRow from '../components/ProductInfoRow.vue'
import { useCompanyStore } from '../stores/company'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const route = useRoute()
const companyStore = useCompanyStore()
const authStore = useAuthStore()
const loading = ref(false)

// ============ Tab 切换 ============
type TabType = 'publish' | 'published'
// 根据 URL 参数初始化 Tab
const initialTab = route.query.tab === 'published' ? 'published' : 'publish'
const activeTab = ref<TabType>(initialTab)

// ============ 已发布列表相关 ============
const supplies = ref<SupplyResponse[]>([])
const listLoading = ref(false)
const listPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})
const listFilters = reactive({
  categoryName: '',
  status: 0 as number | null  // 默认筛选"发布中"
})

const statusOptions = [
  { value: null, label: '全部', color: 'gray', icon: '○' },
  { value: 0, label: '发布中', color: 'emerald', icon: '●' },
  { value: 1, label: '部分成交', color: 'amber', icon: '◐' },
  { value: 2, label: '已下架', color: 'gray', icon: '○' },
  { value: 3, label: '全部成交', color: 'emerald', icon: '✓' }
]

const pagedSupplies = computed(() => {
  const start = (listPagination.page - 1) * listPagination.size
  const end = start + listPagination.size
  return supplies.value.slice(start, end)
})

// 发布中状态的数量（用于Tab显示）
const activeSuppliesCount = computed(() => {
  return supplies.value.filter(s => s.status === 0).length
})

async function loadSupplies() {
  listLoading.value = true
  try {
    const companyId = authStore.me?.companyId
    if (!companyId) {
      supplies.value = []
      listPagination.total = 0
      return
    }
    const r = await listSupplies({
      companyId,
      categoryName: listFilters.categoryName || undefined,
      status: listFilters.status ?? undefined,
      includeExpired: true
    })
    if (r.code === 0) {
      supplies.value = r.data || []
      listPagination.total = supplies.value.length
    } else {
      throw new Error(r.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载供应列表失败')
  } finally {
    listLoading.value = false
  }
}

function handleListFilter() {
  listPagination.page = 1
  loadSupplies()
}

function handlePageChange(page: number) {
  listPagination.page = page
}

function getStatusText(status?: number) {
  return statusOptions.find(o => o.value === status)?.label || '未知'
}

function getStatusColor(status?: number) {
  return statusOptions.find(o => o.value === status)?.color || 'gray'
}

function getStatusIcon(status?: number) {
  return statusOptions.find(o => o.value === status)?.icon || '○'
}

// 格式化过期时间
function formatExpireTime(expireTime?: string): string {
  if (!expireTime) return ''
  const expire = new Date(expireTime)
  const now = new Date()
  if (expire <= now) return '已过期'

  const diff = expire.getTime() - now.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))

  if (days > 0) return `剩${days}天`
  if (hours > 0) return `剩${hours}小时`
  return '即将过期'
}

// 格式化发布时间
function formatPublishTime(createTime?: string): string {
  if (!createTime) return ''
  const date = new Date(createTime)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor(diff / (1000 * 60))

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

// 计算成交进度百分比
function getDealProgress(s: SupplyResponse): number {
  if (!s.quantity || s.quantity <= 0) return 0
  const remaining = s.remainingQuantity ?? s.quantity
  const dealt = s.quantity - remaining
  return Math.round((dealt / s.quantity) * 100)
}

// 解析质量要求参数，返回标签数组
function parseParamsTags(paramsJson?: string): { label: string; value: string }[] {
  if (!paramsJson) return []
  try {
    const params = JSON.parse(paramsJson)
    if (typeof params !== 'object' || params === null) return []
    const entries = Object.entries(params).filter(([_, v]) => v !== undefined && v !== '')
    return entries.slice(0, 5).map(([k, v]) => ({ label: String(k), value: String(v) }))
  } catch {
    return []
  }
}

// 编辑已发布的供应
const editOpen = ref(false)
const saving = ref(false)
const editing = ref<SupplyResponse | null>(null)
const editForm = reactive<SupplyUpdateRequest>({
  quantity: undefined,
  exFactoryPrice: undefined,
  shipAddress: undefined,
  deliveryMode: undefined,
  paymentMethod: undefined,
  invoiceType: undefined,
  packaging: undefined,
  storageMethod: undefined,
  expireMinutes: undefined,
  paramsJson: undefined,
  priceRulesJson: undefined,
  remark: undefined
})

function openEdit(s: SupplyResponse) {
  editing.value = s
  editForm.quantity = s.quantity
  editForm.exFactoryPrice = s.exFactoryPrice
  editForm.shipAddress = s.shipAddress
  editForm.deliveryMode = s.deliveryMode
  editForm.paymentMethod = s.paymentMethod
  editForm.invoiceType = s.invoiceType
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
      quantity: editForm.quantity,
      exFactoryPrice: editForm.exFactoryPrice,
      shipAddress: editForm.shipAddress,
      deliveryMode: editForm.deliveryMode,
      paymentMethod: editForm.paymentMethod,
      invoiceType: editForm.invoiceType,
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

async function revokeSupply(s: SupplyResponse) {
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

async function republishSupply(s: SupplyResponse) {
  if (!s.id) return
  try {
    await ElMessageBox.confirm('将该供应重新发布到大厅，并按有效期重新计时。', '再次发布？', {
      confirmButtonText: '发布',
      cancelButtonText: '取消',
      type: 'info'
    })
    const r = await updateSupply(s.id, { status: 0, expireMinutes: s.expireMinutes ?? 4320 })
    if (r.code !== 0) throw new Error(r.message)
    ElMessage.success('已再次发布')
    await loadSupplies()
  } catch (e: any) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e?.message || '操作失败')
  }
}

// 模板下拉菜单
const templateMenuOpen = ref(false)

// 可选区域折叠
const sectionsCollapsed = reactive({
  publishInfo: false,
  logistics: false
})
const supplyNo = ref<string>('')
const templatePickerOpen = ref(false)

// 公司与用户信息
const company = ref<CompanyResponse | null>(null)
const meUser = ref<UserResponse | null>(null)
const publisherNameInput = ref('')

// 发布表单
const publishForm = reactive({
  categoryId: undefined as number | undefined,
  categoryName: '',
  companyName: '',
  priceType: 0, // 0=现货一口价, 1=基差报价
  exFactoryPrice: undefined as number | undefined,
  quantity: undefined as number | undefined,
  packaging: '散装',
  shipAddress: '',
  deliveryMode: '到厂',
  paymentMethod: '现款',
  invoiceType: '',
  expireMinutes: 4320,
  priceRulesJson: '{}',
  paramsJson: '{}',
  remark: ''
})

// 基差报价相关
const futuresContracts = ref<FuturesContractResponse[]>([])
const basisQuotes = ref<{ contractCode: string; basisPrice: number | undefined; availableQty: number | undefined }[]>([])

// 支持基差报价的品类（豆粕、菜粕等）
const basisSupportedCategories = ['豆粕', '菜粕', '豆油', '菜油']
const showBasisOption = computed(() => {
  if (!publishForm.categoryName) return false
  return basisSupportedCategories.some(c => publishForm.categoryName.includes(c))
})

// 品类到期货合约代码的映射
const categoryToProductCode: Record<string, string> = {
  '豆粕': 'M',
  '菜粕': 'RM',
  '豆油': 'Y',
  '菜油': 'OI'
}

// 获取当前品类对应的期货品种代码
function getProductCodeForCategory(): string | null {
  if (!publishForm.categoryName) return null
  for (const [cat, code] of Object.entries(categoryToProductCode)) {
    if (publishForm.categoryName.includes(cat)) return code
  }
  return null
}

// 品类相关
const categoryParams = ref<ProductParamResponse[]>([])
const dynamicParams = ref<Record<string, any>>({})
const customParams = ref<Array<{ name: string; value: string }>>([])

// 业态与品类选择器
const selectedSchemaCode = ref<string>('feed')
const pickedCategory = ref<PickedCategory | null>(null)
const suspendCategoryWatch = ref(false)

// 当前单位配置（根据业态和品类动态计算）
const currentUnitConfig = computed(() => {
  return getCategoryUnitConfig(selectedSchemaCode.value, publishForm.categoryName || '')
})

// 包装和交付选项（根据业态动态变化）
const currentSchemaConfig = computed(() => {
  return getSchemaUnitConfig(selectedSchemaCode.value)
})

// 模板系统 - 使用 API 响应类型
const templates = ref<SupplyTemplateResponse[]>([])
const saveTemplateDialogVisible = ref(false)
const templateNameInput = ref('')

type TemplateJsonData = {
  templateName?: string
  companyName?: string
  publisherName?: string
  categoryId?: number
  categoryName?: string
  exFactoryPrice?: number
  quantity?: number
  packaging?: string
  shipAddress?: string
  deliveryMode?: string
  paymentMethod?: string
  invoiceType?: string
  expireMinutes?: number
  paramsJson?: string
  remark?: string
}

// 预解析模板数据，转换为 TemplateItem 格式
const parsedTemplates = computed<TemplateItem[]>(() =>
  templates.value.map(t => {
    let parsed: TemplateJsonData = {}
    try {
      parsed = (JSON.parse(t.templateJson || '') ?? {}) as TemplateJsonData
    } catch {
      parsed = {}
    }
    const schemaConfig = getSchemaUnitConfig(parsed.schemaCode || 'feed')
    return {
      id: t.id,
      name: t.templateName,
      category: parsed.categoryName || '未分类',
      quantity: parsed.quantity,
      quantityUnit: schemaConfig.quantityUnit,
      price: parsed.exFactoryPrice,
      priceUnit: schemaConfig.priceUnit,
      // 保留原始数据用于应用模板
      _raw: t
    } as TemplateItem & { _raw: typeof t }
  })
)

function formatPrice(p?: number) {
  const n = Number(p)
  if (!p && p !== 0) return '面议'
  if (Number.isNaN(n)) return '面议'
  return `¥${n}`
}

function formatDate(dateStr?: string) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (Number.isNaN(d.getTime())) return ''
  return d.toLocaleDateString('zh-CN')
}

const previewData = computed(() => {
  const expireDays = publishForm.expireMinutes ? Math.floor(publishForm.expireMinutes / 1440) : 0
  const expireHours = publishForm.expireMinutes ? Math.floor((publishForm.expireMinutes % 1440) / 60) : 0
  const expireText = expireDays > 0 ? `${expireDays}天` : expireHours > 0 ? `${expireHours}小时` : '未设置'
  
  let paramsText = '无'
  try {
    if (categoryParams.value.length > 0) {
      const paramList: string[] = []
      categoryParams.value.forEach(param => {
        const value = dynamicParams.value[param.id]
        if (value !== undefined && value !== '') {
          paramList.push(`${param.paramName}: ${value}`)
        }
      })
      paramsText = paramList.length > 0 ? paramList.join('; ') : '无'
    }
  } catch { paramsText = '无' }
  
  return {
    supplyNo: supplyNo.value || '自动生成',
    companyName: publishForm.companyName || '未指定',
    categoryName: publishForm.categoryName || '未选择',
    exFactoryPrice: publishForm.exFactoryPrice,
    quantity: publishForm.quantity || 0,
    packaging: publishForm.packaging || '未指定',
    shipAddress: publishForm.shipAddress || '未指定',
    deliveryMode: publishForm.deliveryMode || '到厂',
    expireText,
    paramsText,
    remark: publishForm.remark || ''
  }
})

const publisherName = computed(() => {
  const real = meUser.value?.realName?.trim()
  if (real) return real
  return meUser.value?.nickName || meUser.value?.userName || '—'
})

onMounted(async () => {
  await Promise.all([loadCompanyInfo(), loadMeUser(), loadTemplates(), loadFuturesContracts()])
  await loadNextSupplyNo()
  // 如果初始 Tab 是已发布，自动加载列表
  if (activeTab.value === 'published') {
    loadSupplies()
  }
})

// Tab 切换时加载对应数据
watch(activeTab, (tab) => {
  if (tab === 'published' && supplies.value.length === 0) {
    loadSupplies()
  }
})

// 加载期货合约列表
async function loadFuturesContracts() {
  try {
    const r = await listFuturesContracts()
    if (r.code === 0) {
      futuresContracts.value = r.data || []
    }
  } catch { /* silent */ }
}

// 添加基差报价行
function addBasisQuote() {
  basisQuotes.value.push({ contractCode: '', basisPrice: undefined, availableQty: undefined })
}

// 删除基差报价行
function removeBasisQuote(index: number) {
  basisQuotes.value.splice(index, 1)
}

// 获取合约选项（按品种过滤、过滤已过期、过滤已选择的，限未来24个月内显示未来5档）
function getAvailableContracts(currentCode: string) {
  const selectedCodes = basisQuotes.value.map(q => q.contractCode).filter(c => c && c !== currentCode)
  const productCode = getProductCodeForCategory()
  const today = new Date()
  const currentMonth = new Date(today.getFullYear(), today.getMonth(), 1) // 当月1号
  
  const filtered = futuresContracts.value
    .filter(c => {
      // 1. 过滤已选择的合约
      if (selectedCodes.includes(c.contractCode)) return false
      // 2. 按品种过滤（豆粕只显示M系列，菜粕只显示RM系列）
      if (productCode && c.productCode !== productCode) return false
      // 3. 过滤已过期合约（交割月 < 当前月）
      if (c.deliveryMonth) {
        const deliveryDate = new Date(c.deliveryMonth)
        if (deliveryDate < currentMonth) return false
      }
      return true
    })
    .sort((a, b) => {
      if (!a.deliveryMonth || !b.deliveryMonth) return 0
      return new Date(a.deliveryMonth).getTime() - new Date(b.deliveryMonth).getTime()
    })

  // 限制显示未来 5 个活跃月份合约
  return filtered.slice(0, 5)
}

// 根据合约代码获取合约信息
function getContractByCode(code: string): FuturesContractResponse | undefined {
  return futuresContracts.value.find(c => c.contractCode === code)
}

// 计算核算价格（期货价 + 基差）
function calcReferencePrice(contractCode: string, basisPrice: number | undefined): number | null {
  if (basisPrice === undefined) return null
  const contract = getContractByCode(contractCode)
  if (!contract?.lastPrice) return null
  return contract.lastPrice + basisPrice
}

// 监听品类变化，自动切换报价类型
watch(() => publishForm.categoryName, (newName) => {
  if (newName && basisSupportedCategories.some(c => newName.includes(c))) {
    // 如果是支持基差的品类，可以选择基差模式
    // 默认仍为现货模式，用户可以切换
  } else {
    // 不支持基差的品类，强制现货模式
    publishForm.priceType = 0
    basisQuotes.value = []
  }
})

// 监听报价类型变化
watch(() => publishForm.priceType, (newType) => {
  if (newType === 1 && basisQuotes.value.length === 0) {
    // 切换到基差模式时，自动添加一行
    addBasisQuote()
  }
})

async function loadCompanyInfo() {
  try {
    const r = await getMyCompany()
    if (r.code === 0 && r.data) {
      company.value = r.data
      publishForm.companyName = r.data.companyName || ''
      
      // 提取地址部分，处理可能的代码格式
      const p = r.data.province && /^\d+$/.test(r.data.province) ? codeToText[r.data.province] : r.data.province
      const c = r.data.city && /^\d+$/.test(r.data.city) ? codeToText[r.data.city] : r.data.city
      const d = r.data.district && /^\d+$/.test(r.data.district) ? codeToText[r.data.district] : r.data.district
      
      const fullAddress = [p, c, d, r.data.address].filter(Boolean).join('')
      publishForm.shipAddress = fullAddress || ''
    }
  } catch { /* silent */ }
}

async function loadMeUser() {
  try {
    const r = await getMe()
    if (r.code === 0) meUser.value = r.data ?? null
    if (!publisherNameInput.value) publisherNameInput.value = publisherName.value
  } catch { /* silent */ }
}

async function loadNextSupplyNo() {
  try {
    const r = await getNextSupplyNo()
    if (r.code === 0 && r.data) supplyNo.value = r.data
  } catch { /* silent */ }
}

// 业态切换处理
function onSchemaChange(schemaCode: string) {
  selectedSchemaCode.value = schemaCode
  // 重置包装方式为新业态的默认值
  const config = getSchemaUnitConfig(schemaCode)
  if (config.packagingOptions.length > 0) {
    publishForm.packaging = config.packagingOptions[0] ?? '散装'
  }
  if (config.deliveryOptions.length > 0) {
    publishForm.deliveryMode = config.deliveryOptions[0] ?? '到厂'
  }
}

async function loadTemplates() {
  try {
    const res = await getMySupplyTemplates()
    if (res.code === 0) {
      templates.value = res.data || []
    }
  } catch (e) {
    console.error('加载模板失败', e)
  }
}

watch(
  () => pickedCategory.value?.id,
  async (categoryId) => {
    if (suspendCategoryWatch.value) return
    const category = pickedCategory.value
    if (category && categoryId) {
      publishForm.categoryId = category.id
      publishForm.categoryName = category.name
      if (category.schemaCode) {
        selectedSchemaCode.value = category.schemaCode
      }
      await loadCategoryParams(category.id)
      await loadNextSupplyNo()
    } else {
      publishForm.categoryId = undefined
      publishForm.categoryName = ''
      categoryParams.value = []
      dynamicParams.value = {}
      customParams.value = []
    }
  }
)

async function loadCategoryParams(productId: number) {
  try {
    const r = await getProductParams(productId)
    if (r.code === 0) {
      categoryParams.value = r.data || []
      const params: Record<string, any> = {}
      categoryParams.value.forEach(param => { params[param.id] = '' })
      dynamicParams.value = params
    }
  } catch { ElMessage.error('加载品类参数失败') }
}

function buildParamsJson() {
  const params: Record<string, any> = {}
  categoryParams.value.forEach(param => {
    const value = dynamicParams.value[param.id]
    if (value !== undefined && value !== '') {
      params[param.paramName] = value
    }
  })
  customParams.value.forEach(cp => {
    if (cp.name.trim() && cp.value.trim()) {
      params[cp.name.trim()] = cp.value.trim()
    }
  })
  return JSON.stringify(params)
}

async function publishSupply() {
  if (!publishForm.categoryId) { ElMessage.warning('请选择品类'); return }
  
  // 验证报价
  if (publishForm.priceType === 1) {
    const validQuotes = basisQuotes.value.filter(q => q.contractCode && q.basisPrice !== undefined && q.availableQty && q.availableQty > 0)
    if (validQuotes.length === 0) {
      ElMessage.warning('请至少添加一条有效的基差报价')
      return
    }
  } else {
    if (!publishForm.exFactoryPrice) { ElMessage.warning('请输入出厂价'); return }
  }
  
  loading.value = true
  try {
    const paramsJson = buildParamsJson()

    // 构建基差报价明细
    let basisQuotesData: BasisQuoteRequest[] | undefined
    if (publishForm.priceType === 1) {
      basisQuotesData = basisQuotes.value
        .filter(q => q.contractCode && q.basisPrice !== undefined && q.availableQty && q.availableQty > 0)
        .map(q => ({
          contractCode: q.contractCode,
          basisPrice: q.basisPrice!,
          availableQty: q.availableQty!
        }))
    }
    
    const req: SupplyCreateRequest = {
      categoryName: publishForm.categoryName,
      supplyNo: supplyNo.value || undefined,
      quantity: publishForm.quantity,
      packaging: publishForm.packaging || undefined,
      priceType: publishForm.priceType,
      exFactoryPrice: publishForm.priceType === 0 ? publishForm.exFactoryPrice : 0,
      basisQuotes: basisQuotesData,
      shipAddress: publishForm.shipAddress || undefined,
      deliveryMode: publishForm.deliveryMode || undefined,
      paymentMethod: publishForm.paymentMethod || undefined,
      invoiceType: publishForm.invoiceType || undefined,
      expireMinutes: publishForm.expireMinutes,
      priceRulesJson: publishForm.priceRulesJson || '{}',
      paramsJson,
      remark: publishForm.remark || undefined
    }
    
    const r = await createSupply(req)
    if (r.code === 0) {
      ElMessage.success('发布成功')
      // 清除企业资料缓存，确保企业主页显示最新数据
      if (company.value?.id) {
        companyStore.invalidateProfile(company.value.id)
      }
      // 切换到已发布Tab并刷新列表
      activeTab.value = 'published'
      await loadSupplies()
    } else {
      ElMessage.error(r.message || '发布失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '发布失败')
  } finally {
    loading.value = false
  }
}

async function saveAsTemplate() {
  if (!publishForm.categoryId) { ElMessage.warning('请先选择品类'); return }
  saveTemplateDialogVisible.value = true
}

async function confirmSaveTemplate() {
  if (!templateNameInput.value.trim()) { ElMessage.warning('请输入模板名称'); return }
  
  loading.value = true
  try {
    const paramsJson = buildParamsJson()
    const templateJson = JSON.stringify({
      companyName: publishForm.companyName,
      publisherName: publisherNameInput.value,
      schemaCode: selectedSchemaCode.value,
      categoryId: publishForm.categoryId,
      categoryName: publishForm.categoryName,
      exFactoryPrice: publishForm.exFactoryPrice,
      quantity: publishForm.quantity,
      packaging: publishForm.packaging,
      shipAddress: publishForm.shipAddress,
      deliveryMode: publishForm.deliveryMode,
      paymentMethod: publishForm.paymentMethod,
      invoiceType: publishForm.invoiceType,
      paramsJson,
      expireMinutes: publishForm.expireMinutes,
      remark: publishForm.remark
    })
    
    const res = await createSupplyTemplate({
      templateName: templateNameInput.value.trim(),
      templateJson
    })
    
    if (res.code === 0) {
      ElMessage.success('模板保存成功')
      saveTemplateDialogVisible.value = false
      templateNameInput.value = ''
      await loadTemplates() // 重新加载模板列表
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    loading.value = false
  }
}

async function deleteTemplate(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除此模板吗？删除后无法恢复。', '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteSupplyTemplate(id)
    if (res.code === 0) {
      templates.value = templates.value.filter(t => t.id !== id)
      ElMessage.success('模板删除成功')
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e: any) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e?.message || '删除失败')
  }
}

// 处理模板选择（从 CommandPalette）
function handleTemplateSelect(item: TemplateItem & { _raw?: SupplyTemplateResponse }) {
  if (item._raw) {
    applyTemplate(item._raw)
  }
}

async function applyTemplate(template: SupplyTemplateResponse) {
  try {
    const data = JSON.parse(template.templateJson)

    suspendCategoryWatch.value = true

    // 恢复业态代码
    if (data.schemaCode) {
      selectedSchemaCode.value = data.schemaCode
    }

    // 批量更新 publishForm，减少重渲染次数
    Object.assign(publishForm, {
      companyName: data.companyName || publishForm.companyName,
      categoryId: data.categoryId,
      categoryName: data.categoryName || '',
      exFactoryPrice: data.exFactoryPrice,
      quantity: data.quantity,
      packaging: data.packaging || '散装',
      shipAddress: data.shipAddress || '',
      deliveryMode: data.deliveryMode || '到厂',
      paymentMethod: data.paymentMethod || '现款',
      invoiceType: data.invoiceType || '',
      expireMinutes: data.expireMinutes || 4320,
      remark: data.remark || ''
    })

    publisherNameInput.value = data.publisherName || publisherNameInput.value
    pickedCategory.value = data.categoryId ? {
      id: data.categoryId,
      name: data.categoryName || String(data.categoryId),
      schemaCode: data.schemaCode || 'feed'
    } : null

    if (data.categoryId) {
      await loadCategoryParams(data.categoryId)
    } else {
      categoryParams.value = []
      dynamicParams.value = {}
    }

    // 恢复参数值（必须在 loadCategoryParams 之后）
    if (data.paramsJson) {
      try {
        const paramsData = JSON.parse(data.paramsJson)
        const isNewFormat = !paramsData.params && !paramsData.custom

        if (isNewFormat) {
          Object.entries(paramsData).forEach(([name, value]) => {
            const param = categoryParams.value.find(p => p.paramName === name)
            if (param) dynamicParams.value[param.id] = value
          })
        } else {
          const oldParams = paramsData.params || {}
          Object.entries(oldParams).forEach(([paramId, val]: [string, any]) => {
            const actualValue = (typeof val === 'object' && val !== null && 'value' in val) ? val.value : val
            dynamicParams.value[Number(paramId)] = actualValue
          })
        }
      } catch { /* ignore */ }
    }

    suspendCategoryWatch.value = false
    templatePickerOpen.value = false
    ElMessage.success('模板已应用')
  } catch {
    suspendCategoryWatch.value = false
    ElMessage.error('模板数据格式错误')
  }
}
</script>

<template>
  <div class="space-y-6">
    <!-- 页面标题 + Tab 切换 -->
    <div class="flex items-center justify-between flex-wrap gap-4">
      <div class="flex items-center gap-6">
        <h1 class="text-2xl font-bold text-neutral-900">供应管理</h1>
        <!-- Tab 切换 -->
        <div class="flex items-center bg-neutral-100 rounded-xl p-1">
          <button
            :class="[
              'px-4 py-2 rounded-lg text-sm font-bold transition-all',
              activeTab === 'publish'
                ? 'bg-white text-brand-600 shadow-sm'
                : 'text-neutral-500 hover:text-neutral-700'
            ]"
            @click="activeTab = 'publish'"
          >
            <span class="flex items-center gap-2">
              <span class="w-2 h-2 rounded-full" :class="activeTab === 'publish' ? 'bg-brand-500' : 'bg-neutral-300'"></span>
              发布供应
            </span>
          </button>
          <button
            :class="[
              'px-4 py-2 rounded-lg text-sm font-bold transition-all',
              activeTab === 'published'
                ? 'bg-white text-brand-600 shadow-sm'
                : 'text-neutral-500 hover:text-neutral-700'
            ]"
            @click="activeTab = 'published'"
          >
            <span class="flex items-center gap-2">
              <span class="w-2 h-2 rounded-full" :class="activeTab === 'published' ? 'bg-brand-500' : 'bg-neutral-300'"></span>
              已发布
              <span v-if="activeSuppliesCount > 0" class="px-1.5 py-0.5 bg-brand-100 text-brand-600 text-[10px] rounded-full">
                {{ activeSuppliesCount }}
              </span>
            </span>
          </button>
        </div>
      </div>
      <!-- 发布Tab的操作按钮 -->
      <div v-if="activeTab === 'publish'" class="flex items-center gap-3">
        <!-- 模板下拉菜单 -->
        <div class="relative">
          <BaseButton type="secondary" size="sm" @click="templateMenuOpen = !templateMenuOpen">
            <FileText class="w-4 h-4" />
            模板
            <ChevronDown class="w-3 h-3 ml-1" />
          </BaseButton>
          <Transition name="dropdown">
            <div
              v-if="templateMenuOpen"
              class="absolute right-0 mt-2 w-40 bg-white rounded-xl shadow-lg border border-neutral-200 py-1 z-50"
              @click="templateMenuOpen = false"
            >
              <button
                class="w-full px-4 py-2.5 text-left text-sm font-medium text-neutral-700 hover:bg-neutral-50 flex items-center gap-2"
                @click="templatePickerOpen = true"
              >
                <FileText class="w-4 h-4 text-neutral-400" />
                选择模板
              </button>
              <button
                class="w-full px-4 py-2.5 text-left text-sm font-medium text-neutral-700 hover:bg-neutral-50 flex items-center gap-2"
                @click="saveAsTemplate"
              >
                <Save class="w-4 h-4 text-neutral-400" />
                保存为模板
              </button>
            </div>
          </Transition>
        </div>
        <BaseButton type="primary" size="sm" :loading="loading" @click="publishSupply">
          <Send class="w-4 h-4" />
          发布
        </BaseButton>
      </div>
      <!-- 已发布Tab的操作按钮 -->
      <div v-else class="flex items-center gap-3">
        <BaseButton type="secondary" size="sm" :loading="listLoading" @click="loadSupplies">
          <RefreshCcw class="w-4 h-4" />
          刷新
        </BaseButton>
        <BaseButton type="primary" size="sm" @click="activeTab = 'publish'">
          <Plus class="w-4 h-4" />
          发布新供应
        </BaseButton>
      </div>
    </div>

    <!-- 点击外部关闭模板菜单 -->
    <div v-if="templateMenuOpen" class="fixed inset-0 z-40" @click="templateMenuOpen = false"></div>

    <!-- ========== 已发布 Tab 内容 ========== -->
    <template v-if="activeTab === 'published'">
      <!-- 筛选栏 -->
      <div class="bg-white rounded-xl border border-neutral-200 p-4">
        <div class="flex flex-wrap items-center gap-4">
          <!-- 搜索框 -->
          <div class="relative flex-1 min-w-[200px] max-w-[300px]">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-neutral-400" />
            <input
              v-model="listFilters.categoryName"
              type="text"
              placeholder="搜索品类..."
              class="w-full pl-10 pr-4 py-2.5 border-2 border-neutral-200 rounded-xl text-sm focus:border-brand-500 outline-none transition-all"
              @keyup.enter="handleListFilter"
            />
          </div>

          <!-- 状态筛选 -->
          <div class="flex gap-2">
            <button
              v-for="opt in statusOptions"
              :key="opt.value ?? 'all'"
              :class="[
                'px-3 py-2 text-xs font-bold rounded-xl transition-all flex items-center gap-1.5',
                listFilters.status === opt.value
                  ? 'bg-brand-600 text-white'
                  : 'bg-neutral-100 text-neutral-600 hover:bg-neutral-200'
              ]"
              @click="listFilters.status = opt.value; handleListFilter()"
            >
              <span class="text-[10px]">{{ opt.icon }}</span>
              {{ opt.label }}
            </button>
          </div>

          <!-- 统计 -->
          <div class="ml-auto">
            <span class="px-3 py-1.5 bg-neutral-50 text-neutral-600 text-xs font-bold rounded-full border border-neutral-200">
              共 {{ listPagination.total }} 条
            </span>
          </div>
        </div>
      </div>

      <!-- 供应列表（紧凑表格风格） -->
      <div class="bg-white rounded-xl border border-neutral-200 overflow-hidden">
        <!-- 加载状态 -->
        <div v-if="listLoading && supplies.length === 0" class="p-6 space-y-4">
          <Skeleton type="card" />
          <Skeleton type="card" />
          <Skeleton type="card" />
        </div>

        <!-- 空状态 -->
        <EmptyState
          v-else-if="supplies.length === 0"
          type="empty"
          title="暂无供应信息"
          description="点击上方「发布供应」标签开始发布"
          size="md"
        />

        <!-- 产品信息列表 -->
        <div v-else class="p-4 space-y-3">
          <div
            v-for="(s, index) in pagedSupplies"
            :key="s.id"
            class="bg-white rounded-2xl border border-neutral-200 p-4 hover:shadow-md hover:border-brand-200 transition-all duration-200 animate-stagger-in"
            :style="{ animationDelay: `${index * 40}ms` }"
          >
            <ProductInfoRow
              :data="{
                categoryName: s.categoryName || '未知品类',
                quantity: s.quantity,
                quantityUnit: currentUnitConfig.quantityUnit,
                price: s.priceType === 1 ? '基差报价' : s.exFactoryPrice,
                priceUnit: currentUnitConfig.quantityUnit,
                address: s.shipAddress,
                packaging: s.packaging,
                paymentMethod: s.paymentMethod,
                paramsJson: s.paramsJson,
                expireTime: s.expireTime
              }"
              type="supply"
            >
              <!-- 基差报价详情 -->
              <template v-if="s.priceType === 1 && s.basisQuotes?.length" #extra>
                <div class="mt-2 flex flex-wrap gap-2">
                  <div
                    v-for="bq in (s.basisQuotes || []).slice(0, 3)"
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
              <template #status>
                <span
                  :class="[
                    'inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold whitespace-nowrap',
                    s.status === 0 ? 'bg-brand-50 text-brand-600' :
                    s.status === 1 ? 'bg-warning-50 text-warning-600' :
                    s.status === 3 ? 'bg-brand-50 text-brand-600' :
                    'bg-neutral-100 text-neutral-500'
                  ]"
                >
                  {{ getStatusIcon(s.status) }} {{ getStatusText(s.status) }}
                  <template v-if="s.status === 1 && s.remainingQuantity != null"> · {{ getDealProgress(s) }}%</template>
                </span>
              </template>
              <template #actions>
                <button
                  v-if="s.status !== 3"
                  class="px-2 py-0.5 rounded text-[11px] font-medium text-neutral-600 hover:bg-neutral-100 transition-colors"
                  @click="openEdit(s)"
                >编辑</button>
                <button
                  v-if="s.status === 0 || s.status === 1"
                  class="px-2 py-0.5 rounded text-[11px] font-medium text-error-600 hover:bg-error-50 transition-colors"
                  @click="revokeSupply(s)"
                >下架</button>
                <button
                  v-else-if="s.status === 2"
                  class="px-2 py-0.5 rounded text-[11px] font-medium text-brand-600 hover:bg-brand-50 transition-colors"
                  @click="republishSupply(s)"
                >再发布</button>
              </template>
            </ProductInfoRow>
          </div>

          <!-- 分页 -->
          <div v-if="listPagination.total > listPagination.size" class="pt-4 flex justify-center">
            <el-pagination
              v-model:current-page="listPagination.page"
              :page-size="listPagination.size"
              :total="listPagination.total"
              layout="prev, pager, next"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </template>

    <!-- ========== 发布 Tab 内容 ========== -->
    <template v-else>
    <!-- 双栏布局 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧表单区域 -->
      <div class="lg:col-span-2 space-y-6">
        <!-- 发布信息 -->
        <div class="bg-white rounded-xl border border-neutral-200 overflow-hidden animate-fade-in">
          <div class="p-5 border-b border-neutral-200 flex items-center gap-2">
            <div class="w-1.5 h-5 bg-slate-900 rounded-full"></div>
            <h3 class="text-2xl font-bold text-neutral-900">发布信息</h3>
          </div>
          <div class="p-5 space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">公司名称</label>
                <input
                  v-model="publishForm.companyName"
                  type="text"
                  placeholder="默认使用公司名称"
                  class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-brand-500 outline-none transition-all"
                />
              </div>
              <div>
                <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">发布人</label>
                <input
                  v-model="publisherNameInput"
                  type="text"
                  placeholder="默认使用个人信息"
                  class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-brand-500 outline-none transition-all"
                />
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">发货地址</label>
              <input
                v-model="publishForm.shipAddress"
                type="text"
                placeholder="请输入发货地址"
                class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-brand-500 outline-none transition-all"
              />
            </div>
            <p class="text-xs text-neutral-400">以上信息仅用于本次发布，不会修改您的公司/个人资料</p>
          </div>
        </div>

        <!-- 基础信息 -->
        <div class="bg-white rounded-xl border border-neutral-200 overflow-hidden animate-fade-in" style="animation-delay: 50ms">
          <div class="p-5 border-b border-neutral-200 flex items-center gap-2">
            <div class="w-1.5 h-5 bg-brand-600 rounded-full"></div>
            <h3 class="text-2xl font-bold text-neutral-900">基础信息</h3>
          </div>
          <div class="p-5 space-y-4">
            <div class="space-y-4">
              <div>
                <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">
                  业态与品类 <span class="text-red-500">*</span>
                </label>
                <SchemaAwareCategoryPicker
                  v-model="pickedCategory"
                  @schema-change="onSchemaChange"
                />
              </div>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mt-4">
              <div>
                <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">
                  {{ currentUnitConfig.quantityLabel }}
                </label>
                <el-input-number
                  v-model="publishForm.quantity"
                  :min="0"
                  :step="currentSchemaConfig.quantityStep"
                  :controls="false"
                  :placeholder="currentSchemaConfig.quantityPlaceholder"
                  class="w-full neo-input-number"
                />
              </div>
              <div v-if="showBasisOption">
                <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">报价方式</label>
                <el-radio-group v-model="publishForm.priceType" class="w-full">
                  <el-radio-button :value="0" class="flex-1">现货一口价</el-radio-button>
                  <el-radio-button :value="1" class="flex-1">基差报价</el-radio-button>
                </el-radio-group>
              </div>
              <div v-else>
                <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">
                  {{ currentUnitConfig.priceLabel }} <span class="text-red-500">*</span>
                </label>
                <el-input-number
                  v-model="publishForm.exFactoryPrice"
                  :min="0"
                  :step="currentSchemaConfig.priceStep"
                  :controls="false"
                  :placeholder="currentSchemaConfig.pricePlaceholder"
                  class="w-full neo-input-number"
                />
              </div>
            </div>
            
            <!-- 现货一口价输入 -->
            <div v-if="showBasisOption && publishForm.priceType === 0" class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">
                  {{ currentUnitConfig.priceLabel }} <span class="text-red-500">*</span>
                </label>
                <el-input-number
                  v-model="publishForm.exFactoryPrice"
                  :min="0"
                  :step="currentSchemaConfig.priceStep"
                  :controls="false"
                  :placeholder="currentSchemaConfig.pricePlaceholder"
                  class="w-full neo-input-number"
                />
              </div>
            </div>
            
            <!-- 基差报价输入区域 -->
            <div v-if="showBasisOption && publishForm.priceType === 1" class="space-y-4">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-2">
                  <TrendingUp class="w-4 h-4 text-warning-500" />
                  <span class="text-sm font-bold text-neutral-700">基差报价明细</span>
                </div>
                <button
                  type="button"
                  class="flex items-center gap-1 px-3 py-1.5 bg-warning-50 text-warning-600 rounded-lg text-sm font-bold hover:bg-warning-100 transition-all"
                  @click="addBasisQuote"
                >
                  <Plus class="w-4 h-4" />
                  添加合约
                </button>
              </div>
              
              <div class="bg-warning-50/50 rounded-xl p-4 border border-warning-100">
                <div v-if="basisQuotes.length === 0" class="text-center py-4 text-neutral-500 text-sm">
                  点击"添加合约"开始配置基差报价
                </div>
                <div v-else class="space-y-4">
                  <div v-for="(quote, index) in basisQuotes" :key="index" class="bg-white rounded-xl p-4 border border-neutral-200 shadow-sm transition-all hover:shadow-md">
                    <div class="flex flex-col md:flex-row md:items-start gap-4">
                      <div class="flex-1 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                        <div class="space-y-1">
                          <label class="block text-[10px] font-bold text-neutral-400 uppercase tracking-widest">1. 选择期货合约</label>
                          <el-select v-model="quote.contractCode" placeholder="选择活跃合约" class="w-full neo-select" filterable>
                            <el-option
                              v-for="c in getAvailableContracts(quote.contractCode)"
                              :key="c.contractCode"
                              :label="c.contractName"
                              :value="c.contractCode"
                            >
                              <div class="flex items-center justify-between gap-4">
                                <span>{{ c.contractName }}</span>
                                <div class="flex items-center gap-2">
                                  <span v-if="c.lastPrice" class="text-xs font-bold">¥{{ c.lastPrice }}</span>
                                  <span v-if="c.isTrading" class="w-1.5 h-1.5 rounded-full bg-brand-500"></span>
                                  <span v-else class="text-[8px] text-neutral-400">已收盘</span>
                                </div>
                              </div>
                            </el-option>
                          </el-select>
                        </div>
                        <div class="space-y-1">
                          <label class="block text-[10px] font-bold text-neutral-400 uppercase tracking-widest">2. 设定基差 (元/吨)</label>
                          <el-input-number 
                            v-model="quote.basisPrice" 
                            :step="5" 
                            :controls="false" 
                            placeholder="如 +80 或 -20" 
                            class="w-full neo-input-number"
                            :class="(quote.basisPrice || 0) >= 0 ? 'basis-plus' : 'basis-minus'"
                          />
                        </div>
                        <div class="space-y-1">
                          <label class="block text-[10px] font-bold text-neutral-400 uppercase tracking-widest">3. 合约可售量 (吨)</label>
                          <el-input-number v-model="quote.availableQty" :min="0" :step="100" :controls="false" placeholder="输入分配量" class="w-full neo-input-number" />
                        </div>
                        <div class="bg-brand-50/50 rounded-xl p-3 border border-brand-100 flex flex-col justify-center">
                          <label class="block text-[10px] font-bold text-brand-600 uppercase tracking-widest mb-1">核算远期价 (参考)</label>
                          <div class="flex items-baseline gap-1">
                            <template v-if="quote.contractCode && getContractByCode(quote.contractCode)?.lastPrice">
                              <span class="text-2xl font-black text-brand-600">
                                ¥{{ calcReferencePrice(quote.contractCode, quote.basisPrice)?.toFixed(0) || '-' }}
                              </span>
                              <span class="text-xs font-bold text-brand-500">/吨</span>
                            </template>
                            <span v-else class="text-sm font-bold text-neutral-400 italic">等待选择合约</span>
                          </div>
                        </div>
                      </div>
                      
                      <button
                        type="button"
                        class="self-end md:self-start p-2.5 rounded-xl bg-error-50 text-error-500 hover:bg-error-500 hover:text-white transition-all shrink-0  shadow-sm"
                        title="移除此合约"
                        @click="removeBasisQuote(index)"
                      >
                        <X class="w-5 h-5" />
                      </button>
                    </div>

                    <!-- 行情实时面板 -->
                    <div v-if="quote.contractCode && getContractByCode(quote.contractCode)" class="mt-4 pt-4 border-t border-dashed border-neutral-200 flex flex-wrap items-center gap-y-2 gap-x-6 text-xs">
                      <div class="flex items-center gap-2">
                        <div 
                          class="w-1.5 h-1.5 rounded-full" 
                          :class="getContractByCode(quote.contractCode)?.isTrading ? 'bg-brand-500 animate-pulse' : 'bg-neutral-400'"
                        ></div>
                        <span class="text-neutral-500 font-medium">
                          {{ getContractByCode(quote.contractCode)?.isTrading ? '期货实时报价' : '盘面参考价' }}:
                        </span>
                        <span class="font-black text-neutral-900 text-sm italic">¥{{ getContractByCode(quote.contractCode)?.lastPrice || '-' }}</span>
                        <span v-if="!getContractByCode(quote.contractCode)?.isTrading" class="text-[10px] text-neutral-400 font-bold bg-neutral-100 px-1.5 py-0.5 rounded ml-1">
                          {{ getContractByCode(quote.contractCode)?.lastPrice === getContractByCode(quote.contractCode)?.prevClose ? '昨收' : '已收盘' }}
                        </span>
                      </div>
                      
                      <div v-if="quote.basisPrice !== undefined" class="flex items-center gap-2">
                        <span class="text-neutral-500 font-medium">当前基差:</span>
                        <span class="font-black px-2 py-0.5 rounded" :class="(quote.basisPrice || 0) >= 0 ? 'bg-red-50 text-red-600' : 'bg-brand-50 text-brand-600'">
                          {{ (quote.basisPrice || 0) >= 0 ? '+' : '' }}{{ quote.basisPrice }}
                        </span>
                      </div>
                      
                      <div v-if="getContractByCode(quote.contractCode)?.priceUpdateTime" class="ml-auto flex items-center gap-1 text-neutral-400">
                        <Clock class="w-3 h-3" />
                        <span>数据更新: {{ new Date(getContractByCode(quote.contractCode)!.priceUpdateTime!).toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' }) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="mt-4 space-y-2 bg-warning-50 p-4 rounded-xl border border-warning-100/50">
                  <div class="flex items-start gap-2 text-xs text-warning-700">
                    <div class="w-1.5 h-1.5 rounded-full bg-warning-500 mt-1 shrink-0"></div>
                    <p class="font-medium">核心公式：核算远期价 = 期货盘面报价 + 您设定的基差（升水为正，贴水为负）。</p>
                  </div>
                  <div class="flex items-start gap-2 text-xs text-warning-600/80 italic">
                    <div class="w-1.5 h-1.5 rounded-full bg-warning-400/50 mt-1 shrink-0"></div>
                    <p>法律提示：基差报价随盘面实时变动，最终结算以成交时点盘面价+基差为准。如遇休盘，最新价将参考前一交易日收盘价。</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 规格参数 -->
        <div v-if="categoryParams.length > 0" class="bg-white rounded-xl border border-neutral-200 overflow-hidden animate-fade-in" style="animation-delay: 100ms">
          <div class="p-5 border-b border-neutral-200 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <div class="w-1.5 h-5 bg-brand-500 rounded-full"></div>
              <h3 class="text-2xl font-bold text-neutral-900">规格参数</h3>
            </div>
            <span class="text-xs text-neutral-400">选填</span>
          </div>
          <div class="p-5">
            <CategoryParamsForm
              :params="categoryParams"
              v-model="dynamicParams"
              v-model:custom-params="customParams"
            />
          </div>
        </div>

        <!-- 物流与交付 -->
        <div class="bg-white rounded-xl border border-neutral-200 overflow-hidden animate-fade-in" style="animation-delay: 150ms">
          <div class="p-5 border-b border-neutral-200 flex items-center gap-2">
            <div class="w-1.5 h-5 bg-warning-500 rounded-full"></div>
            <h3 class="text-2xl font-bold text-neutral-900">物流与交付</h3>
          </div>
          <div class="p-5 space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">发布有效期</label>
                <el-select v-model="publishForm.expireMinutes" clearable class="w-full neo-select">
                  <el-option label="1小时" :value="60" />
                  <el-option label="1天" :value="1440" />
                  <el-option label="3天" :value="4320" />
                  <el-option label="7天" :value="10080" />
                  <el-option label="30天" :value="43200" />
                </el-select>
              </div>
              <div>
                <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">包装方式</label>
                <el-select v-model="publishForm.packaging" class="w-full neo-select">
                  <el-option
                    v-for="opt in currentSchemaConfig.packagingOptions"
                    :key="opt"
                    :label="opt"
                    :value="opt"
                  />
                </el-select>
              </div>
              <div>
                <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">交付方式</label>
                <el-select v-model="publishForm.deliveryMode" class="w-full neo-select">
                  <el-option
                    v-for="opt in currentSchemaConfig.deliveryOptions"
                    :key="opt"
                    :label="opt"
                    :value="opt"
                  />
                </el-select>
              </div>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">付款方式</label>
                <el-select v-model="publishForm.paymentMethod" class="w-full neo-select">
                  <el-option label="现款" value="现款" />
                  <el-option label="账期" value="账期" />
                </el-select>
              </div>
              <div>
                <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">发票类型</label>
                <el-select v-model="publishForm.invoiceType" clearable class="w-full neo-select">
                  <el-option label="普通发票" value="普通发票" />
                  <el-option label="增值税发票" value="增值税发票" />
                  <el-option label="不需要发票" value="不需要发票" />
                </el-select>
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">补充说明</label>
              <textarea
                v-model="publishForm.remark"
                rows="3"
                placeholder="备注（可选）"
                class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-brand-500 outline-none transition-all resize-none"
              ></textarea>
            </div>
            <div class="flex justify-end gap-3 pt-2">
              <BaseButton type="secondary" @click="saveAsTemplate">
                <Save class="w-4 h-4" />
                保存为模板
              </BaseButton>
              <BaseButton type="primary" :loading="loading" @click="publishSupply">
                <Send class="w-4 h-4" />
                发布供应
              </BaseButton>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧预览区域 -->
      <div class="lg:col-span-1">
        <div class="bg-white rounded-xl border border-neutral-200 overflow-hidden sticky top-24 animate-fade-in" style="animation-delay: 200ms">
          <div class="p-5 border-b border-neutral-200">
            <div class="text-[10px] font-bold uppercase tracking-widest text-neutral-400">summary</div>
            <h3 class="text-2xl font-bold text-neutral-900 mt-1">发布前确认</h3>
          </div>
          
          <div class="p-5 max-h-[70vh] overflow-y-auto">
            <div v-if="!publishForm.categoryName" class="py-8">
              <EmptyState
                type="data"
                title="暂无内容"
                description="填写左侧信息后显示"
                size="sm"
              />
            </div>
            <div v-else class="space-y-3">
              <div class="grid grid-cols-2 gap-3">
                <div class="bg-neutral-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-neutral-400">公司</div>
                  <div class="mt-1 font-bold text-neutral-900 truncate text-sm">{{ publishForm.companyName || '未指定' }}</div>
                </div>
                <div class="bg-neutral-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-neutral-400">发布人</div>
                  <div class="mt-1 font-bold text-neutral-900 truncate text-sm">{{ publisherNameInput || publisherName }}</div>
                </div>
              </div>
              <div class="bg-brand-50 rounded-xl p-3 border border-brand-100">
                <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-brand-600">
                  <Package class="w-3 h-3" />
                  品类
                </div>
                <div class="mt-1 font-bold text-brand-700">{{ previewData.categoryName }}</div>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div class="bg-neutral-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-neutral-400">数量</div>
                  <div class="mt-1 font-bold text-neutral-900">{{ previewData.quantity }} {{ currentUnitConfig.quantityUnit }}</div>
                </div>
                <div v-if="publishForm.priceType === 0" class="bg-neutral-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-neutral-400">单价</div>
                  <div class="mt-1 font-bold text-brand-600">
                    <span v-if="previewData.exFactoryPrice != null">¥{{ previewData.exFactoryPrice }}/{{ currentUnitConfig.quantityUnit }}</span>
                    <span v-else class="text-neutral-500">面议</span>
                  </div>
                </div>
                <div v-else class="bg-warning-50 rounded-xl p-3 border border-warning-100">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-warning-600">报价方式</div>
                  <div class="mt-1 font-bold text-warning-700">基差报价</div>
                </div>
              </div>
              <!-- 基差报价预览 -->
              <div v-if="publishForm.priceType === 1 && basisQuotes.length > 0" class="bg-warning-50/50 rounded-xl p-3 border border-warning-100">
                <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-warning-600 mb-2">
                  <TrendingUp class="w-3 h-3" />
                  基差明细
                </div>
                <div class="space-y-2">
                  <div v-for="(quote, index) in basisQuotes.filter(q => q.contractCode)" :key="index" class="bg-white rounded-lg p-2 border border-warning-100">
                    <div class="flex items-center justify-between text-sm">
                      <span class="font-medium text-neutral-700">{{ getContractByCode(quote.contractCode)?.contractName || quote.contractCode }}</span>
                      <span class="font-bold" :class="(quote.basisPrice || 0) >= 0 ? 'text-red-600' : 'text-green-600'">
                        {{ (quote.basisPrice || 0) >= 0 ? '+' : '' }}{{ quote.basisPrice }}
                      </span>
                    </div>
                    <div class="flex items-center justify-between text-xs text-neutral-500 mt-1">
                      <span>
                        期货 ¥{{ getContractByCode(quote.contractCode)?.lastPrice || '-' }} → 
                        <span class="font-bold text-brand-600">核算 ¥{{ calcReferencePrice(quote.contractCode, quote.basisPrice) || '-' }}</span>
                      </span>
                      <span>{{ quote.availableQty }}吨</span>
                    </div>
                  </div>
                </div>
              </div>
              <div class="bg-neutral-50 rounded-xl p-3">
                <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-neutral-400">
                  <Truck class="w-3 h-3" />
                  发货地址
                </div>
                <div class="mt-1 font-bold text-neutral-900 text-sm">{{ previewData.shipAddress }}</div>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div class="bg-neutral-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-neutral-400">交付</div>
                  <div class="mt-1 font-bold text-neutral-900">{{ previewData.deliveryMode }}</div>
                </div>
                <div class="bg-neutral-50 rounded-xl p-3">
                  <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-neutral-400">
                    <Clock class="w-3 h-3" />
                    有效期
                  </div>
                  <div class="mt-1 font-bold text-neutral-900">{{ previewData.expireText }}</div>
                </div>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div class="bg-neutral-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-neutral-400">付款方式</div>
                  <div class="mt-1 font-bold text-neutral-900">{{ publishForm.paymentMethod || '现款' }}</div>
                </div>
                <div v-if="publishForm.invoiceType" class="bg-neutral-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-neutral-400">发票类型</div>
                  <div class="mt-1 font-bold text-neutral-900">{{ publishForm.invoiceType }}</div>
                </div>
              </div>
              <!-- 规格参数预览 -->
              <div v-if="previewData.paramsText !== '无'" class="bg-brand-50/50 rounded-xl p-3 border border-brand-100">
                <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-brand-600">
                  <FileCheck class="w-3 h-3" />
                  规格参数
                </div>
                <div class="mt-2 space-y-1">
                  <template v-for="param in categoryParams" :key="param.id">
                    <div v-if="dynamicParams[param.id]" class="flex items-center gap-2 text-xs">
                      <span class="text-neutral-500">{{ param.paramName }}:</span>
                      <span class="font-bold text-neutral-800">{{ dynamicParams[param.id] }}{{ param.unit || '' }}</span>
                    </div>
                  </template>
                </div>
              </div>
              <!-- 自定义参数预览 -->
              <template v-if="customParams.length > 0">
                <template v-for="(cp, idx) in customParams" :key="'cp-' + idx">
                  <div v-if="cp.name.trim() && cp.value.trim()" class="flex items-center gap-2 text-xs">
                    <span class="text-neutral-500">{{ cp.name }}:</span>
                    <span class="font-bold text-neutral-800">{{ cp.value }}</span>
                  </div>
                </template>
              </template>
              <div v-if="previewData.remark" class="bg-neutral-50 rounded-xl p-3">
                <div class="text-[10px] font-bold uppercase tracking-widest text-neutral-400">备注</div>
                <div class="mt-1 text-sm text-neutral-700">{{ previewData.remark }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    </template>

    <!-- 模板选择面板 (Command Palette 风格) -->
    <TemplateCommandPalette
      v-model="templatePickerOpen"
      :templates="parsedTemplates"
      title="供应模板"
      empty-text="暂无模板，可在发布表单中保存"
      @select="handleTemplateSelect"
      @delete="deleteTemplate"
    />

    <!-- 保存模板弹窗 -->
    <BaseModal v-model="saveTemplateDialogVisible" title="保存为模板" size="sm">
      <div class="space-y-4">
        <p class="text-sm text-neutral-500">用于一键复用本次发布的品类、条款与指标配置</p>
        <div>
          <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">
            模板名称 <span class="text-red-500">*</span>
          </label>
          <input
            v-model="templateNameInput"
            type="text"
            placeholder="例如：玉米到厂-现款-常规指标"
            class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-brand-500 outline-none transition-all"
            @keyup.enter="confirmSaveTemplate"
          />
        </div>
      </div>
      <template #footer>
        <BaseButton type="secondary" @click="saveTemplateDialogVisible = false">取消</BaseButton>
        <BaseButton type="primary" :loading="loading" @click="confirmSaveTemplate">
          <Save class="w-4 h-4" />
          保存
        </BaseButton>
      </template>
    </BaseModal>

    <!-- 编辑已发布供应弹窗 -->
    <BaseModal
      v-model="editOpen"
      title="编辑供应信息"
      size="lg"
    >
      <div class="space-y-6">
        <p class="text-sm text-neutral-500">
          仅修改本条已发布供应，不影响您的公司/个人档案
        </p>

        <!-- 标的信息 -->
        <div class="bg-neutral-50 rounded-xl p-4 border border-neutral-200">
          <div class="text-[10px] font-bold uppercase tracking-widest text-neutral-400">交易标的</div>
          <div class="mt-1 font-bold text-neutral-900">
            {{ editing?.categoryName || '-' }}
            <span v-if="editing?.supplyNo" class="text-sm text-neutral-500 font-medium ml-2">{{ editing?.supplyNo }}</span>
          </div>
        </div>

        <!-- 表单 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">供应数量（{{ currentUnitConfig.quantityUnit }}）</label>
            <input
              v-model.number="editForm.quantity"
              type="number"
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">出厂价（元/{{ currentUnitConfig.quantityUnit }}）</label>
            <input
              v-model.number="editForm.exFactoryPrice"
              type="number"
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">发货地址</label>
            <input
              v-model="editForm.shipAddress"
              type="text"
              placeholder="例如：山东省济南市..."
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">交付方式</label>
            <input
              v-model="editForm.deliveryMode"
              type="text"
              placeholder="例如：到厂 / 自提"
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">包装方式</label>
            <input
              v-model="editForm.packaging"
              type="text"
              placeholder="例如：散装 / 袋装"
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">付款方式</label>
            <select
              v-model="editForm.paymentMethod"
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-brand-500 outline-none transition-all bg-white"
            >
              <option value="">请选择</option>
              <option value="现款">现款</option>
              <option value="账期">账期</option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">发票类型</label>
            <select
              v-model="editForm.invoiceType"
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-brand-500 outline-none transition-all bg-white"
            >
              <option value="">请选择</option>
              <option value="普通发票">普通发票</option>
              <option value="增值税发票">增值税发票</option>
              <option value="不需要发票">不需要发票</option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">发布有效期</label>
            <select
              v-model="editForm.expireMinutes"
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-brand-500 outline-none transition-all bg-white"
            >
              <option :value="60">1小时</option>
              <option :value="1440">1天</option>
              <option :value="4320">3天</option>
              <option :value="10080">7天</option>
              <option :value="43200">30天</option>
            </select>
          </div>
          <div class="md:col-span-2">
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">备注</label>
            <textarea
              v-model="editForm.remark"
              rows="2"
              placeholder="补充说明（选填）"
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-brand-500 outline-none transition-all resize-none"
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

<style scoped>
/* 下拉菜单过渡 */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.15s ease-out;
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* 折叠过渡 */
.collapse-enter-active,
.collapse-leave-active {
  transition: all 0.2s ease-out;
  overflow: hidden;
}
.collapse-enter-from,
.collapse-leave-to {
  opacity: 0;
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
}
.collapse-enter-to,
.collapse-leave-from {
  max-height: 1000px;
}

/* 入场动画 */
@keyframes stagger-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.animate-stagger-in {
  animation: stagger-in 0.2s ease-out both;
}

/* Element Plus 输入控件统一样式 */
:deep(.neo-input-number .el-input__wrapper),
:deep(.neo-select .el-select__wrapper) {
  border: 2px solid rgb(243 244 246);
  border-radius: 12px;
  box-shadow: none;
  background-color: #fff;
  transition: all 0.15s ease;
}
:deep(.neo-input-number .el-input__wrapper.is-focus),
:deep(.neo-select .el-select__wrapper.is-focus) {
  border-color: rgb(16 185 129);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18);
}
:deep(.neo-input-number .el-input__wrapper:hover),
:deep(.neo-select .el-select__wrapper:hover) {
  border-color: rgb(229 231 235);
}

/* 小尺寸输入控件 */
:deep(.neo-input-number-sm .el-input__wrapper),
:deep(.neo-select-sm .el-select__wrapper) {
  border: 1px solid rgb(229 231 235);
  border-radius: 8px;
  box-shadow: none;
  background-color: #fff;
  transition: all 0.15s ease;
  padding: 4px 8px;
}
:deep(.neo-input-number-sm .el-input__wrapper.is-focus),
:deep(.neo-select-sm .el-select__wrapper.is-focus) {
  border-color: rgb(245 158 11);
  box-shadow: 0 0 0 2px rgba(245, 158, 11, 0.15);
}

/* 基差颜色反馈 */
:deep(.basis-plus .el-input__wrapper) {
  border-color: rgba(239, 68, 68, 0.15);
  background-color: rgba(239, 68, 68, 0.02);
}
:deep(.basis-plus .el-input__inner) {
  color: rgb(220, 38, 38);
  font-weight: var(--font-weight-extrabold);
}
:deep(.basis-minus .el-input__wrapper) {
  border-color: rgba(16, 185, 129, 0.15);
  background-color: rgba(16, 185, 129, 0.02);
}
:deep(.basis-minus .el-input__inner) {
  color: rgb(5, 150, 105);
  font-weight: var(--font-weight-extrabold);
}

/* 单选按钮组样式 */
:deep(.el-radio-group) {
  display: flex;
  gap: 8px;
}
:deep(.el-radio-button__inner) {
  border: 2px solid rgb(243 244 246) !important;
  border-radius: 10px !important;
  box-shadow: none !important;
  font-weight: var(--font-weight-semibold);
  padding: 8px 16px;
}
:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: rgb(16 185 129) !important;
  border-color: rgb(16 185 129) !important;
  color: white !important;
}
:deep(.el-radio-button:first-child .el-radio-button__inner),
:deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 10px !important;
}
</style>
