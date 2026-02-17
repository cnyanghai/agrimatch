<script setup lang="ts">
/**
 * 印章上传器
 * 支持两种模式：
 * 1. 拍照/相册上传印章照片 → 自动提取红色印章 → 上传
 * 2. 输入名称 → 系统自动生成印章
 */
import { ref, computed } from 'vue'
import { extractSealFromPath } from '../composables/useSealExtractor'
import { uploadImage } from '../api/file'

const emit = defineEmits<{
  (e: 'created', sealUrl: string): void
}>()

const props = defineProps<{
  modelValue: boolean
}>()

type Mode = 'choose' | 'upload' | 'generate'
const mode = ref<Mode>('choose')

// 上传提取模式
const originalImage = ref('')
const extractedImage = ref('')
const processing = ref(false)
const uploading = ref(false)
const extractError = ref('')

// 通用
const sealName = ref('')

function close() {
  emit('created', '') // 空值表示取消
  reset()
}

function reset() {
  mode.value = 'choose'
  originalImage.value = ''
  extractedImage.value = ''
  processing.value = false
  uploading.value = false
  extractError.value = ''
  sealName.value = ''
}

/** 选择图片（拍照或相册） */
function chooseImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempPath = res.tempFilePaths[0]
      if (!tempPath) return
      originalImage.value = tempPath
      extractError.value = ''
      processExtract(tempPath)
    },
  })
}

/** 提取印章 */
async function processExtract(imagePath: string) {
  processing.value = true
  extractError.value = ''
  try {
    // H5: 直接使用 Canvas 提取
    // #ifdef H5
    const result = await extractSealFromPath(imagePath)
    extractedImage.value = result
    // #endif
    // #ifndef H5
    // 非 H5 环境也尝试（需要 Canvas 支持）
    const result2 = await extractSealFromPath(imagePath)
    extractedImage.value = result2
    // #endif
  } catch (e: any) {
    extractError.value = e?.message || '印章提取失败'
    extractedImage.value = ''
  } finally {
    processing.value = false
  }
}

/** 确认上传提取结果 */
async function confirmUpload() {
  if (!extractedImage.value || !sealName.value.trim()) {
    uni.showToast({ title: '请填写印章名称', icon: 'none' })
    return
  }
  uploading.value = true
  try {
    // 将 base64 转为临时文件并上传
    // #ifdef H5
    // H5: 将 base64 转 Blob → File → 上传
    const base64 = extractedImage.value
    const arr = base64.split(',')
    const mime = arr[0]?.match(/:(.*?);/)?.[1] || 'image/png'
    const bstr = atob(arr[1] || '')
    const u8arr = new Uint8Array(bstr.length)
    for (let i = 0; i < bstr.length; i++) u8arr[i] = bstr.charCodeAt(i)
    const blob = new Blob([u8arr], { type: mime })
    const tempUrl = URL.createObjectURL(blob)

    const fileRes = await uploadImage(tempUrl)
    emit('created', fileRes.fileUrl)
    URL.revokeObjectURL(tempUrl)
    // #endif

    // #ifndef H5
    // APP-PLUS: 先写 base64 到临时文件再上传
    const tmpPath = await writeBase64ToTemp(extractedImage.value)
    const fileRes2 = await uploadImage(tmpPath)
    emit('created', fileRes2.fileUrl)
    // #endif

    uni.showToast({ title: '印章上传成功', icon: 'success' })
    reset()
  } catch (e: any) {
    uni.showToast({ title: e?.message || '上传失败', icon: 'none' })
  } finally {
    uploading.value = false
  }
}

