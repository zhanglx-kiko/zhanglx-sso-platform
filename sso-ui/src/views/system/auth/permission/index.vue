<template>
  <div class="page-shell">
    <template v-if="canListPermissions">
    <AuthSearchSection :model="queryForm">
        <template #toolbar>
          <el-button plain @click="toggleExpand">{{ expandAll ? '收起树' : '展开树' }}</el-button>
          <el-button v-permission="'permission:remove'" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
          <el-button v-permission="'permission:add'" type="primary" @click="openCreateDialog()">新增顶级节点</el-button>
        </template>
        <el-form-item label="权限名称 / 标识">
          <el-input
            v-model="queryForm.searchKey"
            placeholder="请输入完整名称或完整标识"
            clearable
            @keyup.enter="loadPermissionTree"
          />
        </el-form-item>
        <template #actions>
            <el-button type="primary" @click="loadPermissionTree">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
        </template>
    </AuthSearchSection>

    <section
      v-memo="[loading, permissionTree, selectedIds, expandAll, tableKey, statusLoadingId]"
      class="panel panel--table"
    >
      <div class="panel-header">
        <div>
          <h2 class="panel-title">权限树</h2>
        </div>
      </div>

      <el-table
        :key="tableKey"
        v-loading="loading"
        :data="permissionTree"
        row-key="id"
        :default-expand-all="expandAll"
        :tree-props="{ children: 'children' }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="52" />
        <el-table-column prop="name" label="节点名称" min-width="220" show-overflow-tooltip />
        <el-table-column label="类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ getPermissionTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="identification" label="权限标识" min-width="220" show-overflow-tooltip />
        <el-table-column prop="path" label="路由地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="comPath" label="组件路径" min-width="220" show-overflow-tooltip />
        <el-table-column prop="displayNo" label="排序" width="88" align="center" />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-switch
              v-permission="'permission:status'"
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
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-space :size="10" wrap>
              <el-button v-permission="'permission:view'" link type="primary" @click="openDetailDrawer(row)">详情</el-button>
              <el-button v-permission="'permission:add'" link type="primary" @click="openCreateDialog(row)">新增下级</el-button>
              <el-button v-permission="{ all: ['permission:view', 'permission:edit'] }" link type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-button v-permission="'permission:remove'" link type="danger" @click="handleDelete(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-drawer v-model="detailVisible" title="权限详情" size="520px">
      <div v-loading="detailLoading" class="detail-panel">
        <el-empty v-if="!detailLoading && !detailData" description="暂无权限详情" />
        <template v-else-if="detailData">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="名称">{{ detailData.name || '--' }}</el-descriptions-item>
            <el-descriptions-item label="标识">{{ detailData.identification || '--' }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ getPermissionTypeLabel(detailData.type) }}</el-descriptions-item>
            <el-descriptions-item label="路由地址">{{ detailData.path || '--' }}</el-descriptions-item>
            <el-descriptions-item label="组件路径">{{ detailData.comPath || '--' }}</el-descriptions-item>
            <el-descriptions-item label="图标">{{ detailData.iconStr || '--' }}</el-descriptions-item>
            <el-descriptions-item label="排序">{{ detailData.displayNo }}</el-descriptions-item>
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
      :title="formDialog.mode === 'create' ? '新增权限节点' : '编辑权限节点'"
      width="720px"
      @close="resetFormDialog"
    >
      <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="96px">
        <div class="form-grid">
          <el-form-item label="节点名称" prop="name">
            <el-input v-model="formModel.name" placeholder="请输入节点名称" />
          </el-form-item>
          <el-form-item label="权限标识" prop="identification">
            <el-input v-model="formModel.identification" placeholder="请输入唯一权限标识" />
          </el-form-item>
          <el-form-item label="父节点" prop="parentId">
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
          <el-form-item label="节点类型" prop="type">
            <el-select v-model="formModel.type" placeholder="请选择节点类型">
              <el-option
                v-for="item in PERMISSION_TYPE_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="路由地址" prop="path">
            <el-input v-model="formModel.path" placeholder="/system/auth/config" />
          </el-form-item>
          <el-form-item label="组件路径" prop="comPath">
            <el-input v-model="formModel.comPath" placeholder="system/auth/config/index" />
          </el-form-item>
          <el-form-item label="图标" prop="iconStr">
            <el-input v-model="formModel.iconStr" placeholder="Setting / Lock" />
          </el-form-item>
          <el-form-item label="排序" prop="displayNo">
            <el-input-number v-model="formModel.displayNo" :min="0" :max="9999" />
          </el-form-item>
        </div>

        <el-form-item label="外链类型" prop="isFrame">
          <el-radio-group v-model="formModel.isFrame">
            <el-radio :value="0">非外链</el-radio>
            <el-radio :value="1">外链</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formModel.status">
            <el-radio v-for="item in STATUS_OPTIONS" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="formModel.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="formDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="formDialog.submitting" @click="submitForm">
          {{ formDialog.mode === 'create' ? '创建节点' : '保存节点' }}
        </el-button>
      </template>
    </el-dialog>
    </template>
    <AuthNoPermissionPanel v-else description="当前账号暂无权限列表查看权限" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { usePermissionStore } from '@/stores/permission'
