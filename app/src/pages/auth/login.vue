<script setup lang="ts">
import { ref, computed } from 'vue'
import { BRAND_600, WARM_400, WHITE } from '../../constants/colors'
import { useAuthStore } from '../../store/auth'

type AppAuthStep = 'PHONE' | 'PASSWORD_LOGIN' | 'SMS_LOGIN' | 'REGISTER' | 'RESET_PASSWORD' | 'SUCCESS'

const authStore = useAuthStore()

const currentStep = ref<AppAuthStep>('PHONE')
const direction = ref<'forward' | 'backward'>('forward')
const phone = ref('')
const password = ref('')
const code = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const resetCode = ref('')
const resetNewPassword = ref('')
const resetConfirmPassword = ref('')
const sending = ref(false)
const logging = ref(false)
const checking = ref(false)
const registering = ref(false)
const resetting = ref(false)
const countdown = ref(0)
const resetCountdown = ref(0)
const agreed = ref(false)
const showPassword = ref(false)
const showNewPassword = ref(false)
const showResetPassword = ref(false)

const isPhoneValid = computed(() => /^1\d{10}$/.test(phone.value))

// 状态栏高度（用于返回按钮安全区）
const statusBarHeight = ref(44)
try {
  const sysInfo = uni.getSystemInfoSync()
  statusBarHeight.value = sysInfo.statusBarHeight || 44
} catch {}

// 是否有上一页可返回（首次启动时没有）
const canGoBack = computed(() => {
  const pages = getCurrentPages()
  return pages.length > 1
})

const stepIndex = computed(() => {
  if (currentStep.value === 'PHONE') return 0
  if (currentStep.value === 'SUCCESS') return 2
  return 1
})

// ========== Step navigation ==========

async function submitPhone() {
  if (!isPhoneValid.value) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议和隐私政策', icon: 'none' })
    return
  }

  checking.value = true
  const registered = await authStore.checkPhone(phone.value)
  checking.value = false

  direction.value = 'forward'
  if (registered) {
    currentStep.value = 'PASSWORD_LOGIN'
  } else {
    currentStep.value = 'REGISTER'
    sendRegisterCode()
  }
}

function switchToSms() {
  direction.value = 'forward'
  currentStep.value = 'SMS_LOGIN'
  sendCode()
}

function switchToPassword() {
  direction.value = 'backward'
  currentStep.value = 'PASSWORD_LOGIN'
}

function goBackToPhone() {
  direction.value = 'backward'
  currentStep.value = 'PHONE'
  password.value = ''
  code.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
  resetCode.value = ''
  resetNewPassword.value = ''
  resetConfirmPassword.value = ''
}

function switchToReset() {
  direction.value = 'forward'
  currentStep.value = 'RESET_PASSWORD'
  sendResetCode()
}

function backFromReset() {
  direction.value = 'backward'
  currentStep.value = 'PASSWORD_LOGIN'
  resetCode.value = ''
  resetNewPassword.value = ''
  resetConfirmPassword.value = ''
}

// ========== Password login ==========

async function handlePasswordLogin() {
  if (!password.value) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }
  logging.value = true
  const result = await authStore.loginByPassword(phone.value, password.value)
  logging.value = false

  if (result.ok) {
    goToSuccess()
  } else {
    uni.showToast({ title: result.msg || '登录失败', icon: 'none' })
  }
}

// ========== SMS login ==========

