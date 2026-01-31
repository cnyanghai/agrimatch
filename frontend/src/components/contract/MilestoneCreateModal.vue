<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Truck, Package, CreditCard, ClipboardCheck, Settings, Plus } from 'lucide-vue-next'
import { createMilestone, type MilestoneCreateRequest } from '../../api/contract'
import { BaseModal, BaseButton } from '../ui'
import VehiclePicker from '../VehiclePicker.vue'
import type { VehicleInfo } from '../../api/vehicle'

const props = defineProps<{
  modelValue: boolean
  contractId: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'created'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)

// 表单数据
const form = ref<MilestoneCreateRequest & { responsibleParty?: string }>({
  milestoneType: 'CUSTOM',
  responsibleParty: undefined,
  milestoneName: '',
  description: '',
  expectedDate: '',
  sortOrder: 0
})

// 是否显示负责方选择（仅 CUSTOM 类型）
const showPartySelect = computed(() => form.value.milestoneType === 'CUSTOM')

// 车辆信息（发货节点用）
const vehicleInfo = ref<VehicleInfo>({
  driverName: '',
  driverIdCard: '',
  plateNumber: '',
  driverPhone: ''
})

// 是否显示车辆信息
const showVehicle = computed(() => form.value.milestoneType === 'SHIP')

// 节点类型选项
const typeOptions = [
  { value: 'SHIP', label: '发货', icon: Truck, desc: '卖方发货' },
  { value: 'RECEIVE', label: '收货', icon: Package, desc: '买方确认收货' },
  { value: 'PAY', label: '付款', icon: CreditCard, desc: '买方付款' },
  { value: 'INSPECT', label: '质检', icon: ClipboardCheck, desc: '质量检验' },
  { value: 'CUSTOM', label: '自定义', icon: Settings, desc: '自定义节点' }
]

// 选择类型时自动填充名称
function selectType(type: string) {
  form.value.milestoneType = type
  const option = typeOptions.find(t => t.value === type)
  if (option && type !== 'CUSTOM') {
    form.value.milestoneName = option.label
  }
}

// 重置表单
function resetForm() {
  form.value = {
    milestoneType: 'CUSTOM',
    responsibleParty: undefined,
    milestoneName: '',
    description: '',
    expectedDate: '',
    sortOrder: 0
  }
  vehicleInfo.value = {
    driverName: '',
    driverIdCard: '',
    plateNumber: '',
    driverPhone: ''
  }
}

// 关闭弹窗
function close() {
  visible.value = false
  resetForm()
}

