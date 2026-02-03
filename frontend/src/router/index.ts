import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior(_to, _from, savedPosition) {
    return savedPosition || { top: 0 }
  },
  routes: [
    { path: '/', name: 'landing', component: () => import('../views/HomeView.vue'), meta: { public: true, minimal: true, title: '沃谷 - 领先的农牧原料数字化交易平台' } },
    { path: '/login', name: 'login', component: () => import('../views/LoginView.vue'), meta: { public: true, minimal: true, hideNav: true, title: '登录 - 沃谷' } },
    { path: '/hall/supply', name: 'hall-supply', component: () => import('../views/SupplyHallView.vue'), meta: { public: true, minimal: true, title: '供应大厅 - 实时现货货源直供 - 沃谷' } },
    { path: '/hall/need', name: 'hall-need', component: () => import('../views/PurchaseHallView.vue'), meta: { public: true, minimal: true, title: '采购大厅 - 精准匹配采购需求 - 沃谷' } },
    { path: '/search', name: 'search', component: () => import('../views/UnifiedSearchView.vue'), meta: { public: true, minimal: true, title: '全站搜索 - 沃谷' } },
    { path: '/talks', name: 'talks', component: () => import('../views/TopicSquareView.vue'), meta: { public: true, minimal: true, title: '话题广场 - 行业深度交流与资讯 - 沃谷' } },
    { path: '/talks/:id', name: 'talk-detail', component: () => import('../views/TopicDetailView.vue'), meta: { public: true, minimal: true, title: '话题详情 - 沃谷' } },
    { path: '/talks/publish', name: 'talks-publish', component: () => import('../views/TopicPublishView.vue'), meta: { minimal: true, requiresAuth: true, title: '发布话题 - 沃谷' } },
    { path: '/talks/:id/edit', name: 'talk-edit', component: () => import('../views/TopicEditView.vue'), meta: { minimal: true, requiresAuth: true, title: '编辑话题 - 沃谷' } },
    { path: '/talks/collected', name: 'collected-posts', component: () => import('../views/MyCollectedPostsView.vue'), meta: { requiresAuth: true, minimal: true, title: '我的收藏 - 沃谷' } },
    { path: '/talks/following', name: 'following-posts', component: () => import('../views/MyFollowingPostsView.vue'), meta: { requiresAuth: true, minimal: true, title: '关注动态 - 沃谷' } },
    { path: '/talks/following/users', name: 'following-users', component: () => import('../views/MyFollowingUsersView.vue'), meta: { requiresAuth: true, minimal: true, title: '我的关注 - 沃谷' } },
    { path: '/users/:id/posts', name: 'user-posts', component: () => import('../views/UserPostsView.vue'), meta: { public: true, minimal: true, title: '用户主页 - 沃谷' } },

    {
      path: '/categories',
      name: 'categories',
      component: () => import('../views/CategoryDirectoryView.vue'),
      meta: { public: true, minimal: true, title: '农牧原料分类目录 - 沃谷' }
    },

    { path: '/console', name: 'console', component: () => import('../views/DashboardView.vue'), meta: { title: '控制台首页 - 沃谷' } },
    { path: '/console/publish', name: 'publish-selection', component: () => import('../views/PublishSelectionView.vue'), meta: { title: '发布信息 - 沃谷' } },
    { path: '/console/following', name: 'following-list', component: () => import('../views/FollowingListView.vue'), meta: { title: '关注列表 - 沃谷' } },
    { path: '/profile', name: 'profile', component: () => import('../views/AccountSettingsView.vue'), meta: { title: '用户资料 - 沃谷' } },
    { path: '/points', name: 'points', component: () => import('../views/UserPointsView.vue'), meta: { title: '会员积分 - 沃谷' } },
    { path: '/requirements', name: 'requirements', component: () => import('../views/MyPurchaseManageView.vue'), meta: { title: '我的采购管理 - 沃谷' } },
    { path: '/requirements/published', redirect: { name: 'requirements', query: { tab: 'published' } } },
    { path: '/supply', name: 'supply', component: () => import('../views/MySupplyManageView.vue'), meta: { title: '我的供应管理 - 沃谷' } },
    { path: '/supply/published', redirect: { name: 'supply', query: { tab: 'published' } } },
    { path: '/map', name: 'map', component: () => import('../views/BusinessMapView.vue'), meta: { title: '地图找商 - 沃谷' } },
    { path: '/contracts', name: 'contracts', component: () => import('../views/ContractListView.vue'), meta: { title: '合同管理 - 沃谷' } },
    { path: '/contracts/:id', name: 'contract-detail', component: () => import('../views/ContractDetailView.vue'), meta: { title: '合同详情 - 沃谷' } },
    { path: '/posts', name: 'posts', component: () => import('../views/CommunityForumView.vue'), meta: { title: '文章话题 - 沃谷' } },
    { path: '/notify', name: 'notify', component: () => import('../views/NotificationView.vue'), meta: { title: '消息通知 - 沃谷' } },
    { path: '/chat', name: 'chat', component: () => import('../views/NegotiationWorkspace.vue'), meta: { title: '聊天议价 - 沃谷' } },
    { path: '/vehicles', name: 'vehicles', component: () => import('../views/LogisticsVehicleView.vue'), meta: { title: '物流车辆管理 - 沃谷' } },
    // 管理后台（嵌套路由）
    {
      path: '/admin',
      component: () => import('../views/admin/AdminLayout.vue'),
      meta: { requiresAdmin: true },
      children: [
        { path: '', name: 'admin-dashboard', component: () => import('../views/admin/AdminDashboardView.vue'), meta: { title: '管理后台 - 沃谷' } },
        { path: 'users', name: 'admin-users', component: () => import('../views/admin/AdminUsersView.vue'), meta: { title: '用户管理 - 沃谷' } },
        { path: 'companies', name: 'admin-companies', component: () => import('../views/admin/AdminCompaniesView.vue'), meta: { title: '企业管理 - 沃谷' } },
        { path: 'listings', name: 'admin-listings', component: () => import('../views/admin/AdminListingsView.vue'), meta: { title: '信息审核 - 沃谷' } },
        { path: 'posts', name: 'admin-posts', component: () => import('../views/admin/AdminPostsView.vue'), meta: { title: '话题管理 - 沃谷' } },
        { path: 'jd-redeems', name: 'admin-jd-redeems', component: () => import('../views/JdRedeemManageView.vue'), meta: { title: '京东卡发卡管理 - 沃谷' } },
        { path: 'points-manage', name: 'admin-points-manage', component: () => import('../views/admin/AdminPointsManageView.vue'), meta: { title: '积分管理 - 沃谷' } }
      ]
    },

    // 名录与公司详情
    { path: '/companies/directory', name: 'company-directory', component: () => import('../views/CompanyDirectoryView.vue'), meta: { public: true, minimal: true, title: '企业名录 - 优质供应商与采购商黄页 - 沃谷' } },
    { path: '/companies/:id', name: 'company-profile', component: () => import('../views/CompanyProfileView.vue'), meta: { public: true, minimal: true, title: '企业主页 - 沃谷' } },

    // 法律与合规
    { path: '/legal/:type', name: 'legal', component: () => import('../views/LegalPageView.vue'), meta: { public: true, minimal: true, title: '法律文档 - 沃谷' } }
  ]
})

