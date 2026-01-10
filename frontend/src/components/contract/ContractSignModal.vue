<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Pen, Stamp, Type, Check, FileSignature, Package } from 'lucide-vue-next'
import { signContract, getContract, type ContractResponse } from '../../api/contract'
import { BaseModal, BaseButton } from '../ui'
import SignaturePad from './SignaturePad.vue'

const props = defineProps<{
  modelValue: boolean
  contractId: number | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'signed'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 签署方式
type SignMethod = 'typed' | 'handwrite' | 'seal'
const signMethod = ref<SignMethod>('typed')

// 表单数据
const typedName = ref('')
const signatureImage = ref('')
const loading = ref(false)
const contract = ref<ContractResponse | null>(null)

// 质量指标解析
const qualitySpecs = computed(() => {
  if (!contract.value) return []
  if (contract.value.productParams && contract.value.productParams.length > 0) {
    return contract.value.productParams.map(p => ({ name: p.label, value: p.value }))
  }
  if (contract.value.paramsJson) {
    try {
      const data = JSON.parse(contract.value.paramsJson)
      const params = data?.params || data || {}
      const result: Array<{ name: string; value: string }> = []
      
      const BLACKLIST = [
        'snapshotTime', 'priceType', 'id', 'categoryName', 'title', 
        'productName', 'companyName', 'nickName', 'exFactoryPrice', 'expectedPrice',
        'remainingQuantity', 'unit', 'basisQuotes', 'basisPrice', 
        'contractCode', 'futuresPrice', 'originPrice', 'shipAddress', 'purchaseAddress',
        'deliveryMode', 'storageMethod', 'packaging'
      ]

      const process = (k: string, v: any) => {
        if (BLACKLIST.includes(k) || /^\d+$/.test(k)) return
        if (v === null || v === undefined || v === '') return
        if (typeof v === 'object' && v.name && v.value) {
          result.push({ name: v.name, value: String(v.value) })
        } else if (typeof v !== 'object') {
          result.push({ name: k, value: String(v) })
        }
      }

      Object.entries(params).forEach(([k, v]) => {
        if (k === 'paramsJson' && typeof v === 'string') {
          try {
            const inner = JSON.parse(v)
            const nested = inner?.params || inner || {}
            Object.entries(nested).forEach(([nk, nv]) => process(nk, nv))
          } catch { /* ignore */ }
        } else {
          process(k, v)
        }
      })
      return result
    } catch { /* ignore */ }
  }
  return []
})

// 加载合同信息
watch(() => props.modelValue, async (val) => {
  if (val && props.contractId) {
    try {
      const res = await getContract(props.contractId)
      if (res.code === 0 && res.data) {
        contract.value = res.data
      }
    } catch (e) {
      console.error('加载合同失败', e)
    }
  } else {
    // 重置
    signMethod.value = 'typed'
    typedName.value = ''
    signatureImage.value = ''
    contract.value = null
  }
})

// 提交签署
async function handleSign() {
  if (!props.contractId) return
  
  // 验证
  if (signMethod.value === 'typed' && !typedName.value.trim()) {
    ElMessage.warning('请输入您的姓名')
    return
  }
  if (signMethod.value === 'handwrite' && !signatureImage.value) {
    ElMessage.warning('请在画布上签名')
    return
  }
  
  loading.value = true
  try {
    const res = await signContract(props.contractId, {
      signType: signMethod.value,
      signerName: typedName.value.trim() || undefined,
      signatureData: signatureImage.value || undefined
    })
    
    if (res.code === 0) {
      ElMessage.success('签署成功')
      emit('signed')
      visible.value = false
    } else {
      ElMessage.error(res.message || '签署失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '签署失败')
  } finally {
    loading.value = false
  }
}

// 接收手写签名
function onSignatureChange(data: string) {
  signatureImage.value = data
}
</script>

<template>
  <BaseModal
    v-model="visible"
    title="签署合同"
    subtitle="电子签署"
    size="md"
  >
    <!-- 头部图标 -->
    <template #icon>
      <div class="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center">
        <FileSignature class="w-5 h-5 text-emerald-600" />
      </div>
    </template>

    <!-- 合同信息 -->
    <div v-if="contract" class="bg-gray-50 rounded-2xl p-4 border border-gray-100 mb-5">
      <div class="text-xs text-gray-500 mb-1">合同编号</div>
      <div class="font-bold text-gray-900">{{ contract.contractNo }}</div>
      <div class="flex justify-between items-center mt-2 border-b border-gray-100 pb-3 mb-3">
        <span class="text-sm text-gray-600">{{ contract.productName }}</span>
        <span class="text-sm font-bold text-emerald-600">¥{{ contract.totalAmount?.toLocaleString() }}</span>
      </div>

      <!-- 规格参数摘要 -->
      <div v-if="qualitySpecs.length > 0" class="space-y-2">
        <div class="text-[10px] font-bold text-gray-400 uppercase tracking-widest flex items-center gap-1">
          <Package class="w-3 h-3" /> 质量标准
        </div>
        <div class="flex flex-wrap gap-2">
          <div v-for="spec in qualitySpecs" :key="spec.name" class="bg-white px-2 py-1 rounded-lg border border-gray-100 text-[10px] flex items-center gap-1.5 shadow-sm">
            <span class="text-gray-400 font-medium">{{ spec.name }}:</span>
            <span class="text-gray-700 font-bold">{{ spec.value }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 签署方式选择 -->
    <div class="space-y-4">
      <div class="text-xs font-bold text-gray-500 uppercase tracking-wider">选择签署方式</div>
      
      <div class="grid grid-cols-3 gap-3">
        <button
          :class="[
            'flex flex-col items-center gap-2 p-4 rounded-2xl border-2 transition-all',
            signMethod === 'typed' 
              ? 'border-emerald-500 bg-emerald-50' 
              : 'border-gray-100 hover:border-gray-200 bg-white'
          ]"
          @click="signMethod = 'typed'"
        >
          <div :class="['w-10 h-10 rounded-xl flex items-center justify-center', signMethod === 'typed' ? 'bg-emerald-100' : 'bg-gray-100']">
            <Type :class="['w-5 h-5', signMethod === 'typed' ? 'text-emerald-600' : 'text-gray-500']" />
          </div>
          <span :class="['text-xs font-bold', signMethod === 'typed' ? 'text-emerald-600' : 'text-gray-600']">打字签名</span>
        </button>
        
        <button
          :class="[
            'flex flex-col items-center gap-2 p-4 rounded-2xl border-2 transition-all',
            signMethod === 'handwrite' 
              ? 'border-emerald-500 bg-emerald-50' 
              : 'border-gray-100 hover:border-gray-200 bg-white'
          ]"
          @click="signMethod = 'handwrite'"
        >
          <div :class="['w-10 h-10 rounded-xl flex items-center justify-center', signMethod === 'handwrite' ? 'bg-emerald-100' : 'bg-gray-100']">
            <Pen :class="['w-5 h-5', signMethod === 'handwrite' ? 'text-emerald-600' : 'text-gray-500']" />
          </div>
          <span :class="['text-xs font-bold', signMethod === 'handwrite' ? 'text-emerald-600' : 'text-gray-600']">手写签名</span>
        </button>
        
        <button
          :class="[
            'flex flex-col items-center gap-2 p-4 rounded-2xl border-2 transition-all',
            signMethod === 'seal' 
              ? 'border-emerald-500 bg-emerald-50' 
              : 'border-gray-100 hover:border-gray-200 bg-white'
          ]"
          @click="signMethod = 'seal'"
        >
          <div :class="['w-10 h-10 rounded-xl flex items-center justify-center', signMethod === 'seal' ? 'bg-emerald-100' : 'bg-gray-100']">
            <Stamp :class="['w-5 h-5', signMethod === 'seal' ? 'text-emerald-600' : 'text-gray-500']" />
          </div>
          <span :class="['text-xs font-bold', signMethod === 'seal' ? 'text-emerald-600' : 'text-gray-600']">公司盖章</span>
        </button>
      </div>
      
      <!-- 打字签名 -->
      <div v-if="signMethod === 'typed'" class="mt-4">
        <label class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2 block">请输入您的真实姓名</label>
        <input
          v-model="typedName"
          type="text"
          placeholder="例如：张三"
          class="w-full px-4 py-2.5 border-2 border-gray-100 rounded-xl focus:border-emerald-500 outline-none transition-all text-center text-xl font-bold"
        />
        <p class="text-xs text-gray-400 mt-2 text-center">
          打字签名具有同等法律效力
        </p>
      </div>
      
      <!-- 手写签名 -->
      <div v-if="signMethod === 'handwrite'" class="mt-4">
        <label class="text-xs font-bold text-gray-500 uppercase tracking-wider mb-2 block">请在下方区域手写签名</label>
        <SignaturePad
          :width="360"
          :height="150"
          @update:model-value="onSignatureChange"
        />
      </div>
      
      <!-- 公司盖章 -->
      <div v-if="signMethod === 'seal'" class="mt-4">
        <div class="bg-amber-50 border border-amber-200 rounded-xl p-4 text-center">
          <p class="text-sm text-amber-700">
            公司电子章功能开发中，请先使用打字签名或手写签名
          </p>
        </div>
      </div>
    </div>
    
    <!-- 底部操作 -->
    <template #footer>
      <BaseButton type="secondary" block @click="visible = false">
        取消
      </BaseButton>
      <BaseButton 
        type="primary" 
        block
        :loading="loading" 
        :disabled="signMethod === 'seal'"
        @click="handleSign"
      >
        <Check class="w-4 h-4" />
        确认签署
      </BaseButton>
    </template>
  </BaseModal>
</template>
