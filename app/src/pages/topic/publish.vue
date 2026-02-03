<script setup lang="ts">
import { ref, computed } from 'vue'
import { createPost } from '../../api/post'
import { uploadFile } from '../../utils/request'
import { useAuthStore } from '../../store/auth'

const authStore = useAuthStore()

const form = ref({
  title: '',
  content: '',
  domain: '',
})

const submitting = ref(false)
const showDomainPicker = ref(false)

/** 图片列表 */
interface ImageItem {
  url: string
  uploading: boolean
}
const imageList = ref<ImageItem[]>([])

/** 是否有图片正在上传 */
const uploading = computed(() => imageList.value.some((img) => img.uploading))

const domainOptions = [
  '粮食',
  '油料',
  '饲料',
  '畜牧',
  '水产',
  '果蔬',
  '农资',
  '农机',
  '物流',
  '其他',
]

/** 表单是否可提交 */
const canSubmit = computed(() => {
  return form.value.title.trim().length > 0 && !submitting.value && !uploading.value
})

/** 选择领域 */
function handleDomainPick(e: any) {
  const idx = e.detail.value
  form.value.domain = domainOptions[idx]
  showDomainPicker.value = false
}

/** 清除领域 */
function handleClearDomain() {
  form.value.domain = ''
}

/** 选择图片 */
function chooseImage() {
  const remaining = 9 - imageList.value.length
  if (remaining <= 0) return

  uni.chooseImage({
    count: remaining,
    sourceType: ['album', 'camera'],
    success(res) {
      const tempFiles = res.tempFilePaths as string[]
      tempFiles.forEach((filePath: string) => {
        const idx = imageList.value.length
        // 先用本地临时路径占位，标记为上传中
        imageList.value.push({ url: filePath, uploading: true })
        // 上传到服务器
        uploadFile(filePath, '/api/files/upload/image')
          .then((data: any) => {
            // 上传成功，替换为服务器 URL
            if (imageList.value[idx]) {
              imageList.value[idx].url = data.fileUrl
              imageList.value[idx].uploading = false
            }
          })
          .catch(() => {
            // 上传失败，移除该图片
            const failIdx = imageList.value.findIndex((img) => img.url === filePath)
            if (failIdx !== -1) {
              imageList.value.splice(failIdx, 1)
            }
            uni.showToast({ title: '图片上传失败', icon: 'none' })
          })
      })
    },
  })
}

/** 移除图片 */
function removeImage(idx: number) {
  imageList.value.splice(idx, 1)
}

/** 预览图片 */
function previewImage(idx: number) {
  const urls = imageList.value.filter((img) => !img.uploading).map((img) => img.url)
  const current = imageList.value[idx]?.url || urls[0]
  if (urls.length > 0) {
    uni.previewImage({ urls, current })
  }
}

