<script setup lang="ts">
import { ref, computed } from 'vue'
import { BRAND_600, WHITE } from '../../constants/colors'
import { onLoad } from '@dcloudio/uni-app'
import {
  getContract,
  signContract,
  listSeals,
  createSeal,
  sendSignSmsCode,
  type ContractResponse,
  type ContractSignRequest,
  type SealResponse,
  type SealCreateRequest,
} from '../../api/contract'
import { useAuthStore } from '../../store/auth'
import { getMyCompany, type CompanyResponse } from '../../api/company'
import WgSealUploader from '../../components/WgSealUploader.vue'

const authStore = useAuthStore()

const contractId = ref(0)
const contract = ref<ContractResponse | null>(null)
const loading = ref(true)
const submitting = ref(false)

const myCompany = ref<CompanyResponse | null>(null)
const smsCode = ref('')
const smsSending = ref(false)
const smsCountdown = ref(0)
let smsTimer: ReturnType<typeof setInterval> | null = null

const seals = ref<SealResponse[]>([])
const selectedSealId = ref<number | null>(null)
const sealsLoading = ref(false)
const showCreateSeal = ref(false)

onLoad(async (options) => {
  if (options?.id) {
    contractId.value = Number(options.id)
    await loadData()
  }
})

async function loadData() {
  loading.value = true
  try {
    contract.value = await getContract(contractId.value)
    await loadSeals()
    try {
      myCompany.value = await getMyCompany() ?? null
    } catch { /* non-critical */ }
  } catch { /* handled by request.ts */ }
  finally { loading.value = false }
}

// ==================== SMS 验证码 ====================

const maskedPhone = computed(() => {
  const phone = myCompany.value?.phone
  if (!phone || phone.length < 7) return phone || '未绑定'
  return phone.slice(0, 3) + '****' + phone.slice(-4)
})

async function handleSendSms() {
  const phone = myCompany.value?.phone
  if (!phone) {
    uni.showToast({ title: '公司未绑定手机号', icon: 'none' })
    return
  }
  if (smsSending.value || smsCountdown.value > 0) return

  smsSending.value = true
  try {
    await sendSignSmsCode(phone)
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    smsCountdown.value = 60
    if (smsTimer) clearInterval(smsTimer)
    smsTimer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) {
        if (smsTimer) clearInterval(smsTimer)
        smsTimer = null
      }
    }, 1000)
  } catch { /* handled */ }
  finally { smsSending.value = false }
}

// ==================== 印章管理 ====================

async function loadSeals() {
  sealsLoading.value = true
  try {
    seals.value = await listSeals()
    const defaultSeal = seals.value.find(s => s.isDefault)
    if (defaultSeal) {
      selectedSealId.value = defaultSeal.id
    } else if (seals.value.length > 0) {
      selectedSealId.value = seals.value[0].id
    }
  } catch { seals.value = [] }
  finally { sealsLoading.value = false }
}

function selectSeal(id: number) {
  selectedSealId.value = id
}

async function handleSealCreated(result: string) {
  showCreateSeal.value = false
  if (!result) return

  try {
    await createSeal({ sealName: '上传印章', sealType: 'company', sealUrl: result, generate: false })
    uni.showToast({ title: '印章创建成功', icon: 'success' })
    await loadSeals()
  } catch { /* handled */ }
}

// ==================== 提交签署 ====================

const canSubmit = computed(() => {
  if (submitting.value) return false
  if (smsCode.value.trim().length !== 6) return false
  return selectedSealId.value != null
})

