import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 下拉刷新 composable（纯原生实现，不依赖第三方库）
 *
 * 使用方式：
 *   const { isPulling, pullDistance, isRefreshing } = usePullToRefresh(containerRef, onRefresh)
 *   containerRef 是绑定到模板上的 ref 元素。
 */
export function usePullToRefresh(
  containerRef: ReturnType<typeof ref<HTMLElement | null>>,
  onRefresh: () => Promise<void>,
  options?: {
    threshold?: number      // 触发刷新的下拉距离（px），默认60
    maxDistance?: number     // 最大下拉距离（px），默认100
  }
) {
  const threshold = options?.threshold ?? 60
  const maxDistance = options?.maxDistance ?? 100

  const isPulling = ref(false)
  const pullDistance = ref(0)
  const isRefreshing = ref(false)

  let startY = 0
  let pulling = false

  function isAtTop(): boolean {
    // 检查页面是否在顶部（window 滚动或主滚动容器）
    const mainScroll = document.getElementById('main-scroll')
    if (mainScroll) {
      return mainScroll.scrollTop <= 0
    }
    return window.scrollY <= 0
  }

  function onTouchStart(e: TouchEvent) {
    if (isRefreshing.value) return
    if (!isAtTop()) return
    startY = e.touches[0]!.clientY
    pulling = false
  }

  function onTouchMove(e: TouchEvent) {
    if (isRefreshing.value) return
    if (startY === 0) return

    const currentY = e.touches[0]!.clientY
    const diff = currentY - startY

    // 只在向下拉且页面在顶部时激活
    if (diff > 0 && isAtTop()) {
      pulling = true
      isPulling.value = true
      // 阻尼效果：拉得越远阻力越大
      pullDistance.value = Math.min(diff * 0.4, maxDistance)
      if (pullDistance.value > 10) {
        e.preventDefault()
      }
    } else {
      pulling = false
      isPulling.value = false
      pullDistance.value = 0
    }
  }

  async function onTouchEnd() {
    if (!pulling) {
      startY = 0
      return
    }

    if (pullDistance.value >= threshold && !isRefreshing.value) {
      // 触发刷新
      isRefreshing.value = true
      pullDistance.value = 40 // 保持在刷新指示器位置
      try {
        await onRefresh()
      } finally {
        isRefreshing.value = false
      }
    }

    // 重置状态
    isPulling.value = false
    pullDistance.value = 0
    startY = 0
    pulling = false
  }

  onMounted(() => {
    const el = containerRef.value || document.body
    el.addEventListener('touchstart', onTouchStart, { passive: true })
    el.addEventListener('touchmove', onTouchMove, { passive: false })
    el.addEventListener('touchend', onTouchEnd, { passive: true })
  })

  onUnmounted(() => {
    const el = containerRef.value || document.body
    el.removeEventListener('touchstart', onTouchStart)
    el.removeEventListener('touchmove', onTouchMove)
    el.removeEventListener('touchend', onTouchEnd)
  })

  return {
    isPulling,
    pullDistance,
    isRefreshing
  }
}