import AuthNoPermissionPanel from '@/views/system/auth/components/AuthNoPermissionPanel.vue'
import AuthSearchSection from '@/views/system/auth/components/AuthSearchSection.vue'
import { PERMISSION_TYPE_OPTIONS, STATUS_OPTIONS } from '@/constants/admin'
import {
  batchDeletePermissionsApi,
  createPermissionApi,
  deletePermissionApi,
  getPermissionDetailApi,
  getPermissionTreeApi,
  updatePermissionApi,
  updatePermissionStatusApi,
} from '@/api/permission'
import { showGlobalError } from '@/stores/globalError'
import type { PermissionDTO } from '@/types/system'
import { extractTreeIds, uniqueIds } from '@/utils/admin'

type FormMode = 'create' | 'edit'

interface PermissionFormModel {
  id?: string
  name: string
  identification: string
  parentId: string
  path: string
  comPath: string
  iconStr: string
  displayNo: number
  isFrame: number
  type: number
  status: number
  remark: string
}

const formRef = ref<FormInstance>()
const permissionStore = usePermissionStore()
const canListPermissions = computed(() => permissionStore.hasPermission('permission:list'))

const loading = ref(false)
const permissionTree = ref<PermissionDTO[]>([])
const selectedIds = ref<string[]>([])
const statusLoadingId = ref('')
const expandAll = ref(false)
const tableKey = ref(0)

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<PermissionDTO | null>(null)

const queryForm = reactive({
  searchKey: '',
})

const formDialog = reactive({
  visible: false,
  mode: 'create' as FormMode,
  submitting: false,
})

const formModel = reactive<PermissionFormModel>({
  id: '',
  name: '',
  identification: '',
  parentId: '0',
  path: '',
  comPath: '',
  iconStr: '',
  displayNo: 0,
  isFrame: 0,
  type: 1,
  status: 1,
  remark: '',
})

const parentTreeOptions = ref<PermissionDTO[]>([])
const treeSelectProps = {
  label: 'name',
  children: 'children',
  value: 'id',
}

const formRules: FormRules<PermissionFormModel> = {
  name: [{ required: true, message: '请输入节点名称', trigger: 'blur' }],
  identification: [{ required: true, message: '请输入权限标识', trigger: 'blur' }],
}

const getPermissionTypeLabel = (value?: number) => {
  return PERMISSION_TYPE_OPTIONS.find((item) => item.value === value)?.label || '未知'
}

const cloneTree = (nodes: PermissionDTO[]): PermissionDTO[] => {
  return nodes.map((item) => ({
    ...item,
    children: item.children ? cloneTree(item.children) : [],
  }))
}

const stripSelfAndDescendants = (nodes: PermissionDTO[], blockedId: string): PermissionDTO[] => {
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
  formModel.name = ''
  formModel.identification = ''
  formModel.parentId = '0'
  formModel.path = ''
  formModel.comPath = ''
  formModel.iconStr = ''
  formModel.displayNo = 0
  formModel.isFrame = 0
  formModel.type = 1
  formModel.status = 1
  formModel.remark = ''
  parentTreeOptions.value = cloneTree(permissionTree.value)
  formRef.value?.clearValidate()
}

