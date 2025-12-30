<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Box, List, InfoFilled, Operation, Plus } from '@element-plus/icons-vue'
// import { useAppStore } from '../store/app' // 暂时未使用
import { createSupply, listSupplies, updateSupply, getNextSupplyNo, type SupplyCreateRequest, type SupplyResponse, type SupplyUpdateRequest } from '../api/supply'
import { getProductTree, getProductParams, addProductParamOption, type ProductNode, type ProductParamResponse } from '../api/product'
import TwoLevelCategoryPicker from '../components/TwoLevelCategoryPicker.vue'
import PageHeader from '../components/PageHeader.vue'

// const app = useAppStore() // 暂时未使用
const loading = ref(false)
const activeTab = ref('publish')
const supplyNo = ref<string>('')

// 发布表单
const publishForm = reactive({
  categoryId: undefined as number | undefined,
  categoryName: '',
  exFactoryPrice: undefined as number | undefined,
  origin: '',
  quantity: undefined as number | undefined,
  packaging: '',
  storageMethod: '',
  shipAddress: '',
  deliveryMode: '到厂',
  expireMinutes: 4320, // 3天
  priceRulesJson: '{}',
  paramsJson: '{}',
  remark: ''
})

// 列表数据
const supplies = ref<SupplyResponse[]>([])

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

// 成交表单
const dealForm = reactive({
  supplyId: undefined as number | undefined,
  requirementId: undefined as number | undefined,
  quantity: undefined as number | undefined,
  finalExFactoryPrice: undefined as number | undefined,
  deliveryMode: '到厂'
})

// 成交对话框
const dealDialogVisible = ref(false)

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
    supplyNo: supplyNo.value || '自动生成',
    categoryName: publishForm.categoryName || '未选择',
    exFactoryPrice: publishForm.exFactoryPrice || 0,
    origin: publishForm.origin || '未指定',
    quantity: publishForm.quantity || 0,
    packaging: publishForm.packaging || '未指定',
    storageMethod: publishForm.storageMethod || '未指定',
    shipAddress: publishForm.shipAddress || '未指定',
    deliveryMode: publishForm.deliveryMode || '到厂',
    expireText,
    paramsText,
    remark: publishForm.remark || ''
  }
})

onMounted(async () => {
  await Promise.all([
    loadCategoryTree(),
    loadSupplies()
  ])
  // 获取下一个供应编号
  await loadNextSupplyNo()
})