/** APP-PLUS: base64 写入临时文件 */
function writeBase64ToTemp(dataUrl: string): Promise<string> {
  return new Promise((resolve, reject) => {
    const base64Data = dataUrl.split(',')[1] || ''
    const fileName = `seal_extracted_${Date.now()}.png`
    // @ts-ignore
    if (typeof plus !== 'undefined') {
      const bitmap = new plus.nativeObj.Bitmap!(`seal_${Date.now()}`)
      bitmap.loadBase64Data(dataUrl, () => {
        const tempPath = `_doc/${fileName}`
        bitmap.save(tempPath, { overwrite: true, quality: 95 }, (e: any) => {
          bitmap.clear()
          resolve(e.target as string)
        }, (err: any) => {
          bitmap.clear()
          reject(new Error(err?.message || '保存临时文件失败'))
        })
      }, (err: any) => {
        bitmap.clear()
        reject(new Error(err?.message || '加载 base64 失败'))
      })
    } else {
      reject(new Error('非原生环境'))
    }
  })
}
</script>

<template>
  <view v-if="props.modelValue" class="seal-mask" @tap.self="close">
    <view class="seal-panel" @tap.stop>
      <!-- 顶部标题栏 -->
      <view class="seal-panel__header">
        <text class="seal-panel__title">{{ mode === 'choose' ? '添加印章' : mode === 'upload' ? '上传印章照片' : '系统生成印章' }}</text>
        <view class="seal-panel__close" @tap="close">
          <WgIcon name="x" :size="20" color="#78716C" />
        </view>
      </view>

      <!-- ===== 选择模式 ===== -->
      <template v-if="mode === 'choose'">
        <view class="choose-grid">
          <view class="choose-card" @tap="mode = 'upload'">
            <view class="choose-card__icon choose-card__icon--upload">
              <WgIcon name="camera" :size="28" color="#2D6A4F" />
            </view>
            <text class="choose-card__title">拍照提取</text>
            <text class="choose-card__desc">拍摄白底红章照片，自动提取透明印章</text>
          </view>
          <view class="choose-card" @tap="mode = 'generate'">
            <view class="choose-card__icon choose-card__icon--gen">
              <WgIcon name="sparkles" :size="28" color="#c28a55" />
            </view>
            <text class="choose-card__title">系统生成</text>
            <text class="choose-card__desc">输入公司名称，自动生成电子印章</text>
          </view>
        </view>
      </template>

      <!-- ===== 上传提取模式 ===== -->
      <template v-else-if="mode === 'upload'">
        <view class="upload-body">
          <!-- 原图 / 选择区域 -->
          <view v-if="!originalImage" class="upload-zone" @tap="chooseImage">
            <WgIcon name="image-plus" :size="40" color="#A8A29E" />
            <text class="upload-zone__text">点击选择印章照片</text>
            <text class="upload-zone__hint">支持拍照 / 相册选择，白底红章效果最佳</text>
          </view>

          <!-- 处理中 -->
          <view v-else-if="processing" class="upload-processing">
            <view class="upload-processing__spinner" />
            <text class="upload-processing__text">正在提取红色印章...</text>
          </view>

          <!-- 提取失败 -->
          <view v-else-if="extractError" class="upload-error">
            <WgIcon name="alert-circle" :size="40" color="#ef4444" />
            <text class="upload-error__text">{{ extractError }}</text>
            <view class="upload-error__retry" @tap="chooseImage">
              <text class="upload-error__retry-text">重新选择</text>
            </view>
          </view>

          <!-- 提取成功 -->
          <template v-else-if="extractedImage">
            <view class="extract-result">
              <view class="extract-compare">
                <view class="extract-compare__item">
                  <text class="extract-compare__label">原图</text>
                  <image :src="originalImage" class="extract-compare__img" mode="aspectFit" />
                </view>
                <view class="extract-compare__arrow">
                  <WgIcon name="arrow-right" :size="20" color="#A8A29E" />
                </view>
                <view class="extract-compare__item">
                  <text class="extract-compare__label">提取结果</text>
                  <view class="extract-compare__result-bg">
                    <image :src="extractedImage" class="extract-compare__img" mode="aspectFit" />
                  </view>
                </view>
              </view>

              <!-- 名称输入 -->
              <view class="seal-name-field">
                <text class="seal-name-field__label">印章名称</text>
                <input
                  v-model="sealName"
                  class="seal-name-field__input"
                  placeholder="例如：公司合同专用章"
                  :maxlength="20"
                />
              </view>

              <!-- 操作按钮 -->
              <view class="extract-actions">
                <view class="extract-actions__btn extract-actions__btn--retry" @tap="chooseImage">
                  <WgIcon name="rotate-ccw" :size="16" color="#78716C" />
                  <text class="extract-actions__text">重新选择</text>
                </view>
                <view
                  class="extract-actions__btn extract-actions__btn--confirm"
                  :class="{ 'extract-actions__btn--disabled': uploading || !sealName.trim() }"
                  @tap="confirmUpload"
                >
                  <text class="extract-actions__text extract-actions__text--white">
                    {{ uploading ? '上传中...' : '确认使用' }}
                  </text>
                </view>
              </view>
            </view>
          </template>
        </view>

        <!-- 返回 -->
        <view class="back-row" @tap="mode = 'choose'; originalImage = ''; extractedImage = ''; extractError = ''">
          <WgIcon name="arrow-left" :size="16" color="#78716C" />
          <text class="back-row__text">返回选择</text>
        </view>
      </template>

      <!-- ===== 系统生成模式 ===== -->
      <template v-else-if="mode === 'generate'">
        <view class="gen-body">
          <view class="seal-name-field">
            <text class="seal-name-field__label">印章名称</text>
            <input
              v-model="sealName"
              class="seal-name-field__input"
              placeholder="请输入印章名称（如：XX公司合同章）"
              :maxlength="20"
            />
          </view>
          <text class="gen-body__hint">系统将根据名称自动生成标准电子印章</text>

          <view class="extract-actions">
            <view
              class="extract-actions__btn extract-actions__btn--confirm extract-actions__btn--full"
              :class="{ 'extract-actions__btn--disabled': !sealName.trim() }"
              @tap="$emit('created', `__GENERATE__:${sealName.trim()}`)"
            >
              <WgIcon name="sparkles" :size="16" color="#fff" />
              <text class="extract-actions__text extract-actions__text--white">生成印章</text>
            </view>
          </view>
        </view>

        <view class="back-row" @tap="mode = 'choose'; sealName = ''">
          <WgIcon name="arrow-left" :size="16" color="#78716C" />
          <text class="back-row__text">返回选择</text>
        </view>
      </template>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.seal-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: $bg-mask;
  z-index: 200;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.seal-panel {
  width: 100%;
  max-height: 85vh;
  background: $bg-card;
  border-radius: $radius-xl $radius-xl 0 0;
  padding: $spacing-md $spacing-lg;
  padding-bottom: calc(#{$spacing-lg} + env(safe-area-inset-bottom));
  overflow-y: auto;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-lg;
  }

  &__title {
    font-size: $font-lg;
    font-weight: 700;
    color: $text-primary;
  }

  &__close {
    width: 64rpx; height: 64rpx;
    display: flex; align-items: center; justify-content: center;
    border-radius: 50%;
    background: $bg-page;
  }
}

