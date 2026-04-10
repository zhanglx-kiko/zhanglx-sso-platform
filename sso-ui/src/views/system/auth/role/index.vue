<template>
  <div class="page-shell">
    <AuthSearchSection :model="queryForm">
        <template #toolbar>
          <el-button plain @click="loadRoles">刷新列表</el-button>
          <el-button :disabled="!selectedRoleIds.length" @click="handleBatchDelete">
            批量删除
          </el-button>
          <el-button type="primary" @click="openCreateDialog">新增角色</el-button>
        </template>
        <el-form-item label="关键字">
          <el-input
            v-model="queryForm.searchKey"
            placeholder="角色名称 / 编码 / 应用编码"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <template #actions>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
        </template>
    </AuthSearchSection>

    <section v-memo="[loading, total, roleList, selectedRoleIds, statusLoadingId, queryForm.pageNum, queryForm.pageSize]" class="panel panel--table">
      <div class="panel-header">
        <div>
          <h2 class="panel-title">角色列表</h2>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="roleList"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="52" />
        <el-table-column prop="roleName" label="角色名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="roleCode" label="角色编码" min-width="180" show-overflow-tooltip />
        <el-table-column prop="appCode" label="所属应用" min-width="120" />
        <el-table-column label="数据范围" width="150">
          <template #default="{ row }">
            <el-tag type="info">{{ getDataScopeLabel(row.dataScope) }}</el-tag>
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
              active-text="启"
              inactive-text="停"
              @change="handleStatusChange(row, Number($event))"
            />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-space :size="10" wrap>
              <el-button link type="primary" @click="openDetailDrawer(row)">详情</el-button>
              <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-dropdown @command="handleRowCommand($event, row)">
                <el-button link type="primary">
                  更多
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="users">绑定用户</el-dropdown-item>
                    <el-dropdown-item command="permissions">权限授权</el-dropdown-item>
                    <el-dropdown-item command="depts">部门范围</el-dropdown-item>
                    <el-dropdown-item command="delete" divided>删除角色</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
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
          @size-change="loadRoles"
          @current-change="loadRoles"
        />
      </div>
    </section>

    <el-drawer v-model="detailVisible" title="角色详情" size="540px">
      <div v-loading="detailLoading" class="detail-panel">
        <el-empty v-if="!detailLoading && !detailData" description="暂无角色详情" />
        <template v-else-if="detailData">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="角色名称">
              {{ detailData.roleName || '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="角色编码">
              {{ detailData.roleCode || '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="所属应用">
              {{ detailData.appCode || 'sso' }}
            </el-descriptions-item>
            <el-descriptions-item label="数据范围">
              {{ getDataScopeLabel(detailData.dataScope) }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="detailData.status === 1 ? 'success' : 'danger'">
                {{ detailData.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="备注">
              {{ detailData.remark || '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ detailData.updateTime || '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="已绑权限">
              <div class="permission-chip-list">
                <el-tag
                  v-for="item in detailData.rolePermissions || []"
                  :key="item.id"
                  type="info"
                  class="permission-chip"
                >
                  {{ item.name }}
                </el-tag>
                <span v-if="!(detailData.rolePermissions || []).length" class="soft-note">暂无</span>
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </template>
      </div>
    </el-drawer>

    <el-dialog
      v-model="formDialog.visible"
      :title="formDialog.mode === 'create' ? '新增角色' : '编辑角色'"
      width="640px"
      @close="resetFormDialog"
    >
      <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="96px">
        <div class="form-grid">
          <el-form-item label="角色名称" prop="roleName">
            <el-input v-model="formModel.roleName" placeholder="请输入角色名称" />
          </el-form-item>
          <el-form-item label="角色编码" prop="roleCode">
            <el-input v-model="formModel.roleCode" placeholder="请输入角色编码" />
          </el-form-item>
          <el-form-item label="所属应用" prop="appCode">
            <el-select v-model="formModel.appCode" placeholder="默认 sso" clearable>
              <el-option
                v-for="item in appOptions"
                :key="item.appCode"
                :label="`${item.appName} (${item.appCode})`"
                :value="item.appCode"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="数据范围" prop="dataScope">
            <el-select v-model="formModel.dataScope" placeholder="请选择数据范围">
              <el-option
                v-for="item in ROLE_DATA_SCOPE_OPTIONS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formModel.status">
            <el-radio v-for="item in STATUS_OPTIONS" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="formModel.remark" type="textarea" placeholder="请输入角色备注" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="formDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="formDialog.submitting" @click="submitForm">
          {{ formDialog.mode === 'create' ? '创建角色' : '保存角色' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="userDialog.visible"
      title="绑定用户"
      width="760px"
      @close="resetUserDialog"
    >
      <div v-loading="userDialog.loading" class="binding-panel">
        <p class="soft-note">保存时会直接覆盖当前角色的用户绑定列表，空数组表示清空。</p>
        <el-transfer
          v-model="userDialog.selectedIds"
          filterable
          :data="userDialog.options"
          :titles="['候选用户', '已绑定用户']"
          :props="{ key: 'key', label: 'label', disabled: 'disabled' }"
        />
      </div>

      <template #footer>
        <el-button @click="userDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="userDialog.saving" @click="submitUserBindings">
          保存用户绑定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="permissionDialog.visible"
      title="权限授权"
      width="860px"
      @close="resetPermissionDialog"
    >
      <div v-loading="permissionDialog.loading" class="binding-panel">
        <div class="tree-toolbar">
          <el-input
            v-model="permissionDialog.filterText"
            placeholder="筛选权限名称或标识"
            clearable
          />
          <el-button plain @click="togglePermissionExpand">
            {{ permissionDialog.expandAll ? '收起全部' : '展开全部' }}
          </el-button>
        </div>
        <p class="soft-note">
          没有单独菜单接口，菜单、按钮、接口都在同一棵权限树中；保存时会提交勾选节点与半选父节点。
        </p>
        <el-tree
          ref="permissionTreeRef"
          :key="permissionDialog.treeKey"
          :data="permissionDialog.treeData"
          node-key="id"
          show-checkbox
          :default-expand-all="permissionDialog.expandAll"
          check-on-click-node
          :props="permissionTreeProps"
          :filter-node-method="filterPermissionNode"
          class="permission-tree"
        >
          <template #default="{ data }">
            <div class="tree-node">
              <span class="tree-node__label">{{ data.name }}</span>
              <el-tag size="small" type="info">{{ getPermissionTypeLabel(data.type) }}</el-tag>
              <span class="tree-node__meta">{{ data.identification }}</span>
            </div>
          </template>
        </el-tree>
      </div>

      <template #footer>
        <el-button @click="permissionDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="permissionDialog.saving" @click="submitPermissionBindings">
          保存授权
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="deptDialog.visible"
      title="部门范围授权"
      width="760px"
      @close="resetDeptDialog"
    >
      <div v-loading="deptDialog.loading" class="binding-panel">
        <p class="soft-note">
          只有 `dataScope = 5` 的角色允许绑定自定义部门范围。保存时直接提交部门 ID 数组。
        </p>
        <el-tree
          ref="deptTreeRef"
          :data="deptDialog.treeData"
          node-key="id"
          show-checkbox
          default-expand-all
          check-on-click-node
          :props="deptTreeProps"
          class="permission-tree"
        >
          <template #default="{ data }">
            <div class="tree-node">
              <span class="tree-node__label">{{ data.deptName }}</span>
              <el-tag size="small" :type="data.status === 1 ? 'success' : 'warning'">
                {{ data.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </div>
          </template>
        </el-tree>
      </div>

      <template #footer>
        <el-button @click="deptDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="deptDialog.saving" @click="submitDeptBindings">
          保存部门范围
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type FormRules,
  type TreeInstance,
} from 'element-plus'
import AuthSearchSection from '@/views/system/auth/components/AuthSearchSection.vue'
import {
  DEFAULT_BATCH_PAGE_SIZE,
  DEFAULT_PAGE_SIZE,
  PERMISSION_TYPE_OPTIONS,
  ROLE_DATA_SCOPE_OPTIONS,
  STATUS_OPTIONS,
} from '@/constants/admin'
import { getAppPageApi } from '@/api/app'
import { getRoleDeptsApi, updateRoleDeptsApi } from '@/api/binding'
import { getDeptTreeApi } from '@/api/dept'
import { getPermissionTreeApi } from '@/api/permission'
import {
  batchDeleteRolesApi,
  createRoleApi,
  deleteRoleApi,
  getMyRolesApi,
  getRoleDetailApi,
  getRolePageApi,
  getRoleUsersApi,
  updateRoleApi,
  updateRolePermissionsApi,
  updateRoleStatusApi,
  updateRoleUsersApi,
} from '@/api/role'
import { getUserPageApi } from '@/api/user'
import { showGlobalError } from '@/stores/globalError'
import { useUserStore } from '@/stores/user'
import type { TransferOption } from '@/types/common'
import type {
  AppDTO,
  DeptDTO,
  PermissionDTO,
  RoleDTO,
  RoleInfoVO,
  RolePageQueryDTO,
  UserDTO,
} from '@/types/system'
import { mapToTransferOptions, toPageResult, uniqueIds } from '@/utils/admin'
import { logoutAndRedirect } from '@/utils/auth'

type FormMode = 'create' | 'edit'
type RowCommand = 'users' | 'permissions' | 'depts' | 'delete'

interface RoleFormModel {
  id?: string
  roleName: string
  roleCode: string
  appCode: string
  dataScope: number
  status: number
  remark: string
}

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const permissionTreeRef = ref<TreeInstance>()
const deptTreeRef = ref<TreeInstance>()
const currentUserRoleIds = ref<string[]>([])
const editingRoleContext = ref<{
  appCode: string
  dataScope: number
  status: number
} | null>(null)

const isCurrentUserRole = (roleId?: string): boolean => {
  return Boolean(roleId && currentUserRoleIds.value.includes(roleId))
}

const hasCurrentUserRoleIntersection = (roleIds: string[]): boolean => {
  return roleIds.some((roleId) => currentUserRoleIds.value.includes(roleId))
}

const forceCurrentUserRoleRelogin = async (message: string) => {
  ElMessage.warning(message)
  await logoutAndRedirect({
    redirect: false,
    skipMessage: true,
  })
}

const loadCurrentUserRoles = async () => {
  if (!userStore.hasSession()) {
    currentUserRoleIds.value = []
    return
  }

  try {
    currentUserRoleIds.value = uniqueIds((await getMyRolesApi()).map((item) => item.id))
  } catch {
    currentUserRoleIds.value = []
  }
}

const loading = ref(false)
const total = ref(0)
const roleList = ref<RoleDTO[]>([])
const appOptions = ref<AppDTO[]>([])
const selectedRoleIds = ref<string[]>([])
const statusLoadingId = ref('')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<RoleDTO | null>(null)

const queryForm = reactive<RolePageQueryDTO>({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  searchKey: '',
})

const formDialog = reactive({
  visible: false,
  mode: 'create' as FormMode,
  submitting: false,
})

const formModel = reactive<RoleFormModel>({
  id: '',
  roleName: '',
  roleCode: '',
  appCode: 'sso',
  dataScope: 1,
  status: 1,
  remark: '',
})

const userDialog = reactive({
  visible: false,
  loading: false,
  saving: false,
  roleId: '',
  originalIds: [] as string[],
  selectedIds: [] as string[],
  options: [] as TransferOption[],
})

const permissionDialog = reactive({
  visible: false,
  loading: false,
  saving: false,
  roleId: '',
  treeData: [] as PermissionDTO[],
  filterText: '',
  treeKey: 0,
  expandAll: true,
})

const deptDialog = reactive({
  visible: false,
  loading: false,
  saving: false,
  roleId: '',
  treeData: [] as DeptDTO[],
})

const permissionTreeProps = {
  label: 'name',
  children: 'children',
}

const deptTreeProps = {
  label: 'deptName',
  children: 'children',
}

const formRules: FormRules<RoleFormModel> = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
}

const getDataScopeLabel = (value?: number) => {
  return ROLE_DATA_SCOPE_OPTIONS.find((item) => item.value === value)?.label || '全部数据'
}

const getPermissionTypeLabel = (value?: number) => {
  return PERMISSION_TYPE_OPTIONS.find((item) => item.value === value)?.label || '未知'
}

const resetFormDialog = () => {
  formDialog.submitting = false
  formModel.id = ''
  formModel.roleName = ''
  formModel.roleCode = ''
  formModel.appCode = 'sso'
  formModel.dataScope = 1
  formModel.status = 1
  formModel.remark = ''
  editingRoleContext.value = null
  formRef.value?.clearValidate()
}

const resetUserDialog = () => {
  userDialog.loading = false
  userDialog.saving = false
  userDialog.roleId = ''
  userDialog.originalIds = []
  userDialog.selectedIds = []
  userDialog.options = []
}

const resetPermissionDialog = () => {
  permissionDialog.loading = false
  permissionDialog.saving = false
  permissionDialog.roleId = ''
  permissionDialog.treeData = []
  permissionDialog.filterText = ''
  permissionDialog.treeKey = 0
  permissionDialog.expandAll = true
}

const resetDeptDialog = () => {
  deptDialog.loading = false
  deptDialog.saving = false
  deptDialog.roleId = ''
  deptDialog.treeData = []
}

const loadApps = async () => {
  try {
    const page = toPageResult(
      await getAppPageApi({
        pageNum: 1,
        pageSize: DEFAULT_BATCH_PAGE_SIZE,
        searchKey: '',
        userType: 1,
      }),
    )
    appOptions.value = page.records.filter((item) => item.userType !== 2)
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '加载应用列表失败' })
  }
}

const loadRoles = async () => {
  loading.value = true
  try {
    const page = toPageResult(await getRolePageApi(queryForm))
    roleList.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  loadRoles()
}

const resetQuery = () => {
  queryForm.searchKey = ''
  handleSearch()
}

const handleSelectionChange = (rows: RoleDTO[]) => {
  selectedRoleIds.value = uniqueIds(rows.map((item) => item.id))
}

const openDetailDrawer = async (row: RoleDTO) => {
  detailVisible.value = true
  detailLoading.value = true

  try {
    detailData.value = await getRoleDetailApi(row.id)
  } finally {
    detailLoading.value = false
  }
}

const openCreateDialog = () => {
  resetFormDialog()
  formDialog.mode = 'create'
  formDialog.visible = true
}

const openEditDialog = async (row: RoleDTO) => {
  resetFormDialog()
  formDialog.mode = 'edit'
  formDialog.visible = true

  try {
    const detail = await getRoleDetailApi(row.id)
    formModel.id = detail.id
    formModel.roleName = detail.roleName
    formModel.roleCode = detail.roleCode
    formModel.appCode = detail.appCode || 'sso'
    formModel.dataScope = detail.dataScope ?? 1
    formModel.status = detail.status ?? 1
    formModel.remark = detail.remark || ''
    editingRoleContext.value = {
      appCode: detail.appCode || 'sso',
      dataScope: detail.dataScope ?? 1,
      status: detail.status ?? 1,
    }
  } catch (error) {
    formDialog.visible = false
    showGlobalError(error, { fallbackMessage: '加载角色详情失败' })
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
    const payload: RoleDTO = {
      id: formModel.id || '0',
      roleName: formModel.roleName,
      roleCode: formModel.roleCode,
      appCode: formModel.appCode || 'sso',
      dataScope: formModel.dataScope,
      status: formModel.status,
      remark: formModel.remark || undefined,
    }

    if (formDialog.mode === 'create') {
      await createRoleApi(payload)
      ElMessage.success('角色创建成功')
    } else if (formModel.id) {
      await updateRoleApi(formModel.id, payload)
      ElMessage.success('角色更新成功')
    }

    formDialog.visible = false
    if (
      formDialog.mode === 'edit' &&
      formModel.id &&
      isCurrentUserRole(formModel.id) &&
      editingRoleContext.value &&
      (editingRoleContext.value.appCode !== (payload.appCode || 'sso') ||
        editingRoleContext.value.dataScope !== payload.dataScope ||
        editingRoleContext.value.status !== payload.status)
    ) {
      await forceCurrentUserRoleRelogin('当前账号角色配置已变更，请重新登录')
      return
    }
    await loadRoles()
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '保存角色失败' })
  } finally {
    formDialog.submitting = false
  }
}

const handleStatusChange = async (row: RoleDTO, nextStatus: number) => {
  const previousStatus = nextStatus === 1 ? 0 : 1
  statusLoadingId.value = row.id

  try {
    await updateRoleStatusApi(row.id, { status: nextStatus })
    row.status = nextStatus
    if (isCurrentUserRole(row.id)) {
      await forceCurrentUserRoleRelogin('当前账号角色状态已变更，请重新登录')
      return
    }
    ElMessage.success(nextStatus === 1 ? '角色已启用' : '角色已停用')
  } catch (error) {
    row.status = previousStatus
    showGlobalError(error, { fallbackMessage: '更新角色状态失败' })
  } finally {
    statusLoadingId.value = ''
  }
}

const confirmDelete = async (roleIds: string[], title: string) => {
  try {
    await ElMessageBox.confirm(title, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return false
  }

  if (roleIds.length === 1) {
    await deleteRoleApi(roleIds[0]!)
  } else {
    await batchDeleteRolesApi(roleIds)
  }

  return true
}

const handleDelete = async (row: RoleDTO) => {
  try {
    const confirmed = await confirmDelete([row.id], `确定删除角色“${row.roleName}”吗？`)
    if (!confirmed) return
    if (isCurrentUserRole(row.id)) {
      await forceCurrentUserRoleRelogin('当前账号角色已删除，请重新登录')
      return
    }
    ElMessage.success('角色已删除')
    await loadRoles()
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '删除角色失败' })
  }
}

const handleBatchDelete = async () => {
  if (!selectedRoleIds.value.length) return

  try {
    const confirmed = await confirmDelete(
      selectedRoleIds.value,
      `确定删除已选的 ${selectedRoleIds.value.length} 个角色吗？`,
    )
    if (!confirmed) return
    if (hasCurrentUserRoleIntersection(selectedRoleIds.value)) {
      selectedRoleIds.value = []
      await forceCurrentUserRoleRelogin('当前账号角色已删除，请重新登录')
      return
    }
    selectedRoleIds.value = []
    ElMessage.success('批量删除成功')
    await loadRoles()
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '批量删除角色失败' })
  }
}

const openUserDialog = async (row: RoleDTO) => {
  userDialog.visible = true
  userDialog.loading = true
  userDialog.roleId = row.id

  try {
    const [relation, page] = await Promise.all([
      getRoleUsersApi(row.id),
      getUserPageApi({
        pageNum: 1,
        pageSize: DEFAULT_BATCH_PAGE_SIZE,
        searchKey: '',
      }),
    ])

    userDialog.options = mapToTransferOptions(
      toPageResult<UserDTO>(page).records,
      (item) => item.id,
      (item) => `${item.nickname || item.username} (${item.username})${item.status === 0 ? ' [停用]' : ''}`,
      (item) => item.status === 0,
    )
    userDialog.selectedIds = uniqueIds((relation as RoleInfoVO).userIds || [])
    userDialog.originalIds = [...userDialog.selectedIds]
  } catch (error) {
    userDialog.visible = false
    showGlobalError(error, { fallbackMessage: '加载角色用户绑定失败' })
  } finally {
    userDialog.loading = false
  }
}

const submitUserBindings = async () => {
  if (!userDialog.roleId || userDialog.saving) return
  userDialog.saving = true

  try {
    const nextIds = uniqueIds(userDialog.selectedIds)
    const currentUserId = userStore.getCurrentUserId()
    const currentUserBindingChanged =
      Boolean(currentUserId) &&
      (userDialog.originalIds.includes(currentUserId) !== nextIds.includes(currentUserId))

    await updateRoleUsersApi(userDialog.roleId, nextIds)
    userDialog.visible = false
    if (currentUserBindingChanged) {
      await forceCurrentUserRoleRelogin('当前账号角色绑定已变更，请重新登录')
      return
    }
    ElMessage.success('角色用户绑定已更新')
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '保存角色用户绑定失败' })
  } finally {
    userDialog.saving = false
  }
}

const openPermissionDialog = async (row: RoleDTO) => {
  permissionDialog.visible = true
  permissionDialog.loading = true
  permissionDialog.roleId = row.id

  try {
    const [detail, tree] = await Promise.all([getRoleDetailApi(row.id), getPermissionTreeApi()])
    permissionDialog.treeData = tree
    permissionDialog.treeKey += 1

    await nextTick()
    const checkedIds = uniqueIds((detail.rolePermissions || []).map((item) => item.id))
    permissionTreeRef.value?.setCheckedKeys(checkedIds, false)
  } catch (error) {
    permissionDialog.visible = false
    showGlobalError(error, { fallbackMessage: '加载权限授权数据失败' })
  } finally {
    permissionDialog.loading = false
  }
}

const togglePermissionExpand = async () => {
  permissionDialog.expandAll = !permissionDialog.expandAll
  permissionDialog.treeKey += 1
  const checkedKeys = uniqueIds([
    ...((permissionTreeRef.value?.getCheckedKeys(false) as string[]) || []),
    ...((permissionTreeRef.value?.getHalfCheckedKeys() as string[]) || []),
  ])
  await nextTick()
  permissionTreeRef.value?.setCheckedKeys(checkedKeys, false)
}

const filterPermissionNode = (value: string, data: PermissionDTO) => {
  if (!value) return true
  return data.name.includes(value) || data.identification.includes(value)
}

const submitPermissionBindings = async () => {
  if (!permissionDialog.roleId || permissionDialog.saving) return
  permissionDialog.saving = true

  try {
    const checkedKeys = uniqueIds([
      ...((permissionTreeRef.value?.getCheckedKeys(false) as string[]) || []),
      ...((permissionTreeRef.value?.getHalfCheckedKeys() as string[]) || []),
    ])

    await updateRolePermissionsApi(
      permissionDialog.roleId,
      checkedKeys.map((permissionId) => ({ permissionId })),
    )
    permissionDialog.visible = false
    if (isCurrentUserRole(permissionDialog.roleId)) {
      await forceCurrentUserRoleRelogin('当前账号角色权限已变更，请重新登录')
      return
    }
    ElMessage.success('角色权限授权已更新')
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '保存角色权限失败' })
  } finally {
    permissionDialog.saving = false
  }
}

const openDeptDialog = async (row: RoleDTO) => {
  try {
    const detail = await getRoleDetailApi(row.id)
    if (detail.dataScope !== 5) {
      ElMessage.warning('只有“自定义数据范围”的角色才能绑定部门')
      return
    }

    deptDialog.visible = true
    deptDialog.loading = true
    deptDialog.roleId = row.id

    const [tree, checked] = await Promise.all([getDeptTreeApi(), getRoleDeptsApi(row.id)])
    deptDialog.treeData = tree
    await nextTick()
    deptTreeRef.value?.setCheckedKeys(uniqueIds(checked.map((item) => item.id)), false)
  } catch (error) {
    deptDialog.visible = false
    showGlobalError(error, { fallbackMessage: '加载角色部门范围失败' })
  } finally {
    deptDialog.loading = false
  }
}

const submitDeptBindings = async () => {
  if (!deptDialog.roleId || deptDialog.saving) return
  deptDialog.saving = true

  try {
    const checkedKeys = uniqueIds(deptTreeRef.value?.getCheckedKeys(false) as string[])
    await updateRoleDeptsApi(deptDialog.roleId, checkedKeys)
    deptDialog.visible = false
    if (isCurrentUserRole(deptDialog.roleId)) {
      await forceCurrentUserRoleRelogin('当前账号角色数据范围已变更，请重新登录')
      return
    }
    ElMessage.success('角色部门范围已更新')
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '保存角色部门范围失败' })
  } finally {
    deptDialog.saving = false
  }
}