function startCountdown() {
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

async function sendCode() {
  if (countdown.value > 0) return
  sending.value = true
  const ok = await authStore.sendSmsCode(phone.value, 2)
  sending.value = false
  if (ok) {
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    startCountdown()
  } else {
    uni.showToast({ title: '发送失败，请稍后重试', icon: 'none' })
  }
}

async function handleSmsLogin() {
  if (!code.value) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  logging.value = true
  const ok = await authStore.loginBySms(phone.value, code.value)
  logging.value = false

  if (ok) {
    goToSuccess()
  } else {
    uni.showToast({ title: '验证码错误或已过期', icon: 'none' })
  }
}

// ========== Register (new user) ==========

async function sendRegisterCode() {
  if (countdown.value > 0) return
  sending.value = true
  const ok = await authStore.sendSmsCode(phone.value, 1)
  sending.value = false
  if (ok) {
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    startCountdown()
  } else {
    uni.showToast({ title: '发送失败，请稍后重试', icon: 'none' })
  }
}

async function handleRegister() {
  if (!code.value) {
    uni.showToast({ title: '请输入短信验证码', icon: 'none' })
    return
  }
  if (!newPassword.value || newPassword.value.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' })
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }
  registering.value = true
  const result = await authStore.register(phone.value, newPassword.value)
  registering.value = false

  if (result.ok) {
    goToSuccess()
  } else {
    uni.showToast({ title: result.msg || '注册失败', icon: 'none' })
  }
}

// ========== Reset Password ==========

async function sendResetCode() {
  if (resetCountdown.value > 0) return
  sending.value = true
  const ok = await authStore.sendSmsCode(phone.value, 3)
  sending.value = false
  if (ok) {
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    resetCountdown.value = 60
    const timer = setInterval(() => {
      resetCountdown.value--
      if (resetCountdown.value <= 0) clearInterval(timer)
    }, 1000)
  } else {
    uni.showToast({ title: '发送失败，请稍后重试', icon: 'none' })
  }
}

async function handleResetPassword() {
  if (!resetCode.value) {
    uni.showToast({ title: '请输入短信验证码', icon: 'none' })
    return
  }
  if (!resetNewPassword.value || resetNewPassword.value.length < 6) {
    uni.showToast({ title: '新密码至少6位', icon: 'none' })
    return
  }
  if (resetNewPassword.value !== resetConfirmPassword.value) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }
  resetting.value = true
  const result = await authStore.resetPassword(phone.value, resetCode.value, resetNewPassword.value)
  resetting.value = false

  if (result.ok) {
    uni.showToast({ title: '密码重置成功，请用新密码登录', icon: 'success' })
    backFromReset()
  } else {
    uni.showToast({ title: result.msg || '重置失败', icon: 'none' })
  }
}

// ========== Success ==========

function goToSuccess() {
  direction.value = 'forward'
  currentStep.value = 'SUCCESS'
  setTimeout(() => {
    uni.switchTab({ url: '/pages/home/index' })
  }, 1500)
}

// ========== Nav helpers ==========

function handleBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.switchTab({ url: '/pages/home/index' })
  }
}

// Web 端法律页面基础地址（根据实际部署地址调整）
const LEGAL_BASE_URL = 'https://www.wogucloud.com'

function handleViewAgreement() {
  const url = encodeURIComponent(`${LEGAL_BASE_URL}/legal/terms`)
  uni.navigateTo({ url: `/pages/common/webview?url=${url}` })
}

function handleViewPrivacy() {
  const url = encodeURIComponent(`${LEGAL_BASE_URL}/legal/privacy`)
  uni.navigateTo({ url: `/pages/common/webview?url=${url}` })
}
</script>

