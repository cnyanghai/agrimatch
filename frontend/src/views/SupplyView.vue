<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Trash2, FileText, Save, Send, List, RefreshCw, Package, Truck, Clock, FileCheck } from 'lucide-vue-next'
import { createSupply, getNextSupplyNo, type SupplyCreateRequest } from '../api/supply'
import { getProductTree, getProductParams, addProductParamOption, type ProductNode, type ProductParamResponse } from '../api/product'
import { getMyCompany, type CompanyResponse } from '../api/company'
import { getMe, type UserResponse } from '../api/user'
import TwoLevelCategoryPicker from '../components/TwoLevelCategoryPicker.vue'
import { BaseButton, BaseModal, EmptyState } from '../components/ui'

const router = useRouter()
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

// 品类相关
const categoryTree = ref<ProductNode[]>([])
const categoryParams = ref<ProductParamResponse[]>([])
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
  try { data = (JSON.parse(json) ?? {}) as TemplateJsonData } catch { data = {} }
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
      return Object.entries(custom).slice(0, 6).map(([k, v]) => `${k}:${String(v)}`).join('; ')
    }
    const params = payload?.params && typeof payload.params === 'object' ? payload.params : null
    const cnt = params ? Object.keys(params).length : 0
    return cnt ? `已设置 ${cnt} 项指标` : '无特殊指标'
  } catch { return '无特殊指标' }
}

function formatDate(dateStr?: string) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (Number.isNaN(d.getTime())) return ''
  return d.toLocaleDateString('zh-CN')
}

const canPublish = computed(() => !!(publishForm.categoryId && publishForm.exFactoryPrice && publishForm.exFactoryPrice > 0))

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
  await Promise.all([loadCategoryTree(), loadCompanyInfo(), loadMeUser(), loadTemplates()])
  await loadNextSupplyNo()
})

async function loadCompanyInfo() {
  try {
    const r = await getMyCompany()
    if (r.code === 0 && r.data) {
      company.value = r.data
      publishForm.companyName = r.data.companyName || ''
      const fullAddress = [r.data.province, r.data.city, r.data.district, r.data.address].filter(Boolean).join('')
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
  templates.value = []
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
  if (!publishForm.categoryId) { ElMessage.warning('请选择品类'); return }
  if (!publishForm.exFactoryPrice) { ElMessage.warning('请输入出厂价'); return }
  
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
      ElMessage.success('发布成功')
      router.push('/supply/published')
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
    templates.value = templates.value.filter(t => t.id !== id)
    ElMessage.success('模板删除成功')
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

async function applyTemplate(template: SupplyTemplate) {
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
        if (paramsData.params) {
          Object.entries(paramsData.params).forEach(([paramId, value]) => {
            dynamicParams.value[Number(paramId)] = value
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
        <BaseButton type="primary" size="sm" :disabled="!canPublish" :loading="loading" @click="publishSupply">
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
        <div class="bg-white rounded-2xl border border-gray-100 overflow-hidden animate-fade-in">
          <div class="p-5 border-b border-gray-100 flex items-center gap-2">
            <div class="w-1.5 h-5 bg-slate-900 rounded-full"></div>
            <h2 class="font-bold text-gray-900">发布信息</h2>
          </div>
          <div class="p-5 space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">公司名称</label>
                <input
                  v-model="publishForm.companyName"
                  type="text"
                  placeholder="默认使用公司名称"
                  class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all"
                />
              </div>
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">发布人</label>
                <input
                  v-model="publisherNameInput"
                  type="text"
                  placeholder="默认使用个人信息"
                  class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all"
                />
              </div>
            </div>
            <div>
              <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">发货地址</label>
              <input
                v-model="publishForm.shipAddress"
                type="text"
                placeholder="请输入发货地址"
                class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all"
              />
            </div>
            <p class="text-xs text-gray-400">以上信息仅用于本次发布，不会修改您的公司/个人资料</p>
          </div>
        </div>

        <!-- 基础信息 -->
        <div class="bg-white rounded-2xl border border-gray-100 overflow-hidden animate-fade-in" style="animation-delay: 50ms">
          <div class="p-5 border-b border-gray-100 flex items-center gap-2">
            <div class="w-1.5 h-5 bg-emerald-600 rounded-full"></div>
            <h2 class="font-bold text-gray-900">基础信息</h2>
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
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
                  出厂价（元/吨）<span class="text-red-500">*</span>
                </label>
                <el-input-number v-model="publishForm.exFactoryPrice" :min="0" :step="10" :controls="false" class="w-full neo-input-number" />
              </div>
            </div>
          </div>
        </div>

        <!-- 规格参数 -->
        <div class="bg-white rounded-2xl border border-gray-100 overflow-hidden animate-fade-in" style="animation-delay: 100ms">
          <div class="p-5 border-b border-gray-100 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <div class="w-1.5 h-5 bg-emerald-500 rounded-full"></div>
              <h2 class="font-bold text-gray-900">规格参数</h2>
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
                  class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 物流与交付 -->
        <div class="bg-white rounded-2xl border border-gray-100 overflow-hidden animate-fade-in" style="animation-delay: 150ms">
          <div class="p-5 border-b border-gray-100 flex items-center gap-2">
            <div class="w-1.5 h-5 bg-amber-500 rounded-full"></div>
            <h2 class="font-bold text-gray-900">物流与交付</h2>
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
                class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all resize-none"
              ></textarea>
            </div>
            <div class="flex justify-end gap-3 pt-2">
              <BaseButton type="secondary" @click="saveAsTemplate">
                <Save class="w-4 h-4" />
                保存为模板
              </BaseButton>
              <BaseButton type="primary" :disabled="!canPublish" :loading="loading" @click="publishSupply">
                <Send class="w-4 h-4" />
                发布供应
              </BaseButton>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧预览区域 -->
      <div class="lg:col-span-1">
        <div class="bg-white rounded-2xl border border-gray-100 overflow-hidden sticky top-24 animate-fade-in" style="animation-delay: 200ms">
          <div class="p-5 border-b border-gray-100">
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
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">发布人</div>
                  <div class="mt-1 font-bold text-gray-900 truncate text-sm">{{ publisherNameInput || publisherName }}</div>
                </div>
              </div>
              <div class="bg-emerald-50 rounded-xl p-3 border border-emerald-100">
                <div class="flex items-center gap-1.5 text-[10px] font-bold uppercase tracking-widest text-emerald-600">
                  <Package class="w-3 h-3" />
                  品类
                </div>
                <div class="mt-1 font-bold text-emerald-700">{{ previewData.categoryName }}</div>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div class="bg-gray-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">数量</div>
                  <div class="mt-1 font-bold text-gray-900">{{ previewData.quantity }} 吨</div>
                </div>
                <div class="bg-gray-50 rounded-xl p-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">出厂价</div>
                  <div class="mt-1 font-bold text-emerald-600">
                    <span v-if="previewData.exFactoryPrice != null">¥{{ previewData.exFactoryPrice }}/吨</span>
                    <span v-else class="text-gray-500">面议</span>
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
          class="bg-white rounded-xl border border-gray-100 p-4 hover:shadow-md hover:border-emerald-100 transition-all cursor-pointer"
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
              <div class="font-bold text-emerald-600">{{ formatPrice(getTemplateJson(template).exFactoryPrice) }}</div>
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
            class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all"
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
</style>