const handleRowCommand = async (command: RowCommand, row: RoleDTO) => {
  switch (command) {
    case 'users':
      await openUserDialog(row)
      break
    case 'permissions':
      await openPermissionDialog(row)
      break
    case 'depts':
      await openDeptDialog(row)
      break
    case 'delete':
      await handleDelete(row)
      break
  }
}

watch(
  () => permissionDialog.filterText,
  (value) => {
    permissionTreeRef.value?.filter(value)
  },
)

onMounted(async () => {
  await Promise.all([loadApps(), loadRoles(), loadCurrentUserRoles()])
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

.binding-panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.binding-panel :deep(.el-transfer) {
  width: 100%;
}

.binding-panel :deep(.el-transfer-panel) {
  width: calc(50% - 12px);
}

.tree-toolbar {
  display: flex;
  gap: 12px;
}

.tree-toolbar > *:first-child {
  flex: 1;
}

.permission-tree {
  min-height: 320px;
  padding: 8px 12px;
  border: 1px solid var(--app-border);
  border-radius: 16px;
  background: var(--app-surface-muted);
}

.tree-node {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  padding-right: 8px;
}

.tree-node__label {
  color: var(--app-title);
  font-weight: 600;
}

.tree-node__meta {
  color: var(--app-muted);
  font-size: 12px;
}

.permission-chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.permission-chip {
  margin: 0;
}

@media (max-width: 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .binding-panel :deep(.el-transfer) {
    display: grid;
    gap: 12px;
  }

  .binding-panel :deep(.el-transfer__buttons) {
    justify-content: center;
  }

  .binding-panel :deep(.el-transfer-panel) {
    width: 100%;
  }

  .tree-toolbar {
    flex-direction: column;
  }
}
</style>


