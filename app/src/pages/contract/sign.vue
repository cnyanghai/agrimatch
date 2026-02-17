<script setup lang="ts">
/**
 * 合同签署页面 - 支持多种签署方式
 *
 * 签署方式：
 * 1. seal - 印章签署（选择已有印章或创建新印章）
 * 2. handwrite - 手写签名（canvas画布）
 * 3. typed - 输入签名（输入姓名生成）
 * 4. seal_handwrite - 印章+手写组合
 */
import { ref, computed, nextTick } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  getContract,
  signContract,
  listSeals,
  createSeal,
  getDefaultSeal,
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

// ==================== 数据 ====================
const contractId = ref(0)
const contract = ref<ContractResponse | null>(null)
const loading = ref(true)
const submitting = ref(false)

// SMS 验证码相关
const myCompany = ref<CompanyResponse | null>(null)
const smsCode = ref('')
const smsSending = ref(false)
const smsCountdown = ref(0)
let smsTimer: ReturnType<typeof setInterval> | null = null

// 签署方式
type SignMethod = 'seal' | 'handwrite' | 'typed' | 'seal_handwrite'
const signMethod = ref<SignMethod>('seal')

const signMethods: Array<{ value: SignMethod; label: string; desc: string }> = [
  { value: 'seal', label: '印章签署', desc: '使用公司电子印章' },
  { value: 'handwrite', label: '手写签名', desc: '在画布上手写签名' },
  { value: 'typed', label: '输入签名', desc: '输入姓名生成签名' },
  { value: 'seal_handwrite', label: '印章+手写', desc: '印章与手写组合签署' },
]

// 印章相关
const seals = ref<SealResponse[]>([])
const selectedSealId = ref<number | null>(null)
const sealsLoading = ref(false)
const showCreateSeal = ref(false)
const newSealName = ref('')
const creatingSeal = ref(false)

// 手写签名相关
const canvasCtx = ref<any>(null)
const canvasReady = ref(false)
const isDrawing = ref(false)
const hasDrawn = ref(false)
const lastX = ref(0)
const lastY = ref(0)

// 输入签名相关
const typedName = ref('')
const signerTitle = ref('')

// ==================== 生命周期 ====================
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
    // 加载公司信息以获取手机号（用于SMS验证码）
    try {
      myCompany.value = await getMyCompany() ?? null
    } catch {
      // non-critical
    }
  } catch {
    // handled by request.ts
  } finally {
    loading.value = false
  }
}

// ==================== SMS 验证码 ====================
/** 是否需要SMS验证（seal / seal_handwrite 必须） */
const needsSms = computed(() => {
  return signMethod.value === 'seal' || signMethod.value === 'seal_handwrite'
})

/** 脱敏手机号 */
const maskedPhone = computed(() => {
  const phone = myCompany.value?.phone
  if (!phone || phone.length < 7) return phone || '未绑定'
  return phone.slice(0, 3) + '****' + phone.slice(-4)
})

/** 发送验证码 */
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
    startCountdown()
  } catch {
    // handled by request.ts
  } finally {
    smsSending.value = false
  }
}

function startCountdown() {
  smsCountdown.value = 60
  if (smsTimer) clearInterval(smsTimer)
  smsTimer = setInterval(() => {
    smsCountdown.value--
    if (smsCountdown.value <= 0) {
      if (smsTimer) clearInterval(smsTimer)
      smsTimer = null
    }
  }, 1000)
}

// ==================== 印章管理 ====================
async function loadSeals() {
  sealsLoading.value = true
  try {
    seals.value = await listSeals()
    // 自动选择默认印章
    const defaultSeal = seals.value.find(s => s.isDefault)
    if (defaultSeal) {
      selectedSealId.value = defaultSeal.id
    } else if (seals.value.length > 0) {
      selectedSealId.value = seals.value[0].id
    }
  } catch {
    seals.value = []
  } finally {
    sealsLoading.value = false
  }
}

function selectSeal(id: number) {
  selectedSealId.value = id
}