// 提交
async function handleSubmit() {
  if (!form.value.milestoneName?.trim()) {
    ElMessage.warning('请输入节点名称')
    return
  }

  // 发货节点需要验证车辆信息
  if (showVehicle.value) {
    if (!vehicleInfo.value.driverName?.trim()) {
      ElMessage.warning('请输入司机姓名')
      return
    }
    if (!vehicleInfo.value.plateNumber?.trim()) {
      ElMessage.warning('请输入车牌号')
      return
    }
    if (!vehicleInfo.value.driverPhone?.trim()) {
      ElMessage.warning('请输入联系电话')
      return
    }
  }
  
  loading.value = true
  try {
    // 构建车辆信息 JSON
    let vehicleInfoJson: string | undefined
    if (showVehicle.value && vehicleInfo.value.driverName) {
      vehicleInfoJson = JSON.stringify(vehicleInfo.value)
    }

    const res = await createMilestone(props.contractId, {
      milestoneType: form.value.milestoneType,
      responsibleParty: form.value.milestoneType === 'CUSTOM' ? form.value.responsibleParty : undefined,
      milestoneName: form.value.milestoneName.trim(),
      description: form.value.description?.trim() || undefined,
      expectedDate: form.value.expectedDate || undefined,
      sortOrder: form.value.sortOrder || 0,
      vehicleInfoJson
    })
    
    if (res.code === 0) {
      ElMessage.success('节点添加成功')
      emit('created')
      close()
    } else {
      ElMessage.error(res.message || '添加失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '添加失败')
  } finally {
    loading.value = false
  }
}

// 监听打开时重置表单
watch(visible, (val) => {
  if (val) {
    resetForm()
  }
})
</script>

<template>
  <BaseModal
    v-model="visible"
    title="添加履约节点"
    subtitle="履约管理"
    size="md"
    @close="resetForm"
  >
    <!-- 头部图标 -->
    <template #icon>
      <div class="w-10 h-10 rounded-xl bg-brand-50 flex items-center justify-center">
        <Plus class="w-5 h-5 text-brand-600" />
      </div>
    </template>

    <div class="space-y-5">
      <!-- 节点类型 -->
      <div>
        <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">
          节点类型
        </label>
        <div class="grid grid-cols-5 gap-2">
          <button
            v-for="option in typeOptions"
            :key="option.value"
            :class="[
              'flex flex-col items-center gap-1 p-3 rounded-xl border-2 transition-all',
              form.milestoneType === option.value 
                ? 'border-brand-500 bg-brand-50' 
                : 'border-neutral-200 hover:border-neutral-200 hover:bg-neutral-50'
            ]"
            @click="selectType(option.value)"
          >
            <component 
              :is="option.icon" 
              :class="['w-5 h-5', form.milestoneType === option.value ? 'text-brand-600' : 'text-neutral-400']"
            />
            <span :class="['text-xs font-bold', form.milestoneType === option.value ? 'text-brand-600' : 'text-neutral-600']">
              {{ option.label }}
            </span>
          </button>
        </div>
      </div>
      
      <!-- 节点名称 -->
      <div>
        <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">
          节点名称 <span class="text-red-500">*</span>
        </label>
        <input
          v-model="form.milestoneName"
          type="text"
          placeholder="例如：发货确认、质量检验"
          class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-xl focus:border-brand-500 outline-none transition-all text-sm"
        />
      </div>
      
      <!-- 描述 -->
      <div>
        <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">
          描述说明
        </label>
        <textarea
          v-model="form.description"
          rows="2"
          placeholder="可选，说明此节点的具体要求..."
          class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-xl focus:border-brand-500 outline-none transition-all text-sm resize-none"
        />
      </div>
      
      <!-- 预计完成日期 -->
      <div>
        <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">
          预计完成日期
        </label>
        <input
          v-model="form.expectedDate"
          type="date"
          class="w-full px-4 py-2.5 border-2 border-neutral-200 rounded-xl focus:border-brand-500 outline-none transition-all text-sm"
        />
      </div>

      <!-- 负责方选择（自定义类型） -->
      <div v-if="showPartySelect">
        <label class="block text-xs font-bold text-neutral-500 uppercase tracking-wider mb-2">
          负责方
        </label>
        <div class="flex gap-2">
          <button
            :class="[
              'flex-1 py-2.5 rounded-xl border-2 text-sm font-bold transition-all',
              form.responsibleParty === 'buyer'
                ? 'border-autumn-400 bg-autumn-50 text-autumn-600'
                : 'border-neutral-200 text-neutral-500 hover:border-neutral-300 hover:bg-neutral-50'
            ]"
            @click="form.responsibleParty = 'buyer'"
          >
            买方负责
          </button>
          <button
            :class="[
              'flex-1 py-2.5 rounded-xl border-2 text-sm font-bold transition-all',
              form.responsibleParty === 'seller'
                ? 'border-brand-500 bg-brand-50 text-brand-600'
                : 'border-neutral-200 text-neutral-500 hover:border-neutral-300 hover:bg-neutral-50'
            ]"
            @click="form.responsibleParty = 'seller'"
          >
            卖方负责
          </button>
        </div>
        <p class="text-xs text-neutral-400 mt-1">选择后，只有负责方可以提交凭证，对方确认</p>
      </div>

      <!-- 车辆信息（发货节点） -->
      <VehiclePicker
        v-if="showVehicle"
        v-model="vehicleInfo"
      />
    </div>
    
    <!-- 底部 -->
    <template #footer>
      <BaseButton type="secondary" block @click="close">
        取消
      </BaseButton>
      <BaseButton 
        type="primary" 
        block
        :loading="loading" 
        :disabled="!form.milestoneName?.trim()"
        @click="handleSubmit"
      >
        <Plus v-if="!loading" class="w-4 h-4" />
        添加节点
      </BaseButton>
    </template>
  </BaseModal>
</template>