/* ===== 选择模式 ===== */
.choose-grid {
  display: flex;
  gap: $spacing-md;
  margin-bottom: $spacing-md;
}

.choose-card {
  flex: 1;
  background: $bg-page;
  border-radius: $radius-lg;
  padding: $spacing-lg $spacing-md;
  text-align: center;
  border: 2rpx solid transparent;
  transition: all $transition-fast;

  &:active {
    border-color: $brand-300;
    background: $brand-50;
  }

  &__icon {
    width: 96rpx; height: 96rpx;
    border-radius: 50%;
    display: flex; align-items: center; justify-content: center;
    margin: 0 auto $spacing-sm;

    &--upload { background: rgba($brand-600, 0.08); }
    &--gen { background: rgba($autumn-500, 0.08); }
  }

  &__title {
    font-size: $font-md;
    font-weight: 700;
    color: $text-primary;
    display: block;
    margin-bottom: 4rpx;
  }

  &__desc {
    font-size: $font-xs;
    color: $text-secondary;
    display: block;
    line-height: 1.4;
  }
}

/* ===== 上传区域 ===== */
.upload-body {
  margin-bottom: $spacing-md;
}

.upload-zone {
  background: $bg-page;
  border: 3rpx dashed $border-color;
  border-radius: $radius-lg;
  padding: $spacing-2xl $spacing-md;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;

  &__text {
    font-size: $font-md;
    font-weight: 600;
    color: $text-primary;
  }

  &__hint {
    font-size: $font-xs;
    color: $text-secondary;
  }
}

