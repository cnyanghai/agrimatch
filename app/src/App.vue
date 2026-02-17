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

/* ================================================================
   沃谷 App — Stitch Design Language (全局样式)
   设计理念：大留白 · 大圆角 · 柔和层次 · 品牌色点缀 · 呼吸感
   ================================================================ */

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

/* ===== 文字工具类 ===== */
.text-brand { color: $brand-600; }
.text-accent { color: $accent-400; }
.text-autumn { color: $autumn-400; }
.text-secondary { color: $text-secondary; }
.text-placeholder { color: $text-placeholder; }

.bg-brand { background-color: $brand-600; }
.bg-page { background-color: $bg-page; }
.bg-card { background-color: $bg-card; }
.bg-warm { background-color: $warm-50; }

/* ===== 品牌渐变背景 (Stitch Hero) ===== */
.stitch-hero {
  background: linear-gradient(165deg, $brand-700 0%, $brand-600 45%, $brand-500 100%);
  color: $text-inverse;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: -60%;
    right: -30%;
    width: 500rpx;
    height: 500rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.06);
    pointer-events: none;
  }
}

.stitch-hero--autumn {
  background: linear-gradient(165deg, $autumn-600 0%, $autumn-400 60%, $autumn-300 100%);
  color: $text-inverse;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: -60%;
    right: -30%;
    width: 500rpx;
    height: 500rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.06);
    pointer-events: none;
  }
}

/* ===== Stitch 卡片系统 ===== */
.stitch-card {
  background: $bg-card;
  border-radius: $radius-2xl;
  padding: $spacing-xl;
  box-shadow: 0 2px 8px rgba(120, 90, 50, 0.04), 0 0 1px rgba(120, 90, 50, 0.06);
  transition: transform $transition-fast ease, box-shadow $transition-fast ease;

  &:active {
    transform: scale(0.985);
    box-shadow: 0 1px 4px rgba(120, 90, 50, 0.03);
  }
}

.stitch-card--elevated {
  box-shadow: 0 4px 16px rgba(120, 90, 50, 0.06), 0 0 1px rgba(120, 90, 50, 0.08);
}

/* ===== Stitch 搜索栏 ===== */
.stitch-search {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  height: 84rpx;
  padding: 0 $spacing-xl;
  background: rgba(255, 255, 255, 0.18);
  border-radius: $radius-full;
  border: 1rpx solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(20rpx);

  &__text {
    color: rgba(255, 255, 255, 0.7);
    font-size: $font-md;
  }

  &--solid {
    background: $warm-100;
    border: none;
    backdrop-filter: none;
  }

  &--solid &__text {
    color: $text-placeholder;
  }
}

/* ===== Stitch 胶囊按钮 ===== */
.stitch-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-sm $spacing-xl;
  border-radius: $radius-full;
  font-size: $font-sm;
  font-weight: 600;
  transition: all $transition-fast ease;

  &--brand {
    background: $brand-600;
    color: $text-inverse;
    box-shadow: $shadow-brand;
  }

  &--outline {
    background: transparent;
    color: $text-secondary;
    border: 1rpx solid $warm-200;
  }

  &--soft {
    background: $brand-50;
    color: $brand-600;
  }

  &--autumn {
    background: $autumn-50;
    color: $autumn-500;
  }

  &:active {
    transform: scale(0.95);
    opacity: 0.9;
  }
}

/* ===== Stitch 图标容器 ===== */
.stitch-icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-xl;

  &--sm {
    width: 72rpx;
    height: 72rpx;
    border-radius: $radius-lg;
  }

  &--md {
    width: 96rpx;
    height: 96rpx;
  }

  &--lg {
    width: 112rpx;
    height: 112rpx;
    border-radius: $radius-2xl;
  }

  &--brand { background: $brand-50; }
  &--brand-bold { background: $brand-100; }
  &--autumn { background: $autumn-50; }
  &--action { background: rgba(37, 99, 235, 0.06); }
  &--accent { background: $accent-50; }
  &--warm { background: $warm-100; }
}

/* ===== Stitch 区块标题 ===== */
.stitch-section-title {
  font-size: $font-lg;
  font-weight: 800;
  color: $text-primary;
  letter-spacing: 1rpx;
}

.stitch-section-subtitle {
  font-size: $font-sm;
  color: $text-secondary;
  margin-top: 4rpx;
}

/* ===== 价格 ===== */
.price {
  color: $accent-400;
  font-weight: bold;
  font-variant-numeric: tabular-nums;
}

