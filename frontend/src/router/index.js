import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 默认路由重定向
    {
      path: '/',
      redirect: '/manager',
    },
    {
      path:'/1',
      component: () => import('../views/Game/Dragon.vue'),
    },
    {
      path: '/manager',
      name: '首页',
      component: () => import('../views/Manager.vue'),
      children: [
        {
          path: 'info',
          name: '个人资料',
          component: () => import('../views/UserInfo.vue'),
        },
        {
          path: 'test',
          name: '测试页面',
          component: () => import('../views/ElementTest.vue'),
        },
        {
          path: 'data',
          name: '数据详情',
          component: () => import('../views/Data.vue'),
        },
        {
          path: 'admin',
          name: '用户资料',
          component: () => import('../views/Admin.vue'),
        },
        {
          path: 'article',
          name: '文章仓库',
          component: () => import('../views/Article.vue'),
        }]
    },
    {
      path: '/login',
      name: 'login',
      meta: {
        title: '登录账号'
      },
      component: () => import('../views/Login/Login.vue'),
    },
    {
      path: '/register',
      name: 'register',
      meta: {
        title: '欢迎注册'
      },
      component: () => import('../views/Login/Register.vue'),
    },
    {
      path: '/forgetpwd',
      name: 'forgetpwd',
      meta: {
        title: '重置密码'
      },
      component: () => import('../views/Login/Forgetpwd.vue'),
    },
    {
      path: '/home',
      name: 'home',
      meta: {
        title: '首页'
      },
      component: () => import('../views/Home.vue'),
    },
    {
      path: '/test',
      name: 'test',
      meta: {
        title: '测试页'
      },
      component: () => import('../views/ElementTest.vue'),
    },
    {
      path: '/404',
      name: '404',
      meta: {
        title: 'Not Found Page'
      },
      component: () => import('../views/404.vue'),
    },
    // 定义404路由，位于末位
    {
      path: '/:pathMatch(.*)',
      redirect: '/404'
    }
  ],
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) document.title = to.meta.title
  next()
})

export default router
