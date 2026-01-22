<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Trash2, FileText, Save, Send, List, Package, Truck, Clock, FileCheck, TrendingUp, Plus, X } from 'lucide-vue-next'
import { createSupply, getNextSupplyNo, createSupplyTemplate, getMySupplyTemplates, deleteSupplyTemplate, type SupplyCreateRequest, type BasisQuoteRequest, type SupplyTemplateResponse } from '../api/supply'
import { listFuturesContracts, type FuturesContractResponse } from '../api/futures'
import { getProductTree, getProductParams, addProductParamOption, type ProductNode, type ProductParamResponse } from '../api/product'
import { getMyCompany, type CompanyResponse } from '../api/company'
import { getMe, type UserResponse } from '../api/user'
import { codeToText } from 'element-china-area-data'
import TwoLevelCategoryPicker from '../components/TwoLevelCategoryPicker.vue'
import { BaseButton, BaseModal, EmptyState } from '../components/ui'
import { useCompanyStore } from '../stores/company'

const router = useRouter()
const companyStore = useCompanyStore()
const loading = ref(false)
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
const categoryTree = ref<ProductNode[]>([])
const categoryParams = ref<ProductParamResponse[]>([])
const dynamicParams = ref<Record<string, any>>({})

// 品类选择器
type PickedCategory = { id: number; name: string } | null
const pickedCategory = ref<PickedCategory>(null)
const suspendCategoryWatch = ref(false)

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
  expireMinutes?: number
  paramsJson?: string
  remark?: string
}

const templateJsonCache = new Map<number, { json: string; data: TemplateJsonData }>()

function getTemplateJson(template: SupplyTemplateResponse): TemplateJsonData {
  const key = template.id
  const json = template.templateJson || ''
  const cached = templateJsonCache.get(key)
  if (cached && cached.json === json) return cached.data
  let data: TemplateJsonData = {}
  try { data = (JSON.parse(json) ?? {}) as TemplateJsonData } catch { data = {} }
  templateJsonCache.set(key, { json, data })
  return data
}

