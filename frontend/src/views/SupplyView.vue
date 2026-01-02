<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { Box } from 'lucide-vue-next'
import { createSupply, getNextSupplyNo, type SupplyCreateRequest } from '../api/supply'
import { getProductTree, getProductParams, addProductParamOption, type ProductNode, type ProductParamResponse } from '../api/product'
import { getMyCompany, type CompanyResponse } from '../api/company'
import { getMe, type UserResponse } from '../api/user'
import TwoLevelCategoryPicker from '../components/TwoLevelCategoryPicker.vue'
import PageHeader from '../components/PageHeader.vue'

const router = useRouter()
const loading = ref(false)
const supplyNo = ref<string>('')
const templatePickerOpen = ref(false)

// 公司与用户信息（预填）
const company = ref<CompanyResponse | null>(null)
const meUser = ref<UserResponse | null>(null)
const publisherNameInput = ref('')

// 发布表单
const publishForm = reactive({
  categoryId: undefined as number | undefined,
  categoryName: '',
  companyName: '',
  exFactoryPrice: undefined as number | undefined,
  quantity: undefined as number | undefined,
  packaging: '散装',
  shipAddress: '',
  deliveryMode: '到厂',
  expireMinutes: 4320, // 3天
  priceRulesJson: '{}',
  paramsJson: '{}',
  remark: ''
})

// 品类相关
const categoryTree = ref<ProductNode[]>([])
const categoryParams = ref<ProductParamResponse[]>([])

// 动态参数表单
const dynamicParams = ref<Record<string, any>>({})

// 品类选择器
type PickedCategory = { id: number; name: string } | null
const pickedCategory = ref<PickedCategory>(null)
const suspendCategoryWatch = ref(false)

// 模板系统
interface SupplyTemplate {
  id: number
  templateName: string
  templateJson: string
  createTime?: string
}
const templates = ref<SupplyTemplate[]>([])
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

function getTemplateJson(template: SupplyTemplate): TemplateJsonData {
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

function formatExpireMinutes(m?: number) {
  const minutes = Number(m ?? 0)
  if (!minutes) return '未设置'
  const days = Math.floor(minutes / 1440)
  const hours = Math.floor((minutes % 1440) / 60)
  if (days > 0 && hours > 0) return `${days}天${hours}小时`
  if (days > 0) return `${days}天`
  if (hours > 0) return `${hours}小时`
  return `${minutes}分钟`
}

function formatPrice(p?: number) {
  const n = Number(p)
  if (!p && p !== 0) return '面议'
  if (Number.isNaN(n)) return '面议'
  return `¥${n}/吨`
}

function templateParamsSummary(d: TemplateJsonData) {
  if (!d.paramsJson) return '无特殊指标'
  try {
    const payload = JSON.parse(d.paramsJson) as any
    const custom = payload?.custom && typeof payload.custom === 'object' ? payload.custom : null
    if (custom && Object.keys(custom).length) {
      const parts = Object.entries(custom).slice(0, 6).map(([k, v]) => `${k}:${String(v)}`)
      return parts.join('; ')
    }
    const params = payload?.params && typeof payload.params === 'object' ? payload.params : null
    const cnt = params ? Object.keys(params).length : 0
    return cnt ? `已设置 ${cnt} 项指标` : '无特殊指标'
  } catch {
    return '无特殊指标'
  }
}

function formatDate(dateStr?: string) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (Number.isNaN(d.getTime())) return ''
  return d.toLocaleDateString('zh-CN')
}

// 判断是否可以发布
const canPublish = computed(() => {
  return !!(
    publishForm.categoryId &&
    publishForm.exFactoryPrice &&
    publishForm.exFactoryPrice > 0
  )
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
  await Promise.all([
    loadCategoryTree(),
    loadCompanyInfo(),
    loadMeUser(),
    loadTemplates()
  ])
  await loadNextSupplyNo()
})

