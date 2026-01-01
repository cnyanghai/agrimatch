<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, List, Document, InfoFilled, Operation, Plus, Delete } from '@element-plus/icons-vue'
// import { useAppStore } from '../store/app' // 暂时未使用
import { createRequirement, listRequirements, getNextRequirementNo, type RequirementCreateRequest, type RequirementResponse } from '../api/requirement'
import { listMyRequirementTemplates as listRequirementTemplates, createRequirementTemplate, deleteRequirementTemplate, type RequirementTemplateCreateRequest, type RequirementTemplateResponse } from '../api/requirementTemplate'
import { getProductTree, getProductParams, addProductParamOption, type ProductNode, type ProductParamResponse } from '../api/product'
import { getMyCompany, type CompanyResponse } from '../api/company'
import TwoLevelCategoryPicker from '../components/TwoLevelCategoryPicker.vue'
import PageHeader from '../components/PageHeader.vue'

// const app = useAppStore() // 暂时未使用
const loading = ref(false)
const activeTab = ref('publish')
const contractNo = ref<string>('')

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

// 模板表单
const templateForm = reactive({
  templateName: '',
  categoryId: undefined as number | undefined,
  categoryName: '',
  quantity: undefined as number | undefined,
  packaging: '散装',
  paymentMethod: '现款',
  paramsJson: '{}',
  expireMinutes: 4320,
  purchaseLat: undefined as number | undefined,
  purchaseLng: undefined as number | undefined,
  purchaseAddress: ''
})

// 列表数据
const requirements = ref<RequirementResponse[]>([])
const templates = ref<RequirementTemplateResponse[]>([])

// 品类相关
const categoryTree = ref<ProductNode[]>([])
const categoryParams = ref<ProductParamResponse[]>([])

// 动态参数表单
const dynamicParams = ref<Record<string, any>>({})

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
    contractNo: contractNo.value || '自动生成',
    companyName: publishForm.companyName || '未指定',
    categoryName: publishForm.categoryName || '未选择',
    quantity: publishForm.quantity || 0,
    packaging: publishForm.packaging || '散装',
    paymentMethod: publishForm.paymentMethod || '现款',
    expectedPrice: publishForm.expectedPrice,
    invoiceType: publishForm.invoiceType || '未指定',
    deliveryMethod: publishForm.deliveryMethod || '未指定',
    purchaseAddress: publishForm.purchaseAddress || '未指定',
    expireText,
    paramsText,
    remark: publishForm.remark || ''
  }
})

