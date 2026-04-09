<template>
  <div class="sidebar" :class="{ 'is-collapse': isCollapse, 'is-mobile-visible': mobileVisible }">
    <div class="sidebar__brand">
      <div class="sidebar__brand-mark">台</div>
      <transition name="sidebar-fade">
        <div v-show="!isCollapse" class="sidebar__brand-copy">
          <strong>后台管理系统</strong>
        </div>
      </transition>
    </div>

    <el-scrollbar class="sidebar__scrollbar">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        :unique-opened="true"
        router
        class="sidebar-menu"
        @select="handleSelect"
      >
        <SidebarItem v-for="menu in menuList" :key="menu.id" :item="menu" />
      </el-menu>
    </el-scrollbar>

    <div class="sidebar__footer">
      <button class="sidebar__collapse" type="button" @click="toggleCollapse">
        <el-icon :size="18">
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </el-icon>
        <span v-show="!isCollapse">{{ isCollapse ? '展开' : '收起导航' }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, provide } from 'vue'
import { useRoute } from 'vue-router'
import { useMenuStore } from '@/stores/menu'
import { Expand, Fold } from '@element-plus/icons-vue'
import SidebarItem from './SidebarItem.vue'

const props = withDefaults(
  defineProps<{
    mobileVisible?: boolean
  }>(),
  {
    mobileVisible: false,
  },
)

const emit = defineEmits<{
  (event: 'close-mobile'): void
}>()

const route = useRoute()
const menuStore = useMenuStore()

const isCollapse = computed(() => menuStore.isCollapse)
const activeMenu = computed(() => menuStore.activeMenu || route.path)
const menuList = computed(() => menuStore.menuList)

provide('isCollapse', isCollapse)

const toggleCollapse = () => {
  menuStore.toggleCollapse()
}

const handleSelect = () => {
  if (props.mobileVisible) {
    emit('close-mobile')
  }
}
</script>

<style scoped>
.sidebar {
  display: flex;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--app-border);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: var(--app-shadow-soft);
  transition:
    width 0.3s ease,
    transform 0.3s ease,
    opacity 0.3s ease;
  overflow: hidden;
}

.sidebar.is-collapse {
  padding-left: 12px;
  padding-right: 12px;
}

.sidebar__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 54px;
  padding: 9px 10px;
  border: 1px solid var(--app-border);
  border-radius: 16px;
  background: var(--app-surface-muted);
}

.sidebar__brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  border: 1px solid rgba(26, 115, 232, 0.14);
  background: rgba(26, 115, 232, 0.08);
  color: var(--app-accent-strong);
  font-size: 18px;
  font-weight: 600;
  letter-spacing: -0.04em;
}

.sidebar__brand-copy {
  display: flex;
  align-items: center;
  min-width: 0;
}

.sidebar__brand-copy strong {
  color: var(--sidebar-text);
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
}

.sidebar__scrollbar {
  flex: 1;
  min-height: 0;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: var(--sidebar-text);
  --el-menu-active-color: var(--app-accent-strong);
  --el-menu-hover-bg-color: #f1f5fb;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 100%;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 48px;
  margin-bottom: 4px;
  border-radius: 999px;
  color: var(--sidebar-text);
  line-height: 48px;
}

:deep(.el-sub-menu__title),
:deep(.el-menu-item) {
  padding-left: 16px !important;
}

:deep(.el-menu-item .el-icon),
:deep(.el-sub-menu__title .el-icon) {
  margin-right: 12px;
  color: var(--sidebar-muted);
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: #f1f5fb !important;
}

:deep(.el-menu-item.is-active) {
  background: #e8f0fe !important;
  color: var(--app-accent-strong) !important;
  font-weight: 600;
}

:deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  background: rgba(232, 240, 254, 0.72) !important;
  color: var(--app-accent-strong) !important;
  font-weight: 600;
}

:deep(.el-menu-item.is-active .el-icon),
:deep(.el-sub-menu.is-active > .el-sub-menu__title .el-icon) {
  color: var(--app-accent-strong) !important;
}

:deep(.el-menu--inline) {
  padding-top: 2px;
  padding-bottom: 4px;
}

:deep(.el-sub-menu .el-menu) {
  background: transparent;
}

.sidebar__footer {
  padding-top: 4px;
}

.sidebar__collapse {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  min-height: 44px;
  border: 1px solid var(--app-border);
  border-radius: 999px;
  background: var(--app-surface);
  color: var(--sidebar-text);
  cursor: pointer;
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease;
}

.sidebar__collapse:hover {
  background: #f1f5fb;
  border-color: #c6d4e5;
}

.sidebar-fade-enter-active,
.sidebar-fade-leave-active {
  transition: opacity 0.2s ease;
}

.sidebar-fade-enter-from,
.sidebar-fade-leave-to {
  opacity: 0;
}

@media (max-width: 1024px) {
  .sidebar {
    position: fixed;
    top: 14px;
    bottom: 14px;
    left: 14px;
    width: min(320px, calc(100vw - 28px));
    z-index: 35;
    transform: translateX(-120%);
    opacity: 0;
    visibility: hidden;
  }

  .sidebar.is-mobile-visible {
    transform: translateX(0);
    opacity: 1;
    visibility: visible;
  }
}

@media (max-width: 768px) {
  .sidebar {
    top: 12px;
    bottom: 12px;
    left: 12px;
    width: min(320px, calc(100vw - 24px));
    border-radius: 20px;
  }
}
</style>
