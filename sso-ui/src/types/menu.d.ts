import type { Component } from 'vue'
import type { RouteMeta } from 'vue-router'

export interface ApiPermission {
  id: string
  name: string
  identification: string
  parentId?: string | null
  identityLineage?: string
  comPath?: string
  path?: string
  iconStr?: string
  displayNo: number
  isFrame?: number
  type: number
  status?: number
  remark?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  children?: ApiPermission[]
}

export interface AppMenuMeta extends RouteMeta {
  title: string
  icon?: string
  hidden?: boolean
  keepAlive?: boolean
  permission?: string
  isExternal?: boolean
  requiresAuth?: boolean
  [key: string]: unknown
}

export interface AppMenu {
  id: string
  name: string
  path: string
  component?: () => Promise<unknown>
  redirect?: string
  meta: AppMenuMeta
  children?: AppMenu[]
}

export interface PermissionState {
  menuList: AppMenu[]
  dynamicRoutes: import('vue-router').RouteRecordRaw[]
  isLoaded: boolean
  permissions: string[]
}

export interface MenuItem {
  id: string
  name: string
  path: string
  icon?: string
  title: string
  badge?: string | number
  hidden?: boolean
  external?: boolean
  children?: MenuItem[]
  permission?: string
  keepAlive?: boolean
}

export interface MenuState {
  isCollapse: boolean
  activeMenu: string
  menuList: MenuItem[]
}

export interface LayoutContext {
  isCollapse: boolean
  toggleCollapse: () => void
  activeMenu: string
  setActiveMenu: (path: string) => void
}

export type SidebarTheme = 'light' | 'dark'

export interface BreadcrumbItem {
  title: string
  path?: string
  icon?: string
}

export type IconComponentMap = Record<string, Component>