async function handleCreateSeal() {
  if (!newSealName.value.trim()) {
    uni.showToast({ title: '请输入印章名称', icon: 'none' })
    return
  }
  creatingSeal.value = true
  try {
    const req: SealCreateRequest = {
      sealName: newSealName.value.trim(),
      sealType: 'company',
      generate: true,
    }
    await createSeal(req)
    uni.showToast({ title: '印章创建成功', icon: 'success' })
    showCreateSeal.value = false
    newSealName.value = ''
    await loadSeals()
  } catch {
    // handled by request.ts
  } finally {
    creatingSeal.value = false
  }
}

/** 印章上传器回调 */
async function handleSealCreated(result: string) {
  showCreateSeal.value = false
  if (!result) return // 用户取消

  if (result.startsWith('__GENERATE__:')) {
    // 系统生成模式
    const sealNameStr = result.replace('__GENERATE__:', '')
    try {
      await createSeal({
        sealName: sealNameStr,
        sealType: 'company',
        generate: true,
      })
      uni.showToast({ title: '印章生成成功', icon: 'success' })
      await loadSeals()
    } catch {
      // handled
    }
  } else {
    // 上传提取模式 - result 是上传后的 URL
    try {
      await createSeal({
        sealName: newSealName.value.trim() || '上传印章',
        sealType: 'company',
        sealUrl: result,
        generate: false,
      })
      uni.showToast({ title: '印章创建成功', icon: 'success' })
      await loadSeals()
    } catch {
      // handled
    }
  }
}

// ==================== 手写签名 Canvas ====================
function initCanvas() {
  nextTick(() => {
    const ctx = uni.createCanvasContext('signCanvas', undefined)
    canvasCtx.value = ctx
    // 设置白色背景
    ctx.setFillStyle('#ffffff')
    ctx.fillRect(0, 0, 600, 300)
    ctx.draw()
    canvasReady.value = true
  })
}

function onTouchStart(e: any) {
  if (!canvasCtx.value) return
  isDrawing.value = true
  const touch = e.touches[0]
  lastX.value = touch.x
  lastY.value = touch.y
}

function onTouchMove(e: any) {
  if (!isDrawing.value || !canvasCtx.value) return
  const touch = e.touches[0]
  const ctx = canvasCtx.value

  ctx.beginPath()
  ctx.setStrokeStyle('#000000')
  ctx.setLineWidth(3)
  ctx.setLineCap('round')
  ctx.setLineJoin('round')
  ctx.moveTo(lastX.value, lastY.value)
  ctx.lineTo(touch.x, touch.y)
  ctx.stroke()
  ctx.draw(true)

  lastX.value = touch.x
  lastY.value = touch.y
  hasDrawn.value = true
}

function onTouchEnd() {
  isDrawing.value = false
}

function clearCanvas() {
  if (!canvasCtx.value) return
  const ctx = canvasCtx.value
  ctx.setFillStyle('#ffffff')
  ctx.fillRect(0, 0, 600, 300)
  ctx.draw()
  hasDrawn.value = false
}

function getCanvasDataUrl(): Promise<string> {
  return new Promise((resolve, reject) => {
    uni.canvasToTempFilePath({
      canvasId: 'signCanvas',
      fileType: 'png',
      quality: 0.9,
      success(res) {
        // #ifdef APP-PLUS
        // APP-PLUS 模式：通过 plus.io 读取临时文件为 base64
        plus.io.resolveLocalFileSystemURL(
          res.tempFilePath,
          (entry: any) => {
            entry.file((file: any) => {
              const reader = new plus.io.FileReader()
              reader.onloadend = function(e: any) {
                resolve(e.target.result as string)
              }
              reader.onerror = function() {
                reject(new Error('读取签名文件失败'))
              }
              reader.readAsDataURL(file)
            }, () => reject(new Error('获取文件对象失败')))
          },
          () => reject(new Error('文件路径解析失败'))
        )
        // #endif
        // #ifdef H5
        // H5 模式：canvasToTempFilePath 直接返回 base64 data URL
        resolve(res.tempFilePath)
        // #endif
      },
      fail(err) {
        reject(err)
      },
    })
  })
}

