<script setup lang="ts">
/**
 * 印章管理页面
 * 查看、创建、删除印章，设置默认印章
 */
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  listSeals,
  createSeal,
  deleteSeal,
  setDefaultSeal,
  type SealResponse,
  type SealCreateRequest,
} from '../../api/contract'

const seals = ref<SealResponse[]>([])
const loading = ref(true)
const showUploader = ref(false)

onShow(() => loadSeals())

async function loadSeals() {
  loading.value = true
  try {
    seals.value = await listSeals() || []
  } catch {
    seals.value = []
  } finally {
    loading.value = false
  }
}

/** 设为默认印章 */
async function handleSetDefault(seal: SealResponse) {
  if (seal.isDefault) return
  try {
    await setDefaultSeal(seal.id)
    uni.showToast({ title: '已设为默认', icon: 'success' })
    await loadSeals()
  } catch {
    // handled
  }
}

/** 删除印章 */
function handleDelete(seal: SealResponse) {
  uni.showModal({
    title: '删除印章',
    content: `确定要删除印章「${seal.sealName}」吗？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await deleteSeal(seal.id)
        uni.showToast({ title: '已删除', icon: 'success' })
        await loadSeals()
      } catch {
        // handled
      }
    },
  })
}

/** 印章上传器回调 */
async function handleSealCreated(result: string) {
  showUploader.value = false
  if (!result) return

  if (result.startsWith('__GENERATE__:')) {
    const sealName = result.replace('__GENERATE__:', '')
    try {
      await createSeal({ sealName, sealType: 'company', generate: true })
      uni.showToast({ title: '印章生成成功', icon: 'success' })
      await loadSeals()
    } catch { /* handled */ }
  } else {
    try {
      await createSeal({ sealName: '上传印章', sealType: 'company', sealUrl: result, generate: false })
      uni.showToast({ title: '印章创建成功', icon: 'success' })
      await loadSeals()
    } catch { /* handled */ }
  }
}
</script>

<template>
  <view class="seal-page">
    <!-- 印章列表 -->
    <WgSkeleton v-if="loading" type="list" :rows="3" />

    <template v-else>
      <view v-if="seals.length === 0" class="empty-wrap">
        <WgEmpty text="暂无印章" description="创建电子印章后可用于合同签署" />
      </view>

      <view v-else class="seal-list">
        <view
          v-for="seal in seals"
          :key="seal.id"
          class="seal-card"
        >
          <!-- 印章图片 -->
          <view class="seal-card__visual">
            <image
              v-if="seal.sealUrl"
              :src="seal.sealUrl"
              class="seal-card__img"
              mode="aspectFit"
            />
            <view v-else class="seal-card__placeholder">
              <text class="seal-card__placeholder-text">{{ seal.sealName.slice(0, 4) }}</text>
            </view>
          </view>

          <!-- 信息 -->
          <view class="seal-card__info">
            <view class="seal-card__top">
              <text class="seal-card__name">{{ seal.sealName }}</text>
              <text v-if="seal.isDefault" class="seal-card__default-tag">默认</text>
            </view>
            <text class="seal-card__meta">
              {{ seal.isGenerated ? '系统生成' : '上传提取' }} · {{ seal.sealType === 'company' ? '公司章' : '个人章' }}
            </text>
          </view>

          <!-- 操作 -->
          <view class="seal-card__actions">
            <view
              v-if="!seal.isDefault"
              class="seal-card__btn seal-card__btn--default"
              @tap="handleSetDefault(seal)"
            >
              <text class="seal-card__btn-text">设为默认</text>
            </view>
            <view class="seal-card__btn seal-card__btn--delete" @tap="handleDelete(seal)">
              <WgIcon name="trash" :size="14" color="#ef4444" />
            </view>
          </view>
        </view>
      </view>
    </template>

    <!-- 创建按钮 -->
    <view class="create-bar safe-area-bottom">
      <view class="create-btn" @tap="showUploader = true">
        <WgIcon name="plus-circle" :size="18" color="#fff" />
        <text class="create-btn__text">添加印章</text>
      </view>
    </view>

    <!-- 印章上传器 -->
    <WgSealUploader
      :model-value="showUploader"
      @created="handleSealCreated"
    />
  </view>
</template>

<style lang="scss" scoped>
.seal-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: 140rpx;
}

.empty-wrap {
  padding-top: $spacing-3xl;
}

.seal-list {
  padding: $spacing-md;
}

.seal-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  background: $bg-card;
  border-radius: $radius-xl;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  box-shadow: $shadow-warm-card;

  &__visual {
    width: 120rpx;
    height: 120rpx;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: repeating-conic-gradient(#f5f5f5 0% 25%, #ffffff 0% 50%) 50% / 16rpx 16rpx;
    border-radius: $radius-lg;
    overflow: hidden;
  }

  &__img {
    width: 100%;
    height: 100%;
  }

  &__placeholder {
    width: 100%;
    height: 100%;
    border: 3rpx solid #c53030;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__placeholder-text {
    font-size: $font-sm;
    font-weight: 700;
    color: #c53030;
    text-align: center;
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__top {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    margin-bottom: 4rpx;
  }

  &__name {
    font-size: $font-md;
    font-weight: 700;
    color: $text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__default-tag {
    font-size: $font-xs;
    color: $brand-600;
    background: $brand-50;
    padding: 2rpx 12rpx;
    border-radius: $radius-pill;
    flex-shrink: 0;
  }

  &__meta {
    font-size: $font-xs;
    color: $text-secondary;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    flex-shrink: 0;
  }

  &__btn {
    padding: $spacing-xs $spacing-md;
    border-radius: $radius-pill;
    display: flex;
    align-items: center;
    justify-content: center;

    &--default {
      background: $brand-50;
      border: 1rpx solid $brand-200;
    }

    &--delete {
      padding: $spacing-xs;
      background: rgba(239, 68, 68, 0.06);
      border-radius: 50%;
      width: 56rpx;
      height: 56rpx;
    }

    &:active { opacity: 0.7; }
  }

  &__btn-text {
    font-size: $font-xs;
    color: $brand-600;
    font-weight: 600;
  }
}

.create-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: $spacing-sm $spacing-md;
  background: $bg-card;
  border-top: 1rpx solid $border-light;
}

.create-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  height: 96rpx;
  background: $brand-600;
  border-radius: $radius-lg;

  &:active { transform: scale(0.97); }

  &__text {
    font-size: $font-lg;
    font-weight: 700;
    color: #fff;
  }
}
</style>
