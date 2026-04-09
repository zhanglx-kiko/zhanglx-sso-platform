<template>
  <div class="page-shell">
    <AppPageHeader
      title="日志审计"
      description="统一查看登录日志与操作日志，支持 traceId 复制、详情抽屉和操作日志深分页浏览。"
      :stats="headerStats"
    />

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
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppPageHeader from '@/components/AppPageHeader.vue'
import LoginLogPane from './components/LoginLogPane.vue'
import OperationLogPane from './components/OperationLogPane.vue'

type LogTabKey = 'login' | 'operation'

const route = useRoute()
const router = useRouter()

const resolveTabFromQuery = (): LogTabKey => {
  return route.query.tab === 'operation' ? 'operation' : 'login'
}

const activeTab = ref<LogTabKey>(resolveTabFromQuery())

const headerStats = computed(() => [
  { label: '默认时间范围', value: '最近 7 天', hint: '两类日志默认都按最近 7 天查询' },
  { label: '操作日志分页', value: '浅分页 + 深分页', hint: '超过窗口后自动切换为连续翻页' },
  { label: '链路辅助', value: 'traceId / requestId', hint: '列表与详情都支持一键复制' },
])

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
