<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createRequirement, getNextRequirementNo, type RequirementCreateRequest } from '../api/requirement'
import { listMyRequirementTemplates as listRequirementTemplates, createRequirementTemplate, deleteRequirementTemplate, type RequirementTemplateCreateRequest, type RequirementTemplateResponse } from '../api/requirementTemplate'
import { getProductTree, getProductParams, addProductParamOption, type ProductNode, type ProductParamResponse } from '../api/product'
import { getMyCompany, type CompanyResponse } from '../api/company'
import { getMe, type UserResponse } from '../api/user'
import TwoLevelCategoryPicker from '../components/TwoLevelCategoryPicker.vue'
import { BaseButton, BaseModal, EmptyState } from '../components/ui'
import { FileText, Save, List, Send, Package, MapPin, Clock, FileCheck, CreditCard, Trash2 } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { useCompanyStore } from '../stores/company'

const router = useRouter()
const auth = useAuthStore()
const companyStore = useCompanyStore()
const loading = ref(false)
const contractNo = ref<string>('')
const templatePickerOpen = ref(false)
const meUser = ref<UserResponse | null>(null)
const purchaserNameInput = ref('')

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

type PickedCategory = { id: number; name: string } | null
const pickedCategory = ref<PickedCategory>(null)
const suspendCategoryWatch = ref(false)

type TemplateJsonData = {
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

const templateJsonCache = new Map<number, { json: string; data: TemplateJsonData }>()

function getTemplateJson(template: RequirementTemplateResponse): TemplateJsonData {
  const key = template.id
  const json = template.templateJson || ''
  const cached = templateJsonCache.get(key)
  if (cached && cached.json === json) return cached.data
  let data: TemplateJsonData = {}
  try {
    data = (JSON.parse(json) ?? {}) as TemplateJsonData
  } catch {
    data = {}
  }
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

// 品类相关
const categoryTree = ref<ProductNode[]>([])
const categoryParams = ref<ProductParamResponse[]>([])

// 动态参数表单
const dynamicParams = ref<Record<string, any>>({})

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
    loadCategoryTree(),
    loadTemplates(),
    loadCompanyInfo(),
    loadMeUser()
  ])
  // 获取下一个合同号
  await loadNextContractNo()
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

