<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../store/auth'
import { Plus, Search, Document, Check, Clock, Warning, Edit, View, Download } from '@element-plus/icons-vue'

const auth = useAuthStore()
const loading = ref(false)
const activeTab = ref('all')
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const selectedContract = ref<any>(null)

// 合同列表
const contracts = ref<any[]>([])

// 筛选条件
const filters = reactive({
  keyword: '',
  status: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 创建合同表单
const contractForm = reactive({
  contractNo: '',
  contractType: 'purchase',
  title: '',
  partyA: '',
  partyB: '',
  productName: '',
  quantity: 0,
  unit: '吨',
  unitPrice: 0,
  totalAmount: 0,
  deliveryDate: '',
  deliveryAddress: '',
  paymentMethod: '',
  terms: ''
})

// 合同状态
const statusOptions = [
  { value: '', label: '全部状态' },
  { value: 'draft', label: '草稿' },
  { value: 'pending', label: '待签署' },
  { value: 'signed', label: '已签署' },
  { value: 'executing', label: '履行中' },
  { value: 'completed', label: '已完成' },
  { value: 'cancelled', label: '已取消' }
]

// 筛选后的合同
const filteredContracts = computed(() => {
  let result = contracts.value
  if (activeTab.value !== 'all') {
    result = result.filter(c => c.status === activeTab.value)
  }
  if (filters.keyword) {
    const kw = filters.keyword.toLowerCase()
    result = result.filter(c => 
      c.contractNo?.toLowerCase().includes(kw) ||
      c.title?.toLowerCase().includes(kw) ||
      c.productName?.toLowerCase().includes(kw)
    )
  }
  return result
})

// 统计数据
const stats = computed(() => ({
  all: contracts.value.length,
  draft: contracts.value.filter(c => c.status === 'draft').length,
  pending: contracts.value.filter(c => c.status === 'pending').length,
  signed: contracts.value.filter(c => c.status === 'signed').length,
  executing: contracts.value.filter(c => c.status === 'executing').length,
  completed: contracts.value.filter(c => c.status === 'completed').length
}))

// 获取状态标签
function getStatusTag(status: string) {
  const map: Record<string, { type: string; label: string }> = {
    draft: { type: 'info', label: '草稿' },
    pending: { type: 'warning', label: '待签署' },
    signed: { type: 'primary', label: '已签署' },
    executing: { type: '', label: '履行中' },
    completed: { type: 'success', label: '已完成' },
    cancelled: { type: 'danger', label: '已取消' }
  }
  return map[status] || { type: 'info', label: status }
}

// 加载合同列表
async function loadContracts() {
  loading.value = true
  try {
    // 模拟数据 - 实际应该调用API
    await new Promise(resolve => setTimeout(resolve, 500))
    contracts.value = [
      {
        id: 1,
        contractNo: 'HT-2024-001',
        title: '小麦采购合同',
        contractType: 'purchase',
        partyA: '北京粮食贸易有限公司',
        partyB: '山东优质农产品合作社',
        productName: '优质小麦',
        quantity: 100,
        unit: '吨',
        unitPrice: 2800,
        totalAmount: 280000,
        status: 'executing',
        createTime: '2024-12-20 10:30:00',
        signTime: '2024-12-21 14:00:00'
      },
      {
        id: 2,
        contractNo: 'HT-2024-002',
        title: '玉米供应合同',
        contractType: 'supply',
        partyA: '河南农业科技有限公司',
        partyB: '上海食品加工厂',
        productName: '黄玉米',
        quantity: 50,
        unit: '吨',
        unitPrice: 2500,
        totalAmount: 125000,
        status: 'pending',
        createTime: '2024-12-22 09:15:00'
      },
      {
        id: 3,
        contractNo: 'HT-2024-003',
        title: '大豆采购合同',
        contractType: 'purchase',
        partyA: '江苏食品集团',
        partyB: '黑龙江农场',
        productName: '东北大豆',
        quantity: 200,
        unit: '吨',
        unitPrice: 4500,
        totalAmount: 900000,
        status: 'completed',
        createTime: '2024-12-15 11:20:00',
        signTime: '2024-12-16 10:00:00'
      }
    ]
    pagination.total = contracts.value.length
  } catch (e) {
    ElMessage.error('加载合同列表失败')
  } finally {
    loading.value = false
  }
}

// 创建合同
function openCreateDialog() {
  Object.assign(contractForm, {
    contractNo: `HT-${new Date().getFullYear()}-${String(Date.now()).slice(-4)}`,
    contractType: 'purchase',
    title: '',
    partyA: '',
    partyB: '',
    productName: '',
    quantity: 0,
    unit: '吨',
    unitPrice: 0,
    totalAmount: 0,
    deliveryDate: '',
    deliveryAddress: '',
    paymentMethod: '',
    terms: ''
  })
  showCreateDialog.value = true
}

// 计算总金额
function calculateTotal() {
  contractForm.totalAmount = contractForm.quantity * contractForm.unitPrice
}

// 保存合同
async function saveContract() {
  if (!contractForm.title) {
    ElMessage.warning('请输入合同标题')
    return
  }
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('合同保存成功')
    showCreateDialog.value = false
    await loadContracts()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

// 查看合同详情
function viewContract(contract: any) {
  selectedContract.value = contract
  showDetailDialog.value = true
}

// 发起签署
async function initiateSign(contract: any) {
  try {
    await ElMessageBox.confirm(
      `确定要发起合同 ${contract.contractNo} 的签署流程吗？`,
      '发起签署',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
    )
    ElMessage.success('已发起签署流程，等待对方确认')
  } catch {
    // 取消
  }
}

// 签署合同
async function signContract(contract: any) {
  try {
    await ElMessageBox.confirm(
      `确定要签署合同 ${contract.contractNo} 吗？`,
      '签署合同',
      { confirmButtonText: '确定签署', cancelButtonText: '取消', type: 'warning' }
    )
    ElMessage.success('合同签署成功')
    await loadContracts()
  } catch {
    // 取消
  }
}

// 下载合同
function downloadContract(contract: any) {
  ElMessage.info('合同下载功能开发中...')
}

onMounted(() => {
  loadContracts()
})
</script>

<template>
  <div class="contract-view">
    <!-- 页面标题 -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-800">合同管理</h1>
      <p class="text-gray-500 mt-1">管理您的交易合同，包括创建、签署和履行</p>
    </div>

    <!-- 统计卡片 - 统一蓝灰色调 -->
    <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-6">
      <div class="bg-white rounded-xl p-4 shadow-sm border border-gray-100 cursor-pointer hover:shadow-md transition-all"
           :class="activeTab === 'all' ? 'ring-2 ring-blue-500' : ''"
           @click="activeTab = 'all'">
        <div class="text-2xl font-bold text-gray-800">{{ stats.all }}</div>
        <div class="text-sm text-gray-500">全部合同</div>
      </div>
      <div class="bg-white rounded-xl p-4 shadow-sm border border-gray-100 cursor-pointer hover:shadow-md transition-all"
           :class="activeTab === 'draft' ? 'ring-2 ring-gray-400' : ''"
           @click="activeTab = 'draft'">
        <div class="text-2xl font-bold text-gray-500">{{ stats.draft }}</div>
        <div class="text-sm text-gray-500">草稿</div>
      </div>
      <div class="bg-white rounded-xl p-4 shadow-sm border border-gray-100 cursor-pointer hover:shadow-md transition-all"
           :class="activeTab === 'pending' ? 'ring-2 ring-blue-400' : ''"
           @click="activeTab = 'pending'">
        <div class="text-2xl font-bold text-blue-500">{{ stats.pending }}</div>
        <div class="text-sm text-gray-500">待签署</div>
      </div>
      <div class="bg-white rounded-xl p-4 shadow-sm border border-gray-100 cursor-pointer hover:shadow-md transition-all"
           :class="activeTab === 'signed' ? 'ring-2 ring-blue-500' : ''"
           @click="activeTab = 'signed'">
        <div class="text-2xl font-bold text-blue-600">{{ stats.signed }}</div>
        <div class="text-sm text-gray-500">已签署</div>
      </div>
      <div class="bg-white rounded-xl p-4 shadow-sm border border-gray-100 cursor-pointer hover:shadow-md transition-all"
           :class="activeTab === 'executing' ? 'ring-2 ring-blue-500' : ''"
           @click="activeTab = 'executing'">
        <div class="text-2xl font-bold text-blue-600">{{ stats.executing }}</div>
        <div class="text-sm text-gray-500">履行中</div>
      </div>
      <div class="bg-white rounded-xl p-4 shadow-sm border border-gray-100 cursor-pointer hover:shadow-md transition-all"
           :class="activeTab === 'completed' ? 'ring-2 ring-gray-500' : ''"
           @click="activeTab = 'completed'">
        <div class="text-2xl font-bold text-gray-700">{{ stats.completed }}</div>
        <div class="text-sm text-gray-500">已完成</div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="bg-white rounded-xl shadow-sm p-4 mb-6">
      <div class="flex flex-wrap items-center justify-between gap-4">
        <div class="flex items-center gap-3">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索合同编号/标题/产品"
            :prefix-icon="Search"
            clearable
            class="w-64"
          />
        </div>
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">新建合同</el-button>
      </div>
    </div>

    <!-- 合同列表 -->
    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <el-table :data="filteredContracts" v-loading="loading" stripe>
        <el-table-column prop="contractNo" label="合同编号" width="140" />
        <el-table-column prop="title" label="合同标题" min-width="180">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-icon class="text-blue-500"><Document /></el-icon>
              <span class="font-medium">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="交易类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.contractType === 'purchase' ? 'primary' : 'warning'" size="small">
              {{ row.contractType === 'purchase' ? '采购' : '供应' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="产品名称" width="120" />
        <el-table-column label="数量" width="100">
          <template #default="{ row }">
            {{ row.quantity }} {{ row.unit }}
          </template>
        </el-table-column>
        <el-table-column label="合同金额" width="120">
          <template #default="{ row }">
            <span class="font-medium text-orange-600">¥{{ row.totalAmount?.toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status).type as any" size="small">
              {{ getStatusTag(row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-button size="small" :icon="View" @click="viewContract(row)">查看</el-button>
              <el-button 
                v-if="row.status === 'draft'" 
                type="primary" 
                size="small" 
                :icon="Edit"
                @click="initiateSign(row)"
              >
                发起签署
              </el-button>
              <el-button 
                v-if="row.status === 'pending'" 
                type="success" 
                size="small" 
                :icon="Check"
                @click="signContract(row)"
              >
                签署
              </el-button>
              <el-button 
                v-if="['signed', 'executing', 'completed'].includes(row.status)" 
                size="small" 
                :icon="Download"
                @click="downloadContract(row)"
              >
                下载
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="p-4 border-t border-gray-100 flex justify-end">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
        />
      </div>
    </div>

    <!-- 创建合同对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      title="新建合同" 
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form label-position="top" class="grid grid-cols-2 gap-x-6">
        <el-form-item label="合同编号" class="col-span-1">
          <el-input v-model="contractForm.contractNo" disabled />
        </el-form-item>
        <el-form-item label="合同类型" class="col-span-1">
          <el-radio-group v-model="contractForm.contractType">
            <el-radio value="purchase">采购合同</el-radio>
            <el-radio value="supply">供应合同</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="合同标题" class="col-span-2">
          <el-input v-model="contractForm.title" placeholder="请输入合同标题" />
        </el-form-item>
        <el-form-item label="甲方（采购方）" class="col-span-1">
          <el-input v-model="contractForm.partyA" placeholder="请输入甲方名称" />
        </el-form-item>
        <el-form-item label="乙方（供应方）" class="col-span-1">
          <el-input v-model="contractForm.partyB" placeholder="请输入乙方名称" />
        </el-form-item>
        <el-form-item label="产品名称" class="col-span-1">
          <el-input v-model="contractForm.productName" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="交货日期" class="col-span-1">
          <el-date-picker v-model="contractForm.deliveryDate" type="date" class="w-full" />
        </el-form-item>
        <el-form-item label="数量" class="col-span-1">
          <el-input-number 
            v-model="contractForm.quantity" 
            :min="0" 
            class="w-full"
            @change="calculateTotal"
          />
        </el-form-item>
        <el-form-item label="单位" class="col-span-1">
          <el-select v-model="contractForm.unit" class="w-full">
            <el-option label="吨" value="吨" />
            <el-option label="公斤" value="公斤" />
            <el-option label="斤" value="斤" />
            <el-option label="件" value="件" />
          </el-select>
        </el-form-item>
        <el-form-item label="单价（元）" class="col-span-1">
          <el-input-number 
            v-model="contractForm.unitPrice" 
            :min="0" 
            :precision="2"
            class="w-full"
            @change="calculateTotal"
          />
        </el-form-item>
        <el-form-item label="总金额（元）" class="col-span-1">
          <el-input-number 
            v-model="contractForm.totalAmount" 
            :min="0" 
            :precision="2"
            disabled
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="交货地址" class="col-span-2">
          <el-input v-model="contractForm.deliveryAddress" placeholder="请输入交货地址" />
        </el-form-item>
        <el-form-item label="付款方式" class="col-span-1">
          <el-select v-model="contractForm.paymentMethod" class="w-full">
            <el-option label="款到发货" value="prepay" />
            <el-option label="货到付款" value="cod" />
            <el-option label="分期付款" value="installment" />
            <el-option label="账期付款" value="credit" />
          </el-select>
        </el-form-item>
        <el-form-item label="其他条款" class="col-span-2">
          <el-input 
            v-model="contractForm.terms" 
            type="textarea" 
            :rows="4"
            placeholder="请输入其他合同条款" 
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="info" @click="saveContract">保存草稿</el-button>
        <el-button type="primary" :loading="loading" @click="saveContract">保存并发起签署</el-button>
      </template>
    </el-dialog>

    <!-- 合同详情对话框 -->
    <el-dialog 
      v-model="showDetailDialog" 
      title="合同详情" 
      width="700px"
    >
      <template v-if="selectedContract">
        <div class="space-y-6">
          <!-- 基本信息 -->
          <div class="bg-gray-50 rounded-xl p-4">
            <div class="flex items-center justify-between mb-4">
              <h4 class="font-semibold text-gray-800">{{ selectedContract.title }}</h4>
              <el-tag :type="getStatusTag(selectedContract.status).type as any">
                {{ getStatusTag(selectedContract.status).label }}
              </el-tag>
            </div>
            <div class="grid grid-cols-2 gap-4 text-sm">
              <div><span class="text-gray-500">合同编号：</span>{{ selectedContract.contractNo }}</div>
              <div><span class="text-gray-500">合同类型：</span>{{ selectedContract.contractType === 'purchase' ? '采购合同' : '供应合同' }}</div>
              <div><span class="text-gray-500">甲方：</span>{{ selectedContract.partyA }}</div>
              <div><span class="text-gray-500">乙方：</span>{{ selectedContract.partyB }}</div>
            </div>
          </div>

          <!-- 交易信息 -->
          <div>
            <h4 class="font-semibold text-gray-800 mb-3">交易信息</h4>
            <div class="grid grid-cols-2 gap-4 text-sm">
              <div><span class="text-gray-500">产品名称：</span>{{ selectedContract.productName }}</div>
              <div><span class="text-gray-500">数量：</span>{{ selectedContract.quantity }} {{ selectedContract.unit }}</div>
              <div><span class="text-gray-500">单价：</span>¥{{ selectedContract.unitPrice?.toLocaleString() }}/{{ selectedContract.unit }}</div>
              <div><span class="text-gray-500">总金额：</span><span class="text-orange-600 font-medium">¥{{ selectedContract.totalAmount?.toLocaleString() }}</span></div>
            </div>
          </div>

          <!-- 时间信息 -->
          <div>
            <h4 class="font-semibold text-gray-800 mb-3">时间信息</h4>
            <div class="grid grid-cols-2 gap-4 text-sm">
              <div><span class="text-gray-500">创建时间：</span>{{ selectedContract.createTime }}</div>
              <div v-if="selectedContract.signTime"><span class="text-gray-500">签署时间：</span>{{ selectedContract.signTime }}</div>
            </div>
          </div>
        </div>
      </template>

      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button type="primary" :icon="Download" @click="downloadContract(selectedContract)">下载合同</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.contract-view :deep(.el-table) {
  border-radius: 0;
}
</style>