async function loadCompanyInfo() {
  try {
    const r = await getMyCompany()
    if (r.code === 0 && r.data) {
      company.value = r.data
      publishForm.companyName = r.data.companyName || ''
      // 拼接完整地址：省 + 市 + 区 + 详细地址
      const fullAddress = [
        r.data.province,
        r.data.city,
        r.data.district,
        r.data.address
      ].filter(Boolean).join('')
      publishForm.shipAddress = fullAddress || ''
    }
  } catch (e) {
    // 静默失败
  }
}

async function loadMeUser() {
  try {
    const r = await getMe()
    if (r.code === 0) meUser.value = r.data ?? null
    if (!publisherNameInput.value) {
      publisherNameInput.value = publisherName.value
    }
  } catch {
    // 静默失败
  }
}

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

async function loadTemplates() {
  // TODO: 调用后端 API 获取模板列表
  templates.value = []
}

// 监听品类选择器变化
watch(
  pickedCategory,
  async (category) => {
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
  },
  { deep: true }
)

async function loadCategoryParams(productId: number) {
  try {
    const r = await getProductParams(productId)
    if (r.code === 0) {
      categoryParams.value = r.data || []
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
      if (publishForm.categoryId) {
        await loadCategoryParams(publishForm.categoryId)
      }
    }
  } catch (e) {
    ElMessage.error('添加选项失败')
  }
}

