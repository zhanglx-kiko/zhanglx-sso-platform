<template>
  <div class="page-shell">
    <AppPageHeader
      :title="pageTitle"
      description="在一个页面内维护字典类型和字典数据，便于统一查看类型编码、值域和启停状态。"
      :stats="headerStats"
    >
      <template #actions>
        <el-button plain @click="reloadAll">刷新数据</el-button>
        <el-button type="primary" @click="openTypeDialog('create')">新增字典类型</el-button>
      </template>
    </AppPageHeader>

    <AuthSearchSection title="类型筛选" description="先定位字典类型，再在右侧维护该类型下的字典数据。" :model="typeQuery">
        <el-form-item label="关键字">
          <el-input v-model="typeQuery.searchKey" placeholder="名称 / 类型编码" clearable @keyup.enter="handleTypeSearch" />
        </el-form-item>
        <el-form-item label="字典名称">
          <el-input v-model="typeQuery.dictName" placeholder="请输入字典名称" clearable @keyup.enter="handleTypeSearch" />
        </el-form-item>
        <el-form-item label="类型编码">
          <el-input v-model="typeQuery.dictType" placeholder="请输入类型编码" clearable @keyup.enter="handleTypeSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="typeQuery.status" clearable placeholder="全部状态">
            <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <template #actions>
            <el-button type="primary" @click="handleTypeSearch">查询</el-button>
            <el-button @click="resetTypeQuery">重置</el-button>
        </template>
    </AuthSearchSection>

    <div class="dict-grid">
      <section
        v-memo="[typeLoading, typeTotal, typeList, selectedType, typeStatusLoadingId, typeQuery.pageNum, typeQuery.pageSize]"
        class="panel panel--table"
      >
        <div class="panel-header">
          <div>
            <h2 class="panel-title">字典类型</h2>
            <p class="panel-subtitle">点击某一行即可切换右侧的数据项列表。</p>
          </div>
          <el-button type="primary" link @click="openTypeDialog('create')">新增类型</el-button>
        </div>

        <el-table
          v-loading="typeLoading"
          :data="typeList"
          row-key="id"
          highlight-current-row
          @current-change="handleTypeCurrentChange"
        >
          <el-table-column prop="dictName" label="字典名称" min-width="160" />
          <el-table-column prop="dictType" label="类型编码" min-width="180" show-overflow-tooltip />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                :loading="typeStatusLoadingId === row.id"
                inline-prompt
                active-text="启用"
                inactive-text="停用"
                @change="handleTypeStatusChange(row, Number($event))"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-space :size="8">
                <el-button link type="primary" @click.stop="openTypeDialog('edit', row)">编辑</el-button>
                <el-button link type="danger" @click.stop="handleDeleteType(row)">删除</el-button>
              </el-space>
            </template>
          </el-table-column>
        </el-table>

        <div class="panel-footer">
          <el-pagination
            v-model:current-page="typeQuery.pageNum"
            v-model:page-size="typeQuery.pageSize"
            :page-sizes="[10, 20, 50]"
            :total="typeTotal"
            background
            layout="total, sizes, prev, pager, next"
            @size-change="loadTypePage"
            @current-change="loadTypePage"
          />
        </div>
      </section>

      <section
        v-memo="[dataLoading, dataTotal, dataList, selectedType, dataStatusLoadingId, dataQuery.pageNum, dataQuery.pageSize]"
        class="panel panel--table"
      >
        <div class="panel-header">
          <div>
            <h2 class="panel-title">字典数据</h2>
            <p class="panel-subtitle">
              当前类型：
              <strong>{{ selectedType?.dictName || '未选择' }}</strong>
              <span v-if="selectedType">（{{ selectedType.dictType }}）</span>
            </p>
          </div>
          <el-button type="primary" :disabled="!selectedType" @click="openDataDialog('create')">新增数据</el-button>
        </div>

        <AuthSearchForm :model="dataQuery" compact>
          <el-form-item label="关键字">
            <el-input
              v-model="dataQuery.searchKey"
              :disabled="!selectedType"
              placeholder="标签 / 键值"
              clearable
              @keyup.enter="handleDataSearch"
            />
          </el-form-item>
          <el-form-item label="标签">
            <el-input
              v-model="dataQuery.dictLabel"
              :disabled="!selectedType"
              placeholder="请输入标签"
              clearable
              @keyup.enter="handleDataSearch"
            />
          </el-form-item>
          <el-form-item label="键值">
            <el-input
              v-model="dataQuery.dictValue"
              :disabled="!selectedType"
              placeholder="请输入键值"
              clearable
              @keyup.enter="handleDataSearch"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="dataQuery.status" :disabled="!selectedType" clearable placeholder="全部状态">
              <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <template #actions>
              <el-button type="primary" :disabled="!selectedType" @click="handleDataSearch">查询</el-button>
              <el-button :disabled="!selectedType" @click="resetDataQuery">重置</el-button>
          </template>
        </AuthSearchForm>

        <el-empty v-if="!selectedType" description="请先选择左侧字典类型" />

        <template v-else>
          <el-table v-loading="dataLoading" :data="dataList" row-key="id">
            <el-table-column prop="dictLabel" label="标签" min-width="160" />
            <el-table-column prop="dictValue" label="键值" min-width="180" show-overflow-tooltip />
            <el-table-column prop="dictSort" label="排序" width="88" align="center" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-switch
                  v-model="row.status"
                  :active-value="1"
                  :inactive-value="0"
                  :loading="dataStatusLoadingId === row.id"
                  inline-prompt
                  active-text="启用"
                  inactive-text="停用"
                  @change="handleDataStatusChange(row, Number($event))"
                />
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-space :size="8">
                  <el-button link type="primary" @click="openDataDialog('edit', row)">编辑</el-button>
                  <el-button link type="danger" @click="handleDeleteData(row)">删除</el-button>
                </el-space>
              </template>
            </el-table-column>
          </el-table>

          <div class="panel-footer">
            <el-pagination
              v-model:current-page="dataQuery.pageNum"
              v-model:page-size="dataQuery.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="dataTotal"
              background
              layout="total, sizes, prev, pager, next"
              @size-change="loadDataPage"
              @current-change="loadDataPage"
            />
          </div>
        </template>
      </section>
    </div>

    <el-dialog
      v-model="typeDialog.visible"
      :title="typeDialog.mode === 'create' ? '新增字典类型' : '编辑字典类型'"
      width="560px"
      @close="resetTypeDialog"
    >
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="96px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="类型编码" prop="dictType">
          <el-input v-model="typeForm.dictType" placeholder="请输入类型编码" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="typeForm.status">
            <el-radio v-for="item in STATUS_OPTIONS" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="typeForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="typeDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="typeDialog.submitting" @click="submitTypeForm">
          {{ typeDialog.mode === 'create' ? '创建类型' : '保存类型' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="dataDialog.visible"
      :title="dataDialog.mode === 'create' ? '新增字典数据' : '编辑字典数据'"
      width="620px"
      @close="resetDataDialog"
    >
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="96px">
        <el-form-item label="所属类型" prop="dictType">
          <el-input v-model="dataForm.dictType" disabled />
        </el-form-item>
        <el-form-item label="标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="请输入标签" />
        </el-form-item>
        <el-form-item label="键值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="请输入键值" />
        </el-form-item>
        <el-form-item label="排序" prop="dictSort">
          <el-input-number v-model="dataForm.dictSort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dataForm.status">
            <el-radio v-for="item in STATUS_OPTIONS" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="dataForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dataDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dataDialog.submitting" @click="submitDataForm">
          {{ dataDialog.mode === 'create' ? '创建数据' : '保存数据' }}
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
import AuthSearchForm from '@/views/system/auth/components/AuthSearchForm.vue'
import AuthSearchSection from '@/views/system/auth/components/AuthSearchSection.vue'
import { DEFAULT_PAGE_SIZE, STATUS_OPTIONS } from '@/constants/admin'
import {
  createDictDataApi,
  createDictTypeApi,
  deleteDictDataApi,
  deleteDictTypeApi,
  getDictDataDetailApi,
  getDictDataPageApi,
  getDictTypeDetailApi,
  getDictTypePageApi,
  updateDictDataApi,
  updateDictDataStatusApi,
  updateDictTypeApi,
  updateDictTypeStatusApi,
} from '@/api/dict'
import { showGlobalError } from '@/stores/globalError'
import type { DictDataDTO, DictDataQueryDTO, DictTypeDTO, DictTypeQueryDTO } from '@/types/system'
import { toPageResult } from '@/utils/admin'

type DialogMode = 'create' | 'edit'

interface DictTypeFormModel {
  id?: string
  dictName: string
  dictType: string
  status: number
  remark: string
}

interface DictDataFormModel {
  id?: string
  dictSort: number
  dictLabel: string
  dictValue: string
  dictType: string
  status: number
  remark: string
}

const route = useRoute()
const typeFormRef = ref<FormInstance>()
const dataFormRef = ref<FormInstance>()

const typeLoading = ref(false)
const dataLoading = ref(false)
const typeTotal = ref(0)
const dataTotal = ref(0)
const typeList = ref<DictTypeDTO[]>([])
const dataList = ref<DictDataDTO[]>([])
const selectedType = ref<DictTypeDTO | null>(null)
const typeStatusLoadingId = ref('')
const dataStatusLoadingId = ref('')

const typeQuery = reactive<DictTypeQueryDTO>({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  searchKey: '',
  dictName: '',
  dictType: '',
  status: undefined,
})

const dataQuery = reactive<DictDataQueryDTO>({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  searchKey: '',
  dictType: '',
  dictLabel: '',
  dictValue: '',
  status: undefined,
})

const typeDialog = reactive({
  visible: false,
  mode: 'create' as DialogMode,
  submitting: false,
})

const dataDialog = reactive({
  visible: false,
  mode: 'create' as DialogMode,
  submitting: false,
})

const typeForm = reactive<DictTypeFormModel>({
  id: '',
  dictName: '',
  dictType: '',
  status: 1,
  remark: '',
})

const dataForm = reactive<DictDataFormModel>({
  id: '',
  dictSort: 0,
  dictLabel: '',
  dictValue: '',
  dictType: '',
  status: 1,
  remark: '',
})

const typeRules: FormRules<DictTypeFormModel> = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入类型编码', trigger: 'blur' }],
}

const dataRules: FormRules<DictDataFormModel> = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典键值', trigger: 'blur' }],
  dictType: [{ required: true, message: '请选择字典类型', trigger: 'blur' }],
}