const loadPermissionTree = async () => {
  if (!canListPermissions.value) {
    permissionTree.value = []
    selectedIds.value = []
    parentTreeOptions.value = []
    return
  }

  loading.value = true

  try {
    permissionTree.value = await getPermissionTreeApi(queryForm.searchKey || undefined)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  if (!canListPermissions.value) return
  queryForm.searchKey = ''
  loadPermissionTree()
}

const toggleExpand = () => {
  expandAll.value = !expandAll.value
  tableKey.value += 1
}

const handleSelectionChange = (rows: PermissionDTO[]) => {
  selectedIds.value = uniqueIds(rows.map((item) => item.id))
}

const openDetailDrawer = async (row: PermissionDTO) => {
  detailVisible.value = true
  detailLoading.value = true

  try {
    detailData.value = await getPermissionDetailApi(row.id)
  } finally {
    detailLoading.value = false
  }
}

const openCreateDialog = (parent?: PermissionDTO) => {
  resetFormDialog()
  formDialog.mode = 'create'
  parentTreeOptions.value = cloneTree(permissionTree.value)
  formModel.parentId = parent?.id || '0'
  formModel.type = parent ? Math.min((parent.type ?? 0) + 1, 3) : 1
  formDialog.visible = true
}

const openEditDialog = async (row: PermissionDTO) => {
  resetFormDialog()
  formDialog.mode = 'edit'
  formDialog.visible = true

  try {
    const detail = await getPermissionDetailApi(row.id)
    parentTreeOptions.value = stripSelfAndDescendants(cloneTree(permissionTree.value), row.id)
    formModel.id = detail.id
    formModel.name = detail.name
    formModel.identification = detail.identification
    formModel.parentId = detail.parentId || '0'
    formModel.path = detail.path || ''
    formModel.comPath = detail.comPath || ''
    formModel.iconStr = detail.iconStr || ''
    formModel.displayNo = detail.displayNo ?? 0
    formModel.isFrame = detail.isFrame ?? 0
    formModel.type = detail.type
    formModel.status = detail.status ?? 1
    formModel.remark = detail.remark || ''
  } catch (error) {
    formDialog.visible = false
    showGlobalError(error, { fallbackMessage: '加载权限详情失败' })
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
    const payload: PermissionDTO = {
      id: formModel.id || '0',
      name: formModel.name,
      identification: formModel.identification,
      parentId: formModel.parentId || '0',
      path: formModel.path || undefined,
      comPath: formModel.comPath || undefined,
      iconStr: formModel.iconStr || undefined,
      displayNo: formModel.displayNo,
      isFrame: formModel.isFrame,
      type: formModel.type,
      status: formModel.status,
      remark: formModel.remark || undefined,
    }

    if (formDialog.mode === 'create') {
      await createPermissionApi(payload)
      ElMessage.success('权限节点创建成功')
    } else if (formModel.id) {
      await updatePermissionApi(formModel.id, payload)
      ElMessage.success('权限节点更新成功')
    }

    formDialog.visible = false
    await loadPermissionTree()
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '保存权限节点失败' })
  } finally {
    formDialog.submitting = false
  }
}

const handleStatusChange = async (row: PermissionDTO, nextStatus: number) => {
  const previousStatus = nextStatus === 1 ? 0 : 1
  statusLoadingId.value = row.id

  try {
    await updatePermissionStatusApi(row.id, { status: nextStatus })
    row.status = nextStatus
    ElMessage.success(nextStatus === 1 ? '节点已启用' : '节点已停用')
  } catch (error) {
    row.status = previousStatus
    showGlobalError(error, { fallbackMessage: '更新节点状态失败' })
  } finally {
    statusLoadingId.value = ''
  }
}

const confirmDelete = async (ids: string[], title: string) => {
  try {
    await ElMessageBox.confirm(`${title}。删除会级联清理下级节点与角色绑定关系。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return false
  }

  if (ids.length === 1) {
    await deletePermissionApi(ids[0]!)
  } else {
    await batchDeletePermissionsApi(ids)
  }

  return true
}

const handleDelete = async (row: PermissionDTO) => {
  try {
    const confirmed = await confirmDelete([row.id], `确定删除节点“${row.name}”吗？`)
    if (!confirmed) return
    ElMessage.success('节点已删除')
    await loadPermissionTree()
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '删除节点失败' })
  }
}

const findNodeById = (nodes: PermissionDTO[], id: string): PermissionDTO | null => {
  for (const node of nodes) {
    if (node.id === id) return node
    const childMatched = findNodeById(node.children || [], id)
    if (childMatched) return childMatched
  }

  return null
}

const handleBatchDelete = async () => {
  if (!selectedIds.value.length) return

  const expandedIds = uniqueIds([
    ...selectedIds.value,
    ...selectedIds.value.flatMap((id) => {
      const node = findNodeById(permissionTree.value, id)
      return node ? extractTreeIds(node.children || []) : []
    }),
  ])

  try {
    const confirmed = await confirmDelete(expandedIds, `确定删除已选的 ${selectedIds.value.length} 个节点吗？`)
    if (!confirmed) return
    selectedIds.value = []
    ElMessage.success('批量删除成功')
    await loadPermissionTree()
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '批量删除节点失败' })
  }
}

onMounted(async () => {
  if (!canListPermissions.value) {
    permissionTree.value = []
    parentTreeOptions.value = []
    return
  }
  queryForm.searchKey = ''
  formModel.displayNo = 0
  await loadPermissionTree()
  parentTreeOptions.value = cloneTree(permissionTree.value)
})
</script>

<style scoped>
.detail-panel {
  min-height: 280px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 14px;
}

@media (max-width: 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