onMounted(async () => {
  await Promise.all([
    loadCategoryTree(),
    loadRequirements(),
    loadTemplates(),
    loadCompanyInfo()
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
      publishForm.purchaseAddress = r.data.address || ''
    }
  } catch (e) {
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

async function loadRequirements() {
  loading.value = true
  try {
    const r = await listRequirements({
      categoryName: filters.categoryName || undefined,
      status: filters.status
    })
    if (r.code === 0) {
      requirements.value = r.data || []
      pagination.total = requirements.value.length
    }
  } catch (e) {
    ElMessage.error('加载需求列表失败')
  } finally {
    loading.value = false
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

async function onCategoryChange(category: { id: number; name: string } | null) {
  if (category) {
    publishForm.categoryId = category.id
    publishForm.categoryName = category.name
    await loadCategoryParams(category.id)
    // 重新获取合同号（如果有新编号逻辑）
    await loadNextContractNo()
  } else {
    publishForm.categoryId = undefined
    publishForm.categoryName = ''
    categoryParams.value = []
    dynamicParams.value = {}
  }
}


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
  const custom: Record<string, any> = {}
  
  categoryParams.value.forEach(param => {
    const value = dynamicParams.value[param.id]
    if (value !== undefined && value !== '') {
      params[param.id] = value
    }
  })
  
  return JSON.stringify({ params, custom })
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
      activeTab.value = 'list'
      await loadRequirements()
      // 重置表单（保留公司名称和采购地址的默认值）
      Object.assign(publishForm, {
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
      categoryParams.value = []
      dynamicParams.value = {}
      await loadNextContractNo()
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

function applyTemplate(template: RequirementTemplateResponse) {
  try {
    const data = JSON.parse(template.templateJson)
    // 填充到发布表单
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
      loadCategoryParams(data.categoryId)
      // 如果有保存的参数，需要解析并填充到dynamicParams
      if (data.paramsJson) {
        try {
          const paramsData = JSON.parse(data.paramsJson)
          if (paramsData.params) {
            Object.entries(paramsData.params).forEach(([paramId, value]) => {
              dynamicParams.value[Number(paramId)] = value
            })
          }
        } catch (e) {
          // 忽略解析错误
        }
      }
    }
    ElMessage.success('模板已应用到表单')
  } catch (e) {
    ElMessage.error('模板数据格式错误')
  }
}

function handlePageChange(page: number) {
  pagination.page = page
  loadRequirements()
}

function handleFilter() {
  pagination.page = 1
  loadRequirements()
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

function getStatusType(status?: number) {
  switch (status) {
    case 0: return 'primary'
    case 1: return 'warning'
    case 2: return 'info'
    case 3: return 'success'
    default: return 'info'
  }
}

const tabs = [
  { key: 'publish', label: '发布采购', icon: Edit },
  { key: 'list', label: '已发布', icon: List },
  { key: 'templates', label: '采购模板', icon: Document }
]
</script>

<template>
  <div class="requirement-view">
    <PageHeader title="采购管理" subtitle="发布采购需求、保存需求模板、查看需求列表" />

    <!-- Tab导航 -->
    <div class="bg-white rounded-xl shadow-card border border-gray-100 mb-6 overflow-hidden">
      <div class="flex border-b border-gray-100">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="flex-1 py-4 text-center font-medium transition-all relative"
          :class="activeTab === tab.key ? 'text-emerald-700 bg-emerald-50/50' : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'"
          @click="activeTab = tab.key"
        >
          <span class="mr-2 inline-flex align-text-bottom"><el-icon><component :is="tab.icon" /></el-icon></span>{{ tab.label }}
          <div v-if="activeTab === tab.key" class="absolute bottom-0 left-1/2 -translate-x-1/2 w-16 h-0.5 bg-emerald-600 rounded-full"></div>
        </button>
      </div>
    </div>

    <!-- 发布采购区域 - 双栏布局 -->
    <div v-show="activeTab === 'publish'" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧表单区域（2/3） -->
      <div class="lg:col-span-2">
        <div class="bg-white rounded-xl shadow-card border border-gray-100 overflow-hidden">
          <div class="p-6 border-b border-gray-100">
            <h3 class="text-lg font-semibold text-gray-800">发布采购需求</h3>
            <p class="text-gray-500 text-sm mt-1">填写以下信息发布您的采购需求</p>
          </div>
          
          <el-form :model="publishForm" label-width="120px" label-position="left" class="p-6">
            <!-- 基本信息卡片 -->
            <div class="space-y-4 mb-6">
              <h4 class="text-md font-medium text-gray-700 flex items-center">
                <el-icon class="mr-2 text-primary-600"><InfoFilled /></el-icon>
                基本信息
              </h4>
              
              <div class="bg-gray-50 rounded-lg p-4">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <el-form-item label="公司名称">
                    <el-input 
                      v-model="publishForm.companyName" 
                      placeholder="请输入公司名称"
                    />
                  </el-form-item>
                  
                  <el-form-item label="品类选择" required>
                    <TwoLevelCategoryPicker 
                      :categories="categoryTree" 
                      @update:modelValue="onCategoryChange"
                    />
                  </el-form-item>
                  
                  <el-form-item label="合同编号">
                    <el-input 
                      v-model="contractNo" 
                      placeholder="自动生成" 
                      disabled
                    />
                  </el-form-item>
                  
                  <el-form-item label="采购数量(吨)" required>
                    <el-input-number 
                      v-model="publishForm.quantity" 
                      :min="0" 
                      :step="1" 
                      controls-position="right" 
                      class="w-full"
                    />
                  </el-form-item>
                  
                  <el-form-item label="期望价格(元/吨)">
                    <el-input-number 
                      v-model="publishForm.expectedPrice" 
                      :min="0" 
                      :step="10" 
                      controls-position="right" 
                      class="w-full"
                    />
                  </el-form-item>
                  
                  <el-form-item label="包装方式">
                    <el-select v-model="publishForm.packaging" class="w-full">
                      <el-option label="散装" value="散装" />
                      <el-option label="袋装" value="袋装" />
                      <el-option label="箱装" value="箱装" />
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="付款方式">
                    <el-select v-model="publishForm.paymentMethod" class="w-full">
                      <el-option label="现款" value="现款" />
                      <el-option label="账期" value="账期" />
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="发票类型">
                    <el-select v-model="publishForm.invoiceType" class="w-full" clearable>
                      <el-option label="普通发票" value="普通发票" />
                      <el-option label="增值税发票" value="增值税发票" />
                      <el-option label="不需要发票" value="不需要发票" />
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="交货方式">
                    <el-select v-model="publishForm.deliveryMethod" class="w-full" clearable>
                      <el-option label="到厂" value="到厂" />
                      <el-option label="自提" value="自提" />
                      <el-option label="物流配送" value="物流配送" />
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="采购地址">
                    <el-input 
                      v-model="publishForm.purchaseAddress" 
                      placeholder="请输入采购地址" 
                    />
                  </el-form-item>
                  
                  <el-form-item label="发布有效期">
                    <el-select v-model="publishForm.expireMinutes" class="w-full">
                      <el-option label="1小时" :value="60" />
                      <el-option label="1天" :value="1440" />
                      <el-option label="3天" :value="4320" />
                      <el-option label="7天" :value="10080" />
                      <el-option label="30天" :value="43200" />
                    </el-select>
                  </el-form-item>
                </div>
                
                <el-form-item label="备注">
                  <el-input 
                    v-model="publishForm.remark" 
                    type="textarea" 
                    :rows="3" 
                    placeholder="请输入备注信息"
                  />
                </el-form-item>
              </div>
            </div>
            
            <!-- 品类参数卡片 -->
            <div v-if="categoryParams.length > 0" class="space-y-4 mb-6">
              <h4 class="text-md font-medium text-gray-700 flex items-center">
                <el-icon class="mr-2 text-primary-600"><Operation /></el-icon>
                品类参数
              </h4>
              
              <div class="bg-gray-50 rounded-lg p-4">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div v-for="param in categoryParams" :key="param.id">
                    <el-form-item :label="param.paramName">
                      <!-- 下拉选择 -->
                      <el-select
                        v-if="param.paramType === 1"
                        v-model="dynamicParams[param.id]"
                        :placeholder="`请选择${param.paramName}`"
                        class="w-full"
                        allow-create
                        filterable
                        @visible-change="(visible: boolean) => {
                          if (!visible && dynamicParams[param.id] && !param.options?.includes(dynamicParams[param.id])) {
                            addParamOption(param.id, dynamicParams[param.id])
                          }
                        }"
                      >
                        <el-option
                          v-for="option in param.options"
                          :key="option"
                          :label="option"
                          :value="option"
                        />
                      </el-select>
                      
                      <!-- 文本输入 -->
                      <el-input
                        v-else
                        v-model="dynamicParams[param.id]"
                        :placeholder="`请输入${param.paramName}`"
                        class="w-full"
                      />
                    </el-form-item>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="flex justify-end space-x-4 pt-4 border-t border-gray-100">
              <el-button 
                @click="saveAsTemplate"
              >
                保存为模板
              </el-button>
              <el-button 
                type="primary" 
                :loading="loading" 
                @click="publishRequirement"
                size="large"
              >
                发布采购需求
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
      
      <!-- 右侧预览区域（1/3，sticky） -->
      <div class="lg:col-span-1">
        <div class="bg-white rounded-xl shadow-card border border-gray-100 overflow-hidden sticky top-6">
          <div class="p-6 border-b border-gray-100">
            <h3 class="text-lg font-semibold text-gray-800">需求信息预览</h3>
            <p class="text-gray-500 text-sm mt-1">发布的需求将显示在这里</p>
          </div>
          
          <div class="p-6 max-h-[70vh] overflow-y-auto">
            <div v-if="!publishForm.categoryName" class="text-center py-12">
              <el-icon class="text-gray-300 text-5xl mb-4"><Document /></el-icon>
              <p class="text-gray-500">填写左侧表单查看预览</p>
            </div>
            <div v-else class="space-y-4">
              <div class="text-center mb-6">
                <h2 class="text-xl font-bold text-gray-800">采购需求信息</h2>
                <p class="text-gray-500 text-sm mt-1">合同编号: {{ previewData.contractNo }}</p>
              </div>
              
              <div class="space-y-3">
                <div class="grid grid-cols-2 gap-3">
                  <div>
                    <p class="text-xs text-gray-500 mb-1">公司名称</p>
                    <p class="text-sm font-medium text-gray-800">{{ previewData.companyName }}</p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 mb-1">产品名称</p>
                    <p class="text-sm font-medium text-gray-800">{{ previewData.categoryName }}</p>
                  </div>
                </div>
                
                <div class="grid grid-cols-2 gap-3">
                  <div>
                    <p class="text-xs text-gray-500 mb-1">采购数量</p>
                    <p class="text-sm font-medium text-gray-800">{{ previewData.quantity }} 吨</p>
                  </div>
                </div>
                
                <div class="grid grid-cols-2 gap-3">
                  <div>
                    <p class="text-xs text-gray-500 mb-1">包装方式</p>
                    <p class="text-sm font-medium text-gray-800">{{ previewData.packaging }}</p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 mb-1">付款方式</p>
                    <p class="text-sm font-medium text-gray-800">{{ previewData.paymentMethod }}</p>
                  </div>
                </div>
                
                <div v-if="previewData.expectedPrice" class="grid grid-cols-2 gap-3">
                  <div>
                    <p class="text-xs text-gray-500 mb-1">期望价格</p>
                    <p class="text-sm font-medium text-primary-600">¥{{ previewData.expectedPrice }}/吨</p>
                  </div>
                </div>
                
                <div class="grid grid-cols-2 gap-3">
                  <div>
                    <p class="text-xs text-gray-500 mb-1">发票类型</p>
                    <p class="text-sm font-medium text-gray-800">{{ previewData.invoiceType }}</p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 mb-1">交货方式</p>
                    <p class="text-sm font-medium text-gray-800">{{ previewData.deliveryMethod }}</p>
                  </div>
                </div>
                
                <div class="grid grid-cols-2 gap-3">
                  <div>
                    <p class="text-xs text-gray-500 mb-1">采购地址</p>
                    <p class="text-sm font-medium text-gray-800">{{ previewData.purchaseAddress }}</p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 mb-1">发布有效期</p>
                    <p class="text-sm font-medium text-gray-800">{{ previewData.expireText }}</p>
                  </div>
                </div>
                
                <div v-if="previewData.paramsText !== '无'" class="pt-3 border-t border-gray-100">
                  <p class="text-xs text-gray-500 mb-2">品类参数</p>
                  <p class="text-sm text-gray-700 whitespace-pre-wrap">{{ previewData.paramsText }}</p>
                </div>
                
                <div v-if="previewData.remark" class="pt-3 border-t border-gray-100">
                  <p class="text-xs text-gray-500 mb-1">备注</p>
                  <p class="text-sm text-gray-700">{{ previewData.remark }}</p>
                </div>
              </div>
              
              <div class="mt-6 pt-4 border-t border-gray-100">
                <div class="flex justify-between text-sm">
                  <div>
                    <p class="text-gray-500">发布时间</p>
                    <p class="font-medium mt-1">{{ new Date().toLocaleDateString() }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 需求模板区域 -->
    <div v-show="activeTab === 'templates'" class="bg-white rounded-xl shadow-card border border-gray-100 overflow-hidden">
      <div class="p-6 border-b border-gray-100">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-semibold text-gray-800">采购模板</h3>
            <p class="text-gray-500 text-sm mt-1">管理您的采购需求模板，快速填充表单</p>
          </div>
          <el-button type="primary" @click="activeTab = 'publish'">
            <el-icon class="mr-1"><svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" /></svg></el-icon>
            创建新模板
          </el-button>
        </div>
      </div>
      <div class="p-6">
        <div v-if="templates.length === 0" class="text-center py-12 text-gray-500">
          暂无模板，可在发布表单中点击"保存为模板"创建模板
        </div>
        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div 
            v-for="template in templates" 
            :key="template.id" 
            class="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-all cursor-pointer bg-white"
            @click="applyTemplate(template)"
          >
            <div class="flex justify-between items-start mb-2">
              <div class="font-bold text-gray-800">{{ template.templateName }}</div>
              <el-button 
                type="danger" 
                size="small" 
                circle 
                @click.stop="deleteTemplate(template.id)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <div class="text-sm text-gray-600 mb-1">{{ (() => { try { return JSON.parse(template.templateJson).categoryName || '未分类' } catch { return '未分类' } })() }}</div>
            <div class="text-sm text-gray-500">{{ (() => { try { const d = JSON.parse(template.templateJson); return `${d.quantity || 0} 吨 · ${d.packaging || '散装'}` } catch { return '0 吨 · 散装' } })() }}</div>
            <div class="text-xs text-gray-400 mt-2">
              {{ template.createTime ? new Date(template.createTime).toLocaleDateString() : '' }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 采购列表区域 -->
    <div v-show="activeTab === 'list'" class="bg-white rounded-xl shadow-card border border-gray-100 overflow-hidden">
      <div class="p-6 border-b border-gray-100">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-semibold text-gray-800">已发布的采购需求</h3>
            <p class="text-gray-500 text-sm mt-1">管理您已发布的所有采购需求</p>
          </div>
          <div class="flex items-center gap-3">
            <el-tag type="info" effect="light" size="large" class="!rounded-lg">
              共 {{ pagination.total }} 条
            </el-tag>
            <el-button type="primary" @click="activeTab = 'publish'">
              <el-icon class="mr-1"><Plus /></el-icon>
              发布新需求
            </el-button>
          </div>
        </div>
      </div>
      <div class="p-6">
        <!-- 筛选条件 -->
        <div class="flex flex-wrap gap-4 mb-6 p-4 bg-gray-50 rounded-lg">
            <div class="flex flex-wrap gap-4">
              <div class="flex items-center gap-2">
                <span class="text-gray-600">品类:</span>
                <el-input 
                  v-model="filters.categoryName" 
                  placeholder="搜索品类" 
                  class="w-32"
                  @keyup.enter="handleFilter"
                />
              </div>
              <div class="flex items-center gap-2">
                <span class="text-gray-600">状态:</span>
                <el-select v-model="filters.status" class="w-32" clearable>
                  <el-option label="全部" :value="undefined" />
                  <el-option label="发布中" :value="0" />
                  <el-option label="部分成交" :value="1" />
                  <el-option label="已下架" :value="2" />
                  <el-option label="全部成交" :value="3" />
                </el-select>
              </div>
              <el-button type="primary" @click="handleFilter">搜索</el-button>
            </div>
        </div>
        
        <!-- 需求列表 -->
        <div v-if="requirements.length === 0" class="text-center py-12 text-gray-500">
          暂无采购需求
        </div>
            <div v-else class="space-y-4">
              <div 
                v-for="req in requirements" 
                :key="req.id" 
                class="border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all bg-white"
              >
                <div class="flex justify-between items-start mb-3">
                  <div>
                    <div class="flex items-center gap-2 mb-2">
                      <h3 class="text-lg font-bold text-gray-800">{{ req.categoryName }}</h3>
                      <el-tag :type="getStatusType(req.status)" size="small">
                        {{ getStatusText(req.status) }}
                      </el-tag>
                    </div>
                    <div class="text-gray-600">
                      数量: {{ req.quantity }} 吨 | 
                      包装: {{ req.packaging }} | 
                      付款: {{ req.paymentMethod }}
                    </div>
                  </div>
                  <div class="text-right">
                    <div class="text-sm text-gray-500">
                      {{ req.createTime ? new Date(req.createTime).toLocaleDateString() : '' }}
                    </div>
                    <div v-if="req.remainingQuantity !== undefined" class="text-sm text-gray-500">
                      剩余: {{ req.remainingQuantity }} 吨
                    </div>
                  </div>
                </div>
                
                <div v-if="req.paramsJson" class="mb-3">
                  <div class="text-sm text-gray-600">
                    参数: {{ JSON.parse(req.paramsJson)?.custom ? Object.values(JSON.parse(req.paramsJson).custom).join(', ') : '无特殊要求' }}
                  </div>
                </div>
                
                <div class="flex items-center justify-between text-sm text-gray-500">
                  <div>
                    采购地: {{ req.purchaseAddress || '未指定' }}
                  </div>
                  <div>
                    有效期至: {{ req.expireTime ? new Date(req.expireTime).toLocaleDateString() : '长期有效' }}
                  </div>
                </div>
              </div>
            </div>
          
        <!-- 分页 -->
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

    <!-- 保存为模板对话框 -->
    <el-dialog v-model="saveTemplateDialogVisible" title="保存为模板" width="400px">
      <el-form label-width="100px">
        <el-form-item label="模板名称" required>
          <el-input 
            v-model="templateNameInput" 
            placeholder="请输入模板名称"
            @keyup.enter="confirmSaveTemplate"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="saveTemplateDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmSaveTemplate">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>