function buildParamsJson() {
  const params: Record<string, { name: string; value: any }> = {}
  const custom: Record<string, any> = {}
  
  categoryParams.value.forEach(param => {
    const value = dynamicParams.value[param.id]
    if (value !== undefined && value !== '') {
      params[param.id] = { name: param.paramName, value }
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
      quantity: publishForm.quantity,
      packaging: publishForm.packaging || undefined,
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
      router.push('/supply/published')
      // 重置表单（保留公司名称和发货地址的默认值）
      Object.assign(publishForm, {
        companyName: company.value?.companyName || '',
        categoryId: undefined,
        categoryName: '',
        exFactoryPrice: undefined,
        quantity: undefined,
        packaging: '散装',
        shipAddress: company.value?.address || '',
        deliveryMode: '到厂',
        expireMinutes: 4320,
        priceRulesJson: '{}',
        paramsJson: '{}',
        remark: ''
      })
      publisherNameInput.value = publisherName.value
      pickedCategory.value = null
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

// 保存为模板
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
    
    // TODO: 调用后端 API 保存模板
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
    
    templates.value.push({
      id: Date.now(),
      templateName: templateNameInput.value.trim(),
      templateJson,
      createTime: new Date().toISOString()
    })
    
    ElMessage.success('模板保存成功')
    saveTemplateDialogVisible.value = false
    templateNameInput.value = ''
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    loading.value = false
  }
}

async function deleteTemplate(id: number) {
  try {
    // TODO: 调用后端 API 删除模板
    templates.value = templates.value.filter(t => t.id !== id)
    ElMessage.success('模板删除成功')
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

async function applyTemplate(template: SupplyTemplate) {
  try {
    const data = JSON.parse(template.templateJson)
    // 填充到发布表单
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
    
    // 加载参数
    if (data.categoryId) {
      await loadCategoryParams(data.categoryId)
    } else {
      categoryParams.value = []
      dynamicParams.value = {}
    }

    // loadCategoryParams 会初始化 dynamicParams，这里必须在其后回填模板参数
    if (data.paramsJson) {
      try {
        const paramsData = JSON.parse(data.paramsJson)
        if (paramsData.params) {
          Object.entries(paramsData.params).forEach(([paramId, value]) => {
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
  <div class="bg-gray-50 text-gray-900 min-h-screen">
    <div class="max-w-7xl mx-auto p-4 md:p-6 space-y-6">
      <PageHeader title="发布供应">
        <template #right>
          <el-button class="!rounded-xl transition-all active:scale-95" @click="templatePickerOpen = true">
            选择模板
          </el-button>
          <el-button class="!rounded-xl transition-all active:scale-95" @click="saveAsTemplate">
            保存为模板
          </el-button>
          <el-button class="!rounded-xl transition-all active:scale-95" @click="router.push('/supply/published')">
            已发布
          </el-button>
          <el-button
            type="primary"
            class="!rounded-xl !bg-emerald-600 hover:!bg-emerald-700 !border-emerald-600 transition-all active:scale-95"
            :disabled="!canPublish"
            :loading="loading"
            @click="publishSupply"
          >
            发布
          </el-button>
        </template>
      </PageHeader>

      <!-- 发布供应区域 - 双栏布局 -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 左侧表单区域（2/3） -->
        <div class="lg:col-span-2">
          <el-form :model="publishForm" label-position="top" class="neo-form space-y-6">
            <!-- 发布信息 -->
            <section class="bg-white p-6 md:p-8 rounded-[32px] border border-gray-100 shadow-sm space-y-5">
              <div class="flex items-center gap-2 border-b pb-4">
                <div class="w-1.5 h-6 bg-slate-900 rounded-full"></div>
                <h2 class="font-bold text-lg text-gray-800">发布信息</h2>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div class="bg-slate-50 rounded-2xl border border-gray-100 p-4">
                  <div class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-2">公司（仅本次）</div>
                  <el-input v-model="publishForm.companyName" placeholder="默认使用公司名称，可临时修改" />
                </div>
                <div class="bg-slate-50 rounded-2xl border border-gray-100 p-4">
                  <div class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-2">发布人（仅本次）</div>
                  <el-input v-model="publisherNameInput" placeholder="默认使用个人信息，可临时修改" />
                </div>
                <div class="bg-slate-50 rounded-2xl border border-gray-100 p-4 md:col-span-2">
                  <div class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-2">发货地址（默认公司地址，可修改）</div>
                  <el-input v-model="publishForm.shipAddress" placeholder="请输入发货地址" />
                </div>
                <div class="md:col-span-2 text-xs text-gray-400">
                  说明：以上信息仅用于本次发布与模板复用，不会修改您的公司/个人资料。
                </div>
              </div>
            </section>

            <!-- 基础信息 -->
            <section class="bg-white p-6 md:p-8 rounded-[32px] border border-gray-100 shadow-sm space-y-5">
              <div class="flex items-center gap-2 border-b pb-4">
                <div class="w-1.5 h-6 bg-emerald-600 rounded-full"></div>
                <h2 class="font-bold text-lg text-gray-800">基础信息</h2>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <el-form-item label="品类选择" required>
                  <TwoLevelCategoryPicker v-model="pickedCategory" />
                </el-form-item>

                <el-form-item label="可供数量(吨)">
                  <el-input-number v-model="publishForm.quantity" :min="0" :step="1" :controls="false" />
                </el-form-item>

                <el-form-item label="出厂价(元/吨)" required>
                  <el-input-number v-model="publishForm.exFactoryPrice" :min="0" :step="10" :controls="false" />
                </el-form-item>
              </div>
            </section>

            <!-- 规格参数 -->
            <section class="bg-white p-6 md:p-8 rounded-[32px] border border-gray-100 shadow-sm space-y-5">
              <div class="flex items-center justify-between gap-4 border-b pb-4">
                <div class="flex items-center gap-2">
                  <div class="w-1.5 h-6 bg-emerald-500 rounded-full"></div>
                  <h2 class="font-bold text-lg text-gray-800">规格参数</h2>
                </div>
                <span class="text-[10px] text-gray-400 font-medium">支持自定义添加指标</span>
              </div>

              <div v-if="categoryParams.length === 0" class="text-sm text-gray-500 bg-slate-50 border border-gray-100 rounded-2xl p-4">
                选择品类后，会自动加载对应的指标参数
              </div>

              <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div v-for="param in categoryParams" :key="param.id">
                  <el-form-item :label="param.paramName">
                    <el-select
                      v-if="param.paramType === 1"
                      v-model="dynamicParams[param.id]"
                      :placeholder="`请选择${param.paramName}`"
                      allow-create
                      filterable
                      @visible-change="(visible: boolean) => {
                        if (!visible && dynamicParams[param.id] && !param.options?.includes(dynamicParams[param.id])) {
                          addParamOption(param.id, dynamicParams[param.id])
                        }
                      }"
                    >
                      <el-option v-for="option in param.options" :key="option" :label="option" :value="option" />
                    </el-select>
                    <el-input v-else v-model="dynamicParams[param.id]" :placeholder="`请输入${param.paramName}`" />
                  </el-form-item>
                </div>
              </div>
            </section>

            <!-- 物流与交付 -->
            <section class="bg-white p-6 md:p-8 rounded-[32px] border border-gray-100 shadow-sm space-y-5">
              <div class="flex items-center gap-2 border-b pb-4">
                <div class="w-1.5 h-6 bg-amber-500 rounded-full"></div>
                <h2 class="font-bold text-lg text-gray-800">物流与交付</h2>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <el-form-item label="发布有效期">
                  <el-select v-model="publishForm.expireMinutes" clearable>
                    <el-option label="1小时" :value="60" />
                    <el-option label="1天" :value="1440" />
                    <el-option label="3天" :value="4320" />
                    <el-option label="7天" :value="10080" />
                    <el-option label="30天" :value="43200" />
                  </el-select>
                </el-form-item>

                <el-form-item label="包装方式">
                  <el-select v-model="publishForm.packaging">
                    <el-option label="散装" value="散装" />
                    <el-option label="袋装" value="袋装" />
                    <el-option label="箱装" value="箱装" />
                  </el-select>
                </el-form-item>

                <el-form-item label="交付方式">
                  <el-select v-model="publishForm.deliveryMode">
                    <el-option label="到厂" value="到厂" />
                    <el-option label="自提" value="自提" />
                    <el-option label="物流配送" value="物流配送" />
                  </el-select>
                </el-form-item>

                <el-form-item label="补充说明" class="md:col-span-2">
                  <el-input v-model="publishForm.remark" type="textarea" :rows="3" placeholder="备注（可选）" />
                </el-form-item>
              </div>

              <div class="flex justify-end gap-3 pt-2">
                <el-button class="!rounded-xl transition-all active:scale-95" @click="saveAsTemplate">保存为模板</el-button>
                <el-button
                  type="primary"
                  class="!rounded-xl !bg-emerald-600 hover:!bg-emerald-700 !border-emerald-600 transition-all active:scale-95"
                  :disabled="!canPublish"
                  :loading="loading"
                  @click="publishSupply"
                  size="large"
                >
                  发布供应
                </el-button>
              </div>
            </section>
          </el-form>
        </div>
        
        <!-- 右侧预览区域（1/3，sticky） -->
        <div class="lg:col-span-1">
          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden sticky top-24">
            <div class="p-6 border-b border-gray-100">
              <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">summary</div>
              <h3 class="text-lg font-bold text-gray-900 mt-1">发布前确认</h3>
              <p class="text-gray-500 text-sm mt-1">只展示关键字段，避免重复信息</p>
            </div>
            
            <div class="p-6 max-h-[70vh] overflow-y-auto">
              <div v-if="!publishForm.categoryName" class="text-center py-12 text-gray-500 text-sm">
                填写左侧信息，这里会显示关键确认项
              </div>
              <div v-else class="space-y-3">
                <div class="grid grid-cols-2 gap-3">
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">公司</div>
                    <div class="mt-1 font-bold text-gray-900 truncate">{{ publishForm.companyName || (company?.companyName || previewData.companyName) }}</div>
                  </div>
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">发布人</div>
                    <div class="mt-1 font-bold text-gray-900 truncate">{{ publisherNameInput || publisherName }}</div>
                  </div>
                </div>
                <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">品类</div>
                  <div class="mt-1 font-bold text-gray-900 truncate">{{ previewData.categoryName }}</div>
                </div>
                <div class="grid grid-cols-2 gap-3">
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">数量</div>
                    <div class="mt-1 font-bold text-gray-900">{{ previewData.quantity }} 吨</div>
                  </div>
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">出厂价</div>
                    <div class="mt-1 font-bold text-emerald-600">
                      <span v-if="previewData.exFactoryPrice != null">¥{{ previewData.exFactoryPrice }}/吨</span>
                      <span v-else class="text-gray-500">面议</span>
                    </div>
                  </div>
                </div>
                <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">发货地址</div>
                  <div class="mt-1 font-bold text-gray-900 truncate">{{ previewData.shipAddress }}</div>
                </div>
                <div class="grid grid-cols-2 gap-3">
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">交付</div>
                    <div class="mt-1 font-bold text-gray-900">{{ previewData.deliveryMode }}</div>
                  </div>
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">有效期</div>
                    <div class="mt-1 font-bold text-gray-900">{{ previewData.expireText }}</div>
                  </div>
                </div>
                <div v-if="previewData.paramsText !== '无'" class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">指标</div>
                  <div class="mt-1 text-sm text-gray-700 whitespace-pre-wrap">{{ previewData.paramsText }}</div>
                </div>
                <div v-if="previewData.remark" class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">备注</div>
                  <div class="mt-1 text-sm text-gray-700">{{ previewData.remark }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 模板选择弹窗 -->
    <el-dialog v-model="templatePickerOpen" title="选择供应模板" width="920px" align-center>
      <div v-if="templates.length === 0" class="text-center py-10 text-gray-500">
        暂无模板，可在发布表单中点击"保存为模板"创建
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="template in templates"
          :key="template.id"
          class="bg-white rounded-2xl border border-gray-100 p-5 hover:shadow-md hover:border-gray-200 transition-all active:scale-[0.99] cursor-pointer"
          @click="applyTemplate(template)"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0">
              <div class="flex items-center gap-2">
                <div class="font-bold text-gray-900 truncate">{{ template.templateName }}</div>
                <span class="text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 rounded-full border border-emerald-100 bg-emerald-50 text-emerald-700">
                  {{ getTemplateJson(template).categoryName || '未分类' }}
                </span>
              </div>
              <div class="text-xs text-gray-500 mt-1 truncate">
                <span v-if="getTemplateJson(template).companyName || getTemplateJson(template).publisherName">
                  {{ getTemplateJson(template).companyName || '—' }}
                  <span v-if="getTemplateJson(template).publisherName"> · {{ getTemplateJson(template).publisherName }}</span>
                </span>
                <span v-else>—</span>
              </div>
            </div>
            <el-button
              type="danger"
              size="small"
              circle
              class="shrink-0 !rounded-xl transition-all active:scale-95"
              @click.stop="deleteTemplate(template.id)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>

          <div class="mt-4 grid grid-cols-2 gap-3 text-sm">
            <div class="bg-gray-50 border border-gray-100 rounded-xl px-3 py-2">
              <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">数量</div>
              <div class="mt-0.5 font-bold text-gray-900">{{ getTemplateJson(template).quantity || 0 }} 吨</div>
            </div>
            <div class="bg-gray-50 border border-gray-100 rounded-xl px-3 py-2">
              <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">出厂价</div>
              <div class="mt-0.5 font-bold text-emerald-700">{{ formatPrice(getTemplateJson(template).exFactoryPrice) }}</div>
            </div>
            <div class="bg-gray-50 border border-gray-100 rounded-xl px-3 py-2 col-span-2">
              <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">发货地址</div>
              <div class="mt-0.5 font-bold text-gray-900 truncate">{{ getTemplateJson(template).shipAddress || '未设置' }}</div>
            </div>
            <div class="bg-gray-50 border border-gray-100 rounded-xl px-3 py-2 col-span-2">
              <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">有效期</div>
              <div class="mt-0.5 font-bold text-gray-900">{{ formatExpireMinutes(getTemplateJson(template).expireMinutes) }}</div>
            </div>
          </div>

          <div class="mt-3 flex flex-wrap gap-2 text-xs">
            <span class="px-2 py-1 rounded-full border border-gray-200 bg-white text-gray-700">
              <span class="text-gray-400">包装</span> {{ getTemplateJson(template).packaging || '散装' }}
            </span>
            <span class="px-2 py-1 rounded-full border border-gray-200 bg-white text-gray-700">
              <span class="text-gray-400">交付</span> {{ getTemplateJson(template).deliveryMode || '到厂' }}
            </span>
          </div>

          <div class="mt-3 text-xs text-gray-600 line-clamp-1">
            <span class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mr-2">指标</span>
            {{ templateParamsSummary(getTemplateJson(template)) }}
          </div>

          <div class="mt-3 flex items-center justify-between text-xs text-gray-400">
            <span>{{ formatDate(template.createTime) }}</span>
            <span class="text-[10px] font-bold uppercase tracking-widest text-gray-300">点击使用</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end">
          <el-button class="!rounded-xl transition-all active:scale-95" @click="templatePickerOpen = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 保存为模板对话框 -->
    <el-dialog v-model="saveTemplateDialogVisible" title="保存为模板" width="420px" class="neo-dialog">
      <div class="p-6">
        <div class="text-xs text-gray-500 mb-4">
          用于一键复用本次发布的品类、条款与指标配置
        </div>
        <el-form label-position="top" class="neo-dialog-form">
          <el-form-item label="模板名称" required>
            <el-input
              v-model="templateNameInput"
              placeholder="例如：玉米到厂-现款-常规指标"
              @keyup.enter="confirmSaveTemplate"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-3 px-6 py-4">
          <el-button class="!rounded-xl transition-all active:scale-95" @click="saveTemplateDialogVisible = false">取消</el-button>
          <el-button type="primary" class="!rounded-xl transition-all active:scale-95" @click="confirmSaveTemplate">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* label 统一成"微标签"风格（限定在 neo-form 内） */
:deep(.neo-form .el-form-item__label) {
  font-weight: 800;
  font-size: 12px;
  color: rgb(107 114 128); /* gray-500 */
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

/* 本页表单：Element Plus 输入控件统一为 Neo-Minimal 低噪风格（限定在 .neo-form 内，避免影响全站） */
:deep(.neo-form .el-input__wrapper),
:deep(.neo-form .el-select__wrapper),
:deep(.neo-form .el-textarea__inner),
:deep(.neo-form .el-cascader .el-input__wrapper) {
  border: 2px solid rgb(243 244 246); /* gray-100 */
  border-radius: 12px; /* rounded-xl */
  box-shadow: none;
  background-color: #fff;
  transition: all 0.15s ease;
}

:deep(.neo-form .el-input__wrapper.is-focus),
:deep(.neo-form .el-select__wrapper.is-focus),
:deep(.neo-form .el-cascader .el-input__wrapper.is-focus),
:deep(.neo-form .el-textarea__inner:focus) {
  border-color: rgb(16 185 129); /* emerald-500 */
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18);
}

:deep(.neo-form .el-input__wrapper:hover),
:deep(.neo-form .el-select__wrapper:hover),
:deep(.neo-form .el-cascader .el-input__wrapper:hover),
:deep(.neo-form .el-textarea__inner:hover) {
  border-color: rgb(229 231 235); /* gray-200 */
}

:deep(.neo-form .el-input-number),
:deep(.neo-form .el-select),
:deep(.neo-form .el-cascader) {
  width: 100%;
}

/* 保存为模板弹窗：Soft Glass + Card-First */
:deep(.neo-dialog) {
  border-radius: 32px;
  overflow: hidden;
  border: 1px solid rgb(243 244 246); /* gray-100 */
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
  color: rgb(17 24 39); /* gray-900 */
}

:deep(.neo-dialog .el-dialog__body) {
  padding: 0;
  background: #fff;
}

:deep(.neo-dialog .el-dialog__footer) {
  padding: 0;
  border-top: 1px solid rgb(243 244 246);
  background: rgb(249 250 251); /* gray-50 */
}

/* dialog 内输入框保持一致低噪 */
:deep(.neo-dialog-form .el-form-item__label) {
  font-weight: 800;
  font-size: 12px;
  color: rgb(107 114 128);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}
:deep(.neo-dialog-form .el-input__wrapper) {
  border: 2px solid rgb(243 244 246);
  border-radius: 12px;
  box-shadow: none;
  transition: all 0.15s ease;
}
:deep(.neo-dialog-form .el-input__wrapper.is-focus) {
  border-color: rgb(16 185 129);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18);
}
</style>
