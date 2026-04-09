import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { MenuItem, BreadcrumbItem } from '@/types/menu'

export const useMenuStore = defineStore('menu', () => {
  const isCollapse = ref(false)
  const activeMenu = ref('')
  const menuList = ref<MenuItem[]>([])

  const toggleCollapse = () => {
    isCollapse.value = !isCollapse.value
  }

  const setActiveMenu = (path: string) => {
    activeMenu.value = path
  }

  const setMenuList = (menus: MenuItem[]) => {
    menuList.value = menus
  }

  const breadcrumbs = computed<BreadcrumbItem[]>(() => {
    const result: BreadcrumbItem[] = []
    if (!activeMenu.value) return result

    const findPath = (menus: MenuItem[], targetPath: string, path: BreadcrumbItem[]): boolean => {
      for (const menu of menus) {
        const currentPath = [...path, { title: menu.title, path: menu.path, icon: menu.icon }]
        if (menu.path === targetPath) {
          result.push(...currentPath)
          return true
        }
        if (menu.children && menu.children.length > 0) {
          if (findPath(menu.children, targetPath, currentPath)) {
            return true
          }
        }
      }
      return false
    }

    findPath(menuList.value, activeMenu.value, [])
    return result
  })

  const flattenMenus = computed(() => {
    const result: MenuItem[] = []
    const flatten = (menus: MenuItem[]) => {
      for (const menu of menus) {
        result.push(menu)
        if (menu.children && menu.children.length > 0) {
          flatten(menu.children)
        }
      }
    }
    flatten(menuList.value)
    return result
  })

  const clearMenu = () => {
    menuList.value = []
    activeMenu.value = ''
  }

  return {
    isCollapse,
    activeMenu,
    menuList,
    breadcrumbs,
    flattenMenus,
    toggleCollapse,
    setActiveMenu,
    setMenuList,
    clearMenu,
  }
})
