<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createSupply, getNextSupplyNo, type SupplyCreateRequest } from '../api/supply'
import { getProductTree, getProductParams, addProductParamOption, type ProductNode, type ProductParamResponse } from '../api/product'
import { getMyCompany, type CompanyResponse } from '../api/company'
import { getMe, type UserResponse } from '../api/user'
import TwoLevelCategoryPicker from '../components/TwoLevelCategoryPicker.vue'
import PageHeader from '../components/PageHeader.vue'

const router = useRouter()
const loading = ref(false)
const supplyNo = ref<string>('')

// “发布信息”（仅本次展示/预览，不写入发布接口）
const company = ref<CompanyResponse | null>(null)
const meUser = ref<UserResponse | null>(null)
const companyNameInput = ref('')
const publisherNameInput = ref('')

const publisherName = computed(() => {
  const real = meUser.value?.realName?.trim()
  if (real) return real
  return meUser.value?.nickName || meUser.value?.userName || '—'
})

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
  } catch {
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
    loadNextSupplyNo(),
    loadCompanyInfo(),
    loadMeUser()
  ])
})

async function loadCompanyInfo() {
  try {
    const r = await getMyCompany()
    if (r.code === 0) {
      company.value = r.data ?? null
      if (!companyNameInput.value) {
        companyNameInput.value = company.value?.companyName || ''
      }
      // 默认用公司地址预填发货地址（用户仍可修改）
      if (!publishForm.shipAddress) {
        publishForm.shipAddress = company.value?.address || ''
      }
    }
  } catch {
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
  } catch {
    // 静默失败
  }
}

async function loadCategoryTree() {
  try {
    const r = await getProductTree()
    if (r.code === 0) categoryTree.value = r.data || []
  } catch {
    ElMessage.error('加载品类树失败')
  }
}

function resetPublishForm() {
  Object.assign(publishForm, {
    categoryId: undefined,
    categoryName: '',
    exFactoryPrice: undefined,
    origin: '',
    quantity: undefined,
    packaging: '',
    storageMethod: '',
    shipAddress: company.value?.address || '',
    deliveryMode: '到厂',
    expireMinutes: 4320,
    priceRulesJson: '{}',
    paramsJson: '{}',
    remark: ''
  })
  categoryParams.value = []
  dynamicParams.value = {}
  loadNextSupplyNo()
}

