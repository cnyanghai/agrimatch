<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { BRAND_600 } from '../../constants/colors'
import { useAuthStore } from '../../store/auth'

const authStore = useAuthStore()
const phone = computed(() => authStore.user?.phonenumber || '')

const smsCode = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const submitting = ref(false)

const countdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

onUnmounted(() => { if (timer) clearInterval(timer) })

const maskedPhone = computed(() => {
  const p = phone.value
  if (p.length >= 11) return p.slice(0, 3) + '****' + p.slice(-4)
  return p || '未绑定手机号'
})

async function sendCode() {
  if (countdown.value > 0 || !phone.value) return
  const ok = await authStore.sendSmsCode(phone.value, 3)
  if (ok) {
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0 && timer) { clearInterval(timer); timer = null }
    }, 1000)
  } else {
    uni.showToast({ title: '发送失败，请稍后重试', icon: 'none' })
  }
}

async function handleSubmit() {
  if (!smsCode.value) {
    uni.showToast({ title: '请输入验证码', icon: 'none' }); return
  }
  if (!newPassword.value || newPassword.value.length < 6) {
    uni.showToast({ title: '新密码至少6位', icon: 'none' }); return
  }
  if (newPassword.value !== confirmPassword.value) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' }); return
  }
  submitting.value = true
  try {
    const { ok, msg } = await authStore.resetPassword(phone.value, smsCode.value, newPassword.value)
    if (ok) {
      uni.showToast({ title: '密码修改成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 1200)
    } else {
      uni.showToast({ title: msg || '修改失败', icon: 'none' })
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="change-pwd-page">
    <WgNavBar title="修改密码" />
    <view class="form-card stitch-card">
      <view class="section-tip">
        <WgIcon name="shield" :size="18" :color="BRAND_600" />
        <text class="section-tip__text">将通过手机号 {{ maskedPhone }} 验证身份</text>
      </view>

      <view class="field">
        <text class="field__label">短信验证码</text>
        <view class="field__row">
          <input
            v-model="smsCode"
            type="number"
            maxlength="6"
            placeholder="请输入验证码"
            class="field__input field__input--flex"
          />
          <button
            class="sms-btn"
            :class="{ 'sms-btn--disabled': countdown > 0 || !phone }"
            :disabled="countdown > 0 || !phone"
            @tap="sendCode"
          >
            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
          </button>
        </view>
      </view>

      <view class="field">
        <text class="field__label">新密码</text>
        <input
          v-model="newPassword"
          type="text"
          password
          maxlength="32"
          placeholder="请输入新密码（至少6位）"
          class="field__input"
        />
      </view>

      <view class="field">
        <text class="field__label">确认新密码</text>
        <input
          v-model="confirmPassword"
          type="text"
          password
          maxlength="32"
          placeholder="请再次输入新密码"
          class="field__input"
        />
      </view>

      <button
        class="submit-btn"
        :class="{ 'submit-btn--loading': submitting }"
        :disabled="submitting"
        @tap="handleSubmit"
      >
        {{ submitting ? '提交中...' : '确认修改' }}
      </button>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.change-pwd-page {
  min-height: 100vh;
  background: $bg-page;
  padding: $spacing-md;
}

.form-card {
  padding: $spacing-lg;
}

.section-tip {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-sm $spacing-md;
  background: rgba($color-brand, 0.06);
  border-radius: $radius-md;
  margin-bottom: $spacing-lg;

  &__text {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

.field {
  margin-bottom: $spacing-lg;

  &__label {
    display: block;
    font-size: $font-sm;
    color: $text-secondary;
    font-weight: 600;
    margin-bottom: $spacing-xs;
  }

  &__row {
    display: flex;
    gap: $spacing-sm;
  }

  &__input {
    display: block;
    width: 100%;
    height: 88rpx;
    padding: 0 $spacing-md;
    background: $bg-page;
    border: 2rpx solid $border-color;
    border-radius: $radius-md;
    font-size: $font-md;
    box-sizing: border-box;

    &--flex {
      flex: 1;
      width: auto;
    }
  }
}

.sms-btn {
  flex-shrink: 0;
  height: 88rpx;
  line-height: 88rpx;
  padding: 0 $spacing-lg;
  background: $color-brand;
  color: white;
  border-radius: $radius-md;
  font-size: $font-sm;
  font-weight: 600;
  white-space: nowrap;

  &--disabled {
    background: $warm-200;
    color: $text-placeholder;
  }
}

.submit-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background: $color-brand;
  color: white;
  border-radius: $radius-lg;
  font-size: $font-md;
  font-weight: 700;
  margin-top: $spacing-lg;

  &--loading {
    opacity: 0.6;
  }
}
</style>