<template>
  <view class="login-page">
    <!-- Navigation -->
    <view class="nav" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view v-if="canGoBack" class="nav__back" @tap="handleBack">
        <WgIcon name="left" :size="18" :color="WHITE" />
      </view>
    </view>

    <!-- Brand (small, centered) -->
    <view class="brand">
      <view class="brand__icon">
        <image class="brand__logo" src="/static/logo-white.svg" mode="aspectFit" />
      </view>
      <text class="brand__name">沃谷</text>
      <text class="brand__subtitle">农牧供需智能匹配平台</text>
    </view>

    <!-- Form area -->
    <view class="form-area">
      <!-- Step indicator -->
      <view class="step-indicator">
        <template v-for="i in 3" :key="i">
          <view
            class="step-indicator__dot"
            :class="{
              'step-indicator__dot--active': stepIndex === i - 1,
              'step-indicator__dot--done': stepIndex > i - 1
            }"
          >
            <WgIcon v-if="stepIndex > i - 1" name="check" :size="10" :color="WHITE" />
            <text v-else-if="stepIndex === i - 1" class="step-indicator__num">{{ i }}</text>
          </view>
          <view v-if="i < 3" class="step-indicator__line" :class="{ 'step-indicator__line--done': stepIndex >= i }" />
        </template>
      </view>

      <!-- ========== PHONE step ========== -->
      <view v-if="currentStep === 'PHONE'" class="step-content" :class="direction === 'forward' ? 'step-slide-left' : 'step-slide-right'">
        <text class="step-title">欢迎使用沃谷</text>
        <text class="step-subtitle">输入手机号，已有账号直接登录，新用户注册</text>

        <view class="form-group">
          <view class="form-input-wrap" :class="{ 'form-input-wrap--error': phone.length > 0 && !isPhoneValid }">
            <WgIcon name="phone" :size="18" :color="WARM_400" />
            <input
              v-model="phone"
              type="number"
              maxlength="11"
              placeholder="请输入手机号"
              class="form-input"
              placeholder-class="form-placeholder"
            />
          </view>
          <text v-if="phone.length > 0 && !isPhoneValid" class="form-hint form-hint--error">请输入11位手机号</text>
        </view>

        <!-- Agreement -->
        <view class="agreement">
          <view class="agreement__check" :class="{ 'agreement__check--active': agreed }" @tap="agreed = !agreed">
            <WgIcon v-if="agreed" name="check" :size="11" :color="WHITE" />
          </view>
          <view class="agreement__body">
            <text class="agreement__text" @tap="agreed = !agreed">我已阅读并同意</text>
            <text class="agreement__link" @tap="handleViewAgreement">《用户协议》</text>
            <text class="agreement__text" @tap="agreed = !agreed">和</text>
            <text class="agreement__link" @tap="handleViewPrivacy">《隐私政策》</text>
          </view>
        </view>

        <button class="btn-primary" :disabled="!isPhoneValid || !agreed || checking" @tap="submitPhone">
          {{ checking ? '验证中...' : '继续' }}
        </button>
      </view>

      <!-- ========== PASSWORD_LOGIN step ========== -->
      <view v-else-if="currentStep === 'PASSWORD_LOGIN'" class="step-content" :class="direction === 'forward' ? 'step-slide-left' : 'step-slide-right'">
        <text class="step-title">密码登录</text>
        <text class="step-subtitle">{{ phone }}</text>

        <view class="form-group">
          <view class="form-input-wrap">
            <WgIcon name="lock" :size="18" :color="WARM_400" />
            <input
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              class="form-input"
              placeholder-class="form-placeholder"
            />
            <view class="input-eye" @tap="showPassword = !showPassword">
              <WgIcon :name="showPassword ? 'eye' : 'eye-off'" :size="18" :color="WARM_400" />
            </view>
          </view>
        </view>

        <button class="btn-primary" :disabled="logging || !password" @tap="handlePasswordLogin">
          {{ logging ? '登录中...' : '登录' }}
        </button>

        <view class="step-links">
          <text class="step-links__item" @tap="switchToReset">忘记密码</text>
          <text class="step-links__divider">|</text>
          <text class="step-links__item" @tap="switchToSms">短信登录</text>
          <text class="step-links__divider">|</text>
          <text class="step-links__item" @tap="goBackToPhone">返回</text>
        </view>
      </view>

      <!-- ========== SMS_LOGIN step ========== -->
      <view v-else-if="currentStep === 'SMS_LOGIN'" class="step-content" :class="direction === 'forward' ? 'step-slide-left' : 'step-slide-right'">
        <text class="step-title">输入验证码</text>
        <text class="step-subtitle">验证码已发送至 {{ phone }}</text>

        <view class="form-group form-group--code">
          <view class="form-input-wrap">
            <WgIcon name="lock" :size="18" :color="WARM_400" />
            <input
              v-model="code"
              type="number"
              maxlength="6"
              placeholder="短信验证码"
              class="form-input"
              placeholder-class="form-placeholder"
            />
          </view>
          <button
            class="btn-code"
            :disabled="countdown > 0 || sending"
            @tap="sendCode"
          >
            {{ sending ? '发送中' : countdown > 0 ? `${countdown}s` : '重新发送' }}
          </button>
        </view>

        <button class="btn-primary" :disabled="logging || !code" @tap="handleSmsLogin">
          {{ logging ? '登录中...' : '登录' }}
        </button>

        <view class="step-links">
          <text class="step-links__item" @tap="switchToPassword">改用密码登录</text>
          <text class="step-links__divider">|</text>
          <text class="step-links__item" @tap="goBackToPhone">返回</text>
        </view>
      </view>

      <!-- ========== REGISTER step (new user) ========== -->
      <view v-else-if="currentStep === 'REGISTER'" class="step-content" :class="direction === 'forward' ? 'step-slide-left' : 'step-slide-right'">
        <text class="step-title">注册新账号</text>
        <text class="step-subtitle">验证码已发送至 {{ phone }}</text>

        <view class="form-group form-group--code">
          <view class="form-input-wrap">
            <WgIcon name="mail" :size="18" :color="WARM_400" />
            <input
              v-model="code"
              type="number"
              maxlength="6"
              placeholder="短信验证码"
              class="form-input"
              placeholder-class="form-placeholder"
            />
          </view>
          <button
            class="btn-code"
            :disabled="countdown > 0 || sending"
            @tap="sendRegisterCode"
          >
            {{ sending ? '发送中' : countdown > 0 ? `${countdown}s` : '重新发送' }}
          </button>
        </view>

        <view class="form-group">
          <view class="form-input-wrap">
            <WgIcon name="lock" :size="18" :color="WARM_400" />
            <input
              v-model="newPassword"
              :type="showNewPassword ? 'text' : 'password'"
              placeholder="设置密码（至少6位）"
              class="form-input"
              placeholder-class="form-placeholder"
            />
            <view class="input-eye" @tap="showNewPassword = !showNewPassword">
              <WgIcon :name="showNewPassword ? 'eye' : 'eye-off'" :size="18" :color="WARM_400" />
            </view>
          </view>
        </view>

        <view class="form-group">
          <view class="form-input-wrap" :class="{ 'form-input-wrap--error': confirmPassword && newPassword !== confirmPassword }">
            <WgIcon name="lock" :size="18" :color="WARM_400" />
            <input
              v-model="confirmPassword"
              type="password"
              placeholder="确认密码"
              class="form-input"
              placeholder-class="form-placeholder"
            />
          </view>
          <text v-if="confirmPassword && newPassword !== confirmPassword" class="form-hint form-hint--error">两次密码不一致</text>
        </view>

        <button
          class="btn-primary"
          :disabled="registering || !code || !newPassword || newPassword.length < 6 || newPassword !== confirmPassword"
          @tap="handleRegister"
        >
          {{ registering ? '注册中...' : '注册' }}
        </button>

        <view class="step-links">
          <text class="step-links__item" @tap="goBackToPhone">返回</text>
        </view>
      </view>

      <!-- ========== RESET_PASSWORD step ========== -->
      <view v-else-if="currentStep === 'RESET_PASSWORD'" class="step-content" :class="direction === 'forward' ? 'step-slide-left' : 'step-slide-right'">
        <text class="step-title">重置密码</text>
        <text class="step-subtitle">验证码已发送至 {{ phone }}</text>

        <view class="form-group form-group--code">
          <view class="form-input-wrap">
            <WgIcon name="mail" :size="18" :color="WARM_400" />
            <input
              v-model="resetCode"
              type="number"
              maxlength="6"
              placeholder="短信验证码"
              class="form-input"
              placeholder-class="form-placeholder"
            />
          </view>
          <button
            class="btn-code"
            :disabled="resetCountdown > 0 || sending"
            @tap="sendResetCode"
          >
            {{ sending ? '发送中' : resetCountdown > 0 ? `${resetCountdown}s` : '重新发送' }}
          </button>
        </view>

        <view class="form-group">
          <view class="form-input-wrap">
            <WgIcon name="lock" :size="18" :color="WARM_400" />
            <input
              v-model="resetNewPassword"
              :type="showResetPassword ? 'text' : 'password'"
              placeholder="新密码（至少6位）"
              class="form-input"
              placeholder-class="form-placeholder"
            />
            <view class="input-eye" @tap="showResetPassword = !showResetPassword">
              <WgIcon :name="showResetPassword ? 'eye' : 'eye-off'" :size="18" :color="WARM_400" />
            </view>
          </view>
        </view>

        <view class="form-group">
          <view class="form-input-wrap" :class="{ 'form-input-wrap--error': resetConfirmPassword && resetNewPassword !== resetConfirmPassword }">
            <WgIcon name="lock" :size="18" :color="WARM_400" />
            <input
              v-model="resetConfirmPassword"
              type="password"
              placeholder="确认新密码"
              class="form-input"
              placeholder-class="form-placeholder"
            />
          </view>
          <text v-if="resetConfirmPassword && resetNewPassword !== resetConfirmPassword" class="form-hint form-hint--error">两次密码不一致</text>
        </view>

        <button
          class="btn-primary"
          :disabled="resetting || !resetCode || !resetNewPassword || resetNewPassword.length < 6 || resetNewPassword !== resetConfirmPassword"
          @tap="handleResetPassword"
        >
          {{ resetting ? '重置中...' : '重置密码' }}
        </button>

        <view class="step-links">
          <text class="step-links__item" @tap="backFromReset">返回登录</text>
        </view>
      </view>

      <!-- ========== SUCCESS step ========== -->
      <view v-else-if="currentStep === 'SUCCESS'" class="step-content step-slide-left">
        <view class="success-panel">
          <view class="success-panel__icon">
            <WgIcon name="check" :size="44" :color="BRAND_600" />
          </view>
          <text class="success-panel__title">登录成功</text>
          <text class="success-panel__subtitle">正在为您跳转...</text>
        </view>
      </view>

      <!-- Bottom tagline -->
      <view class="bottom-text">
        <text class="bottom-text__content">让农牧交易更简单</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(175deg, $brand-700 0%, $brand-600 30%, $warm-50 30.1%);
  position: relative;
  overflow-x: hidden;
}

