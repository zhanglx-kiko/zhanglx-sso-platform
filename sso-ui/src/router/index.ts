import {
  RouterView,
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
  type Router,
} from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { useMenuStore } from '@/stores/menu'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import type { MenuItem } from '@/types/menu'
import { ROUTE_WHITE_LIST } from '@/constants'
import { logoutAndRedirect, registerAuthRouter } from '@/utils/auth'
import { resolvePermissionBootstrapFailure } from '@/utils/authFailure'
import { probeAuthSessionStatus } from '@/utils/authSessionProbe'

NProgress.configure({ showSpinner: false })

export const staticRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/AuthLogin.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { title: '页面不存在' },
  },
]

export const layoutRoute: RouteRecordRaw = {
  path: '/',
  name: 'Layout',
  component: () => import('@/layouts/BasicLayout.vue'),
  redirect: '/dashboard',
  children: [
    {
      path: 'dashboard',
      name: 'Dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { title: '仪表盘', requiresAuth: true },
    },
    {
      path: 'system/auth/user',
      name: 'UserMgr',
      component: () => import('@/views/system/auth/user/index.vue'),
      meta: { title: '用户管理', requiresAuth: true },
    },
    {
      path: 'system/auth/role',
      name: 'RoleMgr',
      component: () => import('@/views/system/auth/role/index.vue'),
      meta: { title: '角色管理', requiresAuth: true },
    },
    {
      path: 'system/auth/permission',
      name: 'MenuMgr',
      component: () => import('@/views/system/auth/permission/index.vue'),
      meta: { title: '权限管理', requiresAuth: true },
    },
    {
      path: 'system/auth/dept',
      name: 'DeptMgr',
      component: () => import('@/views/system/auth/dept/index.vue'),
      meta: { title: '部门管理', requiresAuth: true },
    },
    {
      path: 'system/auth/post',
      name: 'PostMgr',
      component: () => import('@/views/system/auth/post/index.vue'),
      meta: { title: '岗位管理', requiresAuth: true },
    },
    {
      path: 'system/auth/app',
      name: 'AppMgr',
      component: () => import('@/views/system/auth/app/index.vue'),
      meta: { title: '应用管理', requiresAuth: true },
    },
    {
      path: 'system/auth/member',
      name: 'MemberMgr',
      component: () => import('@/views/system/auth/member/index.vue'),
      meta: { title: '会员管理', requiresAuth: true },
    },
    {
      path: 'system/auth/log',
      name: 'LogAudit',
      component: () => import('@/views/system/auth/log/index.vue'),
      meta: { title: '日志审计', requiresAuth: true },
    },
    {
      path: 'system/auth/config',
      name: 'ConfigMgr',
      component: () => import('@/views/system/auth/config/index.vue'),
      meta: { title: '系统参数', requiresAuth: true },
    },
    {
      path: 'system/auth/dict',
      name: 'DictMgr',
      component: () => import('@/views/system/auth/dict/index.vue'),
      meta: { title: '字典管理', requiresAuth: true },
    },
  ],
}

