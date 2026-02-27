<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createRequirement, getNextRequirementNo, listRequirements, updateRequirement, type RequirementCreateRequest, type RequirementResponse, type RequirementUpdateRequest } from '../api/requirement'
import { listMyRequirementTemplates as listRequirementTemplates, createRequirementTemplate, deleteRequirementTemplate, type RequirementTemplateCreateRequest, type RequirementTemplateResponse } from '../api/requirementTemplate'
import { getProductParams, type ProductParamResponse } from '../api/product'
import { getMyCompany, type CompanyResponse } from '../api/company'
import { getMe, type UserResponse } from '../api/user'
import SchemaAwareCategoryPicker, { type PickedCategory } from '../components/SchemaAwareCategoryPicker.vue'
import CategoryParamsForm from '../components/CategoryParamsForm.vue'
import { getSchemaUnitConfig, getCategoryUnitConfig } from '../utils/schemaUnits'
import { BaseButton, BaseModal, EmptyState, Skeleton } from '../components/ui'
import TemplateCommandPalette, { type TemplateItem } from '../components/TemplateCommandPalette.vue'
import ProductInfoRow from '../components/ProductInfoRow.vue'
import { FileText, Save, Send, Package, MapPin, Clock, FileCheck, CreditCard, ChevronDown, Plus, RefreshCcw, Search, X } from 'lucide-vue-next'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { useCompanyStore } from '../stores/company'

const route = useRoute()
const auth = useAuthStore()
const companyStore = useCompanyStore()
const loading = ref(false)
const contractNo = ref<string>('')
const templatePickerOpen = ref(false)
const meUser = ref<UserResponse | null>(null)
const purchaserNameInput = ref('')

// ============ Tab 切换 ============
type TabType = 'publish' | 'published'
// 根据 URL 参数初始化 Tab
const initialTab = route.query.tab === 'published' ? 'published' : 'publish'
const activeTab = ref<TabType>(initialTab)

// ============ 已发布列表相关 ============
const requirements = ref<RequirementResponse[]>([])
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

const pagedRequirements = computed(() => {
  const start = (listPagination.page - 1) * listPagination.size
  const end = start + listPagination.size
  return requirements.value.slice(start, end)
})

// 发布中状态的数量（用于Tab显示）
const activeRequirementsCount = computed(() => {
  return requirements.value.filter(r => r.status === 0).length
})

async function loadRequirements() {
  listLoading.value = true
  try {
    const companyId = auth.me?.companyId
    if (!companyId) {
      requirements.value = []
      listPagination.total = 0
      return
    }
    const r = await listRequirements({
      companyId,
      categoryName: listFilters.categoryName || undefined,
      status: listFilters.status ?? undefined,
      includeExpired: true
    })
    if (r.code === 0) {
      requirements.value = r.data || []
      listPagination.total = requirements.value.length
    } else {
      throw new Error(r.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载需求列表失败')
  } finally {
    listLoading.value = false
  }
}

function handleListFilter() {
  listPagination.page = 1
  loadRequirements()
}

function handlePageChange(page: number) {
  listPagination.page = page
}

function getStatusText(status?: number) {
  return statusOptions.find(o => o.value === status)?.label || '未知'
}

function getStatusIcon(status?: number) {
  return statusOptions.find(o => o.value === status)?.icon || '○'
}

// 计算成交进度百分比
function getDealProgress(req: RequirementResponse): number {
  if (!req.quantity || req.quantity <= 0) return 0
  const remaining = req.remainingQuantity ?? req.quantity
  const dealt = req.quantity - remaining
  return Math.round((dealt / req.quantity) * 100)
}

// 编辑已发布的需求
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

async function revokeRequirement(req: RequirementResponse) {
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

async function republishRequirement(req: RequirementResponse) {
  if (!req.id) return
  try {
    await ElMessageBox.confirm('将该需求重新发布到大厅，并按有效期重新计时。', '再次发布？', {
      confirmButtonText: '发布',
      cancelButtonText: '取消',
      type: 'info'
    })
    const r = await updateRequirement(req.id, { status: 0, expireMinutes: req.expireMinutes ?? 4320 })
    if (r.code !== 0) throw new Error(r.message)
    ElMessage.success('已再次发布')
    await loadRequirements()
  } catch (e: any) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e?.message || '操作失败')
  }
}

// 模板下拉菜单
const templateMenuOpen = ref(false)

// 发布表单
const publishForm = reactive({
  categoryId: undefined as number | undefined,
  categoryName: '',
  companyName: '',
  quantity: undefined as number | undefined,
  packaging: '散装',
  paymentMethod: '现款',
  paramsJson: '{}',
  expireMinutes: 4320, // 3天
  purchaseLat: undefined as number | undefined,
  purchaseLng: undefined as number | undefined,
  purchaseAddress: '',
  expectedPrice: undefined as number | undefined,
  invoiceType: '',
  deliveryMethod: '',
  remark: ''
})

// 公司信息
const company = ref<CompanyResponse | null>(null)

const templates = ref<RequirementTemplateResponse[]>([])

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

type TemplateJsonData = {
  schemaCode?: string
  templateName?: string
  companyName?: string
  purchaserName?: string
  categoryId?: number
  categoryName?: string
  quantity?: number
  expectedPrice?: number
  purchaseAddress?: string
  expireMinutes?: number
  packaging?: string
  paymentMethod?: string
  invoiceType?: string
  deliveryMethod?: string
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
      price: parsed.expectedPrice,
      priceUnit: schemaConfig.priceUnit,
      _raw: t
    } as TemplateItem & { _raw: typeof t }
  })
)