// ==============================
// Navigation
// ==============================
.nav {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 7;
  padding-left: 32rpx;
  padding-right: 32rpx;

  &__back {
    margin-top: 16rpx;
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.15);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.2s ease;

    &:active {
      transform: scale(0.92);
      background: rgba(255, 255, 255, 0.25);
    }
  }
}

// ==============================
// Brand (compact, centered)
// ==============================
.brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
  padding-bottom: 60rpx;
  animation: brand-enter 0.5s ease-out;

  &__icon {
    width: 96rpx;
    height: 96rpx;
    border-radius: 28rpx;
    background: rgba(255, 255, 255, 0.2);
    border: 2rpx solid rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(20rpx);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: $spacing-md;
    box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.1);
  }

  &__logo {
    width: 56rpx;
    height: 56rpx;
  }

  &__name {
    font-size: $font-3xl;
    font-weight: 900;
    color: $text-inverse;
    letter-spacing: 6rpx;
    margin-bottom: 8rpx;
  }

  &__subtitle {
    font-size: $font-sm;
    color: rgba(255, 255, 255, 0.7);
    letter-spacing: 4rpx;
  }
}

// ==============================
// Form area
// ==============================
.form-area {
  background: $bg-card;
  border-radius: $radius-2xl $radius-2xl 0 0;
  padding: 52rpx 48rpx 60rpx;
  box-shadow: 0 -4rpx 24rpx rgba(45, 106, 79, 0.08);
  min-height: 55vh;
  position: relative;
  z-index: 2;
}