// ==================== 提交签署 ====================
const canSubmit = computed(() => {
  if (submitting.value) return false
  // 需要SMS验证码的签署方式必须输入6位验证码
  if (needsSms.value && smsCode.value.trim().length !== 6) return false
  switch (signMethod.value) {
    case 'seal':
      return selectedSealId.value != null
    case 'handwrite':
      return hasDrawn.value
    case 'typed':
      return typedName.value.trim().length > 0
    case 'seal_handwrite':
      return selectedSealId.value != null && hasDrawn.value
    default:
      return false
  }
})

async function handleSubmit() {
  if (!canSubmit.value || !contract.value) return

  // 签署确认
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
      signType: signMethod.value,
      signerName: typedName.value.trim() || authStore.user?.nickName || authStore.user?.userName || '',
      signerTitle: signerTitle.value.trim() || undefined,
    }

    // 盖章类签署需附带短信验证码
    if (needsSms.value) {
      req.smsCode = smsCode.value.trim()
    }

    switch (signMethod.value) {
      case 'seal':
        req.sealId = selectedSealId.value || undefined
        break
      case 'handwrite':
        req.signatureData = await getCanvasDataUrl()
        break
      case 'typed':
        req.typedName = typedName.value.trim()
        break
      case 'seal_handwrite':
        req.sealId = selectedSealId.value || undefined
        req.signatureData = await getCanvasDataUrl()
        break
    }

    await signContract(contractId.value, req)
    uni.showToast({ title: '签署成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 800)
  } catch {
    // handled by request.ts
  } finally {
    submitting.value = false
  }
}

// 选择签署方式时初始化 canvas
function onMethodChange(method: SignMethod) {
  signMethod.value = method
  if (method === 'handwrite' || method === 'seal_handwrite') {
    nextTick(() => initCanvas())
  }
}
</script>