// 品类相关
const categoryParams = ref<ProductParamResponse[]>([])

// 动态参数表单
const dynamicParams = ref<Record<string, any>>({})
const customParams = ref<Array<{ name: string; value: string }>>([])

// 实时预览数据
const previewData = computed(() => {
  const expireDays = publishForm.expireMinutes ? Math.floor(publishForm.expireMinutes / 1440) : 0
  const expireHours = publishForm.expireMinutes ? Math.floor((publishForm.expireMinutes % 1440) / 60) : 0
  const expireText = expireDays > 0 ? `${expireDays}天` : expireHours > 0 ? `${expireHours}小时` : '未设置'
  
  // 解析参数
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
  } catch (e) {
    paramsText = '无'
  }
  
  return {
    companyName: publishForm.companyName || '未指定',
    categoryName: publishForm.categoryName || '未选择',
    quantity: publishForm.quantity || 0,
    expectedPrice: publishForm.expectedPrice,
    purchaseAddress: publishForm.purchaseAddress || '未指定',
    expireText,
    paramsText,
    remark: publishForm.remark || ''
  }
})

const purchaserName = computed(() => {
  const real = meUser.value?.realName?.trim()
  if (real) return real
  return auth.me?.nickName || auth.me?.userName || '—'
})

onMounted(async () => {
  await Promise.all([
    loadTemplates(),
    loadCompanyInfo(),
    loadMeUser()
  ])
  // 获取下一个合同号
  await loadNextContractNo()
  // 如果初始 Tab 是已发布，自动加载列表
  if (activeTab.value === 'published') {
    loadRequirements()
  }
})

// Tab 切换时加载对应数据
watch(activeTab, (tab) => {
  if (tab === 'published' && requirements.value.length === 0) {
    loadRequirements()
  }
})

async function loadCompanyInfo() {
  try {
    const r = await getMyCompany()
    if (r.code === 0 && r.data) {
      company.value = r.data
      // 设置默认值
      publishForm.companyName = r.data.companyName || ''
      // 拼接完整地址：省 + 市 + 区 + 详细地址
      const fullAddress = [
        r.data.province,
        r.data.city,
        r.data.district,
        r.data.address
      ].filter(Boolean).join('')
      publishForm.purchaseAddress = fullAddress || ''
    }
  } catch (e) {
    // 静默失败
  }
}

async function loadMeUser() {
  try {
    const r = await getMe()
    if (r.code === 0) meUser.value = r.data ?? null
    if (!purchaserNameInput.value) {
      purchaserNameInput.value = purchaserName.value
    }
  } catch {
    // 静默失败
  }
}

