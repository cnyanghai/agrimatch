<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Plus, Truck, Package, CreditCard, ClipboardCheck, Settings,
  Clock, Upload, Check, X, Eye, Trash2, AlertCircle
} from 'lucide-vue-next'
import { 
  listMilestones, confirmMilestone, rejectMilestone, deleteMilestone,
  type MilestoneResponse 
} from '../../api/contract'

const props = defineProps<{
  contractId: number
  contractStatus: number
  buyerCompanyId?: number
  sellerCompanyId?: number
  currentUserCompanyId?: number
}>()

const emit = defineEmits<{
  (e: 'add'): void
  (e: 'submit', milestone: MilestoneResponse): void
  (e: 'refresh'): void
}>()

const loading = ref(false)
const milestones = ref<MilestoneResponse[]>([])

// 是否可以添加节点（已签署或履约中）
const canAdd = computed(() => props.contractStatus === 2 || props.contractStatus === 3)

// 节点类型映射
const milestoneTypeMap: Record<string, { label: string; icon: any; color: string }> = {
  'SHIP': { label: '发货', icon: Truck, color: 'bg-blue-50 text-blue-600' },
  'RECEIVE': { label: '收货', icon: Package, color: 'bg-emerald-50 text-emerald-600' },
  'PAY': { label: '付款', icon: CreditCard, color: 'bg-amber-50 text-amber-600' },
  'INSPECT': { label: '质检', icon: ClipboardCheck, color: 'bg-purple-50 text-purple-600' },
  'CUSTOM': { label: '自定义', icon: Settings, color: 'bg-gray-100 text-gray-600' }
}

// 状态映射
const statusMap: Record<string, { label: string; color: string; bgColor: string }> = {
  'pending': { label: '待提交', color: 'text-gray-500', bgColor: 'bg-gray-100' },
  'submitted': { label: '待确认', color: 'text-amber-600', bgColor: 'bg-amber-50' },
  'confirmed': { label: '已完成', color: 'text-emerald-600', bgColor: 'bg-emerald-50' },
  'rejected': { label: '已拒绝', color: 'text-red-500', bgColor: 'bg-red-50' }
}

// 加载节点列表
async function loadMilestones() {
  if (!props.contractId) return
  
  loading.value = true
  try {
    const res = await listMilestones(props.contractId)
    if (res.code === 0 && res.data) {
      milestones.value = res.data
    }
  } catch (e: any) {
    console.error('加载履约节点失败', e)
  } finally {
    loading.value = false
  }
}

// 判断当前用户是否是节点创建者
function isCreator(milestone: MilestoneResponse): boolean {
  // 简化判断：通过 operatorUserId 判断
  return false // 需要后端返回当前用户ID来判断
}

// 判断当前用户是否可以确认/拒绝节点
function canConfirm(milestone: MilestoneResponse): boolean {
  if (milestone.status !== 'submitted') return false
  // 只有对方才能确认（不是提交者）
  return true // 简化处理，后端会校验
}

// 判断是否可以提交凭证
function canSubmit(milestone: MilestoneResponse): boolean {
  return milestone.status === 'pending' || milestone.status === 'rejected'
}

// 判断是否可以删除
function canDelete(milestone: MilestoneResponse): boolean {
  return milestone.status === 'pending'
}

// 确认节点
async function handleConfirm(milestone: MilestoneResponse) {
  if (!confirm('确认该履约节点已完成？')) return
  
  try {
    const res = await confirmMilestone(props.contractId, milestone.id)
    if (res.code === 0) {
      ElMessage.success('确认成功')
      loadMilestones()
      emit('refresh')
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '确认失败')
  }
}

