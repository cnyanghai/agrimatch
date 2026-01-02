/**
 * 轻量的路由预加载（Prefetch）
 * - 目标：降低用户第一次进入高频页面时的等待
 * - 策略：首屏渲染完成后，在浏览器空闲时触发，避免影响首屏性能
 */

type Importer = () => Promise<unknown>

const importers: Record<string, Importer> = {
  // Gemini 前台高频
  landing: () => import('../views/gemini/GeminiLandingView.vue'),
  hallSupply: () => import('../views/gemini/GeminiSupplyHallView.vue'),
  hallNeed: () => import('../views/gemini/GeminiNeedHallView.vue'),
  talks: () => import('../views/gemini/GeminiTalksView.vue'),
  talkDetail: () => import('../views/gemini/GeminiTalkDetailView.vue'),

  // 控制台/核心
  chat: () => import('../views/ChatView.vue'),
  profile: () => import('../views/ProfileView.vue'),
  console: () => import('../views/ConsoleHomeView.vue'),
}

function runIdle(cb: () => void, timeout = 2000) {
  const ric = (window as any).requestIdleCallback as undefined | ((fn: () => void, opts?: { timeout?: number }) => void)
  if (ric) {
    ric(cb, { timeout })
    return
  }
  window.setTimeout(cb, 500)
}

export function prefetchCommonRoutes() {
  runIdle(() => {
    // 只预加载“下一跳”更可能进入的页面，保持克制，避免浪费流量
    const keys: Array<keyof typeof importers> = ['hallSupply', 'hallNeed', 'talks', 'chat', 'profile']
    for (const k of keys) {
      const fn = importers[k]
      if (!fn) continue
      fn().catch(() => {
        // 忽略：预加载失败不应影响主流程
      })
    }
  })
}


