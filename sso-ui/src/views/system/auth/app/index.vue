<template>
  <div class="page-shell">
    <AppPageHeader
      :title="pageTitle"
      description="统一维护系统应用与会员应用，为角色归属和用户应用授权提供候选数据源。"
      :stats="headerStats"
    >
      <template #actions>
        <el-button plain @click="loadApps">刷新列表</el-button>
        <el-button :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
        <el-button type="primary" @click="openCreateDialog">新增应用</el-button>
      </template>
    </AppPageHeader>

    <AuthSearchSection title="筛选条件" description="支持按应用编码、应用名称、状态和用户类型筛选。" :model="queryForm">
        <el-form-item label="关键字">
          <el-input v-model="queryForm.searchKey" placeholder="编码 / 名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="应用编码">
          <el-input v-model="queryForm.appCode" placeholder="请输入应用编码" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="应用名称">
          <el-input v-model="queryForm.appName" placeholder="请输入应用名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" clearable placeholder="全部状态">
            <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="queryForm.userType" clearable placeholder="全部类型">
            <el-option
              v-for="item in APP_USER_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <template #actions>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
        </template>
    </AuthSearchSection>

    <section
      v-memo="[loading, total, appList, selectedIds, statusLoadingId, queryForm.pageNum, queryForm.pageSize]"
      class="panel panel--table"
    >
      <div class="panel-header">
        <div>
          <h2 class="panel-title">应用列表</h2>
          <p class="panel-subtitle">用户绑定应用使用 `appCode`，角色归属也依赖应用编码。</p>
        </div>
      </div>

      <el-table v-loading="loading" :data="appList" row-key="id" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="52" />
        <el-table-column prop="appCode" label="应用编码" min-width="160" />
        <el-table-column prop="appName" label="应用名称" min-width="180" />
        <el-table-column label="用户类型" width="140">
          <template #default="{ row }">
            <el-tag type="info">{{ getUserTypeLabel(row.userType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :loading="statusLoadingId === row.id"
              inline-prompt
              active-text="启用"
              inactive-text="停用"
              @change="handleStatusChange(row, Number($event))"
            />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" min-width="168" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-space :size="10">
              <el-button link type="primary" @click="openDetailDrawer(row)">详情</el-button>
              <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="panel-footer">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadApps"
          @current-change="loadApps"
        />
      </div>
    </section>

    <el-drawer v-model="detailVisible" title="应用详情" size="500px">
      <div v-loading="detailLoading" class="detail-panel">
        <el-empty v-if="!detailLoading && !detailData" description="暂无应用详情" />
        <template v-else-if="detailData">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="应用编码">{{ detailData.appCode }}</el-descriptions-item>
            <el-descriptions-item label="应用名称">{{ detailData.appName }}</el-descriptions-item>
            <el-descriptions-item label="用户类型">{{ getUserTypeLabel(detailData.userType) }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="detailData.status === 1 ? 'success' : 'danger'">
                {{ detailData.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="备注">{{ detailData.remark || '--' }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </div>
    </el-drawer>

    <el-dialog
      v-model="formDialog.visible"
      :title="formDialog.mode === 'create' ? '新增应用' : '编辑应用'"
      width="620px"
      @close="resetFormDialog"
    >
      <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="96px">
        <el-form-item label="应用编码" prop="appCode">
          <el-input v-model="formModel.appCode" placeholder="请输入应用编码" />
        </el-form-item>
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="formModel.appName" placeholder="请输入应用名称" />
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-radio-group v-model="formModel.userType">
            <el-radio v-for="item in APP_USER_TYPE_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formModel.status">
            <el-radio v-for="item in STATUS_OPTIONS" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formModel.remark" type="textarea" placeholder="请输入应用备注" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="formDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="formDialog.submitting" @click="submitForm">
          {{ formDialog.mode === 'create' ? '创建应用' : '保存应用' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import AppPageHeader from '@/components/AppPageHeader.vue'
import AuthSearchSection from '@/views/system/auth/components/AuthSearchSection.vue'
import { APP_USER_TYPE_OPTIONS, DEFAULT_PAGE_SIZE, STATUS_OPTIONS } from '@/constants/admin'
import {
  batchDeleteAppsApi,
  createAppApi,
  deleteAppApi,
  getAppDetailApi,
  getAppPageApi,
  updateAppApi,
  updateAppStatusApi,
} from '@/api/app'
import { showGlobalError } from '@/stores/globalError'
import type { AppDTO, AppQueryDTO } from '@/types/system'
import { toPageResult, uniqueIds } from '@/utils/admin'

type FormMode = 'create' | 'edit'

interface AppFormModel {
  id?: string
  appCode: string
  appName: string
  status: number
  userType: number
  remark: string
}

const route = useRoute()
const formRef = ref<FormInstance>()

const loading = ref(false)
const total = ref(0)
const appList = ref<AppDTO[]>([])
const selectedIds = ref<string[]>([])
const statusLoadingId = ref('')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<AppDTO | null>(null)

const queryForm = reactive<AppQueryDTO>({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  searchKey: '',
  appCode: '',
  appName: '',
  status: undefined,
  userType: undefined,
})

const formDialog = reactive({
  visible: false,
  mode: 'create' as FormMode,
  submitting: false,
})

const formModel = reactive<AppFormModel>({
  id: '',
  appCode: '',
  appName: '',
  status: 1,
  userType: 1,
  remark: '',
})

const formRules: FormRules<AppFormModel> = {
  appCode: [{ required: true, message: '请输入应用编码', trigger: 'blur' }],
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
}

const pageTitle = computed(() => String(route.meta.title || '应用管理'))

const headerStats = computed(() => [
  { label: '应用总量', value: total.value, hint: '按后端分页总数统计' },
  { label: '系统应用', value: appList.value.filter((item) => item.userType === 1).length, hint: 'B 端后台可绑定应用' },
  { label: '会员应用', value: appList.value.filter((item) => item.userType === 2).length, hint: 'C 端会员应用' },
  { label: '当前页停用', value: appList.value.filter((item) => item.status === 0).length, hint: '停用应用不可继续授权' },
])

const getUserTypeLabel = (value?: number) => {
  return APP_USER_TYPE_OPTIONS.find((item) => item.value === value)?.label || '系统用户应用'
}

const resetFormDialog = () => {
  formDialog.submitting = false
  formModel.id = ''
  formModel.appCode = ''
  formModel.appName = ''
  formModel.status = 1
  formModel.userType = 1
  formModel.remark = ''
  formRef.value?.clearValidate()
}

const loadApps = async () => {
  loading.value = true

  try {
    const page = toPageResult(await getAppPageApi(queryForm))
    appList.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  loadApps()
}

const resetQuery = () => {
  queryForm.searchKey = ''
  queryForm.appCode = ''
  queryForm.appName = ''
  queryForm.status = undefined
  queryForm.userType = undefined
  handleSearch()
}

const handleSelectionChange = (rows: AppDTO[]) => {
  selectedIds.value = uniqueIds(rows.map((item) => item.id))
}

const openDetailDrawer = async (row: AppDTO) => {
  detailVisible.value = true
  detailLoading.value = true

  try {
    detailData.value = await getAppDetailApi(row.id)
  } finally {
    detailLoading.value = false
  }
}

const openCreateDialog = () => {
  resetFormDialog()
  formDialog.mode = 'create'
  formDialog.visible = true
}

const openEditDialog = async (row: AppDTO) => {
  resetFormDialog()
  formDialog.mode = 'edit'
  formDialog.visible = true

  try {
    const detail = await getAppDetailApi(row.id)
    formModel.id = detail.id
    formModel.appCode = detail.appCode
    formModel.appName = detail.appName
    formModel.status = detail.status ?? 1
    formModel.userType = detail.userType ?? 1
    formModel.remark = detail.remark || ''
  } catch (error) {
    formDialog.visible = false
    showGlobalError(error, { fallbackMessage: '加载应用详情失败' })
  }
}

const submitForm = async () => {
  if (!formRef.value || formDialog.submitting) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  formDialog.submitting = true

  try {
    const payload: AppDTO = {
      id: formModel.id || '0',
      appCode: formModel.appCode,
      appName: formModel.appName,
      status: formModel.status,
      userType: formModel.userType,
      remark: formModel.remark || undefined,
    }

    if (formDialog.mode === 'create') {
      await createAppApi(payload)
      ElMessage.success('应用创建成功')
    } else if (formModel.id) {
      await updateAppApi(formModel.id, payload)
      ElMessage.success('应用更新成功')
    }

    formDialog.visible = false
    await loadApps()
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '保存应用失败' })
  } finally {
    formDialog.submitting = false
  }
}

const handleStatusChange = async (row: AppDTO, nextStatus: number) => {
  const previousStatus = nextStatus === 1 ? 0 : 1
  statusLoadingId.value = row.id

  try {
    await updateAppStatusApi(row.id, { status: nextStatus })
    row.status = nextStatus
    ElMessage.success(nextStatus === 1 ? '应用已启用' : '应用已停用')
  } catch (error) {
    row.status = previousStatus
    showGlobalError(error, { fallbackMessage: '更新应用状态失败' })
  } finally {
    statusLoadingId.value = ''
  }
}

const handleDelete = async (row: AppDTO) => {
  try {
    await ElMessageBox.confirm(`确定删除应用“${row.appName}”吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteAppApi(row.id)
    ElMessage.success('应用已删除')
    await loadApps()
  } catch (error) {
    if (error !== 'cancel') {
      showGlobalError(error, { fallbackMessage: '删除应用失败' })
    }
  }
}

const handleBatchDelete = async () => {
  if (!selectedIds.value.length) return

  try {
    await ElMessageBox.confirm(`确定删除已选的 ${selectedIds.value.length} 个应用吗？`, '批量删除', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await batchDeleteAppsApi(selectedIds.value)
    selectedIds.value = []
    ElMessage.success('批量删除成功')
    await loadApps()
  } catch (error) {
    if (error !== 'cancel') {
      showGlobalError(error, { fallbackMessage: '批量删除应用失败' })
    }
  }
}

onMounted(async () => {
  await loadApps()
})
</script>

<style scoped>
.detail-panel {
  min-height: 240px;
}
</style>