// 拒绝节点
async function handleReject(milestone: MilestoneResponse) {
  const reason = prompt('请输入拒绝原因：')
  if (reason === null) return
  
  try {
    const res = await rejectMilestone(props.contractId, milestone.id, reason || undefined)
    if (res.code === 0) {
      ElMessage.success('已拒绝')
      loadMilestones()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

// 删除节点
async function handleDelete(milestone: MilestoneResponse) {
  if (!confirm('确定要删除此节点吗？')) return
  
  try {
    const res = await deleteMilestone(props.contractId, milestone.id)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      loadMilestones()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '删除失败')
  }
}

// 格式化日期
function formatDate(val?: string): string {
  if (!val) return '-'
  return val.split('T')[0]
}

// 获取类型信息
function getTypeInfo(type: string) {
  return milestoneTypeMap[type] || milestoneTypeMap['CUSTOM']
}

// 获取状态信息
function getStatusInfo(status: string) {
  return statusMap[status] || statusMap['pending']
}

// 监听 contractId 变化
watch(() => props.contractId, () => {
  loadMilestones()
}, { immediate: true })

// 暴露刷新方法
defineExpose({ refresh: loadMilestones })
</script>

<template>
  <div class="milestone-list">
    <!-- 标题栏 -->
    <div class="flex items-center justify-between mb-4">
      <div class="flex items-center gap-2">
        <div class="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center">
          <ClipboardCheck class="w-4 h-4 text-blue-600" />
        </div>
        <h3 class="font-bold text-gray-900">履约进度</h3>
        <span class="text-xs text-gray-400">
          {{ milestones.filter(m => m.status === 'confirmed').length }}/{{ milestones.length }} 已完成
        </span>
      </div>
      
      <button
        v-if="canAdd"
        class="flex items-center gap-1.5 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold rounded-lg transition-all active:scale-95"
        @click="emit('add')"
      >
        <Plus class="w-3.5 h-3.5" />
        添加节点
      </button>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="py-8 text-center text-gray-400">
      加载中...
    </div>
    
    <!-- 空状态 -->
    <div v-else-if="milestones.length === 0" class="py-8 text-center">
      <div class="w-12 h-12 mx-auto rounded-xl bg-gray-100 flex items-center justify-center mb-3">
        <ClipboardCheck class="w-6 h-6 text-gray-400" />
      </div>
      <p class="text-sm text-gray-500">暂无履约节点</p>
      <p v-if="canAdd" class="text-xs text-gray-400 mt-1">点击上方按钮添加履约节点</p>
    </div>
    
    <!-- 节点时间线 -->
    <div v-else class="space-y-4">
      <div
        v-for="(milestone, index) in milestones"
        :key="milestone.id"
        class="relative pl-8"
      >
        <!-- 时间线 -->
        <div class="absolute left-3 top-0 bottom-0 w-px bg-gray-200" v-if="index < milestones.length - 1" />
        
        <!-- 节点图标 -->
        <div 
          :class="[
            'absolute left-0 top-0 w-6 h-6 rounded-full flex items-center justify-center z-10',
            milestone.status === 'confirmed' ? 'bg-emerald-500' : 
            milestone.status === 'submitted' ? 'bg-amber-500' :
            milestone.status === 'rejected' ? 'bg-red-500' : 'bg-gray-300'
          ]"
        >
          <Check v-if="milestone.status === 'confirmed'" class="w-3.5 h-3.5 text-white" />
          <Clock v-else-if="milestone.status === 'submitted'" class="w-3.5 h-3.5 text-white" />
          <X v-else-if="milestone.status === 'rejected'" class="w-3.5 h-3.5 text-white" />
          <span v-else class="w-2 h-2 rounded-full bg-white" />
        </div>
        
        <!-- 节点卡片 -->
        <div class="bg-gray-50 rounded-xl p-4 hover:bg-gray-100/80 transition-colors">
          <div class="flex items-start justify-between gap-3">
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <component 
                  :is="getTypeInfo(milestone.milestoneType).icon"
                  :class="['w-4 h-4', getTypeInfo(milestone.milestoneType).color.split(' ')[1]]"
                />
                <span class="font-bold text-gray-900">{{ milestone.milestoneName }}</span>
                <span :class="['text-[10px] font-bold px-2 py-0.5 rounded-full', getStatusInfo(milestone.status).bgColor, getStatusInfo(milestone.status).color]">
                  {{ getStatusInfo(milestone.status).label }}
                </span>
              </div>
              
              <p v-if="milestone.description" class="text-sm text-gray-500 mb-2">
                {{ milestone.description }}
              </p>
              
              <div class="flex flex-wrap gap-x-4 gap-y-1 text-xs text-gray-400">
                <span v-if="milestone.expectedDate">预计：{{ formatDate(milestone.expectedDate) }}</span>
                <span v-if="milestone.actualDate">实际：{{ formatDate(milestone.actualDate) }}</span>
                <span v-if="milestone.operatorName">提交人：{{ milestone.operatorName }}</span>
                <span v-if="milestone.confirmUserName">确认人：{{ milestone.confirmUserName }}</span>
              </div>
              
              <!-- 凭证预览 -->
              <div v-if="milestone.evidenceUrl" class="mt-2">
                <a 
                  :href="milestone.evidenceUrl" 
                  target="_blank"
                  class="inline-flex items-center gap-1 text-xs text-blue-600 hover:text-blue-700"
                >
                  <Eye class="w-3 h-3" />
                  查看凭证
                </a>
              </div>
              
              <!-- 拒绝原因 -->
              <div v-if="milestone.status === 'rejected' && milestone.remark" class="mt-2 p-2 bg-red-50 rounded-lg">
                <div class="flex items-start gap-1.5 text-xs text-red-600">
                  <AlertCircle class="w-3.5 h-3.5 mt-0.5 shrink-0" />
                  <span>拒绝原因：{{ milestone.remark }}</span>
                </div>
              </div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="flex gap-1 shrink-0">
              <!-- 提交凭证 -->
              <button
                v-if="canSubmit(milestone)"
                class="p-1.5 bg-blue-50 hover:bg-blue-100 rounded-lg transition-colors"
                title="提交凭证"
                @click="emit('submit', milestone)"
              >
                <Upload class="w-4 h-4 text-blue-600" />
              </button>
              
              <!-- 确认 -->
              <button
                v-if="milestone.status === 'submitted'"
                class="p-1.5 bg-emerald-50 hover:bg-emerald-100 rounded-lg transition-colors"
                title="确认完成"
                @click="handleConfirm(milestone)"
              >
                <Check class="w-4 h-4 text-emerald-600" />
              </button>
              
              <!-- 拒绝 -->
              <button
                v-if="milestone.status === 'submitted'"
                class="p-1.5 bg-red-50 hover:bg-red-100 rounded-lg transition-colors"
                title="拒绝"
                @click="handleReject(milestone)"
              >
                <X class="w-4 h-4 text-red-500" />
              </button>
              
              <!-- 删除 -->
              <button
                v-if="canDelete(milestone)"
                class="p-1.5 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
                title="删除"
                @click="handleDelete(milestone)"
              >
                <Trash2 class="w-4 h-4 text-gray-500" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

