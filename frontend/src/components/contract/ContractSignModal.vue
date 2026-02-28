<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { showToast } from '@/composables/useToast'
import { Stamp, Check, FileSignature, Package, ShieldCheck } from 'lucide-vue-next'
import { signContract, getContract, type ContractResponse, type SealResponse } from '../../api/contract'
import { sendSmsCode } from '../../api/sms'
import { BaseModal, BaseButton } from '../ui'
import SealManager from './SealManager.vue'
import { auditLogger } from '../../utils/audit-logger'
import { ErrorHandler } from '../../utils/error-handler'
import { useAuthStore } from '../../store/auth'

const props = defineProps<{
  modelValue: boolean
  contractId: number | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'signed'): void
}>()

const auth = useAuthStore()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 表单数据
const loading = ref(false)
const contract = ref<ContractResponse | null>(null)

// 盖章相关
const selectedSeal = ref<SealResponse | null>(null)
const smsCode = ref('')
const smsSending = ref(false)
const smsCountdown = ref(0)

// 获取签署方手机号（部分脱敏显示）
const signerPhone = computed(() => {
  if (!contract.value || !auth.me?.companyId) return ''
  const isBuyer = auth.me.companyId === contract.value.buyerCompanyId
  const phone = isBuyer ? contract.value.buyerPhone : contract.value.sellerPhone
  return phone || ''
})

const maskedPhone = computed(() => {
  const p = signerPhone.value
  if (!p || p.length < 7) return p
  return p.slice(0, 3) + '****' + p.slice(-4)
})

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
    contract.value = null
    selectedSeal.value = null
    smsCode.value = ''
    smsCountdown.value = 0
  }
})

// 发送短信验证码
async function handleSendSms() {
  if (smsSending.value || smsCountdown.value > 0) return
  if (!signerPhone.value) {
    showToast.warning('未找到签署方手机号，请先在公司资料中完善联系电话')
    return
  }

  smsSending.value = true
  try {
    const res = await sendSmsCode(signerPhone.value, 4)
    if (res.code === 0) {
      showToast.success('验证码已发送')
      smsCountdown.value = 60
      const timer = setInterval(() => {
        smsCountdown.value--
        if (smsCountdown.value <= 0) clearInterval(timer)
      }, 1000)
    } else {
      showToast.error(res.message || '发送失败')
    }
  } catch (e: any) {
    showToast.error(e?.response?.data?.message || e?.message || '发送失败')
  } finally {
    smsSending.value = false
  }
}

// 选择印章
function onSealSelect(seal: SealResponse) {
  selectedSeal.value = seal
}

