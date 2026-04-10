<template>
  <div class="page-shell">
    <AppPageHeader
      :title="pageTitle"
      description="维护岗位编码、名称、排序和启停状态，作为用户岗位绑定的基础数据来源。"
      :stats="headerStats"
    >
      <template #actions>
        <el-button plain @click="loadPosts">刷新列表</el-button>
        <el-button :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
        <el-button type="primary" @click="openCreateDialog">新增岗位</el-button>
      </template>
    </AppPageHeader>

    <AuthSearchSection title="筛选条件" description="支持按岗位编码、岗位名称和状态查询。" :model="queryForm">
        <el-form-item label="关键字">
          <el-input v-model="queryForm.searchKey" placeholder="岗位编码 / 岗位名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="岗位编码">
          <el-input v-model="queryForm.postCode" placeholder="请输入岗位编码" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="岗位名称">
          <el-input v-model="queryForm.postName" placeholder="请输入岗位名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" clearable placeholder="全部状态">
            <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <template #actions>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
        </template>
    </AuthSearchSection>

    <section
      v-memo="[loading, total, postList, selectedIds, statusLoadingId, queryForm.pageNum, queryForm.pageSize]"
      class="panel panel--table"
    >
      <div class="panel-header">
        <div>
          <h2 class="panel-title">岗位列表</h2>
          <p class="panel-subtitle">岗位删除会校验是否仍有用户绑定。</p>
        </div>
      </div>

      <el-table v-loading="loading" :data="postList" row-key="id" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="52" />
        <el-table-column prop="postCode" label="岗位编码" min-width="160" />
        <el-table-column prop="postName" label="岗位名称" min-width="180" />
        <el-table-column prop="sortNum" label="排序" width="88" align="center" />
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
          @size-change="loadPosts"
          @current-change="loadPosts"
        />
      </div>
    </section>

    <el-drawer v-model="detailVisible" title="岗位详情" size="500px">
      <div v-loading="detailLoading" class="detail-panel">
        <el-empty v-if="!detailLoading && !detailData" description="暂无岗位详情" />
        <template v-else-if="detailData">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="岗位编码">{{ detailData.postCode }}</el-descriptions-item>
            <el-descriptions-item label="岗位名称">{{ detailData.postName }}</el-descriptions-item>
            <el-descriptions-item label="排序">{{ detailData.sortNum ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="detailData.status === 1 ? 'success' : 'danger'">
                {{ detailData.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </template>
      </div>
    </el-drawer>

    <el-dialog
      v-model="formDialog.visible"
      :title="formDialog.mode === 'create' ? '新增岗位' : '编辑岗位'"
      width="560px"
      @close="resetFormDialog"
    >
      <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="96px">
        <el-form-item label="岗位编码" prop="postCode">
          <el-input v-model="formModel.postCode" placeholder="请输入岗位编码" />
        </el-form-item>
        <el-form-item label="岗位名称" prop="postName">
          <el-input v-model="formModel.postName" placeholder="请输入岗位名称" />
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
          {{ formDialog.mode === 'create' ? '创建岗位' : '保存岗位' }}
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
import { DEFAULT_PAGE_SIZE, STATUS_OPTIONS } from '@/constants/admin'
import {
  batchDeletePostsApi,
  createPostApi,
  deletePostApi,
  getPostDetailApi,
  getPostPageApi,
  updatePostApi,
  updatePostStatusApi,
} from '@/api/post'
import { showGlobalError } from '@/stores/globalError'
import type { PostDTO, PostQueryDTO } from '@/types/system'
import { toPageResult, uniqueIds } from '@/utils/admin'

type FormMode = 'create' | 'edit'

interface PostFormModel {
  id?: string
  postCode: string
  postName: string
  sortNum: number
  status: number
}

const route = useRoute()
const formRef = ref<FormInstance>()

const loading = ref(false)
const total = ref(0)
const postList = ref<PostDTO[]>([])
const selectedIds = ref<string[]>([])
const statusLoadingId = ref('')

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<PostDTO | null>(null)

const queryForm = reactive<PostQueryDTO>({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  searchKey: '',
  postCode: '',
  postName: '',
  status: undefined,
})

const formDialog = reactive({
  visible: false,
  mode: 'create' as FormMode,
  submitting: false,
})

const formModel = reactive<PostFormModel>({
  id: '',
  postCode: '',
  postName: '',
  sortNum: 0,
  status: 1,
})

const formRules: FormRules<PostFormModel> = {
  postCode: [{ required: true, message: '请输入岗位编码', trigger: 'blur' }],
  postName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
}

const pageTitle = computed(() => String(route.meta.title || '岗位管理'))

const headerStats = computed(() => [
  { label: '岗位总量', value: total.value, hint: '按后端分页总数统计' },
  { label: '当前页启用', value: postList.value.filter((item) => item.status === 1).length, hint: '可绑定岗位' },
  { label: '当前页停用', value: postList.value.filter((item) => item.status === 0).length, hint: '停用岗位会在绑定时置灰' },
  { label: '排序容量', value: '0-9999', hint: '支持业务排序扩展' },
])

const resetFormDialog = () => {
  formDialog.submitting = false
  formModel.id = ''
  formModel.postCode = ''
  formModel.postName = ''
  formModel.sortNum = 0
  formModel.status = 1
  formRef.value?.clearValidate()
}

const loadPosts = async () => {
  loading.value = true

  try {
    const page = toPageResult(await getPostPageApi(queryForm))
    postList.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  loadPosts()
}

const resetQuery = () => {
  queryForm.searchKey = ''
  queryForm.postCode = ''
  queryForm.postName = ''
  queryForm.status = undefined
  handleSearch()
}

const handleSelectionChange = (rows: PostDTO[]) => {
  selectedIds.value = uniqueIds(rows.map((item) => item.id))
}

const openDetailDrawer = async (row: PostDTO) => {
  detailVisible.value = true
  detailLoading.value = true

  try {
    detailData.value = await getPostDetailApi(row.id)
  } finally {
    detailLoading.value = false
  }
}

const openCreateDialog = () => {
  resetFormDialog()
  formDialog.mode = 'create'
  formDialog.visible = true
}

const openEditDialog = async (row: PostDTO) => {
  resetFormDialog()
  formDialog.mode = 'edit'
  formDialog.visible = true

  try {
    const detail = await getPostDetailApi(row.id)
    formModel.id = detail.id
    formModel.postCode = detail.postCode
    formModel.postName = detail.postName
    formModel.sortNum = detail.sortNum ?? 0
    formModel.status = detail.status ?? 1
  } catch (error) {
    formDialog.visible = false
    showGlobalError(error, { fallbackMessage: '加载岗位详情失败' })
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
    const payload: PostDTO = {
      id: formModel.id || '0',
      postCode: formModel.postCode,
      postName: formModel.postName,
      sortNum: formModel.sortNum,
      status: formModel.status,
    }

    if (formDialog.mode === 'create') {
      await createPostApi(payload)
      ElMessage.success('岗位创建成功')
    } else if (formModel.id) {
      await updatePostApi(formModel.id, payload)
      ElMessage.success('岗位更新成功')
    }

    formDialog.visible = false
    await loadPosts()
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '保存岗位失败' })
  } finally {
    formDialog.submitting = false
  }
}

const handleStatusChange = async (row: PostDTO, nextStatus: number) => {
  const previousStatus = nextStatus === 1 ? 0 : 1
  statusLoadingId.value = row.id

  try {
    await updatePostStatusApi(row.id, { status: nextStatus })
    row.status = nextStatus
    ElMessage.success(nextStatus === 1 ? '岗位已启用' : '岗位已停用')
  } catch (error) {
    row.status = previousStatus
    showGlobalError(error, { fallbackMessage: '更新岗位状态失败' })
  } finally {
    statusLoadingId.value = ''
  }
}

const handleDelete = async (row: PostDTO) => {
  try {
    await ElMessageBox.confirm(`确定删除岗位“${row.postName}”吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deletePostApi(row.id)
    ElMessage.success('岗位已删除')
    await loadPosts()
  } catch (error) {
    if (error !== 'cancel') {
      showGlobalError(error, { fallbackMessage: '删除岗位失败' })
    }
  }
}

const handleBatchDelete = async () => {
  if (!selectedIds.value.length) return

  try {
    await ElMessageBox.confirm(`确定删除已选的 ${selectedIds.value.length} 个岗位吗？`, '批量删除', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await batchDeletePostsApi(selectedIds.value)
    selectedIds.value = []
    ElMessage.success('批量删除成功')
    await loadPosts()
  } catch (error) {
    if (error !== 'cancel') {
      showGlobalError(error, { fallbackMessage: '批量删除岗位失败' })
    }
  }
}

onMounted(async () => {
  await loadPosts()
})
</script>

<style scoped>
.detail-panel {
  min-height: 240px;
}
</style>
