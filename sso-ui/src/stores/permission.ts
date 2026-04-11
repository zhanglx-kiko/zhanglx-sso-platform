import { h, resolveComponent } from 'vue'
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { RouterView, type RouteRecordRaw } from 'vue-router'
import { getPermissionsByIdentificationApi } from '@/api/permission'
import { useUserStore } from '@/stores/user'
import type { ApiPermission, AppMenu, MenuItem } from '@/types/menu'

// 使用 Vite 提供的 import.meta.glob 批量静态导入所有视图组件
const viewModules = import.meta.glob('../views/**/*.vue')

export const usePermissionStore = defineStore('permission', () => {
  const isLoaded = ref(false)
  const dynamicRoutes = ref<RouteRecordRaw[]>([])
  const menuList = ref<AppMenu[]>([])
  const permissions = ref<string[]>([]) // 存放按钮级权限标识

  /**
   * 工具方法：将扁平的权限列表转换为树形结构
   * @param list 扁平权限数组
   * @returns 树形权限数组
   */
  const listToTree = (list: ApiPermission[]): ApiPermission[] => {
    const map: Record<string, ApiPermission> = {}
    const roots: ApiPermission[] = []
    const sortNodes = (nodes: ApiPermission[]): ApiPermission[] => {
      return nodes
        .sort((a, b) => (a.displayNo ?? 0) - (b.displayNo ?? 0))
        .map((node) => ({
          ...node,
          children: node.children ? sortNodes(node.children) : [],
        }))
    }

    list.forEach((item) => {
      map[item.id] = { ...item, children: [] }
    })

    list.forEach((item) => {
      const node = map[item.id]
      if (!node) return

      if (item.parentId && String(item.parentId) !== '0' && map[item.parentId]) {
        map[item.parentId]?.children?.push(node)
      } else {
        roots.push(node)
      }
    })

    return sortNodes(roots)
  }

  /**
   * 工具方法：从扁平列表中提取所有操作级别的权限标识 (后端 2=按钮, 3=接口)
   */
  const extractButtonPermissions = (flatList: ApiPermission[]): string[] => {
    return flatList
      .filter((item) => item.type >= 2 && item.identification)
      .map((item) => item.identification)
  }

  /**
   * 兼容旧数据里“会员管理”仍然是空路由模块的情况，前端在运行时补齐菜单页面信息。
   * 这样老环境即使还没手工执行菜单修正 SQL，也能先把页面挂出来。
   */
  const normalizeLegacyPermissions = (flatList: ApiPermission[]): ApiPermission[] => {
    return flatList.map((item) => {
      if (item.identification !== 'system:auth:member') {
        return item
      }

      return {
        ...item,
        type: 1,
        path: item.path || '/system/auth/member',
        comPath: item.comPath || 'system/auth/member/index',
        iconStr: item.iconStr || 'Avatar',
      }
    })
  }

  /**
   * 1. 从后端获取当前登录用户的扁平权限数据，并处理成树形结构
   */
  const fetchPermissions = async (): Promise<ApiPermission[]> => {
    try {
      const userStore = useUserStore()
      const username = userStore.userInfo?.username

      if (!username) {
        console.error('未获取到当前登录用户的 username，无法拉取菜单权限')
        return []
      }

      const rawFlatList = await getPermissionsByIdentificationApi({ username })

      if (!rawFlatList || rawFlatList.length === 0) return []

      const allFlatList = normalizeLegacyPermissions(rawFlatList)

      const btnPerms = extractButtonPermissions(allFlatList)
      setPermissions(btnPerms)

      return listToTree(allFlatList)
    } catch (error) {
      console.error('获取用户权限菜单失败:', error)
      return []
    }
  }

  /**
   * 2. 将后端的 ApiPermission 树转换为前端中转的 AppMenu 树格式
   */
  const transformMenus = (nodes: ApiPermission[]): AppMenu[] => {
    const buildMenu = (treeNodes: ApiPermission[]): AppMenu[] => {
      return treeNodes
        .filter((node) => node.type < 2 && node.status !== 0)
        .map((node) => {
          const menu: AppMenu = {
            id: node.id,
            name:
              node.identification || (node.path ? node.path.replace(/\//g, '') : `Menu_${node.id}`),
            path: node.path || '',
            meta: {
              title: node.name,
              icon: node.iconStr,
              hidden: node.isFrame === 1,
              permission: node.identification,
            },
          }

          if (node.comPath) {
            ;(menu as any).componentPath = node.comPath
          }

          if (node.children && node.children.length > 0) {
            menu.children = buildMenu(node.children)
          }

          return menu
        })
    }

    return buildMenu(nodes)
  }

  /**
   * 3. 将 AppMenu 树转换为 Vue Router 可注册的 RouteRecordRaw 路由数组
   */
  const generateRoutes = (menus: AppMenu[], parentPath = ''): RouteRecordRaw[] => {
    return menus.map((menu) => {
      let routePath = menu.path.startsWith('/') ? menu.path : `/${menu.path}`

      if (parentPath && routePath.startsWith(parentPath + '/')) {
        routePath = routePath.substring(parentPath.length + 1)
      } else if (parentPath && routePath.startsWith(parentPath)) {
        routePath = routePath.substring(parentPath.length)
        if (routePath.startsWith('/')) {
          routePath = routePath.substring(1)
        }
      }

      const componentPath = (menu as any).componentPath
      const hasChildren = menu.children && menu.children.length > 0
      const fullPath = menu.path.startsWith('/') ? menu.path : `/${menu.path}`

      const route: any = {
        path: routePath,
        name: menu.name,
        meta: menu.meta,
      }

      if (hasChildren) {
        route.children = []
        route.component = RouterView
        const firstChildFullPath = menu.children?.[0]?.path || ''
        let firstChildRelativePath = firstChildFullPath
        if (firstChildFullPath.startsWith(fullPath + '/')) {
          firstChildRelativePath = firstChildFullPath.substring(fullPath.length + 1)
        }
        route.redirect = firstChildRelativePath
        route.children = generateRoutes(menu.children!, fullPath)
      } else if (componentPath) {
        const rawPath = componentPath.replace('.vue', '')
        const cleanPath = rawPath
        const matchKey = `../views/${rawPath}.vue`

        const viewComponent = viewModules[matchKey]
        if (!viewComponent) {
          console.error(
            `[动态路由错误] 致命：找不到前端组件文件 -> src/views/${cleanPath}.vue ，页面将降级为 404！请检查文件是否存在或路径拼写是否一致！`,
          )
        }

        route.component = viewComponent || (() => import('@/views/error/NotFound.vue'))
      } else {
        route.component = () => import('@/views/error/NotFound.vue')
      }

      return route
    })
  }

  /**
   * 3. 将 AppMenu 树转换为 Vue Router 可注册的 RouteRecordRaw 路由数组
   */
  // const generateRoutes = (menus: AppMenu[]): RouteRecordRaw[] => {
  //   return menus.map((menu) => {
  //     const route: RouteRecordRaw = {
  //       path: menu.path.startsWith('/') ? menu.path : `/${menu.path}`,
  //       name: menu.name,
  //       meta: menu.meta,
  //       children: [],
  //     }

  //     const componentPath = (menu as any).componentPath
  //     const hasChildren = menu.children && menu.children.length > 0

  //     // ============== 核心路由装配逻辑 ==============
  //     if (hasChildren) {
  //       // 【防坑 1】只要有子节点，强制作为嵌套容器 (忽略数据库里可能填错的 comPath)
  //       // 这样可以彻底避免父目录被渲染成 404，从而导致子路由全部挂掉
  //       route.component = RouterView

  //       // 【防坑 2】自动重定向到第一个子路由。
  //       // 防止用户点击父目录(如 /system)时出现白屏或跳 404
  //       const firstChildPath = menu.children[0].path
  //       route.redirect = firstChildPath.startsWith('/') ? firstChildPath : `/${firstChildPath}`
  //     } else if (componentPath) {
  //       // 叶子节点：正常解析本地 Vue 组件
  //       const cleanPath = componentPath.replace('.vue', '')
  //       const matchKey = `../views/${cleanPath}.vue`
  //       // 如果本地找不到该文件，才使用 404 页面兜底
  //       route.component = viewModules[matchKey] || (() => import('@/views/error/NotFound.vue'))
  //     } else {
  //       // 异常节点兜底
  //       route.component = () => import('@/views/error/NotFound.vue')
  //     }

  //     // 递归处理子节点
  //     if (hasChildren) {
  //       route.children = generateRoutes(menu.children)
  //     }

  //     return route
  //   })
  // }

  /**
   * 4. 将 AppMenu 树转换为 左侧菜单栏 (AppSidebar) 需要的 MenuItem 数组
   */
  const appMenuToMenuItem = (menus: AppMenu[]): MenuItem[] => {
    return menus.map((menu) => {
      const item: MenuItem = {
        id: menu.id,
        name: menu.name,
        path: menu.path,
        title: menu.meta.title,
        icon: menu.meta.icon,
        hidden: menu.meta.hidden,
        permission: menu.meta.permission,
      }

      if (menu.children && menu.children.length > 0) {
        item.children = appMenuToMenuItem(menu.children)
      }

      return item
    })
  }

  /**
   * 5. 这个方法已经在 fetchPermissions 中通过 extractButtonPe rmissions 完成了，
   * 留一个空实现或别名以兼容 router/index.ts 的调用逻辑。
   */
  const extractPermissions = (menus: AppMenu[]): string[] => {
    return permissions.value
  }

  /**
   * 判断当前账号是否拥有指定按钮权限，便于页面做按钮级显隐控制。
   */
  const hasPermission = (permission: string): boolean => {
    return !!permission && permissions.value.includes(permission)
  }

  /**
   * 判断当前账号是否命中任一按钮权限，适合“更多操作”这种聚合入口。
   */
  const hasAnyPermission = (perms: string[]): boolean => {
    return perms.some((permission) => hasPermission(permission))
  }

  // ============== State 变更方法 ==============

  const setMenuList = (menus: AppMenu[]) => {
    menuList.value = menus
  }

  const setDynamicRoutes = (routes: RouteRecordRaw[]) => {
    dynamicRoutes.value = routes
  }

  const setPermissions = (perms: string[]) => {
    permissions.value = perms
  }

  const setLoaded = (loaded: boolean) => {
    isLoaded.value = loaded
  }

  const clearPermission = () => {
    menuList.value = []
    dynamicRoutes.value = []
    permissions.value = []
    isLoaded.value = false
  }

  return {
    isLoaded,
    dynamicRoutes,
    menuList,
    permissions,
    fetchPermissions,
    transformMenus,
    generateRoutes,
    appMenuToMenuItem,
    extractPermissions,
    hasPermission,
    hasAnyPermission,
    setMenuList,
    setDynamicRoutes,
    setPermissions,
    setLoaded,
    clearPermission,
  }
})