async function loadCategoryTree() {
  try {
    const r = await getProductTree()
    if (r.code === 0) categoryTree.value = r.data || []
  } catch (e) {
    ElMessage.error('加载品类树失败')
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
  pickedCategory,
  async (category) => {
    if (suspendCategoryWatch.value) return
    if (category) {
      publishForm.categoryId = category.id
      publishForm.categoryName = category.name
      await loadCategoryParams(category.id)
      await loadNextContractNo()
    } else {
      publishForm.categoryId = undefined
      publishForm.categoryName = ''
      categoryParams.value = []
      dynamicParams.value = {}
    }
  },
  { deep: true }
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

async function addParamOption(paramId: number, optionValue: string) {
  try {
    const r = await addProductParamOption(paramId, optionValue)
    if (r.code === 0) {
      ElMessage.success('选项添加成功')
      // 重新加载参数
      if (publishForm.categoryId) {
        await loadCategoryParams(publishForm.categoryId)
      }
    }
  } catch (e) {
    ElMessage.error('添加选项失败')
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
      router.push('/requirements/published')
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
    const r = await deleteRequirementTemplate(id)
    if (r.code === 0) {
      ElMessage.success('模板删除成功')
      await loadTemplates()
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

async function applyTemplate(template: RequirementTemplateResponse) {
  try {
    const data = JSON.parse(template.templateJson)
    // 填充到发布表单
    publishForm.companyName = data.companyName || publishForm.companyName
    purchaserNameInput.value = data.purchaserName || purchaserNameInput.value

    suspendCategoryWatch.value = true
    pickedCategory.value = data.categoryId ? { id: data.categoryId, name: data.categoryName || String(data.categoryId) } : null
    publishForm.categoryId = data.categoryId
    publishForm.categoryName = data.categoryName || ''
    publishForm.quantity = data.quantity
    publishForm.packaging = data.packaging || '散装'
    publishForm.paymentMethod = data.paymentMethod || '现款'
    publishForm.expireMinutes = data.expireMinutes || 4320
    publishForm.purchaseAddress = data.purchaseAddress || ''
    publishForm.expectedPrice = data.expectedPrice
    publishForm.invoiceType = data.invoiceType || ''
    publishForm.deliveryMethod = data.deliveryMethod || ''
    publishForm.remark = data.remark || ''
    
    // 加载参数
    if (data.categoryId) {
      await loadCategoryParams(data.categoryId)
    } else {
      categoryParams.value = []
      dynamicParams.value = {}
    }

    // loadCategoryParams 会初始化 dynamicParams，这里必须在其后回填模板参数，避免覆盖
    if (data.paramsJson) {
      try {
        const paramsData = JSON.parse(data.paramsJson)
        // 支持新格式 {"参数名": "值"} 和旧格式 {"params": {"ID": "值"}}
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
          Object.entries(oldParams).forEach(([paramId, value]) => {
            dynamicParams.value[Number(paramId)] = value
          })
        }
      } catch {
        // ignore
      }
    }

    suspendCategoryWatch.value = false
    ElMessage.success('模板已应用到表单')
    templatePickerOpen.value = false
  } catch (e) {
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
        <h1 class="text-2xl font-bold text-gray-900">发布采购需求</h1>
        <p class="text-sm text-gray-500 mt-1">填写采购信息并发布到大厅</p>
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
        <BaseButton type="outline" size="sm" @click="router.push('/requirements/published')">
          <List class="w-4 h-4" />
          已发布
        </BaseButton>
        <BaseButton type="primary" size="sm" :loading="loading" @click="publishRequirement">
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
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">采购人</label>
                <input
                  v-model="purchaserNameInput"
                  type="text"
                  placeholder="默认使用个人信息"
                  class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
                />
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">交付地址</label>
              <input
                v-model="publishForm.purchaseAddress"
                type="text"
                placeholder="请输入交付/收货地址"
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
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
                  采购数量（吨）<span class="text-red-500">*</span>
                </label>
                <el-input-number v-model="publishForm.quantity" :min="0" :step="1" :controls="false" class="w-full neo-input-number" />
              </div>
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">期望价格（元/吨）</label>
                <el-input-number v-model="publishForm.expectedPrice" :min="0" :step="10" :controls="false" class="w-full neo-input-number" />
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
                type="folder"
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
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">交货方式</label>
                <el-select v-model="publishForm.deliveryMethod" clearable class="w-full neo-select">
                  <el-option label="到厂" value="到厂" />
                  <el-option label="自提" value="自提" />
                  <el-option label="物流配送" value="物流配送" />
                </el-select>
              </div>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">付款方式</label>
                <el-select v-model="publishForm.paymentMethod" class="w-full neo-select">
                  <el-option label="现款" value="现款" />
                  <el-option label="账期" value="账期" />
                </el-select>
              </div>
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">发票类型</label>
                <el-select v-model="publishForm.invoiceType" clearable class="w-full neo-select">
                  <el-option label="普通发票" value="普通发票" />
                  <el-option label="增值税发票" value="增值税发票" />
                  <el-option label="不需要发票" value="不需要发票" />
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
        <div class="bg-white rounded-xl border border-gray-200 overflow-hidden sticky top-24 animate-fade-in" style="animation-delay: 200ms">
          <div class="p-5 border-b border-gray-200">
            <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">summary</div>
            <h3 class="font-bold text-gray-900 mt-1">发布前确认</h3>
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
                <div class="bg-gray-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">公司</div>
                  <div class="mt-1 font-bold text-gray-900 truncate text-sm">{{ publishForm.companyName || '未指定' }}</div>
                </div>
                <div class="bg-gray-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">采购人</div>
                  <div class="mt-1 font-bold text-gray-900 truncate text-sm">{{ purchaserNameInput || purchaserName }}</div>
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
                <div class="bg-gray-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">期望价</div>
                  <div class="mt-1 font-bold text-brand-600">
                    <span v-if="previewData.expectedPrice != null">¥{{ previewData.expectedPrice }}/吨</span>
                    <span v-else class="text-gray-500">面议</span>
                  </div>
                </div>
              </div>
              <div class="bg-gray-50 rounded-xl p-3">
                <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-gray-400">
                  <MapPin class="w-3 h-3" />
                  交付地址
                </div>
                <div class="mt-1 font-bold text-gray-900 text-sm">{{ previewData.purchaseAddress }}</div>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div class="bg-gray-50 rounded-xl p-3">
                  <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-gray-400">
                    <CreditCard class="w-3 h-3" />
                    付款
                  </div>
                  <div class="mt-1 font-bold text-gray-900">{{ publishForm.paymentMethod || '现款' }}</div>
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
    <BaseModal v-model="templatePickerOpen" title="选择采购模板" size="lg">
      <div v-if="templates.length === 0" class="py-8">
        <EmptyState
          type="folder"
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
              <div class="text-[10px] text-gray-400">期望价</div>
              <div class="font-bold text-brand-600">{{ formatPrice(getTemplateJson(template).expectedPrice) }}</div>
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

    <!-- 保存为模板弹窗 -->
    <BaseModal v-model="saveTemplateDialogVisible" title="保存为模板" size="sm">
      <div class="space-y-4">
        <p class="text-xs text-gray-500">用于一键复用本次发布的品类、条款与指标配置</p>
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
        <div class="flex items-center gap-3">
          <BaseButton type="secondary" @click="saveTemplateDialogVisible = false">取消</BaseButton>
          <BaseButton type="primary" @click="confirmSaveTemplate">保存</BaseButton>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<style scoped>
/* Neo-Minimal 风格：输入控件 */
:deep(.neo-input-number .el-input__wrapper),
:deep(.neo-select .el-select__wrapper) {
  border: 2px solid rgb(243 244 246); /* gray-100 */
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
  border-color: rgb(229 231 235); /* gray-200 */
}

:deep(.neo-input-number),
:deep(.neo-select) {
  width: 100%;
}
</style>
