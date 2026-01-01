import { createRouter, createWebHistory } from 'vue-router'
import GeminiLandingView from '../views/gemini/GeminiLandingView.vue'
import GeminiSupplyHallView from '../views/gemini/GeminiSupplyHallView.vue'
import GeminiNeedHallView from '../views/gemini/GeminiNeedHallView.vue'
import GeminiTalksView from '../views/gemini/GeminiTalksView.vue'
import GeminiInsightsView from '../views/gemini/GeminiInsightsView.vue'
import GeminiTalksPublishView from '../views/gemini/GeminiTalksPublishView.vue'
import GeminiTalkDetailView from '../views/gemini/GeminiTalkDetailView.vue'
import LoginView from '../views/LoginView.vue'
import ProfileView from '../views/ProfileView.vue'
import RequirementDemoView from '../views/RequirementDemoView.vue'
import RequirementBrowseView from '../views/RequirementBrowseView.vue'
import SupplyView from '../views/SupplyView.vue'
import SupplyBrowseView from '../views/SupplyBrowseView.vue'
import PostsView from '../views/PostsView.vue'
import PointsView from '../views/PointsView.vue'
import NotifyView from '../views/NotifyView.vue'
import ChatView from '../views/ChatView.vue'
import MapView from '../views/MapView.vue'
import ContractView from '../views/ContractView.vue'
import { useAuthStore } from '../store/auth'
import { useUiStore } from '../store/ui'
import ConsoleHomeView from '../views/ConsoleHomeView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: LoginView, meta: { public: true, minimal: true } },
    { path: '/', name: 'landing', component: GeminiLandingView, meta: { public: true, minimal: true } },
    { path: '/hall/supply', name: 'hall-supply', component: GeminiSupplyHallView, meta: { public: true, minimal: true } },
    { path: '/hall/need', name: 'hall-need', component: GeminiNeedHallView, meta: { public: true, minimal: true } },
    { path: '/talks', name: 'talks', component: GeminiTalksView, meta: { public: true, minimal: true } },
    { path: '/talks/:id', name: 'talk-detail', component: GeminiTalkDetailView, meta: { public: true, minimal: true } },
    { path: '/insights', name: 'insights', component: GeminiInsightsView, meta: { public: true, minimal: true } },
    { path: '/talks/publish', name: 'talks-publish', component: GeminiTalksPublishView, meta: { minimal: true, requiresAuth: true } },

    { path: '/console', name: 'console', component: ConsoleHomeView },
    { path: '/profile', name: 'profile', component: ProfileView },
    { path: '/requirements', name: 'requirements', component: RequirementDemoView },
    { path: '/requirement-browse', name: 'requirement-browse', component: RequirementBrowseView },
    { path: '/supply', name: 'supply', component: SupplyView },
    { path: '/supply-browse', name: 'supply-browse', component: SupplyBrowseView },
    { path: '/map', name: 'map', component: MapView },
    { path: '/contracts', name: 'contracts', component: ContractView },
    { path: '/posts', name: 'posts', component: PostsView },
    { path: '/points', name: 'points', component: PointsView },
    { path: '/notify', name: 'notify', component: NotifyView },
    { path: '/chat', name: 'chat', component: ChatView }
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


