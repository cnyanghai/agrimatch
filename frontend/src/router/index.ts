import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { useUiStore } from '../store/ui'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'landing', component: () => import('../views/HomeView.vue'), meta: { public: true, minimal: true, title: 'AgriMatch - 领先的农牧原料数字化交易平台' } },
    { path: '/hall/supply', name: 'hall-supply', component: () => import('../views/SupplyHallView.vue'), meta: { public: true, minimal: true, title: '供应大厅 - 实时现货货源直供 - AgriMatch' } },
    { path: '/hall/need', name: 'hall-need', component: () => import('../views/PurchaseHallView.vue'), meta: { public: true, minimal: true, title: '采购大厅 - 精准匹配采购需求 - AgriMatch' } },
    { path: '/talks', name: 'talks', component: () => import('../views/TopicSquareView.vue'), meta: { public: true, minimal: true, title: '话题广场 - 行业深度交流与资讯 - AgriMatch' } },
    { path: '/talks/:id', name: 'talk-detail', component: () => import('../views/TopicDetailView.vue'), meta: { public: true, minimal: true, title: '话题详情 - AgriMatch' } },
    { path: '/talks/publish', name: 'talks-publish', component: () => import('../views/TopicPublishView.vue'), meta: { minimal: true, requiresAuth: true, title: '发布话题 - AgriMatch' } },

    { path: '/console', name: 'console', component: () => import('../views/DashboardView.vue'), meta: { title: '工作台 - AgriMatch' } },
    { path: '/profile', name: 'profile', component: () => import('../views/AccountSettingsView.vue'), meta: { title: '个人中心 - AgriMatch' } },
    { path: '/requirements', name: 'requirements', component: () => import('../views/MyPurchaseManageView.vue'), meta: { title: '我的采购管理 - AgriMatch' } },
    { path: '/requirements/published', name: 'requirements-published', component: () => import('../views/MyPurchaseListView.vue'), meta: { title: '已发布的采购 - AgriMatch' } },
    { path: '/requirement-browse', name: 'requirement-browse', component: () => import('../views/MarketPurchaseBrowseView.vue'), meta: { title: '市场采购浏览 - AgriMatch' } },
    { path: '/supply', name: 'supply', component: () => import('../views/MySupplyManageView.vue'), meta: { title: '我的供应管理 - AgriMatch' } },
    { path: '/supply/published', name: 'supply-published', component: () => import('../views/MySupplyListView.vue'), meta: { title: '已发布的供应 - AgriMatch' } },
    { path: '/supply-browse', name: 'supply-browse', component: () => import('../views/MarketSupplyBrowseView.vue'), meta: { title: '市场供应浏览 - AgriMatch' } },
    { path: '/map', name: 'map', component: () => import('../views/BusinessMapView.vue'), meta: { title: '地图找商 - AgriMatch' } },
    { path: '/contracts', name: 'contracts', component: () => import('../views/ContractListView.vue'), meta: { title: '合同管理 - AgriMatch' } },
    { path: '/contracts/:id', name: 'contract-detail', component: () => import('../views/ContractDetailView.vue'), meta: { title: '合同详情 - AgriMatch' } },
    { path: '/posts', name: 'posts', component: () => import('../views/CommunityForumView.vue'), meta: { title: '文章话题 - AgriMatch' } },
    { path: '/points', name: 'points', component: () => import('../views/UserPointsView.vue'), meta: { title: '我的积分 - AgriMatch' } },
    { path: '/notify', name: 'notify', component: () => import('../views/NotificationView.vue'), meta: { title: '消息通知 - AgriMatch' } },
    { path: '/chat', name: 'chat', component: () => import('../views/BusinessChatView.vue'), meta: { title: '即时通讯 - AgriMatch' } },
    { path: '/vehicles', name: 'vehicles', component: () => import('../views/LogisticsVehicleView.vue'), meta: { title: '物流车辆管理 - AgriMatch' } },
    { path: '/design', name: 'design-showcase', component: () => import('../views/DesignShowcaseView.vue'), meta: { title: '设计规范预览 - AgriMatch', public: true, minimal: true } },
    
    // 名录与公司详情
    { path: '/companies/directory', name: 'company-directory', component: () => import('../views/CompanyDirectoryView.vue'), meta: { public: true, minimal: true, title: '企业名录 - 优质供应商与采购商黄页 - AgriMatch' } },
    { path: '/companies/:id', name: 'company-profile', component: () => import('../views/CompanyProfileView.vue'), meta: { public: true, minimal: true, title: '企业主页 - AgriMatch' } }
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

router.afterEach((to) => {
  const defaultTitle = 'AgriMatch - 农牧原料供需匹配平台'
  document.title = (to.meta.title as string) || defaultTitle
})

export default router


