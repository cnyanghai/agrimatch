export function usePerformanceOptimization() {
  const debouncedRaf = (callback: () => void) => {
    let ticking = false
    return () => {
      if (!ticking) {
        window.requestAnimationFrame(() => {
          callback()
          ticking = false
        })
        ticking = true
      }
    }
  }

  const createPassiveScrollListener = (element: HTMLElement | Window, callback: () => void) => {
    let ticking = false

    element.addEventListener('scroll', () => {
      if (!ticking) {
        window.requestAnimationFrame(() => {
          callback()
          ticking = false
        })
        ticking = true
      }
    }, { passive: true })

    return () => {
      element.removeEventListener('scroll', () => { })
    }
  }

  return {
    debouncedRaf,
    createPassiveScrollListener
  }
}

export function createImagePreloader() {
  const loaded = new Set<string>()
  const loading = new Set<string>()

  const load = (url: string) => {
    if (loaded.has(url) || loading.has(url)) {
      return Promise.resolve()
    }

    loading.add(url)

    return new Promise<void>((resolve) => {
      const img = new Image()
      img.src = url
      img.onload = () => {
        loaded.add(url)
        loading.delete(url)
        resolve()
      }
      img.onerror = () => {
        loading.delete(url)
        resolve()
      }
    })
  }

  const preload = async (urlsToLoad: string[]) => {
    await Promise.all(urlsToLoad.map(load))
  }

  const isLoaded = (url: string) => loaded.has(url)

  return {
    load,
    preload,
    isLoaded
  }
}