const pageTitle = computed(() => String(route.meta.title || '字典管理'))

const headerStats = computed(() => [
  { label: '类型总量', value: typeTotal.value, hint: '按类型分页总数统计' },
  { label: '当前数据量', value: dataTotal.value, hint: selectedType.value ? `当前类型：${selectedType.value.dictType}` : '请选择类型' },
  { label: '启用类型', value: typeList.value.filter((item) => item.status === 1).length, hint: '当前页可用类型' },
  { label: '启用数据', value: dataList.value.filter((item) => item.status === 1).length, hint: '当前类型下可用数据' },
])

const resetTypeDialog = () => {
  typeDialog.submitting = false
  typeForm.id = ''
  typeForm.dictName = ''
  typeForm.dictType = ''
  typeForm.status = 1
  typeForm.remark = ''
  typeFormRef.value?.clearValidate()
}

const resetDataDialog = () => {
  dataDialog.submitting = false
  dataForm.id = ''
  dataForm.dictSort = 0
  dataForm.dictLabel = ''
  dataForm.dictValue = ''
  dataForm.dictType = selectedType.value?.dictType || ''
  dataForm.status = 1
  dataForm.remark = ''
  dataFormRef.value?.clearValidate()
}

const loadTypePage = async () => {
  typeLoading.value = true

  try {
    const page = toPageResult(await getDictTypePageApi(typeQuery))
    typeList.value = page.records
    typeTotal.value = page.total

    if (!page.records.length) {
      selectedType.value = null
      dataList.value = []
      dataTotal.value = 0
      dataQuery.dictType = ''
      return
    }

    const nextSelected = page.records.find((item) => item.id === selectedType.value?.id) ?? page.records[0]!

    if (!selectedType.value || selectedType.value.id !== nextSelected.id || dataQuery.dictType !== nextSelected.dictType) {
      selectedType.value = nextSelected
      dataQuery.dictType = nextSelected.dictType
      dataQuery.pageNum = 1
      await loadDataPage()
    }
  } finally {
    typeLoading.value = false
  }
}

