<template>
  <div class="page-shell">
    <section class="panel">
      <el-tabs v-if="availableTabs.length" v-model="activeTab" class="log-tabs" @tab-change="handleTabChange">
        <el-tab-pane v-if="canViewLoginLogs" label="登录日志" name="login">
          <LoginLogPane v-if="activeTab === 'login'" />
        </el-tab-pane>
        <el-tab-pane v-if="canViewOperationLogs" label="操作日志" name="operation">
          <OperationLogPane v-if="activeTab === 'operation'" />
        </el-tab-pane>
      </el-tabs>
      <el-empty v-else description="当前账号暂无日志审计查看权限" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePermissionStore } from '@/stores/permission'
import LoginLogPane from './components/LoginLogPane.vue'
import OperationLogPane from './components/OperationLogPane.vue'

type LogTabKey = 'login' | 'operation'

const route = useRoute()
const router = useRouter()
const permissionStore = usePermissionStore()

const canViewLoginLogs = computed(() => permissionStore.hasPermission('login-log:list'))
const canViewOperationLogs = computed(() => permissionStore.hasPermission('operation-log:list'))
const availableTabs = computed(() => {
  const tabs: LogTabKey[] = []
  if (canViewLoginLogs.value) {
    tabs.push('login')
  }
  if (canViewOperationLogs.value) {
    tabs.push('operation')
  }
  return tabs
})

const resolveTabFromQuery = (): LogTabKey => {
  const requestedTab: LogTabKey = route.query.tab === 'operation' ? 'operation' : 'login'
  if (requestedTab === 'login' && canViewLoginLogs.value) {
    return 'login'
  }
  if (requestedTab === 'operation' && canViewOperationLogs.value) {
    return 'operation'
  }
  if (canViewLoginLogs.value) {
    return 'login'
  }
  return 'operation'
}

const activeTab = ref<LogTabKey>(resolveTabFromQuery())

const handleTabChange = (value: string | number) => {
  const nextTab = value === 'operation' ? 'operation' : 'login'
  router.replace({
    path: route.path,
    query: {
      ...route.query,
      tab: nextTab,
    },
  })
}

watch(
  [() => route.query.tab, canViewLoginLogs, canViewOperationLogs],
  () => {
    activeTab.value = resolveTabFromQuery()
  },
)
</script>

<style scoped>
.log-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.log-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: var(--app-border);
}

.log-tabs :deep(.el-tabs__item) {
  height: 42px;
  padding: 0 18px;
  color: var(--app-muted);
  font-weight: 600;
}

.log-tabs :deep(.el-tabs__item.is-active) {
  color: var(--app-accent-strong);
}
</style>