/** 提交话题 */
async function handleSubmit() {
  if (!authStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }

  const title = form.value.title.trim()
  if (!title) {
    uni.showToast({ title: '请输入标题', icon: 'none' })
    return
  }
  if (title.length < 2) {
    uni.showToast({ title: '标题至少2个字', icon: 'none' })
    return
  }

  if (uploading.value) {
    uni.showToast({ title: '图片上传中，请稍候', icon: 'none' })
    return
  }

  if (submitting.value) return
  submitting.value = true

  try {
    // 构建 imagesJson
    const uploadedUrls = imageList.value
      .filter((img) => !img.uploading && img.url)
      .map((img) => img.url)
    const imagesJson = uploadedUrls.length > 0 ? JSON.stringify(uploadedUrls) : undefined

    await createPost({
      title,
      content: form.value.content.trim() || undefined,
      imagesJson,
      domain: form.value.domain || undefined,
    })
    uni.showToast({ title: '发布成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  } catch {
    // handled by request.ts
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="publish-page">
    <!-- 标题输入 -->
    <view class="form-card">
      <view class="form-card__field">
        <text class="form-card__label">标题 <text class="form-card__required">*</text></text>
        <input
          class="form-card__input"
          v-model="form.title"
          placeholder="请输入话题标题"
          :maxlength="100"
        />
      </view>

      <!-- 内容输入 -->
      <view class="form-card__field">
        <text class="form-card__label">内容</text>
        <textarea
          class="form-card__textarea"
          v-model="form.content"
          placeholder="分享你的想法、经验或问题..."
          :maxlength="5000"
          :auto-height="true"
        />
        <view class="form-card__counter">
          <text class="form-card__counter-text">{{ form.content.length }} / 5000</text>
        </view>
      </view>

      <!-- 图片上传 -->
      <view class="form-card__field">
        <text class="form-card__label">图片 <text class="form-card__hint">(最多9张)</text></text>
        <view class="image-grid">
          <view v-for="(img, idx) in imageList" :key="idx" class="image-grid__item">
            <image :src="img.url" mode="aspectFill" class="image-grid__img" @tap="previewImage(idx)" />
            <view class="image-grid__delete" @tap.stop="removeImage(idx)">
              <uni-icons type="clear" size="20" color="#fff" />
            </view>
            <view v-if="img.uploading" class="image-grid__loading">
              <view class="image-grid__spinner" />
            </view>
          </view>
          <view v-if="imageList.length < 9" class="image-grid__add" @tap="chooseImage">
            <uni-icons type="plusempty" size="40" color="#ccc" />
          </view>
        </view>
      </view>

      <!-- 领域选择 -->
      <view class="form-card__field">
        <text class="form-card__label">领域</text>
        <view class="form-card__picker-row">
          <picker :range="domainOptions" @change="handleDomainPick">
            <view class="form-card__picker">
              <text
                class="form-card__picker-text"
                :class="{ 'form-card__picker-text--placeholder': !form.domain }"
              >
                {{ form.domain || '选择领域（可选）' }}
              </text>
              <uni-icons type="right" size="16" color="#999" />
            </view>
          </picker>
          <view
            v-if="form.domain"
            class="form-card__clear"
            @tap="handleClearDomain"
          >
            <text class="form-card__clear-text">清除</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 发布按钮 -->
    <view class="publish-page__action">
      <view
        class="publish-btn"
        :class="{ 'publish-btn--disabled': !canSubmit }"
        @tap="handleSubmit"
      >
        <text class="publish-btn__text">{{ submitting ? '发布中...' : (uploading ? '图片上传中...' : '发布话题') }}</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.publish-page {
  min-height: 100vh;
  background: $bg-page;
  padding: $spacing-md;

  &__action {
    margin-top: $spacing-lg;
  }
}

.form-card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md $spacing-lg;

  &__field {
    padding: $spacing-sm 0;
    border-bottom: 1rpx solid $border-light;

    &:last-child {
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

  &__required {
    color: $accent-400;
    font-weight: normal;
  }

  &__hint {
    font-size: $font-xs;
    color: $text-placeholder;
    font-weight: normal;
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
    min-height: 240rpx;
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

  &__picker-row {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__picker {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 80rpx;
  }

  &__picker-text {
    font-size: $font-md;
    color: $text-primary;

    &--placeholder {
      color: $text-placeholder;
    }
  }

  &__picker-arrow {
    font-size: $font-lg;
    color: $text-placeholder;
  }

  &__clear {
    flex-shrink: 0;
    padding: $spacing-xs $spacing-sm;
  }

  &__clear-text {
    font-size: $font-sm;
    color: $accent-400;
  }
}

/* ===== 图片网格 ===== */
.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;

  &__item {
    position: relative;
    width: calc((100% - 32rpx) / 3);
    height: 0;
    padding-bottom: calc((100% - 32rpx) / 3);
    border-radius: $radius-sm;
    overflow: hidden;
  }

  &__img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
  }

  &__delete {
    position: absolute;
    top: 0;
    right: 0;
    width: 44rpx;
    height: 44rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.5);
    border-radius: 0 0 0 $radius-sm;
    z-index: 2;
  }

  &__loading {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.4);
    z-index: 1;
  }

  &__spinner {
    width: 48rpx;
    height: 48rpx;
    border: 4rpx solid rgba(255, 255, 255, 0.3);
    border-top-color: #fff;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  &__add {
    width: calc((100% - 32rpx) / 3);
    height: 0;
    padding-bottom: calc((100% - 32rpx) / 3);
    position: relative;
    background: $bg-page;
    border: 2rpx dashed $border-color;
    border-radius: $radius-sm;

    // Center the icon inside the padding-based square
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.publish-btn {
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