// 提交签署
async function handleSign() {
  if (!props.contractId) return

  if (!selectedSeal.value) {
    showToast.warning('请选择一枚公章')
    return
  }
  if (!smsCode.value || smsCode.value.length !== 6) {
    showToast.warning('请输入6位短信验证码')
    return
  }

  loading.value = true
  try {
    const payload = {
      signType: 'seal' as const,
      sealId: selectedSeal.value.id,
      smsCode: smsCode.value
    }

    auditLogger.log({
      userId: auth.me?.userId ?? 0,
      action: 'contract_sign_prepared',
      resourceType: 'contract',
      resourceId: props.contractId,
      details: {
        contractId: props.contractId,
        signType: 'seal',
        sealId: selectedSeal.value.id
      }
    })

    const res = await signContract(props.contractId, payload)

    if (res.code === 0) {
      auditLogger.log({
        userId: auth.me?.userId ?? 0,
        action: 'contract_signed',
        resourceType: 'contract',
        resourceId: props.contractId,
        details: {
          contractId: props.contractId,
          signType: 'seal',
          success: true
        }
      })
      showToast.success('签署成功')
      emit('signed')
      visible.value = false
    } else {
      auditLogger.log({
        userId: auth.me?.userId ?? 0,
        action: 'contract_signed',
        resourceType: 'contract',
        resourceId: props.contractId,
        details: {
          contractId: props.contractId,
          signType: 'seal',
          success: false,
          error: res.message
        }
      })
      showToast.error(res.message || '签署失败')
    }
  } catch (e: any) {
    ErrorHandler.handle(e)
    auditLogger.log({
      userId: auth.me?.userId ?? 0,
      action: 'contract_sign_error',
      resourceType: 'contract',
      resourceId: props.contractId,
      details: {
        contractId: props.contractId,
        error: e?.message || '未知错误'
      }
    })
    showToast.error(e.message || '签署失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <BaseModal
    v-model="visible"
    title="签署合同"
    subtitle="公章签署"
    size="md"
  >
    <!-- 头部图标 -->
    <template #icon>
      <div class="w-10 h-10 rounded-xl bg-brand-50 flex items-center justify-center">
        <FileSignature class="w-5 h-5 text-brand-600" />
      </div>
    </template>

    <!-- 合同信息 -->
    <div v-if="contract" class="bg-neutral-50 rounded-xl p-4 border border-neutral-200 mb-5">
      <div class="text-xs text-neutral-500 mb-1">合同编号</div>
      <div class="font-bold text-neutral-900">{{ contract.contractNo }}</div>
      <div class="flex justify-between items-center mt-2 border-b border-neutral-200 pb-3 mb-3">
        <span class="text-sm text-neutral-600">{{ contract.productName }}</span>
        <span class="text-sm font-bold text-brand-600">¥{{ contract.totalAmount?.toLocaleString() }}</span>
      </div>

      <!-- 规格参数摘要 -->
      <div v-if="qualitySpecs.length > 0" class="space-y-2">
        <div class="text-[10px] font-bold text-neutral-400 uppercase tracking-widest flex items-center gap-1">
          <Package class="w-3 h-3" /> 质量标准
        </div>
        <div class="flex flex-wrap gap-2">
          <div v-for="spec in qualitySpecs" :key="spec.name" class="bg-white px-2 py-1 rounded-lg border border-neutral-200 text-[10px] flex items-center gap-1.5 shadow-sm">
            <span class="text-neutral-400 font-medium">{{ spec.name }}:</span>
            <span class="text-neutral-700 font-bold">{{ spec.value }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 签署流程：选择印章 + 短信验证 -->
    <div class="space-y-5">
      <!-- Step 1: 选择印章 -->
      <div>
        <div class="flex items-center gap-2 mb-3">
          <div class="w-6 h-6 rounded-full bg-brand-600 text-white flex items-center justify-center text-xs font-bold">1</div>
          <span class="text-sm font-bold text-neutral-700">选择签署印章</span>
          <Stamp class="w-4 h-4 text-neutral-400" />
        </div>
        <SealManager
          selectable
          :selected="selectedSeal?.id ?? null"
          @select="onSealSelect"
        />
      </div>

      <!-- Step 2: 短信验证 -->
      <div>
        <div class="flex items-center gap-2 mb-3">
          <div class="w-6 h-6 rounded-full bg-brand-600 text-white flex items-center justify-center text-xs font-bold">2</div>
          <span class="text-sm font-bold text-neutral-700">短信验证确认</span>
          <ShieldCheck class="w-4 h-4 text-neutral-400" />
        </div>
        <div class="bg-neutral-50 border border-neutral-200 rounded-xl p-4">
          <p class="text-xs text-neutral-500 mb-3">
            为确保签署安全，需向手机号 <span class="font-bold text-neutral-700">{{ maskedPhone || '未绑定' }}</span> 发送验证码确认
          </p>
          <div class="flex gap-3">
            <input
              v-model="smsCode"
              type="text"
              inputmode="numeric"
              maxlength="6"
              placeholder="请输入6位验证码"
              class="flex-1 px-4 py-2.5 border-2 border-neutral-200 rounded-xl focus:border-brand-500 outline-none transition-all text-center text-lg font-bold tracking-widest"
            />
            <button
              class="px-4 py-2.5 bg-brand-600 hover:bg-brand-700 text-white font-bold rounded-xl transition-all whitespace-nowrap disabled:opacity-50 disabled:cursor-not-allowed active:scale-95 text-sm"
              :disabled="smsSending || smsCountdown > 0 || !signerPhone"
              @click="handleSendSms"
            >
              {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
            </button>
          </div>
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
        :disabled="!selectedSeal || smsCode.length !== 6"
        @click="handleSign"
      >
        <Check class="w-4 h-4" />
        确认盖章签署
      </BaseButton>
    </template>
  </BaseModal>
</template>