<template>
  <view class="sign-page">
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

      <!-- 签署方式选择 -->
      <view class="method-section">
        <text class="method-section__title">选择签署方式</text>
        <view class="method-grid">
          <view
            v-for="m in signMethods"
            :key="m.value"
            class="method-card"
            :class="{ 'method-card--active': signMethod === m.value }"
            @tap="onMethodChange(m.value)"
          >
            <text class="method-card__label">{{ m.label }}</text>
            <text class="method-card__desc">{{ m.desc }}</text>
          </view>
        </view>
      </view>

      <!-- ========== 印章签署 ========== -->
      <view v-if="signMethod === 'seal' || signMethod === 'seal_handwrite'" class="sign-section">
        <view class="sign-section__header">
          <text class="sign-section__step">{{ signMethod === 'seal_handwrite' ? '1' : '' }}</text>
          <text class="sign-section__title">选择签署印章</text>
        </view>

        <view v-if="sealsLoading" class="sign-section__loading">
          <text class="sign-section__loading-text">加载印章中...</text>
        </view>

        <view v-else-if="seals.length === 0" class="sign-section__empty">
          <text class="sign-section__empty-text">暂无印章，请先创建</text>
        </view>

        <view v-else class="seal-grid">
          <view
            v-for="seal in seals"
            :key="seal.id"
            class="seal-item"
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
              <WgIcon name="check" :size="14" color="#fff" :stroke-width="3" />
            </view>
          </view>
        </view>

        <!-- 创建新印章 -->
        <view class="create-seal-btn" @tap="showCreateSeal = true">
          <WgIcon name="plus-circle" :size="16" color="#2D6A4F" />
          <text class="create-seal-btn__text">添加印章（拍照提取 / 系统生成）</text>
        </view>

        <!-- 印章上传器（底部面板） -->
        <WgSealUploader
          :model-value="showCreateSeal"
          @created="handleSealCreated"
        />
      </view>

      <!-- ========== SMS 验证码（盖章类签署必填） ========== -->
      <view v-if="needsSms" class="sign-section">
        <view class="sign-section__header">
          <text class="sign-section__title">短信验证</text>
        </view>

        <view class="sms-area">
          <!-- 手机号显示 -->
          <view class="sms-area__phone-row">
            <text class="sms-area__label">验证手机</text>
            <text class="sms-area__phone">{{ maskedPhone }}</text>
          </view>

          <!-- 验证码输入 + 发送按钮 -->
          <view class="sms-area__input-row">
            <input
              v-model="smsCode"
              class="sms-area__input"
              type="number"
              placeholder="请输入6位验证码"
              :maxlength="6"
            />
            <view
              class="sms-area__send-btn"
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

      <!-- ========== 手写签名 ========== -->
      <view v-if="signMethod === 'handwrite' || signMethod === 'seal_handwrite'" class="sign-section">
        <view class="sign-section__header">
          <text class="sign-section__step">{{ signMethod === 'seal_handwrite' ? '2' : '' }}</text>
          <text class="sign-section__title">手写签名</text>
        </view>

        <view class="canvas-wrapper">
          <canvas
            canvas-id="signCanvas"
            class="sign-canvas"
            disable-scroll
            @touchstart="onTouchStart"
            @touchmove="onTouchMove"
            @touchend="onTouchEnd"
          />
          <view v-if="!hasDrawn" class="canvas-tip">
            <text class="canvas-tip__text">请在此区域手写签名</text>
          </view>
        </view>

        <view class="canvas-actions">
          <view class="canvas-actions__btn" @tap="clearCanvas">
            <text class="canvas-actions__btn-text">清除重写</text>
          </view>
        </view>
      </view>

      <!-- ========== 输入签名 ========== -->
      <view v-if="signMethod === 'typed'" class="sign-section">
        <view class="sign-section__header">
          <text class="sign-section__title">输入签名</text>
        </view>

        <view class="typed-form">
          <view class="typed-form__field">
            <text class="typed-form__label">签署人姓名 <text class="typed-form__required">*</text></text>
            <input
              v-model="typedName"
              class="typed-form__input"
              placeholder="请输入签署人姓名"
              :maxlength="20"
            />
          </view>
          <view class="typed-form__field">
            <text class="typed-form__label">职务（可选）</text>
            <input
              v-model="signerTitle"
              class="typed-form__input"
              placeholder="如：总经理"
              :maxlength="20"
            />
          </view>

          <!-- 签名预览 -->
          <view v-if="typedName.trim()" class="typed-preview">
            <text class="typed-preview__label">签名预览</text>
            <view class="typed-preview__box">
              <text class="typed-preview__name">{{ typedName.trim() }}</text>
              <text v-if="signerTitle.trim()" class="typed-preview__title">{{ signerTitle.trim() }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 法律声明 -->
      <view class="legal-notice">
        <text class="legal-notice__text">
          点击"确认签署"即表示您已阅读并同意合同条款，签署后合同将具有法律效力。
        </text>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-placeholder" />

      <!-- 提交按钮 -->
      <view class="submit-bar safe-area-bottom">
        <view
          class="submit-btn"
          :class="{ 'submit-btn--disabled': !canSubmit }"
          @tap="handleSubmit"
        >
          <text class="submit-btn__text">{{ submitting ? '签署中...' : '确认签署' }}</text>
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

/* ===== 合同摘要 ===== */
.sign-summary {
  background: $brand-700;
  padding: $spacing-lg $spacing-md;
  color: #fff;

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

/* ===== 签署方式选择 ===== */
.method-section {
  padding: $spacing-md;

  &__title {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
    display: block;
    margin-bottom: $spacing-sm;
  }
}

.method-grid {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.method-card {
  flex: 1;
  min-width: 44%;
  background: $bg-card;
  border: 3rpx solid $border-color;
  border-radius: $radius-lg;
  padding: $spacing-sm $spacing-md;
  transition: border-color $transition-fast;

  &--active {
    border-color: $brand-600;
    background: $brand-50;
  }

  &__label {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
    display: block;
  }

  &__desc {
    font-size: $font-xs;
    color: $text-secondary;
    display: block;
    margin-top: 4rpx;
  }
}

/* ===== 签署区块 ===== */
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
    width: 40rpx;
    height: 40rpx;
    border-radius: 50%;
    background: $brand-600;
    color: #fff;
    font-size: $font-sm;
    font-weight: bold;
    text-align: center;
    line-height: 40rpx;
  }

  &__title {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
  }

  &__loading,
  &__empty {
    padding: $spacing-lg;
    text-align: center;
  }

  &__loading-text,
  &__empty-text {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

/* ===== 印章网格 ===== */
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

  /* check icon handled by WgIcon */
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
  transition: all $transition-fast;

  &:active {
    background: $brand-100;
  }

  &__text {
    font-size: $font-sm;
    color: $brand-600;
    font-weight: 600;
  }
}

/* 印章上传弹窗样式由 WgSealUploader 组件自带 */

/* ===== SMS 验证码区域 ===== */
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

    &:active {
      transform: scale(0.95);
    }
  }

  &__send-text {
    font-size: $font-sm;
    font-weight: 600;
    color: #fff;
  }

  &__tip {
    font-size: $font-xs;
    color: $text-placeholder;
    display: block;
    margin-top: $spacing-sm;
  }
}

