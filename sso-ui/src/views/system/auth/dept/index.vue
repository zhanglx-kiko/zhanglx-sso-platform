<template>
  <div class="page-shell">
    <template v-if="canListDepts">
    <AuthSearchSection :model="queryForm">
        <template #toolbar>
          <el-button plain @click="toggleExpand">{{ expandAll ? '收起树' : '展开树' }}</el-button>
          <el-button v-permission="'dept:add'" type="primary" @click="openCreateDialog()">新增根部门</el-button>
        </template>
        <el-form-item label="部门名称">
          <el-input v-model="queryForm.deptName" placeholder="请输入部门名称" clearable @keyup.enter="loadDeptTree" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" clearable placeholder="全部状态">
            <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <template #actions>
            <el-button type="primary" @click="loadDeptTree">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
        </template>
    </AuthSearchSection>

    <section v-memo="[loading, deptTree, expandAll, tableKey, statusLoadingId]" class="panel panel--table">
      <div class="panel-header">
        <div>
          <h2 class="panel-title">部门树</h2>
        </div>
      </div>

      <el-table
        :key="tableKey"
        v-loading="loading"
        :data="deptTree"
        row-key="id"
        :default-expand-all="expandAll"
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="deptName" label="部门名称" min-width="220" show-overflow-tooltip />
        <el-table-column prop="sortNum" label="排序" width="88" align="center" />
        <el-table-column prop="ancestors" label="祖级链" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-switch
              v-permission="'dept:status'"
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
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-space :size="10" wrap>
              <el-button v-permission="'dept:view'" link type="primary" @click="openDetailDrawer(row)">详情</el-button>
              <el-button v-permission="'dept:add'" link type="primary" @click="openCreateDialog(row)">新增下级</el-button>
              <el-button v-permission="{ all: ['dept:view', 'dept:edit'] }" link type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-button v-permission="'dept:remove'" link type="danger" @click="handleDelete(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-drawer v-model="detailVisible" title="部门详情" size="500px">
      <div v-loading="detailLoading" class="detail-panel">
        <el-empty v-if="!detailLoading && !detailData" description="暂无部门详情" />
        <template v-else-if="detailData">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="部门名称">{{ detailData.deptName || '--' }}</el-descriptions-item>
            <el-descriptions-item label="祖级链">{{ detailData.ancestors || '--' }}</el-descriptions-item>
            <el-descriptions-item label="排序">{{ detailData.sortNum ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="detailData.status === 1 ? 'success' : 'danger'">
                {{ detailData.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ detailData.updateTime || '--' }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </div>
    </el-drawer>

    <el-dialog
      v-model="formDialog.visible"
      :title="formDialog.mode === 'create' ? '新增部门' : '编辑部门'"
      width="560px"
      @close="resetFormDialog"
    >
      <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="96px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="formModel.parentId"
            :data="parentTreeOptions"
            :props="treeSelectProps"
            check-strictly
            clearable
            node-key="id"
            value-key="id"
            placeholder="根节点可留空或选择 0"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="formModel.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sortNum">
          <el-input-number v-model="formModel.sortNum" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formModel.status">
            <el-radio v-for="item in STATUS_OPTIONS" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="formDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="formDialog.submitting" @click="submitForm">
          {{ formDialog.mode === 'create' ? '创建部门' : '保存部门' }}
        </el-button>
      </template>
    </el-dialog>
    </template>
    <AuthNoPermissionPanel v-else description="当前账号暂无部门列表查看权限" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { usePermissionStore } from '@/stores/permission'
import AuthNoPermissionPanel from '@/views/system/auth/components/AuthNoPermissionPanel.vue'
import AuthSearchSection from '@/views/system/auth/components/AuthSearchSection.vue'
import { STATUS_OPTIONS } from '@/constants/admin'
import {
  createDeptApi,
  deleteDeptApi,
  getDeptDetailApi,
  getDeptTreeApi,
  updateDeptApi,
  updateDeptStatusApi,
} from '@/api/dept'
import { showGlobalError } from '@/stores/globalError'
import type { DeptDTO } from '@/types/system'
import { findNodeById } from '@/utils/admin'

type FormMode = 'create' | 'edit'

interface DeptFormModel {
  id?: string
  parentId: string
  deptName: string
  sortNum: number
  status: number
}

const formRef = ref<FormInstance>()
const permissionStore = usePermissionStore()
const canListDepts = computed(() => permissionStore.hasPermission('dept:list'))

const loading = ref(false)
const deptTree = ref<DeptDTO[]>([])
const expandAll = ref(true)
const tableKey = ref(0)
const statusLoadingId = ref('')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<DeptDTO | null>(null)

const queryForm = reactive({
  deptName: '',
  status: undefined as number | undefined,
})

const formDialog = reactive({
  visible: false,
  mode: 'create' as FormMode,
  submitting: false,
})

const formModel = reactive<DeptFormModel>({
  id: '',
  parentId: '0',
  deptName: '',
  sortNum: 0,
  status: 1,
})

const parentTreeOptions = ref<DeptDTO[]>([])
const treeSelectProps = {
  label: 'deptName',
  children: 'children',
  value: 'id',
}

const formRules: FormRules<DeptFormModel> = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
}

const getNextSortNum = <T extends { sortNum?: number }>(rows: T[]): number => {
  const currentMax = rows.reduce((max, item) => Math.max(max, Number(item.sortNum ?? 0)), 0)
  return currentMax + 1
}

const getCurrentLevelDepts = (nodes: DeptDTO[], parentId: string): DeptDTO[] => {
  if (parentId === '0') return nodes
  return findNodeById(nodes, parentId)?.children || []
}

const resolveNextDeptSortNum = async (parentId: string, fallbackSortNum: number): Promise<number> => {
  try {
    const fullTree = await getDeptTreeApi()
    parentTreeOptions.value = cloneTree(fullTree)
    return getNextSortNum(getCurrentLevelDepts(fullTree, parentId))
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '加载当前层级排序失败' })
    return fallbackSortNum
  }
}

