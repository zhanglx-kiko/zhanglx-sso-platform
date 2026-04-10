<template>
  <div class="page-shell">
    <AppPageHeader
      :title="pageTitle"
      description="维护系统参数键值、内置属性和备注信息，为默认密码、开关型配置等能力提供统一入口。"
      :stats="headerStats"
    >
      <template #actions>
        <el-button plain @click="loadConfigs">刷新列表</el-button>
        <el-button plain @click="handleRefreshRuntimeCache">刷新运行时缓存</el-button>
        <el-button type="primary" @click="openCreateDialog">新增参数</el-button>
      </template>
    </AppPageHeader>

    <AuthSearchSection title="筛选条件" description="支持按参数名称、参数键、分组、敏感标识和状态筛选。" :model="queryForm">
        <el-form-item label="关键字">
          <el-input v-model="queryForm.searchKey" placeholder="参数名称 / 参数键" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="参数名称">
          <el-input v-model="queryForm.configName" placeholder="请输入参数名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="参数键">
          <el-input v-model="queryForm.configKey" placeholder="请输入参数键" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="参数分组">
          <el-input v-model="queryForm.configGroup" placeholder="请输入参数分组" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="敏感参数">
          <el-select v-model="queryForm.sensitiveFlag" clearable placeholder="全部">
            <el-option v-for="item in YES_NO_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" clearable placeholder="全部状态">
            <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="参数类型">
          <el-select v-model="queryForm.configType" clearable placeholder="全部类型">
            <el-option
              v-for="item in CONFIG_TYPE_OPTIONS"
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

    <section v-memo="[loading, total, configList, queryForm.pageNum, queryForm.pageSize]" class="panel panel--table">
      <div class="panel-header">
        <div>
          <h2 class="panel-title">参数列表</h2>
          <p class="panel-subtitle">系统内置参数不允许删除，并限制关键字段修改。</p>
        </div>
      </div>

      <el-table v-loading="loading" :data="configList" row-key="id">
        <el-table-column prop="configName" label="参数名称" min-width="180" />
        <el-table-column prop="configKey" label="参数键" min-width="220" show-overflow-tooltip />
        <el-table-column prop="configGroup" label="参数分组" min-width="140" show-overflow-tooltip />
        <el-table-column prop="configValue" label="参数值" min-width="220" show-overflow-tooltip />
        <el-table-column label="敏感参数" width="110">
          <template #default="{ row }">
            <el-tag :type="row.sensitiveFlag === 1 ? 'danger' : 'info'">
              {{ row.sensitiveFlag === 1 ? '敏感' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="参数类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.configType === 1 ? 'warning' : 'info'">
              {{ row.configType === 1 ? '系统内置' : '普通配置' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" min-width="168" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-space :size="10">
              <el-button link type="primary" @click="openDetailDrawer(row)">详情</el-button>
              <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-button link type="danger" :disabled="row.configType === 1" @click="handleDelete(row)">删除</el-button>
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
          @size-change="loadConfigs"
          @current-change="loadConfigs"
        />
      </div>
    </section>

    <el-drawer v-model="detailVisible" title="参数详情" size="520px">
      <div v-loading="detailLoading" class="detail-panel">
        <el-empty v-if="!detailLoading && !detailData" description="暂无参数详情" />
        <template v-else-if="detailData">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="参数 ID">{{ detailData.id }}</el-descriptions-item>
            <el-descriptions-item label="参数名称">{{ detailData.configName }}</el-descriptions-item>
            <el-descriptions-item label="参数键">{{ detailData.configKey }}</el-descriptions-item>
            <el-descriptions-item label="参数分组">{{ detailData.configGroup }}</el-descriptions-item>
            <el-descriptions-item label="参数值">{{ detailData.configValue }}</el-descriptions-item>
            <el-descriptions-item label="敏感参数">{{ detailData.sensitiveFlag === 1 ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ detailData.status === 1 ? '启用' : '停用' }}</el-descriptions-item>
            <el-descriptions-item label="参数类型">
              {{ detailData.configType === 1 ? '系统内置' : '普通配置' }}
            </el-descriptions-item>
            <el-descriptions-item label="备注">{{ detailData.remark || '--' }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </div>
    </el-drawer>

    <el-dialog
      v-model="formDialog.visible"
      :title="formDialog.mode === 'create' ? '新增参数' : '编辑参数'"
      width="640px"
      @close="resetFormDialog"
    >
      <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="96px">
        <el-form-item label="参数名称" prop="configName">
          <el-input v-model="formModel.configName" placeholder="请输入参数名称" />
        </el-form-item>
        <el-form-item label="参数键" prop="configKey">
          <el-input v-model="formModel.configKey" placeholder="请输入参数键" :disabled="formDialog.mode === 'edit' && formModel.configType === 1" />
        </el-form-item>
        <el-form-item label="参数分组" prop="configGroup">
          <el-input v-model="formModel.configGroup" placeholder="请输入参数分组" :disabled="formDialog.mode === 'edit' && formModel.configType === 1" />
        </el-form-item>
        <el-form-item label="参数值" prop="configValue">
          <el-input
            v-model="formModel.configValue"
            type="textarea"
            :placeholder="formModel.sensitiveFlag === 1 ? '请输入敏感参数值；如未修改请保持脱敏占位符原样保存' : '请输入参数值'"
          />
        </el-form-item>
        <el-form-item label="敏感参数" prop="sensitiveFlag">
          <el-radio-group v-model="formModel.sensitiveFlag" :disabled="formDialog.mode === 'edit' && formModel.configType === 1">
            <el-radio v-for="item in YES_NO_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formModel.status" :disabled="formDialog.mode === 'edit' && formModel.configType === 1">
            <el-radio v-for="item in STATUS_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="参数类型" prop="configType">
          <el-radio-group v-model="formModel.configType">
            <el-radio v-for="item in CONFIG_TYPE_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formModel.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="formDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="formDialog.submitting" @click="submitForm">
          {{ formDialog.mode === 'create' ? '创建参数' : '保存参数' }}
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
import { CONFIG_TYPE_OPTIONS, DEFAULT_PAGE_SIZE, STATUS_OPTIONS, YES_NO_OPTIONS } from '@/constants/admin'
import {
  createConfigApi,
  deleteConfigApi,
  getConfigDetailApi,
  getConfigPageApi,
  refreshConfigRuntimeCacheApi,
  updateConfigApi,
} from '@/api/config'
import { showGlobalError } from '@/stores/globalError'
import type { ConfigDTO, ConfigQueryDTO } from '@/types/system'
import { toPageResult } from '@/utils/admin'

type FormMode = 'create' | 'edit'

interface ConfigFormModel {
  id?: string
  configName: string
  configKey: string
  configValue: string
  configGroup: string
  sensitiveFlag: number
  status: number
  configType: number
  remark: string
}

const route = useRoute()
const formRef = ref<FormInstance>()

const loading = ref(false)
const total = ref(0)
const configList = ref<ConfigDTO[]>([])

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<ConfigDTO | null>(null)

const queryForm = reactive<ConfigQueryDTO>({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  searchKey: '',
  configName: '',
  configKey: '',
  configGroup: '',
  sensitiveFlag: undefined,
  status: undefined,
  configType: undefined,
})

const formDialog = reactive({
  visible: false,
  mode: 'create' as FormMode,
  submitting: false,
})

const formModel = reactive<ConfigFormModel>({
  id: '',
  configName: '',
  configKey: '',
  configValue: '',
  configGroup: 'default',
  sensitiveFlag: 0,
  status: 1,
  configType: 0,
  remark: '',
})

const formRules: FormRules<ConfigFormModel> = {
  configName: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
  configKey: [{ required: true, message: '请输入参数键', trigger: 'blur' }],
  configGroup: [{ required: true, message: '请输入参数分组', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入参数值', trigger: 'blur' }],
}

const pageTitle = computed(() => String(route.meta.title || '系统参数'))

const headerStats = computed(() => [
  { label: '参数总量', value: total.value, hint: '按后端分页总数统计' },
  { label: '系统内置', value: configList.value.filter((item) => item.configType === 1).length, hint: '内置参数不允许删除' },
  { label: '敏感参数', value: configList.value.filter((item) => item.sensitiveFlag === 1).length, hint: '读取时统一脱敏显示' },
  { label: '普通配置', value: configList.value.filter((item) => item.configType === 0).length, hint: '可由管理台维护的参数' },
])

const resetFormDialog = () => {
  formDialog.submitting = false
  formModel.id = ''
  formModel.configName = ''
  formModel.configKey = ''
  formModel.configValue = ''
  formModel.configGroup = 'default'
  formModel.sensitiveFlag = 0
  formModel.status = 1
  formModel.configType = 0
  formModel.remark = ''
  formRef.value?.clearValidate()
}

const loadConfigs = async () => {
  loading.value = true

  try {
    const page = toPageResult(await getConfigPageApi(queryForm))
    configList.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  loadConfigs()
}

const resetQuery = () => {
  queryForm.searchKey = ''
  queryForm.configName = ''
  queryForm.configKey = ''
  queryForm.configGroup = ''
  queryForm.sensitiveFlag = undefined
  queryForm.status = undefined
  queryForm.configType = undefined
  handleSearch()
}

const openDetailDrawer = async (row: ConfigDTO) => {
  detailVisible.value = true
  detailLoading.value = true

  try {
    detailData.value = await getConfigDetailApi(row.id)
  } finally {
    detailLoading.value = false
  }
}

const openCreateDialog = () => {
  resetFormDialog()
  formDialog.mode = 'create'
  formDialog.visible = true
}

const openEditDialog = async (row: ConfigDTO) => {
  resetFormDialog()
  formDialog.mode = 'edit'
  formDialog.visible = true

  try {
    const detail = await getConfigDetailApi(row.id)
    formModel.id = detail.id
    formModel.configName = detail.configName
    formModel.configKey = detail.configKey
    formModel.configValue = detail.configValue
    formModel.configGroup = detail.configGroup
    formModel.sensitiveFlag = detail.sensitiveFlag ?? 0
    formModel.status = detail.status ?? 1
    formModel.configType = detail.configType ?? 0
    formModel.remark = detail.remark || ''
  } catch (error) {
    formDialog.visible = false
    showGlobalError(error, { fallbackMessage: '加载参数详情失败' })
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
    const payload: ConfigDTO = {
      id: formModel.id || '0',
      configName: formModel.configName,
      configKey: formModel.configKey,
      configValue: formModel.configValue,
      configGroup: formModel.configGroup,
      sensitiveFlag: formModel.sensitiveFlag,
      status: formModel.status,
      configType: formModel.configType,
      remark: formModel.remark || undefined,
    }

    if (formDialog.mode === 'create') {
      await createConfigApi(payload)
      ElMessage.success('参数创建成功')
    } else if (formModel.id) {
      await updateConfigApi(formModel.id, payload)
      ElMessage.success('参数更新成功')
    }

    formDialog.visible = false
    await loadConfigs()
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '保存参数失败' })
  } finally {
    formDialog.submitting = false
  }
}

const handleDelete = async (row: ConfigDTO) => {
  if (row.configType === 1) {
    ElMessage.warning('系统内置参数不允许删除')
    return
  }

  try {
    await ElMessageBox.confirm(`确定删除参数“${row.configName}”吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteConfigApi(row.id)
    ElMessage.success('参数已删除')
    await loadConfigs()
  } catch (error) {
    if (error !== 'cancel') {
      showGlobalError(error, { fallbackMessage: '删除参数失败' })
    }
  }
}

const handleRefreshRuntimeCache = async () => {
  try {
    await refreshConfigRuntimeCacheApi()
    ElMessage.success('运行时配置缓存已刷新')
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '刷新运行时配置缓存失败' })
  }
}

onMounted(async () => {
  await loadConfigs()
})
</script>

<style scoped>
.detail-panel {
  min-height: 240px;
}
</style>