const loadDataPage = async () => {
  if (!selectedType.value) {
    dataList.value = []
    dataTotal.value = 0
    return
  }

  dataLoading.value = true

  try {
    const page = toPageResult(
      await getDictDataPageApi({
        ...dataQuery,
        dictType: selectedType.value.dictType,
      }),
    )
    dataList.value = page.records
    dataTotal.value = page.total
  } finally {
    dataLoading.value = false
  }
}

const reloadAll = async () => {
  await loadTypePage()
}

const handleTypeSearch = () => {
  typeQuery.pageNum = 1
  loadTypePage()
}

const resetTypeQuery = () => {
  typeQuery.searchKey = ''
  typeQuery.dictName = ''
  typeQuery.dictType = ''
  typeQuery.status = undefined
  handleTypeSearch()
}

const handleDataSearch = () => {
  dataQuery.pageNum = 1
  loadDataPage()
}

const resetDataQuery = () => {
  dataQuery.searchKey = ''
  dataQuery.dictLabel = ''
  dataQuery.dictValue = ''
  dataQuery.status = undefined
  handleDataSearch()
}

const handleTypeCurrentChange = (row?: DictTypeDTO) => {
  if (!row) return

  selectedType.value = row
  dataQuery.dictType = row.dictType
  dataQuery.pageNum = 1
  loadDataPage()
}

