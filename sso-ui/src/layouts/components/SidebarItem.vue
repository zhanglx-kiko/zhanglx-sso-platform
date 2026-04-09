<template>
  <template v-if="!item.hidden">
    <template v-if="hasChildren">
      <el-sub-menu
        :index="resolvePath(item.path)"
        :class="['menu-node', `menu-node--level-${level}`]"
        :popper-class="'sidebar-popper'"
      >
        <template #title>
          <el-icon v-if="item.icon">
            <component :is="getIcon(item.icon)" />
          </el-icon>
          <span class="menu-node__title">{{ item.title }}</span>
          <el-badge v-if="item.badge" :value="item.badge" class="menu-badge" :max="99" />
        </template>
        <SidebarItem
          v-for="child in visibleChildren"
          :key="child.id"
          :item="child"
          :base-path="resolvePath(item.path)"
          :level="level + 1"
        />
      </el-sub-menu>
    </template>

    <template v-else>
      <el-tooltip v-if="isCollapse" :content="item.title" placement="right" :show-after="300">
        <component
          :is="externalLink ? 'a' : 'router-link'"
          :to="!externalLink ? resolvePath(item.path) : undefined"
          :href="externalLink ? resolvePath(item.path) : undefined"
          :target="externalLink ? '_blank' : undefined"
          class="menu-link"
        >
          <el-menu-item
            :index="resolvePath(item.path)"
            :class="['menu-node', 'menu-node--leaf', `menu-node--level-${level}`]"
          >
            <el-icon v-if="item.icon">
              <component :is="getIcon(item.icon)" />
            </el-icon>
            <template #title>
              <span class="menu-node__title">{{ item.title }}</span>
              <el-badge v-if="item.badge" :value="item.badge" class="menu-badge" :max="99" />
            </template>
          </el-menu-item>
        </component>
      </el-tooltip>

      <component
        v-else
        :is="externalLink ? 'a' : 'router-link'"
        :to="!externalLink ? resolvePath(item.path) : undefined"
        :href="externalLink ? resolvePath(item.path) : undefined"
        :target="externalLink ? '_blank' : undefined"
        class="menu-link"
      >
        <el-menu-item
          :index="resolvePath(item.path)"
          :class="['menu-node', 'menu-node--leaf', `menu-node--level-${level}`]"
        >
          <el-icon v-if="item.icon">
            <component :is="getIcon(item.icon)" />
          </el-icon>
          <template #title>
            <span class="menu-node__title">{{ item.title }}</span>
            <el-badge v-if="item.badge" :value="item.badge" class="menu-badge" :max="99" />
          </template>
        </el-menu-item>
      </component>
    </template>
  </template>
</template>

<script setup lang="ts">
import { computed, inject, unref, type Component } from 'vue'
import type { MenuItem } from '@/types/menu'
import {
  House,
  User,
  UserFilled,
  Setting,
  Document,
  DataAnalysis,
  Message,
  Bell,
  Calendar,
  Folder,
  Grid,
  List,
  Monitor,
  PieChart,
  ShoppingCart,
  Tickets,
  Timer,
  Trophy,
  Van,
  Wallet,
  WarnTriangleFilled,
  ChatDotRound,
  Cpu,
  Connection,
  Link as LinkIcon,
  Lock,
  Shop,
  Present,
} from '@element-plus/icons-vue'

interface Props {
  item: MenuItem
  basePath?: string
  level?: number
}

const props = withDefaults(defineProps<Props>(), {
  basePath: '',
  level: 0,
})

const injectedCollapse = inject('isCollapse', false)
const isCollapse = computed(() => Boolean(unref(injectedCollapse)))

const iconMap: Record<string, Component> = {
  House,
  User,
  UserFilled,
  Setting,
  Document,
  DataAnalysis,
  Message,
  Bell,
  Calendar,
  Folder,
  ChartPie: PieChart,
  Grid,
  List,
  Monitor,
  PieChart,
  ShoppingCart,
  Tickets,
  Timer,
  Trophy,
  Van,
  Wallet,
  WarnTriangleFilled,
  ChatDotRound,
  Cpu,
  Connection,
  Link: LinkIcon,
  Lock,
  Shop,
  Present,
  lock: Lock,
  setting: Setting,
  shop: Shop,
  'shopping-cart': ShoppingCart,
  present: Present,
  document: Document,
}

