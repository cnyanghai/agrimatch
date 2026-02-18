<script setup lang="ts">
import { ref } from 'vue'
import { BRAND_600, WARM_300, WARM_500, AUTUMN_500, ACTION_600 } from '../../constants/colors'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '../../store/auth'

const authStore = useAuthStore()
const showPanel = ref(false)

onShow(() => {
  if (!authStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  showPanel.value = true
})

function goSupplyPublish() {
  showPanel.value = false
  uni.navigateTo({ url: '/pages/supply/publish' })
}

function goRequirementPublish() {
  showPanel.value = false
  uni.navigateTo({ url: '/pages/requirement/publish' })
}

function goTopicPublish() {
  showPanel.value = false
  uni.navigateTo({ url: '/pages/topic/publish' })
}

function handleClose() {
  showPanel.value = false
  uni.switchTab({ url: '/pages/home/index' })
}
</script>

<template>
  <view class="publish-page">
    <WgNavBar title="发布" />

    <!-- 半透明遮罩 + 底部弹窗 -->
    <view v-if="showPanel" class="overlay anim-overlay-in" @tap="handleClose">
      <view class="panel anim-panel-up" @tap.stop>
        <view class="panel__header">
          <text class="panel__title">发布</text>
          <view class="panel__close" @tap="handleClose">
            <WgIcon name="clear" :size="20" :color="WARM_500" />
          </view>
        </view>

        <view class="panel__cards">
          <!-- 发布供应 -->
          <view class="publish-card publish-card--brand" @tap="goSupplyPublish">
            <view class="publish-card__icon">
              <WgIcon name="store" :size="28" :color="BRAND_600" />
            </view>
            <view class="publish-card__body">
              <text class="publish-card__title">发布供应</text>
              <text class="publish-card__desc">发布您的商品供应信息</text>
            </view>
            <WgIcon name="right" :size="18" :color="WARM_300" />
          </view>

          <!-- 发布采购 -->
          <view class="publish-card publish-card--autumn" @tap="goRequirementPublish">
            <view class="publish-card__icon">
              <WgIcon name="shopping-bag" :size="28" :color="AUTUMN_500" />
            </view>
            <view class="publish-card__body">
              <text class="publish-card__title">发布采购</text>
              <text class="publish-card__desc">发布您的采购需求信息</text>
            </view>
            <WgIcon name="right" :size="18" :color="WARM_300" />
          </view>

          <!-- 发布话题 -->
          <view class="publish-card publish-card--action" @tap="goTopicPublish">
            <view class="publish-card__icon">
              <WgIcon name="square-pen" :size="28" :color="ACTION_600" />
            </view>
            <view class="publish-card__body">
              <text class="publish-card__title">发布话题</text>
              <text class="publish-card__desc">分享行业见解和动态</text>
            </view>
            <WgIcon name="right" :size="18" :color="WARM_300" />
          </view>
        </view>
      </view>
    </view>

  </view>
</template>

<style lang="scss" scoped>
.publish-page {
  min-height: 100vh;
  background: $bg-page;
}

.overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 100;
  display: flex;
  align-items: flex-end;
}

.panel {
  width: 100%;
  background: $bg-card;
  border-radius: 32rpx 32rpx 0 0;
  padding: $spacing-lg $spacing-lg;
  padding-bottom: calc(#{$spacing-xl} + constant(safe-area-inset-bottom));
  padding-bottom: calc(#{$spacing-xl} + env(safe-area-inset-bottom));

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-lg;
  }

  &__title {
    font-size: $font-xl;
    font-weight: 800;
    color: $text-primary;
  }

  &__close {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    background: $warm-100;
    display: flex;
    align-items: center;
    justify-content: center;

    &:active {
      background: $warm-200;
    }
  }
}

.panel__cards {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.publish-card {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-lg;
  border-radius: $radius-xl;
  border: 1rpx solid $warm-200;
  transition: transform 150ms ease;

  &:active {
    transform: scale(0.98);
    opacity: 0.85;
  }

  &--brand {
    background: linear-gradient(135deg, $brand-50 0%, rgba(255,255,255,0.6) 100%);
    border-color: $brand-100;
  }

  &--autumn {
    background: linear-gradient(135deg, $autumn-50 0%, rgba(255,255,255,0.6) 100%);
    border-color: $autumn-100;
  }

  &--action {
    background: linear-gradient(135deg, rgba(37, 99, 235, 0.04) 0%, rgba(255,255,255,0.6) 100%);
    border-color: rgba(37, 99, 235, 0.1);
  }

  &__icon {
    width: 96rpx;
    height: 96rpx;
    border-radius: $radius-lg;
    background: rgba(255, 255, 255, 0.8);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    box-shadow: $shadow-sm;
  }

  &__body {
    flex: 1;
    min-width: 0;
  }

  &__title {
    display: block;
    font-size: $font-lg;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: 4rpx;
  }

  &__desc {
    display: block;
    font-size: $font-sm;
    color: $text-secondary;
  }
}
</style>
