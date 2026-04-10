<template>
  <div class="page-shell">
    <AuthSearchSection
      title="操作日志检索"
      description="默认按最近 7 天、最新优先查询；超过普通分页窗口后会自动切换到深分页模式。"
      :model="queryForm"
    >
      <el-form-item label="应用编码">
        <el-select v-model="queryForm.appCode" clearable placeholder="全部应用">
          <el-option
            v-for="option in appOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="平台编码">
        <el-input v-model="queryForm.platformCode" placeholder="请输入平台编码" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="模块">
        <el-input v-model="queryForm.module" placeholder="请输入模块" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="功能">
        <el-input v-model="queryForm.feature" placeholder="请输入功能" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="用户标识">
        <el-input v-model="queryForm.userId" placeholder="请输入用户标识" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="账号">
        <el-input v-model="queryForm.username" placeholder="请输入账号" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="操作类型">
        <el-input
          v-model="queryForm.operationType"
          placeholder="如 CREATE / UPDATE"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="执行结果">
        <el-select v-model="queryForm.resultStatus" clearable placeholder="全部结果">
          <el-option
            v-for="option in LOG_RESULT_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="链路追踪">
        <el-input v-model="queryForm.traceId" placeholder="请输入链路追踪值" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="关键字">
        <el-input
          v-model="queryForm.keyword"
          placeholder="请求路径 / 关键内容"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="排序方式">
        <el-select v-model="queryForm.sortOrder" placeholder="请选择排序方式">
          <el-option
            v-for="option in LOG_SORT_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
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
      v-memo="[loading, pager.total, tableData, pager.pageNum, pager.pageSize, pager.deepPagingMode, deepPagingNotice]"
      class="panel panel--table"
    >
      <div class="panel-header">
        <div>
          <h2 class="panel-title">操作日志列表</h2>
          <p class="panel-subtitle">深分页模式下仅支持连续上一页、下一页浏览，链路追踪值支持一键复制。</p>
        </div>
      </div>

      <el-alert
        v-if="deepPagingNotice"
        type="warning"
        :closable="false"
        show-icon
        :title="deepPagingNotice"
        class="log-alert"
      />

      <el-alert
        v-if="loadError"
        type="error"
        :closable="false"
        show-icon
        :title="loadError"
        class="log-alert"
      />

      <el-table v-loading="loading" :data="tableData" row-key="logId" class="log-table">
        <el-table-column prop="operationName" label="操作名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="operationType" label="操作类型" min-width="120" show-overflow-tooltip />
        <el-table-column prop="module" label="模块" min-width="120" show-overflow-tooltip />
        <el-table-column prop="feature" label="功能" min-width="120" show-overflow-tooltip />
        <el-table-column prop="username" label="账号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="displayName" label="显示名称" min-width="120" show-overflow-tooltip />
        <el-table-column label="请求方式" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getOperationMethodTagType(row.requestMethod)">
              {{ row.requestMethod || '--' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestPath" label="请求路径" min-width="220" show-overflow-tooltip />
        <el-table-column label="执行结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLogResultMeta(row.resultStatus).type">
              {{ getLogResultMeta(row.resultStatus).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="100" align="right">
          <template #default="{ row }">{{ formatDuration(row.durationMs) }}</template>
        </el-table-column>
        <el-table-column prop="clientIp" label="客户端 IP" min-width="136" />
        <el-table-column label="链路追踪" min-width="170">
          <template #default="{ row }">
            <CopyableText :text="row.traceId" monospace />
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" min-width="168" />
        <el-table-column label="操作" width="88" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty :description="loadError ? '操作日志加载失败' : '暂无符合条件的操作日志'" />
        </template>
      </el-table>

      <div class="panel-footer">
        <template v-if="!pager.deepPagingMode">
          <el-pagination
            :current-page="pager.pageNum"
            :page-size="pager.pageSize"
            :page-sizes="[20, 50, 100]"
            :total="pager.total"
            background
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="handlePageChange"
            @size-change="handlePageSizeChange"
          />
        </template>

        <template v-else>
          <div class="deep-paging-bar">
            <span class="deep-paging-bar__summary">共 {{ pager.total }} 条，当前第 {{ pager.pageNum }} 页</span>
            <div class="deep-paging-bar__actions">
              <el-button :disabled="pager.pageNum <= 1" @click="goPrevPage">上一页</el-button>
              <el-button type="primary" @click="goNextPage">下一页</el-button>
            </div>
          </div>
        </template>
      </div>
    </section>

    <el-drawer v-model="detailVisible" title="操作日志详情" size="820px">
      <div v-loading="detailLoading" class="log-detail">
        <el-empty v-if="!detailLoading && !detailData" description="暂无操作日志详情" />

        <template v-else-if="detailData">
          <section class="log-detail__group">
            <h3 class="log-detail__title">基础信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="应用">{{ detailData.appName || detailData.appCode || '--' }}</el-descriptions-item>
              <el-descriptions-item label="平台">{{ detailData.platformName || detailData.platformCode || '--' }}</el-descriptions-item>
              <el-descriptions-item label="模块 / 功能">
                {{ `${detailData.module || '--'} / ${detailData.feature || '--'}` }}
              </el-descriptions-item>
              <el-descriptions-item label="操作类型">{{ detailData.operationType || '--' }}</el-descriptions-item>
              <el-descriptions-item label="操作名称">{{ detailData.operationName || '--' }}</el-descriptions-item>
              <el-descriptions-item label="操作描述" :span="2">{{ detailData.operationDesc || '--' }}</el-descriptions-item>
            </el-descriptions>
          </section>

          <section class="log-detail__group">
            <h3 class="log-detail__title">操作人信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="账号">{{ detailData.username || '--' }}</el-descriptions-item>
              <el-descriptions-item label="显示名称">{{ detailData.displayName || '--' }}</el-descriptions-item>
            </el-descriptions>
          </section>

          <section class="log-detail__group">
            <h3 class="log-detail__title">执行结果</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="结果状态">
                <el-tag :type="getLogResultMeta(detailData.resultStatus).type">
                  {{ getLogResultMeta(detailData.resultStatus).label }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="执行耗时">{{ formatDuration(detailData.durationMs) }}</el-descriptions-item>
              <el-descriptions-item label="错误码">{{ detailData.errorCode || '--' }}</el-descriptions-item>
              <el-descriptions-item label="异常类型">{{ detailData.exceptionType || '--' }}</el-descriptions-item>
              <el-descriptions-item label="错误摘要" :span="2">
                {{ detailData.errorMessageSummary || '--' }}
              </el-descriptions-item>
            </el-descriptions>
          </section>

          <section class="log-detail__group">
            <h3 class="log-detail__title">链路与时间</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="来源系统">{{ detailData.sourceSystem || '--' }}</el-descriptions-item>
              <el-descriptions-item label="客户端 IP">{{ detailData.clientIp || '--' }}</el-descriptions-item>
              <el-descriptions-item label="开始时间">{{ formatDateTime(detailData.startTime) }}</el-descriptions-item>
              <el-descriptions-item label="结束时间">{{ formatDateTime(detailData.endTime) }}</el-descriptions-item>
              <el-descriptions-item label="入库时间">{{ formatDateTime(detailData.ingestTime) }}</el-descriptions-item>
              <el-descriptions-item label="UserAgent">{{ detailData.userAgent || '--' }}</el-descriptions-item>
            </el-descriptions>
          </section>

          <section class="log-detail__text-grid">
            <LogTextBlock title="请求路径" :text="detailData.requestPath" />
            <LogTextBlock title="请求参数" :text="stringifyMaybeJson(detailData.requestQuery)" />
            <LogTextBlock title="请求体摘要" :text="stringifyMaybeJson(detailData.requestBodySummary)" />
            <LogTextBlock title="响应摘要" :text="stringifyMaybeJson(detailData.responseSummary)" />
            <LogTextBlock title="异常堆栈摘要" :text="stringifyMaybeJson(detailData.exceptionStackSummary)" />
            <LogTextBlock title="扩展字段" :text="stringifyMaybeJson(detailData.ext)" />
          </section>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAppPageApi } from '@/api/app'
import { getOperationLogDetailApi, getOperationLogPageApi } from '@/api/log'
import { DEFAULT_BATCH_PAGE_SIZE } from '@/constants/admin'
import { showGlobalError } from '@/stores/globalError'
import type { SelectOption } from '@/types/common'
import type { OperationLogPageState, OperationLogQueryDTO, OperationLogRecord } from '@/types/log'
import {
  LOG_DEFAULT_PAGE_SIZE,
  LOG_RESULT_OPTIONS,
  LOG_SORT_OPTIONS,
  buildOperationLogPayload,
  createOperationLogFingerprint,
  formatDateTime,
  formatDuration,
  getLogResultMeta,
  getOperationMethodTagType,
  getRelativeDateRange,
  isDeepPagingRequest,
  resetOperationLogPagingState,
  stringifyMaybeJson,
} from '@/utils/log'
import AuthSearchSection from '@/views/system/auth/components/AuthSearchSection.vue'
import CopyableText from './CopyableText.vue'
import LogTextBlock from './LogTextBlock.vue'

const loading = ref(false)
const loadError = ref('')
const deepPagingNotice = ref('')
const tableData = ref<OperationLogRecord[]>([])
const timeRange = ref<string[]>(getRelativeDateRange())
const appOptions = ref<SelectOption<string>[]>([])

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<OperationLogRecord | null>(null)

const queryForm = reactive<OperationLogQueryDTO>({
  pageNum: 1,
  pageSize: LOG_DEFAULT_PAGE_SIZE,
  appCode: '',
  platformCode: '',
  module: '',
  feature: '',
  userId: '',
  username: '',
  operationType: '',
  resultStatus: undefined,
  traceId: '',
  keyword: '',
  sortOrder: 'desc',
})

const pager = reactive<OperationLogPageState>({
  pageNum: 1,
  pageSize: LOG_DEFAULT_PAGE_SIZE,
  total: 0,
  sortOrder: 'desc',
  deepPagingMode: false,
  nextTokenByPage: {},
  pageCache: {},
  queryFingerprint: '',
})

const syncPagerStateFromQuery = () => {
  pager.pageNum = queryForm.pageNum
  pager.pageSize = queryForm.pageSize
  pager.sortOrder = queryForm.sortOrder || 'desc'
}

const buildRequestPayload = (pageNum = queryForm.pageNum, searchAfterToken?: string) => {
  return buildOperationLogPayload(
    {
      ...queryForm,
      pageNum,
      pageSize: pager.pageSize,
      sortOrder: queryForm.sortOrder || 'desc',
      searchAfterToken,
    },
    timeRange.value,
  )
}

const loadAppOptions = async () => {
  try {
    const page = await getAppPageApi({
      pageNum: 1,
      pageSize: DEFAULT_BATCH_PAGE_SIZE,
      searchKey: '',
      appCode: '',
      appName: '',
      status: 1,
      userType: 1,
    })

    appOptions.value = (page.records || []).map((item) => ({
      label: `${item.appName} (${item.appCode})`,
      value: item.appCode,
    }))
  } catch {
    appOptions.value = []
  }
}

const loadLogs = async (targetPage = queryForm.pageNum, searchAfterToken?: string) => {
  loading.value = true
  loadError.value = ''
  deepPagingNotice.value = ''

  const payload = buildRequestPayload(targetPage, searchAfterToken)
  const queryFingerprint = createOperationLogFingerprint(payload)
  const currentPageSize = payload.pageSize || LOG_DEFAULT_PAGE_SIZE

  // 查询条件变化时必须清空 token 和缓存，避免深分页状态串页。
  if (pager.queryFingerprint && pager.queryFingerprint !== queryFingerprint) {
    resetOperationLogPagingState(pager, currentPageSize, payload.sortOrder || 'desc')
    targetPage = 1
    payload.pageNum = 1
    payload.searchAfterToken = undefined
  }

  try {
    const page = await getOperationLogPageApi(payload)
    const records = Array.isArray(page.records) ? page.records : []

    tableData.value = records
    pager.pageNum = Number(page.current || targetPage)
    pager.pageSize = Number(page.size || currentPageSize)
    pager.total = Number(page.total || 0)
    pager.queryFingerprint = queryFingerprint
    pager.pageCache[pager.pageNum] = records
    pager.nextTokenByPage[pager.pageNum] = page.nextSearchAfterToken
    pager.deepPagingMode = pager.deepPagingMode || isDeepPagingRequest(pager.pageNum, pager.pageSize)

    queryForm.pageNum = pager.pageNum
    queryForm.pageSize = pager.pageSize

    if (pager.deepPagingMode) {
      deepPagingNotice.value = '当前已进入深分页模式，仅支持连续上一页、下一页翻页。'
    }
  } catch (error) {
    tableData.value = []

    const isDeepPagingLimitError =
      (typeof error === 'object' &&
        error !== null &&
        'response' in error &&
        Number((error as { response?: { status?: number } }).response?.status) === 400) ||
      (typeof error === 'object' &&
        error !== null &&
        'message' in error &&
        String((error as { message?: string }).message).includes('400'))

    const message = isDeepPagingLimitError
      ? '当前查询已超过普通分页窗口，请使用连续翻页方式浏览。'
      : '操作日志加载失败，请稍后重试'

    loadError.value = message
    showGlobalError(error, { fallbackMessage: message })
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  syncPagerStateFromQuery()
  resetOperationLogPagingState(pager, queryForm.pageSize, queryForm.sortOrder || 'desc')
  loadLogs(1)
}

const resetQuery = () => {
  queryForm.pageNum = 1
  queryForm.pageSize = LOG_DEFAULT_PAGE_SIZE
  queryForm.appCode = ''
  queryForm.platformCode = ''
  queryForm.module = ''
  queryForm.feature = ''
  queryForm.userId = ''
  queryForm.username = ''
  queryForm.operationType = ''
  queryForm.resultStatus = undefined
  queryForm.traceId = ''
  queryForm.keyword = ''
  queryForm.sortOrder = 'desc'
  timeRange.value = getRelativeDateRange()
  syncPagerStateFromQuery()
  resetOperationLogPagingState(pager, queryForm.pageSize, queryForm.sortOrder)
  loadLogs(1)
}

const handlePageChange = (pageNum: number) => {
  queryForm.pageNum = pageNum
  syncPagerStateFromQuery()
  loadLogs(pageNum)
}

const handlePageSizeChange = (pageSize: number) => {
  queryForm.pageSize = pageSize
  queryForm.pageNum = 1
  syncPagerStateFromQuery()
  resetOperationLogPagingState(pager, pageSize, queryForm.sortOrder || 'desc')
  loadLogs(1)
}

const goNextPage = async () => {
  const targetPage = pager.pageNum + 1

  if (!pager.deepPagingMode && !isDeepPagingRequest(targetPage, pager.pageSize)) {
    queryForm.pageNum = targetPage
    await loadLogs(targetPage)
    return
  }

  pager.deepPagingMode = true
  const nextToken = pager.nextTokenByPage[pager.pageNum]
  if (!nextToken) {
    ElMessage.warning('没有更多数据或缺少下一页游标')
    return
  }

  queryForm.pageNum = targetPage
  await loadLogs(targetPage, nextToken)
}

const goPrevPage = () => {
  const targetPage = pager.pageNum - 1
  if (targetPage < 1) return

  if (!pager.deepPagingMode) {
    queryForm.pageNum = targetPage
    void loadLogs(targetPage)
    return
  }

  const cached = pager.pageCache[targetPage]
  if (!cached) {
    ElMessage.warning('深分页上一页缓存不存在，请重新查询')
    return
  }

  pager.pageNum = targetPage
  queryForm.pageNum = targetPage
  tableData.value = cached
}

const openDetail = async (row: OperationLogRecord) => {
  detailVisible.value = true
  detailLoading.value = true

  try {
    const [startTime, endTime] = timeRange.value
    detailData.value = await getOperationLogDetailApi(row.logId, {
      startTime,
      endTime,
    })
  } catch (error) {
    detailVisible.value = false
    ElMessage.warning('操作日志详情加载失败')
    showGlobalError(error, { fallbackMessage: '操作日志详情加载失败' })
  } finally {
    detailLoading.value = false
  }
}

onMounted(async () => {
  syncPagerStateFromQuery()
  resetOperationLogPagingState(pager, queryForm.pageSize, queryForm.sortOrder || 'desc')
  await Promise.all([loadAppOptions(), loadLogs(1)])
})
</script>

<style scoped>
.log-alert {
  margin-bottom: 12px;
}

.deep-paging-bar {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px dashed var(--app-border-strong);
  border-radius: 16px;
  background: var(--app-surface-muted);
}

.deep-paging-bar__summary {
  color: var(--app-muted-strong);
  font-size: 12px;
}

.deep-paging-bar__actions {
  display: flex;
  gap: 10px;
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

.log-detail__text-grid {
  display: grid;
  gap: 12px;
}

@media (max-width: 768px) {
  .deep-paging-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .deep-paging-bar__actions {
    justify-content: flex-start;
  }
}
</style>