async function handleSubmit() {
  if (!canSubmit.value || !contract.value) return

  const confirmed = await new Promise<boolean>((resolve) => {
    uni.showModal({
      title: '确认签署',
      content: '签署后合同将具有法律效力，确认签署此合同？',
      confirmText: '确认签署',
      success: (res) => resolve(res.confirm),
    })
  })
  if (!confirmed) return

  submitting.value = true
  try {
    const req: ContractSignRequest = {
      signType: 'seal',
      sealId: selectedSealId.value || undefined,
      smsCode: smsCode.value.trim(),
      signerName: authStore.user?.nickName || authStore.user?.userName || '',
    }

    await signContract(contractId.value, req)
    uni.showToast({ title: '签署成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 800)
  } catch { /* handled by request.ts */ }
  finally { submitting.value = false }
}
</script>

<template>
  <view class="sign-page">
    <WgNavBar title="签署合同" />

    <WgSkeleton v-if="loading" type="detail" />

    <template v-else-if="contract">
      <!-- 合同摘要 -->
      <view class="sign-summary">
        <text class="sign-summary__no">{{ contract.contractNo }}</text>
        <view class="sign-summary__info">
          <text class="sign-summary__product">{{ contract.productName || '-' }}</text>
          <text class="sign-summary__amount">{{ contract.totalAmount ? ('¥' + contract.totalAmount.toLocaleString()) : '待定' }}</text>
        </view>
      </view>

      <!-- 步骤 1：选择印章 -->
      <view class="sign-section">
        <view class="sign-section__header">
          <view class="sign-section__step"><text class="sign-section__step-text">1</text></view>
          <text class="sign-section__title">选择签署印章</text>
        </view>

        <view v-if="sealsLoading" class="sign-section__loading">
          <text class="sign-section__loading-text">加载印章中...</text>
        </view>

        <view v-else-if="seals.length === 0" class="sign-section__empty">
          <WgIcon name="stamp" :size="40" color="#ccc" />
          <text class="sign-section__empty-text">暂无印章，请先创建</text>
        </view>

        <view v-else class="seal-grid">
          <view
            v-for="seal in seals"
            :key="seal.id"
            class="seal-item tap-feedback"
            :class="{ 'seal-item--active': selectedSealId === seal.id }"
            @tap="selectSeal(seal.id)"
          >
            <image
              v-if="seal.sealUrl"
              :src="seal.sealUrl"
              class="seal-item__img"
              mode="aspectFit"
            />
            <view v-else class="seal-item__placeholder">
              <text class="seal-item__placeholder-text">{{ seal.sealName.slice(0, 2) }}</text>
            </view>
            <text class="seal-item__name">{{ seal.sealName }}</text>
            <text v-if="seal.isDefault" class="seal-item__default">默认</text>
            <view v-if="selectedSealId === seal.id" class="seal-item__check">
              <WgIcon name="check" :size="14" :color="WHITE" :stroke-width="3" />
            </view>
          </view>
        </view>

        <view class="create-seal-btn tap-feedback" @tap="showCreateSeal = true">
          <WgIcon name="plus-circle" :size="16" :color="BRAND_600" />
          <text class="create-seal-btn__text">添加印章（拍照提取）</text>
        </view>

        <WgSealUploader
          :model-value="showCreateSeal"
          @created="handleSealCreated"
        />
      </view>

      <!-- 步骤 2：短信验证 -->
      <view class="sign-section">
        <view class="sign-section__header">
          <view class="sign-section__step"><text class="sign-section__step-text">2</text></view>
          <text class="sign-section__title">短信验证</text>
        </view>

        <view class="sms-area">
          <view class="sms-area__phone-row">
            <text class="sms-area__label">验证手机</text>
            <text class="sms-area__phone">{{ maskedPhone }}</text>
          </view>

          <view class="sms-area__input-row">
            <input
              v-model="smsCode"
              class="sms-area__input"
              type="number"
              placeholder="请输入6位验证码"
              :maxlength="6"
            />
            <view
              class="sms-area__send-btn tap-feedback"
              :class="{ 'sms-area__send-btn--disabled': smsSending || smsCountdown > 0 || !myCompany?.phone }"
              @tap="handleSendSms"
            >
              <text class="sms-area__send-text">
                {{ smsSending ? '发送中...' : smsCountdown > 0 ? `${smsCountdown}s` : '发送验证码' }}
              </text>
            </view>
          </view>

          <text class="sms-area__tip">盖章签署需要短信验证以确保安全</text>
        </view>
      </view>

      <!-- 法律声明 -->
      <view class="legal-notice">
        <WgIcon name="shield-check" :size="14" color="#999" />
        <text class="legal-notice__text">
          点击"确认签署"即表示您已阅读并同意合同条款，签署后合同将具有法律效力。
        </text>
      </view>

      <view class="bottom-placeholder" />

      <!-- 提交按钮 -->
      <view class="submit-bar safe-area-bottom">
        <view
          class="submit-btn tap-feedback"
          :class="{ 'submit-btn--disabled': !canSubmit }"
          @tap="handleSubmit"
        >
          <WgIcon name="stamp" :size="18" :color="WHITE" />
          <text class="submit-btn__text">{{ submitting ? '签署中...' : '确认盖章签署' }}</text>
        </view>
      </view>
    </template>
  </view>
</template>

<style lang="scss" scoped>
.sign-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: env(safe-area-inset-bottom);
}

.sign-summary {
  background: $brand-700;
  padding: $spacing-lg $spacing-md;
  color: $text-inverse;

  &__no {
    font-size: $font-sm;
    opacity: 0.7;
    display: block;
    margin-bottom: $spacing-xs;
  }

  &__info {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
  }

  &__product {
    font-size: $font-lg;
    font-weight: bold;
  }

  &__amount {
    font-size: $font-xl;
    font-weight: bold;
    color: $autumn-300;
  }
}

.sign-section {
  background: $bg-card;
  margin: $spacing-sm $spacing-md;
  border-radius: $radius-lg;
  padding: $spacing-md $spacing-lg;

  &__header {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    margin-bottom: $spacing-md;
  }

  &__step {
    width: 44rpx;
    height: 44rpx;
    border-radius: 50%;
    background: $brand-600;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__step-text {
    font-size: $font-sm;
    font-weight: bold;
    color: $text-inverse;
  }

  &__title {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
  }

  &__loading,
  &__empty {
    padding: $spacing-xl;
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $spacing-sm;
  }

  &__loading-text,
  &__empty-text {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

.seal-grid {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.seal-item {
  width: calc(33.33% - #{$spacing-sm});
  background: $bg-page;
  border-radius: $radius-md;
  padding: $spacing-sm;
  text-align: center;
  position: relative;
  border: 3rpx solid transparent;
  transition: border-color $transition-fast, background $transition-fast;

  &--active {
    border-color: $brand-600;
    background: $brand-50;
  }

  &__img {
    width: 120rpx;
    height: 120rpx;
    display: block;
    margin: 0 auto $spacing-xs;
  }

  &__placeholder {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    border: 3rpx solid #c53030;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto $spacing-xs;
    opacity: 0.6;
  }

  &__placeholder-text {
    font-size: $font-md;
    font-weight: bold;
    color: #c53030;
  }

  &__name {
    font-size: $font-xs;
    color: $text-primary;
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__default {
    font-size: 18rpx;
    color: $brand-600;
    display: block;
    margin-top: 2rpx;
  }

  &__check {
    position: absolute;
    top: 8rpx;
    right: 8rpx;
    width: 32rpx;
    height: 32rpx;
    border-radius: 50%;
    background: $brand-600;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.create-seal-btn {
  margin-top: $spacing-md;
  padding: $spacing-sm $spacing-md;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  border: 2rpx dashed $brand-300;
  border-radius: $radius-md;
  background: $brand-50;

  &__text {
    font-size: $font-sm;
    color: $brand-600;
    font-weight: 600;
  }
}

.sms-area {
  &__phone-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-md;
    padding-bottom: $spacing-sm;
    border-bottom: 1rpx solid $border-light;
  }

  &__label {
    font-size: $font-sm;
    color: $text-secondary;
  }

  &__phone {
    font-size: $font-md;
    font-weight: 600;
    color: $text-primary;
    letter-spacing: 2rpx;
  }

  &__input-row {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__input {
    flex: 1;
    height: 88rpx;
    border: 2rpx solid $border-color;
    border-radius: $radius-md;
    padding: 0 $spacing-md;
    font-size: $font-lg;
    color: $text-primary;
    letter-spacing: 8rpx;
    text-align: center;
  }

  &__send-btn {
    width: 220rpx;
    height: 88rpx;
    border-radius: $radius-md;
    background: $brand-600;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &--disabled {
      opacity: 0.5;
    }
  }

  &__send-text {
    font-size: $font-sm;
    font-weight: 600;
    color: $text-inverse;
  }

  &__tip {
    font-size: $font-xs;
    color: $text-placeholder;
    display: block;
    margin-top: $spacing-sm;
  }
}

.legal-notice {
  display: flex;
  align-items: flex-start;
  gap: $spacing-xs;
  padding: $spacing-md;

  &__text {
    flex: 1;
    font-size: $font-xs;
    color: $text-secondary;
    line-height: 1.6;
  }
}

.bottom-placeholder {
  height: 140rpx;
}

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: $spacing-sm $spacing-md;
  background: $bg-card;
  border-top: 1rpx solid $border-light;
  z-index: 10;
}

.submit-btn {
  width: 100%;
  height: 96rpx;
  background: $brand-600;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;

  &--disabled {
    opacity: 0.5;
  }

  &__text {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-inverse;
  }
}
</style>