async function loadNextContractNo() {
  try {
    const r = await getNextRequirementNo()
    if (r.code === 0 && r.data) {
      contractNo.value = r.data
    }
  } catch (e) {
    // 静默失败
  }
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
    publishForm.deliveryMethod = config.deliveryOptions[0] ?? ''
  }
}

async function loadTemplates() {
  try {
    const r = await listRequirementTemplates()
    if (r.code === 0) templates.value = r.data || []
  } catch (e) {
    ElMessage.error('加载模板列表失败')
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
      await loadNextContractNo()
    } else {
      publishForm.categoryId = undefined
      publishForm.categoryName = ''
      categoryParams.value = []
      dynamicParams.value = {}
    }
  }
)


async function loadCategoryParams(productId: number) {
  try {
    const r = await getProductParams(productId)
    if (r.code === 0) {
      categoryParams.value = r.data || []
      // 初始化动态参数
      const params: Record<string, any> = {}
      categoryParams.value.forEach(param => {
        params[param.id] = param.paramType === 1 ? '' : ''
      })
      dynamicParams.value = params
    }
  } catch (e) {
    ElMessage.error('加载品类参数失败')
  }
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

async function publishRequirement() {
  if (!publishForm.categoryId) {
    ElMessage.warning('请选择品类')
    return
  }
  if (!publishForm.quantity) {
    ElMessage.warning('请输入数量')
    return
  }
  if (!publishForm.purchaseAddress) {
    ElMessage.warning('请输入交付地')
    return
  }
  
  loading.value = true
  try {
    const paramsJson = buildParamsJson()

    const req: RequirementCreateRequest = {
      categoryName: publishForm.categoryName,
      quantity: publishForm.quantity,
      packaging: publishForm.packaging,
      paymentMethod: publishForm.paymentMethod,
      contractNo: contractNo.value || undefined,
      expectedPrice: publishForm.expectedPrice,
      invoiceType: publishForm.invoiceType || undefined,
      deliveryMethod: publishForm.deliveryMethod || undefined,
      paramsJson,
      expireMinutes: publishForm.expireMinutes,
      purchaseAddress: publishForm.purchaseAddress,
      remark: publishForm.remark || undefined
    }
    
    const r = await createRequirement(req)
    if (r.code === 0) {
      ElMessage.success('需求发布成功')
      // 清除企业资料缓存，确保企业主页显示最新数据
      if (company.value?.id) {
        companyStore.invalidateProfile(company.value.id)
      }
      // 切换到已发布Tab并刷新列表
      activeTab.value = 'published'
      await loadRequirements()
      // 重置表单（保留公司名称和采购地址的默认值）
      Object.assign(publishForm, {
        companyName: company.value?.companyName || '',
        categoryId: undefined,
        categoryName: '',
        quantity: undefined,
        packaging: '散装',
        paymentMethod: '现款',
        paramsJson: '{}',
        expireMinutes: 4320,
        purchaseLat: undefined,
        purchaseLng: undefined,
        purchaseAddress: company.value?.address || '',
        expectedPrice: undefined,
        invoiceType: '',
        deliveryMethod: '',
        remark: ''
      })
      purchaserNameInput.value = purchaserName.value
      pickedCategory.value = null
      categoryParams.value = []
      dynamicParams.value = {}
      customParams.value = []
      await loadNextContractNo()
    } else {
      ElMessage.error(r.message || '发布失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '发布失败')
  } finally {
    loading.value = false
  }
}

// 保存为模板对话框
const saveTemplateDialogVisible = ref(false)
const templateNameInput = ref('')

async function saveAsTemplate() {
  if (!publishForm.categoryId) {
    ElMessage.warning('请先选择品类')
    return
  }
  saveTemplateDialogVisible.value = true
}

async function confirmSaveTemplate() {
  if (!templateNameInput.value.trim()) {
    ElMessage.warning('请输入模板名称')
    return
  }
  
  loading.value = true
  try {
    const paramsJson = buildParamsJson()
    
    const req: RequirementTemplateCreateRequest = {
      templateName: templateNameInput.value.trim(),
      templateJson: JSON.stringify({
        schemaCode: selectedSchemaCode.value,
        companyName: publishForm.companyName,
        purchaserName: purchaserNameInput.value,
        categoryId: publishForm.categoryId,
        categoryName: publishForm.categoryName,
        quantity: publishForm.quantity,
        packaging: publishForm.packaging,
        paymentMethod: publishForm.paymentMethod,
        expectedPrice: publishForm.expectedPrice,
        invoiceType: publishForm.invoiceType,
        deliveryMethod: publishForm.deliveryMethod,
        paramsJson,
        expireMinutes: publishForm.expireMinutes,
        purchaseLat: publishForm.purchaseLat,
        purchaseLng: publishForm.purchaseLng,
        purchaseAddress: publishForm.purchaseAddress,
        remark: publishForm.remark
      })
    }
    
    const r = await createRequirementTemplate(req)
    if (r.code === 0) {
      ElMessage.success('模板保存成功')
      await loadTemplates()
      saveTemplateDialogVisible.value = false
      templateNameInput.value = ''
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
    const r = await deleteRequirementTemplate(id)
    if (r.code === 0) {
      ElMessage.success('模板删除成功')
      await loadTemplates()
    }
  } catch (e: any) {
    if (e === 'cancel' || e === 'close') return
    ElMessage.error(e?.message || '删除失败')
  }
}

// 处理模板选择（从 CommandPalette）
function handleTemplateSelect(item: TemplateItem & { _raw?: RequirementTemplateResponse }) {
  if (item._raw) {
    applyTemplate(item._raw)
  }
}

async function applyTemplate(template: RequirementTemplateResponse) {
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
      quantity: data.quantity,
      packaging: data.packaging || '散装',
      paymentMethod: data.paymentMethod || '现款',
      expireMinutes: data.expireMinutes || 4320,
      purchaseAddress: data.purchaseAddress || '',
      expectedPrice: data.expectedPrice,
      invoiceType: data.invoiceType || '',
      deliveryMethod: data.deliveryMethod || '',
      remark: data.remark || ''
    })

    purchaserNameInput.value = data.purchaserName || purchaserNameInput.value
    pickedCategory.value = data.categoryId ? {
      id: data.categoryId,
      name: data.categoryName || String(data.categoryId),
      schemaCode: data.schemaCode
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
          Object.entries(oldParams).forEach(([paramId, value]) => {
            dynamicParams.value[Number(paramId)] = value
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
        <h1 class="text-2xl font-bold text-neutral-900">采购管理</h1>
        <!-- Tab 切换 -->
        <div class="flex items-center bg-neutral-100 rounded-xl p-1">
          <button
            :class="[
              'px-4 py-2 rounded-lg text-sm font-bold transition-all',
              activeTab === 'publish'
                ? 'bg-white text-autumn-600 shadow-sm'
                : 'text-neutral-500 hover:text-neutral-700'
            ]"
            @click="activeTab = 'publish'"
          >
            <span class="flex items-center gap-2">
              <span class="w-2 h-2 rounded-full" :class="activeTab === 'publish' ? 'bg-autumn-500' : 'bg-neutral-300'"></span>
              发布采购
            </span>
          </button>
          <button
            :class="[
              'px-4 py-2 rounded-lg text-sm font-bold transition-all',
              activeTab === 'published'
                ? 'bg-white text-autumn-600 shadow-sm'
                : 'text-neutral-500 hover:text-neutral-700'
            ]"
            @click="activeTab = 'published'"
          >
            <span class="flex items-center gap-2">
              <span class="w-2 h-2 rounded-full" :class="activeTab === 'published' ? 'bg-autumn-500' : 'bg-neutral-300'"></span>
              已发布
              <span v-if="activeRequirementsCount > 0" class="px-1.5 py-0.5 bg-autumn-100 text-autumn-600 text-[10px] rounded-full">
                {{ activeRequirementsCount }}
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
        <BaseButton type="primary" size="sm" :loading="loading" @click="publishRequirement">
          <Send class="w-4 h-4" />
          发布
        </BaseButton>
      </div>
      <!-- 已发布Tab的操作按钮 -->
      <div v-else class="flex items-center gap-3">
        <BaseButton type="secondary" size="sm" :loading="listLoading" @click="loadRequirements">
          <RefreshCcw class="w-4 h-4" />
          刷新
        </BaseButton>
        <BaseButton type="primary" size="sm" @click="activeTab = 'publish'">
          <Plus class="w-4 h-4" />
          发布新需求
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
              class="w-full pl-10 pr-4 py-2.5 border-2 border-neutral-200 rounded-xl text-sm focus:border-autumn-500 outline-none transition-all"
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
                  ? 'bg-autumn-600 text-white'
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

      <!-- 需求列表（紧凑表格风格） -->
      <div class="bg-white rounded-xl border border-neutral-200 overflow-hidden">
        <!-- 加载状态 -->
        <div v-if="listLoading && requirements.length === 0" class="p-6 space-y-4">
          <Skeleton type="card" />
          <Skeleton type="card" />
          <Skeleton type="card" />
        </div>

        <!-- 空状态 -->
        <EmptyState
          v-else-if="requirements.length === 0"
          type="empty"
          title="暂无采购需求"
          description="点击上方「发布采购」标签开始发布"
          size="md"
        />

        <!-- 产品信息列表 -->
        <div v-else class="p-4 space-y-3">
          <div
            v-for="(req, index) in pagedRequirements"
            :key="req.id"
            class="bg-white rounded-2xl border border-neutral-200 p-4 hover:shadow-md hover:border-autumn-200 transition-all duration-200 animate-stagger-in"
            :style="{ animationDelay: `${index * 40}ms` }"
          >
            <ProductInfoRow
              :data="{
                categoryName: req.categoryName || '未知品类',
                quantity: req.quantity,
                quantityUnit: currentUnitConfig.quantityUnit,
                price: req.expectedPrice,
                priceUnit: currentUnitConfig.quantityUnit,
                address: req.purchaseAddress,
                packaging: req.packaging,
                paymentMethod: req.paymentMethod,
                paramsJson: req.paramsJson,
                expireTime: req.expireTime
              }"
              type="purchase"
            >
              <template #status>
                <span
                  :class="[
                    'inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold whitespace-nowrap',
                    req.status === 0 ? 'bg-autumn-50 text-autumn-600' :
                    req.status === 1 ? 'bg-warning-50 text-warning-600' :
                    req.status === 3 ? 'bg-brand-50 text-brand-600' :
                    'bg-neutral-100 text-neutral-500'
                  ]"
                >
                  {{ getStatusIcon(req.status) }} {{ getStatusText(req.status) }}
                  <template v-if="req.status === 1 && req.remainingQuantity != null"> · {{ getDealProgress(req) }}%</template>
                </span>
              </template>
              <template #actions>
                <button
                  v-if="req.status !== 3"
                  class="px-2 py-0.5 rounded text-[11px] font-medium text-neutral-600 hover:bg-neutral-100 transition-colors"
                  @click="openEdit(req)"
                >编辑</button>
                <button
                  v-if="req.status === 0 || req.status === 1"
                  class="px-2 py-0.5 rounded text-[11px] font-medium text-error-600 hover:bg-error-50 transition-colors"
                  @click="revokeRequirement(req)"
                >撤销</button>
                <button
                  v-else-if="req.status === 2"
                  class="px-2 py-0.5 rounded text-[11px] font-medium text-autumn-600 hover:bg-autumn-50 transition-colors"
                  @click="republishRequirement(req)"
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
                  class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-autumn-500 outline-none transition-all"
                />
              </div>
              <div>
                <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">采购人</label>
                <input
                  v-model="purchaserNameInput"
                  type="text"
                  placeholder="默认使用个人信息"
                  class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-autumn-500 outline-none transition-all"
                />
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">交付地址</label>
              <input
                v-model="publishForm.purchaseAddress"
                type="text"
                placeholder="请输入交付/收货地址"
                class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-autumn-500 outline-none transition-all"
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
            <div>
              <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">
                业态与品类 <span class="text-red-500">*</span>
              </label>
              <SchemaAwareCategoryPicker
                v-model="pickedCategory"
                @schema-change="onSchemaChange"
              />
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">
                  {{ currentUnitConfig.purchaseQuantityLabel }} <span class="text-red-500">*</span>
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
              <div>
                <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">
                  {{ currentUnitConfig.priceLabel.replace('出厂价', '期望价') }}
                </label>
                <el-input-number
                  v-model="publishForm.expectedPrice"
                  :min="0"
                  :step="currentSchemaConfig.priceStep"
                  :controls="false"
                  :placeholder="currentSchemaConfig.pricePlaceholder"
                  class="w-full neo-input-number"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 质量要求 -->
        <div v-if="categoryParams.length > 0" class="bg-white rounded-xl border border-neutral-200 overflow-hidden animate-fade-in" style="animation-delay: 100ms">
          <div class="p-5 border-b border-neutral-200 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <div class="w-1.5 h-5 bg-brand-500 rounded-full"></div>
              <h3 class="text-2xl font-bold text-neutral-900">质量要求</h3>
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
                <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">交货方式</label>
                <el-select v-model="publishForm.deliveryMethod" clearable class="w-full neo-select">
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
                class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-autumn-500 outline-none transition-all resize-none"
              ></textarea>
            </div>
            <div class="flex justify-end gap-3 pt-2">
              <BaseButton type="secondary" @click="saveAsTemplate">
                <Save class="w-4 h-4" />
                保存为模板
              </BaseButton>
              <BaseButton type="primary" :loading="loading" @click="publishRequirement">
                <Send class="w-4 h-4" />
                发布采购需求
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
                type="folder"
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
                  <div class="text-[10px] font-bold uppercase tracking-widest text-neutral-400">采购人</div>
                  <div class="mt-1 font-bold text-neutral-900 truncate text-sm">{{ purchaserNameInput || purchaserName }}</div>
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
                <div class="bg-neutral-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-neutral-400">期望价</div>
                  <div class="mt-1 font-bold text-brand-600">
                    <span v-if="previewData.expectedPrice != null">¥{{ previewData.expectedPrice }}/{{ currentUnitConfig.priceUnit }}</span>
                    <span v-else class="text-neutral-500">面议</span>
                  </div>
                </div>
              </div>
              <div class="bg-neutral-50 rounded-xl p-3">
                <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-neutral-400">
                  <MapPin class="w-3 h-3" />
                  交付地址
                </div>
                <div class="mt-1 font-bold text-neutral-900 text-sm">{{ previewData.purchaseAddress }}</div>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div class="bg-neutral-50 rounded-xl p-3">
                  <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-neutral-400">
                    <CreditCard class="w-3 h-3" />
                    付款
                  </div>
                  <div class="mt-1 font-bold text-neutral-900">{{ publishForm.paymentMethod || '现款' }}</div>
                </div>
                <div class="bg-neutral-50 rounded-xl p-3">
                  <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-neutral-400">
                    <Clock class="w-3 h-3" />
                    有效期
                  </div>
                  <div class="mt-1 font-bold text-neutral-900">{{ previewData.expireText }}</div>
                </div>
              </div>
              <!-- 自定义参数预览 -->
              <template v-if="customParams.length > 0">
                <div class="bg-neutral-50 rounded-xl p-3">
                  <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-neutral-400">
                    <FileCheck class="w-3 h-3" />
                    自定义参数
                  </div>
                  <div class="mt-1 flex flex-wrap gap-1">
                    <template v-for="(cp, idx) in customParams" :key="'cp-' + idx">
                      <span v-if="cp.name.trim() && cp.value.trim()" class="text-[10px] bg-white px-1.5 py-0.5 rounded border border-neutral-200 text-neutral-600">
                        {{ cp.name }}: {{ cp.value }}
                      </span>
                    </template>
                  </div>
                </div>
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
      title="采购模板"
      empty-text="暂无模板，可在发布表单中保存"
      @select="handleTemplateSelect"
      @delete="deleteTemplate"
    />

    <!-- 保存为模板弹窗 -->
    <BaseModal v-model="saveTemplateDialogVisible" title="保存为模板" size="sm">
      <div class="space-y-4">
        <p class="text-xs text-neutral-500">用于一键复用本次发布的品类、条款与指标配置</p>
        <div>
          <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">
            模板名称 <span class="text-red-500">*</span>
          </label>
          <input
            v-model="templateNameInput"
            type="text"
            placeholder="例如：玉米到厂-现款-常规指标"
            class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-autumn-500 outline-none transition-all"
            @keyup.enter="confirmSaveTemplate"
          />
        </div>
      </div>
      <template #footer>
        <div class="flex items-center gap-3">
          <BaseButton type="secondary" @click="saveTemplateDialogVisible = false">取消</BaseButton>
          <BaseButton type="primary" @click="confirmSaveTemplate">保存</BaseButton>
        </div>
      </template>
    </BaseModal>

    <!-- 编辑已发布需求弹窗 -->
    <BaseModal
      v-model="editOpen"
      title="编辑采购需求"
      size="lg"
    >
      <div class="space-y-6">
        <p class="text-sm text-neutral-500">
          仅修改本条已发布需求，不影响您的公司/个人档案
        </p>

        <!-- 标的信息 -->
        <div class="bg-neutral-50 rounded-xl p-4 border border-neutral-200">
          <div class="text-[10px] font-bold uppercase tracking-widest text-neutral-400">采购标的</div>
          <div class="mt-1 font-bold text-neutral-900">
            {{ editing?.categoryName || '-' }}
            <span v-if="editing?.contractNo" class="text-sm text-neutral-500 font-medium ml-2">{{ editing?.contractNo }}</span>
          </div>
        </div>

        <!-- 表单 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">采购数量（吨）</label>
            <input
              v-model.number="editForm.quantity"
              type="number"
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-autumn-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">期望价格（元/吨）</label>
            <input
              v-model.number="editForm.expectedPrice"
              type="number"
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-autumn-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">包装方式</label>
            <input
              v-model="editForm.packaging"
              type="text"
              placeholder="例如：散装 / 袋装"
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-autumn-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">付款方式</label>
            <select
              v-model="editForm.paymentMethod"
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-autumn-500 outline-none transition-all bg-white"
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
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-autumn-500 outline-none transition-all bg-white"
            >
              <option value="">请选择</option>
              <option value="普通发票">普通发票</option>
              <option value="增值税发票">增值税发票</option>
              <option value="不需要发票">不需要发票</option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">交货方式</label>
            <select
              v-model="editForm.deliveryMethod"
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-autumn-500 outline-none transition-all bg-white"
            >
              <option value="">请选择</option>
              <option v-for="opt in currentSchemaConfig.deliveryOptions" :key="opt" :value="opt">{{ opt }}</option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">发布有效期</label>
            <select
              v-model="editForm.expireMinutes"
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-autumn-500 outline-none transition-all bg-white"
            >
              <option :value="60">1小时</option>
              <option :value="1440">1天</option>
              <option :value="4320">3天</option>
              <option :value="10080">7天</option>
              <option :value="43200">30天</option>
            </select>
          </div>
          <div>
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">交付地址</label>
            <input
              v-model="editForm.purchaseAddress"
              type="text"
              placeholder="例如：北京市朝阳区..."
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-autumn-500 outline-none transition-all"
            />
          </div>
          <div class="md:col-span-2">
            <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">备注</label>
            <textarea
              v-model="editForm.remark"
              rows="2"
              placeholder="补充说明（选填）"
              class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-lg focus:border-autumn-500 outline-none transition-all resize-none"
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

/* Neo-Minimal 风格：输入控件 */
:deep(.neo-input-number .el-input__wrapper),
:deep(.neo-select .el-select__wrapper) {
  border: 2px solid rgb(243 244 246); /* neutral-100 */
  border-radius: 12px; /* rounded-xl */
  box-shadow: none;
  background-color: #fff;
  transition: all 0.15s ease;
}

:deep(.neo-input-number .el-input__wrapper.is-focus),
:deep(.neo-select .el-select__wrapper.is-focus) {
  border-color: rgb(16 185 129); /* brand-500 */
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18);
}

:deep(.neo-input-number .el-input__wrapper:hover),
:deep(.neo-select .el-select__wrapper:hover) {
  border-color: rgb(229 231 235); /* neutral-200 */
}

:deep(.neo-input-number),
:deep(.neo-select) {
  width: 100%;
}
</style>
