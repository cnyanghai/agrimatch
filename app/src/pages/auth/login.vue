<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '../../store/auth'

type AppAuthStep = 'PHONE' | 'PASSWORD_LOGIN' | 'SMS_LOGIN' | 'SUCCESS'

const authStore = useAuthStore()

const currentStep = ref<AppAuthStep>('PHONE')
const direction = ref<'forward' | 'backward'>('forward')
const phone = ref('')
const password = ref('')
const code = ref('')
const captchaKey = ref('')
const captchaImage = ref('')
const captchaCode = ref('')
const sending = ref(false)
const logging = ref(false)
const checking = ref(false)
const countdown = ref(0)
const agreed = ref(false)
const showPassword = ref(false)

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
    loadCaptcha()
  } else {
    currentStep.value = 'SMS_LOGIN'
    sendCode()
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
  loadCaptcha()
}

function goBackToPhone() {
  direction.value = 'backward'
  currentStep.value = 'PHONE'
  password.value = ''
  code.value = ''
  captchaCode.value = ''
}

// ========== Captcha ==========

async function loadCaptcha() {
  const result = await authStore.getCaptcha()
  if (result) {
    captchaKey.value = result.captchaKey
    captchaImage.value = result.captchaImage
  }
}

// ========== Password login ==========

async function handlePasswordLogin() {
  if (!password.value) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }
  if (!captchaCode.value) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  logging.value = true
  const result = await authStore.loginByPassword(phone.value, password.value, captchaKey.value, captchaCode.value)
  logging.value = false

  if (result.ok) {
    goToSuccess()
  } else {
    uni.showToast({ title: result.msg || '登录失败', icon: 'none' })
    loadCaptcha()
    captchaCode.value = ''
  }
}

// ========== SMS login ==========