// ==============================
// Step indicator
// ==============================
.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-bottom: 48rpx;

  &__dot {
    width: 40rpx;
    height: 40rpx;
    border-radius: 50%;
    background: $warm-200;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;

    &--active {
      background: $brand-600;
      box-shadow: 0 4rpx 16rpx rgba(45, 106, 79, 0.35);
      transform: scale(1.15);
    }

    &--done {
      background: rgba(45, 106, 79, 0.65);
    }
  }

  &__num {
    font-size: 20rpx;
    font-weight: 700;
    color: $text-inverse;
  }

  &__line {
    width: 48rpx;
    height: 4rpx;
    background: $warm-200;
    margin: 0 8rpx;
    border-radius: 4rpx;
    transition: all 0.3s ease;

    &--done {
      background: rgba(45, 106, 79, 0.4);
    }
  }
}

// ==============================
// Step content
// ==============================
.step-title {
  display: block;
  font-size: 40rpx;
  font-weight: 900;
  color: $text-primary;
  margin-bottom: 8rpx;
}

.step-subtitle {
  display: block;
  font-size: 26rpx;
  color: $text-placeholder;
  margin-bottom: 48rpx;
}

// ==============================
// Form elements
// ==============================
.form-group {
  margin-bottom: 32rpx;

  &--code {
    display: flex;
    gap: 16rpx;
  }
}

