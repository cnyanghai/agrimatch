import { ref, computed, onMounted, onUnmounted, type Ref, shallowRef } from 'vue'

interface VirtualScrollerOptions<T> {
  items: Ref<T[]>
  containerHeight: number
  bufferSize?: number
  estimateHeight?: (item: T, index: number) => number
}

export function useVirtualScroller<T>({
  items,
  containerHeight,
  bufferSize = 3,
  estimateHeight = () => 80
}: VirtualScrollerOptions<T>) {
  const scrollTop = ref(0)
  const containerRef = ref<HTMLElement>()
  const itemHeights = shallowRef<number[]>([])

  const startIndex = computed(() => {
    let accumulatedHeight = 0
    for (let i = 0; i < items.value.length; i++) {
      const item = items.value[i]
      if (item === undefined) continue
      const height = itemHeights.value[i] || estimateHeight(item, i)
      if (accumulatedHeight + height > scrollTop.value) {
        return Math.max(0, i - bufferSize)
      }
      accumulatedHeight += height
    }
    return Math.max(0, items.value.length - bufferSize - 1)
  })

  const endIndex = computed(() => {
    let accumulatedHeight = 0
    const targetHeight = scrollTop.value + containerHeight
    for (let i = 0; i < items.value.length; i++) {
      const item = items.value[i]
      if (item === undefined) continue
      const height = itemHeights.value[i] || estimateHeight(item, i)
      if (accumulatedHeight > targetHeight) {
        return Math.min(items.value.length, i + bufferSize)
      }
      accumulatedHeight += height
    }
    return items.value.length
  })

  const totalHeight = computed(() => {
    return items.value.reduce((sum, item, i) => {
      if (item === undefined) return sum
      return sum + (itemHeights.value[i] || estimateHeight(item, i))
    }, 0)
  })

  const offsetY = computed(() => {
    let offset = 0
    for (let i = 0; i < startIndex.value; i++) {
      const item = items.value[i]
      if (item === undefined) continue
      offset += itemHeights.value[i] || estimateHeight(item, i)
    }
    return offset
  })

  const visibleItems = computed(() => items.value.slice(startIndex.value, endIndex.value))

  function handleScroll(e: Event) {
    const target = e.target as HTMLElement
    scrollTop.value = target.scrollTop
  }

  function updateItemHeight(index: number, height: number) {
    const newHeights = [...itemHeights.value]
    newHeights[index] = height
    itemHeights.value = newHeights
  }

  onMounted(() => {
    containerRef.value?.addEventListener('scroll', handleScroll)
  })

  onUnmounted(() => {
    containerRef.value?.removeEventListener('scroll', handleScroll)
  })

  return {
    containerRef,
    visibleItems,
    offsetY,
    totalHeight,
    updateItemHeight
  }
}