const openTypeDialog = async (mode: DialogMode, row?: DictTypeDTO) => {
  resetTypeDialog()
  typeDialog.mode = mode
  typeDialog.visible = true

  if (mode === 'edit' && row) {
    try {
      const detail = await getDictTypeDetailApi(row.id)
      typeForm.id = detail.id
      typeForm.dictName = detail.dictName
      typeForm.dictType = detail.dictType
      typeForm.status = detail.status ?? 1
      typeForm.remark = detail.remark || ''
    } catch (error) {
      typeDialog.visible = false
      showGlobalError(error, { fallbackMessage: '加载字典类型详情失败' })
    }
  }
}

const submitTypeForm = async () => {
  if (!typeFormRef.value || typeDialog.submitting) return

  try {
    await typeFormRef.value.validate()
  } catch {
    return
  }

  typeDialog.submitting = true

  try {
    const payload: DictTypeDTO = {
      id: typeForm.id || '0',
      dictName: typeForm.dictName,
      dictType: typeForm.dictType,
      status: typeForm.status,
      remark: typeForm.remark || undefined,
    }

    if (typeDialog.mode === 'create') {
      await createDictTypeApi(payload)
      ElMessage.success('字典类型创建成功')
    } else if (typeForm.id) {
      await updateDictTypeApi(typeForm.id, payload)
      ElMessage.success('字典类型更新成功')
    }

    typeDialog.visible = false
    await loadTypePage()
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '保存字典类型失败' })
  } finally {
    typeDialog.submitting = false
  }
}

const handleTypeStatusChange = async (row: DictTypeDTO, nextStatus: number) => {
  const previousStatus = nextStatus === 1 ? 0 : 1
  typeStatusLoadingId.value = row.id

  try {
    await updateDictTypeStatusApi(row.id, { status: nextStatus })
    row.status = nextStatus
    ElMessage.success(nextStatus === 1 ? '字典类型已启用' : '字典类型已停用')
    await loadDataPage()
  } catch (error) {
    row.status = previousStatus
    showGlobalError(error, { fallbackMessage: '更新字典类型状态失败' })
  } finally {
    typeStatusLoadingId.value = ''
  }
}

