import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

// 布局组件
import Layout from '@/layout/index.vue'

/**
 * 路由配置
 * @type {import('vue-router').RouteRecordRaw[]}
 */
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册', hidden: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: (to) => {
      // 根据是否有token决定重定向目标
      return localStorage.getItem('token') ? '/dashboard' : '/login';
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'dashboard' }
      }
    ]
  },
  {
    path: '/profile',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', hidden: true }
      }
    ]
  },
  {
    path: '/change-password',
    component: Layout,
    children: [
      {
        path: '',
        name: 'ChangePassword',
        component: () => import('@/views/profile/change-password.vue'),
        meta: { title: '修改密码', hidden: true }
      }
    ]
  },
  
  // 项目管理路由
  {
    path: '/project',
    component: Layout,
    meta: { title: '项目管理', icon: 'folder' },
    children: [
      {
        path: 'list',
        name: 'ProjectList',
        component: () => import('@/views/project/list.vue'),
        meta: { title: '项目列表', icon: 'list' }
      },
      {
        path: 'apply',
        name: 'ProjectApply',
        component: () => import('@/views/project/apply.vue'),
        meta: { title: '立项申请', icon: 'plus' }
      },
      {
        path: 'detail/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/detail.vue'),
        meta: { title: '项目详情', hidden: true }
      },
      {
        path: 'edit/:id',
        name: 'ProjectEdit',
        component: () => import('@/views/project/edit.vue'),
        meta: { title: '编辑项目', hidden: true }
      },
      {
        path: 'completion/:id',
        name: 'ProjectCompletion',
        component: () => import('@/views/project/completion.vue'),
        meta: { title: '提交结题报告', hidden: true }
      },
      {
        path: 'completion-audit/:id',
        name: 'ProjectCompletionAudit',
        component: () => import('@/views/project/completion-audit.vue'),
        meta: { title: '项目结题审核', hidden: true }
      },
      {
        path: 'fund-arrival',
        name: 'ProjectFundArrival',
        component: () => import('@/views/project/fund-arrival/list.vue'),
        meta: { 
          title: '经费到账管理', 
          icon: 'money',
        }
      },
      {
        path: 'fund-arrival/detail/:id',
        name: 'ProjectFundArrivalDetail',
        component: () => import('@/views/project/fund-arrival/detail.vue'),
        meta: { 
          title: '经费到账详情', 
          hidden: true
        }
      }
    ]
  },
  
  // 经费管理路由
  {
    path: '/expense',
    component: Layout,
    meta: { 
      title: '经费管理', 
      icon: 'money',
      roles: ['ROLE_RESEARCHER', 'ROLE_ADMIN'] // 科研人员和管理员可见
    },
    children: [
      {
        path: 'apply/list',
        name: 'ExpenseApplyList',
        component: () => import('@/views/expense/apply/list.vue'),
        meta: { title: '经费申请列表', icon: 'list' }
      },
      {
        path: 'apply/create',
        name: 'ExpenseApplyCreate',
        component: () => import('@/views/expense/apply/create.vue'),
        meta: { title: '新建经费申请', hidden: true }
      },
      {
        path: 'apply/edit/:id',
        name: 'ExpenseApplyEdit',
        component: () => import('@/views/expense/apply/edit.vue'),
        meta: { title: '编辑经费申请', hidden: true }
      },
      {
        path: 'apply/detail/:id',
        name: 'ExpenseApplyDetail',
        component: () => import('@/views/expense/apply/detail.vue'),
        meta: { title: '经费申请详情', hidden: true }
      },
      {
        path: 'transfer/list',
        name: 'ExpenseTransferList',
        component: () => import('@/views/expense/transfer/list.vue'),
        meta: { title: '经费结转列表', icon: 'list' }
      },
      {
        path: 'transfer/create',
        name: 'ExpenseTransferCreate',
        component: () => import('@/views/expense/transfer/create.vue'),
        meta: { title: '新建经费结转', hidden: true }
      },
      {
        path: 'transfer/edit/:id',
        name: 'ExpenseTransferEdit',
        component: () => import('@/views/expense/transfer/edit.vue'),
        meta: { title: '编辑经费结转', hidden: true }
      },
      {
        path: 'transfer/detail/:id',
        name: 'ExpenseTransferDetail',
        component: () => import('@/views/expense/transfer/detail.vue'),
        meta: { title: '经费结转详情', hidden: true }
      }
    ]
  },
  
  // 任务管理路由
  {
    path: '/task',
    component: Layout,
    meta: { 
      title: '任务管理', 
      icon: 'task',
      roles: ['ROLE_ADMIN'] // 仅管理员可见
    },
    children: [
      {
        path: 'list',
        name: 'TaskList',
        component: () => import('@/views/task/list.vue'),
        meta: { title: '任务列表', icon: 'list' }
      }
    ]
  },

    // 成果管理路由
    {
      path: '/achievement',
      component: Layout,
      meta: { 
        title: '成果管理', 
        icon: 'achievement',
      },
      children: [
        {
          path: 'list',
          name: 'AchievementList',
          component: () => import('@/views/achievement/list.vue'),
          meta: { title: '成果列表', icon: 'list' }
        },
        {
          path: 'create',
          name: 'AchievementCreate',
          component: () => import('@/views/achievement/create.vue'),
          meta: { title: '新增成果', hidden: true }
        },
        {
          path: 'edit/:id',
          name: 'AchievementEdit',
          component: () => import('@/views/achievement/edit.vue'),
          meta: { title: '编辑成果', hidden: true }
        },
        {
          path: 'detail/:id',
          name: 'AchievementDetail',
          component: () => import('@/views/achievement/detail.vue'),
          meta: { title: '成果详情', hidden: true }
        },
        {
          path: 'audit/:id',
          name: 'AchievementAudit',
          component: () => import('@/views/achievement/audit.vue'),
          meta: { title: '成果审核', hidden: true }
        }
      ]
    },
  
  // 系统管理路由 - 仅管理员可见
  {
    path: '/system',
    component: Layout,
    meta: { 
      title: '系统管理', 
      icon: 'setting',
      roles: ['ROLE_ADMIN'] // 仅管理员可见
    },
    children: [
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/user/index.vue'),
        meta: { 
          title: '用户管理', 
          icon: 'user',
          roles: ['ROLE_ADMIN'] // 仅管理员可见
        }
      }
    ]
  },
  
  // 404页面必须放在最后
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/error/404.vue'),
    meta: { hidden: true }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 始终滚动到顶部
    return { top: 0 }
  }
})

