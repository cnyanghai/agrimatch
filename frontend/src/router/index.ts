import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { useUiStore } from '../store/ui'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: () => import('../views/LoginView.vue'), meta: { public: true, minimal: true } },
    { path: '/', name: 'landing', component: () => import('../views/gemini/GeminiLandingView.vue'), meta: { public: true, minimal: true } },
    { path: '/hall/supply', name: 'hall-supply', component: () => import('../views/gemini/GeminiSupplyHallView.vue'), meta: { public: true, minimal: true } },
    { path: '/hall/need', name: 'hall-need', component: () => import('../views/gemini/GeminiNeedHallView.vue'), meta: { public: true, minimal: true } },
    { path: '/talks', name: 'talks', component: () => import('../views/gemini/GeminiTalksView.vue'), meta: { public: true, minimal: true } },
    { path: '/talks/:id', name: 'talk-detail', component: () => import('../views/gemini/GeminiTalkDetailView.vue'), meta: { public: true, minimal: true } },
    // 见闻页面暂时下线（功能未完善），重定向到话题广场
    { path: '/insights', redirect: '/talks' },
    { path: '/talks/publish', name: 'talks-publish', component: () => import('../views/gemini/GeminiTalksPublishView.vue'), meta: { minimal: true, requiresAuth: true } },

    { path: '/console', name: 'console', component: () => import('../views/ConsoleHomeView.vue') },
    { path: '/profile', name: 'profile', component: () => import('../views/ProfileView.vue') },
    { path: '/requirements', name: 'requirements', component: () => import('../views/RequirementDemoView.vue') },
    { path: '/requirements/published', name: 'requirements-published', component: () => import('../views/RequirementPublishedView.vue') },
    { path: '/requirement-browse', name: 'requirement-browse', component: () => import('../views/RequirementBrowseView.vue') },
    { path: '/supply', name: 'supply', component: () => import('../views/SupplyView.vue') },
    { path: '/supply/published', name: 'supply-published', component: () => import('../views/SupplyPublishedView.vue') },
    { path: '/supply-browse', name: 'supply-browse', component: () => import('../views/SupplyBrowseView.vue') },
    { path: '/map', name: 'map', component: () => import('../views/MapView.vue') },
    { path: '/contracts', name: 'contracts', component: () => import('../views/ContractListView.vue') },
    { path: '/contracts/old', name: 'contracts-old', component: () => import('../views/ContractView.vue') },
    { path: '/contracts/:id', name: 'contract-detail', component: () => import('../views/ContractDetailView.vue') },
    { path: '/posts', name: 'posts', component: () => import('../views/PostsView.vue') },
    { path: '/points', name: 'points', component: () => import('../views/PointsView.vue') },
    { path: '/notify', name: 'notify', component: () => import('../views/NotifyView.vue') },
    { path: '/chat', name: 'chat', component: () => import('../views/ChatView.vue') },
    { path: '/vehicles', name: 'vehicles', component: () => import('../views/VehicleManageView.vue') }
  ]
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  const ui = useUiStore()

  // 显式 public 直接放行
  if (to.meta.public) return true

  // Cookie 登录态：不再依赖 auth.token，优先用 /api/auth/me 恢复会话
  if (!auth.me) {
    try {
      await auth.fetchMe()
    } catch {
      auth.clear()
      ui.openAuthDialog('login', { path: to.path, query: to.query as any })
      return false
    }
  }
  return true
})

export default router


