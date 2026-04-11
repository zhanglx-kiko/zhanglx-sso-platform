<template>
  <div class="page-shell">
    <AuthSearchSection :model="queryForm">
      <el-form-item label="用户标识">
        <el-input v-model="queryForm.userId" placeholder="请输入用户标识" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="账号">
        <el-input v-model="queryForm.username" placeholder="请输入账号" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="事件类型">
        <el-select v-model="queryForm.eventType" clearable placeholder="全部事件">
          <el-option
            v-for="option in LOGIN_EVENT_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="登录结果">
        <el-select v-model="queryForm.loginResult" clearable placeholder="全部结果">
          <el-option
            v-for="option in LOG_RESULT_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="登录 IP">
        <el-input v-model="queryForm.loginIp" placeholder="请输入登录 IP" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="时间范围" class="filters-form__item--wide">
        <el-date-picker
          v-model="timeRange"
          type="datetimerange"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          range-separator="至"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <template #actions>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </template>
    </AuthSearchSection>

    <section
      v-memo="[loading, total, tableData, queryForm.pageNum, queryForm.pageSize]"
      class="panel panel--table"
    >
      <div class="panel-header">
        <h2 class="panel-title">登录日志列表</h2>
      </div>

      <el-alert
        v-if="loadError"
        type="error"
        :closable="false"
        show-icon
        :title="loadError"
        class="log-alert"
      />

      <el-table v-loading="loading" :data="tableData" row-key="id" class="log-table">
        <el-table-column prop="username" label="账号" min-width="140" show-overflow-tooltip />
        <el-table-column prop="displayName" label="显示名称" min-width="140" show-overflow-tooltip />
        <el-table-column label="事件类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getLoginEventMeta(row.eventType).type">
              {{ getLoginEventMeta(row.eventType).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLogResultMeta(row.loginResult).type">
              {{ getLogResultMeta(row.loginResult).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="loginIp" label="登录 IP" min-width="136" />
        <el-table-column prop="deviceType" label="设备类型" min-width="120" show-overflow-tooltip />
        <el-table-column prop="appCode" label="应用编码" min-width="110" show-overflow-tooltip />
        <el-table-column prop="loginTime" label="登录时间" min-width="168" />
        <el-table-column prop="logoutTime" label="登出时间" min-width="168" />
        <el-table-column label="链路追踪" min-width="170">
          <template #default="{ row }">
            <CopyableText :text="row.traceId" monospace />
          </template>
        </el-table-column>
        <el-table-column label="请求追踪" min-width="170">
          <template #default="{ row }">
            <CopyableText :text="row.requestId" monospace />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="88" fixed="right">
          <template #default="{ row }">
            <el-button v-permission="'login-log:view'" link type="primary" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty :description="loadError ? '登录日志加载失败' : '暂无符合条件的登录日志'" />
        </template>
      </el-table>

      <div class="panel-footer">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[20, 50, 100]"
          :total="total"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadLogs"
          @current-change="loadLogs"
        />
      </div>
    </section>

    <el-drawer v-model="detailVisible" title="登录日志详情" size="720px">
      <div v-loading="detailLoading" class="log-detail">
        <el-empty v-if="!detailLoading && !detailData" description="暂无登录日志详情" />

        <template v-else-if="detailData">
          <section class="log-detail__group">
            <h3 class="log-detail__title">基础信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="账号">{{ detailData.username || '--' }}</el-descriptions-item>
              <el-descriptions-item label="显示名称">{{ detailData.displayName || '--' }}</el-descriptions-item>
              <el-descriptions-item label="事件类型">
                <el-tag :type="getLoginEventMeta(detailData.eventType).type">
                  {{ getLoginEventMeta(detailData.eventType).label }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="登录结果">
                <el-tag :type="getLogResultMeta(detailData.loginResult).type">
                  {{ getLogResultMeta(detailData.loginResult).label }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="应用编码">{{ detailData.appCode || '--' }}</el-descriptions-item>
              <el-descriptions-item label="失败原因">{{ detailData.failReason || '--' }}</el-descriptions-item>
            </el-descriptions>
          </section>

          <section class="log-detail__group">
            <h3 class="log-detail__title">网络与链路</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="登录 IP">{{ detailData.loginIp || '--' }}</el-descriptions-item>
              <el-descriptions-item label="设备类型">{{ detailData.deviceType || '--' }}</el-descriptions-item>
              <el-descriptions-item label="客户端">{{ detailData.clientType || '--' }}</el-descriptions-item>
              <el-descriptions-item label="UserAgent">{{ detailData.userAgent || '--' }}</el-descriptions-item>
            </el-descriptions>
          </section>

          <section class="log-detail__group">
            <h3 class="log-detail__title">时间信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="登录时间">{{ formatDateTime(detailData.loginTime) }}</el-descriptions-item>
              <el-descriptions-item label="登出时间">{{ formatDateTime(detailData.logoutTime) }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ formatDateTime(detailData.createTime) }}</el-descriptions-item>
            </el-descriptions>
          </section>

          <LogTextBlock title="扩展信息" :text="stringifyMaybeJson(detailData.extJson)" />
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getLoginLogDetailApi, getLoginLogPageApi } from '@/api/log'
import { showGlobalError } from '@/stores/globalError'
import type { LoginLogQueryDTO, LoginLogRecord } from '@/types/log'
import {
  LOG_DEFAULT_PAGE_SIZE,
  LOGIN_EVENT_OPTIONS,
  LOG_RESULT_OPTIONS,
  buildLoginLogPayload,
  formatDateTime,
  getLoginEventMeta,
  getLogResultMeta,
  getRelativeDateRange,
  stringifyMaybeJson,
} from '@/utils/log'
import { toPageResult } from '@/utils/admin'
import AuthSearchSection from '@/views/system/auth/components/AuthSearchSection.vue'
import CopyableText from './CopyableText.vue'
import LogTextBlock from './LogTextBlock.vue'

const loading = ref(false)
const loadError = ref('')
const total = ref(0)
const tableData = ref<LoginLogRecord[]>([])
const timeRange = ref<string[]>(getRelativeDateRange())

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<LoginLogRecord | null>(null)

const queryForm = reactive<LoginLogQueryDTO>({
  pageNum: 1,
  pageSize: LOG_DEFAULT_PAGE_SIZE,
  userId: '',
  username: '',
  eventType: undefined,
  loginResult: undefined,
  loginIp: '',
})

const loadLogs = async () => {
  loading.value = true
  loadError.value = ''

  try {
    const page = toPageResult(await getLoginLogPageApi(buildLoginLogPayload(queryForm, timeRange.value)))
    tableData.value = page.records
    total.value = page.total
  } catch (error) {
    tableData.value = []
    total.value = 0
    loadError.value = '登录日志加载失败，请稍后重试'
    showGlobalError(error, { fallbackMessage: loadError.value })
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  loadLogs()
}

const resetQuery = () => {
  queryForm.pageNum = 1
  queryForm.pageSize = LOG_DEFAULT_PAGE_SIZE
  queryForm.userId = ''
  queryForm.username = ''
  queryForm.eventType = undefined
  queryForm.loginResult = undefined
  queryForm.loginIp = ''
  timeRange.value = getRelativeDateRange()
  loadLogs()
}

const openDetail = async (row: LoginLogRecord) => {
  detailVisible.value = true
  detailLoading.value = true

  try {
    detailData.value = await getLoginLogDetailApi(row.id)
  } catch (error) {
    detailVisible.value = false
    ElMessage.warning('登录日志详情加载失败')
    showGlobalError(error, { fallbackMessage: '登录日志详情加载失败' })
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.log-alert {
  margin-bottom: 12px;
}

.log-detail {
  display: flex;
  min-height: 240px;
  flex-direction: column;
  gap: 16px;
}

.log-detail__group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.log-detail__title {
  margin: 0;
  color: var(--app-title);
  font-size: 14px;
  font-weight: 600;
}
</style>