.form-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16rpx;
  height: 96rpx;
  background: $warm-100;
  border-radius: 20rpx;
  padding: 0 28rpx;
  border: 2rpx solid transparent;
  transition: all 0.2s ease;

  &--error {
    border-color: $color-error;
    background: #fef2f2;
  }
}

.form-input {
  flex: 1;
  height: 96rpx;
  font-size: 30rpx;
  color: $text-primary;
}

.form-placeholder {
  color: $warm-300;
  font-size: 28rpx;
}

.form-hint {
  font-size: 22rpx;
  margin-top: 8rpx;
  padding-left: 16rpx;

  &--error {
    color: $color-error;
  }
}

.input-eye {
  padding: 8rpx;
  flex-shrink: 0;
}

.btn-code {
  width: 220rpx;
  height: 96rpx;
  line-height: 96rpx;
  background: $warm-100;
  border: 2rpx solid $warm-200;
  border-radius: 20rpx;
  font-size: 26rpx;
  color: $brand-600;
  font-weight: 600;
  text-align: center;
}

// ==============================
// Agreement
// ==============================
.agreement {
  display: flex;
  align-items: flex-start;
  margin-bottom: 40rpx;
  gap: 12rpx;
  padding: 4rpx 0;

  &__check {
    width: 36rpx;
    height: 36rpx;
    border-radius: 10rpx;
    border: 2rpx solid $warm-300;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    margin-top: 2rpx;
    transition: all 0.2s ease;

    &--active {
      background: $brand-600;
      border-color: $brand-600;
    }
  }

  &__body {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
  }

  &__text {
    font-size: 23rpx;
    color: $text-placeholder;
    line-height: 1.6;
  }

  &__link {
    font-size: 23rpx;
    color: $brand-600;
    font-weight: 600;
    line-height: 1.6;
  }
}

// ==============================
// Primary button (flat, no gradient)
// ==============================
.btn-primary {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background: $brand-600;
  color: $text-inverse;
  border: none;
  border-radius: $radius-full;
  font-size: 32rpx;
  font-weight: 700;
  text-align: center;
  letter-spacing: 4rpx;
  box-shadow: $shadow-brand;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.97);
    box-shadow: 0 2rpx 8rpx rgba(45, 106, 79, 0.15);
  }

  &[disabled] {
    background: $warm-200;
    color: $warm-400;
    box-shadow: none;
  }
}

// ==============================
// Step links (bottom of form)
// ==============================
.step-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  margin-top: 36rpx;
  padding: 16rpx;

  &__item {
    font-size: 26rpx;
    color: $brand-600;
    font-weight: 500;
  }

  &__divider {
    font-size: 22rpx;
    color: $warm-300;
  }
}

// ==============================
// Success panel
// ==============================
.success-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0;

  &__icon {
    width: 140rpx;
    height: 140rpx;
    border-radius: 50%;
    background: $brand-50;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 36rpx;
    animation: success-bounce 0.6s ease-out;
  }

  &__title {
    font-size: 40rpx;
    font-weight: 900;
    color: $text-primary;
    margin-bottom: 12rpx;
  }

  &__subtitle {
    font-size: 26rpx;
    color: $text-placeholder;
  }
}

// ==============================
// Bottom text (inside form area)
// ==============================
.bottom-text {
  display: flex;
  justify-content: center;
  margin-top: 60rpx;
  padding-bottom: 40rpx;

  &__content {
    font-size: 22rpx;
    color: $warm-300;
    letter-spacing: 4rpx;
  }
}

// ==============================
// Animations
// ==============================
@keyframes brand-enter {
  from {
    opacity: 0;
    transform: translateY(-20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes success-bounce {
  0% { transform: scale(0); opacity: 0; }
  50% { transform: scale(1.15); opacity: 1; }
  75% { transform: scale(0.95); }
  100% { transform: scale(1); }
}

@keyframes step-slide-left-in {
  from { transform: translateX(60rpx); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}

@keyframes step-slide-right-in {
  from { transform: translateX(-60rpx); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}

.step-slide-left {
  animation: step-slide-left-in 0.3s ease-out;
}

.step-slide-right {
  animation: step-slide-right-in 0.3s ease-out;
}
</style>
