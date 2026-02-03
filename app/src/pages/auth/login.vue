<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '../../store/auth'

const phone = ref('')
const code = ref('')
const sending = ref(false)
const logging = ref(false)
const countdown = ref(0)
const agreed = ref(false)

const authStore = useAuthStore()

async function sendCode() {
  if (!phone.value || phone.value.length !== 11) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
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

async function handleLogin() {
  if (!phone.value || phone.value.length !== 11) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }
  if (!code.value) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议和隐私政策', icon: 'none' })
    return
  }

  logging.value = true
  const ok = await authStore.loginBySms(phone.value, code.value)
  logging.value = false

  if (ok) {
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      // 尝试返回上一页，如果没有上一页则跳转到首页
      const pages = getCurrentPages()
      if (pages.length > 1) {
        uni.navigateBack()
      } else {
        uni.switchTab({ url: '/pages/home/index' })
      }
    }, 500)
  } else {
    uni.showToast({ title: '验证码错误或已过期', icon: 'none' })
  }
}

function handleViewAgreement() {
  uni.showModal({ title: '用户协议', content: '沃谷用户协议详细内容（待完善）', showCancel: false })
}

function handleViewPrivacy() {
  uni.showModal({ title: '隐私政策', content: '沃谷隐私政策详细内容（待完善）', showCancel: false })
}
</script>

<template>
  <view class="login-page">
    <view class="login-card">
      <!-- Logo -->
      <view class="login-card__logo">
        <text class="login-card__logo-text">沃谷</text>
      </view>

      <text class="login-card__title">欢迎使用沃谷</text>
      <text class="login-card__subtitle">验证手机号即可登录，新用户自动注册</text>

      <view class="form-group">
        <view class="form-input-wrap" :class="{ 'form-input-wrap--error': phone.length > 0 && phone.length !== 11 }">
          <uni-icons type="phone" size="18" color="#999" />
          <input
            v-model="phone"
            type="number"
            maxlength="11"
            placeholder="请输入手机号"
            class="form-input"
          />
        </view>
        <text v-if="phone.length > 0 && phone.length !== 11" class="form-hint form-hint--error">请输入11位手机号</text>
      </view>

      <view class="form-group form-group--code">
        <view class="form-input-wrap">
          <uni-icons type="locked" size="18" color="#999" />
          <input
            v-model="code"
            type="number"
            maxlength="6"
            placeholder="验证码"
            class="form-input"
          />
        </view>
        <button
          class="btn-code"
          :disabled="countdown > 0 || sending || phone.length !== 11"
          @tap="sendCode"
        >
          {{ sending ? '发送中...' : countdown > 0 ? `${countdown}s` : '发送验证码' }}
        </button>
      </view>

      <!-- 用户协议 -->
      <view class="agreement">
        <view class="agreement__check" :class="{ 'agreement__check--active': agreed }" @tap="agreed = !agreed">
          <uni-icons v-if="agreed" type="checkmarkempty" size="12" color="#fff" />
        </view>
        <text class="agreement__text">
          我已阅读并同意
          <text class="agreement__link" @tap.stop="handleViewAgreement">《用户协议》</text>
          和
          <text class="agreement__link" @tap.stop="handleViewPrivacy">《隐私政策》</text>
        </text>
      </view>

      <button class="btn-login" :disabled="logging" @tap="handleLogin">
        {{ logging ? '登录中...' : '登录' }}
      </button>

      <!-- 底部提示 -->
      <view class="login-card__footer">
        <view class="login-card__divider">
          <view class="login-card__divider-line" />
          <text class="login-card__divider-text">其他方式</text>
          <view class="login-card__divider-line" />
        </view>
        <text class="login-card__web-hint">使用密码登录请访问网页版</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: $bg-page;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-lg;
}

.login-card {
  width: 100%;
  background: $bg-card;
  border-radius: $radius-xl;
  padding: $spacing-xl;

  &__logo {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background: $brand-600;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto $spacing-md auto;
  }

  &__logo-text {
    font-size: 40rpx;
    font-weight: bold;
    color: #fff;
  }

  &__title {
    font-size: $font-2xl;
    font-weight: bold;
    color: $brand-600;
    display: block;
    text-align: center;
    margin-bottom: $spacing-xs;
  }

  &__subtitle {
    font-size: $font-sm;
    color: $text-secondary;
    display: block;
    text-align: center;
    margin-bottom: $spacing-xl;
  }

  &__footer {
    margin-top: $spacing-xl;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $spacing-sm;
  }

  &__divider {
    display: flex;
    align-items: center;
    width: 100%;
    gap: $spacing-sm;
  }

  &__divider-line {
    flex: 1;
    height: 1rpx;
    background: $border-light;
  }

  &__divider-text {
    font-size: $font-xs;
    color: $text-placeholder;
  }

  &__web-hint {
    font-size: $font-xs;
    color: $text-placeholder;
  }
}

.form-group {
  margin-bottom: $spacing-md;

  &--code {
    display: flex;
    gap: $spacing-sm;
  }
}

.form-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  height: 88rpx;
  background: $bg-page;
  border-radius: $radius-lg;
  padding: 0 $spacing-md;
}

.form-input-wrap--error {
  border: 2rpx solid $color-error;
}

.form-input {
  flex: 1;
  height: 88rpx;
  font-size: $font-md;
}

.form-hint {
  font-size: $font-xs;
  margin-top: $spacing-xs;
  padding-left: $spacing-sm;

  &--error {
    color: $color-error;
  }
}

.btn-code {
  width: 240rpx;
  height: 88rpx;
  line-height: 88rpx;
  background: $bg-page;
  border: 1rpx solid $border-color;
  border-radius: $radius-lg;
  font-size: $font-sm;
  color: $brand-600;
}

.agreement {
  display: flex;
  align-items: center;
  margin-top: $spacing-sm;
  gap: $spacing-xs;

  &__check {
    width: 32rpx;
    height: 32rpx;
    border-radius: 50%;
    border: 2rpx solid $border-color;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &--active {
      background: $brand-600;
      border-color: $brand-600;
    }
  }

  &__text {
    font-size: $font-xs;
    color: $text-secondary;
  }

  &__link {
    color: $brand-600;
  }
}

.btn-login {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background: $brand-600;
  color: #fff;
  border: none;
  border-radius: $radius-lg;
  font-size: $font-lg;
  font-weight: bold;
  margin-top: $spacing-lg;
}
</style>
