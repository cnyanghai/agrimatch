<script setup lang="ts">
import { computed, ref } from 'vue'
import { WARM_900 } from '../constants/colors'

const props = withDefaults(defineProps<{
  title?: string
  back?: boolean
  transparent?: boolean
  light?: boolean
}>(), {
  title: '',
  back: true,
  transparent: false,
  light: false,
})

defineSlots<{
  left?(): any
  right?(): any
}>()

const emit = defineEmits<{
  back: []
}>()

const statusBarHeight = ref(0)
try {
  const sys = uni.getSystemInfoSync()
  if (sys.statusBarHeight && sys.statusBarHeight > 0) {
    statusBarHeight.value = sys.statusBarHeight
  }
} catch {}

const navHeight = computed(() => statusBarHeight.value + 44)

function handleBack() {
  emit('back')
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.switchTab({ url: '/pages/home/index' })
  }
}
</script>

<template>
  <view class="wg-nav-wrapper">
    <view
      class="wg-nav"
      :class="{
        'wg-nav--transparent': transparent,
        'wg-nav--light': light,
      }"
      :style="{ height: navHeight + 'px', paddingTop: statusBarHeight + 'px' }"
    >
      <view class="wg-nav__content">
        <view class="wg-nav__left">
          <slot name="left">
            <view v-if="back" class="wg-nav__back tap-feedback" @tap="handleBack">
              <WgIcon name="arrow-left" :size="20" :color="light ? '#fff' : WARM_900" />
            </view>
          </slot>
        </view>

        <text
          class="wg-nav__title"
          :class="{ 'wg-nav__title--light': light }"
        >{{ title }}</text>

        <view class="wg-nav__right">
          <slot name="right" />
        </view>
      </view>
    </view>
    <!-- 占位，防止 fixed 导航遮盖内容 -->
    <view class="wg-nav-placeholder" :style="{ height: navHeight + 'px' }" />
  </view>
</template>

<style lang="scss" scoped>
.wg-nav-wrapper {
  position: relative;
  z-index: 100;
}

.wg-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: $bg-card;
  box-shadow: 0 1rpx 0 $border-light;

  &--transparent {
    background: transparent;
    box-shadow: none;
  }

  &--light {
    background: transparent;
    box-shadow: none;
  }

  &__content {
    display: flex;
    align-items: center;
    height: 44px;
    padding: 0 $spacing-sm;
  }

  &__left,
  &__right {
    min-width: 80rpx;
    display: flex;
    align-items: center;
  }

  &__right {
    justify-content: flex-end;
  }

  &__back {
    width: 72rpx;
    height: 72rpx;
    border-radius: $radius-full;
    display: flex;
    align-items: center;
    justify-content: center;

    &:active {
      background: $warm-100;
    }
  }

  &__title {
    flex: 1;
    text-align: center;
    font-size: $font-lg;
    font-weight: 700;
    color: $text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    &--light {
      color: $text-inverse;
    }
  }
}

.wg-nav-placeholder {
  width: 100%;
  flex-shrink: 0;
}
</style>