// 备案模式下受限的路由路径
const filingRestrictedPaths = ['/points', '/chat', '/contracts', '/console/publish', '/talks/publish', '/admin']

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  // 拉取站点配置（仅首次）
  await auth.fetchConfig()

  // Already logged in user visiting /login → redirect away
  if (to.path === '/login') {
    if (auth.me) {
      return (to.query.redirect as string) || '/console'
    }
    if (auth.token) {
      try {
        await auth.fetchMe()
        return (to.query.redirect as string) || '/console'
      } catch {
        auth.clear()
      }
    }
    return true
  }

  // 备案模式下拦截受限路由
  if (auth.isFilingMode && filingRestrictedPaths.some(p => to.path.startsWith(p))) {
    return { path: '/' }
  }

  // 显式 public 直接放行
  if (to.meta.public) return true

  // Cookie 登录态：不再依赖 auth.token，优先用 /api/auth/me 恢复会话
  if (!auth.me) {
    try {
      await auth.fetchMe()
    } catch {
      auth.clear()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }

  // 管理员路由守卫
  if (to.matched.some(r => r.meta.requiresAdmin) && !auth.isAdmin) {
    return { name: 'console' }
  }

  return true
})

router.afterEach((to) => {
  const defaultTitle = '沃谷 - 农牧供需智能匹配平台'
  document.title = (to.meta.title as string) || defaultTitle
  // 滚动内部容器到顶部（App.vue 用 overflow-hidden 包裹，window.scrollTo 无效）
  document.getElementById('main-scroll')?.scrollTo({ top: 0 })
})

export default router


