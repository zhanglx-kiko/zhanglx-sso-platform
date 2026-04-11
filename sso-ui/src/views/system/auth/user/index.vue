<template>
  <div class="page-shell">
    <template v-if="canListUsers">
    <AuthSearchSection :model="queryForm">
        <template #toolbar>
          <el-button plain @click="loadUsers">刷新列表</el-button>
          <el-button v-permission="'user:batch-remove'" :disabled="!selectedUserIds.length" @click="handleBatchDelete">
            批量删除
          </el-button>
          <el-button v-permission="'user:add'" type="primary" @click="openCreateDialog">新增用户</el-button>
        </template>
        <el-form-item label="关键字">
          <el-input
            v-model="queryForm.searchKey"
            placeholder="用户名 / 昵称 / 手机号"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="账号名">
          <el-input
            v-model="queryForm.username"
            placeholder="请输入账号名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-tree-select
            v-model="queryForm.deptId"
            class="filter-tree-select"
            :data="deptTree"
            :props="deptTreeProps"
            check-strictly
            clearable
            node-key="id"
            value-key="id"
            placeholder="全部部门"
          />
        </el-form-item>
        <template #actions>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
        </template>
    </AuthSearchSection>

    <section v-memo="[loading, total, userList, selectedUserIds, statusLoadingId, queryForm.pageNum, queryForm.pageSize]" class="panel panel--table">
      <div class="panel-header">
        <div>
          <h2 class="panel-title">用户列表</h2>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="userList"
        row-key="id"
        class="user-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="52" />
        <el-table-column prop="username" label="账号" min-width="160" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="phoneNumber" label="手机号" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="200" show-overflow-tooltip />
        <el-table-column prop="birthday" label="生日" min-width="120">
          <template #default="{ row }">
            {{ row.birthday || '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="所属部门" min-width="160" show-overflow-tooltip />
        <el-table-column label="并发登录" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.allowConcurrentLogin === 1 ? 'success' : 'warning'">
              {{ row.allowConcurrentLogin === 1 ? '允许' : '互顶' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-switch
              v-permission="'user:status'"
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
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-space :size="10" wrap>
              <el-button v-permission="'user:view'" link type="primary" @click="openDetailDrawer(row)">详情</el-button>
              <el-button
                v-permission="{ all: ['user:view', 'user:edit'] }"
                link
                type="primary"
                @click="openEditDialog(row)"
              >
                编辑
              </el-button>
              <el-dropdown
                v-permission="{ any: ['user:assign-app', 'user:assign-post', 'role:bind-user', 'user:reset', 'user:remove'] }"
                @command="handleRowCommand($event, row)"
              >
                <el-button link type="primary">
                  更多
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-permission="{ all: ['user:assign-app', 'app:list'] }" command="apps">
                      绑定应用
                    </el-dropdown-item>
                    <el-dropdown-item v-permission="{ all: ['user:assign-post', 'post:list'] }" command="posts">
                      绑定岗位
                    </el-dropdown-item>
                    <el-dropdown-item
                      v-permission="{ all: ['role:bind-user', 'role:list', 'role:view'] }"
                      command="roles"
                    >
                      绑定角色
                    </el-dropdown-item>
                    <el-dropdown-item v-permission="'user:reset'" command="reset">重置密码</el-dropdown-item>
                    <el-dropdown-item v-permission="'user:remove'" command="delete" divided>删除用户</el-dropdown-item>
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
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </section>

    <el-drawer v-model="detailVisible" title="用户详情" size="520px">
      <div v-loading="detailLoading" class="detail-panel">
        <el-empty v-if="!detailLoading && !detailData" description="暂无详情数据" />
        <template v-else-if="detailData">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="账号">{{ detailData.username || '--' }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ detailData.nickname || '--' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">
              {{ detailData.phoneNumber || '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ detailData.email || '--' }}</el-descriptions-item>
            <el-descriptions-item label="所属部门">
              {{ detailData.deptName || '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="性别">
              {{ getGenderLabel(detailData.sex) }}
            </el-descriptions-item>
            <el-descriptions-item label="生日">
              {{ detailData.birthday || '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="并发登录">
              {{ detailData.allowConcurrentLogin === 1 ? '允许并发' : '禁止并发' }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="detailData.status === 1 ? 'success' : 'danger'">
                {{ detailData.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ detailData.createTime || '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ detailData.updateTime || '--' }}
            </el-descriptions-item>
          </el-descriptions>
        </template>
      </div>
    </el-drawer>

    <el-dialog
      v-model="formDialog.visible"
      :title="formDialog.mode === 'create' ? '新增用户' : '编辑用户'"
      width="680px"
      @close="resetFormDialog"
    >
      <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="96px">
        <div class="form-grid">
          <el-form-item label="账号" prop="username">
            <el-input
              v-model="formModel.username"
              :disabled="formDialog.mode === 'edit'"
              placeholder="请输入登录账号"
            />
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="formModel.nickname" placeholder="请输入用户昵称" />
          </el-form-item>
          <el-form-item label="手机号" prop="phoneNumber">
            <el-input v-model="formModel.phoneNumber" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formModel.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="性别" prop="sex">
            <el-select v-model="formModel.sex" placeholder="请选择性别">
              <el-option
                v-for="option in GENDER_OPTIONS"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="所属部门" prop="deptId">
            <el-tree-select
              v-model="formModel.deptId"
              :data="deptTree"
              :props="deptTreeProps"
              check-strictly
              clearable
              node-key="id"
              value-key="id"
              placeholder="请选择所属部门"
            />
          </el-form-item>
          <el-form-item label="头像地址" prop="avatar">
            <el-input v-model="formModel.avatar" placeholder="可选：头像 URL" />
          </el-form-item>
          <el-form-item label="生日" prop="birthday">
            <el-date-picker
              v-model="formModel.birthday"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择生日"
            />
          </el-form-item>
        </div>

        <el-form-item label="并发策略" prop="allowConcurrentLogin">
          <el-radio-group v-model="formModel.allowConcurrentLogin">
            <el-radio
              v-for="option in ALLOW_CONCURRENT_OPTIONS"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="formDialog.mode === 'create'" label="状态" prop="status">
          <el-radio-group v-model="formModel.status">
            <el-radio v-for="option in STATUS_OPTIONS" :key="option.value" :value="option.value">
              {{ option.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <p v-if="formDialog.mode === 'create'" class="soft-note">
          新增用户不传密码，后端会按系统参数 `default.password` 初始化。
        </p>
      </el-form>

      <template #footer>
        <el-button @click="formDialog.visible = false">取消</el-button>
        <el-button
          v-permission="formDialog.mode === 'create' ? 'user:add' : { all: ['user:view', 'user:edit'] }"
          type="primary"
          :loading="formDialog.submitting"
          @click="submitForm"
        >
          {{ formDialog.mode === 'create' ? '创建用户' : '保存修改' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="appDialog.visible"
      title="绑定应用"
      width="720px"
      @close="resetAppDialog"
    >
      <div v-loading="appDialog.loading" class="binding-panel">
        <p class="soft-note">仅展示系统用户应用，绑定值使用 `appCode`，不是应用记录主键。</p>
        <el-transfer
          v-model="appDialog.selectedCodes"
          filterable
          :data="appDialog.options"
          :titles="['候选应用', '已绑定应用']"
          :props="{ key: 'key', label: 'label', disabled: 'disabled' }"
        />
      </div>

      <template #footer>
        <el-button @click="appDialog.visible = false">取消</el-button>
        <el-button
          v-permission="{ all: ['user:assign-app', 'app:list'] }"
          type="primary"
          :loading="appDialog.saving"
          @click="submitAppBindings"
        >
          保存应用绑定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="postDialog.visible"
      title="绑定岗位"
      width="720px"
      @close="resetPostDialog"
    >
      <div v-loading="postDialog.loading" class="binding-panel">
        <p class="soft-note">岗位绑定值使用岗位主键，停用岗位会被置灰。</p>
        <el-transfer
          v-model="postDialog.selectedIds"
          filterable
          :data="postDialog.options"
          :titles="['候选岗位', '已绑定岗位']"
          :props="{ key: 'key', label: 'label', disabled: 'disabled' }"
        />
      </div>

      <template #footer>
        <el-button @click="postDialog.visible = false">取消</el-button>
        <el-button
          v-permission="{ all: ['user:assign-post', 'post:list'] }"
          type="primary"
          :loading="postDialog.saving"
          @click="submitPostBindings"
        >
          保存岗位绑定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="roleDialog.visible"
      title="绑定角色"
      width="760px"
      @close="resetRoleDialog"
    >
      <div v-loading="roleDialog.loading" class="binding-panel">
        <p class="soft-note">
          文档没有“用户直查角色”接口，这里按角色维度回查用户绑定并在保存时逐个回写。
        </p>
        <el-transfer
          v-model="roleDialog.selectedIds"
          filterable
          :data="roleDialog.options"
          :titles="['候选角色', '已绑定角色']"
          :props="{ key: 'key', label: 'label', disabled: 'disabled' }"
        />
      </div>

      <template #footer>
        <el-button @click="roleDialog.visible = false">取消</el-button>
        <el-button
          v-permission="{ all: ['role:bind-user', 'role:list', 'role:view'] }"
          type="primary"
          :loading="roleDialog.saving"
          @click="submitRoleBindings"
        >
          保存角色绑定
        </el-button>
      </template>
    </el-dialog>
    </template>
    <AuthNoPermissionPanel v-else description="当前账号暂无用户列表查看权限" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { usePermissionStore } from '@/stores/permission'
import AuthNoPermissionPanel from '@/views/system/auth/components/AuthNoPermissionPanel.vue'
import AuthSearchSection from '@/views/system/auth/components/AuthSearchSection.vue'
import {
  ALLOW_CONCURRENT_OPTIONS,
  DEFAULT_BATCH_PAGE_SIZE,
  DEFAULT_PAGE_SIZE,
  GENDER_OPTIONS,
  STATUS_OPTIONS,
} from '@/constants/admin'
import { getAppPageApi } from '@/api/app'
import { resetPasswordApi } from '@/api/auth'
import {
  getUserAppsApi,
  getUserPostsApi,
  updateUserAppsApi,
  updateUserPostsApi,
} from '@/api/binding'
import { getDeptTreeApi } from '@/api/dept'
import { getPostPageApi } from '@/api/post'
import { getRolePageApi, getRoleUsersApi, updateRoleUsersApi } from '@/api/role'
import {
  batchDeleteUsersApi,
  createUserApi,
  deleteUserApi,
  getUserDetailApi,
  getUserPageApi,
  updateUserApi,
  updateUserStatusApi,
} from '@/api/user'
import { showGlobalError } from '@/stores/globalError'
import { useUserStore } from '@/stores/user'
import type { TransferOption } from '@/types/common'
import type {
  AppDTO,
  DeptDTO,
  PostDTO,
  RoleDTO,
  UserCreateDTO,
  UserDTO,
  UserPageQueryDTO,
} from '@/types/system'
import { mapToTransferOptions, toPageResult, uniqueIds } from '@/utils/admin'
import { logoutAndRedirect } from '@/utils/auth'

type FormMode = 'create' | 'edit'
type RowCommand = 'apps' | 'posts' | 'roles' | 'reset' | 'delete'

interface UserFormModel extends UserCreateDTO {
  id?: string
  nickname: string
  phoneNumber: string
  email: string
  avatar: string
  birthday: string | null
  deptId: string
  allowConcurrentLogin: number
  sex: number
  status: number
}

const userStore = useUserStore()
const permissionStore = usePermissionStore()
const formRef = ref<FormInstance>()
const canListUsers = computed(() => permissionStore.hasPermission('user:list'))

const isCurrentSessionUser = (payload?: { id?: string | null; username?: string | null }): boolean => {
  return userStore.isCurrentUser(payload)
}

const forceCurrentUserRelogin = async (
  message: string,
  type: 'success' | 'warning' = 'warning',
) => {
  ElMessage[type](message)
  await logoutAndRedirect({
    redirect: false,
    skipMessage: true,
  })
}

const loading = ref(false)
const total = ref(0)
const userList = ref<UserDTO[]>([])
const deptTree = ref<DeptDTO[]>([])
const selectedUserIds = ref<string[]>([])
const statusLoadingId = ref('')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<UserDTO | null>(null)

const queryForm = reactive<UserPageQueryDTO>({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  searchKey: '',
  username: '',
  deptId: '',
})

const formDialog = reactive({
  visible: false,
  mode: 'create' as FormMode,
  submitting: false,
})

const formModel = reactive<UserFormModel>({
  id: '',
  username: '',
  nickname: '',
  phoneNumber: '',
  email: '',
  avatar: '',
  birthday: null,
  deptId: '',
  allowConcurrentLogin: 1,
  sex: 0,
  status: 1,
})

const appDialog = reactive({
  visible: false,
  loading: false,
  saving: false,
  userId: '',
  selectedCodes: [] as string[],
  options: [] as TransferOption[],
})

const postDialog = reactive({
  visible: false,
  loading: false,
  saving: false,
  userId: '',
  selectedIds: [] as string[],
  options: [] as TransferOption[],
})

const roleDialog = reactive({
  visible: false,
  loading: false,
  saving: false,
  userId: '',
  selectedIds: [] as string[],
  originalIds: [] as string[],
  options: [] as TransferOption[],
})

const deptTreeProps = {
  label: 'deptName',
  children: 'children',
  value: 'id',
}

const formRules: FormRules<UserFormModel> = {
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入用户昵称', trigger: 'blur' }],
}

const getGenderLabel = (value?: number) => {
  return GENDER_OPTIONS.find((item) => item.value === value)?.label || '未知'
}

const resetFormModel = () => {
  formModel.id = ''
  formModel.username = ''
  formModel.nickname = ''
  formModel.phoneNumber = ''
  formModel.email = ''
  formModel.avatar = ''
  formModel.birthday = null
  formModel.deptId = ''
  formModel.allowConcurrentLogin = 1
  formModel.sex = 0
  formModel.status = 1
}

const resetFormDialog = () => {
  formDialog.submitting = false
  resetFormModel()
  formRef.value?.clearValidate()
}

const resetAppDialog = () => {
  appDialog.loading = false
  appDialog.saving = false
  appDialog.userId = ''
  appDialog.selectedCodes = []
  appDialog.options = []
}

const resetPostDialog = () => {
  postDialog.loading = false
  postDialog.saving = false
  postDialog.userId = ''
  postDialog.selectedIds = []
  postDialog.options = []
}

const resetRoleDialog = () => {
  roleDialog.loading = false
  roleDialog.saving = false
  roleDialog.userId = ''
  roleDialog.selectedIds = []
  roleDialog.originalIds = []
  roleDialog.options = []
}

const loadDeptTree = async () => {
  if (!canListUsers.value) {
    deptTree.value = []
    return
  }

  try {
    deptTree.value = await getDeptTreeApi()
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '加载部门树失败' })
  }
}

const loadUsers = async () => {
  if (!canListUsers.value) {
    userList.value = []
    total.value = 0
    selectedUserIds.value = []
    return
  }

  loading.value = true
  try {
    const page = toPageResult(await getUserPageApi(queryForm))
    userList.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (!canListUsers.value) return
  queryForm.pageNum = 1
  loadUsers()
}

const resetQuery = () => {
  if (!canListUsers.value) return
  queryForm.searchKey = ''
  queryForm.username = ''
  queryForm.deptId = ''
  handleSearch()
}

const handleSelectionChange = (rows: UserDTO[]) => {
  selectedUserIds.value = uniqueIds(rows.map((item) => item.id))
}

const openDetailDrawer = async (row: UserDTO) => {
  if (!row.id) return
  detailVisible.value = true
  detailLoading.value = true

  try {
    detailData.value = await getUserDetailApi(row.id)
  } finally {
    detailLoading.value = false
  }
}

const openCreateDialog = () => {
  resetFormDialog()
  formDialog.mode = 'create'
  formDialog.visible = true
}

const openEditDialog = async (row: UserDTO) => {
  if (!row.id) return
  resetFormDialog()
  formDialog.mode = 'edit'
  formDialog.visible = true

  try {
    const detail = await getUserDetailApi(row.id)
    formModel.id = detail.id
    formModel.username = detail.username
    formModel.nickname = detail.nickname || ''
    formModel.phoneNumber = detail.phoneNumber || ''
    formModel.email = detail.email || ''
    formModel.avatar = detail.avatar || ''
    formModel.birthday = detail.birthday || null
    formModel.deptId = detail.deptId || ''
    formModel.allowConcurrentLogin = detail.allowConcurrentLogin ?? 1
    formModel.sex = detail.sex ?? 0
    formModel.status = detail.status ?? 1
  } catch (error) {
    formDialog.visible = false
    showGlobalError(error, { fallbackMessage: '加载用户详情失败' })
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
    if (formDialog.mode === 'create') {
      await createUserApi({
        username: formModel.username,
        nickname: formModel.nickname,
        avatar: formModel.avatar || undefined,
        phoneNumber: formModel.phoneNumber || undefined,
        sex: formModel.sex,
        birthday: formModel.birthday || undefined,
        email: formModel.email || undefined,
        allowConcurrentLogin: formModel.allowConcurrentLogin,
        deptId: formModel.deptId || undefined,
        status: formModel.status,
      })
      ElMessage.success('用户创建成功')
    } else if (formModel.id) {
      await updateUserApi(formModel.id, {
        nickname: formModel.nickname,
        avatar: formModel.avatar || undefined,
        phoneNumber: formModel.phoneNumber || undefined,
        sex: formModel.sex,
        birthday: formModel.birthday || undefined,
        email: formModel.email || undefined,
        allowConcurrentLogin: formModel.allowConcurrentLogin,
        deptId: formModel.deptId || undefined,
      })
      ElMessage.success('用户更新成功')
    }

    formDialog.visible = false
    await loadUsers()
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '保存用户失败' })
  } finally {
    formDialog.submitting = false
  }
}

const handleStatusChange = async (row: UserDTO, nextStatus: number) => {
  if (!row.id) return
  const previousStatus = nextStatus === 1 ? 0 : 1
  statusLoadingId.value = row.id

  try {
    await updateUserStatusApi(row.id, { status: nextStatus })
    row.status = nextStatus
    if (nextStatus !== 1 && isCurrentSessionUser(row)) {
      await forceCurrentUserRelogin('当前账号已停用，请重新登录')
      return
    }
    ElMessage.success(nextStatus === 1 ? '用户已启用' : '用户已停用')
  } catch (error) {
    row.status = previousStatus
    showGlobalError(error, { fallbackMessage: '更新用户状态失败' })
  } finally {
    statusLoadingId.value = ''
  }
}

const confirmDelete = async (userIds: string[], title: string) => {
  try {
    await ElMessageBox.confirm(title, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return false
  }

  if (userIds.length === 1) {
    await deleteUserApi(userIds[0]!)
  } else {
    await batchDeleteUsersApi(userIds)
  }

  return true
}

const handleDelete = async (row: UserDTO) => {
  if (!row.id) return

  try {
    const confirmed = await confirmDelete([row.id], `确定删除用户“${row.nickname || row.username}”吗？`)
    if (!confirmed) return
    if (isCurrentSessionUser(row)) {
      await forceCurrentUserRelogin('当前账号已删除，请重新登录')
      return
    }
    ElMessage.success('用户已删除')
    await loadUsers()
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '删除用户失败' })
  }
}

const handleBatchDelete = async () => {
  if (!selectedUserIds.value.length) return

  try {
    const confirmed = await confirmDelete(
      selectedUserIds.value,
      `确定删除已选的 ${selectedUserIds.value.length} 个用户吗？`,
    )
    if (!confirmed) return
    if (selectedUserIds.value.some((userId) => isCurrentSessionUser({ id: userId }))) {
      selectedUserIds.value = []
      await forceCurrentUserRelogin('当前账号已删除，请重新登录')
      return
    }
    selectedUserIds.value = []
    ElMessage.success('批量删除成功')
    await loadUsers()
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '批量删除失败' })
  }
}

const handleResetPassword = async (row: UserDTO) => {
  if (!row.id) return

  try {
    await ElMessageBox.confirm(
      `确定将“${row.nickname || row.username}”的密码重置为系统默认密码吗？`,
      '重置密码',
      {
        type: 'warning',
        confirmButtonText: '重置',
        cancelButtonText: '取消',
      },
    )
    await resetPasswordApi(row.id)
    if (isCurrentSessionUser(row)) {
      await forceCurrentUserRelogin('密码已重置，请重新登录', 'success')
      return
    }
    ElMessage.success('密码已重置，用户需重新登录')
  } catch (error) {
    if (error !== 'cancel') {
      showGlobalError(error, { fallbackMessage: '重置密码失败' })
    }
  }
}

const openAppDialog = async (row: UserDTO) => {
  if (!row.id) return
  appDialog.visible = true
  appDialog.loading = true
  appDialog.userId = row.id

  try {
    const [page, assignedApps] = await Promise.all([
      getAppPageApi({
        pageNum: 1,
        pageSize: DEFAULT_BATCH_PAGE_SIZE,
        searchKey: '',
        userType: 1,
      }),
      getUserAppsApi(row.id),
    ])

    const optionsSource = toPageResult<AppDTO>(page).records.filter((item) => item.userType !== 2)
    appDialog.options = mapToTransferOptions(
      optionsSource,
      (item) => item.appCode,
      (item) => `${item.appName} (${item.appCode})${item.status === 0 ? ' [停用]' : ''}`,
      (item) => item.status === 0,
    )
    appDialog.selectedCodes = uniqueIds(assignedApps.map((item) => item.appCode))
  } catch (error) {
    appDialog.visible = false
    showGlobalError(error, { fallbackMessage: '加载用户应用绑定失败' })
  } finally {
    appDialog.loading = false
  }
}

const submitAppBindings = async () => {
  if (!appDialog.userId || appDialog.saving) return
  appDialog.saving = true

  try {
    await updateUserAppsApi(appDialog.userId, uniqueIds(appDialog.selectedCodes))
    appDialog.visible = false
    if (isCurrentSessionUser({ id: appDialog.userId })) {
      await forceCurrentUserRelogin('当前账号应用权限已变更，请重新登录')
      return
    }
    ElMessage.success('应用绑定已更新')
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '保存应用绑定失败' })
  } finally {
    appDialog.saving = false
  }
}

const openPostDialog = async (row: UserDTO) => {
  if (!row.id) return
  postDialog.visible = true
  postDialog.loading = true
  postDialog.userId = row.id

  try {
    const [page, assignedPosts] = await Promise.all([
      getPostPageApi({
        pageNum: 1,
        pageSize: DEFAULT_BATCH_PAGE_SIZE,
        searchKey: '',
      }),
      getUserPostsApi(row.id),
    ])

    postDialog.options = mapToTransferOptions(
      toPageResult<PostDTO>(page).records,
      (item) => item.id,
      (item) => `${item.postName} (${item.postCode})${item.status === 0 ? ' [停用]' : ''}`,
      (item) => item.status === 0,
    )
    postDialog.selectedIds = uniqueIds(assignedPosts.map((item) => item.id))
  } catch (error) {
    postDialog.visible = false
    showGlobalError(error, { fallbackMessage: '加载岗位绑定失败' })
  } finally {
    postDialog.loading = false
  }
}

const submitPostBindings = async () => {
  if (!postDialog.userId || postDialog.saving) return
  postDialog.saving = true

  try {
    await updateUserPostsApi(postDialog.userId, uniqueIds(postDialog.selectedIds))
    postDialog.visible = false
    if (isCurrentSessionUser({ id: postDialog.userId })) {
      await forceCurrentUserRelogin('当前账号岗位绑定已变更，请重新登录')
      return
    }
    ElMessage.success('岗位绑定已更新')
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '保存岗位绑定失败' })
  } finally {
    postDialog.saving = false
  }
}

const openRoleDialog = async (row: UserDTO) => {
  if (!row.id) return
  roleDialog.visible = true
  roleDialog.loading = true
  roleDialog.userId = row.id

  try {
    const page = await getRolePageApi({
      pageNum: 1,
      pageSize: DEFAULT_BATCH_PAGE_SIZE,
      searchKey: '',
    })
    const roleRecords = toPageResult<RoleDTO>(page).records
    roleDialog.options = mapToTransferOptions(
      roleRecords,
      (item) => item.id,
      (item) => `${item.roleName} (${item.roleCode})${item.status === 0 ? ' [停用]' : ''}`,
      (item) => item.status === 0,
    )

    const assignedRoleIds = await Promise.all(
      roleRecords.map(async (item) => {
        try {
          const relation = await getRoleUsersApi(item.id)
          return relation.userIds?.includes(row.id) ? item.id : ''
        } catch {
          return ''
        }
      }),
    )

    roleDialog.selectedIds = uniqueIds(assignedRoleIds)
    roleDialog.originalIds = [...roleDialog.selectedIds]
  } catch (error) {
    roleDialog.visible = false
    showGlobalError(error, { fallbackMessage: '加载角色绑定失败' })
  } finally {
    roleDialog.loading = false
  }
}

const submitRoleBindings = async () => {
  if (!roleDialog.userId || roleDialog.saving) return

  const nextIds = uniqueIds(roleDialog.selectedIds)
  const changedIds = uniqueIds([...roleDialog.originalIds, ...nextIds]).filter(
    (id) => roleDialog.originalIds.includes(id) !== nextIds.includes(id),
  )

  if (!changedIds.length) {
    roleDialog.visible = false
    return
  }

  roleDialog.saving = true

  try {
    for (const roleId of changedIds) {
      const relation = await getRoleUsersApi(roleId)
      const mergedUserIds = new Set(relation.userIds || [])

      if (nextIds.includes(roleId)) {
        mergedUserIds.add(roleDialog.userId)
      } else {
        mergedUserIds.delete(roleDialog.userId)
      }

      await updateRoleUsersApi(roleId, Array.from(mergedUserIds))
    }

    roleDialog.originalIds = [...nextIds]
    roleDialog.visible = false
    if (isCurrentSessionUser({ id: roleDialog.userId })) {
      await forceCurrentUserRelogin('当前账号角色绑定已变更，请重新登录')
      return
    }
    ElMessage.success('角色绑定已更新')
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '保存角色绑定失败' })
  } finally {
    roleDialog.saving = false
  }
}

const handleRowCommand = async (command: RowCommand, row: UserDTO) => {
  switch (command) {
    case 'apps':
      await openAppDialog(row)
      break
    case 'posts':
      await openPostDialog(row)
      break
    case 'roles':
      await openRoleDialog(row)
      break
    case 'reset':
      await handleResetPassword(row)
      break
    case 'delete':
      await handleDelete(row)
      break
  }
}

onMounted(async () => {
  if (!canListUsers.value) {
    deptTree.value = []
    userList.value = []
    total.value = 0
    return
  }
  await Promise.all([loadDeptTree(), loadUsers()])
})
</script>

<style scoped>
.filter-tree-select,
.filter-tree-select :deep(.el-select__wrapper) {
  min-width: 220px;
}

.user-table :deep(.el-switch) {
  --el-switch-on-color: var(--app-success);
  --el-switch-off-color: #d0d7e2;
}

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
}
</style>


