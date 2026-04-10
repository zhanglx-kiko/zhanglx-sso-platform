<template>
  <div class="page-shell">
    <section class="panel">
      <el-tabs v-model="activeTab" class="log-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="登录日志" name="login">
          <LoginLogPane v-if="activeTab === 'login'" />
        </el-tab-pane>
        <el-tab-pane label="操作日志" name="operation">
          <OperationLogPane v-if="activeTab === 'operation'" />
        </el-tab-pane>
      </el-tabs>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import LoginLogPane from './components/LoginLogPane.vue'
import OperationLogPane from './components/OperationLogPane.vue'

type LogTabKey = 'login' | 'operation'

const route = useRoute()
const router = useRouter()

const resolveTabFromQuery = (): LogTabKey => {
  return route.query.tab === 'operation' ? 'operation' : 'login'
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
  () => route.query.tab,
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