export const fallbackMenus: MenuItem[] = [
  {
    id: 'dashboard',
    name: 'Dashboard',
    path: '/dashboard',
    icon: 'PieChart',
    title: '仪表盘',
  },
  {
    id: 'system',
    name: 'SystemInfra',
    path: '/system',
    icon: 'Setting',
    title: '系统基础设施',
    children: [
      {
        id: 'system-auth',
        name: 'PermissionCenter',
        path: '/system/auth',
        icon: 'Lock',
        title: '权限管控',
        children: [
          {
            id: 'users',
            name: 'UserMgr',
            path: '/system/auth/user',
            icon: 'User',
            title: '用户管理',
          },
          {
            id: 'system-role',
            name: 'RoleMgr',
            path: '/system/auth/role',
            icon: 'UserFilled',
            title: '角色管理',
          },
          {
            id: 'system-menu',
            name: 'MenuMgr',
            path: '/system/auth/permission',
            icon: 'List',
            title: '权限管理',
          },
          {
            id: 'system-dept',
            name: 'DeptMgr',
            path: '/system/auth/dept',
            icon: 'Folder',
            title: '部门管理',
          },
          {
            id: 'system-post',
            name: 'PostMgr',
            path: '/system/auth/post',
            icon: 'Tickets',
            title: '岗位管理',
          },
          {
            id: 'system-app',
            name: 'AppMgr',
            path: '/system/auth/app',
            icon: 'Grid',
            title: '应用管理',
          },
          {
            id: 'system-member',
            name: 'MemberMgr',
            path: '/system/auth/member',
            icon: 'Avatar',
            title: '会员管理',
          },
          {
            id: 'system-log',
            name: 'LogAudit',
            path: '/system/auth/log',
            icon: 'DocumentChecked',
            title: '日志审计',
          },
          {
            id: 'system-config',
            name: 'ConfigMgr',
            path: '/system/auth/config',
            icon: 'Setting',
            title: '系统参数',
          },
          {
            id: 'system-dict',
            name: 'DictMgr',
            path: '/system/auth/dict',
            icon: 'Document',
            title: '字典管理',
          },
        ],
      },
    ],
  },
]

const router: Router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: staticRoutes,
})

const addDynamicRoutes = (routes: RouteRecordRaw[]): void => {
  routes.forEach((route) => {
    if (!router.hasRoute(route.name as string)) {
      router.addRoute('Layout', route)
    }
  })

  if (!router.hasRoute('CatchAll')) {
    router.addRoute({
      path: '/:pathMatch(.*)*',
      name: 'CatchAll',
      redirect: '/404',
    })
  }
}

const initDynamicRoutes = async (): Promise<MenuItem[]> => {
  const permissionStore = usePermissionStore()
  const menuStore = useMenuStore()
  const userStore = useUserStore()

  try {
    const apiData = await permissionStore.fetchPermissions()

    if (!userStore.hasSession()) {
      return []
    }

    if (!apiData || apiData.length === 0) {
      console.warn('No permission data from API, using fallback menus')
      return initFallbackRoutes()
    }

    const menus = permissionStore.transformMenus(apiData)
    const routes = permissionStore.generateRoutes(menus)
    const menuItems = permissionStore.appMenuToMenuItem(menus)
    const perms = permissionStore.extractPermissions(menus)

    permissionStore.setMenuList(menus)
    permissionStore.setDynamicRoutes(routes)
    permissionStore.setPermissions(perms)
    permissionStore.setLoaded(true)

    menuStore.setMenuList(menuItems)

    addDynamicRoutes(routes)

    return menuItems
  } catch (error) {
    const bootstrapFailure = resolvePermissionBootstrapFailure(error)
    if (bootstrapFailure.matched) {
      await logoutAndRedirect({
        message: bootstrapFailure.message,
      })
      return []
    }

    const sessionStatus = await probeAuthSessionStatus()
    if (sessionStatus && !sessionStatus.systemLoggedIn) {
      await logoutAndRedirect({
        message: '登录状态已失效，请重新登录',
      })
      return []
    }

    if (!userStore.hasSession()) {
      return []
    }

    console.error('Failed to fetch permissions:', error)
    return initFallbackRoutes()
  }
}