const getIcon = (iconName: string): Component => {
  return iconMap[iconName] || Document
}

const resolvePath = (routePath: string): string => {
  if (isExternal(routePath)) {
    return routePath
  }
  if (routePath.startsWith('/')) {
    return routePath
  }
  if (isExternal(props.basePath)) {
    return props.basePath
  }
  const base = props.basePath.endsWith('/') ? props.basePath.slice(0, -1) : props.basePath
  const path = routePath.startsWith('/') ? routePath : `/${routePath}`
  return base ? `${base}${path}` : path
}

const isExternal = (path: string): boolean => {
  return /^(https?:|mailto:|tel:)/.test(path)
}

const externalLink = computed(() => {
  return props.item.external || isExternal(props.item.path)
})

const visibleChildren = computed(() => {
  if (!props.item.children) return []
  return props.item.children.filter((child) => !child.hidden)
})

const hasChildren = computed(() => {
  return visibleChildren.value.length > 0
})
</script>

<style scoped>
.menu-link {
  display: block;
  text-decoration: none;
  color: inherit;
}

.menu-node__title {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.menu-badge {
  margin-left: 8px;
}

:deep(.menu-node),
:deep(.menu-node > .el-sub-menu__title) {
  position: relative;
}

:deep(.menu-node--level-0.el-menu-item),
:deep(.menu-node--level-0 > .el-sub-menu__title) {
  font-size: 14px;
  font-weight: 600;
}

:deep(.menu-node--level-1.el-menu-item),
:deep(.menu-node--level-1 > .el-sub-menu__title) {
  margin-left: 10px;
  padding-left: 24px !important;
  font-size: 13px;
  font-weight: 600;
}

:deep(.menu-node--level-2.el-menu-item),
:deep(.menu-node--level-2 > .el-sub-menu__title) {
  margin-left: 22px;
  padding-left: 30px !important;
  font-size: 13px;
}

:deep(.menu-node--level-3.el-menu-item),
:deep(.menu-node--level-3 > .el-sub-menu__title),
:deep(.menu-node--level-4.el-menu-item),
:deep(.menu-node--level-4 > .el-sub-menu__title) {
  margin-left: 34px;
  padding-left: 28px !important;
  font-size: 12px;
}

:deep(.menu-node--level-1.el-menu-item::before),
:deep(.menu-node--level-1 > .el-sub-menu__title::before) {
  content: '';
  position: absolute;
  top: 12px;
  bottom: 12px;
  left: 12px;
  width: 2px;
  border-radius: 999px;
  background: rgba(26, 115, 232, 0.12);
}

:deep(.menu-node--level-2.el-menu-item::before),
:deep(.menu-node--level-2 > .el-sub-menu__title::before) {
  content: '';
  position: absolute;
  top: 11px;
  bottom: 11px;
  left: 10px;
  width: 2px;
  border-radius: 999px;
  background: rgba(95, 99, 104, 0.12);
}

:deep(.menu-node--level-3.el-menu-item::before),
:deep(.menu-node--level-3 > .el-sub-menu__title::before),
:deep(.menu-node--level-4.el-menu-item::before),
:deep(.menu-node--level-4 > .el-sub-menu__title::before) {
  content: '';
  position: absolute;
  top: 11px;
  bottom: 11px;
  left: 9px;
  width: 2px;
  border-radius: 999px;
  background: rgba(95, 99, 104, 0.18);
}

:deep(.menu-node--level-1 > .el-sub-menu__title .el-sub-menu__icon-arrow),
:deep(.menu-node--level-2 > .el-sub-menu__title .el-sub-menu__icon-arrow),
:deep(.menu-node--level-3 > .el-sub-menu__title .el-sub-menu__icon-arrow) {
  right: 14px;
  color: #8aa4c3;
}

:deep(.menu-node--level-2.el-menu-item.is-active) {
  background: rgba(232, 240, 254, 0.82) !important;
}

:deep(.el-badge__content) {
  transform: scale(0.86);
  border: none;
  box-shadow: none;
}
</style>