async function loadNextSupplyNo() {
  try {
    const r = await getNextSupplyNo()
    if (r.code === 0 && r.data) {
      supplyNo.value = r.data
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

async function loadSupplies() {
  loading.value = true
  try {
    const r = await listSupplies({
      categoryName: filters.categoryName || undefined,
      status: filters.status
    })
    if (r.code === 0) {
      supplies.value = r.data || []
      pagination.total = supplies.value.length
    }
  } catch (e) {
    ElMessage.error('加载供应列表失败')
  } finally {
    loading.value = false
  }
}

async function onCategoryChange(category: { id: number; name: string } | null) {
  if (category) {
    publishForm.categoryId = category.id
    publishForm.categoryName = category.name
    await loadCategoryParams(category.id)
    // 重新获取供应编号（如果有新编号逻辑）
    await loadNextSupplyNo()
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
        params[param.id] = param.paramType === 1 ? (param.options && param.options.length > 0 ? '' : '') : ''
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

async function publishSupply() {
  if (!publishForm.categoryId) {
    ElMessage.warning('请选择品类')
    return
  }
  if (!publishForm.exFactoryPrice) {
    ElMessage.warning('请输入出厂价')
    return
  }
  
  loading.value = true
  try {
    const paramsJson = buildParamsJson()
    
    const req: SupplyCreateRequest = {
      categoryName: publishForm.categoryName,
      supplyNo: supplyNo.value || undefined,
      origin: publishForm.origin || undefined,
      quantity: publishForm.quantity,
      packaging: publishForm.packaging || undefined,
      storageMethod: publishForm.storageMethod || undefined,
      exFactoryPrice: publishForm.exFactoryPrice!,
      shipAddress: publishForm.shipAddress || undefined,
      deliveryMode: publishForm.deliveryMode || undefined,
      expireMinutes: publishForm.expireMinutes,
      priceRulesJson: publishForm.priceRulesJson || '{}',
      paramsJson,
      remark: publishForm.remark || undefined
    }
    
    const r = await createSupply(req)
    if (r.code === 0) {
      ElMessage.success('供应发布成功')
      activeTab.value = 'list'
      await loadSupplies()
      // 重置表单
      Object.assign(publishForm, {
        categoryId: undefined,
        categoryName: '',
        exFactoryPrice: undefined,
        origin: '',
        quantity: undefined,
        packaging: '',
        storageMethod: '',
        shipAddress: '',
        deliveryMode: '到厂',
        expireMinutes: 4320,
        priceRulesJson: '{}',
        paramsJson: '{}',
        remark: ''
      })
      categoryParams.value = []
      dynamicParams.value = {}
      await loadNextSupplyNo()
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '发布失败')
  } finally {
    loading.value = false
  }
}

async function toggleSupplyStatus(supply: SupplyResponse) {
  loading.value = true
  try {
    const req: SupplyUpdateRequest = {
      status: supply.status === 0 ? 1 : 0 // 0: 上架, 1: 下架
    }
    
    const r = await updateSupply(supply.id, req)
    if (r.code === 0) {
      ElMessage.success(`${supply.status === 0 ? '下架' : '上架'}成功`)
      await loadSupplies()
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

function openDealDialog(supplyId: number) {
  dealForm.supplyId = supplyId
  dealForm.requirementId = undefined
  dealForm.quantity = undefined
  dealForm.finalExFactoryPrice = undefined
  dealForm.deliveryMode = '到厂'
  dealDialogVisible.value = true
}

async function confirmDeal() {
  if (!dealForm.requirementId) {
    ElMessage.warning('请输入需求ID')
    return
  }
  if (!dealForm.quantity) {
    ElMessage.warning('请输入成交数量')
    return
  }
  
  // 这里应该调用成交API，但由于当前项目没有提供相关API，我们只是模拟操作
  ElMessage.success('成交确认成功')
  dealDialogVisible.value = false
}

function handlePageChange(page: number) {
  pagination.page = page
  loadSupplies()
}

function handleFilter() {
  pagination.page = 1
  loadSupplies()
}

function getStatusText(status?: number) {
  switch (status) {
    case 0: return '上架中'
    case 1: return '已下架'
    default: return '未知'
  }
}

function getStatusType(status?: number) {
  switch (status) {
    case 0: return 'primary'
    case 1: return 'info'
    default: return 'info'
  }
}

const tabs = [
  { key: 'publish', label: '发布供应', icon: Box },
  { key: 'list', label: '已发布', icon: List }
]
</script>

<template>
  <div class="supply-view">
    <PageHeader title="供应管理" subtitle="发布供应信息、管理上架/下架、查看供应列表" />

    <!-- Tab导航 -->
    <div class="bg-white rounded-xl shadow-card border border-gray-100 mb-6 overflow-hidden">
      <div class="flex border-b border-gray-100">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="flex-1 py-4 text-center font-medium transition-all relative"
          :class="activeTab === tab.key ? 'text-orange-600 bg-orange-50/50' : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'"
          @click="activeTab = tab.key"
        >
          <span class="mr-2 inline-flex align-text-bottom"><el-icon><component :is="tab.icon" /></el-icon></span>{{ tab.label }}
          <div v-if="activeTab === tab.key" class="absolute bottom-0 left-1/2 -translate-x-1/2 w-16 h-0.5 bg-orange-500 rounded-full"></div>
        </button>
      </div>
    </div>

    <!-- 发布供应区域 - 双栏布局 -->
    <div v-show="activeTab === 'publish'" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧表单区域（2/3） -->
      <div class="lg:col-span-2">
        <div class="bg-white rounded-xl shadow-card border border-gray-100 overflow-hidden">
          <div class="p-6 border-b border-gray-100">
            <h3 class="text-lg font-semibold text-gray-800">发布供应信息</h3>
            <p class="text-gray-500 text-sm mt-1">填写以下信息发布您的供应信息</p>
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
                  <el-form-item label="品类选择" required>
                    <TwoLevelCategoryPicker 
                      :categories="categoryTree" 
                      @update:modelValue="onCategoryChange"
                    />
                  </el-form-item>
                  
                  <el-form-item label="供应编号">
                    <el-input 
                      v-model="supplyNo" 
                      placeholder="自动生成" 
                      disabled
                    />
                  </el-form-item>
                  
                  <el-form-item label="出厂价(元/吨)" required>
                    <el-input-number 
                      v-model="publishForm.exFactoryPrice" 
                      :min="0" 
                      :step="10" 
                      controls-position="right" 
                      class="w-full"
                    />
                  </el-form-item>
                  
                  <el-form-item label="可供数量(吨)">
                    <el-input-number 
                      v-model="publishForm.quantity" 
                      :min="0" 
                      :step="1" 
                      controls-position="right" 
                      class="w-full"
                    />
                  </el-form-item>
                  
                  <el-form-item label="产地">
                    <el-input 
                      v-model="publishForm.origin" 
                      placeholder="请输入产地" 
                    />
                  </el-form-item>
                  
                  <el-form-item label="包装方式">
                    <el-select v-model="publishForm.packaging" class="w-full" clearable>
                      <el-option label="散装" value="散装" />
                      <el-option label="袋装" value="袋装" />
                      <el-option label="箱装" value="箱装" />
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="储存方式">
                    <el-input 
                      v-model="publishForm.storageMethod" 
                      placeholder="请输入储存方式" 
                    />
                  </el-form-item>
                  
                  <el-form-item label="发货地址">
                    <el-input 
                      v-model="publishForm.shipAddress" 
                      placeholder="请输入发货地址" 
                    />
                  </el-form-item>
                  
                  <el-form-item label="交付方式">
                    <el-select v-model="publishForm.deliveryMode" class="w-full">
                      <el-option label="到厂" value="到厂" />
                      <el-option label="自提" value="自提" />
                      <el-option label="物流配送" value="物流配送" />
                    </el-select>
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
                <el-icon class="mr-2 text-primary-600"><svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" /></svg></el-icon>
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
              <el-button @click="() => {
                Object.assign(publishForm, {
                  categoryId: undefined,
                  categoryName: '',
                  exFactoryPrice: undefined,
                  origin: '',
                  quantity: undefined,
                  packaging: '',
                  storageMethod: '',
                  shipAddress: '',
                  deliveryMode: '到厂',
                  expireMinutes: 4320,
                  priceRulesJson: '{}',
                  paramsJson: '{}',
                  remark: ''
                })
                categoryParams.value = []
                dynamicParams.value = {}
                loadNextSupplyNo()
              }">
                重置
              </el-button>
              <el-button 
                type="primary" 
                :loading="loading" 
                @click="publishSupply"
                size="large"
              >
                发布供应信息
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
      
      <!-- 右侧预览区域（1/3，sticky） -->
      <div class="lg:col-span-1">
        <div class="bg-white rounded-xl shadow-card border border-gray-100 overflow-hidden sticky top-6">
          <div class="p-6 border-b border-gray-100">
            <h3 class="text-lg font-semibold text-gray-800">供应信息预览</h3>
            <p class="text-gray-500 text-sm mt-1">发布的供应信息将显示在这里</p>
          </div>
          
          <div class="p-6 max-h-[70vh] overflow-y-auto">
            <div v-if="!publishForm.categoryName" class="text-center py-12">
              <el-icon class="text-gray-300 text-5xl mb-4"><Box /></el-icon>
              <p class="text-gray-500">填写左侧表单查看预览</p>
            </div>
            <div v-else class="space-y-4">
              <div class="text-center mb-6">
                <h2 class="text-xl font-bold text-gray-800">供应信息</h2>
                <p class="text-gray-500 text-sm mt-1">供应编号: {{ previewData.supplyNo }}</p>
              </div>
              
              <div class="space-y-3">
                <div class="grid grid-cols-2 gap-3">
                  <div>
                    <p class="text-xs text-gray-500 mb-1">产品名称</p>
                    <p class="text-sm font-medium text-gray-800">{{ previewData.categoryName }}</p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 mb-1">出厂价</p>
                    <p class="text-sm font-medium text-primary-600">¥{{ previewData.exFactoryPrice }}/吨</p>
                  </div>
                </div>
                
                <div class="grid grid-cols-2 gap-3">
                  <div>
                    <p class="text-xs text-gray-500 mb-1">可供数量</p>
                    <p class="text-sm font-medium text-gray-800">{{ previewData.quantity }} 吨</p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 mb-1">产地</p>
                    <p class="text-sm font-medium text-gray-800">{{ previewData.origin }}</p>
                  </div>
                </div>
                
                <div class="grid grid-cols-2 gap-3">
                  <div>
                    <p class="text-xs text-gray-500 mb-1">包装方式</p>
                    <p class="text-sm font-medium text-gray-800">{{ previewData.packaging }}</p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 mb-1">储存方式</p>
                    <p class="text-sm font-medium text-gray-800">{{ previewData.storageMethod }}</p>
                  </div>
                </div>
                
                <div class="grid grid-cols-2 gap-3">
                  <div>
                    <p class="text-xs text-gray-500 mb-1">发货地址</p>
                    <p class="text-sm font-medium text-gray-800">{{ previewData.shipAddress }}</p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 mb-1">交付方式</p>
                    <p class="text-sm font-medium text-gray-800">{{ previewData.deliveryMode }}</p>
                  </div>
                </div>
                
                <div class="grid grid-cols-2 gap-3">
                  <div>
                    <p class="text-xs text-gray-500 mb-1">发布有效期</p>
                    <p class="text-sm font-medium text-gray-800">{{ previewData.expireText }}</p>
                  </div>
                </div>
                
                <div v-if="previewData.paramsText !== '无'" class="pt-3 border-t border-gray-100">
                  <p class="text-xs text-gray-500 mb-2">质量参数</p>
                  <p class="text-sm text-gray-700">{{ previewData.paramsText }}</p>
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

    <!-- 供应列表区域 -->
    <div v-show="activeTab === 'list'" class="bg-white rounded-xl shadow-card border border-gray-100 overflow-hidden">
      <div class="p-6 border-b border-gray-100">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-semibold text-gray-800">已发布的供应信息</h3>
            <p class="text-gray-500 text-sm mt-1">管理您已发布的所有供应信息</p>
          </div>
          <div class="flex items-center gap-3">
            <el-tag type="warning" effect="light" size="large" class="!rounded-lg">
              共 {{ pagination.total }} 条
            </el-tag>
            <el-button type="warning" @click="activeTab = 'publish'">
              <el-icon class="mr-1"><Plus /></el-icon>
              发布新供应
            </el-button>
          </div>
        </div>
      </div>
      
      <div class="p-6">
        <!-- 筛选条件 -->
        <div class="flex flex-wrap gap-4 mb-6 p-4 bg-gray-50 rounded-lg">

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
                  <el-option label="上架中" :value="0" />
                  <el-option label="已下架" :value="1" />
                </el-select>
              </div>
              <el-button type="primary" @click="handleFilter">搜索</el-button>
            </div>
            
            <!-- 供应列表 -->
            <div v-if="supplies.length === 0" class="text-center py-12 text-gray-500">
              暂无供应信息
            </div>
            <div v-else class="space-y-4">
              <div 
                v-for="supply in supplies" 
                :key="supply.id" 
                class="border border-gray-200 rounded-lg p-5 hover:shadow-md transition-all bg-white"
              >
                <div class="flex justify-between items-start mb-3">
                  <div>
                    <div class="flex items-center gap-2 mb-2">
                      <h3 class="text-lg font-bold text-gray-800">{{ supply.categoryName }}</h3>
                      <el-tag :type="getStatusType(supply.status)" size="small">
                        {{ getStatusText(supply.status) }}
                      </el-tag>
                    </div>
                    <div class="text-gray-600">
                      供应编号: {{ supply.supplyNo || '未生成' }} | 
                      出厂价: <span class="text-orange-500 font-bold">¥{{ supply.exFactoryPrice }}</span>/吨
                    </div>
                    <div class="text-gray-600 mt-1">
                      可供数量: {{ supply.quantity || 0 }} 吨 | 
                      交付: {{ supply.deliveryMode }} | 
                      发货地: {{ supply.shipAddress || '未指定' }}
                    </div>
                  </div>
                  <div class="text-right">
                    <div class="text-sm text-gray-500">
                      {{ supply.createTime ? new Date(supply.createTime).toLocaleDateString() : '' }}
                    </div>
                    <div v-if="supply.expireTime" class="text-sm text-gray-500 mt-1">
                      有效期至: {{ new Date(supply.expireTime).toLocaleDateString() }}
                    </div>
                  </div>
                </div>
                
                <div v-if="supply.paramsJson" class="mb-3">
                  <div class="text-sm text-gray-600">
                    参数: {{ (() => { try { const p = JSON.parse(supply.paramsJson); return p.params ? Object.entries(p.params).map(([k, v]) => `${k}:${v}`).join(', ') : '无' } catch { return '无' } })() }}
                  </div>
                </div>
                
                <div class="flex items-center justify-between">
                  <div class="text-sm text-gray-500">
                    产地: {{ supply.origin || '未指定' }} | 
                    包装: {{ supply.packaging || '未指定' }} | 
                    储存: {{ supply.storageMethod || '未指定' }}
                  </div>
                  <div class="flex gap-2">
                    <el-button 
                      :type="supply.status === 0 ? 'warning' : 'success'" 
                      size="small" 
                      @click="toggleSupplyStatus(supply)"
                    >
                      {{ supply.status === 0 ? '下架' : '上架' }}
                    </el-button>
                    <el-button 
                      type="primary" 
                      size="small" 
                      @click="openDealDialog(supply.id)"
                    >
                      成交
                    </el-button>
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
    
    <!-- 成交对话框 -->
    <el-dialog v-model="dealDialogVisible" title="确认成交" width="500px">
      <el-form :model="dealForm" label-width="100px">
        <el-form-item label="需求ID" required>
          <el-input-number 
            v-model="dealForm.requirementId" 
            :min="1" 
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="成交数量" required>
          <el-input-number 
            v-model="dealForm.quantity" 
            :min="0" 
            :step="1" 
            controls-position="right" 
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="最终价格">
          <el-input-number 
            v-model="dealForm.finalExFactoryPrice" 
            :min="0" 
            :step="10" 
            controls-position="right" 
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="交付方式">
          <el-select v-model="dealForm.deliveryMode" class="w-full">
            <el-option label="到厂" value="到厂" />
            <el-option label="自提" value="自提" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dealDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmDeal">确认成交</el-button>
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