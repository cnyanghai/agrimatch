<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Truck, ChevronDown, Plus, Star } from 'lucide-vue-next'
import { listVehicles, type VehicleResponse, type VehicleInfo } from '../api/vehicle'

const props = defineProps<{
  modelValue?: VehicleInfo
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: VehicleInfo): void
}>()

const vehicles = ref<VehicleResponse[]>([])
const loading = ref(false)
const dropdownOpen = ref(false)
const manualMode = ref(false)

// 本地表单数据
const form = ref<VehicleInfo>({
  driverName: '',
  driverIdCard: '',
  plateNumber: '',
  driverPhone: ''
})

// 监听 modelValue 变化
watch(() => props.modelValue, (val) => {
  if (val) {
    form.value = { ...val }
  }
}, { immediate: true })

// 当表单变化时触发
function emitChange() {
  emit('update:modelValue', { ...form.value })
}

// 加载车辆列表
async function loadVehicles() {
  loading.value = true
  try {
    const r = await listVehicles()
    if (r.code === 0) {
      vehicles.value = r.data ?? []
    }
  } catch (e: any) {
    console.error('加载车辆列表失败', e)
  } finally {
    loading.value = false
  }
}

// 选择车辆
function selectVehicle(v: VehicleResponse) {
  form.value = {
    driverName: v.driverName,
    driverIdCard: v.driverIdCard,
    plateNumber: v.plateNumber,
    driverPhone: v.driverPhone
  }
  manualMode.value = false
  dropdownOpen.value = false
  emitChange()
}

// 切换手动输入
function switchToManual() {
  manualMode.value = true
  dropdownOpen.value = false
  form.value = {
    driverName: '',
    driverIdCard: '',
    plateNumber: '',
    driverPhone: ''
  }
  emitChange()
}

// 格式化身份证（脱敏）
function maskIdCard(idCard: string) {
  if (!idCard || idCard.length < 10) return idCard
  return idCard.substring(0, 6) + '****' + idCard.substring(idCard.length - 4)
}

onMounted(() => {
  loadVehicles()
})
</script>

<template>
  <div class="vehicle-picker">
    <!-- 标题行 -->
    <div class="flex items-center justify-between mb-3">
      <div class="flex items-center gap-2">
        <div class="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center">
          <Truck class="w-4 h-4 text-blue-600" />
        </div>
        <span class="text-sm font-bold text-gray-700">提货车辆信息</span>
      </div>
      
      <!-- 常用车辆下拉 -->
      <div class="relative">
        <button
          type="button"
          class="flex items-center gap-1.5 px-3 py-1.5 bg-gray-50 hover:bg-gray-100 rounded-lg text-xs font-bold text-gray-600 transition-all"
          @click="dropdownOpen = !dropdownOpen"
        >
          <span>选择常用车辆</span>
          <ChevronDown class="w-3.5 h-3.5" :class="{ 'rotate-180': dropdownOpen }" />
        </button>
        
        <!-- 下拉菜单 -->
        <div 
          v-if="dropdownOpen"
          class="absolute right-0 top-full mt-1 w-72 bg-white rounded-xl border border-gray-100 shadow-xl z-50 overflow-hidden"
        >
          <div v-if="loading" class="p-4 text-center text-gray-400 text-sm">
            加载中...
          </div>
          <div v-else-if="vehicles.length === 0" class="p-4 text-center text-gray-400 text-sm">
            暂无常用车辆
          </div>
          <div v-else class="max-h-64 overflow-y-auto">
            <button
              v-for="v in vehicles"
              :key="v.id"
              type="button"
              class="w-full px-4 py-3 text-left hover:bg-gray-50 border-b border-gray-50 last:border-b-0 transition-colors"
              @click="selectVehicle(v)"
            >
              <div class="flex items-center justify-between mb-1">
                <span class="font-bold text-gray-900">{{ v.plateNumber }}</span>
                <span v-if="v.isDefault" class="flex items-center gap-0.5 text-amber-500">
                  <Star class="w-3 h-3" fill="currentColor" />
                  <span class="text-[10px] font-bold">默认</span>
                </span>
              </div>
              <div class="text-xs text-gray-500">
                {{ v.driverName }} · {{ v.driverPhone }}
              </div>
            </button>
          </div>
          
          <!-- 手动输入选项 -->
          <button
            type="button"
            class="w-full px-4 py-3 text-left hover:bg-emerald-50 border-t border-gray-100 flex items-center gap-2 text-emerald-600"
            @click="switchToManual"
          >
            <Plus class="w-4 h-4" />
            <span class="text-sm font-bold">手动输入新车辆</span>
          </button>
        </div>
      </div>
    </div>
    
    <!-- 车辆信息表单 -->
    <div class="grid grid-cols-2 gap-3">
      <div>
        <label class="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">
          司机姓名 <span class="text-red-500">*</span>
        </label>
        <input
          v-model="form.driverName"
          type="text"
          placeholder="请输入司机姓名"
          class="w-full px-3 py-2 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-sm"
          @input="emitChange"
        />
      </div>
      <div>
        <label class="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">
          身份证号 <span class="text-red-500">*</span>
        </label>
        <input
          v-model="form.driverIdCard"
          type="text"
          placeholder="请输入身份证号"
          class="w-full px-3 py-2 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-sm"
          @input="emitChange"
        />
      </div>
      <div>
        <label class="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">
          车牌号 <span class="text-red-500">*</span>
        </label>
        <input
          v-model="form.plateNumber"
          type="text"
          placeholder="例如：鄂A12345"
          class="w-full px-3 py-2 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-sm uppercase"
          @input="emitChange"
        />
      </div>
      <div>
        <label class="block text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1">
          联系电话 <span class="text-red-500">*</span>
        </label>
        <input
          v-model="form.driverPhone"
          type="tel"
          placeholder="请输入联系电话"
          class="w-full px-3 py-2 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-sm"
          @input="emitChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.vehicle-picker {
  background: linear-gradient(135deg, #f0f9ff 0%, #f8fafc 100%);
  border: 1px solid #e0f2fe;
  border-radius: 1rem;
  padding: 1rem;
}
</style>

