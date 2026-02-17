<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useAuthStore } from './store/auth'

onLaunch(() => {
  const authStore = useAuthStore()
  authStore.restoreSession()
  // Allow browsing without login - login is prompted when needed
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
    'Noto Sans SC', 'Microsoft YaHei', 'Segoe UI', sans-serif;
  font-size: $font-base;
  color: $text-primary;
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
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
.bg-warm { background-color: $warm-50; }

.price {
  color: $accent-400;
  font-weight: bold;
  font-variant-numeric: tabular-nums;
}

/* 金额等宽字体（数字对齐） */
.font-mono {
  font-family: 'DIN Alternate', 'Roboto Mono', 'SF Mono', 'Menlo', 'Consolas', monospace;
  font-variant-numeric: tabular-nums;
}

/* 金额大号显示 */
.price-lg {
  color: $accent-400;
  font-weight: 800;
  font-size: $font-xl;
  font-variant-numeric: tabular-nums;
  font-family: 'DIN Alternate', 'Roboto Mono', -apple-system, sans-serif;
}

/* 金额小号显示 */
.price-sm {
  color: $accent-400;
  font-weight: 600;
  font-size: $font-sm;
  font-variant-numeric: tabular-nums;
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
  background: $warm-200;
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

/* 卡片基础样式 (暖阴影) */
.card {
  background: $bg-card;
  border-radius: $radius-lg;
  box-shadow: $shadow-warm-card;
}

.card-md {
  background: $bg-card;
  border-radius: $radius-xl;
  box-shadow: $shadow-md;
}

/* 暖色卡片 */
.card-warm {
  background: $bg-card;
  border-radius: $radius-xl;
  box-shadow: $shadow-warm-card;
  border: 1rpx solid $warm-200;
}

/* 区块标题 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-md;
  padding: 0 $spacing-xs;
}

/* 毛玻璃效果 (暖色调) */
.glass {
  background: rgba(255, 253, 248, 0.88);
  backdrop-filter: blur(40rpx);
  -webkit-backdrop-filter: blur(40rpx);
}

/* 品牌渐变背景 */
.bg-brand-gradient {
  background: linear-gradient(180deg, $brand-700 0%, $brand-600 100%);
}

/* 统一点击反馈效果 */
.tap-feedback {
  transition: transform 150ms ease, opacity 150ms ease;

  &:active {
    transform: scale(0.98);
    opacity: 0.85;
  }
}

/* 骨架屏占位高度 */
.skeleton-block {
  background: linear-gradient(90deg, #F5F0E8 25%, #E8E0D4 37%, #F5F0E8 63%);
  background-size: 200% 100%;
  animation: shimmer-block 1.5s infinite ease-in-out;
  border-radius: $radius-sm;
}

@keyframes shimmer-block {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>
