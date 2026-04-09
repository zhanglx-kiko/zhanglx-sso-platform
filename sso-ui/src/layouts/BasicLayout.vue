<template>
  <div class="layout-shell" :style="{ '--sidebar-width': sidebarWidth }">
    <div v-if="mobileMenuVisible" class="layout-shell__overlay" @click="closeMobileMenu" />
    <aside class="layout-shell__sidebar">
      <AppSidebar :mobile-visible="mobileMenuVisible" @close-mobile="closeMobileMenu" />
    </aside>

    <div class="layout-shell__main">
      <AppHeader @toggle-menu="toggleMobileMenu" />

      <main class="layout-shell__content">
        <router-view v-slot="{ Component }">
          <transition name="content-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useMenuStore } from '@/stores/menu'
import AppHeader from './components/AppHeader.vue'
import AppSidebar from './components/AppSidebar.vue'

const route = useRoute()
const menuStore = useMenuStore()

const mobileMenuVisible = ref(false)

const sidebarWidth = computed(() => (menuStore.isCollapse ? '92px' : '280px'))

const toggleMobileMenu = () => {
  mobileMenuVisible.value = !mobileMenuVisible.value
}

const closeMobileMenu = () => {
  mobileMenuVisible.value = false
}

watch(
  () => route.path,
  (path) => {
    menuStore.setActiveMenu(path)
    closeMobileMenu()
  },
  { immediate: true },
)
</script>

<style scoped>
.layout-shell {
  display: grid;
  grid-template-columns: var(--sidebar-width) minmax(0, 1fr);
  gap: 12px;
  min-height: 100vh;
  padding: 12px;
}

.layout-shell__sidebar {
  position: sticky;
  top: 12px;
  height: calc(100vh - 24px);
}

.layout-shell__main {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 12px;
}

.layout-shell__content {
  min-width: 0;
  padding-bottom: 8px;
}

.layout-shell__overlay {
  position: fixed;
  inset: 0;
  z-index: 30;
  background: rgba(60, 64, 67, 0.18);
  backdrop-filter: blur(3px);
}

.content-fade-enter-active,
.content-fade-leave-active {
  transition:
    opacity 0.28s ease,
    transform 0.28s ease;
}

.content-fade-enter-from,
.content-fade-leave-to {
  opacity: 0;
  transform: translateY(14px);
}

@media (max-width: 1024px) {
  .layout-shell {
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 12px;
  }

  .layout-shell__sidebar {
    position: static;
    height: auto;
  }
}

@media (max-width: 768px) {
  .layout-shell {
    padding: 10px;
  }

  .layout-shell__main {
    gap: 10px;
  }
}
</style>
