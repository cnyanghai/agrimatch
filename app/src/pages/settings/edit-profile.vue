<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../../store/auth'
import { updateMe } from '../../api/user'
import { uploadFile } from '../../utils/request'

const authStore = useAuthStore()

const form = ref({
  nickName: '',
  position: '',
  gender: 1 as number,
  bio: '',
  avatar: '',
})

const saving = ref(false)
const uploadingAvatar = ref(false)

const genderOptions = ['男', '女']

onMounted(() => {
  if (!authStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  const u = authStore.user
  if (u) {
    form.value.nickName = u.nickName || ''
    form.value.position = u.position || ''
    form.value.gender = u.gender ?? 1
    form.value.bio = u.bio || ''
    form.value.avatar = u.avatar || ''
  }
})

// Note: the User type doesn't have gender directly from the summary, but UserUpdateRequest supports it
// We'll handle it gracefully

async function handleChangeAvatar() {
  try {
    const res = await new Promise<UniApp.ChooseImageSuccessCallbackResult>((resolve, reject) => {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: resolve,
        fail: reject,
      })
    })

    if (res.tempFilePaths.length === 0) return
    uploadingAvatar.value = true

    const uploadRes = await uploadFile(res.tempFilePaths[0], '/api/files/upload/image')
    form.value.avatar = uploadRes.fileUrl
  } catch {
    // user cancelled or upload failed
  } finally {
    uploadingAvatar.value = false
  }
}

function handleGenderPick(e: any) {
  form.value.gender = Number(e.detail.value) + 1 // index 0 = 男(1), index 1 = 女(2)
}

async function handleSave() {
  if (saving.value) return
  saving.value = true

  try {
    await updateMe({
      nickName: form.value.nickName.trim() || undefined,
      position: form.value.position.trim() || undefined,
      gender: form.value.gender,
      bio: form.value.bio.trim() || undefined,
      avatar: form.value.avatar || undefined,
    })

    // Update local auth store
    if (authStore.user) {
      authStore.saveUser({
        ...authStore.user,
        nickName: form.value.nickName.trim() || authStore.user.nickName,
        position: form.value.position.trim() || authStore.user.position,
        bio: form.value.bio.trim() || authStore.user.bio,
        avatar: form.value.avatar || authStore.user.avatar,
      })
    }

    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1000)
  } catch {
    // handled by request.ts
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <view class="edit-page">
    <!-- 头像 -->
    <view class="avatar-section" @tap="handleChangeAvatar">
      <view class="avatar-section__wrap">
        <image
          v-if="form.avatar"
          :src="form.avatar"
          class="avatar-section__img"
          mode="aspectFill"
        />
        <view v-else class="avatar-section__placeholder">
          <uni-icons type="person" size="48" color="#ccc" />
        </view>
        <view class="avatar-section__overlay">
          <uni-icons type="camera" size="20" color="#fff" />
        </view>
      </view>
      <text class="avatar-section__hint">{{ uploadingAvatar ? '上传中...' : '点击更换头像' }}</text>
    </view>

    <!-- 表单 -->
    <view class="form-card">
      <view class="form-card__field">
        <text class="form-card__label">昵称</text>
        <input
          class="form-card__input"
          v-model="form.nickName"
          placeholder="请输入昵称"
          :maxlength="20"
        />
      </view>

      <view class="form-card__field">
        <text class="form-card__label">职位</text>
        <input
          class="form-card__input"
          v-model="form.position"
          placeholder="请输入职位"
          :maxlength="50"
        />
      </view>

      <view class="form-card__field">
        <text class="form-card__label">性别</text>
        <picker :range="genderOptions" :value="form.gender - 1" @change="handleGenderPick">
          <view class="form-card__picker">
            <text class="form-card__picker-text">{{ form.gender === 1 ? '男' : '女' }}</text>
            <uni-icons type="right" size="16" color="#999" />
          </view>
        </picker>
      </view>

      <view class="form-card__field form-card__field--last">
        <text class="form-card__label">个人简介</text>
        <textarea
          class="form-card__textarea"
          v-model="form.bio"
          placeholder="介绍一下自己"
          :maxlength="200"
          :auto-height="true"
        />
        <view class="form-card__counter">
          <text class="form-card__counter-text">{{ form.bio.length }} / 200</text>
        </view>
      </view>
    </view>

    <!-- 保存按钮 -->
    <view class="edit-page__action">
      <view class="save-btn" :class="{ 'save-btn--disabled': saving }" @tap="handleSave">
        <text class="save-btn__text">{{ saving ? '保存中...' : '保存' }}</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.edit-page {
  min-height: 100vh;
  background: $bg-page;
  padding: $spacing-md;

  &__action {
    margin-top: $spacing-lg;
    padding-bottom: $spacing-xl;
  }
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-xl 0;

  &__wrap {
    position: relative;
    width: 160rpx;
    height: 160rpx;
    border-radius: 50%;
    overflow: hidden;
  }

  &__img {
    width: 100%;
    height: 100%;
  }

  &__placeholder {
    width: 100%;
    height: 100%;
    background: $bg-hover;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 48rpx;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__hint {
    margin-top: $spacing-sm;
    font-size: $font-sm;
    color: $text-secondary;
  }
}

.form-card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: 0 $spacing-lg;

  &__field {
    padding: $spacing-md 0;
    border-bottom: 1rpx solid $border-light;

    &--last {
      border-bottom: none;
    }
  }

  &__label {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
    display: block;
    margin-bottom: $spacing-xs;
  }

  &__input {
    width: 100%;
    height: 80rpx;
    font-size: $font-md;
    color: $text-primary;
    padding: 0;
  }

  &__textarea {
    width: 100%;
    min-height: 160rpx;
    font-size: $font-md;
    color: $text-primary;
    line-height: 1.8;
    padding: $spacing-xs 0;
  }

  &__counter {
    display: flex;
    justify-content: flex-end;
    margin-top: $spacing-xs;
  }

  &__counter-text {
    font-size: $font-xs;
    color: $text-placeholder;
  }

  &__picker {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 80rpx;
  }

  &__picker-text {
    font-size: $font-md;
    color: $text-primary;
  }
}

.save-btn {
  width: 100%;
  height: 96rpx;
  background: $brand-600;
  border-radius: $radius-lg;
  display: flex;
  align-items: center;
  justify-content: center;

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