/* ===== 手写签名画布 ===== */
.canvas-wrapper {
  position: relative;
  width: 100%;
  height: 300rpx;
  border: 2rpx solid $border-color;
  border-radius: $radius-md;
  overflow: hidden;
  background: #ffffff;
}

.sign-canvas {
  width: 100%;
  height: 300rpx;
}

.canvas-tip {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;

  &__text {
    font-size: $font-lg;
    color: $text-placeholder;
    letter-spacing: 4rpx;
  }
}

.canvas-actions {
  margin-top: $spacing-sm;
  display: flex;
  justify-content: flex-end;

  &__btn {
    padding: $spacing-xs $spacing-md;
    background: $bg-page;
    border-radius: $radius-md;
    border: 1rpx solid $border-color;
  }

  &__btn-text {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

/* ===== 输入签名表单 ===== */
.typed-form {
  &__field {
    padding: $spacing-sm 0;
    border-bottom: 1rpx solid $border-light;

    &:last-child {
      border-bottom: none;
    }
  }

  &__label {
    font-size: $font-sm;
    font-weight: 600;
    color: $text-primary;
    display: block;
    margin-bottom: $spacing-xs;
  }

  &__required {
    color: $color-error;
  }

  &__input {
    width: 100%;
    height: 80rpx;
    font-size: $font-md;
    color: $text-primary;
    padding: 0;
  }
}

.typed-preview {
  margin-top: $spacing-md;

  &__label {
    font-size: $font-sm;
    color: $text-secondary;
    display: block;
    margin-bottom: $spacing-xs;
  }

  &__box {
    padding: $spacing-lg;
    background: $bg-page;
    border-radius: $radius-md;
    text-align: center;
  }

  &__name {
    font-size: 60rpx;
    font-weight: bold;
    color: $text-primary;
    font-family: 'STKaiti', 'KaiTi', 'STSong', serif;
    display: block;
  }

  &__title {
    font-size: $font-sm;
    color: $text-secondary;
    display: block;
    margin-top: $spacing-xs;
  }
}

/* ===== 法律声明 ===== */
.legal-notice {
  padding: $spacing-md;

  &__text {
    font-size: $font-xs;
    color: $text-secondary;
    line-height: 1.6;
  }
}

/* ===== 底部占位 ===== */
.bottom-placeholder {
  height: 140rpx;
}

/* ===== 提交按钮 ===== */
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
  transition: transform $transition-fast;

  &:active {
    transform: scale(0.95);
  }

  &--disabled {
    opacity: 0.5;
  }

  &__text {
    font-size: $font-lg;
    font-weight: bold;
    color: #fff;
  }
}
</style>