async function onCategoryChange(category: { id: number; name: string } | null) {
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
}

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
  } catch {
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
  } catch {
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
      custom[param.paramName] = value
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
      router.push('/supply')
    } else {
      throw new Error(r.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '发布失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="bg-gray-50 text-gray-900 min-h-screen">
    <div class="max-w-7xl mx-auto p-4 md:p-6 space-y-6">
      <PageHeader title="发布供应" subtitle="发布您的供应信息，并在供应管理中进行后续维护">
        <template #right>
          <el-button class="!rounded-xl transition-all active:scale-95" @click="router.push('/supply')">
            返回供应管理
          </el-button>
        </template>
      </PageHeader>

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
                  <el-input v-model="companyNameInput" placeholder="默认使用公司名称，可临时修改" />
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
                  说明：以上信息仅用于本次发布与后续维护展示，不会修改您的公司/个人资料。
                </div>
              </div>
            </section>

            <!-- 1) 基础信息 -->
            <section class="bg-white p-6 md:p-8 rounded-[32px] border border-gray-100 shadow-sm space-y-5">
              <div class="flex items-center gap-2 border-b pb-4">
                <div class="w-1.5 h-6 bg-emerald-600 rounded-full"></div>
                <h2 class="font-bold text-lg text-gray-800">基础信息</h2>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <el-form-item label="品类选择" required>
                  <TwoLevelCategoryPicker :categories="categoryTree" @update:modelValue="onCategoryChange" />
                </el-form-item>

                <el-form-item label="出厂价(元/吨)" required>
                  <el-input-number v-model="publishForm.exFactoryPrice" :min="0" :step="10" :controls="false" />
                </el-form-item>

                <el-form-item label="可供数量(吨)">
                  <el-input-number v-model="publishForm.quantity" :min="0" :step="1" :controls="false" />
                </el-form-item>

                <el-form-item label="产地">
                  <el-input v-model="publishForm.origin" placeholder="请输入产地" />
                </el-form-item>
              </div>
            </section>

            <!-- 2) 规格参数 -->
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

            <!-- 3) 物流与交付 -->
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

                <el-form-item label="交付方式">
                  <el-select v-model="publishForm.deliveryMode" clearable>
                    <el-option label="到厂" value="到厂" />
                    <el-option label="自提" value="自提" />
                    <el-option label="物流配送" value="物流配送" />
                  </el-select>
                </el-form-item>

                <el-form-item label="包装方式">
                  <el-select v-model="publishForm.packaging" clearable>
                    <el-option label="散装" value="散装" />
                    <el-option label="袋装" value="袋装" />
                    <el-option label="箱装" value="箱装" />
                  </el-select>
                </el-form-item>

                <el-form-item label="储存方式">
                  <el-input v-model="publishForm.storageMethod" placeholder="例如：常温 / 阴凉干燥" />
                </el-form-item>

                <el-form-item label="备注" class="md:col-span-2">
                  <el-input v-model="publishForm.remark" type="textarea" :rows="3" placeholder="补充说明（选填）" />
                </el-form-item>
              </div>

              <div class="flex justify-end gap-3 pt-2">
                <el-button class="!rounded-xl transition-all active:scale-95" @click="resetPublishForm">重置</el-button>
                <el-button
                  type="primary"
                  class="!rounded-xl !bg-emerald-600 hover:!bg-emerald-700 !border-emerald-600 transition-all active:scale-95"
                  :loading="loading"
                  @click="publishSupply"
                  size="large"
                >
                  发布供应信息
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
                    <div class="mt-1 font-bold text-gray-900 truncate">{{ companyNameInput || (company?.companyName || '—') }}</div>
                  </div>
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">发布人</div>
                    <div class="mt-1 font-bold text-gray-900 truncate">{{ publisherNameInput || publisherName }}</div>
                  </div>
                </div>

                <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">供应编号</div>
                  <div class="mt-1 font-bold text-gray-900 truncate">{{ previewData.supplyNo }}</div>
                </div>

                <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">品类</div>
                  <div class="mt-1 font-bold text-gray-900 truncate">{{ previewData.categoryName }}</div>
                </div>

                <div class="grid grid-cols-2 gap-3">
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">出厂价</div>
                    <div class="mt-1 font-bold text-emerald-700">¥{{ previewData.exFactoryPrice }}/吨</div>
                  </div>
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">数量</div>
                    <div class="mt-1 font-bold text-gray-900">{{ previewData.quantity }} 吨</div>
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-3">
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">产地</div>
                    <div class="mt-1 font-bold text-gray-900 truncate">{{ previewData.origin }}</div>
                  </div>
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">有效期</div>
                    <div class="mt-1 font-bold text-gray-900 truncate">{{ previewData.expireText }}</div>
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-3">
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">发货地</div>
                    <div class="mt-1 font-bold text-gray-900 truncate">{{ previewData.shipAddress }}</div>
                  </div>
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">交付</div>
                    <div class="mt-1 font-bold text-gray-900 truncate">{{ previewData.deliveryMode }}</div>
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-3">
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">包装</div>
                    <div class="mt-1 font-bold text-gray-900 truncate">{{ previewData.packaging }}</div>
                  </div>
                  <div class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                    <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">储存</div>
                    <div class="mt-1 font-bold text-gray-900 truncate">{{ previewData.storageMethod }}</div>
                  </div>
                </div>

                <div v-if="previewData.paramsText !== '无'" class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">指标</div>
                  <div class="mt-1 text-sm text-gray-700 whitespace-pre-wrap">{{ previewData.paramsText }}</div>
                </div>

                <div v-if="previewData.remark" class="bg-gray-50 rounded-2xl border border-gray-100 px-4 py-3">
                  <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">备注</div>
                  <div class="mt-1 text-sm text-gray-700 whitespace-pre-wrap">{{ previewData.remark }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* label 统一成“微标签”风格（限定在 neo-form 内） */
::deep(.neo-form .el-form-item__label) {
  font-weight: 800;
  font-size: 12px;
  color: rgb(107 114 128); /* gray-500 */
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

/* 本页表单：Element Plus 输入控件统一为 Neo-Minimal 低噪风格（限定在 .neo-form 内，避免影响全站） */
::deep(.neo-form .el-input__wrapper),
::deep(.neo-form .el-select__wrapper),
::deep(.neo-form .el-textarea__inner),
::deep(.neo-form .el-cascader .el-input__wrapper) {
  border: 2px solid rgb(243 244 246); /* gray-100 */
  border-radius: 12px; /* rounded-xl */
  box-shadow: none;
  background-color: #fff;
  transition: all 0.15s ease;
}

::deep(.neo-form .el-input__wrapper.is-focus),
::deep(.neo-form .el-select__wrapper.is-focus),
::deep(.neo-form .el-cascader .el-input__wrapper.is-focus),
::deep(.neo-form .el-textarea__inner:focus) {
  border-color: rgb(16 185 129); /* emerald-500 */
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18);
}

::deep(.neo-form .el-input__wrapper:hover),
::deep(.neo-form .el-select__wrapper:hover),
::deep(.neo-form .el-cascader .el-input__wrapper:hover),
::deep(.neo-form .el-textarea__inner:hover) {
  border-color: rgb(229 231 235); /* gray-200 */
}

::deep(.neo-form .el-input-number),
::deep(.neo-form .el-select),
::deep(.neo-form .el-cascader) {
  width: 100%;
}
</style>