// 导航后回调
router.afterEach((to, from) => {
  console.log('导航完成:', to.path)
})

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 科研经费管理系统` : '科研经费管理系统'
  
  // 从本地存储获取token
  const token = localStorage.getItem('token')
  
  // 如果前往登录页且已有token，则重定向到首页
  if (to.path === '/login' && token) {
    next({ path: '/dashboard' })
  } 
  // 如果前往注册页且已有token，则重定向到首页
  else if (to.path === '/register' && token) {
    next({ path: '/dashboard' })
  }
  // 如果不是登录页或注册页但没有token，则重定向到登录页
  else if (to.path !== '/login' && to.path !== '/register' && !token) {
    next({ path: '/login' })
  } 
  // 其他情况正常放行
  else {
    // 如果有token且要访问的不是登录/注册页面，但用户信息不存在，则获取用户信息
    if (token && to.path !== '/login' && to.path !== '/register') {
      const userStore = useUserStore()
      // 只有当用户名为空时才获取用户信息，避免重复请求
      if (!userStore.name) {
        try {
          await userStore.getUserInfo()
        } catch (error) {
          console.error('获取用户信息失败:', error)
          // 如果获取用户信息失败，可能是token失效，清除token并跳转到登录页
          localStorage.removeItem('token')
          next({ path: '/login' })
          return
        }
      }
      
      // 检查页面访问权限
      // 如果路由配置了roles，则检查用户是否有对应的角色
      if (to.meta.roles && to.meta.roles.length > 0) {
        const hasRole = userStore.roles.some(role => to.meta.roles.includes(role))
        if (!hasRole) {
          // 没有权限，跳转到首页
          next({ path: '/dashboard' })
          return
        }
      }
    }
    next()
  }
})

export default router 