.upload-processing {
  padding: $spacing-2xl;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-md;

  &__spinner {
    width: 60rpx; height: 60rpx;
    border: 4rpx solid $border-color;
    border-top-color: $brand-600;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  &__text {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.upload-error {
  padding: $spacing-2xl;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;

  &__text {
    font-size: $font-sm;
    color: $color-error;
    text-align: center;
  }

  &__retry {
    margin-top: $spacing-sm;
    padding: $spacing-xs $spacing-lg;
    background: $bg-page;
    border-radius: $radius-pill;
    border: 1rpx solid $border-color;
  }

  &__retry-text {
    font-size: $font-sm;
    color: $brand-600;
    font-weight: 600;
  }
}

/* ===== 提取结果 ===== */
.extract-result {
  background: $bg-page;
  border-radius: $radius-lg;
  padding: $spacing-md;
}

.extract-compare {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;

  &__item {
    flex: 1;
    text-align: center;
  }

  &__label {
    font-size: $font-xs;
    color: $text-secondary;
    display: block;
    margin-bottom: $spacing-xs;
  }

  &__img {
    width: 200rpx;
    height: 200rpx;
    border-radius: $radius-md;
  }

  &__result-bg {
    background: repeating-conic-gradient(#f0f0f0 0% 25%, #ffffff 0% 50%) 50% / 20rpx 20rpx;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: $spacing-xs;
  }

  &__arrow {
    flex-shrink: 0;
    width: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

/* ===== 印章名称输入 ===== */
.seal-name-field {
  margin-bottom: $spacing-md;

  &__label {
    font-size: $font-sm;
    font-weight: 600;
    color: $text-primary;
    display: block;
    margin-bottom: $spacing-xs;
  }

  &__input {
    width: 100%;
    height: 88rpx;
    background: $bg-card;
    border: 2rpx solid $border-color;
    border-radius: $radius-md;
    padding: 0 $spacing-md;
    font-size: $font-md;
    color: $text-primary;
  }
}

/* ===== 操作按钮 ===== */
.extract-actions {
  display: flex;
  gap: $spacing-sm;

  &__btn {
    flex: 1;
    height: 88rpx;
    border-radius: $radius-lg;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-xs;
    transition: all $transition-fast;

    &--retry {
      background: $bg-card;
      border: 2rpx solid $border-color;
    }

    &--confirm {
      background: $brand-600;
    }

    &--full {
      flex: none;
      width: 100%;
    }

    &--disabled {
      opacity: 0.5;
      pointer-events: none;
    }

    &:active { transform: scale(0.97); }
  }

  &__text {
    font-size: $font-md;
    font-weight: 600;
    color: $text-primary;

    &--white { color: #fff; }
  }
}

/* ===== 系统生成 ===== */
.gen-body {
  margin-bottom: $spacing-md;

  &__hint {
    font-size: $font-xs;
    color: $text-secondary;
    display: block;
    margin-bottom: $spacing-lg;
  }
}

/* ===== 返回行 ===== */
.back-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  padding: $spacing-sm 0;
  margin-top: $spacing-xs;

  &__text {
    font-size: $font-sm;
    color: $text-secondary;
  }
}
</style>