.font-mono {
  font-family: 'DIN Alternate', 'Roboto Mono', 'SF Mono', 'Menlo', 'Consolas', monospace;
  font-variant-numeric: tabular-nums;
}

.price-lg {
  color: $accent-400;
  font-weight: 800;
  font-size: $font-xl;
  font-variant-numeric: tabular-nums;
  font-family: 'DIN Alternate', 'Roboto Mono', -apple-system, sans-serif;
}

.price-sm {
  color: $accent-400;
  font-weight: 600;
  font-size: $font-sm;
  font-variant-numeric: tabular-nums;
}

/* ===== 文本工具 ===== */
.ellipsis {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

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

/* ===== Flex 布局 ===== */
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

/* ===== 安全区域 ===== */
.safe-area-top {
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
}

.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

/* ===== 分割线 ===== */
.divider {
  height: 1rpx;
  background: $warm-100;
  margin: $spacing-lg 0;
}

/* ===== 页面通用 ===== */
.page-padding {
  padding: $spacing-lg;
}

.tab-page-bottom {
  padding-bottom: 160rpx;
}

/* ===== 旧版卡片样式（兼容） ===== */
.card {
  background: $bg-card;
  border-radius: $radius-2xl;
  box-shadow: 0 2px 8px rgba(120, 90, 50, 0.04);
}

.card-md {
  background: $bg-card;
  border-radius: $radius-2xl;
  box-shadow: $shadow-md;
}

.card-warm {
  background: $bg-card;
  border-radius: $radius-2xl;
  box-shadow: $shadow-warm-card;
  border: 1rpx solid $warm-100;
}

/* ===== 区块标题 ===== */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  padding: 0 $spacing-xs;
}

/* ===== 毛玻璃 ===== */
.glass {
  background: rgba(255, 253, 248, 0.82);
  backdrop-filter: blur(40rpx) saturate(180%);
  -webkit-backdrop-filter: blur(40rpx) saturate(180%);
}

/* ===== 品牌渐变 (旧兼容) ===== */
.bg-brand-gradient {
  background: linear-gradient(165deg, $brand-700 0%, $brand-600 45%, $brand-500 100%);
}

/* ===== 统一点击反馈 ===== */
.tap-feedback {
  transition: transform $transition-fast ease, opacity $transition-fast ease;

  &:active {
    transform: scale(0.985);
    opacity: 0.9;
  }
}

/* ===== 骨架屏 ===== */
.skeleton-block {
  background: linear-gradient(90deg, $warm-100 25%, $warm-200 37%, $warm-100 63%);
  background-size: 200% 100%;
  animation: shimmer-block 1.5s infinite ease-in-out;
  border-radius: $radius-md;
}

@keyframes shimmer-block {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

/* ===== Stitch 页面标题（大号、粗体） ===== */
.stitch-page-title {
  font-size: $font-3xl;
  font-weight: 900;
  line-height: 1.2;
  letter-spacing: 2rpx;
}

.stitch-page-subtitle {
  font-size: $font-base;
  font-weight: 400;
  opacity: 0.75;
  margin-top: $spacing-xs;
}

/* ===== Stitch FAB ===== */
.stitch-fab {
  position: fixed;
  right: 32rpx;
  bottom: 200rpx;
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  height: 96rpx;
  padding: 0 $spacing-xl 0 $spacing-lg;
  border-radius: $radius-full;
  background: $brand-600;
  color: $text-inverse;
  box-shadow: 0 8rpx 24rpx rgba(45, 106, 79, 0.3);
  z-index: 20;
  transition: transform $transition-fast ease;

  &:active {
    transform: scale(0.93);
  }

  &__label {
    font-size: $font-sm;
    font-weight: 700;
    color: $text-inverse;
  }

  &--autumn {
    background: $autumn-400;
    box-shadow: 0 8rpx 24rpx rgba(212, 163, 115, 0.35);
  }
}

/* ===== Stitch Badge/Tag ===== */
.stitch-tag {
  display: inline-flex;
  align-items: center;
  padding: 6rpx 16rpx;
  border-radius: $radius-full;
  font-size: $font-xs;
  font-weight: 600;

  &--brand {
    background: $brand-50;
    color: $brand-600;
  }

  &--autumn {
    background: $autumn-50;
    color: $autumn-500;
  }

  &--warm {
    background: $warm-100;
    color: $warm-500;
  }

  &--accent {
    background: $accent-50;
    color: $accent-400;
  }
}
</style>