const initFallbackRoutes = (): MenuItem[] => {
  const permissionStore = usePermissionStore()
  const menuStore = useMenuStore()

  const fallbackRoutes: RouteRecordRaw[] = [
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { title: '仪表盘', requiresAuth: true },
    },
    {
      path: '/system',
      name: 'SystemInfra',
      component: RouterView,
      redirect: '/system/auth/user',
      meta: { title: '系统基础设施', requiresAuth: true },
      children: [
        {
          path: 'auth',
          name: 'PermissionCenter',
          component: RouterView,
          redirect: '/system/auth/user',
          meta: { title: '权限管控', requiresAuth: true },
          children: [
            {
              path: 'user',
              name: 'UserMgr',
              component: () => import('@/views/system/auth/user/index.vue'),
              meta: { title: '用户管理', requiresAuth: true },
            },
            {
              path: 'role',
              name: 'RoleMgr',
              component: () => import('@/views/system/auth/role/index.vue'),
              meta: { title: '角色管理', requiresAuth: true },
            },
            {
              path: 'permission',
              name: 'MenuMgr',
              component: () => import('@/views/system/auth/permission/index.vue'),
              meta: { title: '权限管理', requiresAuth: true },
            },
            {
              path: 'dept',
              name: 'DeptMgr',
              component: () => import('@/views/system/auth/dept/index.vue'),
              meta: { title: '部门管理', requiresAuth: true },
            },
            {
              path: 'post',
              name: 'PostMgr',
              component: () => import('@/views/system/auth/post/index.vue'),
              meta: { title: '岗位管理', requiresAuth: true },
            },
            {
              path: 'app',
              name: 'AppMgr',
              component: () => import('@/views/system/auth/app/index.vue'),
              meta: { title: '应用管理', requiresAuth: true },
            },
            {
              path: 'member',
              name: 'MemberMgr',
              component: () => import('@/views/system/auth/member/index.vue'),
              meta: { title: '会员管理', requiresAuth: true },
            },
            {
              path: 'log',
              name: 'LogAudit',
              component: () => import('@/views/system/auth/log/index.vue'),
              meta: { title: '日志审计', requiresAuth: true },
            },
            {
              path: 'config',
              name: 'ConfigMgr',
              component: () => import('@/views/system/auth/config/index.vue'),
              meta: { title: '系统参数', requiresAuth: true },
            },
            {
              path: 'dict',
              name: 'DictMgr',
              component: () => import('@/views/system/auth/dict/index.vue'),
              meta: { title: '字典管理', requiresAuth: true },
            },
          ],
        },
      ],
    },
  ]

  permissionStore.setDynamicRoutes(fallbackRoutes)
  permissionStore.setLoaded(true)
  menuStore.setMenuList(fallbackMenus)

  addDynamicRoutes(fallbackRoutes)

  return fallbackMenus
}

const isWhiteListPage = (path: string): boolean => {
  return ROUTE_WHITE_LIST.includes(path as (typeof ROUTE_WHITE_LIST)[number])
}

router.beforeEach(async (to, from, next) => {
  NProgress.start()

  const userStore = useUserStore()
  const permissionStore = usePermissionStore()
  const hasToken = userStore.hasToken()
  const hasSession = userStore.hasSession()

  if (hasToken && !hasSession) {
    await logoutAndRedirect({
      message: '登录信息已失效，请重新登录',
    })
    NProgress.done()
    next(false)
    return
  }

  if (to.meta.title) {
    document.title = `${to.meta.title} - 后台管理系统`
  }

  if (hasSession) {
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else {
      if (!permissionStore.isLoaded) {
        if (!router.hasRoute('Layout')) {
          router.addRoute(layoutRoute)
        }

        await initDynamicRoutes()

        if (!userStore.hasSession()) {
          NProgress.done()
          next(false)
          return
        }

        next({ path: to.fullPath, replace: true })
      } else {
        next()
      }
    }
  } else {
    if (isWhiteListPage(to.path)) {
      next()
    } else {
      next({
        path: '/login',
        query: { redirect: to.fullPath },
      })
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})

export const resetRouter = (): void => {
  const permissionStore = usePermissionStore()
  const dynamicRoutes = permissionStore.dynamicRoutes

  dynamicRoutes.forEach((route) => {
    if (route.name && router.hasRoute(route.name)) {
      router.removeRoute(route.name)
    }
  })

  if (router.hasRoute('CatchAll')) {
    router.removeRoute('CatchAll')
  }

  if (router.hasRoute('Layout')) {
    router.removeRoute('Layout')
  }

  permissionStore.clearPermission()
}

registerAuthRouter(router, resetRouter)

export default router

