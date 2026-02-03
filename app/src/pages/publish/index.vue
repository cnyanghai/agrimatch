<script setup lang="ts">
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '../../store/auth'

const authStore = useAuthStore()

onShow(() => {
  if (!authStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }

  uni.showActionSheet({
    itemList: ['发布供应', '发布采购'],
    success: (res) => {
      if (res.tapIndex === 0) {
        uni.navigateTo({ url: '/pages/supply/publish' })
      } else if (res.tapIndex === 1) {
        uni.navigateTo({ url: '/pages/requirement/publish' })
      }
    },
    complete: () => {
      // 返回上一个 tab
      uni.switchTab({ url: '/pages/home/index' })
    }
  })
})
</script>

<template>
  <view class="publish-placeholder">
    <WgTabBar :current="2" />
  </view>
</template>

<style lang="scss" scoped>
.publish-placeholder {
  min-height: 100vh;
  background: $bg-page;
}
</style>