function formatPrice(p?: number) {
  const n = Number(p)
  if (!p && p !== 0) return '面议'
  if (Number.isNaN(n)) return '面议'
  return `¥${n}/吨`
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
  await Promise.all([loadCategoryTree(), loadCompanyInfo(), loadMeUser(), loadTemplates(), loadFuturesContracts()])
  await loadNextSupplyNo()
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

async function loadCategoryTree() {
  try {
    const r = await getProductTree()
    if (r.code === 0) categoryTree.value = r.data || []
  } catch { ElMessage.error('加载品类树失败') }
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

watch(pickedCategory, async (category) => {
  if (suspendCategoryWatch.value) return
  if (category) {
    publishForm.categoryId = category.id
    publishForm.categoryName = category.name
    await loadCategoryParams(category.id)
    await loadNextSupplyNo()
  } else {
    publishForm.categoryId = undefined
    publishForm.categoryName = ''
    categoryParams.value = []
    dynamicParams.value = {}
  }
}, { deep: true })

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

async function addParamOption(paramId: number, optionValue: string) {
  try {
    const r = await addProductParamOption(paramId, optionValue)
    if (r.code === 0) {
      ElMessage.success('选项添加成功')
      if (publishForm.categoryId) await loadCategoryParams(publishForm.categoryId)
    }
  } catch { ElMessage.error('添加选项失败') }
}

function buildParamsJson() {
  const params: Record<string, any> = {}
  categoryParams.value.forEach(param => {
    const value = dynamicParams.value[param.id]
    if (value !== undefined && value !== '') {
      params[param.paramName] = value
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
      router.push('/supply/published')
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
      categoryId: publishForm.categoryId,
      categoryName: publishForm.categoryName,
      exFactoryPrice: publishForm.exFactoryPrice,
      quantity: publishForm.quantity,
      packaging: publishForm.packaging,
      shipAddress: publishForm.shipAddress,
      deliveryMode: publishForm.deliveryMode,
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
    const res = await deleteSupplyTemplate(id)
    if (res.code === 0) {
      templates.value = templates.value.filter(t => t.id !== id)
      ElMessage.success('模板删除成功')
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

async function applyTemplate(template: SupplyTemplateResponse) {
  try {
    const data = JSON.parse(template.templateJson)
    publishForm.companyName = data.companyName || publishForm.companyName
    publisherNameInput.value = data.publisherName || publisherNameInput.value

    suspendCategoryWatch.value = true
    pickedCategory.value = data.categoryId ? { id: data.categoryId, name: data.categoryName || String(data.categoryId) } : null
    publishForm.categoryId = data.categoryId
    publishForm.categoryName = data.categoryName || ''
    publishForm.exFactoryPrice = data.exFactoryPrice
    publishForm.quantity = data.quantity
    publishForm.packaging = data.packaging || '散装'
    publishForm.shipAddress = data.shipAddress || ''
    publishForm.deliveryMode = data.deliveryMode || '到厂'
    publishForm.expireMinutes = data.expireMinutes || 4320
    publishForm.remark = data.remark || ''
    
    if (data.categoryId) await loadCategoryParams(data.categoryId)
    else { categoryParams.value = []; dynamicParams.value = {} }

    if (data.paramsJson) {
      try {
        const paramsData = JSON.parse(data.paramsJson)
        // 支持新格式 {"参数名": "值"} 和旧格式 {"params": {"ID": {"value": "值"}}}
        const isNewFormat = !paramsData.params && !paramsData.custom
        
        if (isNewFormat) {
          // 新格式：通过名称找 ID
          Object.entries(paramsData).forEach(([name, value]) => {
            const param = categoryParams.value.find(p => p.paramName === name)
            if (param) dynamicParams.value[param.id] = value
          })
        } else {
          // 旧格式兼容
          const oldParams = paramsData.params || {}
          Object.entries(oldParams).forEach(([paramId, val]: [string, any]) => {
            const actualValue = (typeof val === 'object' && val !== null && 'value' in val) ? val.value : val
            dynamicParams.value[Number(paramId)] = actualValue
          })
        }
      } catch { /* ignore */ }
    }

    suspendCategoryWatch.value = false
    ElMessage.success('模板已应用')
    templatePickerOpen.value = false
  } catch {
    suspendCategoryWatch.value = false
    ElMessage.error('模板数据格式错误')
  }
}
</script>

<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between flex-wrap gap-4">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">发布供应</h1>
        <p class="text-sm text-gray-500 mt-1">填写供应信息并发布到大厅</p>
      </div>
      <div class="flex items-center gap-3">
        <BaseButton type="secondary" size="sm" @click="templatePickerOpen = true">
          <FileText class="w-4 h-4" />
          选择模板
        </BaseButton>
        <BaseButton type="secondary" size="sm" @click="saveAsTemplate">
          <Save class="w-4 h-4" />
          保存模板
        </BaseButton>
        <BaseButton type="outline" size="sm" @click="router.push('/supply/published')">
          <List class="w-4 h-4" />
          已发布
        </BaseButton>
        <BaseButton type="primary" size="sm" :loading="loading" @click="publishSupply">
          <Send class="w-4 h-4" />
          发布
        </BaseButton>
      </div>
    </div>

    <!-- 双栏布局 -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧表单区域 -->
      <div class="lg:col-span-2 space-y-6">
        <!-- 发布信息 -->
        <div class="bg-white rounded-xl border border-gray-200 overflow-hidden animate-fade-in">
          <div class="p-5 border-b border-gray-200 flex items-center gap-2">
            <div class="w-1.5 h-5 bg-slate-900 rounded-full"></div>
            <h3 class="text-lg font-bold text-gray-900">发布信息</h3>
          </div>
          <div class="p-5 space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">公司名称</label>
                <input
                  v-model="publishForm.companyName"
                  type="text"
                  placeholder="默认使用公司名称"
                  class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
                />
              </div>
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">发布人</label>
                <input
                  v-model="publisherNameInput"
                  type="text"
                  placeholder="默认使用个人信息"
                  class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
                />
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">发货地址</label>
              <input
                v-model="publishForm.shipAddress"
                type="text"
                placeholder="请输入发货地址"
                class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
              />
            </div>
            <p class="text-xs text-gray-400">以上信息仅用于本次发布，不会修改您的公司/个人资料</p>
          </div>
        </div>

        <!-- 基础信息 -->
        <div class="bg-white rounded-xl border border-gray-200 overflow-hidden animate-fade-in" style="animation-delay: 50ms">
          <div class="p-5 border-b border-gray-200 flex items-center gap-2">
            <div class="w-1.5 h-5 bg-brand-600 rounded-full"></div>
            <h3 class="text-lg font-bold text-gray-900">基础信息</h3>
          </div>
          <div class="p-5 space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
                  品类选择 <span class="text-red-500">*</span>
                </label>
                <TwoLevelCategoryPicker v-model="pickedCategory" />
              </div>
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">可供数量（吨）</label>
                <el-input-number v-model="publishForm.quantity" :min="0" :step="1" :controls="false" class="w-full neo-input-number" />
              </div>
              <div v-if="showBasisOption">
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">报价方式</label>
                <el-radio-group v-model="publishForm.priceType" class="w-full">
                  <el-radio-button :value="0" class="flex-1">现货一口价</el-radio-button>
                  <el-radio-button :value="1" class="flex-1">基差报价</el-radio-button>
                </el-radio-group>
              </div>
              <div v-else>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
                  出厂价（元/吨）<span class="text-red-500">*</span>
                </label>
                <el-input-number v-model="publishForm.exFactoryPrice" :min="0" :step="10" :controls="false" class="w-full neo-input-number" />
              </div>
            </div>
            
            <!-- 现货一口价输入 -->
            <div v-if="showBasisOption && publishForm.priceType === 0" class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
                  出厂价（元/吨）<span class="text-red-500">*</span>
                </label>
                <el-input-number v-model="publishForm.exFactoryPrice" :min="0" :step="10" :controls="false" class="w-full neo-input-number" />
              </div>
            </div>
            
            <!-- 基差报价输入区域 -->
            <div v-if="showBasisOption && publishForm.priceType === 1" class="space-y-4">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-2">
                  <TrendingUp class="w-4 h-4 text-amber-500" />
                  <span class="text-sm font-bold text-gray-700">基差报价明细</span>
                </div>
                <button
                  type="button"
                  class="flex items-center gap-1 px-3 py-1.5 bg-amber-50 text-amber-600 rounded-lg text-sm font-bold hover:bg-amber-100 transition-all"
                  @click="addBasisQuote"
                >
                  <Plus class="w-4 h-4" />
                  添加合约
                </button>
              </div>
              
              <div class="bg-amber-50/50 rounded-xl p-4 border border-amber-100">
                <div v-if="basisQuotes.length === 0" class="text-center py-4 text-gray-500 text-sm">
                  点击"添加合约"开始配置基差报价
                </div>
                <div v-else class="space-y-4">
                  <div v-for="(quote, index) in basisQuotes" :key="index" class="bg-white rounded-xl p-4 border border-gray-200 shadow-sm transition-all hover:shadow-md">
                    <div class="flex flex-col md:flex-row md:items-start gap-4">
                      <div class="flex-1 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                        <div class="space-y-1">
                          <label class="block text-[10px] font-bold text-gray-400 uppercase tracking-widest">1. 选择期货合约</label>
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
                                  <span v-else class="text-[8px] text-gray-400">已收盘</span>
                                </div>
                              </div>
                            </el-option>
                          </el-select>
                        </div>
                        <div class="space-y-1">
                          <label class="block text-[10px] font-bold text-gray-400 uppercase tracking-widest">2. 设定基差 (元/吨)</label>
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
                          <label class="block text-[10px] font-bold text-gray-400 uppercase tracking-widest">3. 合约可售量 (吨)</label>
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
                            <span v-else class="text-sm font-bold text-gray-400 italic">等待选择合约</span>
                          </div>
                        </div>
                      </div>
                      
                      <button
                        type="button"
                        class="self-end md:self-start p-2.5 rounded-xl bg-red-50 text-red-500 hover:bg-red-500 hover:text-white transition-all shrink-0  shadow-sm"
                        title="移除此合约"
                        @click="removeBasisQuote(index)"
                      >
                        <X class="w-5 h-5" />
                      </button>
                    </div>

                    <!-- 行情实时面板 -->
                    <div v-if="quote.contractCode && getContractByCode(quote.contractCode)" class="mt-4 pt-4 border-t border-dashed border-gray-200 flex flex-wrap items-center gap-y-2 gap-x-6 text-xs">
                      <div class="flex items-center gap-2">
                        <div 
                          class="w-1.5 h-1.5 rounded-full" 
                          :class="getContractByCode(quote.contractCode)?.isTrading ? 'bg-brand-500 animate-pulse' : 'bg-gray-400'"
                        ></div>
                        <span class="text-gray-500 font-medium">
                          {{ getContractByCode(quote.contractCode)?.isTrading ? '期货实时报价' : '盘面参考价' }}:
                        </span>
                        <span class="font-black text-gray-900 text-sm italic">¥{{ getContractByCode(quote.contractCode)?.lastPrice || '-' }}</span>
                        <span v-if="!getContractByCode(quote.contractCode)?.isTrading" class="text-[10px] text-gray-400 font-bold bg-gray-100 px-1.5 py-0.5 rounded ml-1">
                          {{ getContractByCode(quote.contractCode)?.lastPrice === getContractByCode(quote.contractCode)?.prevClose ? '昨收' : '已收盘' }}
                        </span>
                      </div>
                      
                      <div v-if="quote.basisPrice !== undefined" class="flex items-center gap-2">
                        <span class="text-gray-500 font-medium">当前基差:</span>
                        <span class="font-black px-2 py-0.5 rounded" :class="(quote.basisPrice || 0) >= 0 ? 'bg-red-50 text-red-600' : 'bg-brand-50 text-brand-600'">
                          {{ (quote.basisPrice || 0) >= 0 ? '+' : '' }}{{ quote.basisPrice }}
                        </span>
                      </div>
                      
                      <div v-if="getContractByCode(quote.contractCode)?.priceUpdateTime" class="ml-auto flex items-center gap-1 text-gray-400">
                        <Clock class="w-3 h-3" />
                        <span>数据更新: {{ new Date(getContractByCode(quote.contractCode)!.priceUpdateTime!).toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' }) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="mt-4 space-y-2 bg-amber-50 p-4 rounded-xl border border-amber-100/50">
                  <div class="flex items-start gap-2 text-xs text-amber-700">
                    <div class="w-1.5 h-1.5 rounded-full bg-amber-500 mt-1 shrink-0"></div>
                    <p class="font-medium">核心公式：核算远期价 = 期货盘面报价 + 您设定的基差（升水为正，贴水为负）。</p>
                  </div>
                  <div class="flex items-start gap-2 text-xs text-amber-600/80 italic">
                    <div class="w-1.5 h-1.5 rounded-full bg-amber-400/50 mt-1 shrink-0"></div>
                    <p>法律提示：基差报价随盘面实时变动，最终结算以成交时点盘面价+基差为准。如遇休盘，最新价将参考前一交易日收盘价。</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 规格参数 -->
        <div class="bg-white rounded-xl border border-gray-200 overflow-hidden animate-fade-in" style="animation-delay: 100ms">
          <div class="p-5 border-b border-gray-200 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <div class="w-1.5 h-5 bg-brand-500 rounded-full"></div>
              <h3 class="text-lg font-bold text-gray-900">规格参数</h3>
            </div>
            <span class="text-xs text-gray-400">选择品类后自动加载</span>
          </div>
          <div class="p-5">
            <div v-if="categoryParams.length === 0" class="py-8">
              <EmptyState
                type="data"
                title="暂无参数"
                description="选择品类后，会自动加载对应的指标参数"
                size="sm"
              />
            </div>
            <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div v-for="param in categoryParams" :key="param.id">
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">{{ param.paramName }}</label>
                <el-select
                  v-if="param.paramType === 1"
                  v-model="dynamicParams[param.id]"
                  :placeholder="`请选择${param.paramName}`"
                  allow-create
                  filterable
                  class="w-full neo-select"
                  @visible-change="(visible: boolean) => {
                    if (!visible && dynamicParams[param.id] && !param.options?.includes(dynamicParams[param.id])) {
                      addParamOption(param.id, dynamicParams[param.id])
                    }
                  }"
                >
                  <el-option v-for="option in param.options" :key="option" :label="option" :value="option" />
                </el-select>
                <input
                  v-else
                  v-model="dynamicParams[param.id]"
                  type="text"
                  :placeholder="`请输入${param.paramName}`"
                  class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 物流与交付 -->
        <div class="bg-white rounded-xl border border-gray-200 overflow-hidden animate-fade-in" style="animation-delay: 150ms">
          <div class="p-5 border-b border-gray-200 flex items-center gap-2">
            <div class="w-1.5 h-5 bg-amber-500 rounded-full"></div>
            <h3 class="text-lg font-bold text-gray-900">物流与交付</h3>
          </div>
          <div class="p-5 space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">发布有效期</label>
                <el-select v-model="publishForm.expireMinutes" clearable class="w-full neo-select">
                  <el-option label="1小时" :value="60" />
                  <el-option label="1天" :value="1440" />
                  <el-option label="3天" :value="4320" />
                  <el-option label="7天" :value="10080" />
                  <el-option label="30天" :value="43200" />
                </el-select>
              </div>
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">包装方式</label>
                <el-select v-model="publishForm.packaging" class="w-full neo-select">
                  <el-option label="散装" value="散装" />
                  <el-option label="袋装" value="袋装" />
                  <el-option label="箱装" value="箱装" />
                </el-select>
              </div>
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">交付方式</label>
                <el-select v-model="publishForm.deliveryMode" class="w-full neo-select">
                  <el-option label="到厂" value="到厂" />
                  <el-option label="自提" value="自提" />
                  <el-option label="物流配送" value="物流配送" />
                </el-select>
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">补充说明</label>
              <textarea
                v-model="publishForm.remark"
                rows="3"
                placeholder="备注（可选）"
                class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all resize-none"
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
        <div class="bg-white rounded-xl border border-gray-200 overflow-hidden sticky top-24 animate-fade-in" style="animation-delay: 200ms">
          <div class="p-5 border-b border-gray-200">
            <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">summary</div>
            <h3 class="font-bold text-gray-900 mt-1">发布前确认</h3>
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
                <div class="bg-gray-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">公司</div>
                  <div class="mt-1 font-bold text-gray-900 truncate text-sm">{{ publishForm.companyName || '未指定' }}</div>
                </div>
                <div class="bg-gray-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">发布人</div>
                  <div class="mt-1 font-bold text-gray-900 truncate text-sm">{{ publisherNameInput || publisherName }}</div>
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
                <div class="bg-gray-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">数量</div>
                  <div class="mt-1 font-bold text-gray-900">{{ previewData.quantity }} 吨</div>
                </div>
                <div v-if="publishForm.priceType === 0" class="bg-gray-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">出厂价</div>
                  <div class="mt-1 font-bold text-brand-600">
                    <span v-if="previewData.exFactoryPrice != null">¥{{ previewData.exFactoryPrice }}/吨</span>
                    <span v-else class="text-gray-500">面议</span>
                  </div>
                </div>
                <div v-else class="bg-amber-50 rounded-xl p-3 border border-amber-100">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-amber-600">报价方式</div>
                  <div class="mt-1 font-bold text-amber-700">基差报价</div>
                </div>
              </div>
              <!-- 基差报价预览 -->
              <div v-if="publishForm.priceType === 1 && basisQuotes.length > 0" class="bg-amber-50/50 rounded-xl p-3 border border-amber-100">
                <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-amber-600 mb-2">
                  <TrendingUp class="w-3 h-3" />
                  基差明细
                </div>
                <div class="space-y-2">
                  <div v-for="(quote, index) in basisQuotes.filter(q => q.contractCode)" :key="index" class="bg-white rounded-lg p-2 border border-amber-100">
                    <div class="flex items-center justify-between text-sm">
                      <span class="font-medium text-gray-700">{{ getContractByCode(quote.contractCode)?.contractName || quote.contractCode }}</span>
                      <span class="font-bold" :class="(quote.basisPrice || 0) >= 0 ? 'text-red-600' : 'text-green-600'">
                        {{ (quote.basisPrice || 0) >= 0 ? '+' : '' }}{{ quote.basisPrice }}
                      </span>
                    </div>
                    <div class="flex items-center justify-between text-xs text-gray-500 mt-1">
                      <span>
                        期货 ¥{{ getContractByCode(quote.contractCode)?.lastPrice || '-' }} → 
                        <span class="font-bold text-brand-600">核算 ¥{{ calcReferencePrice(quote.contractCode, quote.basisPrice) || '-' }}</span>
                      </span>
                      <span>{{ quote.availableQty }}吨</span>
                    </div>
                  </div>
                </div>
              </div>
              <div class="bg-gray-50 rounded-xl p-3">
                <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-gray-400">
                  <Truck class="w-3 h-3" />
                  发货地址
                </div>
                <div class="mt-1 font-bold text-gray-900 text-sm">{{ previewData.shipAddress }}</div>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div class="bg-gray-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">交付</div>
                  <div class="mt-1 font-bold text-gray-900">{{ previewData.deliveryMode }}</div>
                </div>
                <div class="bg-gray-50 rounded-xl p-3">
                  <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-gray-400">
                    <Clock class="w-3 h-3" />
                    有效期
                  </div>
                  <div class="mt-1 font-bold text-gray-900">{{ previewData.expireText }}</div>
                </div>
              </div>
              <div v-if="previewData.paramsText !== '无'" class="bg-gray-50 rounded-xl p-3">
                <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-gray-400">
                  <FileCheck class="w-3 h-3" />
                  指标
                </div>
                <div class="mt-1 text-sm text-gray-700 whitespace-pre-wrap">{{ previewData.paramsText }}</div>
              </div>
              <div v-if="previewData.remark" class="bg-gray-50 rounded-xl p-3">
                <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">备注</div>
                <div class="mt-1 text-sm text-gray-700">{{ previewData.remark }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 模板选择弹窗 -->
    <BaseModal v-model="templatePickerOpen" title="选择供应模板" size="lg">
      <div v-if="templates.length === 0" class="py-8">
        <EmptyState
          type="data"
          title="暂无模板"
          description="可在发布表单中点击【保存为模板】创建"
          size="md"
        />
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="template in templates"
          :key="template.id"
          class="bg-white rounded-xl border border-gray-200 p-4 hover:shadow-md hover:border-brand-100 transition-all cursor-pointer"
          @click="applyTemplate(template)"
        >
          <div class="flex items-start justify-between gap-3 mb-3">
            <div class="min-w-0">
              <div class="font-bold text-gray-900 truncate">{{ template.templateName }}</div>
              <div class="text-xs text-gray-500 mt-0.5">{{ getTemplateJson(template).categoryName || '未分类' }}</div>
            </div>
            <button
              class="p-1.5 rounded-lg bg-red-50 text-red-500 hover:bg-red-100 transition-all"
              @click.stop="deleteTemplate(template.id)"
            >
              <Trash2 class="w-4 h-4" />
            </button>
          </div>
          <div class="grid grid-cols-2 gap-2 text-sm">
            <div class="bg-gray-50 rounded-lg px-2.5 py-1.5">
              <div class="text-[10px] text-gray-400">数量</div>
              <div class="font-bold text-gray-900">{{ getTemplateJson(template).quantity || 0 }} 吨</div>
            </div>
            <div class="bg-gray-50 rounded-lg px-2.5 py-1.5">
              <div class="text-[10px] text-gray-400">出厂价</div>
              <div class="font-bold text-brand-600">{{ formatPrice(getTemplateJson(template).exFactoryPrice) }}</div>
            </div>
          </div>
          <div class="mt-2 text-xs text-gray-400">
            {{ formatDate(template.createTime) }} · 点击使用
          </div>
        </div>
      </div>
      <template #footer>
        <BaseButton type="secondary" @click="templatePickerOpen = false">关闭</BaseButton>
      </template>
    </BaseModal>

    <!-- 保存模板弹窗 -->
    <BaseModal v-model="saveTemplateDialogVisible" title="保存为模板" size="sm">
      <div class="space-y-4">
        <p class="text-sm text-gray-500">用于一键复用本次发布的品类、条款与指标配置</p>
        <div>
          <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
            模板名称 <span class="text-red-500">*</span>
          </label>
          <input
            v-model="templateNameInput"
            type="text"
            placeholder="例如：玉米到厂-现款-常规指标"
            class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
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
  </div>
</template>

<style scoped>
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