const handleDeleteType = async (row: DictTypeDTO) => {
  try {
    await ElMessageBox.confirm(`确定删除字典类型“${row.dictName}”吗？若仍有数据项会删除失败。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteDictTypeApi(row.id)
    ElMessage.success('字典类型已删除')
    await loadTypePage()
  } catch (error) {
    if (error !== 'cancel') {
      showGlobalError(error, { fallbackMessage: '删除字典类型失败' })
    }
  }
}

const openDataDialog = async (mode: DialogMode, row?: DictDataDTO) => {
  if (!selectedType.value) {
    ElMessage.warning('请先选择字典类型')
    return
  }

  resetDataDialog()
  dataDialog.mode = mode
  dataDialog.visible = true
  dataForm.dictType = selectedType.value.dictType

  if (mode === 'edit' && row) {
    try {
      const detail = await getDictDataDetailApi(row.id)
      dataForm.id = detail.id
      dataForm.dictSort = detail.dictSort ?? 0
      dataForm.dictLabel = detail.dictLabel
      dataForm.dictValue = detail.dictValue
      dataForm.dictType = detail.dictType
      dataForm.status = detail.status ?? 1
      dataForm.remark = detail.remark || ''
    } catch (error) {
      dataDialog.visible = false
      showGlobalError(error, { fallbackMessage: '加载字典数据详情失败' })
    }
  }
}

const submitDataForm = async () => {
  if (!dataFormRef.value || dataDialog.submitting) return

  try {
    await dataFormRef.value.validate()
  } catch {
    return
  }

  dataDialog.submitting = true

  try {
    const payload: DictDataDTO = {
      id: dataForm.id || '0',
      dictSort: dataForm.dictSort,
      dictLabel: dataForm.dictLabel,
      dictValue: dataForm.dictValue,
      dictType: dataForm.dictType,
      status: dataForm.status,
      remark: dataForm.remark || undefined,
    }

    if (dataDialog.mode === 'create') {
      await createDictDataApi(payload)
      ElMessage.success('字典数据创建成功')
    } else if (dataForm.id) {
      await updateDictDataApi(dataForm.id, payload)
      ElMessage.success('字典数据更新成功')
    }

    dataDialog.visible = false
    await loadDataPage()
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '保存字典数据失败' })
  } finally {
    dataDialog.submitting = false
  }
}

const handleDataStatusChange = async (row: DictDataDTO, nextStatus: number) => {
  const previousStatus = nextStatus === 1 ? 0 : 1
  dataStatusLoadingId.value = row.id

  try {
    await updateDictDataStatusApi(row.id, { status: nextStatus })
    row.status = nextStatus
    ElMessage.success(nextStatus === 1 ? '字典数据已启用' : '字典数据已停用')
  } catch (error) {
    row.status = previousStatus
    showGlobalError(error, { fallbackMessage: '更新字典数据状态失败' })
  } finally {
    dataStatusLoadingId.value = ''
  }
}

const handleDeleteData = async (row: DictDataDTO) => {
  try {
    await ElMessageBox.confirm(`确定删除字典数据“${row.dictLabel}”吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteDictDataApi(row.id)
    ElMessage.success('字典数据已删除')
    await loadDataPage()
  } catch (error) {
    if (error !== 'cancel') {
      showGlobalError(error, { fallbackMessage: '删除字典数据失败' })
    }
  }
}

onMounted(async () => {
  await loadTypePage()
})
</script>

<style scoped>
.dict-grid {
  display: grid;
  grid-template-columns: minmax(340px, 0.9fr) minmax(0, 1.3fr);
  gap: 18px;
}

@media (max-width: 1200px) {
  .dict-grid {
    grid-template-columns: 1fr;
  }
}
</style>