async function sendCode() {
  if (countdown.value > 0) return
  sending.value = true
  const ok = await authStore.sendSmsCode(phone.value)
  sending.value = false
  if (ok) {
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
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
    <!-- ====== Header scene ====== -->
    <view class="scene">
      <!-- Deep gradient background -->
      <view class="scene__bg" />

      <!-- Subtle decorative circles -->
      <view class="scene__circle scene__circle--1" />
      <view class="scene__circle scene__circle--2" />
      <view class="scene__circle scene__circle--3" />

      <!-- Abstract mountain silhouettes -->
      <view class="scene__ridge scene__ridge--far" />
      <view class="scene__ridge scene__ridge--near" />

      <!-- Brand -->
      <view class="scene__brand">
        <view class="scene__brand-row">
          <view class="scene__logo">
            <image class="scene__logo-img" src="/static/logo-white.svg" mode="aspectFit" />
          </view>
          <text class="scene__title">沃谷</text>
        </view>
        <text class="scene__subtitle">农牧供需智能匹配平台</text>
      </view>

      <!-- Back button -->
      <view class="scene__nav" :style="{ paddingTop: statusBarHeight + 'px' }">
        <view v-if="canGoBack" class="scene__back" @tap="handleBack">
          <uni-icons type="left" size="18" color="#fff" />
        </view>
      </view>
    </view>

    <!-- ====== Form card (full-width tray) ====== -->
    <view class="form-card">
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
            <uni-icons v-if="stepIndex > i - 1" type="checkmarkempty" size="10" color="#fff" />
            <text v-else-if="stepIndex === i - 1" class="step-indicator__num">{{ i }}</text>
          </view>
          <view v-if="i < 3" class="step-indicator__line" :class="{ 'step-indicator__line--done': stepIndex >= i }" />
        </template>
      </view>

      <!-- ========== PHONE step ========== -->
      <view v-if="currentStep === 'PHONE'" class="step-content" :class="direction === 'forward' ? 'step-slide-left' : 'step-slide-right'">
        <text class="step-title">欢迎使用沃谷</text>
        <text class="step-subtitle">验证手机号即可登录，新用户自动注册</text>

        <view class="form-group">
          <view class="form-input-wrap" :class="{ 'form-input-wrap--error': phone.length > 0 && !isPhoneValid }">
            <uni-icons type="phone" size="18" color="#9ca3af" />
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
            <uni-icons v-if="agreed" type="checkmarkempty" size="11" color="#fff" />
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
            <uni-icons type="locked" size="18" color="#9ca3af" />
            <input
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              class="form-input"
              placeholder-class="form-placeholder"
            />
            <view class="input-eye" @tap="showPassword = !showPassword">
              <uni-icons :type="showPassword ? 'eye' : 'eye-slash'" size="18" color="#9ca3af" />
            </view>
          </view>
        </view>

        <view class="form-group form-group--captcha">
          <view class="form-input-wrap">
            <uni-icons type="image" size="18" color="#9ca3af" />
            <input
              v-model="captchaCode"
              type="text"
              maxlength="4"
              placeholder="验证码"
              class="form-input"
              placeholder-class="form-placeholder"
            />
          </view>
          <view class="captcha-box" @tap="loadCaptcha">
            <image
              v-if="captchaImage"
              :src="captchaImage"
              mode="aspectFit"
              class="captcha-img"
            />
            <text v-else class="captcha-loading">加载中</text>
          </view>
        </view>

        <button class="btn-primary" :disabled="logging || !password || !captchaCode" @tap="handlePasswordLogin">
          {{ logging ? '登录中...' : '登录' }}
        </button>

        <view class="step-links">
          <text class="step-links__item" @tap="switchToSms">改用短信验证码登录</text>
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
            <uni-icons type="locked" size="18" color="#9ca3af" />
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

      <!-- ========== SUCCESS step ========== -->
      <view v-else-if="currentStep === 'SUCCESS'" class="step-content step-slide-left">
        <view class="success-panel">
          <view class="success-panel__icon">
            <uni-icons type="checkmarkempty" size="44" color="#2D6A4F" />
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
  background: #fafaf9;
  position: relative;
  overflow-x: hidden;
}

// ==============================
// Header scene (professional)
// ==============================
.scene {
  position: relative;
  height: 480rpx;
  overflow: hidden;

  // Deep gradient background
  &__bg {
    position: absolute;
    inset: 0;
    background: linear-gradient(160deg, #0f2e1f 0%, #163b28 25%, #1a4532 50%, #2D6A4F 80%, #347a5c 100%);
    z-index: 0;
  }

  // Subtle decorative circles
  &__circle {
    position: absolute;
    border-radius: 50%;
    z-index: 1;

    &--1 {
      width: 360rpx;
      height: 360rpx;
      top: -100rpx;
      right: -100rpx;
      background: radial-gradient(circle, rgba(255,255,255,0.05) 0%, rgba(255,255,255,0) 70%);
    }

    &--2 {
      width: 240rpx;
      height: 240rpx;
      bottom: 20rpx;
      left: -60rpx;
      background: radial-gradient(circle, rgba(255,255,255,0.04) 0%, rgba(255,255,255,0) 70%);
    }

    &--3 {
      width: 160rpx;
      height: 160rpx;
      top: 60rpx;
      left: 200rpx;
      background: radial-gradient(circle, rgba(255,255,255,0.03) 0%, rgba(255,255,255,0) 70%);
    }
  }

  // Abstract mountain ridges
  &__ridge {
    position: absolute;
    left: -10%;
    width: 120%;
    border-radius: 50% 50% 0 0;

    &--far {
      height: 120rpx;
      bottom: -10rpx;
      background: rgba(0, 0, 0, 0.15);
      z-index: 2;
      animation: ridge-fade 0.8s ease-out 0.1s both;
    }

    &--near {
      height: 80rpx;
      bottom: -16rpx;
      background: rgba(0, 0, 0, 0.1);
      z-index: 3;
      animation: ridge-fade 0.8s ease-out 0.25s both;
    }
  }

  // Brand
  &__brand {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding-bottom: 40rpx;
    z-index: 5;
    animation: brand-fade 0.6s ease-out 0.15s both;
  }

  &__brand-row {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 16rpx;
  }

  &__logo {
    width: 76rpx;
    height: 76rpx;
    border-radius: 20rpx;
    background: rgba(255, 255, 255, 0.12);
    backdrop-filter: blur(8px);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__logo-img {
    width: 52rpx;
    height: 52rpx;
  }

  &__title {
    font-size: 52rpx;
    font-weight: 900;
    color: #fff;
    letter-spacing: 6rpx;
  }

  &__subtitle {
    font-size: 22rpx;
    color: rgba(255, 255, 255, 0.45);
    letter-spacing: 6rpx;
  }

  // Back button
  &__nav {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    z-index: 7;
    padding-left: 32rpx;
    padding-right: 32rpx;
  }

  &__back {
    margin-top: 16rpx;
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(8px);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.2s ease;

    &:active {
      transform: scale(0.92);
      background: rgba(255, 255, 255, 0.2);
    }
  }
}

// ==============================
// Form card (full-width tray)
// ==============================
.form-card {
  position: relative;
  margin-top: -60rpx;
  background: #fff;
  border-radius: 40rpx 40rpx 0 0;
  padding: 52rpx 48rpx 60rpx;
  box-shadow: 0 -4rpx 24rpx rgba(0,0,0,0.06);
  min-height: 55vh;
  z-index: 10;
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
    background: #e5e7eb;
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
    color: #fff;
  }

  &__line {
    width: 48rpx;
    height: 4rpx;
    background: #e5e7eb;
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
  color: #111827;
  margin-bottom: 8rpx;
}

.step-subtitle {
  display: block;
  font-size: 26rpx;
  color: #9ca3af;
  margin-bottom: 48rpx;
}

// ==============================
// Form elements
// ==============================
.form-group {
  margin-bottom: 32rpx;

  &--code, &--captcha {
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
  background: #f9fafb;
  border-radius: 20rpx;
  padding: 0 28rpx;
  border: 2rpx solid transparent;
  transition: all 0.2s ease;

  &--error {
    border-color: #ef4444;
    background: #fef2f2;
  }
}

.form-input {
  flex: 1;
  height: 96rpx;
  font-size: 30rpx;
  color: #111827;
}

.form-placeholder {
  color: #c4c9d2;
  font-size: 28rpx;
}

.form-hint {
  font-size: 22rpx;
  margin-top: 8rpx;
  padding-left: 16rpx;

  &--error {
    color: #ef4444;
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
  background: #f9fafb;
  border: 2rpx solid #e5e7eb;
  border-radius: 20rpx;
  font-size: 26rpx;
  color: $brand-600;
  font-weight: 600;
  text-align: center;
}

// ==============================
// Captcha
// ==============================
.captcha-box {
  width: 220rpx;
  height: 96rpx;
  border-radius: 20rpx;
  overflow: hidden;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.captcha-img {
  width: 100%;
  height: 100%;
}

.captcha-loading {
  font-size: 22rpx;
  color: #9ca3af;
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
    border: 2rpx solid #d1d5db;
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
    color: #9ca3af;
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
// Primary button
// ==============================
.btn-primary {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background: linear-gradient(135deg, #2D6A4F 0%, #3a8263 100%);
  color: #fff;
  border: none;
  border-radius: 20rpx;
  font-size: 32rpx;
  font-weight: 700;
  text-align: center;
  letter-spacing: 4rpx;
  box-shadow: 0 8rpx 24rpx rgba(45, 106, 79, 0.25);
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.97);
    opacity: 0.9;
  }

  &[disabled] {
    background: #e5e7eb;
    color: #9ca3af;
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
    color: #d1d5db;
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
    background: rgba(45, 106, 79, 0.08);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 36rpx;
    animation: success-bounce 0.6s ease-out;
  }

  &__title {
    font-size: 40rpx;
    font-weight: 900;
    color: #111827;
    margin-bottom: 12rpx;
  }

  &__subtitle {
    font-size: 26rpx;
    color: #9ca3af;
  }
}

// ==============================
// Bottom text (inside form card)
// ==============================
.bottom-text {
  display: flex;
  justify-content: center;
  margin-top: 60rpx;
  padding-bottom: 40rpx;

  &__content {
    font-size: 22rpx;
    color: #d1d5db;
    letter-spacing: 4rpx;
  }
}

// ==============================
// Animations (restrained)
// ==============================

// Ridge silhouette fade-in
@keyframes ridge-fade {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// Brand fade-in
@keyframes brand-fade {
  from {
    opacity: 0;
    transform: translateY(-16rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// Success bounce (preserved)
@keyframes success-bounce {
  0% { transform: scale(0); opacity: 0; }
  50% { transform: scale(1.15); opacity: 1; }
  75% { transform: scale(0.95); }
  100% { transform: scale(1); }
}

// Step slide animations (preserved)
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
