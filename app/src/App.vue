<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useAuthStore } from './store/auth'

onLaunch(() => {
  const authStore = useAuthStore()
  authStore.restoreSession()

  // 未登录时强制跳转登录页
  if (!authStore.isLoggedIn) {
    uni.reLaunch({ url: '/pages/auth/login' })
  }
})

onShow(() => {
  // App 从后台切换到前台
})

onHide(() => {
  // App 切换到后台
})
</script>

<style lang="scss">
@import './static/css/animations.scss';

/* ===== 全局 Reset ===== */
page {
  background-color: $bg-page;
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', 'PingFang SC',
    'Microsoft YaHei', sans-serif;
  font-size: $font-base;
  color: $text-primary;
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
}

view, text, scroll-view, swiper, swiper-item, image, navigator {
  box-sizing: border-box;
}

image {
  display: block;
  width: 100%;
  height: auto;
}

button {
  margin: 0;
  padding: 0;
  background: none;
  border: none;
  line-height: inherit;
  font-size: inherit;
  color: inherit;

  &::after {
    border: none;
  }
}

input, textarea {
  box-sizing: border-box;
}

/* ===== 全局工具类 ===== */
.text-brand { color: $brand-600; }
.text-accent { color: $accent-400; }
.text-autumn { color: $autumn-400; }
.text-secondary { color: $text-secondary; }
.text-placeholder { color: $text-placeholder; }

.bg-brand { background-color: $brand-600; }
.bg-page { background-color: $bg-page; }
.bg-card { background-color: $bg-card; }

.price {
  color: $accent-400;
  font-weight: bold;
}

/* 单行文本省略 */
.ellipsis {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* 多行文本省略 */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
}

/* Flex 布局 */
.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 安全区域间距 */
.safe-area-top {
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
}

.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

/* 分割线 */
.divider {
  height: 1rpx;
  background: $border-light;
  margin: $spacing-md 0;
}

/* 页面内容区通用内边距 */
.page-padding {
  padding: $spacing-md;
}

/* TabBar 底部占位 */
.tab-page-bottom {
  padding-bottom: 130rpx;
}

/* 卡片基础样式 */
.card {
  background: $bg-card;
  border-radius: $radius-lg;
  box-shadow: $shadow-sm;
}

.card-md {
  background: $bg-card;
  border-radius: $radius-xl;
  box-shadow: $shadow-md;
}

/* 毛玻璃效果 */
.glass {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(40rpx);
  -webkit-backdrop-filter: blur(40rpx);
}

/* 品牌渐变背景 */
.bg-brand-gradient {
  background: linear-gradient(180deg, $brand-700 0%, $brand-600 100%);
}
</style>