const cloneTree = (nodes: DeptDTO[]): DeptDTO[] => {
  return nodes.map((item) => ({
    ...item,
    children: item.children ? cloneTree(item.children) : [],
  }))
}

const stripSelfAndDescendants = (nodes: DeptDTO[], blockedId: string): DeptDTO[] => {
  return nodes
    .filter((item) => item.id !== blockedId)
    .map((item) => ({
      ...item,
      children: stripSelfAndDescendants(item.children || [], blockedId),
    }))
}

const resetFormDialog = () => {
  formDialog.submitting = false
  formModel.id = ''
  formModel.parentId = '0'
  formModel.deptName = ''
  formModel.sortNum = 0
  formModel.status = 1
  parentTreeOptions.value = cloneTree(deptTree.value)
  formRef.value?.clearValidate()
}

const loadDeptTree = async () => {
  if (!canListDepts.value) {
    deptTree.value = []
    parentTreeOptions.value = []
    return
  }

  loading.value = true

  try {
    deptTree.value = await getDeptTreeApi({
      deptName: queryForm.deptName || undefined,
      status: queryForm.status,
    })
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  if (!canListDepts.value) return
  queryForm.deptName = ''
  queryForm.status = undefined
  loadDeptTree()
}

const toggleExpand = () => {
  expandAll.value = !expandAll.value
  tableKey.value += 1
}

const openDetailDrawer = async (row: DeptDTO) => {
  detailVisible.value = true
  detailLoading.value = true

  try {
    detailData.value = await getDeptDetailApi(row.id)
  } finally {
    detailLoading.value = false
  }
}

const openCreateDialog = async (parent?: DeptDTO) => {
  resetFormDialog()
  formDialog.mode = 'create'
  parentTreeOptions.value = cloneTree(deptTree.value)
  formModel.parentId = parent?.id || '0'
  formModel.sortNum = getNextSortNum(getCurrentLevelDepts(deptTree.value, formModel.parentId))
  formDialog.visible = true
  formModel.sortNum = await resolveNextDeptSortNum(formModel.parentId, formModel.sortNum)
}

const openEditDialog = async (row: DeptDTO) => {
  resetFormDialog()
  formDialog.mode = 'edit'
  formDialog.visible = true

  try {
    const detail = await getDeptDetailApi(row.id)
    parentTreeOptions.value = stripSelfAndDescendants(cloneTree(deptTree.value), row.id)
    formModel.id = detail.id
    formModel.parentId = detail.parentId || '0'
    formModel.deptName = detail.deptName
    formModel.sortNum = detail.sortNum ?? 0
    formModel.status = detail.status ?? 1
  } catch (error) {
    formDialog.visible = false
    showGlobalError(error, { fallbackMessage: '加载部门详情失败' })
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
    const payload: DeptDTO = {
      id: formModel.id || '0',
      parentId: formModel.parentId || '0',
      deptName: formModel.deptName,
      sortNum: formModel.sortNum,
      status: formModel.status,
    }

    if (formDialog.mode === 'create') {
      await createDeptApi(payload)
      ElMessage.success('部门创建成功')
    } else if (formModel.id) {
      await updateDeptApi(formModel.id, payload)
      ElMessage.success('部门更新成功')
    }

    formDialog.visible = false
    await loadDeptTree()
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '保存部门失败' })
  } finally {
    formDialog.submitting = false
  }
}

const handleStatusChange = async (row: DeptDTO, nextStatus: number) => {
  const previousStatus = nextStatus === 1 ? 0 : 1
  statusLoadingId.value = row.id

  try {
    await updateDeptStatusApi(row.id, { status: nextStatus })
    row.status = nextStatus
    ElMessage.success(nextStatus === 1 ? '部门已启用' : '部门已停用')
    await loadDeptTree()
  } catch (error) {
    row.status = previousStatus
    showGlobalError(error, { fallbackMessage: '更新部门状态失败' })
  } finally {
    statusLoadingId.value = ''
  }
}

const handleDelete = async (row: DeptDTO) => {
  try {
    await ElMessageBox.confirm(`确定删除部门“${row.deptName}”吗？若存在子部门将无法删除。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteDeptApi(row.id)
    ElMessage.success('部门已删除')
    await loadDeptTree()
  } catch (error) {
    if (error !== 'cancel') {
      showGlobalError(error, { fallbackMessage: '删除部门失败' })
    }
  }
}

onMounted(async () => {
  if (!canListDepts.value) {
    deptTree.value = []
    parentTreeOptions.value = []
    return
  }
  await loadDeptTree()
  parentTreeOptions.value = cloneTree(deptTree.value)
})
</script>

<style scoped>
.detail-panel {
  min-height: 240px;
}
</style>
