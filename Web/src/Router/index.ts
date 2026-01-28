import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [{
    path: '/',
    redirect: '/new'
  },{
    path: '/login',
    name: 'login',
    component: () => import('@/Views/Login/index.vue')
  },{
    path: '/register',
    name: 'register',
    component: () => import('@/Views/Register/index.vue')
  },{
    path: '/new',
    name: 'new',
    component: () => import('@/Views/New/index.vue')
  },{
    path: '/history',
    name: 'history',
    component: () => import('@/Views/History/index.vue')
  },{
    path: '/history/:conversationId',
    name: 'history-with-conversation',
    component: () => import('@/Views/History/index.vue')
  },{
    path: '/company',
    name: 'company',
    component: () => import('@/Views/Company/index.vue')
  },{
    path: '/datas',
    name: 'datas',
    component: () => import('@/Views/DataCenter/index.vue')
  },{
    path: '/tools',
    name: 'tools',
    component: () => import('@/Views/ToolCenter/index.vue')
  }],
})

// 路由守卫 - 检查登录状态
router.beforeEach(async (to, from, next) => {
  // 动态导入store避免循环依赖
  const { useAuthStore } = await import('@/Stores/AuthStore')
  const authStore = useAuthStore()
  
  // 公开路由，不需要登录验证
  const publicRoutes = ['/login', '/register', '/company-select']
  
  // 如果目标路由不是公开路由且用户未登录
  if (!publicRoutes.includes(to.path) && !authStore.isLogin) {
    // 重定向到登录页面
    next('/login')
  } else if (to.path === '/login' && authStore.isLogin) {
    // 如果用户已登录且访问登录页，重定向到首页
    next('/new')
  } else {
    // 否则继续导航
    next()
  }
})

export default router
