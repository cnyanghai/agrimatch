<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Truck, Plus, Star, Edit2, Trash2, Phone, CreditCard, ArrowLeft } from 'lucide-vue-next'
import { 
  listVehicles, 
  createVehicle, 
  updateVehicle, 
  deleteVehicle, 
  setDefaultVehicle,
  type VehicleResponse, 
  type VehicleCreateRequest 
} from '../api/vehicle'

const router = useRouter()

const loading = ref(false)
const vehicles = ref<VehicleResponse[]>([])

// 弹窗状态
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingId = ref<number | null>(null)
const submitting = ref(false)

// 表单
const form = ref<VehicleCreateRequest>({
  driverName: '',
  driverIdCard: '',
  plateNumber: '',
  driverPhone: '',
  vehicleType: '',
  remark: ''
})

// 加载车辆列表
async function loadVehicles() {
  loading.value = true
  try {
    const r = await listVehicles()
    if (r.code === 0) {
      vehicles.value = r.data ?? []
    } else {
      ElMessage.error(r.message || '加载失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载车辆列表失败')
  } finally {
    loading.value = false
  }
}

// 打开新增弹窗
function openCreate() {
  dialogMode.value = 'create'
  editingId.value = null
  form.value = {
    driverName: '',
    driverIdCard: '',
    plateNumber: '',
    driverPhone: '',
    vehicleType: '',
    remark: ''
  }
  dialogVisible.value = true
}

// 打开编辑弹窗
function openEdit(v: VehicleResponse) {
  dialogMode.value = 'edit'
  editingId.value = v.id
  form.value = {
    driverName: v.driverName,
    driverIdCard: v.driverIdCard,
    plateNumber: v.plateNumber,
    driverPhone: v.driverPhone,
    vehicleType: v.vehicleType || '',
    remark: v.remark || ''
  }
  dialogVisible.value = true
}

// 提交表单
async function handleSubmit() {
  if (!form.value.driverName?.trim()) {
    ElMessage.warning('请输入司机姓名')
    return
  }
  if (!form.value.driverIdCard?.trim()) {
    ElMessage.warning('请输入身份证号')
    return
  }
  if (!form.value.plateNumber?.trim()) {
    ElMessage.warning('请输入车牌号')
    return
  }
  if (!form.value.driverPhone?.trim()) {
    ElMessage.warning('请输入联系电话')
    return
  }

  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      const r = await createVehicle(form.value)
      if (r.code === 0) {
        ElMessage.success('车辆添加成功')
        dialogVisible.value = false
        loadVehicles()
      } else {
        ElMessage.error(r.message || '添加失败')
      }
    } else {
      const r = await updateVehicle(editingId.value!, form.value)
      if (r.code === 0) {
        ElMessage.success('车辆修改成功')
        dialogVisible.value = false
        loadVehicles()
      } else {
        ElMessage.error(r.message || '修改失败')
      }
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除车辆
async function handleDelete(v: VehicleResponse) {
  try {
    await ElMessageBox.confirm(
      `确定要删除车辆 "${v.plateNumber}" 吗？`,
      '删除确认',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    
    const r = await deleteVehicle(v.id)
    if (r.code === 0) {
      ElMessage.success('删除成功')
      loadVehicles()
    } else {
      ElMessage.error(r.message || '删除失败')
    }
  } catch {
    // 用户取消
  }
}

// 设为默认
async function handleSetDefault(v: VehicleResponse) {
  try {
    const r = await setDefaultVehicle(v.id)
    if (r.code === 0) {
      ElMessage.success(`已将 ${v.plateNumber} 设为默认车辆`)
      loadVehicles()
    } else {
      ElMessage.error(r.message || '设置失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '操作失败')
  }
}

// 脱敏身份证
function maskIdCard(idCard: string) {
  if (!idCard || idCard.length < 10) return idCard
  return idCard.substring(0, 6) + '****' + idCard.substring(idCard.length - 4)
}

onMounted(() => {
  loadVehicles()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 顶部导航 -->
    <header class="bg-white border-b border-gray-200 sticky top-0 z-40">
      <div class="max-w-7xl mx-auto px-4 py-4 flex items-center gap-4">
        <button 
          class="p-2 hover:bg-gray-100 rounded-xl transition-colors"
          @click="router.back()"
        >
          <ArrowLeft class="w-5 h-5 text-gray-600" />
        </button>
        <div>
          <h1 class="text-2xl font-bold text-gray-900">常用车辆管理</h1>
          <p class="text-xs text-gray-500">管理公司常用提货车辆信息</p>
        </div>
      </div>
    </header>

    <div class="max-w-7xl mx-auto p-6">
      <!-- 头部操作 -->
      <div class="flex items-center justify-between mb-8">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 rounded-xl bg-blue-50 flex items-center justify-center">
            <Truck class="w-6 h-6 text-blue-600" />
          </div>
          <div>
            <p class="text-sm text-gray-500">共 {{ vehicles.length }} 辆常用车辆</p>
          </div>
        </div>
        <button
          class="flex items-center gap-2 px-5 py-2.5 bg-brand-600 text-white rounded-xl font-bold hover:bg-brand-700 transition-all  shadow-md shadow-brand-500/20"
          @click="openCreate"
        >
          <Plus class="w-5 h-5" />
          添加车辆
        </button>
      </div>

      <!-- 车辆列表 -->
      <div v-loading="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="v in vehicles"
          :key="v.id"
          class="bg-white rounded-xl border border-gray-200 p-6 hover:shadow-md hover:border-brand-100 transition-all group"
        >
          <!-- 头部：车牌号 + 默认标记 -->
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 rounded-xl bg-blue-50 flex items-center justify-center">
                <Truck class="w-6 h-6 text-blue-600" />
              </div>
              <div>
                <div class="text-lg font-black text-gray-900">{{ v.plateNumber }}</div>
                <div v-if="v.vehicleType" class="text-xs text-gray-400">{{ v.vehicleType }}</div>
              </div>
            </div>
            <span 
              v-if="v.isDefault"
              class="flex items-center gap-1 px-2.5 py-1 bg-amber-50 text-amber-600 rounded-lg"
            >
              <Star class="w-3.5 h-3.5" fill="currentColor" />
              <span class="text-[10px] font-bold">默认</span>
            </span>
          </div>

          <!-- 司机信息 -->
          <div class="space-y-2 mb-4">
            <div class="flex items-center gap-2 text-sm">
              <span class="text-gray-400">司机姓名：</span>
              <span class="font-bold text-gray-900">{{ v.driverName }}</span>
            </div>
            <div class="flex items-center gap-2 text-sm">
              <CreditCard class="w-4 h-4 text-gray-400" />
              <span class="text-gray-600">{{ maskIdCard(v.driverIdCard) }}</span>
            </div>
            <div class="flex items-center gap-2 text-sm">
              <Phone class="w-4 h-4 text-gray-400" />
              <span class="text-gray-600">{{ v.driverPhone }}</span>
            </div>
          </div>

          <!-- 备注 -->
          <div v-if="v.remark" class="text-xs text-gray-400 mb-4 line-clamp-2">
            {{ v.remark }}
          </div>

          <!-- 操作按钮 -->
          <div class="flex items-center gap-2 pt-4 border-t border-gray-50">
            <button
              v-if="!v.isDefault"
              class="flex-1 flex items-center justify-center gap-1.5 py-2 text-xs font-bold text-amber-600 hover:bg-amber-50 rounded-lg transition-colors"
              @click="handleSetDefault(v)"
            >
              <Star class="w-3.5 h-3.5" />
              设为默认
            </button>
            <button
              class="flex-1 flex items-center justify-center gap-1.5 py-2 text-xs font-bold text-gray-600 hover:bg-gray-50 rounded-lg transition-colors"
              @click="openEdit(v)"
            >
              <Edit2 class="w-3.5 h-3.5" />
              编辑
            </button>
            <button
              class="flex-1 flex items-center justify-center gap-1.5 py-2 text-xs font-bold text-red-500 hover:bg-red-50 rounded-lg transition-colors"
              @click="handleDelete(v)"
            >
              <Trash2 class="w-3.5 h-3.5" />
              删除
            </button>
          </div>
        </div>

        <!-- 空状态 -->
        <div 
          v-if="!loading && vehicles.length === 0"
          class="col-span-full flex flex-col items-center justify-center py-16 bg-white rounded-xl border border-dashed border-gray-200"
        >
          <Truck class="w-16 h-16 text-gray-200 mb-4" />
          <p class="text-gray-400 font-bold mb-4">暂无常用车辆</p>
          <button
            class="px-6 py-2 bg-brand-600 text-white rounded-xl font-bold hover:bg-brand-700 transition-all"
            @click="openCreate"
          >
            添加第一辆车
          </button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '添加常用车辆' : '编辑车辆信息'"
      width="480px"
      :close-on-click-modal="false"
      align-center
      class="!rounded-[24px]"
    >
      <div class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-bold text-gray-500 mb-1.5">司机姓名 <span class="text-red-500">*</span></label>
            <input
              v-model="form.driverName"
              type="text"
              placeholder="请输入司机姓名"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all text-sm"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 mb-1.5">联系电话 <span class="text-red-500">*</span></label>
            <input
              v-model="form.driverPhone"
              type="tel"
              placeholder="请输入联系电话"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all text-sm"
            />
          </div>
        </div>
        
        <div>
          <label class="block text-xs font-bold text-gray-500 mb-1.5">身份证号 <span class="text-red-500">*</span></label>
          <input
            v-model="form.driverIdCard"
            type="text"
            placeholder="请输入身份证号"
            class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all text-sm"
          />
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-bold text-gray-500 mb-1.5">车牌号 <span class="text-red-500">*</span></label>
            <input
              v-model="form.plateNumber"
              type="text"
              placeholder="例如：鄂A12345"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all text-sm uppercase"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 mb-1.5">车辆类型</label>
            <input
              v-model="form.vehicleType"
              type="text"
              placeholder="例如：货车、挂车"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all text-sm"
            />
          </div>
        </div>

        <div>
          <label class="block text-xs font-bold text-gray-500 mb-1.5">备注</label>
          <textarea
            v-model="form.remark"
            rows="2"
            placeholder="可选，添加备注信息..."
            class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all text-sm resize-none"
          />
        </div>
      </div>

      <template #footer>
        <div class="flex gap-3">
          <button
            class="flex-1 py-2.5 bg-gray-100 text-gray-700 rounded-xl font-bold hover:bg-gray-200 transition-all"
            @click="dialogVisible = false"
          >
            取消
          </button>
          <button
            class="flex-1 py-2.5 bg-brand-600 text-white rounded-xl font-bold hover:bg-brand-700 transition-all disabled:opacity-50"
            :disabled="submitting"
            @click="handleSubmit"
          >
            {{ submitting ? '保存中...' : '保存' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

